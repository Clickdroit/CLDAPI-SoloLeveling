package fr.clickdroit.sololeveling.role.hunters;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RolePlayer;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Yoo Jin-Ho - L'Assistant Loyal
 * Chasseur de rang B, bras droit de Sung Jin-Woo.
 * 
 * Pouvoirs:
 * - Soutien: Révèle la position d'un chasseur allié
 * - Lien Fraternel: Boost quand proche de Jin-Woo
 */
@RoleInfo(name = "Yoo Jin-Ho", description = "Chasseur de rang B, assistant de Jin-Woo.", camp = Camp.HUNTERS, rarity = RoleRarity.COMMON, lore = {
        "§7Le loyal assistant du Shadowmonarch.",
        "§7Sa dévotion est sans limite.",
        "",
        "§6Soutien Tactique:",
        "§7Révèle la position exacte",
        "§7d'un chasseur allié.",
        "",
        "§6Lien Fraternel:",
        "§7Bonus passif si Jin-Woo est proche."
})
public class YooJinHo extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new TacticalSupport());
        addPower(new BrotherlyBond());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public void onTick(int gameTime) {
        Player owner = getOwner();
        if (owner == null)
            return;

        // Lien Fraternel: Bonus si Jin-Woo est proche
        if (gameTime % 100 == 0) { // Toutes les 5 secondes
            Player jinWoo = findJinWoo();
            if (jinWoo != null && jinWoo.getLocation().distance(owner.getLocation()) <= 15) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 0)); // Force I
                owner.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 0)); // Résistance I
            }
        }
    }

    /**
     * Trouve le joueur ayant le rôle Sung Jin-Woo
     */
    private Player findJinWoo() {
        for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
            if (rp.getRole() instanceof SungJinWoo) {
                return Bukkit.getPlayer(rp.getUuid());
            }
        }
        return null;
    }

    // === POUVOIRS ===

    /**
     * Soutien Tactique - Révèle la position d'un allié
     */
    private class TacticalSupport extends AbstractPower {
        public TacticalSupport() {
            super(
                    "Soutien Tactique",
                    "Révèle la position exacte de tous les chasseurs alliés.",
                    PowerType.ACTIVE,
                    60, // 1 min cooldown
                    -1 // Illimité
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int alliesFound = 0;

            player.sendMessage("§b§l[SOUTIEN] §fPositions des chasseurs alliés:");
            player.sendMessage("");

            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() != null && rp.getRole().getCamp() == Camp.HUNTERS) {
                    Player ally = Bukkit.getPlayer(rp.getUuid());
                    if (ally != null && ally != player && ally.isOnline() && rp.isAlive()) {
                        Location loc = ally.getLocation();
                        String roleName = rp.getRole().getName();
                        double distance = loc.distance(player.getLocation());

                        player.sendMessage("§e• " + ally.getName() + " §7(" + roleName + ")");
                        player.sendMessage("  §7X: §f" + loc.getBlockX() +
                                " §7Y: §f" + loc.getBlockY() +
                                " §7Z: §f" + loc.getBlockZ() +
                                " §7Distance: §f" + String.format("%.0f", distance) + " blocs");

                        // Position déjà affichée en texte - pas d'effet visuel en 1.8

                        alliesFound++;
                    }
                }
            }

            if (alliesFound == 0) {
                player.sendMessage("§7Aucun chasseur allié trouvé en vie.");
            } else {
                player.sendMessage("");
                player.sendMessage("§b§l[SOUTIEN] §7" + alliesFound + " chasseur(s) localisé(s)!");
            }

            return true;
        }
    }

    /**
     * Lien Fraternel - Bonus passif avec Jin-Woo
     */
    private class BrotherlyBond extends AbstractPower {
        public BrotherlyBond() {
            super(
                    "Lien Fraternel",
                    "Recevez des bonus quand vous êtes proche de Sung Jin-Woo.",
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
