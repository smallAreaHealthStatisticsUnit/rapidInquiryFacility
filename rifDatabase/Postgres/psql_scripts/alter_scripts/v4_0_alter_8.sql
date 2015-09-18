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
-- Rapid Enquiry Facility (RIF) - RIF alter script 8 - Test harness support
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

\echo Running SAHSULAND schema alter script #8 test harness support.

/*

Alter script #8: test harness support

1. Create test harness tables: rif40_test_runs, rif40_test_harness and related sequences
2. Add "Test harness" to RIF40_TABLES_AND_VIEWS classes
3. Add to RIF40_TABLES_AND_VIEWS, RIF40_COLUMNS

TODO:

4. Integrate into rif40_sql_pkg.rif40_sql_test() to:
   - Add test harness support
   - Auto register test cases
   - Auto register error and informational messages in RIF40_ERROR_MESSAGES
5. Check serial behaviour access savepoints/transactions
6. Integrate test harness support into test 8 (and 1)
   - Check transaction control using savepoints is correct 
7. Create run function rif40_sql_pkg.rif40_sql_test_run(test_run_title DEFAULT NULL - all)
8. Create test dump to dump test harness to common CSV file, excluding array data

Intended transactional control:

BEGIN -* Test harness transaction *-;

-- Setup

-- Call rif40_sql_pkg.rif40_sql_test(); e.g.

	IF NOT (rif40_sql_pkg.rif40_sql_test(	
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #2'', ''EXTRACT_TRIGGER_TEST_2'', ''MAP_TRIGGER_TEST_2'', 1 -* Diease mapping *-, ''LEVEL1'', ''LEVEL4'', ''SAHSULAND_POP'', NULL -* FAIL HERE *-)
RETURNING 1::Text AS test_value',
		'TRIGGER TEST #2: rif40_studies.suppression_value IS NULL',
		'{1}'::Text[][] -* Results for trigger - DEFAULTED value will not work - table is mutating and row does not exist at the point of INSERT *-,
		NULL 			-* Expected SQLCODE *-, 
		FALSE 			-* Do not RAISE EXCEPTION on failure *-)) THEN
		errors:=errors+1;
	ELSE	
		PERFORM rif40_sql_pkg.rif40_ddl('DELETE FROM rif40_studies WHERE study_name = ''TRIGGER TEST #2''');
    END IF;	
In rif40_sql_pkg.rif40_sql_test()
- Auto register test case
- Create savepoint
- Run test case
- On error, if raise_exception_on_failure is FALSE, catch
- On success, rollback to savepoint, update rif40_test_runs, rif40_test_harness
 
-- Update rif40_test_runs, rif40_test_harness
 
END -* Test harness transaction: COMMIT test harness results *-;

IF errors RAISE exception

 */
BEGIN;

--
-- 1. Create test harness tables: rif40_test_runs, rif40_test_harness and related sequences
--
DROP TABLE IF EXISTS rif40_test_runs CASCADE;
DROP SEQUENCE IF EXISTS rif40_test_run_id_seq;

CREATE SEQUENCE rif40_test_run_id_seq;
COMMENT ON SEQUENCE rif40_test_run_id_seq IS 'Artificial primary key for: RIF40_TEST_RUNS';
GRANT SELECT, USAGE ON SEQUENCE rif40_test_id_seq TO rif40, rif_manager, notarifuser;
GRANT SELECT, USAGE ON SEQUENCE rif40_test_run_id_seq TO rif40, rif_manager, notarifuser;

CREATE TABLE rif40_test_runs (
	test_run_id 					INTEGER NOT NULL 					DEFAULT (nextval('rif40_test_run_id_seq'::regclass))::integer, 
	test_run_title					VARCHAR NOT NULL,	
	test_date						TIMESTAMP WITH TIME ZONE NOT NULL 	DEFAULT "statement_timestamp"(),
	time_taken						NUMERIC	NOT NULL 					DEFAULT 0,
	username 						VARCHAR(90) NOT NULL 				DEFAULT "current_user"(),
	tests_run						INTEGER NOT NULL 					DEFAULT 0,
	number_passed					INTEGER NOT NULL 					DEFAULT 0,
	number_failed					INTEGER NOT NULL 					DEFAULT 0,
	number_test_cases_registered	INTEGER NOT NULL 					DEFAULT 0,
	number_messages_registered		INTEGER NOT NULL 					DEFAULT 0,
	CONSTRAINT rif40_test_runs_pk PRIMARY KEY (test_run_id)
	);

GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_test_runs TO rif_manager, notarifuser;
GRANT SELECT ON TABLE rif40_test_runs TO rif_user;
	
COMMENT ON TABLE rif40_test_runs IS 'Test harness test run information';		
COMMENT ON COLUMN rif40_test_runs.test_run_id IS 'Unique investigation index: test_run_id. Created by SEQUENCE rif40_test_run_id_seq';
COMMENT ON COLUMN rif40_test_runs.test_run_title IS 'Test run title';
COMMENT ON COLUMN rif40_test_runs.test_date IS 'Test date';
COMMENT ON COLUMN rif40_test_runs.time_taken IS 'Time taken for test run (seconds)';
COMMENT ON COLUMN rif40_test_runs.username IS 'user name running test run';
COMMENT ON COLUMN rif40_test_runs.tests_run IS 'Number of tests run (should equal passed+failed!)';
COMMENT ON COLUMN rif40_test_runs.number_passed IS 'Number of tests passed';
COMMENT ON COLUMN rif40_test_runs.number_failed IS 'Number of tests failed';
COMMENT ON COLUMN rif40_test_runs.number_test_cases_registered IS 'Number of test cases registered';
COMMENT ON COLUMN rif40_test_runs.number_messages_registered IS 'Number of error and informational messages registered';

DROP TABLE IF EXISTS rif40_test_harness_old;
DO LANGUAGE plpgsql $$
DECLARE
	c1_a8 CURSOR FOR
		SELECT table_name 
		  FROM information_schema.tables 
		 WHERE table_name  = 'rif40_test_harness';
	c1_rec RECORD;
BEGIN
    PERFORM rif40_log_pkg.rif40_log_setup();
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_ddl:DEBUG1');
	PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Check if column exists
--
	OPEN c1_a8;
	FETCH c1_a8 INTO c1_rec;
	CLOSE c1_a8;
	IF c1_rec.table_name = 'rif40_test_harness' THEN
--
-- Re-create; remove constraints and indexes
--
		PERFORM rif40_sql_pkg.rif40_ddl('ALTER TABLE rif40_test_harness RENAME TO rif40_test_harness_old');
		PERFORM rif40_sql_pkg.rif40_ddl('ALTER TABLE rif40_test_harness_old DROP CONSTRAINT IF EXISTS rif40_test_harness_parent_test_id_fk');
		PERFORM rif40_sql_pkg.rif40_ddl('ALTER TABLE rif40_test_harness_old DROP CONSTRAINT IF EXISTS rif40_test_harness_test_run_id_fk');
		PERFORM rif40_sql_pkg.rif40_ddl('ALTER TABLE rif40_test_harness_old DROP CONSTRAINT IF EXISTS rif40_test_harness_pk');
		PERFORM rif40_sql_pkg.rif40_ddl('DROP INDEX IF EXISTS rif40_test_harness_uk');
	ELSE
--
-- Does not exist - can anyway
--
		PERFORM rif40_sql_pkg.rif40_ddl('DROP TABLE IF EXISTS rif40_test_harness');
		PERFORM rif40_sql_pkg.rif40_ddl('DROP SEQUENCE IF EXISTS rif40_test_id_seq'); 
		PERFORM rif40_sql_pkg.rif40_ddl('CREATE SEQUENCE rif40_test_id_seq'); 
		PERFORM rif40_sql_pkg.rif40_ddl('COMMENT ON SEQUENCE rif40_test_id_seq IS ''Artificial primary key for: RIF40_TEST_HARNESS''');
	END IF;
END;
$$;
		
CREATE TABLE rif40_test_harness (
	test_id 					INTEGER NOT NULL DEFAULT (nextval('rif40_test_id_seq'::regclass))::integer, 
	parent_test_id 				INTEGER, 		
	test_run_class				VARCHAR NOT NULL,	
	test_stmt					VARCHAR NOT NULL,
	test_case_title				VARCHAR NOT NULL,
	pg_error_code_expected		VARCHAR DEFAULT NULL,
	mssql_error_code_expected	VARCHAR DEFAULT NULL,
	raise_exception_on_failure 	BOOLEAN NOT NULL DEFAULT TRUE,
	expected_result			 	BOOLEAN NOT NULL DEFAULT TRUE,
	register_date				TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT "statement_timestamp"(),
	results 					Text[][],
	results_xml 				XML,	
	pass 						BOOLEAN,
	test_run_id					INTEGER,
	test_date					TIMESTAMP WITH TIME ZONE,
	time_taken					NUMERIC,
	pg_debug_functions			Text[],
	CONSTRAINT rif40_test_harness_pk PRIMARY KEY (test_id),
	CONSTRAINT rif40_test_harness_parent_test_id_fk FOREIGN KEY (parent_test_id)
		REFERENCES rif40_test_harness (test_id),
	CONSTRAINT rif40_test_harness_test_run_id_fk FOREIGN KEY (test_run_id)
		REFERENCES rif40_test_runs (test_run_id)
	);
