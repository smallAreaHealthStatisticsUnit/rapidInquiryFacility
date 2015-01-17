-- ************************************************************************
-- *
-- * THIS SCRIPT MAY BE EDITED - NO NEED TO USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - Create PG psql code (Geographic processing)
--								  populate_hierarchy_table() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.populate_hierarchy_table()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	populate_hierarchy_table()
Parameters:	Nothing
Returns:	Nothing
Description:	Populate hierarchy table

 */
DECLARE
	c2_hier2 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
--
	c2_rec rif40_geographies%ROWTYPE;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'populate_hierarchy_table', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Create lookup and Hierarchy tables
--
	FOR c2_rec IN c2_hier2 LOOP
		PERFORM rif40_geo_pkg.populate_hierarchy_table(c2_rec.geography, c2_rec.hierarchytable);
	END LOOP;
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.populate_hierarchy_table() IS 'Function: 	populate_hierarchy_table()
Parameters:	Nothing
Returns:	Nothing
Description:	Populate hierarchy table';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.populate_hierarchy_table(l_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	populate_hierarchy_table()
Parameters:	geography
Returns:	Nothing
Description:	Populate hierarchy table
		Check rows were inserted
		Re-index
		Analyze
		Check intersections

This algorithm implments the prototype in: ../postgres/gis_intersection_prototype.sql. The SAHSU alogoritm differs in that
Where intersections overlap (e.g. level4 appears in more than 1 level3) the level3 with the greatest intersected area is chosen 
This is differed to ARCGis - see the prototype file for a more detailed investigation

This function must be run after rif40_geo_pkg.populate_rif40_geometry_tables() i.e. area_id, shapefile_geometry and shapefile_geometry must exist

Population SQL used to fail with (fixed with ST_MakeValid()):

ERROR:  Error performing intersection: TopologyException: found non-noded intersection between LINESTRING (-2.99198 53.3669, -2.98262 53.367) and LINESTRING (-2.98556 53.367, -2.98854 53.3655) at -2.9855578257498308 53.366966593247653

To test for invalid polygons:

SELECT area_id, ST_Isvalid(shapefile_geometry) AS v1, ST_Isvalid(shapefile_geometry) AS v2
  FROM p_rif40_geolevels_geometry_ew01_oa2001
 WHERE NOT ST_Isvalid(shapefile_geometry) OR NOT ST_Isvalid(shapefile_geometry);

NOTICE:  Self-intersection at or near point -2.9855576987063932 53.366966657364905
NOTICE:  Self-intersection at or near point -2.9855576987063932 53.366966657364905
NOTICE:  Self-intersection at or near point -4.1539493321927816 51.672864859911542
NOTICE:  Self-intersection at or near point -4.1539493321927816 51.672864859911542
NOTICE:  Self-intersection at or near point 0.27233850962861539 50.88057609104721
NOTICE:  Self-intersection at or near point 0.27233850962861539 50.88057609104721
  area_id   | v1 | v2 
------------+----+----
 00CBFD0032 | t  | f
 00NUQQ0006 | t  | f
 21UHHJ0015 | t  | f
(3 rows)

Example population SQL>

