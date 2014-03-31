-- ************************************************************************
-- *
-- * DO NOT EDIT THIS SCRIPT OR MODIFY THE RIF SCHEMA - USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - Create and populate rif40_geolevels_geometry, intersection and lookup tables (Geographic processing) - SAHSULAND version
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
-- Check database is sahsuland_dev
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'Database check: %', current_database();	
	ELSE
		RAISE EXCEPTION 'C20901: Database check failed: % is not sahsuland_dev', current_database();	
	END IF;
END;
$$;

\echo Create and populate rif40_geolevels_geometry, intersection and lookup tables (Geographic processing)- SAHSULAND version...
--

\set ECHO all
\timing
\set ON_ERROR_STOP OFF
\pset pager on
SELECT rif40_geo_pkg.drop_rif40_geolevels_geometry_tables('SAHSU');
SELECT rif40_geo_pkg.drop_rif40_geolevels_lookup_tables('SAHSU');
DROP TABLE sahsuland_geography_orig;
\set ON_ERROR_STOP ON

--
-- Comment out this for more debug and do not exit on error
--

--\set ON_ERROR_STOP OFF
\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
--
	rif40_geo_pkg_functions 	VARCHAR[] := ARRAY['lf_check_rif40_hierarchy_lookup_tables', 
							'populate_rif40_geometry_tables', 
							'populate_hierarchy_table', 
							'create_rif40_geolevels_geometry_tables',
							'add_population_to_rif40_geolevels_geometry',
							'fix_null_geolevel_names',
							'rif40_ddl',
							'simplify_geometry'];
	l_function 			VARCHAR;
	i				INTEGER:=0;
--
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
BEGIN
--
	stp:=clock_timestamp();
--
-- Call init function is case called from main build scripts
--
	PERFORM rif40_sql_pkg.rif40_startup();
--
-- Turn on some debug
--
        PERFORM rif40_log_pkg.rif40_log_setup();
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
	FOREACH l_function IN ARRAY rif40_geo_pkg_functions LOOP
		RAISE INFO 'Enable debug for function: %', l_function;
		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
	END LOOP;
--
-- These are the new T_RIF40_<GEOGRAPHY>_GEOMETRY tables and
-- new T_RIF40_GEOLEVELS_GEOMETRY_<GEOGRAPHY>_<GEOELVELS> partitioned tables
--
	PERFORM rif40_geo_pkg.create_rif40_geolevels_geometry_tables('SAHSU');
--
-- Create and populate rif40_geolevels lookup and create hierarchy tables 
--
	PERFORM rif40_geo_pkg.create_rif40_geolevels_lookup_tables('SAHSU');
--
-- Populate geometry tables
--
	PERFORM rif40_geo_pkg.populate_rif40_geometry_tables('SAHSU');
--
-- Simplify geometry
--
-- Must be done before to avoid invalid geometry errors in intersection code:
--
-- psql:rif40_geolevels_ew01_geometry.sql:174: ERROR:  Error performing intersection: TopologyException: found non-noded intersection between LINESTRING (-2.9938 53.3669, -2.98342 53.367) and LINESTRING (-2.98556 53.367, -2.98556 53.367) at -2.9855578257498334 53.366966593247653
--
	PERFORM rif40_geo_pkg.simplify_geometry('SAHSU', 10 /* l_min_point_resolution [1] */);
