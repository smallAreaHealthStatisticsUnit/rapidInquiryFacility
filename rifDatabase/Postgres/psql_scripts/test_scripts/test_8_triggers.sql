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
-- Rapid Enquiry Facility (RIF) - Test 8: Trigger test harness (added in alter_7)
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

--
-- Call commomn setup to run as testuser (passed parameter)
--
\i ../psql_scripts/test_scripts/common_setup.sql

\echo Test 8: Trigger test harness (added in alter_8)...

\set ndebug_level '''XXXX':debug_level''''
SET rif40.debug_level TO :ndebug_level;
--
-- Start transaction
--
BEGIN;

\dS t_rif40_studies

--
-- Test test harness!
--	
DO LANGUAGE plpgsql $$
DECLARE
	c1th CURSOR FOR 
		SELECT *
		  FROM rif40_studies 
		 WHERE study_name LIKE 'TRIGGER TEST%';
	c2th CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c1th_rec RECORD;
	c2th_rec RECORD;
--
	debug_level		INTEGER:=1;	
BEGIN	
	OPEN c2th;
	FETCH c2th INTO c2th_rec;
	CLOSE c2th;
--
-- Test parameter
--
	IF c2th_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'T1-01: test_8_triggers.sql: No -v debug_level=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c2th_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'T8--02: test_8_triggers.sql: debug level parameter="%"', debug_level::Text;
	END IF;
	
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
	RAISE INFO 'T8--03: test_8_triggers.sql: Setup logging and debug';
	PERFORM rif40_sql_pkg._rif40_sql_test_log_setup(debug_level);
		
--
-- Clean up old test cases
--
	RAISE INFO 'T8--04: test_8_triggers.sql: Clean up old test cases';
	FOR c1th_rec IN c1th LOOP
		PERFORM rif40_sm_pkg.rif40_delete_study(c1th_rec.study_id);
	END LOOP;
END;
$$;	

DO LANGUAGE plpgsql $$
DECLARE
	c2th CURSOR FOR 
		SELECT array_agg(level3) AS level3_array FROM sahsuland_level3;	 
--
	c2th_rec RECORD;	
--
	condition_array				VARCHAR[4][2]:='{{"SAHSULAND_ICD", "C34", NULL, NULL}, {"SAHSULAND_ICD", "162", "1629", NULL}}';	
										 /* investigation ICD conditions 2 dimensional array (i.e. matrix); 4 columnsxN rows:
											outcome_group_name, min_condition, max_condition, predefined_group_name */
	investigation_desc_array	VARCHAR[]:=array['Lung cancer'];
	covariate_array				VARCHAR[]:=array['SES'];			
--
	error_message VARCHAR;
--
	debug_level		INTEGER:=1;	
--
	v_sqlstate 	VARCHAR;
	v_context	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN

--
-- Create the standard test study to populate standard study 1 test and dependencies
--
--    PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_create_disease_mapping_example:DEBUG1');
--	PERFORM rif40_log_pkg.rif40_add_to_debug('_rif40_create_disease_mapping_example:DEBUG1');
--	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_ddl:DEBUG1');	
	RAISE INFO 'T8--05: test_8_triggers.sql: Create the standard test study to populate standard study 1 test and dependencies';
	OPEN c2th;
	FETCH c2th INTO c2th_rec;
	CLOSE c2th;
	PERFORM rif40_sm_pkg.rif40_create_disease_mapping_example(
		'SAHSU'::VARCHAR 			/* Geography */,
		'LEVEL1'::VARCHAR			/* Geolevel view */,
		'01'::VARCHAR				/* Geolevel area */,
		'LEVEL4'::VARCHAR			/* Geolevel map */,
		'LEVEL3'::VARCHAR			/* Geolevel select */,
		c2th_rec.level3_array 		/* Geolevel selection array */,
		'TEST'::VARCHAR 			/* project */, 
		'SAHSULAND test 4 study_id 1 example'::VARCHAR /* study name */, 
		'SAHSULAND_POP'::VARCHAR 	/* denominator table */, 
		'SAHSULAND_CANCER'::VARCHAR /* numerator table */,
 		1989						/* year_start */, 
		1996						/* year_stop */,
		condition_array 			/* investigation ICD conditions 2 dimensional array (i.e. matrix); 4 columnsxN rows:
											outcome_group_name, min_condition, max_condition, predefined_group_name */,
		investigation_desc_array 	/* investigation description array */,
		covariate_array				/* covariate array */);	
		
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
--
-- Tests
--
	RAISE INFO 'T8--06: test_8_triggers.sql: Test harness tests';
	PERFORM rif40_sql_pkg._rif40_sql_test_register(
                 'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
				 'test_8_triggers.sql',	
                 'T8--07: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
 '{{01,01.015,01.015.016200,01.015.016200.2}
 ,{01,01.015,01.015.016200,01.015.016200.3}
 ,{01,01.015,01.015.016200,01.015.016200.4}
 ,{01,01.015,01.015.016900,01.015.016900.1}
 ,{01,01.015,01.015.016900,01.015.016900.2}
 ,{01,01.015,01.015.016900,01.015.016900.3}
 }'::Text[][],
 '<row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.2</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.3</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.4</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.1</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.2</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.3</level4>
 </row>'::XML);
	
