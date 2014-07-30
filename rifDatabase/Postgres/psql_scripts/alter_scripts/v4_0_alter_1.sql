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
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl', 
		'rif40_get_geojson_as_js', '_rif40_get_geojson_as_js', '_rif40_getGeoLevelExtentCommon', 'rif40_get_geojson_tiles', 
		'rif40_getAllAttributesForGeoLevelAttributeTheme', 'rif40_GetGeometryColumnNames', 'rif40_GetMapAreaAttributeValue',
		'rif40_closeGetMapAreaAttributeCursor', 'rif40_CreateMapAreaAttributeSource', 'rif40_DeleteMapAreaAttributeSource'];
--
	c1alter2 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
	c2alter2 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography = l_geography;
	c3alter2 CURSOR(l_table VARCHAR) FOR
		SELECT relhassubclass 
		  FROM pg_class t, pg_namespace n
		 WHERE t.relname = l_table AND t.relkind = 'r' /* Table */ AND n.nspname = 'rif40'  AND t.relnamespace = n.oid ;
	c4alter2 CURSOR(l_table VARCHAR, l_column VARCHAR) FOR
		SELECT column_name 
		  FROM information_schema.columns 
		  WHERE table_name = l_table AND column_name = l_column;	
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
--
	l_function 			VARCHAR;
	ddl_stmt			VARCHAR[];
	l_partition			VARCHAr;
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
		OPEN c3alter2('t_rif40_'||LOWER(c1_rec.geography)||'_geometry');
		FETCH c3alter2 INTO c3_rec;
		CLOSE c3alter2;
		OPEN c4alter2('t_rif40_'||LOWER(c1_rec.geography)||'_geometry', 'gid_rowindex');
		FETCH c4alter2 INTO c4_rec;
		CLOSE c4alter2;
-- Geometry table exists and column has not been added yet
		IF c3_rec.relhassubclass THEN 
			IF c4_rec.column_name IS NOT NULL THEN
				IF ddl_stmt IS NULL THEN
					ddl_stmt[1]:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||' DROP COLUMN gid_rowindex';
				ELSE
					ddl_stmt[array_length(ddl_stmt, 1)+1]:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||' DROP COLUMN gid_rowindex';
				END IF;
			END IF;
-- Add column to master table
			IF ddl_stmt IS NULL THEN
				ddl_stmt[1]:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||' ADD COLUMN gid_rowindex VARCHAR(50)';
			ELSE
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='ALTER TABLE '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||
					' ADD COLUMN gid_rowindex VARCHAR(50)';
			END IF;
-- Comment master and inherited partitions
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON COLUMN '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||
				'.gid_rowindex IS ''GID rowindex record locator unique key''';
--
			FOR c2_rec IN c2alter2(c1_rec.geography) LOOP
				l_partition:=quote_ident('t_rif40_geolevels_geometry_'||LOWER(c2_rec.geography)||'_'||LOWER(c2_rec.geolevel_name));
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON COLUMN '||l_partition||
					'.gid_rowindex IS ''GID rowindex record locator unique key''';
/*
Other databases may have issue with this CTE update syntax:

WITH a AS (
	SELECT area_id, gid, gid||'_'||ROW_NUMBER() OVER(PARTITION BY gid ORDER BY area_id) AS gid_rowindex
	  FROM t_rif40_geolevels_geometry_sahsu_level2
)
UPDATE t_rif40_geolevels_geometry_sahsu_level2 b
   SET gid_rowindex = a.gid_rowindex
  FROM a
 WHERE b.area_id = a.area_id;

 */
