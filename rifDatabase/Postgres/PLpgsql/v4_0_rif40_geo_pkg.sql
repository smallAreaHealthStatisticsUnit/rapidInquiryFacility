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

\echo Creating PG psql code (Geographic processing)...

CREATE OR REPLACE FUNCTION rif40_geo_pkg.get_srid_projection_parameters(l_geography VARCHAR, l_param VARCHAR)
RETURNS VARCHAR 
SECURITY INVOKER
AS $body$
/*
Function: 	get_srid_projection_parameters()
Parameters:	Geography, projection parameter
Returns:	Projection parameter value
Description:	Get projection parameter value from SPATIAL_REF_SYS for geography

Typically used to get the units (+units). 

WITH a AS (
	SELECT string_to_array(unnest(string_to_array(proj4text, ' ')), '=') AS projparams 
	  FROM spatial_ref_sys
	 WHERE srid = 27700
)
SELECT projparams[1] AS param, projparams[2] AS value
  FROM a
 WHERE projparams[1] IS NOT NULL;

  param   |                      value                       
----------+--------------------------------------------------
 +proj    | tmerc
 +lat_0   | 49
 +lon_0   | -2
 +k       | 0.9996012717
 +x_0     | 400000
 +y_0     | -100000
 +ellps   | airy
 +towgs84 | 446.448,-125.157,542.06,0.15,0.247,0.842,-20.489
 +units   | m
 +no_defs | 
(10 rows)
 */
DECLARE
	c1_srid CURSOR(l_geography VARCHAR, l_param VARCHAR) FOR
		WITH a AS (
			SELECT string_to_array(unnest(string_to_array(proj4text, ' ')), '=') AS projparams 
			  FROM spatial_ref_sys
			 WHERE srid = (SELECT srid FROM rif40_geographies WHERE geography = l_geography)
		)
		SELECT projparams[1] AS param, projparams[2] AS value
		  FROM a
		 WHERE projparams[1] = l_param;
	c1_rec RECORD;
BEGIN
	OPEN c1_srid(l_geography, l_param);
	FETCH c1_srid INTO c1_rec;
	CLOSE c1_srid;
--
	IF c1_rec.param IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-19001, 'get_srid_projection_parameters', 'Geography (%) or projection parameter (%) is invalid', 
			l_geography::VARCHAR 	/* Geography */,
			l_param::VARCHAR	/* Projection parameter */);
	END IF;
--
	RETURN c1_rec.value;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.get_srid_projection_parameters(VARCHAR, VARCHAR) IS 'Function: 	get_srid_projection_parameters()
Parameters:	Geography, projection parameter
Returns:	Projection parameter value
Description:	Get projection parameter value from SPATIAL_REF_SYS for geography

Typically used to get the units (+units). 

WITH a AS (
	SELECT string_to_array(unnest(string_to_array(proj4text, '' '')), ''='') AS projparams 
	  FROM spatial_ref_sys
	 WHERE srid = 27700
)
SELECT projparams[1] AS param, projparams[2] AS value
  FROM a
 WHERE projparams[1] IS NOT NULL;

  param   |                      value                       
----------+--------------------------------------------------
 +proj    | tmerc
 +lat_0   | 49
 +lon_0   | -2
 +k       | 0.9996012717
 +x_0     | 400000
 +y_0     | -100000
 +ellps   | airy
 +towgs84 | 446.448,-125.157,542.06,0.15,0.247,0.842,-20.489
 +units   | m
 +no_defs | 
(10 rows)';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	drop_rif40_geolevels_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Drop rif40_geolevels lookup and hierarchy tables

 */
DECLARE
	c1 CURSOR FOR
		SELECT * FROM information_schema.tables 
		 WHERE table_name IN (SELECT DISTINCT LOWER(lookup_table)
				        FROM t_rif40_geolevels WHERE lookup_table IS NOT NULL
				       UNION 
				      SELECT DISTINCT LOWER(hierarchytable)
				        FROM rif40_geographies
				       UNION 
				      SELECT DISTINCT LOWER(hierarchytable)||'_orig'
				        FROM rif40_geographies
				 /*    UNION 		-* NOT shapefile/centroid tables; these are loaded by ../shapefiles/shapefiles.sql and are now GIS objects *-
				      SELECT DISTINCT LOWER(shapefile_table)
				        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
				       UNION 
				      SELECT DISTINCT LOWER(centroids_table)
				        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL */);
--
	c1_rec information_schema.tables%ROWTYPE;
--
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'drop_rif40_geolevels_lookup_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR 	/* User name */);
	END IF;
----
-- Drop existing objects
--
	FOR c1_rec IN c1 LOOP
		sql_stmt:='DROP TABLE '||quote_ident(LOWER(c1_rec.table_name))||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables() IS 'Function: 	drop_rif40_geolevels_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Drop rif40_geolevels lookup and hierarchy tables';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables(geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	drop_rif40_geolevels_lookup_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Drop rif40_geolevels lookup and hierarchy tables

 */
DECLARE
	c1 CURSOR(l_geography VARCHAR) FOR
		SELECT * FROM information_schema.tables 
		 WHERE table_name IN (SELECT DISTINCT LOWER(lookup_table)
				        FROM t_rif40_geolevels a
				       WHERE lookup_table IS NOT NULL
				         AND a.geography = l_geography
				       UNION 
				      SELECT DISTINCT LOWER(hierarchytable)
				        FROM rif40_geographies b
				       WHERE b.geography = l_geography
				       UNION 
				      SELECT DISTINCT LOWER(hierarchytable)||'_orig'
				        FROM rif40_geographies c
				       WHERE c.geography = l_geography
				 /*    UNION 		-* NOT shapefile/centroid tables; these are loaded by ../shapefiles/shapefiles.sql and are now GIS objects *-
				      SELECT DISTINCT LOWER(shapefile_table)
				        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
				       UNION 
				      SELECT DISTINCT LOWER(centroids_table)
				        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL */);
--
	c1_rec information_schema.tables%ROWTYPE;
--
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'drop_rif40_geolevels_lookup_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR 	/* User name */);
	END IF;
----
-- Drop existing objects
--
	FOR c1_rec IN c1(geography) LOOP
		sql_stmt:='DROP TABLE '||quote_ident(LOWER(c1_rec.table_name))||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables(VARCHAR) IS 'Function: 	drop_rif40_geolevels_lookup_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Drop rif40_geolevels lookup and hierarchy tables';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	create_rif40_geolevels_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Create and popualte rif40_geolevels lookup and create hierarchy tables
 */
DECLARE
	c2geolook2 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
--
	c2_rec rif40_geographies%ROWTYPE;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'create_rif40_geolevels_lookup_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR 	/* User name */);
	END IF;
--
	FOR c2_rec IN c2geolook2 LOOP
		PERFORM rif40_geo_pkg.create_rif40_geolevels_lookup_tables(c2_rec.geography);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables() IS 'Function: 	create_rif40_geolevels_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Create and popualte rif40_geolevels lookup and create hierarchy tables';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables(l_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	create_rif40_geolevels_lookup_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Create and populate rif40_geolevels lookup and create hierarchy tables

Lookup table example:

CREATE TABLE ew2001_soa2001
AS
WITH a AS (
	SELECT soa2001 AS soa2001, NULL AS name, ST_Union(geom) AS geom
	  FROM x_ew01_soa2001
	 GROUP BY soa2001
)
SELECT soa2001, name
  FROM a
 ORDER BY 1;

Hierarchy table example

CREATE TABLE sahsuland_geography (
          level1 VARCHAR(100)
        , level2 VARCHAR(100)
        , level3 VARCHAR(100)
        , level4 VARCHAR(100)
);

The Hierarchy table is not populated; this is assumed to be done in ArcGIS
as ARcGIS has an aggregation function that uses best fit to build a hierarchy.
The below method will do the same. The method removes duplicates by picking the 
intersection where the intersected area most closely matches the smaller of the 2 areas

See: ../postgres/gis_intersection_prototype.sql

 */
DECLARE
	c1geolook CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id;
	c2geolook CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
--
	c1_rec t_rif40_geolevels%ROWTYPE;
	c2_rec rif40_geographies%ROWTYPE;
--
	sql_stmt VARCHAR;
	hier_stmt1 VARCHAR;
	hier_stmt2 VARCHAR[]:=NULL;
	hier_stmt3 VARCHAR[]:=NULL;
	hier_stmt4 VARCHAR;
	previous_geography VARCHAR:=NULL;
	hierarchytable VARCHAR:=NULL;
	i INTEGER;
	last INTEGER;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'create_rif40_geolevels_lookup_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR 	/* User name */);
	END IF;
--
-- Create lookup and Hierarchy tables
--
	FOR c1_rec IN c1geolook(l_geography) LOOP
		IF previous_geography IS NULL THEN
			OPEN c2geolook(c1_rec.geography);
			FETCH c2geolook INTO c2_rec;
			CLOSE c2geolook;
			hierarchytable:=c2_rec.hierarchytable;
			hier_stmt1:='CREATE TABLE '||quote_ident(LOWER(hierarchytable))||' ('||E'\n'||E'\t'||'  '||quote_ident(LOWER(c1_rec.geolevel_name))||' VARCHAR(100)'||E'\n';
		ELSIF previous_geography != c1_rec.geography THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'create_rif40_geolevels_lookup_tables', 'Creating % geography hierarchy table: %',
				c1_rec.geography, hierarchytable);
			hier_stmt1:=hier_stmt1||')';
			PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
			hier_stmt1:='GRANT SELECT ON '||quote_ident(LOWER(hierarchytable))||' TO PUBLIC';
			PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
			i:=0;
			last:=0;
			FOREACH hier_stmt1 IN ARRAY hier_stmt3 LOOP /* Column comments */
				last:=last+1;
			END LOOP;	
			FOREACH hier_stmt1 IN ARRAY hier_stmt3 LOOP /* Indexes */
				i:=i+1;
				IF i != last THEN
					hier_stmt4:='CREATE INDEX '||hier_stmt1;
				ELSE
					hier_stmt4:='CREATE UNIQUE INDEX '||hier_stmt1;
				END IF;
				PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt4);
			END LOOP;	
			FOREACH hier_stmt1 IN ARRAY hier_stmt2 LOOP
				PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
			END LOOP;	
			hier_stmt1:='ANALYZE VERBOSE '||quote_ident(LOWER(hierarchytable));
			PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
			OPEN c2geolook(previous_geography);
			FETCH c2geolook INTO c2_rec;
			CLOSE c2geolook;
			hier_stmt1:='COMMENT ON TABLE '||quote_ident(LOWER(hierarchytable))||' IS '||quote_literal(c2_rec.description||' geo-level hierarchy table');
			PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
--
-- Next geography
--
			OPEN c2geolook(c1_rec.geography);
			FETCH c2geolook INTO c2_rec;
			CLOSE c2geolook;
			hierarchytable:=c2_rec.hierarchytable;
--
-- Reset arrays
--
			hier_stmt2:=NULL;
			hier_stmt3:=NULL;
--
-- Create table - can be CTAS in future (see comment at top)
--
			hier_stmt1:='CREATE TABLE '||quote_ident(LOWER(hierarchytable))||' ('||E'\n'||E'\t'||'  '||quote_ident(LOWER(c1_rec.geolevel_name))||' VARCHAR(100)'||E'\n';
		ELSE
			hier_stmt1:=hier_stmt1||E'\t'||', '||quote_ident(LOWER(c1_rec.geolevel_name))||' VARCHAR(100)'||E'\n';
		END IF;
		hier_stmt2[c1_rec.geolevel_id]:='COMMENT ON COLUMN '||quote_ident(LOWER(hierarchytable))||'.'||quote_ident(LOWER(c1_rec.geolevel_name))||
			' IS '||quote_literal(c1_rec.description);
		hier_stmt3[c1_rec.geolevel_id]:='/* Create index */ '||quote_ident(LOWER(c1_rec.geolevel_name)||'_idx'||c1_rec.geolevel_id)||' ON '||
			quote_ident(LOWER(hierarchytable))||'('||quote_ident(LOWER(c1_rec.geolevel_name))||')';
		previous_geography:=c1_rec.geography;
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'create_rif40_geolevels_lookup_tables', 'Creating % geography % lookup table: %',
			c1_rec.geography, c1_rec.geolevel_name, c1_rec.lookup_table);
--
-- Create table - using CTAS
--
		sql_stmt:='CREATE TABLE '||quote_ident(LOWER(c1_rec.lookup_table))||E'\n'||'AS'||E'\n';
--
-- WITH block
--
		sql_stmt:=sql_stmt||'WITH a AS ('||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'SELECT '||quote_ident(LOWER(c1_rec.shapefile_area_id_column))||' AS '||quote_ident(LOWER(c1_rec.geolevel_name));
		IF c1_rec.shapefile_desc_column IS NOT NULL THEN
			sql_stmt:=sql_stmt||', '||quote_ident(LOWER(c1_rec.shapefile_desc_column))||' AS '||quote_ident(LOWER(c1_rec.lookup_desc_column));
		ELSE
			sql_stmt:=sql_stmt||', NULL::Text AS '||quote_ident(LOWER(c1_rec.lookup_desc_column));
		END IF;
		sql_stmt:=sql_stmt||', ST_Union(geom) AS geom'||E'\n';
		IF c1_rec.centroids_table IS NOT NULL AND 
		   c1_rec.centroidxcoordinate_column IS NULL AND 
		   c1_rec.centroidycoordinate_column IS NULL THEN
			sql_stmt:=sql_stmt||E'\t'||'  FROM '||quote_ident(LOWER(c1_rec.centroids_table))||E'\n';
		ELSE
			sql_stmt:=sql_stmt||E'\t'||'  FROM '||quote_ident(LOWER(c1_rec.shapefile_table))||E'\n';
		END IF;
		IF c1_rec.shapefile_desc_column IS NOT NULL THEN
			sql_stmt:=sql_stmt||E'\t'||' GROUP BY '||quote_ident(LOWER(c1_rec.shapefile_area_id_column))||', '||quote_ident(LOWER(c1_rec.shapefile_desc_column))||E'\n';
		ELSE
			sql_stmt:=sql_stmt||E'\t'||' GROUP BY '||quote_ident(LOWER(c1_rec.shapefile_area_id_column))||E'\n';
		END IF;
		sql_stmt:=sql_stmt||')'||E'\n';
