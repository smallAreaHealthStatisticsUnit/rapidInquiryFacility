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
-- Rapid Enquiry Facility (RIF) - Test 8: Trigger test cases rif40_studies
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

\echo Test 8: Trigger test cases for T_RIF40_STUDIES...

DO LANGUAGE plpgsql $$
DECLARE
	c2th CURSOR FOR 
		SELECT array_agg(level3) AS level3_array FROM sahsuland_level3;	 
--
	c2th_rec RECORD;	
--
	condition_array				VARCHAR[4][2]:='{{"SAHSULAND_ICD", "C34", NULL, NULL}, {"SAHSULAND_ICD", "162", "1629", NULL}}';	
										 /* investigation ICD conditions 2 dimensional array (i.e. matrix); 4 columnsxN rows:
											outcome_group_name, min_condition, max_condition, predefined_group_name */
	investigation_desc_array	VARCHAR[]:=array['Lung cancer'];
	covariate_array				VARCHAR[]:=array['SES'];	
--
	test_id INTEGER;
BEGIN
	DELETE FROM rif40_test_harness 
	 WHERE test_run_class = 'trgf_rif40_studies';
--	 
	RAISE INFO '01: trgf_rif40_studies.sql: Testing test harness handles wrong error codes OK';
	test_id:=rif40_sql_pkg._rif40_sql_test_register(
		'INSERT /* 1 */ INTO t_rif40_studies ( /* Testing test harness handles wrong error codes OK */
				username,
				geography, project, study_name, study_type,
				year_start,
				comparison_geolevel_name, study_geolevel_name, denom_tab,
				suppression_value, extract_permitted, transfer_permitted)
			VALUES (
				 ''rif40''                               /* User name */,
				 ''SAHSU''                               /* geography */,
				 ''TEST''                                /* project */,
				 ''SAHSULAND test 4 study_id 1 example'' /* study_name */,
				 1                                       /* study_type [disease mapping] */,
				 1999									 /* Not a valid column */
				 ''LEVEL2''                              /* comparison_geolevel_name */,
				 ''LEVEL4''                              /* study_geolevel_name */,
				 ''SAHSULAND_POP''                       /* denom_tab */,
				 5                                       /* suppression_value */,
				 1                                       /* extract_permitted */,
				 1                                       /* transfer_permitted */)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'01: Testing test harness handles wrong error codes OK - 20200 T_RIF40_STUDIES study <var> username: <var> is not USER: <var> INSERT' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20200'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		FALSE          /* expected_result */,
		NULL           /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);
	RAISE INFO '02: 20200 T_RIF40_STUDIES study <var> username: <var> is not USER: <var> INSERT';
	test_id:=rif40_sql_pkg._rif40_sql_test_register(
		'INSERT /* 1 */ INTO t_rif40_studies (
				username,
				geography, project, study_name, study_type,
				comparison_geolevel_name, study_geolevel_name, denom_tab,
				suppression_value, extract_permitted, transfer_permitted)
			VALUES (
				 ''rif40''                               /* User name */,
				 ''SAHSU''                               /* geography */,
				 ''TEST''                                /* project */,
				 ''SAHSULAND test 4 study_id 1 example'' /* study_name */,
				 1                                       /* study_type [disease mapping] */,
				 ''LEVEL2''                              /* comparison_geolevel_name */,
				 ''LEVEL4''                              /* study_geolevel_name */,
				 ''SAHSULAND_POP''                       /* denom_tab */,
				 5                                       /* suppression_value */,
				 1                                       /* extract_permitted */,
				 1                                       /* transfer_permitted */)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'02: 20200 T_RIF40_STUDIES study <var> username: <var> is not USER: <var> INSERT' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20200'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result: expected to failed with pg_error_code_expected */,
		NULL           /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);
