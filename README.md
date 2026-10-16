# Hotel Reservation System (`task_1`)

A robust, enterprise-grade Hotel Reservation and Management System developed in **Java** following strict **Object-Oriented Programming (OOP)** principles and **File I/O Persistence**.

This project fulfills all requirements for the **Hotel Reservation System**:
- 🔍 **Search, Book, and Manage Hotel Rooms** (filters by category, budget, and stay dates)
- 🛏️ **Room Categorization** (`Standard`, `Deluxe`, `Suite` with specific amenities, pricing, and occupancy)
- 📅 **Make and Cancel Reservations** (prevents double-booking for overlapping dates, dynamic availability)
- 💳 **Payment Simulation & Booking Details View** (supports Credit/Debit Card, UPI, Cash, auto-generates invoice receipts, and simulated refunds)
- 💾 **OOP + File I/O Storage** (persists rooms, bookings, and payment records into `data/` directory so state is preserved across runs)
- 🖥️ **Dual Interface Support**: Interactive **Terminal / CLI** interface and modern **Desktop Swing GUI**!

---

## 🏗️ Project Architecture & OOP Principles

- **Encapsulation**: Private class attributes with verified getters, setters, and business invariant validations (`Booking`, `Guest`, `Payment`, `Room`).
- **Inheritance & Polymorphism**:
  - `Room` (Base abstract class)
    - `StandardRoom` (Specialized features & base pricing)
    - `DeluxeRoom` (Upgraded balcony, mini-bar, smart TV)
    - `SuiteRoom` (Presidential layout, jacuzzi, butler service)
  - Polymorphic descriptions (`getRoomTypeDetails()`) and dynamic instantiation via factory methods.
- **Abstraction**: Clear separation between Data Access (`FileStorageManager`), Business Logic (`HotelService`), Payment Gateway (`PaymentService`), and Presentation (`ConsoleUI` & `HotelGUI`).
- **Data Persistence**: File-based flat-file database using formatted records stored in `data/rooms.txt`, `data/bookings.txt`, and `data/payments.txt`.

---

## 📁 Directory Structure

```
task_1/
├── src/
│   └── com/
│       └── hotel/
│           ├── Main.java                     # Application entry point (CLI/GUI launcher)
│           ├── model/
│           │   ├── RoomCategory.java         # Standard, Deluxe, Suite enum
│           │   ├── Room.java                 # Abstract base Room class
│           │   ├── StandardRoom.java         # Standard room subclass
│           │   ├── DeluxeRoom.java           # Deluxe room subclass
│           │   ├── SuiteRoom.java            # Suite room subclass
│           │   ├── Guest.java                # Guest contact and ID model
│           │   ├── Booking.java              # Reservation entity & receipt generator
│           │   ├── BookingStatus.java        # Confirmed, Cancelled, etc.
│           │   ├── Payment.java              # Transaction details
│           │   ├── PaymentMethod.java        # Credit, Debit, UPI, Cash
│           │   └── PaymentStatus.java        # Completed, Refunded, Pending
│           ├── storage/
│           │   └── FileStorageManager.java   # File I/O persistence engine
│           ├── service/
│           │   ├── HotelService.java         # Search, Booking, Cancellation core logic
│           │   └── PaymentService.java       # Gateway simulation & refund logic
│           ├── ui/
│           │   ├── ConsoleUI.java            # Interactive command-line terminal UI
│           │   └── HotelGUI.java             # Modern Java Swing desktop window
│           └── test/
│               └── HotelSystemTest.java      # Automated verification test suite
├── data/
│   ├── rooms.txt                             # Persistent room inventory
│   ├── bookings.txt                          # Saved reservations
│   └── payments.txt                          # Payment transaction records
├── bin/                                      # Compiled .class binaries
├── build.bat                                 # 1-click Windows compilation script
├── run.bat                                   # 1-click Windows launcher (CLI & GUI choice)
├── run-gui.bat                               # 1-click direct Desktop GUI launcher
├── test.bat                                  # 1-click automated test runner
└── README.md                                 # Documentation
```

