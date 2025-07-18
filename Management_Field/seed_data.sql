-- Comprehensive seed script for 50 diverse users with realistic sport profiles
-- This script creates users with varied sport preferences, skill levels, and tags

-- Insert 50 diverse users with realistic sport profiles
INSERT INTO users (
    username, 
    password, 
    email, 
    full_name, 
    phone_number, 
    address, 
    provider, 
    is_active, 
    email_verified, 
    status, 
    join_date, 
    sport_profiles, 
    is_discoverable, 
    has_completed_profile
) VALUES 
-- Football enthusiasts
('minh_striker', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'minh.striker@gmail.com', 'Nguyễn Văn Minh', '0901234567', 'Quận 1, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_DA": {"sport": "BONG_DA", "skill": 4, "tags": ["tiền đạo", "sút tốt", "tốc độ cao", "phối hợp nhóm"]}}', 1, 1),
('duc_midfielder', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'duc.mid@gmail.com', 'Trần Văn Đức', '0901234568', 'Quận 3, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_DA": {"sport": "BONG_DA", "skill": 3, "tags": ["tiền vệ", "chuyền bóng", "kiểm soát", "tầm nhìn tốt"]}}', 1, 1),
('nam_defender', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'nam.def@gmail.com', 'Lê Hoàng Nam', '0901234569', 'Quận 7, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_DA": {"sport": "BONG_DA", "skill": 4, "tags": ["hậu vệ", "phòng ngự", "đánh đầu", "chắc chắn"]}}', 1, 1),
('tuan_goalkeeper', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'tuan.gk@gmail.com', 'Phạm Anh Tuấn', '0901234570', 'Quận 2, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_DA": {"sport": "BONG_DA", "skill": 5, "tags": ["thủ môn", "phản xạ nhanh", "bắt bóng", "chỉ đạo"]}}', 1, 1),

-- Badminton players
('linh_badminton', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'linh.badminton@gmail.com', 'Nguyễn Thị Linh', '0901234571', 'Quận 5, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CAU_LONG": {"sport": "CAU_LONG", "skill": 4, "tags": ["đánh đôi", "smash mạnh", "di chuyển nhanh", "chiến thuật"]}}', 1, 1),
('hoa_shuttler', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'hoa.shuttle@gmail.com', 'Trần Thị Hoa', '0901234572', 'Quận 10, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CAU_LONG": {"sport": "CAU_LONG", "skill": 3, "tags": ["đánh đơn", "phòng thủ", "kiên nhẫn", "kỹ thuật tốt"]}}', 1, 1),
('quan_racket', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'quan.racket@gmail.com', 'Lê Văn Quân', '0901234573', 'Quận 4, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CAU_LONG": {"sport": "CAU_LONG", "skill": 5, "tags": ["tấn công", "drop shot", "net play", "thể lực tốt"]}}', 1, 1),

-- Tennis enthusiasts
('mai_tennis', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'mai.tennis@gmail.com', 'Nguyễn Thị Mai', '0901234574', 'Quận 6, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"TENNIS": {"sport": "TENNIS", "skill": 4, "tags": ["forehand mạnh", "baseline", "topspin", "endurance"]}}', 1, 1),
('hung_serve', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'hung.serve@gmail.com', 'Trần Văn Hùng', '0901234575', 'Quận 8, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"TENNIS": {"sport": "TENNIS", "skill": 3, "tags": ["serve tốt", "volley", "net game", "chiến thuật"]}}', 1, 1),

-- Basketball players
('long_center', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'long.center@gmail.com', 'Phạm Thanh Long', '0901234576', 'Quận 9, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_RO": {"sport": "BONG_RO", "skill": 4, "tags": ["center", "rebound", "post up", "cao to"]}}', 1, 1),
('thao_guard', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'thao.guard@gmail.com', 'Lê Thị Thảo', '0901234577', 'Quận 11, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_RO": {"sport": "BONG_RO", "skill": 3, "tags": ["point guard", "dribbling", "3-point", "tốc độ"]}}', 1, 1),
('khai_forward', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'khai.forward@gmail.com', 'Nguyễn Văn Khải', '0901234578', 'Quận 12, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_RO": {"sport": "BONG_RO", "skill": 4, "tags": ["small forward", "shooting", "versatile", "defense"]}}', 1, 1),

