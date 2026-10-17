package com.scc.dao;

import com.scc.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public UserDao(){

        try(Connection conn = Database_Connection.getConnection();
            Statement stat = conn.createStatement()){
            defaultAdminGenerator(conn);
        }catch(SQLException e){
            System.err.println("Error seeding default admin: " + e.getMessage());
        }



    }


    public void defaultAdminGenerator(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        String insertSql = "INSERT INTO users (username, password, role, status) VALUES ('admin', 'admin123', 'ADMIN', 'APPROVED')";

        try (Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stat.executeUpdate(insertSql);
                System.out.println("-> Default Admin Created (Username: admin | Password: admin123)");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public static boolean registerUser(String username, String password, String role) {

        String sql = "INSERT INTO  users (username, password, role, status) VALUES (?, ?, ?, 'PENDING')";

        try(Connection conn = Database_Connection.getConnection();
            PreparedStatement pstst = conn.prepareStatement(sql)){

            pstst.setString(1, username);
            pstst.setString(2, password);
            pstst.setString(3, role.toUpperCase());
            pstst.executeUpdate();
            System.out.println("-> Successful Registration, Waiting for Approval!");
            return true;

        } catch (SQLException o) {
            System.err.println("Failed: "+ o.getMessage());
            return false;
        }
    }

    public User login(String username, String password) {
        String sql = "SELECT id, username, password, role, status " +
                "FROM users WHERE username = ? AND password = ?";

        try (Connection conn = Database_Connection.getConnection();
             PreparedStatement pstst = conn.prepareStatement(sql)) {

            pstst.setString(1, username);
            pstst.setString(2, password);

            try (ResultSet rs = pstst.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");

                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println(
                                "-> Account is pending, Waiting for Approval!"
                        );
                        return null;
                    }

                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            status
                    );

                } else {
                    System.out.println(
                            "-> Invalid username or password!"
                    );
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            return null;
        }



    }

    public List<User> getAllUsers() {
        List<User> userlist = new ArrayList<>();
        String sql = "SELECT id, username, password, role, status FROM users";

        try (Connection conn = Database_Connection.getConnection();
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery(sql)) {

            while (rs.next()) {
                userlist.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")

                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }
        return userlist;
    }

    public List<User> getPendingUsers() {
        List<User> pendingList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'PENDING'";

            try(Connection conn = Database_Connection.getConnection()){
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {

                    pendingList.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")

                    ));
                }

            } catch (SQLException e) {
                System.err.println("Error fetching all users: " + e.getMessage());
            }
            return pendingList;
    }

    public boolean approveUser(int userId) {

        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?";

        try(Connection conn = Database_Connection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, userId);
            int rowdUpdated = pstmt.executeUpdate();

            if(rowdUpdated > 0){
                System.out.println(" -> User ID: "+userId+" approved successfully!");
                return true;
            }else{
                System.out.println(" -> ID not found");
            }

        } catch (Exception e) {
            System.err.println("Error approving users: " + e.getMessage());
        }
        return false;

    }
}