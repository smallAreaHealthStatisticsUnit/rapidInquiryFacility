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
--     				  Encapulate geoJSON in Javascript if required
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
-- rif40_xml_pkg.rif40_get_geojson_tiles: 		50400 to 50599
--
CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_get_geojson_tiles(
	l_geography 		VARCHAR, 
	l_geolevel_view 	VARCHAR,
	y_max 			REAL,
	x_max 			REAL, 
	y_min 			REAL, 
	x_min 			REAL, 
	zoom_level		INTEGER DEFAULT  8 /* 1 in 2,325,012 */,
	tile_name		VARCHAR DEFAULT NULL,
	return_one_row 		BOOLEAN DEFAULT TRUE)
RETURNS SETOF text
SECURITY INVOKER
AS $body$
/*

Function: 	rif40_get_geojson_tiles()
Parameters:	Geography, geolevel_view, 
			Y max, X max, Y min, X min as a record (bounding box), 
			Zoom level [Default: 8; scaling 1 in 2,325,012], tile name [Default: NULL]
			return one row (TRUE/FALSE)
Returns:	Text table [1 or more rows dependent on return_one_row]
Description:	Get GeoJSON data as a Javascript variable. 
		Fetch tiles bounding box Y max, X max, Y min, X min for <geography> <geolevel view>
		SRID is 4326 (WGS84)
		Check bounding box is 1x1 tile in size.
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

		1 row means 1 row, no CRLFs etc!
		If return_one_row is false then a header, 1 rows/area_id and a footer is returned so the Javascript is easier to read

Calls: _rif40_get_geojson_as_js to extract the data using the following SQL:

WITH a AS (
        SELECT 0 ord, '{ "type": "FeatureCollection","features": [ /- Start -/' js
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

Generates geoJSON that looks (when prettified using http://jsbeautifier.org/) like:

{
    "type": "FeatureCollection",
    "features": [ {
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
    }, {
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
    }]
} ;

Validated with JSlint: http://www.javascriptlint.com/online_lint.php

*/
DECLARE
	c1geojson2 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c2geojson2 CURSOR(l_geography VARCHAR, l_geolevel VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel;
	c3geojson2 REFCURSOR;
	c5geojson2 CURSOR(l_geography VARCHAR, l_geolevel_view VARCHAR, l_geolevel_area_id_list VARCHAR[], l_expected_rows INTEGER, l_zoom_level INTEGER) FOR
		WITH a AS (
			SELECT js FROM rif40_xml_pkg._rif40_get_geojson_as_js(
						l_geography, l_geolevel_view, l_geolevel_area_id_list, l_expected_rows, 
						TRUE /* Produce JSON not JS */, l_zoom_level)
		)
		SELECT ARRAY_AGG(js) AS js
		  FROM a;
	c6geojson2 CURSOR(l_y_max REAL, l_x_max REAL, l_y_min REAL, l_x_min REAL, l_zoom_level INTEGER) FOR
		WITH b AS (
			SELECT ST_Centroid(ST_MakeEnvelope(l_x_min, l_y_min, l_x_max, l_y_max)) AS centroid
		), c AS (
			SELECT ST_X(b.centroid) AS X_centroid, ST_Y(b.centroid) AS Y_centroid	  
			FROM b
		), d AS (
			SELECT X_centroid, Y_centroid, 
				rif40_geo_pkg.latitude2tile(X_centroid, l_zoom_level) AS X_tile,
				rif40_geo_pkg.longitude2tile(Y_centroid, l_zoom_level) AS Y_tile
			FROM c
		), e AS (
			SELECT X_centroid, Y_centroid,
				   rif40_geo_pkg.tile2latitude(X_tile, l_zoom_level) AS tile_X_min,
				   rif40_geo_pkg.tile2longitude(Y_tile, l_zoom_level) AS tile_Y_min,			
				   rif40_geo_pkg.tile2latitude(X_tile+1, l_zoom_level) AS tile_X_max,
				   rif40_geo_pkg.tile2longitude(Y_tile+1, l_zoom_level) AS tile_Y_max,
				   X_tile, Y_tile
		     FROM d
		)
		SELECT l_zoom_level AS zoom_level, X_centroid, Y_centroid, 
		       tile_X_min, tile_Y_min, tile_X_max, tile_Y_max, X_tile, Y_tile,
		       ABS(tile_X_min-l_x_min) AS x_min_diff,
		       ABS(tile_X_max-l_x_max) AS x_max_diff,
		       ABS(tile_Y_min-l_y_min) AS y_min_diff,
		       ABS(tile_Y_max-l_y_max) AS y_max_diff,
		       ROUND((ABS(tile_X_min-l_x_min)/tile_X_min)::NUMERIC*100, 4) AS x_min_diff_pct,
		       ROUND((ABS(tile_X_max-l_x_max)/tile_X_max)::NUMERIC*100, 4) AS x_max_diff_pct,
		       ROUND((ABS(tile_Y_min-l_y_min)/tile_Y_min)::NUMERIC*100, 4) AS y_min_diff_pct,
		       ROUND((ABS(tile_Y_max-l_y_max)/tile_Y_max)::NUMERIC*100, 4) AS y_max_diff_pct
  		  FROM e; 
--
	c1_rec rif40_geographies%ROWTYPE;
	c2a_rec rif40_geolevels%ROWTYPE;
	c3_rec RECORD;
	c4_rec RECORD;
	c5_rec RECORD;
	c6_rec RECORD;
--
	sql_stmt 		VARCHAR;
	i 				INTEGER:=0;
	geolevel_area_id_list	VARCHAR[];
	js				VARCHAR[];
	js_text			VARCHAR;
	error_count		INTEGER:=0;
--
	drop_stmt 		VARCHAR;
	explain_text 	VARCHAR;
	temp_table 		VARCHAR;
--
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	etp 			TIMESTAMP WITH TIME ZONE;
	took 			INTERVAL;
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-50400, 'rif40_get_geojson_tiles', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Create unique results temporary table
--
	IF rif40_log_pkg.rif40_is_debug_enabled('rif40_get_geojson_tiles', 'DEBUG2') THEN
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
	END IF;
--
-- Test geography
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50401, 'rif40_get_geojson_tiles', 'NULL geography parameter');
	END IF;	
