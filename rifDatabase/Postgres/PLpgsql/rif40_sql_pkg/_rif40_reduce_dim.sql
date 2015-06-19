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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_reduce_dim(my_array ANYARRAY)
RETURNS SETOF ANYARRAY
LANGUAGE plpgsql
AS $func$
/*
Function: 	_rif40_reduce_dim()
Parameters:	2D array 
Returns:	Set of 1D array rows
Description:
			Array reduce from two to one dimension as a set of 1D array rows
			Can then be cast to a record. Helper function for rif40_sql_test()
E.g.

	WITH a AS (
		SELECT '{{01,01.015,01.015.016200,01.015.016200.2}
	,{01,01.015,01.015.016200,01.015.016200.3} 
	,{01,01.015,01.015.016200,01.015.016200.4} 
	,{01,01.015,01.015.016900,01.015.016900.1} 
	,{01,01.015,01.015.016900,01.015.016900.2} 
	,{01,01.015,01.015.016900,01.015.016900.3} 
	}'::Text[][] AS res
	)
	SELECT rif40_sql_pkg._rif40_reduce_dim(a.res) AS res
	  FROM a;
		
						res
	-------------------------------------------
	 {01,01.015,01.015.016200,01.015.016200.2}
	 {01,01.015,01.015.016200,01.015.016200.3}
	 {01,01.015,01.015.016200,01.015.016200.4}
	 {01,01.015,01.015.016900,01.015.016900.1}
	 {01,01.015,01.015.016900,01.015.016900.2}
	 {01,01.015,01.015.016900,01.015.016900.3}
	(6 rows)
	
 */
DECLARE 
	s my_array%type;
BEGIN
	FOREACH s SLICE 1  IN ARRAY $1 LOOP
		RETURN NEXT s;
	END LOOP;
--
	RETURN;
END;
$func$;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_reduce_dim(ANYARRAY) IS 'Function: 	_rif40_reduce_dim()
Parameters:	2D array 
Returns:	Set of 1D array rows
Description:
			Array reduce from two to one dimension as a set of 1D array rows
			Can then be cast to a record. Helper function for rif40_sql_test()
E.g.

	WITH a AS (
		SELECT ''{{01,01.015,01.015.016200,01.015.016200.2}
	,{01,01.015,01.015.016200,01.015.016200.3} 
	,{01,01.015,01.015.016200,01.015.016200.4} 
	,{01,01.015,01.015.016900,01.015.016900.1} 
	,{01,01.015,01.015.016900,01.015.016900.2} 
	,{01,01.015,01.015.016900,01.015.016900.3} 
	}''::Text[][] AS res
	)
	SELECT rif40_sql_pkg._rif40_reduce_dim(a.res) AS res
	  FROM a;
		
						res
	-------------------------------------------
	 {01,01.015,01.015.016200,01.015.016200.2}
	 {01,01.015,01.015.016200,01.015.016200.3}
	 {01,01.015,01.015.016200,01.015.016200.4}
	 {01,01.015,01.015.016900,01.015.016900.1}
	 {01,01.015,01.015.016900,01.015.016900.2}
	 {01,01.015,01.015.016900,01.015.016900.3}
	(6 rows)';
	
--
-- Eof