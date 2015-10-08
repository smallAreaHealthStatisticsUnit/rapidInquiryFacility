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
-- 
-- _rif40_sql_test:									71250 to 71299
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

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, INTEGER, VARCHAR);
-- Old	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN, INTEGER);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, INTEGER);
	
CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_sql_test(
	test_stmt 					VARCHAR, 
	test_case_title 			VARCHAR, 
	results 					ANYARRAY, 
	results_xml					XML,
	pg_error_code_expected 		VARCHAR, 
	raise_exception_on_failure 	BOOLEAN, 
	f_test_id 					INTEGER, 
	pg_debug_functions			Text[] DEFAULT NULL)
RETURNS boolean
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_sql_test()
Parameters:	SQL test (SELECT or INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title,
 			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first 
				negative number in the message is assumed to be the number; 
				NULL means it is expected to NOT raise an exception, 
			raise exception on failure,
			test_id,
			Array of Postgres functions for test harness to enable debug on
Returns:	Test pass (true)/Fail (false) unless raise_exception_on_failure is true

			Note that this is the result of the test and is not influenced by the expected result:
			
			To pass:
			
			* No exception, results as expected;
			* Exception as expected
			
			Everything else is a fail.
Description: Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

			Used to check test SQL statements and triggers	
*/
DECLARE
--
	c1sqlt 				REFCURSOR;
	c1sqlt_result_row 	RECORD;
	c2sqlt 				REFCURSOR;
	c2sqlt_result_row 	RECORD;
--	
	sql_frag 	VARCHAR;
	sql_stmt 	VARCHAR;
--
	extra		INTEGER:=0;
	missing		INTEGER:=0;	
--
	f_pass		BOOLEAN;
	f_result	BOOLEAN;
--
	l_pg_debug_function VARCHAR;
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
-- Add test specific debug messages
--
	IF pg_debug_functions IS NOT NULL THEN
		FOREACH l_pg_debug_function IN ARRAY pg_debug_functions LOOP
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
				'[71250]: Enable debug for function: %', 
				l_pg_debug_function::VARCHAR);
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_pg_debug_function||':DEBUG1');		
		END LOOP;
	END IF;
	
--
-- Do test
--
	IF UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 6)) = 'SELECT' OR UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 4)) = 'WITH' THEN
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
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_sql_test', '[71251] SQL[SELECT]> %;', 
			sql_stmt::VARCHAR);
		OPEN c1sqlt FOR EXECUTE sql_stmt;
		LOOP
			FETCH c1sqlt INTO c1sqlt_result_row;
			IF NOT FOUND THEN EXIT; END IF;
--
			extra:=extra+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71252] Test case %: % extra[%]: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, c1sqlt_result_row.extra_data::VARCHAR);		
		END LOOP;
		CLOSE c1sqlt;
		IF extra = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
				'[71253] PASSED: test case %: no extra rows for test: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71254] FAILED: test case %: % extra rows for test: %', 
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
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71255] Test case %: % missing[%]: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, c2sqlt_result_row.missing_data::VARCHAR);
		END LOOP;
		CLOSE c2sqlt;
		
--
-- Check for missing
--
		IF missing = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
				'[71256] PASSED: no missing rows for test case %: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71257] FAILED: Test case %: % % missing rows for test: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, test_case_title::VARCHAR);	
		END IF;
--		
		IF pg_error_code_expected IS NOT NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71258] FAILED: Test case %: % no exception, expected SQLSTATE: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, pg_error_code_expected::VARCHAR);				
			f_pass:=FALSE;	
		ELSIF extra = 0 AND missing = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
				'[71259] PASSED: Test case %: % no exceptions, no errors, no missing or extra data', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);			
			f_pass:=TRUE;
		ELSIF raise_exception_on_failure THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71260] FAILED: Test case %: % % missing, % extra', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, extra::VARCHAR);		
			RAISE no_data_found;
		ELSE /* Just failed above */
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71261] FAILED: Test case %: % % missing, % extra', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, extra::VARCHAR);			
			f_pass:=FALSE;	
		END IF;
--
-- Other SQL test statements (i.e. INSERT/UPDATE/DELETE for triggers) with RETURNING clause
--
	ELSIF UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 6)) IN ('INSERT', 'UPDATE', 'DELETE') OR
	      UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 2)) = 'DO' THEN
		IF results IS NOT NULL THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_sql_test', '[71262] SQL[INSERT/UPDATE/DELETE; RETURNING]> %;', 
				test_stmt::VARCHAR);	
			OPEN c1sqlt FOR EXECUTE test_stmt;
			FETCH c1sqlt INTO c1sqlt_result_row;
			CLOSE c1sqlt;
		
--
-- Check for errors (or rather the lack of them)
--		
			IF pg_error_code_expected IS NOT NULL THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71263] FAILED: Test case %: % no exception, expected SQLSTATE: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, pg_error_code_expected::VARCHAR);				
				f_pass:=FALSE;	
			ELSIF c1sqlt_result_row.test_value = results[1] THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
					'[71264] PASSED: Test case %: % no exceptions, no errors, return value as expected: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, results[1]::VARCHAR);			
				f_pass:=TRUE;
			ELSIF raise_exception_on_failure THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71265] FAILED: Test case %: % got: %, expected: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, c1sqlt_result_row.test_value::VARCHAR, results[1]::VARCHAR);			
				RAISE no_data_found;
			ELSE /* Value test failed */
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71266] FAILED: Test case %: % got: %, expected: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, c1sqlt_result_row.test_value::VARCHAR, results[1]::VARCHAR);			
				f_pass:=FALSE;
			END IF;
		
