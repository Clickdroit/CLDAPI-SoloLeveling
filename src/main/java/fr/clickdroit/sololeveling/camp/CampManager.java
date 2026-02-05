package fr.clickdroit.sololeveling.camp;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.role.RolePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestionnaire des camps.
 * Gère les conditions de victoire et le suivi des camps.
 */
public class CampManager {

    private final SoloLevelingPlugin plugin;

    public CampManager(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Obtient tous les joueurs vivants d'un camp donné.
     */
    public List<Player> getAlivePlayers(Camp camp) {
        return plugin.getRoleManager().getRolePlayers().values().stream()
                .filter(RolePlayer::isAlive)
                .filter(rp -> rp.getRole() != null && rp.getRole().getCamp() == camp)
                .map(rp -> Bukkit.getPlayer(rp.getUuid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Compte les joueurs vivants par camp.
     */
    public Map<Camp, Integer> getAliveCountByCamp() {
        Map<Camp, Integer> counts = new EnumMap<>(Camp.class);
        for (Camp camp : Camp.values()) {
            counts.put(camp, 0);
        }

        for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
            if (rp.isAlive() && rp.getRole() != null) {
                Camp camp = rp.getRole().getCamp();
                counts.put(camp, counts.get(camp) + 1);
            }
        }

        return counts;
    }

    /**
     * Vérifie si un camp a gagné.
     * 
     * @return Le camp gagnant ou null si la partie continue
     */
    public Camp checkWinCondition() {
        Map<Camp, Integer> aliveCounts = getAliveCountByCamp();

        int hunters = aliveCounts.get(Camp.HUNTERS);
        int rulers = aliveCounts.get(Camp.RULERS);
        int monarchs = aliveCounts.get(Camp.MONARCHS);
        int neutrals = aliveCounts.get(Camp.NEUTRAL);

        int allianceHunters = hunters + rulers; // Chasseurs + Dirigeants

        // Victoire des Monarques: tous les Chasseurs et Dirigeants sont morts
        if (allianceHunters == 0 && monarchs > 0) {
            return Camp.MONARCHS;
        }

        // Victoire des Chasseurs/Dirigeants: tous les Monarques sont morts
        if (monarchs == 0 && allianceHunters > 0) {
            return Camp.HUNTERS; // Les Dirigeants gagnent avec les Chasseurs
        }

        // Cas spécial: il ne reste qu'un seul joueur
        int totalAlive = hunters + rulers + monarchs + neutrals;
        if (totalAlive == 1) {
            // Le dernier survivant gagne
            for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
                if (rp.isAlive() && rp.getRole() != null) {
                    return rp.getRole().getCamp();
                }
            }
        }

        // Pas encore de gagnant
        return null;
    }

    /**
     * Annonce la victoire d'un camp.
     */
    public void announceVictory(Camp winningCamp) {
        String message = "\n§8§l§m                                                §r\n";
        message += "\n";
        message += "  " + winningCamp.getColorCode() + "§l" + winningCamp.getDisplayName().toUpperCase()
                + " §fONT GAGNÉ!\n";
        message += "\n";
        message += "§8§l§m                                                §r\n";

        Bukkit.broadcastMessage(message);

        // Liste des gagnants
        List<Player> winners = getWinners(winningCamp);
        if (!winners.isEmpty()) {
            StringBuilder winnersMsg = new StringBuilder("§7Gagnants: ");
            for (int i = 0; i < winners.size(); i++) {
                Player p = winners.get(i);
                RolePlayer rp = plugin.getRoleManager().getRolePlayer(p.getUniqueId());
                String roleName = rp != null && rp.getRole() != null ? rp.getRole().getName() : "Inconnu";

                winnersMsg.append(winningCamp.getColorCode())
                        .append(p.getName())
                        .append(" §8(§7")
                        .append(roleName)
                        .append("§8)");

                if (i < winners.size() - 1) {
                    winnersMsg.append("§7, ");
                }
            }
            Bukkit.broadcastMessage(winnersMsg.toString());
        }
    }

    /**
     * Obtient la liste des gagnants basée sur le camp victorieux.
     */
    private List<Player> getWinners(Camp winningCamp) {
        List<Player> winners = new ArrayList<>();

        for (RolePlayer rp : plugin.getRoleManager().getRolePlayers().values()) {
            if (rp.getRole() == null)
                continue;

            Camp playerCamp = rp.getRole().getCamp();

            // Les joueurs du camp gagnant
            if (playerCamp == winningCamp) {
                Player p = Bukkit.getPlayer(rp.getUuid());
                if (p != null)
                    winners.add(p);
            }
            // Les Dirigeants gagnent avec les Chasseurs
            else if (winningCamp == Camp.HUNTERS && playerCamp == Camp.RULERS) {
                Player p = Bukkit.getPlayer(rp.getUuid());
                if (p != null)
                    winners.add(p);
            }
            // Les Chasseurs gagnent même si annoncé comme Dirigeants
            else if (winningCamp == Camp.RULERS && playerCamp == Camp.HUNTERS) {
                Player p = Bukkit.getPlayer(rp.getUuid());
                if (p != null)
                    winners.add(p);
            }
        }

        return winners;
    }

    /**
     * Vérifie si deux joueurs sont dans le même camp ou alliés.
     */
    public boolean areAllies(Player player1, Player player2) {
        RolePlayer rp1 = plugin.getRoleManager().getRolePlayer(player1.getUniqueId());
        RolePlayer rp2 = plugin.getRoleManager().getRolePlayer(player2.getUniqueId());

        if (rp1 == null || rp2 == null)
            return false;
        if (rp1.getRole() == null || rp2.getRole() == null)
            return false;

        return rp1.getRole().getCamp().isAlliedWith(rp2.getRole().getCamp());
    }

    /**
     * Vérifie si deux joueurs sont ennemis.
     */
    public boolean areEnemies(Player player1, Player player2) {
        RolePlayer rp1 = plugin.getRoleManager().getRolePlayer(player1.getUniqueId());
        RolePlayer rp2 = plugin.getRoleManager().getRolePlayer(player2.getUniqueId());

        if (rp1 == null || rp2 == null)
            return false;
        if (rp1.getRole() == null || rp2.getRole() == null)
            return false;

        return rp1.getRole().getCamp().isEnemyOf(rp2.getRole().getCamp());
    }
}
