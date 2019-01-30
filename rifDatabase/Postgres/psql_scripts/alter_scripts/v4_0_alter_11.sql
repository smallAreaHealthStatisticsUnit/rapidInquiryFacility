-- ************************************************************************
-- *
-- * THIS IS A SCHEMA ALTER SCRIPT - IT CAN BE RE-RUN BUT THEY MUST BE RUN 
-- * IN NUMERIC ORDER
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
-- Rapid Inquiry Facility (RIF) - RIF alter script 11 - More risk Analysis Enhancements; support for Postgres 10 partitioning
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
-- Working directory: c:/Users/Peter/Documents/GitHub/rapidInquiryFacility/rifDatabase/Postgres/psql_scripts
-- psql -U rif40 -d sahsuland -w -e -P pager=off -f alter_scripts/v4_0_alter_11.sql
--
-- The middleware must be down for this to run
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #11 More risk Analysis Enhancements; support for Postgres 10 partitioning.

/*

* Alter 11: More risk Analysis Enhancements; support for Postgres 10 partitioning

 1. Support for Postgres 10 partitioning;
 2. Intersection counting (study areas only);	
 3. Exposure value support;
 4. Add intersection counting and exposure value support to extracts;
 5. View rif40_exposure_values;
 
 */
BEGIN;

--
-- 1. Support for Postgres 10 partitioning
--
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_rif40_tables_checks.sql

--
-- 2. Intersection counting (study areas only)	
--
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE t_rif40_study_areas ADD COLUMN intersect_count INTEGER NULL;
	ALTER TABLE t_rif40_study_areas ADD COLUMN distance_from_nearest_source NUMERIC NULL;
	ALTER TABLE t_rif40_study_areas ADD COLUMN nearest_rifshapepolyid VARCHAR NULL;
	COMMENT ON COLUMN t_rif40_study_areas.intersect_count IS 'Number of intersects with shapes';
	COMMENT ON COLUMN t_rif40_study_areas.distance_from_nearest_source IS 'Distance from nearest source (Km)';
	COMMENT ON COLUMN t_rif40_study_areas.nearest_rifshapepolyid IS 'Nearest rifshapepolyid (shape reference)';
EXCEPTION	
	WHEN duplicate_column THEN
		RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;  
END;
$$;

--
-- 3. Exposure value support
--
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE t_rif40_study_areas ADD COLUMN exposure_value NUMERIC NULL;
	COMMENT ON COLUMN t_rif40_study_areas.exposure_value IS 'Exposure value (when bands selected by exposure values)';
EXCEPTION	
	WHEN duplicate_column THEN
		RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;  
END;
$$;

DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE t_rif40_studies ADD COLUMN risk_analysis_exposure_field VARCHAR(30) NULL;
	COMMENT ON COLUMN t_rif40_studies.risk_analysis_exposure_field IS 'Risk analysis exposure field (when bands selected by exposure values)';
EXCEPTION	
	WHEN duplicate_column THEN
		RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;  
END;
$$;
	
DROP VIEW IF EXISTS rif40.rif40_exposure_values;
DROP VIEW IF EXISTS rif40.rif40_study_areas;
DROP VIEW IF EXISTS rif40.rif40_studes;
ALTER TABLE t_rif40_study_areas ALTER COLUMN nearest_rifshapepolyid SET DATA TYPE VARCHAR;
ALTER TABLE t_rif40_study_areas ALTER COLUMN distance_from_nearest_source SET DATA TYPE NUMERIC;

--
-- Rebuild view: rif40_study_areas
--
CREATE VIEW rif40.rif40_study_areas AS
 SELECT c.username,
    c.study_id,
    c.area_id,
    c.band_id,
	c.intersect_count,
	c.distance_from_nearest_source,
	c.nearest_rifshapepolyid,
	c.exposure_value
   FROM t_rif40_study_areas c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username::name = "current_user"()
  WHERE c.username::name = "current_user"() OR 'RIF_MANAGER'::text = (( SELECT user_role_privs.granted_role
           FROM user_role_privs
          WHERE user_role_privs.granted_role = 'RIF_MANAGER'::text)) OR s.grantee_username IS NOT NULL AND s.grantee_username::text <> ''::text
  ORDER BY c.username;

ALTER TABLE rif40.rif40_study_areas
    OWNER TO rif40;
COMMENT ON VIEW rif40.rif40_study_areas
    IS 'Links study areas and bands for a given study.';
