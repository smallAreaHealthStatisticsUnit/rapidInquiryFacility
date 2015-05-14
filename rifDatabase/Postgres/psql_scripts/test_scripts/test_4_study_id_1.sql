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
-- Rapid Enquiry Facility (RIF) - Test 4: Study ID 1
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

--
-- Call commomn setup to run as testuser (passed parameter)
--
\i ../psql_scripts/test_scripts/common_setup.sql

\echo test 4: Creating SAHSULAND example study 1...

\set ndebug_level '''XXXX':debug_level''''
SET rif40.debug_level TO :ndebug_level;
--
-- Start transaction
--
BEGIN;
DO LANGUAGE plpgsql $$
DECLARE
	c1sm CURSOR FOR /* Get list of study_id tables with trigger functions */
		WITH a AS (
			SELECT DISTINCT table_name
			  FROM information_schema.columns
			 WHERE column_name = 'study_id'
			   AND table_name NOT LIKE 'g_rif40%'
			   AND table_name IN (
				SELECT table_name
				  FROM information_schema.tables
			 	 WHERE table_schema = 'rif40'
				   AND table_type = 'BASE TABLE')
		)
		SELECT TRANSLATE(SUBSTR(action_statement, STRPOS(action_statement, '.')+1), '()', '') AS function,
		       a.table_name, action_timing, COUNT(trigger_name) AS t
		  FROM a 
			LEFT OUTER JOIN information_schema.triggers b ON (
		  		trigger_schema = 'rif40'
		  	    AND action_timing IN ('BEFORE', 'AFTER') 
			    AND event_object_table = a.table_name)
		 GROUP BY TRANSLATE(SUBSTR(action_statement, STRPOS(action_statement, '.')+1), '()', ''),
		       a.table_name, action_timing
		 ORDER BY 1 DESC, 3;
	c2sm CURSOR FOR 
		SELECT array_agg(level3) AS level3_array FROM sahsuland_level3;
	c3sm CURSOR FOR /* Old studies to delete - not study 1 */
		SELECT study_id
		  FROM rif40_studies
		 WHERE study_name = 'SAHSULAND test 4 study_id 1 example'
		   AND username   = USER
		   AND study_id > 1;
	c4sm CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c1sm_rec RECORD;
	c2sm_rec RECORD;
	c3sm_rec RECORD;
	c4sm_rec RECORD;
--
	investigation_icd_array		VARCHAR[]:=array['"icd" LIKE ''C34%'' OR "icd" LIKE ''162%'''];	
	investigation_desc_array	VARCHAR[]:=array['Lung cancer'];
	covariate_array				VARCHAR[]:=array['SES'];	
--
	rif40_sm_pkg_functions 		VARCHAR[] := ARRAY['rif40_verify_state_change', 
						'rif40_run_study', 'rif40_ddl', 'rif40_study_ddl_definer', 
						'rif40_create_insert_statement', 'rif40_execute_insert_statement', 
						'rif40_compute_results', 'rif40_startup'];
--
	l_function 		VARCHAR;
	i				INTEGER:=0;
--
	sql_stmt		VARCHAR[];
	debug_level		INTEGER;
	study_ran_ok	BOOLEAN;
BEGIN
	OPEN c4sm;
	FETCH c4sm INTO c4sm_rec;
	CLOSE c4sm;
--
-- Test parameter
--
	IF c4sm_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'test_4_study_id_1.sql: T4--01: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'test_4_study_id_1.sql: T4--02: debug level parameter="%"', debug_level::Text;
	END IF;
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_4_study_id_1.sql: T4--03: Invslid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);

		FOR c1sm_rec IN c1sm LOOP
			IF c1sm_rec.function IS NOT NULL THEN
				RAISE INFO 'test_4_study_id_1.sql: T4--04: Enable debug for % trigger function: % on table: %',
					c1sm_rec.action_timing, c1sm_rec.function, c1sm_rec.table_name;
					PERFORM rif40_log_pkg.rif40_add_to_debug(c1sm_rec.function||':DEBUG'||debug_level::Text);
			ELSE
				RAISE WARNING 'test_4_study_id_1.sql: T4--05: No trigger function found for table: %', c1sm_rec.table_name;
			END IF;
		END LOOP;
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_sm_pkg_functions LOOP
			RAISE INFO 'test_4_study_id_1.sql: T4--06: Enable debug for function: %', l_function;
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG'||debug_level::Text);
		END LOOP;
	END IF;
--
-- Call init function is case called from main build scripts
--
	PERFORM rif40_sql_pkg.rif40_startup();
--
-- Get "SELECTED" study geolevels
--
	OPEN c2sm;
	FETCH c2sm INTO c2sm_rec;
	CLOSE c2sm;
--
--
-- Delete old test studies
--
	FOR c3sm_rec IN c3sm LOOP
		i:=i+1;
		RAISE INFO 'test_4_study_id_1.sql: T4--07: Delete of SAHSULAND test example study: %', c3sm_rec.study_id::VARCHAR;
		PERFORM rif40_sm_pkg.rif40_delete_study(c3sm_rec.study_id);
	END LOOP;
--
-- Create new test study
--
	PERFORM rif40_sm_pkg.rif40_create_disease_mapping_example(
		'SAHSU'::VARCHAR 		/* Geography */,
		'LEVEL1'::VARCHAR		/* Geolevel view */,
		'01'::VARCHAR			/* Geolevel area */,
		'LEVEL4'::VARCHAR		/* Geolevel map */,
		'LEVEL3'::VARCHAR		/* Geolevel select */,
		c2sm_rec.level3_array 	/* Geolevel selection array */,
		'TEST'::VARCHAR 		/* project */, 
		'SAHSULAND test 4 study_id 1 example'::VARCHAR /* study name */, 
		'SAHSULAND_POP'::VARCHAR 	/* denominator table */, 
		'SAHSULAND_CANCER'::VARCHAR 	/* numerator table */,
 		1989				/* year_start */, 
		1996				/* year_stop */,
		investigation_icd_array 	/* investigation ICD  condition array */,
		investigation_desc_array 	/* investigation description array */,
		covariate_array			/* covariate array */);
