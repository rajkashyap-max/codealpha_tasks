package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Represents a complete Hotel Reservation.
 */
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double TAX_RATE = 0.12; // 12% standard hotel tax

    private String bookingId;
    private Guest guest;
    private String roomNumber;
    private RoomCategory roomCategory;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long numberOfNights;
    private double pricePerNight;
    private double subtotal;
    private double taxAmount;
    private double totalAmount;
    private LocalDateTime bookingDateTime;
    private BookingStatus status;
    private Payment payment;

    public Booking(String bookingId, Guest guest, String roomNumber, RoomCategory roomCategory,
                   LocalDate checkInDate, LocalDate checkOutDate, double pricePerNight,
                   LocalDateTime bookingDateTime, BookingStatus status, Payment payment) {
        this.bookingId = bookingId;
        this.guest = guest;
        this.roomNumber = roomNumber;
        this.roomCategory = roomCategory;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.pricePerNight = pricePerNight;
        this.numberOfNights = Math.max(1, ChronoUnit.DAYS.between(checkInDate, checkOutDate));
        this.subtotal = pricePerNight * this.numberOfNights;
        this.taxAmount = this.subtotal * TAX_RATE;
        this.totalAmount = this.subtotal + this.taxAmount;
        this.bookingDateTime = bookingDateTime;
        this.status = status;
        this.payment = payment;
    }

    public Booking(String bookingId, Guest guest, String roomNumber, RoomCategory roomCategory,
                   LocalDate checkInDate, LocalDate checkOutDate, long numberOfNights,
                   double pricePerNight, double subtotal, double taxAmount, double totalAmount,
                   LocalDateTime bookingDateTime, BookingStatus status, Payment payment) {
        this.bookingId = bookingId;
        this.guest = guest;
        this.roomNumber = roomNumber;
        this.roomCategory = roomCategory;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfNights = numberOfNights;
        this.pricePerNight = pricePerNight;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.bookingDateTime = bookingDateTime;
        this.status = status;
        this.payment = payment;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Guest getGuest() {
        return guest;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public RoomCategory getRoomCategory() {
        return roomCategory;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public long getNumberOfNights() {
        return numberOfNights;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getBookingDateTime() {
        return bookingDateTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    /**
     * Checks if this booking overlaps with a given date range.
     * Overlap occurs if requestedCheckIn < bookingCheckOut AND requestedCheckOut > bookingCheckIn.
     */
    public boolean overlapsWith(LocalDate start, LocalDate end) {
        if (status == BookingStatus.CANCELLED) {
            return false;
        }
        return start.isBefore(checkOutDate) && end.isAfter(checkInDate);
    }

    /**
     * Serializes this booking object to a single file line format.
     */
    public String toDataString() {
        String guestStr = guest != null ? guest.toDataString() : "NONE";
        String paymentStr = payment != null ? payment.toDataString() : "NONE";
        return String.format("%s^%s^%s^%s^%s^%s^%d^%.2f^%.2f^%.2f^%.2f^%s^%s^%s",
                bookingId,
                guestStr,
                roomNumber,
                roomCategory.name(),
                checkInDate.toString(),
                checkOutDate.toString(),
                numberOfNights,
                pricePerNight,
                subtotal,
                taxAmount,
                totalAmount,
                bookingDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                status.name(),
                paymentStr
        );
    }

    public static Booking fromDataString(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\^");
        if (parts.length < 14) return null;

        String id = parts[0].trim();
        Guest guest = "NONE".equals(parts[1].trim()) ? null : Guest.fromDataString(parts[1].trim());
        String roomNumber = parts[2].trim();
        RoomCategory cat = RoomCategory.valueOf(parts[3].trim());
        LocalDate checkIn = LocalDate.parse(parts[4].trim());
        LocalDate checkOut = LocalDate.parse(parts[5].trim());
        long nights = Long.parseLong(parts[6].trim());
        double price = Double.parseDouble(parts[7].trim());
        double subtotal = Double.parseDouble(parts[8].trim());
        double tax = Double.parseDouble(parts[9].trim());
        double total = Double.parseDouble(parts[10].trim());
        LocalDateTime created = LocalDateTime.parse(parts[11].trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        BookingStatus status = BookingStatus.valueOf(parts[12].trim());
        Payment payment = "NONE".equals(parts[13].trim()) ? null : Payment.fromDataString(parts[13].trim());

        return new Booking(id, guest, roomNumber, cat, checkIn, checkOut, nights, price, subtotal, tax, total, created, status, payment);
    }

    /**
     * Generates a beautifully formatted text invoice/receipt.
     */
    public String generateReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("                   GRAND OASIS HOTEL RESERVATION            \n");
        sb.append("                       BOOKING CONFIRMATION                 \n");
        sb.append("============================================================\n");
        sb.append(String.format("Booking ID       : %s\n", bookingId));
        sb.append(String.format("Booking Status   : %s\n", status.getDisplayName().toUpperCase()));
        sb.append(String.format("Date & Time      : %s\n", bookingDateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"))));
        sb.append("------------------------------------------------------------\n");
        sb.append("GUEST DETAILS:\n");
        sb.append(String.format("  Name           : %s\n", guest.getFullName()));
        sb.append(String.format("  Phone          : %s\n", guest.getPhoneNumber()));
        sb.append(String.format("  Email          : %s\n", guest.getEmail()));
        sb.append(String.format("  ID Proof       : %s\n", guest.getIdProof()));
        sb.append("------------------------------------------------------------\n");
        sb.append("STAY DETAILS:\n");
        sb.append(String.format("  Room Number    : %s\n", roomNumber));
        sb.append(String.format("  Category       : %s\n", roomCategory.getDisplayName()));
        sb.append(String.format("  Check-In Date  : %s\n", checkInDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));
        sb.append(String.format("  Check-Out Date : %s\n", checkOutDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));
        sb.append(String.format("  Total Nights   : %d night(s)\n", numberOfNights));
        sb.append("------------------------------------------------------------\n");
        sb.append("BILLING BREAKDOWN:\n");
        sb.append(String.format("  Rate / Night   : $%.2f\n", pricePerNight));
        sb.append(String.format("  Room Subtotal  : $%.2f\n", subtotal));
        sb.append(String.format("  Taxes (12%%)    : $%.2f\n", taxAmount));
        sb.append(String.format("  TOTAL PAID     : $%.2f\n", totalAmount));
        sb.append("------------------------------------------------------------\n");
        if (payment != null) {
            sb.append("PAYMENT INFORMATION:\n");
            sb.append(String.format("  Transaction ID : %s\n", payment.getTransactionId()));
            sb.append(String.format("  Payment Method : %s\n", payment.getMethod().getDisplayName()));
            sb.append(String.format("  Payment Status : %s\n", payment.getStatus()));
            sb.append(String.format("  Paid At        : %s\n", payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"))));
        }
        sb.append("============================================================\n");
        sb.append("    Thank you for choosing Grand Oasis Hotel & Suites!     \n");
        sb.append("============================================================\n");
        return sb.toString();
    }
}
