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

--
-- Test case 
--
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	INTEGER, BOOLEAN);		
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_sql_test(test_stmt VARCHAR, test_case_title VARCHAR, results ANYARRAY,
	error_code_expected VARCHAR DEFAULT NULL, raise_exception_on_failure BOOLEAN DEFAULT TRUE)
RETURNS boolean
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_sql_test()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title, result arrays,
			[negative] error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, raise exception on failure  
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

Used to check test SQL statements and triggers

All SAVEPOINT/ROLLBACK function disabled in Postgres 9.3 (it does work on 9.4!)

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
--
	c1sqlt 				REFCURSOR;
	c1sqlt_result_row 	RECORD;
	c2sqlt 				REFCURSOR;
	c2sqlt_result_row 	RECORD;
--
	c3st CURSOR (l_test_stmt 					VARCHAR, 
			     l_test_case_title 				VARCHAR, 
				 l_results 						Text[][], 
				 l_error_code_expected			VARCHAR, 
				 l_raise_exception_on_failure 	BOOLEAN) FOR
		WITH a AS (
			SELECT a.test_case_title, a.test_id AS old_test_id
			  FROM rif40_test_harness a
			 WHERE a.test_case_title = l_test_case_title
		)
		INSERT INTO rif40_test_harness (
			test_stmt,
			test_case_title,
			error_code_expected,
			raise_exception_on_failure,
			results,
			results_xml)
		SELECT l_test_stmt, l_test_case_title, l_error_code_expected, l_raise_exception_on_failure, l_results, NULL /* l_results::XML */
		 WHERE NOT EXISTS (
			SELECT a.test_id
			  FROM rif40_test_harness a
			 WHERE a.test_case_title = l_test_case_title)
		RETURNING *;
	c3st_rec RECORD;
	c4st CURSOR FOR
	 	SELECT version() AS version, 
		       SUBSTR(version(), 12, 3)::NUMERIC as major_version, 
		       SUBSTR(version(), 16, position(', ' IN version())-16)::NUMERIC as minor_version;
	c4st_rec	RECORD;
	c5st CURSOR (l_test_case_title VARCHAR) FOR
		SELECT a.test_case_title, a.test_id
		  FROM rif40_test_harness a
		 WHERE a.test_case_title = l_test_case_title;
	c5st_rec RECORD;
--
	f_test_id 	INTEGER;
--	
	sql_frag 	VARCHAR;
	sql_stmt 	VARCHAR;
--
	extra		INTEGER:=0;
	missing		INTEGER:=0;	
--
	f_pass		BOOLEAN;
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
-- Check Postgres version. SAVEPOINT/ROLLBACK functionality requires Postgres 9.4
--
	OPEN c4st;
	FETCH c4st INTO c4st_rec;
	CLOSE c4st;
	
--
-- Auto register test case
--
	OPEN c3st(test_stmt, test_case_title, results, error_code_expected, raise_exception_on_failure);
	FETCH c3st INTO c3st_rec;
	CLOSE c3st;
	OPEN c5st(test_case_title);
	FETCH c5st INTO c5st_rec;
	CLOSE c5st;		
	f_test_id:=c5st_rec.test_id;	
	IF f_test_id IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71150, 'rif40_sql_test', 
			'Test id for test case NOT FOUND: %; TEST CASE NOT INSERTED',
			c3st_rec.test_case_title::VARCHAR);	
	END IF;
--	
	IF c3st_rec.test_case_title IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', '[71151] Registered test case %: %', 
			f_test_id::VARCHAR, test_case_title::VARCHAR);	
		UPDATE rif40_test_runs
		   SET number_test_cases_registered = number_test_cases_registered + 1
		 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;	
	ELSE	
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', '[71152] Test case already registered %: %', 
			f_test_id::VARCHAR, test_case_title::VARCHAR);
	END IF;
	
--
-- Check Postgres version. SAVEPOINT/ROLLBACK functionality requires Postgres 9.4
--
	OPEN c4st;
	FETCH c4st INTO c4st_rec;
	CLOSE c4st;
	IF c4st_rec.major_version < 9.4 THEN
--
-- These can be commented out for test_8_triggers.sql on 9.3 ONLY
-- BE CAREFUL IS YOU CHANGE THIS ERROR CODE - IT IS TESTED FOR INTO test_8_triggers.sql
--
--		PERFORM rif40_log_pkg.rif40_error(-71153, 'rif40_sql_test', 
--			'Postgres version %.% SAVEPOINT/ROLLBACK functionality; rif40_sql_test() is disabled',
--			c4st_rec.major_version::VARCHAR, c4st_rec.minor_version::VARCHAR);
	END IF;

