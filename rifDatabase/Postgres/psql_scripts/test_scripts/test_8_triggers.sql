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
-- Rapid Enquiry Facility (RIF) - Test 8: Trigger test harness (added in alter_7)
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

\echo Test 8: Trigger test harness (added in alter_7)...

\set ndebug_level '''XXXX':debug_level''''
SET rif40.debug_level TO :ndebug_level;
--
-- Start transaction
--
BEGIN;

\dS t_rif40_studies

--
-- Test case a)
--	
DO LANGUAGE plpgsql $$
DECLARE
	c1th CURSOR FOR 
		SELECT *
		  FROM rif40_studies 
		 WHERE study_name LIKE 'TRIGGER TEST%';
	c2th CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c3th CURSOR FOR 
		SELECT COUNT(study_id) AS total_studies
		  FROM rif40_studies 
		 WHERE study_name LIKE 'TRIGGER TEST%';		
	c1th_rec RECORD;
	c2th_rec RECORD;
	c3th_rec RECORD;	
--
	errors INTEGER:=0;
--
	error_message VARCHAR;
--
	rif40_pkg_functions 		VARCHAR[] := ARRAY[
				'rif40_delete_study', 'rif40_ddl'];
	l_function 			VARCHAR;	
	debug_level		INTEGER;	
--
	v_sqlstate 	VARCHAR;
	v_context	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN	
	OPEN c2th;
	FETCH c2th INTO c2th_rec;
	CLOSE c2th;
--
-- Test parameter
--
	IF c2th_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'test_8_triggers.sql: T1-01: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c2th_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'T8--02: test_8_triggers.sql: debug level parameter="%"', debug_level::Text;
	END IF;
	--
-- Turn on some debug (all BEFORE/AFTER trigger functions for tables containing the study_id column) 
--
    PERFORM rif40_log_pkg.rif40_log_setup();
	IF debug_level IS NULL THEN
		RAISE INFO 'T8--04: NULL debug_level';
		debug_level:=0;
	ELSIF debug_level > 4 THEN
		RAISE EXCEPTION 'test_8_triggers.sql: T8--03: Invalid debug level [0-4]: %', debug_level;
	ELSIF debug_level BETWEEN 1 AND 4 THEN
		RAISE INFO 'T8--04: test_8_triggers.sql: debug_level %', debug_level;
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_pkg_functions LOOP
			RAISE INFO 'T8--05: test_8_triggers.sql: Enable debug for function: %', l_function;
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
		END LOOP;
	ELSE
		RAISE INFO 'T8--04: test_8_triggers.sql: debug_level %', debug_level;
	END IF;
--
-- Clear up old test cases
--
	FOR c1th_rec IN c1th LOOP
		PERFORM rif40_sm_pkg.rif40_delete_study(c1th_rec.study_id);
	END LOOP;
--
	IF NOT (rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][]
		/* Use defaults */)) THEN
        errors:=errors+1;
    END IF;		
--
-- DELIBERATE FAIL: level 3: 01.015.016900 removed
--
	IF (rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 [DELIBERATE FAIL TEST]',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][],
		NULL  /* Expected SQLCODE */, 
		FALSE /* Do not RAISE EXCEPTION on failure */)) THEN
        errors:=errors+1;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
			'DELIBERATE FAIL TEST PASSED.');
    END IF;		