--
-- Dump extract/map tables to CSV via a temporary table
-- Ordering is for githubs benefit
--
	sql_stmt[1]:='DROP TABLE IF EXISTS test_4_study_id_1_extract';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE IF EXISTS test_4_study_id_1_map';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE IF EXISTS test_4_study_id_1_bands';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE TEMPORARY TABLE test_4_study_id_1_extract'||E'\n'||
'AS'||E'\n'||
'SELECT *'||E'\n'||
'  FROM rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_extract'||
' ORDER BY study_id, study_or_comparison, year, band_id, area_id, sex, age_group, '||array_to_string(covariate_array, ',');
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE TEMPORARY TABLE test_4_study_id_1_map'||E'\n'||
'AS'||E'\n'||
'SELECT *'||E'\n'||
'  FROM rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_map'||
' ORDER BY gid_rowindex';
--' ORDER BY study_id, band_id, inv_id, genders, adjusted, direct_standardisation, gid_rowindex';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE TEMPORARY TABLE test_4_study_id_1_bands'||E'\n'||
'AS'||E'\n'||
'SELECT *'||E'\n'||
'  FROM rif40_study_areas'||E'\n'||
' WHERE study_id = '||currval('rif40_study_id_seq'::regclass)::VARCHAR||E'\n'||
' ORDER BY band_id';
--
-- Set study_id, inv_id to 1 for GITHUBs benefit
--
-- test_4_study_id_1_extract: when inv_1 is changed to inv_<inv_id> the column will need to be renamed
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='UPDATE test_4_study_id_1_extract SET study_id = 1';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='UPDATE test_4_study_id_1_map SET study_id = 1, inv_id = 1';
	sql_stmt[array_length(sql_stmt, 1)+1]:='UPDATE test_4_study_id_1_bands SET study_id = 1';
--
-- Execute in USER context
--
	EXECUTE 'SELECT '||USER||'.rif40_run_study(currval(''rif40_study_id_seq''::regclass)::INTEGER)' INTO study_ran_ok;
	IF study_ran_ok THEN
		RAISE INFO 'test_4_study_id_1.sql: T4--08: Test 4; Study: % run OK', currval('rif40_study_id_seq'::regclass)::VARCHAR;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	ELSE
		RAISE EXCEPTION 'test_4_study_id_1.sql: T4--09: Test 4; Study: % run failed; see trace', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	END IF;
END;
$$;

/*
SELECT * FROM rif40_comparison_areas WHERE study_id = currval('rif40_study_id_seq'::regclass) LIMIT 20;
SELECT * FROM rif40_study_areas WHERE study_id = currval('rif40_study_id_seq'::regclass) LIMIT 20;
SELECT * FROM rif40_inv_covariates WHERE study_id = currval('rif40_study_id_seq'::regclass) LIMIT 20;
SELECT * FROM rif40_study_sql_log WHERE study_id = currval('rif40_study_id_seq'::regclass) LIMIT 20;

WITH a AS (
	SELECT rif40_log_pkg.rif40_get_debug(CURRENT_SETTING('rif40.debug')) AS debug /-	Current debug -/
)
SELECT (a.debug).function_name AS function_name,
       (a.debug).debug AS debug,
       SUBSTR((a.debug).debug::TEXT, 6)::INTEGER AS debug_level
  FROM a;
 */
 
 /*
\dS rif40_num_denom
\dS+ test_4_study_id_1_extract
\dS+ test_4_study_id_1_map
\dS+ test_4_study_id_1_bands
 */
--
-- psql:../psql_scripts/v4_0_test_4_study_id_1s.sql:229: WARNING:  rif40_ddl(): SQL in error (23505)> ALTER TABLE rif_studies.s6_map ADD CONSTRAINT s6_map_pk PRIMARY KEY (study_id, band_id, inv_id, genders, adjusted, direct_standardisation);
-- psql:../psql_scripts/v4_0_test_4_study_id_1s.sql:229: WARNING:  rif40_study_ddl_definer(): [90628] Study 6: statement 80 error: 23505 "could not create unique index "s6_map_pk"" raised by
-- Now OK
/*
SELECT COUNT(gid_rowindex) AS total
  FROM test_4_study_id_1_map;
SELECT study_id, band_id, inv_id, genders, adjusted, direct_standardisation, COUNT(gid_rowindex) AS total
  FROM test_4_study_id_1_map
 GROUP BY study_id, band_id, inv_id, genders, adjusted, direct_standardisation
 HAVING COUNT(gid_rowindex) > 1
 ORDER BY study_id, band_id, inv_id, genders, adjusted, direct_standardisation LIMIT 20;
SELECT study_id, band_id, inv_id, genders, adjusted, direct_standardisation, gid_rowindex
  FROM test_4_study_id_1_map
 ORDER BY study_id, band_id, inv_id, genders, adjusted, direct_standardisation, gid_rowindex LIMIT 20;
SELECT study_id, band_id, inv_id, genders, adjusted, direct_standardisation
  FROM rif40_results
 WHERE study_id = currval('rif40_study_id_seq'::regclass) 
 ORDER BY study_id, band_id, inv_id, genders, adjusted, direct_standardisation LIMIT 20;
 */
