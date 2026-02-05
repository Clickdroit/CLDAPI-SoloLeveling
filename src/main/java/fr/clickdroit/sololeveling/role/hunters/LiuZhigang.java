package fr.clickdroit.sololeveling.role.hunters;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Liu Zhigang - Le Maître des Arts Martiaux
 * Chasseur National chinois, expert en combat rapproché.
 * 
 * Pouvoirs:
 * - Contre-Attaque: Renvoie une partie des dégâts reçus
 * - Posture Martiale: Mode combat avec boost de vitesse
 */
@RoleInfo(name = "Liu Zhigang", description = "Chasseur National chinois.", camp = Camp.HUNTERS, rarity = RoleRarity.RARE, lore = {
        "§7Maître incontesté des arts martiaux.",
        "§7Chaque coup reçu est renvoyé.",
        "",
        "§6Contre-Attaque:",
        "§7Active: Renvoie 50% des dégâts",
        "§7reçus pendant 15 secondes.",
        "",
        "§6Posture Martiale:",
        "§7Boost de vitesse et esquive."
})
public class LiuZhigang extends AbstractRole {

    private boolean counterActive = false;
    private Map<UUID, Long> lastCounterTime = new HashMap<>();

    @Override
    protected void initPowers() {
        addPower(new CounterStrike());
        addPower(new MartialStance());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.GOLD_SWORD;
    }

    /**
     * Appelé quand le joueur reçoit des dégâts
     * Cette méthode devrait être appelée depuis un listener externe
     */
    public void onDamageTaken(Player player, Player attacker, double damage) {
        if (!counterActive)
            return;
        if (attacker == null)
            return;

        // Cooldown de 1 seconde entre les contre-attaques
        long now = System.currentTimeMillis();
        Long lastTime = lastCounterTime.get(attacker.getUniqueId());
        if (lastTime != null && now - lastTime < 1000)
            return;

        lastCounterTime.put(attacker.getUniqueId(), now);

        // Renvoyer 50% des dégâts
        double counterDamage = damage * 0.5;
        attacker.damage(counterDamage, player);

        player.sendMessage("§e§l[CONTRE] §fVous renvoyez §c" + String.format("%.1f", counterDamage / 2) + " §f♥ à "
                + attacker.getName() + "!");
        attacker.sendMessage("§e§l[CONTRE] §fLiu Zhigang vous renvoie vos dégâts!");
    }

    public boolean isCounterActive() {
        return counterActive;
    }

    // === POUVOIRS ===

    /**
     * Contre-Attaque - Renvoie les dégâts
     */
    private class CounterStrike extends AbstractPower {
        public CounterStrike() {
            super(
                    "Contre-Attaque",
                    "Renvoyez 50% des dégâts reçus à vos attaquants.",
                    PowerType.ACTIVE,
                    120, // 2 min cooldown
                    4 // 4 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            counterActive = true;

            player.sendMessage("§e§l[MARTIAL] §fMode Contre-Attaque activé!");
            player.sendMessage("§7Les dégâts reçus seront partiellement renvoyés pendant 15 secondes.");

            // Effet visuel (vitesse légère pour indiquer le mode actif en 1.8)
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 0));

            // Désactiver après 15 secondes
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                counterActive = false;
                lastCounterTime.clear();
                player.sendMessage("§e§l[MARTIAL] §7La contre-attaque prend fin.");
            }, 300L);

            return true;
        }
    }

    /**
     * Posture Martiale - Boost de combat
     */
    private class MartialStance extends AbstractPower {
        public MartialStance() {
            super(
                    "Posture Martiale",
                    "Adoptez une posture de combat parfaite.",
                    PowerType.ACTIVE,
                    90, // 1.5 min cooldown
                    5 // 5 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            // Effets de combat
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1)); // Vitesse II 20s
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 400, 0)); // Force I 20s
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 0)); // Résistance I 20s

            player.sendMessage("§e§l[MARTIAL] §fPosture Martiale adoptée!");
            player.sendMessage("§7Vitesse II, Force I, Résistance I pendant 20 secondes.");

            return true;
        }
    }
}
