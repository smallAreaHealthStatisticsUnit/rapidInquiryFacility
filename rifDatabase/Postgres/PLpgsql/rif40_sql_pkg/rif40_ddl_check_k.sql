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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check k) Missing comments
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
--rif40_ddl_check_k:									70550 to 70599
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_k()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_k()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check k) Missing comments for all objects owned by rif40
         Schemas: rif40, rif_studies, and rif*pkg are errors; others are for INFO
 */
DECLARE
	c11 CURSOR FOR /* All rif40_objects and comments */
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
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid AND (d.objsubid IS NULL OR d.objsubid = 0))
			 WHERE c.relowner IN (SELECT oid FROM pg_roles
								   WHERE rolname IN ('rif40', USER, 'pop', 'gis', 'data_load'))
			UNION
			SELECT p.proname AS object_name, 
			       n.nspname AS object_schema,
			       'FUNCTION'  AS object_type,
			       d.description
			  FROM pg_proc p
				LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = p.oid AND d.objsubid = 0)
			 WHERE p.proowner IN (SELECT oid FROM pg_roles 
								   WHERE rolname IN ('rif40', USER, 'pop', 'gis', 'data_load'))
			UNION
			SELECT t.tgname AS object_name, 
			       n.nspname AS object_schema,
			       'TRIGGER'  AS object_type,
			       d.description
			  FROM pg_trigger t, pg_class c
				LEFT OUTER JOIN pg_namespace n ON (n.oid = c.relnamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid AND d.objsubid = 0)
			 WHERE c.relowner IN (SELECT oid FROM pg_roles
								   WHERE rolname IN ('rif40', USER, 'pop', 'gis', 'data_load'))
			   AND c.oid = t.tgrelid
			   AND t.tgisinternal = FALSE /* trigger function */
			UNION
			SELECT DISTINCT relname||'.'||column_name AS object_name, 
			       n.nspname AS object_schema,
			       'COLUMN'  AS object_type,
			       b.description
			  FROM (
				SELECT table_name table_or_view, column_name, ordinal_position
				  FROM information_schema.columns a
					LEFT OUTER JOIN pg_tables b1 ON 
						(b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
					LEFT OUTER JOIN pg_views b2  ON 
						(b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
				 WHERE tableowner IN (USER, 'rif40', 'pop', 'gis', 'data_load')
				    OR viewowner IN (USER, 'rif40')) c, 
				pg_class a
				LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid)
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE a.relowner IN (SELECT oid FROM pg_roles
								   WHERE rolname IN (USER, 'rif40', 'pop', 'gis', 'data_load')) 
			   AND b.objsubid = c.ordinal_position
			   AND a.relname  = c.table_or_view
		), b AS (
			SELECT object_type, object_schema, COUNT(object_name) AS total_objects, 
			       CASE
					WHEN object_schema = 'pg_toast' OR
					     object_type IN ('INDEX', 'COMPOSITE TYPE') THEN NULL
					ELSE 					     	     COUNT(object_name)-COUNT(description) 
			       END AS missing_comments
			  FROM a
			 GROUP BY object_type, object_schema
		)
		SELECT object_type, object_schema, total_objects, missing_comments
		  FROM b
		 ORDER BY 1, 2;
	c12 CURSOR(l_object_type VARCHAR, l_object_schema VARCHAR) FOR /* All rif40_objects and comments */
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
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid AND (d.objsubid IS NULL OR d.objsubid = 0))
			 WHERE c.relowner IN (SELECT oid FROM pg_roles
								   WHERE rolname IN ('rif40', USER, 'pop', 'gis', 'data_load'))
			UNION
			SELECT p.proname AS object_name, 
			       n.nspname AS object_schema,
			       'FUNCTION'  AS object_type,
			       d.description
			  FROM pg_proc p
				LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = p.oid AND d.objsubid = 0)
			 WHERE p.proowner IN (SELECT oid FROM pg_roles
								   WHERE rolname IN ('rif40', USER, 'pop', 'gis', 'data_load'))
			UNION
			SELECT t.tgname AS object_name, 
			       n.nspname AS object_schema,
			       'TRIGGER'  AS object_type,
			       d.description
			  FROM pg_trigger t, pg_class c
				LEFT OUTER JOIN pg_namespace n ON (n.oid = c.relnamespace)			
				LEFT OUTER JOIN pg_description d ON (d.objoid = c.oid AND d.objsubid = 0)
			 WHERE c.relowner IN (SELECT oid FROM pg_roles 
								   WHERE rolname IN ('rif40', USER, 'pop', 'gis', 'data_load'))
			   AND c.oid = t.tgrelid
			   AND t.tgisinternal = FALSE /* trigger function */
			UNION
			SELECT DISTINCT relname||'.'||column_name AS object_name, 
			       n.nspname AS object_schema,
			       'COLUMN'  AS object_type,
			       b.description
			  FROM (
				SELECT table_name table_or_view, column_name, ordinal_position
				  FROM information_schema.columns a
					LEFT OUTER JOIN pg_tables b1 ON 
						(b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
					LEFT OUTER JOIN pg_views b2  ON 
						(b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
				 WHERE tableowner IN (USER, 'rif40') 
				    OR viewowner IN (USER, 'rif40', 'pop', 'gis', 'data_load')
                   		   AND a.table_schema  != 'rif_studies'			/* Exclude map/extract tables */
				) c, 
				pg_class a
				LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid)
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
			 WHERE a.relowner IN (SELECT oid FROM pg_roles
								   WHERE rolname IN (USER, 'rif40', 'pop', 'gis', 'data_load')) 
			   AND b.objsubid = c.ordinal_position
			   AND a.relname  = c.table_or_view
		)
		SELECT object_type, object_schema, object_name, description
		  FROM a
		 WHERE NOT (object_schema = 'pg_toast' OR object_type IN ('INDEX'))
		   AND l_object_schema = object_schema
		   AND l_object_type   = object_type
		 ORDER BY 1, 2, 3;
