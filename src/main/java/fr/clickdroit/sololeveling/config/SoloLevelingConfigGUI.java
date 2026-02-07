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
 * GUI principal de configuration Solo Leveling.
 * Permet d'accéder aux différentes sections de configuration :
 * - Timers (temps de révélation des rôles, etc.)
 * - Configuration des rôles par camp
 */
public class SoloLevelingConfigGUI implements Listener {

    private final SoloLevelingPlugin plugin;
    private static final String GUI_NAME = "§5§l⚔ §dConfig Solo Leveling";

    // Paramètres configurables via le module
    // private int roleRevealTime = 60; // Délégué au module
    // private int minPlayersToStart = 4; // Délégué au module

    public SoloLevelingConfigGUI(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Ouvre le GUI principal de configuration
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, GUI_NAME);
        RoleManager rm = plugin.getRoleManager();

        // Bordure décorative
        ItemStack glass = createItem(Material.STAINED_GLASS_PANE, (short) 15, " ");
        for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44 }) {
            inv.setItem(i, glass);
        }

        // === SECTION TIMERS ===
        inv.setItem(11, createItem(
                Material.WATCH,
                (short) 0,
                "§6§l⏱ TIMERS",
                "",
                "§6§l⏱ TIMERS",
                "",
                " §8▸ §7Temps révélation: §e" + getRoleRevealTime() + "s",
                " §8▸ §7Joueurs minimum: §e" + getMinPlayersToStart(),
                "",
                "§e▶ Clic gauche §7: +10s révélation",
                "§e▶ Clic droit §7: -10s révélation",
                "§e▶ Shift+Clic §7: Modifier joueurs min"));

        // === SECTION RÔLES ===
        int totalRoles = rm.getRegisteredRolesCount();
        int enabledRoles = countEnabledRoles(rm);

        inv.setItem(13, createItem(
                Material.SKULL_ITEM,
                (short) 1,
                "§5§l⚔ CONFIGURATION RÔLES",
                "",
                " §8▸ §7Rôles activés: §a" + enabledRoles + "§7/§f" + totalRoles,
                "",
                "  §8| §7Configurez les rôles par camp",
                "  §8| §7Activez/désactivez les rôles",
                "  §8| §7individuellement.",
                "",
                "§e▶ Clic pour ouvrir"));

        // === SECTION ÉQUILIBRAGE ===
        int playersNeeded = countTotalQuantity(rm);
        inv.setItem(15, createItem(
                Material.EMERALD,
                (short) 0,
                "§a§l⚖ ÉQUILIBRAGE",
                "",
                " §8▸ §7Rôles activés: §e" + enabledRoles,
                " §8▸ §7Joueurs nécessaires: §c" + playersNeeded,
                "",
                "  §8| §cAttention: §7Si il y a plus de",
                "  §8| §7rôles que de joueurs, certains",
                "  §8| §7joueurs n'auront pas de rôle!",
                "",
                "§7La partie vérifiera automatiquement",
                "§7l'équilibrage avant de démarrer."));

        // === SECTION QUANTITÉ DES RÔLES ===
        inv.setItem(20, createItem(
                Material.CHEST,
                (short) 0,
                "§6§l📦 QUANTITÉ DES RÔLES",
                "",
                " §8▸ §7Configurez le nombre",
                " §8▸ §7de chaque rôle en jeu",
                "",
                "  §8| §7Définissez combien de fois",
                "  §8| §7chaque rôle peut être attribué.",
                "",
                "§e▶ Clic pour ouvrir"));

        // === STATISTIQUES ===
        inv.setItem(31, createStatsItem(rm));

        // === BOUTON RETOUR ===
        inv.setItem(40, createItem(
                Material.ARROW,
                (short) 0,
                "§c§lRetour",
                "",
                "§7Retour au menu principal"));

        player.openInventory(inv);
    }

    private ItemStack createStatsItem(RoleManager rm) {
        int hunters = countRolesByCamp(rm, fr.clickdroit.sololeveling.camp.Camp.HUNTERS);
        int monarchs = countRolesByCamp(rm, fr.clickdroit.sololeveling.camp.Camp.MONARCHS);
        int rulers = countRolesByCamp(rm, fr.clickdroit.sololeveling.camp.Camp.RULERS);
        int neutral = countRolesByCamp(rm, fr.clickdroit.sololeveling.camp.Camp.NEUTRAL);
        int total = hunters + monarchs + rulers + neutral;

        return createItem(
                Material.BOOK,
                (short) 0,
                "§6§lStatistiques",
                "",
                "§7Rôles activés par camp:",
                "",
                " §b⚔ §bChasseurs: §f" + hunters,
                " §c☠ §cMonarques: §f" + monarchs,
                " §e✦ §eDirigeants: §f" + rulers,
                " §7◆ §7Neutres: §f" + neutral,
                "",
                "§7Total: §e" + total + " §7rôles activés");
    }

    private int countEnabledRoles(RoleManager rm) {
        int count = 0;
        for (Class<? extends Role> roleClass : rm.getRegisteredRoles()) {
            try {
                Role role = roleClass.newInstance();
                if (rm.isRoleEnabled(role.getName())) {
                    count++;
                }
            } catch (Exception ignored) {
            }
        }
        return count;
    }

    private int countRolesByCamp(RoleManager rm, fr.clickdroit.sololeveling.camp.Camp camp) {
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

    private int countTotalQuantity(RoleManager rm) {
        int total = 0;
        for (Class<? extends Role> roleClass : rm.getRegisteredRoles()) {
            try {
                Role role = roleClass.newInstance();
                if (rm.isRoleEnabled(role.getName())) {
                    total += rm.getRoleQuantity(role.getName());
                }
            } catch (Exception ignored) {
            }
        }
        return total;
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
        ClickType clickType = event.getClick();

        switch (slot) {
            case 11: // Timers
                handleTimerClick(player, clickType);
                break;
            case 13: // Configuration des rôles
                plugin.getRoleConfigGUI().open(player);
                break;
            case 20: // Quantité des rôles
                plugin.getRoleQuantityConfigGUI().open(player);
                break;
            case 40: // Retour
                player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
                plugin.getAPI().openInventory(player, fr.clickdroit.api.config.ConfigMainGUI.class);
                break;
        }
    }

    private void handleTimerClick(Player player, ClickType clickType) {
        int currentMinPlayers = getMinPlayersToStart();
        int currentRevealTime = getRoleRevealTime();

        if (clickType.isShiftClick()) {
            // Modifier le nombre minimum de joueurs
            if (clickType.isLeftClick()) {
                currentMinPlayers = Math.min(currentMinPlayers + 1, 50);
            } else {
                currentMinPlayers = Math.max(currentMinPlayers - 1, 2);
            }
            plugin.getGameModule().getInternalModule().setMinPlayersToStart(currentMinPlayers);
            player.sendMessage("§6§l[TIMER] §fJoueurs minimum: §e" + currentMinPlayers);
        } else {
            // Modifier le temps de révélation
            if (clickType.isLeftClick()) {
                currentRevealTime = Math.min(currentRevealTime + 10, 600);
            } else {
                currentRevealTime = Math.max(currentRevealTime - 10, 10);
            }
            // Mettre à jour le module
            plugin.getGameModule().getInternalModule().setRoleRevealTime(currentRevealTime);
            player.sendMessage("§6§l[TIMER] §fTemps de révélation: §e" + currentRevealTime + "s");
        }
        player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
        open(player); // Refresh
    }

    // === GETTERS ===
    public int getRoleRevealTime() {
        return plugin.getGameModule().getInternalModule().getRoleRevealTime();
    }

    public int getMinPlayersToStart() {
        return plugin.getGameModule().getInternalModule().getMinPlayersToStart();
    }
}
