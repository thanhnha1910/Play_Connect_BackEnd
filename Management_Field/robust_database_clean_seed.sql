-- =====================================================
-- ROBUST DATABASE CLEAN AND SEED SCRIPT
-- Generated based on Java Models Analysis
-- =====================================================

-- Disable foreign key constraints for cleanup
EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';
GO

-- =====================================================
-- CLEANUP SECTION - Delete all data
-- =====================================================
PRINT 'Starting database cleanup...';

-- Delete in correct order to respect foreign key dependencies
DELETE FROM booking_users;
DELETE FROM admin_revenues;
Delete From sports
DELETE FROM bookings;
DELETE FROM matches;
DELETE FROM participating_teams;
DELETE FROM tournaments;
DELETE FROM teams;
DELETE FROM team_rosters;
DELETE FROM fields;
DELETE FROM field_types;
DELETE FROM field_categories;
DELETE FROM locations;
DELETE FROM owners;
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM roles;
DELETE FROM payments;
DELETE FROM field_closure;
DELETE FROM global_closure;
DELETE FROM holiday_closure;
DELETE FROM operating_hours;
DELETE FROM posts;
DELETE FROM notifications;
DELETE FROM chat_rooms;
DELETE FROM chat_messages;
DELETE FROM location_reviews;
DELETE FROM tags;
DELETE FROM admins;
DELETE FROM invitations;
DELETE FROM booking_recruitment;
DELETE FROM draft_match_user_status;
DELETE FROM system_settings;

PRINT 'Data cleanup completed.';

-- =====================================================
-- RESET IDENTITY COLUMNS
-- =====================================================
PRINT 'Resetting identity columns...';

DBCC CHECKIDENT ('roles', RESEED, 0);
DBCC CHECKIDENT ('users', RESEED, 0);
DBCC CHECKIDENT ('owners', RESEED, 0);
DBCC CHECKIDENT ('locations', RESEED, 0);
DBCC CHECKIDENT ('field_categories', RESEED, 0);
DBCC CHECKIDENT ('field_types', RESEED, 0);
DBCC CHECKIDENT ('fields', RESEED, 0);
DBCC CHECKIDENT ('teams', RESEED, 0);
DBCC CHECKIDENT ('tournaments', RESEED, 0);
DBCC CHECKIDENT ('participating_teams', RESEED, 0);
DBCC CHECKIDENT ('matches', RESEED, 0);
DBCC CHECKIDENT ('bookings', RESEED, 0);
DBCC CHECKIDENT ('payments', RESEED, 0);
DBCC CHECKIDENT ('operating_hours', RESEED, 0);
DBCC CHECKIDENT ('posts', RESEED, 0);
DBCC CHECKIDENT ('notifications', RESEED, 0);
DBCC CHECKIDENT ('chat_rooms', RESEED, 0);
DBCC CHECKIDENT ('chat_messages', RESEED, 0);
DBCC CHECKIDENT ('location_reviews', RESEED, 0);
DBCC CHECKIDENT ('tags', RESEED, 0);
DBCC CHECKIDENT ('admins', RESEED, 0);
DBCC CHECKIDENT ('invitations', RESEED, 0);
DBCC CHECKIDENT ('booking_recruitment', RESEED, 0);
DBCC CHECKIDENT ('draft_match_user_status', RESEED, 0);
DBCC CHECKIDENT ('system_settings', RESEED, 0);
DBCC CHECKIDENT ('field_closure', RESEED, 0);
DBCC CHECKIDENT ('global_closure', RESEED, 0);
DBCC CHECKIDENT ('holiday_closure', RESEED, 0);

PRINT 'Identity columns reset completed.';

-- =====================================================
-- DATA INSERTION SECTION
-- =====================================================
-- =================================================================================================
-- SCRIPT DỮ LIỆU MẪU TOÀN DIỆN - PHIÊN BẢN ĐÃ SỬA LỖI
-- Target: SQL Server (T-SQL)
-- MẬT KHẨU ĐÃ BĂM: Mật khẩu của người dùng đã được băm bằng BCrypt. Mật khẩu gốc là '123123a'.
-- CÁC THAY ĐỔI CHÍNH:
-- 1. Đã loại bỏ việc chèn giá trị ID tường minh cho các bảng có cột identity (tự tăng).
-- 2. Đã sửa lại tất cả các tên cột không hợp lệ để khớp với quy ước snake_case từ các model Java.
-- 3. Đã khắc phục các lỗi khóa chính và tên đối tượng không hợp lệ.
-- =================================================================================================

