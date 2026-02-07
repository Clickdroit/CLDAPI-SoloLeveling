package fr.clickdroit.sololeveling.config;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.role.Role;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RoleManager;
import fr.clickdroit.sololeveling.role.RoleRarity;
import fr.clickdroit.sololeveling.power.Power;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI pour afficher et configurer les rôles d'un camp spécifique.
 */
public class RoleCampGUI implements Listener {

    private final SoloLevelingPlugin plugin;
    private static final String GUI_PREFIX = "§5§l⚔ §dRôles: ";

    public RoleCampGUI(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Ouvre le GUI pour un camp spécifique
     */
    public void open(Player player, Camp camp) {
        String title = GUI_PREFIX + camp.getColorCode() + camp.getDisplayName();
        RoleCampHolder holder = new RoleCampHolder(camp);
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);
        RoleManager rm = plugin.getRoleManager();

        // Bordure décorative
        ItemStack glass = createItem(Material.STAINED_GLASS_PANE, getCampGlassColor(camp), " ");
        for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 50, 51, 52, 53 }) {
            inv.setItem(i, glass);
        }

        // Remplir avec les rôles du camp
        int slot = 9;
        for (Class<? extends Role> roleClass : rm.getRegisteredRoles()) {
            try {
                Role role = roleClass.newInstance();
                if (role.getCamp() == camp) {
                    boolean enabled = rm.isRoleEnabled(role.getName());
                    inv.setItem(slot, createRoleItem(role, enabled));
                    slot++;
                    if (slot == 45)
                        break; // Limite
                }
            } catch (Exception ignored) {
            }
        }

        // Bouton retour
        inv.setItem(49, createItem(
                Material.ARROW,
                (short) 0,
                "§c§l← Retour",
                "",
                "§7Retourner au menu principal"));

        player.openInventory(inv);
    }

    private short getCampGlassColor(Camp camp) {
        switch (camp) {
            case HUNTERS:
                return 11; // Bleu
            case MONARCHS:
                return 14; // Rouge
            case RULERS:
                return 4; // Jaune
            case NEUTRAL:
                return 7; // Gris
            default:
                return 15; // Noir
        }
    }

    private ItemStack createRoleItem(Role role, boolean enabled) {
        ItemStack item = role.getIcon().clone();
        ItemMeta meta = item.getItemMeta();

        // Ajouter le statut enabled/disabled
        String status = enabled ? "§a✔ Activé" : "§c✖ Désactivé";
        String displayName = (enabled ? "§a" : "§c") + role.getName();
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + role.getDescription());
        lore.add("");
        lore.add("§8Camp: " + role.getCamp().getColoredName());
        lore.add("§8Rareté: " + role.getRarity().getColoredName());
        lore.add("");
        lore.add("§7Statut: " + status);
        lore.add("");

        if (role.getPowers() != null && !role.getPowers().isEmpty()) {
            lore.add("§6Pouvoirs:");
            for (Power power : role.getPowers()) {
                lore.add("§8• §e" + power.getName());
            }
            lore.add("");
        }

        lore.add(enabled ? "§e▶ Clic pour désactiver" : "§e▶ Clic pour activer");
        lore.add("§e▶ Shift+Clic pour détails");

        meta.setLore(lore);
        item.setItemMeta(meta);

        // Ajouter un indicateur visuel (enchantement si activé)
        if (enabled) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createItem(Material material, short durability, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1, durability);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        try {
            if (event.getClickedInventory() == null)
                return;
            if (!(event.getInventory().getHolder() instanceof RoleCampHolder))
                return;

            event.setCancelled(true);

            RoleCampHolder holder = (RoleCampHolder) event.getInventory().getHolder();
            Camp currentCamp = holder.getCamp();

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();
            ItemStack clicked = event.getCurrentItem();

            if (clicked == null || clicked.getType() == Material.AIR)
                return;
            if (clicked.getType() == Material.STAINED_GLASS_PANE)
                return;

            // Bouton retour
            if (slot == 49) {
                plugin.getRoleConfigGUI().open(player);
                return;
            }

            // Clic sur un rôle
            if (slot >= 9 && slot < 45 && clicked.hasItemMeta()) {
                String displayName = clicked.getItemMeta().getDisplayName();
                String roleName = extractRoleName(displayName);

                if (roleName != null) {
                    RoleManager rm = plugin.getRoleManager();

                    if (event.isShiftClick()) {
                        // Ouvrir les détails du rôle
                        plugin.getRoleDetailGUI().open(player, roleName, currentCamp);
                    } else {
                        // Toggle enable/disable
                        boolean currentState = rm.isRoleEnabled(roleName);
                        rm.setRoleEnabled(roleName, !currentState);

                        String status = !currentState ? "§aactivé" : "§cdésactivé";
                        player.sendMessage("§5§l[RÔLES] §fRôle §e" + roleName + " §f" + status + "§f!");

                        // Refresh
                        open(player, currentCamp);
                    }
                } else {
                    plugin.getLogger().warning("Impossible d'extraire le nom du rôle depuis: " + displayName);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors du clic dans RoleCampGUI:");
            e.printStackTrace();
            if (event.getWhoClicked() instanceof Player) {
                ((Player) event.getWhoClicked())
                        .sendMessage("§cUne erreur est survenue lors de l'interaction avec le menu.");
            }
        }
    }

    private String extractRoleName(String displayName) {
        if (displayName == null)
            return null;
        // Retirer les codes couleur au début (§a ou §c)
        if (displayName.length() > 2 && displayName.charAt(0) == '§') {
            return displayName.substring(2);
        }
        return displayName;
    }

    public static class RoleCampHolder implements InventoryHolder {
        private final Camp camp;
        private Inventory inventory;

        public RoleCampHolder(Camp camp) {
            this.camp = camp;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        public Camp getCamp() {
            return camp;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
