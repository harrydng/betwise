package com.betwise.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Schema changes that Hibernate's ddl-auto=update can't make itself. It
 * adds new tables and columns but never loosens an existing column, so
 * databases created before Google sign-in still have a NOT NULL
 * password_hash, which would reject Google-only accounts.
 *
 * Each step checks first, so this is safe to run on every startup. Swap
 * this for a migration tool such as Flyway once the schema grows.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SchemaUpgrades implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaUpgrades.class);

    private final JdbcTemplate jdbc;

    public SchemaUpgrades(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        try {
            makePasswordHashNullable();
        } catch (Exception exception) {
            log.warn("Couldn't apply schema upgrades: {}", exception.getMessage());
        }
    }

    private void makePasswordHashNullable() {
        List<String> nullable = jdbc.queryForList(
                """
                SELECT IS_NULLABLE FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'users'
                  AND COLUMN_NAME = 'password_hash'
                """,
                String.class);

        if (!nullable.isEmpty() && "NO".equalsIgnoreCase(nullable.get(0))) {
            jdbc.execute("ALTER TABLE users MODIFY password_hash VARCHAR(255) NULL");
            log.info("Made users.password_hash nullable for Google sign-in");
        }
    }
}
