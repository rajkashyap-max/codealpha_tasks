package com.hotel.model;

/**
 * Enumeration representing different hotel room categories,
 * each with standard base pricing, capacity, and luxury amenities.
 */
public enum RoomCategory {
    STANDARD("Standard", 80.0, 2, "Single/Double Bed, AC, Free High-Speed Wi-Fi, Flat-screen TV, Attached Bathroom"),
    DELUXE("Deluxe", 150.0, 3, "King Size Bed, City View Balcony, Smart TV, Mini-Bar, Coffee Maker, Premium Toiletries"),
    SUITE("Suite", 300.0, 4, "Master Bedroom, Separate Living Room, Jacuzzi, Ocean View, Complimentary Breakfast, Butler Service");

    private final String displayName;
    private final double basePricePerNight;
    private final int maxOccupancy;
    private final String amenities;

    RoomCategory(String displayName, double basePricePerNight, int maxOccupancy, String amenities) {
        this.displayName = displayName;
        this.basePricePerNight = basePricePerNight;
        this.maxOccupancy = maxOccupancy;
        this.amenities = amenities;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBasePricePerNight() {
        return basePricePerNight;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public String getAmenities() {
        return amenities;
    }

    public static RoomCategory fromString(String text) {
        if (text == null) return null;
        for (RoomCategory cat : RoomCategory.values()) {
            if (cat.name().equalsIgnoreCase(text.trim()) || cat.displayName.equalsIgnoreCase(text.trim())) {
                return cat;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName + " ($" + String.format("%.2f", basePricePerNight) + "/night, Max: " + maxOccupancy + " guests)";
    }
}
