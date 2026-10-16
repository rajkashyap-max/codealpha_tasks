package com.hotel.service;

import com.hotel.model.*;
import com.hotel.storage.FileStorageManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business logic service managing rooms, reservations, availability,
 * and persistence.
 */
public class HotelService {
    private final FileStorageManager storageManager;
    private final PaymentService paymentService;

    private final List<Room> rooms;
    private final List<Booking> bookings;
    private final List<Payment> payments;

    private final Random random = new Random();

    public HotelService() {
        this.storageManager = new FileStorageManager();
        this.paymentService = new PaymentService();
        this.rooms = new ArrayList<>(storageManager.loadRooms());
        this.bookings = new ArrayList<>(storageManager.loadBookings());
        this.payments = new ArrayList<>(storageManager.loadPayments());
    }

    public HotelService(FileStorageManager storageManager, PaymentService paymentService) {
        this.storageManager = storageManager;
        this.paymentService = paymentService;
        this.rooms = new ArrayList<>(storageManager.loadRooms());
        this.bookings = new ArrayList<>(storageManager.loadBookings());
        this.payments = new ArrayList<>(storageManager.loadPayments());
    }

    // ==================== ROOM SEARCH & MANAGEMENT ====================

    public synchronized List<Room> getAllRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public synchronized Room getRoomByNumber(String roomNumber) {
        for (Room r : rooms) {
            if (r.getRoomNumber().equalsIgnoreCase(roomNumber.trim())) {
                return r;
            }
        }
        return null;
    }

    /**
     * Searches for rooms matching optional category, max price, and availability dates.
     */
    public synchronized List<Room> searchRooms(RoomCategory category, Double maxPrice,
                                               LocalDate checkIn, LocalDate checkOut) {
        return rooms.stream()
                .filter(r -> !r.isUnderMaintenance())
                .filter(r -> category == null || r.getCategory() == category)
                .filter(r -> maxPrice == null || r.getPricePerNight() <= maxPrice)
                .filter(r -> {
                    if (checkIn != null && checkOut != null) {
                        return isRoomAvailableForDates(r.getRoomNumber(), checkIn, checkOut);
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Room::getPricePerNight))
                .collect(Collectors.toList());
    }

    /**
     * Checks whether a room is free from overlapping active bookings during [checkIn, checkOut).
     */
    public synchronized boolean isRoomAvailableForDates(String roomNumber, LocalDate checkIn, LocalDate checkOut) {
        Room r = getRoomByNumber(roomNumber);
        if (r == null || r.isUnderMaintenance()) {
            return false;
        }

        for (Booking b : bookings) {
            if (b.getRoomNumber().equalsIgnoreCase(roomNumber) && b.getStatus() != BookingStatus.CANCELLED) {
                if (b.overlapsWith(checkIn, checkOut)) {
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized boolean addRoom(Room room) {
        if (room == null || getRoomByNumber(room.getRoomNumber()) != null) {
            return false;
        }
        rooms.add(room);
        storageManager.saveRooms(rooms);
        return true;
    }

    public synchronized boolean toggleMaintenance(String roomNumber) {
        Room room = getRoomByNumber(roomNumber);
        if (room == null) return false;
        room.setUnderMaintenance(!room.isUnderMaintenance());
        storageManager.saveRooms(rooms);
        return true;
    }

    public synchronized boolean updateRoomPrice(String roomNumber, double newPrice) {
        Room room = getRoomByNumber(roomNumber);
        if (room == null || newPrice <= 0) return false;
        room.setPricePerNight(newPrice);
        storageManager.saveRooms(rooms);
        return true;
    }

    // ==================== RESERVATION / BOOKING ====================

    /**
     * Creates a new booking, processes simulated payment, and updates storage.
     */
    public synchronized Booking makeReservation(Guest guest, String roomNumber,
                                                LocalDate checkIn, LocalDate checkOut,
                                                PaymentMethod paymentMethod, String paymentDetail) throws Exception {
        if (guest == null) throw new IllegalArgumentException("Guest details cannot be null.");
        if (roomNumber == null || roomNumber.trim().isEmpty()) throw new IllegalArgumentException("Room number is required.");
        if (checkIn == null || checkOut == null) throw new IllegalArgumentException("Valid check-in and check-out dates are required.");
        if (!checkOut.isAfter(checkIn)) throw new IllegalArgumentException("Check-out date must be strictly after check-in date.");
        if (checkIn.isBefore(LocalDate.now())) throw new IllegalArgumentException("Check-in date cannot be in the past.");

        Room room = getRoomByNumber(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room #" + roomNumber + " does not exist.");
        }
        if (room.isUnderMaintenance()) {
            throw new IllegalStateException("Room #" + roomNumber + " is currently under maintenance.");
        }
        if (!isRoomAvailableForDates(roomNumber, checkIn, checkOut)) {
            throw new IllegalStateException("Room #" + roomNumber + " is already booked for the selected dates.");
        }

        String bookingId = generateUniqueBookingId();
        double pricePerNight = room.getPricePerNight();

        // Calculate initial booking to determine total amount for payment
        Booking tempBooking = new Booking(bookingId, guest, room.getRoomNumber(), room.getCategory(),
                checkIn, checkOut, pricePerNight, LocalDateTime.now(), BookingStatus.CONFIRMED, null);

        // Process payment simulation
        Payment payment = paymentService.processPayment(tempBooking.getTotalAmount(), paymentMethod, paymentDetail);
        tempBooking.setPayment(payment);

        // Save
        bookings.add(tempBooking);
        payments.add(payment);

        storageManager.saveBookings(bookings);
        storageManager.savePayments(payments);

        return tempBooking;
    }

    /**
     * Cancels an existing booking and processes a simulated refund.
     */
    public synchronized boolean cancelReservation(String bookingId, String reason) throws Exception {
        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking ID " + bookingId + " not found.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking " + bookingId + " is already cancelled.");
        }

        // Process refund
        Payment origPayment = booking.getPayment();
        if (origPayment != null && origPayment.getStatus() == PaymentStatus.COMPLETED) {
            double refundAmount = booking.getTotalAmount();
            // Optional: 10% fee if check-in is today
            if (booking.getCheckInDate().isEqual(LocalDate.now())) {
                refundAmount = booking.getTotalAmount() * 0.90;
            }
            Payment refundPayment = paymentService.processRefund(origPayment, refundAmount, reason != null ? reason : "Guest requested cancellation");
            payments.add(refundPayment);
            storageManager.savePayments(payments);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        storageManager.saveBookings(bookings);
        return true;
    }

    // ==================== BOOKING DETAILS & VIEWS ====================

    public synchronized Booking getBookingById(String bookingId) {
        if (bookingId == null) return null;
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(bookingId.trim())) {
                return b;
            }
        }
        return null;
    }

    public synchronized List<Booking> searchBookingsByGuest(String query) {
        if (query == null || query.trim().isEmpty()) return Collections.emptyList();
        String q = query.trim().toLowerCase();
        return bookings.stream()
                .filter(b -> b.getGuest() != null &&
                        (b.getGuest().getFullName().toLowerCase().contains(q) ||
                         b.getGuest().getPhoneNumber().contains(q) ||
                         b.getGuest().getEmail().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    public synchronized List<Booking> getAllBookings() {
        return Collections.unmodifiableList(bookings);
    }

    public synchronized List<Payment> getAllPayments() {
        return Collections.unmodifiableList(payments);
    }

    private String generateUniqueBookingId() {
        String id;
        do {
            int num = 1000 + random.nextInt(9000);
            id = "BK-" + num;
        } while (getBookingById(id) != null);
        return id;
    }
}
