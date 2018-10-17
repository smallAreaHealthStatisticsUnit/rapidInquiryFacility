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
-- Rapid Inquiry Facility (RIF) - RIF alter script 10 - Risk Analysis Enhancements
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
-- psql -U rif40 -d sahsuland -w -e -P pager=off -f alter_scripts/v4_0_alter_10.sql
--
-- The middleware must be down for this to run
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #10 Risk Analysis Enhancements.

/*

* Alter 10: Risk Analysis Enhancements

 1. Save/restore user selection methods to/from database (rif40_studies.select_state);	
 2. Save user print selection to/from database (rif40_studies.print_state), export_date;	
 3. The column predefined_group_name in the table t_rif40_inv_conditions is defined as varchar(5) in Postgres. It should be varchar(30);
    [Issue 21](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/21)
 4. Add table t_rif40_homogeneity, view rif40_homogeneity:
  
  | Column name      | Column description                                                                  |
  |------------------|-------------------------------------------------------------------------------------| 
  | study_id[PK][FK] | rif40_studies.study_id                                                              | 
  | inv_id[PK][FK]   | rif40_investigations.inv_id                                                         | 
  | adjusted[PK]     | 0 or 1 indicating adjusted/unadjusted results                                       | 
  | genders[PK]      | 1, 2 or 3, indicating Males, Females or Both                                        | 
  | homogeneity_dof  | the number of degrees of freedom                                                    | 
  | homogeneity_chi2 | the chi2-value for the homogeneity test                                             | 
  | homogeneity_p    | the p-value for the homogeneity test                                                | 
  | linearity_chi2   | the chi2-value for the linearity test                                               | 
  | linearity_p      | the p-value for the linearity test                                                  | 
  | explt5           | the number of bands in the study which have an expected number of cases less than 1 | 
  
 5. Add unique keys to description files on rif tables/projects/health themes to protect against the middleware using them as a key;
 6. Add default background layer support for geography (so sahsuland has no background);
 
 */
BEGIN;

DO LANGUAGE plpgsql $$
BEGIN
--
-- 1. Save/restore user selection methods to/from database (rif40_studies.select_state);	
--
	ALTER TABLE t_rif40_studies ADD COLUMN select_state JSON NULL;
	COMMENT ON COLUMN t_rif40_studies.select_state IS 'RIF Study selection state: what the user selected (see: rifs-dsub-selectstate.js):

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

--
-- 2. Save user print selection to/from database (rif40_studies.print_state), export date;	
--
	ALTER TABLE t_rif40_studies ADD COLUMN print_state JSON NULL;
	ALTER TABLE t_rif40_studies ADD COLUMN export_date timestamp without time zone NULL; -- Export date;
	COMMENT ON COLUMN t_rif40_studies.print_state IS 'RIF Study print state: what the user selected (see: rifs-util-printstate.js)';
	COMMENT ON COLUMN t_rif40_studies.export_date IS 'RIF Study export date';
EXCEPTION
	WHEN duplicate_column THEN
		RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;  
END;
$$;
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
	c.export_date
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
				export_date)
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
				NEW.export_date);
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
				   export_date=NEW.export_date
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
-- 3. The column predefined_group_name in the table t_rif40_inv_conditions is defined as varchar(5) in Postgres. It should be varchar(30);
--    [Issue 21](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/21)
--
-- Function: rif40_trg_pkg.trgf_rif40_inv_conditions()

DROP VIEW rif40_inv_conditions;
DROP FUNCTION rif40_trg_pkg.trgf_rif40_inv_conditions();
		 
ALTER TABLE t_rif40_inv_conditions ALTER COLUMN predefined_group_name SET DATA TYPE VARCHAR(30);

-- View: rif40_inv_conditions

-- DROP VIEW rif40_inv_conditions;

