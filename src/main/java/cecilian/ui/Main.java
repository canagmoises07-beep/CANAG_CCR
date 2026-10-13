package cecilian.ui;

import cecilian.dao.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================");
        System.out.println(" CECILIANS CLEARANCE SYSTEM");
        System.out.println("==================================");

        DatabaseConnection.initializeDatabase();

        AuthMenu.showMenu();
    }
}