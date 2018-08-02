Postgres data processing example log
====================================

```
You are connected to database "sahsuland_dev" as user "peter" on host "localhost" at port "5432".
DO LANGUAGE plpgsql $$
DECLARE
 	c1 CURSOR FOR
		SELECT p.proname
		  FROM pg_proc p, pg_namespace n
		 WHERE p.proname  = 'rif40_startup'
		   AND n.nspname  = 'rif40_sql_pkg'
		   AND p.proowner = (SELECT oid FROM pg_roles WHERE rolname = 'rif40')
		   AND n.oid      = p.pronamespace;
--
	c1_rec RECORD;
	sql_stmt VARCHAR;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.proname = 'rif40_startup' THEN
		PERFORM rif40_sql_pkg.rif40_startup();
	ELSE
		RAISE INFO 'RIF startup: not a RIF database';
	END IF;
--
-- Set a default path, schema to user
--
	IF current_user = 'rif40' THEN
		sql_stmt:='SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
	ELSE
		sql_stmt:='SET search_path TO '||USER||',rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
	END IF;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_log_setup() DEFAULTED send DEBUG to INFO: off; debug function list: []
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): SQL> SET search_path TO peter,rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions,rif_studies;
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): SQL> DROP FUNCTION IF EXISTS peter.rif40_run_study(INTEGER, INTEGER);
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: NOTICE:  function peter.rif40_run_study(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): Created temporary table: g_rif40_study_areas
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): Created temporary table: g_rif40_comparison_areas
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): PostGIS extension V2.2.1 (POSTGIS="2.2.1 r14555" GEOS="3.5.0-CAPI-1.9.0 r4090" PROJ="Rel. 4.9.1, 04 March 2015" GDAL="GDAL 2.0.1, released 2015/09/15" LIBXML="2.7.8" LIBJSON="0.12" RASTER)
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: peter
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): search_path: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, rif_studies, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: NOTICE:  relation "study_status" already exists, skipping
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  rif40_startup(): Deleted 0, created 2 tables/views/foreign data wrapper tables
psql:c:/Program Files/PostgreSQL/9.5/etc/psqlrc:48: INFO:  SQL> SET search_path TO peter,rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
Pager usage is off.
\set ON_ERROR_STOP ON
\timing
Timing is on.

--
-- Make the same as the CSV files
--
SET client_encoding='UTF-8';
SET
Time: 0.425 ms
-- SQL statement 0: Start transaction >>>
BEGIN TRANSACTION;
BEGIN
Time: 0.237 ms
-- SQL statement 1: NON RIF initialisation >>>
/*
 * SQL statement name: 	startup.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:			None
 *
 * Description:			Run non RIF startup script (geoDataLoader version)
 * Note:				% becomes % after substitution
 */
DO LANGUAGE plpgsql $$
DECLARE
	sql_stmt VARCHAR;
BEGIN
--
-- Set a default path and schema for user
--
	IF current_user != 'rif40' THEN
		sql_stmt:='SET SESSION search_path TO '||current_user||',public, topology';
	ELSE
		RAISE EXCEPTION 'RIF startup(geoDataLoader): RIF user: % is not allowed to run this script', current_user;
	END IF;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
psql:pg_USA_2014.sql:112: INFO:  SQL> SET SESSION search_path TO peter,public, topology;
DO
Time: 0.836 ms
--
-- Eof;
-- SQL statement 2: Drop table cb_2014_us_county_500k >>>
DROP TABLE IF EXISTS cb_2014_us_county_500k;
psql:pg_USA_2014.sql:117: NOTICE:  table "cb_2014_us_county_500k" does not exist, skipping
DROP TABLE
Time: 0.780 ms
-- SQL statement 3: Create tablecb_2014_us_county_500k >>>
CREATE TABLE cb_2014_us_county_500k (
	statefp                        	text /* Current state Federal Information Processing Series (FIPS) code */,
	countyfp                       	text /* Current county Federal Information Processing Series (FIPS) code */,
	countyns                       	text /* Current county Geographic Names Information System (GNIS) code */,
	affgeoid                       	text /* American FactFinder summary level code + geovariant code + ''00US'' + GEOID */,
	geoid                          	text /* County identifier; a concatenation of current state Federal Information Processing Series (FIPS) code and county FIPS code */,
	name                           	text /* Current county name */,
	lsad                           	text /* Current legal/statistical area description code for county */,
	aland                          	text /* Current land area (square meters) */,
	awater                         	text /* Current water area (square meters) */,
	gid                            	integer	NOT NULL /* Unique geographic index */,
	areaid                         	text	NOT NULL /* Area ID (COUNTYNS): Current county Geographic Names Information System (GNIS) code */,
	areaname                       	text	NOT NULL /* Area name (NAME): Current county name */,
	area_km2                       	numeric /* Area in square km */,
	geographic_centroid_wkt        	text /* Wellknown text for geographic centroid */,
	wkt_9                          	text /* Wellknown text for zoomlevel 9 */,
	wkt_8                          	text /* Wellknown text for zoomlevel 8 */,
	wkt_7                          	text /* Wellknown text for zoomlevel 7 */,
	wkt_6                          	text /* Wellknown text for zoomlevel 6 */);
CREATE TABLE
Time: 9.662 ms
-- SQL statement 4: Comment geospatial data table >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE cb_2014_us_county_500k IS 'The County at a scale of 1:500,000';
COMMENT
Time: 0.342 ms
-- SQL statement 5: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.gid IS 'Unique geographic index';
COMMENT
Time: 0.529 ms
-- SQL statement 6: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.areaid IS 'Area ID (COUNTYNS): Current county Geographic Names Information System (GNIS) code';
COMMENT
Time: 0.622 ms
-- SQL statement 7: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.areaname IS 'Area name (NAME): Current county name';
COMMENT
Time: 0.425 ms
-- SQL statement 8: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.area_km2 IS 'Area in square km';
COMMENT
Time: 0.368 ms
-- SQL statement 9: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.geographic_centroid_wkt IS 'Wellknown text for geographic centroid';
COMMENT
Time: 0.315 ms
-- SQL statement 10: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.wkt_6 IS 'Wellknown text for zoomlevel 6';
COMMENT
Time: 0.720 ms
-- SQL statement 11: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.wkt_7 IS 'Wellknown text for zoomlevel 7';
COMMENT
Time: 0.429 ms
-- SQL statement 12: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.wkt_8 IS 'Wellknown text for zoomlevel 8';
COMMENT
Time: 0.467 ms
-- SQL statement 13: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.wkt_9 IS 'Wellknown text for zoomlevel 9';
COMMENT
Time: 0.376 ms
-- SQL statement 14: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.statefp IS 'Current state Federal Information Processing Series (FIPS) code';
COMMENT
Time: 0.440 ms
-- SQL statement 15: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.countyfp IS 'Current county Federal Information Processing Series (FIPS) code';
COMMENT
Time: 0.331 ms
-- SQL statement 16: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.countyns IS 'Current county Geographic Names Information System (GNIS) code';
COMMENT
Time: 0.368 ms
-- SQL statement 17: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.affgeoid IS 'American FactFinder summary level code + geovariant code + ''00US'' + GEOID';
COMMENT
Time: 0.462 ms
-- SQL statement 18: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.geoid IS 'County identifier; a concatenation of current state Federal Information Processing Series (FIPS) code and county FIPS code';
COMMENT
Time: 0.429 ms
-- SQL statement 19: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.name IS 'Current county name';
COMMENT
Time: 0.468 ms
-- SQL statement 20: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.lsad IS 'Current legal/statistical area description code for county';
COMMENT
Time: 0.485 ms
-- SQL statement 21: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.aland IS 'Current land area (square meters)';
COMMENT
Time: 0.402 ms
-- SQL statement 22: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_county_500k.awater IS 'Current water area (square meters)';
COMMENT
Time: 0.381 ms
-- SQL statement 23: Load table from CSV file >>>
\copy cb_2014_us_county_500k FROM 'cb_2014_us_county_500k.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 3233
Time: 7335.880 ms
-- SQL statement 24: Row check: 3233 >>>
DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	csvfile_rowcheck.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: Expected number of rows; e.g. 3233
 *						3: Column to count; e.g. gid
 *
 * Description:			Check number of rows in loaded CSV file is as expected
 * Note:				%% becomes % after substitution
 */
	c1 CURSOR FOR
		SELECT COUNT(gid) AS total
		  FROM cb_2014_us_county_500k;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.total = 3233 THEN
		RAISE INFO 'Table: cb_2014_us_county_500k row check OK: %', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: cb_2014_us_county_500k row check FAILED: expected: 3233 got: %', c1_rec.total;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:455: INFO:  Table: cb_2014_us_county_500k row check OK: 3233
DO
Time: 4.795 ms
-- SQL statement 25: Add primary key cb_2014_us_county_500k >>>
ALTER TABLE cb_2014_us_county_500k ADD PRIMARY KEY (gid);
ALTER TABLE
Time: 16.893 ms
-- SQL statement 26: Add unique key cb_2014_us_county_500k >>>
/*
 * SQL statement name: 	add_unique_key.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. cb_2014_us_nation_5m
 *						2: constraint name; e.g. cb_2014_us_nation_5m_uk
 *						3: fields; e.g. areaid
 *
 * Description:			Add unique key constraint
 * Note:				%% becomes % after substitution
 */
ALTER TABLE cb_2014_us_county_500k ADD CONSTRAINT cb_2014_us_county_500k_uk UNIQUE(areaid);
ALTER TABLE
Time: 30.221 ms
--
-- Add geometric  data
--
-- SQL statement 28: Add geometry column: geographic centroid >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_county_500k','geographic_centroid', 4326, 'POINT', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                               addgeometrycolumn                               
-------------------------------------------------------------------------------
 peter.cb_2014_us_county_500k.geographic_centroid SRID:4326 TYPE:POINT DIMS:2 
(1 row)

Time: 11.144 ms
-- SQL statement 29: Add geometry column for original SRID geometry >>>
/*
 * SQL statement name: 	add_geometry_column2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *                      5: Schema (rif_data. or "") [NEVER USED IN POSTGRES]
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_county_500k','geom_orig', 4269, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                             addgeometrycolumn                              
----------------------------------------------------------------------------
 peter.cb_2014_us_county_500k.geom_orig SRID:4269 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 7.478 ms
-- SQL statement 30: Add geometry column for zoomlevel: 6 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_county_500k','geom_6', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                            addgeometrycolumn                            
-------------------------------------------------------------------------
 peter.cb_2014_us_county_500k.geom_6 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 6.769 ms
-- SQL statement 31: Add geometry column for zoomlevel: 7 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_county_500k','geom_7', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                            addgeometrycolumn                            
-------------------------------------------------------------------------
 peter.cb_2014_us_county_500k.geom_7 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 6.916 ms
-- SQL statement 32: Add geometry column for zoomlevel: 8 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_county_500k','geom_8', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                            addgeometrycolumn                            
-------------------------------------------------------------------------
 peter.cb_2014_us_county_500k.geom_8 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 6.301 ms
-- SQL statement 33: Add geometry column for zoomlevel: 9 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_county_500k','geom_9', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                            addgeometrycolumn                            
-------------------------------------------------------------------------
 peter.cb_2014_us_county_500k.geom_9 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 7.190 ms
-- SQL statement 34: Update geographic centroid, geometry columns, handle polygons and mutlipolygons, convert highest zoomlevel to original SRID >>>
UPDATE cb_2014_us_county_500k
   SET geographic_centroid = ST_GeomFromText(geographic_centroid_wkt, 4326),
       geom_6 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_6, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_6, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_6, 4326))
       		END,
       geom_7 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_7, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_7, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_7, 4326))
       		END,
       geom_8 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_8, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_8, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_8, 4326))
       		END,
       geom_9 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_9, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_9, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_9, 4326))
       		END,
       geom_orig = ST_Transform(
       		CASE ST_IsCollection(ST_GeomFromText(wkt_9, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_9, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_9, 4326))
       		END, 4269);
UPDATE 3233
Time: 13955.809 ms
-- SQL statement 35: Make geometry columns valid >>>
UPDATE cb_2014_us_county_500k
   SET
       geom_6 = CASE ST_IsValid(geom_6)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_6), 3 /* Remove non polygons */)
				ELSE geom_6
			END,
       geom_7 = CASE ST_IsValid(geom_7)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_7), 3 /* Remove non polygons */)
				ELSE geom_7
			END,
       geom_8 = CASE ST_IsValid(geom_8)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_8), 3 /* Remove non polygons */)
				ELSE geom_8
			END,
       geom_9 = CASE ST_IsValid(geom_9)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_9), 3 /* Remove non polygons */)
				ELSE geom_9
			END,
       geom_orig = CASE ST_IsValid(geom_orig)
			WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_orig), 3 /* Remove non polygons */)
			ELSE geom_orig
		END;
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -85.2045985857456 46.044431737627747
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -84.769219239639256 45.839784716078725
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -84.439364648483661 45.996148099462111
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -89.528959972298978 29.651621096712105
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -149.31411756797058 59.956820817190831
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -68.983577325577343 44.175116080674087
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -162.79221305489506 55.324770369325378
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -131.38639491395594 55.250884374979393
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -80.296620697321714 25.326798923010927
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -156.76186812997113 56.928697844261862
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -68.269311127101147 44.285601416405427
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -162.87907356747357 64.452955401038409
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -105.05294356541158 39.913698399795408
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -105.0816577844458 39.635680867581875
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -105.0816577844458 39.635680867581875
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -105.0816577844458 39.635680867581875
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -105.05294356541158 39.913698399795408
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.338802459648605 33.969379649821356
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.338802459648605 33.969379649821356
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -76.092141175236193 39.535247463639472
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -69.797266507459526 45.783511080850097
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -69.797266507459526 45.783511080850097
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -70.398470468488483 43.392955507969518
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -70.398470468488483 43.392955507969518
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -86.727170050035056 45.522504510125522
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -86.727170050035056 45.522504510125522
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -83.191731831446845 42.035343318835331
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -83.191731831446845 42.035343318835331
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.915388535380544 44.107845460240469
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.915388535380544 44.107845460240469
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -91.774770828512843 46.946012696542709
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -91.774770828512843 46.946012696542709
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -70.627825293024301 42.976272865436876
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -74.008206728827744 40.686752094127101
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -74.008206728827744 40.686752094127101
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -76.079929284989959 43.216650764907854
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -76.079929284989959 43.216650764907854
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.332899720985736 35.161454340197352
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.332899720985736 35.161454340197352
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -77.527134271471283 35.243416245553263
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -77.527134271471283 35.243416245553256
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -99.623443673778681 43.742367530068542
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -99.623443673778681 43.742367530068542
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -95.005120469862476 29.621207559504569
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -95.005120469862476 29.621207559504569
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -95.005120469862476 29.621207559504569
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -97.244829554531563 27.860057561715571
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -97.244829554531563 27.860057561715571
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -97.244829554531563 27.860057561715571
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.185037582823625 37.466151168343657
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.185037582823625 37.466151168343657
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -77.255067046122065 37.377089117161127
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -77.255067046122065 37.377089117161127
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -77.255067046122065 37.377089117161127
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -77.255067046122065 37.377089117161127
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -124.50933945526046 47.798880447557458
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -124.50933945526046 47.798880447557458
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -135.33962501949202 58.126853747923761
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -164.58577496131997 63.062008600501613
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -164.58577496131997 63.062008600501613
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -64.733513980775001 18.314846231682239
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -64.733513980775001 18.314846231682239
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -86.149655319709339 34.533595216150225
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -86.149655319709339 34.533595216150225
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -86.149655319709339 34.533595216150225
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -86.149655319709339 34.533595216150225
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -86.149655319709339 34.533595216150225
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -69.797266507459526 45.783511080850097
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -69.797266507459526 45.783511080850097
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -76.329400888614401 39.31505881204005
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -76.325803132627144 39.313933136389146
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -76.325803132627144 39.313933136389146
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -76.329400888614401 39.31505881204005
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -104.9405991834402 39.694789663058671
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -104.90255284321985 39.685682784685795
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -104.90398855417156 39.624082484748492
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -105.06371139754941 39.652949570911581
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -104.9405991834402 39.694789663058671
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -73.613386217107234 40.988911445423454
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.128946799442815 33.474276250701259
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.128946799442815 33.474276250701259
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -73.611591578417588 43.545452763741778
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -73.611591578417588 43.545452763741771
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -98.761467604835062 47.971544398865809
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -98.761515861680081 47.971547633115946
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -71.190982913832926 42.283033227788238
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -71.190982913832926 42.283033227788238
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -71.040478954044048 42.302795802230833
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -70.892355035877046 42.328395791758801
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -70.892355035877046 42.328395791758801
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -85.301868002724021 45.816759778305787
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.6830765825366 46.491785659208666
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -84.683794438012455 46.491785659208666
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -84.683794438012455 46.491785659208666
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.6830765825366 46.491785659208666
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -88.851663330829339 47.912974169059183
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -85.568192384266396 45.099807891308899
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -85.568192384266396 45.099807891308899
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -87.959010046603055 46.950136565994576
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -87.542653870606884 46.704164935979946
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -86.522581239416255 36.141302904750916
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.471823321186619 29.557179497712536
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -80.518796967098979 37.132578024244033
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -80.518796967098979 37.132578024244033
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -80.518796967098979 37.132578024244033
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -122.57041181497482 48.537826304964312
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -99.623443673778681 43.742367530068542
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -99.623443673778681 43.742367530068542
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -86.522581239416255 36.141302904750916
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -96.765661024398042 28.422364344267351
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -88.211336246366258 30.322265366322377
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -134.7093832160586 58.225699856486287
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -161.06002779165578 66.2109265827916
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -133.41433663324764 56.472666614042623
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -83.82990534948236 29.973025172117183
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -104.9405991834402 39.694789663058671
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -104.931625989992 39.700202241714251
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -104.931625989992 39.700202241714251
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -104.9405991834402 39.694789663058671
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.128946799442815 33.474276250701259
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.128946799442815 33.474276250701259
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -98.761467604835062 47.971544398865809
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -98.761515861680081 47.971547633115946
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -76.079929284989959 43.216650764907854
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -76.079929284989959 43.216650764907854
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -73.611591578417588 43.545452763741778
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -73.611591578417588 43.545452763741771
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -77.527134271471283 35.243416245553263
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -77.527134271471283 35.243416245553256
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.637629370486394 35.158705093896103
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.63762937048638 35.158705093896103
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.332899720985736 35.161454340197352
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.332899720985736 35.161454340197352
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -91.031072122561866 32.120522727412002
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -91.031072122561866 32.120522727412002
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -91.031072122561866 32.120522727412002
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -76.533378846247672 39.207368963000143
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -76.533378846247672 39.207368963000143
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -70.835644453284473 42.484415519354528
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -70.782882075809084 42.553490332673341
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -83.440109826092836 45.037004796114807
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -83.440109826092836 45.037004796114807
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -168.1524398931079 -14.53606964980365
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -168.1524398931079 -14.53606964980365
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -97.075774589967608 28.543674837309844
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -97.075774589967608 28.543674837309844
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -97.075774589967608 28.543674837309844
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -114.04121103776512 41.850564882761844
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -114.04121103776512 41.850564882761844
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -77.29706159145961 37.310763550143562
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -77.29706159145961 37.310763550143562
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -77.29706159145961 37.310763550143562
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.121768244684262 36.646133256817265
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.121768244684262 36.646133256817265
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.121768244684262 36.646133256817265
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.121768244684262 36.646133256817265
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.121768244684262 36.646133256817265
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point 145.71084375458872 15.242563490039492
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -136.20212837373236 57.725721529782547
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -110.85788026144199 39.813264985200973
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -110.85787971746954 39.813264981600788
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -110.85788026144198 39.813264985200973
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -110.85788026144198 39.813264985200973
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -110.85788026144198 39.813264985200973
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -124.47703595884697 42.667154484440495
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -124.47703595884697 42.667154484440495
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -71.385521747789767 41.760544255248504
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -123.15187475041776 48.641094869154877
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -76.812150217519232 38.252294494373501
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -132.38457295313296 55.516616212784228
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.003036210144032 32.530325267299119
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.003161495200402 32.530239837863462
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.003161495200402 32.530239837863462
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.003036210144032 32.530325267299119
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.003036210144032 32.530325267299119
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -97.075774589967608 28.543674837309844
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -97.075774589967608 28.543674837309844
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -97.075774589967608 28.543674837309844
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -82.787938126278135 41.678113127567137
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -82.834598732208747 41.591082299343306
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.637629370486394 35.158705093896103
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.63762937048638 35.158705093896103
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -124.21753120432521 41.950803995072008
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.049982697098713 30.362301265584271
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.049982697098713 30.362301265584271
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -67.746712340678357 44.465247479402493
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -80.518796967098979 37.132578024244033
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -80.518796967098979 37.132578024244033
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -80.518796967098979 37.132578024244033
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -122.42038002052104 37.863362056694065
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -122.42038002052104 37.863362056694065
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -65.750715190062209 18.387959000506008
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -65.750715190062209 18.387959000506008
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -148.7696241895342 70.44321943566645
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -147.82600316652218 70.239345639639652
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.450491408359426 31.62128424366125
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.338802459648605 33.969379649821356
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.338802459648605 33.969379649821356
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -88.490223098736109 37.067798908270916
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -88.490223098736109 37.067798908270916
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -85.388728515302532 29.924397878163887
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -85.388728515302532 29.924397878163887
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.83642804716068 31.810891344376074
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.836427719204991 31.810940956622463
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.836427719204991 31.810940956622463
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.83642804716068 31.810891344376074
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.83642804716068 31.810891344376074
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -101.32547900703202 40.002705248798257
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -101.32547900703202 40.002705248798257
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -101.32547900703202 40.002705248798257
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -101.32547900703202 40.002705248798257
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -101.32547900703202 40.002705248798257
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.915388535380544 44.107845460240469
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.915388535380544 44.107845460240469
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -114.04121103776512 41.850564882761844
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -114.04121103776512 41.850564882761844
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -93.067269612790625 38.529968370799381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -93.067269612790625 38.529968370799381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -93.067269612790625 38.529968370799381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -93.067269612790625 38.529968370799381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -93.067269612790625 38.529968370799381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -97.148700260955977 27.885477982478932
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -97.512948574763584 27.871484116655125
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -97.335997199965206 27.870367235345242
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -97.335997199965206 27.870367235345242
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -97.148700260955977 27.885477982478932
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -95.809477530558539 36.622936491150497
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -95.809477530558539 36.622936491150497
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -95.809477530558539 36.622936491150497
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -95.809477530558539 36.622936491150497
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -105.14698263274865 39.913870227689237
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -104.9800727892537 40.029593281925372
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -105.14698263274865 39.913870227689237
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -105.14698263274865 39.913870227689237
psql:pg_USA_2014.sql:632: NOTICE:  Ring Self-intersection at or near point -105.14698263274865 39.913870227689237
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.098796869456876 38.958335310112318
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -82.098796869456876 38.958335310112318
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -146.81418587330387 60.864845322124339
psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -132.1469627906248 56.150317485221493
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.305125674498683 31.691046368555376
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.305125674498683 31.691046368555376
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.305125674498683 31.691046368555376
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.305125674498683 31.691046368555376
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.305125674498683 31.691046368555376
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -76.799978957098574 40.878516094996286
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.601326764693781 39.530350368665381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.601326764693781 39.530350368665381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.601326764693781 39.530350368665381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.601326764693781 39.530350368665381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -94.601326764693781 39.530350368665381
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -73.000341387145497 42.312170037412372
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -104.98007693607646 40.029510400235409
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.809549621436886 39.230600279418809
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -79.809549621436886 39.230600279418809
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.298664975215985 32.999687607948616
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.298664975215985 32.999687607948616
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.298664975215985 32.999687607948616
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.298664975215985 32.999687607948616
psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -84.298664975215985 32.999687607948616
UPDATE 3233
Time: 27529.711 ms
--
-- Test geometry and make valid if required
--
-- SQL statement 37: Check validity of geometry columns >>>
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
SELECT areaname,
       6::Text AS geolevel,
       ST_IsValidReason(geom_6) AS reason
  FROM cb_2014_us_county_500k
 WHERE NOT ST_IsValid(geom_6)
UNION
SELECT areaname,
       7::Text AS geolevel,
       ST_IsValidReason(geom_7) AS reason
  FROM cb_2014_us_county_500k
 WHERE NOT ST_IsValid(geom_7)
UNION
SELECT areaname,
       8::Text AS geolevel,
       ST_IsValidReason(geom_8) AS reason
  FROM cb_2014_us_county_500k
 WHERE NOT ST_IsValid(geom_8)
UNION
SELECT areaname,
       9::Text AS geolevel,
       ST_IsValidReason(geom_9) AS reason
  FROM cb_2014_us_county_500k
 WHERE NOT ST_IsValid(geom_9)
UNION
SELECT areaname,
       'geom_orig'::Text AS geolevel,
       ST_IsValidReason(geom_orig) AS reason
  FROM cb_2014_us_county_500k
 WHERE NOT ST_IsValid(geom_orig)
 ORDER BY 1, 2;
	c1_rec RECORD;
	total INTEGER:=0;
BEGIN
	FOR c1_rec IN c1 LOOP
		total:=total+1;
		RAISE INFO 'Area: %, geolevel: %: %', c1_rec.areaname, c1_rec.geolevel, c1_rec.reason;
	END LOOP;
	IF total = 0 THEN
		RAISE INFO 'Table: cb_2014_us_county_500k no invalid geometry check OK';
	ELSE
		RAISE EXCEPTION 'Table: cb_2014_us_county_500k no invalid geometry check FAILED: % invalid', total;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:685: INFO:  Table: cb_2014_us_county_500k no invalid geometry check OK
DO
Time: 13247.778 ms
--
-- Make all polygons right handed
--
-- SQL statement 39: Make all polygons right handed for original geometry >>>
UPDATE cb_2014_us_county_500k
   SET       geom_6 = ST_ForceRHR(geom_6),
       geom_7 = ST_ForceRHR(geom_7),
       geom_8 = ST_ForceRHR(geom_8),
       geom_9 = ST_ForceRHR(geom_9),
       geom_orig = ST_ForceRHR(geom_orig);
UPDATE 3233
Time: 2309.552 ms
--
-- Test Turf and DB areas agree to within 1%
--
--
-- Create spatial indexes
--
-- SQL statement 42: Index geometry column for zoomlevel: 6 >>>
CREATE INDEX cb_2014_us_county_500k_geom_6_gix ON cb_2014_us_county_500k USING GIST (geom_6);
CREATE INDEX
Time: 121.216 ms
-- SQL statement 43: Index geometry column for zoomlevel: 7 >>>
CREATE INDEX cb_2014_us_county_500k_geom_7_gix ON cb_2014_us_county_500k USING GIST (geom_7);
CREATE INDEX
Time: 117.606 ms
-- SQL statement 44: Index geometry column for zoomlevel: 8 >>>
CREATE INDEX cb_2014_us_county_500k_geom_8_gix ON cb_2014_us_county_500k USING GIST (geom_8);
CREATE INDEX
Time: 129.539 ms
-- SQL statement 45: Index geometry column for zoomlevel: 9 >>>
CREATE INDEX cb_2014_us_county_500k_geom_9_gix ON cb_2014_us_county_500k USING GIST (geom_9);
CREATE INDEX
Time: 127.699 ms
-- SQL statement 46: Index geometry column for original SRID geometry >>>
CREATE INDEX cb_2014_us_county_500k_geom_orig_gix ON cb_2014_us_county_500k USING GIST (geom_orig);
CREATE INDEX
Time: 125.912 ms
--
-- Reports
--
-- SQL statement 48: Areas and centroids report >>>
/*
 * SQL statement name: 	area_centroid_report.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Area and centroids report
 * Note:				% becomes % after substitution
 */
WITH a AS (
	SELECT areaname,
		   ROUND(area_km2::numeric, 2) AS area_km2,
		   ROUND(
				(ST_Area(geography(geom_9))/(1000*1000))::numeric, 2) AS area_km2_calc,
		   ROUND(ST_X(geographic_centroid)::numeric, 4)||','||ROUND(ST_Y(geographic_centroid)::numeric, 4) AS geographic_centroid,
		   ROUND(ST_X(ST_Centroid(geom_9))::numeric, 4)||','||ROUND(ST_Y(ST_Centroid(geom_9))::numeric, 4) AS geographic_centroid_calc,
		   ROUND(ST_Distance_Sphere(ST_Centroid(geom_9), geographic_centroid)::numeric/1000, 2) AS centroid_diff_km
	  FROM cb_2014_us_county_500k
	 GROUP BY areaname, area_km2, geom_9, geographic_centroid
)
SELECT a.areaname,
       a.area_km2,
	   a.area_km2_calc,
	   ROUND(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2_calc), 2) AS pct_km2_diff,
	   a.geographic_centroid,
      a.geographic_centroid_calc,
	   a.centroid_diff_km
  FROM a
 ORDER BY 1
 LIMIT 100;
    areaname    | area_km2 | area_km2_calc | pct_km2_diff | geographic_centroid | geographic_centroid_calc | centroid_diff_km 
----------------+----------+---------------+--------------+---------------------+--------------------------+------------------
 Abbeville      |  1327.14 |       1324.10 |         0.23 | -82.4484,34.2237    | -82.4587,34.2226         |             0.96
 Acadia         |  1708.59 |       1702.89 |         0.33 | -92.5021,30.2285    | -92.4120,30.2905         |            11.07
 Accomack       |  1461.09 |       1458.80 |         0.16 | -75.7416,37.7659    | -75.6332,37.7643         |             9.52
 Ada            |  2747.65 |       2746.39 |         0.05 | -116.3414,43.3948   | -116.2412,43.4512        |            10.24
 Adair          |  1478.32 |       1477.05 |         0.09 | -94.4794,41.3765    | -94.4710,41.3307         |             5.14
 Adair          |  1497.78 |       1494.62 |         0.21 | -94.6354,35.9175    | -94.6587,35.8839         |             4.28
 Adair          |  1069.93 |       1067.96 |         0.18 | -85.2449,37.2149    | -85.2806,37.1042         |            12.71
 Adair          |  1476.35 |       1474.71 |         0.11 | -92.5541,40.1906    | -92.6007,40.1906         |             3.96
 Adams          |  1519.82 |       1517.62 |         0.14 | -83.4708,38.7986    | -83.4720,38.8456         |             5.23
 Adams          |  1462.84 |       1461.18 |         0.11 | -98.4938,40.5043    | -98.5012,40.5245         |             2.33
 Adams          |  1352.42 |       1350.81 |         0.12 | -77.1022,39.8977    | -77.2179,39.8715         |            10.29
 Adams          |   881.46 |        880.42 |         0.12 | -84.9089,40.7140    | -84.9366,40.7457         |             4.23
 Adams          |  1266.52 |       1262.64 |         0.31 | -91.3982,31.4292    | -91.3536,31.4829         |             7.32
 Adams          |  1780.97 |       1780.61 |         0.02 | -89.8964,43.9834    | -89.7704,43.9695         |            10.20
 Adams          |  1103.21 |       1102.10 |         0.10 | -94.7096,41.0278    | -94.6992,41.0290         |             0.88
 Adams          |  3547.39 |       3547.12 |         0.01 | -116.3207,44.8697   | -116.4538,44.8897        |            10.72
 Adams          |  2560.10 |       2561.00 |         0.04 | -102.4694,46.0764   | -102.5285,46.0968        |             5.09
 Adams          |  2259.74 |       2257.03 |         0.12 | -91.2799,39.9657    | -91.1885,39.9879         |             8.17
 Adams          |  3069.84 |       3065.99 |         0.13 | -104.7480,39.8336   | -104.3379,39.8736        |            35.30
 Adams          |  4997.04 |       4999.80 |         0.06 | -118.3232,46.8749   | -118.5605,46.9834        |            21.68
 Addison        |  2091.91 |       2091.30 |         0.03 | -73.2474,44.0346    | -73.1408,44.0309         |             8.53
 Adjuntas       |   174.70 |        173.78 |         0.53 | -66.7567,18.1820    | -66.7539,18.1797         |             0.39
 Aguada         |    81.08 |         80.66 |         0.52 | -67.1745,18.3620    | -67.1750,18.3605         |             0.17
 Aguadilla      |    95.47 |         95.00 |         0.49 | -67.1260,18.4463    | -67.1208,18.4597         |             1.59
 Aguas Buenas   |    78.29 |         77.85 |         0.57 | -66.1315,18.2475    | -66.1274,18.2511         |             0.59
 Aibonito       |    81.54 |         81.07 |         0.58 | -66.2551,18.1364    | -66.2644,18.1311         |             1.15
 Aiken          |  2806.29 |       2798.87 |         0.27 | -81.6528,33.5439    | -81.6348,33.5443         |             1.67
 Aitkin         |  5166.06 |       5168.30 |         0.04 | -93.4591,46.5104    | -93.4154,46.6082         |            11.38
 Alachua        |  2518.06 |       2509.27 |         0.35 | -82.3110,29.6935    | -82.3577,29.6748         |             4.97
 Alamance       |  1128.04 |       1125.88 |         0.19 | -79.3433,36.0430    | -79.3995,36.0440         |             5.05
 Alameda        |  1953.03 |       1949.62 |         0.17 | -122.0272,37.6944   | -121.8892,37.6466        |            13.26
 Alamosa        |  1876.16 |       1872.77 |         0.18 | -105.6213,37.6142   | -105.7883,37.5729        |            15.41
 Albany         |  1382.04 |       1381.12 |         0.07 | -73.9152,42.6427    | -73.9736,42.6002         |             6.72
 Albany         | 11168.38 |      11159.34 |         0.08 | -105.6434,41.8968   | -105.7238,41.6546        |            27.75
 Albemarle      |  1883.83 |       1880.71 |         0.17 | -78.6100,38.0189    | -78.5566,38.0229         |             4.70
 Alcona         |  1799.11 |       1798.90 |         0.01 | -83.4152,44.6897    | -83.5937,44.6854         |            14.12
 Alcorn         |  1042.49 |       1039.89 |         0.25 | -88.5884,34.8691    | -88.5803,34.8808         |             1.50
 Aleutians East | 18469.69 |      18513.75 |         0.24 | -161.8647,55.0634   | -161.9818,55.3666        |            34.52
 Aleutians West | 11724.71 |      11746.22 |         0.18 | -163.1204,52.8876   | -106.6079,52.7980        |          3695.10
 Alexander      |   655.52 |        654.29 |         0.19 | -89.2862,37.1678    | -89.3376,37.1915         |             5.26
 Alexander      |   684.30 |        682.84 |         0.21 | -81.2133,35.9048    | -81.1770,35.9210         |             3.73
 Alexandria     |    40.33 |         40.26 |         0.17 | -77.0897,38.8212    | -77.0861,38.8184         |             0.44
 Alfalfa        |  2287.46 |       2282.91 |         0.20 | -98.2911,36.7485    | -98.3240,36.7310         |             3.52
 Alger          |  2427.18 |       2427.89 |         0.03 | -86.6694,46.4939    | -86.6041,46.4086         |            10.72
 Allamakee      |  1706.82 |       1706.02 |         0.05 | -91.3008,43.3051    | -91.3780,43.2843         |             6.67
 Allegan        |  2183.55 |       2182.24 |         0.06 | -85.9839,42.6055    | -85.8884,42.5913         |             7.97
 Allegany       |  2680.49 |       2678.84 |         0.06 | -78.0524,42.2850    | -78.0276,42.2574         |             3.69
 Allegany       |  1114.71 |       1113.31 |         0.13 | -78.6507,39.5873    | -78.6990,39.6215         |             5.61
 Alleghany      |   613.99 |        612.81 |         0.19 | -81.1691,36.4618    | -81.1272,36.4914         |             4.98
 Alleghany      |  1164.64 |       1162.66 |         0.17 | -80.0136,37.7752    | -80.0071,37.7876         |             1.49
 Allegheny      |  1930.24 |       1928.12 |         0.11 | -79.9146,40.4591    | -79.9812,40.4688         |             5.74
 Allen          |  1055.00 |       1053.69 |         0.12 | -84.1309,40.7964    | -84.1058,40.7715         |             3.48
 Allen          |   913.52 |        911.90 |         0.18 | -86.1720,36.8198    | -86.1904,36.7513         |             7.79
 Allen          |  1991.10 |       1984.74 |         0.32 | -92.7148,30.5998    | -92.8279,30.6529         |            12.33
 Allen          |  1711.01 |       1709.53 |         0.09 | -85.0688,41.1036    | -85.0665,41.0909         |             1.43
 Allen          |  1310.83 |       1308.72 |         0.16 | -95.3228,37.8728    | -95.3014,37.8857         |             2.37
 Allendale      |  1071.64 |       1068.61 |         0.28 | -81.4096,32.9611    | -81.3583,32.9881         |             5.65
 Alpena         |  1550.24 |       1550.22 |         0.00 | -83.3849,45.0387    | -83.6258,45.0349         |            18.93
 Alpine         |  1927.31 |       1924.14 |         0.16 | -119.7197,38.5991   | -119.8206,38.5972        |             8.78
 Amador         |  1571.96 |       1569.81 |         0.14 | -120.5714,38.4766   | -120.6511,38.4464        |             7.70
 Amelia         |   930.49 |        928.97 |         0.16 | -77.8947,37.3580    | -77.9761,37.3360         |             7.60
 Amherst        |  1242.32 |       1240.28 |         0.16 | -79.1476,37.6131    | -79.1451,37.6048         |             0.96
 Amite          |  1900.55 |       1894.63 |         0.31 | -90.9190,31.2350    | -90.8044,31.1744         |            12.81
 Añasco         |   103.31 |        102.73 |         0.56 | -67.1330,18.2848    | -67.1206,18.2881         |             1.36
 Anchorage      |  4471.43 |       4487.66 |         0.36 | -149.6095,61.1353   | -149.1096,61.1510        |            26.89
 Anderson       |   529.92 |        529.21 |         0.13 | -85.0101,37.9870    | -84.9910,38.0039         |             2.51
 Anderson       |   894.87 |        893.00 |         0.21 | -84.1845,36.1310    | -84.1985,36.1184         |             1.88
 Anderson       |  2800.38 |       2791.89 |         0.30 | -95.6384,31.7959    | -95.6525,31.8133         |             2.35
 Anderson       |  1966.46 |       1961.76 |         0.24 | -82.5749,34.5760    | -82.6379,34.5191         |             8.56
 Anderson       |  1514.33 |       1512.06 |         0.15 | -95.3190,38.2337    | -95.2934,38.2142         |             3.12
 Andrew         |  1131.61 |       1130.36 |         0.11 | -94.9604,40.0029    | -94.8020,39.9835         |            13.66
 Andrews        |  3899.24 |       3888.16 |         0.28 | -102.6322,32.3234   | -102.6377,32.3050        |             2.11
 Androscoggin   |  1287.72 |       1287.61 |         0.01 | -70.1858,44.1220    | -70.2065,44.1658         |             5.14
 Angelina       |  2246.47 |       2239.66 |         0.30 | -94.6295,31.2728    | -94.6118,31.2548         |             2.62
 Anne Arundel   |  1165.64 |       1164.06 |         0.14 | -76.5993,38.9755    | -76.6051,39.0065         |             3.48
 Anoka          |  1155.36 |       1155.30 |         0.01 | -93.2702,45.1931    | -93.2465,45.2733         |             9.11
 Anson          |  1394.16 |       1390.93 |         0.23 | -80.0852,35.1134    | -80.1027,34.9739         |            15.60
 Antelope       |  2225.18 |       2223.54 |         0.07 | -98.0726,42.1476    | -98.0667,42.1769         |             3.29
 Antrim         |  1360.23 |       1360.42 |         0.01 | -85.3079,45.0017    | -85.1402,44.9991         |            13.18
 Apache         | 29121.65 |      29057.98 |         0.22 | -109.5480,34.3181   | -109.4888,35.3956        |           119.93
 Appanoose      |  1338.36 |       1336.90 |         0.11 | -92.8445,40.7179    | -92.8687,40.7431         |             3.47
 Appling        |  1330.38 |       1326.42 |         0.30 | -82.2735,31.7920    | -82.2889,31.7492         |             4.97
 Appomattox     |   867.91 |        866.23 |         0.19 | -78.7932,37.3693    | -78.8122,37.3722         |             1.70
 Aransas        |   757.78 |        755.21 |         0.34 | -97.0071,28.0886    | -96.9935,28.1247         |             4.23
 Arapahoe       |  2088.66 |       2085.89 |         0.13 | -104.8085,39.6634   | -104.3392,39.6498        |            40.21
 Archer         |  2403.29 |       2396.86 |         0.27 | -98.6034,33.6477    | -98.6876,33.6152         |             8.59
 Archuleta      |  3515.30 |       3509.05 |         0.18 | -107.0221,37.1522   | -107.0483,37.1935        |             5.15
 Arecibo        |   331.00 |        329.24 |         0.53 | -66.6715,18.3949    | -66.6752,18.4069         |             1.39
 Arenac         |   963.23 |        962.85 |         0.04 | -83.7480,44.0239    | -83.8940,44.0647         |            12.52
 Arkansas       |  2683.93 |       2677.43 |         0.24 | -91.2712,34.2148    | -91.3749,34.2908         |            12.74
 Arlington      |    67.40 |         67.31 |         0.13 | -77.0916,38.8722    | -77.1011,38.8786         |             1.09
 Armstrong      |  2372.13 |       2366.94 |         0.22 | -101.3911,34.9518   | -101.3574,34.9650        |             3.41
 Armstrong      |  1721.01 |       1719.23 |         0.10 | -79.5053,40.8833    | -79.4645,40.8123         |             8.61
 Aroostook      | 17675.88 |      17681.34 |         0.03 | -68.4708,46.9148    | -68.5989,46.6589         |            30.08
 Arroyo         |    39.25 |         39.04 |         0.54 | -66.0577,17.9939    | -66.0563,17.9981         |             0.49
 Arthur         |  1861.60 |       1860.12 |         0.08 | -101.5805,41.5939   | -101.6958,41.5690        |             9.98
 Ascension      |   786.69 |        784.06 |         0.34 | -90.8416,30.2517    | -90.9113,30.2035         |             8.58
 Ashe           |  1114.26 |       1111.94 |         0.21 | -81.4678,36.4020    | -81.5003,36.4342         |             4.62
 Ashland        |  2735.19 |       2736.02 |         0.03 | -90.6547,46.8236    | -90.6779,46.3161         |            56.46
 Ashland        |  1106.62 |       1105.52 |         0.10 | -82.2977,40.8081    | -82.2707,40.8460         |             4.78
(100 rows)

Time: 2764.649 ms
-- SQL statement 49: Drop table cb_2014_us_nation_5m >>>
DROP TABLE IF EXISTS cb_2014_us_nation_5m;
psql:pg_USA_2014.sql:760: NOTICE:  table "cb_2014_us_nation_5m" does not exist, skipping
DROP TABLE
Time: 0.399 ms
-- SQL statement 50: Create tablecb_2014_us_nation_5m >>>
CREATE TABLE cb_2014_us_nation_5m (
	affgeoid                       	text /* American FactFinder summary level code + geovariant code + ''00US'' */,
	geoid                          	text /* Nation identifier */,
	name                           	text /* Nation name */,
	gid                            	integer	NOT NULL /* Unique geographic index */,
	areaid                         	text	NOT NULL /* Area ID (GEOID): Nation identifier */,
	areaname                       	text	NOT NULL /* Area name (NAME): Nation name */,
	area_km2                       	numeric /* Area in square km */,
	geographic_centroid_wkt        	text /* Wellknown text for geographic centroid */,
	wkt_9                          	text /* Wellknown text for zoomlevel 9 */,
	wkt_8                          	text /* Wellknown text for zoomlevel 8 */,
	wkt_7                          	text /* Wellknown text for zoomlevel 7 */,
	wkt_6                          	text /* Wellknown text for zoomlevel 6 */);
CREATE TABLE
Time: 9.598 ms
-- SQL statement 51: Comment geospatial data table >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE cb_2014_us_nation_5m IS 'The nation at a scale of 1:5,000,000';
COMMENT
Time: 0.563 ms
-- SQL statement 52: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.gid IS 'Unique geographic index';
COMMENT
Time: 0.455 ms
-- SQL statement 53: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.areaid IS 'Area ID (GEOID): Nation identifier';
COMMENT
Time: 0.407 ms
-- SQL statement 54: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.areaname IS 'Area name (NAME): Nation name';
COMMENT
Time: 0.332 ms
-- SQL statement 55: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.area_km2 IS 'Area in square km';
COMMENT
Time: 0.337 ms
-- SQL statement 56: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.geographic_centroid_wkt IS 'Wellknown text for geographic centroid';
COMMENT
Time: 0.462 ms
-- SQL statement 57: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.wkt_6 IS 'Wellknown text for zoomlevel 6';
COMMENT
Time: 0.472 ms
-- SQL statement 58: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.wkt_7 IS 'Wellknown text for zoomlevel 7';
COMMENT
Time: 0.419 ms
-- SQL statement 59: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.wkt_8 IS 'Wellknown text for zoomlevel 8';
COMMENT
Time: 0.465 ms
-- SQL statement 60: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.wkt_9 IS 'Wellknown text for zoomlevel 9';
COMMENT
Time: 0.395 ms
-- SQL statement 61: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.affgeoid IS 'American FactFinder summary level code + geovariant code + ''00US''';
COMMENT
Time: 0.437 ms
-- SQL statement 62: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.geoid IS 'Nation identifier';
COMMENT
Time: 1.902 ms
-- SQL statement 63: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_nation_5m.name IS 'Nation name';
COMMENT
Time: 0.426 ms
-- SQL statement 64: Load table from CSV file >>>
\copy cb_2014_us_nation_5m FROM 'cb_2014_us_nation_5m.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 1
Time: 296.459 ms
-- SQL statement 65: Row check: 1 >>>
DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	csvfile_rowcheck.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: Expected number of rows; e.g. 3233
 *						3: Column to count; e.g. gid
 *
 * Description:			Check number of rows in loaded CSV file is as expected
 * Note:				%% becomes % after substitution
 */
	c1 CURSOR FOR
		SELECT COUNT(gid) AS total
		  FROM cb_2014_us_nation_5m;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.total = 1 THEN
		RAISE INFO 'Table: cb_2014_us_nation_5m row check OK: %', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: cb_2014_us_nation_5m row check FAILED: expected: 1 got: %', c1_rec.total;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:1002: INFO:  Table: cb_2014_us_nation_5m row check OK: 1
DO
Time: 0.765 ms
-- SQL statement 66: Add primary key cb_2014_us_nation_5m >>>
ALTER TABLE cb_2014_us_nation_5m ADD PRIMARY KEY (gid);
ALTER TABLE
Time: 6.563 ms
-- SQL statement 67: Add unique key cb_2014_us_nation_5m >>>
/*
 * SQL statement name: 	add_unique_key.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. cb_2014_us_nation_5m
 *						2: constraint name; e.g. cb_2014_us_nation_5m_uk
 *						3: fields; e.g. areaid
 *
 * Description:			Add unique key constraint
 * Note:				%% becomes % after substitution
 */
ALTER TABLE cb_2014_us_nation_5m ADD CONSTRAINT cb_2014_us_nation_5m_uk UNIQUE(areaid);
ALTER TABLE
Time: 6.600 ms
--
-- Add geometric  data
--
-- SQL statement 69: Add geometry column: geographic centroid >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_nation_5m','geographic_centroid', 4326, 'POINT', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                              addgeometrycolumn                              
-----------------------------------------------------------------------------
 peter.cb_2014_us_nation_5m.geographic_centroid SRID:4326 TYPE:POINT DIMS:2 
(1 row)

Time: 6.045 ms
-- SQL statement 70: Add geometry column for original SRID geometry >>>
/*
 * SQL statement name: 	add_geometry_column2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *                      5: Schema (rif_data. or "") [NEVER USED IN POSTGRES]
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_nation_5m','geom_orig', 4269, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                            addgeometrycolumn                             
--------------------------------------------------------------------------
 peter.cb_2014_us_nation_5m.geom_orig SRID:4269 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 4.019 ms
-- SQL statement 71: Add geometry column for zoomlevel: 6 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_nation_5m','geom_6', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                           addgeometrycolumn                           
-----------------------------------------------------------------------
 peter.cb_2014_us_nation_5m.geom_6 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 3.413 ms
-- SQL statement 72: Add geometry column for zoomlevel: 7 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_nation_5m','geom_7', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                           addgeometrycolumn                           
-----------------------------------------------------------------------
 peter.cb_2014_us_nation_5m.geom_7 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 4.612 ms
-- SQL statement 73: Add geometry column for zoomlevel: 8 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_nation_5m','geom_8', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                           addgeometrycolumn                           
-----------------------------------------------------------------------
 peter.cb_2014_us_nation_5m.geom_8 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 4.367 ms
-- SQL statement 74: Add geometry column for zoomlevel: 9 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_nation_5m','geom_9', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                           addgeometrycolumn                           
-----------------------------------------------------------------------
 peter.cb_2014_us_nation_5m.geom_9 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 4.078 ms
-- SQL statement 75: Update geographic centroid, geometry columns, handle polygons and mutlipolygons, convert highest zoomlevel to original SRID >>>
UPDATE cb_2014_us_nation_5m
   SET geographic_centroid = ST_GeomFromText(geographic_centroid_wkt, 4326),
       geom_6 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_6, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_6, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_6, 4326))
       		END,
       geom_7 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_7, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_7, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_7, 4326))
       		END,
       geom_8 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_8, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_8, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_8, 4326))
       		END,
       geom_9 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_9, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_9, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_9, 4326))
       		END,
       geom_orig = ST_Transform(
       		CASE ST_IsCollection(ST_GeomFromText(wkt_9, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_9, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_9, 4326))
       		END, 4269);
UPDATE 1
Time: 433.868 ms
-- SQL statement 76: Make geometry columns valid >>>
UPDATE cb_2014_us_nation_5m
   SET
       geom_6 = CASE ST_IsValid(geom_6)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_6), 3 /* Remove non polygons */)
				ELSE geom_6
			END,
       geom_7 = CASE ST_IsValid(geom_7)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_7), 3 /* Remove non polygons */)
				ELSE geom_7
			END,
       geom_8 = CASE ST_IsValid(geom_8)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_8), 3 /* Remove non polygons */)
				ELSE geom_8
			END,
       geom_9 = CASE ST_IsValid(geom_9)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_9), 3 /* Remove non polygons */)
				ELSE geom_9
			END,
       geom_orig = CASE ST_IsValid(geom_orig)
			WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_orig), 3 /* Remove non polygons */)
			ELSE geom_orig
		END;
psql:pg_USA_2014.sql:1179: NOTICE:  Too few points in geometry component at or near point -122.37778249179249 37.83063545387445
UPDATE 1
Time: 3219.131 ms
--
-- Test geometry and make valid if required
--
-- SQL statement 78: Check validity of geometry columns >>>
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
SELECT areaname,
       6::Text AS geolevel,
       ST_IsValidReason(geom_6) AS reason
  FROM cb_2014_us_nation_5m
 WHERE NOT ST_IsValid(geom_6)
UNION
SELECT areaname,
       7::Text AS geolevel,
       ST_IsValidReason(geom_7) AS reason
  FROM cb_2014_us_nation_5m
 WHERE NOT ST_IsValid(geom_7)
UNION
SELECT areaname,
       8::Text AS geolevel,
       ST_IsValidReason(geom_8) AS reason
  FROM cb_2014_us_nation_5m
 WHERE NOT ST_IsValid(geom_8)
UNION
SELECT areaname,
       9::Text AS geolevel,
       ST_IsValidReason(geom_9) AS reason
  FROM cb_2014_us_nation_5m
 WHERE NOT ST_IsValid(geom_9)
UNION
SELECT areaname,
       'geom_orig'::Text AS geolevel,
       ST_IsValidReason(geom_orig) AS reason
  FROM cb_2014_us_nation_5m
 WHERE NOT ST_IsValid(geom_orig)
 ORDER BY 1, 2;
	c1_rec RECORD;
	total INTEGER:=0;
BEGIN
	FOR c1_rec IN c1 LOOP
		total:=total+1;
		RAISE INFO 'Area: %, geolevel: %: %', c1_rec.areaname, c1_rec.geolevel, c1_rec.reason;
	END LOOP;
	IF total = 0 THEN
		RAISE INFO 'Table: cb_2014_us_nation_5m no invalid geometry check OK';
	ELSE
		RAISE EXCEPTION 'Table: cb_2014_us_nation_5m no invalid geometry check FAILED: % invalid', total;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:1232: INFO:  Table: cb_2014_us_nation_5m no invalid geometry check OK
DO
Time: 1516.958 ms
--
-- Make all polygons right handed
--
-- SQL statement 80: Make all polygons right handed for original geometry >>>
UPDATE cb_2014_us_nation_5m
   SET       geom_6 = ST_ForceRHR(geom_6),
       geom_7 = ST_ForceRHR(geom_7),
       geom_8 = ST_ForceRHR(geom_8),
       geom_9 = ST_ForceRHR(geom_9),
       geom_orig = ST_ForceRHR(geom_orig);
UPDATE 1
Time: 51.389 ms
--
-- Test Turf and DB areas agree to within 1%
--
--
-- Create spatial indexes
--
-- SQL statement 83: Index geometry column for zoomlevel: 6 >>>
CREATE INDEX cb_2014_us_nation_5m_geom_6_gix ON cb_2014_us_nation_5m USING GIST (geom_6);
CREATE INDEX
Time: 2.782 ms
-- SQL statement 84: Index geometry column for zoomlevel: 7 >>>
CREATE INDEX cb_2014_us_nation_5m_geom_7_gix ON cb_2014_us_nation_5m USING GIST (geom_7);
CREATE INDEX
Time: 2.926 ms
-- SQL statement 85: Index geometry column for zoomlevel: 8 >>>
CREATE INDEX cb_2014_us_nation_5m_geom_8_gix ON cb_2014_us_nation_5m USING GIST (geom_8);
CREATE INDEX
Time: 3.352 ms
-- SQL statement 86: Index geometry column for zoomlevel: 9 >>>
CREATE INDEX cb_2014_us_nation_5m_geom_9_gix ON cb_2014_us_nation_5m USING GIST (geom_9);
CREATE INDEX
Time: 3.111 ms
-- SQL statement 87: Index geometry column for original SRID geometry >>>
CREATE INDEX cb_2014_us_nation_5m_geom_orig_gix ON cb_2014_us_nation_5m USING GIST (geom_orig);
CREATE INDEX
Time: 2.640 ms
--
-- Reports
--
-- SQL statement 89: Areas and centroids report >>>
/*
 * SQL statement name: 	area_centroid_report.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Area and centroids report
 * Note:				% becomes % after substitution
 */
WITH a AS (
	SELECT areaname,
		   ROUND(area_km2::numeric, 2) AS area_km2,
		   ROUND(
				(ST_Area(geography(geom_9))/(1000*1000))::numeric, 2) AS area_km2_calc,
		   ROUND(ST_X(geographic_centroid)::numeric, 4)||','||ROUND(ST_Y(geographic_centroid)::numeric, 4) AS geographic_centroid,
		   ROUND(ST_X(ST_Centroid(geom_9))::numeric, 4)||','||ROUND(ST_Y(ST_Centroid(geom_9))::numeric, 4) AS geographic_centroid_calc,
		   ROUND(ST_Distance_Sphere(ST_Centroid(geom_9), geographic_centroid)::numeric/1000, 2) AS centroid_diff_km
	  FROM cb_2014_us_nation_5m
	 GROUP BY areaname, area_km2, geom_9, geographic_centroid
)
SELECT a.areaname,
       a.area_km2,
	   a.area_km2_calc,
	   ROUND(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2_calc), 2) AS pct_km2_diff,
	   a.geographic_centroid,
      a.geographic_centroid_calc,
	   a.centroid_diff_km
  FROM a
 ORDER BY 1
 LIMIT 100;
   areaname    |  area_km2  | area_km2_calc | pct_km2_diff | geographic_centroid | geographic_centroid_calc | centroid_diff_km 
---------------+------------+---------------+--------------+---------------------+--------------------------+------------------
 United States | 9371409.57 |    9366617.11 |         0.05 | -108.5281,45.1076   | -112.9353,45.6992        |           350.26
(1 row)

Time: 76.519 ms
-- SQL statement 90: Drop table cb_2014_us_state_500k >>>
DROP TABLE IF EXISTS cb_2014_us_state_500k;
psql:pg_USA_2014.sql:1307: NOTICE:  table "cb_2014_us_state_500k" does not exist, skipping
DROP TABLE
Time: 0.395 ms
-- SQL statement 91: Create tablecb_2014_us_state_500k >>>
CREATE TABLE cb_2014_us_state_500k (
	statefp                        	text /* Current state Federal Information Processing Series (FIPS) code */,
	statens                        	text /* Current state Geographic Names Information System (GNIS) code */,
	affgeoid                       	text /* American FactFinder summary level code + geovariant code + ''00US'' + GEOID */,
	geoid                          	text /* State identifier; state FIPS code */,
	stusps                         	text /* Current United States Postal Service state abbreviation */,
	name                           	text /* Current State name */,
	lsad                           	text /* Current legal/statistical area description code for state */,
	aland                          	text /* Current land area (square meters) */,
	awater                         	text /* Current water area (square meters) */,
	gid                            	integer	NOT NULL /* Unique geographic index */,
	areaid                         	text	NOT NULL /* Area ID (STATENS): Current state Geographic Names Information System (GNIS) code */,
	areaname                       	text	NOT NULL /* Area name (NAME): Current State name */,
	area_km2                       	numeric /* Area in square km */,
	geographic_centroid_wkt        	text /* Wellknown text for geographic centroid */,
	wkt_9                          	text /* Wellknown text for zoomlevel 9 */,
	wkt_8                          	text /* Wellknown text for zoomlevel 8 */,
	wkt_7                          	text /* Wellknown text for zoomlevel 7 */,
	wkt_6                          	text /* Wellknown text for zoomlevel 6 */);
CREATE TABLE
Time: 11.845 ms
-- SQL statement 92: Comment geospatial data table >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE cb_2014_us_state_500k IS 'The State at a scale of 1:500,000';
COMMENT
Time: 0.461 ms
-- SQL statement 93: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.gid IS 'Unique geographic index';
COMMENT
Time: 0.802 ms
-- SQL statement 94: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.areaid IS 'Area ID (STATENS): Current state Geographic Names Information System (GNIS) code';
COMMENT
Time: 0.496 ms
-- SQL statement 95: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.areaname IS 'Area name (NAME): Current State name';
COMMENT
Time: 0.442 ms
-- SQL statement 96: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.area_km2 IS 'Area in square km';
COMMENT
Time: 0.498 ms
-- SQL statement 97: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.geographic_centroid_wkt IS 'Wellknown text for geographic centroid';
COMMENT
Time: 0.437 ms
-- SQL statement 98: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.wkt_6 IS 'Wellknown text for zoomlevel 6';
COMMENT
Time: 0.411 ms
-- SQL statement 99: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.wkt_7 IS 'Wellknown text for zoomlevel 7';
COMMENT
Time: 0.402 ms
-- SQL statement 100: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.wkt_8 IS 'Wellknown text for zoomlevel 8';
COMMENT
Time: 0.411 ms
-- SQL statement 101: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.wkt_9 IS 'Wellknown text for zoomlevel 9';
COMMENT
Time: 0.414 ms
-- SQL statement 102: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.statefp IS 'Current state Federal Information Processing Series (FIPS) code';
COMMENT
Time: 0.385 ms
-- SQL statement 103: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.statens IS 'Current state Geographic Names Information System (GNIS) code';
COMMENT
Time: 0.334 ms
-- SQL statement 104: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.affgeoid IS 'American FactFinder summary level code + geovariant code + ''00US'' + GEOID';
COMMENT
Time: 0.287 ms
-- SQL statement 105: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.geoid IS 'State identifier; state FIPS code';
COMMENT
Time: 0.324 ms
-- SQL statement 106: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.stusps IS 'Current United States Postal Service state abbreviation';
COMMENT
Time: 0.350 ms
-- SQL statement 107: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.name IS 'Current State name';
COMMENT
Time: 0.316 ms
-- SQL statement 108: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.lsad IS 'Current legal/statistical area description code for state';
COMMENT
Time: 0.382 ms
-- SQL statement 109: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.aland IS 'Current land area (square meters)';
COMMENT
Time: 0.318 ms
-- SQL statement 110: Comment geospatial data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN cb_2014_us_state_500k.awater IS 'Current water area (square meters)';
COMMENT
Time: 0.256 ms
-- SQL statement 111: Load table from CSV file >>>
\copy cb_2014_us_state_500k FROM 'cb_2014_us_state_500k.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 56
Time: 2525.333 ms
-- SQL statement 112: Row check: 56 >>>
DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	csvfile_rowcheck.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: Expected number of rows; e.g. 3233
 *						3: Column to count; e.g. gid
 *
 * Description:			Check number of rows in loaded CSV file is as expected
 * Note:				%% becomes % after substitution
 */
	c1 CURSOR FOR
		SELECT COUNT(gid) AS total
		  FROM cb_2014_us_state_500k;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.total = 56 THEN
		RAISE INFO 'Table: cb_2014_us_state_500k row check OK: %', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: cb_2014_us_state_500k row check FAILED: expected: 56 got: %', c1_rec.total;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:1645: INFO:  Table: cb_2014_us_state_500k row check OK: 56
DO
Time: 2.382 ms
-- SQL statement 113: Add primary key cb_2014_us_state_500k >>>
ALTER TABLE cb_2014_us_state_500k ADD PRIMARY KEY (gid);
ALTER TABLE
Time: 121.292 ms
-- SQL statement 114: Add unique key cb_2014_us_state_500k >>>
/*
 * SQL statement name: 	add_unique_key.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. cb_2014_us_nation_5m
 *						2: constraint name; e.g. cb_2014_us_nation_5m_uk
 *						3: fields; e.g. areaid
 *
 * Description:			Add unique key constraint
 * Note:				%% becomes % after substitution
 */
ALTER TABLE cb_2014_us_state_500k ADD CONSTRAINT cb_2014_us_state_500k_uk UNIQUE(areaid);
ALTER TABLE
Time: 136.271 ms
--
-- Add geometric  data
--
-- SQL statement 116: Add geometry column: geographic centroid >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_state_500k','geographic_centroid', 4326, 'POINT', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                              addgeometrycolumn                               
------------------------------------------------------------------------------
 peter.cb_2014_us_state_500k.geographic_centroid SRID:4326 TYPE:POINT DIMS:2 
(1 row)

Time: 5.559 ms
-- SQL statement 117: Add geometry column for original SRID geometry >>>
/*
 * SQL statement name: 	add_geometry_column2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *                      5: Schema (rif_data. or "") [NEVER USED IN POSTGRES]
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_state_500k','geom_orig', 4269, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                             addgeometrycolumn                             
---------------------------------------------------------------------------
 peter.cb_2014_us_state_500k.geom_orig SRID:4269 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 5.535 ms
-- SQL statement 118: Add geometry column for zoomlevel: 6 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_state_500k','geom_6', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                           addgeometrycolumn                            
------------------------------------------------------------------------
 peter.cb_2014_us_state_500k.geom_6 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 7.640 ms
-- SQL statement 119: Add geometry column for zoomlevel: 7 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_state_500k','geom_7', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                           addgeometrycolumn                            
------------------------------------------------------------------------
 peter.cb_2014_us_state_500k.geom_7 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 3.302 ms
-- SQL statement 120: Add geometry column for zoomlevel: 8 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_state_500k','geom_8', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                           addgeometrycolumn                            
------------------------------------------------------------------------
 peter.cb_2014_us_state_500k.geom_8 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 3.259 ms
-- SQL statement 121: Add geometry column for zoomlevel: 9 >>>
/*
 * SQL statement name: 	add_geometry_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('cb_2014_us_state_500k','geom_9', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                           addgeometrycolumn                            
------------------------------------------------------------------------
 peter.cb_2014_us_state_500k.geom_9 SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 4.280 ms
-- SQL statement 122: Update geographic centroid, geometry columns, handle polygons and mutlipolygons, convert highest zoomlevel to original SRID >>>
UPDATE cb_2014_us_state_500k
   SET geographic_centroid = ST_GeomFromText(geographic_centroid_wkt, 4326),
       geom_6 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_6, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_6, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_6, 4326))
       		END,
       geom_7 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_7, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_7, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_7, 4326))
       		END,
       geom_8 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_8, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_8, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_8, 4326))
       		END,
       geom_9 = 
       		CASE ST_IsCollection(ST_GeomFromText(wkt_9, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_9, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_9, 4326))
       		END,
       geom_orig = ST_Transform(
       		CASE ST_IsCollection(ST_GeomFromText(wkt_9, 4326)) /* Convert to Multipolygon */
       			WHEN true THEN 	ST_GeomFromText(wkt_9, 4326)
       			ELSE 			ST_Multi(ST_GeomFromText(wkt_9, 4326))
       		END, 4269);
UPDATE 56
Time: 4037.814 ms
-- SQL statement 123: Make geometry columns valid >>>
UPDATE cb_2014_us_state_500k
   SET
       geom_6 = CASE ST_IsValid(geom_6)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_6), 3 /* Remove non polygons */)
				ELSE geom_6
			END,
       geom_7 = CASE ST_IsValid(geom_7)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_7), 3 /* Remove non polygons */)
				ELSE geom_7
			END,
       geom_8 = CASE ST_IsValid(geom_8)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_8), 3 /* Remove non polygons */)
				ELSE geom_8
			END,
       geom_9 = CASE ST_IsValid(geom_9)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_9), 3 /* Remove non polygons */)
				ELSE geom_9
			END,
       geom_orig = CASE ST_IsValid(geom_orig)
			WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_orig), 3 /* Remove non polygons */)
			ELSE geom_orig
		END;
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -135.50365499572501 57.149153032043046
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -147.82600316652218 70.239345639639652
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -70.627825293024301 42.976272865436876
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -89.30068193097695 29.382882270765279
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -70.398470468488483 43.392955507969518
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -67.746712340678357 44.465247479402493
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -70.398470468488483 43.392955507969518
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -70.892355035877046 42.328395791758801
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -70.892355035877046 42.328395791758801
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -84.6830765825366 46.491785659208666
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -88.851663330829339 47.912974169059183
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -87.542653870606884 46.704164935979946
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -84.439364648483661 45.996148099462111
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -84.6830765825366 46.491785659208666
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -73.613386217107234 40.988911445423454
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -76.329400888614401 39.31505881204005
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -76.812150217519232 38.252294494373501
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -76.325803132627144 39.313933136389146
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -76.329400888614401 39.31505881204005
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -91.774770828512843 46.946012696542709
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -91.774770828512843 46.946012696542709
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -88.211336246366258 30.322265366322377
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -124.21753120432521 41.950803995072008
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -114.04121103776512 41.850564882761844
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -114.04121103776512 41.850564882761844
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -124.47703595884697 42.667154484440495
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -124.47703595884697 42.667154484440495
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -83.82990534948236 29.973025172117183
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -114.04121103776512 41.850564882761844
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -114.04121103776512 41.850564882761844
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -82.867979011836027 41.645895397474405
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -82.834598732208747 41.591082299343306
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -124.76274243823744 48.177245469766483
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -124.50933945526046 47.798880447557458
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -168.1524398931079 -14.53606964980365
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point -168.1524398931079 -14.53606964980365
psql:pg_USA_2014.sql:1822: NOTICE:  Too few points in geometry component at or near point 145.71084375458872 15.242563490039492
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -64.733513980775001 18.314846231682239
psql:pg_USA_2014.sql:1822: NOTICE:  Self-intersection at or near point -64.733513980775001 18.314846231682239
UPDATE 56
Time: 46127.624 ms
--
-- Test geometry and make valid if required
--
-- SQL statement 125: Check validity of geometry columns >>>
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
SELECT areaname,
       6::Text AS geolevel,
       ST_IsValidReason(geom_6) AS reason
  FROM cb_2014_us_state_500k
 WHERE NOT ST_IsValid(geom_6)
UNION
SELECT areaname,
       7::Text AS geolevel,
       ST_IsValidReason(geom_7) AS reason
  FROM cb_2014_us_state_500k
 WHERE NOT ST_IsValid(geom_7)
UNION
SELECT areaname,
       8::Text AS geolevel,
       ST_IsValidReason(geom_8) AS reason
  FROM cb_2014_us_state_500k
 WHERE NOT ST_IsValid(geom_8)
UNION
SELECT areaname,
       9::Text AS geolevel,
       ST_IsValidReason(geom_9) AS reason
  FROM cb_2014_us_state_500k
 WHERE NOT ST_IsValid(geom_9)
UNION
SELECT areaname,
       'geom_orig'::Text AS geolevel,
       ST_IsValidReason(geom_orig) AS reason
  FROM cb_2014_us_state_500k
 WHERE NOT ST_IsValid(geom_orig)
 ORDER BY 1, 2;
	c1_rec RECORD;
	total INTEGER:=0;
BEGIN
	FOR c1_rec IN c1 LOOP
		total:=total+1;
		RAISE INFO 'Area: %, geolevel: %: %', c1_rec.areaname, c1_rec.geolevel, c1_rec.reason;
	END LOOP;
	IF total = 0 THEN
		RAISE INFO 'Table: cb_2014_us_state_500k no invalid geometry check OK';
	ELSE
		RAISE EXCEPTION 'Table: cb_2014_us_state_500k no invalid geometry check FAILED: % invalid', total;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:1875: INFO:  Table: cb_2014_us_state_500k no invalid geometry check OK
DO
Time: 12453.032 ms
--
-- Make all polygons right handed
--
-- SQL statement 127: Make all polygons right handed for original geometry >>>
UPDATE cb_2014_us_state_500k
   SET       geom_6 = ST_ForceRHR(geom_6),
       geom_7 = ST_ForceRHR(geom_7),
       geom_8 = ST_ForceRHR(geom_8),
       geom_9 = ST_ForceRHR(geom_9),
       geom_orig = ST_ForceRHR(geom_orig);
UPDATE 56
Time: 493.792 ms
--
-- Test Turf and DB areas agree to within 1%
--
--
-- Create spatial indexes
--
-- SQL statement 130: Index geometry column for zoomlevel: 6 >>>
CREATE INDEX cb_2014_us_state_500k_geom_6_gix ON cb_2014_us_state_500k USING GIST (geom_6);
CREATE INDEX
Time: 3.881 ms
-- SQL statement 131: Index geometry column for zoomlevel: 7 >>>
CREATE INDEX cb_2014_us_state_500k_geom_7_gix ON cb_2014_us_state_500k USING GIST (geom_7);
CREATE INDEX
Time: 3.624 ms
-- SQL statement 132: Index geometry column for zoomlevel: 8 >>>
CREATE INDEX cb_2014_us_state_500k_geom_8_gix ON cb_2014_us_state_500k USING GIST (geom_8);
CREATE INDEX
Time: 3.438 ms
-- SQL statement 133: Index geometry column for zoomlevel: 9 >>>
CREATE INDEX cb_2014_us_state_500k_geom_9_gix ON cb_2014_us_state_500k USING GIST (geom_9);
CREATE INDEX
Time: 4.307 ms
-- SQL statement 134: Index geometry column for original SRID geometry >>>
CREATE INDEX cb_2014_us_state_500k_geom_orig_gix ON cb_2014_us_state_500k USING GIST (geom_orig);
CREATE INDEX
Time: 3.436 ms
--
-- Reports
--
-- SQL statement 136: Areas and centroids report >>>
/*
 * SQL statement name: 	area_centroid_report.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Area and centroids report
 * Note:				% becomes % after substitution
 */
WITH a AS (
	SELECT areaname,
		   ROUND(area_km2::numeric, 2) AS area_km2,
		   ROUND(
				(ST_Area(geography(geom_9))/(1000*1000))::numeric, 2) AS area_km2_calc,
		   ROUND(ST_X(geographic_centroid)::numeric, 4)||','||ROUND(ST_Y(geographic_centroid)::numeric, 4) AS geographic_centroid,
		   ROUND(ST_X(ST_Centroid(geom_9))::numeric, 4)||','||ROUND(ST_Y(ST_Centroid(geom_9))::numeric, 4) AS geographic_centroid_calc,
		   ROUND(ST_Distance_Sphere(ST_Centroid(geom_9), geographic_centroid)::numeric/1000, 2) AS centroid_diff_km
	  FROM cb_2014_us_state_500k
	 GROUP BY areaname, area_km2, geom_9, geographic_centroid
)
SELECT a.areaname,
       a.area_km2,
	   a.area_km2_calc,
	   ROUND(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2_calc), 2) AS pct_km2_diff,
	   a.geographic_centroid,
      a.geographic_centroid_calc,
	   a.centroid_diff_km
  FROM a
 ORDER BY 1
 LIMIT 100;
                   areaname                   |  area_km2  | area_km2_calc | pct_km2_diff | geographic_centroid | geographic_centroid_calc | centroid_diff_km 
----------------------------------------------+------------+---------------+--------------+---------------------+--------------------------+------------------
 Alabama                                      |  134271.66 |     133897.60 |         0.28 | -86.7665,31.7691    | -86.8284,32.7898         |           113.65
 Alaska                                       | 1516559.36 |    1522697.98 |         0.40 | -150.4187,58.3829   | -152.2210,64.2175        |           655.80
 American Samoa                               |     203.20 |        201.99 |         0.60 | -170.3028,-14.2006  | -170.3698,-14.2201       |             7.54
 Arizona                                      |  295963.56 |     295235.53 |         0.25 | -113.3962,34.4587   | -111.6644,34.2931        |           160.00
 Arkansas                                     |  138053.82 |     137732.71 |         0.23 | -91.7461,34.7990    | -92.4393,34.8997         |            64.24
 California                                   |  410633.25 |     409888.03 |         0.18 | -120.2495,36.2457   | -119.6107,37.2461        |           124.95
 Colorado                                     |  269980.25 |     269600.62 |         0.14 | -104.8084,38.9256   | -105.5478,38.9985        |            64.45
 Commonwealth of the Northern Mariana Islands |     480.48 |        477.74 |         0.57 | 145.6158,16.1727    | 145.6077,15.9221         |            27.88
 Connecticut                                  |   12941.42 |      12930.94 |         0.08 | -72.8529,41.3457    | -72.7257,41.6203         |            32.32
 Delaware                                     |    5247.47 |       5239.95 |         0.14 | -75.4994,39.2171    | -75.5003,38.9915         |            25.08
 District of Columbia                         |     177.18 |        176.97 |         0.12 | -77.0161,38.8993    | -77.0163,38.9047         |             0.60
 Florida                                      |  151512.42 |     150962.67 |         0.36 | -82.1913,27.2641    | -82.4975,28.6284         |           154.65
 Georgia                                      |  153016.86 |     152586.97 |         0.28 | -82.8985,32.6450    | -83.4463,32.6492         |            51.29
 Guam                                         |     564.01 |        560.58 |         0.61 | 144.7633,13.4280    | 144.7742,13.4438         |             2.11
 Hawaii                                       |   16835.76 |      16749.69 |         0.51 | -157.7523,21.0612   | -156.3631,20.2548        |           170.10
 Idaho                                        |  216480.01 |     216445.19 |         0.02 | -114.6750,45.4195   | -114.6594,44.3891        |           114.58
 Illinois                                     |  146089.13 |     145919.05 |         0.12 | -89.2220,39.8411    | -89.1984,40.0650         |            24.99
 Indiana                                      |   93834.86 |      93722.30 |         0.12 | -86.6396,39.2584    | -86.2756,39.9081         |            78.69
 Iowa                                         |  145844.14 |     145742.34 |         0.07 | -93.8228,42.0221    | -93.5001,42.0746         |            27.28
 Kansas                                       |  213421.42 |     213096.15 |         0.15 | -96.9603,38.7478    | -98.3802,38.4847         |           126.78
 Kentucky                                     |  104841.59 |     104658.47 |         0.17 | -85.2123,37.7224    | -85.2905,37.5267         |            22.83
 Louisiana                                    |  122527.32 |     122142.32 |         0.32 | -91.2370,30.3596    | -91.9856,31.0584         |           105.64
 Maine                                        |   84895.48 |      84901.80 |         0.01 | -68.9047,44.5331    | -69.2267,45.3714         |            96.59
 Maryland                                     |   26683.38 |      26646.73 |         0.14 | -76.5695,38.7769    | -76.7724,39.0369         |            33.82
 Massachusetts                                |   21269.26 |      21255.42 |         0.07 | -70.8497,41.9575    | -71.7985,42.2533         |            84.91
 Michigan                                     |  151072.91 |     151047.18 |         0.02 | -85.7125,45.4651    | -85.4372,44.3507         |           125.80
 Minnesota                                    |  218495.03 |     218557.53 |         0.03 | -94.2946,46.9143    | -94.3090,46.3163         |            66.50
 Mississippi                                  |  123840.91 |     123495.21 |         0.28 | -90.2099,32.2488    | -89.6652,32.7509         |            75.67
 Missouri                                     |  180825.62 |     180544.22 |         0.16 | -92.1883,38.3682    | -92.4774,38.3676         |            25.20
 Montana                                      |  380654.05 |     380831.58 |         0.05 | -112.4402,46.0954   | -109.6451,47.0335        |           237.77
 Nebraska                                     |  200500.14 |     200334.43 |         0.08 | -97.8497,41.4427    | -99.8108,41.5271         |           163.63
 Nevada                                       |  286754.89 |     286372.77 |         0.13 | -115.9925,37.9128   | -116.6552,39.3562        |           170.51
 New Hampshire                                |   24046.36 |      24038.51 |         0.03 | -71.5978,43.9586    | -71.5776,43.6856         |            30.39
 New Jersey                                   |   20173.21 |      20150.47 |         0.11 | -74.7342,40.1862    | -74.6609,40.1842         |             6.23
 New Mexico                                   |  315686.58 |     314916.82 |         0.24 | -106.2103,34.0797   | -106.1084,34.4214        |            39.13
 New York                                     |  127049.38 |     126986.52 |         0.05 | -74.9314,42.2530    | -75.5031,42.9404         |            89.63
 North Carolina                               |  128880.33 |     128599.17 |         0.22 | -78.1838,35.4144    | -79.3724,35.5415         |           108.55
 North Dakota                                 |  183006.42 |     183108.85 |         0.06 | -97.7974,47.3592    | -100.4693,47.4463        |           201.31
 Ohio                                         |  107013.72 |     106894.42 |         0.11 | -82.4310,40.3799    | -82.7901,40.2918         |            31.98
 Oklahoma                                     |  181432.18 |     181035.08 |         0.22 | -97.1409,34.7125    | -97.5083,35.5835         |           102.45
 Oregon                                       |  251404.32 |     251335.95 |         0.03 | -121.2657,44.5161   | -120.5554,43.9366        |            85.76
 Pennsylvania                                 |  117457.55 |     117343.70 |         0.10 | -76.8261,40.9802    | -77.7996,40.8738         |            82.64
 Puerto Rico                                  |    9078.68 |       9029.78 |         0.54 | -66.2424,18.2032    | -66.4650,18.2220         |            23.61
 Rhode Island                                 |    2848.58 |       2846.23 |         0.08 | -71.3999,41.5702    | -71.5536,41.6761         |            17.38
 South Carolina                               |   80794.71 |      80589.34 |         0.25 | -81.3588,33.5808    | -80.8961,33.9080         |            56.15
 South Dakota                                 |  199756.73 |     199726.92 |         0.01 | -98.1393,43.8930    | -100.2305,44.4362        |           177.39
 Tennessee                                    |  109382.29 |     109150.38 |         0.21 | -85.8408,35.8337    | -86.3434,35.8430         |            45.32
 Texas                                        |  689860.28 |     687753.43 |         0.31 | -98.7725,29.8220    | -99.3507,31.4845         |           192.96
 United States Virgin Islands                 |     357.81 |        355.95 |         0.52 | -64.8355,18.1760    | -64.8020,17.9690         |            23.28
 Utah                                         |  220178.60 |     219881.36 |         0.14 | -111.6210,39.1152   | -111.6782,39.3238        |            23.71
 Vermont                                      |   24908.02 |      24902.41 |         0.02 | -72.4363,44.0670    | -72.6626,44.0752         |            18.11
 Virginia                                     |  105025.77 |     104842.89 |         0.17 | -78.0542,37.6982    | -78.8123,37.5150         |            69.82
 Washington                                   |  175346.49 |     175443.55 |         0.06 | -122.6400,47.7959   | -120.4469,47.3810        |           170.82
 West Virginia                                |   62848.44 |      62753.77 |         0.15 | -80.3551,38.7506    | -80.6137,38.6426         |            25.45
 Wisconsin                                    |  145379.21 |     145364.17 |         0.01 | -89.5095,45.1986    | -90.0117,44.6380         |            73.82
 Wyoming                                      |  253457.97 |     253335.86 |         0.05 | -107.5718,42.9599   | -107.5514,42.9996        |             4.72
(56 rows)

Time: 633.579 ms
--
-- Geography meta data
--
--
-- Drop dependent objects: tiles view and generate_series() [MS SQL Server only]
--
-- SQL statement 139: Drop dependent object - view tiles_usa_2014 >>>
DROP VIEW IF EXISTS tiles_usa_2014;
psql:pg_USA_2014.sql:1958: NOTICE:  view "tiles_usa_2014" does not exist, skipping
DROP VIEW
Time: 0.436 ms
-- SQL statement 140: Drop dependent object - FK table geolevels_usa_2014 >>>
DROP TABLE IF EXISTS geolevels_usa_2014;
psql:pg_USA_2014.sql:1961: NOTICE:  table "geolevels_usa_2014" does not exist, skipping
DROP TABLE
Time: 0.389 ms
-- SQL statement 141: Drop table geography_usa_2014 >>>
DROP TABLE IF EXISTS geography_usa_2014;
psql:pg_USA_2014.sql:1964: NOTICE:  table "geography_usa_2014" does not exist, skipping
DROP TABLE
Time: 0.641 ms
-- SQL statement 142: Create geography meta data table >>>
/*
 * SQL statement name: 	create_geography_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: geography table; e.g. geography_cb_2014_us_county_500k
 *
 * Description:			Create geography table compatible with RIF40_GEOGRAPHIES
 *
 *		CREATE TABLE rif40_geographies
 *		(
 *		  geography character varying(50) NOT NULL, -- Geography name
 *		  description character varying(250) NOT NULL, -- Description
 *		  hierarchytable character varying(30) NOT NULL, -- Hierarchy table
 *		  tiletable character varying(30) NOT NULL, -- Tile table
 *		  adjacencytable character varying(30) NOT NULL, -- Adjacency table
 *		  geometrytable character varying(30) NOT NULL, -- Geometry table
 *		  srid integer DEFAULT 0, -- Postgres projection SRID
 *		  defaultcomparea character varying(30), -- Default comparison area
 *		  defaultstudyarea character varying(30), -- Default study area
 *		  postal_population_table character varying(30), -- Postal population table. Table of postal points (e.g. postcodes, ZIP codes); geolevels; X and YCOORDINATES (in projection SRID); male, female and total populations. Converted to SRID points by loader [not in 4326 Web Mercator lat/long]. Used in creating population wieght centroids and in converting postal points to geolevels. Expected columns &lt;postal_point_column&gt;, XCOORDINATE, YCOORDINATE, 1+ &lt;GEOLEVEL_NAME&gt;, MALES, FEMALES, TOTAL
 *		  postal_point_column character varying(30), -- Column name for postal points (e.g. POSTCODE, ZIP_CODE)
 *		  partition smallint DEFAULT 0, -- Enable partitioning. Extract tables will be partition if the number of years >= 2x the RIF40_PARAMETERS parameters Parallelisation [which has a default of 4, so extracts covering 8 years or more will be partitioned].
 *		  max_geojson_digits smallint DEFAULT 8, -- Max digits in ST_AsGeoJson() [optimises file size by removing unecessary precision, the default value of 8 is normally fine.]
 *		  CONSTRAINT rif40_geographies_pk PRIMARY KEY (geography),
 *		  CONSTRAINT partition_ck CHECK (partition = ANY (ARRAY[0, 1])),
 *		  CONSTRAINT postal_population_table_ck CHECK (postal_population_table IS NOT NULL AND postal_point_column IS NOT NULL OR postal_population_table IS NULL AND postal_point_column IS NULL)
 *		)
 *		 
 * Note:				%% becomes % after substitution
 */
CREATE TABLE geography_usa_2014 (
       geography               VARCHAR(50)  NOT NULL,
       description             VARCHAR(250) NOT NULL,
       hierarchytable          VARCHAR(30)  NOT NULL,
       geometrytable           VARCHAR(30)  NOT NULL,
       tiletable               VARCHAR(30)  NOT NULL,			/* New for DB */
	   adjacencytable 		   VARCHAR(30) NOT NULL,			/* New for DB */
       srid                    INTEGER      NOT NULL DEFAULT 0,
       defaultcomparea         VARCHAR(30)  NULL,
       defaultstudyarea        VARCHAR(30)  NULL,
       minzoomlevel       	   INTEGER      NOT NULL DEFAULT 6,  /* New for DB */
       maxzoomlevel       	   INTEGER      NOT NULL DEFAULT 11, /* New for DB */
       postal_population_table VARCHAR(30)  NULL,
       postal_point_column 	   VARCHAR(30)  NULL,
       partition 			   INTEGER      NOT NULL DEFAULT 0, 
       max_geojson_digits 	   INTEGER      NOT NULL DEFAULT 8, 	   
       CONSTRAINT geography_usa_2014_pk PRIMARY KEY(geography),
	   CONSTRAINT geography_usa_2014_part_ck CHECK (partition IN (0, 1)),
	   CONSTRAINT geography_usa_2014_ppt_ck CHECK (
			postal_population_table IS NOT NULL AND postal_point_column IS NOT NULL OR postal_population_table IS NULL AND postal_point_column IS NULL)
	);
CREATE TABLE
Time: 19.105 ms
-- SQL statement 143: Comment geography meta data table >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE geography_usa_2014 IS 'Hierarchial geographies. Usually based on Census geography';
COMMENT
Time: 0.410 ms
-- SQL statement 144: Populate geography meta data table >>>
/*
 * SQL statement name: 	insert_geography.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. GEOGRAPHY_CB_2014_US_COUNTY_500K
 *						2: geography; e.g. CB_2014_US_500K
 *						3: geography description; e.g. United states to county level
 *						4: hierarchytable; e.g. HIERARCHY_CB_2014_US_500K
 *						5: geometrytable; e.g. GEOMETRY_CB_2014_US_500K
 *						6: tiletable; e.g. TILES_CB_2014_US_500K
 * 						7: SRID; e.g. 4269
 *						8: Default comparision area, e.g. GEOID
 *						9: Default study area, e.g. STATENS
 *						10: Min zoomlevel
 *						11: Max zoomlevel
 *           			12: Postal population table (quote enclosed or NULL)
 *      				13: Postal point column (quote enclosed or NULL)
 *						14: Partition (0/1)
 *						15: Max geojson digits
 *						16: adjacencytable; e.g. ADJACENCY_CB_2014_US_500K
 *
 * Description:			Insert into geography table
 * Note:				%% becomes % after substitution
 */
INSERT INTO geography_usa_2014 (
geography, description, hierarchytable, geometrytable, tiletable, adjacencytable, srid, defaultcomparea, defaultstudyarea, minzoomlevel, maxzoomlevel,
		postal_population_table, postal_point_column, partition, max_geojson_digits)
SELECT 'USA_2014' AS geography,
       'US 2014 Census geography to county level' AS description,
       'HIERARCHY_USA_2014' AS hierarchytable,
	   'GEOMETRY_USA_2014' AS geometrytable,
	   'TILES_USA_2014' AS tiletable,
	   'ADJACENCY_USA_2014' AS adjacencytable,
       4269   AS srid,
       NULL AS defaultcomparea,	/* See: update_geography.sql */
       NULL AS defaultstudyarea,
	   6  AS minzoomlevel,
	   9  AS maxzoomlevel,
	   NULL  AS postal_population_table,
       NULL  AS postal_point_column,
       1  AS partition, 
       6  AS max_geojson_digits;
INSERT 0 1
Time: 1.797 ms
-- SQL statement 145: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.geography IS 'Geography name';
COMMENT
Time: 0.526 ms
-- SQL statement 146: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.description IS 'Description';
COMMENT
Time: 0.357 ms
-- SQL statement 147: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.hierarchytable IS 'Hierarchy table';
COMMENT
Time: 0.288 ms
-- SQL statement 148: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.geometrytable IS 'Geometry table';
COMMENT
Time: 0.445 ms
-- SQL statement 149: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.tiletable IS 'Tile table';
COMMENT
Time: 0.412 ms
-- SQL statement 150: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.srid IS 'Projection SRID';
COMMENT
Time: 0.413 ms
-- SQL statement 151: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.defaultcomparea IS 'Default comparison area: lowest resolution geolevel';
COMMENT
Time: 0.405 ms
-- SQL statement 152: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.defaultstudyarea IS 'Default study area: highest resolution geolevel';
COMMENT
Time: 0.379 ms
-- SQL statement 153: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.minzoomlevel IS 'Min zoomlevel';
COMMENT
Time: 0.314 ms
-- SQL statement 154: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.maxzoomlevel IS 'Max zoomlevel';
COMMENT
Time: 0.392 ms
-- SQL statement 155: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.postal_population_table IS 'Postal_population_table';
COMMENT
Time: 0.696 ms
-- SQL statement 156: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.postal_point_column IS 'Postal_point_column';
COMMENT
Time: 0.424 ms
-- SQL statement 157: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.partition IS 'Partition geometry and tile tables (0/1)';
COMMENT
Time: 0.721 ms
-- SQL statement 158: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.max_geojson_digits IS 'Maximum digits in geojson (topojson quantisation)';
COMMENT
Time: 0.843 ms
-- SQL statement 159: Comment geography meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geography_usa_2014.adjacencytable IS 'Adjacency table';
COMMENT
Time: 0.450 ms
--
-- Geolevels meta data
--
-- SQL statement 161: Create geolevels meta data table >>>
/*
 * SQL statement name: 	create_geolevels_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: geolevels table; e.g. geolevels_cb_2014_us_county_500k
 *						2: geography table; e.g. geography_cb_2014_us_county_500k
 *
 * Description:			Create geography table compatible with RIF40_GEOGRAPHIES
 *
 *	CREATE TABLE t_rif40_geolevels
 *	(
 *	  geography character varying(50) NOT NULL, -- Geography (e.g EW2001)
 *	  geolevel_name character varying(30) NOT NULL, -- Name of geolevel. This will be a column name in the numerator/denominator tables
 *	  geolevel_id smallint NOT NULL, -- ID for ordering (1=lowest resolution). Up to 99 supported.
 *	  description character varying(250) NOT NULL, -- Description
 *	  lookup_table character varying(30) NOT NULL, -- Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is &quot;Westminster&quot;
 *	  lookup_desc_column character varying(30) NOT NULL, -- Lookup table description column name.
 *	  centroidxcoordinate_column character varying(30), -- Lookup table centroid X co-ordinate column name. Can also use CENTROIDSFILE instead.
 *	  centroidycoordinate_column character varying(30), -- Lookup table centroid Y co-ordinate column name.
 *	  shapefile character varying(512), -- Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead,
 *	  centroidsfile character varying(512), -- Location of the GIS centroids file. Can also use CENTROIDXCOORDINATE_COLUMN, CENTROIDYCOORDINATE_COLUMN instead.
 *	  shapefile_table character varying(30), -- Table containing GIS shape file data (created using shp2pgsql).
 *	  shapefile_area_id_column character varying(30), -- Column containing the AREA_IDs in SHAPEFILE_TABLE
 *	  shapefile_desc_column character varying(30), -- Column containing the AREA_ID descriptions in SHAPEFILE_TABLE
 *	  centroids_table character varying(30), -- Table containing GIS shape file data with Arc GIS calculated population weighted centroids (created using shp2pgsql). PostGIS does not support population weighted centroids.
 *	  centroids_area_id_column character varying(30), -- Column containing the AREA_IDs in CENTROIDS_TABLE. X and Y co-ordinates ciolumns are asummed to be named after CENTROIDXCOORDINATE_COLUMN and CENTROIDYCOORDINATE_COLUMN.
 *	  covariate_table character varying(30), -- Name of table used for covariates at this geolevel
 *	  restricted smallint DEFAULT 0, -- Is geolevel access rectricted by Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. This is enforced by the RIF application.
 *	  resolution smallint NOT NULL, -- Can use a map for selection at this resolution (0/1)
 *	  comparea smallint NOT NULL, -- Able to be used as a comparison area (0/1)
 *	  listing smallint NOT NULL, -- Able to be used in a disease map listing (0/1)
 *	  CONSTRAINT t_rif40_geolevels_pk PRIMARY KEY (geography, geolevel_name),
 *	  CONSTRAINT t_rif40_geol_comparea_ck CHECK (comparea IN (0, 1)),
 *	  CONSTRAINT t_rif40_geol_listing_ck CHECK (listing iN (0, 1)),
 *	  CONSTRAINT t_rif40_geol_resolution_ck CHECK (resolution IN (0, 1)),
 *	  CONSTRAINT t_rif40_geol_restricted_ck CHECK (restricted IN (0, 1))
 *	)
 *		 
 * Note:				%% becomes % after substitution
*/
CREATE TABLE geolevels_usa_2014 (
       geography                       VARCHAR(50)  NOT NULL,
       geolevel_name                   VARCHAR(30)  NOT NULL,
       geolevel_id			           INTEGER	    NOT NULL,
       description                     VARCHAR(250) NOT NULL,
       lookup_table                    VARCHAR(30)  NOT NULL,
       lookup_desc_column              VARCHAR(30)  NOT NULL,
       shapefile                       VARCHAR(512) NOT NULL,
       shapefile_table                 VARCHAR(30)  NULL,
       shapefile_area_id_column        VARCHAR(30)  NOT NULL,
       shapefile_desc_column           VARCHAR(30)  NULL,
	   centroids_table 				   VARCHAR(30)  NULL, 
	   centroids_area_id_column 	   VARCHAR(30)  NULL,
	   covariate_table 				   VARCHAR(30)  NULL, 
       restricted 					   INTEGER      NULL DEFAULT 0,
       resolution                      INTEGER      NULL,
       comparea                        INTEGER      NULL,
       listing                         INTEGER      NULL,
	   areaid_count 				   INTEGER      NULL,
       CONSTRAINT geolevels_usa_2014_pk PRIMARY KEY(geography, geolevel_name),
	   CONSTRAINT geolevels_usa_2014_fk FOREIGN KEY (geography)
			REFERENCES geography_usa_2014 (geography), 
	   CONSTRAINT geolevels_usa_2014_comparea_ck CHECK (comparea IN (0, 1)),
	   CONSTRAINT geolevels_usa_2014_listing_ck CHECK (listing iN (0, 1)),
	   CONSTRAINT geolevels_usa_2014_resolution_ck CHECK (resolution IN (0, 1)),
	   CONSTRAINT geolevels_usa_2014_restricted_ck CHECK (restricted IN (0, 1))
);
CREATE TABLE
Time: 18.632 ms
-- SQL statement 162: Comment geolevels meta data table >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE geolevels_usa_2014 IS 'Geolevels: hierarchy of level within a geography';
COMMENT
Time: 0.468 ms
-- SQL statement 163: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.geography IS 'Geography (e.g EW2001)';
COMMENT
Time: 0.383 ms
-- SQL statement 164: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.geolevel_name IS 'Name of geolevel. This will be a column name in the numerator/denominator tables';
COMMENT
Time: 0.702 ms
-- SQL statement 165: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT
Time: 0.760 ms
-- SQL statement 166: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.description IS 'Description';
COMMENT
Time: 0.945 ms
-- SQL statement 167: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.lookup_table IS 'Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is "Westminster"';
COMMENT
Time: 0.672 ms
-- SQL statement 168: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.lookup_desc_column IS 'Lookup table description column name.';
COMMENT
Time: 0.654 ms
-- SQL statement 169: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.shapefile IS 'Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead';
COMMENT
Time: 0.439 ms
-- SQL statement 170: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.shapefile_table IS 'Table containing GIS shape file data.';
COMMENT
Time: 0.771 ms
-- SQL statement 171: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.shapefile_area_id_column IS 'Column containing the AREA_IDs in SHAPEFILE_TABLE';
COMMENT
Time: 0.482 ms
-- SQL statement 172: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.shapefile_desc_column IS 'Column containing the AREA_ID descriptions in SHAPEFILE_TABLE';
COMMENT
Time: 0.662 ms
-- SQL statement 173: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.resolution IS 'Can use a map for selection at this resolution (0/1)';
COMMENT
Time: 0.622 ms
-- SQL statement 174: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.comparea IS 'Able to be used as a comparison area (0/1)';
COMMENT
Time: 0.691 ms
-- SQL statement 175: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.listing IS 'Able to be used in a disease map listing (0/1)';
COMMENT
Time: 0.899 ms
-- SQL statement 176: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.areaid_count IS 'Total number of area IDs within the geolevel';
COMMENT
Time: 0.625 ms
-- SQL statement 177: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.centroids_table IS 'Centroids table';
COMMENT
Time: 0.705 ms
-- SQL statement 178: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.centroids_area_id_column IS 'Centroids area id column';
COMMENT
Time: 0.608 ms
-- SQL statement 179: Comment geolevels meta data column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geolevels_usa_2014.covariate_table IS 'Covariate table';
COMMENT
Time: 0.317 ms
-- SQL statement 180: Insert geolevels meta data for: cb_2014_us_nation_5m >>>
/*
 * SQL statement name: 	insert_geolevel.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. GEOLEVELS_CB_2014_US_COUNTY_500K
 *						2: geography; e.g. CB_2014_US_500K
 *						3: Geolevel name; e.g. CB_2014_US_COUNTY_500K
 *						4: Geolevel id; e.g. 3
 *						5: Geolevel description; e.g. "The State-County at a scale of 1:500,000"
 *						6: lookup table; e.g. LOOKUP_CB_2014_US_COUNTY_500K
 * 						7: shapefile; e.g. cb_2014_us_county_500k.shp
 *						8: shapefile table; e.g. CB_2014_US_COUNTY_500K
 *						9: covariate_table; e.g. CB_2014_US_500K_COVARIATES_CB_2014_US_COUNTY_500K
 *						10: shapefile_area_id_column; e.g. COUNTYNS
 *						11: shapefile_desc_column; e.g. NAME
 * 						12: lookup_desc_column; e.g. AREANAME
 *						13: resolution: Can use a map for selection at this resolution (0/1)
 *						14: comparea: Able to be used as a comparison area (0/1)
 *						15: listing: Able to be used in a disease map listing (0/1)
 *
 * Description:			Insert into geography table
 * Note:				%% becomes % after substitution
 */
INSERT INTO geolevels_usa_2014 (
   geography, geolevel_name, geolevel_id, description, lookup_table,
   lookup_desc_column, shapefile, shapefile_table, shapefile_area_id_column, shapefile_desc_column,
   resolution, comparea, listing, covariate_table)
SELECT 'USA_2014' AS geography,
       'CB_2014_US_NATION_5M' AS geolevel_name,
       1 AS geolevel_id,
       'The nation at a scale of 1:5,000,000' AS description,
       'LOOKUP_CB_2014_US_NATION_5M' AS lookup_table,
       'AREANAME' AS lookup_desc_column,
       'cb_2014_us_nation_5m.shp' AS shapefile,
       'CB_2014_US_NATION_5M' AS shapefile_table,
       'GEOID' AS shapefile_area_id_column,
       'NAME' AS shapefile_desc_column,
       1 AS resolution,
       1 AS comparea,
       1 AS listing,
	   NULL AS covariate_table;
INSERT 0 1
Time: 2.762 ms
-- SQL statement 181: Insert geolevels meta data for: cb_2014_us_state_500k >>>
/*
 * SQL statement name: 	insert_geolevel.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. GEOLEVELS_CB_2014_US_COUNTY_500K
 *						2: geography; e.g. CB_2014_US_500K
 *						3: Geolevel name; e.g. CB_2014_US_COUNTY_500K
 *						4: Geolevel id; e.g. 3
 *						5: Geolevel description; e.g. "The State-County at a scale of 1:500,000"
 *						6: lookup table; e.g. LOOKUP_CB_2014_US_COUNTY_500K
 * 						7: shapefile; e.g. cb_2014_us_county_500k.shp
 *						8: shapefile table; e.g. CB_2014_US_COUNTY_500K
 *						9: covariate_table; e.g. CB_2014_US_500K_COVARIATES_CB_2014_US_COUNTY_500K
 *						10: shapefile_area_id_column; e.g. COUNTYNS
 *						11: shapefile_desc_column; e.g. NAME
 * 						12: lookup_desc_column; e.g. AREANAME
 *						13: resolution: Can use a map for selection at this resolution (0/1)
 *						14: comparea: Able to be used as a comparison area (0/1)
 *						15: listing: Able to be used in a disease map listing (0/1)
 *
 * Description:			Insert into geography table
 * Note:				%% becomes % after substitution
 */
INSERT INTO geolevels_usa_2014 (
   geography, geolevel_name, geolevel_id, description, lookup_table,
   lookup_desc_column, shapefile, shapefile_table, shapefile_area_id_column, shapefile_desc_column,
   resolution, comparea, listing, covariate_table)
SELECT 'USA_2014' AS geography,
       'CB_2014_US_STATE_500K' AS geolevel_name,
       2 AS geolevel_id,
       'The State at a scale of 1:500,000' AS description,
       'LOOKUP_CB_2014_US_STATE_500K' AS lookup_table,
       'AREANAME' AS lookup_desc_column,
       'cb_2014_us_state_500k.shp' AS shapefile,
       'CB_2014_US_STATE_500K' AS shapefile_table,
       'STATENS' AS shapefile_area_id_column,
       'NAME' AS shapefile_desc_column,
       1 AS resolution,
       1 AS comparea,
       1 AS listing,
	   'COV_CB_2014_US_STATE_500K' AS covariate_table;
INSERT 0 1
Time: 1.244 ms
-- SQL statement 182: Insert geolevels meta data for: cb_2014_us_county_500k >>>
/*
 * SQL statement name: 	insert_geolevel.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. GEOLEVELS_CB_2014_US_COUNTY_500K
 *						2: geography; e.g. CB_2014_US_500K
 *						3: Geolevel name; e.g. CB_2014_US_COUNTY_500K
 *						4: Geolevel id; e.g. 3
 *						5: Geolevel description; e.g. "The State-County at a scale of 1:500,000"
 *						6: lookup table; e.g. LOOKUP_CB_2014_US_COUNTY_500K
 * 						7: shapefile; e.g. cb_2014_us_county_500k.shp
 *						8: shapefile table; e.g. CB_2014_US_COUNTY_500K
 *						9: covariate_table; e.g. CB_2014_US_500K_COVARIATES_CB_2014_US_COUNTY_500K
 *						10: shapefile_area_id_column; e.g. COUNTYNS
 *						11: shapefile_desc_column; e.g. NAME
 * 						12: lookup_desc_column; e.g. AREANAME
 *						13: resolution: Can use a map for selection at this resolution (0/1)
 *						14: comparea: Able to be used as a comparison area (0/1)
 *						15: listing: Able to be used in a disease map listing (0/1)
 *
 * Description:			Insert into geography table
 * Note:				%% becomes % after substitution
 */
INSERT INTO geolevels_usa_2014 (
   geography, geolevel_name, geolevel_id, description, lookup_table,
   lookup_desc_column, shapefile, shapefile_table, shapefile_area_id_column, shapefile_desc_column,
   resolution, comparea, listing, covariate_table)
SELECT 'USA_2014' AS geography,
       'CB_2014_US_COUNTY_500K' AS geolevel_name,
       3 AS geolevel_id,
       'The County at a scale of 1:500,000' AS description,
       'LOOKUP_CB_2014_US_COUNTY_500K' AS lookup_table,
       'AREANAME' AS lookup_desc_column,
       'cb_2014_us_county_500k.shp' AS shapefile,
       'CB_2014_US_COUNTY_500K' AS shapefile_table,
       'COUNTYNS' AS shapefile_area_id_column,
       'NAME' AS shapefile_desc_column,
       1 AS resolution,
       1 AS comparea,
       1 AS listing,
	   'COV_CB_2014_US_COUNTY_500K' AS covariate_table;
INSERT 0 1
Time: 1.453 ms
--
-- Create Geolevels lookup tables
--
-- SQL statement 184: Drop table lookup_cb_2014_us_nation_5m >>>
DROP TABLE IF EXISTS lookup_cb_2014_us_nation_5m;
psql:pg_USA_2014.sql:2778: NOTICE:  table "lookup_cb_2014_us_nation_5m" does not exist, skipping
DROP TABLE
Time: 13.241 ms
-- SQL statement 185: Create table lookup_cb_2014_us_nation_5m >>>
/*
 * SQL statement name: 	create_lookup_table.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: table; e.g. lookup_sahsu_grd_level1
 * 						2: Lookup column - shapefile table name, e.g. sahsu_grd_level1
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create lookup table
 * Note:				%% becomes % after substitution
 */
CREATE TABLE lookup_cb_2014_us_nation_5m (
	cb_2014_us_nation_5m			VARCHAR(100)  NOT NULL,
	areaname	VARCHAR(1000),
	gid			INTEGER		  NOT NULL,
	geographic_centroid		JSON,
	PRIMARY KEY (cb_2014_us_nation_5m)
);
CREATE TABLE
Time: 14.670 ms
-- SQL statement 186: Comment table lookup_cb_2014_us_nation_5m >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE lookup_cb_2014_us_nation_5m IS 'Lookup table for The County at a scale of 1:500,000';
COMMENT
Time: 0.573 ms
-- SQL statement 187: Comment lookup_cb_2014_us_nation_5m columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_nation_5m.cb_2014_us_nation_5m IS 'Area ID field';
COMMENT
Time: 1.123 ms
-- SQL statement 188: Comment lookup_cb_2014_us_nation_5m columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_nation_5m.gid IS 'GID field';
COMMENT
Time: 0.640 ms
-- SQL statement 189: Comment lookup_cb_2014_us_nation_5m columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_nation_5m.areaname IS 'Area Name field';
COMMENT
Time: 0.776 ms
-- SQL statement 190: Comment lookup_cb_2014_us_nation_5m columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_nation_5m.geographic_centroid IS 'Geographic centroid';
COMMENT
Time: 0.584 ms
-- SQL statement 191: Drop table lookup_cb_2014_us_state_500k >>>
DROP TABLE IF EXISTS lookup_cb_2014_us_state_500k;
psql:pg_USA_2014.sql:2875: NOTICE:  table "lookup_cb_2014_us_state_500k" does not exist, skipping
DROP TABLE
Time: 0.554 ms
-- SQL statement 192: Create table lookup_cb_2014_us_state_500k >>>
/*
 * SQL statement name: 	create_lookup_table.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: table; e.g. lookup_sahsu_grd_level1
 * 						2: Lookup column - shapefile table name, e.g. sahsu_grd_level1
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create lookup table
 * Note:				%% becomes % after substitution
 */
CREATE TABLE lookup_cb_2014_us_state_500k (
	cb_2014_us_state_500k			VARCHAR(100)  NOT NULL,
	areaname	VARCHAR(1000),
	gid			INTEGER		  NOT NULL,
	geographic_centroid		JSON,
	PRIMARY KEY (cb_2014_us_state_500k)
);
CREATE TABLE
Time: 14.879 ms
-- SQL statement 193: Comment table lookup_cb_2014_us_state_500k >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE lookup_cb_2014_us_state_500k IS 'Lookup table for The nation at a scale of 1:5,000,000';
COMMENT
Time: 0.578 ms
-- SQL statement 194: Comment lookup_cb_2014_us_state_500k columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_state_500k.cb_2014_us_state_500k IS 'Area ID field';
COMMENT
Time: 0.518 ms
-- SQL statement 195: Comment lookup_cb_2014_us_state_500k columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_state_500k.gid IS 'GID field';
COMMENT
Time: 0.774 ms
-- SQL statement 196: Comment lookup_cb_2014_us_state_500k columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_state_500k.areaname IS 'Area Name field';
COMMENT
Time: 0.402 ms
-- SQL statement 197: Comment lookup_cb_2014_us_state_500k columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_state_500k.geographic_centroid IS 'Geographic centroid';
COMMENT
Time: 0.772 ms
-- SQL statement 198: Drop table lookup_cb_2014_us_county_500k >>>
DROP TABLE IF EXISTS lookup_cb_2014_us_county_500k;
psql:pg_USA_2014.sql:2972: NOTICE:  table "lookup_cb_2014_us_county_500k" does not exist, skipping
DROP TABLE
Time: 0.564 ms
-- SQL statement 199: Create table lookup_cb_2014_us_county_500k >>>
/*
 * SQL statement name: 	create_lookup_table.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: table; e.g. lookup_sahsu_grd_level1
 * 						2: Lookup column - shapefile table name, e.g. sahsu_grd_level1
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create lookup table
 * Note:				%% becomes % after substitution
 */
CREATE TABLE lookup_cb_2014_us_county_500k (
	cb_2014_us_county_500k			VARCHAR(100)  NOT NULL,
	areaname	VARCHAR(1000),
	gid			INTEGER		  NOT NULL,
	geographic_centroid		JSON,
	PRIMARY KEY (cb_2014_us_county_500k)
);
CREATE TABLE
Time: 15.716 ms
-- SQL statement 200: Comment table lookup_cb_2014_us_county_500k >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE lookup_cb_2014_us_county_500k IS 'Lookup table for The State at a scale of 1:500,000';
COMMENT
Time: 0.655 ms
-- SQL statement 201: Comment lookup_cb_2014_us_county_500k columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_county_500k.cb_2014_us_county_500k IS 'Area ID field';
COMMENT
Time: 0.378 ms
-- SQL statement 202: Comment lookup_cb_2014_us_county_500k columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_county_500k.gid IS 'GID field';
COMMENT
Time: 0.757 ms
-- SQL statement 203: Comment lookup_cb_2014_us_county_500k columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_county_500k.areaname IS 'Area Name field';
COMMENT
Time: 0.610 ms
-- SQL statement 204: Comment lookup_cb_2014_us_county_500k columns >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN lookup_cb_2014_us_county_500k.geographic_centroid IS 'Geographic centroid';
COMMENT
Time: 0.782 ms
--
-- Insert Geolevels lookup tables
--
-- SQL statement 206: Insert table lookup_cb_2014_us_nation_5m >>>
/*
 * SQL statement name: 	insert_lookup_table.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: lookup table; e.g. lookup_sahsu_grd_level1
 *						2: shapefile table; e.g. sahsu_grd_level4
 *
 * Description:			Insert data into lookup table
 *						Centroid JSON: {"type":"Point","coordinates":[-6.36447811663261,55.1846108882703]}
 * Note:				%% becomes % after substitution
 */
INSERT INTO lookup_cb_2014_us_nation_5m(cb_2014_us_nation_5m, areaname, gid, geographic_centroid)
SELECT areaid, areaname, ROW_NUMBER() OVER(ORDER BY areaid) AS gid, ST_AsGeoJSON(geographic_centroid)::JSON AS geographic_centroid
  FROM cb_2014_us_nation_5m
 ORDER BY 1;
INSERT 0 1
Time: 2.644 ms
-- SQL statement 207: Insert table lookup_cb_2014_us_state_500k >>>
/*
 * SQL statement name: 	insert_lookup_table.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: lookup table; e.g. lookup_sahsu_grd_level1
 *						2: shapefile table; e.g. sahsu_grd_level4
 *
 * Description:			Insert data into lookup table
 *						Centroid JSON: {"type":"Point","coordinates":[-6.36447811663261,55.1846108882703]}
 * Note:				%% becomes % after substitution
 */
INSERT INTO lookup_cb_2014_us_state_500k(cb_2014_us_state_500k, areaname, gid, geographic_centroid)
SELECT areaid, areaname, ROW_NUMBER() OVER(ORDER BY areaid) AS gid, ST_AsGeoJSON(geographic_centroid)::JSON AS geographic_centroid
  FROM cb_2014_us_state_500k
 ORDER BY 1;
INSERT 0 56
Time: 3.062 ms
-- SQL statement 208: Insert table lookup_cb_2014_us_county_500k >>>
/*
 * SQL statement name: 	insert_lookup_table.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: lookup table; e.g. lookup_sahsu_grd_level1
 *						2: shapefile table; e.g. sahsu_grd_level4
 *
 * Description:			Insert data into lookup table
 *						Centroid JSON: {"type":"Point","coordinates":[-6.36447811663261,55.1846108882703]}
 * Note:				%% becomes % after substitution
 */
INSERT INTO lookup_cb_2014_us_county_500k(cb_2014_us_county_500k, areaname, gid, geographic_centroid)
SELECT areaid, areaname, ROW_NUMBER() OVER(ORDER BY areaid) AS gid, ST_AsGeoJSON(geographic_centroid)::JSON AS geographic_centroid
  FROM cb_2014_us_county_500k
 ORDER BY 1;
INSERT 0 3233
Time: 71.993 ms
--
-- Hierarchy table
--
-- SQL statement 210: Drop table hierarchy_usa_2014 >>>
DROP TABLE IF EXISTS hierarchy_usa_2014;
psql:pg_USA_2014.sql:3128: NOTICE:  table "hierarchy_usa_2014" does not exist, skipping
DROP TABLE
Time: 0.601 ms
-- SQL statement 211: Create table hierarchy_usa_2014 >>>
CREATE TABLE hierarchy_usa_2014 (
	cb_2014_us_county_500k	VARCHAR(100)  NOT NULL,
	cb_2014_us_nation_5m	VARCHAR(100)  NOT NULL,
	cb_2014_us_state_500k	VARCHAR(100)  NOT NULL);
CREATE TABLE
Time: 2.796 ms
-- SQL statement 212: Add primary key hierarchy_usa_2014 >>>
ALTER TABLE hierarchy_usa_2014 ADD PRIMARY KEY (cb_2014_us_county_500k);
ALTER TABLE
Time: 6.191 ms
-- SQL statement 213: Add index key hierarchy_usa_2014_cb_2014_us_state_500k >>>
CREATE INDEX hierarchy_usa_2014_cb_2014_us_state_500k ON hierarchy_usa_2014 (cb_2014_us_state_500k);
CREATE INDEX
Time: 5.086 ms
-- SQL statement 214: Comment table: hierarchy_usa_2014 >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE hierarchy_usa_2014 IS 'Hierarchy lookup table for US 2014 Census geography to county level';
COMMENT
Time: 0.560 ms
-- SQL statement 215: Comment column: hierarchy_usa_2014.cb_2014_us_county_500k >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN hierarchy_usa_2014.cb_2014_us_county_500k IS 'Hierarchy lookup for The County at a scale of 1:500,000';
COMMENT
Time: 0.417 ms
-- SQL statement 216: Comment column: hierarchy_usa_2014.cb_2014_us_nation_5m >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN hierarchy_usa_2014.cb_2014_us_nation_5m IS 'Hierarchy lookup for The nation at a scale of 1:5,000,000';
COMMENT
Time: 0.409 ms
-- SQL statement 217: Comment column: hierarchy_usa_2014.cb_2014_us_state_500k >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN hierarchy_usa_2014.cb_2014_us_state_500k IS 'Hierarchy lookup for The State at a scale of 1:500,000';
COMMENT
Time: 0.298 ms
-- SQL statement 218: Create function check_hierarchy_usa_2014 >>>
CREATE OR REPLACE FUNCTION check_hierarchy_usa_2014(l_geography VARCHAR, l_hierarchytable VARCHAR, l_type VARCHAR)
RETURNS integer 
SECURITY INVOKER
AS $body$
/*
 * SQL statement name: 	check_hierarchy_function.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: function name; e.g. check_hierarchy_cb_2014_us_500k
 *
 * Description:			Create hierarchy check function
 * Note:				%% becomes % after substitution
 */
 
/*
Function: 		check_hierarchy_usa_2014()
Parameters:		Geography, hierarchy table, type: 'missing', 'spurious additional' or 'multiple hierarchy'
Returns:		Nothing
Description:	Diff geography hierarchy table using dynamic method 4
				Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on
 */
DECLARE
	c2 CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM geolevels_usa_2014
		 WHERE geography = l_geography
		 ORDER BY geolevel_id;
	c3 REFCURSOR;
	c4 CURSOR(l_geography VARCHAR, l_geolevel_id INTEGER) FOR
		SELECT * 
		  FROM geolevels_usa_2014
		 WHERE geography   = l_geography
		   AND geolevel_id = l_geolevel_id
		 ORDER BY geolevel_id;
--
	c2_rec geolevels_usa_2014%ROWTYPE;
	c3_rec RECORD;
	c4_rec geolevels_usa_2014%ROWTYPE;
--
	sql_stmt 		VARCHAR;
	previous_geolevel_name 	VARCHAR:=NULL;
	i INTEGER;
	e INTEGER:=0;
	field INTEGER;
BEGIN
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
			RAISE EXCEPTION 'Invalid check type: %, valid types are: ''missing'', ''spurious additional'', or ''multiple hierarchy''', 
				l_type::VARCHAR 	/* Check type */;
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
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
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
			RAISE WARNING 'Geography: % geolevel %: [%] % codes: %', 
				l_geography::VARCHAR		/* Geography */, 
				i::VARCHAR					/* Geolevel ID */, 
				LOWER(c4_rec.geolevel_name)::VARCHAR	/* Geolevel name */, 
				l_type::VARCHAR				/* Check type */, 
				field::VARCHAR				/* Area ID */;
			e:=e+1;
		ELSE
			RAISE INFO 'Geography: % geolevel %: [%] no % codes', 
				l_geography::VARCHAR		/* Geography */, 
				i::VARCHAR					/* Geolevel ID */, 
				LOWER(c4_rec.geolevel_name)::VARCHAR	/* Geolevel name */, 
				l_type::VARCHAR				/* Check type */;
		END IF;
	END LOOP;
--
	RETURN e;
END;
$body$
LANGUAGE PLPGSQL;
CREATE FUNCTION
Time: 3.452 ms
-- SQL statement 219: Comment function check_hierarchy_usa_2014 >>>
COMMENT /*
 * SQL statement name: 	check_hierarchy_function_comment.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: function name; e.g. check_hierarchy_cb_2014_us_500k
 *
 * Description:			Create hierarchy check function comment
 * Note:				%% becomes % after substitution
 */
	ON FUNCTION check_hierarchy_usa_2014(VARCHAR, VARCHAR, VARCHAR) IS 'Function: 		check_hierarchy_usa_2014()
Parameters:		Geography, hierarchy table, type: "missing", "spurious additional" or "multiple hierarchy"
Returns:		Nothing
Description:	Diff geography hierarchy table using dynamic method 4
				Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on

Example of dynamic SQL. Note the use of an array return type to achieve method 4

WITH /* missing */ a1 AS (
        SELECT COUNT(*) AS cb_2014_us_nation_5m_total
          FROM (
                SELECT cb_2014_us_nation_5m FROM hierarchy_cb_2014_us_500k
                EXCEPT
                SELECT cb_2014_us_nation_5m FROM lookup_cb_2014_us_nation_5m) as1)
, a2 AS (
        SELECT COUNT(*) AS cb_2014_us_state_500k_total
          FROM (
                SELECT cb_2014_us_state_500k FROM hierarchy_cb_2014_us_500k
                EXCEPT
                SELECT cb_2014_us_state_500k FROM lookup_cb_2014_us_state_500k) as2)
, a3 AS (
        SELECT COUNT(*) AS cb_2014_us_county_500k_total
          FROM (
                SELECT cb_2014_us_county_500k FROM hierarchy_cb_2014_us_500k
                EXCEPT
                SELECT cb_2014_us_county_500k FROM lookup_cb_2014_us_county_500k) as3)
SELECT ARRAY[a1.cb_2014_us_nation_5m_total, a2.cb_2014_us_state_500k_total, a3.cb_2014_us_county_500k_total] AS res_array
FROM a1, a2, a3;

Or: 

WITH /* multiple hierarchy */ a2 AS (
        SELECT COUNT(*) AS cb_2014_us_state_500k_total
          FROM (
                SELECT cb_2014_us_state_500k, COUNT(DISTINCT(cb_2014_us_nation_5m)) AS total
                  FROM hierarchy_cb_2014_us_500k
                 GROUP BY cb_2014_us_state_500k
                HAVING COUNT(DISTINCT(cb_2014_us_nation_5m)) > 1) as2)
, a3 AS (
        SELECT COUNT(*) AS cb_2014_us_county_500k_total
          FROM (
                SELECT cb_2014_us_county_500k, COUNT(DISTINCT(cb_2014_us_state_500k)) AS total
                  FROM hierarchy_cb_2014_us_500k
                 GROUP BY cb_2014_us_county_500k
                HAVING COUNT(DISTINCT(cb_2014_us_state_500k)) > 1) as3)
SELECT ARRAY[a2.cb_2014_us_state_500k_total, a3.cb_2014_us_county_500k_total] AS res_array
FROM a2, a3;
';
COMMENT
Time: 0.778 ms
-- SQL statement 220: Insert into hierarchy_usa_2014 >>>
DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	insert_hierarchy.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *						2: Max zoomlevel
 *
 * Description:			Create insert statement into hierarchy table
 * Note:				%% becomes % after substitution
 */
	l_geography VARCHAR:='USA_2014';
--
	c1_hier CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM geolevels_USA_2014
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id;
	c2_hier CURSOR(l_geography VARCHAR) FOR
		SELECT * FROM pg_indexes
		 WHERE schemaname = USER
		   AND tablename IN (SELECT DISTINCT LOWER(hierarchytable)
				       FROM geography_USA_2014
				      WHERE geography = l_geography)
		 ORDER BY 1;	
	c3 REFCURSOR;
	c4_hier CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM geography_USA_2014
		 WHERE geography = l_geography;
	c1_rec geolevels_USA_2014%ROWTYPE;
	c2_rec geography_USA_2014%ROWTYPE;
	c3_rec	RECORD;
	c4_rec geography_USA_2014%ROWTYPE;
--
	columns			VARCHAR;
	sql_stmt	 	VARCHAR;
	i				INTEGER:=0;
	num_geolevels	INTEGER:=0;
--
	geolevel_name			VARCHAR[];
	shapefile_table      		VARCHAR[];
 	shapefile_area_id_column	VARCHAR[];
 	shapefile_desc_column		VARCHAR[];
--
BEGIN
--
	OPEN c4_hier(l_geography);
	FETCH c4_hier INTO c4_rec;
	CLOSE c4_hier;
--
	IF c4_rec.geography IS NULL THEN
		RAISE EXCEPTION 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */;
	END IF;	
--
	 RAISE INFO 'Populating % geography hierarchy table: %',
		l_geography, c4_rec.hierarchytable;
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
		IF i = 1 THEN
			columns:=geolevel_name[i];
		ELSE
			columns:=columns||', '||geolevel_name[i];
		END IF;
	END LOOP;
	num_geolevels:=i;
	IF num_geolevels = 0 THEN
		RAISE EXCEPTION 'No rows found in: geolevels_USA_2014 for geography %', 
			l_geography::VARCHAR /* Geography */;
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
	SELECT a2.areaid AS level2, a3.areaid AS level3,
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
				'SELECT a'||i||'.areaid AS '||geolevel_name[i]||', a'||i+1||'.areaid AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.geom_9) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.geom_9, a'||i+1||'.geom_9)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||shapefile_table[i]||' a'||i||' CROSS JOIN '||shapefile_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.geom_9, a'||i+1||'.geom_9)'||E'\n'||
				'), ';
		ELSIF i < (num_geolevels-1) THEN
			sql_stmt:=sql_stmt||
				'x'||i||i+1||' AS ( /* Subqueries x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||': intersection aggregate geometries starting from the lowest resolution.'||E'\n'||
         		      	E'\t'||'       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.'||E'\n'||
 	       			E'\t'||'       Calculate the area of the higher resolution geolevel and the area of the intersected area */'||E'\n'||
				'SELECT a'||i||'.areaid AS '||geolevel_name[i]||', a'||i+1||'.areaid AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.geom_9) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.geom_9, a'||i+1||'.geom_9)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||shapefile_table[i]||' a'||i||' CROSS JOIN '||shapefile_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.geom_9, a'||i+1||'.geom_9)'||E'\n'||
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
				'SELECT a'||i||'.areaid AS '||geolevel_name[i]||', a'||i+1||'.areaid AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.geom_9) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.geom_9, a'||i+1||'.geom_9)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||shapefile_table[i]||' a'||i||' CROSS JOIN '||shapefile_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.geom_9, a'||i+1||'.geom_9)'||E'\n'||
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
	
	sql_stmt:=sql_stmt||' ORDER BY 1';
	FOR i IN 2 .. num_geolevels LOOP /* ORDER BY clause */ 	
		sql_stmt:=sql_stmt||', '||i;
	END LOOP;
	
	RAISE NOTICE 'SQL> %;', sql_stmt;
	EXECUTE sql_stmt;
--
-- Check rows were inserted
--
	sql_stmt:='SELECT COUNT(*) AS total FROM '||quote_ident(LOWER(c4_rec.hierarchytable));
	OPEN c3 FOR EXECUTE sql_stmt;
	FETCH c3 INTO c3_rec;
	CLOSE c3;	
	IF c3_rec.total = 0 THEN
		RAISE EXCEPTION 'No rows found in % geography hierarchy table: %', 
			l_geography::VARCHAR 			/* Geography */,
			quote_ident(LOWER(c4_rec.hierarchytable))	/* Hierarchy table */;
	END IF;
--
-- Re-index
--
	FOR c2_rec IN c2_hier(l_geography) LOOP
		sql_stmt:='REINDEX INDEX /* '||quote_ident(c2_rec.tablename)||' */ '||quote_ident(c2_rec.indexname);
		RAISE NOTICE 'SQL> %;', sql_stmt;
		EXECUTE sql_stmt;
	END LOOP;
--
-- Analyze
--
	sql_stmt:='ANALYZE VERBOSE '||quote_ident(LOWER(c4_rec.hierarchytable));
	RAISE NOTICE 'SQL> %;', sql_stmt;
	EXECUTE sql_stmt;
END;
$$;
psql:pg_USA_2014.sql:3701: INFO:  Populating USA_2014 geography hierarchy table: HIERARCHY_USA_2014
psql:pg_USA_2014.sql:3701: NOTICE:  SQL> INSERT INTO hierarchy_usa_2014 (cb_2014_us_nation_5m, cb_2014_us_state_500k, cb_2014_us_county_500k)
WITH x12 AS ( /* Subqueries x12 ... x23: intersection aggregate geometries starting from the lowest resolution.
	       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
	       Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a1.areaid AS cb_2014_us_nation_5m, a2.areaid AS cb_2014_us_state_500k,
       ST_Area(a2.geom_9) AS a2_area,
       ST_Area(ST_Intersection(a1.geom_9, a2.geom_9)) AS a12_area
  FROM cb_2014_us_nation_5m a1 CROSS JOIN cb_2014_us_state_500k a2
 WHERE ST_Intersects(a1.geom_9, a2.geom_9)
), x23 AS ( /* Subqueries x23 ... x23: intersection aggregate geometries starting from the lowest resolution.
	       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
	       Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a2.areaid AS cb_2014_us_state_500k, a3.areaid AS cb_2014_us_county_500k,
       ST_Area(a3.geom_9) AS a3_area,
       ST_Area(ST_Intersection(a2.geom_9, a3.geom_9)) AS a23_area
  FROM cb_2014_us_state_500k a2 CROSS JOIN cb_2014_us_county_500k a3
 WHERE ST_Intersects(a2.geom_9, a3.geom_9)
), y AS ( /* Join x34 ... x23intersections, pass through the computed areas, compute intersected area/higher resolution geolevel area,
	     compute maximum intersected area/higher resolution geolevel area using an analytic partition of all
	     duplicate higher resolution geolevels */
SELECT x12.cb_2014_us_nation_5m, 
       x12.cb_2014_us_state_500k, 
       x23.cb_2014_us_county_500k, 
       CASE WHEN x12.a2_area > 0 THEN x12.a12_area/x12.a2_area ELSE NULL END test12,
       MAX(x12.a12_area/x12.a2_area) OVER (PARTITION BY x12.cb_2014_us_state_500k) AS max12,
       CASE WHEN x23.a3_area > 0 THEN x23.a23_area/x23.a3_area ELSE NULL END test23,
       MAX(x23.a23_area/x23.a3_area) OVER (PARTITION BY x23.cb_2014_us_county_500k) AS max23
  FROM x12, x23
 WHERE x12.cb_2014_us_state_500k = x23.cb_2014_us_state_500k
)
SELECT /* Select y intersection, eliminating duplicates using selecting the lower geolevel resolution
	 with the largest intersection by area for each (higher resolution) geolevel */
       cb_2014_us_nation_5m, cb_2014_us_state_500k, cb_2014_us_county_500k
  FROM y
 WHERE max12 = test12
   AND max23 = test23
 ORDER BY 1, 2, 3;
psql:pg_USA_2014.sql:3701: NOTICE:  SQL> REINDEX INDEX /* hierarchy_usa_2014 */ hierarchy_usa_2014_cb_2014_us_state_500k;
psql:pg_USA_2014.sql:3701: NOTICE:  SQL> REINDEX INDEX /* hierarchy_usa_2014 */ hierarchy_usa_2014_pkey;
psql:pg_USA_2014.sql:3701: NOTICE:  SQL> ANALYZE VERBOSE hierarchy_usa_2014;
psql:pg_USA_2014.sql:3701: INFO:  analyzing "peter.hierarchy_usa_2014"
CONTEXT:  SQL statement "ANALYZE VERBOSE hierarchy_usa_2014"
PL/pgSQL function inline_code_block line 267 at EXECUTE
psql:pg_USA_2014.sql:3701: INFO:  "hierarchy_usa_2014": scanned 21 of 21 pages, containing 3233 live rows and 0 dead rows; 3233 rows in sample, 3233 estimated total rows
CONTEXT:  SQL statement "ANALYZE VERBOSE hierarchy_usa_2014"
PL/pgSQL function inline_code_block line 267 at EXECUTE
DO
Time: 129779.208 ms
-- SQL statement 221: Check intersctions  for geograpy: usa_2014 >>>
DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	check_intersections.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *
 * Description:			Check intersections
 * Note:				%% becomes % after substitution
 */
	l_geography VARCHAR:='USA_2014';
--
	c1 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM geography_USA_2014
		 WHERE geography = l_geography;
	c1_rec geography_USA_2014%ROWTYPE;
--
	e INTEGER:=0;
	f INTEGER:=0;
	g INTEGER:=0;
BEGIN
--
	OPEN c1(l_geography);
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.geography IS NULL THEN
		RAISE EXCEPTION 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */;
	END IF;	
--
-- Call diff and multiple hierarchy tests
--
	e:=check_hierarchy_USA_2014(c1_rec.geography, c1_rec.hierarchytable, 'missing');
	f:=check_hierarchy_USA_2014(c1_rec.geography, c1_rec.hierarchytable, 'spurious additional');
	g:=check_hierarchy_USA_2014(c1_rec.geography, c1_rec.hierarchytable, 'multiple hierarchy');
--
	IF e+f > 0 THEN
		RAISE EXCEPTION 'Geography: % codes check % missing, % spurious additional, % hierarchy fails', 
			c1_rec.geography	/* Geography */, 
			e::VARCHAR		/* Missing */, 
			f::VARCHAR		/* Spurious additional */, 
			g::VARCHAR		/* Multiple hierarchy */;
	ELSE
		RAISE INFO 'Geography: % codes check OK', 
			c1_rec.geography::VARCHAR	/* Geography */;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:3754: INFO:  SQL> WITH /* missing */ a1 AS (
	SELECT COUNT(*) AS cb_2014_us_nation_5m_total
	  FROM (
		SELECT cb_2014_us_nation_5m FROM hierarchy_usa_2014
		EXCEPT
		SELECT cb_2014_us_nation_5m FROM lookup_cb_2014_us_nation_5m) as1)
, a2 AS (
	SELECT COUNT(*) AS cb_2014_us_state_500k_total
	  FROM (
		SELECT cb_2014_us_state_500k FROM hierarchy_usa_2014
		EXCEPT
		SELECT cb_2014_us_state_500k FROM lookup_cb_2014_us_state_500k) as2)
, a3 AS (
	SELECT COUNT(*) AS cb_2014_us_county_500k_total
	  FROM (
		SELECT cb_2014_us_county_500k FROM hierarchy_usa_2014
		EXCEPT
		SELECT cb_2014_us_county_500k FROM lookup_cb_2014_us_county_500k) as3)
SELECT ARRAY[a1.cb_2014_us_nation_5m_total, a2.cb_2014_us_state_500k_total, a3.cb_2014_us_county_500k_total] AS res_array
FROM a1, a2, a3;
CONTEXT:  PL/pgSQL function inline_code_block line 36 at assignment
psql:pg_USA_2014.sql:3754: INFO:  Geography: USA_2014 geolevel 1: [cb_2014_us_nation_5m] no missing codes
CONTEXT:  PL/pgSQL function inline_code_block line 36 at assignment
psql:pg_USA_2014.sql:3754: INFO:  Geography: USA_2014 geolevel 2: [cb_2014_us_state_500k] no missing codes
CONTEXT:  PL/pgSQL function inline_code_block line 36 at assignment
psql:pg_USA_2014.sql:3754: INFO:  Geography: USA_2014 geolevel 3: [cb_2014_us_county_500k] no missing codes
CONTEXT:  PL/pgSQL function inline_code_block line 36 at assignment
psql:pg_USA_2014.sql:3754: INFO:  SQL> WITH /* spurious additional */ a1 AS (
	SELECT COUNT(*) AS cb_2014_us_nation_5m_total
	  FROM (
		SELECT cb_2014_us_nation_5m FROM lookup_cb_2014_us_nation_5m
		EXCEPT
		SELECT cb_2014_us_nation_5m FROM hierarchy_usa_2014) as1)
, a2 AS (
	SELECT COUNT(*) AS cb_2014_us_state_500k_total
	  FROM (
		SELECT cb_2014_us_state_500k FROM lookup_cb_2014_us_state_500k
		EXCEPT
		SELECT cb_2014_us_state_500k FROM hierarchy_usa_2014) as2)
, a3 AS (
	SELECT COUNT(*) AS cb_2014_us_county_500k_total
	  FROM (
		SELECT cb_2014_us_county_500k FROM lookup_cb_2014_us_county_500k
		EXCEPT
		SELECT cb_2014_us_county_500k FROM hierarchy_usa_2014) as3)
SELECT ARRAY[a1.cb_2014_us_nation_5m_total, a2.cb_2014_us_state_500k_total, a3.cb_2014_us_county_500k_total] AS res_array
FROM a1, a2, a3;
CONTEXT:  PL/pgSQL function inline_code_block line 37 at assignment
psql:pg_USA_2014.sql:3754: INFO:  Geography: USA_2014 geolevel 1: [cb_2014_us_nation_5m] no spurious additional codes
CONTEXT:  PL/pgSQL function inline_code_block line 37 at assignment
psql:pg_USA_2014.sql:3754: INFO:  Geography: USA_2014 geolevel 2: [cb_2014_us_state_500k] no spurious additional codes
CONTEXT:  PL/pgSQL function inline_code_block line 37 at assignment
psql:pg_USA_2014.sql:3754: INFO:  Geography: USA_2014 geolevel 3: [cb_2014_us_county_500k] no spurious additional codes
CONTEXT:  PL/pgSQL function inline_code_block line 37 at assignment
psql:pg_USA_2014.sql:3754: INFO:  SQL> WITH /* multiple hierarchy */ a2 AS (
	SELECT COUNT(*) AS cb_2014_us_state_500k_total
	  FROM (
		SELECT cb_2014_us_state_500k, COUNT(DISTINCT(cb_2014_us_nation_5m)) AS total
		  FROM hierarchy_usa_2014
		 GROUP BY cb_2014_us_state_500k
		HAVING COUNT(DISTINCT(cb_2014_us_nation_5m)) > 1) as2)
, a3 AS (
	SELECT COUNT(*) AS cb_2014_us_county_500k_total
	  FROM (
		SELECT cb_2014_us_county_500k, COUNT(DISTINCT(cb_2014_us_state_500k)) AS total
		  FROM hierarchy_usa_2014
		 GROUP BY cb_2014_us_county_500k
		HAVING COUNT(DISTINCT(cb_2014_us_state_500k)) > 1) as3)
SELECT ARRAY[a2.cb_2014_us_state_500k_total, a3.cb_2014_us_county_500k_total] AS res_array
FROM a2, a3;
CONTEXT:  PL/pgSQL function inline_code_block line 38 at assignment
psql:pg_USA_2014.sql:3754: INFO:  Geography: USA_2014 geolevel 1: [cb_2014_us_nation_5m] no multiple hierarchy codes
CONTEXT:  PL/pgSQL function inline_code_block line 38 at assignment
psql:pg_USA_2014.sql:3754: INFO:  Geography: USA_2014 geolevel 2: [cb_2014_us_state_500k] no multiple hierarchy codes
CONTEXT:  PL/pgSQL function inline_code_block line 38 at assignment
psql:pg_USA_2014.sql:3754: INFO:  Geography: USA_2014 codes check OK
DO
Time: 30.921 ms
--
-- Create geometry table
--
-- SQL statement 223: Drop geometry table geometry_usa_2014 >>>
DROP TABLE IF EXISTS geometry_usa_2014 CASCADE;
psql:pg_USA_2014.sql:3761: NOTICE:  table "geometry_usa_2014" does not exist, skipping
DROP TABLE
Time: 0.644 ms
-- SQL statement 224: Create geometry table geometry_usa_2014 >>>
/*
 * SQL statement name: 	create_geometry_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. geometry_cb_2014_us_500k
 *						2: schema; e.g.rif_data. or ""
 *
 * Description:			Create geometry table
 * Note:				% becomes % after substitution
 */
CREATE TABLE geometry_usa_2014 (
	geolevel_id		INTEGER			NOT NULL,
	areaid			VARCHAR(200)	NOT NULL,
	zoomlevel		INTEGER			NOT NULL);
CREATE TABLE
Time: 2.353 ms
-- SQL statement 225: Add geom geometry column >>>
/*
 * SQL statement name: 	add_geometry_column2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *                      5: Schema (rif_data. or "") [NEVER USED IN POSTGRES]
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('geometry_usa_2014','geom', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                        addgeometrycolumn                         
------------------------------------------------------------------
 peter.geometry_usa_2014.geom SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 9.996 ms
-- SQL statement 226: Comment geometry table >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE geometry_usa_2014 IS 'All geolevels geometry combined into a single table for a single geography';
COMMENT
Time: 0.483 ms
-- SQL statement 227: Comment geometry table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geometry_usa_2014.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT
Time: 0.424 ms
-- SQL statement 228: Comment geometry table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geometry_usa_2014.zoomlevel IS 'Zoom level: 0 to maxoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11';
COMMENT
Time: 0.453 ms
-- SQL statement 229: Comment geometry table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geometry_usa_2014.areaid IS 'Area ID.';
COMMENT
Time: 0.628 ms
-- SQL statement 230: Comment geometry table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN geometry_usa_2014.geom IS 'Geometry data in SRID 4326 (WGS84).';
COMMENT
Time: 0.423 ms
-- SQL statement 231: Create partitioned tables and insert function for geometry table; comment partitioned tables and columns >>>
DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	partition_geometry_table1.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *						2: Max zoomlevel; e.g. 11
 * 						3: Number of geolevels (e.g. 3)
 *
 * Description:			Create partitioned tables and insert function for geometry table; comment partitioned tables and columns
 * Note:				%% becomes % after substitution
 */
	l_table 	Text:='geometry_usa_2014';
	sql_stmt	VARCHAR[];
	trigger_sql	VARCHAR;
BEGIN
	FOR i IN 1 .. 3 LOOP
		FOR j IN 6 .. 9 LOOP
			sql_stmt[COALESCE(array_length(sql_stmt, 1), 0)]:='CREATE TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' ('||E'\n'||
					  '    CHECK ( geolevel_id = '||i::Text||' AND zoomlevel = '||j::Text||' )'||E'\n'||
					  ') INHERITS ('||l_table||')';	
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
					  ' IS ''All geolevels geometry combined into a single table.  Geolevel '||
							i::Text||', zoomlevel '||j::Text||' partition.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
							'.zoomlevel IS ''Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
							'.areaid IS ''Area ID.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
							'.geolevel_id IS ''ID for ordering (1=lowest resolution). Up to 99 supported.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
							'.geom IS ''Geometry data in SRID 4326 (WGS84).''';
			IF trigger_sql IS NULL THEN
				trigger_sql:='IF ( NEW.zoomlevel = '||j::Text||' AND NEW.geolevel_id = '||i::Text||' ) THEN'||E'\n'||
							' 	INSERT INTO '||l_table||'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' VALUES (NEW.*);'||E'\n';
			ELSE
				trigger_sql:=trigger_sql||
							'ELSIF ( NEW.zoomlevel = '||j::Text||' AND NEW.geolevel_id = '||i::Text||' ) THEN'||E'\n'||
							' 	INSERT INTO '||l_table||'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' VALUES (NEW.*);'||E'\n';
			END IF;
		END LOOP;
	END LOOP;
	
	sql_stmt[array_length(sql_stmt, 1)]:='CREATE OR REPLACE FUNCTION '||l_table||'_insert_trigger()'||E'\n'||
		'RETURNS TRIGGER AS $trigger$'||E'\n'||
		'BEGIN'||E'\n'||
		trigger_sql||
		'    ELSE'||E'\n'||
		'        RAISE EXCEPTION ''Zoomlevel (%) or geolevel_id(%) out of range. '||
					'Fix the '||l_table||'_insert_trigger() function!'','||E'\n'||
		'			NEW.zoomlevel, NEW.geolevel_id;'||E'\n'||
		'    END IF;'||E'\n'||
		'    RETURN NULL;'||E'\n'||
		'END;'||E'\n'||
		'$trigger$'||E'\n'||
		'LANGUAGE plpgsql';
--
	FOR i IN 0 .. (array_length(sql_stmt, 1)-1) LOOP
		RAISE INFO 'SQL> %;', sql_stmt[i];
		EXECUTE sql_stmt[i];
	END LOOP;
END;
$$;
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_6 (
    CHECK ( geolevel_id = 1 AND zoomlevel = 6 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_6 IS 'All geolevels geometry combined into a single table.  Geolevel 1, zoomlevel 6 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_6.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_6.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_6.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_6.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_7 (
    CHECK ( geolevel_id = 1 AND zoomlevel = 7 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_7 IS 'All geolevels geometry combined into a single table.  Geolevel 1, zoomlevel 7 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_7.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_7.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_7.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_7.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_8 (
    CHECK ( geolevel_id = 1 AND zoomlevel = 8 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_8 IS 'All geolevels geometry combined into a single table.  Geolevel 1, zoomlevel 8 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_8.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_8.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_8.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_8.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_9 (
    CHECK ( geolevel_id = 1 AND zoomlevel = 9 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_9 IS 'All geolevels geometry combined into a single table.  Geolevel 1, zoomlevel 9 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_9.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_9.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_9.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_9.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_6 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 6 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_6 IS 'All geolevels geometry combined into a single table.  Geolevel 2, zoomlevel 6 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_6.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_6.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_6.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_6.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_7 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 7 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_7 IS 'All geolevels geometry combined into a single table.  Geolevel 2, zoomlevel 7 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_7.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_7.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_7.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_7.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_8 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 8 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_8 IS 'All geolevels geometry combined into a single table.  Geolevel 2, zoomlevel 8 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_8.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_8.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_8.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_8.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_9 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 9 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_9 IS 'All geolevels geometry combined into a single table.  Geolevel 2, zoomlevel 9 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_9.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_9.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_9.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_9.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_6 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 6 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_6 IS 'All geolevels geometry combined into a single table.  Geolevel 3, zoomlevel 6 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_6.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_6.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_6.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_6.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_7 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 7 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_7 IS 'All geolevels geometry combined into a single table.  Geolevel 3, zoomlevel 7 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_7.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_7.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_7.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_7.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_8 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 8 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_8 IS 'All geolevels geometry combined into a single table.  Geolevel 3, zoomlevel 8 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_8.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_8.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_8.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_8.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_9 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 9 )
) INHERITS (geometry_usa_2014);
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_9 IS 'All geolevels geometry combined into a single table.  Geolevel 3, zoomlevel 9 partition.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_9.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_9.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_9.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:3941: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_9.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:pg_USA_2014.sql:3941: INFO:  SQL> CREATE OR REPLACE FUNCTION geometry_usa_2014_insert_trigger()
RETURNS TRIGGER AS $trigger$
BEGIN
IF ( NEW.zoomlevel = 6 AND NEW.geolevel_id = 1 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_1_zoomlevel_6 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 7 AND NEW.geolevel_id = 1 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_1_zoomlevel_7 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 8 AND NEW.geolevel_id = 1 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_1_zoomlevel_8 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 9 AND NEW.geolevel_id = 1 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_1_zoomlevel_9 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 6 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_2_zoomlevel_6 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 7 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_2_zoomlevel_7 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 8 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_2_zoomlevel_8 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 9 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_2_zoomlevel_9 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 6 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_3_zoomlevel_6 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 7 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_3_zoomlevel_7 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 8 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_3_zoomlevel_8 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 9 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO geometry_usa_2014_geolevel_id_3_zoomlevel_9 VALUES (NEW.*);
    ELSE
        RAISE EXCEPTION 'Zoomlevel (%) or geolevel_id(%) out of range. Fix the geometry_usa_2014_insert_trigger() function!',
			NEW.zoomlevel, NEW.geolevel_id;
    END IF;
    RETURN NULL;
END;
$trigger$
LANGUAGE plpgsql;
DO
Time: 139.303 ms
-- SQL statement 232: Partition geometry table: insert trigger >>>
/*
 * SQL statement name: 	partition_trigger.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Create partitioned tables insert trigger
 * Note:				%% becomes % after substitution
 */
 CREATE TRIGGER insert_geometry_usa_2014_trigger
    BEFORE INSERT ON geometry_usa_2014
    FOR EACH ROW EXECUTE PROCEDURE geometry_usa_2014_insert_trigger();
CREATE TRIGGER
Time: 1.362 ms
-- SQL statement 233: Comment partition geometry table: insert trigger >>>
/*
 * SQL statement name: 	comment_partition_trigger.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Comment create partitioned tables insert trigger
 * Note:				%% becomes % after substitution
 */
 COMMENT ON TRIGGER insert_geometry_usa_2014_trigger ON geometry_usa_2014 IS 'Partitioned tables insert trigger';
COMMENT
Time: 0.461 ms
--
-- Insert geometry table
--
-- SQL statement 235: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 3 geolevel_id,
       areaid,
        6 AS zoomlevel,
       geom_6 AS geom
  FROM cb_2014_us_county_500k
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 302.729 ms
-- SQL statement 236: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 3 geolevel_id,
       areaid,
        7 AS zoomlevel,
       geom_7 AS geom
  FROM cb_2014_us_county_500k
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 364.619 ms
-- SQL statement 237: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 3 geolevel_id,
       areaid,
        8 AS zoomlevel,
       geom_8 AS geom
  FROM cb_2014_us_county_500k
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 695.255 ms
-- SQL statement 238: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 3 geolevel_id,
       areaid,
        9 AS zoomlevel,
       geom_9 AS geom
  FROM cb_2014_us_county_500k
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 533.395 ms
-- SQL statement 239: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 1 geolevel_id,
       areaid,
        6 AS zoomlevel,
       geom_6 AS geom
  FROM cb_2014_us_nation_5m
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 8.475 ms
-- SQL statement 240: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 1 geolevel_id,
       areaid,
        7 AS zoomlevel,
       geom_7 AS geom
  FROM cb_2014_us_nation_5m
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 10.168 ms
-- SQL statement 241: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 1 geolevel_id,
       areaid,
        8 AS zoomlevel,
       geom_8 AS geom
  FROM cb_2014_us_nation_5m
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 19.519 ms
-- SQL statement 242: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 1 geolevel_id,
       areaid,
        9 AS zoomlevel,
       geom_9 AS geom
  FROM cb_2014_us_nation_5m
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 27.724 ms
-- SQL statement 243: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 2 geolevel_id,
       areaid,
        6 AS zoomlevel,
       geom_6 AS geom
  FROM cb_2014_us_state_500k
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 87.770 ms
-- SQL statement 244: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 2 geolevel_id,
       areaid,
        7 AS zoomlevel,
       geom_7 AS geom
  FROM cb_2014_us_state_500k
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 173.940 ms
-- SQL statement 245: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 2 geolevel_id,
       areaid,
        8 AS zoomlevel,
       geom_8 AS geom
  FROM cb_2014_us_state_500k
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 199.875 ms
-- SQL statement 246: Insert into geometry table >>>
INSERT INTO geometry_usa_2014(geolevel_id, areaid, zoomlevel, geom)
SELECT 2 geolevel_id,
       areaid,
        9 AS zoomlevel,
       geom_9 AS geom
  FROM cb_2014_us_state_500k
ORDER BY 1, 3, 2;
INSERT 0 0
Time: 259.223 ms
-- SQL statement 247: Add primary key, index and cluster (convert to index organized table) >>>
DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	partition_geometry_table2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *						2: Max zoomlevel; e.g. 11
 * 						3: Number of geolevels (e.g. 3)
 *
 * Description:			Add primary key, index and cluster (convert to index organized table)
 * Note:				%% becomes % after substitution
 */
	l_table 	Text:='geometry_usa_2014';
	sql_stmt	VARCHAR[];
BEGIN
	FOR i IN 1 .. 3 LOOP
		FOR j IN 6 .. 9 LOOP
			sql_stmt[COALESCE(array_length(sql_stmt, 1), 0)]:='ALTER TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||E'\n'||
					  ' ADD CONSTRAINT '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_pk PRIMARY KEY (areaid)';	
			sql_stmt[array_length(sql_stmt, 1)]:='CREATE INDEX '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_geom_gix'||E'\n'||
					  ' ON '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' USING GIST (geom);';
-- Convert to IOT
			sql_stmt[array_length(sql_stmt, 1)]:='CLUSTER VERBOSE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||E'\n'||
					  ' USING '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_pk';
-- Analyze
			sql_stmt[array_length(sql_stmt, 1)]:='ANALYZE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text;
		END LOOP;
	END LOOP;
--
	FOR i IN 0 .. (array_length(sql_stmt, 1)-1) LOOP
		RAISE INFO 'SQL> %;', sql_stmt[i];
		EXECUTE sql_stmt[i];
	END LOOP;
END;
$$;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_6
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_1_zoomlevel_6_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_1_zoomlevel_6_geom_gix
 ON geometry_usa_2014_geolevel_id_1_zoomlevel_6 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_6_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_1_zoomlevel_6" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_6_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_1_zoomlevel_6": found 0 removable, 1 nonremovable row versions in 1 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.00s/0.00u sec elapsed 0.01 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_6_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_1_zoomlevel_6;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_7
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_1_zoomlevel_7_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_1_zoomlevel_7_geom_gix
 ON geometry_usa_2014_geolevel_id_1_zoomlevel_7 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_7_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_1_zoomlevel_7" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_7_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_1_zoomlevel_7": found 0 removable, 1 nonremovable row versions in 1 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.00s/0.00u sec elapsed 0.02 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_7_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_1_zoomlevel_7;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_8
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_1_zoomlevel_8_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_1_zoomlevel_8_geom_gix
 ON geometry_usa_2014_geolevel_id_1_zoomlevel_8 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_8_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_1_zoomlevel_8" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_8_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_1_zoomlevel_8": found 0 removable, 1 nonremovable row versions in 1 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.04s/0.00u sec elapsed 0.07 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_8_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_1_zoomlevel_8;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_9
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_1_zoomlevel_9_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_1_zoomlevel_9_geom_gix
 ON geometry_usa_2014_geolevel_id_1_zoomlevel_9 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_9_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_1_zoomlevel_9" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_9_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_1_zoomlevel_9": found 0 removable, 1 nonremovable row versions in 1 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.01s/0.00u sec elapsed 0.02 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_9_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_1_zoomlevel_9;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_6
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_2_zoomlevel_6_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_2_zoomlevel_6_geom_gix
 ON geometry_usa_2014_geolevel_id_2_zoomlevel_6 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_6_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_2_zoomlevel_6" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_6_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_2_zoomlevel_6": found 0 removable, 56 nonremovable row versions in 9 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.00s/0.06u sec elapsed 0.09 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_6_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_2_zoomlevel_6;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_7
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_2_zoomlevel_7_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_2_zoomlevel_7_geom_gix
 ON geometry_usa_2014_geolevel_id_2_zoomlevel_7 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_7_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_2_zoomlevel_7" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_7_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_2_zoomlevel_7": found 0 removable, 56 nonremovable row versions in 7 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.00s/0.04u sec elapsed 0.13 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_7_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_2_zoomlevel_7;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_8
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_2_zoomlevel_8_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_2_zoomlevel_8_geom_gix
 ON geometry_usa_2014_geolevel_id_2_zoomlevel_8 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_8_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_2_zoomlevel_8" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_8_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_2_zoomlevel_8": found 0 removable, 56 nonremovable row versions in 6 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.01s/0.07u sec elapsed 0.19 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_8_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_2_zoomlevel_8;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_9
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_2_zoomlevel_9_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_2_zoomlevel_9_geom_gix
 ON geometry_usa_2014_geolevel_id_2_zoomlevel_9 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_9_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_2_zoomlevel_9" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_9_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_2_zoomlevel_9": found 0 removable, 56 nonremovable row versions in 5 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.03s/0.09u sec elapsed 0.23 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_9_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_2_zoomlevel_9;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_6
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_3_zoomlevel_6_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_3_zoomlevel_6_geom_gix
 ON geometry_usa_2014_geolevel_id_3_zoomlevel_6 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_6_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_3_zoomlevel_6" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_6_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_3_zoomlevel_6": found 0 removable, 3233 nonremovable row versions in 768 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.04s/0.10u sec elapsed 0.16 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_6_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_3_zoomlevel_6;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_7
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_3_zoomlevel_7_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_3_zoomlevel_7_geom_gix
 ON geometry_usa_2014_geolevel_id_3_zoomlevel_7 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_7_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_3_zoomlevel_7" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_7_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_3_zoomlevel_7": found 0 removable, 3233 nonremovable row versions in 961 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.03s/0.17u sec elapsed 0.29 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_7_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_3_zoomlevel_7;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_8
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_3_zoomlevel_8_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_3_zoomlevel_8_geom_gix
 ON geometry_usa_2014_geolevel_id_3_zoomlevel_8 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_8_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_3_zoomlevel_8" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_8_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_3_zoomlevel_8": found 0 removable, 3233 nonremovable row versions in 1093 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.04s/0.18u sec elapsed 0.44 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_8_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_3_zoomlevel_8;
psql:pg_USA_2014.sql:4124: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_9
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_3_zoomlevel_9_pk PRIMARY KEY (areaid);
psql:pg_USA_2014.sql:4124: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_3_zoomlevel_9_geom_gix
 ON geometry_usa_2014_geolevel_id_3_zoomlevel_9 USING GIST (geom);;
psql:pg_USA_2014.sql:4124: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_9_pk;
psql:pg_USA_2014.sql:4124: INFO:  clustering "peter.geometry_usa_2014_geolevel_id_3_zoomlevel_9" using sequential scan and sort
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_9_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  "geometry_usa_2014_geolevel_id_3_zoomlevel_9": found 0 removable, 3233 nonremovable row versions in 1180 pages
DETAIL:  0 dead row versions cannot be removed yet.
CPU 0.06s/0.25u sec elapsed 0.42 sec.
CONTEXT:  SQL statement "CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_9_pk"
PL/pgSQL function inline_code_block line 40 at EXECUTE
psql:pg_USA_2014.sql:4124: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_3_zoomlevel_9;
DO
Time: 3195.418 ms
-- SQL statement 248: Update areaid_count column in geolevels table using geometry table >>>
/*
 * SQL statement name: 	geolevels_areaid_update.sql
 * Type:				Postgres SQL statement
 * Parameters:
 *						1: Geolevels table; e.g. geolevels_cb_2014_us_500k
 *						2: Geometry table geometry_cb_2014_us_500k
 *
 * Description:			Update areaid_count column in geolevels table using geometry table
 * Note:				% becomes % after substitution
 */
UPDATE geolevels_usa_2014 a
   SET areaid_count = (
			SELECT COUNT(DISTINCT(areaid)) AS areaid_count
			  FROM geometry_usa_2014 b
			 WHERE a.geolevel_id = b.geolevel_id);
UPDATE 3
Time: 67.528 ms
--
-- Adjacency table
--
-- SQL statement 250: Drop table adjacency_usa_2014 >>>
DROP TABLE IF EXISTS adjacency_usa_2014;
psql:pg_USA_2014.sql:4148: NOTICE:  table "adjacency_usa_2014" does not exist, skipping
DROP TABLE
Time: 0.418 ms
-- SQL statement 251: Create table adjacency_usa_2014 >>>
/*
 * SQL statement name: 	create_adjacency_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: adjacency table; e.g. adjacency_cb_2014_us_500k
 *						2: schema; e.g.rif_data. or ""
 *
 * Description:			Create adjacency table
 * Note:				% becomes % after substitution
 */
CREATE TABLE adjacency_usa_2014 (
	geolevel_id		INTEGER			NOT NULL,
	areaid			VARCHAR(200)	NOT NULL,
	num_adjacencies INTEGER			NOT NULL,
	adjacency_list	VARCHAR(8000)	NOT NULL,
	CONSTRAINT adjacency_usa_2014_pk PRIMARY KEY (geolevel_id, areaid)
);
CREATE TABLE
Time: 13.908 ms
-- SQL statement 252: Comment table: adjacency_usa_2014 >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE adjacency_usa_2014 IS 'Adjacency lookup table for US 2014 Census geography to county level';
COMMENT
Time: 0.462 ms
-- SQL statement 253: Comment column: adjacency_usa_2014.geolevel_id >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN adjacency_usa_2014.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT
Time: 0.336 ms
-- SQL statement 254: Comment column: adjacency_usa_2014.areaid >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN adjacency_usa_2014.areaid IS 'Area Id';
COMMENT
Time: 0.412 ms
-- SQL statement 255: Comment column: adjacency_usa_2014.num_adjacencies >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN adjacency_usa_2014.num_adjacencies IS 'Number of adjacencies';
COMMENT
Time: 0.420 ms
-- SQL statement 256: Comment column: adjacency_usa_2014.adjacency_list >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN adjacency_usa_2014.adjacency_list IS 'Adjacent area Ids';
COMMENT
Time: 0.366 ms
-- SQL statement 257: Insert into adjacency_usa_2014 >>>
/*
 * SQL statement name: 	insert_adjacency.sql
 * Type:				Postgres/PostGIS SQL
 * Parameters:
 *						1: adjacency table; e.g. adjacency_cb_2014_us_500k
 *						2: geometry table; e.g. geometry_cb_2014_us_500k
 *						3: Max zoomlevel
 *
 * Description:			Create insert statement into adjacency table
 * Note:				%% becomes % after substitution
 */
INSERT INTO adjacency_usa_2014(geolevel_id, areaid, num_adjacencies, adjacency_list)
WITH a AS (
	SELECT a.geolevel_id, a.areaid, b.areaid AS adjacent_areaid
	  FROM geometry_usa_2014 a, geometry_usa_2014 b
	 WHERE a.zoomlevel   = 9
	   AND b.zoomlevel   = 9
	   AND a.geolevel_id = b.geolevel_id
	   AND a.areaid      != b.areaid
	   AND a.geom        && b.geom 			/* Bounding box intersect first for efficiency */
	   AND ST_Intersects(a.geom, b.geom)
)
SELECT geolevel_id, areaid,
	   COUNT(adjacent_areaid) AS num_adjacencies, 
	   STRING_AGG(adjacent_areaid, ',' ORDER BY adjacent_areaid)::VARCHAR AS adjacency_list
  FROM a
 GROUP BY geolevel_id, areaid
 ORDER BY 1, 2;
INSERT 0 3262
Time: 13698.059 ms
--
-- Create tiles functions
--
-- SQL statement 259: Create function: longitude2tile.sql >>>
/*
 * SQL statement name: 	longitude2tile.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert longitude (WGS84 - 4326) to OSM tile x
 * Note:				% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_longitude2tile(DOUBLE PRECISION, INTEGER);
psql:pg_USA_2014.sql:4286: NOTICE:  function tilemaker_longitude2tile(pg_catalog.float8,pg_catalog.int4) does not exist, skipping
DROP FUNCTION
Time: 0.859 ms

CREATE OR REPLACE FUNCTION tileMaker_longitude2tile(longitude DOUBLE PRECISION, zoom_level INTEGER)
RETURNS INTEGER AS
$$
    SELECT FLOOR( (longitude + 180) / 360 * (1 << zoom_level) )::INTEGER
$$
LANGUAGE sql IMMUTABLE;
CREATE FUNCTION
Time: 1.820 ms
  
COMMENT ON FUNCTION tileMaker_longitude2tile(DOUBLE PRECISION, INTEGER) IS 'Function: 	 tileMaker_longitude2tile()
Parameters:	 Longitude, zoom level
Returns:	 OSM Tile x
Description: Convert longitude (WGS84 - 4326) to OSM tile x

Derivation of the tile X/Y 

* Reproject the coordinates to the Mercator projection (from EPSG:4326 to EPSG:3857):

x = lon
y = arsinh(tan(lat)) = log[tan(lat) + sec(lat)]
(lat and lon are in radians)

* Transform range of x and y to 0 � 1 and shift origin to top left corner:

x = [1 + (x / p)] / 2
y = [1 - (y / p)] / 2

* Calculate the number of tiles across the map, n, using 2**zoom
* Multiply x and y by n. Round results down to give tilex and tiley.';
COMMENT
Time: 0.474 ms
-- SQL statement 260: Create function: latitude2tile.sql >>>
/*
 * SQL statement name: 	latitude2tile.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert latitude (WGS84 - 4326) to OSM tile y
 * Note:				% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_latitude2tile(DOUBLE PRECISION, INTEGER);
psql:pg_USA_2014.sql:4325: NOTICE:  function tilemaker_latitude2tile(pg_catalog.float8,pg_catalog.int4) does not exist, skipping
DROP FUNCTION
Time: 0.524 ms

CREATE OR REPLACE FUNCTION tileMaker_latitude2tile(latitude DOUBLE PRECISION, zoom_level INTEGER)
RETURNS INTEGER AS
$$
    SELECT FLOOR( (1.0 - LN(TAN(RADIANS(latitude)) + 1.0 / COS(RADIANS(latitude))) / PI()) / 2.0 * (1 << zoom_level) )::INTEGER
$$
LANGUAGE sql IMMUTABLE;
CREATE FUNCTION
Time: 1.521 ms
  
COMMENT ON FUNCTION tileMaker_latitude2tile(DOUBLE PRECISION, INTEGER) IS 'Function: 	 tileMaker_latitude2tile()
Parameters:	 Latitude, zoom level
Returns:	 OSM Tile y
Description: Convert latitude (WGS84 - 4326) to OSM tile x

Derivation of the tile X/Y 

* Reproject the coordinates to the Mercator projection (from EPSG:4326 to EPSG:3857):

x = lon
y = arsinh(tan(lat)) = log[tan(lat) + sec(lat)]
(lat and lon are in radians)

* Transform range of x and y to 0 � 1 and shift origin to top left corner:

x = [1 + (x / p)] / 2
y = [1 - (y / p)] / 2

* Calculate the number of tiles across the map, n, using 2**zoom
* Multiply x and y by n. Round results down to give tilex and tiley.';
COMMENT
Time: 0.393 ms
-- SQL statement 261: Create function: tile2longitude.sql >>>
/*
 * SQL statement name: 	tile2longitude.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert OSM tile x to longitude (WGS84 - 4326) 
 * Note:				% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_tile2longitude(INTEGER, INTEGER);
psql:pg_USA_2014.sql:4364: NOTICE:  function tilemaker_tile2longitude(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
DROP FUNCTION
Time: 0.385 ms

CREATE OR REPLACE FUNCTION tileMaker_tile2longitude(x INTEGER, zoom_level INTEGER)
RETURNS DOUBLE PRECISION AS
$$
	SELECT ( ( (x * 1.0) / (1 << zoom_level) * 360.0) - 180.0)::DOUBLE PRECISION
$$
LANGUAGE sql IMMUTABLE;
CREATE FUNCTION
Time: 0.923 ms
  
COMMENT ON FUNCTION tileMaker_tile2longitude(INTEGER, INTEGER) IS 'Function: 	 tileMaker_tile2longitude()
Parameters:	 OSM Tile x, zoom level
Returns:	 Longitude
Description: Convert OSM tile x to longitude (WGS84 - 4326)';
COMMENT
Time: 0.456 ms
-- SQL statement 262: Create function: tile2latitude.sql >>>
/*
 * SQL statement name: 	tileMaker_tile2latitude.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert OSM tile y to latitude (WGS84 - 4326)
 * Note:				% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_tile2latitude(INTEGER, INTEGER);
psql:pg_USA_2014.sql:4387: NOTICE:  function tilemaker_tile2latitude(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
DROP FUNCTION
Time: 0.474 ms

CREATE OR REPLACE FUNCTION tileMaker_tile2latitude(y INTEGER, zoom_level INTEGER)
RETURNS DOUBLE PRECISION AS
$BODY$
DECLARE
	n FLOAT;
	sinh FLOAT;
	E FLOAT = 2.7182818284;
BEGIN
    n = PI() - (2.0 * PI() * y) / POWER(2.0, zoom_level);
    sinh = (1 - POWER(E, -2*n)) / (2 * POWER(E, -n));
    RETURN DEGREES(ATAN(sinh));
END;
$BODY$
LANGUAGE plpgsql IMMUTABLE;
CREATE FUNCTION
Time: 0.789 ms
  
COMMENT ON FUNCTION tileMaker_tile2latitude(INTEGER, INTEGER) IS 'Function: 	 tileMaker_tile2latitude()
Parameters:	 OSM Tile y, zoom level
Returns:	 Latitude
Description: Convert OSM tile y to latitude (WGS84 - 4326)';
COMMENT
Time: 0.408 ms
-- SQL statement 263: Tile check >>>
/*
 * SQL statement name: 	tile_check.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			
 *						1: Lowest resolution geolevels table
 *						2: Geography
 *						3: min_zoomlevel
 *						4: max_zoomlevel
 *						5: Geolevel id = 1 geometry table
 *
 * Description:			Convert OSM tile y to latitude (WGS84 - 4326)
 * Note:				% becomes % after substitution
 */
WITH a AS ( /* Geolevel summary */
		SELECT a1.geography, 
		       a1.geolevel_name AS min_geolevel_name,
               MIN(geolevel_id) AS min_geolevel_id,
               9::INTEGER AS zoomlevel,
               a2.max_geolevel_id
          FROM geolevels_usa_2014 a1, (
                        SELECT geography, MAX(geolevel_id) AS max_geolevel_id
  						  FROM geolevels_usa_2014 
						 GROUP BY geography
						) a2
         WHERE a1.geography     = 'USA_2014' 
           AND a1.geography     = a2.geography
         GROUP BY a1.geography, a1.geolevel_name, a2.max_geolevel_id
        HAVING MIN(geolevel_id) = 1
), b AS ( /* Get bounds of geography */
        SELECT a2.geography,
               a2.min_geolevel_id,
               a2.max_geolevel_id,
               a2.zoomlevel,
          CASE
                                WHEN a2.zoomlevel <= 6 THEN ST_XMax(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (6+1) AND 9 THEN ST_XMax(b.geom_9)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Xmax,
          CASE
                                WHEN a2.zoomlevel <= 6 THEN ST_XMin(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (6+1) AND 9 THEN ST_XMin(b.geom_9)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Xmin,
          CASE
                                WHEN a2.zoomlevel <= 6 THEN ST_YMax(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (6+1) AND 9 THEN ST_YMax(b.geom_9)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Ymax,
          CASE
                                WHEN a2.zoomlevel <= 6 THEN ST_YMin(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (6+1) AND 9 THEN ST_YMin(b.geom_9)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Ymin
      FROM cb_2014_us_nation_5m b, a a2  
), d AS ( /* Convert XY bounds to tile numbers */
        SELECT geography, min_geolevel_id, max_geolevel_id, zoomlevel,
                   Xmin AS area_Xmin, Xmax AS area_Xmax, Ymin AS area_Ymin, Ymax AS area_Ymax,
           tileMaker_latitude2tile(Ymin, zoomlevel) AS Y_mintile,
           tileMaker_latitude2tile(Ymax, zoomlevel) AS Y_maxtile,
           tileMaker_longitude2tile(Xmin, zoomlevel) AS X_mintile,
           tileMaker_longitude2tile(Xmax, zoomlevel) AS X_maxtile
      FROM b
)
SELECT * FROM d;
 geography | min_geolevel_id | max_geolevel_id | zoomlevel | area_xmin  | area_xmax | area_ymin  | area_ymax | y_mintile | y_maxtile | x_mintile | x_maxtile 
-----------+-----------------+-----------------+-----------+------------+-----------+------------+-----------+-----------+-----------+-----------+-----------
 USA_2014  |               1 |               3 |         9 | -179.14734 | 179.77847 | -14.552549 | 71.352561 |       276 |       108 |         1 |       511
(1 row)

Time: 9.547 ms
--
-- Create tiles tables
--
-- SQL statement 265: Drop table t_tiles_usa_2014 >>>
DROP TABLE IF EXISTS t_tiles_usa_2014;
psql:pg_USA_2014.sql:4480: NOTICE:  table "t_tiles_usa_2014" does not exist, skipping
DROP TABLE
Time: 0.506 ms
-- SQL statement 266: Create tiles table >>>
/*
 * SQL statement name: 	create_tiles_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. t_tiles_cb_2014_us_county_500k
 *						2: JSON datatype (Postgres JSON, SQL server Text)
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create tiles table
 * Note:				%% becomes % after substitution
 */
CREATE TABLE t_tiles_usa_2014 (
	geolevel_id			INTEGER			NOT NULL,
	zoomlevel			INTEGER			NOT NULL,
	x					INTEGER			NOT NULL, 
	y					INTEGER			NOT NULL,
	optimised_topojson	JSON,
	tile_id				VARCHAR(200)	NOT NULL,
	areaid_count		INTEGER			NOT NULL,
	PRIMARY KEY (tile_id));
CREATE TABLE
Time: 13.814 ms
-- SQL statement 267: Comment tiles table >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE t_tiles_usa_2014 IS 'Maptiles for geography; empty tiles are added to complete zoomlevels for zoomlevels 0 to 11';
COMMENT
Time: 0.633 ms
-- SQL statement 268: Comment tiles table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN t_tiles_usa_2014.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT
Time: 0.380 ms
-- SQL statement 269: Comment tiles table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN t_tiles_usa_2014.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11';
COMMENT
Time: 0.648 ms
-- SQL statement 270: Comment tiles table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN t_tiles_usa_2014.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT
Time: 0.455 ms
-- SQL statement 271: Comment tiles table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN t_tiles_usa_2014.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT
Time: 0.425 ms
-- SQL statement 272: Comment tiles table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN t_tiles_usa_2014.optimised_topojson IS 'Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.';
COMMENT
Time: 0.336 ms
-- SQL statement 273: Comment tiles table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN t_tiles_usa_2014.tile_id IS 'Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>';
COMMENT
Time: 0.372 ms
-- SQL statement 274: Comment tiles table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN t_tiles_usa_2014.areaid_count IS 'Total number of areaIDs (geoJSON features)';
COMMENT
Time: 0.426 ms
-- SQL statement 275: Add tiles index: t_tiles_usa_2014_x_tile >>>
CREATE INDEX t_tiles_usa_2014_x_tile ON t_tiles_usa_2014 (geolevel_id, zoomlevel, x);
CREATE INDEX
Time: 5.008 ms
-- SQL statement 276: Add tiles index: t_tiles_usa_2014_y_tile >>>
CREATE INDEX t_tiles_usa_2014_y_tile ON t_tiles_usa_2014 (geolevel_id, zoomlevel, x);
CREATE INDEX
Time: 6.781 ms
-- SQL statement 277: Add tiles index: t_tiles_usa_2014_xy_tile >>>
CREATE INDEX t_tiles_usa_2014_xy_tile ON t_tiles_usa_2014 (geolevel_id, zoomlevel, x, y);
CREATE INDEX
Time: 6.606 ms
-- SQL statement 278: Add tiles index: t_tiles_usa_2014_areaid_count >>>
CREATE INDEX t_tiles_usa_2014_areaid_count ON t_tiles_usa_2014 (areaid_count);
CREATE INDEX
Time: 6.412 ms
-- SQL statement 279: Create tiles view >>>
/*
 * SQL statement name: 	create_tiles_view.sql
 * Type:				Postgres/PostGIS SQL statement
 * Parameters:
 *						1: tiles view; e.g. tiles_cb_2014_us_county_500k
 *						2: geolevel table; e.g. geolevels_cb_2014_us_county_500k
 *						3: JSON datatype (Postgres JSON, SQL server VARCHAR) [No longer used]
 *						4: tiles table; e.g. t_tiles_cb_2014_us_500k
 *  					5: Max zoomlevel; e.g. 11
 *						6: Schema; e.g. rif_data. or ""
 *						7: RIF or user schema; e.g. $(USERNAME) or rif40
 *						8: Geography; e.g. USA_2014
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
CREATE VIEW tiles_usa_2014 AS 
WITH a AS (
        SELECT geography,
               MAX(geolevel_id) AS max_geolevel_id
          FROM geolevels_usa_2014
		 WHERE geography = 'USA_2014'
         GROUP BY geography
), b AS (
         SELECT a.geography,
                generate_series(1, a.max_geolevel_id::INTEGER, 1) AS geolevel_id
           FROM a
), c AS (
        SELECT b2.geolevel_name,
               b.geolevel_id,
               b.geography,
			   b2.areaid_count
          FROM b, geolevels_usa_2014 b2
		 WHERE b.geolevel_id = b2.geolevel_id
		   AND b.geography   = b2.geography
), d AS (
        SELECT generate_series(0, 9, 1) AS zoomlevel
), ex AS (
         SELECT d.zoomlevel,
                generate_series(0, POWER(2::DOUBLE PRECISION, d.zoomlevel::DOUBLE PRECISION)::INTEGER - 1, 1) AS xy_series
           FROM d
), ey AS (
        SELECT c.geolevel_name,
			   c.areaid_count,
               c.geolevel_id,
               c.geography,
               ex.zoomlevel,
               ex.xy_series
          FROM c,
               ex 
)
SELECT z.geography,
       z.geolevel_id,
       z.geolevel_name,
       CASE
            WHEN h1.tile_id IS NULL AND h2.tile_id IS NULL THEN 1
            ELSE 0
       END AS no_area_ids, 
       COALESCE(h1.tile_id, 
				z.geolevel_id::VARCHAR||'_'||z.geolevel_name||'_'||z.zoomlevel::VARCHAR||'_'||z.x::VARCHAR||'_'||z.y::VARCHAR) AS tile_id,
       z.x,
       z.y,
       z.zoomlevel,
       COALESCE(h1.optimised_topojson, 
				h2.optimised_topojson, 
				'{"type": "FeatureCollection","features":[]}'::JSON /* NULL geojson */) AS optimised_topojson
  FROM ( 
		SELECT ey.geolevel_name,
				ey.areaid_count,
                ey.geolevel_id,
                ey.geography,
                ex.zoomlevel,
                ex.xy_series AS x,
                ey.xy_series AS y
           FROM ey, ex /* Cross join */
          WHERE ex.zoomlevel = ey.zoomlevel
		) z 
		 LEFT JOIN t_tiles_usa_2014 h1 ON ( /* Multiple area ids in the geolevel */
				z.areaid_count > 1 AND
				z.zoomlevel    = h1.zoomlevel AND 
				z.x            = h1.x AND 
				z.y            = h1.y AND 
				z.geolevel_id  = h1.geolevel_id)
		 LEFT JOIN t_tiles_usa_2014 h2 ON ( /* Single area ids in the geolevel */
				z.areaid_count = 1 AND
				h2.zoomlevel   = 0 AND 
				h2.x           = 0 AND 
				h2.y           = 0 AND 
				h2.geolevel_id = 1);
CREATE VIEW
Time: 12.364 ms
-- SQL statement 280: Comment tiles view >>>
COMMENT /*
 * SQL statement name: 	comment_view.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: view; e.g. tiles_cb_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment view
 * Note:				%% becomes % after substitution
 */
	ON VIEW tiles_usa_2014 IS 'Maptiles view for geography; empty tiles are added to complete zoomlevels for zoomlevels 0 to 11. This view is efficent!';
COMMENT
Time: 0.545 ms
-- SQL statement 281: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_usa_2014.geography IS 'Geography';
COMMENT
Time: 0.365 ms
-- SQL statement 282: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_usa_2014.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT
Time: 0.324 ms
-- SQL statement 283: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_usa_2014.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11';
COMMENT
Time: 0.278 ms
-- SQL statement 284: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_usa_2014.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT
Time: 0.564 ms
-- SQL statement 285: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_usa_2014.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT
Time: 0.307 ms
-- SQL statement 286: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_usa_2014.optimised_topojson IS 'Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.';
COMMENT
Time: 0.711 ms
-- SQL statement 287: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_usa_2014.tile_id IS 'Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>';
COMMENT
Time: 0.744 ms
-- SQL statement 288: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_usa_2014.geolevel_name IS 'Name of geolevel. This will be a column name in the numerator/denominator tables';
COMMENT
Time: 0.453 ms
-- SQL statement 289: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_usa_2014.no_area_ids IS 'Tile contains no area_ids flag: 0/1';
COMMENT
Time: 0.797 ms
--
-- Create tile limits table
--
-- SQL statement 291: Drop table tile_limits_usa_2014 >>>
DROP TABLE IF EXISTS tile_limits_usa_2014;
psql:pg_USA_2014.sql:4880: NOTICE:  table "tile_limits_usa_2014" does not exist, skipping
DROP TABLE
Time: 0.674 ms
-- SQL statement 292: Create table tile_limits_usa_2014 >>>
/*
 * SQL statement name: 	create_tile_limits_table.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: Tile limits table; e.g. tile_limits_cb_2014_us_500k
 *						2: Geometry table; e.g. geometry_cb_2014_us_500k
 *						3: max_zoomlevel
 *
 * Description:			Comment table
 * Note:				% becomes % after substitution
 */
CREATE TABLE tile_limits_usa_2014
AS 
WITH a AS (
	SELECT generate_series(0, 9, 1) AS zoomlevel
 ), b AS ( /* Get bounds of geography */
        SELECT a.zoomlevel,
		       ST_XMax(b.geom) AS Xmax,
		       ST_XMin(b.geom) AS Xmin,
		       ST_YMax(b.geom) AS Ymax,
		       ST_YMin(b.geom) AS Ymin
      FROM a 
			LEFT OUTER JOIN geometry_usa_2014 b ON (b.geolevel_id = 1 AND a.zoomlevel = b.zoomlevel)
), c AS (
        SELECT b.zoomlevel,
		       ST_XMax(b.geom) AS Xmax,
		       ST_XMin(b.geom) AS Xmin,
		       ST_YMax(b.geom) AS Ymax,
		       ST_YMin(b.geom) AS Ymin
      FROM geometry_usa_2014 b
	 WHERE b.geolevel_id  = 1
	   AND b.zoomlevel = 6
), d AS ( /* Convert XY bounds to tile numbers */
        SELECT b.zoomlevel,
               COALESCE(b.Xmin, c.Xmin) AS x_min, 
			   COALESCE(b.Xmax, c.Xmax) AS x_max, 
			   COALESCE(b.Ymin, c.Ymin) AS y_min, 
			   COALESCE(b.Ymax, c.Ymax) AS y_max,
               tileMaker_latitude2tile(COALESCE(b.Ymax, c.Ymax), b.zoomlevel) AS Y_mintile,
               tileMaker_latitude2tile(COALESCE(b.Ymin, c.Ymin), b.zoomlevel) AS Y_maxtile,
               tileMaker_longitude2tile(COALESCE(b.Xmin, c.Xmin), b.zoomlevel) AS X_mintile,
               tileMaker_longitude2tile(COALESCE(b.Xmax, c.Xmax), b.zoomlevel) AS X_maxtile
      FROM b, c
)
SELECT d.*,
       ST_MakeEnvelope(d.x_min, d.y_min, d.x_max, d.y_max, 4326) AS bbox
  FROM d;
SELECT 10
Time: 29.795 ms
-- SQL statement 293: Comment tile limits table >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE tile_limits_usa_2014 IS 'Tile limits';
COMMENT
Time: 0.456 ms
-- SQL statement 294: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at max zooomlevel (11)';
COMMENT
Time: 0.404 ms
-- SQL statement 295: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.x_min IS 'Min X (longitude)';
COMMENT
Time: 0.623 ms
-- SQL statement 296: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.x_max IS 'Max X (longitude)';
COMMENT
Time: 0.382 ms
-- SQL statement 297: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.y_min IS 'Min Y (latitude)';
COMMENT
Time: 0.659 ms
-- SQL statement 298: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.y_max IS 'Max Y (latitude)';
COMMENT
Time: 0.484 ms
-- SQL statement 299: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.y_mintile IS 'Min Y tile number (latitude)';
COMMENT
Time: 0.644 ms
-- SQL statement 300: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.y_maxtile IS 'Max Y tile number (latitude)';
COMMENT
Time: 0.840 ms
-- SQL statement 301: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.x_mintile IS 'Min X tile number (longitude)';
COMMENT
Time: 0.924 ms
-- SQL statement 302: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.x_maxtile IS 'Max X tile number (longitude)';
COMMENT
Time: 1.091 ms
-- SQL statement 303: Comment tile limits table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_limits_usa_2014.bbox IS 'Bounding box polygon for geolevel_id 1 area';
COMMENT
Time: 1.262 ms
-- SQL statement 304: Add primary key >>>
ALTER TABLE tile_limits_usa_2014 ADD PRIMARY KEY (zoomlevel);
ALTER TABLE
Time: 8.110 ms
-- SQL statement 305: Analyze table >>>
ANALYZE tile_limits_usa_2014;
ANALYZE
Time: 3.577 ms
-- SQL statement 306: Analyze table >>>
SELECT zoomlevel, x_min, x_max, y_min, y_max, y_mintile, y_maxtile, x_mintile, x_maxtile FROM tile_limits_usa_2014;
 zoomlevel |   x_min    |   x_max   |       y_min       |   y_max   | y_mintile | y_maxtile | x_mintile | x_maxtile 
-----------+------------+-----------+-------------------+-----------+-----------+-----------+-----------+-----------
         0 | -179.14734 | 179.77847 | -14.5495423181433 | 71.352561 |         0 |         0 |         0 |         0
         1 | -179.14734 | 179.77847 | -14.5495423181433 | 71.352561 |         0 |         1 |         0 |         1
         2 | -179.14734 | 179.77847 | -14.5495423181433 | 71.352561 |         0 |         2 |         0 |         3
         3 | -179.14734 | 179.77847 | -14.5495423181433 | 71.352561 |         1 |         4 |         0 |         7
         4 | -179.14734 | 179.77847 | -14.5495423181433 | 71.352561 |         3 |         8 |         0 |        15
         5 | -179.14734 | 179.77847 | -14.5495423181433 | 71.352561 |         6 |        17 |         0 |        31
         6 | -179.14734 | 179.77847 | -14.5495423181433 | 71.352561 |        13 |        34 |         0 |        63
         7 | -179.14734 | 179.77847 | -14.5495423181433 | 71.352561 |        27 |        69 |         0 |       127
         8 | -179.14734 | 179.77847 |        -14.552549 | 71.352561 |        54 |       138 |         0 |       255
         9 | -179.14734 | 179.77847 |        -14.552549 | 71.352561 |       108 |       276 |         1 |       511
(10 rows)

Time: 1.150 ms
-- SQL statement 307: Drop table tile_intersects_usa_2014 >>>
DROP TABLE IF EXISTS tile_intersects_usa_2014 CASCADE;
psql:pg_USA_2014.sql:5105: NOTICE:  table "tile_intersects_usa_2014" does not exist, skipping
DROP TABLE
Time: 0.685 ms
-- SQL statement 308: Create tile intersects table >>>
/*
 * SQL statement name: 	create_tile_intersects_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. tile_intersects_cb_2014_us_500k
 *						2: JSON datatype (Postgres: JSON, MS SQL Server: Text)
 *						3: ST_Within()/STWithin() return datatype: bit (SQL Server: 0/1) or BOOLEAN (Postgres)
 *
 * Description:			Create tile intersects table
 * Note:				% becomes % after substitution
 */
CREATE TABLE tile_intersects_usa_2014 (
	geolevel_id				INTEGER			NOT NULL,
	zoomlevel				INTEGER			NOT NULL, 
	areaid					VARCHAR(200)	NOT NULL,
	x						INTEGER			NOT NULL, 
	y						INTEGER			NOT NULL, 
    optimised_geojson		JSON,
	within					BOOLEAN				NOT NULL
);
CREATE TABLE
Time: 9.163 ms
-- SQL statement 309: Add geometry column: bbox >>>
/*
 * SQL statement name: 	add_geometry_column2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *                      5: Schema (rif_data. or "") [NEVER USED IN POSTGRES]
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('tile_intersects_usa_2014','bbox', 4326, 'POLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                         addgeometrycolumn                          
--------------------------------------------------------------------
 peter.tile_intersects_usa_2014.bbox SRID:4326 TYPE:POLYGON DIMS:2 
(1 row)

Time: 4.275 ms
-- SQL statement 310: Add geometry column: geom >>>
/*
 * SQL statement name: 	add_geometry_column2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *                      5: Schema (rif_data. or "") [NEVER USED IN POSTGRES]
 *
 * Description:			Add geometry column to table
 * Note:				%% becomes % after substitution
 */
SELECT AddGeometryColumn('tile_intersects_usa_2014','geom', 4326, 'MULTIPOLYGON', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */);
                            addgeometrycolumn                            
-------------------------------------------------------------------------
 peter.tile_intersects_usa_2014.geom SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 3.559 ms
-- SQL statement 311: Comment tile intersects table >>>
COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON TABLE tile_intersects_usa_2014 IS 'Tile area id intersects';
COMMENT
Time: 0.770 ms
-- SQL statement 312: Comment tile intersects table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_intersects_usa_2014.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT
Time: 0.844 ms
-- SQL statement 313: Comment tile intersects table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_intersects_usa_2014.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11';
COMMENT
Time: 1.032 ms
-- SQL statement 314: Comment tile intersects table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_intersects_usa_2014.areaid IS 'Area ID';
COMMENT
Time: 0.270 ms
-- SQL statement 315: Comment tile intersects table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_intersects_usa_2014.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT
Time: 0.650 ms
-- SQL statement 316: Comment tile intersects table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_intersects_usa_2014.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT
Time: 0.756 ms
-- SQL statement 317: Comment tile intersects table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_intersects_usa_2014.optimised_geojson IS 'Tile areaid intersect multipolygon in GeoJSON format, optimised for zoomlevel N.';
COMMENT
Time: 0.432 ms
-- SQL statement 318: Comment tile intersects table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_intersects_usa_2014.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
COMMENT
Time: 0.689 ms
-- SQL statement 319: Comment tile intersects table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_intersects_usa_2014.bbox IS 'Bounding box of tile as a polygon.';
COMMENT
Time: 0.694 ms
-- SQL statement 320: Comment tile intersects table column >>>
COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tile_intersects_usa_2014.geom IS 'Geometry of area.';
COMMENT
Time: 0.471 ms
-- SQL statement 321: Create partitioned tables and insert function for tile intersects table; comment partitioned tables and columns >>>
DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	partition_tile_intersects_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: tile intersects table; e.g. tile_intersects_cb_2014_us_500k
 *						2: Max zoomlevel; e.g. 11
 *						3: Geolevels table; e.g. geolevels_cb_2014_us_500k
 * 						4: Number of geolevels (e.g. 3)
 *
 * Description:			Create partitioned tables and insert function for tile intersects table; comment partitioned tables and columns
 * Note:				%% becomes % after substitution
 */
DECLARE
	c2_areaid_count 	CURSOR FOR	
		SELECT areaid_count
		  FROM geolevels_usa_2014	
		 WHERE geolevel_id = 1;
	l_areaid_count	INTEGER;
	end_zoomlevel	INTEGER;
--	
	l_table 	Text:='tile_intersects_usa_2014';
	sql_stmt	VARCHAR[];
	trigger_sql	VARCHAR;
BEGIN
	OPEN c2_areaid_count;
	FETCH c2_areaid_count INTO l_areaid_count;
	CLOSE c2_areaid_count;	
--
	FOR i IN 1 .. 3 LOOP
		IF i = 1 AND l_areaid_count = 1 THEN
			end_zoomlevel=0;	
		ELSE
			end_zoomlevel=9;
		END IF;
--	
		FOR j IN 0 .. end_zoomlevel LOOP
			sql_stmt[COALESCE(array_length(sql_stmt, 1), 0)]:='CREATE TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' ('||E'\n'||
					  '    CHECK ( geolevel_id = '||i::Text||' AND zoomlevel = '||j::Text||' )'||E'\n'||
					  ') INHERITS ('||l_table||')';	
--
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' IS ''Tile area ID intersects''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.geolevel_id IS ''ID for ordering (1=lowest resolution). Up to 99 supported.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.zoomlevel IS ''Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.areaid IS ''Area ID.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.x IS ''X tile number. From 0 to (2**<zoomlevel>)-1.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.y IS ''Y tile number. From 0 to (2**<zoomlevel>)-1.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.optimised_geojson IS ''Tile multipolygon in GeoJSON format, optimised for zoomlevel N.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.within IS ''Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.bbox IS ''Bounding box of tile as a polygon.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.geom IS ''Geometry of area.''';
--
			sql_stmt[array_length(sql_stmt, 1)]:='ALTER TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||E'\n'||
					  ' ADD CONSTRAINT '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_pk PRIMARY KEY (areaid, x, y)';
			sql_stmt[array_length(sql_stmt, 1)]:='CREATE INDEX '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_geom_gix'||E'\n'||
					  ' ON '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' USING GIST (geom);';
-- Analyze
			sql_stmt[array_length(sql_stmt, 1)]:='ANALYZE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text;
							
			IF trigger_sql IS NULL THEN
				trigger_sql:='IF ( NEW.zoomlevel = '||j::Text||' AND NEW.geolevel_id = '||i::Text||' ) THEN'||E'\n'||
							' 	INSERT INTO '||l_table||'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' VALUES (NEW.*);'||E'\n';
			ELSE
				trigger_sql:=trigger_sql||
							'ELSIF ( NEW.zoomlevel = '||j::Text||' AND NEW.geolevel_id = '||i::Text||' ) THEN'||E'\n'||
							' 	INSERT INTO '||l_table||'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' VALUES (NEW.*);'||E'\n';
			END IF;
		END LOOP;
	END LOOP;

	sql_stmt[array_length(sql_stmt, 1)]:='CREATE OR REPLACE FUNCTION '||l_table||'_insert_trigger()'||E'\n'||
		'RETURNS TRIGGER AS $trigger$'||E'\n'||
		'BEGIN'||E'\n'||
		trigger_sql||
		'    ELSE'||E'\n'||
		'        RAISE EXCEPTION ''Zoomlevel (%) or geolevel_id(%) out of range. '||
					'Fix the %_insert_trigger() function!'','||E'\n'||
		'			NEW.zoomlevel, NEW.geolevel_id, l_table;'||E'\n'||
		'    END IF;'||E'\n'||
		'    RETURN NULL;'||E'\n'||
		'END;'||E'\n'||
		'$trigger$'||E'\n'||
		'LANGUAGE plpgsql';
--
	FOR i IN 0 .. (array_length(sql_stmt, 1)-1) LOOP
		RAISE INFO 'SQL> %;', sql_stmt[i];
		EXECUTE sql_stmt[i];
	END LOOP;
END;
$$ ;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0 (
    CHECK ( geolevel_id = 1 AND zoomlevel = 0 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 0 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 1 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 2 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 3 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 4 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 5 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 6 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 7 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 8 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 9 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 0 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 1 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 2 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 3 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 4 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 5 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 6 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 7 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 8 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 9 )
) INHERITS (tile_intersects_usa_2014);
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9 IS 'Tile area ID intersects';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9.zoomlevel IS 'Zoom level: 0 to 9. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 9.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9.areaid IS 'Area ID.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9.bbox IS 'Bounding box of tile as a polygon.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> COMMENT ON COLUMN tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9.geom IS 'Geometry of area.';
psql:pg_USA_2014.sql:5430: INFO:  SQL> ALTER TABLE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9
 ADD CONSTRAINT tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9_pk PRIMARY KEY (areaid, x, y);
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE INDEX tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9_geom_gix
 ON tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9 USING GIST (geom);;
psql:pg_USA_2014.sql:5430: INFO:  SQL> ANALYZE tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9;
psql:pg_USA_2014.sql:5430: INFO:  SQL> CREATE OR REPLACE FUNCTION tile_intersects_usa_2014_insert_trigger()
RETURNS TRIGGER AS $trigger$
BEGIN
IF ( NEW.zoomlevel = 0 AND NEW.geolevel_id = 1 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 0 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 1 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 2 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 3 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 4 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 5 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 6 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 7 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 8 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 9 AND NEW.geolevel_id = 2 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 0 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 1 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 2 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 3 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 4 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 5 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 6 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 7 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 8 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8 VALUES (NEW.*);
ELSIF ( NEW.zoomlevel = 9 AND NEW.geolevel_id = 3 ) THEN
 	INSERT INTO tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9 VALUES (NEW.*);
    ELSE
        RAISE EXCEPTION 'Zoomlevel (%) or geolevel_id(%) out of range. Fix the %_insert_trigger() function!',
			NEW.zoomlevel, NEW.geolevel_id, l_table;
    END IF;
    RETURN NULL;
END;
$trigger$
LANGUAGE plpgsql;
DO
Time: 506.494 ms
-- SQL statement 322: Partition tile intersects table: insert trigger >>>
/*
 * SQL statement name: 	partition_trigger.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Create partitioned tables insert trigger
 * Note:				%% becomes % after substitution
 */
 CREATE TRIGGER insert_tile_intersects_usa_2014_trigger
    BEFORE INSERT ON tile_intersects_usa_2014
    FOR EACH ROW EXECUTE PROCEDURE tile_intersects_usa_2014_insert_trigger();
CREATE TRIGGER
Time: 1.204 ms
-- SQL statement 323: Comment partition tile intersects table: insert trigger >>>
/*
 * SQL statement name: 	comment_partition_trigger.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Comment create partitioned tables insert trigger
 * Note:				%% becomes % after substitution
 */
 COMMENT ON TRIGGER insert_tile_intersects_usa_2014_trigger ON tile_intersects_usa_2014 IS 'Partitioned tables insert trigger';
COMMENT
Time: 0.638 ms
-- SQL statement 324: INSERT into tile intersects table >>>
/*
 * SQL statement name: 	tile_intersects_insert.sql
 * Type:				Postgres/PostGIS SQL
 * Parameters:
 *						1: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *						2: Tile limits table name; e.g. tile_limits_cb_2014_us_500k
 *						3: Geometry table name; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Insert into tile intersects table
 * Note:				% becomes % after substitution
 */
EXPLAIN ANALYZE INSERT INTO tile_intersects_usa_2014 (
	geolevel_id,
	zoomlevel, 
	areaid,
	x, 
	y, 
	bbox,
	geom,
    optimised_geojson,
	within
)
WITH a AS (
	SELECT zoomlevel, x_mintile, x_maxtile, y_mintile, y_maxtile	  
	  FROM tile_limits_usa_2014
	 WHERE zoomlevel = 0
), x AS (
	SELECT zoomlevel, generate_series(x_mintile, x_maxtile) AS x_series
	  FROM a
), y AS (	 
	SELECT zoomlevel, generate_series(y_mintile, y_maxtile) AS y_series	
	  FROM a       
), b AS (
	SELECT x.zoomlevel, 
	       x.x_series AS x, 
	       y.y_series AS y,      
	       tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
		   tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
		   tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
		   tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
      FROM x, y
	 WHERE x.zoomlevel = y.zoomlevel
), c AS (
	SELECT b.zoomlevel, b.x, b.y, 
		   ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) AS bbox,
		   c.geolevel_id,
		   c.areaid,
		   c.geom
	  FROM b, geometry_usa_2014 c
	 WHERE c.zoomlevel = 6
	   AND ST_Intersects(ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), c.geom) /* intersects */
)
SELECT c.geolevel_id,
	   c.zoomlevel, 
	   c.areaid,
	   c.x, 
	   c.y, 
	   c.bbox,
	   c.geom,
       ST_AsGeoJson(c.geom)::JSON AS optimised_geojson,
	   ST_Within(c.bbox, c.geom) AS within /* Used to exclude any tile boundary completely within the area, i.e. there are no bounaries in the tile */
  FROM c
 ORDER BY c.geolevel_id, c.zoomlevel, c.x, c.y;
                                                                                                                 QUERY PLAN                                                                                                                  
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 Insert on tile_intersects_usa_2014  (cost=15084.98..15153.55 rows=5485 width=531) (actual time=2790.231..2790.231 rows=0 loops=1)
   ->  Subquery Scan on "*SELECT*"  (cost=15084.98..15153.55 rows=5485 width=531) (actual time=1267.341..1271.530 rows=3289 loops=1)
         ->  Sort  (cost=15084.98..15098.70 rows=5485 width=498) (actual time=1267.339..1268.180 rows=3289 loops=1)
               Sort Key: c.geolevel_id, c.zoomlevel, c.x, c.y
               Sort Method: quicksort  Memory: 30030kB
               CTE a
                 ->  Seq Scan on tile_limits_usa_2014  (cost=0.00..1.13 rows=1 width=20) (actual time=0.009..0.010 rows=1 loops=1)
                       Filter: (zoomlevel = 0)
                       Rows Removed by Filter: 9
               CTE x
                 ->  CTE Scan on a  (cost=0.00..5.02 rows=1000 width=12) (actual time=0.015..0.017 rows=1 loops=1)
               CTE y
                 ->  CTE Scan on a a_1  (cost=0.00..5.02 rows=1000 width=12) (actual time=0.002..0.003 rows=1 loops=1)
               CTE b
                 ->  Merge Join  (cost=139.66..2944.66 rows=5000 width=12) (actual time=1.627..1.629 rows=1 loops=1)
                       Merge Cond: (x.zoomlevel = y.zoomlevel)
                       ->  Sort  (cost=69.83..72.33 rows=1000 width=8) (actual time=0.022..0.023 rows=1 loops=1)
                             Sort Key: x.zoomlevel
                             Sort Method: quicksort  Memory: 25kB
                             ->  CTE Scan on x  (cost=0.00..20.00 rows=1000 width=8) (actual time=0.016..0.018 rows=1 loops=1)
                       ->  Sort  (cost=69.83..72.33 rows=1000 width=8) (actual time=0.006..0.006 rows=1 loops=1)
                             Sort Key: y.zoomlevel
                             Sort Method: quicksort  Memory: 25kB
                             ->  CTE Scan on y  (cost=0.00..20.00 rows=1000 width=8) (actual time=0.003..0.003 rows=1 loops=1)
               CTE c
                 ->  Nested Loop  (cost=0.00..10252.71 rows=5485 width=2867) (actual time=3.982..94.161 rows=3289 loops=1)
                       ->  CTE Scan on b  (cost=0.00..100.00 rows=5000 width=44) (actual time=1.635..1.638 rows=1 loops=1)
                       ->  Append  (cost=0.00..1.99 rows=4 width=61461) (actual time=2.311..89.197 rows=3289 loops=1)
                             ->  Seq Scan on geometry_usa_2014 c_1  (cost=0.00..0.00 rows=1 width=454) (actual time=0.003..0.003 rows=0 loops=1)
                                   Filter: ((zoomlevel = 6) AND (st_makeenvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) && geom) AND _st_intersects(st_makeenvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), geom))
                             ->  Index Scan using geometry_usa_2014_geolevel_id_1_zoomlevel_6_geom_gix on geometry_usa_2014_geolevel_id_1_zoomlevel_6 c_2  (cost=0.13..0.40 rows=1 width=207639) (actual time=2.246..2.344 rows=1 loops=1)
                                   Index Cond: (st_makeenvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) && geom)
                                   Filter: ((zoomlevel = 6) AND _st_intersects(st_makeenvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), geom))
                             ->  Index Scan using geometry_usa_2014_geolevel_id_2_zoomlevel_6_geom_gix on geometry_usa_2014_geolevel_id_2_zoomlevel_6 c_3  (cost=0.14..0.42 rows=1 width=35556) (actual time=0.299..18.006 rows=56 loops=1)
                                   Index Cond: (st_makeenvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) && geom)
                                   Filter: ((zoomlevel = 6) AND _st_intersects(st_makeenvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), geom))
                             ->  Index Scan using geometry_usa_2014_geolevel_id_3_zoomlevel_6_geom_gix on geometry_usa_2014_geolevel_id_3_zoomlevel_6 c_4  (cost=0.15..1.16 rows=1 width=2194) (actual time=0.064..68.505 rows=3232 loops=1)
                                   Index Cond: (st_makeenvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) && geom)
                                   Filter: ((zoomlevel = 6) AND _st_intersects(st_makeenvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), geom))
                                   Rows Removed by Filter: 1
               ->  CTE Scan on c  (cost=0.00..1535.80 rows=5485 width=498) (actual time=31.646..1234.629 rows=3289 loops=1)
 Planning time: 2.754 ms
 Trigger insert_tile_intersects_usa_2014_trigger: time=1510.213 calls=3289
 Execution time: 2796.367 ms
(44 rows)

Time: 2801.206 ms
-- SQL statement 325: Add primary key >>>
ALTER TABLE tile_intersects_usa_2014 ADD PRIMARY KEY (geolevel_id, zoomlevel, areaid, x, y);
ALTER TABLE
Time: 7.887 ms
-- SQL statement 326: Analyze table >>>
ANALYZE tile_intersects_usa_2014;
ANALYZE
Time: 142.897 ms
-- SQL statement 327: SELECT from tile intersects table >>>
/*
 * SQL statement name: 	tile_intersects_select.sql
 * Type:				Postgres/PostGIS SQL
 * Parameters:
 *						1: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *
 * Description:			Select from tile intersects table
 
 geolevel_id | zoomlevel | areaid | x | y | within |                                                                             bbox

-------------+-----------+--------+---+---+--------+--------------------------------------------------------------------------------------------------------------------------------------------------------------
           1 |         0 | US     | 0 | 0 | f      | {"type":"Polygon","coordinates":[[[-180,85.0511287794693],[-180,-85.0511287794693],[180,-85.0511287794693],[180,85.0511287794693],[-180,85.0511287794693]]]}
(1 row)

 * Note:				% becomes % after substitution
 */
SELECT geolevel_id,
	   zoomlevel, 
	   areaid,
	   x, 
	   y, 
	   within,
	   ST_AsGeoJson(bbox) AS bbox
  FROM tile_intersects_usa_2014
 WHERE zoomlevel = 0 AND geolevel_id = 1;
 geolevel_id | zoomlevel | areaid | x | y | within |                                                                             bbox                                                                             
-------------+-----------+--------+---+---+--------+--------------------------------------------------------------------------------------------------------------------------------------------------------------
           1 |         0 | US     | 0 | 0 | f      | {"type":"Polygon","coordinates":[[[-180,85.0511287794693],[-180,-85.0511287794693],[180,-85.0511287794693],[180,85.0511287794693],[-180,85.0511287794693]]]}
(1 row)

Time: 4.464 ms
-- SQL statement 328: Create tile intersects table INSERT function >>>
CREATE OR REPLACE FUNCTION tileMaker_intersector_usa_2014(
	l_geolevel_id INTEGER, 
	l_zoomlevel INTEGER, 
	l_use_zoomlevel INTEGER, 
	l_debug BOOLEAN DEFAULT FALSE)
RETURNS INTEGER
AS
$BODY$
/*
 * SQL statement name: 	tileMaker_intersector_function.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: function name; e.g. tileMaker_intersector_cb_2014_us_500k
 *						2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k
 *						3: tile limits table; e.g. tile_limits_cb_2014_us_500k
 *						4: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Create tile intersects table INSERT function
 * Note:				%% becomes % after substitution
 */
 
/*
Function: 		tileMaker_intersector_usa_2014()
Parameters:		geolevel_id, zoomlevel, use zoomlevel (source of data), debug (TRUE/FALSE)
Returns:		Nothing
Description:	tile intersects table INSERT function. Zoomlevels <6 use zoomlevel 6 data
				Insert tile area id intersections.  
 */
DECLARE
	c1_i1	CURSOR FOR
		SELECT COUNT(areaid) AS total
		  FROM tile_intersects_usa_2014
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel;
--
	num_rows INTEGER;
	explain_line	text;
	explain_text	text:='';
BEGIN
	FOR explain_line IN EXPLAIN ANALYZE 
	INSERT INTO tile_intersects_usa_2014(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
	WITH a	AS (
		SELECT b.zoomlevel AS zoomlevel, b.x_mintile, b.x_maxtile, b.y_mintile, b.y_maxtile	  
		  FROM tile_limits_usa_2014 b
		 WHERE l_zoomlevel = b.zoomlevel
	), x AS (
		SELECT zoomlevel, generate_series(x_mintile, x_maxtile) AS x_series
		  FROM a
	), y AS (	 
		SELECT zoomlevel, generate_series(y_mintile, y_maxtile) AS y_series	
		  FROM a       
	), b AS (
		SELECT x.zoomlevel, 
			   x.x_series AS x, 
			   y.y_series AS y,      
			   tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
			   tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
			   tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
			   tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
		  FROM x, y
		 WHERE x.zoomlevel = y.zoomlevel
	), c AS ( /* Calculate bounding box, parent X/Y min */
		SELECT b.zoomlevel, 
		       b.x,
			   b.y, 
			   ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) AS bbox,
			   tileMaker_latitude2tile(b.ymin, b.zoomlevel-1) AS parent_ymin,
			   tileMaker_longitude2tile(b.xmin, b.zoomlevel-1) AS parent_xmin
		  FROM b
	), d AS (
		SELECT c.zoomlevel, c.x, c.y, c.bbox, p.areaid, p.within
		  FROM c, tile_intersects_usa_2014 p /* Parent */
		 WHERE p.geolevel_id = l_geolevel_id
		   AND p.zoomlevel 	 = l_zoomlevel -1/* Join to parent tile from previous geolevel_id; i.e. exclude if not present */
		   AND c.parent_xmin = p.x  
	       AND c.parent_ymin = p.y	
	), e AS (
		SELECT d.zoomlevel, d.x, d.y, d.bbox, d.areaid
		  FROM d
		 WHERE NOT EXISTS (SELECT c2.areaid
						     FROM tile_intersects_usa_2014 c2
						    WHERE c2.geolevel_id = l_geolevel_id
						      AND c2.zoomlevel   = l_zoomlevel
						 	  AND c2.x           = d.x
					 		  AND c2.y           = d.y
							  AND c2.areaid      = d.areaid)
	), f AS (
		SELECT e.zoomlevel, l_geolevel_id AS geolevel_id, e.x, e.y, e.bbox, e2.areaid, e2.geom
		  FROM e, geometry_usa_2014 e2
	     WHERE e2.zoomlevel    = l_use_zoomlevel
		   AND e2.geolevel_id  = l_geolevel_id
		   AND e2.areaid       = e.areaid
		   AND (e.bbox && e2.geom) 			  /* Intersect by bounding box */
		   AND ST_Intersects(e.bbox, e2.geom) /* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
	)
	SELECT f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y, f.bbox, f.geom, 
	       ST_AsGeoJson(f.geom)::JSON AS optimised_geojson,
	       true::BOOLEAN AS within
	  FROM f
	 WHERE NOT ST_Within(f.bbox, f.geom) /* Exclude any tile bounding completely within the area */
	 ORDER BY f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y 
	LOOP
		IF num_rows IS NULL THEN
		END IF;
		explain_text:=explain_text||E'\n'||explain_line;	
	END LOOP; 
	IF l_debug THEN
		RAISE INFO '%', explain_text;
	END IF;
--
	OPEN c1_i1;
	FETCH c1_i1 INTO num_rows;
	CLOSE c1_i1;
--	 
	RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;
CREATE FUNCTION
Time: 5.114 ms
-- SQL statement 329: Create second tile intersects table INSERT function (simplification errors) >>>
CREATE OR REPLACE FUNCTION tileMaker_intersector2_usa_2014(
	l_geolevel_id INTEGER, 
	l_zoomlevel INTEGER, 
	l_use_zoomlevel INTEGER, 
	l_debug BOOLEAN DEFAULT FALSE)
RETURNS INTEGER
AS
$BODY$
/*
 * SQL statement name: 	tileMaker_intersector_function2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: function name; e.g. tileMaker_intersector2_cb_2014_us_500k
 *						2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k
 *						3: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Create tile intersects table INSERT function
 * Note:				%% becomes % after substitution
 */
 
/*
Function: 		tileMaker_intersector2_usa_2014()
Parameters:		geolevel_id, zoomlevel, use zoomlevel (source of data), debug (TRUE/FALSE)
Returns:		Nothing
Description:	tile intersects table INSERT function. Zoomlevels <6 use zoomlevel 6 data
				Insert tile area id intersections missing where not in the previous layer; 
				this is usually due to it being simplified out of existance.  
 */
DECLARE
	c1_i2	CURSOR FOR
		SELECT COUNT(areaid) AS total
		  FROM tile_intersects_usa_2014
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel
		   AND NOT within;
--			  
	num_rows 		INTEGER;
	explain_line	text;
	explain_text	text:='';
BEGIN
 	FOR explain_line IN EXPLAIN ANALYZE 
	INSERT INTO tile_intersects_usa_2014(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
	WITH a AS (
		SELECT DISTINCT geolevel_id, areaid
		  FROM geometry_usa_2014
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel
		EXCEPT 
		SELECT DISTINCT geolevel_id, areaid
		  FROM tile_intersects_usa_2014 a
		 WHERE zoomlevel = l_zoomlevel
		   AND geolevel_id = l_geolevel_id
	), b AS (
		SELECT a.geolevel_id, a.areaid, ST_Envelope(b.geom) AS bbox, b.geom
		  FROM a, geometry_usa_2014 b
		 WHERE a.geolevel_id = l_geolevel_id
		   AND zoomlevel     = l_zoomlevel
		   AND a.areaid      = b.areaid
		   AND NOT ST_IsEmpty(b.geom)
	), c AS (
		SELECT l_zoomlevel AS zoomlevel, 
			   b.geolevel_id, b.areaid,
			   tileMaker_latitude2tile(ST_Ymin(bbox), l_zoomlevel) AS y_mintile,
			   tileMaker_longitude2tile(ST_Xmin(bbox), l_zoomlevel) AS x_mintile,
			   tileMaker_latitude2tile(ST_Ymax(bbox), l_zoomlevel) AS y_maxtile,
			   tileMaker_longitude2tile(ST_Xmax(bbox), l_zoomlevel) AS x_maxtile,
			   b.geom
		   FROM b
	), x AS (
		SELECT c.zoomlevel, 
			   c.geolevel_id, 
			   c.areaid,
			   generate_series(x_mintile, x_maxtile) AS x_series
		  FROM c
	), y AS (	 
		SELECT c.zoomlevel, 
			   c.geolevel_id, 
			   c.areaid,
			   generate_series(y_mintile, y_maxtile) AS y_series	
		  FROM c 
	), d AS (
		SELECT x.zoomlevel, 
			   x.geolevel_id, 
			   x.areaid,
			   x.x_series AS x, 
			   y.y_series AS y,      
			   tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
			   tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
			   tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
			   tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
		  FROM x, y
		 WHERE x.zoomlevel   = y.zoomlevel	
		   AND x.geolevel_id = y.geolevel_id
		   AND x.areaid      = y.areaid
	), e AS (
		SELECT d.zoomlevel, 
			   d.geolevel_id, 
			   d.areaid,
			   d.x,
			   d.y, 
			   ST_MakeEnvelope(d.xmin, d.ymin, d.xmax, d.ymax, 4326) AS bbox
		  FROM d
	), f AS (
		SELECT DISTINCT e.zoomlevel, 
			   e.geolevel_id, 
			   e.areaid, 
			   e.x,
			   e.y,
			   e.bbox
		  FROM e
		 WHERE NOT EXISTS (SELECT c2.areaid
							 FROM tile_intersects_usa_2014 c2
							WHERE c2.geolevel_id = l_geolevel_id
							  AND c2.zoomlevel   = l_zoomlevel
							  AND c2.x           = e.x
							  AND c2.y           = e.y	
							  AND c2.areaid      = e.areaid)
	), g AS (
			SELECT f.zoomlevel, f.geolevel_id, f.x, f.y, f.bbox, e2.areaid, e2.geom
			  FROM f, geometry_usa_2014 e2
			 WHERE e2.zoomlevel    = l_use_zoomlevel
			   AND e2.geolevel_id  = l_geolevel_id
			   AND e2.areaid       = f.areaid
			   AND (f.bbox && e2.geom) 			  /* Intersect by bounding box */
			   AND ST_Intersects(f.bbox, e2.geom) /* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
	)
	SELECT geolevel_id, zoomlevel, areaid, x, y, bbox, geom,	
	       ST_AsGeoJson(g.geom)::JSON AS optimised_geojson,
	       ST_Within(g.bbox, g.geom) AS within
 	  FROM g 
	 ORDER BY geolevel_id, zoomlevel, areaid, x, y
	LOOP		
		explain_text:=explain_text||E'\n'||explain_line;
	END LOOP;
	IF l_debug THEN
		RAISE INFO '%', explain_text;
	END IF;
--
	OPEN c1_i2;
	FETCH c1_i2 INTO num_rows;
	CLOSE c1_i2;
--	 
	RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;
CREATE FUNCTION
Time: 2.335 ms
-- SQL statement 330: Create tiles table INSERT function (tile aggregator) >>>
CREATE OR REPLACE FUNCTION tileMaker_aggregator_usa_2014(
	l_geolevel_id INTEGER, 
	l_zoomlevel INTEGER,  
	l_debug BOOLEAN DEFAULT FALSE)
RETURNS INTEGER
AS
$BODY$
/*
 * SQL statement name: 	tileMaker_aggregator_function.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: function name; e.g. tileMaker_aggregator_cb_2014_us_500k
 *						2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k
 *						3: tiles table; e.g. t_tiles_cb_2014_us_500k
 *						4: geolevels table; e.g. geolevels_cb_2014_us_500k
 *
 * Description:			Create tiles table INSERT function (tile aggregator)
 * Note:				%% becomes % after substitution
 */
 
/*
Function: 		tileMaker_aggregator_usa_2014()
Parameters:		geolevel_id, zoomlevel, use zoomlevel (source of data), debug (TRUE/FALSE)
Returns:		Nothing
Description:	tiles table INSERT function. Aggregate area_id JSON into featureCollection
 */
DECLARE
	c1_i3	CURSOR FOR
		SELECT COUNT(tile_id) AS total
		  FROM t_tiles_usa_2014
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel;
--
	num_rows 		INTEGER;
	explain_line	text;
	explain_text	text:='';
--
	sql_stmt		text;
BEGIN
 	FOR explain_line IN EXPLAIN ANALYZE 
	INSERT INTO t_tiles_usa_2014(
		geolevel_id,
		zoomlevel,
		x, 
		y,
		optimised_geojson,
		optimised_topojson,
		tile_id,
		areaid_count)
	SELECT a.geolevel_id,
		   zoomlevel,
		   x, 
		   y,
		   json_agg(optimised_geojson) AS optimised_geojson,
		   NULL::JSON AS optimised_topojson,
		   a.geolevel_id::Text||'_'||b.geolevel_name||'_'||a.zoomlevel||'_'||a.x::Text||'_'||a.y::Text AS tile_id,
		   COUNT(a.geolevel_id) AS areaid_count
	  FROM tile_intersects_usa_2014 a, geolevels_usa_2014 b
	 WHERE a.geolevel_id = b.geolevel_id 
	   AND a.zoomlevel   = l_zoomlevel
	   AND a.geolevel_id = l_geolevel_id
	 GROUP BY a.geolevel_id,
			  b.geolevel_name,
			  a.zoomlevel,
			  a.x, 
			  a.y
	 ORDER BY 1, 2, 3, 4
	LOOP		
		explain_text:=explain_text||E'\n'||explain_line;
	END LOOP;
	IF l_debug THEN
		RAISE INFO '%', explain_text;
	END IF;
--
	sql_stmt:='REINDEX TABLE tile_intersects_usa_2014'||'_geolevel_id_'||l_geolevel_id::Text||'_zoomlevel_'||l_zoomlevel::Text;
	EXECUTE sql_stmt;
	sql_stmt:='ANALYZE tile_intersects_usa_2014'||'_geolevel_id_'||l_geolevel_id::Text||'_zoomlevel_'||l_zoomlevel::Text;
	EXECUTE sql_stmt;
--
	OPEN c1_i3;
	FETCH c1_i3 INTO num_rows;
	CLOSE c1_i3;
--	 
	RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;
CREATE FUNCTION
Time: 1.961 ms
-- SQL statement 331: Create tiles table INSERT function (tile aggregator) >>>
/*
 * SQL statement name: 	tileMaker_main_function.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *						2: geometry table; e.g. geometry_cb_2014_us_500k
 *						3: geolevels table; e.g. geolevels_cb_2014_us_500k
 *
 * Description:			Main tileMaker function. Create geoJSON tiles
 * Note:				%% becomes % after substitution
 */
DO LANGUAGE plpgsql $$
DECLARE
	max_geolevel_id		INTEGER;
	max_zoomlevel 		INTEGER;
	l_areaid_count		INTEGER;
	start_geolevel_id	INTEGER;
--
	c1_maxgeolevel_id 	CURSOR FOR
		SELECT MAX(geolevel_id) AS max_geolevel_id,
	           MAX(zoomlevel) AS max_zoomlevel
	      FROM geometry_usa_2014;
	c2_areaid_count 	CURSOR FOR	
		SELECT areaid_count
		  FROM geolevels_usa_2014	
		 WHERE geolevel_id = 1;
--
	num_rows 		INTEGER:=0;
	num_rows2 		INTEGER:=0;
	num_rows3 		INTEGER:=0;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
	took3 			INTERVAL;
	took4 			INTERVAL;
--
	tiles_per_s		NUMERIC;
--
	l_use_zoomlevel INTEGER;
	l_debug 		BOOLEAN;
BEGIN
	OPEN c1_maxgeolevel_id;
	FETCH c1_maxgeolevel_id INTO max_geolevel_id, max_zoomlevel;
	CLOSE c1_maxgeolevel_id;
	OPEN c2_areaid_count;
	FETCH c2_areaid_count INTO l_areaid_count;
	CLOSE c2_areaid_count;	
--	 
--	max_zoomlevel 	:=10;		/* Override for test purposes */
	IF l_areaid_count = 1 THEN	/* 0/0/0 tile only;  */			
		start_geolevel_id=2;	
	ELSE
		start_geolevel_id=1;
	END IF;
--
-- Create zoomleve l0 tiles. Intersect already created
--	
	FOR i IN 1 .. max_geolevel_id LOOP
--			
		stp2:=clock_timestamp();
--		num_rows3:=tileMaker_aggregator_usa_2014(i, 0, l_debug);	
		etp:=clock_timestamp();
		took4:=age(etp, stp2);
--			
		IF num_rows3 > 0 THEN
			took:=age(etp, stp);
			tiles_per_s:=ROUND(num_rows3::NUMERIC/EXTRACT(EPOCH FROM took4)::NUMERIC, 1);
			RAISE INFO 'Processed % tile for geolevel id %/% zoomlevel: %/% in %s, %s total;% tiles/s', 
				num_rows3, 
				i, max_geolevel_id, 0, max_zoomlevel,  
				ROUND(EXTRACT(EPOCH FROM took4)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
				tiles_per_s;	
		END IF;
	END LOOP;
		
--
-- Timing; 3 zoomlevels to:
--
-- Zoomlevel 7: 1 minute (75)
-- Zoomlevel 8: 3 minutes (321..260..154..218..230..217..166 seconds with tile aggregation)
-- Zoomlevel 9: 8 minutes (673..627..460)
-- Zoomlevel 10: 24 minutes (1473)
-- Zoomlevel 11: 80 minutes (4810)
--
	FOR i IN start_geolevel_id .. max_geolevel_id LOOP
		FOR j IN 1 .. max_zoomlevel LOOP
			l_debug:=FALSE;
			IF j = max_zoomlevel AND i = max_geolevel_id THEN
				l_debug:=TRUE;
			END IF;
			l_use_zoomlevel=j;
			IF j<6 THEN 
				l_use_zoomlevel=6;
			END IF;
			stp2:=clock_timestamp();
			num_rows:=tileMaker_intersector_usa_2014(i, j, l_use_zoomlevel, l_debug);
			etp:=clock_timestamp();
			took2:=age(etp, stp2);
--			
			stp2:=clock_timestamp();
			num_rows2:=tileMaker_intersector2_usa_2014(i, j, l_use_zoomlevel, l_debug);	
			etp:=clock_timestamp();
			took3:=age(etp, stp2);
--			
			stp2:=clock_timestamp();
--			num_rows3:=tileMaker_aggregator_cb_2014_us_500k(i, j, l_debug);	/* Replaced by pgTileMaker.js */
			etp:=clock_timestamp();
			took4:=age(etp, stp2);
--			
			took:=age(etp, stp);
			tiles_per_s:=ROUND((num_rows+num_rows2+num_rows3)::NUMERIC/EXTRACT(EPOCH FROM took2)::NUMERIC, 1);
			RAISE INFO 'Processed %+% total areaid intersects for geolevel id %/% zoomlevel: %/% in %+%s+%s, %s total; % intesects/s', 
				num_rows, num_rows2,  
				i, max_geolevel_id, j, max_zoomlevel, 
				ROUND(EXTRACT(EPOCH FROM took2)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took3)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took4)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
				tiles_per_s;			
		END LOOP;	
	END LOOP;
END;
$$;
psql:pg_USA_2014.sql:6039: INFO:  Processed 57+0 total areaid intersects for geolevel id 2/3 zoomlevel: 1/9 in 0.8+0.0s+0.0s, 0.8s total; 71.2 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 67+0 total areaid intersects for geolevel id 2/3 zoomlevel: 2/9 in 1.2+0.0s+0.0s, 2.0s total; 57.5 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 81+0 total areaid intersects for geolevel id 2/3 zoomlevel: 3/9 in 1.6+0.0s+0.0s, 3.6s total; 50.6 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 95+0 total areaid intersects for geolevel id 2/3 zoomlevel: 4/9 in 3.1+0.0s+0.0s, 6.7s total; 30.9 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 142+0 total areaid intersects for geolevel id 2/3 zoomlevel: 5/9 in 5.4+0.0s+0.0s, 12.1s total; 26.3 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 242+0 total areaid intersects for geolevel id 2/3 zoomlevel: 6/9 in 12.4+0.0s+0.0s, 24.5s total; 19.6 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 487+0 total areaid intersects for geolevel id 2/3 zoomlevel: 7/9 in 31.7+0.0s+0.0s, 56.2s total; 15.4 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 1020+0 total areaid intersects for geolevel id 2/3 zoomlevel: 8/9 in 101.4+0.0s+0.0s, 157.6s total; 10.1 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 2248+0 total areaid intersects for geolevel id 2/3 zoomlevel: 9/9 in 313.8+0.0s+0.0s, 471.4s total; 7.2 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 3233+0 total areaid intersects for geolevel id 3/3 zoomlevel: 1/9 in 7.0+0.0s+0.0s, 478.5s total; 459.1 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 3291+0 total areaid intersects for geolevel id 3/3 zoomlevel: 2/9 in 6.9+0.0s+0.0s, 485.3s total; 480.2 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 3390+0 total areaid intersects for geolevel id 3/3 zoomlevel: 3/9 in 4.9+0.0s+0.0s, 490.2s total; 693.6 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 3440+0 total areaid intersects for geolevel id 3/3 zoomlevel: 4/9 in 4.2+0.0s+0.0s, 494.4s total; 821.5 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 3658+0 total areaid intersects for geolevel id 3/3 zoomlevel: 5/9 in 4.6+0.0s+0.0s, 499.1s total; 795.8 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 4065+3 total areaid intersects for geolevel id 3/3 zoomlevel: 6/9 in 4.7+0.0s+0.0s, 503.8s total; 866.9 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 4989+1 total areaid intersects for geolevel id 3/3 zoomlevel: 7/9 in 7.8+0.0s+0.0s, 511.7s total; 636.6 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  Processed 7127+4 total areaid intersects for geolevel id 3/3 zoomlevel: 8/9 in 19.1+0.1s+0.0s, 530.8s total; 373.4 intesects/s
psql:pg_USA_2014.sql:6039: INFO:  
Insert on tile_intersects_usa_2014  (cost=6581.76..6581.77 rows=1 width=531) (actual time=48351.791..48351.791 rows=0 loops=1)
  ->  Subquery Scan on "*SELECT*"  (cost=6581.76..6581.77 rows=1 width=531) (actual time=26855.286..27212.065 rows=12271 loops=1)
        ->  Sort  (cost=6581.76..6581.76 rows=1 width=498) (actual time=26855.283..27180.611 rows=12271 loops=1)
              Sort Key: f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y
              Sort Method: external merge  Disk: 379984kB
              CTE a
                ->  Seq Scan on tile_limits_usa_2014 b  (cost=0.00..1.13 rows=1 width=20) (actual time=0.004..0.005 rows=1 loops=1)
                      Filter: (9 = zoomlevel)
                      Rows Removed by Filter: 9
              CTE x
                ->  CTE Scan on a  (cost=0.00..5.02 rows=1000 width=12) (actual time=0.009..0.038 rows=511 loops=1)
              CTE y
                ->  CTE Scan on a a_1  (cost=0.00..5.02 rows=1000 width=12) (actual time=0.002..0.015 rows=169 loops=1)
              CTE b
                ->  Merge Join  (cost=139.66..2944.66 rows=5000 width=12) (actual time=0.275..1008.321 rows=86359 loops=1)
                      Merge Cond: (x.zoomlevel = y.zoomlevel)
                      ->  Sort  (cost=69.83..72.33 rows=1000 width=8) (actual time=0.162..0.190 rows=511 loops=1)
                            Sort Key: x.zoomlevel
                            Sort Method: quicksort  Memory: 48kB
                            ->  CTE Scan on x  (cost=0.00..20.00 rows=1000 width=8) (actual time=0.009..0.122 rows=511 loops=1)
                      ->  Sort  (cost=69.83..72.33 rows=1000 width=8) (actual time=0.061..4.789 rows=85849 loops=1)
                            Sort Key: y.zoomlevel
                            Sort Method: quicksort  Memory: 32kB
                            ->  CTE Scan on y  (cost=0.00..20.00 rows=1000 width=8) (actual time=0.002..0.045 rows=169 loops=1)
              CTE c
                ->  CTE Scan on b b_1  (cost=0.00..412.50 rows=5000 width=44) (actual time=0.300..1236.382 rows=86359 loops=1)
              CTE d
                ->  Hash Join  (cost=175.00..3196.47 rows=1 width=463) (actual time=1316.573..1350.940 rows=28504 loops=1)
                      Hash Cond: ((p.x = c.parent_xmin) AND (p.y = c.parent_ymin))
                      ->  Append  (cost=0.00..3018.95 rows=2 width=427) (actual time=0.006..11.204 rows=7131 loops=1)
                            ->  Seq Scan on tile_intersects_usa_2014 p  (cost=0.00..0.00 rows=1 width=427) (actual time=0.000..0.000 rows=0 loops=1)
                                  Filter: ((geolevel_id = 3) AND (zoomlevel = 8))
                            ->  Seq Scan on tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8 p_1  (cost=0.00..3018.95 rows=1 width=427) (actual time=0.005..10.387 rows=7131 loops=1)
                                  Filter: ((geolevel_id = 3) AND (zoomlevel = 8))
                      ->  Hash  (cost=100.00..100.00 rows=5000 width=52) (actual time=1316.530..1316.530 rows=86359 loops=1)
                            Buckets: 131072 (originally 8192)  Batches: 1 (originally 1)  Memory Usage: 15530kB
                            ->  CTE Scan on c  (cost=0.00..100.00 rows=5000 width=52) (actual time=0.302..1284.138 rows=86359 loops=1)
              CTE e
                ->  Nested Loop Anti Join  (cost=0.00..8.21 rows=1 width=462) (actual time=1316.610..1576.689 rows=28504 loops=1)
                      ->  CTE Scan on d  (cost=0.00..0.02 rows=1 width=462) (actual time=1316.575..1377.222 rows=28504 loops=1)
                      ->  Append  (cost=0.00..8.17 rows=2 width=426) (actual time=0.006..0.006 rows=0 loops=28504)
                            ->  Seq Scan on tile_intersects_usa_2014 c2  (cost=0.00..0.00 rows=1 width=426) (actual time=0.000..0.000 rows=0 loops=28504)
                                  Filter: ((geolevel_id = 3) AND (zoomlevel = 9) AND (x = d.x) AND (y = d.y) AND ((areaid)::text = (d.areaid)::text))
                            ->  Index Scan using tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9_pk on tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9 c2_1  (cost=0.14..8.17 rows=1 width=426) (actual time=0.001..0.001 rows=0 loops=28504)
                                  Index Cond: (((areaid)::text = (d.areaid)::text) AND (x = d.x) AND (y = d.y))
                                  Filter: ((geolevel_id = 3) AND (zoomlevel = 9))
              CTE f
                ->  Nested Loop  (cost=0.00..8.46 rows=1 width=5264) (actual time=1317.260..6338.052 rows=12598 loops=1)
                      ->  CTE Scan on e  (cost=0.00..0.02 rows=1 width=462) (actual time=1316.612..1595.239 rows=28504 loops=1)
                      ->  Append  (cost=0.00..8.43 rows=2 width=2836) (actual time=0.163..0.166 rows=0 loops=28504)
                            ->  Seq Scan on geometry_usa_2014 e2  (cost=0.00..0.00 rows=1 width=450) (actual time=0.000..0.000 rows=0 loops=28504)
                                  Filter: ((zoomlevel = 9) AND (geolevel_id = 3) AND (e.bbox && geom) AND (e.bbox && geom) AND ((e.areaid)::text = (areaid)::text) AND _st_intersects(e.bbox, geom))
                            ->  Index Scan using geometry_usa_2014_geolevel_id_3_zoomlevel_9_geom_gix on geometry_usa_2014_geolevel_id_3_zoomlevel_9 e2_1  (cost=0.15..8.43 rows=1 width=5222) (actual time=0.159..0.162 rows=0 loops=28504)
                                  Index Cond: ((e.bbox && geom) AND (e.bbox && geom))
                                  Filter: ((zoomlevel = 9) AND (geolevel_id = 3) AND ((e.areaid)::text = (areaid)::text) AND _st_intersects(e.bbox, geom))
                                  Rows Removed by Filter: 6
              ->  CTE Scan on f  (cost=0.00..0.28 rows=1 width=498) (actual time=1317.767..25916.841 rows=12271 loops=1)
                    Filter: ((NOT (geom ~ bbox)) OR (NOT _st_contains(geom, bbox)))
                    Rows Removed by Filter: 327
Planning time: 4.038 ms
Trigger insert_tile_intersects_usa_2014_trigger: time=21062.474 calls=12271
Execution time: 48411.090 ms
CONTEXT:  PL/pgSQL function inline_code_block line 88 at assignment
psql:pg_USA_2014.sql:6039: INFO:  
Insert on tile_intersects_usa_2014  (cost=12075.42..12075.44 rows=1 width=531) (actual time=68.588..68.588 rows=0 loops=1)
  ->  Subquery Scan on "*SELECT*"  (cost=12075.42..12075.44 rows=1 width=531) (actual time=65.672..65.679 rows=5 loops=1)
        ->  Sort  (cost=12075.42..12075.43 rows=1 width=498) (actual time=65.671..65.672 rows=5 loops=1)
              Sort Key: g.geolevel_id, g.zoomlevel, g.areaid, g.x, g.y
              Sort Method: quicksort  Memory: 93kB
              CTE a
                ->  HashSetOp Except  (cost=1503.67..6504.28 rows=324 width=16) (actual time=45.467..45.536 rows=4 loops=1)
                      ->  Append  (cost=1503.67..6502.65 rows=326 width=16) (actual time=2.590..43.766 rows=6462 loops=1)
                            ->  Subquery Scan on "*SELECT* 1"  (cost=1503.67..1510.15 rows=324 width=13) (actual time=2.590..3.265 rows=3233 loops=1)
                                  ->  HashAggregate  (cost=1503.67..1506.91 rows=324 width=13) (actual time=2.589..2.999 rows=3233 loops=1)
                                        Group Key: geometry_usa_2014.geolevel_id, geometry_usa_2014.areaid
                                        ->  Append  (cost=0.00..1487.49 rows=3234 width=13) (actual time=0.012..1.696 rows=3233 loops=1)
                                              ->  Seq Scan on geometry_usa_2014  (cost=0.00..0.00 rows=1 width=422) (actual time=0.001..0.001 rows=0 loops=1)
                                                    Filter: ((geolevel_id = 3) AND (zoomlevel = 9))
                                              ->  Seq Scan on geometry_usa_2014_geolevel_id_3_zoomlevel_9  (cost=0.00..1487.49 rows=3233 width=13) (actual time=0.010..1.599 rows=3233 loops=1)
                                                    Filter: ((geolevel_id = 3) AND (zoomlevel = 9))
                            ->  Subquery Scan on "*SELECT* 2"  (cost=4992.47..4992.51 rows=2 width=422) (actual time=38.500..40.316 rows=3229 loops=1)
                                  ->  Unique  (cost=4992.47..4992.49 rows=2 width=422) (actual time=38.497..40.039 rows=3229 loops=1)
                                        ->  Sort  (cost=4992.47..4992.48 rows=2 width=422) (actual time=38.495..38.784 rows=12271 loops=1)
                                              Sort Key: a.areaid
                                              Sort Method: quicksort  Memory: 960kB
                                              ->  Append  (cost=0.00..4992.46 rows=2 width=422) (actual time=0.018..6.122 rows=12271 loops=1)
                                                    ->  Seq Scan on tile_intersects_usa_2014 a  (cost=0.00..0.00 rows=1 width=422) (actual time=0.000..0.000 rows=0 loops=1)
                                                          Filter: ((zoomlevel = 9) AND (geolevel_id = 3))
                                                    ->  Seq Scan on tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9 a_1  (cost=0.00..4992.46 rows=1 width=422) (actual time=0.017..5.751 rows=12271 loops=1)
                                                          Filter: ((zoomlevel = 9) AND (geolevel_id = 3))
              CTE b
                ->  Nested Loop  (cost=0.00..38.02 rows=22 width=7169) (actual time=52.074..61.650 rows=4 loops=1)
                      ->  CTE Scan on a a_2  (cost=0.00..7.29 rows=2 width=422) (actual time=45.478..45.551 rows=4 loops=1)
                            Filter: (geolevel_id = 3)
                      ->  Append  (cost=0.00..15.30 rows=4 width=143973) (actual time=4.015..4.016 rows=1 loops=4)
                            ->  Seq Scan on geometry_usa_2014 b  (cost=0.00..0.00 rows=1 width=450) (actual time=0.000..0.000 rows=0 loops=4)
                                  Filter: ((NOT st_isempty(geom)) AND (zoomlevel = 9) AND ((a_2.areaid)::text = (areaid)::text))
                            ->  Seq Scan on geometry_usa_2014_geolevel_id_1_zoomlevel_9 b_1  (cost=0.00..1.02 rows=1 width=486931) (actual time=0.409..0.409 rows=0 loops=4)
                                  Filter: ((NOT st_isempty(geom)) AND (zoomlevel = 9) AND ((a_2.areaid)::text = (areaid)::text))
                                  Rows Removed by Filter: 1
                            ->  Seq Scan on geometry_usa_2014_geolevel_id_2_zoomlevel_9 b_2  (cost=0.00..5.98 rows=1 width=83289) (actual time=3.550..3.550 rows=0 loops=4)
                                  Filter: ((NOT st_isempty(geom)) AND (zoomlevel = 9) AND ((a_2.areaid)::text = (areaid)::text))
                                  Rows Removed by Filter: 56
                            ->  Index Scan using geometry_usa_2014_geolevel_id_3_zoomlevel_9_pk on geometry_usa_2014_geolevel_id_3_zoomlevel_9 b_3  (cost=0.28..8.30 rows=1 width=5222) (actual time=0.023..0.023 rows=1 loops=4)
                                  Index Cond: ((areaid)::text = (a_2.areaid)::text)
                                  Filter: ((NOT st_isempty(geom)) AND (zoomlevel = 9))
              CTE c
                ->  CTE Scan on b b_4  (cost=0.00..3.08 rows=22 width=486) (actual time=52.119..61.770 rows=4 loops=1)
              CTE x
                ->  CTE Scan on c  (cost=0.00..110.39 rows=22000 width=434) (actual time=52.126..61.795 rows=5 loops=1)
              CTE y
                ->  CTE Scan on c c_1  (cost=0.00..110.39 rows=22000 width=434) (actual time=0.005..0.009 rows=4 loops=1)
              CTE d
                ->  Merge Join  (cost=4053.55..4527.40 rows=61 width=434) (actual time=61.899..61.970 rows=5 loops=1)
                      Merge Cond: ((x.zoomlevel = y.zoomlevel) AND (x.geolevel_id = y.geolevel_id) AND ((x.areaid)::text = (y.areaid)::text))
                      ->  Sort  (cost=2026.77..2081.77 rows=22000 width=430) (actual time=61.813..61.813 rows=5 loops=1)
                            Sort Key: x.zoomlevel, x.geolevel_id, x.areaid
                            Sort Method: quicksort  Memory: 25kB
                            ->  CTE Scan on x  (cost=0.00..440.00 rows=22000 width=430) (actual time=52.128..61.801 rows=5 loops=1)
                      ->  Sort  (cost=2026.77..2081.77 rows=22000 width=430) (actual time=0.023..0.023 rows=5 loops=1)
                            Sort Key: y.zoomlevel, y.geolevel_id, y.areaid
                            Sort Method: quicksort  Memory: 25kB
                            ->  CTE Scan on y  (cost=0.00..440.00 rows=22000 width=430) (actual time=0.007..0.012 rows=4 loops=1)
              CTE e
                ->  CTE Scan on d  (cost=0.00..1.37 rows=61 width=466) (actual time=61.905..61.982 rows=5 loops=1)
              CTE f
                ->  Unique  (cost=442.69..443.76 rows=61 width=466) (actual time=62.073..62.127 rows=5 loops=1)
                      ->  Sort  (cost=442.69..442.84 rows=61 width=466) (actual time=62.073..62.073 rows=5 loops=1)
                            Sort Key: e.zoomlevel, e.geolevel_id, e.areaid, e.x, e.y, e.bbox
                            Sort Method: quicksort  Memory: 26kB
                            ->  Nested Loop Anti Join  (cost=0.00..440.88 rows=61 width=466) (actual time=61.934..62.062 rows=5 loops=1)
                                  ->  CTE Scan on e  (cost=0.00..1.22 rows=61 width=466) (actual time=61.906..61.986 rows=5 loops=1)
                                  ->  Append  (cost=0.00..7.07 rows=2 width=426) (actual time=0.014..0.014 rows=0 loops=5)
                                        ->  Seq Scan on tile_intersects_usa_2014 c2  (cost=0.00..0.00 rows=1 width=426) (actual time=0.000..0.000 rows=0 loops=5)
                                              Filter: ((geolevel_id = 3) AND (zoomlevel = 9) AND (x = e.x) AND (y = e.y) AND ((areaid)::text = (e.areaid)::text))
                                        ->  Index Scan using tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9_pk on tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9 c2_1  (cost=0.29..7.07 rows=1 width=426) (actual time=0.006..0.006 rows=0 loops=5)
                                              Index Cond: (((areaid)::text = (e.areaid)::text) AND (x = e.x) AND (y = e.y))
                                              Filter: ((geolevel_id = 3) AND (zoomlevel = 9))
              CTE g
                ->  Nested Loop  (cost=0.00..336.45 rows=1 width=5268) (actual time=62.164..62.905 rows=5 loops=1)
                      ->  CTE Scan on f  (cost=0.00..1.22 rows=61 width=466) (actual time=62.074..62.132 rows=5 loops=1)
                      ->  Append  (cost=0.00..5.48 rows=2 width=2836) (actual time=0.152..0.153 rows=1 loops=5)
                            ->  Seq Scan on geometry_usa_2014 e2  (cost=0.00..0.00 rows=1 width=450) (actual time=0.000..0.000 rows=0 loops=5)
                                  Filter: ((zoomlevel = 9) AND (geolevel_id = 3) AND (f.bbox && geom) AND (f.bbox && geom) AND ((f.areaid)::text = (areaid)::text) AND _st_intersects(f.bbox, geom))
                            ->  Index Scan using geometry_usa_2014_geolevel_id_3_zoomlevel_9_pk on geometry_usa_2014_geolevel_id_3_zoomlevel_9 e2_1  (cost=0.28..5.48 rows=1 width=5222) (actual time=0.144..0.145 rows=1 loops=5)
                                  Index Cond: ((areaid)::text = (f.areaid)::text)
                                  Filter: ((zoomlevel = 9) AND (geolevel_id = 3) AND (f.bbox && geom) AND (f.bbox && geom) AND _st_intersects(f.bbox, geom))
              ->  CTE Scan on g  (cost=0.00..0.28 rows=1 width=498) (actual time=63.087..65.634 rows=5 loops=1)
Planning time: 5.889 ms
Trigger insert_tile_intersects_usa_2014_trigger: time=2.884 calls=5
Execution time: 68.886 ms
CONTEXT:  PL/pgSQL function inline_code_block line 93 at assignment
psql:pg_USA_2014.sql:6039: INFO:  Processed 12271+5 total areaid intersects for geolevel id 3/3 zoomlevel: 9/9 in 48.4+0.1s+0.0s, 579.3s total; 253.4 intesects/s
DO
Time: 579350.884 ms
-- SQL statement 332: Tile intersects table % savings >>>
/*
 * SQL statement name: 	tile_intersects_select2.sql
 * Type:				Postgres/PostGIS SQL
 * Parameters:
 *						1: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *
 * Description:			Select from tile intersects table
 
 geolevel_id | zoomlevel | areas | xmin | ymin | xmax | ymax | possible_tiles | tiles | pct_saving
-------------+-----------+-------+------+------+------+------+----------------+-------+------------
           1 |         0 |     1 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
           2 |         0 |    56 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
           2 |         1 |    56 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
           2 |         2 |    56 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
           2 |         3 |    56 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
           2 |         4 |    56 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
           2 |         5 |    56 |    0 |    6 |   31 |   17 |            384 |    48 |      87.50
           2 |         6 |    56 |    0 |   13 |   63 |   34 |           1408 |   111 |      92.12
           2 |         7 |    56 |    0 |   27 |  127 |   69 |           5504 |   281 |      94.89
           2 |         8 |    56 |    0 |   54 |  255 |  135 |          20992 |   665 |      96.83
           3 |         0 |  3233 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
           3 |         1 |  3233 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
           3 |         2 |  3233 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
           3 |         3 |  3233 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
           3 |         4 |  3233 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
           3 |         5 |  3233 |    0 |    6 |   31 |   17 |            384 |    49 |      87.24
           3 |         6 |  3233 |    0 |   13 |   63 |   34 |           1408 |   119 |      91.55
           3 |         7 |  3233 |    0 |   27 |  127 |   69 |           5504 |   333 |      93.95
           3 |         8 |  3233 |    0 |   54 |  255 |  138 |          21760 |   992 |      95.44
(19 rows)

 * Note:				% becomes % after substitution
 */
SELECT geolevel_id, zoomlevel, 
       COUNT(DISTINCT(areaid)) AS areas,
       MIN(x) AS xmin, MIN(y) AS ymin, 
       MAX(x) AS xmax, MAX(y) AS ymax, 
	   (MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1) AS possible_tiles,
       COUNT(DISTINCT(x::Text||y::Text)) AS tiles,
	   ROUND((((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1)-COUNT(DISTINCT(x::Text||y::Text)))::numeric/
			((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1))::numeric)*100, 2) AS pct_saving
  FROM tile_intersects_usa_2014
 GROUP BY geolevel_id, zoomlevel
 ORDER BY 1, 2;
 geolevel_id | zoomlevel | areas | xmin | ymin | xmax | ymax | possible_tiles | tiles | pct_saving 
-------------+-----------+-------+------+------+------+------+----------------+-------+------------
           1 |         0 |     1 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
           2 |         0 |    56 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
           2 |         1 |    56 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
           2 |         2 |    56 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
           2 |         3 |    56 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
           2 |         4 |    56 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
           2 |         5 |    56 |    0 |    6 |   31 |   17 |            384 |    47 |      87.76
           2 |         6 |    56 |    0 |   13 |   63 |   34 |           1408 |   111 |      92.12
           2 |         7 |    56 |    0 |   27 |  127 |   69 |           5504 |   281 |      94.89
           2 |         8 |    56 |    0 |   54 |  255 |  135 |          20992 |   665 |      96.83
           2 |         9 |    56 |    1 |  108 |  511 |  271 |          83804 |  1568 |      98.13
           3 |         0 |  3232 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
           3 |         1 |  3232 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
           3 |         2 |  3232 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
           3 |         3 |  3232 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
           3 |         4 |  3232 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
           3 |         5 |  3232 |    0 |    6 |   31 |   17 |            384 |    48 |      87.50
           3 |         6 |  3232 |    0 |   13 |   63 |   34 |           1408 |   118 |      91.62
           3 |         7 |  3233 |    0 |   27 |  127 |   69 |           5504 |   333 |      93.95
           3 |         8 |  3233 |    0 |   54 |  255 |  138 |          21760 |   992 |      95.44
           3 |         9 |  3233 |    1 |  108 |  511 |  276 |          86359 |  3137 |      96.37
(21 rows)

Time: 335.322 ms
-- SQL statement 333: Commit transaction >>>
END;
COMMIT
Time: 43.279 ms
--
-- Analyze tables
--
-- SQL statement 335: Describe table cb_2014_us_county_500k >>>
\dS+ cb_2014_us_county_500k;
                                                                                 Table "peter.cb_2014_us_county_500k"
         Column          |   Type   | Modifiers | Storage  | Stats target |                                                        Description                                                         
-------------------------+----------+-----------+----------+--------------+----------------------------------------------------------------------------------------------------------------------------
 statefp                 | text     |           | extended |              | Current state Federal Information Processing Series (FIPS) code
 countyfp                | text     |           | extended |              | Current county Federal Information Processing Series (FIPS) code
 countyns                | text     |           | extended |              | Current county Geographic Names Information System (GNIS) code
 affgeoid                | text     |           | extended |              | American FactFinder summary level code + geovariant code + '00US' + GEOID
 geoid                   | text     |           | extended |              | County identifier; a concatenation of current state Federal Information Processing Series (FIPS) code and county FIPS code
 name                    | text     |           | extended |              | Current county name
 lsad                    | text     |           | extended |              | Current legal/statistical area description code for county
 aland                   | text     |           | extended |              | Current land area (square meters)
 awater                  | text     |           | extended |              | Current water area (square meters)
 gid                     | integer  | not null  | plain    |              | Unique geographic index
 areaid                  | text     | not null  | extended |              | Area ID (COUNTYNS): Current county Geographic Names Information System (GNIS) code
 areaname                | text     | not null  | extended |              | Area name (NAME): Current county name
 area_km2                | numeric  |           | main     |              | Area in square km
 geographic_centroid_wkt | text     |           | extended |              | Wellknown text for geographic centroid
 wkt_9                   | text     |           | extended |              | Wellknown text for zoomlevel 9
 wkt_8                   | text     |           | extended |              | Wellknown text for zoomlevel 8
 wkt_7                   | text     |           | extended |              | Wellknown text for zoomlevel 7
 wkt_6                   | text     |           | extended |              | Wellknown text for zoomlevel 6
 geographic_centroid     | geometry |           | main     |              | 
 geom_orig               | geometry |           | main     |              | 
 geom_6                  | geometry |           | main     |              | 
 geom_7                  | geometry |           | main     |              | 
 geom_8                  | geometry |           | main     |              | 
 geom_9                  | geometry |           | main     |              | 
Indexes:
    "cb_2014_us_county_500k_pkey" PRIMARY KEY, btree (gid)
    "cb_2014_us_county_500k_uk" UNIQUE CONSTRAINT, btree (areaid)
    "cb_2014_us_county_500k_geom_6_gix" gist (geom_6)
    "cb_2014_us_county_500k_geom_7_gix" gist (geom_7)
    "cb_2014_us_county_500k_geom_8_gix" gist (geom_8)
    "cb_2014_us_county_500k_geom_9_gix" gist (geom_9)
    "cb_2014_us_county_500k_geom_orig_gix" gist (geom_orig)
Check constraints:
    "enforce_dims_geographic_centroid" CHECK (st_ndims(geographic_centroid) = 2)
    "enforce_dims_geom_6" CHECK (st_ndims(geom_6) = 2)
    "enforce_dims_geom_7" CHECK (st_ndims(geom_7) = 2)
    "enforce_dims_geom_8" CHECK (st_ndims(geom_8) = 2)
    "enforce_dims_geom_9" CHECK (st_ndims(geom_9) = 2)
    "enforce_dims_geom_orig" CHECK (st_ndims(geom_orig) = 2)
    "enforce_geotype_geographic_centroid" CHECK (geometrytype(geographic_centroid) = 'POINT'::text OR geographic_centroid IS NULL)
    "enforce_geotype_geom_6" CHECK (geometrytype(geom_6) = 'MULTIPOLYGON'::text OR geom_6 IS NULL)
    "enforce_geotype_geom_7" CHECK (geometrytype(geom_7) = 'MULTIPOLYGON'::text OR geom_7 IS NULL)
    "enforce_geotype_geom_8" CHECK (geometrytype(geom_8) = 'MULTIPOLYGON'::text OR geom_8 IS NULL)
    "enforce_geotype_geom_9" CHECK (geometrytype(geom_9) = 'MULTIPOLYGON'::text OR geom_9 IS NULL)
    "enforce_geotype_geom_orig" CHECK (geometrytype(geom_orig) = 'MULTIPOLYGON'::text OR geom_orig IS NULL)
    "enforce_srid_geographic_centroid" CHECK (st_srid(geographic_centroid) = 4326)
    "enforce_srid_geom_6" CHECK (st_srid(geom_6) = 4326)
    "enforce_srid_geom_7" CHECK (st_srid(geom_7) = 4326)
    "enforce_srid_geom_8" CHECK (st_srid(geom_8) = 4326)
    "enforce_srid_geom_9" CHECK (st_srid(geom_9) = 4326)
    "enforce_srid_geom_orig" CHECK (st_srid(geom_orig) = 4269)

-- SQL statement 336: Analyze table cb_2014_us_county_500k >>>
VACUUM ANALYZE cb_2014_us_county_500k;
VACUUM
Time: 1096.397 ms
-- SQL statement 337: Describe table lookup_cb_2014_us_nation_5m >>>
\dS+ lookup_cb_2014_us_nation_5m;
                                 Table "peter.lookup_cb_2014_us_nation_5m"
        Column        |          Type           | Modifiers | Storage  | Stats target |     Description     
----------------------+-------------------------+-----------+----------+--------------+---------------------
 cb_2014_us_nation_5m | character varying(100)  | not null  | extended |              | Area ID field
 areaname             | character varying(1000) |           | extended |              | Area Name field
 gid                  | integer                 | not null  | plain    |              | GID field
 geographic_centroid  | json                    |           | extended |              | Geographic centroid
Indexes:
    "lookup_cb_2014_us_nation_5m_pkey" PRIMARY KEY, btree (cb_2014_us_nation_5m)

-- SQL statement 338: Analyze table lookup_cb_2014_us_nation_5m >>>
VACUUM ANALYZE lookup_cb_2014_us_nation_5m;
VACUUM
Time: 17.787 ms
-- SQL statement 339: Describe table cb_2014_us_nation_5m >>>
\dS+ cb_2014_us_nation_5m;
                                                      Table "peter.cb_2014_us_nation_5m"
         Column          |   Type   | Modifiers | Storage  | Stats target |                            Description                            
-------------------------+----------+-----------+----------+--------------+-------------------------------------------------------------------
 affgeoid                | text     |           | extended |              | American FactFinder summary level code + geovariant code + '00US'
 geoid                   | text     |           | extended |              | Nation identifier
 name                    | text     |           | extended |              | Nation name
 gid                     | integer  | not null  | plain    |              | Unique geographic index
 areaid                  | text     | not null  | extended |              | Area ID (GEOID): Nation identifier
 areaname                | text     | not null  | extended |              | Area name (NAME): Nation name
 area_km2                | numeric  |           | main     |              | Area in square km
 geographic_centroid_wkt | text     |           | extended |              | Wellknown text for geographic centroid
 wkt_9                   | text     |           | extended |              | Wellknown text for zoomlevel 9
 wkt_8                   | text     |           | extended |              | Wellknown text for zoomlevel 8
 wkt_7                   | text     |           | extended |              | Wellknown text for zoomlevel 7
 wkt_6                   | text     |           | extended |              | Wellknown text for zoomlevel 6
 geographic_centroid     | geometry |           | main     |              | 
 geom_orig               | geometry |           | main     |              | 
 geom_6                  | geometry |           | main     |              | 
 geom_7                  | geometry |           | main     |              | 
 geom_8                  | geometry |           | main     |              | 
 geom_9                  | geometry |           | main     |              | 
Indexes:
    "cb_2014_us_nation_5m_pkey" PRIMARY KEY, btree (gid)
    "cb_2014_us_nation_5m_uk" UNIQUE CONSTRAINT, btree (areaid)
    "cb_2014_us_nation_5m_geom_6_gix" gist (geom_6)
    "cb_2014_us_nation_5m_geom_7_gix" gist (geom_7)
    "cb_2014_us_nation_5m_geom_8_gix" gist (geom_8)
    "cb_2014_us_nation_5m_geom_9_gix" gist (geom_9)
    "cb_2014_us_nation_5m_geom_orig_gix" gist (geom_orig)
Check constraints:
    "enforce_dims_geographic_centroid" CHECK (st_ndims(geographic_centroid) = 2)
    "enforce_dims_geom_6" CHECK (st_ndims(geom_6) = 2)
    "enforce_dims_geom_7" CHECK (st_ndims(geom_7) = 2)
    "enforce_dims_geom_8" CHECK (st_ndims(geom_8) = 2)
    "enforce_dims_geom_9" CHECK (st_ndims(geom_9) = 2)
    "enforce_dims_geom_orig" CHECK (st_ndims(geom_orig) = 2)
    "enforce_geotype_geographic_centroid" CHECK (geometrytype(geographic_centroid) = 'POINT'::text OR geographic_centroid IS NULL)
    "enforce_geotype_geom_6" CHECK (geometrytype(geom_6) = 'MULTIPOLYGON'::text OR geom_6 IS NULL)
    "enforce_geotype_geom_7" CHECK (geometrytype(geom_7) = 'MULTIPOLYGON'::text OR geom_7 IS NULL)
    "enforce_geotype_geom_8" CHECK (geometrytype(geom_8) = 'MULTIPOLYGON'::text OR geom_8 IS NULL)
    "enforce_geotype_geom_9" CHECK (geometrytype(geom_9) = 'MULTIPOLYGON'::text OR geom_9 IS NULL)
    "enforce_geotype_geom_orig" CHECK (geometrytype(geom_orig) = 'MULTIPOLYGON'::text OR geom_orig IS NULL)
    "enforce_srid_geographic_centroid" CHECK (st_srid(geographic_centroid) = 4326)
    "enforce_srid_geom_6" CHECK (st_srid(geom_6) = 4326)
    "enforce_srid_geom_7" CHECK (st_srid(geom_7) = 4326)
    "enforce_srid_geom_8" CHECK (st_srid(geom_8) = 4326)
    "enforce_srid_geom_9" CHECK (st_srid(geom_9) = 4326)
    "enforce_srid_geom_orig" CHECK (st_srid(geom_orig) = 4269)

-- SQL statement 340: Analyze table cb_2014_us_nation_5m >>>
VACUUM ANALYZE cb_2014_us_nation_5m;
VACUUM
Time: 53.199 ms
-- SQL statement 341: Describe table lookup_cb_2014_us_state_500k >>>
\dS+ lookup_cb_2014_us_state_500k;
                                 Table "peter.lookup_cb_2014_us_state_500k"
        Column         |          Type           | Modifiers | Storage  | Stats target |     Description     
-----------------------+-------------------------+-----------+----------+--------------+---------------------
 cb_2014_us_state_500k | character varying(100)  | not null  | extended |              | Area ID field
 areaname              | character varying(1000) |           | extended |              | Area Name field
 gid                   | integer                 | not null  | plain    |              | GID field
 geographic_centroid   | json                    |           | extended |              | Geographic centroid
Indexes:
    "lookup_cb_2014_us_state_500k_pkey" PRIMARY KEY, btree (cb_2014_us_state_500k)

-- SQL statement 342: Analyze table lookup_cb_2014_us_state_500k >>>
VACUUM ANALYZE lookup_cb_2014_us_state_500k;
VACUUM
Time: 5.597 ms
-- SQL statement 343: Describe table cb_2014_us_state_500k >>>
\dS+ cb_2014_us_state_500k;
                                                             Table "peter.cb_2014_us_state_500k"
         Column          |   Type   | Modifiers | Storage  | Stats target |                                   Description                                    
-------------------------+----------+-----------+----------+--------------+----------------------------------------------------------------------------------
 statefp                 | text     |           | extended |              | Current state Federal Information Processing Series (FIPS) code
 statens                 | text     |           | extended |              | Current state Geographic Names Information System (GNIS) code
 affgeoid                | text     |           | extended |              | American FactFinder summary level code + geovariant code + '00US' + GEOID
 geoid                   | text     |           | extended |              | State identifier; state FIPS code
 stusps                  | text     |           | extended |              | Current United States Postal Service state abbreviation
 name                    | text     |           | extended |              | Current State name
 lsad                    | text     |           | extended |              | Current legal/statistical area description code for state
 aland                   | text     |           | extended |              | Current land area (square meters)
 awater                  | text     |           | extended |              | Current water area (square meters)
 gid                     | integer  | not null  | plain    |              | Unique geographic index
 areaid                  | text     | not null  | extended |              | Area ID (STATENS): Current state Geographic Names Information System (GNIS) code
 areaname                | text     | not null  | extended |              | Area name (NAME): Current State name
 area_km2                | numeric  |           | main     |              | Area in square km
 geographic_centroid_wkt | text     |           | extended |              | Wellknown text for geographic centroid
 wkt_9                   | text     |           | extended |              | Wellknown text for zoomlevel 9
 wkt_8                   | text     |           | extended |              | Wellknown text for zoomlevel 8
 wkt_7                   | text     |           | extended |              | Wellknown text for zoomlevel 7
 wkt_6                   | text     |           | extended |              | Wellknown text for zoomlevel 6
 geographic_centroid     | geometry |           | main     |              | 
 geom_orig               | geometry |           | main     |              | 
 geom_6                  | geometry |           | main     |              | 
 geom_7                  | geometry |           | main     |              | 
 geom_8                  | geometry |           | main     |              | 
 geom_9                  | geometry |           | main     |              | 
Indexes:
    "cb_2014_us_state_500k_pkey" PRIMARY KEY, btree (gid)
    "cb_2014_us_state_500k_uk" UNIQUE CONSTRAINT, btree (areaid)
    "cb_2014_us_state_500k_geom_6_gix" gist (geom_6)
    "cb_2014_us_state_500k_geom_7_gix" gist (geom_7)
    "cb_2014_us_state_500k_geom_8_gix" gist (geom_8)
    "cb_2014_us_state_500k_geom_9_gix" gist (geom_9)
    "cb_2014_us_state_500k_geom_orig_gix" gist (geom_orig)
Check constraints:
    "enforce_dims_geographic_centroid" CHECK (st_ndims(geographic_centroid) = 2)
    "enforce_dims_geom_6" CHECK (st_ndims(geom_6) = 2)
    "enforce_dims_geom_7" CHECK (st_ndims(geom_7) = 2)
    "enforce_dims_geom_8" CHECK (st_ndims(geom_8) = 2)
    "enforce_dims_geom_9" CHECK (st_ndims(geom_9) = 2)
    "enforce_dims_geom_orig" CHECK (st_ndims(geom_orig) = 2)
    "enforce_geotype_geographic_centroid" CHECK (geometrytype(geographic_centroid) = 'POINT'::text OR geographic_centroid IS NULL)
    "enforce_geotype_geom_6" CHECK (geometrytype(geom_6) = 'MULTIPOLYGON'::text OR geom_6 IS NULL)
    "enforce_geotype_geom_7" CHECK (geometrytype(geom_7) = 'MULTIPOLYGON'::text OR geom_7 IS NULL)
    "enforce_geotype_geom_8" CHECK (geometrytype(geom_8) = 'MULTIPOLYGON'::text OR geom_8 IS NULL)
    "enforce_geotype_geom_9" CHECK (geometrytype(geom_9) = 'MULTIPOLYGON'::text OR geom_9 IS NULL)
    "enforce_geotype_geom_orig" CHECK (geometrytype(geom_orig) = 'MULTIPOLYGON'::text OR geom_orig IS NULL)
    "enforce_srid_geographic_centroid" CHECK (st_srid(geographic_centroid) = 4326)
    "enforce_srid_geom_6" CHECK (st_srid(geom_6) = 4326)
    "enforce_srid_geom_7" CHECK (st_srid(geom_7) = 4326)
    "enforce_srid_geom_8" CHECK (st_srid(geom_8) = 4326)
    "enforce_srid_geom_9" CHECK (st_srid(geom_9) = 4326)
    "enforce_srid_geom_orig" CHECK (st_srid(geom_orig) = 4269)

-- SQL statement 344: Analyze table cb_2014_us_state_500k >>>
VACUUM ANALYZE cb_2014_us_state_500k;
VACUUM
Time: 210.629 ms
-- SQL statement 345: Describe table lookup_cb_2014_us_county_500k >>>
\dS+ lookup_cb_2014_us_county_500k;
                                 Table "peter.lookup_cb_2014_us_county_500k"
         Column         |          Type           | Modifiers | Storage  | Stats target |     Description     
------------------------+-------------------------+-----------+----------+--------------+---------------------
 cb_2014_us_county_500k | character varying(100)  | not null  | extended |              | Area ID field
 areaname               | character varying(1000) |           | extended |              | Area Name field
 gid                    | integer                 | not null  | plain    |              | GID field
 geographic_centroid    | json                    |           | extended |              | Geographic centroid
Indexes:
    "lookup_cb_2014_us_county_500k_pkey" PRIMARY KEY, btree (cb_2014_us_county_500k)

-- SQL statement 346: Analyze table lookup_cb_2014_us_county_500k >>>
VACUUM ANALYZE lookup_cb_2014_us_county_500k;
VACUUM
Time: 19.518 ms
-- SQL statement 347: Describe table geolevels_usa_2014 >>>
\dS+ geolevels_usa_2014;
                                                                                    Table "peter.geolevels_usa_2014"
          Column          |          Type          | Modifiers | Storage  | Stats target |                                                 Description                                                  
--------------------------+------------------------+-----------+----------+--------------+--------------------------------------------------------------------------------------------------------------
 geography                | character varying(50)  | not null  | extended |              | Geography (e.g EW2001)
 geolevel_name            | character varying(30)  | not null  | extended |              | Name of geolevel. This will be a column name in the numerator/denominator tables
 geolevel_id              | integer                | not null  | plain    |              | ID for ordering (1=lowest resolution). Up to 99 supported.
 description              | character varying(250) | not null  | extended |              | Description
 lookup_table             | character varying(30)  | not null  | extended |              | Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is "Westminster"
 lookup_desc_column       | character varying(30)  | not null  | extended |              | Lookup table description column name.
 shapefile                | character varying(512) | not null  | extended |              | Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead
 shapefile_table          | character varying(30)  |           | extended |              | Table containing GIS shape file data.
 shapefile_area_id_column | character varying(30)  | not null  | extended |              | Column containing the AREA_IDs in SHAPEFILE_TABLE
 shapefile_desc_column    | character varying(30)  |           | extended |              | Column containing the AREA_ID descriptions in SHAPEFILE_TABLE
 centroids_table          | character varying(30)  |           | extended |              | Centroids table
 centroids_area_id_column | character varying(30)  |           | extended |              | Centroids area id column
 covariate_table          | character varying(30)  |           | extended |              | Covariate table
 restricted               | integer                | default 0 | plain    |              | 
 resolution               | integer                |           | plain    |              | Can use a map for selection at this resolution (0/1)
 comparea                 | integer                |           | plain    |              | Able to be used as a comparison area (0/1)
 listing                  | integer                |           | plain    |              | Able to be used in a disease map listing (0/1)
 areaid_count             | integer                |           | plain    |              | Total number of area IDs within the geolevel
Indexes:
    "geolevels_usa_2014_pk" PRIMARY KEY, btree (geography, geolevel_name)
Check constraints:
    "geolevels_usa_2014_comparea_ck" CHECK (comparea = ANY (ARRAY[0, 1]))
    "geolevels_usa_2014_listing_ck" CHECK (listing = ANY (ARRAY[0, 1]))
    "geolevels_usa_2014_resolution_ck" CHECK (resolution = ANY (ARRAY[0, 1]))
    "geolevels_usa_2014_restricted_ck" CHECK (restricted = ANY (ARRAY[0, 1]))
Foreign-key constraints:
    "geolevels_usa_2014_fk" FOREIGN KEY (geography) REFERENCES geography_usa_2014(geography)

-- SQL statement 348: Analyze table geolevels_usa_2014 >>>
VACUUM ANALYZE geolevels_usa_2014;
VACUUM
Time: 9.721 ms
-- SQL statement 349: Describe table geography_usa_2014 >>>
\dS+ geography_usa_2014;
                                                            Table "peter.geography_usa_2014"
         Column          |          Type          |      Modifiers      | Storage  | Stats target |                     Description                     
-------------------------+------------------------+---------------------+----------+--------------+-----------------------------------------------------
 geography               | character varying(50)  | not null            | extended |              | Geography name
 description             | character varying(250) | not null            | extended |              | Description
 hierarchytable          | character varying(30)  | not null            | extended |              | Hierarchy table
 geometrytable           | character varying(30)  | not null            | extended |              | Geometry table
 tiletable               | character varying(30)  | not null            | extended |              | Tile table
 adjacencytable          | character varying(30)  | not null            | extended |              | Adjacency table
 srid                    | integer                | not null default 0  | plain    |              | Projection SRID
 defaultcomparea         | character varying(30)  |                     | extended |              | Default comparison area: lowest resolution geolevel
 defaultstudyarea        | character varying(30)  |                     | extended |              | Default study area: highest resolution geolevel
 minzoomlevel            | integer                | not null default 6  | plain    |              | Min zoomlevel
 maxzoomlevel            | integer                | not null default 11 | plain    |              | Max zoomlevel
 postal_population_table | character varying(30)  |                     | extended |              | Postal_population_table
 postal_point_column     | character varying(30)  |                     | extended |              | Postal_point_column
 partition               | integer                | not null default 0  | plain    |              | Partition geometry and tile tables (0/1)
 max_geojson_digits      | integer                | not null default 8  | plain    |              | Maximum digits in geojson (topojson quantisation)
Indexes:
    "geography_usa_2014_pk" PRIMARY KEY, btree (geography)
Check constraints:
    "geography_usa_2014_part_ck" CHECK (partition = ANY (ARRAY[0, 1]))
    "geography_usa_2014_ppt_ck" CHECK (postal_population_table IS NOT NULL AND postal_point_column IS NOT NULL OR postal_population_table IS NULL AND postal_point_column IS NULL)
Referenced by:
    TABLE "geolevels_usa_2014" CONSTRAINT "geolevels_usa_2014_fk" FOREIGN KEY (geography) REFERENCES geography_usa_2014(geography)

-- SQL statement 350: Analyze table geography_usa_2014 >>>
VACUUM ANALYZE geography_usa_2014;
VACUUM
Time: 6.095 ms
-- SQL statement 351: Describe table hierarchy_usa_2014 >>>
\dS+ hierarchy_usa_2014;
                                                         Table "peter.hierarchy_usa_2014"
         Column         |          Type          | Modifiers | Storage  | Stats target |                        Description                        
------------------------+------------------------+-----------+----------+--------------+-----------------------------------------------------------
 cb_2014_us_county_500k | character varying(100) | not null  | extended |              | Hierarchy lookup for The County at a scale of 1:500,000
 cb_2014_us_nation_5m   | character varying(100) | not null  | extended |              | Hierarchy lookup for The nation at a scale of 1:5,000,000
 cb_2014_us_state_500k  | character varying(100) | not null  | extended |              | Hierarchy lookup for The State at a scale of 1:500,000
Indexes:
    "hierarchy_usa_2014_pkey" PRIMARY KEY, btree (cb_2014_us_county_500k)
    "hierarchy_usa_2014_cb_2014_us_state_500k" btree (cb_2014_us_state_500k)

-- SQL statement 352: Analyze table hierarchy_usa_2014 >>>
VACUUM ANALYZE hierarchy_usa_2014;
VACUUM
Time: 17.612 ms
-- SQL statement 353: Describe table geometry_usa_2014 >>>
\dS+ geometry_usa_2014;
                                                                                           Table "peter.geometry_usa_2014"
   Column    |          Type          | Modifiers | Storage  | Stats target |                                                               Description                                                               
-------------+------------------------+-----------+----------+--------------+-----------------------------------------------------------------------------------------------------------------------------------------
 geolevel_id | integer                | not null  | plain    |              | ID for ordering (1=lowest resolution). Up to 99 supported.
 areaid      | character varying(200) | not null  | extended |              | Area ID.
 zoomlevel   | integer                | not null  | plain    |              | Zoom level: 0 to maxoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11
 geom        | geometry               |           | main     |              | Geometry data in SRID 4326 (WGS84).
Check constraints:
    "enforce_dims_geom" CHECK (st_ndims(geom) = 2)
    "enforce_geotype_geom" CHECK (geometrytype(geom) = 'MULTIPOLYGON'::text OR geom IS NULL)
    "enforce_srid_geom" CHECK (st_srid(geom) = 4326)
Triggers:
    insert_geometry_usa_2014_trigger BEFORE INSERT ON geometry_usa_2014 FOR EACH ROW EXECUTE PROCEDURE geometry_usa_2014_insert_trigger()
Child tables: geometry_usa_2014_geolevel_id_1_zoomlevel_6,
              geometry_usa_2014_geolevel_id_1_zoomlevel_7,
              geometry_usa_2014_geolevel_id_1_zoomlevel_8,
              geometry_usa_2014_geolevel_id_1_zoomlevel_9,
              geometry_usa_2014_geolevel_id_2_zoomlevel_6,
              geometry_usa_2014_geolevel_id_2_zoomlevel_7,
              geometry_usa_2014_geolevel_id_2_zoomlevel_8,
              geometry_usa_2014_geolevel_id_2_zoomlevel_9,
              geometry_usa_2014_geolevel_id_3_zoomlevel_6,
              geometry_usa_2014_geolevel_id_3_zoomlevel_7,
              geometry_usa_2014_geolevel_id_3_zoomlevel_8,
              geometry_usa_2014_geolevel_id_3_zoomlevel_9

-- SQL statement 354: Analyze table geometry_usa_2014 >>>
VACUUM ANALYZE geometry_usa_2014;
VACUUM
Time: 193.178 ms
-- SQL statement 355: Describe table tile_intersects_usa_2014 >>>
\dS+ tile_intersects_usa_2014;
                                                                                    Table "peter.tile_intersects_usa_2014"
      Column       |          Type          | Modifiers | Storage  | Stats target |                                                        Description                                                        
-------------------+------------------------+-----------+----------+--------------+---------------------------------------------------------------------------------------------------------------------------
 geolevel_id       | integer                | not null  | plain    |              | ID for ordering (1=lowest resolution). Up to 99 supported.
 zoomlevel         | integer                | not null  | plain    |              | Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11
 areaid            | character varying(200) | not null  | extended |              | Area ID
 x                 | integer                | not null  | plain    |              | X tile number. From 0 to (2**<zoomlevel>)-1
 y                 | integer                | not null  | plain    |              | Y tile number. From 0 to (2**<zoomlevel>)-1
 optimised_geojson | json                   |           | extended |              | Tile areaid intersect multipolygon in GeoJSON format, optimised for zoomlevel N.
 within            | boolean                | not null  | plain    |              | Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.
 bbox              | geometry               |           | main     |              | Bounding box of tile as a polygon.
 geom              | geometry               |           | main     |              | Geometry of area.
Indexes:
    "tile_intersects_usa_2014_pkey" PRIMARY KEY, btree (geolevel_id, zoomlevel, areaid, x, y)
Check constraints:
    "enforce_dims_bbox" CHECK (st_ndims(bbox) = 2)
    "enforce_dims_geom" CHECK (st_ndims(geom) = 2)
    "enforce_geotype_bbox" CHECK (geometrytype(bbox) = 'POLYGON'::text OR bbox IS NULL)
    "enforce_geotype_geom" CHECK (geometrytype(geom) = 'MULTIPOLYGON'::text OR geom IS NULL)
    "enforce_srid_bbox" CHECK (st_srid(bbox) = 4326)
    "enforce_srid_geom" CHECK (st_srid(geom) = 4326)
Triggers:
    insert_tile_intersects_usa_2014_trigger BEFORE INSERT ON tile_intersects_usa_2014 FOR EACH ROW EXECUTE PROCEDURE tile_intersects_usa_2014_insert_trigger()
Child tables: tile_intersects_usa_2014_geolevel_id_1_zoomlevel_0,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_0,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_1,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_2,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_3,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_4,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_5,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_6,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_7,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_8,
              tile_intersects_usa_2014_geolevel_id_2_zoomlevel_9,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_0,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_1,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_2,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_3,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_4,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_5,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_6,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_7,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_8,
              tile_intersects_usa_2014_geolevel_id_3_zoomlevel_9

-- SQL statement 356: Analyze table tile_intersects_usa_2014 >>>
VACUUM ANALYZE tile_intersects_usa_2014;
VACUUM
Time: 2504.894 ms
-- SQL statement 357: Describe table tile_limits_usa_2014 >>>
\dS+ tile_limits_usa_2014;
                                                                                  Table "peter.tile_limits_usa_2014"
  Column   |       Type       | Modifiers | Storage | Stats target |                                                           Description                                                            
-----------+------------------+-----------+---------+--------------+----------------------------------------------------------------------------------------------------------------------------------
 zoomlevel | integer          | not null  | plain   |              | Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at max zooomlevel (11)
 x_min     | double precision |           | plain   |              | Min X (longitude)
 x_max     | double precision |           | plain   |              | Max X (longitude)
 y_min     | double precision |           | plain   |              | Min Y (latitude)
 y_max     | double precision |           | plain   |              | Max Y (latitude)
 y_mintile | integer          |           | plain   |              | Min Y tile number (latitude)
 y_maxtile | integer          |           | plain   |              | Max Y tile number (latitude)
 x_mintile | integer          |           | plain   |              | Min X tile number (longitude)
 x_maxtile | integer          |           | plain   |              | Max X tile number (longitude)
 bbox      | geometry         |           | main    |              | Bounding box polygon for geolevel_id 1 area
Indexes:
    "tile_limits_usa_2014_pkey" PRIMARY KEY, btree (zoomlevel)

-- SQL statement 358: Analyze table tile_limits_usa_2014 >>>
VACUUM ANALYZE tile_limits_usa_2014;
VACUUM
Time: 16.075 ms
-- SQL statement 359: Describe table t_tiles_usa_2014 >>>
\dS+ t_tiles_usa_2014;
                                                                                        Table "peter.t_tiles_usa_2014"
       Column       |          Type          | Modifiers | Storage  | Stats target |                                                        Description                                                        
--------------------+------------------------+-----------+----------+--------------+---------------------------------------------------------------------------------------------------------------------------
 geolevel_id        | integer                | not null  | plain    |              | ID for ordering (1=lowest resolution). Up to 99 supported.
 zoomlevel          | integer                | not null  | plain    |              | Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11
 x                  | integer                | not null  | plain    |              | X tile number. From 0 to (2**<zoomlevel>)-1
 y                  | integer                | not null  | plain    |              | Y tile number. From 0 to (2**<zoomlevel>)-1
 optimised_topojson | json                   |           | extended |              | Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.
 tile_id            | character varying(200) | not null  | extended |              | Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>
 areaid_count       | integer                | not null  | plain    |              | Total number of areaIDs (geoJSON features)
Indexes:
    "t_tiles_usa_2014_pkey" PRIMARY KEY, btree (tile_id)
    "t_tiles_usa_2014_areaid_count" btree (areaid_count)
    "t_tiles_usa_2014_x_tile" btree (geolevel_id, zoomlevel, x)
    "t_tiles_usa_2014_xy_tile" btree (geolevel_id, zoomlevel, x, y)
    "t_tiles_usa_2014_y_tile" btree (geolevel_id, zoomlevel, x)

-- SQL statement 360: Analyze table t_tiles_usa_2014 >>>
VACUUM ANALYZE t_tiles_usa_2014;
VACUUM
Time: 3.658 ms
--
-- Check areas
--
-- SQL statement 362: Test Turf and DB areas agree to within 1% (Postgres)/5% (SQL server) >>>
DO LANGUAGE plpgsql $$
DECLARE 
/*
 * SQL statement name: 	area_check.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Check Turf araa (area_km2) compared to SQL Server calculated area (area_km2_calc)
 *						Allow for 1% error
 *						Ignore small areas <= 10 km2
 * Note:				%% becomes % after substitution
 */
	c1 CURSOR FOR
		WITH a AS (
			SELECT areaname,
				   area_km2 AS area_km2,
				   ST_Area(geography(geom_9))/(1000*1000) AS area_km2_calc
			  FROM cb_2014_us_county_500k
		), b AS (
		SELECT a.areaname,
			   a.area_km2,
			   a.area_km2_calc,
			   CASE WHEN a.area_km2 > 0 THEN 100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2)
					WHEN a.area_km2 = a.area_km2_calc THEN 0
					ELSE NULL
			   END AS pct_km2_diff 
		  FROM a
		), c AS (
			SELECT COUNT(areaname) AS total_areas
			  FROM a
		), d AS (
			SELECT b.areaname, b.area_km2, b.area_km2_calc, b.pct_km2_diff
			  FROM b, c
			 WHERE b.pct_km2_diff > 1 /* Allow for 1% error */
			   AND b.area_km2_calc > 10 /* Ignore small areas <= 10 km2 */
		), e AS (
			SELECT COUNT(areaname) AS total_areas_in_error
			  FROM d
		)
		SELECT d.*, c.total_areas, e.total_areas_in_error, 
		       ROUND((100*e.total_areas_in_error::NUMERIC/c.total_areas::NUMERIC), 2) AS pct_in_error
		  FROM d, c, e;

	c1_rec RECORD;
	total INTEGER:=0;
	pct_in_error NUMERIC:=0;
BEGIN
	FOR c1_rec IN c1 LOOP
		IF total = 0 THEN
			RAISE WARNING '% areas in error of %, % pct', c1_rec.total_areas_in_error, c1_rec.total_areas, c1_rec.pct_in_error;
			pct_in_error:=c1_rec.pct_in_error;
		END IF;
		total:=total+1;
		RAISE WARNING 'Area: %, area km2: %:, calc: %, diff %',
			c1_rec.areaname, c1_rec.area_km2, c1_rec.area_km2_calc, c1_rec.pct_km2_diff;
	END LOOP;
	IF total = 0 THEN
		RAISE INFO 'Table: cb_2014_us_county_500k no invalid areas check OK';
	ELSIF pct_in_error < 10 THEN
		RAISE WARNING 'Table: cb_2014_us_county_500k no invalid areas check WARNING: % invalid (<10 pct)', pct_in_error;
	ELSE
		RAISE EXCEPTION 'Table: cb_2014_us_county_500k no invalid areas check FAILED: % invalid', total;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:6243: INFO:  Table: cb_2014_us_county_500k no invalid areas check OK
DO
Time: 1663.849 ms
-- SQL statement 363: Test Turf and DB areas agree to within 1% (Postgres)/5% (SQL server) >>>
DO LANGUAGE plpgsql $$
DECLARE 
/*
 * SQL statement name: 	area_check.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Check Turf araa (area_km2) compared to SQL Server calculated area (area_km2_calc)
 *						Allow for 1% error
 *						Ignore small areas <= 10 km2
 * Note:				%% becomes % after substitution
 */
	c1 CURSOR FOR
		WITH a AS (
			SELECT areaname,
				   area_km2 AS area_km2,
				   ST_Area(geography(geom_9))/(1000*1000) AS area_km2_calc
			  FROM cb_2014_us_nation_5m
		), b AS (
		SELECT a.areaname,
			   a.area_km2,
			   a.area_km2_calc,
			   CASE WHEN a.area_km2 > 0 THEN 100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2)
					WHEN a.area_km2 = a.area_km2_calc THEN 0
					ELSE NULL
			   END AS pct_km2_diff 
		  FROM a
		), c AS (
			SELECT COUNT(areaname) AS total_areas
			  FROM a
		), d AS (
			SELECT b.areaname, b.area_km2, b.area_km2_calc, b.pct_km2_diff
			  FROM b, c
			 WHERE b.pct_km2_diff > 1 /* Allow for 1% error */
			   AND b.area_km2_calc > 10 /* Ignore small areas <= 10 km2 */
		), e AS (
			SELECT COUNT(areaname) AS total_areas_in_error
			  FROM d
		)
		SELECT d.*, c.total_areas, e.total_areas_in_error, 
		       ROUND((100*e.total_areas_in_error::NUMERIC/c.total_areas::NUMERIC), 2) AS pct_in_error
		  FROM d, c, e;

	c1_rec RECORD;
	total INTEGER:=0;
	pct_in_error NUMERIC:=0;
BEGIN
	FOR c1_rec IN c1 LOOP
		IF total = 0 THEN
			RAISE WARNING '% areas in error of %, % pct', c1_rec.total_areas_in_error, c1_rec.total_areas, c1_rec.pct_in_error;
			pct_in_error:=c1_rec.pct_in_error;
		END IF;
		total:=total+1;
		RAISE WARNING 'Area: %, area km2: %:, calc: %, diff %',
			c1_rec.areaname, c1_rec.area_km2, c1_rec.area_km2_calc, c1_rec.pct_km2_diff;
	END LOOP;
	IF total = 0 THEN
		RAISE INFO 'Table: cb_2014_us_nation_5m no invalid areas check OK';
	ELSIF pct_in_error < 10 THEN
		RAISE WARNING 'Table: cb_2014_us_nation_5m no invalid areas check WARNING: % invalid (<10 pct)', pct_in_error;
	ELSE
		RAISE EXCEPTION 'Table: cb_2014_us_nation_5m no invalid areas check FAILED: % invalid', total;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:6312: INFO:  Table: cb_2014_us_nation_5m no invalid areas check OK
DO
Time: 55.589 ms
-- SQL statement 364: Test Turf and DB areas agree to within 1% (Postgres)/5% (SQL server) >>>
DO LANGUAGE plpgsql $$
DECLARE 
/*
 * SQL statement name: 	area_check.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Check Turf araa (area_km2) compared to SQL Server calculated area (area_km2_calc)
 *						Allow for 1% error
 *						Ignore small areas <= 10 km2
 * Note:				%% becomes % after substitution
 */
	c1 CURSOR FOR
		WITH a AS (
			SELECT areaname,
				   area_km2 AS area_km2,
				   ST_Area(geography(geom_9))/(1000*1000) AS area_km2_calc
			  FROM cb_2014_us_state_500k
		), b AS (
		SELECT a.areaname,
			   a.area_km2,
			   a.area_km2_calc,
			   CASE WHEN a.area_km2 > 0 THEN 100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2)
					WHEN a.area_km2 = a.area_km2_calc THEN 0
					ELSE NULL
			   END AS pct_km2_diff 
		  FROM a
		), c AS (
			SELECT COUNT(areaname) AS total_areas
			  FROM a
		), d AS (
			SELECT b.areaname, b.area_km2, b.area_km2_calc, b.pct_km2_diff
			  FROM b, c
			 WHERE b.pct_km2_diff > 1 /* Allow for 1% error */
			   AND b.area_km2_calc > 10 /* Ignore small areas <= 10 km2 */
		), e AS (
			SELECT COUNT(areaname) AS total_areas_in_error
			  FROM d
		)
		SELECT d.*, c.total_areas, e.total_areas_in_error, 
		       ROUND((100*e.total_areas_in_error::NUMERIC/c.total_areas::NUMERIC), 2) AS pct_in_error
		  FROM d, c, e;

	c1_rec RECORD;
	total INTEGER:=0;
	pct_in_error NUMERIC:=0;
BEGIN
	FOR c1_rec IN c1 LOOP
		IF total = 0 THEN
			RAISE WARNING '% areas in error of %, % pct', c1_rec.total_areas_in_error, c1_rec.total_areas, c1_rec.pct_in_error;
			pct_in_error:=c1_rec.pct_in_error;
		END IF;
		total:=total+1;
		RAISE WARNING 'Area: %, area km2: %:, calc: %, diff %',
			c1_rec.areaname, c1_rec.area_km2, c1_rec.area_km2_calc, c1_rec.pct_km2_diff;
	END LOOP;
	IF total = 0 THEN
		RAISE INFO 'Table: cb_2014_us_state_500k no invalid areas check OK';
	ELSIF pct_in_error < 10 THEN
		RAISE WARNING 'Table: cb_2014_us_state_500k no invalid areas check WARNING: % invalid (<10 pct)', pct_in_error;
	ELSE
		RAISE EXCEPTION 'Table: cb_2014_us_state_500k no invalid areas check FAILED: % invalid', total;
	END IF;
END;
$$;
psql:pg_USA_2014.sql:6381: INFO:  Table: cb_2014_us_state_500k no invalid areas check OK
DO
Time: 433.550 ms
--
-- EOF
```