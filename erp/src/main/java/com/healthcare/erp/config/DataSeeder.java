package com.healthcare.erp.config;

import com.healthcare.erp.model.Role;
import com.healthcare.erp.model.User;
import com.healthcare.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@healthcare-erp.com")) {
            User superAdmin = User.builder()
                    .email("admin@healthcare-erp.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Super")
                    .lastName("Admin")
                    .role(Role.SUPER_ADMIN)
                    .hospital(null)
                    .build();

            userRepository.save(superAdmin);
            log.info("✅ Default SUPER_ADMIN created: admin@healthcare-erp.com / admin123");
        } else {
            log.info("✅ SUPER_ADMIN already exists, skipping seed.");
        }
    }
}
