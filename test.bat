@echo off
if not exist bin\com\hotel\test\HotelSystemTest.class (
    echo Building project first...
    call build.bat
)

java -cp bin com.hotel.test.HotelSystemTest
pause