-- =================================================================================================
-- PHẦN 1: DỮ LIỆU CỐT LÕI VÀ CẤU HÌNH HỆ THỐNG
-- =================================================================================================

-- Bảng `roles`
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_OWNER'), ('ROLE_ADMIN');
GO

-- Bảng `sports`
INSERT INTO sports (name, sport_code, icon, is_active) VALUES
(N'Bóng đá', 'SOCCER', '⚽', 1),
(N'Bóng rổ', 'BASKETBALL', '🏀', 1),
(N'Cầu lông', 'BADMINTON', '🏸', 1),
(N'Bóng chuyền', 'VOLLEYBALL', '🏐', 0);
GO

-- Bảng `tags`
INSERT INTO tags (name, sport_id, is_active) VALUES
(N'Thủ môn', 1, 1),
(N'Hậu vệ', 1, 1),
(N'Tiền vệ', 1, 1),
(N'Tiền đạo', 1, 1),
(N'Kèo tối', 1, 1),
(N'Giao lưu', 1, 1),
(N'Bắt cặp nhanh', 2, 1);
GO



-- =================================================================================================
-- PHẦN 2: NGƯỜI DÙNG VÀ QUẢN LÝ TRUY CẬP
-- =================================================================================================

-- Bảng `users`
INSERT INTO users (username, password, email, full_name, phone_number, address, profile_picture, image_url, provider, provider_id, is_active, email_verified, verification_token, verification_token_expiry, reset_password_token, reset_password_token_expiry, status, join_date, sport_profiles, is_discoverable, has_completed_profile, booking_count, member_level) VALUES
('manutd', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'manutd.fan@example.com', N'Nguyễn Văn Hùng', '0912345678', N'123 Đường Lê Lợi, Quận 1, TP.HCM', 'https://i.pravatar.cc/150?u=user1', 'https://i.pravatar.cc/150?u=user1', 'LOCAL', NULL, 1, 1, NULL, NULL, NULL, NULL, 'ACTIVE', '2025-01-15T09:30:00', N'{"soccer": {"preferred_position": "midfielder", "skill_level": 8}}', 1, 1, 5, 2),
('owner.celadon', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'owner@gmail.com', N'Trần Ngọc Lan', '0987654321', N'456 Đường Nguyễn Huệ, Quận 1, TP.HCM', 'https://i.pravatar.cc/150?u=user2', 'https://i.pravatar.cc/150?u=user2', 'LOCAL', NULL, 1, 1, NULL, NULL, NULL, NULL, 'ACTIVE', '2025-02-20T11:00:00', N'{"soccer": {"skill_level": 5}}', 1, 1, 2, 1),
('sysadmin', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'admin@gmail.com', N'Hoàng Quản Trị', '0909090909', N'789 Đường Pasteur, Quận 3, TP.HCM', 'https://i.pravatar.cc/150?u=user3', 'https://i.pravatar.cc/150?u=user3', 'LOCAL', NULL, 1, 1, NULL, NULL, NULL, NULL, 'ACTIVE', '2025-01-01T08:00:00', NULL, 0, 1, 0, 1),
('thanh.vo.google', NULL, 'thanh.vo.google@gmail.com', N'Võ Thị Thanh', '0938111222', N'101 Đường Calmette, Quận 4, TP.HCM', 'https://i.pravatar.cc/150?u=user4', 'https://i.pravatar.cc/150?u=user4', 'GOOGLE', '10293847561029384756', 1, 1, NULL, NULL, NULL, NULL, 'ACTIVE', '2025-03-10T14:20:00', N'{"badminton": {"skill_level": "advanced"}}', 1, 1, 10, 3),
('user.inactive', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'inactive@example.com', N'Lê Văn Tèo', '0944333222', N'222 Đường Võ Văn Tần, Quận 3, TP.HCM', 'https://i.pravatar.cc/150?u=user5', 'https://i.pravatar.cc/150?u=user5', 'LOCAL', NULL, 0, 1, NULL, NULL, NULL, NULL, 'INACTIVE', '2025-04-05T18:00:00', NULL, 0, 0, 0, 1);
GO
select * from roles
select * from users


-- Bảng `user_roles`
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1), (2, 2), (3, 3), (4, 1), (5, 1);
GO

