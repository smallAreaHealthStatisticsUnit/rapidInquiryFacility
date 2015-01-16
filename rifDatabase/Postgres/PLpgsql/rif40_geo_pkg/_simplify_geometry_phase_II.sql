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
--								  Phase II: Create the lines table
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
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_II(VARCHAR, VARCHAR, VARCHAR, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_II(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC);

CREATE OR REPLACE FUNCTION rif40_geo_pkg._simplify_geometry_phase_II(
	l_geography VARCHAR, l_geolevel VARCHAR, l_filter VARCHAR DEFAULT NULL, 
	l_min_point_resolution NUMERIC DEFAULT 1 /* metre */)
RETURNS void 
SECURITY INVOKER
AS $body$
DECLARE
/*
Function: 	_simplify_geometry_phase_II()
Parameters:	Geography, geolevel, 
            geolevel; filter (for testing, no default), 
            minimum point resolution (default 1 - assumed metre, but depends on the geometry)
Returns:	Nothing
Description:	Simplify geography geolevel - Phase II: Create the lines table

Phase II: Create the lines table

Create and populate simplification_lines table. Processed in two sections:

a) Good joins - where there is a 1:1 mapping between join_seq, polygon_number and joined_join_seq
b) Add in the duplicate joins - where there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq

Display the duplicate joins to be removed (duplicate_join_to_be_removed=Y). 

Note:

a) Duplicate joins to be removed are all one or two polygon joins
b) OK joins are either 1, 2 or 3 polygons joins (1 is an outside edge) and MIN == MAX

Report on join duplicates. This is the polygon_number/join_seq/joined_join_seq where there is not a 1:1 mapping between join_seq, 
polygon_number and joined_join_seq cannot used shared simplification and are therefore a potential source of slivers

The table simplification_lines_join_duplicates table allows the join duplicates to be explored offline within the same session

Report on 1:1 mapping between join_seq and joined_join_seq test

Flag lines for reversal. Where shared simplification is in use the lines may need to be reversed if:

a) Start point line A == end point line b
b) End point line A == start point line b
c) duplicate_join_to_be_removed = N is both
d) Number of poibnts in both lines are the same
e) If line A join sequence is bigger line A stays the same
   If line B join sequence is bigger line B is reversed
f) Line of the line (as an INT) is the same [originally assumed metre type units; has been changed to a % test]

Carry out line reversal

Display joins and reversals

The duplicate_join_to_be_removed flag is where there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq
duplicate_join_to_be_removed applies to both side of a joined line so:

  N/Y means the line cannot be reversed or replaced as the duplicate status varies
  Y/ duplicates, but only a single point  
  N/ no duplicates, but only a single point
  N/N No duplicates; 1, 2 or 3 polygon join; Can reverse:Y, stays same: S or N [default no matching criteria; should not be any]

Test 9: join sequence(s) causing invalid joins and reversals
Update spatial linestrings table, e.g. t_rif40_linestrings_ew01_ward2001 if there is no filter [NOT CURRENTLY IMPLEMENTED]

 */
	c1_sII	CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c2_sII CURSOR(l_geography VARCHAR, l_geolevel_name VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		   AND geolevel_name = l_geolevel_name;
	c3_s11 CURSOR(l_geography VARCHAR) FOR
		WITH b AS (
			SELECT geography, srid, rif40_geo_pkg.rif40_zoom_levels(	
				ST_Y( 														/* Get latitude */
						ST_GeomFromEWKT('SRID='||a.srid||';POINT(0 0)') 	/* Grid Origin */
					)::NUMERIC) AS zl
			  FROM rif40_geographies a
			 WHERE a.geography = l_geography		  
		), c6 AS (
		SELECT (zl).simplify_tolerance AS st_simplify_tolerance_zoomlevel_6
		  FROM b
		 WHERE (zl).zoom_level = 6 /* RIF zoomlevels */
		), c8 AS (
		SELECT (zl).simplify_tolerance AS st_simplify_tolerance_zoomlevel_8
		  FROM b
		 WHERE (zl).zoom_level = 8 /* RIF zoomlevels */
		), c11 AS (
		SELECT (zl).simplify_tolerance AS st_simplify_tolerance_zoomlevel_11
		  FROM b
		 WHERE (zl).zoom_level = 11 /* RIF zoomlevels */
		)
		SELECT c6.st_simplify_tolerance_zoomlevel_6,
		       c8.st_simplify_tolerance_zoomlevel_8,
			   c11.st_simplify_tolerance_zoomlevel_11
		  FROM c6, c8, c11;
--
	c1_rec rif40_geographies%ROWTYPE;
	c2_rec t_rif40_geolevels%ROWTYPE;
	c3_rec RECORD;
--
	sql_stmt	VARCHAR[];
	l_sql_stmt	VARCHAR;
--
	l_spatial_linestrings_table	VARCHAR;
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
		PERFORM rif40_log_pkg.rif40_error(-10001, '_simplify_geometry_phase_II', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	stp:=clock_timestamp();
--
	OPEN c1_sII(l_geography);
	FETCH c1_sII INTO c1_rec;
	CLOSE c1_sII;
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10091, '_simplify_geometry_phase_II', 'No geography specified');
	END IF;
	OPEN c2_sII(l_geography, l_geolevel);
	FETCH c2_sII INTO c2_rec;
	CLOSE c2_sII;
	IF c2_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10092, '_simplify_geometry_phase_II', 'No geolevel name specified for geography: %',
			l_geography::VARCHAR);
	ELSIF c1_rec.srid IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10094, '_simplify_geometry_phase_II', 'No srid specified for geography: % geolevel: %',
			l_geography::VARCHAR);
	END IF;
--
-- Now get lat/log of grid origin for geography and then call rif40_geo_pkg.rif40_zoom_levels()
-- to get st_simplify_tolerance for zoom levels 6, 8, and 11
--
	OPEN c3_s11(l_geography);
	FETCH c3_s11 INTO c3_rec;
	CLOSE c3_s11;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_phase_II', 'Phase II (lines creation) for geography: % geolevel: %. Parameters - l_min_point_resolution: %; units: %',
		l_geography::VARCHAR,
		l_geolevel::VARCHAR,
		l_min_point_resolution::VARCHAR,
		rif40_geo_pkg.get_srid_projection_parameters(l_geography, '+units')::VARCHAR);
--
-- First block of SQL statements
--
-- Create lines table
--
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_lines';
	IF l_filter IS NULL THEN /* Non test mode */
		l_sql_stmt:='CREATE TEMPORARY TABLE simplification_lines('||E'\n';
	ELSE
		l_sql_stmt:='CREATE TABLE simplification_lines('||E'\n';
	END IF;
	sql_stmt[array_length(sql_stmt, 1)+1]:=l_sql_stmt||
' geolevel_name			VARCHAR(30),'||E'\n'||
' polygon_number    	   	bigint,'||E'\n'||   
' area_id      	  		VARCHAR(300),'||E'\n'||
' join_seq       		bigint,'||E'\n'||   
' joined_join_seq       	bigint,'||E'\n'||   
' line_length	 		NUMERIC,'||E'\n'||  
' simplified_line_length 	NUMERIC,'||E'\n'||  
' min_num_polygon_number    	bigint,'||E'\n'||  
' max_num_polygon_number    	bigint,'||E'\n'||  
' min_pointflow  		bigint,'||E'\n'||
' max_pointflow  		bigint,'||E'\n'||
' num_points     		bigint,'||E'\n'||
' duplicate_join_to_be_removed 	VARCHAR(1))';

