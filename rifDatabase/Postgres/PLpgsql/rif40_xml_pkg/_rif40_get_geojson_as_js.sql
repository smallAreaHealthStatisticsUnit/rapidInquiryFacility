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
-- Rapid Enquiry Facility (RIF) - Web services integration functions for middleware
--     				  Encapulate geoJSON in Javascript (internal common function)
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

--
-- Error codes assignment (see PLpgsql\Error_codes.txt):
--
-- rif40_xml_pkgt._rif40_get_geojson_as_js: 		50600 to 50799
--

CREATE OR REPLACE FUNCTION rif40_xml_pkg._rif40_get_geojson_as_js(
	l_geography VARCHAR, geolevel_view VARCHAR, geolevel_area_id_list VARCHAR[], l_expected_rows INTEGER)
RETURNS TABLE(js VARCHAR)
SECURITY INVOKER
AS $body$
/*

Function: 	_rif40_get_geojson_as_js()
Parameters:	Geography, geolevel_view, geolevel area ID list, l_expected_rows
Returns:	Text table
Description:	Get GeoJSON data as a Javascript variable. 

Execute a SQL statement as below and return a Javascript variable. Check rows are as expected.
EXPLIAN ANALYZE the query.

WITH a AS (
        SELECT 0 ord, 'var spatialData={ "type": "FeatureCollection","features": [ /- Start -/' js
        UNION
        SELECT ROW_NUMBER() OVER(ORDER BY area_id) ord,
               '{"type": "Feature","properties":'||
                        '{"area_id":"'||area_id||'",'||
                         '"name":"'||COALESCE(name, '')||'",'||
                         '"area":"'||COALESCE(area::Text, '')||'",'||
                         '"total_males":"'||COALESCE(total_males::Text, '')||'",'||
                         '"total_females":"'||COALESCE(total_females::Text, '')||'",'||
                         '"population_year":"'||COALESCE(LTRIM(TO_CHAR(population_year, '9990')), '')||'",'||
                         '"gid":"'||gid||'"},'||
                         '"geometry": '||optimised_geojson||'} /- '||
               row_number() over()||' : '||area_id||' : '||' : '||COALESCE(name, '')||' -/ ' AS js
          FROM t_rif40_sahsu_geometry
         WHERE geolevel_name = $1 /- <geolevel view> -/ AND area_id IN (SELECT UNNEST($2) /- <geolevel area id list> -/)
        UNION
        SELECT 999999 ord, ']} /- End: total expected rows: 59 -/' js
)
SELECT CASE WHEN ord BETWEEN 2 AND 999998 THEN ','||js ELSE js END::VARCHAR AS js
  FROM a
 ORDER BY ord;

Generates a Javascript variable encapulation geoJSON that looks (when prettified using http://jsbeautifier.org/) like:

var spatialData = {
    "type": "FeatureCollection",
    "features": [ /- Start -/ {
        "type": "Feature",
        "properties": {
            "area_id": "01.004.011100.1",
            "name": "Hambly LEVEL4(01.004.011100.1)",
            "area": "20.60",
            "total_males": "2540.00",
            "total_females": "2710.00",
            "population_year": "1996",
            "gid": "231"
        },
        "geometry": {
            "type": "MultiPolygon",
            "coordinates": [
                [
                    [
                        [-6.53098091, 55.01219818],
                        [-6.53098091, 55.01219818],
                        [-6.53096899, 55.0120511],

...

                        [-6.54086072, 55.0093148],
                        [-6.53129474, 55.01219097],
                        [-6.53098091, 55.01219818]
                    ]
                ]
            ]
        }
    } /- 1 : 01.004.011100.1 :  : Hambly LEVEL4(01.004.011100.1) -/ , {
        "type": "Feature",
        "properties": {
            "area_id": "01.004.011100.2",
            "name": "Hambly LEVEL4(01.004.011100.2)",
            "area": "17.70",
            "total_males": "8560.00",
            "total_females": "8678.00",
            "population_year": "1996",
            "gid": "233"
        },
        "geometry": {
            "type": "MultiPolygon",
            "coordinates": [
                [
                    [
                        [-6.5047827, 54.96467527],
                        [-6.5047827, 54.96467527],
                        [-6.50468401, 54.96445045],

...

                        [-6.45468136, 54.66232533],
                        [-6.45341036, 54.66048396],
                        [-6.45273179, 54.65880877]
                    ]
                ]
            ]
        }
    } /- 57 : 01.004.011900.3 :  : Hambly LEVEL4(01.004.011900.3) -/ ]
} /- End: total expected rows: 59 -/ ;

Validated with JSlint: http://www.javascriptlint.com/online_lint.php

*/
DECLARE
	c3_rec 			RECORD;
