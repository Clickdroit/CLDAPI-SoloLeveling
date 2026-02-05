package fr.clickdroit.sololeveling.role.monarchs;

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

/**
 * Querehsha - La Reine des Insectes
 * Monarque contrôlant les insectes venimeux.
 * 
 * Pouvoirs:
 * - Essaim Venimeux: Empoisonne tous les ennemis proches
 * - Carapace: Résistance passive aux dégâts
 */
@RoleInfo(name = "Querehsha", description = "La Reine des Insectes.", camp = Camp.MONARCHS, rarity = RoleRarity.EPIC, lore = {
        "§7La terrifiante Reine des Insectes,",
        "§7dont le venin est mortel.",
        "",
        "§2Essaim Venimeux:",
        "§7Empoisonne tous les ennemis",
        "§7dans un rayon de 12 blocs.",
        "",
        "§2Carapace:",
        "§7Résistance passive aux dégâts."
})
public class Querehsha extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new VenomousSwarm());
        addPower(new Carapace());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.SPIDER_EYE;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Résistance passive aux dégâts
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Empoisonner la victime même à la mort (effet narratif)
        player.sendMessage("§2§l[INSECTE] §fVotre venin consume sa proie!");
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
    }

    @Override
    public void onTick(int gameTime) {
        Player owner = getOwner();
        if (owner == null)
            return;

        // Poison passif subtil sur les joueurs très proches (3 blocs)
        if (gameTime % 100 == 0) { // Toutes les 5 secondes
            for (Entity entity : owner.getNearbyEntities(3, 3, 3)) {
                if (entity instanceof Player && entity != owner) {
                    Player target = (Player) entity;
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0)); // Poison léger
                }
            }
        }
    }

    // === POUVOIRS ===

    /**
     * Essaim Venimeux - AoE poison
     */
    private class VenomousSwarm extends AbstractPower {
        public VenomousSwarm() {
            super(
                    "Essaim Venimeux",
                    "Lâchez un essaim d'insectes qui empoisonne tous les ennemis.",
                    PowerType.ACTIVE,
                    120, // 2 min cooldown
                    4 // 4 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int playersHit = 0;

            for (Entity entity : player.getNearbyEntities(12, 12, 12)) {
                if (entity instanceof Player && entity != player) {
                    Player target = (Player) entity;

                    // Poison sévère + nausée + faim
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1)); // Poison II 10s
                    target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0)); // Nausée 10s
                    target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 1)); // Faim II 10s
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0)); // Lenteur 5s

                    target.sendMessage("§2§l[ESSAIM] §fQuerehsha vous envoie son essaim venimeux!");
                    playersHit++;
                }
            }

            player.sendMessage("§2§l[INSECTE] §fEssaim Venimeux déployé! §7(" + playersHit + " joueurs empoisonnés)");

            return true;
        }
    }

    /**
     * Carapace - Pouvoir passif de résistance
     */
    private class Carapace extends AbstractPower {
        public Carapace() {
            super(
                    "Carapace",
                    "Votre exosquelette vous accorde une résistance aux dégâts.",
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
