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
-- Rapid Enquiry Facility (RIF) - Dynamic method 4 select implementation
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
		RAISE EXCEPTION 'C20900: UsEr check failed: % is not rif40', user;	
	END IF;
END;
$$;

--
-- Dynamic SQL method 4 (Oracle name) SELECT 
--
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_method4(select_stmt VARCHAR, title VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_method4()
Parameters:	SQL SELECT statement, title
Returns:	Nothing
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement

SELECT column_name, data_type, character_maximum_length, character_octet_length, numeric_precision, numeric_precision_radix, numeric_scale, datetime_precision
  FROM information_schema.columns a, pg_tables b 
		 WHERE b.schemaname = a.table_schema
		   AND a.table_name = b.tablename
		   AND b.tablename  = 'rif40_geographies';
 */
DECLARE
	sql_stmt 	VARCHAR;
	drop_stmt 	VARCHAR;
	select_text	VARCHAR:=NULL;
	temp_table 	VARCHAR:=NULL;
--
	c1m4 CURSOR(l_table VARCHAR) FOR /* Extra table/view columns */
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
				     character_maximum_length <= 40 								THEN character_maximum_length
				WHEN character_maximum_length > LENGTH(column_name) AND 
				     character_maximum_length > 40 								THEN 40 /* Truncate at 40 characters */
				ELSE LENGTH(column_name)
		       END column_length
		  FROM information_schema.columns a, pg_tables b 
		 WHERE b.schemaname = a.table_schema
		   AND a.table_name = b.tablename
		   AND b.tableowner = USER
		   AND b.tablename  = l_table; 
	c2m4 REFCURSOR;
	c1m4_rec RECORD;
	c2m4_result_row	VARCHAR[];
--
	stp 		TIMESTAMP WITH TIME ZONE;
	etp 		TIMESTAMP WITH TIME ZONE;
	took 		INTERVAL;
	l_rows 		INTEGER:=0;
	j 		INTEGER:=0;
	display_len	INTEGER:=0;
	column_len	INTEGER[];
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Must be rifupg34, postgres, rif40 or have rif_user or rif_manager role
--
	IF USER NOT IN ('postgres', 'rifupg34', 'rif40') AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_method4', 'User % must be postgres, rif40, rifupg34 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	IF select_stmt IS NULL THEN
		IF title IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_method4', 'NULL SQL statement, NULL title');
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_method4', 'NULL SQL statement for: %', title::VARCHAR);
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
	sql_stmt:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE '||temp_table||' AS '||E'\n'||select_stmt;
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Process table header
--
	FOR c1m4_rec IN c1m4(temp_table) LOOP
		j:=j+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_method4', 'Column[%] %.%, length: %', 
			j::VARCHAR, 
			c1m4_rec.table_name::VARCHAR, 
			c1m4_rec.column_name::VARCHAR, 
			c1m4_rec.column_length::VARCHAR);
		display_len:=display_len+c1m4_rec.column_length+3;
		column_len[j]:=c1m4_rec.column_length;
		IF select_text IS NULL THEN
			select_text:=E'\n'||RPAD(c1m4_rec.column_name, c1m4_rec.column_length);
		ELSE
			select_text:=select_text||' | '||RPAD(c1m4_rec.column_name, c1m4_rec.column_length);
		END IF;
	END LOOP;
	select_text:=select_text||E'\n'||RPAD('-', display_len, '-');
--
-- Title
--
	IF title IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_method4', E'\n'||title||E'\n'||RPAD('-', LENGTH(title), '-'));
	END IF;
--
-- FETCH from temporary table as an array
--
	sql_stmt:='SELECT TRANSLATE(string_to_array(x.*::Text, '','')::Text, ''()'', '''')::text[] FROM '||temp_table||' AS x';
	PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_method4', 'SQL> %;', 
		select_stmt::VARCHAR);
	OPEN c2m4 FOR EXECUTE sql_stmt;
	LOOP
		FETCH c2m4 INTO c2m4_result_row;
		IF NOT FOUND THEN EXIT; END IF;
--  
		l_rows:=l_rows+1;
		select_text:=select_text||E'\n';
--
-- Process row array
--
		FOR i IN 1 .. j LOOP
			IF LENGTH(c2m4_result_row[i]) > column_len[i] THEN
				column_len[i]:=LENGTH(c2m4_result_row[i]);
			END IF;
--
			IF i = 1 THEN
				select_text:=select_text||RPAD(c2m4_result_row[i], column_len[i]);
			ELSE
				select_text:=select_text||' | '||RPAD(c2m4_result_row[i], column_len[i]);
			END IF;
		END LOOP;
	END LOOP;
	CLOSE c2m4;
--
	IF l_rows = 0 THEN
		select_text:=select_text||E'\n'||'(no rows)';
	ELSIF l_rows = 1 THEN
		select_text:=select_text||E'\n'||'('||l_rows::Text||' row)';
	ELSE
		select_text:=select_text||E'\n'||'('||l_rows::Text||' rows)';
	END IF;
--
-- Print SELECT
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_method4', select_text);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	IF l_rows IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_method4', 'Statement took: %', 
			took::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_method4', 'Statement took: %, proccessed % rows', 
			took::VARCHAR, l_rows::VARCHAR);
	END IF;
--
	PERFORM rif40_sql_pkg.rif40_ddl(drop_stmt);
--
	RETURN;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		error_message:='rif40_method4('''||coalesce(title::VARCHAR, '')||''') caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
--
-- This will not work if the cursor is open, but as it is a temporary table it will be deleted on session end
--
		IF drop_stmt IS NOT NULL THEN
			BEGIN
				PERFORM rif40_sql_pkg.rif40_ddl(drop_stmt);
			EXCEPTION
				WHEN others THEN
-- 
-- Not supported until 9.2
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='rif40_method4('''||coalesce(title::VARCHAR, '')||''') caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '11: %', error_message;
			END;
		END IF;
		RAISE;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_method4(VARCHAR, VARCHAR) IS 'Function: 	rif40_method4()
Parameters:	SQL SELECT statement
Returns:	Nothing
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement
';

--
-- Eof
