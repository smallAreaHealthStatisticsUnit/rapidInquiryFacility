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
-- Rapid Enquiry Facility (RIF) - Test 4: Study ID 1
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
-- the Free Software Foundation, either verquote_ident(l_schema)||'.'||quote_ident(l_tablesion 3 of the License, or
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

\echo Test 6: Middleware tests 1...

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
	rif40_sm_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl', 'rif40_CreateMapAreaAttributeSource', 'rif40_GetMapAreaAttributeValue'
								'rif40_DeleteMapAreaAttributeSourc', 'rif40_closeGetMapAreaAttributeCursor'];
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
		RAISE EXCEPTION 'test_4_study_id_1.sql: T4--01: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'test_4_study_id_1.sql: T4--02: debug level parameter="%"', debug_level::Text;
	END IF;
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_4_study_id_1.sql: T4--03: Invslid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_sm_pkg_functions LOOP
			RAISE INFO 'test_4_study_id_1.sql: T4--06: Enable debug for function: %', l_function;
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
-- Tests
--
-- Check gid, gid_rowindex present
--
\pset title 'Check gid, gid_rowindex present'
SElECT area_id, name, gid, gid_rowindex
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL2'
 ORDER BY gid;
/*
 area_id |    name    | gid |     gid_rowindex
---------+------------+-----+-----------------------
 01.001  | Abellan    |   1 | 0000000001_0000000001
 01.002  | Cobley     |   2 | 0000000002_0000000001
 01.003  | Beale      |   3 | 0000000003_0000000001
 01.004  | Hambly     |   4 | 0000000004_0000000001
 01.005  | Briggs     |   5 | 0000000005_0000000001
 01.006  | Andersson  |   6 | 0000000006_0000000001
 01.007  | Hodgson    |   7 | 0000000007_0000000001
 01.008  | Jarup      |   8 | 0000000008_0000000001
 01.009  | Elliot     |   9 | 0000000009_0000000001
 01.011  | Clarke     |  10 | 0000000010_0000000001
 01.012  | Tirado     |  11 | 0000000011_0000000001
 01.013  | Kozniewska |  12 | 0000000012_0000000001
 01.014  | Stordy     |  13 | 0000000013_0000000001
 01.015  | Maitland   |  14 | 0000000014_0000000001
 01.016  | De Hoogh   |  15 | 0000000015_0000000001
 01.017  | Savigny    |  16 | 0000000016_0000000001
 01.018  | Cockings   |  17 | 0000000017_0000000001
(17 rows)
 */
 
--
-- Bounding box functions
--
\pset title 'rif40_getGeoLevelFullExtentForStudy: LEVEL2'
SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelFullExtent('SAHSU' /* Geography */, 'LEVEL2' /* Geolevel view */);
/*
  y_max  |  x_max   |  y_min  |  x_min
---------+----------+---------+----------
 55.5268 | -4.88654 | 52.6875 | -7.58829
(1 row)
 */

\pset title 'rif40_getGeoLevelFullExtentForStudy: LEVEL4'
WITH a AS (
	SELECT MIN(study_id) AS min_study_id
	  FROM t_rif40_studies
)
SELECT b.* 
  FROM a, rif40_xml_pkg.rif40_getGeoLevelFullExtentForStudy(
	'SAHSU' /* Geography */, 'LEVEL4' /* Geolevel view */, a.min_study_id /* Study ID */) b;
/*
  y_max  |  x_max   |  y_min  |  x_min
---------+----------+---------+----------
 55.5268 | -4.88654 | 52.6875 | -7.58829
(1 row)
*/

\pset title 'rif40_getGeoLevelBoundsForArea'
SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU' /* Geography */, 'LEVEL2' /* Geolevel view */, '01.004' /* Map area ID */);
/*
  y_max  |  x_max   |  y_min  |  x_min
---------+----------+---------+----------
 55.0122 | -6.32507 | 54.6456 | -6.68853
(1 row)
 */

