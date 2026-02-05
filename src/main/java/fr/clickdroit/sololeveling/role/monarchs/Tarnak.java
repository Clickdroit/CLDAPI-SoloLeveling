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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Tarnak - Le Roi des Bêtes
 * Monarque commandant les bêtes sauvages.
 * 
 * Pouvoirs:
 * - Meute: Invoque des loups temporaires
 * - Rage Bestiale: Boost de combat sauvage
 */
@RoleInfo(name = "Tarnak", description = "Le Roi des Bêtes.", camp = Camp.MONARCHS, rarity = RoleRarity.RARE, lore = {
        "§7Le féroce Roi des Bêtes,",
        "§7commandant des créatures sauvages.",
        "",
        "§6Meute:",
        "§7Invoque des loups enragés",
        "§7qui attaquent vos ennemis.",
        "",
        "§6Rage Bestiale:",
        "§7Déchaînez votre furie sauvage."
})
public class Tarnak extends AbstractRole {

    private List<Wolf> summonedWolves = new ArrayList<>();
    private boolean raging = false;

    @Override
    protected void initPowers() {
        addPower(new WolfPack());
        addPower(new BeastialRage());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.BONE;
    }

    @Override
    public void onKill(Player player, Player victim) {
        player.sendMessage("§6§l[BÊTE] §fLa meute hurle de victoire!");
        // Boost les loups présents
        for (Wolf wolf : summonedWolves) {
            if (wolf != null && !wolf.isDead()) {
                wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1));
            }
        }
    }

    @Override
    public void onDeath(Player player, Player killer) {
        // Désespérer les loups
        for (Wolf wolf : summonedWolves) {
            if (wolf != null && !wolf.isDead()) {
                wolf.remove();
            }
        }
        summonedWolves.clear();
    }

    public boolean isRaging() {
        return raging;
    }

    // === POUVOIRS ===

    /**
     * Meute - Invoque des loups
     */
    private class WolfPack extends AbstractPower {
        public WolfPack() {
            super(
                    "Meute",
                    "Invoquez une meute de loups enragés.",
                    PowerType.ACTIVE,
                    180, // 3 min cooldown
                    3 // 3 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            // Nettoyer les anciens loups morts
            summonedWolves.removeIf(wolf -> wolf == null || wolf.isDead());

            // Limiter à 4 loups max
            if (summonedWolves.size() >= 4) {
                player.sendMessage("§c§l[BÊTE] §cVous avez déjà trop de loups! (4 max)");
                return false;
            }

            // Invoquer 2 loups
            int toSpawn = Math.min(2, 4 - summonedWolves.size());

            for (int i = 0; i < toSpawn; i++) {
                Location spawnLoc = player.getLocation().add(
                        (Math.random() - 0.5) * 4,
                        0,
                        (Math.random() - 0.5) * 4);

                Wolf wolf = (Wolf) player.getWorld().spawnEntity(spawnLoc, EntityType.WOLF);
                wolf.setOwner(player);
                wolf.setAngry(true);
                wolf.setCustomName("§6Loup de " + player.getName());
                wolf.setCustomNameVisible(true);

                // Boost les loups
                wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));

                summonedWolves.add(wolf);
            }

            player.sendMessage("§6§l[BÊTE] §fMeute invoquée! §7(" + summonedWolves.size() + " loups actifs)");

            // Les loups disparaissent après 2 minutes
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Wolf wolf : new ArrayList<>(summonedWolves)) {
                    if (wolf != null && !wolf.isDead()) {
                        wolf.remove();
                    }
                }
                summonedWolves.clear();
                if (player != null && player.isOnline()) {
                    player.sendMessage("§6§l[BÊTE] §7La meute retourne dans les ombres...");
                }
            }, 2400L); // 2 minutes

            return true;
        }
    }

    /**
     * Rage Bestiale - Boost de combat
     */
    private class BeastialRage extends AbstractPower {
        public BeastialRage() {
            super(
                    "Rage Bestiale",
                    "Entrez dans une rage sauvage, augmentant votre puissance.",
                    PowerType.ACTIVE,
                    120, // 2 min cooldown
                    4 // 4 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            raging = true;

            // Effets de rage
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 400, 1)); // Force II 20s
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1)); // Vitesse II 20s
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0)); // Regen I 20s

            player.sendMessage("§6§l[BÊTE] §fRAGE BESTIALE!");
            player.sendMessage("§7Force II, Vitesse II, Régénération I pendant 20 secondes!");

            // Hurlement visuel
            player.getWorld().strikeLightningEffect(player.getLocation());

            // Fin de rage
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                raging = false;
                player.sendMessage("§6§l[BÊTE] §7La rage se calme...");
            }, 400L);

            return true;
        }
    }
}
