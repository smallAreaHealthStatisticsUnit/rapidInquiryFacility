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
--								  create_rif40_geolevels_geometry_tables() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables(t_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	create_rif40_geolevels_geometry_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Create rif40_geolevels_geometry tables

INSERT INTO t_rif40_sahsu_geometry(
        geography, geolevel_name, area_id, name, gid, area, 
		shapefile_geometry, optimised_geometry, optimised_geojson)
WITH a AS ( /- Aggregate geometries with the same area_id -/
        SELECT 'LEVEL1' AS geolevel_name, level1 AS area_id,         NULL::Text AS name,
             ST_Union(CASE WHEN ST_IsValid(geom) = FALSE THEN ST_MakeValid(geom) 
				ELSE geom END) AS geom /- Make valid if required, Union polygons together -/,
        ROW_NUMBER() OVER (ORDER BY level1) AS gid
          FROM x_sahsu_level1
         GROUP BY level1
        UNION
        SELECT 'LEVEL2' AS geolevel_name, level2 AS area_id,         name AS name,
             ST_Union(CASE WHEN ST_IsValid(geom) = FALSE THEN ST_MakeValid(geom) 
				ELSE geom END) AS geom /- Make valid if required, Union polygons together -/,
        ROW_NUMBER() OVER (ORDER BY level2) AS gid
          FROM x_sahsu_level2
         GROUP BY level2, name
        UNION
        SELECT 'LEVEL3' AS geolevel_name, level3 AS area_id,         NULL::Text AS name,
             ST_Union(CASE WHEN ST_IsValid(geom) = FALSE THEN ST_MakeValid(geom) 
				ELSE geom END) AS geom /- Make valid if required, Union polygons together -/,
        ROW_NUMBER() OVER (ORDER BY level3) AS gid
          FROM x_sahsu_level3
         GROUP BY level3
        UNION
        SELECT 'LEVEL4' AS geolevel_name, level4 AS area_id,         NULL::Text AS name,
             ST_Union(CASE WHEN ST_IsValid(geom) = FALSE THEN ST_MakeValid(geom) 
				ELSE geom END) AS geom /- Make valid if required, Union polygons together -/,
        ROW_NUMBER() OVER (ORDER BY level4) AS gid
          FROM x_sahsu_level4
         GROUP BY level4
), b /- t_rif40_geolevels -/ AS (
        SELECT geography, geolevel_name, st_simplify_tolerance
          FROM t_rif40_geolevels
         WHERE geography = 'SAHSU'
), c /- rif40_geographies -/ AS (
        SELECT max_geojson_digits
          FROM rif40_geographies
         WHERE geography = 'SAHSU'
), d AS (
        SELECT a.geolevel_name, geom, area_id, name, a.gid,
               ST_SimplifyPreserveTopology(geom, b.st_simplify_tolerance) AS simplified_topology
          FROM a, b
         WHERE a.geolevel_name = b.geolevel_name
)
SELECT 'SAHSU' geography, b.geolevel_name, area_id, NULLIF(name, 'Unknown: ['||area_id||']') AS name, d.gid,
        round(CAST(ST_area(geom)/1000000 AS numeric), 1) AS area,
        ST_Multi(geom) AS shapefile_geometry,
        ST_transform(ST_Multi(simplified_topology), 4326 /- WGS 84 -/) AS optimised_geometry,
        ST_AsGeoJson(
                ST_transform(
                        simplified_topology, 4326 /- WGS 84 -/),
                                c.max_geojson_digits, 0 /- no options -/) AS optimised_geojson
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
         FROM t_rif40_sahsu_geometry b
         GROUP BY geolevel_name) AS b1
 WHERE a.geolevel_name = b1.geolevel_name
   AND a.geography     = 'SAHSU';

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
         FROM t_rif40_sahsu_geometry b
         GROUP BY geolevel_name) AS b1
 WHERE a.geolevel_name = b1.geolevel_name
   AND a.geography     = 'SAHSU';
CREATE INDEX t_rif40_sahsu_geo_gin1 ON t_rif40_sahsu_geometry USING GIST(shapefile_geometry);
CREATE INDEX t_rif40_sahsu_geo_gin2 ON t_rif40_sahsu_geometry USING GIST(optimised_geometry);
REINDEX INDEX t_rif40_sahsu_geometry_pk;
REINDEX INDEX t_rif40_sahsu_geometry_uk;
REINDEX INDEX t_rif40_sahsu_geometry_gid;
ANALYZE VERBOSE t_rif40_sahsu_geometry;

 */
DECLARE
 	c2geogeom CURSOR(l_geography VARCHAR) FOR
		SELECT * FROM rif40_geographies
		 WHERE geography = l_geography;
 	c3geogeom CURSOR(l_geography VARCHAR) FOR
		SELECT * FROM t_rif40_geolevels
		 WHERE geography = l_geography;
--
	c2_rec rif40_geographies%ROWTYPE;
	c3_rec t_rif40_geolevels%ROWTYPE;
