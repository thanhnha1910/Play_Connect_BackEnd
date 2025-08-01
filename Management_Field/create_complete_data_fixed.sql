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

-- Insert Owner record for the Owner user
INSERT INTO owners (business_name, user_id) 
SELECT N'Sân Thể Thao Chuyên Nghiệp', u.id 
FROM users u 
WHERE u.username = 'Owner';

-- Insert basic locations for testing
INSERT INTO locations (name, address, latitude, longitude, owner_id, slug) 
SELECT 
    N'Sân Thể Thao Tân Thới Hiệp',
    N'35/6 Đường TTH15, Khu phố 30, Phường Tân Thới Hiệp, Quận 12, TP.HCM',
    10.8525,
    106.6312,
    o.owner_id,
    'san-the-thao-tan-thoi-hiep'
FROM owners o 
WHERE o.business_name = N'Sân Thể Thao Chuyên Nghiệp';

INSERT INTO locations (name, address, latitude, longitude, owner_id, slug) 
SELECT 
    N'Khu Liên Hợp Thể Thao Quận 12',
    N'123 Đường Lê Văn Khương, Phường Hiệp Thành, Quận 12, TP.HCM',
    10.8528,
    106.6315,
    o.owner_id,
    'khu-lien-hop-the-thao-quan-12'
FROM owners o 
WHERE o.business_name = N'Sân Thể Thao Chuyên Nghiệp';

INSERT INTO locations (name, address, latitude, longitude, owner_id, slug) 
SELECT 
    N'Sân Bóng Đá Mini Thành Đạt',
    N'456 Đường Quang Trung, Phường 10, Quận Gò Vấp, TP.HCM',
    10.8522,
    106.6309,
    o.owner_id,
    'san-bong-da-mini-thanh-dat'
FROM owners o 
WHERE o.business_name = N'Sân Thể Thao Chuyên Nghiệp';

-- Insert basic sports data
INSERT INTO sports (name, sport_code, icon, is_active) VALUES 
(N'Bóng đá', 'FOOTBALL', 'soccer', 1),
(N'Cầu lông', 'BADMINTON', 'badminton', 1),
(N'Tennis', 'TENNIS', 'tennis', 1);

-- Insert basic field types
INSERT INTO field_types (name, team_capacity, max_capacity) VALUES
(N'Sân Bóng Đá 5 Người', 5, 10),
(N'Sân Cầu Lông', 2, 4),
(N'Sân Tennis', 2, 4);

-- Insert basic field categories
INSERT INTO field_categories (name, description, location_id) 
SELECT 
    N'Sân Cỏ Nhân Tạo Cao Cấp',
    N'Sân cỏ nhân tạo chất lượng cao, phù hợp cho các trận đấu chuyên nghiệp và giải trí',
    l.location_id
FROM locations l 
WHERE l.slug = 'san-the-thao-tan-thoi-hiep';

INSERT INTO field_categories (name, description, location_id) 
SELECT 
    N'Khu Sân Trong Nhà',
    N'Các sân thể thao trong nhà có mái che, không bị ảnh hưởng bởi thời tiết',
    l.location_id
FROM locations l 
WHERE l.slug = 'khu-lien-hop-the-thao-quan-12';

-- Insert basic fields
INSERT INTO fields (name, hourly_rate, description, category_id, type_id, location_id, is_active) 
SELECT 
    N'Sân A1 - Cỏ Nhân Tạo',
    280000,
    N'Sân bóng đá 5 người cỏ nhân tạo cao cấp với hệ thống đèn chiếu sáng hiện đại',
    fc.category_id,
    ft.type_id,
    l.location_id,
    1
FROM field_categories fc, field_types ft, locations l
WHERE fc.name = N'Sân Cỏ Nhân Tạo Cao Cấp' 
  AND ft.name = N'Sân Bóng Đá 5 Người'
  AND l.slug = 'san-the-thao-tan-thoi-hiep';

INSERT INTO fields (name, hourly_rate, description, category_id, type_id, location_id, is_active) 
SELECT 
    N'Sân Cầu Lông 1',
    120000,
    N'Sân cầu lông trong nhà với sàn gỗ chuyên nghiệp và điều hòa',
    fc.category_id,
    ft.type_id,
    l.location_id,
    1
FROM field_categories fc, field_types ft, locations l
WHERE fc.name = N'Khu Sân Trong Nhà' 
  AND ft.name = N'Sân Cầu Lông'
  AND l.slug = 'khu-lien-hop-the-thao-quan-12';

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
    STRING_AGG(r.name, ', ') AS roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username IN ('Admin', 'Owner', 'user1')
GROUP BY u.id, u.username, u.email, u.full_name, u.status, u.is_active, u.email_verified, u.provider
ORDER BY u.username;

SELECT 'Owners in database:' as info;
SELECT o.owner_id, o.business_name, u.username, u.full_name 
FROM owners o 
JOIN users u ON o.user_id = u.id;

SELECT 'Locations in database:' as info;
SELECT 
    l.location_id,
    l.name,
    l.address,
    l.slug,
    o.business_name as owner_name
FROM locations l
JOIN owners o ON l.owner_id = o.owner_id
ORDER BY l.location_id;

SELECT 'Sports in database:' as info;
SELECT id, name, sport_code, icon, is_active FROM sports ORDER BY id;

SELECT 'Field Types in database:' as info;
SELECT type_id, name, team_capacity, max_capacity FROM field_types ORDER BY type_id;

SELECT 'Field Categories in database:' as info;
SELECT 
    fc.category_id,
    fc.name,
    fc.description,
    l.name as location_name
FROM field_categories fc
JOIN locations l ON fc.location_id = l.location_id
ORDER BY fc.category_id;

SELECT 'Fields in database:' as info;
SELECT 
    f.field_id,
    f.name,
    f.hourly_rate,
    f.description,
    fc.name as category_name,
    ft.name as type_name,
    l.name as location_name,
    f.is_active
FROM fields f
JOIN field_categories fc ON f.category_id = fc.category_id
JOIN field_types ft ON f.type_id = ft.type_id
JOIN locations l ON f.location_id = l.location_id
ORDER BY f.field_id;

-- Login credentials:
-- Admin: Admin@gmail.com / 123123a (username: Admin)
-- Owner: Owner@gmail.com / 123123a (username: Owner)
-- User: user1@gmail.com / 123123a (username: user1)
-- All accounts are ACTIVE and email verified.
-- 
-- Sample locations created:
-- 1. Sân Thể Thao Tân Thới Hiệp (slug: san-the-thao-tan-thoi-hiep)
-- 2. Khu Liên Hợp Thể Thao Quận 12 (slug: khu-lien-hop-the-thao-quan-12)
-- 3. Sân Bóng Đá Mini Thành Đạt (slug: san-bong-da-mini-thanh-dat)
-- 
-- Sample fields created:
-- 1. Sân A1 - Cỏ Nhân Tạo (Bóng đá 5 người - 280,000 VND/giờ)
-- 2. Sân Cầu Lông 1 (Cầu lông - 120,000 VND/giờ)