-- Disable foreign key constraints
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all"

-- Delete data from all tables in correct order (child tables first)
DELETE FROM recruitment_participants;
DELETE FROM booking_users;
DELETE FROM booking_recruitment;
DELETE FROM field_reviews;
DELETE FROM location_reviews;
DELETE FROM notifications;
DELETE FROM matches;
DELETE FROM participating_teams;
DELETE FROM team_rosters;
DELETE FROM bookings;
DELETE FROM payments;
DELETE FROM refresh_tokens;
DELETE FROM holiday_closure;
DELETE FROM global_closure;
DELETE FROM fields;
DELETE FROM field_types;
DELETE FROM field_categories;
DELETE FROM tournaments;
DELETE FROM teams;
DELETE FROM locations;
DELETE FROM owners;
DELETE FROM users;
DELETE FROM roles;

-- Reset identity columns
DBCC CHECKIDENT ('recruitment_participants', RESEED, 0);
DBCC CHECKIDENT ('field_reviews', RESEED, 0);
DBCC CHECKIDENT ('location_reviews', RESEED, 0);
DBCC CHECKIDENT ('notifications', RESEED, 0);
DBCC CHECKIDENT ('matches', RESEED, 0);
DBCC CHECKIDENT ('participating_teams', RESEED, 0);
DBCC CHECKIDENT ('bookings', RESEED, 0);
DBCC CHECKIDENT ('payments', RESEED, 0);
DBCC CHECKIDENT ('refresh_tokens', RESEED, 0);
DBCC CHECKIDENT ('holiday_closure', RESEED, 0);
DBCC CHECKIDENT ('global_closure', RESEED, 0);
DBCC CHECKIDENT ('fields', RESEED, 0);
DBCC CHECKIDENT ('field_types', RESEED, 0);
DBCC CHECKIDENT ('field_categories', RESEED, 0);
DBCC CHECKIDENT ('tournaments', RESEED, 0);
DBCC CHECKIDENT ('teams', RESEED, 0);
DBCC CHECKIDENT ('locations', RESEED, 0);
DBCC CHECKIDENT ('owners', RESEED, 0);
DBCC CHECKIDENT ('users', RESEED, 0);
DBCC CHECKIDENT ('roles', RESEED, 0);

-- Re-enable foreign key constraints
EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all"

PRINT 'All data cleared successfully!';