INSERT INTO ew2001_geography (scntry2001, cntry2001, gor2001, ladua2001, ward2001, soa2001, oa2001)
WITH x12 AS ( -* Subqueries x12 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a1.area_id AS scntry2001, a2.area_id AS cntry2001,
       ST_Area(a2.shapefile_geometry) AS a2_area,
       ST_Area(ST_Intersection(a1.shapefile_geometry, a2.shapefile_geometry)) a12_area
  FROM p_rif40_geolevels_geometry_ew01_scntry2001 a1 CROSS JOIN p_rif40_geolevels_geometry_ew01_cntry2001 a2
 WHERE ST_Intersects(a1.shapefile_geometry, a2.shapefile_geometry)
), x23 AS ( -* Subqueries x23 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a2.area_id AS cntry2001, a3.area_id AS gor2001,
       ST_Area(a3.shapefile_geometry) AS a3_area,
       ST_Area(ST_Intersection(a2.shapefile_geometry, a3.shapefile_geometry)) a23_area
  FROM p_rif40_geolevels_geometry_ew01_cntry2001 a2 CROSS JOIN p_rif40_geolevels_geometry_ew01_gor2001 a3
 WHERE ST_Intersects(a2.shapefile_geometry, a3.shapefile_geometry)
), x34 AS ( -* Subqueries x34 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a3.area_id AS gor2001, a4.area_id AS ladua2001,
       ST_Area(a4.shapefile_geometry) AS a4_area,
       ST_Area(ST_Intersection(a3.shapefile_geometry, a4.shapefile_geometry)) a34_area
  FROM p_rif40_geolevels_geometry_ew01_gor2001 a3 CROSS JOIN p_rif40_geolevels_geometry_ew01_ladua2001 a4
 WHERE ST_Intersects(a3.shapefile_geometry, a4.shapefile_geometry)
), x45 AS ( -* Subqueries x45 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a4.area_id AS ladua2001, a5.area_id AS ward2001,
       ST_Area(a5.shapefile_geometry) AS a5_area,
       ST_Area(ST_Intersection(a4.shapefile_geometry, a5.shapefile_geometry)) a45_area
  FROM p_rif40_geolevels_geometry_ew01_ladua2001 a4 CROSS JOIN p_rif40_geolevels_geometry_ew01_ward2001 a5
 WHERE ST_Intersects(a4.shapefile_geometry, a5.shapefile_geometry)
), x56 AS ( -* Subqueries x56 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a5.area_id AS ward2001, a6.area_id AS soa2001,
       ST_Area(a6.shapefile_geometry) AS a6_area,
       ST_Area(ST_Intersection(a5.shapefile_geometry, a6.shapefile_geometry)) a56_area
  FROM p_rif40_geolevels_geometry_ew01_ward2001 a5 CROSS JOIN p_rif40_geolevels_geometry_ew01_soa2001 a6
 WHERE ST_Intersects(a5.shapefile_geometry, a6.shapefile_geometry)
), x67 AS ( -* Subqueries x67 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a6.area_id AS soa2001, a7.area_id AS oa2001,
       ST_Area(a7.shapefile_geometry) AS a7_area,
       ST_Area(ST_Intersection(a6.shapefile_geometry, a7.shapefile_geometry)) a67_area
  FROM p_rif40_geolevels_geometry_ew01_soa2001 a6 CROSS JOIN p_rif40_geolevels_geometry_ew01_oa2001 a7
 WHERE ST_Intersects(a6.shapefile_geometry, a7.shapefile_geometry)
), y AS ( -* Join x78 ... x67intersections, pass through the computed areas, compute intersected area/higher resolution geolevel area,
             compute maximum intersected area/higher resolution geolevel area using an analytic partition of all
             duplicate higher resolution geolevels *-
SELECT x12.scntry2001, 
       x12.cntry2001, 
       x23.gor2001, 
       x34.ladua2001, 
       x45.ward2001, 
       x56.soa2001, 
       x67.oa2001, 
       CASE WHEN x12.a2_area > 0 THEN x12.a12_area/x12.a2_area ELSE NULL END test12,
       MAX(x12.a12_area/x12.a2_area) OVER (PARTITION BY x12.cntry2001) AS max12,
       CASE WHEN x23.a3_area > 0 THEN x23.a23_area/x23.a3_area ELSE NULL END test23,
       MAX(x23.a23_area/x23.a3_area) OVER (PARTITION BY x23.gor2001) AS max23,
       CASE WHEN x34.a4_area > 0 THEN x34.a34_area/x34.a4_area ELSE NULL END test34,
       MAX(x34.a34_area/x34.a4_area) OVER (PARTITION BY x34.ladua2001) AS max34,
       CASE WHEN x45.a5_area > 0 THEN x45.a45_area/x45.a5_area ELSE NULL END test45,
       MAX(x45.a45_area/x45.a5_area) OVER (PARTITION BY x45.ward2001) AS max45,
       CASE WHEN x56.a6_area > 0 THEN x56.a56_area/x56.a6_area ELSE NULL END test56,
       MAX(x56.a56_area/x56.a6_area) OVER (PARTITION BY x56.soa2001) AS max56,
       CASE WHEN x67.a7_area > 0 THEN x67.a67_area/x67.a7_area ELSE NULL END test67,
       MAX(x67.a67_area/x67.a7_area) OVER (PARTITION BY x67.oa2001) AS max67
  FROM x12, x23, x34, x45, x56, x67
 WHERE x12.cntry2001 = x23.cntry2001
   AND x23.gor2001 = x34.gor2001
   AND x34.ladua2001 = x45.ladua2001
   AND x45.ward2001 = x56.ward2001
   AND x56.soa2001 = x67.soa2001
)
SELECT -* Select y intersection, eliminating duplicates using selecting the lower geolevel resolution
         with the largest intersection by area for each (higher resolution) geolevel *-
       scntry2001, cntry2001, gor2001, ladua2001, ward2001, soa2001, oa2001
  FROM y
 WHERE max12 = test12
   AND max23 = test23
   AND max34 = test34
   AND max45 = test45
   AND max56 = test56
   AND max67 = test67
 ORDER BY 1, 2, 3, 4;

 */
