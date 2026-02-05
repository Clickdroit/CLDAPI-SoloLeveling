package fr.clickdroit.sololeveling.role.monarchs;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Antares - Le Roi des Dragons
 * Le plus puissant des Monarques, roi des dragons.
 * 
 * Pouvoirs:
 * - Souffle du Dragon: Inflige des dégâts de feu aux joueurs proches
 * - Écailles de Dragon: Résistance aux dégâts
 * - Destruction: Pouvoir ultime de destruction massive
 */
@RoleInfo(name = "Antares", description = "Le Roi des Dragons, le plus puissant des Monarques.", camp = Camp.MONARCHS, rarity = RoleRarity.LEGENDARY, lore = {
        "§7Le souverain suprême des Monarques.",
        "§7Votre pouvoir de destruction est sans égal.",
        "",
        "§6Souffle du Dragon:",
        "§7Enflamme tous les joueurs dans un rayon de 10 blocs.",
        "§7Cooldown: 3 minutes",
        "",
        "§6Écailles de Dragon:",
        "§7Résistance passive aux dégâts.",
        "",
        "§c§lDestruction:",
        "§7Pouvoir ultime dévastateur.",
        "§7Utilisable une seule fois."
})
public class Antares extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new DragonBreath());
        addPower(new DragonScales());
        addPower(new Destruction());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.DRAGON_EGG;
    }

    @Override
    public void onGameStart() {
        // Écailles de Dragon - résistance passive
        if (getOwner() != null) {
            getOwner().addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        }
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Régénération après un kill
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
        player.sendMessage("§c§l[DRAGON] §fVous absorbez l'énergie vitale de votre victime.");
    }

    // === POUVOIRS ===

    private class DragonBreath extends AbstractPower {
        public DragonBreath() {
            super(
                    "Souffle du Dragon",
                    "Enflamme tous les joueurs proches.",
                    PowerType.ACTIVE,
                    180,
                    -1);
        }

        @Override
        protected boolean onExecute(Player player) {
            Location loc = player.getLocation();
            int affected = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(player))
                    continue;
                if (!p.getWorld().equals(player.getWorld()))
                    continue;

                if (p.getLocation().distance(loc) <= 10) {
                    p.setFireTicks(100); // 5 secondes de feu
                    p.damage(4.0); // 2 coeurs de dégâts
                    affected++;
                }
            }

            player.sendMessage("§c§l[DRAGON] §fVotre souffle enflamme " + affected + " joueur(s)!");

            // Effet visuel
            player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(),
                    0, false, false);

            return true;
        }
    }

    private class DragonScales extends AbstractPower {
        public DragonScales() {
            super(
                    "Écailles de Dragon",
                    "Résistance passive aux dégâts.",
                    PowerType.PASSIVE,
                    0,
                    -1);
        }

        @Override
        protected boolean onExecute(Player player) {
            // Passif, géré dans onGameStart
            return true;
        }
    }

    private class Destruction extends AbstractPower {
        public Destruction() {
            super(
                    "Destruction",
                    "Pouvoir ultime: inflige des dégâts massifs à tous les joueurs.",
                    PowerType.ULTIMATE,
                    0, // Pas de cooldown
                    1 // Une seule utilisation
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            Bukkit.broadcastMessage("§c§l☠ DESTRUCTION ☠");
            Bukkit.broadcastMessage("§7" + player.getName() + " déclenche le pouvoir de destruction!");

            Location loc = player.getLocation();

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(player))
                    continue;

                // Dégâts basés sur la distance
                if (p.getWorld().equals(player.getWorld())) {
                    double distance = p.getLocation().distance(loc);

                    if (distance <= 30) {
                        double damage = 12.0 - (distance / 5); // 12 à 6 dégâts selon distance
                        p.damage(Math.max(damage, 2));
                        p.setFireTicks(200);
                        p.sendMessage("§c§l[!] §fL'onde de destruction vous frappe!");
                    }
                }
            }

            // Effets visuels
            player.getWorld().strikeLightningEffect(loc);
            player.getWorld().createExplosion(loc.getX(), loc.getY() + 5, loc.getZ(),
                    0, false, false);

            return true;
        }
    }
}
