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
-- Rapid Enquiry Facility (RIF) - Test 5: clone delete test
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
-- the Free Software Foundation, either verquote_ident(l_schema)||'.'||quote_ident(l_tablesion 3 of the License, or
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

--
-- Call commomn setup to run as testuser (passed parameter)
--
\i ../psql_scripts/test_scripts/common_setup.sql

\echo Test 5: Clone delete test...

SELECT study_id
  FROM rif40_studies
 WHERE username = USER
   AND study_name = 'SAHSULAND test 4 study_id 1 example';
		   
\set ndebug_level '''XXXX':debug_level''''
SET rif40.debug_level TO :ndebug_level;
--
-- Start transaction
--
BEGIN;
--
-- Clone delete test 5 - MOVE
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR	
		SELECT MAX(study_id) AS study_id
		  FROM rif40_studies
		 WHERE username = USER
		   AND study_name = 'SAHSULAND test 4 study_id 1 example';
	c4sm CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c4sm_rec RECORD;
	c1_rec RECORD;
--
	old_study_id	INTEGER;
	new_study_id	INTEGER;
--
	rif40_sm_pkg_functions 		VARCHAR[] := ARRAY[
				'rif40_compute_results', 'rif40_startup', 'rif40_clone_study', 'rif40_reset_study', 'rif40_delete_study', 'rif40_ddl'];
	l_function 			VARCHAR;
	debug_level		INTEGER;
BEGIN
	OPEN c4sm;
	FETCH c4sm INTO c4sm_rec;
	CLOSE c4sm;
--
-- Test parameter
--
	IF c4sm_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'test_5_clone_delete_test.sql: T5--01: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'T5--02: test_5_clone_delete_test.sql: debug level parameter="%"', debug_level::Text;
	END IF;
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_5_clone_delete_test.sql: T5--03: Invslid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_sm_pkg_functions LOOP
			RAISE INFO 'T5--04: test_5_clone_delete_test.sql: Enable debug for function: %', l_function;
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
		END LOOP;
	END IF;
--
-- Fetch last test 4 study 1 study ID
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	IF NOT FOUND OR c1_rec.study_id IS NULL THEN
		CLOSE c1;
		RAISE EXCEPTION 'test_5_clone_delete_test.sql: T5--05: Unable to find last test 4 study 1 study ID';
	END IF;
	CLOSE c1;
	old_study_id:=c1_rec.study_id;
--
	new_study_id:=rif40_sm_pkg.rif40_clone_study(old_study_id);
	IF new_study_id IS NULL THEN
		RAISE EXCEPTION 'test_5_clone_delete_test.sql: T5--06: Study: % clone failed; see trace', new_study_id::VARCHAR;
	END IF;
	RAISE INFO 'test_5_clone_delete_test.sql: T5--07: old study: %, new: %', old_study_id::VARCHAR, new_study_id::VARCHAR;
--
	IF NOT rif40_sm_pkg.rif40_run_study(new_study_id, FALSE /* Debug */) THEN
		RAISE WARNING 'test_5_clone_delete_test.sql: T5--08: Study: % run failed; see trace', new_study_id::VARCHAR;
		PERFORM rif40_sm_pkg.rif40_reset_study(new_study_id);
		IF NOT rif40_sm_pkg.rif40_run_study(new_study_id, FALSE /* Debug */) THEN
			RAISE EXCEPTION 'test_5_clone_delete_test.sql: T5--09: Study: % run failed again; see trace', new_study_id::VARCHAR;
		END IF;	
	ELSE
		RAISE INFO 'test_5_clone_delete_test.sql: T5--10: Study: % run OK', new_study_id::VARCHAR;
	
		PERFORM rif40_sm_pkg.rif40_delete_study(new_study_id);
		RAISE INFO 'test_5_clone_delete_test.sql: T5--11: Cloned study: % as new study %, then deleted',
			old_study_id::VARCHAR,
			new_study_id::VARCHAR;
	END IF;
END;
$$;

SELECT study_id
  FROM rif40_studies
 WHERE username = USER
   AND study_name = 'SAHSULAND test 4 study_id 1 example';
--
-- End single transaction
--
END;
\echo Test 5: Clone delete test OK...
		
--
-- Eof