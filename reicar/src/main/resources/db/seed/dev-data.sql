/*
 * Reicar - Development Seed Data
 * This script populates the database with realistic test data for development.
 * Run with: make seed
 */

/* Customers - Realistic Brazilian names and addresses */
INSERT IGNORE INTO customers (id, name, phone, city, state) VALUES
(1, 'João Carlos da Silva', '(61) 99123-4567', 'Sol Nascente', 'DF'),
(2, 'Maria Fernanda Oliveira', '(61) 98765-4321', 'Ceilândia', 'DF'),
(3, 'Pedro Henrique Santos', '(61) 99876-5432', 'Taguatinga', 'DF'),
(4, 'Ana Paula Costa', '(61) 98234-5678', 'Samambaia', 'DF'),
(5, 'Carlos Eduardo Ferreira', '(61) 99345-6789', 'Águas Claras', 'DF'),
(6, 'Fernanda Rodrigues Lima', '(61) 98456-7890', 'Vicente Pires', 'DF'),
(7, 'Ricardo Almeida Souza', '(61) 99567-8901', 'Riacho Fundo', 'DF'),
(8, 'Juliana Martins Pereira', '(61) 98678-9012', 'Recanto das Emas', 'DF'),
(9, 'Marcos Vinícius Barbosa', '(61) 99789-0123', 'Santa Maria', 'DF'),
(10, 'Camila Beatriz Nascimento', '(61) 98890-1234', 'Gama', 'DF');

