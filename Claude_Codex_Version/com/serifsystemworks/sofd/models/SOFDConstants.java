package com.serifsystemworks.sofd.models;

public class SOFDConstants {

    // Target Game IDs: ULUS10374 (First Departure) / ULUS10375 (Second Evolution)
    
    // --- ENEMY TABLE CONSTANTS ---
    public static final long ENEMY_TABLE_BASE_OFFSET = 0x00120000L; 
    public static final int ENEMY_RECORD_SIZE = 32;                 
    public static final int ENEMY_COUNT = 256;                      

    public static class EnemyStruct {
        public static final long ID = 0x00;                  // uint16
        public static final long HP = 0x02;                  // int32
        public static final long MP = 0x06;                  // uint16
        public static final long ATTACK = 0x08;              // uint16
        public static final long DEFENSE = 0x0A;             // uint16
        public static final long AGILITY = 0x0C;             // uint16
        public static final long EXP_REWARD = 0x0E;          // int32
        public static final long FOL_REWARD = 0x12;          // int32
        public static final long ELEMENT_RESIST_MASK = 0x16; // byte
    }

    // --- DROP TABLE CONSTANTS ---
    public static final long DROP_TABLE_BASE_OFFSET = 0x00124000L;
    public static final int DROP_RECORD_SIZE = 12;                
    public static final int DROP_COUNT = 256;                     

    public static class DropStruct {
        public static final long ENEMY_ID = 0x00;     // uint16
        public static final long ITEM_1_ID = 0x02;    // uint16
        public static final long ITEM_1_RATE = 0x04;  // uint16
        public static final long ITEM_2_ID = 0x06;    // uint16
        public static final long ITEM_2_RATE = 0x08;  // uint16
    }

    // --- PARTY MEMBER TABLE CONSTANTS ---
    public static final long PARTY_TABLE_BASE_OFFSET = 0x00128000L;
    public static final int PARTY_RECORD_SIZE = 24;
    public static final int PARTY_COUNT = 12;

    public static class PartyStruct {
        public static final long ID = 0x00;                 // uint16
        public static final long INITIAL_LEVEL = 0x02;      // uint16
        public static final long BASE_HP = 0x04;            // int32
        public static final long BASE_MP = 0x08;            // uint16
        public static final long STR = 0x0A;                // uint16
        public static final long CON = 0x0C;                // uint16
        public static final long AGL = 0x0E;                // uint16
        public static final long INT = 0x10;                // uint16
        public static final long LUC = 0x12;                // uint16
        public static final long INITIAL_WEAPON = 0x14;     // uint16
    }

    // --- EQUIPMENT TABLE CONSTANTS ---
    public static final long EQUIPMENT_TABLE_BASE_OFFSET = 0x0012C000L;
    public static final int EQUIPMENT_RECORD_SIZE = 20;
    public static final int EQUIPMENT_COUNT = 200;

    public static class EquipmentStruct {
        public static final long ID = 0x00;                 // uint16
        public static final long TYPE = 0x02;               // uint16
        public static final long ATK = 0x04;                // uint16
        public static final long DEF = 0x06;                // uint16
        public static final long MAG_ATK = 0x08;            // uint16
        public static final long HIT = 0x0A;                // uint16
        public static final long AVD = 0x0C;                // uint16
        public static final long PRICE = 0x0E;              // int32
        public static final long ELEMENT_MASK = 0x12;        // byte
    }

    // --- PALETTE CONSTANTS ---
    public static final long PALETTE_TABLE_BASE_OFFSET = 0x00130000L;
    public static final int PALETTE_COLORS_PER_CLUT = 16;
    public static final int PALETTE_COUNT = 64;

    // --- SKILL & IC TABLE CONSTANTS ---
    public static final long SKILL_TABLE_BASE_OFFSET = 0x00134000L;
    public static final int SKILL_RECORD_SIZE = 12;
    public static final int SKILL_COUNT = 96;

    public static class SkillStruct {
        public static final long ID = 0x00;                 // uint16
        public static final long REQ_SP = 0x02;             // uint16
        public static final long MAX_LEVEL = 0x04;          // uint16
        public static final long IC_SUCCESS_RATE = 0x06;    // uint16
        public static final long CATEGORY = 0x08;           // uint16
    }

    // --- ENCOUNTER & MAP SPAWN TABLE CONSTANTS ---
    public static final long ENCOUNTER_TABLE_BASE_OFFSET = 0x00138000L;
    public static final int ENCOUNTER_RECORD_SIZE = 16;
    public static final int ENCOUNTER_ZONE_COUNT = 128;
    public static final int ENCOUNTER_SLOTS_PER_ZONE = 6;

    public static class EncounterStruct {
        public static final long ZONE_ID = 0x00;            // uint16
        public static final long ENCOUNTER_RATE = 0x02;     // uint16
        public static final long ENEMY_SLOT_1 = 0x04;       // uint16
        public static final long ENEMY_SLOT_2 = 0x06;       // uint16
        public static final long ENEMY_SLOT_3 = 0x08;       // uint16
        public static final long ENEMY_SLOT_4 = 0x0A;       // uint16
        public static final long ENEMY_SLOT_5 = 0x0C;       // uint16
        public static final long ENEMY_SLOT_6 = 0x0E;       // uint16
    }
}