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
-- Rapid Enquiry Facility (RIF) - RIF startup procedure.
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

DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_startup();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_startup(BOOLEAN);

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_startup(no_checks BOOLEAN DEFAULT FALSE)
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_startup()
Parameters:	No check boolean; default FALSE (do the checks)
Returns:	1 
Description:	Startup functions - for calling from /usr/local/pgsql/etc/psqlrc
		Postgres has no ON-LOGON trigger
		Java users will need to run this first
		
		Check running non privileged, SET search path, setup logging
		If no checks flag is set, no further checks or object creation carried out
		
		Check if FDW functionality enabled (RIF40_PARAMETER FDWServerName), settings, FOREIGN SERVER setup OK 
		Create if required FDW tables i.e. those numerators in RIF40_NUM_DENOM_ERRORS with no local table of the same name

Create:	 	
        TABLE t_rif40_num_denom [if required]
		VIEWS rif40_user_version, rif40_num_denom, rif40_num_denom_errors [if required]
		TEMPORARY TABLE g_rif40_study_areas, g_rif40_comparison_areas

		Only creates objects if rif40_geographies, rif40_tables, rif40_health_study_themes exist
		The VIEW rif40_user_version contains the revision string from this file. If it does not match or the view does not exist the objects are rebuilt.
		To force a rebuild:

DROP VIEW rif40_user_version;

Check access to RIF40 tables

SQL generated:

a) User local numerator and denominator pairs

CREATE TABLE peterh.t_rif40_num_denom (
        geography              VARCHAR(50)     NOT NULL,
        numerator_table        VARCHAR(30)     NOT NULL,
        denominator_table      VARCHAR(30)     NOT NULL);

b) Errors in rif40_num_denom VIEW of valid numerator and denominator pairs

CREATE OR REPLACE VIEW peterh.rif40_num_denom_errors
AS
WITH n AS (
        SELECT geography, numerator_table, numerator_description, automatic, is_object_resolvable, n_num_denom_validated, numerator_owner
          FROM (
                SELECT g.geography, n.table_name numerator_table, n.description numerator_description, n.automatic,
                       rif40_sql_pkg.rif40_is_object_resolvable(n.table_name) AS is_object_resolvable,
		       rif40_sql_pkg.rif40_num_denom_validate(g.geography, n.table_name) AS n_num_denom_validated,
		       rif40_sql_pkg.rif40_object_resolve(n.table_name) AS numerator_owner
                  FROM rif40_geographies g, rif40_tables n
                 WHERE n.isnumerator = 1
                   AND n.automatic   = 1) AS n1 -* Automatic numerators *-
), d AS (
        SELECT geography, denominator_table, denominator_description, is_object_resolvable, d_num_denom_validated, denominator_owner,
               rif40_sql_pkg.rif40_auto_indirect_checks(denominator_table)::text AS auto_indirect_error
          FROM (
	        SELECT g.geography, d.table_name denominator_table, d.description denominator_description,
	               rif40_sql_pkg.rif40_is_object_resolvable(d.table_name) is_object_resolvable,
		       rif40_sql_pkg.rif40_num_denom_validate(g.geography, d.table_name) AS d_num_denom_validated,
		       rif40_sql_pkg.rif40_object_resolve(d.table_name) AS denominator_owner
	          FROM rif40_geographies g, rif40_tables d
	         WHERE d.isindirectdenominator = 1
	           AND d.automatic             = 1) AS d1 -* Automatic denominators *-
)
SELECT n.geography,
       n.numerator_owner,
       n.numerator_table,
       n.is_object_resolvable AS is_numerator_resolvable,
       n.n_num_denom_validated,
       n.numerator_description,
       d.denominator_owner,
       d.denominator_table,
       d.is_object_resolvable AS is_denominator_resolvable,
       d.d_num_denom_validated,
       d.denominator_description,
       n.automatic,
       CASE
                WHEN d.auto_indirect_error IS NULL THEN 0 ELSE 1 END AS auto_indirect_error_flag,
       d.auto_indirect_error AS auto_indirect_error,
       f.create_status AS n_fdw_create_status,
       f.error_message AS n_fdw_error_message,
       f.date_created AS n_fdw_date_created,
       f.rowtest_passed AS n_fdw_rowtest_passed
  FROM d, n
	LEFT OUTER JOIN rif40_fdw_tables f ON (n.numerator_table = f.table_name)
 WHERE n.geography = d.geography
UNION
SELECT g.geography,
       rif40_sql_pkg.rif40_object_resolve(g.numerator_table) AS numerator_owner,
       g.numerator_table,
       rif40_sql_pkg.rif40_is_object_resolvable(g.numerator_table) AS is_numerator_resolvable,
       rif40_sql_pkg.rif40_num_denom_validate(g.geography, g.numerator_table) AS n_num_denom_validated, 
       n.description AS numerator_description,
       rif40_sql_pkg.rif40_object_resolve(g.denominator_table) AS denominator_owner,
       g.denominator_table,
       rif40_sql_pkg.rif40_is_object_resolvable(g.denominator_table) AS is_denominator_resolvable,
       rif40_sql_pkg.rif40_num_denom_validate(g.geography, g.denominator_table) AS d_num_denom_validated, 
       d.description AS denominator_description,
       0 AS automatic,
       0 AS auto_indirect_error_flag,
       NULL::text AS auto_indirect_error,
       f.create_status AS n_fdw_create_status,
       f.error_message AS n_fdw_error_message,
       f.date_created AS n_fdw_date_created,
       f.rowtest_passed AS n_fdw_rowtest_passed
  FROM t_rif40_num_denom g
        LEFT OUTER JOIN rif40_tables n ON (n.table_name = g.numerator_table)
        LEFT OUTER JOIN rif40_tables d ON (d.table_name = g.denominator_table)
	LEFT OUTER JOIN rif40_fdw_tables f ON (n.table_name = f.table_name)
 ORDER BY 1, 2, 5;

Query to display RIF40_NUM_DENOM_ERRORS:

SELECT geography AS geog, 
       numerator_owner AS n_owner, numerator_table, is_numerator_resolvable AS n_resolv, n_num_denom_validated AS n_nd_valid, 
       denominator_owner AS d_owner, denominator_table, is_denominator_resolvable AS d_resolv, d_num_denom_validated AS d_nd_valid, 
       automatic AS auto, n_fdw_date_created,
       CASE WHEN n_num_denom_validated = 1 AND d_num_denom_validated = 1 THEN 1 ELSE 0 END AS ok
 FROM rif40_num_denom_errors
 ORDER BY n_num_denom_validated DESC, d_num_denom_validated DESC, is_numerator_resolvable DESC, is_denominator_resolvable DESC, denominator_table, numerator_table;

c) rif40_num_denom VIEW of valid numerator and denominator pairs

CREATE OR REPLACE VIEW peterh.rif40_num_denom
AS
WITH n AS (
        SELECT geography, numerator_table, numerator_description, automatic, theme_description
          FROM (
                SELECT g.geography, n.table_name AS numerator_table, n.description AS numerator_description, n.automatic,
                       t.description AS theme_description
                  FROM rif40_geographies g, rif40_tables n, rif40_health_study_themes t
                 WHERE n.isnumerator = 1
                   AND n.automatic   = 1
                   AND rif40_sql_pkg.rif40_is_object_resolvable(n.table_name) = 1
                   AND n.theme       = t.theme) n1
         WHERE rif40_sql_pkg.rif40_num_denom_validate(n1.geography, n1.numerator_table) = 1
), d AS (
        SELECT geography, denominator_table, denominator_description
          FROM (
                        SELECT g.geography, d.table_name AS denominator_table, d.description AS denominator_description
                          FROM rif40_geographies g, rif40_tables d
                         WHERE d.isindirectdenominator = 1
                           AND d.automatic             = 1
                           AND rif40_sql_pkg.rif40_is_object_resolvable(d.table_name) = 1) d1
        WHERE rif40_sql_pkg.rif40_num_denom_validate(d1.geography, d1.denominator_table) = 1
          AND rif40_sql_pkg.rif40_auto_indirect_checks(d1.denominator_table) IS NULL
)
SELECT n.geography,
       n.numerator_table,
       n.numerator_description,
       n.theme_description,
       d.denominator_table,
       d.denominator_description,
       n.automatic
  FROM d, n
 WHERE n.geography = d.geography
