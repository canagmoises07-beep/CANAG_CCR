package cecilian.ui;

import cecilian.dao.UserDAO;
import cecilian.model.User;
import cecilian.service.AuthService;

import java.util.Scanner;

public class Authentication {

    private final UserDAO userDAO;
    private final Scanner scanner;
    private final AuthService authService;

    public Authentication(UserDAO userDAO, Scanner scanner) {
        this.userDAO = userDAO;
        this.scanner = scanner;
        this.authService = new AuthService(userDAO);
    }

    public void start() {
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

    private void registerAccount() {
        System.out.println("\n--- ACCOUNT REGISTRATION ---");
        System.out.println("1. Student");
        System.out.println("2. Teacher / Department Staff");
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
                schoolId, fullName, username, password, confirmPassword, role
        );

        System.out.println(result);
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = authService.login(username, password);

        if (user == null) {
            System.out.println("Login failed. Check your credentials or account status.");
            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.fullName() + "!");
        System.out.println("Role: " + user.role());

        String role = user.role().toUpperCase();
        switch (role) {
            case "ADMIN":
                AdminMenu.showMenu(user, userDAO, authService, scanner);
                break;
            case "STUDENT":
                StudentMenu.showMenu(user, authService, scanner);
                break;
            case "TEACHER":
                showTeacherMenu(user);
                break;
            default:
                System.out.println("Unrecognized account role.");
                authService.logout(user);
        }
    }

    private void setupInitialAdmin() {
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
                schoolId, fullName, username, password, confirmPassword
        );

        System.out.println(result);
    }

    private void showTeacherMenu(User teacher) {
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
}