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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check f) Extra tables and views
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
--rif40_ddl_check_f:									70300 to 70349
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_f()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_f()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check f) Extra tables and views:
		 * User temporary tables
		 * User foreign data wrapper (i.e. remote Oracle) tables
		 * User and rif40 local tables
		 * User and rif40 views
		Removing (EXCEPT/MINUS depending on database):
		* Tables listed in rif30_tables_and_views.table_name
		* Geolevel lookup tables (t_rif40_geolevels.lookup_table)
		* Hierarchy tables (t_rif40_geolevels.hierarchytable)
		* All partitions of tables (if they appear as tables)
		* Numerator, denominator tables (rif40_tables.table_name)
		* Covariate tables (t_rif40_geolevels.covariate_table)
		* Loaded shapefile tables (t_rif40_geolevels.shapefile_table)
		* Loaded centroids tables (t_rif40_geolevels.centroids_table)
 */
DECLARE
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
			SELECT viewname table_or_view		 		/* Geospatial views created by rif40_geo_pkg functions */
			  FROM pg_views
			 WHERE viewname IN (		 
			      SELECT DISTINCT 'rif40_'||LOWER(geography)||'_maptiles' viewname
										/* Maptiles tables */
			        FROM rif40_geographies
					)
			UNION
			SELECT tablename table_or_view		 		/* Geospatial tables created by rif40_geo_pkg functions */
			  FROM pg_tables
			 WHERE tablename IN (
				SELECT DISTINCT LOWER(lookup_table) tablename			/* Geolevel lookup tables */
			        FROM t_rif40_geolevels WHERE lookup_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(hierarchytable) tablename		/* Hierarchy table */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 't_rif40_'||LOWER(geography)||'_geometry' tablename
																		/* Geometry tables */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 'p_rif40_geolevels_geometry_'||		/* Geometry table partitions */
					LOWER(geography)||'_'||LOWER(geolevel_name) tablename
			        FROM t_rif40_geolevels
			       UNION 
			      SELECT DISTINCT 't_rif40_'||LOWER(geography)||'_maptiles' tablename
										/* Maptiles view */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 'p_rif40_geolevels_maptiles_'||		/* Maptile table partitions */
					LOWER(geography)||'_'||LOWER(geolevel_name)||'_zoom_'||generate_series(0, 11, 1)::Text tablename
			        FROM t_rif40_geolevels
			       UNION 
			      SELECT DISTINCT LOWER(table_name) tablename			/* Numerator and denominators */
			        FROM rif40_tables WHERE table_name IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(covariate_table) tablename		/* Covariate tables */
			        FROM t_rif40_geolevels WHERE covariate_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(shapefile_table) tablename		/* Loaded shapefile tables */
			        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
			       UNION 
			      SELECT DISTINCT LOWER(centroids_table) tablename		/* Centroids tables */
			        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL
				/* rif40_outcomes lookup tables not included in this list as supplied as part of the initial setup 
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
			      SELECT DISTINCT 'p_rif40_geolevels_geometry_'||	/* Geometry table partitions */
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
				/* rif40_outcomes lookup tables not included in this list as supplied as part of the initial setup 
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
--
	c6_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	i INTEGER:=0;
BEGIN	
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_f', '[70300]: Checking for extra table/views');
	FOR c6_rec IN c6(schema_owner) LOOP
		IF USER NOT IN ('rif40', 'rifupg34') AND c6_rec.table_or_view IN ( /* Ordinary users - ignore  */
			'fdw_all_tab_columns', 'fdw_all_tables') THEN
			NULL;
		ELSIF USER IN ('peterh') THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_f', '[70301]: Extra table/view: %.% [IGNORED]', 
				USER::VARCHAR,
				c6_rec.table_or_view::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_f', '[70302]: Extra table/view: %', 
				c6_rec.table_or_view::VARCHAR);
			i:=i+1;
		END IF;
	END LOOP;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_f() IS 'Function: 		rif40_ddl_check_f()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check f) Extra tables and views:
		 * User temporary tables
		 * User foreign data wrapper (i.e. remote Oracle) tables
		 * User and rif40 local tables
		 * User and rif40 views
		Removing (EXCEPT/MINUS depending on database):
		* Tables listed in rif30_tables_and_views.table_name
		* Geolevel lookup tables (t_rif40_geolevels.lookup_table)
		* Hierarchy tables (t_rif40_geolevels.hierarchytable)
		* All partitions of tables (if they appear as tables)
		* Numerator, denominator tables (rif40_tables.table_name)
		* Covariate tables (t_rif40_geolevels.covariate_table)
		* Loaded shapefile tables (t_rif40_geolevels.shapefile_table)
		* Loaded centroids tables (t_rif40_geolevels.centroids_table)';

--
-- Eof