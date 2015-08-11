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
-- _rif40_test_sql_template:										71100 to 71149
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

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_test_sql_template(VARCHAR, VARCHAR);

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_test_sql_template(test_stmt VARCHAR, test_case_title VARCHAR)
RETURNS SETOF VARCHAR
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_test_sql_template()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, test case title  
Returns:	PL/pgsql template code 
Description: Generate PL/pgsql template code, e.g.


Add support for XML:

WITH a AS (
	SELECT query_to_xml(
'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4', 
true /- nulls -/, true /- tableforest -/, ''::Text /- Namespace -/) AS xml_line
)
SELECT regexp_split_to_table(a.xml_line, '\n') AS xml_str
  FROM a;
  

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

SELECT ''''||
		   REPLACE(ARRAY_AGG(
				(ARRAY[level1::Text, level2::Text, level3::Text, level4::Text]::Text||E'\n')::Text ORDER BY level4)::Text, 
				'"'::Text, ''::Text)||'''::Text[][]' AS res 
	  FROM sahsuland_geography
	 WHERE level3 IN ('01.015.016900', '01.015.016200');
	 
 */
DECLARE 
	sql_stmt 	VARCHAR;
	drop_stmt 	VARCHAR;
	select_text	VARCHAR:=NULL;
	temp_table 	VARCHAR:=NULL;
--
	c1sqlt CURSOR(l_table VARCHAR) FOR /* Extra table/view columns */
		SELECT table_name, column_name,
		       CASE 													/* Work out column length */
				WHEN numeric_precision IS NOT NULL /* bits */ AND
				     LENGTH((2^numeric_precision)::Text) > LENGTH(column_name) AND 
				     LENGTH((2^numeric_precision)::Text) <= 40							THEN LENGTH((2^numeric_precision)::Text)
				WHEN numeric_precision IS NOT NULL /* bits */ AND
				     LENGTH((2^numeric_precision)::Text) > LENGTH(column_name) AND 
				     LENGTH((2^numeric_precision)::Text) > 40							THEN 40 /* Truncate at 40 characters */
				WHEN datetime_precision IS NOT NULL /* bits */  AND
				     LENGTH((2^datetime_precision)::Text) > LENGTH(column_name) AND 
				     LENGTH((2^datetime_precision)::Text) <= 40							THEN LENGTH((2^datetime_precision)::Text)
				WHEN datetime_precision IS NOT NULL /* bits */  AND
				     LENGTH((2^datetime_precision)::Text) > LENGTH(column_name) AND 
				     LENGTH((2^datetime_precision)::Text) > 40							THEN 40 /* Truncate at 40 characters */
				WHEN character_maximum_length > LENGTH(column_name) AND 
				     character_maximum_length <= 40 									THEN character_maximum_length
				WHEN character_maximum_length > LENGTH(column_name) AND 
				     character_maximum_length > 40 										THEN 40 /* Truncate at 40 characters */
				ELSE LENGTH(column_name)
		       END column_length
		  FROM information_schema.columns a, pg_tables b 
		 WHERE b.schemaname = a.table_schema
		   AND a.table_name = b.tablename
		   AND b.tableowner = USER
		   AND b.tablename  = l_table; 
	c2sqlt REFCURSOR;
	c3sqlt CURSOR(l_sql_stmt VARCHAR) FOR
		WITH a AS (
			SELECT query_to_xml(l_sql_stmt,
						true /* include nulls */, true /* tableforest */, ''::Text /* No namespace */)::Text AS xml_line
		), b as (
			SELECT regexp_split_to_table(a.xml_line, '\n') AS xml_str
			FROM a
		)
		SELECT xml_str AS xml_str
		  FROM b
		  WHERE b.xml_str IS NOT NULL AND b.xml_str != '';
 --
	c1sqlt_rec RECORD;
	c2sqlt_rec RECORD;
	c3sqlt_rec RECORD;
--
	xml_str			VARCHAR;
--
	stp 			TIMESTAMP WITH TIME ZONE;
	etp 			TIMESTAMP WITH TIME ZONE;
	took 			INTERVAL;
	j 				INTEGER:=0;
	l_column_name	VARCHAR;
--
	error_message 	VARCHAR;
	v_sqlstate 		VARCHAR;
	v_context		VARCHAR;	
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- Must be postgres, rif40 or have rif_user or rif_manager role
--
	IF USER NOT IN ('postgres', 'rif40') AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-71100, '_rif40_test_sql_template', 'User % must be postgres, rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	IF test_stmt IS NULL THEN
		IF title IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_test_sql_template', '[71101] NULL SQL statement, NULL title');
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_test_sql_template', '[71102] NULL SQL statement for: %', title::VARCHAR);
		END IF;
		RETURN;
	END IF;
	stp:=clock_timestamp();
--
-- Create results temporary table
--
	temp_table:='l_'||REPLACE(rif40_sql_pkg.sys_context(NULL, 'AUDSID'), '.', '_');
--
-- This could do with checking first to remove the notice:
-- psql:v4_0_rif40_sql_pkg.sql:3601: NOTICE:  table "l_7388_2456528_62637_130282_7388" does not exist, skipping
-- CONTEXT:  SQL statement "DROP TABLE IF EXISTS l_7388_2456528_62637_130282"
-- PL/pgSQL function "rif40_ddl" line 32 at EXECUTE statement
--
	drop_stmt:='DROP TABLE IF EXISTS '||temp_table;
	PERFORM rif40_sql_pkg.rif40_drop_user_table_or_view(temp_table);
