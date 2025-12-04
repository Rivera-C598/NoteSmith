@echo off
REM ========================================
REM NoteSmith - Master Build Script
REM ========================================
REM This builds everything you need for distribution
REM
REM What it does:
REM   1. Compiles Java source code
REM   2. Creates NoteSmith.jar
REM   3. Builds portable Windows app (NoteSmith.exe)
REM   4. Bundles Java runtime (students don't need Java!)
REM
REM Output: NoteSmith\ folder with NoteSmith.exe
REM ========================================

echo.
echo ========================================
echo   Building NoteSmith for Distribution
echo ========================================
echo.

REM Step 1: Build the JAR first
echo Building JAR...
call build.bat

if %errorlevel% neq 0 (
    echo JAR build failed!
    exit /b 1
)

REM Step 2: Check if jpackage is available
where jpackage >nul 2>&1
if %errorlevel% neq 0 (
    echo jpackage not found!
    echo Make sure you're using Java 17 or higher
    echo Run: java -version
    exit /b 1
)

REM Step 3: Create installer
echo Creating Windows application package...
echo.
echo Note: Creating app-image (portable app) instead of installer
echo This doesn't require WiX Toolset
echo.

jpackage ^
  --input build ^
  --name NoteSmith ^
  --main-jar NoteSmith.jar ^
  --main-class com.notesmith.Main ^
  --type app-image ^
  --icon resources\icon.ico ^
  --app-version 1.0 ^
  --vendor "Charlie Shane M. Rivera" ^
  --copyright "Copyright @2025 Charlie Shane Rivera" ^
  --description "Smart Note-Taking Application with AI Features hehe"

if %errorlevel% equ 0 (
    echo.
    echo Application package created successfully!
    echo.
    echo Package folder: NoteSmith\
    echo Inside you'll find: NoteSmith.exe
    echo.
    echo To distribute:
    echo   1. ZIP the entire NoteSmith\ folder
    echo   2. Students extract and run NoteSmith.exe
    echo   3. No Java installation required!
    echo.
    echo Want a real installer? Install WiX Toolset from:
    echo https://wixtoolset.org/
    echo.
) else (
    echo Application package creation failed!
    exit /b 1
)

pause
