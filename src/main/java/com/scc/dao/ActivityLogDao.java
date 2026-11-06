package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public void logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            // a logging failure must never crash the main action
            System.err.println("Warning: failed to write activity log: " + e.getMessage());
        }
    }

    public List<ActivityLog> getAllLogs() {
        return query("SELECT id, user_id, action_type, description, timestamp "
                + "FROM activity_logs ORDER BY timestamp DESC, id DESC", null);
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        return query("SELECT id, user_id, action_type, description, timestamp "
                + "FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, id DESC", userId);
    }

    public int clearLogs() {
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM activity_logs")) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing logs: " + e.getMessage());
            return 0;
        }
    }

    private List<ActivityLog> query(String sql, Integer userId) {
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ActivityLog(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("action_type"),
                            rs.getString("description"),
                            rs.getString("timestamp")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading logs: " + e.getMessage());
        }
        return logs;
    }
}