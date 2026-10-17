package com.scc;

import com.scc.dao.UserDao;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);

            public static void main(String[] args) {

                System.out.println("\n========================================================");
                System.out.println(" STUDENT RECORDS MANAGEMENT ");
                System.out.println("==================================");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                String choice = sc.nextLine();

                switch (choice) {
                    case "1":
                        break;
                    case "2":
                        System.out.print("Enter new username: ");
                        String username = sc.nextLine();
                        System.out.print("Enter Password: ");
                        String password = sc.nextLine();

                        System.out.print("Select Role [1] ADMIN | [2] STAFF | [3] TENANT | [4] BILLING OFFICER: ");
                        int roleChoice = sc.nextInt();
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
                                default:
                                    role = null;
                            }

                            if(role != null) {
                                UserDao.registerUser(username, password, role);

                            }else {
                                System.out.println("Invalid role selection!");
                            }
                }
            }





}