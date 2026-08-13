package com.serifsystemworks.sofd.randomizer;

import com.serifsystemworks.sofd.models.EncounterSpec;
import java.util.List;
import java.util.Random;

public class EncounterRandomizer {

    public static void randomize(List<EncounterSpec> encounters, int maxEnemyId, Random rng) {
        for (EncounterSpec zone : encounters) {
            int[] slots = zone.getEnemySlotIds();
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] != 0) {
                    slots[i] = 1 + rng.nextInt(maxEnemyId);
                }
            }
            zone.setEnemySlotIds(slots);
        }
    }
}