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
--								  populate_rif40_geometry_tables() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	populate_rif40_geometry_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Populate geographic specific geometry tables

Calls:		rif40_geo_pkg.populate_rif40_geometry_tables(geography);

 */
DECLARE
	c0 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
--
	c0_rec rif40_geographies%ROWTYPE;
	i INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'populate_rif40_geometry_tables', 'User % must be rif40 or have rif_manager role', USER::VARCHAR);
	END IF;
--
	FOR c0_rec IN c0 LOOP
		PERFORM rif40_geo_pkg.populate_rif40_geometry_tables(c0_rec.geography);
		i:=i+1;
	END LOOP;
	IF i = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10014, 'populate_rif40_geometry_tables', 'No geographies set up');
	END IF;
	PERFORM rif40_log_pkg.rif40_log('INFO', 'populate_rif40_geometry_tables', 'All geography geometry tables populated OK');
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables() IS 'Function: 	populate_rif40_geometry_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Populate geographic specific geometry tables

Calls:		rif40_geo_pkg.populate_rif40_geometry_tables(geography);';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables(l_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	populate_rif40_geometry_tables()
Parameters:	Geography 
Returns:	Nothing
Description:	Populate geographic specific geometry tables

INSERT INTO t_rif40_ew01_geometry(
        geography, geolevel_name, area_id, name, gid, area, shapefile_geometry, optimised_geometry, optimised_geojson)
WITH a AS ( -* Aggregate geometries with the same area_id *-
        SELECT 'SCNTRY2001' AS geolevel_name, scntry2001 AS area_id,
         	   name AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY scntry2001) AS gid
          FROM x_ew01_scntry2001
         GROUP BY scntry2001, name
        UNION
        SELECT 'CNTRY2001' AS geolevel_name, cntry2001 AS area_id,
         	   name AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY cntry2001) AS gid
          FROM x_ew01_cntry2001
         GROUP BY cntry2001, name
        UNION
        SELECT 'GOR2001' AS geolevel_name, gor2001 AS area_id,
         	   gorname AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY gor2001) AS gid
          FROM x_ew01_gor2001
         GROUP BY gor2001, gorname
        UNION
        SELECT 'LADUA2001' AS geolevel_name, ladua2001 AS area_id,
         	   laduaname AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY ladua2001) AS gid
          FROM x_ew01_ladua2001
         GROUP BY ladua2001, laduaname
        UNION
        SELECT 'WARD2001' AS geolevel_name, ward2001 AS area_id,
         	   wardname AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY ward2001) AS gid
          FROM x_ew01_ward2001
         GROUP BY ward2001, wardname
        UNION
        SELECT 'SOA2001' AS geolevel_name, soa2001 AS area_id,
         	   NULL AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY soa2001) AS gid
          FROM x_ew01_soa2001
         GROUP BY soa2001
        UNION
        SELECT 'OA2001' AS geolevel_name, coa2001 AS area_id,
         	   NULL AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY coa2001) AS gid
          FROM x_ew01_coa2001
         GROUP BY coa2001
), b -* t_rif40_geolevels *- AS (
        SELECT geography, geolevel_name, st_simplify_tolerance
          FROM t_rif40_geolevels
         WHERE geography = 'EW01'
), c -* rif40_geographies *- AS (
        SELECT max_geojson_digits
          FROM rif40_geographies
         WHERE geography = 'EW01'
), d AS (
        SELECT a.geolevel_name, geom, area_id, name, a.gid,
   		       ST_MakeValid((ST_SimplifyPreserveTopology(geom, b.st_simplify_tolerance)) AS simplified_topology
          FROM a, b
         WHERE a.geolevel_name = b.geolevel_name
)
SELECT 'EW01' geography, b.geolevel_name, area_id, NULLIF(name, 'Unknown: ['||area_id||']') AS name, d.gid,
        round(CAST(ST_area(geom)/1000000 AS numeric), 1) AS area,
        ST_Multi(geom) AS shapefile_geometry, 
        ST_MakeValid(ST_transform(ST_Multi(simplified_topology), 4326 -* WGS 84 *-)) AS optimised_geometry,
	ST_AsGeoJson(
		ST_transform(
        		simplified_topology, 4326 -* WGS 84 *-),
       				c.max_geojson_digits, 0 -* no options *-) AS optimised_geojson
  FROM b, c, d
 WHERE b.geolevel_name = d.geolevel_name
 ORDER BY 1, 2, 3;

UPDATE t_rif40_geolevels a
   SET avg_npoints_geom = b1.avg_npoints_geom,
       avg_npoints_opt  = b1.avg_npoints_opt,
       file_geojson_len = b1.file_geojson_len,
       leg_geom         = b1.leg_geom,
       leg_opt          = b1.leg_opt
  FROM (
        SELECT geolevel_name,
               ROUND(CAST(AVG(ST_NPOINTS(SHAPEFILE_GEOMETRY)) AS numeric), 1) AS avg_npoints_geom,
               ROUND(CAST(AVG(ST_NPoints(optimised_geometry)) AS numeric), 1) AS avg_npoints_opt,
               ROUND(CAST(SUM(length(optimised_geojson)) AS numeric), 1) AS file_geojson_len,
               ROUND(CAST(AVG(ST_perimeter(shapefile_geometry)/ST_NPoints(shapefile_geometry)) AS numeric), 1) AS leg_geom,
               ROUND(CAST(AVG(ST_perimeter(optimised_geometry)/ST_NPoints(optimised_geometry)) AS numeric), 1) AS leg_opt
         FROM t_rif40_ew01_geometry b
         GROUP BY geolevel_name) AS b1
 WHERE a.geolevel_name = b1.geolevel_name
   AND a.geography     = 'EW01';

 */
DECLARE
	c1 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c2 CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geolevel_id;
	c3 REFCURSOR;
--
	c1_rec rif40_geographies%ROWTYPE;
	c2_rec t_rif40_geolevels%ROWTYPE;
	c3_rec RECORD;
--
	sql_stmt VARCHAR;
	i INTEGER:=0;
	insert_rows INTEGER:=0;
	update_rows INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'populate_rif40_geometry_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10005, 'populate_rif40_geometry_tables', 'NULL geography parameter');
	END IF;	
--
	OPEN c1(l_geography);
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10006, 'populate_rif40_geometry_tables', 'geography: % not found', 
			l_geography::VARCHAR	/* Geograpy */);
	END IF;	
