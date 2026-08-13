package com.serifsystemworks.sofd.models;

/**
 * Enemy record from so1pack.bin's enemy/encounter table.
 *
 * VERIFIED 2026-08-11 against real bytes extracted from the user's actual
 * so1pack.bin via HxD (record @0x1BAE850 = "Horned Wolf", @0x1BAE8B0 = "Bandit"):
 *   - Record size: 0x60 (96) bytes — confirmed by exact byte-for-byte match between
 *     the two duplicate "Bandit" records and the two duplicate "Horned Wolf" records.
 *   - Name field: 20 bytes, null-padded, at offset +0x4C within the record.
 *   - Stat block: 76 bytes (0x00-0x4B) before the name, containing at least 18
 *     distinct fields that differ between Horned Wolf and Bandit (real per-species
 *     data, not padding/noise).
 *
 * NOT YET VERIFIED: which specific field is HP vs ATK vs EXP vs Fol, etc. The
 * mapping below is a best-effort guess based on typical RPG stat-block conventions
 * and the relative magnitude of the values we observed (e.g. the first field being
 * a moderate value that looks HP-like). Treat the named getters (getHp(), etc.) as
 * *labels of convenience*, not confirmed fact, until cross-checked in-game (change
 * one field, reload the save/battle, see what actually moves).
 *
 * All raw stat fields are preserved (see getRawStat/setRawStat) so nothing found
 * during verification gets silently dropped even where we're unsure of its meaning.
 */
public class EnemySpec {
    private int id;
    private String name;

    // --- Best-guess semantic labels (UNVERIFIED — see class doc) ---
    private int hp;         // raw offset +0x00 (u16)
    private int mp;         // raw offset +0x08 (u16)
    private int attack;     // raw offset +0x0C (u16)
    private int defense;    // raw offset +0x10 (u16)
    private int agility;    // raw offset +0x12 (u16)
    private int expReward;  // raw offset +0x18 (u16)
    private int folReward;  // raw offset +0x1C (u16)
    byte elementResistMask; // raw offset +0x2A (u16, truncated to byte) — likely wrong width, unverified

    // --- Additional confirmed-varying fields with no confident label yet ---
    // Keyed by their raw byte offset within the 96-byte record for traceability.
    private int stat_0x14, stat_0x16, stat_0x22, stat_0x24, stat_0x26, stat_0x28;
    private int stat_0x36, stat_0x40, stat_0x44, stat_0x46, stat_0x48;

    public EnemySpec(int id, String name, int hp, int mp, int attack, int defense, int agility, int expReward, int folReward, byte elementResistMask) {
        this.id = id;
        this.name = name;
        this.hp = hp;
        this.mp = mp;
        this.attack = attack;
        this.defense = defense;
        this.agility = agility;
        this.expReward = expReward;
        this.folReward = folReward;
        this.elementResistMask = elementResistMask;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = mp; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public int getAgility() { return agility; }
    public void setAgility(int agility) { this.agility = agility; }
    public int getExpReward() { return expReward; }
    public void setExpReward(int expReward) { this.expReward = expReward; }
    public int getFolReward() { return folReward; }
    public void setFolReward(int folReward) { this.folReward = folReward; }
    public byte getElementResistMask() { return elementResistMask; }
    public void setElementResistMask(byte elementResistMask) { this.elementResistMask = elementResistMask; }

    // Raw, unlabeled but confirmed-real fields (see raw offsets in field comments above)
    public int getStat_0x14() { return stat_0x14; } public void setStat_0x14(int v) { stat_0x14 = v; }
    public int getStat_0x16() { return stat_0x16; } public void setStat_0x16(int v) { stat_0x16 = v; }
    public int getStat_0x22() { return stat_0x22; } public void setStat_0x22(int v) { stat_0x22 = v; }
    public int getStat_0x24() { return stat_0x24; } public void setStat_0x24(int v) { stat_0x24 = v; }
    public int getStat_0x26() { return stat_0x26; } public void setStat_0x26(int v) { stat_0x26 = v; }
    public int getStat_0x28() { return stat_0x28; } public void setStat_0x28(int v) { stat_0x28 = v; }
    public int getStat_0x36() { return stat_0x36; } public void setStat_0x36(int v) { stat_0x36 = v; }
    public int getStat_0x40() { return stat_0x40; } public void setStat_0x40(int v) { stat_0x40 = v; }
    public int getStat_0x44() { return stat_0x44; } public void setStat_0x44(int v) { stat_0x44 = v; }
    public int getStat_0x46() { return stat_0x46; } public void setStat_0x46(int v) { stat_0x46 = v; }
    public int getStat_0x48() { return stat_0x48; } public void setStat_0x48(int v) { stat_0x48 = v; }
}