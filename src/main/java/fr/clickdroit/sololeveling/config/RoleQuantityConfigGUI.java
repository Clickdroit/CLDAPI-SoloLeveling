package fr.clickdroit.sololeveling.config;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.role.Role;
import fr.clickdroit.sololeveling.role.RoleManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI pour configurer la quantité de chaque rôle.
 * Affiche les rôles avec leur quantité comme nombre d'items dans le stack.
 * - Clic gauche: +1 quantité
 * - Clic droit: -1 quantité
 * - Shift+Clic: Toggle enable/disable
 */
public class RoleQuantityConfigGUI implements Listener {

    private final SoloLevelingPlugin plugin;
    private static final String GUI_NAME = "§5§l⚔ §dQuantité des Rôles";
    private static final int MAX_QUANTITY = 64;

    public RoleQuantityConfigGUI(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Ouvre le GUI de configuration des quantités
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_NAME);
        RoleManager rm = plugin.getRoleManager();

        // Bordure décorative haut et bas
        ItemStack glass = createItem(Material.STAINED_GLASS_PANE, (short) 10, " ");
        for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 50, 51, 52, 53 }) {
            inv.setItem(i, glass);
        }

        // Remplir avec les rôles
        int slot = 9;
        for (Class<? extends Role> roleClass : rm.getRegisteredRoles()) {
            if (slot >= 45)
                break;

            try {
                Role role = roleClass.newInstance();
                String roleName = role.getName();
                boolean enabled = rm.isRoleEnabled(roleName);
                int quantity = rm.getRoleQuantity(roleName);

                inv.setItem(slot, createRoleItem(role, enabled, quantity));
                slot++;
            } catch (Exception ignored) {
            }
        }

        // Légende/Instructions
        inv.setItem(4, createItem(
                Material.BOOK,
                (short) 0,
                "§6§lInstructions",
                "",
                " §8» §eClic gauche §7: +1 quantité",
                " §8» §eClic droit §7: -1 quantité",
                " §8» §eShift+Clic §7: Toggle ON/OFF",
                "",
                " §8» §fLa quantité affichée",
                " §8» §fest le nombre de fois",
                " §8» §fque le rôle sera distribué."));

        // Statistiques
        int totalRoles = 0;
        int totalQuantity = 0;
        for (Class<? extends Role> roleClass : rm.getRegisteredRoles()) {
            try {
                Role role = roleClass.newInstance();
                if (rm.isRoleEnabled(role.getName())) {
                    totalRoles++;
                    totalQuantity += rm.getRoleQuantity(role.getName());
                }
            } catch (Exception ignored) {
            }
        }

        inv.setItem(49, createItem(
                Material.PAPER,
                (short) 0,
                "§e§lStatistiques",
                "",
                " §8» §fRôles activés: §a" + totalRoles,
                " §8» §fPlaces totales: §b" + totalQuantity,
                "",
                " §7Le nombre de places correspond",
                " §7au nombre maximum de joueurs",
                " §7qui peuvent recevoir un rôle."));

        // Bouton retour
        inv.setItem(45, createItem(
                Material.ARROW,
                (short) 0,
                "§c§l← Retour",
                "",
                "§7Retourner au menu de config"));

        player.openInventory(inv);
    }

    private ItemStack createRoleItem(Role role, boolean enabled, int quantity) {
        ItemStack icon = role.getIcon().clone();

        // Quantité visible comme nombre d'items
        int displayQuantity = enabled ? Math.max(1, Math.min(quantity, MAX_QUANTITY)) : 1;
        icon.setAmount(displayQuantity);

        ItemMeta meta = icon.getItemMeta();

        // Nom avec statut
        String statusIcon = enabled ? "§a✔" : "§c✖";
        String colorCode = role.getCamp().getColorCode();
        meta.setDisplayName(colorCode + role.getName() + " " + statusIcon);

        // Lore
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + role.getDescription());
        lore.add("");
        lore.add("§8Camp: " + role.getCamp().getColoredName());
        lore.add("§8Rareté: " + role.getRarity().getColoredName());
        lore.add("");

        if (enabled) {
            lore.add("§7Quantité: §e" + quantity);
            lore.add("");
            lore.add("§a▶ Clic gauche §7: +1");
            lore.add("§c▶ Clic droit §7: -1");
            lore.add("§7▶ Shift+Clic: Désactiver");
        } else {
            lore.add("§cDésactivé");
            lore.add("");
            lore.add("§7▶ Shift+Clic: Activer");
        }

        meta.setLore(lore);

        // Enchantement visuel si activé
        if (enabled) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        icon.setItemMeta(meta);
        return icon;
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
        if (!event.getView().getTitle().equals(GUI_NAME))
            return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();
        ClickType clickType = event.getClick();

        if (clicked == null || clicked.getType() == Material.AIR)
            return;
        if (clicked.getType() == Material.STAINED_GLASS_PANE)
            return;

        // Bouton retour
        if (slot == 45) {
            plugin.getConfigGUI().open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
            return;
        }

        // Ignorer les items décoratifs
        if (slot < 9 || slot >= 45)
            return;
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName())
            return;

        // Extraire le nom du rôle
        String roleName = extractRoleName(clicked.getItemMeta().getDisplayName());
        if (roleName == null)
            return;

        RoleManager rm = plugin.getRoleManager();
        boolean enabled = rm.isRoleEnabled(roleName);

        if (clickType.isShiftClick()) {
            // Toggle enable/disable
            rm.setRoleEnabled(roleName, !enabled);
            String status = !enabled ? "§aactivé" : "§cdésactivé";
            player.sendMessage("§5§l[RÔLES] §fRôle §e" + roleName + " §f" + status + "§f!");
            player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
        } else if (enabled) {
            int quantity = rm.getRoleQuantity(roleName);

            if (clickType.isLeftClick()) {
                // Augmenter la quantité
                int newQuantity = Math.min(quantity + 1, MAX_QUANTITY);
                rm.setRoleQuantity(roleName, newQuantity);
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.5F, 1.2F);
            } else if (clickType.isRightClick()) {
                // Diminuer la quantité
                int newQuantity = Math.max(quantity - 1, 1);
                rm.setRoleQuantity(roleName, newQuantity);
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.5F, 0.8F);
            }
        } else {
            player.sendMessage("§c§l[RÔLES] §fActivez d'abord le rôle avec Shift+Clic!");
        }

        // Rafraîchir l'inventaire
        open(player);
    }

    private String extractRoleName(String displayName) {
        if (displayName == null)
            return null;

        // Le format est: "§X<nom> §a✔" ou "§X<nom> §c✖"
        // On doit retirer le code couleur du début et le statut à la fin
        String name = displayName;

        // Retirer le statut à la fin (espace + §X + symbole)
        int lastSpace = name.lastIndexOf(' ');
        if (lastSpace > 0) {
            name = name.substring(0, lastSpace);
        }

        // Retirer le code couleur au début
        if (name.length() > 2 && name.charAt(0) == '§') {
            name = name.substring(2);
        }

        return name;
    }
}
