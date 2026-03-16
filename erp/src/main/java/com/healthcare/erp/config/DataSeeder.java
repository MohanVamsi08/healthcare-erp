package com.healthcare.erp.config;

import com.healthcare.erp.model.Role;
import com.healthcare.erp.model.User;
import com.healthcare.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SUPER_ADMIN_EMAIL:admin@healthcare-erp.com}")
    private String adminEmail;

    @Value("${SUPER_ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("⚠️  SUPER_ADMIN_PASSWORD env var not set. Skipping super-admin seed. Set it to bootstrap the first admin.");
            return;
        }

        if (!userRepository.existsByEmail(adminEmail)) {
            User superAdmin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName("Super")
                    .lastName("Admin")
                    .role(Role.SUPER_ADMIN)
                    .hospital(null)
                    .build();

            userRepository.save(superAdmin);
            log.info("✅ Default SUPER_ADMIN created: {}", adminEmail);
        } else {
            log.info("✅ SUPER_ADMIN already exists, skipping seed.");
        }
    }
}
