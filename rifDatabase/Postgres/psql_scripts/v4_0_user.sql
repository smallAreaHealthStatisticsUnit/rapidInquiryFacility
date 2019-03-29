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
-- Rapid Enquiry Facility (RIF) - creates user specific objects
--
-- Functionality replaced by rif40_sql_pkg.rif40_startup() function
--
-- Now used to test user settings
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
\set ECHO :echo
\set ON_ERROR_STOP ON

--
-- Run RIF startup script
--
\set VERBOSITY :verbosity
\set ndebug_level '''XXXX':debug_level''''
SET rif40.debug_level TO :ndebug_level;

DO LANGUAGE plpgsql $$
DECLARE
	c4us CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.debug_level') AS debug_level;
	c4us_rec 		RECORD;
--
	sql_stmt 		VARCHAR;
	debug_level		INTEGER;
BEGIN
	BEGIN
		OPEN c4us;
		FETCH c4us INTO c4us_rec;
		CLOSE c4us;
	EXCEPTION
		WHEN others THEN /* Try defaulting settings */
			SET rif40.debug = '';
			SET rif40.send_debug_to_info = 'off';
--
			OPEN c4us;
			FETCH c4us INTO c4us_rec;
			CLOSE c4us;
--
		RAISE INFO 'v4_0_user() DEFAULTED send DEBUG to INFO: %; debug function list: [%]', 
			c1lgs_rec.send_debug_to_info, c1lgs_rec.debug;
	END;
--
-- Test parameter
--
	IF c4us_rec.debug_level IN ('XXXX', 'XXXX:debug_level') THEN
		RAISE EXCEPTION 'v4_0_user.sql() C209xx: No -v testuser=<debug level> parameter';	
	ELSE
		debug_level:=LOWER(SUBSTR(c4us_rec.debug_level, 5))::INTEGER;
		RAISE INFO 'v4_0_user.sql() debug level parameter="%"', debug_level::Text;
	END IF;
--
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_ddl_checks:DEBUG'||debug_level::Text);
	PERFORM rif40_log_pkg.rif40_log_setup();
	PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Force rebuild of user objects
--
	sql_stmt:='DROP VIEW IF EXISTS '||USER||'.rif40_user_version';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
    PERFORM rif40_sql_pkg.rif40_startup();
--	IF USER != 'rif40' THEN
--	        PERFORM rif40_sql_pkg.rif40_ddl_checks();
--	END IF;
	PERFORM rif40_log_pkg.rif40_remove_from_debug('rif40_ddl_checks');
END;
$$;
/*
SELECT geography, numerator_table, theme_description, denominator_table, automatic 
  FROM rif40_num_denom; 
 */
/*
 geography |      numerator_table       |            theme_description             |      denominator_table      | automatic 
-----------+----------------------------+------------------------------------------+-----------------------------+-----------
 EW01      | RIF_CANC_EW_74_ON_OA2001   | Cancer Incidence                         | V_EW01_RIF_POP_ASG_1_OA2001 |         1
 EW01      | RIF_DTHS_EW_81_ON_OA2001   | Mortality                                | V_EW01_RIF_POP_ASG_1_OA2001 |         1
 EW01      | RIF_HES_E_89_ON_OA2001     | Hospital Episode Statistics              | V_EW01_RIF_POP_ASG_1_OA2001 |         1
 EW01      | RIF_PH_CANC_EW_74_OA2001   | Cancer Incidence                         | V_EW01_RIF_POP_ASG_1_OA2001 |         1
 EW01      | RIF_PHDTHS_EW_81_ON_OA2001 | Mortality                                | V_EW01_RIF_POP_ASG_1_OA2001 |         1
 SAHSU     | SAHSULAND_CANCER           | SAHSU land cancer incidence example data | SAHSULAND_POP               |         1
 UK91      | RIF_CANC_EW_74_ON_ED91     | Cancer Incidence                         | UK91_RIF_POP_ASG_1_ED91     |         1
 UK91      | RIF_DTHS_EW_81_ON_ED91     | Mortality                                | UK91_RIF_POP_ASG_1_ED91     |         1
 UK91      | RIF_HES_E_89_ON_ED91       | Hospital Episode Statistics              | UK91_RIF_POP_ASG_1_ED91     |         1
 UK91      | RIF_PH_CANC_EW_74_ED91     | Cancer Incidence                         | UK91_RIF_POP_ASG_1_ED91     |         1
 UK91      | RIF_PHDTHS_EW_81_ON_ED91   | Mortality                                | UK91_RIF_POP_ASG_1_ED91     |         1
(11 rows)

 */