-- OK
/*
SELECT area_id, COUNT(band_id) AS total
  FROM test_4_study_id_1_bands
 GROUP BY area_id
 HAVING COUNT(band_id) > 1
 ORDER BY 1 LIMIT 20;
SELECT band_id, COUNT(area_id) AS total
  FROM test_4_study_id_1_bands
 GROUP BY band_id
 HAVING COUNT(area_id) > 1
 ORDER BY 1 LIMIT 20;
 */
 
--
-- Bug: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/8
--
-- Should cover the whole of level4
--
-- Here are the missing areas in sahsuland_example_map.csv:
--
-- missing
-- [1] "01.008.003901.1" "01.008.003901.2" "01.008.003901.3" "01.008.003901.4" "01.008.003901.5"
-- [6] "01.008.003901.6" "01.008.003901.7" "01.008.003901.9" "01.008.009401.1" "01.008.009401.2"
-- [11] "01.008.009401.3" "01.008.009401.4"
--
-- This is NOT the fault of the extract
--
/*
\pset title 'Missing area_ids'
SELECT level4 FROM sahsuland_level4
EXCEPT
SELECT area_id
  FROM test_4_study_id_1_extract
 WHERE study_or_comparison = 'S'
ORDER BY 1;
\pset title 'Extra area_ids'
SELECT area_id AS level4
  FROM test_4_study_id_1_extract
 WHERE study_or_comparison = 'S'
EXCEPT
SELECT level4 FROM sahsuland_level4
ORDER BY 1;
\pset title 'sahsuland_level4 total'
SELECT COUNT(level4) AS sahsuland_level4_total 
  FROM sahsuland_level4;
\pset title 'test_4_study_id_1_extract total'
SELECT COUNT(DISTINCT(area_id)) AS test_4_study_id_1_extract_area_id
  FROM test_4_study_id_1_extract
 WHERE study_or_comparison = 'S';
\pset title
 */
--
-- Check all level4 area_ids presents in: 
--
-- v_test_4_study_id_1_extract, v_test_4_study_id_1_map, v_test_4_study_id_1_bands
-- rif40_results WHERE study_id = currval('rif40_study_id_seq'::regclass)
--
--\set VERBOSITY verbose
\echo Area IDs comparision...
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR /* test_4_study_id_1_extract diffs */
		WITH a AS ( /* Missing */
			SELECT level4 FROM sahsuland_level4
			EXCEPT
			SELECT area_id
  			  FROM test_4_study_id_1_extract
			 WHERE study_or_comparison = 'S'
		), b AS ( /* Extra */
			SELECT area_id AS level4
  			  FROM test_4_study_id_1_extract
			 WHERE study_or_comparison = 'S'
			EXCEPT
			SELECT level4 FROM sahsuland_level4
		), a1 AS (
			SELECT COUNT(level4) AS missing_level4
			  FROM a
		), b1 AS (
			SELECT COUNT(level4) AS extra_level4
			  FROM b
		)
		SELECT missing_level4, extra_level4
		  FROM a1, b1;
	c2 CURSOR FOR /* test_4_study_id_1_map diffs */
		WITH a AS ( /* Missing */
			SELECT level4 FROM sahsuland_level4
			EXCEPT
			SELECT area_id FROM test_4_study_id_1_map
		), b AS ( /* Extra */
			SELECT area_id AS level4 FROM test_4_study_id_1_map
			EXCEPT
			SELECT level4 FROM sahsuland_level4
		), a1 AS (
			SELECT COUNT(level4) AS missing_level4
			  FROM a
		), b1 AS (
			SELECT COUNT(level4) AS extra_level4
			  FROM b
		)
		SELECT missing_level4, extra_level4
		  FROM a1, b1;
	c3 CURSOR FOR /* test_4_study_id_1_bands diffs */
		WITH a AS ( /* Missing */
			SELECT level4 FROM sahsuland_level4
			EXCEPT
			SELECT area_id FROM test_4_study_id_1_bands
		), b AS ( /* Extra */
			SELECT area_id AS level4 FROM test_4_study_id_1_bands
			EXCEPT
			SELECT level4 FROM sahsuland_level4
		), a1 AS (
			SELECT COUNT(level4) AS missing_level4
			  FROM a
		), b1 AS (
			SELECT COUNT(level4) AS extra_level4
			  FROM b
		)
		SELECT missing_level4, extra_level4
		  FROM a1, b1;
	c4 CURSOR FOR /* rif40_results WHERE study_id = currval('rif40_study_id_seq'::regclass) diffs */
		WITH a AS ( /* Missing */
			SELECT level4 FROM sahsuland_level4
			EXCEPT
			SELECT b.area_id
  			  FROM rif40_results a, rif40_study_areas b
			 WHERE a.study_id = currval('rif40_study_id_seq'::regclass)
			   AND a.study_id = b.study_id
			   AND a.band_id  = b.band_id
		), b AS ( /* Extra */
			SELECT b.area_id AS level4
  			  FROM rif40_results a, rif40_study_areas b
			 WHERE a.study_id = currval('rif40_study_id_seq'::regclass)
			   AND a.study_id = b.study_id
			   AND a.band_id  = b.band_id
			EXCEPT
			SELECT level4 FROM sahsuland_level4
		), a1 AS (
			SELECT COUNT(level4) AS missing_level4
			  FROM a
		), b1 AS (
			SELECT COUNT(level4) AS extra_level4
			  FROM b
		)
		SELECT missing_level4, extra_level4
		  FROM a1, b1;
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
--
	errors INTEGER:=0;
BEGIN
--
-- test_4_study_id_1_extract diffs
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.missing_level4 = 0 AND c1_rec.extra_level4 = 0 THEN
		RAISE INFO 'test_4_study_id_1.sql: T4--10: Test 4.1; Study: % study area all present in test_4_study_id_1_extract', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql: T4--11: Test 4.1; Study: % study area not all present in test_4_study_id_1_extract; % missing, % extra', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			c1_rec.missing_level4::VARCHAR,
			c1_rec.extra_level4::VARCHAR;
		errors:=errors+c1_rec.missing_level4+c1_rec.extra_level4;
	END IF;