COMMENT ON COLUMN rif40.rif40_study_areas.username
    IS 'Username';
COMMENT ON COLUMN rif40.rif40_study_areas.study_id
    IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40.rif40_study_areas.area_id
    IS 'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE';
COMMENT ON COLUMN rif40.rif40_study_areas.band_id
    IS 'A band allocated to the area';
COMMENT ON COLUMN rif40_study_areas.intersect_count 
	IS 'Number of intersects with shapes';
COMMENT ON COLUMN rif40_study_areas.distance_from_nearest_source 
	IS 'Distance from nearest source (Km)';
COMMENT ON COLUMN rif40_study_areas.nearest_rifshapepolyid 
	IS 'Nearest rifshapepolyid (shape reference)';
COMMENT ON COLUMN rif40_study_areas.exposure_value 
	IS 'Exposure value (when bands selected by exposure values)';
	
GRANT ALL ON TABLE rif40.rif40_study_areas TO rif40;
GRANT INSERT, SELECT, UPDATE, DELETE ON TABLE rif40.rif40_study_areas TO rif_user;
GRANT INSERT, SELECT, UPDATE, DELETE ON TABLE rif40.rif40_study_areas TO rif_manager;

--
-- Rebuild view trigger
--
CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_rif40_study_areas()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF 
AS $BODY$

BEGIN
	IF TG_OP = 'INSERT' THEN
--
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
		IF (USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) AND rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
			INSERT INTO t_rif40_study_areas (
				username,
				study_id,
				area_id,
				band_id,
				intersect_count,
				distance_from_nearest_source,
				nearest_rifshapepolyid,
				exposure_value
				)
			VALUES(
				coalesce(NEW.username, "current_user"()),
				coalesce(NEW.study_id, (currval('rif40_study_id_seq'::regclass))::integer),
				NEW.area_id /* no default value */,
				NEW.band_id /* no default value */,
				NEW.intersect_count /* no default value */,
				NEW.distance_from_nearest_source /* no default value */,
				NEW.nearest_rifshapepolyid /* no default value */,
				NEW.exposure_value /* no default value */);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_study_areas',
				'Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL', USER::VARCHAR, NEW.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
		IF USER = OLD.username AND NEW.username = OLD.username THEN
			UPDATE t_rif40_study_areas
			   SET username=NEW.username,
			       study_id=NEW.study_id,
			       area_id=NEW.area_id,
			       band_id=NEW.band_id,
				   intersect_count=NEW.intersect_count,
				   distance_from_nearest_source=NEW.distance_from_nearest_source,
				   nearest_rifshapepolyid=NEW.nearest_rifshapepolyid,
				   exposure_value=NEW.exposure_value
			 WHERE study_id=OLD.study_id
			   AND area_id=OLD.area_id;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_study_areas',
				'Cannot UPDATE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'DELETE' THEN
--
-- Check USER = OLD.username; if OK DELETE
--
		IF USER = OLD.username THEN
			DELETE FROM t_rif40_study_areas
			 WHERE study_id=OLD.study_id
			   AND area_id=OLD.area_id;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_study_areas',
				'Cannot DELETE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NULL;
	END IF;
	RETURN NEW;
END;

$BODY$;

ALTER FUNCTION rif40_trg_pkg.trgf_rif40_study_areas()
    OWNER TO rif40;

COMMENT ON FUNCTION rif40_trg_pkg.trgf_rif40_study_areas()
    IS 'INSTEAD OF trigger for view T_RIF40_STUDY_AREAS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
 [NO TABLE/VIEW comments available]';
 
DROP TRIGGER IF EXISTS trg_rif40_study_areas ON rif40.rif40_study_areas;
CREATE TRIGGER trg_rif40_study_areas
    INSTEAD OF INSERT OR DELETE OR UPDATE 
    ON rif40.rif40_study_areas
    FOR EACH ROW
    EXECUTE PROCEDURE rif40_trg_pkg.trgf_rif40_study_areas();

COMMENT ON TRIGGER trg_rif40_study_areas ON rif40.rif40_study_areas
    IS 'INSTEAD OF trigger for view T_RIF40_STUDY_AREAS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
 [NO TABLE/VIEW comments available]';
 