UNION
SELECT g.geography,
       g.numerator_table,
       n.description AS numerator_description,
       'Local user theme' AS theme_description,
       g.denominator_table,
       d.description AS denominator_description,
       0 automatic
  FROM t_rif40_num_denom g
        LEFT OUTER JOIN rif40_tables n ON (n.table_name = g.numerator_table)
        LEFT OUTER JOIN rif40_tables d ON (d.table_name = g.denominator_table)
 ORDER BY 1, 2, 4;

d) Temporary study and comparision area tables.  Used to speed up extracts. On Postgres they are not global and need to be created for each session

CREATE GLOBAL TEMPORARY TABLE g_rif40_study_areas (
        study_id                INTEGER         NOT NULL,
        area_id                 VARCHAR(300)    NOT NULL,
        band_id                 INTEGER) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE g_rif40_comparison_areas (
        study_id                INTEGER         NOT NULL,
        area_id                 VARCHAR(300)    NOT NULL) ON COMMIT PRESERVE ROWS;

e) Revision control view

CREATE OR REPLACE VIEW peterh.rif40_user_version AS SELECT CAST('1.10' AS numeric) AS user_schema_revision;

f) Study status table required by Java middleware

CREATE TABLE study_status (
  study_id 			integer NOT NULL,
  study_state 		character varying NOT NULL,
  creation_date 	timestamp without time zone NOT NULL,
  ith_update 		serial NOT NULL,
  message 			character varying(255));

 */
DECLARE
	c1start CURSOR FOR
		SELECT COUNT(tablename) AS total
		  FROM pg_tables
		 WHERE tablename IN ('rif40_geographies', 'rif40_tables', 'rif40_health_study_themes');
	c2 CURSOR(l_table_or_view VARCHAR) FOR 		/* User objects */
		SELECT tablename AS table_or_view			/* Local tables */
		  FROM pg_tables
		 WHERE tableowner = USER
		   AND schemaname = USER
		   AND tablename  = l_table_or_view
		 UNION
		SELECT viewname	AS table_or_view 			/* Local views */
		  FROM pg_views
		 WHERE viewowner  = USER
		   AND schemaname = USER
		   AND viewname   = l_table_or_view
		 UNION
		SELECT a.relname table_or_view 				/* Temporary tables */
 		 FROM pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relkind        = 'r' 				/* Relational table */
		   AND a.relpersistence = 't' 				/* Persistence: temporary */
		   AND a.relowner       = r.oid
		   AND n.nspname        LIKE 'pg_temp%'
		   AND pg_table_is_visible(a.oid)		/* Table is visible in users current schema */
		   AND a.relname        = l_table_or_view
		ORDER BY 1;
	c3 CURSOR FOR					/* Current revision of user schema */
		SELECT user_schema_revision::VARCHAR
		  FROM rif40.rif40_user_version;
	c4 CURSOR FOR					/* Schema search path */
		SELECT name, REPLACE(setting, USER||', ', '') AS setting, reset_val
	 	  FROM pg_settings
		 WHERE name = 'search_path';
	c5 CURSOR FOR					/* FDW Settings in RIF40_PARAMETERS */
		SELECT a.param_value AS fdwservername,
		       b.param_value AS fdwservertype,
		       c.param_value AS fdwdbserver
		  FROM rif40.rif40_parameters a, rif40.rif40_parameters b, rif40.rif40_parameters c
		 WHERE a.param_name = 'FDWServerName'
		   AND b.param_name = 'FDWServerType'
		   AND c.param_name = 'FDWDBServer';
	c6 CURSOR(l_fdwservername VARCHAR) FOR 		/* FDW server name check */
		SELECT r.rolname AS srvowner, s.srvname
		  FROM pg_foreign_server s, pg_roles r
		 WHERE s.srvname  = l_fdwservername
		   AND s.srvowner = r.oid;
	c7 CURSOR FOR					/* Potential FDW tables i.e. those numerators in RIF40_NUM_DENOM_ERRORS with no local table of the same name */
		WITH a AS (
			SELECT numerator_table, numerator_owner, is_numerator_resolvable, COUNT(DISTINCT(geography))::INTEGER AS total_geographies
			  FROM rif40.rif40_num_denom_errors
			 GROUP BY numerator_table, numerator_owner, is_numerator_resolvable
		), b AS (
			SELECT a.relname AS foreign_table				/* FDW tables */
			  FROM pg_foreign_table b, pg_roles r, pg_class a, pg_namespace n
			 WHERE b.ftrelid  = a.oid
			   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relowner = r.oid
			   AND n.nspname  = USER
			   AND n.oid      = a.relnamespace
			   AND a.relname NOT LIKE 'fdw%'
		), c AS (
			SELECT LOWER(a.numerator_table) AS table_or_view
			  FROM a
			 UNION
			SELECT LOWER(b.foreign_table) AS table_or_view
			  FROM b
		)
		SELECT c.table_or_view AS foreign_table,
 		       CASE WHEN b.foreign_table = c.table_or_view THEN 1 ELSE 0 END AS foreign_table_exists,
                       CASE WHEN a.numerator_owner IS NOT NULL AND a.is_numerator_resolvable = 1 THEN 1 ELSE 0 END AS table_ok,
		       a.total_geographies,
                       f.create_status, f.error_message, f.date_created, f.rowtest_passed
		  FROM c
			LEFT OUTER JOIN rif40.rif40_fdw_tables f ON (c.table_or_view = LOWER(f.table_name))
			LEFT OUTER JOIN b ON (b.foreign_table = c.table_or_view)
			LEFT OUTER JOIN a ON (c.table_or_view = LOWER(a.numerator_table))
		 ORDER BY 1;
	c8 CURSOR FOR					/* Obsolete FDW tables i.e. those no longer in RIF40_NUM_DENOM_ERRORS */
		WITH a AS (
			SELECT numerator_table, denominator_table
			  FROM rif40.rif40_num_denom_errors
		), b AS (
			SELECT a.relname AS foreign_table				/* FDW tables */
			  FROM pg_foreign_table b, pg_roles r, pg_class a, pg_namespace n, rif40_fdw_tables t
			 WHERE b.ftrelid  = a.oid
			   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relowner = r.oid
			   AND n.nspname  = USER
			   AND n.oid      = a.relnamespace
			   AND a.relname NOT LIKE 'fdw%'
			   AND a.relname  = LOWER(t.table_name)
		), c AS (
			SELECT b.foreign_table
			  FROM b 
			EXCEPT 
			SELECT b1.table_or_view
			  FROM (
				SELECT LOWER(a.numerator_table) AS table_or_view
				  FROM a
				 UNION
				SELECT LOWER(a.denominator_table) AS table_or_view
				  FROM a) AS b1
		)
		SELECT c.foreign_table, b.create_status, b.error_message, b.date_created, b.rowtest_passed
		  FROM c, rif40.rif40_fdw_tables b
		 WHERE c.foreign_table = LOWER(b.table_name)
		 ORDER BY 1;
