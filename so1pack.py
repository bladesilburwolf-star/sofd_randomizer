from typing import List, BinaryIO, Dict
from models.encounter import EncounterSpec
from models.enemy import EnemySpec
import struct

# === CONSTANTS ===
# Enemy Table
OFFSET_ENEMY_TABLE = 0x1BAE800
ENEMY_ENTRY_SIZE = 0x70  # 112 bytes
NUM_ENEMIES = 256

# Zone Name Table
OFFSET_ZONE_NAME_TABLE = 0x4289C00  # Confirmed from HxD
ZONE_NAME_ENTRY_SIZE = 0x10  # 16 bytes
NUM_ZONE_NAMES = 100

# Encounter Data Table (UPDATED)
OFFSET_ENCOUNTER_TABLE = 0x428A240  # After zone names: 0x4289C00 + (100 * 0x10)
ENCOUNTER_ENTRY_SIZE = 0x10  # 16 bytes
NUM_ENCOUNTERS = 100

# === ZONE NAME PARSING (FIXED) ===
def read_zone_names(file: BinaryIO) -> Dict[int, str]:
    """Read all zone names from the zone name table."""
    zone_names = {}
    file.seek(OFFSET_ZONE_NAME_TABLE)
    for i in range(NUM_ZONE_NAMES):
        data = file.read(ZONE_NAME_ENTRY_SIZE)
        if len(data) < ZONE_NAME_ENTRY_SIZE:
            break
        # Extract first 12 bytes (zone names are 12 chars max)
        name = data[0:12].split(b'\x00')[0].decode('ascii', errors='ignore').strip()
        if name:
            zone_names[i] = name
    return zone_names

# === ENEMY PARSING ===
def parse_enemy_data(data: bytes) -> EnemySpec:
    """Parse a single enemy entry from binary data."""
    hp = struct.unpack('<I', data[0x00:0x04])[0]
    mp = struct.unpack('<I', data[0x04:0x08])[0]
    attack = struct.unpack('<I', data[0x08:0x0C])[0]
    defense = struct.unpack('<I', data[0x0C:0x10])[0]
    agility = struct.unpack('<I', data[0x10:0x14])[0]

    # Name: 32 bytes, null-terminated
    name = data[0x14:0x34].split(b'\x00')[0].decode('ascii', errors='ignore')

    exp_reward = struct.unpack('<I', data[0x34:0x38])[0]
    fol_reward = struct.unpack('<I', data[0x38:0x3C])[0]

    element_resist_mask = data[0x6F]

    return EnemySpec(
        id=0, name=name, hp=hp, mp=mp, attack=attack,
        defense=defense, agility=agility, exp_reward=exp_reward,
        fol_reward=fol_reward, element_resist_mask=element_resist_mask
    )

def read_enemies(file: BinaryIO) -> List[EnemySpec]:
    """Read all enemies from so1pack.bin."""
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

# === ENCOUNTER PARSING (FIXED) ===
def parse_encounter_data(data: bytes, zone_names: Dict[int, str]) -> EncounterSpec:
    """Parse a single encounter entry from binary data."""
    zone_id = data[0x00]
    encounter_rate = data[0x01]

    # Enemy slots: 6 x 1-byte values
    enemy_slot_ids = list(data[0x02:0x08])

    # Flags: Boss/Locked (1 byte)
    flags = data[0x08]
    boss_encounter = (flags & 0x01) != 0
    locked = (flags & 0x02) != 0

    zone_name = zone_names.get(zone_id, f"Unknown Zone {zone_id}")

    return EncounterSpec(
        zone_id=zone_id,
        zone_name=zone_name,
        encounter_rate=encounter_rate,
        enemy_slot_ids=enemy_slot_ids,
        boss_encounter=boss_encounter,
        locked=locked
    )

def read_encounters(file: BinaryIO, zone_names: Dict[int, str]) -> List[EncounterSpec]:
    """Read all encounters from so1pack.bin."""
    encounters = []
    file.seek(OFFSET_ENCOUNTER_TABLE)
    for i in range(NUM_ENCOUNTERS):
        data = file.read(ENCOUNTER_ENTRY_SIZE)
        if len(data) < ENCOUNTER_ENTRY_SIZE:
            break
        encounter = parse_encounter_data(data, zone_names)
        encounters.append(encounter)
    return encounters

# === WRITING FUNCTIONS ===
def write_enemies(file: BinaryIO, enemies: List[EnemySpec]) -> None:
    """Write all enemies back to so1pack.bin."""
    file.seek(OFFSET_ENEMY_TABLE)
    for enemy in enemies:
        data = (
            struct.pack('<I', enemy.hp) +
            struct.pack('<I', enemy.mp) +
            struct.pack('<I', enemy.attack) +
            struct.pack('<I', enemy.defense) +
            struct.pack('<I', enemy.agility) +
            enemy.name.encode('ascii').ljust(32, b'\x00')[:32] +
            struct.pack('<I', enemy.exp_reward) +
            struct.pack('<I', enemy.fol_reward) +
            b'\x00' * (ENEMY_ENTRY_SIZE - 0x3C - 1) +
            bytes([enemy.element_resist_mask])
        )
        file.write(data)

def write_encounters(file: BinaryIO, encounters: List[EncounterSpec]) -> None:
    """Write all encounters back to so1pack.bin."""
    file.seek(OFFSET_ENCOUNTER_TABLE)
    for encounter in encounters:
        data = (
            bytes([encounter.zone_id]) +
            bytes([encounter.encounter_rate]) +
            bytes(encounter.enemy_slot_ids) +  # 6 x 1-byte values
            bytes([(1 if encounter.boss_encounter else 0) | (2 if encounter.locked else 0)]) +
            b'\x00' * 7  # Padding to 16 bytes
        )
        file.write(data)

# === DEBUG: Test when run directly ===
if __name__ == "__main__":
    with open("so1pack.bin", "rb") as f:
        # Test zone names
        zone_names = read_zone_names(f)
        print("Zone Names:", zone_names)

        # Test enemies
        f.seek(0)
        enemies = read_enemies(f)
        print(f"Read {len(enemies)} enemies.")
        if enemies:
            print("First Enemy:", enemies[0].name, "HP:", enemies[0].hp)

        # Test encounters
        f.seek(0)
        encounters = read_encounters(f, zone_names)
        print(f"Read {len(encounters)} encounters.")
        if encounters:
            print(
                "First Encounter:",
                encounters[0].zone_name,
                "Rate:", encounters[0].encounter_rate,
                "Slots:", encounters[0].enemy_slot_ids
            )