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
	old_study_id	INTEGER:=currval('rif40_study_id_seq'::regclass);
	new_study_id	INTEGER;
--
	rif40_sm_pkg_functions 		VARCHAR[] := ARRAY['rif40_verify_state_change', 
						'rif40_run_study'];
	l_function 			VARCHAR;
BEGIN
--
-- Enabled debug on select rif40_sm_pkg functions
--
        PERFORM rif40_log_pkg.rif40_log_setup();
    	PERFORM rif40_log_pkg.rif40_send_debug_to_info(FALSE);
	FOREACH l_function IN ARRAY rif40_sm_pkg_functions LOOP
--		RAISE INFO 'Enable debug for function: %', l_function;
--		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
		PERFORM rif40_log_pkg.rif40_remove_from_debug(l_function);
	END LOOP;
--
	new_study_id:=rif40_sm_pkg.rif40_clone_study(old_study_id);
	IF NOT rif40_sm_pkg.rif40_run_study(new_study_id) THEN
		RAISE WARNING 'Study: % run failed; see trace', new_study_id::VARCHAR;
		PERFORM rif40_sm_pkg.rif40_reset_study(new_study_id);
		IF NOT rif40_sm_pkg.rif40_run_study(new_study_id) THEN
			RAISE WARNING 'Study: % run failed again; see trace', new_study_id::VARCHAR;
		END IF;	
	ELSE
		RAISE INFO 'Study: % run OK', new_study_id::VARCHAR;
	
		PERFORM rif40_sm_pkg.rif40_delete_study(new_study_id);
		RAISE INFO 'Cloned study: % as new study %, then deleted',
			old_study_id::VARCHAR,
			new_study_id::VARCHAR;
	END IF;
END;
$$;

--
-- End single transaction
--
END;
\echo Test 5: Clone delete test OK...
		
--
-- Eof