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
-- Rapid Enquiry Facility (RIF) - Test harness implementation
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
-- Error codes:
-- rif40_sql_test:										71050 to 71099
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing
\set VERBOSITY terse
--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20900: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_test_sql_template(test_stmt VARCHAR, test_case_title VARCHAR)
RETURNS SETOF VARCHAR
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_test_sql_template()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, test case title  
Returns:	PL/pgsql template code 
Description: Generate PL/pgsql template code, e.g.

SELECT rif40_sql_pkg._rif40_test_sql_template(
	'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
	'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200') AS template;
	
template
--------	
	IF NOT (PERFORM rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][])) THEN
		errors:=errors+1;
	END IF;
		
 */
DECLARE 

BEGIN
	RETURN NEXT E'\t'||'IF NOT (PERFORM rif40_sql_pkg.rif40_sql_test(';
	RETURN NEXT E'\t'||E'\t'||''''||REPLACE(test_stmt, '''', '''''')||'''';
	RETURN NEXT E'\t'||E'\t'||''''||REPLACE(test_case_title, '''', '''''')||'''';		
	RETURN NEXT E'\t'||E'\t'||'}''::Text[][])) THEN';
	RETURN NEXT E'\t'||E'\t'||'errors:=errors+1;';
	RETURN NEXT E'\t'||'END IF;'
--
	RETURN;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_test_sql_template(VARCHAR, VARCHAR) IS 'Function: 	_rif40_test_sql_template()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, test case title  
Returns:	PL/pgsql template code 
Description: Generate PL/pgsql template code, e.g.

SELECT rif40_sql_pkg._rif40_test_sql_template(
	''SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''''01.015.016900'''', ''''01.015.016200'''') ORDER BY level4'',
	''Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200'') AS template;
	
template
--------	
	IF NOT (PERFORM rif40_sql_pkg.rif40_sql_test(
		''SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''''01.015.016900'''', ''''01.015.016200'''') ORDER BY level4'',
		''Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200'',
		''{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}''::Text[][])) THEN
		errors:=errors+1;
	END IF;
	
';
 
--
-- Eof