DECLARE
	c1_hier CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id;
	c2_hier CURSOR(l_geography VARCHAR) FOR
		SELECT * FROM pg_indexes
		 WHERE schemaname = USER
		   AND tablename IN (SELECT DISTINCT LOWER(hierarchytable)
				       FROM rif40_geographies
				      WHERE geography = l_geography)
		 ORDER BY 1;	
	c3 REFCURSOR;
	c4_hier CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c1_rec t_rif40_geolevels%ROWTYPE;
	c2_rec rif40_geographies%ROWTYPE;
	c3_rec	RECORD;
	c4_rec rif40_geographies%ROWTYPE;
--
	columns		VARCHAR;
	sql_stmt 	VARCHAR;
	i		INTEGER:=0;
	num_geolevels	INTEGER:=0;
--
	geolevel_name			VARCHAR[];
	shapefile_table      		VARCHAR[];
 	shapefile_area_id_column	VARCHAR[];
 	shapefile_desc_column		VARCHAR[];
 	geolevel_table			VARCHAR[];
--
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
BEGIN
--
	stp:=clock_timestamp();
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'populate_hierarchy_table', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10003, 'populate_hierarchy_table', 'NULL geography parameter');
	END IF;	
--
	OPEN c4_hier(l_geography);
	FETCH c4_hier INTO c4_rec;
	CLOSE c4_hier;
--
	IF c4_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10004, 'populate_hierarchy_table', 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */);
	END IF;	
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'populate_hierarchy_table', 'Populating % geography hierarchy table: %',
		l_geography, c4_rec.hierarchytable);
--
-- INSERT statement
--
	sql_stmt:='INSERT INTO '||quote_ident(LOWER(c4_rec.hierarchytable))||' (';
	FOR c1_rec IN c1_hier(l_geography) LOOP
		i:=i+1;
		geolevel_name[i]:=quote_ident(LOWER(c1_rec.geolevel_name));
		shapefile_table[i]:=quote_ident(LOWER(c1_rec.shapefile_table));      	
 		shapefile_area_id_column[i]:=quote_ident(LOWER(c1_rec.shapefile_area_id_column));	
 		shapefile_desc_column[i]:=quote_ident(LOWER(c1_rec.shapefile_desc_column));	
		geolevel_table[i]:=quote_ident('p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c1_rec.geolevel_name));
		IF i = 1 THEN
			columns:=geolevel_name[i];
		ELSE
			columns:=columns||', '||geolevel_name[i];
		END IF;
	END LOOP;
	num_geolevels:=i;
	IF num_geolevels = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10017, 'populate_hierarchy_table', 'No rows found in: t_rif40_geolevels for geography %', 
			l_geography::VARCHAR /* Geography */);
	END IF;
	sql_stmt:=sql_stmt||columns||')'||E'\n';
