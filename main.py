import random
import shutil
from typing import List
from models.enemy import EnemySpec
from models.encounter import EncounterSpec
from randomizers.enemy_randomizer import EnemyRandomizer
from randomizers.encounter_randomizer import randomize_encounters
import so1pack

def main():
    input_file = "so1pack.bin"
    output_file = "so1pack_randomized.bin"

    # Option A: Make a copy first, then modify the copy in place
    shutil.copyfile(input_file, output_file)

    with open(output_file, "rb+") as f:
        # Read enemies and encounters
        enemies = so1pack.read_enemies(f)
        zone_names = so1pack.read_zone_names(f)
        encounters = so1pack.read_encounters(f, zone_names)

        # Randomize enemies
        enemy_randomizer = EnemyRandomizer(seed=42)
        enemy_randomizer.set_randomize_stats(True)
        enemy_randomizer.set_randomize_rewards(True)
        enemy_randomizer.set_shuffle_resistances(False)
        enemy_randomizer.process_enemies(enemies)

        # Randomize encounters
        rng = random.Random(42)
        randomize_encounters(
            encounters,
            max_enemy_id=255,
            rng=rng,
            include_empty_slots=False
        )

        # Write back to the output file
        f.seek(0)
        so1pack.write_enemies(f, enemies)
        so1pack.write_encounters(f, encounters)

    print(f"Randomization complete. Output saved to: C:\\Emulators\\PSP\\so1_randomizer\\{output_file}")

if __name__ == "__main__":
    main()