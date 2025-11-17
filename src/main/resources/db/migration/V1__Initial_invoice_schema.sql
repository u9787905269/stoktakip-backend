-- Migration V1: Initial invoice schema
-- This migration creates the invoices and invoice_items tables
-- Flyway will run this automatically on application startup

-- Create invoices table if it doesn't exist
CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    invoice_date DATE NOT NULL,
    due_date DATE,
    customer_name VARCHAR(255),
    customer_address VARCHAR(500),
    customer_tax_number VARCHAR(100),
    customer_email VARCHAR(255),
    customer_phone VARCHAR(100),
    seller_name VARCHAR(255),
    seller_address VARCHAR(500),
    seller_tax_number VARCHAR(100),
    seller_email VARCHAR(255),
    seller_phone VARCHAR(100),
    seller_bank_account VARCHAR(255),
    subtotal NUMERIC(12, 2) NOT NULL DEFAULT 0,
    tax_rate INTEGER NOT NULL DEFAULT 0,
    tax_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(1000),
    terms VARCHAR(1000),
    status VARCHAR(50) DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create invoice_items table if it doesn't exist
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    item_number INTEGER NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_code VARCHAR(100),
    description VARCHAR(500),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    tax_rate INTEGER NOT NULL DEFAULT 0,
    discount_percent NUMERIC(5, 2) NOT NULL DEFAULT 0,
    subtotal NUMERIC(12, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_invoices_invoice_number ON invoices(invoice_number);
CREATE INDEX IF NOT EXISTS idx_invoices_invoice_date ON invoices(invoice_date);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice_id ON invoice_items(invoice_id);