--
-- Main select
--
		sql_stmt:=sql_stmt||'SELECT '||quote_ident(LOWER(c1_rec.geolevel_name))||', '||quote_ident(LOWER(c1_rec.lookup_desc_column));
		IF c1_rec.centroids_table IS NOT NULL AND 
		   c1_rec.centroidxcoordinate_column IS NULL AND 
		   c1_rec.centroidycoordinate_column IS NULL THEN
			sql_stmt:=sql_stmt||', ST_X(a.geom) AS '||quote_ident(LOWER(c1_rec.centroidxcoordinate_column))||
				', ST_Y(a.geom) AS '||quote_ident(LOWER(c1_rec.centroidycoordinate_column))||E'\n'||
				'  FROM a'||E'\n';
		ELSE
			sql_stmt:=sql_stmt||E'\n'||'  FROM a'||E'\n';
		END IF;
		sql_stmt:=sql_stmt||' ORDER BY 1'||E'\n';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Fix name column type
--
		sql_stmt:='ALTER TABLE '||quote_ident(LOWER(c1_rec.lookup_table))||' ALTER COLUMN name TYPE VARCHAR(100);'||E'\n';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Index
--
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c1_rec.lookup_table)||'_pk')||' ON '||quote_ident(LOWER(c1_rec.lookup_table))||
			'('||quote_ident(LOWER(c1_rec.geolevel_name))||')';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Grant
--
		sql_stmt:='GRANT SELECT ON '||quote_ident(LOWER(c1_rec.lookup_table))||' TO PUBLIC';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
--  Analyze
--
		sql_stmt:='ANALYZE VERBOSE '||quote_ident(LOWER(c1_rec.lookup_table));
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Comment
--
		sql_stmt:='COMMENT ON TABLE '||quote_ident(LOWER(c1_rec.lookup_table))||' IS '||quote_literal(c1_rec.description||' lookup table');
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident(LOWER(c1_rec.lookup_table))||'.'||quote_ident(LOWER(c1_rec.geolevel_name))||' IS '''||quote_ident(LOWER(c1_rec.geolevel_name))||'''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||quote_ident(LOWER(c1_rec.lookup_table))||'.name IS '''||quote_ident(LOWER(c1_rec.geolevel_name))||' name''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
--
-- Do the last Hierarchy table
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'create_rif40_geolevels_lookup_tables', 'Creating % geography hierarchy table: %',
		c1_rec.geography	/* Geography */, 
		hierarchytable		/* Hierarchy table */);
--
	hier_stmt1:=hier_stmt1||')';
	PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
	hier_stmt1:='ANALYZE VERBOSE '||quote_ident(LOWER(hierarchytable));
	PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
	hier_stmt1:='GRANT SELECT ON '||quote_ident(LOWER(hierarchytable))||' TO PUBLIC';
	PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
	i:=0;
	last:=0;
	FOREACH hier_stmt1 IN ARRAY hier_stmt3 LOOP /* Column comments */
		last:=last+1;
	END LOOP;	
	FOREACH hier_stmt1 IN ARRAY hier_stmt3 LOOP /* Indexes */
		i:=i+1;
		IF i != last THEN
			hier_stmt4:='CREATE INDEX '||hier_stmt1;
		ELSE
			hier_stmt4:='CREATE UNIQUE INDEX '||hier_stmt1;
		END IF;
		PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt4);
	END LOOP;	
	FOREACH hier_stmt1 IN ARRAY hier_stmt2 LOOP
		PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
	END LOOP;	
	OPEN c2geolook(previous_geography);
	FETCH c2geolook INTO c2_rec;
	CLOSE c2geolook;
	hier_stmt1:='COMMENT ON TABLE '||quote_ident(LOWER(hierarchytable))||' IS '||quote_literal(c2_rec.description||' geo-level hierarchy table');
	PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables(VARCHAR) IS 'Function: 	create_rif40_geolevels_lookup_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Create and popualte rif40_geolevels lookup and create hierarchy tables

Lookup table example:

CREATE TABLE ew2001_soa2001
AS
WITH a AS (
	SELECT soa2001 AS soa2001, NULL AS name, ST_Union(geom) AS geom
	  FROM x_ew01_soa2001
	 GROUP BY soa2001
)
SELECT soa2001, name
  FROM a
 ORDER BY 1;

Hierarchy table example

CREATE TABLE sahsuland_geography (
          level1 VARCHAR(100)
        , level2 VARCHAR(100)
        , level3 VARCHAR(100)
        , level4 VARCHAR(100)
);

The Hierarchy table is not populated; this is assumed to be done in ArcGIS
as ARcGIS has an aggregation function that uses best fit to build a hierarchy.
The below method will do the same. The method removes duplicates by picking the 
intersection where the intersected area most closely matches the smaller of the 2 areas

See: ../postgres/gis_intersection_prototype.sql';

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

This function must be run after rif40_geo_pkg.populate_rif40_geometry_tables() i.e. area_id, optimised_geometry and optimised_geometry must exist

Population SQL used to fail with (fixed with ST_MakeValid()):

ERROR:  Error performing intersection: TopologyException: found non-noded intersection between LINESTRING (-2.99198 53.3669, -2.98262 53.367) and LINESTRING (-2.98556 53.367, -2.98854 53.3655) at -2.9855578257498308 53.366966593247653

To test for invalid polygons:

