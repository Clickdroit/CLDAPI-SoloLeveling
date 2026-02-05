package fr.clickdroit.sololeveling.util;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Utilitaire pour l'envoi de messages formatés.
 * Centralise la mise en forme des messages pour assurer une cohérence visuelle.
 */
public final class MessageUtils {

    // Préfixes standards
    public static final String PREFIX = "§5§l[SL] §r";
    public static final String PREFIX_SUCCESS = "§a§l[✔] §r";
    public static final String PREFIX_ERROR = "§c§l[✖] §r";
    public static final String PREFIX_WARNING = "§e§l[!] §r";
    public static final String PREFIX_INFO = "§b§l[i] §r";

    // Séparateurs
    public static final String SEPARATOR = "§8§l§m                                                §r";
    public static final String SEPARATOR_THIN = "§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

    private MessageUtils() {
        // Classe utilitaire, pas d'instanciation
    }

    // ===== Messages simples =====

    /**
     * Envoie un message avec le préfixe Solo Leveling.
     */
    public static void send(Player player, String message) {
        player.sendMessage(PREFIX + message);
    }

    /**
     * Envoie un message de succès.
     */
    public static void sendSuccess(Player player, String message) {
        player.sendMessage(PREFIX_SUCCESS + "§a" + message);
    }

    /**
     * Envoie un message d'erreur.
     */
    public static void sendError(Player player, String message) {
        player.sendMessage(PREFIX_ERROR + "§c" + message);
    }

    /**
     * Envoie un message d'avertissement.
     */
    public static void sendWarning(Player player, String message) {
        player.sendMessage(PREFIX_WARNING + "§e" + message);
    }

    /**
     * Envoie un message d'information.
     */
    public static void sendInfo(Player player, String message) {
        player.sendMessage(PREFIX_INFO + "§7" + message);
    }

    // ===== Messages de broadcast =====

    /**
     * Diffuse un message à tous les joueurs.
     */
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(PREFIX + message);
    }

    /**
     * Diffuse un message important à tous les joueurs avec un séparateur.
     */
    public static void broadcastImportant(String... lines) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(SEPARATOR_THIN);
        Bukkit.broadcastMessage("");
        for (String line : lines) {
            Bukkit.broadcastMessage("  " + line);
        }
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(SEPARATOR_THIN);
        Bukkit.broadcastMessage("");
    }

    /**
     * Diffuse un message à une collection de joueurs.
     */
    public static void broadcast(Collection<Player> players, String message) {
        for (Player player : players) {
            if (player != null && player.isOnline()) {
                player.sendMessage(PREFIX + message);
            }
        }
    }

    // ===== Messages avec sons =====

    /**
     * Envoie un message de succès avec un son.
     */
    public static void sendSuccessWithSound(Player player, String message) {
        sendSuccess(player, message);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.5F);
    }

    /**
     * Envoie un message d'erreur avec un son.
     */
    public static void sendErrorWithSound(Player player, String message) {
        sendError(player, message);
        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
    }

    /**
     * Envoie un message d'avertissement avec un son.
     */
    public static void sendWarningWithSound(Player player, String message) {
        sendWarning(player, message);
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 0.5F);
    }

    // ===== Formatage de texte =====

    /**
     * Formate un texte avec un titre centré.
     */
    public static String formatTitle(String title) {
        return "§5§l━━━━━ " + title + " ━━━━━";
    }

    /**
     * Formate une liste d'items.
     */
    public static String formatListItem(String item) {
        return "  §8• §7" + item;
    }

    /**
     * Formate une paire clé-valeur.
     */
    public static String formatKeyValue(String key, String value) {
        return "§7" + key + ": §f" + value;
    }

    /**
     * Formate une durée en secondes en format lisible.
     */
    public static String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        if (remainingSeconds == 0) {
            return minutes + "min";
        }
        return minutes + "min " + remainingSeconds + "s";
    }

    /**
     * Formate un pourcentage.
     */
    public static String formatPercent(double value) {
        return String.format("%.1f%%", value * 100);
    }

    /**
     * Crée une barre de progression.
     */
    public static String createProgressBar(double progress, int length, String filledChar, String emptyChar) {
        int filled = (int) Math.round(progress * length);
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append(filledChar);
            } else {
                bar.append("§7").append(emptyChar);
            }
        }
        return bar.toString();
    }

    /**
     * Crée une barre de progression simple avec des caractères par défaut.
     */
    public static String createProgressBar(double progress, int length) {
        return createProgressBar(progress, length, "█", "░");
    }

    // ===== Couleurs conditionnelles =====

    /**
     * Retourne une couleur basée sur un pourcentage de santé.
     */
    public static String getHealthColor(double healthPercent) {
        if (healthPercent > 0.6) {
            return "§a";
        } else if (healthPercent > 0.3) {
            return "§e";
        } else {
            return "§c";
        }
    }

    /**
     * Formate une santé avec la bonne couleur.
     */
    public static String formatHealth(double health, double maxHealth) {
        double percent = health / maxHealth;
        return getHealthColor(percent) + String.format("%.1f", health) + "§7/§f" + String.format("%.1f", maxHealth);
    }
}
