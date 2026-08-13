package com.serifsystemworks.sofd.randomizer;

import com.serifsystemworks.sofd.models.EnemySpec;
import java.util.List;
import java.util.Random;

public class SeedManager {

    public enum Preset {
        BALANCED("Standard Run", 0.8f, 1.2f),
        CHAOS("Chaos Mode", 0.4f, 2.5f),
        HARDCORE("Hardcore Scaling", 1.2f, 3.0f);

        private final String label;
        private final float minScale;
        private final float maxScale;

        Preset(String label, float minScale, float maxScale) {
            this.label = label;
            this.minScale = minScale;
            this.maxScale = maxScale;
        }

        public String getLabel() { return label; }
        public float getMinScale() { return minScale; }
        public float getMaxScale() { return maxScale; }
    }

    public static void applySeedAndPreset(List<EnemySpec> enemies, String seedText, Preset preset) {
        long seed = seedText.trim().hashCode();
        Random rng = new Random(seed);

        for (EnemySpec enemy : enemies) {
            float scale = preset.getMinScale() + (rng.nextFloat() * (preset.getMaxScale() - preset.getMinScale()));
            enemy.setHp((int) Math.min(99999, Math.max(1, enemy.getHp() * scale)));
            enemy.setMp((int) Math.min(9999, Math.max(0, enemy.getMp() * scale)));
            enemy.setAttack((int) Math.min(9999, Math.max(1, enemy.getAttack() * scale)));
            enemy.setDefense((int) Math.min(9999, Math.max(0, enemy.getDefense() * scale)));
        }
    }
}