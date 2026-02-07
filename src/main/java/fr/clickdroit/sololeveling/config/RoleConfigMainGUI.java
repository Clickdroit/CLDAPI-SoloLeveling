package fr.clickdroit.sololeveling.config;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
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
 * GUI principal pour la configuration des rôles.
 * Affiche les camps avec leurs rôles et permet la navigation.
 */
public class RoleConfigMainGUI implements Listener {

    private final SoloLevelingPlugin plugin;
    private static final String GUI_NAME = "§5§l⚔ §dConfiguration des Rôles";

    public RoleConfigMainGUI(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Ouvre le GUI principal de configuration des rôles
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, GUI_NAME);
        RoleManager rm = plugin.getRoleManager();

        // Bordure décorative
        ItemStack glass = createItem(Material.STAINED_GLASS_PANE, (short) 15, " ");
        for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44 }) {
            inv.setItem(i, glass);
        }

        // === Camp: Chasseurs ===
        int huntersEnabled = countEnabledRoles(rm, Camp.HUNTERS);
        int huntersTotal = countTotalRoles(Camp.HUNTERS);
        inv.setItem(11, createCampItem(
                Material.IRON_SWORD,
                "§b§lCHASSEURS",
                Camp.HUNTERS,
                huntersEnabled,
                huntersTotal,
                "§7Les protecteurs de l'humanité.",
                "§7Leur but est d'éliminer les Monarques."));

        // === Camp: Monarques ===
        int monarchsEnabled = countEnabledRoles(rm, Camp.MONARCHS);
        int monarchsTotal = countTotalRoles(Camp.MONARCHS);
        inv.setItem(13, createCampItem(
                Material.SKULL_ITEM,
                "§c§lMONARQUES",
                Camp.MONARCHS,
                monarchsEnabled,
                monarchsTotal,
                "§7Les puissants rois des ténèbres.",
                "§7Leur but est de dominer le monde."));

        // === Camp: Dirigeants ===
        int rulersEnabled = countEnabledRoles(rm, Camp.RULERS);
        int rulersTotal = countTotalRoles(Camp.RULERS);
        inv.setItem(15, createCampItem(
                Material.GLOWSTONE_DUST,
                "§e§lDIRIGEANTS",
                Camp.RULERS,
                rulersEnabled,
                rulersTotal,
                "§7Les gardiens de la lumière.",
                "§7Leur but est de préserver l'équilibre."));

        // === Camp: Neutres ===
        int neutralEnabled = countEnabledRoles(rm, Camp.NEUTRAL);
        int neutralTotal = countTotalRoles(Camp.NEUTRAL);
        inv.setItem(22, createCampItem(
                Material.COMPASS,
                "§7§lNEUTRES",
                Camp.NEUTRAL,
                neutralEnabled,
                neutralTotal,
                "§7N'appartiennent à aucun camp.",
                "§7Chacun a son propre objectif."));

        // === Statistiques ===
        int totalEnabled = huntersEnabled + monarchsEnabled + rulersEnabled + neutralEnabled;
        int totalRoles = huntersTotal + monarchsTotal + rulersTotal + neutralTotal;
        inv.setItem(31, createItem(
                Material.BOOK,
                (short) 0,
                "§6§lStatistiques",
                "",
                "§7Rôles activés: §e" + totalEnabled + "§7/§f" + totalRoles,
                "",
                "§7Chasseurs: §b" + huntersEnabled + "§7/§f" + huntersTotal,
                "§7Monarques: §c" + monarchsEnabled + "§7/§f" + monarchsTotal,
                "§7Dirigeants: §e" + rulersEnabled + "§7/§f" + rulersTotal,
                "§7Neutres: §7" + neutralEnabled + "§7/§f" + neutralTotal));

        // === Boutons d'action ===
        inv.setItem(29, createItem(
                Material.INK_SACK,
                (short) 10,
                "§a§lActiver Tous",
                "",
                "§7Cliquez pour activer",
                "§7tous les rôles."));

        inv.setItem(33, createItem(
                Material.INK_SACK,
                (short) 1,
                "§c§lDésactiver Tous",
                "",
                "§7Cliquez pour désactiver",
                "§7tous les rôles."));

        // === Bouton Retour ===
        inv.setItem(40, createItem(
                Material.ARROW,
                (short) 0,
                "§c§lRetour",
                "",
                "§7Retour au menu de config"));

        player.openInventory(inv);
    }

    private int countEnabledRoles(RoleManager rm, Camp camp) {
        int count = 0;
        for (Class<? extends Role> roleClass : rm.getRegisteredRoles()) {
            try {
                Role role = roleClass.newInstance();
                if (role.getCamp() == camp && rm.isRoleEnabled(role.getName())) {
                    count++;
                }
            } catch (Exception ignored) {
            }
        }
        return count;
    }

    private int countTotalRoles(Camp camp) {
        int count = 0;
        try {
            for (Class<? extends Role> roleClass : plugin.getRoleManager().getRegisteredRoles()) {
                Role role = roleClass.newInstance();
                if (role.getCamp() == camp) {
                    count++;
                }
            }
        } catch (Exception ignored) {
        }
        return count;
    }

    private ItemStack createCampItem(Material material, String name, Camp camp, int enabled, int total,
            String... description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add("");
        for (String line : description) {
            lore.add(line);
        }
        lore.add("");
        lore.add("§7Rôles activés: " + (enabled == total ? "§a" : "§e") + enabled + "§7/§f" + total);
        lore.add("");
        lore.add("§e▶ Clic pour configurer");

        meta.setLore(lore);
        item.setItemMeta(meta);
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
            if (!event.getView().getTitle().equals(GUI_NAME))
                return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            RoleCampGUI campGUI = plugin.getRoleCampGUI();

            switch (slot) {
                case 11: // Chasseurs
                    campGUI.open(player, Camp.HUNTERS);
                    break;
                case 13: // Monarques
                    campGUI.open(player, Camp.MONARCHS);
                    break;
                case 15: // Dirigeants
                    campGUI.open(player, Camp.RULERS);
                    break;
                case 22: // Neutres
                    campGUI.open(player, Camp.NEUTRAL);
                    break;
                case 29: // Activer tous
                    enableAllRoles(true);
                    player.sendMessage("§a§l[RÔLES] §fTous les rôles ont été activés!");
                    open(player); // Refresh
                    break;
                case 33: // Désactiver tous
                    enableAllRoles(false);
                    player.sendMessage("§c§l[RÔLES] §fTous les rôles ont été désactivés!");
                    open(player); // Refresh
                    break;
                case 40: // Retour
                    plugin.getConfigGUI().open(player);
                    break;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors du clic dans RoleConfigMainGUI:");
            e.printStackTrace();
        }
    }

    private void enableAllRoles(boolean enabled) {
        RoleManager rm = plugin.getRoleManager();
        for (Class<? extends Role> roleClass : rm.getRegisteredRoles()) {
            try {
                Role role = roleClass.newInstance();
                rm.setRoleEnabled(role.getName(), enabled);
            } catch (Exception ignored) {
            }
        }
    }
}
