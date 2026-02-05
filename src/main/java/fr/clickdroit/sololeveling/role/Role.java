package fr.clickdroit.sololeveling.role;

import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.power.Power;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Interface définissant un rôle dans Solo Leveling UHC.
 * Chaque rôle doit implémenter cette interface.
 */
public interface Role {

    /**
     * @return Le nom du rôle
     */
    String getName();

    /**
     * @return La description du rôle
     */
    String getDescription();

    /**
     * @return Le camp auquel appartient ce rôle
     */
    Camp getCamp();

    /**
     * @return La rareté du rôle
     */
    RoleRarity getRarity();

    /**
     * @return La liste des pouvoirs du rôle
     */
    List<Power> getPowers();

    /**
     * @return L'icône du rôle pour les GUIs
     */
    ItemStack getIcon();

    /**
     * @return Les lignes de lore pour l'affichage
     */
    String[] getLore();

    // === Événements du cycle de vie ===

    /**
     * Appelé quand le rôle est attribué au joueur.
     */
    void onRoleAssigned(Player player);

    /**
     * Appelé quand le rôle est révélé au joueur.
     */
    void onRoleReveal(Player player);

    /**
     * Appelé quand le joueur meurt.
     */
    void onDeath(Player player, Player killer);

    /**
     * Appelé quand le joueur tue quelqu'un.
     */
    void onKill(Player player, Player victim);

    // === Événements de temps ===

    /**
     * Appelé à chaque passage à la nuit.
     */
    void onNight();

    /**
     * Appelé à chaque passage au jour.
     */
    void onDay();

    /**
     * Appelé à chaque épisode.
     */
    void onEpisode(int episode);

    /**
     * Appelé à chaque seconde de jeu.
     */
    void onTick(int gameTime);

    // === Événements de jeu ===

    /**
     * Appelé au début de la partie.
     */
    void onGameStart();

    /**
     * Appelé à la fin de la partie.
     */
    void onGameEnd();
}
