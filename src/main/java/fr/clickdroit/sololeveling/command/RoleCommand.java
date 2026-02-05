package fr.clickdroit.sololeveling.command;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.power.Power;
import fr.clickdroit.sololeveling.role.Role;
import fr.clickdroit.sololeveling.role.RolePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande /role pour afficher les informations du rôle.
 */
public class RoleCommand implements CommandExecutor {

    private final SoloLevelingPlugin plugin;

    public RoleCommand(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs!");
            return true;
        }

        Player player = (Player) sender;
        RolePlayer rp = plugin.getRoleManager().getRolePlayer(player.getUniqueId());

        if (rp == null || rp.getRole() == null) {
            player.sendMessage("§cVous n'avez pas de rôle assigné!");
            return true;
        }

        if (!rp.isRoleRevealed()) {
            player.sendMessage("§cVotre rôle n'a pas encore été révélé!");
            return true;
        }

        Role role = rp.getRole();

        player.sendMessage("");
        player.sendMessage("§5§l━━━━━━━━━ VOTRE RÔLE ━━━━━━━━━");
        player.sendMessage("");
        player.sendMessage("  §7Nom: " + role.getCamp().getColorCode() + "§l" + role.getName());
        player.sendMessage("  §7Rareté: " + role.getRarity().getColoredName());
        player.sendMessage("  §7Camp: " + role.getCamp().getColoredName());
        player.sendMessage("");
        player.sendMessage("  §7" + role.getDescription());
        player.sendMessage("");

        if (!role.getPowers().isEmpty()) {
            player.sendMessage("  §6Pouvoirs:");
            for (Power power : role.getPowers()) {
                player.sendMessage("  §8• §e" + power.getName() + " §7(" + power.getType().getColoredName() + "§7)");
                player.sendMessage("    §7" + power.getDescription());
            }
            player.sendMessage("");
        }

        player.sendMessage("  §7Objectif: §f" + role.getCamp().getObjective());
        player.sendMessage("");
        player.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return true;
    }
}
