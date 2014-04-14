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
-- Rapid Enquiry Facility (RIF) - Create PG psql code (SQL and Oracle compatibility processing)
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

\echo Creating PG psql code (SQL and Oracle compatibility processing)...

CREATE OR REPLACE VIEW rif40_sql_pkg.user_role_privs
AS
SELECT UPPER(role_name) AS granted_role 
  FROM information_schema.enabled_roles;
COMMENT ON VIEW rif40_sql_pkg.user_role_privs IS 'All roles granted to user';
COMMENT ON COLUMN rif40_sql_pkg.user_role_privs.granted_role IS 'Role granted to user. warning this is in uppercase so the roles are compatible with the Oracle uppercase naming convention';

\dv rif40_sql_pkg.user_role_privs

CREATE OR REPLACE FUNCTION rif40_sql_pkg._print_table_size(table_name VARCHAR)
RETURNS BIGINT
SECURITY INVOKER
AS $func$
/*
Function: 	_print_table_size()
Parameters:	Table name
Returns:	Size of table and indexes in bytes 
Description:	Print size of table nicely
 */
DECLARE
	t_size BIGINT;
	rel_size BIGINT;
BEGIN
	t_size:=pg_table_size(table_name);
	rel_size:=pg_total_relation_size(table_name);
	PERFORM rif40_log_pkg.rif40_log('INFO', '_print_table_size', 'Size of table %: %; indexes: %', 
		table_name::VARCHAR				/* Table name */,
		pg_size_pretty(t_size)::VARCHAR			/* Size of table */,
		pg_size_pretty(rel_size-t_size)::VARCHAR	/* Size of indexes */);
--
	RETURN rel_size;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg._print_table_size(VARCHAR) IS 'Function: 	_print_table_size()
Parameters:	Table name
Returns:	Size of table in bytes 
Description:	Print size of table nicely';

CREATE OR REPLACE FUNCTION rif40_sql_pkg.does_role_exist(username VARCHAR)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
/*
Function: 	does_role_exist()
Parameters:	Username
Returns:	TRUE or FALSE 
Description:	Does the role exist

To prevent errors like this:

rif40=> \COPY t_rif40_studies FROM '../sahsuv3_v4/data/t_rif40_studies.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
ERROR:  42704: role "LINDAB@PRIVATE.NET" does not exist
CONTEXT:  PL/pgSQL function "trigger_fct_t_rif40_studies_checks" line 127 at FETCH
COPY t_rif40_studies, line 1: "1046,LINDAB@PRIVATE.NET,EW01,SAHSU,Enter study name here,,,,S1046_EXTRACT,S1046_MAP,2013-03-22 17:15..."
LOCATION:  get_role_oid, acl.c:4822

 */
DECLARE
	c1_umors CURSOR(l_username VARCHAR) FOR
		SELECT rolname FROM pg_roles WHERE rolname = l_username;
	c1_rec RECORD;
BEGIN
	OPEN c1_umors(username);	
	FETCH c1_umors INTO c1_rec;
	CLOSE c1_umors;
--
	IF c1_rec.rolname = username THEN
		RETURN TRUE;
	ELSE
		RETURN FALSE;
	END IF;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg.does_role_exist(VARCHAR) IS 'Function: 	does_role_exist()
Parameters:	Username
Returns:	TRUE or FALSE 
Description:	Does the role exist

To prevent errors like this:

rif40=> \COPY t_rif40_studies FROM ''../sahsuv3_v4/data/t_rif40_studies.csv'' WITH (FORMAT csv, QUOTE ''"'', ESCAPE ''\\'');
ERROR:  42704: role "LINDAB@PRIVATE.NET" does not exist
CONTEXT:  PL/pgSQL function "trigger_fct_t_rif40_studies_checks" line 127 at FETCH
COPY t_rif40_studies, line 1: "1046,LINDAB@PRIVATE.NET,EW01,SAHSU,Enter study name here,,,,S1046_EXTRACT,S1046_MAP,2013-03-22 17:15..."
LOCATION:  get_role_oid, acl.c:4822';

CREATE OR REPLACE FUNCTION rif40_sql_pkg.is_rif40_user_manager_or_schema()
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
/*

Function: 	is_rif40_user_manager_or_schema()
Parameters:	None
Returns:	TRUE or FALSE 
Description:	Is user rif40 or does the user have the rif_user or rif_manager role?
		The user postgres is never a RIF_USER
 */
DECLARE
BEGIN
	IF USER = 'rif40' THEN
		RETURN TRUE;
	ELSIF USER = 'postgres' THEN
		RETURN FALSE;
	ELSIF pg_has_role(USER, 'rif_user', 'USAGE') THEN
		RETURN TRUE;
	ELSIF pg_has_role(USER, 'rif_manager', 'USAGE') THEN
		RETURN TRUE;
	ELSE
		RETURN FALSE;
	END IF;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg.is_rif40_user_manager_or_schema() IS 'Function: 	is_rif40_user_manager_or_schema()
Parameters:	None
Returns:	TRUE or FALSE 
Description:	Is user rif40 or does the user have the rif_user or rif_manager role?
		The user postgres is never a RIF_USER';

\df rif40_sql_pkg.is_rif40_user_manager_or_schema

CREATE OR REPLACE FUNCTION rif40_sql_pkg.is_rif40_manager_or_schema()
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
/*

Function: 	is_rif40_manager_or_schema()
Parameters:	None
Returns:	TRUE or FALSE 
Description:	Is user rif40 or does the user have the rif_manager role?
 */
DECLARE
BEGIN
	IF USER = 'rif40' THEN
		RETURN TRUE;
	ELSIF pg_has_role(USER, 'rif_manager', 'USAGE') THEN
		RETURN TRUE;
	ELSE
		RETURN FALSE;
	END IF;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.is_rif40_manager_or_schema() IS 'Function: 	is_rif40_manager_or_schema()
Parameters:	None
Returns:	TRUE or FALSE 
Description:	Is user rif40 or does the user have the rif_manager role?';

\df rif40_sql_pkg.is_rif40_manager_or_schema

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_startup()
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_startup()
Parameters:	None
Returns:	1 
Description:	Startup functions - for calling from /usr/local/pgsql/etc/psqlrc
		Postgres has no ON-LOGON trigger
		Java users will need to run this first
		Check 1FDW functionality enabled (RIF40_PARAMETER FDWServerName), settings, FOREIGN SERVER setup OK 
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

 */
DECLARE
	c1 CURSOR FOR
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
		  FROM rif40_user_version;
	c4 CURSOR FOR					/* Schema search path */
		SELECT name, REPLACE(setting, USER||', ', '') AS setting, reset_val
	 	  FROM pg_settings
		 WHERE name = 'search_path';
	c5 CURSOR FOR					/* FDW Settings in RIF40_PARAMETERS */
		SELECT a.param_value AS fdwservername,
		       b.param_value AS fdwservertype,
		       c.param_value AS fdwdbserver
		  FROM rif40_parameters a, rif40_parameters b, rif40_parameters c
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
			  FROM rif40_num_denom_errors
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
			LEFT OUTER JOIN rif40_fdw_tables f ON (c.table_or_view = LOWER(f.table_name))
			LEFT OUTER JOIN b ON (b.foreign_table = c.table_or_view)
			LEFT OUTER JOIN a ON (c.table_or_view = LOWER(a.numerator_table))
		 ORDER BY 1;
	c8 CURSOR FOR					/* Obsolete FDW tables i.e. those no longer in RIF40_NUM_DENOM_ERRORS */
		WITH a AS (
			SELECT numerator_table, denominator_table
			  FROM rif40_num_denom_errors
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
		  FROM c, rif40_fdw_tables b
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
--
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
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
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
	OPEN c2('rif40_user_version');
	FETCH c2 INTO c2_rec;
	IF c2_rec.table_or_view = 'rif40_user_version' THEN
		rif40_user_version:=TRUE;
	END IF;
	CLOSE c2;
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
	IF NOT g_rif40_study_areas THEN
--		sql_stmt:='DROP TABLE IF EXISTS g_rif40_study_areas';
--		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE GLOBAL TEMPORARY TABLE g_rif40_study_areas ('||E'\n'||
			E'\t'||'study_id		INTEGER 	NOT NULL,'||E'\n'||
			E'\t'||'area_id			VARCHAR(300) 	NOT NULL,'||E'\n'||
			E'\t'||'band_id			INTEGER) ON COMMIT PRESERVE ROWS';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON TABLE g_rif40_study_areas 		IS ''Local session cache of links study areas and bands for a given study. Created for high performance in extracts.''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.study_id 	IS ''Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.area_id 	IS ''An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='COMMENT ON COLUMN g_rif40_study_areas.band_id 	IS ''A band allocated to the area''';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_startup', 'Created temporary table: g_rif40_study_areas');
		i:=i+1;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_startup', 'Temporary table: g_rif40_study_areas exists');
	END IF;
	IF NOT g_rif40_comparison_areas THEN
--		sql_stmt:='DROP TABLE IF EXISTS g_rif40_comparison_areas';
--		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		sql_stmt:='CREATE GLOBAL TEMPORARY TABLE g_rif40_comparison_areas ('||E'\n'||
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

COMMENT ON FUNCTION rif40_sql_pkg.rif40_startup() IS 'Function: 	rif40_startup()
Parameters:	None
Returns:	1 
Description:	Startup functions - for calling from /usr/local/pgsql/etc/psqlrc
		Postgres has no ON-LOGON trigger
		Java users will need to run this first
		Check 1FDW functionality enabled (RIF40_PARAMETER FDWServerName), settings, FOREIGN SERVER setup OK 
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
';

\df rif40_sql_pkg.rif40_startup

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_create_fdw_table(numerator_table VARCHAR, total_geographies INTEGER, drop_objects BOOLEAN, fdwservername VARCHAR, fdwservertype VARCHAR) 
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_create_fdw_table()
Parameters: 	numerator_table, total_geographies, drop objects (boolean), FDW server name, FDW server type
Returns: 	Nothing
Description:	Attmept if needed to create FDW tables for potential FDW tables i.e. those numerators in RIF40_NUM_DENOM_ERRORS with no local table of the same name 

		ONLY SUPPORTS NUMERATORS - DENOMINATORS ARE EXPECTED TO ALWAYS BE LOCAL
		ONLY SUPPORTS TABLES AT PRESENT - MAY SUPPORT VIEWS AND MATERIALIZED VIEWS IN FUTURE
 */
DECLARE
	c2cfdwt CURSOR(l_table_or_view VARCHAR) FOR 	
		SELECT a.relname AS table_or_view				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a, pg_namespace n 
		 WHERE b.ftrelid  = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND n.nspname  = USER
		   AND n.oid      = a.relnamespace
		   AND a.relname  = LOWER(l_table_or_view);
	c3cfdwt CURSOR(l_table_or_view VARCHAR) FOR 	
		SELECT *
		  FROM fdw_all_tables
		 WHERE table_name = UPPER(l_table_or_view);
	c4cfdwt CURSOR(l_table_or_view VARCHAR) FOR
		SELECT table_name, column_name,
		       CASE
				WHEN data_type = 'VARCHAR2'		 		THEN 'varchar('||data_length||')'
				WHEN data_type = 'CHAR' 				THEN 'char('||data_length||')'
				WHEN data_type = 'NUMBER' AND data_scale = 0 		THEN 'integer'
				WHEN data_type = 'NUMBER' AND data_scale != 0 		THEN 'numeric('||data_precision||','||data_scale||')'
				WHEN data_type = 'NUMBER' AND data_scale IS NULL 	THEN 'double precision'
				WHEN data_type = 'DATE' 				THEN 'timestamp'
				WHEN data_type LIKE 'TIMESTAMP(%) WITH TIME ZONE' 	THEN 'timestamp'
				WHEN data_type LIKE 'TIMESTAMP(%)' 			THEN 'timestamp'
				ELSE NULL	/* NEED TO RAISE AN ERROR HERE - IT WILL CAUSE ONE LATER */
		       END data_defn, data_type, data_length, data_scale, data_precision, column_id, comments
		  FROM fdw_all_tab_columns
		 WHERE table_name  = UPPER(l_table_or_view)
		 ORDER BY column_id;
	c5cfdwt CURSOR(l_table_or_view VARCHAR) FOR 	
		SELECT *
		  FROM rif40_fdw_tables
		 WHERE table_name = UPPER(l_table_or_view);
--
	c2cfdwt_rec RECORD;
	c3cfdwt_rec RECORD;
	c4cfdwt_rec RECORD;
	c5cfdwt_rec RECORD;
--
	create_fdw BOOLEAN:=FALSE;
	sql_stmt VARCHAR[];
	l_sql_stmt VARCHAR;
	column_list VARCHAR[];
	column_defn VARCHAR[];
	column_comment VARCHAR[];
	test_column VARCHAR;
	test VARCHAR;
--
	c_idx INTEGER:=0;
	j INTEGER:=0;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Check if table exists
--
	OPEN c2cfdwt(numerator_table);
	FETCH c2cfdwt INTO c2cfdwt_rec;
	CLOSE c2cfdwt;
--
-- Get records from FDW_ALL_TABLES
--
	OPEN c3cfdwt(numerator_table);
	FETCH c3cfdwt INTO c3cfdwt_rec;
	CLOSE c3cfdwt;
--
-- Get records from RIF40_FDW_TABLES
--
	OPEN c5cfdwt(numerator_table);
	FETCH c5cfdwt INTO c5cfdwt_rec;
	CLOSE c5cfdwt;
--
	IF c2cfdwt_rec.table_or_view IS NOT NULL /* Already exists */ AND drop_objects THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_table', 'Re-creating foreign data wrapper table: %', 
			c2cfdwt_rec.table_or_view::VARCHAR);
		create_fdw:=TRUE;
		sql_stmt[1]:='DROP FOREIGN TABLE '||USER||'.'||LOWER(numerator_table);
	ELSIF c2cfdwt_rec.table_or_view IS NOT NULL /* Already exists */ AND NOT drop_objects AND c5cfdwt_rec.table_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_table', 'Re-creating foreign data wrapper table: %; no entry in RIF40_FDW_TABLES', 
			c2cfdwt_rec.table_or_view::VARCHAR);
		create_fdw:=TRUE;
		sql_stmt[1]:='DROP FOREIGN TABLE '||USER||'.'||LOWER(numerator_table);
	ELSIF c2cfdwt_rec.table_or_view IS NOT NULL /* Already exists */ AND NOT drop_objects THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_table', 'Foreign data wrapper table: % exists; create status: %, date: %', 
			c2cfdwt_rec.table_or_view::VARCHAR, c5cfdwt_rec.create_status::VARCHAR, c5cfdwt_rec.date_created::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_table', 'Creating foreign data wrapper table: %', 
			LOWER(numerator_table)::VARCHAR);
		create_fdw:=TRUE;
	END IF;
