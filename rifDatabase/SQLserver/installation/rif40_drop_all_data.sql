-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Delete everything; truncate if it has not got foreign keys
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
-- Usage: sqlcmd -E -b -m-1 -e -r1 -i rif40_drop_all_data.sql
-- Connect flags if required: -E -S<myServerinstanceName>
--

-- Delete everything; truncate if it has not got foreign keys
-- Disable, delete then enable triggers on rif40_tables, t_rif40_geolevels
--
-- When studies are present they will need to be deleted to.
--

--
-- Old sahsu geography tables
--
IF OBJECT_ID('rif_data.sahsuland_level1', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[sahsuland_level1];
GO
IF OBJECT_ID('rif_data.sahsuland_level2', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[sahsuland_level2];
GO
IF OBJECT_ID('rif_data.sahsuland_level3', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[sahsuland_level3];
GO
IF OBJECT_ID('rif_data.sahsuland_level4', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[sahsuland_level4];
GO
IF OBJECT_ID('rif_data.sahsuland_covariates_level3', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[sahsuland_covariates_level3];
GO
IF OBJECT_ID('rif_data.sahsuland_covariates_level4', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[sahsuland_covariates_level4];
GO
IF OBJECT_ID('rif_data.sahsuland_geography', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[sahsuland_geography];
GO

--
-- ~New sahsuland geography tables
--
IF OBJECT_ID('rif_data.t_tiles_sahsuland', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[t_tiles_sahsuland];
GO
IF OBJECT_ID('rif_data.tiles_sahsuland', 'V') IS NOT NULL DROP VIEW [rif_data].[tiles_sahsuland];
GO
IF OBJECT_ID('rif_data.geometry_sahsuland', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[geometry_sahsuland];
GO
IF OBJECT_ID('rif_data.hierarchy_sahsuland', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[hierarchy_sahsuland];
GO
IF OBJECT_ID('rif_data.lookup_sahsu_grd_level1', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[lookup_sahsu_grd_level1];
GO
IF OBJECT_ID('rif_data.lookup_sahsu_grd_level2', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[lookup_sahsu_grd_level2];
GO
IF OBJECT_ID('rif_data.lookup_sahsu_grd_level3', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[lookup_sahsu_grd_level3];
GO
IF OBJECT_ID('rif_data.lookup_sahsu_grd_level4', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[lookup_sahsu_grd_level4];
GO
IF OBJECT_ID('rif_data.cov_sahsu_grd_level2', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[cov_sahsu_grd_level2];
GO
IF OBJECT_ID('rif_data.cov_sahsu_grd_level3', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[cov_sahsu_grd_level3];
GO
IF OBJECT_ID('rif_data.cov_sahsu_grd_level4', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[cov_sahsu_grd_level4];
GO
IF OBJECT_ID('rif_data.rif40_version', 'U') IS NOT NULL TRUNCATE TABLE [rif40].[rif40_version];
GO
IF OBJECT_ID('rif_data.rif40_age_group_names', 'U') IS NOT NULL TRUNCATE TABLE [rif40].[rif40_age_group_names];
GO
IF OBJECT_ID('rif_data.rif40_age_groups', 'U') IS NOT NULL TRUNCATE TABLE [rif40].[rif40_age_groups];
GO
IF OBJECT_ID('rif_data.rif40_predefined_groups', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_predefined_groups];
GO
IF OBJECT_ID('rif_data.rif40_table_outcomes', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_table_outcomes];
GO
IF OBJECT_ID('rif_data.rif40_outcome_groups', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_outcome_groups];
GO
IF OBJECT_ID('rif_data.rif40_table_outcomes', 'U') IS NOT NULL TRUNCATE TABLE [rif40].[rif40_table_outcomes];
GO

IF EXISTS (SELECT *
             FROM sys.objects
		    WHERE object_id = OBJECT_ID(N'[rif40].[tr_rif40_tables_checks]')
		      AND type IN ( N'TR' ))
	DISABLE TRIGGER [rif40].[tr_rif40_tables_checks] ON [rif40].[rif40_tables];
GO
IF OBJECT_ID('rif_data.rif40_tables', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_tables];
GO
IF EXISTS (SELECT *
             FROM sys.objects
		    WHERE object_id = OBJECT_ID(N'[rif40].[tr_rif40_tables_checks]')
		      AND type IN ( N'TR' ))
	ENABLE TRIGGER [rif40].[tr_rif40_tables_checks] ON [rif40].[rif40_tables];
GO

IF OBJECT_ID('rif_data.rif40_age_group_names', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_age_group_names];
GO
IF OBJECT_ID('rif_data.rif40_reference_tables', 'U') IS NOT NULL TRUNCATE TABLE [rif40].[rif40_reference_tables];
GO
IF OBJECT_ID('rif_data.rif40_outcomes', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_outcomes];
GO
IF OBJECT_ID('rif_data.rif40_health_study_themes', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_health_study_themes];
GO
IF OBJECT_ID('rif_data.rif40_covariates', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_covariates];
GO

IF EXISTS (SELECT *
             FROM sys.objects
		    WHERE object_id = OBJECT_ID(N'[rif40].[tr_geolevel_check]')
		      AND type IN ( N'TR' ))
	DISABLE TRIGGER [rif40].[tr_geolevel_check] ON [rif40].[t_rif40_geolevels];
GO
IF OBJECT_ID('rif_data.t_rif40_geolevels', 'U') IS NOT NULL DELETE FROM [rif40].[t_rif40_geolevels];
GO
IF OBJECT_ID('rif_data.t_rif40_geolevels', 'U') IS NOT NULL DELETE FROM [rif40].[t_rif40_geolevels];
GO
IF EXISTS (SELECT *
             FROM sys.objects
		    WHERE object_id = OBJECT_ID(N'[rif40].[tr_geolevel_check]')
		      AND type IN ( N'TR' ))
	ENABLE TRIGGER [rif40].[tr_geolevel_check] ON [rif40].[t_rif40_geolevels];
GO

IF OBJECT_ID('rif_data.rif40_geographies', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_geographies];
GO
IF OBJECT_ID('rif_data.t_rif40_parameters', 'U') IS NOT NULL TRUNCATE TABLE [rif40].[t_rif40_parameters];
GO
IF OBJECT_ID('rif_data.rif40_columns', 'U') IS NOT NULL TRUNCATE TABLE [rif40].[rif40_columns];
GO
IF OBJECT_ID('rif_data.rif40_triggers', 'U') IS NOT NULL TRUNCATE TABLE [rif40].[rif40_triggers];
GO
IF OBJECT_ID('rif_data.rif40_tables_and_views', 'U') IS NOT NULL DELETE FROM [rif40].[rif40_tables_and_views];
GO

IF OBJECT_ID('rif_data.sahsuland_cancer', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[sahsuland_cancer];
GO
IF OBJECT_ID('rif_data.sahsuland_pop', 'U') IS NOT NULL TRUNCATE TABLE [rif_data].[sahsuland_pop];
GO

--
-- Eof