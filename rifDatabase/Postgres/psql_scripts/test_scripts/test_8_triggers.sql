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
	PERFORM rif40_sql_pkg._rif40_sql_test_log_setup(debug_level);
		
--
-- Clear up old test cases
--
	FOR c1th_rec IN c1th LOOP
		PERFORM rif40_sm_pkg.rif40_delete_study(c1th_rec.study_id);
	END LOOP;
END;
$$;	

--
-- You must commit here or you will get a lock in the dblink() sub transaction!
--
END;

--
-- Start new transaction
--
BEGIN;
\echo 'test_8_triggers.sql: Create SAVEPOINT test_8_triggers';
SAVEPOINT test_8_triggers;

DO LANGUAGE plpgsql $$
DECLARE
	c3th CURSOR FOR 
		SELECT COUNT(study_id) AS total_studies
		  FROM rif40_studies 
		 WHERE study_name LIKE 'TRIGGER TEST%';	
	c5th CURSOR FOR
		SELECT *
		  FROM rif40_test_runs
		WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;		 
--
	c3th_rec RECORD;
	c5th_rec RECORD;	
--
	stp TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;	
--
	f_errors INTEGER:=0;
	f_tests_run INTEGER:=0;
--
	error_message VARCHAR;
--
	debug_level		INTEGER:=1;	
--
	v_sqlstate 	VARCHAR;
	v_context	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
	INSERT INTO rif40_test_runs(test_run_title, time_taken, 
		tests_run, number_passed, number_failed, number_test_cases_registered, number_messages_registered)
	VALUES ('test_8_triggers.sql', 0, 0, 0, 0, 0, 0);
	
--
-- Create dblink substranction to isolate testing from test run
--
	PERFORM rif40_sql_pkg.rif40_sql_test_dblink_connect('test_8_triggers.sql', debug_level);
	
--
	IF NOT (rif40_sql_pkg.rif40_sql_test(
		'test_8_triggers.sql',
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
		'T8--09: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][]
		/* Use defaults */)) THEN
        f_errors:=f_errors+1;
		RAISE WARNING 'T8--03: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 TEST FAILED.';		
	ELSE
		RAISE INFO 'T8--04: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 TEST PASSED.';		
    END IF;	
	f_tests_run:=f_tests_run+1;
	
--
-- DELIBERATE FAIL: level 3: 01.015.016900 removed
--
	IF (rif40_sql_pkg.rif40_sql_test(
		'test_8_triggers.sql',
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016200'') ORDER BY level4',
		'T8--11: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 [DELIBERATE FAIL TEST]',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][],
		NULL  /* Expected SQLCODE */, 
		FALSE /* Do not RAISE EXCEPTION on failure */)) THEN
        f_errors:=f_errors+1;
		RAISE INFO 'T8--05: test_8_triggers.sql: DELIBERATE FAIL TEST FAILED.';		
	ELSE
		RAISE INFO 'T8--06: test_8_triggers.sql: DELIBERATE FAIL TEST PASSED.';
    END IF;	
	f_tests_run:=f_tests_run+1;

--
-- rif40_log_pkg.rif40_error() test (and the test harness handling of RIF error codes)
--
	IF NOT (rif40_sql_pkg.rif40_sql_test(
		'test_8_triggers.sql',
		'SELECT rif40_log_pkg.rif40_error(-90125, ''rif40_error'', ''Dummy error: %'', ''rif40_error test''::VARCHAR) AS x',
		'T8--13: test_8_triggers.sql: rif40_log_pkg.rif40_error() test',
		NULL::Text[][] 	/* No results for SELECT */,		
		'-90125'	 	/* Expected SQLCODE */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */)) THEN
        f_errors:=f_errors+1;
		RAISE INFO 'T8--07: test_8_triggers.sql: rif40_log_pkg.rif40_error() dummy error TEST FAILED.';		
	ELSE
		RAISE INFO 'T8--08: test_8_triggers.sql: rif40_log_pkg.rif40_error() dummy error TEST PASSED.';
    END IF;	
	f_tests_run:=f_tests_run+1;	
	
