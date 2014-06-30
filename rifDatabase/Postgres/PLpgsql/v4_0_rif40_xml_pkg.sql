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
-- rif40_xml_pkg:
--
-- _rif40_getGeoLevelExtentCommon: 			50000 to 50099
-- rif40_get_geojson_as_js: 				50200 to 50399
-- rif40_get_geojson_tiles: 				50400 to 50599
-- _rif40_get_geojson_as_js: 				50600 to 50799
-- getAllAttributesForGeoLevelAttributeTheme: 		50800 to 50999
-- rif40_GetGeometryColumnNames: 			51000 to 51199
-- rif40_GetMapAreaAttributeValue: 			51200 to 51399
--

--
-- Add enumerated type for geolevelAttributeTheme
--
DROP TYPE IF EXISTS rif40_xml_pkg.rif40_geolevelAttributeTheme;
CREATE TYPE rif40_xml_pkg.rif40_geolevelAttributeTheme AS ENUM (
	'covariate', 'health', 'extract', 'results', 'population', 'geometry');

--
-- Include common code
--
\i ../PLpgsql/rif40_xml_pkg/_rif40_getGeoLevelExtentCommon.sql
\i ../PLpgsql/rif40_xml_pkg/_rif40_get_geojson_as_js.sql

--
-- Include functions
--
\i ../PLpgsql/rif40_xml_pkg/rif40_get_geojson_as_js.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_get_geojson_tiles.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_getGeoLevelFullExtent.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_getGeoLevelFullExtentForStudy.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_getGeoLevelBoundsForArea.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_getAllAttributesForGeoLevelAttributeTheme.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_GetGeometryColumnNames.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_GetMapAreaAttributeValue.sql

COMMENT ON SCHEMA rif40_xml_pkg
  IS 'RIF XML support.

Functions:

Function: 	rif40_get_geojson_tiles()
Parameters:	Geography, geolevel_view, Y max, X max, Y min, X min as a record, return one row (TRUE/FALSE)
Returns:	Text table [1 or more rows dependent on return_one_row]
Description:	Get GeoJSON data as a Javascript variable. 
		Fetch tiles bounding box Y max, X max, Y min, X min for <geography> <geolevel view>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

		1 row means 1 row, no CRLFs etc!
		If return_one_row is false then a header, 1 rows/area_id and a footer is returned so the Javascript is easier to read

Function: 	rif40_get_geojson_as_js()
Parameters:	Geography, geolevel_view, geolevel_area, geolevel_area_id, return one row (TRUE/FALSE)
Returns:	Text table [1 or more rows dependent on return_one_row]
Description:	Get GeoJSON data as a Javascript variable. 
		For the disease mapping selection dialog phrase:
		"View the <geolevel view> (e.g. GOR2001) of <geolevel area> (e.g. London) and select at <geolevel select> (e.g. LADUA2001) level."
		1 row means 1 row, no CRLFs etc!
		If return_one_row is false then a header, 1 rows/area_id and a footer is returned so the Javascript is easier to read

Function: 	rif40_getGeoLevelBoundsForArea()
Parameters:	Geography, geolevel_view, map area id
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view> <map area ID>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

Function: 	rif40_getGeoLevelFullExtent()
Parameters:	Geography, geolevel_view
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

Function: 	rif40_getGeoLevelFullExtentForStudy()
Parameters:	Geography, geolevel_view
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view> <study>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

Function: 	rif40_getAllAttributesForGeoLevelAttributeTheme()
Parameters:	Geography, <geolevel select>, theme (enum: rif40_xml_pkg.rif40_geolevelAttributeTheme), attribute name array [Default: NULL - do not filter, return all attributes]
Returns:	Table: attribute_source, attribute_name, theme, source_description, name_description, is_numeric
Description:	Get all atrributes for geography geolevel theme

E.g.

      attribute_source       | attribute_name |   theme   |       source_description       |                         name_description                          | is_numeric
-----------------------------+----------------+-----------+--------------------------------+-------------------------------------------------------------------+------------
 sahsuland_covariates_level4 | areatri1km     | covariate | SAHSU land covariates - level4 | Toxic Release Inventory within 1km of area (0=no/1=yes)           | t
 sahsuland_covariates_level4 | near_dist      | covariate | SAHSU land covariates - level4 | Distance (m) from area centroid to nearest TRI site               | t
 sahsuland_covariates_level4 | ses            | covariate | SAHSU land covariates - level4 | Social Economic Status (quintiles)                                | t
 sahsuland_covariates_level4 | tri_1km        | covariate | SAHSU land covariates - level4 | Toxic Release Inventory within 1km of areai centroid (0=no/1=yes) | t
(4 rows)

Warning: this is slow as it uses rif40_num_denom, takes 408mS on my laptop to fetch all attributes for all themes

- User must be rif40 or have rif_user or rif_manager role
- Test geography
- Process themes
- If attribute name array is used, then all must be found

Uses column comment where present to provide descriptions

rif40_getNumericAttributesForGeoLevelAttributeTheme() not implemented, use the is_numeric BOOLEAN flag, e.g.

SELECT *
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(''SAHSU'', ''LEVEL4'', ''population'')
 WHERE is_numeric /* rif40_getNumericAttributesForGeoLevelAttributeTheme() example */;
