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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function
--
-- This function needs splitting into sub-functions by check and using pg_ tables only is possible
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

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_checks()
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_ddl_checks()
Parameters: 	None
Returns: 	Nothing
Description:	Validate RIF DDL

Check for:

a) Missing tables and views
b) Missing table/view comment
c) Missing table/view columns
d) Missing table/view column comments
e) Missing triggers
f) Extra tables and views
g) Missing sequences
h) All tables, views and sequences GRANT SELECT to rif_user and rif_manager
i) All functions in rif40_sql_pkg, rif40_geo_pkg GRANT EXECUTE to rif40, rif_user and rif_manager
j) Extra columns

Will work as any RIF user, not just RIF40

 */
DECLARE
	c1 CURSOR(l_schema VARCHAR) FOR /* Missing tables and views */
		SELECT LOWER(table_or_view_name_hide) table_or_view	/* RIF40 list of tables and views */
		  FROM rif40_tables_and_views
		EXCEPT 
		SELECT a.relname table_or_view 				/* Temporary tables */
 		 FROM pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relkind        = 'r' 				/* Relational table */
		   AND a.relpersistence = 't' 				/* Persistence: temporary */
		   AND a.relowner = r.oid
		   AND n.nspname        LIKE 'pg_temp%'
		EXCEPT					
		SELECT a.relname table_or_view				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.ftrelid = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND COALESCE(n.nspname, r.rolname) IN (USER, 'public')
		EXCEPT
		SELECT t.tablename table_or_view			/* Local tables */
		  FROM pg_tables t, pg_class c
		 WHERE t.tableowner IN (USER, l_schema)
		   AND t.schemaname IN (USER, l_schema)
		   AND c.relowner   IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema))
		   AND c.relname    = t.tablename
		   AND c.relkind    = 'r' 				/* Relational table */
		   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
		EXCEPT
		SELECT viewname	table_or_view 				/* Local views */
		  FROM pg_views
		 WHERE viewowner  IN (USER, l_schema)
		   AND schemaname IN (USER, l_schema)
		 ORDER BY 1;
	c2 CURSOR(l_schema VARCHAR) FOR /* Missing table/view comment */	
		SELECT DISTINCT relname table_or_view, n.nspname AS schema_owner, b.description, comments
		  FROM rif40_tables_and_views c, pg_class a
			LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid AND b.objsubid = 0)
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema))
		   AND b.description IS NULL
		   AND LOWER(table_or_view_name_hide) = a.relname
		 ORDER BY 1;
	c3 CURSOR(l_schema VARCHAR) FOR /* Missing table/view columns */
		SELECT LOWER(table_or_view_name_hide) table_or_view, LOWER(column_name_hide) column_name
		  FROM rif40_columns
                 WHERE LOWER(table_or_view_name_hide) NOT IN ( /* Exclude missing tables/views (i.e. cursor c1) */
			SELECT LOWER(table_or_view_name_hide) table_or_view	/* RIF40 list of tables and views */
			  FROM rif40_tables_and_views
			EXCEPT 
			SELECT a.relname table_or_view 				/* Temporary tables */
 			 FROM pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relkind        = 'r' 				/* Relational table */
			   AND a.relpersistence = 't' 				/* Persistence: temporary */
			   AND a.relowner = r.oid
			   AND n.nspname        LIKE 'pg_temp%'
			EXCEPT					
			SELECT a.relname table_or_view				/* FDW tables */
			  FROM pg_foreign_table b, pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE b.ftrelid = a.oid
			   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relowner = r.oid
				   AND COALESCE(n.nspname, r.rolname) IN (USER, 'public')
			EXCEPT
			SELECT t.tablename table_or_view			/* Local tables */
			  FROM pg_tables t, pg_class c
			 WHERE t.tableowner IN (USER, l_schema)
			   AND t.schemaname IN (USER, l_schema)
			   AND c.relowner   IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema))
			   AND c.relname    = t.tablename
			   AND c.relkind    = 'r' 				/* Relational table */
			   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
			EXCEPT
			SELECT viewname	table_or_view				/* Local views */
			  FROM pg_views
			 WHERE viewowner  IN (USER, l_schema)
			   AND schemaname IN (USER, l_schema)
		)
		   AND table_or_view_name_hide NOT LIKE 'G%'
		EXCEPT
		SELECT table_name table_or_view, column_name
		  FROM information_schema.columns a
			LEFT OUTER JOIN pg_tables b1 ON (b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
			LEFT OUTER JOIN pg_views b2 ON (b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
			LEFT OUTER JOIN (
				SELECT c.relname fdw_table, r.rolname fdw_tableowner
				  FROM pg_class c, pg_foreign_table t, pg_roles r
				 WHERE t.ftrelid  = c.oid 
				   AND c.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)) b3 ON
				(b3.fdw_table = a.table_name)
		 WHERE tableowner IN (USER, l_schema) OR viewowner IN (USER, l_schema) OR fdw_tableowner = USER
		 ORDER BY 1, 2;
	c4 CURSOR(l_schema VARCHAR) FOR /* Missing table/view column comments */ 		
		WITH a AS (
	 		SELECT table_name table_or_view, column_name, ordinal_position
			  FROM information_schema.columns a
					LEFT OUTER JOIN pg_views b2  ON 
						(b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
					LEFT OUTER JOIN pg_tables b1 ON 
						(b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
			 WHERE (viewowner IN (USER, l_schema) OR tableowner IN (USER, l_schema))
			   AND table_schema = 'rif40' 
		), b AS (
			SELECT table_or_view, column_name, ordinal_position, b.oid
  			  FROM a, pg_class b
			 WHERE b.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema)) 
			   AND b.relname  = a.table_or_view
		)
		SELECT table_or_view, column_name, ordinal_position, c.description
		  FROM b
			LEFT OUTER JOIN pg_description c ON (c.objoid = b.oid AND c.objsubid = b.ordinal_position)
		 WHERE c.description IS NULL
		 ORDER BY 1, 2;
	c5 CURSOR(l_schema VARCHAR) FOR  /* Missing triggers */
		SELECT LOWER(trigger_name) trigger_name, LOWER(table_name) table_name
		  FROM rif40_triggers
		 WHERE LOWER(table_name) NOT IN (
			SELECT a.relname object_name	/* FDW tables */
			  FROM pg_foreign_table b, pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE b.ftrelid = a.oid
			   AND a.relowner IN (SELECT oid FROM pg_roles WHERE rolname = l_schema)
			   AND a.relowner = r.oid
			   AND COALESCE(n.nspname, r.rolname) = l_schema)
		EXCEPT
		SELECT tgname trigger_name, relname table_name
		  FROM pg_class a, pg_trigger b
			 WHERE tgrelid = a.oid
			   AND relowner IN (SELECT oid FROM pg_roles WHERE rolname = l_schema)
		 ORDER BY 1, 2;
	c6 CURSOR(l_schema VARCHAR) FOR /* Extra tables and views */
		SELECT a.relname table_or_view 				/* Temporary tables */
 		 FROM pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relkind        = 'r' 				/* Relational table */
		   AND a.relpersistence = 't' 				/* Persistence: temporary */
		   AND a.relowner = r.oid
		   AND n.nspname        LIKE 'pg_temp%'
		 UNION
		SELECT a.relname table_or_view				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.ftrelid = a.oid
		   AND a.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema))
		   AND a.relowner = r.oid
		   AND COALESCE(n.nspname, r.rolname) IN (USER, l_schema)
		 UNION
		SELECT t.tablename table_or_view			/* Local tables */
		  FROM pg_tables t, pg_class c
		 WHERE t.tableowner IN (USER, l_schema)
		   AND t.schemaname IN (USER, l_schema)
		   AND c.relowner   IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema))
		   AND c.relname    = t.tablename
		   AND c.relkind    = 'r' 				/* Relational table */
		   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
		 UNION
		SELECT viewname	table_or_view 				/* Local views */
		  FROM pg_views
		 WHERE viewowner  IN (USER, l_schema)
		   AND schemaname IN (USER, l_schema)
		EXCEPT  
		SELECT table_or_view
		  FROM (
			SELECT LOWER(table_or_view_name_hide) table_or_view	/* RIF40 list of tables and views */
			  FROM rif40_tables_and_views
			 UNION
			SELECT tablename table_or_view		 		/* Geospatial tables created by rif40_geo_pkg functions */
			  FROM pg_tables
			 WHERE tablename IN (
				SELECT DISTINCT LOWER(lookup_table) tablename	/* Geolevel lookup tables */
			        FROM t_rif40_geolevels WHERE lookup_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(hierarchytable) tablename	/* Hierarchy table */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 't_rif40_'||LOWER(geography)||'_geometry' tablename
										/* Geometry tables */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 't_rif40_geolevels_geometry_'||	/* Geometry table partitions */
					LOWER(geography)||'_'||LOWER(geolevel_name) tablename
			        FROM t_rif40_geolevels
			       UNION 
			      SELECT DISTINCT LOWER(table_name) tablename	/* Numerator and denominators */
			        FROM rif40_tables WHERE table_name IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(covariate_table) tablename	/* Covariate tables */
			        FROM t_rif40_geolevels WHERE covariate_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(shapefile_table) tablename	/* Loaded shapefile tables */
			        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(centroids_table) tablename	/* Centroids tables */
			        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL
				/* rif40_outcomes lookup tables not inluded in this list as supplied as part of the initial setup 
			      UNION
			     SELECT tablename 
			       FROM (
				   SELECT LOWER(current_lookup_table) AS tablename
				     FROM rif40_outcomes
				    UNION
				   SELECT LOWER(previous_lookup_table) AS tablename
				     FROM rif40_outcomes) AS a
		  	      WHERE tablename IS NOT NULL
				*/ 
				)
			 UNION
			SELECT a.relname AS table_or_view		 		/* FDW tables created by  rif40_sql_pkg.rif40_startup() */
			  FROM pg_foreign_table b, pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE b.ftrelid = a.oid
			   AND a.relowner IN (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relowner = r.oid
			   AND n.nspname = USER
			   AND a.relname IN (
			      SELECT LOWER(table_name) AS tablename			/* FDW interconnect tables to Oracle */
				FROM rif40_fdw_tables
			       WHERE create_status IN ('C', 'E')
				)
			 UNION
			SELECT a.relname AS table_or_view		 		/* FDW Geospatial tables created by rif40_geo_pkg functions (rifupg34 only) */
			  FROM pg_foreign_table b, pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE b.ftrelid = a.oid
			   AND a.relowner IN (SELECT oid FROM pg_roles WHERE rolname = 'rifupg34')
			   AND a.relowner = r.oid
			   AND COALESCE(n.nspname, r.rolname) = l_schema
			   AND a.relname IN (
				SELECT DISTINCT LOWER(lookup_table) tablename	/* Geolevel lookup tables */
			        FROM t_rif40_geolevels WHERE lookup_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(hierarchytable) tablename	/* Hierarchy table */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 't_rif40_'||LOWER(geography)||'_geometry' tablename
										/* Geometry tables */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 't_rif40_geolevels_geometry_'||	/* Geometry table partitions */
					LOWER(geography)||'_'||LOWER(geolevel_name) tablename
			        FROM t_rif40_geolevels
			       UNION 
			      SELECT DISTINCT LOWER(table_name) tablename	/* Numerator and denominators */
			        FROM rif40_tables WHERE table_name IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(covariate_table) tablename	/* Covariate tables */
			        FROM t_rif40_geolevels WHERE covariate_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(shapefile_table) tablename	/* Loaded shapefile tables */
			        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(centroids_table) tablename	/* Centroids tables */
			        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL
				/* rif40_outcomes lookup tables not inluded in this list as supplied as part of the initial setup 
			      UNION
			     SELECT tablename 
			       FROM (
				   SELECT LOWER(current_lookup_table) AS tablename
				     FROM rif40_outcomes
				    UNION
				   SELECT LOWER(previous_lookup_table) AS tablename
				     FROM rif40_outcomes) AS a
		  	      WHERE tablename IS NOT NULL
				*/ 
				)
			) AS a
		 ORDER BY 1;
	c7 CURSOR(l_schema VARCHAR) FOR /* Missing sequences */
		SELECT 'rif40_study_id_seq' AS sequences
		 UNION
		SELECT 'rif40_inv_id_seq' AS sequences
		EXCEPT
		SELECT c.relname AS sequences
		  FROM pg_class c
		 WHERE relkind = 'S'
		   AND c.relowner   = (SELECT oid FROM pg_roles WHERE rolname = l_schema)
		 ORDER BY 1;
	c8 CURSOR(l_schema VARCHAR) FOR /* All tables, views and sequences GRANT SELECT to rif_user and rif_manager */
		SELECT LOWER(table_or_view_name_hide) table_or_view, /* rif40_tables_and_views */
  		       has_table_privilege(USER, LOWER(table_or_view_name_hide), 'select') has_user_select,
  		       has_table_privilege('rif_user', LOWER(table_or_view_name_hide), 'select') has_rif_user_select,
  		       has_table_privilege('rif_manager', LOWER(table_or_view_name_hide), 'select') has_rif_manager_select,
  		       has_table_privilege('public', LOWER(table_or_view_name_hide), 'select') has_public_select
		  FROM rif40_tables_and_views
                 WHERE LOWER(table_or_view_name_hide) NOT IN ( /* Exclude missing tables/views (i.e. cursor c1) */
			SELECT LOWER(table_or_view_name_hide) table_or_view	/* RIF40 list of tables and views */
			  FROM rif40_tables_and_views
			EXCEPT 
			SELECT a.relname table_or_view 				/* Temporary tables */
 			 FROM pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relkind        = 'r' 				/* Relational table */
			   AND a.relpersistence = 't' 				/* Persistence: temporary */
			   AND a.relowner = r.oid
			   AND n.nspname        LIKE 'pg_temp%'
			EXCEPT					
			SELECT a.relname table_or_view				/* FDW tables */
			  FROM pg_foreign_table b, pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE b.ftrelid = a.oid
			   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relowner = r.oid
			   AND COALESCE(n.nspname, r.rolname) = USER
			EXCEPT
			SELECT t.tablename table_or_view			/* Local tables */
			  FROM pg_tables t, pg_class c
			 WHERE t.tableowner IN (USER, l_schema)
			   AND t.schemaname IN (USER, l_schema)
			   AND c.relowner   IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema))
			   AND c.relname    = t.tablename
			   AND c.relkind    = 'r' 				/* Relational table */
			   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
			EXCEPT
			SELECT viewname AS table_or_view			/* Local views */
			  FROM pg_views
			 WHERE viewowner  IN (USER, l_schema)
			   AND schemaname IN (USER, l_schema)
		)
		   AND table_or_view_name_hide NOT LIKE 'G%'			/* Temporary tables */
		   AND LOWER(table_or_view_name_hide) NOT IN ('rif40_num_denom', 'rif40_num_denom_errors', 't_rif40_num_denom', 'rif40_user_version') /* Local user tables/views */
		 UNION
		SELECT rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||tablename table_or_view,	/* Geospatial tables created by rif40_geo_pkg functions */
  		       has_table_privilege(USER, rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||LOWER(tablename), 'select') has_user_select,
  		       has_table_privilege('rif_user', rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||LOWER(tablename), 'select') has_rif_user_select,
  		       has_table_privilege('rif_manager', rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||LOWER(tablename), 'select') has_rif_manager_select,
  		       has_table_privilege('public', rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||LOWER(tablename), 'select') has_public_select
		  FROM pg_tables
		 WHERE tablename IN (
			SELECT DISTINCT LOWER(lookup_table) tablename	/* Geolevel lookup tables */
		        FROM t_rif40_geolevels WHERE lookup_table IS NOT NULL
		       UNION 
		      SELECT DISTINCT LOWER(hierarchytable) tablename	/* Hierarchy table */
		        FROM rif40_geographies
		       UNION 
		      SELECT DISTINCT 't_rif40_'||LOWER(geography)||'_geometry' tablename
									/* Geometry tables */
		        FROM rif40_geographies
		       UNION 
		      SELECT DISTINCT LOWER(covariate_table) tablename	/* Covariate tables */
		        FROM t_rif40_geolevels WHERE covariate_table IS NOT NULL
		       UNION 
		      SELECT DISTINCT LOWER(shapefile_table) tablename	/* Loaded shapefile tables */
		        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
		       UNION 
		      SELECT DISTINCT LOWER(centroids_table) tablename	/* Centroids tables */
		        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL)
		   AND rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR) IS NOT NULL	/* Object exists in the search path */
		UNION
		SELECT rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||tablename table_or_view,	/* Numerator and denominators */
  		       has_table_privilege(USER, rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||LOWER(tablename), 'select') has_user_select,
  		       has_table_privilege('rif_user', rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||LOWER(tablename), 'select') has_rif_user_select,
  		       has_table_privilege('rif_manager', rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||LOWER(tablename), 'select') has_rif_manager_select,
  		       has_table_privilege('public', rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||LOWER(tablename), 'select') has_public_select
		  FROM pg_tables
		 WHERE tablename IN (
		      SELECT DISTINCT LOWER(table_name) tablename	/* Numerator and denominators */
		        FROM rif40_tables WHERE table_name IS NOT NULL)
		   AND rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR) IS NOT NULL	/* Object exists in the search path */
		 UNION
		SELECT table_or_view,					/* Sequences */
  		       has_table_privilege(USER, LOWER(table_or_view), 'select') has_user_select,
  		       has_table_privilege('rif_user', LOWER(table_or_view), 'select') has_rif_user_select,
  		       has_table_privilege('rif_manager', LOWER(table_or_view), 'select') has_rif_manager_select,
  		       has_table_privilege('public', LOWER(table_or_view), 'select') has_public_select
		  FROM (
			SELECT 'rif40_study_id_seq' AS table_or_view
			 UNION
			SELECT 'rif40_inv_id_seq' AS table_or_view
			) b, pg_class a
		 WHERE has_schema_privilege('rif40', 'USAGE') 
	  	  AND a.relkind  = 'S' 
		  AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = l_schema)
		  AND a.relname  = b.table_or_view
	        ORDER BY 1;
	c9 CURSOR(l_schema VARCHAR) FOR /* All functions in rif40_sql_pkg, rif40_geo_pkg GRANT EXECUTE to rif40, rif_user and rif_manager */
		SELECT object_type, 
                       schemaname||'.'||function_name AS function_name,
  		       has_function_privilege(USER, schemaname||'.'||function_name, 'execute') AS  has_user_select,
  		       has_function_privilege('rif40', schemaname||'.'||function_name, 'execute') AS has_rif40_select,
  		       has_function_privilege('rif_user', schemaname||'.'||function_name, 'execute') AS has_rif_user_select,
  		       has_function_privilege('rif_manager', schemaname||'.'||function_name, 'execute') AS has_rif_manager_select
		  FROM (
			SELECT l.lanname||' function' AS object_type, 
			       COALESCE(n.nspname, r.rolname) AS schemaname,
			       p.proname||rif40_sql_pkg.rif40_get_function_arg_types(p.proargtypes) AS function_name	
			  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
				LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
			 WHERE p.prolang    = l.oid
			   AND p.prorettype = t.oid
			   AND p.proowner   = (SELECT oid FROM pg_roles WHERE rolname = l_schema)
			   AND p.proowner   = r.oid
			) AS a
		 WHERE has_schema_privilege('rif40', 'USAGE') 
		 ORDER BY 1, 2;
	c10 CURSOR(l_schema VARCHAR) FOR /* Extra table/view columns */
		WITH a AS (
		SELECT table_name table_or_view, column_name
		  FROM information_schema.columns a
			LEFT OUTER JOIN pg_tables b1 ON (b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
			LEFT OUTER JOIN pg_views b2 ON (b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
			LEFT OUTER JOIN (
				SELECT c.relname fdw_table, r.rolname fdw_tableowner
				  FROM pg_foreign_table t, pg_roles r, pg_class c
					LEFT OUTER JOIN pg_namespace n ON (n.oid = c.relnamespace)			
				 WHERE t.ftrelid  = c.oid 
  				   AND n.nspname IN (USER, l_schema)
				   AND c.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
				) b3 ON (b3.fdw_table = a.table_name)
		 WHERE (tableowner IN (USER, l_schema) OR viewowner IN (USER, l_schema) OR fdw_tableowner = USER)
                   AND a.table_schema  != 'rif_studies'			/* Exclude map/extract tables */
		   AND NOT (a.table_name IN ('g_rif40_comparison_areas', 'g_rif40_study_areas', 'user_role_privs', 'sahsuland_geography_test')
    		       OR   a.table_name IN (
			SELECT tablename table_or_view		 		/* Geospatial tables created by rif40_geo_pkg functions */
			  FROM pg_tables
			 WHERE tablename IN (
		    	      SELECT DISTINCT LOWER(lookup_table) tablename	/* Geolevel lookup tables */
			        FROM t_rif40_geolevels WHERE lookup_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(hierarchytable) tablename	/* Hierarchy table */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 't_rif40_'||LOWER(geography)||'_geometry' tablename
										/* Geometry tables */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 't_rif40_geolevels_geometry_'||	/* Geometry table partitions */
					LOWER(geography)||'_'||LOWER(geolevel_name) tablename
			        FROM t_rif40_geolevels
			       UNION 
			      SELECT DISTINCT LOWER(table_name) tablename	/* Numerator and denominators */
			        FROM rif40_tables WHERE table_name IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(covariate_table) tablename	/* Covariate tables */
			        FROM t_rif40_geolevels WHERE covariate_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(shapefile_table) tablename	/* Loaded shapefile tables */
			        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(centroids_table) tablename	/* Centroids tables */
			        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL
				/* rif40_outcomes lookup tables not inluded in this list as supplied as part of the initial setup 
			      UNION
			     SELECT tablename 
			       FROM (
				   SELECT LOWER(current_lookup_table) AS tablename
				     FROM rif40_outcomes
				    UNION
				   SELECT LOWER(previous_lookup_table) AS tablename
				     FROM rif40_outcomes) AS a
		  	      WHERE tablename IS NOT NULL
				*/ 
				)
			 UNION
			SELECT a.relname AS table_or_view		 		/* FDW tables created by  rif40_sql_pkg.rif40_startup() */
			  FROM pg_foreign_table b, pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE b.ftrelid = a.oid
			   AND a.relowner IN (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relowner = r.oid
			   AND n.nspname = USER
			   AND a.relname IN (
			      SELECT LOWER(table_name) AS tablename			/* FDW interconnect tables to Oracle */
				FROM rif40_fdw_tables
			       WHERE create_status IN ('C', 'E')
				) 
			))
		EXCEPT
		SELECT LOWER(table_or_view_name_hide) table_or_view, LOWER(column_name_hide) column_name
		  FROM rif40_columns
                 WHERE LOWER(table_or_view_name_hide) NOT IN ( /* Exclude missing tables/views (i.e. cursor c1) */
			SELECT LOWER(table_or_view_name_hide) table_or_view	/* RIF40 list of tables and views */
			  FROM rif40_tables_and_views
			EXCEPT 
			SELECT a.relname table_or_view 				/* Temporary tables */
 			 FROM pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relkind        = 'r' 				/* Relational table */
			   AND a.relpersistence = 't' 				/* Persistence: temporary */
			   AND a.relowner = r.oid
			   AND n.nspname        LIKE 'pg_temp%'
			EXCEPT					
			SELECT a.relname table_or_view				/* FDW tables */
			  FROM pg_foreign_table b, pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE b.ftrelid = a.oid
			   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relowner = r.oid
			   AND COALESCE(n.nspname, r.rolname) = USER
			EXCEPT
			SELECT t.tablename table_or_view			/* Local tables */
			  FROM pg_tables t, pg_class c
			 WHERE t.tableowner IN (USER, l_schema)
			   AND t.schemaname IN (USER, l_schema)
			   AND c.relowner   IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema))
			   AND c.relname    = t.tablename
			   AND c.relkind    = 'r' 				/* Relational table */
			   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
			EXCEPT
			SELECT viewname table_or_view				/* Local views */
			  FROM pg_views
			 WHERE viewowner  IN (USER, l_schema)
			   AND schemaname IN (USER, l_schema)
		)
		   AND table_or_view_name_hide NOT LIKE 'G%'
		)
		SELECT table_schema, a.table_or_view, a.column_name
		  FROM a, information_schema.columns b
		 WHERE a.table_or_view = b.table_name
		   AND a.column_name   = b.column_name
		 ORDER BY 1, 2;
	c11 CURSOR FOR /* All rif40_objects and comments */
		WITH a AS (
			SELECT c.relname AS object_name, 
			       n.nspname AS object_schema, 
			       CASE 
					WHEN c.relkind        = 'r' AND			/* Relational table */
					     c.relpersistence = 'p' 			/* Persistence: unlogged */	 THEN 'TABLE'
					WHEN c.relkind        = 'r' AND			/* Relational table */
					     c.relpersistence = 'u' 			/* Persistence: unlogged */ 	THEN 'UNLOGGED TABLE'
					WHEN c.relkind        = 'r' AND			/* Relational table */
					     c.relpersistence = 't' 			/* Persistence: temporary */ 	THEN 'TEMPORARY TABLE'
					WHEN c.relkind        = 'i'			/* Index */			THEN 'INDEX'
					WHEN c.relkind        = 'v'			/* View */			THEN 'VIEW'
					WHEN c.relkind        = 'S'			/* Sequence */			THEN 'SEQUENCE'
					WHEN c.relkind        = 't'			/* TOAST Table */		THEN 'TOAST TABLE'
					WHEN c.relkind        = 'f'			/* Foreign Table */		THEN 'FOREIGN TABLE'
					WHEN c.relkind        = 'c'			/* Composite type */		THEN 'COMPOSITE TYPE'
					ELSE 										     'Unknown relkind: '||c.relkind
			       END AS object_type,
			       d.description
			  FROM pg_class c
				LEFT OUTER JOIN pg_namespace n ON (n.oid = c.relnamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid AND (d.objsubid IS NULL OR d.objsubid = 0))
			 WHERE c.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', USER))
			UNION
			SELECT p.proname AS object_name, 
			       n.nspname AS object_schema,
			       'FUNCTION'  AS object_type,
			       d.description
			  FROM pg_proc p
				LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = p.oid AND d.objsubid = 0)
			 WHERE p.proowner IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', USER))
			UNION
			SELECT t.tgname AS object_name, 
			       n.nspname AS object_schema,
			       'TRIGGER'  AS object_type,
			       d.description
			  FROM pg_trigger t, pg_class c
				LEFT OUTER JOIN pg_namespace n ON (n.oid = c.relnamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid AND d.objsubid = 0)
			 WHERE c.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', USER))
			   AND c.oid = t.tgrelid
			   AND t.tgisinternal = FALSE /* trigger function */
			UNION
			SELECT DISTINCT relname||'.'||column_name AS object_name, 
			       n.nspname AS object_schema,
			       'COLUMN'  AS object_type,
			       b.description
			  FROM (
				SELECT table_name table_or_view, column_name, ordinal_position
				  FROM information_schema.columns a
					LEFT OUTER JOIN pg_tables b1 ON 
						(b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
					LEFT OUTER JOIN pg_views b2  ON 
						(b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
				 WHERE tableowner IN (USER, 'rif40') OR viewowner IN (USER, 'rif40')) c, 
				pg_class a
				LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid)
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE a.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, 'rif40')) 
			   AND b.objsubid = c.ordinal_position
			   AND a.relname  = c.table_or_view
		), b AS (
			SELECT object_type, object_schema, COUNT(object_name) AS total_objects, 
			       CASE
					WHEN object_schema = 'pg_toast' OR
					     object_type IN ('INDEX', 'COMPOSITE TYPE') THEN NULL
					ELSE 					     	     COUNT(object_name)-COUNT(description) 
			       END AS missing_comments
			  FROM a
			 GROUP BY object_type, object_schema
		)
		SELECT object_type, object_schema, total_objects, missing_comments
		  FROM b
		 ORDER BY 1, 2;
	c12 CURSOR(l_object_type VARCHAR, l_object_schema VARCHAR) FOR /* All rif40_objects and comments */
		WITH a AS (
			SELECT c.relname AS object_name, 
			       n.nspname AS object_schema, 
			       CASE 
					WHEN c.relkind        = 'r' AND			/* Relational table */
					     c.relpersistence = 'p' 			/* Persistence: unlogged */	 THEN 'TABLE'
					WHEN c.relkind        = 'r' AND			/* Relational table */
					     c.relpersistence = 'u' 			/* Persistence: unlogged */ 	THEN 'UNLOGGED TABLE'
					WHEN c.relkind        = 'r' AND			/* Relational table */
					     c.relpersistence = 't' 			/* Persistence: temporary */ 	THEN 'TEMPORARY TABLE'
					WHEN c.relkind        = 'i'			/* Index */			THEN 'INDEX'
					WHEN c.relkind        = 'v'			/* View */			THEN 'VIEW'
					WHEN c.relkind        = 'S'			/* Sequence */			THEN 'SEQUENCE'
					WHEN c.relkind        = 't'			/* TOAST Table */		THEN 'TOAST TABLE'
					WHEN c.relkind        = 'f'			/* Foreign Table */		THEN 'FOREIGN TABLE'
					WHEN c.relkind        = 'c'			/* Composite type */		THEN 'COMPOSITE TYPE'
					ELSE 										     'Unknown relkind: '||c.relkind
			       END AS object_type,
			       d.description
			  FROM pg_class c
				LEFT OUTER JOIN pg_namespace n ON (n.oid = c.relnamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid AND (d.objsubid IS NULL OR d.objsubid = 0))
			 WHERE c.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', USER))
			UNION
			SELECT p.proname AS object_name, 
			       n.nspname AS object_schema,
			       'FUNCTION'  AS object_type,
			       d.description
			  FROM pg_proc p
				LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = p.oid AND d.objsubid = 0)
			 WHERE p.proowner IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', USER))
			UNION
			SELECT t.tgname AS object_name, 
			       n.nspname AS object_schema,
			       'TRIGGER'  AS object_type,
			       d.description
			  FROM pg_trigger t, pg_class c
				LEFT OUTER JOIN pg_namespace n ON (n.oid = c.relnamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid AND d.objsubid = 0)
			 WHERE c.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', USER))
			   AND c.oid = t.tgrelid
			   AND t.tgisinternal = FALSE /* trigger function */
			UNION
			SELECT DISTINCT relname||'.'||column_name AS object_name, 
			       n.nspname AS object_schema,
			       'COLUMN'  AS object_type,
			       b.description
			  FROM (
				SELECT table_name table_or_view, column_name, ordinal_position
				  FROM information_schema.columns a
					LEFT OUTER JOIN pg_tables b1 ON 
						(b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
					LEFT OUTER JOIN pg_views b2  ON 
						(b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
				 WHERE tableowner IN (USER, 'rif40') OR viewowner IN (USER, 'rif40')
                   		   AND a.table_schema  != 'rif_studies'			/* Exclude map/extract tables */
				) c, 
				pg_class a
				LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid)
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE a.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, 'rif40')) 
			   AND b.objsubid = c.ordinal_position
			   AND a.relname  = c.table_or_view
		)
		SELECT object_type, object_schema, object_name
		  FROM a
		 WHERE NOT (object_schema = 'pg_toast' OR object_type IN ('INDEX'))
		   AND description IS NULL
		   AND l_object_schema = object_schema
		   AND l_object_type   = object_type
		 ORDER BY 1, 2, 3;
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
--
	i INTEGER:=0;
	j INTEGER:=0;
	k INTEGER:=0;