--
	OPEN c1geojson2(l_geography);
	FETCH c1geojson2 INTO c1_rec;
	CLOSE c1geojson2;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50402, 'rif40_get_geojson_tiles', 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */);
	END IF;	
--
-- Test <geolevel view/area> exist
--
	OPEN c2geojson2(l_geography, l_geolevel_view);
	FETCH c2geojson2 INTO c2a_rec;
	CLOSE c2geojson2;
--
	IF c2a_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50403, 'rif40_get_geojson_tiles', 'geography: %, <geoevel view> %: not found', 
			l_geography::VARCHAR	/* Geography */, 
			geolevel_view::VARCHAR	/* Geolevel view */);
	END IF;	
/*
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')
), b AS (
	SELECT ST_Centroid(ST_MakeEnvelope(a.x_min, a.y_min, a.x_max, a.y_max)) AS centroid
	  FROM a
), c AS (
	SELECT ST_X(b.centroid) AS X_min, ST_Y(b.centroid) AS Y_min, 11 AS zoom_level	  
	  FROM b
), d AS (
	SELECT zoom_level, X_min, Y_min, 
		   rif40_geo_pkg.latitude2tile(X_min, zoom_level) AS X_tile,
		   rif40_geo_pkg.longitude2tile(Y_min, zoom_level) AS Y_tile
	  FROM c
), e AS (
	SELECT zoom_level, X_min, Y_min, 
		   rif40_geo_pkg.tile2latitude(X_tile+1, zoom_level) AS X_max,
		   rif40_geo_pkg.tile2longitude(Y_tile+1, zoom_level) AS Y_max,
		   X_tile, Y_tile
	  FROM d
)
SELECT * FROM e;	
  
*/
--
-- Test tile bounds by computing the centroid of the bounding box, get X/Y tile numbers and then computing the correct tile bounds.
-- Compare as a %; >0.01% different is an error 
--
	OPEN c6geojson2(y_max, x_max, y_min, x_min, zoom_level);
	FETCH c6geojson2 INTO c6_rec;
	CLOSE c6geojson2;
