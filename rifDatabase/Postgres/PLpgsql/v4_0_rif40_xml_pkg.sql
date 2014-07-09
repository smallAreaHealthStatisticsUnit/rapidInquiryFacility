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
-- rif40_closeGetMapAreaAttributeCursor: 		51400 to 51599
-- rif40_CreateMapAreaAttributeSource: 			51600 to 51799
-- rif40_DeleteMapAreaAttributeSource: 			51800 to 51999
--

--
-- Add enumerated type for geolevelAttributeTheme
--
DROP TYPE IF EXISTS rif40_xml_pkg.rif40_geolevelAttributeTheme CASCADE;
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
\i ../PLpgsql/rif40_xml_pkg/rif40_closeGetMapAreaAttributeCursor.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_CreateMapAreaAttributeSource.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_DeleteMapAreaAttributeSource.sql

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

Function: 	rif40_CreateMapAreaAttributeSource()
Parameters:	Temporary table name (same as REFCURSOR), Geography, <geolevel select>, 
		theme (enum: rif40_xml_pkg.rif40_geolevelAttributeTheme), attribute source, 
 		attribute name array [Default NULL - All attributes]
Returns:	Temporary table name
Description:	Create temporary table with all values for attributes source, attribute names, geography and geolevel select

Warnings: 

a) This function can be slow as if uses rif40_num_denom, takes 398mS on my laptop to fetch rows to RI40_NUM_DEMON based
   (health and population) themes
b) This function is VOLATILE. Calling it twice with the same parameters will not necessarily create a temporary table 
   with the same set of rows in the same order.
   This is because databases to not guarantee data block access order, even without parallelisation.
   To minimise the volatility:
	i) Define a sort order. This has a severe performance cost for large tables.

Checks:

- User must be rif40 or have rif_user or rif_manager role
- Test geography
- Test <geolevel select> exists
- Check attribute exists, build SQL injection proof select list
- Process themes
- If attribute name array is used, then all must be found
- Check if GID, gid_rowindex exist [they do not have to, but the query runs quicker if they do]
- Geometry tables must have gid and gid_rowindex
- Check temporary table does NOT exist

Examples:

Example 1) Full SELECT of sahsuland_pop
	   This performs well on a server, takes about 3 seconds on a laptop

--
-- Create temporary table
--
SELECT *
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
                ''c4getallatt4theme_3'' /* Temporary table */,
                ''SAHSU'', ''LEVEL2'', ''population'', ''sahsuland_pop'');
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_getAllAttributesForGeoLevelAttributeTheme(): [50807] Geography: SAHSU, geolevel select: LEVEL2, theme: population; SQL fetch returned 6 attribute names, took: 00:00:00.213.
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51612] Neither gid or gid_rowindex columns are present in attrribute source table: sahsuland_pop
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_DeleteMapAreaAttributeSource(): [51616] Temporary table: c4getallatt4theme_3 does not exist.
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51620] c4getallatt4theme SQL>
CREATE TEMPORARY TABLE c4getallatt4theme_3
AS
WITH a AS (
        SELECT a.level2 /* Map <geolevel select> to area_id */ AS area_id,
       g.gid,
       LPAD(g.gid::Text, 10, ''0''::Text)||''_''||LPAD(ROW_NUMBER() OVER(PARTITION BY a.level2 /* Use default table order */)::Text, 10,
 ''0''::Text) AS gid_rowindex,
       a.year /* ordinal_position: 1 */,
       a.age_sex_group /* ordinal_position: 2 */,
       a.level1 /* ordinal_position: 3 */,
       a.level3 /* ordinal_position: 5 */,
       a.level4 /* ordinal_position: 6 */,
       a.total /* ordinal_position: 7 */
          FROM sahsuland_pop a,
               t_rif40_sahsu_geometry g
         WHERE g.geography     = $1
           AND g.geolevel_name = $2 /* Partition elimination */
           AND g.area_id       = a.level2 /* Link gid */
) /* Force CTE to be executed entirely */
SELECT *
  FROM a
 ORDER BY 3 /* gid_rowindex */;
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51622] Cursor: c4getallatt4theme_3, geography: SAHSU, geolevel select: LEVEL2, theme: population, attribute names: [], source: sahsuland_pop, SQL parse took: 00:00:04.7
59.
 rif40_createmapareaattributesource
------------------------------------
 c4getallatt4theme_3
(1 row)

--
-- Create REFCURSOR
--
SELECT *
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(''c4getallatt4theme_3'' /* Temporary table */,
                NULL /* Cursor name - NULL = same as temporarty table - must be unique with a TX */,
                0 /* No offset */, NULL /* No row limit */);
psql:alter_scripts/v4_0_alter_1.sql:645: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51205] c4getallatt4theme SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:645: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51207] Cursor: c4getallatt4theme_3, temporary table: c4getallatt4theme_3; offset: 0, row limit: , SQL parse took: 00:00:00.016.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

--
-- Fetch tests
--
FETCH FORWARD 5 IN c4getallatt4theme_3 /* 1.3 seconds with no row limit, no sort list, no gid/gid_rowindex columns built in */;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000000001 | 1989 |           100 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000002 | 1989 |           101 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000003 | 1989 |           102 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000004 | 1989 |           103 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000005 | 1989 |           104 | 01     | 01.001.000100 | 01.001.000100.1 |    34
(5 rows)

