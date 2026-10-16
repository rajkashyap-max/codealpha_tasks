package com.hotel;

import com.hotel.service.HotelService;
import com.hotel.ui.ConsoleUI;
import com.hotel.ui.HotelGUI;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.util.Scanner;

/**
 * Application Entry Point for Hotel Reservation System (Task 2).
 */
public class Main {
    public static void main(String[] args) {
        HotelService hotelService = new HotelService();

        // Check command-line flags
        if (args.length > 0) {
            if ("--gui".equalsIgnoreCase(args[0])) {
                launchGui(hotelService);
                return;
            } else if ("--cli".equalsIgnoreCase(args[0])) {
                launchCli(hotelService);
                return;
            }
        }

        // Check if graphics environment is available
        boolean hasGraphics = !GraphicsEnvironment.isHeadless();

        System.out.println("================================================================================");
        System.out.println("             WELCOME TO HOTEL RESERVATION SYSTEM (TASK 2)                       ");
        System.out.println("================================================================================");
        System.out.println("Select how you would like to run the application:");
        System.out.println("  1. Interactive Command Line Interface (CLI)");
        if (hasGraphics) {
            System.out.println("  2. Graphical User Interface (Desktop Swing Window)");
        }
        System.out.println("  3. Exit");
        System.out.println("================================================================================");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine().trim();

        if (hasGraphics && "2".equals(choice)) {
            launchGui(hotelService);
        } else if ("1".equals(choice)) {
            launchCli(hotelService);
        } else if ("3".equals(choice)) {
            System.out.println("Exiting application.");
        } else {
            System.out.println("Defaulting to Interactive Console Interface...");
            launchCli(hotelService);
        }
    }

    private static void launchCli(HotelService hotelService) {
        ConsoleUI consoleUI = new ConsoleUI(hotelService);
        consoleUI.start();
    }

    private static void launchGui(HotelService hotelService) {
        SwingUtilities.invokeLater(() -> {
            try {
                HotelGUI gui = new HotelGUI(hotelService);
                gui.setVisible(true);
            } catch (Exception e) {
                System.err.println("Could not open GUI window (" + e.getMessage() + "). Falling back to CLI...");
                launchCli(hotelService);
            }
        });
    }
}