CREATE OR REPLACE VIEW rif40_inv_conditions AS 
WITH a AS (
         SELECT c.username,
            c.study_id,
            c.inv_id,
            c.line_number,
            c.min_condition,
            c.max_condition,
            c.predefined_group_name,
            c.outcome_group_name,
            i.numer_tab,
            b.field_name,
                CASE
                    WHEN c.predefined_group_name IS NOT NULL THEN ((ROW(g.condition, '%', quote_ident(lower(b.field_name::text))) || ' /* Pre defined group: '::text) || g.predefined_group_description::text) || ' */'::text
                    WHEN c.max_condition IS NOT NULL THEN ((((quote_ident(lower(b.field_name::text)) || ' BETWEEN '''::text) || c.min_condition::text) || ''' AND '''::text) || c.max_condition::text) || '~'' /* Range filter */'::text
                    ELSE ((quote_ident(lower(b.field_name::text)) || ' LIKE '''::text) || c.min_condition::text) || '%'' /* Value filter */'::text
                END AS condition
           FROM t_rif40_investigations i,
            rif40_outcome_groups b,
            t_rif40_inv_conditions c
             LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username::name = "current_user"()
             LEFT JOIN rif40_predefined_groups g ON c.predefined_group_name::text = g.predefined_group_name::text
          WHERE c.inv_id = i.inv_id AND c.study_id = i.study_id AND c.outcome_group_name::text = b.outcome_group_name::text AND ("current_user"() = 'rif40'::name OR c.username::name = "current_user"() OR 'RIF_MANAGER'::text = (( SELECT user_role_privs.granted_role
                   FROM user_role_privs
                  WHERE user_role_privs.granted_role = 'RIF_MANAGER'::text)) OR s.grantee_username IS NOT NULL AND s.grantee_username::text <> ''::text)
        )
 SELECT a.username,
    a.study_id,
    a.inv_id,
    a.line_number,
    a.min_condition,
    a.max_condition,
    a.predefined_group_name,
    a.outcome_group_name,
    a.numer_tab,
    a.field_name,
    a.condition,
        CASE
            WHEN d.attrelid IS NOT NULL THEN true
            ELSE false
        END AS columnn_exists,
    col_description(lower(a.numer_tab::text)::regclass::oid, d.attnum::integer) AS column_comment
   FROM a
     LEFT JOIN pg_attribute d ON lower(a.numer_tab::text)::regclass::oid = d.attrelid AND d.attname::text = lower(a.field_name::text)
  ORDER BY a.username, a.inv_id;

ALTER TABLE rif40_inv_conditions
  OWNER TO rif40;
GRANT ALL ON TABLE rif40_inv_conditions TO rif40;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_inv_conditions TO rif_user;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_inv_conditions TO rif_manager;
COMMENT ON VIEW rif40_inv_conditions
  IS 'Lines of SQL conditions pertinent to an investigation.';
COMMENT ON COLUMN rif40_inv_conditions.username IS 'Username';
COMMENT ON COLUMN rif40_inv_conditions.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_inv_conditions.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN rif40_inv_conditions.line_number IS 'Line number';
COMMENT ON COLUMN rif40_inv_conditions.min_condition IS 'Minimum condition; if max condition is not null SQL WHERE Clause evaluates to: &quot;WHERE &lt;field_name&gt; LIKE ''&lt;min_condition&gt;%''&quot;.';
COMMENT ON COLUMN rif40_inv_conditions.max_condition IS 'Maximum condition; if max condition is not null SQL WHERE Clause evaluates to: &quot;WHERE &lt;field_name&gt; BETWEEN ''&lt;min_condition&gt;'' AND ''&lt;max_condition&gt;~''&quot;.';
COMMENT ON COLUMN rif40_inv_conditions.predefined_group_name IS 'Predefined Group Name. E.g LUNG_CANCER';
COMMENT ON COLUMN rif40_inv_conditions.outcome_group_name IS 'Outcome Group Name. E.g SINGLE_VARIABLE_ICD';
COMMENT ON COLUMN rif40_inv_conditions.numer_tab IS 'Numerator table';
COMMENT ON COLUMN rif40_inv_conditions.field_name IS 'Numerator table outcome field name, e.g. ICD_SAHSU_01, ICD_SAHSU';
COMMENT ON COLUMN rif40_inv_conditions.condition IS 'Condition SQL fragment';
COMMENT ON COLUMN rif40_inv_conditions.columnn_exists IS 'Numerator table outcome columnn exists';
COMMENT ON COLUMN rif40_inv_conditions.column_comment IS 'Numerator table outcome column comment';

CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_rif40_inv_conditions()
  RETURNS trigger AS
$BODY$
BEGIN
	IF TG_OP = 'INSERT' THEN
