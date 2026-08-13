package com.serifsystemworks.sofd.models;

public class EquipmentSpec {
    private int id;
    private String typeName;
    private String name;
    private int atk;
    private int def;
    private int magAtk;
    private int hit;
    private int avd;
    private int price;
    private byte elementMask;

    public EquipmentSpec(int id, String typeName, String name, int atk, int def, int magAtk, int hit, int avd, int price, byte elementMask) {
        this.id = id;
        this.typeName = typeName;
        this.name = name;
        this.atk = atk;
        this.def = def;
        this.magAtk = magAtk;
        this.hit = hit;
        this.avd = avd;
        this.price = price;
        this.elementMask = elementMask;
    }

    public int getId() { return id; }
    public String getTypeName() { return typeName; }
    public String getName() { return name; }
    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }
    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }
    public int getMagAtk() { return magAtk; }
    public void setMagAtk(int magAtk) { this.magAtk = magAtk; }
    public int getHit() { return hit; }
    public void setHit(int hit) { this.hit = hit; }
    public int getAvd() { return avd; }
    public void setAvd(int avd) { this.avd = avd; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public byte getElementMask() { return elementMask; }
    public void setElementMask(byte elementMask) { this.elementMask = elementMask; }
}