--
-- Old rif40_get_geojson_as_js() - technically not part of the new web services interface
--
-- View the <geolevel view> (e.g. GOR2001) of <geolevel area> (e.g. London) and select at <geolevel select> (e.g. LADUA2001) level.
-- or in this case: 
-- "View the <geolevel view> (e.g. LEVEL4) of <geolevel area> (e.g. 01.004) and select at <geolevel select> (e.g. LEVEL2) level".
--
\copy (SELECT * FROM rif40_xml_pkg.rif40_get_geojson_as_js('SAHSU' /* Geography */, 'LEVEL4' /* geolevel view */, 'LEVEL2' /* geolevel area */, '01.004' /* geolevel area id */, FALSE /* return_one_row flag: output multiple rows so it is readable! */)) to ../psql_scripts/test_scripts/data/test_6_geojson_test_01.js 

--
-- GetGeoJsonTiles interface
--
\pset title 'GetGeoJsonTiles interface'
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')
) 
SELECT SUBSTRING(
		rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSU' 	/* Geography */, 
			'LEVEL4' 	/* geolevel view */, 
			a.y_max, a.x_max, a.y_min, a.x_min, /* Bounding box - from cte */
			FALSE /* return_one_row flag: output multiple rows so it is readable! */) FROM 1 FOR 160) AS js 
  FROM a LIMIT 4;
/*
                                                                                js
-----------------------------------------------------------------------------------------------------------------------------------------------------------------
 var spatialData={ "type": "FeatureCollection","features": [ /- Start -/
 {"type": "Feature","properties":{"area_id":"01.001.000100.1","name":"Abellan LEVEL4(01.001.000100.1)","area":"238.40","total_males":"4924.00","total_females":"5
 ,{"type": "Feature","properties":{"area_id":"01.002.001100.1","name":"Cobley LEVEL4(01.002.001100.1)","area":"10.00","total_males":"5246.00","total_females":"54
 ,{"type": "Feature","properties":{"area_id":"01.002.001300.5","name":"Cobley LEVEL4(01.002.001300.5)","area":"2.80","total_males":"2344.00","total_females":"218
(4 rows)
 */
--
-- Use copy to create a Javascript file that can be tested
--
\copy (WITH a AS (SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')) SELECT rif40_xml_pkg.rif40_get_geojson_tiles('SAHSU' /* Geography */, 'LEVEL4' /* geolevel view */, a.y_max, a.x_max, a.y_min, a.x_min, FALSE /* return_one_row flag: output multiple rows so it is readable! */) FROM a) to ../psql_scripts/test_scripts/data/test_6_geojson_test_02.js

--
-- Attribute fetch functions
--
\pset title 'Get SAHSULAND LEVEL4 covariates'
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'covariate');
/*
      attribute_source       | attribute_name |   theme   |       source_description       |                         name_descriptio
n                          | ordinal_position | is_numeric
-----------------------------+----------------+-----------+--------------------------------+----------------------------------------
---------------------------+------------------+------------
 sahsuland_covariates_level4 | year           | covariate | SAHSU land covariates - level4 | Year
                           |                1 | t
 sahsuland_covariates_level4 | ses            | covariate | SAHSU land covariates - level4 | Social Economic Status (quintiles)
                           |                3 | t
 sahsuland_covariates_level4 | areatri1km     | covariate | SAHSU land covariates - level4 | Toxic Release Inventory within 1km of a
rea (0=no/1=yes)           |                4 | t
 sahsuland_covariates_level4 | near_dist      | covariate | SAHSU land covariates - level4 | Distance (m) from area centroid to near
est TRI site               |                5 | t
 sahsuland_covariates_level4 | tri_1km        | covariate | SAHSU land covariates - level4 | Toxic Release Inventory within 1km of a
reai centroid (0=no/1=yes) |                6 | t
(5 rows)
 */
 
/*
rif40_getNumericAttributesForGeoLevelAttributeTheme() not implemented, use the is_numeric BOOLEAN flag in above
rif40_AttributeExistsForGeoLevelAttributeTheme() not implemented, use the attribute name filter to select by named attribute

Note there is currently no support for health themes.
 */
 
\pset title 'rif40_AttributeExistsForGeoLevelAttributeTheme() example'
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'population')
 WHERE is_numeric /* rif40_getNumericAttributesForGeoLevelAttributeTheme() example */;
