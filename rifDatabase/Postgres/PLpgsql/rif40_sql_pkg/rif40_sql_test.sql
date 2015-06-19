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
-- rif40_sql_test:										71050 to 71099
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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_reduce_dim(my_array ANYARRAY)
RETURNS SETOF ANYARRAY
LANGUAGE plpgsql
AS $func$
/*
Function: 	_rif40_reduce_dim()
Parameters:	2D array 
Returns:	Set of 1D array rows
Description:
			Array reduce from two to one dimension as a set of 1D array rows
			Can then be cast to a record. Helper function for rif40_sql_test()
E.g.

	WITH a AS (
		SELECT '{{01,01.015,01.015.016200,01.015.016200.2}
	,{01,01.015,01.015.016200,01.015.016200.3} 
	,{01,01.015,01.015.016200,01.015.016200.4} 
	,{01,01.015,01.015.016900,01.015.016900.1} 
	,{01,01.015,01.015.016900,01.015.016900.2} 
	,{01,01.015,01.015.016900,01.015.016900.3} 
	}'::Text[][] AS res
	)
	SELECT rif40_sql_pkg._rif40_reduce_dim(a.res) AS res
	  FROM a;
		
						res
	-------------------------------------------
	 {01,01.015,01.015.016200,01.015.016200.2}
	 {01,01.015,01.015.016200,01.015.016200.3}
	 {01,01.015,01.015.016200,01.015.016200.4}
	 {01,01.015,01.015.016900,01.015.016900.1}
	 {01,01.015,01.015.016900,01.015.016900.2}
	 {01,01.015,01.015.016900,01.015.016900.3}
	(6 rows)
	
 */
DECLARE 
	s my_array%type;
BEGIN
	FOREACH s SLICE 1  IN ARRAY $1 LOOP
		RETURN NEXT s;
	END LOOP;
--
	RETURN;
END;
$func$;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_reduce_dim(ANYARRAY) IS 'Function: 	_rif40_reduce_dim()
Parameters:	2D array 
Returns:	Set of 1D array rows
Description:
			Array reduce from two to one dimension as a set of 1D array rows
			Can then be cast to a record. Helper function for rif40_sql_test()
E.g.

	WITH a AS (
		SELECT ''{{01,01.015,01.015.016200,01.015.016200.2}
	,{01,01.015,01.015.016200,01.015.016200.3} 
	,{01,01.015,01.015.016200,01.015.016200.4} 
	,{01,01.015,01.015.016900,01.015.016900.1} 
	,{01,01.015,01.015.016900,01.015.016900.2} 
	,{01,01.015,01.015.016900,01.015.016900.3} 
	}''::Text[][] AS res
	)
	SELECT rif40_sql_pkg._rif40_reduce_dim(a.res) AS res
	  FROM a;
		
						res
	-------------------------------------------
	 {01,01.015,01.015.016200,01.015.016200.2}
	 {01,01.015,01.015.016200,01.015.016200.3}
	 {01,01.015,01.015.016200,01.015.016200.4}
	 {01,01.015,01.015.016900,01.015.016900.1}
	 {01,01.015,01.015.016900,01.015.016900.2}
	 {01,01.015,01.015.016900,01.015.016900.3}
	(6 rows)';

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_test_sql_template(test_stmt VARCHAR, test_case_title VARCHAR)
RETURNS SETOF VARCHAR
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_test_sql_template()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, test case title  
Returns:	PL/pgsql template code 
Description: Generate PL/pgsql template code, e.g.

SELECT rif40_sql_pkg._rif40_test_sql_template(
	'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
	'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200') AS template;
	
template
--------	
	IF NOT (PERFORM rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][])) THEN
		errors:=errors+1;
	END IF;
		
 */
DECLARE 

BEGIN
	RETURN NEXT E'\t'||'IF NOT (PERFORM rif40_sql_pkg.rif40_sql_test(';
	RETURN NEXT E'\t'||E'\t'||''''||REPLACE(test_stmt, '''', '''''')||'''';
	RETURN NEXT E'\t'||E'\t'||''''||REPLACE(test_case_title, '''', '''''')||'''';		
	RETURN NEXT E'\t'||E'\t'||'}''::Text[][])) THEN';
	RETURN NEXT E'\t'||E'\t'||'errors:=errors+1;';
	RETURN NEXT E'\t'||'END IF;'
--
	RETURN;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_test_sql_template(VARCHAR, VARCHAR) IS 'Function: 	_rif40_test_sql_template()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, test case title  
Returns:	PL/pgsql template code 
Description: Generate PL/pgsql template code, e.g.

SELECT rif40_sql_pkg._rif40_test_sql_template(
	''SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''''01.015.016900'''', ''''01.015.016200'''') ORDER BY level4'',
	''Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200'') AS template;
	
template
--------	
	IF NOT (PERFORM rif40_sql_pkg.rif40_sql_test(
		''SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''''01.015.016900'''', ''''01.015.016200'''') ORDER BY level4'',
		''Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200'',
		''{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}''::Text[][])) THEN
		errors:=errors+1;
	END IF;
	
';
 
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
			
			Usage:
			
			a) SELECT statements:
			
			   i)   Original SQL statement:
			        SELECT * FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200');
			   ii)  Add ORDER BY clause, expand * (This becomes the test case SQL)
			        SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200') ORDER BY level4;			   
			   iii) Convert to array form (Cast to text, string ) 
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
 */