--
-- All SAVEPOINT/ROLLBACK function disabled in Postgres 9.3 (it does work on 9.4!)
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71154] SAVEPOINT; Test case %: %', 
		f_test_id::VARCHAR, test_case_title::VARCHAR);
	IF c4st_rec.major_version >= 9.4 THEN	
		SAVEPOINT rif40_sql_test;
	END IF;
	
--
-- Do test
--
	IF UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 6)) = 'SELECT' THEN
		PERFORM rif40_sql_pkg.rif40_method4(test_stmt, test_case_title);
--
		sql_frag:='WITH a AS ( /* Test data */'||E'\n'||
'	SELECT '''||
		results::Text||
		'''::Text[][] AS res'||E'\n'||
'), b AS ( /* Test SQL */'||E'\n'||
	test_stmt||E'\n'||
'), c AS ( /* Convert to 2D array via record */'||E'\n'||
'	SELECT REPLACE('||E'\n'||
'				REPLACE('||E'\n'||
'					REPLACE('||E'\n'||
'							ARRAY_AGG(b.*)::Text, '||E'\n'||
'							''"''::Text, ''''::Text),'||E'\n'|| 
'						''(''::Text, ''{''::Text),'||E'\n'|| 
'					'')''::Text, ''}''::Text)::Text[][] AS res'||E'\n'||
'	FROM b'||E'\n'||
')';
--
-- Check for extra data
--
		sql_stmt:=sql_frag||E'\n'||
'SELECT rif40_sql_pkg._rif40_reduce_dim(c.res) AS extra_data'||E'\n'||
'  FROM c /* Test SQL data */'||E'\n'||
'EXCEPT'||E'\n'|| 
'SELECT rif40_sql_pkg._rif40_reduce_dim(a.res)'||E'\n'||
'  FROM a /* Test data */';
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71152] SQL[SELECT]> %;', 
			sql_stmt::VARCHAR);
		OPEN c1sqlt FOR EXECUTE sql_stmt;
		LOOP
			FETCH c1sqlt INTO c1sqlt_result_row;
			IF NOT FOUND THEN EXIT; END IF;
--
			extra:=extra+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71153] Test case %: % extra[%]: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, c1sqlt_result_row.extra_data::VARCHAR);		
		END LOOP;
		CLOSE c1sqlt;
		IF extra = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71154] PASSED: test case %: no extra rows for test: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71155] FAILED: test case %: % extra rows for test: %', 
				f_test_id::VARCHAR, extra::VARCHAR, test_case_title::VARCHAR);	
		END IF;
--
		sql_stmt:=sql_frag||E'\n'||
'SELECT rif40_sql_pkg._rif40_reduce_dim(a.res) AS missing_data'||E'\n'||
'  FROM a /* Test data */'||E'\n'||
'EXCEPT'||E'\n'|| 
'SELECT rif40_sql_pkg._rif40_reduce_dim(c.res)'||E'\n'||
'  FROM c /* Test SQL data */';
		OPEN c2sqlt FOR EXECUTE sql_stmt;
		LOOP
			FETCH c2sqlt INTO c2sqlt_result_row;
			IF NOT FOUND THEN EXIT; END IF;
--
			missing:=missing+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71156] Test case %: % missing[%]: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, c2sqlt_result_row.missing_data::VARCHAR);
		END LOOP;
		CLOSE c2sqlt;
		
--
-- Reverse effects of test
-- 
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71157] ROLLBACK TO SAVEPOINT; Test case %: %', 
			f_test_id::VARCHAR, test_case_title::VARCHAR);
		IF c4st_rec.major_version >= 9.4 THEN
			ROLLBACK TO SAVEPOINT rif40_sql_test;	
		END IF;
		
--
-- Check for missing
--
		IF missing = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71158] PASSED: no missing rows for test case %: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71159] FAILED: Test case %: % % missing rows for test: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, test_case_title::VARCHAR);	
		END IF;
--		
		IF error_code_expected IS NOT NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71160] FAILED: Test case %: % no exception, expected SQLSTATE: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, error_code_expected::VARCHAR);				
			f_pass:=FALSE;	
		ELSIF extra = 0 AND missing = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71161] PASSED: Test case %: % no exceptions, no errors, no missing or extra data', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);			
			f_pass:=TRUE;
		ELSIF raise_exception_on_failure THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71162] FAILED: Test case %: % % missing, % extra', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, extra::VARCHAR);		
			RAISE no_data_found;
		ELSE /* Just failed above */
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71163] FAILED: Test case %: % % missing, % extra', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, extra::VARCHAR);			
			f_pass:=FALSE;	
		END IF;
