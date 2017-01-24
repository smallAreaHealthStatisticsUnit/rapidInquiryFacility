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
-- Rapid Enquiry Facility (RIF) - Test 7: Middleware tests 2 for alter 2
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

--
-- Call commomn setup to run as testuser (passed parameter)
--
\i ../psql_scripts/test_scripts/common_setup.sql

\echo Test 7: Middleware tests 2 for alter 2...

\set ndebug_level '''XXXX':debug_level''''
SET rif40.debug_level TO :ndebug_level;
--
-- Start transaction
--
BEGIN;
DO LANGUAGE plpgsql $$
DECLARE
	c4sm CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c4sm_rec RECORD;
--
-- Functions to enable debug for
--
	rif40_xml_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl', 'rif40_GetMapAreas',
		'rif40_getAllAttributesForGeoLevelAttributeTheme', 'rif40_GetMapAreaAttributeValue',
		'rif40_GetMapAreaAttributeValue','rif40_CreateMapAreaAttributeSource'];
	l_function				VARCHAR;
--
	debug_level				INTEGER;
BEGIN
	OPEN c4sm;
	FETCH c4sm INTO c4sm_rec;
	CLOSE c4sm;
--
-- Test parameter
--
	IF c4sm_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'test_7_middleware_2.sql: T7--01: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'test_7_middleware_2.sql: T7--02: debug level parameter="%"', debug_level::Text;
	END IF;
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_7_middleware_2.sql: T7--03: Invslid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_xml_pkg_functions LOOP
			RAISE INFO 'test_7_middleware_2.sql: T7--06: Enable debug for function: %', l_function;
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG'||debug_level::Text);
		END LOOP;
	END IF;
--
-- Call init function is case called from main build scripts
--
	PERFORM rif40_sql_pkg.rif40_startup();
END;
$$;

--
-- Dump all attributes and themes
--
\pset title 'Dump all attributes and themes'
WITH a AS (
	SELECT e.enumlabel, e.enumsortorder
      FROM pg_type t, pg_enum e
 	 WHERE t.oid = e.enumtypid
	   AND  t.typname = 'rif40_geolevelattributetheme'
	 ORDER BY enumsortorder
)	
SELECT b.theme, b.attribute_source, b.attribute_name, b.name_description, b.ordinal_position, b.is_numeric
  FROM a, rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(
	'SAHSULAND'::VARCHAR, 'SAHSU_GRD_LEVEL4'::VARCHAR, a.enumlabel::VARCHAR) b;


--
-- Demo 1. Sahsuland cancer. All defaults (i.e. all columns, fetch 1000 rows at offset 0)
--
\pset title 'Create temporary table'
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_2' /* Temporary table */, 
		'SAHSULAND', 'SAHSU_GRD_LEVEL2', 'health', 'sahsuland_cancer');
\pset title 'Demo 1. Sahsuland cancer. All defaults (i.e. all columns, fetch 1000 rows at offset 0)'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(
		'c4getallatt4theme_2' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_2;

--
-- Demo 2. covariate theme; specified columns (forcing re-sort); otherwise defaults
--
\pset title 'Create temporary table'
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_1' /* Temporary table */, 
		'SAHSULAND', 'SAHSU_GRD_LEVEL4', 'covariate', 'sahsuland_covariates_level4', ARRAY['SES', 'year']);
\pset title 'Demo 2. covariate theme; specified columns (forcing re-sort); otherwise defaults'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(
		'c4getallatt4theme_1' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_1;

/*
psql:alter_scripts/v4_0_alter_2.sql:504: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51214] Cursor: c4getallatt4theme_3, geog
raphy: SAHSU, geolevel select: LEVEL2, theme: population, attribute names: [], source: sahsuland_pop; SQL parse took: 00:00:12.027.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

Time: 12174.738 ms
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000000001 | 1989 |           100 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000002 | 1989 |           100 | 01     | 01.001.000100 | 01.001.000100.2 |    30
 01.001  |   1 | 0000000001_0000000003 | 1989 |           100 | 01     | 01.001.000200 | 01.001.000200.1 |    64
 01.001  |   1 | 0000000001_0000000004 | 1989 |           100 | 01     | 01.001.000300 | 01.001.000300.1 |    80
 01.001  |   1 | 0000000001_0000000005 | 1989 |           101 | 01     | 01.001.000100 | 01.001.000100.1 |    34
(5 rows)

Time: 6559.317 ms
 */
--
-- Demo 3: Poor performance on large tables without gid, gid_rowindex built in and a sort
-- REF_CURSOR takes 12 seconds to parse and execute (caused by the rowindex sort) with explain plan
-- FETCH originally took 7 seconds - i.e. copies results (hopefully in server)! Fixed by creating scrollable REFCURSOR
--
-- Only sorting when the attribute list is specified sppeds things up 4x
--
-- Performance is fine on SAHSULAND_POP
--

\timing
--
-- Create temporary table
--
\pset title 'Create temporary table'
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_3' /* Temporary table */, 
		'SAHSULAND', 'SAHSU_GRD_LEVEL2', 'population', 'sahsuland_pop');
