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
--     				  Encapulate geoJSON in Javascript
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
-- rif40_xml_pkgt.rif40_get_geojson_as_js: 		50200 to 50399
--
CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(
	l_geography 		VARCHAR, 
	geolevel_view 		VARCHAR, 
	geolevel_area 		VARCHAR, 
	geolevel_area_id	VARCHAR, 
	return_one_row 		BOOLEAN DEFAULT TRUE)
RETURNS SETOF text 
SECURITY INVOKER
AS $body$
/*

Function: 	rif40_get_geojson_as_js()
Parameters:	Geography, geolevel_view, geolevel_area, geolevel_area_id, return one row (TRUE/FALSE)
Returns:	Text table [1 or more rows dependent on return_one_row]
Description:	Get GeoJSON data as a Javascript variable. 
		For the disease mapping selection dialog phrase:
		"View the <geolevel view> (e.g. GOR2001) of <geolevel area> (e.g. London) and select at <geolevel select> (e.g. LADUA2001) level."
		1 row means 1 row, no CRLFs etc!
		If return_one_row is false then a header, 1 rows/area_id and a footer is returned so the Javascript is easier to read

Calls: _rif40_get_geojson_as_js to extract the data using the following SQL:

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
	c1geojson1 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c2geojson1 CURSOR(l_geography VARCHAR, l_geolevel VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel;
	c2b_geojson1 CURSOR(l_geography VARCHAR, l_geolevel VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel;
	c3geojson1 REFCURSOR;
	c5geojson1 CURSOR(l_geography VARCHAR, l_geolevel_view VARCHAR, l_geolevel_area_id_list VARCHAR[], l_expected_rows INTEGER) FOR
		WITH a AS (
			SELECT js FROM rif40_xml_pkg._rif40_get_geojson_as_js(
						l_geography, l_geolevel_view, l_geolevel_area_id_list, l_expected_rows)
		)
		SELECT ARRAY_AGG(js) AS js
		  FROM a;
--
	c1_rec rif40_geographies%ROWTYPE;
	c2a_rec rif40_geolevels%ROWTYPE;
	c2b_rec rif40_geolevels%ROWTYPE;
	c3_rec RECORD;
	c5_rec RECORD;
--
	sql_stmt 		VARCHAR;
	i 			INTEGER:=0;
	geolevel_area_id_list	VARCHAR[];
	js			VARCHAR[];
	js_text			VARCHAR;
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-50200, 'rif40_get_geojson_as_js', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Test geography
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50201, 'rif40_get_geojson_as_js', 'NULL geography parameter');
	END IF;	
--
	OPEN c1geojson1(l_geography);
	FETCH c1geojson1 INTO c1_rec;
	CLOSE c1geojson1;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50202, 'rif40_get_geojson_as_js', 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */);
	END IF;	
--
-- Test <geolevel view/area> exist
--
	OPEN c2geojson1(l_geography, geolevel_view);
	FETCH c2geojson1 INTO c2a_rec;
	CLOSE c2geojson1;
--
	IF c2a_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50203, 'rif40_get_geojson_as_js', 'geography: %, <geoevel view> %: not found', 
			l_geography::VARCHAR	/* Geography */, 
			geolevel_view::VARCHAR	/* geolevel view */);
	END IF;	
--
	OPEN c2b_geojson1(l_geography, geolevel_area);
	FETCH c2b_geojson1 INTO c2b_rec;
	CLOSE c2b_geojson1;
--
	IF c2b_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50204, 'rif40_get_geojson_as_js', 'geography: %, <geoevel area> %: not found', 
			l_geography::VARCHAR	/* Geography */, 
			geolevel_area::VARCHAR	/* Geoelvel area */);
	END IF;	
--
-- Test <geoevel area> resolution is higher than <geoevel view> resolution
-- e.g.
-- get_geojson_as_js() geography: EW01, <geoevel area> GOR2001 resolution (3) is NOT higher than <geoevel view> OA2001 resolution (7)
--
	IF c2a_rec.geolevel_id /* <geoevel view> */ < c2b_rec.geolevel_id /* <geoevel area> */ THEN
		PERFORM rif40_log_pkg.rif40_error(-50205, 'rif40_get_geojson_as_js', 
			'geography: %, <geoevel area> % resolution (%) higher than <geoevel view> % resolution (%)', 			
			l_geography::VARCHAR		/* Geography */, 
			geolevel_area::VARCHAR		/* Geolevel area */, 
			c2b_rec.geolevel_id::VARCHAR, 	/* Geolevel area ID (resolution) */
			geolevel_view::VARCHAR		/* geolevel view */, c2a_rec.geolevel_id::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_as_js', 
			'[50206] Geography: %, <geoevel area> % resolution (%) is NOT higher than <geoevel view> % resolution (%)', 		
			l_geography::VARCHAR		/* Geography */, 
			geolevel_area::VARCHAR		/* Geolevel area */, 
			c2b_rec.geolevel_id::VARCHAR	/* Geolevel area ID (resolution) */, 
			geolevel_view::VARCHAR		/* geolevel view */, 
			c2a_rec.geolevel_id::VARCHAR	/* Geolevel view ID (resolution) */);
	END IF;