-- Bảng `admins`
INSERT INTO admins (note, created_at, updated_at) VALUES (N'Tài khoản quản trị viên hệ thống chính', '2025-01-01T08:00:00', '2025-01-01T08:00:00');
GO

-- Bảng `owners`
INSERT INTO owners (business_name, user_id) VALUES (N'Hệ thống sân Celadon City', 2);
GO


-- =================================================================================================
-- PHẦN 3: CƠ SỞ HẠ TẦNG (VỊ TRÍ, SÂN)
-- =================================================================================================

-- Bảng `locations`
INSERT INTO locations (name, slug, address, city, country, latitude, longitude, owner_id, thumbnail_url, image_gallery) VALUES
(N'Sân bóng đá Celadon City', 'san-bong-da-celadon-city', N'Đường N1, phường Sơn Kỳ, quận Tân Phú', N'Ho Chi Minh City', N'Vietnam', 10.801567, 106.603598, 1, 'https://images.pexels.com/photos/274506/pexels-photo-274506.jpeg', N'["https://images.pexels.com/photos/1171084/pexels-photo-1171084.jpeg", "https://images.pexels.com/photos/46798/the-ball-stadion-football-the-pitch-46798.jpeg"]'),
(N'Sân bóng rổ Phan Đình Phùng', 'san-bong-ro-phan-dinh-phung', N'8 Võ Văn Tần, Phường 6, Quận 3', N'Ho Chi Minh City', N'Vietnam', 10.781600, 106.692410, NULL, 'https://images.pexels.com/photos/2834917/pexels-photo-2834917.jpeg', N'[]');
GO

-- Bảng `field_categories`
INSERT INTO field_categories (name, description, location_id) VALUES
(N'Sân cỏ nhân tạo', N'Sân cỏ nhân tạo đạt chuẩn, giảm chấn thương.', 1),
(N'Sân Futsal trong nhà', N'Sân gỗ tiêu chuẩn thi đấu Futsal.', 1),
(N'Sân bóng rổ ngoài trời', N'Sân xi măng sơn chuyên dụng cho bóng rổ.', 2);
GO

-- Bảng `field_types`
INSERT INTO field_types (name, team_capacity, max_capacity, hourly_rate, description, location_id) VALUES
(N'Sân 5 người', 5, 10, 300000, N'Sân 5v5 kích thước 20m x 40m', 1),
(N'Sân 7 người', 7, 14, 500000, N'Sân 7v7 kích thước 30m x 50m', 1),
(N'Sân bóng rổ 5x5', 5, 10, 200000, N'Sân bóng rổ tiêu chuẩn 5x5', 2);
GO

-- Bảng `fields`
INSERT INTO fields (name, hourly_rate, description, created_at, category_id, type_id, location_id, is_active, thumbnail_url, image_gallery) VALUES
(N'Celadon A1', 300000, N'Sân 5 người, gần cổng chính', '2025-03-01T08:00:00', 1, 1, 1, 1, 'https://images.pexels.com/photos/54123/pexels-photo-54123.jpeg', N'[]'),
(N'Celadon A2', 300000, N'Sân 5 người, có lưới che', '2025-03-01T08:00:00', 1, 1, 1, 1, 'https://images.pexels.com/photos/221210/pexels-photo-221210.jpeg', N'[]'),
(N'Celadon B1', 500000, N'Sân 7 người, mặt cỏ mới 2025', '2025-03-15T09:00:00', 1, 2, 1, 1, 'https://images.pexels.com/photos/262524/pexels-photo-262524.jpeg', N'[]'),
(N'Phan Đình Phùng R1', 200000, N'Sân bóng rổ A, gần khu vực gửi xe', '2025-04-01T10:00:00', 3, 3, 2, 1, 'https://images.pexels.com/photos/1752757/pexels-photo-1752757.jpeg', N'[]');
GO


-- Bảng `holiday_closure`
INSERT INTO holiday_closure (holiday_name, date, location_id) VALUES (N'Tết Nguyên Đán', '2026-01-29', 1);
GO

-- Bảng `field_closure`
INSERT INTO field_closure (field_id, start_date, end_date, reason) VALUES (3, '2025-09-01T00:00:00', '2025-09-07T23:59:59', N'Bảo trì, thay mặt cỏ định kỳ');
GO

-- =================================================================================================
-- PHẦN 4: TÍNH NĂNG MẠNG XÃ HỘI
-- =================================================================================================

