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
 * Go Gun-Hee - Le Président de l'Association
 * Leader des chasseurs coréens, spécialisé dans le support.
 * 
 * Pouvoirs:
 * - Bouclier National: Donne une résistance massive à un allié
 * - Commandement: Boost de stats pour les chasseurs proches (passif)
 */
@RoleInfo(name = "Go Gun-Hee", description = "Président de l'Association des Chasseurs.", camp = Camp.HUNTERS, rarity = RoleRarity.EPIC, lore = {
        "§7Le sage leader des chasseurs coréens.",
        "§7Protège ses alliés au péril de sa vie.",
        "",
        "§6Bouclier National:",
        "§7Accorde une protection divine",
        "§7à un chasseur allié.",
        "",
        "§6Commandement:",
        "§7Les chasseurs proches reçoivent",
        "§7un boost de résistance."
})
public class GoGunHee extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new NationalShield());
        addPower(new Leadership());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.IRON_CHESTPLATE;
    }

    @Override
    public void onTick(int gameTime) {
        Player owner = getOwner();
        if (owner == null)
            return;

        // Commandement passif: boost les chasseurs proches toutes les 10 secondes
        if (gameTime % 200 == 0) { // Toutes les 10 secondes
            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() != null && rp.getRole().getCamp() == Camp.HUNTERS) {
                    Player ally = Bukkit.getPlayer(rp.getUuid());
                    if (ally != null && ally != owner && ally.getLocation().distance(owner.getLocation()) <= 20) {
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 220, 0)); // Résistance
                                                                                                            // I
                    }
                }
            }
        }
    }

    // === POUVOIRS ===

    /**
     * Bouclier National - Protection d'un allié
     */
    private class NationalShield extends AbstractPower {
        public NationalShield() {
            super(
                    "Bouclier National",
                    "Protégez un chasseur allié avec une résistance divine.",
                    PowerType.ACTIVE,
                    240, // 4 min cooldown
                    2 // 2 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            // Trouver le chasseur le plus proche
            Player closestHunter = null;
            double closestDist = Double.MAX_VALUE;

            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() != null && rp.getRole().getCamp() == Camp.HUNTERS) {
                    Player ally = Bukkit.getPlayer(rp.getUuid());
                    if (ally != null && ally != player && ally.isOnline()) {
                        double dist = ally.getLocation().distance(player.getLocation());
                        if (dist < closestDist && dist <= 30) {
                            closestDist = dist;
                            closestHunter = ally;
                        }
                    }
                }
            }

            if (closestHunter == null) {
                player.sendMessage("§c§l[BOUCLIER] §cAucun chasseur allié à proximité! (30 blocs max)");
                return false;
            }

            // Appliquer le bouclier
            closestHunter.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 2)); // Résistance
                                                                                                         // III 30s
            closestHunter.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1)); // Regen II 30s
            closestHunter.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 2)); // Absorption III 30s

            closestHunter.sendMessage("§e§l[BOUCLIER] §f" + player.getName() + " §7vous accorde sa protection!");
            player.sendMessage("§e§l[BOUCLIER] §fProtection accordée à §e" + closestHunter.getName() + "§f!");

            return true;
        }
    }

    /**
     * Commandement - Aura passive de boost
     */
    private class Leadership extends AbstractPower {
        public Leadership() {
            super(
                    "Commandement",
                    "Votre présence renforce les chasseurs proches.",
                    PowerType.PASSIVE,
                    0,
                    -1);
        }

        @Override
        protected boolean onExecute(Player player) {
            // Passif, géré dans onTick
            return true;
        }
    }
}