--
-- Test  <geolevel area id> is valid
--
/* 
SELECT COUNT(DISTINCT(level4)) AS total, 
       ARRAY_AGG(level4) AS area_id_list
  FROM sahsuland_geography
 WHERE level2 = '01.004';
*/
	sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(LOWER(geolevel_view))||')) AS total,'||E'\n'||
		  '       ARRAY_AGG('||quote_ident(LOWER(geolevel_view))||') AS area_id_list'||E'\n'||
		  '  FROM '||quote_ident(LOWER(c1_rec.hierarchytable))||E'\n'||
		  ' WHERE '||quote_ident(LOWER(geolevel_area))||' = $1';
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_as_js', '[50207] <geolevel area id> SQL> [using %]'||E'\n'||'%;',
		geolevel_area_id::VARCHAR	/* Geolevel area ID */,
		sql_stmt::VARCHAR		/* SQL statement */);
		BEGIN
			OPEN c3geojson1 FOR EXECUTE sql_stmt USING geolevel_area_id;
			FETCH c3geojson1 INTO c3_rec;
			CLOSE c3geojson1;
		EXCEPTION
		WHEN others THEN
--
-- Print exception to INFO, re-raise
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='rif40_get_geojson_as_js() caught: '||E'\n'||
				SQLERRM::VARCHAR||' in SQL> '||E'\n'||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
			RAISE INFO '50208: %', error_message;
--
			RAISE;
	END;
	IF c3_rec.total = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-50209, 'rif40_get_geojson_as_js', 
			'Geography: %, <geoevel area> % id % does not exist in hierarchy table: %', 			
			l_geography::VARCHAR			/* Geography */, 
			geolevel_area::VARCHAR			/* Geoelvel area */,  
			geolevel_area_id::VARCHAR		/* Geoelvel area ID */, 
			LOWER(c1_rec.hierarchytable)::VARCHAR	/* Hierarchy table */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_as_js', 
			'[50210] Geography: %, <geoevel area> % id % will return: % area_id', 		
			l_geography::VARCHAR		/* Geography */, 
			geolevel_area::VARCHAR		/* Geoelvel area */, 
			geolevel_area_id::VARCHAR	/* Geoelvel area ID */, 
			c3_rec.total::VARCHAR		/* Number of area_ids */);
	END IF;
--
-- List of area IDs to dump
--
	geolevel_area_id_list:=c3_rec.area_id_list; 
--
-- Call  rif40_xml_pkg._rif40_get_geojson_as_js()
--
	OPEN c5geojson1(l_geography, geolevel_view, geolevel_area_id_list, (c3_rec.total+2)::INTEGER /* l_expected_rows */);
	FETCH c5geojson1 INTO c5_rec;
	CLOSE c5geojson1;
--
-- Return as one row
--
	IF return_one_row THEN
		FOR i IN array_lower(c5_rec.js, 1) .. array_upper(c5_rec.js, 1) LOOP
			IF i = 1 THEN
				js_text:=c5_rec.js[i];
			ELSE
				js_text:=js_text||c5_rec.js[i]; /* Do not add CRLF or CR - it irritates psql copy command */
			END IF;
--			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_as_js', '[50200] i: %, length c5_rec.js[i]: %, js_text: %', 
--				i::VARCHAR, LENGTH(c5_rec.js[i])::VARCHAR, LENGTH(js_text)::VARCHAR);
		END LOOP;
		RETURN NEXT js_text;
	ELSE
--
-- Multi row return
--
		FOR i IN array_lower(c5_rec.js, 1) .. array_upper(c5_rec.js, 1) LOOP
			RETURN NEXT c5_rec.js[i];
		END LOOP;
	END IF;
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR, BOOLEAN) IS 'Function: 	rif40_get_geojson_as_js()
Parameters:	Geography, geolevel_view, geolevel_area, geolevel_area_id, return one row (TRUE/FALSE)
Returns:	Text table [1 or more rows dependent on return_one_row]
Description:	Get GeoJSON data as a Javascript variable. 
		For the disease mapping selection dialog phrase:
		"View the <geolevel view> (e.g. GOR2001) of <geolevel area> (e.g. London) and select at <geolevel select> (e.g. LADUA2001) level."
		1 row means 1 row, no CRLFs etc!
		If return_one_row is false then a header, 1 rows/area_id and a footer is returned so the Javascript is easier to read

Calls: _rif40_get_geojson_as_js to extract the data using the following SQL:

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

GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR, BOOLEAN) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR, BOOLEAN) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR, BOOLEAN) TO rif40;

--
-- Eof
