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
-- rif40_sql_test:										71150 to 71199
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

--
-- Include support functions
--
\i ../PLpgsql/rif40_sql_pkg/_rif40_reduce_dim.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_test_sql_template.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_test_harness.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_sql_test.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_sql_test_register.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_sql_test_log_setup.sql

--
-- Now obsoleted
--
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test_dblink_connect(VARCHAR, INTEGER);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test_dblink_disconnect(VARCHAR);
--\i ../PLpgsql/rif40_sql_pkg/_rif40_sql_test_dblink.sql

--
-- Test case 
--
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN);	
	
-- Old	
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	INTEGER, BOOLEAN);	
	
\q 

--
-- This function is now obsoleted
--	
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_sql_test(
	connection_name 			VARCHAR, 
	test_stmt 					VARCHAR, 
	test_case_title 			VARCHAR, 
	results 					ANYARRAY,
	results_xml					XML,
	pg_error_code_expected 		VARCHAR DEFAULT NULL, 
	raise_exception_on_failure 	BOOLEAN DEFAULT TRUE, 
	expected_result 			BOOLEAN DEFAULT TRUE)
RETURNS boolean
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_sql_test()
Parameters:	Connection name,
			SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title, 
			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, 
			raise exception on failure,
			expected result [has NO effect on the return code; for test harness] 
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

Used to check test SQL statements and triggers

Usage:

a) SELECT statements:
			
IF NOT (rif40_sql_pkg.rif40_sql_test(
	'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
	'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
	'{{01,01.015,01.015.016200,01.015.016200.2}
	,{01,01.015,01.015.016200,01.015.016200.3} 
	,{01,01.015,01.015.016200,01.015.016200.4} 
	,{01,01.015,01.015.016900,01.015.016900.1} 
	,{01,01.015,01.015.016900,01.015.016900.2} 
	,{01,01.015,01.015.016900,01.015.016900.3} 
	}'::Text[][]
	/- Use defaults -/)) THEN
	errors:=errors+1;
END IF;				

psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_method4():
Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200
---------------------------------------------------------------------
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_method4():
level1                                   | level2                                   | level3                                   | level4
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
01                                       | 01.015                                   | 01.015.016200                            | 01.015.016200.2
01                                       | 01.015                                   | 01.015.016200                            | 01.015.016200.3
01                                       | 01.015                                   | 01.015.016200                            | 01.015.016200.4
01                                       | 01.015                                   | 01.015.016900                            | 01.015.016900.1
01                                       | 01.015                                   | 01.015.016900                            | 01.015.016900.2
01                                       | 01.015                                   | 01.015.016900                            | 01.015.016900.3
(6 rows)
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_sql_test(): [71152] PASSED: no extra rows for test: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_sql_test(): [71155] PASSED: no missing rows for test: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_sql_test(): [71158] PASSED: Test case: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 no exceptions, no errors, no missing or extra data
				
i)   Original SQL statement:
	SELECT * FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200');
ii)  Add ORDER BY clause, expand * (This becomes the test case SQL)
	SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200') ORDER BY level4;			   
