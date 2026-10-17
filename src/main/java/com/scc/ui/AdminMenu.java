package com.scc.ui;

import com.scc.dao.Database_Connection;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;

    }


    public void display(User admin) {
        boolean loggedIn = true;
        while(loggedIn) {
            System.out.println("\n=================================================");
            System.out.println("| Administrator Menu |");
            System.out.println("=================================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration]");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch(choice) {
                case "1" -> viewAllUser();
                default -> System.out.println(" -> [ERROR] Invalid Option");

            }
        }
    }

    private void displayUserTable(List<User> users, String title){
        System.out.println("\n================================================" +title+ "================================================");
        if(users.isEmpty()){
            System.out.println(" -> No Records Found");
            return;
        }

        System.out.println("+--------+------------------+--------------+--------------+");
        System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println("+------+----------------+------------+--------------------+");
            for(User u : users){
                System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n",
                        u.id(), u.username(), u.role(), u.status());
            }
        System.out.println("+--------+------------------+--------------+--------------+");




    }
    private void viewAllUser(){
        displayUserTable(userDao.getAllUsers(), "ALL REGISTERED USER");

    }
}
