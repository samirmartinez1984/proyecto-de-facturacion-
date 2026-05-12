-- ========================================
-- V1__create_initial_tables.sql
-- Sistema de Facturación - Creación de Tablas Iniciales
-- Fecha: 2026-03-13
-- Descripción: Creación de todas las tablas del sistema según contrato AGENT.md
-- ========================================

-- ========================================
-- TABLA: roles (Entidad Role)
-- ========================================
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL COMMENT 'Nombre del rol, ej: ROLE_ADMIN, ROLE_USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Tabla de roles del sistema de autenticación';

-- ========================================
-- TABLA: users (Entidad User)
-- ========================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT 'Nombre de usuario único',
    password VARCHAR(255) NOT NULL COMMENT 'Contraseña encriptada con BCrypt',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT 'Correo electrónico único',
    enabled BOOLEAN DEFAULT TRUE COMMENT 'Usuario habilitado/deshabilitado',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_username (username),
    INDEX idx_user_email (email),
    INDEX idx_user_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Tabla de usuarios del sistema';

-- ========================================
-- TABLA: user_roles (Relación ManyToMany User-Role)
-- ========================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_user_roles_role_id
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_user_roles_user_id (user_id),
    INDEX idx_user_roles_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Tabla de relación entre usuarios y roles (Many-to-Many)';

-- ========================================
-- TABLA: products (Entidad Product)
-- ========================================
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Nombre del producto',
    description TEXT COMMENT 'Descripción detallada del producto',
    price DECIMAL(10,2) NOT NULL COMMENT 'Precio unitario del producto',
    stock INT DEFAULT 0 COMMENT 'Cantidad en inventario',
    enabled BOOLEAN DEFAULT TRUE COMMENT 'Producto activo/inactivo',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_product_name (name),
    INDEX idx_product_enabled (enabled),
    INDEX idx_product_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Tabla de productos del sistema';

-- ========================================
-- TABLA: invoices (Entidad Invoice)
-- ========================================
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio VARCHAR(50) UNIQUE NOT NULL COMMENT 'Folio único de la factura',
    description TEXT COMMENT 'Descripción de la factura',
    status ENUM('DRAFT', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'DRAFT'
        COMMENT 'Estado de la factura: DRAFT=Borrador, PAID=Pagada, CANCELLED=Cancelada',
    subtotal DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Subtotal sin impuestos',
    tax_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Monto de impuestos (IVA)',
    total DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Total incluyendo impuestos',
    user_id BIGINT NOT NULL COMMENT 'Usuario que creó la factura',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    INDEX idx_invoice_folio (folio),
    INDEX idx_invoice_status (status),
    INDEX idx_invoice_user_id (user_id),
    INDEX idx_invoice_created_at (created_at),
    INDEX idx_invoice_total (total)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Tabla de facturas del sistema';

-- ========================================
-- TABLA: invoice_items (Entidad InvoiceItem/Item)
-- ========================================
CREATE TABLE invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL COMMENT 'Nombre del producto en el momento de la factura',
    quantity INT NOT NULL COMMENT 'Cantidad de productos',
    unit_price DECIMAL(10,2) NOT NULL COMMENT 'Precio unitario en el momento de la factura',
    line_total DECIMAL(10,2) NOT NULL COMMENT 'Total de la línea (quantity * unit_price)',
    invoice_id BIGINT NOT NULL COMMENT 'Factura a la que pertenece el item',

    CONSTRAINT fk_invoice_item_invoice_id
        FOREIGN KEY (invoice_id) REFERENCES invoices(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_invoice_item_invoice_id (invoice_id),
    INDEX idx_invoice_item_product_name (product_name),
    INDEX idx_invoice_item_line_total (line_total),

    CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_unit_price_positive CHECK (unit_price >= 0),
    CONSTRAINT chk_line_total_positive CHECK (line_total >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Tabla de items/líneas de las facturas';

-- ========================================
-- DATOS INICIALES - ROLES
-- ========================================
INSERT INTO roles (name, created_at) VALUES
    ('ROLE_ADMIN', NOW()),
    ('ROLE_USER', NOW());

-- ========================================
-- DATOS INICIALES - USUARIO ADMINISTRADOR
-- ========================================
-- Contraseña: admin123 (encriptada con BCrypt)
-- $2a$10$... es el hash BCrypt para 'admin123'
INSERT INTO users (username, password, email, enabled, created_at) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9dVHNrg2LjY4l8m', 'admin@facturacion.com', TRUE, NOW());

-- ========================================
-- ASIGNAR ROL ADMIN AL USUARIO INICIAL
-- ========================================
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- ========================================
-- COMENTARIOS Y VALIDACIONES
-- ========================================
-- IMPORTANTE:
-- 1. Todas las tablas usan ENGINE=InnoDB para transacciones ACID
-- 2. Charset utf8mb4 para soporte completo de Unicode
-- 3. Índices optimizados para consultas frecuentes
-- 4. Constraints de integridad referencial con CASCADE apropiado
-- 5. Campos obligatorios marcados como NOT NULL según contrato AGENT.md
-- 6. Nombres en snake_case y tablas en plural (estándar del contrato)
-- 7. Auditoría: created_at en todas las tablas principales