--
	schema_owner VARCHAR:='rif40';
	tag VARCHAR;
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager rol
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_ddl_checks', 'User % must be rif40 or have rif_user or rif_manager role',
			USER::VARCHAR);
	END IF;
--
-- Schema owner is RIF40 unless we are RIFUPG34 and then it is public
--
	IF USER = 'rifupg34' THEN
		schema_owner:='public';
	END IF;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for missing table/view column comments');
	FOR c4_rec IN c4(schema_owner) LOOP
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Missing table/view column comment: %.%', 
			c4_rec.table_or_view::VARCHAR, c4_rec.column_name::VARCHAR);
		i:=i+1;
	END LOOP;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for missing table/view columns');
	FOR c3_rec IN c3(schema_owner) LOOP
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Missing table/view column: %.%', 
			c3_rec.table_or_view::VARCHAR, c3_rec.column_name::VARCHAR);
		i:=i+1;
	END LOOP;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for missing table/view comments');
	FOR c2_rec IN c2(schema_owner) LOOP
		IF c2_rec.description IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Missing table/view comment: %', 
				c2_rec.table_or_view::VARCHAR);
			i:=i+1;
		END IF;
	END LOOP;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for extra table/views');
	FOR c6_rec IN c6(schema_owner) LOOP
		IF USER NOT IN ('rif40', 'rifupg34') AND c6_rec.table_or_view IN ( /* Ordinary users - ignore  */
			'fdw_all_tab_columns', 'fdw_all_tables') THEN
			NULL;
		ELSIF USER IN ('peterh') THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Extra table/view: %.% [IGNORED]', 
				USER::VARCHAR,
				c6_rec.table_or_view::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Extra table/view: %', 
				c6_rec.table_or_view::VARCHAR);
			j:=j+1;
		END IF;
	END LOOP;
