#!/bin/bash
cd "$(dirname "$0")"
echo "Compiling..."
javac -encoding UTF-8 \
  com/serifsystemworks/sofd/models/*.java \
  com/serifsystemworks/sofd/io/*.java \
  com/serifsystemworks/sofd/randomizer/*.java \
  com/serifsystemworks/sofd/tools/*.java \
  com/serifsystemworks/sofd/ui/*.java
if [ $? -ne 0 ]; then
  echo "Compile failed."
  exit 1
fi
echo "Starting editor..."
# Override with SOFD_RAM_MB=2048 ./run.sh if a larger source binary needs it.
java -Xmx"${SOFD_RAM_MB:-1536}"m com.serifsystemworks.sofd.ui.MainFrame
