package fr.clickdroit.sololeveling.listener;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.role.RolePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener pour les événements liés aux rôles.
 */
public class RoleListener implements Listener {

    private final SoloLevelingPlugin plugin;

    public RoleListener(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Vérifier si le joueur a un rôle
        RolePlayer rp = plugin.getRoleManager().getRolePlayer(event.getPlayer().getUniqueId());
        if (rp != null && rp.getRole() != null && rp.isRoleRevealed()) {
            event.getPlayer().sendMessage("§7Rappel de votre rôle: " +
                    rp.getRole().getCamp().getColorCode() + rp.getRole().getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Géré par l'API parente
    }
}
