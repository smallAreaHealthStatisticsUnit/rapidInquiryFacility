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
-- Rapid Enquiry Facility (RIF) - RIF alter script 2
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

\echo Running SAHSULAND schema alter script #2 Miscelleanous database changes...

BEGIN;

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
-- This alter script can be run >once
--
-- Additional tables/columns as required by Fred (being specified):
-- 
--    Add: gid_rowindex (i.e 1_1). Where gid corresponds to gid in geometry table
--         row_index is an incremental serial aggregated by gid ( starts from one for each gid)
-- 

--
-- Middleware enhancements as required by Kev (being specified):
--
--    Remove: foreign key t_rif40_inv_geography_fk from t_rif40_investigations, NUll data, remove column
-- 
-- Drop constraint
ALTER TABLE t_rif40_investigations DROP CONSTRAINT IF EXISTS t_rif40_inv_geography_fk;
-- Drop trigger
DROP TRIGGER t_rif40_investigations_checks ON t_rif40_investigations;

DO LANGUAGE plpgsql $$
DECLARE
DECLARE
--
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl'];
	c1alter2 CURSOR FOR
		SELECT column_name 
		  FROM information_schema.columns 
		 WHERE table_name = 't_rif40_investigations' AND column_name = 'geography' AND table_schema = 'rif40';
	c1_rec RECORD;
	l_function 			VARCHAR;
BEGIN
	OPEN c1alter2;
	FETCH c1alter2 INTO c1_rec;
	CLOSE c1alter2;
--
-- Turn on some debug
--
        PERFORM rif40_log_pkg.rif40_log_setup();
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
	FOREACH l_function IN ARRAY rif40_sql_pkg_functions LOOP
		RAISE INFO 'Enable debug for function: %', l_function;
		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
	END LOOP;
	IF c1_rec.column_name = 'geography' THEN
-- Make not NULL
		PERFORM rif40_sql_pkg.rif40_ddl('ALTER TABLE t_rif40_investigations ALTER COLUMN geography DROP NOT NULL');
-- NULL column
		PERFORM rif40_sql_pkg.rif40_ddl('UPDATE t_rif40_investigations SET geography = NULL');
	END IF;
END;
$$;

-- Put trigger back
CREATE TRIGGER t_rif40_investigations_checks
	BEFORE INSERT OR UPDATE OF username, inv_name, inv_description, year_start, year_stop,
		max_age_group, min_age_group, genders, numer_tab, investigation_state,
	        study_id, inv_id, classifier, classifier_bands, mh_test_type ON t_rif40_investigations
	FOR EACH ROW	
	WHEN ((NEW.username IS NOT NULL AND NEW.username::text <> '') OR 
		(NEW.inv_name IS NOT NULL AND NEW.inv_name::text <> '') OR 
		(NEW.inv_description IS NOT NULL AND NEW.inv_description::text <> '') OR
		(NEW.year_start IS NOT NULL AND NEW.year_start::text <> '') OR 
		(NEW.year_stop IS NOT NULL AND NEW.year_stop::text <> '') OR 
		(NEW.max_age_group IS NOT NULL AND NEW.max_age_group::text <> '') OR 
		(NEW.min_age_group IS NOT NULL AND NEW.min_age_group::text <> '') OR
		(NEW.genders IS NOT NULL AND NEW.genders::text <> '') OR
		(NEW.investigation_state IS NOT NULL AND NEW.investigation_state::text <> '') OR 
		(NEW.numer_tab IS NOT NULL AND NEW.numer_tab::text <> '') OR
	       	(NEW.study_id IS NOT NULL AND NEW.study_id::text <> '') OR 
		(NEW.inv_id IS NOT NULL AND NEW.inv_id::text <> '') OR
		(NEW.classifier IS NOT NULL AND NEW.classifier::text <> '') OR 
		(NEW.classifier_bands IS NOT NULL AND NEW.classifier_bands::text <> '') OR 
		(NEW.mh_test_type IS NOT NULL AND NEW.mh_test_type::text <> ''))
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks();
COMMENT ON TRIGGER t_rif40_investigations_checks ON t_rif40_investigations IS 'DELETE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks()';

