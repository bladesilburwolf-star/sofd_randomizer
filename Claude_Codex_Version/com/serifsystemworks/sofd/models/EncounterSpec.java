package com.serifsystemworks.sofd.models;

public class EncounterSpec {
    private int zoneId;
    private String zoneName;
    private int encounterRate;
    private int[] enemySlotIds;

    public EncounterSpec(int zoneId, String zoneName, int encounterRate, int[] enemySlotIds) {
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.encounterRate = encounterRate;
        this.enemySlotIds = enemySlotIds != null ? enemySlotIds : new int[6];
    }

    public EncounterSpec(int zoneId, String zoneName, int encounterRate, int s1, int s2, int s3, int s4, int s5, int s6) {
        this(zoneId, zoneName, encounterRate, new int[]{s1, s2, s3, s4, s5, s6});
    }

    public int getZoneId() { return zoneId; }
    public String getZoneName() { return zoneName; }
    public String getAreaName() { return zoneName; }
    public int getEncounterRate() { return encounterRate; }
    public void setEncounterRate(int encounterRate) { this.encounterRate = encounterRate; }
    public int[] getEnemySlotIds() { return enemySlotIds; }
    public void setEnemySlotIds(int[] enemySlotIds) { this.enemySlotIds = enemySlotIds; }
    
    public int getSlot1() { return enemySlotIds.length > 0 ? enemySlotIds[0] : 0; }
    public int getSlot2() { return enemySlotIds.length > 1 ? enemySlotIds[1] : 0; }
    public int getSlot3() { return enemySlotIds.length > 2 ? enemySlotIds[2] : 0; }
    public int getSlot4() { return enemySlotIds.length > 3 ? enemySlotIds[3] : 0; }
    public int getSlot5() { return enemySlotIds.length > 4 ? enemySlotIds[4] : 0; }
    public int getSlot6() { return enemySlotIds.length > 5 ? enemySlotIds[5] : 0; }
}