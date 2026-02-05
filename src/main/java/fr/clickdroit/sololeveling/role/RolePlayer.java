package fr.clickdroit.sololeveling.role;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Représente un joueur avec son rôle attribué.
 */
public class RolePlayer {

    private final UUID uuid;
    private final String name;
    private Role role;
    private boolean alive;
    private boolean roleRevealed;

    // Statistiques
    private int kills;
    private int powersUsed;

    public RolePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.role = null;
        this.alive = true;
        this.roleRevealed = false;
        this.kills = 0;
        this.powersUsed = 0;
    }

    public RolePlayer(Player player) {
        this(player.getUniqueId(), player.getName());
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isRoleRevealed() {
        return roleRevealed;
    }

    public void setRoleRevealed(boolean roleRevealed) {
        this.roleRevealed = roleRevealed;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getPowersUsed() {
        return powersUsed;
    }

    public void addPowerUsed() {
        this.powersUsed++;
    }

    /**
     * Vérifie si le joueur a un rôle assigné.
     */
    public boolean hasRole() {
        return role != null;
    }

    /**
     * Obtient le nom coloré du joueur selon son camp.
     */
    public String getColoredName() {
        if (role == null) {
            return "§7" + name;
        }
        return role.getCamp().getColorCode() + name;
    }

    /**
     * Obtient le préfixe du joueur pour le chat.
     */
    public String getChatPrefix() {
        if (role == null) {
            return "§7";
        }
        if (!roleRevealed) {
            return "§7";
        }
        return role.getCamp().getColorCode() + "[" + role.getName() + "] ";
    }
}
