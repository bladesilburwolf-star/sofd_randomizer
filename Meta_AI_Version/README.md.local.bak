# sofd_randomizer - Star Ocean First Departure USA PSP (ULUS10374) Randomizer

Repo: bladesilburwolf-star/sofd_randomizer
`gh repo clone bladesilburwolf-star/sofd_randomizer`

## Target
- **Star Ocean First Departure USA PSP** - ULUS10374
- NOT Steam R port (that's different engine)
- PSP is tricky: ISO is UMD, USRDIR has DATA.BIN, FIELD, BATTLE archives with offset tables

## Team
- Grok + Gemini: Final Fantasy Type-0 randomizer (big project)
- Claude + Meta AI (this repo): Star Ocean First Departure USA PSP

## Setup (Linux Mint 21 Cinnamon / PSP)
```bash
# Clone
gh repo clone bladesilburwolf-star/sofd_randomizer
cd sofd_randomizer

# Build
javac src/SoFDRandomizer.java -d .
java SoFDRandomizer

# Or build with gradle/maven later
```

## Workflow USA PSP
1. Extract USA ISO with UMDGen or `ps2iso unpack` / 7zip
2. Select `PSP_GAME/USRDIR` folder in tool (button 1)
3. Select output folder (button 2)
4. Unpack ALL (USA PSP) - handles LE+BE offset tables, recursive FIELD archives
5. Scan SoFD Tables - finds chests/shops/enemies/drops by size heuristic
6. Randomize:
   - Chests: FIELD/*.BIN subfiles, 4-256 bytes, item IDs u16 LE
   - Shops: SHOP.BIN, 64-4096 bytes
   - Enemies: BATTLE/*.BIN, 200-1200 bytes
   - Drops: drop tables 16-512 bytes
7. Repack for PSP - creates .repacked + .repacked.BE (use LE version for USA)
8. Replace original BINs in USRDIR, rebuild ISO with UMDGen

## PSP Tricky Notes
- EBOOT.BIN is encrypted - don't touch, work on USRDIR only
- Offset tables: first int = count, next count ints = offsets, then data
- Can be little-endian (PSX compat) or big-endian (PSP native) - tool detects both
- Nested archives: DATA.BIN contains FIELD.BIN which contains 0000.bin etc - recursive unpack needed
- Use -Xmx4G for large ISO unpack

## Previous Project
This repo evolved from Darkstone PSM unpacker (PsmManTool.java) which handled similar offset tables.
Darkstone demake v5 phone-fixed is in /mnt/data/DARKSTONE_DEMAKE_V5_PHONE_FIXED.BAS - working on Linux Mint QB64-PE.

## Seed System
- Full randomizer uses base seed + derived: seed_chest, seed_shop, seed_enemy, seed_drop
- Deterministic: same seed = same shuffle

## TODO v1.2
- Auto-chain full rando
- Item ID whitelist (avoid key items softlock)
- Character stat randomization (talents)
- GUI for seed + options

## Screenshot
Working on Linux Mint 21 Cinnamon, QB64-PE 4.6.0 - Town demo with textured buildings (isometric)

## License
MIT - for preservation / randomizer community