--
-- If the table needs to be created, check it is accessible remotely
--
	IF create_fdw THEN
--
		IF c3cfdwt_rec.table_name IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_create_fdw_table', 'Cannot find numerator table % in remote FDW_ALL_TABLES on % server %', 
				LOWER(numerator_table)::VARCHAR, fdwservertype::VARCHAR, fdwservername::VARCHAR);
			create_fdw:=FALSE;
		ELSIF STRPOS(c3cfdwt_rec.owner, '@') > 1 /* Kerberos user */ THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_fdw_table', 'Found numerator table "%".% in remote FDW_ALL_TABLES on % server %', 
				c3cfdwt_rec.owner::VARCHAR, c3cfdwt_rec.table_name::VARCHAR, fdwservertype::VARCHAR, fdwservername::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_fdw_table', 'Found numerator table %.% in remote FDW_ALL_TABLES on % server %', 
				c3cfdwt_rec.owner::VARCHAR, c3cfdwt_rec.table_name::VARCHAR, fdwservertype::VARCHAR, fdwservername::VARCHAR);
		END IF;
	END IF;
--
-- OK, so get list of columns
--
	IF create_fdw THEN
		l_sql_stmt:='CREATE FOREIGN TABLE '||USER||'.'||LOWER(numerator_table)||' ('||E'\n';
		FOR c4cfdwt_rec IN c4cfdwt(numerator_table) LOOP
			column_list[c4cfdwt_rec.column_id]:=c4cfdwt_rec.column_name;
			column_defn[c4cfdwt_rec.column_id]:=LOWER(c4cfdwt_rec.column_name)||E'\t'||c4cfdwt_rec.data_defn;
			column_comment[c4cfdwt_rec.column_id]:=c4cfdwt_rec.comments;
		END LOOP;
		FOR i IN array_lower(column_list, 1) .. array_upper(column_list, 1) LOOP
			IF column_defn[i] IS NOT NULL THEN
				c_idx:=c_idx+1;
				IF c_idx = 1 THEN
					test_column:=column_list[i];
					l_sql_stmt:=l_sql_stmt||E'\t'||column_defn[i];
				ELSE
					l_sql_stmt:=l_sql_stmt||','||E'\n'||E'\t'||column_defn[i];
				END IF;
			END IF;
		END LOOP;
		IF STRPOS(c3cfdwt_rec.owner, '@') > 1 /* Kerberos user */ THEN
			l_sql_stmt:=l_sql_stmt||E'\n'||') SERVER '||fdwservername||' OPTIONS (SCHEMA ''"'||c3cfdwt_rec.owner||'"'', TABLE '''||c3cfdwt_rec.table_name||''')';
		ELSE
			l_sql_stmt:=l_sql_stmt||E'\n'||') SERVER '||fdwservername||' OPTIONS (SCHEMA '''||c3cfdwt_rec.owner||''', TABLE '''||c3cfdwt_rec.table_name||''')';
		END IF;
		sql_stmt[2]:=l_sql_stmt;
		FOR i IN array_lower(column_list, 1) .. array_upper(column_list, 1) LOOP
			sql_stmt[array_upper(sql_stmt, 1)+1]:='COMMENT ON FOREIGN TABLE '||USER||'.'||LOWER(numerator_table)||' IS '''||
				REPLACE(column_comment[i], '''', ''''||'''' /* Escape comments */)||'''';
		END LOOP;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		j:=1;
	END IF;

--
-- Test
--
	IF test_column IS NULL THEN /* Guess year */
		test:=rif40_sql_pkg.rif40_fdw_table_select_test(LOWER(numerator_table), 'YEAR', fdwservername, fdwservertype);
	ELSE
		test:=rif40_sql_pkg.rif40_fdw_table_select_test(LOWER(numerator_table), test_column, fdwservername, fdwservertype);
	END IF;
	IF test IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_fdw_table', 'Foreign data wrapper numerator table: % tested OK', 
			LOWER(numerator_table)::VARCHAR);
		PERFORM rif40_sql_pkg.rif40_update_fdw_tables(
			UPPER(numerator_table) /* table name */, 'C' /* Create status */, NULL::varchar /* Error message */, 1 /* Rowtest passed */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_create_fdw_table', 'returned: %', 
			test::VARCHAR);
		l_sql_stmt:='DROP FOREIGN TABLE '||USER||'.'||LOWER(numerator_table);
		PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		PERFORM rif40_sql_pkg.rif40_update_fdw_tables(
			UPPER(numerator_table) /* table name */, 'E' /* Create status */, test /* Error message */, 0 /* Rowtest NOT passed */);
--
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_create_fdw_table', 'Foreign data wrapper numerator table: % test SELECT returned error', 
			LOWER(numerator_table)::VARCHAR);
	END IF;
