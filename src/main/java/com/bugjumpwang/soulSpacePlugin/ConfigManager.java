package com.bugjumpwang.soulSpacePlugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final SoulSpacePlugin plugin;
    private int firstCost;
    private int upgradeCost;
    private int maxLevel;
    private String prefix;
    private String defaultLanguage;
    private String storageType;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;

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
        config.addDefault("default-language", "zh-cn");
        config.addDefault("storage-type", "sqlite");
        config.addDefault("mysql.host", "localhost");
        config.addDefault("mysql.port", 3306);
        config.addDefault("mysql.database", "soulspace");
        config.addDefault("mysql.username", "root");
        config.addDefault("mysql.password", "");
        config.options().copyDefaults(true);
        plugin.saveConfig();

        firstCost = config.getInt("first-cost");
        upgradeCost = config.getInt("upgrade-cost");
        maxLevel = config.getInt("max-level");
        prefix = config.getString("prefix", "&b[SSP] ");
        defaultLanguage = config.getString("default-language", "zh-cn");
        storageType = config.getString("storage-type", "sqlite");
        mysqlHost = config.getString("mysql.host", "localhost");
        mysqlPort = config.getInt("mysql.port", 3306);
        mysqlDatabase = config.getString("mysql.database", "soulspace");
        mysqlUsername = config.getString("mysql.username", "root");
        mysqlPassword = config.getString("mysql.password", "");
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

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public String getStorageType() {
        return storageType;
    }

    public boolean isMysql() {
        return "mysql".equalsIgnoreCase(storageType);
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }
}