--
-- Check given vs comnputed
--
	IF c6_rec.x_min_diff_pct = 0 AND c6_rec.y_min_diff_pct = 0 AND c6_rec.x_max_diff_pct = 0 AND c6_rec.y_max_diff_pct = 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_tiles', 
			'[50404] Geography: %, <geoevel view> % bound [%, %, %, %] tile XY(%,%) verified [% %]; zoom level %; percent diffs [% % % %]', 			 
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_view::VARCHAR		/* Geoelvel view */,  
			x_min::VARCHAR					/* Xmin */,
			y_min::VARCHAR					/* Ymin */,
			x_max::VARCHAR					/* Xmax */,
			y_max::VARCHAR					/* Ymax */,
			c6_rec.x_tile::VARCHAR			/* X tile */,
			c6_rec.y_tile::VARCHAR			/* Y tile */,
			c6_rec.x_centroid::VARCHAR		/* X tile centroid */,
			c6_rec.y_centroid::VARCHAR		/* Y tile centroid */,
			zoom_level::VARCHAR				/* Zoom level */,
			c6_rec.x_min_diff_pct::VARCHAR	/* X min diff as a percentage */,
			c6_rec.y_min_diff_pct::VARCHAR	/* Y max diff as a percentage */,
			c6_rec.x_max_diff_pct::VARCHAR	/* X min diff as a percentage */,
			c6_rec.y_max_diff_pct::VARCHAR	/* Y max diff as a percentage */);
	ELSE
--
-- OK - different - display why
--
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_get_geojson_tiles', 
			'[50405] Geography: %, <geolevel view> % bound [%, %, %, %] tile XY(%,%) verified [% %]; zoom level %; expected bound [%, %, %, %]; percent diffs [% % % %]', 			 
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_view::VARCHAR		/* Geolevel view */,  
			x_min::VARCHAR					/* Xmin */,
			y_min::VARCHAR					/* Ymin */,
			x_max::VARCHAR					/* Xmax */,
			y_max::VARCHAR					/* Ymax */,
			c6_rec.x_tile::VARCHAR			/* X tile */,
			c6_rec.y_tile::VARCHAR			/* Y tile */,
			c6_rec.x_centroid::VARCHAR		/* X tile centroid */,
			c6_rec.y_centroid::VARCHAR		/* Y tile centroid */,
			zoom_level::VARCHAR				/* Zoom level */,
			c6_rec.tile_x_min::VARCHAR		/* tile Xmin */,
			c6_rec.tile_y_min::VARCHAR		/* tile Ymin */,
			c6_rec.tile_x_max::VARCHAR		/* tile Xmax */,
			c6_rec.tile_y_max::VARCHAR		/* tile Ymax */,			
			c6_rec.x_min_diff_pct::VARCHAR	/* X min diff as a percentage */,
			c6_rec.y_min_diff_pct::VARCHAR	/* Y max diff as a percentage */,
			c6_rec.x_max_diff_pct::VARCHAR	/* X min diff as a percentage */,
			c6_rec.y_max_diff_pct::VARCHAR	/* Y max diff as a percentage */);
