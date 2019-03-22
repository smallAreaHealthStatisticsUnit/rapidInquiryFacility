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
-- Rapid Inquiry Facility (RIF) - RIF alter script 12 - More risk Analysis Enhancements, additional covariate support;
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
-- psql -U rif40 -d sahsuland -w -e -P pager=off -f alter_scripts/v4_0_alter_12.sql
--
-- The middleware must be down for this to run
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #12 More risk Analysis Enhancements, additional covariate support; ;

/*

* Alter 12: More risk Analysis Enhancements, additional covariate support;

 1. rif40_homogeneity view grants
 2. Additional covariate support

 */
BEGIN;

--
-- 1. rif40_homogeneity view grants
--
GRANT SELECT ON rif40_homogeneity TO rif_user, rif_manager;

--
-- 2. Additional covariate support
--
-- Table: rif40.t_rif40_inv_covariates: add covariate_type flag
--
DO LANGUAGE plpgsql $$
    BEGIN
        ALTER TABLE rif40.t_rif40_inv_covariates ADD COLUMN covariate_type character varying(1) NULL;
    EXCEPTION
    WHEN duplicate_column THEN
        RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;
    END;
$$;

DROP TRIGGER t_rif40_inv_covariates_checks ON rif40.t_rif40_inv_covariates;
UPDATE rif40.t_rif40_inv_covariates
   SET covariate_type = 'N'
 WHERE covariate_type IS NULL;
ALTER TABLE rif40.t_rif40_inv_covariates DROP CONSTRAINT IF EXISTS rif40_covariates_type_ck;
ALTER TABLE rif40.t_rif40_inv_covariates ADD CONSTRAINT rif40_covariates_type_ck CHECK (covariate_type = ANY (ARRAY['N', 'A']));
ALTER TABLE rif40.t_rif40_inv_covariates ALTER COLUMN covariate_type SET NOT NULL;
ALTER TABLE rif40.t_rif40_inv_covariates ALTER COLUMN covariate_type SET DEFAULT 'N';

COMMENT ON COLUMN rif40.t_rif40_inv_covariates.covariate_type
    IS 'Covariate type: N normal; A: additional (not used in the calculations)';

-- Trigger: t_rif40_inv_covariates_checks: NO CHANGES
CREATE TRIGGER t_rif40_inv_covariates_checks
    BEFORE INSERT OR DELETE OR UPDATE
    ON rif40.t_rif40_inv_covariates
    FOR EACH ROW
    EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_inv_covariates_checks();

-- View: rif40.rif40_inv_covariates (usually auto-generated)
DROP VIEW rif40.rif40_inv_covariates;
CREATE VIEW rif40.rif40_inv_covariates AS
 SELECT c.username,
    c.study_id,
    c.inv_id,
    c.covariate_name,
    c.covariate_type,
    c.min,
    c.max,
    c.geography,
    c.study_geolevel_name
   FROM t_rif40_inv_covariates c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username::name = "current_user"()
  WHERE c.username::name = "current_user"() OR 'RIF_MANAGER'::text = (( SELECT user_role_privs.granted_role
           FROM user_role_privs
          WHERE user_role_privs.granted_role = 'RIF_MANAGER'::text)) OR s.grantee_username IS NOT NULL AND s.grantee_username::text <> ''::text
  ORDER BY c.username;

ALTER TABLE rif40.rif40_inv_covariates
    OWNER TO rif40;
COMMENT ON VIEW rif40.rif40_inv_covariates
    IS 'Details of each covariate used by an investigation in a study';
COMMENT ON COLUMN rif40.rif40_inv_covariates.inv_id
    IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN rif40.rif40_inv_covariates.study_id
    IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40.rif40_inv_covariates.covariate_name
    IS 'Covariate name';
COMMENT ON COLUMN rif40.rif40_inv_covariates.username
    IS 'Username';
COMMENT ON COLUMN rif40.rif40_inv_covariates.geography
    IS 'Geography (e.g EW2001). Cannot be changed by the user; present to allow a foreign key to be enforced.';
COMMENT ON COLUMN rif40.rif40_inv_covariates.study_geolevel_name
    IS 'Study area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS. Cannot be changed by the user; present to allow a foreign key to be enforced.';
COMMENT ON COLUMN rif40.rif40_inv_covariates.min
    IS 'Minimum value for a covariate';