--
-- Rebuild View: rif40_studies
--
CREATE OR REPLACE VIEW rif40_studies AS 
 SELECT c.username,
    c.study_id,
    c.extract_table,
    c.study_name,
    c.summary,
    c.description,
    c.other_notes,
    c.study_date,
    c.geography,
    c.study_type,
    c.study_state,
    c.comparison_geolevel_name,
    c.denom_tab,
    c.direct_stand_tab,
    i.year_start,
    i.year_stop,
    i.max_age_group,
    i.min_age_group,
    c.study_geolevel_name,
    c.map_table,
    c.suppression_value,
    c.extract_permitted,
    c.transfer_permitted,
    c.authorised_by,
    c.authorised_on,
    c.authorised_notes,
    c.audsid,
        CASE
            WHEN g.partition = 1 AND (i.year_stop - i.year_start)::numeric >= (2::numeric * p.parallelisation) THEN p.parallelisation
            ELSE 0::numeric
        END AS partition_parallelisation,
    l.covariate_table,
    c.project,
    pj.description AS project_description,
    c.stats_method,
	c.select_state,
	c.print_state,
	c.export_date,
	c.risk_analysis_exposure_field
   FROM t_rif40_studies c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username::name = "current_user"()
     LEFT JOIN ( SELECT i2.study_id,
            max(i2.year_stop) AS year_stop,
            min(i2.year_start) AS year_start,
            max(i2.max_age_group) AS max_age_group,
            min(i2.min_age_group) AS min_age_group
           FROM t_rif40_investigations i2
          GROUP BY i2.study_id) i ON c.study_id = i.study_id
     LEFT JOIN rif40_geographies g ON c.geography::text = g.geography::text
     LEFT JOIN ( SELECT to_number(t_rif40_parameters.param_value::text, '999990'::text) AS parallelisation
           FROM t_rif40_parameters
          WHERE t_rif40_parameters.param_name::text = 'Parallelisation'::text) p ON 1 = 1
     LEFT JOIN t_rif40_geolevels l ON c.geography::text = l.geography::text AND c.study_geolevel_name::text = l.geolevel_name::text
     LEFT JOIN t_rif40_projects pj ON pj.project::text = c.project::text
  WHERE c.username::name = "current_user"() OR 'RIF_MANAGER'::text = (( SELECT user_role_privs.granted_role
           FROM user_role_privs
          WHERE user_role_privs.granted_role = 'RIF_MANAGER'::text)) OR s.grantee_username IS NOT NULL AND s.grantee_username::text <> ''::text
  ORDER BY c.username;

ALTER TABLE rif40_studies
  OWNER TO rif40;
GRANT ALL ON TABLE rif40_studies TO rif40;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_studies TO rif_user;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_studies TO rif_manager;
COMMENT ON VIEW rif40_studies
  IS 'RIF studies';
