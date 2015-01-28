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
--								  Simplify geography geolevel - checks
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg._simplify_geometry_checks(l_geography VARCHAR, l_geolevel VARCHAR, check_number INTEGER)
RETURNS void 
SECURITY INVOKER
AS $body$
DECLARE
/*
Function: 	_simplify_geometry_checks()
Parameters:	Geography, geolevel,  check number
Returns:	Nothing
Description:	Simplify geography geolevel - checks

Test 0: Geometry is valid
Test 1: Check last_pointflow = one per area (should be zero)
Test 1.1: Check there are 1 or more distinct areas in simplification_points_a (i.e. filter is not broken)
Test 2: for un-detected duplicates
Test 2.1: for invalid duplicates
Test 2.2: Check array dimensions are [1:3] as expected
Test 3: for areas with no join sequence
Test 4: for areas with mis-joined sequences
Test 5: join sequence(s) used by more than one polygon_number
Test 6: polygon_number, coord_id, duplicate(s) used by more than one join sequence
Test 7: areas(s) with non unique join sequences
Test 8: areas(s) with invalid geometry in simplification_polygons
Test 9: join sequence(s) causing invalid joins and reversals

 */
	c0_sgck REFCURSOR;
	c11_sgck CURSOR FOR
		SELECT COUNT(DISTINCT(area_id)) AS t_areas
		  FROM simplification_points_a;
	c1_sgck CURSOR FOR
		SELECT COUNT(area_id) AS t_areas
		  FROM (
			SELECT area_id, polygon_number, COUNT(DISTINCT(last_pointflow)) t_last_pointflow	
			  FROM simplification_points_a
			 GROUP BY area_id, polygon_number
			 HAVING COUNT(DISTINCT(last_pointflow)) != 1) a;
	c2_sgck CURSOR FOR	
		SELECT SUM(distinct_polys) AS distinct_polys
		  FROM (
			SELECT coord_id, COUNT(DISTINCT(num_polygon_number)) AS distinct_polys
			  FROM simplification_points_d
			 WHERE num_polygon_number = 2
			   AND duplicate IN ('N', 'L') 
			 GROUP BY coord_id
			HAVING COUNT(DISTINCT(polygon_number)) > 2) a;
	c21_sgck CURSOR FOR	
		SELECT COUNT(polygon_number) AS t_areas
		  FROM simplification_points_c
		 WHERE duplicate = '?';
	c22_sgck CURSOR FOR	
		SELECT array_dims(path) AS array_dims, COUNT(DISTINCT(area_id)) AS total
	         FROM simplification_points_a
	        GROUP BY array_dims(path);
	c3_sgck CURSOR FOR	
		SELECT COUNT(join_seq) AS t_join_seq
		  FROM simplification_points
		 WHERE join_seq IS NULL;
	c4_sgck CURSOR FOR	
		SELECT COUNT(polygon_number) t_polygon_number
		  FROM ( 
			SELECT polygon_number, MIN(joined_polygon_number) AS joined_polygon_number, MAX(joined_polygon_number) AS joined_polygon_number2,
			       join_seq, MIN(pointflow) AS min, MAX(pointflow) AS max, COUNT(pointflow) AS total
			  FROM simplification_points
			 GROUP BY polygon_number, join_seq
			 HAVING MIN(joined_polygon_number) != MAX(joined_polygon_number)) a;
	c5_sgck CURSOR FOR	
		SELECT COUNT(join_seq) t_join_seq
		  FROM ( 
			SELECT join_seq, COUNT(DISTINCT(joined_polygon_number)) AS t_joined_polygon_number
			  FROM simplification_points_f
			 GROUP BY join_seq
		 HAVING COUNT(DISTINCT(joined_polygon_number)) > 1) a;
	c6_sgck CURSOR FOR	
		SELECT COUNT(joined_polygon_number) t_joined_polygon_number
		  FROM ( 
			SELECT joined_polygon_number, coord_id, duplicate, COUNT(DISTINCT(join_seq)) AS t_join_seq
			  FROM simplification_points_f
			 GROUP BY joined_polygon_number, coord_id, duplicate
			 HAVING COUNT(DISTINCT(join_seq)) > 1) a;
	c7_sgck CURSOR FOR	
		SELECT COUNT(join_seq) t_join_seq
		  FROM (
			SELECT join_seq, COUNT(DISTINCT(polygon_number)) AS distinct_area
			  FROM simplification_points
			 GROUP BY join_seq
			 HAVING COUNT(DISTINCT(polygon_number)) > 1) a;
	c8_sgck CURSOR FOR	
		SELECT COUNT(area_id) t_area_id
		  FROM simplification_polygons
		 WHERE ST_ISValid(geometry) = FALSE OR ST_ISValid(topo_optimised_geometry) = FALSE;
	c9_sgck CURSOR FOR	
		SELECT COUNT(join_seq) AS t_join_seq, MAX(line_test) AS max_line_test, MAX(simplified_line_test) AS max_simplified_line_test
		  FROM simplification_lines_temp
		 WHERE duplicate_join_to_be_removed = 'N/N'
		   AND reverse_line = 'N'
		   AND reverse_simplified_line = 'N';
	c90_sgck REFCURSOR;
	c0_rec RECORD;
	c1_rec RECORD;
	c11_rec RECORD;
	c2_rec RECORD;
	c21_rec RECORD;
	c22_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
	c5_rec RECORD;
	c6_rec RECORD;
	c7_rec RECORD;
	c8_rec RECORD;
	c9_rec RECORD;
	c90_rec RECORD;