--
-- T_RIF40_STUDIES
--
/*	sahsuland_dev=> \dS t_rif40_studies
                                                       Table "rif40.t_rif40_studies"
          Column          |            Type             |                                     Modifiers
--------------------------+-----------------------------+-----------------------------------------------------------------------------------
 study_id                 | integer                     | not null default (nextval('rif40_study_id_seq'::regclass))::integer
 username                 | character varying(90)       | default "current_user"()
 geography                | character varying(30)       | not null
 project                  | character varying(30)       | not null
 study_name               | character varying(200)      | not null
 summary                  | character varying(200)      |
 description              | character varying(2000)     |
 other_notes              | character varying(2000)     |
 extract_table            | character varying(30)       | not null
 map_table                | character varying(30)       | not null
 study_date               | timestamp without time zone | default ('now'::text)::timestamp without time zone
 study_type               | smallint                    | not null
 study_state              | character varying(1)        | default 'C'::character varying
 comparison_geolevel_name | character varying(30)       | not null
 study_geolevel_name      | character varying(30)       | not null
 denom_tab                | character varying(30)       | not null
 direct_stand_tab         | character varying(30)       |
 suppression_value        | smallint                    | not null
 extract_permitted        | smallint                    | default 0
 transfer_permitted       | smallint                    | default 0
 authorised_by            | character varying(90)       |
 authorised_on            | timestamp without time zone |
 authorised_notes         | character varying(200)      |
 audsid                   | character varying(90)       | default sys_context('USERENV'::character varying, 'SESSIONID'::character varying)
Indexes:
    "t_rif40_studies_pk" PRIMARY KEY, btree (study_id)
    "t_rif40_extract_table_uk" UNIQUE, btree (extract_table)
    "t_rif40_map_table_uk" UNIQUE, btree (map_table)
Check constraints:
    "t_rif40_stud_extract_perm_ck" CHECK (extract_permitted = ANY (ARRAY[0, 1]))
    "t_rif40_stud_transfer_perm_ck" CHECK (transfer_permitted = ANY (ARRAY[0, 1]))
    "t_rif40_studies_study_state_ck" CHECK (study_state::text = ANY (ARRAY['C'::character varying, 'V'::character varying, 'E'::character varying, 'R'::character varying, 'U'::char
acter varying]::text[]))
    "t_rif40_studies_study_type_ck" CHECK (study_type = ANY (ARRAY[1, 11, 12, 13, 14, 15]))
Foreign-key constraints:
    "rif40_studies_project_fk" FOREIGN KEY (project) REFERENCES t_rif40_projects(project)
    "t_rif40_std_comp_geolevel_fk" FOREIGN KEY (geography, comparison_geolevel_name) REFERENCES t_rif40_geolevels(geography, geolevel_name)
    "t_rif40_std_study_geolevel_fk" FOREIGN KEY (geography, study_geolevel_name) REFERENCES t_rif40_geolevels(geography, geolevel_name)
    "t_rif40_stud_denom_tab_fk" FOREIGN KEY (denom_tab) REFERENCES rif40_tables(table_name)
    "t_rif40_stud_direct_stand_fk" FOREIGN KEY (direct_stand_tab) REFERENCES rif40_tables(table_name)
    "t_rif40_studies_geography_fk" FOREIGN KEY (geography) REFERENCES rif40_geographies(geography)
 */
--
-- NULL tests
--
	IF NOT (rif40_sql_pkg.rif40_sql_test(
		'test_8_triggers.sql',	
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #1'', ''EXTRACT_TRIGGER_TEST_1'', ''MAP_TRIGGER_TEST_1'', 1 /* Diease mapping */, ''LEVEL1'', ''LEVEL4'', NULL /* FAIL HERE */, 0)',
		'T8--15: test_8_triggers.sql: TRIGGER TEST #1: rif40_studies.denom_tab IS NULL',
		NULL::Text[][] 	/* No results for trigger */,
		'-20211' 		/* Expected SQLCODE (P0001 - PGpsql raise_exception (from rif40_error) with detail: rif40_error(() code -20211 */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */)) THEN
		f_errors:=f_errors+1;
		RAISE INFO 'T8--09: test_8_triggers.sql: TRIGGER TEST #1: rif40_studies.denom_tab IS NULL TEST FAILED.';		
	ELSE
		RAISE INFO 'T8--10: test_8_triggers.sql: TRIGGER TEST #1: rif40_studies.denom_tab IS NULL TEST PASSED.';		
    END IF;	
	f_tests_run:=f_tests_run+1;
	
	IF NOT (rif40_sql_pkg.rif40_sql_test(
		'test_8_triggers.sql',	
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #2'', ''EXTRACT_TRIGGER_TEST_2'', ''MAP_TRIGGER_TEST_2'', 1 /* Diease mapping */, ''LEVEL1'', ''LEVEL4'', ''SAHSULAND_POP'', NULL /* FAIL HERE */)
RETURNING 1::Text AS test_value',
		'T8--17: test_8_triggers.sql: TRIGGER TEST #2: rif40_studies.suppression_value IS NULL',
		'{1}'::Text[][] /* Results for trigger - DEFAULTED value will not work - table is mutating and row does not exist at the point of INSERT */,
		NULL 			/* Expected SQLCODE */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */)) THEN
		f_errors:=f_errors+1;
		RAISE INFO 'T8--11: test_8_triggers.sql: TRIGGER TEST #2: rif40_studies.suppression_value IS NULL FAILED.';		
	ELSE	
		PERFORM rif40_sql_pkg.rif40_ddl('DELETE FROM rif40_studies WHERE study_name = ''TRIGGER TEST #2''');
		RAISE INFO 'T8--12: test_8_triggers.sql: TRIGGER TEST #2: rif40_studies.suppression_value IS NULL PASSED.';
    END IF;		
	f_tests_run:=f_tests_run+1;
	