COMMENT ON COLUMN rif40_studies.username IS 'Username';
COMMENT ON COLUMN rif40_studies.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_studies.extract_table IS 'Extract table. Must only contain A-Z0-9_ and start with a letter.';
COMMENT ON COLUMN rif40_studies.study_name IS 'Study name';
COMMENT ON COLUMN rif40_studies.summary IS 'Study summary';
COMMENT ON COLUMN rif40_studies.description IS 'Study description';
COMMENT ON COLUMN rif40_studies.other_notes IS 'Study other notes';
COMMENT ON COLUMN rif40_studies.study_date IS 'Study date';
COMMENT ON COLUMN rif40_studies.geography IS 'Geography (e.g EW2001)';
COMMENT ON COLUMN rif40_studies.study_type IS 'Study type: 1 - disease mapping, 11 - Risk Analysis (many areas, one band), 12 - Risk Analysis (point sources), 13 - Risk Analysis (exposure covariates), 14 - Risk Analysis (coverage shapefile), 15 - Risk Analysis (exposure shapefile)';
COMMENT ON COLUMN rif40_studies.study_state IS 'Study state - C: created, not verfied; V: verified, but no other work done; E - extracted imported or created, but no results or maps created; R: results computed; U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.';
COMMENT ON COLUMN rif40_studies.comparison_geolevel_name IS 'Comparison area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1';
COMMENT ON COLUMN rif40_studies.denom_tab IS 'Denominator table name. May be &quot;DUMMY&quot; if extract created outside of the RIF. Note the old RIF allowed studies to have different denominators between investigations; this capability has been removed to simplify the extract SQL and to allow a single rotated high performance extract table to be used for all investigations in a study. The extract table is then based on the standard rotated denominator (i.e. in age_sex_group, total format rather than M0 .. M5-9 ... etc) with one extract column per covariate and investigation. Multiple investigations may use a different numerator (1 per investigation).';
COMMENT ON COLUMN rif40_studies.direct_stand_tab IS 'Name of table to be used in direct standardisation. COMPARISON_GEOLEVEL_NAME must be NULL. May be &quot;DUMMY&quot; if extract created outside of the RIF. Note the old RIF allowed studies to have different denominators between investigations; this capability has been removed to simplify the extract SQL and to allow a single rotated high performance extract table to be used for all investigations in a study. The extract table is then based on the standard rotated denominator (i.e. in age_sex_group, total format rather than M0 ... M5-9 ... etc) with one extract column per covariate and investigation. Multiple investigations may use a different numerator (1 per investigation).';
COMMENT ON COLUMN rif40_studies.year_start IS 'Minimum start year for investigations';
COMMENT ON COLUMN rif40_studies.year_stop IS 'Maximum stop year for investigations';
COMMENT ON COLUMN rif40_studies.max_age_group IS 'Minimum age group for investigations';
COMMENT ON COLUMN rif40_studies.min_age_group IS 'Maximum age group for investigations';
COMMENT ON COLUMN rif40_studies.study_geolevel_name IS 'Study area geolevel name';
COMMENT ON COLUMN rif40_studies.map_table IS 'Map table. Must only contain A-Z0-9_ and start with a letter.';
COMMENT ON COLUMN rif40_studies.suppression_value IS 'Suppress results with low cell counts below this value. If the role RIF_NO_SUPRESSION is granted and the user is not a RIF_STUDENT then SUPPRESSION_VALUE=0; otherwise is equals the parameter &quot;SuppressionValue&quot;. If >0 all results with the value or below will be set to 0.';
COMMENT ON COLUMN rif40_studies.extract_permitted IS 'Is extract permitted from the database: 0/1. Only a RIF MANAGER may change this value. This user is still permitted to create and run a RIF study and to view the results. Geolevel access is rectricted by the RIF40_GEOLEVELS.RESTRICTED Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. All students must be granted permission by a RIF_MANAGER for any extract if the system parameter ExtractControl=1. This is enforced by the RIF application.';
COMMENT ON COLUMN rif40_studies.transfer_permitted IS 'Is transfer permitted from the Secure or Private Network: 0/1. This is for purely documentatary purposes only. Only a RIF MANAGER may change this value. The value defaults to the same as EXTRACT_PERMITTED. Only geolevels where RIF40_GEOLEVELS.RESTRICTED=0 may be transferred.';
COMMENT ON COLUMN rif40_studies.authorised_by IS 'Who authorised extract and/or transfer. Must be a RIF MANAGER.';
COMMENT ON COLUMN rif40_studies.authorised_on IS 'When was the extract and/or transfer authorised';
COMMENT ON COLUMN rif40_studies.authorised_notes IS 'IG authorisation notes. Must be filled in if EXTRACT_PERMITTED=1.';
COMMENT ON COLUMN rif40_studies.audsid IS 'Link to Oracle audit subsystem. On Postgres is &quot;backend PID.Julian day.Seconds from midnight.uSeconds (backend start time)&quot;. This can be correlated to the logging messages.';
COMMENT ON COLUMN rif40_studies.partition_parallelisation IS 'Degree of parallelisation used in the study extraction';
COMMENT ON COLUMN rif40_studies.covariate_table IS 'Covraite table name';
COMMENT ON COLUMN rif40_studies.project IS 'Project running the study. The user must be allocated to the project.';
COMMENT ON COLUMN rif40_studies.project_description IS 'Project description';
COMMENT ON COLUMN rif40_studies.stats_method IS 'RIF Bayesian statistical method: HET, BYM, CAR or NONE';
COMMENT ON COLUMN rif40_studies.select_state IS 'RIF Study selection state: what the user selected (see: rifs-dsub-selectstate.js):

{
	studyType: "risk_analysis_study",
	studySelection: {			
		studySelectAt: undefined,
		studySelectedAreas: [],
		riskAnalysisType: 12, 
		riskAnalysisDescription: "Risk Analysis (point sources, many areas, one to six bands)",
		studyShapes: [],
		comparisonShapes: [],
		comparisonSelectAt: undefined,
		comparisonSelectedAreas: [],
		fileList: [],
		bandAttr: []
	},
	showHideCentroids: false,
	showHideSelectionShapes: true
};
					
//
// Risk analysis study types (as per rif40_studies.stype_type): 
//
// 11 - Risk Analysis (many areas, one band), 
// 12 - Risk Analysis (point sources, many areas, one to six bands) [DEFAULT], 
// 13 - Risk Analysis (exposure covariates), 
// 14 - Risk Analysis (coverage shapefile), 
// 15 - Risk Analysis (exposure shapefile)