--
	c3_count INTEGER:=0;
	func_sql VARCHAR;
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'create_rif40_geolevels_geometry_tables', 'User % must be rif40 or have rif_manager role', USER::VARCHAR);
	END IF;
--
	IF t_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10003, 'check_rif40_hierarchy_lookup_tables', 'NULL geography parameter');
	END IF;	
--
	OPEN c2geogeom(t_geography);
	FETCH c2geogeom INTO c2_rec;
	CLOSE c2geogeom;
--
	IF c2_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10004, 'create_rif40_geolevels_geometry_tables', 'geography: % not found', 
			t_geography::VARCHAR	/* Geography */);
	ELSE
--
-- Create a T_RIF40_<GEOGRAPHY>_GEOMETRY base table to inherit from 
--
		sql_stmt:='CREATE TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' ('||E'\n'||
			'geography         CHARACTER VARYING(50)  NOT NULL,'||E'\n'||
			'geolevel_name     CHARACTER VARYING(30)  NOT NULL,'||E'\n'||
			'area_id           CHARACTER VARYING(300) NOT NULL,'||E'\n'||
			'name              CHARACTER VARYING(300) NULL,'||E'\n'||
			'optimised_geojson JSON			          NOT NULL,'||E'\n'||
			'optimised_geojson_1 JSON			      NOT NULL,'||E'\n'||
			'optimised_geojson_2 JSON		          NOT NULL,'||E'\n'||
			'area              NUMERIC(12,2)          NOT NULL,'||E'\n'||
			'total_males	   NUMERIC(12,2)          NULL,'||E'\n'||
			'total_females	   NUMERIC(12,2)          NULL,'||E'\n'||
			'population_year   NUMERIC(12,2)          NULL,'||E'\n'||
			'gid               INTEGER                NOT NULL)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--		
-- Comment base table
--
		sql_stmt:='COMMENT ON TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' IS ''Geolevels geometry: geometry for hierarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels are restricted to LADUA/DISTRICT level resolution or lower. This table contains no data on Oracle. This replaces the shapefiles used in previous RIF releases. Populating this table checks the lookup and hierarchy tables and thus it must be populated last. Any insert into T_RIF40_GEOLEVELS_GEOMETRY must be a single statement insert.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.geography 	IS ''Geography (e.g EW2001)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.geolevel_name 	IS ''Name of geolevel. This will be a column name in the numerator/denominator tables''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.area_id 	IS ''An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.name 	IS ''The name of an area id''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.area 	IS ''The area in square km of an area id''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.total_males 	IS ''Total males''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.total_females 	IS ''Total females''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.population_year 	IS ''Year of population data''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||
			'.optimised_geojson 	IS ''Shapefile multipolygon in GeoJSON format, optimised for zoomlevel 6. '||
			'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output '||
			'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
			'(in metres for most projections) between simplified points. '||
			'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
			'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
			'and not necessarily in the same manner). '||
			'See also TOPO_OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_ChangeEdgeGeometry() and ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||
			'.optimised_geojson_2 	IS ''Shapefile multipolygon in optimised GeoJSON format. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works onj an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). This can be fixed using the PostGIS Topology extension and processing as edges. See also TOPO_OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_ChangeEdgeGeometry() and ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||
			'.optimised_geojson_3 	IS ''Shapefile multipolygon in optimised GeoJSON format. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works onj an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). This can be fixed using the PostGIS Topology extension and processing as edges. See also TOPO_OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_ChangeEdgeGeometry() and ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.gid 	IS ''Geographic ID (artificial primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add a multipolygon columns SHAPEFILE_GEOMETRY and OPTIMISED_GEOMETRY with the SRID for the geography
--
		sql_stmt:='SELECT AddGeometryColumn('||quote_literal('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||', ''shapefile_geometry'', '||
			c2_rec.srid||', ''MULTIPOLYGON'', 2)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' ALTER COLUMN shapefile_geometry SET NOT NULL';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.shapefile_geometry	 IS ''Spatial data for geolevel (PostGress/PostGIS only). Can also use SHAPEFILE instead,''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
		sql_stmt:='SELECT AddGeometryColumn('||quote_literal('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||', ''optimised_geometry'', 4326, ''MULTIPOLYGON'', 2)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' ALTER COLUMN optimised_geometry SET NOT NULL';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.optimised_geometry	 IS ''Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only). Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works onj an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner. This is fixed using the PostGIS Topology extension and processing as edges.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add PK/UK indexes
--
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_uk')||' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'(geography, area_id)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_gid')||' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'(geography, gid)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' ADD CONSTRAINT '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_pk')||' PRIMARY KEY (geography, geolevel_name, area_id)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Base table INSERT fuuntion
--
		func_sql:='CREATE OR REPLACE FUNCTION '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_insert')||'()'||E'\n'||
			'RETURNS TRIGGER AS $func$'||E'\n'||
			'BEGIN'||E'\n';
--
-- For each geolevel with the geography:
--
		c3_count:=0;
		FOR c3_rec IN c3geogeom(c2_rec.geography) LOOP
