package fr.clickdroit.sololeveling.listener;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Listener pour le suivi des statistiques de combat.
 * Enregistre les dégâts infligés et reçus entre joueurs.
 */
public class CombatStatsListener implements Listener {

    private final SoloLevelingPlugin plugin;

    public CombatStatsListener(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // Vérifier que c'est un joueur qui inflige des dégâts à un autre joueur
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = getAttacker(event);

        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        double damage = event.getFinalDamage();

        // Enregistrer les dégâts dans les statistiques
        plugin.getStatsManager().recordDamage(attacker.getUniqueId(), victim.getUniqueId(), damage);
    }

    /**
     * Obtient l'attaquant à partir de l'événement de dégâts.
     * Gère les cas directs et indirects (projectiles).
     */
    private Player getAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }

        // Gérer les projectiles
        if (event.getDamager() instanceof org.bukkit.entity.Projectile) {
            org.bukkit.entity.Projectile projectile = (org.bukkit.entity.Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }

        return null;
    }
}
