-- ************************************************************************
-- *
-- * THIS IS A SCHEMA ALTER SCRIPT - IT CAN BE RE-RUN BUT THEY MUST BE RUN 
-- * IN NUMERIC ORDER
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
-- Rapid Enquiry Facility (RIF) - RIF alter script 5 - Zoomlevel support
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

\echo Running SAHSULAND schema alter script #5 zoomlevel support.

/*

Alter script #5

Rebuilds all geolevel tables with full partitioning (alter #3 support):

1. Convert to 4326 (WGS84 GPS projection) before simplification. Optimised geometry is 
   always in 4326.
2. Zoomlevel support. Optimised geometry is level 6, OPTIMISED_GEOMETRY_1 is level 8,
   OPTIMISED_GEOMETRY_2 is level 11; likewise OPTIMISED_GEOJSON (which will become a JOSN type). 
3. Simplification to warn if bounds of map at zoomlevel 6 exceeds 4x3 tiles.
4. Simplification to fail if bounds of map < 5% of zoomlevel 11 (bound area: 78x58km); 
   i.e. the map is not projected correctly (as sahsuland is not at present). Fix
   sahsuland projection (i.e. it is 27700; do the export using GDAL correctly).
5. Calculate the latitude of the middle of the total map bound; use this as the latitude
   in if40_geo_pkg.rif40_zoom_levels() for the correct m/pixel.
6. Remove ST_SIMPLIFY_TOLERANCE; replace with m/pixel for zoomlevel (hence the reason 
   for converting to 4326 before simplification).
8. Add support for regionINLA.txt on a per study basis.
7. Convert simplification and intersection code to Java; call via PL/Java. Object creation
   functions will remain port specific (because of Postgres partitioning).

<total area_id>
<area_id> <total (N)> <adjacent area 1> .. <adjacent area N>

*/
   
BEGIN;

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
-- Add zoomlevel support etc
--
\i ../PLpgsql/v4_0_rif40_geo_pkg.sql
\i ../PLpgsql/v4_0_rif40_xml_pkg.sql

\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
--
-- Functions to enable debug for
--
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl', 
		'rif40_zoom_levels', 'rif40_GetMapAreas'];
--
	l_function 					VARCHAR;
BEGIN
--
-- Turn on some debug
--
        PERFORM rif40_log_pkg.rif40_log_setup();
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
	FOREACH l_function IN ARRAY rif40_sql_pkg_functions LOOP
		RAISE INFO 'Enable debug for function: %', l_function;
		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
	END LOOP;
	
END;
$$;
  
SELECT * FROM rif40_geo_pkg.rif40_zoom_levels()/* 0 */;
SELECT * FROM rif40_geo_pkg.rif40_zoom_levels(30);
SELECT * FROM rif40_geo_pkg.rif40_zoom_levels(60);	
SELECT * FROM rif40_geo_pkg.rif40_zoom_levels(80);
SELECT * FROM rif40_geo_pkg.rif40_zoom_levels(88);
SELECT * FROM rif40_geo_pkg.rif40_zoom_levels(89.99);
DO LANGUAGE plpgsql $$
BEGIN
	SELECT * FROM rif40_geo_pkg.rif40_zoom_levels(90);
EXCEPTION 
	WHEN others THEN NULL;
END;
$$;	

WITH b AS (
	SELECT geography, srid, rif40_geo_pkg.rif40_zoom_levels(	
			ST_Y( 														/* Get latitude */
				ST_transform( 											/* Transform to 4326 */
					ST_GeomFromEWKT('SRID='||a.srid||';POINT(0 0)') 	/* Grid Origin */, 
						4326)
				)::NUMERIC) AS zl
	   FROM rif40_geographies a
)
SELECT geography, srid,
       (zl).zoom_level,
       (zl).latitude, 
	   (zl).tiles, 
	   (zl).degrees_per_tile, 
	   (zl).m_x_per_pixel, 
	   (zl).m_y_per_pixel, 
	   (zl).simplify_tolerance,
	   (zl).scale
  FROM b
 WHERE (zl).zoom_level IN (6, 8, 11) /* RIF zoomlevels */;
 
