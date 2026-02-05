package fr.clickdroit.sololeveling.command;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.power.AbstractPower;
import fr.clickdroit.sololeveling.power.Power;
import fr.clickdroit.sololeveling.role.Role;
import fr.clickdroit.sololeveling.role.RolePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande /powers pour afficher et utiliser les pouvoirs.
 */
public class PowersCommand implements CommandExecutor {

    private final SoloLevelingPlugin plugin;

    public PowersCommand(SoloLevelingPlugin plugin) {
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

        // Si un argument est donné, essayer d'utiliser le pouvoir
        if (args.length > 0) {
            String powerName = String.join(" ", args);
            for (Power power : role.getPowers()) {
                if (power.getName().equalsIgnoreCase(powerName)) {
                    power.execute(player);
                    return true;
                }
            }
            player.sendMessage("§cPouvoir introuvable: " + powerName);
            return true;
        }

        // Afficher la liste des pouvoirs
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━ VOS POUVOIRS ━━━━━━━━━");
        player.sendMessage("");

        if (role.getPowers().isEmpty()) {
            player.sendMessage("  §7Vous n'avez aucun pouvoir.");
        } else {
            int index = 1;
            for (Power power : role.getPowers()) {
                player.sendMessage("  §e" + index + ". " + power.getName() +
                        " §7(" + power.getType().getColoredName() + "§7)");
                player.sendMessage("     §7" + power.getDescription());

                // Afficher les infos de cooldown
                if (power instanceof AbstractPower) {
                    AbstractPower ap = (AbstractPower) power;
                    int remaining = ap.getRemainingCooldown(player.getUniqueId());
                    if (remaining > 0) {
                        player.sendMessage("     §c⏱ Cooldown: " + remaining + "s");
                    } else {
                        player.sendMessage("     §a✔ Prêt à l'emploi");
                    }

                    int uses = ap.getUsesRemaining(player.getUniqueId());
                    if (uses > 0) {
                        player.sendMessage("     §7Utilisations: " + uses + "/" + ap.getMaxUses());
                    } else if (uses == 0) {
                        player.sendMessage("     §cÉpuisé");
                    }
                }

                player.sendMessage("");
                index++;
            }
        }

        player.sendMessage("§7Utilisez §e/powers <nom>§7 pour activer un pouvoir.");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return true;
    }
}
