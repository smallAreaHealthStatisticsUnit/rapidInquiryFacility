-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/01/14 08:59:48 $
-- Type: Postgres PSQL script
-- $RCSfile: v4_0_rif40_dmp_pkg.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/PLpgsql/v4_0_rif40_dmp_pkg.sql,v $
-- $Revision: 1.1 $
-- $Id: v4_0_rif40_dmp_pkg.sql,v 1.1 2014/01/14 08:59:48 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) -  Create PG psql code (Data extract dump output help functions)
--
-- Copyright:
--
-- The RIF is free software; you can redistribute it and/or modify it under
-- the terms of the GNU General Public License as published by the Free
-- Software Foundation; either version 2, or (at your option) any later
-- version.
--
-- The RIF is distributed in the hope that it will be useful, but WITHOUT ANY
-- WARRANTY; without even the implied warranty of MERCHANTABILITY or
-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
-- for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this file; see the file LICENCE.  If not, write to:
--
-- UK Small Area Health Statistics Unit,
-- Dept. of Epidemiology and Biostatistics
-- Imperial College School of Medicine (St. Mary's Campus),
-- Norfolk Place,
-- Paddington,
-- London, W2 1PG
-- United Kingdom
--
-- The RIF uses Oracle 11g, PL/SQL, PostGres and PostGIS as part of its implementation.
--
-- Oracle11g, PL/SQL and Pro*C are trademarks of the Oracle Corporation.
--
-- All terms mentioned in this software and supporting documentation that are known to be trademarks
-- or service marks have been appropriately capitalised. Imperial College cannot attest to the accuracy
-- of this information. The use of a term in this software or supporting documentation should NOT be
-- regarded as affecting the validity of any trademark or service mark.
--
-- Summary of functions/procedures:
--
-- To be added
--
-- Error handling strategy:
--
-- Output and logging procedures do not HANDLE or PROPAGATE errors. This makes them safe to use
-- in package initialisation and NON recursive.
--
-- References:
--
-- 	None
--
-- Dependencies:
--
--	Packages: None
--
-- 	<This should include: packages, non packages procedures and functions, tables, views, objects>
--
-- Portability:
--
--	Linux, Windows 2003/2008, Oracle 11gR1
--
-- Limitations:
--
-- Change log:
--
-- $Log: v4_0_rif40_dmp_pkg.sql,v $
-- Revision 1.1  2014/01/14 08:59:48  peterh
--
-- Baseline prior to adding multipolygon support for simplification
--
-- Revision 1.5  2013/09/25 12:12:22  peterh
-- Baseline after 2x full clean builds at Postgres level
-- TODO.txt uptodate
--
-- Revision 1.4  2013/09/18 15:20:32  peterh
-- Checkin at end of 6 week RIF focus. Got as far as SAHSULAND run study to completion for observed only
--
-- Revision 1.3  2013/09/02 14:08:33  peterh
--
-- Baseline after full trigger implmentation
--
-- Revision 1.2  2013/03/14 17:35:39  peterh
-- Baseline for TX to laptop
--
--
\set ON_ERROR_STOP ON
\echo Creating PG psql code (Data extract dump output help functions)...

--
-- Drop code (to move to drop SQL script
--
DROP TYPE IF EXISTS rif40_dmp_pkg.DELIMITER_TYPE CASCADE;
DROP TYPE IF EXISTS rif40_dmp_pkg.LINE_TERMINATOR CASCADE;

CREATE TYPE rif40_dmp_pkg.DELIMITER_TYPE AS enum ('COMMA', 'PIPE');
CREATE TYPE rif40_dmp_pkg.LINE_TERMINATOR AS enum ('DOS', 'UNIX', 'MACOS');

CREATE OR REPLACE FUNCTION rif40_dmp_pkg.csv_dump(
	table_or_view_name 	VARCHAR, 
	delimiter		rif40_dmp_pkg.DELIMITER_TYPE 	DEFAULT 'COMMA',
	line_terminator		rif40_dmp_pkg.LINE_TERMINATOR 	DEFAULT 'DOS',
	with_header 		BOOLEAN DEFAULT TRUE,
	check_rows_and_columns 	BOOLEAN DEFAULT FALSE,
	batch_rows_at		INTEGER DEFAULT 100)
RETURNS SETOF VARCHAR
SECURITY INVOKER
AS $func$
/*
Function: 	csv_dump()
Parameters:	Table or view name, delimiter (COMMA [default] or PIPE), line terminator (DOS [default], UNIX or MAC), with header (TRUE[default]/FALSE),
                check rows and columns (TRUE/FALSE[defualt]), batch rows at (default 100 rows)
Returns:	Setof CSV rows. No line terminator on end of row (assumes output program puts on the correct type)
Description:	Dump table to CSV

Escaping is automatic as part of the array conversion mechanisms (geometry does need to be tested)
Datatype casting is as for the array interface.
Line terminators - COPY and \copy will escape them; fix with sed. As this function is designed for use with Java this is not an issue, e.g.

\copy (SELECT rif40_dmp_pkg.csv_dump('t_rif40_sahsu_geometry')) TO ../postgres/tmp/p_t_rif40_sahsu_geometry.csv WITH (FORMAT text) 
\! sed -ibak -e 's/\\r\\n/\r\n/g' ../postgres/tmp/p_t_rif40_sahsu_geometry.csv

or (for Unixen):

\! sed -ibak -e 's/\\n/\n/g' ../postgres/tmp/p_t_rif40_sahsu_geometry.csv

 a) Method 4 - Using RIF_ROW [probably FASTEST]

    Create <batch_rows_at> subcursors using MOD; union together results. No need for FOR loop

 b) Using a pg/psql FOR LOOP [probably 4xSLOWER]
 
    FETCH as an array, processiong <batch_rows_at> per output line:

SELECT TRANSLATE(array_to_string(string_to_array(x.*::Text, ','), ',', ' '), '()', '')::Text AS y 
  FROM rif40_tables AS x;

 Calling csv_escape() overhead was 25% (now deleted as it was not needed)

 Table: sahsuland_pop: statement took: 00:00:11.263311, proccessed 432960 rows in 4287 block(s) of 100 rows; 38439.85 rows/second; first block: 5933 bytes

 Conventiently postgres arrays use CSV format, so string_to_array(x.*::Text, ',') does all the CSV escaping for you (including line terminators)

Todo: check rows and columns
      Automatic block resize if default to optimal 10K

 */
DECLARE
	c1dcsv CURSOR(l_table_or_view_name VARCHAR) FOR			
		SELECT column_name
		  FROM information_schema.columns a, pg_tables b 
		 WHERE b.schemaname = a.table_schema
		   AND a.table_name = b.tablename
		   AND b.tablename  = l_table_or_view_name;
	c2dcsv REFCURSOR;
	c1dcsv_rec RECORD;
	c2dcsv_result_row	VARCHAR;
--
	sql_stmt	VARCHAR;
--
	stp 		TIMESTAMP WITH TIME ZONE;
	etp 		TIMESTAMP WITH TIME ZONE;
	took 		INTERVAL;
	rate 		NUMERIC;
	l_rows 		INTEGER:=0;
	l_columns 	INTEGER:=0;
	i 		INTEGER:=0;
	blocks 		INTEGER:=1;
	first_block_size INTEGER:=0;
--
	schema		VARCHAR;
	header_row	VARCHAR;
	line_term_text	VARCHAR;
	delimiter_text 	VARCHAR;
	has_rif_row	BOOLEAN:=FALSE;
--
	select_text 	VARCHAR;
--
	error_message 	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager role
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'csv_dump', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	IF table_or_view_name IS NULL THEN
		RETURN;
	END IF;
	stp:=clock_timestamp();
--
-- Find schema for object
--
	schema:=rif40_sql_pkg.rif40_object_resolve(table_or_view_name);
	IF schema IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'csv_dump', 'User % cannot access table/view/amterialized view % or % does not exist', 
			USER::VARCHAR,
			table_or_view_name::VARCHAR,
			table_or_view_name::VARCHAR);
	END IF;

--
-- Line terminators. Note COPY and \copy will escape them; fix with sed. As this function is designed for use with Java this is not an issue
--
	IF line_terminator = 'DOS' THEN
		line_term_text:=E'\r'||E'\n';
	ELSIF line_terminator = 'UNIX' THEN
		line_term_text:=E'\n';
	ELSIF line_terminator = 'MACOS' THEN
		line_term_text:=E'\r';
	END IF;

--
-- Process table header
--
	FOR c1dcsv_rec IN c1dcsv(table_or_view_name) LOOP
		l_columns:=l_columns+1;
		IF c1dcsv_rec.column_name = 'rif_row' THEN
			has_rif_row:=TRUE;
		END IF;
		IF with_header THEN
			IF l_columns = 1 THEN
				header_row:=UPPER(c1dcsv_rec.column_name);
			ELSE
				header_row:=header_row||','||UPPER(c1dcsv_rec.column_name);
			END IF;
		END IF;
	END LOOP;
	IF with_header THEN
		RETURN NEXT header_row;
	END IF;
--
-- Delimiters
--
	IF delimiter = 'COMMA' THEN
		delimiter_text:=',';
	ELSIF delimiter = 'PIPE' THEN
		delimiter_text:='|';
	END IF;

--
-- a) Method 4 - Using RIF_ROW [probably FASTEST]
--
-- Create <batch_rows_at> subcursors using MOD; union together results. No need for FOR loop
--
-- b) Using a pg/psql FOR LOOP [probably 4xSLOWER]
-- 
-- FETCH as an array, processiong <batch_rows_at> per output line:
--
-- SELECT TRANSLATE(
--		array_to_string(
--			string_to_array(x.*::Text, ',' 			/* separate array elements with , */), 
--				',' /* delimiter charater */, ' ' 	/* Nulls to space */)
--			, '()', '' 					/* Remove array () */
--		)||E'\n'::Text  					/* Add line terminator */
--	   	AS y 
--   FROM rif40_tables AS x;
--
-- Calling csv_escape() overhead was 25% (now deleted as it was not needed)
--
-- Table: sahsuland_pop: statement took: 00:00:11.263311, proccessed 432960 rows in 4287 block(s) of 100 rows; 38439.85 rows/second; first block: 5933 bytes
--
-- Conventiently postgres arrays use CSV format, so string_to_array(x.*::Text, ',') does all the CSV escaping for you (including line terminators)
--
-- Needs to handle ARRAY ()'s if they are in the data (i.e. ignore them)
--
	sql_stmt:='SELECT TRANSLATE(array_to_string(string_to_array(x.*::Text /* CSV escape */, '','' /* separate array elements with , */), '||
		''''||delimiter_text||''' /* delimiter charater */, '' '' /* Nulls to space */), ''()'', '''' /* Remove array () */)||'''||
		line_term_text||'''::Text /* Add line terminator */ AS y FROM '||
		table_or_view_name||' AS x';
	PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'csv_dump', 'SQL> %;', 
		sql_stmt::VARCHAR);
	OPEN c2dcsv FOR EXECUTE sql_stmt;
	LOOP
		FETCH c2dcsv INTO c2dcsv_result_row;
		IF NOT FOUND THEN EXIT; END IF;
--  
		l_rows:=l_rows+1;
		i:=i+1;
--
-- Process row array
--
		IF i = 1 THEN
			select_text:=c2dcsv_result_row;
		ELSE
			select_text:=select_text||c2dcsv_result_row;
		END IF;
--
		IF i > batch_rows_at THEN
			i:=0;
			IF blocks = 1 THEN
				first_block_size=length(select_text);
			END IF;
			blocks:=blocks+1;
			RETURN NEXT trim(trailing line_term_text from select_text); /* Remove last CRLF */
			select_text:=NULL;
		END IF;
	END LOOP;
	CLOSE c2dcsv;
	IF blocks = 1 THEN
		first_block_size=length(select_text);		/* This may be used to tune <batch_rows_at> in future if set to default value */
	END IF;
--
	RETURN NEXT trim(trailing line_term_text from select_text); /* Remove last CRLF */
--
-- Diagnostics
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	rate:=ROUND((l_rows/(EXTRACT(EPOCH FROM took) /* convert to seconds */)::NUMERIC), 2);
	IF l_rows IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'csv_dump', 'Table: %: statement took: %, proccessed no rows', 
			table_or_view_name::VARCHAR,
			took::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'csv_dump', 
			'Table: %: statement took: %, proccessed % rows in % block(s) of % rows; % rows/second; first block: % bytes', 
			table_or_view_name::VARCHAR,
			took::VARCHAR, 
			l_rows::VARCHAR, 
			blocks::VARCHAR,
			batch_rows_at::VARCHAR,
			rate::VARCHAR,
			first_block_size::VARCHAR);
	END IF;
--
	RETURN;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
--		GET STACKED DIAGNOTICS v_detail = PG_EXCEPTION_DETAIL;
		error_message:='csv_dump() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
--
		RAISE;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_dmp_pkg.csv_dump(VARCHAR, rif40_dmp_pkg.DELIMITER_TYPE, rif40_dmp_pkg.LINE_TERMINATOR, BOOLEAN, BOOLEAN, INTEGER) IS 'Function: 	csv_dump()
Function: 	csv_dump()
Parameters:	Table or view name, delimiter (COMMA [default] or PIPE), line terminator (DOS [default], UNIX or MAC), with header (TRUE[default]/FALSE),
                check rows and columns (TRUE/FALSE[defualt]), batch rows at (default 100 rows)
Returns:	Setof CSV rows. No line terminator on end of row (assumes output program puts on the correct type)
Description:	Dump table to CSV

Escaping is automatic as part of the array conversion mechanisms (geometry does need to be tested)
Datatype casting is as for the array interface.
Line terminators - COPY and \copy will escape them; fix with sed. As this function is designed for use with Java this is not an issue, e.g.

\copy (SELECT rif40_dmp_pkg.csv_dump(''t_rif40_sahsu_geometry'')) TO ../postgres/tmp/p_t_rif40_sahsu_geometry.csv WITH (FORMAT text) 
\! sed -ibak -e ''s/\\r\\n/\r\n/g'' ../postgres/tmp/p_t_rif40_sahsu_geometry.csv

or (for Unixen):

\! sed -ibak -e ''s/\\n/\n/g'' ../postgres/tmp/p_t_rif40_sahsu_geometry.csv

 a) Method 4 - Using RIF_ROW [probably FASTEST]

    Create <batch_rows_at> subcursors using MOD; union together results. No need for FOR loop

 b) Using a pg/psql FOR LOOP [probably 4xSLOWER]
 
    FETCH as an array, processiong <batch_rows_at> per output line:

SELECT TRANSLATE(array_to_string(string_to_array(x.*::Text, '',''), '','', '' ''), ''()'', '''')::Text AS y 
  FROM rif40_tables AS x;

 Calling csv_escape() overhead was 25% (now deleted as it was not needed)

 Table: sahsuland_pop: statement took: 00:00:11.263311, proccessed 432960 rows in 4287 block(s) of 100 rows; 38439.85 rows/second; first block: 5933 bytes