--
-- Execute first block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add geometry
--
	PERFORM AddGeometryColumn('simplification_lines', 'line', c1_rec.srid, 'LINESTRING', 2);
    PERFORM AddGeometryColumn('simplification_lines', 
		'simplified_line', c1_rec.srid, 'LINESTRING', 2) /* zoomlevel 6 */;
    PERFORM AddGeometryColumn('simplification_lines', 
		'simplified_line_2', c1_rec.srid, 'LINESTRING', 2) /* zoomlevel 8 */;
    PERFORM AddGeometryColumn('simplification_lines', 
		'simplified_line_3', c1_rec.srid, 'LINESTRING', 2) /* zoomlevel 11 */;
--
-- Second block of SQL statements
--
	sql_stmt:=NULL;	/* Empty statement array */
--
-- Populate simplification_lines table. Processed in two sections:
--
-- a) Good joins - where there is a 1:1 mapping between join_seq, polygon_number and joined_join_seq
-- b) Add in the duplicate joins - where there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq
--
-- ST_Makeline() is assumed to be valid at the moment, attempting ST_MakeValid() causes: 
-- Geometry type (Point) does not match column type (LineString)
--
-- Note that the SQL has not been split and indexed as the process is linear apart from the 1:1 mapping check and does not consume huge amounts of sort
-- Could be improved
--
	sql_stmt[1]:='EXPLAIN ANALYZE VERBOSE INSERT INTO simplification_lines'||E'\n'|| 