--
-- Populate hierarchy tables
--
-- The following SQL snippet from rif40_geo_pkg.populate_hierarchy_table() 
-- causes ERROR:  invalid join selectivity: 1.000000 in PostGIS 2.1.1 (fixed in 2.2.1/2.1.2 - to be release May 3rd 2014)
--
-- See: http://trac.osgeo.org/postgis/ticket/2543
--
-- SELECT a2.area_id AS level2, a3.area_id AS level3,
--       ST_Area(a3.optimised_geometry) AS a3_area,
--       ST_Area(ST_Intersection(a2.optimised_geometry, a3.optimised_geometry)) AS a23_area
--  FROM t_rif40_geolevels_geometry_sahsu_level3 a3, t_rif40_geolevels_geometry_sahsu_level2 a2  
-- WHERE ST_Intersects(a2.optimised_geometry, a3.optimised_geometry);
--
--	PERFORM rif40_geo_pkg.populate_hierarchy_table('SAHSU'); 
--
-- Add denominator population table to geography geolevel geomtry data
--
--	PERFORM rif40_geo_pkg.add_population_to_rif40_geolevels_geometry('SAHSU', 'SAHSULAND_POP'); 
--
-- Fix NULL geolevel names in geography geolevel geometry and lookup table data 
--
--	PERFORM rif40_geo_pkg.fix_null_geolevel_names('SAHSU'); 
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	RAISE INFO 'Processed SAHSU geography: %s', took;
--
END;
$$;

--
-- Temporary workaround for: http://trac.osgeo.org/postgis/ticket/2543
--
\COPY sahsuland_geography(level1, level2, level3, level4) FROM  '../sahsuland/data/sahsuland_geography.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
CREATE UNIQUE INDEX sahsuland_geography_pk ON sahsuland_geography(level4);
-- For vi's benefit

DO LANGUAGE plpgsql $$
BEGIN
	PERFORM rif40_geo_pkg.add_population_to_rif40_geolevels_geometry('SAHSU', 'SAHSULAND_POP'); 
	PERFORM rif40_geo_pkg.fix_null_geolevel_names('SAHSU'); 
--
END;
$$;

ALTER TABLE "sahsuland_level1" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "sahsuland_level2" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "sahsuland_level3" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "sahsuland_level4" ALTER COLUMN name  SET NOT NULL; 

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL1';

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL2';

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL3'
 ORDER BY 2 LIMIT 20;

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL4'
 ORDER BY 2 LIMIT 20;

--
-- Make columns NOT NULL - Cannot be done with PL/pgsql - causes:
-- cannot ALTER TABLE "sahsuland_level1" because it is being used by active queries in this session
--

ALTER TABLE "t_rif40_sahsu_geometry" ALTER COLUMN name  SET NOT NULL;

--
-- Vaccum ANALYZE tables
--
VACUUM ANALYZE VERBOSE sahsuland_geography;
VACUUM ANALYZE VERBOSE t_rif40_sahsu_geometry;
VACUUM ANALYZE VERBOSE sahsuland_level1;
VACUUM ANALYZE VERBOSE sahsuland_level2;
VACUUM ANALYZE VERBOSE sahsuland_level3;
VACUUM ANALYZE VERBOSE sahsuland_level4;

--\q

CREATE TABLE sahsuland_geography_orig AS SELECT * FROM sahsuland_geography;
TRUNCATE TABLE sahsuland_geography_orig;
--
-- Import geospatial intersction files
--
\COPY sahsuland_geography_orig(level1, level2, level3, level4) FROM  '../sahsuland/data/sahsuland_geography.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
CREATE UNIQUE INDEX sahsuland_geography_orig_pk ON sahsuland_geography_orig(level4);


--
-- For vi's benefit

