package fr.clickdroit.sololeveling.role.neutral;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Récupérateur - L'Expert en Loot
 * Neutre spécialisé dans la collecte de ressources.
 * 
 * Pouvoirs:
 * - Pillage: Génère des items aléatoires utiles
 * - Radar à Loot: Détecte les coffres et ressources proches
 */
@RoleInfo(name = "Récupérateur", description = "Expert en loot.", camp = Camp.NEUTRAL, rarity = RoleRarity.COMMON, lore = {
        "§7Le maître de la récupération,",
        "§7qui trouve toujours quelque chose.",
        "",
        "§aLoot Bonus:",
        "§7Génère des items aléatoires",
        "§7utiles pour la survie.",
        "",
        "§aFlair du Récupérateur:",
        "§7Chance bonus sur les drops."
})
public class Scavenger extends AbstractRole {

    private Random random = new Random();

    @Override
    protected void initPowers() {
        addPower(new BonusLoot());
        addPower(new ScavengerFlair());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.CHEST;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Vitesse passive pour collecter plus vite
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Double loot sur kill
        player.sendMessage("§a§l[LOOT] §fVous récupérez des ressources supplémentaires!");
        giveRandomLoot(player, 2);
    }

    /**
     * Donne du loot aléatoire
     */
    private void giveRandomLoot(Player player, int amount) {
        ItemStack[] possibleLoot = {
                new ItemStack(Material.GOLD_INGOT, 2),
                new ItemStack(Material.IRON_INGOT, 3),
                new ItemStack(Material.DIAMOND, 1),
                new ItemStack(Material.GOLDEN_APPLE, 1),
                new ItemStack(Material.ARROW, 16),
                new ItemStack(Material.ENDER_PEARL, 1),
                new ItemStack(Material.COOKED_BEEF, 4),
                new ItemStack(Material.BOOK, 1),
                new ItemStack(Material.STRING, 4),
                new ItemStack(Material.LEATHER, 3)
        };

        for (int i = 0; i < amount; i++) {
            ItemStack loot = possibleLoot[random.nextInt(possibleLoot.length)].clone();
            player.getInventory().addItem(loot);
        }
    }

    // === POUVOIRS ===

    /**
     * Loot Bonus - Génère des items aléatoires
     */
    private class BonusLoot extends AbstractPower {
        public BonusLoot() {
            super(
                    "Loot Bonus",
                    "Générez des items aléatoires utiles.",
                    PowerType.ACTIVE,
                    240, // 4 min cooldown
                    5 // 5 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int itemCount = 2 + random.nextInt(3); // 2-4 items
            giveRandomLoot(player, itemCount);

            player.sendMessage("§a§l[RÉCUP] §fVous avez récupéré §e" + itemCount + " items§f!");

            // Parfois un item rare bonus
            if (random.nextInt(4) == 0) {
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
                player.sendMessage("§6§l[RARE] §fBonus: Pomme Dorée!");
            }

            return true;
        }
    }

    /**
     * Flair du Récupérateur - Buff de loot passif
     */
    private class ScavengerFlair extends AbstractPower {
        public ScavengerFlair() {
            super(
                    "Flair du Récupérateur",
                    "Votre instinct vous guide vers les ressources.",
                    PowerType.ACTIVE,
                    120, // 2 min cooldown
                    -1 // Illimité
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            // Boost temporaire pour le farming
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 600, 1)); // Haste II 30s
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1)); // Vitesse II 30s
            

            player.sendMessage("§a§l[RÉCUP] §fFlair activé! Haste II, Vitesse II, Chance II pendant 30s.");

            return true;
        }
    }
}