--
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
		IF ((USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) AND 
		    (rif40_sql_pkg.is_rif40_user_manager_or_schema())) OR 
			(USER = 'rif40' AND NEW.study_id = 1 AND NEW.inv_id = 1) /* Allow alter_7 */ THEN
			INSERT INTO t_rif40_inv_conditions (
				username,
				study_id,
				inv_id,
				line_number,
				outcome_group_name, 
				min_condition, 
				max_condition, 
				predefined_group_name)
			VALUES(
				coalesce(NEW.username, "current_user"()),
				coalesce(NEW.study_id, (currval('rif40_study_id_seq'::regclass))::integer),
				coalesce(NEW.inv_id, (currval('rif40_inv_id_seq'::regclass))::integer),
				coalesce(NEW.line_number, 1),
				NEW.outcome_group_name, 
				NEW.min_condition, 
				NEW.max_condition, 
				NEW.predefined_group_name);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_inv_conditions',
				'Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL', USER::VARCHAR, NEW.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
		IF USER = OLD.username AND NEW.username = OLD.username THEN
			UPDATE t_rif40_inv_conditions
			   SET username=NEW.username,
			       study_id=NEW.study_id,
			       inv_id=NEW.inv_id,
			       line_number=NEW.line_number,
				   outcome_group_name=NEW.outcome_group_name,
			       min_condition=NEW.min_condition, 
				   max_condition=NEW.max_condition, 
				   predefined_group_name=NEW.predefined_group_name
			 WHERE study_id=OLD.study_id
			   AND inv_id=OLD.inv_id
			   AND line_number=OLD.line_number;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_inv_conditions',
				'Cannot UPDATE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'DELETE' THEN
--
-- Check USER = OLD.username; if OK DELETE
--
		IF USER = OLD.username THEN
			DELETE FROM t_rif40_inv_conditions
			 WHERE study_id=OLD.study_id
			   AND inv_id=OLD.inv_id
			   AND line_number=OLD.line_number;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_inv_conditions',
				'Cannot DELETE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
			RETURN OLD;
		END IF;
		RETURN NULL;
	END IF;
	RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION rif40_trg_pkg.trgf_rif40_inv_conditions()
  OWNER TO rif40;
COMMENT ON FUNCTION rif40_trg_pkg.trgf_rif40_inv_conditions() IS 'INSTEAD OF trigger for view T_RIF40_INV_CONDITIONS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
		 [NO TABLE/VIEW comments available]';

-- Trigger: trg_rif40_inv_conditions on rif40_inv_conditions

-- DROP TRIGGER trg_rif40_inv_conditions ON rif40_inv_conditions;

CREATE TRIGGER trg_rif40_inv_conditions
  INSTEAD OF INSERT OR UPDATE OR DELETE
  ON rif40_inv_conditions
  FOR EACH ROW
  EXECUTE PROCEDURE rif40_trg_pkg.trgf_rif40_inv_conditions();
COMMENT ON TRIGGER trg_rif40_inv_conditions ON rif40_inv_conditions IS 'INSTEAD OF trigger for view T_RIF40_INV_CONDITIONS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
		 [Lines of SQL conditions pertinent to an investigation.]';
		 
--
-- 4. Add table t_rif40_homogeneity, view rif40_homogeneity:
--  
--  | Column name      | Column description                                                                  |
--  |------------------|-------------------------------------------------------------------------------------| 
--  | study_id[PK][FK] | rif40_studies.study_id                                                              | 
--  | inv_id[PK][FK]   | rif40_investigations.inv_id                                                         | 
--  | adjusted[PK]     | 0 or 1 indicating adjusted/unadjusted results                                       | 
--  | genders[PK]      | 1, 2 or 3, indicating Males, Females or Both                                        | 
--  | homogeneity_dof  | the number of degrees of freedom                                                    | 
--  | homogeneity_chi2 | the chi2-value for the homogeneity test                                             | 
--  | homogeneity_p    | the p-value for the homogeneity test                                                | 
--  | linearity_chi2   | the chi2-value for the linearity test                                               | 
--  | linearity_p      | the p-value for the linearity test                                                  | 
--  | explt5           | the number of bands in the study which have an expected number of cases less than 1 | 
--
DO LANGUAGE plpgsql $$
BEGIN
	IF to_regclass('rif40.t_rif40_homogeneity') IS NULL THEN
		CREATE TABLE t_rif40_homogeneity
		(
		  inv_id integer NOT NULL, 
		  study_id integer NOT NULL, 
		  username character varying(90) DEFAULT "current_user"() NOT NULL, 	-- Username
		  adjusted smallint NOT NULL,								-- 0 or 1 indicating adjusted/unadjusted results                                    
		  genders smallint NOT NULL,								-- 1, 2 or 3, indicating Males, Females or Both                                       
		  homogeneity_dof  double precision, 						-- the number of degrees of freedom                                                    
		  homogeneity_chi2 double precision,						-- the chi2-value for the homogeneity test                                            
		  homogeneity_p double precision,   						-- the p-value for the homogeneity test                                                
		  linearity_chi2 double precision,  						-- the chi2-value for the linearity test                                               
		  linearity_p double precision,     						-- the p-value for the linearity test                                                  
		  explt5  double precision,      			    			-- the number of bands in the study which have an expected number of cases less than 1 
		  CONSTRAINT t_rif40_homogeneity_pk PRIMARY KEY (study_id, inv_id, adjusted, genders),
		  CONSTRAINT t_rif40_homogeneity_si_fk FOREIGN KEY (study_id, inv_id)
			  REFERENCES t_rif40_investigations (study_id, inv_id) MATCH SIMPLE
			  ON UPDATE NO ACTION ON DELETE NO ACTION,
		  CONSTRAINT adjusted_ck CHECK (adjusted BETWEEN 0 AND 1),
		  CONSTRAINT genders_ck CHECK (genders BETWEEN 1 AND 3)
		);