--
-- Other SQL test statements (i.e. INSERT/UPDATE/DELETE for triggers)
--
		ELSE
			PERFORM rif40_sql_pkg.rif40_ddl(test_stmt);
				
--
-- Check for errors (or rather the lack of them)
--			
			IF pg_error_code_expected IS NULL THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
					'[71267] PASSED: Test case %: % no exceptions, no error expected', 
					f_test_id::VARCHAR, test_case_title::VARCHAR);		
				f_pass:=TRUE;
			ELSIF raise_exception_on_failure THEN		
				RAISE no_data_found;			
			ELSE
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71268] FAILED: Test case %: % no exceptions, expected SQLSTATE: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, pg_error_code_expected::VARCHAR);		
				f_pass:=FALSE;		
			END IF;
		END IF;
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-71269, '_rif40_sql_test', 
			'Test case %: % FAILED, invalid statement type: %SQL> %;', 
			f_test_id::VARCHAR, test_case_title::VARCHAR, 
			E'\n'::VARCHAR, test_stmt::VARCHAR);	
	END IF;
--
-- Remove test specific debug messages
--
	IF pg_debug_functions IS NOT NULL THEN
		FOREACH l_pg_debug_function IN ARRAY pg_debug_functions LOOP
			PERFORM rif40_log_pkg.rif40_remove_from_debug(l_pg_debug_function);		
		END LOOP;
	END IF;	
--
	RETURN f_pass;
--
EXCEPTION
	WHEN no_data_found THEN	
		IF pg_error_code_expected IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-71270, '_rif40_sql_test', 
				'Test case: % FAILED, % errors', 
				test_case_title::VARCHAR, (extra+missing)::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-71271, '_rif40_sql_test', 
				'Test case: % FAILED, % errors; expecting SQLSTATE: %; not thrown', 
				test_case_title::VARCHAR, (extra+missing)::VARCHAR, pg_error_code_expected::VARCHAR);	
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
		
		error_message:='_rif40_sql_test('''||coalesce(test_case_title::VARCHAR, '')||''') caught: '||E'\n'||SQLERRM::VARCHAR||
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
		RAISE WARNING '71272: %', error_message;
--
-- Check error SQLSTATE
--
		BEGIN
			IF pg_error_code_expected IS NULL THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71273] Test case: % FAILED, no error expected; got: %;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);		
				IF raise_exception_on_failure THEN
					RAISE;
				ELSE					
					RETURN FALSE;
				END IF;		
			ELSIF v_sqlstate = 'P0001' /* PL/pgSQL raise_exception */ AND pg_error_code_expected = v_detail THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
					'[71274] Test case: % PASSED, caught expecting SQLSTATE/RIF error code: %/%;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR, pg_error_code_expected::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
						'Detail:   '||v_detail::VARCHAR);								
				RETURN TRUE;				
			ELSIF pg_error_code_expected = v_sqlstate THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
					'[71275] Test case: % PASSED, caught expecting SQLSTATE %;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
						'Detail:   '||v_detail::VARCHAR);							
				RETURN TRUE;			
			ELSIF v_sqlstate = 'P0001' /* PL/pgSQL raise_exception */ AND pg_error_code_expected != v_detail THEN			
--
-- Test case failed with a different error to that expected; re-RAISE with no_data_found
-- for rif40_sql_test() to process
--			
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71276] Test case: % FAILED, expecting SQLSTATE PG_EXCEPTION_DETAIL %; got: %;%', 
					test_case_title::VARCHAR, pg_error_code_expected::VARCHAR, v_detail::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
						'Detail:   '||v_detail::VARCHAR||E'\n');	
				RAISE EXCEPTION SQLSTATE 'P0002' /* no_data_found */ USING DETAIL=v_detail;
			ELSE				
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71277] Test case: % FAILED, expecting SQLSTATE P0001; got %; expecting PG_EXCEPTION_DETAIL: %;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR, pg_error_code_expected::VARCHAR, v_detail::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n');	
				RAISE EXCEPTION SQLSTATE 'P0002' /* no_data_found */ USING DETAIL=v_sqlstate;			
			END IF;
		EXCEPTION
--
-- Test case failed with a different error to that expected; re-RAISE with no_data_found
-- for rif40_sql_test() to process
--		
			WHEN no_data_found THEN	
				RAISE;

			WHEN others THEN			
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
				GET STACKED DIAGNOSTICS v_context = PG_EXCEPTION_CONTEXT;
				error_message:='_rif40_sql_test() exception handler caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||
					'Detail: '||v_detail::VARCHAR||E'\n'||
					'Context: '||v_context::VARCHAR||E'\n'||
					'SQLSTATE: '||v_sqlstate::VARCHAR;
				RAISE EXCEPTION '71278: %', error_message USING DETAIL=v_detail;			
		END;
END;
$func$ LANGUAGE plpgsql;
	
COMMENT ON FUNCTION rif40_sql_pkg._rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, INTEGER, Text[]) IS 'Function: 	_rif40_sql_test()
Parameters:	SQL test (SELECT or INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title,
 			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first 
			negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, raise exception on failure,
			test_id,
			Array of Postgres functions for test harness to enable debug on
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE

			Note that this is the result of the test and is not influenced by the expected result:
			
			To pass:
			
			* No exception, results as expected;
			* Exception as expected
			
			Everything else is a fail.
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

			Used to check test SQL statements and triggers';
	
--
-- Eof	