
package cecilian;

public record User(
        int userId,
        String schoolId,
        String fullName,
        String username,
        String passwordHash,
        String role,
        Integer departmentId,
        String status
) {
}