-- Bảng `posts`
INSERT INTO posts (title, content, category, image_url, user_id) VALUES
(N'Highlight Vòng 1 Ngoại Hạng Anh 2025/26', N'Tổng hợp những bàn thắng đẹp và các pha bóng đáng chú ý nhất của vòng đấu mở màn.', N'Tin tức', 'https://images.pexels.com/photos/47730/the-ball-stadion-football-the-pitch-47730.jpeg', 3),
(N'Tìm 5 anh em đá kèo giao lưu tối thứ 6 tại Celadon', N'Team mình 5 người, trình trung bình, muốn tìm 1 team đá giao hữu lúc 20h-21h thứ 6 tuần này (08/08/2025) tại sân Celadon. Liên hệ Hùng 0912345678.', N'Tìm kèo', NULL, 1);
GO

-- Bảng `comments`
INSERT INTO comments (content, created_at, user_id, post_id, parent_comment_id) VALUES
(N'Thông tin rất hữu ích, cảm ơn admin!', '2025-08-08T15:00:00', 4, 1, NULL),
(N'Team mình cũng 5 người đây, đá không bạn ơi?', '2025-08-08T15:05:00', 2, 2, NULL),
(N'Ok bạn, chốt kèo nhé. Bạn add Zalo mình đi.', '2025-08-08T15:10:00', 1, 2, 2);
GO

-- Bảng `post_likes`
INSERT INTO post_likes (post_id, user_id, like_count) VALUES
(1, 1, 1),
(1, 2, 1),
(1, 4, 1),
(2, 2, 1);
GO

-- Bảng `comment_likes`
INSERT INTO comment_likes (user_id, comment_id, like_count) VALUES (1, 1, 1), (2, 3, 1);
GO

-- Bảng `friends`
INSERT INTO friends (user_id1, user_id2, created_at) VALUES (1, 4, '2025-06-01T10:00:00');
GO

-- =================================================================================================
-- PHẦN 5: ĐẶT SÂN, TÌM KÈO, TRẬN ĐẤU
-- =================================================================================================

-- Bảng `bookings`
INSERT INTO bookings (user_id, from_time, to_time, slots, status, field_id, payment_token, created_at, reminder_sent) VALUES
(1, '2025-08-08T12:00:00Z', '2025-08-08T13:00:00Z', 10, 'confirmed', 1, 'PAY-12345ABC', '2025-08-08T02:00:00', 0),
(4, '2025-08-10T11:00:00Z', '2025-08-10T12:00:00Z', 10, 'pending', 4, NULL, '2025-08-08T03:00:00', 0);
GO

-- Bảng `booking_users`
INSERT INTO booking_users (booking_id, user_id, is_booker, position) VALUES
(1, 1, 1, N'Tiền đạo'),
(1, 2, 0, N'Hậu vệ');
GO

-- Bảng `draft_matches`
INSERT INTO draft_matches (creator_user_id, sport_type, location_description, estimated_start_time, estimated_end_time, slots_needed, required_tags, skill_level, status, created_at) VALUES
(1, N'Bóng đá', N'Quận Tân Phú hoặc Tân Bình', '2025-08-12T20:00:00', '2025-08-12T21:00:00', 4, N'["Giao lưu", "Tiền vệ"]', 'INTERMEDIATE', 'RECRUITING', '2025-08-08T14:00:00');
GO

-- Bảng `draft_match_interested_users`
INSERT INTO draft_match_interested_users (draft_match_id, user_id, status, created_at, updated_at, message, ai_compatibility_score, ai_explicit_score, ai_implicit_score, ai_last_updated) VALUES
(1, 4, 'PENDING', '2025-08-08T14:10:00', '2025-08-08T14:10:00', N'Team mình có 2 người, tham gia được không bạn?', 85.5, 90.0, 81.0, '2025-08-08T14:10:01');
GO

-- Bảng `booking_recruitment`
INSERT INTO booking_recruitment (user_name, phone, field_number, field_location, play_time, people_needed, message, created_at, user_id) VALUES
(N'Nguyễn Văn Hùng', '0912345678', N'Celadon A1', N'Sân bóng đá Celadon City', '2025-08-08T19:00:00', 4, N'Team thiếu 4 người, cần vị trí thủ môn và hậu vệ.', '2025-08-08T10:00:00', 1);
GO

-- Bảng `recruitment_participants`
INSERT INTO recruitment_participants (message, is_accepted, number_of_people, recruitment_id, user_id) VALUES
(N'Mình và 1 bạn nữa đá được hậu vệ nhé', NULL, 2, 1, 4);
GO