-- 
-- Update tests
--
	RAISE INFO '03: 20200 T_RIF40_STUDIES study <var> username: <var> is not USER: <var> INSERT #2';
	test_id:=rif40_sql_pkg._rif40_sql_test_register(
		'INSERT /* 1 */ INTO t_rif40_studies (
				geography, project, study_name, study_type,
				comparison_geolevel_name, study_geolevel_name, denom_tab,
				suppression_value, extract_permitted, transfer_permitted)
			VALUES (
				 ''SAHSU''                               /* geography */,
				 ''TEST''                                /* project */,
				 ''SAHSULAND test 4 study_id 1 example'' /* study_name */,
				 1                                       /* study_type [disease mapping] */,
				 ''LEVEL2''                              /* comparison_geolevel_name */,
				 ''LEVEL4''                              /* study_geolevel_name */,
				 ''SAHSULAND_POP''                       /* denom_tab */,
				 5                                       /* suppression_value */,
				 1                                       /* extract_permitted */,
				 1                                       /* transfer_permitted */)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'03: 20200 T_RIF40_STUDIES study <var> username: <var> is not USER: <var> INSERT #1' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		NULL           /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);
	RAISE INFO '04: 20200 T_RIF40_STUDIES study <var> username: <var> is not USER: <var> INSERT #2';		
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies /* Testing test harness handles wrong error codes OK */
			SET extract_permitted = 0                    /* extract_permitted */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'04: Testing test harness handles wrong error codes OK - 20202 T_RIF40_STUDIES study <var> UPDATE state changes allowed on T_RIF40_STUDIES by user: <var>' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20200'       /* pg_error_code_expected - should be -20202 [DELIBERATE] */,
		FALSE          /* raise_exception_on_failure */, 
		FALSE          /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);	
	RAISE INFO '03: 20200 T_RIF40_STUDIES study <var> username: <var> is not USER: <var> INSERT #2';		
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies 
			SET extract_permitted = 0                    /* extract_permitted */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'03: 20202 T_RIF40_STUDIES study <var> UPDATE state changes allowed on T_RIF40_STUDIES by user: <var> UPDATE #2' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20202'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result: expected to failed with pg_error_code_expected */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);	
		
--
-- Test rif40_sm_pkg.rif40_verify_state_change
--
-- OK case:
--
	RAISE INFO '05: Attempting to change the state (<var>=><var>) of a completed study <var>. Please clone';		
	OPEN c2th;
	FETCH c2th INTO c2th_rec;
	CLOSE c2th;
	test_id:=rif40_sm_pkg.rif40_create_disease_mapping_example(
		'SAHSU'::VARCHAR 			/* Geography */,
		'LEVEL1'::VARCHAR			/* Geolevel view */,
		'01'::VARCHAR				/* Geolevel area */,
		'LEVEL4'::VARCHAR			/* Geolevel map */,
		'LEVEL3'::VARCHAR			/* Geolevel select */,
		c2th_rec.level3_array 		/* Geolevel selection array */,
		'TEST'::VARCHAR 			/* project */, 
		'05: Attempting to change the state (<var>=><var>) of a completed study <var>. Please clone'::VARCHAR /* study name */, 
		'SAHSULAND_POP'::VARCHAR 	/* denominator table */, 
		'SAHSULAND_CANCER'::VARCHAR /* numerator table */,
 		1989						/* year_start */, 
		1996						/* year_stop */,
		condition_array 			/* investigation ICD conditions 2 dimensional array (i.e. matrix); 4 columnsxN rows:
											outcome_group_name, min_condition, max_condition, predefined_group_name */,
		investigation_desc_array 	/* investigation description array */,
		covariate_array				/* covariate array */,
		NULL						/* stop after table */,
		'trgf_rif40_studies' 		/* test run class */);	
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies /* Attempting to change the state (<var>=><var>) of a completed study <var>. Please clone */
			SET study_state = ''V''                    /* verified */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'05: Attempting to change the state (<var>=><var>) of a completed study <var>. Please clone UPDATE #6' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks, rif40_verify_state_change}'::text[] /* pg debug functions */);	
