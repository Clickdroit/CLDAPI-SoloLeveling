package fr.clickdroit.sololeveling.role.hunters;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Choi Jong-In - Le Mage de Feu
 * Chasseur de rang S spécialisé dans la magie de feu.
 * 
 * Pouvoirs:
 * - Flamme de l'Enfer: Met le feu aux ennemis proches
 * - Résistance au Feu: Immunité au feu (passif)
 */
@RoleInfo(name = "Choi Jong-In", description = "Mage de feu de rang S.", camp = Camp.HUNTERS, rarity = RoleRarity.RARE, lore = {
        "§7Le plus puissant mage de feu de Corée.",
        "§7Ses flammes consument tout.",
        "",
        "§6Flamme de l'Enfer:",
        "§7Enflamme tous les joueurs",
        "§7dans un rayon de 10 blocs.",
        "",
        "§6Résistance au Feu:",
        "§7Immunité au feu permanente."
})
public class ChoiJongIn extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new HellfireBlast());
        addPower(new FireResistance());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.BLAZE_POWDER;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Résistance au feu permanente
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Boost de feu après un kill
        player.sendMessage("§c§l[FEU] §fLes flammes de votre victime vous renforcent!");
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0)); // Force I 5s
    }

    // === POUVOIRS ===

    /**
     * Flamme de l'Enfer - AoE de feu
     */
    private class HellfireBlast extends AbstractPower {
        public HellfireBlast() {
            super(
                    "Flamme de l'Enfer",
                    "Déchaînez une vague de feu qui consume tous les ennemis proches.",
                    PowerType.ACTIVE,
                    120, // 2 min cooldown
                    5 // 5 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            Location loc = player.getLocation();
            int playersHit = 0;

            // Cibler tous les joueurs dans un rayon de 10 blocs
            for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                if (entity instanceof Player && entity != player) {
                    Player target = (Player) entity;

                    // Mettre le feu et repousser
                    target.setFireTicks(100); // 5 secondes de feu
                    target.damage(4.0, player); // 2 coeurs de dégâts

                    // Knockback depuis le joueur
                    Vector direction = target.getLocation().toVector()
                            .subtract(player.getLocation().toVector())
                            .normalize()
                            .multiply(1.5);
                    direction.setY(0.5);
                    target.setVelocity(direction);

                    target.sendMessage("§c§l[FEU] §fVous êtes brûlé par " + player.getName() + "!");
                    playersHit++;
                }
            }

            player.sendMessage("§c§l[FEU] §fFlamme de l'Enfer activée! §7(" + playersHit + " joueurs touchés)");

            // Effets visuels - explosions de feu
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2 / 8) * i;
                double x = Math.cos(angle) * 3;
                double z = Math.sin(angle) * 3;
                Location effectLoc = loc.clone().add(x, 0, z);
                player.getWorld().createExplosion(effectLoc, 0, true); // Explosion visuelle avec feu
            }

            return true;
        }
    }

    /**
     * Résistance au Feu - Pouvoir passif
     */
    private class FireResistance extends AbstractPower {
        public FireResistance() {
            super(
                    "Résistance au Feu",
                    "Immunité totale au feu et à la lave.",
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
