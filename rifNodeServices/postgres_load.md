Postgres production data load log
=================================

```SQL
You are connected to database "sahsuland_dev" as user "rif40" on host "localhost" at port "5432".
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
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  rif40_log_setup() DEFAULTED send DEBUG to INFO: off; debug function list: []
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.03s  rif40_startup(): search_path not set for: rif40
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.03s  rif40_startup(): SQL> DROP FUNCTION IF EXISTS rif40.rif40_run_study(INTEGER, INTEGER);
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: NOTICE:  function rif40.rif40_run_study(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.14s  rif40_startup(): Created temporary table: g_rif40_study_areas
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.16s  rif40_startup(): Created temporary table: g_rif40_comparison_areas
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.21s  rif40_startup(): PostGIS extension V2.3.5 (POSTGIS="2.3.5 r16110" GEOS="3.6.2-CAPI-1.10.2 4d2925d" PROJ="Rel. 4.9.3, 15 August 2016" GDAL="GDAL 2.2.2, released 2017/09/15" LIBXML="2.7.8" LIBJSON="0.12" RASTER)
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.22s  rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.23s  rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.23s  rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: rif40
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.23s  rif40_startup(): search_path: public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.23s  rif40_startup(): Deleted 0, created 2 tables/views/foreign data wrapper tables
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  SQL> SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
Pager usage is off.
\set ON_ERROR_STOP ON
\timing
Timing is on.
-- SQL statement 0: Start transaction >>>
BEGIN TRANSACTION;
BEGIN
Time: 0.618 ms
-- SQL statement 1: RIF initialisation >>>
/*
 * SQL statement name: 	rif_startup.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:			None
 *
 * Description:			Run RIF startup script (geoDataLoader version)
 * Note:				% becomes % after substitution
 */
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
		RAISE EXCEPTION 'RIF startup(geoDataLoader): not a RIF database';
	END IF;
--
-- Set a default path and schema for user
--
	IF current_user = 'rif40' THEN
		sql_stmt:='SET SESSION search_path TO rif_data /* default schema */, rif40, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
	ELSE
		RAISE EXCEPTION 'RIF startup(geoDataLoader): RIF user: % is not rif40', current_user;
	END IF;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
psql:rif_pg_usa_2014.sql:118: INFO:  rif40_log_setup() send DEBUG to INFO: off; debug function list: []
psql:rif_pg_usa_2014.sql:118: INFO:  +00000.01s  rif40_startup(): search_path not set for: rif40
psql:rif_pg_usa_2014.sql:118: INFO:  +00000.01s  rif40_startup(): SQL> DROP FUNCTION IF EXISTS rif40.rif40_run_study(INTEGER, INTEGER);
psql:rif_pg_usa_2014.sql:118: NOTICE:  function rif40.rif40_run_study(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
psql:rif_pg_usa_2014.sql:118: INFO:  +00000.02s  rif40_startup(): Temporary table: g_rif40_comparison_areas exists
psql:rif_pg_usa_2014.sql:118: INFO:  +00000.04s  rif40_startup(): PostGIS extension V2.3.5 (POSTGIS="2.3.5 r16110" GEOS="3.6.2-CAPI-1.10.2 4d2925d" PROJ="Rel. 4.9.3, 15 August 2016" GDAL="GDAL 2.2.2, released 2017/09/15" LIBXML="2.7.8" LIBJSON="0.12" RASTER)
psql:rif_pg_usa_2014.sql:118: INFO:  +00000.04s  rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
psql:rif_pg_usa_2014.sql:118: INFO:  +00000.04s  rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
psql:rif_pg_usa_2014.sql:118: INFO:  +00000.04s  rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: rif40
psql:rif_pg_usa_2014.sql:118: INFO:  +00000.04s  rif40_startup(): search_path: public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
psql:rif_pg_usa_2014.sql:118: INFO:  SQL> SET SESSION search_path TO rif_data /* default schema */, rif40, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
Time: 41.788 ms
--
-- Eof;
-- SQL statement 2: Check if geography is in use in studies. Raise error if it is. >>>
DO LANGUAGE plpgsql $$
/*
 * SQL statement name: 	in_use_check.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Geography; e.g. SAHSULAND
 *
 * Description:			Check if geography is in use in studies. Raise error if it is. 
 *						To prevent accidental replacement
 * Note:				% becomes % after substitution
 */
DECLARE
	c1 CURSOR FOR
		SELECT COUNT(DISTINCT(a.study_id)) AS total
		  FROM t_rif40_studies a
		 WHERE a.geography  = 'USA_2014';
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total = 0 THEN
		RAISE INFO 'Geography: USA_2014 is not used by any studies';
	ELSE
		RAISE EXCEPTION 'Geography: USA_2014 is used by: % studies', c1_rec.total;
	END IF;
END;
$$;
psql:rif_pg_usa_2014.sql:151: INFO:  Geography: USA_2014 is not used by any studies
DO
Time: 10.022 ms
--
-- Create Geolevels lookup tables
--
-- SQL statement 4: Drop table lookup_cb_2014_us_nation_5m >>>
DROP TABLE IF EXISTS lookup_cb_2014_us_nation_5m;
DROP TABLE
Time: 12.363 ms
-- SQL statement 5: Create table lookup_cb_2014_us_nation_5m >>>
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
CREATE TABLE rif_data.lookup_cb_2014_us_nation_5m (
	cb_2014_us_nation_5m			VARCHAR(100)  NOT NULL,
	areaname	VARCHAR(1000),
	gid			INTEGER		  NOT NULL,
	geographic_centroid		JSON,
	PRIMARY KEY (cb_2014_us_nation_5m)
);
CREATE TABLE
Time: 38.825 ms
-- SQL statement 6: Comment table lookup_cb_2014_us_nation_5m >>>
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
Time: 0.335 ms
-- SQL statement 7: Comment lookup_cb_2014_us_nation_5m columns >>>
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
Time: 0.442 ms
-- SQL statement 8: Comment lookup_cb_2014_us_nation_5m columns >>>
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
Time: 0.336 ms
-- SQL statement 9: Comment lookup_cb_2014_us_nation_5m columns >>>
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
Time: 0.448 ms
-- SQL statement 10: Comment lookup_cb_2014_us_nation_5m columns >>>
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
Time: 0.523 ms
-- SQL statement 11: Drop table lookup_cb_2014_us_state_500k >>>
DROP TABLE IF EXISTS lookup_cb_2014_us_state_500k;
DROP TABLE
Time: 1.773 ms
-- SQL statement 12: Create table lookup_cb_2014_us_state_500k >>>
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
CREATE TABLE rif_data.lookup_cb_2014_us_state_500k (
	cb_2014_us_state_500k			VARCHAR(100)  NOT NULL,
	areaname	VARCHAR(1000),
	gid			INTEGER		  NOT NULL,
	geographic_centroid		JSON,
	PRIMARY KEY (cb_2014_us_state_500k)
);
CREATE TABLE
Time: 6.518 ms
-- SQL statement 13: Comment table lookup_cb_2014_us_state_500k >>>
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
Time: 0.393 ms
-- SQL statement 14: Comment lookup_cb_2014_us_state_500k columns >>>
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
Time: 0.291 ms
-- SQL statement 15: Comment lookup_cb_2014_us_state_500k columns >>>
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
Time: 0.237 ms
-- SQL statement 16: Comment lookup_cb_2014_us_state_500k columns >>>
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
Time: 0.379 ms
-- SQL statement 17: Comment lookup_cb_2014_us_state_500k columns >>>
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
Time: 0.346 ms
-- SQL statement 18: Drop table lookup_cb_2014_us_county_500k >>>
DROP TABLE IF EXISTS lookup_cb_2014_us_county_500k;
DROP TABLE
Time: 9.086 ms
-- SQL statement 19: Create table lookup_cb_2014_us_county_500k >>>
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
CREATE TABLE rif_data.lookup_cb_2014_us_county_500k (
	cb_2014_us_county_500k			VARCHAR(100)  NOT NULL,
	areaname	VARCHAR(1000),
	gid			INTEGER		  NOT NULL,
	geographic_centroid		JSON,
	PRIMARY KEY (cb_2014_us_county_500k)
);
CREATE TABLE
Time: 6.954 ms
-- SQL statement 20: Comment table lookup_cb_2014_us_county_500k >>>
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
Time: 0.447 ms
-- SQL statement 21: Comment lookup_cb_2014_us_county_500k columns >>>
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
Time: 0.417 ms
-- SQL statement 22: Comment lookup_cb_2014_us_county_500k columns >>>
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
Time: 0.232 ms
-- SQL statement 23: Comment lookup_cb_2014_us_county_500k columns >>>
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
Time: 0.346 ms
-- SQL statement 24: Comment lookup_cb_2014_us_county_500k columns >>>
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
Time: 0.192 ms
--
-- Load geolevel lookup tables
--
-- SQL statement 26: Load DB specific geolevel lookup table: (mssql_/pg_)lookup_cb_2014_us_nation_5m >>>
\copy lookup_cb_2014_us_nation_5m(cb_2014_us_nation_5m, areaname, gid, geographic_centroid) FROM 'pg_lookup_cb_2014_us_nation_5m.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 1
Time: 4.823 ms
-- SQL statement 27: Load DB specific geolevel lookup table: (mssql_/pg_)lookup_cb_2014_us_state_500k >>>
\copy lookup_cb_2014_us_state_500k(cb_2014_us_state_500k, areaname, gid, geographic_centroid) FROM 'pg_lookup_cb_2014_us_state_500k.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 56
Time: 7.823 ms
-- SQL statement 28: Load DB specific geolevel lookup table: (mssql_/pg_)lookup_cb_2014_us_county_500k >>>
\copy lookup_cb_2014_us_county_500k(cb_2014_us_county_500k, areaname, gid, geographic_centroid) FROM 'pg_lookup_cb_2014_us_county_500k.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 3233
Time: 18.446 ms
--
-- Hierarchy table
--
-- SQL statement 30: Drop table hierarchy_usa_2014 >>>
DROP TABLE IF EXISTS hierarchy_usa_2014;
DROP TABLE
Time: 1.136 ms
-- SQL statement 31: Create table hierarchy_usa_2014 >>>
CREATE TABLE rif_data.hierarchy_usa_2014 (
	cb_2014_us_county_500k	VARCHAR(100)  NOT NULL,
	cb_2014_us_nation_5m	VARCHAR(100)  NOT NULL,
	cb_2014_us_state_500k	VARCHAR(100)  NOT NULL);
CREATE TABLE
Time: 1.048 ms
-- SQL statement 32: Add primary key hierarchy_usa_2014 >>>
ALTER TABLE rif_data.hierarchy_usa_2014 ADD PRIMARY KEY (cb_2014_us_county_500k);
ALTER TABLE
Time: 3.103 ms
-- SQL statement 33: Add index key hierarchy_usa_2014_cb_2014_us_state_500k >>>
CREATE INDEX hierarchy_usa_2014_cb_2014_us_state_500k ON rif_data.hierarchy_usa_2014 (cb_2014_us_state_500k);
CREATE INDEX
Time: 2.875 ms
-- SQL statement 34: Comment table: hierarchy_usa_2014 >>>
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
Time: 0.485 ms
-- SQL statement 35: Comment column: hierarchy_usa_2014.cb_2014_us_county_500k >>>
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
Time: 0.411 ms
-- SQL statement 36: Comment column: hierarchy_usa_2014.cb_2014_us_nation_5m >>>
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
Time: 1.179 ms
-- SQL statement 37: Comment column: hierarchy_usa_2014.cb_2014_us_state_500k >>>
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
Time: 0.422 ms
--
-- Load hierarchy table
--
-- SQL statement 39: Load DB dependent hierarchy table from CSV file >>>
\copy hierarchy_usa_2014 FROM 'pg_hierarchy_usa_2014.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 3233
Time: 30.547 ms
--
-- Create geometry table
--
-- SQL statement 41: Drop geometry table geometry_usa_2014 >>>
DROP TABLE IF EXISTS geometry_usa_2014 CASCADE;
psql:rif_pg_usa_2014.sql:551: NOTICE:  drop cascades to 12 other objects
DETAIL:  drop cascades to table geometry_usa_2014_geolevel_id_1_zoomlevel_6
drop cascades to table geometry_usa_2014_geolevel_id_1_zoomlevel_7
drop cascades to table geometry_usa_2014_geolevel_id_1_zoomlevel_8
drop cascades to table geometry_usa_2014_geolevel_id_1_zoomlevel_9
drop cascades to table geometry_usa_2014_geolevel_id_2_zoomlevel_6
drop cascades to table geometry_usa_2014_geolevel_id_2_zoomlevel_7
drop cascades to table geometry_usa_2014_geolevel_id_2_zoomlevel_8
drop cascades to table geometry_usa_2014_geolevel_id_2_zoomlevel_9
drop cascades to table geometry_usa_2014_geolevel_id_3_zoomlevel_6
drop cascades to table geometry_usa_2014_geolevel_id_3_zoomlevel_7
drop cascades to table geometry_usa_2014_geolevel_id_3_zoomlevel_8
drop cascades to table geometry_usa_2014_geolevel_id_3_zoomlevel_9
DROP TABLE
Time: 33.938 ms
-- SQL statement 42: Create geometry table geometry_usa_2014 >>>
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
CREATE TABLE rif_data.geometry_usa_2014 (
	geolevel_id		INTEGER			NOT NULL,
	areaid			VARCHAR(200)	NOT NULL,
	zoomlevel		INTEGER			NOT NULL);
CREATE TABLE
Time: 1.496 ms
-- SQL statement 43: Add geom geometry column >>>
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
---------------------------------------------------------------------
 rif_data.geometry_usa_2014.geom SRID:4326 TYPE:MULTIPOLYGON DIMS:2 
(1 row)

Time: 24.592 ms
-- SQL statement 44: Comment geometry table >>>
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
Time: 0.429 ms
-- SQL statement 45: Comment geometry table column >>>
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
Time: 0.320 ms
-- SQL statement 46: Comment geometry table column >>>
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
Time: 0.412 ms
-- SQL statement 47: Comment geometry table column >>>
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
Time: 0.666 ms
-- SQL statement 48: Comment geometry table column >>>
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
Time: 0.324 ms
-- SQL statement 49: Create partitioned tables and insert function for geometry table; comment partitioned tables and columns >>>
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
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_6 (
    CHECK ( geolevel_id = 1 AND zoomlevel = 6 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_6 IS 'All geolevels geometry combined into a single table.  Geolevel 1, zoomlevel 6 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_6.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_6.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_6.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_6.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_7 (
    CHECK ( geolevel_id = 1 AND zoomlevel = 7 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_7 IS 'All geolevels geometry combined into a single table.  Geolevel 1, zoomlevel 7 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_7.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_7.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_7.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_7.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_8 (
    CHECK ( geolevel_id = 1 AND zoomlevel = 8 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_8 IS 'All geolevels geometry combined into a single table.  Geolevel 1, zoomlevel 8 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_8.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_8.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_8.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_8.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_9 (
    CHECK ( geolevel_id = 1 AND zoomlevel = 9 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_9 IS 'All geolevels geometry combined into a single table.  Geolevel 1, zoomlevel 9 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_9.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_9.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_9.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_1_zoomlevel_9.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_6 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 6 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_6 IS 'All geolevels geometry combined into a single table.  Geolevel 2, zoomlevel 6 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_6.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_6.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_6.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_6.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_7 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 7 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_7 IS 'All geolevels geometry combined into a single table.  Geolevel 2, zoomlevel 7 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_7.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_7.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_7.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_7.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_8 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 8 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_8 IS 'All geolevels geometry combined into a single table.  Geolevel 2, zoomlevel 8 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_8.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_8.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_8.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_8.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_9 (
    CHECK ( geolevel_id = 2 AND zoomlevel = 9 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_9 IS 'All geolevels geometry combined into a single table.  Geolevel 2, zoomlevel 9 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_9.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_9.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_9.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_2_zoomlevel_9.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_6 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 6 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_6 IS 'All geolevels geometry combined into a single table.  Geolevel 3, zoomlevel 6 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_6.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_6.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_6.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_6.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_7 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 7 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_7 IS 'All geolevels geometry combined into a single table.  Geolevel 3, zoomlevel 7 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_7.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_7.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_7.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_7.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_8 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 8 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_8 IS 'All geolevels geometry combined into a single table.  Geolevel 3, zoomlevel 8 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_8.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_8.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_8.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_8.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_9 (
    CHECK ( geolevel_id = 3 AND zoomlevel = 9 )
) INHERITS (geometry_usa_2014);
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_9 IS 'All geolevels geometry combined into a single table.  Geolevel 3, zoomlevel 9 partition.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_9.zoomlevel IS 'Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_9.areaid IS 'Area ID.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_9.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> COMMENT ON COLUMN geometry_usa_2014_geolevel_id_3_zoomlevel_9.geom IS 'Geometry data in SRID 4326 (WGS84).';
psql:rif_pg_usa_2014.sql:731: INFO:  SQL> CREATE OR REPLACE FUNCTION geometry_usa_2014_insert_trigger()
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
Time: 81.936 ms
-- SQL statement 50: Partition geometry table: insert trigger >>>
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
Time: 8.019 ms
-- SQL statement 51: Comment partition geometry table: insert trigger >>>
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
Time: 0.753 ms
--
-- Load geometry table
--
-- SQL statement 53: Add WKT column >>>
/*
 * SQL statement name: 	add_column.sql
 * Type:				Common SQL
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table
 * Note:				% becomes % after substitution
 */
ALTER TABLE geometry_usa_2014
  ADD WKT Text;
ALTER TABLE
Time: 2.298 ms
-- SQL statement 54: Comment geometry WKT column >>>
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
	ON COLUMN geometry_usa_2014.wkt IS 'Well known text';
COMMENT
Time: 0.403 ms
-- SQL statement 55: Load DB dependent geometry table from CSV file >>>
\copy geometry_usa_2014(geolevel_id, areaid, zoomlevel, wkt) FROM 'pg_geometry_usa_2014.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 0
Time: 7229.553 ms
-- SQL statement 56: Add WKT column >>>
/*
 * SQL statement name: 	update_geometry.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: srid; e.g. 4326
 *
 * Description:			Add column to table
 * Note:				% becomes % after substitution
 */
UPDATE geometry_usa_2014
   SET geom = ST_GeomFromText(wkt, 4326);
UPDATE 13160
Time: 5122.984 ms
-- SQL statement 57: Add primary key, index and cluster (convert to index organized table) >>>
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
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_6
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_1_zoomlevel_6_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_1_zoomlevel_6_geom_gix
 ON geometry_usa_2014_geolevel_id_1_zoomlevel_6 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_6_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_1_zoomlevel_6" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_1_zoomlevel_6": found 0 removable, 2 nonremovable row versions in 1 pages
DETAIL:  1 dead row versions cannot be removed yet.
CPU 0.00s/0.00u sec elapsed 0.01 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_1_zoomlevel_6;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_7
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_1_zoomlevel_7_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_1_zoomlevel_7_geom_gix
 ON geometry_usa_2014_geolevel_id_1_zoomlevel_7 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_7_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_1_zoomlevel_7" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_1_zoomlevel_7": found 0 removable, 2 nonremovable row versions in 1 pages
DETAIL:  1 dead row versions cannot be removed yet.
CPU 0.00s/0.01u sec elapsed 0.01 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_1_zoomlevel_7;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_8
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_1_zoomlevel_8_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_1_zoomlevel_8_geom_gix
 ON geometry_usa_2014_geolevel_id_1_zoomlevel_8 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_8_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_1_zoomlevel_8" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_1_zoomlevel_8": found 0 removable, 2 nonremovable row versions in 1 pages
DETAIL:  1 dead row versions cannot be removed yet.
CPU 0.01s/0.00u sec elapsed 0.01 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_1_zoomlevel_8;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_1_zoomlevel_9
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_1_zoomlevel_9_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_1_zoomlevel_9_geom_gix
 ON geometry_usa_2014_geolevel_id_1_zoomlevel_9 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_1_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_1_zoomlevel_9_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_1_zoomlevel_9" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_1_zoomlevel_9": found 0 removable, 2 nonremovable row versions in 1 pages
DETAIL:  1 dead row versions cannot be removed yet.
CPU 0.00s/0.01u sec elapsed 0.02 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_1_zoomlevel_9;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_6
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_2_zoomlevel_6_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_2_zoomlevel_6_geom_gix
 ON geometry_usa_2014_geolevel_id_2_zoomlevel_6 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_6_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_2_zoomlevel_6" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_2_zoomlevel_6": found 0 removable, 112 nonremovable row versions in 10 pages
DETAIL:  56 dead row versions cannot be removed yet.
CPU 0.00s/0.04u sec elapsed 0.06 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_2_zoomlevel_6;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_7
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_2_zoomlevel_7_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_2_zoomlevel_7_geom_gix
 ON geometry_usa_2014_geolevel_id_2_zoomlevel_7 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_7_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_2_zoomlevel_7" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_2_zoomlevel_7": found 0 removable, 112 nonremovable row versions in 8 pages
DETAIL:  56 dead row versions cannot be removed yet.
CPU 0.00s/0.06u sec elapsed 0.08 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_2_zoomlevel_7;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_8
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_2_zoomlevel_8_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_2_zoomlevel_8_geom_gix
 ON geometry_usa_2014_geolevel_id_2_zoomlevel_8 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_8_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_2_zoomlevel_8" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_2_zoomlevel_8": found 0 removable, 112 nonremovable row versions in 7 pages
DETAIL:  56 dead row versions cannot be removed yet.
CPU 0.00s/0.10u sec elapsed 0.11 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_2_zoomlevel_8;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_2_zoomlevel_9
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_2_zoomlevel_9_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_2_zoomlevel_9_geom_gix
 ON geometry_usa_2014_geolevel_id_2_zoomlevel_9 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_2_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_2_zoomlevel_9_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_2_zoomlevel_9" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_2_zoomlevel_9": found 0 removable, 112 nonremovable row versions in 6 pages
DETAIL:  56 dead row versions cannot be removed yet.
CPU 0.01s/0.09u sec elapsed 0.14 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_2_zoomlevel_9;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_6
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_3_zoomlevel_6_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_3_zoomlevel_6_geom_gix
 ON geometry_usa_2014_geolevel_id_3_zoomlevel_6 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_6
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_6_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_3_zoomlevel_6" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_3_zoomlevel_6": found 0 removable, 6466 nonremovable row versions in 1150 pages
DETAIL:  3233 dead row versions cannot be removed yet.
CPU 0.07s/0.10u sec elapsed 0.23 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_3_zoomlevel_6;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_7
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_3_zoomlevel_7_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_3_zoomlevel_7_geom_gix
 ON geometry_usa_2014_geolevel_id_3_zoomlevel_7 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_7
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_7_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_3_zoomlevel_7" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_3_zoomlevel_7": found 0 removable, 6466 nonremovable row versions in 1317 pages
DETAIL:  3233 dead row versions cannot be removed yet.
CPU 0.12s/0.09u sec elapsed 0.29 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_3_zoomlevel_7;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_8
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_3_zoomlevel_8_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_3_zoomlevel_8_geom_gix
 ON geometry_usa_2014_geolevel_id_3_zoomlevel_8 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_8
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_8_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_3_zoomlevel_8" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_3_zoomlevel_8": found 0 removable, 6466 nonremovable row versions in 1398 pages
DETAIL:  3233 dead row versions cannot be removed yet.
CPU 0.03s/0.26u sec elapsed 0.38 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_3_zoomlevel_8;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ALTER TABLE geometry_usa_2014_geolevel_id_3_zoomlevel_9
 ADD CONSTRAINT geometry_usa_2014_geolevel_id_3_zoomlevel_9_pk PRIMARY KEY (areaid);
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CREATE INDEX geometry_usa_2014_geolevel_id_3_zoomlevel_9_geom_gix
 ON geometry_usa_2014_geolevel_id_3_zoomlevel_9 USING GIST (geom);;
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> CLUSTER VERBOSE geometry_usa_2014_geolevel_id_3_zoomlevel_9
 USING geometry_usa_2014_geolevel_id_3_zoomlevel_9_pk;
psql:rif_pg_usa_2014.sql:853: INFO:  clustering "rif_data.geometry_usa_2014_geolevel_id_3_zoomlevel_9" using sequential scan and sort
psql:rif_pg_usa_2014.sql:853: INFO:  "geometry_usa_2014_geolevel_id_3_zoomlevel_9": found 0 removable, 6466 nonremovable row versions in 1393 pages
DETAIL:  3233 dead row versions cannot be removed yet.
CPU 0.12s/0.28u sec elapsed 0.56 sec.
psql:rif_pg_usa_2014.sql:853: INFO:  SQL> ANALYZE geometry_usa_2014_geolevel_id_3_zoomlevel_9;
DO
Time: 3186.533 ms
--
-- Adjacency table
--
-- SQL statement 59: Drop table adjacency_usa_2014 >>>
DROP TABLE IF EXISTS adjacency_usa_2014;
DROP TABLE
Time: 2.121 ms
-- SQL statement 60: Create table adjacency_usa_2014 >>>
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
CREATE TABLE rif_data.adjacency_usa_2014 (
	geolevel_id		INTEGER			NOT NULL,
	areaid			VARCHAR(200)	NOT NULL,
	num_adjacencies INTEGER			NOT NULL,
	adjacency_list	VARCHAR(8000)	NOT NULL,
	CONSTRAINT adjacency_usa_2014_pk PRIMARY KEY (geolevel_id, areaid)
);
CREATE TABLE
Time: 13.204 ms
-- SQL statement 61: Comment table: adjacency_usa_2014 >>>
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
Time: 0.663 ms
-- SQL statement 62: Comment column: adjacency_usa_2014.geolevel_id >>>
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
Time: 0.408 ms
-- SQL statement 63: Comment column: adjacency_usa_2014.areaid >>>
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
Time: 0.246 ms
-- SQL statement 64: Comment column: adjacency_usa_2014.num_adjacencies >>>
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
Time: 0.326 ms
-- SQL statement 65: Comment column: adjacency_usa_2014.adjacency_list >>>
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
Time: 0.310 ms
--
-- Load adjacency table
--
-- SQL statement 67: Load DB dependent adjacency table from CSV file >>>
\copy adjacency_usa_2014 FROM 'pg_adjacency_usa_2014.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 3262
Time: 20.056 ms
-- SQL statement 68: Remove old geolevels meta data table >>>
DELETE FROM t_rif40_geolevels WHERE geography = 'USA_2014';
DELETE 3
Time: 65.431 ms
-- SQL statement 69: Remove old geography meta data table >>>
DELETE FROM rif40_geographies WHERE geography = 'USA_2014';
DELETE 1
Time: 25.174 ms
-- SQL statement 70: Setup geography meta data table column: geometrytable >>>
/*
 * SQL statement name: 	add_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table if it does not exist
 * Note:				% becomes % after substitution
 */
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE rif40_geographies ADD geometrytable VARCHAR(30);
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column geometrytable already exists in rif40_geographies.';
        END;
    END;
$$;
psql:rif_pg_usa_2014.sql:988: NOTICE:  column geometrytable already exists in rif40_geographies.
DO
Time: 1.167 ms
-- SQL statement 71: Comment geography meta data table columngeometrytable >>>
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
	ON COLUMN rif40_geographies.geometrytable IS 'Geometry table name';
COMMENT
Time: 1.038 ms
-- SQL statement 72: Setup geography meta data table column: tiletable >>>
/*
 * SQL statement name: 	add_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table if it does not exist
 * Note:				% becomes % after substitution
 */
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE rif40_geographies ADD tiletable VARCHAR(30);
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column tiletable already exists in rif40_geographies.';
        END;
    END;
$$;
psql:rif_pg_usa_2014.sql:1025: NOTICE:  column tiletable already exists in rif40_geographies.
DO
Time: 0.687 ms
-- SQL statement 73: Comment geography meta data table columntiletable >>>
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
	ON COLUMN rif40_geographies.tiletable IS 'Tile table name';
COMMENT
Time: 0.304 ms
-- SQL statement 74: Setup geography meta data table column: minzoomlevel >>>
/*
 * SQL statement name: 	add_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table if it does not exist
 * Note:				% becomes % after substitution
 */
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE rif40_geographies ADD minzoomlevel INTEGER;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column minzoomlevel already exists in rif40_geographies.';
        END;
    END;
$$;
psql:rif_pg_usa_2014.sql:1062: NOTICE:  column minzoomlevel already exists in rif40_geographies.
DO
Time: 0.483 ms
-- SQL statement 75: Comment geography meta data table columnminzoomlevel >>>
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
	ON COLUMN rif40_geographies.minzoomlevel IS 'Minimum zoomlevel';
COMMENT
Time: 0.187 ms
-- SQL statement 76: Setup geography meta data table column: maxzoomlevel >>>
/*
 * SQL statement name: 	add_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table if it does not exist
 * Note:				% becomes % after substitution
 */
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE rif40_geographies ADD maxzoomlevel INTEGER;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column maxzoomlevel already exists in rif40_geographies.';
        END;
    END;
$$;
psql:rif_pg_usa_2014.sql:1099: NOTICE:  column maxzoomlevel already exists in rif40_geographies.
DO
Time: 0.491 ms
-- SQL statement 77: Comment geography meta data table columnmaxzoomlevel >>>
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
	ON COLUMN rif40_geographies.maxzoomlevel IS 'Maximum zoomlevel';
COMMENT
Time: 0.308 ms
-- SQL statement 78: Setup geography meta data table column: adjacencytable >>>
/*
 * SQL statement name: 	add_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table if it does not exist
 * Note:				% becomes % after substitution
 */
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE rif40_geographies ADD adjacencytable VARCHAR(30);
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column adjacencytable already exists in rif40_geographies.';
        END;
    END;
$$;
psql:rif_pg_usa_2014.sql:1136: NOTICE:  column adjacencytable already exists in rif40_geographies.
DO
Time: 0.977 ms
-- SQL statement 79: Comment geography meta data table columnadjacencytable >>>
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
	ON COLUMN rif40_geographies.adjacencytable IS 'Adjacency table';
COMMENT
Time: 0.665 ms
-- SQL statement 80: Setup geolevels meta data table column: areaid_count >>>
/*
 * SQL statement name: 	add_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table if it does not exist
 * Note:				% becomes % after substitution
 */
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE t_rif40_geolevels ADD areaid_count INTEGER;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column areaid_count already exists in t_rif40_geolevels.';
        END;
    END;
$$;
psql:rif_pg_usa_2014.sql:1173: NOTICE:  column areaid_count already exists in t_rif40_geolevels.
DO
Time: 0.960 ms
-- SQL statement 81: Comment geolevels meta data table columnareaid_count >>>
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
	ON COLUMN t_rif40_geolevels.areaid_count IS 'Area ID count';
COMMENT
Time: 0.238 ms
-- SQL statement 82: Recreate rif40_geolevels view with new columns >>>
/*
 * SQL statement name: 	rif40_geolevels_view.sql
 * Type:				Postgres/PostGIS SQL
 * Parameters:			NONE
 *
 * Description:			Recreate rif40_geolevels
 * Note:				% becomes % after substitution
 */
CREATE OR REPLACE VIEW rif40_geolevels AS 
SELECT a.geography,
    a.geolevel_name,
    a.geolevel_id,
    a.description,
    a.lookup_table,
    a.lookup_desc_column,
    a.shapefile,
    a.centroidsfile,
    a.shapefile_table,
    a.shapefile_area_id_column,
    a.shapefile_desc_column,
    a.centroids_table,
    a.centroids_area_id_column,
    a.avg_npoints_geom,
    a.avg_npoints_opt,
    a.file_geojson_len,
    a.leg_geom,
    a.leg_opt,
    a.covariate_table,
    a.resolution,
    a.comparea,
    a.listing,
    a.restricted,
    a.centroidxcoordinate_column,
    a.centroidycoordinate_column,
	a.areaid_count
   FROM t_rif40_geolevels a
  WHERE sys_context('SAHSU_CONTEXT'::character varying, 'RIF_STUDENT'::character varying)::text = 'YES'::text 
    AND a.restricted <> 1
UNION
 SELECT a.geography,
    a.geolevel_name,
    a.geolevel_id,
    a.description,
    a.lookup_table,
    a.lookup_desc_column,
    a.shapefile,
    a.centroidsfile,
    a.shapefile_table,
    a.shapefile_area_id_column,
    a.shapefile_desc_column,
    a.centroids_table,
    a.centroids_area_id_column,
    a.avg_npoints_geom,
    a.avg_npoints_opt,
    a.file_geojson_len,
    a.leg_geom,
    a.leg_opt,
    a.covariate_table,
    a.resolution,
    a.comparea,
    a.listing,
    a.restricted,
    a.centroidxcoordinate_column,
    a.centroidycoordinate_column,
	a.areaid_count
   FROM t_rif40_geolevels a
  WHERE sys_context('SAHSU_CONTEXT'::character varying, 'RIF_STUDENT'::character varying) IS NULL 
     OR sys_context('SAHSU_CONTEXT'::character varying, 'RIF_STUDENT'::character varying)::text = 'NO'::text
  ORDER BY 1, 3 DESC;
CREATE VIEW
Time: 15.072 ms

GRANT SELECT ON TABLE rif40_geolevels TO rif_user, rif_manager;
GRANT
Time: 0.420 ms
COMMENT ON VIEW rif40_geolevels
  IS 'Geolevels: hierarchy of level with a geography. Use this table for SELECT; use T_RIF40_GEOLEVELS for INSERT/UPDATE/DELETE. View with RIF_STUDENT security context support. If the user has the RIF_STUDENT role the geolevels are restricted to e.g. LADUA/DISTRICT level resolution or lower. This is controlled by the RESTRICTED field.';
COMMENT
Time: 0.989 ms
COMMENT ON COLUMN rif40_geolevels.geography IS 'Geography (e.g EW2001)';
COMMENT
Time: 1.053 ms
COMMENT ON COLUMN rif40_geolevels.geolevel_name IS 'Name of geolevel. This will be a column name in the numerator/denominator tables';
COMMENT
Time: 0.312 ms
COMMENT ON COLUMN rif40_geolevels.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT
Time: 0.265 ms
COMMENT ON COLUMN rif40_geolevels.description IS 'Description';
COMMENT
Time: 0.235 ms
COMMENT ON COLUMN rif40_geolevels.lookup_table IS 'Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is &quot;Westminster&quot;';
COMMENT
Time: 0.370 ms
COMMENT ON COLUMN rif40_geolevels.lookup_desc_column IS 'Lookup table description column name.';
COMMENT
Time: 0.349 ms
COMMENT ON COLUMN rif40_geolevels.shapefile IS 'Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead,';
COMMENT
Time: 0.404 ms
COMMENT ON COLUMN rif40_geolevels.centroidsfile IS 'Location of the GIS centroids file. Can also use CENTROIDXCOORDINATE_COLUMN, CENTROIDYCOORDINATE_COLUMN instead.';
COMMENT
Time: 0.203 ms
COMMENT ON COLUMN rif40_geolevels.shapefile_table IS 'Table containing GIS shape file data (created using shp2pgsql).';
COMMENT
Time: 0.235 ms
COMMENT ON COLUMN rif40_geolevels.shapefile_area_id_column IS 'Column containing the AREA_IDs in SHAPEFILE_TABLE';
COMMENT
Time: 0.229 ms
COMMENT ON COLUMN rif40_geolevels.shapefile_desc_column IS 'Column containing the AREA_ID descriptions in SHAPEFILE_TABLE';
COMMENT
Time: 0.200 ms
COMMENT ON COLUMN rif40_geolevels.centroids_table IS 'Table containing GIS shape file data with Arc GIS calculated population weighted centroids (created using shp2pgsql). PostGIS does not support population weighted centroids.';
COMMENT
Time: 0.644 ms
COMMENT ON COLUMN rif40_geolevels.centroids_area_id_column IS 'Column containing the AREA_IDs in CENTROIDS_TABLE. X and Y co-ordinates ciolumns are asummed to be named after CENTROIDXCOORDINATE_COLUMN and CENTROIDYCOORDINATE_COLUMN.';
COMMENT
Time: 0.539 ms
COMMENT ON COLUMN rif40_geolevels.avg_npoints_geom IS 'Average number of points in a geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT
Time: 0.692 ms
COMMENT ON COLUMN rif40_geolevels.avg_npoints_opt IS 'Average number of points in a ST_SimplifyPreserveTopology() optimsed geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT
Time: 0.664 ms
COMMENT ON COLUMN rif40_geolevels.file_geojson_len IS 'File length estimate (in bytes) for conversion of the entire geolevel geometry to GeoJSON. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT
Time: 0.354 ms
COMMENT ON COLUMN rif40_geolevels.leg_geom IS 'The average length (in projection units - usually metres) of a vector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT
Time: 0.191 ms
COMMENT ON COLUMN rif40_geolevels.leg_opt IS 'The average length (in projection units - usually metres) of a ST_SimplifyPreserveTopology() optimsed geometryvector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT
Time: 0.182 ms
COMMENT ON COLUMN rif40_geolevels.covariate_table IS 'Name of table used for covariates at this geolevel';
COMMENT
Time: 0.169 ms
COMMENT ON COLUMN rif40_geolevels.resolution IS 'Can use a map for selection at this resolution (0/1)';
COMMENT
Time: 0.166 ms
COMMENT ON COLUMN rif40_geolevels.comparea IS 'Able to be used as a comparison area (0/1)';
COMMENT
Time: 0.167 ms
COMMENT ON COLUMN rif40_geolevels.listing IS 'Able to be used in a disease map listing (0/1)';
COMMENT
Time: 0.163 ms
COMMENT ON COLUMN rif40_geolevels.restricted IS 'Is geolevel access rectricted by Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. This is enforced by the RIF application.';
COMMENT
Time: 0.536 ms
COMMENT ON COLUMN rif40_geolevels.centroidxcoordinate_column IS 'Lookup table centroid X co-ordinate column name. Can also use CENTROIDSFILE instead.';
COMMENT
Time: 0.434 ms
COMMENT ON COLUMN rif40_geolevels.centroidycoordinate_column IS 'Lookup table centroid Y co-ordinate column name.';
COMMENT
Time: 0.485 ms
COMMENT ON COLUMN rif40_geolevels.areaid_count IS 'Area ID count'

;
COMMENT
Time: 1.505 ms
-- SQL statement 83: Populate geography meta data table >>>
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
INSERT INTO rif40_geographies (
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
Time: 17.502 ms
-- SQL statement 84: Insert geolevels meta data for: cb_2014_us_nation_5m >>>
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
INSERT INTO t_rif40_geolevels (
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
Time: 10.385 ms
-- SQL statement 85: Create (if required) geolevels covariate table for: cb_2014_us_state_500k >>>
/*
 * SQL statement name: 	create_covariate_table.sql
 * Type:				Postgres/PostGIS SQL statement
 * Parameters:
 *						1: covariate_table; e.g. COV_CB_2014_US_STATE_500K
 *						2: Geolevel name: CB_2014_US_STATE_500K
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create example covariate table if it does not exist
 * Note:				%% becomes % after substitution
 */
DO $$ 
    BEGIN
        BEGIN
             CREATE TABLE rif_data.cov_cb_2014_us_state_500k (
				year 	INTEGER 	NOT NULL,
				cb_2014_us_state_500k		VARCHAR(30)	NOT NULL,
				PRIMARY KEY (year, cb_2014_us_state_500k)
			 );
        EXCEPTION
            WHEN duplicate_table THEN RAISE NOTICE 'Table cov_cb_2014_us_state_500k already exists.';
        END;
    END;
$$ 
;
psql:rif_pg_usa_2014.sql:1405: NOTICE:  Table cov_cb_2014_us_state_500k already exists.
DO
Time: 0.566 ms
-- SQL statement 86: Comment covariate table >>>
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
	ON TABLE cov_cb_2014_us_state_500k IS 'Example covariate table for: The State at a scale of 1:500,000';
COMMENT
Time: 1.162 ms
-- SQL statement 87: Comment covariate year column >>>
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
	ON COLUMN cov_cb_2014_us_state_500k.year IS 'Year';
COMMENT
Time: 0.885 ms
-- SQL statement 88: Comment covariate year column >>>
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
	ON COLUMN cov_cb_2014_us_state_500k.cb_2014_us_state_500k IS 'Geolevel name';
COMMENT
Time: 0.593 ms
-- SQL statement 89: Insert geolevels meta data for: cb_2014_us_state_500k >>>
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
INSERT INTO t_rif40_geolevels (
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
Time: 1.145 ms
-- SQL statement 90: Create (if required) geolevels covariate table for: cb_2014_us_county_500k >>>
/*
 * SQL statement name: 	create_covariate_table.sql
 * Type:				Postgres/PostGIS SQL statement
 * Parameters:
 *						1: covariate_table; e.g. COV_CB_2014_US_STATE_500K
 *						2: Geolevel name: CB_2014_US_STATE_500K
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create example covariate table if it does not exist
 * Note:				%% becomes % after substitution
 */
DO $$ 
    BEGIN
        BEGIN
             CREATE TABLE rif_data.cov_cb_2014_us_county_500k (
				year 	INTEGER 	NOT NULL,
				cb_2014_us_county_500k		VARCHAR(30)	NOT NULL,
				PRIMARY KEY (year, cb_2014_us_county_500k)
			 );
        EXCEPTION
            WHEN duplicate_table THEN RAISE NOTICE 'Table cov_cb_2014_us_county_500k already exists.';
        END;
    END;
$$ 
;
psql:rif_pg_usa_2014.sql:1519: NOTICE:  Table cov_cb_2014_us_county_500k already exists.
DO
Time: 0.642 ms
-- SQL statement 91: Comment covariate table >>>
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
	ON TABLE cov_cb_2014_us_county_500k IS 'Example covariate table for: The County at a scale of 1:500,000';
COMMENT
Time: 0.658 ms
-- SQL statement 92: Comment covariate year column >>>
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
	ON COLUMN cov_cb_2014_us_county_500k.year IS 'Year';
COMMENT
Time: 0.386 ms
-- SQL statement 93: Comment covariate year column >>>
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
	ON COLUMN cov_cb_2014_us_county_500k.cb_2014_us_county_500k IS 'Geolevel name';
COMMENT
Time: 0.515 ms
-- SQL statement 94: Insert geolevels meta data for: cb_2014_us_county_500k >>>
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
INSERT INTO t_rif40_geolevels (
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
Time: 0.890 ms
-- SQL statement 95: Populate geography meta data table >>>
/*
 * SQL statement name: 	update_geography.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. GEOGRAPHY_CB_2014_US_COUNTY_500K
 *						2: geography; e.g. CB_2014_US_500K
 *						3: Default comparision area, e.g. GEOID
 *						4: Default study area, e.g. STATENS
 *
 * Description:			Insert into geography table
 * Note:				%% becomes % after substitution
 */
UPDATE rif40_geographies
   SET defaultcomparea  = 'CB_2014_US_NATION_5M',
       defaultstudyarea = 'CB_2014_US_STATE_500K'
 WHERE geography = 'USA_2014';
UPDATE 1
Time: 3.097 ms
-- SQL statement 96: Update areaid_count column in geolevels table using geometry table >>>
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
UPDATE t_rif40_geolevels a
   SET areaid_count = (
			SELECT COUNT(DISTINCT(areaid)) AS areaid_count
			  FROM geometry_usa_2014 b
			 WHERE a.geolevel_id = b.geolevel_id)
 WHERE geography = 'USA_2014';
UPDATE 3
Time: 53.251 ms
-- SQL statement 97: Drop dependent object - view tiles_usa_2014 >>>
DROP VIEW IF EXISTS rif_data.tiles_usa_2014;
DROP VIEW
Time: 3.407 ms
--
-- Create tiles functions
--
-- SQL statement 99: Create function: longitude2tile.sql >>>
/*
 * SQL statement name: 	longitude2tile.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert longitude (WGS84 - 4326) to OSM tile x
 * Note:				% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_longitude2tile(DOUBLE PRECISION, INTEGER);
DROP FUNCTION
Time: 0.819 ms

CREATE OR REPLACE FUNCTION tileMaker_longitude2tile(longitude DOUBLE PRECISION, zoom_level INTEGER)
RETURNS INTEGER AS
$$
    SELECT FLOOR( (longitude + 180) / 360 * (1 << zoom_level) )::INTEGER
$$
LANGUAGE sql IMMUTABLE;
CREATE FUNCTION
Time: 0.582 ms
  
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
Time: 0.249 ms
-- SQL statement 100: Create function: latitude2tile.sql >>>
/*
 * SQL statement name: 	latitude2tile.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert latitude (WGS84 - 4326) to OSM tile y
 * Note:				% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_latitude2tile(DOUBLE PRECISION, INTEGER);
DROP FUNCTION
Time: 0.316 ms

CREATE OR REPLACE FUNCTION tileMaker_latitude2tile(latitude DOUBLE PRECISION, zoom_level INTEGER)
RETURNS INTEGER AS
$$
    SELECT FLOOR( (1.0 - LN(TAN(RADIANS(latitude)) + 1.0 / COS(RADIANS(latitude))) / PI()) / 2.0 * (1 << zoom_level) )::INTEGER
$$
LANGUAGE sql IMMUTABLE;
CREATE FUNCTION
Time: 0.893 ms
  
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
Time: 0.545 ms
-- SQL statement 101: Create function: tile2longitude.sql >>>
/*
 * SQL statement name: 	tile2longitude.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert OSM tile x to longitude (WGS84 - 4326) 
 * Note:				% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_tile2longitude(INTEGER, INTEGER);
DROP FUNCTION
Time: 0.298 ms

CREATE OR REPLACE FUNCTION tileMaker_tile2longitude(x INTEGER, zoom_level INTEGER)
RETURNS DOUBLE PRECISION AS
$$
	SELECT ( ( (x * 1.0) / (1 << zoom_level) * 360.0) - 180.0)::DOUBLE PRECISION
$$
LANGUAGE sql IMMUTABLE;
CREATE FUNCTION
Time: 0.407 ms
  
COMMENT ON FUNCTION tileMaker_tile2longitude(INTEGER, INTEGER) IS 'Function: 	 tileMaker_tile2longitude()
Parameters:	 OSM Tile x, zoom level
Returns:	 Longitude
Description: Convert OSM tile x to longitude (WGS84 - 4326)';
COMMENT
Time: 0.187 ms
-- SQL statement 102: Create function: tile2latitude.sql >>>
/*
 * SQL statement name: 	tileMaker_tile2latitude.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert OSM tile y to latitude (WGS84 - 4326)
 * Note:				% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_tile2latitude(INTEGER, INTEGER);
DROP FUNCTION
Time: 0.536 ms

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
Time: 0.600 ms
  
COMMENT ON FUNCTION tileMaker_tile2latitude(INTEGER, INTEGER) IS 'Function: 	 tileMaker_tile2latitude()
Parameters:	 OSM Tile y, zoom level
Returns:	 Latitude
Description: Convert OSM tile y to latitude (WGS84 - 4326)';
COMMENT
Time: 0.246 ms
--
-- Create tiles tables
--
-- SQL statement 104: Drop table t_tiles_usa_2014 >>>
DROP TABLE IF EXISTS rif_data.t_tiles_usa_2014;
DROP TABLE
Time: 1.101 ms
-- SQL statement 105: Create tiles table >>>
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
CREATE TABLE rif_data.t_tiles_usa_2014 (
	geolevel_id			INTEGER			NOT NULL,
	zoomlevel			INTEGER			NOT NULL,
	x					INTEGER			NOT NULL, 
	y					INTEGER			NOT NULL,
	optimised_topojson	JSON,
	tile_id				VARCHAR(200)	NOT NULL,
	areaid_count		INTEGER			NOT NULL,
	PRIMARY KEY (tile_id));
CREATE TABLE
Time: 49.793 ms
-- SQL statement 106: Comment tiles table >>>
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
Time: 0.465 ms
-- SQL statement 107: Comment tiles table column >>>
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
Time: 0.240 ms
-- SQL statement 108: Comment tiles table column >>>
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
Time: 0.282 ms
-- SQL statement 109: Comment tiles table column >>>
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
Time: 0.276 ms
-- SQL statement 110: Comment tiles table column >>>
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
Time: 0.258 ms
-- SQL statement 111: Comment tiles table column >>>
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
Time: 0.299 ms
-- SQL statement 112: Comment tiles table column >>>
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
Time: 0.251 ms
-- SQL statement 113: Comment tiles table column >>>
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
Time: 0.427 ms
-- SQL statement 114: Add tiles index: t_tiles_usa_2014_x_tile >>>
CREATE INDEX t_tiles_usa_2014_x_tile ON rif_data.t_tiles_usa_2014 (geolevel_id, zoomlevel, x);
CREATE INDEX
Time: 3.458 ms
-- SQL statement 115: Add tiles index: t_tiles_usa_2014_y_tile >>>
CREATE INDEX t_tiles_usa_2014_y_tile ON rif_data.t_tiles_usa_2014 (geolevel_id, zoomlevel, x);
CREATE INDEX
Time: 3.152 ms
-- SQL statement 116: Add tiles index: t_tiles_usa_2014_xy_tile >>>
CREATE INDEX t_tiles_usa_2014_xy_tile ON rif_data.t_tiles_usa_2014 (geolevel_id, zoomlevel, x, y);
CREATE INDEX
Time: 4.746 ms
-- SQL statement 117: Add tiles index: t_tiles_usa_2014_areaid_count >>>
CREATE INDEX t_tiles_usa_2014_areaid_count ON rif_data.t_tiles_usa_2014 (areaid_count);
CREATE INDEX
Time: 3.311 ms
-- SQL statement 118: Create tiles view >>>
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
CREATE VIEW rif_data.tiles_usa_2014 AS 
WITH a AS (
        SELECT geography,
               MAX(geolevel_id) AS max_geolevel_id
          FROM t_rif40_geolevels
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
          FROM b, t_rif40_geolevels b2
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
Time: 12.363 ms
-- SQL statement 119: Comment tiles view >>>
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
Time: 0.555 ms
-- SQL statement 120: Comment tiles view column >>>
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
Time: 0.621 ms
-- SQL statement 121: Comment tiles view column >>>
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
Time: 0.344 ms
-- SQL statement 122: Comment tiles view column >>>
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
Time: 0.570 ms
-- SQL statement 123: Comment tiles view column >>>
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
Time: 0.628 ms
-- SQL statement 124: Comment tiles view column >>>
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
Time: 0.377 ms
-- SQL statement 125: Comment tiles view column >>>
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
Time: 0.396 ms
-- SQL statement 126: Comment tiles view column >>>
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
Time: 0.309 ms
-- SQL statement 127: Comment tiles view column >>>
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
Time: 0.480 ms
-- SQL statement 128: Comment tiles view column >>>
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
Time: 0.526 ms
--
-- Load tiles table
--
-- SQL statement 130: Load DB dependent tiles table from geolevel CSV files >>>
\copy t_tiles_usa_2014(geolevel_id,zoomlevel,x,y,tile_id,areaid_count,optimised_topojson) FROM 'pg_t_tiles_cb_2014_us_nation_5m.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 1
Time: 11.893 ms
-- SQL statement 131: Load DB dependent tiles table from geolevel CSV files >>>
\copy t_tiles_usa_2014(geolevel_id,zoomlevel,x,y,tile_id,areaid_count,optimised_topojson) FROM 'pg_t_tiles_cb_2014_us_state_500k.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 2713
Time: 24883.043 ms
-- SQL statement 132: Load DB dependent tiles table from geolevel CSV files >>>
\copy t_tiles_usa_2014(geolevel_id,zoomlevel,x,y,tile_id,areaid_count,optimised_topojson) FROM 'pg_t_tiles_cb_2014_us_county_500k.csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8';
COPY 4669
Time: 7134.948 ms
--
-- Analyze tables
--
-- SQL statement 134: Grant table/view lookup_cb_2014_us_nation_5m >>>
/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
GRANT SELECT ON rif_data.lookup_cb_2014_us_nation_5m TO rif_user, rif_manager;
GRANT
Time: 5.277 ms
-- SQL statement 135: Grant table/view lookup_cb_2014_us_state_500k >>>
/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
GRANT SELECT ON rif_data.lookup_cb_2014_us_state_500k TO rif_user, rif_manager;
GRANT
Time: 4.171 ms
-- SQL statement 136: Grant table/view lookup_cb_2014_us_county_500k >>>
/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
GRANT SELECT ON rif_data.lookup_cb_2014_us_county_500k TO rif_user, rif_manager;
GRANT
Time: 5.273 ms
-- SQL statement 137: Grant table/view hierarchy_usa_2014 >>>
/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
GRANT SELECT ON rif_data.hierarchy_usa_2014 TO rif_user, rif_manager;
GRANT
Time: 6.908 ms
-- SQL statement 138: Grant table/view geometry_usa_2014 >>>
/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
GRANT SELECT ON rif_data.geometry_usa_2014 TO rif_user, rif_manager;
GRANT
Time: 0.868 ms
-- SQL statement 139: Grant table/view adjacency_usa_2014 >>>
/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
GRANT SELECT ON rif_data.adjacency_usa_2014 TO rif_user, rif_manager;
GRANT
Time: 3.988 ms
-- SQL statement 140: Grant table/view t_tiles_usa_2014 >>>
/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
GRANT SELECT ON rif_data.t_tiles_usa_2014 TO rif_user, rif_manager;
GRANT
Time: 3.693 ms
-- SQL statement 141: Grant table/view tiles_usa_2014 >>>
/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
GRANT SELECT ON rif_data.tiles_usa_2014 TO rif_user, rif_manager;
GRANT
Time: 0.870 ms
-- SQL statement 142: Grant table/view adjacency_usa_2014 >>>
/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%% becomes % after substitution
 */
GRANT SELECT ON rif_data.adjacency_usa_2014 TO rif_user, rif_manager;
GRANT
Time: 0.352 ms
-- SQL statement 143: Commit transaction >>>
END;
COMMIT
Time: 448.987 ms
--
-- Analyze tables
--
-- SQL statement 145: Describe table lookup_cb_2014_us_nation_5m >>>
\dS+ rif_data.lookup_cb_2014_us_nation_5m;
                                Table "rif_data.lookup_cb_2014_us_nation_5m"
        Column        |          Type           | Modifiers | Storage  | Stats target |     Description     
----------------------+-------------------------+-----------+----------+--------------+---------------------
 cb_2014_us_nation_5m | character varying(100)  | not null  | extended |              | Area ID field
 areaname             | character varying(1000) |           | extended |              | Area Name field
 gid                  | integer                 | not null  | plain    |              | GID field
 geographic_centroid  | json                    |           | extended |              | Geographic centroid
Indexes:
    "lookup_cb_2014_us_nation_5m_pkey" PRIMARY KEY, btree (cb_2014_us_nation_5m)

-- SQL statement 146: Analyze table lookup_cb_2014_us_nation_5m >>>
VACUUM ANALYZE rif_data.lookup_cb_2014_us_nation_5m;
VACUUM
Time: 35.410 ms
-- SQL statement 147: Describe table lookup_cb_2014_us_state_500k >>>
\dS+ rif_data.lookup_cb_2014_us_state_500k;
                                Table "rif_data.lookup_cb_2014_us_state_500k"
        Column         |          Type           | Modifiers | Storage  | Stats target |     Description     
-----------------------+-------------------------+-----------+----------+--------------+---------------------
 cb_2014_us_state_500k | character varying(100)  | not null  | extended |              | Area ID field
 areaname              | character varying(1000) |           | extended |              | Area Name field
 gid                   | integer                 | not null  | plain    |              | GID field
 geographic_centroid   | json                    |           | extended |              | Geographic centroid
Indexes:
    "lookup_cb_2014_us_state_500k_pkey" PRIMARY KEY, btree (cb_2014_us_state_500k)

-- SQL statement 148: Analyze table lookup_cb_2014_us_state_500k >>>
VACUUM ANALYZE rif_data.lookup_cb_2014_us_state_500k;
VACUUM
Time: 10.869 ms
-- SQL statement 149: Describe table lookup_cb_2014_us_county_500k >>>
\dS+ rif_data.lookup_cb_2014_us_county_500k;
                                Table "rif_data.lookup_cb_2014_us_county_500k"
         Column         |          Type           | Modifiers | Storage  | Stats target |     Description     
------------------------+-------------------------+-----------+----------+--------------+---------------------
 cb_2014_us_county_500k | character varying(100)  | not null  | extended |              | Area ID field
 areaname               | character varying(1000) |           | extended |              | Area Name field
 gid                    | integer                 | not null  | plain    |              | GID field
 geographic_centroid    | json                    |           | extended |              | Geographic centroid
Indexes:
    "lookup_cb_2014_us_county_500k_pkey" PRIMARY KEY, btree (cb_2014_us_county_500k)

-- SQL statement 150: Analyze table lookup_cb_2014_us_county_500k >>>
VACUUM ANALYZE rif_data.lookup_cb_2014_us_county_500k;
VACUUM
Time: 20.719 ms
-- SQL statement 151: Describe table hierarchy_usa_2014 >>>
\dS+ rif_data.hierarchy_usa_2014;
                                                        Table "rif_data.hierarchy_usa_2014"
         Column         |          Type          | Modifiers | Storage  | Stats target |                        Description                        
------------------------+------------------------+-----------+----------+--------------+-----------------------------------------------------------
 cb_2014_us_county_500k | character varying(100) | not null  | extended |              | Hierarchy lookup for The County at a scale of 1:500,000
 cb_2014_us_nation_5m   | character varying(100) | not null  | extended |              | Hierarchy lookup for The nation at a scale of 1:5,000,000
 cb_2014_us_state_500k  | character varying(100) | not null  | extended |              | Hierarchy lookup for The State at a scale of 1:500,000
Indexes:
    "hierarchy_usa_2014_pkey" PRIMARY KEY, btree (cb_2014_us_county_500k)
    "hierarchy_usa_2014_cb_2014_us_state_500k" btree (cb_2014_us_state_500k)

-- SQL statement 152: Analyze table hierarchy_usa_2014 >>>
VACUUM ANALYZE rif_data.hierarchy_usa_2014;
VACUUM
Time: 15.061 ms
-- SQL statement 153: Describe table geometry_usa_2014 >>>
\dS+ rif_data.geometry_usa_2014;
                                                                                          Table "rif_data.geometry_usa_2014"
   Column    |          Type          | Modifiers | Storage  | Stats target |                                                               Description                                                               
-------------+------------------------+-----------+----------+--------------+-----------------------------------------------------------------------------------------------------------------------------------------
 geolevel_id | integer                | not null  | plain    |              | ID for ordering (1=lowest resolution). Up to 99 supported.
 areaid      | character varying(200) | not null  | extended |              | Area ID.
 zoomlevel   | integer                | not null  | plain    |              | Zoom level: 0 to maxoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11
 geom        | geometry               |           | main     |              | Geometry data in SRID 4326 (WGS84).
 wkt         | text                   |           | extended |              | Well known text
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

-- SQL statement 154: Analyze table geometry_usa_2014 >>>
VACUUM ANALYZE rif_data.geometry_usa_2014;
VACUUM
Time: 122.188 ms
-- SQL statement 155: Describe table t_tiles_usa_2014 >>>
\dS+ rif_data.t_tiles_usa_2014;
                                                                                       Table "rif_data.t_tiles_usa_2014"
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

-- SQL statement 156: Analyze table t_tiles_usa_2014 >>>
VACUUM ANALYZE rif_data.t_tiles_usa_2014;
VACUUM
Time: 103.844 ms
--
-- EOF
```
