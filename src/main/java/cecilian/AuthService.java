
package cecilian;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    // Register a student or department staff account
    public String registerAccount(
            String schoolId,
            String fullName,
            String username,
            String password,
            String confirmPassword,
            String role) {

        schoolId = schoolId.trim();
        fullName = fullName.trim();
        username = username.trim();

        if (schoolId.isEmpty() || fullName.isEmpty()
                || username.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty()) {
            return "All fields are required.";
        }

        if (!"STUDENT".equals(role) && !"TEACHER".equals(role)) {
            return "Invalid account type.";
        }

        if (password.length() < 8) {
            return "Password must be at least 8 characters.";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        try {
            if (userDAO.findByUsername(username) != null) {
                return "Username is already taken.";
            }

            String passwordHash = PasswordUtil.hashPassword(password);

            boolean created = userDAO.createUser(
                    schoolId,
                    fullName,
                    username,
                    passwordHash,
                    role
            );

            if (created) {
                User user = userDAO.findByUsername(username);

                if (user != null) {
                    userDAO.logAction(
                            user.userId(),
                            "REGISTRATION",
                            role + " account registered with PENDING status."
                    );
                }

                return "Registration successful! Your account is PENDING approval.";
            }

        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().contains("UNIQUE")) {
                return "School ID or username already exists.";
            }

            System.out.println("Database error: " + e.getMessage());

        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
        }

        return "Registration failed. Please try again.";
    }

    // Log in an active account
    public User login(String username, String password) {

        try {
            User user = userDAO.findByUsername(username.trim());

            if (user == null) {
                return null;
            }

            if (!"ACTIVE".equalsIgnoreCase(user.status())) {
                return null;
            }

            boolean validPassword = PasswordUtil.verifyPassword(
                    password,
                    user.passwordHash()
            );

            if (!validPassword) {
                userDAO.logAction(
                        user.userId(),
                        "LOGIN_FAILED",
                        "Incorrect password."
                );
                return null;
            }

            userDAO.logAction(
                    user.userId(),
                    "LOGIN_SUCCESS",
                    "User logged in successfully."
            );

            return user;

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            return null;
        }
    }

    // Record logout activity
    public void logout(User user) {

        if (user == null) {
            return;
        }

        try {
            userDAO.logAction(
                    user.userId(),
                    "LOGOUT",
                    "User logged out."
            );
        } catch (SQLException e) {
            System.out.println("Unable to record logout.");
        }
    }

    public String setupInitialAdmin(
            String schoolId,
            String fullName,
            String username,
            String password,
            String confirmPassword) {

        schoolId = schoolId.trim();
        fullName = fullName.trim();
        username = username.trim();

        if (schoolId.isEmpty() || fullName.isEmpty()
                || username.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty()) {
            return "All fields are required.";
        }

        if (password.length() < 8) {
            return "Password must be at least 8 characters.";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        try {
            if (userDAO.findByUsername(username) != null) {
                return "Username is already taken.";
            }

            String passwordHash = PasswordUtil.hashPassword(password);

            boolean created = userDAO.createInitialAdmin(
                    schoolId,
                    fullName,
                    username,
                    passwordHash
            );

            if (created) {
                User admin = userDAO.findByUsername(username);

                if (admin != null) {
                    userDAO.logAction(
                            admin.userId(),
                            "ADMIN_SETUP",
                            "Initial administrator account created."
                    );
                }

                return "Administrator account created successfully!";
            }

            return "An administrator already exists. Setup is disabled.";

        } catch (Exception e) {
            System.out.println("Admin setup error: " + e.getMessage());
            return "Administrator setup failed.";
        }
    }

    public java.util.List<User> getPendingUsers() {
        try {
            return userDAO.findPendingUsers();
        } catch (Exception e) {
            System.out.println("Error loading pending accounts: "
                    + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public boolean approveAccount(int userId, User admin) {
        if (admin == null || !"ADMIN".equalsIgnoreCase(admin.role())
                || !"ACTIVE".equalsIgnoreCase(admin.status())) {
            return false;
        }

        try {
            boolean approved = userDAO.approveUser(userId);

            if (approved) {
                userDAO.logAction(
                        admin.userId(),
                        "ACCOUNT_APPROVED",
                        "Approved account with user ID: " + userId
                );
            }

            return approved;

        } catch (Exception e) {
            System.out.println("Error approving account: "
                    + e.getMessage());
            return false;
        }
    }
}