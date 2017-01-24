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
-- Rapid Enquiry Facility (RIF) - Test 1: sahsuland geography
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
\set ECHO :echo
\set ON_ERROR_STOP ON
\set VERBOSITY :verbosity

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'test_1_hierarchy_sahsuland.sql: T1__01: User check: %', user;	
	ELSE
		RAISE EXCEPTION 'test_1_hierarchy_sahsuland.sql: T1__02: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

\set ndebug_level '''XXXX':debug_level''''
SET rif40.debug_level TO :ndebug_level;
	
/*	
SELECT area_id, name, area, gid, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL1';

SELECT area_id, name, area, gid, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL2';

SELECT area_id, name, area, gid, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL3'
 ORDER BY 2 LIMIT 20;

SELECT area_id, a.name, area, a.gid, b.gid, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry a
			LEFT OUTER JOIN sahsu_grd_level4 b ON (a.area_id = b.level4)
 WHERE geolevel_name = 'LEVEL4'
 ORDER BY 2 LIMIT 20;
 
 */
 
--
-- There is a fault in the original intersection where islands were not handled correctly, so hierarchy_sahsuland was updated
-- Note that the numbers now appear to be wrong!
--
-- \COPY hierarchy_sahsuland(level1, level2, level3, level4) TO '../sahsuland/data/hierarchy_sahsuland.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
--
-- This algorithm implments the prototype in: ../postgres/gis_intersection_prototype.sql. The SAHSU alogoritm differs in that
-- Where intersections overlap (e.g. level4 appears in more than 1 level3) the level3 with the greatest intersected area is chosen 
-- This is differed to ARCGis - see the prototype file for a more detailed investigation
--
-- \i ../postgres/gis_intersection_prototype.sql
--
/*
 level1 | level2 |    level3     |     level4
--------+--------+---------------+-----------------
 01     | 01.002 | 01.002.001400 | 01.002.001600.3
 01     | 01.002 | 01.002.001500 | 01.002.001300.7
 01     | 01.002 | 01.002.001600 | 01.002.001400.6
 01     | 01.002 | 01.002.001600 | 01.002.001500.1
 01     | 01.002 | 01.002.002300 | 01.002.002100.8
 01     | 01.002 | 01.002.002300 | 01.002.002200.5
 01     | 01.008 | 01.008.003900 | 01.008.003901.1
 01     | 01.008 | 01.008.003900 | 01.008.003901.2
 01     | 01.008 | 01.008.003900 | 01.008.003901.3
 01     | 01.008 | 01.008.003900 | 01.008.003901.4
 01     | 01.008 | 01.008.003900 | 01.008.003901.5
 01     | 01.008 | 01.008.003900 | 01.008.003901.6
 01     | 01.008 | 01.008.003900 | 01.008.003901.7
 01     | 01.008 | 01.008.003900 | 01.008.003901.9
 01     | 01.008 | 01.008.006000 | 01.008.006800.1
 01     | 01.008 | 01.008.006200 | 01.008.006000.7
 01     | 01.008 | 01.008.006800 | 01.008.006200.6
 01     | 01.008 | 01.008.009400 | 01.008.009401.1
 01     | 01.008 | 01.008.009400 | 01.008.009401.2
 01     | 01.008 | 01.008.009400 | 01.008.009401.3
 01     | 01.008 | 01.008.009400 | 01.008.009401.4
 01     | 01.013 | 01.013.016200 | 01.013.016100.2
(22 rows)
 */
\pset title 'SAHSULAND geography WHERE level4 level3 substring != level3 (intersction fault in old RIF)'
SELECT * FROM hierarchy_sahsuland
 WHERE substr(sahsu_grd_level4, 1, 13) != sahsu_grd_level3;
\pset title 
SELECT * FROM hierarchy_sahsuland WHERE sahsu_grd_level3 IN ('01.015.016900', '01.015.016200');

DROP TABLE IF EXISTS hierarchy_sahsuland_orig;
CREATE TABLE hierarchy_sahsuland_orig AS SELECT * FROM hierarchy_sahsuland;
TRUNCATE TABLE hierarchy_sahsuland_orig;
--
-- Import geospatial intersection files
--
\COPY hierarchy_sahsuland_orig(sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4) FROM  '../sahsuland/data/sahsuland_geography.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
--
-- For vi's benefit
CREATE UNIQUE INDEX hierarchy_sahsuland_orig_pk ON hierarchy_sahsuland_orig(sahsu_grd_level4);

SELECT * FROM hierarchy_sahsuland
EXCEPT
SELECT * FROM hierarchy_sahsuland_orig
ORDER BY 1, 2, 3, 4;

SELECT * FROM hierarchy_sahsuland_orig
EXCEPT
SELECT * FROM hierarchy_sahsuland
ORDER BY 1, 2, 3, 4;
/*
DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'Aborting (script being tested)';
	RAISE EXCEPTION 'C20999: Abort';
END;
$$;
 */
