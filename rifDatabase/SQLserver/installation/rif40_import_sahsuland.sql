-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 create data load tables for SAHSULAND data
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
-- Usage: sqlcmd -d sahsuland_dev -b -m-1 -e -i rif40_import_sahsuland.sql -v path="%cd%\..\.." -I
--
-- MUST BE RUN AS ADMINSTRATOR SO CAN CREATE OBJECTS OR RUN AS RIF40 (with -U rif40)
--
:on error exit
SET QUOTED_IDENTIFIER ON;
-- SET STATISTICS TIME ON;

--
-- This script must be run from the installation directory
--

--
-- Use a single transaction
--
BEGIN TRANSACTION;
GO

--
-- Delete everything; truncate if it has not got foreign keys
-- Disable, delete then enable triggers on rif40_tables, t_rif40_geolevels
--
-- When studies are present they will need to be deleted to.
--
:r rif40_drop_all_data.sql

/*
Created using psql with:

\copy rif40_tables_and_views to rif40_tables_and_views.txt with delimiter '|';
\copy rif40_columns to rif40_columns.txt with delimiter '|';

These are the latest versions POST patching

Make sure the files are in Windows format
 */
BULK
INSERT [rif40].[rif40_columns]
FROM '$(path)\SQLserver\sahsuland_dev\rif_data\rif40_columns.txt'
WITH
(
FIELDTERMINATOR = '|',
ROWTERMINATOR = '0x0a'
)
GO
BULK
INSERT [rif40].[rif40_tables_and_views]
FROM '$(path)\SQLserver\sahsuland_dev\rif_data\rif40_tables_and_views.txt'
WITH
(
FIELDTERMINATOR = '|',
ROWTERMINATOR = '0x0a'
)
GO

/*
BULK
INSERT [rif_data].[sahsuland_level1]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_level1.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_level2]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_level2.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_level3]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_level3.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_level4]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_level4.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_covariates_level3]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_covariates_level3.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_covariates_level4]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_covariates_level4.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_geography]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_geography.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO
 */
BULK
INSERT [rif40].[rif40_version]
FROM '$(path)\Postgres\sahsuland\data\rif40_version.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '0x0a'
)
GO

BULK
INSERT [rif40].[rif40_age_groups]
FROM '$(path)\Postgres\sahsuland\data\rif40_age_groups.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '0x0a'
)
GO
BULK
INSERT [rif40].[rif40_age_group_names]
FROM '$(path)\Postgres\sahsuland\data\rif40_age_group_names.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '0x0a'
)
GO

BULK
INSERT [rif40].[rif40_reference_tables]
FROM '$(path)\Postgres\sahsuland\data\rif40_reference_tables.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '0x0a'
)
GO

BULK
INSERT [rif40].[rif40_outcomes]
FROM '$(path)\Postgres\sahsuland\data\rif40_outcomes.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '0x0a'
)
GO

BULK
INSERT [rif40].[rif40_health_study_themes]
FROM '$(path)\Postgres\sahsuland\data\rif40_health_study_themes.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '0x0a'
)
GO
/*
BULK
INSERT [rif40].[rif40_geographies]
FROM '$(path)\Postgres\sahsuland\data\rif40_geographies.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = ''
)
GO

BULK
INSERT [rif40].[t_rif40_geolevels]
FROM '$(path)\Postgres\sahsuland\data\t_rif40_geolevels.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO
*/
/*
BULK
INSERT [rif40].[rif40_tables]
FROM '$(path)\Postgres\sahsuland\data\rif40_tables.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO 

BULK
INSERT [rif40].[rif40_covariates]
FROM '$(path)\Postgres\sahsuland\data\rif40_covariates.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO
*/

BULK
INSERT [rif40].[rif40_outcome_groups]
FROM '$(path)\Postgres\sahsuland\data\rif40_outcome_groups.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '0x0a'
)
GO

BULK
INSERT [rif40].[rif40_table_outcomes]
FROM '$(path)\Postgres\sahsuland\data\rif40_table_outcomes.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '0x0a'
)
GO

/* FOREIGN DATA WRAPPER TABLES - Postgres only!
TRUNCATE TABLE [rif40].[t_rif40_fdw_tables];
GO
BULK
INSERT [rif40].[t_rif40_fdw_tables]
FROM '$(path)\Postgres\sahsuland\data\t_rif40_fdw_tables.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO
*/

BULK
INSERT [rif40].[t_rif40_parameters]
FROM '$(path)\Postgres\sahsuland\data\t_rif40_parameters.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '0x0a'
)

--
-- csv needs to be preprocessed (hence (_mssql version); an alternative is a .fmt file. 
-- These file do not really change; so it is not really worth it
--

BULK
INSERT [rif40].[rif40_triggers]
FROM '$(path)\Postgres\sahsuland\data\rif40_triggers_mssql.csv'
WITH
(
FIELDTERMINATOR = '|',
ROWTERMINATOR = '0x0a'
)
GO

BULK
INSERT [rif40].[rif40_predefined_groups]
FROM '$(path)\Postgres\sahsuland\data\rif40_predefined_groups_mssql.csv'
WITH
(
FIELDTERMINATOR = '|',
ROWTERMINATOR = '0x0a'
)
GO
/*
BULK
INSERT [rif_data].[sahsuland_cancer]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_cancer.csv'
WITH
(
	FORMATFILE = '$(path)\SQLserver\sahsuland_dev\rif_data\sahsuland_cancer.fmt',		-- Use a format file
	TABLOCK																				-- Table lock
)
GO

BULK
INSERT [rif_data].[sahsuland_pop]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_pop.csv'
WITH
(
	FORMATFILE = '$(path)\SQLserver\sahsuland_dev\rif_data\sahsuland_pop.fmt',		-- Use a format file
	TABLOCK																				-- Table lock
)
GO
 */
 
COMMIT;
GO

--
-- Eof