--		  
		ALTER TABLE t_rif40_homogeneity
		  OWNER TO rif40;
		GRANT ALL ON TABLE t_rif40_homogeneity TO rif40;
		GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE t_rif40_homogeneity TO rif_user;
		GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE t_rif40_homogeneity TO rif_manager;
		COMMENT ON TABLE t_rif40_homogeneity
		  IS 'Riak analysis homogeneity tests.';
		COMMENT ON COLUMN t_rif40_homogeneity.inv_id IS 'Unique investigation index: inv_id';
		COMMENT ON COLUMN t_rif40_homogeneity.study_id IS 'Unique study index: study_id';
		COMMENT ON COLUMN t_rif40_homogeneity.username IS 'Username';
		COMMENT ON COLUMN t_rif40_homogeneity.adjusted IS '0 or 1 indicating adjusted/unadjusted results';
		COMMENT ON COLUMN t_rif40_homogeneity.genders IS '1, 2 or 3, indicating Males, Females or Both';
		COMMENT ON COLUMN t_rif40_homogeneity.homogeneity_dof IS 'the number of degrees of freedom';
		COMMENT ON COLUMN t_rif40_homogeneity.homogeneity_chi2 IS 'the chi2-value for the homogeneity test';
		COMMENT ON COLUMN t_rif40_homogeneity.homogeneity_p IS 'the chi2-value for the homogeneity test';
		COMMENT ON COLUMN t_rif40_homogeneity.linearity_chi2 IS 'the number of bands in the study which have an expected number of cases less than 1';
		COMMENT ON COLUMN t_rif40_homogeneity.linearity_p IS 'the chi2-value for the linearity test';
		COMMENT ON COLUMN t_rif40_homogeneity.explt5 IS 'the chi2-value for the linearity test';
	END IF;
END;
$$;

CREATE OR REPLACE VIEW rif40_homogeneity AS 
 SELECT c.username,
    c.study_id,
	c.inv_id, 
	c.adjusted,								-- 0 or 1 indicating adjusted/unadjusted results                                    
	c.genders,								-- 1, 2 or 3, indicating Males, Females or Both                                       
	c.homogeneity_dof, 						-- the number of degrees of freedom                                                    
	c.homogeneity_chi2,						-- the chi2-value for the homogeneity test                                            
	c.homogeneity_p,   						-- the p-value for the homogeneity test                                                
	c.linearity_chi2,  						-- the chi2-value for the linearity test                                               
	c.linearity_p,     						-- the p-value for the linearity test                                                  
	c.explt5 
   FROM t_rif40_homogeneity c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username::name = "current_user"()
  WHERE c.username::name = "current_user"() OR 'RIF_MANAGER'::text = (( SELECT user_role_privs.granted_role
           FROM user_role_privs
          WHERE user_role_privs.granted_role = 'RIF_MANAGER'::text)) OR s.grantee_username IS NOT NULL AND s.grantee_username::text <> ''::text
  ORDER BY c.username;
  
COMMENT ON VIEW rif40_homogeneity
  IS 'Riak analysis homogeneity tests.';