--
-- DELIBERATE FAIL: level 3: 01.015.016900 removed
--
	PERFORM rif40_sql_pkg._rif40_sql_test_register(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016200'') ORDER BY level4',
		'test_8_triggers.sql',
		'T8--08: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 [DELIBERATE FAIL TEST]',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][],
 '<row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.2</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.3</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.4</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.1</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.2</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.3</level4>
 </row>'::XML,
		NULL  /* Expected SQLCODE */, 
		FALSE /* Do not RAISE EXCEPTION on failure */,
		FALSE /* Is expected to fail */);

--
-- rif40_log_pkg.rif40_error() test (and the test harness handling of RIF error codes)
--
	PERFORM rif40_sql_pkg._rif40_sql_test_register(
		'SELECT rif40_log_pkg.rif40_error(-90125, ''rif40_error'', ''Dummy error: %'', ''rif40_error test''::VARCHAR) AS x',
		'test_8_triggers.sql',
		'T8--09: test_8_triggers.sql: rif40_log_pkg.rif40_error() test',
		NULL::Text[][] 	/* No results for SELECT */,	
		NULL::XML 		/* No results for SELECT */,		
		'-90125'	 	/* Expected SQLCODE */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */);	
	
--
-- NULL tests
--
	PERFORM rif40_sql_pkg._rif40_sql_test_register(
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #1'', ''EXTRACT_TRIGGER_TEST_1'', ''MAP_TRIGGER_TEST_1'', 1 /* Diease mapping */, ''LEVEL1'', ''LEVEL4'', NULL /* FAIL HERE */, 0)',	
		'test_8_triggers.sql',	
		'T8--10: test_8_triggers.sql: TRIGGER TEST #1: rif40_studies.denom_tab IS NULL',
		NULL::Text[][] 	/* No results for trigger */,	
		NULL::XML 		/* No results for trigger */,
		'-20211' 		/* Expected SQLCODE (P0001 - PGpsql raise_exception (from rif40_error) with detail: rif40_error(() code -20211 */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */);

	PERFORM rif40_sql_pkg._rif40_sql_test_register(
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #2'', ''EXTRACT_TRIGGER_TEST_2'', ''MAP_TRIGGER_TEST_2'', 1 /* Diease mapping */, ''LEVEL1'', ''LEVEL4'', ''SAHSULAND_POP'', NULL /* FAIL HERE */)
RETURNING 1::Text AS test_value',
		'test_8_triggers.sql',	
		'T8--11: test_8_triggers.sql: TRIGGER TEST #2: rif40_studies.suppression_value IS NULL',
		'{1}'::Text[][] /* Results for trigger - DEFAULTED value will not work - table is mutating and row does not exist at the point of INSERT */,
		'<row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <test_value>1</test_value>
 </row>'::XML /* Results for trigger - DEFAULTED value will not work - table is mutating and row does not exist at the point of INSERT */,
		NULL 			/* Expected SQLCODE */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */);		