/*
SELECT * FROM rif40_geo_pkg.rif40_zoom_levels();
psql:alter_scripts/v4_0_alter_5.sql:134: INFO:  [DEBUG1] rif40_zoom_levels(): [60001] latitude: 0
 zoom_level | latitude |    tiles     | degrees_per_tile | m_x_per_pixel_est | m_x_per_pixel | m_y_per_pixel |   m_x    |   m_y    | simplify_tolerance |      scale
------------+----------+--------------+------------------+-------------------+---------------+---------------+----------+----------+--------------------+------------------
          0 |        0 |            1 |              360 |            156412 |        155497 |               | 39807187 |          |               1.40 | 1 in 591,225,112
          1 |        0 |            4 |              180 |             78206 |         77748 |               | 19903593 |          |               0.70 | 1 in 295,612,556
          2 |        0 |           16 |               90 |             39103 |         39136 |         39070 | 10018754 | 10001966 |               0.35 | 1 in 148,800,745
          3 |        0 |           64 |               45 |             19552 |         19568 |         19472 |  5009377 |  4984944 |               0.18 | 1 in 74,400,373
          4 |        0 |          256 |             22.5 |              9776 |          9784 |          9723 |  2504689 |  2489167 |               0.09 | 1 in 37,200,186
          5 |        0 |         1024 |            11.25 |              4888 |          4892 |          4860 |  1252344 |  1244120 |               0.04 | 1 in 18,600,093
          6 |        0 |         4096 |            5.625 |              2444 |          2446 |          2430 |   626172 |   622000 |              0.022 | 1 in 9,300,047
          7 |        0 |        16384 |            2.813 |              1222 |          1223 |          1215 |   313086 |   310993 |              0.011 | 1 in 4,650,023
          8 |        0 |        65536 |            1.406 |               611 |           611 |           607 |   156543 |   155495 |             0.0055 | 1 in 2,325,012
          9 |        0 |       262144 |            0.703 |               305 |           306 |           304 |    78272 |    77748 |             0.0027 | 1 in 1,162,506
         10 |        0 |      1048576 |            0.352 |               153 |           153 |           152 |    39136 |    38874 |             0.0014 | 1 in 581,253
         11 |        0 |      4194304 |            0.176 |                76 |            76 |            76 |    19568 |    19437 |            0.00069 | 1 in 290,626
         12 |        0 |     16777216 |            0.088 |                38 |            38 |            38 |     9784 |     9718 |            0.00034 | 1 in 145,313
         13 |        0 |     67108864 |            0.044 |                19 |            19 |            19 |     4892 |     4859 |            0.00017 | 1 in 72,657
         14 |        0 |    268435456 |            0.022 |               9.5 |           9.6 |           9.5 |     2446 |     2430 |          0.0000858 | 1 in 36,328
         15 |        0 |   1073741824 |            0.011 |               4.8 |           4.8 |           4.7 |     1223 |     1215 |          0.0000429 | 1 in 18,164
         16 |        0 |   4294967296 |            0.005 |               2.4 |           2.4 |           2.4 |      611 |      607 |          0.0000215 | 1 in 9,082
         17 |        0 |  17179869184 |            0.003 |              1.19 |          1.19 |          1.19 |      306 |      304 |          0.0000107 | 1 in 4,541
         18 |        0 |  68719476736 |           0.0014 |              0.60 |          0.60 |          0.59 |      153 |      152 |          0.0000054 | 1 in 2,271
         19 |        0 | 274877906944 |          0.00069 |              0.30 |          0.30 |          0.30 |       76 |       76 |          0.0000027 | 1 in 1,135
(20 rows)
 */
--
-- Projection was wrong, is now correct; i.e. SAHSULAND is 3,286 square km is size...
--
SELECT a.geolevel_name, 
       b.st_simplify_tolerance,