--
	c11_rec RECORD;
	c12_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	tag VARCHAR;
--
	i INTEGER:=0;
BEGIN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_k', '[70550]: Checking for missing comments');
		FOR c11_rec IN c11 LOOP
		 	IF c11_rec.missing_comments > 0 THEN
				IF c11_rec.object_schema LIKE 'rif40%' OR c11_rec.object_schema = 'rif_studies' THEN
					tag:='WARNING';
				ELSE
					tag:='INFO';
				END IF;
				PERFORM rif40_log_pkg.rif40_log(tag::rif40_log_pkg.rif40_log_debug_level, 'rif40_ddl_check_k', '[70551]: Missing %/% comments for schema % %s', 
					c11_rec.missing_comments::VARCHAR, 
					c11_rec.total_objects::VARCHAR,
					c11_rec.object_schema::VARCHAR, 
					c11_rec.object_type::VARCHAR); 
				FOR c12_rec IN c12(c11_rec.object_type, c11_rec.object_schema) LOOP
					IF c12_rec.description IS NOT NULL THEN
						PERFORM rif40_log_pkg.rif40_log(tag::rif40_log_pkg.rif40_log_debug_level, 'rif40_ddl_check_k', '[70552]: Found comment for % %.%', 
							c12_rec.object_type::VARCHAR,
							c12_rec.object_schema::VARCHAR, 
							c12_rec.object_name::VARCHAR);					
					ELSIF c11_rec.object_schema LIKE 'rif40%' OR c11_rec.object_schema = 'rif_studies' THEN
						i:=i+1;
						PERFORM rif40_log_pkg.rif40_log(tag::rif40_log_pkg.rif40_log_debug_level, 'rif40_ddl_check_k', '[70552]: Missing comment for % %.%', 
							c12_rec.object_type::VARCHAR,
							c12_rec.object_schema::VARCHAR, 
							c12_rec.object_name::VARCHAR);
					ELSE
						PERFORM rif40_log_pkg.rif40_log(tag::rif40_log_pkg.rif40_log_debug_level, 'rif40_ddl_check_k', '[70552]: Missing comment for % %.% [INGORED]', 
							c12_rec.object_type::VARCHAR,
							c12_rec.object_schema::VARCHAR, 
							c12_rec.object_name::VARCHAR);
					END IF;
				END LOOP;
			ELSE
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_k', '[70553]: No missing comments for schema % %s (%)', 
					c11_rec.object_schema::VARCHAR, 
					c11_rec.object_type::VARCHAR,
					c11_rec.total_objects::VARCHAR);
			END IF;
		END LOOP;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_k() IS 'Function: 		rif40_ddl_check_k()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check k) Missing comments
         Schemas: rif40, rif_studies, and rif*pkg are errors; others are for INFO';

--
-- Eof