@echo off
if not exist HotelReservationSystem.jar (
    echo Building project first...
    call build.bat
)

java -jar HotelReservationSystem.jar --gui