SELECT area_id, ST_Isvalid(optimised_geometry) AS v1, ST_Isvalid(optimised_geometry) AS v2
  FROM t_rif40_geolevels_geometry_ew01_oa2001
 WHERE NOT ST_Isvalid(optimised_geometry) OR NOT ST_Isvalid(optimised_geometry);

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
       ST_Area(a2.optimised_geometry) AS a2_area,
       ST_Area(ST_Intersection(a1.optimised_geometry, a2.optimised_geometry)) a12_area
  FROM t_rif40_geolevels_geometry_ew01_scntry2001 a1 CROSS JOIN t_rif40_geolevels_geometry_ew01_cntry2001 a2
 WHERE ST_Intersects(a1.optimised_geometry, a2.optimised_geometry)
), x23 AS ( -* Subqueries x23 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a2.area_id AS cntry2001, a3.area_id AS gor2001,
       ST_Area(a3.optimised_geometry) AS a3_area,
       ST_Area(ST_Intersection(a2.optimised_geometry, a3.optimised_geometry)) a23_area
  FROM t_rif40_geolevels_geometry_ew01_cntry2001 a2 CROSS JOIN t_rif40_geolevels_geometry_ew01_gor2001 a3
 WHERE ST_Intersects(a2.optimised_geometry, a3.optimised_geometry)
), x34 AS ( -* Subqueries x34 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a3.area_id AS gor2001, a4.area_id AS ladua2001,
       ST_Area(a4.optimised_geometry) AS a4_area,
       ST_Area(ST_Intersection(a3.optimised_geometry, a4.optimised_geometry)) a34_area
  FROM t_rif40_geolevels_geometry_ew01_gor2001 a3 CROSS JOIN t_rif40_geolevels_geometry_ew01_ladua2001 a4
 WHERE ST_Intersects(a3.optimised_geometry, a4.optimised_geometry)
), x45 AS ( -* Subqueries x45 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a4.area_id AS ladua2001, a5.area_id AS ward2001,
       ST_Area(a5.optimised_geometry) AS a5_area,
       ST_Area(ST_Intersection(a4.optimised_geometry, a5.optimised_geometry)) a45_area
  FROM t_rif40_geolevels_geometry_ew01_ladua2001 a4 CROSS JOIN t_rif40_geolevels_geometry_ew01_ward2001 a5
 WHERE ST_Intersects(a4.optimised_geometry, a5.optimised_geometry)
), x56 AS ( -* Subqueries x56 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a5.area_id AS ward2001, a6.area_id AS soa2001,
       ST_Area(a6.optimised_geometry) AS a6_area,
       ST_Area(ST_Intersection(a5.optimised_geometry, a6.optimised_geometry)) a56_area
  FROM t_rif40_geolevels_geometry_ew01_ward2001 a5 CROSS JOIN t_rif40_geolevels_geometry_ew01_soa2001 a6
 WHERE ST_Intersects(a5.optimised_geometry, a6.optimised_geometry)
), x67 AS ( -* Subqueries x67 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area *-
SELECT a6.area_id AS soa2001, a7.area_id AS oa2001,
       ST_Area(a7.optimised_geometry) AS a7_area,
       ST_Area(ST_Intersection(a6.optimised_geometry, a7.optimised_geometry)) a67_area
  FROM t_rif40_geolevels_geometry_ew01_soa2001 a6 CROSS JOIN t_rif40_geolevels_geometry_ew01_oa2001 a7
 WHERE ST_Intersects(a6.optimised_geometry, a7.optimised_geometry)
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
		geolevel_table[i]:=quote_ident('t_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c1_rec.geolevel_name));
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
				'       ST_Area(a'||i+1||'.optimised_geometry) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.optimised_geometry, a'||i+1||'.optimised_geometry)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||geolevel_table[i]||' a'||i||' CROSS JOIN '||geolevel_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.optimised_geometry, a'||i+1||'.optimised_geometry)'||E'\n'||
				'), ';
		ELSIF i < (num_geolevels-1) THEN
			sql_stmt:=sql_stmt||
				'x'||i||i+1||' AS ( /* Subqueries x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||': intersection aggregate geometries starting from the lowest resolution.'||E'\n'||
         		      	E'\t'||'       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.'||E'\n'||
 	       			E'\t'||'       Calculate the area of the higher resolution geolevel and the area of the intersected area */'||E'\n'||
				'SELECT a'||i||'.area_id AS '||geolevel_name[i]||', a'||i+1||'.area_id AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.optimised_geometry) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.optimised_geometry, a'||i+1||'.optimised_geometry)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||geolevel_table[i]||' a'||i||' CROSS JOIN '||geolevel_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.optimised_geometry, a'||i+1||'.optimised_geometry)'||E'\n'||
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
				'       ST_Area(a'||i+1||'.optimised_geometry) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.optimised_geometry, a'||i+1||'.optimised_geometry)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||geolevel_table[i]||' a'||i||' CROSS JOIN '||geolevel_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.optimised_geometry, a'||i+1||'.optimised_geometry)'||E'\n'||
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

This function must be run after rif40_geo_pkg.populate_rif40_geometry_tables() i.e. area_id, optimised_geometry and optimised_geometry must exist

Population SQL used to fail with (fixed with ST_MakeValid()):

ERROR:  Error performing intersection: TopologyException: found non-noded intersection between LINESTRING (-2.99198 53.3669, -2.98262 53.367) and LINESTRING (-2.98556 53.367, -2.98854 53.3655) at -2.9855578257498308 53.366966593247653

To test for invalid polygons:

SELECT area_id, ST_Isvalid(optimised_geometry) AS v1, ST_Isvalid(optimised_geometry) AS v2
  FROM t_rif40_geolevels_geometry_ew01_oa2001
 WHERE NOT ST_Isvalid(optimised_geometry) OR NOT ST_Isvalid(optimised_geometry);

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
       ST_Area(a2.optimised_geometry) AS a2_area,
       ST_Area(ST_Intersection(a1.optimised_geometry, a2.optimised_geometry)) a12_area
  FROM t_rif40_geolevels_geometry_ew01_scntry2001 a1 CROSS JOIN t_rif40_geolevels_geometry_ew01_cntry2001 a2
 WHERE ST_Intersects(a1.optimised_geometry, a2.optimised_geometry)
), x23 AS ( /* Subqueries x23 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a2.area_id AS cntry2001, a3.area_id AS gor2001,
       ST_Area(a3.optimised_geometry) AS a3_area,
       ST_Area(ST_Intersection(a2.optimised_geometry, a3.optimised_geometry)) a23_area
  FROM t_rif40_geolevels_geometry_ew01_cntry2001 a2 CROSS JOIN t_rif40_geolevels_geometry_ew01_gor2001 a3
 WHERE ST_Intersects(a2.optimised_geometry, a3.optimised_geometry)
), x34 AS ( /* Subqueries x34 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a3.area_id AS gor2001, a4.area_id AS ladua2001,
       ST_Area(a4.optimised_geometry) AS a4_area,
       ST_Area(ST_Intersection(a3.optimised_geometry, a4.optimised_geometry)) a34_area
  FROM t_rif40_geolevels_geometry_ew01_gor2001 a3 CROSS JOIN t_rif40_geolevels_geometry_ew01_ladua2001 a4
 WHERE ST_Intersects(a3.optimised_geometry, a4.optimised_geometry)
), x45 AS ( /* Subqueries x45 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a4.area_id AS ladua2001, a5.area_id AS ward2001,
       ST_Area(a5.optimised_geometry) AS a5_area,
       ST_Area(ST_Intersection(a4.optimised_geometry, a5.optimised_geometry)) a45_area
  FROM t_rif40_geolevels_geometry_ew01_ladua2001 a4 CROSS JOIN t_rif40_geolevels_geometry_ew01_ward2001 a5
 WHERE ST_Intersects(a4.optimised_geometry, a5.optimised_geometry)
), x56 AS ( /* Subqueries x56 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a5.area_id AS ward2001, a6.area_id AS soa2001,
       ST_Area(a6.optimised_geometry) AS a6_area,
       ST_Area(ST_Intersection(a5.optimised_geometry, a6.optimised_geometry)) a56_area
  FROM t_rif40_geolevels_geometry_ew01_ward2001 a5 CROSS JOIN t_rif40_geolevels_geometry_ew01_soa2001 a6
 WHERE ST_Intersects(a5.optimised_geometry, a6.optimised_geometry)
), x67 AS ( /* Subqueries x67 ... x67: intersection aggregate geometries starting from the lowest resolution.
               Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
               Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a6.area_id AS soa2001, a7.area_id AS oa2001,
       ST_Area(a7.optimised_geometry) AS a7_area,
       ST_Area(ST_Intersection(a6.optimised_geometry, a7.optimised_geometry)) a67_area
  FROM t_rif40_geolevels_geometry_ew01_soa2001 a6 CROSS JOIN t_rif40_geolevels_geometry_ew01_oa2001 a7
 WHERE ST_Intersects(a6.optimised_geometry, a7.optimised_geometry)
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
       ST_Area(a3.optimised_geometry) AS a3_area,
       ST_Area(ST_Intersection(a2.optimised_geometry, a3.optimised_geometry)) AS a23_area
  FROM t_rif40_geolevels_geometry_sahsu_level3 a3, t_rif40_geolevels_geometry_sahsu_level2 a2  
 WHERE ST_Intersects(a2.optimised_geometry, a3.optimised_geometry);

';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.fix_null_geolevel_names()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	fix_null_geolevel_names()
Parameters:	None
Returns:	Nothing
Description:	Fix NULL geolevel names in geography geolevel geometry and lookup table data, re-analyze
		Add unique index
 */
DECLARE
	c2_fixnul2 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
--
	c2_rec rif40_geographies%ROWTYPE;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'fix_null_geolevel_names', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Create lookup and Hierarchy tables
--
	FOR c2_rec IN c2_fixnul2 LOOP
		PERFORM rif40_geo_pkg.fix_null_geolevel_names(c2_rec.geography);
	END LOOP;
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names() IS 'Function: 	fix_null_geolevel_names()
Parameters:	None
Returns:	Nothing
Description:	Fix NULL geolevel names in geography geolevel geometry and lookup table data, re-analyze
		Add unique index';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.fix_null_geolevel_names(l_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	fix_null_geolevel_names()
Parameters:	geography
Returns:	Nothing
Description:	Fix NULL geolevel names in geography geolevel geometry and lookup table data, re-analyze
		Fix non-unique names in lookup tables and geolevel geometry table
		Add unique index

Fix NULL geolevel names in lookup table data example SQL>

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
	SELECT DISTINCT a.ladua2001 -* source *-, a.oa2001 -* target *-, b2.name||' OA2001('||COALESCE(a.oa2001, 'UNK')||')' AS oa2001_name
          FROM ew2001_geography -* Hierarchy table *- a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                -* highest resolution geolevel with complete descriptive names *-
         WHERE NOT EXISTS (
                SELECT b1.name
                  FROM ew2001_coa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.oa2001);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_coa2001 c
           SET name = (SELECT oa2001_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.oa2001)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

Fix NULL geolevel names in geography geolevel geometry data example SQL>

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
SELECT DISTINCT a.ladua2001 -* source *-, a.oa2001 -* target *-, b2.name||' OA2001('||COALESCE(a.oa2001, 'UNK')||')' AS oa2001_name
          FROM ew2001_geography -* Hierarchy table *- a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                -* highest resolution geolevel with complete descriptive names *-
         WHERE NOT EXISTS (
                SELECT b1.area_id
                  FROM t_rif40_geolevels_geometry_ew01_oa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.area_id);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE t_rif40_geolevels_geometry_ew01_oa2001 c
           SET name = (SELECT oa2001_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.area_id)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

Fix non-unique names in lookup tables and geolevel geometry table example SQL

DROP TABLE IF EXISTS temp_fix_null_geolevel_names;

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS
WITH a AS (
	SELECT name, COUNT(*) AS total
	  FROM ew2001_ward2001
	 GROUP BY name
	HAVING COUNT(*) > 1
)
SELECT DISTINCT b.ward2001, a.name, c.ladua2001, a.name||'('||c.ladua2001||')' AS new_name
  FROM a, ew2001_ward2001 b, ew2001_geography c
 WHERE a.name = b.name
   AND c.ward2001 = b.ward2001;

CREATE UNIQUE INDEX ward2001_pk ON temp_fix_null_geolevel_names(ward2001);
CREATE UNIQUE INDEX ward2001_uk ON temp_fix_null_geolevel_names(new_name);
ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_ward2001 c
           SET name = (SELECT new_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.ward2001)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

UPDATE t_rif40_geolevels_geometry_ew01_ward2001 c
           SET name = (SELECT new_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.area_id)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

 */

DECLARE
	c1_fixnul CURSOR(l_geography VARCHAR) FOR
		SELECT COUNT(geolevel_id) AS total_geolevel 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography;
	c2a_fixnul CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id;
	c2_fixnul CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id DESC;
	c3_fixnul 	REFCURSOR;
	c4_fixnul CURSOR(l_geography VARCHAR, l_geolevel_id INTEGER) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography   = l_geography
		   AND geolevel_id = l_geolevel_id;
	c6_fixnul CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM rif40_geographies
		 WHERE geography   = l_geography;
--
	total_geolevel	INTEGER;
	desc_geolevel	INTEGER=NULL;
	total_desc	INTEGER=NULL;
	l_rows		INTEGER;
	c2a_rec 	t_rif40_geolevels%ROWTYPE;
	c2_rec 		t_rif40_geolevels%ROWTYPE;
	c3_rec 		RECORD;
	higher_c4_rec 	t_rif40_geolevels%ROWTYPE;
	c5_rec 		RECORD;
	c6_rec 		rif40_geographies%ROWTYPE;
--
	sql_stmt 	VARCHAR;
	i 		INTEGER:=0;
	j 		INTEGER:=0;
	re_analyze 	BOOLEAN;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'fix_null_geolevel_names', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	OPEN c1_fixnul(l_geography);
	FETCH c1_fixnul INTO total_geolevel;
	CLOSE c1_fixnul;
--
	OPEN c6_fixnul(l_geography);
	FETCH c6_fixnul INTO c6_rec;
	CLOSE c6_fixnul;
	IF total_geolevel = 0 OR c6_rec.hierarchytable IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10031, 'fix_null_geolevel_names', 'Geography % not found', 
			l_geography::VARCHAR	/* Geography */);
	END IF;
--
-- First past. Start with lowest resolution geolevel 
--
	i:=total_geolevel;
--
-- Check geolevels were processed
--
	IF total_geolevel = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10017, 'fix_null_geolevel_names', 'No rows found in: t_rif40_geolevels for geography: %',
			l_geography::VARCHAR 	/* Geography */);
	END IF;
--
	FOR c2a_rec IN c2a_fixnul(l_geography) LOOP
		j:=j+1;
--
-- Get the total description fields for the geolevel lookup table
--
		sql_stmt:='SELECT COUNT('||quote_ident(LOWER(c2a_rec.geolevel_name))||') AS total_area_ids,'||E'\n'||
			  '       COUNT('||quote_ident(LOWER(c2a_rec.lookup_desc_column))||') AS total_desc,'||E'\n'||
			  '       COUNT(DISTINCT('||quote_ident(LOWER(c2a_rec.lookup_desc_column))||')) AS total_uniq_desc'||E'\n'||
			  '  FROM '||quote_ident(LOWER(c2a_rec.lookup_table));
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'fix_null_geolevel_names', 'Pass: 1 Get the total description fields SQL> %', 
			sql_stmt::VARCHAR		/* SQL statement */);
		OPEN c3_fixnul FOR EXECUTE sql_stmt;
		FETCH c3_fixnul INTO c3_rec;
		CLOSE c3_fixnul;
--
-- OK, found a complete set of descriptions
--
		IF c3_rec.total_desc = c3_rec.total_area_ids AND c3_rec.total_desc = c3_rec.total_uniq_desc THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'fix_null_geolevel_names',
				'Pass: 1 Geography % geolevel % lookup table: % has % unique description rows (%)', 
				l_geography::VARCHAR 				/* Geography */, 
				c2a_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2a_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				LOWER(c2a_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
--
-- Oops, there is a gap
--
			IF desc_geolevel IS NOT NULL AND c2a_rec.geolevel_id - desc_geolevel > 1 THEN
				PERFORM rif40_log_pkg.rif40_error(-10026, 'fix_null_geolevel_names', 
					'Pass: 1 Geography % gap found between geolevel IDs: % and % (with complete descriptive names) for geography: % not found',
					l_geography::VARCHAR 		/* Geography */, 
					desc_geolevel::VARCHAR 		/* First geolevel ID with complete descriptive names */,
					c2a_rec.geolevel_id::VARCHAR 	/* Current geolevel ID with complete descriptive names */,
					l_geography::VARCHAR 		/* Geography */);
			ELSE
				desc_geolevel:=c2a_rec.geolevel_id;
				total_desc:=c3_rec.total_desc;
			END IF;
/*		ELSE
			desc_geolevel checked later */
		END IF;
	END LOOP;
--
-- Check geolevels were processed
--
	IF j = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10048, 'fix_null_geolevel_names', 
			'No geolevels were processed in: t_rif40_geolevels for geography: %, expected geolevels: %',
			l_geography::VARCHAR 	/* Geography */,
			total_geolevel::VARCHAR	/* Expected geolevels */);
	END IF;
--
-- Check there is a geolevel with complete descriptive names 
--
	IF desc_geolevel IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10027, 'fix_null_geolevel_names', 'No geolevel with complete descriptive names for geography: %',
			l_geography::VARCHAR 	/* Geography */);
	END IF;
--
-- Get the geolevel definition of the highest resolution geolevel with complete descriptive names. This is as the first name part of the defaulted
-- missing descriptions.
--
-- Abellan LEVEL3(01.001.000100)
--
	OPEN c4_fixnul(l_geography, desc_geolevel);
	FETCH c4_fixnul INTO higher_c4_rec;
	CLOSE c4_fixnul;
	IF higher_c4_rec IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10028, 'fix_null_geolevel_names', 'Geolevel ID: % (with complete descriptive names) for geography: % not found',
			desc_geolevel::VARCHAR 	/* Geolevel ID with complete descriptive names */,
			l_geography::VARCHAR 	/* Geography */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names',
			'[10028] Geography % using geolevel % lookup table: % has % unique description rows (%) as the higher reference geolevel for descriptive names', 
			l_geography::VARCHAR					/* Geography */, 
			higher_c4_rec.geolevel_name::VARCHAR			/* Higher reference geolevel name */, 
			higher_c4_rec.lookup_table::VARCHAR			/* Higher reference geolevel lookup table */,
			total_desc::VARCHAR					/* Geolevel lookup table unique description rows */, 
			LOWER(higher_c4_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
	END IF;
--
-- Second pass. Start with highest resolution geolevel 
--
	j:=0;
	FOR c2_rec IN c2_fixnul(l_geography) LOOP
		j:=j+1;
		i:=i-1;
		re_analyze:=FALSE;
--
-- Get the total description fields for the geolevel lookup table
--
		sql_stmt:='SELECT COUNT('||quote_ident(LOWER(c2_rec.geolevel_name))||') AS total_area_ids,'||E'\n'||
			  '       COUNT('||quote_ident(LOWER(c2_rec.lookup_desc_column))||') AS total_desc,'||E'\n'||
			  '       SUM(CASE WHEN '||quote_ident(LOWER(c2_rec.lookup_desc_column))||' IS NULL THEN 1 ELSE 0 END) AS total_null_desc,'||E'\n'||
			  '       COUNT(DISTINCT('||quote_ident(LOWER(c2_rec.lookup_desc_column))||')) AS total_uniq_desc'||E'\n'||
			  '  FROM '||quote_ident(LOWER(c2_rec.lookup_table));
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'fix_null_geolevel_names', 'Pass: 2 Get the total description fields SQL> %', 
			sql_stmt::VARCHAR		/* SQL statement */);
		OPEN c3_fixnul FOR EXECUTE sql_stmt;
		FETCH c3_fixnul INTO c3_rec;
		CLOSE c3_fixnul;
--
-- Checks:
--
-- a) Lookup table empty
--
		l_rows:=NULL;
		IF c3_rec.total_area_ids = 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-10022, 'fix_null_geolevel_names', 
				'Pass: 2a Geography % geolevel % lookup table: % has no rows', 
				l_geography::VARCHAR		/* Geography */, 
				c2_rec.geolevel_name::VARCHAR	/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR	/* Geolevel lookup table */);