iii) Convert results to array form (Cast to text, string ) 
	[the function rif40_sql_pkg._rif40_test_sql_template() will automate this]

	SELECT ''''||
		   REPLACE(ARRAY_AGG(
				(ARRAY[level1::Text, level2::Text, level3::Text, level4::Text]::Text||E'\n')::Text ORDER BY level4)::Text, 
				'"'::Text, ''::Text)||'''::Text[][]' AS res 
	  FROM sahsuland_geography
	 WHERE level3 IN ('01.015.016900', '01.015.016200');

						 res
	---------------------------------------------
	 '{{01,01.015,01.015.016200,01.015.016200.2}+
	 ,{01,01.015,01.015.016200,01.015.016200.3} +
	 ,{01,01.015,01.015.016200,01.015.016200.4} +
	 ,{01,01.015,01.015.016900,01.015.016900.1} +
	 ,{01,01.015,01.015.016900,01.015.016900.2} +
	 ,{01,01.015,01.015.016900,01.015.016900.3} +
	 }'::Text[][]
	(1 row)
	
	Example call:
	
	PERFORM rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][]);

	Example expand of array to setof record
		  
		WITH a AS (
			SELECT '{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][] AS res
		), row AS (
			SELECT generate_series(1,array_upper(a.res, 1)) AS series
			  FROM a
		)
		SELECT  row.series, 
				(a.res)[row.series][1] AS level1, 
				(a.res)[row.series][2] AS level2, 
				(a.res)[row.series][3] AS level3, 
				(a.res)[row.series][4] AS level4
		  FROM row, a;
		
	WITH a AS ( /- Test data -/ 
		SELECT '{{01,01.015,01.015.016200,01.015.016200.2}
			,{01,01.015,01.015.016200,01.015.016200.3} 
			,{01,01.015,01.015.016200,01.015.016200.4} 
			,{01,01.015,01.015.016900,01.015.016900.1} 
			,{01,01.015,01.015.016900,01.015.016900.2} 
			,{01,01.015,01.015.016900,01.015.016900.3} 
			}'::Text[][] AS res
	), b AS ( /- Test SQL -/
		SELECT level1, level2, level3, level4 
		  FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200') 
		 ORDER BY level4
	), c AS ( /- Convert to 2D array via record -/
		SELECT REPLACE(
					REPLACE(
						REPLACE(
								ARRAY_AGG(b.*)::Text, 
								'"'::Text, ''::Text), 
							'('::Text, '{'::Text), 
						')'::Text, '}'::Text)::Text[][] AS res
		FROM b
	)
	SELECT rif40_sql_pkg._rif40_reduce_dim(c.res) AS missing_data
	  FROM c
	EXCEPT 
	SELECT rif40_sql_pkg._rif40_reduce_dim(a.res)
	  FROM a;

b) TRIGGERS
			
These use INSERT/UPDATE OR DELETE statements. RETURNING is supported, but it must be a single 
text value (test_value). The results array should should also be  single value. Beware that the INSERTed data from the table is not in scope, so you can return a sequence, an input value, but not trigger modified data 

	IF NOT (rif40_sql_pkg.rif40_sql_test(	
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #1'', ''EXTRACT_TRIGGER_TEST_1'', ''MAP_TRIGGER_TEST_1'', 1 /- Disease mapping -/, ''LEVEL1'', ''LEVEL4'', NULL /- FAIL HERE -/, 0)',
		'TRIGGER TEST #1: rif40_studies.denom_tab IS NULL',
		NULL::Text[][] 	/- No results for trigger -/,
		'P0001' 		/- Expected SQLCODE (P0001 - PGpsql raise_exception (from rif40_error) -/, 
		FALSE 			/- Do not RAISE EXCEPTION on failure -/)) THEN
		errors:=errors+1;
    END IF;	 

psql:test_scripts/test_8_triggers.sql:276: WARNING:  rif40_ddl(): SQL in error (P0001)> INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES ('SAHSU', 'TEST', 'TRIGGER TEST #1', 'EXTRACT_TRIGGER_TEST_1', 'MAP_TRIGGER_TEST_1', 1 /- Diease mapping -/, 'LEVEL1', 'LEVEL4', NULL /- FAIL HERE -/, 0);
psql:test_scripts/test_8_triggers.sql:276: WARNING:  71167: rif40_sql_test('TRIGGER TEST #1: rif40_studies.denom_tab IS NULL') caught:
rif40_trg_pkg.trigger_fct_t_rif40_studies_checks(): T_RIF40_STUDIES study 140 denominator:  not found in RIF40_TABLES in SQL >>>
INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES ('SAHSU', 'TEST', 'TRIGGER TEST #1', 'EXTRACT_TRIGGER_TEST_1', 'MAP_TRIGGER_TEST_1', 1 /- Diease mapping -/, 'LEVEL1', 'LEVEL4', NULL /- FAIL HERE -/, 0);
<<<
Error context and message >>>
Message:  rif40_trg_pkg.trigger_fct_t_rif40_studies_checks(): T_RIF40_STUDIES study 140 denominator:  not found in RIF40_TABLES
Hint:     Consult message text
Detail:   -20211
Context:  SQL statement "SELECT rif40_log_pkg.rif40_error(-20211, 'trigger_fct_t_rif40_studies_checks',
                        'T_RIF40_STUDIES study % denominator: % not found in RIF40_TABLES',
                        NEW.study_id::VARCHAR           /- Study id -/,
                        NEW.denom_tab::VARCHAR          /- Denominator -/)"