-- Fix gid so it it unique per area_id /(ST_Union'ed together - so are)
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='WITH a AS ('||E'\n'||
E'\t'||'SELECT area_id, gid'||E'\n'||
E'\t'||'  FROM '||l_partition||E'\n'||
E'\t'||'  ORDER BY area_id '||E'\n'||
'), b AS ('||E'\n'||
E'\t'||'SELECT a.area_id, a.gid, ROW_NUMBER() OVER() AS new_gid'||E'\n'||
E'\t'||'  FROM a'||E'\n'||
')'||E'\n'||
'UPDATE '||l_partition||' c'||E'\n'||
'   SET gid = b.new_gid'||E'\n'||
'  FROM b'||E'\n'||
' WHERE c.area_id = b.area_id';
-- Update gid_rowindex
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='WITH a AS ('||E'\n'||
E'\t'||'SELECT area_id, gid,'||E'\n'||
E'\t'||'       LPAD(gid::Text, 10, ''0''::Text)||''_''||LPAD(ROW_NUMBER() OVER(PARTITION BY gid ORDER BY area_id)::Text, 10, ''0''::Text) AS gid_rowindex'||E'\n'||
E'\t'||'  FROM '||l_partition||E'\n'||
')'||E'\n'||
'UPDATE '||l_partition||' b'||E'\n'||
'   SET gid_rowindex = a.gid_rowindex'||E'\n'||
'  FROM a'||E'\n'||
' WHERE b.area_id = a.area_id';
-- Create unqiue indexes
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='CREATE UNIQUE INDEX '||l_partition||'_gidr ON '||l_partition||'(gid_rowindex)';
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='DROP INDEX IF EXISTS '||l_partition||'_gid';
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='CREATE UNIQUE INDEX '||l_partition||'_gid ON '||l_partition||'(gid)';
-- Make not null
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='ALTER TABLE '||l_partition||' ALTER COLUMN gid_rowindex SET NOT NULL';
-- Analyse
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='ANALYZE VERBOSE '||l_partition;
			END LOOP;

-- Analyze at master level
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='ANALYZE VERBOSE '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry');
		END IF;
	END LOOP;
--
	IF ddl_stmt IS NOT NULL THEN
		PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);
	ELSE
		RAISE INFO 'GID and GID_ROWINDEX support already added';
	END IF;
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
BEGIN
	OPEN c1alter2;
	FETCH c1alter2 INTO c1_rec;
	CLOSE c1alter2;
--
	IF c1_rec.relhassubclass IS NULL THEN
		RAISE EXCEPTION 'C20900: t_rif40_investigations does not exist';	
	ELSIF c1_rec.relhassubclass THEN
		RAISE EXCEPTION 'C20900: t_rif40_investigations is partitioned, alter_3.sql has been run';	
	END IF;
END;
$$;

-- Drop constraint
ALTER TABLE t_rif40_investigations DROP CONSTRAINT IF EXISTS t_rif40_inv_geography_fk;
-- Drop trigger
DROP TRIGGER t_rif40_investigations_checks ON t_rif40_investigations;

DO LANGUAGE plpgsql $$
DECLARE
--
-- Functions to enable debug for
--
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl'];
--
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

SElECT area_id, name, gid, gid_rowindex
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL2'
 ORDER BY gid;

--
-- Bounding box functions
--
SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelFullExtent('SAHSU' /* Geography */, 'LEVEL2' /* Geolevel view */);
WITH a AS (
	SELECT MIN(study_id) AS min_study_id
	  FROM t_rif40_studies
)
SELECT b.* 
  FROM a, rif40_xml_pkg.rif40_getGeoLevelFullExtentForStudy(
	'SAHSU' /* Geography */, 'LEVEL4' /* Geolevel view */, a.min_study_id /* Study ID */) b;
SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU' /* Geography */, 'LEVEL2' /* Geolevel view */, '01.004' /* Map area ID */);
/*
  y_max  |  x_max   |  y_min  |  x_min
---------+----------+---------+----------
 55.0122 | -6.32507 | 54.6456 | -6.68853
(1 row)
 */

--
-- Old rif40_get_geojson_as_js() - technically not part of the new web services interface
--
-- View the <geolevel view> (e.g. GOR2001) of <geolevel area> (e.g. London) and select at <geolevel select> (e.g. LADUA2001) level.
-- or in this case: 
-- "View the <geolevel view> (e.g. LEVEL4) of <geolevel area> (e.g. 01.004) and select at <geolevel select> (e.g. LEVEL2) level".
--
\copy (SELECT * FROM rif40_xml_pkg.rif40_get_geojson_as_js('SAHSU' /* Geography */, 'LEVEL4' /* geolevel view */, 'LEVEL2' /* geolevel area */, '01.004' /* geolevel area id */, FALSE /* return_one_row flag: output multiple rows so it is readable! */)) to ../tests/sahsu_geojson_test_01.js 

--
-- GetGeoJsonTiles interface
--
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')
) 
SELECT SUBSTRING(
		rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSU' 	/* Geography */, 
			'LEVEL4' 	/* geolevel view */, 
			a.y_max, a.x_max, a.y_min, a.x_min, /* Bounding box - from cte */
			FALSE /* return_one_row flag: output multiple rows so it is readable! */) FROM 1 FOR 160) AS js 
  FROM a LIMIT 4;
/*
                                                                                js
-----------------------------------------------------------------------------------------------------------------------------------------------------------------
 var spatialData={ "type": "FeatureCollection","features": [ /- Start -/
 {"type": "Feature","properties":{"area_id":"01.001.000100.1","name":"Abellan LEVEL4(01.001.000100.1)","area":"238.40","total_males":"4924.00","total_females":"5
 ,{"type": "Feature","properties":{"area_id":"01.002.001100.1","name":"Cobley LEVEL4(01.002.001100.1)","area":"10.00","total_males":"5246.00","total_females":"54
 ,{"type": "Feature","properties":{"area_id":"01.002.001300.5","name":"Cobley LEVEL4(01.002.001300.5)","area":"2.80","total_males":"2344.00","total_females":"218
(4 rows)
 */