--
-- Create REFCURSOR
--
\pset title 'Create REFCURSOR'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_3' /* Temporary table */,
		NULL /* Cursor name - NULL = same as temporary table - must be unique with a TX */, 
		0 /* No offset */, NULL /* No row limit */);
--
-- Fetch tests
--
FETCH FORWARD 5 IN c4getallatt4theme_3 /* 1.3 seconds with no row limit, no sort list, no gid/gid_rowindex columns built in */;
MOVE ABSOLUTE 1000 IN c4getallatt4theme_3 /* move to row 1000 */;
FETCH FORWARD 5 IN c4getallatt4theme_3;
MOVE ABSOLUTE 10000 IN c4getallatt4theme_3 /* move to row 10000 */;
FETCH FORWARD 5 IN c4getallatt4theme_3;
MOVE ABSOLUTE 432958 IN c4getallatt4theme_3 /* move to row 432958 - two from the end */;
FETCH FORWARD 5 IN c4getallatt4theme_3;
--
-- Test cursor close. Does release resources!!!!
--
\pset title 'Test cursor close. Does release resources!!!!'
SELECT rif40_xml_pkg.rif40_closeGetMapAreaAttributeCursor('c4getallatt4theme_3');

--
-- Demo 1+2: resources not released until end of transaction
--
\pset title 'Display session cursors'
SELECT * FROM pg_cursors;

\pset title 'Demo 1+2: resources not released until end of transaction'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_3' /* Temporary table */,
		NULL /* Cursor name - NULL = same as temporary table - must be unique with a TX */, 
		10000 /* Offset */, NULL /* No row limit */);
FETCH FORWARD 5 IN c4getallatt4theme_3;

\pset title 'Delete c4getallatt4theme_3'
SELECT rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource('c4getallatt4theme_3');
--
-- Demo 4: Use of offset and row limit, cursor control using FETCH
--
\pset title 'Create cursor c4getallatt4theme_3'
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_3' /* Temporary table */, 
		'SAHSULAND', 'SAHSU_GRD_LEVEL2', 'population', 'sahsuland_pop');
\pset title 'Demo 4: Use of offset and row limit, cursor control using FETCH'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_3' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_3 /* 12 seconds parse refcursor seconds with 1000 row limit, cursor fetch is fast, no gid/gid_rowindex columns built in */;
MOVE ABSOLUTE 995 IN c4getallatt4theme_3 /* move to row 995 */;
FETCH FORWARD 5 IN c4getallatt4theme_3 /* Fetch last 5 rows */;
--
-- Open second REFCURSOR
--
\pset title 'Open second REFCURSOR'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(
		'c4getallatt4theme_3' /* Temporary table */, 
		'c4getallatt4theme_3b' /* Cursor name - must be unique with a TX */, 
		1000 /* Offset */, 1000 /* Row limit */);
FETCH FORWARD 5 IN c4getallatt4theme_3b;
--
-- Release resources. Note order; cursors must be released before temporary tables or you will get:
-- cannot DROP TABLE "c4getallatt4theme_3" because it is being used by active queries in this session
--
\pset title 'Close c4getallatt4theme_3b'
SELECT rif40_xml_pkg.rif40_closeGetMapAreaAttributeCursor('c4getallatt4theme_3b');
\pset title 'Delete c4getallatt4theme_3'
SELECT rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource('c4getallatt4theme_3');

--
-- Extract/map tables tested in alter_2.sql as user rif40 cannot see studies
--


--
-- Demo 5: Check offset works OK
--
\pset title 'rif40_remove_from_debug'
SELECT rif40_log_pkg.rif40_remove_from_debug('rif40_CreateMapAreaAttributeSource');
\pset title 'rif40_add_to_debug: rif40_add_to_debug:DEBUG1'
SELECT rif40_log_pkg.rif40_add_to_debug('rif40_add_to_debug:DEBUG1');
\pset title 'rif40_add_to_debug: rif40_CreateMapAreaAttributeSource:DEBUG2'
SELECT rif40_log_pkg.rif40_add_to_debug('rif40_CreateMapAreaAttributeSource:DEBUG2' /* Enable EXPLAIN PLAN */);
\pset title 'Create c4getallatt4theme_5'
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_5' /* Must be unique with a TX */, 
		'SAHSULAND', 'SAHSU_GRD_LEVEL2', 'geometry', 't_rif40_sahsu_geometry', 
		ARRAY['name', 'area', 'total_males', 'total_females', 'population_year']);