-- Volleyball players
('vy_spiker', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'vy.spike@gmail.com', 'Trần Thị Vy', '0901234579', 'Thủ Đức, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_CHUYEN": {"sport": "BONG_CHUYEN", "skill": 4, "tags": ["spiker", "jump cao", "tấn công", "block tốt"]}}', 1, 1),
('dat_setter', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'dat.setter@gmail.com', 'Lê Văn Đạt', '0901234580', 'Bình Thạnh, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_CHUYEN": {"sport": "BONG_CHUYEN", "skill": 5, "tags": ["setter", "toss chính xác", "chiến thuật", "leadership"]}}', 1, 1),

-- Table tennis players
('son_paddle', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'son.paddle@gmail.com', 'Nguyễn Văn Sơn', '0901234581', 'Gò Vấp, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_BAN": {"sport": "BONG_BAN", "skill": 4, "tags": ["topspin", "phản xạ nhanh", "forehand", "aggressive"]}}', 1, 1),
('lan_defensive', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'lan.def@gmail.com', 'Phạm Thị Lan', '0901234582', 'Tân Bình, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_BAN": {"sport": "BONG_BAN", "skill": 3, "tags": ["defensive", "chop", "kiên nhẫn", "counter attack"]}}', 1, 1),

-- Swimming enthusiasts
('hai_swimmer', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'hai.swim@gmail.com', 'Trần Văn Hải', '0901234583', 'Phú Nhuận, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BOI_LOI": {"sport": "BOI_LOI", "skill": 4, "tags": ["freestyle", "endurance", "technique", "competitive"]}}', 1, 1),
('nga_backstroke', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'nga.back@gmail.com', 'Lê Thị Nga', '0901234584', 'Tân Phú, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BOI_LOI": {"sport": "BOI_LOI", "skill": 3, "tags": ["backstroke", "relaxed", "form tốt", "morning swim"]}}', 1, 1),

-- Running enthusiasts
('duc_runner', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'duc.run@gmail.com', 'Nguyễn Văn Đức', '0901234585', 'Bình Tân, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CHAY_BO": {"sport": "CHAY_BO", "skill": 4, "tags": ["marathon", "endurance", "pace steady", "early morning"]}}', 1, 1),
('huong_sprinter', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'huong.sprint@gmail.com', 'Trần Thị Hương', '0901234586', 'Quận 1, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CHAY_BO": {"sport": "CHAY_BO", "skill": 3, "tags": ["sprint", "tốc độ cao", "interval", "competitive"]}}', 1, 1),

-- Cycling enthusiasts
('binh_cyclist', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'binh.cycle@gmail.com', 'Lê Văn Bình', '0901234587', 'Quận 2, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"DAP_XE": {"sport": "DAP_XE", "skill": 4, "tags": ["road bike", "long distance", "group ride", "weekend"]}}', 1, 1),
('oanh_mountain', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'oanh.mtb@gmail.com', 'Phạm Thị Oanh', '0901234588', 'Quận 3, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"DAP_XE": {"sport": "DAP_XE", "skill": 3, "tags": ["mountain bike", "off-road", "adventure", "technical"]}}', 1, 1),

-- Yoga practitioners
('linh_yoga', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'linh.yoga@gmail.com', 'Nguyễn Thị Linh', '0901234589', 'Quận 4, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"YOGA": {"sport": "YOGA", "skill": 4, "tags": ["hatha yoga", "flexibility", "meditation", "balance"]}}', 1, 1),
('minh_vinyasa', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'minh.vinyasa@gmail.com', 'Trần Văn Minh', '0901234590', 'Quận 5, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"YOGA": {"sport": "YOGA", "skill": 3, "tags": ["vinyasa", "flow", "strength", "mindfulness"]}}', 1, 1),

-- Multi-sport athletes
('thang_multi', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'thang.multi@gmail.com', 'Lê Văn Thắng', '0901234591', 'Quận 6, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_DA": {"sport": "BONG_DA", "skill": 3, "tags": ["tiền vệ", "chuyền bóng"]}, "TENNIS": {"sport": "TENNIS", "skill": 3, "tags": ["baseline", "consistent"]}}', 1, 1),
('phuong_athlete', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'phuong.athlete@gmail.com', 'Nguyễn Thị Phương', '0901234592', 'Quận 7, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CAU_LONG": {"sport": "CAU_LONG", "skill": 4, "tags": ["đánh đôi", "smash"]}, "BONG_CHUYEN": {"sport": "BONG_CHUYEN", "skill": 3, "tags": ["spiker", "jump"]}}', 1, 1),
('hoang_versatile', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'hoang.versatile@gmail.com', 'Trần Văn Hoàng', '0901234593', 'Quận 8, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_RO": {"sport": "BONG_RO", "skill": 3, "tags": ["guard", "shooting"]}, "CHAY_BO": {"sport": "CHAY_BO", "skill": 4, "tags": ["endurance", "pace"]}}', 1, 1),

