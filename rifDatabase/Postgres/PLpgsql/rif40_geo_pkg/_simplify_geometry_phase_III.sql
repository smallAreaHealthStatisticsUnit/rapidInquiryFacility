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
--								  Phase III: Create the polygons table, Update spatial geolevel table
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
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_III(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_III(VARCHAR, VARCHAR, VARCHAR, NUMERIC);

CREATE OR REPLACE FUNCTION rif40_geo_pkg._simplify_geometry_phase_III(
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
Description:	Simplify geography geolevel - Phase III: Create the polygons table, Update spatial geolevel table

Phase III: Create the polygons table, Update spatial geolevel table

Create temporary polygon table: simplification_polygons
Add geometry columns: geometry, topo_optimised_geometry 
INSERT valid multipolygons INTO simplification_polygons
Display the increase in geoJSON length between the old simplification algorithm and the new
Test 8: areas(s) with invalid geometry in simplification_polygons
Update spatial geolevel table, e.g. p_rif40_geolevels_ward2001 if there is no filter

 */
	c1_sIII	CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c2_sIII CURSOR(l_geography VARCHAR, l_geolevel_name VARCHAR) FOR
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
	l_spatial_geolevel_table	VARCHAR;
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
		PERFORM rif40_log_pkg.rif40_error(-10001, '_simplify_geometry_phase_III', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	stp:=clock_timestamp();
--
	OPEN c1_sIII(l_geography);
	FETCH c1_sIII INTO c1_rec;
	CLOSE c1_sIII;
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10091, '_simplify_geometry_phase_III', 'No geography specified');
	END IF;
	OPEN c2_sIII(l_geography, l_geolevel);
	FETCH c2_sIII INTO c2_rec;
	CLOSE c2_sIII;
	IF c2_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10092, '_simplify_geometry_phase_III', 'No geolevel name specified for geography: %',
			l_geography::VARCHAR);
	END IF;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_phase_III', 'Phase III (polygons creation) for geography: % geolevel: %',
		l_geography::VARCHAR,
		l_geolevel::VARCHAR);
--
-- First block of SQL statements
--

--
-- Create temporary polygon table: simplification_polygons
--
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_polygons CASCADE';
	IF l_filter IS NULL THEN /* Non test mode */
		l_sql_stmt:='CREATE TEMPORARY TABLE simplification_polygons('||E'\n';
	ELSE
		l_sql_stmt:='CREATE TABLE simplification_polygons('||E'\n';
	END IF;
	sql_stmt[array_length(sql_stmt, 1)+1]:=l_sql_stmt||
' area_id        		VARCHAR(300)  	    	NOT NULL,'||E'\n'||
' name	 			VARCHAR(300),'||E'\n'||
' topo_optimised_geojson 	JSON       NOT NULL,'||E'\n'||
' topo_optimised_geojson_2 	JSON       NOT NULL,'||E'\n'||
' topo_optimised_geojson_3 	JSON       NOT NULL,'||E'\n'||
' num_lines     		bigint,'||E'\n'||
' num_points     		bigint,'||E'\n'||
' topo_optimised_num_points    	bigint)';

--
-- Execute first block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add geometry columns: geometry, topo_optimised_geometry 
--
	PERFORM AddGeometryColumn('simplification_polygons', 'geometry', c1_rec.srid, 'MULTIPOLYGON', 2);
    PERFORM AddGeometryColumn('simplification_polygons', 'topo_optimised_geometry', 4326  /* WGS 84 */, 'MULTIPOLYGON', 2);
    PERFORM AddGeometryColumn('simplification_polygons', 'topo_optimised_geometry_2', 4326  /* WGS 84 */, 'MULTIPOLYGON', 2);
    PERFORM AddGeometryColumn('simplification_polygons', 'topo_optimised_geometry_3', 4326  /* WGS 84 */, 'MULTIPOLYGON', 2);

--
-- Second block of SQL statements
--
	sql_stmt:=NULL;	/* Empty statement array */
--
-- INSERT valid multipolygons INTO simplification_polygons
--
-- Note that the SQL has not been split and indexed as the process is linear and does not consume huge amounts of sort
--
	sql_stmt[1]:='EXPLAIN ANALYZE VERBOSE INSERT INTO simplification_polygons'||E'\n'||
