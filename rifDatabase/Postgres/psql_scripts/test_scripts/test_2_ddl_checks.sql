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
-- Rapid Enquiry Facility (RIF) - Test 2: DDL checks
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
\set ECHO :echo
\set ON_ERROR_STOP ON

--
-- Start transaction
--
BEGIN;

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'test_2_ddl_checks.sql: T2__01: User check: %', user;	
	ELSE
		RAISE EXCEPTION 'test_2_ddl_checks.sql: T2__02: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

--
-- Check all tables and views are built
--
SELECT table_or_view, LOWER(table_or_view_name_hide) AS table_or_view_name
  FROM rif40_tables_and_views
 WHERE table_or_view = 'TABLE'
EXCEPT
SELECT 'TABLE' AS table_or_view, table_name AS table_or_view_name
  FROM information_schema.tables
UNION
SELECT table_or_view, LOWER(table_or_view_name_hide) AS table_or_view_name
  FROM rif40_tables_and_views
 WHERE table_or_view = 'VIEW'
EXCEPT
SELECT 'TABLE' AS table_or_view, table_name AS table_or_view_name
  FROM information_schema.views
 ORDER BY 1, 2;
 
 --
 -- List objects and comments
 --
 \pset title 'Objects and comments'
 WITH a AS (
			SELECT c.relname AS object_name, 
			       n.nspname AS object_schema, 
			       CASE 
					WHEN c.relkind        = 'r' AND			/* Relational table */
					     c.relpersistence = 'p' 			/* Persistence: unlogged */	 THEN 'TABLE'
					WHEN c.relkind        = 'r' AND			/* Relational table */
					     c.relpersistence = 'u' 			/* Persistence: unlogged */ 	THEN 'UNLOGGED TABLE'
					WHEN c.relkind        = 'r' AND			/* Relational table */
					     c.relpersistence = 't' 			/* Persistence: temporary */ 	THEN 'TEMPORARY TABLE'
					WHEN c.relkind        = 'i'			/* Index */			THEN 'INDEX'
					WHEN c.relkind        = 'v'			/* View */			THEN 'VIEW'
					WHEN c.relkind        = 'S'			/* Sequence */			THEN 'SEQUENCE'
					WHEN c.relkind        = 't'			/* TOAST Table */		THEN 'TOAST TABLE'
					WHEN c.relkind        = 'f'			/* Foreign Table */		THEN 'FOREIGN TABLE'
					WHEN c.relkind        = 'c'			/* Composite type */		THEN 'COMPOSITE TYPE'
					ELSE 										     'Unknown relkind: '||c.relkind
			       END AS object_type,
			       d.description
			  FROM pg_class c
				LEFT OUTER JOIN pg_namespace n ON (n.oid = c.relnamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid)
			 WHERE c.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', USER))
			UNION
			SELECT p.proname AS object_name, 
			       n.nspname AS object_schema,
			       'FUNCTION'  AS object_type,
			       d.description
			  FROM pg_proc p
				LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = p.oid)
			 WHERE p.proowner IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', USER))
			UNION
			SELECT t.tgname AS object_name, 
			       n.nspname AS object_schema,
			       'TRIGGER'  AS object_type,
			       d.description
			  FROM pg_trigger t, pg_class c
				LEFT OUTER JOIN pg_namespace n ON (n.oid = c.relnamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid)
			 WHERE c.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', USER))
			   AND c.oid = t.tgrelid
			   AND t.tgisinternal = FALSE /* trigger function */
		)
		SELECT object_type, object_schema, COUNT(object_name) AS total_objects, 
		       CASE
				WHEN object_schema = 'pg_toast' OR
				     object_type IN ('INDEX') 		THEN NULL
				ELSE 					     COUNT(object_name)-COUNT(description) 
		       END AS missing_comments
		  FROM a
		 GROUP BY object_type, object_schema
		 ORDER BY 1, 2;
\pset title
		 
--
-- Check all tables, triggers, columns and comments are present, objects granted to rif_user/rif_manmger, sequences granted
-- 
\i ../psql_scripts/v4_0_postgres_ddl_checks.sql

\echo Test 2 - DDL checks completed OK.
--
-- End transaction
--
END;

--
-- Eof