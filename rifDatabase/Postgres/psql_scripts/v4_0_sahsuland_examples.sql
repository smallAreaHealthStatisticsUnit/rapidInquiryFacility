-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/02/28 10:54:02 $
-- Type: Postgres PSQL script
-- $RCSfile: v4_0_sahsuland_examples.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/psql_scripts/v4_0_sahsuland_examples.sql,v $
-- $Revision: 1.7 $
-- $Id: v4_0_sahsuland_examples.sql,v 1.7 2014/02/28 10:54:02 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) -  Create SAHSULAND example studies
--
-- Copyright:
--
-- The RIF is free software; you can redistribute it and/or modify it under
-- the terms of the GNU General Public License as published by the Free
-- Software Foundation; either version 2, or (at your option) any later
-- version.
--
-- The RIF is distributed in the hope that it will be useful, but WITHOUT ANY
-- WARRANTY; without even the implied warranty of MERCHANTABILITY or
-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
-- for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this file; see the file LICENCE.  If not, write to:
--
-- UK Small Area Health Statistics Unit,
-- Dept. of Epidemiology and Biostatistics
-- Imperial College School of Medicine (St. Mary's Campus),
-- Norfolk Place,
-- Paddington,
-- London, W2 1PG
-- United Kingdom
--
-- The RIF uses Oracle 11g, PL/SQL, PostGres and PostGIS as part of its implementation.
--
-- Oracle11g, PL/SQL and Pro*C are trademarks of the Oracle Corporation.
--
-- All terms mentioned in this software and supporting documentation that are known to be trademarks
-- or service marks have been appropriately capitalised. Imperial College cannot attest to the accuracy
-- of this information. The use of a term in this software or supporting documentation should NOT be
-- regarded as affecting the validity of any trademark or service mark.
--
-- Summary of functions/procedures:
--
-- To be added
--
-- Error handling strategy:
--
-- Output and logging procedures do not HANDLE or PROPAGATE errors. This makes them safe to use
-- in package initialisation and NON recursive.
--
-- References:
--
-- 	None
--
-- Dependencies:
--
--	Packages: None
--
-- 	<This should include: packages, non packages procedures and functions, tables, views, objects>
--
-- Portability:
--
--	Linux, Windows 2003/2008, Oracle 11gR1
--
-- Limitations:
--
-- Change log:
--
-- $Log: v4_0_sahsuland_examples.sql,v $
-- Revision 1.7  2014/02/28 10:54:02  peterh
--
-- Further work on transfer of SAHSUland to github. sahsuland build scripts Ok, UK91 geog added.
--
-- Revision 1.6  2014/02/27 11:29:40  peterh
--
-- About to test isolated code tree for trasnfer to Github/public network
--
-- Revision 1.5  2014/02/11 14:56:25  peterh
-- Baseline after simplifcation integration and testing of new Geo setup scripts
--
-- Revision 1.4  2014/01/22 16:53:24  peterh
-- Baseline prior to extending join to exterior lines in simplification modules
--
-- Revision 1.3  2014/01/14 08:59:49  peterh
--
-- Baseline prior to adding multipolygon support for simplification
--
-- Revision 1.2  2013/09/18 15:20:32  peterh
-- Checkin at end of 6 week RIF focus. Got as far as SAHSULAND run study to completion for observed only
--
-- Revision 1.1  2013/09/02 14:08:34  peterh
--
-- Baseline after full trigger implmentation
--
-- Revision 1.2  2013/03/14 17:35:39  peterh
-- Baseline for TX to laptop
--
--
\set ON_ERROR_STOP ON
\echo Creating SAHSULAND example studies...
\set ECHO all
\timing

--
-- Comment out this for more debug
--
\set VERBOSITY terse
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
		 WHERE study_name = 'SAHSULAND test example';
	c1sm_rec RECORD;
	c2sm_rec RECORD;
	c3sm_rec RECORD;
--
	investigation_icd_array		VARCHAR[]:=array['"icd" LIKE ''C34%'' OR "icd" LIKE ''162%'''];	
	investigation_desc_array	VARCHAR[]:=array['Lung cancer'];
	covariate_array			VARCHAR[]:=array['SES'];	
--
	rif40_sm_pkg_functions 		VARCHAR[] := ARRAY['rif40_verify_state_change', 
						'rif40_run_study', 'rif40_ddl', 'rif40_study_ddl_definer', 
						'rif40_create_insert_statement', 'rif40_execute_insert_statement', 
						'rif40_compute_results', 'rif40_startup'];
	l_function 			VARCHAR;
	i				INTEGER:=0;
--
	sql_stmt			VARCHAR[];
BEGIN
--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
        PERFORM rif40_log_pkg.rif40_log_setup();
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
	FOR c1sm_rec IN c1sm LOOP
		IF c1sm_rec.function IS NOT NULL THEN
			RAISE INFO 'Enable debug for % trigger function: % on table: %',
				c1sm_rec.action_timing, c1sm_rec.function, c1sm_rec.table_name;
		        PERFORM rif40_log_pkg.rif40_add_to_debug(c1sm_rec.function||':DEBUG1');
		ELSE
			RAISE WARNING 'No trigger function found for table: %', c1sm_rec.table_name;
		END IF;
	END LOOP;
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
-- Enabled debug on select rif40_sm_pkg functions
--
	FOREACH l_function IN ARRAY rif40_sm_pkg_functions LOOP
		RAISE INFO 'Enable debug for function: %', l_function;
		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
	END LOOP;
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
		c2sm_rec.level3_array 		/* Geolevel selection array */,
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
--
	sql_stmt[1]:='DROP TABLE IF EXISTS sahsuland_example_extract';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE IF EXISTS sahsuland_example_map';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE IF EXISTS sahsuland_example_bands';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE TEMPORARY TABLE sahsuland_example_extract'||E'\n'||
'AS'||E'\n'||
'SELECT * FROM rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_extract';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE TEMPORARY TABLE sahsuland_example_map'||E'\n'||
'AS'||E'\n'||
'SELECT * FROM rif_studies.s'||currval('rif40_study_id_seq'::regclass)||'_map';
	sql_stmt[array_length(sql_stmt, 1)+1]:='CREATE TEMPORARY TABLE sahsuland_example_bands'||E'\n'||
'AS'||E'\n'||
'SELECT * FROM rif40_study_areas WHERE study_id = '||currval('rif40_study_id_seq'::regclass)::VARCHAR||' ORDER BY band_id';
--
	IF rif40_sm_pkg.rif40_run_study(currval('rif40_study_id_seq'::regclass)::INTEGER) THEN
		RAISE INFO 'Study: % run OK', currval('rif40_study_id_seq'::regclass)::VARCHAR;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	ELSE
		RAISE WARNING 'Study: % run failed; see trace', currval('rif40_study_id_seq'::regclass)::VARCHAR;
	END IF;
END;
$$;
SELECT * FROM rif40_comparison_areas WHERE study_id = currval('rif40_study_id_seq'::regclass);
SELECT * FROM rif40_study_areas WHERE study_id = currval('rif40_study_id_seq'::regclass);
SELECT * FROM rif40_inv_covariates WHERE study_id = currval('rif40_study_id_seq'::regclass);
SELECT * FROM rif40_study_sql_log WHERE study_id = currval('rif40_study_id_seq'::regclass);

WITH a AS (
	SELECT rif40_log_pkg.rif40_get_debug(CURRENT_SETTING('rif40.debug')) AS debug /* Current debug */
)
SELECT (a.debug).function_name AS function_name,
       (a.debug).debug AS debug,
       SUBSTR((a.debug).debug::TEXT, 6)::INTEGER AS debug_level
  FROM a;

\dS+ sahsuland_example_extract
\dS+ sahsuland_example_map
--
SELECT COUNT(area_id) AS total FROM sahsuland_example_extract;
SELECT COUNT(band_id) AS total FROM sahsuland_example_map;
SELECT COUNT(band_id) AS total FROM sahsuland_example_bands;

-- Check study really has run by dumping out data
--
\copy ( SELECT * FROM sahsuland_example_extract) to ../example_data/sahsuland_example_extract.csv WITH CSV HEADER
\copy ( SELECT * FROM sahsuland_example_map) to ../example_data/sahsuland_example_map.csv WITH CSV HEADER
\copy ( SELECT * FROM sahsuland_example_bands) to ../example_data/sahsuland_example_bands.csv WITH CSV HEADER

--
-- Clone delete test
--
DO LANGUAGE plpgsql $$
DECLARE
	old_study_id	INTEGER:=currval('rif40_study_id_seq'::regclass);
	new_study_id	INTEGER;
--
	rif40_sm_pkg_functions 		VARCHAR[] := ARRAY['rif40_verify_state_change', 
						'rif40_run_study'];
	l_function 			VARCHAR;
BEGIN
--
-- Enabled debug on select rif40_sm_pkg functions
--
        PERFORM rif40_log_pkg.rif40_log_setup();
    	PERFORM rif40_log_pkg.rif40_send_debug_to_info(FALSE);
	FOREACH l_function IN ARRAY rif40_sm_pkg_functions LOOP
--		RAISE INFO 'Enable debug for function: %', l_function;
--		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
		PERFORM rif40_log_pkg.rif40_remove_from_debug(l_function);
	END LOOP;
--
	new_study_id:=rif40_sm_pkg.rif40_clone_study(old_study_id);
	IF NOT rif40_sm_pkg.rif40_run_study(new_study_id) THEN
		RAISE WARNING 'Study: % run failed; see trace', new_study_id::VARCHAR;
		PERFORM rif40_sm_pkg.rif40_reset_study(new_study_id);
		IF NOT rif40_sm_pkg.rif40_run_study(new_study_id) THEN
			RAISE WARNING 'Study: % run failed again; see trace', new_study_id::VARCHAR;
		END IF;	
	ELSE
		RAISE INFO 'Study: % run OK', new_study_id::VARCHAR;
	
		PERFORM rif40_sm_pkg.rif40_delete_study(new_study_id);
		RAISE INFO 'Cloned study: % as new study %, then deleted',
			old_study_id::VARCHAR,
			new_study_id::VARCHAR;
	END IF;
END;
$$;
--
-- End single transaction
--
END;

\set VERBOSITY default

\echo Created SAHSULAND example studies.
--
-- Eof