DECLARE	
	sql_frag 	VARCHAR;
	sql_stmt 	VARCHAR;
--
	c1sqlt 		REFCURSOR;
	c1sqlt_result_row RECORD;
	c2sqlt 		REFCURSOR;
	c2sqlt_result_row RECORD;	
--
	extra		INTEGER:=0;
	missing		INTEGER:=0;	
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
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71050] SQL[SELECT]> %;', 
			sql_stmt::VARCHAR);
		OPEN c1sqlt FOR EXECUTE sql_stmt;
		LOOP
			FETCH c1sqlt INTO c1sqlt_result_row;
			IF NOT FOUND THEN EXIT; END IF;
--
			extra:=extra+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71051] extra[%]: %', missing::VARCHAR, c1sqlt_result_row.extra_data::VARCHAR);		
		END LOOP;
		CLOSE c1sqlt;
		IF extra = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71052] PASSED: no extra rows for test: %', test_case_title::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71053] FAILED: % extra rows for test: %', extra::VARCHAR, test_case_title::VARCHAR);	
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
				'[71054] missing[%]: %', missing::VARCHAR, c2sqlt_result_row.missing_data::VARCHAR);
		END LOOP;
		CLOSE c2sqlt;
		IF missing = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71055] PASSED: no missing rows for test: %', test_case_title::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71056] FAILED: % missing rows for test: %', missing::VARCHAR, test_case_title::VARCHAR);	
		END IF;
--		
		IF error_code_expected IS NOT NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71057] FAILED: Test case: % no exception, expected SQLSTATE: %', 
				test_case_title::VARCHAR, error_code_expected::VARCHAR);				
			RETURN FALSE;	
		ELSIF extra = 0 AND missing = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71058] PASSED: Test case: % no exceptions, no errors, no missing or extra data', 
				test_case_title::VARCHAR);			
			RETURN TRUE;
		ELSIF raise_exception_on_failure THEN
			RAISE no_data_found;
		ELSE /* Just failed above */
			RETURN FALSE;	
		END IF;
--
-- Other SQL test statements (i.e. INSERT/UPDATE/DELETE for triggers) with RETURNING clause
--
	ELSIF results IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71050] SQL[INSERT/UPDATE/DELETE; RETURNING]> %;', 
			test_stmt::VARCHAR);	
		OPEN c1sqlt FOR EXECUTE test_stmt;
		FETCH c1sqlt INTO c1sqlt_result_row;
		CLOSE c1sqlt;
		IF error_code_expected IS NOT NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71059] FAILED: Test case: % no exception, expected SQLSTATE: %', 
				test_case_title::VARCHAR, error_code_expected::VARCHAR);				
			RETURN FALSE;	
		ELSIF c1sqlt_result_row.test_value = results[1] THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71060] PASSED: Test case: % no exceptions, no errors, return value as expected: %', 
				test_case_title::VARCHAR, results[1]::VARCHAR);			
			RETURN TRUE;
		ELSIF raise_exception_on_failure THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71061] FAILED: Test case: % got: %, expected: %', 
				test_case_title::VARCHAR, c1sqlt_result_row.test_value::VARCHAR, results[1]::VARCHAR);			
			RAISE no_data_found;
		ELSE /* Value test failed */
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71062] FAILED: Test case: % got: %, expected: %', 
				test_case_title::VARCHAR, c1sqlt_result_row.test_value::VARCHAR, results[1]::VARCHAR);			
			RETURN FALSE;
 		END IF;
	
--
-- Other SQL test statements (i.e. INSERT/UPDATE/DELETE for triggers)
--
	ELSE
		PERFORM rif40_sql_pkg.rif40_ddl(test_stmt);

		IF error_code_expected IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71063] PASSED: Test case: % no exceptions, no error expected', 
				test_case_title::VARCHAR);		
			RETURN TRUE;
		ELSIF raise_exception_on_failure THEN		
			RAISE no_data_found;			
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71064] FAILED: Test case: % no exceptions, expected SQLSTATE: %', 
				test_case_title::VARCHAR, error_code_expected::VARCHAR);		
			RETURN FALSE;		
		END IF;
	END IF;
--
EXCEPTION
	WHEN no_data_found THEN	
		IF error_code_expected IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-71065, 'rif40_sql_test', 
				'Test case: % FAILED, % errors', 
				test_case_title::VARCHAR, (extra+missing)::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-71066, 'rif40_sql_test', 
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
		RAISE WARNING '71067: %', error_message;
--
-- Check error SQLSTATE
--
		BEGIN
			IF error_code_expected IS NULL THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71068] Test case: % FAILED, no error expected; got: %;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);		
				IF raise_exception_on_failure THEN
					RAISE;
				ELSE				
					RETURN FALSE;
				END IF;			
			ELSIF error_code_expected = v_sqlstate THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71069] Test case: % PASSED, caught expecting SQLSTATE %;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);			
				RETURN TRUE;
			ELSE	
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71070] Test case: % FAILED, expecting SQLSTATE %; got: %;%', 
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
				RAISE EXCEPTION '71070: %', error_message USING DETAIL=v_detail;			
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

			Used to check test SQL statements and triggers';
	
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
	
