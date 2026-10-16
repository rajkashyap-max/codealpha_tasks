package com.hotel.model;

/**
 * Deluxe Room subclass with upgraded amenities and spacious layout.
 */
public class DeluxeRoom extends Room {
    private static final long serialVersionUID = 1L;

    public DeluxeRoom(String roomNumber) {
        super(roomNumber, RoomCategory.DELUXE, RoomCategory.DELUXE.getBasePricePerNight(), RoomCategory.DELUXE.getMaxOccupancy());
    }

    public DeluxeRoom(String roomNumber, double price, int capacity, boolean isUnderMaintenance) {
        super(roomNumber, RoomCategory.DELUXE, price, capacity, isUnderMaintenance);
    }

    @Override
    public String getRoomTypeDetails() {
        return "Spacious deluxe room featuring a scenic balcony, king bed, smart 4K entertainment, and stocked mini-bar.";
    }
}
