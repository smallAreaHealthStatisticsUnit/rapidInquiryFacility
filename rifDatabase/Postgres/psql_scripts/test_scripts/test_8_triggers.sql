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
	errors INTEGER:=0;
--
	error_message VARCHAR;
--
	v_sqlstate 	VARCHAR;
	v_context	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN	
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
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #1'', ''EXTRACT_TRIGGER_TEST'', ''MAP_TRIGGER_TEST'', 1 /* Diease mapping */, ''LEVEL1'', ''LEVEL4'', NULL /* FAIL HERE */, 0)',
		'TRIGGER TEST #1: rif40_studies.denom_tab IS NULL',
		NULL::Text[][] 	/* No results for trigger */,
		'P0001' 		/* Expected SQLCODE (P0001 - PGpsql raise_exception (from rif40_error) */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */)) THEN
		errors:=errors+1;
    END IF;	
	IF NOT (rif40_sql_pkg.rif40_sql_test(	
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #1'', ''EXTRACT_TRIGGER_TEST'', ''MAP_TRIGGER_TEST'', 1 /* Diease mapping */, ''LEVEL1'', ''LEVEL4'', ''SAHSULAND_POP'', NULL /* FAIL HERE */)
RETURNING suppression_value',
		'TRIGGER TEST #2: rif40_studies.suppression_value IS NULL',
		'{{0}}'::Text[][] 	/* Results for trigger - DEFAULTED value */,
		NULL 			/* Expected SQLCODE */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */)) THEN
		errors:=errors+1;
    END IF;		
--	
	IF errors = 0 THEN
		RAISE NOTICE 'No test harness errors.';		
	ELSE
		RAISE EXCEPTION 'Test harness errors: %', errors;
	END IF;
EXCEPTION
	WHEN others THEN
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
		GET STACKED DIAGNOSTICS v_context = PG_EXCEPTION_CONTEXT;
		error_message:='Test harness caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||
			'Detail: '||v_detail::VARCHAR||E'\n'||
			'Context: '||v_context::VARCHAR||E'\n'||
			'SQLSTATE: '||v_sqlstate::VARCHAR;
		RAISE EXCEPTION '1: %', error_message;
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
