@echo off
if not exist bin\com\hotel\Main.class (
    echo Building project first...
    call build.bat
)

java -cp bin com.hotel.Main
pause
