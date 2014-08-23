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
-- Rapid Enquiry Facility (RIF) - Create SAHSULAND rif40 exmaple schema
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

\echo Creating SAHSULAND rif40 example schema...

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

\pset pager off

--
-- Start transaction
--
BEGIN;

--
-- Drop all objects
--
\i ../psql_scripts/v4_0_drop_rif40.sql

--
-- Check no objects left
--
\i ../psql_scripts/v4_0_postgres_undeleted_checks.sql

--
-- Check availability of extensions
--
DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR(l_name VARCHAR) FOR
                SELECT name, default_version
                  FROM pg_available_extensions
                 WHERE name = l_name;
        c1_rec RECORD;
--
        namelist VARCHAR[]:=ARRAY['adminpack', 'plperl', 'postgis', 'postgis_topology', 'pgcrypto', 'sslinfo', 'xml2', 'dblink', 'plr'];
        x VARCHAR;
        i INTEGER:=0;
BEGIN
        FOREACH x IN ARRAY namelist LOOP
                OPEN c1(x);
                FETCH c1 INTO c1_rec;
                IF c1_rec.name IS NULL THEN
                        RAISE WARNING 'v4_0_create_sahsuland.sql() RIF required extension: % is not installable', x;
                        i:=i+1;
                ELSE
                        RAISE INFO 'v4_0_create_sahsuland.sql() RIF required extension: % V% is installable', c1_rec.name, c1_rec.default_version;
                END IF;
                CLOSE c1;
        END LOOP;
        IF i > 0 THEN
                RAISE WARNING 'v4_0_create_sahsuland.sql() C209xx: % RIF required extensions are not installable', i::VARCHAR USING HINT='See previous warnings';
        END IF;
END;
$$;

--
-- Test user account
-- 
\set ntestuser '''XXXX':testuser''''
SET rif40.testuser TO :ntestuser;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.testuser') AS testuser;
	c2 CURSOR(l_usename VARCHAR) FOR 
		SELECT * FROM pg_user WHERE usename = l_usename;
	c1_rec RECORD;
	c2_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
-- Test parameter
--
	IF c1_rec.testuser IN ('XXXX', 'XXXX:testuser') THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v testuser=<test user account> parameter';	
	ELSE
		RAISE INFO 'db_create.sql() test user account parameter="%"', c1_rec.testuser;
	END IF;
--
-- Test account exists
--
	OPEN c2(LOWER(SUBSTR(c1_rec.testuser, 5)));
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.usename IS NULL THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: User account does not exist: %', LOWER(SUBSTR(c1_rec.testuser, 5));	
	ELSIF pg_has_role(c2_rec.usename, 'rif_user', 'MEMBER') THEN
		RAISE INFO 'db_create.sql() user account="%" is a rif_user', c2_rec.usename;
	ELSIF pg_has_role(c2_rec.usename, 'rif_manager', 'MEMBER') THEN
		RAISE INFO 'db_create.sql() user account="%" is a rif manager', c2_rec.usename;
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User account: % is not a rif_user or rif_manager', c2_rec.usename;	
	END IF;
--
END;
$$;
--
-- PG psql code (logging, auditing and debug)
--
\i ../PLpgsql/v4_0_rif40_log_pkg.sql

--
-- PG psql code (SQL and Oracle compatibility processing)
--
\i ../PLpgsql/v4_0_rif40_sql_pkg.sql
\i ../PLpgsql/v4_0_rif40_sql_pkg_ddl_checks.sql

--
-- Create schema
--
\i ../psql_scripts/v4_0_postgres_tables.sql 

--
-- Add DDL control views (RIF40_TABLES_AND_VIEWS, RIF40_COLUMNS , RIF40_TRIGGERS) [actually tables on postgres]
--
\i ../psql_scripts/create_v4_0_postgres_ddl_control_views.sql

--
-- Create views
--
\i ../psql_scripts/v4_0_postgres_views.sql