Conventiently postgres arrays use CSV format, so string_to_array(x.*::Text, '','') does all the CSV escaping for you (including line terminators)

Todo: check rows and columns
      Automatic block resize if default to optimal 10K';

GRANT EXECUTE ON FUNCTION rif40_dmp_pkg.csv_dump(VARCHAR, rif40_dmp_pkg.DELIMITER_TYPE, rif40_dmp_pkg.LINE_TERMINATOR, BOOLEAN, BOOLEAN, INTEGER) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_dmp_pkg.csv_dump(VARCHAR, rif40_dmp_pkg.DELIMITER_TYPE, rif40_dmp_pkg.LINE_TERMINATOR, BOOLEAN, BOOLEAN, INTEGER) TO rif_manager;

--
-- Comment out to test. These objects do not exist when the script is run in the DB build
--
\q

-- 
SELECT * FROM rif40_geographies;
SELECT rif40_dmp_pkg.csv_dump('rif40_geographies' /* Table name */);

\copy (SELECT rif40_dmp_pkg.csv_dump('rif40_geographies' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 100 /* batch_rows_at */)) TO ../postgres/tmp/p_rif40_geographies.csv WITH (FORMAT text) 
\! sed -ibak -e 's/\\r\\n/\r\n/g' ../postgres/tmp/p_rif40_geographies.csv
\! sed -ibak -e 's/\\n/\n/g' ../postgres/tmp/p_rif40_geographies.csv

