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
-- Rapid Enquiry Facility (RIF) - Test 7: Middleware tests 2 for alter 2
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

\echo Test 7: Middleware tests 2 for alter 2...

\set ndebug_level '''XXXX':debug_level''''
SET rif40.debug_level TO :ndebug_level;
--
-- Start transaction
--
BEGIN;
DO LANGUAGE plpgsql $$
DECLARE
	c4sm CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c4sm_rec RECORD;
--
-- Functions to enable debug for
--
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_ddl', 
		'rif40_getAllAttributesForGeoLevelAttributeTheme', 'rif40_GetMapAreaAttributeValue',
		'rif40_GetMapAreaAttributeValue','rif40_CreateMapAreaAttributeSource'];
	l_function				VARCHAR;
--
	debug_level				INTEGER;
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
END;
$$;

--
-- Test middle ware interface functions
--
\pset title 'rif40_getAllAttributesForGeoLevelAttributeTheme SAHSU level 4 extract'
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'extract');

\pset title 'rif40_CreateMapAreaAttributeSource: c4getallatt4theme_4'
WITH a AS (
	SELECT MIN(attribute_source) AS min_attribute_source
	  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'extract')
)
SELECT b.* 
  FROM a, rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_4', 
		'SAHSU', 'LEVEL4', 'extract', a.min_attribute_source) b;
		
\pset title 'rif40_GetMapAreaAttributeValue: c4getallatt4theme_4'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_4' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_4;
/*
                                             rif40_GetMapAreaAttributeValue: c4getallatt4theme_4
     area_id     | gid |     gid_rowindex      | year | study_or_comparison | study_id | band_id | sex | age_group | ses | inv_1 | t
otal_pop
-----------------+-----+-----------------------+------+---------------------+----------+---------+-----+-----------+-----+-------+--
---------
 01.001.000100.1 |   1 | 0000000001_0000000001 | 1989 | S                   |        9 |       1 |   1 |         0 | 4   |     0 |
      34
 01.001.000100.1 |   1 | 0000000001_0000000002 | 1989 | S                   |        9 |       1 |   1 |         1 | 4   |     0 |
      34
 01.001.000100.1 |   1 | 0000000001_0000000003 | 1989 | S                   |        9 |       1 |   1 |         2 | 4   |     0 |
      34
 01.001.000100.1 |   1 | 0000000001_0000000004 | 1989 | S                   |        9 |       1 |   1 |         3 | 4   |     0 |
      34
 01.001.000100.1 |   1 | 0000000001_0000000005 | 1989 | S                   |        9 |       1 |   1 |         4 | 4   |     0 |
      34
(5 rows)
 */

\pset title 'rif40_getAllAttributesForGeoLevelAttributeTheme SAHSU level 4 results'
SELECT * 
  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'results');

\pset title 'rif40_CreateMapAreaAttributeSource SAHSU level 4 results'
WITH a AS (
	SELECT MIN(attribute_source) AS min_attribute_source
	  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme('SAHSU', 'LEVEL4', 'results')
)
SELECT b.* 
  FROM a, rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
		'c4getallatt4theme_5', 
		'SAHSU', 'LEVEL4', 'results', a.min_attribute_source) b;
		
\pset title 'rif40_GetMapAreaAttributeValue: c4getallatt4theme_5'
SELECT * 
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_5' /* Temporary table */);
FETCH FORWARD 5 IN c4getallatt4theme_5;
/*
                                                            rif40_GetMapAreaAttributeValue: c4getallatt4theme_5
     area_id     | gid |     gid_rowindex      | username | study_id | inv_id | band_id | genders | direct_standardisation | adjuste
d | observed | expected | lower95 | upper95 | relative_risk | smoothed_relative_risk | posterior_probability | posterior_probability
_upper95 | posterior_probability_lower95 | residual_relative_risk | residual_rr_lower95 | residual_rr_upper95 | smoothed_smr | smoot
hed_smr_lower95 | smoothed_smr_upper95
-----------------+-----+-----------------------+----------+----------+--------+---------+---------+------------------------+--------
--+----------+----------+---------+---------+---------------+------------------------+-----------------------+----------------------
---------+-------------------------------+------------------------+---------------------+---------------------+--------------+------
----------------+----------------------
 01.001.000100.1 |   1 | 0000000001_0000000001 | pch      |        9 |      9 |       1 |       1 |                      0 |
0 |       38 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
 01.001.000100.1 |   1 | 0000000001_0000000002 | pch      |        9 |      9 |       1 |       2 |                      0 |
0 |       36 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
 01.001.000100.1 |   1 | 0000000001_0000000003 | pch      |        9 |      9 |       1 |       3 |                      0 |
0 |       74 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
 01.001.000100.2 |   2 | 0000000002_0000000001 | pch      |        9 |      9 |       2 |       1 |                      0 |
0 |       40 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
 01.001.000100.2 |   2 | 0000000002_0000000002 | pch      |        9 |      9 |       2 |       2 |                      0 |
0 |       18 |          |         |         |               |                        |                       |
         |                               |                        |                     |                     |              |
                |
(5 rows)
 */

--
-- End single transaction
--
END;

\echo Test 7: Middleware tests 2 for alter 2 OK    

--
-- Eof
