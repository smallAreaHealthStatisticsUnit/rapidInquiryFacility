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
--								  Create t_rif40_<geography>_geometry and 
--							      t_rif40_<geography>_maptiles tables
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
Description:	Create t_rif40_<geography>_geometry and t_rif40_<geography>_maptiles tables
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
--
	zoomlevel INTEGER;
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
			'optimised_geojson_2 JSON			      NOT NULL,'||E'\n'||
			'optimised_geojson_3 JSON		          NOT NULL,'||E'\n'||
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
			'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
			'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
			'(in metres for most projections) between simplified points. '||
			'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
			'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
			'and not necessarily in the same manner). '||
			'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
			'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||
			'.optimised_geojson_2 	IS ''Shapefile multipolygon in optimised GeoJSON format, optimised for zoomlevel 8. '||
			'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
			'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
			'(in metres for most projections) between simplified points. '||
			'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
			'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
			'and not necessarily in the same manner). '||
			'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
			'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||
			'.optimised_geojson_3 	IS ''Shapefile multipolygon in optimised GeoJSON format, optimised for zoomlevel 11. '||
			'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
			'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
			'(in metres for most projections) between simplified points. '||
			'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
			'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
			'and not necessarily in the same manner). '||
			'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
			'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.gid 	IS ''Geographic ID (artificial primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add a multipolygon columns SHAPEFILE_GEOMETRY and OPTIMISED_GEOMETRY with the SRID for the geography