' 	(area_id, name, geometry,'||E'\n'||
' 	 topo_optimised_geometry, topo_optimised_geometry_2, topo_optimised_geometry_3,'||E'\n'||
'    topo_optimised_geojson, topo_optimised_geojson_2, topo_optimised_geojson_3,'||E'\n'||
' 	 num_lines, num_points, topo_optimised_num_points)'||E'\n'||
'WITH a /* Join all lines in a polygon */ AS ('||E'\n'||
'	SELECT area_id, polygon_number,'||E'\n'||
'	       ST_Makeline(array_agg(line ORDER BY join_seq)) AS line,'||E'\n'||
'	       ST_Makeline(array_agg(simplified_line ORDER BY join_seq)) AS simplified_line,'||E'\n'||
'	       ST_Makeline(array_agg(simplified_line_2 ORDER BY join_seq)) AS simplified_line_2,'||E'\n'||
'	       ST_Makeline(array_agg(simplified_line_3 ORDER BY join_seq)) AS simplified_line_3,'||E'\n'||
'	       COUNT(num_points) AS num_lines,'||E'\n'||
'	       SUM(num_points) AS num_points'||E'\n'||
'	  FROM simplification_lines'||E'\n'||
'	 GROUP BY area_id, polygon_number'||E'\n'||
'), b /* Close linestrings to create POLYGONs */ AS ('||E'\n'||
'        SELECT area_id, polygon_number,'||E'\n'|| 
'               CASE      /* Close polygon if needed */'||E'\n'|| 
'                         WHEN ST_IsClosed(line) THEN ST_Polygon(line, '||c1_rec.srid||')'||E'\n'|| 
'                         ELSE /* Close LINESTRING */'||E'\n'|| 
'                         	ST_Polygon(ST_AddPoint(line, ST_Startpoint(line)), '||c1_rec.srid||')'||E'\n'|| 
'               END line_polygon,'||E'\n'|| 
'               CASE      /* Close polygon if needed */'||E'\n'|| 
'                         WHEN ST_IsClosed(simplified_line) THEN ST_Polygon(line, '||c1_rec.srid||')'||E'\n'|| 
'                         ELSE /* Close LINESTRING */'||E'\n'|| 
'                         	ST_Polygon(ST_AddPoint(simplified_line, ST_Startpoint(line)),'||E'\n'|| 
'                         		'||c1_rec.srid||')'||E'\n'|| 
'               END simplified_line_polygon,'||E'\n'|| 
'               CASE      /* Close polygon if needed */'||E'\n'|| 
'                         WHEN ST_IsClosed(simplified_line_2) THEN ST_Polygon(line, '||c1_rec.srid||')'||E'\n'|| 
'                         ELSE /* Close LINESTRING */'||E'\n'|| 
'                         	ST_Polygon(ST_AddPoint(simplified_line_2, ST_Startpoint(line)),'||E'\n'|| 
'                         		'||c1_rec.srid||')'||E'\n'|| 
'               END simplified_line_polygon_2,'||E'\n'|| 
'               CASE      /* Close polygon if needed */'||E'\n'|| 
'                         WHEN ST_IsClosed(simplified_line_3) THEN ST_Polygon(line, '||c1_rec.srid||')'||E'\n'|| 
'                         ELSE /* Close LINESTRING */'||E'\n'|| 
'                         	ST_Polygon(ST_AddPoint(simplified_line_3, ST_Startpoint(line)),'||E'\n'|| 
'                         		'||c1_rec.srid||')'||E'\n'|| 
'               END simplified_line_polygon_3,'||E'\n'|| 
'               num_lines, num_points'||E'\n'|| 
'	  FROM a'||E'\n'|| 
'), c /* Create valid MULITPOLYGONs by aggregation to area_id */ AS ('||E'\n'|| 
'	SELECT area_id, SUM(num_lines) AS num_lines, SUM(num_points) AS num_points,'||E'\n'|| 
'              ST_ForceRHR(    	   		  /* Orientate all polygons clockwise */'||E'\n'|| 
'                ST_CollectionExtract(    /* Remove orphaned LINESTRINGs and POINTs */'||E'\n'|| 
'                  ST_Multi(              /* Convert to MULTIPOLYGON */'||E'\n'|| 
'                    ST_Union(            /* Polygons */'||E'\n'|| 
'			     CASE       			  /* Remove self intersections */'||E'\n'||  
'				WHEN ST_IsValid(line_polygon) THEN line_polygon'||E'\n'|| 
'				ELSE                               ST_MakeValid(line_polygon)'||E'\n'|| 
'				 						   /* ST_Buffer(geom, 0.0) removed as was occaisonially causing corruption */'||E'\n'|| 
'			      END)), 3 /* MULTIPOLYGON */)) AS geometry,'||E'\n'|| 
'              ST_transform(              /* Convert to WGS 84 */'||E'\n'|| 
'                ST_ForceRHR(    		  /* Orientate all polygons clockwise */'||E'\n'|| 
'                  ST_CollectionExtract(  /* Remove orphaned LINESTRINGs and POINTs */'||E'\n'|| 
'                    ST_Multi(            /* Convert to MULTIPOLYGON */'||E'\n'|| 
'                      ST_Union(          /* Polygons */'||E'\n'|| 
'			     CASE        			 /* Remove self intersections */'||E'\n'||  
'				WHEN ST_IsValid(simplified_line_polygon) THEN simplified_line_polygon'||E'\n'|| 
'				ELSE                                          ST_MakeValid(simplified_line_polygon)'||E'\n'|| 
'				 					      /* ST_Buffer(geom, 0.0) removed as was occaisonially causing corruption */'||E'\n'|| 
'			      END)), 3 /* MULTIPOLYGON */)),'||E'\n'|| 
'                      	       4326 /* WGS 84 */) AS topo_optimised_geometry,'||E'\n'|| 
'              ST_transform(              /* Convert to WGS 84 */'||E'\n'|| 
'                ST_ForceRHR(    		  /* Orientate all polygons clockwise */'||E'\n'|| 
'                  ST_CollectionExtract(  /* Remove orphaned LINESTRINGs and POINTs */'||E'\n'|| 
'                    ST_Multi(            /* Convert to MULTIPOLYGON */'||E'\n'|| 
'                      ST_Union(          /* Polygons */'||E'\n'|| 
'			     CASE        			 /* Remove self intersections */'||E'\n'||  
'				WHEN ST_IsValid(simplified_line_polygon_2) THEN simplified_line_polygon_2'||E'\n'|| 
'				ELSE                                          ST_MakeValid(simplified_line_polygon_2)'||E'\n'|| 
'				 					      /* ST_Buffer(geom, 0.0) removed as was occaisonially causing corruption */'||E'\n'|| 
'			      END)), 3 /* MULTIPOLYGON */)),'||E'\n'|| 
'                      	       4326 /* WGS 84 */) AS topo_optimised_geometry_2,'||E'\n'|| 
'              ST_transform(              /* Convert to WGS 84 */'||E'\n'|| 
'                ST_ForceRHR(    		  /* Orientate all polygons clockwise */'||E'\n'|| 
'                  ST_CollectionExtract(  /* Remove orphaned LINESTRINGs and POINTs */'||E'\n'|| 
'                    ST_Multi(            /* Convert to MULTIPOLYGON */'||E'\n'|| 
'                      ST_Union(          /* Polygons */'||E'\n'|| 
'			     CASE        			 /* Remove self intersections */'||E'\n'||  
'				WHEN ST_IsValid(simplified_line_polygon_3) THEN simplified_line_polygon_3'||E'\n'|| 
'				ELSE                                          ST_MakeValid(simplified_line_polygon_3)'||E'\n'|| 
'				 					      /* ST_Buffer(geom, 0.0) removed as was occaisonially causing corruption */'||E'\n'|| 
'			      END)), 3 /* MULTIPOLYGON */)),'||E'\n'|| 
'                      	       4326 /* WGS 84 */) AS topo_optimised_geometry_3'||E'\n'|| 
'          FROM b'||E'\n'|| 
'         GROUP BY area_id'||E'\n'|| 
'), d AS /* Forcbily make valid if still not valid */ ('||E'\n'|| 
'	SELECT area_id, num_lines, num_points,'||E'\n'|| 
'   	   CASE WHEN ST_IsValid(geometry) THEN geometry ELSE ST_MakeValid(geometry) END geometry,'||E'\n'|| 
'	       CASE WHEN ST_IsValid(topo_optimised_geometry) THEN topo_optimised_geometry'||E'\n'|| 
'   	   		ELSE ST_MakeValid(topo_optimised_geometry) END topo_optimised_geometry,'||E'\n'|| 
'	       CASE WHEN ST_IsValid(topo_optimised_geometry_2) THEN topo_optimised_geometry_2'||E'\n'|| 
'   	   		ELSE ST_MakeValid(topo_optimised_geometry_2) END topo_optimised_geometry_2,'||E'\n'||
'	       CASE WHEN ST_IsValid(topo_optimised_geometry_3) THEN topo_optimised_geometry_3'||E'\n'|| 
'   	   		ELSE ST_MakeValid(topo_optimised_geometry_3) END topo_optimised_geometry_3'||E'\n'||
'          FROM c'||E'\n'|| 
')'||E'\n'|| 
'SELECT d.area_id, e.name, geometry,'||E'\n'||
'       topo_optimised_geometry,'||E'\n'||
'       topo_optimised_geometry,'||E'\n'||
'       topo_optimised_geometry,'||E'\n'||
'       ST_AsGeoJson(topo_optimised_geometry, '||c1_rec.max_geojson_digits||' /* max_geojson_digits */,'||E'\n'|| 
'			0 /* no options */)::JSON AS topo_optimised_geojson,'||E'\n'||
'       ST_AsGeoJson(topo_optimised_geometry, '||c1_rec.max_geojson_digits||' /* max_geojson_digits */,'||E'\n'|| 
'			0 /* no options */)::JSON AS topo_optimised_geojson_2,'||E'\n'||
'       ST_AsGeoJson(topo_optimised_geometry, '||c1_rec.max_geojson_digits||' /* max_geojson_digits */,'||E'\n'|| 
'			0 /* no options */)::JSON AS topo_optimised_geojson_3,'||E'\n'||
'       num_lines, num_points, ST_NPoints(topo_optimised_geometry) AS topo_optimised_num_points'||E'\n'||
'  FROM d'||E'\n'||
'	LEFT OUTER JOIN '||quote_ident(LOWER(c2_rec.lookup_table))||' e ON (e.'||quote_ident(LOWER(l_geolevel))||' = d.area_id)'||E'\n'||
' ORDER BY d.area_id';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE simplification_polygons ADD PRIMARY KEY(area_id)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE simplification_polygons';
--
-- Execute second block of SQL statements
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	rel_size:=rel_size+rif40_sql_pkg._print_table_size('simplification_polygons');