--
-- This algorithm implments the prototype in: ../postgres/gis_intersection_prototype.sql. The SAHSU alogoritm differs in that
-- Where intersections overlap (e.g. level4 appears in more than 1 level3) the level3 with the greatest intersected area is chosen 
-- This is differed to ARCGis - see the prototype file for a more detailed investigation
--
-- \i ../postgres/gis_intersection_prototype.sql
-- \COPY sahsuland_geography(level1, level2, level3, level4) FROM  '../sahsuv3_v4/data/sahsuland_geography.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
WITH a_missing AS (
	SELECT level1, level2, level3, level4
	  FROM sahsuland_geography
	EXCEPT
	SELECT level1, level2, level3, level4
	  FROM sahsuland_geography_orig
),
a_extra AS (
	SELECT level1, level2, level3, level4
	  FROM sahsuland_geography_orig
	EXCEPT
	SELECT level1, level2, level3, level4
	  FROM sahsuland_geography
), b AS (
	SELECT a_missing.level4, 
               a_missing.level3 AS missing_level3, 
               a_extra.level3 AS extra_level3, 
               a_missing.level2 AS missing_level2, 
               a_extra.level2 AS extra_level2, 
               a_missing.level1 AS missing_level1, 
               a_extra.level1 AS extra_level1 
	  FROM a_missing
		LEFT OUTER JOIN a_extra ON (a_extra.level4 = a_missing.level4)
)
SELECT level4,
       CASE WHEN missing_level3 = extra_level3 THEN 'Same: '||extra_level3 ELSE 'Move: '||extra_level3||'=>'||missing_level3 END AS level3,
       CASE WHEN missing_level2 = extra_level2 THEN 'Same: '||extra_level2 ELSE 'Move: '||extra_level2||'=>'||missing_level2 END AS level2,
       CASE WHEN missing_level1 = extra_level1 THEN 'Same: '||extra_level1 ELSE 'Move: '||extra_level1||'=>'||missing_level1 END AS level1
  FROM b
ORDER BY 1, 2, 3, 4;

DROP TABLE IF EXISTS sahsuland_geography_orig;

/*

SELECT level4, COUNT(DISTINCT(level3)) AS total
  FROM sahsuland_geography
 GROUP BY level4
HAVING COUNT(DISTINCT(level3)) > 1;


SELECT level3 FROM sahsuland_geography EXCEPT SELECT level3 FROM sahsuland_level3;
SELECT level3 FROM x_sahsu_level3 WHERE level3 IN (SELECT level3 FROM sahsuland_geography EXCEPT SELECT level3 FROM sahsuland_level3);
SELECT level4 FROM sahsuland_level4 EXCEPT SELECT level4 FROM sahsuland_geography;
 */

SELECT geography, geolevel_name, geolevel_id, shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance
  FROM t_rif40_geolevels
 ORDER BY geography, geolevel_id;

/*
 geography | geolevel_name | geolevel_id |  shapefile_table  | shapefile_area_id_column | shapefile_desc_column | st_simplify_tolerance 
-----------+---------------+-------------+-------------------+--------------------------+-----------------------+-----------------------
 SAHSU     | LEVEL1        |           1 | x_sahsu_level1    | level1                   |                       |                   500
 SAHSU     | LEVEL2        |           2 | x_sahsu_level2    | level2                   |                       |                   100
 SAHSU     | LEVEL3        |           3 | x_sahsu_level3    | level3                   |                       |                    50
 SAHSU     | LEVEL4        |           4 | x_sahsu_level4    | level4                   |                       |                    10
(4 rows)
 */


SELECT geography, geolevel_name, avg_npoints_geom, avg_npoints_opt, file_geojson_len, leg_geom, leg_opt
  FROM t_rif40_geolevels
 ORDER BY geography, geolevel_id;

\pset pager off
\dS+ t_rif40_sahsu_geometry
\dS+ sahsuland_geography

-- \dS+ rif40_geolevels_geometry

\echo Created and populated rif40_geolevels_geometry, intersection and lookup tables (Geographic processing)- SAHSULAND version.
\q

--
-- Create some GeoJSON tester Java files
--
\copy (SELECT * FROM rif40_geo_pkg.get_geojson_as_js('SAHSU', 'LEVEL4', 'LEVEL2', '01.004' /* Hambly */)) to ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.js 

\copy (SELECT encode(ST_asPNG(ST_asRaster(shapefile_geometry, 1000, 1000)), 'hex') AS png FROM t_rif40_sahsu_geometry WHERE geolevel_name = 'LEVEL4' AND area_id IN (SELECT DISTINCT level4 FROM sahsuland_geography WHERE level2 = '01.004')) to ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.hex
\! xxd -p -r ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.hex ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.png
-- \! display -verbose ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.png &

-- 
-- Eof
