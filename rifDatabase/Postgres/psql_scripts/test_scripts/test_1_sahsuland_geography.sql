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
-- Start transaction
--
BEGIN;

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'test_1_sahsuland_geography.sql: T1__01: User check: %', user;	
	ELSE
		RAISE EXCEPTION 'test_1_sahsuland_geography.sql: T1__02: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

\set ndebug_level '''XXXX':debug_level''''
SET rif40.debug_level TO :ndebug_level;
	
/*	
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
 */
 
--
-- There is a fault in the original intersection where islands were not handled correctly, so sahsuland_geography was updated
-- Note that the numbers now appear to be wrong!
--
-- \COPY sahsuland_geography(level1, level2, level3, level4) TO '../sahsuland/data/sahsuland_geography.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
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
\pset title 'SAHSULAND geography where level4 level3 substring != level3 (intersction fault in old RIF)'
SELECT * FROM sahsuland_geography
 WHERE substr(level4, 1, 13) != level3;
\pset title 
  
DROP TABLE IF EXISTS sahsuland_geography_orig;
CREATE TABLE sahsuland_geography_orig AS SELECT * FROM sahsuland_geography;
TRUNCATE TABLE sahsuland_geography_orig;
--
-- Import geospatial intersction files
--
\COPY sahsuland_geography_orig(level1, level2, level3, level4) FROM  '../sahsuland/data/sahsuland_geography.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
--
-- For vi's benefit
CREATE UNIQUE INDEX sahsuland_geography_orig_pk ON sahsuland_geography_orig(level4);

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
		RAISE EXCEPTION 'test_1_sahsuland_geography.sql: T1-01: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'T1--02: test_1_sahsuland_geography.sql: debug level parameter="%"', debug_level::Text;
	END IF;
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_1_sahsuland_geography.sql: T1--03: Invslid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_pkg_functions LOOP
			RAISE INFO 'T1--04: test_1_sahsuland_geography.sql: Enable debug for function: %', l_function;
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
		END LOOP;
	END IF;
--
-- Validate 2 sahsuland_geography tables are the same
--
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__04(sahsuland_geography)' /* Test tag */, 'sahsuland_geography', 'sahsuland_geography_orig');
--
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT level4 FROM sahsuland_level4;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT level4 FROM sahsuland_geography;
	CREATE TEMPORARY TABLE test_1_temp_3 AS
	SELECT DISTINCT level4 FROM x_sahsu_level4;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__05(level4)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_2');
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__06(x_sahsu_level4)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_3');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
	DROP TABLE test_1_temp_3;
--
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT level3 FROM sahsuland_level3;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT DISTINCT level3 FROM sahsuland_geography;
	CREATE TEMPORARY TABLE test_1_temp_3 AS
	SELECT DISTINCT level3 FROM x_sahsu_level3;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__06(level3)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_2');
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__07(x_sahsu_level3)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_3');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
	DROP TABLE test_1_temp_3;
--
	CREATE TEMPORARY TABLE test_1_temp_1 AS
	SELECT level2 FROM sahsuland_level2;
	CREATE TEMPORARY TABLE test_1_temp_2 AS
	SELECT DISTINCT level2 FROM sahsuland_geography;
	CREATE TEMPORARY TABLE test_1_temp_3 AS
	SELECT DISTINCT level2 FROM x_sahsu_level2;
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__08(level2)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_2');
	PERFORM rif40_sql_pkg.rif40_table_diff('T1__09(x_sahsu_level2)' /* Test tag */, 'test_1_temp_1', 'test_1_temp_3');
	DROP TABLE test_1_temp_1;
	DROP TABLE test_1_temp_2;
	DROP TABLE test_1_temp_3;
--
--	RAISE EXCEPTION 'TEST Abort';
END;
$$;

\pset title 'Movement diff sahsuland_geography_orig vs. sahsuland_geography'
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
\pset title

