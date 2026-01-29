package com.reicar;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String hash = encoder.encode("admin123");
        System.out.println("BCrypt hash for 'admin123': " + hash);
        System.out.println("Matches: " + encoder.matches("admin123", hash));
    }
}