--
-- Error if >0.01% different
--
		IF c6_rec.y_max_diff_pct > 0.01 /* percent */ THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_get_geojson_tiles', 
				'[50406] Y max diff percent: %, diff: %, parameter Y max %, tile % Y max: %',
				c6_rec.y_max_diff_pct::VARCHAR	/* Y max diff as a percentage */,
				c6_rec.y_max_diff::VARCHAR		/* Y max diff */, 
				y_max::VARCHAR					/* Ymax */,
				c6_rec.y_tile::VARCHAR			/* Y tile */,
				c6_rec.tile_Y_max::VARCHAR		/* Tile Y max (i.e. what is being diffed with) */);	
			error_count:=error_count+1;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_tiles', 
				'[50407] Y max diff percent: %, diff: %, parameter Y max %, tile % Y max: %',
				c6_rec.y_max_diff_pct::VARCHAR	/* Y max diff as a percentage */,
				c6_rec.y_max_diff::VARCHAR		/* Y max diff */, 
				y_max::VARCHAR					/* Ymax */,
				c6_rec.y_tile::VARCHAR			/* Y tile */,
				c6_rec.tile_Y_max::VARCHAR		/* Tile Y max (i.e. what is being diffed with) */);			
		END IF;
		IF c6_rec.x_max_diff_pct > 0.01 /* percent */ THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_get_geojson_tiles', 
				'[50408] X max diff percent: %, diff: %, parameter X max %, tile % X max: %',
				c6_rec.x_max_diff_pct::VARCHAR	/* X max diff as a percentage */,
				c6_rec.x_max_diff::VARCHAR		/* X max diff */, 
				y_max::VARCHAR					/* Ymax */,
				c6_rec.x_tile::VARCHAR			/* X tile */,
				c6_rec.tile_X_max::VARCHAR		/* Tile X max (i.e. what is being diffed with)*/);	
			error_count:=error_count+1;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_tiles', 
				'[50409] X max diff percent: %, diff: %, parameter X max %, tile % X max: %',
				c6_rec.x_max_diff_pct::VARCHAR	/* X max diff as a percentage */,
				c6_rec.x_max_diff::VARCHAR		/* X max diff */, 
				y_max::VARCHAR					/* Xmax */,
				c6_rec.x_tile::VARCHAR			/* X tile */,
				c6_rec.tile_x_max::VARCHAR		/* Tile X max (i.e. what is being diffed with)*/);			
		END IF;
		
		IF c6_rec.y_min_diff_pct > 0.01 /* percent */ THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_get_geojson_tiles', 
				'[50410] Y min diff percent: %, diff: %, parameter Y min %, tile % Y min: %',
				c6_rec.y_min_diff_pct::VARCHAR	/* Y min diff as a percentage */,
				c6_rec.y_min_diff::VARCHAR		/* Y min diff */, 
				y_min::VARCHAR					/* Ymin */,
				c6_rec.y_tile::VARCHAR			/* Y tile */,
				c6_rec.tile_Y_min::VARCHAR		/* Tile Y min (i.e. what is being diffed with) */);	
			error_count:=error_count+1;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_tiles', 
				'[50411] Y min diff percent: %, diff: %, parameter Y min %, tile % Y min: %',
				c6_rec.y_min_diff_pct::VARCHAR	/* Y min diff as a percentage */,
				c6_rec.y_min_diff::VARCHAR		/* Y min diff */, 
				y_min::VARCHAR					/* Ymin */,
				c6_rec.y_tile::VARCHAR			/* Y tile */,
				c6_rec.tile_Y_min::VARCHAR		/* Tile Y min (i.e. what is being diffed with) */);			
		END IF;
		IF c6_rec.x_min_diff_pct > 0.01 /* percent */ THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_get_geojson_tiles', 
				'[50412] X min diff percent: %, diff: %, parameter X min %, tile % X min: %',
				c6_rec.x_min_diff_pct::VARCHAR	/* X min diff as a percentage */,
				c6_rec.x_min_diff::VARCHAR		/* X min diff */, 
				x_min::VARCHAR					/* Ymin */,
				c6_rec.x_tile::VARCHAR			/* X tile */,
				c6_rec.tile_x_min::VARCHAR		/* Tile X min (i.e. what is being diffed with)*/);	
			error_count:=error_count+1;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_tiles', 
				'[50413] X min diff percent: %, diff: %, parameter X min %, tile % X min: %',
				c6_rec.x_min_diff_pct::VARCHAR	/* X min diff as a percentage */,
				c6_rec.x_min_diff::VARCHAR		/* X min diff */, 
				x_min::VARCHAR					/* Xmin */,
				c6_rec.x_tile::VARCHAR			/* X tile */,
				c6_rec.tile_x_min::VARCHAR		/* Tile X min (i.e. what is being diffed with)*/);			
		END IF;
--
-- Raise error if required
--
		IF error_count > 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-50414, 'rif40_get_geojson_tiles', 
				'[50414] Geography: %, <geoevel view> % bound validation [%, %, %, %] tile XY(%,%); zoom level %; had % errors', 			 
				l_geography::VARCHAR			/* Geography */, 
				l_geolevel_view::VARCHAR		/* Geoelvel view */,  
				x_min::VARCHAR					/* Xmin */,
				y_min::VARCHAR					/* Ymin */,
				x_max::VARCHAR					/* Xmax */,
				y_max::VARCHAR					/* Ymax */,
				c6_rec.x_tile::VARCHAR			/* X tile */,
				c6_rec.y_tile::VARCHAR			/* Y tile */,
				zoom_level::VARCHAR				/* Zoom level */,
				error_count::VARCHAR			/* */);
		END IF;
	END IF;
--
-- Cache code will go here...
--	
	
