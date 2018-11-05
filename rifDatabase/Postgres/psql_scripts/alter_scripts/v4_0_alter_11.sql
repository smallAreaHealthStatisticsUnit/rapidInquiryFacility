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
 
 */
BEGIN;

--
-- 1. Support for Postgres 10 partitioning
--
\i ../../PLpgsql/rif40_trg_pkg/trigger_fct_rif40_tables_checks.sql

--
-- 2. Intersection counting (study areas only)	
--
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE t_rif40_study_areas ADD COLUMN intersect_count INTEGER NULL;
	ALTER TABLE t_rif40_study_areas ADD COLUMN distance_from_nearest_source INTEGER NULL;
	ALTER TABLE t_rif40_study_areas ADD COLUMN nearest_rifshapepolyid VARCHAR NULL;
	COMMENT ON COLUMN t_rif40_study_areas.intersect_count IS 'Number of intersects with shapes';
	COMMENT ON COLUMN t_rif40_study_areas.distance_from_nearest_source IS 'Distance from nearest source (Km)';
	COMMENT ON COLUMN t_rif40_study_areas.nearest_rifshapepolyid IS 'Nearest rifshapepolyid (shape reference)';
EXCEPTION	
	WHEN duplicate_column THEN
		RAISE NOTICE 'Column already renamed: %',SQLERRM::Text;  
END;
$$;

DROP VIEW IF EXISTS rif40.rif40_study_areas;
ALTER TABLE t_rif40_study_areas ALTER COLUMN nearest_rifshapepolyid SET DATA TYPE VARCHAR;
--
-- Rebuild view
--
CREATE VIEW rif40.rif40_study_areas AS
 SELECT c.username,
    c.study_id,
    c.area_id,
    c.band_id,
	c.intersect_count,
	c.distance_from_nearest_source,
	c.nearest_rifshapepolyid
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
				nearest_rifshapepolyid
				)
			VALUES(
				coalesce(NEW.username, "current_user"()),
				coalesce(NEW.study_id, (currval('rif40_study_id_seq'::regclass))::integer),
				NEW.area_id /* no default value */,
				NEW.band_id /* no default value */,
				NEW.intersect_count /* no default value */,
				NEW.distance_from_nearest_source /* no default value */,
				NEW.nearest_rifshapepolyid /* no default value */);
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
				   nearest_rifshapepolyid=NEW.nearest_rifshapepolyid
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