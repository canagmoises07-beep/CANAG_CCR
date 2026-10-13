package cecilian.ui;

import cecilian.model.User;
import cecilian.service.AuthService;

import java.util.Scanner;

public class AuthMenu {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthService authService = new AuthService();

    public static void showMenu() {
        while (true) {
            System.out.println("\n==================================");
            System.out.println(" CECILIANS CLEARANCE SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Initial Admin Setup");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    registerAccount();
                    break;
                case "2":
                    login();
                    break;
                case "3":
                    setupInitialAdmin();
                    break;
                case "4":
                    System.out.println("Thank you for using the system!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void registerAccount() {
        System.out.println("\n--- ACCOUNT REGISTRATION ---");
        System.out.println("1. Student");
        System.out.println("2. Department Staff (Teacher)");
        System.out.print("Select account type: ");

        String roleChoice = scanner.nextLine();
        String role;

        switch (roleChoice) {
            case "1":
                role = "STUDENT";
                break;
            case "2":
                role = "TEACHER";
                break;
            default:
                System.out.println("Invalid account type.");
                return;
        }

        System.out.print("School ID: ");
        String schoolId = scanner.nextLine();

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password (minimum 8 characters): ");
        String password = scanner.nextLine();

        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine();

        String result = authService.registerAccount(
                schoolId,
                fullName,
                username,
                password,
                confirmPassword,
                role
        );

        System.out.println(result);
    }

    private static void login() {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = authService.login(username, password);

        if (user == null) {
            System.out.println(
                    "Login failed. Check your credentials or account status."
            );
            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.fullName() + "!");
        System.out.println("Role: " + user.role());

        if ("ADMIN".equalsIgnoreCase(user.role())) {
            showAdminMenu(user);
        } else if ("STUDENT".equalsIgnoreCase(user.role())) {
            StudentMenu.showMenu(user, authService);
        } else if ("TEACHER".equalsIgnoreCase(user.role())) {
            showTeacherMenu(user);
        } else {
            System.out.println("Unrecognized account role.");
            authService.logout(user);
        }
    }

    private static void showTeacherMenu(User teacher) {
        while (true) {
            System.out.println("\n==================================");
            System.out.println(" DEPARTMENT STAFF (TEACHER) DASHBOARD");
            System.out.println("==================================");
            System.out.println("Welcome, " + teacher.fullName() + "!");
            System.out.println("1. View Department Clearance Requests");
            System.out.println("2. Sign/Approve Clearance");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\nView clearance requests feature is coming next.");
                    break;

                case "2":
                    System.out.println("\nSign/approve clearance feature is coming next.");
                    break;

                case "3":
                    authService.logout(teacher);
                    System.out.println("You have been logged out.");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void setupInitialAdmin() {
        System.out.println("\n--- INITIAL ADMIN SETUP ---");

        System.out.print("School ID: ");
        String schoolId = scanner.nextLine();

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();

        System.out.print("Admin Username: ");
        String username = scanner.nextLine();

        System.out.print("Password (minimum 8 characters): ");
        String password = scanner.nextLine();

        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine();

        String result = authService.setupInitialAdmin(
                schoolId,
                fullName,
                username,
                password,
                confirmPassword
        );

        System.out.println(result);
    }

    private static void showAdminMenu(User admin) {
        while (true) {
            System.out.println("\n==================================");
            System.out.println(" ADMINISTRATOR DASHBOARD");
            System.out.println("==================================");
            System.out.println("1. View Pending Accounts");
            System.out.println("2. Approve Account");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewPendingAccounts();
                    break;

                case "2":
                    approveAccount(admin);
                    break;

                case "3":
                    authService.logout(admin);
                    System.out.println("You have been logged out.");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewPendingAccounts() {
        java.util.List<User> users = authService.getPendingUsers();

        System.out.println("\n--- PENDING ACCOUNTS ---");

        if (users.isEmpty()) {
            System.out.println("No pending accounts found.");
            return;
        }

        for (User user : users) {
            System.out.println(
                    "ID: " + user.userId()
                            + " | Name: " + user.fullName()
                            + " | Username: " + user.username()
                            + " | Role: " + user.role()
                            + " | Status: " + user.status()
            );
        }
    }

    private static void approveAccount(User admin) {
        viewPendingAccounts();

        System.out.print("\nEnter the User ID to approve (0 to cancel): ");
        String input = scanner.nextLine();

        try {
            int userId = Integer.parseInt(input);

            if (userId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            boolean approved = authService.approveAccount(userId, admin);

            if (approved) {
                System.out.println("Account approved successfully!");
            } else {
                System.out.println(
                        "Approval failed. Check the User ID and account status."
                );
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID. Please enter a number.");
        }
    }
}