-- This script adds sample data for the sports field management system
-- The 'locations' data has been updated to District 12, near TTH15 Street, and translated to English.

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

-- =================================================================
-- UPDATED LOCATIONS SECTION TO 35/6 TTH15 STREET, DISTRICT 12
-- =================================================================
INSERT INTO locations (name, address, latitude, longitude, owner_id, slug) VALUES
(N'TTH15 Sports Zone', N'35/6 TTH15 Street, Group 30, Quarter 3A, Tan Thoi Hiep Ward, District 12, Ho Chi Minh City', 10.8525, 106.6312, 1, 'tth15-sports-zone'),
(N'District 12 Arena', N'35/6 TTH15 Street, Group 30, Quarter 3A, Tan Thoi Hiep Ward, District 12, Ho Chi Minh City', 10.8528, 106.6315, 2, 'district-12-arena'),
(N'Tan Thoi Hiep Pitch', N'35/6 TTH15 Street, Group 30, Quarter 3A, Tan Thoi Hiep Ward, District 12, Ho Chi Minh City', 10.8522, 106.6309, 3, 'tan-thoi-hiep-pitch'),
(N'Quarter 3A Sports Complex', N'35/6 TTH15 Street, Group 30, Quarter 3A, Tan Thoi Hiep Ward, District 12, Ho Chi Minh City', 10.8531, 106.6318, 4, 'quarter-3a-sports-complex'),
(N'The Northside Courts', N'35/6 TTH15 Street, Group 30, Quarter 3A, Tan Thoi Hiep Ward, District 12, Ho Chi Minh City', 10.8519, 106.6306, 1, 'the-northside-courts'),
(N'An Phu Dong Premier Fields', N'35/6 TTH15 Street, Group 30, Quarter 3A, Tan Thoi Hiep Ward, District 12, Ho Chi Minh City', 10.8535, 106.6321, 5, 'an-phu-dong-premier-fields'),
(N'Hiep Thanh Athletics Club', N'35/6 TTH15 Street, Group 30, Quarter 3A, Tan Thoi Hiep Ward, District 12, Ho Chi Minh City', 10.8520, 106.6314, 2, 'hiep-thanh-athletics-club');
GO
-- =================================================================
-- END OF UPDATED SECTION
-- =================================================================

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
-- Categories for TTH15 Sports Zone (location_id = 1)
(N'Artificial Turf', N'High-quality artificial turf, suitable for professional matches', 1),
(N'Natural Grass', N'Natural grass field, soft surface, suitable for recreation', 1),
-- Categories for District 12 Arena (location_id = 2)
(N'Indoor Courts', N'Indoor sports courts with roofing, unaffected by weather', 2),
(N'Outdoor Courts', N'Outdoor sports courts, airy with a beautiful view', 2),
-- Categories for Tan Thoi Hiep Pitch (location_id = 3)
(N'Mini Fields', N'Mini football fields, suitable for small groups', 3),
(N'Standard Fields', N'Standard football fields, suitable for official competitions', 3),
-- Categories for Quarter 3A Sports Complex (location_id = 4)
(N'Multi-purpose Courts', N'Multi-purpose sports courts for various sports', 4),
(N'Dedicated Courts', N'Courts dedicated to specific sports', 4),
-- Categories for The Northside Courts (location_id = 5)
(N'VIP Fields', N'VIP fields with premium services and private changing rooms', 5),
(N'Regular Fields', N'Regular fields with affordable prices', 5),
-- Categories for An Phu Dong Premier Fields (location_id = 6)
(N'Premium Courts', N'Premium sports courts with full amenities', 6),
(N'Standard Courts', N'Standard sports courts at reasonable prices', 6),
-- Categories for Hiep Thanh Athletics Club (location_id = 7)
(N'New Courts', N'Newly built, modern sports courts', 7),
(N'Renovated Courts', N'Older sports courts that have been renovated', 7);
GO