-- Rebuild updateable view
DROP VIEW rif40_investigations;
CREATE VIEW rif40_investigations AS 
 SELECT c.username,
    c.inv_id,
    c.study_id,
    c.inv_name,
    c.year_start,
    c.year_stop,
    c.max_age_group,
    c.min_age_group,
    c.genders,
    c.numer_tab,
    c.mh_test_type,
    c.inv_description,
    c.classifier,
    c.classifier_bands,
    c.investigation_state
   FROM t_rif40_investigations c
   LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username::name = "current_user"()
  WHERE c.username::name = "current_user"() OR 'RIF_MANAGER'::text = (( SELECT user_role_privs.granted_role
      FROM user_role_privs
     WHERE user_role_privs.granted_role = 'RIF_MANAGER'::text)) OR s.grantee_username IS NOT NULL AND s.grantee_username::text <> ''::text
  ORDER BY c.username;

ALTER TABLE rif40_investigations
  OWNER TO rif40;
GRANT ALL ON TABLE rif40_investigations TO rif40;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_investigations TO rif_user;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_investigations TO rif_manager;
COMMENT ON VIEW rif40_investigations
  IS 'Details of each investigation in a study';
COMMENT ON COLUMN rif40_investigations.username IS 'Username';
COMMENT ON COLUMN rif40_investigations.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN rif40_investigations.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_investigations.inv_name IS 'Name of investigation. Must be a valid database column name, i.e. only contain A-Z0-9_ and start with a letter.';
COMMENT ON COLUMN rif40_investigations.year_start IS 'Year investigation is to start. Must be between the limnits specified in the numerator and denominator tables';
COMMENT ON COLUMN rif40_investigations.year_stop IS 'Year investigation is to stop';
COMMENT ON COLUMN rif40_investigations.max_age_group IS 'Maximum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &gt; MIN_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME';
COMMENT ON COLUMN rif40_investigations.min_age_group IS 'Minimum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &lt; MAX_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME';
COMMENT ON COLUMN rif40_investigations.genders IS 'Genders to be investigated: 1 - males, 2 female or 3 - both';
COMMENT ON COLUMN rif40_investigations.numer_tab IS 'Numerator table name. May be &quot;DUMMY&quot; if extract created outside of the RIF.';
COMMENT ON COLUMN rif40_investigations.mh_test_type IS 'Mantel-Haenszel test type: &quot;No test&quot;, &quot;Comparison Areas&quot;, &quot;Unexposed Area&quot;.';
COMMENT ON COLUMN rif40_investigations.inv_description IS 'Description of investigation';
COMMENT ON COLUMN rif40_investigations.classifier IS 'Maps classifier. EQUAL_INTERVAL: each classifier band represents the same sized range and intervals change based on max an min, JENKS: Jenks natural breaks, QUANTILE: equiheight (even number) distribution, STANDARD_DEVIATION, UNIQUE_INTERVAL: a version of EQUAL_INTERVAL that takes into account unique values, &lt;BESPOKE&gt;; default QUANTILE. &lt;BESPOKE&gt; classification bands are defined in: RIF40_CLASSFIER_BANDS, RIF40_CLASSFIER_BAND_NAMES and are used to create maps that are comparable accross investigations';
COMMENT ON COLUMN rif40_investigations.classifier_bands IS 'Map classifier bands; default 5. Must be between 2 and 20';
COMMENT ON COLUMN rif40_investigations.investigation_state IS 'Investigation state - C: created, not verfied; V: verified, but no other work done; E - extracted imported or created, but no results or maps created; R: results computed; U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.';
CREATE TRIGGER trg_rif40_investigations
  INSTEAD OF INSERT OR UPDATE OR DELETE
  ON rif40_investigations
  FOR EACH ROW
  EXECUTE PROCEDURE rif40_trg_pkg.trgf_rif40_investigations();
COMMENT ON TRIGGER trg_rif40_investigations ON rif40_investigations IS 'INSTEAD OF trigger for view T_RIF40_INVESTIGATIONS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
 [NO TABLE/VIEW comments available]';

-- FINALLY: Drop the column
ALTER TABLE t_rif40_investigations DROP COLUMN IF EXISTS geography;
--\dS+ t_rif40_investigations

DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'Aborting (script being tested)';
	RAISE EXCEPTION 'C20999: Abort';
END;
$$;

--
-- End of transaction
--
	RAISE INFO 'alter_2.sql completed OK';
END;

--
-- Eof
