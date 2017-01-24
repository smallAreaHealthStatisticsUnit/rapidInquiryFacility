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
-- Rapid Enquiry Facility (RIF) - Test 6: Middleware tests 1 for alter 1
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

\echo Test 6: Middleware tests 1 for alter 1...

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
	rif40_xml_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl', 'rif40_CreateMapAreaAttributeSource', 'rif40_GetMapAreaAttributeValue'
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
		RAISE EXCEPTION 'test_6_middleware_1.sql: T6--01: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'test_6_middleware_1.sql: T6--02: debug level parameter="%"', debug_level::Text;
	END IF;
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_6_middleware_1.sql: T6--03: Invslid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_xml_pkg_functions LOOP
			RAISE INFO 'test_6_middleware_1.sql: T6--06: Enable debug for function: %', l_function;
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
-- Check gid, gid_rowindex present [REMOVED]
--
\pset title 'Check gid, gid_rowindex present'
SElECT areaid
  FROM geometry_sahsuland
 WHERE geolevel_id = 2 AND zoomlevel = 11
 ORDER BY areaid;
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
SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelFullExtent('SAHSULAND' /* Geography */, 'SAHSU_GRD_LEVEL2' /* Geolevel view */);
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
	'SAHSULAND' /* Geography */, 'SAHSU_GRD_LEVEL4' /* Geolevel view */, a.min_study_id /* Study ID */) b;
/*
  y_max  |  x_max   |  y_min  |  x_min
---------+----------+---------+----------
 55.5268 | -4.88654 | 52.6875 | -7.58829
(1 row)
*/

\pset title 'rif40_getGeoLevelBoundsForArea'
SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSULAND' /* Geography */, 'SAHSU_GRD_LEVEL2' /* Geolevel view */, '01.004' /* Map area ID */);
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
\copy (SELECT * FROM rif40_xml_pkg.rif40_get_geojson_as_js('SAHSULAND' /* Geography */, 'SAHSU_GRD_LEVEL4' /* geolevel view */, 'SAHSU_GRD_LEVEL2' /* geolevel area */, '01.004' /* geolevel area id */, FALSE /* return_one_row flag: output multiple rows so it is readable! */)) to ../psql_scripts/test_scripts/data/test_6_geojson_test_01.js 
\copy (SELECT * FROM rif40_xml_pkg.rif40_get_geojson_as_js('SAHSULAND' /* Geography */, 'SAHSU_GRD_LEVEL4' /* geolevel view */, 'SAHSU_GRD_LEVEL2' /* geolevel area */, '01.004' /* geolevel area id */, FALSE /* return_one_row flag: output multiple rows so it is readable! */, TRUE /* Produce JSON not JS */)) to ../psql_scripts/test_scripts/data/test_6_geojson_test_01.json 
\copy (SELECT optimised_topojson FROM t_tiles_sahsuland WHERE tile_id = 'SAHSU_4_LEVEL4_0_0_0') to ../psql_scripts/test_scripts/data/test_6_sahsu_4_level4_0_0_0.json
 
--
-- GetGeoJsonTiles interface
--
\pset title 'GetGeoJsonTiles interface'
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSULAND', 'SAHSU_GRD_LEVEL2', '01.004')
), b AS (
	SELECT ST_Centroid(ST_MakeEnvelope(a.x_min, a.y_min, a.x_max, a.y_max)) AS centroid
	  FROM a
), c AS (
	SELECT ST_X(b.centroid) AS X_centroid, ST_Y(b.centroid) AS Y_centroid, 11 AS zoom_level	  
	  FROM b
), d AS (
	SELECT zoom_level, X_centroid, Y_centroid, 
		   rif40_geo_pkg.latitude2tile(Y_centroid, zoom_level) AS Y_tile,
		   rif40_geo_pkg.longitude2tile(X_centroid, zoom_level) AS X_tile
	  FROM c
), e AS (
	SELECT zoom_level, X_centroid, Y_centroid,
		   rif40_geo_pkg.tile2latitude(Y_tile, zoom_level) AS Y_min,
		   rif40_geo_pkg.tile2longitude(X_tile, zoom_level) AS X_min,	
		   rif40_geo_pkg.tile2latitude(Y_tile+1, zoom_level) AS Y_max,
		   rif40_geo_pkg.tile2longitude(X_tile+1, zoom_level) AS X_max,
		   X_tile, Y_tile
	  FROM d
) 
SELECT SUBSTRING(
		rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSULAND'::VARCHAR 	/* Geography */, 
			'SAHSU_GRD_LEVEL4'::VARCHAR 	/* geolevel view */, 
			e.y_max::REAL, e.x_max::REAL, e.y_min::REAL, e.x_min::REAL, /* Bounding box - from cte */
			e.zoom_level::INTEGER /* Zoom level */)::Text 
			FROM 1 FOR 160 /* Truncate to 160 chars */) AS json 
  FROM e LIMIT 4;	
/*
                                                                               json
-----------------------------------------------------------------------------------------------------------------------------------------------------------------
 { "type": "FeatureCollection","features": [
 {"type": "Feature","properties":{"area_id":"01.001.000100.1","name":"Abellan LEVEL4(01.001.000100.1)","area":"238.40","total_males":"4924.00","total_females":"5
 ,{"type": "Feature","properties":{"area_id":"01.002.001100.1","name":"Cobley LEVEL4(01.002.001100.1)","area":"10.00","total_males":"5246.00","total_females":"54
 ,{"type": "Feature","properties":{"area_id":"01.002.001300.5","name":"Cobley LEVEL4(01.002.001300.5)","area":"2.80","total_males":"2344.00","total_females":"218
(4 rows)
 */
--
-- Use copy to create a Javascript file that can be tested
--
--\copy (WITH a AS (SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')) SELECT rif40_xml_pkg.rif40_get_geojson_tiles('SAHSU' /* Geography */, 'LEVEL4' /* geolevel view */, a.y_max, a.x_max, a.y_min, a.x_min, 9 /* Zoom level */) FROM a) to ../psql_scripts/test_scripts/data/test_6_geojson_test_02.json

--
-- rif40_GetMapAreas interface
--
\pset title 'rif40_GetMapAreas interface'
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSULAND', 'SAHSU_GRD_LEVEL2', '01.004')
) 
SELECT rif40_xml_pkg.rif40_GetMapAreas(
			'SAHSULAND' 	/* Geography */, 
			'SAHSU_GRD_LEVEL4' 	/* geolevel view */, 
			a.y_max, a.x_max, a.y_min, a.x_min /* Bounding box - from cte */) AS json 
  FROM a LIMIT 4;
--
-- Attribute fetch functions
--
\pset title 'Get SAHSULAND LEVEL4 covariates'
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSULAND', 'SAHSU_GRD_LEVEL4', 'covariate');
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
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSULAND', 'SAHSU_GRD_LEVEL4', 'population')
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
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSULAND', 'SAHSU_GRD_LEVEL4', 'covariate', 
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
\pset title 'rif40_GetGeometryColumnNames(SAHSULAND)'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetGeometryColumnNames('SAHSULAND');
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
-- End single transaction
--
END;

\echo Test 6: Middleware tests 1 for alter 1 OK    

--
-- Eof
