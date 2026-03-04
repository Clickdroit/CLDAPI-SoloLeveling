package fr.clickdroit.sololeveling.role;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.Power;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Classe abstraite de base pour tous les rôles.
 * Fournit des implémentations par défaut et des utilitaires.
 */
public abstract class AbstractRole implements Role {

    protected final SoloLevelingPlugin plugin;
    protected final List<Power> powers;
    protected UUID ownerUuid;

    public AbstractRole() {
        this.plugin = SoloLevelingPlugin.getInstance();
        this.powers = new ArrayList<>();
        initPowers();
    }

    /**
     * Initialise les pouvoirs du rôle.
     * À surcharger dans les classes enfants.
     */
    protected abstract void initPowers();

    /**
     * Ajoute un pouvoir au rôle.
     */
    protected void addPower(Power power) {
        powers.add(power);
    }

    @Override
    public List<Power> getPowers() {
        return powers;
    }

    /**
     * Obtient les informations du rôle depuis l'annotation.
     */
    protected RoleInfo getRoleInfo() {
        return getClass().getAnnotation(RoleInfo.class);
    }

    @Override
    public String getName() {
        RoleInfo info = getRoleInfo();
        return info != null ? info.name() : getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        RoleInfo info = getRoleInfo();
        return info != null ? info.description() : "Aucune description.";
    }

    @Override
    public Camp getCamp() {
        RoleInfo info = getRoleInfo();
        return info != null ? info.camp() : Camp.NEUTRAL;
    }

    @Override
    public RoleRarity getRarity() {
        RoleInfo info = getRoleInfo();
        return info != null ? info.rarity() : RoleRarity.COMMON;
    }

    @Override
    public String[] getLore() {
        RoleInfo info = getRoleInfo();
        return info != null ? info.lore() : new String[0];
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(getIconMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(getCamp().getColorCode() + getName());

        List<String> lore = new ArrayList<>();
        lore.add("§7" + getDescription());
        lore.add("");
        lore.add("§8Camp: " + getCamp().getColoredName());
        lore.add("§8Rareté: " + getRarity().getColoredName());
        lore.add("");
        lore.addAll(Arrays.asList(getLore()));

        if (!powers.isEmpty()) {
            lore.add("");
            lore.add("§6Pouvoirs:");
            for (Power power : powers) {
                lore.add("§8• §e" + power.getName());
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Retourne le matériau de l'icône.
     * Peut être surchargé par les classes enfants.
     */
    protected Material getIconMaterial() {
        return Material.PAPER;
    }

    // === Implémentations par défaut des événements ===

    @Override
    public void onRoleAssigned(Player player) {
        this.ownerUuid = player.getUniqueId();
        for (Power power : powers) {
            power.onRoleAssigned(player);
        }
    }

    @Override
    public void onRoleReveal(Player player) {
        // Afficher les informations du rôle
        player.sendMessage("");
        player.sendMessage("§8§l§m                                                §r");
        player.sendMessage("");
        player.sendMessage("  §7Vous êtes: " + getCamp().getColorCode() + "§l" + getName());
        player.sendMessage("  " + getRarity().getStars());
        player.sendMessage("");
        player.sendMessage("  §7Camp: " + getCamp().getColoredName());
        player.sendMessage("  §7" + getDescription());
        player.sendMessage("");

        if (!powers.isEmpty()) {
            player.sendMessage("  §6Vos pouvoirs:");
            for (Power power : powers) {
                player.sendMessage("  §8• §e" + power.getName() + " §7- " + power.getDescription());
            }
            player.sendMessage("");
        }

        player.sendMessage("  §7Objectif: §f" + getCamp().getObjective());
        player.sendMessage("");
        player.sendMessage("§8§l§m                                                §r");
        player.sendMessage("");

        for (Power power : powers) {
            power.onRoleReveal(player);
        }
    }

    @Override
    public void onDeath(Player player, Player killer) {
        for (Power power : powers) {
            power.onDeath(player, killer);
        }
    }

    @Override
    public void onKill(Player player, Player victim) {
        for (Power power : powers) {
            power.onKill(player, victim);
        }
    }

    @Override
    public void onNight() {
        for (Power power : powers) {
            power.onNight();
        }
    }

    @Override
    public void onDay() {
        for (Power power : powers) {
            power.onDay();
        }
    }

    @Override
    public void onEpisode(int episode) {
        for (Power power : powers) {
            power.onEpisode(episode);
        }
    }

    @Override
    public void onTick(int gameTime) {
        for (Power power : powers) {
            power.onTick(gameTime);
        }
    }

    @Override
    public void onGameStart() {
        for (Power power : powers) {
            power.onGameStart();
        }
    }

    @Override
    public void onGameEnd() {
        for (Power power : powers) {
            power.onGameEnd();
        }
    }

    /**
     * Obtient le joueur propriétaire de ce rôle.
     */
    protected Player getOwner() {
        if (ownerUuid == null)
            return null;
        return plugin.getServer().getPlayer(ownerUuid);
    }

    /**
     * Envoie un message à tous les joueurs du même camp.
     */
    protected void sendCampMessage(String message) {
        for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
            if (rp.getRole() != null && rp.getRole().getCamp() == getCamp()) {
                Player p = plugin.getServer().getPlayer(rp.getUuid());
                if (p != null) {
                    p.sendMessage(getCamp().getColorCode() + "[" + getCamp().getDisplayName() + "] §f" + message);
                }
            }
        }
    }
}