DO LANGUAGE plpgsql $$
DECLARE
	c1_a8 CURSOR FOR
		SELECT table_name 
		  FROM information_schema.tables 
		 WHERE table_name  = 'rif40_test_harness_old';
	c1_rec RECORD;
BEGIN
--
-- Check if column exists
--
	OPEN c1_a8;
	FETCH c1_a8 INTO c1_rec;
	CLOSE c1_a8;
	IF c1_rec.table_name = 'rif40_test_harness_old' THEN
		PERFORM rif40_sql_pkg.rif40_ddl('INSERT INTO rif40_test_harness(test_id, parent_test_id, test_run_class, test_stmt, test_case_title, pg_error_code_expected,
						mssql_error_code_expected, raise_exception_on_failure, expected_result, register_date, results,
						results_xml, pass, test_run_id, test_date, time_taken, pg_debug_functions)
		SELECT test_id, parent_test_id, test_run_class, test_stmt, test_case_title, pg_error_code_expected,
			   mssql_error_code_expected, raise_exception_on_failure, expected_result, register_date, results,
	   		   results_xml, pass, test_run_id, test_date, time_taken, pg_debug_functions
		  FROM rif40_test_harness_old
		 ORDER BY test_id');
		PERFORM rif40_sql_pkg.rif40_ddl('DROP TABLE IF EXISTS rif40_test_harness_old'); 
	END IF;
END;
$$;

--
-- Multiple inheritance of test cases is not permitted!
--
CREATE UNIQUE INDEX rif40_test_harness_uk ON rif40_test_harness(parent_test_id);

GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_test_harness TO rif_manager, notarifuser;
GRANT SELECT ON TABLE rif40_test_harness TO rif_user; 
		
COMMENT ON TABLE rif40_test_harness IS 'Test harness test cases and last run information';		
COMMENT ON COLUMN rif40_test_harness.test_id IS 'Unique investigation index: test_id. Created by SEQUENCE rif40_test_id_seq';
COMMENT ON COLUMN rif40_test_harness.parent_test_id IS 'Parent test ID; NULL means first (test statement). Allows for a string of connected test cases. Multiple inheritance of test cases is not permitted!';
COMMENT ON COLUMN rif40_test_harness.test_run_class IS 'Test run class; usually the name of the SQL script that originally ran it';
COMMENT ON COLUMN rif40_test_harness.test_stmt IS 'SQL statement for test';
COMMENT ON COLUMN rif40_test_harness.test_case_title IS 'Test case title. Must be unique';
COMMENT ON COLUMN rif40_test_harness.pg_error_code_expected IS '[negative] Postgres error SQLSTATE expected [as part of an exception]; passed as PG_EXCEPTION_DETAIL';
COMMENT ON COLUMN rif40_test_harness.mssql_error_code_expected IS 'Microsoft SQL server error code expected [as part of an exception].';
COMMENT ON COLUMN rif40_test_harness.raise_exception_on_failure IS 'Raise exception on failure. NULL means it is expected to NOT raise an exception, raise exception on failure';
COMMENT ON COLUMN rif40_test_harness.expected_result IS 'Expected result; tests are allowed to deliberately fail! If the test raises the expection pg_error_code_expected it would normally be expected to pass.';
COMMENT ON COLUMN rif40_test_harness.register_date IS 'Date registered';
COMMENT ON COLUMN rif40_test_harness.results IS 'Results array';
COMMENT ON COLUMN rif40_test_harness.results_xml IS 'Results array in portable XML';
COMMENT ON COLUMN rif40_test_harness.pass IS 'Was the test passed? Note that some tests do fail deliberately to test the harness';
COMMENT ON COLUMN rif40_test_harness.test_run_id IS 'Test run id for test. Foreign key to rif40_test_runs table.';
COMMENT ON COLUMN rif40_test_harness.test_date IS 'Test date';
COMMENT ON COLUMN rif40_test_harness.time_taken IS 'Time taken for test (seconds)';
COMMENT ON COLUMN rif40_test_harness.pg_debug_functions IS 'Array of Postgres functions for test harness to enable debug on';

