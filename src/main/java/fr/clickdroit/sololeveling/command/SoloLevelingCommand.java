package fr.clickdroit.sololeveling.command;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.role.Role;
import fr.clickdroit.sololeveling.role.RolePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Commande principale /sololeveling pour la gestion du mode.
 * Fournit des informations sur le mode de jeu, les rôles et les camps.
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

            case "stats":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cCette commande est réservée aux joueurs!");
                    return true;
                }
                plugin.getStatsManager().showPersonalStats((Player) sender);
                break;

            case "reset":
                if (!sender.hasPermission("sololeveling.admin")) {
                    sender.sendMessage("§cVous n'avez pas la permission!");
                    return true;
                }
                plugin.getGameModule().reset();
                sender.sendMessage("§a§l[SL] §fModule réinitialisé!");
                break;

            case "reload":
                if (!sender.hasPermission("sololeveling.admin")) {
                    sender.sendMessage("§cVous n'avez pas la permission!");
                    return true;
                }
                sender.sendMessage("§a§l[SL] §fConfiguration rechargée!");
                break;

            case "setrole":
                if (!sender.hasPermission("sololeveling.admin")) {
                    sender.sendMessage("§cVous n'avez pas la permission!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /sl setrole <joueur> <role>");
                    return true;
                }
                handleSetRole(sender, args[1], args[2]);
                break;

            case "config":
                if (!sender.hasPermission("sololeveling.admin")) {
                    sender.sendMessage("§cVous n'avez pas la permission!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cCette commande est réservée aux joueurs!");
                    return true;
                }
                plugin.getGameModule().openConfig((Player) sender);
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
        sender.sendMessage("  §e/sl stats §7- Vos statistiques personnelles");
        sender.sendMessage("  §e/sl info §7- Informations sur le plugin");
        sender.sendMessage("");
        sender.sendMessage("  §e/role §7- Affiche votre rôle");
        sender.sendMessage("  §e/powers §7- Affiche vos pouvoirs");
        sender.sendMessage("  §e/camp §7- Affiche votre camp");
        sender.sendMessage("  §e/cc <message> §7- Chat de camp (alliés seulement)");
        sender.sendMessage("");

        if (sender.hasPermission("sololeveling.admin")) {
            sender.sendMessage("  §c§lAdmin:");
            sender.sendMessage("  §e/sl config §7- Ouvre la configuration");
            sender.sendMessage("  §e/sl setrole <joueur> <rôle> §7- Force un rôle à un joueur");
            sender.sendMessage("  §e/sl reset §7- Réinitialise le module");
            sender.sendMessage("  §e/sl reload §7- Recharge la configuration");
            sender.sendMessage("");
        }

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
                    String status = plugin.getRoleManager().isRoleEnabled(role.getName()) ? "§a✔" : "§c✖";
                    sender.sendMessage("  §8• " + role.getRarity().getColorCode() + role.getName() + " " + status);
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
        sender.sendMessage("  §7Joueurs en partie: §e" + plugin.getRoleManager().getRolePlayers().size());
        sender.sendMessage("");
        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void handleSetRole(CommandSender sender, String playerName, String roleName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + playerName);
            return;
        }

        Role role = plugin.getRoleManager().createRole(roleName);
        if (role == null) {
            sender.sendMessage("§cRôle introuvable: " + roleName);
            sender.sendMessage("§7Utilisez §e/sl roles §7pour voir les rôles disponibles.");
            return;
        }

        RolePlayer rp = plugin.getRoleManager().getRolePlayer(target.getUniqueId());
        if (rp == null) {
            rp = new RolePlayer(target);
            plugin.getRoleManager().getRolePlayers().put(target.getUniqueId(), rp);
        }

        // Nettoyer les effets de potion de l'ancien rôle avant d'assigner le nouveau
        target.getActivePotionEffects().forEach(effect -> target.removePotionEffect(effect.getType()));

        rp.setRole(role);
        rp.setRoleRevealed(true);
        role.onRoleAssigned(target);
        role.onRoleReveal(target);

        sender.sendMessage("§a§l[SL] §fRôle §e" + role.getName() + " §fattribué à §e" + target.getName() + "§f.");
        target.sendMessage("§5§l[SL] §fVotre rôle a été modifié par un administrateur.");
    }
}
