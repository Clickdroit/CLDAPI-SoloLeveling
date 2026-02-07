package fr.clickdroit.sololeveling.scoreboard;

import fr.clickdroit.api.API;
import fr.clickdroit.api.common.scoreboard.ScoreboardContents;
import fr.clickdroit.api.game.GameManager;
import fr.clickdroit.api.game.GameState;
import fr.clickdroit.api.game.GameUtils;
import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.Power;
import fr.clickdroit.sololeveling.role.Role;
import fr.clickdroit.sololeveling.role.RolePlayer;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Contenu du scoreboard personnalisé pour Solo Leveling.
 * Affiche les informations spécifiques au mode de jeu.
 */
public class SoloLevelingScoreboardContents implements ScoreboardContents {

    private final API api;
    private final SoloLevelingPlugin plugin;
    private final GameManager gameManager;

    // Données en cache
    private int playersAlive;
    private int totalPlayers;
    private int episode;
    private String dayNight;
    private int huntersAlive;
    private int monarchsAlive;
    private int rulersAlive;
    private int neutralAlive;

    public SoloLevelingScoreboardContents(API api, SoloLevelingPlugin plugin) {
        this.api = api;
        this.plugin = plugin;
        this.gameManager = api.getGameManager();
    }

    @Override
    public void reloadData(UUID playerUUID) {
        this.totalPlayers = GameUtils.getPlayerAmount();
        this.playersAlive = 0;
        this.huntersAlive = 0;
        this.monarchsAlive = 0;
        this.rulersAlive = 0;
        this.neutralAlive = 0;

        // Compter les joueurs vivants par camp
        for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
            if (rp.isAlive()) {
                playersAlive++;
                if (rp.getRole() != null) {
                    switch (rp.getRole().getCamp()) {
                        case HUNTERS:
                            huntersAlive++;
                            break;
                        case MONARCHS:
                            monarchsAlive++;
                            break;
                        case RULERS:
                            rulersAlive++;
                            break;
                        case NEUTRAL:
                            neutralAlive++;
                            break;
                    }
                }
            }
        }

        // Épisode et jour/nuit
        this.episode = plugin.getRoleManager().getCurrentEpisode();
        long time = Bukkit.getWorlds().get(0).getTime();
        this.dayNight = (time >= 0 && time < 12300) ? "§eJour ☀" : "§9Nuit ☾";
    }

    @Override
    public void setLines(BPlayerBoard board, UUID playerUUID, String ip) {
        GameState state = gameManager.getGameState();
        int line = 14;

        // Titre animé
        board.setName("§5§l⚔ §dSOLO LEVELING");

        // Ligne vide
        board.set("§1", line--);

        if (state == GameState.WAITING || state == GameState.STARTING) {
            // Scoreboard d'attente
            setWaitingLines(board, line, playerUUID);
        } else {
            // Scoreboard en jeu
            setGameLines(board, line, playerUUID);
        }
    }

    private void setWaitingLines(BPlayerBoard board, int line, UUID playerUUID) {
        board.set(" §8» §fJoueurs: §d" + totalPlayers + "§f/§d" + gameManager.getGameConfig().getGameSlot(), line--);
        board.set("§2", line--);

        // Afficher le rôle si déjà attribué
        RolePlayer rp = plugin.getRoleManager().getRolePlayer(playerUUID);
        if (rp != null && rp.getRole() != null) {
            board.set(" §8» §fVotre rôle:", line--);
            board.set("   " + rp.getRole().getCamp().getColorCode() + rp.getRole().getName(), line--);
        } else {
            board.set(" §8» §fRôle: §7En attente...", line--);
        }

        board.set("§3", line--);
        board.set(" §8» §fMode: §dSolo Leveling", line--);
        board.set("§4", line--);
    }

    private void setGameLines(BPlayerBoard board, int line, UUID playerUUID) {
        // Épisode et temps
        board.set(" §8» §fÉpisode: §d" + episode, line--);
        board.set(" §8» §f" + dayNight, line--);

        // Timer de révélation des rôles
        int countdown = plugin.getGameModule().getInternalModule().getRoleRevealCountdown();
        if (countdown > 0) {
            board.set(" §8» §fRôle dans: §e" + formatTime(countdown), line--);
        }

        board.set("§2", line--);

        // Joueurs en vie
        board.set(" §8» §fEn vie: §a" + playersAlive + "§f/§7" + totalPlayers, line--);
        board.set("§3", line--);

        // Camps (seulement afficher les camps avec des joueurs)
        if (huntersAlive > 0) {
            board.set(" §8» §bChasseurs: §f" + huntersAlive, line--);
        }
        if (monarchsAlive > 0) {
            board.set(" §8» §cMonarques: §f" + monarchsAlive, line--);
        }
        if (rulersAlive > 0) {
            board.set(" §8» §eDirigeants: §f" + rulersAlive, line--);
        }
        if (neutralAlive > 0) {
            board.set(" §8» §7Neutres: §f" + neutralAlive, line--);
        }

        board.set("§4", line--);

        // Rôle du joueur
        RolePlayer rp = plugin.getRoleManager().getRolePlayer(playerUUID);
        if (rp != null && rp.getRole() != null) {
            Role role = rp.getRole();
            board.set(" §8» §fRôle:", line--);
            board.set("   " + role.getCamp().getColorCode() + role.getName(), line--);

            // Afficher les pouvoirs et leurs cooldowns
            if (role.getPowers() != null && !role.getPowers().isEmpty()) {
                board.set("§5", line--);
                board.set(" §8» §6Pouvoirs:", line--);
                for (Power power : role.getPowers()) {
                    String status;
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (power.canUse(player)) {
                        status = "§a✔";
                    } else {
                        int remaining = power.getRemainingCooldown(player);
                        if (remaining > 0) {
                            status = "§c" + remaining + "s";
                        } else {
                            status = "§c✖";
                        }
                    }
                    board.set("   §7" + power.getName() + " " + status, line--);
                    if (line < 1)
                        break;
                }
            }
        } else {
            board.set(" §8» §fRôle: §7???", line--);
        }

        // Ligne finale
        if (line > 0) {
            board.set("§6", line--);
        }
    }

    /**
     * Formate le temps en secondes en format lisible.
     */
    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return minutes + "m " + secs + "s";
        }
    }
}
