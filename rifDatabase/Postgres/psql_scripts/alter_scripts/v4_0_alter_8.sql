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

Alter script #8:

* Create test harness tables: rif40_test_runs, rif40_test_harness

 */
BEGIN;

DROP SEQUENCE IF EXISTS rif40_test_id_seq; 
DROP SEQUENCE IF EXISTS rif40_test_run_id_seq;
DROP TABLE IF EXISTS rif40_test_harness;
DROP TABLE IF EXISTS rif40_test_runs;

CREATE SEQUENCE rif40_test_id_seq; 
CREATE SEQUENCE rif40_test_run_id_seq;

CREATE TABLE rif40_test_runs (
	test_run_id 					INTEGER NOT NULL DEFAULT (nextval('rif40_test_run_id_seq'::regclass))::integer, 
	test_run_title					VARCHAR NOT NULL,	
	test_date						TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT "statement_timestamp"(),
	time_taken						NUMERIC	NOT NULL,
	username 						VARCHAR(90) NOT NULL DEFAULT "current_user"(),
	number_passed					INTEGER NOT NULL,
	number_failed					INTEGER NOT NULL,
	number_test_cases_registered	INTEGER NOT NULL DEFAULT 0,
	number_messages_registered		INTEGER NOT NULL DEFAULT 0,
	CONSTRAINT rif40_test_runs_pk PRIMARY KEY (test_run_id)
	);

GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_test_runs TO rif_manager, notarifuser;
	
COMMENT ON TABLE rif40_test_runs IS 'Test harness test run information';		
COMMENT ON COLUMN rif40_test_runs.test_run_id IS 'Unique investigation index: test_run_id. Created by SEQUENCE rif40_test_run_id_seq';
COMMENT ON COLUMN rif40_test_runs.test_run_title IS 'Test run title';
COMMENT ON COLUMN rif40_test_runs.test_date IS 'Test date';
COMMENT ON COLUMN rif40_test_runs.time_taken IS 'Time taken for test run (seconds)';
COMMENT ON COLUMN rif40_test_runs.username IS 'user name running test run';
COMMENT ON COLUMN rif40_test_runs.number_passed IS 'Number of tests passed';
COMMENT ON COLUMN rif40_test_runs.number_failed IS 'Number of tests failed';
COMMENT ON COLUMN rif40_test_runs.number_test_cases_registered IS 'Number of test cases registered';
COMMENT ON COLUMN rif40_test_runs.number_messages_registered IS 'Number of error and informational messages registered';
	
CREATE TABLE rif40_test_harness (
	test_id 					INTEGER NOT NULL DEFAULT (nextval('rif40_test_id_seq'::regclass))::integer, 
	test_stmt					VARCHAR NOT NULL,
	test_case_title				VARCHAR NOT NULL,
	error_code_expected			VARCHAR DEFAULT NULL,
	raise_exception_on_failure 	BOOLEAN NOT NULL DEFAULT TRUE,
	register_date				TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT "statement_timestamp"(),
	results 					Text[][],
	results_xml 				XML,	
	pass 						BOOLEAN,
	test_run_id					INTEGER,
	test_date					TIMESTAMP WITH TIME ZONE,
	time_taken					NUMERIC,
	CONSTRAINT rif40_test_harness_pk PRIMARY KEY (test_id),
	CONSTRAINT rif40_test_harness_test_run_id_fk FOREIGN KEY (test_run_id)
		REFERENCES rif40_test_runs (test_run_id)
	);
CREATE UNIQUE INDEX rif40_test_harness_uk ON rif40_test_harness(test_case_title);

GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_test_harness TO rif_manager, notarifuser;
		
COMMENT ON TABLE rif40_test_harness IS 'Test harness test cases and last run information';		
COMMENT ON COLUMN rif40_test_harness.test_id IS 'Unique investigation index: test_id. Created by SEQUENCE rif40_test_id_seq';
COMMENT ON COLUMN rif40_test_harness.test_stmt IS 'SQL statement for test';
COMMENT ON COLUMN rif40_test_harness.test_case_title IS 'Test case title. Must be unique';
COMMENT ON COLUMN rif40_test_harness.error_code_expected IS '[negative] error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number';
COMMENT ON COLUMN rif40_test_harness.raise_exception_on_failure IS 'Raise exception on failure. NULL means it is expected to NOT raise an exception, raise exception on failure';
COMMENT ON COLUMN rif40_test_harness.register_date IS 'Date registered';
COMMENT ON COLUMN rif40_test_harness.results IS 'Results array';
COMMENT ON COLUMN rif40_test_harness.results_xml IS 'Results array in portable XML';
COMMENT ON COLUMN rif40_test_harness.pass IS 'Was the test passed?';
COMMENT ON COLUMN rif40_test_harness.test_run_id IS 'Test run id for test. Foreign key to rif40_test_runs table.';
COMMENT ON COLUMN rif40_test_harness.test_date IS 'Test date';
COMMENT ON COLUMN rif40_test_harness.time_taken IS 'Time taken for test (seconds)';

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

--
-- Testing stop
--
--DO LANGUAGE plpgsql $$
--BEGIN
--	RAISE EXCEPTION 'Stop processing';
--END;
--$$;

END;
--
--  Eof 