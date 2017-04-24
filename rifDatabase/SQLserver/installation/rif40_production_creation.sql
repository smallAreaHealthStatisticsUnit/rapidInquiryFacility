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
-- Usage: sqlcmd -E -b -m-1 -e -r1 -i rif40_database_creation.sql -v import_dir="%cd%\..\production\" -v newdb="%NEWDB%"
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

/*

OK:

SQL[dbo]> RESTORE DATABASE [sahsuland]
        FROM DISK='C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\production\sahsuland_dev.bak'
        WITH REPLACE,
        MOVE 'sahsuland_dev' TO 'F:\SqlServer\sahsuland.mdf',
        MOVE 'sahsuland_dev_log' TO 'F:\SqlServer\sahsuland_log.ldf';
Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
Processed 45648 pages for database 'sahsuland', file 'sahsuland_dev' on file 1.
Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
Processed 14 pages for database 'sahsuland', file 'sahsuland_dev_log' on file 1.
Msg 3014, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
RESTORE DATABASE successfully processed 45662 pages in 5.130 seconds (69.538 MB/sec).
SQL[dbo]> ALTER DATABASE [sahsuland] MODIFY FILE ( NAME = sahsuland_dev, NEWNAME = sahsuland);
Msg 5021, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
The file name 'sahsuland' has been set.
SQL[dbo]> ALTER DATABASE [sahsuland] MODIFY FILE ( NAME = sahsuland_dev_log, NEWNAME = sahsuland_log);
Msg 5021, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
The file name 'sahsuland_log' has been set.

 */	

DECLARE @sql_stmt NVARCHAR(MAX);
SET @sql_stmt =	'RESTORE DATABASE [$(NEWDB)]' + @crlf + 
'        FROM DISK=''$(import_dir)sahsuland_dev.bak''' + @crlf +
'        WITH REPLACE,' + @crlf +
'        MOVE ''sahsuland_dev'' TO ''' + @physical_db_filename + ''',' + @crlf +
'        MOVE ''sahsuland_dev_log'' TO ''' + @physical_log_filename + '''';
PRINT 'SQL[' + USER + ']> ' + @sql_stmt + ';';
EXECUTE sp_executesql @sql_stmt;
SET @sql_stmt='ALTER DATABASE [$(NEWDB)] MODIFY FILE ( NAME = sahsuland_dev, NEWNAME = $(NEWDB))';
PRINT 'SQL[' + USER + ']> ' + @sql_stmt + ';';
EXECUTE sp_executesql @sql_stmt;
SET @sql_stmt='ALTER DATABASE [$(NEWDB)] MODIFY FILE ( NAME = sahsuland_dev_log, NEWNAME = $(NEWDB)_log)';
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
-- Wait for 10secs
--
WAITFOR DELAY '00:00:10';
GO

-- 
-- Use master DB
--
USE master;

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
-- Eof (rif40_production_creation.sql)
