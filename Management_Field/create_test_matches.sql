-- First create some bookings for the open matches
INSERT INTO bookings (
    user_id,
    field_id,
    from_time,
    to_time,
    slots,
    status,
    created_at
) VALUES 
(
    14, -- testuser id
    1,  -- field id
    '2024-08-02T18:00:00Z',
    '2024-08-02T20:00:00Z',
    2,
    'confirmed',
    GETDATE()
),
(
    14, -- testuser id
    2,  -- field id
    '2024-08-03T19:00:00Z',
    '2024-08-03T21:00:00Z',
    2,
    'confirmed',
    GETDATE()
),
(
    14, -- testuser id
    3,  -- field id
    '2024-08-04T17:00:00Z',
    '2024-08-04T19:00:00Z',
    2,
    'confirmed',
    GETDATE()
);

-- Now create open matches linked to these bookings
DECLARE @booking1 BIGINT, @booking2 BIGINT, @booking3 BIGINT;

SET @booking1 = (SELECT TOP 1 booking_id FROM bookings WHERE user_id = 14 AND field_id = 1 ORDER BY created_at DESC);
SET @booking2 = (SELECT TOP 1 booking_id FROM bookings WHERE user_id = 14 AND field_id = 2 ORDER BY created_at DESC);
SET @booking3 = (SELECT TOP 1 booking_id FROM bookings WHERE user_id = 14 AND field_id = 3 ORDER BY created_at DESC);

INSERT INTO open_matches (
    booking_id,
    creator_user_id,
    sport_type,
    slots_needed,
    required_tags,
    status,
    created_at
) VALUES 
(
    @booking1,
    14,
    'Football',
    8,
    '["thiếu tiền vệ", "kỹ thuật tốt", "chơi đồng đội", "sân cỏ tự nhiên"]',
    'OPEN',
    GETDATE()
),
(
    @booking2,
    14,
    'Football',
    6,
    '["thiếu hậu vệ", "phòng ngự", "tối chơi", "quận 7"]',
    'OPEN',
    GETDATE()
),
(
    @booking3,
    14,
    'Football',
    10,
    '["thiếu thủ môn", "kinh nghiệm", "tốc độ cao", "sân 11 người"]',
    'OPEN',
    GETDATE()
);

PRINT 'Test bookings and open matches created successfully';