--
-- Create partitions T_RIF40_GEOLEVELS_GEOMETRY_<GEOGRAPHY>_<GEOELVELS>
--
			c3_count:=c3_count+1;
			sql_stmt:='CREATE TABLE '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||E'\n'||
				'( CHECK (geography = '''||c2_rec.geography||
				''' AND geolevel_name = '''||c3_rec.geolevel_name||''')'||E'\n'||
				') INHERITS (t_rif40_'||LOWER(c2_rec.geography)||'_geometry)';
			IF c3_count = 1 THEN
				func_sql:=func_sql||E'\n'||'	IF NEW.geography = '''||c2_rec.geography||
					''' AND NEW.geolevel_name = '''||c3_rec.geolevel_name||''' THEN'||E'\n'||
					'INSERT INTO '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' VALUES (NEW.*);';
			ELSE
				func_sql:=func_sql||E'\n'||'	ELSIF NEW.geography = '''||c2_rec.geography||
					''' AND NEW.geolevel_name = '''||c3_rec.geolevel_name||''' THEN'||E'\n'||
					'INSERT INTO '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' VALUES (NEW.*);';
			END IF;
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Comment
--
			sql_stmt:='COMMENT ON TABLE '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' 		IS ''Geolevels geometry: geometry for hierarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels are restricted to LADUA/DISTRICT level resolution or lower. This table contains no data on Oracle. This replaces the shapefiles used in previous RIF releases. Populating this table checks the lookup and hierarchy tables and thus it must be populated last. Any insert into T_RIF40_GEOLEVELS_GEOMETRY must be a single statement insert. This is the partition for geogrpahy: '||
				LOWER(c2_rec.geography)||', geo level: '||LOWER(c3_rec.geolevel_name)||'''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.geography 	IS ''Geography (e.g EW2001)''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.geolevel_name 	IS ''Name of geolevel. This will be a column name in the numerator/denominator tables''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.area_id 	IS ''An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.optimised_geojson 	IS ''Shapefile multipolygon in optimised GeoJSON format. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). This can be` fixed using the PostGIS Topology extension and processing as edges. See also TOPO_OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_ChangeEdgeGeometry() and ST_Simplify().''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.area 	IS ''The area in square km of an area id''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.name 	IS ''The name of an area id''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.gid 	IS ''Geographic ID (artificial primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.shapefile_geometry	 IS ''Spatial data for geolevel (PostGress/PostGIS only). Can also use SHAPEFILE instead,''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.optimised_geometry	 IS ''Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only). Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works onj an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner. This is fixed using the PostGIS Topology extension and processing as edges.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.total_males	 IS ''Total males.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.total_females	 IS ''Total females.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.population_year	 IS ''Population year.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END LOOP;
--
-- Create base insert function
--
		func_sql:=func_sql||E'\n'||
'	ELSE'||E'\n'||
'--'||E'\n'||
'-- Eventually this will automatically add a partition'||E'\n'||
'--'||E'\n'||
'		PERFORM rif40_log_pkg.rif40_error(-10002, ''t_rif40_geolevels_geometry_insert'', ''no partition for geography: %, geolevel: %'','||E'\n'||
'			NEW.geography, NEW.geolevel_name);'||E'\n'||
'	END IF;'||E'\n'||
'	RETURN NULL;'||E'\n'||
'END;'||E'\n'||
'$func$'||E'\n'||
'LANGUAGE plpgsql';
		PERFORM rif40_sql_pkg.rif40_ddl(func_sql);
		sql_stmt:='COMMENT ON FUNCTION '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_insert')||'() IS ''Partition INSERT function for geography: '||c2_rec.geography||'''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Trigger
--
		sql_stmt:='CREATE TRIGGER '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_insert')||E'\n'||
			'BEFORE INSERT ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||E'\n'||
			'FOR EACH ROW EXECUTE PROCEDURE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_insert')||'()';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON TRIGGER '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_insert')||E'\n'||E'\t'||'ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' IS ''Partition INSERT trigger for geography: '||c2_rec.geography||'''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);

--
-- Analyze
--

		sql_stmt:='GRANT SELECT,INSERT,UPDATE,DELETE ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' TO rif_manager';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='GRANT SELECT ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' TO PUBLIC';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ANALYZE VERBOSE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry');
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	END IF;
--
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables(VARCHAR) IS 'Function: 	create_rif40_geolevels_geometry_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Crea rif40_geolevels_geometry tables';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	create_rif40_geolevels_geometry_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Create rif40_geolevels_geometry tables

 */
DECLARE
	c2geogeom2 CURSOR FOR
		SELECT * FROM rif40_geographies;
--
	c2_rec rif40_geographies%ROWTYPE;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'create_rif40_geolevels_geometry_tables', 'User % must be rif40 or have rif_manager role', USER::VARCHAR);
	END IF;
--
-- For each geography:
--
	FOR c2_rec IN c2geogeom2 LOOP
		PERFORM rif40_geo_pkg.create_rif40_geolevels_geometry_tables(c2_rec.geography);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables() IS 'Function: 	create_rif40_geolevels_geometry_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Create rif40_geolevels_geometry tables

ADD GENERATED SQL';

--
-- Eof