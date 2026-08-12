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