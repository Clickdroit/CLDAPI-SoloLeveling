package fr.clickdroit.sololeveling.role.hunters;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Baek Yoon-Ho - Le Maître de la Bête
 * Chasseur de rang S spécialisé dans la transformation.
 * 
 * Pouvoirs:
 * - Transformation Bestiale: Boost massif de force et vitesse
 * - Instinct Sauvage: Détecte les joueurs proches (passif)
 */
@RoleInfo(name = "Baek Yoon-Ho", description = "Chasseur de rang S, maître de la bête.", camp = Camp.HUNTERS, rarity = RoleRarity.RARE, lore = {
        "§7Le plus puissant chasseur de type bête.",
        "§7Capable de se transformer partiellement.",
        "",
        "§6Transformation Bestiale:",
        "§7Obtenez Force III, Vitesse II",
        "§7et Régénération pendant 20s.",
        "",
        "§6Instinct Sauvage:",
        "§7Vision nocturne permanente."
})
public class BaekYoonHo extends AbstractRole {

    private boolean transformed = false;

    @Override
    protected void initPowers() {
        addPower(new BeastTransformation());
        addPower(new WildInstinct());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.LEATHER;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Vision nocturne permanente
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Régénération sur kill
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
        player.sendMessage("§6§l[BÊTE] §fL'instinct de prédateur vous régénère!");
    }

    public boolean isTransformed() {
        return transformed;
    }

    // === POUVOIRS ===

    /**
     * Transformation Bestiale - Boost massif temporaire
     */
    private class BeastTransformation extends AbstractPower {
        public BeastTransformation() {
            super(
                    "Transformation Bestiale",
                    "Transformez-vous en bête pour un boost de combat massif.",
                    PowerType.ACTIVE,
                    180, // 3 min cooldown
                    3 // 3 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            transformed = true;

            // Effets de transformation
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 400, 2)); // Force III 20s
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1)); // Vitesse II 20s
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1)); // Regen II 20s
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 400, 1)); // Jump II 20s

            player.sendMessage("§6§l[BÊTE] §fVOUS VOUS TRANSFORMEZ!");
            player.sendMessage("§7Force III, Vitesse II, Régénération II pendant 20 secondes!");

            // Effets visuels
            player.getWorld().strikeLightningEffect(player.getLocation());

            // Reset après la durée
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                transformed = false;
                player.sendMessage("§6§l[BÊTE] §7La transformation prend fin...");
            }, 400L);

            return true;
        }
    }

    /**
     * Instinct Sauvage - Pouvoir passif (vision nocturne + détection)
     */
    private class WildInstinct extends AbstractPower {
        public WildInstinct() {
            super(
                    "Instinct Sauvage",
                    "Vos sens de prédateur vous accordent une vision nocturne permanente.",
                    PowerType.PASSIVE,
                    0,
                    -1);
        }

        @Override
        protected boolean onExecute(Player player) {
            // Passif, géré dans onRoleAssigned
            return true;
        }
    }
}
