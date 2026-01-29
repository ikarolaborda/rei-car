/*
 * Migration: Create default admin user
 * Username: admin
 * Password: admin123 (BCrypt hashed with cost factor 10)
 * IMPORTANT: Change this password immediately after first login!
 */

INSERT INTO users (username, password, role, enabled) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKFqWAIABOJaHhTg1ZKiCNP9LwEe', 'ADMIN', TRUE);