psql:alter_scripts/v4_0_alter_2.sql:455: INFO:  [DEBUG1] rif40_getAllAttributesForGeoLevelAttributeTheme(): [50806] Geography: SAHSU, geolevel select: LEVEL4, theme: population; SQ
L fetch returned 6 rows, took: 00:00:00.235.
 attribute_source | attribute_name |   theme    | source_description | name_description | is_numeric
------------------+----------------+------------+--------------------+------------------+------------
 sahsuland_pop    | age_sex_group  | population | SAHSULAND_POP      | Age sex group    | t
 sahsuland_pop    | total          | population | SAHSULAND_POP      | Total            | t
 sahsuland_pop    | year           | population | SAHSULAND_POP      | Year             | t
(3 rows)

rif40_AttributeExistsForGeoLevelAttributeTheme() not implemented, use the attribute name filter to select by named attribute

SELECT *
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(''SAHSU'', ''LEVEL4'', ''covariate'',
                ARRAY[''SES''] /* rif40_AttributeExistsForGeoLevelAttributeTheme() example */);
psql:alter_scripts/v4_0_alter_2.sql:458: INFO:  [DEBUG1] rif40_getAllAttributesForGeoLevelAttributeTheme(): [50806] Geography: SAHSU, geolevel select: LEVEL4, theme: covariate; SQL
 fetch returned 1 rows, took: 00:00:00.016.
      attribute_source       | attribute_name |   theme   |       source_description       |          name_description          | is_numeric
-----------------------------+----------------+-----------+--------------------------------+------------------------------------+------------
 sahsuland_covariates_level4 | ses            | covariate | SAHSU land covariates - level4 | Social Economic Status (quintiles) | t
(1 row)

Note there is currently no support for health themes.

Function: 	rif40_GetGeometryColumnNames()
Parameters:	Geography
Returns:	Table: column_name, column_description
Description:	Get all the SRID 4326 (WGS84) geometry column names for geography

SELECT *
  FROM rif40_xml_pkg.rif40_GetGeometryColumnNames(''SAHSU'');
psql:alter_scripts/v4_0_alter_2.sql:480: INFO:  [DEBUG1] rif40_GetGeometryColumnNames(): [51004] Geography: SAHSU; SQL fetch returned 1 rows, took: 00:00:00.016.
    column_name     |
                                                                                                                                                                 column_description


--------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------------------
 optimised_geometry | Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only). Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determ
ines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points
. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works onj an object by object basis; the edge between two areas will therefore be p
rocessed independently and not necessarily in the same manner. This is fixed using the PostGIS Topology extension and processing as edges.
(1 row)

Function: 	rif40_GetMapAreaAttributeValue()
Parameters:	REFCURSOR, Geography, <geolevel select>, theme (enum: rif40_xml_pkg.rif40_geolevelAttributeTheme), attribute source, attribute name array
Returns:	Table: area_id, attribute_value, is_numeric
Description:	Get all the SRID 4326 (WGS84) geometry column names for geography
		This function returns a REFCURSOR, so only parses the SQL and does not execute it.

Beware: the cursor name in the FETCH statement (c4getallatt4theme_5) must be unqiue for each open (the SELECT just before)

Warning: this can be slow as it uses rif40_num_denom, takes 398mS on my laptop to fetch rows to RI40_NUM_DEMON based (health and population) themes

- User must be rif40 or have rif_user or rif_manager role
- Test geography
- Test <geolevel select> exists
- Check attribute exists, build SQL injection proof select list
- Process themes
- If attribute name array is used, then all must be found

E.g.

SELECT *
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(
                ''c4getallatt4theme_5'' /* Must be unique with a TX */,
                ''SAHSU'', ''LEVEL2'', ''geometry'', ''t_rif40_sahsu_geometry'');
psql:alter_scripts/v4_0_alter_2.sql:526: INFO:  [DEBUG1] rif40_getAllAttributesForGeoLevelAttributeTheme(): [50807] Geography: SAHSU, geolevel select: LEVEL2, theme: geometry; SQL
fetch returned 5 attribute names, took: 00:00:00.016.
psql:alter_scripts/v4_0_alter_2.sql:526: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51206] c4getallatt4theme SQL>
SELECT area, name, population_year, total_females, total_males, area_id
  FROM t_rif40_sahsu_geometry
 ORDER BY 1, 2;
psql:alter_scripts/v4_0_alter_2.sql:526: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51208] Geography: SAHSU, geolevel select: LEVEL2, theme: geometry, attribute names: [],
source: t_rif40_sahsu_geometry; SQL parse took: 00:00:00.047.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_5
(1 row)


Time: 70.550 ms
FETCH FORWARD 5 IN c4getallatt4theme_5;
 area |              name              | population_year | total_females | total_males |     area_id
------+--------------------------------+-----------------+---------------+-------------+-----------------
 0.10 | Clarke LEVEL4(01.011.014600.6) |         1996.00 |       5716.00 |     5500.00 | 01.011.014600.6
 0.10 | Clarke LEVEL4(01.011.014700.4) |         1996.00 |       6612.00 |     6334.00 | 01.011.014700.4
 0.10 | Clarke LEVEL4(01.011.014800.1) |         1996.00 |       4816.00 |     4542.00 | 01.011.014800.1
 0.10 | Clarke LEVEL4(01.011.014800.2) |         1996.00 |       1812.00 |     1692.00 | 01.011.014800.2
 0.10 | Clarke LEVEL4(01.011.014800.3) |         1996.00 |       5772.00 |     5328.00 | 01.011.014800.3
(5 rows)

';

--
-- Eof