--
-- test_4_study_id_1_map diffs
--
	OPEN c2;
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.missing_level4 = 0 AND c2_rec.extra_level4 = 0 THEN
		RAISE INFO 'test_4_study_id_1.sql: T4--12: Test 4.2; Study: % study area all present in test_4_study_id_1_map', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql: T4--13: Test 4.2; Study: % study area not all present in test_4_study_id_1_map; % missing, % extra', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			c2_rec.missing_level4::VARCHAR,
			c2_rec.extra_level4::VARCHAR;
		errors:=errors+c2_rec.missing_level4+c2_rec.extra_level4;
	END IF;
--
-- test_4_study_id_1_bands diffs
--
	OPEN c3;
	FETCH c3 INTO c3_rec;
	CLOSE c3;
	IF c3_rec.missing_level4 = 0 AND c3_rec.extra_level4 = 0 THEN
		RAISE INFO 'test_4_study_id_1.sql: T4--14: Test 4.3; Study: % study area all present in test_4_study_id_1_bands', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql: T4--15: Test 4.3; Study: % study area not all present in test_4_study_id_1_bands; % missing, % extra', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			c3_rec.missing_level4::VARCHAR,
			c3_rec.extra_level4::VARCHAR;
		errors:=errors+c3_rec.missing_level4+c3_rec.extra_level4;
	END IF;
--
-- rif40_results WHERE study_id = currval('rif40_study_id_seq'::regclass) diffs
--
	OPEN c4;
	FETCH c4 INTO c4_rec;
	CLOSE c4;
	IF c4_rec.missing_level4 = 0 AND c4_rec.extra_level4 = 0 THEN
		RAISE INFO 'test_4_study_id_1.sql: T4--16: Test 4.4; Study: % study area all present in rif40_results', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql: T4--17: Test 4.4; Study: % study area not all present in rif40_results; % missing, % extra', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			c4_rec.missing_level4::VARCHAR,
			c4_rec.extra_level4::VARCHAR;
		errors:=errors+c4_rec.missing_level4+c4_rec.extra_level4;
	END IF;
-- 
	IF errors > 0 THEN
		RAISE EXCEPTION	'test_4_study_id_1.sql: T4--18:  Test 4.1-4; Study: % % missing/extra level4 errors', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			errors::VARCHAR;
	END IF;
END;
$$;

--
-- Reference test data (usually comment out unless it needs to be updated)
-- 
--\copy ( SELECT * FROM test_4_study_id_1_extract ORDER BY year, study_or_comparison, study_id, area_id, band_id, sex, age_group) to ../example_data/test_4_study_id_1_extract.csv WITH CSV HEADER
--\copy ( SELECT study_id, inv_id, band_id, area_id, gid, genders, direct_standardisation, adjusted, observed, expected, lower95, upper95, relative_risk, smoothed_relative_risk, posterior_probability, posterior_probability_upper95, posterior_probability_lower95, residual_relative_risk, residual_rr_lower95, residual_rr_upper95, smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95 FROM test_4_study_id_1_map ORDER BY gid_rowindex) to ../example_data/test_4_study_id_1_map.csv WITH CSV HEADER
--\copy ( SELECT study_id,area_id,band_id FROM test_4_study_id_1_bands ORDER BY study_id, area_id, band_id) to ../example_data/test_4_study_id_1_bands.csv WITH CSV HEADER

--
-- Load correct test data, dump new data,  compare test with new
--
\echo Setup comparision...
DROP TABLE IF EXISTS v_test_4_study_id_1_extract;
DROP TABLE IF EXISTS v_test_4_study_id_1_map;
DROP TABLE IF EXISTS v_test_4_study_id_1_bands;
CREATE TEMPORARY TABLE v_test_4_study_id_1_extract AS SELECT * FROM test_4_study_id_1_extract LIMIT 1;
CREATE TEMPORARY TABLE v_test_4_study_id_1_map AS 
SELECT study_id, inv_id, band_id, area_id, gid, genders, direct_standardisation, adjusted, observed, expected, lower95, upper95, relative_risk,
       smoothed_relative_risk, posterior_probability, posterior_probability_upper95, posterior_probability_lower95, residual_relative_risk,
       residual_rr_lower95, residual_rr_upper95, smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95 FROM test_4_study_id_1_map LIMIT 1;
