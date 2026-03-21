package com.flowable.platform.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for test data.
 * Run this class to generate fresh hashes for the test migration.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("Generating BCrypt hashes for test passwords:");
        System.out.println("=".repeat(60));

        String[] passwords = {"admin123", "user123", "other123"};

        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.printf("%s -> %s%n", password, hash);
        }

        System.out.println("=".repeat(60));
        System.out.println("Use these hashes in V1__test_data.sql");
    }
}
