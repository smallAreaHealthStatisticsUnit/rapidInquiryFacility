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
-- Rapid Enquiry Facility (RIF) - Create PL/Pgsql Geographic simplification internal code
--								  Phase I: Create the points table
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
-- Drop old and new (without st_simplify_tolerance) forms
--
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_I(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_I(VARCHAR, VARCHAR, VARCHAR, NUMERIC);

CREATE OR REPLACE FUNCTION rif40_geo_pkg._simplify_geometry_phase_I(
	l_geography VARCHAR, l_geolevel VARCHAR, l_filter VARCHAR DEFAULT NULL, 
	l_min_point_resolution NUMERIC DEFAULT 1 /* metre */)
RETURNS void 
SECURITY INVOKER
AS $body$
DECLARE
/*

Function: 	_simplify_geometry_phase_I()
Parameters:	Geography, geolevel, 
                geolevel; filter (for testing, no default), 
                minimum point resolution (default 1 - assumed metre, but depends on the geometry)
Returns:	Nothing
Description:	Simplify geography geolevel - Phase I: Create the points table

Phase I: Create the points table

Test 0 for valid geometry
Create a sequence so that points are in the original order specified by ST_DumpPoints()
Create table simplification_points_a and convert geometry to points, reduce precision to <l_min_point_resolution>m
from the relevent geolevel geometry table. Give each polygon a separate polygon number

Test 11: Check there are 1 or more distinct areas in simplification_points_a (i.e. filter is not broken)
Print out polygon/points reports [limited to first 20 rows]

a) Print points per first two dimensions (i.e. [1] and [2])
b) Polygons per area
c) Multiple polygons

Test 1 - check last_pointflow = one per area (should be zero)

Create table simplification_points_b from simplification_points_a to:

a) Round first and last points in area (were not rounded in the previous step); convert to points
b) Create the variables needed to detect duplcates within co-ordinates (a side effect of the round in the previous statement)

Note that a duplicate co-ordinate is defined as being within an area_id and coordinate pair; polygons are ignored 
as self intersections were removed by ST_Buffer(geom, 0.0) during shapefile geometry load into the geography geometry table

The geometry is not checked for validity at this time; hence test 0 above. This is to allow the initial load
to complete even if there are uncorrectable errors in the geometry.

Create table simplification_points_c to detect and classify duplcates within co-ordinates (a side effect of the 
round) from simplification_points_b:

O: original - first or last point in area
N: Not a dupicate
Y: A dupicate (two or more consecutive points sharing the same co-ordinate); removed later
L: Loop duplicate (two or more NON consecutive points sharing the same co-ordinate)

Print duplicate flags
Print numbers of multiple loop and origin flags per area. There will only be one origin; but a small number  of multiple loops. 
Display first two "L" (loop) duplicates and the three rows either side
Test 2.1 for invalid duplicate flag: "?"
Create table simplification_points_d to count up number of distinct polygon_number using the same
co-ordinate from simplification_points_c

List by number of distinct polygon_number using the same co-ordinate

num_polygon_number (join type if >1)
------------------ -----------------

1            Outer edge (no join)
2            Join between two polygons
3            Triple point - junction of three polygons
...
N            Nth point - junction of N polygons

Test 2 for un-detected duplicates

Join up adjacent edges by update of simplification_points_d
List joins by number of distinct polygon_number using the same co-ordinate
List of joinable co-ordinates by duplicate type
Create table simplification_points_e to categorise join types from simplification_points_d:

 Join code	Description						Increment join sequence
 ---------	-----------						-----------------------
 A		Start of polygon					YES
 B		Junction						YES
 C		Junction (previous area_id)				YES
 D		Duplicate and change in area_id				YES
 V		End of polygon						NO
 W		next area_id == current, but previous != current	YES
		(Normally there is a three cornered point and this does not occur as there is a NULL in the next/prev area_id)
 X		next == current						NO
 Y		Previous area_id == current				NO
 Z		ELSE							YES

Create the points table: simplification_points from simplification_points_e adding in joinable points

Note that a duplicate co-ordinate is defined as being within an area_id and coordinate pair; polygons are ignored 
as self intersections were removed by ST_Buffer(geom, 0.0) during shapefile geometry load into the geography geometry table

The geometry is not checked for validity at this time; hence test 0 above. This is to allow the initial load
to complete even if there are uncorrectable errors in the geometry.

Test 3 and 4 - Checks - MUST BE 0
	
a) Areas with no join sequence
b) Areas with mis-joined sequences

Create a list of joined_polygon_numbers as temporary table simplification_points_f (i.e. the num_join_seq is 1)

Test 5: For join sequence(s) used by more than one polygon_number
Test 6: For polygon_number, coord_id, duplicate(s) used by more than one join sequence

Update simplification_points.joined_join_seq using joined_polygon_numbers in simplification_points_f (i.e. the num_join_seq is 1) 
joining on joined_polygon_number, coord_id and the duplicate flag

Display number of polygons and duplicate flag
Display number of polygons joined at a point (1=edge; 2=shared polygon boundary between 2 polygons; 3+: triple or more point)
List duplicates
Test 7: areas(s) with non unique join sequences.
 */
	c1_sI	CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c2_sI CURSOR(l_geography VARCHAR, l_geolevel_name VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		   AND geolevel_name = l_geolevel_name;
--
	c1_rec rif40_geographies%ROWTYPE;
	c2_rec t_rif40_geolevels%ROWTYPE;
--
	sql_stmt	VARCHAR[];
	l_sql_stmt	VARCHAR;
--
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
--
	rel_size BIGINT=0;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, '_simplify_geometry_phase_I', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	stp:=clock_timestamp();
--
	OPEN c1_sI(l_geography);
	FETCH c1_sI INTO c1_rec;
	CLOSE c1_sI;
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10091, '_simplify_geometry_phase_I', 'No geography specified');
	END IF;
	OPEN c2_sI(l_geography, l_geolevel);
	FETCH c2_sI INTO c2_rec;
	CLOSE c2_sI;
	IF c2_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10092, '_simplify_geometry_phase_I', 'No geolevel name specified for geography: %',
			l_geography::VARCHAR);
	ELSIF c1_rec.srid IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10094, '_simplify_geometry_phase_I', 'No srid specified for geography: % geolevel: %',
			l_geography::VARCHAR);
	ELSE
--
		PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_phase_I', 'Phase I (points creation) for geography: % geolevel: %. Parameters - l_min_point_resolution: %; units: %',
			l_geography::VARCHAR,
			l_geolevel::VARCHAR,
			l_min_point_resolution::VARCHAR,
			rif40_geo_pkg.get_srid_projection_parameters(l_geography, '+units')::VARCHAR);
	END IF;
--
-- Test 0 for valid geometry
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 0);
--
-- Create a sequence so that points are in the original order specified by ST_DumpPoints()
--
	sql_stmt[1]:='DROP SEQUENCE IF EXISTS simplification_points_seq';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP SEQUENCE IF EXISTS simplification_points_poly_seq';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE SEQUENCE simplification_points_seq';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE SEQUENCE simplification_points_poly_seq';
--
-- Create table simplification_points_a and convert geometry to points, reduce precision to <l_min_point_resolution>m
-- from the relevent geolevel geometry table
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE IF EXISTS simplification_points_a';
	l_sql_stmt:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE simplification_points_a'||E'\n'||
