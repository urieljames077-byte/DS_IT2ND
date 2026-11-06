package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        try (Connection conn = Database_Connection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    status TEXT NOT NULL
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id     INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp   DATETIME DEFAULT (datetime('now', 'localtime')),
                    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Room (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_num  TEXT UNIQUE NOT NULL,
                    room_type TEXT NOT NULL,
                    rate      REAL NOT NULL,
                    status    TEXT NOT NULL DEFAULT 'VACANT'
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Booking (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    tenant_id      INTEGER NOT NULL,
                    room_id        INTEGER NOT NULL,
                    start_date     TEXT NOT NULL,
                    end_date       TEXT NOT NULL,
                    booking_status TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK (booking_status IN ('PENDING','APPROVED','CANCELED')),
                    FOREIGN KEY (tenant_id) REFERENCES Users(id) ON DELETE CASCADE,
                    FOREIGN KEY (room_id)   REFERENCES Room(id)
                )
                """);

            // sample rooms so bookings can be demonstrated
            stmt.execute("INSERT OR IGNORE INTO Room (room_num, room_type, rate) VALUES ('101', 'Single', 2500)");
            stmt.execute("INSERT OR IGNORE INTO Room (room_num, room_type, rate) VALUES ('102', 'Double', 4000)");
            stmt.execute("INSERT OR IGNORE INTO Room (room_num, room_type, rate) VALUES ('201', 'Single', 2800)");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }

        new UserDao(); // seeds default admin; must run after tables exist
        System.out.println("-> Database tables verified.");
    }
}