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
-- Rapid Enquiry Facility (RIF) - RIF alter script 1 - Middleware support
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
-- To run:
--
-- Sync github to current
-- cd to: <Github repository>\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
-- e.g. P:\Github\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
-- Run script alter_scripts\v4_0_alter_1.sql using psql on sahusland_dev as rif40 (schema owner)
-- The relative path is important!
-- P:\Github\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>psql -U rif40 -d sahsuland_dev -w -e -f alter_scripts\v4_0_alter_1.sql
-- Beware: script discards all changes whilst under development!:
/* At end of script>>>
--
-- Disable script by discarding all changes
--
DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'Aborting (script being tested)';
	RAISE EXCEPTION 'C20999: Abort';
END;
$$;
 */
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #1 Miscelleanous database changes, web services...

--
-- Note gid fix for geometry tables not yet (25/6/2014) fixed in geometry load functions
--
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
-- Add web services
--
\i ../PLpgsql/v4_0_rif40_geo_pkg.sql
\i ../PLpgsql/v4_0_rif40_xml_pkg.sql

--
-- This alter script can be run >once
--
-- Additional tables/columns as required by Fred (being specified):
-- 
--    Add: gid_rowindex (i.e 1_1). Where gid corresponds to gid in geometry table
--         row_index is an incremental serial aggregated by gid ( starts from one for each gid)
-- 
-- This script must be run first
\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
--
-- Functions to enable debug for
--
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl', 'gid_rowindex_fix',
		'rif40_get_geojson_as_js', '_rif40_get_geojson_as_js', '_rif40_getGeoLevelExtentCommon', 'rif40_get_geojson_tiles', 
		'rif40_getAllAttributesForGeoLevelAttributeTheme', 'rif40_GetGeometryColumnNames', 'rif40_GetMapAreaAttributeValue',
		'rif40_closeGetMapAreaAttributeCursor', 'rif40_CreateMapAreaAttributeSource', 'rif40_DeleteMapAreaAttributeSource'];
--
	c1alter2 CURSOR FOR /* Built geographies */
		SELECT *
		  FROM rif40_geographies a, pg_tables b
		 WHERE b.tablename = 't_rif40_'||LOWER(a.geography)||'_geometry';
	c1_rec RECORD;
--
	l_function 			VARCHAR;
BEGIN
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
	FOR c1_rec IN c1alter2 LOOP
		PERFORM rif40_geo_pkg.gid_rowindex_fix(c1_rec.geography);
	END LOOP;
END;
$$;

--
-- Middleware enhancements as required by Kev (being specified):
--
--    Remove: foreign key t_rif40_inv_geography_fk from t_rif40_investigations, NUll data, remove column
-- 
-- This must be run before alter_3.sql (hash partitioning of all tables with study_id as a column)
-- Check if partitioned
DO LANGUAGE plpgsql $$
DECLARE
--
	c1alter2 CURSOR FOR
		SELECT relhassubclass 
		  FROM pg_class t, pg_namespace n
		 WHERE t.relname = 't_rif40_investigations' AND t.relkind = 'r' /* Table */ AND n.nspname = 'rif40'  AND t.relnamespace = n.oid ;
	c1_rec RECORD;
--
-- Functions to enable debug for
--
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl'];
--
	c2alter2 CURSOR FOR
		SELECT column_name 
		  FROM information_schema.columns 
		 WHERE table_name = 't_rif40_investigations' AND column_name = 'geography' AND table_schema = 'rif40';
	c2_rec RECORD;	
	l_function 			VARCHAR;
BEGIN
	OPEN c1alter2;
	FETCH c1alter2 INTO c1_rec;
	CLOSE c1alter2;
	OPEN c2alter2;
	FETCH c2alter2 INTO c2_rec;
	CLOSE c2alter2;	
--
	IF c1_rec.relhassubclass IS NULL THEN
		RAISE EXCEPTION 'C20900: t_rif40_investigations does not exist';	
	ELSIF c1_rec.relhassubclass THEN
		IF c2_rec.column_name = 'geography' THEN
			RAISE EXCEPTION 'C20900: t_rif40_investigations is partitioned, but the geography column still exists';	
		ELSE
			RAISE NOTICE 'C20900: t_rif40_investigations is partitioned, alter_3.sql has been run';	
		END IF;
	ELSE

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
-- Drop constraint
		PERFORM rif40_sql_pkg.rif40_ddl('ALTER TABLE t_rif40_investigations DROP CONSTRAINT IF EXISTS t_rif40_inv_geography_fk');
-- Drop trigger
		PERFORM rif40_sql_pkg.rif40_ddl('DROP TRIGGER t_rif40_investigations_checks ON t_rif40_investigations');
		
		IF c2_rec.column_name = 'geography' THEN
		
-- Make not NULL
			PERFORM rif40_sql_pkg.rif40_ddl('ALTER TABLE t_rif40_investigations ALTER COLUMN geography DROP NOT NULL');
