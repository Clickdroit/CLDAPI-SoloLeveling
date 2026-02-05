package fr.clickdroit.sololeveling.role.neutral;

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

import java.util.Random;

/**
 * Éveillé - Le Joueur Évolutif
 * Neutre au potentiel inconnu qui peut s'éveiller en cours de partie.
 * 
 * Pouvoirs:
 * - Éveil: Active un boost aléatoire puissant
 * - Potentiel Caché: Bonus passif qui s'améliore avec le temps
 */
@RoleInfo(name = "Éveillé", description = "Joueur évolutif.", camp = Camp.NEUTRAL, rarity = RoleRarity.UNCOMMON, lore = {
        "§7Votre potentiel est inconnu,",
        "§7mais grandit avec le temps.",
        "",
        "§dÉveil:",
        "§7Active un boost aléatoire",
        "§7parmi plusieurs possibilités.",
        "",
        "§dPotentiel Caché:",
        "§7Vos stats augmentent",
        "§7au fil de la partie."
})
public class Awakened extends AbstractRole {

    private int awakeningLevel = 0;
    private Random random = new Random();

    @Override
    protected void initPowers() {
        addPower(new Awakening());
        addPower(new HiddenPotential());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.NETHER_STAR;
    }

    @Override
    public void onEpisode(int episode) {
        Player owner = getOwner();
        if (owner == null)
            return;

        // Le potentiel augmente à chaque épisode
        awakeningLevel++;

        owner.sendMessage("§d§l[ÉVEIL] §fVotre potentiel s'éveille... §7(Niveau " + awakeningLevel + ")");

        // Bonus progressif
        if (awakeningLevel >= 2) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
            owner.sendMessage("§d• §7Vitesse I débloquée!");
        }
        if (awakeningLevel >= 3) {
            owner.addPotionEffect(
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
            owner.sendMessage("§d• §7Résistance I débloquée!");
        }
        if (awakeningLevel >= 4) {
            owner.setMaxHealth(24); // 12 coeurs
            owner.sendMessage("§d• §7Santé maximale augmentée!");
        }
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Kill accélère l'éveil
        awakeningLevel++;
        player.sendMessage("§d§l[ÉVEIL] §fLe combat vous fait évoluer! §7(Niveau " + awakeningLevel + ")");
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
    }

    // === POUVOIRS ===

    /**
     * Éveil - Boost aléatoire
     */
    private class Awakening extends AbstractPower {
        public Awakening() {
            super(
                    "Éveil",
                    "Déclenchez un éveil aléatoire parmi plusieurs possibilités.",
                    PowerType.ACTIVE,
                    180, // 3 min cooldown
                    3 // 3 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int awakening = random.nextInt(5);

            player.sendMessage("");
            player.sendMessage("§d§l§m                                                §r");
            player.sendMessage("");
            player.sendMessage("  §d§l⚡ ÉVEIL ⚡");
            player.sendMessage("");

            switch (awakening) {
                case 0: // Guerrier
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 2)); // Force III
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 1)); // Résistance
                                                                                                          // II
                    player.sendMessage("  §c§lÉVEIL DU GUERRIER");
                    player.sendMessage("  §7Force III + Résistance II (30s)");
                    break;

                case 1: // Chasseur
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 2)); // Vitesse III
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 600, 2)); // Saut III
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
                    player.sendMessage("  §e§lÉVEIL DU CHASSEUR");
                    player.sendMessage("  §7Vitesse III + Saut III (30s)");
                    break;

                case 2: // Guérisseur
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 2)); // Regen III
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 4)); // Absorption V
                    player.setHealth(player.getMaxHealth());
                    player.sendMessage("  §a§lÉVEIL DU GUÉRISSEUR");
                    player.sendMessage("  §7Régénération III + Soin complet");
                    break;

                case 3: // Fantôme
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 400, 0)); // Invisibilité 20s
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1));
                    player.sendMessage("  §7§lÉVEIL DU FANTÔME");
                    player.sendMessage("  §7Invisibilité + Vitesse II (20s)");
                    break;

                case 4: // Berserker
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 400, 3)); // Force IV
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 2)); // Vitesse III
                    // Contrepartie: moins de défense
                    player.sendMessage("  §4§lÉVEIL DU BERSERKER");
                    player.sendMessage("  §7Force IV + Vitesse III (20s)");
                    player.sendMessage("  §c§oAttention: Pas de défense!");
                    break;
            }

            player.sendMessage("");
            player.sendMessage("§d§l§m                                                §r");
            player.sendMessage("");

            return true;
        }
    }

    /**
     * Potentiel Caché - Passif progressif
     */
    private class HiddenPotential extends AbstractPower {
        public HiddenPotential() {
            super(
                    "Potentiel Caché",
                    "Vos capacités augmentent au fil du temps.",
                    PowerType.PASSIVE,
                    0,
                    -1);
        }

        @Override
        protected boolean onExecute(Player player) {
            // Passif, géré dans onEpisode et onKill
            return true;
        }
    }
}
