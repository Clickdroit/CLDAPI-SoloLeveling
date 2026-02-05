package fr.clickdroit.sololeveling.role.hunters;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Thomas Andre - Le Tank Ultime
 * Chasseur National américain, spécialisé dans la défense.
 * 
 * Pouvoirs:
 * - Renforcement: Résistance massive et effets de tank
 * - Poing Dévastateur: Attaque de zone avec knockback
 */
@RoleInfo(name = "Thomas Andre", description = "Chasseur National américain.", camp = Camp.HUNTERS, rarity = RoleRarity.EPIC, lore = {
        "§7Le chasseur le plus résistant au monde.",
        "§7Aucune attaque ne peut le briser.",
        "",
        "§6Renforcement:",
        "§7Résistance III et Absorption V",
        "§7pendant 30 secondes.",
        "",
        "§6Poing Dévastateur:",
        "§7Repousse et endommage tous",
        "§7les ennemis proches."
})
public class ThomasAndre extends AbstractRole {

    private boolean reinforced = false;

    @Override
    protected void initPowers() {
        addPower(new Reinforcement());
        addPower(new DevastatingPunch());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.IRON_CHESTPLATE;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Résistance passive de base
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onDeath(Player player, Player killer) {
        // Message dramatique
        if (killer != null) {
            killer.sendMessage("§6§l[LÉGENDE] §fVous avez terrassé le Tank Ultime!");
        }
    }

    public boolean isReinforced() {
        return reinforced;
    }

    // === POUVOIRS ===

    /**
     * Renforcement - Tank mode
     */
    private class Reinforcement extends AbstractPower {
        public Reinforcement() {
            super(
                    "Renforcement",
                    "Activez un mode défensif ultime.",
                    PowerType.ACTIVE,
                    180, // 3 min cooldown
                    3 // 3 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            reinforced = true;

            // Effets de tank
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 2)); // Résistance III 30s
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 4)); // Absorption V 30s
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 600, 0)); // Lenteur I (contrepartie)
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 0)); // Regen I 30s

            player.sendMessage("§6§l[TANK] §fMODE RENFORCEMENT ACTIVÉ!");
            player.sendMessage("§7Vous devenez pratiquement invincible, mais plus lent.");

            // Reset après la durée
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                reinforced = false;
                player.sendMessage("§6§l[TANK] §7Le renforcement prend fin.");
            }, 600L);

            return true;
        }
    }

    /**
     * Poing Dévastateur - AoE knockback + dégâts
     */
    private class DevastatingPunch extends AbstractPower {
        public DevastatingPunch() {
            super(
                    "Poing Dévastateur",
                    "Un coup de poing qui repousse et endommage tous les ennemis.",
                    PowerType.ACTIVE,
                    90, // 1.5 min cooldown
                    5 // 5 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int playersHit = 0;

            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player && entity != player) {
                    Player target = (Player) entity;

                    // Dégâts et knockback
                    target.damage(6.0, player); // 3 coeurs

                    Vector direction = target.getLocation().toVector()
                            .subtract(player.getLocation().toVector())
                            .normalize()
                            .multiply(2.5);
                    direction.setY(0.8);
                    target.setVelocity(direction);

                    target.sendMessage("§6§l[IMPACT] §fVous êtes repoussé par Thomas Andre!");
                    playersHit++;
                }
            }

            player.sendMessage("§6§l[TANK] §fPoing Dévastateur! §7(" + playersHit + " joueurs touchés)");

            // Effet visuel
            player.getWorld().createExplosion(player.getLocation(), 0, false);

            return true;
        }
    }
}
