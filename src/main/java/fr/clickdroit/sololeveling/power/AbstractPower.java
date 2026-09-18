package fr.clickdroit.sololeveling.power;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Classe abstraite de base pour les pouvoirs.
 * Gère les cooldowns et les utilisations.
 */
public abstract class AbstractPower implements Power {

    protected final SoloLevelingPlugin plugin;
    protected final String name;
    protected final String description;
    protected final PowerType type;
    protected final int cooldownSeconds;
    protected final int maxUses;

    // Cooldowns par joueur (timestamp de fin)
    private final Map<UUID, Long> cooldowns;

    // Utilisations restantes par joueur
    private final Map<UUID, Integer> usesLeft;

    public AbstractPower(String name, String description, PowerType type,
            int cooldownSeconds, int maxUses) {
        this.plugin = SoloLevelingPlugin.getInstance();
        this.name = name;
        this.description = description;
        this.type = type;
        this.cooldownSeconds = cooldownSeconds;
        this.maxUses = maxUses;
        this.cooldowns = new HashMap<>();
        this.usesLeft = new HashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public PowerType getType() {
        return type;
    }

    @Override
    public int getCooldown() {
        return cooldownSeconds;
    }

    @Override
    public int getMaxUses() {
        return maxUses;
    }

    @Override
    public boolean canUse(Player player) {
        UUID uuid = player.getUniqueId();

        // Vérifier le cooldown
        if (isOnCooldown(uuid)) {
            onCooldown(player, getRemainingCooldown(uuid));
            return false;
        }

        // Vérifier les utilisations
        if (maxUses > 0) {
            int remaining = usesLeft.getOrDefault(uuid, maxUses);
            if (remaining <= 0) {
                player.sendMessage("§cVous avez épuisé toutes les utilisations de ce pouvoir!");
                return false;
            }
        }

        // Vérifications spécifiques au pouvoir
        return canExecute(player);
    }

    /**
     * Vérifie les conditions spécifiques pour l'utilisation du pouvoir.
     * À surcharger dans les classes enfants.
     */
    protected boolean canExecute(Player player) {
        return true;
    }

    @Override
    public boolean execute(Player player) {
        if (!canUse(player)) {
            return false;
        }

        // Exécuter le pouvoir
        boolean success = onExecute(player);

        if (success) {
            UUID uuid = player.getUniqueId();

            // Appliquer le cooldown
            if (cooldownSeconds > 0) {
                cooldowns.put(uuid, System.currentTimeMillis() + (cooldownSeconds * 1000L));
            }

            // Décrémenter les utilisations
            if (maxUses > 0) {
                int remaining = usesLeft.getOrDefault(uuid, maxUses);
                usesLeft.put(uuid, remaining - 1);
            }

            // Notifier le joueur
            player.sendMessage("§aPouvoir §e" + name + " §aactivé!");
        }

        return success;
    }

    /**
     * Exécute l'effet du pouvoir.
     * À implémenter dans les classes enfants.
     */
    protected abstract boolean onExecute(Player player);

    @Override
    public void onCooldown(Player player, int remainingSeconds) {
        player.sendMessage("§cCe pouvoir est en cooldown! §7(" + remainingSeconds + "s)");
    }

    /**
     * Vérifie si le pouvoir est en cooldown pour un joueur.
     */
    public boolean isOnCooldown(UUID uuid) {
        Long endTime = cooldowns.get(uuid);
        if (endTime == null)
            return false;
        return System.currentTimeMillis() < endTime;
    }

    @Override
    public int getRemainingCooldown(Player player) {
        return getRemainingCooldown(player.getUniqueId());
    }

    /**
     * Obtient le temps restant de cooldown.
     */
    public int getRemainingCooldown(UUID uuid) {
        Long endTime = cooldowns.get(uuid);
        if (endTime == null)
            return 0;
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }

    /**
     * Obtient les utilisations restantes pour un joueur.
     */
    public int getUsesRemaining(UUID uuid) {
        if (maxUses <= 0)
            return -1; // Illimité
        return usesLeft.getOrDefault(uuid, maxUses);
    }

    /**
     * Réinitialise le cooldown d'un joueur.
     */
    public void resetCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }

    /**
     * Réinitialise les utilisations d'un joueur.
     */
    public void resetUses(UUID uuid) {
        usesLeft.put(uuid, maxUses);
    }

    /**
     * Réinitialise tout pour un joueur.
     */
    public void reset(UUID uuid) {
        resetCooldown(uuid);
        resetUses(uuid);
    }

    // === Implémentations par défaut des événements ===

    @Override
    public void onRoleAssigned(Player player) {
    }

    @Override
    public void onRoleReveal(Player player) {
    }

    @Override
    public void onDeath(Player player, Player killer) {
    }

    @Override
    public void onKill(Player player, Player victim) {
    }

    @Override
    public void onNight() {
    }

    @Override
    public void onDay() {
    }

    @Override
    public void onEpisode(int episode) {
    }

    @Override
    public void onTick(int gameTime) {
    }

    @Override
    public void onGameStart() {
    }

    @Override
    public void onGameEnd() {
    }
}