--
-- b) level 1 (lowest resolution geolevel) must have only 1 row
--
		ELSIF c2_rec.geolevel_id = 1 AND c3_rec.total_area_ids > 1 THEN 
			PERFORM rif40_log_pkg.rif40_error(-10023, 'fix_null_geolevel_names',
				'Pass: 2b Geography % lowest resolution geolevel % lookup table: % has >1: % rows (%) - OK', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
--
-- c) OK - all present and unique
--
		ELSIF c3_rec.total_desc = c3_rec.total_area_ids AND c3_rec.total_desc = c3_rec.total_uniq_desc THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names',
				'Pass: 2c Geography % geolevel % lookup table: % has % unique description rows (%) - OK', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
--
-- d) Not unique
--
		ELSIF c3_rec.total_desc = c3_rec.total_area_ids AND c3_rec.total_desc != c3_rec.total_uniq_desc THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'fix_null_geolevel_names',
				'Pass: 2d Geography % geolevel % lookup table: % has % description rows (%), only % are unique, % are NULL - FIX', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table description rows */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */,
				c3_rec.total_uniq_desc::VARCHAR			/* Geolevel lookup table unique description rows */,
				c3_rec.total_null_desc::VARCHAR			/* Geolevel lookup table NULL description rows */);
--
-- Fix non unique names
--
/*
DROP TABLE IF EXISTS temp_fix_null_geolevel_names;

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS
WITH a AS (
	SELECT name, COUNT(*) AS total
	  FROM ew2001_ward2001
	 GROUP BY name
	HAVING COUNT(*) > 1
)
SELECT DISTINCT b.ward2001, a.name, c.ladua2001, a.name||'('||c.ladua2001||')' AS new_name
  FROM a, ew2001_ward2001 b, ew2001_geography c
 WHERE a.name = b.name
   AND c.ward2001 = b.ward2001;

CREATE UNIQUE INDEX ward2001_pk ON temp_fix_null_geolevel_names(ward2001);
CREATE UNIQUE INDEX ward2001_uk ON temp_fix_null_geolevel_names(new_name);
ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_ward2001 c
           SET name = (SELECT new_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.ward2001)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

UPDATE t_rif40_geolevels_geometry_ew01_ward2001 c
           SET name = (SELECT new_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.area_id)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

 */
			sql_stmt:='DROP TABLE IF EXISTS temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS /* Lookup table de-duplicate update */'||E'\n'||
				'WITH a AS ('||E'\n'||
				E'\t'||'SELECT name, COUNT(*) AS total'||E'\n'||
				E'\t'||'  FROM '||quote_ident(LOWER(c2_rec.lookup_table))||E'\n'||
				E'\t'||' GROUP BY name'||E'\n'||
				E'\t'||'HAVING COUNT(*) > 1'||E'\n'||
				')'||E'\n'||
				'SELECT DISTINCT b.'||quote_ident(LOWER(c2_rec.geolevel_name))||
					', a.name'||
					', c.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||
					', a.name'||'||''(''||c.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||'||'')'' AS new_name'||E'\n'||
				'  FROM a, '||quote_ident(LOWER(c2_rec.lookup_table))||' b, ew2001_geography c'||E'\n'||
				' WHERE a.name'||' = b.'||quote_ident(LOWER(c2_rec.geolevel_name))||''||E'\n'||
				'   AND c.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = b.'||quote_ident(LOWER(c2_rec.geolevel_name));

			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.geolevel_name))||'_pk ON'||
				' temp_fix_null_geolevel_names('||quote_ident(LOWER(c2_rec.geolevel_name))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.geolevel_name))||'_uk ON temp_fix_null_geolevel_names(new_name)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='ANALYZE VERBOSE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
			sql_stmt:='UPDATE /* d.1 */ '||quote_ident(LOWER(c2_rec.lookup_table))||' c'||E'\n'||
				'   SET name = (SELECT new_name /* Replacement 3 */ '||E'\n'||
				'		  FROM temp_fix_null_geolevel_names a'||E'\n'||
				'		 WHERE a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = c.'||quote_ident(LOWER(c2_rec.geolevel_name))||')'||E'\n'||
				' WHERE c.'||quote_ident(LOWER(c2_rec.geolevel_name))||' IN ('||E'\n'||
				E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||E'\n'||
				E'\t'||'  FROM temp_fix_null_geolevel_names)';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
				'Pass: 2d Geography % fixed geolevel %: % resolution duplicate lookup table % % descriptions', 
				l_geography::VARCHAR			/* Geography */, 
				c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
				c2_rec.lookup_table::VARCHAR		/* Lookup table */,
				l_rows::VARCHAR				/* Rows updated */);
--
			sql_stmt:='UPDATE /* d.2 */ '||quote_ident('t_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))||' /* Geometry table */ c'||E'\n'||
				'   SET name = (SELECT new_name /* Replacement 4 */ '||E'\n'||
				'		  FROM temp_fix_null_geolevel_names a'||E'\n'||
				'		 WHERE a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = c.area_id)'||E'\n'||
				' WHERE c.area_id IN ('||E'\n'||
				E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||E'\n'||
				E'\t'||'  FROM temp_fix_null_geolevel_names)';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
				'Pass: 2d Geography % fixed geolevel %: % resolution duplicate geolevel geometry % % descriptions', 
				l_geography::VARCHAR			/* Geography */, 
				c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
				quote_ident('t_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))::VARCHAR /* Geometry table */,
				l_rows::VARCHAR				/* Rows updated */);
--
			sql_stmt:='DROP TABLE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			re_analyze:=TRUE;

--
-- e) Missing single item top level - fix using geography description
--
		ELSIF c2_rec.geolevel_id = 1 AND c3_rec.total_area_ids = 1 THEN /* Fix top level lookup */
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'fix_null_geolevel_names',
				'Pass: 2f Geography % geolevel % lookup table: % missing single item top level %/% incomplete description rows (%) - FIX ', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				c3_rec.total_area_ids::VARCHAR			/* Geolevel lookup table unique name rows (AREA_IDs) */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);

			sql_stmt:='UPDATE /* e.1 */ '||quote_ident(LOWER(c2_rec.lookup_table))||E'\n'||
				  '   SET '||quote_ident(LOWER(c2_rec.lookup_desc_column))||' = '||quote_literal(l_geography);
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
				'Pass: 2e Geography % fixed geolevel %: % lowest resolution lookup table % % description', 
				l_geography::VARCHAR			/* Geography */, 
				c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
				c2_rec.lookup_desc_column::VARCHAR	/* Lookup table */,
				l_rows::VARCHAR				/* Rows updated */);
--
-- cannot ALTER TABLE "sahsuland_level1" because it is being used by active queries in this session
--
--			sql_stmt:='ALTER TABLE '||quote_ident(LOWER(c2_rec.lookup_table))||' ALTER COLUMN '||quote_ident(LOWER(c2_rec.lookup_desc_column))||' SET NOT NULL';
--			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Re-create unique index
--
			sql_stmt:='DROP INDEX IF EXISTS '||quote_ident(LOWER(c2_rec.lookup_table)||'_uk2');
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.lookup_table)||'_uk2')||
				' ON '||quote_ident(LOWER(c2_rec.lookup_table))||'('||quote_ident(LOWER(c2_rec.lookup_desc_column))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Fix top level shapefile if possible
