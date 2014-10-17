-- ************************************************************************
-- *
-- * DO NOT EDIT THIS SCRIPT OR MODIFY THE RIF SCHEMA - USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - Table/view/sequence grants
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

--
-- Check database is sahsuland_dev
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'Database check: %', current_database();	
	ELSE
		RAISE EXCEPTION 'C20901: Database check failed: % is not sahsuland_dev', current_database();	
	END IF;
END;
$$;

\echo Table/view/sequence grants...

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C209xx: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

GRANT SELECT,REFERENCES ON rif40_age_group_names TO PUBLIC;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_age_group_names TO rif_manager;
GRANT SELECT,REFERENCES ON rif40_age_groups TO PUBLIC;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_age_groups TO rif_manager;
-- GRANT SELECT,REFERENCES ON rif40_chi2 TO PUBLIC;
-- GRANT SELECT,REFERENCES ON rif40_pois_distribution TO PUBLIC;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_parameters TO rif_manager;
GRANT SELECT ON t_rif40_parameters TO PUBLIC;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_geographies TO rif_manager;
GRANT SELECT,REFERENCES ON rif40_geographies TO PUBLIC;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_geolevels TO rif_manager;
GRANT SELECT ON t_rif40_geolevels TO PUBLIC;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_covariates TO rif_manager;
GRANT SELECT ON rif40_covariates TO rif_user;
GRANT SELECT ON rif40_version TO PUBLIC;
GRANT UPDATE ON rif40_version TO rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_tables TO rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_health_study_themes TO rif_manager;
GRANT SELECT,REFERENCES ON rif40_health_study_themes TO PUBLIC;
GRANT SELECT,REFERENCES ON rif40_tables TO PUBLIC;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_fdw_tables TO rif_user, rif_manager;
GRANT SELECT,REFERENCES ON t_rif40_fdw_tables TO PUBLIC;
GRANT SELECT ON rif40_error_messages TO PUBLIC;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_studies TO rif_user, rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_investigations TO rif_user, rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_inv_conditions TO rif_user, rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_inv_covariates TO rif_user, rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_study_shares TO rif_manager;
GRANT SELECT ON rif40_study_shares TO rif_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_study_areas TO rif_user, rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_comparison_areas TO rif_user, rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_results TO rif_user, rif_manager;
--GRANT INSERT,DELETE ON t_rif40_result_maps TO rif_user, rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_contextual_stats TO rif_user, rif_manager;

GRANT SELECT ON rif40_reference_tables TO PUBLIC;
GRANT SELECT ON rif40_outcomes TO PUBLIC;
GRANT SELECT ON rif40_outcome_groups TO PUBLIC;
GRANT SELECT ON rif40_table_outcomes TO PUBLIC;
GRANT SELECT ON rif40_predefined_groups TO PUBLIC;
GRANT SELECT ON rif40_population_us TO PUBLIC;
GRANT SELECT ON rif40_population_europe TO PUBLIC;
GRANT SELECT ON rif40_population_world TO PUBLIC;
GRANT SELECT ON rif40_icd9 TO PUBLIC;
GRANT SELECT ON rif40_icd10 TO PUBLIC;
GRANT SELECT ON rif40_opcs4 TO PUBLIC;
GRANT SELECT ON rif40_icd_o_3 TO PUBLIC;
GRANT SELECT ON rif40_a_and_e TO PUBLIC;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_outcomes TO rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_outcome_groups TO rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_table_outcomes TO rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON rif40_predefined_groups TO rif_manager;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_study_sql TO rif_user, rif_manager; 
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_study_sql_log TO rif_user, rif_manager; 
GRANT SELECT ON t_rif40_projects TO rif_user;
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_projects TO rif_manager;
GRANT SELECT ON t_rif40_user_projects TO rif_user; 
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_user_projects TO rif_manager; 
GRANT SELECT,INSERT,UPDATE,DELETE ON t_rif40_fdw_tables TO rif_user, rif_manager; 

GRANT SELECT ON rif40_tables_and_views TO rif_manager; 
GRANT SELECT ON rif40_triggers TO rif_manager; 
GRANT SELECT ON rif40_columns TO rif_manager; 
GRANT SELECT ON rif40_dual TO PUBLIC;

GRANT SELECT ON rif40.sahsuland_cancer TO rif_user, rif_manager; 
GRANT SELECT ON rif40.sahsuland_pop TO rif_user, rif_manager; 

--
-- Grant SELECT to all views
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT viewname 								/* Local views */
		  FROM pg_views
		 WHERE viewowner = 'rif40';
--
	c1_rec RECORD;
--
	sql_stmt VARCHAR;
BEGIN
--
	FOR c1_rec IN c1 LOOP
		sql_stmt:='GRANT SELECT ON '||c1_rec.viewname||' TO rif_user, rif_manager'; 
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$$;

