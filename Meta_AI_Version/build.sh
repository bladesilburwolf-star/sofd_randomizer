#!/bin/bash
# SoFD Randomizer USA PSP - ULUS10374 - Linux Mint 21 Cinnamon
# v2.0 ACCESSIBLE EDITION - Minecraft Style + Font Size 14+

set -e
echo "Building SoFD Randomizer USA PSP - ULUS10374 - ACCESSIBLE v2.0"
echo "Features: Minecraft Style, Font 14-32, High Contrast, Dark Mode"

mkdir -p build

# Find source (case-insensitive)
SRC_FILE=$(find src -iname "sofd*.java" -o -iname "psm*.java" | grep -v "(1)" | head -n 1)
if [ -z "$SRC_FILE" ]; then
  echo "No source found in src/"
  ls -la src/
  exit 1
fi

echo "Using source: $SRC_FILE"

# Compile
javac -Xlint:serial "$SRC_FILE" -d build/

if [ $? -eq 0 ]; then
  echo ""
  echo "=========================================="
  echo "Build OK - ACCESSIBLE EDITION v2.0"
  echo "Features:"
  echo "  - Minecraft Style UI (big blocky buttons)"
  echo "  - Font Size Selector: 12-32pt (14+ recommended)"
  echo "  - High Contrast Mode (yellow on black)"
  echo "  - Dark Mode (easier on eyes)"
  echo "  - Preferences saved automatically"
  echo "=========================================="
  ls build/*.class
  echo ""
  if [ -f "build/SoFDRandomizer.class" ]; then
    echo "Starting SoFDRandomizer ACCESSIBLE..."
    java -cp build SoFDRandomizer
  elif [ -f "build/PsmManTool.class" ]; then
    echo "Starting PsmManTool ACCESSIBLE..."
    java -cp build PsmManTool
  fi
else
  echo "Build failed"
  exit 1
fi
