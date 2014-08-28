-- ************************************************************************
-- *
-- * THIS SCRIPT MAY BE EDITED - NO NEED TO USE ALTER SCRIPTS
-- *
-- ************************************************************************
--
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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check d) Missing table/view column comments
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
\set ON_ERROR_STOP ON
\timing

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
--rif40_ddl_check_d:									70200 to 70249
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_d()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_d()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check d) Missing table/view column comments
 */
DECLARE
	c4 CURSOR(l_schema VARCHAR) FOR /* Missing table/view column comments */ 		
		WITH a AS (
	 		SELECT table_name table_or_view, column_name, ordinal_position
			  FROM information_schema.columns a
					LEFT OUTER JOIN pg_views b2  ON 
						(b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
					LEFT OUTER JOIN pg_tables b1 ON 
						(b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
			 WHERE (viewowner IN (USER, l_schema) OR tableowner IN (USER, l_schema))
			   AND table_schema = 'rif40' 
		), b AS (
			SELECT table_or_view, column_name, ordinal_position, b.oid
  			  FROM a, pg_class b
			 WHERE b.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema)) 
			   AND b.relname  = a.table_or_view
		)
		SELECT table_or_view, column_name, ordinal_position, c.description
		  FROM b
			LEFT OUTER JOIN pg_description c ON (c.objoid = b.oid AND c.objsubid = b.ordinal_position)
		 WHERE c.description IS NULL
		 ORDER BY 1, 2;
--
	c4_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	i INTEGER:=0;
BEGIN	
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_d', '[70200]: Checking for missing table/view column comments');
	FOR c4_rec IN c4(schema_owner) LOOP
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_d', '[70201]: Missing table/view column comment: %.%', 
			c4_rec.table_or_view::VARCHAR, c4_rec.column_name::VARCHAR);
		i:=i+1;
	END LOOP;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_d() IS 'Function: 		rif40_ddl_check_d()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check d) Missing table/view column comments';

--
-- Eof