--
	sql_stmt	VARCHAR;
--
	line_test_limit NUMERIC:=0.002; /* This is the maximum difference tolerated between joined lines when all the points are the same (i.e. the is a slight misorder) */
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, '_simplify_geometry_checks', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Test 0: Geometry is valid
--
	IF check_number = 0 THEN
		sql_stmt:='SELECT COUNT(DISTINCT(CASE WHEN ST_Isvalid(shapefile_geometry) = FALSE THEN area_id ELSE NULL END)) AS invalid_area_id_geometry'||E'\n'||
'          FROM rif40_partitions.'||quote_ident('p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(l_geolevel))||E'\n'||
'         WHERE geolevel_name = '''||UPPER(l_geolevel)||'''';
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_simplify_geometry_checks', 'SQL> %;', sql_stmt::VARCHAR);
		OPEN c0_sgck FOR EXECUTE sql_stmt;
		FETCH c0_sgck INTO c0_rec;	
		CLOSE c0_sgck;
		IF c0_rec.invalid_area_id_geometry > 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-10070, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % areas/polygons(s) with invalid geometry',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */, 
				c0_rec.invalid_area_id_geometry::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No areas/polygons(s) with invalid geometry',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
--
	ELSIF check_number = 1 THEN
--
-- Test 1: Check last_pointflow = one per area (should be zero)
--
		OPEN c1_sgck;
		FETCH c1_sgck INTO c1_rec;	
		CLOSE c1_sgck;
--
		IF c1_rec.t_areas > 0 THEN
			PERFORM rif40_sql_pkg.rif40_method4('SELECT polygon_number, COUNT(DISTINCT(last_pointflow)) t_last_pointflow'||E'\n'||
'  FROM simplification_points_a'||E'\n'||
' GROUP BY polygon_number, polygon_number'||E'\n'||
' HAVING COUNT(DISTINCT(last_pointflow)) != 1 LIMIT 20', 'Test 1: Check last_pointflow = one per area (should be zero)');
			PERFORM rif40_log_pkg.rif40_error(-10071, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % polygons(s) with >1 last_pointflow values',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */, 
				c1_rec.t_areas::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No polygons(s) have >1 last_pointflow values',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
	ELSIF check_number = 11 THEN
--
-- Test 1.1: Check there are 1 or more distinct areas in simplification_points_a (i.e. filter is not broken)
--
		OPEN c11_sgck;
		FETCH c11_sgck INTO c11_rec;	
		CLOSE c11_sgck;
--
		IF c11_rec.t_areas = 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-10072, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found no areas(s)',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % distinct areas(s)',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */, 
				c11_rec.t_areas::VARCHAR);
		END IF;
	ELSIF check_number = 2 THEN
--
-- Test 2: for un-detected duplicates
--
		OPEN c2_sgck;
		FETCH c2_sgck INTO c2_rec;	
		CLOSE c2_sgck;
--
		IF c2_rec.distinct_polys > 0 THEN
			PERFORM rif40_sql_pkg.rif40_method4('SELECT coord_id, COUNT(DISTINCT(num_polygon_number)) AS distinct_polygons'||E'\n'||
'  FROM simplification_points_d'||E'\n'||
' WHERE num_polygon_number = 2'||E'\n'||
'   AND duplicate IN (''N'', ''L'')'||E'\n'||
' GROUP BY coord_id'||E'\n'||
'HAVING COUNT(DISTINCT(num_polygon_number)) > 2 LIMIT 100', 'Test 2: for un-detected duplicates');
			PERFORM rif40_log_pkg.rif40_error(-10073, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % polygons(s) with undetected duplicates', 
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */,
				c2_rec.distinct_area::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No polygons(s) have undetected duplicates',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
	ELSIF check_number = 21 THEN
--
-- Test 2.1: for invalid duplicates
--
		OPEN c21_sgck;
		FETCH c21_sgck INTO c21_rec;	
		CLOSE c21_sgck;
--
		IF c21_rec.t_areas > 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-10074, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % polygons(s) with invalid (''?'') duplicate codes', 
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */,
				c21_rec.t_areas::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No polygons(s) have invalid (''?'') duplicate codes',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
	ELSIF check_number = 22 THEN
--
-- Test 2.2: Check array dimensions are [1:3] as expected
--
		FOR c22_rec IN c22_sgck LOOP
			IF c22_rec.array_dims != '[1:3]' THEN
				PERFORM rif40_log_pkg.rif40_error(-10075, '_simplify_geometry_checks', 
					'Check %: % geography geolevel %: Found % area_id(s) with unsupported (%) geography_dump dimensions', 
					check_number::VARCHAR		/* Check number */,
					l_geography::VARCHAR		/* Geography */, 
					l_geolevel::VARCHAR		/* Geolevel */,
					c22_rec.total::VARCHAR		/* Total polygons */,
					c22_rec.array_dims::VARCHAR 	/* geography_dump array dimension */);
			ELSE
				
			END IF;
		END LOOP;
--
		PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
			'Check %: % geography geolevel %: No area_id(s) unsupported (not [1:3]) geography_dump dimensions',
			check_number::VARCHAR	/* Check number */,
			l_geography::VARCHAR	/* Geography */, 
			l_geolevel::VARCHAR	/* Geolevel */);
--
	ELSIF check_number = 3 THEN
--	
-- Test 3: for areas with no join sequence
--
		OPEN c3_sgck;
		FETCH c3_sgck INTO c3_rec;	
		CLOSE c3_sgck;
--
		IF c3_rec.t_join_seq > 0 THEN
			PERFORM rif40_sql_pkg.rif40_method4('SELECT * FROM simplification_points WHERE join_seq IS NULL LIMIT 100', 'Test 3: for areas with no join sequence');
			PERFORM rif40_log_pkg.rif40_error(-10076, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % polygons(s) with no join sequence',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */,
				c3_rec.t_join_seq::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No polygons(s) with no join sequences',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
	ELSIF check_number = 4 THEN
--
-- Test 4: for areas with mis-joined sequences
--
		OPEN c4_sgck;
		FETCH c4_sgck INTO c4_rec;	
		CLOSE c4_sgck;
--
		IF c4_rec.t_polygon_number > 0 THEN
			PERFORM rif40_sql_pkg.rif40_method4('SELECT polygon_number,'||E'\n'||
'       MIN(joined_polygon_number) AS joined_polygon_number, MAX(joined_polygon_number) AS joined_polygon_number2,'||E'\n'||
'       join_seq, MIN(pointflow) AS min, MAX(pointflow) AS max, COUNT(pointflow) AS total'||E'\n'||
'  FROM simplification_points'||E'\n'||
' GROUP BY polygon_number, join_seq'||E'\n'||
' HAVING MIN(joined_polygon_number) != MAX(joined_polygon_number)'||E'\n'||
' ORDER BY polygon_number, join_seq', 'Test 4: for areas with mis-joined sequences');
			PERFORM rif40_log_pkg.rif40_error(-10077, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % polygons(s) with mis-joined sequences',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */, 
				c4_rec.t_polygon_number::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No polygons(s) with no mis-joined sequences',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
	ELSIF check_number = 5 THEN
--
-- Test 5: join sequence(s) used by more than one polygon_number
--
		OPEN c5_sgck;
		FETCH c5_sgck INTO c5_rec;	
		CLOSE c5_sgck;
		IF c5_rec.t_join_seq > 0 THEN
			PERFORM rif40_sql_pkg.rif40_method4('SELECT join_seq, COUNT(DISTINCT(joined_polygon_number)) AS t_polygon_number'||E'\n'||
'  FROM simplification_points_f'||E'\n'||
' GROUP BY join_seq'||E'\n'||
' HAVING COUNT(DISTINCT(joined_polygon_number)) > 1 LIMIT 10', 'Test 5: join sequence(s) used by more than one polygon_number');
			PERFORM rif40_log_pkg.rif40_error(-10078, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % join sequence(s) used by more than one polygon_number',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */, 
				c5_rec.t_join_seq::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No join sequence(s) not used by more than one polygon_number',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
	ELSIF check_number = 6 THEN
--
-- Test 6: polygon_number, coord_id, duplicate(s) used by more than one join sequence
--
		OPEN c6_sgck;
		FETCH c6_sgck INTO c6_rec;	
		CLOSE c6_sgck;
		IF c6_rec.t_joined_polygon_number > 0 THEN
			PERFORM rif40_sql_pkg.rif40_method4('SELECT joined_polygon_number, coord_id, duplicate, COUNT(DISTINCT(join_seq)) AS t_join_seq'||E'\n'||
'  FROM simplification_points_f'||E'\n'||
' GROUP BY joined_polygon_number, coord_id, duplicate'||E'\n'||
' HAVING COUNT(DISTINCT(join_seq)) > 1 LIMIT 10', 'Test 6: polygon_number, coord_id, duplicate(s) used by more than one join sequence');
			PERFORM rif40_log_pkg.rif40_error(-10079, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % joined_polygon_number, coord_id, duplicate(s) used by more than one join sequence',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */, 
				c6_rec.t_joined_polygon_number::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No joined_polygon_number, coord_id, duplicate(s) not used by more than one join sequence',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
	ELSIF check_number = 7 THEN
--
-- Test 7: areas(s) with non unique join sequences
--
		OPEN c7_sgck;
		FETCH c7_sgck INTO c7_rec;	
		CLOSE c7_sgck;
		IF c7_rec.t_join_seq > 0 THEN
			PERFORM rif40_sql_pkg.rif40_method4('SELECT join_seq, COUNT(DISTINCT(polygon_number)) AS distinct_polys'||E'\n'||
'  FROM simplification_points'||E'\n'||
' GROUP BY join_seq'||E'\n'||
' HAVING COUNT(DISTINCT(polygon_number)) > 1', 'Test 7: areas(s) with non unique join sequences');
			PERFORM rif40_log_pkg.rif40_error(-10080, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % polygons(s) with non unique join sequences',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */, 
				c7_rec.t_join_seq::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No polygons(s) with no non unique join sequences',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
--
-- Test 8: areas(s) with invalid geometry in simplification_polygons
--
	ELSIF check_number = 8 THEN
		OPEN c8_sgck;
		FETCH c8_sgck INTO c8_rec;	
		CLOSE c8_sgck;
		IF c8_rec.t_area_id > 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-10081, '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: Found % areas/polygons(s) with invalid geometry in simplification_polygons',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */,
				c8_rec.t_area_id::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No areas/polygons(s) with invalid geometry in simplification_polygons',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
--
-- Test 9: join sequence(s) causing invalid joins and reversals
--
	ELSIF check_number = 9 THEN
		OPEN c9_sgck;
		FETCH c9_sgck INTO c9_rec;	
		CLOSE c9_sgck;
		IF c9_rec.t_join_seq > 0 THEN
			PERFORM rif40_sql_pkg.rif40_method4('SELECT polygon_number, join_seq, joined_join_seq, joined_polygon_number,'||E'\n'||
'       duplicate_join_to_be_removed, reverse_line, reverse_simplified_line, num_points_a, num_points_b,'||E'\n'||
'       simplified_line_test, line_test, simplified_line_length, line_length, line_start_endpoints_match, simplified_line_start_endpoints_match'||E'\n'||
'  FROM simplification_lines_temp'||E'\n'||
' WHERE duplicate_join_to_be_removed = ''N/N'''||E'\n'||
'   AND reverse_line = ''N'''||E'\n'||
'   AND reverse_simplified_line = ''N''', 'Invalid joins and reversals');
/*
Invalid joins and reversals
---------------------------
psql:n_ph_simplification.sql:55: INFO:  rif40_method4(): 
polygon_number       | join_seq             | joined_join_seq      | joined_polygon_number | duplicate_join_to_be_removed | reverse_line | reverse_simplified_line | num_points           | simplified_line_test | line_test | simplified_line_length | line_length         
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
60                   | 371                  | 382                  | 61                    | N/N                          | N            | N                       | 1                    | 1.000                | 1.000     | 0                      | 0                   
61                   | 382                  | 371                  | 60                    | N/N                          | N            | N                       | 1                    | 1.000                | 1.000     | 0                      | 0                   
(2 rows)

polygon_number       | join_seq             | joined_join_seq      | joined_polygon_number | duplicate_join_to_be_removed | reverse_line | reverse_simplified_line | num_points_a         | num_points_b         | simplified_line_test | line_test | simplified_line_length | line_length          | line_start_endpoints_match | simplified_line_start_endpoints_match
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
9                    | 118                  | 2223                 | 193                   | N/N                          | N            | N                       | 149                  | 149                  | 0.002                | 0.001     | 30436.8158167104       | 30529.7292139443     | 0                          | 0                                    
193                  | 2223                 | 118                  | 9                     | N/N                          | N            | N                       | 149                  | 149                  | 0.002                | 0.001     | 30378.9009040035       | 30501.8416425816     | 0                          | 0                                    
(2 rows)

 */
