package com.example.bizflow.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Repairs common data issues in bizflow_auth_db that can break login.
 *
 * Context:
 * - Some environments end up with blank password/password_hash fields in auth DB users.
 * - AuthService expects BCrypt hashes in password_hash (preferred) or password (fallback).
 *
 * This runner keeps behavior backward-compatible by:
 * 1) Backfilling password_hash from password when password_hash is blank.
 * 2) If a canonical bizflow_db.users exists, backfilling missing auth passwords from it.
 */
@Component
public class AuthDbRepairRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public AuthDbRepairRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // 1) Keep auth DB consistent: if password_hash is blank but password has a hash, copy it.
        jdbcTemplate.update(
                "UPDATE users " +
                "SET password_hash = password " +
                "WHERE (password_hash IS NULL OR password_hash = '') " +
                "  AND (password IS NOT NULL AND password <> '')"
        );

        // 2) If the main DB exists, use it as a source of truth to backfill blank passwords.
        Integer mainUsersTableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = 'bizflow_db' AND table_name = 'users'",
                Integer.class
        );

        if (mainUsersTableExists != null && mainUsersTableExists > 0) {
            // Collation can differ between databases, so compare with explicit collation.
            jdbcTemplate.update(
                    "UPDATE bizflow_auth_db.users u " +
                    "JOIN bizflow_db.users bu " +
                    "  ON bu.username COLLATE utf8mb4_unicode_ci = u.username COLLATE utf8mb4_unicode_ci " +
                    "SET u.password = bu.password, u.password_hash = bu.password " +
                    "WHERE (u.password IS NULL OR u.password = '') " +
                    "  AND (u.password_hash IS NULL OR u.password_hash = '') " +
                    "  AND (bu.password IS NOT NULL AND bu.password <> '')"
            );
        }
    }
}
