from typing import List, BinaryIO, Dict
from models.encounter import EncounterSpec
from models.enemy import EnemySpec
import struct

# === CONSTANTS ===

# --- Enemy Table ---
# VERIFIED 2026-08-11 via HxD against the user's real so1pack.bin: record @0x1BAE850
# decodes to "Horned Wolf", record @0x1BAE8B0 decodes to "Bandit", and both repeat
# byte-for-byte identically later in the same block (proof of real static per-species
# data, not noise/padding).
#   - Record size: 0x60 (96) bytes — confirmed.
#   - Name field: 0x14 (20) bytes, null-padded, at offset +0x4C within the record — confirmed.
#   - Stat block: 76 bytes (0x00-0x4B) before the name, with 18+ fields that genuinely
#     differ between Horned Wolf and Bandit.
# NOT yet confirmed: which raw stat field means what (hp/mp/attack/etc. below are a
# best-effort label based on typical RPG conventions and value magnitude, not a
# confirmed fact — cross-check in-game before trusting the labels). Also not confirmed:
# the true start/length of the whole table — 0x1BAE850 is just the first record we could
# fully verify; NUM_ENEMIES below is deliberately conservative (only what we've actually
# seen), not a real table size.
OFFSET_ENEMY_TABLE = 0x1BAE850
ENEMY_ENTRY_SIZE = 0x60   # 96 bytes — VERIFIED
NUM_ENEMIES = 4            # VERIFIED count only (Horned Wolf, Bandit, Bandit, Horned Wolf)

# --- Zone Name Table ---
# NOT VERIFIED — actually WRONG as previously written. The region this used to point at
# (~0x428B180) turned out on closer inspection to still be SLZ-COMPRESSED payload, not
# raw text: a "IEND" PNG chunk marker found nearby has a stray byte spliced into the
# middle of it, which only happens inside an LZSS-compressed bitstream (literal-byte
# runs interrupted by match-copy control bytes), never in real raw/uncompressed bytes.
# The real place names we found there (Tatroi, Purgatorium, Mt. Eckdar, etc.) ARE
# genuinely from the game — LZSS literal bytes are exact copies of the source text — but
# the surrounding "record structure" we thought we saw was a compression artifact, not
# a real fixed-stride table. The true SLZ container header for this data is somewhere
# before 0x4289000, not yet located. Do not use these constants until that's found and
# decompressed with SlzCodec the same way the enemy table was verified.
OFFSET_ZONE_NAME_TABLE = 0x428B180   # UNRELIABLE — see note above
ZONE_NAME_ENTRY_SIZE = 0x10
NUM_ZONE_NAMES = 100

# --- Encounter Data Table (STUBBED - NEEDS OFFSETS) ---
OFFSET_ENCOUNTER_TABLE = 0x000000  # TODO: Find this
ENCOUNTER_ENTRY_SIZE = 0x00        # TODO: Find this (likely 0x10 or 0x20)
NUM_ENCOUNTERS = 256               # Adjust based on actual count

# === ZONE NAME PARSING ===
def read_zone_names(file: BinaryIO) -> Dict[int, str]:
    """Read all zone names from the zone name table.
    UNRELIABLE — see OFFSET_ZONE_NAME_TABLE note above. Will likely return garbage
    until the real (currently-compressed) source is located and decompressed."""
    zone_names = {}
    file.seek(OFFSET_ZONE_NAME_TABLE)
    for i in range(NUM_ZONE_NAMES):
        data = file.read(ZONE_NAME_ENTRY_SIZE)
        if len(data) < ZONE_NAME_ENTRY_SIZE:
            break
        name = data.split(b'\x00')[0].decode('utf-8', errors='ignore')
        if name:
            zone_names[i] = name
    return zone_names

# === ENEMY PARSING ===
def parse_enemy_data(data: bytes) -> EnemySpec:
    """Parse a single 0x60-byte enemy entry (VERIFIED layout — see header comment)."""
    hp     = struct.unpack_from('<H', data, 0x00)[0]
    mp     = struct.unpack_from('<H', data, 0x08)[0]
    attack = struct.unpack_from('<H', data, 0x0C)[0]
    defense = struct.unpack_from('<H', data, 0x10)[0]
    agility = struct.unpack_from('<H', data, 0x12)[0]
    exp_reward = struct.unpack_from('<H', data, 0x18)[0]
    fol_reward = struct.unpack_from('<H', data, 0x1C)[0]
    element_resist_mask = struct.unpack_from('<H', data, 0x2A)[0] & 0xFF  # width unconfirmed

    name = data[0x4C:0x4C + 0x14].split(b'\x00')[0].decode('ascii', errors='ignore')

    enemy = EnemySpec(
        id=0, name=name, hp=hp, mp=mp, attack=attack,
        defense=defense, agility=agility, exp_reward=exp_reward,
        fol_reward=fol_reward, element_resist_mask=element_resist_mask
    )
    # Preserve the additional confirmed-varying fields we don't have confident labels for yet
    enemy.stat_0x14 = struct.unpack_from('<H', data, 0x14)[0]
    enemy.stat_0x16 = struct.unpack_from('<H', data, 0x16)[0]
    enemy.stat_0x22 = struct.unpack_from('<H', data, 0x22)[0]
    enemy.stat_0x24 = struct.unpack_from('<H', data, 0x24)[0]
    enemy.stat_0x26 = struct.unpack_from('<H', data, 0x26)[0]
    enemy.stat_0x28 = struct.unpack_from('<H', data, 0x28)[0]
    enemy.stat_0x36 = struct.unpack_from('<H', data, 0x36)[0]
    enemy.stat_0x40 = struct.unpack_from('<H', data, 0x40)[0]
    enemy.stat_0x44 = struct.unpack_from('<H', data, 0x44)[0]
    enemy.stat_0x46 = struct.unpack_from('<H', data, 0x46)[0]
    enemy.stat_0x48 = struct.unpack_from('<H', data, 0x48)[0]
    return enemy

