package com.betwise.config;

import com.betwise.model.User;
import com.betwise.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Makes sure an admin account exists, configured with:
 *
 *   betwise.admin.email=you@example.com
 *   betwise.admin.password=...   (only used if the account doesn't exist yet)
 *
 * or the BETWISE_ADMIN_EMAIL / BETWISE_ADMIN_PASSWORD environment variables.
 * An existing account with that email is promoted to admin; its password is
 * never changed. With no email configured, this does nothing.
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${betwise.admin.email:}")
    private String adminEmail;

    @Value("${betwise.admin.password:}")
    private String adminPassword;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }

        String email = adminEmail.toLowerCase().trim();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("betwise.admin.email is set but no account exists and no betwise.admin.password was given");
                return;
            }

            user = new User();
            user.setName("Admin");
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(adminPassword));
            log.info("Created admin account {}", email);
        } else if (user.isAdmin()) {
            return;
        } else {
            log.info("Promoted {} to admin", email);
        }

        user.setRole(User.Role.ADMIN);
        userRepository.save(user);
    }
}