--
	c9 CURSOR FOR
		SELECT PostGIS_Version() AS PostGIS_Version /* Expecting "2.0 USE_GEOS=1 USE_PROJ=1 USE_STATS=1" */, 
		       PostGIS_geos_version() AS PostGIS_geos_version,
		       PostGIS_lib_version() AS PostGIS_lib_version,  
		       PostGIS_libXML_version() AS PostGIS_libXML_version,  
		       PostGIS_PROJ_version() AS PostGIS_PROJ_version,  
		       PostGIS_GDAL_version() AS PostGIS_GDAL_version, 
		       PostGIS_raster_lib_version() AS PostGIS_raster_lib_version,
		       PostGIS_Full_Version() AS PostGIS_Full_Version;
	c10 CURSOR(l_extension VARCHAR) FOR
		SELECT extname, extversion /* Required: postgis plpgsql; optional: adminpack, plperl, oracle_fdw, postgis_topology, dblink */
		  FROM pg_extension
		 WHERE extname = l_extension;
	c11 CURSOR FOR 
		SELECT *
		  FROM pg_user WHERE usename = CURRENT_USER;
	c12 CURSOR FOR
		SELECT proname
		  FROM pg_proc a, pg_namespace b
		 WHERE a.proname      = 'rif40_run_study'
		   AND a.pronamespace = b.oid
		   AND b.nspname      = USER;
	c14 CURSOR FOR
		SELECT *
		  FROM pg_stat_activity
		 WHERE pid = pg_backend_pid();
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
	c5_rec RECORD;
	c6_rec RECORD;
	c7_rec RECORD;
	c8_rec RECORD;
	c9_rec RECORD;
	c10_rec RECORD;
	c11_rec RECORD;
	c12_rec RECORD;
	c14_rec RECORD;
--
	rif40_run_study BOOLEAN:=FALSE;	
	rif40_num_denom BOOLEAN:=FALSE;	
	rif40_num_denom_errors BOOLEAN:=FALSE;	
	t_rif40_num_denom BOOLEAN:=FALSE;	
	g_rif40_study_areas BOOLEAN:=FALSE;	
	g_rif40_comparison_areas BOOLEAN:=FALSE;	
	rif40_user_version BOOLEAN:=FALSE;	
--
	fdw_enabled BOOLEAN:=FALSE;	
--
	drop_objects BOOLEAN:=FALSE; 			/* To force a rebuild */
--
	sql_stmt VARCHAR;
	i INTEGER:=0;					/* CREATE count */
	j INTEGER:=0;					/* DROP count */
--
-- Revision control rebuild will blow up if you branch...
--
	cvs_revision VARCHAR:='$Revision: 1.11 $';	/* DO NOT EDIT THIS - IT IS FOR CVSs benefit */
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'disabled - user % is not or has rif_user or rif_manager role', USER::VARCHAR);
		RETURN;
	END IF;
--
-- User is a rif user; and MUST be non privileged; streaming replication for backup is allowed
--
	OPEN c11;
	FETCH c11 INTO c11_rec;
	CLOSE c11;
	IF c11_rec.usesuper IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'Cannot detect if user is superuser');
	ELSIF c11_rec.usesuper THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'User % is superuser', 
			c11_rec.usename::VARCHAR);
	ELSIF c11_rec.usecreatedb THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'User % has database creation privilege', 
			c11_rec.usename::VARCHAR);
	ELSIF SUBSTR(version(), 12, 3)::NUMERIC < 9.5 THEN	
		IF c11_rec.usecatupd THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'User % has update system catalog privilege', 
				c11_rec.usename::VARCHAR);
		END IF;
	END IF;
	
--
-- Setup logging
--
	PERFORM rif40_log_pkg.rif40_log_setup();
--
-- Prepend user to search path if not RIF40 (otherwise the function will try and create the objects in the rif40 schema, 
-- and you will use the rif40 t_rif40_num_denom). Add rif_studies (schema for study extracts/maps)
--
	OPEN c4;
	FETCH c4 INTO c4_rec;
	CLOSE c4;
	IF c4_rec.name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'search_path NOT found');
	ELSIF USER NOT IN ('rif40', 'postgres') THEN
		sql_stmt:='SET search_path TO '||USER||','||c4_rec.setting||',rif_studies';
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'SQL> %;', sql_stmt);
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		OPEN c4;
		FETCH c4 INTO c4_rec;
		CLOSE c4;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'search_path not set for: %', USER::VARCHAR);
	END IF;	

--
-- Drop old rum_study() function
--	
	sql_stmt:='DROP FUNCTION IF EXISTS '||USER||'.rif40_run_study(INTEGER, INTEGER)';
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'SQL> %;', sql_stmt);
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);

--
-- Create temporary tables if required
--
	OPEN c2('g_rif40_study_areas');
	FETCH c2 INTO c2_rec;
--
-- Fixed - for multiple logons 
--
	IF c2_rec.table_or_view = 'g_rif40_study_areas' THEN
		g_rif40_study_areas:=TRUE;
	END IF;
	CLOSE c2;
	OPEN c2('g_rif40_comparison_areas');
	FETCH c2 INTO c2_rec;
	IF c2_rec.table_or_view = 'g_rif40_comparison_areas' THEN		
		g_rif40_comparison_areas:=TRUE;
	END IF;
	CLOSE c2;
	IF NOT g_rif40_study_areas THEN
--		sql_stmt:='DROP TABLE IF EXISTS g_rif40_study_areas';
--		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE TEMPORARY TABLE g_rif40_study_areas ('||E'\n'||
			E'\t'||'study_id						INTEGER 		NOT NULL,'||E'\n'||
			E'\t'||'area_id							VARCHAR(300) 	NOT NULL,'||E'\n'||
			E'\t'||'band_id							INTEGER			NULL,'||E'\n'||
			E'\t'||'intersect_count 				INTEGER 		NULL,'||E'\n'||
			E'\t'||'distance_from_nearest_source 	NUMERIC 		NULL,'||E'\n'||
			E'\t'||'nearest_rifshapepolyid 			VARCHAR 		NULL,'||E'\n'||
			E'\t'||'exposure_value					NUMERIC			NULL) ON COMMIT PRESERVE ROWS';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON TABLE g_rif40_study_areas 		IS ''Local session cache of links study areas and bands for a given study. Created for high performance in extracts.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.study_id 	IS ''Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.area_id 	IS ''An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.band_id 	IS ''A band allocated to the area''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.intersect_count IS ''Number of intersects with shapes''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.distance_from_nearest_source IS ''Distance from nearest source (Km)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.nearest_rifshapepolyid IS ''Nearest rifshapepolyid (shape reference)''';	
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.exposure_value IS ''Exposure value (when bands selected by exposure values)''';	
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
		
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'Created temporary table: g_rif40_study_areas');
		i:=i+1;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_startup', 'Temporary table: g_rif40_study_areas exists');
	END IF;
	IF NOT g_rif40_comparison_areas THEN
--		sql_stmt:='DROP TABLE IF EXISTS g_rif40_comparison_areas';
--		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE TEMPORARY TABLE g_rif40_comparison_areas ('||E'\n'||
			E'\t'||'study_id		INTEGER 	NOT NULL,'||E'\n'||
			E'\t'||'area_id			VARCHAR(300) 	NOT NULL) ON COMMIT PRESERVE ROWS';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON TABLE g_rif40_comparison_areas 		IS ''Local session cache of links comparison areas and bands for a given study. Created for high performance in extracts.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_comparison_areas.study_id 	IS ''Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_comparison_areas.area_id 	IS ''An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'Created temporary table: g_rif40_comparison_areas');
		i:=i+1;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'Temporary table: g_rif40_comparison_areas exists');
	END IF;