--
-- Start SELECT statement; WITH clause; aggreagate geometries
--
-- Removed ST_Union for performance reasons
--

--
-- WITH clause - INTERSECTION
--
	FOR i IN 1 .. num_geolevels LOOP /* WITH clause - INTERSECTION */
/* E.g

x23 AS (
	SELECT a2.area_id AS level2, a3.area_id AS level3,
  	       ST_Area(a3.geom) AS a3_area,
	       ST_Area(ST_Intersection(a2.geom, a3.geom)) a23_area
          FROM a2 CROSS JOIN a3
	 WHERE ST_Intersects(a2.geom, a3.geom)
 */
		IF i = 1 THEN
			sql_stmt:=sql_stmt||
				'WITH x'||i||i+1||' AS ( /* Subqueries x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||': intersection aggregate geometries starting from the lowest resolution.'||E'\n'||
         		      	E'\t'||'       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.'||E'\n'||
 	       			E'\t'||'       Calculate the area of the higher resolution geolevel and the area of the intersected area */'||E'\n'||
				'SELECT a'||i||'.area_id AS '||geolevel_name[i]||', a'||i+1||'.area_id AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.shapefile_geometry) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.shapefile_geometry, a'||i+1||'.shapefile_geometry)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||geolevel_table[i]||' a'||i||' CROSS JOIN '||geolevel_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.shapefile_geometry, a'||i+1||'.shapefile_geometry)'||E'\n'||
				'), ';
		ELSIF i < (num_geolevels-1) THEN
			sql_stmt:=sql_stmt||
				'x'||i||i+1||' AS ( /* Subqueries x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||': intersection aggregate geometries starting from the lowest resolution.'||E'\n'||
         		      	E'\t'||'       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.'||E'\n'||
 	       			E'\t'||'       Calculate the area of the higher resolution geolevel and the area of the intersected area */'||E'\n'||
				'SELECT a'||i||'.area_id AS '||geolevel_name[i]||', a'||i+1||'.area_id AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.shapefile_geometry) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.shapefile_geometry, a'||i+1||'.shapefile_geometry)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||geolevel_table[i]||' a'||i||' CROSS JOIN '||geolevel_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.shapefile_geometry, a'||i+1||'.shapefile_geometry)'||E'\n'||
				'), ';
		ELSIF i < num_geolevels THEN
/* E.g.

 x34 AS (
	SELECT a3.level3, a4.level4,
	       total_a3_gid, total_a4_gid,
  	       ST_Area(a4.geom) AS a4_area,
	       ST_Area(ST_Intersection(a3.geom, a4.geom)) a34_area
          FROM a3 CROSS JOIN a4
	 WHERE ST_Intersects(a3.geom, a4.geom)
*/
			sql_stmt:=sql_stmt||
				'x'||i||i+1||' AS ( /* Subqueries x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||': intersection aggregate geometries starting from the lowest resolution.'||E'\n'||
         		      	E'\t'||'       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.'||E'\n'||
 	       			E'\t'||'       Calculate the area of the higher resolution geolevel and the area of the intersected area */'||E'\n'||
				'SELECT a'||i||'.area_id AS '||geolevel_name[i]||', a'||i+1||'.area_id AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.shapefile_geometry) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.shapefile_geometry, a'||i+1||'.shapefile_geometry)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||geolevel_table[i]||' a'||i||' CROSS JOIN '||geolevel_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.shapefile_geometry, a'||i+1||'.shapefile_geometry)'||E'\n'||
				'), ';
		END IF;
	END LOOP;
--
-- Compute intersected area, order analytically
--

