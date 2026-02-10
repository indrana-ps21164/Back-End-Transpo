-- Flyway migration to add missing columns to reservations
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS paid BOOLEAN DEFAULT FALSE;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS payment_method VARCHAR(64);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(128);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS username VARCHAR(128);
