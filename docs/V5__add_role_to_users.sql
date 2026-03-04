-- Add role column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Set admin user role
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@enpractice.com';
