package fr.clickdroit.sololeveling.stats;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestionnaire des statistiques de partie.
 * Centralise le suivi et l'affichage des performances des joueurs.
 */
public class StatsManager {

    private final SoloLevelingPlugin plugin;
    private final Map<UUID, PlayerStats> playerStats;
    private long gameStartTime;
    private boolean gameActive;

    public StatsManager(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
        this.playerStats = new HashMap<>();
        this.gameActive = false;
    }

    /**
     * Initialise les statistiques pour une nouvelle partie.
     */
    public void startGame(Collection<UUID> players) {
        playerStats.clear();
        gameStartTime = System.currentTimeMillis();
        gameActive = true;

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                playerStats.put(uuid, new PlayerStats(uuid, player.getName()));
            }
        }

        plugin.getLogger().info("Statistiques initialisées pour " + playerStats.size() + " joueurs.");
    }

    /**
     * Termine la partie et génère le rapport final.
     */
    public void endGame() {
        gameActive = false;
        broadcastFinalStats();
    }

    /**
     * Réinitialise toutes les statistiques.
     */
    public void reset() {
        playerStats.clear();
        gameActive = false;
    }

    // ===== Getters =====

    /**
     * Obtient les statistiques d'un joueur.
     */
    public PlayerStats getStats(UUID uuid) {
        return playerStats.get(uuid);
    }

    /**
     * Obtient les statistiques d'un joueur, en les créant si nécessaire.
     */
    public PlayerStats getOrCreateStats(UUID uuid, String name) {
        return playerStats.computeIfAbsent(uuid, u -> new PlayerStats(u, name));
    }

    /**
     * Obtient toutes les statistiques.
     */
    public Collection<PlayerStats> getAllStats() {
        return playerStats.values();
    }

    /**
     * Obtient le temps de jeu total.
     */
    public long getGameDuration() {
        return System.currentTimeMillis() - gameStartTime;
    }

    /**
     * Formate la durée de jeu.
     */
    public String getFormattedGameDuration() {
        long seconds = getGameDuration() / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    // ===== Enregistrement d'événements =====

    /**
     * Enregistre un kill.
     */
    public void recordKill(UUID killer, UUID victim) {
        PlayerStats killerStats = playerStats.get(killer);
        PlayerStats victimStats = playerStats.get(victim);

        if (killerStats != null) {
            killerStats.addKill();
        }
        if (victimStats != null) {
            victimStats.addDeath();
        }
    }

    /**
     * Enregistre des dégâts infligés.
     */
    public void recordDamage(UUID attacker, UUID victim, double damage) {
        PlayerStats attackerStats = playerStats.get(attacker);
        PlayerStats victimStats = playerStats.get(victim);

        if (attackerStats != null) {
            attackerStats.addDamageDealt(damage);
        }
        if (victimStats != null) {
            victimStats.addDamageTaken(damage);
        }
    }

    /**
     * Enregistre l'utilisation d'un pouvoir.
     */
    public void recordPowerUse(UUID player, boolean successful) {
        PlayerStats stats = playerStats.get(player);
        if (stats != null) {
            stats.addPowerUsed(successful);
        }
    }

    /**
     * Enregistre une aide à un allié.
     */
    public void recordAllyHelped(UUID player) {
        PlayerStats stats = playerStats.get(player);
        if (stats != null) {
            stats.addAllyHelped();
        }
    }

    // ===== Classements =====

    /**
     * Obtient le classement par kills.
     */
    public List<PlayerStats> getKillLeaderboard() {
        return playerStats.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getKills).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtient le classement par score de performance.
     */
    public List<PlayerStats> getPerformanceLeaderboard() {
        return playerStats.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::calculatePerformanceScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtient le classement par dégâts infligés.
     */
    public List<PlayerStats> getDamageLeaderboard() {
        return playerStats.values().stream()
                .sorted(Comparator.comparingDouble(PlayerStats::getDamageDealt).reversed())
                .collect(Collectors.toList());
    }

    // ===== Affichage =====

    /**
     * Diffuse les statistiques finales à tous les joueurs.
     */
    public void broadcastFinalStats() {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§5§l━━━━━━ STATISTIQUES FINALES ━━━━━━");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7Durée de la partie: §f" + getFormattedGameDuration());
        Bukkit.broadcastMessage("");

        // Top 3 des kills
        List<PlayerStats> killBoard = getKillLeaderboard();
        Bukkit.broadcastMessage("  §c§l⚔ TOP KILLS");
        int rank = 1;
        for (PlayerStats stats : killBoard) {
            if (rank > 3) break;
            String medal = getMedal(rank);
            Bukkit.broadcastMessage("    " + medal + " §f" + stats.getPlayerName() + " §7- §c" + stats.getKills() + " kills");
            rank++;
        }
        Bukkit.broadcastMessage("");

        // Top 3 des scores
        List<PlayerStats> scoreBoard = getPerformanceLeaderboard();
        Bukkit.broadcastMessage("  §e§l★ TOP PERFORMANCE");
        rank = 1;
        for (PlayerStats stats : scoreBoard) {
            if (rank > 3) break;
            String medal = getMedal(rank);
            Bukkit.broadcastMessage("    " + medal + " §f" + stats.getPlayerName() + " §7- §e" + stats.calculatePerformanceScore() + " pts");
            rank++;
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("");
    }

    /**
     * Affiche les statistiques personnelles à un joueur.
     */
    public void showPersonalStats(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            player.sendMessage("§cAucune statistique disponible.");
            return;
        }

        player.sendMessage("");
        player.sendMessage("§5§l━━━━━━ VOS STATISTIQUES ━━━━━━");
        player.sendMessage("");
        player.sendMessage("  §7Temps de survie: §f" + stats.getFormattedSurvivalTime());
        player.sendMessage("");
        player.sendMessage("  §c⚔ Combat:");
        player.sendMessage("    §7Kills: §f" + stats.getKills());
        player.sendMessage("    §7Deaths: §f" + stats.getDeaths());
        player.sendMessage("    §7K/D: §f" + String.format("%.2f", stats.getKDRatio()));
        player.sendMessage("    §7Dégâts infligés: §f" + String.format("%.1f", stats.getDamageDealt()));
        player.sendMessage("    §7Dégâts reçus: §f" + String.format("%.1f", stats.getDamageTaken()));
        player.sendMessage("");
        player.sendMessage("  §6⚡ Pouvoirs:");
        player.sendMessage("    §7Utilisations: §f" + stats.getPowersUsed());
        player.sendMessage("    §7Taux de réussite: §f" + String.format("%.1f%%", stats.getPowerSuccessRate()));
        player.sendMessage("");
        player.sendMessage("  §e★ Score de performance: §f" + stats.calculatePerformanceScore());
        player.sendMessage("");
        player.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
    }

    /**
     * Retourne l'emoji de médaille selon le rang.
     * Utilise des caractères texte pour la compatibilité Minecraft.
     */
    private String getMedal(int rank) {
        switch (rank) {
            case 1: return "§6[1er]";
            case 2: return "§7[2e]";
            case 3: return "§c[3e]";
            default: return "§7[" + rank + "e]";
        }
    }
}