-- Insert fields (without slugs)
INSERT INTO fields (name, hourly_rate, description, category_id, type_id, location_id) VALUES
-- Fields for TTH15 Sports Zone
(N'Field A1', 250000, N'5-a-side artificial turf field with floodlights', 1, 1, 1),
(N'Field A2', 250000, N'5-a-side artificial turf field with a great view', 1, 1, 1),
(N'Field B1', 220000, N'5-a-side natural grass field, cool and pleasant', 2, 1, 1),
(N'Field C1', 350000, N'7-a-side artificial turf field', 1, 2, 1),

-- Fields for District 12 Arena
(N'Badminton Court 1', 100000, N'Indoor badminton court with professional wooden flooring', 3, 4, 2),
(N'Badminton Court 2', 100000, N'Indoor, air-conditioned badminton court', 3, 4, 2),
(N'Tennis Court 1', 150000, N'Outdoor tennis court with a concrete surface', 4, 5, 2),
(N'Basketball Court 1', 180000, N'Outdoor basketball court with spectator stands', 4, 6, 2),

-- Fields for Tan Thoi Hiep Pitch
(N'Mini Field 1', 200000, N'Mini 5-a-side field, perfect for a group of friends', 5, 1, 3),
(N'Mini Field 2', 200000, N'Mini 5-a-side field with spacious parking', 5, 1, 3),
(N'Standard Field 1', 400000, N'Standard 11-a-side football field', 6, 3, 3),
(N'Futsal Court 1', 230000, N'Dedicated futsal court with wooden flooring', 6, 8, 3),

-- Fields for Quarter 3A Sports Complex
(N'Multi-purpose Court 1', 120000, N'Multi-purpose court for badminton and tennis', 7, 4, 4),
(N'Football Field 1', 280000, N'Dedicated 7-a-side football field', 8, 2, 4),
(N'Basketball Court 2', 170000, N'Dedicated basketball court with standard markings', 8, 6, 4),
(N'Volleyball Court 1', 140000, N'Standard volleyball court', 8, 7, 4),

-- Fields for The Northside Courts
(N'VIP Field 1', 320000, N'VIP 5-a-side football field with private changing room', 9, 1, 5),
(N'VIP Field 2', 320000, N'VIP 7-a-side football field with premium service', 9, 2, 5),
(N'Regular Field 1', 210000, N'Regular 5-a-side football field, affordable price', 10, 1, 5),
(N'Regular Field 2', 210000, N'Regular 5-a-side football field with a good view', 10, 1, 5),

-- Fields for An Phu Dong Premier Fields
(N'Premium Field 1', 300000, N'Premium 5-a-side football field', 11, 1, 6),
(N'Premium Field 2', 380000, N'Premium 7-a-side football field', 11, 2, 6),
(N'Tennis Court 2', 160000, N'Premium tennis court', 11, 5, 6),
(N'Standard Court 1', 180000, N'Standard sports court at reasonable price', 12, 1, 6),

-- Fields for Hiep Thanh Athletics Club
(N'New Field 1', 260000, N'Newly built 5-a-side football field', 13, 1, 7),
(N'New Field 2', 340000, N'Newly built 7-a-side football field', 13, 2, 7),
(N'Renovated Field 1', 180000, N'Renovated 5-a-side football field', 14, 1, 7),
(N'Badminton Court 3', 95000, N'Renovated badminton court', 14, 4, 7);
GO

-- Insert bookings for today and next 7 days using dynamic dates
-- The logic remains the same as it uses dynamic date functions
INSERT INTO bookings (from_time, to_time, slots, status, field_id) VALUES
-- Today's bookings
(DATEADD(HOUR, 6, CAST(GETDATE() AS DATETIME)), DATEADD(HOUR, 7, CAST(GETDATE() AS DATETIME)), 8, 'confirmed', 1),
(DATEADD(HOUR, 7, CAST(GETDATE() AS DATETIME)), DATEADD(HOUR, 8, CAST(GETDATE() AS DATETIME)), 10, 'confirmed', 1),
(DATEADD(HOUR, 19, CAST(GETDATE() AS DATETIME)), DATEADD(HOUR, 20, CAST(GETDATE() AS DATETIME)), 8, 'pending', 1),

-- Tomorrow's bookings
(DATEADD(HOUR, 6, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 7, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), 6, 'confirmed', 1),
(DATEADD(HOUR, 20, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 21, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), 10, 'confirmed', 1),

