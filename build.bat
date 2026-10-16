@echo off
echo ===================================================
echo   Compiling and Packaging Hotel Reservation System (Task 2)
echo ===================================================

if not exist bin mkdir bin
if not exist data mkdir data

echo Compiling Java source files...
javac -d bin -sourcepath src src\com\hotel\Main.java src\com\hotel\test\HotelSystemTest.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation successful!
    echo Packaging executable HotelReservationSystem.jar...
    jar --create --file HotelReservationSystem.jar --main-class com.hotel.Main -C bin com
    if %ERRORLEVEL% equ 0 (
        echo [SUCCESS] Executable JAR created: HotelReservationSystem.jar
    )
) else (
    echo [ERROR] Compilation failed.
)
pause
