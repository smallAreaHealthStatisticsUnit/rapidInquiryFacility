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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check a) Missing tables and views
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
--rif40_ddl_check_a:									70050 to 70099
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_a()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_a()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check a) Missing tables and views:
		 Tables listed in rif30_tables_and_views.table_name
		 Removing (EXCEPT/MINUS depending on database):
		 * User temporary tables
		 * User foreign data wrapper (i.e. Oracle) tables
		 * User and rif40 local tables
		 * User views	 
		 
 */
DECLARE
	c1 CURSOR(l_schema VARCHAR) FOR /* Missing tables and views */
		SELECT LOWER(table_or_view_name_hide) table_or_view	/* RIF40 list of tables and views */
		  FROM rif40_tables_and_views
		EXCEPT 
		SELECT a.relname table_or_view 				/* Temporary tables */
 		 FROM pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relkind        = 'r' 				/* Relational table */
		   AND a.relpersistence = 't' 				/* Persistence: temporary */
		   AND a.relowner = r.oid
		   AND n.nspname        LIKE 'pg_temp%'
		EXCEPT					
		SELECT a.relname table_or_view				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.ftrelid = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND COALESCE(n.nspname, r.rolname) IN (USER, 'public')
		EXCEPT
		SELECT t.tablename table_or_view			/* Local tables */
		  FROM pg_tables t, pg_class c
		 WHERE t.tableowner IN (USER, l_schema)
		   AND t.schemaname IN (USER, l_schema)
		   AND c.relowner   IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, l_schema))
		   AND c.relname    = t.tablename
		   AND c.relkind    = 'r' 					/* Relational table */
		   AND c.relpersistence IN ('p', 'u') 		/* Persistence: permanent/unlogged */
		EXCEPT
		SELECT viewname	table_or_view 				/* Local views */
		  FROM pg_views
		 WHERE viewowner  IN (USER, l_schema)
		   AND schemaname IN (USER, l_schema)
		 ORDER BY 1;
--
	c1_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	i INTEGER:=0;
BEGIN	
	FOR c1_rec IN c1(schema_owner) LOOP
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_a', '[70050]: Checking for missing table/views');
		IF USER = 'rifupg34' AND c1_rec.table_or_view IN ( /* Not created in the upgrade schema */
				'g_rif40_comparison_areas', 'g_rif40_study_areas', 'rif40_user_version') THEN
			NULL;
		ELSIF USER = 'rifupg34' THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_a', '[70051]: Missing foreign data wrapper table/table/view: %', 
				c1_rec.table_or_view::VARCHAR);
			i:=i+1;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_a', '[70052]: Missing table/view: %', 
				c1_rec.table_or_view::VARCHAR);
			i:=i+1;
		END IF;
	END LOOP;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_a() IS 'Function: 		rif40_ddl_check_a()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check a) Missing tables and views:
		 Tables listed in rif30_tables_and_views.table_name
		 Removing (EXCEPT/MINUS depending on database):
		 * User temporary tables
		 * User foreign data wrapper (i.e. Oracle) tables
		 * User and rif40 local tables
		 * User views';

--
-- Eof