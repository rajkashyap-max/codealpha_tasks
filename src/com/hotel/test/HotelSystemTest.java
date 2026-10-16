package com.hotel.test;

import com.hotel.model.*;
import com.hotel.service.HotelService;
import com.hotel.service.PaymentService;
import com.hotel.storage.FileStorageManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Automated test suite verifying all core requirements:
 * 1. Room categorization & search
 * 2. Booking creation & double-booking prevention
 * 3. Payment simulation & receipt generation
 * 4. Cancellation & refund processing
 * 5. File I/O persistence
 */
public class HotelSystemTest {
    public static void main(String[] args) {
        System.out.println(">>> RUNNING HOTEL RESERVATION SYSTEM AUTOMATED TESTS <<<\n");

        Path testDataDir = Paths.get("data_test");
        cleanDirectory(testDataDir);

        try {
            FileStorageManager storage = new FileStorageManager(testDataDir);
            PaymentService paymentService = new PaymentService();
            HotelService hotel = new HotelService(storage, paymentService);

            // Test 1: Check initial seeded rooms
            List<Room> allRooms = hotel.getAllRooms();
            assertTrue(allRooms.size() >= 10, "Expected at least 10 seeded rooms, got: " + allRooms.size());
            System.out.println("[PASS] Initial seeded rooms loaded successfully (" + allRooms.size() + " rooms).");

            // Test 2: Room categorization
            List<Room> standardRooms = hotel.searchRooms(RoomCategory.STANDARD, null, null, null);
            List<Room> deluxeRooms = hotel.searchRooms(RoomCategory.DELUXE, null, null, null);
            List<Room> suiteRooms = hotel.searchRooms(RoomCategory.SUITE, null, null, null);
            assertTrue(!standardRooms.isEmpty(), "Standard rooms should exist");
            assertTrue(!deluxeRooms.isEmpty(), "Deluxe rooms should exist");
            assertTrue(!suiteRooms.isEmpty(), "Suite rooms should exist");
            System.out.println("[PASS] Room categorization verified (Standard: " + standardRooms.size()
                    + ", Deluxe: " + deluxeRooms.size() + ", Suite: " + suiteRooms.size() + ").");

            // Test 3: Budget search
            List<Room> affordable = hotel.searchRooms(null, 100.0, null, null);
            for (Room r : affordable) {
                assertTrue(r.getPricePerNight() <= 100.0, "Room price should be <= $100");
            }
            System.out.println("[PASS] Budget search filter verified.");

            // Test 4: Make a booking & Payment simulation
            LocalDate checkIn = LocalDate.now().plusDays(2);
            LocalDate checkOut = LocalDate.now().plusDays(5);
            Guest guest = new Guest("John Doe", "john@example.com", "+1-555-4321", "PASSPORT-7788");

            Booking booking = hotel.makeReservation(guest, "101", checkIn, checkOut,
                    PaymentMethod.CREDIT_CARD, "4532-1111-2222-3333");

            assertTrue(booking != null, "Booking should not be null");
            assertTrue(booking.getBookingId().startsWith("BK-"), "Booking ID should start with BK-");
            assertTrue(booking.getPayment() != null, "Payment should be generated");
            assertTrue(booking.getPayment().getStatus() == PaymentStatus.COMPLETED, "Payment status should be COMPLETED");
            assertTrue(booking.getNumberOfNights() == 3, "Nights should be 3");
            System.out.println("[PASS] Booking created: " + booking.getBookingId() + " with Payment TXN: " + booking.getPayment().getTransactionId());

            // Test 5: Verify double-booking prevention
            boolean isAvailable = hotel.isRoomAvailableForDates("101", checkIn, checkOut);
            assertTrue(!isAvailable, "Room 101 should NOT be available for overlapping dates");

            boolean doubleBookFailed = false;
            try {
                hotel.makeReservation(new Guest("Jane Smith", "jane@example.com", "555-1234", "ID-2"),
                        "101", checkIn, checkOut, PaymentMethod.UPI, "jane@upi");
            } catch (Exception ex) {
                doubleBookFailed = true;
            }
            assertTrue(doubleBookFailed, "Double booking same room on overlapping dates MUST fail");
            System.out.println("[PASS] Double-booking prevention verified.");

            // Test 6: Verify Receipt Generation
            String receipt = booking.generateReceipt();
            assertTrue(receipt.contains("GRAND OASIS HOTEL RESERVATION"), "Receipt must contain header");
            assertTrue(receipt.contains("John Doe"), "Receipt must contain guest name");
            assertTrue(receipt.contains(booking.getBookingId()), "Receipt must contain booking ID");
            System.out.println("[PASS] Formatted invoice receipt generation verified.");

            // Test 7: Cancellation & Refund simulation
            hotel.cancelReservation(booking.getBookingId(), "Travel plans changed");
            Booking updatedBooking = hotel.getBookingById(booking.getBookingId());
            assertTrue(updatedBooking.getStatus() == BookingStatus.CANCELLED, "Booking status must be CANCELLED");

            // Verify room is available again after cancellation
            boolean availableAfterCancel = hotel.isRoomAvailableForDates("101", checkIn, checkOut);
            assertTrue(availableAfterCancel, "Room 101 should be available again after cancellation");
            System.out.println("[PASS] Cancellation and refund simulation verified.");

            // Test 8: Data Persistence Reloading Test
            // Create a second hotel service reading from the same directory to verify disk persistence
            HotelService reloadedHotel = new HotelService(new FileStorageManager(testDataDir), new PaymentService());
            Booking reloadedBooking = reloadedHotel.getBookingById(booking.getBookingId());
            assertTrue(reloadedBooking != null, "Reloaded booking must exist on disk");
            assertTrue(reloadedBooking.getStatus() == BookingStatus.CANCELLED, "Persisted status must match");
            System.out.println("[PASS] File I/O persistence across reloads verified.");

            System.out.println("\n>>> ALL TESTS PASSED SUCCESSFULLY! (8/8) <<<");
        } catch (Exception e) {
            System.err.println("TEST FAILED WITH EXCEPTION:");
            e.printStackTrace();
            System.exit(1);
        } finally {
            cleanDirectory(testDataDir);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("ASSERTION FAILED: " + message);
        }
    }

    private static void cleanDirectory(Path dir) {
        if (!Files.exists(dir)) return;
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception ignored) {}
    }
}
