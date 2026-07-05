package com.bugjumpwang.soulSpacePlugin;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private boolean hasPaid;
    private int level;

    public PlayerData(UUID uuid, boolean hasPaid, int level) {
        this.uuid = uuid;
        this.hasPaid = hasPaid;
        this.level = level;
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
}
