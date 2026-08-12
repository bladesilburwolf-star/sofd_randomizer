@echo off
echo Compiling scan tool...
javac -encoding UTF-8 com\serifsystemworks\sofd\tools\SlzCodec.java com\serifsystemworks\sofd\tools\PackScanTool.java
if errorlevel 1 (
    echo Compile failed - check you have a JDK installed and on PATH.
    pause
    exit /b 1
)
echo.
if "%~1"=="" (
    echo Usage: RUN_SCAN.bat "C:\path\to\so1pack.bin"
    echo   or drag-and-drop your so1pack.bin onto this .bat file
    pause
    exit /b 1
)
java -Xmx2g com.serifsystemworks.sofd.tools.PackScanTool "%~1"
echo.
echo Report written to pack_scan_report.txt in this folder.
pause
