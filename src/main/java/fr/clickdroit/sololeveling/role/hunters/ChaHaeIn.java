package fr.clickdroit.sololeveling.role.hunters;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RolePlayer;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Cha Hae-In - La Chasseuse aux sens aiguisés
 * Une des plus puissantes chasseuses de rang S.
 * 
 * Pouvoirs:
 * - Détection d'aura: Détecte le camp d'un joueur proche
 * - Lame rapide: Bonus de vitesse et dégâts
 */
@RoleInfo(name = "Cha Hae-In", description = "Chasseuse de rang S aux sens aiguisés.", camp = Camp.HUNTERS, rarity = RoleRarity.EPIC, lore = {
        "§7La vice-maître de la guilde Hunters.",
        "§7Vos sens vous permettent de détecter les auras.",
        "",
        "§6Détection d'Aura:",
        "§7Révèle le camp du joueur le plus proche.",
        "§7Cooldown: 2 minutes",
        "",
        "§6Lame Rapide:",
        "§7Bonus de vitesse et force pendant 20s."
})
public class ChaHaeIn extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new AuraDetection());
        addPower(new SwiftBlade());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.DIAMOND_SWORD;
    }

    // === POUVOIRS ===

    private class AuraDetection extends AbstractPower {
        public AuraDetection() {
            super(
                    "Détection d'Aura",
                    "Révèle le camp du joueur le plus proche.",
                    PowerType.ACTIVE,
                    120,
                    5);
        }

        @Override
        protected boolean onExecute(Player player) {
            Player nearest = getNearestPlayer(player, 50);

            if (nearest == null) {
                player.sendMessage("§cAucun joueur à proximité.");
                return false;
            }

            RolePlayer rp = SoloLevelingPlugin.getInstance().getRoleManager()
                    .getRolePlayer(nearest.getUniqueId());

            if (rp != null && rp.getRole() != null) {
                Camp camp = rp.getRole().getCamp();
                player.sendMessage("§6§l[AURA] §fVous détectez une aura " +
                        camp.getColorCode() + camp.getDisplayName() +
                        " §fprès de vous...");
                player.sendMessage("§7Distance: " + (int) player.getLocation().distance(nearest.getLocation()) + "m");
            } else {
                player.sendMessage("§6§l[AURA] §fL'aura de ce joueur est indéchiffrable.");
            }

            return true;
        }

        private Player getNearestPlayer(Player player, double maxDistance) {
            Player nearest = null;
            double nearestDist = maxDistance;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(player))
                    continue;
                if (!p.getWorld().equals(player.getWorld()))
                    continue;

                double dist = p.getLocation().distance(player.getLocation());
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = p;
                }
            }

            return nearest;
        }
    }

    private class SwiftBlade extends AbstractPower {
        public SwiftBlade() {
            super(
                    "Lame Rapide",
                    "Augmente votre vitesse et vos dégâts.",
                    PowerType.ACTIVE,
                    90,
                    -1);
        }

        @Override
        protected boolean onExecute(Player player) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SPEED, 400, 1));
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 400, 0));

            player.sendMessage("§e§l[LAME] §fVotre vitesse de combat augmente!");
            return true;
        }
    }
}