--
-- Set application name to RIF; save original
--
	OPEN c14;
	FETCH c14 INTO c14_rec;
	CLOSE c14;
	sql_stmt:='SET rif40.application_name = '''||c14_rec.application_name||''';';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
	IF c14_rec.application_name = 'psql' THEN
		sql_stmt:='SET application_name = ''RIF (psql)'';';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
--
-- Need to detect JAVA
--
	ELSE
		sql_stmt:='SET application_name = ''RIF (other/'||c14_rec.application_name||')'';';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
	END IF;
--
-- If no checks flag is set, no further checks or object creation carried out
--
	IF no_checks THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'No checks flag set, no checks or object creation carried out');
		RETURN;
	END IF;
	
--
-- Check extensions Required: postgis plpgsql; optional: adminpack, plperl, oracle_fdw, postgis_topology, dblink
-- 
	OPEN c10('postgis');
	FETCH c10 INTO c10_rec;
	CLOSE c10;
	IF c10_rec.extname IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'PostGIS extension is not loaded');
	ELSE
		OPEN c9;
		FETCH c9 INTO c9_rec;
		CLOSE c9;
		IF c9_rec.postgis_geos_version IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'PostGIS extension was not compiled with GEOS');
		ELSIF c9_rec.postgis_libxml_version IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'PostGIS extension was not compiled with XML');
		ELSIF c9_rec.postgis_proj_version IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'PostGIS extension was not compiled with PROJ');
		ELSIF c9_rec.postgis_gdal_version IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'PostGIS extension was not compiled with GDAL');
		ELSIF c9_rec.postgis_raster_lib_version IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'PostGIS extension was not compiled with RASTER');
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'PostGIS extension V% (%)',
				c10_rec.extversion::VARCHAR, c9_rec.PostGIS_Full_Version::VARCHAR);
		END IF;
	END IF;

--
-- Get FDWServerName, FDWServerType, FDWDBServer from RIF40_PARAMETERS
--
	OPEN c5;
	FETCH c5 INTO c5_rec;
	CLOSE c5;
	IF c5_rec.fdwservername IS NULL OR c5_rec.fdwservertype IS NULL OR c5_rec.fdwdbserver IS NULL OR 
	   c5_rec.fdwservername = ''    OR c5_rec.fdwservertype = ''    OR c5_rec.fdwdbserver = '' THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.');
	ELSE
--
-- Check FDW server is installed
--
		OPEN c10(c5_rec.fdwservertype);
		FETCH c10 INTO c10_rec;
		CLOSE c10;
		IF c10_rec.extname IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'Foreign data wrapper server extension: % not loaded', 
				c5_rec.fdwservertype::VARCHAR);
		END IF;
--
-- Check access to FDWServerName
--
		OPEN c6(c5_rec.fdwservername);
		FETCH c6 INTO c6_rec;
		CLOSE c6;
		IF c6_rec.srvowner IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'FDW functionality disabled - FDWServerName: % not found', 
				c5_rec.fdwservername::VARCHAR);
		ELSIF has_server_privilege(c6_rec.srvowner, c6_rec.srvname, 'USAGE') THEN
			fdw_enabled:=TRUE;
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'FDW functionality enabled to FDWServerName: %.% using % V%', 
				c6_rec.srvowner::VARCHAR, c6_rec.srvname::VARCHAR, c5_rec.fdwservertype::VARCHAR, c10_rec.extversion::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'FDW functionality disabled - no access to FDWServerName: %.%', 
				c6_rec.srvowner::VARCHAR, c6_rec.srvname::VARCHAR);
		END IF;
--
-- DB link is required
--
		OPEN c10('dblink');
		FETCH c10 INTO c10_rec;
		CLOSE c10;
		IF c10_rec.extname IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_startup', 'Dblink extension is not loaded');
		END IF;
	END IF;
--
-- Check rif40_geographies, rif40_tables, rif40_health_study_themes exist
--
	OPEN c1start;
	FETCH c1start INTO c1_rec;
	CLOSE c1start;
--
-- Check user objects exist
--
	OPEN c2('rif40_num_denom');
	FETCH c2 INTO c2_rec;
	IF c2_rec.table_or_view = 'rif40_num_denom' THEN
		rif40_num_denom:=TRUE;
	END IF;
	CLOSE c2;
	OPEN c2('rif40_num_denom_errors');
	FETCH c2 INTO c2_rec;
	IF c2_rec.table_or_view = 'rif40_num_denom_errors' THEN
		rif40_num_denom_errors:=TRUE;
	END IF;
	CLOSE c2;
	OPEN c2('t_rif40_num_denom');
	FETCH c2 INTO c2_rec;
	IF c2_rec.table_or_view = 't_rif40_num_denom' THEN
		t_rif40_num_denom:=TRUE;
	END IF;
	CLOSE c2;
	OPEN c2('rif40_user_version');
	FETCH c2 INTO c2_rec;
	IF c2_rec.table_or_view = 'rif40_user_version' THEN
		rif40_user_version:=TRUE;
	END IF;
	CLOSE c2;
--
-- Only build DEFINER rif40_run_study if NOT rif40
--
	IF USER != 'rif40' THEN
		OPEN c12;
		FETCH c12 INTO c12_rec;
		CLOSE c12;
		IF c12_rec.proname = 'rif40_run_study' THEN
			rif40_run_study:=TRUE;
		END IF;
	END IF;
--
-- Get rif40_user_version revision
--
	IF rif40_user_version THEN
		OPEN c3;
		FETCH c3 INTO c3_rec;
		CLOSE c3;
		IF cvs_revision = c3_rec.user_schema_revision THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'V% DB version % matches', 
				cvs_revision::VARCHAR, c3_rec.user_schema_revision::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'V% DB version % mismatch; object; rebuild forced', 
				cvs_revision::VARCHAR, c3_rec.user_schema_revision::VARCHAR);
			drop_objects:=TRUE;
		END IF;
	END IF;
--
-- Check for the presence of dependent tables
--
	IF c1_rec.total != 3 THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 
			'V% rif40_geographies, rif40_tables, rif40_health_study_themes do not [YET] exist; rebuild forced when they do', 
			cvs_revision::VARCHAR);
		drop_objects:=TRUE;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'V% rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: %', 
			cvs_revision::VARCHAR, USER::VARCHAR);
	END IF;
--
-- If rif40_user_version does not exist - force a rebuild
--
	IF NOT rif40_user_version THEN
		drop_objects:=TRUE;
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'VIEW rif40_user_version not found; rebuild forced');
	END IF;
--
-- Display search path
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'search_path: %, reset: %', 
		c4_rec.setting::VARCHAR, c4_rec.reset_val::VARCHAR);
--
	IF drop_objects THEN
--
-- If user objects exist - then delete them
--
		IF rif40_user_version THEN
			sql_stmt:='DROP VIEW rif40_user_version';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			rif40_user_version:=FALSE;
			j:=j+1;
		END IF;
		IF rif40_num_denom THEN
			sql_stmt:='DROP VIEW rif40_num_denom';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			rif40_num_denom:=FALSE;
			j:=j+1;
		END IF;
		IF rif40_num_denom_errors THEN
			sql_stmt:='DROP VIEW rif40_num_denom_errors';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			rif40_num_denom_errors:=FALSE;
			j:=j+1;
		END IF;
		IF t_rif40_num_denom THEN
			sql_stmt:='DROP TABLE t_rif40_num_denom';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			t_rif40_num_denom:=FALSE;
			j:=j+1;
		END IF;
		IF rif40_run_study AND USER != 'rif40' THEN
			sql_stmt:='DROP FUNCTION IF EXISTS '||USER||'.rif40_run_study(INTEGER, BOOLEAN, INTEGER)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			rif40_run_study:=FALSE;
			j:=j+1;
		END IF;		
	END IF;
--
-- Check for the presence of dependent tables - only continue of they do
--
	IF c1_rec.total != 3 THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 
			'Check for the presence of dependent tables - only continue of they do, c1_rec.total: %', 
			c1_rec.total::VARCHAR);
		RETURN;
	END IF;
--
-- If user objects do not exist them create them
--
	IF NOT rif40_run_study AND USER != 'rif40' THEN
		sql_stmt:='CREATE OR REPLACE FUNCTION '||USER||'.rif40_run_study(study_id INTEGER, debug BOOLEAN, recursion_level INTEGER DEFAULT 0)'||E'\n'||
'RETURNS BOOLEAN'||E'\n'||		
'SECURITY DEFINER'||E'\n'||
'AS '||CHR(36)||'func'||CHR(36)||E'\n'||
'DECLARE'||E'\n'||
'/*'||E'\n'||
'Function:	rif40_run_study()'||E'\n'||
'Parameter:	Study ID, enable debug boolean (default FALSE), recursion level (internal parameter DO NOT USE)'||E'\n'||
'Returns:	Success or failure [BOOLEAN]'||E'\n'||
'			Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)'||E'\n'||
'			Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost'||E'\n'||
'Description:	Run study '||E'\n'||
'Runs DEFINER USER; calls rif40_sm_pkg.rif40_run_study(). Intended for batch.'||E'\n'||
' */'||E'\n'||
'BEGIN'||E'\n'||
'	RETURN rif40_sm_pkg.rif40_run_study(study_id, debug, recursion_level);'||E'\n'||
'END;'||E'\n'||
CHR(36)||'func'||CHR(36)||E'\n'||
'LANGUAGE ''plpgsql'''||E'\n'||
'';
--
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON FUNCTION '||USER||'.rif40_run_study(INTEGER, BOOLEAN, INTEGER) IS '''||
'Function:	rif40_run_study()'||E'\n'||
'Parameter:	Study ID'||E'\n'||
'Returns:	Success or failure [BOOLEAN]'||E'\n'||
'			Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)'||E'\n'||
'			Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost'||E'\n'||
'Description:	Run study '||E'\n'||
E'\n'||
'Runs DEFINER USER; calls rif40_sm_pkg.rif40_run_study(). Intended for batch.'||E'\n'||
'''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
		sql_stmt:='GRANT EXECUTE ON FUNCTION '||USER||'.rif40_run_study(INTEGER, BOOLEAN, INTEGER) TO rif40';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
	END IF;
	IF NOT t_rif40_num_denom THEN
		sql_stmt:='CREATE TABLE '||USER||'.t_rif40_num_denom ('||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'geography              VARCHAR(50)     NOT NULL,'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'numerator_table        VARCHAR(30)     NOT NULL,'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'denominator_table      VARCHAR(30)     NOT NULL)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE UNIQUE INDEX rif40_num_denom_pk ON '||USER||'.t_rif40_num_denom(geography, numerator_table, denominator_table)';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='ANALYZE VERBOSE '||USER||'.t_rif40_num_denom';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON TABLE '||USER||'.t_rif40_num_denom IS ''Private copy of extra numerator and denominator pairs not added automatically.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.t_rif40_num_denom.denominator_table IS ''Denominator table''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.t_rif40_num_denom.geography IS ''Geography (e.g EW2001)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.t_rif40_num_denom.numerator_table IS ''Numerator table''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Add triggers
