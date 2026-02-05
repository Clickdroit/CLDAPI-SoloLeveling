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
 * Commande /camp pour afficher les informations sur le camp du joueur.
 * Permet également aux admins de voir les statistiques des camps.
 */
public class CampCommand implements CommandExecutor {

    private final SoloLevelingPlugin plugin;

    public CampCommand(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs!");
            return true;
        }

        Player player = (Player) sender;

        // Sous-commande admin: /camp stats
        if (args.length > 0 && args[0].equalsIgnoreCase("stats")) {
            if (!player.hasPermission("sololeveling.admin")) {
                player.sendMessage("§cVous n'avez pas la permission!");
                return true;
            }
            sendCampStats(player);
            return true;
        }

        // Commande normale: afficher le camp du joueur
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
        Camp camp = role.getCamp();

        sendCampInfo(player, camp);
        return true;
    }

    /**
     * Affiche les informations détaillées sur un camp.
     */
    private void sendCampInfo(Player player, Camp camp) {
        player.sendMessage("");
        player.sendMessage("§8§l§m                                                §r");
        player.sendMessage("");
        player.sendMessage("  " + camp.getColorCode() + "§l" + camp.getDisplayName().toUpperCase());
        player.sendMessage("");
        player.sendMessage("  §7" + camp.getObjective());
        player.sendMessage("");

        // Afficher les alliés vivants du même camp
        List<Player> allies = plugin.getCampManager().getAlivePlayers(camp);
        if (allies.size() > 1) { // Plus d'un = au moins un autre allié
            player.sendMessage("  §6Alliés en vie: §f" + (allies.size() - 1));
            for (Player ally : allies) {
                if (!ally.getUniqueId().equals(player.getUniqueId())) {
                    RolePlayer allyRp = plugin.getRoleManager().getRolePlayer(ally.getUniqueId());
                    if (allyRp != null && allyRp.getRole() != null && allyRp.isRoleRevealed()) {
                        player.sendMessage("    §8• §7" + ally.getName() + " §8(§f" + allyRp.getRole().getName() + "§8)");
                    } else {
                        player.sendMessage("    §8• §7" + ally.getName());
                    }
                }
            }
        } else {
            player.sendMessage("  §7Vous êtes seul dans votre camp.");
        }

        player.sendMessage("");

        // Afficher les relations avec les autres camps
        player.sendMessage("  §6Relations:");
        for (Camp otherCamp : Camp.values()) {
            if (otherCamp != camp) {
                String relation;
                if (camp.isAlliedWith(otherCamp)) {
                    relation = "§a⬤ Allié";
                } else if (camp.isEnemyOf(otherCamp)) {
                    relation = "§c⬤ Ennemi";
                } else {
                    relation = "§7⬤ Neutre";
                }
                player.sendMessage("    " + otherCamp.getColorCode() + otherCamp.getDisplayName() + " §8- " + relation);
            }
        }

        player.sendMessage("");
        player.sendMessage("§8§l§m                                                §r");
        player.sendMessage("");
    }

    /**
     * Affiche les statistiques des camps (admin).
     */
    private void sendCampStats(Player player) {
        player.sendMessage("");
        player.sendMessage("§5§l━━━━━ STATISTIQUES DES CAMPS ━━━━━");
        player.sendMessage("");

        var aliveCounts = plugin.getCampManager().getAliveCountByCamp();
        int totalAlive = 0;

        for (Camp camp : Camp.values()) {
            int alive = aliveCounts.get(camp);
            totalAlive += alive;

            List<Player> players = plugin.getCampManager().getAlivePlayers(camp);

            player.sendMessage(camp.getColorCode() + "§l" + camp.getDisplayName() + " §7- §f" + alive + " en vie");

            for (Player p : players) {
                RolePlayer rp = plugin.getRoleManager().getRolePlayer(p.getUniqueId());
                String roleName = rp != null && rp.getRole() != null ? rp.getRole().getName() : "???";
                String health = String.format("%.1f", p.getHealth());
                player.sendMessage("  §8• §7" + p.getName() + " §8(§f" + roleName + "§8) §c❤ " + health);
            }

            player.sendMessage("");
        }

        player.sendMessage("§7Total en vie: §f" + totalAlive);
        player.sendMessage("");
        player.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
