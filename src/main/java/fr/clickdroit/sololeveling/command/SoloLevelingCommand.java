package fr.clickdroit.sololeveling.command;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.role.Role;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Commande principale /sololeveling pour la gestion du mode.
 */
public class SoloLevelingCommand implements CommandExecutor {

    private final SoloLevelingPlugin plugin;

    public SoloLevelingCommand(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                break;

            case "roles":
                sendRolesList(sender);
                break;

            case "camps":
                sendCampsList(sender);
                break;

            case "info":
                sendInfo(sender);
                break;

            case "reload":
                if (!sender.hasPermission("sololeveling.admin")) {
                    sender.sendMessage("§cVous n'avez pas la permission!");
                    return true;
                }
                sender.sendMessage("§aConfiguration rechargée!");
                break;

            default:
                sender.sendMessage("§cCommande inconnue. Utilisez /sl help");
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§5§l━━━━━ SOLO LEVELING UHC ━━━━━");
        sender.sendMessage("");
        sender.sendMessage("  §e/sl help §7- Affiche cette aide");
        sender.sendMessage("  §e/sl roles §7- Liste des rôles disponibles");
        sender.sendMessage("  §e/sl camps §7- Information sur les camps");
        sender.sendMessage("  §e/sl info §7- Informations sur le plugin");
        sender.sendMessage("");
        sender.sendMessage("  §e/role §7- Affiche votre rôle");
        sender.sendMessage("  §e/powers §7- Affiche vos pouvoirs");
        sender.sendMessage("");
        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void sendRolesList(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§5§l━━━━━ RÔLES DISPONIBLES ━━━━━");
        sender.sendMessage("");

        for (Camp camp : Camp.values()) {
            List<Role> roles = plugin.getRoleManager().getRolesByCamp(camp);
            if (!roles.isEmpty()) {
                sender.sendMessage(
                        camp.getColorCode() + "§l" + camp.getDisplayName() + " §7(" + roles.size() + " rôles)");
                for (Role role : roles) {
                    sender.sendMessage("  §8• " + role.getRarity().getColorCode() + role.getName());
                }
                sender.sendMessage("");
            }
        }

        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void sendCampsList(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§5§l━━━━━━━ LES CAMPS ━━━━━━━");
        sender.sendMessage("");

        for (Camp camp : Camp.values()) {
            sender.sendMessage(camp.getColorCode() + "§l" + camp.getDisplayName());
            sender.sendMessage("  §7" + camp.getObjective());
            sender.sendMessage("");
        }

        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§5§l━━━━━━ INFORMATIONS ━━━━━━");
        sender.sendMessage("");
        sender.sendMessage("  §7Plugin: §5Solo Leveling UHC");
        sender.sendMessage("  §7Version: §f1.0.0");
        sender.sendMessage("  §7Auteur: §fClickdroit");
        sender.sendMessage("");
        sender.sendMessage("  §7Rôles enregistrés: §e" + plugin.getRoleManager().getRegisteredRolesCount());
        sender.sendMessage("  §7Camps: §e" + Camp.values().length);
        sender.sendMessage("");
        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
