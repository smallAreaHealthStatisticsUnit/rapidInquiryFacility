-- ************************************************************************
-- *
-- * THIS SCRIPT MAY BE EDITED - NO NEED TO USE ALTER SCRIPTS
-- *
-- ************************************************************************
--
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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_table_diff() Validate 2 tables are the same		
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
--rif40_table_diff:									71000 to 71050

-- DROP FUNCTION rif40_table_diff(character varying,character varying,character varying,character varying[],character varying[]);
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_table_diff(test_tag VARCHAR, table_1 VARCHAR, table_2 VARCHAR, 
		table_1_columns VARCHAR[] DEFAULT NULL, table_2_columns VARCHAR[] DEFAULT NULL)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_table_diff()
Parameters: 	test tag, table 1, table 2, table 1 column list; default: NULL means ALL, table 2 column list; default: NULL means ALL
Returns: 		Nothing
Description:	Validate 2 tables are the same	
 */
DECLARE
	c1tdiff CURSOR(l_table_name VARCHAR) FOR
		SELECT t.*, has_table_privilege(LOWER(l_table_name), 'SELECT') AS can_select
		  FROM pg_tables t
		 WHERE tablename  = LOWER(l_table_name);
	c2tdiff CURSOR(l_table_name VARCHAR, l_table_column VARCHAR) FOR
		SELECT c.*, has_column_privilege(LOWER(l_table_name), LOWER(l_table_column), 'SELECT') AS can_select
		  FROM information_schema.columns
		 WHERE tablename  = LOWER(l_table_name)
		   AND columnname = LOWER(l_table_column);
	c3tdiff 		REFCURSOR;
--
	c1a_rec 		RECORD;
	c1b_rec 		RECORD;	
	c2_rec			RECORD;
	c3_rec			RECORD;
--
	l_column		VARCHAR;
	column_list_1	VARCHAR;
	column_list_2	VARCHAR;	
--
	sql_stmt		VARCHAR;
	errors			INTEGER:=0;
--
	error_message 	VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
	IF test_tag IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(71000, 'rif40_table_diff', 'NULL test tag');
	END IF;
--
-- Check tables exist
--
	OPEN c1tdiff(table_1);
	FETCH c1tdiff INTO c1a_rec;
	CLOSE c1tdiff;
	IF c1a_rec.tablename IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(71001, 'rif40_table_diff', '[%] Cannot find table/view 1: %', 
			test_tag::VARCHAR,
			c1_rec.tablename::VARCHAR);
	ELSIF c1a_rec.can_select = FALSE THEN
		PERFORM rif40_log_pkg.rif40_error(71002, 'rif40_table_diff', '[%] Cannot access table/view 1: %.%', 
			test_tag::VARCHAR,
			c1a_rec.tableowner::VARCHAR,
			c1a_rec.tablename::VARCHAR);
	END IF;
	OPEN c1tdiff(table_2);
	FETCH c1tdiff INTO c1b_rec;
	CLOSE c1tdiff;
	IF c1b_rec.tablename IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(71003, 'rif40_table_diff', '[%] Cannot find table/view 2: %', 
			test_tag::VARCHAR,
			c1b_rec.tablename::VARCHAR);
	ELSIF c1b_rec.can_select = FALSE THEN
		PERFORM rif40_log_pkg.rif40_error(71004, 'rif40_table_diff', '[%] Cannot access table/view 2: %.%', 
			test_tag::VARCHAR,
			c1b_rec.tableowner::VARCHAR,
			c1b_rec.tablename::VARCHAR);
	END IF;
-- 
-- Check columns exist
--
	IF table_1_columns IS NULL THEN
		column_list_1:='*';
	ELSE
		FOREACH l_column IN ARRAY table_1_columns LOOP
			OPEN c2tdiff(table_1, l_column);
			FETCH c2tdiff INTO c2_rec;
			CLOSE c2tdiff;
			IF c2_rec.tablename IS NULL THEN
				PERFORM rif40_log_pkg.rif40_error(71005, 'rif40_table_diff', '[%] Cannot find table/view 1 column: %.%', 
					test_tag::VARCHAR,
					c1a_rec.tablename::VARCHAR,
					l_column::VARCHAR);
			ELSIF c2_rec.can_select = FALSE THEN
				PERFORM rif40_log_pkg.rif40_error(71006, 'rif40_table_diff', '[%] Cannot access table/view 1 column: %.%', 
					test_tag::VARCHAR,
					c2_rec.tablename::VARCHAR,
					c2_rec.columnname::VARCHAR);
			ELSIF column_list_1 IS NULL THEN
				column_list_1:=quote_ident(c2_rec.columnname);
			ELSE
				column_list_1:=column_list_1||', '||quote_ident(c2_rec.columnname);
			END IF;
		END LOOP;
	END IF;
	IF table_2_columns IS NULL THEN
		column_list_2:='*';
	ELSE
		FOREACH l_column IN ARRAY table_2_columns LOOP
			OPEN c2tdiff(table_2, l_column);
			FETCH c2tdiff INTO c2_rec;
			CLOSE c2tdiff;
			IF c2_rec.tablename IS NULL THEN
				PERFORM rif40_log_pkg.rif40_error(71007, 'rif40_table_diff', '[%] Cannot find table/view 2 column: %.%', 
					test_tag::VARCHAR,
					c1b_rec.tablename::VARCHAR,
					l_column::VARCHAR);
			ELSIF c2_rec.can_select = FALSE THEN
				PERFORM rif40_log_pkg.rif40_error(71008, 'rif40_table_diff', '[%] Cannot access table/view 2 column: %.%', 
					test_tag::VARCHAR,
					c2_rec.tablename::VARCHAR,
					c2_rec.columnname::VARCHAR);
			ELSIF column_list_2 IS NULL THEN
				column_list_2:=quote_ident(c2_rec.columnname);
			ELSE
				column_list_2:=column_list_1||', '||quote_ident(c2_rec.columnname);
			END IF;
		END LOOP;
	END IF;
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_table_diff', '[71009]: [%] Table 1 %.% column list (%)', 
		test_tag::VARCHAR,
		c1a_rec.tableowner::VARCHAR,
		c1a_rec.tablename::VARCHAR,
		column_list_1::VARCHAR);
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_table_diff', '[71010]: [%] Table 2 %.% column list (%)', 
		test_tag::VARCHAR,
		c1b_rec.tableowner::VARCHAR,
		c1b_rec.tablename::VARCHAR,
		column_list_2::VARCHAR);