--
	sql_stmt:='DELETE FROM '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	sql_stmt:='INSERT INTO '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||'('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'geography, geolevel_name, area_id, name, gid, area, shapefile_geometry, optimised_geometry, optimised_geojson)'||E'\n';	
	sql_stmt:=sql_stmt||'WITH a AS ( /* Aggregate geometries with the same area_id */'||E'\n';
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF i > 1 THEN
			sql_stmt:=sql_stmt||E'\t'||'UNION'||E'\n';
		END IF;
		sql_stmt:=sql_stmt||E'\t'||'SELECT '||quote_literal(UPPER(c2_rec.geolevel_name))||' AS geolevel_name, '||quote_ident(LOWER(c2_rec.shapefile_area_id_column))||' AS area_id, ';
		IF c2_rec.shapefile_desc_column IS NOT NULL THEN
			sql_stmt:=sql_stmt||E'\t'||'     '||quote_ident(LOWER(c2_rec.shapefile_desc_column))||' AS name,'||E'\n';
		ELSE
			sql_stmt:=sql_stmt||E'\t'||'     NULL::Text AS name,'||E'\n';
		END IF;
--
-- ST_MakeValid() replaced - causing corruption in simplification algorithm
--
--		sql_stmt:=sql_stmt||E'\t'||'     ST_MakeValid(ST_Union(geom)) AS geom'||E'\n';
--
-- the other way around could be tried
--
--		sql_stmt:=sql_stmt||E'\t'||'     ST_Union(ST_MakeValid(geom)) AS geom'||E'\n';
--
-- ST_Buffer() replacement for simplification algorithm issue (but removed from simplification as causing issues)
-- now removed as causing geometry errors in intersection code (actually it is the ST_simplify() optimised_geometry
-- causing trouble):
--
-- psql:rif40_geolevels_ew01_geometry.sql:174: ERROR:  Error performing intersection: TopologyException: found non-noded intersection between LINESTRING (-2.9938 53.3669, -2.98342 53.367) and LINESTRING (-2.98556 53.367, -2.98556 53.367) at -2.9855578257498334 53.366966593247653
--
--		sql_stmt:=sql_stmt||E'\t'||'     ST_Union(CASE WHEN ST_IsValid(geom) = FALSE THEN ST_Buffer(geom, 0.0) ELSE geom END) AS geom	/* Make valid if required, Union polygons together */'||E'\n';
--
-- Current favorite. May need to go back to the original below and use ST_Buffer(geom, 0.0) in the initial read into the
-- simplification algoriithm:
--
--		sql_stmt:=sql_stmt||E'\t'||'     ST_Union(geom) AS geom'||E'\n';
--
-- Current version. this is a more sophistication version of: ST_Union(ST_MakeValid(geom))
--
		sql_stmt:=sql_stmt||E'\t'||'     ST_Union(CASE WHEN ST_IsValid(geom) = FALSE THEN ST_MakeValid(geom) ELSE geom END) AS geom	/* Make valid if required, Union polygons together */,'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'ROW_NUMBER() OVER (ORDER BY '||quote_ident(LOWER(c2_rec.shapefile_area_id_column))||') AS gid'||E'\n';