--
-- Check no TEST TIRGGER studies actually created 
--
	OPEN c3th;
	FETCH c3th INTO c3th_rec;
	CLOSE c3th;
	IF c3th_rec.total_studies > 0 THEN
		PERFORM rif40_sql_pkg.rif40_method4('SELECT study_id, study_name FROM rif40_studies  WHERE study_name LIKE ''TRIGGER TEST%''', 
			'Check no TEST TIRGGER studies actually created');
		RAISE EXCEPTION 'T8--13: test_8_triggers.sql: % TEST TIRGGER studies have been created', c3th_rec.total_studies;
	ELSE
		RAISE NOTICE 'T8--14: test_8_triggers.sql: No TEST TIRGGER studies actually created.';		
	END IF;

	IF NOT (rif40_sql_pkg.rif40_sql_test(
			 'test_8_triggers.sql',
			 'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
			 'T8--21: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
'{{01,01.015,01.015.016200,01.015.016200.2}                                                                                                            
,{01,01.015,01.015.016200,01.015.016200.3}                                                                                                             
,{01,01.015,01.015.016200,01.015.016200.4}                                                                                                             
,{01,01.015,01.015.016900,01.015.016900.1}                                                                                                             
,{01,01.015,01.015.016900,01.015.016900.2}                                                                                                             
,{01,01.015,01.015.016900,01.015.016900.3}                                                                                                             
}'::Text[][]
			 )) THEN
			 f_errors:=f_errors+1;
		RAISE INFO 'T8--15: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 FAILED.';			 
	ELSE		 
		RAISE INFO 'T8--16: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 PASSED.';
	END IF;	
	f_tests_run:=f_tests_run+1;
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	UPDATE rif40_test_runs
	   SET tests_run = f_tests_run,
	       time_taken    = EXTRACT(EPOCH FROM took)::NUMERIC
	 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
--
-- Check tests_run = number_passed + number_failed
--
	OPEN c5th;
	FETCH c5th INTO c5th_rec;
	CLOSE c5th;
	IF c5th_rec.tests_run != (c5th_rec.number_passed + c5th_rec.number_failed) THEN
		RAISE EXCEPTION 'T8--17: test_8_triggers.sql: Test harness error: tests_run (%) != number_passed (%) + number_failed (%)', 
			c5th_rec.tests_run, c5th_rec.number_passed, c5th_rec.number_failed;
	ELSE
		RAISE INFO 'T8--18: test_8_triggers.sql: Test harness error: tests_run (%) = number_passed (%) + number_failed (%)', 
			c5th_rec.tests_run, c5th_rec.number_passed, c5th_rec.number_failed;				
	END IF;
--
	PERFORM rif40_sql_pkg.rif40_sql_test_dblink_disconnect('test_8_triggers.sql');
	
--	
	IF f_errors = 0 THEN
		RAISE NOTICE 'T8--19: test_8_triggers.sql: No test harness errors.';		
	ELSE
		RAISE WARNING 'T8--20: test_8_triggers.sql: Test harness errors: %', f_errors;
	END IF;
EXCEPTION
	WHEN others THEN
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
		GET STACKED DIAGNOSTICS v_context = PG_EXCEPTION_CONTEXT;
		error_message:='T8--21: test_8_triggers.sql: Test harness caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||
			'Detail: '||v_detail::VARCHAR||E'\n'||
			'Context: '||v_context::VARCHAR||E'\n'||
			'SQLSTATE: '||v_sqlstate::VARCHAR;
		BEGIN
			PERFORM rif40_sql_pkg.rif40_sql_test_dblink_disconnect('test_8_triggers.sql');
		EXCEPTION
			WHEN others THEN		
				RAISE WARNING '8--22: rif40_sql_pkg.rif40_sql_test_dblink_disconnect raised: % [IGNORED]', SQLERRM;
		END;
		IF v_sqlstate = 'P0001' AND v_detail = '-71153' THEN
			RAISE EXCEPTION 'T8--23: test_8_triggers.sql: 1: %', error_message;					
		ELSE
			RAISE EXCEPTION 'T8--24: test_8_triggers.sql: 1: %', error_message;
		END IF;
END;
$$;
 
--
-- Test code generator
--
SELECT rif40_sql_pkg._rif40_test_sql_template(
	'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
	'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200') AS template;
	
SELECT * FROM rif40_test_runs
 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
 
--
-- Dump test harness
--
SELECT test_id, error_code_expected, pass, time_taken, raise_exception_on_failure, test_stmt, test_case_title
  FROM rif40_test_harness
 ORDER BY test_id;

--
-- Re-run trigger test harness
--

--
-- Rollback to SAVEPOINT
--
	
\echo 'test_8_triggers.sql: ROLLBACK TO SAVEPOINT test_8_triggers';
ROLLBACK TO SAVEPOINT test_8_triggers;
 
--
-- Testing stop
--
DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'Stop processing';
END;
$$;

--
-- End single transaction
--
END;

\echo Test 8: Trigger test harness (added in alter_8) OK    

--
-- Eof
--
-- Eof