--
-- Display the increase in geoJSON length between the old simplification algorithm and the new
--
	PERFORM rif40_sql_pkg.rif40_method4('WITH a AS ('||E'\n'||
'	SELECT SUM(LENGTH(topo_optimised_geojson::Text)) AS new_geojson_len'||E'\n'||
'	  FROM simplification_polygons'||E'\n'||
'), b AS ('||E'\n'||
'	SELECT file_geojson_len AS old_geojson_len'||E'\n'||
'	  FROM t_rif40_geolevels'||E'\n'||
'	 WHERE geography = '''||l_geography||''' AND geolevel_name = '''||l_geolevel||''''||E'\n'||
')'||E'\n'||
'SELECT old_geojson_len, new_geojson_len, ROUND((CAST((new_geojson_len-old_geojson_len) AS numeric)/old_geojson_len)*100, 2) AS pct_increase'||E'\n'||
'  FROM a, b', 'Display the increase in geoJSON length between the old simplification algorithm and the new');
/*
old_geojson_len      | new_geojson_len      | pct_increase
-------------------------------------------------------------
16812544             | 30503245             | 81.43       
 */
--
-- Test 8: areas(s) with invalid geometry in simplification_polygons
--
	PERFORM rif40_geo_pkg._simplify_geometry_checks(l_geography, l_geolevel, 8);

--
-- Update spatial geolevel table, e.g. p_rif40_geolevels_geometry_ew01_ward2001 if there is no filter
--
	l_spatial_geolevel_table:='p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(l_geolevel);
	IF l_filter IS NULL THEN /* Non test mode */
		PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_phase_III', 'Simplify % geography geolevel %: Phase III (Create the polygons table) Update spatial geolevel table: %', 
			l_geography::VARCHAR, 
			l_geolevel::VARCHAR,
			l_spatial_geolevel_table::VARCHAR); 
--
-- Do update
--

--
-- Third block of SQL statements
--
		sql_stmt:=NULL;	/* Empty statement array */
		sql_stmt[1]:='EXPLAIN ANALYZE VERBOSE UPDATE rif40_partitions.'||quote_ident(LOWER(l_spatial_geolevel_table))||' a'||E'\n'||
'   SET optimised_geometry = ('||E'\n'||
'	SELECT topo_optimised_geometry'||E'\n'||
'	  FROM simplification_polygons b'||E'\n'||
'	 WHERE a.area_id = b.area_id)';
		sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE UPDATE rif40_partitions.'||quote_ident(LOWER(l_spatial_geolevel_table))||' a'||E'\n'||
'   SET optimised_geometry_2 = ('||E'\n'||
'	SELECT topo_optimised_geometry_2'||E'\n'||
'	  FROM simplification_polygons b'||E'\n'||
'	 WHERE a.area_id = b.area_id)';
		sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE UPDATE rif40_partitions.'||quote_ident(LOWER(l_spatial_geolevel_table))||' a'||E'\n'||
'   SET optimised_geometry_3 = ('||E'\n'||
'	SELECT topo_optimised_geometry_3'||E'\n'||
'	  FROM simplification_polygons b'||E'\n'||
'	 WHERE a.area_id = b.area_id)';
		sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE UPDATE rif40_partitions.'||quote_ident(LOWER(l_spatial_geolevel_table))||' a'||E'\n'||
'   SET optimised_geojson = ('||E'\n'||
'	SELECT topo_optimised_geojson'||E'\n'||
'	  FROM simplification_polygons b'||E'\n'||
'	 WHERE a.area_id = b.area_id)';
		sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE UPDATE rif40_partitions.'||quote_ident(LOWER(l_spatial_geolevel_table))||' a'||E'\n'||
'   SET optimised_geojson_2 = ('||E'\n'||
'	SELECT topo_optimised_geojson_2'||E'\n'||
'	  FROM simplification_polygons b'||E'\n'||
'	 WHERE a.area_id = b.area_id)';
		sql_stmt[array_length(sql_stmt, 1)+1]:='EXPLAIN ANALYZE VERBOSE UPDATE rif40_partitions.'||quote_ident(LOWER(l_spatial_geolevel_table))||' a'||E'\n'||
'   SET optimised_geojson_3 = ('||E'\n'||
'	SELECT topo_optimised_geojson_3'||E'\n'||
'	  FROM simplification_polygons b'||E'\n'||
'	 WHERE a.area_id = b.area_id)';
		sql_stmt[array_length(sql_stmt, 1)+1]:='ANALYZE VERBOSE rif40_partitions.'||quote_ident(LOWER(l_spatial_geolevel_table));
--
-- Execute third block of SQL statements
--
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_simplify_geometry_phase_III', 'Simplify % geography geolevel %: Phase III (Create the polygons table) No update of spatial geolevel table: %, test filter present', 
			l_geography::VARCHAR, 
			l_geolevel::VARCHAR,
			l_spatial_geolevel_table::VARCHAR); 
	END IF;

--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('INFO', '_simplify_geometry_phase_III', 'Simplify % geography geolevel %: Phase III (Create the polygons table) took %s, total space used: %', 
		l_geography::VARCHAR			/* Geography */, 
		l_geolevel::VARCHAR			/* Geolevel */, 
		took::VARCHAR				/* Took */,
		pg_size_pretty(rel_size)::VARCHAR	/* Total size of table and indexes */);
--
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg._simplify_geometry_phase_III(VARCHAR, VARCHAR, VARCHAR, NUMERIC) IS 'Function: 	_simplify_geometry_phase_II()
Parameters:	Geography, geolevel, 
                geolevel; filter (for testing, no default), 
                minimum point resolution (default 1 - assumed metre, but depends on the geometry)
Returns:	Nothing
Description:	Simplify geography geolevel - Phase III: Create the polygons table, Update spatial geolevel table

Phase III: Create the polygons table, Update spatial geolevel table

Create temporary polygon table: simplification_polygons
Add geometry columns: geometry, topo_optimised_geometry 
INSERT valid multipolygons INTO simplification_polygons
Display the increase in geoJSON length between the old simplification algorithm and the new
Test 8: areas(s) with invalid geometry in simplification_polygons
Update spatial geolevel table, e.g. p_rif40_geolevels_ward2001 if there is no filter

Example INSERT statements:

INSERT INTO simplification_polygons
        (area_id, name, geometry, topo_optimised_geometry, topo_optimised_geojson, num_lines, num_points, topo_optimised_num_points)
WITH a /* Join all lines in a polygon */ AS (
        SELECT area_id, polygon_number,
               ST_Makeline(array_agg(line ORDER BY join_seq)) AS line,
               ST_Makeline(array_agg(simplified_line ORDER BY join_seq)) AS simplified_line,
               COUNT(num_points) AS num_lines,
               SUM(num_points) AS num_points
          FROM simplification_lines
         GROUP BY area_id, polygon_number
), b /* Close linestrings to create POLYGONs */ AS (
        SELECT area_id, polygon_number,
               CASE      /* Close polygon if needed */
                         WHEN ST_IsClosed(line) THEN ST_Polygon(line, 27700)
                         ELSE /* Close LINESTRING */ ST_Polygon(ST_AddPoint(line, ST_Startpoint(line)), 27700)
               END line_polygon,
               CASE      /* Close polygon if needed */
                         WHEN ST_IsClosed(simplified_line) THEN ST_Polygon(line, 27700)
                         ELSE /* Close LINESTRING */ ST_Polygon(ST_AddPoint(simplified_line, ST_Startpoint(line)), 27700)
               END simplified_line_polygon,
               num_lines, num_points
          FROM a
), c /* Create valid MULITPOLYGONs by aggregation to area_id */ AS (
        SELECT area_id, SUM(num_lines) AS num_lines, SUM(num_points) AS num_points,
               ST_ForceRHR(               /* Orientate all polygons clockwise */
                 ST_CollectionExtract(    /* Remove orphaned LINESTRINGs and POINTs */
                   ST_Multi(              /* Convert to MULTIPOLYGON */
                     ST_Union(            /* Polygons */
                             CASE         /* Remove self intersections */
                                WHEN ST_IsValid(line_polygon) THEN line_polygon
                                ELSE                               ST_MakeValid(line_polygon)
                              END)), 3 /* MULTIPOLYGON */)) AS geometry,
              ST_transform(               /* Convert to WGS 84 */
                 ST_ForceRHR(             /* Orientate all polygons clockwise */
                  ST_CollectionExtract(   /* Remove orphaned LINESTRINGs and POINTs */
                     ST_Multi(            /* Convert to MULTIPOLYGON */
                       ST_Union(          /* Polygons */
                               CASE       /* Remove self intersections */
                                        WHEN ST_IsValid(simplified_line_polygon) THEN simplified_line_polygon
                                        ELSE                                          ST_MakeValid(simplified_line_polygon)
                               END)), 3 /* MULTIPOLYGON */)), 4326 /* WGS 84 */) AS topo_optimised_geometry
          FROM b
         GROUP BY area_id
), d AS /* Forcbily make valid if still not valid */ (
        SELECT area_id, num_lines, num_points,
              CASE WHEN ST_IsValid(geometry) THEN geometry ELSE ST_MakeValid(geometry) END geometry,
              CASE WHEN ST_IsValid(topo_optimised_geometry) THEN topo_optimised_geometry ELSE ST_MakeValid(topo_optimised_geometry) END topo_optimised_geometry
          FROM c
)
SELECT d.area_id, e.name, geometry, topo_optimised_geometry,
       ST_AsGeoJson(topo_optimised_geometry, 8 /* max_geojson_digits */,
                0 /* no options */) AS topo_optimised_geojson,
       num_lines, num_points, ST_NPoints(topo_optimised_geometry) AS topo_optimised_num_points
  FROM d
        LEFT OUTER JOIN sahsuland_level4 e ON (e.level4 = d.area_id)
 ORDER BY d.area_id;

UPDATE p_rif40_geolevels_geometry_sahsu_level4 a
   SET optimised_geojson = (
        SELECT topo_optimised_geojson
          FROM simplification_polygons b
         WHERE a.area_id = b.area_id);

';

--
-- Eof