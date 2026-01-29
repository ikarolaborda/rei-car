/*
 * Migration: Create users table for authentication
 * Supports three roles: ADMIN, MECHANIC, CUSTOMER
 * Customer role users link to an existing customer entity
 */

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    customer_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'MECHANIC', 'CUSTOMER'))
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
