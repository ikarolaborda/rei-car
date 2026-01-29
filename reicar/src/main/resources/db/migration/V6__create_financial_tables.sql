/*
 * Migration: Create financial module tables
 * - invoices: Generated from completed service orders
 * - payments: Payment records against invoices
 * - invoice_status_history: Audit trail for status changes
 */

CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(30) NOT NULL UNIQUE,
    issue_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    service_order_id BIGINT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    total_value DECIMAL(10, 2) NOT NULL,
    paid_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_service_order FOREIGN KEY (service_order_id) REFERENCES service_orders(id),
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT chk_invoice_status CHECK (status IN ('UNPAID', 'PARTIAL', 'PAID', 'CANCELLED'))
);

CREATE INDEX idx_invoice_status ON invoices(status);
CREATE INDEX idx_invoice_issue_date ON invoices(issue_date);
CREATE INDEX idx_invoice_customer ON invoices(customer_id);

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    recorded_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BANK_TRANSFER'))
);

CREATE INDEX idx_payment_invoice ON payments(invoice_id);
CREATE INDEX idx_payment_date ON payments(payment_date);
CREATE INDEX idx_payment_method ON payments(payment_method);

CREATE TABLE invoice_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    changed_by VARCHAR(100),

    CONSTRAINT fk_history_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE INDEX idx_history_invoice ON invoice_status_history(invoice_id);
