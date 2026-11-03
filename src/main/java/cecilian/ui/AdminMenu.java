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
            System.out.println("4. Update User Role & Status");
            System.out.println("5. Logout");
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
                    updateUserRoleAndStatus(admin, userDAO, scanner);
                    break;
                case "5":
                    authService.logout(admin);
                    System.out.println("You have been logged out.");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

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

            int targetId = readIntInput("Enter the User ID to approve (0 to cancel): ", scanner);
            if (targetId == 0) {
                System.out.println("Approval action cancelled.");
                return;
            }

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

        } catch (Exception e) {
            System.out.println("Database error during approval: " + e.getMessage());
        }
    }

    private static void updateUserRoleAndStatus(User admin, UserDAO userDAO, Scanner scanner) {
        try {
            List<User> users = userDAO.getAllUsers();
            System.out.println("\n--- UPDATE USER ROLE & STATUS ---");
            if (users.isEmpty()) {
                System.out.println("No users found in the system.");
                return;
            }

            printUserTable(users);

            int targetId = readIntInput("Enter the User ID to update (0 to cancel): ", scanner);
            if (targetId == 0) {
                System.out.println("Update action cancelled.");
                return;
            }

            // Self-Edit Restriction: Block admins from changing their own active account
            if (targetId == admin.userId()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                return;
            }

            // Verify target user exists
            User targetUser = users.stream().filter(u -> u.userId() == targetId).findFirst().orElse(null);
            if (targetUser == null) {
                System.out.println("Error: User ID not found.");
                return;
            }

            System.out.println("Selected User: " + targetUser.username() + " | Current Role: " + targetUser.role() + " | Status: " + targetUser.status());

            String newRole = readValidRole(scanner);
            if (newRole.equals("CANCEL")) {
                System.out.println("Update action cancelled.");
                return;
            }

            String newStatus = readValidStatus(scanner);
            if (newStatus.equals("CANCEL")) {
                System.out.println("Update action cancelled.");
                return;
            }

            boolean updated = userDAO.updateUserRoleAndStatus(targetId, newRole, newStatus);
            if (updated) {
                userDAO.logAction(
                        admin.userId(),
                        "ROLE_STATUS_UPDATED",
                        "Updated user ID " + targetId + " to Role: " + newRole + ", Status: " + newStatus
                );
                System.out.println("Success: User account ID " + targetId + " successfully updated!");
            } else {
                System.out.println("Update failed. Please try again.");
            }

        } catch (Exception e) {
            System.out.println("Database error during update: " + e.getMessage());
        }
    }

    // --- Input Validation Wrappers ---

    private static int readIntInput(String prompt, Scanner scanner) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number (or 0 to cancel).");
            }
        }
    }

    private static String readValidRole(Scanner scanner) {
        while (true) {
            System.out.print("Enter new role (STUDENT, TEACHER, ADMIN, or 0 to cancel): ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0")) {
                return "CANCEL";
            }

            if (role.equals("STUDENT") || role.equals("TEACHER") || role.equals("ADMIN")) {
                return role;
            }

            System.out.println("Invalid role! Please enter STUDENT, TEACHER, or ADMIN.");
        }
    }

    private static String readValidStatus(Scanner scanner) {
        while (true) {
            System.out.print("Enter new status (APPROVED, PENDING, or 0 to cancel): ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0")) {
                return "CANCEL";
            }

            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }

            System.out.println("Invalid status! Please enter APPROVED or PENDING.");
        }
    }
}