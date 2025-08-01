USE Project4_VN;
GO

PRINT '--- Starting cleanup of all related tables in correct dependency order ---';

-- Level 1: Tables with the most dependencies (many-to-many, join tables, etc.)
DELETE FROM dbo.open_match_participants;
DELETE FROM dbo.invitations;
DELETE FROM dbo.team_rosters;
DELETE FROM dbo.matches;
DELETE FROM dbo.participating_teams;
DELETE FROM dbo.field_reviews;
DELETE FROM dbo.location_reviews;
DELETE FROM dbo.notifications;
DELETE FROM dbo.booking_users;
DELETE FROM dbo.payments;
DELETE FROM dbo.user_roles;

-- Level 2: Tables that Level 1 depends on
DELETE FROM dbo.open_matches;
DELETE FROM dbo.bookings;
DELETE FROM dbo.field_closure;
DELETE FROM dbo.holiday_closure;
DELETE FROM dbo.global_closure;
DELETE FROM dbo.operating_hours;
DELETE FROM dbo.refresh_tokens;

-- Level 3: Core entities that Level 2 depends on
DELETE FROM dbo.fields;
DELETE FROM dbo.field_categories;
DELETE FROM dbo.field_types;
DELETE FROM dbo.teams;
DELETE FROM dbo.tournaments;
DELETE FROM dbo.admins;
DELETE FROM dbo.locations;
DELETE FROM dbo.owners;

-- Level 3.5: Deleting Draft Matches and all their dependencies
DELETE FROM dbo.draft_match_user_status; -- FIX: Added this line to resolve the new foreign key conflict.
DELETE FROM dbo.draft_match_interested_users;
DELETE FROM dbo.draft_matches; 

-- Level 4: The root entities
DELETE FROM dbo.users;
DELETE FROM dbo.roles;

PRINT '--- All data deleted successfully ---';
GO

-- Reset identity for all tables that have it
PRINT '--- Reseeding identity columns ---';

DBCC CHECKIDENT ('dbo.users', RESEED, 0);
DBCC CHECKIDENT ('dbo.roles', RESEED, 0);
DBCC CHECKIDENT ('dbo.owners', RESEED, 0);
DBCC CHECKIDENT ('dbo.locations', RESEED, 0);
DBCC CHECKIDENT ('dbo.field_types', RESEED, 0);
DBCC CHECKIDENT ('dbo.field_categories', RESEED, 0);
DBCC CHECKIDENT ('dbo.fields', RESEED, 0);
DBCC CHECKIDENT ('dbo.bookings', RESEED, 0);
DBCC CHECKIDENT ('dbo.payments', RESEED, 0);
DBCC CHECKIDENT ('dbo.notifications', RESEED, 0);
DBCC CHECKIDENT ('dbo.open_matches', RESEED, 0);
DBCC CHECKIDENT ('dbo.open_match_participants', RESEED, 0);
DBCC CHECKIDENT ('dbo.invitations', RESEED, 0);
DBCC CHECKIDENT ('dbo.teams', RESEED, 0);
DBCC CHECKIDENT ('dbo.tournaments', RESEED, 0);
DBCC CHECKIDENT ('dbo.matches', RESEED, 0);
DBCC CHECKIDENT ('dbo.field_reviews', RESEED, 0);
DBCC CHECKIDENT ('dbo.location_reviews', RESEED, 0);
DBCC CHECKIDENT ('dbo.operating_hours', RESEED, 0);
DBCC CHECKIDENT ('dbo.field_closure', RESEED, 0);
DBCC CHECKIDENT ('dbo.holiday_closure', RESEED, 0);
DBCC CHECKIDENT ('dbo.global_closure', RESEED, 0);
DBCC CHECKIDENT ('dbo.admins', RESEED, 0);

PRINT '--- Identity reseeding completed ---';
GO