--
-- The usual cause of this is a misordered join
--
-- Check all the co-ordinates are the same
--

			sql_stmt:='WITH z AS ('||E'\n'||
'	SELECT a2.polygon_number, a2.joined_polygon_number, a2.join_seq, a2.joined_join_seq'||E'\n'||
'	  FROM simplification_lines_temp a2'||E'\n'||
'	 WHERE duplicate_join_to_be_removed = ''N/N'''||E'\n'||
'	   AND reverse_line = ''N'''||E'\n'||
'	   AND reverse_simplified_line = ''N'''||E'\n'||
'), a AS ('||E'\n'||
'	SELECT a1.* '||E'\n'||
'          FROM simplification_points a1, z'||E'\n'||
'  	 WHERE a1.join_seq = z.join_seq OR a1.join_seq = z.joined_join_seq'||E'\n'||
'), b AS ('||E'\n'||
'	SELECT a.polygon_number, a.joined_polygon_number, pointflow, coord_id'||E'\n'||
'	  FROM a, z'||E'\n'||
'	 WHERE a.join_seq = z.join_seq'||E'\n'||
'	EXCEPT'||E'\n'||
'	SELECT a.polygon_number, a.joined_polygon_number, pointflow, coord_id'||E'\n'||
'	  FROM a, z'||E'\n'||
'	 WHERE a.join_seq = z.joined_join_seq'||E'\n'||
'), c AS ('||E'\n'||
'	SELECT polygon_number, joined_polygon_number, COUNT(pointflow) AS total'||E'\n'||
'	  FROM b'||E'\n'||
'	 GROUP BY polygon_number, joined_polygon_number'||E'\n'||
')'||E'\n'||
'SELECT SUM(total) AS total'||E'\n'||
'  FROM c';
--
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_simplify_geometry_checks', 'SQL> %;', sql_stmt::VARCHAR);
			OPEN c90_sgck FOR EXECUTE sql_stmt;
			FETCH c90_sgck INTO c90_rec;	
			CLOSE c90_sgck;
