-- ============================================================
-- Lab 11: Database Setup Script for User Login Validation
-- ============================================================
-- Run this script in MySQL before starting the application:
--   mysql -u root -p < setup_db.sql
-- ============================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS user_login_db;
USE user_login_db;

-- Create the users table
CREATE TABLE IF NOT EXISTS users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    fullname VARCHAR(100) NOT NULL
);

-- Insert sample user records
INSERT INTO users (username, password, fullname) VALUES
    ('admin', 'admin123',   'Administrator'),
    ('john',  'pass456',    'John Smith'),
    ('jane',  'secret789',  'Jane Doe')
ON DUPLICATE KEY UPDATE password = VALUES(password);

-- Verify the data
SELECT * FROM users;

-- ============================================================
-- Expected output:
-- +----+----------+-----------+---------------+
-- | id | username | password  | fullname      |
-- +----+----------+-----------+---------------+
-- |  1 | admin    | admin123  | Administrator |
-- |  2 | john     | pass456   | John Smith    |
-- |  3 | jane     | secret789 | Jane Doe      |
-- +----+----------+-----------+---------------+
-- ============================================================
