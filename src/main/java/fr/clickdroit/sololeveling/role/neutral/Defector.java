package fr.clickdroit.sololeveling.role.neutral;

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

/**
 * Transfuge - L'Espion
 * Neutre qui peut changer de camp en cours de partie.
 * 
 * Pouvoirs:
 * - Trahison: Change de camp pour rejoindre une faction
 * - Infiltration: Se fait passer pour un autre camp temporairement
 */
@RoleInfo(name = "Transfuge", description = "Espion qui peut changer de camp.", camp = Camp.NEUTRAL, rarity = RoleRarity.EPIC, lore = {
        "§7N'appartient à aucun camp,",
        "§7mais peut tous les rejoindre.",
        "",
        "§8Trahison:",
        "§7Change définitivement de camp",
        "§7pour rejoindre les Chasseurs ou Monarques.",
        "",
        "§8Infiltration:",
        "§7Se fait passer pour un autre",
        "§7camp pendant 1 minute."
})
public class Defector extends AbstractRole {

    private Camp currentCamp = Camp.NEUTRAL;
    private boolean hasSwitched = false;
    private Camp infiltratedCamp = null;

    @Override
    protected void initPowers() {
        addPower(new Betrayal());
        addPower(new Infiltration());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.COMPASS;
    }

    @Override
    public Camp getCamp() {
        // Si infiltré, retourne le camp infiltré
        if (infiltratedCamp != null) {
            return infiltratedCamp;
        }
        return currentCamp;
    }

    @Override
    public void onGameStart() {
        Player owner = getOwner();
        if (owner != null) {
            owner.sendMessage("§8§l[TRANSFUGE] §7Vous êtes un espion. Choisissez votre camp avec sagesse...");
            owner.sendMessage("§7Utilisez §f/power §7pour accéder à vos pouvoirs.");
        }
    }

    // === POUVOIRS ===

    /**
     * Trahison - Change de camp définitivement
     */
    private class Betrayal extends AbstractPower {
        public Betrayal() {
            super(
                    "Trahison",
                    "Changez définitivement de camp. (Une seule fois)",
                    PowerType.ACTIVE,
                    0, // Pas de cooldown
                    1 // 1 seule utilisation
            );
        }

        @Override
        protected boolean canExecute(Player player) {
            if (hasSwitched) {
                player.sendMessage("§c§l[TRANSFUGE] §cVous avez déjà changé de camp!");
                return false;
            }
            return true;
        }

        @Override
        protected boolean onExecute(Player player) {
            // Par défaut, rejoindre les Chasseurs
            // Dans un vrai système, on aurait un menu de sélection
            currentCamp = Camp.HUNTERS;
            hasSwitched = true;

            player.sendMessage("");
            player.sendMessage("§8§l§m                                                §r");
            player.sendMessage("");
            player.sendMessage("  §8§l⚔ TRAHISON ⚔");
            player.sendMessage("");
            player.sendMessage("  §fVous avez rejoint les §b" + currentCamp.getDisplayName() + "§f!");
            player.sendMessage("  §7Votre objectif est maintenant le leur.");
            player.sendMessage("");
            player.sendMessage("§8§l§m                                                §r");
            player.sendMessage("");

            // Annoncer aux membres du nouveau camp
            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() != null && rp.getRole().getCamp() == currentCamp) {
                    Player ally = Bukkit.getPlayer(rp.getUuid());
                    if (ally != null && ally != player) {
                        ally.sendMessage("§8§l[INFO] §fUn Transfuge a rejoint votre camp!");
                    }
                }
            }

            // Bonus de changement de camp
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 0));

            return true;
        }
    }

    /**
     * Infiltration - Se fait passer pour un autre camp
     */
    private class Infiltration extends AbstractPower {
        public Infiltration() {
            super(
                    "Infiltration",
                    "Se faire passer pour un autre camp pendant 1 minute.",
                    PowerType.ACTIVE,
                    300, // 5 min cooldown
                    3 // 3 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            // Alterner entre les camps
            if (infiltratedCamp == null || infiltratedCamp == Camp.MONARCHS) {
                infiltratedCamp = Camp.HUNTERS;
            } else {
                infiltratedCamp = Camp.MONARCHS;
            }

            player.sendMessage("§8§l[INFILTRATION] §fVous vous faites passer pour les §b"
                    + infiltratedCamp.getDisplayName() + "§f!");
            player.sendMessage("§7Durée: 1 minute. Les détections vous verront comme un allié.");

            // Fin de l'infiltration après 1 minute
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                infiltratedCamp = null;
                if (player != null && player.isOnline()) {
                    player.sendMessage("§8§l[INFILTRATION] §7L'infiltration prend fin.");
                }
            }, 1200L); // 1 minute

            return true;
        }
    }
}
