# PSP Projects Focus - TODO

## Current Focus: Star Ocean First Departure USA PSP (ULUS10374)
- Repo: bladesilburwolf-star/sofd_randomizer
- Status: v1.1 - Unpack, Scan, Randomize Chests/Shops/Enemies/Drops, Repack
- Platform: Linux Mint 21 Cinnamon, QB64-PE not needed, Java 17+

### Next Steps PSP (Hard)
1. Test unpack on real ULUS10374 USRDIR/DATA.BIN
2. Identify actual chest table offsets (FIELD/*.BIN)
3. Build item ID whitelist to avoid softlock (key items)
4. Auto-chain full randomizer with single seed
5. UMDGen repack testing on PSP emulator (PPSSPP)

### Other PSP Projects (backlog)
- Final Fantasy Type-0 - Assigned to Grok+Gemini (big randomizer)
- Other PSP RPGs use same offset table technique

### Back Burner: Darkstone
- Darkstone PSX PSM unpacker done (PsmManTool v8.x)
- Demake v5 phone-fixed working on Mint QB64-PE (isometric town)
- Controls need mouse + keyboard improvement
- Paused until PSP projects stable

### Notes
- PSP is tricky: BE vs LE, encrypted EBOOT.BIN, nested archives
- Linux Mint 21 is primary dev environment now (not phone)
- Build system must be case-sensitive safe
