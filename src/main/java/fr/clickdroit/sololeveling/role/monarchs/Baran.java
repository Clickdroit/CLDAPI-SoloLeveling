package fr.clickdroit.sololeveling.role.monarchs;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Baran - Le Roi des Démons
 * Monarque contrôlant la foudre démoniaque.
 * 
 * Pouvoirs:
 * - Foudre Démoniaque: Frappe les ennemis d'éclairs
 * - Aura Démoniaque: Résistance au feu et bonus passif
 */
@RoleInfo(name = "Baran", description = "Le Roi des Démons.", camp = Camp.MONARCHS, rarity = RoleRarity.EPIC, lore = {
        "§7Le terrifiant Roi des Démons,",
        "§7maître de la foudre destructrice.",
        "",
        "§cFoudre Démoniaque:",
        "§7Invoque des éclairs sur",
        "§7les ennemis à portée.",
        "",
        "§cAura Démoniaque:",
        "§7Immunité au feu permanente."
})
public class Baran extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new DemonicLightning());
        addPower(new DemonicAura());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.NETHER_STAR;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Immunité au feu permanente
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Effet de foudre sur le kill
        player.getWorld().strikeLightningEffect(victim.getLocation());
        player.sendMessage("§c§l[DÉMON] §fL'âme de votre victime nourrit votre puissance!");
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0)); // Force I 10s
    }

    // === POUVOIRS ===

    /**
     * Foudre Démoniaque - AoE lightning strike
     */
    private class DemonicLightning extends AbstractPower {
        public DemonicLightning() {
            super(
                    "Foudre Démoniaque",
                    "Invoquez la foudre sur tous les ennemis proches.",
                    PowerType.ACTIVE,
                    90, // 1.5 min cooldown
                    5 // 5 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int playersHit = 0;

            for (Entity entity : player.getNearbyEntities(12, 12, 12)) {
                if (entity instanceof Player && entity != player) {
                    Player target = (Player) entity;

                    // Éclair sur la cible
                    target.getWorld().strikeLightningEffect(target.getLocation());
                    target.damage(6.0, player); // 3 coeurs
                    target.setFireTicks(60); // 3s de feu

                    // Knockback
                    Vector knockback = new Vector(0, 0.8, 0);
                    target.setVelocity(knockback);

                    target.sendMessage("§c§l[FOUDRE] §fBaran vous frappe de sa foudre démoniaque!");
                    playersHit++;
                }
            }

            player.sendMessage("§c§l[DÉMON] §fFoudre Démoniaque! §7(" + playersHit + " joueurs frappés)");

            // Éclair sur le joueur aussi pour l'effet
            player.getWorld().strikeLightningEffect(player.getLocation());

            return true;
        }
    }

    /**
     * Aura Démoniaque - Pouvoir passif
     */
    private class DemonicAura extends AbstractPower {
        public DemonicAura() {
            super(
                    "Aura Démoniaque",
                    "Une aura de feu vous protège des flammes.",
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
