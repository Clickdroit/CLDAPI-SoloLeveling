package fr.clickdroit.sololeveling.role.rulers;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RolePlayer;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Gardien - Le Protecteur Divin
 * Dirigeant spécialisé dans la protection des alliés.
 * 
 * Pouvoirs:
 * - Bouclier Divin: Donne une résistance massive à un allié ciblé
 * - Aura Protectrice: Réduit passif les dégâts pour les alliés proches
 */
@RoleInfo(name = "Gardien", description = "Protecteur des Dirigeants.", camp = Camp.RULERS, rarity = RoleRarity.RARE, lore = {
        "§7Le gardien vigilant,",
        "§7qui protège ses alliés.",
        "",
        "§eBouclier Divin:",
        "§7Accorde une protection massive",
        "§7à un allié Dirigeant.",
        "",
        "§eAura Protectrice:",
        "§7Les alliés proches reçoivent",
        "§7une résistance aux dégâts."
})
public class Guardian extends AbstractRole {

    private UUID protectedAlly = null;

    @Override
    protected void initPowers() {
        addPower(new DivineShield());
        addPower(new ProtectiveAura());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.IRON_CHESTPLATE;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Résistance personnelle permanente
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onTick(int gameTime) {
        Player owner = getOwner();
        if (owner == null)
            return;

        // Aura Protectrice passive toutes les 10 secondes
        if (gameTime % 200 == 0) {
            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() != null && rp.getRole().getCamp() == Camp.RULERS) {
                    Player ally = Bukkit.getPlayer(rp.getUuid());
                    if (ally != null && ally != owner && ally.getLocation().distance(owner.getLocation()) <= 15) {
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 220, 0));
                    }
                }
            }
        }
    }

    @Override
    public void onDeath(Player player, Player killer) {
        // Le bouclier divin expire si le Gardien meurt
        protectedAlly = null;
    }

    // === POUVOIRS ===

    /**
     * Bouclier Divin - Protection ciblée
     */
    private class DivineShield extends AbstractPower {
        public DivineShield() {
            super(
                    "Bouclier Divin",
                    "Accordez une protection divine à un allié Dirigeant.",
                    PowerType.ACTIVE,
                    240, // 4 min cooldown
                    2 // 2 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            // Trouver le Dirigeant le plus proche
            Player closestRuler = null;
            double closestDist = Double.MAX_VALUE;

            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() != null && rp.getRole().getCamp() == Camp.RULERS) {
                    Player ally = Bukkit.getPlayer(rp.getUuid());
                    if (ally != null && ally != player && ally.isOnline()) {
                        double dist = ally.getLocation().distance(player.getLocation());
                        if (dist < closestDist && dist <= 25) {
                            closestDist = dist;
                            closestRuler = ally;
                        }
                    }
                }
            }

            if (closestRuler == null) {
                player.sendMessage("§c§l[GARDIEN] §cAucun allié Dirigeant à proximité! (25 blocs max)");
                return false;
            }

            protectedAlly = closestRuler.getUniqueId();

            // Appliquer le bouclier divin
            closestRuler.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 800, 2)); // Résistance
                                                                                                        // III 40s
            closestRuler.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 800, 1)); // Regen II 40s
            closestRuler.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 800, 3)); // Absorption IV 40s

            closestRuler.sendMessage("§e§l[GARDIEN] §f" + player.getName() + " §7vous accorde sa protection divine!");
            player.sendMessage("§e§l[GARDIEN] §fBouclier Divin accordé à §e" + closestRuler.getName() + "§f!");

            return true;
        }
    }

    /**
     * Aura Protectrice - Pouvoir passif
     */
    private class ProtectiveAura extends AbstractPower {
        public ProtectiveAura() {
            super(
                    "Aura Protectrice",
                    "Votre présence renforce la défense des alliés proches.",
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
