package cecilian;

import java.util.Scanner;

public class StudentMenu {

    private static final Scanner scanner = new Scanner(System.in);

    public static void showMenu(User student, AuthService authService) {
        while (true) {
            System.out.println("\n==================================");
            System.out.println(" STUDENT DASHBOARD");
            System.out.println("==================================");
            System.out.println("Welcome, " + student.fullName() + "!");
            System.out.println("1. View Clearance Status");
            System.out.println("2. Submit Clearance Request");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println(
                            "\nClearance status feature is coming next."
                    );
                    break;

                case "2":
                    System.out.println(
                            "\nClearance request feature is coming next."
                    );
                    break;

                case "3":
                    authService.logout(student);
                    System.out.println("You have been logged out.");
                    return;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }
}