/*
y AS ( 
	SELECT x12.level1, x12.level2, x23.level3, x34.level4, 
	       CASE WHEN a2_area > 0 THEN a12_area/a2_area ELSE NULL END test12,
	       CASE WHEN a3_area > 0 THEN a23_area/a3_area ELSE NULL END test23,
	       CASE WHEN a4_area > 0 THEN a34_area/a4_area ELSE NULL END test34,
	       MAX(a12_area/a2_area) OVER (PARTITION BY x12.level2) AS max12,
	       MAX(a23_area/a3_area) OVER (PARTITION BY x23.level3) AS max23,
	       MAX(a34_area/a4_area) OVER (PARTITION BY x34.level4) AS max34
	  FROM x12, x23, x34
	 WHERE x12.level2 = x23.level2
   	   AND x23.level3 = x34.level3
)
 */
	sql_stmt:=sql_stmt||
		'y AS ( /* Join x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||
			'intersections, pass through the computed areas, compute intersected area/higher resolution geolevel area,'||E'\n'||
		E'\t'||'     compute maximum intersected area/higher resolution geolevel area using an analytic partition of all'||E'\n'||
		E'\t'||'     duplicate higher resolution geolevels */'||E'\n';
	FOR i IN 1 .. num_geolevels LOOP /* x12.level1, x12.level2, x23.level3, x34.level4, */
		IF i < num_geolevels THEN
			IF i = 1 THEN
				sql_stmt:=sql_stmt||
					'SELECT x'||i||i+1||'.'||geolevel_name[i]||', '||E'\n';
			END IF;
			sql_stmt:=sql_stmt||
				'       x'||i||i+1||'.'||geolevel_name[i+1]||', '||E'\n';
		END IF;
	END LOOP;
	FOR i IN 1 .. num_geolevels LOOP /* CASE MAX analytic clause */ 
		IF i < num_geolevels THEN
			sql_stmt:=sql_stmt||
	    		   	'       CASE WHEN x'||i||i+1||'.a'||i+1||'_area > 0 THEN x'||i||i+1||'.a'||i||i+1||'_area/x'||i||i+1||'.a'||i+1||
				'_area ELSE NULL END test'||i||i+1||','||E'\n';
			sql_stmt:=sql_stmt||
				'       MAX(x'||i||i+1||'.a'||i||i+1||'_area/x'||i||i+1||'.a'||i+1||'_area)'||
				' OVER (PARTITION BY x'||i||i+1||'.'||geolevel_name[i+1]||') AS max'||i||i+1||','||E'\n';
		END IF;
	END LOOP;
	sql_stmt:=SUBSTR(sql_stmt, 1, LENGTH(sql_stmt)-LENGTH(','||E'\n')) /* Chop off last ",\n" */||E'\n';
	FOR i IN 1 .. num_geolevels LOOP /* FROM clause */ 
		IF i < num_geolevels THEN
			IF i = 1 THEN
				sql_stmt:=sql_stmt||
					'  FROM x'||i||i+1;
			ELSE
				sql_stmt:=sql_stmt||
					', x'||i||i+1;
			END IF;
		END IF;
	END LOOP;
	FOR i IN 1 .. (num_geolevels-2) LOOP /* WHERE clause */ 
		IF i = 1 THEN
			sql_stmt:=sql_stmt||E'\n'||
				' WHERE x'||i||i+1||'.'||geolevel_name[i+1]||' = x'||i+1||i+2||'.'||geolevel_name[i+1];
		ELSE
			sql_stmt:=sql_stmt||E'\n'||
				'   AND x'||i||i+1||'.'||geolevel_name[i+1]||' = x'||i+1||i+2||'.'||geolevel_name[i+1];
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||E'\n'||')'||E'\n';
--
-- Final SELECT
--
	sql_stmt:=sql_stmt||'SELECT /* Select y intersection, eliminating duplicates using selecting the lower geolevel resolution'||E'\n'||
         E'\t'||' with the largest intersection by area for each (higher resolution) geolevel */'||E'\n'||'       '||columns||E'\n';
	sql_stmt:=sql_stmt||'  FROM y'||E'\n';
