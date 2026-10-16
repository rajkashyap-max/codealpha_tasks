@echo off
echo ===================================================
echo   Compiling Hotel Reservation System (Task 1)...
echo ===================================================

if not exist bin mkdir bin
if not exist data mkdir data

javac -d bin -sourcepath src src\com\hotel\Main.java src\com\hotel\test\HotelSystemTest.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation successful! Classes compiled to bin/
) else (
    echo [ERROR] Compilation failed.
)
pause
