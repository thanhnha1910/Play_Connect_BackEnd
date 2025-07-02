-- Fixed script to insert roles and users with correct BCrypt password hashes
-- This script will create all necessary roles and users for the application

-- Clear existing data first to avoid conflicts
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM roles;

-- Insert Roles first
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_OWNER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Insert Admin User
INSERT INTO users (
    username, 
    password, 
    email, 
    full_name, 
    phone_number, 
    address, 
    provider, 
    provider_id,
    is_active, 
    email_verified, 
    verification_token,
    verification_token_expiry,
    reset_password_token,
    reset_password_token_expiry,
    status
) VALUES (
    'Admin',
    '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', -- password: 123123a
    'Admin@gmail.com',
    'System Administrator',
    '+84901234567',
    'Ho Chi Minh City, Vietnam',
    'LOCAL',
    NULL,
    1, -- is_active = true
    1, -- email_verified = true
    NULL, -- verification_token
    NULL, -- verification_token_expiry
    NULL, -- reset_password_token
    NULL, -- reset_password_token_expiry
    'ACTIVE'
);

-- Insert Owner User (Already Approved)
INSERT INTO users (
    username, 
    password, 
    email, 
    full_name, 
    phone_number, 
    address, 
    provider, 
    provider_id,
    is_active, 
    email_verified, 
    verification_token,
    verification_token_expiry,
    reset_password_token,
    reset_password_token_expiry,
    status
) VALUES (
    'Owner',
    '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', -- password: 123123a
    'Owner@gmail.com',
    'Field Owner',
    '+84907654321',
    'District 12, Ho Chi Minh City, Vietnam',
    'LOCAL',
    NULL,
    1, -- is_active = true
    1, -- email_verified = true
    NULL, -- verification_token
    NULL, -- verification_token_expiry
    NULL, -- reset_password_token
    NULL, -- reset_password_token_expiry
    'ACTIVE' -- Already approved, not PENDING_APPROVAL
);

-- Insert Regular User
INSERT INTO users (
    username, 
    password, 
    email, 
    full_name, 
    phone_number, 
    address, 
    provider, 
    provider_id,
    is_active, 
    email_verified, 
    verification_token,
    verification_token_expiry,
    reset_password_token,
    reset_password_token_expiry,
    status
) VALUES (
    'user1',
    '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', -- password: 123123a
    'user1@gmail.com',
    'Regular User',
    '+84909876543',
    'Ho Chi Minh City, Vietnam',
    'LOCAL',
    NULL,
    1, -- is_active = true
    1, -- email_verified = true
    NULL, -- verification_token
    NULL, -- verification_token_expiry
    NULL, -- reset_password_token
    NULL, -- reset_password_token_expiry
    'ACTIVE'
);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'Admin' AND r.name = 'ROLE_ADMIN';

-- Assign OWNER role to owner user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'Owner' AND r.name = 'ROLE_OWNER';

-- Assign USER role to regular user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'user1' AND r.name = 'ROLE_USER';

-- Verify the data was inserted successfully
SELECT 'Roles in database:' as info;
SELECT id, name FROM roles ORDER BY id;

SELECT 'Users and their roles:' as info;
SELECT 
    u.id,
    u.username,
    u.email,
    u.full_name,
    u.status,
    u.is_active,
    u.email_verified,
    u.provider,
    GROUP_CONCAT(r.name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username IN ('Admin', 'Owner', 'user1')
GROUP BY u.id, u.username, u.email, u.full_name, u.status, u.is_active, u.email_verified, u.provider
ORDER BY u.username;

-- Login credentials:
-- Admin: Admin@gmail.com / 123123a (username: Admin)
-- Owner: Owner@gmail.com / 123123a (username: Owner)
-- User: user1@gmail.com / 123123a (username: user1)
-- All accounts are ACTIVE and email verified.