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
--								  create_rif40_geolevels_lookup_tables() function
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
	SELECT soa2001 AS soa2001, NULL AS name, ST_Union(geom) AS geom, 
	       ROW_NUMBER() OVER (ORDER BY soa2001) AS gid
	  FROM x_ew01_soa2001
	 GROUP BY soa2001
)
SELECT soa2001, name, gid
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
			hier_stmt1:='CREATE TABLE rif_data.'||quote_ident(LOWER(hierarchytable))||' ('||E'\n'||E'\t'||'  '||quote_ident(LOWER(c1_rec.geolevel_name))||' VARCHAR(100)'||E'\n';
		ELSIF previous_geography != c1_rec.geography THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'create_rif40_geolevels_lookup_tables', 'Creating % geography hierarchy table: %',
				c1_rec.geography, hierarchytable);
			hier_stmt1:=hier_stmt1||')';
			PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
			hier_stmt1:='GRANT SELECT ON rif_data.'||quote_ident(LOWER(hierarchytable))||' TO PUBLIC';
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
			hier_stmt1:='ANALYZE VERBOSE rif_data.'||quote_ident(LOWER(hierarchytable));
			PERFORM rif40_sql_pkg.rif40_ddl(hier_stmt1);
			OPEN c2geolook(previous_geography);
			FETCH c2geolook INTO c2_rec;
			CLOSE c2geolook;
			hier_stmt1:='COMMENT ON TABLE rif_data.'||quote_ident(LOWER(hierarchytable))||' IS '||quote_literal(c2_rec.description||' geo-level hierarchy table');
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
			hier_stmt1:='CREATE TABLE rif_data.'||quote_ident(LOWER(hierarchytable))||' ('||E'\n'||E'\t'||'  '||quote_ident(LOWER(c1_rec.geolevel_name))||' VARCHAR(100)'||E'\n';
		ELSE
			hier_stmt1:=hier_stmt1||E'\t'||', '||quote_ident(LOWER(c1_rec.geolevel_name))||' VARCHAR(100)'||E'\n';
		END IF;
		hier_stmt2[c1_rec.geolevel_id]:='COMMENT ON COLUMN rif_data.'||quote_ident(LOWER(hierarchytable))||'.'||quote_ident(LOWER(c1_rec.geolevel_name))||
			' IS '||quote_literal(c1_rec.description);
		hier_stmt3[c1_rec.geolevel_id]:='/* Create index */ '||quote_ident(LOWER(c1_rec.geolevel_name)||'_idx'||c1_rec.geolevel_id)||' ON rif_data.'||
			quote_ident(LOWER(hierarchytable))||'('||quote_ident(LOWER(c1_rec.geolevel_name))||')';
		previous_geography:=c1_rec.geography;
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'create_rif40_geolevels_lookup_tables', 'Creating % geography % lookup table: %',
			c1_rec.geography, c1_rec.geolevel_name, c1_rec.lookup_table);
--
-- Create table - using CTAS
--
		sql_stmt:='CREATE TABLE rif_data.'||quote_ident(LOWER(c1_rec.lookup_table))||E'\n'||'AS'||E'\n';
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
		sql_stmt:=sql_stmt||', ST_Union(geom) AS geom, '||E'\n';
	    sql_stmt:=sql_stmt||'ROW_NUMBER() OVER (ORDER BY '||
			quote_ident(LOWER(c1_rec.shapefile_area_id_column))||') AS gid'||E'\n';
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
		sql_stmt:=sql_stmt||'SELECT '||quote_ident(LOWER(c1_rec.geolevel_name))||', '||quote_ident(LOWER(c1_rec.lookup_desc_column))||', gid';
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
		sql_stmt:='ALTER TABLE rif_data.'||quote_ident(LOWER(c1_rec.lookup_table))||' ALTER COLUMN name TYPE VARCHAR(100);'||E'\n';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Index
--
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c1_rec.lookup_table)||'_pk')||
			' ON rif_data.'||quote_ident(LOWER(c1_rec.lookup_table))||
			'('||quote_ident(LOWER(c1_rec.geolevel_name))||')';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c1_rec.lookup_table)||'_gid')||
			' ON rif_data.'||quote_ident(LOWER(c1_rec.lookup_table))||
			'(gid)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Grant
--
		sql_stmt:='GRANT SELECT ON rif_data.'||quote_ident(LOWER(c1_rec.lookup_table))||' TO PUBLIC';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
--  Analyze
--
		sql_stmt:='ANALYZE VERBOSE rif_data.'||quote_ident(LOWER(c1_rec.lookup_table));
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Comment
--
		sql_stmt:='COMMENT ON TABLE rif_data.'||quote_ident(LOWER(c1_rec.lookup_table))||' IS '||quote_literal(c1_rec.description||' lookup table');
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN rif_data.'||quote_ident(LOWER(c1_rec.lookup_table))||'.'||quote_ident(LOWER(c1_rec.geolevel_name))||' IS '''||quote_ident(LOWER(c1_rec.geolevel_name))||'''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN rif_data.'||quote_ident(LOWER(c1_rec.lookup_table))||'.name IS '''||quote_ident(LOWER(c1_rec.geolevel_name))||' name''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN rif_data.'||quote_ident(LOWER(c1_rec.lookup_table))||'.gid IS ''Artifical primary key for RIF web interface''';
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
	hier_stmt1:='ANALYZE VERBOSE rif_data.'||quote_ident(LOWER(hierarchytable));
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
	hier_stmt1:='COMMENT ON TABLE rif_data.'||quote_ident(LOWER(hierarchytable))||' IS '||quote_literal(c2_rec.description||' geo-level hierarchy table');
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
	SELECT soa2001 AS soa2001, NULL AS name, ST_Union(geom) AS geom, 
	       ROW_NUMBER() OVER (ORDER BY soa2001) AS gid
	  FROM x_ew01_soa2001
	 GROUP BY soa2001
)
SELECT soa2001, name, gid
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

--
-- Eof