/*
 attribute_source | attribute_name |   theme    | source_description | name_description | ordinal_position | is_numeric
------------------+----------------+------------+--------------------+------------------+------------------+------------
 sahsuland_pop    | year           | population | SAHSULAND_POP      | Year             |                1 | t
 sahsuland_pop    | age_sex_group  | population | SAHSULAND_POP      | Age sex group    |                2 | t
 sahsuland_pop    | total          | population | SAHSULAND_POP      | Total            |                7 | t
(3 rows)
 */
 
\pset title 'rif40_AttributeExistsForGeoLevelAttributeTheme() example 2'
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'covariate', 
		ARRAY['SES'] /* rif40_AttributeExistsForGeoLevelAttributeTheme() example */);
/*
      attribute_source       | attribute_name |   theme   |       source_description       |          name_description          | or
dinal_position | is_numeric
-----------------------------+----------------+-----------+--------------------------------+------------------------------------+---
---------------+------------
 sahsuland_covariates_level4 | ses            | covariate | SAHSU land covariates - level4 | Social Economic Status (quintiles) |
             3 | t
(1 row)
 */
 
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
	'SAHSU'::VARCHAR, 'LEVEL4'::VARCHAR, a.enumlabel::VARCHAR) b;
/*
   theme    |      attribute_source       |        attribute_name         |
                         name_description                                                                                  | ordinal
_position | is_numeric
------------+-----------------------------+-------------------------------+---------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------+--------
----------+------------
 covariate  | sahsuland_covariates_level4 | year                          | Year
                                                                                                                           |
        1 | t
 covariate  | sahsuland_covariates_level4 | ses                           | Social Economic Status (quintiles)
                                                                                                                           |
        3 | t
 covariate  | sahsuland_covariates_level4 | areatri1km                    | Toxic Release Inventory within 1km of area (0=no/1=yes)
                                                                                                                           |
        4 | t
 covariate  | sahsuland_covariates_level4 | near_dist                     | Distance (m) from area centroid to nearest TRI site
                                                                                                                           |
																														   
...

*/
--
-- rif40_GetGeoLevelAttributeTheme() not implemented, purpose unclear
-- rif40_GetGeometryColumnNames() 
--
\pset title 'rif40_GetGeometryColumnNames(SAHSU)'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetGeometryColumnNames('SAHSU');
/*
    column_name     |

                                                                             column_description



--------------------+---------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------
-------------------
 optimised_geometry | Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only). Can also use SHAPEFILE inst
ead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLE
RANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small slivers and ove
rlaps due to limitation in the Douglas-Peucker algorithm (it works onj an object by object basis; the edge between two areas will th
erefore be processed independently and not necessarily in the same manner. This is fixed using the PostGIS Topology extension and pr
ocessing as edges.
(1 row)
*/

--
-- rif40_GetMapAreaAttributeValue();
--

--
-- Demo 1. Sahsuland cancer. All defaults (i.e. all columns, fetch 1000 rows at offset 0)
--
\pset title 'Create temporary table'
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_2' /* Temporary table */, 
		'SAHSU', 'LEVEL2', 'health', 'sahsuland_cancer');
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
		'SAHSU', 'LEVEL4', 'covariate', 'sahsuland_covariates_level4', ARRAY['SES', 'year']);
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
-- REF_CURSOR takes 12 secnds to parse and execute (caused by the rowindex sort) with explain plan
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
		'SAHSU', 'LEVEL2', 'population', 'sahsuland_pop');
--
-- Create REFCURSOR
--
\pset title 'Create REFCURSOR'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_3' /* Temporary table */,
		NULL /* Cursor name - NULL = same as temporarty table - must be unique with a TX */, 
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
		NULL /* Cursor name - NULL = same as temporarty table - must be unique with a TX */, 
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
		'SAHSU', 'LEVEL2', 'population', 'sahsuland_pop');
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
		'SAHSU', 'LEVEL2', 'geometry', 't_rif40_sahsu_geometry');
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
-- End single transaction
--
END;

\echo Test 6: Middleware tests 1 OK    

--
-- Eof