/*	   ST_Distance_Spheroid(
			ST_GeomFromEWKT('SRID=27700;POINT(0 0)'), 
			ST_GeomFromEWKT('SRID=27700;POINT('||b.st_simplify_tolerance||' 0)'),
			'SPHEROID["Airy 1830",6377563.396,299.3249646]') st_simplify_tolerance_in_m, WRONG - IN DEGREES */
       COUNT(a.area_id) AS t_areas, 
       SUM(ST_NPoints(a.shapefile_geometry)) AS t_points, 
	   ROUND((SUM(ST_Area(a.shapefile_geometry)))::NUMERIC, 1) AS t_area, 
	   ROUND((SUM(ST_perimeter(a.shapefile_geometry)))::NUMERIC, 1) AS t_perimeter,
	   ROUND((SUM(ST_Area(a.shapefile_geometry))/10000001)::NUMERIC, 1) AS t_area_km2, 
	   ROUND((SUM(ST_perimeter(a.shapefile_geometry))/1000)::NUMERIC, 1) AS t_perimeter_km
  FROM t_rif40_sahsu_geometry a, rif40_geolevels b
 WHERE a.geolevel_name = b.geolevel_name
 GROUP BY b.geolevel_id, a.geolevel_name, b.st_simplify_tolerance
 ORDER BY b.geolevel_id;
/*
 geolevel_name | st_simplify_tolerance | st_simplify_tolerance_in_m | t_areas | t_points |    t_area     | t_perimeter | t_area_km2 | t_perimeter_km
---------------+-----------------------+----------------------------+---------+----------+---------------+-------------+------------+----------------
 LEVEL1        |                   500 |           15583327.1301406 |       1 |    12344 | 32857211853.1 |   1677331.4 |     3285.7 |         1677.3
 LEVEL2        |                   100 |           11130947.9501005 |      17 |    41566 | 32857211853.0 |   4774429.0 |     3285.7 |         4774.4
 LEVEL3        |                    50 |           5565473.97505023 |     200 |    85247 | 32857211852.8 |   9651243.6 |     3285.7 |         9651.2
 LEVEL4        |                    10 |           1113094.79501005 |    1230 |   135813 | 32857211853.1 |  15287760.5 |     3285.7 |        15287.8
(4 rows)
 */
 
 /*
 "PROJCS["OSGB 1936 / British National Grid",
	GEOGCS["OSGB 1936",
		DATUM["OSGB_1936",
			SPHEROID["Airy 1830",6377563.396,299.3249646,AUTHORITY["EPSG","7001"]],
			TOWGS84[446.448,-125.157,542.06,0.15,0.247,0.842,-20.489],AUTHORITY["EPSG","6277"]
			],
		PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],
		UNIT["degree",0.0174532925199433,AUTHORITY["EPSG","9122"]],
		AUTHORITY["EPSG","4277"]
		],
	UNIT["metre",1,AUTHORITY["EPSG","9001"]],
	PROJECTION["Transverse_Mercator"],
		PARAMETER["latitude_of_origin",49],
		PARAMETER["central_meridian",-2],
		PARAMETER["scale_factor",0.9996012717],
		PARAMETER["false_easting",400000],
		PARAMETER["false_northing",-100000],
		AUTHORITY["EPSG","27700"],
		AXIS["Easting",EAST],
		AXIS["Northing",NORTH]
	]"
	
GEOGCS["WGS 84",
	DATUM["WGS_1984",
		SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],
		AUTHORITY["EPSG","6326"]],
	PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],
	UNIT["degree",0.0174532925199433,
	AUTHORITY["EPSG","9122"]],
AUTHORITY["EPSG","4326"]]
 */
 
