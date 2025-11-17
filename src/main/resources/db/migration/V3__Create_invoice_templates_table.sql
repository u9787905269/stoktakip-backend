-- Migration V3: Create invoice_templates table
-- This migration creates the invoice_templates table for custom invoice templates

CREATE TABLE IF NOT EXISTS invoice_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    is_default BOOLEAN DEFAULT FALSE,
    header_html TEXT,
    footer_html TEXT,
    logo_url VARCHAR(500),
    primary_color VARCHAR(50) DEFAULT '#000000',
    secondary_color VARCHAR(50) DEFAULT '#666666',
    font_family VARCHAR(100) DEFAULT 'Arial',
    font_size INTEGER DEFAULT 12,
    show_logo BOOLEAN DEFAULT TRUE,
    show_border BOOLEAN DEFAULT TRUE,
    show_qr_code BOOLEAN DEFAULT FALSE,
    custom_fields TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_invoice_templates_is_default ON invoice_templates(is_default);

