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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check j) Extra table/view columns
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

-- Fix partitioning problem

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
--rif40_ddl_check_j:									70500 to 70549
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_j()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_j()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check j) Extra table/view columns in INFORMATION_SCHEMA.COLUMS:
        Excluding using NOT IN:
		 * map/extract tables 
		 * Geolevel lookup tables (t_rif40_geolevels.lookup_table)
		 * Hierarchy tables (t_rif40_geolevels.hierarchytable)
		 * All partitions of tables (if they appear as tables)
		 * Numerator, denominator tables (rif40_tables.table_name)
		 * Covariate tables (t_rif40_geolevels.covariate_table)
		 * Loaded shapefile tables (t_rif40_geolevels.shapefile_table)
		 * Loaded centroids tables (t_rif40_geolevels.centroids_table)
		 * Foreign data wrapper (Oracle) tables listed in rif40_fdw_tables created on startup by: rif40_sql_pkg.rif40_startup()
		Removing (EXCEPT/MINUS depending on database):
         * Columns in rif40.columns
		   Ignoring using NOT IN/WHERE:
		   * Missing tables/views (i.e. check a)
		   * G_ temporary tables created created on startup by: rif40_sql_pkg.rif40_startup()
 */
DECLARE
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
					   AND n.nspname IN (USER, l_schema, 'rif_data', 'pop', 'gis', 'data_load')
					   AND c.relowner IN (SELECT oid FROM pg_roles 
										  WHERE rolname IN (USER, 'pop', 'gis', 'data_load'))
					) b3 ON (b3.fdw_table = a.table_name)
		 WHERE (tableowner IN (USER, l_schema, 'pop', 'gis', 'data_load') 
				OR viewowner IN (USER, l_schema, 'pop', 'gis', 'data_load') 
				OR fdw_tableowner = USER)
           AND a.table_schema NOT IN ('rif_studies', 'rif40_partitions')			/* Exclude map/extract, partitioned tables */
		   AND NOT (a.table_name IN ('g_rif40_comparison_areas', 'g_rif40_study_areas', 'user_role_privs', 
									 'sahsuland_geography_test')
    		       OR   a.table_name IN (
			SELECT viewname table_or_view		 		/* Geospatial views created by rif40_geo_pkg functions */
			  FROM pg_views
			 WHERE viewname IN (
			      SELECT DISTINCT 'rif40_'||LOWER(geography)||'_maptiles' viewname
										/* Maptiles view */
			        FROM rif40_geographies
					)
				)
    		       OR   a.table_name IN (
			SELECT tablename table_or_view		 		/* Geospatial views created by rif40_geo_pkg functions */
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
			      SELECT DISTINCT 't_rif40_'||LOWER(geography)||'_maptiles' tablename
										/* Maptiles tables */
			        FROM rif40_geographies
			       UNION 
			      SELECT DISTINCT 'p_rif40_geolevels_geometry_'||	/* Geometry table partitions */
					LOWER(geography)||'_'||LOWER(geolevel_name) tablename
			        FROM t_rif40_geolevels
			       UNION 
			      SELECT DISTINCT 'p_rif40_geolevels_maptiles_'||	/* Maptile table partitions */
					LOWER(geography)||'_'||LOWER(geolevel_name)||'_zoom_'||generate_series(0, 11, 1)::Text tablename
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
			 WHERE t.tableowner IN (USER, l_schema, 'pop', 'gis', 'data_load')
			   AND t.schemaname IN (USER, l_schema, 'rif_data', 'pop', 'gis', 'data_load')
			   AND c.relowner   IN (SELECT oid FROM pg_roles 
									 WHERE rolname IN (USER, l_schema, 'pop', 'gis', 'data_load'))
			   AND c.relname    = t.tablename
			   AND c.relkind    = 'r' 				/* Relational table */
			   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
			EXCEPT
			SELECT viewname table_or_view				/* Local views */
			  FROM pg_views
			 WHERE viewowner  IN (USER, l_schema, 'pop', 'gis', 'data_load')
			   AND schemaname IN (USER, l_schema, 'rif_data', 'pop', 'gis', 'data_load')
		)
		   AND table_or_view_name_hide NOT LIKE 'G%'
		)
		SELECT b.table_schema, a.table_or_view, a.column_name
		  FROM a, information_schema.columns b
		 WHERE a.table_or_view = b.table_name
		   AND a.column_name   = b.column_name
		   AND b.table_schema NOT IN ('rif40_partitions')			/* Exclude partitioned tables */
		 ORDER BY 1, 2;
