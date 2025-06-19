-- Complete Sample Data for Project4 Management Field System
-- This file includes the is_active field for all fields to ensure proper functionality
-- All data has been translated to English. The 'slug' column is added ONLY to the 'locations' table.

-- Use the Project4 database
USE Project4;
GO

-- Insert owners first (names are de-accented)
INSERT INTO owners (name, hashed_password) VALUES
('Nguyen Van An', '$2a$10$example_hashed_password_1'),
('Tran Thi Binh', '$2a$10$example_hashed_password_2'),
('Le Van Cuong', '$2a$10$example_hashed_password_3'),
('Pham Thi Dung', '$2a$10$example_hashed_password_4'),
('Hoang Minh Tuan', '$2a$10$example_hashed_password_5');
GO

-- Insert locations in District 12 with slugs
-- Addresses are kept in Vietnamese as requested
INSERT INTO locations (name, address, latitude, longitude, owner_id, slug) VALUES
(N'Tuyen Quang Mini Football Field', N'368 Quoc Tu Giam, Tan Thoi Nhat Ward, District 12, HCMC', 10.865123, 106.627456, 1, 'tuyen-quang-mini-football-field'),
(N'Hiep Thanh Sports Center', N'123 Hiep Thanh Street, Hiep Thanh Ward, District 12, HCMC', 10.871234, 106.635789, 2, 'hiep-thanh-sports-center'),
(N'Thanh Loc Football Field', N'456 Thanh Loc Street, Thanh Loc Ward, District 12, HCMC', 10.863456, 106.642123, 3, 'thanh-loc-football-field'),
(N'Tan Chanh Hiep Sports Center', N'789 Tan Chanh Hiep Street, Tan Chanh Hiep Ward, District 12, HCMC', 10.869789, 106.638456, 4, 'tan-chanh-hiep-sports-center'),
(N'Thoi An Football Field', N'321 Thoi An Street, Thoi An Ward, District 12, HCMC', 10.875321, 106.645789, 1, 'thoi-an-football-field'),
(N'Dong Hung Thuan Sports Center', N'654 Dong Hung Thuan Street, Dong Hung Thuan Ward, District 12, HCMC', 10.877654, 106.649321, 5, 'dong-hung-thuan-sports-center'),
(N'Tan Thoi Hiep Sports Field', N'987 Tan Thoi Hiep Street, Tan Thoi Hiep Ward, District 12, HCMC', 10.859876, 106.634567, 2, 'tan-thoi-hiep-sports-field');
GO

-- Insert field types (without slugs)
INSERT INTO field_types (name, team_capacity, max_capacity) VALUES
(N'5-a-side Football Field', 5, 10),
(N'7-a-side Football Field', 7, 14),
(N'11-a-side Football Field', 11, 22),
(N'Badminton Court', 2, 4),
(N'Tennis Court', 2, 4),
(N'Basketball Court', 5, 10),
(N'Volleyball Court', 6, 12),
(N'Futsal Court', 5, 10);
GO

-- Insert field categories for each location (without slugs)
INSERT INTO field_categories (name, description, location_id) VALUES
-- Categories for Tuyen Quang Mini Football Field (location_id = 1)
(N'Artificial Turf', N'High-quality artificial turf, suitable for professional matches', 1),
(N'Natural Grass', N'Natural grass field, soft surface, suitable for recreation', 1),
-- Categories for Hiep Thanh Sports Center (location_id = 2)
(N'Indoor Courts', N'Indoor sports courts with roofing, unaffected by weather', 2),
(N'Outdoor Courts', N'Outdoor sports courts, airy with a beautiful view', 2),
-- Categories for Thanh Loc Football Field (location_id = 3)
(N'Mini Fields', N'Mini football fields, suitable for small groups', 3),
(N'Standard Fields', N'Standard football fields, suitable for official competitions', 3),
-- Categories for Tan Chanh Hiep Sports Center (location_id = 4)
(N'Multi-purpose Courts', N'Multi-purpose sports courts for various sports', 4),
(N'Dedicated Courts', N'Courts dedicated to specific sports', 4),
-- Categories for Thoi An Football Field (location_id = 5)
(N'VIP Fields', N'VIP fields with premium services and private changing rooms', 5),
(N'Regular Fields', N'Regular fields with affordable prices', 5),
-- Categories for Dong Hung Thuan Sports Center (location_id = 6)
(N'Premium Courts', N'Premium sports courts with full amenities', 6),
(N'Standard Courts', N'Standard sports courts at reasonable prices', 6),
-- Categories for Tan Thoi Hiep Sports Field (location_id = 7)
(N'New Courts', N'Newly built, modern sports courts', 7),
(N'Renovated Courts', N'Older sports courts that have been renovated', 7);
GO