MOVE ABSOLUTE 1000 IN c4getallatt4theme_3 /* move to row 1000 */;
MOVE 1
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000001001 | 1994 |           210 | 01     | 01.001.000200 | 01.001.000200.1 |   390
 01.001  |   1 | 0000000001_0000001002 | 1994 |           211 | 01     | 01.001.000200 | 01.001.000200.1 |   356
 01.001  |   1 | 0000000001_0000001003 | 1994 |           212 | 01     | 01.001.000200 | 01.001.000200.1 |   392
 01.001  |   1 | 0000000001_0000001004 | 1994 |           213 | 01     | 01.001.000200 | 01.001.000200.1 |   388
 01.001  |   1 | 0000000001_0000001005 | 1994 |           214 | 01     | 01.001.000200 | 01.001.000200.1 |   286
(5 rows)

MOVE ABSOLUTE 10000 IN c4getallatt4theme_3 /* move to row 10000 */;
MOVE 1
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.002  |   2 | 0000000002_0000008593 | 1990 |           112 | 01     | 01.002.000700 | 01.002.000700.2 |   728
 01.002  |   2 | 0000000002_0000008594 | 1990 |           113 | 01     | 01.002.000700 | 01.002.000700.2 |   542
 01.002  |   2 | 0000000002_0000008595 | 1990 |           114 | 01     | 01.002.000700 | 01.002.000700.2 |   514
 01.002  |   2 | 0000000002_0000008596 | 1990 |           115 | 01     | 01.002.000700 | 01.002.000700.2 |   494
 01.002  |   2 | 0000000002_0000008597 | 1990 |           116 | 01     | 01.002.000700 | 01.002.000700.2 |   528
(5 rows)

MOVE ABSOLUTE 432958 IN c4getallatt4theme_3 /* move to row 432958 - two from the end */;
MOVE 1
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.018  |  17 | 0000000017_0000005983 | 1996 |           220 | 01     | 01.018.019500 | 01.018.019500.3 |   388
 01.018  |  17 | 0000000017_0000005984 | 1996 |           221 | 01     | 01.018.019500 | 01.018.019500.3 |   402
(2 rows)

--
-- Test cursor close. Does release resources!!!!
--
SELECT rif40_xml_pkg.rif40_closeGetMapAreaAttributeCursor(''c4getallatt4theme_3'');
psql:alter_scripts/v4_0_alter_1.sql:659: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51401] Cursor: c4getallatt4theme_3 in use, created: 2014-07-09 09:21:59.422+01, SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:659: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51404] Cursor: c1closecursor1 closed
 rif40_closegetmapareaattributecursor
--------------------------------------

(1 row)

SELECT *
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(''c4getallatt4theme_3'' /* Temporary table */,
                NULL /* Cursor name - NULL = same as temporarty table - must be unique with a TX */,
                10000 /* Offset */, NULL /* No row limit */);
psql:alter_scripts/v4_0_alter_1.sql:665: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51205] c4getallatt4theme SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:665: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51207] Cursor: c4getallatt4theme_3, temporary table: c4getallatt4theme_3; offset: 10000, row limit: , SQL parse took: 00:00:00.01.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.002  |   2 | 0000000002_0000008593 | 1990 |           112 | 01     | 01.002.000700 | 01.002.000700.2 |   728
 01.002  |   2 | 0000000002_0000008594 | 1990 |           113 | 01     | 01.002.000700 | 01.002.000700.2 |   542
 01.002  |   2 | 0000000002_0000008595 | 1990 |           114 | 01     | 01.002.000700 | 01.002.000700.2 |   514
 01.002  |   2 | 0000000002_0000008596 | 1990 |           115 | 01     | 01.002.000700 | 01.002.000700.2 |   494
 01.002  |   2 | 0000000002_0000008597 | 1990 |           116 | 01     | 01.002.000700 | 01.002.000700.2 |   528
(5 rows)

SELECT rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource(''c4getallatt4theme_3'');
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_DeleteMapAreaAttributeSource(): [51801] Cursor: c4getallatt4theme_3 in use, created: 2014-07-09 09:22:03.179+01, SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51401] Cursor: c4getallatt4theme_3 in use, created: 2014-07-09 09:22:03.179+01, SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51404] Cursor: c1closecursor1 closed
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_DeleteMapAreaAttributeSource(): [51804] Found temporary table: c4getallatt4theme_3.
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_ddl(): SQL> DROP TABLE c4getallatt4theme_3;
 rif40_deletemapareaattributesource
------------------------------------

(1 row)

Debug:

DEBUG1 prints SQL and diagnostics
DEBUG2 does an EXPLAIN PLAN

Function: 	rif40_GetMapAreaAttributeValue()
Parameters:	Temporary table (created by rif40_CreateMapAreaAttributeSource()), 
		REFCURSOR [Default NULL - same as temporary table name,
		offset [Default 0], row limit [Default NULL - All rows]
Returns:	Scrollable REFCURSOR
Description:    Return REFCURSOR as SELECT FROM temporary table
		This function returns a REFCURSOR, so only parses the SQL and does not execute it.
		Offset and row limit are used for cursor control

Function: 	rif40_closeGetMapAreaAttributeCursor()
Parameters:	Cursor name
Returns: 	Nothing
Description:	Close REF_CURSOR (created by rif40_GetMapAreaAttributeValue)

Checks:

- Check if cursor exists

Function: 	rif40_DeleteMapAreaAttributeSource()
Parameters:	Map attribute source (temporary table name)
Returns: 	Nothing
Description:	Drop map attribute source (temporary table)

Checks:

- Check if cursor exists, if yes close REF_CURSOR (created by rif40_GetMapAreaAttributeValue)
- Check temporary table exists
';

--
-- Eof