-- =================================================================================================
-- PHẦN 6: ĐỘI BÓNG, GIẢI ĐẤU, THANH TOÁN
-- =================================================================================================

-- Bảng `teams`
INSERT INTO teams (name, code, logo, description, created_at) VALUES
(N'FC CoderNeverSleep', 'CNS', 'https://example.com/logo_cns.png', N'Đội bóng của các lập trình viên thức khuya.', '2025-02-01T23:00:00'),
(N'The Avengers FC', 'AVN', 'https://example.com/logo_avengers.png', N'Tập hợp các anh em có siêu năng lực đá bóng.', '2025-03-01T10:00:00');
GO

-- Bảng `team_rosters`
INSERT INTO team_rosters (team_id, user_id, position, is_captain) VALUES
(1, 3, N'Coach', 1),
(2, 1, N'Captain', 1),
(2, 4, N'Midfielder', 0);
GO

-- Bảng `tournaments`
INSERT INTO tournaments (name, slug, description, start_date, end_date, prize, entry_fee, slots, status, location_id) VALUES
(N'Giải FPT Aptech mở rộng 2025', 'giai-fpt-aptech-mo-rong-2025', N'Giải đấu thường niên quy tụ các đội bóng sinh viên và cựu sinh viên.', '2025-10-20T08:00:00', '2025-11-20T21:00:00', 20000000, 1000000, 16, 'UPCOMING', 1);
GO

-- Bảng `payments`
INSERT INTO payments (total, method, status, created_at, updated_at, transaction_id, payable_id, payable_type) VALUES
(300000, 'PAYPAL', 'SUCCESS', '2025-08-08T02:00:05', '2025-08-08T02:00:10', 'PAYPAL-TRANS-123', 1, 'BOOKING'),
(1000000, 'PAYPAL', 'PENDING', '2025-08-08T16:00:00', '2025-08-08T16:00:00', NULL, 1, 'TOURNAMENT');
GO

-- Bảng `participating_teams`
INSERT INTO participating_teams (team_id, tournament_id, status, entry_payment_id) VALUES
(1, 1, 'REGISTERED', 2),
(2, 1, 'PENDING_APPROVAL', NULL);
GO

-- Bảng `matches`
INSERT INTO matches (team1_id, team2_id, team1_score, team2_score, is_finished, type, start_time, field_id, tournament_id) VALUES
(1, 2, NULL, NULL, 0, N'Vòng bảng', '2025-10-20T18:00:00', 3, 1);
GO

-- =================================================================================================
-- PHẦN 7: ĐÁNH GIÁ, THÔNG BÁO VÀ CHAT
-- =================================================================================================

-- Bảng `location_reviews`
INSERT INTO location_reviews (rating, comment, location_id, user_id) VALUES (4.5, N'Sân Celadon rất đẹp, cỏ tốt, dịch vụ chuyên nghiệp. Sẽ quay lại!', 1, 1);
GO

-- Bảng `field_reviews`
INSERT INTO field_reviews (rating, comment, field_id, user_id, created_at) VALUES (5.0, N'Sân A1 mặt cỏ rất êm, ít bị trơn trượt.', 1, 4, '2025-08-08T14:00:00');
GO

-- Bảng `notifications`
INSERT INTO notifications (title, content, type, related_entity_id, created_at, is_read, recipient_id) VALUES
(N'Lời mời kết bạn', N'Võ Thị Thanh đã gửi cho bạn lời mời kết bạn.', 'FRIEND_REQUEST', 4, '2025-06-01T09:59:00', 1, 1),
(N'Lượt thích mới', N'Trần Ngọc Lan đã thích bài viết của bạn.', 'POST_LIKE', 1, '2025-08-08T15:01:00', 0, 3);
GO

-- Bảng `chat_rooms`
INSERT INTO chat_rooms (name, created_at, updated_at, is_active) VALUES ('FC CoderNeverSleep Group', '2025-02-01T23:05:00', '2025-08-08T15:20:00', 1);
GO



-- Bảng `chat_messages`
INSERT INTO chat_messages (chat_room_id, user_id, content, sent_at) VALUES (1, 3, N'Chào mừng mọi người đến với group của team!', '2025-02-01T23:06:00');
GO


PRINT '====== DỮ LIỆU MẪU ĐÃ ĐƯỢC CHÈN THÀNH CÔNG! ======';