'AS'||E'\n'||
'WITH a AS ('||E'\n'||
'        SELECT area_id,'||E'\n'||
'               ST_DumpPoints(shapefile_geometry) AS geometry_data,     /* Convert geometry to points */'||E'\n'||
'               nextval(''simplification_points_seq'') AS point_order_seq'||E'\n'||
'          FROM '||quote_ident('p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(l_geolevel))||E'\n'||
'         WHERE geolevel_name = '''||UPPER(l_geolevel)||''''||E'\n';
	IF l_filter IS NOT NULL THEN
		l_sql_stmt:=l_sql_stmt||E'\t'||'   AND '||l_filter||E'\n';
	END IF;
	sql_stmt[array_length(sql_stmt, 1)+1]:=l_sql_stmt||'), b /* Extract points and point order within a polygon */ AS ('||E'\n'||
'	SELECT area_id, point_order_seq /* Point order in shapefile */,'||E'\n'||
'	       (geometry_data).geom AS points,				/* Points component */'||E'\n'||
'	       (geometry_data).path AS path,				/* Path component */'||E'\n'||
'	       CASE'||E'\n'||
'			WHEN (geometry_data).path[3] = 1 /* OR */       /* New polygon */ '||E'\n'||
'		/*	     LAG((geometry_data).path[1]) OVER x < (geometry_data).path[1] OR'||E'\n'||
'			     LAG((geometry_data).path[2]) OVER x < (geometry_data).path[2] */ THEN nextval(''simplification_points_poly_seq'')'||E'\n'||
'			ELSE                                            /* Current polygon */      currval(''simplification_points_poly_seq'')'||E'\n'||
'	       END AS polygon_number'||E'\n'||
'	  FROM a'||E'\n'||
'	/* WINDOW x AS (ORDER BY point_order_seq) */'||E'\n'||
')'||E'\n'||
'SELECT area_id, polygon_number, path, '||E'\n'||
'       ST_MakePoint(ROUND(CAST(st_X(points) AS numeric), '||l_min_point_resolution||'),'||E'\n'||
'               ROUND(CAST(st_Y(points) AS numeric), '||l_min_point_resolution||')) /* Reduce precision to <l_min_point_resolution> m */ AS coordinate, '||E'\n'||
'       ROW_NUMBER() OVER w AS pointflow, '||E'\n'||
'       COUNT(point_order_seq) OVER w AS last_pointflow,'||E'\n'|| 
'       LAST_VALUE(points) OVER w AS lastpoint,                         /* Lastpoint should be the same as the first */'||E'\n'||
'       FIRST_VALUE(points) OVER w AS firstpoint                        /* firstpoint should be the same as the first */'||E'\n'||
'  FROM b'||E'\n'||
'	WINDOW w AS (PARTITION BY polygon_number ORDER BY point_order_seq RANGE BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)'||E'\n'||
'                    /* Disable window sliding (i.e. COUNT/LAST_VALUE works over the whole partition) */';
--
-- Total runtime: 108228.675 ms => 149895.648 ms (with ORDER BY)
-- Indexes are not efficient - PK implmented in the third step
--
-- Not used in tune
--
--	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_points_a ADD PRIMARY KEY(polygon_number, pointflow)';
-- 	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_a_coordinate ON simplification_points_a(coordinate)';
-- 	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_a_lastpoint ON simplification_points_a(last_pointflow)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_points_a';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP SEQUENCE simplification_points_seq';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP SEQUENCE simplification_points_poly_seq';
--
-- Execute first block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_points_a');
--
-- Test 11: Check there are 1 or more distinct areas in simplification_points_a (i.e. filter is not broken)
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 11);
--
-- Check array dimensions are [1:3] as expected (Test 22)
--
PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 22);
--
-- Print out polygon/points reports [limited to first 20 rows]
--
-- a) Print points per first two dimensions (i.e. [1] and [2])
--
	PERFORM rif40_sql_pkg.rif40_method4('SELECT path[1] AS path_1, path[2] AS path_2, COUNT(*)  AS total '||E'\n'|| 
'         FROM simplification_points_a GROUP BY path[1], path[2] LIMIT 10', 'Check array dimensions are [1:3] as expected');
/*
path_1     | path_2     | total               
-------------------------------------------------
8          | 1          | 23693               
1          | 1          | 487364              
4          | 1          | 307                 
7          | 1          | 123                 
3          | 1          | 29486               
5          | 1          | 344                 
2          | 1          | 107159              
6          | 1          | 44093               
(8 rows)
 */
--
-- b) Polygons per area
--
	PERFORM rif40_sql_pkg.rif40_method4('SELECT area_id, '||E'\n'|| 
'       MAX(pointflow) AS max_points, '||E'\n'||
'       MIN(polygon_number) AS min_polynum, MAX(polygon_number) AS max_polynum, COUNT(DISTINCT(polygon_number)) AS tot_polys'||E'\n'||
'  FROM simplification_points_a'||E'\n'||
' GROUP BY area_id'||E'\n'||
' ORDER BY MIN(polygon_number) LIMIT 20', 'Polygons per area');
/*
area_id                                  | max_points           | min_polynum          | max_polynum          | tot_polys           
---------------------------------------------------------------------------------------------------------------------------------------
00HG                                     | 5679                 | 1                    | 3                    | 3                   
00HH                                     | 2756                 | 4                    | 4                    | 1                   
00HA                                     | 13801                | 5                    | 5                    | 1                   
00HB                                     | 13541                | 6                    | 6                    | 1                   
00HN                                     | 4185                 | 7                    | 7                    | 1                   
00HC                                     | 10540                | 8                    | 9                    | 2                   
00HP                                     | 3774                 | 10                   | 10                   | 1                   
00HD                                     | 13719                | 11                   | 11                   | 1                   
00HX                                     | 8448                 | 12                   | 12                   | 1                   
15UH                                     | 44                   | 13                   | 13                   | 1                   
18UC                                     | 3009                 | 14                   | 14                   | 1                   
18UD                                     | 22904                | 15                   | 15                   | 1                   
18UE                                     | 24122                | 16                   | 16                   | 1                   
18UG                                     | 40614                | 17                   | 18                   | 2                   
15UB                                     | 22907                | 19                   | 20                   | 2                   
15UC                                     | 23040                | 21                   | 23                   | 3                   
15UD                                     | 25569                | 24                   | 25                   | 2                   
15UE                                     | 43967                | 26                   | 31                   | 6                   
15UF                                     | 23693                | 32                   | 39                   | 8                   
15UG                                     | 21136                | 40                   | 40                   | 1                   
(20 rows)
 */
--
-- c) Multiple polygons
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH a AS /* Multiple polygons */ ('||E'\n'||
'	SELECT area_id, COUNT(DISTINCT(polygon_number)) AS tot_poly'||E'\n'||
'	  FROM simplification_points_a'||E'\n'||
'	 GROUP BY area_id'||E'\n'||
'	HAVING COUNT(DISTINCT(polygon_number)) > 1'||E'\n'||
')'||E'\n'||
'SELECT b.area_id, polygon_number, MIN(pointflow) AS min_points, MAX(pointflow) AS max_points, COUNT(pointflow) AS tot_points'||E'\n'||
'  FROM simplification_points_a b, a'||E'\n'||
' WHERE a.area_id = b.area_id'||E'\n'||
' GROUP BY b.area_id, polygon_number'||E'\n'||
' ORDER BY b.area_id, polygon_number LIMIT 20', 'Multiple polygons');
/*
area_id                                  | polygon_number       | min_points           | max_points           | tot_points          
---------------------------------------------------------------------------------------------------------------------------------------
00HC                                     | 8                    | 1                    | 305                  | 305                 
00HC                                     | 9                    | 1                    | 10540                | 10540               
00HG                                     | 1                    | 1                    | 182                  | 182                 
00HG                                     | 2                    | 1                    | 385                  | 385                 
00HG                                     | 3                    | 1                    | 5679                 | 5679                
15UB                                     | 19                   | 1                    | 427                  | 427                 
15UB                                     | 20                   | 1                    | 22907                | 22907               
15UC                                     | 21                   | 1                    | 47                   | 47                  
15UC                                     | 22                   | 1                    | 151                  | 151                 
15UC                                     | 23                   | 1                    | 23040                | 23040               
15UD                                     | 24                   | 1                    | 228                  | 228                 
15UD                                     | 25                   | 1                    | 25569                | 25569               
15UE                                     | 26                   | 1                    | 79                   | 79                  
15UE                                     | 27                   | 1                    | 95                   | 95                  
15UE                                     | 28                   | 1                    | 717                  | 717                 
15UE                                     | 29                   | 1                    | 104                  | 104                 
15UE                                     | 30                   | 1                    | 214                  | 214                 
15UE                                     | 31                   | 1                    | 43967                | 43967               
15UF                                     | 32                   | 1                    | 82                   | 82                  
15UF                                     | 33                   | 1                    | 112                  | 112                 
(20 rows)
 */
--
-- Test 1 - check last_pointflow = one per area (should be zero)
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 1);
--
-- Second block of SQL statements
--
	sql_stmt:=NULL;	/* Empty statement array */
-- 
-- Create table simplification_points_b from simplification_points_a to:
-- a) Round first and last points in area (were not rounded in the previous step); convert to points
-- b) Create the variables needed to detect duplcates within co-ordinates (a side effect of the round in the previous statement)
--
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_points_b';
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE simplification_points_b'||E'\n'||
'AS'||E'\n'||
'SELECT area_id, pointflow, polygon_number, last_pointflow,'||E'\n'||
'       ST_MakePoint(ROUND(CAST(st_X(firstpoint) AS numeric), '||l_min_point_resolution||'),
		ROUND(CAST(st_Y(firstpoint) AS numeric), '||l_min_point_resolution||')) AS firstpoint,	/* Convert firstpoint to POINT() datatype */'||E'\n'||
'       ST_MakePoint(ROUND(CAST(st_X(lastpoint) AS numeric), '||l_min_point_resolution||'),
		ROUND(CAST(st_Y(lastpoint) AS numeric), '||l_min_point_resolution||')) AS lastpoint,	/* Convert lastpoint to POINT() datatype */'||E'\n'||
'       coordinate,'||E'\n'||
'       COUNT(pointflow) OVER w AS tot_dup_coords, 		/* Total duplicate co-ordinates within an area_id and coordinate pair */'||E'\n'||
'       LAG(pointflow) OVER w AS prev_pointflow, 		/* Previous pointflow within a duplicate */'||E'\n'||
'       LEAD(polygon_number) OVER w AS next_polygon_number, 	/* Next polygon_number */'||E'\n'||
'       LAG(polygon_number) OVER w AS prev_polygon_number, 	/* Previous polygon_number */'||E'\n'||
'       ROW_NUMBER() OVER w AS dup_coord_order 			/* Find pointflows with the same coordinate [i.e. side effect of the ROUND()] */'||E'\n'||
'  FROM simplification_points_a'||E'\n'||
'WINDOW w AS (PARTITION BY polygon_number, coordinate ORDER BY pointflow)'||E'\n'||
' ORDER BY coordinate, polygon_number /* Optimise index: simplification_points_b_coordinate; not the primary key */';
--
-- Note that a duplicate co-ordinate is defined as being within an area_id and coordinate pair; polygons are ignored 
-- as self intersections were removed by ST_Buffer(geom, 0.0) during shapefile geometry load into the geography geometry table
--
-- The geometry is not checked for validity at this time; hence test 0 above. This is to allow the initial load
-- to complete even if there are uncorrectable errors in the geometry.
--
--
-- PK(simplification_points_b_pkey) was used, simplification_points_b_pointflow_last_pointflow in preference
-- 
--	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_points_b ADD PRIMARY KEY(polygon_number, pointflow)';
--
-- Added for tune
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_b_area_id ON simplification_points_b(area_id)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_b_pointflow_last_pointflow ON simplification_points_b(pointflow, last_pointflow)';
-- None of these improved matters
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_b_coordinate ON simplification_points_b(coordinate)';
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_b_lastpoint_coordinate ON simplification_points_b(lastpoint, coordinate)';
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_b_coord_last_pointflow ON simplification_points_b(coordinate, last_pointflow)';
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_b_last_pointflow ON simplification_points_b(last_pointflow)';
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_b_pointflow ON simplification_points_b(pointflow)';
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_b_lastpoint ON simplification_points_b(lastpoint)';
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_b_firstpoint ON simplification_points_b(firstpoint)';
--
-- Unable to fix CTE b full table scan - need to find out why the optimiser ignored the indexes (i.e. 10054 type trace). An Oracle trace would have:
--
-- HASH(join)
--   HASH(join)
--     INDEX RANGE SCAN(last_pointflow)
--     INDEX RANGE SCAN(pointflow)
--   HASH(join)
--     INDEX RANGE SCAN(coordinate)
--     INDEX RANGE SCAN(lastpoint)
--
-- Or with composite indexes:
--
--   HASH(join)
--     INDEX FAST FULL SCAN(last_pointflow, pointflow)
--     INDEX FAST FULL SCAN(coordinate, lastpoint)
--
/*
[WITH] b AS (
	SELECT area_id, coordinate AS lastpoint, pointflow AS last_pointflow
	  FROM simplification_points_b
	 WHERE last_pointflow = pointflow AND coordinate = lastpoint
)

  CTE b
    ->  Seq Scan on pg_temp_2.simplification_points_b  (cost=0.00..182218.22 rows=26 width=151) (actual time=1.945..1637.435 rows=3380 loops=1)
          Output: pg_temp_2.simplification_points_b.area_id, pg_temp_2.simplification_points_b.coordinate, pg_temp_2.simplification_points_b.pointflow
          Filter: ((pg_temp_2.simplification_points_b.coordinate = pg_temp_2.simplification_points_b.lastpoint) AND (pg_temp_2.simplification_points_b.last_pointflow = pg_temp_2.simplification_points_b.pointflow))
 */
 	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE simplification_points_a';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_points_b';
-- 164s for WARD in London
--
-- Create table simplification_points_c to detect and classify duplcates within co-ordinates (a side effect of the 
-- round) from simplification_points_b:
--
-- O: original - first or last point in area
-- N: Not a duplicate
-- Y: A duplicate (two or more consecutive points sharing the same co-ordinate); removed later
-- L: Loop duplicate (two or more NON consecutive points sharing the same co-ordinate)
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE IF EXISTS simplification_points_c';
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE simplification_points_c'||E'\n'||
'AS'||E'\n'||
'WITH a /* First point in polygon_number */ AS ('||E'\n'||
'	SELECT polygon_number, coordinate AS firstpoint, pointflow AS first_pointflow'||E'\n'||
'	  FROM simplification_points_b'||E'\n'||
'	 WHERE pointflow = 1 AND coordinate = firstpoint'||E'\n'||
'), b /* Last point in polygon */ AS ('||E'\n'||
'	SELECT polygon_number, coordinate AS lastpoint, pointflow AS last_pointflow'||E'\n'||
'	  FROM simplification_points_b'||E'\n'||
'	 WHERE last_pointflow = pointflow AND coordinate = lastpoint'||E'\n'||
'), c /* Total polygons */ AS ('||E'\n'||
'	SELECT DISTINCT polygon_number'||E'\n'||
'	  FROM simplification_points_b'||E'\n'||
'), d AS ('||E'\n'||
'	SELECT c.polygon_number,'||E'\n'||
'       	       CASE WHEN a.firstpoint = b.lastpoint THEN 1 ELSE 0 END origin_verified'||E'\n'||
'	  FROM c'||E'\n'||
'	 	LEFT OUTER JOIN a ON (c.polygon_number = a.polygon_number)'||E'\n'||
' 		LEFT OUTER JOIN b ON (c.polygon_number = b.polygon_number)'||E'\n'||
')'||E'\n'||
'SELECT area_id, e.polygon_number, prev_polygon_number, pointflow, last_pointflow, tot_dup_coords, dup_coord_order, prev_pointflow, origin_verified,'||E'\n'||
'      DENSE_RANK() OVER (ORDER BY coordinate) AS coord_id,		/* Add coordinate index number per geolevel */'||E'\n'||
'      coordinate,'||E'\n'||
'      CASE 								/* Is co-ordinate a duplicate within a polygon? */'||E'\n'||
'		WHEN pointflow = 1 AND coordinate = firstpoint  THEN                                       ''O'' /* Origin */'||E'\n'||
'		WHEN last_pointflow = pointflow AND coordinate = lastpoint AND d.origin_verified = 1 THEN  ''E'' /* End (same as Origin) */ '||E'\n'||
'		WHEN tot_dup_coords = 1 OR (tot_dup_coords > 1  AND dup_coord_order = 1) THEN              ''N'' /* No */'||E'\n'||
'		WHEN tot_dup_coords > 1 AND dup_coord_order > 1 AND'||E'\n'||
'		        prev_pointflow + 1 = pointflow THEN                                                ''Y'' /* Yes */'||E'\n'||
'		WHEN tot_dup_coords > 1 AND dup_coord_order > 1 AND '||E'\n'||
'			prev_pointflow IS NOT NULL AND prev_pointflow + 1 != pointflow THEN                ''L'' /* Loop */'||E'\n'||
'		ELSE                                                                                       ''?'' /* Unknown */'||E'\n'||
'      END duplicate							/* Flag duplicates */'||E'\n'||
'  FROM simplification_points_b e, d'||E'\n'||
' WHERE e.polygon_number = d.polygon_number'||E'\n'||
' ORDER BY coord_id, polygon_number /* Sort order in next statement */';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_points_c ADD PRIMARY KEY(polygon_number, pointflow)';
--
-- Added for tune
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_c_coord_id ON simplification_points_c(coord_id)';
--
-- Not needed - OK to remove - try again with multi polygon mode
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_c_polygon_number ON simplification_points_c(polygon_number)';
-- 
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_points_c';
--
-- Execute second block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_points_b');
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_points_c');
--
-- Print duplicate flags
--
	PERFORM rif40_sql_pkg.rif40_method4('SELECT duplicate, COUNT(DISTINCT(polygon_number)) AS t_polys, COUNT(polygon_number) AS t_points'||E'\n'||
'  FROM simplification_points_c'||E'\n'||
' GROUP BY duplicate', 'Print duplicate flags');
/*
duplicate | t_polys              | t_points            
----------------------------------------------------------
E         | 66                   | 66                  
L         | 14                   | 23                  
N         | 66                   | 679904              
O         | 66                   | 66                  
Y         | 60                   | 12510               
(5 rows)              
 */
--
-- Print numbers of multiple loop and origin flags per area
-- There will only be one origin; but a small number  of multiple loops. 
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH a AS ('||E'\n'||
'	SELECT duplicate, polygon_number, COUNT(duplicate) AS t_duplicate'||E'\n'||
'	  FROM simplification_points_c'||E'\n'||
'	 WHERE duplicate IN (''O'' /* Origin */, ''L'' /* Loop */)'||E'\n'||
'	 GROUP BY duplicate, polygon_number'||E'\n'||
')'||E'\n'||
'SELECT duplicate, t_duplicate, COUNT(DISTINCT(polygon_number)) AS t_polys, COUNT(polygon_number) AS t_points'||E'\n'||
'  FROM a'||E'\n'||
' GROUP BY duplicate, t_duplicate', 'Print numbers of multiple loop and origin flags per area');
/*
duplicate | t_duplicate          | t_polys              | t_points            
---------------------------------------------------------------------------------
L         | 1                    | 7                    | 7                   
L         | 2                    | 5                    | 5                   
L         | 3                    | 2                    | 2                   
O         | 1                    | 66                   | 66                  
(4 rows)
 */
--
-- Display first two 'L' (loop) duplicates and the three rows either side
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH a AS ('||E'\n'||
'	SELECT polygon_number, pointflow'||E'\n'||
' 	  FROM simplification_points_c'||E'\n'||
'	 WHERE duplicate = ''L'' LIMIT 2'||E'\n'||
'), b AS ('||E'\n'||
'	SELECT polygon_number, pointflow-3 AS pointflow'||E'\n'||
'	  FROM a'||E'\n'||
'	UNION'||E'\n'||
'	SELECT polygon_number, pointflow-2'||E'\n'||
'	  FROM a'||E'\n'||
'	UNION'||E'\n'||
'	SELECT polygon_number, pointflow-1'||E'\n'||
'	  FROM a'||E'\n'||
'	UNION'||E'\n'||
'	SELECT polygon_number, pointflow'||E'\n'||
'	  FROM a'||E'\n'||
'	UNION'||E'\n'||
'	SELECT polygon_number, pointflow+1'||E'\n'||
'	  FROM a'||E'\n'||
'	UNION'||E'\n'||
'	SELECT polygon_number, pointflow+2'||E'\n'||
'	  FROM a'||E'\n'||
'	UNION'||E'\n'||
'	SELECT polygon_number, pointflow+3'||E'\n'||
'	  FROM a'||E'\n'||
')'||E'\n'||
'SELECT area_id, c.polygon_number AS poly_no, c.pointflow,'||E'\n'||
'       last_pointflow AS last_pf, tot_dup_coords, dup_coord_order AS dup_co, prev_pointflow AS prev_pf, origin_verified AS orig_ver,'||E'\n'||
'       duplicate, coord_id'||E'\n'||
'  FROM simplification_points_c c, b'||E'\n'||
' WHERE b.pointflow      = c.pointflow'||E'\n'||
'   AND b.polygon_number = c.polygon_number'||E'\n'||
' ORDER BY area_id, c.polygon_number, c.pointflow', 'Display first two ''L'' (loop) duplicates and the three rows either side');

/*
 area_id | poly_no | pointflow | last_pf | tot_dup_coords | dup_co | prev_pf | orig_ver | duplicate | coord_id 
---------+---------+-----------+---------+----------------+--------+---------+----------+-----------+----------
 15UE    |      31 |     20751 |   43967 |              1 |      1 |         |        1 | N         |    62605
 15UE    |      31 |     20752 |   43967 |              1 |      1 |         |        1 | N         |    62625
 15UE    |      31 |     20753 |   43967 |              1 |      1 |         |        1 | N         |    62626
 15UE    |      31 |     20754 |   43967 |              2 |      2 |   20752 |        1 | L         |    62625
 15UE    |      31 |     20755 |   43967 |              1 |      1 |         |        1 | N         |    62612
 15UE    |      31 |     20756 |   43967 |              1 |      1 |         |        1 | N         |    62611
 15UE    |      31 |     20757 |   43967 |              1 |      1 |         |        1 | N         |    62583
 15UF    |      39 |      9615 |   23693 |              1 |      1 |         |        1 | N         |     6915
 15UF    |      39 |      9616 |   23693 |              1 |      1 |         |        1 | N         |     6902
 15UF    |      39 |      9617 |   23693 |              1 |      1 |         |        1 | N         |     6903
 15UF    |      39 |      9618 |   23693 |              2 |      2 |    9616 |        1 | L         |     6902
 15UF    |      39 |      9619 |   23693 |              1 |      1 |         |        1 | N         |     6904
 15UF    |      39 |      9620 |   23693 |              1 |      1 |         |        1 | N         |     6898
 15UF    |      39 |      9621 |   23693 |              1 |      1 |         |        1 | N         |     6929
(14 rows)
 */
--
-- Test 2.1 for invalid duplicate flag: '?'
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 21);
--
-- Third block of SQL statements
--
	sql_stmt:=NULL;	/* Empty statement array */
--
-- Create table simplification_points_d to count up number of distinct polygon_number using the same co-ordinate from simplification_points_c
--
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_points_d';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE simplification_points_b';
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE simplification_points_d'||E'\n'||
'AS'||E'\n'||
'WITH f AS ('||E'\n'||
'	SELECT coord_id,'||E'\n'||
'	       COUNT(DISTINCT(polygon_number)) AS num_polygon_number	/* Count up number of distinct polygons using the same co-ordinate */'||E'\n'||	
'	 FROM simplification_points_c'||E'\n'||
'	 GROUP BY coord_id'||E'\n'||
') '||E'\n'||
'SELECT area_id, polygon_number, pointflow, e.coord_id, coordinate, tot_dup_coords, dup_coord_order, prev_pointflow, duplicate,'||E'\n'||
'       f.num_polygon_number'||E'\n'||
'  FROM  simplification_points_c e'||E'\n'||
'		LEFT OUTER JOIN f ON (e.coord_id = f.coord_id)'||E'\n'||
' ORDER BY polygon_number, pointflow';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_points_d ADD PRIMARY KEY(polygon_number, pointflow)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_d_coord_id ON simplification_points_d(coord_id)';
--
-- Added for tune
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_d_polygon_number ON simplification_points_d(polygon_number)';
--
-- None of these improved matters
--
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_d_num_polygon_number ON simplification_points_d(num_polygon_number)';
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_d_duplicate ON simplification_points_d(duplicate)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_points_d ADD joined_polygon_number VARCHAR(300)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE simplification_points_c';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_points_d';
--
-- Execute third block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- List by number of distinct polygon_number using the same co-ordinate
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH a AS ('||E'\n'||
'	SELECT COUNT(coord_id) AS total_coords'||E'\n'||
' 	  FROM simplification_points_d'||E'\n'||
')'||E'\n'||
'SELECT num_polygon_number	/* Count of distinct polygons using the same co-ordinate */,'||E'\n'||
'       COUNT(num_polygon_number) AS num_coords,'||E'\n'||
'       ROUND((CAST(COUNT(num_polygon_number) AS numeric)/a.total_coords)*100.0, 4) AS pct'||E'\n'||
'  FROM simplification_points_d, a'||E'\n'||
' GROUP BY num_polygon_number, a.total_coords'||E'\n'||
' ORDER BY num_polygon_number', 'List by number of distinct polygon_number using the same co-ordinate');
/*
num_polygon_number   | num_coords           | pct
----------------------------------------------------
1                    | 245339               | 35.
2                    | 447043               | 64.
3                    | 187                  | 0.0
 */
--
-- num_polygon_number (join type if >1)
-- ------------------ -----------------
--
-- 1                  Outer edge (no join)
-- 2                  Join between two polygons
-- 3                  Triple point - junction of three polygons
-- ...
-- N            Nth point - junction of N polygons
--
-- Test 2 for un-detected duplicates
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 2);
--
-- Fourth block of SQL statements
--
	sql_stmt:=NULL;	/* Empty statement array */
--
-- Join up adjacent edges by update of simplification_points_d
--
/*
Update on pg_temp_2.simplification_points_d g1  (cost=0.00..16936.95 rows=1438 width=197) (actual time=9.395..9.395 rows=0 loops=1)
  ->  Index Scan using simplification_points_d_polygon_number on pg_temp_2.simplification_points_d g1  (cost=0.00..16936.95 rows=1438 width=197) (actual time=0.075..2.242 rows=380 loops=1)
        Output: g1.area_id, g1.polygon_number, g1.pointflow, g1.coord_id, g1.coordinate, g1.tot_dup_coords, g1.dup_coord_order, g1.prev_pointflow, g1.duplicate, g1.num_polygon_number, (SubPlan 1), g1.ctid
        Index Cond: (g1.polygon_number = 2)
        Filter: (g1.duplicate = ANY ('{N,O,L}'::text[]))
        SubPlan 1
          ->  Unique  (cost=0.00..11.72 rows=1 width=8) (actual time=0.003..0.004 rows=1 loops=380)
                Output: g2.polygon_number
                ->  Index Scan using simplification_points_d_coord_id on pg_temp_2.simplification_points_d g2  (cost=0.00..11.71 rows=1 width=8) (actual time=0.003..0.003 rows=1 loops=380)
                      Output: g2.polygon_number
                      Index Cond: (g1.coord_id = g2.coord_id)
                      Filter: ((g1.polygon_number = g2.polygon_number) AND ((g1.duplicate = g2.duplicate) OR ((g1.duplicate = 'N'::text) AND (g2.duplicate = 'O'::text)) OR ((g1.duplicate = 'O'::text) AND (g2.duplicate = 'N'::text))))
Total runtime: 9.433 ms
 */
-- 
-- This code does NOT handle polygon outside edges (i.e. where num_polygon_number = 1)
--
	sql_stmt[1]:='EXPLAIN ANALYZE VERBOSE UPDATE simplification_points_d g1'||E'\n'||
'   SET joined_polygon_number = ('||E'\n'||
'	SELECT DISTINCT g2.polygon_number AS joined_polygon_number'||E'\n'||
'	  FROM simplification_points_d g2'||E'\n'||
'         WHERE g1.num_polygon_number = g2.num_polygon_number			/* Join type matches */'||E'\n'||
'	    AND g1.coord_id           = g2.coord_id 				/* Co-ordinates are the same */'||E'\n'||
'	    AND g1.polygon_number    != g2.polygon_number         		/* Polygons are NOT the same */'||E'\n'||
'	    AND ('||E'\n'||
'		 (g1.duplicate        = g2.duplicate) OR 			/* Duplicate flags match */'||E'\n'||
'		 (g1.duplicate        = ''N'' AND g2.duplicate = ''O'') OR 	/* Handle different origins */'||E'\n'||
'		 (g1.duplicate        = ''O'' AND g2.duplicate = ''N'')'||E'\n'||
'               )'||E'\n'||
'	)'||E'\n'||
' WHERE g1.num_polygon_number = 2	/* Join points shared between two polygons only - i.e. shares linestrings */'||E'\n'||
'   AND g1.duplicate IN (''N'' /* Not a duplicate */, ''O'' /* Origin */, ''L'' /* Loop */)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_d_joined_polygon_number ON simplification_points_d(joined_polygon_number)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_points_d';
--
-- Execute fourth block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- List joins by number of distinct polygon_number using the same co-ordinate
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH a AS ('||E'\n'||
'	SELECT COUNT(coord_id) AS total_coords'||E'\n'||
' 	  FROM simplification_points_d'||E'\n'||
')'||E'\n'||
'SELECT num_polygon_number	/* Count of distinct polygons using the same co-ordinate */,'||E'\n'||
'       COUNT(num_polygon_number) AS num_coords,'||E'\n'||
'       COUNT(joined_polygon_number) AS num_joined_coords,'||E'\n'||
'       ROUND((CAST(COUNT(joined_polygon_number) AS numeric)/a.total_coords)*100.0, 4) AS pct_joined'||E'\n'||
'  FROM simplification_points_d, a'||E'\n'||
' GROUP BY num_polygon_number, a.total_coords'||E'\n'||
' ORDER BY num_polygon_number', 'List joins by number of distinct polygon_number using the same co-ordinate');
/*
num_polygon_number   | num_coords           | num_joined_coords    | pct_joined
----------------------------------------------------------------------------------
1                    | 245339               | 0                    | 0.0000    
2                    | 447043               | 439422               | 63.4481   
3                    | 187                  | 0                    | 0.0000  
 */
--
-- List of joinable co-ordinates by duplicate type
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH b AS ('||E'\n'||
'	SELECT COUNT(duplicate) AS gtotal'||E'\n'||
'	  FROM simplification_points_d'||E'\n'||
')'||E'\n'||
'SELECT duplicate,'||E'\n'||
'       CASE'||E'\n'||
'		WHEN duplicate IN (''N'' /* Not a duplicate */, ''O'' /* Origin */, ''L'' /* Loop */) AND'||E'\n'||
'		     polygon_number IN (1, 2) THEN ''Joinable: ''||tot_dup_coords::Text'||E'\n'||
'		ELSE                               ''Not Joinable: ''||tot_dup_coords::Text'||E'\n'||
'       END join_type,'||E'\n'||
'       COUNT(pointflow) AS points,'||E'\n'||
'       COUNT(DISTINCT(polygon_number)) AS polygons,'||E'\n'||
'       ROUND((CAST(COUNT(pointflow) AS numeric)/b.gtotal)*100.0, 4) AS pct_points,'||E'\n'||
'       SUM(CASE WHEN duplicate = ''Y'' THEN 1 ELSE 0 END) AS tot_dups,'||E'\n'||
'       ROUND((CAST(SUM(CASE WHEN duplicate = ''Y'' THEN 1 ELSE 0 END) AS numeric)/b.gtotal)*100.0, 4) AS pct_dup,'||E'\n'||
'       COUNT(joined_polygon_number) AS tot_joined, ROUND((CAST(COUNT(joined_polygon_number) AS numeric)/b.gtotal)*100.0, 4) AS pct_joined,'||E'\n'||
'       COUNT(pointflow)- COUNT(joined_polygon_number) AS tot_unjoined,'||E'\n'|| 
'       ROUND((CAST(COUNT(pointflow)- COUNT(joined_polygon_number) AS numeric)/b.gtotal)*100.0, 4) AS pct_unjoined'||E'\n'||
'  FROM simplification_points_d, b'||E'\n'||
' GROUP BY duplicate,'||E'\n'|| 
'       CASE'||E'\n'|| 
'		WHEN duplicate IN (''N'' /* Not a duplicate */, ''O'' /* Origin */, ''L'' /* Loop */) AND'||E'\n'||
'		     polygon_number IN (1, 2) THEN ''Joinable: ''||tot_dup_coords::Text'||E'\n'||
'		ELSE                               ''Not Joinable: ''||tot_dup_coords::Text'||E'\n'|| 
'       END,'||E'\n'||
'       b.gtotal'||E'\n'||
' ORDER BY 1, 2', 'List of joinable co-ordinates by duplicate type');
/*
duplicate | join_type | points               | polygons             | pct_points | tot_dups             | pct_dup | tot_joined           | pct_joined | tot_unjoined         | pct_unjoined
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
E         | "Not Join | 60                   | 60                   | 0.0087     | 0                    | 0.0000  | 0                    | 0.0000     | 60                   | 0.0087      
E         | "Not Join | 6                    | 6                    | 0.0009     | 0                    | 0.0000  | 0                    | 0.0000     | 6                    | 0.0009      
L         | "Not Join | 23                   | 14                   | 0.0033     | 0                    | 0.0000  | 6                    | 0.0009     | 17                   | 0.0025      
N         | "Joinable | 379                  | 1                    | 0.0547     | 0                    | 0.0000  | 0                    | 0.0000     | 379                  | 0.0547      
N         | "Not Join | 679525               | 65                   | 98.1166    | 0                    | 0.0000  | 439383               | 63.4425    | 240142               | 34.6741     
O         | "Joinable | 1                    | 1                    | 0.0001     | 0                    | 0.0000  | 0                    | 0.0000     | 1                    | 0.0001      
O         | "Not Join | 65                   | 65                   | 0.0094     | 0                    | 0.0000  | 33                   | 0.0048     | 32                   | 0.0046      
Y         | "Not Join | 12060                | 60                   | 1.7413     | 12060                | 1.7413  | 0                    | 0.0000     | 12060                | 1.7413      
Y         | "Not Join | 447                  | 41                   | 0.0645     | 447                  | 0.0645  | 0                    | 0.0000     | 447                  | 0.0645      
Y         | "Not Join | 3                    | 3                    | 0.0004     | 3                    | 0.0004  | 0                    | 0.0000     | 3                    | 0.0004      
(10 rows)
 */

--
-- Fifth block of SQL statements
--
	sql_stmt:=NULL;	/* Empty statement array */
--
-- Create table simplification_points_e to categorise join types from simplification_points_d:
--
-- Join code	Description						Increment join sequence
-- ---------	-----------						-----------------------
-- A		Start of polygon					YES
-- B		Junction						YES
-- C		Junction (previous polygon_number)			YES
-- D		Loop duplicate and change in polygon_number		YES
-- V		End of polygon						NO
-- W		next polygon_number == current, but previous != current	YES
--		(Normally there is a three cornered point and this does not occur as there is a NULL in the next/prev polygon_number)
-- X		next == current						NO
-- Y		Previous polygon_number == current			NO
-- Z		ELSE							YES
--

	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_points_e';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE SEQUENCE simplification_points_seq';
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE simplification_points_e'||E'\n'||
'AS'||E'\n'||
'WITH a AS ('||E'\n'||
'	SELECT area_id, polygon_number, pointflow, coord_id, coordinate, duplicate, num_polygon_number, joined_polygon_number,'||E'\n'||
'	       LAG(polygon_number) OVER w 		AS prev_polygon_number,'||E'\n'||
'	       LAG(num_polygon_number) OVER w 		AS prev_num_polygon_number,'||E'\n'||
'	       LAG(pointflow) OVER w 			AS prev_pointflow,'||E'\n'||
'	       LAG(joined_polygon_number) OVER w 	AS prev_joined_polygon_number,'||E'\n'||
'	       LEAD(pointflow) OVER w 			AS next_pointflow,'||E'\n'||
'	       LEAD(joined_polygon_number) OVER w 	AS next_joined_polygon_number,'||E'\n'||
'	       LEAD(polygon_number) OVER w 		AS next_polygon_number,'||E'\n'||
'	       LEAD(num_polygon_number) OVER w 		AS next_num_polygon_number'||E'\n'||
'	  FROM simplification_points_d'||E'\n'||
'	 WHERE duplicate NOT IN(''E'', ''Y'')		/* Exclude end point and duplicates */'||E'\n'||
'	WINDOW w AS (PARTITION BY polygon_number ORDER BY pointflow)'||E'\n'||
')'||E'\n'||
'SELECT area_id, polygon_number::bigint, pointflow::bigint, duplicate, coord_id::bigint, coordinate, num_polygon_number::bigint, joined_polygon_number::bigint,'||E'\n'||
'       next_polygon_number::bigint, next_pointflow::bigint, next_joined_polygon_number::bigint,'||E'\n'||
'       prev_polygon_number::bigint, prev_pointflow::bigint, prev_joined_polygon_number::bigint,'||E'\n'||
'       CASE'||E'\n'|| 
'		WHEN pointflow = 1                                              THEN ''A'' /* nextval: start */'||E'\n'||
'		WHEN num_polygon_number > 2                                     THEN ''B'' /* nextval: junction */'||E'\n'||
'		WHEN prev_num_polygon_number > 2                                THEN ''C'' /* nextval: junction */'||E'\n'||
'		WHEN duplicate = ''L'' AND next_joined_polygon_number IS NULL   THEN ''D'' /* nextval: duplicate and change in polygon_number */'||E'\n'||
'		WHEN num_polygon_number = 1 AND next_num_polygon_number = 1     THEN ''E'' /* currval: outside edge */'||E'\n'||
'		WHEN pointflow != 1 AND duplicate = ''O''                       THEN ''V'' /* currval: end */'||E'\n'||
'		WHEN polygon_number  = next_polygon_number AND joined_polygon_number = next_joined_polygon_number'||E'\n'|| 
'			AND joined_polygon_number != prev_joined_polygon_number THEN ''W'' /* nextval: next == current, but previous != current */'||E'\n'||
'			/* Normally there is a three cornered point and this does not occur as there is a NULL in the next/prev polygon_number */'||E'\n'||
'		WHEN polygon_number  = next_polygon_number AND'||E'\n'|| 
'			joined_polygon_number = next_joined_polygon_number      THEN ''X'' /* currval: next == current */'||E'\n'||
'		WHEN polygon_number  = prev_polygon_number AND'||E'\n'|| 
'			joined_polygon_number = prev_joined_polygon_number      THEN ''Y'' /* currval: previous == current */'||E'\n'||
'		ELSE ''Z'' /* nextval */'||E'\n'||
'       END join_code,'||E'\n'||
'       CASE'||E'\n'|| 
'		WHEN pointflow = 1                                              THEN nextval(''simplification_points_seq'') /* nextval: start */'||E'\n'||
'		WHEN num_polygon_number > 2                                     THEN nextval(''simplification_points_seq'') /* nextval: junction */'||E'\n'||
'		WHEN prev_num_polygon_number > 2                                THEN nextval(''simplification_points_seq'') /* nextval: junction */'||E'\n'||
'		WHEN duplicate = ''L'' AND next_joined_polygon_number IS NULL   THEN nextval(''simplification_points_seq'') /* nextval: duplicate and change in polygon_number */'||E'\n'||
'		WHEN num_polygon_number = 1 AND next_num_polygon_number = 1     THEN currval(''simplification_points_seq'') /* currval: outside edge */'||E'\n'||
'		WHEN pointflow != 1 AND duplicate = ''O''                       THEN currval(''simplification_points_seq'') /* currval: end */'||E'\n'||
'		WHEN polygon_number  = next_polygon_number AND joined_polygon_number = next_joined_polygon_number'||E'\n'|| 
'			AND joined_polygon_number != prev_joined_polygon_number THEN nextval(''simplification_points_seq'') /* nextval: next == current, but previous != current */'||E'\n'||
'			/* Normally there is a three cornered point and this does not occur as there is a NULL in the next/prev polygon_number */'||E'\n'||
'		WHEN polygon_number  = next_polygon_number AND'||E'\n'|| 
'			joined_polygon_number = next_joined_polygon_number      THEN currval(''simplification_points_seq'') /* currval: next == current */'||E'\n'||
'		WHEN polygon_number  = prev_polygon_number AND'||E'\n'|| 
'			joined_polygon_number = prev_joined_polygon_number      THEN currval(''simplification_points_seq'') /* currval: previous == current */'||E'\n'||
'		ELSE nextval(''simplification_points_seq'')'||E'\n'||
'       END join_seq'||E'\n'||
'  FROM a'||E'\n'||
' ORDER BY polygon_number, pointflow';
-- Statement took: 00:01:44.925395 (104.925395)
-- 
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP SEQUENCE IF EXISTS simplification_points_seq';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_points_e ADD PRIMARY KEY(polygon_number, pointflow)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_e_coord_id ON simplification_points_e(coord_id)';
--
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_e_polygon_number ON simplification_points_e(polygon_number)';
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_e_num_polygon_number ON simplification_points_e(num_polygon_number)';
--	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_e_duplicate ON simplification_points_e(duplicate)';
-- 
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_points_e';
--
-- Execute fifth block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_points_d');
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_points_e');
--
-- Sixth block of SQL statements
--
	sql_stmt:=NULL;	/* Empty statement array */
--
-- Create the points table: simplification_points from simplification_points_e adding in joinable points
--
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_points';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE simplification_points_d';
	IF l_filter IS NULL THEN /* Non test mode */
		l_sql_stmt:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE simplification_points'||E'\n';
	ELSE
		l_sql_stmt:='EXPLAIN ANALYZE VERBOSE CREATE TABLE simplification_points'||E'\n';
	END IF;
	sql_stmt[array_length(sql_stmt, 1)+1]:=l_sql_stmt||
'AS'||E'\n'||
'WITH b AS /* Joinable polygons */ ('||E'\n'||
'	SELECT joined_polygon_number, coord_id, duplicate, COUNT(DISTINCT(join_seq)) AS num_join_seq'||E'\n'||
'	  FROM simplification_points_e c'||E'\n'||
'	 WHERE num_polygon_number = 2'||E'\n'||
'	   AND joined_polygon_number IS NOT NULL'||E'\n'||
'	 GROUP BY joined_polygon_number, coord_id, duplicate'||E'\n'||
'	HAVING COUNT(DISTINCT(join_seq)) = 1'||E'\n'||
')'||E'\n'||
'SELECT c.area_id, polygon_number, pointflow, c.coord_id, coordinate, c.duplicate,'||E'\n'|| 
'       c.num_polygon_number, c.joined_polygon_number,'||E'\n'|| 
'       next_polygon_number, next_pointflow, next_joined_polygon_number,'||E'\n'||
'       prev_polygon_number, prev_pointflow, prev_joined_polygon_number,'||E'\n'||
'       join_seq, join_code, b.num_join_seq'||E'\n'||
'  FROM simplification_points_e c'||E'\n'||
'	LEFT OUTER JOIN b /* For joined_polygon_number */ ON ('||E'\n'||
' 	       b.joined_polygon_number = c.polygon_number'||E'\n'|| 
'	   AND b.coord_id              = c.coord_id'||E'\n'||
'           AND ('||E'\n'||
'		(b.duplicate           = c.duplicate) /* Same flag */ OR'||E'\n'||
'		(b.duplicate           = ''N'' AND c.duplicate = ''O'') OR /* Handle different origins */'||E'\n'||
'		(b.duplicate           = ''O'' AND c.duplicate = ''N'')'||E'\n'||
'               )'||E'\n'||
'	   AND c.num_polygon_number    = 2 /* Shared boundary between two polygons only */)'||E'\n'||
' ORDER BY polygon_number, pointflow';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_points ADD PRIMARY KEY(polygon_number, pointflow)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_num_join_seq ON simplification_points(num_join_seq)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_num_polygon_number ON simplification_points(num_polygon_number)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE simplification_points_e';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_points';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_points ADD joined_join_seq BIGINT';
--
-- Execute sixth block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Checks - MUST BE 0
--
-- a) Areas with no join sequence
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 3);
--
-- b) Areas with mis-joined sequences
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 4);

--
-- Seventh block of SQL statements
--
	sql_stmt:=NULL;	/* Empty statement array */
--
-- Create a list of joined_polygon_numbers as temporary table simplification_points_f (i.e. the num_join_seq is 1)
--
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_points_f';
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE simplification_points_f'||E'\n'||
'AS'||E'\n'||
'WITH a AS ('||E'\n'||
'	SELECT polygon_number AS joined_polygon_number, coord_id, duplicate, join_seq, COUNT(join_seq) AS total_join_seq'||E'\n'||
'	  FROM simplification_points'||E'\n'||
'	 WHERE num_join_seq  = 1'||E'\n'||
'	 GROUP BY polygon_number, coord_id, duplicate, join_seq'||E'\n'||
'), b /* Exclude a small number of loop duplicates that would break the primary key */ AS ('||E'\n'||
'	SELECT joined_polygon_number, coord_id, duplicate, COUNT(join_seq) AS total'||E'\n'||
'	  FROM a'||E'\n'||
'	 GROUP BY joined_polygon_number, coord_id, duplicate'||E'\n'||
'	HAVING COUNT(join_seq) > 1'||E'\n'||
')'||E'\n'||
'SELECT a.joined_polygon_number, a.coord_id, a.duplicate, a.join_seq, a.total_join_seq'||E'\n'||
'  FROM a'||E'\n'||
'	LEFT OUTER JOIN b ON (a.joined_polygon_number = b.joined_polygon_number AND a.coord_id = b.coord_id AND a.duplicate = b.duplicate)'||E'\n'||
' WHERE b.total IS NULL'||E'\n'||
' ORDER BY a.joined_polygon_number, a.coord_id, a.duplicate, a.join_seq';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_points_f ADD PRIMARY KEY(joined_polygon_number, coord_id, duplicate)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_points_f';
--
-- Execute seventh block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Test 5: For join sequence(s) used by more than one polygon_number
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 5);
--
-- Test 6: For polygon_number, coord_id, duplicate(s) used by more than one join sequence
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 6);
--
-- Seventh block of SQL statements
--
	sql_stmt:=NULL;	/* Empty statement array */
--
-- Update simplification_points.joined_join_seq using joined_polygon_numbers in simplification_points_f (i.e. the num_join_seq is 1) 
-- joining on joined_polygon_number, coord_id and the duplicate flag
--
	sql_stmt[1]:='EXPLAIN ANALYZE VERBOSE UPDATE simplification_points a'||E'\n'||
'   SET joined_join_seq = ('||E'\n'||
'	SELECT join_seq'||E'\n'||
'	  FROM simplification_points_f b1'||E'\n'||
' 	 WHERE a.joined_polygon_number = b1.joined_polygon_number'||E'\n'||
'	   AND a.coord_id       = b1.coord_id'||E'\n'||
'	   AND a.duplicate      = b1.duplicate)';
-- UPDATE 13795468 Time: 549892.370 ms (9 mins)
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE INDEX simplification_points_joined_join_seq ON simplification_points(joined_join_seq)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE simplification_points_f';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_points';
--
-- Execute seventh block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_points');
--
-- Display number of polygons and duplicate flag
--
	PERFORM rif40_sql_pkg.rif40_method4('SELECT num_polygon_number, duplicate, num_join_seq,'||E'\n'|| 
'       COUNT(joined_polygon_number) AS t_joined_polygon_number,'||E'\n'|| 
'       COUNT(join_seq) AS t_join_seq,'||E'\n'|| 
'       COUNT(joined_join_seq) AS t_joined_join_seq'||E'\n'||
'  FROM simplification_points'||E'\n'||
' GROUP BY num_polygon_number, duplicate, num_join_seq'||E'\n'||
' ORDER BY num_polygon_number, duplicate, num_join_seq', 'Nnumber of polygons and duplicate flag');
/*
num_polygon_number   | duplicate | num_join_seq         | t_joined_polygon_number | t_join_seq           | t_joined_join_seq   
----------------------------------------------------------------------------------------------------------------------------------
1                    | L         |                      | 0                       | 13                   | 0                   
1                    | N         |                      | 0                       | 240375               | 0                   
1                    | O         |                      | 0                       | 23                   | 0                   
2                    | L         | 1                    | 6                       | 6                    | 6                   
2                    | L         |                      | 0                       | 3                    | 0                   
2                    | N         | 1                    | 439383                  | 439383               | 439354              
2                    | O         | 1                    | 33                      | 33                   | 4                   
3                    | L         |                      | 0                       | 1                    | 0                   
3                    | N         |                      | 0                       | 146                  | 0                   
3                    | O         |                      | 0                       | 10                   | 0                   
(10 rows)
 */
--
-- Display number of polygons joined at a point (1=edge; 2=shared polygon boundary between 2 polygons; 3+: triple or more point)
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH b AS ('||E'\n'||
'	SELECT COUNT(coord_id) AS total_coords'||E'\n'||
' 	  FROM simplification_points'||E'\n'||
')'||E'\n'||
'SELECT num_polygon_number, SUM(num_polygon_number) AS num_coords, ROUND((CAST(SUM(num_polygon_number) AS numeric)/b.total_coords)*100.0, 4) AS pct'||E'\n'||
'  FROM ('||E'\n'||
'	SELECT coord_id, COUNT(polygon_number) AS num_polygon_number'||E'\n'||
' 	 FROM simplification_points'||E'\n'||
'	 GROUP BY coord_id) a, b'||E'\n'||
' GROUP BY num_polygon_number, b.total_coords'||E'\n'||
' ORDER BY num_polygon_number', 'Number of polygons joined at a point (1=edge; 2=shared polygon boundary between 2 polygons; 3+: triple or more point)');
/*
num_polygon_number   | num_coords | pct
------------------------------------------
1                    | 240385     | 35.
2                    | 439430     | 64.
3                    | 162        | 0.0
4                    | 16         | 0.0
 */
--
-- List duplicates
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH b AS ('||E'\n'||
'	SELECT COUNT(duplicate) AS gtotal'||E'\n'||
'	  FROM simplification_points'||E'\n'||
')'||E'\n'||
'SELECT duplicate,'||E'\n'||
'       COUNT(join_seq) AS tot_seqs, ROUND((CAST(COUNT(join_seq) AS numeric)/b.gtotal)*100.0, 4) AS pct_seq,'||E'\n'||
'       COUNT(joined_polygon_number) AS tot_joined, ROUND((CAST(COUNT(joined_polygon_number) AS numeric)/b.gtotal)*100.0, 4) AS pct_joined'||E'\n'||
'  FROM simplification_points, b'||E'\n'||
' GROUP BY duplicate, b.gtotal'||E'\n'||
' ORDER BY 1, 2', 'List duplicates');
/*
duplicate | tot_seqs             | pct_seq | tot_joined           | pct_joined
---------------------------------------------------------------------------------
L         | 23                   | 0.0034  | 6                    | 0.0009    
N         | 679904               | 99.9869 | 439383               | 64.6158   
O         | 66                   | 0.0097  | 33                   | 0.0049   
 */

--
-- Test 7: areas(s) with non unique join sequences
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 7);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_phase_I', 'Simplify % geography geolevel %: Phase I (Create the points table) took %s, total space used: %', 
		l_geography::VARCHAR			/* Geography */, 
		l_geolevel::VARCHAR			/* Geolevel */, 
		took::VARCHAR				/* Took */,
		pg_size_pretty(rel_size)::VARCHAR	/* Total size of table and indexes */);
--
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg._simplify_geometry_phase_I(VARCHAR, VARCHAR, VARCHAR, NUMERIC) IS 'Function: 	_simplify_geometry_phase_I()
Parameters:	Geography, geolevel, 
                geolevel; filter (for testing, no default), 
                minimum point resolution (default 1 - assumed metre, but depends on the geometry)
Returns:	Nothing
Description:	Simplify geography geolevel - Phase I: Create the points table

Phase I: Create the points table

Test 0 for valid geometry
Create a sequence so that points are in the original order specified by ST_DumpPoints()
Create table simplification_points_a and convert geometry to points, reduce precision to <l_min_point_resolution>m
from the relevent geolevel geometry table. Give each polygon a separate polygon number

Test 11: Check there are 1 or more distinct areas in simplification_points_a (i.e. filter is not broken)
Print out polygon/points reports [limited to first 20 rows]

a) Print points per first two dimensions (i.e. [1] and [2])
b) Polygons per area
c) Multiple polygons

Test 1 - check last_pointflow = one per area (should be zero)

Create table simplification_points_b from simplification_points_a to:

a) Round first and last points in area (were not rounded in the previous step); convert to points
b) Create the variables needed to detect duplcates within co-ordinates (a side effect of the round in the previous statement)

Note that a duplicate co-ordinate is defined as being within an area_id and coordinate pair; polygons are ignored 
as self intersections were removed by ST_Buffer(geom, 0.0) during shapefile geometry load into the geography geometry table

The geometry is not checked for validity at this time; hence test 0 above. This is to allow the initial load
to complete even if there are uncorrectable errors in the geometry.

Create table simplification_points_c to detect and classify duplcates within co-ordinates (a side effect of the 
round) from simplification_points_b:

O: original - first or last point in area
N: Not a dupicate
Y: A dupicate (two or more consecutive points sharing the same co-ordinate); removed later
L: Loop duplicate (two or more NON consecutive points sharing the same co-ordinate)

Print duplicate flags
Print numbers of multiple loop and origin flags per area. There will only be one origin; but a small number  of multiple loops. 
Display first two "L" (loop) duplicates and the three rows either side
Test 2.1 for invalid duplicate flag: "?"
Create table simplification_points_d to count up number of distinct polygon_number using the same
co-ordinate from simplification_points_c

List by number of distinct polygon_number using the same co-ordinate

num_polygon_number (join type if >1)
------------------ -----------------

1            Outer edge (no join)
2            Join between two polygons
3            Triple point - junction of three polygons
...
N            Nth point - junction of N polygons

Test 2 for un-detected duplicates

Join up adjacent edges by update of simplification_points_d
List joins by number of distinct polygon_number using the same co-ordinate
List of joinable co-ordinates by duplicate type
Create table simplification_points_e to categorise join types from simplification_points_d:

 Join code	Description						Increment join sequence
 ---------	-----------						-----------------------
 A		Start of polygon					YES
 B		Junction						YES
 C		Junction (previous area_id)				YES
 D		Duplicate and change in area_id				YES
 V		End of polygon						NO
 W		next area_id == current, but previous != current	YES
		(Normally there is a three cornered point and this does not occur as there is a NULL in the next/prev area_id)
 X		next == current						NO
 Y		Previous area_id == current				NO
 Z		ELSE							YES

Create the points table: simplification_points from simplification_points_e adding in joinable points

Note that a duplicate co-ordinate is defined as being within an area_id and coordinate pair; polygons are ignored 
as self intersections were removed by ST_Buffer(geom, 0.0) during shapefile geometry load into the geography geometry table

The geometry is not checked for validity at this time; hence test 0 above. This is to allow the initial load
to complete even if there are uncorrectable errors in the geometry.

Test 3 and 4 - Checks - MUST BE 0
	
a) Areas with no join sequence
b) Areas with mis-joined sequences

Create a list of joined_polygon_numbers as temporary table simplification_points_f (i.e. the num_join_seq is 1)

Test 5: For join sequence(s) used by more than one polygon_number
Test 6: For polygon_number, coord_id, duplicate(s) used by more than one join sequence

Update simplification_points.joined_join_seq using joined_polygon_numbers in simplification_points_f (i.e. the num_join_seq is 1) 
joining on joined_polygon_number, coord_id and the duplicate flag

Display number of polygons and duplicate flag
Display number of polygons joined at a point (1=edge; 2=shared polygon boundary between 2 polygons; 3+: triple or more point)
List duplicates
Test 7: areas(s) with non unique join sequences.

Example SQL>

CREATE TEMPORARY TABLE simplification_points_a
AS
WITH a AS (
        SELECT area_id,
               ST_DumpPoints(shapefile_geometry) AS geometry_data,     /* Convert geometry to points */
               nextval(''simplification_points_seq'') AS point_order_seq
          FROM p_rif40_geolevels_geometry_ew01_ladua2001
         WHERE geolevel_name = ''LADUA2001''
           AND area_id IN (SELECT DISTINCT ladua2001 FROM ew2001_geography WHERE gor2001 = ''K'') /* Restrict to The South West  */
), b /* Extract points and point order within a polygon */ AS (
        SELECT area_id, point_order_seq /* Point order in shapefile */,
               (geometry_data).geom AS points,                          /* Points component */
               (geometry_data).path AS path,                            /* Path component */
               CASE
                        WHEN (geometry_data).path[3] = 1 /* OR */       /* New polygon */ 
                /*           LAG((geometry_data).path[1]) OVER x < (geometry_data).path[1] OR
                             LAG((geometry_data).path[2]) OVER x < (geometry_data).path[2] */ THEN nextval(''simplification_points_poly_seq'')
                        ELSE                                            /* Current polygon */      currval(''simplification_points_poly_seq'')
               END AS polygon_number
          FROM a
        /* WINDOW x AS (ORDER BY point_order_seq) */
)
SELECT area_id, polygon_number, path, 
       ST_MakePoint(ROUND(CAST(st_X(points) AS numeric), 10),
               ROUND(CAST(st_Y(points) AS numeric), 10)) /* Reduce precision to <l_min_point_resolution> m */ AS coordinate, 
       ROW_NUMBER() OVER w AS pointflow, 
       COUNT(point_order_seq) OVER w AS last_pointflow,
       LAST_VALUE(points) OVER w AS lastpoint,                         /* Lastpoint should be the same as the first */
       FIRST_VALUE(points) OVER w AS firstpoint                        /* firstpoint should be the same as the first */
  FROM b
        WINDOW w AS (PARTITION BY polygon_number ORDER BY point_order_seq RANGE BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)
                    /* Disable window sliding (i.e. COUNT/LAST_VALUE works over the whole partition) */;

CREATE TEMPORARY TABLE simplification_points_b
AS
SELECT area_id, pointflow, polygon_number, last_pointflow,
       ST_MakePoint(ROUND(CAST(st_X(firstpoint) AS numeric), 10),
                ROUND(CAST(st_Y(firstpoint) AS numeric), 10)) AS firstpoint,    /* Convert firstpoint to POINT() datatype */
       ST_MakePoint(ROUND(CAST(st_X(lastpoint) AS numeric), 10),
                ROUND(CAST(st_Y(lastpoint) AS numeric), 10)) AS lastpoint,      /* Convert lastpoint to POINT() datatype */
       coordinate,
       COUNT(pointflow) OVER w AS tot_dup_coords,               /* Total duplicate co-ordinates within an area_id and coordinate pair */
       LAG(pointflow) OVER w AS prev_pointflow,                 /* Previous pointflow within a duplicate */
       LEAD(polygon_number) OVER w AS next_polygon_number,      /* Next polygon_number */
       LAG(polygon_number) OVER w AS prev_polygon_number,       /* Previous polygon_number */
       ROW_NUMBER() OVER w AS dup_coord_order                   /* Find pointflows with the same coordinate [i.e. side effect of the ROUND()] */
  FROM simplification_points_a
WINDOW w AS (PARTITION BY polygon_number, coordinate ORDER BY pointflow)
 ORDER BY coordinate, polygon_number /* Optimise index: simplification_points_b_coordinate; not the primary key */;

CREATE TEMPORARY TABLE simplification_points_c
AS
WITH a /* First point in polygon_number */ AS (
        SELECT polygon_number, coordinate AS firstpoint, pointflow AS first_pointflow
          FROM simplification_points_b
         WHERE pointflow = 1 AND coordinate = firstpoint
), b /* Last point in polygon */ AS (
        SELECT polygon_number, coordinate AS lastpoint, pointflow AS last_pointflow
          FROM simplification_points_b
         WHERE last_pointflow = pointflow AND coordinate = lastpoint
), c /* Total polygons */ AS (
        SELECT DISTINCT polygon_number
          FROM simplification_points_b
), d AS (
        SELECT c.polygon_number,
               CASE WHEN a.firstpoint = b.lastpoint THEN 1 ELSE 0 END origin_verified
          FROM c
                LEFT OUTER JOIN a ON (c.polygon_number = a.polygon_number)
                LEFT OUTER JOIN b ON (c.polygon_number = b.polygon_number)
)
SELECT area_id, e.polygon_number, prev_polygon_number, pointflow, last_pointflow, tot_dup_coords, dup_coord_order, prev_pointflow, origin_verified,
      DENSE_RANK() OVER (ORDER BY coordinate) AS coord_id,              /* Add coordinate index number per geolevel */
      coordinate,
      CASE                                                              /* Is co-ordinate a duplicate within a polygon? */
                WHEN pointflow = 1 AND coordinate = firstpoint  THEN                                       ''O'' /* Origin */
                WHEN last_pointflow = pointflow AND coordinate = lastpoint AND d.origin_verified = 1 THEN  ''E'' /* End (same as Origin) */ 
                WHEN tot_dup_coords = 1 OR (tot_dup_coords > 1  AND dup_coord_order = 1) THEN              ''N'' /* No */
                WHEN tot_dup_coords > 1 AND dup_coord_order > 1 AND
                        prev_pointflow + 1 = pointflow THEN                                                ''Y'' /* Yes */
                WHEN tot_dup_coords > 1 AND dup_coord_order > 1 AND 
                        prev_pointflow IS NOT NULL AND prev_pointflow + 1 != pointflow THEN                ''L'' /* Loop */
                ELSE                                                                                       ''?'' /* Unknown */
      END duplicate                                                     /* Flag duplicates */
  FROM simplification_points_b e, d
 WHERE e.polygon_number = d.polygon_number
 ORDER BY coord_id, polygon_number /* Sort order in next statement */;

CREATE TEMPORARY TABLE simplification_points_d
AS
WITH f AS (
        SELECT coord_id,
               COUNT(DISTINCT(polygon_number)) AS num_polygon_number    /* Count up number of distinct polygons using the same co-ordinate */
         FROM simplification_points_c
         GROUP BY coord_id
) 
SELECT area_id, polygon_number, pointflow, e.coord_id, coordinate, tot_dup_coords, dup_coord_order, prev_pointflow, duplicate,
       f.num_polygon_number
  FROM  simplification_points_c e
                LEFT OUTER JOIN f ON (e.coord_id = f.coord_id)
 ORDER BY polygon_number, pointflow;

UPDATE simplification_points_d g1
   SET joined_polygon_number = (
        SELECT DISTINCT g2.polygon_number AS joined_polygon_number
          FROM simplification_points_d g2
         WHERE g1.num_polygon_number = g2.num_polygon_number                    /* Join type matches */
            AND g1.coord_id           = g2.coord_id                             /* Co-ordinates are the same */
            AND g1.polygon_number    != g2.polygon_number                       /* Polygons are NOT the same */
            AND (
                 (g1.duplicate        = g2.duplicate) OR                        /* Duplicate flags match */
                 (g1.duplicate        = ''N'' AND g2.duplicate = ''O'') OR      /* Handle different origins */
                 (g1.duplicate        = ''O'' AND g2.duplicate = ''N'')
               )
        )
 WHERE g1.num_polygon_number = 2        /* Join points shared between two polygons only - i.e. shares linestrings */
   AND g1.duplicate IN (''N'' /* Not a duplicate */, ''O'' /* Origin */, ''L'' /* Loop */);

CREATE TEMPORARY TABLE simplification_points_e
AS
WITH a AS (
        SELECT area_id, polygon_number, pointflow, coord_id, coordinate, duplicate, num_polygon_number, joined_polygon_number,
               LAG(polygon_number) OVER w               AS prev_polygon_number,
               LAG(num_polygon_number) OVER w           AS prev_num_polygon_number,
               LAG(pointflow) OVER w                    AS prev_pointflow,
               LAG(joined_polygon_number) OVER w        AS prev_joined_polygon_number,
               LEAD(pointflow) OVER w                   AS next_pointflow,
               LEAD(joined_polygon_number) OVER w       AS next_joined_polygon_number,
               LEAD(polygon_number) OVER w              AS next_polygon_number,
               LEAD(num_polygon_number) OVER w          AS next_num_polygon_number
          FROM simplification_points_d
         WHERE duplicate NOT IN(''E'', ''Y'')               /* Exclude end point and duplicates */
        WINDOW w AS (PARTITION BY polygon_number ORDER BY pointflow)
)
SELECT area_id, polygon_number::bigint, pointflow::bigint, duplicate, coord_id::bigint, coordinate, num_polygon_number::bigint, joined_polygon_number::bigint,
       next_polygon_number::bigint, next_pointflow::bigint, next_joined_polygon_number::bigint,
       prev_polygon_number::bigint, prev_pointflow::bigint, prev_joined_polygon_number::bigint,
       CASE
                WHEN pointflow = 1                                              THEN ''A'' /* nextval: start */
                WHEN num_polygon_number > 2                                     THEN ''B'' /* nextval: junction */
                WHEN prev_num_polygon_number > 2                                THEN ''C'' /* nextval: junction */
                WHEN duplicate = ''L'' AND next_joined_polygon_number IS NULL   THEN ''D'' /* nextval: duplicate and change in polygon_number */
                WHEN num_polygon_number = 1 AND next_num_polygon_number = 1     THEN ''E'' /* currval: outside edge */
                WHEN pointflow != 1 AND duplicate = ''O''                       THEN ''V'' /* currval: end */
                WHEN polygon_number  = next_polygon_number AND joined_polygon_number = next_joined_polygon_number
                        AND joined_polygon_number != prev_joined_polygon_number THEN ''W'' /* nextval: next == current, but previous != current */
                        /* Normally there is a three cornered point and this does not occur as there is a NULL in the next/prev polygon_number */
                WHEN polygon_number  = next_polygon_number AND
                        joined_polygon_number = next_joined_polygon_number      THEN ''X'' /* currval: next == current */
                WHEN polygon_number  = prev_polygon_number AND
                        joined_polygon_number = prev_joined_polygon_number      THEN ''Y'' /* currval: previous == current */
                ELSE ''Z'' /* nextval */
       END join_code,
       CASE
                WHEN pointflow = 1                                              THEN nextval(''simplification_points_seq'') /* nextval: start */
                WHEN num_polygon_number > 2                                     THEN nextval(''simplification_points_seq'') /* nextval: junction */
                WHEN prev_num_polygon_number > 2                                THEN nextval(''simplification_points_seq'') /* nextval: junction */
                WHEN duplicate = ''L'' AND next_joined_polygon_number IS NULL   THEN nextval(''simplification_points_seq'') /* nextval: duplicate and change in polygon_number */
                WHEN num_polygon_number = 1 AND next_num_polygon_number = 1     THEN currval(''simplification_points_seq'') /* currval: outside edge */
                WHEN pointflow != 1 AND duplicate = ''O''                       THEN currval(''simplification_points_seq'') /* currval: end */
                WHEN polygon_number  = next_polygon_number AND joined_polygon_number = next_joined_polygon_number
                        AND joined_polygon_number != prev_joined_polygon_number THEN nextval(''simplification_points_seq'') /* nextval: next == current, but previous != current */
                        /* Normally there is a three cornered point and this does not occur as there is a NULL in the next/prev polygon_number */
                WHEN polygon_number  = next_polygon_number AND
                        joined_polygon_number = next_joined_polygon_number      THEN currval(''simplification_points_seq'') /* currval: next == current */
                WHEN polygon_number  = prev_polygon_number AND
                        joined_polygon_number = prev_joined_polygon_number      THEN currval(''simplification_points_seq'') /* currval: previous == current */
                ELSE nextval(''simplification_points_seq'')
       END join_seq
  FROM a
 ORDER BY polygon_number, pointflow;

CREATE TABLE simplification_points
AS
WITH b AS /* Joinable polygons */ (
        SELECT joined_polygon_number, coord_id, duplicate, COUNT(DISTINCT(join_seq)) AS num_join_seq
          FROM simplification_points_e c
         WHERE num_polygon_number = 2
           AND joined_polygon_number IS NOT NULL
         GROUP BY joined_polygon_number, coord_id, duplicate
        HAVING COUNT(DISTINCT(join_seq)) = 1
)
SELECT c.area_id, polygon_number, pointflow, c.coord_id, coordinate, c.duplicate,
       c.num_polygon_number, c.joined_polygon_number,
       next_polygon_number, next_pointflow, next_joined_polygon_number,
       prev_polygon_number, prev_pointflow, prev_joined_polygon_number,
       join_seq, join_code, b.num_join_seq
  FROM simplification_points_e c
        LEFT OUTER JOIN b /* For joined_polygon_number */ ON (
               b.joined_polygon_number = c.polygon_number
           AND b.coord_id              = c.coord_id
           AND (
                (b.duplicate           = c.duplicate) /* Same flag */ OR
                (b.duplicate           = ''N'' AND c.duplicate = ''O'') OR /* Handle different origins */
                (b.duplicate           = ''O'' AND c.duplicate = ''N'')
               )
           AND c.num_polygon_number    = 2 /* Shared boundary between two polygons only */)
 ORDER BY polygon_number, pointflow;

CREATE TEMPORARY TABLE simplification_points_f
AS
WITH a AS (
        SELECT polygon_number AS joined_polygon_number, coord_id, duplicate, join_seq, COUNT(join_seq) AS total_join_seq
          FROM simplification_points
         WHERE num_join_seq  = 1
         GROUP BY polygon_number, coord_id, duplicate, join_seq
), b /* Exclude a small number of loop duplicates that would break the primary key */ AS (
        SELECT joined_polygon_number, coord_id, duplicate, COUNT(join_seq) AS total
          FROM a
         GROUP BY joined_polygon_number, coord_id, duplicate
        HAVING COUNT(join_seq) > 1
)
SELECT a.joined_polygon_number, a.coord_id, a.duplicate, a.join_seq, a.total_join_seq
  FROM a
        LEFT OUTER JOIN b ON (a.joined_polygon_number = b.joined_polygon_number AND a.coord_id = b.coord_id AND a.duplicate = b.duplicate)
 WHERE b.total IS NULL
 ORDER BY a.joined_polygon_number, a.coord_id, a.duplicate, a.join_seq;

UPDATE simplification_points a
   SET joined_join_seq = (
        SELECT join_seq
          FROM simplification_points_f b1
         WHERE a.joined_polygon_number = b1.joined_polygon_number
           AND a.coord_id       = b1.coord_id
           AND a.duplicate      = b1.duplicate);';

--
-- Eof