/*
================================================================
PLAYERCONNECT - ROBUST DATABASE CLEAN & SEED SCRIPT
================================================================
Based on actual model structure from Java entities
*/

USE Project4_VN;
GO

PRINT '--- Phase 1: Cleaning all data in correct dependency order ---';

-- Start from tables that depend on others the most
DELETE FROM dbo.booking_users;
DELETE FROM dbo.bookings;
DELETE FROM dbo.field_closures;
DELETE FROM dbo.fields;
DELETE FROM dbo.field_categories;
DELETE FROM dbo.field_types;
DELETE FROM dbo.tournaments;
DELETE FROM dbo.teams;
DELETE FROM dbo.tags;
DELETE FROM dbo.locations;
DELETE FROM dbo.owners;
DELETE FROM dbo.user_roles;
DELETE FROM dbo.users;
DELETE FROM dbo.roles;
DELETE FROM dbo.sports;
DELETE FROM dbo.system_settings;
GO

PRINT '--- Phase 2: Reseeding identity columns ---';
DBCC CHECKIDENT ('dbo.users', RESEED, 0);
DBCC CHECKIDENT ('dbo.roles', RESEED, 0);
DBCC CHECKIDENT ('dbo.sports', RESEED, 0);
DBCC CHECKIDENT ('dbo.owners', RESEED, 0);
DBCC CHECKIDENT ('dbo.locations', RESEED, 0);
DBCC CHECKIDENT ('dbo.field_types', RESEED, 0);
DBCC CHECKIDENT ('dbo.field_categories', RESEED, 0);
DBCC CHECKIDENT ('dbo.fields', RESEED, 0);
DBCC CHECKIDENT ('dbo.teams', RESEED, 0);
DBCC CHECKIDENT ('dbo.tournaments', RESEED, 0);
DBCC CHECKIDENT ('dbo.bookings', RESEED, 0);
DBCC CHECKIDENT ('dbo.tags', RESEED, 0);
DBCC CHECKIDENT ('dbo.system_settings', RESEED, 0);
GO

PRINT '--- Phase 3: Inserting Seed Data ---';

-- Insert Roles
INSERT INTO roles (name) VALUES 
('ROLE_USER'),
('ROLE_OWNER'),
('ROLE_ADMIN');
GO

-- Insert Sports (with required fields: sport_code, is_active)
INSERT INTO sports (name, sport_code, icon, is_active) VALUES 
(N'Bóng đá', 'FOOTBALL', 'soccer', 1),
(N'Cầu lông', 'BADMINTON', 'badminton', 1),
(N'Tennis', 'TENNIS', 'tennis', 1),
(N'Bóng rổ', 'BASKETBALL', 'basketball', 1),
(N'Bóng chuyền', 'VOLLEYBALL', 'volleyball', 1);
GO