--
	RETURN j;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		error_message:='rif40_create_fdw_table() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'for FDW '||
			LOWER(numerator_table)::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_create_fdw_table', 'Caught exception: % [IGNORED]', 
			error_message::VARCHAR);
		RAISE INFO '2: %', error_message;
		PERFORM rif40_sql_pkg.rif40_update_fdw_tables(
			UPPER(numerator_table) /* table name */, 'N' /* Create status */, error_message /* Error message */, 0 /* Rowtest NOT passed */);
		BEGIN
			l_sql_stmt:='DROP FOREIGN TABLE '||USER||'.'||LOWER(numerator_table);
			PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		EXCEPTION
			WHEN others THEN NULL;
		END;
--		RAISE /* the original error */;
--
-- Or you could just return (which is what we will do in the long run)
--
		RETURN 0;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_create_fdw_table(VARCHAR, INTEGER, BOOLEAN, VARCHAR, VARCHAR) IS 'Function: 	rif40_create_fdw_table()
Parameters: 	numerator_table, total_geographies, drop objects (boolean), FDW server name, FDW server type
Returns: 	Nothing
Description:	Attmept if needed to create FDW tables for potential FDW tables i.e. those numerators in RIF40_NUM_DENOM_ERRORS with no local table of the same name 

		ONLY SUPPORTS NUMERATORS - DENOMINATORS ARE EXPECTED TO ALWAYS BE LOCAL
		ONLY SUPPORTS TABLES AT PRESENT - MAY SUPPORT VIEWS AND MATERIALIZED VIEWS IN FUTURE';

\df rif40_sql_pkg.rif40_create_fdw_table

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_update_fdw_tables(l_table_name VARCHAR, create_status VARCHAR, error_message VARCHAR, rowtest_passed INTEGER)
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_update_fdw_tables()
Parameters: 	Table name, Create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors), error message, rowtest passed (0/1)
Returns: 	Nothing
Description:	MERGE rif40_fdw_tables

 */
DECLARE
	c1ufdw CURSOR(l_table_name VARCHAR) FOR
		SELECT * 
		  FROM rif40_fdw_tables
		 WHERE table_name = UPPER(l_table_name);
--
	c1ufdw_rec RECORD;
--
	sql_stmt VARCHAR;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
	OPEN c1ufdw(l_table_name);
	FETCH c1ufdw INTO c1ufdw_rec;
	CLOSE c1ufdw;
--
	IF c1ufdw_rec.table_name IS NULL THEN
		sql_stmt:='INSERT INTO t_rif40_fdw_tables (create_status, error_message, rowtest_passed, table_name, username) VALUES ($1, $2, $3, $4, $5)';
	ELSE
		sql_stmt:='UPDATE t_rif40_fdw_tables SET create_status=$1, error_message=$2, rowtest_passed=$3 WHERE table_name=$4 AND username = $5';
	END IF;
	EXECUTE sql_stmt USING create_status, error_message, rowtest_passed, UPPER(l_table_name), USER;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		IF sql_stmt IS NULL THEN
			error_message:='rif40_update_fdw_tables() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL> (Not known, probably CURSOR c1ufdw)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		ELSE
			error_message:='rif40_update_fdw_tables() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL>'||sql_stmt::VARCHAR||E'\n'||'Detail: '||v_detail::VARCHAR;
		END IF;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_update_fdw_tables', 'Caught exception: % [IGNORED]', 
			error_message::VARCHAR);
END;

$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_update_fdw_tables(VARCHAR, VARCHAR, VARCHAR, INTEGER) IS 'Function: 	rif40_update_fdw_tables()
Parameters: 	Table name, Create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors), error message, rowtest passed (0/1)
Returns: 	Nothing
Description:	MERGE rif40_fdw_tables
';

\df rif40_sql_pkg.rif40_update_fdw_tables

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_create_fdw_view(drop_objects BOOLEAN, fdwservername VARCHAR, fdwservertype VARCHAR, l_view VARCHAR, l_column VARCHAR, sql_stmt VARCHAR[]) 
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_create_fdw_view()
Parameters: 	Drop objects (boolean), FDW server name, FDW server type, FDW name for remote view, remote column to be tested, arrays of SQL statements
Returns: 	Nothing
Description:	Create support view if needed (FDW_ALL_TABLES, FDW_ALL_TAB_COLUMNS)

Sse rif40_create_fdw_views() for SQL exmaples

This contains Oracle specific code (i.e. data dictionary items ALL_TABLES, ALL_TAB_COLUMNS owned by SYS)

Test user can select from FDW table name using rif40_sql_pkg.rif40_fdw_table_select_test()

 */
DECLARE
	c2cfdwv CURSOR(l_table_or_view VARCHAR) FOR 	
		SELECT a.relname AS table_or_view				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a, pg_namespace n 
		 WHERE b.ftrelid  = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND n.nspname  = USER
		   AND n.oid      = a.relnamespace
		   AND a.relname  = LOWER(l_table_or_view);
--
	c2cfdwv_rec RECORD;
--
	test VARCHAR;
	i INTEGER:=0;
	l_sql_stmt VARCHAR[]:=sql_stmt;
BEGIN
	OPEN c2cfdwv(l_view);
	FETCH c2cfdwv INTO c2cfdwv_rec;
	CLOSE c2cfdwv;
	IF c2cfdwv_rec.table_or_view IS NOT NULL /* Already exists */ AND drop_objects THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_view', 'Re-creating foreign data wrapper view: %', 
			c2cfdwv_rec.table_or_view::VARCHAR);
		l_sql_stmt[1]:='DROP FOREIGN TABLE '||USER||'.'||l_view;
		PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		i:=1;
	ELSIF c2cfdwv_rec.table_or_view IS NOT NULL /* Already exists */ AND NOT drop_objects THEN
		NULL;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_view', 'Creating foreign data wrapper view: %', 
			l_view::VARCHAR);
		PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		i:=i+1;
	END IF;
--
-- Test
--
	test:=rif40_sql_pkg.rif40_fdw_table_select_test(l_view, l_column, fdwservername, fdwservertype);
	IF test IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_fdw_view', 'Foreign data wrapper view: % tested OK', 
			l_view::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_create_fdw_view', 'returned: %', 
			test::VARCHAR);
		l_sql_stmt:=NULL;
		l_sql_stmt[1]:='DROP FOREIGN TABLE '||USER||'.'||l_view;
		PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_create_fdw_view', 'Foreign data wrapper view: % test SELECT returned error', 
			l_view::VARCHAR);
	END IF;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON  FUNCTION rif40_sql_pkg.rif40_create_fdw_view(BOOLEAN, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) IS 'Function: 	rif40_create_fdw_view()
Parameters: 	Drop objects (boolean), FDW server name, FDW server type, FDW name for remote view, remote column to be tested, arrays of SQL statements
Returns: 	Nothing
Description:	Create support view if needed (FDW_ALL_TABLES, FDW_ALL_TAB_COLUMNS)

Sse rif40_create_fdw_views() for SQL exmaples

This contains Oracle specific code (i.e. data dictionary items ALL_TABLES, ALL_TAB_COLUMNS owned by SYS)

Test user can select from FDW table name using rif40_sql_pkg.rif40_fdw_table_select_test()';

\df rif40_sql_pkg.rif40_create_fdw_view

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_create_fdw_views(drop_objects BOOLEAN, fdwservername VARCHAR, fdwservertype VARCHAR) 
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_create_fdw_views()
Parameters: 	Drop objects (boolean), FDW server name, FDW server type
Returns: 	Nothing
Description:	Create support views if needed (FDW_ALL_TABLES, FDW_ALL_TAB_COLUMNS) using rif40_sql_pkg.rif40_create_fdw_view()

CREATE FOREIGN TABLE fdw_all_tables (
	owner		VARCHAR(30),
	table_name	VARCHAR(30),
	comments	VARCHAR(4000)
) SERVER eph1 OPTIONS (SCHEMA 'SAHSU', TABLE 'RIF_ALL_TABLES');

CREATE FOREIGN TABLE fdw_all_tab_columns (
	owner		VARCHAR(30),
	table_name	VARCHAR(30),
	column_name	VARCHAR(30),
	data_type       VARCHAR(106),
 	data_length     NUMERIC(22,0), 
 	data_precision  NUMERIC(22,0),
 	data_scale      NUMERIC(22,0),
 	column_id       NUMERIC(22,0),
	comments	VARCHAR(4000)
) SERVER eph1 OPTIONS (SCHEMA 'SAHSU', TABLE 'RIF_ALL_TAB_COLUMNS');

This contains Oracle specific code (i.e. data dictionary items ALL_TABLES, ALL_TAB_COLUMNS owned by SYS)

Test user can select from FDW table name

RIF_ALL_TAB_COLUMNS is defined (as user SAHSU) as:

CREATE OR REPLACE VIEW rif_all_tab_columns AS
SELECT t.owner, t.table_name, t.column_name, 
       data_type,
       CAST(data_length AS NUMBER(22,0)) AS data_length,  
       CAST(data_precision AS NUMBER(22,0)) AS data_precision, 
       CAST(data_scale AS NUMBER(22,0)) AS data_scale,
       CAST(column_id AS NUMBER(22,0)) AS column_id, 
       c.comments
  FROM all_tab_columns t
	LEFT OUTER JOIN all_col_comments c ON (c.owner = t.owner AND c.table_name = t.table_name AND c.column_name = t.column_name);
GRANT SELECT ON rif_all_tab_columns TO PUBLIC;
EXECUTE recreate_public_synonym('rif_all_tab_columns');
DESC rif_all_tab_columns
COLUMN data_type FORMAT a20
SELECT  * from rif_all_tab_columns where table_name = 'RIF_CANC_EW_74_ON_ED91';

CREATE OR REPLACE VIEW rif40_all_tables AS
SELECT t.owner, t.table_name, c.comments
  FROM all_tab_columns t
	LEFT OUTER JOIN all_col_comments c ON (c.owner = t.owner AND c.table_name = t.table_name);
