#!/bin/bash
# SoFD USA PSP Randomizer - Build script for Linux Mint 21
echo "Building SoFD Randomizer USA PSP - ULUS10374"
javac -Xlint src/SoFDRandomizer.java -d build/
if [ $? -eq 0 ]; then
  echo "Build OK - Run with: java -cp build SoFDRandomizer"
  java -cp build SoFDRandomizer
else
  echo "Build failed"
  exit 1
fi
