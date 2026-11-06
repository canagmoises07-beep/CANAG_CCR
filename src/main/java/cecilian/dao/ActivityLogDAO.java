package cecilian.dao;

import cecilian.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {

    // INSERT NEW ACTIVITY LOG
    public boolean logAction(
            int userId,
            String actionType,
            String description) throws SQLException {

        String sql = """
                INSERT INTO activity_logs
                (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, actionType);
            statement.setString(3, description);

            return statement.executeUpdate() > 0;
        }
    }

    // GET ALL SYSTEM LOGS
    public List<ActivityLog> getAllLogs() throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM activity_logs
                ORDER BY timestamp DESC, log_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                logs.add(new ActivityLog(
                        result.getInt("log_id"),
                        result.getInt("user_id"),
                        result.getString("action_type"),
                        result.getString("description"),
                        result.getString("timestamp")
                ));
            }
        }

        return logs;
    }

    // FILTER LOGS BY USER ID
    public List<ActivityLog> getLogsByUserId(int userId)
            throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT * FROM activity_logs
                WHERE user_id = ?
                ORDER BY timestamp DESC, log_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    logs.add(new ActivityLog(
                            result.getInt("log_id"),
                            result.getInt("user_id"),
                            result.getString("action_type"),
                            result.getString("description"),
                            result.getString("timestamp")
                    ));
                }
            }
        }

        return logs;
    }

    // CLEAR HISTORICAL LOGS
    public boolean clearAllLogs() throws SQLException {

        String sql = "DELETE FROM activity_logs";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate() >= 0;
        }
    }
}
