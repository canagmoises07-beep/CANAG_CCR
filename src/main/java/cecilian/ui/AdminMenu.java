package cecilian.ui;

import cecilian.dao.UserDAO;
import cecilian.model.User;
import cecilian.service.AuthService;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    public static void showMenu(User admin, UserDAO userDAO, AuthService authService, Scanner scanner) {
        while (true) {
            System.out.println("\n==================================");
            System.out.println(" ADMINISTRATOR DASHBOARD");
            System.out.println("==================================");
            System.out.println("Welcome, " + admin.fullName() + "!");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAllUsers(userDAO);
                    break;
                case "2":
                    viewPendingRegistrations(userDAO);
                    break;
                case "3":
                    approveUserAccount(admin, userDAO, authService, scanner);
                    break;
                case "4":
                    authService.logout(admin);
                    System.out.println("You have been logged out.");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Formatted Console Output helper using string formatters
    private static void printUserTable(List<User> users) {
        String format = "| %-5s | %-15s | %-15s | %-12s | %-10s |\n";
        String border = "+-------+-----------------+-----------------+--------------+------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "Username", "Full Name", "Role", "Status");
        System.out.println(border);

        for (User u : users) {
            System.out.printf(format,
                    u.userId(),
                    u.username(),
                    u.fullName(),
                    u.role(),
                    u.status()
            );
        }
        System.out.println(border);
    }

    private static void viewAllUsers(UserDAO userDAO) {
        try {
            List<User> users = userDAO.getAllUsers();
            System.out.println("\n--- ALL SYSTEM USERS ---");
            if (users.isEmpty()) {
                System.out.println("No users found in the system.");
                return;
            }
            printUserTable(users);
        } catch (Exception e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
    }

    private static void viewPendingRegistrations(UserDAO userDAO) {
        try {
            List<User> pendingUsers = userDAO.getPendingUsers();
            System.out.println("\n--- PENDING REGISTRATIONS ---");
            if (pendingUsers.isEmpty()) {
                System.out.println("No pending accounts found.");
                return;
            }
            printUserTable(pendingUsers);
        } catch (Exception e) {
            System.out.println("Error fetching pending registrations: " + e.getMessage());
        }
    }

    private static void approveUserAccount(User admin, UserDAO userDAO, AuthService authService, Scanner scanner) {
        try {
            List<User> pendingUsers = userDAO.getPendingUsers();
            System.out.println("\n--- APPROVE USER ACCOUNT ---");

            if (pendingUsers.isEmpty()) {
                System.out.println("No pending accounts to approve.");
                return;
            }

            printUserTable(pendingUsers);

            System.out.print("\nEnter the User ID to approve (0 to cancel): ");
            String input = scanner.nextLine();

            int targetId = Integer.parseInt(input);
            if (targetId == 0) {
                System.out.println("Approval action cancelled.");
                return;
            }

            // Verify ID exists in the pending list
            boolean existsInPending = pendingUsers.stream().anyMatch(u -> u.userId() == targetId);

            if (!existsInPending) {
                System.out.println("Error: User ID not found in the pending registrations list.");
                return;
            }

            boolean approved = userDAO.approveUser(targetId);
            if (approved) {
                userDAO.logAction(
                        admin.userId(),
                        "ACCOUNT_APPROVED",
                        "Approved account ID: " + targetId
                );
                System.out.println("Success: User account ID " + targetId + " status updated to APPROVED!");
            } else {
                System.out.println("Approval failed. Please check the User ID.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid numeric User ID.");
        } catch (Exception e) {
            System.out.println("Database error during approval: " + e.getMessage());
        }
    }
}