'      (geolevel_name, area_id, polygon_number, join_seq, joined_join_seq, line_length, simplified_line_length,'||E'\n'||  
'	    min_num_polygon_number, max_num_polygon_number, min_pointflow, max_pointflow, num_points, line,'||E'\n'|| 
'       simplified_line, simplified_line_2, simplified_line_3, duplicate_join_to_be_removed)'||E'\n'|| 
'WITH a AS /* Convert co-ordinate points to LINESTRINGs */ ('||E'\n'|| 
'	SELECT area_id, polygon_number, join_seq, joined_join_seq,'||E'\n'||  
'	       MIN(num_polygon_number) AS min_num_polygon_number,'||E'\n'|| 
'	       MAX(num_polygon_number) AS max_num_polygon_number,'||E'\n'|| 
'	       MIN(pointflow) AS min_pointflow,'||E'\n'|| 
'	       MAX(pointflow) AS max_pointflow,'||E'\n'|| 
'	       COUNT(pointflow) AS num_points,'||E'\n'|| 
'	       ST_RemoveRepeatedPoints( 			/* Remove repeated points */'||E'\n'|| 
'	         ST_Makeline(array_agg(				/* Convert to line */'||E'\n'|| 
'	           ST_SetSRID(coordinate, '||c1_rec.srid||'	/* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))) AS line,'||E'\n'|| 
'	       ST_SimplifyPreserveTopology(			/* Run Douglas-Peuker on line to simplify */'||E'\n'|| 
'	         ST_RemoveRepeatedPoints(  			/* Remove repeated points */'||E'\n'|| 
'	           ST_Makeline(array_agg(			/* Convert to line */'||E'\n'|| 
'	             ST_SetSRID(coordinate, '||c1_rec.srid||'	/* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))), '||E'\n'|| 
'	           '||c3_rec.st_simplify_tolerance_zoomlevel_6||'			/* Simplify tolerance */) AS simplified_line,'||E'\n'|| 
'	       ST_SimplifyPreserveTopology(			/* Run Douglas-Peuker on line to simplify zoomlevel 6 */'||E'\n'|| 
'	         ST_RemoveRepeatedPoints(  			/* Remove repeated points */'||E'\n'|| 
'	           ST_Makeline(array_agg(			/* Convert to line */'||E'\n'|| 
'	             ST_SetSRID(coordinate, '||c1_rec.srid||'	/* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))), '||E'\n'|| 
'	           '||c3_rec.st_simplify_tolerance_zoomlevel_8||'			/* Simplify tolerance */) AS simplified_line_2,'||E'\n'|| 
'	       ST_SimplifyPreserveTopology(			/* Run Douglas-Peuker on line to simplify zoomlevel 8 */'||E'\n'|| 
'	         ST_RemoveRepeatedPoints(  			/* Remove repeated points */'||E'\n'|| 
'	           ST_Makeline(array_agg(			/* Convert to line */'||E'\n'|| 
'	             ST_SetSRID(coordinate, '||c1_rec.srid||'	/* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))), '||E'\n'|| 
'	           '||c3_rec.st_simplify_tolerance_zoomlevel_11||'			/* Simplify tolerance zoomlevel 11 */) AS simplified_line_3'||E'\n'|| 
'	  FROM simplification_points'||E'\n'|| 
'	 GROUP BY area_id, polygon_number, join_seq, joined_join_seq'||E'\n'|| 
'), b AS /* 1:1 mapping between join_seq, polygon_number and joined_join_seq - can use shared implification */ ('||E'\n'|| 
'	SELECT polygon_number, join_seq, COUNT(polygon_number) AS total'||E'\n'|| 
'	  FROM a'||E'\n'|| 
'	 GROUP BY polygon_number, join_seq'||E'\n'|| 
'	 HAVING COUNT(polygon_number) = 1'||E'\n'|| 	
'), c AS /* No 1:1 mapping between join_seq, polygon_number and joined_join_seq */ ('||E'\n'|| 
'	SELECT DISTINCT join_seq'||E'\n'||  
'	  FROM ('||E'\n'|| 
'		SELECT polygon_number, join_seq, COUNT(polygon_number) AS total'||E'\n'|| 
'		  FROM a'||E'\n'|| 
'		 GROUP BY polygon_number, join_seq'||E'\n'|| 
'		 HAVING COUNT(polygon_number) > 1'||E'\n'|| 
'	) c1'||E'\n'|| 
'), d AS /* Simplify separately - as there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq */ ('||E'\n'|| 
'	SELECT d1.area_id, d1.polygon_number, d1.join_seq, NULL::bigint joined_join_seq,'||E'\n'||  
'	       MIN(num_polygon_number) AS min_num_polygon_number,'||E'\n'|| 
'	       MAX(num_polygon_number) AS max_num_polygon_number,'||E'\n'|| 
'	       MIN(pointflow) AS min_pointflow,'||E'\n'|| 
'	       MAX(pointflow) AS max_pointflow,'||E'\n'|| 
'	       COUNT(pointflow) AS num_points,'||E'\n'|| 
'	       ST_RemoveRepeatedPoints(				/* Remove repeated points */'||E'\n'|| 
'	         ST_Makeline(array_agg(				/* Convert to line */'||E'\n'|| 
'	           ST_SetSRID(coordinate, '||c1_rec.srid||'	/* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))) AS line,'||E'\n'|| 
'	       ST_SimplifyPreserveTopology(			/* Run Douglas-Peuker on line to simplify */'||E'\n'|| 
'	         ST_RemoveRepeatedPoints( 			/* Remove repeated points */'||E'\n'|| 
'	           ST_Makeline(array_agg(			/* Convert to line */'||E'\n'|| 
'	             ST_SetSRID(coordinate, '||c1_rec.srid||'	/* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))), '||
'                  '||c3_rec.st_simplify_tolerance_zoomlevel_6||'	/* Simplify tolerance */) AS simplified_line,'||E'\n'|| 
'	       ST_SimplifyPreserveTopology(			/* Run Douglas-Peuker on line to simplify */'||E'\n'|| 
'	         ST_RemoveRepeatedPoints( 			/* Remove repeated points */'||E'\n'|| 
'	           ST_Makeline(array_agg(			/* Convert to line */'||E'\n'|| 
'	             ST_SetSRID(coordinate, '||c1_rec.srid||'	/* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))), '||
'                  '||c3_rec.st_simplify_tolerance_zoomlevel_8||'	/* Simplify tolerance */) AS simplified_line_2,'||E'\n'|| 
'	       ST_SimplifyPreserveTopology(			/* Run Douglas-Peuker on line to simplify */'||E'\n'|| 
'	         ST_RemoveRepeatedPoints( 			/* Remove repeated points */'||E'\n'|| 
'	           ST_Makeline(array_agg(			/* Convert to line */'||E'\n'|| 
'	             ST_SetSRID(coordinate, '||c1_rec.srid||'	/* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))), '||
'                  '||c3_rec.st_simplify_tolerance_zoomlevel_11||'	/* Simplify tolerance */) AS simplified_line_3'||E'\n'|| 
'	  FROM simplification_points d1, c'||E'\n'|| 
'	 WHERE d1.join_seq = c.join_seq'||E'\n'|| 
'	 GROUP BY d1.area_id, d1.polygon_number, d1.join_seq'||E'\n'|| 
')'||E'\n'|| 
'SELECT /* Good joins - where there is a 1:1 mapping between join_seq, polygon_number and joined_join_seq */ '''||c2_rec.geolevel_name||''' AS geolevel_name,'||E'\n'||  
'       area_id, a.polygon_number, a.join_seq, joined_join_seq,'||E'\n'||  
'       ROUND(CAST(ST_Length(line) AS numeric), 0) AS line_length,'||E'\n'||  
'       ROUND(CAST(ST_Length(simplified_line) AS numeric), 0) AS simplified_line_length, '||E'\n'|| 
'       min_num_polygon_number, max_num_polygon_number, min_pointflow, max_pointflow, num_points, line,'||E'\n'|| 
'       simplified_line, simplified_line_2, simplified_line_3, ''N'' AS duplicate_join_to_be_removed'||E'\n'|| 
'  FROM a, b'||E'\n'|| 
' WHERE a.polygon_number  = b.polygon_number'||E'\n'|| 
'   AND a.join_seq        = b.join_seq'||E'\n'|| 
'UNION'||E'\n'|| 
'SELECT /* Add in the duplicate joins - where there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq */ '''||c2_rec.geolevel_name||''' AS geolevel_name,'||E'\n'||  
'       area_id, polygon_number, join_seq, joined_join_seq /* This is NULL */,'||E'\n'||  
'       ROUND(CAST(ST_Length(line) AS numeric), 0) AS line_length,'||E'\n'||  
'       ROUND(CAST(ST_Length(simplified_line) AS numeric), 0) AS simplified_line_length,'||E'\n'||  
'       min_num_polygon_number, max_num_polygon_number, min_pointflow, max_pointflow, num_points, line,'||E'\n'||  
'       simplified_line, simplified_line_2, simplified_line_3,'||E'\n'||  
'       CASE'||E'\n'||  
'          	WHEN max_num_polygon_number > 2  THEN ''M'' /* Mutli (>2) polygon join, e.g. triple point between 3 polygons */'||E'\n'||  
'          	WHEN max_num_polygon_number = 2  THEN ''Y'' /* Two polygon join */'||E'\n'||  
'          	WHEN max_num_polygon_number = 1  THEN ''E'' /* Outer edge */'||E'\n'||  
E'          	ELSE                                  ''?'' /* Unknown */'||E'\n'||  
'       END AS duplicate_join_to_be_removed'||E'\n'|| 
'  FROM d'||E'\n'|| 
' ORDER BY 1, 2, 3, 4';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_lines ADD PRIMARY KEY(polygon_number, join_seq)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE UNIQUE INDEX simplification_lines_uk ON simplification_lines(join_seq)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_lines';
--
-- Report on invalid LINESTRINGs [Removed - separate test at the polygon level - ST_MakeValid(linestrings) causes errors]
--
/*
SELECT COUNT(join_seq) AS total_single_point_lines, SUM(ST_Length(line)) AS total_line_length
  FROM simplification_lines
 WHERE ST_NPoints(line) = 1;

SELECT ST_IsValid(line) AS line_ok, ST_IsValid(simplified_line) AS simplified_line, COUNT(join_seq) AS total_lines,
       SUM(ST_Length(line)) AS total_line_length, SUM(ST_Length(simplified_line)) AS total_simplified_line_line_length,
       SUM(ST_NPoints(line)) AS total_line_points, SUM(ST_NPoints(simplified_line)) AS total_simplified_line_points
  FROM simplification_lines
 WHERE ST_NPoints(line) > 1 
 GROUP BY ST_IsValid(line), ST_IsValid(simplified_line);
 
SELECT ST_IsValidReason(line), join_seq, ST_Dump(line), ST_NPoints(line)
  FROM simplification_lines 
 WHERE ST_NPoints(line) > 1 AND NOT ST_IsValid(line)
 LIMIT 10; 
 */
--
-- Execute second block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Display the duplicate joins to be removed (duplicate_join_to_be_removed=Y). 
--
-- Note:
--
-- a) Duplicate joins to be removed are all one or two polygon joins
-- b) OK joins are either 1, 2 or 3 polygons joins (1 is an outside edge) and MIN == MAX
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH b AS ('||E'\n'||
'	SELECT COUNT(DISTINCT(join_seq)) AS total, SUM(num_points) AS total_points, SUM(simplified_line_length) AS total_simplified_line_length'||E'\n'||
'	  FROM simplification_lines'||E'\n'||
')'||E'\n'||
'SELECT duplicate_join_to_be_removed, min_num_polygon_number, max_num_polygon_number, '||E'\n'|| 
'       COUNT(DISTINCT(join_seq)) AS t_join_seq, ROUND((CAST(COUNT(DISTINCT(join_seq)) AS numeric)/b.total)*100.0, 4) AS pct_join_seq,'||E'\n'||
'       SUM(num_points) AS points,'||E'\n'||
'       CASE'||E'\n'|| 
'       	WHEN b.total_points > 0 THEN'||E'\n'|| 
'       		ROUND((CAST( SUM(num_points) AS numeric)/b.total_points)*100.0, 4)'||E'\n'||
'		ELSE 0 END AS pct_points,'||E'\n'||
'       SUM(simplified_line_length) AS t_simp_line_length,'||E'\n'|| 
'       CASE'||E'\n'|| 
'       	WHEN b.total_simplified_line_length > 0 THEN'||E'\n'|| 
'       		ROUND((CAST( SUM(simplified_line_length) AS numeric)/b.total_simplified_line_length)*100.0, 4)'||E'\n'||
'		ELSE 0 END AS pct_simp_line_length,'||E'\n'||
'       COUNT(DISTINCT(polygon_number)) AS t_polygon_number'||E'\n'||
'  FROM simplification_lines a, b'||E'\n'||
' GROUP BY duplicate_join_to_be_removed, min_num_polygon_number, max_num_polygon_number, total, total_points, total_simplified_line_length', 
	'Display the duplicate joins to be removed (duplicate_join_to_be_removed=Y)');

/*
duplicate_join_to_be_removed | min_num_polygon_number | max_num_polygon_number | t_join_seq           | pct_join_seq | points | pct_points | t_simp_line_length | pct_simp_line_length | t_polygon_number    
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
N                            | 1                      | 1                      | 63                   | 14.2212      | 82158  | 12.0822    | 502980             | 7.1737               | 33                  
N                            | 2                      | 2                      | 120                  | 27.0880      | 219028 | 32.2103    | 2590187            | 36.9425              | 38                  
N                            | 3                      | 3                      | 157                  | 35.4402      | 157    | 0.0231     | 0                  | 0.0000               | 40                  
Y                            | 1                      | 2                      | 62                   | 13.9955      | 266455 | 39.1850    | 2680832            | 38.2353              | 31                  
Y                            | 2                      | 2                      | 41                   | 9.2551       | 112195 | 16.4994    | 1237402            | 17.6484              | 31                  
(5 rows)

 */
--
-- Report on join duplicates. This is the polygon_number/join_seq/joined_join_seq where there is not a 1:1 mapping between join_seq, 
-- polygon_number and joined_join_seq cannot used shared simplification and are therefore a potential source of slivers
--
-- Third block of SQL statements
--
-- The table simplification_lines_join_duplicates table allows the join duplicates to be explored offline within the same session
--
/*
XXXX
PH Notes
It is strongly suspected that these are mainly caused by the joined_join_seq=0, line_length=0 pairs
 
SELECT COUNT(polygon_number)
  FROM simplification_lines_join_duplicates;

 count 
-------
   281
(1 row)

SELECT total, COUNT(total) 
  FROM (
	SELECT polygon_number, join_seq, COUNT(joined_join_seq) AS total
	  FROM simplification_lines_join_duplicates
	 GROUP BY polygon_number, join_seq) t
 GROUP BY total;

 total | count 
-------+-------
     3 |     7
     2 |   130
(2 rows)

WITH a AS (
	SELECT polygon_number, join_seq, COUNT(joined_join_seq) AS total
	  FROM simplification_lines_join_duplicates
	 GROUP BY polygon_number, join_seq
	 HAVING COUNT(joined_join_seq) = 2
)
SELECT polygon_number, join_seq, COUNT(joined_join_seq) AS total, MIN(joined_join_seq) AS min, MAX(joined_join_seq) AS max
  FROM simplification_lines_join_duplicates
 GROUP BY polygon_number, join_seq LIMIT 20;

 polygon_number | join_seq | total | min | max  
----------------+----------+-------+-----+------
             26 |      298 |     2 |   0 |  400
             31 |      362 |     2 |   0 |  421
             34 |      400 |     3 |   0 |  312
             35 |      411 |     2 |   0 |  496
             38 |      443 |     2 |   0 | 1998
             38 |      449 |     2 |   0 |  497
             41 |      478 |     2 |   0 |  494
             42 |      494 |     3 |   0 |  490
             42 |      496 |     2 |   0 |  411
             42 |      497 |     2 |   0 |  449
             54 |      626 |     2 |   0 |  725
             54 |      627 |     2 |   0 |  745
             56 |      641 |     2 |   0 |  746
             56 |      642 |     2 |   0 | 1090
             63 |      724 |     2 |   0 | 3391
             63 |      725 |     2 |   0 |  626
             65 |      745 |     2 |   0 |  627
             65 |      746 |     2 |   0 |  641
             87 |     1006 |     2 |   0 | 1129
             87 |     1007 |     2 |   0 | 1545
(20 rows)

WITH a AS (
	SELECT polygon_number, join_seq, COUNT(joined_join_seq) AS total
	  FROM simplification_lines_join_duplicates
	 GROUP BY polygon_number, join_seq
	 HAVING COUNT(joined_join_seq) = 3
)
SELECT a.polygon_number, a.join_seq, joined_join_seq, line_length, min_pointflow, max_pointflow, num_points
  FROM a, simplification_lines_join_duplicates b
 WHERE a.polygon_number = b.polygon_number
   AND a.join_seq       = b.join_seq ORDER BY 1, 2, 3 LIMIT 20;

 polygon_number | join_seq | joined_join_seq | line_length | min_pointflow | max_pointflow | num_points 
----------------+----------+-----------------+-------------+---------------+---------------+------------
             34 |      400 |               0 |           0 |           751 |           751 |          1
             34 |      400 |             298 |        2675 |           267 |           750 |        477
             34 |      400 |             312 |         606 |           752 |           792 |         41
             42 |      494 |               0 |           0 |           840 |           840 |          1
             42 |      494 |             478 |        3122 |           153 |           839 |        652
             42 |      494 |             490 |        1601 |           841 |          1147 |        290
            257 |     2938 |               0 |           0 |           787 |           787 |          1
            257 |     2938 |            2535 |        1905 |           327 |           786 |        440
            257 |     2938 |            2547 |         511 |           788 |           951 |        163
            289 |     3297 |               0 |           0 |          1009 |          1009 |          1
            289 |     3297 |            3270 |        2542 |           542 |          1008 |        426
            289 |     3297 |            3277 |        1101 |          1010 |          1146 |        137
            290 |     3307 |               0 |           0 |           806 |           806 |          1
            290 |     3307 |            3341 |         830 |           640 |           805 |        166
            290 |     3307 |            3349 |        3751 |           807 |          1332 |        508
            433 |     4886 |               0 |           0 |           592 |           592 |          1
            433 |     4886 |            4754 |        2035 |           117 |           591 |        471
            433 |     4886 |            4764 |        1278 |           593 |           832 |        237
            447 |     5047 |               0 |           0 |           371 |           371 |          1
            447 |     5047 |            5064 |         409 |           277 |           370 |         94
(20 rows)
  */
	sql_stmt:=NULL;	/* Empty statement array */
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_lines_join_duplicates';
	IF l_filter IS NULL THEN /* Non test mode */
		l_sql_stmt:='CREATE TEMPORARY TABLE simplification_lines_join_duplicates'||E'\n';
	ELSE
		l_sql_stmt:='CREATE TABLE simplification_lines_join_duplicates'||E'\n';
	END IF;
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE '||l_sql_stmt||E'\n'||
'AS'||E'\n'||
'WITH a AS ('||E'\n'||
'	SELECT polygon_number, join_seq, joined_join_seq,'||E'\n'|| 
'	       MIN(num_polygon_number) AS min_num_polygon_number,'||E'\n'||
'	       MAX(num_polygon_number) AS max_num_polygon_number,'||E'\n'||
'	       MIN(pointflow) AS min_pointflow,'||E'\n'||
'	       MAX(pointflow) AS max_pointflow,'||E'\n'||
'	       COUNT(pointflow) AS num_points,'||E'\n'||
'	       ST_Makeline(array_agg(ST_SetSRID(coordinate, '||c1_rec.srid||') ORDER BY pointflow)) AS line,'||E'\n'||
'	       ST_SimplifyPreserveTopology('||E'\n'||
'	       		ST_Makeline('||E'\n'||
'	       			array_agg('||E'\n'||
'	       				ST_SetSRID(coordinate, '||c1_rec.srid||')'||E'\n'||
'	       				ORDER BY pointflow)), '||c3_rec.st_simplify_tolerance_zoomlevel_6||') AS simplified_line,'||E'\n'||
'	       ST_SimplifyPreserveTopology('||E'\n'||
'	       		ST_Makeline('||E'\n'||
'	       			array_agg('||E'\n'||
'	       				ST_SetSRID(coordinate, '||c1_rec.srid||')'||E'\n'||
'	       				ORDER BY pointflow)), '||c3_rec.st_simplify_tolerance_zoomlevel_8||') AS simplified_line_2,'||E'\n'||
'	       ST_SimplifyPreserveTopology('||E'\n'||
'	       		ST_Makeline('||E'\n'||
'	       			array_agg('||E'\n'||
'	       				ST_SetSRID(coordinate, '||c1_rec.srid||')'||E'\n'||
'	       				ORDER BY pointflow)), '||c3_rec.st_simplify_tolerance_zoomlevel_11||') AS simplified_line_3'||E'\n'||
'	  FROM simplification_points'||E'\n'||
'	 GROUP BY polygon_number, join_seq, joined_join_seq'||E'\n'||
'), b AS ('||E'\n'||
'	SELECT polygon_number, join_seq, COUNT(polygon_number) AS total'||E'\n'||
'	  FROM a'||E'\n'||
'	 GROUP BY polygon_number, join_seq'||E'\n'||
'	 HAVING COUNT(polygon_number) > 1'||E'\n'||	
')'||E'\n'||
'SELECT a.polygon_number, a.join_seq, COALESCE(joined_join_seq, 0) AS joined_join_seq,'||E'\n'||
'       ROUND(CAST(ST_Length(line) AS numeric), 0) AS line_length,'||E'\n'||
'       ROUND(CAST(ST_Length(simplified_line) AS numeric), 0) AS simplified_line_length,'||E'\n'||
'       min_num_polygon_number, max_num_polygon_number, min_pointflow, max_pointflow, num_points, line,'||E'\n'||
'       simplified_line, simplified_line_2, simplified_line_3, ''Y''::Text duplicate_join_to_be_removed'||E'\n'||
'  FROM a, b'||E'\n'||
' WHERE a.polygon_number  = b.polygon_number'||E'\n'||
'   AND a.join_seq = b.join_seq'||E'\n'||
' ORDER BY polygon_number, join_seq, joined_join_seq';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_lines_join_duplicates ADD PRIMARY KEY(polygon_number, join_seq, joined_join_seq)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_lines_join_duplicates';
--
-- Execute third block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_lines_join_duplicates');
--
-- Report on 1:1 mapping between join_seq and joined_join_seq test
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH b AS ('||E'\n'||
'	SELECT COUNT(DISTINCT(join_seq)) AS total, SUM(num_points) AS total_points, SUM(simplified_line_length) AS total_simplified_line_length'||E'\n'||
'	  FROM simplification_lines_join_duplicates'||E'\n'||
')'||E'\n'||
'SELECT duplicate_join_to_be_removed AS dup_join_removed,'||E'\n'|| 
'       COUNT(DISTINCT(join_seq)) AS t_join_seq, ROUND((CAST(COUNT(DISTINCT(join_seq)) AS numeric)/b.total)*100.0, 4) AS pct_join_seq,'||E'\n'||
'       SUM(num_points) AS points, ROUND((CAST( SUM(num_points) AS numeric)/b.total_points)*100.0, 4) AS pct_points,'||E'\n'||
'       SUM(simplified_line_length) AS t_simp_line_length,'||E'\n'|| 
'       ROUND((CAST( SUM(simplified_line_length) AS numeric)/b.total_simplified_line_length)*100.0, 4) AS pct_simp_line_length,'||E'\n'||
'       COUNT(DISTINCT(polygon_number)) AS t_polygon_number'||E'\n'||
'  FROM simplification_lines_join_duplicates a, b'||E'\n'||
' GROUP BY duplicate_join_to_be_removed, total, total_points, total_simplified_line_length', 'Report on 1:1 mapping between join_seq and joined_join_seq test');
/*
 dup_join_removed | t_join_seq | pct_join_seq | points  | pct_points | t_simp_line_length | pct_simp_line_length | t_area_id 
------------------+------------+--------------+---------+------------+--------------------+----------------------+-----------
 Y                |       2404 |     100.0000 | 1278172 |   100.0000 |           14023163 |             100.0000 |      1976
 */
--
-- Fourth block of SQL statements
--
-- Flag lines for reversal. Where shared simplification is in use the lines may need to be reversed if:
--
-- a) Start point line A == end point line b
-- b) End point line A == start point line b
-- c) duplicate_join_to_be_removed = N is both
-- d) Number of poibnts in both lines are the same
-- e) If line A join sequence is bigger line A stays the same
--    If line B join sequence is bigger line B is reversed
-- f) Line of the line (as an INT) is the same [originally assumed metre type units; has been changed to a % test]
--
	sql_stmt:=NULL;	/* Empty statement array */
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_lines_temp';
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE simplification_lines_temp'||E'\n'||
'AS'||E'\n'||
'SELECT a.polygon_number, a.join_seq, a.joined_join_seq, b.polygon_number AS joined_polygon_number,'||E'\n'||
'       a.duplicate_join_to_be_removed||''/''||COALESCE(b.duplicate_join_to_be_removed, '''') AS duplicate_join_to_be_removed,'||E'\n'||
'      CASE'||E'\n'|| 
'		WHEN ST_Startpoint(a.line)		 	  = ST_Endpoint(b.line)   AND'||E'\n'||  
'		     ST_Endpoint(a.line)   		  	  = ST_Startpoint(b.line) AND'||E'\n'|| 
'		     a.duplicate_join_to_be_removed               = ''N''                   AND'||E'\n'||
'		     b.duplicate_join_to_be_removed               = ''N''                   AND'||E'\n'||
'		     a.num_points                                 = b.num_points          AND'||E'\n'||
'		     a.join_seq                                   > b.join_seq            AND'||E'\n'||
--'		     ROUND(CAST(ST_Length(a.line) AS numeric), 0) = ROUND(CAST(ST_Length(b.line) AS numeric), 0) THEN ''Y'' /* Reverse */'||E'\n'||
'		     (a.num_points = 1 OR ROUND(ABS(1-(CAST(ST_Length(a.line) AS numeric)/(CAST(ST_Length(b.line) AS numeric)+0.00000001))), 3) <= 0.002)'||E'\n'|| 
'                 	 /* Avooid assuming decical range units, e.g. metres when rounding */ THEN ''Y'' /* Reverse */'||E'\n'|| 
'		WHEN ST_Startpoint(a.line)		 	  = ST_Endpoint(b.line)   AND'||E'\n'||  
'		     ST_Endpoint(a.line)   		  	  = ST_Startpoint(b.line) AND'||E'\n'|| 
'		     a.duplicate_join_to_be_removed               = ''N''                   AND'||E'\n'||
'		     b.duplicate_join_to_be_removed               = ''N''                   AND'||E'\n'||
'		     a.num_points                                 = b.num_points          AND'||E'\n'||
'		     a.join_seq                                   < b.join_seq            AND'||E'\n'||
--'		     ROUND(CAST(ST_Length(a.line) AS numeric), 0) = ROUND(CAST(ST_Length(b.line) AS numeric), 0) THEN ''S'' /* Stays the same */'||E'\n'|| 
'		     (a.num_points = 1 OR ROUND(ABS(1-(CAST(ST_Length(a.line) AS numeric)/(CAST(ST_Length(b.line) AS numeric)+0.00000001))), 3) <= 0.002)'||E'\n'|| 
'                 	 /* Avoid assuming decical range units, e.g. metres when rounding */ THEN ''S'' /* Stays the same */'||E'\n'|| 
'		ELSE ''N'''||E'\n'|| 
'       END AS reverse_line,'||E'\n'||
'      CASE'||E'\n'|| 
'		WHEN ST_Startpoint(a.simplified_line)			     = ST_Endpoint(b.simplified_line)   AND'||E'\n'|| 
'		     ST_Endpoint(a.simplified_line)   			     = ST_Startpoint(b.simplified_line) AND'||E'\n'||
'		     a.duplicate_join_to_be_removed              	     = ''N''                              AND'||E'\n'||
'		     b.duplicate_join_to_be_removed                  	     = ''N''                              AND'||E'\n'||
'		     a.num_points                                            = b.num_points                     AND'||E'\n'||
'		     a.join_seq                                              > b.join_seq                       AND'||E'\n'||
--'		     ROUND(CAST(ST_Length(a.simplified_line) AS numeric), 0) = ROUND(CAST(ST_Length(b.simplified_line) AS numeric), 0) THEN ''Y'' /* Reverse */'||E'\n'|| 
'		     (a.num_points = 1 OR ROUND(ABS(1-(CAST(ST_Length(a.simplified_line) AS numeric)/(CAST(ST_Length(b.simplified_line) AS numeric)+0.00000001))), 3) <= 0.002)'||E'\n'|| 
'                 	 /* Avoid assuming decical range units, e.g. metres when rounding */ THEN ''Y'' /* Reverse */'||E'\n'|| 
'		WHEN ST_Startpoint(a.simplified_line)			     = ST_Endpoint(b.simplified_line)   AND'||E'\n'|| 
'		     ST_Endpoint(a.simplified_line)   			     = ST_Startpoint(b.simplified_line) AND'||E'\n'||
'		     a.duplicate_join_to_be_removed                  	     = ''N''                              AND'||E'\n'||
'		     b.duplicate_join_to_be_removed                  	     = ''N''                              AND'||E'\n'||
'		     a.num_points                                            = b.num_points                     AND'||E'\n'||
'		     a.join_seq                                              < b.join_seq                       AND'||E'\n'||
--'		     ROUND(CAST(ST_Length(a.simplified_line) AS numeric), 0) = ROUND(CAST(ST_Length(b.simplified_line) AS numeric), 0) THEN ''S'' /* Stays the same */'||E'\n'||
'		     (a.num_points = 1 OR ROUND(ABS(1-(CAST(ST_Length(a.simplified_line) AS numeric)/(CAST(ST_Length(b.simplified_line) AS numeric)+0.00000001))), 3) <= 0.002)'||E'\n'|| 
'                 	 /* Avoid assuming decical range units, e.g. metres when rounding */ THEN ''S'' /* Stays the same */'||E'\n'|| 
'		ELSE ''N'''||E'\n'|| 
'       END AS reverse_simplified_line,'||E'\n'||
'       a.num_points AS num_points_a,'||E'\n'||
'       b.num_points AS num_points_b,'||E'\n'||
'       CASE'||E'\n'||
'		WHEN ST_Startpoint(a.line)		 	  = ST_Endpoint(b.line)   AND'||E'\n'||  
'		     ST_Endpoint(a.line)   		  	  = ST_Startpoint(b.line) THEN 1'||E'\n'||
'		ELSE 0'||E'\n'||
'       END AS line_start_endpoints_match,'||E'\n'||
'       CASE'||E'\n'||
'		WHEN ST_Startpoint(a.simplified_line)		 	  = ST_Endpoint(b.simplified_line)   AND'||E'\n'||  
'		     ST_Endpoint(a.simplified_line)   		  	  = ST_Startpoint(b.simplified_line) THEN 1'||E'\n'||
'		ELSE 0'||E'\n'||
'       END AS simplified_line_start_endpoints_match,'||E'\n'||
'       ROUND(ABS(1-(CAST(ST_Length(a.simplified_line) AS numeric)/(CAST(ST_Length(b.simplified_line) AS numeric)+0.00000001))), 3) AS simplified_line_test,'||E'\n'||
'       ROUND(ABS(1-(CAST(ST_Length(a.line) AS numeric)/(CAST(ST_Length(b.line) AS numeric)+0.00000001))), 3) AS line_test,'||E'\n'||
'       ST_Length(a.simplified_line) AS simplified_line_length,'||E'\n'||
'       ST_Length(a.line) AS line_length,'||E'\n'||
'       ST_Reverse(b.line) AS revered_line,'||E'\n'||
'       ST_Reverse(b.simplified_line) AS reversed_simplified_line,'||E'\n'||
'       ST_Reverse(b.simplified_line_2) AS reversed_simplified_line_2,'||E'\n'||
'       ST_Reverse(b.simplified_line_3) AS reversed_simplified_line_3'||E'\n'||
'  FROM simplification_lines a'||E'\n'||
'	LEFT OUTER JOIN simplification_lines b ON (a.joined_join_seq = b.join_seq)'||E'\n'||
' ORDER BY a.polygon_number, a.join_seq';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_lines_temp ADD PRIMARY KEY(polygon_number, join_seq)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE UNIQUE INDEX simplification_lines_temp_uk ON simplification_lines_temp(join_seq)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_lines_temp';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_lines ADD reverse_simplified_line VARCHAR(1)';
--
-- Carry out line reversal
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE UPDATE simplification_lines a'||E'\n'||
'   SET simplified_line = ('||E'\n'||
'	SELECT reversed_simplified_line'||E'\n'|| 
'	  FROM simplification_lines_temp b'||E'\n'||
'	 WHERE a.join_seq = b.join_seq), reverse_simplified_line = ''Y'''||E'\n'||
' WHERE a.join_seq IN ('||E'\n'||
'	SELECT join_seq'||E'\n'||
'	  FROM simplification_lines_temp'||E'\n'||
'	 WHERE reverse_simplified_line = ''Y'')';
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE UPDATE simplification_lines a'||E'\n'||
'   SET simplified_line_2 = ('||E'\n'||
'	SELECT reversed_simplified_line_2'||E'\n'|| 
'	  FROM simplification_lines_temp b'||E'\n'||
'	 WHERE a.join_seq = b.join_seq), reverse_simplified_line = ''Y'''||E'\n'||
' WHERE a.join_seq IN ('||E'\n'||
'	SELECT join_seq'||E'\n'||
'	  FROM simplification_lines_temp'||E'\n'||
'	 WHERE reverse_simplified_line = ''Y'')';
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE UPDATE simplification_lines a'||E'\n'||
'   SET simplified_line_3 = ('||E'\n'||
'	SELECT reversed_simplified_line_3'||E'\n'|| 
'	  FROM simplification_lines_temp b'||E'\n'||
'	 WHERE a.join_seq = b.join_seq), reverse_simplified_line = ''Y'''||E'\n'||
' WHERE a.join_seq IN ('||E'\n'||
'	SELECT join_seq'||E'\n'||
'	  FROM simplification_lines_temp'||E'\n'||
'	 WHERE reverse_simplified_line = ''Y'')';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE UPDATE simplification_lines'||E'\n'||
'  SET reverse_simplified_line = ''N'''||E'\n'||
' WHERE reverse_simplified_line IS NULL';
--
-- Execute fourth block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_lines_temp');
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_lines');
--
-- Display joins and reversals
--
-- The duplicate_join_to_be_removed flag is where there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq
-- duplicate_join_to_be_removed applies to both side of a joined line so:
--
-- N/Y means the line cannot be reversed or replaced as the duplicate status varies
-- Y/ duplicates, but only a single point
-- N/ no duplicates, but only a single point
-- N/N No duplicates; 1, 2 or 3 polygon join; Can reverse:Y, stays same: S or N [default no matching criteria; should not be any]
--
	PERFORM rif40_sql_pkg.rif40_method4('SELECT duplicate_join_to_be_removed, reverse_line, reverse_simplified_line,'||E'\n'||
'       COUNT(join_seq) AS t_join_seq,'||E'\n'||
'       SUM(num_points_a) AS num_points_a,'||E'\n'||
'       SUM(COALESCE(num_points_b, 0)) AS num_points_b'||E'\n'||
'  FROM simplification_lines_temp'||E'\n'||
' GROUP BY duplicate_join_to_be_removed, reverse_line, reverse_simplified_line'||E'\n'||
' ORDER BY 2, 3', 'Joins and reversals');
/*
duplicate_join_to_be_removed | reverse_line | reverse_simplified_line | t_join_seq           | num_points
------------------------------------------------------------------------------------------------------------
N/Y                          | N            | N                       | 23                   | 42055     
N/                           | N            | N                       | 223                  | 82318     
Y/                           | N            | N                       | 103                  | 378650    
N/N                          | S            | S                       | 47                   | 88485     
N/N                          | Y            | Y                       | 47                   | 88485     
(5 rows)
 */