--
	c10_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	i INTEGER:=0;
BEGIN	 
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_j', '[70500]: Checking for extra table/view columns');
		FOR c10_rec IN c10(schema_owner) LOOP
			IF USER NOT IN ('rif40', 'rifupg34') AND c10_rec.table_or_view IN ( /* Ordinary users - ignore  */
				'fdw_all_tab_columns', 'fdw_all_tables') THEN
				NULL;
			ELSIF c10_rec.table_or_view IN ('range_partition_test_old', 'range_partition_test_new',
										    'hash_partition_test_old',  'hash_partition_test_new') THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_j', 
					'[70501]: Extra partition related temporary table: %.% [IGNORED]', 
					c10_rec.table_schema::VARCHAR, c10_rec.table_or_view::VARCHAR);				
			ELSIF c10_rec.table_schema IN ('gis', 'rif_data') THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_j', 
					'[70502]: Extra table/view column: %.%.% [IGNORED]', 
					c10_rec.table_schema::VARCHAR, c10_rec.table_or_view::VARCHAR, c10_rec.column_name::VARCHAR);
			ELSIF c10_rec.table_schema = current_user  THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_j', 
					'[70503]: Extra user table/view column: %.%.% [IGNORED]', 
					c10_rec.table_schema::VARCHAR, c10_rec.table_or_view::VARCHAR, c10_rec.column_name::VARCHAR);
			ELSIF c10_rec.column_name IN ('hash_partition_number') THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_j', 
					'[70504]: Extra partition related table/view column: %.%.% [IGNORED]', 
					c10_rec.table_schema::VARCHAR, c10_rec.table_or_view::VARCHAR, c10_rec.column_name::VARCHAR);								
			ELSE
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_j', 
					'[70505]: Extra table/view column: %.%.% [FAILURE]', 
					c10_rec.table_schema::VARCHAR, c10_rec.table_or_view::VARCHAR, c10_rec.column_name::VARCHAR);
				i:=i+1;
			END IF;
		END LOOP;
--
	IF i = 0 THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_j', '[70506]: Any extra table/view column(s) were IGNORED');
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_j', '[70507]: % extra table/view column(s) are FAILURES', 
			i::VARCHAR);
	END IF;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_j() IS 'Function: 		rif40_ddl_check_j()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check j) Extra table/view columns in INFORMATION_SCHEMA.COLUMS:
         Excluding using NOT IN:
		  * map/extract tables 
		  * Geolevel lookup tables (t_rif40_geolevels.lookup_table)
		  * Hierarchy tables (t_rif40_geolevels.hierarchytable)
		  * All partitions of tables (if they appear as tables)
		  * Numerator, denominator tables (rif40_tables.table_name)
		  * Covariate tables (t_rif40_geolevels.covariate_table)
		  * Loaded shapefile tables (t_rif40_geolevels.shapefile_table)
		  * Loaded centroids tables (t_rif40_geolevels.centroids_table)
		  * Foreign data wrapper (Oracle) tables listed in rif40_fdw_tables created on startup by: rif40_sql_pkg.rif40_startup()
		 Removing (EXCEPT/MINUS depending on database):
          * Columns in rif40.columns
		    Ignoring using NOT IN/WHERE:
		    * Missing tables/views (i.e. check a)
		    * G_ temporary tables created created on startup by: rif40_sql_pkg.rif40_startup()';

--
-- Eof