COMMENT ON COLUMN rif40.rif40_inv_covariates.max
    IS 'Maximum value for a covariate';
COMMENT ON COLUMN rif40.rif40_inv_covariates.covariate_type
    IS 'Covariate type: N normal; A: additional (not used in the calculations)';

GRANT ALL ON TABLE rif40.rif40_inv_covariates TO rif40;
GRANT DELETE, UPDATE, SELECT, INSERT ON TABLE rif40.rif40_inv_covariates TO rif_user;
GRANT DELETE, UPDATE, SELECT, INSERT ON TABLE rif40.rif40_inv_covariates TO rif_manager;

-- FUNCTION: rif40_trg_pkg.trgf_rif40_inv_covariates()

CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_rif40_inv_covariates()
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
			INSERT INTO t_rif40_inv_covariates (
				username,
				study_id,
				inv_id,
				covariate_name,
				covariate_type,
				min,
				max,
				geography,
				study_geolevel_name)
			VALUES(
				coalesce(NEW.username, "current_user"()),
				coalesce(NEW.study_id, (currval('rif40_study_id_seq'::regclass))::integer),
				coalesce(NEW.inv_id, (currval('rif40_inv_id_seq'::regclass))::integer),
				NEW.covariate_name /* no default value */,
				coalesce(NEW.covariate_type, 'N'),
				NEW.min /* no default value */,
				NEW.max /* no default value */,
				NEW.geography /* no default value */,
				NEW.study_geolevel_name /* no default value */);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_inv_covariates',
				'Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL', USER::VARCHAR, NEW.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
		IF USER = OLD.username AND NEW.username = OLD.username THEN
			UPDATE t_rif40_inv_covariates
			   SET username=NEW.username,
			       study_id=NEW.study_id,
			       inv_id=NEW.inv_id,
			       covariate_name=NEW.covariate_name,
			       covariate_type=NEW.covariate_type,
			       min=NEW.min,
			       max=NEW.max,
			       geography=NEW.geography,
			       study_geolevel_name=NEW.study_geolevel_name
			 WHERE study_id=OLD.study_id
			   AND inv_id=OLD.inv_id
			   AND covariate_name=OLD.covariate_name;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_inv_covariates',
				'Cannot UPDATE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'DELETE' THEN
--
-- Check USER = OLD.username; if OK DELETE
--
		IF USER = OLD.username THEN
			DELETE FROM t_rif40_inv_covariates
			 WHERE study_id=OLD.study_id
			   AND inv_id=OLD.inv_id
			   AND covariate_name=OLD.covariate_name;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_inv_covariates',
				'Cannot DELETE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NULL;
	END IF;
	RETURN NEW;
END;

$BODY$;

ALTER FUNCTION rif40_trg_pkg.trgf_rif40_inv_covariates()
    OWNER TO rif40;

COMMENT ON FUNCTION rif40_trg_pkg.trgf_rif40_inv_covariates()
    IS 'INSTEAD OF trigger for table T_RIF40_INV_COVARIATES to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted.
 [NO TABLE/VIEW comments available]';

CREATE TRIGGER trg_rif40_inv_covariates
    INSTEAD OF INSERT OR DELETE OR UPDATE
    ON rif40.rif40_inv_covariates
    FOR EACH ROW
    EXECUTE PROCEDURE rif40_trg_pkg.trgf_rif40_inv_covariates();

COMMENT ON TRIGGER trg_rif40_inv_covariates ON rif40.rif40_inv_covariates
    IS 'INSTEAD OF trigger for view T_RIF40_INV_COVARIATES to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted.
 [NO TABLE/VIEW comments available]';

SELECT geography, covariate_name, covariate_type, COUNT(*) AS total
  FROM rif40.t_rif40_inv_covariates
 GROUP BY geography, covariate_name, covariate_type;
SELECT geography, covariate_name, covariate_type, COUNT(*) AS total
  FROM rif40.rif40_inv_covariates
 GROUP BY geography, covariate_name, covariate_type;

\dS+ rif40.rif40_inv_covariates
\dS+ rif40.t_rif40_inv_covariates

\i ../PLpgsql/rif40_sm_pkg/rif40_verify_state_change.sql

/*
--
-- Testing stop
--

DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'Stop processing';
END;
$$;
 */

END;
--
--  Eof
