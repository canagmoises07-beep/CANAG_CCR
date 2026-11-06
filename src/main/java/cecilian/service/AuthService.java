
package cecilian.service;

import cecilian.dao.UserDAO;
import cecilian.dao.PasswordUtil;
import cecilian.dao.ActivityLogDAO;
import cecilian.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class AuthService {

    private final UserDAO userDAO;
    private final ActivityLogDAO activityLogDao;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.activityLogDao = new ActivityLogDAO();
    }

    public AuthService() {
        this.userDAO = new UserDAO();
        this.activityLogDao = new ActivityLogDAO();
    }

    // Centralized Activity Logging
    private void logActivity(int userId, String actionType, String description) {
        try {
            activityLogDao.logAction(userId, actionType, description);
        } catch (SQLException e) {
            System.out.println("Activity logging error: " + e.getMessage());
        }
    }

    // ACCOUNT REGISTRATION
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
                    logActivity(
                            user.userId(),
                            "USER_REGISTERED",
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

    // USER LOGIN
    public User login(String username, String password) {
        try {
            User user = userDAO.findByUsername(username.trim());

            if (user == null) {
                return null;
            }

            if (!"ACTIVE".equalsIgnoreCase(user.status())
                    && !"APPROVED".equalsIgnoreCase(user.status())) {

                logActivity(
                        user.userId(),
                        "LOGIN_FAILED",
                        "Login attempt rejected due to account status: " + user.status()
                );

                return null;
            }

            boolean validPassword = PasswordUtil.verifyPassword(
                    password,
                    user.passwordHash()
            );

            if (!validPassword) {
                logActivity(
                        user.userId(),
                        "LOGIN_FAILED",
                        "Incorrect password."
                );

                return null;
            }

            logActivity(
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

    // USER LOGOUT
    public void logout(User user) {
        if (user == null) {
            return;
        }

        logActivity(
                user.userId(),
                "LOGOUT",
                "User logged out."
        );
    }

    // INITIAL ADMIN SETUP
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
                    logActivity(
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

    // RETRIEVE PENDING USERS
    public List<User> getPendingUsers() {
        try {
            return userDAO.getPendingUsers();
        } catch (Exception e) {
            System.out.println("Error loading pending accounts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // APPROVE USER ACCOUNT
    public boolean approveAccount(int userId, User admin) {

        if (admin == null || !"ADMIN".equalsIgnoreCase(admin.role())) {
            return false;
        }

        try {
            boolean approved = userDAO.approveUser(userId);

            if (approved) {
                logActivity(
                        admin.userId(),
                        "USER_APPROVED",
                        "Admin [" + admin.userId()
                                + "] approved account for User ID " + userId
                );
            }

            return approved;

        } catch (Exception e) {
            System.out.println("Error approving account: " + e.getMessage());
            return false;
        }
    }
    public boolean updateUserRoleAndStatus(
            int targetId,
            String newRole,
            String newStatus,
            User admin
    ) {
        if (admin == null || !"ADMIN".equalsIgnoreCase(admin.role())) {
            System.out.println("Access denied. Admin only.");
            return false;
        }

        try {
            boolean updated = userDAO.updateUserRoleAndStatus(
                    targetId,
                    newRole,
                    newStatus
            );

            if (updated) {
                logActivity(
                        admin.userId(),
                        "USER_UPDATED",
                        "Updated user ID " + targetId
                                + " | Role: " + newRole
                                + " | Status: " + newStatus
                );
            }

            return updated;

        } catch (java.sql.SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
}