--
			IF c2_rec.shapefile_desc_column IS NOT NULL THEN 
				sql_stmt:='UPDATE /* e.2 */ '||quote_ident(LOWER(c2_rec.shapefile_table))||E'\n'||
				  '   SET '||quote_ident(LOWER(c2_rec.shapefile_desc_column))||
					' = (SELECT description FROM rif40_geographies WHERE geography = '''||l_geography||''')';
				l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
					'Pass: 2e Geography % fixed geolevel %: % lowest resolution shapefile table % description', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
					l_rows::VARCHAR				/* Rows updated */);
			END IF;
--
-- Fix geometry table (it must exist)
--
			sql_stmt:='UPDATE /* e.3 */ '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||E'\n'||
				  '   SET name = (SELECT description FROM rif40_geographies WHERE geography = '''||l_geography||''')'||E'\n'||
				  ' WHERE geolevel_name = '''||c2_rec.geolevel_name||'''';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
				'Pass: 2e Geography % fixed geolevel %: % lowest resolution % % descriptions', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_id::VARCHAR			/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR			/* Curent geolevel name */, 
				't_rif40_'||LOWER(l_geography)||'_geometry'	/* Geometry table */,
				l_rows::VARCHAR					/* Rows updated */);
			re_analyze:=TRUE;
--
-- f) Level 2 (second lowest) resolution geolevel must be complete
--
		ELSIF c2_rec.geolevel_id = 2 AND c3_rec.total_desc != c3_rec.total_area_ids THEN
			PERFORM rif40_log_pkg.rif40_error(-10025, 'fix_null_geolevel_names',
				'Pass: 2f Geography % geolevel % lookup table: % has %/% incomplete description rows (%) - FIX ', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				c3_rec.total_area_ids::VARCHAR			/* Geolevel lookup table unique name rows (AREA_IDs) */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
--
-- g) Fix Level 3 (third lowest) resolution geolevel and so on if required
--
/*
CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
	SELECT DISTINCT a.ladua2001 -* source *-, a.oa2001 -* target *-, b2.name||' OA2001('||COALESCE(a.oa2001, 'UNK')||')' AS oa2001_name
          FROM ew2001_geography -* Hierarchy table *- a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                -* highest resolution geolevel with complete descriptive names *-
         WHERE NOT EXISTS (
                SELECT b1.name
                  FROM ew2001_coa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.oa2001);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_coa2001 c
           SET name = (SELECT oa2001_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.oa2001)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

 */
		ELSIF c3_rec.total_desc != c3_rec.total_area_ids THEN
			sql_stmt:='DROP TABLE IF EXISTS temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS /* Lookup table update */'||E'\n'||
				E'\t'||'SELECT DISTINCT a.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||' /* source */, '||
					'a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' /* target */, b2.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||
					'||'' '||UPPER(c2_rec.geolevel_name)||'(''||COALESCE(a.'||quote_ident(LOWER(c2_rec.geolevel_name))||', ''UNK'')||'')'' AS '||
					quote_ident(LOWER(c2_rec.geolevel_name)||'_name')||E'\n'||
				E'\t'||'  FROM '||quote_ident(LOWER(c6_rec.hierarchytable))||' /* Hierarchy table */ a'||E'\n'||
				E'\t'||'	LEFT OUTER JOIN '||quote_ident(LOWER(higher_c4_rec.lookup_table))||' b2 ON '||
					'(a.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||' = b2.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||')'||E'\n'||
				E'\t'||E'\t'||'/* highest resolution geolevel with complete descriptive names */'||E'\n'||
				E'\t'||' WHERE NOT EXISTS ('||E'\n'||
				E'\t'||E'\t'||'SELECT b1.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||E'\n'||
				E'\t'||E'\t'||'  FROM '||quote_ident(LOWER(c2_rec.lookup_table))||' b1'||E'\n'||
				E'\t'||E'\t'||' WHERE b1.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||' IS NOT NULL'||E'\n'||
				E'\t'||E'\t'||'   AND a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' /* target */ = '||
					'b1.'||quote_ident(LOWER(c2_rec.geolevel_name))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.geolevel_name))||'_pk ON'||
				' temp_fix_null_geolevel_names('||quote_ident(LOWER(c2_rec.geolevel_name))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='ANALYZE VERBOSE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='UPDATE /* f.1 */ '||quote_ident(LOWER(c2_rec.lookup_table))||' c'||E'\n'||
				'   SET name = (SELECT '||quote_ident(LOWER(c2_rec.geolevel_name)||'_name')||' /* Replacement 1 */ '||E'\n'||
				'		 FROM temp_fix_null_geolevel_names a'||E'\n'||
				'		WHERE a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = c.'||quote_ident(LOWER(c2_rec.geolevel_name))||')'||E'\n'||
				' WHERE c.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||' IS NULL';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Check l_rows is NOT 0 i.e. no rows were updated
--
			IF l_rows = 0 THEN
				PERFORM rif40_sql_pkg.rif40_method4('SELECT * FROM temp_fix_null_geolevel_names LIMIT 100', 
					'Pass: 2f Update 1 Dump of temp_fix_null_geolevel_names');
				PERFORM rif40_log_pkg.rif40_error(-10028, 'fix_null_geolevel_names', 
					'Pass: 2f Update 1 Geography % fixed geolevel %: % resolution lookup table % no rows updated', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */,
					c2_rec.lookup_table::VARCHAR		/* Lookup table */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
					'Pass: 2f Geography % fixed geolevel %: % resolution lookup table % % descriptions', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
					c2_rec.lookup_table::VARCHAR		/* Lookup table */,
					l_rows::VARCHAR				/* Rows updated */);
			END IF;
			sql_stmt:='DROP TABLE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);

/*
CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
SELECT DISTINCT a.ladua2001 -* source *-, a.oa2001 -* target *-, b2.name||' OA2001('||COALESCE(a.oa2001, 'UNK')||')' AS oa2001_name
          FROM ew2001_geography -* Hierarchy table *- a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                -* highest resolution geolevel with complete descriptive names *-
         WHERE NOT EXISTS (
                SELECT b1.area_id
                  FROM t_rif40_geolevels_geometry_ew01_oa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.area_id);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE t_rif40_geolevels_geometry_ew01_oa2001 c
           SET name = (SELECT oa2001_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.area_id)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

 */
			sql_stmt:='CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS /* Geolevel geometry table update */'||E'\n'||
				E'\t'||'SELECT DISTINCT a.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||' /* source */, '||
					'a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' /* target */, b2.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||
					'||'' '||UPPER(c2_rec.geolevel_name)||'(''||COALESCE(a.'||quote_ident(LOWER(c2_rec.geolevel_name))||', ''UNK'')||'')'' AS '||
					quote_ident(LOWER(c2_rec.geolevel_name)||'_name')||E'\n'||
				E'\t'||'  FROM '||quote_ident(LOWER(c6_rec.hierarchytable))||' /* Hierarchy table */ a'||E'\n'||
				E'\t'||'	LEFT OUTER JOIN '||quote_ident(LOWER(higher_c4_rec.lookup_table))||' b2 ON '||
					'(a.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||' = b2.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||')'||E'\n'||
				E'\t'||E'\t'||'/* highest resolution geolevel with complete descriptive names */'||E'\n'||
				E'\t'||' WHERE NOT EXISTS ('||E'\n'||
				E'\t'||E'\t'||'SELECT b1.area_id'||E'\n'||
				E'\t'||E'\t'||'  FROM '||quote_ident('t_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))||' b1 /* Geometry table */'||E'\n'||
				E'\t'||E'\t'||' WHERE b1.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||' IS NOT NULL'||E'\n'||
				E'\t'||E'\t'||'   AND a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' /* target */ = b1.area_id)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.geolevel_name))||'_pk ON'||
				' temp_fix_null_geolevel_names('||quote_ident(LOWER(c2_rec.geolevel_name))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='ANALYZE VERBOSE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='UPDATE /* f.2 */ '||quote_ident('t_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))||' c /* Geometry table */'||E'\n'||
				'   SET name = (SELECT '||quote_ident(LOWER(c2_rec.geolevel_name)||'_name')||' /* Replacement 2 */ '||E'\n'||
				'		 FROM temp_fix_null_geolevel_names a'||E'\n'||
				'		WHERE a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = c.area_id)'||E'\n'||
				' WHERE c.name IS NULL';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Check l_rows is NOT 0 i.e. no rows were updated
--
			IF l_rows = 0 THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'fix_null_geolevel_names', 
					'Pass: 2f Update 2 Geography % fixed geolevel %: % resolution geometry table % no rows updated', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */,
					quote_ident('t_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))::VARCHAR /* Geometry table */);
				PERFORM rif40_sql_pkg.rif40_method4('SELECT * FROM temp_fix_null_geolevel_names LIMIT 100', 
					'Pass: 2f Update 2 Dump of temp_fix_null_geolevel_names');
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
					'Pass: 2f Geography % fixed geolevel %: % resolution geometry table % % descriptions', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
					quote_ident('t_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))::VARCHAR /* Geometry table */,
					l_rows::VARCHAR				/* Rows updated */);
			END IF;
--
			sql_stmt:='DROP TABLE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Re-create unique index
--
			sql_stmt:='DROP INDEX IF EXISTS '||quote_ident(LOWER(c2_rec.lookup_table)||'_uk2');
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.lookup_table)||'_uk2')||
				' ON '||quote_ident(LOWER(c2_rec.lookup_table))||'('||quote_ident(LOWER(c2_rec.lookup_desc_column))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Unexpected logical condition
--
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-10029, 'fix_null_geolevel_names', 
				'Pass: 2 Geography % fixed geolevel %: % Unexpected logical condition in first loop; total_area_ids: %, total_desc: %, total_null_desc: %, total_uniq_desc: %', 
				l_geography::VARCHAR			/* Geography */, 
				c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
				c3_rec.total_area_ids::VARCHAR,
				c3_rec.total_desc::VARCHAR,
				c3_rec.total_null_desc::VARCHAR,
				c3_rec.total_uniq_desc::VARCHAR);
		END IF;

--
--
-- Re-analyze lookup and shapefile tables
--
		IF re_analyze THEN
			sql_stmt:='ANALYZE VERBOSE '||quote_ident(LOWER(c2_rec.lookup_table));
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
			sql_stmt:='ANALYZE VERBOSE '||quote_ident(LOWER(c2_rec.shapefile_table));
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
		END IF;
	END LOOP;
--
-- Check geolevels were processed
--
	IF j = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10049, 'fix_null_geolevel_names', 
			'No geolevels were processed in: t_rif40_geolevels for geography: %, expected geolevels: %',
			l_geography::VARCHAR 	/* Geography */,
			total_geolevel::VARCHAR	/* Expected geolevels */);
	END IF;
--
-- Re-create unique index
--
	sql_stmt:='DROP INDEX IF EXISTS '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry_uk2');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry_uk2')||
		' ON '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||'(name)';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Re-analyze geometry tables
--
	sql_stmt:='ANALYZE VERBOSE '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names(VARCHAR) IS 'Function: 	fix_null_geolevel_names()
Parameters:	geography
Returns:	Nothing
Description:	Fix NULL geolevel names in geography geolevel geometry and lookup table data, re-analyze
		Fix non-unique names in lookup tables and geolevel geometry table
		Add unique index

Fix NULL geolevel names in lookup table data example SQL>

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
	SELECT DISTINCT a.ladua2001 /* source */, a.oa2001 /* target */, b2.name||'' OA2001(''||COALESCE(a.oa2001, ''UNK'')||'')'' AS oa2001_name
          FROM ew2001_geography /* Hierarchy table */ a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                /* highest resolution geolevel with complete descriptive names */
         WHERE NOT EXISTS (
                SELECT b1.name
                  FROM ew2001_coa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.oa2001);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_coa2001 c
           SET name = (SELECT oa2001_name /* Replacement */ 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.oa2001)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

Fix NULL geolevel names in geography geolevel geometry data example SQL>

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
SELECT DISTINCT a.ladua2001 /* source */, a.oa2001 /* target */, b2.name||'' OA2001(''||COALESCE(a.oa2001, ''UNK'')||'')'' AS oa2001_name
          FROM ew2001_geography /* Hierarchy table */ a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                /* highest resolution geolevel with complete descriptive names */
         WHERE NOT EXISTS (
                SELECT b1.area_id
                  FROM t_rif40_geolevels_geometry_ew01_oa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.area_id);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE t_rif40_geolevels_geometry_ew01_oa2001 c
           SET name = (SELECT oa2001_name /* Replacement */ 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.area_id)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

Fix non-unique names in lookup tables and geolevel geometry table example SQL

DROP TABLE IF EXISTS temp_fix_null_geolevel_names;

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS
WITH a AS (
	SELECT name, COUNT(*) AS total
	  FROM ew2001_ward2001
	 GROUP BY name
	HAVING COUNT(*) > 1
)
SELECT DISTINCT b.ward2001, a.name, c.ladua2001, a.name||''(''||c.ladua2001||'')'' AS new_name
  FROM a, ew2001_ward2001 b, ew2001_geography c
 WHERE a.name = b.name
   AND c.ward2001 = b.ward2001;

CREATE UNIQUE INDEX ward2001_pk ON temp_fix_null_geolevel_names(ward2001);
CREATE UNIQUE INDEX ward2001_uk ON temp_fix_null_geolevel_names(new_name);
ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_ward2001 c
           SET name = (SELECT new_name /* Replacement */ 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.ward2001)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

UPDATE t_rif40_geolevels_geometry_ew01_ward2001 c
           SET name = (SELECT new_name /* Replacement */ 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.area_id)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);
';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.simplify_geometry(
	l_geography VARCHAR, l_min_point_resolution NUMERIC DEFAULT 1 /* metre */)
RETURNS void 
SECURITY INVOKER
AS $body$
DECLARE
/*
Function: 	simplify_geometry()
Parameters:	Geography, 
                minimum point resolution (default 1 - assumed metre, but depends on the geometry), 
                override for rif40_geoelvels.st_simplify_tolerance
Returns:	Nothing
Description:	Simplify geography geometry. Carried out in three phases:

Phase III: Create the polygons table, Update spatial geolevel table
 */
	c1_sg	CURSOR FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography;
	c1_rec RECORD;
--
	sql_stmt	VARCHAR[];
--
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
BEGIN
--
	stp:=clock_timestamp();
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'simplify_geometry', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	FOR c1_rec IN c1_sg LOOP
		PERFORM rif40_geo_pkg.simplify_geometry(c1_rec.geography, c1_rec.geolevel_name, 
			NULL /* Filter */, 
			l_min_point_resolution /* minimum point resolution (default 1 - assumed metre, but depends on the geometry) */,
			c1_rec.st_simplify_tolerance /* Default for geolevel */);
	END LOOP;
--
-- Drop temporary tables
--
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_lines';
	sql_stmt[2]:='DROP TABLE IF EXISTS simplification_points';
	sql_stmt[3]:='DROP TABLE IF EXISTS simplification_polygons';
	sql_stmt[4]:='DROP TABLE IF EXISTS simplification_lines_join_duplicates';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'simplify_geometry', 'Simplified % geography in: %', 
		l_geography::VARCHAR, 
		took::VARCHAR);
--
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, NUMERIC) IS 'Function: 	simplify_geometry()
Parameters:	Geography, 
                minimum point resolution (default 1 - assumed metre, but depends on the geometry), 
                override for rif40_geolevels.st_simplify_tolerance
Returns:	Nothing
Description:	Simplify geography geometry. Carried out in three phases:

Phase III: Create the polygons table, Update spatial geolevel table';


CREATE OR REPLACE FUNCTION rif40_geo_pkg.simplify_geometry(
	l_geography VARCHAR, l_geolevel VARCHAR, l_filter VARCHAR DEFAULT NULL, 
	l_min_point_resolution NUMERIC DEFAULT 1 /* metre */, l_st_simplify_tolerance NUMERIC DEFAULT 1)
RETURNS void 
SECURITY INVOKER
AS $body$
DECLARE
/*
 */
	sql_stmt	VARCHAR[];
--
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
--
	error_message 	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
	stp:=clock_timestamp();
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'simplify_geometry', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Phase I: Create the points table
--
-- Create a sequence so that points are in the original order specified by ST_DumpPoints()
-- Convert geometry to points, reduce precision to 1m
-- Test 1 - check last_pointflow = one per area (should be zero)
-- Detect and classify duplcates within co-ordinates (a side effect of the round)
--
-- O: original - first or last point in area
-- N: Not a dupicate
-- Y: A dupicate (two or more consecutive points sharing the same co-ordinate); removed later
-- L: Loop duplicate (two or more NON consecutive points sharing the same co-ordinate)
--
-- Count up number of distinct area_id using the same co-ordinate
-- Test 2 for un-detected duplicates
-- Join up adjacent edges
-- Create the points table
-- Test 3 and 4 - Checks - MUST BE 0
--	
-- a) Areas with no join sequence
-- b) Areas with mis-joined sequences
--
-- Create a list of joined_area_ids (i.e. the num_join_seq is 1)
-- Update from list of joined_area_ids (i.e. the num_join_seq is 1)
--
	PERFORM rif40_geo_pkg._simplify_geometry_phase_I(l_geography, l_geolevel, l_filter, l_min_point_resolution, l_st_simplify_tolerance);
--
-- Phase II: Create the lines table
--
	PERFORM rif40_geo_pkg._simplify_geometry_phase_II(l_geography, l_geolevel, l_filter, l_min_point_resolution, l_st_simplify_tolerance);
--
-- Phase III: Create the polygons table
--
	PERFORM rif40_geo_pkg._simplify_geometry_phase_III(l_geography, l_geolevel, l_filter, l_min_point_resolution, l_st_simplify_tolerance);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'simplify_geometry', 'Simplified % geography geolevel %: %', 
		l_geography::VARCHAR, 
		l_geolevel::VARCHAR, 
		took::VARCHAR);
--
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
--		IF v_version THEN
--			GET STACKED DIAGNOTICS v_detail = PG_EXCETION_DETAIL;
--		END IF;
		error_message:='simplify_geometry('||l_geography::VARCHAR||', '||l_geolevel::VARCHAR||') caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
--
		RAISE;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC) IS 'Function: 	simplify_geometry()
Parameters:	Geography, geolevel, 
                geolevel; filter (for testing, no default), 
                minimum point resolution (default 1 - assumed metre, but depends on the geometry), 
                override for rif40+geoelvelsl_st_simplify_tolerance
Returns:	Nothing
Description:	Simplify geography geolevel';

--
-- Simplification sub-programs (now run separately)
--
--\i  ../PLpgsql/v4_0_rif40_geo_pkg_simplification.sql

CREATE OR REPLACE FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	add_population_to_rif40_geolevels_geometry()
Parameters:	None
Returns:	Nothing
Description:	Add denominator population table to geography geolevel geometry data, re-analyze
 */
DECLARE
	c1_adpop2 CURSOR(l_geography VARCHAR) FOR
		SELECT denominator_table, COUNT(*) total_numerators 
		  FROM rif40_num_denom
		 WHERE geography = l_geography
		   AND automatic =1
		 GROUP BY denominator_table;
	c2_adpop2 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
--
	c2_rec rif40_geographies%ROWTYPE;
	c1_rec RECORD;
--
	i INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'add_population_to_rif40_geolevels_geometry', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Create lookup and Hierarchy tables
--
	FOR c2_rec IN c2_adpop2 LOOP
		i:=0;
		FOR c1_rec IN c1_adpop2(c2_rec.geography) LOOP
			i:=i+1;
			IF i = 1 THEN
				PERFORM rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(c2_rec.geography, c1_rec.denominator_table);
			END IF;
		END LOOP;
--
		IF i = 0 THEN
--
-- Demoted to a warning for EW01 geography testing
--
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'add_population_to_rif40_geolevels_geometry', 'No automatic denominators for geography: %', 
				c2_rec.geography::VARCHAR	/* Geography */);
--			PERFORM rif40_log_pkg.rif40_error(-10052, 'add_population_to_rif40_geolevels_geometry', 'No automatic denominators for geography: %', 
--				c2_rec.geography::VARCHAR	/* Geography */);
		ELSIF i > 1 THEN
			PERFORM rif40_log_pkg.rif40_error(-10051, 'add_population_to_rif40_geolevels_geometry', '>1 (%) automatic denominators for geography: %', 
				c2_rec.geography::VARCHAR	/* Geography */,
				i::VARCHAR			/* Number of automatic denominators */);
		END IF;
	END LOOP;
--
	IF i = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10053, 'add_population_to_rif40_geolevels_geometry', 'No geographies found');
	END IF;
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry() IS 'Function: 	add_population_to_rif40_geolevels_geometry()
Parameters:	None
Returns:	Nothing
Description:	Add denominator population table to geography geolevel geometry data, re-analyze';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(l_geography VARCHAR, l_population_table VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	add_population_to_rif40_geolevels_geometry()
Parameters:	geography, denominator population table
Returns:	Nothing
Description:	Add denominator population table to geography geolevel geometry data, re-analyze

Example update statement:

WITH a AS (
	SELECT MAX(year) AS max_year 
	  FROM sahsuland_pop
), b AS (
	SELECT level2, TRUNC(age_sex_group/100) AS sex, year AS population_year, SUM(total) AS total
	  FROM sahsuland_pop b, a
	 WHERE b.year = a.max_year
	 GROUP BY level2, TRUNC(age_sex_group/100), year
), c AS (
	SELECT b1.level2, b1.total AS total_males, b2.total AS total_females, b1.population_year
	  FROM b b1, b b2
	 WHERE b1.level2 = b2.level2
	   AND b1.sex    = 1
	   AND b2.sex    = 2
)
UPDATE t_rif40_sahsu_geometry d
   SET total_males = (
	SELECT total_males
	  FROM c
	 WHERE c.level2 = d.area_id),
       total_females = (
	SELECT total_females
	  FROM c
	 WHERE c.level2 = d.area_id),
       population_year = (
	SELECT population_year
	  FROM c
	 WHERE c.level2 = d.area_id)
 WHERE d.geolevel_name = 'LEVEL2';

To test:

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL2';

EW01 test SELECT

WITH a AS (
	SELECT MAX(year) AS max_year 
	  FROM v_ew01_rif_pop_asg_1_oa2001
), b AS (
	SELECT ladua2001, TRUNC(age_sex_group/100) AS sex, year AS population_year, SUM(total) AS total
	  FROM v_ew01_rif_pop_asg_1_oa2001 b, a
	 WHERE b.year = a.max_year
	 GROUP BY ladua2001, TRUNC(age_sex_group/100), year
), c AS (
	SELECT b1.ladua2001, b1.total AS total_males, b2.total AS total_females, b1.population_year
	  FROM b b1, b b2
	 WHERE b1.ladua2001 = b2.ladua2001
	   AND b1.sex    = 1
	   AND b2.sex    = 2
)
SELECT * 
  FROM c;

 */
DECLARE
	c1_adpop CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id;
	c2_adpop CURSOR(l_population_table VARCHAR) FOR
		SELECT *
		  FROM rif40_tables
		 WHERE table_name = l_population_table;
	c3_adpop CURSOR(l_population_table VARCHAR, l_schema VARCHAR, l_column VARCHAR) FOR
		SELECT *
		  FROM information_schema.columns
		 WHERE table_name   = LOWER(l_population_table)
		   AND table_schema = l_schema
		   AND column_name  = LOWER(l_column);
	c1_rec t_rif40_geolevels%ROWTYPE;
	c2_rec rif40_tables%ROWTYPE;
	c3_rec information_schema.columns%ROWTYPE;
--
	l_schema VARCHAR;
	sql_stmt VARCHAR;
	i INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'add_population_to_rif40_geolevels_geometry', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR			/* Username */);
	ELSIF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10041, 'add_population_to_rif40_geolevels_geometry', 'Null geography');
	ELSIF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10042, 'add_population_to_rif40_geolevels_geometry', 'Null population table for geography: %', 
			USER::VARCHAR			/* Geography */);
	END IF;
--
-- Check population table is a valid RIF denominator and exists
--
	OPEN c2_adpop(l_population_table);
	FETCH c2_adpop INTO c2_rec;
	CLOSE c2_adpop;
	IF c2_rec.table_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10018, 'add_population_to_rif40_geolevels_geometry', 'Geography: % Population table: % is not a RIF table', 
			l_geography::VARCHAR		/* Geography */,
			l_population_table::VARCHAR	/* Population table */);
	ELSIF c2_rec.isindirectdenominator != 1 THEN
		PERFORM rif40_log_pkg.rif40_error(-10019, 'add_population_to_rif40_geolevels_geometry', 'Geography: % Population table: % is a RIF table, but not an indirect denominator', 
			l_geography::VARCHAR		/* Geography */,
			l_population_table::VARCHAR	/* Population table */);
	ELSIF rif40_sql_pkg.rif40_is_object_resolvable(l_population_table) != 1 THEN
		PERFORM rif40_log_pkg.rif40_error(-10020, 'add_population_to_rif40_geolevels_geometry', 
			'Geography: % Population table: % is a RIF indirect denominator but is not resolvable', 
			l_geography::VARCHAR		/* Geography */,
			l_population_table::VARCHAR	/* Population table */);
	END IF;
	l_schema:=rif40_sql_pkg.rif40_object_resolve(l_population_table);
--
-- Do population update once per geolevel
--
	FOR c1_rec IN c1_adpop(l_geography) LOOP
--
-- Check geography geolevel is supported
--
		OPEN c3_adpop(l_population_table, l_schema, c1_rec.geolevel_name);
		FETCH c3_adpop INTO c3_rec;
		CLOSE c3_adpop;
		IF c3_rec.column_name IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-10021, 'add_population_to_rif40_geolevels_geometry', 
				'Population table: %.% does not have % geography geolevel column: %', 
				l_schema::VARCHAR			/* Schema */, 
				LOWER(l_population_table)::VARCHAR	/* Population table */, 
				l_geography::VARCHAR			/* Geography */, 
				LOWER(c1_rec.geolevel_name)::VARCHAR	/* Geolevel name */);
		END IF;
--
-- Create UPDATE statement
--
		sql_stmt:='WITH a AS ('||E'\n'||
			  E'\t'||'SELECT MAX(year) AS max_year'||E'\n'|| 
			  E'\t'||'  FROM '||quote_ident(LOWER(l_population_table))||E'\n'||
			  '), b AS ('||E'\n'||
			  E'\t'||'SELECT '||quote_ident(LOWER(c1_rec.geolevel_name))||', TRUNC(age_sex_group/100) AS sex, year AS population_year, SUM(total) AS total'||E'\n'||
			  E'\t'||'  FROM '||quote_ident(LOWER(l_population_table))||' b, a'||E'\n'||
			  E'\t'||' WHERE b.year = a.max_year'||E'\n'||
			  E'\t'||' GROUP BY '||quote_ident(LOWER(c1_rec.geolevel_name))||', TRUNC(age_sex_group/100), year'||E'\n'||
			  '), c AS ('||E'\n'||
			  E'\t'||'SELECT b1.'||quote_ident(LOWER(c1_rec.geolevel_name))||', b1.total AS total_males, b2.total AS total_females, b1.population_year'||E'\n'||
			  E'\t'||'  FROM b b1, b b2'||E'\n'||
			  E'\t'||' WHERE b1.'||quote_ident(LOWER(c1_rec.geolevel_name))||' = b2.'||quote_ident(LOWER(c1_rec.geolevel_name))||''||E'\n'||
			  E'\t'||'   AND b1.sex    = 1'||E'\n'||
	 		  E'\t'||'   AND b2.sex    = 2'||E'\n'||
			  ')'||E'\n'||
			  'UPDATE '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||' d'||E'\n'||
			  '   SET total_males = ('||E'\n'||
			  E'\t'||'SELECT total_males'||E'\n'||
			  E'\t'||'  FROM c'||E'\n'||
			  E'\t'||' WHERE c.'||quote_ident(LOWER(c1_rec.geolevel_name))||' = d.area_id),'||E'\n'||
			  '       total_females = ('||E'\n'||
			  E'\t'||'SELECT total_females'||E'\n'||
			  E'\t'||'  FROM c'||E'\n'||
			  E'\t'||' WHERE c.'||quote_ident(LOWER(c1_rec.geolevel_name))||' = d.area_id),'||E'\n'||
			  '       population_year = ('||E'\n'||
			  E'\t'||'SELECT population_year'||E'\n'||
			  E'\t'||'  FROM c'||E'\n'||
			  E'\t'||' WHERE c.'||quote_ident(LOWER(c1_rec.geolevel_name))||' = d.area_id)'||E'\n'||
 			  ' WHERE d.geolevel_name = '''||c1_rec.geolevel_name||'''';
		IF sql_stmt IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-10037, 'add_population_to_rif40_geolevels_geometry', 'NULL SQL statement for %.%',
				c1_rec.geography::VARCHAR		/* Geography */,
				c1_rec.geolevel_name::VARCHAR		/* Geolevel name */);
		END IF;
		i:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'add_population_to_rif40_geolevels_geometry', 'SQL> %', sql_stmt::VARCHAR);
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
--
-- Check geolevels were processed
--
	IF i = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10017, 'add_population_to_rif40_geolevels_geometry', 'Geography: % No rows found in: t_rif40_geolevels',
			l_geography::VARCHAR		/* Geography */);
	END IF;
--
-- Re-analyze
--
	sql_stmt:='ANALYZE VERBOSE '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry');
	IF sql_stmt IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10038, 'add_population_to_rif40_geolevels_geometry', 'NULL SQL statement for Geography: %',
			l_geography::VARCHAR		/* Geography */);
	END IF;
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(VARCHAR, VARCHAR) IS 'Function: 	add_population_to_rif40_geolevels_geometry()
Parameters:	geography, denominator population table
Returns:	Nothing
Description:	Add denominator population table to geography geolevel geometry data, re-analyze

Example update statement:

WITH a AS (
	SELECT MAX(year) AS max_year 
	  FROM sahsuland_pop
), b AS (
	SELECT level2, TRUNC(age_sex_group/100) AS sex, year AS population_year, SUM(total) AS total
	  FROM sahsuland_pop b, a
	 WHERE b.year = a.max_year
	 GROUP BY level2, TRUNC(age_sex_group/100), year
), c AS (
	SELECT b1.level2, b1.total AS total_males, b2.total AS total_females, b1.population_year
	  FROM b b1, b b2
	 WHERE b1.level2 = b2.level2
	   AND b1.sex    = 1
	   AND b2.sex    = 2
)
UPDATE t_rif40_sahsu_geometry d
   SET total_males = (
	SELECT total_males
	  FROM c
	 WHERE c.level2 = d.area_id),
       total_females = (
	SELECT total_females
	  FROM c
	 WHERE c.level2 = d.area_id),
       population_year = (
	SELECT population_year
	  FROM c
	 WHERE c.level2 = d.area_id)
 WHERE d.geolevel_name = ''LEVEL2'';';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	drop_rif40_geolevels_geometry_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Drop rif40_geolevels_geometry tables

 */
DECLARE
	c1 CURSOR FOR
		SELECT * FROM information_schema.tables 
		 WHERE table_name LIKE 't_rif40_%_geometry' AND table_name != 't_rif40_geolevels_geometry';
	c1a CURSOR(l_trigger VARCHAR) FOR
		SELECT * FROM information_schema.triggers 
		 WHERE trigger_name = l_trigger;
	c1b CURSOR(l_routines VARCHAR) FOR
		SELECT * FROM information_schema.routines 
		 WHERE routine_name = l_routines;
--
	c1_rec information_schema.tables%ROWTYPE;
	c1a_rec information_schema.triggers%ROWTYPE;
	c1b_rec information_schema.routines%ROWTYPE;
--
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'drop_rif40_geolevels_geometry_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR /* Username */);
	END IF;
--
-- Drop existing objects
--
	FOR c1_rec IN c1 LOOP
		OPEN c1a(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1a INTO c1a_rec;
		CLOSE c1a;
		IF c1a_rec.trigger_name IS NOT NULL THEN
			sql_stmt:='DROP TRIGGER '||quote_ident(c1_rec.table_name||'_insert')||' ON '||c1_rec.table_name;
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		OPEN c1b(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1b INTO c1b_rec;
		CLOSE c1b;
		IF c1b_rec.routine_name IS NOT NULL THEN
			sql_stmt:='DROP FUNCTION '||quote_ident(c1_rec.table_name||'_insert')||'()';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		sql_stmt:='DROP TABLE '||quote_ident(c1_rec.table_name)||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables() IS 'Function: 	drop_rif40_geolevels_geometry_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Drop rif40_geolevels_geometry tables';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables(t_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	drop_rif40_geolevels_geometry_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Drop rif40_geolevels_geometry tables

 */
DECLARE
	c1 CURSOR(l_table_name VARCHAR) FOR
		SELECT * FROM information_schema.tables 
		 WHERE table_name = quote_ident(l_table_name) AND table_name != 't_rif40_geolevels_geometry';
	c1a CURSOR(l_trigger VARCHAR) FOR
		SELECT * FROM information_schema.triggers 
		 WHERE trigger_name = l_trigger;
	c1b CURSOR(l_routines VARCHAR) FOR
		SELECT * FROM information_schema.routines 
		 WHERE routine_name = l_routines;
--
	c1_rec information_schema.tables%ROWTYPE;
	c1a_rec information_schema.triggers%ROWTYPE;
	c1b_rec information_schema.routines%ROWTYPE;
--
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'drop_rif40_geolevels_geometry_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR /* Username */);
	END IF;
--
-- Drop existing objects
--
	FOR c1_rec IN c1('t_rif40_'||LOWER(t_geography)||'_geometry') LOOP
		OPEN c1a(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1a INTO c1a_rec;
		CLOSE c1a;
		IF c1a_rec.trigger_name IS NOT NULL THEN
			sql_stmt:='DROP TRIGGER '||quote_ident(c1_rec.table_name||'_insert')||' ON '||c1_rec.table_name;
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		OPEN c1b(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1b INTO c1b_rec;
		CLOSE c1b;
		IF c1b_rec.routine_name IS NOT NULL THEN
			sql_stmt:='DROP FUNCTION '||quote_ident(c1_rec.table_name||'_insert')||'()';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		sql_stmt:='DROP TABLE '||quote_ident(c1_rec.table_name)||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables(VARCHAR) IS 'Function: 	drop_rif40_geolevels_geometry_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Drop rif40_geolevels_geometry tables';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables(t_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	create_rif40_geolevels_geometry_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Create rif40_geolevels_geometry tables

ADD GENERATED SQL

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
			'optimised_geojson CHARACTER VARYING      NOT NULL,'||E'\n'||
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
		sql_stmt:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'.optimised_geojson 	IS ''Shapefile multipolygon in optimised GeoJSON format. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works onj an object by object basis; the edge between two areas will therefore be processed independently and not necessarily in the same manner). This can be fixed using the PostGIS Topology extension and processing as edges. See also TOPO_OPTIMISED_GEOJSON; i.e. GeoJson optimised using ST_ChangeEdgeGeometry() and ST_Simplify(). The SRID is always 4326.''';
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
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry_uk')||' ON '||quote_ident('t_rif40_'||LOWER(c2_rec.geography)||'_geometry')||'(geography, gid, area_id)';
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	check_rif40_hierarchy_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Diff geography hierarchy tables for 'missing' or 'spurious additional' or 'multiple hierarchy' geolevel codes
		Calls rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(geography)
 */