CREATE TEMPORARY TABLE v_test_4_study_id_1_bands AS SELECT study_id,area_id,band_id FROM test_4_study_id_1_bands LIMIT 1;
TRUNCATE TABLE v_test_4_study_id_1_extract;
TRUNCATE TABLE v_test_4_study_id_1_map;
TRUNCATE TABLE v_test_4_study_id_1_bands;
\copy v_test_4_study_id_1_extract from ../example_data/test_4_study_id_1_extract.csv WITH (HEADER true, FORMAT csv, QUOTE '"', ESCAPE '\');
\copy v_test_4_study_id_1_map from ../example_data/test_4_study_id_1_map.csv WITH (HEADER true, FORMAT csv, QUOTE '"', ESCAPE '\');
\copy v_test_4_study_id_1_bands from ../example_data/test_4_study_id_1_bands.csv WITH (HEADER true, FORMAT csv, QUOTE '"', ESCAPE '\');
--
-- For vi's benefit

--
-- Dump
--
\echo Dump test data to ../psql_scripts/test_scripts/data...
\copy ( SELECT * FROM test_4_study_id_1_extract ORDER BY year, study_or_comparison, study_id, area_id, band_id, sex, age_group) to ../psql_scripts/test_scripts/data/test_4_study_id_1_extract.csv WITH CSV HEADER
\copy ( SELECT study_id, inv_id, band_id, area_id, gid, genders, direct_standardisation, adjusted, observed, expected, lower95, upper95, relative_risk, smoothed_relative_risk, posterior_probability, posterior_probability_upper95, posterior_probability_lower95, residual_relative_risk, residual_rr_lower95, residual_rr_upper95, smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95 FROM test_4_study_id_1_map ORDER BY gid_rowindex) to ../psql_scripts/test_scripts/data/test_4_study_id_1_map.csv WITH CSV HEADER
\copy ( SELECT study_id,area_id,band_id FROM test_4_study_id_1_bands ORDER BY study_id, area_id, band_id) to ../psql_scripts/test_scripts/data/stest_4_study_id_1_bands.csv WITH CSV HEADER

--
-- Compare
--
\echo Do comparision test_4_study_id_1_extract/v_test_4_study_id_1_extract etc...
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR /* v_test_4_study_id_1_extract diffs */
		WITH a AS ( /* Missing */
			SELECT * FROM test_4_study_id_1_extract
			EXCEPT
			SELECT * FROM v_test_4_study_id_1_extract
		), b AS ( /* Extra */
			SELECT * FROM v_test_4_study_id_1_extract
			EXCEPT
			SELECT * FROM test_4_study_id_1_extract
		), a1 AS (
			SELECT COUNT(area_id) AS missing_diffs
			  FROM a
		), b1 AS (
			SELECT COUNT(area_id) AS extra_diffs
			  FROM b
		)
		SELECT missing_diffs, extra_diffs
		  FROM a1, b1;
	c2 CURSOR FOR /* v_test_4_study_id_1_map diffs */
		WITH a AS ( /* Missing */
			SELECT study_id, inv_id, band_id, area_id, gid, genders, direct_standardisation, adjusted, observed, expected,
                   lower95, upper95, relative_risk, smoothed_relative_risk, posterior_probability, 
				   posterior_probability_upper95, posterior_probability_lower95,
                   residual_relative_risk, residual_rr_lower95, residual_rr_upper95, 
				   smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95
              FROM test_4_study_id_1_map
			EXCEPT
			SELECT study_id, inv_id, band_id, area_id, gid, genders, direct_standardisation, adjusted, observed, expected,
                   lower95, upper95, relative_risk, smoothed_relative_risk, posterior_probability, 
				   posterior_probability_upper95, posterior_probability_lower95,
                   residual_relative_risk, residual_rr_lower95, residual_rr_upper95, 
				   smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95
              FROM v_test_4_study_id_1_map
		), b AS ( /* Extra */
			SELECT study_id, inv_id, band_id, area_id, gid, genders, direct_standardisation, adjusted, observed, expected,
                   lower95, upper95, relative_risk, smoothed_relative_risk, posterior_probability, 
				   posterior_probability_upper95, posterior_probability_lower95,
                   residual_relative_risk, residual_rr_lower95, residual_rr_upper95, 
				   smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95
              FROM v_test_4_study_id_1_map
			EXCEPT
			SELECT study_id, inv_id, band_id, area_id, gid, genders, direct_standardisation, adjusted, observed, expected,
                   lower95, upper95, relative_risk, smoothed_relative_risk, posterior_probability, 
				   posterior_probability_upper95, posterior_probability_lower95,
                   residual_relative_risk, residual_rr_lower95, residual_rr_upper95, 
				   smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95
              FROM test_4_study_id_1_map
		), a1 AS (
			SELECT COUNT(band_id) AS missing_diffs
			  FROM a
		), b1 AS (
			SELECT COUNT(band_id) AS extra_diffs
			  FROM b
		)
		SELECT missing_diffs, extra_diffs
		  FROM a1, b1;
	c3 CURSOR FOR /* v_test_4_study_id_1_bands	diffs */
		WITH a AS ( /* Missing */
			SELECT study_id,area_id,band_id FROM test_4_study_id_1_bands
			EXCEPT
			SELECT study_id,area_id,band_id FROM v_test_4_study_id_1_bands
		), b AS ( /* Extra */
			SELECT study_id,area_id,band_id FROM v_test_4_study_id_1_bands
			EXCEPT
			SELECT study_id,area_id,band_id FROM test_4_study_id_1_bands
		), a1 AS (
			SELECT COUNT(area_id) AS missing_diffs
			  FROM a
		), b1 AS (
			SELECT COUNT(area_id) AS extra_diffs
			  FROM b
		)
		SELECT missing_diffs, extra_diffs
		  FROM a1, b1;
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
--
	errors INTEGER:=0;
BEGIN
--
-- v_test_4_study_id_1_extract diffs
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.missing_diffs	= 0 AND c1_rec.extra_diffs = 0 THEN
		RAISE INFO 'test_4_study_id_1.sql: T4--19: Test 4.5; Study: % test_4_study_id_1_extract and v_test_4_study_id_1_extract are the same', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql: T4--20: Test 4.5; Study: % stest_4_study_id_1_extract/v_test_4_study_id_1_extract diffs; % missing, % extra', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			c1_rec.missing_diffs::VARCHAR,
			c1_rec.extra_diffs::VARCHAR;
		errors:=errors+c1_rec.missing_diffs+c1_rec.extra_diffs;
	END IF;
--
-- v_test_4_study_id_1_map diffs
--
	OPEN c2;
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.missing_diffs	= 0 AND c2_rec.extra_diffs = 0 THEN
		RAISE INFO 'test_4_study_id_1.sql: T4--21: Test 4.6; Study: % test_4_study_id_1_map and v_test_4_study_id_1_map are the same', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql: T4--22: Test 4.6; Study: % stest_4_study_id_1_map/v_test_4_study_id_1_map diffs; % missing, % extra', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			c2_rec.missing_diffs::VARCHAR,
			c2_rec.extra_diffs::VARCHAR;
		errors:=errors+c2_rec.missing_diffs+c2_rec.extra_diffs;
	END IF;
--
-- v_test_4_study_id_1_bands diffs
--
	OPEN c3;
	FETCH c3 INTO c3_rec;
	CLOSE c3;
	IF c3_rec.missing_diffs	= 0 AND c3_rec.extra_diffs = 0 THEN
		RAISE INFO 'test_4_study_id_1.sql: T4--23: Test 4.7; Study: % test_4_study_id_1_bands and v_test_4_study_id_1_bands are the same', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql: T4--24: Test 4.7; Study: % stest_4_study_id_1_bands/v_test_4_study_id_1_bands diffs; % missing, % extra', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			c3_rec.missing_diffs::VARCHAR,
			c3_rec.extra_diffs::VARCHAR;
		errors:=errors+c3_rec.missing_diffs+c3_rec.extra_diffs;
	END IF;
-- 
	IF errors > 0 THEN
		RAISE EXCEPTION	'test_4_study_id_1.sql: T4--25: Test 4.5-7; Study: % % missing/extra diffs compared with reference', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			errors::VARCHAR;
	END IF;
END;
$$;

/*
Should cover the whole of level4

Here are the missing areas in sahsuland_example_map.csv:

missing
[1] "01.008.003901.1" "01.008.003901.2" "01.008.003901.3" "01.008.003901.4" "01.008.003901.5"
[6] "01.008.003901.6" "01.008.003901.7" "01.008.003901.9" "01.008.009401.1" "01.008.009401.2"
[11] "01.008.009401.3" "01.008.009401.4"

Test above and study extraction SQL is failing...
 */

 /*
SELECT level4 FROM sahsuland_level4
EXCEPT
SELECT area_id
  FROM test_4_study_id_1_extract
 WHERE study_or_comparison = 'S' LIMIT 20;
-- 0
SELECT level4 FROM sahsuland_level4
EXCEPT
SELECT area_id
  FROM test_4_study_id_1_extract
 WHERE study_or_comparison = 'S' LIMIT 20;
-- 0 
DO LANGUAGE plpgsql $$ 
DECLARE 
BEGIN 
	PERFORM rif40_sql_pkg.rif40_method4(
'SELECT level4 FROM sahsuland_level4'||E'\n'||
'EXCEPT'||E'\n'||
'SELECT area_id'||E'\n'||
'  FROM rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_extract'||E'\n'||
' WHERE study_or_comparison = ''S'''||E'\n'||
'   AND study_id = '||currval('rif40_study_id_seq'::regclass)||' LIMIT 20', 'rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_extract area_id diff');
END; 
$$;
-- 0
SELECT area_id, band_id FROM rif40_study_areas
 WHERE area_id IN ('01.008.003901.1', '01.008.003901.2', '01.008.003901.3', '01.008.003901.4', '01.008.003901.5', '01.008.003901.6',
				   '01.008.003901.7', '01.008.003901.9', '01.008.009401.1', '01.008.009401.2', '01.008.009401.3', '01.008.009401.4')
   AND study_id = currval('rif40_study_id_seq'::regclass);
*/
   /*
        area_id     | band_id
-----------------+---------
 01.008.009401.4 |     714
 01.008.003901.7 |     316
 01.008.009401.1 |     711
 01.008.003901.3 |     312
 01.008.003901.2 |     311
 01.008.009401.2 |     712
 01.008.003901.6 |     315
 01.008.003901.4 |     313
 01.008.009401.3 |     713
 01.008.003901.1 |     310
 01.008.003901.9 |     317
 01.008.003901.5 |     314
(12 rows)
 */
 /*
SELECT COUNT(area_id) FROM rif40_study_areas
 WHERE area_id IN ('01.008.003901.1', '01.008.003901.2', '01.008.003901.3', '01.008.003901.4', '01.008.003901.5', '01.008.003901.6',
				   '01.008.003901.7', '01.008.003901.9', '01.008.009401.1', '01.008.009401.2', '01.008.009401.3', '01.008.009401.4')
   AND study_id = currval('rif40_study_id_seq'::regclass);
-- 12
DO LANGUAGE plpgsql $$ 
DECLARE 
BEGIN 
	PERFORM rif40_sql_pkg.rif40_method4(
'SELECT COUNT(DISTINCT(area_id)) FROM rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_extract'||E'\n'||
' WHERE area_id IN (''01.008.003901.1'', ''01.008.003901.2'', ''01.008.003901.3'', ''01.008.003901.4'', ''01.008.003901.5'', ''01.008.003901.6'','||E'\n'||
'				    ''01.008.003901.7'', ''01.008.003901.9'', ''01.008.009401.1'', ''01.008.009401.2'', ''01.008.009401.3'', ''01.008.009401.4'')'||E'\n'||
'   AND study_id = currval(''rif40_study_id_seq''::regclass)', 'rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_extract area_id diff');
END; 
$$;
-- 12
DO LANGUAGE plpgsql $$ 
DECLARE 
BEGIN 
	PERFORM rif40_sql_pkg.rif40_method4(
'SELECT COUNT(DISTINCT(area_id)) FROM rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_map'||E'\n'||
' WHERE area_id IN (''01.008.003901.1'', ''01.008.003901.2'', ''01.008.003901.3'', ''01.008.003901.4'', ''01.008.003901.5'', ''01.008.003901.6'','||E'\n'||
'				    ''01.008.003901.7'', ''01.008.003901.9'', ''01.008.009401.1'', ''01.008.009401.2'', ''01.008.009401.3'', ''01.008.009401.4'')'||E'\n'||
'   AND study_id = currval(''rif40_study_id_seq''::regclass)', 'rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_extract area_id diff');
END; 
$$;
-- 12
SELECT COUNT(DISTINCT(level4)) FROM sahsuland_pop
 WHERE level4 IN ('01.008.003901.1', '01.008.003901.2', '01.008.003901.3', '01.008.003901.4', '01.008.003901.5', '01.008.003901.6',
				   '01.008.003901.7', '01.008.003901.9', '01.008.009401.1', '01.008.009401.2', '01.008.009401.3', '01.008.009401.4');
-- 12
SELECT COUNT(DISTINCT(level4)) FROM sahsuland_cancer
 WHERE level4 IN ('01.008.003901.1', '01.008.003901.2', '01.008.003901.3', '01.008.003901.4', '01.008.003901.5', '01.008.003901.6',
				   '01.008.003901.7', '01.008.003901.9', '01.008.009401.1', '01.008.009401.2', '01.008.009401.3', '01.008.009401.4');
-- 10
SELECT COUNT(DISTINCT(level4)) FROM sahsuland_covariates_level4
 WHERE level4 IN ('01.008.003901.1', '01.008.003901.2', '01.008.003901.3', '01.008.003901.4', '01.008.003901.5', '01.008.003901.6',
				   '01.008.003901.7', '01.008.003901.9', '01.008.009401.1', '01.008.009401.2', '01.008.009401.3', '01.008.009401.4');
-- 12
 
*/

/*
SELECT COUNT(area_id) AS total FROM test_4_study_id_1_extract;
SELECT COUNT(band_id) AS total FROM test_4_study_id_1_map;
SELECT COUNT(band_id) AS total FROM test_4_study_id_1_bands;
 */
--
-- Testing stop
--
--DO LANGUAGE plpgsql $$
--BEGIN
--	RAISE EXCEPTION 'Stop processing';
--END;
--$$;

\echo Created SAHSULAND example study 1.  

SELECT study_id
  FROM rif40_studies
 WHERE username = USER
   AND study_name = 'SAHSULAND test 4 study_id 1 example';
   
--
-- Rename study N to study 1
--
-- \set VERBOSITY verbose
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT * FROM rif40_studies
		 WHERE study_id = 1;
	c2 CURSOR FOR 
		SELECT COUNT(study_id) AS total FROM rif40_studies;
	c4sm CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c4sm_rec RECORD;
	c1_rec RECORD;
    c2_rec RECORD;
--
-- RIF40_STUDY_SQL
-- RIF40_STUDY_SQL_LOG
-- RIF40_RESULTS
-- RIF40_CONTEXTUAL_STATS
-- RIF40_INV_CONDITIONS 
-- RIF40_INV_COVARIATES 
-- RIF40_INVESTIGATIONS 
-- RIF40_STUDY_AREAS 
-- RIF40_COMPARISON_AREAS 
-- RIF40_STUDY_SHARES
-- RIF40_STUDIES 
--
	rows 		INTEGER;
	sql_stmt 	VARCHAR[];
	debug_level	INTEGER;
BEGIN
	OPEN c4sm;
	FETCH c4sm INTO c4sm_rec;
	CLOSE c4sm;
--
-- Test parameter
--
	IF c4sm_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'test_4_study_id_1.sql: T4--26: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'test_4_study_id_1.sql: T4--27: debug level parameter="%"', debug_level::Text;
	END IF;
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_rename_map_and_extract_tables:DEBUG'||debug_level::Text);
--
-- Fetch study 1
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	OPEN c2;
	FETCH c2 INTO c2_rec;
	CLOSE c2;                                                                   
--
-- Check study name
--
	IF c1_rec.study_name IS NULL AND c2_rec.total = 1 THEN
-- Make EXCEPTION                                                                                
        RAISE NOTICE 'test_4_study_id_1.sql: T4--28: no study 1';
        RETURN;
    ELSIF c1_rec.study_name IS NULL THEN
		RAISE EXCEPTION	'test_4_study_id_1.sql: T4--28: Test 4.8 no study 1 found; total = %', c2_rec.total::Text;
	ELSIF c1_rec.study_name != 'SAHSULAND test 4 study_id 1 example' THEN
		RAISE EXCEPTION	'test_4_study_id_1.sql: T4--29: Test 4.9; Study: 1 name (%) is not test 4 example', 
			c1_rec.study_name;
	END IF;
--
-- Check NOT study 1 - i.e. first run
--
	IF currval('rif40_study_id_seq'::regclass) = 1 THEN
        RAISE INFO 'test_4_study_id_1.sql: T4--28: Only study 1 present';                                                                          
		RETURN;
	END IF;
--
	sql_stmt[1]:='DELETE FROM rif40_inv_conditions'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_inv_covariates'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
-- Do full INSERT for study and comparison areas
-- This is to cope with expected geo-spatial changes
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_areas'||E'\n'||
'	 WHERE study_id = 1';
	sql_stmt[array_length(sql_stmt, 1)+1]:='INSERT INTO rif40_study_areas'||E'\n'||
'SELECT username, 1 study_id, area_id, band_id'||E'\n'||
'  FROM rif40_study_areas'||E'\n'||
' WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_areas'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_comparison_areas'||E'\n'||
'	 WHERE study_id = 1';
	sql_stmt[array_length(sql_stmt, 1)+1]:='INSERT INTO rif40_comparison_areas'||E'\n'||
'SELECT username, 1 study_id, area_id'||E'\n'||
'  FROM rif40_comparison_areas'||E'\n'||
' WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_comparison_areas'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_sql'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_sql_log'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
-- Do full INSERT for results
--
-- This will need to become more sophisticated if T_RIF40_RESULTS is modified
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_results'||E'\n'||
'	 WHERE study_id = 1';
	sql_stmt[array_length(sql_stmt, 1)+1]:='INSERT INTO rif40_results'||E'\n'||
'SELECT  username, 1 AS study_id, 1 AS inv_id, band_id, genders, direct_standardisation, adjusted, observed, expected, lower95,'||E'\n'||
'        upper95, relative_risk, smoothed_relative_risk, posterior_probability, posterior_probability_upper95,'||E'\n'||
'        posterior_probability_lower95, residual_relative_risk, residual_rr_lower95, residual_rr_upper95,'||E'\n'||
'        smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95 '||E'\n'||
'  FROM rif40_results'||E'\n'||
' WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_results'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_contextual_stats'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_shares'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';

--
-- Run
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Delete extract and map tables for <new study id>; 
-- rename <old study id> extract and map tables to <new study id> extract and map tables
--
	PERFORM rif40_sm_pkg.rif40_rename_map_and_extract_tables(currval('rif40_study_id_seq'::regclass)::INTEGER /* Old */, 1::INTEGER	/* New */);
--
-- Now delete study N
--
	sql_stmt:=NULL;
--
	sql_stmt[1]:='DELETE FROM rif40_investigations'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_studies'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	RAISE INFO 'test_4_study_id_1.sql: T4--30: Study: % renamed to study 1; rows processed: %', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR, 
			rows::VARCHAR;
--
-- Fetch study 1
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
-- Check study name again
--
	IF c1_rec.study_name IS NULL THEN
		RAISE EXCEPTION	'test_4_study_id_1.sql: T4--31: Test 4.10 study 1 no longer found';
	ELSIF c1_rec.study_name != 'SAHSULAND test 4 study_id 1 example' THEN
		RAISE EXCEPTION	'test_4_study_id_1.sql: T4--32: Test 4.11; Study: 1 name (%) is no longer the test 4 example', 
			c1_rec.study_name;
	END IF;
END;
$$;

--
-- End single transaction
--
END;
--\dS+ s1_extract
--\dS+ s1_map
\echo Test 4: Created SAHSULAND example study 1.    

SELECT study_id
  FROM rif40_studies
 WHERE username = USER
   AND study_name = 'SAHSULAND test 4 study_id 1 example';

   /*
SELECT COUNT(DISTINCT(band_id)) FROM rif_studies.s1_map
 WHERE band_id IN (SELECT band_id FROM rif40_study_areas WHERE study_id = 1)
   AND study_id = 1
   AND area_id IN ('01.008.003901.1', '01.008.003901.2', '01.008.003901.3', '01.008.003901.4', '01.008.003901.5', '01.008.003901.6',
				   '01.008.003901.7', '01.008.003901.9', '01.008.009401.1', '01.008.009401.2', '01.008.009401.3', '01.008.009401.4');
-- 12
SELECT COUNT(DISTINCT(band_id)) FROM rif_studies.s1_extract
 WHERE band_id IN (SELECT band_id FROM rif40_study_areas WHERE study_id = 1)
   AND study_id = 1
   AND area_id IN ('01.008.003901.1', '01.008.003901.2', '01.008.003901.3', '01.008.003901.4', '01.008.003901.5', '01.008.003901.6',
				   '01.008.003901.7', '01.008.003901.9', '01.008.009401.1', '01.008.009401.2', '01.008.009401.3', '01.008.009401.4');
-- 12
-- should be 12   
 */
 
-- 
-- Diff s1_extract/map and v_test_4_study_id_1_extract/map
--
--\set VERBOSITY verbose
DO LANGUAGE plpgsql $$
DECLARE
	c1sm CURSOR FOR 
		SELECT array_agg(column_name::Text) AS s1_map_columns
		  FROM information_schema.columns
		 WHERE table_name = 's1_map'
		   AND column_name NOT IN ('gid', 'gid_rowindex', 'username');
	c4sm CURSOR FOR 		
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c1sm_rec RECORD;
	c4sm_rec RECORD;
--
	old_study_id	INTEGER;
	new_study_id	INTEGER;
--
	rif40_pkg_functions 		VARCHAR[] := ARRAY[
				'rif40_table_diff'];
	l_function 			VARCHAR;
	debug_level		INTEGER;
BEGIN
	OPEN c4sm;
	FETCH c4sm INTO c4sm_rec;
	CLOSE c4sm;
	OPEN c1sm;
	FETCH c1sm INTO c1sm_rec;
	CLOSE c1sm;
--
-- Test parameter
--
	IF c4sm_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'test_4_study_id_1.sql: T4-33: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'T4--34: test_4_study_id_1.sql: debug level parameter="%"', debug_level::Text;
	END IF;
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_4_study_id_1.sql: T4--35: Invalid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_pkg_functions LOOP
			RAISE INFO 'T4--36: test_4_study_id_1.sql: Enable debug for function: %', l_function;
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
		END LOOP;
	END IF;
--
-- Validate 2 sahsuland_geography tables are the same
--
	PERFORM rif40_sql_pkg.rif40_table_diff('T4__37(s1_extract)' /* Test tag */, 's1_extract', 'v_test_4_study_id_1_extract');
	PERFORM rif40_sql_pkg.rif40_table_diff('T4__38(s1_map)' /* Test tag */, 's1_map', 'v_test_4_study_id_1_map', 
                    c1sm_rec. s1_map_columns, c1sm_rec. s1_map_columns);
--
--	RAISE EXCEPTION 'TEST Abort';
END;
$$;	
	
--
-- Testing stop
--
--DO LANGUAGE plpgsql $$
--BEGIN
--	RAISE EXCEPTION 'Stop processing';
--END;
--$$;

--
-- Eof