--
		sql_stmt:=sql_stmt||E'\t'||'  FROM '||quote_ident(LOWER(c2_rec.shapefile_table))||E'\n';
		IF c2_rec.shapefile_desc_column IS NOT NULL THEN
			sql_stmt:=sql_stmt||E'\t'||' GROUP BY '||quote_ident(LOWER(c2_rec.shapefile_area_id_column))||', '||quote_ident(LOWER(c2_rec.shapefile_desc_column))||E'\n';
		ELSE
			sql_stmt:=sql_stmt||E'\t'||' GROUP BY '||quote_ident(LOWER(c2_rec.shapefile_area_id_column))||E'\n';
		END IF;
	END LOOP;
	IF i = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10015, 'populate_rif40_geometry_tables', 'No rows found in: t_rif40_geolevels');
	END IF;
	sql_stmt:=sql_stmt||'), b /* t_rif40_geolevels */ AS ('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT geography, geolevel_name, st_simplify_tolerance'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'  FROM t_rif40_geolevels'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' WHERE geography = '||quote_literal(c1_rec.geography)||E'\n';
	sql_stmt:=sql_stmt||'), c /* rif40_geographies */ AS ('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT max_geojson_digits'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'  FROM rif40_geographies'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' WHERE geography = '||quote_literal(c1_rec.geography)||E'\n';
	sql_stmt:=sql_stmt||'), d AS ('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT a.geolevel_name, geom, area_id, name, a.gid,'||E'\n';
--	sql_stmt:=sql_stmt||E'\t'||'       ST_MakeValid(ST_SimplifyPreserveTopology(geom, b.st_simplify_tolerance)) AS simplified_topology'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'       ST_SimplifyPreserveTopology(geom, b.st_simplify_tolerance) AS simplified_topology'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'  FROM a, b'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' WHERE a.geolevel_name = b.geolevel_name'||E'\n';
	sql_stmt:=sql_stmt||')'||E'\n';
	sql_stmt:=sql_stmt||'SELECT '||quote_literal(c1_rec.geography)||
		' geography, b.geolevel_name, area_id, NULLIF(name, ''Unknown: [''||area_id||'']'') AS name, d.gid,'||E'\n';
	sql_stmt:=sql_stmt||'        round(CAST(ST_area(geom)/1000000 AS numeric), 1) AS area,'||E'\n';
	sql_stmt:=sql_stmt||'        ST_Multi(geom) AS shapefile_geometry, '||E'\n';
