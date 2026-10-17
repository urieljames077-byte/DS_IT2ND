package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database_Connection {
    private static final String DB_URL = "jdbc:sqlite:Dorm.db";

    public static Connection getConnection() throws SQLException{
        Connection conn = DriverManager.getConnection(DB_URL);

        try(Statement stat = conn.createStatement()){
            stat.execute("PRAGMA foreign_keys=ON");
        }
        return conn;
    }
}