--
-- Other SQL test statements (i.e. INSERT/UPDATE/DELETE for triggers) with RETURNING clause
--
	ELSIF UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 6)) IN ('INSERT', 'UPDATE', 'DELETE') THEN
		IF results IS NOT NULL THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71162] SQL[INSERT/UPDATE/DELETE; RETURNING]> %;', 
				test_stmt::VARCHAR);	
			OPEN c1sqlt FOR EXECUTE test_stmt;
			FETCH c1sqlt INTO c1sqlt_result_row;
			CLOSE c1sqlt;
				
--
-- Reverse effects of test
-- 
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71163] ROLLBACK TO SAVEPOINT; Test case %: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);
			IF c4st_rec.major_version >= 9.4 THEN
				ROLLBACK TO SAVEPOINT rif40_sql_test;	
			END IF;
		
--
-- Check for errors (or rather the lack of them)
--		
			IF error_code_expected IS NOT NULL THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71164] FAILED: Test case %: % no exception, expected SQLSTATE: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, error_code_expected::VARCHAR);				
				f_pass:=FALSE;	
			ELSIF c1sqlt_result_row.test_value = results[1] THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
					'[71165] PASSED: Test case %: % no exceptions, no errors, return value as expected: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, results[1]::VARCHAR);			
				f_pass:=TRUE;
			ELSIF raise_exception_on_failure THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71166] FAILED: Test case %: % got: %, expected: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, c1sqlt_result_row.test_value::VARCHAR, results[1]::VARCHAR);			
				RAISE no_data_found;
			ELSE /* Value test failed */
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71167] FAILED: Test case %: % got: %, expected: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, c1sqlt_result_row.test_value::VARCHAR, results[1]::VARCHAR);			
				f_pass:=FALSE;
			END IF;
		
--
-- Other SQL test statements (i.e. INSERT/UPDATE/DELETE for triggers)
--
		ELSE
			PERFORM rif40_sql_pkg.rif40_ddl(test_stmt);
				
--
-- Reverse effects of test
-- 
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71168] ROLLBACK TO SAVEPOINT; Test case %: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);
			IF c4st_rec.major_version >= 9.4 THEN
				ROLLBACK TO SAVEPOINT rif40_sql_test;	
			END IF;
	
--
-- Check for errors (or rather the lack of them)
--			
			IF error_code_expected IS NULL THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
					'[71169] PASSED: Test case %: % no exceptions, no error expected', 
					f_test_id::VARCHAR, test_case_title::VARCHAR);		
				f_pass:=TRUE;
			ELSIF raise_exception_on_failure THEN		
				RAISE no_data_found;			
			ELSE
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71170] FAILED: Test case %: % no exceptions, expected SQLSTATE: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, error_code_expected::VARCHAR);		
				f_pass:=FALSE;		
			END IF;
		END IF;
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-71171, 'rif40_sql_test', 
			'Test case %: % FAILED, invalid statement type: % %SQL> %;', 
			f_test_id::VARCHAR, test_case_title::VARCHAR, UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 6))::VARCHAR, E'\n'::VARCHAR, test_stmt::VARCHAR);	
	END IF;
--
-- Process return code
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	UPDATE rif40_test_harness
	   SET pass        = f_pass,
	       test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer,
		   test_date   = statement_timestamp(),
		   time_taken  = EXTRACT(EPOCH FROM took)::NUMERIC
	 WHERE test_id = f_test_id;
	IF f_pass THEN
		UPDATE rif40_test_runs
		   SET number_passed = number_passed + 1
		 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
	ELSE
		UPDATE rif40_test_runs
		   SET number_failed = number_failed + 1 
		 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
	END IF;
	RETURN f_pass;
--
EXCEPTION
	WHEN no_data_found THEN	
--
-- Reverse effects of test
-- 
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71172] ROLLBACK TO SAVEPOINT: %', test_case_title::VARCHAR);
		IF c4st_rec.major_version >= 9.4 THEN
			ROLLBACK TO SAVEPOINT rif40_sql_test;	
		END IF;
		IF error_code_expected IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-71173, 'rif40_sql_test', 
				'Test case: % FAILED, % errors', 
				test_case_title::VARCHAR, (extra+missing)::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-71174, 'rif40_sql_test', 
				'Test case: % FAILED, % errors; expecting SQLSTATE: %; not thrown', 
				test_case_title::VARCHAR, (extra+missing)::VARCHAR, error_code_expected::VARCHAR);	
		END IF;