DO LANGUAGE plpgsql $$
DECLARE
    c0 CURSOR FOR
		WITH a AS (
			SELECT COUNT(a.level4) AS total_sahsuland_geography
			  FROM sahsuland_geography a
		), b AS (
			SELECT COUNT(b.level4) AS total_sahsuland_geography_orig
			  FROM sahsuland_geography_orig b
		)
		SELECT a.total_sahsuland_geography, b.total_sahsuland_geography_orig
		  FROM a, b;
	c1 CURSOR FOR 
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
	IF c0_rec.total_sahsuland_geography = c0_rec.total_sahsuland_geography_orig THEN
			RAISE INFO 'test_1_sahsuland_geography.sql: T1__03: sahsuland_geography: % (under test) and sahsuland_geography_orig: % (reference version) have the same row counts',
				c0_rec.total_sahsuland_geography, c0_rec.total_sahsuland_geography_orig;
	ELSE
			RAISE EXCEPTION 'test_1_sahsuland_geography.sql: T1__04: sahsuland_geography: % (under test) and sahsuland_geography_orig: % (reference version) have differing row counts',
				c0_rec.total_sahsuland_geography, c0_rec.total_sahsuland_geography_orig;
	END IF;
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total_level3_moved > 0 OR c1_rec.total_level2_moved > 0 OR c1_rec.total_level1_moved > 0 THEN
			RAISE EXCEPTION 'test_1_sahsuland_geography.sql: T1__05: sahsuland_geography (under test) and sahsuland_geography_orig (reference version) are NOT the same; movement detected; level3: % ,level2: %, level1: %', 
				c1_rec.total_level3_moved::Text, 
				c1_rec.total_level2_moved::Text, 
				c1_rec.total_level1_moved::Text;	
	ELSE
			RAISE INFO 'test_1_sahsuland_geography.sql: T1__06: Geospatial tables: sahsuland_geography (under test) and sahsuland_geography_orig (reference version) are the same';
	END IF;
END;
$$;

DROP TABLE IF EXISTS sahsuland_geography_orig;

/*

SELECT level4, COUNT(DISTINCT(level3)) AS total
  FROM sahsuland_geography
 GROUP BY level4
HAVING COUNT(DISTINCT(level3)) > 1;


SELECT level3 FROM sahsuland_geography EXCEPT SELECT level3 FROM sahsuland_level3;
SELECT level3 FROM x_sahsu_level3 WHERE level3 IN (SELECT level3 FROM sahsuland_geography EXCEPT SELECT level3 FROM sahsuland_level3);
SELECT level3 FROM x_sahsu_level3 EXCEPT SELECT level3 FROM sahsuland_level3;
SELECT level4 FROM sahsuland_level4 EXCEPT SELECT level4 FROM sahsuland_geography;
SELECT level4 FROM x_sahsu_level4 WHERE level4 IN (SELECT level4 FROM sahsuland_geography EXCEPT SELECT level4 FROM sahsuland_level4);
SELECT level4 FROM x_sahsu_level4 EXCEPT SELECT level4 FROM sahsuland_geography;
 */

\pset title 'T_RIF40_GEOLEVELS a)'
SELECT geography, geolevel_name, geolevel_id, shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance
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

\pset title 'Geolevels: sahsuland_geography'
SELECT COUNT(DISTINCT(level1)) AS level1,
       COUNT(DISTINCT(level2)) AS level2,
       COUNT(DISTINCT(level3)) AS level3,
       COUNT(DISTINCT(level4)) AS level4
  FROM sahsuland_geography;

\pset title 'Geolevels: sahsuland_geography: lookup tables'
WITH a AS (
	SELECT COUNT(level1) AS level1
	   FROM sahsuland_level1
), b AS (
	SELECT COUNT(level2) AS level2
	   FROM sahsuland_level2
), c AS (
	SELECT COUNT(level3) AS level3
	   FROM sahsuland_level3
), d AS (
	SELECT COUNT(level4) AS level4
	   FROM sahsuland_level4
)
SELECT level1,level2, level3, level4
  FROM a, b, c, d;

--
-- Check GID is unique at geolevel
--

 /*
\pset pager off
\dS+ t_rif40_sahsu_geometry
\dS+ sahsuland_geography
\dS+ rif40_geolevels_geometry
 */
 --
 -- End of transaction
 --
 END;
 
\echo Test 1 - SAHSULAND geometry iontersection validation completed OK.

--
-- Eof