package cecilian.ui;

import cecilian.dao.UserDAO;
import cecilian.dao.ActivityLogDAO;
import cecilian.model.User;
import cecilian.model.ActivityLog;
import cecilian.service.AuthService;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    // ======================================
    // ADMINISTRATOR DASHBOARD
    // ======================================

    public static void showMenu(
            User admin,
            UserDAO userDAO,
            AuthService authService,
            Scanner scanner) {

        while (true) {

            System.out.println("\n==================================");
            System.out.println(" ADMINISTRATOR DASHBOARD");
            System.out.println("==================================");
            System.out.println("Welcome, " + admin.fullName() + "!");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role & Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Logout");
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
                    approveUserAccount(
                            admin,
                            userDAO,
                            authService,
                            scanner
                    );
                    break;

                case "4":
                    updateUserRoleAndStatus(
                            admin,
                            userDAO,
                            authService,
                            scanner
                    );
                    break;

                case "5":
                    showAuditLogMenu(admin, scanner);
                    break;

                case "6":
                    authService.logout(admin);
                    System.out.println("You have been logged out.");
                    return;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    // ======================================
    // PRINT USER TABLE
    // ======================================

    private static void printUserTable(List<User> users) {

        String format =
                "| %-5s | %-15s | %-15s | %-12s | %-10s |%n";

        String border =
                "+-------+-----------------+-----------------+--------------+------------+";

        System.out.println(border);

        System.out.printf(
                format,
                "ID",
                "Username",
                "Full Name",
                "Role",
                "Status"
        );

        System.out.println(border);

        for (User u : users) {

            System.out.printf(
                    format,
                    u.userId(),
                    u.username(),
                    u.fullName(),
                    u.role(),
                    u.status()
            );
        }

        System.out.println(border);
    }

    // ======================================
    // VIEW ALL SYSTEM USERS
    // ======================================

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

            System.out.println(
                    "Error fetching users: " + e.getMessage()
            );
        }
    }

    // ======================================
    // VIEW PENDING REGISTRATIONS
    // ======================================

    private static void viewPendingRegistrations(
            UserDAO userDAO) {

        try {

            List<User> pendingUsers =
                    userDAO.getPendingUsers();

            System.out.println("\n--- PENDING REGISTRATIONS ---");

            if (pendingUsers.isEmpty()) {
                System.out.println("No pending accounts found.");
                return;
            }

            printUserTable(pendingUsers);

        } catch (Exception e) {

            System.out.println(
                    "Error fetching pending registrations: "
                            + e.getMessage()
            );
        }
    }

    // ======================================
    // APPROVE USER ACCOUNT
    // ======================================

    private static void approveUserAccount(
            User admin,
            UserDAO userDAO,
            AuthService authService,
            Scanner scanner) {

        try {

            List<User> pendingUsers =
                    userDAO.getPendingUsers();

            System.out.println("\n--- APPROVE USER ACCOUNT ---");

            if (pendingUsers.isEmpty()) {
                System.out.println(
                        "No pending accounts to approve."
                );
                return;
            }

            printUserTable(pendingUsers);

            int targetId = readIntInput(
                    "Enter the User ID to approve (0 to cancel): ",
                    scanner
            );

            if (targetId == 0) {
                System.out.println(
                        "Approval action cancelled."
                );
                return;
            }

            boolean existsInPending =
                    pendingUsers.stream()
                            .anyMatch(u -> u.userId() == targetId);

            if (!existsInPending) {

                System.out.println(
                        "Error: User ID not found in pending registrations."
                );

                return;
            }

            // AuthService handles approval and activity logging
            boolean approved = authService.approveAccount(
                    targetId,
                    admin
            );

            if (approved) {

                System.out.println(
                        "Success: User account ID "
                                + targetId
                                + " successfully approved!"
                );

            } else {

                System.out.println(
                        "Approval failed. Please try again."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Database error during approval: "
                            + e.getMessage()
            );
        }
    }

    // ======================================
    // UPDATE USER ROLE AND STATUS
    // ======================================

    private static void updateUserRoleAndStatus(
            User admin,
            UserDAO userDAO,
            AuthService authService,
            Scanner scanner) {

        try {

            List<User> users = userDAO.getAllUsers();

            System.out.println(
                    "\n--- UPDATE USER ROLE & STATUS ---"
            );

            if (users.isEmpty()) {
                System.out.println("No users found in the system.");
                return;
            }

            printUserTable(users);

            int targetId = readIntInput(
                    "Enter the User ID to update (0 to cancel): ",
                    scanner
            );

            if (targetId == 0) {
                System.out.println(
                        "Update action cancelled."
                );
                return;
            }

            // Prevent administrators from modifying themselves
            if (targetId == admin.userId()) {

                System.out.println(
                        "WARNING: Modifying the currently active "
                                + "admin account is restricted."
                );

                return;
            }

            // Verify target user exists
            User targetUser = users.stream()
                    .filter(u -> u.userId() == targetId)
                    .findFirst()
                    .orElse(null);

            if (targetUser == null) {

                System.out.println(
                        "Error: User ID not found."
                );

                return;
            }

            System.out.println(
                    "Selected User: " + targetUser.username()
            );

            System.out.println(
                    "Current Role: " + targetUser.role()
            );

            System.out.println(
                    "Current Status: " + targetUser.status()
            );

            String newRole = readValidRole(scanner);

            if (newRole.equals("CANCEL")) {
                System.out.println(
                        "Update action cancelled."
                );
                return;
            }

            String newStatus = readValidStatus(scanner);

            if (newStatus.equals("CANCEL")) {
                System.out.println(
                        "Update action cancelled."
                );
                return;
            }

            // AuthService handles updating and activity logging
            boolean updated =
                    authService.updateUserRoleAndStatus(
                            targetId,
                            newRole,
                            newStatus,
                            admin
                    );

            if (updated) {

                System.out.println(
                        "Success: User account ID "
                                + targetId
                                + " successfully updated!"
                );

            } else {

                System.out.println(
                        "Update failed. Please try again."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Database error during update: "
                            + e.getMessage()
            );
        }
    }

    // ======================================
    // SYSTEM AUDIT LOG MENU
    // ======================================

    private static void showAuditLogMenu(
            User admin,
            Scanner scanner) {

        ActivityLogDAO activityLogDAO =
                new ActivityLogDAO();

        while (true) {

            System.out.println("\n==================================");
            System.out.println(" SYSTEM AUDIT LOGS");
            System.out.println("==================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Back to Administrator Dashboard");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    viewAllActivityLogs(activityLogDAO);
                    break;

                case "2":

                    int userId = readIntInput(
                            "Enter User ID: ",
                            scanner
                    );

                    if (userId <= 0) {
                        System.out.println(
                                "Please enter a valid User ID."
                        );
                        break;
                    }

                    viewUserActivityLogs(
                            activityLogDAO,
                            userId
                    );

                    break;

                case "3":
                    clearHistoricalLogs(
                            activityLogDAO,
                            admin,
                            scanner
                    );
                    break;

                case "4":
                    return;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    // ======================================
    // VIEW ALL ACTIVITY LOGS
    // ======================================

    private static void viewAllActivityLogs(
            ActivityLogDAO activityLogDAO) {

        try {

            List<ActivityLog> logs =
                    activityLogDAO.getAllLogs();

            System.out.println(
                    "\n--- ALL SYSTEM AUDIT LOGS ---"
            );

            printActivityLogs(logs);

        } catch (Exception e) {

            System.out.println(
                    "Error retrieving activity logs: "
                            + e.getMessage()
            );
        }
    }

    // ======================================
    // FILTER LOGS BY USER ID
    // ======================================

    private static void viewUserActivityLogs(
            ActivityLogDAO activityLogDAO,
            int userId) {

        try {

            List<ActivityLog> logs =
                    activityLogDAO.getLogsByUserId(userId);

            System.out.println(
                    "\n--- ACTIVITY LOGS FOR USER ID: "
                            + userId + " ---"
            );

            printActivityLogs(logs);

        } catch (Exception e) {

            System.out.println(
                    "Error filtering activity logs: "
                            + e.getMessage()
            );
        }
    }

    // ======================================
    // PRINT FORMATTED ACTIVITY LOG TABLE
    // ======================================

    private static void printActivityLogs(
            List<ActivityLog> logs) {

        String format =
                "| %-5s | %-8s | %-18s | %-35s | %-20s |%n";

        String border =
                "+-------+----------+--------------------+-------------------------------------+----------------------+";

        System.out.println(border);

        System.out.printf(
                format,
                "ID",
                "User ID",
                "Action Type",
                "Description",
                "Timestamp"
        );

        System.out.println(border);

        if (logs.isEmpty()) {

            System.out.printf(
                    "| %-84s |%n",
                    "No activity logs found."
            );

        } else {

            for (ActivityLog log : logs) {

                System.out.printf(
                        format,
                        log.logId(),
                        log.userId(),
                        shorten(log.actionType(), 18),
                        shorten(log.description(), 35),
                        shorten(log.timestamp(), 20)
                );
            }
        }

        System.out.println(border);
    }

    // ======================================
    // SHORTEN LONG TEXT
    // ======================================

    private static String shorten(
            String value,
            int maxLength) {

        if (value == null) {
            return "";
        }

        if (value.length() > maxLength) {

            return value.substring(
                    0,
                    maxLength - 3
            ) + "...";
        }

        return value;
    }

    // ======================================
    // CLEAR HISTORICAL ACTIVITY LOGS
    // ======================================

    private static void clearHistoricalLogs(
            ActivityLogDAO activityLogDAO,
            User admin,
            Scanner scanner) {

        try {

            List<ActivityLog> logs =
                    activityLogDAO.getAllLogs();

            if (logs.isEmpty()) {

                System.out.println(
                        "There are no activity logs to clear."
                );

                return;
            }

            System.out.println("\nWARNING!");

            System.out.println(
                    "You are about to delete "
                            + logs.size()
                            + " historical activity records."
            );

            System.out.print(
                    "Are you sure? Type YES to confirm: "
            );

            String confirmation =
                    scanner.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("YES")) {

                System.out.println(
                        "Clear operation cancelled."
                );

                return;
            }

            boolean cleared =
                    activityLogDAO.clearAllLogs();

            if (cleared) {

                // Record the clearing action after deletion
                activityLogDAO.logAction(
                        admin.userId(),
                        "LOGS_CLEARED",
                        "Administrator ["
                                + admin.userId()
                                + "] cleared historical activity logs."
                );

                System.out.println(
                        "Historical logs cleared successfully."
                );

                System.out.println(
                        "The clearing action has been recorded."
                );

            } else {

                System.out.println(
                        "Unable to clear historical logs."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Error clearing activity logs: "
                            + e.getMessage()
            );
        }
    }

    // ======================================
    // INPUT VALIDATION
    // ======================================

    private static int readIntInput(
            String prompt,
            Scanner scanner) {

        while (true) {

            System.out.print(prompt);

            String input =
                    scanner.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid input! Please enter a valid number."
                );
            }
        }
    }

    // ======================================
    // VALIDATE USER ROLE
    // ======================================

    private static String readValidRole(
            Scanner scanner) {

        while (true) {

            System.out.print(
                    "Enter new role "
                            + "(STUDENT, TEACHER, ADMIN, or 0 to cancel): "
            );

            String role =
                    scanner.nextLine().trim().toUpperCase();

            if (role.equals("0")) {
                return "CANCEL";
            }

            if (role.equals("STUDENT")
                    || role.equals("TEACHER")
                    || role.equals("ADMIN")) {

                return role;
            }

            System.out.println(
                    "Invalid role! Please enter STUDENT, TEACHER, or ADMIN."
            );
        }
    }

    // ======================================
    // VALIDATE USER STATUS
    // ======================================

    private static String readValidStatus(
            Scanner scanner) {

        while (true) {

            System.out.print(
                    "Enter new status "
                            + "(APPROVED, PENDING, or 0 to cancel): "
            );

            String status =
                    scanner.nextLine().trim().toUpperCase();

            if (status.equals("0")) {
                return "CANCEL";
            }

            if (status.equals("APPROVED")
                    || status.equals("PENDING")) {

                return status;
            }

            System.out.println(
                    "Invalid status! Please enter APPROVED or PENDING."
            );
        }
    }
}
