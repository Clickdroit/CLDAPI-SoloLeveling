package fr.clickdroit.sololeveling.command;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.role.RolePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande /cc (camp chat) pour envoyer un message uniquement
 * aux membres de son camp (alliés).
 */
public class CampChatCommand implements CommandExecutor {

    private final SoloLevelingPlugin plugin;

    public CampChatCommand(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cUsage: /cc <message>");
            return true;
        }

        RolePlayer rp = plugin.getRoleManager().getRolePlayer(player.getUniqueId());

        if (rp == null || rp.getRole() == null) {
            player.sendMessage("§cVous n'avez pas de rôle assigné!");
            return true;
        }

        if (!rp.isRoleRevealed()) {
            player.sendMessage("§cVotre rôle n'a pas encore été révélé!");
            return true;
        }

        Camp camp = rp.getRole().getCamp();
        String message = String.join(" ", args);
        String formattedMessage = camp.getColorCode() + "§l[" + camp.getDisplayName() + "] §r"
                + camp.getColorCode() + player.getName() + " §8➤ §f" + message;

        // Envoyer le message aux alliés en vie et aux spectateurs/admins
        int recipients = 0;
        for (RolePlayer ally : plugin.getRoleManager().getRolePlayers().values()) {
            if (ally.getRole() == null) continue;
            if (!camp.isAlliedWith(ally.getRole().getCamp())) continue;
            if (ally.getUuid().equals(player.getUniqueId())) continue; // ne pas compter l'expéditeur

            Player allyPlayer = Bukkit.getPlayer(ally.getUuid());
            if (allyPlayer != null && allyPlayer.isOnline()) {
                allyPlayer.sendMessage(formattedMessage);
                recipients++;
            }
        }

        // Envoyer le message à l'expéditeur en dernier
        player.sendMessage(formattedMessage);

        // Notifier l'expéditeur s'il n'y a aucun autre allié en ligne
        if (recipients == 0) {
            player.sendMessage("§7§o(Aucun allié en ligne pour recevoir votre message.)");
        }

        // Permettre aux admins de lire tous les messages de camp
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("sololeveling.admin")) {
                RolePlayer adminRp = plugin.getRoleManager().getRolePlayer(online.getUniqueId());
                // Ne pas envoyer deux fois aux admins qui sont dans le même camp
                if (adminRp != null && adminRp.getRole() != null
                        && camp.isAlliedWith(adminRp.getRole().getCamp())) {
                    continue;
                }
                online.sendMessage("§8[SPY] " + formattedMessage);
            }
        }

        return true;
    }
}