--
		sql_stmt:='CREATE TRIGGER t_rif40_num_denom_checks'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'BEFORE INSERT OR UPDATE OF geography, numerator_table, denominator_table ON t_rif40_num_denom'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'FOR EACH ROW'||E'\n';	
		sql_stmt:=sql_stmt||E'\t'||'WHEN ((NEW.geography IS NOT NULL AND NEW.geography::text <> '''') OR (NEW.numerator_table IS NOT NULL AND NEW.numerator_table::text <> '''') OR (NEW.denominator_table IS NOT NULL AND NEW.denominator_table::text <> ''''))'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_num_denom_checks()';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE TRIGGER t_rif40_num_denom_checks_del'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'BEFORE DELETE ON t_rif40_num_denom'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'FOR EACH ROW'||E'\n';	
		sql_stmt:=sql_stmt||E'\t'||'EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_num_denom_checks()';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON TRIGGER t_rif40_num_denom_checks ON t_rif40_num_denom IS ''INSERT OR UPDATE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_num_denom_checks()''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON TRIGGER t_rif40_num_denom_checks_del ON t_rif40_num_denom IS ''DELETE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_num_denom_checks()''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'Created table: t_rif40_num_denom');
		i:=i+1;
	END IF;
	IF NOT rif40_num_denom_errors THEN
		sql_stmt:='CREATE OR REPLACE VIEW '||USER||'.rif40_num_denom_errors'||E'\n'||
			'AS'||E'\n'||
			'WITH n AS ('||E'\n'||
			E'\t'||'SELECT geography, numerator_table, numerator_description, automatic, is_object_resolvable, n_num_denom_validated, numerator_owner'||E'\n'||
			E'\t'||'  FROM ('||E'\n'||
			E'\t'||E'\t'||'SELECT g.geography, n.table_name numerator_table, n.description numerator_description, n.automatic,'||E'\n'||
			E'\t'||E'\t'||'       rif40_sql_pkg.rif40_is_object_resolvable(n.table_name) AS is_object_resolvable,'||E'\n'||
		        E'\t'||E'\t'||'       rif40_sql_pkg.rif40_num_denom_validate(g.geography, n.table_name) AS n_num_denom_validated,'||E'\n'||
		        E'\t'||E'\t'||'       rif40_sql_pkg.rif40_object_resolve(n.table_name) AS numerator_owner'||E'\n'||
			E'\t'||E'\t'||'  FROM rif40_geographies g, rif40_tables n'||E'\n'||
			E'\t'||E'\t'||' WHERE n.isnumerator = 1'||E'\n'||
			E'\t'||E'\t'||'   AND n.automatic   = 1) AS n1 /* Automatic numerators */'||E'\n'||
			'), d AS ('||E'\n'||
			E'\t'||'SELECT geography, denominator_table, denominator_description, is_object_resolvable, d_num_denom_validated, denominator_owner,'||E'\n'||
			E'\t'||'       rif40_sql_pkg.rif40_auto_indirect_checks(denominator_table)::text AS auto_indirect_error'||E'\n'||
			E'\t'||'  FROM ('||E'\n'||
			E'\t'||E'\t'||'SELECT g.geography, d.table_name denominator_table, d.description denominator_description,'||E'\n'||
			E'\t'||E'\t'||'       rif40_sql_pkg.rif40_is_object_resolvable(d.table_name) is_object_resolvable,'||E'\n'||
		        E'\t'||E'\t'||'       rif40_sql_pkg.rif40_num_denom_validate(g.geography, d.table_name) AS d_num_denom_validated,'||E'\n'||
		        E'\t'||E'\t'||'       rif40_sql_pkg.rif40_object_resolve(d.table_name) AS denominator_owner'||E'\n'||
			E'\t'||E'\t'||'  FROM rif40_geographies g, rif40_tables d'||E'\n'||
			E'\t'||E'\t'||' WHERE d.isindirectdenominator = 1'||E'\n'||
			E'\t'||E'\t'||'   AND d.automatic             = 1) AS d1 /* Automatic denominators */'||E'\n'||
			')'||E'\n'||
			'SELECT n.geography,'||E'\n'|| 
			'       n.numerator_owner,'||E'\n'|| 
			'       n.numerator_table,'||E'\n'|| 
			'       n.is_object_resolvable AS is_numerator_resolvable,'||E'\n'|| 
			'       n.n_num_denom_validated,'||E'\n'|| 
			'       n.numerator_description,'||E'\n'|| 
			'       d.denominator_owner,'||E'\n'|| 
			'       d.denominator_table,'||E'\n'|| 
			'       d.is_object_resolvable AS is_denominator_resolvable,'||E'\n'|| 
			'       d.d_num_denom_validated,'||E'\n'|| 
			'       d.denominator_description,'||E'\n'|| 
			'       n.automatic,'||E'\n'|| 
			'       CASE'||E'\n'||
			E'\t'||'	WHEN d.auto_indirect_error IS NULL THEN 0 ELSE 1 END AS auto_indirect_error_flag,'||E'\n'||
			'       d.auto_indirect_error AS auto_indirect_error,'||E'\n'||
			'       f.create_status AS n_fdw_create_status,'||E'\n'||
			'       f.error_message AS n_fdw_error_message,'||E'\n'||
			'       f.date_created AS n_fdw_date_created,'||E'\n'||
			'       f.rowtest_passed AS n_fdw_rowtest_passed'||E'\n'||
			'  FROM d, n'||E'\n'||
			E'\t'||'LEFT OUTER JOIN rif40_fdw_tables f ON (n.numerator_table = f.table_name)'||E'\n'||
			' WHERE n.geography = d.geography'||E'\n';
		IF  USER != 'rif40' THEN
			sql_stmt:=sql_stmt||'UNION'||E'\n'||
				'SELECT g.geography,'||E'\n'|| 
			'       rif40_sql_pkg.rif40_object_resolve(g.numerator_table) AS numerator_owner,'||E'\n'|| 
			'       g.numerator_table,'||E'\n'|| 
			'       rif40_sql_pkg.rif40_is_object_resolvable(g.numerator_table) AS is_numerator_resolvable,'||E'\n'|| 
			'       rif40_sql_pkg.rif40_num_denom_validate(g.geography, g.numerator_table) AS num_denom_validated,'||E'\n'|| 
			'       n.description AS numerator_description,'||E'\n'||
			'       rif40_sql_pkg.rif40_object_resolve(g.denominator_table) AS denominator_owner,'||E'\n'|| 
			'       g.denominator_table,'||E'\n'|| 
			'       rif40_sql_pkg.rif40_is_object_resolvable(g.denominator_table) AS is_denominator_resolvable,'||E'\n'|| 
			'       rif40_sql_pkg.rif40_num_denom_validate(g.geography, g.denominator_table) AS num_denom_validated,'||E'\n'|| 
			'       d.description AS denominator_description,'||E'\n'||
			'       0 AS automatic,'||E'\n'|| 
			'       0 AS auto_indirect_error_flag,'||E'\n'|| 
			'       NULL::text AS auto_indirect_error,'||E'\n'||
			'       f.create_status AS n_fdw_create_status,'||E'\n'||
			'       f.error_message AS n_fdw_error_message,'||E'\n'||
			'       f.date_created AS n_fdw_date_created,'||E'\n'||
			'       f.rowtest_passed AS n_fdw_rowtest_passed'||E'\n'||
			'  FROM t_rif40_num_denom g'||E'\n'||
			E'\t'||'LEFT OUTER JOIN rif40_tables n ON (n.table_name = g.numerator_table)'||E'\n'||
			E'\t'||'LEFT OUTER JOIN rif40_tables d ON (d.table_name = g.denominator_table)'||E'\n'||
			E'\t'||'LEFT OUTER JOIN rif40_fdw_tables f ON (n.table_name = f.table_name)'||E'\n';
		END IF;
		sql_stmt:=sql_stmt||' ORDER BY 1, 2, 5';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON VIEW '||USER||'.rif40_num_denom_errors 				IS ''All possible numerator and indirect standardisation denominator pairs with error diagnostic fields. As this is a CROSS JOIN the will be a lot of output as tables are not rejected on the basis of user access or containing the correct geography geolevel fields.'''; 
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.geography 		IS ''Geography''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.numerator_owner 		IS ''Numerator table owner''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.numerator_table 		IS ''Numerator table''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.denominator_owner 	IS ''Denominator table owner''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.denominator_table 	IS ''Denominator table''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.is_numerator_resolvable 	IS ''Is the numerator table resolvable and accessible (0/1)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.n_num_denom_validated      IS ''Is the numerator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.is_denominator_resolvable IS ''Is the denominator table resolvable and accessible (0/1)''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.numerator_description	IS ''Numerator table description''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.d_num_denom_validated      IS ''Is the denominator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.denominator_description 	IS ''Denominator table description''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.automatic 		IS ''Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.auto_indirect_error_flag IS ''Error flag 0/1. Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.auto_indirect_error 	IS ''Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator. List of geographies and tables in error.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.n_fdw_create_status 	IS ''RIF numerator foreign data wrappers table create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors).''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.n_fdw_error_message 	IS ''RIF numerator foreign data wrappers table error message when create status is: E(Created, errors in test SELECT, N(Not created, errors).''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.n_fdw_date_created 	IS ''RIF numerator foreign data wrappers table date FDW table created (or attempted to be).''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom_errors.n_fdw_rowtest_passed 	IS ''RIF numerator foreign data wrappers table SELECT rowtest passed (0/1).''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'Created view: rif40_num_denom_errors');
		i:=i+1;
	END IF;
	IF NOT rif40_num_denom THEN
		sql_stmt:='CREATE OR REPLACE VIEW '||USER||'.rif40_num_denom'||E'\n';
		sql_stmt:=sql_stmt||'AS'||E'\n'||
			'WITH n AS ('||E'\n'||
			E'\t'||'SELECT geography, numerator_table, numerator_description, automatic, theme_description'||E'\n'||
			E'\t'||'  FROM ('||E'\n'||
			E'\t'||E'\t'||'SELECT g.geography, n.table_name AS numerator_table, n.description AS numerator_description, n.automatic,'||E'\n'||
       			E'\t'||E'\t'||'       t.description AS theme_description'||E'\n'||
			E'\t'||E'\t'||'  FROM rif40_geographies g, rif40_tables n, rif40_health_study_themes t'||E'\n'||
			E'\t'||E'\t'||' WHERE n.isnumerator = 1'||E'\n'||
			E'\t'||E'\t'||'   AND n.automatic   = 1'||E'\n'||
			E'\t'||E'\t'||'   AND rif40_sql_pkg.rif40_is_object_resolvable(n.table_name) = 1'||E'\n'||
			E'\t'||E'\t'||'   AND n.theme       = t.theme) AS n1'||E'\n'||
			E'\t'||' WHERE rif40_sql_pkg.rif40_num_denom_validate(n1.geography, n1.numerator_table) = 1'||E'\n'||
			'), d AS ('||E'\n'||
			E'\t'||'SELECT geography, denominator_table, denominator_description'||E'\n'||
			E'\t'||'  FROM ('||E'\n'||
			E'\t'||E'\t'||'	SELECT g.geography, d.table_name AS denominator_table, d.description AS denominator_description'||E'\n'||
			E'\t'||E'\t'||'	  FROM rif40_geographies g, rif40_tables d'||E'\n'||
			E'\t'||E'\t'||'	 WHERE d.isindirectdenominator = 1'||E'\n'||
			E'\t'||E'\t'||'	   AND d.automatic             = 1'||E'\n'||
			E'\t'||E'\t'||'	   AND rif40_sql_pkg.rif40_is_object_resolvable(d.table_name) = 1) AS d1'||E'\n'||
			E'\t'||'WHERE rif40_sql_pkg.rif40_num_denom_validate(d1.geography, d1.denominator_table) = 1'||E'\n'||
		   	E'\t'||'  AND rif40_sql_pkg.rif40_auto_indirect_checks(d1.denominator_table) IS NULL'||E'\n'||
			')'||E'\n'||
			'SELECT n.geography,'||E'\n'||
			'       n.numerator_table,'||E'\n'||
			'       n.numerator_description,'||E'\n'||
			'       n.theme_description,'||E'\n'||
			'       d.denominator_table,'||E'\n'||
			'       d.denominator_description,'||E'\n'||
			'       n.automatic'||E'\n'||
			'  FROM n, d'||E'\n'||
			' WHERE n.geography = d.geography'||E'\n';
