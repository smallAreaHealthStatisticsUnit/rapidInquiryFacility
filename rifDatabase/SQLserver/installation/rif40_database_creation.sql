-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Creation of sahsuland_dev database
--
-- Copyright:
--
-- The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
-- that rapidly addresses epidemiological and public health questions using 
-- routinely collected health and population data and generates standardised 
-- rates and relative risks for any given health outcome, for specified age 
-- and year ranges, for any given geographical area.
--
-- Copyright 2014 Imperial College London, developed by the Small Area
-- Health Statistics Unit. The work of the Small Area Health Statistics Unit 
-- is funded by the Public Health England as part of the MRC-PHE Centre for 
-- Environment and Health. Funding for this project has also been received 
-- from the Centers for Disease Control and Prevention.  
--
-- This file is part of the Rapid Inquiry Facility (RIF) project.
-- RIF is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- RIF is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public License
-- along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
-- to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
-- Boston, MA 02110-1301 USA
--
-- Author:
--
-- Peter Hambly, SAHSU
--
-- THIS SCRIPT MUST BE RUN AS ADMINSITRATOR (i.e. in an administrator commnd window: https://technet.microsoft.com/en-us/library/cc947813(v=ws.10).aspx)
--
-- MS SQL Server specific parameters
--
-- Usage: sqlcmd -E -b -m-1 -e -r1 -i rif40_database_creation.sql 
-- Connect flags if required: -E -S<myServerinstanceName>
--

-- 
-- Use master DB
--
USE master;

--
-- Check user is an adminstrator
--
GO
DECLARE @CurrentUser sysname
SELECT @CurrentUser = user_name(); 
IF IS_SRVROLEMEMBER('sysadmin') = 1
	PRINT 'User: ' + @CurrentUser + ' OK';
ELSE
	RAISERROR('User: %s is not an administrator.', 16, 1, @CurrentUser);
GO
	
--
-- Re-create databases. This will destroy all existing users and data
--
IF EXISTS(SELECT * FROM sys.sysdatabases where name='sahsuland_dev')
	DROP DATABASE sahsuland_dev;
GO	
CREATE DATABASE sahsuland_dev;
GO

IF EXISTS(SELECT * FROM sys.sysdatabases where name='sahsuland')
	DROP DATABASE sahsuland;
GO	
CREATE DATABASE sahsuland;
GO

IF EXISTS(SELECT * FROM sys.sysdatabases where name='test')
	DROP DATABASE test;
GO
CREATE DATABASE test;
GO

IF EXISTS (SELECT * FROM sys.sql_logins WHERE name = N'rif40')
DROP LOGIN rif40;
GO

SELECT name FROM sys.server_principals;
GO

--
-- Re-create logins
--
IF EXISTS (SELECT name FROM sys.server_principals WHERE name = N'rif40')
	DROP LOGIN [rif40];
GO
IF EXISTS (SELECT name FROM sys.server_principals WHERE name = N'rifuser')
	DROP LOGIN [rifuser];
GO
IF EXISTS (SELECT name FROM sys.server_principals WHERE name = N'rifmanager')
	DROP LOGIN [rifmanager];
GO
--
-- RIF40: Schemn owner
--
CREATE LOGIN [rif40] WITH PASSWORD='rif40', CHECK_POLICY = OFF;	-- Chnage this password if your SQL Server database is networked!
GO

--
-- Allow BULK INSERT
--
EXEC sp_addsrvrolemember @loginame = N'rif40', @rolename = N'bulkadmin';
GO

--
-- RIFUSER: Test user
--
CREATE LOGIN [rifuser] WITH PASSWORD='rifuser', CHECK_POLICY = OFF; -- Chnage this password if your SQL Server database is networked!
GO

--
-- RIFMANAGER: Test manager
--
CREATE LOGIN [rifmanager] WITH PASSWORD='rifmanager', CHECK_POLICY = OFF; -- Chnage this password if your SQL Server database is networked!
GO

SELECT name FROM sys.server_principals WHERE name LIKE 'rif%';
GO

--
-- Create database users, roles and schemas for sahsuland and sahsuland_dev
--
USE [sahsuland_dev];

-- 
-- 1. USers
--
-- This will only work if the user has no objects
--
IF EXISTS (SELECT name FROM sys.database_principals WHERE name = N'rif40')
	DROP USER [rif40];
GO
CREATE USER [rif40] FOR LOGIN [rif40] WITH DEFAULT_SCHEMA=[dbo];
GO
IF EXISTS (SELECT name FROM sys.database_principals WHERE name = N'rifuser')
	DROP USER [rifuser];
GO
CREATE USER [rifuser] FOR LOGIN [rifuser] WITH DEFAULT_SCHEMA=[dbo];
GO
IF EXISTS (SELECT name FROM sys.database_principals WHERE name = N'rifmanager')
	DROP USER [rifmanager];
GO
CREATE USER [rifmanager] FOR LOGIN [rifmanager] WITH DEFAULT_SCHEMA=[dbo];
GO

--
-- Schemas
--	
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif40')
EXEC('CREATE SCHEMA [rif40] AUTHORIZATION [rif40]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rifuser')
EXEC('CREATE SCHEMA [rifuser] AUTHORIZATION [rifuser]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rifmanager')
EXEC('CREATE SCHEMA [rifmanager] AUTHORIZATION [rifmanager]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif_data')
EXEC('CREATE SCHEMA [rif_data] AUTHORIZATION [rif40]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif_studies')
EXEC('CREATE SCHEMA [rif_studies] AUTHORIZATION [rif40]');
GO

--
-- Default schemas
--
ALTER USER [rif40] WITH DEFAULT_SCHEMA=[rif40];
GO
ALTER USER [rifuser] WITH DEFAULT_SCHEMA=[rifuser];
GO
ALTER USER [rifmanager] WITH DEFAULT_SCHEMA=[rifmanager];
GO

--
-- Default per database (not server!) roles
--
IF DATABASE_PRINCIPAL_ID('rif_manager') IS NULL
	CREATE ROLE [rif_manager];
GO
IF DATABASE_PRINCIPAL_ID('rif_user') IS NULL
	CREATE ROLE [rif_user];
GO
IF DATABASE_PRINCIPAL_ID('rif_student') IS NULL
	CREATE ROLE [rif_student];
GO	
IF DATABASE_PRINCIPAL_ID('rif_no_suppression') IS NULL
	CREATE ROLE [rif_no_suppression];
GO
IF DATABASE_PRINCIPAL_ID('notarifuser') IS NULL
	CREATE ROLE [notarifuser];
GO

--
-- Object privilege grants
--
GRANT CREATE FUNCTION TO [rif_manager];
GO
GRANT CREATE PROCEDURE TO [rif_manager];
GO
GRANT CREATE TABLE TO [rif_manager];
GO
GRANT CREATE VIEW TO [rif_manager];
GO
GRANT CREATE TABLE TO [rif_user];
GO
GRANT CREATE VIEW TO [rif_user];
GO

GRANT CREATE FUNCTION TO [rif40];
GO
GRANT CREATE PROCEDURE TO [rif40];
GO
GRANT CREATE TABLE TO [rif40];
GO
GRANT CREATE VIEW TO [rif40];
GO
GRANT CREATE TYPE TO [rif40];
GO

GRANT ALTER ON SCHEMA :: rif_studies TO [rif40];
GO

--
-- Grant roles to users
--
EXEC sp_addrolemember @membername = N'rifuser', @rolename = N'rif_user'; 
GO
EXEC sp_addrolemember @membername = N'rif40', @rolename = N'rif_user'; 
GO
EXEC sp_addrolemember @membername = N'rifmanager', @rolename = N'rif_user'; 
GO
EXEC sp_addrolemember @membername = N'rifmanager', @rolename = N'rif_manager'; 
GO

SELECT name, type_desc FROM sys.database_principals WHERE name LIKE '%rif%';
GO

--
-- Repeat for sahsuland
--
USE [sahsuland];

IF EXISTS (SELECT name FROM sys.database_principals WHERE name = N'rif40')
	DROP USER [rif40];
GO
CREATE USER [rif40] FOR LOGIN [rif40] WITH DEFAULT_SCHEMA=[dbo];
GO
IF EXISTS (SELECT name FROM sys.database_principals WHERE name = N'rifuser')
	DROP USER [rifuser];
GO
CREATE USER [rifuser] FOR LOGIN [rifuser] WITH DEFAULT_SCHEMA=[dbo];
GO
IF EXISTS (SELECT name FROM sys.database_principals WHERE name = N'rifmanager')
	DROP USER [rifmanager];
GO
CREATE USER [rifmanager] FOR LOGIN [rifmanager] WITH DEFAULT_SCHEMA=[dbo];
GO
	
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif40')
EXEC('CREATE SCHEMA [rif40] AUTHORIZATION [rif40]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rifuser')
EXEC('CREATE SCHEMA [rifuser] AUTHORIZATION [rifuser]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rifmanager')
EXEC('CREATE SCHEMA [rifmanager] AUTHORIZATION [rifmanager]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif_data')
EXEC('CREATE SCHEMA [rif_data] AUTHORIZATION [rif40]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif_studies')
EXEC('CREATE SCHEMA [rif_studies] AUTHORIZATION [rif40]');
GO

ALTER USER [rif40] WITH DEFAULT_SCHEMA=[rif40];
GO
ALTER USER [rifuser] WITH DEFAULT_SCHEMA=[rifuser];
GO
ALTER USER [rifmanager] WITH DEFAULT_SCHEMA=[rifmanager];
GO

ALTER LOGIN [rif40] WITH DEFAULT_DATABASE = [sahsuland_dev];
GO
ALTER LOGIN [rifuser] WITH DEFAULT_DATABASE = [sahsuland_dev];
GO
ALTER LOGIN [rifmanager] WITH DEFAULT_DATABASE = [sahsuland_dev];
GO

--
-- Default per databasxe (not server!) roles
--
IF DATABASE_PRINCIPAL_ID('rif_manager') IS NULL
	CREATE ROLE [rif_manager];
GO
IF DATABASE_PRINCIPAL_ID('rif_user') IS NULL
	CREATE ROLE [rif_user];
GO
IF DATABASE_PRINCIPAL_ID('rif_student') IS NULL
	CREATE ROLE [rif_student];
GO	
IF DATABASE_PRINCIPAL_ID('rif_no_suppression') IS NULL
	CREATE ROLE [rif_no_suppression];
GO
IF DATABASE_PRINCIPAL_ID('notarifuser') IS NULL
	CREATE ROLE [notarifuser];
GO

--
-- Object privilege grants
--
GRANT CREATE FUNCTION TO [rif_manager];
GO
GRANT CREATE PROCEDURE TO [rif_manager];
GO
GRANT CREATE TABLE TO [rif_manager];
GO
GRANT CREATE VIEW TO [rif_manager];
GO
GRANT CREATE TABLE TO [rif_user];
GO
GRANT CREATE VIEW TO [rif_user];
GO

GRANT CREATE FUNCTION TO [rif40];
GO
GRANT CREATE PROCEDURE TO [rif40];
GO
GRANT CREATE TABLE TO [rif40];
GO
GRANT CREATE VIEW TO [rif40];
GO

GRANT ALTER ON SCHEMA :: rif_studies TO [rif40];
GO

--
-- Grant roles to users
--
EXEC sp_addrolemember @membername = N'rifuser', @rolename = N'rif_user'; 
GO
EXEC sp_addrolemember @membername = N'rifmanager', @rolename = N'rif_user'; 
GO
EXEC sp_addrolemember @membername = N'rifmanager', @rolename = N'rif_manager'; 
GO

SELECT name, type_desc FROM sys.database_principals WHERE name LIKE '%rif%';
GO

--
-- Eof