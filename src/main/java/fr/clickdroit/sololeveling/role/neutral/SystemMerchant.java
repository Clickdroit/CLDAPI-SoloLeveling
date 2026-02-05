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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

/**
 * Marchand du Système - Le Vendeur Mystérieux
 * Neutre qui peut vendre des items spéciaux aux joueurs.
 * 
 * Pouvoirs:
 * - Boutique: Génère des items spéciaux pour lui-même
 * - Transaction: Peut donner des buffs temporaires
 */
@RoleInfo(name = "Marchand du Système", description = "Vend des items spéciaux.", camp = Camp.NEUTRAL, rarity = RoleRarity.RARE, lore = {
        "§7Le mystérieux Marchand,",
        "§7commerce avec tous les camps.",
        "",
        "§2Boutique du Système:",
        "§7Génère des items spéciaux",
        "§7uniques au Marchand.",
        "",
        "§2Commerce:",
        "§7Votre objectif est de survivre",
        "§7et de prospérer."
})
public class SystemMerchant extends AbstractRole {

    private int goldCoins = 0;

    @Override
    protected void initPowers() {
        addPower(new SystemShop());
        addPower(new TradeSecret());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.EMERALD;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Démarrage avec des émeraudes
        player.getInventory().addItem(new ItemStack(Material.EMERALD, 5));
        goldCoins = 5;
        player.sendMessage("§2§l[MARCHAND] §7Vous commencez avec §a5 pièces d'or§7.");
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Gain de pièces sur kill
        goldCoins += 3;
        player.getInventory().addItem(new ItemStack(Material.EMERALD, 3));
        player.sendMessage(
                "§2§l[MARCHAND] §fVous récupérez §a3 pièces§f de votre victime! §7(Total: " + goldCoins + ")");
    }

    @Override
    public void onEpisode(int episode) {
        Player owner = getOwner();
        if (owner == null)
            return;

        // Revenu passif par épisode
        goldCoins += 2;
        owner.getInventory().addItem(new ItemStack(Material.EMERALD, 2));
        owner.sendMessage("§2§l[MARCHAND] §7Revenu d'épisode: §a+2 pièces§7. (Total: " + goldCoins + ")");
    }

    /**
     * Crée un item spécial du marchand
     */
    private ItemStack createMerchantItem(String name, Material material, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§l" + name);
        meta.setLore(Arrays.asList(loreLines));
        item.setItemMeta(meta);
        return item;
    }

    // === POUVOIRS ===

    /**
     * Boutique du Système - Génère des items spéciaux
     */
    private class SystemShop extends AbstractPower {
        public SystemShop() {
            super(
                    "Boutique du Système",
                    "Achetez des items spéciaux avec vos pièces.",
                    PowerType.ACTIVE,
                    60, // 1 min cooldown
                    -1 // Illimité
            );
        }

        @Override
        protected boolean canExecute(Player player) {
            if (goldCoins < 3) {
                player.sendMessage("§c§l[BOUTIQUE] §cPas assez de pièces! (Minimum: 3, Vous avez: " + goldCoins + ")");
                return false;
            }
            return true;
        }

        @Override
        protected boolean onExecute(Player player) {
            goldCoins -= 3;
            // Retirer les émeraudes de l'inventaire
            player.getInventory().removeItem(new ItemStack(Material.EMERALD, 3));

            // Item aléatoire de la boutique
            int itemType = (int) (Math.random() * 4);

            switch (itemType) {
                case 0: // Pomme dorée
                    player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2));
                    player.sendMessage("§2§l[BOUTIQUE] §fAcheté: §6x2 Pommes Dorées§f! §7(-3 pièces)");
                    break;

                case 1: // Perle de l'ender
                    player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 3));
                    player.sendMessage("§2§l[BOUTIQUE] §fAcheté: §dx3 Perles de l'Ender§f! §7(-3 pièces)");
                    break;

                case 2: // Diamants
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
                    player.sendMessage("§2§l[BOUTIQUE] §fAcheté: §bx3 Diamants§f! §7(-3 pièces)");
                    break;

                case 3: // Buff
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1200, 0)); // Force I 60s
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1)); // Regen II 20s
                    player.sendMessage("§2§l[BOUTIQUE] §fAcheté: §ePotion de Combat§f! §7(-3 pièces)");
                    break;
            }

            player.sendMessage("§7Pièces restantes: §a" + goldCoins);

            return true;
        }
    }

    /**
     * Secret Commercial - Buff personnel
     */
    private class TradeSecret extends AbstractPower {
        public TradeSecret() {
            super(
                    "Secret Commercial",
                    "Utilisez vos secrets pour vous protéger.",
                    PowerType.ACTIVE,
                    180, // 3 min cooldown
                    3 // 3 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            // Protection temporaire
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 1)); // Résistance II 20s
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1)); // Vitesse II 20s
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0)); // Invisibilité 10s

            player.sendMessage("§2§l[MARCHAND] §fSecret Commercial activé!");
            player.sendMessage("§7Résistance II, Vitesse II, Invisibilité pendant 10-20s.");

            return true;
        }
    }
}
