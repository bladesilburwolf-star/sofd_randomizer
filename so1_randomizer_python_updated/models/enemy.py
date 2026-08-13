from dataclasses import dataclass, field
from typing import Optional

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
    element_resist_mask: int  # Stored as int (0-255), but written as byte

    # --- Additional confirmed-varying raw fields (see so1pack.py header comment) ---
    # Named by their raw byte offset within the 96-byte record. Real, verified data —
    # just not yet mapped to a confident semantic label the way hp/attack/etc. are
    # (and even those labels are still best-guesses; see so1pack.py for details).
    stat_0x14: int = 0
    stat_0x16: int = 0
    stat_0x22: int = 0
    stat_0x24: int = 0
    stat_0x26: int = 0
    stat_0x28: int = 0
    stat_0x36: int = 0
    stat_0x40: int = 0
    stat_0x44: int = 0
    stat_0x46: int = 0
    stat_0x48: int = 0

    def __post_init__(self):
        # Clamp values to match Java logic
        self.hp = max(1, min(99999, self.hp))
        self.mp = max(0, min(9999, self.mp))
        self.attack = max(1, min(9999, self.attack))
        self.defense = max(0, min(9999, self.defense))
        self.agility = max(0, min(9999, self.agility))
        self.exp_reward = max(1, min(999999, self.exp_reward))
        self.fol_reward = max(0, min(999999, self.fol_reward))
        self.element_resist_mask = max(0, min(255, self.element_resist_mask))
