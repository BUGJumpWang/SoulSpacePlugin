package com.bugjumpwang.soulSpacePlugin;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private final SoulSpacePlugin plugin;
    private Connection connection;

    public DatabaseManager(SoulSpacePlugin plugin) {
        this.plugin = plugin;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/data.db");
        } catch (Exception e) {
            plugin.getLogger().severe("链接SQLite失败：" + e.getMessage());
        }
    }

    public void initializeTables() {
        String playersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "has_paid BOOLEAN DEFAULT 0, " +
                "level INT DEFAULT 1" +
                ");";

        String pagesTable = "CREATE TABLE IF NOT EXISTS pages (" +
                "uuid VARCHAR(36), " +
                "page_number INT, " +
                "contents TEXT, " +
                "page_name TEXT DEFAULT '', " +
                "page_icon TEXT DEFAULT 'STONE', " +
                "PRIMARY KEY (uuid, page_number)" +
                ");";
        try (Statement statement = connection.createStatement()) {
            statement.execute(playersTable);
            statement.execute(pagesTable);

            try {
                statement.execute("ALTER TABLE pages ADD COLUMN page_name TEXT DEFAULT '';");
            } catch (SQLException ignored) {}
            try {
                statement.execute("ALTER TABLE pages ADD COLUMN page_icon TEXT DEFAULT 'STONE';");
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            plugin.getLogger().severe("创建数据表失败：" + e.getMessage());
        }
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
        String sql = "SELECT has_paid, level FROM players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PlayerData(uuid, rs.getBoolean("has_paid"), rs.getInt("level"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("加载玩家数据失败：" + e.getMessage());
        }
        return null;
    }

    public void savePlayerData(PlayerData data) {
        String sql = "INSERT OR REPLACE INTO players (uuid, has_paid, level) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ps.setBoolean(2, data.isHasPaid());
            ps.setInt(3, data.getLevel());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("保存玩家数据失败：" + e.getMessage());
        }
    }

    public void savePage(UUID uuid, int pageNumber, String contents, String pageName, String pageIcon) {
        String sql = "INSERT OR REPLACE INTO pages (uuid, page_number, contents, page_name, page_icon) VALUES (?, ?, ?, ?, ?)";
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