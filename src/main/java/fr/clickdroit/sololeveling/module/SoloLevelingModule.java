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
 */
public class SoloLevelingModule extends Modules {

    private final SoloLevelingPlugin plugin;
    private int roleRevealTime = 60; // Révélation des rôles après 60 secondes

    public SoloLevelingModule(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {
        plugin.getLogger().info("Module Solo Leveling chargé!");
    }

    @Override
    public void onStart(API api) {
        super.onStart(api);

        // Récupérer les joueurs en jeu
        List<UUID> inGamePlayers = new ArrayList<>(api.getGameManager().getInGamePlayers());

        // Distribuer les rôles
        plugin.getRoleManager().distributeRoles(inGamePlayers);

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
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            revealRoles();
        }, roleRevealTime * 20L);
    }

    private void revealRoles() {
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

        // Notifier le killer
        if (killer != null) {
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
            plugin.getCampManager().announceVictory(winner);
        }
    }

    @Override
    public void onClockUpdate(int gameTime) {
        // Mettre à jour les rôles chaque seconde
        plugin.getRoleManager().onTick(gameTime);
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
        // Récupérer l'épisode actuel depuis l'API
        // plugin.getRoleManager().onEpisode(episode);
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
    }

    @Override
    public void openConfig(Player player) {
        new fr.clickdroit.sololeveling.config.RoleConfigMainGUI(plugin).open(player);
    }
}
