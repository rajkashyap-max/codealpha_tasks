package com.hotel.storage;

import com.hotel.model.*;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent File I/O operations for Rooms, Bookings, and Payments.
 * Keeps data synchronized on disk across application runs.
 */
public class FileStorageManager {
    private final Path dataDirectory;
    private final Path roomsFilePath;
    private final Path bookingsFilePath;
    private final Path paymentsFilePath;

    public FileStorageManager() {
        this(Paths.get("data"));
    }

    public FileStorageManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.roomsFilePath = dataDirectory.resolve("rooms.txt");
        this.bookingsFilePath = dataDirectory.resolve("bookings.txt");
        this.paymentsFilePath = dataDirectory.resolve("payments.txt");
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            if (!Files.exists(roomsFilePath)) {
                Files.createFile(roomsFilePath);
                seedDefaultRooms();
            }

            if (!Files.exists(bookingsFilePath)) {
                Files.createFile(bookingsFilePath);
            }

            if (!Files.exists(paymentsFilePath)) {
                Files.createFile(paymentsFilePath);
            }
        } catch (IOException e) {
            System.err.println("Error initializing storage directory: " + e.getMessage());
        }
    }

    /**
     * Seeds initial hotel room inventory if rooms.txt is newly created.
     */
    private void seedDefaultRooms() {
        List<Room> defaultRooms = new ArrayList<>();
        // Standard rooms on 1st floor
        defaultRooms.add(new StandardRoom("101", 80.0, 2, false));
        defaultRooms.add(new StandardRoom("102", 80.0, 2, false));
        defaultRooms.add(new StandardRoom("103", 85.0, 2, false));
        defaultRooms.add(new StandardRoom("104", 85.0, 2, false));
        defaultRooms.add(new StandardRoom("105", 90.0, 3, false));

        // Deluxe rooms on 2nd floor
        defaultRooms.add(new DeluxeRoom("201", 150.0, 2, false));
        defaultRooms.add(new DeluxeRoom("202", 150.0, 3, false));
        defaultRooms.add(new DeluxeRoom("203", 160.0, 3, false));
        defaultRooms.add(new DeluxeRoom("204", 175.0, 4, false));

        // Suites on 3rd floor
        defaultRooms.add(new SuiteRoom("301", 300.0, 4, false));
        defaultRooms.add(new SuiteRoom("302", 320.0, 4, false));
        defaultRooms.add(new SuiteRoom("303", 350.0, 5, false));

        saveRooms(defaultRooms);
    }

    public synchronized List<Room> loadRooms() {
        List<Room> rooms = new ArrayList<>();
        if (!Files.exists(roomsFilePath)) {
            return rooms;
        }

        try (BufferedReader reader = Files.newBufferedReader(roomsFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Room room = Room.fromDataString(line);
                if (room != null) {
                    rooms.add(room);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading rooms from storage: " + e.getMessage());
        }
        return rooms;
    }

    public synchronized void saveRooms(List<Room> rooms) {
        try (BufferedWriter writer = Files.newBufferedWriter(roomsFilePath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("# Room Format: roomNumber|category|pricePerNight|capacity|isUnderMaintenance\n");
            for (Room room : rooms) {
                writer.write(room.toDataString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving rooms to storage: " + e.getMessage());
        }
    }

    public synchronized List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        if (!Files.exists(bookingsFilePath)) {
            return bookings;
        }

        try (BufferedReader reader = Files.newBufferedReader(bookingsFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Booking booking = Booking.fromDataString(line);
                if (booking != null) {
                    bookings.add(booking);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading bookings from storage: " + e.getMessage());
        }
        return bookings;
    }

    public synchronized void saveBookings(List<Booking> bookings) {
        try (BufferedWriter writer = Files.newBufferedWriter(bookingsFilePath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("# Bookings Format: bookingId^guest^roomNumber^roomCategory^checkIn^checkOut^nights^price^subtotal^tax^total^dateTime^status^payment\n");
            for (Booking booking : bookings) {
                writer.write(booking.toDataString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving bookings to storage: " + e.getMessage());
        }
    }

    public synchronized List<Payment> loadPayments() {
        List<Payment> payments = new ArrayList<>();
        if (!Files.exists(paymentsFilePath)) {
            return payments;
        }

        try (BufferedReader reader = Files.newBufferedReader(paymentsFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Payment payment = Payment.fromDataString(line);
                if (payment != null) {
                    payments.add(payment);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading payments from storage: " + e.getMessage());
        }
        return payments;
    }

    public synchronized void savePayments(List<Payment> payments) {
        try (BufferedWriter writer = Files.newBufferedWriter(paymentsFilePath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("# Payments Format: transactionId~amount~method~paymentDate~status~notes\n");
            for (Payment payment : payments) {
                writer.write(payment.toDataString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving payments to storage: " + e.getMessage());
        }
    }
}
