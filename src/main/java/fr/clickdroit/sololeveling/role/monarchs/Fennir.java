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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Fennir - Le Monarque des Bêtes
 * Monarque contrôlant les prédateurs sauvages.
 *
 * Pouvoirs:
 * - Cri du Prédateur: Ralentit et affaiblit les ennemis proches
 * - Charge Bestiale: Bondit vers un ennemi et le repousse
 * - Meute Sauvage: Invoque une meute de loups temporaire (ultime)
 */
@RoleInfo(
        name = "Fennir",
        description = "Le Monarque des Bêtes, seigneur des prédateurs.",
        camp = Camp.MONARCHS,
        rarity = RoleRarity.LEGENDARY,
        lore = {
                "§7Le souverain de toutes les bêtes sauvages.",
                "§7Sa force et sa vitesse sont sans égales.",
                "",
                "§6Cri du Prédateur:",
                "§7Ralentit et affaiblit tous les ennemis",
                "§7dans un rayon de 12 blocs.",
                "§7Cooldown: 90 secondes",
                "",
                "§6Charge Bestiale:",
                "§7Foncez sur un ennemi et repoussez-le.",
                "§7Cooldown: 45 secondes",
                "",
                "§c§lMeute Sauvage:",
                "§7Invoquez des loups pour vous défendre.",
                "§7Utilisable une seule fois."
        },
        maxPerGame = 1
)
public class Fennir extends AbstractRole {

    private final List<Wolf> summonedWolves = new ArrayList<>();

    @Override
    protected void initPowers() {
        addPower(new PredatorRoar());
        addPower(new BeastCharge());
        addPower(new WildPack());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.BONE;
    }

    @Override
    public void onGameStart() {
        Player owner = getOwner();
        if (owner != null) {
            // Vitesse passive permanente
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        }
    }

    @Override
    public void onKill(Player player, Player victim) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1));
        player.sendMessage("§c§l[BÊTE] §fVous ressentez la fièvre de la chasse!");
    }

    @Override
    public void onDeath(Player player, Player killer) {
        // Retirer les loups invoqués à la mort
        for (Wolf wolf : summonedWolves) {
            if (!wolf.isDead()) {
                wolf.remove();
            }
        }
        summonedWolves.clear();
    }

    // === POUVOIRS ===

    /**
     * Cri du Prédateur – Affaiblit les ennemis proches.
     */
    private class PredatorRoar extends AbstractPower {
        public PredatorRoar() {
            super(
                    "Cri du Prédateur",
                    "Ralentit et affaiblit les ennemis dans un rayon de 12 blocs.",
                    PowerType.ACTIVE,
                    90,
                    -1
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int affected = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(player)) continue;
                if (!p.getWorld().equals(player.getWorld())) continue;
                if (p.getLocation().distance(player.getLocation()) > 12) continue;
                if (!plugin.getCampManager().areEnemies(player, p)) continue;

                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                p.sendMessage("§c§l[!] §fLe cri de Fennir vous paralyse de peur!");
                affected++;
            }

            player.sendMessage("§c§l[BÊTE] §fVotre cri terrorise §f" + affected + " ennemi(s)!");
            player.getWorld().strikeLightningEffect(player.getLocation());
            return true;
        }
    }

    /**
     * Charge Bestiale – Bondit vers le joueur le plus proche et le repousse.
     */
    private class BeastCharge extends AbstractPower {
        public BeastCharge() {
            super(
                    "Charge Bestiale",
                    "Foncez vers le joueur ennemi le plus proche et repoussez-le.",
                    PowerType.ACTIVE,
                    45,
                    -1
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            Player target = null;
            double nearestDist = Double.MAX_VALUE;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(player)) continue;
                if (!p.getWorld().equals(player.getWorld())) continue;

                double dist = p.getLocation().distance(player.getLocation());
                if (dist < nearestDist && dist <= 20) {
                    // Vérifier que c'est un ennemi
                    if (plugin.getCampManager().areEnemies(player, p)) {
                        nearestDist = dist;
                        target = p;
                    }
                }
            }

            if (target == null) {
                player.sendMessage("§c§l[BÊTE] §cAucun ennemi à portée!");
                return false;
            }

            // Lancer Fennir vers la cible
            Vector direction = target.getLocation().subtract(player.getLocation()).toVector().normalize();
            player.setVelocity(direction.multiply(1.8).setY(0.4));

            // Repousser la cible
            Player finalTarget = target;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Vector knockback = finalTarget.getLocation().subtract(player.getLocation()).toVector().normalize();
                finalTarget.setVelocity(knockback.multiply(1.5).setY(0.5));
                finalTarget.damage(6.0, player);
                finalTarget.sendMessage("§c§l[!] §fFennir vous percute de plein fouet!");
            }, 5L);

            player.sendMessage("§c§l[BÊTE] §fVous chargez sur §f" + target.getName() + "§c!");
            return true;
        }
    }

    /**
     * Meute Sauvage – Invoque des loups (ultime unique).
     */
    private class WildPack extends AbstractPower {
        public WildPack() {
            super(
                    "Meute Sauvage",
                    "Invoquez 3 loups qui attaquent vos ennemis.",
                    PowerType.ULTIMATE,
                    0,
                    1
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            Bukkit.broadcastMessage("§c§l☠ MEUTE SAUVAGE ☠");
            Bukkit.broadcastMessage("§7" + player.getName() + " libère sa meute!");

            Location loc = player.getLocation();
            for (int i = 0; i < 3; i++) {
                Entity entity = loc.getWorld().spawnEntity(
                        loc.clone().add((i - 1) * 2, 0, 1),
                        EntityType.WOLF
                );
                if (entity instanceof Wolf) {
                    Wolf wolf = (Wolf) entity;
                    wolf.setTamed(true);
                    wolf.setOwner(player);
                    wolf.setAngry(true);
                    wolf.setMaxHealth(20.0);
                    wolf.setHealth(20.0);
                    wolf.setCustomName("§c§lMeute de Fennir");
                    wolf.setCustomNameVisible(true);
                    summonedWolves.add(wolf);
                }
            }

            player.sendMessage("§c§l[BÊTE] §fVotre meute est libérée!");
            return true;
        }
    }
}
