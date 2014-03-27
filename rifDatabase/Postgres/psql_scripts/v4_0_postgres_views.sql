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
-- Rapid Enquiry Facility (RIF) - Create Postgres views
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

CREATE OR REPLACE VIEW "rif40_comparison_areas" ("username", "study_id", "area_id") AS SELECT  username, c.study_id, area_id
   FROM t_rif40_comparison_areas c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_contextual_stats" ("username", "study_id", "inv_id", "area_id", "area_population", "area_observed", "total_comparision_population", "variance_high", "variance_low") AS SELECT  username, c.study_id, inv_id, area_id, area_population, area_observed, total_comparision_population, variance_high, variance_low
   FROM t_rif40_contextual_stats c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_fdw_tables" ("username", "table_name", "create_status", "error_message", "date_created", "rowtest_passed") AS SELECT  username, table_name, create_status, error_message, date_created, rowtest_passed
   FROM t_rif40_fdw_tables
 WHERE username = USER;
CREATE OR REPLACE VIEW "rif40_geolevels" ("geography", "geolevel_name", "geolevel_id", "description", "lookup_table", "lookup_desc_column", "shapefile", "centroidsfile", "shapefile_table", "shapefile_area_id_column", "shapefile_desc_column", "st_simplify_tolerance", "centroids_table", "centroids_area_id_column", "avg_npoints_geom", "avg_npoints_opt", "file_geojson_len", "leg_geom", "leg_opt", "covariate_table", "resolution", "comparea", "listing", "restricted", "centroidxcoordinate_column", "centroidycoordinate_column") AS SELECT  a.geography, a.geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, /*     shapefile_geometry, */
       shapefile, centroidsfile, shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance,
       centroids_table, centroids_area_id_column, avg_npoints_geom, avg_npoints_opt, file_geojson_len, leg_geom, leg_opt,
       covariate_table, resolution, comparea, listing, restricted,
       centroidxcoordinate_column, centroidycoordinate_column
   FROM t_rif40_geolevels a
 WHERE SYS_CONTEXT('SAHSU_CONTEXT', 'RIF_STUDENT') = 'YES'
   AND restricted != 1
UNION
SELECT a.geography, a.geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, /*     shapefile_geometry, */
       shapefile, centroidsfile, shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance,
       centroids_table, centroids_area_id_column, avg_npoints_geom, avg_npoints_opt, file_geojson_len, leg_geom, leg_opt,
       covariate_table, resolution, comparea, listing, restricted,
       centroidxcoordinate_column, centroidycoordinate_column
  FROM t_rif40_geolevels a
 WHERE SYS_CONTEXT('SAHSU_CONTEXT', 'RIF_STUDENT') IS NULL OR SYS_CONTEXT('SAHSU_CONTEXT', 'RIF_STUDENT') = 'NO'
 ORDER BY 1, 3 DESC;
CREATE OR REPLACE VIEW "rif40_investigations" ("username", "inv_id", "study_id", "inv_name", "year_start", "year_stop", "max_age_group", "min_age_group", "genders", "numer_tab", "mh_test_type", "geography", "inv_description", "classifier", "classifier_bands", "investigation_state") AS SELECT  username, inv_id, c.study_id, inv_name, year_start, year_stop, max_age_group, min_age_group, genders,
       numer_tab, mh_test_type, geography, inv_description, classifier, classifier_bands, investigation_state
   FROM t_rif40_investigations c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_inv_conditions" ("username", "study_id", "inv_id", "line_number", "condition") AS SELECT  username, c.study_id, inv_id, line_number, condition
   FROM t_rif40_inv_conditions c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_inv_covariates" ("username", "study_id", "inv_id", "covariate_name", "min", "max", "geography", "study_geolevel_name") AS SELECT  username, c.study_id, inv_id, covariate_name, min, max, geography, study_geolevel_name
   FROM t_rif40_inv_covariates c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_parameters" ("param_name", "param_value", "param_description") AS SELECT  param_name, param_value, param_description
   FROM t_rif40_parameters
 WHERE param_name != 'SuppressionValue'
UNION
SELECT param_name, (CASE WHEN a.total=1 THEN '0' /* Not Suppressed */ ELSE param_value END) param_value, param_description
  FROM t_rif40_parameters p, (
        SELECT COUNT(*) total
       	  FROM user_role_privs
	 WHERE granted_role = 'RIF_NO_SUPPRESSION') a
 WHERE param_name = 'SuppressionValue'
