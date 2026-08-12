from dataclasses import dataclass

@dataclass
class EnemySpec:
    """Represents an enemy in Star Ocean: First Departure."""
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
        """Clamp values to match Java logic."""
        self.hp = max(1, min(99999, self.hp))
        self.mp = max(0, min(9999, self.mp))
        self.attack = max(1, min(9999, self.attack))
        self.defense = max(0, min(9999, self.defense))
        self.agility = max(0, min(9999, self.agility))
        self.exp_reward = max(1, min(999999, self.exp_reward))
        self.fol_reward = max(0, min(999999, self.fol_reward))
        self.element_resist_mask = max(0, min(255, self.element_resist_mask))