--
-- Test 9: join sequence(s) causing invalid joins and reversals
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 9);
--
-- Execute final block of SQL statements
--
	sql_stmt=NULL;
	sql_stmt[1]:='DROP TABLE simplification_lines_temp';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);

--
-- Update spatial linestrings table, e.g. t_rif40_linestrings_ew01_ward2001 if there is no filter
--
	l_spatial_linestrings_table:='t_rif40_linestrings_'||LOWER(l_geography)||'_'||LOWER(l_geolevel);
	IF l_filter IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_phase_II', 'Simplify % geography geolevel %: Phase II (Create the lines table) Update spatial geolevel table: %', 
			l_geography::VARCHAR, 
			l_geolevel::VARCHAR,
			l_spatial_linestrings_table::VARCHAR); 
--
-- Do update
--
-- ADD
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_simplify_geometry_phase_II', 'Simplify % geography geolevel %: Phase II (Create the lines table) No update of spatial geolevel table: %, test filter present', 
			l_geography::VARCHAR, 
			l_geolevel::VARCHAR,
			l_spatial_linestrings_table::VARCHAR); 
	END IF;

--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_phase_II', 'Simplify % geography geolevel %: Phase II (Create the lines table) took: %, total space used: %', 
		l_geography::VARCHAR		/* Geography */, 
		l_geolevel::VARCHAR			/* Geolevel */, 
		took::VARCHAR				/* Took */,
		pg_size_pretty(rel_size)::VARCHAR	/* Total size of table and indexes */);
