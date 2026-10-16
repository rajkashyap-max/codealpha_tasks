package com.hotel.ui;

import com.hotel.model.*;
import com.hotel.service.HotelService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Graphical User Interface for the Hotel Reservation System built with Swing.
 */
public class HotelGUI extends JFrame {
    private final HotelService hotelService;

    private JTabbedPane tabbedPane;

    // Room Search Tab components
    private JComboBox<String> searchCategoryCombo;
    private JTextField searchMaxPriceField;
    private JTextField searchCheckInField;
    private JTextField searchCheckOutField;
    private JTable roomTable;
    private DefaultTableModel roomTableModel;

    // Book Room Tab components
    private JTextField bookRoomField;
    private JTextField bookCheckInField;
    private JTextField bookCheckOutField;
    private JTextField bookNameField;
    private JTextField bookEmailField;
    private JTextField bookPhoneField;
    private JTextField bookIdProofField;
    private JComboBox<PaymentMethod> bookPaymentMethodCombo;
    private JTextField bookPaymentDetailField;
    private JLabel summaryLabel;

    // Manage Bookings Tab components
    private JTable bookingTable;
    private DefaultTableModel bookingTableModel;
    private JTextField searchBookingQueryField;

    // Room Inventory Tab components
    private JTable inventoryTable;
    private DefaultTableModel inventoryTableModel;

    public HotelGUI(HotelService hotelService) {
        super("Grand Oasis Hotel Reservation & Management System");
        this.hotelService = hotelService;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        refreshAllData();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Search & Browse Rooms", createSearchRoomsPanel());
        tabbedPane.addTab("Make Reservation", createBookRoomPanel());
        tabbedPane.addTab("Manage Bookings", createManageBookingsPanel());
        tabbedPane.addTab("Room Inventory Admin", createRoomInventoryPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==================== 1. SEARCH ROOMS PANEL ====================

    private JPanel createSearchRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        filterPanel.add(new JLabel("Category:"));
        searchCategoryCombo = new JComboBox<>(new String[]{"All Categories", "Standard", "Deluxe", "Suite"});
        filterPanel.add(searchCategoryCombo);

        filterPanel.add(new JLabel("Max $/Night:"));
        searchMaxPriceField = new JTextField(6);
        filterPanel.add(searchMaxPriceField);

        filterPanel.add(new JLabel("Check-In:"));
        searchCheckInField = new JTextField(LocalDate.now().toString(), 8);
        filterPanel.add(searchCheckInField);

        filterPanel.add(new JLabel("Check-Out:"));
        searchCheckOutField = new JTextField(LocalDate.now().plusDays(1).toString(), 8);
        filterPanel.add(searchCheckOutField);

        JButton searchBtn = new JButton("Search Available Rooms");
        searchBtn.addActionListener(e -> executeRoomSearch());
        filterPanel.add(searchBtn);

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            searchCategoryCombo.setSelectedIndex(0);
            searchMaxPriceField.setText("");
            searchCheckInField.setText(LocalDate.now().toString());
            searchCheckOutField.setText(LocalDate.now().plusDays(1).toString());
            executeRoomSearch();
        });
        filterPanel.add(resetBtn);

        panel.add(filterPanel, BorderLayout.NORTH);

