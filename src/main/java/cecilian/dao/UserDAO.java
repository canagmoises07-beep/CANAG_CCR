package cecilian.dao;

import cecilian.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class UserDAO {

    // Save a new user account
    public boolean createUser(
            String schoolId,
            String fullName,
            String username,
            String passwordHash,
            String role) throws SQLException {

        String sql = """
            INSERT INTO USERS_TBL
            (school_id, full_name, username, password_hash, role, status)
            VALUES (?, ?, ?, ?, ?, 'PENDING')
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, schoolId);
            statement.setString(2, fullName);
            statement.setString(3, username);
            statement.setString(4, passwordHash);
            statement.setString(5, role);

            return statement.executeUpdate() > 0;
        }
    }

    // Find a user by username
    public User findByUsername(String username) throws SQLException {

        String sql = """
                SELECT * FROM USERS_TBL
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {

                    Object departmentValue =
                            result.getObject("department_id");

                    Integer departmentId = departmentValue == null
                            ? null
                            : ((Number) departmentValue).intValue();

                    return new User(
                            result.getInt("user_id"),
                            result.getString("school_id"),
                            result.getString("full_name"),
                            result.getString("username"),
                            result.getString("password_hash"),
                            result.getString("role"),
                            departmentId,
                            result.getString("status")
                    );
                }
            }
        }

        return null;
    }

    // Record an action in the audit log
    public void logAction(
            Integer userId,
            String action,
            String description) throws SQLException {

        String sql = """
                INSERT INTO AUDIT_LOGS
                (user_id, action, description)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (userId == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, userId);
            }

            statement.setString(2, action);
            statement.setString(3, description);

            statement.executeUpdate();
        }
    }

    public boolean createInitialAdmin(
            String schoolId,
            String fullName,
            String username,
            String passwordHash) throws SQLException {

        String sql = """
            INSERT INTO USERS_TBL
            (school_id, full_name, username, password_hash, role, status)
            SELECT ?, ?, ?, ?, 'ADMIN', 'ACTIVE'
            WHERE NOT EXISTS (
                SELECT 1 FROM USERS_TBL WHERE role = 'ADMIN'
            )
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, schoolId);
            statement.setString(2, fullName);
            statement.setString(3, username);
            statement.setString(4, passwordHash);

            return statement.executeUpdate() > 0;
        }
    }

    public java.util.List<User> findPendingUsers() throws SQLException {
        java.util.List<User> users = new java.util.ArrayList<>();

        String sql = """
            SELECT * FROM USERS_TBL
            WHERE status = 'PENDING'
            ORDER BY user_id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                Object departmentValue =
                        result.getObject("department_id");

                Integer departmentId = departmentValue == null
                        ? null
                        : ((Number) departmentValue).intValue();

                users.add(new User(
                        result.getInt("user_id"),
                        result.getString("school_id"),
                        result.getString("full_name"),
                        result.getString("username"),
                        result.getString("password_hash"),
                        result.getString("role"),
                        departmentId,
                        result.getString("status")
                ));
            }
        }

        return users;
    }

    public boolean approveUser(int userId) throws SQLException {
        String sql = """
            UPDATE USERS_TBL
            SET status = 'ACTIVE'
            WHERE user_id = ?
            AND status = 'PENDING'
            AND role IN ('STUDENT', 'TEACHER')
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;
        }
    }
}