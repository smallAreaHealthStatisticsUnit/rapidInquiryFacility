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
   OPTIMISED_GEOMETRY_2 is level 11; likewise OPTIMISED_GEOJSON.
3. Simplification to warn if bounds of map at zoomlevel 6 exceed 4x3 tiles.
4. Simplification to fail if bounds of map < 5% of zoomlevel 11 (bound area: 78x58km); 
   i.e. the map is not projected correctly (as sahsuland is not at pressent). Fix
   sahsuland projection (i.e. not 27700).
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

\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
--
-- Functions to enable debug for
--
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl', 
		'rif40_zoom_levels'];
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
	
SELECT * FROM rif40_geo_pkg.rif40_zoom_levels(30);

SELECT * FROM rif40_geo_pkg.rif40_zoom_levels();
/*
SELECT * FROM rif40_geo_pkg.rif40_zoom_levels();
 zoomlevel | latitude |    tiles     | degrees_per_tile | m_x_per_pixel_est | m_x_per_pixel | m_y_per_pixel |   m_x    |   m_y    |      scale
-----------+----------+--------------+------------------+-------------------+---------------+---------------+----------+----------+------------------
         0 |        0 |            1 |              360 |            156412 |        155497 |               | 39807187 |          | 1 in 591,225,112
         1 |        0 |            4 |              180 |             78206 |         77748 |               | 19903593 |          | 1 in 295,612,556
         2 |        0 |           16 |               90 |             39103 |         39136 |         39070 | 10018754 | 10001966 | 1 in 148,800,745
         3 |        0 |           64 |               45 |             19552 |         19568 |         19472 |  5009377 |  4984944 | 1 in 74,400,373
         4 |        0 |          256 |             22.5 |              9776 |          9784 |          9723 |  2504689 |  2489167 | 1 in 37,200,186
         5 |        0 |         1024 |            11.25 |              4888 |          4892 |          4860 |  1252344 |  1244120 | 1 in 18,600,093
         6 |        0 |         4096 |            5.625 |              2444 |          2446 |          2430 |   626172 |   622000 | 1 in 9,300,047
         7 |        0 |        16384 |            2.813 |              1222 |          1223 |          1215 |   313086 |   310993 | 1 in 4,650,023
         8 |        0 |        65536 |            1.406 |               611 |           611 |           607 |   156543 |   155495 | 1 in 2,325,012
         9 |        0 |       262144 |            0.703 |               305 |           306 |           304 |    78272 |    77748 | 1 in 1,162,506
        10 |        0 |      1048576 |            0.352 |               153 |           153 |           152 |    39136 |    38874 | 1 in 581,253
        11 |        0 |      4194304 |            0.176 |                76 |            76 |            76 |    19568 |    19437 | 1 in 290,626
        12 |        0 |     16777216 |            0.088 |                38 |            38 |            38 |     9784 |     9718 | 1 in 145,313
        13 |        0 |     67108864 |            0.044 |                19 |            19 |            19 |     4892 |     4859 | 1 in 72,657
        14 |        0 |    268435456 |            0.022 |               9.5 |           9.6 |           9.5 |     2446 |     2430 | 1 in 36,328
        15 |        0 |   1073741824 |            0.011 |               4.8 |           4.8 |           4.7 |     1223 |     1215 | 1 in 18,164
        16 |        0 |   4294967296 |            0.005 |               2.4 |           2.4 |           2.4 |      611 |      607 | 1 in 9,082
        17 |        0 |  17179869184 |            0.003 |              1.19 |          1.19 |          1.19 |      306 |      304 | 1 in 4,541
        18 |        0 |  68719476736 |           0.0014 |              0.60 |          0.60 |          0.59 |      153 |      152 | 1 in 2,271
        19 |        0 | 274877906944 |          0.00069 |              0.30 |          0.30 |          0.30 |       76 |       76 | 1 in 1,135
(20 rows)

Time: 40.133 ms

Time: 2.648 ms
 */

-- Projection is wrong
SELECT a.geolevel_name, 
       b.st_simplify_tolerance,
	   ST_Distance_Spheroid(
			ST_GeomFromEWKT('SRID=27700;POINT(0 0)'), ST_GeomFromEWKT('SRID=27700;POINT('||b.st_simplify_tolerance||' 0)'),
			'SPHEROID["Airy 1830",6377563.396,299.3249646]') st_simplify_tolerance_in_m,
       COUNT(a.area_id) AS t_areas, 
       SUM(ST_NPoints(a.optimised_geometry)) AS t_points, 
	   SUM(ST_Area(a.optimised_geometry)) AS t_area, 
	   SUM(ST_perimeter(a.optimised_geometry)) AS t_perimeter
  FROM t_rif40_sahsu_geometry a, rif40_geolevels b
 WHERE a.geolevel_name = b.geolevel_name
 GROUP BY b.geolevel_id, a.geolevel_name, b.st_simplify_tolerance
 ORDER BY b.geolevel_id;
  
 
WITH a AS (
	SELECT srid, substring(srtext, position('SPHEROID[' in srtext)) AS spheroid
	FROM spatial_ref_sys	
	WHERE srid IN (27700, 4326)
)
SELECT srid, substring(spheroid, 1, position(',AUTHORITY[' in spheroid)-1)||']' AS spheroid
  FROM a;
 
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
	
DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'Aborting (script being tested)';
	RAISE EXCEPTION 'C20999: Abort';
END;
$$;

END;

--
-- Eof