-- Day 2 bookings
(DATEADD(HOUR, 8, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 9, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), 8, 'confirmed', 2),
(DATEADD(HOUR, 18, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 19, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), 6, 'confirmed', 2),
(DATEADD(HOUR, 7, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 8, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), 10, 'pending', 2),

-- Day 3 bookings
(DATEADD(HOUR, 9, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 10, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), 8, 'confirmed', 3),
(DATEADD(HOUR, 8, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 9, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), 6, 'confirmed', 3),
(DATEADD(HOUR, 19, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 20, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), 8, 'pending', 3),

-- Day 4 bookings
(DATEADD(HOUR, 10, DATEADD(DAY, 4, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 11, DATEADD(DAY, 4, CAST(GETDATE() AS DATETIME))), 12, 'confirmed', 4),
(DATEADD(HOUR, 9, DATEADD(DAY, 4, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 10, DATEADD(DAY, 4, CAST(GETDATE() AS DATETIME))), 14, 'confirmed', 4),
(DATEADD(HOUR, 18, DATEADD(DAY, 4, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 19, DATEADD(DAY, 4, CAST(GETDATE() AS DATETIME))), 10, 'pending', 4),

-- Day 5 bookings for badminton courts
(DATEADD(HOUR, 11, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 12, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), 2, 'confirmed', 5),
(DATEADD(HOUR, 14, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 15, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), 4, 'confirmed', 5),
(DATEADD(HOUR, 10, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 11, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), 2, 'confirmed', 5),
(DATEADD(HOUR, 16, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 17, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), 4, 'pending', 5),

-- Day 6 bookings for various courts
(DATEADD(HOUR, 12, DATEADD(DAY, 6, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 13, DATEADD(DAY, 6, CAST(GETDATE() AS DATETIME))), 2, 'confirmed', 6),
(DATEADD(HOUR, 11, DATEADD(DAY, 6, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 12, DATEADD(DAY, 6, CAST(GETDATE() AS DATETIME))), 4, 'confirmed', 6),
(DATEADD(HOUR, 15, DATEADD(DAY, 6, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 16, DATEADD(DAY, 6, CAST(GETDATE() AS DATETIME))), 2, 'pending', 6),

-- Day 7 bookings for tennis
(DATEADD(HOUR, 13, DATEADD(DAY, 7, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 14, DATEADD(DAY, 7, CAST(GETDATE() AS DATETIME))), 2, 'confirmed', 7),
(DATEADD(HOUR, 12, DATEADD(DAY, 7, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 13, DATEADD(DAY, 7, CAST(GETDATE() AS DATETIME))), 4, 'confirmed', 7),
(DATEADD(HOUR, 16, DATEADD(DAY, 7, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 17, DATEADD(DAY, 7, CAST(GETDATE() AS DATETIME))), 2, 'pending', 7),

-- More bookings for basketball and other sports
(DATEADD(HOUR, 15, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 16, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), 8, 'confirmed', 8),
(DATEADD(HOUR, 13, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 14, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), 10, 'confirmed', 8),
(DATEADD(HOUR, 17, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 18, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), 6, 'pending', 8),

-- Additional bookings for new fields
(DATEADD(HOUR, 14, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 15, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), 8, 'confirmed', 21),
(DATEADD(HOUR, 16, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 17, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), 10, 'confirmed', 22),
(DATEADD(HOUR, 18, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 19, DATEADD(DAY, 3, CAST(GETDATE() AS DATETIME))), 6, 'pending', 23),

-- VIP field bookings
(DATEADD(HOUR, 22, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 23, DATEADD(DAY, 1, CAST(GETDATE() AS DATETIME))), 8, 'confirmed', 17),
(DATEADD(HOUR, 20, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 21, DATEADD(DAY, 2, CAST(GETDATE() AS DATETIME))), 10, 'confirmed', 17),
(DATEADD(HOUR, 19, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), DATEADD(HOUR, 20, DATEADD(DAY, 5, CAST(GETDATE() AS DATETIME))), 6, 'pending', 17);
GO

-- Verify the data insertion
PRINT 'Sample data insertion completed successfully!';
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
GO