-- Beginner enthusiasts
('an_beginner', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'an.begin@gmail.com', 'Lê Thị An', '0901234594', 'Quận 9, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_DA": {"sport": "BONG_DA", "skill": 1, "tags": ["mới bắt đầu", "học hỏi", "nhiệt tình"]}}', 1, 1),
('tung_newbie', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'tung.new@gmail.com', 'Phạm Văn Tùng', '0901234595', 'Quận 10, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"TENNIS": {"sport": "TENNIS", "skill": 1, "tags": ["beginner", "eager", "practice"]}}', 1, 1),
('ha_starter', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'ha.start@gmail.com', 'Nguyễn Thị Hà', '0901234596', 'Quận 11, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CAU_LONG": {"sport": "CAU_LONG", "skill": 2, "tags": ["improving", "fun", "social"]}}', 1, 1),

-- Advanced players
('khang_pro', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'khang.pro@gmail.com', 'Trần Văn Khang', '0901234597', 'Quận 12, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_DA": {"sport": "BONG_DA", "skill": 5, "tags": ["professional", "captain", "leadership", "tactical"]}}', 1, 1),
('yen_expert', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'yen.expert@gmail.com', 'Lê Thị Yến', '0901234598', 'Thủ Đức, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"TENNIS": {"sport": "TENNIS", "skill": 5, "tags": ["tournament", "coach", "advanced", "mentor"]}}', 1, 1),
('duy_master', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'duy.master@gmail.com', 'Nguyễn Văn Duy', '0901234599', 'Bình Thạnh, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CAU_LONG": {"sport": "CAU_LONG", "skill": 5, "tags": ["master", "competition", "training", "technique"]}}', 1, 1),

-- Casual players
('linh_casual', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'linh.casual@gmail.com', 'Phạm Thị Linh', '0901234600', 'Gò Vấp, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_RO": {"sport": "BONG_RO", "skill": 2, "tags": ["casual", "fun", "weekend", "social"]}}', 1, 1),
('tam_relaxed', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'tam.relax@gmail.com', 'Trần Văn Tâm', '0901234601', 'Tân Bình, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BOI_LOI": {"sport": "BOI_LOI", "skill": 2, "tags": ["leisure", "health", "relaxing", "slow pace"]}}', 1, 1),
('huyen_social', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'huyen.social@gmail.com', 'Lê Thị Huyền', '0901234602', 'Phú Nhuận, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"YOGA": {"sport": "YOGA", "skill": 2, "tags": ["beginner", "stress relief", "community", "gentle"]}}', 1, 1),

-- Competitive players
('duc_competitive', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'duc.comp@gmail.com', 'Nguyễn Văn Đức', '0901234603', 'Tân Phú, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_BAN": {"sport": "BONG_BAN", "skill": 4, "tags": ["competitive", "tournament", "aggressive", "win-focused"]}}', 1, 1),
('mai_fighter', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'mai.fight@gmail.com', 'Trần Thị Mai', '0901234604', 'Bình Tân, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_CHUYEN": {"sport": "BONG_CHUYEN", "skill": 4, "tags": ["competitive", "intense", "team captain", "strategic"]}}', 1, 1),

-- Fitness enthusiasts
('long_fitness', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'long.fit@gmail.com', 'Phạm Thanh Long', '0901234605', 'Quận 1, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CHAY_BO": {"sport": "CHAY_BO", "skill": 3, "tags": ["fitness", "weight loss", "health", "consistent"]}, "DAP_XE": {"sport": "DAP_XE", "skill": 3, "tags": ["cardio", "endurance", "outdoor"]}}', 1, 1),
('thao_wellness', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'thao.well@gmail.com', 'Lê Thị Thảo', '0901234606', 'Quận 2, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"YOGA": {"sport": "YOGA", "skill": 3, "tags": ["wellness", "mindful", "flexibility", "balance"]}, "BOI_LOI": {"sport": "BOI_LOI", "skill": 3, "tags": ["low impact", "therapeutic", "gentle"]}}', 1, 1),