--
-- Test sahsuland_geography
--
	PERFORM rif40_sql_pkg._rif40_sql_test_register(
			 'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
			 'test_8_triggers.sql',
			 'T8--12: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
'{{01,01.015,01.015.016200,01.015.016200.2}                                                                                                            
,{01,01.015,01.015.016200,01.015.016200.3}                                                                                                             
,{01,01.015,01.015.016200,01.015.016200.4}                                                                                                             
,{01,01.015,01.015.016900,01.015.016900.1}                                                                                                             
,{01,01.015,01.015.016900,01.015.016900.2}                                                                                                             
,{01,01.015,01.015.016900,01.015.016900.3}                                                                                                             
}'::Text[][],
 '<row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.2</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.3</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.4</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.1</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.2</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.3</level4>
 </row>'::XML);	
--
--
--
	PERFORM rif40_sql_pkg._rif40_sql_test_register('INSERT INTO rif40_test_harness
WITH a AS (
	SELECT test_id, parent_test_id, test_case_title
	  FROM rif40_test_harness
	 WHERE test_run_class = ''rif40_create_disease_mapping_example''
	   AND parent_test_id IS NULL
)
SELECT b.*
  FROM a, rif40_test_harness b
 WHERE a.test_id = b.parent_test_id',
 		'test_8_triggers.sql',	
		'T8--12: test_8_triggers.sql: Check that multiple inheritance of test cases is not permitted',
		NULL::Text[][] 	/* No results for SELECT */,	
		NULL::XML 		/* No results for SELECT */,		
		'23505'	 		/* Expected SQLCODE (unique_violation) */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */);

--
EXCEPTION
	WHEN others THEN
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
		GET STACKED DIAGNOSTICS v_context = PG_EXCEPTION_CONTEXT;
		error_message:='T8--13: test_8_triggers.sql: Test harness caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||
			'Detail: '||v_detail::VARCHAR||E'\n'||
			'Context: '||v_context::VARCHAR||E'\n'||
			'SQLSTATE: '||v_sqlstate::VARCHAR;
		IF v_sqlstate = 'P0001' AND v_detail = '-71153' THEN
			RAISE EXCEPTION 'T8--14: test_8_triggers.sql: 1: %', error_message;					
		ELSE
			RAISE EXCEPTION 'T8--15: test_8_triggers.sql: 1: %', error_message;
		END IF;
END;
$$;
 
--
-- Test code generator
--
DO LANGUAGE plpgsql $$
DECLARE
	c9th CURSOR FOR
		SELECT rif40_sql_pkg._rif40_test_sql_template(
			'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
			'test_8_triggers.sql',
			'T8--16: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200') AS template;
	c9th_rec 	RECORD;
--
	sql_stmt	VARCHAR:=NULL;
BEGIN
	RAISE INFO 'T8--17: test_8_triggers.sql: Test code generator';
	FOR c9th_rec IN c9th LOOP
		IF sql_stmt IS NULL THEN	
			sql_stmt:=c9th_rec.template;
		ELSE
			sql_stmt:=sql_stmt||E'\n'||c9th_rec.template;
		END IF;
	END LOOP;
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
END;
$$;
 
SELECT test_id, pg_error_code_expected, pass, expected_result, time_taken, raise_exception_on_failure, 
	   SUBSTRING(test_stmt FROM 1 FOR 30) AS test_stmt, test_case_title
  FROM rif40_test_harness
 WHERE test_run_class = 'rif40_create_disease_mapping_example'
 ORDER BY test_id;
 
SELECT test_id, pg_error_code_expected, pass, expected_result, time_taken, raise_exception_on_failure, test_stmt, test_case_title
  FROM rif40_test_harness
 WHERE test_run_class = 'test_8_triggers.sql'
 ORDER BY test_id;
 
--
-- Add triggers test cases
--
\i test_scripts/test_8_trigger_test_cases/trgf_rif40_studies.sql

--
-- End transaction so Node.js test harness can be called
--
END;

--
-- Start new transaction
--
BEGIN;
 
--
-- Re-run trigger test harness
--
DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'T8--18: test_8_triggers.sql: Run trigger test harness';
END;
$$;
 
--
-- Run the Node.js test harness; building required modules if needed
--
\! make -C ../../TestHarness/db_test_harness all
 