/*
RETURNED_SQLSTATE		the SQLSTATE error code of the exception
COLUMN_NAME				the name of column related to exception
CONSTRAINT_NAME			the name of constraint related to exception
PG_DATATYPE_NAME		the name of datatype related to exception
MESSAGE_TEXT			the text of the exception's primary message
TABLE_NAME				the name of table related to exception
SCHEMA_NAME				the name of schema related to exception
PG_EXCEPTION_DETAIL		the text of the exception's detail message, if any
PG_EXCEPTION_HINT		the text of the exception's hint message, if any
PG_EXCEPTION_CONTEXT	line(s) of text describing the call stack
 */			
	WHEN others THEN
--
-- Reverse effects of test
-- 
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71175] ROLLBACK TO SAVEPOINT: %', test_case_title::VARCHAR);
		IF c4st_rec.major_version >= 9.4 THEN
			ROLLBACK TO SAVEPOINT rif40_sql_test;	
		END IF;	
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
			'<<<'||E'\n'||'Error context and message >>>'||E'\n'||			
			'Message:  '||v_message_text::VARCHAR||E'\n'||
			'Hint:     '||v_pg_exception_hint::VARCHAR||E'\n'||
			'Detail:   '||v_detail::VARCHAR||E'\n'||
			'Context:  '||v_context::VARCHAR||E'\n'||
			'SQLSTATE: '||v_sqlstate::VARCHAR||E'\n'||'<<< End of trace.'||E'\n';
		IF COALESCE(v_schema_name, '') != '' AND COALESCE(v_table_name, '') != '' THEN
			error_message:=error_message||'Object: '||v_schema_name||'.'||v_table_name;	
		END IF;
		IF COALESCE(v_column_name, '') != '' THEN
			error_message:=error_message||'.'||v_column_name;	
		END IF;
		IF COALESCE(v_pg_datatype_name, '') != '' THEN
			error_message:=error_message||'; datatype: '||v_pg_datatype_name;	
		END IF;
		IF COALESCE(v_constraint_name, '') != '' THEN
			error_message:=error_message||'; constraint: '||v_constraint_name;	
		END IF;			
		RAISE WARNING '71176: %', error_message;
--
-- Check error SQLSTATE
--
		BEGIN
			IF error_code_expected IS NULL THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71177] Test case: % FAILED, no error expected; got: %;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);		
				IF raise_exception_on_failure THEN
					RAISE;
				ELSE					
					RETURN FALSE;
				END IF;		
			ELSIF v_sqlstate = 'P0001' AND error_code_expected = v_detail THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
					'[71178] Test case: % PASSED, caught expecting SQLSTATE/RIF error code: %/%;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR, error_code_expected::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);								
				RETURN TRUE;				
			ELSIF error_code_expected = v_sqlstate THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
					'[71179] Test case: % PASSED, caught expecting SQLSTATE %;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);							
				RETURN TRUE;			
			ELSE	
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71180] Test case: % FAILED, expecting SQLSTATE %; got: %;%', 
					test_case_title::VARCHAR, error_code_expected::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);		
				IF raise_exception_on_failure THEN
					RAISE;
				ELSE							
					RETURN FALSE;
				END IF;
			END IF;
		EXCEPTION
			WHEN others THEN			
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
				GET STACKED DIAGNOSTICS v_context = PG_EXCEPTION_CONTEXT;
				error_message:='rif40_sql_test() exception handler caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||
					'Detail: '||v_detail::VARCHAR||E'\n'||
					'Context: '||v_context::VARCHAR||E'\n'||
					'SQLSTATE: '||v_sqlstate::VARCHAR;
				RAISE EXCEPTION '71181: %', error_message USING DETAIL=v_detail;			
		END;
END;
$func$ LANGUAGE plpgsql;
	
COMMENT ON FUNCTION rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN) IS 'Function: 	rif40_sql_test()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title, result arrays,
			[negative] error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, raise exception on failure  
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

			Used to check test SQL statements and triggers
			
All SAVEPOINT/ROLLBACK function disabled in Postgres 9.3 (it does work on 9.4!)';
	
--
-- So can be used to test non rif user access to functions 
--
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY, VARCHAR, BOOLEAN) TO PUBLIC;
	
--
-- Test code generator
--
SELECT rif40_sql_pkg._rif40_test_sql_template(
	'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
	'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200') AS template;
	
--
-- Eof
	
