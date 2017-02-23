-- ************************************************************************
-- *
-- * THIS IS A SCHEMA ALTER SCRIPT - IT CAN BE RE-RUN BUT THEY MUST BE RUN 
-- * IN NUMERIC ORDER
-- *
-- ************************************************************************
--
-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- $Id$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF alter script 9 - Misc integration fixes
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
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #9 Misc integration fixes.

/*

* Alter 9: Misc integration fixes

  1. Replace old geosptial build code with new data loader. Obsolete t_rif40_sahsu_geometry/t_rif40_sahsu_maptiles; 
     use rif40_geolevels lookup_table/tile_table
  2. Make RIF40_TABLES.THEME nullable for denominators
  3. INSERT INTO rif40_table_outcomes wrong OUTCOME_GROUP_NAME used in v4_0_postgres_sahsuland_imports.sql, suspect ICD hard coded. [Not a bug]
  4. Fix:
     * RIF40_NUMERATOR_OUTCOME_COLUMNS.COLUMNN_EXISTS to COLUMN_EXISTS
     * T_RIF40_CONTEXTUAL_STATS/RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISION_POPULATION to TOTAL_COMPARISON_POPULATION
  5. Resolve: RIF40_PARAMETERS.DESCRIPTION (SQL Server) or PARAM_DESCRIPTION (Postgres)

 */
BEGIN;
--
--  1. Replace old geosptial build code with new data loader. Obsolete t_rif40_sahsu_geometry/t_rif40_sahsu_maptiles; 
--     use rif40_geolevels lookup_table/tile_table
--
-- Load new tileMaker SAHSULAND
--
--\i ../../GeospatialData/tileMaker/rif_pg_SAHSULAND.sql

--
--  2. Make RIF40_TABLES.THEME nullable for denominators
--
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE rif40_tables ALTER COLUMN theme DROP NOT NULL;
EXCEPTION
	WHEN undefined_column THEN
		RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;  
END;
$$;

--
--  3. INSERT INTO rif40_table_outcomes wrong OUTCOME_GROUP_NAME used in v4_0_postgres_sahsuland_imports.sql, suspect ICD hard coded. 
--     [Not a bug]
--

--  4. Fix:
--     * RIF40_NUMERATOR_OUTCOME_COLUMNS.COLUMNN_EXISTS to COLUMN_EXISTS
--     * T_RIF40_CONTEXTUAL_STATS/RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISION_POPULATION to TOTAL_COMPARISON_POPULATION 
--		 (Fixed in core build: too many interactions)
--
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE rif40_tables RENAME COLUMN columnn_exists TO column_exists;
	UPDATE rif40_columns
 	   SET column_name_hide = 'column_exists'
 	 WHERE column_name_hide = 'columnn_exists' 
	   AND table_or_view_name_hide = 'rif40_tables';
EXCEPTION
	WHEN undefined_column THEN
		RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;  
END;
$$;
   
--
--  5. Resolve: RIF40_PARAMETERS.DESCRIPTION (SQL Server) from PARAM_DESCRIPTION (Postgres)
--     Stick with Postgres
--
/*
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE rif40_parameters RENAME COLUMN param_description TO description; 
EXCEPTION
	WHEN undefined_column THEN
		RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;  
END;
$$; */
--
-- Missing comments
--
COMMENT ON COLUMN "rif40_parameters"."param_description" IS 'Description';
COMMENT ON COLUMN "rif40_parameters"."param_name" IS 'Parameter';
COMMENT ON COLUMN "rif40_parameters"."param_value" IS 'Value';
COMMENT ON COLUMN "rif40_projects"."date_ended" IS 'Date project ended';
COMMENT ON COLUMN "rif40_projects"."date_started" IS 'Date project started';
COMMENT ON COLUMN "rif40_projects"."description" IS 'Project description';
COMMENT ON COLUMN "rif40_projects"."project" IS 'Project name';

--
-- Testing stop
--
/*
DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'Stop processing';
END;
$$;
 */  
END;
--
--  Eof 