PL/pgSQL function rif40_trg_pkg.trigger_fct_t_rif40_studies_checks() line 460 at PERFORM
SQL statement "INSERT INTO t_rif40_studies (
                                username,
                                study_id,
                                extract_table,
                                study_name,
                                summary,
                                description,
                                other_notes,
                                study_date,
                                geography,
                                study_type,
                                study_state,
                                comparison_geolevel_name,
                                denom_tab,
                                direct_stand_tab,
                                study_geolevel_name,
                                map_table,
                                suppression_value,
                                extract_permitted,
                                transfer_permitted,
                                authorised_by,
                                authorised_on,
                                authorised_notes,
                                audsid,
                                project)
                        VALUES(
                                coalesce(NEW.username, "current_user"()),
                                coalesce(NEW.study_id, (nextval('rif40_study_id_seq'::regclass))::integer),
                                NEW.extract_table /- no default value -/,
                                NEW.study_name /- no default value -/,
                                NEW.summary /- no default value -/,
                                NEW.description /- no default value -/,
                                NEW.other_notes /- no default value -/,
                                coalesce(NEW.study_date, ('now'::text)::timestamp without time zone),
                                NEW.geography /- no default value -/,
                                NEW.study_type /- no default value -/,
                                coalesce(NEW.study_state, 'C'::character varying),
                                NEW.comparison_geolevel_name /- no default value -/,
                                NEW.denom_tab /- no default value -/,
                                NEW.direct_stand_tab /- no default value -/,
                                NEW.study_geolevel_name /- no default value -/,
                                NEW.map_table /- no default value -/,
                                NEW.suppression_value /- no default value -/,
                                coalesce(NEW.extract_permitted, 0),
                                coalesce(NEW.transfer_permitted, 0),
                                NEW.authorised_by /- no default value -/,
                                NEW.authorised_on /- no default value -/,
                                NEW.authorised_notes /- no default value -/,
                                coalesce(NEW.audsid, sys_context('USERENV'::character varying, 'SESSIONID'::character varying)),
                                NEW.project /- no default value -/)"
PL/pgSQL function rif40_trg_pkg.trgf_rif40_studies() line 8 at SQL statement
SQL statement "INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES ('SAHSU', 'TEST', 'TRIGGER TEST #1', 'EXTRACT_TRIGGER_TEST_1', 'MAP_TRIGGER_TEST_1', 1 /- Diease mapping -/, 'LEVEL1', 'LEVEL4', NULL /- FAIL HERE -/, 0)"
PL/pgSQL function rif40_ddl(character varying) line 51 at EXECUTE statement
SQL statement "SELECT rif40_sql_pkg.rif40_ddl(test_stmt)"
PL/pgSQL function rif40_sql_test(character varying,character varying,anyarray,character varying,boolean) line 253 at PERFORM
PL/pgSQL function inline_code_block line 157 at IF
SQLSTATE: P0001
<<< End of trace.

psql:test_scripts/test_8_triggers.sql:276: WARNING:  rif40_sql_test(): [71169] Test case: TRIGGER TEST #1: rif40_studies.denom_tab IS NULL PASSED, caught expecting SQLSTATE P0001;
Message:  rif40_trg_pkg.trigger_fct_t_rif40_studies_checks(): T_RIF40_STUDIES study 140 denominator:  not found in RIF40_TABLES
Detail:   -20211

 */
DECLARE			  
	d1st CURSOR(l_connection_name VARCHAR, l_sql_stmt VARCHAR) FOR
		SELECT a.rcode
 		  FROM dblink(l_connection_name,
				      l_sql_stmt) AS a(rcode INTEGER);
	d2st CURSOR(l_connection_name VARCHAR) FOR
		WITH a AS (
			SELECT UNNEST(dblink_get_connections()) AS connection_name
		)
		SELECT a.connection_name
		  FROM a
		 WHERE a.connection_name = l_connection_name;