--\set VERBOSITY verbose
DO LANGUAGE plpgsql $$
DECLARE
	c4sm CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c4sm_rec RECORD;
--
	old_study_id	INTEGER;
	new_study_id	INTEGER;
--
	rif40_pkg_functions 		VARCHAR[] := ARRAY[
				'rif40_table_diff'];
	l_function 			VARCHAR;
	debug_level		INTEGER;
BEGIN
	OPEN c4sm;
	FETCH c4sm INTO c4sm_rec;
	CLOSE c4sm;
--
-- Test parameter
--
	IF c4sm_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'test_1_hierarchy_sahsuland.sql: T1-01: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'T1--02: test_1_hierarchy_sahsuland.sql: debug level parameter="%"', debug_level::Text;
	END IF;
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		RAISE INFO 'T1--04: NULL debug_level';
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_1_hierarchy_sahsuland.sql: T1--03: Invalid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
		RAISE INFO 'T1--04: debug_level %', debug_level;
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_pkg_functions LOOP
			RAISE INFO 'T1--05: test_1_hierarchy_sahsuland.sql: Enable debug for function: %', l_function;
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
		END LOOP;
	ELSE
		RAISE INFO 'T1--04: debug_level %', debug_level;
	END IF;
--
-- Validate 2 hierarchy_sahsuland tables are the same
--
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__06(hierarchy_sahsuland)' /* Test tag */, 'hierarchy_sahsuland', 'hierarchy_sahsuland_orig');
--
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT sahsu_grd_level4 FROM lookup_sahsu_grd_level4;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT sahsu_grd_level4 FROM hierarchy_sahsuland;
--	CREATE TEMPORARY TABLE test_1_temp_3 AS
--	SELECT DISTINCT level4 FROM x_sahsu_level4;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__07(level4)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_2');
--	PERFORM rif40_sql_pkg.rif40_table_diff('T1__08(x_sahsu_level4)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_3');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
--	DROP TABLE test_1_temp_3;
--
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT sahsu_grd_level3 FROM lookup_sahsu_grd_level3;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT DISTINCT sahsu_grd_level3 FROM hierarchy_sahsuland;
--	CREATE TEMPORARY TABLE test_1_temp_3 AS
--	SELECT DISTINCT level3 FROM x_sahsu_level3;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__09(level3)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_2');
--	PERFORM rif40_sql_pkg.rif40_table_diff('T1__10(x_sahsu_level3)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_3');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
--  DROP TABLE test_1_temp_3;
--
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT sahsu_grd_level2 FROM lookup_sahsu_grd_level2;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT DISTINCT sahsu_grd_level2 FROM hierarchy_sahsuland;
--	CREATE TEMPORARY TABLE test_1_temp_3 AS
--	SELECT DISTINCT level2 FROM x_sahsu_level2;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__11(level2)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_2');
--	PERFORM rif40_sql_pkg.rif40_table_diff('T1__12(x_sahsu_level2)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_3');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
--	DROP TABLE test_1_temp_3;
--
-- Tests 13-16 gid match
--
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT sahsu_grd_level1 AS area_id FROM lookup_sahsu_grd_level1;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT areaid AS area_id
	  FROM geometry_sahsuland
	 WHERE geolevel_id = 1  AND zoomlevel = 11;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__13(level1 gid match: sahsu_grd_level1, t_rif40_sahsu_geometry[LEVEL1])' /* Test tag */, 
		'test_1_temp_1', 'test_1_temp_2');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
	
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT sahsu_grd_level2 AS area_id FROM lookup_sahsu_grd_level2;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT areaid AS area_id
	  FROM geometry_sahsuland 
	 WHERE geolevel_id = 2 AND zoomlevel = 11;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__14(level2 gid match: sahsu_grd_level2, geometry_sahsuland[2])' /* Test tag */, 
		'test_1_temp_1', 'test_1_temp_2');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
	
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT sahsu_grd_level3 AS area_id FROM lookup_sahsu_grd_level3;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT areaid AS area_id
	  FROM geometry_sahsuland 
	 WHERE geolevel_id = 3 AND zoomlevel = 11;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__15(level3 gid match: sahsu_grd_level3, geometry_sahsuland[3])' /* Test tag */, 
		'test_1_temp_1', 'test_1_temp_2');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
	
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT sahsu_grd_level4 AS area_id FROM lookup_sahsu_grd_level4;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT areaid AS area_id
	  FROM geometry_sahsuland 
	 WHERE geolevel_id = 4 AND zoomlevel = 11;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__16(level4 gid match: sahsu_grd_level4, geometry_sahsuland[4])' /* Test tag */, 
		'test_1_temp_1', 'test_1_temp_2');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