--
		sql_stmt:='SELECT AddGeometryColumn('||quote_literal('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||', ''shapefile_geometry'', '||
			c2_rec.srid||', ''MULTIPOLYGON'', 2)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
		sql_stmt:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' ALTER COLUMN shapefile_geometry SET NOT NULL';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.shapefile_geometry	 IS ''Spatial data for geolevel (PostGress/PostGIS only). Can also use SHAPEFILE instead,''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
		sql_stmt:='SELECT AddGeometryColumn('||quote_literal('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||', ''optimised_geometry'', 4326, ''MULTIPOLYGON'', 2)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' ALTER COLUMN optimised_geometry SET NOT NULL';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.optimised_geometry	 IS '||
				'''Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 6. '||
				'Can also use SHAPEFILE instead. '||
				'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and '||
				'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance '||
				'(in metres for most projections) between simplified points. '||
				'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
				'(it works on an object by object basis; the edge between two areas will therefore be processed '||
				'independently and not necessarily in the same manner. '||
				'This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		
		sql_stmt:='SELECT AddGeometryColumn('||quote_literal('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||', ''optimised_geometry_2'', 4326, ''MULTIPOLYGON'', 2)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' ALTER COLUMN optimised_geometry_2 SET NOT NULL';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.optimised_geometry_2	 IS '||
				'''Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 8. '||
				'Can also use SHAPEFILE instead. '||
				'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and '||
				'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance '||
				'(in metres for most projections) between simplified points. '||
				'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
				'(it works on an object by object basis; the edge between two areas will therefore be processed '||
				'independently and not necessarily in the same manner. '||
				'This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		
		sql_stmt:='SELECT AddGeometryColumn('||quote_literal('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||', ''optimised_geometry_3'', 4326, ''MULTIPOLYGON'', 2)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||' ALTER COLUMN optimised_geometry_3 SET NOT NULL';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.optimised_geometry_3	 IS '||
				'''Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 11. '||
				'Can also use SHAPEFILE instead. '||
				'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and '||
				'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance '||
				'(in metres for most projections) between simplified points. '||
				'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
				'(it works on an object by object basis; the edge between two areas will therefore be processed '||
				'independently and not necessarily in the same manner. '||
				'This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add PK/UK indexes
--
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_uk')||' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'(geography, area_id)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_gid')||' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'(geography, gid)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||
			' ADD CONSTRAINT '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_pk')||' PRIMARY KEY (geography, geolevel_name, area_id)';
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
-- Create partitions P_RIF40_GEOLEVELS_GEOMETRY_<GEOGRAPHY>_<GEOELVELS>
--
			c3_count:=c3_count+1;
			sql_stmt:='CREATE TABLE '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||E'\n'||
				'( CHECK (geography = '''||c2_rec.geography||
				''' AND geolevel_name = '''||c3_rec.geolevel_name||''')'||E'\n'||
				') INHERITS (t_rif40_'||LOWER(c2_rec.geography)||'_geometry)';
			IF c3_count = 1 THEN
				func_sql:=func_sql||E'\n'||'	IF NEW.geography = '''||c2_rec.geography||
					''' AND NEW.geolevel_name = '''||c3_rec.geolevel_name||''' THEN'||E'\n'||
					'INSERT INTO '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' VALUES (NEW.*);';
			ELSE
				func_sql:=func_sql||E'\n'||'	ELSIF NEW.geography = '''||c2_rec.geography||
					''' AND NEW.geolevel_name = '''||c3_rec.geolevel_name||''' THEN'||E'\n'||
					'INSERT INTO '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' VALUES (NEW.*);';
			END IF;
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Comment
--
			sql_stmt:='COMMENT ON TABLE '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' 		IS ''Geolevels geometry: geometry for hierarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels are restricted to LADUA/DISTRICT level resolution or lower. This table contains no data on Oracle. This replaces the shapefiles used in previous RIF releases. Populating this table checks the lookup and hierarchy tables and thus it must be populated last. Any insert into T_RIF40_GEOLEVELS_GEOMETRY must be a single statement insert. This is the partition for geogrpahy: '||
				LOWER(c2_rec.geography)||', geo level: '||LOWER(c3_rec.geolevel_name)||'''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.geography 	IS ''Geography (e.g EW2001)''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.geolevel_name 	IS ''Name of geolevel. This will be a column name in the numerator/denominator tables''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.area_id 	IS ''An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.optimised_geojson 	IS '||
				'''Shapefile multipolygon in GeoJSON format, optimised for zoomlevel 6. '||
				'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
				'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
				'(in metres for most projections) between simplified points. '||
				'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
				'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
				'and not necessarily in the same manner). '||
				'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
				'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';			
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.optimised_geojson_2 	IS '||
				'''Shapefile multipolygon in GeoJSON format, optimised for zoomlevel 8. '||
				'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
				'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
				'(in metres for most projections) between simplified points. '||
				'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
				'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
				'and not necessarily in the same manner). '||
				'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
				'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';			
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.optimised_geojson_3 	IS '||
				'''Shapefile multipolygon in GeoJSON format, optimised for zoomlevel 11. '||
				'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
				'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
				'(in metres for most projections) between simplified points. '||
				'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
				'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
				'and not necessarily in the same manner). '||
				'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
				'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';			
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.area 	IS ''The area in square km of an area id''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.name 	IS ''The name of an area id''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.gid 	IS ''Geographic ID (artificial primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.shapefile_geometry	 IS ''Spatial data for geolevel (PostGress/PostGIS only). Can also use SHAPEFILE instead,''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.optimised_geometry	 IS '||
				'''Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 6. '||
				'Can also use SHAPEFILE instead. '||
				'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and '||
				'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance '||
				'(in metres for most projections) between simplified points. '||
				'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
				'(it works on an object by object basis; the edge between two areas will therefore be processed '||
				'independently and not necessarily in the same manner. '||
				'This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.optimised_geometry_2	 IS '||
				'''Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 8. '||
				'Can also use SHAPEFILE instead. '||
				'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and '||
				'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance '||
				'(in metres for most projections) between simplified points. '||
				'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
				'(it works on an object by object basis; the edge between two areas will therefore be processed '||
				'independently and not necessarily in the same manner. '||
				'This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.optimised_geometry_3	 IS '||
				'''Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 11. '||
				'Can also use SHAPEFILE instead. '||
				'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and '||
				'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance '||
				'(in metres for most projections) between simplified points. '||
				'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
				'(it works on an object by object basis; the edge between two areas will therefore be processed '||
				'independently and not necessarily in the same manner. '||
				'This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.total_males	 IS ''Total males.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.total_females	 IS ''Total females.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'.population_year	 IS ''Population year.''';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add PK/UK indexes
--
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_uk')||
				' ON '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'(geography, area_id)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_gid')||
				' ON '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||'(geography, gid)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			
			sql_stmt:='ALTER TABLE '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||
				' ADD CONSTRAINT '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||LOWER(c3_rec.geolevel_name)||'_geometry_pk')||
				' PRIMARY KEY (geography, geolevel_name, area_id)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add GIST/GIN indexes
--
			sql_stmt:='CREATE INDEX '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_sgin1')||
				' ON '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' USING GIST(shapefile_geometry)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);

			sql_stmt:='CREATE INDEX '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_ogin1')||
				' ON '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' USING GIST(optimised_geometry)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE INDEX '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_ogin2')||
				' ON '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' USING GIST(optimised_geometry_2)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE INDEX '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_ogin3')||
				' ON '||quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name))||' USING GIST(optimised_geometry_3)';
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
'		PERFORM rif40_log_pkg.rif40_error(-10002, ''p_rif40_geolevels_geometry_insert'', ''no partition for geography: %, geolevel: %'','||E'\n'||
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
-- Create a T_RIF40_<GEOGRAPHY>_MAPTILES base table to inherit from 
--
		sql_stmt:='CREATE TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||' ('||E'\n'||
			'geography         CHARACTER VARYING(50)  NOT NULL,'||E'\n'||
			'geolevel_name     CHARACTER VARYING(30)  NOT NULL,'||E'\n'||
			'tile_id           CHARACTER VARYING(300) NOT NULL,'||E'\n'||
			'x_tile_number     INTEGER NOT NULL,'||E'\n'||
			'y_tile_number     INTEGER NOT NULL,'||E'\n'||
			'zoomlevel		   INTEGER CHECK(zoomlevel BETWEEN 0 AND 11) NOT NULL,'||E'\n'||
			'optimised_geojson JSON			          NOT NULL,'||E'\n'||
			'optimised_topojson JSON			      NOT NULL,'||E'\n'||
			'gid               INTEGER                NOT NULL)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--		
-- Comment base table
--
		sql_stmt:='COMMENT ON TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			' IS ''Maptiles table for geography; separate partions per geolevel and zoomlevel. Use this table for INSERT/UPDATE/DELETE''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.geography 	IS ''Geography (e.g EW2001)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.geolevel_name 	IS ''Name of geolevel. This will be a column name in the numerator/denominator tables''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);		
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.x_tile_number IS ''X tile number. From 0 to (2**<zoomlevel>)-1''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);		
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.y_tile_number IS ''Y tile number. From 0 to (2**<zoomlevel>)-1''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);		
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.zoomlevel 	IS ''Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.tile_id 	IS ''Tile ID in the format <geography>_<geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.optimised_geojson 	IS ''Shapefile multipolygon in GeoJSON format, optimised for zoomlevel N. '||
			'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
			'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
			'(in metres for most projections) between simplified points. '||
			'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
			'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
			'and not necessarily in the same manner). '||
			'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
			'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.optimised_topojson 	IS ''Shapefile multipolygon in TopoJSON format, optimised for zoomlevel N. '||
			'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
			'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
			'(in metres for most projections) between simplified points. '||
			'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
			'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
			'and not necessarily in the same manner). '||
			'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
			'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||'.gid 	IS ''Geographic ID (artificial primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add PK/UK indexes
--
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_uk')||
			' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||'(geography, tile_id)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_gid')||
			' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||'(geography, zoomlevel, gid)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_x_tile')||
			' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||'(geolevel_name, zoomlevel, x_tile_number)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_y_tile')||
			' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||'(geolevel_name, zoomlevel, y_tile_number)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt); 
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_xy_tile')||
			' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||'(geolevel_name, zoomlevel, x_tile_number, y_tile_number)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt); 
		sql_stmt:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			' ADD CONSTRAINT '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_pk')||
			' PRIMARY KEY (geography, geolevel_name, tile_id)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt); 
--
-- Base table INSERT function
--
		func_sql:='CREATE OR REPLACE FUNCTION '||
			quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_insert')||'()'||E'\n'||
			'RETURNS TRIGGER AS $func$'||E'\n'||
			'BEGIN'||E'\n';
--
-- For each geolevel with the geography:
--
		c3_count:=0;
		FOR c3_rec IN c3geogeom(c2_rec.geography) LOOP
--
-- Create partitions P_RIF40_GEOLEVELS_MAPTILES_<GEOGRAPHY>_<GEOELVEL>_zoom_<ZOOMLEVEL>
--
			FOR zoomlevel IN 0 .. 11 LOOP
				c3_count:=c3_count+1;
				sql_stmt:='CREATE TABLE '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||E'\n'||
					'( CHECK (geography = '''||c2_rec.geography||''''||E'\n'||
					'     AND zoomlevel = '||zoomlevel::Text||E'\n'||
					'     AND geolevel_name = '''||c3_rec.geolevel_name||''')'||E'\n'||
					') INHERITS (t_rif40_'||LOWER(c2_rec.geography)||'_maptiles)';
				IF c3_count = 1 THEN
					func_sql:=func_sql||E'\n'||'	IF NEW.geography = '''||c2_rec.geography||
						''' AND NEW.geolevel_name = '''||c3_rec.geolevel_name||''''||E'\n'||
						'   AND NEW.zoomlevel     = '||zoomlevel::Text||' THEN'||E'\n'||
						'INSERT INTO '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||' VALUES (NEW.*);';
				ELSE
					func_sql:=func_sql||E'\n'||'	ELSIF NEW.geography = '''||c2_rec.geography||
						''' AND NEW.geolevel_name = '''||c3_rec.geolevel_name||''''||E'\n'||
						'   AND NEW.zoomlevel     = '||zoomlevel::Text||' THEN'||E'\n'||
						'INSERT INTO '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||' VALUES (NEW.*);';
				END IF;
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--		
-- Comment partition table
--
				sql_stmt:='COMMENT ON TABLE '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||
					' IS ''Maptiles table for geography; separate partions per geolevel and zoomlevel. Partition for geolevel: '||c3_rec.geolevel_name||
					'; zoomlevel: '||zoomlevel::Text||'. Use this table for INSERT/UPDATE/DELETE''';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
				sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||
					'.geography 	IS ''Geography (e.g EW2001)''';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
				sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||
					'.geolevel_name 	IS ''Name of geolevel. This will be a column name in the numerator/denominator tables''';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);		
				sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||
					'.x_tile_number IS ''X tile number. From 0 to (2**<zoomlevel>)-1''';					
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);		
				sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||	
					'.y_tile_number IS ''Y tile number. From 0 to (2**<zoomlevel>)-1''';					
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);		
				sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||
					'.zoomlevel 	IS ''Zoom level: '||zoomlevel::Text||'. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.''';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
				sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||
					'.tile_id 	IS ''Tile ID in the format: <geography>_<geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>''';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
				sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||
					'.optimised_geojson 	IS ''Shapefile multipolygon in GeoJSON format, optimised for zoomlevel N. '||
					'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
					'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
					'(in metres for most projections) between simplified points. '||
					'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
					'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
					'and not necessarily in the same manner). '||
					'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
					'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
				sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||
					'.optimised_topojson 	IS ''Shapefile multipolygon in TopoJSON format, optimised for zoomlevel N. '||
					'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
					'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
					'(in metres for most projections) between simplified points. '||
					'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
					'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
					'and not necessarily in the same manner). '||	
					'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
					'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
				sql_stmt:='COMMENT ON COLUMN '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||'.gid 	IS ''Geographic ID (artificial primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)''';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add PK/UK indexes
