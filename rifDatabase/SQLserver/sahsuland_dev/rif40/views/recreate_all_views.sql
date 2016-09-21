-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 views creation
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
-- Usage: sqlcmd -d sahsuland_dev -b -m-1 -e -i ..\sahsuland_dev\rif40\views\recreate_all_views.sql
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
:r ..\sahsuland_dev\rif40\views\rif40_comparison_areas.sql
:r ..\sahsuland_dev\rif40\views\rif40_contextual_stats.sql
--:r ..\sahsuland_dev\rif40\views\rif40_fdw_tables.sql
:r ..\sahsuland_dev\rif40\views\rif40_geolevels.sql
:r ..\sahsuland_dev\rif40\views\rif40_investigations.sql
:r ..\sahsuland_dev\rif40\views\rif40_inv_conditions.sql
:r ..\sahsuland_dev\rif40\views\rif40_inv_covariates.sql
:r ..\sahsuland_dev\rif40\views\rif40_parameters.sql
:r ..\sahsuland_dev\rif40\views\rif40_projects.sql
:r ..\sahsuland_dev\rif40\views\rif40_studies.sql
:r ..\sahsuland_dev\rif40\views\rif40_study_sql.sql
:r ..\sahsuland_dev\rif40\views\rif40_study_sql_log.sql
:r ..\sahsuland_dev\rif40\views\rif40_user_projects.sql
:r ..\sahsuland_dev\rif40\views\rif40_user_version.sql
:r ..\sahsuland_dev\rif40\views\rif40_results.sql
:r ..\sahsuland_dev\rif40\views\rif40_num_denom.sql
:r ..\sahsuland_dev\rif40\views\rif40_numerator_outcome_columns.sql
:r ..\sahsuland_dev\rif40\views\rif40_num_denom_errors.sql
:r ..\sahsuland_dev\rif40\views\rif40_study_areas.sql

GO

COMMIT;
GO