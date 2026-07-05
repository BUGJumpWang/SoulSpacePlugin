package com.bugjumpwang.soulSpacePlugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final SoulSpacePlugin plugin;
    private int firstCost;
    private int upgradeCost;
    private int maxLevel;
    private String prefix;

    public ConfigManager(SoulSpacePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        config.addDefault("first-cost", 2000);
        config.addDefault("upgrade-cost", 5000);
        config.addDefault("max-level", 30);
        config.addDefault("prefix", "&b[SSP] ");
        config.options().copyDefaults(true);
        plugin.saveConfig();

        firstCost = config.getInt("first-cost");
        upgradeCost = config.getInt("upgrade-cost");
        maxLevel = config.getInt("max-level");
        prefix = config.getString("prefix", "&b[SSP] ");
    }

    public int getFirstCost() {
        return firstCost;
    }

    public int getUpgradeCost() {
        return upgradeCost;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', prefix) + "灵魂空间";
    }
}