--
	IF USER != 'rifupg34' THEN
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for missing table triggers');
		FOR c5_rec IN c5(schema_owner) LOOP
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Missing table trigger: (%) % [TRIGGER NEEDS TO BE ADDED]', 
				c5_rec.table_name::VARCHAR, c5_rec.trigger_name::VARCHAR);
			k:=k+1;
		END LOOP;
		IF k > 0 THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', '% missing table triggers', 
				k::VARCHAR);
			i:=i+k; /* This is now an error */
		END IF;
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for missing sequences');
		FOR c7_rec IN c7(schema_owner) LOOP
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Missing sequences: %', 
				c7_rec.sequences::VARCHAR);
			i:=i+1;
		END LOOP;
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for missing tables, views and sequences GRANT SELECT to rif_user and rif_manager');
		FOR c8_rec IN c8(schema_owner) LOOP
			IF (c8_rec.has_user_select = FALSE OR c8_rec.has_rif_user_select = FALSE OR c8_rec.has_rif_manager_select = FALSE) AND c8_rec.has_public_select = FALSE THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Missing grant SELECT on % to rif_user(%) and rif_manager(%); user(%); public(%)', 
					c8_rec.table_or_view::VARCHAR, c8_rec.has_rif_user_select::VARCHAR, c8_rec.has_rif_manager_select::VARCHAR, 
					c8_rec.has_user_select::VARCHAR, c8_rec.has_public_select);
				IF c8_rec.has_rif_user_select = FALSE THEN
					PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Required SQL> GRANT SELECT ON % TO rif_user;', 
					c8_rec.table_or_view::VARCHAR);
				END IF;
				IF c8_rec.has_rif_manager_select = FALSE THEN
					PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Required SQL> GRANT SELECT ON % TO rif_manager;', c8_rec.table_or_view);
				END IF;
				i:=i+1;
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl_checks', 'SELECT grant OK on % to rif_user(%) and rif_manager(%); user(%); public(%)', 
					c8_rec.table_or_view::VARCHAR, c8_rec.has_rif_user_select::VARCHAR, c8_rec.has_rif_manager_select::VARCHAR, 
					c8_rec.has_user_select::VARCHAR, c8_rec.has_public_select::VARCHAR);
			END IF;
		END LOOP;
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking all functions in rif40_sql_pkg, rif40_geo_pkg for GRANT EXECUTE to rif40, rif_user and rif_manager');
		FOR c9_rec IN c9(schema_owner) LOOP
			IF c9_rec.has_rif40_select = FALSE OR c9_rec.has_rif_user_select = FALSE OR c9_rec.has_rif_manager_select = FALSE THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Missing grant EXECUTE on % % to rif40(%), rif_user(%) and rif_manager(%)', 
					c9_rec.object_type::VARCHAR, c9_rec.function_name::VARCHAR, c9_rec.has_rif40_select::VARCHAR, 
					c9_rec.has_rif_user_select::VARCHAR, c9_rec.has_rif_manager_select::VARCHAR);
				i:=i+1;
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl_checks', 'EXECUTE grant OK on % % to rif40(%), rif_user(%) and rif_manager(%)', 
					c9_rec.object_type::VARCHAR, c9_rec.function_name::VARCHAR, c9_rec.has_rif40_select::VARCHAR, 
					c9_rec.has_rif_user_select::VARCHAR, c9_rec.has_rif_manager_select::VARCHAR);
			END IF;
		END LOOP;
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for extra table/view columns');
		FOR c10_rec IN c10(schema_owner) LOOP
			IF USER NOT IN ('rif40', 'rifupg34') AND c10_rec.table_or_view IN ( /* Ordinary users - ignore  */
				'fdw_all_tab_columns', 'fdw_all_tables') THEN
				NULL;
			ELSIF c10_rec.table_schema IN ('gis', 'peterh') THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Extra table/view column: %.%.% [IGNORED]', 
					c10_rec.table_schema::VARCHAR, c10_rec.table_or_view::VARCHAR, c10_rec.column_name::VARCHAR);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Extra table/view column: %.%.%', 
					c10_rec.table_schema::VARCHAR, c10_rec.table_or_view::VARCHAR, c10_rec.column_name::VARCHAR);
				j:=j+1;
			END IF;
		END LOOP;
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for missing comments');
		FOR c11_rec IN c11 LOOP
		 	IF c11_rec.missing_comments > 0 THEN
				IF c11_rec.object_schema LIKE 'rif40%' THEN
					tag:='WARNING';
				ELSE
					tag:='INFO';
				END IF;
				PERFORM rif40_log_pkg.rif40_log(tag::rif40_log_pkg.rif40_log_debug_level, 'rif40_ddl_checks', 'Missing %/% comments for schema % %s', 
					c11_rec.missing_comments::VARCHAR, 
					c11_rec.total_objects::VARCHAR,
					c11_rec.object_schema::VARCHAR, 
					c11_rec.object_type::VARCHAR); 
				FOR c12_rec IN c12(c11_rec.object_type, c11_rec.object_schema) LOOP
					IF c11_rec.object_schema LIKE 'rif40%' THEN
						i:=i+1;
				END IF;
					PERFORM rif40_log_pkg.rif40_log(tag::rif40_log_pkg.rif40_log_debug_level, 'rif40_ddl_checks', 'Missing comment for % %.%', 
						c12_rec.object_type::VARCHAR,
						c12_rec.object_schema::VARCHAR, 
						c12_rec.object_name::VARCHAR);
				END LOOP;
			ELSE
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'No missing comments for schema % %s (%)', 
					c11_rec.object_schema::VARCHAR, 
					c11_rec.object_type::VARCHAR,
					c11_rec.total_objects::VARCHAR);
			END IF;
		END LOOP;
	END IF;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 'Checking for missing table/views');
	FOR c1_rec IN c1(schema_owner) LOOP
		IF USER = 'rifupg34' AND c1_rec.table_or_view IN ( /* Not created in the upgrade schema */
				'g_rif40_comparison_areas', 'g_rif40_study_areas', 'rif40_user_version') THEN
			NULL;
		ELSIF USER = 'rifupg34' THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Missing foreign data wrapper table/table/view: %', 
				c1_rec.table_or_view::VARCHAR);
			i:=i+1;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_checks', 'Missing table/view: %', 
				c1_rec.table_or_view::VARCHAR);
			i:=i+1;
		END IF;
	END LOOP;
--
	IF i > 0 OR j > 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_ddl_checks', 
			'Schema: % % missing tables/triggers/views/columns/comments/sequences/grants; % extra; % warnings', 
			schema_owner::VARCHAR, i::VARCHAR, j::VARCHAR, k::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_checks', 
			'Schema: % All tables/triggers/views/columns/comments/sequences/grants present; % warnings', 
			schema_owner::VARCHAR, k::VARCHAR);
	END IF;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_checks() IS 'Function: 	rif40_ddl_checks()
Parameters: 	None
Returns: 	Nothing
Description:	Validate RIF DDL

Check for:

a) Missing tables and views
b) Missing table/view comment
c) Missing table/view columns
d) Missing table/view column comments
e) Missing triggers
f) Extra tables and views
g) Missing sequences
h) All tables, views and sequences GRANT SELECT to rif_user and rif_manager
i) All functions in rif40_sql_pkg, rif40_geo_pkg GRANT EXECUTE to rif40, rif_user and rif_manager
j) Extra columns

Will work as any RIF user, not just RIF40';

\df rif40_sql_pkg.rif40_ddl_checks

GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl_checks() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl_checks() TO PUBLIC;

-- SELECT rif40_sql_pkg.rif40_ddl_checks();

--
-- Eof
