-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 create sahsuland/sahsuland_dev database objects and install data
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
-- Margaret Douglass, Peter Hambly, SAHSU
--
-- MS SQL Server specific parameters
--
-- Usage: sqlcmd -d <sahsuland/sahsuland_dev< -b -m-1 -e -i rif40_sahsuland_dev_install.sql -v path="%cd%\..\.." -I
--
-- MUST BE RUN AS ADMINSTRATOR SO CAN CREATE OBJECTS OR RUN AS RIF40 (with -U rif40)
--
:on error exit
SET QUOTED_IDENTIFIER ON;
-- SET STATISTICS TIME ON;

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

/*
 * Run SQL scripts originally called in order from:
 *
 * rif40_install_sequences.bat
 * rif40_install_tables.bat
 * rif40_install_functions.bat
 * rif40_install_views.bat
 * rif40_install_log_error_handling.bat
 * rif40_install_table_triggers.bat
 * rif40_install_view_triggers.bat
 * rif40_data_install_tables.bat
 */
 
/*

All the constraints have now been named so they can be dropped and recreated. On an earlier database you will get errors like:

Msg 3729, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 6
Cannot DROP FUNCTION 'rif40.rif40_sequence_current_value' because it is being referenced by object 'DF__t_rif40_r__inv_i__12A9974E'.

Fix by running manually:

rif40_drop_all_data.sql
..\sahsuland_dev\rif40\tables\recreate_all_tables.sql

 */
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

:r ..\sahsuland_dev\rif40\sequences\recreate_all_sequences.sql
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

:r rif40_drop_all_data.sql
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

:r ..\sahsuland_dev\rif40\tables\recreate_all_tables.sql
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

:r ..\sahsuland_dev\rif40\functions\recreate_all_functions.sql
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

:r ..\sahsuland_dev\rif40\views\recreate_all_views.sql
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

:r ..\sahsuland_dev\error_handling\recreate_error_handling.sql
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

:r ..\sahsuland_dev\rif40\table_triggers\recreate_table_triggers.sql
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

:r ..\sahsuland_dev\rif40\view_triggers\recreate_view_triggers.sql
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

:r rif40_import_sahsuland.sql
 
DECLARE @CurrentDb sysname
SELECT @CurrentDb = db_name(); 
IF db_name() = 'sahsuland'
	PRINT 'Database: ' + @CurrentDb + ' OK';
ELSE
	RAISERROR('Database: %s is not sahsuland.', 16, 1, @CurrentDb);
GO

PRINT 'All done: RIF40 create sahsuland database objects and install data.';
GO

--
-- Eof