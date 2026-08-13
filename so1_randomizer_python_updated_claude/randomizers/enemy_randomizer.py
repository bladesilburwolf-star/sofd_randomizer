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
        # OFF by default: several of the newly-preserved raw fields (stat_0x36, stat_0x44,
        # stat_0x46, stat_0x48, etc.) have unconfirmed meaning — they could be stats, but
        # could just as easily be AI pattern IDs, sprite/model IDs, or sound IDs. Scaling
        # those blindly risks glitches/crashes rather than just re-balancing difficulty.
        # Leave this off until we've confirmed what each field actually controls in-game.
        self.randomize_unknown_stats = False

    def set_randomize_stats(self, value: bool) -> None:
        self.randomize_stats = value

    def set_randomize_rewards(self, value: bool) -> None:
        self.randomize_rewards = value

    def set_shuffle_resistances(self, value: bool) -> None:
        self.shuffle_resistances = value

    def set_randomize_unknown_stats(self, value: bool) -> None:
        self.randomize_unknown_stats = value

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

            if self.randomize_unknown_stats:
                for attr in ("stat_0x14", "stat_0x16", "stat_0x22", "stat_0x24",
                             "stat_0x26", "stat_0x28", "stat_0x36", "stat_0x40",
                             "stat_0x44", "stat_0x46", "stat_0x48"):
                    val = getattr(enemy, attr)
                    setattr(enemy, attr, max(0, min(65535, int(val * scale))))
