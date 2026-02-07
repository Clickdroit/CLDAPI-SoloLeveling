package fr.clickdroit.sololeveling.listener;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.power.Power;
import fr.clickdroit.sololeveling.power.PowerType;
import fr.clickdroit.sololeveling.role.RolePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener pour l'activation des pouvoirs.
 * Gère les interactions avec les items pour activer les pouvoirs des rôles.
 */
public class PowerListener implements Listener {

    private final SoloLevelingPlugin plugin;

    public PowerListener(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        // Vérifier si c'est l'item d'activation de pouvoir (Nether Star)
        if (item == null || item.getType() != Material.NETHER_STAR) {
            return;
        }

        RolePlayer rp = plugin.getRoleManager().getRolePlayer(player.getUniqueId());
        if (rp == null || rp.getRole() == null) {
            player.sendMessage("§cVous n'avez pas de rôle!");
            return;
        }

        // Activer le premier pouvoir actif disponible
        for (Power power : rp.getRole().getPowers()) {
            PowerType type = power.getType();
            if (type == PowerType.ACTIVE || type == PowerType.ULTIMATE) {
                if (power.execute(player)) {
                    rp.addPowerUsed();
                    break;
                }
            }
        }
    }
}
