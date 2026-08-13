#!/bin/bash
# Fix for untracked files conflict shown in screenshot
# error: The following untracked working tree files would be overwritten by merge: README.md build.sh

echo "Fixing git merge conflict..."
cd ~/sofd_randomizer

# Save your local work
echo "Backing up local README.md and build.sh..."
cp README.md README.md.local.bak 2>/dev/null || true
cp build.sh build.sh.local.bak 2>/dev/null || true

# Remove problematic files
rm -rf src/SoFDRandomizer\ (1).java
rm -rf src/*\(1\)*
rm -rf build/

# Option 1: Stash and pull (recommended)
echo "Stashing local changes and pulling..."
git stash push -m "local changes backup" --keep-index
git pull origin main

# Restore your improved versions
echo "Restoring improved build.sh..."
cat > build.sh << 'EOF'
#!/bin/bash
# SoFD Randomizer USA PSP - ULUS10374 - Linux Mint 21 Cinnamon
# Fixed for case sensitivity + both class names
set -e
echo "Building SoFD Randomizer USA PSP - ULUS10374"

mkdir -p build

# Find actual java file (case-insensitive)
SRC_FILE=$(find src -iname "sofd*.java" -o -iname "psm*.java" | head -n 1)
if [ -z "$SRC_FILE" ]; then
  echo "No source found in src/"
  ls -la src/
  exit 1
fi

echo "Using source: $SRC_FILE"

javac -Xlint:serial "$SRC_FILE" -d build/

if [ $? -eq 0 ]; then
  echo ""
  echo "Build OK"
  ls build/*.class
  echo ""
  if [ -f "build/SoFDRandomizer.class" ]; then
    echo "Starting SoFDRandomizer..."
    java -cp build SoFDRandomizer
  elif [ -f "build/PsmManTool.class" ]; then
    echo "Starting PsmManTool..."
    java -cp build PsmManTool
  fi
else
  echo "Build failed"
  exit 1
fi
EOF

chmod +x build.sh
echo "Fixed! Now run ./build.sh"