DECLARE
	c0 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
--
	c0_rec rif40_geographies%ROWTYPE;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'check_rif40_hierarchy_lookup_tables', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	FOR c0_rec IN c0 LOOP
		PERFORM rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(c0_rec.geography);
	END LOOP;
	PERFORM rif40_log_pkg.rif40_log('INFO', 'check_rif40_hierarchy_lookup_tables', 'All geography codes check OK');
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables() IS 'Function: 	check_rif40_hierarchy_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Diff geography hierarchy tables for ''missing'', ''spurious additional'' or ''multiple hierarchy'' geolevel codes
		Calls rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(geography)';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(l_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	check_rif40_hierarchy_lookup_tables()
Parameters:	Geography 
Returns:	Nothing
Description:	Diff geography hierarchy table for 'missing', 'spurious additional' or 'multiple hierarchy' geolevel codes

		Calls lf_check_rif40_hierarchy_lookup_tables() to do diff
 */
DECLARE
	c1 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c1_rec rif40_geographies%ROWTYPE;
--
	e INTEGER:=0;
	f INTEGER:=0;
	g INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'check_rif40_hierarchy_lookup_tables', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10003, 'check_rif40_hierarchy_lookup_tables', 'NULL geography parameter');
	END IF;	
--
	OPEN c1(l_geography);
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10004, 'check_rif40_hierarchy_lookup_tables', 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */);
	END IF;	
