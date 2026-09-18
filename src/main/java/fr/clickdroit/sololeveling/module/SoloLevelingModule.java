package fr.clickdroit.sololeveling.module;

import fr.clickdroit.api.API;

import fr.clickdroit.api.module.Modules;
import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.role.RolePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Module principal Solo Leveling qui s'intègre avec CLDAPI.
 * Ce module gère le mode de jeu basé sur l'univers Solo Leveling.
 */
public class SoloLevelingModule extends Modules {

    private static final int DEFAULT_ROLE_REVEAL_TIME = 60;

    private final SoloLevelingPlugin plugin;

    private int minPlayersToStart = 4; // Nombre minimum de joueurs
    private int roleRevealCountdown = -1; // -1 = pas encore démarré ou déjà révélé
    private int roleRevealTime = DEFAULT_ROLE_REVEAL_TIME; // Révélation des rôles après 60 secondes
    private org.bukkit.scheduler.BukkitTask revealTask;

    public SoloLevelingModule(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Retourne le nom d'affichage du module.
     * 
     * @return Le nom du module
     */
    public String getDisplayName() {
        return "§5Solo Leveling";
    }

    @Override
    public void onLoad() {
        plugin.getLogger().info("Module Solo Leveling chargé!");
        loadConfiguration();
    }

    private void loadConfiguration() {
        if (plugin.getConfig().contains("timers.role_reveal")) {
            this.roleRevealTime = plugin.getConfig().getInt("timers.role_reveal");
        }
        if (plugin.getConfig().contains("timers.min_players")) {
            this.minPlayersToStart = plugin.getConfig().getInt("timers.min_players");
        }
    }

    private void saveConfiguration() {
        plugin.getConfig().set("timers.role_reveal", this.roleRevealTime);
        plugin.getConfig().set("timers.min_players", this.minPlayersToStart);
        plugin.saveConfig();
    }

    @Override
    public void onStart(API api) {
        super.onStart(api);
        api.getCommon().getScoreboardManager().setScoreboardContents(() -> new fr.clickdroit.sololeveling.scoreboard.SoloLevelingScoreboardContents(api, plugin));

        // Récupérer les joueurs en jeu
        List<UUID> inGamePlayers = new ArrayList<>(api.getGameManager().getInGamePlayers());

        // Initialiser les statistiques
        plugin.getStatsManager().startGame(inGamePlayers);

        // Distribuer les rôles
        plugin.getRoleManager().distributeRoles(inGamePlayers);

        // Démarrer le countdown pour le scoreboard
        this.roleRevealCountdown = roleRevealTime;

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §5§lSOLO LEVELING UHC");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7Les rôles seront révélés dans §e" + roleRevealTime + " secondes§7.");
        Bukkit.broadcastMessage("  §7Préparez-vous au combat!");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("");

        // Programmer la révélation des rôles
        if (this.revealTask != null) {
            this.revealTask.cancel();
        }
        this.revealTask = Bukkit.getScheduler().runTaskLater(plugin, this::revealRoles, roleRevealTime * 20L);
    }