--
-- line_test_limit is the maximum difference tolerated between joined lines when all the points are the same (i.e. the is a slight misorder) 
--
			IF (c90_rec.total = 0 OR c90_rec.total IS NULL) AND c9_rec.max_simplified_line_test <= line_test_limit AND c9_rec.max_line_test <= line_test_limit THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_simplify_geometry_checks', 
					'Check %: % geography geolevel %: Found % join sequence(s) causing invalid joins and reversals caused by misorder joins; max line test error: %, max simplified line test error: %; limit: %',
					check_number::VARCHAR				/* Check number */,
					l_geography::VARCHAR				/* Geography */, 
					l_geolevel::VARCHAR				/* Geolevel */,
					c9_rec.t_join_seq::VARCHAR			/* Invalid joins and reversals */,
					c9_rec.max_line_test::VARCHAR			/* Max error in line test */,
					c9_rec.max_simplified_line_test::VARCHAR	/* Max error in simplified line test */,
					line_test_limit::VARCHAR			/* Maximum difference tolerated between joined lines */);
--
-- Co-ordinates are the same, just misordered slightly...
-- 
				PERFORM rif40_sql_pkg.rif40_method4('WITH z AS ('||E'\n'||
'	SELECT a2.join_seq, a2.joined_join_seq'||E'\n'||
'	  FROM simplification_lines_temp a2'||E'\n'||
'	 WHERE duplicate_join_to_be_removed = ''N/N'''||E'\n'||
'	   AND reverse_line = ''N'''||E'\n'||
'	   AND reverse_simplified_line = ''N'' LIMIT 1'||E'\n'||
'), a AS ('||E'\n'||
'	SELECT a1.* '||E'\n'||
'          FROM simplification_points a1, z'||E'\n'||
'  	 WHERE a1.join_seq = z.join_seq OR a1.join_seq = z.joined_join_seq'||E'\n'||
'), b AS ('||E'\n'||
'	SELECT a.pointflow, a.coord_id'||E'\n'||
'	  FROM a, z'||E'\n'||	
'	 WHERE a.join_seq = z.join_seq'||E'\n'||
'), c AS ('||E'\n'||
'	SELECT a.pointflow, a.coord_id'||E'\n'||
'	  FROM a, z'||E'\n'||
'	 WHERE a.join_seq = z.joined_join_seq'||E'\n'||
')'||E'\n'||
'SELECT b.pointflow AS pointflow_join, b.coord_id AS coord_id_join,'||E'\n'||
'       c.pointflow AS pointflow_joined, c.coord_id AS coord_id_joined'||E'\n'||
'  FROM b, c'||E'\n'||
' WHERE b.coord_id = c.coord_id'||E'\n'||
' ORDER BY b.pointflow LIMIT 100', 'First 100 coordinates of first misordered join');
--
			ELSIF (c90_rec.total = 0 OR c90_rec.total IS NULL) AND (c9_rec.max_simplified_line_test > line_test_limit OR c9_rec.max_line_test > line_test_limit) THEN
				PERFORM rif40_log_pkg.rif40_error(-10062, '_simplify_geometry_checks', 
					'Check %: % geography geolevel %: Found % join sequence(s) causing invalid joins and reversals caused by misorder joins; max line test error: %, max simplified line test error: %; limit: %',
					check_number::VARCHAR				/* Check number */,
					l_geography::VARCHAR				/* Geography */, 
					l_geolevel::VARCHAR				/* Geolevel */,
					c9_rec.t_join_seq::VARCHAR			/* Invalid joins and reversals */,
					c9_rec.max_line_test::VARCHAR			/* Max error in line test */,
					c9_rec.max_simplified_line_test::VARCHAR	/* Max error in simplified line test */,
					line_test_limit::VARCHAR			/* Maximum difference tolerated between joined lines */);
