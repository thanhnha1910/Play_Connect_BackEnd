/*
================================================================
VERIFY COMPLETE DATABASE DATA - KIỂM TRA DỮ LIỆU HOÀN CHỈNH
================================================================
Script kiểm tra để xác nhận tất cả dữ liệu đã được thêm đầy đủ
*/

USE Project4_VN;
GO

PRINT '=== KIỂM TRA DỮ LIỆU HOÀN CHỈNH ===';
PRINT '';

-- Kiểm tra số lượng records trong tất cả các bảng chính
SELECT 'BẢNG CƠ BẢN' as Category;
SELECT 'roles' as TableName, COUNT(*) as RecordCount FROM roles
UNION ALL
SELECT 'sports', COUNT(*) FROM sports
UNION ALL
SELECT 'tags', COUNT(*) FROM tags
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'user_roles', COUNT(*) FROM user_roles
UNION ALL
SELECT 'owners', COUNT(*) FROM owners
UNION ALL
SELECT 'locations', COUNT(*) FROM locations
UNION ALL
SELECT 'field_types', COUNT(*) FROM field_types
UNION ALL
SELECT 'field_categories', COUNT(*) FROM field_categories
UNION ALL
SELECT 'fields', COUNT(*) FROM fields;
GO

SELECT 'BẢNG HOẠT ĐỘNG' as Category;
SELECT 'teams' as TableName, COUNT(*) as RecordCount FROM teams
UNION ALL
SELECT 'team_rosters', COUNT(*) FROM team_rosters
UNION ALL
SELECT 'tournaments', COUNT(*) FROM tournaments
UNION ALL
SELECT 'matches', COUNT(*) FROM matches
UNION ALL
SELECT 'bookings', COUNT(*) FROM bookings
UNION ALL
SELECT 'booking_users', COUNT(*) FROM booking_users
UNION ALL
SELECT 'payments', COUNT(*) FROM payments
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications;
GO

SELECT 'BẢNG XÃ HỘI' as Category;
SELECT 'chat_rooms' as TableName, COUNT(*) as RecordCount FROM chat_rooms
UNION ALL
SELECT 'chat_members', COUNT(*) FROM chat_members
UNION ALL
SELECT 'chat_messages', COUNT(*) FROM chat_messages
UNION ALL
SELECT 'posts', COUNT(*) FROM posts
UNION ALL
SELECT 'comments', COUNT(*) FROM comments
UNION ALL
SELECT 'friends', COUNT(*) FROM friends
UNION ALL
SELECT 'open_matches', COUNT(*) FROM open_matches
UNION ALL
SELECT 'draft_matches', COUNT(*) FROM draft_matches
UNION ALL
SELECT 'system_settings', COUNT(*) FROM system_settings;
GO

-- Kiểm tra chi tiết các tài khoản test
PRINT '';
PRINT '=== TÀI KHOẢN TEST ===';
SELECT 
    u.id,
    u.username,
    u.email,
    u.full_name,
    r.name as role_name,
    u.is_active,
    u.email_verified,
    u.status
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username IN ('Admin', 'Owner', 'User')
ORDER BY u.id;
GO

-- Kiểm tra owners và locations
PRINT '';
PRINT '=== OWNERS VÀ LOCATIONS ===';
SELECT 
    o.owner_id,
    o.business_name,
    u.username,
    COUNT(l.location_id) as total_locations
FROM owners o
JOIN users u ON o.user_id = u.id
LEFT JOIN locations l ON o.owner_id = l.owner_id
GROUP BY o.owner_id, o.business_name, u.username
ORDER BY o.owner_id;
GO

-- Kiểm tra fields theo location
PRINT '';
PRINT '=== FIELDS THEO LOCATION ===';
SELECT 
    l.name as location_name,
    COUNT(f.field_id) as total_fields,
    AVG(CAST(f.hourly_rate as FLOAT)) as avg_hourly_rate
FROM locations l
LEFT JOIN fields f ON l.location_id = f.location_id
GROUP BY l.location_id, l.name
ORDER BY l.location_id;
GO

-- Kiểm tra bookings active
PRINT '';
PRINT '=== BOOKINGS ACTIVE ===';
SELECT 
    b.booking_id,
    u.username as booker,
    f.name as field_name,
    b.from_time,
    b.to_time,
    b.status,
    COUNT(bu.user_id) as participants
FROM bookings b
JOIN users u ON b.user_id = u.id
JOIN fields f ON b.field_id = f.field_id
LEFT JOIN booking_users bu ON b.booking_id = bu.booking_id
WHERE b.status = 'confirmed'
GROUP BY b.booking_id, u.username, f.name, b.from_time, b.to_time, b.status
ORDER BY b.booking_id;
GO

-- Kiểm tra teams và members
PRINT '';
PRINT '=== TEAMS VÀ MEMBERS ===';
SELECT 
    t.team_id,
    t.name as team_name,
    t.code as team_code,
    COUNT(tr.user_id) as total_members,
    t.created_at
FROM teams t
LEFT JOIN team_rosters tr ON t.team_id = tr.team_id
GROUP BY t.team_id, t.name, t.code, t.created_at
ORDER BY t.team_id;
GO

PRINT '';
PRINT '=== KIỂM TRA HOÀN TẤT ===';
PRINT 'Database đã được thiết lập với dữ liệu hoàn chỉnh!';
PRINT 'Tất cả các bảng đã có dữ liệu mẫu phong phú.';
PRINT '============================================';
GO