/*
SELECT level1, level2, level3, level4,
  FROM y
 WHERE max12 = test12
   AND max23 = test23
   AND max34 = test34
 ORDER BY 1, 2, 3, 4;  
 */
	FOR i IN 1 .. num_geolevels LOOP /* FROM clause */ 
		IF i < num_geolevels THEN
			IF i = 1 THEN
				sql_stmt:=sql_stmt||' WHERE max'||i||i+1||' = test'||i||i+1||E'\n';
			ELSE
				sql_stmt:=sql_stmt||'   AND max'||i||i+1||' = test'||i||i+1||E'\n';
			END IF;
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||' ORDER BY 1, 2, 3, 4';
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'populate_hierarchy_table', 'SQL> %', sql_stmt::VARCHAR);
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Check rows were inserted
--
	sql_stmt:='SELECT COUNT(*) AS total FROM '||quote_ident(LOWER(c4_rec.hierarchytable));
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'populate_hierarchy_table', 'SQL> %', sql_stmt::VARCHAR);
	OPEN c3 FOR EXECUTE sql_stmt;
	FETCH c3 INTO c3_rec;
	CLOSE c3;	
	IF c3_rec.total = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10016, 'populate_hierarchy_table', 'No rows found in % geography hierarchy table: %', 
			l_geography::VARCHAR 			/* Geography */,
			quote_ident(LOWER(c4_rec.hierarchytable))	/* Hierarchy table */);
	END IF;
--
-- Re-index
--
	FOR c2_rec IN c2_hier(l_geography) LOOP
		sql_stmt:='REINDEX INDEX /* '||quote_ident(c2_rec.tablename)||' */ '||quote_ident(c2_rec.indexname);
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
--
-- Analyze
--
	sql_stmt:='ANALYZE VERBOSE '||quote_ident(LOWER(c4_rec.hierarchytable));
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Check intersections
--
	PERFORM rif40_sql_pkg.rif40_method4('SELECT level3 FROM sahsuland_level3
EXCEPT
SELECT level3 FROM sahsuland_geography', 'TEST');
	PERFORM rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(l_geography);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'populate_hierarchy_table', 'Populated % rows in % geography hierarchy table: % in %', 
		c3_rec.total::VARCHAR 					/* Rows */,
		l_geography::VARCHAR 					/* Geography */,
		quote_ident(LOWER(c4_rec.hierarchytable))::VARCHAR	/* Hierarchy table */,
		took::VARCHAR						/* Time taken */);
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.populate_hierarchy_table(VARCHAR) IS 'Function: 	populate_hierarchy_table()
Parameters:	geography
Returns:	Nothing
Description:	Populate hierarchy table
		Check rows were inserted
		Re-index
		Analyze
		Check intersections

This algorithm implments the prototype in: ../postgres/gis_intersection_prototype.sql. The SAHSU alogoritm differs in that
Where intersections overlap (e.g. level4 appears in more than 1 level3) the level3 with the greatest intersected area is chosen 
This is differed to ARCGis - see the prototype file for a more detailed investigation

This function must be run after rif40_geo_pkg.populate_rif40_geometry_tables() i.e. area_id, shapefile_geometry and shapefile_geometry must exist

Population SQL used to fail with (fixed with ST_MakeValid()):

ERROR:  Error performing intersection: TopologyException: found non-noded intersection between LINESTRING (-2.99198 53.3669, -2.98262 53.367) and LINESTRING (-2.98556 53.367, -2.98854 53.3655) at -2.9855578257498308 53.366966593247653

To test for invalid polygons:

SELECT area_id, ST_Isvalid(shapefile_geometry) AS v1, ST_Isvalid(shapefile_geometry) AS v2
  FROM p_rif40_geolevels_geometry_ew01_oa2001
 WHERE NOT ST_Isvalid(shapefile_geometry) OR NOT ST_Isvalid(shapefile_geometry);

