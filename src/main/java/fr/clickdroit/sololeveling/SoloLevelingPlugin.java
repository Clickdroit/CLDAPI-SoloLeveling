package fr.clickdroit.sololeveling;

import fr.clickdroit.api.API;
import fr.clickdroit.sololeveling.camp.CampManager;
import fr.clickdroit.sololeveling.command.CampCommand;
import fr.clickdroit.sololeveling.command.PowersCommand;
import fr.clickdroit.sololeveling.command.RoleCommand;
import fr.clickdroit.sololeveling.command.SoloLevelingCommand;
import fr.clickdroit.sololeveling.command.SoloLevelingTabCompleter;
import fr.clickdroit.sololeveling.listener.CombatStatsListener;
import fr.clickdroit.sololeveling.listener.PowerListener;
import fr.clickdroit.sololeveling.listener.RoleListener;
import fr.clickdroit.sololeveling.config.SoloLevelingConfigGUI;
import fr.clickdroit.sololeveling.config.RoleConfigMainGUI;
import fr.clickdroit.sololeveling.config.RoleQuantityConfigGUI;
import fr.clickdroit.sololeveling.module.SoloLevelingGameModule;
import fr.clickdroit.sololeveling.role.RoleManager;
import fr.clickdroit.sololeveling.stats.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin principal pour le mode Solo Leveling UHC.
 * Ce plugin dépend de CLDAPI et ajoute un système de rôles
 * basé sur l'univers Solo Leveling.
 */
public class SoloLevelingPlugin extends JavaPlugin {

    private static SoloLevelingPlugin instance;

    private RoleManager roleManager;
    private CampManager campManager;
    private SoloLevelingGameModule gameModule;
    private SoloLevelingConfigGUI configGUI;
    private RoleConfigMainGUI roleConfigGUI;
    private RoleQuantityConfigGUI roleQuantityConfigGUI;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;

        // Vérifier que UHCAPI est chargé
        if (getServer().getPluginManager().getPlugin("UHCAPI") == null) {
            getLogger().severe("UHCAPI n'est pas installé! Ce plugin requiert UHCAPI.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialiser les managers
        this.campManager = new CampManager(this);
        this.roleManager = new RoleManager(this);
        this.statsManager = new StatsManager(this);

        // Créer et enregistrer le module via le nouveau système GameModuleRegistry
        this.gameModule = new SoloLevelingGameModule(this);
        API.getAPI().getModuleRegistry().registerModule(this.gameModule);

        // Enregistrer les listeners
        getServer().getPluginManager().registerEvents(new RoleListener(this), this);
        getServer().getPluginManager().registerEvents(new PowerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatStatsListener(this), this);

        // Initialiser les GUIs de configuration (singletons)
        this.configGUI = new SoloLevelingConfigGUI(this);
        this.roleConfigGUI = new RoleConfigMainGUI(this);
        this.roleQuantityConfigGUI = new RoleQuantityConfigGUI(this);

        // Enregistrer les commandes
        SoloLevelingTabCompleter tabCompleter = new SoloLevelingTabCompleter(this);

        getCommand("sololeveling").setExecutor(new SoloLevelingCommand(this));
        getCommand("sololeveling").setTabCompleter(tabCompleter);
        getCommand("role").setExecutor(new RoleCommand(this));
        getCommand("role").setTabCompleter(tabCompleter);
        getCommand("powers").setExecutor(new PowersCommand(this));
        getCommand("powers").setTabCompleter(tabCompleter);
        getCommand("camp").setExecutor(new CampCommand(this));
        getCommand("camp").setTabCompleter(tabCompleter);

        getLogger().info("§5Solo Leveling UHC §fchargé avec succès!");
        getLogger().info("§7" + roleManager.getRegisteredRolesCount() + " rôles enregistrés.");
        getLogger().info("§aModule enregistré dans le GameModuleRegistry.");
    }

    @Override
    public void onDisable() {
        // Désenregistrer le module au désactivation
        if (API.getAPI() != null && API.getAPI().getModuleRegistry() != null) {
            API.getAPI().getModuleRegistry().unregisterModule("SOLOLEVELING");
        }
        getLogger().info("§5Solo Leveling UHC §fdésactivé.");
    }

    public static SoloLevelingPlugin getInstance() {
        return instance;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public CampManager getCampManager() {
        return campManager;
    }

    public SoloLevelingGameModule getGameModule() {
        return gameModule;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public API getAPI() {
        return API.getAPI();
    }

    public RoleConfigMainGUI getRoleConfigGUI() {
        return roleConfigGUI;
    }

    public SoloLevelingConfigGUI getConfigGUI() {
        return configGUI;
    }

    public RoleQuantityConfigGUI getRoleQuantityConfigGUI() {
        return roleQuantityConfigGUI;
    }
}
