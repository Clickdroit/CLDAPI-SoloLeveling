package fr.clickdroit.sololeveling.role.monarchs;

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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Ashborn - Le Roi des Ombres Originel
 * L'ancien monarque qui a donné ses pouvoirs à Sung Jin-Woo.
 * 
 * Pouvoirs:
 * - Ombre Originelle: Extrait l'ombre d'une cible tuée (révèle son rôle)
 * - Domaine des Ombres: Zone d'ombre qui affaiblit les ennemis
 */
@RoleInfo(name = "Ashborn", description = "Le Roi des Ombres originel.", camp = Camp.MONARCHS, rarity = RoleRarity.LEGENDARY, lore = {
        "§7Le premier Roi des Ombres,",
        "§7qui a choisi un successeur digne.",
        "",
        "§5Ombre Originelle:",
        "§7Extrait l'ombre de votre victime",
        "§7pour connaître son rôle.",
        "",
        "§5Domaine des Ombres:",
        "§7Crée une zone qui affaiblit",
        "§7tous les non-monarques proches."
})
public class Ashborn extends AbstractRole {

    private Set<String> extractedShadows = new HashSet<>();

    @Override
    protected void initPowers() {
        addPower(new OriginalShadow());
        addPower(new ShadowDomain());
    }

    @Override
    protected Material getIconMaterial() {
        return Material.ENDER_PEARL;
    }

    @Override
    public void onKill(Player player, Player victim) {
        // Extraction d'ombre automatique
        RolePlayer rp = plugin.getRoleManager().getRolePlayer(victim.getUniqueId());
        if (rp != null && rp.getRole() != null) {
            String roleName = rp.getRole().getName();
            extractedShadows.add(roleName);

            player.sendMessage("");
            player.sendMessage("§5§l§m                                                §r");
            player.sendMessage("");
            player.sendMessage("  §5§l[OMBRE] §fExtraction d'Ombre réussie!");
            player.sendMessage("");
            player.sendMessage("  §7Rôle de §f" + victim.getName() + "§7:");
            player.sendMessage("  §5» §f" + roleName);
            player.sendMessage("  §7Camp: " + rp.getRole().getCamp().getColoredName());
            player.sendMessage("");
            player.sendMessage("§5§l§m                                                §r");
            player.sendMessage("");
        }

        // Régénération sur kill
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
    }

    public Set<String> getExtractedShadows() {
        return extractedShadows;
    }

    // === POUVOIRS ===

    /**
     * Ombre Originelle - Pouvoir passif d'extraction
     */
    private class OriginalShadow extends AbstractPower {
        public OriginalShadow() {
            super(
                    "Ombre Originelle",
                    "Extrait automatiquement l'ombre des victimes pour connaître leur rôle.",
                    PowerType.PASSIVE,
                    0,
                    -1);
        }

        @Override
        protected boolean onExecute(Player player) {
            // Passif, géré dans onKill
            return true;
        }
    }

    /**
     * Domaine des Ombres - Zone d'affaiblissement
     */
    private class ShadowDomain extends AbstractPower {
        public ShadowDomain() {
            super(
                    "Domaine des Ombres",
                    "Crée un domaine qui affaiblit les non-monarques proches.",
                    PowerType.ACTIVE,
                    240, // 4 min cooldown
                    2 // 2 utilisations max
            );
        }

        @Override
        protected boolean onExecute(Player player) {
            player.sendMessage("§5§l[OMBRE] §fDomaine des Ombres déployé!");

            // Durée de 30 secondes
            for (int tick = 0; tick < 600; tick += 40) { // Toutes les 2 secondes pendant 30s
                final int currentTick = tick;
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player == null || !player.isOnline())
                        return;

                    for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                        if (rp.getRole() == null)
                            continue;
                        if (rp.getRole().getCamp() == Camp.MONARCHS)
                            continue; // Ne pas affecter les monarques

                        Player target = Bukkit.getPlayer(rp.getUuid());
                        if (target == null || target == player)
                            continue;

                        if (target.getLocation().distance(player.getLocation()) <= 15) {
                            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));

                            if (currentTick == 0) {
                                target.sendMessage("§5§l[OMBRE] §7Vous êtes dans le Domaine des Ombres d'Ashborn!");
                            }
                        }
                    }
                }, tick);
            }

            return true;
        }
    }
}