--
-- Use copy to create a Javascript file that can be tested
--
\copy (WITH a AS (SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')) SELECT rif40_xml_pkg.rif40_get_geojson_tiles('SAHSU' /* Geography */, 'LEVEL4' /* geolevel view */, a.y_max, a.x_max, a.y_min, a.x_min, FALSE /* return_one_row flag: output multiple rows so it is readable! */) FROM a) to ../tests/sahsu_geojson_test_02.js

--
-- Attribute fetch functions
--
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'covariate');
/*
rif40_getNumericAttributesForGeoLevelAttributeTheme() not implemented, use the is_numeric BOOLEAN flag in above
rif40_AttributeExistsForGeoLevelAttributeTheme() not implemented, use the attribute name filter to select by named attribute

Note there is currently no support for health themes.
 */
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'population')
 WHERE is_numeric /* rif40_getNumericAttributesForGeoLevelAttributeTheme() example */;
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'covariate', 
		ARRAY['SES'] /* rif40_AttributeExistsForGeoLevelAttributeTheme() example */);

--
-- Dump all attributes and themes
--
WITH a AS (
	SELECT e.enumlabel, e.enumsortorder
          FROM pg_type t, pg_enum e
 	 WHERE t.oid = e.enumtypid
	   AND  t.typname = 'rif40_geolevelattributetheme'
	 ORDER BY enumsortorder
)	
SELECT b.theme, b.attribute_source, b.attribute_name, b.name_description, b.ordinal_position, b.is_numeric
  FROM a, rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(
	'SAHSU'::VARCHAR, 'LEVEL4'::VARCHAR, a.enumlabel::rif40_xml_pkg.rif40_geolevelAttributeTheme) b;

--
-- rif40_GetGeoLevelAttributeTheme() not implemented, purpose unclear
-- rif40_GetGeometryColumnNames() 
--
SELECT * 
  FROM rif40_xml_pkg.rif40_GetGeometryColumnNames('SAHSU');

--
-- rif40_GetMapAreaAttributeValue();
--

--
-- Demo 1. Sahsuland cancer. All defaults (i.e. all columns, fetch 1000 rows at offset 0)
--
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_2' /* Temporary table */, 
		'SAHSU', 'LEVEL2', 'health', 'sahsuland_cancer');
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(
		'c4getallatt4theme_2' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_2;

--
-- Demo 2. covariate theme; specified columns (forcing re-sort); otherwise defaults
--
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_1' /* Temporary table */, 
		'SAHSU', 'LEVEL4', 'covariate', 'sahsuland_covariates_level4', ARRAY['SES', 'year']);
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(
		'c4getallatt4theme_1' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_1;

/*
psql:alter_scripts/v4_0_alter_2.sql:504: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51214] Cursor: c4getallatt4theme_3, geog
raphy: SAHSU, geolevel select: LEVEL2, theme: population, attribute names: [], source: sahsuland_pop; SQL parse took: 00:00:12.027.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

Time: 12174.738 ms
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000000001 | 1989 |           100 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000002 | 1989 |           100 | 01     | 01.001.000100 | 01.001.000100.2 |    30
 01.001  |   1 | 0000000001_0000000003 | 1989 |           100 | 01     | 01.001.000200 | 01.001.000200.1 |    64
 01.001  |   1 | 0000000001_0000000004 | 1989 |           100 | 01     | 01.001.000300 | 01.001.000300.1 |    80
 01.001  |   1 | 0000000001_0000000005 | 1989 |           101 | 01     | 01.001.000100 | 01.001.000100.1 |    34
(5 rows)

Time: 6559.317 ms
 */
--
-- Demo 3: Poor performance on large tables without gid, gid_rowindex built in and a sort
-- REF_CURSOR takes 12 secnds to parse and execute (caused by the rowindex sort) with explain plan
-- FETCH originally took 7 seconds - i.e. copies results (hopefully in server)! Fixed by creating scrollable REFCURSOR
--
-- Only sorting when the attribute list is specified sppeds things up 4x
--
-- Performance is fine on SAHSULAND_POP
--

\timing
--
-- Create temporary table
--
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_3' /* Temporary table */, 
		'SAHSU', 'LEVEL2', 'population', 'sahsuland_pop');
