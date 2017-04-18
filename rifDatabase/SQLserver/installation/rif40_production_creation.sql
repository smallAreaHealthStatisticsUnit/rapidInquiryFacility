-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Creation of sahsuland production database
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
-- Expects: $(IMPORT_DIR) and $(NEWDB) to set in sqlcmd
--
	
--
-- Re-create production databass. This will destroy all existing users and data
--
IF EXISTS(SELECT * FROM sys.sysdatabases where name='$(NEWDB)')
	DROP DATABASE $(NEWDB);
GO	
CREATE DATABASE $(NEWDB);
GO

--
-- Re-create logins and roles
--
:r rif40_roles.sql

--
-- Create database users, roles and schemas for $(NEWDB)
--
USE [$(NEWDB)];

:r rif40_database_roles.sql

-- 
-- Use master DB
--
USE master;

--
-- Add custom error messages
--
:r ..\sahsuland_dev\error_handling\rif40_custom_error_messages.sql

--
-- Find the actual database file names for the new $(NEWDB) DB
--
SELECT DB_NAME(mf1.database_id) AS database_name,
	   mf1.physical_name AS physical_db_filename,
	   mf2.physical_name AS physical_log_filename 
  FROM sys.master_files mf1, sys.master_files mf2
 WHERE mf1.database_id = mf2.database_id
   AND mf1.name        = '$(NEWDB)'
   AND mf2.name        = '$(NEWDB)_log';
GO

--
-- Import database from ../production/$(NEWDB)_dev.bak
-- Grant local users read access to this directory
--
DECLARE c1_db CURSOR FOR
	SELECT DB_NAME(mf1.database_id) AS database_name,
	       mf1.physical_name AS physical_db_filename,
	       mf2.physical_name AS physical_log_filename 
	  FROM sys.master_files mf1, sys.master_files mf2
	 WHERE mf1.database_id = mf2.database_id
	   AND mf1.name        = '$(NEWDB)'
	   AND mf2.name        = '$(NEWDB)_log';
DECLARE @database_name 			VARCHAR(30);
DECLARE @physical_db_filename 	VARCHAR(MAX);
DECLARE @physical_log_filename 	VARCHAR(MAX);
DECLARE @crlf  		VARCHAR(2)=CHAR(10)+CHAR(13);

OPEN c1_db;
FETCH NEXT FROM c1_db INTO @database_name, @physical_db_filename, @physical_log_filename;
CLOSE c1_db;
DEALLOCATE c1_db;
	
DECLARE @sql_stmt NVARCHAR(MAX);
SET @sql_stmt =	'RESTORE DATABASE [$(NEWDB)]' + @crlf + 
'        FROM DISK=''$(import_dir)$(NEWDB)_dev.bak''' + @crlf +
'        WITH REPLACE,' + @crlf +
'        MOVE ''$(NEWDB)_dev'' TO ''' + @physical_db_filename + ''',' + @crlf +
'        MOVE ''$(NEWDB)_dev_log'' TO ''' + @physical_log_filename + '''';
PRINT 'SQL[' + USER + ']> ' + @sql_stmt + ';';
EXECUTE sp_executesql @sql_stmt;
GO

--
-- Export database to ../production/$(NEWDB).bak
-- Grant local users full control to this directory
--
BACKUP DATABASE [$(NEWDB)] TO DISK='$(import_dir)$(NEWDB).bak';
GO

--
-- Eof (rif40_production_creation.sql)