--	sql_stmt:=sql_stmt||'        ST_MakeValid(ST_transform(ST_Multi(simplified_topology), 4326 /* WGS 84 */)) AS optimised_geometry,'||E'\n';
	sql_stmt:=sql_stmt||'        ST_transform(ST_Multi(simplified_topology), 4326 /* WGS 84 */) AS optimised_geometry,'||E'\n';
	sql_stmt:=sql_stmt||'        ST_AsGeoJson('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||E'\t'||'ST_transform('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'simplified_topology, 4326 /* WGS 84 */),'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||E'\t'||'c.max_geojson_digits, 0 /* no options */) AS optimised_geojson'||E'\n';
	sql_stmt:=sql_stmt||'  FROM b, c, d'||E'\n';
	sql_stmt:=sql_stmt||' WHERE b.geolevel_name = d.geolevel_name'||E'\n';
	sql_stmt:=sql_stmt||' ORDER BY 1, 2, 3';
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'populate_rif40_geometry_tables', 'SQL> %;', sql_stmt::VARCHAR);
	insert_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	sql_stmt:='UPDATE t_rif40_geolevels a'||E'\n';
	sql_stmt:=sql_stmt||'   SET avg_npoints_geom = b1.avg_npoints_geom,'||E'\n';
	sql_stmt:=sql_stmt||'       avg_npoints_opt  = b1.avg_npoints_opt,'||E'\n'; 
	sql_stmt:=sql_stmt||'       file_geojson_len = b1.file_geojson_len,'||E'\n'; 
	sql_stmt:=sql_stmt||'       leg_geom         = b1.leg_geom,'||E'\n'; 
	sql_stmt:=sql_stmt||'       leg_opt          = b1.leg_opt'||E'\n';
	sql_stmt:=sql_stmt||'  FROM ('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT geolevel_name,'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'       ROUND(CAST(AVG(ST_NPOINTS(SHAPEFILE_GEOMETRY)) AS numeric), 1) AS avg_npoints_geom,'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||' 	   ROUND(CAST(AVG(ST_NPoints(optimised_geometry)) AS numeric), 1) AS avg_npoints_opt,'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||' 	   ROUND(CAST(SUM(length(optimised_geojson)) AS numeric), 1) AS file_geojson_len,'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'       ROUND(CAST(AVG(ST_perimeter(shapefile_geometry)/ST_NPoints(shapefile_geometry)) AS numeric), 1) AS leg_geom,'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'       ROUND(CAST(AVG(ST_perimeter(optimised_geometry)/ST_NPoints(optimised_geometry)) AS numeric), 1) AS leg_opt'||E'\n'; 
	sql_stmt:=sql_stmt||E'\t'||' FROM t_rif40_'||quote_ident(LOWER(c1_rec.geography))||'_geometry b'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' GROUP BY geolevel_name) AS b1'||E'\n';
	sql_stmt:=sql_stmt||' WHERE a.geolevel_name = b1.geolevel_name'||E'\n';
	sql_stmt:=sql_stmt||'   AND a.geography     = '||quote_literal(c1_rec.geography);
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'populate_rif40_geometry_tables', 'SQL> %;', sql_stmt::VARCHAR);
	update_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Check rows were inserted
--
	sql_stmt:='SELECT COUNT(*) AS total FROM '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry');
	OPEN c3 FOR EXECUTE sql_stmt;
	FETCH c3 INTO c3_rec;
	CLOSE c3;	
	IF c3_rec.total = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10016, 'populate_rif40_geometry_tables', 'No rows found in: geography: % % geometry table',
			c1_rec.geography::VARCHAR					/* Geography */, 
			't_rif40_'||LOWER(c1_rec.geography)||'_geometry'::VARCHAR	/* Geolevel geometry table */);
	END IF;
--
-- Add GIST indexes. GIN are for vectors only (they are better for static objects and and faster. Will not work for weighted vectors)
--
	sql_stmt:='CREATE INDEX '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geo_gin1')||' ON '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||' USING GIST(shapefile_geometry)';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	sql_stmt:='CREATE INDEX '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geo_gin2')||' ON '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||' USING GIST(optimised_geometry)';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Re-index, re-analyze
--
	sql_stmt:='REINDEX INDEX '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry_pk');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	sql_stmt:='REINDEX INDEX '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry_uk');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	sql_stmt:='REINDEX INDEX '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry_gid');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	sql_stmt:='ANALYZE VERBOSE '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'populate_rif40_geometry_tables', 'Geography: % % geometry table populated with % rows', 
		c1_rec.geography::VARCHAR					/* Geography */, 
		't_rif40_'||LOWER(c1_rec.geography)||'_geometry'::VARCHAR	/* Geolevel geometry table */, 
		insert_rows::VARCHAR						/* Rows inserted */);
--	RAISE EXCEPTION 'Stop';
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables(VARCHAR) IS 'Function: 	populate_rif40_geometry_tables()
Parameters:	Geography 
Returns:	Nothing
Description:	Populate geographic specific geometry tables

INSERT INTO t_rif40_ew01_geometry(
        geography, geolevel_name, area_id, name, gid, area, shapefile_geometry, optimised_geometry, optimised_geojson)