--
	sql_stmt 		VARCHAR;
	drop_stmt 		VARCHAR;
	i 			INTEGER:=0;
--
	explain_text 		VARCHAR;
	temp_table 		VARCHAR;
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- Create unique results temporary table
--
	temp_table:='l_'||REPLACE(rif40_sql_pkg.sys_context(NULL, 'AUDSID'), '.', '_');
--
-- Drop results temporary table
--
-- This could do with checking first to remove the notice:
-- psql:v4_0_rif40_sql_pkg.sql:3601: NOTICE:  table "l_7388_2456528_62637_130282_7388" does not exist, skipping
-- CONTEXT:  SQL statement "DROP TABLE IF EXISTS l_7388_2456528_62637_130282"
-- PL/pgSQL function "rif40_ddl" line 32 at EXECUTE statement
--
	drop_stmt:='DROP TABLE IF EXISTS '||temp_table;
	PERFORM rif40_sql_pkg.rif40_ddl(drop_stmt);
--
-- Create SQL statement
-- 
	sql_stmt:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE '||temp_table||' AS '||E'\n';
	sql_stmt:=sql_stmt||'WITH a AS ('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT 0 ord, ''var spatialData={ "type": "FeatureCollection","features": [ /* Start */'' js'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'UNION'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT ROW_NUMBER() OVER(ORDER BY area_id) ord,'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||'       ''{"type": "Feature","properties":''||'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'''{"area_id":"''||area_id||''",''||'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||' ''"name":"''||COALESCE(name, '''')||''",''||'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||' ''"area":"''||COALESCE(area::Text, '''')||''",''||'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||' ''"total_males":"''||COALESCE(total_males::Text, '''')||''",''||'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||' ''"total_females":"''||COALESCE(total_females::Text, '''')||''",''||'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||' ''"population_year":"''||COALESCE(LTRIM(TO_CHAR(population_year, ''9990'')), '''')||''",''||'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||' ''"gid":"''||gid||''"},''||'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||' ''"geometry": ''||optimised_geojson||''} /* ''||'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'       row_number() over()||'' : ''||area_id||'' : ''||'' : ''||COALESCE(name, '''')||'' */ '' AS js'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||'  FROM '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' WHERE geolevel_name = $1 /* <geolevel view> */ AND area_id IN (SELECT UNNEST($2) /* <geolevel area id list> */)'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||'UNION'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT 999999 ord, '']} /* End: total expected rows: '||l_expected_rows||' */'' js'||E'\n';
	sql_stmt:=sql_stmt||')'||E'\n';
	sql_stmt:=sql_stmt||'SELECT ord, CASE WHEN ord BETWEEN 2 AND 999998 THEN '',''||js ELSE js END::VARCHAR AS js'||E'\n';
	sql_stmt:=sql_stmt||'  FROM a'||E'\n';
	sql_stmt:=sql_stmt||' ORDER BY ord';
--
-- Execute SQL statement, returning JS
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_get_geojson_as_js', '[50210] Results EXPLAIN SQL> [using: %, %]'||E'\n'||'%;', 
		geolevel_view::VARCHAR, 
		geolevel_area_id_list::VARCHAR,
		sql_stmt::VARCHAR		/* SQL statement */);
--
-- Begin execution block to trap parse errors
--
	BEGIN
--
-- Create results temporary table, extract explain plan  using _rif40_geojson_explain_ddl() helper function.
-- This ensures the EXPLAIN PLAN output is a field called explain_line 
--
		sql_stmt:='SELECT explain_line FROM rif40_sql_pkg._rif40_geojson_explain_ddl('||quote_literal(sql_stmt)||', $1, $2)';
		FOR c3_rec IN EXECUTE sql_stmt USING geolevel_view, geolevel_area_id_list LOOP
			IF explain_text IS NULL THEN
				explain_text:=c3_rec.explain_line;
			ELSE
				explain_text:=explain_text||E'\n'||c3_rec.explain_line;
			END IF;
		END LOOP;
--
-- Now extract actual results from temp table
--
		sql_stmt:='SELECT js FROM '||temp_table||' ORDER BY ord';
		RETURN QUERY EXECUTE sql_stmt;
		GET DIAGNOSTICS i = ROW_COUNT;
	EXCEPTION
		WHEN others THEN
--
-- Print exception to INFO, re-raise
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='_rif40_get_geojson_as_js() caught: '||E'\n'||
				SQLERRM::VARCHAR||' in SQL> '||E'\n'||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
			RAISE INFO '50211: %', error_message;
--
			RAISE;
	END;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_get_geojson_as_js', '[50212] Results EXPLAIN PLAN.'||E'\n'||'%', explain_text::VARCHAR);
--
-- Check number of rows processed
--
	IF i IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50213, '_rif40_get_geojson_as_js', 'geography: %, SQL fetch returned NULL area rows.', 
			l_geography::VARCHAR			/* Geography */);
	ELSIF i = 2 THEN
		PERFORM rif40_log_pkg.rif40_error(-50214, '_rif40_get_geojson_as_js', 'geography: %, SQL fetch returned no area rows.', 
			l_geography::VARCHAR			/* Geography */);
	ELSIF i != l_expected_rows THEN
		PERFORM rif40_log_pkg.rif40_error(-50215, '_rif40_get_geojson_as_js', 
			'geography: %, SQL fetch returned wrong number of area rows, expecting: %, got: %.', 			
			l_geography::VARCHAR			/* Geography */, 
			l_expected_rows::VARCHAR		/* Expected rows */,
			i::VARCHAR				/* Actual */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_get_geojson_as_js', 
			'[50216] Geography: %, SQL fetch returned correct number of area rows, got: %.', 			
			l_geography::VARCHAR			/* Geography */, 
			i::VARCHAR				/* Actual */);
	END IF;
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg._rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR[], INTEGER) IS 'Function: 	_rif40_get_geojson_as_js()
Parameters:	Geography, geolevel_view, geolevel area ID list, l_expected_rows
Returns:	Text table
Description:	Get GeoJSON data as a Javascript variable. 

Execute a SQL statement as below and return a Javascript variable. Check rows are as expected.
EXPLIAN ANALYZE the query.

WITH a AS (
        SELECT 0 ord, ''var spatialData={ "type": "FeatureCollection","features": [ /* Start */'' js
        UNION
        SELECT ROW_NUMBER() OVER(ORDER BY area_id) ord,
               ''{"type": "Feature","properties":''||
                        ''{"area_id":"''||area_id||''",''||
                         ''"name":"''||COALESCE(name, '''')||''",''||
                         ''"area":"''||COALESCE(area::Text, '''')||''",''||
                         ''"total_males":"''||COALESCE(total_males::Text, '''')||''",''||
                         ''"total_females":"''||COALESCE(total_females::Text, '''')||''",''||
                         ''"population_year":"''||COALESCE(LTRIM(TO_CHAR(population_year, ''9990'')), '''')||''",''||
                         ''"gid":"''||gid||''"},''||
                         ''"geometry": ''||optimised_geojson||''} /* ''||
               row_number() over()||'' : ''||area_id||'' : ''||'' : ''||COALESCE(name, '''')||'' */ '' AS js
          FROM t_rif40_sahsu_geometry
         WHERE geolevel_name = $1 /* <geolevel view> */ AND area_id IN (SELECT UNNEST($2) /* <geolevel area id list> */)
        UNION
        SELECT 999999 ord, '']} /* End: total expected rows: 59 */'' js
)
SELECT CASE WHEN ord BETWEEN 2 AND 999998 THEN '',''||js ELSE js END::VARCHAR AS js
  FROM a
 ORDER BY ord;