GRANT SELECT ON rif40_all_tables TO PUBLIC;
EXECUTE recreate_public_synonym('rif40_all_tables');
DESC rif40_all_tables
SELECT  * from rif40_all_tables where table_name = 'RIF_CANC_EW_74_ON_ED91';

This has been create to define the NUMBER data type exactly (the is a bug in Oracle FDW); and to include the column comments

The ordering of column in Oracle FDW must be the same as in Oracle

 */
DECLARE
	l_view	VARCHAR;
	l_column VARCHAR;
	sql_stmt VARCHAR[];
	i INTEGER:=0;
BEGIN
	l_view:='fdw_all_tables';
	l_column:='table_name';
	sql_stmt[2]:='CREATE FOREIGN TABLE '||USER||'.fdw_all_tables ('||E'\n'||
		E'\t'||'owner		VARCHAR(30),'||E'\n'||
		E'\t'||'table_name	VARCHAR(30),'||E'\n'||
		E'\t'||'comments	VARCHAR(4000)'||E'\n'||
		') SERVER '||fdwservername||' OPTIONS (SCHEMA ''SAHSU'', TABLE ''RIF40_ALL_TABLES'')';
	sql_stmt[3]:='COMMENT ON FOREIGN TABLE '||USER||'.fdw_all_tables IS ''List of remote tables/views/materialized views accessible via foreign data wrapper '||
		fdwservertype||' server '||fdwservername||'. Used to check table exists''';
	sql_stmt[4]:='COMMENT ON COLUMN '||USER||'.fdw_all_tables.owner IS ''Owner of remote table/view/materialized view''';
	sql_stmt[5]:='COMMENT ON COLUMN '||USER||'.fdw_all_tables.table_name IS ''Name of remote table/view/materialized view''';
	sql_stmt[6]:='COMMENT ON COLUMN '||USER||'.fdw_all_tables.comments IS ''Comments''';
--
	i:=i+rif40_sql_pkg.rif40_create_fdw_view(drop_objects, fdwservername, fdwservertype, l_view, l_column, sql_stmt);