--
--	RAISE EXCEPTION 'TEST Abort';
END;
$$;

\pset title 'Movement diff hierarchy_sahsuland_orig vs. hierarchy_sahsuland'
WITH a_missing AS (
	SELECT sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4
	  FROM hierarchy_sahsuland
	EXCEPT
	SELECT sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4
	  FROM hierarchy_sahsuland_orig
),
a_extra AS (
	SELECT sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4
	  FROM hierarchy_sahsuland_orig
	EXCEPT
	SELECT sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4
	  FROM hierarchy_sahsuland
), b AS (
	SELECT a_missing.sahsu_grd_level4, 
               a_missing.sahsu_grd_level3 AS missing_level3, 
               a_extra.sahsu_grd_level3 AS extra_level3, 
               a_missing.sahsu_grd_level2 AS missing_level2, 
               a_extra.sahsu_grd_level2 AS extra_level2, 
               a_missing.sahsu_grd_level1 AS missing_level1, 
               a_extra.sahsu_grd_level1 AS extra_level1 
	  FROM a_missing
		LEFT OUTER JOIN a_extra ON (a_extra.sahsu_grd_level4 = a_missing.sahsu_grd_level4)
)
SELECT sahsu_grd_level4,
       CASE WHEN missing_level3 = extra_level3 THEN 'Same: '||extra_level3 ELSE 'Move: '||extra_level3||'=>'||missing_level3 END AS level3,
       CASE WHEN missing_level2 = extra_level2 THEN 'Same: '||extra_level2 ELSE 'Move: '||extra_level2||'=>'||missing_level2 END AS level2,
       CASE WHEN missing_level1 = extra_level1 THEN 'Same: '||extra_level1 ELSE 'Move: '||extra_level1||'=>'||missing_level1 END AS level1
  FROM b
ORDER BY 1, 2, 3, 4;

\pset title

DO LANGUAGE plpgsql $$
DECLARE
    c0 CURSOR FOR
		WITH a AS (
			SELECT COUNT(a.sahsu_grd_level4) AS total_hierarchy_sahsuland
			  FROM hierarchy_sahsuland a
		), b AS (
			SELECT COUNT(b.sahsu_grd_level4) AS total_hierarchy_sahsuland_orig
			  FROM hierarchy_sahsuland_orig b
		)
		SELECT a.total_hierarchy_sahsuland, b.total_hierarchy_sahsuland_orig
		  FROM a, b;
	c1 CURSOR FOR 
		WITH a_missing AS (
			SELECT sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4
			  FROM hierarchy_sahsuland
			EXCEPT
			SELECT sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4
			  FROM hierarchy_sahsuland_orig
		),
		a_extra AS (
			SELECT sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4
			  FROM hierarchy_sahsuland_orig
			EXCEPT
			SELECT sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4
			  FROM hierarchy_sahsuland
		), b AS (
			SELECT a_missing.sahsu_grd_level4, 
				   a_missing.sahsu_grd_level3 AS missing_level3, 
                   a_extra.sahsu_grd_level3 AS extra_level3, 
                   a_missing.sahsu_grd_level2 AS missing_level2, 
                   a_extra.sahsu_grd_level2 AS extra_level2, 
                   a_missing.sahsu_grd_level1 AS missing_level1, 
                   a_extra.sahsu_grd_level1 AS extra_level1 
			  FROM a_missing
					LEFT OUTER JOIN a_extra ON (a_extra.sahsu_grd_level4 = a_missing.sahsu_grd_level4)
		)
		SELECT SUM(CASE WHEN missing_level3 = extra_level3 THEN 0 ELSE 1 END) AS total_level3_moved,
			   SUM(CASE WHEN missing_level2 = extra_level2 THEN 0 ELSE 1 END) AS total_level2_moved,
			   SUM(CASE WHEN missing_level1 = extra_level1 THEN 0 ELSE 1 END) AS total_level1_moved
		FROM b;
--
	c0_rec RECORD;
	c1_rec RECORD;
BEGIN
	OPEN c0;
	FETCH c0 INTO c0_rec;
	CLOSE c0;
	IF c0_rec.total_hierarchy_sahsuland = c0_rec.total_hierarchy_sahsuland_orig THEN
			RAISE INFO 'test_1_hierarchy_sahsuland.sql: T1__03: hierarchy_sahsuland: % (under test) and hierarchy_sahsuland_orig: % (reference version) have the same row counts',
				c0_rec.total_hierarchy_sahsuland, c0_rec.total_hierarchy_sahsuland_orig;
	ELSE
			RAISE EXCEPTION 'test_1_hierarchy_sahsuland.sql: T1__04: hierarchy_sahsuland: % (under test) and hierarchy_sahsuland_orig: % (reference version) have differing row counts',
				c0_rec.total_hierarchy_sahsuland, c0_rec.total_hierarchy_sahsuland_orig;
	END IF;
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total_level3_moved > 0 OR c1_rec.total_level2_moved > 0 OR c1_rec.total_level1_moved > 0 THEN
			RAISE EXCEPTION 'test_1_hierarchy_sahsuland.sql: T1__05: hierarchy_sahsuland (under test) and hierarchy_sahsuland_orig (reference version) are NOT the same; movement detected; level3: % ,level2: %, level1: %', 
				c1_rec.total_level3_moved::Text, 
				c1_rec.total_level2_moved::Text, 
				c1_rec.total_level1_moved::Text;	
	ELSE
			RAISE INFO 'test_1_hierarchy_sahsuland.sql: T1__06: Geospatial tables: hierarchy_sahsuland (under test) and hierarchy_sahsuland_orig (reference version) are the same';
	END IF;
