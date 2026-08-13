# SOFD binary research

`BinaryResearchTool` is a **read-only** companion for locating and validating data before any randomizer UI writes to it. It accepts decimal values or `0x` hexadecimal values.

The app's **Research Tools** tab provides the same functions without a terminal, including a per-run helper memory limit. The main app uses 1536 MB by default; launch with `SOFD_RAM_MB=2048 ./run.sh` to raise it (or choose a smaller value if your computer needs it).

Compile the project, then run these from this folder:

```bash
java com.serifsystemworks.sofd.tools.BinaryResearchTool dump slz_decompressed/slz_decompressed_0x4246800.bin 0x3EA00 0x180
java com.serifsystemworks.sofd.tools.BinaryResearchTool strings slz_decompressed/slz_decompressed_0x4246800.bin "Muah Castle"
java com.serifsystemworks.sofd.tools.BinaryResearchTool names so1pack.bin 0x60 0x4C 0x14 0x1BAE850 0x1BAE9D0
java com.serifsystemworks.sofd.tools.BinaryResearchTool refs16 slz_decompressed/slz_decompressed_0x4246800.bin 0x0018
java com.serifsystemworks.sofd.tools.BinaryResearchTool runs16 slz_decompressed/slz_decompressed_0x4246800.bin 0x0200 6
```

Suggested workflow:

1. Use `strings` to anchor a town/zone name and `dump` its surrounding data.
2. Use `runs16` over the surrounding region. Groups of small values are useful encounter-table leads, but are not proof by themselves.
3. After discovering an ID with an in-game test, use `refs16` to list every use of that ID and compare nearby fields across locations.
4. Use `names` to validate fixed-size named-record layouts. The known enemy layout is stride `0x60`, name field `0x4C`, length `0x14`; its initial known anchor is `0x1BAE850`. Supply that anchor as the optional `start` parameter instead of assuming records are aligned from byte zero.

Do not enable a UI randomizer for encounters, drops, equipment, party, or skills until its table layout is confirmed through this workflow. Their current offsets are placeholders.

## Unaligned SLZ containers

The initial scan checked only 0x800-byte sector boundaries. That misses valid containers such as the resource header at `0x1BA08F8`, so use the following read-only helper when following a lead inside `so1pack.bin`:

```bash
java com.serifsystemworks.sofd.tools.SlzResearchTool locate so1pack.bin 0x1BA0000 0x1BB0000
java com.serifsystemworks.sofd.tools.SlzResearchTool extract so1pack.bin 0x1BA08F8 slz_research/enemy_area.bin
```
