-- Migration V2: Add discount_amount column if it doesn't exist
-- This is a safety migration in case the column was not created by V1

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'invoices' 
        AND column_name = 'discount_amount'
    ) THEN
        ALTER TABLE invoices 
        ADD COLUMN discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0;
        
        RAISE NOTICE 'Column discount_amount added to invoices table';
    ELSE
        RAISE NOTICE 'Column discount_amount already exists in invoices table';
    END IF;
END $$;

