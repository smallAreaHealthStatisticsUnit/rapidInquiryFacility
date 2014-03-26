-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/02/27 11:29:40 $
-- Type: Postgres PSQL script
-- $RCSfile: v4_0_postgres_grants.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/psql_scripts/v4_0_postgres_grants.sql,v $
-- $Revision: 1.6 $
-- $Id: v4_0_postgres_grants.sql,v 1.6 2014/02/27 11:29:40 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Table/view/sequence grants
--
-- Copyright:
--
-- The RIF is free software; you can redistribute it and/or modify it under
-- the terms of the GNU General Public License as published by the Free
-- Software Foundation; either version 2, or (at your option) any later
-- version.
--
-- The RIF is distributed in the hope that it will be useful, but WITHOUT ANY
-- WARRANTY; without even the implied warranty of MERCHANTABILITY or
-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
-- for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this file; see the file LICENCE.  If not, write to:
--
-- UK Small Area Health Statistics Unit,
-- Dept. of Epidemiology and Biostatistics
-- Imperial College School of Medicine (St. Mary's Campus),
-- Norfolk Place,
-- Paddington,
-- London, W2 1PG
-- United Kingdom
--
-- The RIF uses Oracle 11g, PL/SQL, PostGres and PostGIS as part of its implementation.
--
-- Oracle11g, PL/SQL and Pro*C are trademarks of the Oracle Corporation.
--
-- All terms mentioned in this software and supporting documentation that are known to be trademarks
-- or service marks have been appropriately capitalised. Imperial College cannot attest to the accuracy
-- of this information. The use of a term in this software or supporting documentation should NOT be
-- regarded as affecting the validity of any trademark or service mark.
--
-- Summary of functions/procedures:
--
-- To be added
--
-- Error handling strategy:
--
-- Output and logging procedures do not HANDLE or PROPAGATE errors. This makes them safe to use
-- in package initialisation and NON recursive.
--
-- References:
--
-- 	None
--
-- Dependencies:
--
--	Packages: None
--
-- 	<This should include: packages, non packages procedures and functions, tables, views, objects>
--
-- Portability:
--
--	Linux, Windows 2003/2008, Oracle 11gR1
--
-- Limitations:
--
-- Change log:
--
-- $Log: v4_0_postgres_grants.sql,v $
-- Revision 1.6  2014/02/27 11:29:40  peterh
--
-- About to test isolated code tree for trasnfer to Github/public network
--
-- Revision 1.5  2014/02/24 10:50:28  peterh
-- Full build from Oracle, including default study area and removal of year_start/stop min/max_age_group from T_RIF40_STUDIES
--
-- Still present in view
--
-- Revision 1.4  2013/09/02 14:08:33  peterh
--
-- Baseline after full trigger implmentation
--
-- Revision 1.3  2013/03/14 17:35:20  peterh
-- Baseline for TX to laptop
--
-- Revision 1.2  2013/02/18 15:47:16  peterh
--
-- Convert ORAPG to normal grant script
--
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

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

\echo Table/view/sequence grants done.

--
-- Eof
