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
-- rif40_xml_pkgt.rif40_get_geojson_as_js: 		50200 to 50199
--
CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(l_geography VARCHAR, geolevel_view VARCHAR, geolevel_area VARCHAR, geolevel_area_id VARCHAR)
RETURNS SETOF text 
SECURITY INVOKER
AS $body$
/*

Function: 	rif40_get_geojson_as_js()
Parameters:	Geography, geolevel_view, geolevel_area, geolevel_area_id
Returns:	Text table
Description:	Get GeoJSON data as a Javascript variable. 
		For the disease mapping selection dialog phrase:
		"View the <geolevel view> (e.g. GOR2001) of <geolevel area> (e.g. London) and select at <geolevel select> (e.g. LADUA2001) level."

Execute a SQL statement like and return JS text:

WITH a AS (
        SELECT 0 ord, ''var spatialData={ "type": "FeatureCollection","features": [ /- Start -/'' js
        UNION
        SELECT row_number() over() ord,
               ''{"type": "Feature","properties":{"area_id":"''||area_id||''","name":"''||COALESCE(name, '')||''"},"geometry": ''||optimised_geojson||''} -/ ''||row_number() over()||'' : ''||area_id||'' : ''||'' : ''||COALESCE(name, '')||'' -/'' AS js
          FROM t_rif40_ew01_geometry
         WHERE geolevel_name = ''LADUA2001'' /- <geolevel view> -/ AND area_id IN (
                SELECT DISTINCT ladua2001 /- <geolevel view> -/
                  FROM ew2001_geography /- Heirarchy table /- WHERE gor2001 /- <geolevel area> -/ = ''H'' /- <geolevel area id> -/)
                UNION
                SELECT 999999 ord, '']} /- End -/'' js
)
SELECT CASE WHEN ord BETWEEN 2 AND 999998 THEN '',''||js ELSE js END AS js
  FROM a
 ORDER BY ord LIMIT 2;'

*/
DECLARE
	c1 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c2 CURSOR(l_geography VARCHAR, l_geolevel VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel;
	c2b CURSOR(l_geography VARCHAR, l_geolevel VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel;
	c3 REFCURSOR;
--
	c1_rec rif40_geographies%ROWTYPE;
	c2a_rec rif40_geolevels%ROWTYPE;
	c2b_rec rif40_geolevels%ROWTYPE;
	c3_rec RECORD;
	c4_rec RECORD;
--
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'rif40_get_geojson_as_js', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Test geography
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10007, 'rif40_get_geojson_as_js', 'NULL geography parameter');
	END IF;	
--
	OPEN c1(l_geography);
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10008, 'rif40_get_geojson_as_js', 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */);
	END IF;	
--
-- Test <geolevel view/area> exist
--
	OPEN c2(l_geography, geolevel_view);
	FETCH c2 INTO c2a_rec;
	CLOSE c2;
--
	IF c2a_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10009, 'rif40_get_geojson_as_js', 'geography: %, <geoevel view> %: not found', 
			l_geography::VARCHAR	/* Geography */, 
			geolevel_view::VARCHAR	/* geolevel view */);
	END IF;	
--
	OPEN c2b(l_geography, geolevel_area);
	FETCH c2b INTO c2b_rec;
	CLOSE c2b;
--
	IF c2b_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10010, 'rif40_get_geojson_as_js', 'geography: %, <geoevel area> %: not found', 
			l_geography::VARCHAR	/* Geography */, 
			geolevel_area::VARCHAR	/* Geoelvel area */);
	END IF;	
--
-- Test <geoevel area> resolution is higher than <geoevel view> resolution
-- e.g.
-- get_geojson_as_js() geography: EW01, <geoevel area> GOR2001 resolution (3) is NOT higher than <geoevel view> OA2001 resolution (7)
--
	IF c2a_rec.geolevel_id /* <geoevel view> */ < c2b_rec.geolevel_id /* <geoevel area> */ THEN
		PERFORM rif40_log_pkg.rif40_error(-10011, 'rif40_get_geojson_as_js', 'geography: %, <geoevel area> % resolution (%) higher than <geoevel view> % resolution (%)', 			
			l_geography::VARCHAR		/* Geography */, 
			geolevel_area::VARCHAR		/* Geolevel area */, 
			c2b_rec.geolevel_id::VARCHAR, 	/* Geolevel area ID (resolution) */
			geolevel_view::VARCHAR		/* geolevel view */, c2a_rec.geolevel_id::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_as_js', 'geography: %, <geoevel area> % resolution (%) is NOT higher than <geoevel view> % resolution (%)', 		
			l_geography::VARCHAR		/* Geography */, 
			geolevel_area::VARCHAR		/* Geolevel area */, 
			c2b_rec.geolevel_id::VARCHAR	/* Geolevel area ID (resolution) */, 
			geolevel_view::VARCHAR		/* geolevel view */, 
			c2a_rec.geolevel_id::VARCHAR	/* Geolevel view ID (resolution) */);
	END IF;
