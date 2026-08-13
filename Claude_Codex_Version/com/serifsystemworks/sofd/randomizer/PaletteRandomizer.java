package com.serifsystemworks.sofd.randomizer;

import java.util.Random;

public class PaletteRandomizer {

    /**
     * Shifts RGBA/BGR555 palette buffers while preserving index 0 (transparency).
     */
    public static void randomizePaletteBuffer(byte[] paletteData, long seed, boolean hueShiftOnly) {
        Random rng = new Random(seed);

        // Assuming 16-color or 256-color palette blocks (2 bytes per BGR555 color)
        for (int i = 2; i < paletteData.length; i += 2) {
            int colorWord = ((paletteData[i + 1] & 0xFF) << 8) | (paletteData[i] & 0xFF);

            // Extract 5-bit RGB
            int r = colorWord & 0x1F;
            int g = (colorWord >> 5) & 0x1F;
            int b = (colorWord >> 10) & 0x1F;

            if (hueShiftOnly) {
                // Rotate channels
                int temp = r;
                r = g;
                g = b;
                b = temp;
            } else {
                // Random noise shift within bounds
                r = Math.min(31, Math.max(0, r + (rng.nextInt(11) - 5)));
                g = Math.min(31, Math.max(0, g + (rng.nextInt(11) - 5)));
                b = Math.min(31, Math.max(0, b + (rng.nextInt(11) - 5)));
            }

            int newColorWord = (colorWord & 0x8000) | (b << 10) | (g << 5) | r;
            paletteData[i] = (byte) (newColorWord & 0xFF);
            paletteData[i + 1] = (byte) ((newColorWord >> 8) & 0xFF);
        }
    }
}