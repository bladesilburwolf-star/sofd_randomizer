package com.serifsystemworks.sofd.patcher;

import com.serifsystemworks.sofd.models.EnemySpec;
import com.serifsystemworks.sofd.models.SOFDConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PatchExporter {

    /**
     * Generates a PPSSPP cheat file (.ini) from current enemy table modifications.
     */
    public static void exportPpssppPatch(File outputFile, String gameId, List<EnemySpec> enemies) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("_S " + gameId + "\n");
            writer.write("_G Star Ocean First Departure - Randomized Patch\n\n");
            writer.write("_C0 SOFD Randomizer Master Patch\n");

            for (int i = 0; i < enemies.size() && i < SOFDConstants.ENEMY_COUNT; i++) {
                EnemySpec enemy = enemies.get(i);
                long baseOffset = SOFDConstants.ENEMY_TABLE_BASE_OFFSET + ((long) i * SOFDConstants.ENEMY_RECORD_SIZE);

                // Convert offset to PSP RAM address space (User RAM starts at 0x08800000)
                long pspAddress = 0x08800000L + baseOffset;

                // Write HP (32-bit int: CWCheat code type 0x2)
                writer.write(String.format("_L 0x2%07X 0x%08X\n", (pspAddress + SOFDConstants.EnemyStruct.HP) & 0x0FFFFFFF, enemy.getHp()));

                // Write MP (16-bit uint: CWCheat code type 0x1)
                writer.write(String.format("_L 0x1%07X 0x%04X\n", (pspAddress + SOFDConstants.EnemyStruct.MP) & 0x0FFFFFFF, enemy.getMp()));

                // Write Attack (16-bit uint)
                writer.write(String.format("_L 0x1%07X 0x%04X\n", (pspAddress + SOFDConstants.EnemyStruct.ATTACK) & 0x0FFFFFFF, enemy.getAttack()));

                // Write Defense (16-bit uint)
                writer.write(String.format("_L 0x1%07X 0x%04X\n", (pspAddress + SOFDConstants.EnemyStruct.DEFENSE) & 0x0FFFFFFF, enemy.getDefense()));
            }
        }
    }

    /**
     * Calculates a CRC32 checksum over the binary target to verify data integrity.
     */
    public static long calculateFileCrc32(File binaryFile) throws IOException {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        try (java.io.InputStream is = new java.io.FileInputStream(binaryFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
        }
        return crc.getValue();
    }
}