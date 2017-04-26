-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Creation of production database test user
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
-- Usage: sqlcmd -E -b -m-1 -e -r1 -i rif40_production_user.sql -v newuser=%NEWUSER% -v newdb=%NEWDB% -v newpw=%NEWPW%
-- Connect flags if required: -E -S<myServerinstanceName>
--
-- User is created with rif_user (can create tables and views), rif_manager (can also create procedures and functions), can do BULK INSERT
-- User can use sahsuland, sahsuland_dev and test databases.
-- The test database is for geospatial processing and does not have the rif_user and rif_manager roles, the user can create tables, views,
-- procedures and function and do BULK INSERTs
-- Will fail to re-create a user if the user already has objects (tables, views etc)
--

--
-- Expects: $(NEWUSER), $(NEWPW) and $(NEWDB) to set in sqlcmd
--
USE [master];
GO

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
-- Test $(NEWDB)
--
DECLARE @newdb VARCHAR(MAX)='$(NEWDB)';
DECLARE @invalid_chars INTEGER;
DECLARE @first_char VARCHAR(1);
SET @invalid_chars=PATINDEX('%[^0-9a-z_]%', @newdb);
SET @first_char=SUBSTRING(@newdb, 1, 1);
IF @invalid_chars IS NULL
	RAISERROR('New database name is null', 16, 1, @newdb);
ELSE IF @invalid_chars > 0
	RAISERROR('New database name: %s contains invalid character(s) starting at position: %i.', 16, 1, 
		@newdb, @invalid_chars);
ELSE IF (LEN(@newdb) > 30) 
	RAISERROR('New database name: %s is too long (30 characters max).', 16, 1, @newdb);
ELSE IF ISNUMERIC(@first_char) = 1
	RAISERROR('First character in database name: %s is numeric: %s.', 16, 1, @newdb, @first_char);
ELSE 
	PRINT 'New database name: ' + @newdb + ' OK';
GO

--
-- Test $(NEWUSER)
--	
DECLARE @newuser VARCHAR(MAX)='$(NEWUSER)';
DECLARE @invalid_chars INTEGER;
DECLARE @first_char VARCHAR(1);
SET @invalid_chars=PATINDEX('%[^0-9a-z_]%', @newuser);
SET @first_char=SUBSTRING(@newuser, 1, 1);
IF @invalid_chars IS NULL
	RAISERROR('New username is null', 16, 1, @newuser);
ELSE IF @invalid_chars > 0
	RAISERROR('New username: %s contains invalid character(s) starting at position: %i.', 16, 1, 
		@newuser, @invalid_chars);
ELSE IF (LEN(@newuser) > 30) 
	RAISERROR('New username: %s is too long (30 characters max).', 16, 1, @newuser);
ELSE IF ISNUMERIC(@first_char) = 1
	RAISERROR('First character in username: %s is numeric: %s.', 16, 1, @newuser, @first_char);
ELSE 
	PRINT 'New username: ' + @newuser + ' OK';	
GO

IF NOT EXISTS (SELECT * FROM sys.sql_logins WHERE name = N'$(NEWUSER)')
CREATE LOGIN [$(NEWUSER)] WITH PASSWORD='$(NEWPW)', CHECK_POLICY = OFF;
GO

ALTER LOGIN [$(NEWUSER)] WITH DEFAULT_DATABASE = [$(NEWDB)];
GO	
	
USE [$(NEWDB)];

:r rif40_user_objects.sql

--
-- Eof (rif40_production_user.sql)
