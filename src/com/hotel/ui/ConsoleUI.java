package com.hotel.ui;

import com.hotel.model.*;
import com.hotel.service.HotelService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line interface for the Hotel Reservation System.
 */
public class ConsoleUI {
    private final HotelService hotelService;
    private final Scanner scanner;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ConsoleUI(HotelService hotelService) {
        this.hotelService = hotelService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntInput("Select an option (1-7): ", 1, 7);
            System.out.println();

            switch (choice) {
                case 1:
                    handleSearchRooms();
                    break;
                case 2:
                    handleBookRoom();
                    break;
                case 3:
                    handleViewBookingDetails();
                    break;
                case 4:
                    handleCancelReservation();
                    break;
                case 5:
                    handleManageRooms();
                    break;
                case 6:
                    handleViewTransactions();
                    break;
                case 7:
                    System.out.println("Thank you for using Grand Oasis Hotel Reservation System. Goodbye!");
                    running = false;
                    break;
            }
            if (running) {
                System.out.print("\nPress ENTER to return to the main menu...");
                safeReadLine();
            }
        }
    }

    private void printBanner() {
        System.out.println("================================================================================");
        System.out.println("                   GRAND OASIS HOTEL RESERVATION SYSTEM                         ");
        System.out.println("             OOP-Powered Hotel Booking, Payment & Room Management              ");
        System.out.println("================================================================================");
    }

    private void printMainMenu() {
        System.out.println("\n--------------------------------- MAIN MENU -----------------------------------");
        System.out.println("  1. Search & View Available Rooms");
        System.out.println("  2. Book a Room (Make Reservation & Payment)");
        System.out.println("  3. View Booking Details / Search by ID");
        System.out.println("  4. Cancel a Reservation (with Refund Simulation)");
        System.out.println("  5. Room Management (Admin: Add room, Toggle Maintenance, Update Price)");
        System.out.println("  6. View Simulated Payment & Transaction Logs");
        System.out.println("  7. Exit Application");
        System.out.println("--------------------------------------------------------------------------------");
    }

    // ==================== 1. SEARCH ROOMS ====================

    private void handleSearchRooms() {
        System.out.println("=== SEARCH & BROWSE ROOMS ===");
        System.out.println("Search Filters (press ENTER to skip any filter):");

        System.out.println("Filter by Category:");
        System.out.println("  0. All Categories");
        System.out.println("  1. Standard ($80/night)");
        System.out.println("  2. Deluxe   ($150/night)");
        System.out.println("  3. Suite    ($300/night)");
        int catChoice = readIntInput("Select category (0-3): ", 0, 3);
        RoomCategory category = null;
        if (catChoice == 1) category = RoomCategory.STANDARD;
        else if (catChoice == 2) category = RoomCategory.DELUXE;
        else if (catChoice == 3) category = RoomCategory.SUITE;

        Double maxPrice = null;
        System.out.print("Enter maximum budget per night in $ (or press ENTER to skip): ");
        String priceStr = safeReadLine();
        if (!priceStr.isEmpty()) {
            try {
                maxPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format. Skipping budget filter.");
            }
        }

        LocalDate checkIn = null;
        LocalDate checkOut = null;
        System.out.print("Filter by stay dates? (y/N): ");
        String dateFilterChoice = safeReadLine();
        if (dateFilterChoice.equalsIgnoreCase("y")) {
            checkIn = readDateInput("Enter Check-in Date (YYYY-MM-DD): ", LocalDate.now(), null);
            checkOut = readDateInput("Enter Check-out Date (YYYY-MM-DD): ", checkIn.plusDays(1), null);
        }

        List<Room> results = hotelService.searchRooms(category, maxPrice, checkIn, checkOut);

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.printf("%-10s %-12s %-12s %-10s %-30s\n", "Room No.", "Category", "Rate/Night", "Capacity", "Amenities");
        System.out.println("--------------------------------------------------------------------------------");
        if (results.isEmpty()) {
            System.out.println("No rooms match your filter criteria or dates.");
        } else {
            for (Room r : results) {
                System.out.printf("%-10s %-12s $%-11.2f %-10d %-30s\n",
                        r.getRoomNumber(),
                        r.getCategory().getDisplayName(),
                        r.getPricePerNight(),
                        r.getCapacity(),
                        r.getCategory().getAmenities().substring(0, Math.min(30, r.getCategory().getAmenities().length())) + "...");
            }
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Total matching rooms found: " + results.size());
    }

    // ==================== 2. BOOK A ROOM ====================

    private void handleBookRoom() {
        System.out.println("=== BOOK A HOTEL ROOM ===");

        LocalDate checkIn = readDateInput("Enter Check-in Date (YYYY-MM-DD): ", LocalDate.now(), null);
        LocalDate checkOut = readDateInput("Enter Check-out Date (YYYY-MM-DD): ", checkIn.plusDays(1), null);

        List<Room> availableRooms = hotelService.searchRooms(null, null, checkIn, checkOut);
        if (availableRooms.isEmpty()) {
            System.out.println("Sorry, no rooms are available for the selected dates (" + checkIn + " to " + checkOut + ").");
            return;
        }

        System.out.println("\nAvailable Rooms for " + checkIn + " to " + checkOut + ":");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-10s %-12s %-12s %-10s %-30s\n", "Room No.", "Category", "Rate/Night", "Capacity", "Features");
        System.out.println("--------------------------------------------------------------------------------");
        for (Room r : availableRooms) {
            System.out.printf("%-10s %-12s $%-11.2f %-10d %-30s\n",
                    r.getRoomNumber(), r.getCategory().getDisplayName(),
                    r.getPricePerNight(), r.getCapacity(), r.getRoomTypeDetails().substring(0, Math.min(30, r.getRoomTypeDetails().length())) + "...");
        }
        System.out.println("--------------------------------------------------------------------------------");

        Room selectedRoom = null;
        while (selectedRoom == null) {
            System.out.print("Enter Room Number to book (or 'cancel' to abort): ");
            String roomNum = safeReadLine();
            if (roomNum.equalsIgnoreCase("cancel")) {
                System.out.println("Booking cancelled by user.");
                return;
            }
            selectedRoom = hotelService.getRoomByNumber(roomNum);
            if (selectedRoom == null) {
                System.out.println("Room #" + roomNum + " does not exist. Please enter a valid room number.");
            } else if (!hotelService.isRoomAvailableForDates(roomNum, checkIn, checkOut)) {
                System.out.println("Room #" + roomNum + " is not available for those dates. Please choose another.");
                selectedRoom = null;
            }
        }

        System.out.println("\n--- GUEST INFORMATION ---");
        String name = readRequiredString("Enter Guest Full Name: ");
        String email = readRequiredString("Enter Guest Email: ");
        String phone = readRequiredString("Enter Guest Phone Number: ");
        String idProof = readRequiredString("Enter ID Proof (e.g., Passport/DL/Govt ID): ");
        Guest guest = new Guest(name, email, phone, idProof);

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double subtotal = selectedRoom.getPricePerNight() * nights;
        double tax = subtotal * Booking.TAX_RATE;
        double total = subtotal + tax;

        System.out.println("\n--- PRICE SUMMARY ---");
        System.out.printf("Room Selected   : #%s (%s)\n", selectedRoom.getRoomNumber(), selectedRoom.getCategory().getDisplayName());
        System.out.printf("Stay Duration   : %d night(s) [%s to %s]\n", nights, checkIn, checkOut);
        System.out.printf("Base Room Price : $%.2f/night\n", selectedRoom.getPricePerNight());
        System.out.printf("Subtotal        : $%.2f\n", subtotal);
        System.out.printf("Tax (12%%)       : $%.2f\n", tax);
        System.out.printf("Total Payable   : $%.2f\n", total);

        System.out.println("\n--- PAYMENT METHOD SIMULATION ---");
        System.out.println("  1. Credit Card");
        System.out.println("  2. Debit Card");
        System.out.println("  3. UPI / Instant Payment");
        System.out.println("  4. Cash at Front Desk");
        int payChoice = readIntInput("Select payment method (1-4): ", 1, 4);

        PaymentMethod method = PaymentMethod.CREDIT_CARD;
        String payDetail = "N/A";
        if (payChoice == 1 || payChoice == 2) {
            method = (payChoice == 1) ? PaymentMethod.CREDIT_CARD : PaymentMethod.DEBIT_CARD;
            System.out.print("Enter Card Number (e.g. 4532-xxxx-xxxx-8899): ");
            payDetail = safeReadLine();
            if (payDetail.isEmpty()) payDetail = "4532-1111-2222-8899";
        } else if (payChoice == 3) {
            method = PaymentMethod.UPI;
            System.out.print("Enter UPI ID (e.g. username@bank): ");
            payDetail = safeReadLine();
            if (payDetail.isEmpty()) payDetail = "guest@okbank";
        } else {
            method = PaymentMethod.CASH;
            payDetail = "Desk Cash Register";
        }

        System.out.println("\n[Simulating secure transaction gateway...]");
        try {
            Thread.sleep(800);
            Booking booking = hotelService.makeReservation(guest, selectedRoom.getRoomNumber(),
                    checkIn, checkOut, method, payDetail);

            System.out.println("\n>>> RESERVATION SUCCESSFUL! <<<");
            System.out.println(booking.generateReceipt());
        } catch (Exception e) {
            System.out.println("Error completing reservation: " + e.getMessage());
        }
    }

    // ==================== 3. VIEW BOOKING DETAILS ====================

    private void handleViewBookingDetails() {
        System.out.println("=== VIEW BOOKING DETAILS ===");
        System.out.println("  1. Lookup by Booking ID (e.g., BK-1001)");
        System.out.println("  2. Search by Guest Name / Phone / Email");
        System.out.println("  3. View All Bookings (Admin Overview)");
        int choice = readIntInput("Select option (1-3): ", 1, 3);

        switch (choice) {
            case 1:
                System.out.print("Enter Booking ID: ");
                String bId = safeReadLine();
                Booking booking = hotelService.getBookingById(bId);
                if (booking != null) {
                    System.out.println(booking.generateReceipt());
                } else {
                    System.out.println("No booking found with ID: " + bId);
                }
                break;
            case 2:
                System.out.print("Enter search keyword (Name, Phone, or Email): ");
                String query = safeReadLine();
                List<Booking> matches = hotelService.searchBookingsByGuest(query);
                if (matches.isEmpty()) {
                    System.out.println("No bookings found matching: " + query);
                } else {
                    System.out.println("\nMatching Bookings (" + matches.size() + "):");
                    for (Booking b : matches) {
                        System.out.println("------------------------------------------------------------");
                        System.out.printf("ID: %s | Room: #%s | %s to %s | Guest: %s | Status: %s | Total: $%.2f\n",
                                b.getBookingId(), b.getRoomNumber(), b.getCheckInDate(), b.getCheckOutDate(),
                                b.getGuest().getFullName(), b.getStatus(), b.getTotalAmount());
                    }
                }
                break;
            case 3:
                List<Booking> all = hotelService.getAllBookings();
                if (all.isEmpty()) {
                    System.out.println("No bookings recorded yet.");
                } else {
                    System.out.println("\nAll Hotel Bookings (" + all.size() + " total):");
                    System.out.println("--------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-10s %-15s %-8s %-12s %-12s %-8s %-12s %-12s\n",
                            "Booking ID", "Guest Name", "Room #", "Check-In", "Check-Out", "Nights", "Status", "Total Amount");
                    System.out.println("--------------------------------------------------------------------------------------------------------");
                    for (Booking b : all) {
                        System.out.printf("%-10s %-15s %-8s %-12s %-12s %-8d %-12s $%-11.2f\n",
                                b.getBookingId(),
                                b.getGuest() != null ? b.getGuest().getFullName() : "N/A",
                                b.getRoomNumber(),
                                b.getCheckInDate(),
                                b.getCheckOutDate(),
                                b.getNumberOfNights(),
                                b.getStatus().getDisplayName(),
                                b.getTotalAmount());
                    }
                    System.out.println("--------------------------------------------------------------------------------------------------------");
                }
                break;
        }
    }

    // ==================== 4. CANCEL RESERVATION ====================

    private void handleCancelReservation() {
        System.out.println("=== CANCEL RESERVATION ===");
        System.out.print("Enter Booking ID to cancel (e.g. BK-1001): ");
        String bId = safeReadLine();

        Booking booking = hotelService.getBookingById(bId);
        if (booking == null) {
            System.out.println("No booking found with ID: " + bId);
            return;
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            System.out.println("Booking " + bId + " is ALREADY cancelled.");
            return;
        }

        System.out.println("\nBooking Details Found:");
        System.out.printf("Guest: %s | Room: #%s (%s) | Dates: %s to %s | Total: $%.2f\n",
                booking.getGuest().getFullName(), booking.getRoomNumber(),
                booking.getRoomCategory().getDisplayName(),
                booking.getCheckInDate(), booking.getCheckOutDate(),
                booking.getTotalAmount());

        System.out.print("\nAre you sure you want to cancel this reservation? (y/N): ");
        String confirm = safeReadLine();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        System.out.print("Enter reason for cancellation: ");
        String reason = safeReadLine();
        if (reason.isEmpty()) reason = "Customer request";

        try {
            hotelService.cancelReservation(bId, reason);
            System.out.println("\n>>> RESERVATION CANCELLED SUCCESSFULLY <<<");
            System.out.println("Room #" + booking.getRoomNumber() + " is now marked available for dates "
                    + booking.getCheckInDate() + " to " + booking.getCheckOutDate() + ".");
            System.out.println("Simulated refund processed for amount: $" + String.format("%.2f", booking.getTotalAmount()));
            System.out.println("Payment status updated to REFUNDED.");
        } catch (Exception e) {
            System.out.println("Error cancelling reservation: " + e.getMessage());
        }
    }

    // ==================== 5. MANAGE ROOMS (ADMIN) ====================

    private void handleManageRooms() {
        System.out.println("=== ROOM INVENTORY MANAGEMENT ===");
        System.out.println("  1. List All Rooms with Status");
        System.out.println("  2. Add New Room");
        System.out.println("  3. Toggle Room Maintenance Status");
        System.out.println("  4. Update Room Price Per Night");
        int choice = readIntInput("Select option (1-4): ", 1, 4);

        switch (choice) {
            case 1:
                List<Room> all = hotelService.getAllRooms();
                System.out.println("\n--------------------------------------------------------------------------------");
                System.out.printf("%-10s %-12s %-12s %-10s %-18s\n", "Room No.", "Category", "Rate/Night", "Capacity", "Status");
                System.out.println("--------------------------------------------------------------------------------");
                for (Room r : all) {
                    System.out.printf("%-10s %-12s $%-11.2f %-10d %-18s\n",
                            r.getRoomNumber(),
                            r.getCategory().getDisplayName(),
                            r.getPricePerNight(),
                            r.getCapacity(),
                            r.isUnderMaintenance() ? "MAINTENANCE" : "AVAILABLE/ACTIVE");
                }
                System.out.println("--------------------------------------------------------------------------------");
                break;
            case 2:
                System.out.print("Enter New Room Number (e.g. 401): ");
                String roomNum = safeReadLine();
                if (hotelService.getRoomByNumber(roomNum) != null) {
                    System.out.println("Room number " + roomNum + " already exists!");
                    return;
                }
                System.out.println("Select Category:");
                System.out.println("  1. Standard");
                System.out.println("  2. Deluxe");
                System.out.println("  3. Suite");
                int c = readIntInput("Choice (1-3): ", 1, 3);
                RoomCategory cat = (c == 1) ? RoomCategory.STANDARD : (c == 2) ? RoomCategory.DELUXE : RoomCategory.SUITE;

                System.out.print("Enter Price per Night in $ [Default: $" + cat.getBasePricePerNight() + "]: ");
                String pStr = safeReadLine();
                double price = pStr.isEmpty() ? cat.getBasePricePerNight() : Double.parseDouble(pStr);

                System.out.print("Enter Max Occupancy [Default: " + cat.getMaxOccupancy() + "]: ");
                String capStr = safeReadLine();
                int cap = capStr.isEmpty() ? cat.getMaxOccupancy() : Integer.parseInt(capStr);

                Room newRoom;
                if (cat == RoomCategory.DELUXE) {
                    newRoom = new DeluxeRoom(roomNum, price, cap, false);
                } else if (cat == RoomCategory.SUITE) {
                    newRoom = new SuiteRoom(roomNum, price, cap, false);
                } else {
                    newRoom = new StandardRoom(roomNum, price, cap, false);
                }

                if (hotelService.addRoom(newRoom)) {
                    System.out.println("Room #" + roomNum + " added successfully and saved to storage.");
                } else {
                    System.out.println("Failed to add room.");
                }
                break;
            case 3:
                System.out.print("Enter Room Number to toggle maintenance: ");
                String rNum = safeReadLine();
                Room target = hotelService.getRoomByNumber(rNum);
                if (target == null) {
                    System.out.println("Room #" + rNum + " not found.");
                } else {
                    hotelService.toggleMaintenance(rNum);
                    System.out.println("Room #" + rNum + " maintenance state is now: " +
                            (target.isUnderMaintenance() ? "UNDER MAINTENANCE" : "ACTIVE / AVAILABLE"));
                }
                break;
            case 4:
                System.out.print("Enter Room Number to update price: ");
                String prNum = safeReadLine();
                Room pTarget = hotelService.getRoomByNumber(prNum);
                if (pTarget == null) {
                    System.out.println("Room #" + prNum + " not found.");
                } else {
                    System.out.print("Enter New Price per Night (Current: $" + pTarget.getPricePerNight() + "): $");
                    double np = Double.parseDouble(safeReadLine());
                    if (hotelService.updateRoomPrice(prNum, np)) {
                        System.out.println("Price updated successfully to $" + np + ".");
                    } else {
                        System.out.println("Invalid price.");
                    }
                }
                break;
        }
    }

    // ==================== 6. VIEW TRANSACTIONS ====================

    private void handleViewTransactions() {
        System.out.println("=== PAYMENT & TRANSACTION LOGS ===");
        List<Payment> payments = hotelService.getAllPayments();
        if (payments.isEmpty()) {
            System.out.println("No payment transactions recorded yet.");
            return;
        }

        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s %-10s %-16s %-12s %-20s %-25s\n",
                "Txn ID", "Amount", "Method", "Status", "Date & Time", "Notes");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for (Payment p : payments) {
            System.out.printf("%-12s $%-9.2f %-16s %-12s %-20s %-25s\n",
                    p.getTransactionId(),
                    p.getAmount(),
                    p.getMethod().getDisplayName(),
                    p.getStatus(),
                    p.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    p.getNotes().length() > 25 ? p.getNotes().substring(0, 22) + "..." : p.getNotes());
        }
        System.out.println("--------------------------------------------------------------------------------------------------------");
    }

    // ==================== HELPER METHODS ====================

    private String safeReadLine() {
        if (!scanner.hasNextLine()) {
            System.out.println("\nInput stream closed. Exiting session...");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    private int readIntInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = safeReadLine();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private LocalDate readDateInput(String prompt, LocalDate minDate, LocalDate maxDate) {
        while (true) {
            System.out.print(prompt);
            String input = safeReadLine();
            try {
                LocalDate date = LocalDate.parse(input, dateFormatter);
                if (minDate != null && date.isBefore(minDate)) {
                    System.out.println("Date cannot be earlier than " + minDate + ".");
                    continue;
                }
                if (maxDate != null && date.isAfter(maxDate)) {
                    System.out.println("Date cannot be later than " + maxDate + ".");
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD (e.g. 2026-11-01).");
            }
        }
    }

    private String readRequiredString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = safeReadLine();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty.");
        }
    }
}