UNION
SELECT 'RifParametersTable' param_name, 'Virtual' param_value,
       'Is this the T_RIF40_PARAMETERS table or the VIRTUAL view' param_description
  
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_projects" ("project", "description", "date_started", "date_ended") AS SELECT  project, description, date_started, date_ended
   FROM t_rif40_projects
 WHERE project IN (
	SELECT project FROM t_rif40_user_projects
 	 WHERE username = USER OR
   	    ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')))
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_results" ("username", "study_id", "inv_id", "band_id", "genders", "direct_standardisation", "adjusted", "observed", "expected", "lower95", "upper95", "relative_risk", "smoothed_relative_risk", "posterior_probability", "posterior_probability_upper95", "posterior_probability_lower95", "residual_relative_risk", "residual_rr_lower95", "residual_rr_upper95", "smoothed_smr", "smoothed_smr_lower95", "smoothed_smr_upper95") AS SELECT  username, c.study_id, inv_id, band_id, genders, direct_standardisation, adjusted, observed, expected,
       lower95, upper95, relative_risk, smoothed_relative_risk,
       posterior_probability, posterior_probability_upper95, posterior_probability_lower95,
       residual_relative_risk, residual_rr_lower95, residual_rr_upper95,
       smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95
   FROM t_rif40_results c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_studies" ("username", "study_id", "extract_table", "study_name", "summary", "description", "other_notes", "study_date", "geography", "study_type", "study_state", "comparison_geolevel_name", "denom_tab", "direct_stand_tab", "year_start", "year_stop", "max_age_group", "min_age_group", "study_geolevel_name", "map_table", "suppression_value", "extract_permitted", "transfer_permitted", "authorised_by", "authorised_on", "authorised_notes", "audsid", "partition_parallelisation", "covariate_table", "project", "project_description") AS SELECT  username, c.study_id,
       extract_table, study_name, summary, c.description, other_notes, study_date,
       c.geography, study_type, study_state, comparison_geolevel_name,
       denom_tab, direct_stand_tab, year_start, year_stop, max_age_group, min_age_group, study_geolevel_name,
       map_table, suppression_value, extract_permitted, transfer_permitted,
       authorised_by, authorised_on, authorised_notes, audsid,
       CASE /* Parallelisation support */
		WHEN g.partition = 1 AND (i.year_stop - i.year_start) >= 2*p.parallelisation THEN p.parallelisation
		ELSE												0
       END partition_parallelisation,
       l.covariate_table,
       c.project, pj.description project_description
   FROM t_rif40_studies c
		LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
		LEFT OUTER JOIN (
			SELECT /*+ MATERIALIZE */ study_id,
			       MAX(year_stop) year_stop,
			       MIN(year_start) year_start,
			       MAX(max_age_group) max_age_group,
			       MIN(min_age_group) min_age_group
			  FROM t_rif40_investigations i2
			 GROUP BY study_id
		) i ON (c.study_id = i.study_id)
		LEFT OUTER JOIN rif40_geographies g  ON (c.geography  = g.geography)
		LEFT OUTER JOIN (
			SELECT TO_NUMBER(param_value, '999990') parallelisation
			  FROM t_rif40_parameters
			 WHERE param_name = 'Parallelisation') p ON (1=1)
		LEFT OUTER JOIN t_rif40_geolevels l  ON
			(c.geography  = l.geography AND c.study_geolevel_name = l.geolevel_name)
		LEFT OUTER JOIN t_rif40_projects pj  ON (pj.project   = c.project)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_study_areas" ("username", "study_id", "area_id", "band_id") AS SELECT  username, c.study_id, area_id, band_id
   FROM t_rif40_study_areas c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_study_sql" ("username", "study_id", "statement_type", "statement_number", "sql_text", "line_number", "status") AS SELECT  username, c.study_id,
       /* CASE WHEN statement_type = 1 THEN 'CREATE'
	    WHEN statement_type = 2 THEN 'INSERT'
	    WHEN statement_type = 3 THEN 'POST_INSERT'
	    WHEN statement_type = 4 THEN 'NUMERATOR_CHECK'
	    WHEN statement_type = 5 THEN 'DENOMINATOR_CHECK'
	    ELSE 'Unknown: '||statement_type END */ statement_type,
       statement_number, sql_text, line_number, status
   FROM t_rif40_study_sql c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_study_sql_log" ("username", "study_id", "statement_type", "statement_number", "log_message", "audsid", "log_sqlcode", "rowcount", "start_time", "elapsed_time") AS SELECT  username, c.study_id, statement_type,
       statement_number, log_message,
       audsid, log_sqlcode, rowcount, start_time, elapsed_time
   FROM t_rif40_study_sql_log c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_user_projects" ("project", "username", "grant_date", "revoke_date", "description", "date_started", "date_ended") AS SELECT  a.project, username, grant_date, revoke_date, description, date_started, date_ended
   FROM t_rif40_user_projects a, t_rif40_projects b
 WHERE a.project = b.project
   AND a.username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER'))
 ORDER BY 1;
CREATE OR REPLACE VIEW "rif40_user_version" ("user_schema_revision") AS SELECT CAST('1.0' AS numeric(6,3)) AS user_schema_revision ;

