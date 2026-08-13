package com.serifsystemworks.sofd.models;

public class DropEntry {
    private int id;
    private int enemyId;
    private int itemId;
    private int dropRate;
    private int extraFlags;

    public DropEntry(int id, int enemyId, int itemId, int dropRate, int extraFlags) {
        this.id = id;
        this.enemyId = enemyId;
        this.itemId = itemId;
        this.dropRate = dropRate;
        this.extraFlags = extraFlags;
    }

    public DropEntry(int enemyId, int itemId, int dropRate) {
        this(0, enemyId, itemId, dropRate, 0);
    }

    public int getId() { return id; }
    public int getEnemyId() { return enemyId; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public int getDropRate() { return dropRate; }
    public void setDropRate(int dropRate) { this.dropRate = dropRate; }
    public int getExtraFlags() { return extraFlags; }
}