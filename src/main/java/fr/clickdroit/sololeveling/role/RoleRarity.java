package fr.clickdroit.sololeveling.role;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Rareté des rôles.
 * Affecte la probabilité d'apparition et l'affichage.
 */
public enum RoleRarity {

    COMMON("Commun", "§7", ChatColor.GRAY, Material.IRON_INGOT, 40),
    UNCOMMON("Peu commun", "§a", ChatColor.GREEN, Material.EMERALD, 30),
    RARE("Rare", "§9", ChatColor.BLUE, Material.DIAMOND, 20),
    EPIC("Épique", "§5", ChatColor.DARK_PURPLE, Material.OBSIDIAN, 8),
    LEGENDARY("Légendaire", "§6", ChatColor.GOLD, Material.GOLD_BLOCK, 2);

    private final String displayName;
    private final String colorCode;
    private final ChatColor chatColor;
    private final Material icon;
    private final int weight; // Poids pour la distribution

    RoleRarity(String displayName, String colorCode, ChatColor chatColor,
            Material icon, int weight) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.chatColor = chatColor;
        this.icon = icon;
        this.weight = weight;
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

    public int getWeight() {
        return weight;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    /**
     * Obtient les étoiles représentant la rareté.
     */
    public String getStars() {
        switch (this) {
            case COMMON:
                return "§7★";
            case UNCOMMON:
                return "§a★§a★";
            case RARE:
                return "§9★§9★§9★";
            case EPIC:
                return "§5★§5★§5★§5★";
            case LEGENDARY:
                return "§6★§6★§6★§6★§6★";
            default:
                return "§7★";
        }
    }
}
