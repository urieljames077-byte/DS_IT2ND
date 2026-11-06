package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.BookingDao;
import com.scc.model.LogAction;
import com.scc.model.Room;
import com.scc.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class TenantMenu {

    private final BookingDao bookingDao = new BookingDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner scanner;

    public TenantMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void display(User tenant) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================================");
            System.out.println("| Tenant Menu |");
            System.out.println("=================================================");
            System.out.println("1. View Rooms");
            System.out.println("2. Request Room Booking");
            System.out.println("3. Logout");
            System.out.print("Choose an option (1-3): ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewRooms();
                case "2" -> requestBooking(tenant);
                case "3" -> {
                    logDao.logAction(tenant.id(), LogAction.USER_LOGOUT,
                            "User [" + tenant.username() + "] (ID: " + tenant.id() + ") logged out.");
                    System.out.println(" -> Logged out.");
                    loggedIn = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid Option");
            }
        }
    }

    private void viewRooms() {
        List<Room> rooms = bookingDao.getAllRooms();
        if (rooms.isEmpty()) {
            System.out.println(" -> No rooms found.");
            return;
        }
        String fmt = "%-5s | %-8s | %-10s | %-10s | %-12s%n";
        System.out.println();
        System.out.printf(fmt, "ID", "Room No", "Type", "Rate", "Status");
        System.out.println("-".repeat(56));
        for (Room r : rooms) {
            System.out.printf(fmt, r.id(), r.roomNum(), r.roomType(), r.rate(), r.status());
        }
    }

    private void requestBooking(User tenant) {
        viewRooms();
        int roomId = readInt("Enter Room ID to book (or 0 to cancel): ");
        if (roomId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }
        if (bookingDao.getAllRooms().stream().noneMatch(r -> r.id() == roomId)) {
            System.out.println(" -> [Error] Room ID not found.");
            return;
        }

        LocalDate start = readDate("Move-in date (yyyy-MM-dd): ");
        LocalDate end = readDate("Move-out date (yyyy-MM-dd): ");
        if (!end.isAfter(start)) {
            System.out.println(" -> [Error] Move-out date must be after move-in date.");
            return;
        }

        int bookId = bookingDao.createBooking(tenant.id(), roomId, start.toString(), end.toString());
        if (bookId > 0) {
            System.out.println(" -> Booking request #" + bookId + " submitted. Waiting for approval!");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid number.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println(" -> [ERROR] Use the format yyyy-MM-dd, e.g. 2026-11-15.");
            }
        }
    }
}