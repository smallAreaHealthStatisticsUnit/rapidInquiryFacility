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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check b) Missing table/view comments
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
--rif40_ddl_check_b:									70100 to 70149
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_b()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_b()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check b) Missing table/view comments for all tables in RIF40_TABLES_AND_VIEWS
 */
DECLARE
	c2 CURSOR(l_schema VARCHAR) FOR /* Missing table/view comment */	
		SELECT DISTINCT relname table_or_view, n.nspname AS schema_owner, b.description, comments
		  FROM rif40_tables_and_views c, pg_class a
			LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid AND b.objsubid = 0)
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema))
		   AND b.description IS NULL
		   AND LOWER(table_or_view_name_hide) = a.relname
		 ORDER BY 1;
--
	c2_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	i INTEGER:=0;
BEGIN	
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_b', '[70100]: Checking for missing table/view comments');
	FOR c2_rec IN c2(schema_owner) LOOP
		IF c2_rec.description IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_b', '[70101]: Missing table/view comment: %', 
				c2_rec.table_or_view::VARCHAR);
			i:=i+1;
		END IF;
	END LOOP;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_b() IS 'Function: 		rif40_ddl_check_b()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check b) Missing table/view comments for all tables in RIF40_TABLES_AND_VIEWS';

--
-- Eof