{
	studyType: "disease_mapping_study",
	studySelection: {			
		studySelectAt: undefined,
		studySelectedAreas: [],
		studyShapes: [],
		comparisonSelectAt: undefined,
		comparisonSelectedAreas: [],
		fileList: [],
		bandAttr: []
	},
	showHideCentroids: false,
	showHideSelectionShapes: true
};';
COMMENT ON COLUMN rif40_studies.print_state IS 'RIF Study print state: what the user selected (see: rifs-util-printstate.js)';
COMMENT ON COLUMN rif40_studies.export_date IS 'RIF Study export date';
COMMENT ON COLUMN rif40_studies.risk_analysis_exposure_field IS 'Risk analysis exposure field (when bands selected by exposure values)';
	
-- Function: rif40_trg_pkg.trgf_rif40_studies()

-- DROP FUNCTION rif40_trg_pkg.trgf_rif40_studies();

CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_rif40_studies()
  RETURNS trigger AS
$BODY$
BEGIN
	IF TG_OP = 'INSERT' THEN
--
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
		IF (USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) AND rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
			INSERT INTO t_rif40_studies (
				username,
				study_id,
				extract_table,
				study_name,
				summary,
				description,
				other_notes,
				study_date,
				geography,
				study_type,
				study_state,
				comparison_geolevel_name,
				denom_tab,
				direct_stand_tab,
				study_geolevel_name,
				map_table,
				suppression_value,
				extract_permitted,
				transfer_permitted,
				authorised_by,
				authorised_on,
				authorised_notes,
				audsid,
				project,
				stats_method,
				select_state,
				print_state,
				export_date,
				risk_analysis_exposure_field)
			VALUES(
				coalesce(NEW.username, "current_user"()),
				coalesce(NEW.study_id, (nextval('rif40_study_id_seq'::regclass))::integer),
				NEW.extract_table /* no default value */,
				NEW.study_name /* no default value */,
				NEW.summary /* no default value */,
				NEW.description /* no default value */,
				NEW.other_notes /* no default value */,
				coalesce(NEW.study_date, ('now'::text)::timestamp without time zone),
				NEW.geography /* no default value */,
				NEW.study_type /* no default value */,
				coalesce(NEW.study_state, 'C'::character varying),
				NEW.comparison_geolevel_name /* no default value */,
				NEW.denom_tab /* no default value */,
				NEW.direct_stand_tab /* no default value */,
				NEW.study_geolevel_name /* no default value */,
				NEW.map_table /* no default value */,
				NEW.suppression_value /* no default value */,
				coalesce(NEW.extract_permitted, 0),
				coalesce(NEW.transfer_permitted, 0),
				NEW.authorised_by /* no default value */,
				NEW.authorised_on /* no default value */,
				NEW.authorised_notes /* no default value */,
				coalesce(NEW.audsid, sys_context('USERENV'::character varying, 'SESSIONID'::character varying)),
				NEW.project /* no default value */,
				COALESCE(NEW.stats_method, 'NONE'),
				NEW.select_state,
				NEW.print_state,
				NEW.export_date,
				NEW.risk_analysis_exposure_field);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_studies',
				'Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL', USER::VARCHAR, NEW.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
		IF USER = OLD.username AND NEW.username = OLD.username THEN
			UPDATE t_rif40_studies
			   SET username=NEW.username,
			       study_id=NEW.study_id,
			       extract_table=NEW.extract_table,
			       study_name=NEW.study_name,
			       summary=NEW.summary,
			       description=NEW.description,
			       other_notes=NEW.other_notes,
			       study_date=NEW.study_date,
			       geography=NEW.geography,
			       study_type=NEW.study_type,
			       study_state=NEW.study_state,
			       comparison_geolevel_name=NEW.comparison_geolevel_name,
			       denom_tab=NEW.denom_tab,
			       direct_stand_tab=NEW.direct_stand_tab,
			       study_geolevel_name=NEW.study_geolevel_name,
			       map_table=NEW.map_table,
			       suppression_value=NEW.suppression_value,
			       extract_permitted=NEW.extract_permitted,
			       transfer_permitted=NEW.transfer_permitted,
			       authorised_by=NEW.authorised_by,
			       authorised_on=NEW.authorised_on,
			       authorised_notes=NEW.authorised_notes,
			       audsid=NEW.audsid,
			       project=NEW.project,
				   stats_method=NEW.stats_method,
				   select_state=NEW.select_state,
				   print_state=NEW.print_state,
				   export_date=NEW.export_date,
				   risk_analysis_exposure_field=NEW.risk_analysis_exposure_field
			 WHERE study_id=OLD.study_id;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_studies',
				'Cannot UPDATE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'DELETE' THEN
