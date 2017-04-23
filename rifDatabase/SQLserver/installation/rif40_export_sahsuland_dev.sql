-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 export sahsuland_dev to ..\production directory
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
-- Usage: sqlcmd -d <sahsuland/sahsuland_dev> -b -m-1 -e -i rif40_export_sahsuland_dev.sql -v export_dir="%cd%\..\production\"
--
-- MUST BE RUN AS ADMINSTRATOR SO CAN CREATE OBJECTS OR RUN AS RIF40 (with -U rif40)
--

--
-- Expects: $(EXPORT_DIR) to set in sqlcmd
--
SELECT DB_NAME(mf1.database_id) AS database_name,
	   mf1.physical_name AS physical_db_filename
  FROM sys.master_files mf1
 WHERE DB_NAME(mf1.database_id) = 'sahsuland_dev';
GO

--
-- Export database to ../production/sahsuland_dev.bak
-- Grant local users full control to this directory
--
BACKUP DATABASE [sahsuland_dev] TO DISK='$(export_dir)sahsuland_dev.bak' 
  WITH COPY_ONLY, INIT;
GO
/*
Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 6
Processed 42040 pages for database 'sahsuland_dev', file 'sahsuland_dev' on file 54.
Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 6
Processed 2 pages for database 'sahsuland_dev', file 'sahsuland_dev_log' on file 54.
Msg 3014, Level 0, State 1, Server PETER-PC\SAHSU, Line 6
BACKUP DATABASE successfully processed 42042 pages in 3.940 seconds (83.363 MB/sec).
 */
--
-- Eof (rif40_export_sahsuland_dev.sql)