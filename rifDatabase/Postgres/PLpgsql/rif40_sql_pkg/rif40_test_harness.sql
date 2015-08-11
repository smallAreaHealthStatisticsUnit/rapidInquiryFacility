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
-- rif40_test_harness:									71200 to 71249
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

DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_test_harness(INTEGER); 
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_test_harness(VARCHAR, INTEGER); 
-- DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_test_harness(VARCHAR); 

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_test_harness(test_run_class VARCHAR, debug_level INTEGER DEFAULT 0)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_test_harness()
Parameters:	Test run class; usually the name of the SQL script that originally ran it,
			debug level; debug level must be 0 to 4
Returns:	OK/exception
Description:Run test harness on the specified test class
 */
DECLARE
	c1_rth CURSOR(l_test_run_class VARCHAR) FOR
		SELECT COUNT(test_id) AS total_test_id
		  FROM rif40_test_harness a
		 WHERE a.test_run_class = l_test_run_class;
	c2_rth CURSOR(l_test_run_class VARCHAR) FOR
		SELECT *
		  FROM rif40_test_harness a
		 WHERE a.test_run_class = l_test_run_class;		
	c5_rth CURSOR FOR
		SELECT *
		  FROM rif40_test_runs
		WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;			 
	c1_rth_rec RECORD;
	c2_rth_rec RECORD;
	c5_rth_rec RECORD;
--
	stp TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;		
--
	f_errors	INTEGER:=0;
	f_tests_run	INTEGER:=0;
	f_result	BOOLEAN;
BEGIN
	OPEN c1_rth(test_run_class);
	FETCH c1_rth INTO c1_rth_rec;
	CLOSE c1_rth;
	IF c1_rth_rec.total_test_id = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(71200, 'rif40_test_harness', 
			'[71200] Test harness class: %; no tests to run',
			test_run_class::VARCHAR);	
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_test_harness', 
			'[71201] Test harness class: %; % tests to run',
			test_run_class::VARCHAR, c1_rth_rec.total_test_id::VARCHAR);
	END IF;
--
-- Create dblink substranction to isolate testing from test run
--
	PERFORM rif40_sql_pkg.rif40_sql_test_dblink_connect('test_8_triggers.sql', debug_level);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_test_harness', 
			'[71202] Test harness class: %; connection took: %', 
			test_run_class::VARCHAR, took::VARCHAR);
--
-- Set up test run
--
	stp:=clock_timestamp();	
	INSERT INTO rif40_test_runs(test_run_title, time_taken, 
		tests_run, number_passed, number_failed, number_test_cases_registered, number_messages_registered)
	VALUES (test_run_class, 0, 0, 0, 0, 0, 0);	
--
-- Loop through test cases
-- 
	FOR c2_rth_rec IN c2_rth(test_run_class) LOOP
		f_tests_run:=f_tests_run+1;
		f_result:=rif40_sql_pkg.rif40_sql_test(
						c2_rth_rec.test_run_class, 
						c2_rth_rec.test_stmt, 
						c2_rth_rec.test_case_title, 
						c2_rth_rec.results,
						c2_rth_rec.results_xml,
						c2_rth_rec.error_code_expected, 
						c2_rth_rec.raise_exception_on_failure);
		IF NOT f_result THEN 	/* Test failed */
			IF NOT c2_rth_rec.expected_result THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_test_harness', 
					'[71203] TEST %/%: % FAILED AS EXPECTED.',
					f_tests_run::VARCHAR, c1_rth_rec.total_test_id::VARCHAR, c2_rth_rec.test_case_title::VARCHAR);	
			ELSE
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_test_harness', 
					'[71204] TEST %/%: % FAILED; EXPECTED TO PASS.',
					f_tests_run::VARCHAR, c1_rth_rec.total_test_id::VARCHAR, c2_rth_rec.test_case_title::VARCHAR);	
				f_errors:=f_errors+1; 				
			END IF;
		ELSE					/* Passed */
			IF NOT c2_rth_rec.expected_result THEN		
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_test_harness', 
					'[71205] TEST %/%: % FAILED; EXPECTED TO PASS.',
					f_tests_run::VARCHAR, c1_rth_rec.total_test_id::VARCHAR, c2_rth_rec.test_case_title::VARCHAR);
				f_errors:=f_errors+1; 									
			ELSE		
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_test_harness', 
					'[71206] TEST %/%: % PASSED AS EXPECTED.',
					f_tests_run::VARCHAR, c1_rth_rec.total_test_id::VARCHAR, c2_rth_rec.test_case_title::VARCHAR);	
			END IF;
		END IF;		
	END LOOP; 

--
-- Check tests_run = number_passed + number_failed
-- 
	OPEN c5_rth;
	FETCH c5_rth INTO c5_rth_rec;
	CLOSE c5_rth;
	IF c5_rth_rec.tests_run != (c5_rth_rec.number_passed + c5_rth_rec.number_failed) THEN
		PERFORM rif40_log_pkg.rif40_error(71200, 'rif40_test_harness', 
			'[71207] Test harness class: %; test harness error: tests_run (%) != number_passed (%) + number_failed (%)', 
			test_run_class::VARCHAR, c5_rth_rec.tests_run::VARCHAR, 
			c5_rth_rec.number_passed::VARCHAR, c5_rth_rec.number_failed::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_test_harness', 
			'[71208] Test harness class: %; tests_run (%) = number_passed (%) + number_failed (%)', 
			test_run_class::VARCHAR, c5_rth_rec.tests_run::VARCHAR, 
			c5_rth_rec.number_passed::VARCHAR, c5_rth_rec.number_failed::VARCHAR);				
	END IF;	
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
--
-- Release subtransaction
--
	PERFORM rif40_sql_pkg.rif40_sql_test_dblink_disconnect('test_8_triggers.sql');	
--	
	IF f_errors = 0 THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_test_harness', 
			'[71209] Test harness class: %; no test harness errors; took: %.', 
			test_run_class::VARCHAR, took::VARCHAR);		
	ELSE
		PERFORM rif40_log_pkg.rif40_error(71210, 'rif40_test_harness', 
			'Test harness class: %; test harness errors: %; took: %.', 
			test_run_class::VARCHAR, f_errors::VARCHAR, took::VARCHAR);
	END IF;	
--
	RETURN 'OK';
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_test_harness(VARCHAR, INTEGER) IS 'Function: 	rif40_test_harness()
Parameters:	Test run class; usually the name of the SQL script that originally ran it,
			debug level; debug level must be 0 to 4
Returns:	OK/exception
Description:Run test harness on the specified test class';

GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_test_harness(VARCHAR, INTEGER) TO PUBLIC;

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_test_harness(debug_level INTEGER DEFAULT 0)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_test_harness()
Parameters:	Debug level; debug level must be 0 to 4
Returns:	OK/exception
Description:Run test harness on all test classes
 */
DECLARE
	c3_rth CURSOR FOR
		SELECT DISTINCT test_run_class
		  FROM rif40_test_harness a;
	c3_rth_rec RECORD;
BEGIN
--
-- Loop through test classes
-- 
	FOR c3_rth_rec IN c3_rth LOOP
		PERFORM rif40_sql_pkg.rif40_test_harness(c3_rth_rec.test_run_class, debug_level);
	END LOOP;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_test_harness(INTEGER) IS 'Function: 	rif40_test_harness()
Parameters:	Debug level; debug level must be 0 to 4
Returns:	OK/exception
Description:Run test harness';

GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_test_harness(INTEGER) TO PUBLIC;

--
-- Eof