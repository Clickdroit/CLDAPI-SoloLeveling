package fr.clickdroit.sololeveling.scoreboard;

import fr.clickdroit.api.API;
import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire du scoreboard pour Solo Leveling.
 * Gère la création, mise à jour et suppression des scoreboards.
 */
public class SoloLevelingScoreboardManager implements Listener {

    private final SoloLevelingPlugin plugin;
    private final API api;
    private final Netherboard netherboard;
    private final SoloLevelingScoreboardContents contents;
    private final Map<UUID, BPlayerBoard> playerBoards;
    private BukkitTask updateTask;
    private boolean enabled = true;

    public SoloLevelingScoreboardManager(SoloLevelingPlugin plugin, API api) {
        this.plugin = plugin;
        this.api = api;
        this.netherboard = Netherboard.instance();
        this.contents = new SoloLevelingScoreboardContents(api, plugin);
        this.playerBoards = new HashMap<>();

        // Enregistrer les événements
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Démarrer la mise à jour automatique
        startUpdateTask();
    }

    /**
     * Démarre la tâche de mise à jour périodique
     */
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled)
                    return;
                updateAllBoards();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Mise à jour toutes les secondes
    }

    /**
     * Met à jour tous les scoreboards
     */
    public void updateAllBoards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateBoard(player);
        }
    }

    /**
     * Met à jour le scoreboard d'un joueur
     */
    public void updateBoard(Player player) {
        if (!enabled || player == null || !player.isOnline())
            return;

        BPlayerBoard board = getOrCreateBoard(player);
        if (board == null)
            return;

        try {
            contents.reloadData(player.getUniqueId());
            contents.setLines(board, player.getUniqueId(), "");
        } catch (Exception e) {
            plugin.getLogger().warning(
                    "Erreur lors de la mise à jour du scoreboard pour " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Récupère ou crée le scoreboard d'un joueur
     */
    private BPlayerBoard getOrCreateBoard(Player player) {
        UUID uuid = player.getUniqueId();

        if (playerBoards.containsKey(uuid)) {
            return playerBoards.get(uuid);
        }

        // Créer un nouveau board
        BPlayerBoard board = netherboard.createBoard(player, null, "§5§l⚔ §dSOLO LEVELING");
        playerBoards.put(uuid, board);
        return board;
    }

    /**
     * Supprime le scoreboard d'un joueur
     */
    public void removeBoard(Player player) {
        UUID uuid = player.getUniqueId();
        BPlayerBoard board = playerBoards.remove(uuid);
        if (board != null) {
            board.delete();
        }
    }

    /**
     * Active ou désactive le scoreboard
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            // Supprimer tous les boards
            for (BPlayerBoard board : playerBoards.values()) {
                board.delete();
            }
            playerBoards.clear();
        }
    }

    /**
     * Arrête le gestionnaire
     */
    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        for (BPlayerBoard board : playerBoards.values()) {
            board.delete();
        }
        playerBoards.clear();
    }

    // === ÉVÉNEMENTS ===

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!enabled)
            return;

        // Petit délai pour s'assurer que le joueur est bien connecté
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateBoard(event.getPlayer());
        }, 5L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeBoard(event.getPlayer());
    }

    // === GETTERS ===

    public boolean isEnabled() {
        return enabled;
    }

    public SoloLevelingScoreboardContents getContents() {
        return contents;
    }
}