--
-- Co-ordinates are the same, misordered too much...
-- 
				PERFORM rif40_sql_pkg.rif40_method4('WITH z AS ('||E'\n'||
'	SELECT a2.join_seq, a2.joined_join_seq'||E'\n'||
'	  FROM simplification_lines_temp a2'||E'\n'||
'	 WHERE duplicate_join_to_be_removed = ''N/N'''||E'\n'||
'	   AND reverse_line = ''N'''||E'\n'||
'	   AND reverse_simplified_line = ''N'' LIMIT 1'||E'\n'||
'), a AS ('||E'\n'||
'	SELECT a1.* '||E'\n'||
'          FROM simplification_points a1, z'||E'\n'||
'  	 WHERE a1.join_seq = z.join_seq OR a1.join_seq = z.joined_join_seq'||E'\n'||
'), b AS ('||E'\n'||
'	SELECT a.pointflow, a.coord_id'||E'\n'||
'	  FROM a, z'||E'\n'||	
'	 WHERE a.join_seq = z.join_seq'||E'\n'||
'), c AS ('||E'\n'||
'	SELECT a.pointflow, a.coord_id'||E'\n'||
'	  FROM a, z'||E'\n'||
'	 WHERE a.join_seq = z.joined_join_seq'||E'\n'||
')'||E'\n'||
'SELECT b.pointflow AS pointflow_join, b.coord_id AS coord_id_join,'||E'\n'||
'       c.pointflow AS pointflow_joined, c.coord_id AS coord_id_joined'||E'\n'||
'  FROM b, c'||E'\n'||
' WHERE b.coord_id = c.coord_id'||E'\n'||
' ORDER BY b.pointflow LIMIT 100', 'First 100 coordinates of first misordered join');
			ELSE