--
-- Dump test harness
--
DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'T8--19: test_8_triggers.sql: Dump test harness';
END;
$$;
SELECT test_id, pg_error_code_expected, pass, expected_result, time_taken, raise_exception_on_failure, test_stmt, test_case_title
  FROM rif40_test_harness
 WHERE test_run_class = 'test_8_triggers.sql'
 ORDER BY test_id;

SELECT test_id, pg_error_code_expected, pass, expected_result, time_taken, raise_exception_on_failure, test_stmt, test_case_title
  FROM rif40_test_harness
 WHERE test_run_class = 'trgf_rif40_studies'
 ORDER BY test_id;
 
SELECT test_run_class, pass, COUNT(COALESCE(pass::Text, 'Null')) AS total 
  FROM rif40_test_harness
 GROUP BY test_run_class, pass
 ORDER BY test_run_class, pass;
 
SELECT * 
  FROM rif40_test_runs;
 
--
-- Check tests and runs
--
DO LANGUAGE plpgsql $$
DECLARE
	c1th CURSOR FOR
		SELECT COUNT(COALESCE(pass, FALSE)) AS failed
		  FROM rif40_test_harness
		 WHERE NOT pass OR pass IS NULL;
	c2th CURSOR FOR
		SELECT COUNT(test_date) AS total_pass
		  FROM rif40_test_runs
		 WHERE tests_run > 0 
		   AND test_date > statement_timestamp() - (10 * interval '1 minute') /* Test harness has been run in the last 10 minutes */;
	c3th CURSOR FOR
		SELECT COUNT(test_date) AS total_zero
		  FROM rif40_test_runs
		 WHERE time_taken = 0 
		   AND tests_run = 0 
		   AND number_passed = 0 
		   AND number_failed = 0;
    c1th_rec RECORD;
	c2th_rec RECORD;
	c3th_rec RECORD;
BEGIN
	OPEN c1th;
	FETCH c1th INTO c1th_rec;
	CLOSE c1th;
	OPEN c2th;
	FETCH c2th INTO c2th_rec;
	CLOSE c2th;	
	OPEN c3th;
	FETCH c3th INTO c3th_rec;
	CLOSE c3th;	
--	
-- Check for probable db_test_harness.js error
--
	IF c2th_rec.total_pass = 0 THEN
		RAISE EXCEPTION 'T8--20: test_8_triggers.sql: Test harness has not been run sucessfully in the last 10 minutes (probable db_test_harness.js error)';
	END IF;
--		
-- Check for failed tests
--
	IF c1th_rec.failed > 0 THEN
		RAISE EXCEPTION 'T8--21: test_8_triggers.sql: % tests failed or were not run (pass is NULL)', c1th_rec.failed;
	END IF;
--
-- Check for test run fails
--		
	IF c3th_rec.total_zero > 0 THEN
		RAISE EXCEPTION 'T8--22: test_8_triggers.sql: % test runs failed (time_taken, tests_run, number_passed, number_failed are all zero)', c3th_rec.total_zero;
	END IF;	
END;
$$;

\copy (SELECT test_id, parent_test_id, test_run_class, test_stmt, test_case_title, pg_error_code_expected, mssql_error_code_expected, raise_exception_on_failure, expected_result, register_date, results, results_xml, NULL AS pass, NULL AS test_run_id, NULL AS test_date, NULL AS time_taken, pg_debug_functions FROM rif40_test_harness ORDER BY test_id) TO ../../TestHarness/rif40_test_harness.csv WITH CSV HEADER 
\copy (SELECT * FROM rif40_test_harness ORDER BY test_id) TO test_scripts/data/rif40_test_harness_:dbname.csv WITH CSV HEADER
\copy (SELECT * FROM rif40_test_runs ORDER BY test_run_id) TO test_scripts/data/rif40_test_runs_:dbname.csv WITH CSV HEADER
 
--
-- Testing stop
--
DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'T8--98: test_8_triggers.sql: Stop processing';
END;
$$;
VACUUM (FULL,VERBOSE,ANALYZE) rif40_test_harness;
VACUUM (FULL,VERBOSE,ANALYZE) rif40_test_runs;

--
-- End single transaction
--
END;

DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'T8--99: test_8_triggers.sql: Test 8: Trigger test harness (added in alter_8) OK';
END;
$$;    

--
-- Eof
