package com.bugjumpwang.soulSpacePlugin;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private boolean hasPaid;
    private int level;
    private String language;

    public PlayerData(UUID uuid, boolean hasPaid, int level) {
        this(uuid, hasPaid, level, null);
    }

    public PlayerData(UUID uuid, boolean hasPaid, int level, String language) {
        this.uuid = uuid;
        this.hasPaid = hasPaid;
        this.level = level;
        this.language = language;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isHasPaid() {
        return hasPaid;
    }

    public void setHasPaid(boolean hasPaid) {
        this.hasPaid = hasPaid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