--
-- Add t_rif40_num_denom for non rif40 users
--
		IF  USER != 'rif40' THEN
			sql_stmt:=sql_stmt||'UNION'||E'\n'||
				'SELECT g.geography,'||E'\n'||
				'       g.numerator_table,'||E'\n'||
				'       n.description AS numerator_description,'||E'\n'||
				'       ''Local user theme'' AS theme_description,'||E'\n'||
				'       g.denominator_table,'||E'\n'||
				'       d.description AS denominator_description,'||E'\n'||
				'       0 automatic'||E'\n'||
				'  FROM t_rif40_num_denom g'||E'\n'||
				'	LEFT OUTER JOIN rif40_tables n ON (n.table_name = g.numerator_table)'||E'\n'||
				'	LEFT OUTER JOIN rif40_tables d ON (d.table_name = g.denominator_table)'||E'\n';
 		END IF;
		sql_stmt:=sql_stmt||' ORDER BY 1, 2, 4';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON VIEW '||USER||'.rif40_num_denom 				IS ''Numerator and indirect standardisation denominator pairs. Use RIF40_NUM_DENOM_ERROR if your numerator and denominator table pair is missing. You must have your own copy of RIF40_NUM_DENOM or you will only see the tables RIF40 has access to. Tables not rejected if the user does not have access or the table does not contain the correct geography geolevel fields.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom.geography 		IS ''Geography''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom.numerator_table 	IS ''Numerator table''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom.denominator_table 	IS ''Denominator table''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom.numerator_description	IS ''Numerator table description''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom.denominator_description	IS ''Denominator table description''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom.automatic 		IS ''Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_num_denom.theme_description 	IS ''Numerator table health study theme description''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'Created view: rif40_num_denom');
		i:=i+1;
	END IF;