-- Weekend warriors
('khai_weekend', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'khai.weekend@gmail.com', 'Nguyễn Văn Khải', '0901234607', 'Quận 3, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_DA": {"sport": "BONG_DA", "skill": 3, "tags": ["weekend", "amateur", "fun", "stress relief"]}}', 1, 1),
('vy_saturday', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'vy.sat@gmail.com', 'Trần Thị Vy', '0901234608', 'Quận 4, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"TENNIS": {"sport": "TENNIS", "skill": 3, "tags": ["saturday morning", "recreational", "social", "exercise"]}}', 1, 1),

-- Team players
('dat_team', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'dat.team@gmail.com', 'Lê Văn Đạt', '0901234609', 'Quận 5, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_CHUYEN": {"sport": "BONG_CHUYEN", "skill": 4, "tags": ["team player", "communication", "support", "reliable"]}}', 1, 1),
('son_captain', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'son.cap@gmail.com', 'Nguyễn Văn Sơn', '0901234610', 'Quận 6, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_RO": {"sport": "BONG_RO", "skill": 4, "tags": ["team captain", "leadership", "motivator", "experienced"]}}', 1, 1),

-- Technical players
('lan_technical', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'lan.tech@gmail.com', 'Phạm Thị Lan', '0901234611', 'Quận 7, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"CAU_LONG": {"sport": "CAU_LONG", "skill": 4, "tags": ["technical", "precision", "strategy", "analytical"]}}', 1, 1),
('hai_precise', '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', 'hai.precise@gmail.com', 'Trần Văn Hải', '0901234612', 'Quận 8, TP.HCM', 'LOCAL', 1, 1, 'ACTIVE', NOW(), '{"BONG_BAN": {"sport": "BONG_BAN", "skill": 4, "tags": ["precision", "control", "placement", "tactical"]}}', 1, 1);

-- Assign ROLE_USER to all seeded users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username IN (
    'minh_striker', 'duc_midfielder', 'nam_defender', 'tuan_goalkeeper',
    'linh_badminton', 'hoa_shuttler', 'quan_racket',
    'mai_tennis', 'hung_serve',
    'long_center', 'thao_guard', 'khai_forward',
    'vy_spiker', 'dat_setter',
    'son_paddle', 'lan_defensive',
    'hai_swimmer', 'nga_backstroke',
    'duc_runner', 'huong_sprinter',
    'binh_cyclist', 'oanh_mountain',
    'linh_yoga', 'minh_vinyasa',
    'thang_multi', 'phuong_athlete', 'hoang_versatile',
    'an_beginner', 'tung_newbie', 'ha_starter',
    'khang_pro', 'yen_expert', 'duy_master',
    'linh_casual', 'tam_relaxed', 'huyen_social',
    'duc_competitive', 'mai_fighter',
    'long_fitness', 'thao_wellness',
    'khai_weekend', 'vy_saturday',
    'dat_team', 'son_captain',
    'lan_technical', 'hai_precise'
) AND r.name = 'ROLE_USER';

-- Create some sample booking history for implicit data signals
-- Note: This assumes you have a bookings table. Adjust table name and columns as needed.
-- INSERT INTO bookings (user_id, field_id, booking_date, sport_type, status) VALUES
-- (1, 1, DATE_SUB(NOW(), INTERVAL 7 DAY), 'BONG_DA', 'COMPLETED'),
-- (2, 1, DATE_SUB(NOW(), INTERVAL 14 DAY), 'BONG_DA', 'COMPLETED'),
-- (3, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 'CAU_LONG', 'COMPLETED'),
-- (4, 2, DATE_SUB(NOW(), INTERVAL 10 DAY), 'CAU_LONG', 'COMPLETED'),
-- (5, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), 'TENNIS', 'COMPLETED');

-- Verify the seeded data
SELECT 'Total users seeded:' as info, COUNT(*) as count FROM users WHERE username LIKE '%\_%%';
SELECT 'Users with sport profiles:' as info, COUNT(*) as count FROM users WHERE sport_profiles IS NOT NULL AND sport_profiles != '';
SELECT 'Users with ROLE_USER:' as info, COUNT(*) as count FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE r.name = 'ROLE_USER' AND u.username LIKE '%\_%%';

SELECT 'Sample sport profiles:' as info;
SELECT username, full_name, LEFT(sport_profiles, 100) as sport_profile_preview 
FROM users 
WHERE username IN ('minh_striker', 'linh_badminton', 'mai_tennis', 'thang_multi', 'khang_pro') 
ORDER BY username;