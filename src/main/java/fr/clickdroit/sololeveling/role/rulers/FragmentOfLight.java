package fr.clickdroit.sololeveling.role.rulers;

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

import java.util.UUID;

/**
 * Fragment de Lumière - L'Émissaire Divin
 * Dirigeant porteur de la lumière, capable de ressusciter les alliés.
 * 
 * Pouvoirs:
 * - Résurrection: Peut ressusciter un allié mort une fois
 * - Lumière Purificatrice: Soigne et protège les alliés proches
 */
@RoleInfo(name = "Fragment de Lumière", description = "Émissaire des Dirigeants.", camp = Camp.RULERS, rarity = RoleRarity.EPIC, lore = {
        "§7Porteur de la lumière divine,",
        "§7capable de défier la mort.",
        "",
        "§eLumière Purificatrice:",
        "§7Soigne tous les Dirigeants",
        "§7proches de 4 cœurs.",
        "",
        "§e✦ Résurrection (Ultime):",
        "§7Peut ressusciter un allié",
        "§7mort une fois par partie."
})
public class FragmentOfLight extends AbstractRole {

    private boolean resurrectionUsed = false;
    private UUID resurrectedPlayer = null;

    @Override
    protected void initPowers() {
        addPower(new PurifyingLight());
        addPower(new Resurrection());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.GLOWSTONE_DUST;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Aura lumineuse permanente (vision nocturne représente la lumière en 1.8)
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onKill(Player player, Player victim) {
        player.sendMessage("§e§l[LUMIÈRE] §fLa lumière consume les ténèbres!");
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
    }

    /**
     * Vérifie si la résurrection a été utilisée
     */
    public boolean isResurrectionUsed() {
        return resurrectionUsed;
    }

    // === POUVOIRS ===

    /**
     * Lumière Purificatrice - Soin de zone
     */
    private class PurifyingLight extends AbstractPower {
        public PurifyingLight() {
            super(
                    "Lumière Purificatrice",
                    "Baignez vos alliés dans la lumière pour les soigner.",
                    PowerType.ACTIVE,
                    180, // 3 min cooldown
                    3 // 3 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int alliesHealed = 0;

            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() != null && rp.getRole().getCamp() == Camp.RULERS) {
                    Player ally = Bukkit.getPlayer(rp.getUuid());
                    if (ally != null && ally.isOnline() && rp.isAlive()) {
                        if (ally.getLocation().distance(player.getLocation()) <= 20) {
                            // Soin de 8 HP (4 cœurs)
                            double newHealth = Math.min(ally.getMaxHealth(), ally.getHealth() + 8);
                            ally.setHealth(newHealth);

                            // Effets positifs
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 1));

                            ally.sendMessage("§e§l[LUMIÈRE] §fVous êtes baigné dans la lumière purificatrice!");
                            alliesHealed++;
                        }
                    }
                }
            }

            player.sendMessage("§e§l[LUMIÈRE] §fLumière Purificatrice! §7(" + alliesHealed + " alliés soignés)");

            return true;
        }
    }

    /**
     * Résurrection - Pouvoir ultime unique
     */
    private class Resurrection extends AbstractPower {
        public Resurrection() {
            super(
                    "Résurrection",
                    "Ressuscitez un allié mort au combat.",
                    PowerType.ACTIVE,
                    0, // Pas de cooldown
                    1 // 1 seule utilisation
            );
        }

        @Override
        protected boolean canExecute(Player player) {
            if (resurrectionUsed) {
                player.sendMessage("§c§l[LUMIÈRE] §cVous avez déjà utilisé votre résurrection!");
                return false;
            }
            return true;
        }

        @Override
        protected boolean onExecute(Player player) {
            // Trouver un allié mort (Ruler)
            RolePlayer deadAlly = null;

            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() != null
                        && rp.getRole().getCamp() == Camp.RULERS
                        && !rp.isAlive()
                        && Bukkit.getPlayer(rp.getUuid()) == null) {
                    deadAlly = rp;
                    break;
                }
            }

            if (deadAlly == null) {
                player.sendMessage("§c§l[LUMIÈRE] §cAucun allié Dirigeant mort à ressusciter!");
                return false;
            }

            // Marquer la résurrection comme utilisée
            resurrectionUsed = true;
            resurrectedPlayer = deadAlly.getUuid();

            // Annoncer
            player.sendMessage("");
            player.sendMessage("§e§l§m                                                §r");
            player.sendMessage("");
            player.sendMessage("  §e§l✦ RÉSURRECTION ✦");
            player.sendMessage("");
            player.sendMessage("  §7Vous avez ressuscité un allié Dirigeant!");
            player.sendMessage("  §7Il reviendra à la vie sous peu.");
            player.sendMessage("");
            player.sendMessage("§e§l§m                                                §r");
            player.sendMessage("");

            // Note: La vraie résurrection dépend du système de respawn du serveur
            // Ici on marque juste que le joueur peut être ressuscité
            deadAlly.setAlive(true);

            return true;
        }
    }
}
