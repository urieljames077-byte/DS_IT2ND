package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.LogAction;
import com.scc.model.User;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;

    }


    public void start(){

        System.out.println("\n========================================================");
        System.out.println(" DORM SPACE MANAGEMENT ");
        System.out.println("========================================================");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                System.out.print("Enter new username: ");
                String username = sc.nextLine();
                System.out.print("Enter Password: ");
                String password = sc.nextLine();

                System.out.print("Select Role [1] ADMIN | [2] STAFF | [3] TENANT | [4] BILLING OFFICER: ");
                int roleChoice;
                try {
                    roleChoice = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    roleChoice = 0;
                }

                String role;

                switch (roleChoice) {
                    case 1:
                        role = "ADMIN";
                        break;
                    case 2:
                        role = "STAFF";
                        break;
                    case 3:
                        role = "TENANT";
                        break;
                    case 4:
                        role = "BILLING OFFICER";
                        break;
                    default:
                        role = null;
                }

                if(role != null) {
                    userDao.registerUser(username, password, role);

                }else {
                    System.out.println("Invalid role selection!");
                }
        }
    }

    private void handleLogin(){
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        User user = userDao.login(username, password);

        if(user != null) {
            logDao.logAction(user.id(), LogAction.USER_LOGIN,
                    "User [" + user.username() + "] (ID: " + user.id() + ") logged in.");
            System.out.println("\n -> Welcome "+user.username()+" | ROLE: "+user.role()+"");
            switch (user.role()) {
                case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
                case "TENANT" -> new TenantMenu(sc).display(user);
                case "STAFF" -> new StaffMenu(sc).display(user);
                default -> System.out.println("Account role not found!");
            }
        }
    }

}