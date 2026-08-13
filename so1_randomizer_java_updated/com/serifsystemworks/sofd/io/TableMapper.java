package com.serifsystemworks.sofd.io;

import com.serifsystemworks.sofd.models.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TableMapper {

    // --- ENEMY TABLE (so1pack.bin) ---
    // VERIFIED 2026-08-11 via HxD against the user's real so1pack.bin: record @0x1BAE850
    // decodes to "Horned Wolf", record @0x1BAE8B0 decodes to "Bandit", and both repeat
    // identically later in the same block (proves real static per-species data, not noise).
    // Confirmed facts: record size 0x60 (96) bytes; name field 20 bytes at +0x4C, null-padded.
    // NOT yet confirmed: the true start/count of the whole table (0x1BAE850 is just the first
    // record we could fully verify inside our sampled window — there may be earlier entries).
    // NOT yet confirmed: which raw stat field means what (see EnemySpec class doc).
    // Target file is so1pack.bin (PSP_GAME/USRDIR/so1pack.bin) — NOT DATA.BIN.
    public static final long OFFSET_ENEMIES = 0x1BAE850L;
    public static final int ENEMY_RECORD_SIZE = 0x60;
    public static final int ENEMY_NAME_OFFSET = 0x4C;
    public static final int ENEMY_NAME_LENGTH = 0x14;

    // --- OTHER TABLES (drops/party/equipment/palettes/skills/encounters) ---
    // STILL UNVERIFIED — these offsets are placeholders inherited from before the real
    // so1pack.bin format was cracked. Do not trust them without the same HxD + duplicate-
    // record verification treatment the enemy table got. Left in place only so the rest
    // of the tool keeps compiling; loadX()/saveX() below will very likely read garbage.
    public static final long OFFSET_DROPS      = 0x00018000L;
    public static final long OFFSET_PARTY      = 0x00020000L;
    public static final long OFFSET_EQUIPMENT  = 0x00024000L;
    public static final long OFFSET_PALETTES   = 0x0002B000L;
    public static final long OFFSET_SKILLS     = 0x00031000L;
    public static final long OFFSET_ENCOUNTERS = 0x00038000L;

    // Helper: Sanitizes string buffers preventing multi-byte corruption
    private static String readCleanString(RandomAccessFile raf, int length) throws Exception {
        byte[] buf = new byte[length];
        raf.readFully(buf);
        int end = 0;
        while (end < buf.length && buf[end] != 0) {
            end++;
        }
        return new String(buf, 0, end, StandardCharsets.UTF_8).trim();
    }

    private static void writeCleanString(RandomAccessFile raf, String str, int length) throws Exception {
        byte[] buf = new byte[length];
        if (str != null) {
            byte[] src = str.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(src, 0, buf, 0, Math.min(src.length, length));
        }
        raf.write(buf);
    }

    // --- ENEMIES ---
    /**
     * Reads enemy records starting at the VERIFIED anchor OFFSET_ENEMIES.
     * count defaults conservatively — the true table start/length isn't confirmed yet
     * (see class-level notes), so this only walks forward from a known-good record
     * rather than assuming a large fixed table size.
     */
    public static List<EnemySpec> loadEnemies(File file) throws Exception {
        return loadEnemies(file, 4); // 4 = the count we've actually verified (HornedWolf, Bandit x2, HornedWolf)
    }

    public static List<EnemySpec> loadEnemies(File file, int count) throws Exception {
        List<EnemySpec> list = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            for (int i = 0; i < count; i++) {
                raf.seek(OFFSET_ENEMIES + (long) i * ENEMY_RECORD_SIZE);
                byte[] record = new byte[ENEMY_RECORD_SIZE];
                raf.readFully(record);
                list.add(parseEnemyRecord(i, record));
            }
        }
        return list;
    }

    private static EnemySpec parseEnemyRecord(int id, byte[] r) {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(r).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int hp     = bb.getShort(0x00) & 0xFFFF;
        int mp     = bb.getShort(0x08) & 0xFFFF;
        int attack = bb.getShort(0x0C) & 0xFFFF;
        int def    = bb.getShort(0x10) & 0xFFFF;
        int agl    = bb.getShort(0x12) & 0xFFFF;
        int exp    = bb.getShort(0x18) & 0xFFFF;
        int fol    = bb.getShort(0x1C) & 0xFFFF;
        int resist = bb.getShort(0x2A) & 0xFFFF;

        int nameEnd = ENEMY_NAME_OFFSET;
        while (nameEnd < ENEMY_NAME_OFFSET + ENEMY_NAME_LENGTH && r[nameEnd] != 0) nameEnd++;
        String name = new String(r, ENEMY_NAME_OFFSET, nameEnd - ENEMY_NAME_OFFSET, StandardCharsets.US_ASCII);

        EnemySpec e = new EnemySpec(id, name, hp, mp, attack, def, agl, exp, fol, (byte) resist);
        e.setStat_0x14(bb.getShort(0x14) & 0xFFFF);
        e.setStat_0x16(bb.getShort(0x16) & 0xFFFF);
        e.setStat_0x22(bb.getShort(0x22) & 0xFFFF);
        e.setStat_0x24(bb.getShort(0x24) & 0xFFFF);
        e.setStat_0x26(bb.getShort(0x26) & 0xFFFF);
        e.setStat_0x28(bb.getShort(0x28) & 0xFFFF);
        e.setStat_0x36(bb.getShort(0x36) & 0xFFFF);
        e.setStat_0x40(bb.getShort(0x40) & 0xFFFF);
        e.setStat_0x44(bb.getShort(0x44) & 0xFFFF);
        e.setStat_0x46(bb.getShort(0x46) & 0xFFFF);
        e.setStat_0x48(bb.getShort(0x48) & 0xFFFF);
        return e;
    }

    public static void saveEnemies(File file, List<EnemySpec> list) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            for (int i = 0; i < list.size(); i++) {
                EnemySpec e = list.get(i);
                long recordOffset = OFFSET_ENEMIES + (long) i * ENEMY_RECORD_SIZE;
                raf.seek(recordOffset);
                byte[] record = new byte[ENEMY_RECORD_SIZE];
                raf.readFully(record); // preserve fields we don't know the meaning of yet
                java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(record).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                bb.putShort(0x00, (short) e.getHp());
                bb.putShort(0x08, (short) e.getMp());
                bb.putShort(0x0C, (short) e.getAttack());
                bb.putShort(0x10, (short) e.getDefense());
                bb.putShort(0x12, (short) e.getAgility());
                bb.putShort(0x18, (short) e.getExpReward());
                bb.putShort(0x1C, (short) e.getFolReward());
                bb.putShort(0x2A, (short) e.getElementResistMask());
                bb.putShort(0x14, (short) e.getStat_0x14());
                bb.putShort(0x16, (short) e.getStat_0x16());
                bb.putShort(0x22, (short) e.getStat_0x22());
                bb.putShort(0x24, (short) e.getStat_0x24());
                bb.putShort(0x26, (short) e.getStat_0x26());
                bb.putShort(0x28, (short) e.getStat_0x28());
                bb.putShort(0x36, (short) e.getStat_0x36());
                bb.putShort(0x40, (short) e.getStat_0x40());
                bb.putShort(0x44, (short) e.getStat_0x44());
                bb.putShort(0x46, (short) e.getStat_0x46());
                bb.putShort(0x48, (short) e.getStat_0x48());
                // Name field: only rewrite if it still fits the verified 20-byte slot
                byte[] nameBytes = new byte[ENEMY_NAME_LENGTH];
                if (e.getName() != null) {
                    byte[] src = e.getName().getBytes(StandardCharsets.US_ASCII);
                    System.arraycopy(src, 0, nameBytes, 0, Math.min(src.length, ENEMY_NAME_LENGTH));
                }
                System.arraycopy(nameBytes, 0, record, ENEMY_NAME_OFFSET, ENEMY_NAME_LENGTH);
                raf.seek(recordOffset);
                raf.write(record);
            }
        }
    }

    // --- DROPS ---
    public static List<DropEntry> loadDrops(File file) throws Exception {
        List<DropEntry> list = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(OFFSET_DROPS);
            for (int i = 0; i < 32; i++) {
                int id = raf.readUnsignedShort();
                int enemyId = raf.readUnsignedShort();
                int itemId = raf.readUnsignedShort();
                int rate = Math.min(100, raf.readUnsignedShort()); // Cap rate to 100%
                int flags = raf.readUnsignedShort();
                list.add(new DropEntry(id, enemyId, itemId, rate, flags));
            }
        }
        return list;
    }

    public static void saveDrops(File file, List<DropEntry> list) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(OFFSET_DROPS);
            for (DropEntry d : list) {
                raf.writeShort(d.getId() & 0xFFFF);
                raf.writeShort(d.getEnemyId() & 0xFFFF);
                raf.writeShort(d.getItemId() & 0xFFFF);
                raf.writeShort(d.getDropRate() & 0xFFFF);
                raf.writeShort(d.getExtraFlags() & 0xFFFF);
            }
        }
    }

    // --- PARTY MEMBERS ---
    public static List<PartyMemberSpec> loadParty(File file) throws Exception {
        List<PartyMemberSpec> list = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(OFFSET_PARTY);
            for (int i = 0; i < 12; i++) {
                int id = raf.readUnsignedShort();
                String name = readCleanString(raf, 16);
                int lvl = raf.readUnsignedByte();
                
                int hp = Math.max(1, Math.min(9999, raf.readInt()));
                int mp = Math.min(999, raf.readUnsignedShort());
                int str = Math.min(255, raf.readUnsignedShort());
                int con = Math.min(255, raf.readUnsignedShort());
                int agl = Math.min(255, raf.readUnsignedShort());
                int intel = Math.min(255, raf.readUnsignedShort());
                int luc = Math.min(255, raf.readUnsignedShort());
                int weaponId = raf.readUnsignedShort();

                list.add(new PartyMemberSpec(id, name, lvl, hp, mp, str, con, agl, intel, luc, weaponId));
            }
        }
        return list;
    }

    public static void saveParty(File file, List<PartyMemberSpec> list) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(OFFSET_PARTY);
            for (PartyMemberSpec p : list) {
                raf.writeShort(p.getCharacterId() & 0xFFFF);
                writeCleanString(raf, p.getName(), 16);
                raf.writeByte(p.getLevel() & 0xFF);
                raf.writeInt(Math.max(1, p.getHp()));
                raf.writeShort(p.getMp() & 0xFFFF);
                raf.writeShort(p.getStr() & 0xFFFF);
                raf.writeShort(p.getCon() & 0xFFFF);
                raf.writeShort(p.getAgl() & 0xFFFF);
                raf.writeShort(p.getIntStat() & 0xFFFF);
                raf.writeShort(p.getLuc() & 0xFFFF);
                raf.writeShort(p.getInitialWeaponId() & 0xFFFF);
            }
        }
    }

    // --- EQUIPMENT ---
    public static List<EquipmentSpec> loadEquipment(File file) throws Exception {
        List<EquipmentSpec> list = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(OFFSET_EQUIPMENT);
            for (int i = 0; i < 48; i++) {
                int id = raf.readUnsignedShort();
                String type = readCleanString(raf, 8);
                String name = readCleanString(raf, 16);
                int atk = Math.min(9999, raf.readUnsignedShort());
                int def = Math.min(9999, raf.readUnsignedShort());
                int mag = Math.min(9999, raf.readUnsignedShort());
                int hit = Math.min(9999, raf.readUnsignedShort());
                int avd = Math.min(9999, raf.readUnsignedShort());
                int price = Math.max(0, raf.readInt());
                byte elem = raf.readByte();
                raf.readByte();

                list.add(new EquipmentSpec(id, type, name, atk, def, mag, hit, avd, price, elem));
            }
        }
        return list;
    }

    public static void saveEquipment(File file, List<EquipmentSpec> list) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(OFFSET_EQUIPMENT);
            for (EquipmentSpec eq : list) {
                raf.writeShort(eq.getId() & 0xFFFF);
                writeCleanString(raf, eq.getTypeName(), 8);
                writeCleanString(raf, eq.getName(), 16);
                raf.writeShort(eq.getAtk() & 0xFFFF);
                raf.writeShort(eq.getDef() & 0xFFFF);
                raf.writeShort(eq.getMagAtk() & 0xFFFF);
                raf.writeShort(eq.getHit() & 0xFFFF);
                raf.writeShort(eq.getAvd() & 0xFFFF);
                raf.writeInt(Math.max(0, eq.getPrice()));
                raf.writeByte(eq.getElementMask());
                raf.writeByte(0);
            }
        }
    }

    // --- PALETTES ---
    public static List<PaletteSpec> loadPalettes(File file) throws Exception {
        List<PaletteSpec> list = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(OFFSET_PALETTES);
            for (int i = 0; i < 24; i++) {
                int spriteId = raf.readUnsignedShort();
                long clutOffset = raf.readInt() & 0xFFFFFFFFL;
                String name = readCleanString(raf, 16);
                int colors = raf.readUnsignedShort();

                list.add(new PaletteSpec(spriteId, clutOffset, name, colors));
            }
        }
        return list;
    }

    public static void savePalettes(File file, List<PaletteSpec> list) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(OFFSET_PALETTES);
            for (PaletteSpec pal : list) {
                raf.writeShort(pal.getSpriteId() & 0xFFFF);
                raf.writeInt((int) (pal.getClutOffset() & 0xFFFFFFFFL));
                writeCleanString(raf, pal.getPaletteName(), 16);
                raf.writeShort(pal.getColorCount() & 0xFFFF);
            }
        }
    }

    // --- SKILLS ---
    public static List<SkillSpec> loadSkills(File file) throws Exception {
        List<SkillSpec> list = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(OFFSET_SKILLS);
            for (int i = 0; i < 30; i++) {
                int id = raf.readUnsignedShort();
                String cat = readCleanString(raf, 12);
                String name = readCleanString(raf, 16);
                int sp = raf.readUnsignedShort();
                int maxLvl = raf.readUnsignedByte();
                int icRate = raf.readUnsignedByte();

                list.add(new SkillSpec(id, cat, name, sp, maxLvl, icRate));
            }
        }
        return list;
    }

    public static void saveSkills(File file, List<SkillSpec> list) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(OFFSET_SKILLS);
            for (SkillSpec s : list) {
                raf.writeShort(s.getId() & 0xFFFF);
                writeCleanString(raf, s.getCategoryName(), 12);
                writeCleanString(raf, s.getName(), 16);
                raf.writeShort(s.getReqSp() & 0xFFFF);
                raf.writeByte(s.getMaxLevel() & 0xFF);
                raf.writeByte(s.getIcSuccessRate() & 0xFF);
            }
        }
    }

    // --- ENCOUNTERS ---
    public static List<EncounterSpec> loadEncounters(File file) throws Exception {
        List<EncounterSpec> list = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(OFFSET_ENCOUNTERS);
            for (int i = 0; i < 20; i++) {
                int zoneId = raf.readUnsignedShort();
                String area = readCleanString(raf, 16);
                int rate = raf.readUnsignedByte();
                raf.readByte(); // Padding byte
                int[] slots = new int[6];
                for (int s = 0; s < 6; s++) {
                    slots[s] = raf.readUnsignedShort();
                }

                list.add(new EncounterSpec(zoneId, area, rate, slots));
            }
        }
        return list;
    }

    public static void saveEncounters(File file, List<EncounterSpec> list) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(OFFSET_ENCOUNTERS);
            for (EncounterSpec enc : list) {
                raf.writeShort(enc.getZoneId() & 0xFFFF);
                writeCleanString(raf, enc.getZoneName(), 16);
                raf.writeByte(enc.getEncounterRate() & 0xFF);
                raf.writeByte(0);
                int[] slots = enc.getEnemySlotIds();
                for (int s = 0; s < 6; s++) {
                    raf.writeShort((s < slots.length ? slots[s] : 0) & 0xFFFF);
                }
            }
        }
    }
}