--
-- Co-ordinates are NOT the same
-- 
				PERFORM rif40_sql_pkg.rif40_method4('WITH z AS ('||E'\n'||
'	SELECT a2.polygon_number, a2.joined_polygon_number, a2.join_seq, a2.joined_join_seq'||E'\n'||
'	  FROM simplification_lines_temp a2'||E'\n'||
'	 WHERE duplicate_join_to_be_removed = ''N/N'''||E'\n'||
'	   AND reverse_line = ''N'''||E'\n'||
'	   AND reverse_simplified_line = ''N'''||E'\n'||
'), a AS ('||E'\n'||
'	SELECT a1.* '||E'\n'||
'          FROM simplification_points a1, z'||E'\n'||
'  	 WHERE a1.join_seq = z.join_seq OR a1.join_seq = z.joined_join_seq'||E'\n'||
'), b AS ('||E'\n'||
'	SELECT a.polygon_number, a.joined_polygon_number, pointflow, coord_id'||E'\n'||
'	  FROM a, z'||E'\n'||
'	 WHERE a.join_seq = z.join_seq'||E'\n'||
'	EXCEPT'||E'\n'||
'	SELECT a.polygon_number, a.joined_polygon_number, pointflow, coord_id'||E'\n'||
'	  FROM a, z'||E'\n'||
'	 WHERE a.join_seq = z.joined_join_seq'||E'\n'||
')'||E'\n'||
'SELECT polygon_number, joined_polygon_number, COUNT(pointflow) AS total'||E'\n'||
'  FROM b'||E'\n'||
' GROUP BY polygon_number, joined_polygon_number', 'Coordinate differences between potentially misjoined polygons');
--
				PERFORM rif40_log_pkg.rif40_error(-10082, '_simplify_geometry_checks', 
					'Check %: % geography geolevel %: Found % join sequence(s) causing invalid joins and reversals, % co-ordinates are different; max line test error: %, max simplified line test error: %; limit: %',
					check_number::VARCHAR				/* Check number */,
					l_geography::VARCHAR				/* Geography */, 
					l_geolevel::VARCHAR				/* Geolevel */,
					c9_rec.t_join_seq::VARCHAR			/* Invalid joins and reversals */,
					c90_rec.total::VARCHAR				/* Different co-ordinates */,
					c9_rec.max_line_test::VARCHAR			/* Max error in line test */,
					c9_rec.max_simplified_line_test::VARCHAR	/* Max error in simplified line test */,
					line_test_limit::VARCHAR			/* Maximum difference tolerated between joined lines */);
			END IF;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_checks', 
				'Check %: % geography geolevel %: No join sequence(s) causing invalid joins and reversals',
				check_number::VARCHAR	/* Check number */,
				l_geography::VARCHAR	/* Geography */, 
				l_geolevel::VARCHAR	/* Geolevel */);
		END IF;
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-10083, '_simplify_geometry_checks', 
			'% geography geolevel %: Invalid check number: %',
			l_geography::VARCHAR	/* Geography */, 
			l_geolevel::VARCHAR	/* Geolevel */, 
			check_number::VARCHAR 	/* Check number */); 
	END IF;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg._simplify_geometry_checks(VARCHAR, VARCHAR, INTEGER) IS 'Function: 	_simplify_geometry_checks()
Parameters:	Geography, geolevel, Check number
Returns:	Nothing
Description:	Simplify geography geolevel check functions

Test 0: Geometry is valid
Test 1: Check last_pointflow = one per area (should be zero)
Test 1.1: Check there are 1 or more distinct areas in simplification_points_a (i.e. filter is not broken)
Test 2: for un-detected duplicates
Test 2.1: for invalid duplicates
Test 2.2: Check array dimensions are [1:3] as expected
Test 3: for areas with no join sequence
Test 4: for areas with mis-joined sequences
Test 5: join sequence(s) used by more than one polygon_number
Test 6: polygon_number, coord_id, duplicate(s) used by more than one join sequence
Test 7: areas(s) with non unique join sequences
Test 8: areas(s) with invalid geometry in simplification_polygons
Test 9: join sequence(s) causing invalid joins and reversals';

--
-- Eof