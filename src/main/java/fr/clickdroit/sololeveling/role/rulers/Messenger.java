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

/**
 * Messager - L'Informateur Divin
 * Dirigeant spécialisé dans la collecte d'informations.
 * 
 * Pouvoirs:
 * - Vision Divine: Révèle la position des Monarques
 * - Télépathie: Envoie un message privé à un allié
 */
@RoleInfo(name = "Messager", description = "Informateur des Dirigeants.", camp = Camp.RULERS, rarity = RoleRarity.UNCOMMON, lore = {
        "§7L'œil vigilant des Dirigeants,",
        "§7qui voit tout et sait tout.",
        "",
        "§eVision Divine:",
        "§7Révèle la position de tous",
        "§7les Monarques en vie.",
        "",
        "§eTélépathie:",
        "§7Communiquez secrètement avec",
        "§7vos alliés Dirigeants."
})
public class Messenger extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new DivineVision());
        addPower(new Telepathy());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.EYE_OF_ENDER;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Vision nocturne pour mieux observer
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onGameStart() {
        Player owner = getOwner();
        if (owner != null) {
            owner.sendMessage("§e§l[MESSAGER] §7Votre mission: surveiller les Monarques et informer vos alliés.");
        }
    }

    // === POUVOIRS ===

    /**
     * Vision Divine - Révèle les Monarques
     */
    private class DivineVision extends AbstractPower {
        public DivineVision() {
            super(
                    "Vision Divine",
                    "Révèle la position de tous les Monarques en vie.",
                    PowerType.ACTIVE,
                    120, // 2 min cooldown
                    -1 // Illimité
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int monarchsFound = 0;

            player.sendMessage("");
            player.sendMessage("§e§l[VISION] §fPositions des Monarques:");
            player.sendMessage("");

            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.getRole() != null && rp.getRole().getCamp() == Camp.MONARCHS && rp.isAlive()) {
                    Player monarch = Bukkit.getPlayer(rp.getUuid());
                    if (monarch != null && monarch.isOnline()) {
                        Location loc = monarch.getLocation();
                        double distance = loc.distance(player.getLocation());

                        // Ne pas révéler le rôle exact, juste la position
                        player.sendMessage("§c• Monarque détecté §7(Camp ennemi)");
                        player.sendMessage("  §7X: §f" + loc.getBlockX() +
                                " §7Y: §f" + loc.getBlockY() +
                                " §7Z: §f" + loc.getBlockZ() +
                                " §7Distance: §f" + String.format("%.0f", distance) + " blocs");

                        // Position déjà affichée en texte - pas d'effet visuel en 1.8

                        monarchsFound++;
                    }
                }
            }

            if (monarchsFound == 0) {
                player.sendMessage("§7Aucun Monarque détecté en vie.");
            } else {
                player.sendMessage("");
                player.sendMessage("§e§l[VISION] §7" + monarchsFound + " Monarque(s) localisé(s)!");

                // Partager avec les alliés Dirigeants
                for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                    if (rp.getRole() != null && rp.getRole().getCamp() == Camp.RULERS && rp.isAlive()) {
                        Player ally = Bukkit.getPlayer(rp.getUuid());
                        if (ally != null && ally != player) {
                            ally.sendMessage("§e§l[MESSAGER] §7" + player.getName() + " a localisé " + monarchsFound
                                    + " Monarque(s)!");
                        }
                    }
                }
            }

            return true;
        }
    }

    /**
     * Télépathie - Communication secrète
     */
    private class Telepathy extends AbstractPower {
        public Telepathy() {
            super(
                    "Télépathie",
                    "Envoyez un message à tous les Dirigeants alliés.",
                    PowerType.ACTIVE,
                    30, // 30 sec cooldown
                    -1 // Illimité
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            // Ce pouvoir n'est pas activable directement
            // Il faudrait un système de commande ou de chat
            player.sendMessage("§e§l[TÉLÉPATHIE] §7Utilisez §f/camp <message> §7pour communiquer avec les Dirigeants.");
            return true;
        }
    }
}
