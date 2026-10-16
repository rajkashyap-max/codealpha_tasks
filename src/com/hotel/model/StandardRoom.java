package com.hotel.model;

/**
 * Standard Room subclass implementing specific room details.
 */
public class StandardRoom extends Room {
    private static final long serialVersionUID = 1L;

    public StandardRoom(String roomNumber) {
        super(roomNumber, RoomCategory.STANDARD, RoomCategory.STANDARD.getBasePricePerNight(), RoomCategory.STANDARD.getMaxOccupancy());
    }

    public StandardRoom(String roomNumber, double price, int capacity, boolean isUnderMaintenance) {
        super(roomNumber, RoomCategory.STANDARD, price, capacity, isUnderMaintenance);
    }

    @Override
    public String getRoomTypeDetails() {
        return "Cozy room equipped with comfortable bedding, workstation, air conditioning, and high-speed Wi-Fi.";
    }
}