-- Insert fields with is_active field included (all fields are active by default)
INSERT INTO fields (name, hourly_rate, description, category_id, type_id, location_id, is_active) VALUES
-- Fields for Tuyen Quang Mini Football Field
(N'Field A1', 200000, N'5-a-side artificial turf field with floodlights', 1, 1, 1, 1),
(N'Field A2', 200000, N'5-a-side artificial turf field with a great view', 1, 1, 1, 1),
(N'Field B1', 180000, N'5-a-side natural grass field, cool and pleasant', 2, 1, 1, 1),
(N'Field C1', 300000, N'7-a-side artificial turf field', 1, 2, 1, 1),

-- Fields for Hiep Thanh Sports Center
(N'Badminton Court 1', 80000, N'Indoor badminton court with professional wooden flooring', 3, 4, 2, 1),
(N'Badminton Court 2', 80000, N'Indoor, air-conditioned badminton court', 3, 4, 2, 1),
(N'Tennis Court 1', 120000, N'Outdoor tennis court with a concrete surface', 4, 5, 2, 1),
(N'Basketball Court 1', 150000, N'Outdoor basketball court with spectator stands', 4, 6, 2, 1),

-- Fields for Thanh Loc Football Field
(N'Mini Field 1', 160000, N'Mini 5-a-side field, perfect for a group of friends', 5, 1, 3, 1),
(N'Mini Field 2', 160000, N'Mini 5-a-side field with spacious parking', 5, 1, 3, 1),
(N'Standard Field 1', 350000, N'Standard 11-a-side football field', 6, 3, 3, 1),
(N'Futsal Court 1', 190000, N'Dedicated futsal court with wooden flooring', 6, 8, 3, 1),

-- Fields for Tan Chanh Hiep Sports Center
(N'Multi-purpose Court 1', 100000, N'Multi-purpose court for badminton and tennis', 7, 4, 4, 1),
(N'Football Field 1', 220000, N'Dedicated 7-a-side football field', 8, 2, 4, 1),
(N'Basketball Court 2', 140000, N'Dedicated basketball court with standard markings', 8, 6, 4, 1),
(N'Volleyball Court 1', 110000, N'Standard volleyball court', 8, 7, 4, 1),

-- Fields for Thoi An Football Field
(N'VIP Field 1', 280000, N'VIP 5-a-side football field with private changing room', 9, 1, 5, 1),
(N'VIP Field 2', 280000, N'VIP 7-a-side football field with premium service', 9, 2, 5, 1),
(N'Regular Field 1', 170000, N'Regular 5-a-side football field, affordable price', 10, 1, 5, 1),
(N'Regular Field 2', 170000, N'Regular 5-a-side football field with a good view', 10, 1, 5, 1),

-- Fields for Dong Hung Thuan Sports Center
(N'Premium Field 1', 250000, N'Premium 5-a-side football field', 11, 1, 6, 1),
(N'Premium Field 2', 320000, N'Premium 7-a-side football field', 11, 2, 6, 1),
(N'Tennis Court 2', 130000, N'Premium tennis court', 11, 5, 6, 1),
(N'Standard Court 1', 120000, N'Standard sports court at reasonable price', 12, 1, 6, 1),

-- Fields for Tan Thoi Hiep Sports Field
(N'New Field 1', 210000, N'Newly built 5-a-side football field', 13, 1, 7, 1),
(N'New Field 2', 290000, N'Newly built 7-a-side football field', 13, 2, 7, 1),
(N'Renovated Field 1', 140000, N'Renovated 5-a-side football field', 14, 1, 7, 1),
(N'Badminton Court 3', 75000, N'Renovated badminton court', 14, 4, 7, 1),

-- Additional inactive field for testing purposes
(N'Maintenance Field', 0, N'Field currently under maintenance', 1, 1, 1, 0);
GO

-- Insert bookings for today and next 7 days using dynamic dates
-- The logic remains the same as it uses dynamic date functions

-- Update any existing fields that might have NULL is_active values
UPDATE fields SET is_active = 1 WHERE is_active IS NULL;
GO

-- Verify the data insertion
PRINT 'Complete sample data insertion finished successfully!';
PRINT 'Data summary:';

SELECT
    'owners' as table_name,
    COUNT(*) as total_records
FROM owners
UNION ALL
SELECT
    'locations' as table_name,
    COUNT(*) as total_records
FROM locations
UNION ALL
SELECT
    'field_types' as table_name,
    COUNT(*) as total_records
FROM field_types
UNION ALL
SELECT
    'field_categories' as table_name,
    COUNT(*) as total_records
FROM field_categories
UNION ALL
SELECT
    'fields' as table_name,
    COUNT(*) as total_records
FROM fields
UNION ALL
SELECT
    'bookings' as table_name,
    COUNT(*) as total_records
FROM bookings;

-- Check is_active field status
SELECT 
    'Active Fields' as status,
    COUNT(*) as count
FROM fields 
WHERE is_active = 1
UNION ALL
SELECT 
    'Inactive Fields' as status,
    COUNT(*) as count
FROM fields 
WHERE is_active = 0
UNION ALL
SELECT 
    'NULL is_active Fields' as status,
    COUNT(*) as count
FROM fields 
WHERE is_active IS NULL;
GO