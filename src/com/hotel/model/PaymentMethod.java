package com.hotel.model;

/**
 * Payment methods supported in the hotel reservation simulation.
 */
public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    UPI("UPI / QR Payment"),
    CASH("Cash at Desk");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromString(String text) {
        if (text == null) return null;
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.name().equalsIgnoreCase(text.trim()) || method.displayName.equalsIgnoreCase(text.trim())) {
                return method;
            }
        }
        return null;
    }
}
