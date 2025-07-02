-- SQL Server Script to Truncate All Transactional and Master Data
-- Run this before inserting new sample data to ensure a clean state.

-- Use the target database
USE Project4;
GO

-- Disable foreign key constraints on all tables to avoid order issues
-- Comment out sp_msforeachtable as it may cause issues
-- EXEC sp_msforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all";

PRINT 'Deleting all data from tables...';

-- Delete data in reverse dependency order to avoid foreign key issues
-- Using DELETE instead of TRUNCATE to handle foreign key constraints

-- Transactional data (delete first)
IF OBJECT_ID('dbo.payments', 'U') IS NOT NULL DELETE FROM dbo.payments;
IF OBJECT_ID('dbo.booking_users', 'U') IS NOT NULL DELETE FROM dbo.booking_users;
IF OBJECT_ID('dbo.field_reviews', 'U') IS NOT NULL DELETE FROM dbo.field_reviews;
IF OBJECT_ID('dbo.location_reviews', 'U') IS NOT NULL DELETE FROM dbo.location_reviews;
IF OBJECT_ID('dbo.notifications', 'U') IS NOT NULL DELETE FROM dbo.notifications;
IF OBJECT_ID('dbo.matches', 'U') IS NOT NULL DELETE FROM dbo.matches;
IF OBJECT_ID('dbo.participating_teams', 'U') IS NOT NULL DELETE FROM dbo.participating_teams;
IF OBJECT_ID('dbo.team_rosters', 'U') IS NOT NULL DELETE FROM dbo.team_rosters;
IF OBJECT_ID('dbo.friends', 'U') IS NOT NULL DELETE FROM dbo.friends;

-- Closure and operating hours data
IF OBJECT_ID('dbo.operating_hours', 'U') IS NOT NULL DELETE FROM dbo.operating_hours;
IF OBJECT_ID('dbo.field_closure', 'U') IS NOT NULL DELETE FROM dbo.field_closure;
IF OBJECT_ID('dbo.holiday_closure', 'U') IS NOT NULL DELETE FROM dbo.holiday_closure;
IF OBJECT_ID('dbo.global_closure', 'U') IS NOT NULL DELETE FROM dbo.global_closure;

-- Core data (child to parent order)
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL DELETE FROM dbo.bookings;
IF OBJECT_ID('dbo.fields', 'U') IS NOT NULL DELETE FROM dbo.fields;
IF OBJECT_ID('dbo.field_categories', 'U') IS NOT NULL DELETE FROM dbo.field_categories;
IF OBJECT_ID('dbo.field_types', 'U') IS NOT NULL DELETE FROM dbo.field_types;
IF OBJECT_ID('dbo.locations', 'U') IS NOT NULL DELETE FROM dbo.locations;
IF OBJECT_ID('dbo.teams', 'U') IS NOT NULL DELETE FROM dbo.teams;
IF OBJECT_ID('dbo.tournaments', 'U') IS NOT NULL DELETE FROM dbo.tournaments;

-- User and Auth related data
IF OBJECT_ID('dbo.refresh_tokens', 'U') IS NOT NULL DELETE FROM dbo.refresh_tokens;
IF OBJECT_ID('dbo.user_roles', 'U') IS NOT NULL DELETE FROM dbo.user_roles;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DELETE FROM dbo.users;
IF OBJECT_ID('dbo.roles', 'U') IS NOT NULL DELETE FROM dbo.roles;
IF OBJECT_ID('dbo.owners', 'U') IS NOT NULL DELETE FROM dbo.owners;
IF OBJECT_ID('dbo.admins', 'U') IS NOT NULL DELETE FROM dbo.admins;

PRINT 'All data has been deleted successfully.';
GO

-- Reset identity values for tables that have identity columns
IF OBJECT_ID('dbo.owners', 'U') IS NOT NULL DBCC CHECKIDENT ('dbo.owners', RESEED, 0);
IF OBJECT_ID('dbo.locations', 'U') IS NOT NULL DBCC CHECKIDENT ('dbo.locations', RESEED, 0);
IF OBJECT_ID('dbo.field_types', 'U') IS NOT NULL DBCC CHECKIDENT ('dbo.field_types', RESEED, 0);
IF OBJECT_ID('dbo.field_categories', 'U') IS NOT NULL DBCC CHECKIDENT ('dbo.field_categories', RESEED, 0);
IF OBJECT_ID('dbo.fields', 'U') IS NOT NULL DBCC CHECKIDENT ('dbo.fields', RESEED, 0);
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL DBCC CHECKIDENT ('dbo.bookings', RESEED, 0);

PRINT 'Identity values have been reset.';
PRINT 'Database is now clean and ready for new data insertion.';
GO