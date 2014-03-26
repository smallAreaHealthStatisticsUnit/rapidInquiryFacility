-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/02/28 10:54:02 $
-- Type: Postgres PSQL script
-- $RCSfile: rif40_geolevels_sahsuland_geometry.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/psql_scripts/rif40_geolevels_sahsuland_geometry.sql,v $
-- $Revision: 1.7 $
-- $Id: rif40_geolevels_sahsuland_geometry.sql,v 1.7 2014/02/28 10:54:02 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) -  Create and populate rif40_geolevels_geometry, intersection and lookup tables (Geographic processing) - SAHSULAND version
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
-- $Log: rif40_geolevels_sahsuland_geometry.sql,v $
-- Revision 1.7  2014/02/28 10:54:02  peterh
--
-- Further work on transfer of SAHSUland to github. sahsuland build scripts Ok, UK91 geog added.
--
-- Revision 1.6  2014/02/14 17:18:40  peterh
--
-- Clean build. Issue with ST_simplify(), intersection code and UK geography (to be resolved)
-- Fully commented (and check now works)
--
-- Stubs for range/hash partitioning added
--
-- Revision 1.5  2014/02/11 14:56:24  peterh
-- Baseline after simplifcation integration and testing of new Geo setup scripts
--
-- Revision 1.4  2014/01/31 17:03:12  peterh
-- Completed GEO simplification apart from joined_join_seq=0, line_length=0 pairs
-- in simplification_lines_join_duplicates
--
-- Revision 1.3  2014/01/22 16:53:24  peterh
-- Baseline prior to extending join to exterior lines in simplification modules
--
-- Revision 1.2  2014/01/14 08:59:47  peterh
--
-- Baseline prior to adding multipolygon support for simplification
--
-- Revision 1.1  2013/03/14 17:35:20  peterh
-- Baseline for TX to laptop
--
--
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
	PERFORM rif40_geo_pkg.populate_hierarchy_table('SAHSU'); 
--
-- Add denominator population table to geography geolevel geomtry data
--
	PERFORM rif40_geo_pkg.add_population_to_rif40_geolevels_geometry('SAHSU', 'SAHSULAND_POP'); 
--
-- Fix NULL geolevel names in geography geolevel geometry and lookup table data 
--
	PERFORM rif40_geo_pkg.fix_null_geolevel_names('SAHSU'); 
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	RAISE INFO 'Processed SAHSU geography: %s', took;
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

\q

--
-- Create some GeoJSON tester Java files
--
\copy (SELECT * FROM rif40_geo_pkg.get_geojson_as_js('SAHSU', 'LEVEL4', 'LEVEL2', '01.004' /* Hambly */)) to ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.js 

\copy (SELECT encode(ST_asPNG(ST_asRaster(shapefile_geometry, 1000, 1000)), 'hex') AS png FROM t_rif40_sahsu_geometry WHERE geolevel_name = 'LEVEL4' AND area_id IN (SELECT DISTINCT level4 FROM sahsuland_geography WHERE level2 = '01.004')) to ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.hex
\! xxd -p -r ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.hex ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.png
-- \! display -verbose ../postgres/GeoJSON_tester/geojson_data/sahsu_level2.png &

\echo Created and populated rif40_geolevels_geometry, intersection and lookup tables (Geographic processing)- SAHSULAND version.

-- 
-- Eof
