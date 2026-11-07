package com.scc.dao;

import com.scc.model.Booking;
import com.scc.model.LogAction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    private final ActivityLogDao logDao = new ActivityLogDao();

    /** Core business process: a tenant submits a booking request. Returns the new booking id, or -1. */
    public int createBooking(int tenantUserId, int roomId, String startDate, String endDate) {
        String sql = "INSERT INTO Booking (tenant_id, room_id, start_date, end_date, booking_status) "
                + "VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tenantUserId);
            ps.setInt(2, roomId);
            ps.setString(3, startDate);
            ps.setString(4, endDate);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int bookId = keys.getInt(1);
                    logDao.logAction(tenantUserId, LogAction.BOOKING_CREATED,
                            "User [" + tenantUserId + "] requested Room ID " + roomId
                                    + " (Booking #" + bookId + ") " + startDate + " to " + endDate);
                    return bookId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating booking: " + e.getMessage());
        }
        return -1;
    }

    /** Lifecycle change: PENDING -> APPROVED / CANCELED. */
    public boolean updateBookingStatus(int bookId, String newStatus, int actorUserId) {
        newStatus = newStatus.toUpperCase();
        String selectSql = "SELECT booking_status FROM Booking WHERE id = ?";
        String updateSql = "UPDATE Booking SET booking_status = ? WHERE id = ?";

        try (Connection conn = Database_Connection.getConnection()) {
            String oldStatus;
            try (PreparedStatement sel = conn.prepareStatement(selectSql)) {
                sel.setInt(1, bookId);
                try (ResultSet rs = sel.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println(" -> Booking ID not found.");
                        return false;
                    }
                    oldStatus = rs.getString("booking_status");
                }
            }

            try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                upd.setString(1, newStatus);
                upd.setInt(2, bookId);
                if (upd.executeUpdate() > 0) {
                    String type = switch (newStatus) {
                        case "APPROVED" -> LogAction.BOOKING_APPROVED;
                        case "CANCELED" -> LogAction.BOOKING_CANCELED;
                        default -> "BOOKING_UPDATED";
                    };
                    logDao.logAction(actorUserId, type,
                            "Updated Booking #" + bookId + " status from " + oldStatus + " to " + newStatus);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating booking: " + e.getMessage());
        }
        return false;
    }

    public List<Booking> getPendingBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT id, tenant_id, room_id, start_date, end_date, booking_status "
                + "FROM Booking WHERE booking_status = 'PENDING' ORDER BY id";
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Booking(rs.getInt("id"), rs.getInt("tenant_id"), rs.getInt("room_id"),
                        rs.getString("start_date"), rs.getString("end_date"), rs.getString("booking_status")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
        }
        return list;
    }
}