--
	l_view:='fdw_all_tab_columns';
	l_column:='table_name';
	sql_stmt:=NULL;
	sql_stmt[2]:='CREATE FOREIGN TABLE '||USER||'.fdw_all_tab_columns ('||E'\n'||
		E'\t'||'owner		VARCHAR(30),'||E'\n'||
		E'\t'||'table_name	VARCHAR(30),'||E'\n'||
		E'\t'||'column_name	VARCHAR(30),'||E'\n'||
		E'\t'||'data_type	VARCHAR(30),'||E'\n'||
		E'\t'||'data_length	NUMERIC(22,0),'||E'\n'||
		E'\t'||'data_precision	NUMERIC(22,0),'||E'\n'||
		E'\t'||'data_scale	NUMERIC(22,0),'||E'\n'||
		E'\t'||'column_id	NUMERIC(22,0),'||E'\n'||
		E'\t'||'comments	VARCHAR(4000)'||E'\n'||
		') SERVER '||fdwservername||' OPTIONS (SCHEMA ''SAHSU'', TABLE ''RIF_ALL_TAB_COLUMNS'')';
	sql_stmt[3]:='COMMENT ON FOREIGN TABLE '||USER||'.fdw_all_tab_columns IS ''List of remote table/view/materialized view columns accessible via foreign data wrapper '||
		fdwservertype||' server '||fdwservername||'. Used to check table exists''';
	sql_stmt[4]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.owner IS ''Owner of remote table/view/materialized view''';
	sql_stmt[5]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.table_name IS ''Name of remote table/view/materialized view''';
	sql_stmt[6]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.column_name IS ''Column name''';
	sql_stmt[7]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.data_type IS ''Data type''';
	sql_stmt[8]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.data_length IS ''Data length''';
	sql_stmt[9]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.data_precision IS ''Data precision''';
	sql_stmt[10]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.data_scale IS ''Data scale''';
	sql_stmt[11]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.column_id IS ''Column id (order)''';
	sql_stmt[12]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.comments IS ''Comments''';
--
	i:=i+rif40_sql_pkg.rif40_create_fdw_view(drop_objects, fdwservername, fdwservertype, l_view, l_column, sql_stmt);
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_create_fdw_views(BOOLEAN, VARCHAR, VARCHAR) IS 'Function: 	rif40_create_fdw_views()
Parameters: 	Drop objects (boolean), FDW server name, FDW server type
Returns: 	Nothing
Description:	Create support views if needed (FDW_ALL_TABLES, FDW_ALL_TAB_COLUMNS) using rif40_sql_pkg.rif40_create_fdw_view()

CREATE FOREIGN TABLE fdw_all_tables (
	owner		VARCHAR(30),
	table_name	VARCHAR(30),
	comments	VARCHAR(4000)
) SERVER eph1 OPTIONS (SCHEMA ''SAHSU'', TABLE ''RIF_ALL_TABLES'');

CREATE FOREIGN TABLE fdw_all_tab_columns (
	owner		VARCHAR(30),
	table_name	VARCHAR(30),
	column_name	VARCHAR(30),
	data_type       VARCHAR(106),
 	data_length     NUMERIC(22,0), 
 	data_precision  NUMERIC(22,0),
 	data_scale      NUMERIC(22,0),
 	column_id       NUMERIC(22,0),
	comments	VARCHAR(4000)
) SERVER eph1 OPTIONS (SCHEMA ''SAHSU'', TABLE ''RIF_ALL_TAB_COLUMNS'');

This contains Oracle specific code (i.e. data dictionary items ALL_TABLES, ALL_TAB_COLUMNS owned by SYS)

Test user can select from FDW table name

RIF_ALL_TAB_COLUMNS is defined (as user SAHSU) as:

CREATE OR REPLACE VIEW rif_all_tab_columns AS
SELECT t.owner, t.table_name, t.column_name, 
       data_type,
       CAST(data_length AS NUMBER(22,0)) AS data_length,  
       CAST(data_precision AS NUMBER(22,0)) AS data_precision, 
       CAST(data_scale AS NUMBER(22,0)) AS data_scale,
       CAST(column_id AS NUMBER(22,0)) AS column_id, 
       c.comments
  FROM all_tab_columns t
	LEFT OUTER JOIN all_col_comments c ON (c.owner = t.owner AND c.table_name = t.table_name AND c.column_name = t.column_name);
GRANT SELECT ON rif_all_tab_columns TO PUBLIC;
EXECUTE recreate_public_synonym(''rif_all_tab_columns'');
DESC rif_all_tab_columns
COLUMN data_type FORMAT a20
SELECT  * from rif_all_tab_columns where table_name = ''RIF_CANC_EW_74_ON_ED91'';

CREATE OR REPLACE VIEW rif40_all_tables AS
SELECT t.owner, t.table_name, c.comments
  FROM all_tab_columns t
	LEFT OUTER JOIN all_col_comments c ON (c.owner = t.owner AND c.table_name = t.table_name);
GRANT SELECT ON rif40_all_tables TO PUBLIC;
EXECUTE recreate_public_synonym(''rif40_all_tables'');
DESC rif40_all_tables
SELECT  * from rif40_all_tables where table_name = ''RIF_CANC_EW_74_ON_ED91'';

This has been create to define the NUMBER data type exactly (the is a bug in Oracle FDW); and to include the column comments

The ordering of column in Oracle FDW must be the same as in Oracle
';

\df rif40_sql_pkg.rif40_create_fdw_views

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_fdw_table_select_test(l_table_name VARCHAR, l_column_name VARCHAR, fdwservername VARCHAR, fdwservertype VARCHAR) 
RETURNS text
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_fdw_table_select_test()
Parameters: 	Table name, column name, FDW server name, FDW server type
Returns: 	Text of error or NULL if all OK
Description:	Test user can select from FDW table name
		
		SQL> SELECT <column name> AS test FROM <table name> LIMIT 1;

		IF 1 row returned, RETURN NULL
		IF any of the inputs are NULL RETURN custom error message
		IF FDW server name is invalid/not accessible RETURN custom error message
		IF SQL error RETURN SQL error message

 */
DECLARE
	c1_st REFCURSOR;
	c6_st CURSOR(l_fdwservername VARCHAR) FOR 		/* FDW server name check */
		SELECT r.rolname AS srvowner, s.srvname
		  FROM pg_foreign_server s, pg_roles r
		 WHERE s.srvname  = l_fdwservername
		   AND s.srvowner = r.oid;
--
	c6_st_rec RECORD;
--
	test VARCHAR;
	sql_stmt VARCHAR;
	error_message VARCHAR:=NULL;
	l_rows INTEGER:=NULL;
--
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- IF any of the inputs are NULL RETURN custom error message
--
	IF l_table_name IS NULL OR l_column_name IS NULL OR fdwservername IS NULL OR fdwservertype IS NULL THEN
		error_message:='rif40_fdw_table_select_test() C209xx: NULL Table name, column name, FDW server name or FDW server type';
	END IF;
	sql_stmt:='SELECT '||l_column_name||'::VARCHAR AS test FROM '||USER||'.'||l_table_name||' LIMIT 1';
--
-- Check access to FDWServerName
--
	IF error_message IS NULL THEN
		OPEN c6_st(fdwservername);
		FETCH c6_st INTO c6_st_rec;
		CLOSE c6_st;
		IF c6_st_rec.srvowner IS NULL THEN
			error_message:='rif40_fdw_table_select_test() FDW functionality disabled - FDWServerName: '||fdwservername::VARCHAR||' not found';
		ELSIF has_server_privilege(c6_st_rec.srvowner, c6_st_rec.srvname, 'USAGE') THEN
			NULL;
		ELSE
			error_message:='rif40_fdw_table_select_test() FDW functionality disabled - no access to FDWServerName: '||c6_st_rec.srvowner::VARCHAR||'.'||c6_st_rec.srvname::VARCHAR;
		END IF;
	END IF;
--
-- Run SELECT test
--
	IF error_message IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_fdw_table_select_test', 'SQL> %;', 
			sql_stmt::VARCHAR);
		OPEN c1_st FOR EXECUTE sql_stmt;
		FETCH c1_st INTO test;
		GET DIAGNOSTICS l_rows = ROW_COUNT;
		CLOSE c1_st;
--
		IF l_rows IS NULL THEN
			error_message:='rif40_fdw_table_select_test() C209xx: NULL ROW_COUNT in: '||E'\n'||'SQL> '||sql_stmt||E'\n'||'for FDW '||
				l_table_name::VARCHAR||'.'||l_column_name::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR;
		ELSIF l_rows = 0 THEN
			error_message:='rif40_fdw_table_select_test() C209xx: 0 ROW_COUNT in: '||E'\n'||'SQL> '||sql_stmt||E'\n'||'for FDW '||
				l_table_name::VARCHAR||'.'||l_column_name::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR;
		ELSIF l_rows > 1 THEN
			error_message:='rif40_fdw_table_select_test() C209xx: Non zero ('||l_rows||') ROW_COUNT in: '||E'\n'||'SQL> '||sql_stmt||E'\n'||'for FDW '||
				l_table_name::VARCHAR||'.'||l_column_name::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR;
		END IF;
	END IF;
--
	IF error_message IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_fdw_table_select_test', 'SQL> %; %', 
			sql_stmt::VARCHAR, error_message::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_fdw_table_select_test', 'OK SQL> %;', 
			sql_stmt::VARCHAR);
	END IF;
--
	RETURN error_message;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		error_message:='rif40_fdw_table_select_test() caught: '||E'\n'||SQLERRM::VARCHAR||' in: '||E'\n'||'SQL> '||sql_stmt::VARCHAR||E'\n'||'for FDW '||
			l_table_name::VARCHAR||'.'||l_column_name::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR||E'\n'||'Detail: '||v_detail::VARCHAR;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_fdw_table_select_test', 'Caught exception: % [IGNORED]', 
			error_message::VARCHAR);
		RETURN error_message;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_fdw_table_select_test(VARCHAR, VARCHAR, VARCHAR, VARCHAR)  IS 'Function: 	rif40_fdw_table_select_test()
Parameters: 	Table name, column name, FDW server name, FDW server type
Returns: 	Text of error or NULL if all OK
Description:	Test user can select from FDW table name
		
		SQL> SELECT <column name> AS test FROM <table name> LIMIT 1;

		IF 1 row returned, RETURN NULL
		IF any of the inputs are NULL RETURN custom error message
		IF FDW server name is invalid/not accessible RETURN custom error message
		IF SQL error RETURN SQL error message
';

\df rif40_sql_pkg.rif40_fdw_table_select_test

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_get_function_arg_types(arg_type_list oid[]) 
RETURNS text
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_get_function_arg_types()
Parameters: 	arg_type_list (pg_types.oid)
Returns: 	Text
Description:	Returns function signature e.g (character varying, character varying)

		Similar to pg_get_function_identity_arguments() without the function names
		For use with pg_proc
 */
DECLARE
	ret TEXT;
	j INTEGER:=0;
BEGIN
	ret:='(';
--
	IF arg_type_list IS NOT NULL THEN
		FOR i IN array_lower(arg_type_list, 1) .. array_upper(arg_type_list, 1) LOOP
			IF i = array_lower(arg_type_list, 1) THEN
				ret:=ret||format_type(arg_type_list[i], NULL);
			ELSE
				ret:=ret||', '||format_type(arg_type_list[i], NULL);
			END IF;
			j:=j+1;
		END LOOP;
	END IF;
--
	ret:=ret||')';
	PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_get_function_arg_types', 'args: %, signature: %', 
		j::VARCHAR, ret::VARCHAR);
	RETURN ret;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_get_function_arg_types(oid[]) IS 'Function: 	rif40_get_function_arg_types()
Parameters: 	arg_type_list (pg_types.oid)
Returns: 	Text
Description:	Returns function signature e.g (character varying, character varying)

		Similar to pg_get_function_identity_arguments() without the function names
		For use with pg_proc';

\df rif40_sql_pkg.rif40_get_function_arg_types

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_num_denom_validate(l_geography VARCHAR, l_table_name VARCHAR)
RETURNS integer
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_num_denom_validate()
Parameters: 	geography, table/view/foreign table name
Returns: 	1 - table/view/foreign table has all geolevels present in geography, 0 - table/view/foreign table has some/all geolevels missing in geography
Description:	Validate numerator or denominator geolevels

 */
DECLARE
	c1ndv CURSOR(l_geography VARCHAR, l_owner VARCHAR, l_table VARCHAR) FOR
		WITH all_tab_columns AS (
			SELECT UPPER(a.tablename) AS tablename, UPPER(b.attname) AS columnname, UPPER(schemaname) AS schemaname	/* Tables */
			  FROM pg_tables a, pg_attribute b, pg_class c
			 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.tablename
			   AND c.relkind    = 'r' /* Relational table */
			   AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
			 UNION
			SELECT UPPER(a.viewname) AS tablename, UPPER(b.attname) AS columnname, UPPER(schemaname) AS schemaname	/* Views */
			  FROM pg_views a, pg_attribute b, pg_class c
			 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.viewname
			   AND c.relkind    = 'v' /* View */
			 UNION
			SELECT UPPER(a.relname) AS tablename, UPPER(d.attname) AS columnname, UPPER(n.nspname) AS schemaname				/* User FDW foreign tables */
			  FROM pg_foreign_table b, pg_roles r, pg_attribute d, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE b.ftrelid  = a.oid
			   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relowner = r.oid
			   AND n.nspname  = USER
			   AND a.oid      = d.attrelid
		)
		SELECT COUNT(l.geolevel_name) total_geolevels,
    		       COUNT(c.columnname) total_columns 
		  FROM t_rif40_geolevels l
			LEFT OUTER JOIN all_tab_columns c ON (c.schemaname = l_owner AND 
				c.tablename = l_table   AND c.columnname = l.geolevel_name)
		 WHERE geography  = l_geography
   		   AND resolution = 1;
	c1_rec RECORD;
--
	l_owner	VARCHAR:=NULL;
	l_table	VARCHAR:=NULL;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_num_denom_validate', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- If inputs are NULL return 0
--
	IF l_geography IS NULL OR l_table_name IS NULL THEN
		RETURN 0;
	END IF;
--
-- Resolve schema owner of table (oracle version of this has to deal with synonyms as well)
--
	l_owner:=UPPER(rif40_sql_pkg.rif40_object_resolve(l_table_name));
--
-- Check geolevels
--
	IF l_owner IS NOT NULL THEN
		OPEN c1ndv(l_geography, l_owner, l_table_name);
		FETCH c1ndv INTO c1_rec;
		CLOSE c1ndv;
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_num_denom_validate', '[%,%.%] total_geolevels (in geography): %; total_columns (in table/view/foreign table; matching geolevels columns): %', 
			l_geography::VARCHAR, l_owner::VARCHAR, l_table_name::VARCHAR, c1_rec.total_geolevels::VARCHAR, c1_rec.total_columns::VARCHAR);
		IF c1_rec.total_geolevels = c1_rec.total_columns THEN 
			RETURN 1;		/* Validated */
		END IF;
	END IF;
--
	RETURN 0;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON  FUNCTION rif40_sql_pkg.rif40_num_denom_validate(VARCHAR, VARCHAR) IS 'Function: 	rif40_num_denom_validate()
Parameters: 	geography, table/view/foreign table name
Returns: 	1 - table/view/foreign table has all geolevels present in geography, 0 - table/view/foreign table has some/all geolevels missing in geography
Description:	Validate numerator or denominator geolevels';

\df rif40_sql_pkg.rif40_num_denom_validate

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_is_object_resolvable(l_table_name VARCHAR)
RETURNS integer
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_is_object_resolvable()
Parameters:	Table/view name
Returns: 	1 - resolvable and accessible, 0 - NOT
Description:	Is object resolvable?

Search search path for table/view/foreign table; check resolvable

Will need OracleFDW objects to check remote access

 */
DECLARE
	c1 CURSOR FOR
		SELECT REPLACE(regexp_split_to_table(setting, E'\\s+'), ',', '') AS schemaname
	 	  FROM pg_settings
		 WHERE name = 'search_path';
	c2 CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR
		SELECT schemaname, tablename
		  FROM pg_tables
		 WHERE schemaname = l_schema
		   AND tablename  = LOWER(l_table)
		 UNION
		SELECT schemaname, viewname AS tablename
		  FROM pg_views
		 WHERE schemaname = l_schema
		   AND viewname   = LOWER(l_table)
	 	 UNION
		SELECT n.nspname schemaname, a.relname tablename				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.ftrelid = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND n.nspname  = USER
		   AND n.nspname  = l_schema
		   AND a.relname  = LOWER(l_table);
--
	c1_rec RECORD;
	c2_rec RECORD;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_is_object_resolvable', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- If inputs are NULL return 0
--
	IF l_table_name IS NULL THEN
		RETURN 0;
	END IF;
--
	FOR c1_rec IN c1 LOOP
		OPEN c2(c1_rec.schemaname, l_table_name);
		FETCH c2 INTO c2_rec;
--
		IF c2_rec.tablename IS NOT NULL AND has_schema_privilege(USER, c1_rec.schemaname, 'USAGE') AND 
		   has_table_privilege(USER, c1_rec.schemaname||'.'||c2_rec.tablename, 'SELECT') /* or view or foreign table */ THEN
			CLOSE c2;
			RETURN 1;
		ELSIF c2_rec.tablename IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_is_object_resolvable', 'No table/view/foreign table: %.%', 
				c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
		ELSIF c2_rec.tablename IS NOT NULL AND NOT has_schema_privilege(USER, c1_rec.schemaname, 'USAGE') THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_is_object_resolvable', 'No access to schema % for table/view/foreign table: %.%', 
				c1_rec.schemaname::VARCHAR, c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_is_object_resolvable', 'No access to: %.%', 
				c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
		END IF;
		CLOSE c2;
	END LOOP;
--
	RETURN 0;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_is_object_resolvable(VARCHAR) IS 'Function: 	rif40_is_object_resolvable()
Parameters:	Table/view name
Returns: 	1 - resolvable and accessible, 0 - NOT
Description:	Is object resolvable?

Search search path for table/view/foreign table; check resolvable

Will need OracleFDW objects to check remote access';

\df rif40_sql_pkg.rif40_is_object_resolvable

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_object_resolve(l_table_name VARCHAR)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_object_resolve()
Parameters:	Table/view name (forced into lower case)
Returns: 	Schema owner for first object found with name in search path
Description:	Is object resolvable?

Search search path for table/view/foreign table

 */
DECLARE
	c1or CURSOR FOR
		SELECT REPLACE(regexp_split_to_table(setting, E'\\s+'), ',', '') AS schemaname
	 	  FROM pg_settings
		 WHERE name = 'search_path';
	c2or CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR
		SELECT schemaname, tablename
		  FROM pg_tables
		 WHERE schemaname = l_schema
		   AND tablename  = LOWER(l_table)
		 UNION
		SELECT schemaname, viewname AS tablename
		  FROM pg_views
		 WHERE schemaname = l_schema
		   AND viewname   = LOWER(l_table)
	 	 UNION
		SELECT n.nspname schemaname, a.relname tablename				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.ftrelid = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND n.nspname  = l_schema
		   AND a.relname  = LOWER(l_table);
--
	c1_rec RECORD;
	c2_rec RECORD;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_object_resolve', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- If inputs are NULL return 0
--
	IF l_table_name IS NULL THEN
		RETURN NULL;
	END IF;
--
	FOR c1_rec IN c1or LOOP
		OPEN c2or(c1_rec.schemaname, l_table_name);
		FETCH c2or INTO c2_rec;
--
		IF c2_rec.tablename IS NOT NULL AND has_schema_privilege(USER, c1_rec.schemaname, 'USAGE') /* or view or foreign table */ THEN
			CLOSE c2or;
			RETURN c1_rec.schemaname;
		ELSIF c2_rec.tablename IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_object_resolve', 'No table/view/foreign table: %.%', 
				c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
		ELSIF c2_rec.tablename IS NOT NULL AND NOT has_schema_privilege(USER, c1_rec.schemaname, 'USAGE') THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_object_resolve', 'No access to schema % for table/view/foreign table: %.%', 
				c1_rec.schemaname::VARCHAR, c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_object_resolve', 'No access to: %.%', 
				c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
		END IF;
		CLOSE c2or;
	END LOOP;
--
	RETURN NULL;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_object_resolve(VARCHAR) IS 'Function: 	rif40_object_resolve()
Parameters:	Table/view name
Returns: 	Schema owner for first object found with name in search path
Description:	Is object resolvable?

Search search path for table/view/foreign table
';

\df rif40_sql_pkg.rif40_object_resolve

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(l_table_name VARCHAR)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_auto_indirect_checks()
Parameters:	table name
Returns: 	NULL if not found or not an indirect denominator, geography and counts
Description:	Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). 
		A user specific T_RIF40_NUM_DENOM is supplied for other combinations. 
		Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.
 */
DECLARE
	c1 CURSOR(l_table VARCHAR) IS
		SELECT isindirectdenominator, isnumerator, automatic
		  FROM rif40_tables
		 WHERE l_table = table_name;
	c2 CURSOR(l_table VARCHAR) IS
		WITH valid_geog AS (
			SELECT g.geography
			  FROM rif40_geographies g
			 WHERE rif40_sql_pkg.rif40_num_denom_validate(g.geography, l_table) = 1
		)
		SELECT valid_geog.geography geography, COUNT(t.table_name) total_denominators
		  FROM rif40_tables t, valid_geog 
		 WHERE t.table_name            != l_table
		   AND t.isindirectdenominator = 1
   		   AND t.automatic             = 1
		   AND rif40_sql_pkg.rif40_num_denom_validate(valid_geog.geography, t.table_name) = 1
		 GROUP BY valid_geog.geography
		 ORDER BY 1;
	c3 CURSOR(l_geography VARCHAR, l_table VARCHAR) IS
		SELECT t.table_name 
		  FROM rif40_tables t 
		 WHERE t.table_name != l_table
		   AND t.isindirectdenominator = 1
   		   AND t.automatic             = 1
		   AND rif40_sql_pkg.rif40_num_denom_validate(l_geography, t.table_name) = 1
		 ORDER BY 1;
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
--
	msg 		VARCHAR:=NULL;
	dmsg 		VARCHAR:=NULL;
	i		INTEGER:=0;
	j		INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_auto_indirect_checks', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- If inputs are NULL return NULL 
--
	IF l_table_name IS NULL THEN
		RETURN NULL;
	END IF;
--
-- automatic indirect denominator checks
--
	OPEN c1(l_table_name);
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.automatic = 0 OR c1_rec.isindirectdenominator != 1 OR c1_rec.isnumerator = 1 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_auto_indirect_checks', 'table: % is not an automatic indirect denominator', 
			l_table_name::VARCHAR);
		RETURN NULL;
	END IF;
--
-- Check object is resolvable
--
	IF rif40_sql_pkg.rif40_is_object_resolvable(l_table_name) = 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_auto_indirect_checks', 'table: % not resolvable', 
			l_table_name::VARCHAR);
		RETURN NULL;
	END IF;
--
	IF c1_rec.isindirectdenominator = 1 THEN
		FOR c2_rec IN c2(l_table_name) LOOP
			j:=j+1;
			IF c2_rec.total_denominators > 0 THEN
				IF msg IS NULL THEN
					msg:=E'\n'||c2_rec.geography||' '||c2_rec.total_denominators;
				ELSE
					msg:=msg||', '||c2_rec.geography||' '||c2_rec.total_denominators;
				END IF;
--
				i:=0;
				FOR c3_rec IN c3(c2_rec.geography, l_table_name) LOOP
					i:=i+1;
					IF i = 1 THEN
						dmsg:=' ('||c3_rec.table_name;
					ELSE
						dmsg:=dmsg||', '||c3_rec.table_name;
					END IF;
				END LOOP;
				dmsg:=dmsg||')';
				PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_auto_indirect_checks', 'table[%]: %, geography: %; total_denominators = %; %', 
					j::VARCHAR, l_table_name::VARCHAR, c2_rec.geography::VARCHAR, c2_rec.total_denominators::VARCHAR, dmsg::VARCHAR);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_auto_indirect_checks', 'table[%]: %, geography: %; total_denominators = 0', 
					j::VARCHAR, l_table_name::VARCHAR, c2_rec.geography::VARCHAR);
			END IF;	
		END LOOP;
	END IF;	
	RETURN msg;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(VARCHAR) IS 'Function: 	rif40_auto_indirect_checks()
Parameters:	table name
Returns: 	NULL if not found or not an indirect denominator, geography and counts
Description:	Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). 
		A user specific T_RIF40_NUM_DENOM is supplied for other combinations. 
		Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.';

\df rif40_sql_pkg.rif40_auto_indirect_checks

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_explain_ddl(sql_stmt VARCHAR)
RETURNS TABLE(explain_line	TEXT)
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_explain_ddl()
Parameters:	SQL statement
Returns: 	TABLE of explain_line
Description:	Coerce EXPLAIN output into a table with a known column
		Supports EXPLAIN and EXPLAIN ANALYZE as text ONLY
 */
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager role
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_explain_ddl', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	RETURN QUERY EXECUTE sql_stmt;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_explain_ddl(VARCHAR) IS 'Function: 	_rif40_explain_ddl()
Parameters:	SQL statement
Returns: 	TABLE of explain_line
Description:	Coerce EXPLAIN output into a table with a known column. 
		Supports EXPLAIN and EXPLAIN ANALYZE as text ONLY.';

\df rif40_sql_pkg._rif40_explain_ddl

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl(sql_stmt VARCHAR)
RETURNS integer
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_ddl()
Parameters:	SQL statement
Returns:	Rows
Description:	Log and execute SQL (rif40 schema create version)
		If NULL SQL raises no data exception (02000) with message "ERROR:  rif40_ddl() Null SQL statement"
		Supports EXPLAIN and EXPLAIN ANALYZE as text ONLY

 */
DECLARE
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
	l_rows INTEGER:=NULL;
	l_pos INTEGER:=NULL;
--
	explain_rec	RECORD;
	explain_text	VARCHAR;
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager role
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_ddl', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	IF sql_stmt IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl', 'Null SQL statement');
		RAISE SQLSTATE '02000' /* No data found */ USING MESSAGE='rif40_ddl() Null SQL statement';
	END IF;
--
	stp:=clock_timestamp();
	l_pos:=position('EXPLAIN' IN UPPER(sql_stmt));
	IF l_pos = 1 THEN /* EXPLAIN ANALYZE statement */
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'EXPLAIN SQL> %;', sql_stmt::VARCHAR);
		sql_stmt:='SELECT explain_line FROM rif40_sql_pkg._rif40_explain_ddl('||quote_literal(sql_stmt)||')';
		FOR explain_rec IN EXECUTE sql_stmt LOOP
			IF explain_text IS NULL THEN
				explain_text:=explain_rec.explain_line::VARCHAR;
			ELSE
				explain_text:=explain_text||E'\n'||explain_rec.explain_line::VARCHAR;
			END IF;
		END LOOP;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', '%', explain_text::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'SQL> %;', sql_stmt::VARCHAR);
		EXECUTE sql_stmt;
	END IF;
	GET DIAGNOSTICS l_rows = ROW_COUNT;
	etp:=clock_timestamp();
	took:=age(etp, stp);
--	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'Statement took: % (%)', 
--			took::VARCHAR, EXTRACT ('epoch' FROM took)::VARCHAR);
	if (EXTRACT ('epoch' FROM took) > 1) THEN
		IF l_rows IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'Statement took: %', 
				took::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'Statement took: %, proccessed % rows', 
				took::VARCHAR, l_rows::VARCHAR);
		END IF;
	ELSE
		IF l_rows IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_ddl', 'Statement took: %', 
				took::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_ddl', 'Statement took: %, proccessed % rows', 
				took::VARCHAR, l_rows::VARCHAR);
		END IF;
	END IF;
--
	RETURN l_rows;
EXCEPTION
	WHEN SQLSTATE '02000' /* No data found */ THEN
		RAISE;
	WHEN others THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl', 'SQL in error (%)> %;', 
			SQLSTATE::VARCHAR /* SQL error state */,
			sql_stmt::VARCHAR /* SQL statement */); 
		RAISE;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR) IS 'Function: 	rif40_ddl()
Parameters:	SQL statement
Returns:	Rows
Description:	Log and execute SQL (rif40 schema create version)
';

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl(sql_stmt VARCHAR[])
RETURNS integer
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_ddl()
Parameters:	SQL statement array (0+ statements; not multiline statements)
Returns:	Rows
Description:	Log and execute SQL (rif40 schema create version) - ARRAY VERSION

 */
DECLARE
	l_rows INTEGER:=NULL;
	l2_rows INTEGER:=0;
--
	l_sql_stmt VARCHAR;
	l_idx INTEGER;
BEGIN
--
	IF sql_stmt IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-20997, 'rif40_ddl', 'Null SQL statement array');
	END IF;
--
	FOR i IN array_lower(sql_stmt, 1) .. array_upper(sql_stmt, 1) LOOP
		l_idx:=i;
		l_sql_stmt:=sql_stmt[l_idx];
		l_rows:=rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		IF l_rows IS NOT NULL THEN
			l2_rows:=l2_rows+l_rows;
		END IF;
	END LOOP;
--
	RETURN l2_rows;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR[]) IS 'Function: 	rif40_ddl()
Parameters:	SQL statement array (0+ statements; not multiline statements)
Returns:	Rows
Description:	Log and execute SQL (rif40 schema create version) - ARRAY VERSION
';

\df rif40_sql_pkg.rif40_ddl

--
-- Oracle compatibility objects
--
CREATE OR REPLACE FUNCTION rif40_sql_pkg.sys_context(namespace VARCHAR, parameter VARCHAR) 
RETURNS VARCHAR 
SECURITY INVOKER
AS
$func$
/*

Function: 	sys_context()
Parameters:	Namespace, parameter
Returns:	TIMESTAMP
Description:	Oracle compatability function SYS_CONTEXT
		Namespace is ignored unless it is SAHSU_CONTEXT when the role is checked to see if it was GRANTED the ROLE
		Parameter must be one of: DB_NAME, CURRENT_SCHEMA, AUDSID

		Used to support auditing

		Could be extended to use FDW to get the real AUDSID
 */
DECLARE
	ret	VARCHAR;
	c1 CURSOR FOR
		SELECT pid /* Procpid in 9.2 */ ||'.'||TO_CHAR(backend_start, 'J.SSSS.US') audsid 
			/* Backend PID.Julian day.Seconds from midnight.uSeconds (backend start) */
		  FROM pg_stat_activity
		 WHERE datname     = current_database()
		   AND usename     = session_user
		   AND client_addr = inet_client_addr()
		   AND client_port = inet_client_port();		
	c2c CURSOR(l_granted_role VARCHAR) FOR
		SELECT * 
		  FROM user_role_privs
		 WHERE l_granted_role = granted_role;
	c2c_rec user_role_privs%ROWTYPE;
BEGIN
--
-- Emulate Oracle security contexts by returning YES if the user has the role of the same name
--
	IF namespace = 'SAHSU_CONTEXT' THEN 
		OPEN c2c(LOWER(parameter));
		FETCH c2c INTO c2c_rec;
		CLOSE c2c;
		IF c2c_rec.granted_role IS NULL THEN
			RETURN 'NO';
		ELSE
			RETURN 'YES';
		END IF;
	ELSE
		IF parameter = 'DB_NAME' THEN 
			ret:=current_database();
		ELSIF parameter = 'CURRENT_SCHEMA' THEN 
			ret:=current_schema();
		ELSIF parameter = 'AUDSID' OR parameter = 'SESSIONID' THEN 
			OPEN c1;
			FETCH c1 INTO ret;
			CLOSE c1;
		ELSIF namespace IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'sys_context', 'namespace: NULL invalid parameter: %', 
				parameter::VARCHAR);

		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'sys_context', 'namespace: % invalid parameter: %', 
				namespace::VARCHAR, parameter::VARCHAR);
		END IF;
	END IF;
--
	RETURN ret;
END;
$func$ LANGUAGE plpgsql;
COMMENT ON FUNCTION rif40_sql_pkg.sys_context(VARCHAR, VARCHAR) IS 'Replacement for Oracle SYS_CONTEXT(namespace, parameter). Only limited parameters are supported, namespace is ignored.';

\df rif40_sql_pkg.sys_context

SELECT rif40_sql_pkg.sys_context(NULL, 'DB_NAME');
SELECT rif40_sql_pkg.sys_context(NULL, 'CURRENT_SCHEMA');
SELECT rif40_sql_pkg.sys_context(NULL, 'AUDSID');

CREATE OR REPLACE FUNCTION rif40_sql_pkg.systimestamp(tprecision BIGINT) RETURNS TIMESTAMP
AS
$func$
/*

Function: 	systimestamp()
Parameters:	Precision
Returns:	TIMESTAMP
Description:	Oracle compatability function SYSTIMESTAMP

 */
DECLARE
BEGIN
	RETURN current_time;
END;
$func$ LANGUAGE plpgsql;
COMMENT ON FUNCTION rif40_sql_pkg.systimestamp(tprecision BIGINT) IS 'Replacement for Oracle SYSTIMESTAMP. Precision is not used';

\df rif40_sql_pkg.systimestamp

SELECT SYS_CONTEXT('SAHSU_CONTEXT', 'RIF_STUDENT');

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_drop_user_table_or_view(table_or_view VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_drop_user_table_or_view()
Parameters:	Table or view name
Returns:	Nothing
Description:	Drops users table, view or temporary table if it exists. Trucates table first.
 */
DECLARE
	c1dut CURSOR(l_table_or_view VARCHAR) FOR 		/* User objects */
		SELECT TRUE AS truncate_table, TRUE AS is_table, tablename AS table_or_view		/* Local tables */
		  FROM pg_tables
		 WHERE tableowner = USER
		   AND schemaname = USER
		   AND tablename  = l_table_or_view
		 UNION
		SELECT FALSE AS truncate_table, FALSE AS is_table, viewname AS table_or_view 		/* Local views */
		  FROM pg_views
		 WHERE viewowner  = USER
		   AND schemaname = USER
		   AND viewname   = l_table_or_view
		 UNION
		SELECT FALSE AS truncate_table, TRUE AS is_table, a.relname AS table_or_view 		/* Temporary tables */
 		 FROM pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relkind        = 'r' 				/* Relational table */
		   AND a.relpersistence = 't' 				/* Persistence: temporary */
		   AND a.relowner       = r.oid
		   AND n.nspname        LIKE 'pg_temp%'
		   AND a.relname        = l_table_or_view
		ORDER BY 1;
	c1dut_rec  RECORD;
--
	sql_stmt 	VARCHAR;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
	v_version BOOLEAN:=(string_to_array(version()::Text, ' '::Text))[2] >= '9.2'; /* Check Postgres server version */
BEGIN
	IF table_or_view IS NULL THEN 
		RETURN;
	END IF;
--
	OPEN c1dut(table_or_view);
	FETCH c1dut INTO c1dut_rec;
	IF NOT FOUND THEN /* Does not exist, ignore */
		CLOSE c1dut;
		RETURN; 
	END IF;
	CLOSE c1dut;
--
--  Bin object
--
	IF c1dut.truncate_table THEN
		sql_stmt:='TRUNCATE TABLE '||table_or_view;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_drop_user_table_or_view', 'SQL> %;', 
			sql_stmt::VARCHAR);
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END IF;
	IF c1dut.is_table THEN
		sql_stmt:='DROP TABLE '||table_or_view;
	ELSE
		sql_stmt:='DROP VIEW '||table_or_view;
	END IF;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_drop_user_table_or_view', 'SQL> %;', 
		sql_stmt::VARCHAR);
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		IF v_version THEN
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		END IF;
		error_message:='rif40_drop_user_table_or_view() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
--
		RAISE;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_drop_user_table_or_view(VARCHAR) IS 'Function: 	rif40_drop_user_table_or_view()
Parameters:	Table or view name
Returns:	Nothing
Description:	Drops users table, view or temporary table if it exists. Trucates table first.';

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_does_role_exist(role VARCHAR)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_does_role_exist()
Parameters:	Database role
Returns:	TRUE or FALSE
Description:	Check if database role exists
 */
DECLARE
	c1dre CURSOR(l_role VARCHAR) FOR /* Extra table/view columns */
		SELECT rolname
		  FROM pg_roles
		 WHERE rolname = l_role;
	c1dre_rec RECORD;
BEGIN
	IF role IS NULL THEN 
		RETURN FALSE;
	END IF;
--
	OPEN c1dre(role);
	FETCH c1dre INTO c1dre_rec;
	IF NOT FOUND THEN /* Does not exist, ignore */
		CLOSE c1dre;
		RETURN FALSE; 
	END IF;
	CLOSE c1dre;
--
	RETURN TRUE;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_does_role_exist(VARCHAR) IS 'Function: 	rif40_does_role_exist()
Parameters:	Database role
Returns:	TRUE or FALSE
Description:	Check if database role exists.';

SELECT rif40_sql_pkg.rif40_does_role_exist('rif_student');

--
-- Dynamic SQL method 4 (Oracle name) SELECT 
--
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_method4(select_stmt VARCHAR, title VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_method4()
Parameters:	SQL SELECT statement, title
Returns:	Nothing
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement

SELECT column_name, data_type, character_maximum_length, character_octet_length, numeric_precision, numeric_precision_radix, numeric_scale, datetime_precision
  FROM information_schema.columns a, pg_tables b 
		 WHERE b.schemaname = a.table_schema
		   AND a.table_name = b.tablename
		   AND b.tablename  = 'rif40_geographies';
 */
DECLARE
	sql_stmt 	VARCHAR;
	drop_stmt 	VARCHAR;
	select_text	VARCHAR:=NULL;
	temp_table 	VARCHAR:=NULL;
--
	c1m4 CURSOR(l_table VARCHAR) FOR /* Extra table/view columns */
		SELECT table_name, column_name,
		       CASE 													/* Work out column length */
				WHEN numeric_precision IS NOT NULL /* bits */ AND
				     LENGTH((2^numeric_precision)::Text) > LENGTH(column_name) AND 
				     LENGTH((2^numeric_precision)::Text) <= 40							THEN LENGTH((2^numeric_precision)::Text)
				WHEN numeric_precision IS NOT NULL /* bits */ AND
				     LENGTH((2^numeric_precision)::Text) > LENGTH(column_name) AND 
				     LENGTH((2^numeric_precision)::Text) > 40							THEN 40 /* Truncate at 40 characters */
				WHEN datetime_precision IS NOT NULL /* bits */  AND
				     LENGTH((2^datetime_precision)::Text) > LENGTH(column_name) AND 
				     LENGTH((2^datetime_precision)::Text) <= 40							THEN LENGTH((2^datetime_precision)::Text)
				WHEN datetime_precision IS NOT NULL /* bits */  AND
				     LENGTH((2^datetime_precision)::Text) > LENGTH(column_name) AND 
				     LENGTH((2^datetime_precision)::Text) > 40							THEN 40 /* Truncate at 40 characters */
				WHEN character_maximum_length > LENGTH(column_name) AND 
				     character_maximum_length <= 40 								THEN character_maximum_length
				WHEN character_maximum_length > LENGTH(column_name) AND 
				     character_maximum_length > 40 								THEN 40 /* Truncate at 40 characters */
				ELSE LENGTH(column_name)
		       END column_length
		  FROM information_schema.columns a, pg_tables b 
		 WHERE b.schemaname = a.table_schema
		   AND a.table_name = b.tablename
		   AND b.tableowner = USER
		   AND b.tablename  = l_table; 
	c2m4 REFCURSOR;
	c1m4_rec RECORD;
	c2m4_result_row	VARCHAR[];
--
	stp 		TIMESTAMP WITH TIME ZONE;
	etp 		TIMESTAMP WITH TIME ZONE;
	took 		INTERVAL;
	l_rows 		INTEGER:=0;
	j 		INTEGER:=0;
	display_len	INTEGER:=0;
	column_len	INTEGER[];
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager role
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_method4', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	IF select_stmt IS NULL THEN
		RETURN;
	END IF;
	stp:=clock_timestamp();
--
-- Create results temporary table
--
	temp_table:='l_'||REPLACE(rif40_sql_pkg.sys_context(NULL, 'AUDSID'), '.', '_');
--
-- This could do with checking first to remove the notice:
-- psql:v4_0_rif40_sql_pkg.sql:3601: NOTICE:  table "l_7388_2456528_62637_130282_7388" does not exist, skipping
-- CONTEXT:  SQL statement "DROP TABLE IF EXISTS l_7388_2456528_62637_130282"
-- PL/pgSQL function "rif40_ddl" line 32 at EXECUTE statement
--
	drop_stmt:='DROP TABLE IF EXISTS '||temp_table;
	PERFORM rif40_sql_pkg.rif40_drop_user_table_or_view(temp_table);
--
-- SQL injection check
--
-- ADD

--
	sql_stmt:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE '||temp_table||' AS '||E'\n'||select_stmt;
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Process table header
--
	FOR c1m4_rec IN c1m4(temp_table) LOOP
		j:=j+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_method4', 'Column[%] %.%, length: %', 
			j::Text, 
			c1m4_rec.table_name, 
			c1m4_rec.column_name, 
			c1m4_rec.column_length::Text);
		display_len:=display_len+c1m4_rec.column_length+3;
		column_len[j]:=c1m4_rec.column_length;
		IF select_text IS NULL THEN
			select_text:=E'\n'||RPAD(c1m4_rec.column_name, c1m4_rec.column_length);
		ELSE
			select_text:=select_text||' | '||RPAD(c1m4_rec.column_name, c1m4_rec.column_length);
		END IF;
	END LOOP;
	select_text:=select_text||E'\n'||RPAD('-', display_len, '-');
--
-- Title
--
	IF title IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_method4', E'\n'||title||E'\n'||RPAD('-', LENGTH(title), '-'));
	END IF;
--
-- FETCH from temporary table as an array
--
	sql_stmt:='SELECT TRANSLATE(string_to_array(x.*::Text, '','')::Text, ''()'', '''')::text[] FROM '||temp_table||' AS x';
	PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_method4', 'SQL> %;', 
		select_stmt::VARCHAR);
	OPEN c2m4 FOR EXECUTE sql_stmt;
	LOOP
		FETCH c2m4 INTO c2m4_result_row;
		IF NOT FOUND THEN EXIT; END IF;
