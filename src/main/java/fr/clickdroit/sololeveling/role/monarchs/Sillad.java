package fr.clickdroit.sololeveling.role.monarchs;

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

/**
 * Sillad - Le Roi du Gel
 * Monarque contrôlant la glace et le froid.
 * 
 * Pouvoirs:
 * - Blizzard: Ralentit et endommage les ennemis
 * - Gel Éternel: Immunité au froid et résistance à la faim
 */
@RoleInfo(name = "Sillad", description = "Le Roi du Gel.", camp = Camp.MONARCHS, rarity = RoleRarity.RARE, lore = {
        "§7Le glacial Roi du Gel,",
        "§7dont le froid est mortel.",
        "",
        "§bBlizzard:",
        "§7Déclenche une tempête de neige",
        "§7qui ralentit et endommage.",
        "",
        "§bGel Éternel:",
        "§7Immunité au froid et à la faim."
})
public class Sillad extends AbstractRole {

    @Override
    protected void initPowers() {
        addPower(new Blizzard());
        addPower(new EternalFrost());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.ICE;
    }

    @Override
    public void onRoleAssigned(Player player) {
        super.onRoleAssigned(player);
        // Immunité à la faim
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onKill(Player player, Player victim) {
        player.sendMessage("§b§l[GEL] §fLe froid consume votre victime!");
        // Geler le sol autour de la victime
        freezeArea(victim.getLocation(), 3);
    }

    /**
     * Gèle une zone (effet visuel)
     */
    private void freezeArea(Location center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location loc = center.clone().add(x, 0, z);
                Block block = loc.getBlock();
                if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
                    block.setType(Material.ICE);
                    // Programmer le retour à l'eau
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (block.getType() == Material.ICE) {
                            block.setType(Material.WATER);
                        }
                    }, 400L); // 20 secondes
                }
            }
        }
    }

    // === POUVOIRS ===

    /**
     * Blizzard - Zone de ralentissement et dégâts
     */
    private class Blizzard extends AbstractPower {
        public Blizzard() {
            super(
                    "Blizzard",
                    "Déclenchez une tempête de neige gelant vos ennemis.",
                    PowerType.ACTIVE,
                    150, // 2.5 min cooldown
                    3 // 3 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            player.sendMessage("§b§l[GEL] §fBlizzard déclenché!");

            // Durée de 20 secondes
            for (int tick = 0; tick < 400; tick += 40) { // Toutes les 2 secondes
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player == null || !player.isOnline())
                        return;

                    for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                        if (entity instanceof Player && entity != player) {
                            Player target = (Player) entity;

                            // Effets de gel
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1)); // Lenteur II
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 60, 0)); // Mining
                                                                                                            // fatigue
                            target.damage(2.0, player); // 1 coeur

                            target.sendMessage("§b§l[GEL] §7Le blizzard vous gèle!");
                        }
                    }
                }, tick);
            }

            return true;
        }
    }

    /**
     * Gel Éternel - Pouvoir passif
     */
    private class EternalFrost extends AbstractPower {
        public EternalFrost() {
            super(
                    "Gel Éternel",
                    "Le froid qui vous habite vous protège de la faim.",
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
