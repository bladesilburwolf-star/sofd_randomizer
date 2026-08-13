package com.serifsystemworks.sofd.randomizer;

import com.serifsystemworks.sofd.models.SkillSpec;
import java.util.List;
import java.util.Random;

public class SkillRandomizer {

    public static void randomize(List<SkillSpec> skills, double scale, double spMultiplier, boolean boostIcSuccess, Random rng) {
        for (SkillSpec skill : skills) {
            int newSp = Math.max(1, (int) (skill.getReqSp() * scale * spMultiplier));
            skill.setReqSp(newSp);

            if (boostIcSuccess && "Specialty".equalsIgnoreCase(skill.getCategoryName())) {
                int boosted = Math.min(100, skill.getIcSuccessRate() + 20 + rng.nextInt(15));
                skill.setIcSuccessRate(boosted);
            }
        }
    }
}