Generates a Javascript variable encapulation geoJSON that looks (when prettified using http://jsbeautifier.org/) like:

var spatialData = {
    "type": "FeatureCollection",
    "features": [ /* Start */ {
        "type": "Feature",
        "properties": {
            "area_id": "01.004.011100.1",
            "name": "Hambly LEVEL4(01.004.011100.1)",
            "area": "20.60",
            "total_males": "2540.00",
            "total_females": "2710.00",
            "population_year": "1996",
            "gid": "231"
        },
        "geometry": {
            "type": "MultiPolygon",
            "coordinates": [
                [
                    [
                        [-6.53098091, 55.01219818],
                        [-6.53098091, 55.01219818],
                        [-6.53096899, 55.0120511],

...

                        [-6.54086072, 55.0093148],
                        [-6.53129474, 55.01219097],
                        [-6.53098091, 55.01219818]
                    ]
                ]
            ]
        }
    } /* 1 : 01.004.011100.1 :  : Hambly LEVEL4(01.004.011100.1) */ , {
        "type": "Feature",
        "properties": {
            "area_id": "01.004.011100.2",
            "name": "Hambly LEVEL4(01.004.011100.2)",
            "area": "17.70",
            "total_males": "8560.00",
            "total_females": "8678.00",
            "population_year": "1996",
            "gid": "233"
        },
        "geometry": {
            "type": "MultiPolygon",
            "coordinates": [
                [
                    [
                        [-6.5047827, 54.96467527],
                        [-6.5047827, 54.96467527],
                        [-6.50468401, 54.96445045],

...

                        [-6.45468136, 54.66232533],
                        [-6.45341036, 54.66048396],
                        [-6.45273179, 54.65880877]
                    ]
                ]
            ]
        }
    } /* 57 : 01.004.011900.3 :  : Hambly LEVEL4(01.004.011900.3) */ ]
} /* End: total expected rows: 59 */ ;

Validated with JSlint: http://www.javascriptlint.com/online_lint.php';

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_geojson_explain_ddl(sql_stmt VARCHAR, geolevel_view VARCHAR, geolevel_area_id_list VARCHAR[])
RETURNS TABLE(explain_line	TEXT)
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_geojson_explain_ddl()
Parameters:	SQL statement, geolevel view, geolevel area ID list
Returns: 	TABLE of explain_line
Description:	Coerce EXPLAIN output into a table with a known column. 
		Supports EXPLAIN and EXPLAIN ANALYZE for _rif40_get_geojson_as_js() ONLY.
 */
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager role
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-50299, '_rif40_geojson_explain_ddl', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	RETURN QUERY EXECUTE sql_stmt USING geolevel_view, geolevel_area_id_list;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_geojson_explain_ddl(VARCHAR, VARCHAR, VARCHAR[]) IS 'Function: 	_rif40_geojson_explain_ddl()
Parameters:	SQL statement, geolevel view, geolevel area ID list
Returns: 	TABLE of explain_line
Description:	Coerce EXPLAIN output into a table with a known column. 
		Supports EXPLAIN and EXPLAIN ANALYZE for _rif40_get_geojson_as_js() ONLY.';

--
-- No GRANTS - private function
--

--
-- Eof
