package fr.clickdroit.sololeveling.camp;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Énumération des camps (factions) dans Solo Leveling UHC.
 * Chaque camp a ses propres objectifs et conditions de victoire.
 */
public enum Camp {

    /**
     * Camp des Chasseurs - Les protecteurs de l'humanité
     * Objectif: Éliminer les Monarques et survivre
     */
    HUNTERS(
            "Chasseurs",
            "§a",
            ChatColor.GREEN,
            Material.DIAMOND_SWORD,
            "Éliminez les Monarques pour protéger l'humanité.",
            new String[] {
                    "§7Les Chasseurs sont les protecteurs de l'humanité.",
                    "§7Ils doivent éliminer les Monarques pour gagner.",
                    "§7Travaillez ensemble pour vaincre le mal!"
            }),

    /**
     * Camp des Monarques - Les destructeurs
     * Objectif: Détruire tous les Chasseurs
     */
    MONARCHS(
            "Monarques",
            "§c",
            ChatColor.RED,
            Material.DRAGON_EGG,
            "Détruisez tous les Chasseurs et dominez le monde.",
            new String[] {
                    "§7Les Monarques sont des êtres de destruction.",
                    "§7Leur but est d'anéantir l'humanité.",
                    "§7Éliminez tous les Chasseurs pour gagner!"
            }),

    /**
     * Camp des Dirigeants - Les êtres de lumière
     * Objectif: Protéger les Chasseurs et vaincre les Monarques
     */
    RULERS(
            "Dirigeants",
            "§b",
            ChatColor.AQUA,
            Material.NETHER_STAR,
            "Guidez les Chasseurs vers la victoire.",
            new String[] {
                    "§7Les Dirigeants sont des êtres de lumière.",
                    "§7Ils travaillent dans l'ombre pour aider les Chasseurs.",
                    "§7Gagnez avec les Chasseurs!"
            }),

    /**
     * Camp Neutre - Objectifs personnels
     * Objectif: Variable selon le rôle
     */
    NEUTRAL(
            "Neutre",
            "§7",
            ChatColor.GRAY,
            Material.COMPASS,
            "Accomplissez votre objectif personnel.",
            new String[] {
                    "§7Vous n'appartenez à aucun camp.",
                    "§7Votre objectif dépend de votre rôle.",
                    "§7Survivez et atteignez votre but!"
            });

    private final String displayName;
    private final String colorCode;
    private final ChatColor chatColor;
    private final Material icon;
    private final String objective;
    private final String[] lore;

    Camp(String displayName, String colorCode, ChatColor chatColor,
            Material icon, String objective, String[] lore) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.chatColor = chatColor;
        this.icon = icon;
        this.objective = objective;
        this.lore = lore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public Material getIcon() {
        return icon;
    }

    public String getObjective() {
        return objective;
    }

    public String[] getLore() {
        return lore;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    /**
     * Vérifie si ce camp est allié avec un autre camp.
     */
    public boolean isAlliedWith(Camp other) {
        if (this == other)
            return true;

        // Les Dirigeants sont alliés aux Chasseurs
        if ((this == RULERS && other == HUNTERS) ||
                (this == HUNTERS && other == RULERS)) {
            return true;
        }

        return false;
    }

    /**
     * Vérifie si ce camp est ennemi d'un autre camp.
     */
    public boolean isEnemyOf(Camp other) {
        if (this == NEUTRAL || other == NEUTRAL)
            return false;
        return !isAlliedWith(other);
    }
}
