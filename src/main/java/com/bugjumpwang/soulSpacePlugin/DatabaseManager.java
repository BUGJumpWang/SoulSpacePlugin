package com.bugjumpwang.soulSpacePlugin;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private final SoulSpacePlugin plugin;
    private Connection connection;
    private boolean mysql;

    public DatabaseManager(SoulSpacePlugin plugin) {
        this.plugin = plugin;
        if (plugin.getConfigManager().isMysql()) {
            connectMysql();
        } else {
            connectSqlite();
        }
        if (connection == null) {
            plugin.getLogger().warning("数据库连接失败，请检查配置后重载插件");
        }
    }

    private void connectSqlite() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/data.db");
            mysql = false;
            plugin.getLogger().info("已连接SQLite数据库：" + plugin.getDataFolder() + "/data.db");
        } catch (Exception e) {
            plugin.getLogger().severe("链接SQLite失败：" + e.getMessage());
        }
    }

    private void connectMysql() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String host = plugin.getConfigManager().getMysqlHost();
            int port = plugin.getConfigManager().getMysqlPort();
            String database = plugin.getConfigManager().getMysqlDatabase();
            String username = plugin.getConfigManager().getMysqlUsername();
            String password = plugin.getConfigManager().getMysqlPassword();
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&characterEncoding=utf8&serverTimezone=UTC";
            connection = DriverManager.getConnection(url, username, password);
            mysql = true;
            plugin.getLogger().info("已连接MySQL数据库：" + database);
        } catch (Exception e) {
            plugin.getLogger().severe("链接MySQL失败：" + e.getMessage() + "，回退到SQLite存储");
            connection = null;
            connectSqlite();
        }
    }

    public void initializeTables() {
        String playersTable;
        String pagesTable;
        String alterPlayerLang;
        String alterPageName;
        String alterPageIcon;
        if (mysql) {
            playersTable = "CREATE TABLE IF NOT EXISTS players (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "has_paid TINYINT(1) DEFAULT 0, " +
                    "level INT DEFAULT 1, " +
                    "language VARCHAR(16) DEFAULT 'zh-cn'" +
                    ");";
            pagesTable = "CREATE TABLE IF NOT EXISTS pages (" +
                    "uuid VARCHAR(36), " +
                    "page_number INT, " +
                    "contents LONGTEXT, " +
                    "page_name VARCHAR(255) DEFAULT '', " +
                    "page_icon VARCHAR(64) DEFAULT 'STONE', " +
                    "PRIMARY KEY (uuid, page_number)" +
                    ");";
            alterPlayerLang = "ALTER TABLE players ADD COLUMN language VARCHAR(16) DEFAULT 'zh-cn';";
            alterPageName = "ALTER TABLE pages ADD COLUMN page_name VARCHAR(255) DEFAULT '';";
            alterPageIcon = "ALTER TABLE pages ADD COLUMN page_icon VARCHAR(64) DEFAULT 'STONE';";
        } else {
            playersTable = "CREATE TABLE IF NOT EXISTS players (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "has_paid BOOLEAN DEFAULT 0, " +
                    "level INT DEFAULT 1" +
                    ");";
            pagesTable = "CREATE TABLE IF NOT EXISTS pages (" +
                    "uuid VARCHAR(36), " +
                    "page_number INT, " +
                    "contents TEXT, " +
                    "page_name TEXT DEFAULT '', " +
                    "page_icon TEXT DEFAULT 'STONE', " +
                    "PRIMARY KEY (uuid, page_number)" +
                    ");";
            alterPlayerLang = "ALTER TABLE players ADD COLUMN language TEXT DEFAULT 'zh-cn';";
            alterPageName = "ALTER TABLE pages ADD COLUMN page_name TEXT DEFAULT '';";
            alterPageIcon = "ALTER TABLE pages ADD COLUMN page_icon TEXT DEFAULT 'STONE';";
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(playersTable);
            statement.execute(pagesTable);

            try {
                statement.execute(alterPlayerLang);
            } catch (SQLException ignored) {}
            try {
                statement.execute(alterPageName);
            } catch (SQLException ignored) {}
            try {
                statement.execute(alterPageIcon);
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            plugin.getLogger().severe("创建数据表失败：" + e.getMessage());
        }
    }

    public boolean isMysql() {
        return mysql;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlayerData loadPlayerData(UUID uuid) {
        String sql = "SELECT has_paid, level, language FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String lang = rs.getString("language");
                return new PlayerData(uuid, rs.getBoolean("has_paid"), rs.getInt("level"),
                        lang == null ? "" : lang);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("加载玩家数据失败：" + e.getMessage());
        }
        return null;
    }

    public void savePlayerData(PlayerData data) {
        String sql;
        if (mysql) {
            sql = "INSERT INTO players (uuid, has_paid, level, language) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE has_paid = VALUES(has_paid), level = VALUES(level), language = VALUES(language)";
        } else {
            sql = "INSERT OR REPLACE INTO players (uuid, has_paid, level, language) VALUES (?, ?, ?, ?)";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ps.setBoolean(2, data.isHasPaid());
            ps.setInt(3, data.getLevel());
            ps.setString(4, data.getLanguage() == null ? "" : data.getLanguage());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("保存玩家数据失败：" + e.getMessage());
        }
    }

    public void updatePlayerLanguage(UUID uuid, String language) {
        String sql = "UPDATE players SET language = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, language);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("更新玩家语言失败：" + e.getMessage());
        }
    }

    public void savePage(UUID uuid, int pageNumber, String contents, String pageName, String pageIcon) {
        String sql;
        if (mysql) {
            sql = "INSERT INTO pages (uuid, page_number, contents, page_name, page_icon) VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE contents = VALUES(contents), page_name = VALUES(page_name), page_icon = VALUES(page_icon)";
        } else {
            sql = "INSERT OR REPLACE INTO pages (uuid, page_number, contents, page_name, page_icon) VALUES (?, ?, ?, ?, ?)";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, pageNumber);
            ps.setString(3, contents);
            ps.setString(4, pageName == null ? "" : pageName);
            ps.setString(5, pageIcon == null ? "STONE" : pageIcon);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("保存页面失败：" + e.getMessage());
        }
    }

    public void savePage(UUID uuid, int pageNumber, String contents) {
        PageInfo info = loadPageInfo(uuid, pageNumber);
        String name = (info != null) ? info.getName() : "";
        String icon = (info != null) ? info.getIcon() : "STONE";
        savePage(uuid, pageNumber, contents, name, icon);
    }

    public void updatePageName(UUID uuid, int pageNumber, String newName) {
        String sql = "UPDATE pages SET page_name = ? WHERE uuid = ? AND page_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, uuid.toString());
            ps.setInt(3, pageNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("更新页面名称失败：" + e.getMessage());
        }
    }

    public void updatePageIcon(UUID uuid, int pageNumber, String newIcon) {
        String sql = "UPDATE pages SET page_icon = ? WHERE uuid = ? AND page_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newIcon);
            ps.setString(2, uuid.toString());
            ps.setInt(3, pageNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("更新页面图标失败：" + e.getMessage());
        }
    }

    public PageInfo loadPageInfo(UUID uuid, int pageNumber) {
        String sql = "SELECT contents, page_name, page_icon FROM pages WHERE uuid = ? AND page_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, pageNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String contents = rs.getString("contents");
                String name = rs.getString("page_name");
                String icon = rs.getString("page_icon");
                return new PageInfo(contents, name == null ? "" : name, icon == null ? "STONE" : icon);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("加载页面信息失败：" + e.getMessage());
        }
        return null;
    }

    public void deleteAllPages(UUID uuid) {
        String sql = "DELETE FROM pages WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("删除页面失败：" + e.getMessage());
        }
    }

    public static class PageInfo {
        private final String contents;
        private final String name;
        private final String icon;

        public PageInfo(String contents, String name, String icon) {
            this.contents = contents;
            this.name = name;
            this.icon = icon;
        }

        public String getContents() { return contents; }
        public String getName() { return name; }
        public String getIcon() { return icon; }
    }
}