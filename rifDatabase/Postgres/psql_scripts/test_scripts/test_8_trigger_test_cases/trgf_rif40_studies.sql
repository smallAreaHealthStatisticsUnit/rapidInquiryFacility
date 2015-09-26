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
	test_id INTEGER;
BEGIN
	DELETE FROM rif40_test_harness 
	 WHERE test_run_class = 'trgf_rif40_studies';
--	 
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
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - 20200 T_RIF40_STUDIES study % username: % is not USER: % INSERT' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20200'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		FALSE          /* expected_result */,
		NULL           /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);
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
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - 20200 T_RIF40_STUDIES study % username: % is not USER: % INSERT' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20200'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		NULL           /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);
-- 
-- Update tests
--
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
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - 20200 T_RIF40_STUDIES study % username: % is not USER: % INSERT #2' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		NULL           /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies /* Testing test harness handles wrong error codes OK */
			SET extract_permitted = 0                    /* extract_permitted */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - 20202 T_RIF40_STUDIES study % UPDATE state changes allowed on T_RIF40_STUDIES by user: %' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20200'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		FALSE          /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);	
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies 
			SET extract_permitted = 0                    /* extract_permitted */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - 20202 T_RIF40_STUDIES study % UPDATE state changes allowed on T_RIF40_STUDIES by user: %' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20202'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);	
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies 
			SET extract_permitted = 0                    /* extract_permitted */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - 20202 T_RIF40_STUDIES study % UPDATE state changes allowed on T_RIF40_STUDIES by user: %' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20202'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);	
		
--
-- Test rif40_sm_pkg.rif40_verify_state_change
--
-- OK case:
--
	test_id:=rif40_sql_pkg._rif40_sql_test_register(
		'INSERT /* 1 */ INTO rif40_studies (
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
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - Test rif40_sm_pkg.rif40_verify_state_change setup #1' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		NULL           /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'INSERT /* 2 */ INTO rif40_investigations(
	inv_name,
	inv_description,
	genders,
	numer_tab,
	year_start,
	year_stop,
	max_age_group,
	min_age_group
)
VALUES (
	''INV_1'' 		/* inv_name */,
	''Lung cancer''	/* inv_description */,
	3			/* genders [both] */,
	''SAHSULAND_CANCER''		/* numer_tab */,
	1989		/* year_start */,
	1996 		/* year_stop */,
	21 /* max_age_group */,
	0 /* min_age_group */) /* Test stmt */',
		'trgf_rif40_studies' /* test run class */,
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - Test rif40_sm_pkg.rif40_verify_state_change setup #2' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_investigations_checks}'::text[] /* pg debug functions */);	
	test_id:=rif40_sql_pkg._rif40_sql_test_register(
		'INSERT /* 3 */ INTO rif40_inv_conditions(
			outcome_group_name, min_condition, max_condition, predefined_group_name, line_number)
