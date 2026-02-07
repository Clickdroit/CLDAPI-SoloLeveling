package fr.clickdroit.sololeveling.role;

import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import fr.clickdroit.sololeveling.camp.Camp;
import fr.clickdroit.sololeveling.role.hunters.*;
import fr.clickdroit.sololeveling.role.monarchs.*;
import fr.clickdroit.sololeveling.role.neutral.*;
import fr.clickdroit.sololeveling.role.rulers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Gestionnaire des rôles.
 * Gère l'enregistrement, l'attribution et le suivi des rôles.
 */
public class RoleManager {

    private final SoloLevelingPlugin plugin;

    // Tous les rôles enregistrés
    private final Map<String, Class<? extends Role>> registeredRoles;

    // Rôles activés pour la prochaine partie
    private final Set<String> enabledRoles;

    // Quantité de chaque rôle (combien de fois le rôle peut être distribué)
    private final Map<String, Integer> roleQuantities;

    // Joueurs avec leur rôle
    private final Map<UUID, RolePlayer> rolePlayers;

    // Épisode actuel
    private int currentEpisode = 1;

    public RoleManager(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
        this.registeredRoles = new HashMap<>();
        this.enabledRoles = new HashSet<>();
        this.roleQuantities = new HashMap<>();
        this.rolePlayers = new HashMap<>();

        registerAllRoles();
        loadConfiguration();
    }

    /**
     * Charge la configuration depuis le fichier.
     */
    public void loadConfiguration() {
        // Charger les rôles activés
        List<String> enabledList = plugin.getConfig().getStringList("roles.enabled");
        if (enabledList != null && !enabledList.isEmpty()) {
            enabledRoles.clear();
            for (String role : enabledList) {
                enabledRoles.add(role.toLowerCase());
            }
        }

        // Charger les quantités
        if (plugin.getConfig().isConfigurationSection("roles.quantity")) {
            for (String roleName : plugin.getConfig().getConfigurationSection("roles.quantity").getKeys(false)) {
                int quantity = plugin.getConfig().getInt("roles.quantity." + roleName);
                roleQuantities.put(roleName.toLowerCase(), quantity);
            }
        }
    }

    /**
     * Sauvegarde la configuration dans le fichier.
     */
    public void saveConfiguration() {
        // Sauvegarder les rôles activés
        List<String> enabledList = new ArrayList<>(enabledRoles);
        plugin.getConfig().set("roles.enabled", enabledList);

        // Sauvegarder les quantités
        for (Map.Entry<String, Integer> entry : roleQuantities.entrySet()) {
            plugin.getConfig().set("roles.quantity." + entry.getKey(), entry.getValue());
        }

        plugin.saveConfig();
    }

    /**
     * Enregistre tous les rôles disponibles.
     */
    private void registerAllRoles() {
        // === CHASSEURS ===
        registerRole(SungJinWoo.class);
        registerRole(ChaHaeIn.class);
        registerRole(GoGunHee.class);
        registerRole(BaekYoonHo.class);
        registerRole(ChoiJongIn.class);
        registerRole(ThomasAndre.class);
        registerRole(LiuZhigang.class);
        registerRole(YooJinHo.class);

        // === MONARQUES ===
        registerRole(Antares.class);
        registerRole(Ashborn.class);
        registerRole(Querehsha.class);
        registerRole(Baran.class);
        registerRole(Tarnak.class);
        registerRole(Sillad.class);

        // === DIRIGEANTS ===
        registerRole(FragmentOfLight.class);
        registerRole(Guardian.class);
        registerRole(Messenger.class);

        // === NEUTRES ===
        registerRole(SystemMerchant.class);
        registerRole(Architect.class);
        registerRole(Scavenger.class);
        registerRole(Defector.class);
        registerRole(Awakened.class);
    }

    /**
     * Enregistre un rôle.
     */
    public void registerRole(Class<? extends Role> roleClass) {
        try {
            Role instance = roleClass.newInstance();
            String name = instance.getName();
            String key = name.toLowerCase();
            registeredRoles.put(key, roleClass);

            // Activer par défaut si l'annotation le permet
            RoleInfo info = roleClass.getAnnotation(RoleInfo.class);
            if (info != null && info.enabled()) {
                enabledRoles.add(key);
            }

            // Quantité par défaut basée sur l'annotation
            int defaultQuantity = info != null ? info.maxPerGame() : 1;
            roleQuantities.put(key, defaultQuantity);
        } catch (Exception e) {
            plugin.getLogger().warning("Impossible d'enregistrer le rôle: " + roleClass.getSimpleName());
            e.printStackTrace();
        }
    }

    /**
     * Obtient le nombre de rôles enregistrés.
     */
    public int getRegisteredRolesCount() {
        return registeredRoles.size();
    }

    /**
     * Obtient tous les rôles enregistrés.
     */
    public Collection<Class<? extends Role>> getRegisteredRoles() {
        return registeredRoles.values();
    }

    /**
     * Obtient les noms de tous les rôles enregistrés.
     * Méthode optimisée pour éviter de créer des instances.
     */
    public Set<String> getRegisteredRoleNames() {
        return new HashSet<>(registeredRoles.keySet());
    }