WITH a AS ( /* Aggregate geometries with the same area_id */
        SELECT ''SCNTRY2001'' AS geolevel_name, scntry2001 AS area_id,
         	   name AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY scntry2001) AS gid
          FROM x_ew01_scntry2001
         GROUP BY scntry2001, name
        UNION
        SELECT ''CNTRY2001'' AS geolevel_name, cntry2001 AS area_id,
         	   name AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY cntry2001) AS gid
          FROM x_ew01_cntry2001
         GROUP BY cntry2001, name
        UNION
        SELECT ''GOR2001'' AS geolevel_name, gor2001 AS area_id,
         	   gorname AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY gor2001) AS gid
          FROM x_ew01_gor2001
         GROUP BY gor2001, gorname
        UNION
        SELECT ''LADUA2001'' AS geolevel_name, ladua2001 AS area_id,
         	   laduaname AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY ladua2001) AS gid
          FROM x_ew01_ladua2001
         GROUP BY ladua2001, laduaname
        UNION
        SELECT ''WARD2001'' AS geolevel_name, ward2001 AS area_id,
         	   wardname AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY ward2001) AS gid
          FROM x_ew01_ward2001
         GROUP BY ward2001, wardname
        UNION
        SELECT ''SOA2001'' AS geolevel_name, soa2001 AS area_id,
         	   NULL AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY soa2001) AS gid
          FROM x_ew01_soa2001
         GROUP BY soa2001
        UNION
        SELECT ''OA2001'' AS geolevel_name, coa2001 AS area_id,
         	   NULL AS name, ST_MakeValid(ST_Union(geom)) AS geom, 
			   ROW_NUMBER() OVER (ORDER BY coa2001) AS gid
          FROM x_ew01_coa2001
         GROUP BY coa2001
), b /* t_rif40_geolevels */ AS (
        SELECT geography, geolevel_name, st_simplify_tolerance
          FROM t_rif40_geolevels
         WHERE geography = ''EW01''
), c /* rif40_geographies */ AS (
        SELECT max_geojson_digits
          FROM rif40_geographies
         WHERE geography = ''EW01''
), d AS (
        SELECT a.geolevel_name, geom, area_id, name, a.gid,
		       ST_MakeValid(ST_SimplifyPreserveTopology(geom, b.st_simplify_tolerance)) AS simplified_topology
          FROM a, b
         WHERE a.geolevel_name = b.geolevel_name
)
SELECT ''EW01'' geography, b.geolevel_name, area_id, 
        NULLIF(name, ''Unknown: [''||area_id||'']'') AS name, d.gid,
        round(CAST(ST_area(geom)/1000000 AS numeric), 1) AS area,
        ST_Multi(geom) AS shapefile_geometry, 
        ST_MakeValid(ST_transform(ST_Multi(simplified_topology), 4326 /* WGS 84 */)) AS optimised_geometry,
	ST_AsGeoJson(
		ST_transform(
        		simplified_topology, 4326 /* WGS 84 */),
       				c.max_geojson_digits, 0 /* no options */) AS optimised_geojson
  FROM b, c, d
 WHERE b.geolevel_name = d.geolevel_name
 ORDER BY 1, 2, 3;

UPDATE t_rif40_geolevels a
   SET avg_npoints_geom = b1.avg_npoints_geom,
       avg_npoints_opt  = b1.avg_npoints_opt,
       file_geojson_len = b1.file_geojson_len,
       leg_geom         = b1.leg_geom,
       leg_opt          = b1.leg_opt
  FROM (
        SELECT geolevel_name,
               ROUND(CAST(AVG(ST_NPOINTS(SHAPEFILE_GEOMETRY)) AS numeric), 1) AS avg_npoints_geom,
               ROUND(CAST(AVG(ST_NPoints(optimised_geometry)) AS numeric), 1) AS avg_npoints_opt,
               ROUND(CAST(SUM(length(optimised_geojson)) AS numeric), 1) AS file_geojson_len,
               ROUND(CAST(AVG(ST_perimeter(shapefile_geometry)/ST_NPoints(shapefile_geometry)) AS numeric), 1) AS leg_geom,
               ROUND(CAST(AVG(ST_perimeter(optimised_geometry)/ST_NPoints(optimised_geometry)) AS numeric), 1) AS leg_opt
         FROM t_rif40_ew01_geometry b
         GROUP BY geolevel_name) AS b1
 WHERE a.geolevel_name = b1.geolevel_name
   AND a.geography     = ''EW01'';
';

--
-- Eof