--
				sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text||'_uk')||
					' ON '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||'(geography, tile_id)';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
				sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text||'_gid')||
					' ON '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||'(geography, zoomlevel, gid)';
				sql_stmt:='CREATE INDEX '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text||'_x_tile')||
					' ON '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||'(x_tile_number)';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
				sql_stmt:='CREATE INDEX '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text||'_y_tile')||
					' ON '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||'(y_tile_number)';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
				sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text||'_xy_tile')||
					' ON '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||'(x_tile_number, y_tile_number)';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
				sql_stmt:='ALTER TABLE '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text)||
					' ADD CONSTRAINT '||quote_ident('p_rif40_geolevels_maptiles_'||
					LOWER(c2_rec.geography)||'_'||LOWER(c3_rec.geolevel_name)||'_zoom_'||zoomlevel::Text||'_pk')||
					' PRIMARY KEY (geography, geolevel_name, tile_id)';
				PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			END LOOP;
		END LOOP;
--
-- Create base insert function
--
		func_sql:=func_sql||E'\n'||
'	ELSE'||E'\n'||
'--'||E'\n'||
'-- Eventually this will automatically add a partition'||E'\n'||
'--'||E'\n'||
'		PERFORM rif40_log_pkg.rif40_error(-10003, ''p_rif40_geolevels_maptiles_insert'', ''no partition for maptiles geography: %, geolevel: %'','||E'\n'||
'			NEW.geography, NEW.geolevel_name);'||E'\n'||
'	END IF;'||E'\n'||
'	RETURN NULL;'||E'\n'||
'END;'||E'\n'||
'$func$'||E'\n'||
'LANGUAGE plpgsql';
		PERFORM rif40_sql_pkg.rif40_ddl(func_sql);
		sql_stmt:='COMMENT ON FUNCTION '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_insert')||
			'() IS ''Partition INSERT function for maptile geography: '||c2_rec.geography||'''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Trigger
--
		sql_stmt:='CREATE TRIGGER '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_insert')||E'\n'||
			'BEFORE INSERT ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||E'\n'||
			'FOR EACH ROW EXECUTE PROCEDURE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_insert')||'()';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON TRIGGER '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles_insert')||E'\n'||E'\t'||
			'ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			' IS ''Partition INSERT trigger for maptiles geography: '||c2_rec.geography||'''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);

--
-- Analyze
--
		sql_stmt:='GRANT SELECT,INSERT,UPDATE,DELETE ON '||
			quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||' TO rif_manager';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='GRANT SELECT ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||' TO PUBLIC';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ANALYZE VERBOSE '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles');
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- View
--
		sql_stmt:='CREATE OR REPLACE VIEW '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||E'\n'||
'AS'||E'\n'||
'WITH a AS ( /* Get max geolevels for geography */'||E'\n'||
'	SELECT geography, MAX(geolevel_id) AS max_geolevel_id'||E'\n'||
'	  FROM rif40_geolevels'||E'\n'|| 
'	 WHERE geography = '''||c2_rec.geography||''''||E'\n'||
'	 GROUP BY geography'||E'\n'||
'), b AS ( /* Generate geolevels IDs */'||E'\n'||
'	SELECT a.geography,'||E'\n'|| 
'	       generate_series(1, a.max_geolevel_id, 1) AS geolevel_id'||E'\n'||
'	  FROM a'||E'\n'||
'), c AS ( /* Geolevel ID to names (hard coded) */'||E'\n'||
'	SELECT CASE'||E'\n'; 
		FOR c3_rec IN c3geogeom(c2_rec.geography) LOOP
			sql_stmt:=sql_stmt||
'				WHEN b.geolevel_id = '||c3_rec.geolevel_id||' THEN '''||c3_rec.geolevel_name||''''||E'\n';
		END LOOP;
		sql_stmt:=sql_stmt||
'				ELSE NULL'||E'\n'||
'	   END AS geolevel_name, b.geolevel_id, b.geography'||E'\n'||
'	  FROM b'||E'\n'||
'), d AS ( /* Zoomlevel generator */'||E'\n'||
'	SELECT generate_series(0, 11, 1) AS zoomlevel'||E'\n'||
'), ex AS ( /* X tile numnbers */'||E'\n'||
'	SELECT d.zoomlevel, generate_series(0, POWER(2, d.zoomlevel)::INTEGER-1, 1) AS xy_series'||E'\n'||
'	  FROM d'||E'\n'||
'), ey AS ( /* Y tile numbers, geolevels */'||E'\n'||
'	SELECT c.geolevel_name, c.geolevel_id, c.geography, ex.zoomlevel, ex.xy_series'||E'\n'||
'	  FROM c, ex'||E'\n'||
')'||E'\n'||
'SELECT z.geography,'||E'\n'||
'      z.geolevel_name,'||E'\n'||
'      CASE WHEN h.tile_id IS NULL THEN 1 ELSE 0 END no_area_ids,'||E'\n'||
'      COALESCE(h.tile_id, /* Generate tile_id for tile with no area_id */'||E'\n'||
'			z.geography||''_''||'||E'\n'||
'			z.geolevel_id||''_''||'||E'\n'||
'			z.geolevel_name||''_''||'||E'\n'||			
'			z.zoomlevel||''_''||'||E'\n'||
'			z.x_tile_number||''_''||'||E'\n'||	
'			z.y_tile_number	'||E'\n'||			
'			) AS tile_id, '||E'\n'||
'       z.x_tile_number, z.y_tile_number, z.zoomlevel, '||E'\n'||
'	   COALESCE(h.optimised_topojson, ''{"type": "FeatureCollection","features":[]}''::JSON /* Null featureset */) AS optimised_topojson, '||E'\n'||
'	   COALESCE(h.optimised_geojson, ''{"type": "FeatureCollection","features":[]}''::JSON /* Null featureset */) AS optimised_geojson'||E'\n'||
'  FROM ( /* Use sub query so optimise can unnest; a CTE with cause all tile X Y numbers to be generated */'||E'\n'||
'	SELECT ey.geolevel_name, ey.geolevel_id, ey.geography, ex.zoomlevel, ex.xy_series AS x_tile_number, ey.xy_series AS y_tile_number'||E'\n'||
'	  FROM ey, ex'||E'\n'||
'    WHERE ex.zoomlevel  = ey.zoomlevel) z'||E'\n'||
'		LEFT OUTER JOIN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_maptiles')||' h /* For tiles with area IDs */'||E'\n'||
'			ON (z.zoomlevel     = h.zoomlevel AND '||E'\n'||
'			    z.x_tile_number = h.x_tile_number AND '||E'\n'||
'			    z.y_tile_number = h.y_tile_number AND '||E'\n'||
'			    z.geolevel_name = h.geolevel_name)';

		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON VIEW '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			' IS ''Maptiles view for geography; empty tiles are added to complete zoomlevels for zoomlevels 0 to 11. This view is efficent!''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.geography 	IS ''Geography (e.g EW2001)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.geolevel_name 	IS ''Name of geolevel. This will be a column name in the numerator/denominator tables''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.no_area_ids IS ''Tile contains no area_ids flag: 0/1''';			
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='COMMENT ON COLUMN '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.x_tile_number IS ''X tile number. From 0 to (2**<zoomlevel>)-1''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);		
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.y_tile_number IS ''Y tile number. From 0 to (2**<zoomlevel>)-1''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);		
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.tile_id 	IS ''Tile ID in the format <geography>_<geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);		
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.zoomlevel 	IS ''Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.optimised_geojson 	IS ''Shapefile multipolygon in GeoJSON format, optimised for zoomlevel N. '||
			'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
			'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
			'(in metres for most projections) between simplified points. '||
			'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
			'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
			'and not necessarily in the same manner). '||
			'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
			'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||
			'.optimised_topojson 	IS ''Shapefile multipolygon in TopoJSON format, optimised for zoomlevel N. '||
			'RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. '||
			'RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. '||
			'(in metres for most projections) between simplified points. '||
			'Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm '||
			'(it works on an object by object basis; the edge between two areas will therefore be processed independently '||
			'and not necessarily in the same manner). '||
			'Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GEOJSON; '||
			'i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
		sql_stmt:='GRANT SELECT ON '||quote_ident('rif40_'||LOWER(c2_rec.geography)||'_maptiles')||' TO PUBLIC';
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
Description:	Create t_rif40_<geography>_geometry and t_rif40_<geography>_maptiles tables';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	create_rif40_geolevels_geometry_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Create t_rif40_<geography>_geometry and t_rif40_<geography>_maptiles tables

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
Description:	Create t_rif40_<geography>_geometry and t_rif40_<geography>_maptiles tables

