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
-- Rapid Enquiry Facility (RIF) - Test 8: Trigger test cases
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
	test_id:=rif40_sql_pkg._rif40_sql_test_register(
		'INSERT /* 1 */ INTO t_rif40_studies (
				username,
				geography, project, study_name, study_type,
				comparison_geolevel_name, study_geolevel_name, denom_tab,
				year_start, year_stop, max_age_group, min_age_group,
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
				 1989                                    /* year_start */,
				 1996                                    /* year_stop */,
				 21                                      /* max_age_group */,
				 0                                       /* min_age_group */,
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
-- 
	test_id:=rif40_sql_pkg._rif40_sql_test_register(
		'INSERT /* 1 */ INTO t_rif40_studies (
				geography, project, study_name, study_type,
				comparison_geolevel_name, study_geolevel_name, denom_tab,
				year_start, year_stop, max_age_group, min_age_group,
				suppression_value, extract_permitted, transfer_permitted)
			VALUES (
				 ''SAHSU''                               /* geography */,
				 ''TEST''                                /* project */,
				 ''SAHSULAND test 4 study_id 1 example'' /* study_name */,
				 1                                       /* study_type [disease mapping] */,
				 ''LEVEL2''                              /* comparison_geolevel_name */,
				 ''LEVEL4''                              /* study_geolevel_name */,
				 ''SAHSULAND_POP''                       /* denom_tab */,
				 1989                                    /* year_start */,
				 1996                                    /* year_stop */,
				 21                                      /* max_age_group */,
				 0                                       /* min_age_group */,
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
		'UPDATE t_rif40_studies 
			SET extract_permitted = 0                    /* extract_permitted */
		  WHERE study_id = currval(''rif40_study_id_seq''::regclass)' /* Test stmt */,
		'trgf_rif40_studies' /* test run class */,
		'SAHSULAND test 4 study_id 1 example - trgf_rif40_studies - 20200 T_RIF40_STUDIES study % username: % is not USER: % UPDATE' /* test case title */,
		NULL::Text[][] /* results */,
		NULL::XML      /* results_xml */,
		'-20200'       /* pg_error_code_expected */,
		FALSE          /* raise_exception_on_failure */, 
		FALSE          /* expected_result */,
		test_id        /* parent_test_id */,
		'{trigger_fct_t_rif40_studies_checks}'::text[] /* pg debug functions */);		
--
-- State change by another user cannot be tested
--
END;
$$;

--
-- Eof