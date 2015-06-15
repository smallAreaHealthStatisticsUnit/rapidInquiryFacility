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
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_sql_test(test_stmt VARCHAR, test_case_title VARCHAR, results ANYARRAY,
	error_code_expected INTEGER DEFAULT NULL, raise_exception_on_failure BOOLEAN DEFAULT TRUE)
RETURNS boolean
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_sql_test()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title, result arrays,
			[negative] error code expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
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
	missing	INTEGER:=0;	
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
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
	PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_sql_test', 'SQL[1]> %;', 
		sql_stmt::VARCHAR);
	OPEN c1sqlt FOR EXECUTE sql_stmt;
	LOOP
		FETCH c1sqlt INTO c1sqlt_result_row;
		IF NOT FOUND THEN EXIT; END IF;
--
		extra:=extra+1;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
			'extra[%]: %', missing::VARCHAR, c1sqlt_result_row.extra_data::VARCHAR);		
	END LOOP;
	CLOSE c1sqlt;
	IF extra = 0 THEN 
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
			'PASSED: no extra rows for test: %', test_case_title::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
			'FAILED: % extra rows for test: %', extra::VARCHAR, test_case_title::VARCHAR);	
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
			'missing[%]: %', missing::VARCHAR, c2sqlt_result_row.missing_data::VARCHAR);
	END LOOP;
	CLOSE c2sqlt;
	IF missing = 0 THEN 
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
			'PASSED: no missing rows for test: %', test_case_title::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
			'FAILED: % missing rows for test: %', missing::VARCHAR, test_case_title::VARCHAR);	
	END IF;
--		
	IF extra = 0 AND missing = 0 THEN 
		RETURN TRUE;
	ELSIF raise_exception_on_failure THEN
		RAISE no_data_found;
	ELSE
		RETURN FALSE;	
	END IF;
--
EXCEPTION
	WHEN no_data_found THEN	
		PERFORM rif40_log_pkg.rif40_error(-90125, 'rif40_sql_test', 
			'Test case: % FAILED, % errors', test_case_title::VARCHAR, (extra+missing)::VARCHAR);
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		error_message:='rif40_sql_test('''||coalesce(test_case_title::VARCHAR, '')||''') caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE WARNING '1: %', error_message;
		RAISE;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	INTEGER, BOOLEAN) IS 'Function: 	rif40_sql_test()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title, result arrays,
			[negative] error code expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, raise exception on failure  
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

			Used to check test SQL statements and triggers';
	
--
-- So can be used to test non rif user access to functions 
--
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY, INTEGER, BOOLEAN) TO PUBLIC;

--
-- Test code generator
--
SELECT rif40_sql_pkg._rif40_test_sql_template(
	'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
	'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200') AS template;
	
--
-- Test case a)
--	
DO LANGUAGE plpgsql $$
BEGIN	
	PERFORM rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][]
		/* Use defaults */);
--
/*
	PERFORM rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][]
		-* Use defaults *=); */
END;
$$;
  
--
-- Eof
	