---

## 🚀 How to Run the Project

### Prerequisites
- **Java SE Development Kit (JDK 8 or higher)** installed (Java 26 verified).

### Quick Launch on Windows
You can simply double-click any of the batch files:
- **`run.bat`**: Launches the main menu where you can choose CLI or GUI.
- **`run-gui.bat`**: Directly opens the Desktop Graphical User Interface (Swing).
- **`test.bat`**: Runs the 8/8 automated test suite.

### Manual Terminal Commands (PowerShell / Command Prompt)

1. **Compile all files:**
   ```powershell
   javac -d bin -sourcepath src src/com/hotel/Main.java src/com/hotel/test/HotelSystemTest.java
   ```

2. **Run in Interactive Terminal CLI mode:**
   ```powershell
   java -cp bin com.hotel.Main --cli
   ```

3. **Run in Desktop Swing GUI mode:**
   ```powershell
   java -cp bin com.hotel.Main --gui
   ```

4. **Run the Automated Test Suite:**
   ```powershell
   java -cp bin com.hotel.test.HotelSystemTest
   ```

---

## 📋 Features Overview

### 1. Room Search & Filtering
- Filter by category (`Standard`, `Deluxe`, `Suite`).
- Filter by maximum nightly budget in `$`.
- Filter by specific check-in and check-out dates to show only rooms that are free and not under maintenance.

### 2. Making a Reservation
- Select available room and enter stay duration (`YYYY-MM-DD`).
- Validates that check-out is after check-in and dates are not in the past.
- Calculates subtotal, 12% hotel tax, and final payable amount.
- Collects guest contact details (Name, Phone, Email, ID Proof).
- Simulates payment gateway authorization across **Credit Card**, **Debit Card**, **UPI**, and **Cash**.
- Generates a formatted ASCII invoice receipt with unique Booking ID (`BK-XXXX`) and Transaction ID (`TXN-XXXXXX`).

### 3. Preventing Double-Booking
- Automatically validates date intervals against all active bookings using interval overlap logic:
  $$\text{Overlap} \iff (\text{RequestedCheckIn} < \text{ExistingCheckOut}) \land (\text{RequestedCheckOut} > \text{ExistingCheckIn})$$

### 4. Cancellation & Refund Simulation
- Users can cancel existing bookings by entering their `Booking ID`.
- Calculates simulated refund based on cancellation timing.
- Updates payment status to `REFUNDED` and records refund transaction.
- Instantly releases room availability so other guests can book it.

### 5. Room Management (Admin)
- View inventory status of all rooms.
- Add new rooms with custom categories, occupancy, and rates.
- Toggle room maintenance mode (takes room off search without deleting).
- Update room pricing dynamically.

---

## 🧾 Sample Receipt Output

```text
============================================================
                   GRAND OASIS HOTEL RESERVATION            
                       BOOKING CONFIRMATION                 
============================================================
Booking ID       : BK-3477
Booking Status   : CONFIRMED
Date & Time      : 17-Oct-2026 00:09:58
------------------------------------------------------------
GUEST DETAILS:
  Name           : John Doe
  Phone          : +1-555-4321
  Email          : john@example.com
  ID Proof       : PASSPORT-7788
------------------------------------------------------------
STAY DETAILS:
  Room Number    : 101
  Category       : Standard
  Check-In Date  : 19-Oct-2026
  Check-Out Date : 22-Oct-2026
  Total Nights   : 3 night(s)
------------------------------------------------------------
BILLING BREAKDOWN:
  Rate / Night   : $80.00
  Room Subtotal  : $240.00
  Taxes (12%)    : $28.80
  TOTAL PAID     : $268.80
------------------------------------------------------------
PAYMENT INFORMATION:
  Transaction ID : TXN-334139
  Payment Method : Credit Card
  Payment Status : COMPLETED
  Paid At        : 17-Oct-2026 00:09:58
============================================================
    Thank you for choosing Grand Oasis Hotel & Suites!     
============================================================
```
