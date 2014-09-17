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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check e) Missing triggers
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
--rif40_ddl_check_e:									70250 to 70299
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_e()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_e()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check e) Missing triggers:
		 Tables listed in rif40_triggers.trigger_name, table_name
		 Removing (EXCEPT/MINUS depending on database) triggers for user RIF40
 */
DECLARE
	c5 CURSOR(l_schema VARCHAR) FOR  /* Missing triggers */
		SELECT LOWER(trigger_name) trigger_name, LOWER(table_name) table_name
		  FROM rif40_triggers
		 WHERE LOWER(table_name) NOT IN (
			SELECT a.relname object_name	/* FDW tables */
			  FROM pg_foreign_table b, pg_roles r, pg_class a
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE b.ftrelid = a.oid
			   AND a.relowner IN (SELECT oid FROM pg_roles WHERE rolname = l_schema)
			   AND a.relowner = r.oid
			   AND COALESCE(n.nspname, r.rolname) = l_schema)
		EXCEPT
		SELECT tgname trigger_name, relname table_name
		  FROM pg_class a, pg_trigger b
			 WHERE tgrelid = a.oid
			   AND relowner IN (SELECT oid FROM pg_roles WHERE rolname = l_schema)
		 ORDER BY 1, 2;
--
	c5_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	i INTEGER:=0;
BEGIN	

	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_e', '[70250]: Checking for missing table triggers');
	FOR c5_rec IN c5(schema_owner) LOOP
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_e', '[70251]: Missing table trigger: (%) % [TRIGGER NEEDS TO BE ADDED]', 
			c5_rec.table_name::VARCHAR, c5_rec.trigger_name::VARCHAR);
		i:=i+1;
	END LOOP;
	IF i > 0 THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_e', '[70252]: % missing table triggers', 
			i::VARCHAR);
	END IF;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_e() IS 'Function: 		rif40_ddl_check_e()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check e) Missing triggers:
		 Tables listed in rif40_triggers.trigger_name, table_name
		 Removing (EXCEPT/MINUS depending on database) triggers for user RIF40';

--
-- Eof