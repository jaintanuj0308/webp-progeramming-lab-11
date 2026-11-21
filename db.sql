-- ========================================================
-- Lab 11: User Validation Web Application - MySQL Database
-- ========================================================

CREATE DATABASE IF NOT EXISTS lab11_db;
USE lab11_db;

-- Drop table if exists for fresh creation
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    fullname VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'User'
);

-- Insert sample users
INSERT INTO users (username, password, fullname, role) VALUES
('admin', 'admin123', 'System Administrator', 'Administrator'),
('john',  'pass456',  'John Doe',             'Student'),
('alice', 'secret789','Alice Johnson',         'Faculty');

-- Verify insertion
SELECT * FROM users;
