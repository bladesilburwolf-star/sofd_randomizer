import random
from typing import List, Optional
from models.encounter import EncounterSpec

def randomize_encounters(
    encounters: List[EncounterSpec],
    max_enemy_id: int,
    rng: random.Random,
    include_empty_slots: bool = False
) -> None:
    """
    Randomizes enemy slots across all provided encounter zones.

    Args:
        encounters: List of EncounterSpec objects to randomize.
        max_enemy_id: Maximum valid enemy ID (1 to max_enemy_id).
        rng: Random instance to maintain seed continuity.
        include_empty_slots: If True, slots set to 0 will also be assigned enemies.
    """
    if not encounters or max_enemy_id <= 0 or rng is None:
        return

    for zone in encounters:
        if zone is None:
            continue

        # Skip boss or locked encounters
        if zone.boss_encounter or zone.locked:
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