COMMENT ON TABLE t_rif40_sahsu_geometry IS ''Geolevels geometry: geometry for hierarchy of level with a ge
ography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels are restricted to LADUA/DIST
RICT level resolution or lower. This table contains no data on Oracle. This replaces the shapefiles used in previous RIF releases. Populating this table checks the lookup and hiera
rchy tables and thus it must be populated last. Any insert into T_RIF40_GEOLEVELS_GEOMETRY must be a single statement insert.'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.geography   IS ''Geography (e.g EW2001)'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.geolevel_name       IS ''Name of geolevel. This will be a column
name in the numerator/denominator tables'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.area_id     IS ''An area id, the value of a geolevel; i.e. the va
lue of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.name        IS ''The name of an area id'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.area        IS ''The area in square km of an area id'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.total_males         IS ''Total males'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.total_females       IS ''Total females'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.population_year     IS ''Year of population data'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.optimised_geojson   IS ''Shapefile multipolygon in GeoJSON format
, optimised for zoomlevel 6. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. (i
n metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by objec
t basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GE
OJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.optimised_geojson_2         IS ''Shapefile multipolygon in optimi
sed GeoJSON format, optimised for zoomlevel 8. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is
no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on
an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replac
ed by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.optimised_geojson_3         IS ''Shapefile multipolygon in optimi
sed GeoJSON format, optimised for zoomlevel 11. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is
 no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on
 an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are repla
ced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN t_rif40_sahsu_geometry.gid         IS ''Geographic ID (artificial primary key originally
 created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
SELECT AddGeometryColumn(''t_rif40_sahsu_geometry'', ''shapefile_geometry'', 27700, ''MULTIPOLYGON'', 2);
ALTER TABLE t_rif40_sahsu_geometry ALTER COLUMN shapefile_geometry SET NOT NULL;
COMMENT ON COLUMN t_rif40_sahsu_geometry.shapefile_geometry   IS ''Spatial data for geolevel (PostGress/Po
stGIS only). Can also use SHAPEFILE instead,'';
SELECT AddGeometryColumn(''t_rif40_sahsu_geometry'', ''optimised_geometry'', 4326, ''MULTIPOLYGON'', 2);
ALTER TABLE t_rif40_sahsu_geometry ALTER COLUMN optimised_geometry SET NOT NULL;
COMMENT ON COLUMN t_rif40_sahsu_geometry.optimised_geometry   IS ''Optimised spatial data for geolevel in
SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 6. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the G
eoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small slivers and o
verlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed independently and not nece
ssarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
SELECT AddGeometryColumn(''t_rif40_sahsu_geometry'', ''optimised_geometry_2'', 4326, ''MULTIPOLYGON'', 2);
ALTER TABLE t_rif40_sahsu_geometry ALTER COLUMN optimised_geometry_2 SET NOT NULL;
COMMENT ON COLUMN t_rif40_sahsu_geometry.optimised_geometry_2         IS ''Optimised spatial data for geol
evel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 8. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits
in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small slive
rs and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed independently and
not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
SELECT AddGeometryColumn(''t_rif40_sahsu_geometry'', ''optimised_geometry_3'', 4326, ''MULTIPOLYGON'', 2);
ALTER TABLE t_rif40_sahsu_geometry ALTER COLUMN optimised_geometry_3 SET NOT NULL;
COMMENT ON COLUMN t_rif40_sahsu_geometry.optimised_geometry_3         IS ''Optimised spatial data for geol
evel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 11. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits
 in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small sliv
ers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed independently and
 not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
CREATE UNIQUE INDEX t_rif40_sahsu_geometry_uk ON t_rif40_sahsu_geometry(geography, area_id);
CREATE UNIQUE INDEX t_rif40_sahsu_geometry_gid ON t_rif40_sahsu_geometry(geography, gid);
ALTER TABLE t_rif40_sahsu_geometry ADD CONSTRAINT t_rif40_sahsu_geometry_pk PRIMARY KEY (geography, geole
vel_name, area_id);
CREATE TABLE p_rif40_geolevels_geometry_sahsu_level2
( CHECK (geography = ''SAHSU'' AND geolevel_name = ''LEVEL2'')
) INHERITS (t_rif40_sahsu_geometry);
COMMENT ON TABLE p_rif40_geolevels_geometry_sahsu_level2             IS ''Geolevels geometry: geometry for
 hierarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels
 are restricted to LADUA/DISTRICT level resolution or lower. This table contains no data on Oracle. This replaces the shapefiles used in previous RIF releases. Populating this tabl
e checks the lookup and hierarchy tables and thus it must be populated last. Any insert into T_RIF40_GEOLEVELS_GEOMETRY must be a single statement insert. This is the partition for
 geogrpahy: sahsu, geo level: level2'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.geography  IS ''Geography (e.g EW2001)'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.geolevel_name      IS ''Name of geolevel. This w
ill be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.area_id    IS ''An area id, the value of a geole
vel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.optimised_geojson  IS ''Shapefile multipolygon i
n GeoJSON format, optimised for zoomlevel 6. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no
 longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an
 object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replaced
 by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.optimised_geojson_2        IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel 8. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.optimised_geojson_3        IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel 11. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.area       IS ''The area in square km of an area
 id'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.name       IS ''The name of an area id'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.gid        IS ''Geographic ID (artificial primar
y key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.shapefile_geometry  IS ''Spatial data for geolev
el (PostGress/PostGIS only). Can also use SHAPEFILE instead,'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.optimised_geometry  IS ''Optimised spatial data
for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 6. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of
 digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain sma
ll slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed independen
tly and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.optimised_geometry_2        IS ''Optimised spati
al data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 8. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the n
umber of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will con
tain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed in
dependently and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.optimised_geometry_3        IS ''Optimised spati
al data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 11. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the
number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will co
ntain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed i
ndependently and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.total_males         IS ''Total males.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.total_females       IS ''Total females.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level2.population_year     IS ''Population year.'';
CREATE UNIQUE INDEX p_rif40_geolevels_geometry_sahsu_level2_uk ON p_rif40_geolevels_geometry_sahsu_level2
(geography, area_id);
CREATE UNIQUE INDEX p_rif40_geolevels_geometry_sahsu_level2_gid ON p_rif40_geolevels_geometry_sahsu_level
2(geography, gid);
ALTER TABLE p_rif40_geolevels_geometry_sahsu_level2 ADD CONSTRAINT p_rif40_geolevels_geometry_sahsulevel2
_geometry_pk PRIMARY KEY (geography, geolevel_name, area_id);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level2_sgin1 ON p_rif40_geolevels_geometry_sahsu_level2 USI
NG GIST(shapefile_geometry);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level2_ogin1 ON p_rif40_geolevels_geometry_sahsu_level2 USI
NG GIST(optimised_geometry);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level2_ogin2 ON p_rif40_geolevels_geometry_sahsu_level2 USI
NG GIST(optimised_geometry_2);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level2_ogin3 ON p_rif40_geolevels_geometry_sahsu_level2 USI
NG GIST(optimised_geometry_3);
CREATE TABLE p_rif40_geolevels_geometry_sahsu_level1
( CHECK (geography = ''SAHSU'' AND geolevel_name = ''LEVEL1'')
) INHERITS (t_rif40_sahsu_geometry);
COMMENT ON TABLE p_rif40_geolevels_geometry_sahsu_level1             IS ''Geolevels geometry: geometry for
 hierarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels
 are restricted to LADUA/DISTRICT level resolution or lower. This table contains no data on Oracle. This replaces the shapefiles used in previous RIF releases. Populating this tabl
e checks the lookup and hierarchy tables and thus it must be populated last. Any insert into T_RIF40_GEOLEVELS_GEOMETRY must be a single statement insert. This is the partition for
 geogrpahy: sahsu, geo level: level1'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.geography  IS ''Geography (e.g EW2001)'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.geolevel_name      IS ''Name of geolevel. This w
ill be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.area_id    IS ''An area id, the value of a geole
vel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.optimised_geojson  IS ''Shapefile multipolygon i
n GeoJSON format, optimised for zoomlevel 6. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no
 longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an
 object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replaced
 by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.optimised_geojson_2        IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel 8. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.optimised_geojson_3        IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel 11. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.area       IS ''The area in square km of an area
 id'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.name       IS ''The name of an area id'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.gid        IS ''Geographic ID (artificial primar
y key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.shapefile_geometry  IS ''Spatial data for geolev
el (PostGress/PostGIS only). Can also use SHAPEFILE instead,'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.optimised_geometry  IS ''Optimised spatial data
for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 6. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of
 digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain sma
ll slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed independen
tly and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.optimised_geometry_2        IS ''Optimised spati
al data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 8. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the n
umber of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will con
tain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed in
dependently and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.optimised_geometry_3        IS ''Optimised spati
al data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 11. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the
number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will co
ntain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed i
ndependently and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.total_males         IS ''Total males.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.total_females       IS ''Total females.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level1.population_year     IS ''Population year.'';
CREATE UNIQUE INDEX p_rif40_geolevels_geometry_sahsu_level1_uk ON p_rif40_geolevels_geometry_sahsu_level1
(geography, area_id);
CREATE UNIQUE INDEX p_rif40_geolevels_geometry_sahsu_level1_gid ON p_rif40_geolevels_geometry_sahsu_level
1(geography, gid);
ALTER TABLE p_rif40_geolevels_geometry_sahsu_level1 ADD CONSTRAINT p_rif40_geolevels_geometry_sahsulevel1
_geometry_pk PRIMARY KEY (geography, geolevel_name, area_id);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level1_sgin1 ON p_rif40_geolevels_geometry_sahsu_level1 USI
NG GIST(shapefile_geometry);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level1_ogin1 ON p_rif40_geolevels_geometry_sahsu_level1 USI
NG GIST(optimised_geometry);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level1_ogin2 ON p_rif40_geolevels_geometry_sahsu_level1 USI
NG GIST(optimised_geometry_2);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level1_ogin3 ON p_rif40_geolevels_geometry_sahsu_level1 USI
NG GIST(optimised_geometry_3);
CREATE TABLE p_rif40_geolevels_geometry_sahsu_level3
( CHECK (geography = ''SAHSU'' AND geolevel_name = ''LEVEL3'')
) INHERITS (t_rif40_sahsu_geometry);
COMMENT ON TABLE p_rif40_geolevels_geometry_sahsu_level3             IS ''Geolevels geometry: geometry for
 hierarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels
 are restricted to LADUA/DISTRICT level resolution or lower. This table contains no data on Oracle. This replaces the shapefiles used in previous RIF releases. Populating this tabl
e checks the lookup and hierarchy tables and thus it must be populated last. Any insert into T_RIF40_GEOLEVELS_GEOMETRY must be a single statement insert. This is the partition for
 geogrpahy: sahsu, geo level: level3'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.geography  IS ''Geography (e.g EW2001)'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.geolevel_name      IS ''Name of geolevel. This w
ill be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.area_id    IS ''An area id, the value of a geole
vel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.optimised_geojson  IS ''Shapefile multipolygon i
n GeoJSON format, optimised for zoomlevel 6. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no
 longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an
 object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replaced
 by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.optimised_geojson_2        IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel 8. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.optimised_geojson_3        IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel 11. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.area       IS ''The area in square km of an area
 id'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.name       IS ''The name of an area id'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.gid        IS ''Geographic ID (artificial primar
y key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.shapefile_geometry  IS ''Spatial data for geolev
el (PostGress/PostGIS only). Can also use SHAPEFILE instead,'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.optimised_geometry  IS ''Optimised spatial data
for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 6. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of
 digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain sma
ll slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed independen
tly and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.optimised_geometry_2        IS ''Optimised spati
al data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 8. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the n
umber of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will con
tain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed in
dependently and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.optimised_geometry_3        IS ''Optimised spati
al data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 11. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the
number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will co
ntain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed i
ndependently and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.total_males         IS ''Total males.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.total_females       IS ''Total females.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level3.population_year     IS ''Population year.'';
CREATE UNIQUE INDEX p_rif40_geolevels_geometry_sahsu_level3_uk ON p_rif40_geolevels_geometry_sahsu_level3
(geography, area_id);
CREATE UNIQUE INDEX p_rif40_geolevels_geometry_sahsu_level3_gid ON p_rif40_geolevels_geometry_sahsu_level
3(geography, gid);
ALTER TABLE p_rif40_geolevels_geometry_sahsu_level3 ADD CONSTRAINT p_rif40_geolevels_geometry_sahsulevel3
_geometry_pk PRIMARY KEY (geography, geolevel_name, area_id);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level3_sgin1 ON p_rif40_geolevels_geometry_sahsu_level3 USI
NG GIST(shapefile_geometry);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level3_ogin1 ON p_rif40_geolevels_geometry_sahsu_level3 USI
NG GIST(optimised_geometry);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level3_ogin2 ON p_rif40_geolevels_geometry_sahsu_level3 USI
NG GIST(optimised_geometry_2);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level3_ogin3 ON p_rif40_geolevels_geometry_sahsu_level3 USI
NG GIST(optimised_geometry_3);
CREATE TABLE p_rif40_geolevels_geometry_sahsu_level4
( CHECK (geography = ''SAHSU'' AND geolevel_name = ''LEVEL4'')
) INHERITS (t_rif40_sahsu_geometry);
COMMENT ON TABLE p_rif40_geolevels_geometry_sahsu_level4             IS ''Geolevels geometry: geometry for
 hierarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels
 are restricted to LADUA/DISTRICT level resolution or lower. This table contains no data on Oracle. This replaces the shapefiles used in previous RIF releases. Populating this tabl
e checks the lookup and hierarchy tables and thus it must be populated last. Any insert into T_RIF40_GEOLEVELS_GEOMETRY must be a single statement insert. This is the partition for
 geogrpahy: sahsu, geo level: level4'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.geography  IS ''Geography (e.g EW2001)'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.geolevel_name      IS ''Name of geolevel. This w
ill be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.area_id    IS ''An area id, the value of a geole
vel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.optimised_geojson  IS ''Shapefile multipolygon i
n GeoJSON format, optimised for zoomlevel 6. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no
 longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an
 object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replaced
 by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.optimised_geojson_2        IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel 8. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.optimised_geojson_3        IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel 11. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.area       IS ''The area in square km of an area
 id'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.name       IS ''The name of an area id'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.gid        IS ''Geographic ID (artificial primar
y key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.shapefile_geometry  IS ''Spatial data for geolev
el (PostGress/PostGIS only). Can also use SHAPEFILE instead,'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.optimised_geometry  IS ''Optimised spatial data
for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 6. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of
 digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain sma
ll slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed independen
tly and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.optimised_geometry_2        IS ''Optimised spati
al data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 8. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the n
umber of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will con
tain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed in
dependently and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.optimised_geometry_3        IS ''Optimised spati
al data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only), optimised for zoomlevel 11. Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the
number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will co
ntain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by object basis; the edge between two areas will therefore be processed i
ndependently and not necessarily in the same manner. This is fixed using the simplifaction package rif40_geo_pkg.simplify_geometry() function and processing as edges.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.total_males         IS ''Total males.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.total_females       IS ''Total females.'';
COMMENT ON COLUMN p_rif40_geolevels_geometry_sahsu_level4.population_year     IS ''Population year.'';
CREATE UNIQUE INDEX p_rif40_geolevels_geometry_sahsu_level4_uk ON p_rif40_geolevels_geometry_sahsu_level4
(geography, area_id);
CREATE UNIQUE INDEX p_rif40_geolevels_geometry_sahsu_level4_gid ON p_rif40_geolevels_geometry_sahsu_level
4(geography, gid);
ALTER TABLE p_rif40_geolevels_geometry_sahsu_level4 ADD CONSTRAINT p_rif40_geolevels_geometry_sahsulevel4
_geometry_pk PRIMARY KEY (geography, geolevel_name, area_id);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level4_sgin1 ON p_rif40_geolevels_geometry_sahsu_level4 USI
NG GIST(shapefile_geometry);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level4_ogin1 ON p_rif40_geolevels_geometry_sahsu_level4 USI
NG GIST(optimised_geometry);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level4_ogin2 ON p_rif40_geolevels_geometry_sahsu_level4 USI
NG GIST(optimised_geometry_2);
CREATE INDEX p_rif40_geolevels_geometry_sahsu_level4_ogin3 ON p_rif40_geolevels_geometry_sahsu_level4 USI
NG GIST(optimised_geometry_3);
CREATE OR REPLACE FUNCTION t_rif40_sahsu_geometry_insert()
RETURNS TRIGGER AS $func$
BEGIN

        IF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL2'' THEN
INSERT INTO p_rif40_geolevels_geometry_sahsu_level2 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL1'' THEN
INSERT INTO p_rif40_geolevels_geometry_sahsu_level1 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL3'' THEN
INSERT INTO p_rif40_geolevels_geometry_sahsu_level3 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL4'' THEN
INSERT INTO p_rif40_geolevels_geometry_sahsu_level4 VALUES (NEW.*);
        ELSE
--
-- Eventually this will automatically add a partition
--
                PERFORM rif40_log_pkg.rif40_error(-10002, ''p_rif40_geolevels_geometry_insert'', ''no partition for geography: %, geolevel: %'',
                        NEW.geography, NEW.geolevel_name);
        END IF;
        RETURN NULL;
END;
$func$
LANGUAGE plpgsql;
COMMENT ON FUNCTION t_rif40_sahsu_geometry_insert() IS ''Partition INSERT function for geography: SAHSU'';
CREATE TRIGGER t_rif40_sahsu_geometry_insert
BEFORE INSERT ON t_rif40_sahsu_geometry
FOR EACH ROW EXECUTE PROCEDURE t_rif40_sahsu_geometry_insert();
COMMENT ON TRIGGER t_rif40_sahsu_geometry_insert
        ON t_rif40_sahsu_geometry IS ''Partition INSERT trigger for geography: SAHSU'';
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_sahsu_geometry TO rif_manager;
GRANT SELECT ON t_rif40_sahsu_geometry TO PUBLIC;
ANALYZE VERBOSE t_rif40_sahsu_geometry;
CREATE TABLE t_rif40_sahsu_maptiles (
geography         CHARACTER VARYING(50)  NOT NULL,
geolevel_name     CHARACTER VARYING(30)  NOT NULL,
tile_id           CHARACTER VARYING(300) NOT NULL,
zoomlevel                  INTEGER CHECK(zoomlevel IN (6, 8, 11)) NOT NULL,
optimised_geojson JSON                            NOT NULL,
optimised_topojson JSON                       NOT NULL,
gid               INTEGER                NOT NULL);
COMMENT ON TABLE t_rif40_sahsu_maptiles IS ''Geolevels geometry: maptiles for hierarchy of level with a ge
ography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN t_rif40_sahsu_maptiles.geography   IS ''Geography (e.g EW2001)'';
COMMENT ON COLUMN t_rif40_sahsu_maptiles.geolevel_name       IS ''Name of geolevel. This will be a column
name in the numerator/denominator tables'';
COMMENT ON COLUMN t_rif40_sahsu_maptiles.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN t_rif40_sahsu_maptiles.zoomlevel   IS ''Zoom level: 6, 8 or 11'';
COMMENT ON COLUMN t_rif40_sahsu_maptiles.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN t_rif40_sahsu_maptiles.optimised_geojson   IS ''Shapefile multipolygon in GeoJSON format
, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. (i
n metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by objec
t basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GE
OJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN t_rif40_sahsu_maptiles.optimised_topojson  IS ''Shapefile multipolygon in TopoJSON forma
t, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. (
in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by obje
ct basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_G
EOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN t_rif40_sahsu_maptiles.gid         IS ''Geographic ID (artificial primary key originally
 created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX t_rif40_sahsu_maptiles_uk ON t_rif40_sahsu_maptiles(geography, tile_id);
CREATE UNIQUE INDEX t_rif40_sahsu_maptiles_gid ON t_rif40_sahsu_maptiles(geography, gid);
ALTER TABLE t_rif40_sahsu_maptiles ADD CONSTRAINT t_rif40_sahsu_maptiles_pk PRIMARY KEY (geography, geole
vel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level2_zoom_6
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 6
     AND geolevel_name = ''LEVEL2'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level2_zoom_6 IS ''Geolevels geometry: maptiles for hier
archy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_6.geography   IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_6.geolevel_name       IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_6.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_6.zoomlevel   IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_6.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_6.optimised_geojson   IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_6.optimised_topojson  IS ''Shapefile multip
olygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_6.gid         IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level2_zoom_6_uk ON p_rif40_geolevels_maptiles_sahsu
_level2_zoom_6(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level2_zoom_6_gid ON p_rif40_geolevels_maptiles_sahs
u_level2_zoom_6(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level2_zoom_6 ADD CONSTRAINT p_rif40_geolevels_maptiles_sahs
u_level2_zoom_6_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level2_zoom_8
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 8
     AND geolevel_name = ''LEVEL2'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level2_zoom_8 IS ''Geolevels geometry: maptiles for hier
archy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_8.geography   IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_8.geolevel_name       IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_8.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_8.zoomlevel   IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_8.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_8.optimised_geojson   IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_8.optimised_topojson  IS ''Shapefile multip
olygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_8.gid         IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level2_zoom_8_uk ON p_rif40_geolevels_maptiles_sahsu
_level2_zoom_8(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level2_zoom_8_gid ON p_rif40_geolevels_maptiles_sahs
u_level2_zoom_8(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level2_zoom_8 ADD CONSTRAINT p_rif40_geolevels_maptiles_sahs
u_level2_zoom_8_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level2_zoom_11
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 11
     AND geolevel_name = ''LEVEL2'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level2_zoom_11 IS ''Geolevels geometry: maptiles for hie
rarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_11.geography  IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_11.geolevel_name      IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_11.tile_id    IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_11.zoomlevel  IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_11.tile_id    IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_11.optimised_geojson  IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_11.optimised_topojson         IS ''Shapefil
e multipolygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIF
Y_TOLERANCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorith
m (it works on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEO
JSON are replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level2_zoom_11.gid        IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level2_zoom_11_uk ON p_rif40_geolevels_maptiles_sahs
u_level2_zoom_11(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level2_zoom_11_gid ON p_rif40_geolevels_maptiles_sah
su_level2_zoom_11(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level2_zoom_11 ADD CONSTRAINT p_rif40_geolevels_maptiles_sah
su_level2_zoom_11_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level1_zoom_6
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 6
     AND geolevel_name = ''LEVEL1'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level1_zoom_6 IS ''Geolevels geometry: maptiles for hier
archy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_6.geography   IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_6.geolevel_name       IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_6.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_6.zoomlevel   IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_6.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_6.optimised_geojson   IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_6.optimised_topojson  IS ''Shapefile multip
olygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_6.gid         IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level1_zoom_6_uk ON p_rif40_geolevels_maptiles_sahsu
_level1_zoom_6(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level1_zoom_6_gid ON p_rif40_geolevels_maptiles_sahs
u_level1_zoom_6(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level1_zoom_6 ADD CONSTRAINT p_rif40_geolevels_maptiles_sahs
u_level1_zoom_6_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level1_zoom_8
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 8
     AND geolevel_name = ''LEVEL1'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level1_zoom_8 IS ''Geolevels geometry: maptiles for hier
archy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_8.geography   IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_8.geolevel_name       IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_8.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_8.zoomlevel   IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_8.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_8.optimised_geojson   IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_8.optimised_topojson  IS ''Shapefile multip
olygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_8.gid         IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level1_zoom_8_uk ON p_rif40_geolevels_maptiles_sahsu
_level1_zoom_8(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level1_zoom_8_gid ON p_rif40_geolevels_maptiles_sahs
u_level1_zoom_8(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level1_zoom_8 ADD CONSTRAINT p_rif40_geolevels_maptiles_sahs
u_level1_zoom_8_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level1_zoom_11
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 11
     AND geolevel_name = ''LEVEL1'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level1_zoom_11 IS ''Geolevels geometry: maptiles for hie
rarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_11.geography  IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_11.geolevel_name      IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_11.tile_id    IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_11.zoomlevel  IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_11.tile_id    IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_11.optimised_geojson  IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_11.optimised_topojson         IS ''Shapefil
e multipolygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIF
Y_TOLERANCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorith
m (it works on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEO
JSON are replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level1_zoom_11.gid        IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level1_zoom_11_uk ON p_rif40_geolevels_maptiles_sahs
u_level1_zoom_11(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level1_zoom_11_gid ON p_rif40_geolevels_maptiles_sah
su_level1_zoom_11(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level1_zoom_11 ADD CONSTRAINT p_rif40_geolevels_maptiles_sah
su_level1_zoom_11_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level3_zoom_6
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 6
     AND geolevel_name = ''LEVEL3'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level3_zoom_6 IS ''Geolevels geometry: maptiles for hier
archy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_6.geography   IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_6.geolevel_name       IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_6.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_6.zoomlevel   IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_6.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_6.optimised_geojson   IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_6.optimised_topojson  IS ''Shapefile multip
olygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_6.gid         IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level3_zoom_6_uk ON p_rif40_geolevels_maptiles_sahsu
_level3_zoom_6(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level3_zoom_6_gid ON p_rif40_geolevels_maptiles_sahs
u_level3_zoom_6(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level3_zoom_6 ADD CONSTRAINT p_rif40_geolevels_maptiles_sahs
u_level3_zoom_6_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level3_zoom_8
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 8
     AND geolevel_name = ''LEVEL3'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level3_zoom_8 IS ''Geolevels geometry: maptiles for hier
archy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_8.geography   IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_8.geolevel_name       IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_8.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_8.zoomlevel   IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_8.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_8.optimised_geojson   IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_8.optimised_topojson  IS ''Shapefile multip
olygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_8.gid         IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level3_zoom_8_uk ON p_rif40_geolevels_maptiles_sahsu
_level3_zoom_8(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level3_zoom_8_gid ON p_rif40_geolevels_maptiles_sahs
u_level3_zoom_8(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level3_zoom_8 ADD CONSTRAINT p_rif40_geolevels_maptiles_sahs
u_level3_zoom_8_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level3_zoom_11
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 11
     AND geolevel_name = ''LEVEL3'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level3_zoom_11 IS ''Geolevels geometry: maptiles for hie
rarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_11.geography  IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_11.geolevel_name      IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_11.tile_id    IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_11.zoomlevel  IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_11.tile_id    IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_11.optimised_geojson  IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_11.optimised_topojson         IS ''Shapefil
e multipolygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIF
Y_TOLERANCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorith
m (it works on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEO
JSON are replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level3_zoom_11.gid        IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level3_zoom_11_uk ON p_rif40_geolevels_maptiles_sahs
u_level3_zoom_11(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level3_zoom_11_gid ON p_rif40_geolevels_maptiles_sah
su_level3_zoom_11(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level3_zoom_11 ADD CONSTRAINT p_rif40_geolevels_maptiles_sah
su_level3_zoom_11_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level4_zoom_6
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 6
     AND geolevel_name = ''LEVEL4'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level4_zoom_6 IS ''Geolevels geometry: maptiles for hier
archy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_6.geography   IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_6.geolevel_name       IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_6.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_6.zoomlevel   IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_6.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_6.optimised_geojson   IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_6.optimised_topojson  IS ''Shapefile multip
olygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_6.gid         IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level4_zoom_6_uk ON p_rif40_geolevels_maptiles_sahsu
_level4_zoom_6(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level4_zoom_6_gid ON p_rif40_geolevels_maptiles_sahs
u_level4_zoom_6(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level4_zoom_6 ADD CONSTRAINT p_rif40_geolevels_maptiles_sahs
u_level4_zoom_6_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level4_zoom_8
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 8
     AND geolevel_name = ''LEVEL4'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level4_zoom_8 IS ''Geolevels geometry: maptiles for hier
archy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_8.geography   IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_8.geolevel_name       IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_8.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_8.zoomlevel   IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_8.tile_id     IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_8.optimised_geojson   IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_8.optimised_topojson  IS ''Shapefile multip
olygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERA
NCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wo
rks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
 replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_8.gid         IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level4_zoom_8_uk ON p_rif40_geolevels_maptiles_sahsu
_level4_zoom_8(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level4_zoom_8_gid ON p_rif40_geolevels_maptiles_sahs
u_level4_zoom_8(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level4_zoom_8 ADD CONSTRAINT p_rif40_geolevels_maptiles_sahs
u_level4_zoom_8_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE TABLE p_rif40_geolevels_maptiles_sahsu_level4_zoom_11
( CHECK (geography = ''SAHSU''
     AND zoomlevel = 11
     AND geolevel_name = ''LEVEL4'')
) INHERITS (t_rif40_sahsu_maptiles);
COMMENT ON TABLE p_rif40_geolevels_maptiles_sahsu_level4_zoom_11 IS ''Geolevels geometry: maptiles for hie
rarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_11.geography  IS ''Geography (e.g EW2001)'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_11.geolevel_name      IS ''Name of geolevel
. This will be a column name in the numerator/denominator tables'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_11.tile_id    IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_11.zoomlevel  IS ''Zoom level; 6, 8 or 11'';

COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_11.tile_id    IS ''Tile ID in the format'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_11.optimised_geojson  IS ''Shapefile multip
olygon in GeoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERAN
CE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it wor
ks on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are
replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_11.optimised_topojson         IS ''Shapefil
e multipolygon in TopoJSON format, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIF
Y_TOLERANCE is no longer used. (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorith
m (it works on an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEO
JSON are replaced by OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN p_rif40_geolevels_maptiles_sahsu_level4_zoom_11.gid        IS ''Geographic ID (artificia
l primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level4_zoom_11_uk ON p_rif40_geolevels_maptiles_sahs
u_level4_zoom_11(geography, tile_id);
CREATE UNIQUE INDEX p_rif40_geolevels_maptiles_sahsu_level4_zoom_11_gid ON p_rif40_geolevels_maptiles_sah
su_level4_zoom_11(geography, zoomlevel, gid);
ALTER TABLE p_rif40_geolevels_maptiles_sahsu_level4_zoom_11 ADD CONSTRAINT p_rif40_geolevels_maptiles_sah
su_level4_zoom_11_pk PRIMARY KEY (geography, geolevel_name, tile_id);
CREATE OR REPLACE FUNCTION t_rif40_sahsu_maptiles_insert()
RETURNS TRIGGER AS $func$
BEGIN

        IF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL2'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level2_zoom_6 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL2'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level2_zoom_8 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL2'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level2_zoom_11 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL1'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level1_zoom_6 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL1'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level1_zoom_8 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL1'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level1_zoom_11 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL3'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level3_zoom_6 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL3'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level3_zoom_8 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL3'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level3_zoom_11 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL4'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level4_zoom_6 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL4'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level4_zoom_8 VALUES (NEW.*);
        ELSIF NEW.geography = ''SAHSU'' AND NEW.geolevel_name = ''LEVEL4'' THEN
INSERT INTO p_rif40_geolevels_maptiles_sahsu_level4_zoom_11 VALUES (NEW.*);
        ELSE
--
-- Eventually this will automatically add a partition
--
                PERFORM rif40_log_pkg.rif40_error(-10003, ''p_rif40_geolevels_maptiles_insert'', ''no partition for maptiles geography: %, geolevel: %'',
                        NEW.geography, NEW.geolevel_name);
        END IF;
        RETURN NULL;
END;
$func$
LANGUAGE plpgsql;
COMMENT ON FUNCTION t_rif40_sahsu_maptiles_insert() IS ''Partition INSERT function for maptile geography:
SAHSU'';
CREATE TRIGGER t_rif40_sahsu_maptiles_insert
BEFORE INSERT ON t_rif40_sahsu_maptiles
FOR EACH ROW EXECUTE PROCEDURE t_rif40_sahsu_maptiles_insert();
COMMENT ON TRIGGER t_rif40_sahsu_maptiles_insert
        ON t_rif40_sahsu_maptiles IS ''Partition INSERT trigger for maptiles geography: SAHSU'';
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_sahsu_maptiles TO rif_manager;
GRANT SELECT ON t_rif40_sahsu_maptiles TO PUBLIC;
ANALYZE VERBOSE t_rif40_sahsu_maptiles;
CREATE OR REPLACE VIEW rif40_sahsu_maptiles
AS SELECT * FROM t_rif40_sahsu_maptiles;
COMMENT ON VIEW rif40_sahsu_maptiles IS ''Geolevels geometry: maptiles for hierarchy of level with a geogr
aphy. Use this table for INSERT/UPDATE/DELETE'';
COMMENT ON COLUMN rif40_sahsu_maptiles.geography     IS ''Geography (e.g EW2001)'';
COMMENT ON COLUMN rif40_sahsu_maptiles.geolevel_name         IS ''Name of geolevel. This will be a column
name in the numerator/denominator tables'';
COMMENT ON COLUMN rif40_sahsu_maptiles.tile_id       IS ''Tile ID in the format'';
COMMENT ON COLUMN rif40_sahsu_maptiles.zoomlevel     IS ''Zoom level: 1 to 11'';
COMMENT ON COLUMN rif40_sahsu_maptiles.tile_id       IS ''Tile ID in the format'';
COMMENT ON COLUMN rif40_sahsu_maptiles.optimised_geojson     IS ''Shapefile multipolygon in GeoJSON format
, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. (i
n metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by objec
t basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_GE
OJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN rif40_sahsu_maptiles.optimised_topojson    IS ''Shapefile multipolygon in TopoJSON forma
t, optimised for zoomlevel N. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output. RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE is no longer used. (
in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works on an object by obje
ct basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). Note also TOPO_OPTIMISED_GEOJSON are replaced by OPTIMISED_G
EOJSON; i.e. GeoJson optimised using ST_Simplify(). The SRID is always 4326.'';
COMMENT ON COLUMN rif40_sahsu_maptiles.gid   IS ''Geographic ID (artificial primary key originally created
 by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'';
';

--
-- Eof