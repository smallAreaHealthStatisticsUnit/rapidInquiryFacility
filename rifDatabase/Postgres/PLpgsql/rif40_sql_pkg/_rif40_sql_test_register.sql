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
-- _rif40_sql_test_register:							71300 to 71349
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

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

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN, INTEGER, Text[]);	
-- Old	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN, INTEGER);
	
CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_sql_test_register(
	test_stmt 					VARCHAR, 
	test_run_class 				VARCHAR, 
	test_case_title 			VARCHAR, 
	results 					ANYARRAY,
	results_xml					XML,
	pg_error_code_expected 		VARCHAR DEFAULT NULL, 
	raise_exception_on_failure 	BOOLEAN DEFAULT FALSE, 
	expected_result 			BOOLEAN DEFAULT TRUE,
	parent_test_id				INTEGER DEFAULT NULL,
	pg_debug_functions			Text[] DEFAULT NULL)
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_sql_test_register()
Parameters:	SQL test (SELECT or INSERT/UPDATE/DELETE with RETURNING clause) statement, 
			Test run class; usually the name of the SQL script that originally ran it,
            test case title,
 			results 3d text array,
			results as XML,
			[negative] Postgres error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception; default: NULL, 
			raise exception on failure; default: FALSE (do not halt test harness),
			expected result; default: TRUE (pass),
			parent test id; default: NULL,
			Array of Postgres functions for test harness to enable debug on; default: NULL
Returns:	Test id, NULL if rif40_test_harness table not created yet
Description:Autoregister test case
 */
DECLARE
	c1st CURSOR FOR	
		SELECT * FROM pg_tables WHERE tablename = 'rif40_test_harness';
	c3st CURSOR (l_test_stmt 					VARCHAR, 
				 l_test_run_class				VARCHAR,
			     l_test_case_title 				VARCHAR, 
				 l_results 						Text[][], 
				 l_results_xml 					XML,
				 l_pg_error_code_expected		VARCHAR, 
				 l_raise_exception_on_failure 	BOOLEAN,
				 l_expected_result				BOOLEAN,
				 l_parent_test_id				INTEGER,
				 l_pg_debug_functions			Text[]) FOR
		INSERT INTO rif40_test_harness (
			test_stmt,
			test_run_class,
			test_case_title,
			pg_error_code_expected,
			raise_exception_on_failure,
			results,
			results_xml,
			expected_result,
			parent_test_id,
			pg_debug_functions)
		SELECT l_test_stmt, l_test_run_class, l_test_case_title, 
			   l_pg_error_code_expected, l_raise_exception_on_failure, l_results, 
			   l_results_xml, l_expected_result, l_parent_test_id, l_pg_debug_functions
		 WHERE NOT EXISTS (
			SELECT a.test_id
			  FROM rif40_test_harness a
			 WHERE a.test_case_title = l_test_case_title
		   AND COALESCE(a.parent_test_id, 0) = COALESCE(l_parent_test_id, 0))
		RETURNING *;
	c1st_rec RECORD;		
	c3st_rec RECORD;
	c5st CURSOR (l_test_case_title VARCHAR, l_parent_test_id INTEGER) FOR
		SELECT a.test_case_title, a.test_id
		  FROM rif40_test_harness a
		 WHERE a.test_case_title             = l_test_case_title
		   AND COALESCE(a.parent_test_id, 0) = COALESCE(l_parent_test_id, 0);
	c5st_rec RECORD;
	c6_st CURSOR(l_function_name VARCHAR) FOR 
		SELECT l.lanname||' function' AS object_type, 
			COALESCE(n.nspname, r.rolname)||'.'||p.proname AS object_name 					/* Functions */
		  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
			LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
		 WHERE p.prolang    = l.oid
		   AND p.prorettype = t.oid
		   AND p.proowner   IN (SELECT oid FROM pg_roles WHERE rolname IN ('postgres', 'rif40'))
		   AND p.proowner   = r.oid
		   AND p.proname    = LOWER(l_function_name);
	c6st_rec RECORD;	
--
	f_test_id 	INTEGER;	
	l_func		VARCHAR;
BEGIN
	OPEN c1st;
	FETCH c1st INTO c1st_rec;
	CLOSE c1st;
	IF c1st_rec.tablename IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test_register', 
			'[71300] No test harness table NOT FOUND: %; TEST CASE NOT INSERTED',
			test_case_title::VARCHAR);		
		RETURN NULL;
	END IF;
	IF pg_debug_functions IS NOT NULL THEN
		FOREACH l_func IN ARRAY pg_debug_functions LOOP
			OPEN c6_st(l_func);
			FETCH c6_st INTO c6st_rec;
			IF c6st_rec.object_name IS NULL THEN
				PERFORM rif40_log_pkg.rif40_error(-71301, '_rif40_sql_test_register', 
					'Test case: %; debug function: % not found',
					test_case_title::VARCHAR,
					l_func::VARCHAR);						
			END IF;
			CLOSE c6_st;
		END LOOP;
	END IF;
--
	OPEN c3st(test_stmt, test_run_class, test_case_title, 
		results, results_xml, pg_error_code_expected, raise_exception_on_failure, expected_result, parent_test_id, pg_debug_functions);
	FETCH c3st INTO c3st_rec;
	CLOSE c3st;
	OPEN c5st(test_case_title, parent_test_id);
	FETCH c5st INTO c5st_rec;
	CLOSE c5st;		
	f_test_id:=c5st_rec.test_id;	
	IF f_test_id IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71302, '_rif40_sql_test_register', 
			'Test id for test case NOT FOUND: %; TEST CASE NOT INSERTED',
			c3st_rec.test_case_title::VARCHAR);	
	END IF;
--	
	IF c3st_rec.test_case_title IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_register', '[71303] Registered test case %: %; parent: %', 
			f_test_id::VARCHAR, test_case_title::VARCHAR, parent_test_id::VARCHAR);	
		UPDATE rif40_test_runs
		   SET number_test_cases_registered = number_test_cases_registered + 1
		 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;	
	ELSE	
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_register', '[71304] Test case already registered %: %', 
			f_test_id::VARCHAR, test_case_title::VARCHAR);
	END IF;
--
	IF f_test_id IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71305, '_rif40_sql_test_register', 
			'Test id for test case NOT FOUND: %; TEST CASE NEVER INSERTED',
			c3st_rec.test_case_title::VARCHAR);	
	END IF;
--
	RETURN f_test_id;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN, INTEGER, Text[]) IS 'Function: 	_rif40_sql_test_register()
Parameters:	SQL test (SELECT or INSERT/UPDATE/DELETE with RETURNING clause) statement,
			Test run class; usually the name of the SQL script that originally ran it,
            test case title,
 			results 3d text array,
			results as XML,
			[negative] Postgres error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception; default: NULL, 
			raise exception on failure; default: FALSE (do not halt test harness),
			expected result; default: TRUE (pass),
			parent test id; default: NULL,
			Array of Postgres functions for test harness to enable debug on; default: NULL
Returns:	Test id, NULL if rif40_test_harness table not created yet
Description:Autoregister test case';

--
-- Eof