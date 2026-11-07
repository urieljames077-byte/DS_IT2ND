package com.scc.ui;

import com.scc.model.Room;

import java.util.List;

final class RoomPrinter {
    private RoomPrinter() {}

    static void print(List<Room> rooms) {
        if (rooms.isEmpty()) {
            System.out.println(" -> No rooms found.");
            return;
        }
        String fmt = "%-5s | %-8s | %-10s | %-10s | %-12s%n";
        System.out.println();
        System.out.printf(fmt, "ID", "Room No", "Type", "Rate", "Status");
        System.out.println("-".repeat(56));
        for (Room r : rooms) {
            System.out.printf("%-5s | %-8s | %-10s | %-10.2f | %-12s%n",
                    r.id(), r.roomNum(), r.roomType(), r.rate(), r.status());
        }
    }
}