--
	RAISE INFO '06: Attempting to change the state (<var>=><var>) of upgraded RIF30 study <var>. Please clone';
	test_id:=rif40_sm_pkg.rif40_create_disease_mapping_example(
		'SAHSU'::VARCHAR 			/* Geography */,
		'LEVEL1'::VARCHAR			/* Geolevel view */,
		'01'::VARCHAR				/* Geolevel area */,
		'LEVEL4'::VARCHAR			/* Geolevel map */,
		'LEVEL3'::VARCHAR			/* Geolevel select */,
		c2th_rec.level3_array 		/* Geolevel selection array */,
		'TEST'::VARCHAR 			/* project */, 
		'06: Attempting to change the state (<var>=><var>) of upgraded RIF30 study <var>. Please clone'::VARCHAR /* study name */, 
		'SAHSULAND_POP'::VARCHAR 	/* denominator table */, 
		'SAHSULAND_CANCER'::VARCHAR /* numerator table */,
 		1989						/* year_start */, 
		1996						/* year_stop */,
		condition_array 			/* investigation ICD conditions 2 dimensional array (i.e. matrix); 4 columnsxN rows:
											outcome_group_name, min_condition, max_condition, predefined_group_name */,
		investigation_desc_array 	/* investigation description array */,
		covariate_array				/* covariate array */,
		NULL						/* stop after table */,
		'trgf_rif40_studies' 		/* test run class */);	
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies /* Attempting to change the state (%=>%) of upgraded RIF30 study %. Please clone */
			SET study_state = ''U''                    /* upgraded */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'06: Attempting to change the state (<var>=><var>) of upgraded RIF30 study <var>. Please clone #UPDATE' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-55002'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result: expected to failed with pg_error_code_expected */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks, rif40_verify_state_change}'::text[] /* pg debug functions */);
--
	RAISE INFO '07: Test rif40_sm_pkg.rif40_verify_state_change C=>V (verify study)';	
		test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies /* Attempting to change the state (C=>V) of a completed study %. Please clone */
			SET study_state = ''V''                    /* verified */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'08: Verify study' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks, rif40_verify_state_change}'::text[] /* pg debug functions */);		
	RAISE INFO '08: Test rif40_sm_pkg.rif40_verify_state_change V=>E (extract study)';		
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies /* Test rif40_sm_pkg.rif40_verify_state_change C=>R */
			SET study_state = ''E''                    /* extract */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'08: Test rif40_sm_pkg.rif40_verify_state_change V=>E (extract study) [needs to be run properly using rif40_run_study()]' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-55006'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks, rif40_verify_state_change}'::text[] /* pg debug functions */);	
--
-- Create new example, run study
--
	RAISE INFO '09: Test rif40_sm_pkg.rif40_verify_state_change E=>R (run study)';		
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies /* Test rif40_sm_pkg.rif40_verify_state_change E=>R */
			SET study_state = ''R''                    /* run */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'09: Test rif40_sm_pkg.rif40_verify_state_change E=>R (run study) [needs to be run properly using rif40_run_study()]' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-55002'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks, rif40_verify_state_change}'::text[] /* pg debug functions */);
--		
	RAISE INFO '10: Attempting to change the state (<var>=><var>) of a completed study <var>. Please clone';		
--	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
--		'UPDATE t_rif40_studies /* Attempting to change the state (%=>%) of a completed study %. Please clone */
--			SET study_state = ''V''                    /* verified */
--		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
--		'trgf_rif40_studies' /* test run class */,
--		'10: Attempting to change the state (<var>=><var>) of a completed study <var>. Please clone' /* test case title */,
--		NULL::Text[][] /* results */,
--		NULL::XML      /* results_xml */,
--		'-55001'       /* pg_error_code_expected */,
--		FALSE          /* raise_exception_on_failure */, 
--		TRUE           /* expected_result: expected to failed with pg_error_code_expected */,
--		test_id        /* parent_test_id */,
--		'{trigger_fct_t_rif40_studies_checks, rif40_verify_state_change}'::text[] /* pg debug functions */);		
		
--
-- State change by another user cannot be tested, also update and delete checks (-20203 to 5)
--

--
-- Check - Comparison area geolevel name(COMPARISON_GEOLEVEL_NAME). Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1.
--

END;
$$;

--
-- Eof