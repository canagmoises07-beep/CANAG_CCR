package cecilian;

import cecilian.dao.DatabaseInitializer;
import cecilian.dao.UserDAO;
import cecilian.ui.Authentication;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================");
        System.out.println(" CECILIANS CLEARANCE SYSTEM");
        System.out.println("==================================");

        DatabaseInitializer.initializeDatabase();

        UserDAO userDAO = new UserDAO();
        Scanner scanner = new Scanner(System.in);

        Authentication authentication = new Authentication(userDAO, scanner);
        authentication.start();
    }
}