--
-- FDW  support
--
	IF fdw_enabled AND USER != 'rif40' THEN
--
-- Create support views if needed (FDW_USER_TABLES, FDW_USER_TAB_COLUMNS)
--
		i:=i+rif40_sql_pkg.rif40_create_fdw_views(drop_objects, c5_rec.fdwservername, c5_rec.fdwservertype);
		FOR c7_rec IN c7 LOOP
--
-- Attmept to create FDW tables for potential FDW tables i.e. those numerators in RIF40_NUM_DENOM_ERRORS with no local table of the same name 
--
			IF c7_rec.foreign_table_exists = 0 AND c7_rec.table_ok = 1 THEN /* Local table - DO NOT TOUCH */
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'Local numerator: %', 
					c7_rec.foreign_table::VARCHAR);
			ELSE
				i:=i+rif40_sql_pkg.rif40_create_fdw_table(c7_rec.foreign_table, c7_rec.total_geographies, drop_objects, c5_rec.fdwservername, c5_rec.fdwservertype);
			END IF;
		END LOOP;
--
-- It would also be desirable to clean up FDW tables no longer in use
--
		FOR c8_rec IN c8 LOOP
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_startup', 'Remote foreign data wrapper health table % is obsolete and needs to be removed', 
				c8_rec.foreign_table::VARCHAR);
		END LOOP;
	END IF;
