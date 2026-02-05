package fr.clickdroit.sololeveling.power;

/**
 * Types de pouvoirs disponibles.
 */
public enum PowerType {

    /**
     * Pouvoir actif - Doit être activé manuellement.
     */
    ACTIVE("Actif", "§e", "Clic droit pour activer"),

    /**
     * Pouvoir passif - Toujours actif.
     */
    PASSIVE("Passif", "§a", "Toujours actif"),

    /**
     * Pouvoir ultime - Très puissant, utilisation limitée.
     */
    ULTIMATE("Ultime", "§6", "Pouvoir très puissant"),

    /**
     * Pouvoir de nuit - Disponible uniquement la nuit.
     */
    NIGHT("Nocturne", "§9", "Disponible uniquement la nuit"),

    /**
     * Pouvoir de jour - Disponible uniquement le jour.
     */
    DAY("Diurne", "§e", "Disponible uniquement le jour");

    private final String displayName;
    private final String colorCode;
    private final String description;

    PowerType(String displayName, String colorCode, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getDescription() {
        return description;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }
}