--
-- 2. Add "Test harness" to RIF40_TABLES_AND_VIEWS classes
--
ALTER TABLE rif40_tables_and_views
      DROP CONSTRAINT class_ck; 
ALTER TABLE rif40_tables_and_views
      ADD CONSTRAINT class_ck 
	  CHECK (class::text = ANY (ARRAY[
			'Test harness'::character varying, 
			'Configuration'::character varying, 
			'Documentation'::character varying, 
			'Lookup'::character varying, 
			'Other'::character varying, 
			'Results'::character varying, 
			'SQL Generator'::character varying, 
			'Study Setup'::character varying]::text[]));

--
-- 3. Add to RIF40_TABLES_AND_VIEWS, RIF40_COLUMNS
--
DELETE FROM rif40_columns WHERE table_or_view_name_hide IN ('RIF40_TEST_RUNS', 'RIF40_TEST_HARNESS');  
DELETE FROM rif40_tables_and_views WHERE table_or_view_name_hide IN ('RIF40_TEST_RUNS', 'RIF40_TEST_HARNESS');
    
INSERT INTO rif40_tables_and_views (class, table_or_view, comments, table_or_view_name_hide)
SELECT 'Test harness' AS class, 'TABLE' AS table_or_view, 
       obj_description(b.oid) AS comments, UPPER(b.relname) AS table_or_view_name_hide
  FROM pg_class b
 WHERE b.relname = 'rif40_test_runs'
   AND b.relkind = 'r'
   AND NOT EXISTS (SELECT 1 FROM rif40_tables_and_views WHERE table_or_view_name_hide IN ('RIF40_TEST_RUNS'));  
INSERT INTO rif40_tables_and_views (class, table_or_view, comments, table_or_view_name_hide)
SELECT 'Test harness' AS class, 'TABLE' AS table_or_view, 
       obj_description(b.oid) AS comments, UPPER(b.relname) AS table_or_view_name_hide
  FROM pg_class b
 WHERE b.relname = 'rif40_test_harness'
   AND b.relkind = 'r'
   AND NOT EXISTS (SELECT 1 FROM rif40_tables_and_views WHERE table_or_view_name_hide IN ('RIF40_TEST_HARNESS'));  
   
INSERT INTO rif40_columns (table_or_view_name_hide, column_name_hide, nullable, oracle_data_type, comments)   		 
SELECT UPPER(b.relname) AS table_or_view_name_hide, UPPER(d.attname) AS column_name_hide, 
      CASE WHEN d.attnotnull THEN 'NOT NULL' ELSE 'NULL' END AS nullable, t.typname AS oracle_data_type,
       col_description(b.oid, d.attnum) AS comments
  FROM pg_class b, pg_attribute d, pg_type t
 WHERE b.relname::regclass = d.attrelid
   AND d.atttypid = t.oid
   AND b.relname IN ('rif40_test_runs', 'rif40_test_harness')
   AND b.relkind = 'r'
   AND col_description(b.oid, d.attnum) IS NOT NULL
   AND d.attname NOT IN (
		SELECT LOWER(column_name_hide) AS column_name
 		  FROM rif40_columns
		 WHERE table_or_view_name_hide IN ('RIF40_TEST_RUNS', 'RIF40_TEST_HARNESS')); 
		 
--
-- Reload triggers (including fixes for test harness)
--
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_studies_checks.sql

--
-- Reload log package
--
\i ../PLpgsql/v4_0_rif40_log_pkg.sql

--
-- Load test harness
--
\i ../PLpgsql/rif40_sql_pkg/rif40_sql_test.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_test_harness.sql

--
-- Reload rif40_create_disease_mapping_example
--
\i ../PLpgsql/rif40_sm_pkg/rif40_create_disease_mapping_example.sql

--
-- Load tests - for all rif40 schema tables
--
-- Constraints:
--
-- a) NOT null
-- b) Check i) correct; ii) incorrect
-- c) Primary key i) correct; ii) duplicate iii) missing parent
--
-- Access control:
--
-- d) notarifuser access
--
-- Business logic:
--
-- e) Triggers
--
-- Testing stop
--
--DO LANGUAGE plpgsql $$
--BEGIN
--	RAISE EXCEPTION 'Stop processing';
--END;
--$$;

SELECT COUNT(test_id) AS total_tests
  FROM rif40_test_harness;
  
END;
--
--  Eof 