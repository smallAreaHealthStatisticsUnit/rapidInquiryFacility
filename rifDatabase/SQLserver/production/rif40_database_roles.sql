-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Creation of sahsuland database roles and users
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
-- Check database is NOT master
--
DECLARE @database_name 	VARCHAR(30)=DB_NAME();
IF (@database_name = 'master')
	RAISERROR('rif40_database_roles.sql: Database is master: %s', 16, 1, @database_name);
GO

--
-- Create database users, roles and schemas for sahsuland/sahsuland_dev
--

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = N'rif40')
	CREATE USER [rif40] FOR LOGIN [rif40] WITH DEFAULT_SCHEMA=[dbo]
	ELSE ALTER USER [rif40] WITH LOGIN=[rif40];
GO

IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif40')
EXEC('CREATE SCHEMA [rif40] AUTHORIZATION [rif40]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif_data')
EXEC('CREATE SCHEMA [rif_data] AUTHORIZATION [rif40]');
GO
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif_studies')
EXEC('CREATE SCHEMA [rif_studies] AUTHORIZATION [rif40]');
GO

ALTER USER [rif40] WITH DEFAULT_SCHEMA=[rif40];
GO

ALTER LOGIN [rif40] WITH DEFAULT_DATABASE = [$(NEWDB)];
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
GRANT CREATE TYPE TO [rif40];
GO
--
-- Allow SHOWPLAN
--
GRANT SHOWPLAN TO [rif40];
GO

--
-- Grant USAGE on the rif_studies schema to RIF40. This implies control
--
GRANT ALTER ON SCHEMA :: rif_studies TO [rif40];
GO

SELECT name, type_desc FROM sys.database_principals WHERE name LIKE '%rif%';
GO

--
-- Eof (rif40_database_roles.sql)