--
	c3_rth CURSOR(l_test_case_title VARCHAR) FOR
		SELECT a.test_case_title, COUNT(test_id) AS total_test_id
		  FROM rif40_test_harness a
		 WHERE a.test_case_title = l_test_case_title
		   AND a.parent_test_id  IS NOT NULL
		 GROUP BY a.test_case_title;	
	c3_rth_rec RECORD;	
	c4_rth CURSOR(l_test_case_title VARCHAR) FOR
		SELECT *
		  FROM rif40_test_harness a
		 WHERE a.test_case_title = l_test_case_title
		   AND a.parent_test_id  IS NOT NULL
		 ORDER BY a.parent_test_id;	
--		 
	d2st_rec RECORD;					  
	d1st_rec	RECORD;
	c4_rth_rec RECORD;	
--		
	rcode 		BOOLEAN;
	f_test_id 	INTEGER;
	sql_stmt	VARCHAR;
	dblink_status	VARCHAR;
	op			VARCHAR;
--
	f_errors	INTEGER:=0;	
	f_tests_run	INTEGER:=0;	
--
	stp TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
--
	v_message_text		VARCHAR;
	v_pg_exception_hint	VARCHAR;
	v_column_name		VARCHAR;
	v_constraint_name	VARCHAR;
	v_pg_datatype_name	VARCHAR;
	v_table_name		VARCHAR;
	v_schema_name		VARCHAR;
--
	error_message VARCHAR;
	v_sqlstate 	VARCHAR;
	v_context	VARCHAR;	
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';		
BEGIN
--
-- Check connection does exist
--
	OPEN d2st(connection_name);
	FETCH d2st INTO d2st_rec;
	CLOSE d2st;
	IF d2st_rec.connection_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71162, 'rif40_sql_test', 
			'dblink() subtransaction: % does not exist',
			connection_name::VARCHAR);		
	END IF;
	
--
-- Auto register test case
--
	f_test_id:=rif40_sql_pkg._rif40_sql_test_register(test_stmt, connection_name, test_case_title, results, results_xml,
		pg_error_code_expected, raise_exception_on_failure, expected_result);
			
--
-- Do test; reversing effects
--	
	sql_stmt:='SELECT rif40_sql_pkg._rif40_sql_test('||E'\n'||
						coalesce(quote_literal(test_stmt), 'NULL')||'::VARCHAR /* test_stmt */,'||E'\n'||
						coalesce(quote_literal(test_case_title), 'NULL')||'::VARCHAR /* test_case_title */,'||E'\n'||
						coalesce(quote_literal(results), 'NULL')||'::VARCHAR[][] /* results */,'||E'\n'||
						coalesce(quote_literal(results_xml), 'NULL')||'::XML /* results_xml */,'||E'\n'||
						coalesce(quote_literal(pg_error_code_expected), 'NULL')||'::VARCHAR /* pg_error_code_expected */,'||E'\n'||
						coalesce(quote_literal(raise_exception_on_failure), 'NULL')||'::BOOLEAN /* raise_exception_on_failure */,'||E'\n'||
						coalesce(quote_literal(f_test_id), 'NULL')||'::INTEGER /* test_id */)::INTEGER AS rcode';
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71163] SQL[SELECT]> %;', 
			sql_stmt::VARCHAR);						
	BEGIN	
		op='dblink (BEGIN)';		
		PERFORM dblink(connection_name, 'BEGIN;');				
--		
		op='dblink open(SQL)';
		OPEN d1st(connection_name, sql_stmt);
		op='dblink fetch(SQL)';		
		FETCH d1st INTO d1st_rec;
		op='dblink close(SQL)';		
		CLOSE d1st;
--
		rcode:=d1st_rec.rcode;
