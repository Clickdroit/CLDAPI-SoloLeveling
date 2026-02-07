package fr.clickdroit.sololeveling.module;

import fr.clickdroit.api.API;
import fr.clickdroit.api.module.GameModule;
import fr.clickdroit.sololeveling.SoloLevelingPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Implémentation de l'interface GameModule pour le mode Solo Leveling.
 * Délègue les appels au SoloLevelingModule existant.
 * 
 * @author Clickdroit
 * @version 1.0
 */
public class SoloLevelingGameModule implements GameModule {

    private final SoloLevelingPlugin plugin;
    private final SoloLevelingModule module;

    public SoloLevelingGameModule(SoloLevelingPlugin plugin) {
        this.plugin = plugin;
        this.module = new SoloLevelingModule(plugin);
    }

    @Override
    public String getId() {
        return "SOLOLEVELING";
    }

    @Override
    public String getDisplayName() {
        return "Solo Leveling";
    }

    @Override
    public String getColor() {
        return "§5";
    }

    @Override
    public Material getIconMaterial() {
        return Material.EYE_OF_ENDER;
    }

    @Override
    public boolean hasRoles() {
        return true;
    }

    @Override
    public boolean hasTeams() {
        return false;
    }

    @Override
    public boolean shouldDeleteSpawn() {
        return true;
    }

    @Override
    public JavaPlugin getOwnerPlugin() {
        return plugin;
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                "§5⚔ Mode Solo Leveling UHC",
                "§7Basé sur l'univers Solo Leveling",
                "",
                "§7Chasseurs, Monarques et Dirigeants",
                "§7s'affrontent pour la victoire!"
        };
    }

    @Override
    public void onLoad() {
        module.onLoad();
    }

    @Override
    public void onEnable(API api) {
        module.init();
    }

    @Override
    public void onDisable(API api) {
        // Cleanup si nécessaire
    }

    @Override
    public void onGameStart(API api) {
        module.onStart(api);
    }

    @Override
    public void onPlayerDeath(Player player, Player killer) {
        module.onPlayerDeath(player, killer);
    }

    @Override
    public void onPlayerDeathByDisconnect(UUID uuid) {
        module.onPlayerDieByDisconnect(uuid);
    }

    @Override
    public void onClockUpdate(int gameTime) {
        module.onClockUpdate(gameTime);
    }

    @Override
    public void onDay(boolean sendMessage) {
        module.onDay(sendMessage);
    }

    @Override
    public void onNight(boolean sendMessage) {
        module.onNight(sendMessage);
    }

    @Override
    public void onEpisodeSwitch() {
        module.onEpisodeSwitch();
    }

    @Override
    public void onPlayerReconnect(Player player) {
        module.onPlayerReconnect(player);
    }

    @Override
    public void onPlayerDisconnect(Player player) {
        module.onPlayerDisconnect(player);
    }

    @Override
    public void onPlayerChat(Player player, String message) {
        module.onPlayerChat(player, message);
    }

    @Override
    public void openConfig(Player player) {
        module.openConfig(player);
    }

    /**
     * Récupère le module interne Solo Leveling.
     * 
     * @return le module interne
     */
    public SoloLevelingModule getInternalModule() {
        return module;
    }
}