-- NULL column
			PERFORM rif40_sql_pkg.rif40_ddl('UPDATE t_rif40_investigations SET geography = NULL');
		END IF;		
	END IF;
END;
$$;

-- Put trigger back
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_investigations_checks.sql

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
CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_rif40_investigations()
  RETURNS trigger AS
$BODY$
BEGIN
	IF TG_OP = 'INSERT' THEN
--
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
		IF (USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) AND rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
			INSERT INTO t_rif40_investigations (
				username,
				inv_id,
				study_id,
				inv_name,
				year_start,
				year_stop,
				max_age_group,
				min_age_group,
				genders,
				numer_tab,
				mh_test_type,
				inv_description,
				classifier,
				classifier_bands,
				investigation_state)
			VALUES(
				coalesce(NEW.username, "current_user"()),
				coalesce(NEW.inv_id, (nextval('rif40_inv_id_seq'::regclass))::integer),
				coalesce(NEW.study_id, (currval('rif40_study_id_seq'::regclass))::integer),
				NEW.inv_name /* no default value */,
				NEW.year_start /* no default value */,
				NEW.year_stop /* no default value */,
				NEW.max_age_group /* no default value */,
				NEW.min_age_group /* no default value */,
				NEW.genders /* no default value */,
				NEW.numer_tab /* no default value */,
				coalesce(NEW.mh_test_type, 'No Test'::character varying),
				NEW.inv_description /* no default value */,
				coalesce(NEW.classifier, 'QUANTILE'::character varying),
				coalesce(NEW.classifier_bands, 5),
				coalesce(NEW.investigation_state, 'C'::character varying));
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_investigations',
				'Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL', USER::VARCHAR, NEW.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
		IF USER = OLD.username AND NEW.username = OLD.username THEN
			UPDATE t_rif40_investigations
			   SET username=NEW.username,
			       inv_id=NEW.inv_id,
			       study_id=NEW.study_id,
			       inv_name=NEW.inv_name,
			       year_start=NEW.year_start,
			       year_stop=NEW.year_stop,
			       max_age_group=NEW.max_age_group,
			       min_age_group=NEW.min_age_group,
			       genders=NEW.genders,
			       numer_tab=NEW.numer_tab,
			       mh_test_type=NEW.mh_test_type,
			       inv_description=NEW.inv_description,
			       classifier=NEW.classifier,
			       classifier_bands=NEW.classifier_bands,
			       investigation_state=NEW.investigation_state
			 WHERE inv_id=OLD.inv_id
			   AND study_id=OLD.study_id;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_investigations',
				'Cannot UPDATE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'DELETE' THEN
--
-- Check USER = OLD.username; if OK DELETE
--
		IF USER = OLD.username THEN
			DELETE FROM t_rif40_investigations
			 WHERE inv_id=OLD.inv_id
			   AND study_id=OLD.study_id;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_investigations',
				'Cannot DELETE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NULL;
	END IF;
	RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION rif40_trg_pkg.trgf_rif40_investigations() IS 'INSTEAD OF trigger for view T_RIF40_INVESTIGATIONS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
 [NO TABLE/VIEW comments available]';
	
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

--
-- Remove t_rif40_investigations.geography, rif40_investigations.geography from rif40_error_messages
--
SELECT table_or_view_name_hide, column_name_hide
  FROM rif40_columns
 WHERE column_name_hide = 'GEOGRAPHY'
   AND table_or_view_name_hide IN ('RIF40_INVESTIGATIONS','T_RIF40_INVESTIGATIONS');
DELETE FROM rif40_columns
 WHERE column_name_hide = 'GEOGRAPHY'
   AND table_or_view_name_hide IN ('RIF40_INVESTIGATIONS','T_RIF40_INVESTIGATIONS');
   

--
-- Done
--

--
-- Disable script by discarding all changes
--
DO LANGUAGE plpgsql $$
BEGIN
--
--	RAISE INFO 'Aborting (script being tested)';
--	RAISE EXCEPTION 'C20999: Abort';
--
	RAISE INFO 'alter_1.sql completed OK';
END;
$$;

--
-- End of transaction
--
END;

--
-- Vacuum geometry tables, partitions and t_rif40_investigations
--
/* VACUUM ANALYSE t_rif40_sahsu_geometry;
VACUUM ANALYSE p_rif40_geolevels_geometry_sahsu_level1;
VACUUM ANALYSE p_rif40_geolevels_geometry_sahsu_level2;
VACUUM ANALYSE p_rif40_geolevels_geometry_sahsu_level3;
VACUUM ANALYSE p_rif40_geolevels_geometry_sahsu_level4;
New SAHSULAND */
VACUUM ANALYSE t_rif40_investigations;

DO LANGUAGE plpgsql $$
BEGIN
--VARCHAR
	RAISE INFO 'alter_1.sql completed OK, VACUUM ANALYZE OK';
END;
$$;

--
-- Eof