\pset title 'Demo 5: Check offset works OK'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_5' /* Temporary table */, 
		'c4getallatt4theme_5' /* Cursor name - must be unique with a TX */, 
		0 /* Offset */, 10 /* Row limit */);
FETCH FORWARD 15 IN c4getallatt4theme_5 /* Only fetches 10... */;
\pset title 'Repeat for second cursor'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_5' /* Temporary table */, 
		'c4getallatt4theme_5a' /* Cursor name - must be unique with a TX */, 
		0 /* Offset */, 10 /* Row limit */);
FETCH FORWARD 15 IN c4getallatt4theme_5a /* Only fetches 10... */;

\pset title 'Display session cursors'
SELECT * FROM pg_cursors;

--
-- Test for code 50426 - now a debug message - not an error. Tile does not intersect sahsuland
--
SELECT substring(rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSULAND'::VARCHAR 	/* Geography */, 
			'SAHSU_GRD_LEVEL4'::VARCHAR 	/* geolevel view */, 
			52.4827805::REAL /* y_max */, 0::REAL /* x_max */, 50.736454::REAL /* y_min */, -2.8125 /* x_min - Bounding box */,
			6::INTEGER /* Zoom level */,
			FALSE /* Check tile co-ordinates [Default: FALSE] */,			
			FALSE /* Lack of topoJSON is an error [Default: TRUE] */)::Text from 1 for 100) AS json;	
--
-- Test for missing tiles
--
SELECT substring(rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSULAND'::VARCHAR 	/* Geography */, 
			'SAHSU_GRD_LEVEL4'::VARCHAR 	/* geolevel view */, 
			54.52108149544362::REAL /* y_max */, 6.679687499999985::REAL /* x_max */, 54.47003761280576::REAL /* y_min */, -6.767578125000016 /* x_min - Bounding box */,
			10::INTEGER /* Zoom level */,
			FALSE /* Check tile co-ordinates [Default: FALSE] */,			
			FALSE /* Lack of topoJSON is an error [Default: TRUE] */)::Text from 1 for 100) AS json;
SELECT substring(rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSULAND'::VARCHAR 	/* Geography */, 
			'SAHSU_GRD_LEVEL4'::VARCHAR 	/* geolevel view */, 
			54.52108149544362::REAL /* y_max */, 6.679687499999985::REAL /* x_max */, 54.47003761280576::REAL /* y_min */, 6.767578125000016 /* x_min - Bounding box */,
			12::INTEGER /* Zoom level */,
			FALSE /* Check tile co-ordinates [Default: FALSE] */,			
			FALSE /* Lack of topoJSON is an error [Default: TRUE] */)::Text from 1 for 100) AS json;
SELECT substring(rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSULAND'::VARCHAR 	/* Geography */, 
			'SAHSU_GRD_LEVEL4'::VARCHAR 	/* geolevel view */, 
			54.110942942724314::REAL /* y_max */, -6.328124999999994::REAL /* x_max */, 54.05938788662357::REAL /* y_min */, -6.416015624999993 /* x_min - Bounding box */,
			12::INTEGER /* Zoom level */,
			FALSE /* Check tile co-ordinates [Default: FALSE] */,			
			FALSE /* Lack of topoJSON is an error [Default: TRUE] */)::Text from 1 for 100) AS json;
			
SELECT substring(rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSULAND'::VARCHAR 	/* Geography */, 
			'SAHSU_GRD_LEVEL4'::VARCHAR 	/* geolevel view */, 
			11::INTEGER 		/* Zoom level */,
			989::INTEGER 		/* X tile number */,
			660::INTEGER		/* Y tile number */)::Text from 1 for 100) AS json;
			
--
-- Test middleware interface functions
--
\pset title 'rif40_getAllAttributesForGeoLevelAttributeTheme SAHSU level 4 extract'
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSULAND', 'SAHSU_GRD_LEVEL4', 'extract');

\pset title 'rif40_CreateMapAreaAttributeSource: c4getallatt4theme_4'
WITH a AS (
	SELECT MIN(attribute_source) AS min_attribute_source
	  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSULAND', 'SAHSU_GRD_LEVEL4', 'extract')
)
SELECT b.* 
  FROM a, rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_4', 
		'SAHSULAND', 'SAHSU_GRD_LEVEL4', 'extract', a.min_attribute_source) b;
		