--
-- Comment table and views
--
\i ../psql_scripts/create_v4_0_postgres_comments.sql

--
-- PG psql code (geo processing)
--
\i ../PLpgsql/v4_0_rif40_geo_pkg.sql
\i ../PLpgsql/rif40_geo_pkg/v4_0_rif40_geo_pkg_simplification.sql

--
-- PG psql code (state machine and extract SQL generation)
--
\i ../PLpgsql/v4_0_rif40_sm_pkg.sql

--
-- PG psql code (Data extract dump output help functions)
--
\i ../PLpgsql/v4_0_rif40_dmp_pkg.sql

--
-- Create INSTEAD OF triggers to allow INSERT/UPDATE/DELETE on views with USERNAME as a column where USER = username
--
\i ../PLpgsql/v4_0_rif40_trg_pkg.sql
--
-- Add triggers
--
\i ../psql_scripts/v4_0_postgres_triggers.sql

--
-- Disable rif40_geog_defcomparea_fk
--
--ALTER TABLE rif40_geographies DROP CONSTRAINT rif40_geog_defcomparea_fk;

\set VERBOSITY default

--
-- RIF40_PARAMETERS
--
DELETE FROM t_rif40_parameters;
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'SuppressionValue',               '5',							'Suppress results with low cell counts below this value unless the user has the RIF_NO_SUPPRESSION role');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'DeleteTempBayes',                '1',							'Delete INLA Bayesian temporary files (0/1)');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'Metric',                         'Kms',						'Scale metric');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'Geography',                      'SAHSU',						'Default geography');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'UsePostGIS',                    '1',						        'Use the PostGIS Geospatial Database instead of Shape files (0/1).');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'PostGISDB',                      'localhost:sahsuland',				'Name PostGIS Geospatial Database server in the form &lt;host&gt:<&lt>database&gt;');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'FDWServerName',                  '',							'Name of the the Foreign Data Wrapper Server (FDW). '''' disables the use of the Foreign Data Wrappers (remote health data source)');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'FDWServerType',       		  '',							'Type of the the Foreign Data Wrapper Server (FDW). Currently only oracle_fdw is supported. MySQL, Informix and OBDC (thence SQL Server) could potentially be supported. '''' disables the use of the Foreign Data Wrappers (remote health data source)');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'FDWDBServer',       		  '',							'Connector string for the the Foreign Data Wrapper Server (FDW). Currently only oracle_fdw is supported. '''' disables the use of the Foreign Data Wrappers (remote health data source)');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'LinuxPath',		    	  '/home/EPH/group/RIF',				'Path to RIF files on a Linux system');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'WindowsPath',			   'G:\RIF',						'Path to RIF files on a Windows system');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'SaTScanPrmFile',                 'SaTScan\RIF_SaTScan.prm',				'Satscan parameter file, relative to thwe RIf directory. Use Windows path sematics i.e. "\"');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'SaTScanExeFile',                 'SaTScan\SaTScan.exe',				'Satscan executable');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'WinBUGSExeFile',                 'WinBUGS14\WinBUGS14.exe',				'Winbugs executable');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'INLAExeFile',                    'INLA\inla.exe',					'Inla executable');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'ExtractControl',		  '1', 							'Enforce extract permissions.');
INSERT INTO t_rif40_parameters(param_name, param_value, param_description)
VALUES(
'Parallelisation', 		 '4', 							'Level of Parallelisation. Only supported on Oracle; under development in PoatGres/PostGIS.');

--
-- Load SAHSULAND data 
-- 
\i ../sahsuland/v4_0_postgres_sahsuland_imports.sql

--
-- SAHSUland geolevel setup. Fully processed
--
\i ../psql_scripts/v4_0_geolevel_setup_sahsuland.sql

--
-- RIF40_HEALTH_STUDY_THEMES
--
DELETE FROM rif40_health_study_themes  WHERE theme = 'SAHSULAND';
INSERT INTO rif40_health_study_themes(theme, description) VALUES('SAHSULAND', 'SAHSU land cancer incidence example data');

--
-- RIF40_COVARIATES, RIF40_TABLES, RIF40_TABLE_OUTCOMES
--
DELETE FROM rif40_covariates  WHERE geography = 'SAHSU';
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'SAHSU', 'LEVEL3', 'SES', MIN(ses), MAX(ses), 1 FROM sahsuland_covariates_level3;
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'SAHSU', 'LEVEL3', 'ETHNICITY', MIN(ethnicity), MAX(ethnicity), 1 FROM sahsuland_covariates_level3;

INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'SAHSU', 'LEVEL4', 'SES', MIN(ses), MAX(ses), 1 FROM sahsuland_covariates_level4;
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'SAHSU', 'LEVEL4', 'AREATRI1KM', MIN(areatri1km), MAX(areatri1km), 1 FROM sahsuland_covariates_level4;
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'SAHSU', 'LEVEL4', 'TRI_1KM', MIN(tri_1km), MAX(tri_1km), 1 FROM sahsuland_covariates_level4;
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'SAHSU', 'LEVEL4', 'NEAR_DIST', MIN(near_dist), MAX(near_dist), 2 FROM sahsuland_covariates_level4;

DELETE FROM rif40_tables  WHERE table_name IN ('SAHSULAND_CANCER', 'SAHSULAND_POP');
INSERT INTO rif40_tables(table_name, description, theme, year_start, year_stop, total_field, isindirectdenominator, isdirectdenominator, isnumerator, automatic, age_group_id)
VALUES (
'SAHSULAND_CANCER','Cancer cases in SAHSU land', 'SAHSULAND', 1989,1996,'TOTAL',0,0,1,1,1);
INSERT INTO rif40_tables(table_name, description, theme, year_start, year_stop, total_field, isindirectdenominator, isdirectdenominator, isnumerator, automatic, age_group_id)
VALUES (
'SAHSULAND_POP','SAHSU land population', 'SAHSULAND', 1989,1996,NULL,1,0,0,1,1);

DELETE FROM rif40_table_outcomes  WHERE numer_tab = 'SAHSULAND_CANCER';
INSERT INTO rif40_table_outcomes(outcome_group_name, numer_tab, current_version_start_year)
VALUES ('SINGLE_ICD', 'SAHSULAND_CANCER', 1993);

\set VERBOSITY terse
--
-- RIF40_PROJECTS (fix to real users list)
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT * FROM pg_user
 		 WHERE usename != 'postgres' 
		   AND (pg_has_role(usename, 'rif_user', 'MEMBER') OR pg_has_role(usename, 'rif_manager', 'MEMBER'));
--
	c1_rec RECORD;
	sql_stmt VARCHAR;
BEGIN
	FOR c1_rec IN c1 LOOP
		sql_stmt:='INSERT INTO t_rif40_user_projects(project, username) VALUES (''TEST'', '''||c1_rec.usename||''')';
		RAISE INFO 'SQL> %;', sql_stmt;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$$;

--
-- EW01 geography test data for Kevin. It is NOT processed (i.e. no simplification, intersection etc)
-- 
\i ../psql_scripts/v4_0_geolevel_setup_ew01.sql
--
-- UK91 geography test data for Kevin. It is NOT processed (i.e. no simplification, intersection etc)
--
\i ../psql_scripts/v4_0_geolevel_setup_uk91.sql

--
-- Re-enable rif40_geog_defcomparea_fk
--
--ALTER TABLE "rif40_geographies" ADD CONSTRAINT "rif40_geog_defcomparea_fk" FOREIGN KEY ("geography","defaultcomparea") REFERENCES "t_rif40_geolevels" ("geography","geolevel_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

--
-- Re-index
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT * FROM pg_indexes
		 WHERE schemaname = USER
		   AND tablename != 'spatial_ref_sys'
		 ORDER BY 1;
--
	c1_rec 		RECORD;
	sql_stmt 	VARCHAR;
BEGIN
	FOR c1_rec IN c1 LOOP
		sql_stmt:='REINDEX INDEX /* '||c1_rec.tablename||' */ '||c1_rec.indexname;
		RAISE INFO 'SQL> %;', sql_stmt;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$$;

--
-- Cluster Population tables
--
CLUSTER sahsuland_pop USING sahsuland_pop_pk;

--
-- Analyze
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT tablename FROM pg_tables
		 WHERE tableowner = USER
		   AND schemaname = USER
		 ORDER BY 1;
--
	c1_rec 		RECORD;
	sql_stmt 	VARCHAR;
BEGIN
	FOR c1_rec IN c1 LOOP
		sql_stmt:='ANALYZE VERBOSE '||c1_rec.tablename;
		RAISE INFO 'SQL> %;', sql_stmt;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$$;

\set VERBOSITY default

--
-- Create sequences. 
--
CREATE SEQUENCE "rif40_inv_id_seq"; 
CREATE SEQUENCE "rif40_study_id_seq";
COMMENT ON SEQUENCE rif40_inv_id_seq IS 'Used as sequence for unique study index: study_id; auto populated.';
COMMENT ON SEQUENCE rif40_study_id_seq IS 'Used as sequence for unique study index: study_id; auto populated.';

--
-- Set columns to use defaults. 
--
ALTER TABLE t_rif40_studies ALTER COLUMN study_id SET DEFAULT NEXTVAL('rif40_study_id_seq')::INTEGER;
ALTER TABLE t_rif40_investigations ALTER COLUMN inv_id SET DEFAULT NEXTVAL('rif40_inv_id_seq')::INTEGER;
ALTER TABLE t_rif40_investigations ALTER COLUMN study_id SET DEFAULT CURRVAL('rif40_study_id_seq')::INTEGER;

\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR /* All tables with study_id, inv_id foreign key columns apart from t_rif40_investigations */
		SELECT a.tablename, b.attname AS columnname
		  FROM pg_tables a, pg_attribute b, pg_class c
		 WHERE c.relowner IN (SELECT oid FROM pg_roles WHERE rolname = 'rif40') 
		   AND c.oid = b.attrelid
		   AND b.attname IN ('study_id', 'inv_id')
		   AND a.tablename NOT IN ('t_rif40_investigations', 't_rif40_studies')
		   AND a.schemaname != 'rif_studies'			/* Exclude map/extract tables */
		   AND c.relname    = a.tablename
		   AND c.relkind    = 'r' 				/* Relational table */
		   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
		 ORDER BY 1, 2;
--
	c1_rec RECORD;
--
	sql_stmt VARCHAR;
BEGIN
	FOR c1_rec IN c1 LOOP
		sql_stmt:='ALTER TABLE '||c1_rec.tablename||' ALTER COLUMN '||c1_rec.columnname||
			' SET DEFAULT CURRVAL(''rif40_'||c1_rec.columnname||'_seq'')::INTEGER';
		RAISE INFO 'SQL> %;', sql_stmt;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$$;

\set VERBOSITY default
--
-- Grants
--
\i ../psql_scripts/v4_0_postgres_grants.sql 


-- 
-- Load shapefiles - now moved to GIS schema; and run as gis
-- 
-- \i ../shapefiles/shapefiles.sql

--
-- This is a modified ../postgres/rif40_geolevels_geometry.sql (creates T_RIF40_<GEOELVEL>_GEOMETRY etc)
--
\i ../psql_scripts/rif40_geolevels_sahsuland_geometry.sql

--
-- Rebuild INSTEAD OF triggers with correct permissions
--
\i ../PLpgsql/v4_0_rif40_trg_pkg.sql

--
-- Additional view comments
--
\i ../psql_scripts/v4_0_rif40_additional_view_comments.sql

--
-- Non SAHSU specific user schema
--
\i  ../psql_scripts/v4_0_user.sql

--
-- Cleanup map and extract tables not referenced by a study (runs as rif40) 
--
SELECT rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(TRUE /* Truncate */);

--
-- There should be no studies and no orphaned study tables at this point
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT COUNT(study_id) AS num_studies 
		  FROM t_rif40_studies;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.num_studies > 0 THEN
			RAISE EXCEPTION 'C20900: % studies found in database', c1_rec.num_studies::Text;	
	ELSE
			RAISE INFO 'No studies found in database';
	END IF;
END;
$$;

DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT COUNT(tablename) AS num_tables 
		  FROM pg_tables
	     WHERE schemaname = 'rif_studies';
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.num_tables > 0 THEN
			RAISE EXCEPTION 'C20900: % orphaned study tables found in database', c1_rec.num_tables::Text;	
	ELSE
			RAISE INFO 'No orphaned study tables found in database';
	END IF;
END;
$$;

END;

\q

--
-- Check all tables and views are built
--
SELECT table_or_view, LOWER(table_or_view_name_hide) AS table_or_view_name
  FROM rif40_tables_and_views
 WHERE table_or_view = 'TABLE'
EXCEPT
SELECT 'TABLE' AS table_or_view, table_name AS table_or_view_name
  FROM information_schema.tables
UNION
SELECT table_or_view, LOWER(table_or_view_name_hide) AS table_or_view_name
  FROM rif40_tables_and_views
 WHERE table_or_view = 'VIEW'
EXCEPT
SELECT 'TABLE' AS table_or_view, table_name AS table_or_view_name
  FROM information_schema.views
 ORDER BY 1, 2;
--
-- Check all tables, triggers, columns and comments are present, objects granted to rif_user/rif_manmger, sequences granted
-- 
\i ../psql_scripts/v4_0_postgres_ddl_checks.sql

--
-- End of transaction
--
END;

--
-- Post transaction SQL
--
ALTER TABLE "sahsuland_level1" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "sahsuland_level2" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "sahsuland_level3" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "sahsuland_level4" ALTER COLUMN name  SET NOT NULL; 

--
-- Make columns NOT NULL - Cannot be done with PL/pgsql - causes:
-- cannot ALTER TABLE "sahsuland_level1" because it is being used by active queries in this session
--

ALTER TABLE "t_rif40_sahsu_geometry" ALTER COLUMN name  SET NOT NULL;

--
-- Vaccum ANALYZE all RIF40 tables
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT tablename FROM pg_tables
		 WHERE tableowner = USER
--		   AND schemaname = USER
		 ORDER BY 1;
--
	c1_rec 		RECORD;
	sql_stmt 	VARCHAR;
BEGIN
	FOR c1_rec IN c1 LOOP
		sql_stmt:='VACUUM ANALYZE VERBOSE '||c1_rec.tablename;
		RAISE INFO 'SQL> %;', sql_stmt;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$$;

\echo Created SAHSULAND rif40 example schema.
--
-- Eof
\q

--
-- Connect as testuser
--
\c sahsuland_dev :testuser
--
-- Test in a single transaction  
--
-- Initialise user
--
\i ../psql_scripts/v4_0_user.sql      
--
-- Test user access
--
\c sahsuland_dev :testuser
\i ../psql_scripts/v4_0_sahsuland_examples.sql      

--
-- Dump sahsuland database (usually run separately/from create_v4_0.sql Oracle script)
--
-- Exclude UK91, EW01 shapefiles from non dev dumps
--
--\! pg_dump -U postgres -w -F custom sahsuland -T '*x_uk*' -T '*.x_ew01*' -v > ../install/sahsuland.dump
--\! pg_dump -U postgres -w -F plain sahsuland -v > ../install/sahsuland.sql
--\! gzip -f ../install/sahsuland.sql 

--
-- Test access to all tables in SAHSU_TABLES_AND_VIEWS
--
-- ADD
\echo Created SAHSULAND rif40 example schema.

--
-- Eof