--
-- Check USER = OLD.username; if OK DELETE
--
		IF USER = OLD.username THEN
			DELETE FROM t_rif40_studies
			 WHERE study_id=OLD.study_id;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_studies',
				'Cannot DELETE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NULL;
	END IF;
	RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION rif40_trg_pkg.trgf_rif40_studies()
  OWNER TO rif40;
COMMENT ON FUNCTION rif40_trg_pkg.trgf_rif40_studies() IS 'INSTEAD OF trigger for view T_RIF40_STUDIES to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
 [NO TABLE/VIEW comments available]';


-- Trigger: trg_rif40_studies on rif40_studies

DROP TRIGGER IF EXISTS trg_rif40_studies ON rif40_studies;

CREATE TRIGGER trg_rif40_studies
  INSTEAD OF INSERT OR UPDATE OR DELETE
  ON rif40_studies
  FOR EACH ROW
  EXECUTE PROCEDURE rif40_trg_pkg.trgf_rif40_studies();
COMMENT ON TRIGGER trg_rif40_studies ON rif40_studies IS 'INSTEAD OF trigger for view T_RIF40_STUDIES to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
 [NO TABLE/VIEW comments available]';
  
--
-- 4. Add intersection counting and exposure value support to extracts
--
\i ../PLpgsql/rif40_sql_pkg/rif40_startup.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_create_extract.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_create_insert_statement.sql

--
-- 5. View rif40_exposure_values
--
CREATE VIEW rif40.rif40_exposure_values AS
 SELECT username,
    study_id,
	band_id,
    COUNT(area_id) AS total_areas,
	COUNT(DISTINCT(nearest_rifshapepolyid)) AS total_rifshapepolyid,
	MIN(intersect_count) AS min_intersect_count,
	MAX(intersect_count) AS max_intersect_count,
	MIN(distance_from_nearest_source) AS min_distance_from_nearest_source,
	MAX(distance_from_nearest_source) AS max_distance_from_nearest_source,
	MIN(exposure_value) AS min_exposure_value,
	MAX(exposure_value) AS max_exposure_value
   FROM rif40.rif40_study_areas
  WHERE exposure_value IS NOT NULL
  GROUP BY username, study_id, band_id
  ORDER BY username, study_id, band_id;
GRANT SELECT ON rif40.rif40_exposure_values TO rif_user, rif_manager;
COMMENT ON VIEW rif40.rif40_exposure_values 
	IS 'Minimum/maximum exposure values by band for risk analysis study areas. Study type: 13 (exposure covariates)';
COMMENT ON COLUMN rif40.rif40_exposure_values.username
    IS 'Username';
COMMENT ON COLUMN rif40.rif40_exposure_values.study_id
    IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40.rif40_exposure_values.band_id
    IS 'A band allocated to the area';
COMMENT ON COLUMN rif40.rif40_exposure_values.total_areas
    IS 'Total area id';
COMMENT ON COLUMN rif40.rif40_exposure_values.total_rifshapepolyid
    IS 'Total rifshapepolyid (shape reference)';
COMMENT ON COLUMN rif40.rif40_exposure_values.min_distance_from_nearest_source 
	IS 'Minimum distance from nearest source (Km)';
COMMENT ON COLUMN rif40.rif40_exposure_values.max_distance_from_nearest_source 
	IS 'Maximum distance from nearest source (Km)';
COMMENT ON COLUMN rif40.rif40_exposure_values.min_intersect_count 
	IS 'Minimum number of intersects with shapes';
COMMENT ON COLUMN rif40.rif40_exposure_values.max_intersect_count 
	IS 'Maximum number of intersects with shapes';
COMMENT ON COLUMN rif40.rif40_exposure_values.min_exposure_value 
	IS 'Minimum exposure value';
COMMENT ON COLUMN rif40.rif40_exposure_values.max_exposure_value 
	IS 'Maximum exposure value';
\dS+ rif40.rif40_exposure_values
	
--
-- Testing stop
--
/*
DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'Stop processing';
END;
$$;
 */
 
END;
--
--  Eof 