-- Insert Users (with required fields: email_verified, provider)
INSERT INTO users (username, email, password, full_name, phone_number, address, provider, provider_id, is_active, email_verified, status, join_date, is_discoverable, has_completed_profile) VALUES 
('admin', 'admin@playerconnect.vn', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', N'Quản trị viên', '0901234567', N'TP.HCM', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1),
('nguyenvana', 'nguyenvana@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', N'Nguyễn Văn A', '0912345678', N'Quận 1, TP.HCM', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1),
('tranthib', 'tranthib@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', N'Trần Thị B', '0923456789', N'Quận 2, TP.HCM', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1),
('lequangc', 'lequangc@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', N'Lê Quang C', '0934567890', N'Quận 12, TP.HCM', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1),
('phamthid', 'phamthid@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', N'Phạm Thị D', '0945678901', N'Quận 3, TP.HCM', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1),
('hoangvane', 'hoangvane@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', N'Hoàng Văn E', '0956789012', N'Gò Vấp, TP.HCM', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1),
('johnsmith', 'john.smith@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'John Smith', '0967890123', 'District 7, HCMC', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1),
('maryjohnson', 'mary.johnson@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Mary Johnson', '0978901234', 'District 2, HCMC', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1),
('davidbrown', 'david.brown@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'David Brown', '0989012345', 'District 3, HCMC', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1),
('sarahwilson', 'sarah.wilson@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Sarah Wilson', '0990123456', 'District 1, HCMC', 'LOCAL', NULL, 1, 1, 'ACTIVE', GETDATE(), 1, 1);
GO

-- Insert User Roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 3), -- admin has ROLE_ADMIN
(2, 1), -- nguyenvana has ROLE_USER
(3, 1), -- tranthib has ROLE_USER
(4, 2), -- lequangc has ROLE_OWNER
(5, 1), -- phamthid has ROLE_USER
(6, 2), -- hoangvane has ROLE_OWNER
(7, 1), -- johnsmith has ROLE_USER
(8, 1), -- maryjohnson has ROLE_USER
(9, 2), -- davidbrown has ROLE_OWNER
(10, 1); -- sarahwilson has ROLE_USER
GO

-- Insert Owners (only business_name and user_id)
INSERT INTO owners (business_name, user_id) VALUES 
(N'Công ty TNHH Thể Thao Quang C', 4),
(N'Doanh nghiệp Thể Thao Hoàng E', 6),
('Sports Complex Brown Ltd', 9),
(N'PlayerConnect Admin', 1);
GO

-- Insert Locations with unique slugs (using owner_id from owners table)
INSERT INTO locations (name, address, latitude, longitude, owner_id, slug) VALUES 
(N'Sân Thể Thao Tân Thới Hiệp', N'35/6 Đường TTH15, Phường Tân Thới Hiệp, Quận 12, TP.HCM', 10.8525, 106.6312, 1, 'san-the-thao-tan-thoi-hiep'),
(N'Sân Bóng Quận 12', N'123 Đường Lê Văn Khương, Phường Hiệp Thành, Quận 12, TP.HCM', 10.8456, 106.6234, 2, 'san-bong-quan-12'),
(N'Sân Thể Thao Thành Đạt', N'789 Đường Nguyễn Ảnh Thủ, Phường Trung Mỹ Tây, Quận 12, TP.HCM', 10.8398, 106.6189, 3, 'san-the-thao-thanh-dat'),
(N'Sân Bóng Đá Mini Hòa Bình', N'456 Đường Tô Ký, Phường Tân Chánh Hiệp, Quận 12, TP.HCM', 10.8567, 106.6345, 4, 'san-bong-da-mini-hoa-binh'),
(N'Sân Tennis Luxury', N'321 Đường Phan Văn Hớn, Phường Tân Thới Nhất, Quận 12, TP.HCM', 10.8234, 106.6123, 1, 'san-tennis-luxury'),
(N'Sân Cầu Lông Vip', N'654 Đường Quang Trung, Phường 8, Quận Gò Vấp, TP.HCM', 10.8345, 106.6456, 2, 'san-cau-long-vip'),
(N'Sân Bóng Rổ Chuyên Nghiệp', N'987 Đường Nguyễn Văn Lượng, Phường 17, Quận Gò Vấp, TP.HCM', 10.8123, 106.6567, 3, 'san-bong-ro-chuyen-nghiep'),
(N'Sân Thể Thao Đa Năng', N'147 Đường Phạm Văn Đồng, Phường Linh Tây, Thành phố Thủ Đức, TP.HCM', 10.8678, 106.6789, 4, 'san-the-thao-da-nang'),
('Sports Complex International', '258 International Boulevard, District 7, HCMC', 10.7456, 106.7123, 1, 'sports-complex-international'),
('Elite Football Center', '369 Elite Street, District 2, HCMC', 10.7789, 106.7456, 2, 'elite-football-center');
GO

-- Insert Field Types (using correct column names)
INSERT INTO field_types (name, team_capacity, max_capacity) VALUES 
(N'Sân Bóng Đá 5 Người', 5, 10),
(N'Sân Bóng Đá 7 Người', 7, 14),
(N'Sân Bóng Đá 11 Người', 11, 22),
(N'Sân Cầu Lông Đơn', 1, 2),
(N'Sân Cầu Lông Đôi', 2, 4),
(N'Sân Tennis Đơn', 1, 2),
(N'Sân Tennis Đôi', 2, 4),
(N'Sân Bóng Rổ Nửa Sân', 3, 6),
(N'Sân Bóng Rổ Toàn Sân', 5, 10),
(N'Sân Bóng Chuyền 6 Người', 6, 12),
(N'Sân Bóng Chuyền Bãi Biển', 2, 4);
GO

-- Insert Field Categories (with location_id)
INSERT INTO field_categories (name, description, location_id) VALUES 
(N'Sân Cỏ Nhân Tạo Cao Cấp', N'Sân cỏ nhân tạo chất lượng cao, phù hợp cho thi đấu chuyên nghiệp', 1),
(N'Sân Cỏ Nhân Tạo Tiêu Chuẩn', N'Sân cỏ nhân tạo chất lượng tốt, phù hợp cho giải trí', 2),
(N'Sân Cỏ Tự Nhiên', N'Sân cỏ tự nhiên, mang lại cảm giác chân thực', 3),
(N'Khu Sân Trong Nhà', N'Sân được xây dựng trong nhà, không bị ảnh hưởng thời tiết', 4),
(N'Khu Sân Ngoài Trời', N'Sân ngoài trời, thoáng mát và rộng rãi', 5),
(N'Sân VIP', N'Sân cao cấp với đầy đủ tiện nghi', 6),
(N'Sân Tiêu Chuẩn', N'Sân với chất lượng tiêu chuẩn, giá cả hợp lý', 7),
(N'Sân Mini', N'Sân nhỏ gọn, phù hợp cho nhóm ít người', 8);
GO

-- Insert Fields (using correct column names: category_id, type_id, location_id)
INSERT INTO fields (name, location_id, category_id, type_id, hourly_rate, description, is_active, created_at) VALUES 
(N'Sân A1 - Cỏ Nhân Tạo', 1, 1, 1, 150000, N'Sân bóng đá 5 người với cỏ nhân tạo cao cấp', 1, GETDATE()),
(N'Sân A2 - Cỏ Nhân Tạo', 1, 2, 1, 120000, N'Sân bóng đá 5 người với cỏ nhân tạo tiêu chuẩn', 1, GETDATE()),
(N'Sân Cầu Lông 1', 1, 4, 4, 80000, N'Sân cầu lông đơn trong nhà, có điều hòa', 1, GETDATE()),
(N'Sân Cầu Lông 2', 1, 4, 5, 100000, N'Sân cầu lông đôi trong nhà, có điều hòa', 1, GETDATE()),
(N'Sân B1 - VIP', 2, 6, 2, 200000, N'Sân bóng đá 7 người VIP với đầy đủ tiện nghi', 1, GETDATE()),
(N'Sân B2 - Tiêu Chuẩn', 2, 7, 1, 130000, N'Sân bóng đá 5 người tiêu chuẩn', 1, GETDATE()),
(N'Sân Tennis 1', 3, 5, 6, 180000, N'Sân tennis đơn ngoài trời', 1, GETDATE()),
(N'Sân Tennis 2', 3, 5, 7, 220000, N'Sân tennis đôi ngoài trời', 1, GETDATE()),
(N'Sân C1 - Mini', 4, 8, 1, 100000, N'Sân bóng đá 5 người mini', 1, GETDATE()),
(N'Sân C2 - Mini', 4, 8, 1, 100000, N'Sân bóng đá 5 người mini', 1, GETDATE()),
(N'Sân Tennis VIP', 5, 6, 6, 250000, N'Sân tennis đơn VIP với dịch vụ cao cấp', 1, GETDATE()),
(N'Sân Cầu Lông VIP 1', 6, 6, 4, 120000, N'Sân cầu lông đơn VIP', 1, GETDATE()),
(N'Sân Cầu Lông VIP 2', 6, 6, 5, 150000, N'Sân cầu lông đôi VIP', 1, GETDATE()),
(N'Sân Bóng Rổ 1', 7, 5, 8, 160000, N'Sân bóng rổ nửa sân ngoài trời', 1, GETDATE()),
(N'Sân Bóng Rổ 2', 7, 4, 9, 200000, N'Sân bóng rổ toàn sân trong nhà', 1, GETDATE()),
(N'Sân Đa Năng 1', 8, 1, 1, 180000, N'Sân đa năng có thể chơi nhiều môn thể thao', 1, GETDATE()),
(N'Sân Đa Năng 2', 8, 1, 2, 220000, N'Sân đa năng lớn với cỏ nhân tạo cao cấp', 1, GETDATE()),
('Football Field A', 9, 1, 3, 300000, 'Professional 11-player football field', 1, GETDATE()),
('Tennis Court Premium', 9, 6, 6, 280000, 'Premium tennis court with professional equipment', 1, GETDATE()),
('Elite Football Mini', 10, 6, 1, 200000, 'Elite 5-player football field with VIP amenities', 1, GETDATE());
GO

-- Insert Teams (using correct column names)
INSERT INTO teams (name, code, created_at) VALUES 
(N'Đội Bóng Tân Thới Hiệp', 'TTH01', GETDATE()),
(N'FC Quận 12', 'Q12FC', GETDATE()),
(N'Cầu Lông Thành Đạt', 'CLTD', GETDATE()),
(N'Tennis Club VIP', 'TCVIP', GETDATE()),
(N'Bóng Rổ Gò Vấp', 'BRGV', GETDATE()),
('International Football Team', 'IFT01', GETDATE()),
('Elite Badminton Club', 'EBC01', GETDATE()),
('Tennis Masters', 'TM01', GETDATE());
GO

-- Insert Tournaments (using correct column names)
INSERT INTO tournaments (name, prize, total_participants, location_id) VALUES 
(N'Giải Bóng Đá Mùa Hè 2024', 10000000, 16, 1),
(N'Giải Cầu Lông Mở Rộng', 5000000, 32, 6),
(N'Giải Tennis Chuyên Nghiệp', 20000000, 8, 5),
('Summer Football Championship', 15000000, 12, 9),
('Badminton Open 2024', 8000000, 24, 6);
GO

-- Insert Bookings (using correct column names and data types)
INSERT INTO bookings (user_id, field_id, from_time, to_time, slots, status, payment_token, created_at, reminder_sent) VALUES 
(2, 1, '2024-02-15T08:00:00Z', '2024-02-15T10:00:00Z', 2, 'confirmed', 'PAY001', GETDATE(), 0),
(3, 3, '2024-02-16T14:00:00Z', '2024-02-16T16:00:00Z', 2, 'confirmed', 'PAY002', GETDATE(), 0),
(5, 7, '2024-02-17T09:00:00Z', '2024-02-17T11:00:00Z', 2, 'pending', 'PAY003', GETDATE(), 0),
(7, 11, '2024-02-18T16:00:00Z', '2024-02-18T18:00:00Z', 2, 'confirmed', 'PAY004', GETDATE(), 0),
(8, 15, '2024-02-19T10:00:00Z', '2024-02-19T12:00:00Z', 2, 'confirmed', 'PAY005', GETDATE(), 0),
(10, 19, '2024-02-20T15:00:00Z', '2024-02-20T17:00:00Z', 2, 'pending', 'PAY006', GETDATE(), 0);
GO

-- Insert Tags (with sport_id and is_active)
INSERT INTO tags (name, sport_id, is_active) VALUES 
(N'Bóng đá 5 người', 1, 1),
(N'Bóng đá 7 người', 1, 1),
(N'Bóng đá 11 người', 1, 1),
(N'Cầu lông đơn', 2, 1),
(N'Cầu lông đôi', 2, 1),
(N'Tennis đơn', 3, 1),
(N'Tennis đôi', 3, 1),
(N'Bóng rổ', 4, 1),
(N'Bóng chuyền', 5, 1),
(N'Thể thao giải trí', NULL, 1),
(N'Thể thao chuyên nghiệp', NULL, 1),
(N'Sân trong nhà', NULL, 1),
(N'Sân ngoài trời', NULL, 1),
(N'Sân VIP', NULL, 1),
(N'Giá rẻ', NULL, 1),
('Indoor Sports', NULL, 1),
('Outdoor Sports', NULL, 1),
('Professional', NULL, 1),
('Recreational', NULL, 1),
('Premium', NULL, 1);
GO

-- Insert System Settings (using correct column names)
INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES 
('BOOKING_ADVANCE_DAYS', '30', 'INTEGER', N'Số ngày tối đa có thể đặt sân trước'),
('CANCELLATION_HOURS', '24', 'INTEGER', N'Số giờ tối thiểu để hủy booking'),
('DEFAULT_BOOKING_DURATION', '2', 'INTEGER', N'Thời gian đặt sân mặc định (giờ)'),
('MAX_BOOKING_DURATION', '8', 'INTEGER', N'Thời gian đặt sân tối đa (giờ)'),
('PLATFORM_FEE_PERCENTAGE', '5', 'INTEGER', N'Phần trăm phí nền tảng'),
('REFUND_PERCENTAGE', '80', 'INTEGER', N'Phần trăm hoàn tiền khi hủy booking'),
('NOTIFICATION_EMAIL_ENABLED', 'true', 'BOOLEAN', N'Bật/tắt thông báo email'),
('NOTIFICATION_SMS_ENABLED', 'false', 'BOOLEAN', N'Bật/tắt thông báo SMS'),
('MAINTENANCE_MODE', 'false', 'BOOLEAN', N'Chế độ bảo trì hệ thống'),
('CURRENCY', 'VND', 'STRING', N'Đơn vị tiền tệ'),
('TIMEZONE', 'Asia/Ho_Chi_Minh', 'STRING', N'Múi giờ hệ thống');
GO

PRINT '--- Phase 4: Verification Queries ---';

-- Verify data insertion
SELECT 'Roles' as TableName, COUNT(*) as RecordCount FROM roles
UNION ALL
SELECT 'Sports', COUNT(*) FROM sports
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'Owners', COUNT(*) FROM owners
UNION ALL
SELECT 'Locations', COUNT(*) FROM locations
UNION ALL
SELECT 'Field Types', COUNT(*) FROM field_types
UNION ALL
SELECT 'Field Categories', COUNT(*) FROM field_categories
UNION ALL
SELECT 'Fields', COUNT(*) FROM fields
UNION ALL
SELECT 'Teams', COUNT(*) FROM teams
UNION ALL
SELECT 'Tournaments', COUNT(*) FROM tournaments
UNION ALL
SELECT 'Bookings', COUNT(*) FROM bookings
UNION ALL
SELECT 'Tags', COUNT(*) FROM tags
UNION ALL
SELECT 'System Settings', COUNT(*) FROM system_settings;
GO

-- Verify unique slugs for locations
SELECT 'Location Slugs Verification' as Info, name, slug FROM locations ORDER BY location_id;
GO

-- Check for duplicate slugs (should return 0 rows)
SELECT 'Duplicate Slugs Check' as Info, slug, COUNT(*) as DuplicateCount 
FROM locations 
GROUP BY slug 
HAVING COUNT(*) > 1;
GO

-- Verify field relationships
SELECT 'Field Relationships Check' as Info, 
       f.name as FieldName,
       l.name as LocationName,
       fc.name as CategoryName,
       ft.name as TypeName
FROM fields f
JOIN locations l ON f.location_id = l.location_id
JOIN field_categories fc ON f.category_id = fc.category_id
JOIN field_types ft ON f.type_id = ft.type_id
ORDER BY f.field_id;
GO

PRINT '--- SCRIPT COMPLETED SUCCESSFULLY ---';
GO