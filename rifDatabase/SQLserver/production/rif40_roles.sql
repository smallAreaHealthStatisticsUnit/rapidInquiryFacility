-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Creation of cluster wide logons and roles
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
-- Check database is master
--
DECLARE @database_name 	VARCHAR(30)=DB_NAME();
IF (@database_name != 'master')
	RAISERROR('rif40_roles.sql: Database is NOT master: %s', 16, 1, @database_name);
GO

--
-- Re-create cluster wide logons and roles
--

--
-- RIF40: Schema owner
--
IF NOT EXISTS (SELECT * FROM sys.sql_logins WHERE name = N'rif40') BEGIN
	DECLARE @sql_stmt NVARCHAR(MAX);
	DECLARE @stp VARCHAR(60)=CAST(GETDATE() AS VARCHAR);
	SET @sql_stmt =	'CREATE LOGIN [rif40] WITH PASSWORD=''' + CONVERT(VARCHAR(32), HASHBYTES('MD5', @stp), 2) + /* PW changes every minute */
		''', CHECK_POLICY = OFF';
	PRINT 'SQL[' + USER + ']> ' + @sql_stmt + ';';
	EXECUTE sp_executesql @sql_stmt;	;	-- Change this password if you want to logon!
END;
GO

--
-- Allow rif40 BULK INSERT
--
EXEC sp_addsrvrolemember @loginame = N'rif40', @rolename = N'bulkadmin';
GO

SELECT name FROM sys.server_principals WHERE name LIKE 'rif%';
GO

--
-- Allow SHOWPLAN
--
GRANT VIEW SERVER STATE TO [rif40];
GO

--
-- Eof (rif40_roles.sql)
