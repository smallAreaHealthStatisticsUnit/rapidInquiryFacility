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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check c) Missing table/view columns
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
--rif40_ddl_check_c:									70150 to 70199
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_c()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_c()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check c) Missing table/view columns:
		 Table and column combinations listed in rif30_columns.table_name, column_name
		 * Exclude missing tables/views (i.e. check a)
		 * Removing (EXCEPT/MINUS depending on database) columns with comments

 */
DECLARE
	c3 CURSOR(l_schema VARCHAR) FOR /* Missing table/view columns */
		SELECT LOWER(table_or_view_name_hide) table_or_view, LOWER(column_name_hide) column_name
		  FROM rif40_columns
                 WHERE LOWER(table_or_view_name_hide) NOT IN ( /* Exclude missing tables/views (i.e. check a) */
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
			   AND c.relkind    = 'r' 				/* Relational table */
			   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
			EXCEPT
			SELECT viewname	table_or_view				/* Local views */
			  FROM pg_views
			 WHERE viewowner  IN (USER, l_schema)
			   AND schemaname IN (USER, l_schema)
		)
		   AND table_or_view_name_hide NOT LIKE 'G%'
		EXCEPT
		SELECT table_name table_or_view, column_name
		  FROM information_schema.columns a
			LEFT OUTER JOIN pg_tables b1 ON (b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
			LEFT OUTER JOIN pg_views b2 ON (b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
			LEFT OUTER JOIN (
				SELECT c.relname fdw_table, r.rolname fdw_tableowner
				  FROM pg_class c, pg_foreign_table t, pg_roles r
				 WHERE t.ftrelid  = c.oid 
				   AND c.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)) b3 ON
				(b3.fdw_table = a.table_name)
		 WHERE tableowner IN (USER, l_schema) OR viewowner IN (USER, l_schema) OR fdw_tableowner = USER
		 ORDER BY 1, 2;
--
	c3_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	i INTEGER:=0;
BEGIN	
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_c', '[70150]: Checking for missing table/view columns');
	FOR c3_rec IN c3(schema_owner) LOOP
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_c', '[70151]: Missing table/view column: %.%', 
			c3_rec.table_or_view::VARCHAR, c3_rec.column_name::VARCHAR);
		i:=i+1;
	END LOOP;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_c() IS 'Function: 		rif40_ddl_check_c()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check c) Missing table/view columns:
		 Table and column combinations listed in rif30_columns.table_name, column_name
		 * Exclude missing tables/views (i.e. check a)
		 * Removing (EXCEPT/MINUS depending on database) columns with comments';

--
-- Eof