--
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg._simplify_geometry_phase_II(VARCHAR, VARCHAR, VARCHAR, NUMERIC) IS 'Function: 	_simplify_geometry_phase_II()
Parameters:	Geography, geolevel, 
            geolevel; filter (for testing, no default), 
            minimum point resolution (default 1 - assumed metre, but depends on the geometry)
Returns:	Nothing
Description:	Simplify geography geolevel - Phase II: Create the lines table

Phase II: Create the lines table

Create and populate simplification_lines table. Processed in two sections:

a) Good joins - where there is a 1:1 mapping between join_seq, polygon_number and joined_join_seq
b) Add in the duplicate joins - where there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq

Display the duplicate joins to be removed (duplicate_join_to_be_removed=Y). 

Note:

a) Duplicate joins to be removed are all one or two polygon joins
b) OK joins are either 1, 2 or 3 polygons joins (1 is an outside edge) and MIN == MAX

Report on join duplicates. This is the polygon_number/join_seq/joined_join_seq where there is not a 1:1 mapping between join_seq, 
polygon_number and joined_join_seq cannot used shared simplification and are therefore a potential source of slivers

The table simplification_lines_join_duplicates table allows the join duplicates to be explored offline within the same session

Report on 1:1 mapping between join_seq and joined_join_seq test