--
-- Process return code
--
		etp:=clock_timestamp();
		took:=age(etp, stp);
		UPDATE rif40_test_harness
		   SET pass        = rcode,
			   test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer,
			   test_date   = statement_timestamp(),
			   time_taken  = EXTRACT(EPOCH FROM took)::NUMERIC
		 WHERE test_id = f_test_id;
		IF rcode THEN
			UPDATE rif40_test_runs
			   SET number_passed = number_passed + 1,
				   tests_run     = tests_run + 1
			 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
		ELSE
			UPDATE rif40_test_runs
			   SET number_failed = number_failed + 1,
				   tests_run     = tests_run + 1
			 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
		END IF;			
--
-- Do dependent tests
--
		OPEN c3_rth(test_case_title);
		FETCH c3_rth INTO c3_rth_rec;
		CLOSE c3_rth;
		IF c3_rth_rec.total_test_id = 0 THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71207] Test % no dependent tests to run',
				c3_rth_rec.test_case_title::VARCHAR);
		ELSIF NOT rcode THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71208a] Test % % dependent tests to run; halting because of error in master test',
				c3_rth_rec.test_case_title::VARCHAR, c3_rth_rec.total_test_id::VARCHAR);		
			RETURN rcode;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71208] Test % % dependent tests to run',
				c3_rth_rec.test_case_title::VARCHAR, c3_rth_rec.total_test_id::VARCHAR);	
--	
-- Causes:
--		
-- 2: notice: _rif40_sql_test(): [71187] Test case: SAHSULAND test 4 study_id 1 example FAILED, no error expected; got: 42P03;
-- Message:  cursor "c4_rth" already in use
-- Detail:
--		
-- Solution - move code to Node.js trest harness - also can then update
--
			FOR c4_rth_rec IN c4_rth(c3_rth_rec.test_case_title) LOOP
				f_tests_run:=f_tests_run+1;	
--
-- 	test_stmt 					VARCHAR, 
--	test_case_title 			VARCHAR, 
--	results 					ANYARRAY, 
--	results_xml					XML,
--	pg_error_code_expected 		VARCHAR, 
--	raise_exception_on_failure 	BOOLEAN, 
--	f_test_id 					INTEGER
--
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
					'[71208a] Dependent %/% test: %',
					f_tests_run::VARCHAR, 
					c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);
				sql_stmt:='SELECT rif40_sql_pkg._rif40_sql_test('||E'\n'||
									coalesce(quote_literal(c4_rth_rec.test_stmt), 'NULL')||'::VARCHAR /* test_stmt */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.test_case_title), 'NULL')||'::VARCHAR /* test_case_title */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.results), 'NULL')||'::VARCHAR[][] /* results */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.results_xml), 'NULL')||'::XML /* results_xml */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.pg_error_code_expected), 'NULL')||'::VARCHAR /* pg_error_code_expected */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.raise_exception_on_failure), 'NULL')||'::BOOLEAN /* raise_exception_on_failure */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.test_id), 'NULL')||'::INTEGER /* test_id */)::INTEGER AS rcode';
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71163a] SQL[SELECT] %/%> %;',
						f_tests_run::VARCHAR, 
						c3_rth_rec.total_test_id::VARCHAR, 
						sql_stmt::VARCHAR);	
--		
				op='dblink open(SQL) ['||f_tests_run::VARCHAR||'/'||c3_rth_rec.total_test_id::VARCHAR||']';
				OPEN d1st(connection_name, sql_stmt);
				op='dblink fetch(SQL) ['||f_tests_run::VARCHAR||'/'||c3_rth_rec.total_test_id::VARCHAR||']';		
				FETCH d1st INTO d1st_rec;
				op='dblink close(SQL) ['||f_tests_run::VARCHAR||'/'||c3_rth_rec.total_test_id::VARCHAR||']';		
				CLOSE d1st;
