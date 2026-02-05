package fr.clickdroit.sololeveling.command;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.Power;
import fr.clickdroit.sololeveling.role.RolePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TabCompleter pour toutes les commandes Solo Leveling.
 * Fournit l'auto-complétion intelligente des commandes.
 */
public class SoloLevelingTabCompleter implements TabCompleter {

    private final SoloLevelingPlugin plugin;

    public SoloLevelingTabCompleter(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "sololeveling":
                completions = completeSoloLevelingCommand(sender, args);
                break;
            case "role":
                completions = completeRoleCommand(sender, args);
                break;
            case "powers":
                completions = completePowersCommand(sender, args);
                break;
            case "camp":
                completions = completeCampCommand(sender, args);
                break;
        }

        // Filtrer par ce que l'utilisateur a déjà tapé
        String lastArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Auto-complétion pour /sololeveling
     */
    private List<String> completeSoloLevelingCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList(
                "help", "roles", "camps", "info"
            ));
            
            // Commandes admin uniquement
            if (sender.hasPermission("sololeveling.admin")) {
                subCommands.add("reload");
                subCommands.add("config");
                subCommands.add("setrole");
                subCommands.add("reset");
            }
            
            return subCommands;
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "setrole":
                    // Liste des joueurs en ligne
                    return plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                case "roles":
                    // Liste des camps
                    return Arrays.stream(Camp.values())
                            .map(Camp::getDisplayName)
                            .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setrole")) {
                // Utiliser les noms des rôles déjà enregistrés (sans créer de nouvelles instances)
                return new ArrayList<>(plugin.getRoleManager().getRegisteredRoleNames());
            }
        }

        return new ArrayList<>();
    }

    /**
     * Auto-complétion pour /role
     */
    private List<String> completeRoleCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("info");
            
            if (sender.hasPermission("sololeveling.admin")) {
                subCommands.add("list");
                subCommands.add("reveal");
            }
            
            return subCommands;
        }
        
        return new ArrayList<>();
    }

    /**
     * Auto-complétion pour /powers
     */
    private List<String> completePowersCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;
        RolePlayer rp = plugin.getRoleManager().getRolePlayer(player.getUniqueId());

        if (rp == null || rp.getRole() == null) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // Liste des pouvoirs du joueur
            List<String> powerNames = new ArrayList<>();
            for (Power power : rp.getRole().getPowers()) {
                powerNames.add(power.getName().replace(" ", "_"));
            }
            return powerNames;
        }

        return new ArrayList<>();
    }

    /**
     * Auto-complétion pour /camp
     */
    private List<String> completeCampCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("info");
            
            if (sender.hasPermission("sololeveling.admin")) {
                subCommands.add("stats");
                subCommands.add("list");
            }
            
            return subCommands;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            return Arrays.stream(Camp.values())
                    .map(Camp::getDisplayName)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