Flag lines for reversal. Where shared simplification is in use the lines may need to be reversed if:

a) Start point line A == end point line b
b) End point line A == start point line b
c) duplicate_join_to_be_removed = N is both
d) Number of poibnts in both lines are the same
e) If line A join sequence is bigger line A stays the same
   If line B join sequence is bigger line B is reversed
f) Line of the line (as an INT) is the same [originally assumed metre type units; has been changed to a % test]

Carry out line reversal

Display joins and reversals

The duplicate_join_to_be_removed flag is where there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq
duplicate_join_to_be_removed applies to both side of a joined line so:

  N/Y means the line cannot be reversed or replaced as the duplicate status varies
  Y/ duplicates, but only a single point  
  N/ no duplicates, but only a single point
  N/N No duplicates; 1, 2 or 3 polygon join; Can reverse:Y, stays same: S or N [default no matching criteria; should not be any]

Test 9: join sequence(s) causing invalid joins and reversals
Update spatial linestrings table, e.g. t_rif40_linestrings_ew01_ward2001 if there is no filter [NOT CURRENTLY IMPLEMENTED]

Example SQL>

CREATE TABLE simplification_lines(
 geolevel_name                  VARCHAR(30),
 polygon_number                 bigint,
 area_id                        VARCHAR(300),
 join_seq                       bigint,
 joined_join_seq        bigint,
 line_length                    NUMERIC,
 simplified_line_length         NUMERIC,
 min_num_polygon_number         bigint,
 max_num_polygon_number         bigint,
 min_pointflow                  bigint,
 max_pointflow                  bigint,
 num_points                     bigint,
 duplicate_join_to_be_removed   VARCHAR(1));