--
-- Test  <geolevel area id list> is valid
--
/* 
WITH a AS (
        SELECT area_id,
            ST_MakeEnvelope($1 /- Xmin -/, $2 /- Ymin -/, $3 /- Xmax -/, $4 /- YMax -/) AS geom /- Bound -/
          FROM t_rif40_sahsu_geometry
         WHERE optimised_geometry && ST_MakeEnvelope($1 /- Xmin -/, $2 /- Ymin -/, $3 /- Xmax -/, $4 /- YMax -/)
           AND geolevel_name = $5               /- Partition eliminate -/
           /- Intersect bound with geolevel geometry -/
)
SELECT COUNT(DISTINCT(a.area_id)) AS total      /- Total area IDs -/,
       ARRAY_AGG(a.area_id) AS area_id_list     /- Array of area IDs -/,
       ST_IsValid(a.geom) AS is_valid   /- Test bound -/,
       ST_Area(a.geom) AS area          /- Area of bound -/
  FROM a
 GROUP BY ST_IsValid(a.geom), ST_Area(a.geom);

psql:alter_scripts/v4_0_alter_2.sql:439: INFO:  [DEBUG1] rif40_get_geojson_tiles(): [50408] Geography: SAHSU, <geoevel view> LEVEL4 bound [-6.68853, 54.6456, -6.32507, 55.0122] will return: 117 area_id, area: 0.133226261787058, took: 00:00:00.031.

psql:alter_scripts/v4_0_alter_2.sql:439: INFO:  [DEBUG1] rif40_get_geojson_tiles(): [50212] Bounds EXPLAIN PLAN.
GroupAggregate  (cost=1255.78..1277.98 rows=80 width=548) (actual time=16.627..16.627 rows=1 loops=1)
  Output: count(DISTINCT a.area_id), array_agg(a.area_id), (st_isvalid(a.geom)), (st_area(a.geom))
  CTE a
    ->  Result  (cost=0.00..1231.45 rows=80 width=22) (actual time=0.065..2.239 rows=117 loops=1)
          Output: t_rif40_sahsu_geometry.area_id, '01030000000100000005000000000000000DC11AC000000080A4524B40000000000DC11AC0000000C08F814B4000000040DF4C19C0000000C08F814B400000004
0DF4C19C000000080A4524B40000000000DC11AC000000080A4524B40'::geometry
          ->  Append  (cost=0.00..1231.45 rows=80 width=22) (actual time=0.065..2.170 rows=117 loops=1)
                ->  Seq Scan on rif40.t_rif40_sahsu_geometry  (cost=0.00..0.00 rows=1 width=516) (actual time=0.001..0.001 rows=0 loops=1)
                      Output: t_rif40_sahsu_geometry.area_id
                      Filter: ((t_rif40_sahsu_geometry.optimised_geometry && '01030000000100000005000000000000000DC11AC000000080A4524B40000000000DC11AC0000000C08F814B4000000040DF4C
19C0000000C08F814B4000000040DF4C19C000000080A4524B40000000000DC11AC000000080A4524B40'::geometry) AND ((t_rif40_sahsu_geometry.geolevel_name)::text = 'LEVEL4'::text))
                ->  Seq Scan on rif40.t_rif40_geolevels_geometry_sahsu_level4  (cost=0.00..1231.45 rows=79 width=16) (actual time=0.063..2.116 rows=117 loops=1)
                      Output: t_rif40_geolevels_geometry_sahsu_level4.area_id
                      Filter: ((t_rif40_geolevels_geometry_sahsu_level4.optimised_geometry && '01030000000100000005000000000000000DC11AC000000080A4524B40000000000DC11AC0000000C08F8
14B4000000040DF4C19C0000000C08F814B4000000040DF4C19C000000080A4524B40000000000DC11AC000000080A4524B40'::geometry) AND ((t_rif40_geolevels_geometry_sahsu_level4.geolevel_name)::text
 = 'LEVEL4'::text))
                      Rows Removed by Filter: 1113
  ->  Sort  (cost=24.33..24.53 rows=80 width=548) (actual time=16.281..16.304 rows=117 loops=1)
        Output: (st_isvalid(a.geom)), (st_area(a.geom)), a.area_id
        Sort Key: (st_isvalid(a.geom)), (st_area(a.geom))
        Sort Method: quicksort  Memory: 34kB
        ->  CTE Scan on a  (cost=0.00..21.80 rows=80 width=548) (actual time=0.306..16.150 rows=117 loops=1)
              Output: st_isvalid(a.geom), st_area(a.geom), a.area_id
Total runtime: 19.472 ms

*/
-- 		  '	  optimised_geometry && ST_MakeEnvelope($1 /* Xmin */, $2 /* Ymin */, $3 /* Xmax */, $4 /* YMax */)'||E'\n'||

	sql_stmt:='WITH a AS ('||E'\n'||
		  '	SELECT area_id,'||E'\n'||
		  '            ST_MakeEnvelope($1 /* Xmin */, $2 /* Ymin */, $3 /* Xmax */, $4 /* YMax */) AS geom	/* Bound */'||E'\n'||
		  '	  FROM '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||E'\n'||
		  '	 WHERE ST_Intersects(optimised_geometry,'||E'\n'||
		  '        		ST_MakeEnvelope($1 /* Xmin */,'||E'\n'||
		  '        					 $2 	/* Ymin */,'||E'\n'||
		  '       			 		 $3 	/* Xmax */,'||E'\n'||
		  '        					 $4 	/* YMax */,'||E'\n'||
		  '        					 4326 	/* WGS 84 */))'||E'\n'||
		  '	   AND geolevel_name = $5		/* Partition eliminate */'||E'\n'||
		  '	   /* Intersect bound with geolevel geometry */'||E'\n'||
		  ')'||E'\n'||
		  'SELECT COUNT(DISTINCT(a.area_id)) AS total	/* Total area IDs */,'||E'\n'||
		  '       ARRAY_AGG(a.area_id) AS area_id_list	/* Array of area IDs */,'||E'\n'||
		  '       ST_IsValid(a.geom) AS is_valid	/* Test bound */,'||E'\n'||
		  '       ST_Area(a.geom) AS area		/* Area of bound */'||E'\n'||
		  '  FROM a'||E'\n'||
		  ' GROUP BY ST_IsValid(a.geom), ST_Area(a.geom)';
