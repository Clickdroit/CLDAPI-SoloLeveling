package fr.clickdroit.sololeveling.config;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.Power;
import fr.clickdroit.sololeveling.role.Role;
import fr.clickdroit.sololeveling.role.RoleManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI pour afficher les détails complets d'un rôle.
 */
public class RoleDetailGUI implements Listener {

    private final SoloLevelingPlugin plugin;
    private static final String GUI_PREFIX = "§5§lDétails: §d";
    private Camp parentCamp;
    private String currentRoleName;

    public RoleDetailGUI(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Ouvre le GUI de détails pour un rôle
     */
    public void open(Player player, String roleName, Camp parentCamp) {
        this.parentCamp = parentCamp;
        this.currentRoleName = roleName;

        RoleManager rm = plugin.getRoleManager();
        Role role = rm.createRole(roleName);

        if (role == null) {
            player.sendMessage("§c§l[ERREUR] §fRôle non trouvé: " + roleName);
            return;
        }

        String title = GUI_PREFIX + role.getName();
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory inv = Bukkit.createInventory(null, 45, title);

        // Bordure décorative
        ItemStack glass = createItem(Material.STAINED_GLASS_PANE, (short) 10, " ");
        for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 41, 42, 43, 44 }) {
            inv.setItem(i, glass);
        }

        // === Icône du rôle (centre haut) ===
        inv.setItem(4, role.getIcon());

        // === Informations générales ===
        boolean enabled = rm.isRoleEnabled(roleName);
        inv.setItem(11, createItem(
                Material.NAME_TAG,
                (short) 0,
                "§6§lInformations",
                "",
                "§7Nom: §f" + role.getName(),
                "§7Description: §f" + role.getDescription(),
                "",
                "§7Camp: " + role.getCamp().getColoredName(),
                "§7Rareté: " + role.getRarity().getColoredName(),
                "",
                "§7Statut: " + (enabled ? "§a✔ Activé" : "§c✖ Désactivé")));

        // === Lore du rôle ===
        List<String> loreLore = new ArrayList<>();
        loreLore.add("");
        for (String line : role.getLore()) {
            loreLore.add(line);
        }
        inv.setItem(13, createItemWithLore(
                Material.BOOK,
                (short) 0,
                "§e§lHistoire",
                loreLore));

        // === Pouvoirs ===
        int powerSlot = 20;
        if (role.getPowers() != null) {
            for (Power power : role.getPowers()) {
                if (powerSlot > 26)
                    break;
                inv.setItem(powerSlot, createPowerItem(power));
                powerSlot += 2;
            }
        }

        // === Bouton Toggle ===
        inv.setItem(29, createItem(
                enabled ? Material.INK_SACK : Material.INK_SACK,
                enabled ? (short) 1 : (short) 10,
                enabled ? "§c§lDésactiver ce rôle" : "§a§lActiver ce rôle",
                "",
                "§7Cliquez pour " + (enabled ? "désactiver" : "activer") + " ce rôle."));

        // === Bouton retour ===
        inv.setItem(40, createItem(
                Material.ARROW,
                (short) 0,
                "§c§l← Retour",
                "",
                "§7Retourner à la liste des rôles"));

        player.openInventory(inv);
    }

    private ItemStack createPowerItem(Power power) {
        Material material;
        switch (power.getType()) {
            case ACTIVE:
                material = Material.BLAZE_POWDER;
                break;
            case PASSIVE:
                material = Material.GLOWSTONE_DUST;
                break;
            case ULTIMATE:
                material = Material.NETHER_STAR;
                break;
            default:
                material = Material.PAPER;
        }

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + power.getDescription());
        lore.add("");
        lore.add("§8Type: §f" + power.getType().name());
        if (power.getCooldown() > 0) {
            lore.add("§8Cooldown: §f" + power.getCooldown() + "s");
        }
        if (power.getMaxUses() > 0) {
            lore.add("§8Utilisations: §f" + power.getMaxUses());
        } else if (power.getMaxUses() == -1) {
            lore.add("§8Utilisations: §f∞");
        }

        return createItemWithLore(material, (short) 0, "§6" + power.getName(), lore);
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

    private ItemStack createItemWithLore(Material material, short durability, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1, durability);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(GUI_PREFIX))
            return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR)
            return;
        if (clicked.getType() == Material.STAINED_GLASS_PANE)
            return;

        // Bouton retour
        if (slot == 40) {
            new RoleCampGUI(plugin).open(player, parentCamp);
            return;
        }

        // Bouton toggle
        if (slot == 29) {
            RoleManager rm = plugin.getRoleManager();
            boolean currentState = rm.isRoleEnabled(currentRoleName);
            rm.setRoleEnabled(currentRoleName, !currentState);

            String status = !currentState ? "§aactivé" : "§cdésactivé";
            player.sendMessage("§5§l[RÔLES] §fRôle §e" + currentRoleName + " §f" + status + "§f!");

            // Refresh
            open(player, currentRoleName, parentCamp);
        }
    }
}
