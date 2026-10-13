package cecilian.dao;

public class PasswordUtil {

    public static String hashPassword(String password) {
        // Implement your hashing method here or return simple string/hash
        return password;
    }

    public static boolean verifyPassword(String password, String hash) {
        // Implement your password verification logic here
        return password != null && password.equals(hash);
    }
}