--
-- Process return code
--
				etp:=clock_timestamp();
				took:=age(etp, stp);
				UPDATE rif40_test_harness
				   SET pass        = d1st_rec.rcode,
					   test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer,
					   test_date   = statement_timestamp(),
					   time_taken  = EXTRACT(EPOCH FROM took)::NUMERIC
				 WHERE test_id = c4_rth_rec.test_id;
				IF d1st_rec.rcode THEN
					UPDATE rif40_test_runs
					   SET number_passed = number_passed + 1,
						   tests_run     = tests_run + 1
					 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
				ELSE
					UPDATE rif40_test_runs
					   SET number_failed = number_failed + 1,
						   tests_run     = tests_run + 1
					 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
				END IF;							
				IF NOT d1st_rec.rcode THEN 	/* Test failed */
					IF NOT c4_rth_rec.expected_result THEN
						PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
							'[71209] TEST %/%: % FAILED AS EXPECTED.',
							f_tests_run::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);	
					ELSE
						PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
							'[71210] TEST %/%: % FAILED; EXPECTED TO PASS.',
							f_tests_run::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);	
						f_errors:=f_errors+1; 				
					END IF;
				ELSE					/* Passed */
					IF NOT c4_rth_rec.expected_result THEN		
						PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
							'[71211] TEST %/%: % FAILED; EXPECTED TO PASS.',
							f_tests_run::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);
						f_errors:=f_errors+1; 									
					ELSE		
						PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
							'[71212] TEST %/%: % PASSED AS EXPECTED.',
							f_tests_run::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);	
					END IF;
				END IF;									
			END LOOP;
			IF f_errors > 0 THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71212a] Test % % dependent tests ran, % failed',
					c3_rth_rec.test_case_title::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, f_errors::VARCHAR);	
					rcode:=FALSE;			
			ELSE
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 		
					'[71212b] Test % % dependent tests ran OK',
					c3_rth_rec.test_case_title::VARCHAR, c3_rth_rec.total_test_id::VARCHAR);		
			END IF;
		END IF;			
--		
		op='dblink (ROLLBACK)';		
		PERFORM dblink(connection_name, 'ROLLBACK;');			
	EXCEPTION	
		WHEN others THEN
-- 
-- Not supported until 9.2
--
			GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
			GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
			GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
			GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
			GET STACKED DIAGNOSTICS v_pg_exception_hint = PG_EXCEPTION_HINT;
			
			GET STACKED DIAGNOSTICS v_column_name       = COLUMN_NAME;
			GET STACKED DIAGNOSTICS v_constraint_name   = CONSTRAINT_NAME;
			GET STACKED DIAGNOSTICS v_pg_datatype_name  = PG_DATATYPE_NAME;
			GET STACKED DIAGNOSTICS v_table_name        = TABLE_NAME;
			GET STACKED DIAGNOSTICS v_schema_name       = SCHEMA_NAME;
			
			error_message:='rif40_sql_test('''||coalesce(test_case_title::VARCHAR, '')||''') caught: '||E'\n'||SQLERRM::VARCHAR||
				' in SQL >>>'||E'\n'||test_stmt||';'||E'\n'||
				'<<<'||E'\n'||
				' in '||op||' SQL >>>'||E'\n'||COALESCE(sql_stmt, 'NULL')||';'||E'\n'||
				'<<<'||E'\n'||				
				'Error context and message >>>'||E'\n'||			
				'Message:  '||v_message_text::VARCHAR||E'\n'||
				'Hint:     '||v_pg_exception_hint::VARCHAR||E'\n'||
				'Detail:   '||v_detail::VARCHAR||E'\n'||
				'Context:  '||v_context::VARCHAR||E'\n'||
				'SQLSTATE: '||v_sqlstate::VARCHAR||E'\n'||'<<< End of trace.'||E'\n';
--
			PERFORM rif40_log_pkg.rif40_error(-71164, 'rif40_sql_test', 
				'Test case: % FAILED, error in dblink_open(); link: %: %', 
				test_case_title::VARCHAR,
				connection_name::VARCHAR,
				error_message::VARCHAR);
			RAISE;
	END;				

	RETURN rcode;
END;
$func$ LANGUAGE plpgsql;
			
COMMENT ON FUNCTION rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN) IS 'Function: 	rif40_sql_test()
Parameters:	Connection name.
			SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title, 
			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, 
			raise exception on failure,
			expected result [has NO effect on the return code; for test harness]   
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

			Used to check test SQL statements and triggers';
--
-- So can be used to test non rif user access to functions 
--
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML, VARCHAR, BOOLEAN, BOOLEAN) TO PUBLIC;

--
-- Eof
	