--  
		l_rows:=l_rows+1;
		select_text:=select_text||E'\n';
--
-- Process row array
--
		FOR i IN 1 .. j LOOP
			IF i = 1 THEN
				select_text:=select_text||RPAD(c2m4_result_row[i], column_len[i]);
			ELSE
				select_text:=select_text||' | '||RPAD(c2m4_result_row[i], column_len[i]);
			END IF;
		END LOOP;
	END LOOP;
	CLOSE c2m4;
--
	IF l_rows = 0 THEN
		select_text:=select_text||E'\n'||'(no rows)';
	ELSIF l_rows = 1 THEN
		select_text:=select_text||E'\n'||'('||l_rows::Text||' row)';
	ELSE
		select_text:=select_text||E'\n'||'('||l_rows::Text||' rows)';
	END IF;
--
-- Print SELECT
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_method4', select_text);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	IF l_rows IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_method4', 'Statement took: %', 
			took::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_method4', 'Statement took: %, proccessed % rows', 
			took::VARCHAR, l_rows::VARCHAR);
	END IF;
--
	PERFORM rif40_sql_pkg.rif40_ddl(drop_stmt);
--
	RETURN;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		error_message:='rif40_method4('''||coalesce(title::VARCHAR, '')||''') caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
--
-- This will not work if the cursor is open, but as it is a temporary table it will be deleted on session end
--
		PERFORM rif40_sql_pkg.rif40_ddl(drop_stmt);
		RAISE;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_method4(VARCHAR, VARCHAR) IS 'Function: 	rif40_method4()
Parameters:	SQL SELECT statement
Returns:	Nothing
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement
';

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_hash_partition(l_schema VARCHAR, t_table VARCHAR, l_ciolumn VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_hash_partition()
Parameters:	Schema, table, column
Returns:	Nothing
Description:	Hash partition schema.table on column

 */
DECLARE
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_hash_partition', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_hash_partition(VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	rif40_method4()
Parameters:	Schema, table, column
Returns:	Nothing
Description:	Hash partition schema.table on column
';

\i ../PLpgsql/rif40_sql_pkg/rif40_range_partition.sql

--
-- Add DDL checks (now run separately)
--
--\i ../PLpgsql/v4_0_rif40_sql_pkg_ddl_checks.sql 

/*
DO LANGUAGE plpgsql $$
BEGIN
	PERFORM rif40_sql_pkg.rif40_method4('SELECT * 
  FROM rif40_geographies', 'Test');
END;
$$;
 */
GRANT SELECT ON rif40_sql_pkg.user_role_privs TO rif_user;
GRANT SELECT ON rif40_sql_pkg.user_role_privs TO rif40;

GRANT EXECUTE ON FUNCTION rif40_sql_pkg.does_role_exist(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.does_role_exist(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_does_role_exist(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_does_role_exist(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_drop_user_table_or_view(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_drop_user_table_or_view(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_method4(VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_method4(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.systimestamp(BIGINT) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.systimestamp(BIGINT) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR[]) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR[]) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.sys_context(VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.sys_context(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_startup() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_startup() TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_num_denom_validate(VARCHAR, VARCHAR) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(VARCHAR) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_is_object_resolvable(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_is_object_resolvable(VARCHAR) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_object_resolve(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_object_resolve(VARCHAR) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.is_rif40_manager_or_schema() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.is_rif40_manager_or_schema() TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.is_rif40_user_manager_or_schema() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.is_rif40_user_manager_or_schema() TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_get_function_arg_types(oid[]) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_get_function_arg_types(oid[]) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_table(VARCHAR, INTEGER, BOOLEAN, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_table(VARCHAR, INTEGER, BOOLEAN, VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_views(BOOLEAN, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_views(BOOLEAN, VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_view(BOOLEAN, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_view(BOOLEAN, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_fdw_table_select_test(VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_fdw_table_select_test(VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_update_fdw_tables(VARCHAR, VARCHAR, VARCHAR, INTEGER) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_update_fdw_tables(VARCHAR, VARCHAR, VARCHAR, INTEGER) TO rif_user, rif_manager;

\echo Created PG psql code (SQL and Oracle compatibility processing).

--
-- Eof