        String[] cols = {"Room #", "Category", "Rate / Night", "Max Guests", "Status", "Amenities"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        roomTable = new JTable(roomTableModel);
        roomTable.setRowHeight(25);
        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton proceedToBookBtn = new JButton("Book Selected Room ->");
        proceedToBookBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        proceedToBookBtn.addActionListener(e -> {
            int row = roomTable.getSelectedRow();
            if (row >= 0) {
                String roomNum = (String) roomTableModel.getValueAt(row, 0);
                bookRoomField.setText(roomNum);
                bookCheckInField.setText(searchCheckInField.getText());
                bookCheckOutField.setText(searchCheckOutField.getText());
                calculateBookingEstimate();
                tabbedPane.setSelectedIndex(1);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a room from the table first.", "Selection Needed", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomBar.add(proceedToBookBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void executeRoomSearch() {
        try {
            String catStr = (String) searchCategoryCombo.getSelectedItem();
            RoomCategory category = null;
            if ("Standard".equalsIgnoreCase(catStr)) category = RoomCategory.STANDARD;
            else if ("Deluxe".equalsIgnoreCase(catStr)) category = RoomCategory.DELUXE;
            else if ("Suite".equalsIgnoreCase(catStr)) category = RoomCategory.SUITE;

            Double maxPrice = null;
            if (!searchMaxPriceField.getText().trim().isEmpty()) {
                maxPrice = Double.parseDouble(searchMaxPriceField.getText().trim());
            }

            LocalDate in = null;
            LocalDate out = null;
            if (!searchCheckInField.getText().trim().isEmpty() && !searchCheckOutField.getText().trim().isEmpty()) {
                in = LocalDate.parse(searchCheckInField.getText().trim());
                out = LocalDate.parse(searchCheckOutField.getText().trim());
            }

            List<Room> results = hotelService.searchRooms(category, maxPrice, in, out);
            roomTableModel.setRowCount(0);
            for (Room r : results) {
                roomTableModel.addRow(new Object[]{
                        r.getRoomNumber(),
                        r.getCategory().getDisplayName(),
                        String.format("$%.2f", r.getPricePerNight()),
                        r.getCapacity(),
                        r.isUnderMaintenance() ? "Maintenance" : "Available",
                        r.getCategory().getAmenities()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search input error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== 2. BOOK ROOM PANEL ====================

    private JPanel createBookRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formGrid = new JPanel(new GridLayout(10, 2, 10, 10));
        formGrid.setBorder(BorderFactory.createTitledBorder("Guest & Reservation Information"));

        formGrid.add(new JLabel("Room Number:"));
        bookRoomField = new JTextField("101");
        formGrid.add(bookRoomField);

        formGrid.add(new JLabel("Check-In Date (YYYY-MM-DD):"));
        bookCheckInField = new JTextField(LocalDate.now().toString());
        formGrid.add(bookCheckInField);

        formGrid.add(new JLabel("Check-Out Date (YYYY-MM-DD):"));
        bookCheckOutField = new JTextField(LocalDate.now().plusDays(1).toString());
        formGrid.add(bookCheckOutField);

        formGrid.add(new JLabel("Guest Full Name:"));
        bookNameField = new JTextField("Alex Morgan");
        formGrid.add(bookNameField);

        formGrid.add(new JLabel("Email Address:"));
        bookEmailField = new JTextField("alex.morgan@example.com");
        formGrid.add(bookEmailField);

        formGrid.add(new JLabel("Phone Number:"));
        bookPhoneField = new JTextField("+1-555-0199");
        formGrid.add(bookPhoneField);

        formGrid.add(new JLabel("ID Proof (Passport / DL):"));
        bookIdProofField = new JTextField("DL-982143");
        formGrid.add(bookIdProofField);

        formGrid.add(new JLabel("Payment Method:"));
        bookPaymentMethodCombo = new JComboBox<>(PaymentMethod.values());
        formGrid.add(bookPaymentMethodCombo);

        formGrid.add(new JLabel("Card / UPI Reference:"));
        bookPaymentDetailField = new JTextField("4532-8821-9921-3344");
        formGrid.add(bookPaymentDetailField);

        JButton calcBtn = new JButton("Calculate Total & Verify Availability");
        calcBtn.addActionListener(e -> calculateBookingEstimate());
        formGrid.add(calcBtn);

        summaryLabel = new JLabel("Click 'Calculate' to see total summary.");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formGrid.add(summaryLabel);

        panel.add(formGrid, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton bookSubmitBtn = new JButton("Process Payment & Confirm Reservation");
        bookSubmitBtn.setPreferredSize(new Dimension(300, 40));
        bookSubmitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookSubmitBtn.addActionListener(e -> submitReservation());
        bottomPanel.add(bookSubmitBtn);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void calculateBookingEstimate() {
        try {
            String roomNum = bookRoomField.getText().trim();
            Room room = hotelService.getRoomByNumber(roomNum);
            if (room == null) {
                summaryLabel.setText("Room #" + roomNum + " not found!");
                return;
            }
            LocalDate in = LocalDate.parse(bookCheckInField.getText().trim());
            LocalDate out = LocalDate.parse(bookCheckOutField.getText().trim());

            if (!out.isAfter(in)) {
                summaryLabel.setText("Check-out must be after check-in.");
                return;
            }

            boolean avail = hotelService.isRoomAvailableForDates(roomNum, in, out);
            long nights = ChronoUnit.DAYS.between(in, out);
            double subtotal = room.getPricePerNight() * nights;
            double tax = subtotal * Booking.TAX_RATE;
            double total = subtotal + tax;

            if (avail) {
                summaryLabel.setText(String.format("Available! %d night(s) | Total: $%.2f (incl. tax)", nights, total));
            } else {
                summaryLabel.setText("ROOM UNAVAILABLE for these dates!");
            }
        } catch (Exception ex) {
            summaryLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void submitReservation() {
        try {
            String roomNum = bookRoomField.getText().trim();
            LocalDate in = LocalDate.parse(bookCheckInField.getText().trim());
            LocalDate out = LocalDate.parse(bookCheckOutField.getText().trim());
            String name = bookNameField.getText().trim();
            String email = bookEmailField.getText().trim();
            String phone = bookPhoneField.getText().trim();
            String idProof = bookIdProofField.getText().trim();
            PaymentMethod method = (PaymentMethod) bookPaymentMethodCombo.getSelectedItem();
            String detail = bookPaymentDetailField.getText().trim();

            Guest guest = new Guest(name, email, phone, idProof);

            Booking booking = hotelService.makeReservation(guest, roomNum, in, out, method, detail);

            // Show receipt dialog
            JTextArea receiptArea = new JTextArea(booking.generateReceipt());
            receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            receiptArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(receiptArea);
            scrollPane.setPreferredSize(new Dimension(550, 420));

            JOptionPane.showMessageDialog(this, scrollPane, "Reservation Confirmation & Receipt", JOptionPane.INFORMATION_MESSAGE);

            refreshAllData();
            tabbedPane.setSelectedIndex(2);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== 3. MANAGE BOOKINGS PANEL ====================

    private JPanel createManageBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.add(new JLabel("Search Bookings:"));
        searchBookingQueryField = new JTextField(15);
        topBar.add(searchBookingQueryField);

        JButton filterBtn = new JButton("Filter");
        filterBtn.addActionListener(e -> filterBookingsTable());
        topBar.add(filterBtn);

        JButton refreshBtn = new JButton("Show All Bookings");
        refreshBtn.addActionListener(e -> {
            searchBookingQueryField.setText("");
            refreshBookingsTable();
        });
        topBar.add(refreshBtn);

        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Guest Name", "Phone", "Room #", "Category", "Check-In", "Check-Out", "Total Paid", "Status"};
        bookingTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(25);
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        JButton viewReceiptBtn = new JButton("View Full Receipt");
        viewReceiptBtn.addActionListener(e -> {
            int row = bookingTable.getSelectedRow();
            if (row >= 0) {
                String bId = (String) bookingTableModel.getValueAt(row, 0);
                Booking b = hotelService.getBookingById(bId);
                if (b != null) {
                    JTextArea receiptArea = new JTextArea(b.generateReceipt());
                    receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    receiptArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(receiptArea);
                    scrollPane.setPreferredSize(new Dimension(550, 420));
                    JOptionPane.showMessageDialog(this, scrollPane, "Booking Receipt: " + bId, JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a booking from the table first.", "Info", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomBar.add(viewReceiptBtn);

        JButton cancelBtn = new JButton("Cancel Selected Booking");
        cancelBtn.setForeground(Color.RED);
        cancelBtn.addActionListener(e -> {
            int row = bookingTable.getSelectedRow();
            if (row >= 0) {
                String bId = (String) bookingTableModel.getValueAt(row, 0);
                Booking b = hotelService.getBookingById(bId);
                if (b == null) return;
                if (b.getStatus() == BookingStatus.CANCELLED) {
                    JOptionPane.showMessageDialog(this, "Booking is already cancelled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to cancel booking " + bId + "?\nSimulated refund of $" + String.format("%.2f", b.getTotalAmount()) + " will be issued.",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        hotelService.cancelReservation(bId, "Customer GUI Cancellation");
                        JOptionPane.showMessageDialog(this, "Booking cancelled successfully and room released!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshAllData();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error cancelling: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a booking to cancel.", "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomBar.add(cancelBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshBookingsTable() {
        bookingTableModel.setRowCount(0);
        for (Booking b : hotelService.getAllBookings()) {
            bookingTableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getGuest() != null ? b.getGuest().getFullName() : "N/A",
                    b.getGuest() != null ? b.getGuest().getPhoneNumber() : "N/A",
                    b.getRoomNumber(),
                    b.getRoomCategory().getDisplayName(),
                    b.getCheckInDate(),
                    b.getCheckOutDate(),
                    String.format("$%.2f", b.getTotalAmount()),
                    b.getStatus().getDisplayName()
            });
        }
    }

    private void filterBookingsTable() {
        String q = searchBookingQueryField.getText().trim();
        if (q.isEmpty()) {
            refreshBookingsTable();
            return;
        }
        bookingTableModel.setRowCount(0);
        for (Booking b : hotelService.searchBookingsByGuest(q)) {
            bookingTableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getGuest().getFullName(),
                    b.getGuest().getPhoneNumber(),
                    b.getRoomNumber(),
                    b.getRoomCategory().getDisplayName(),
                    b.getCheckInDate(),
                    b.getCheckOutDate(),
                    String.format("$%.2f", b.getTotalAmount()),
                    b.getStatus().getDisplayName()
            });
        }
    }

    // ==================== 4. ROOM INVENTORY PANEL ====================

    private JPanel createRoomInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"Room Number", "Category", "Rate / Night", "Max Guests", "Status"};
        inventoryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setRowHeight(25);
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        JButton toggleMaintBtn = new JButton("Toggle Maintenance Status");
        toggleMaintBtn.addActionListener(e -> {
            int row = inventoryTable.getSelectedRow();
            if (row >= 0) {
                String roomNum = (String) inventoryTableModel.getValueAt(row, 0);
                hotelService.toggleMaintenance(roomNum);
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, "Select a room from the inventory table.", "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomBar.add(toggleMaintBtn);

        JButton addRoomBtn = new JButton("+ Add New Room");
        addRoomBtn.addActionListener(e -> showAddRoomDialog());
        bottomBar.add(addRoomBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);
        return panel;
    }

    private void showAddRoomDialog() {
        JTextField numField = new JTextField();
        JComboBox<RoomCategory> catCombo = new JComboBox<>(RoomCategory.values());
        JTextField priceField = new JTextField("120.00");
        JTextField capField = new JTextField("2");

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("Room Number:"));
        form.add(numField);
        form.add(new JLabel("Category:"));
        form.add(catCombo);
        form.add(new JLabel("Price Per Night ($):"));
        form.add(priceField);
        form.add(new JLabel("Max Capacity:"));
        form.add(capField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Hotel Room", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String num = numField.getText().trim();
                RoomCategory cat = (RoomCategory) catCombo.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());
                int cap = Integer.parseInt(capField.getText().trim());

                Room newRoom;
                if (cat == RoomCategory.DELUXE) {
                    newRoom = new DeluxeRoom(num, price, cap, false);
                } else if (cat == RoomCategory.SUITE) {
                    newRoom = new SuiteRoom(num, price, cap, false);
                } else {
                    newRoom = new StandardRoom(num, price, cap, false);
                }

                if (hotelService.addRoom(newRoom)) {
                    JOptionPane.showMessageDialog(this, "Room #" + num + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllData();
                } else {
                    JOptionPane.showMessageDialog(this, "Room #" + num + " already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid room data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshInventoryTable() {
        inventoryTableModel.setRowCount(0);
        for (Room r : hotelService.getAllRooms()) {
            inventoryTableModel.addRow(new Object[]{
                    r.getRoomNumber(),
                    r.getCategory().getDisplayName(),
                    String.format("$%.2f", r.getPricePerNight()),
                    r.getCapacity(),
                    r.isUnderMaintenance() ? "UNDER MAINTENANCE" : "ACTIVE / AVAILABLE"
            });
        }
    }

    public void refreshAllData() {
        executeRoomSearch();
        refreshBookingsTable();
        refreshInventoryTable();
    }
}