--
-- Grant SELECT to all referenced tables to rif_user, rif_manager
--
DO LANGUAGE plpgsql $$
DECLARE
	c1g CURSOR FOR
		SELECT 'GRANT SELECT ON '||rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR)||'.'||tablename||
		       		' TO rif_user, rif_manager' sql_stmt
		  FROM pg_tables
		 WHERE tablename IN (
		      SELECT DISTINCT LOWER(lookup_table) tablename/* Geolevel lookup tables */
  		        FROM t_rif40_geolevels WHERE lookup_table IS NOT NULL
		       UNION 
		      SELECT DISTINCT LOWER(hierarchytable) tablename/* Hierarchy table */
		        FROM rif40_geographies
		       UNION 
		      SELECT DISTINCT 't_rif40_'||LOWER(geography)||'_geometry' tablename /* Geometry tables */
		        FROM rif40_geographies
		       UNION 
		      SELECT DISTINCT LOWER(covariate_table) tablename/* Covariate tables */
		        FROM t_rif40_geolevels WHERE covariate_table IS NOT NULL
		       UNION 
		      SELECT DISTINCT LOWER(shapefile_table) tablename/* Loaded shapefile tables */
		        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
 		       UNION 
		      SELECT DISTINCT LOWER(centroids_table) tablename/* Centroids tables */
		        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL)
 		   AND rif40_sql_pkg.rif40_object_resolve(tablename::VARCHAR) IS NOT NULL /* Object exists in the search path */;
--
	c1g_rec RECORD;
BEGIN
--
	FOR c1g_rec IN c1g LOOP
		PERFORM rif40_sql_pkg.rif40_ddl(c1g_rec.sql_stmt);
	END LOOP;
END;
$$;

--
-- Added above
--
/* 
GRANT SELECT ON rif40_comparison_areas TO rif_user; 
GRANT SELECT ON rif40_comparison_areas TO rif_manager; 
GRANT SELECT ON rif40_contextual_stats TO rif_user; 
GRANT SELECT ON rif40_contextual_stats TO rif_manager; 
GRANT SELECT ON rif40_dual TO rif_user; 
GRANT SELECT ON rif40_dual TO rif_manager; 
GRANT SELECT ON rif40_geolevels TO rif_user; 
GRANT SELECT ON rif40_geolevels TO rif_manager; 
GRANT SELECT ON rif40_inv_conditions TO rif_user; 
GRANT SELECT ON rif40_inv_conditions TO rif_manager; 
GRANT SELECT ON rif40_inv_covariates TO rif_user; 
GRANT SELECT ON rif40_inv_covariates TO rif_manager; 
GRANT SELECT ON rif40_investigations TO rif_user; 
GRANT SELECT ON rif40_investigations TO rif_manager; 
GRANT SELECT ON rif40_num_denom TO rif_user; 
GRANT SELECT ON rif40_num_denom TO rif_manager; 
GRANT SELECT ON rif40_num_denom_errors TO rif_user; 
GRANT SELECT ON rif40_num_denom_errors TO rif_manager; 
GRANT SELECT ON rif40_parameters TO rif_user; 
GRANT SELECT ON rif40_parameters TO rif_manager; 
GRANT SELECT ON rif40_projects TO rif_user; 
GRANT SELECT ON rif40_projects TO rif_manager; 
GRANT SELECT ON rif40_results TO rif_user; 
GRANT SELECT ON rif40_results TO rif_manager; 
GRANT SELECT ON rif40_studies TO rif_user; 
GRANT SELECT ON rif40_studies TO rif_manager; 
GRANT SELECT ON rif40_study_areas TO rif_user; 
GRANT SELECT ON rif40_study_areas TO rif_manager; 

GRANT SELECT ON t_rif40_comparison_areas TO rif_user; 
GRANT SELECT ON t_rif40_comparison_areas TO rif_manager; 
GRANT SELECT ON t_rif40_contextual_stats TO rif_user; 
GRANT SELECT ON t_rif40_contextual_stats TO rif_manager; 
GRANT SELECT ON t_rif40_inv_conditions TO rif_user; 
GRANT SELECT ON t_rif40_inv_conditions TO rif_manager; 
GRANT SELECT ON t_rif40_inv_covariates TO rif_user; 
GRANT SELECT ON t_rif40_inv_covariates TO rif_manager; 
GRANT SELECT ON t_rif40_investigations TO rif_user; 
GRANT SELECT ON t_rif40_investigations TO rif_manager; 
GRANT SELECT ON t_rif40_num_denom TO rif_user; 
GRANT SELECT ON t_rif40_num_denom TO rif_manager; 
GRANT SELECT ON t_rif40_projects TO rif_user; 
GRANT SELECT ON t_rif40_projects TO rif_manager; 
GRANT SELECT ON t_rif40_results TO rif_user; 
GRANT SELECT ON t_rif40_results TO rif_manager; 
GRANT SELECT ON t_rif40_studies TO rif_user; 
GRANT SELECT ON t_rif40_studies TO rif_manager; 
GRANT SELECT ON t_rif40_study_areas TO rif_user; 
GRANT SELECT ON t_rif40_study_areas TO rif_manager; 
GRANT SELECT ON t_rif40_study_sql TO rif_user; 
GRANT SELECT ON t_rif40_study_sql TO rif_manager; 
GRANT SELECT ON t_rif40_study_sql_log TO rif_user; 
GRANT SELECT ON t_rif40_study_sql_log TO rif_manager; 
GRANT SELECT ON t_rif40_user_projects TO rif_user; 
GRANT SELECT ON t_rif40_user_projects TO rif_manager; 
 */

GRANT SELECT,UPDATE ON SEQUENCE rif40_inv_id_seq TO rif_user;
GRANT SELECT,UPDATE ON SEQUENCE rif40_study_id_seq TO rif_manager;
GRANT SELECT,UPDATE ON SEQUENCE rif40_study_id_seq TO rif_user; 
GRANT SELECT,UPDATE ON SEQUENCE rif40_inv_id_seq TO rif_manager; 

GRANT SELECT ON rif40_user_version TO rif_user;
GRANT SELECT ON rif40_user_version TO rif_manager;

\echo Table/view/sequence grants done.
\q

--
-- Eof
