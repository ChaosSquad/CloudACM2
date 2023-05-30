package net.jandie1505;

import net.jandie1505.config.DefaultConfigValues;
import net.jandie1505.configmanager.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CloudACM2 extends JavaPlugin {
    private ConfigManager configManager;

    @Override
    public void onEnable() {

        // Config

        this.configManager = new ConfigManager(DefaultConfigValues.getConfig(), false, this.getDataFolder(), "config.json");
        this.configManager.reloadConfig();


    }
}