WITH a AS (
	SELECT srid, 
	       substring(srtext, position('SPHEROID[' in srtext)) AS l_spheroid
	FROM spatial_ref_sys
   WHERE srid = 4326
      OR srid IN (SELECT DISTINCT srid FROM rif40_geographies)   
), d AS (
	SELECT srid, 
	       ST_Distance_Spheroid(
				ST_GeomFromEWKT('SRID='||a.srid||';POINT(0 0)'), /* POINT(lat long) */
				ST_GeomFromEWKT('SRID='||a.srid||';POINT(1 0)'),
				a.l_spheroid::spheroid) one_unit_in_m,
				a.l_spheroid
	  FROM a
), e AS (
	SELECT srid, 
	       ST_Distance_Spheroid(
				ST_GeomFromEWKT('SRID='||d.srid||';POINT(0 0)'), 
				ST_GeomFromEWKT('SRID='||d.srid||';POINT('||100/d.one_unit_in_m||' 0)'),
				d.l_spheroid::spheroid) one_hundred_m
	  FROM d
)			
SELECT b.geography, 
       a.srid, 
	   d.one_unit_in_m,
	   e.one_hundred_m,
       substring(a.l_spheroid, 1, position(',AUTHORITY[' in a.l_spheroid)-1)||']' AS spheroid
  FROM d, e, a LEFT OUTER JOIN rif40_geographies b ON (a.srid = b.srid)
 WHERE a.srid = d.srid
   AND d.srid = e.srid;
/*
 geography | srid  |  one_unit_in_m   | one_hundred_m |                   spheroid
-----------+-------+------------------+---------------+-----------------------------------------------
           |  4326 | 111319.490779206 |           100 | SPHEROID["WGS 84",6378137,298.257223563]
 SAHSU     | 27700 | 111309.479501005 |           100 | SPHEROID["Airy 1830",6377563.396,299.3249646]
 EW01      | 27700 | 111309.479501005 |           100 | SPHEROID["Airy 1830",6377563.396,299.3249646]
 UK91      | 27700 | 111309.479501005 |           100 | SPHEROID["Airy 1830",6377563.396,299.3249646]
(4 rows)
 */
  
SELECT substring(a1.spheroid, 1, position(',AUTHORITY[' in a1.spheroid)-1)||']' AS spheroid
  FROM (
	SELECT substring(srtext, position('SPHEROID[' in srtext)) AS spheroid
		FROM spatial_ref_sys	
		WHERE srid = 4326) a1;
/*
tileinfo 17 70406 42988
-I-> Input: Osm-Tile z/x/y[17/70406/42988] tms[88083]
tile_osm : 17/70406/42988
tile_tms : 17/70406/88083
center=13.37722778,52.51538515
bbox=13.375854492,52.514549436,13.378601074,52.516220864
SRID=4326;POINT(13.37722778 52.51538515)
SRID=4326;POLYGON((13.375854492 52.516220864,13.378601074 52.516220864,13.378601074 52.514549436,13.375854492 52.514549436,13.375854492 52.516220864))
wget 'http://mt3.google.com/vt/lyrs=s,h&z=17&x=70406&y=42988' -O 17_70406_42988_osm.png */

SELECT rif40_geo_pkg.longitude2tile(13.37722778, 17);
-- 70406
SELECT rif40_geo_pkg.tile2longitude(70406, 17);
-- 13.37722778
SELECT rif40_geo_pkg.latitude2tile(52.51538515, 17);
-- 42988
SELECT rif40_geo_pkg.tile2latitude(42988, 17);
-- 52.51538515
SELECT rif40_geo_pkg.y_osm_tile2_tms_tile(rif40_geo_pkg.latitude2tile(52.51538515, 17), 17);
-- 88083
	
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')
) 
SELECT SUBSTRING(
		rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSU' 	/* Geography */, 
			'LEVEL4' 	/* geolevel view */, 
			a.y_max, a.x_max, a.y_min, a.x_min, /* Bounding box - from cte */
			9 			/* Zoom level */,
			NULL		/* Tile name */, 
			FALSE 		/* return_one_row flag: output multiple rows so it is readable! */) 
			FROM 1 FOR 160 /* Truncate to 160 chars */) AS json 
  FROM a LIMIT 4;	
--
-- rif40_GetMapAreas interface
--
\pset title 'rif40_GetMapAreas interface'
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')
) 
SELECT rif40_xml_pkg.rif40_GetMapAreas(
			'SAHSU' 	/* Geography */, 
			'LEVEL4' 	/* geolevel view */, 
			a.y_max, a.x_max, a.y_min, a.x_min /* Bounding box - from cte */) AS json 
  FROM a LIMIT 4;
  
DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'Aborting (script being tested)';
	RAISE EXCEPTION 'C20999: Abort';
END;
$$;

END;

--
-- Eof