--
-- Create SQL statement
--
	sql_stmt:='WITH a_1_minus_2 AS ('||CHR(10)||
'	SELECT '||column_list_1||CHR(10)||
'	  FROM '||quote_ident(LOWER(table_1))||CHR(10)||
'	EXCEPT'||CHR(10)||
'	SELECT '||column_list_2||CHR(10)||
'	  FROM '||quote_ident(LOWER(table_2))||CHR(10)||
'),'||CHR(10)||
'b_2_minus_1 AS ('||CHR(10)||
'	SELECT '||column_list_2||CHR(10)||
'	  FROM '||quote_ident(LOWER(table_2))||CHR(10)||
'	EXCEPT'||CHR(10)||
'	SELECT '||column_list_1||CHR(10)||
'	  FROM '||quote_ident(LOWER(table_1))||CHR(10)||
'), a AS ('||CHR(10)||
'	SELECT COUNT(a.*) AS a_1_minus_2_total'||CHR(10)||
'     FROM a_1_minus_2 a'||CHR(10)||
'), b AS ('||CHR(10)||
'	SELECT COUNT(b.*) AS b_2_minus_1_total'||CHR(10)||
'     FROM b_2_minus_1 b'||CHR(10)||
')'||CHR(10)||
'SELECT a.a_1_minus_2_total, b.b_2_minus_1_total'||CHR(10)||
'  FROM a, b';
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_table_diff', '[71011]: [%] SQL> %;', 
		test_tag::VARCHAR,
		sql_stmt::VARCHAR);
--
	BEGIN
		OPEN c3tdiff FOR EXECUTE sql_stmt;
		FETCH c3tdiff INTO c3_rec;
		CLOSE c3tdiff;
	EXCEPTION
		WHEN others THEN
--
-- Print exception to INFO, re-raise
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='rif40_table_diff() caught: '||E'\n'||
				SQLERRM::VARCHAR||', detail: '||v_detail::VARCHAR;
			RAISE INFO '71012: [%] %', test_tag, error_message;
--
			RAISE;
	END;
	IF c3_rec.a_1_minus_2_total > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_table_diff', '[71010]: [%] Table 1 %.% has % extra records not in 2 %.%', 
			test_tag::VARCHAR,
			c1a_rec.tableowner::VARCHAR,
			c1a_rec.tablename::VARCHAR,
			c3_rec.a_1_minus_2_total::VARCHAR,
			c1b_rec.tableowner::VARCHAR,
			c1b_rec.tablename::VARCHAR);
		errors:=errors+1;
	ELSIF c3_rec.b_2_minus_1_total > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_table_diff', '[71010]: [%] Table 1 %.% has % missing records that are in 2 %.%', 
			test_tag::VARCHAR,
			c1a_rec.tableowner::VARCHAR,
			c1a_rec.tablename::VARCHAR,
			c3_rec.b_2_minus_1_total::VARCHAR,
			c1b_rec.tableowner::VARCHAR,
			c1b_rec.tablename::VARCHAR);
		errors:=errors+1;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_table_diff', '[71012]: [%] Tables 1 %.% AND 2 %.% are the same', 
			test_tag::VARCHAR,
			c1a_rec.tableowner::VARCHAR,
			c1a_rec.tablename::VARCHAR,
			c1b_rec.tableowner::VARCHAR,
			c1b_rec.tablename::VARCHAR);
	END IF;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_table_diff(VARCHAR, VARCHAR, VARCHAR, VARCHAR[], VARCHAR[]) IS 'Function: 		rif40_table_diff()
Parameters: 	test tag, table 1, table 2, table 1 column list; default: NULL means ALL, table 2 column list; default: NULL means ALL
Returns: 		Nothing
Description:	Validate 2 tables are the same';

GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_table_diff(VARCHAR, VARCHAR, VARCHAR, VARCHAR[], VARCHAR[]) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_table_diff(VARCHAR, VARCHAR, VARCHAR, VARCHAR[], VARCHAR[]) TO rif_manager;

--
-- Eof
