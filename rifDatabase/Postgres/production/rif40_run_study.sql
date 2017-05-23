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

\set ECHO all
\set ON_ERROR_STOP on
\set VERBOSITY terse

\echo test 4: Creating SAHSULAND example study 1...

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
		SELECT array_agg(sahsu_grd_level3) AS level3_array FROM lookup_sahsu_grd_level3;
	c3sm CURSOR FOR /* Old studies to delete - not study 1 */
		SELECT study_id
		  FROM rif40_studies
		 WHERE study_name = 'SAHSULAND test 4 study_id 1 example'
		   AND username   = USER
		   AND study_id > 1;
		   
--
	c1sm_rec RECORD;
	c2sm_rec RECORD;
	c3sm_rec RECORD;
--
	condition_array				VARCHAR[4][2]:='{{"SAHSULAND_ICD", "C34", NULL, NULL}, {"SAHSULAND_ICD", "162", "1629", NULL}}';	
										 /* investigation ICD conditions 2 dimensional array (i.e. matrix); 4 columnsxN rows:
											outcome_group_name, min_condition, max_condition, predefined_group_name */
	investigation_desc_array	VARCHAR[]:=array['Lung cancer'];
	covariate_array				VARCHAR[]:=array['SES'];	
--
	rif40_sm_pkg_functions 		VARCHAR[] := ARRAY['rif40_verify_state_change', 
						'rif40_run_study', 'rif40_ddl', 'rif40_study_ddl_definer', 
						'rif40_create_insert_statement', 'rif40_execute_insert_statement', 
						'rif40_compute_results', 'rif40_startup', 'rif40_GetAdjacencyMatrix'];
--
	l_function 		VARCHAR;
	i				INTEGER:=0;
--
	sql_stmt		VARCHAR[];
	debug_level		INTEGER=0;
	study_ran_ok	BOOLEAN;
BEGIN
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
	RAISE INFO 'test_4_study_id_1.sql: T4--07: Call init function rif40_sql_pkg.rif40_startup();';
	PERFORM rif40_sql_pkg.rif40_startup();
--
-- Get "SELECTED" study geolevels
--
	OPEN c2sm;
	FETCH c2sm INTO c2sm_rec;
	CLOSE c2sm;

--
-- Delete old test studies
--
	RAISE INFO 'test_4_study_id_1.sql: T4--08: Delete old test studies (NOT study 1)';
	FOR c3sm_rec IN c3sm LOOP
		i:=i+1;
		RAISE INFO 'test_4_study_id_1.sql: T4--09: Delete of SAHSULAND test example study: %', c3sm_rec.study_id::VARCHAR;
		PERFORM rif40_sm_pkg.rif40_delete_study(c3sm_rec.study_id);
	END LOOP;

--
-- Create new test study
--
	RAISE INFO 'test_4_study_id_1.sql: T4--10: Create new test study';
	PERFORM rif40_sm_pkg.rif40_create_disease_mapping_example(
		'SAHSULAND'::VARCHAR 		/* Geography */,
		'SAHSU_GRD_LEVEL1'::VARCHAR	/* Geolevel view */,
		'01'::VARCHAR				/* Geolevel area */,
		'SAHSU_GRD_LEVEL4'::VARCHAR	/* Geolevel map */,
		'SAHSU_GRD_LEVEL3'::VARCHAR	/* Geolevel select */,
		c2sm_rec.level3_array 		/* Geolevel selection array */,
		'TEST'::VARCHAR 			/* project */, 
		'SAHSULAND test 4 study_id 1 example'::VARCHAR /* study name */, 
		'POP_SAHSULAND_POP'::VARCHAR 	/* denominator table */, 
		'NUM_SAHSULAND_CANCER'::VARCHAR /* numerator table */,
 		1989						/* year_start */, 
		1996						/* year_stop */,
		condition_array 			/* investigation ICD conditions 2 dimensional array (i.e. matrix); 4 columnsxN rows:
											outcome_group_name, min_condition, max_condition, predefined_group_name */,
		investigation_desc_array 	/* investigation description array */,
		covariate_array				/* covariate array */);

--
-- Execute in USER context
--
	EXECUTE 'SELECT '||USER||'.rif40_run_study(currval(''rif40_study_id_seq''::regclass)::INTEGER, FALSE /* Debug */)' INTO study_ran_ok;
	IF study_ran_ok THEN
		RAISE INFO 'test_4_study_id_1.sql: T4--12: Test 4; Study: % run OK', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	ELSE
		RAISE EXCEPTION 'test_4_study_id_1.sql: T4--13: Test 4; Study: % run failed; see trace', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	END IF;
END;
$$;

--
-- Test rif40_GetAdjacencyMatrix()
--
SELECT * FROM rif40_xml_pkg.rif40_GetAdjacencyMatrix(currval('rif40_study_id_seq'::regclass)::INTEGER) LIMIT 10;

--
-- End transaction
--
END;
	
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