-- ************************************************************************
-- *
-- * THIS SCRIPT MAY BE EDITED - NO NEED TO USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - PG psql code (state machine and extract SQL generation)
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

\echo Building rif40_sm_pkg (state machine and extract SQL generation) package...

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C209xx: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

--
-- Include common code
--
\i ../PLpgsql/rif40_sm_pkg/rif40_study_ddl_definer.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_execute_insert_statement.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_verify_state_change.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_create_extract.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_compute_results.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_insert_extract.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_create_insert_statement.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_reset_study.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_rename_map_and_extract_tables.sql

--
-- Include functions
--
\i ../PLpgsql/rif40_sm_pkg/rif40_run_study.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_create_disease_mapping_example.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_delete_study.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_clone_study.sql

--
-- Housekeeping functions
--
\i ../PLpgsql/rif40_sm_pkg/cleanup_orphaned_extract_and_map_tables.sql

\echo Built rif40_sm_pkg (state machine and extract SQL generation) package.
--
-- Eof

