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
-- Rapid Enquiry Facility (RIF) - Test harness implementation
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
-- Error codes:
-- rif40_sql_test_log_setup.sql:						71400 to 71449
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing
\set VERBOSITY terse
--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20900: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_log_setup(INTEGER);
	
CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_sql_test_log_setup(debug_level INTEGER DEFAULT 0)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_sql_test_log_setup()
Parameters:	Debug level 
Returns:	Nothing
Description:Setup debug for test harness. Debug level must be 0 to 4
 */
DECLARE
	rif40_pkg_functions 		VARCHAR[] := ARRAY[
				'rif40_delete_study', 'rif40_ddl', '_rif40_sql_test', '_rif40_test_sql_template', '_rif40_sql_test_register',
				'rif40_test_harness', 'rif40_startup', '_rif40_sql_test_log_setup', 'rif40_remove_from_debug'];
	l_function 			VARCHAR;	
BEGIN
    PERFORM rif40_log_pkg.rif40_log_setup();
	PERFORM rif40_log_pkg.rif40_add_to_debug('_rif40_sql_test_log_setup:DEBUG1');
	IF debug_level IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_log_setup', 
			'[71400]: NULL debug_level; set to 1');
		debug_level:=1;
	ELSIF debug_level > 4 THEN
		PERFORM rif40_log_pkg.rif40_error(-71401, '_rif40_sql_test_log_setup', 'Invalid debug level [0-4]: %', debug_level::VARCHAR);
	ELSIF debug_level BETWEEN 1 AND 4 THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_log_setup', 
			'[71402]: debug_level %', debug_level::VARCHAR);
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_pkg_functions LOOP
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_log_setup', 
				'[71403]: Enable debug for function: %', 
				l_function::VARCHAR);
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
		END LOOP;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_log_setup', 
			'[71404]: debug_level %', debug_level::VARCHAR);
	END IF;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg._rif40_sql_test_log_setup(INTEGER) IS 'Function: 	_rif40_sql_test_log_setup()
Parameters:	Debug level 
Returns:	Nothing
Description:Setup debug for test harness. Debug level must be 0 to 4';
GRANT EXECUTE ON FUNCTION rif40_sql_pkg._rif40_sql_test_log_setup(INTEGER) TO PUBLIC;
	
--
-- Eof