package com.serifsystemworks.sofd.randomizer;

import com.serifsystemworks.sofd.models.PartyMemberSpec;
import java.util.List;
import java.util.Random;

public class PartyRandomizer {

    public static void randomize(List<PartyMemberSpec> party, double scale, int maxWeaponId, Random rng) {
        for (PartyMemberSpec member : party) {
            member.setHp(Math.max(1, (int) (member.getHp() * scale)));
            member.setMp(Math.max(1, (int) (member.getMp() * scale)));
            member.setStr(Math.max(1, (int) (member.getStr() * scale)));
            member.setCon(Math.max(1, (int) (member.getCon() * scale)));
            member.setAgl(Math.max(1, (int) (member.getAgl() * scale)));
            member.setIntStat(Math.max(1, (int) (member.getIntStat() * scale)));
            member.setLuc(Math.max(1, (int) (member.getLuc() * scale)));

            if (maxWeaponId > 0) {
                member.setInitialWeaponId(rng.nextInt(maxWeaponId));
            }
        }
    }
}