--
-- T_RIF40_STUDIES
--
/*	sahsuland_dev=> \dS t_rif40_studies
                                                       Table "rif40.t_rif40_studies"
          Column          |            Type             |                                     Modifiers
--------------------------+-----------------------------+-----------------------------------------------------------------------------------
 study_id                 | integer                     | not null default (nextval('rif40_study_id_seq'::regclass))::integer
 username                 | character varying(90)       | default "current_user"()
 geography                | character varying(30)       | not null
 project                  | character varying(30)       | not null
 study_name               | character varying(200)      | not null
 summary                  | character varying(200)      |
 description              | character varying(2000)     |
 other_notes              | character varying(2000)     |
 extract_table            | character varying(30)       | not null
 map_table                | character varying(30)       | not null
 study_date               | timestamp without time zone | default ('now'::text)::timestamp without time zone
 study_type               | smallint                    | not null
 study_state              | character varying(1)        | default 'C'::character varying
 comparison_geolevel_name | character varying(30)       | not null
 study_geolevel_name      | character varying(30)       | not null
 denom_tab                | character varying(30)       | not null
 direct_stand_tab         | character varying(30)       |
 suppression_value        | smallint                    | not null
 extract_permitted        | smallint                    | default 0
 transfer_permitted       | smallint                    | default 0
 authorised_by            | character varying(90)       |
 authorised_on            | timestamp without time zone |
 authorised_notes         | character varying(200)      |
 audsid                   | character varying(90)       | default sys_context('USERENV'::character varying, 'SESSIONID'::character varying)
Indexes:
    "t_rif40_studies_pk" PRIMARY KEY, btree (study_id)
    "t_rif40_extract_table_uk" UNIQUE, btree (extract_table)
    "t_rif40_map_table_uk" UNIQUE, btree (map_table)
Check constraints:
    "t_rif40_stud_extract_perm_ck" CHECK (extract_permitted = ANY (ARRAY[0, 1]))
    "t_rif40_stud_transfer_perm_ck" CHECK (transfer_permitted = ANY (ARRAY[0, 1]))
    "t_rif40_studies_study_state_ck" CHECK (study_state::text = ANY (ARRAY['C'::character varying, 'V'::character varying, 'E'::character varying, 'R'::character varying, 'U'::char
acter varying]::text[]))
    "t_rif40_studies_study_type_ck" CHECK (study_type = ANY (ARRAY[1, 11, 12, 13, 14, 15]))
Foreign-key constraints:
    "rif40_studies_project_fk" FOREIGN KEY (project) REFERENCES t_rif40_projects(project)
    "t_rif40_std_comp_geolevel_fk" FOREIGN KEY (geography, comparison_geolevel_name) REFERENCES t_rif40_geolevels(geography, geolevel_name)
    "t_rif40_std_study_geolevel_fk" FOREIGN KEY (geography, study_geolevel_name) REFERENCES t_rif40_geolevels(geography, geolevel_name)
    "t_rif40_stud_denom_tab_fk" FOREIGN KEY (denom_tab) REFERENCES rif40_tables(table_name)
    "t_rif40_stud_direct_stand_fk" FOREIGN KEY (direct_stand_tab) REFERENCES rif40_tables(table_name)
    "t_rif40_studies_geography_fk" FOREIGN KEY (geography) REFERENCES rif40_geographies(geography)
 */
--
-- NULL tests
--
	IF NOT (rif40_sql_pkg.rif40_sql_test(	
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #1'', ''EXTRACT_TRIGGER_TEST_1'', ''MAP_TRIGGER_TEST_1'', 1 /* Diease mapping */, ''LEVEL1'', ''LEVEL4'', NULL /* FAIL HERE */, 0)',
		'TRIGGER TEST #1: rif40_studies.denom_tab IS NULL',
		NULL::Text[][] 	/* No results for trigger */,
		'P0001' 		/* Expected SQLCODE (P0001 - PGpsql raise_exception (from rif40_error) */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */)) THEN
		errors:=errors+1;
    END IF;	
	IF NOT (rif40_sql_pkg.rif40_sql_test(	
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #2'', ''EXTRACT_TRIGGER_TEST_2'', ''MAP_TRIGGER_TEST_2'', 1 /* Diease mapping */, ''LEVEL1'', ''LEVEL4'', ''SAHSULAND_POP'', NULL /* FAIL HERE */)
RETURNING suppression_value',
		'TRIGGER TEST #2: rif40_studies.suppression_value IS NULL',
		'{{0}}'::Text[][] 	/* Results for trigger - DEFAULTED value */,
		NULL 			/* Expected SQLCODE */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */)) THEN
		errors:=errors+1;
	ELSE	
		PERFORM rif40_sql_pkg.rif40_ddl('DELETE FROM rif40_studies WHERE study_name = ''TRIGGER TEST #2''');
    END IF;		
	
--
-- Check no TEST TIRGGER studies actually created 
--
	OPEN c3th;
	FETCH c3th INTO c3th_rec;
	CLOSE c3th;
	IF c3th_rec.total_studies > 0 THEN
		PERFORM rif40_sql_pkg.rif40_method4('SELECT study_id, study_name FROM rif40_studies  WHERE study_name LIKE ''TRIGGER TEST%''', 
			'Check no TEST TIRGGER studies actually created');
		RAISE EXCEPTION 'T8--06: test_8_triggers.sql: % TEST TIRGGER studies have been created', c3th_rec.total_studies;
	ELSE
		RAISE NOTICE 'T8--11: test_8_triggers.sql: No TEST TIRGGER studies actually created.';		
	END IF;
	
--	
	IF errors = 0 THEN
		RAISE NOTICE 'T8--09: test_8_triggers.sql: No test harness errors.';		
	ELSE
		RAISE EXCEPTION 'T8--10: test_8_triggers.sql: Test harness errors: %', errors;
	END IF;
EXCEPTION
	WHEN others THEN
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
		GET STACKED DIAGNOSTICS v_context = PG_EXCEPTION_CONTEXT;
		error_message:='T8--07: test_8_triggers.sql: Test harness caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||
			'Detail: '||v_detail::VARCHAR||E'\n'||
			'Context: '||v_context::VARCHAR||E'\n'||
			'SQLSTATE: '||v_sqlstate::VARCHAR;
		RAISE EXCEPTION 'T8--08: test_8_triggers.sql: 1: %', error_message;
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
-- End single transaction
--
END;

\echo Test 8: Trigger test harness (added in alter_7) OK    

--
-- Eof
--
-- Eof