    /**
     * Active ou désactive un rôle.
     */
    public void setRoleEnabled(String roleName, boolean enabled) {
        String key = roleName.toLowerCase();
        if (enabled) {
            enabledRoles.add(key);
        } else {
            enabledRoles.remove(key);
        }
        saveConfiguration();
    }

    /**
     * Vérifie si un rôle est activé.
     */
    public boolean isRoleEnabled(String roleName) {
        return enabledRoles.contains(roleName.toLowerCase());
    }

    /**
     * Obtient la quantité configurée pour un rôle.
     */
    public int getRoleQuantity(String roleName) {
        return roleQuantities.getOrDefault(roleName.toLowerCase(), 1);
    }

    /**
     * Définit la quantité pour un rôle.
     */
    public void setRoleQuantity(String roleName, int quantity) {
        roleQuantities.put(roleName.toLowerCase(), Math.max(0, quantity));
        saveConfiguration();
    }

    /**
     * Crée une instance d'un rôle.
     */
    public Role createRole(String roleName) {
        Class<? extends Role> roleClass = registeredRoles.get(roleName.toLowerCase());
        if (roleClass == null)
            return null;

        try {
            return roleClass.newInstance();
        } catch (Exception e) {
            plugin.getLogger().warning("Impossible de créer le rôle: " + roleName);
            return null;
        }
    }

    /**
     * Attribue les rôles à tous les joueurs en jeu.
     */
    public void distributeRoles(List<UUID> players) {
        // Collecter les rôles activés
        List<Role> availableRoles = new ArrayList<>();

        for (String roleName : enabledRoles) {
            Role role = createRole(roleName);
            if (role != null) {
                // Utiliser la quantité configurée
                int quantity = getRoleQuantity(roleName);

                // Ajouter le rôle le nombre de fois configuré
                for (int i = 0; i < quantity && availableRoles.size() < players.size(); i++) {
                    availableRoles.add(createRole(roleName));
                }
            }
        }

        // Mélanger les rôles
        Collections.shuffle(availableRoles);

        // Mélanger les joueurs
        List<UUID> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        // Attribuer les rôles
        for (int i = 0; i < shuffledPlayers.size(); i++) {
            UUID uuid = shuffledPlayers.get(i);
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            // Créer le RolePlayer
            RolePlayer rolePlayer = new RolePlayer(player);

            // Attribuer un rôle si disponible
            if (i < availableRoles.size()) {
                Role role = availableRoles.get(i);
                rolePlayer.setRole(role);
                role.onRoleAssigned(player);
            }

            rolePlayers.put(uuid, rolePlayer);
        }

        plugin.getLogger().info("Rôles distribués à " + rolePlayers.size() + " joueurs.");
    }

    /**
     * Révèle les rôles à tous les joueurs.
     */
    public void revealAllRoles() {
        for (RolePlayer rp : rolePlayers.values()) {
            if (rp.getRole() != null && !rp.isRoleRevealed()) {
                Player player = Bukkit.getPlayer(rp.getUuid());
                if (player != null) {
                    rp.getRole().onRoleReveal(player);
                    rp.setRoleRevealed(true);
                }
            }
        }
    }

    /**
     * Obtient le RolePlayer d'un joueur.
     */
    public RolePlayer getRolePlayer(UUID uuid) {
        return rolePlayers.get(uuid);
    }

    /**
     * Obtient tous les RolePlayers.
     */
    public Map<UUID, RolePlayer> getRolePlayers() {
        return rolePlayers;
    }

    /**
     * Réinitialise tous les rôles.
     */
    public void reset() {
        rolePlayers.clear();
        currentEpisode = 1;
    }

    /**
     * Obtient l'épisode actuel.
     */
    public int getCurrentEpisode() {
        return currentEpisode;
    }

    /**
     * Obtient tous les rôles d'un camp.
     */
    public List<Role> getRolesByCamp(Camp camp) {
        List<Role> roles = new ArrayList<>();
        for (Class<? extends Role> roleClass : registeredRoles.values()) {
            try {
                Role role = roleClass.newInstance();
                if (role.getCamp() == camp) {
                    roles.add(role);
                }
            } catch (Exception ignored) {
            }
        }
        return roles;
    }

    /**
     * Notifie tous les rôles d'un événement de temps.
     */
    public void onTick(int gameTime) {
        for (RolePlayer rp : rolePlayers.values()) {
            if (rp.isAlive() && rp.getRole() != null) {
                rp.getRole().onTick(gameTime);
            }
        }
    }

    /**
     * Notifie tous les rôles du passage à la nuit.
     */
    public void onNight() {
        for (RolePlayer rp : rolePlayers.values()) {
            if (rp.isAlive() && rp.getRole() != null) {
                rp.getRole().onNight();
            }
        }
    }

    /**
     * Notifie tous les rôles du passage au jour.
     */
    public void onDay() {
        for (RolePlayer rp : rolePlayers.values()) {
            if (rp.isAlive() && rp.getRole() != null) {
                rp.getRole().onDay();
            }
        }
    }

    /**
     * Notifie tous les rôles d'un changement d'épisode.
     */
    public void onEpisode(int episode) {
        this.currentEpisode = episode;
        for (RolePlayer rp : rolePlayers.values()) {
            if (rp.isAlive() && rp.getRole() != null) {
                rp.getRole().onEpisode(episode);
            }
        }
    }
}
