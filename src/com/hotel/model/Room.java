package com.hotel.model;

import java.io.Serializable;

/**
 * Abstract Room class demonstrating OOP Encapsulation and Inheritance.
 * Represents a generic hotel room.
 */
public abstract class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomNumber;
    private RoomCategory category;
    private double pricePerNight;
    private int capacity;
    private boolean isUnderMaintenance;

    public Room(String roomNumber, RoomCategory category, double pricePerNight, int capacity) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.isUnderMaintenance = false;
    }

    public Room(String roomNumber, RoomCategory category, double pricePerNight, int capacity, boolean isUnderMaintenance) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.isUnderMaintenance = isUnderMaintenance;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public void setCategory(RoomCategory category) {
        this.category = category;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isUnderMaintenance() {
        return isUnderMaintenance;
    }

    public void setUnderMaintenance(boolean underMaintenance) {
        isUnderMaintenance = underMaintenance;
    }

    /**
     * Polymorphic method implemented by each specific Room subclass.
     */
    public abstract String getRoomTypeDetails();

    /**
     * Serializes room data into a standardized delimiter-separated format for File I/O.
     */
    public String toDataString() {
        return String.format("%s|%s|%.2f|%d|%b",
                roomNumber,
                category.name(),
                pricePerNight,
                capacity,
                isUnderMaintenance
        );
    }

    /**
     * Factory method parsing a serialized line and instantiating the appropriate subclass.
     */
    public static Room fromDataString(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|");
        if (parts.length < 5) return null;

        String roomNumber = parts[0].trim();
        RoomCategory cat = RoomCategory.fromString(parts[1].trim());
        double price = Double.parseDouble(parts[2].trim());
        int capacity = Integer.parseInt(parts[3].trim());
        boolean maintenance = Boolean.parseBoolean(parts[4].trim());

        if (cat == null) cat = RoomCategory.STANDARD;

        switch (cat) {
            case DELUXE:
                return new DeluxeRoom(roomNumber, price, capacity, maintenance);
            case SUITE:
                return new SuiteRoom(roomNumber, price, capacity, maintenance);
            case STANDARD:
            default:
                return new StandardRoom(roomNumber, price, capacity, maintenance);
        }
    }

    @Override
    public String toString() {
        return String.format("Room #%s [%s] - $%.2f/night (Max Guests: %d)%s",
                roomNumber,
                category.getDisplayName(),
                pricePerNight,
                capacity,
                isUnderMaintenance ? " [UNDER MAINTENANCE]" : "");
    }
}
