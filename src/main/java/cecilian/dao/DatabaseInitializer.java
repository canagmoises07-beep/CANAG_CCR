package cecilian.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        String usersTable = """
                CREATE TABLE IF NOT EXISTS USERS_TBL (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    school_id TEXT NOT NULL UNIQUE,
                    full_name TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL,
                    department_id INTEGER,
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )
                """;

        String auditTable = """
                CREATE TABLE IF NOT EXISTS AUDIT_LOGS (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action TEXT NOT NULL,
                    description TEXT,
                    timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES USERS_TBL(user_id)
                )
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(usersTable);
            statement.execute(auditTable);

            System.out.println("SQLite database connected!");
            System.out.println("Database tables initialized.");

        } catch (SQLException e) {
            System.out.println("Database error during initialization: " + e.getMessage());
        }
    }
}