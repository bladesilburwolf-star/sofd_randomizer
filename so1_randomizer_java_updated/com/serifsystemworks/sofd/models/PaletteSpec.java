package com.serifsystemworks.sofd.models;

public class PaletteSpec {
    private int spriteId;
    private long clutOffset;
    private String paletteName;
    private int[] argbColors;

    public PaletteSpec(int spriteId, long clutOffset, String paletteName, int[] argbColors) {
        this.spriteId = spriteId;
        this.clutOffset = clutOffset;
        this.paletteName = paletteName;
        this.argbColors = argbColors != null ? argbColors : new int[0];
    }

    public PaletteSpec(int spriteId, long clutOffset, String paletteName, int colorCount) {
        this(spriteId, clutOffset, paletteName, new int[colorCount]);
    }

    public int getSpriteId() { return spriteId; }
    public long getClutOffset() { return clutOffset; }
    public String getPaletteName() { return paletteName; }
    public int[] getArgbColors() { return argbColors; }
    public void setArgbColors(int[] argbColors) { this.argbColors = argbColors; }
    public int getColorCount() { return argbColors.length; }
}