--
-- Call diff and multiple hierarchy tests
--
	e:=rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(c1_rec.geography, c1_rec.hierarchytable, 'missing');
	f:=rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(c1_rec.geography, c1_rec.hierarchytable, 'spurious additional');
	g:=rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(c1_rec.geography, c1_rec.hierarchytable, 'multiple hierarchy');
--
	IF e+f > 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10005, 'check_rif40_hierarchy_lookup_tables', 'Geography: % codes check % missing, % spurious additional, % hierarchy fails', 
			c1_rec.geography	/* Geography */, 
			e::VARCHAR		/* Missing */, 
			f::VARCHAR		/* Spurious additional */, 
			g::VARCHAR		/* Multiple hierarchy */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'check_rif40_hierarchy_lookup_tables', 'Geography: % codes check OK', 
			c1_rec.geography::VARCHAR	/* Geography */);
	END IF;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(VARCHAR) IS 'Function: 	check_rif40_hierarchy_lookup_tables()
Parameters:	Geography 
Returns:	Nothing
Description:	Diff geography hierarchy table for ''missing'', ''spurious additional'' or ''multiple hierarchy'' geolevel codes

		Calls lf_check_rif40_hierarchy_lookup_tables() to do diff';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(l_geography VARCHAR, l_hierarchytable VARCHAR, l_type VARCHAR)
RETURNS integer 
SECURITY INVOKER
AS $body$
/*

Function: 	lf_check_rif40_hierarchy_lookup_tables()
Parameters:	Geography, hierarchy table, type: 'missing', 'spurious additional' or 'multiple hierarchy'
Returns:	Nothing
Description:	Diff geography hierarchy table using dynamic method 4
		Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on

Example of dynamic SQL. Note the use of an array return type to achieve method 4

WITH /- missing -/ a1 AS (
        SELECT COUNT(*) AS level1_total
          FROM (
                SELECT level1 FROM sahsuland_geography
                EXCEPT
                SELECT level1 FROM sahsuland_level1) as1)
, a2 AS (
        SELECT COUNT(*) AS level2_total
          FROM (
                SELECT level2 FROM sahsuland_geography
                EXCEPT
                SELECT level2 FROM sahsuland_level2) as2)
, a3 AS (
        SELECT COUNT(*) AS level3_total
          FROM (
                SELECT level3 FROM sahsuland_geography
                EXCEPT
                SELECT level3 FROM sahsuland_level3) as3)
, a4 AS (
        SELECT COUNT(*) AS level4_total
          FROM (
                SELECT level4 FROM sahsuland_geography
                EXCEPT
                SELECT level4 FROM sahsuland_level4) as4)
SELECT ARRAY[a1.level1_total, a2.level2_total, a3.level3_total, a4.level4_total] AS res_array
FROM a1, a2, a3, a4;

Or: 

WITH -* hierarchy *- a2 AS (
        SELECT COUNT(*) AS level2_total
          FROM (
                SELECT level2, COUNT(DISTINCT(level1)) AS total
                  FROM sahsuland_geography
                 GROUP BY level2
                HAVING COUNT(DISTINCT(level1)) > 1) AS2)
, a3 AS (
        SELECT COUNT(*) AS level3_total
          FROM (
                SELECT level3, COUNT(DISTINCT(level2)) AS total
                  FROM sahsuland_geography
                 GROUP BY level3
                HAVING COUNT(DISTINCT(level2)) > 1) AS3)
, a4 AS (
        SELECT COUNT(*) AS level4_total
          FROM (
                SELECT level4, COUNT(DISTINCT(level3)) AS total
                  FROM sahsuland_geography
                 GROUP BY level4
                HAVING COUNT(DISTINCT(level3)) > 1) AS4)
SELECT ARRAY[a2.level2_total, a3.level3_total, a4.level4_total] AS res_array
FROM a2, a3, a4;
 
 */
DECLARE
	c2 CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geolevel_id;
	c3 REFCURSOR;
	c4 CURSOR(l_geography VARCHAR, l_geolevel_id INTEGER) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography   = l_geography
		   AND geolevel_id = l_geolevel_id
		 ORDER BY geolevel_id;
--
	c2_rec t_rif40_geolevels%ROWTYPE;
	c3_rec RECORD;
	c4_rec t_rif40_geolevels%ROWTYPE;
--
	sql_stmt 		VARCHAR;
	previous_geolevel_name 	VARCHAR:=NULL;
	i INTEGER;
	e INTEGER:=0;
	field INTEGER;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'lf_check_rif40_hierarchy_lookup_tables', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	sql_stmt:='WITH /* '||l_type||' */ ';
	i:=0;
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF l_type = 'multiple hierarchy' THEN
			IF i = 1 THEN
				NULL;
			ELSIF i > 2 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'SELECT COUNT(*) AS '||quote_ident(LOWER(c2_rec.geolevel_name)||'_total')||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'  FROM ('||E'\n';
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'SELECT COUNT(*) AS '||quote_ident(LOWER(c2_rec.geolevel_name)||'_total')||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'  FROM ('||E'\n';
			END IF;
		ELSE
			IF i != 1 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
			END IF;
			sql_stmt:=sql_stmt||E'\t'||'SELECT COUNT(*) AS '||quote_ident(LOWER(c2_rec.geolevel_name)||'_total')||E'\n';
			sql_stmt:=sql_stmt||E'\t'||'  FROM ('||E'\n';
		END IF;
		IF l_type = 'missing' THEN
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(l_hierarchytable))||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'EXCEPT'||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(c2_rec.lookup_table))||
				') '||quote_ident('as'||c2_rec.geolevel_id)||')'||E'\n';
		ELSIF l_type = 'spurious additional' THEN
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(c2_rec.lookup_table))||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'EXCEPT'||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(l_hierarchytable))||
				') '||quote_ident('as'||c2_rec.geolevel_id)||')'||E'\n';
		ELSIF l_type = 'multiple hierarchy' THEN
			IF previous_geolevel_name IS NOT NULL THEN
				sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||
					', COUNT(DISTINCT('||previous_geolevel_name||')) AS total'||E'\n';
				sql_stmt:=sql_stmt||E'\t'||E'\t'||'  FROM '||quote_ident(LOWER(l_hierarchytable))||E'\n';
				sql_stmt:=sql_stmt||E'\t'||E'\t'||' GROUP BY '||quote_ident(LOWER(c2_rec.geolevel_name))||E'\n';
				sql_stmt:=sql_stmt||E'\t'||E'\t'||'HAVING COUNT(DISTINCT('||previous_geolevel_name||')) > 1'||
					') '||quote_ident('as'||c2_rec.geolevel_id)||')'||E'\n';
			END IF;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-10017, 'lf_check_rif40_hierarchy_lookup_tables', 
				'Invalid check type: %, valid types are: ''missing'', ''spurious additional'', or ''multiple hierarchy''', 
				l_type::VARCHAR 	/* Check type */);
		END IF;
		previous_geolevel_name:=quote_ident(LOWER(c2_rec.geolevel_name));
	END LOOP;
	sql_stmt:=sql_stmt||'SELECT ARRAY[';
	i:=0;
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF l_type = 'multiple hierarchy' THEN
			IF i = 1 THEN
				NULL;
			ELSIF i > 2 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			END IF;
		ELSE
			IF i != 1 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			END IF;
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||'] AS res_array'||E'\n'||'FROM ';
	i:=0;
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF l_type = 'multiple hierarchy' THEN
			IF i = 1 THEN
				NULL;
			ELSIF i > 2 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id);
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id);
			END IF;
		ELSE
			IF i != 1 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id);
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id);
			END IF;
		END IF;
	END LOOP;
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'lf_check_rif40_hierarchy_lookup_tables', 'SQL> %;', sql_stmt::VARCHAR);
	OPEN c3 FOR EXECUTE sql_stmt;
	FETCH c3 INTO c3_rec;
--
-- Process results array
--
	i:=0;
	FOREACH field IN ARRAY c3_rec.res_array LOOP
		i:=i+1;
		OPEN c4(l_geography, i);
		FETCH c4 INTO c4_rec;
		CLOSE c4;
		IF field != 0 THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'lf_check_rif40_hierarchy_lookup_tables', 'Geography: % geolevel %: [%] % codes: %', 
				l_geography::VARCHAR			/* Geography */, 
				i::VARCHAR				/* Geolevel ID */, 
				LOWER(c4_rec.geolevel_name)::VARCHAR	/* Geolevel name */, 
				l_type::VARCHAR				/* Check type */, 
				field::VARCHAR				/* Area ID */);
			e:=e+1;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'lf_check_rif40_hierarchy_lookup_tables', 'Geography: % geolevel %: [%] no % codes', 
				l_geography::VARCHAR			/* Geography */, 
				i::VARCHAR				/* Geolevel ID */, 
				LOWER(c4_rec.geolevel_name)::VARCHAR	/* Geolevel name */, 
				l_type::VARCHAR				/* Check type */);
		END IF;
	END LOOP;
--
	RETURN e;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	lf_check_rif40_hierarchy_lookup_tables()
Parameters:	Geography, hierarchy table, type: ''missing'', ''spurious additional'' or ''multiple hierarchy''
Returns:	Nothing
Description:	Diff geography hierarchy table using dynamic method 4
		Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on

Example of dynamic SQL. Note the use of an array return type to achieve method 4

WITH /* missing */ a1 AS (
        SELECT COUNT(*) AS level1_total
          FROM (
                SELECT level1 FROM sahsuland_geography
                EXCEPT
                SELECT level1 FROM sahsuland_level1) as1)
