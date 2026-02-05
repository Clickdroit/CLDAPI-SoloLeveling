package fr.clickdroit.sololeveling.role.neutral;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.AbstractRole;
import fr.clickdroit.sololeveling.role.RoleInfo;
import fr.clickdroit.sololeveling.role.RoleRarity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Architecte - Le Créateur de Donjons
 * Neutre manipulant l'environnement pour créer des pièges.
 * 
 * Pouvoirs:
 * - Piège: Place un piège invisible qui ralentit et endommage
 * - Vision du Bâtisseur: Détecte les joueurs dans les grottes/souterrains
 */
@RoleInfo(name = "Architecte", description = "Créateur de donjons.", camp = Camp.NEUTRAL, rarity = RoleRarity.RARE, lore = {
        "§7Le mystérieux Architecte,",
        "§7qui façonne le monde à sa guise.",
        "",
        "§8Piège Mortel:",
        "§7Place un piège invisible",
        "§7qui endommage les passants.",
        "",
        "§8Vision du Bâtisseur:",
        "§7Détecte les joueurs souterrains."
})
public class Architect extends AbstractRole {

    private List<Location> trapLocations = new ArrayList<>();
    private static final int MAX_TRAPS = 5;

    @Override
    protected void initPowers() {
        addPower(new DeadlyTrap());
        addPower(new BuilderVision());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.BEDROCK;
    }

    @Override
    public void onTick(int gameTime) {
        Player owner = getOwner();
        if (owner == null)
            return;

        // Vérifier les pièges toutes les secondes
        if (gameTime % 20 == 0) {
            checkTraps();
        }
    }

    /**
     * Vérifie si des joueurs déclenchent les pièges
     */
    private void checkTraps() {
        Player owner = getOwner();
        if (owner == null)
            return;

        for (Location trapLoc : new ArrayList<>(trapLocations)) {
            for (Entity entity : trapLoc.getWorld().getNearbyEntities(trapLoc, 2, 2, 2)) {
                if (entity instanceof Player && entity != owner) {
                    Player target = (Player) entity;

                    // Déclencher le piège
                    target.damage(4.0, owner); // 2 coeurs
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));

                    target.sendMessage("§8§l[PIÈGE] §fVous avez déclenché un piège de l'Architecte!");
                    owner.sendMessage("§8§l[PIÈGE] §f" + target.getName() + " a déclenché votre piège!");

                    // Retirer le piège après déclenchement
                    trapLocations.remove(trapLoc);
                    break;
                }
            }
        }
    }

    // === POUVOIRS ===

    /**
     * Piège Mortel - Place un piège
     */
    private class DeadlyTrap extends AbstractPower {
        public DeadlyTrap() {
            super(
                    "Piège Mortel",
                    "Placez un piège invisible à votre position.",
                    PowerType.ACTIVE,
                    60, // 1 min cooldown
                    8 // 8 utilisations max
            );
        }

        @Override
        protected boolean canExecute(Player player) {
            if (trapLocations.size() >= MAX_TRAPS) {
                player.sendMessage("§c§l[PIÈGE] §cVous avez déjà " + MAX_TRAPS + " pièges actifs!");
                return false;
            }
            return true;
        }

        @Override
        protected boolean onExecute(Player player) {
            Location loc = player.getLocation().clone();
            trapLocations.add(loc);

            player.sendMessage("§8§l[ARCHITECTE] §fPiège placé! §7(" + trapLocations.size() + "/" + MAX_TRAPS + ")");
            player.sendMessage("§7Position: X=" + loc.getBlockX() + " Y=" + loc.getBlockY() + " Z=" + loc.getBlockZ());

            return true;
        }
    }

    /**
     * Vision du Bâtisseur - Détection souterraine
     */
    private class BuilderVision extends AbstractPower {
        public BuilderVision() {
            super(
                    "Vision du Bâtisseur",
                    "Détectez les joueurs sous terre dans un grand rayon.",
                    PowerType.ACTIVE,
                    120, // 2 min cooldown
                    -1 // Illimité
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            int playersFound = 0;

            player.sendMessage("");
            player.sendMessage("§8§l[VISION] §fJoueurs souterrains détectés:");
            player.sendMessage("");

            for (Player target : player.getWorld().getPlayers()) {
                if (target == player)
                    continue;
                if (target.getLocation().distance(player.getLocation()) > 50)
                    continue;

                // Vérifier si le joueur est sous terre (Y < 60 ou bloc au-dessus)
                Location loc = target.getLocation();
                Block above = loc.clone().add(0, 2, 0).getBlock();

                if (loc.getBlockY() < 60 || above.getType().isSolid()) {
                    double distance = loc.distance(player.getLocation());

                    player.sendMessage("§8• §fJoueur détecté sous terre");
                    player.sendMessage("  §7Y: §f" + loc.getBlockY() +
                            " §7Distance: §f" + String.format("%.0f", distance) + " blocs");

                    // Position déjà affichée en texte - pas d'effet visuel en 1.8
                    playersFound++;
                }
            }

            if (playersFound == 0) {
                player.sendMessage("§7Aucun joueur souterrain détecté.");
            } else {
                player.sendMessage("");
                player.sendMessage("§8§l[VISION] §7" + playersFound + " joueur(s) détecté(s)!");
            }

            return true;
        }
    }
}