NOTICE:  Self-intersection at or near point -2.9855576987063932 53.366966657364905
NOTICE:  Self-intersection at or near point -2.9855576987063932 53.366966657364905
NOTICE:  Self-intersection at or near point -4.1539493321927816 51.672864859911542
NOTICE:  Self-intersection at or near point -4.1539493321927816 51.672864859911542
NOTICE:  Self-intersection at or near point 0.27233850962861539 50.88057609104721
NOTICE:  Self-intersection at or near point 0.27233850962861539 50.88057609104721
  area_id   | v1 | v2 
------------+----+----
 00CBFD0032 | t  | f
 00NUQQ0006 | t  | f
 21UHHJ0015 | t  | f
(3 rows)

Example population SQL>

INSERT INTO ew2001_geography (scntry2001, cntry2001, gor2001, ladua2001, ward2001, soa2001, oa2001)
WITH x12 AS ( /* Subqueries x12 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a1.area_id AS scntry2001, a2.area_id AS cntry2001,
       ST_Area(a2.shapefile_geometry) AS a2_area,
       ST_Area(ST_Intersection(a1.shapefile_geometry, a2.shapefile_geometry)) a12_area
  FROM p_rif40_geolevels_geometry_ew01_scntry2001 a1 CROSS JOIN p_rif40_geolevels_geometry_ew01_cntry2001 a2
 WHERE ST_Intersects(a1.shapefile_geometry, a2.shapefile_geometry)
), x23 AS ( /* Subqueries x23 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a2.area_id AS cntry2001, a3.area_id AS gor2001,
       ST_Area(a3.shapefile_geometry) AS a3_area,
       ST_Area(ST_Intersection(a2.shapefile_geometry, a3.shapefile_geometry)) a23_area
  FROM p_rif40_geolevels_geometry_ew01_cntry2001 a2 CROSS JOIN p_rif40_geolevels_geometry_ew01_gor2001 a3
 WHERE ST_Intersects(a2.shapefile_geometry, a3.shapefile_geometry)
), x34 AS ( /* Subqueries x34 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a3.area_id AS gor2001, a4.area_id AS ladua2001,
       ST_Area(a4.shapefile_geometry) AS a4_area,
       ST_Area(ST_Intersection(a3.shapefile_geometry, a4.shapefile_geometry)) a34_area
  FROM p_rif40_geolevels_geometry_ew01_gor2001 a3 CROSS JOIN p_rif40_geolevels_geometry_ew01_ladua2001 a4
 WHERE ST_Intersects(a3.shapefile_geometry, a4.shapefile_geometry)
), x45 AS ( /* Subqueries x45 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a4.area_id AS ladua2001, a5.area_id AS ward2001,
       ST_Area(a5.shapefile_geometry) AS a5_area,
       ST_Area(ST_Intersection(a4.shapefile_geometry, a5.shapefile_geometry)) a45_area
  FROM p_rif40_geolevels_geometry_ew01_ladua2001 a4 CROSS JOIN p_rif40_geolevels_geometry_ew01_ward2001 a5
 WHERE ST_Intersects(a4.shapefile_geometry, a5.shapefile_geometry)
), x56 AS ( /* Subqueries x56 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a5.area_id AS ward2001, a6.area_id AS soa2001,
       ST_Area(a6.shapefile_geometry) AS a6_area,
       ST_Area(ST_Intersection(a5.shapefile_geometry, a6.shapefile_geometry)) a56_area
  FROM p_rif40_geolevels_geometry_ew01_ward2001 a5 CROSS JOIN p_rif40_geolevels_geometry_ew01_soa2001 a6
 WHERE ST_Intersects(a5.shapefile_geometry, a6.shapefile_geometry)
), x67 AS ( /* Subqueries x67 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a6.area_id AS soa2001, a7.area_id AS oa2001,
       ST_Area(a7.shapefile_geometry) AS a7_area,
       ST_Area(ST_Intersection(a6.shapefile_geometry, a7.shapefile_geometry)) a67_area
  FROM p_rif40_geolevels_geometry_ew01_soa2001 a6 CROSS JOIN p_rif40_geolevels_geometry_ew01_oa2001 a7
 WHERE ST_Intersects(a6.shapefile_geometry, a7.shapefile_geometry)
), y AS ( /* Join x78 ... x67intersections, pass through the computed areas, compute intersected area/higher resolution geolevel area,
             compute maximum intersected area/higher resolution geolevel area using an analytic partition of all
             duplicate higher resolution geolevels */
SELECT x12.scntry2001, 
       x12.cntry2001, 
       x23.gor2001, 
       x34.ladua2001, 
       x45.ward2001, 
       x56.soa2001, 
       x67.oa2001, 
       CASE WHEN x12.a2_area > 0 THEN x12.a12_area/x12.a2_area ELSE NULL END test12,
       MAX(x12.a12_area/x12.a2_area) OVER (PARTITION BY x12.cntry2001) AS max12,
       CASE WHEN x23.a3_area > 0 THEN x23.a23_area/x23.a3_area ELSE NULL END test23,
       MAX(x23.a23_area/x23.a3_area) OVER (PARTITION BY x23.gor2001) AS max23,
       CASE WHEN x34.a4_area > 0 THEN x34.a34_area/x34.a4_area ELSE NULL END test34,
       MAX(x34.a34_area/x34.a4_area) OVER (PARTITION BY x34.ladua2001) AS max34,
       CASE WHEN x45.a5_area > 0 THEN x45.a45_area/x45.a5_area ELSE NULL END test45,
       MAX(x45.a45_area/x45.a5_area) OVER (PARTITION BY x45.ward2001) AS max45,
       CASE WHEN x56.a6_area > 0 THEN x56.a56_area/x56.a6_area ELSE NULL END test56,
       MAX(x56.a56_area/x56.a6_area) OVER (PARTITION BY x56.soa2001) AS max56,
       CASE WHEN x67.a7_area > 0 THEN x67.a67_area/x67.a7_area ELSE NULL END test67,
       MAX(x67.a67_area/x67.a7_area) OVER (PARTITION BY x67.oa2001) AS max67
  FROM x12, x23, x34, x45, x56, x67
 WHERE x12.cntry2001 = x23.cntry2001
   AND x23.gor2001 = x34.gor2001
   AND x34.ladua2001 = x45.ladua2001
   AND x45.ward2001 = x56.ward2001
   AND x56.soa2001 = x67.soa2001
)
SELECT /* Select y intersection, eliminating duplicates using selecting the lower geolevel resolution
         with the largest intersection by area for each (higher resolution) geolevel */
       scntry2001, cntry2001, gor2001, ladua2001, ward2001, soa2001, oa2001
  FROM y
 WHERE max12 = test12
   AND max23 = test23
   AND max34 = test34
   AND max45 = test45
   AND max56 = test56
   AND max67 = test67
 ORDER BY 1, 2, 3, 4

The following SQL snippet causes ERROR:  invalid join selectivity: 1.000000 in PostGIS 2.1.1 (fixed in 2.2.1/2.1.2 - to be release May 3rd 2014)

See: http://trac.osgeo.org/postgis/ticket/2543

SELECT a2.area_id AS level2, a3.area_id AS level3,
       ST_Area(a3.shapefile_geometry) AS a3_area,
       ST_Area(ST_Intersection(a2.shapefile_geometry, a3.shapefile_geometry)) AS a23_area
  FROM p_rif40_geolevels_geometry_sahsu_level3 a3, p_rif40_geolevels_geometry_sahsu_level2 a2  
 WHERE ST_Intersects(a2.shapefile_geometry, a3.shapefile_geometry);

';

--
-- Eof