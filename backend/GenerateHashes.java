import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHashes {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String adminPassword = "admin123";
        String userPassword = "user123";

        String adminHash = encoder.encode(adminPassword);
        String userHash = encoder.encode(userPassword);

        System.out.println("BCrypt hash for 'admin123': " + adminHash);
        System.out.println("BCrypt hash for 'user123': " + userHash);

        // Verify they work
        System.out.println("\nVerification:");
        System.out.println("admin123 matches adminHash? " + encoder.matches(adminPassword, adminHash));
        System.out.println("user123 matches userHash? " + encoder.matches(userPassword, userHash));
    }
}
