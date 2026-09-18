package fr.clickdroit.sololeveling.role.hunters;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Min Byung-Gyu - Le Chasseur National Défensif
 * Chasseur National de rang S spécialisé dans la défense et le soutien.
 *
 * Pouvoirs:
 * - Bouclier Indestructible: Résistance temporaire aux dégâts pour lui et ses alliés proches
 * - Regain de Force: Régénère sa santé et celle d'un allié ciblé
 * - Dernier Bastion: Pouvoir ultime – immunité temporaire à la mort
 */
@RoleInfo(
        name = "Min Byung-Gyu",
        description = "Chasseur National défensif, pilier des alliés.",
        camp = Camp.HUNTERS,
        rarity = RoleRarity.EPIC,
        lore = {
                "§7L'un des cinq Chasseurs Nationaux coréens,",
                "§7spécialisé dans la protection de ses alliés.",
                "",
                "§6Bouclier Indestructible:",
                "§7Confère Résistance II à vous et",
                "§7vos alliés dans un rayon de 8 blocs.",
                "§7Cooldown: 2 minutes",
                "",
                "§6Regain de Force:",
                "§7Soigne un allié proche.",
                "§7Cooldown: 90 secondes",
                "",
                "§c§lDernier Bastion:",
                "§7Devenez temporairement invincible.",
                "§7Utilisable une seule fois."
        },
        maxPerGame = 1
)
public class MinByungGyu extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new ShieldOfSteel());
        addPower(new StrengthRecovery());
        addPower(new LastBastion());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.IRON_CHESTPLATE;
    }

    @Override
    public void onGameStart() {
        Player owner = getOwner();
        if (owner != null) {
            // Résistance passive légère au démarrage
            owner.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        }
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Régénération modérée après chaque kill
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
        player.sendMessage("§b§l[BOUCLIER] §fVous absorbez l'énergie du combat.");
    }

    // === POUVOIRS ===

    /**
     * Bouclier Indestructible – Résistance pour soi et les alliés proches.
     */
    private class ShieldOfSteel extends AbstractPower {
        public ShieldOfSteel() {
            super(
                    "Bouclier Indestructible",
                    "Confère Résistance II à vous et vos alliés proches pendant 20 secondes.",
                    PowerType.ACTIVE,
                    120,
                    -1
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int affected = 0;

            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() == null || !rp.isAlive()) continue;
                if (!rp.getRole().getCamp().isAlliedWith(getCamp())) continue;

                Player ally = Bukkit.getPlayer(rp.getUuid());
                if (ally == null || !ally.isOnline()) continue;
                if (ally.getLocation().distance(player.getLocation()) > 8) continue;

                ally.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 1));
                ally.sendMessage("§b§l[BOUCLIER] §fMin Byung-Gyu vous protège!");
                affected++;
            }

            player.sendMessage("§b§l[BOUCLIER] §fVotre bouclier protège §f" + affected + " allié(s)!");
            return true;
        }
    }

    /**
     * Regain de Force – Soigne l'allié vivant le plus proche.
     */
    private class StrengthRecovery extends AbstractPower {
        public StrengthRecovery() {
            super(
                    "Regain de Force",
                    "Soigne l'allié vivant le plus proche.",
                    PowerType.ACTIVE,
                    90,
                    -1
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            Player nearestAlly = null;
            double nearestDistance = Double.MAX_VALUE;

            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getUuid().equals(player.getUniqueId())) continue;
                if (rp.getRole() == null || !rp.isAlive()) continue;
                if (!rp.getRole().getCamp().isAlliedWith(getCamp())) continue;

                Player ally = Bukkit.getPlayer(rp.getUuid());
                if (ally == null || !ally.isOnline()) continue;

                double distance = ally.getLocation().distance(player.getLocation());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestAlly = ally;
                }
            }

            if (nearestAlly == null || nearestDistance > 15) {
                player.sendMessage("§c§l[BOUCLIER] §cAucun allié à portée!");
                return false;
            }

            nearestAlly.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2));
            nearestAlly.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 0));
            nearestAlly.sendMessage("§b§l[BOUCLIER] §fMin Byung-Gyu vous redonne des forces!");
            player.sendMessage("§b§l[BOUCLIER] §fVous soignez §f" + nearestAlly.getName() + "§f.");
            return true;
        }
    }

    /**
     * Dernier Bastion – Invincibilité temporaire (ultime unique).
     */
    private class LastBastion extends AbstractPower {
        public LastBastion() {
            super(
                    "Dernier Bastion",
                    "Devenez invincible pendant 8 secondes.",
                    PowerType.ULTIMATE,
                    0,
                    1
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            Bukkit.broadcastMessage("§b§l⚡ DERNIER BASTION ⚡");
            Bukkit.broadcastMessage("§7" + player.getName() + " dresse le Dernier Bastion!");

            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 4)); // ~invincible 8s
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 2));
            player.sendMessage("§b§l[BASTION] §fVous êtes temporairement invincible!");

            return true;
        }
    }
}
