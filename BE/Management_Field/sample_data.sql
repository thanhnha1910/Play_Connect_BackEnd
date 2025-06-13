-- Sample data for football field booking database
-- Insert into locations table
INSERT INTO locations (location_id, name, address, latitude, longitude, phone, email, description, created_at, updated_at) VALUES
(1, 'Sân Bóng Thể Thao Quận 1', '123 Nguyễn Huệ, Quận 1, TP.HCM', 10.7769, 106.7009, '0901234567', 'quan1@example.com', 'Sân bóng đá hiện đại tại trung tâm Quận 1 với đầy đủ tiện nghi', GETDATE(), GETDATE()),
(2, 'Sân Bóng Tân Bình Sports', '456 Cộng Hòa, Tân Bình, TP.HCM', 10.8012, 106.6557, '0901234568', 'tanbinhsports@example.com', 'Khu liên hợp thể thao với nhiều sân bóng chất lượng cao', GETDATE(), GETDATE()),
(3, 'Sân Bóng Bình Thạnh Center', '789 Xô Viết Nghệ Tĩnh, Bình Thạnh, TP.HCM', 10.8142, 106.7106, '0901234569', 'binhthanh@example.com', 'Trung tâm thể thao với sân cỏ nhân tạo và tự nhiên', GETDATE(), GETDATE()),
(4, 'Sân Bóng Thủ Đức United', '321 Võ Văn Ngân, Thủ Đức, TP.HCM', 10.8498, 106.7717, '0901234570', 'thuducunited@example.com', 'Sân bóng rộng rãi với hệ thống chiếu sáng hiện đại', GETDATE(), GETDATE()),
(5, 'Sân Bóng Gò Vấp Arena', '654 Quang Trung, Gò Vấp, TP.HCM', 10.8376, 106.6752, '0901234571', 'govap@example.com', 'Arena thể thao đa năng với sân bóng đạt tiêu chuẩn quốc tế', GETDATE(), GETDATE());

-- Insert into field_types table
INSERT INTO field_types (type_id, name, description, created_at, updated_at) VALUES
(1, '5-a-side', 'Sân bóng 5 người, kích thước nhỏ gọn phù hợp cho các trận đấu nhanh', GETDATE(), GETDATE()),
(2, '7-a-side', 'Sân bóng 7 người, kích thước trung bình phù hợp cho các giải đấu nghiệp dư', GETDATE(), GETDATE()),
(3, '11-a-side', 'Sân bóng 11 người, kích thước tiêu chuẩn FIFA cho các trận đấu chuyên nghiệp', GETDATE(), GETDATE());

-- Insert into field_categories table
INSERT INTO field_categories (category_id, name, description, created_at, updated_at) VALUES
(1, 'Artificial Turf', 'Sân cỏ nhân tạo, bền đẹp và ít bị ảnh hưởng bởi thời tiết', GETDATE(), GETDATE()),
(2, 'Natural Grass', 'Sân cỏ tự nhiên, mang lại cảm giác chơi bóng tự nhiên nhất', GETDATE(), GETDATE());

-- Insert into fields table
-- Location 1: Sân Bóng Thể Thao Quận 1 (3 fields)
INSERT INTO fields (field_id, location_id, type_id, category_id, description, hourly_rate, created_at, updated_at) VALUES
(1, 1, 1, 1, 'Sân 5 người cỏ nhân tạo A1', 150000, GETDATE(), GETDATE()),
(2, 1, 2, 1, 'Sân 7 người cỏ nhân tạo B1', 250000, GETDATE(), GETDATE()),
(3, 1, 3, 2, 'Sân 11 người cỏ tự nhiên C1', 500000, GETDATE(), GETDATE());

-- Location 2: Sân Bóng Tân Bình Sports (3 fields)
INSERT INTO fields (field_id, location_id, type_id, category_id, description, hourly_rate, created_at, updated_at) VALUES
(4, 2, 1, 1, 'Sân 5 người cỏ nhân tạo A2', 180000, GETDATE(), GETDATE()),
(5, 2, 2, 1, 'Sân 7 người cỏ nhân tạo B2', 280000, GETDATE(), GETDATE()),
(6, 2, 1, 1, 'Sân 5 người cỏ nhân tạo A3', 160000, GETDATE(), GETDATE());

-- Location 3: Sân Bóng Bình Thạnh Center (2 fields)
INSERT INTO fields (field_id, location_id, type_id, category_id, description, hourly_rate, created_at, updated_at) VALUES
(7, 3, 2, 2, 'Sân 7 người cỏ tự nhiên B3', 320000, GETDATE(), GETDATE()),
(8, 3, 3, 2, 'Sân 11 người cỏ tự nhiên C2', 550000, GETDATE(), GETDATE());

-- Location 4: Sân Bóng Thủ Đức United (3 fields)
INSERT INTO fields (field_id, location_id, type_id, category_id, description, hourly_rate, created_at, updated_at) VALUES
(9, 4, 1, 1, 'Sân 5 người cỏ nhân tạo A4', 140000, GETDATE(), GETDATE()),
(10, 4, 2, 1, 'Sân 7 người cỏ nhân tạo B4', 240000, GETDATE(), GETDATE()),
(11, 4, 3, 1, 'Sân 11 người cỏ nhân tạo C3', 450000, GETDATE(), GETDATE());

-- Location 5: Sân Bóng Gò Vấp Arena (2 fields)
INSERT INTO fields (field_id, location_id, type_id, category_id, description, hourly_rate, created_at, updated_at) VALUES
(12, 5, 2, 1, 'Sân 7 người cỏ nhân tạo B5', 300000, GETDATE(), GETDATE()),
(13, 5, 3, 2, 'Sân 11 người cỏ tự nhiên C4', 600000, GETDATE(), GETDATE());

-- Optional: Insert some sample location reviews for testing average rating
-- Note: You may need to create the location_reviews table first if it doesn't exist
/*
CREATE TABLE location_reviews (
    review_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    location_id BIGINT NOT NULL,
    user_id BIGINT,
    rating DECIMAL(2,1) NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment NVARCHAR(1000),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (location_id) REFERENCES locations(location_id)
);

INSERT INTO location_reviews (location_id, user_id, rating, comment, created_at, updated_at) VALUES
(1, 1, 4.5, 'Sân đẹp, dịch vụ tốt', GETDATE(), GETDATE()),
(1, 2, 4.0, 'Vị trí thuận tiện', GETDATE(), GETDATE()),
(2, 1, 4.8, 'Chất lượng sân rất tốt', GETDATE(), GETDATE()),
(2, 3, 4.2, 'Giá cả hợp lý', GETDATE(), GETDATE()),
(3, 2, 4.6, 'Cỏ tự nhiên rất đẹp', GETDATE(), GETDATE()),
(4, 1, 4.3, 'Sân rộng rãi, thoáng mát', GETDATE(), GETDATE()),
(5, 3, 4.7, 'Tiêu chuẩn quốc tế', GETDATE(), GETDATE());
*/