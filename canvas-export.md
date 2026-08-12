# Star Ocean: First Departure Randomizer (Python)

This is a Python refactor of the Java-based Star Ocean: First Departure randomizer. The goal is to modularize the logic for randomizing enemies, encounters, and other game data in `so1pack.bin`.

## Structure

```
so1_randomizer/
├── models/
│   ├── __init__.py
│   ├── enemy.py          # EnemySpec: Enemy data model
│   └── encounter.py      # EncounterSpec: Encounter data model
├── randomizers/
│   ├── __init__.py
│   ├── enemy_randomizer.py      # Randomizes enemy stats, rewards, and resistances
│   └── encounter_randomizer.py # Randomizes enemy slots in encounters
├── so1pack.py            # Handles reading/writing so1pack.bin (stubbed for offsets)
└── main.py               # Main script to tie everything together
```

## Key Features

### Models
- **`EnemySpec`**: Represents enemy data (ID, name, HP, MP, stats, rewards, resistances).
- **`EncounterSpec`**: Represents encounter zones (ID, name, rate, enemy slots, flags).

### Randomizers
- **`EnemyRandomizer`**: Scales enemy stats/rewards and shuffles resistances.
- **`EncounterRandomizer`**: Randomizes enemy slots in encounter zones.

### Binary I/O (Stubbed)
- **`so1pack.py`**: Placeholder for reading/writing `so1pack.bin`. Replace offsets and parsing logic once known.

## Usage

1. **Update Offsets**: Fill in the actual offsets for `so1pack.bin` in `so1pack.py`.
2. **Run the Randomizer**:
   ```bash
   python main.py
   ```
3. **Output**: A randomized `so1pack_randomized.bin` will be generated.

## Notes
- The binary parsing logic in `so1pack.py` is **stubbed** and requires actual offsets from `so1pack.bin`.
- The Java UI (`EnemyPanel.java`, `EncounterPanel.java`) is not ported here. Focus is on core logic.

## Next Steps
1. **Discover Offsets**: Use a hex editor (e.g., HxD, 010 Editor) or existing documentation to find the offsets for enemy and encounter data in `so1pack.bin`.
2. **Implement Binary Parsing**: Update `so1pack.py` with the correct `struct.unpack` formats and offsets.
3. **Test**: Validate the randomized output with the game.

---

## File Contents

### `models/enemy.py`
```python
from dataclasses import dataclass

@dataclass
class EnemySpec:
    id: int
    name: str
    hp: int
    mp: int
    attack: int
    defense: int
    agility: int
    exp_reward: int
    fol_reward: int
    element_resist_mask: int

    def __post_init__(self):
        self.hp = max(1, min(99999, self.hp))
        self.mp = max(0, min(9999, self.mp))
        self.attack = max(1, min(9999, self.attack))
        self.defense = max(0, min(9999, self.defense))
        self.agility = max(0, min(9999, self.agility))
        self.exp_reward = max(1, min(999999, self.exp_reward))
        self.fol_reward = max(0, min(999999, self.fol_reward))
        self.element_resist_mask = max(0, min(255, self.element_resist_mask))
```

### `models/encounter.py`
```python
from dataclasses import dataclass, field
from typing import List

@dataclass
class EncounterSpec:
    zone_id: int
    zone_name: str
    encounter_rate: int = 0
    enemy_slot_ids: List[int] = field(default_factory=lambda: [0] * 6)
    boss_encounter: bool = False
    locked: bool = False

    def __post_init__(self):
        self.encounter_rate = max(0, min(100, self.encounter_rate))
        if len(self.enemy_slot_ids) > 6:
            self.enemy_slot_ids = self.enemy_slot_ids[:6]
        else:
            self.enemy_slot_ids = self.enemy_slot_ids + [0] * (6 - len(self.enemy_slot_ids))
```

### `randomizers/enemy_randomizer.py`
```python
import random
from typing import List
from models.enemy import EnemySpec

class EnemyRandomizer:
    def __init__(self, seed: int):
        self.rng = random.Random(seed)
        self.randomize_stats = True
        self.randomize_rewards = True
        self.shuffle_resistances = False
        self.scale_min = 0.8
        self.scale_max = 1.3

    def set_randomize_stats(self, value: bool) -> None:
        self.randomize_stats = value

    def set_randomize_rewards(self, value: bool) -> None:
        self.randomize_rewards = value

    def set_shuffle_resistances(self, value: bool) -> None:
        self.shuffle_resistances = value

    def set_scale_min(self, value: float) -> None:
        self.scale_min = value

    def set_scale_max(self, value: float) -> None:
        self.scale_max = value

    def process_enemies(self, enemies: List[EnemySpec]) -> None:
        for enemy in enemies:
            scale = self.scale_min + (self.rng.random() * (self.scale_max - self.scale_min))
            if self.randomize_stats:
                enemy.hp = max(1, min(99999, int(enemy.hp * scale)))
                enemy.mp = max(0, min(9999, int(enemy.mp * scale)))
                enemy.attack = max(1, min(9999, int(enemy.attack * scale)))
                enemy.defense = max(0, min(9999, int(enemy.defense * scale)))
                enemy.agility = max(0, min(9999, int(enemy.agility * scale)))
            if self.randomize_rewards:
                enemy.exp_reward = max(1, min(999999, int(enemy.exp_reward * scale)))
                enemy.fol_reward = max(0, min(999999, int(enemy.fol_reward * scale)))
            if self.shuffle_resistances:
                enemy.element_resist_mask = self.rng.randint(0, 255)
```

