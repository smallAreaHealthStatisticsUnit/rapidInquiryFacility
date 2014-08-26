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
-- the Free Software Foundation, either verquote_ident(l_schema)||'.'||quote_ident(l_tablesion 3 of the License, or
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

\echo test 1: Creating SAHSULAND example study 1...

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
	c3sm CURSOR FOR 
		SELECT study_id
		  FROM rif40_studies
		 WHERE study_name = 'SAHSULAND test example'
		   AND username = USER;
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
BEGIN
	OPEN c4sm;
	FETCH c4sm INTO c4sm_rec;
	CLOSE c4sm;
--
-- Test parameter
--
	IF c4sm_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'test_4_study_id_1.sql() C209xx: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4sm_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'test_4_study_id_1.sql() debug level parameter="%"', debug_level::Text;
	END IF;
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_4_study_id_1.sql() C209xx: Invslid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);

		FOR c1sm_rec IN c1sm LOOP
			IF c1sm_rec.function IS NOT NULL THEN
				RAISE INFO 'Enable debug for % trigger function: % on table: %',
					c1sm_rec.action_timing, c1sm_rec.function, c1sm_rec.table_name;
					PERFORM rif40_log_pkg.rif40_add_to_debug(c1sm_rec.function||':DEBUG'||debug_level::Text);
			ELSE
				RAISE WARNING 'No trigger function found for table: %', c1sm_rec.table_name;
			END IF;
		END LOOP;
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_sm_pkg_functions LOOP
			RAISE INFO 'Enable debug for function: %', l_function;
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
		RAISE INFO 'Delete of SAHSULAND test example study: %', c3sm_rec.study_id::VARCHAR;
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
		'SAHSULAND test example'::VARCHAR /* study name */, 
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
	IF rif40_sm_pkg.rif40_run_study(currval('rif40_study_id_seq'::regclass)::INTEGER) THEN
		RAISE INFO 'Test 4; Study: % run OK', currval('rif40_study_id_seq'::regclass)::VARCHAR;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	ELSE
		RAISE EXCEPTION 'test_4_study_id_1.sql() C209xx: Test 4; Study: % run failed; see trace', currval('rif40_study_id_seq'::regclass)::VARCHAR;
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
\dS rif40_num_denom
\dS+ test_4_study_id_1_extract
\dS+ test_4_study_id_1_map
\dS+ test_4_study_id_1_bands
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
\pset title
--
-- Check all level4 area_ids presents in: 
--
-- v_test_4_study_id_1_extract, v_test_4_study_id_1_map, v_test_4_study_id_1_bands
-- rif40_results WHERE study_id = currval('rif40_study_id_seq'::regclass)
--
--\set VERBOSITY verbose
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
		RAISE INFO 'Test 4; Study: % study area all present in test_4_study_id_1_extract', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql() C209xx:  Test 4; Study: % study area not all present in test_4_study_id_1_extract; % missing, % extra', 
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
		RAISE INFO 'Test 4; Study: % study area all present in test_4_study_id_1_map', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql() C209xx:  Test 4; Study: % study area not all present in test_4_study_id_1_map; % missing, % extra', 
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
		RAISE INFO 'Test 4; Study: % study area all present in test_4_study_id_1_bands', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql() C209xx:  Test 4; Study: % study area not all present in test_4_study_id_1_bands; % missing, % extra', 
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
		RAISE INFO 'Test 4; Study: % study area all present in rif40_results', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE WARNING 'test_4_study_id_1.sql() C209xx:  Test 4; Study: % study area not all present in rif40_results; % missing, % extra', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			c4_rec.missing_level4::VARCHAR,
			c4_rec.extra_level4::VARCHAR;
		errors:=errors+c4_rec.missing_level4+c4_rec.extra_level4;
	END IF;
-- 
	IF errors > 0 THEN
		RAISE EXCEPTION	'test_4_study_id_1.sql() C209xx:  Test 4; Study: % % missing/extra level4 errors', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR,
			errors::VARCHAR;
	END IF;
END;
$$;

--
-- Reference test data (usually comment out unless it needs to be updated)
-- 
--\copy ( SELECT * FROM test_4_study_id_1_extract ORDER BY year, study_or_comparison, study_id, area_id, band_id, sex, age_group) to ../example_data/test_4_study_id_1_extract.csv WITH CSV HEADER
--\copy ( SELECT * FROM test_4_study_id_1_map ORDER BY gid_rowindex) to ../example_data/test_4_study_id_1_map.csv WITH CSV HEADER
--\copy ( SELECT * FROM test_4_study_id_1_bands ORDER BY study_id, area_id, band_id) to ../example_data/test_4_study_id_1_bands.csv WITH CSV HEADER

--
-- Load correct test data, dump new data,  compare test with new
--
DROP TABLE IF EXISTS v_test_4_study_id_1_extract;
DROP TABLE IF EXISTS v_test_4_study_id_1_map;
DROP TABLE IF EXISTS v_test_4_study_id_1_bands;
CREATE TEMPORARY TABLE v_test_4_study_id_1_extract AS SELECT * FROM test_4_study_id_1_extract LIMIT 1;
CREATE TEMPORARY TABLE v_test_4_study_id_1_map AS SELECT * FROM test_4_study_id_1_map LIMIT 1;
CREATE TEMPORARY TABLE v_test_4_study_id_1_bands AS SELECT * FROM test_4_study_id_1_bands LIMIT 1;
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
\copy ( SELECT * FROM test_4_study_id_1_extract ORDER BY year, study_or_comparison, study_id, area_id, band_id, sex, age_group) to ../psql_scripts/test_scripts/data/test_4_study_id_1_extract.csv WITH CSV HEADER
\copy ( SELECT * FROM test_4_study_id_1_map ORDER BY gid_rowindex) to ../psql_scripts/test_scripts/data/test_4_study_id_1_map.csv WITH CSV HEADER
\copy ( SELECT * FROM test_4_study_id_1_bands ORDER BY study_id, area_id, band_id) to ../psql_scripts/test_scripts/data/stest_4_study_id_1_bands.csv WITH CSV HEADER

--
-- Compare
--


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

--
-- Rename study N to study 1
--


--
-- End single transaction
--
END;

\echo Test 1: Created SAHSULAND example study 1.    

--
-- Eof