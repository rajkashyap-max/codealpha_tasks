package com.hotel.model;

/**
 * Suite Room subclass offering supreme luxury and executive services.
 */
public class SuiteRoom extends Room {
    private static final long serialVersionUID = 1L;

    public SuiteRoom(String roomNumber) {
        super(roomNumber, RoomCategory.SUITE, RoomCategory.SUITE.getBasePricePerNight(), RoomCategory.SUITE.getMaxOccupancy());
    }

    public SuiteRoom(String roomNumber, double price, int capacity, boolean isUnderMaintenance) {
        super(roomNumber, RoomCategory.SUITE, price, capacity, isUnderMaintenance);
    }

    @Override
    public String getRoomTypeDetails() {
        return "Opulent presidential suite with master bedroom, private jacuzzi, separate living room, and dedicated butler service.";
    }
}
