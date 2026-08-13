package com.serifsystemworks.sofd.models;

public class PartyMemberSpec {
    private int characterId;
    private String name;
    private int level;
    private int hp;
    private int mp;
    private int str;
    private int con;
    private int agl;
    private int intStat;
    private int luc;
    private int initialWeaponId;

    public PartyMemberSpec(int characterId, String name, int level, int hp, int mp, int str, int con, int agl, int intStat, int luc, int initialWeaponId) {
        this.characterId = characterId;
        this.name = name;
        this.level = level;
        this.hp = hp;
        this.mp = mp;
        this.str = str;
        this.con = con;
        this.agl = agl;
        this.intStat = intStat;
        this.luc = luc;
        this.initialWeaponId = initialWeaponId;
    }

    public int getCharacterId() { return characterId; }
    public int getId() { return characterId; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = mp; }
    public int getStr() { return str; }
    public void setStr(int str) { this.str = str; }
    public int getCon() { return con; }
    public void setCon(int con) { this.con = con; }
    public int getAgl() { return agl; }
    public void setAgl(int agl) { this.agl = agl; }
    public int getIntStat() { return intStat; }
    public void setIntStat(int intStat) { this.intStat = intStat; }
    public int getLuc() { return luc; }
    public void setLuc(int luc) { this.luc = luc; }
    public int getInitialWeaponId() { return initialWeaponId; }
    public void setInitialWeaponId(int initialWeaponId) { this.initialWeaponId = initialWeaponId; }
}