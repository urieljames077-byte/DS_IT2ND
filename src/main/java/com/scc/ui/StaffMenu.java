package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.RoomDao;
import com.scc.model.LogAction;
import com.scc.model.User;

import java.util.Scanner;

public class StaffMenu {

    private final RoomDao roomDao = new RoomDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner scanner;

    public StaffMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void display(User staff) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================================");
            System.out.println("| Staff Menu |");
            System.out.println("=================================================");
            System.out.println("1. View Rooms");
            System.out.println("2. Update Room Status");
            System.out.println("3. Logout");
            System.out.print("Choose an option (1-3): ");

            switch (scanner.nextLine().trim()) {
                case "1" -> RoomPrinter.print(roomDao.getAllRooms());
                case "2" -> updateRoomStatus(staff);
                case "3" -> {
                    logDao.logAction(staff.id(), LogAction.USER_LOGOUT,
                            "User [" + staff.username() + "] (ID: " + staff.id() + ") logged out.");
                    System.out.println(" -> Logged out.");
                    loggedIn = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid Option");
            }
        }
    }

    private void updateRoomStatus(User staff) {
        RoomPrinter.print(roomDao.getAllRooms());
        int roomId = readInt("Enter Room ID to update (or 0 to cancel): ");
        if (roomId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }
        String status = readStatus();
        if (status == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }
        roomDao.updateStatus(roomId, status, staff.id());
    }

    private String readStatus() {
        while (true) {
            System.out.print("Enter Status (VACANT / OCCUPIED / MAINTENANCE) [0 to cancel]: ");
            String s = scanner.nextLine().trim().toUpperCase();
            if (s.equals("0") || s.equals("CANCEL")) {
                return null;
            }
            if (s.equals("VACANT") || s.equals("OCCUPIED") || s.equals("MAINTENANCE")) {
                return s;
            }
            System.out.println(" -> [ERROR] Invalid status.");
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
}