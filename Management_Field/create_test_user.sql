-- Create test user with sport profiles
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
    status,
    join_date,
    is_discoverable,
    has_completed_profile,
    sport_profiles
) VALUES (
    'testuser',
    '$2b$12$6YHGx0DRhhQuhIepIAE1o.hhCI4D3wIoja/6IEb4WoyhOL6bq0E6q', -- password: 123123a
    'testuser@example.com',
    'Test User',
    '+84901234567',
    'Test Address',
    'LOCAL',
    NULL,
    1, -- is_active = true
    1, -- email_verified = true
    NULL, -- verification_token
    NULL, -- verification_token_expiry
    NULL, -- reset_password_token
    NULL, -- reset_password_token_expiry
    'ACTIVE',
    GETDATE(),
    1,
    1,
    '{"FOOTBALL":{"sport":"FOOTBALL","skill":5,"tags":["Bóng đá 5 người","Bóng đá 7 người"]}}'
);

-- Assign USER role to test user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'testuser' AND r.name = 'ROLE_USER';

PRINT 'Test user created successfully';