def read_enemies(file: BinaryIO) -> List[EnemySpec]:
    """Read all (currently: verified 4) enemies from so1pack.bin."""
    enemies = []
    file.seek(OFFSET_ENEMY_TABLE)
    for i in range(NUM_ENEMIES):
        data = file.read(ENEMY_ENTRY_SIZE)
        if len(data) < ENEMY_ENTRY_SIZE:
            break
        enemy = parse_enemy_data(data)
        enemy.id = i
        enemies.append(enemy)
    return enemies

# === ENCOUNTER PARSING (STUBBED) ===
def parse_encounter_data(data: bytes, zone_names: Dict[int, str]) -> EncounterSpec:
    """Parse a single encounter entry from binary data.
    TODO: Replace with actual parsing logic once offsets are known."""
    return EncounterSpec(
        zone_id=0,
        zone_name="Placeholder",
        encounter_rate=0,
        enemy_slot_ids=[0] * 6,
        boss_encounter=False,
        locked=False
    )

def read_encounters(file: BinaryIO, zone_names: Dict[int, str]) -> List[EncounterSpec]:
    """Read all encounters from so1pack.bin. STUBBED until OFFSET_ENCOUNTER_TABLE is found."""
    encounters = []
    file.seek(OFFSET_ENCOUNTER_TABLE)
    for i in range(NUM_ENCOUNTERS):
        data = file.read(ENCOUNTER_ENTRY_SIZE)
        if len(data) < ENCOUNTER_ENTRY_SIZE:
            break
        encounter = parse_encounter_data(data, zone_names)
        encounter.zone_id = i
        encounters.append(encounter)
    return encounters

# === WRITING FUNCTIONS ===
def write_enemies(file: BinaryIO, enemies: List[EnemySpec]) -> None:
    """Write enemies back to so1pack.bin, preserving any bytes we don't have a
    confirmed field mapping for yet (read-modify-write per record, not a blind overwrite)."""
    for i, enemy in enumerate(enemies):
        record_offset = OFFSET_ENEMY_TABLE + i * ENEMY_ENTRY_SIZE
        file.seek(record_offset)
        record = bytearray(file.read(ENEMY_ENTRY_SIZE))
        if len(record) < ENEMY_ENTRY_SIZE:
            raise IOError(f"Short read at enemy record {i} (offset 0x{record_offset:X})")

        struct.pack_into('<H', record, 0x00, enemy.hp & 0xFFFF)
        struct.pack_into('<H', record, 0x08, enemy.mp & 0xFFFF)
        struct.pack_into('<H', record, 0x0C, enemy.attack & 0xFFFF)
        struct.pack_into('<H', record, 0x10, enemy.defense & 0xFFFF)
        struct.pack_into('<H', record, 0x12, enemy.agility & 0xFFFF)
        struct.pack_into('<H', record, 0x18, enemy.exp_reward & 0xFFFF)
        struct.pack_into('<H', record, 0x1C, enemy.fol_reward & 0xFFFF)
        struct.pack_into('<H', record, 0x2A, enemy.element_resist_mask & 0xFFFF)
        struct.pack_into('<H', record, 0x14, enemy.stat_0x14 & 0xFFFF)
        struct.pack_into('<H', record, 0x16, enemy.stat_0x16 & 0xFFFF)
        struct.pack_into('<H', record, 0x22, enemy.stat_0x22 & 0xFFFF)
        struct.pack_into('<H', record, 0x24, enemy.stat_0x24 & 0xFFFF)
        struct.pack_into('<H', record, 0x26, enemy.stat_0x26 & 0xFFFF)
        struct.pack_into('<H', record, 0x28, enemy.stat_0x28 & 0xFFFF)
        struct.pack_into('<H', record, 0x36, enemy.stat_0x36 & 0xFFFF)
        struct.pack_into('<H', record, 0x40, enemy.stat_0x40 & 0xFFFF)
        struct.pack_into('<H', record, 0x44, enemy.stat_0x44 & 0xFFFF)
        struct.pack_into('<H', record, 0x46, enemy.stat_0x46 & 0xFFFF)
        struct.pack_into('<H', record, 0x48, enemy.stat_0x48 & 0xFFFF)

        name_bytes = enemy.name.encode('ascii', errors='ignore')[:0x14].ljust(0x14, b'\x00')
        record[0x4C:0x4C + 0x14] = name_bytes

        file.seek(record_offset)
        file.write(record)

def write_encounters(file: BinaryIO, encounters: List[EncounterSpec], zone_names: Dict[int, str]) -> None:
    """Write all encounters back to so1pack.bin."""
    # TODO: Implement once encounter table offsets are known
    pass