--
-- RIF40 doesn't have a version view
--
	IF NOT rif40_user_version THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'V% Creating view: rif40_user_version', 
			cvs_revision::VARCHAR);
		sql_stmt:='CREATE OR REPLACE VIEW '||USER||'.rif40_user_version AS SELECT CAST('''||cvs_revision||''' AS VARCHAR) AS user_schema_revision';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON VIEW '||USER||'.rif40_user_version 				IS ''User schema revision control view.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN '||USER||'.rif40_user_version.user_schema_revision 	IS ''Revision (derived from CVS).''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		IF USER = 'rif40' THEN
			sql_stmt:='GRANT SELECT ON rif40_user_version TO rif_user';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='GRANT SELECT ON rif40_user_version TO rif_manager';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		i:=i+1;
	END IF;
--
	IF i > 0 OR j > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'Deleted %, created % tables/views/foreign data wrapper tables', 
			j::VARCHAR, i::VARCHAR);
	END IF;
--
END;
$func$
LANGUAGE PLPGSQL;

GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_startup(BOOLEAN) TO rif40, PUBLIC;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_startup(BOOLEAN) IS 'Function: 	rif40_startup()
Parameters:	None
Returns:	1 
Description:	Startup functions - for calling from /usr/local/pgsql/etc/psqlrc
		Postgres has no ON-LOGON trigger
		Java users will need to run this first
						
		Check running non privileged, SET search path, setup logging
		If no checks flag is set, no further checks or object creation carried out

		Check if FDW functionality enabled (RIF40_PARAMETER FDWServerName), settings, FOREIGN SERVER setup OK 
		Create if required FDW tables i.e. those numerators in RIF40_NUM_DENOM_ERRORS with no local table of the same name

Create:	 	TABLE t_rif40_num_denom [if required]
		VIEWS rif40_user_version, rif40_num_denom, rif40_num_denom_errors [if required]
		TEMPORARY TABLE g_rif40_study_areas, g_rif40_comparison_areas

		Only creates objects if rif40_geographies, rif40_tables, rif40_health_study_themes exist
		The VIEW rif40_user_version contains the revision string from this file. If it does not match or the view does not exist the objects are rebuilt.
		To force a rebuild:

DROP VIEW rif40_user_version;

Check access to RIF40 tables

SQL generated:

a) User local numerator and denominator pairs

CREATE TABLE peterh.t_rif40_num_denom (
        geography              VARCHAR(50)     NOT NULL,
        numerator_table        VARCHAR(30)     NOT NULL,
        denominator_table      VARCHAR(30)     NOT NULL);

b) Errors in rif40_num_denom VIEW of valid numerator and denominator pairs

CREATE OR REPLACE VIEW peterh.rif40_num_denom_errors
AS
WITH n AS (
        SELECT geography, numerator_table, numerator_description, automatic, is_object_resolvable, n_num_denom_validated, numerator_owner
          FROM (
                SELECT g.geography, n.table_name numerator_table, n.description numerator_description, n.automatic,
                       rif40_sql_pkg.rif40_is_object_resolvable(n.table_name) AS is_object_resolvable,
		       rif40_sql_pkg.rif40_num_denom_validate(g.geography, n.table_name) AS n_num_denom_validated,
		       rif40_sql_pkg.rif40_object_resolve(n.table_name) AS numerator_owner
                  FROM rif40_geographies g, rif40_tables n
                 WHERE n.isnumerator = 1
                   AND n.automatic   = 1) AS n1 -* Automatic numerators *-
), d AS (
        SELECT geography, denominator_table, denominator_description, is_object_resolvable, d_num_denom_validated, denominator_owner,
               rif40_sql_pkg.rif40_auto_indirect_checks(denominator_table)::text AS auto_indirect_error
          FROM (
	        SELECT g.geography, d.table_name denominator_table, d.description denominator_description,
	               rif40_sql_pkg.rif40_is_object_resolvable(d.table_name) is_object_resolvable,
		       rif40_sql_pkg.rif40_num_denom_validate(g.geography, d.table_name) AS d_num_denom_validated,
		       rif40_sql_pkg.rif40_object_resolve(d.table_name) AS denominator_owner
	          FROM rif40_geographies g, rif40_tables d
	         WHERE d.isindirectdenominator = 1
	           AND d.automatic             = 1) AS d1 -* Automatic denominators *-
)
SELECT n.geography,
       n.numerator_owner,
       n.numerator_table,
       n.is_object_resolvable AS is_numerator_resolvable,
       n.n_num_denom_validated,
       n.numerator_description,
       d.denominator_owner,
       d.denominator_table,
       d.is_object_resolvable AS is_denominator_resolvable,
       d.d_num_denom_validated,
       d.denominator_description,
       n.automatic,
       CASE
                WHEN d.auto_indirect_error IS NULL THEN 0 ELSE 1 END AS auto_indirect_error_flag,
       d.auto_indirect_error AS auto_indirect_error,
       f.create_status AS n_fdw_create_status,
       f.error_message AS n_fdw_error_message,
       f.date_created AS n_fdw_date_created,
       f.rowtest_passed AS n_fdw_rowtest_passed
  FROM d, n
	LEFT OUTER JOIN rif40_fdw_tables f ON (n.numerator_table = f.table_name)
 WHERE n.geography = d.geography
UNION
SELECT g.geography,
       rif40_sql_pkg.rif40_object_resolve(g.numerator_table) AS numerator_owner,
       g.numerator_table,
       rif40_sql_pkg.rif40_is_object_resolvable(g.numerator_table) AS is_numerator_resolvable,
       rif40_sql_pkg.rif40_num_denom_validate(g.geography, g.numerator_table) AS n_num_denom_validated, 
       n.description AS numerator_description,
       rif40_sql_pkg.rif40_object_resolve(g.denominator_table) AS denominator_owner,
       g.denominator_table,
       rif40_sql_pkg.rif40_is_object_resolvable(g.denominator_table) AS is_denominator_resolvable,
       rif40_sql_pkg.rif40_num_denom_validate(g.geography, g.denominator_table) AS d_num_denom_validated, 
       d.description AS denominator_description,
       0 AS automatic,
       0 AS auto_indirect_error_flag,
       NULL::text AS auto_indirect_error,
       f.create_status AS n_fdw_create_status,
       f.error_message AS n_fdw_error_message,
       f.date_created AS n_fdw_date_created,
       f.rowtest_passed AS n_fdw_rowtest_passed
  FROM t_rif40_num_denom g
        LEFT OUTER JOIN rif40_tables n ON (n.table_name = g.numerator_table)
        LEFT OUTER JOIN rif40_tables d ON (d.table_name = g.denominator_table)
	LEFT OUTER JOIN rif40_fdw_tables f ON (n.table_name = f.table_name)
 ORDER BY 1, 2, 5;

Query to display RIF40_NUM_DENOM_ERRORS:

SELECT geography AS geog, 
       numerator_owner AS n_owner, numerator_table, is_numerator_resolvable AS n_resolv, n_num_denom_validated AS n_nd_valid, 
       denominator_owner AS d_owner, denominator_table, is_denominator_resolvable AS d_resolv, d_num_denom_validated AS d_nd_valid, 
       automatic AS auto, n_fdw_date_created,
       CASE WHEN n_num_denom_validated = 1 AND d_num_denom_validated = 1 THEN 1 ELSE 0 END AS ok
 FROM rif40_num_denom_errors
 ORDER BY n_num_denom_validated DESC, d_num_denom_validated DESC, is_numerator_resolvable DESC, is_denominator_resolvable DESC, denominator_table, numerator_table;

c) rif40_num_denom VIEW of valid numerator and denominator pairs

CREATE OR REPLACE VIEW peterh.rif40_num_denom
AS
WITH n AS (
        SELECT geography, numerator_table, numerator_description, automatic, theme_description
          FROM (
                SELECT g.geography, n.table_name AS numerator_table, n.description AS numerator_description, n.automatic,
                       t.description AS theme_description
                  FROM rif40_geographies g, rif40_tables n, rif40_health_study_themes t
                 WHERE n.isnumerator = 1
                   AND n.automatic   = 1
                   AND rif40_sql_pkg.rif40_is_object_resolvable(n.table_name) = 1
                   AND n.theme       = t.theme) n1
         WHERE rif40_sql_pkg.rif40_num_denom_validate(n1.geography, n1.numerator_table) = 1
), d AS (
        SELECT geography, denominator_table, denominator_description
          FROM (
                        SELECT g.geography, d.table_name AS denominator_table, d.description AS denominator_description
                          FROM rif40_geographies g, rif40_tables d
                         WHERE d.isindirectdenominator = 1
                           AND d.automatic             = 1
                           AND rif40_sql_pkg.rif40_is_object_resolvable(d.table_name) = 1) d1
        WHERE rif40_sql_pkg.rif40_num_denom_validate(d1.geography, d1.denominator_table) = 1
          AND rif40_sql_pkg.rif40_auto_indirect_checks(d1.denominator_table) IS NULL
)
SELECT n.geography,
       n.numerator_table,
       n.numerator_description,
       n.theme_description,
       d.denominator_table,
       d.denominator_description,
       n.automatic
  FROM d, n
 WHERE n.geography = d.geography
UNION
SELECT g.geography,
       g.numerator_table,
       n.description AS numerator_description,
       ''Local user theme'' AS theme_description,
       g.denominator_table,
       d.description AS denominator_description,
       0 automatic
  FROM t_rif40_num_denom g
        LEFT OUTER JOIN rif40_tables n ON (n.table_name = g.numerator_table)
        LEFT OUTER JOIN rif40_tables d ON (d.table_name = g.denominator_table)
 ORDER BY 1, 2, 4;

d) Temporary study and comparision area tables.  Used to speed up extracts. On Postgres they are not global and need to be created for each session

CREATE GLOBAL TEMPORARY TABLE g_rif40_study_areas (
        study_id                INTEGER         NOT NULL,
        area_id                 VARCHAR(300)    NOT NULL,
        band_id                 INTEGER) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE g_rif40_comparison_areas (
        study_id                INTEGER         NOT NULL,
        area_id                 VARCHAR(300)    NOT NULL) ON COMMIT PRESERVE ROWS;

e) Revision control view

CREATE OR REPLACE VIEW peterh.rif40_user_version AS SELECT CAST(''1.10'' AS numeric) AS user_schema_revision;

f) Study status table required by Java middleware

CREATE TABLE study_status (
  study_id 			integer NOT NULL,
  study_state 		character varying NOT NULL,
  creation_date 	timestamp without time zone NOT NULL,
  ith_update 		serial NOT NULL,
  message 			character varying(255));
  
';

\df rif40_sql_pkg.rif40_startup

--
-- Eof
