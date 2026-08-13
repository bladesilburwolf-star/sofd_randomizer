@echo off
title SOFD Randomizer Build System
echo Building Star Ocean: First Departure Randomizer...
echo ===================================================

:: 1. Create bin output folder if missing
if not exist "bin" mkdir bin

:: 2. Collect all Java source files dynamically across all subdirectories
dir /s /b src\*.java > sources.txt

:: 3. Compile all source files and log errors if compilation fails
javac -d bin @sources.txt 2> build_error.log

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed! Error log saved to build_error.log.
    del sources.txt
    pause
    exit /b %errorlevel%
)

:: Clean up log if compile succeeded
if exist build_error.log del build_error.log

:: 4. Clean up source list file
del sources.txt

echo.
echo Build Successful! Launching application...
echo ===================================================

:: 5. Launch the application with runtime log redirect option
java -cp bin com.serifsystemworks.sofd.ui.MainFrame 2>> run_error.log

pause