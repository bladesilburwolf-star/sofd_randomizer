package com.serifsystemworks.sofd.randomizer;

import com.serifsystemworks.sofd.models.EquipmentSpec;

import java.util.List;
import java.util.Random;

public class EquipmentRandomizer {

    private boolean randomizeStats = true;
    private boolean randomizePrices = true;
    private boolean randomizeElements = false;
    private double minScale = 0.7;
    private double maxScale = 1.4;
    private final long seed;

    public EquipmentRandomizer(long seed) {
        this.seed = seed;
    }

    public void processEquipment(List<EquipmentSpec> items) {
        Random rng = new Random(seed);

        for (EquipmentSpec item : items) {
            if (randomizeStats) {
                double scale = minScale + (rng.nextDouble() * (maxScale - minScale));
                item.setAtk((int) (item.getAtk() * scale));
                item.setDef((int) (item.getDef() * scale));
                item.setMagAtk((int) (item.getMagAtk() * scale));
                item.setHit((int) (item.getHit() * scale));
                item.setAvd((int) (item.getAvd() * scale));
            }

            if (randomizePrices) {
                double priceScale = 0.5 + (rng.nextDouble() * 1.5);
                item.setPrice(Math.max(10, (int) (item.getPrice() * priceScale)));
            }

            if (randomizeElements) {
                byte randomMask = (byte) rng.nextInt(256);
                item.setElementMask(randomMask);
            }
        }
    }

    public void setRandomizeStats(boolean randomizeStats) { this.randomizeStats = randomizeStats; }
    public void setRandomizePrices(boolean randomizePrices) { this.randomizePrices = randomizePrices; }
    public void setRandomizeElements(boolean randomizeElements) { this.randomizeElements = randomizeElements; }
    public void setMinScale(double minScale) { this.minScale = minScale; }
    public void setMaxScale(double maxScale) { this.maxScale = maxScale; }
}