, a2 AS (
        SELECT COUNT(*) AS level2_total
          FROM (
                SELECT level2 FROM sahsuland_geography
                EXCEPT
                SELECT level2 FROM sahsuland_level2) as2)
, a3 AS (
        SELECT COUNT(*) AS level3_total
          FROM (
                SELECT level3 FROM sahsuland_geography
                EXCEPT
                SELECT level3 FROM sahsuland_level3) as3)
, a4 AS (
        SELECT COUNT(*) AS level4_total
          FROM (
                SELECT level4 FROM sahsuland_geography
                EXCEPT
                SELECT level4 FROM sahsuland_level4) as4)
SELECT ARRAY[a1.level1_total, a2.level2_total, a3.level3_total, a4.level4_total] AS res_array
FROM a1, a2, a3, a4;

Or:

WITH /* hierarchy */ a2 AS (
        SELECT COUNT(*) AS level2_total
          FROM (
                SELECT level2, COUNT(DISTINCT(level1)) AS total
                  FROM sahsuland_geography
                 GROUP BY level2
                HAVING COUNT(DISTINCT(level1)) > 1) AS2)
, a3 AS (
        SELECT COUNT(*) AS level3_total
          FROM (
                SELECT level3, COUNT(DISTINCT(level2)) AS total
                  FROM sahsuland_geography
                 GROUP BY level3
                HAVING COUNT(DISTINCT(level2)) > 1) AS3)
, a4 AS (
        SELECT COUNT(*) AS level4_total
          FROM (
                SELECT level4, COUNT(DISTINCT(level3)) AS total
                  FROM sahsuland_geography
                 GROUP BY level4
                HAVING COUNT(DISTINCT(level3)) > 1) AS4)
SELECT ARRAY[a2.level2_total, a3.level3_total, a4.level4_total] AS res_array
FROM a2, a3, a4;

';

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
        SELECT 'SCNTRY2001' AS geolevel_name, scntry2001 AS area_id,         name AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_scntry2001
         GROUP BY scntry2001, name
        UNION
        SELECT 'CNTRY2001' AS geolevel_name, cntry2001 AS area_id,           name AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_cntry2001
         GROUP BY cntry2001, name
        UNION
        SELECT 'GOR2001' AS geolevel_name, gor2001 AS area_id,       gorname AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_gor2001
         GROUP BY gor2001, gorname
        UNION
        SELECT 'LADUA2001' AS geolevel_name, ladua2001 AS area_id,           laduaname AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_ladua2001
         GROUP BY ladua2001, laduaname
        UNION
        SELECT 'WARD2001' AS geolevel_name, ward2001 AS area_id,             wardname AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_ward2001
         GROUP BY ward2001, wardname
        UNION
        SELECT 'SOA2001' AS geolevel_name, soa2001 AS area_id,       NULL AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_soa2001
         GROUP BY soa2001
        UNION
        SELECT 'OA2001' AS geolevel_name, coa2001 AS area_id,        NULL AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_coa2001
         GROUP BY coa2001
), b -* t_rif40_geolevels *- AS (
        SELECT geography, geolevel_name, geolevel_id AS gid, st_simplify_tolerance
          FROM t_rif40_geolevels
         WHERE geography = 'EW01'
), c -* rif40_geographies *- AS (
        SELECT max_geojson_digits
          FROM rif40_geographies
         WHERE geography = 'EW01'
), d AS (
        SELECT a.geolevel_name, geom, area_id, name, ST_MakeValid((ST_SimplifyPreserveTopology(geom, b.st_simplify_tolerance)) AS simplified_topology
          FROM a, b
         WHERE a.geolevel_name = b.geolevel_name
)
SELECT 'EW01' geography, b.geolevel_name, area_id, NULLIF(name, 'Unknown: ['||area_id||']') AS name, b.gid -* RIF40_GEOLEVELS.GEOLEVEL_ID *-,
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
		sql_stmt:=sql_stmt||E'\t'||'     ST_Union(CASE WHEN ST_IsValid(geom) = FALSE THEN ST_MakeValid(geom) ELSE geom END) AS geom	/* Make valid if required, Union polygons together */'||E'\n';
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
	sql_stmt:=sql_stmt||E'\t'||'SELECT geography, geolevel_name, geolevel_id AS gid, st_simplify_tolerance'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'  FROM t_rif40_geolevels'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' WHERE geography = '||quote_literal(c1_rec.geography)||E'\n';
	sql_stmt:=sql_stmt||'), c /* rif40_geographies */ AS ('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT max_geojson_digits'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'  FROM rif40_geographies'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' WHERE geography = '||quote_literal(c1_rec.geography)||E'\n';
	sql_stmt:=sql_stmt||'), d AS ('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT a.geolevel_name, geom, area_id, name,'||E'\n';
--	sql_stmt:=sql_stmt||E'\t'||'       ST_MakeValid(ST_SimplifyPreserveTopology(geom, b.st_simplify_tolerance)) AS simplified_topology'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'       ST_SimplifyPreserveTopology(geom, b.st_simplify_tolerance) AS simplified_topology'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'  FROM a, b'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' WHERE a.geolevel_name = b.geolevel_name'||E'\n';
	sql_stmt:=sql_stmt||')'||E'\n';
	sql_stmt:=sql_stmt||'SELECT '||quote_literal(c1_rec.geography)||
		' geography, b.geolevel_name, area_id, NULLIF(name, ''Unknown: [''||area_id||'']'') AS name, b.gid /* RIF40_GEOLEVELS.GEOLEVEL_ID */,'||E'\n';
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
	sql_stmt:='ANALYZE VERBOSE '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'populate_rif40_geometry_tables', 'Geography: % % geometry table populated with % rows', 
		c1_rec.geography::VARCHAR					/* Geography */, 
		't_rif40_'||LOWER(c1_rec.geography)||'_geometry'::VARCHAR	/* Geolevel geometry table */, 
		insert_rows::VARCHAR						/* Rows inserted */);
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
        SELECT ''SCNTRY2001'' AS geolevel_name, scntry2001 AS area_id,        	name AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_scntry2001
         GROUP BY scntry2001, name
        UNION
        SELECT ''CNTRY2001'' AS geolevel_name, cntry2001 AS area_id,           	name AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_cntry2001
         GROUP BY cntry2001, name
        UNION
        SELECT ''GOR2001'' AS geolevel_name, gor2001 AS area_id,       		gorname AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_gor2001
         GROUP BY gor2001, gorname
        UNION
        SELECT ''LADUA2001'' AS geolevel_name, ladua2001 AS area_id,           	laduaname AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_ladua2001
         GROUP BY ladua2001, laduaname
        UNION
        SELECT ''WARD2001'' AS geolevel_name, ward2001 AS area_id,             	wardname AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_ward2001
         GROUP BY ward2001, wardname
        UNION
        SELECT ''SOA2001'' AS geolevel_name, soa2001 AS area_id,       		NULL AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_soa2001
         GROUP BY soa2001
        UNION
        SELECT ''OA2001'' AS geolevel_name, coa2001 AS area_id,        		NULL AS name, ST_MakeValid(ST_Union(geom)) AS geom
          FROM x_ew01_coa2001
         GROUP BY coa2001
), b /* t_rif40_geolevels */ AS (
        SELECT geography, geolevel_name, geolevel_id AS gid, st_simplify_tolerance
          FROM t_rif40_geolevels
         WHERE geography = ''EW01''
), c /* rif40_geographies */ AS (
        SELECT max_geojson_digits
          FROM rif40_geographies
         WHERE geography = ''EW01''
), d AS (
        SELECT a.geolevel_name, geom, area_id, name, ST_MakeValid(ST_SimplifyPreserveTopology(geom, b.st_simplify_tolerance)) AS simplified_topology
          FROM a, b
         WHERE a.geolevel_name = b.geolevel_name
)
SELECT ''EW01'' geography, b.geolevel_name, area_id, NULLIF(name, ''Unknown: [''||area_id||'']'') AS name, b.gid /* RIF40_GEOLEVELS.GEOLEVEL_ID */,
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
-- Moved to XML package
--
DROP FUNCTION IF EXISTS rif40_geo_pkg.get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR);

CREATE OR REPLACE FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(
	l_geography VARCHAR, geolevel_select VARCHAR, geolevel_selection VARCHAR[])
RETURNS VARCHAR[] 
SECURITY INVOKER
AS $body$
/*

Function: 	rif40_get_default_comparison_area()
Parameters:	Geography, geolevel_select, geolevel_selection ARRAY
Returns:	Array of area IDs of default comparison area
Description:	Get default comparison area

1. Get the default comparison area (defaultcomparea) and hierarchy table name <hierarchytable> from RIF40_GEOGRPAHIES
2. Returns DISTINCT <defaultcomparea> FROM <hierarchytable> WHERE <geolevel_select> IN (SELECT unnest(<geolevel_selection>))
   i.e. all areas at the default comparison area level covered by the users selected geolevels

Test statement:

SELECT array_agg(DISTINCT level2) AS comparison_area
  FROM sahsuland_geography 
 WHERE level2 IN (
	SELECT unnest(level2_array)
	  FROM (
		SELECT array_agg(level2) AS level2_array FROM sahsuland_level2) a);

 */
DECLARE
	c1gdca CURSOR(l_geography VARCHAR) FOR
		SELECT defaultcomparea, hierarchytable
		  FROM rif40_geographies a
		 WHERE a.geography = l_geography;
	c2gdca		REFCURSOR;	
	c1gdca_rec 	RECORD;
	c2gdca_rec 	RECORD;
--
	sql_stmt 	VARCHAR;
--
	error_message 	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Get comparison geolevel - is the default set in rif40_geographies
--
	OPEN c1gdca(l_geography);
	FETCH c1gdca INTO c1gdca_rec;
	IF NOT FOUND THEN
		PERFORM rif40_log_pkg.rif40_error(-10013, 'rif40_get_default_comparison_area', 
			'Cannot find rif40_geographies geography: %',
			l_geography::VARCHAR			/* Geography */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_get_default_comparison_area', 
			'Default comparision area for geography: % is: %',
			l_geography::VARCHAR			/* Geography */,
			c1gdca_rec.defaultcomparea::VARCHAR	/* Default comparison area defined for geography */);
	END IF;
	CLOSE c1gdca;
--
	sql_stmt:='SELECT array_agg(DISTINCT '||quote_ident(LOWER(c1gdca_rec.defaultcomparea))||') AS comparision_area /* defaultcomparea */'||E'\n'||
'  FROM '||quote_ident(LOWER(c1gdca_rec.hierarchytable))||' /* hierarchytable */'||E'\n'||
' WHERE '||quote_ident(LOWER(geolevel_select))||' /* <Gelevel select> */ IN ('||E'\n'||
'	SELECT unnest($1))';
--
-- Execute SQL statement, returning comparison area
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_default_comparison_area', 'SQL> %;', sql_stmt::VARCHAR);
	OPEN c2gdca FOR EXECUTE sql_stmt USING geolevel_selection;
	FETCH c2gdca INTO c2gdca_rec;
	CLOSE c2gdca;
--
	RETURN c2gdca_rec.comparision_area;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
--		IF v_version THEN
--			GET STACKED DIAGNOTICS v_detail = PG_EXCETION_DETAIL;
--		END IF;
		error_message:='rif40_get_default_comparison_area() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
--
		RAISE;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(VARCHAR, VARCHAR, VARCHAR[]) IS 'Function: 	rif40_get_default_comparison_area()
Parameters:	Geography, geolevel_select, geolevel_selection ARRAY
Returns:	Array of area IDs of default comparison area
Description:	Get default comparison area

1. Get the default comparison area (defaultcomparea) and hierarchy table name <hierarchytable> from RIF40_GEOGRPAHIES
2. Returns DISTINCT <defaultcomparea> FROM <hierarchytable> WHERE <geolevel_select> IN (SELECT unnest(<geolevel_selection>))
   i.e. all areas at the default comparison area level covered by the users selected geolevels

Test statement:

SELECT array_agg(DISTINCT level2) AS comparision_area /* defaultcomparea */
  FROM sahsuland_geography /* hierarchytable */
 WHERE level2 /* <Gelevel select> */ IN (
	SELECT unnest(level2_array)
	  FROM (
		SELECT array_agg(level2) AS level2_array FROM sahsuland_level2) a /* Geolevel selection array */);';

GRANT EXECUTE ON FUNCTION rif40_geo_pkg.get_srid_projection_parameters(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.get_srid_projection_parameters(VARCHAR, VARCHAR) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.get_srid_projection_parameters(VARCHAR, VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(VARCHAR, VARCHAR, VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(VARCHAR, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_hierarchy_table() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_hierarchy_table() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_hierarchy_table(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_hierarchy_table(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(VARCHAR, VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, NUMERIC) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, NUMERIC) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(VARCHAR, VARCHAR, VARCHAR[]) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(VARCHAR, VARCHAR, VARCHAR[]) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(VARCHAR, VARCHAR, VARCHAR[]) TO rif40;

\df rif40_geo_pkg.create_rif40_geolevels_geometry_tables
\df rif40_geo_pkg.create_rif40_geolevels_lookup_tables
\df rif40_geo_pkg.drop_rif40_geolevels_lookup_tables
\df rif40_geo_pkg.drop_rif40_geolevels_geometry_tables
\df rif40_geo_pkg.populate_rif40_geometry_tables
\df rif40_geo_pkg.rif40_get_default_comparison_area

\echo Created PG psql code (Geographic processing).
--
-- Eof