INSERT INTO simplification_lines
      (geolevel_name, area_id, polygon_number, join_seq, joined_join_seq, line_length, simplified_line_length,
        min_num_polygon_number, max_num_polygon_number, min_pointflow, max_pointflow, num_points, line, simplified_line, duplicate_join_to_be_removed)
WITH a AS /* Convert co-ordinate points to LINESTRINGs */ (
        SELECT area_id, polygon_number, join_seq, joined_join_seq,
               MIN(num_polygon_number) AS min_num_polygon_number,
               MAX(num_polygon_number) AS max_num_polygon_number,
               MIN(pointflow) AS min_pointflow,
               MAX(pointflow) AS max_pointflow,
               COUNT(pointflow) AS num_points,
               ST_RemoveRepeatedPoints(                         /* Remove repeated points */
                 ST_Makeline(array_agg(                         /* Convert to line */
                   ST_SetSRID(coordinate, 27700 /* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))) AS line,
               ST_SimplifyPreserveTopology(                     /* Run Douglas-Peuker on line to simplify */
                 ST_RemoveRepeatedPoints(                       /* Remove repeated points */
                   ST_Makeline(array_agg(                       /* Convert to line */
                     ST_SetSRID(coordinate, 27700       /* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))), 
                   50                   /* Simplify tolerance */) AS simplified_line
          FROM simplification_points
         GROUP BY area_id, polygon_number, join_seq, joined_join_seq
), b AS /* 1:1 mapping between join_seq, polygon_number and joined_join_seq - can use shared implification */ (
        SELECT polygon_number, join_seq, COUNT(polygon_number) AS total
          FROM a
         GROUP BY polygon_number, join_seq
         HAVING COUNT(polygon_number) = 1
), c AS /* No 1:1 mapping between join_seq, polygon_number and joined_join_seq */ (
        SELECT DISTINCT join_seq
          FROM (
                SELECT polygon_number, join_seq, COUNT(polygon_number) AS total
                  FROM a
                 GROUP BY polygon_number, join_seq
                 HAVING COUNT(polygon_number) > 1
        ) c1
), d AS /* Simplify separately - as there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq */ (
        SELECT d1.area_id, d1.polygon_number, d1.join_seq, NULL::bigint joined_join_seq,
               MIN(num_polygon_number) AS min_num_polygon_number,
               MAX(num_polygon_number) AS max_num_polygon_number,
               MIN(pointflow) AS min_pointflow,
               MAX(pointflow) AS max_pointflow,
               COUNT(pointflow) AS num_points,
               ST_RemoveRepeatedPoints(                         /* Remove repeated points */
                 ST_Makeline(array_agg(                         /* Convert to line */
                   ST_SetSRID(coordinate, 27700 /* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))) AS line,
               ST_SimplifyPreserveTopology(                     /* Run Douglas-Peuker on line to simplify */
                 ST_RemoveRepeatedPoints(                       /* Remove repeated points */
                   ST_Makeline(array_agg(                       /* Convert to line */
                     ST_SetSRID(coordinate, 27700       /* Convert coordinates to Geogrpahy SRID */) ORDER BY pointflow))),
                   50                       /* Simplify tolerance */) AS simplified_line
          FROM simplification_points d1, c
         WHERE d1.join_seq = c.join_seq
         GROUP BY d1.area_id, d1.polygon_number, d1.join_seq
)
SELECT /* Good joins - where there is a 1:1 mapping between join_seq, polygon_number and joined_join_seq */ ''LADUA2001'' AS geolevel_name,
       area_id, a.polygon_number, a.join_seq, joined_join_seq,
       ROUND(CAST(ST_Length(line) AS numeric), 0) AS line_length,
       ROUND(CAST(ST_Length(simplified_line) AS numeric), 0) AS simplified_line_length, 
       min_num_polygon_number, max_num_polygon_number, min_pointflow, max_pointflow, num_points, line, simplified_line, ''N'' AS duplicate_join_to_be_removed
  FROM a, b
 WHERE a.polygon_number  = b.polygon_number
   AND a.join_seq        = b.join_seq
UNION
SELECT /* Add in the duplicate joins - where there is no 1:1 mapping between join_seq, polygon_number and joined_join_seq */ ''LADUA2001'' AS geolevel_name,
       area_id, polygon_number, join_seq, joined_join_seq /* This is NULL */,
       ROUND(CAST(ST_Length(line) AS numeric), 0) AS line_length,
       ROUND(CAST(ST_Length(simplified_line) AS numeric), 0) AS simplified_line_length,
       min_num_polygon_number, max_num_polygon_number, min_pointflow, max_pointflow, num_points, line, simplified_line,
       CASE
                WHEN max_num_polygon_number > 2  THEN ''M'' /* Mutli (>2) polygon join, e.g. triple point between 3 polygons */
                WHEN max_num_polygon_number = 2  THEN ''Y'' /* Two polygon join */
                WHEN max_num_polygon_number = 1  THEN ''E'' /* Outer edge */
                ELSE                                  ''?'' /* Unknown */
       END AS duplicate_join_to_be_removed
  FROM d
 ORDER BY 1, 2, 3, 4;

CREATE TEMPORARY TABLE simplification_lines_join_duplicates
AS
WITH a AS (
        SELECT polygon_number, join_seq, joined_join_seq,
               MIN(num_polygon_number) AS min_num_polygon_number,
               MAX(num_polygon_number) AS max_num_polygon_number,
               MIN(pointflow) AS min_pointflow,
               MAX(pointflow) AS max_pointflow,
               COUNT(pointflow) AS num_points,
               ST_Makeline(array_agg(ST_SetSRID(coordinate, 27700) ORDER BY pointflow)) AS line,
               ST_SimplifyPreserveTopology(ST_Makeline(array_agg(ST_SetSRID(coordinate, 27700) ORDER BY pointflow)), 50) AS simplified_line
          FROM simplification_points
         GROUP BY polygon_number, join_seq, joined_join_seq
), b AS (
        SELECT polygon_number, join_seq, COUNT(polygon_number) AS total
          FROM a
         GROUP BY polygon_number, join_seq
         HAVING COUNT(polygon_number) > 1
)
SELECT a.polygon_number, a.join_seq, COALESCE(joined_join_seq, 0) AS joined_join_seq,
       ROUND(CAST(ST_Length(line) AS numeric), 0) AS line_length,
       ROUND(CAST(ST_Length(simplified_line) AS numeric), 0) AS simplified_line_length,
       min_num_polygon_number, max_num_polygon_number, min_pointflow, max_pointflow, num_points, line, simplified_line, ''Y''::Text duplicate_join_to_be_removed
  FROM a, b
 WHERE a.polygon_number  = b.polygon_number
   AND a.join_seq = b.join_seq
 ORDER BY polygon_number, join_seq, joined_join_seq;

CREATE TEMPORARY TABLE simplification_lines_temp
AS
SELECT a.polygon_number, a.join_seq, a.joined_join_seq, b.polygon_number AS joined_polygon_number,
       a.duplicate_join_to_be_removed||''/''||COALESCE(b.duplicate_join_to_be_removed, '') AS duplicate_join_to_be_removed,
      CASE
                WHEN ST_Startpoint(a.line)                        = ST_Endpoint(b.line)   AND
                     ST_Endpoint(a.line)                          = ST_Startpoint(b.line) AND
                     a.duplicate_join_to_be_removed               = ''N''                   AND
                     b.duplicate_join_to_be_removed               = ''N''                   AND
                     a.num_points                                 = b.num_points          AND
                     a.join_seq                                   > b.join_seq            AND
                     (a.num_points = 1 OR ROUND(ABS(1-(CAST(ST_Length(a.line) AS numeric)/(CAST(ST_Length(b.line) AS numeric)+0.00000001))), 3) <= 0.001)
                         /* Avooid assuming decical range units, e.g. metres when rounding */ THEN ''Y'' /* Reverse */
                WHEN ST_Startpoint(a.line)                        = ST_Endpoint(b.line)   AND
                     ST_Endpoint(a.line)                          = ST_Startpoint(b.line) AND
                     a.duplicate_join_to_be_removed               = ''N''                   AND
                     b.duplicate_join_to_be_removed               = ''N''                   AND
                     a.num_points                                 = b.num_points          AND
                     a.join_seq                                   < b.join_seq            AND
                     (a.num_points = 1 OR ROUND(ABS(1-(CAST(ST_Length(a.line) AS numeric)/(CAST(ST_Length(b.line) AS numeric)+0.00000001))), 3) <= 0.001)
                         /* Avoid assuming decical range units, e.g. metres when rounding */ THEN ''S'' /* Stays the same */
                ELSE ''N''
       END AS reverse_line,
      CASE
                WHEN ST_Startpoint(a.simplified_line)                        = ST_Endpoint(b.simplified_line)   AND
                     ST_Endpoint(a.simplified_line)                          = ST_Startpoint(b.simplified_line) AND
                     a.duplicate_join_to_be_removed                          = ''N''                              AND
                     b.duplicate_join_to_be_removed                          = ''N''                              AND
                     a.num_points                                            = b.num_points                     AND
                     a.join_seq                                              > b.join_seq                       AND
                     (a.num_points = 1 OR ROUND(ABS(1-(CAST(ST_Length(a.simplified_line) AS numeric)/(CAST(ST_Length(b.simplified_line) AS numeric)+0.00000001))), 3) <= 0.001)
                         /* Avoid assuming decical range units, e.g. metres when rounding */ THEN ''Y'' /* Reverse */
                WHEN ST_Startpoint(a.simplified_line)                        = ST_Endpoint(b.simplified_line)   AND
                     ST_Endpoint(a.simplified_line)                          = ST_Startpoint(b.simplified_line) AND
                     a.duplicate_join_to_be_removed                          = ''N''                              AND
                     b.duplicate_join_to_be_removed                          = ''N''                              AND
                     a.num_points                                            = b.num_points                     AND
                     a.join_seq                                              < b.join_seq                       AND
                     (a.num_points = 1 OR ROUND(ABS(1-(CAST(ST_Length(a.simplified_line) AS numeric)/(CAST(ST_Length(b.simplified_line) AS numeric)+0.00000001))), 3) <= 0.001)
                         /* Avoid assuming decical range units, e.g. metres when rounding */ THEN ''S'' /* Stays the same */
                ELSE ''N''
       END AS reverse_simplified_line,
       a.num_points,
       ROUND(ABS(1-(CAST(ST_Length(a.simplified_line) AS numeric)/(CAST(ST_Length(b.simplified_line) AS numeric)+0.00000001))), 3) AS simplified_line_test,
       ROUND(ABS(1-(CAST(ST_Length(a.line) AS numeric)/(CAST(ST_Length(b.line) AS numeric)+0.00000001))), 3) AS line_test,
       ST_Length(a.simplified_line) AS simplified_line_length,
       ST_Length(a.line) AS line_length,
       ST_Reverse(b.line) AS revered_line, ST_Reverse(b.simplified_line) AS reversed_simplified_line
  FROM simplification_lines a
        LEFT OUTER JOIN simplification_lines b ON (a.joined_join_seq = b.join_seq)
 ORDER BY a.polygon_number, a.join_seq;';

 --
 -- Eof