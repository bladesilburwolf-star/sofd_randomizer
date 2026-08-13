package com.serifsystemworks.sofd.randomizer;

import com.serifsystemworks.sofd.models.PaletteSpec;
import java.util.List;
import java.util.Random;

public class PaletteEngine {

    public static void randomizePalettes(List<PaletteSpec> palettes, Random rng) {
        for (PaletteSpec spec : palettes) {
            int[] colors = spec.getArgbColors();
            for (int i = 0; i < colors.length; i++) {
                int a = (colors[i] >> 24) & 0xFF;
                int r = rng.nextInt(256);
                int g = rng.nextInt(256);
                int b = rng.nextInt(256);
                colors[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
            spec.setArgbColors(colors);
        }
    }
}