package com.bugjumpwang.soulSpacePlugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageManager {

    private static final String[] BUILTIN_LANGUAGES = {"zh-cn", "en-us"};

    private final SoulSpacePlugin plugin;
    private final Map<String, FileConfiguration> languages = new HashMap<>();
    private String defaultLanguage;

    public LanguageManager(SoulSpacePlugin plugin) {
        this.plugin = plugin;
        this.defaultLanguage = plugin.getConfigManager().getDefaultLanguage();
        loadLanguages();
    }

    private void loadLanguages() {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        for (String code : BUILTIN_LANGUAGES) {
            File target = new File(langDir, code + ".yml");
            if (!target.exists()) {
                try (InputStream in = plugin.getResource("lang/" + code + ".yml")) {
                    if (in != null) {
                        Files.copy(in, target.toPath());
                        plugin.getLogger().info("已生成语言文件：lang/" + code + ".yml");
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("写入语言文件失败 " + code + "：" + e.getMessage());
                }
            }
            languages.put(code, YamlConfiguration.loadConfiguration(target));
        }

        File[] extra = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (extra != null) {
            for (File file : extra) {
                String code = file.getName().substring(0, file.getName().length() - 4);
                if (!languages.containsKey(code)) {
                    languages.put(code, YamlConfiguration.loadConfiguration(file));
                }
            }
        }

        if (!languages.containsKey(defaultLanguage)) {
            defaultLanguage = "zh-cn";
        }
        plugin.getLogger().info("多语言系统已加载，可用语言：" + getAvailableLanguages());
    }

    public void reload() {
        languages.clear();
        loadLanguages();
    }

    public boolean isSupported(String code) {
        return code != null && languages.containsKey(code.toLowerCase());
    }

    public List<String> getAvailableLanguages() {
        return new ArrayList<>(languages.keySet());
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public String getPlayerLanguage(Player player) {
        PlayerData data = plugin.getDatabaseManager().loadPlayerData(player.getUniqueId());
        if (data != null && data.getLanguage() != null && languages.containsKey(data.getLanguage())) {
            return data.getLanguage();
        }
        return defaultLanguage;
    }

    public String getMessage(Player player, String key, String... args) {
        return getMessage(getPlayerLanguage(player), key, args);
    }

    public String getMessage(String langCode, String key, String... args) {
        FileConfiguration lang = languages.get(langCode);
        String raw = null;
        if (lang != null && lang.contains(key)) {
            raw = lang.getString(key);
        } else {
            FileConfiguration fallback = languages.get(defaultLanguage);
            if (fallback != null && fallback.contains(key)) {
                raw = fallback.getString(key);
            }
        }
        if (raw == null) {
            raw = key;
        }
        for (int i = 0; i < args.length; i++) {
            raw = raw.replace("{" + i + "}", args[i] == null ? "" : args[i]);
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String getSoulSpaceDisplayName(Player player) {
        return getMessage(player, "soul-space-prefix") + getMessage(player, "soul-space-name");
    }
}
