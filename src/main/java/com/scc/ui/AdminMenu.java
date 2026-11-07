package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.BookingDao;
import com.scc.dao.RoomDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.Booking;
import com.scc.model.LogAction;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final BookingDao bookingDao = new BookingDao();
    private final RoomDao roomDao = new RoomDao();
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================================");
            System.out.println("| Administrator Menu |");
            System.out.println("=================================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Manage Rooms");
            System.out.println("8. Review Pending Bookings");
            System.out.println("9. View System Audit Logs");
            System.out.println("10. Logout");
            System.out.print("Choose an option (1-10): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUserApprove();
                case "3" -> approveUser(admin);
                case "5" -> updateUser(admin);
                case "7" -> manageRooms(admin);
                case "8" -> reviewBookings(admin);
                case "9" -> viewAuditLogs(admin);
                case "10" -> {
                    logDao.logAction(admin.id(), LogAction.USER_LOGOUT,
                            "User [" + admin.username() + "] (ID: " + admin.id() + ") logged out.");
                    System.out.println(" -> Logged out.");
                    loggedIn = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid Option");
            }
        }
    }

    private void displayUserTable(List<User> users, String title) {
        System.out.println("\n================================================" + title + "================================================");
        if (users.isEmpty()) {
            System.out.println(" -> No Records Found");
            return;
        }

        String line = "+-----+-----------------+------------+------------+";
        System.out.println(line);
        System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println(line);
        for (User u : users) {
            System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n",
                    u.id(), u.username(), u.role(), u.status());
        }
        System.out.println(line);
    }

    private void viewAllUser() {
        displayUserTable(userDao.getAllUsers(), "ALL REGISTERED USER");
    }

    private void viewPendingUserApprove() {
        displayUserTable(userDao.getPendingUsers(), "PENDING APPROVALS");
    }

    private void approveUser(User admin) {
        List<User> pendingUsers = userDao.getPendingUsers();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if (pendingUsers.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter User ID to approve (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println(" -> Operation Cancelled by the user.");
            return;
        }

        boolean exists = pendingUsers.stream().anyMatch(u -> u.id() == userId);
        if (!exists) {
            System.out.println(" -> [Error] User ID not found.");
            return;
        }

        // logging happens inside UserDao.approveUser()
        userDao.approveUser(userId, admin.id());
    }

    private void updateUser(User currentAdmin) {
        List<User> users = userDao.getAllUsers();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if (users.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter selected ID to update (or 0 to cancel): ");

        if (userId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (userId == currentAdmin.id()) {
            System.out.println(" -> [WARNING] Modifying current active Admin is restricted.");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        // logging happens inside UserDao.updateUserAndStatus()
        userDao.updateUserAndStatus(userId, newRole, newStatus, currentAdmin.id());
    }



    // ---------------- Rooms ----------------

    private void manageRooms(User admin) {
        while (true) {
            System.out.println("\n=== MANAGE ROOMS ===");
            System.out.println("1. View Rooms");
            System.out.println("2. Add Room");
            System.out.println("3. Update Room Rate");
            System.out.println("4. Update Room Status");
            System.out.println("5. Remove Room");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> RoomPrinter.print(roomDao.getAllRooms());
                case "2" -> addRoom(admin);
                case "3" -> {
                    RoomPrinter.print(roomDao.getAllRooms());
                    int id = readIntInput("Enter Room ID (or 0 to cancel): ");
                    if (id != 0) {
                        double rate = readRate();
                        roomDao.updateRate(id, rate, admin.id());
                    }
                }
                case "4" -> {
                    RoomPrinter.print(roomDao.getAllRooms());
                    int id = readIntInput("Enter Room ID (or 0 to cancel): ");
                    if (id != 0) {
                        String status = readRoomStatus();
                        if (status != null) {
                            roomDao.updateStatus(id, status, admin.id());
                        }
                    }
                }
                case "5" -> {
                    RoomPrinter.print(roomDao.getAllRooms());
                    int id = readIntInput("Enter Room ID to remove (or 0 to cancel): ");
                    if (id != 0) {
                        System.out.print("Type YES to confirm removal: ");
                        if (scanner.nextLine().trim().equalsIgnoreCase("YES")) {
                            roomDao.deleteRoom(id, admin.id());
                        } else {
                            System.out.println(" -> Cancelled.");
                        }
                    }
                }
                case "0" -> { return; }
                default -> System.out.println(" -> [ERROR] Invalid Option");
            }
        }
    }

    private void addRoom(User admin) {
        System.out.print("Room number (e.g. 301): ");
        String roomNum = scanner.nextLine().trim();
        if (roomNum.isEmpty()) {
            System.out.println(" -> [ERROR] Room number cannot be empty.");
            return;
        }
        System.out.print("Room type (e.g. Single / Double): ");
        String type = scanner.nextLine().trim();
        if (type.isEmpty()) {
            System.out.println(" -> [ERROR] Room type cannot be empty.");
            return;
        }
        double rate = readRate();
        roomDao.addRoom(roomNum, type, rate, admin.id());
    }

    private double readRate() {
        while (true) {
            System.out.print("Monthly rate (PHP): ");
            try {
                double rate = Double.parseDouble(scanner.nextLine().trim());
                if (rate > 0) {
                    return rate;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println(" -> [ERROR] Enter a number greater than 0.");
        }
    }

    private String readRoomStatus() {
        while (true) {
            System.out.print("Enter Status (VACANT / OCCUPIED / MAINTENANCE) [0 to cancel]: ");
            String s = scanner.nextLine().trim().toUpperCase();
            if (s.equals("0") || s.equals("CANCEL")) {
                System.out.println(" -> Operation Cancelled.");
                return null;
            }
            if (s.equals("VACANT") || s.equals("OCCUPIED") || s.equals("MAINTENANCE")) {
                return s;
            }
            System.out.println(" -> [ERROR] Invalid status.");
        }
    }

    // ---------------- Bookings ----------------

    private void reviewBookings(User admin) {
        List<Booking> pending = bookingDao.getPendingBookings();
        if (pending.isEmpty()) {
            System.out.println(" -> No pending bookings.");
            return;
        }

        String fmt = "%-5s | %-9s | %-7s | %-12s | %-12s%n";
        System.out.println();
        System.out.printf(fmt, "ID", "Tenant ID", "Room ID", "Start", "End");
        System.out.println("-".repeat(56));
        for (Booking b : pending) {
            System.out.printf(fmt, b.id(), b.tenantId(), b.roomId(), b.startDate(), b.endDate());
        }

        int bookId = readIntInput("Enter Booking ID (or 0 to cancel): ");
        if (bookId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }
        if (pending.stream().noneMatch(b -> b.id() == bookId)) {
            System.out.println(" -> [Error] Booking ID not found in pending list.");
            return;
        }

        System.out.print("[A]pprove or [C]ancel this booking? ");
        String action = scanner.nextLine().trim().toUpperCase();
        switch (action) {
            case "A" -> bookingDao.updateBookingStatus(bookId, "APPROVED", admin.id());
            case "C" -> bookingDao.updateBookingStatus(bookId, "CANCELED", admin.id());
            default -> System.out.println(" -> Invalid choice, nothing changed.");
        }
    }

    // ---------------- Audit Logs ----------------

    private void viewAuditLogs(User admin) {
        while (true) {
            System.out.println("\n=== SYSTEM AUDIT LOGS ===");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear All Logs");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> printLogs(logDao.getAllLogs());
                case "2" -> {
                    int id = readIntInput("Enter User ID: ");
                    printLogs(logDao.getLogsByUserId(id));
                }
                case "3" -> {
                    System.out.print("Type CLEAR to permanently delete all logs: ");
                    if (scanner.nextLine().trim().equals("CLEAR")) {
                        int deleted = logDao.clearLogs();
                        System.out.println(" -> " + deleted + " log(s) deleted.");
                        // leave a trace so clearing is never silent
                        logDao.logAction(admin.id(), LogAction.LOGS_CLEARED,
                                "Admin [" + admin.id() + "] cleared " + deleted + " log entries");
                    } else {
                        System.out.println(" -> Cancelled.");
                    }
                }
                case "0" -> { return; }
                default -> System.out.println(" -> [ERROR] Invalid Option");
            }
        }
    }

    private void printLogs(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println(" -> No logs found.");
            return;
        }
        String fmt = "%-5s | %-8s | %-18s | %-35s | %-20s%n";
        System.out.println();
        System.out.printf(fmt, "ID", "User ID", "Action", "Description", "Timestamp");
        System.out.println("-".repeat(98));
        for (ActivityLog l : logs) {
            String desc = l.description();
            if (desc.length() > 35) {
                desc = desc.substring(0, 32) + "...";
            }
            System.out.printf(fmt, l.logId(), l.userId(), l.actionType(), desc, l.timestamp());
        }
    }

    // ---------------- Input helpers ----------------

    private String readValidRole() {
        while (true) {
            System.out.print("Enter Role (TENANT/ADMIN/STAFF/BILLING OFFICER) [0 to cancel]: ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("0") || role.equals("CANCEL")) {
                return null;
            }
            if (role.equals("TENANT") || role.equals("STAFF")
                    || role.equals("BILLING OFFICER") || role.equals("ADMIN")) {
                return role;
            }
            System.out.println(" -> [ERROR] Invalid Role.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter Status (APPROVED / PENDING) [0 to cancel]: ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0") || status.equals("CANCEL")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println(" -> [ERROR] Invalid Status. Must be APPROVED or PENDING.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid numeric ID.");
            }
        }
    }
}