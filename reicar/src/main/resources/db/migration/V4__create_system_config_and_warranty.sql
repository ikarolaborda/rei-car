/*
 * Migration: Create system configuration table for warranty and other settings
 * Also adds warranty tracking to service orders
 */

CREATE TABLE system_config (
    config_key VARCHAR(50) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

/* Default warranty period: 90 days */
INSERT INTO system_config (config_key, config_value, description) VALUES
    ('warranty_days', '90', 'Período de garantia em dias para serviços realizados');

/* Add warranty claim tracking to service orders */
ALTER TABLE service_orders ADD COLUMN warranty_claimed BOOLEAN DEFAULT FALSE;
ALTER TABLE service_orders ADD COLUMN warranty_claim_date DATE NULL;
ALTER TABLE service_orders ADD COLUMN warranty_claim_reason VARCHAR(500) NULL;