\pset title 'rif40_GetMapAreaAttributeValue: c4getallatt4theme_4'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_4' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_4;
/*
                                             rif40_GetMapAreaAttributeValue: c4getallatt4theme_4
     area_id     | gid |     gid_rowindex      | year | study_or_comparison | study_id | band_id | sex | age_group | ses | inv_1 | t
otal_pop
-----------------+-----+-----------------------+------+---------------------+----------+---------+-----+-----------+-----+-------+--
---------
 01.001.000100.1 |   1 | 0000000001_0000000001 | 1989 | S                   |        9 |       1 |   1 |         0 | 4   |     0 |
      34
 01.001.000100.1 |   1 | 0000000001_0000000002 | 1989 | S                   |        9 |       1 |   1 |         1 | 4   |     0 |
      34
 01.001.000100.1 |   1 | 0000000001_0000000003 | 1989 | S                   |        9 |       1 |   1 |         2 | 4   |     0 |
      34
 01.001.000100.1 |   1 | 0000000001_0000000004 | 1989 | S                   |        9 |       1 |   1 |         3 | 4   |     0 |
      34
 01.001.000100.1 |   1 | 0000000001_0000000005 | 1989 | S                   |        9 |       1 |   1 |         4 | 4   |     0 |
      34
(5 rows)
 */

\pset title 'rif40_getAllAttributesForGeoLevelAttributeTheme SAHSU level 4 results'
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSULAND', 'SAHSU_GRD_LEVEL4', 'results');

\pset title 'rif40_CreateMapAreaAttributeSource SAHSU level 4 results'
WITH a AS (
	SELECT MIN(attribute_source) AS min_attribute_source
	  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSULAND', 'SAHSU_GRD_LEVEL4', 'results')
)
SELECT b.* 
  FROM a, rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_5', 
		'SAHSULAND', 'SAHSU_GRD_LEVEL4', 'results', a.min_attribute_source) b;
		
\pset title 'rif40_GetMapAreaAttributeValue: c4getallatt4theme_5'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_5' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_5;
/*
                                                            rif40_GetMapAreaAttributeValue: c4getallatt4theme_5
     area_id     | gid |     gid_rowindex      | username | study_id | inv_id | band_id | genders | direct_standardisation | adjuste
d | observed | expected | lower95 | upper95 | relative_risk | smoothed_relative_risk | posterior_probability | posterior_probability
_upper95 | posterior_probability_lower95 | residual_relative_risk | residual_rr_lower95 | residual_rr_upper95 | smoothed_smr | smoot
hed_smr_lower95 | smoothed_smr_upper95
-----------------+-----+-----------------------+----------+----------+--------+---------+---------+------------------------+--------
--+----------+----------+---------+---------+---------------+------------------------+-----------------------+----------------------
---------+-------------------------------+------------------------+---------------------+---------------------+--------------+------
----------------+----------------------
 01.001.000100.1 |   1 | 0000000001_0000000001 | pch      |        9 |      9 |       1 |       1 |                      0 |
0 |       38 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
 01.001.000100.1 |   1 | 0000000001_0000000002 | pch      |        9 |      9 |       1 |       2 |                      0 |
0 |       36 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
 01.001.000100.1 |   1 | 0000000001_0000000003 | pch      |        9 |      9 |       1 |       3 |                      0 |
0 |       74 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
 01.001.000100.2 |   2 | 0000000002_0000000001 | pch      |        9 |      9 |       2 |       1 |                      0 |
0 |       40 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
 01.001.000100.2 |   2 | 0000000002_0000000002 | pch      |        9 |      9 |       2 |       2 |                      0 |
0 |       18 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
(5 rows)
 */
--
-- Check TopoJSON really has been converted
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT COUNT(CASE WHEN optimised_topojson::Text = '"X"'::Text THEN tile_id ELSE NULL END) AS unconverted_tiles, 
		       COUNT(tile_id) AS total_tiles
		  FROM t_rif40_sahsu_maptiles;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total_tiles = 0 THEN
		RAISE WARNING 'C20999: No tiles found';	
	ELSIF c1_rec.unconverted_tiles > 0 THEN
		RAISE WARNING 'C20999: %/% Unconverted optimised_topojson files found', c1_rec.unconverted_tiles, c1_rec.total_tiles;
	ELSE
		RAISE INFO 'C20999: All % optimised_topojson files converted', c1_rec.total_tiles;
	END IF;
END;
$$; 

SELECT SUBSTRING(rif40_xml_pkg.rif40_GetMapAreas(
			'SAHSULAND' 	/* Geography */, 
			'SAHSU_GRD_LEVEL3' 	/* geolevel view */, 
			55.5268097::REAL /* y_max */, -4.88653803 /* x_max */, 52.6875343 /* y_min */, -7.58829451 /* x_min - Bounding box - from cte */)::Text
				FROM 1 FOR 160) AS json 
LIMIT 4;

--
-- End single transaction
--
END;

\echo Test 7: Middleware tests 2 for alter 2 OK    

--
-- Eof
