package com.swpts.enpracticebe.config;

import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .email("admin@enpractice.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .displayName("Admin")
                    .build();

            userRepository.save(admin);
            log.info("✅ Seeded admin user: admin@enpractice.com / admin123");
        } else {
            log.info("ℹ️ Users table already has data, skipping seed.");
        }
    }
}