--
-- Instrument
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	stp:=clock_timestamp();
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_tiles', 
		'[50415] <geolevel area id list> SQL> [using %, %, %, %, %], time taken so far: %'||E'\n'||'%;',
		x_min::VARCHAR			/* Xmin */,
		y_min::VARCHAR			/* Ymin */,
		x_max::VARCHAR			/* Xmax */,
		y_max::VARCHAR			/* Ymax */,
		l_geolevel_view::VARCHAR	/* Geolevel view */,
		took::VARCHAR			/* Time taken */,
		sql_stmt::VARCHAR		/* SQL statement */);
--
-- Begin execution block to trap parse errors
--
-- EXPLAIN PLAN version
--
	IF rif40_log_pkg.rif40_is_debug_enabled('rif40_get_geojson_tiles', 'DEBUG2') THEN
		BEGIN
--
-- Create results temporary table, extract explain plan  using _rif40_geojson_explain_ddl2() helper function.
-- This ensures the EXPLAIN PLAN output is a field called explain_line 
--
			sql_stmt:='SELECT explain_line FROM rif40_xml_pkg._rif40_geojson_explain_ddl2('||quote_literal(
					'EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE '||temp_table||' AS '||E'\n'||sql_stmt)||', $1, $2, $3, $4, $5)';
			FOR c4_rec IN EXECUTE sql_stmt USING x_min, y_min, x_max, y_max, l_geolevel_view LOOP
				IF explain_text IS NULL THEN
					explain_text:=c4_rec.explain_line;
				ELSE
					explain_text:=explain_text||E'\n'||c4_rec.explain_line;
				END IF;
			END LOOP;
--
-- Now extract actual results from temp table
--	
			OPEN c3geojson2 FOR EXECUTE 'SELECT * FROM '||temp_table;
			FETCH c3geojson2 INTO c3_rec;
			CLOSE c3geojson2;
		EXCEPTION
			WHEN others THEN
--
-- Print exception to INFO, re-raise
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='rif40_get_geojson_tiles() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||E'\n'||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '50416: %', error_message;
--
				RAISE;
		END;
--
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_get_geojson_tiles', '[50212] Bounds EXPLAIN PLAN.'||E'\n'||'%', explain_text::VARCHAR);
	ELSE
		BEGIN
			OPEN c3geojson2 FOR EXECUTE sql_stmt USING x_min, y_min, x_max, y_max, l_geolevel_view;
			FETCH c3geojson2 INTO c3_rec;
			CLOSE c3geojson2;
		EXCEPTION
			WHEN others THEN
--
-- Print exception to INFO, re-raise
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='rif40_get_geojson_tiles() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||E'\n'||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '50417: %', error_message;
--
				RAISE;
		END;
	END IF;