/* Service Orders - Mix of MECHANIC and TIRE_SHOP types */
INSERT IGNORE INTO service_orders (id, order_number, customer_id, service_type, vehicle_km, tire_position, service_value, status, entry_date) VALUES
(1, 'OS-2026-0001', 1, 'MECHANIC', 45230, NULL, 350.00, 'DONE', DATE_SUB(CURDATE(), INTERVAL 30 DAY)),
(2, 'OS-2026-0002', 2, 'TIRE_SHOP', NULL, 'Dianteiro Esquerdo', 80.00, 'DONE', DATE_SUB(CURDATE(), INTERVAL 28 DAY)),
(3, 'OS-2026-0003', 3, 'MECHANIC', 78500, NULL, 1200.00, 'DONE', DATE_SUB(CURDATE(), INTERVAL 25 DAY)),
(4, 'OS-2026-0004', 4, 'TIRE_SHOP', NULL, 'Traseiro Direito', 120.00, 'DONE', DATE_SUB(CURDATE(), INTERVAL 20 DAY)),
(5, 'OS-2026-0005', 5, 'MECHANIC', 32100, NULL, 580.00, 'DONE', DATE_SUB(CURDATE(), INTERVAL 15 DAY)),
(6, 'OS-2026-0006', 1, 'MECHANIC', 45890, NULL, 450.00, 'DONE', DATE_SUB(CURDATE(), INTERVAL 10 DAY)),
(7, 'OS-2026-0007', 6, 'TIRE_SHOP', NULL, 'Dianteiro Direito', 150.00, 'DONE', DATE_SUB(CURDATE(), INTERVAL 7 DAY)),
(8, 'OS-2026-0008', 7, 'MECHANIC', 89200, NULL, 2500.00, 'DONE', DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(9, 'OS-2026-0009', 8, 'MECHANIC', 56700, NULL, 380.00, 'OPEN', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(10, 'OS-2026-0010', 9, 'TIRE_SHOP', NULL, 'Todos (4 pneus)', 1600.00, 'OPEN', DATE_SUB(CURDATE(), INTERVAL 1 DAY));

/* Service Order Items - Parts and labor */
INSERT IGNORE INTO service_items (id, service_order_id, quantity, description, unit_price) VALUES
(1, 1, 1, 'Filtro de óleo', 45.00),
(2, 1, 4, 'Litros de óleo 5W30', 42.00),
(3, 1, 1, 'Filtro de ar', 65.00),
(4, 2, 1, 'Conserto de pneu 195/65R15', 80.00),
(5, 3, 1, 'Kit de embreagem completo', 850.00),
(6, 3, 1, 'Rolamento de embreagem', 120.00),
(7, 4, 1, 'Pneu 175/70R14 Remold', 120.00),
(8, 5, 2, 'Pastilhas de freio dianteiras', 89.00),
(9, 5, 2, 'Discos de freio dianteiros', 145.00),
(10, 6, 1, 'Bomba dágua', 180.00),
(11, 6, 1, 'Correia dentada', 95.00),
(12, 6, 1, 'Tensor da correia', 75.00),
(13, 7, 1, 'Balanceamento e alinhamento', 150.00),
(14, 8, 1, 'Junta do cabeçote', 450.00),
(15, 8, 1, 'Retífica do cabeçote', 800.00),
(16, 8, 1, 'Kit de juntas', 320.00),
(17, 9, 1, 'Amortecedor dianteiro esquerdo', 280.00),
(18, 9, 1, 'Coifa do amortecedor', 45.00),
(19, 10, 4, 'Pneu 185/65R15 Pirelli P1', 350.00);

/* Invoices for completed service orders */
INSERT IGNORE INTO invoices (id, service_order_id, customer_id, invoice_number, issue_date, total_value, paid_amount, status) VALUES
(1, 1, 1, 'FAT-2026-0001', DATE_SUB(CURDATE(), INTERVAL 30 DAY), 568.00, 568.00, 'PAID'),
(2, 2, 2, 'FAT-2026-0002', DATE_SUB(CURDATE(), INTERVAL 28 DAY), 80.00, 80.00, 'PAID'),
(3, 3, 3, 'FAT-2026-0003', DATE_SUB(CURDATE(), INTERVAL 25 DAY), 2461.00, 2461.00, 'PAID'),
(4, 4, 4, 'FAT-2026-0004', DATE_SUB(CURDATE(), INTERVAL 20 DAY), 120.00, 120.00, 'PAID'),
(5, 5, 5, 'FAT-2026-0005', DATE_SUB(CURDATE(), INTERVAL 15 DAY), 1048.00, 500.00, 'PARTIAL'),
(6, 6, 1, 'FAT-2026-0006', DATE_SUB(CURDATE(), INTERVAL 10 DAY), 905.00, 0.00, 'UNPAID'),
(7, 7, 6, 'FAT-2026-0007', DATE_SUB(CURDATE(), INTERVAL 7 DAY), 150.00, 150.00, 'PAID'),
(8, 8, 7, 'FAT-2026-0008', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 4541.00, 2000.00, 'PARTIAL');

/* Payments */
INSERT IGNORE INTO payments (id, invoice_id, amount, payment_method, payment_date, recorded_by) VALUES
(1, 1, 568.00, 'PIX', DATE_SUB(NOW(), INTERVAL 29 DAY), 'admin'),
(2, 2, 80.00, 'CASH', DATE_SUB(NOW(), INTERVAL 28 DAY), 'admin'),
(3, 3, 1500.00, 'CREDIT_CARD', DATE_SUB(NOW(), INTERVAL 24 DAY), 'admin'),
(4, 3, 961.00, 'PIX', DATE_SUB(NOW(), INTERVAL 20 DAY), 'admin'),
(5, 4, 120.00, 'DEBIT_CARD', DATE_SUB(NOW(), INTERVAL 20 DAY), 'admin'),
(6, 5, 500.00, 'CASH', DATE_SUB(NOW(), INTERVAL 14 DAY), 'admin'),
(7, 7, 150.00, 'PIX', DATE_SUB(NOW(), INTERVAL 6 DAY), 'admin'),
(8, 8, 2000.00, 'CREDIT_CARD', DATE_SUB(NOW(), INTERVAL 4 DAY), 'admin');

/* Customer Users - Link some customers to user accounts */
INSERT IGNORE INTO users (id, username, password, role, enabled, customer_id) VALUES
(2, 'joao.silva', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqIiThq8Nva.o.aGrJVFPbT3Y.OBY8G', 'CUSTOMER', true, 1),
(3, 'maria.oliveira', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqIiThq8Nva.o.aGrJVFPbT3Y.OBY8G', 'CUSTOMER', true, 2),
(4, 'mecanico', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqIiThq8Nva.o.aGrJVFPbT3Y.OBY8G', 'MECHANIC', true, NULL);

SELECT 'Seed data inserted successfully!' AS status;
