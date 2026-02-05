package fr.clickdroit.sololeveling.power;

import org.bukkit.entity.Player;

/**
 * Interface représentant un pouvoir de rôle.
 */
public interface Power {

    /**
     * @return Le nom du pouvoir
     */
    String getName();

    /**
     * @return La description du pouvoir
     */
    String getDescription();

    /**
     * @return Le type de pouvoir (actif, passif, ultime)
     */
    PowerType getType();

    /**
     * @return Le cooldown en secondes
     */
    int getCooldown();

    /**
     * @return Le nombre d'utilisations maximum (-1 = illimité)
     */
    int getMaxUses();

    /**
     * Vérifie si le joueur peut utiliser ce pouvoir.
     */
    boolean canUse(Player player);

    /**
     * Exécute le pouvoir.
     * 
     * @return true si le pouvoir a été utilisé avec succès
     */
    boolean execute(Player player);

    /**
     * Retourne le temps de cooldown restant pour ce joueur.
     * 
     * @param player Le joueur
     * @return Le temps restant en secondes, 0 si pas de cooldown
     */
    int getRemainingCooldown(Player player);

    /**
     * Appelé lorsque le pouvoir est sur cooldown et le joueur essaie de l'utiliser.
     */
    void onCooldown(Player player, int remainingSeconds);
}
