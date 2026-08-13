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

    # Copy first so we NEVER touch the user's original archive in place.
    # (The previous version of this script opened input_file directly in "rb+"
    # mode and wrote into it, while printing a message claiming it wrote to
    # output_file — it never actually did. Fixed here.)
    shutil.copyfile(input_file, output_file)

    with open(output_file, "rb+") as f:
        enemies = so1pack.read_enemies(f)
        print(f"Read {len(enemies)} enemy record(s) "
              f"(only {so1pack.NUM_ENEMIES} verified so far — see so1pack.py header comment).")
        for e in enemies:
            print(f"  [{e.id}] {e.name!r}  hp={e.hp} mp={e.mp} atk={e.attack} "
                  f"def={e.defense} agl={e.agility} exp={e.exp_reward} fol={e.fol_reward}")

        # zone_names intentionally empty for now — see OFFSET_ZONE_NAME_TABLE note in
        # so1pack.py; that region turned out to still be compressed, not real offsets yet.
        encounters = so1pack.read_encounters(f, zone_names={})  # still stubbed — see so1pack.py TODOs

        enemy_randomizer = EnemyRandomizer(seed=42)
        enemy_randomizer.set_randomize_stats(True)
        enemy_randomizer.set_randomize_rewards(True)
        enemy_randomizer.set_shuffle_resistances(False)
        enemy_randomizer.process_enemies(enemies)

        rng = random.Random(42)
        randomize_encounters(
            encounters,
            max_enemy_id=255,  # placeholder — encounter table not located yet
            rng=rng,
            include_empty_slots=False
        )

        so1pack.write_enemies(f, enemies)
        so1pack.write_encounters(f, encounters, zone_names={})  # currently a no-op stub

    print(f"Randomization complete. Output saved to {output_file} (original {input_file} untouched).")

if __name__ == "__main__":
    main()