### `randomizers/encounter_randomizer.py`
```python
import random
from typing import List
from models.encounter import EncounterSpec

def randomize_encounters(
    encounters: List[EncounterSpec],
    max_enemy_id: int,
    rng: random.Random,
    include_empty_slots: bool = False
) -> None:
    if not encounters or max_enemy_id <= 0 or rng is None:
        return
    for zone in encounters:
        if zone is None or zone.boss_encounter or zone.locked:
            continue
        slots = zone.enemy_slot_ids
        if not slots:
            continue
        modified = False
        for i in range(len(slots)):
            if slots[i] != 0 or include_empty_slots:
                slots[i] = 1 + rng.randint(0, max_enemy_id - 1)
                modified = True
        if modified:
            zone.enemy_slot_ids = slots
```

### `so1pack.py`
```python
import struct
from typing import List, BinaryIO
from models.encounter import EncounterSpec
from models.enemy import EnemySpec

# TODO: Replace with actual offsets from so1pack.bin
OFFSET_ENEMY_TABLE = 0x0000
OFFSET_ENCOUNTER_TABLE = 0x0000
ENEMY_ENTRY_SIZE = 0x00
ENCOUNTER_ENTRY_SIZE = 0x00
NUM_ENEMIES = 0
NUM_ENCOUNTERS = 0

def read_enemies(file: BinaryIO) -> List[EnemySpec]:
    enemies = []
    # file.seek(OFFSET_ENEMY_TABLE)
    # for _ in range(NUM_ENEMIES):
    #     data = file.read(ENEMY_ENTRY_SIZE)
    #     enemy = parse_enemy_data(data)
    #     enemies.append(enemy)
    return enemies

def write_enemies(file: BinaryIO, enemies: List[EnemySpec]) -> None:
    pass

def read_encounters(file: BinaryIO) -> List[EncounterSpec]:
    encounters = []
    # file.seek(OFFSET_ENCOUNTER_TABLE)
    # for _ in range(NUM_ENCOUNTERS):
    #     data = file.read(ENCOUNTER_ENTRY_SIZE)
    #     encounter = parse_encounter_data(data)
    #     encounters.append(encounter)
    return encounters

def write_encounters(file: BinaryIO, encounters: List[EncounterSpec]) -> None:
    pass

def parse_enemy_data(data: bytes) -> EnemySpec:
    return EnemySpec(
        id=0, name="Placeholder", hp=0, mp=0, attack=0,
        defense=0, agility=0, exp_reward=0, fol_reward=0, element_resist_mask=0
    )

def parse_encounter_data(data: bytes) -> EncounterSpec:
    return EncounterSpec(
        zone_id=0, zone_name="Placeholder", encounter_rate=0,
        enemy_slot_ids=[0] * 6, boss_encounter=False, locked=False
    )
```

### `main.py`
```python
import random
from models.enemy import EnemySpec
from models.encounter import EncounterSpec
from randomizers.enemy_randomizer import EnemyRandomizer
from randomizers.encounter_randomizer import randomize_encounters
import so1pack

def main():
    input_file = "so1pack.bin"
    output_file = "so1pack_randomized.bin"
    with open(input_file, "rb") as f:
        enemies = so1pack.read_enemies(f)
        encounters = so1pack.read_encounters(f)
    enemy_randomizer = EnemyRandomizer(seed=42)
    enemy_randomizer.set_randomize_stats(True)
    enemy_randomizer.set_randomize_rewards(True)
    enemy_randomizer.process_enemies(enemies)
    rng = random.Random(42)
    randomize_encounters(encounters, max_enemy_id=255, rng=rng)
    with open(output_file, "wb") as f:
        so1pack.write_enemies(f, enemies)
        so1pack.write_encounters(f, encounters)
    print(f"Randomization complete. Output saved to {output_file}")

if __name__ == "__main__":
    main()
```