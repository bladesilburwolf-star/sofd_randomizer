package com.serifsystemworks.sofd.randomizer;

import com.serifsystemworks.sofd.models.DropEntry;
import java.util.List;
import java.util.Random;

public class DropTableEngine {

    private long seed;
    private int maxItemId;

    public DropTableEngine() {
        this(System.currentTimeMillis());
    }

    public DropTableEngine(long seed) {
        this.seed = seed;
        this.maxItemId = 255;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getMaxItemId() {
        return maxItemId;
    }

    public void setMaxItemId(int maxItemId) {
        this.maxItemId = maxItemId;
    }

    public void randomize(List<DropEntry> drops) {
        if (drops == null || maxItemId <= 0) return;
        Random rng = new Random(seed);
        
        for (DropEntry drop : drops) {
            if (drop.getItemId() > 0) {
                drop.setItemId(1 + rng.nextInt(maxItemId));
                int newRate = Math.min(100, Math.max(1, drop.getDropRate() + (rng.nextInt(21) - 10)));
                drop.setDropRate(newRate);
            }
        }
    }

    public static void randomizeDrops(List<DropEntry> drops, int maxItemId, Random rng) {
        if (drops == null || maxItemId <= 0) return;
        
        for (DropEntry drop : drops) {
            if (drop.getItemId() > 0) {
                drop.setItemId(1 + rng.nextInt(maxItemId));
                int newRate = Math.min(100, Math.max(1, drop.getDropRate() + (rng.nextInt(21) - 10)));
                drop.setDropRate(newRate);
            }
        }
    }
}