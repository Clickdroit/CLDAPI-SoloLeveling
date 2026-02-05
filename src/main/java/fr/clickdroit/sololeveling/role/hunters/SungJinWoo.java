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
 * Sung Jin-Woo - Le Chasseur des Ombres
 * Le protagoniste de Solo Leveling, le plus puissant des chasseurs.
 * 
 * Pouvoirs:
 * - Extraction d'ombre: Marque un joueur tué pour le ressusciter comme ombre
 * - Armée de l'ombre: Invoque des renforts temporaires (force + vitesse)
 * - Domaine du Monarque: Zone de boost massif (ultime)
 */
@RoleInfo(name = "Sung Jin-Woo", description = "Le Chasseur des Ombres, le plus puissant des chasseurs.", camp = Camp.HUNTERS, rarity = RoleRarity.LEGENDARY, lore = {
        "§7Autrefois le plus faible des chasseurs,",
        "§7vous êtes devenu le Monarque des Ombres.",
        "",
        "§6Extraction d'Ombre:",
        "§7Marquez un joueur tué pour obtenir",
        "§7des informations sur son rôle.",
        "",
        "§6Armée de l'Ombre:",
        "§7Obtenez Force II et Vitesse II",
        "§7pendant 30 secondes.",
        "",
        "§c§lÉveil:",
        "§7Après 3 kills, vos pouvoirs sont améliorés."
})
public class SungJinWoo extends AbstractRole {

    private int shadowCount = 0;
    private boolean awakened = false;

    @Override
    protected void initPowers() {
        addPower(new ShadowExtraction());
        addPower(new ShadowArmy());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.EYE_OF_ENDER;
    }

    @Override
    public void onKill(Player player, Player victim) {
        shadowCount++;
        player.sendMessage("§5§l[OMBRE] §fVous avez extrait une ombre. §7(" + shadowCount + " ombres)");

        // Régénération après un kill
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));

        // Vérifier l'éveil
        if (shadowCount >= 3 && !awakened) {
            awaken(player);
        }
    }

    private void awaken(Player player) {
        awakened = true;
        player.sendMessage("");
        player.sendMessage("§5§l§m                                                §r");
        player.sendMessage("");
        player.sendMessage("  §5§l⚡ ÉVEIL §5§l⚡");
        player.sendMessage("");
        player.sendMessage("  §7Vous avez atteint votre plein potentiel!");
        player.sendMessage("  §7Vos pouvoirs sont maintenant améliorés.");
        player.sendMessage("");
        player.sendMessage("  §6• Régénération passive");
        player.sendMessage("  §6• Cooldowns réduits");
        player.sendMessage("  §6• Résistance augmentée");
        player.sendMessage("");
        player.sendMessage("§5§l§m                                                §r");
        player.sendMessage("");

        // Effets permanents après éveil
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
    }

    @Override
    public void onTick(int gameTime) {
        if (awakened && getOwner() != null) {
            // Régénération lente passive après éveil
            if (gameTime % 60 == 0) { // Toutes les minutes
                getOwner().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
            }
        }
    }

    public boolean isAwakened() {
        return awakened;
    }

    public int getShadowCount() {
        return shadowCount;
    }

    // === POUVOIRS ===

    /**
     * Extraction d'Ombre - Pouvoir passif qui s'active au kill
     */
    private class ShadowExtraction extends AbstractPower {
        public ShadowExtraction() {
            super(
                    "Extraction d'Ombre",
                    "Extrait l'ombre d'un ennemi tué pour obtenir des bonus.",
                    PowerType.PASSIVE,
                    0,
                    -1);
        }

        @Override
        protected boolean onExecute(Player player) {
            // Ce pouvoir est passif, géré dans onKill
            return true;
        }
    }

    /**
     * Armée de l'Ombre - Pouvoir actif de boost
     */
    private class ShadowArmy extends AbstractPower {
        public ShadowArmy() {
            super(
                    "Armée de l'Ombre",
                    "Invoque la force de vos ombres pour vous renforcer.",
                    PowerType.ACTIVE,
                    awakened ? 120 : 180, // Cooldown réduit si éveillé
                    3);
        }

        @Override
        protected boolean onExecute(Player player) {
            int duration = awakened ? 800 : 600; // 40s éveillé, 30s normal
            int amplifier = awakened ? 2 : 1; // Niveau 3 éveillé, niveau 2 normal

            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, amplifier));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));

            player.sendMessage("§5§l[OMBRE] §fL'armée de l'ombre vous renforce!");

            // Effet visuel
            player.getWorld().strikeLightningEffect(player.getLocation());

            return true;
        }
    }
}
