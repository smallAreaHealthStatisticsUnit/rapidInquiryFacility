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
  6. rif40_GetAdjacencyMatrix.sql: change ST_Touches() to ST_Intersects() to fix missing adjacencies caused by small slivers

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
-- 6. rif40_GetAdjacencyMatrix.sql: change ST_Touches() to ST_Intersects() to fix missing adjacencies caused by small slivers
--
\i ../PLpgsql/rif40_xml_pkg/rif40_GetAdjacencyMatrix.sql

--
-- Needs study 1
--
-- SELECT * FROM rif40_xml_pkg.rif40_GetAdjacencyMatrix(1) LIMIT 10;
  
DROP TABLE IF EXISTS t_rif40_study_status CASCADE;
CREATE TABLE t_rif40_study_status (
	username		VARCHAR(90) NOT NULL 	DEFAULT (current_user),
	study_id 		INTEGER 				NOT NULL,
	study_state 	VARCHAR(1) 	NOT NULL 	CONSTRAINT check_study_state
			CHECK (study_state IN ('C', 'V', 'E', 'G', 'R', 'R', 'S', 'F', 'W')),
	creation_date 	TIMESTAMP WITH TIME ZONE	NOT NULL 	DEFAULT (current_timestamp),
	ith_update		SERIAL 		NOT NULL,
	message 		Text,
	CONSTRAINT t_rif40_study_status_pk PRIMARY KEY (study_id, study_state),
	CONSTRAINT t_rif40_studystatus_study_id_fk FOREIGN KEY (study_id)
			REFERENCES rif40.t_rif40_studies (study_id)
);

--permissions

GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE t_rif40_study_status TO rif_user;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE t_rif40_study_status TO rif_manager;

--comments
COMMENT ON TABLE t_rif40_study_status IS 'Status events for a given study.';
COMMENT ON COLUMN t_rif40_study_status.username IS 'Username';
COMMENT ON COLUMN t_rif40_study_status.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_study_status.study_state IS 'Study state: 
C: created, not verified; 
V: verified, but no other work done; 
E: extracted imported or created, but no results or maps created; 
G: Extract failure, extract, results or maps not created;
R: initial results population, create map table; 
S: R success;
F: R failure, R has caught one or more exceptions [depends on the exception handler design]
W: R warning.';
COMMENT ON COLUMN t_rif40_study_status.creation_date IS 'Creation date';
COMMENT ON COLUMN t_rif40_study_status.ith_update IS 'Update number (for ordering)';
COMMENT ON COLUMN t_rif40_study_status.message IS 'Status message; includes exception where relevant';

--indices
CREATE INDEX t_rif40_study_status_uname ON t_rif40_study_status(username);

CREATE OR REPLACE VIEW rif40_study_status AS 
 SELECT c.username,
    c.study_id,
    c.study_state,
    c.creation_date,
    c.ith_update,
    c.message
   FROM t_rif40_study_status c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username::name = "current_user"()
  WHERE c.username::name = "current_user"() OR 'RIF_MANAGER'::text = (( SELECT user_role_privs.granted_role
           FROM user_role_privs
          WHERE user_role_privs.granted_role = 'RIF_MANAGER'::text)) OR s.grantee_username IS NOT NULL AND s.grantee_username::text <> ''::text
  ORDER BY c.username;  

COMMENT ON VIEW rif40_study_status IS 'Status events for a given study.';
COMMENT ON COLUMN rif40_study_status.username IS 'Username';
COMMENT ON COLUMN rif40_study_status.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_study_status.study_state IS 'Study state: 
C: created, not verified; 
V: verified, but no other work done; 
E: extracted imported or created, but no results or maps created; 
G: Extract failure, extract, results or maps not created;
R: initial results population, create map table; 
S: R success;
F: R failure, R has caught one or more exceptions [depends on the exception handler design]
W: R warning.';
COMMENT ON COLUMN rif40_study_status.creation_date IS 'Creation date';
COMMENT ON COLUMN rif40_study_status.ith_update IS 'Update number (for ordering)';
COMMENT ON COLUMN rif40_study_status.message IS 'Status message; includes exception where relevant'; 

-- Grants
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_study_status TO rif_user;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_study_status TO rif_manager;

-- CRUD trigger
\i ../PLpgsql/rif40_trg_pkg/trg_rif40_study_status.sql

CREATE TRIGGER trg_rif40_study_status
  INSTEAD OF INSERT OR UPDATE OR DELETE
  ON rif40_study_status
  FOR EACH ROW
  EXECUTE PROCEDURE rif40_trg_pkg.trg_rif40_study_status();
COMMENT ON TRIGGER trg_rif40_study_status ON rif40_study_status IS 
	'INSTEAD OF trigger for view T_RIF40_STUDY_STATUS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted.'; 
 
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