\copy (SELECT rif40_dmp_pkg.csv_dump('t_rif40_sahsu_geometry' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 100 /* batch_rows_at */)) TO ../postgres/tmp/p_t_rif40_sahsu_geometry.csv WITH (FORMAT text) 
\! sed -ibak -e 's/\\r\\n/\r\n/g' ../postgres/tmp/p_t_rif40_sahsu_geometry.csv
\! sed -ibak -e 's/\\n/\n/g' ../postgres/tmp/p_t_rif40_sahsu_geometry.csv

--
-- Block size testing - up to 200 good with sahusland_pop. 100 seems a sane default
--
-- 
\timing on
\set VERBOSITY terse
\echo Block size 50 
\copy (SELECT rif40_dmp_pkg.csv_dump('sahsuland_pop' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 50 /* batch_rows_at */)) TO ../postgres/tmp/p_sahsuland_pop.csv WITH (FORMAT text) 
\echo Block size 100 
\copy (SELECT rif40_dmp_pkg.csv_dump('sahsuland_pop' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 100 /* batch_rows_at */)) TO ../postgres/tmp/p_sahsuland_pop.csv WITH (FORMAT text) 
\echo Block size 200 
\copy (SELECT rif40_dmp_pkg.csv_dump('sahsuland_pop' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 200 /* batch_rows_at */)) TO ../postgres/tmp/p_sahsuland_pop.csv WITH (FORMAT text) 
\echo Block size 300 
\copy (SELECT rif40_dmp_pkg.csv_dump('sahsuland_pop' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 300 /* batch_rows_at */)) TO ../postgres/tmp/p_sahsuland_pop.csv WITH (FORMAT text) 
\echo Block size 400 
\copy (SELECT rif40_dmp_pkg.csv_dump('sahsuland_pop' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 400 /* batch_rows_at */)) TO ../postgres/tmp/p_sahsuland_pop.csv WITH (FORMAT text) 
\echo Block size 500 
\copy (SELECT rif40_dmp_pkg.csv_dump('sahsuland_pop' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 500 /* batch_rows_at */)) TO ../postgres/tmp/p_sahsuland_pop.csv WITH (FORMAT text) 
\echo Block size 1000 
\copy (SELECT rif40_dmp_pkg.csv_dump('sahsuland_pop' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 1000 /* batch_rows_at */)) TO ../postgres/tmp/p_sahsuland_pop.csv WITH (FORMAT text) 
\! sed -ibak -e 's/\\r\\n/\r\n/g' ../postgres/tmp/p_sahsuland_pop.csv
\! sed -ibak -e 's/\\n/\n/g' ../postgres/tmp/p_sahsuland_pop.csv
\copy (SELECT * FROM sahsuland_pop) TO ../postgres/tmp/p2_sahsuland_pop.csv WITH (FORMAT csv) 

\echo Block size 100 
\copy (SELECT rif40_dmp_pkg.csv_dump('sahsuland_cancer' /* Table name */, 'COMMA' /* delimiter */, 'UNIX' /* line terminator */, TRUE /* with header */, FALSE /* check rows and columns */, 100 /* batch_rows_at */)) TO ../postgres/tmp//p_sahsuland_cancer.csv WITH (FORMAT text) 
\! sed -ibak -e 's/\\r\\n/\r\n/g' ../postgres/tmp/p_sahsuland_cancer.csv
\! sed -ibak -e 's/\\n/\n/g' ../postgres/tmp/p_sahsuland_cancer.csv
\set VERBOSITY default

\copy (SELECT * FROM sahsuland_cancer) TO ../postgres/tmp/p2_sahsuland_cancer.csv WITH (FORMAT csv) 

--
-- Eof
