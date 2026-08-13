package com.serifsystemworks.sofd.randomizer;

import com.serifsystemworks.sofd.models.EnemySpec;
import java.util.List;
import java.util.Random;

public class EnemyRandomizer {

    private final Random rng;
    private boolean randomizeStats = true;
    private boolean randomizeRewards = true;
    private boolean shuffleResistances = false;
    private double scaleMin = 0.8;
    private double scaleMax = 1.3;

    public EnemyRandomizer(long seed) {
        this.rng = new Random(seed);
    }

    public void setRandomizeStats(boolean randomizeStats) {
        this.randomizeStats = randomizeStats;
    }

    public void setRandomizeRewards(boolean randomizeRewards) {
        this.randomizeRewards = randomizeRewards;
    }

    public void setShuffleResistances(boolean shuffleResistances) {
        this.shuffleResistances = shuffleResistances;
    }

    public void setScaleMin(double scaleMin) {
        this.scaleMin = scaleMin;
    }

    public void setScaleMax(double scaleMax) {
        this.scaleMax = scaleMax;
    }

    public void processEnemies(List<EnemySpec> enemies) {
        for (EnemySpec enemy : enemies) {
            double scale = scaleMin + (rng.nextDouble() * (scaleMax - scaleMin));

            if (randomizeStats) {
                enemy.setHp((int) Math.min(99999, Math.max(1, enemy.getHp() * scale)));
                enemy.setMp((int) Math.min(9999, Math.max(0, enemy.getMp() * scale)));
                enemy.setAttack((int) Math.min(9999, Math.max(1, enemy.getAttack() * scale)));
                enemy.setDefense((int) Math.min(9999, Math.max(0, enemy.getDefense() * scale)));
                enemy.setAgility((int) Math.min(9999, Math.max(0, enemy.getAgility() * scale)));
            }

            if (randomizeRewards) {
                enemy.setExpReward((int) Math.min(999999, Math.max(1, enemy.getExpReward() * scale)));
                enemy.setFolReward((int) Math.min(999999, Math.max(0, enemy.getFolReward() * scale)));
            }

            if (shuffleResistances) {
                enemy.setElementResistMask((byte) rng.nextInt(256));
            }
        }
    }
}