--
-- Create REFCURSOR
--
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_3' /* Temporary table */,
		NULL /* Cursor name - NULL = same as temporarty table - must be unique with a TX */, 
		0 /* No offset */, NULL /* No row limit */);
--
-- Fetch tests
--
FETCH FORWARD 5 IN c4getallatt4theme_3 /* 1.3 seconds with no row limit, no sort list, no gid/gid_rowindex columns built in */;
MOVE ABSOLUTE 1000 IN c4getallatt4theme_3 /* move to row 1000 */;
FETCH FORWARD 5 IN c4getallatt4theme_3;
MOVE ABSOLUTE 10000 IN c4getallatt4theme_3 /* move to row 10000 */;
FETCH FORWARD 5 IN c4getallatt4theme_3;
MOVE ABSOLUTE 432958 IN c4getallatt4theme_3 /* move to row 432958 - two from the end */;
FETCH FORWARD 5 IN c4getallatt4theme_3;
--
-- Test cursor close. Does release resources!!!!
--
SELECT rif40_xml_pkg.rif40_closeGetMapAreaAttributeCursor('c4getallatt4theme_3');

--
-- Demo 1+2: resources not released until end of transaction
--
SELECT * FROM pg_cursors;
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_3' /* Temporary table */,
		NULL /* Cursor name - NULL = same as temporarty table - must be unique with a TX */, 
		10000 /* Offset */, NULL /* No row limit */);
FETCH FORWARD 5 IN c4getallatt4theme_3;

SELECT rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource('c4getallatt4theme_3');
--
-- Demo 4: Use of offset and row limit, cursor control using FETCH
--
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_3' /* Temporary table */, 
		'SAHSU', 'LEVEL2', 'population', 'sahsuland_pop');
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_3' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_3 /* 12 seconds parse refcursor seconds with 1000 row limit, cursor fetch is fast, no gid/gid_rowindex columns built in */;
MOVE ABSOLUTE 995 IN c4getallatt4theme_3 /* move to row 995 */;
FETCH FORWARD 5 IN c4getallatt4theme_3 /* Fetch last 5 rows */;
--
-- Open second REFCURSOR
--
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(
		'c4getallatt4theme_3' /* Temporary table */, 
		'c4getallatt4theme_3b' /* Cursor name - must be unique with a TX */, 
		1000 /* Offset */, 1000 /* Row limit */);
FETCH FORWARD 5 IN c4getallatt4theme_3b;
--
-- Release resources. Note order; cursors must be released before temporary tables or you will get:
-- cannot DROP TABLE "c4getallatt4theme_3" because it is being used by active queries in this session
--
SELECT rif40_xml_pkg.rif40_closeGetMapAreaAttributeCursor('c4getallatt4theme_3b');
SELECT rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource('c4getallatt4theme_3');

--
-- Extract/map tables tested in alter_2.sql as user rif40 cannot see studies
--


--
-- Demo 5: Check offset works OK
--
SELECT rif40_log_pkg.rif40_remove_from_debug('rif40_CreateMapAreaAttributeSource');
SELECT rif40_log_pkg.rif40_add_to_debug('rif40_add_to_debug:DEBUG1');
SELECT rif40_log_pkg.rif40_add_to_debug('rif40_CreateMapAreaAttributeSource:DEBUG2' /* Enable EXPLAIN PLAN */);
SELECT * 
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_5' /* Must be unique with a TX */, 
		'SAHSU', 'LEVEL2', 'geometry', 't_rif40_sahsu_geometry');
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_5' /* Temporary table */, 
		'c4getallatt4theme_5' /* Cursor name - must be unique with a TX */, 
		0 /* Offset */, 10 /* Row limit */);
FETCH FORWARD 15 IN c4getallatt4theme_5 /* Only fetches 10... */;
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_5' /* Temporary table */, 
		'c4getallatt4theme_5a' /* Cursor name - must be unique with a TX */, 
		0 /* Offset */, 10 /* Row limit */);
FETCH FORWARD 15 IN c4getallatt4theme_5a /* Only fetches 10... */;

SELECT * FROM pg_cursors;

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
VACUUM ANALYSE t_rif40_sahsu_geometry;
VACUUM ANALYSE t_rif40_geolevels_geometry_sahsu_level1;
VACUUM ANALYSE t_rif40_geolevels_geometry_sahsu_level2;
VACUUM ANALYSE t_rif40_geolevels_geometry_sahsu_level3;
VACUUM ANALYSE t_rif40_geolevels_geometry_sahsu_level4;
VACUUM ANALYSE t_rif40_investigations;

DO LANGUAGE plpgsql $$
BEGIN
--
	RAISE INFO 'alter_1.sql completed OK, VACUUM ANALYZE OK';
END;
$$;

--
-- Eof
