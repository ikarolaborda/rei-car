package com.reicar.config;

import com.reicar.entities.User;
import com.reicar.entities.enums.Role;
import com.reicar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner initAdminUser() {
        return args -> {
            var existingAdmin = userRepository.findByUsername("admin");
            if (existingAdmin.isPresent()) {
                User admin = existingAdmin.get();
                if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(admin);
                    log.info("Admin password has been reset to default");
                }
            } else {
                User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
                userRepository.save(admin);
                log.info("Default admin user created");
            }
        };
    }
}
