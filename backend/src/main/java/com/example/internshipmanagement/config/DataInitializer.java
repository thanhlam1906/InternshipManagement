package com.example.internshipmanagement.config;

import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.entity.enums.AuthProvider;
import com.example.internshipmanagement.entity.enums.Role;
import com.example.internshipmanagement.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a default admin account on first startup if one doesn't exist.
 * Configure via environment variables:
 *   ADMIN_USERNAME  — default: admin
 *   ADMIN_PASSWORD  — required, throw error if not set
 *   ADMIN_EMAIL     — default: admin@internship.local
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.email:admin@internship.local}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_PASSWORD not set — skipping default admin creation. " +
                     "Set env var ADMIN_PASSWORD to auto-create an admin account.");
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user '{}' already exists — skipping creation.", adminUsername);
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .fullName("System Admin")
                .email(adminEmail)
                .role(Role.ADMIN)
                .provider(AuthProvider.LOCAL)
                .emailVerified(true)
                .isActive(true)
                .build();

        userRepository.save(admin);
        log.info("======================================================");
        log.info("  Default admin account created!");
        log.info("  Username: {}", adminUsername);
        log.info("  Password: [set via ADMIN_PASSWORD env var]");
        log.info("======================================================");
    }
}