COMMENT ON COLUMN rif40_homogeneity.inv_id IS 'Unique investigation index: inv_id';
COMMENT ON COLUMN rif40_homogeneity.study_id IS 'Unique study index: study_id';
COMMENT ON COLUMN rif40_homogeneity.username IS 'Username';
COMMENT ON COLUMN rif40_homogeneity.adjusted IS '0 or 1 indicating adjusted/unadjusted results';
COMMENT ON COLUMN rif40_homogeneity.genders IS '1, 2 or 3, indicating Males, Females or Both';
COMMENT ON COLUMN rif40_homogeneity.homogeneity_dof IS 'the number of degrees of freedom';
COMMENT ON COLUMN rif40_homogeneity.homogeneity_chi2 IS 'the chi2-value for the homogeneity test';
COMMENT ON COLUMN rif40_homogeneity.homogeneity_p IS 'the chi2-value for the homogeneity test';
COMMENT ON COLUMN rif40_homogeneity.linearity_chi2 IS 'the number of bands in the study which have an expected number of cases less than 1';
COMMENT ON COLUMN rif40_homogeneity.linearity_p IS 'the chi2-value for the linearity test';
COMMENT ON COLUMN rif40_homogeneity.explt5 IS 'the chi2-value for the linearity test';		

--
-- 5. Add unique keys to description files on rif tables/projects/health themes to protect against the middleware using them as a key;
--
CREATE UNIQUE INDEX IF NOT EXISTS rif40_geographies_desc ON rif40_geographies(description);
CREATE UNIQUE INDEX IF NOT EXISTS rif40_health_study_themes_desc ON rif40_health_study_themes(description);
UPDATE rif40_outcome_groups 
   SET outcome_group_description = 'SAHSULAND Single ICD'
 WHERE outcome_group_name = 'SAHSULAND_ICD' AND outcome_group_description = 'Single ICD';
CREATE UNIQUE INDEX IF NOT EXISTS rif40_outcome_groups_desc ON rif40_outcome_groups(outcome_group_description);
CREATE UNIQUE INDEX IF NOT EXISTS rif40_outcomes_desc ON rif40_outcomes(outcome_description);
CREATE UNIQUE INDEX IF NOT EXISTS rif40_predefined_groups_desc ON rif40_predefined_groups(predefined_group_description);
CREATE UNIQUE INDEX IF NOT EXISTS rif40_tables_desc ON rif40_tables(description);
CREATE UNIQUE INDEX IF NOT EXISTS t_rif40_geolevels_desc ON t_rif40_geolevels(description);
CREATE UNIQUE INDEX IF NOT EXISTS t_rif40_parameters_desc ON t_rif40_parameters(param_description);
CREATE UNIQUE INDEX IF NOT EXISTS t_rif40_projects_desc ON t_rif40_projects(description);
 
--
-- 6. Add default background layer support for geography (so sahsuland has no background);
--
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE rif40_geographies ADD COLUMN map_background VARCHAR(200) DEFAULT 'OpenStreetMap Mapnik' NULL;
	ALTER TABLE rif40_geographies ADD CONSTRAINT map_background_ck CHECK (map_background IN (
		'OpenStreetMap Mapnik','OpenStreetMap BlackAndWhite','OpenTopoMap','Humanitarian OpenStreetMap','Thunderforest OpenCycleMap',
		'Thunderforest Transport','Thunderforest TransportDark','Thunderforest Landscape','Thunderforest SpinalMap','Thunderforest Outdoors',
		'Thunderforest Pioneer','Thunderforest Mobile Atlas','Thunderforest Neighbourhood','OpenMapSurfer Roads','OpenMapSurfer Grayscale',
		'Hydda Full','Hydda Base','Stamen Toner','Stamen TonerBackground','Stamen TonerLite','Stamen Watercolor','Esri WorldStreetMap',
		'Esri DeLorme','Esri WorldTopoMap','Esri WorldImagery','Esri WorldTerrain','Esri WorldShadedRelief','Esri WorldPhysical',
		'Esri OceanBasemap','Esri NatGeoWorldMap','Esri WorldGrayCanvas','CartoDB Positron','CartoDB PositronNoLabels',
		'CartoDB PositronOnlyLabels','CartoDB DarkMatter','CartoDB DarkMatterNoLabels','CartoDB DarkMatterOnlyLabels',
		'HikeBike HikeBike','HikeBike HillShading','NASAGIBS ViirsEarthAtNight2012','OSM UK Postcodes','Code-Point Open UK Postcodes'));
	COMMENT ON COLUMN rif40_geographies.map_background IS 'RIF geography map background';
EXCEPTION
	WHEN duplicate_column THEN
		RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;  
END;
$$;

UPDATE rif40_geographies SET map_background = 'OpenStreetMap Mapnik' WHERE geography != 'SAHSULAND' AND map_background IS NULL;
UPDATE rif40_geographies SET map_background = NULL WHERE geography = 'SAHSULAND' AND map_background IS NOT NULL;

\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_studies_checks.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_verify_state_change.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_create_extract.sql

SELECT geography, map_background
  FROM rif40_geographies
 ORDER BY 1;

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