-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 tables creation
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
-- Usage: sqlcmd -d sahsuland_dev -b -m-1 -e -i ..\sahsuland_dev\rif40\tables\recreate_all_tables.sql
--
-- MUST BE RUN AS ADMINSTRATOR SO CAN CREATE OBJECTS OR RUN AS RIF40 (with -U rif40)
--
:on error exit
SET QUOTED_IDENTIFIER ON;
-- SET STATISTICS TIME ON;

--
-- Use a single transaction
--
BEGIN TRANSACTION;
GO

-- This script must be run from the installation directory
:r ..\sahsuland_dev\rif40\tables\rif40_version.sql
-- :r ..\sahsuland_dev\rif40\tables\rif40_a_and_e.sql
:r ..\sahsuland_dev\rif40\tables\rif40_age_group_names.sql
:r ..\sahsuland_dev\rif40\tables\rif40_geographies.sql
:r ..\sahsuland_dev\rif40\tables\rif40_health_study_themes.sql
-- :r ..\sahsuland_dev\rif40\tables\rif40_icd10.sql
-- :r ..\sahsuland_dev\rif40\tables\rif40_icd9.sql
-- :r ..\sahsuland_dev\rif40\tables\rif40_icd_o_3.sql
-- :r ..\sahsuland_dev\rif40\tables\rif40_opcs4.sql
:r ..\sahsuland_dev\rif40\tables\rif40_outcomes.sql
:r ..\sahsuland_dev\rif40\tables\rif40_reference_tables.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_projects.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_num_denom.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_parameters.sql
:r ..\sahsuland_dev\rif40\tables\rif40_error_messages.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_user_projects.sql
:r ..\sahsuland_dev\rif40\tables\rif40_tables_and_views.sql
:r ..\sahsuland_dev\rif40\tables\rif40_age_groups.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_geolevels.sql
:r ..\sahsuland_dev\rif40\tables\rif40_covariates.sql
:r ..\sahsuland_dev\rif40\tables\rif40_outcome_groups.sql
:r ..\sahsuland_dev\rif40\tables\rif40_predefined_groups.sql
:r ..\sahsuland_dev\rif40\tables\rif40_tables.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_studies.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_investigations.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_results.sql
:r ..\sahsuland_dev\rif40\tables\rif40_table_outcomes.sql
:r ..\sahsuland_dev\rif40\tables\rif40_study_shares.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_comparison_areas.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_contextual_stats.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_fdw_tables.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_study_sql_log.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_study_sql.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_inv_covariates.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_inv_conditions.sql
:r ..\sahsuland_dev\rif40\tables\rif40_columns.sql
:r ..\sahsuland_dev\rif40\tables\rif40_triggers.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_study_areas.sql
:r ..\sahsuland_dev\rif40\tables\t_rif40_study_status.sql
GO

COMMIT;
GO