--
-- Test  <geolevel area id> is valid
-- 
	sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(LOWER(geolevel_view))||')) AS total'||E'\n'||
		  '  FROM '||quote_ident(LOWER(c1_rec.hierarchytable))||E'\n'||
		  ' WHERE '||quote_ident(LOWER(geolevel_area))||' = '||quote_literal(geolevel_area_id);
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_as_js', 'SQL> %;', sql_stmt::VARCHAR);
	OPEN c3 FOR EXECUTE sql_stmt;
	FETCH c3 INTO c3_rec;
	CLOSE c3;
	IF c3_rec.total = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10012, 'rif40_get_geojson_as_js', 'geography: %, <geoevel area> % id % does not exist in hierarchy table: %', 			
			l_geography::VARCHAR			/* Geography */, 
			geolevel_area::VARCHAR			/* Geoelvel area */,  
			geolevel_area_id::VARCHAR		/* Geoelvel area ID */, 
			LOWER(c1_rec.hierarchytable)::VARCHAR	/* Hierarchy table */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_as_js', 'geography: %, <geoevel area> % id % will return: % area_id', 		
			l_geography::VARCHAR		/* Geography */, 
			geolevel_area::VARCHAR		/* Geoelvel area */, 
			geolevel_area_id::VARCHAR	/* Geoelvel area ID */, 
			c3_rec.total::VARCHAR		/* Number of area_ids */);
	END IF;
--
-- Create SQL statement
-- 
	sql_stmt:='WITH a AS ('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT 0 ord, ''var spatialData={ "type": "FeatureCollection","features": [ /* Start */'' js'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'UNION'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT row_number() over() ord,'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||'       ''{"type": "Feature","properties":{"area_id":"''||area_id||''","name":"''||COALESCE(name, '''')||''"},"geometry": ''||optimised_geojson||''} /* ''||'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'row_number() over()||'' : ''||area_id||'' : ''||'' : ''||COALESCE(name, '''')||'' */ '' AS js'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||'  FROM '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' WHERE geolevel_name = '''||quote_ident(UPPER(geolevel_view))||''' /* <geolevel view> */ AND area_id IN ('||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT DISTINCT '||quote_ident(LOWER(geolevel_view))||' /* <geolevel view> */'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||'  FROM '||quote_ident(LOWER(c1_rec.hierarchytable))||' /* Heirarchy table */ WHERE '||
		quote_ident(LOWER(geolevel_area))||' /* <geolevel area> */ = '||quote_literal(geolevel_area_id)||' /* <geolevel area id> */)'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||E'\t'||'UNION'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT 999999 ord, '']} /* End */'' js'||E'\n';
	sql_stmt:=sql_stmt||')'||E'\n';
	sql_stmt:=sql_stmt||'SELECT CASE WHEN ord BETWEEN 2 AND 999998 THEN '',''||js ELSE js END AS js'||E'\n';
	sql_stmt:=sql_stmt||'  FROM a'||E'\n';
	sql_stmt:=sql_stmt||' ORDER BY ord';
--
-- Execute SQL statement, returning JS
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_as_js', 'SQL> %;', sql_stmt::VARCHAR);
	FOR c4_rec IN EXECUTE sql_stmt LOOP
		RETURN NEXT c4_rec.js;
	END LOOP;
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	rif40_get_geojson_as_js()
Parameters:	Geography, geolevel_view, geolevel_area, geolevel_area_id
Returns:	Text table
Description:	Get GeoJSON data as a Javascript variable. 
		For the disease mapping selection dialog phrase:
		"View the <geolevel view> (e.g. GOR2001) of <geolevel area> (e.g. London) and select at <geolevel select> (e.g. LADUA2001) level."

Execute a SQL statement like and return JS text:

WITH a AS (
        SELECT 0 ord, ''var spatialData={ "type": "FeatureCollection","features": [ /* Start */'' js
        UNION
        SELECT row_number() over() ord,
               ''{"type": "Feature","properties":{"area_id":"''||area_id||''","name":"''||COALESCE(name, '')||''"},"geometry": ''||topo_optimised_geojson||''} /* ''||row_number() over()||'' : ''||area_id||'' : ''||'' : ''||COALESCE(name, '')||'' */'' AS js
          FROM t_rif40_ew01_geometry
         WHERE geolevel_name = ''LADUA2001'' /* <geolevel view> */ AND area_id IN (
                SELECT DISTINCT ladua2001 /* <geolevel view> */
                  FROM ew2001_geography /* Heirarchy table */ WHERE gor2001 /* <geolevel area> */ = ''H'' /* <geolevel area id> */)
                UNION
                SELECT 999999 ord, '']} /* End */'' js
)
SELECT CASE WHEN ord BETWEEN 2 AND 999998 THEN '',''||js ELSE js END AS js
  FROM a
 ORDER BY ord;';

GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif40;

--
-- test it (when geography tables are present)
--
--\copy (SELECT * FROM rif40_xml_pkg.rif40_get_geojson_as_js('EW01', 'WARD2001', 'GOR2001', 'H')) to ../postgres/GeoJSON_tester/geojson_data/london_ward2001.js 
--\copy (SELECT * FROM rif40_xml_pkg.rif40_get_geojson_as_js('EW01', 'OA2001', 'GOR2001', 'H')) to ../postgres/GeoJSON_tester/geojson_data/london_oa2001.js 

--
-- Eof