--
-- Instrument
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	stp:=clock_timestamp();
--
-- Check error flags
--
	IF c3_rec.is_valid = FALSE THEN
		PERFORM rif40_log_pkg.rif40_error(-50418, 'rif40_get_geojson_tiles', 
			'Geography: %, <geoevel view> % bound [%, %, %, %] is invalid, ST_Intersects() took: %.', 			
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_view::VARCHAR		/* Geoelvel view */,  
			x_min::VARCHAR				/* Xmin */,
			y_min::VARCHAR				/* Ymin */,
			x_max::VARCHAR				/* Xmax */,
			y_max::VARCHAR				/* Ymax */,
			took::VARCHAR				/* Time taken */);
	ELSIF c3_rec.total = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-50419, 'rif40_get_geojson_tiles', 
			'Geography: %, <geoevel view> % bound [%, %, %, %] returns no area IDs, ST_Intersects() took: %.', 			
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_view::VARCHAR		/* Geoelvel view */,  
			x_min::VARCHAR				/* Xmin */,
			y_min::VARCHAR				/* Ymin */,
			x_max::VARCHAR				/* Xmax */,
			y_max::VARCHAR				/* Ymax */,
			took::VARCHAR				/* Time taken */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_tiles', 
			'[50420] Geography: %, <geoevel view> % bound [%, %, %, %] will return: % area_id, area: %, ST_Intersects() took: %.', 		
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_view::VARCHAR		/* Geoelvel view */, 
			x_min::VARCHAR				/* Xmin */,
			y_min::VARCHAR				/* Ymin */,
			x_max::VARCHAR				/* Xmax */,
			y_max::VARCHAR				/* Ymax */,
			c3_rec.total::VARCHAR			/* Number of area_ids */,
		        c3_rec.area::VARCHAR			/* Area of ST_MakeEnvelope() */,
			took::VARCHAR				/* Time taken */);
	END IF;
--
-- List of area IDs to dump
--
	geolevel_area_id_list:=c3_rec.area_id_list; 
--
-- Call  rif40_xml_pkg._rif40_get_geojson_as_js()
--
	OPEN c5geojson2(l_geography, l_geolevel_view, geolevel_area_id_list, (c3_rec.total+2)::INTEGER /* l_expected_rows */, zoom_level);
	FETCH c5geojson2 INTO c5_rec;
	CLOSE c5geojson2;
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
-- Instrument
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_geojson_tiles', 
		'[50421] Geography: %, <geoevel view> % bound [%, %, %, %] complete, db extract took: %.', 		
		l_geography::VARCHAR			/* Geography */, 
		l_geolevel_view::VARCHAR		/* Geoelvel view */, 
		x_min::VARCHAR				/* Xmin */,
		y_min::VARCHAR				/* Ymin */,
		x_max::VARCHAR				/* Xmax */,
		y_max::VARCHAR				/* Ymax */,
		took::VARCHAR				/* Time taken */);
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_get_geojson_tiles(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL, 
	INTEGER, VARCHAR, BOOLEAN) IS 'Function: 	rif40_get_geojson_tiles()
Parameters:	Geography, geolevel_view, 
			Y max, X max, Y min, X min as a record (bounding box), 
			Zoom level [Default: 8; scaling 1 in 2,325,012], tile name [Default: NULL]
			return one row (TRUE/FALSE)
Returns:	Text table [1 or more rows dependent on return_one_row]
Description:	Get GeoJSON data as a Javascript variable. 
		Fetch tiles bounding box Y max, X max, Y min, X min for <geography> <geolevel view>
		SRID is 4326 (WGS84)
		Check bounding box is 1x1 tile in size.
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

		1 row means 1 row, no CRLFs etc!
		If return_one_row is false then a header, 1 rows/area_id and a footer is returned so the Javascript is easier to read

Calls: _rif40_get_geojson_as_js to extract the data using the following SQL:

WITH a AS (
        SELECT 0 ord, ''{ "type": "FeatureCollection","features": [ /* Start */'' js
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

Generates geoJSON that looks (when prettified using http://jsbeautifier.org/) like:

{
    "type": "FeatureCollection",
    "features": [ {
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
    }, {
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
    } ]
};

Validated with JSlint: http://www.javascriptlint.com/online_lint.php';

GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_get_geojson_tiles(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL, INTEGER, VARCHAR, BOOLEAN) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_get_geojson_tiles(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL, INTEGER, VARCHAR, BOOLEAN) TO rif_user;

--
-- Eof