--
-- SQL injection check
--
-- ADD

--
	sql_stmt:='CREATE TEMPORARY TABLE '||quote_ident(temp_table)||' AS '||E'\n'||test_stmt;
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
--
-- Process table header
--
/*
	SELECT ''''||
		   REPLACE(
				ARRAY_AGG(
						(ARRAY[
							level1::Text, level2::Text, level3::Text, level4::Text]
							::Text||E'\n')::Text ORDER BY level4)::Text, 
				'"'::Text, ''::Text)||'''::Text[][]' AS res 
	  FROM sahsuland_geography
	 WHERE level3 IN ('01.015.016900', '01.015.016200');
				
SELECT ''''|| REPLACE(
                ARRAY_AGG(
                        (ARRAY[
                                level1::Text,
                                level2::Text,
                                level3::Text,
                                level4::Text]::Text||E'\n')::Text
                         ORDER BY level4)::Text,
                '"'::Text, ''::Text)||'''::Text[][]' AS res
  FROM l_10900_2457200_51137_541000
 */
	select_text:='SELECT ''''''''|| REPLACE('||E'\n'||
				E'\t'||E'\t'||'ARRAY_AGG('||E'\n'||
				E'\t'||E'\t'||E'\t'||'(ARRAY[';

	FOR c1sqlt_rec IN c1sqlt(temp_table) LOOP
		j:=j+1;
		l_column_name:=c1sqlt_rec.column_name;
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', '_rif40_test_sql_template', '[71104] Column[%] %.%, length: %', 
			j::VARCHAR, 
			c1sqlt_rec.table_name::VARCHAR, 
			c1sqlt_rec.column_name::VARCHAR, 
			c1sqlt_rec.column_length::VARCHAR);
		IF j = 1 THEN
			select_text:=select_text||E'\n'||E'\t'||E'\t'||E'\t'||E'\t'||quote_ident(c1sqlt_rec.column_name)||'::Text';
		ELSE
			select_text:=select_text||','||E'\n'||E'\t'||E'\t'||E'\t'||E'\t'||quote_ident(c1sqlt_rec.column_name)||'::Text';
		END IF;			
	END LOOP;
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('DEBUG2', '_rif40_test_sql_template', '[71105] Statement took: %', 
		took::VARCHAR);	
--
	select_text:=select_text||']::Text||E''\n'')::Text'||E'\n'||E'\t'||E'\t'||E'\t'||' ORDER BY '||l_column_name||')::Text,'||E'\n'||
		E'\t'||E'\t'||'''"''::Text, ''''::Text)||''''''::Text[][]'' AS res'||E'\n'||
		'  FROM '||quote_ident(temp_table);	
	sql_stmt:=select_text;
	OPEN c2sqlt FOR EXECUTE sql_stmt;
	FETCH c2sqlt INTO c2sqlt_rec;
	IF NOT FOUND THEN
		PERFORM rif40_log_pkg.rif40_error(-71105, '_rif40_test_sql_template', 
			'No data returned for cursor c2sqlt, SQL> %, test SQL> %;',
			sql_stmt::VARCHAR,
			test_stmt::VARCHAR);
	END IF;
	CLOSE c2sqlt;
--
	PERFORM rif40_sql_pkg.rif40_ddl(drop_stmt);		
--
	RETURN NEXT E'\t'||'IF NOT (rif40_sql_pkg.rif40_sql_test(';
	RETURN NEXT E'\t'||E'\t'||''''||REPLACE(test_stmt, '''', '''''')||''',';
	RETURN NEXT E'\t'||E'\t'||''''||REPLACE(test_case_title, '''', '''''')||''',';		
--
--	RETURN NEXT E'\t'||E'\t'||'/* SQL> '||E'\n'||select_text||E'\n'||' */';
	RETURN NEXT c2sqlt_rec.res||',';
--
-- Now dump XML
--
	j:=0;
	FOR c3sqlt_rec IN c3sqlt(test_stmt) LOOP
		j:=j+1;	
		IF j = 1 THEN
			xml_str:=''''||c3sqlt_rec.xml_str||E'\n';
		ELSE
			xml_str:=xml_str||c3sqlt_rec.xml_str||E'\n';
		END IF;
	END LOOP;
	RETURN NEXT xml_str||'''::XML';
	RETURN NEXT E'\t'||E'\t'||')) THEN';
	RETURN NEXT E'\t'||E'\t'||'f_errors:=f_errors+1;';
	RETURN NEXT E'\t'||'END IF;'
--
	RETURN;
EXCEPTION
	WHEN others THEN
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
		GET STACKED DIAGNOSTICS v_context = PG_EXCEPTION_CONTEXT;
		error_message:='_rif40_test_sql_template() exception handler caught: '||E'\n'||SQLERRM::VARCHAR||E'\n'||
			'in SQL> '||sql_stmt||';'||E'\n'||
			'test SQL> '||test_stmt||';'||E'\n'||			
			'Detail: '||v_detail::VARCHAR||E'\n'||
			'Context: '||v_context::VARCHAR||E'\n'||
			'SQLSTATE: '||v_sqlstate::VARCHAR;
		RAISE EXCEPTION '71103: %', error_message USING DETAIL=v_detail;			
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
		}''::Text[][],
		''<row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
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
 </row>''::XML
		)) THEN
		errors:=errors+1;
	END IF;
	
';
	
--
-- Eof