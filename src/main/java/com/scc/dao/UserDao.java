package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDao {

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

}