END;
$$;

DROP TABLE IF EXISTS hierarchy_sahsuland_orig;

/*

SELECT level4, COUNT(DISTINCT(level3)) AS total
  FROM hierarchy_sahsuland
 GROUP BY level4
HAVING COUNT(DISTINCT(level3)) > 1;


SELECT level3 FROM hierarchy_sahsuland EXCEPT SELECT level3 FROM sahsu_grd_level3;
SELECT level3 FROM x_sahsu_level3 WHERE level3 IN (SELECT level3 FROM hierarchy_sahsuland EXCEPT SELECT level3 FROM sahsu_grd_level3);
SELECT level3 FROM x_sahsu_level3 EXCEPT SELECT level3 FROM sahsu_grd_level3;
SELECT level4 FROM sahsu_grd_level4 EXCEPT SELECT level4 FROM hierarchy_sahsuland;
SELECT level4 FROM x_sahsu_level4 WHERE level4 IN (SELECT level4 FROM hierarchy_sahsuland EXCEPT SELECT level4 FROM sahsu_grd_level4);
SELECT level4 FROM x_sahsu_level4 EXCEPT SELECT level4 FROM hierarchy_sahsuland;
 */

\pset title 'T_RIF40_GEOLEVELS a)'
SELECT geography, geolevel_name, geolevel_id, shapefile_table, shapefile_area_id_column, shapefile_desc_column
  FROM t_rif40_geolevels
 WHERE geography = 'SAHSU'
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

\pset title 'T_RIF40_GEOLEVELS b)'
SELECT geography, geolevel_name, avg_npoints_geom, avg_npoints_opt, file_geojson_len, leg_geom, leg_opt
  FROM t_rif40_geolevels
 WHERE geography = 'SAHSU'
 ORDER BY geography, geolevel_id;
\pset title

\pset title 'Geolevels: hierarchy_sahsuland'
SELECT COUNT(DISTINCT(sahsu_grd_level1)) AS sahsu_grd_level1,
       COUNT(DISTINCT(sahsu_grd_level2)) AS sahsu_grd_level2,
       COUNT(DISTINCT(sahsu_grd_level3)) AS sahsu_grd_level3,
       COUNT(DISTINCT(sahsu_grd_level4)) AS sahsu_grd_level4
  FROM hierarchy_sahsuland;

\pset title 'Geolevels: hierarchy_sahsuland: lookup tables'
WITH a AS (
	SELECT COUNT(sahsu_grd_level1) AS sahsu_grd_level1
	   FROM lookup_sahsu_grd_level1
), b AS (
	SELECT COUNT(sahsu_grd_level2) AS sahsu_grd_level2
	   FROM lookup_sahsu_grd_level2
), c AS (
	SELECT COUNT(sahsu_grd_level3) AS sahsu_grd_level3
	   FROM lookup_sahsu_grd_level3
), d AS (
	SELECT COUNT(sahsu_grd_level4) AS sahsu_grd_level4
	   FROM lookup_sahsu_grd_level4
)
SELECT sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4
  FROM a, b, c, d;
/*

SELECT oid, lowrite(lo_open(oid, 131072), png) As num_bytes
 FROM 
 ( VALUES (lo_create(0), 
   ST_AsPNG( (SELECT rast FROM aerials.boston WHERE rid=1) ) 
  ) ) As v(oid,png);
-- you'll get an output something like --
   oid   | num_bytes
---------+-----------
 2630819 |     74860
 
-- next note the oid and do this replacing the c:/test.png to file path location
-- on your local computer
 \lo_export 2630819 'C:/temp/aerial_samp.png'
 
-- this deletes the file from large object storage on db
SELECT lo_unlink(2630819);

 */
--
-- Check GID is unique at geolevel
--

 /*
\pset pager off
\dS+ t_rif40_sahsu_geometry
\dS+ hierarchy_sahsuland
\dS+ rif40_geolevels_geometry
 */
 
\echo Test 1 - SAHSULAND geometry intersection validation completed OK.

--
-- Eof