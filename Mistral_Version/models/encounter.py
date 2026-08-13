from dataclasses import dataclass, field
from typing import List, Optional

@dataclass
class EncounterSpec:
    zone_id: int
    zone_name: str
    encounter_rate: int = 0
    enemy_slot_ids: List[int] = field(default_factory=lambda: [0] * 6)
    boss_encounter: bool = False
    locked: bool = False

    def __post_init__(self):
        # Clamp encounter_rate to 0-100
        self.encounter_rate = max(0, min(100, self.encounter_rate))
        # Ensure enemy_slot_ids is exactly 6 elements
        if len(self.enemy_slot_ids) > 6:
            self.enemy_slot_ids = self.enemy_slot_ids[:6]
        else:
            self.enemy_slot_ids = self.enemy_slot_ids + [0] * (6 - len(self.enemy_slot_ids))