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
import fr.clickdroit.sololeveling.module.SoloLevelingModule;
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
    private StatsManager statsManager;
    private SoloLevelingModule module;

    @Override
    public void onEnable() {
        instance = this;

        // Vérifier que CLDAPI est chargé
        if (getServer().getPluginManager().getPlugin("CLDAPI") == null) {
            getLogger().severe("CLDAPI n'est pas installé! Ce plugin requiert CLDAPI.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialiser les managers
        this.campManager = new CampManager(this);
        this.roleManager = new RoleManager(this);
        this.statsManager = new StatsManager(this);

        // Créer et enregistrer le module
        this.module = new SoloLevelingModule(this);

        // Enregistrer le module auprès de l'API
        // L'API récupère automatiquement le module via setModules() et getModuleType()
        API.getAPI().setModules(this.module);
        API.getAPI().getGameManager().getModuleManager().setCurrentModule(this.module.getModuleType());

        // Enregistrer les listeners
        getServer().getPluginManager().registerEvents(new RoleListener(this), this);
        getServer().getPluginManager().registerEvents(new PowerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatStatsListener(this), this);

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
    }

    @Override
    public void onDisable() {
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

    public SoloLevelingModule getModule() {
        return module;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public API getAPI() {
        return API.getAPI();
    }
}