    private void revealRoles() {
        // Reset countdown
        this.revealTask = null;
        this.roleRevealCountdown = -1;

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§5§l⚡ RÉVÉLATION DES RÔLES ⚡");
        Bukkit.broadcastMessage("");

        plugin.getRoleManager().revealAllRoles();

        // Son de révélation
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 0.5F);
        }
    }

    @Override
    public void onPlayerDeath(Player player, Player killer) {
        super.onPlayerDeath(player, killer);

        RolePlayer rp = plugin.getRoleManager().getRolePlayer(player.getUniqueId());
        if (rp != null) {
            rp.setAlive(false);

            // Notifier le rôle de la mort
            if (rp.getRole() != null) {
                rp.getRole().onDeath(player, killer);

                // Révéler le rôle à la mort
                Bukkit.broadcastMessage("§7" + player.getName() + " était: " +
                        rp.getRole().getCamp().getColorCode() + rp.getRole().getName());
            }
        }

        // Enregistrer les statistiques
        if (killer != null) {
            plugin.getStatsManager().recordKill(killer.getUniqueId(), player.getUniqueId());

            RolePlayer killerRp = plugin.getRoleManager().getRolePlayer(killer.getUniqueId());
            if (killerRp != null && killerRp.getRole() != null) {
                killerRp.addKill();
                killerRp.getRole().onKill(killer, player);
            }
        }

        // Vérifier les conditions de victoire
        checkWinConditions();
    }

    @Override
    public void onPlayerDieByDisconnect(UUID uuid) {
        super.onPlayerDieByDisconnect(uuid);

        RolePlayer rp = plugin.getRoleManager().getRolePlayer(uuid);
        if (rp != null) {
            rp.setAlive(false);
        }

        checkWinConditions();
    }

    private void checkWinConditions() {
        Camp winner = plugin.getCampManager().checkWinCondition();
        if (winner != null) {
            // Terminer les statistiques avant d'annoncer la victoire
            plugin.getStatsManager().endGame();
            plugin.getCampManager().announceVictory(winner);
        }
    }

    /**
     * Réinitialise le module pour une nouvelle partie.
     */
    public void reset() {
        plugin.getRoleManager().reset();
        plugin.getStatsManager().reset();
        roleRevealTime = DEFAULT_ROLE_REVEAL_TIME;
        if (this.revealTask != null) {
            this.revealTask.cancel();
            this.revealTask = null;
        }
        plugin.getLogger().info("Module Solo Leveling réinitialisé.");
    }

    @Override
    public void onClockUpdate(int gameTime) {
        // Mettre à jour les rôles chaque seconde
        plugin.getRoleManager().onTick(gameTime);

        // Décrémenter le countdown de révélation
        if (roleRevealCountdown > 0) {
            roleRevealCountdown--;
        }
    }

    @Override
    public void onDay(boolean sendMessage) {
        super.onDay(sendMessage);
        plugin.getRoleManager().onDay();
    }

    @Override
    public void onNight(boolean sendMessage) {
        super.onNight(sendMessage);
        plugin.getRoleManager().onNight();
    }

    @Override
    public void onEpisodeSwitch() {
        int episode = plugin.getRoleManager().getCurrentEpisode() + 1;
        plugin.getRoleManager().onEpisode(episode);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("  §5§lÉPISODE " + episode + " §7— Le monde change...");
        Bukkit.broadcastMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("");
    }

    @Override
    public void init() {
        // Initialisation
    }

    @Override
    public void onPlayerReconnect(Player player) {
        RolePlayer rp = plugin.getRoleManager().getRolePlayer(player.getUniqueId());
        if (rp != null && rp.getRole() != null && rp.isRoleRevealed()) {
            // Réafficher le rôle au joueur qui se reconnecte
            player.sendMessage("§7Rappel: Vous êtes " +
                    rp.getRole().getCamp().getColorCode() + rp.getRole().getName());
        }
    }

    @Override
    public void onPlayerDisconnect(Player player) {
        // Géré par l'API parente
    }

    @Override
    public void onPlayerChat(Player player, String message) {
        RolePlayer rp = plugin.getRoleManager().getRolePlayer(player.getUniqueId());

        String prefix = "§7";
        if (rp != null && rp.getRole() != null && rp.isRoleRevealed()) {
            prefix = rp.getChatPrefix();
        }

        // Format du chat: [Rôle] Joueur > Message
        String formattedMessage = prefix + player.getName() + " §8> §f" + message;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(formattedMessage);
        }
    }

    public void setRoleRevealTime(int seconds) {
        this.roleRevealTime = seconds;
        saveConfiguration();
    }

    public int getRoleRevealTime() {
        return roleRevealTime;
    }

    public void setMinPlayersToStart(int minPlayers) {
        this.minPlayersToStart = minPlayers;
        saveConfiguration();
    }

    public int getMinPlayersToStart() {
        return minPlayersToStart;
    }

    public int getRoleRevealCountdown() {
        return roleRevealCountdown;
    }

    @Override
    public void openConfig(Player player) {
        plugin.getConfigGUI().open(player);
    }
}
