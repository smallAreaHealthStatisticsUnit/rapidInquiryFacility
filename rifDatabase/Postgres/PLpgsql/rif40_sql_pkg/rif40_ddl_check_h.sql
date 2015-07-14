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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check h) 
--                                All tables, views and sequences GRANT SELECT to rif_user and rif_manager
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
--rif40_ddl_check_h:									70400 to 70449
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_h()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_h()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check h) All tables, views and sequences GRANT SELECT to rif_user and rif_manager
         Exclude missing tables/views (i.e. check a) 
 */
DECLARE
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
--
	c8_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	i INTEGER:=0;
BEGIN	
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_h', '[70400]: Checking for missing tables, views and sequences GRANT SELECT to rif_user and rif_manager');
		FOR c8_rec IN c8(schema_owner) LOOP
			IF (c8_rec.has_user_select = FALSE OR c8_rec.has_rif_user_select = FALSE OR c8_rec.has_rif_manager_select = FALSE) AND c8_rec.has_public_select = FALSE THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_h', '[70401]: Missing grant SELECT on % to rif_user(%) and rif_manager(%); user(%); public(%)', 
					c8_rec.table_or_view::VARCHAR, c8_rec.has_rif_user_select::VARCHAR, c8_rec.has_rif_manager_select::VARCHAR, 
					c8_rec.has_user_select::VARCHAR, c8_rec.has_public_select::VARCHAR);
				IF c8_rec.has_rif_user_select = FALSE THEN
					PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_h', '[70402]: Required SQL> GRANT SELECT ON % TO rif_user;', 
					c8_rec.table_or_view::VARCHAR);
				END IF;
				IF c8_rec.has_rif_manager_select = FALSE THEN
					PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_h', '[70403]: Required SQL> GRANT SELECT ON % TO rif_manager;', c8_rec.table_or_view);
				END IF;
				i:=i+1;
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl_check_h', '[70404]: SELECT grant OK on % to rif_user(%) and rif_manager(%); user(%); public(%)', 
					c8_rec.table_or_view::VARCHAR, c8_rec.has_rif_user_select::VARCHAR, c8_rec.has_rif_manager_select::VARCHAR, 
					c8_rec.has_user_select::VARCHAR, c8_rec.has_public_select::VARCHAR);
			END IF;
		END LOOP;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_h() IS 'Function: 		rif40_ddl_check_h()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check h) All tables, views and sequences GRANT SELECT to rif_user and rif_manager
         Exclude missing tables/views (i.e. check a) ';

--
-- Eof