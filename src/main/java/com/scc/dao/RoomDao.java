package com.scc.dao;

import com.scc.model.LogAction;
import com.scc.model.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {

    private final ActivityLogDao logDao = new ActivityLogDao();

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id, room_num, room_type, rate, status FROM Room ORDER BY room_num";
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(toRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rooms: " + e.getMessage());
        }
        return rooms;
    }

    public Room findById(int roomId) {
        String sql = "SELECT id, room_num, room_type, rate, status FROM Room WHERE id = ?";
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toRoom(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding room: " + e.getMessage());
        }
        return null;
    }

    public boolean addRoom(String roomNum, String roomType, double rate, int actorId) {
        String sql = "INSERT INTO Room (room_num, room_type, rate, status) VALUES (?, ?, ?, 'VACANT')";
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNum);
            ps.setString(2, roomType);
            ps.setDouble(3, rate);
            ps.executeUpdate();
            System.out.println(" -> Room " + roomNum + " added successfully!");
            logDao.logAction(actorId, LogAction.ROOM_ADDED,
                    "User [" + actorId + "] added Room " + roomNum + " (" + roomType
                            + ", PHP " + String.format("%.2f", rate) + ")");
            return true;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toUpperCase().contains("UNIQUE")) {
                System.out.println(" -> [Error] Room number " + roomNum + " already exists.");
            } else {
                System.err.println("Error adding room: " + e.getMessage());
            }
            return false;
        }
    }

    public boolean updateRate(int roomId, double newRate, int actorId) {
        Room room = findById(roomId);
        if (room == null) {
            System.out.println(" -> [Error] Room ID not found.");
            return false;
        }
        String sql = "UPDATE Room SET rate = ? WHERE id = ?";
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newRate);
            ps.setInt(2, roomId);
            if (ps.executeUpdate() > 0) {
                System.out.println(" -> Room " + room.roomNum() + " rate updated successfully!");
                logDao.logAction(actorId, LogAction.ROOM_UPDATED,
                        "User [" + actorId + "] changed Room " + room.roomNum() + " rate from "
                                + String.format("%.2f", room.rate()) + " to " + String.format("%.2f", newRate));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating room rate: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int roomId, String newStatus, int actorId) {
        newStatus = newStatus.toUpperCase();
        Room room = findById(roomId);
        if (room == null) {
            System.out.println(" -> [Error] Room ID not found.");
            return false;
        }
        String sql = "UPDATE Room SET status = ? WHERE id = ?";
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, roomId);
            if (ps.executeUpdate() > 0) {
                System.out.println(" -> Room " + room.roomNum() + " status updated successfully!");
                logDao.logAction(actorId, LogAction.ROOM_UPDATED,
                        "Updated Room " + room.roomNum() + " status from " + room.status() + " to " + newStatus);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating room status: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteRoom(int roomId, int actorId) {
        Room room = findById(roomId);
        if (room == null) {
            System.out.println(" -> [Error] Room ID not found.");
            return false;
        }
        String sql = "DELETE FROM Room WHERE id = ?";
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            if (ps.executeUpdate() > 0) {
                System.out.println(" -> Room " + room.roomNum() + " removed.");
                logDao.logAction(actorId, LogAction.ROOM_DELETED,
                        "User [" + actorId + "] removed Room " + room.roomNum());
                return true;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toUpperCase().contains("FOREIGN KEY")) {
                System.out.println(" -> [Error] Room " + room.roomNum()
                        + " has bookings and cannot be removed. Set it to MAINTENANCE instead.");
            } else {
                System.err.println("Error removing room: " + e.getMessage());
            }
        }
        return false;
    }

    private Room toRoom(ResultSet rs) throws SQLException {
        return new Room(rs.getInt("id"), rs.getString("room_num"),
                rs.getString("room_type"), rs.getDouble("rate"), rs.getString("status"));
    }
}