WITH data AS (
				SELECT ''{{SAHSULAND_ICD,C34,NULL,NULL},{SAHSULAND_ICD,162,1629,NULL}}''::Text[][] AS arr
			), b AS (
				SELECT arr[i][1] AS outcome_group_name,
					   arr[i][2] AS min_condition, 
					   arr[i][3] AS max_condition, 
					   arr[i][4] AS predefined_group_name, 
       	           ROW_NUMBER() OVER() AS line_number
			      FROM data, generate_subscripts((SELECT arr FROM data), 1) i
			)
		SELECT outcome_group_name, min_condition, max_condition, predefined_group_name, line_number
		  FROM b /* Test stmt */',
		'trgf_rif40_studies' /* test run class */,
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - Test rif40_sm_pkg.rif40_verify_state_change setup #3' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);		  
	test_id:=rif40_sql_pkg._rif40_sql_test_register(
		'INSERT /* 4 */ INTO rif40_study_areas(area_id, band_id)
SELECT DISTINCT level4, ROW_NUMBER() OVER() AS band_id
	  FROM sahsuland_geography
	 WHERE level3 IN (
	SELECT unnest(
''{01.001.000100,01.001.000200,01.001.000300,01.002.000300,01.002.000400,01.002.000500,01.002.000600,01.002.000700,01.002.000800,01.002.000900,01.002.001000,01.002.001100,01.002.001200,01.002.001300,01.002.001400,01.002.001500,01.002.001600,01.002.001700,01.002.001800,01.002.001900,01.002.002000,01.002.002100,01.002.002200,01.002.002300,01.003.003300,01.003.003400,01.004.011100,01.004.011200,01.004.011300,01.004.011400,01.004.011500,01.004.011600,01.004.011700,01.004.011800,01.004.011900,01.005.002400,01.006.002500,01.006.002600,01.006.002700,01.007.012000,01.007.012100,01.007.012200,01.007.012300,01.007.012400,01.007.012500,01.008.001000,01.008.002900,01.008.003500,01.008.003600,01.008.003700,01.008.003800,01.008.003900,01.008.004000,01.008.004100,01.008.004200,01.008.004300,01.008.004400,01.008.004500,01.008.004600,01.008.004700,01.008.004800,01.008.004900,01.008.005000,01.008.005100,01.008.005200,01.008.005300,01.008.005400,01.008.005500,01.008.005600,01.008.005700,01.008.005800,01.008.005900,01.008.006000,01.008.006100,01.008.006200,01.008.006300,01.008.006400,01.008.006500,01.008.006600,01.008.006700,01.008.006800,01.008.006900,01.008.007000,01.008.007100,01.008.007200,01.008.007300,01.008.007400,01.008.007500,01.008.007600,01.008.007700,01.008.007800,01.008.007900,01.008.008000,01.008.008100,01.008.008200,01.008.008300,01.008.008400,01.008.008500,01.008.008600,01.008.008700,01.008.008800,01.008.008900,01.008.009000,01.008.009100,01.008.009200,01.008.009300,01.008.009400,01.008.009500,01.008.009600,01.008.009700,01.008.009800,01.008.009900,01.008.010100,01.008.010200,01.008.010300,01.008.010400,01.008.010500,01.008.010600,01.008.010700,01.008.010800,01.008.010900,01.008.011000,01.009.002700,01.009.002800,01.009.002900,01.009.003000,01.009.003100,01.009.003200,01.011.012600,01.011.012700,01.011.012800,01.011.012900,01.011.013000,01.011.013100,01.011.013200,01.011.013300,01.011.013400,01.011.013500,01.011.013600,01.011.013700,01.011.013800,01.011.013900,01.011.014000,01.011.014100,01.011.014200,01.011.014300,01.011.014400,01.011.014500,01.011.014600,01.011.014700,01.011.014800,01.011.014900,01.011.015000,01.011.015100,01.011.015200,01.011.015300,01.011.015400,01.011.015500,01.011.015600,01.011.015700,01.011.015800,01.012.003000,01.012.015900,01.012.016000,01.013.016000,01.013.016100,01.013.016200,01.013.016300,01.013.016400,01.013.016500,01.013.016600,01.013.016700,01.013.016800,01.014.017200,01.014.017300,01.014.017500,01.014.017600,01.014.017700,01.014.017800,01.014.017900,01.014.018000,01.014.018100,01.014.018200,01.014.018300,01.014.018400,01.014.018500,01.014.018600,01.014.018700,01.014.018800,01.015.016200,01.015.016900,01.016.017000,01.016.017100,01.017.018900,01.017.019000,01.018.019100,01.018.019200,01.018.019300,01.018.019400,01.018.019500}''::Text[]) /* at Geolevel select */ AS study_area)',
		'trgf_rif40_studies' /* test run class */,
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - Test rif40_sm_pkg.rif40_verify_state_change setup #4' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);	test_id:=rif40_sql_pkg._rif40_sql_test_register(
		'INSERT /* 5 */ INTO rif40_comparison_areas(area_id)
SELECT unnest(
''{01.001,01.002,01.003,01.004,01.005,01.006,01.007,01.008,01.009,01.011,01.012,01.013,01.014,01.015,01.016,01.017,01.018}''::Text[]) AS comparision_area',
		'trgf_rif40_studies' /* test run class */,
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - Test rif40_sm_pkg.rif40_verify_state_change setup #5' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);
	test_id:=rif40_sql_pkg._rif40_sql_test_register(		
		'UPDATE t_rif40_studies /* Attempting to change the state (%=>%) of a completed study %. Please clone */
			SET study_state = ''V''                    /* verified */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - Attempting to change the state (%=>%) of a completed study %. Please clone' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		NULL           /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		TRUE           /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);	
--
	
		
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