SELECT geography AS geog, 
       numerator_owner AS n_owner, numerator_table, is_numerator_resolvable AS n_resolv, n_num_denom_validated AS n_nd_valid, 
       denominator_owner AS d_owner, denominator_table, is_denominator_resolvable AS d_resolv, d_num_denom_validated AS d_nd_valid, 
       automatic AS auto, n_fdw_date_created,
       CASE WHEN n_num_denom_validated = 1 AND d_num_denom_validated = 1 THEN 1 ELSE 0 END AS ok
 FROM rif40_num_denom_errors
 ORDER BY n_num_denom_validated DESC, d_num_denom_validated DESC, is_numerator_resolvable DESC, is_denominator_resolvable DESC, denominator_table, numerator_table;
/*
 geog  | n_owner |      numerator_table       | n_resolv | n_nd_valid | d_owner |      denominator_table      | d_resolv | d_nd_valid | auto |    n_fdw_date_created     | ok 
-------+---------+----------------------------+----------+------------+---------+-----------------------------+----------+------------+------+---------------------------+----
 SAHSU | rif40   | SAHSULAND_CANCER           |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 |                           |  1
 UK91  | peterh  | RIF_CANC_EW_74_ON_ED91     |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 UK91  | peterh  | RIF_DTHS_EW_81_ON_ED91     |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 UK91  | peterh  | RIF_HES_E_89_ON_ED91       |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 UK91  | peterh  | RIF_PH_CANC_EW_74_ED91     |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 UK91  | peterh  | RIF_PHDTHS_EW_81_ON_ED91   |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 EW01  | peterh  | RIF_CANC_EW_74_ON_OA2001   |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 EW01  | peterh  | RIF_DTHS_EW_81_ON_OA2001   |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 EW01  | peterh  | RIF_HES_E_89_ON_OA2001     |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 EW01  | peterh  | RIF_PH_CANC_EW_74_OA2001   |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 EW01  | peterh  | RIF_PHDTHS_EW_81_ON_OA2001 |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  1
 UK91  | peterh  | RIF_CANC_EW_74_ON_ED91     |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_CANC_EW_74_ON_OA2001   |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_DTHS_EW_81_ON_ED91     |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_DTHS_EW_81_ON_OA2001   |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_HES_E_89_ON_ED91       |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_HES_E_89_ON_OA2001     |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PH_CANC_EW_74_ED91     |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PH_CANC_EW_74_OA2001   |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PHDTHS_EW_81_ON_ED91   |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PHDTHS_EW_81_ON_OA2001 |        1 |          1 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_CANC_EW_74_ON_OA2001   |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_DTHS_EW_81_ON_OA2001   |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_HES_E_89_ON_OA2001     |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PH_CANC_EW_74_OA2001   |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PHDTHS_EW_81_ON_OA2001 |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | rif40   | SAHSULAND_CANCER           |        1 |          1 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 |                           |  0
 UK91  | peterh  | RIF_CANC_EW_74_ON_ED91     |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_DTHS_EW_81_ON_ED91     |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_HES_E_89_ON_ED91       |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PH_CANC_EW_74_ED91     |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PHDTHS_EW_81_ON_ED91   |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | rif40   | SAHSULAND_CANCER           |        1 |          1 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 |                           |  0
 SAHSU | peterh  | RIF_CANC_EW_74_ON_ED91     |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_CANC_EW_74_ON_OA2001   |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_DTHS_EW_81_ON_ED91     |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_DTHS_EW_81_ON_OA2001   |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_HES_E_89_ON_ED91       |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_HES_E_89_ON_OA2001     |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PH_CANC_EW_74_ED91     |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PH_CANC_EW_74_OA2001   |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PHDTHS_EW_81_ON_ED91   |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PHDTHS_EW_81_ON_OA2001 |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_CANC_EW_74_ON_OA2001   |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_DTHS_EW_81_ON_OA2001   |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_HES_E_89_ON_OA2001     |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PH_CANC_EW_74_OA2001   |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PHDTHS_EW_81_ON_OA2001 |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | rif40   | SAHSULAND_CANCER           |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          1 |    1 |                           |  0
 EW01  | peterh  | RIF_CANC_EW_74_ON_ED91     |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_DTHS_EW_81_ON_ED91     |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_HES_E_89_ON_ED91       |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PH_CANC_EW_74_ED91     |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PHDTHS_EW_81_ON_ED91   |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | rif40   | SAHSULAND_CANCER           |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          1 |    1 |                           |  0
 EW01  | peterh  | RIF_CANC_EW_74_ON_ED91     |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_CANC_EW_74_ON_OA2001   |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_DTHS_EW_81_ON_ED91     |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_DTHS_EW_81_ON_OA2001   |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_HES_E_89_ON_ED91       |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_HES_E_89_ON_OA2001     |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PH_CANC_EW_74_ED91     |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PH_CANC_EW_74_OA2001   |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PHDTHS_EW_81_ON_ED91   |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PHDTHS_EW_81_ON_OA2001 |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | rif40   | SAHSULAND_CANCER           |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 |                           |  0
 UK91  | rif40   | SAHSULAND_CANCER           |        1 |          0 | rif40   | SAHSULAND_POP               |        1 |          0 |    1 |                           |  0
 SAHSU | peterh  | RIF_CANC_EW_74_ON_ED91     |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_CANC_EW_74_ON_ED91     |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_CANC_EW_74_ON_OA2001   |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_DTHS_EW_81_ON_ED91     |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_DTHS_EW_81_ON_ED91     |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_DTHS_EW_81_ON_OA2001   |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_HES_E_89_ON_ED91       |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_HES_E_89_ON_ED91       |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_HES_E_89_ON_OA2001     |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PH_CANC_EW_74_ED91     |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PH_CANC_EW_74_ED91     |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PH_CANC_EW_74_OA2001   |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PHDTHS_EW_81_ON_ED91   |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | peterh  | RIF_PHDTHS_EW_81_ON_ED91   |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PHDTHS_EW_81_ON_OA2001 |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 EW01  | rif40   | SAHSULAND_CANCER           |        1 |          0 | pop     | UK91_RIF_POP_ASG_1_ED91     |        1 |          0 |    1 |                           |  0
 SAHSU | peterh  | RIF_CANC_EW_74_ON_ED91     |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_CANC_EW_74_ON_OA2001   |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_CANC_EW_74_ON_OA2001   |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_DTHS_EW_81_ON_ED91     |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_DTHS_EW_81_ON_OA2001   |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_DTHS_EW_81_ON_OA2001   |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_HES_E_89_ON_ED91       |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_HES_E_89_ON_OA2001     |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_HES_E_89_ON_OA2001     |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PH_CANC_EW_74_ED91     |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PH_CANC_EW_74_OA2001   |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PH_CANC_EW_74_OA2001   |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PHDTHS_EW_81_ON_ED91   |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 SAHSU | peterh  | RIF_PHDTHS_EW_81_ON_OA2001 |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | peterh  | RIF_PHDTHS_EW_81_ON_OA2001 |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 | 2013-03-11 12:41:02.90311 |  0
 UK91  | rif40   | SAHSULAND_CANCER           |        1 |          0 | pop     | V_EW01_RIF_POP_ASG_1_OA2001 |        1 |          0 |    1 |                           |  0
(99 rows)

 */

-- RESET search_path;
-- SELECT CURRENT_SETTING('rif40.send_debug_to_info') AS send_debug_to_info, CURRENT_SETTING('rif40.debug') AS debug;

--
-- Eof
