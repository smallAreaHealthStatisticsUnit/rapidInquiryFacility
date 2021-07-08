-- ************************************************************************
-- *
-- * THIS IS A SCHEMA ALTER SCRIPT - IT CAN BE RE-RUN BUT THEY MUST BE RUN 
-- * IN NUMERIC ORDER
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
-- Rapid Enquiry Facility (RIF) - RIF alter script 4 - Hash partitioning
--
-- Alter script 4 must go after 7 - Support for ontologies (e.g. ICD9, 10); removed previous table based support.
--								    Modify t_rif40_inv_conditions to remove SQL injection risk
--
-- This is because 7 was written before the partitioning was enabled and does not support it
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

\echo Running SAHSULAND schema alter script #4 Hash partitioning...

BEGIN;

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'v4_0_alter_4.sql: User check: %', user;	
	ELSE
		RAISE EXCEPTION 'v4_0_alter_4.sql: C20900: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

--
-- Run common code on all pre-existing partitions (i.e. geolevel partitions)
-- to add indexes, grants etc
--

--
-- PG psql code (SQL and Oracle compatibility processing)
--
\i ../PLpgsql/v4_0_rif40_sql_pkg.sql

--
-- Partition enabled DDL checks
--
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_b.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_j.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_k.sql

--
-- Reload triggers (including fixes for test harness)
--
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_studies_checks.sql
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_investigations_checks.sql
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_inv_conditions_checks.sql
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_study_areas_checks.sql
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_comparison_areas_checks.sql
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_inv_covariates_checks.sql
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_rif40_study_shares_checks.sql

WITH c AS (   
	SELECT cn.nspname AS schema_child, c.relname AS child, pn.nspname AS schema_parent, p.relname AS parent
	FROM pg_attribute b, pg_inherits 
			LEFT OUTER JOIN pg_class AS c ON (inhrelid=c.oid)
			LEFT OUTER JOIN pg_class as p ON (inhparent=p.oid)
			LEFT OUTER JOIN pg_namespace pn ON pn.oid = p.relnamespace
			LEFT OUTER JOIN pg_namespace cn ON cn.oid = c.relnamespace
	WHERE cn.nspname = 'rif40_partitions'
	  AND p.relkind  = 'r' 
	  AND p.relpersistence IN ('p', 'u') 
	  AND p.oid      = b.attrelid
	  AND b.attname  = 'study_id'
), b AS (
	SELECT 'C' parent_or_child, child AS table_name
	FROM c
	UNION
	SELECT 'P', parent
	FROM c
)
SELECT * FROM b
 ORDER BY 1, 2;
 
CREATE LOCAL TEMPORARY TABLE hash_partition_test_old
AS
WITH y AS (
			SELECT ARRAY_AGG(a.tablename) AS table_list
			  FROM pg_tables a, pg_attribute b, pg_class c
	 		 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.tablename
  		       AND c.relkind    = 'r' /* Relational table */
		       AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		       AND b.attname    = 'study_id'
		       AND a.schemaname = 'rif40'	
)
SELECT a.relname AS object_name, NULL AS sub_object_name, b.nspname AS schema, 'table' AS object_type, 1 AS object_order, NULL AS sub_type, 
       obj_description(a.oid, 'pg_class') AS comment
  FROM pg_namespace b, pg_class a	
		LEFT OUTER JOIN pg_type c ON (a.reltype = c.oid)
 WHERE a.relnamespace = b.oid  	
   AND a.relname      IN (SELECT UNNEST(y.table_list) FROM y)
UNION   
SELECT z.relname AS object_name, a.attname AS sub_object_name, NULL AS schema, 'column' AS object_type, 2 AS object_order, 
       pg_catalog.format_type(a.atttypid, a.atttypmod) AS sub_type, pg_catalog.col_description(a.attrelid, a.attnum) AS comment
  FROM pg_attribute a, pg_class z
 WHERE a.attrelid = z.oid
   AND z.relname  IN (SELECT UNNEST(y.table_list) FROM y)
   AND a.attnum   > 0
   AND NOT a.attisdropped
UNION
SELECT c.relname AS object_name, c2.relname AS sub_object_name, NULL AS schema,
       CASE 
			WHEN i.indisprimary THEN 'primary key index'
			WHEN i.indisunique  THEN 'unique index'
			ELSE                     'other index' END AS object_type, 3 AS object_order,
	   pg_catalog.pg_get_indexdef(i.indexrelid, 0, true) AS subtype,
       pg_catalog.pg_get_constraintdef(con.oid, true) AS comment
FROM pg_catalog.pg_class c, pg_catalog.pg_class c2, pg_catalog.pg_index i
	LEFT JOIN pg_catalog.pg_constraint con ON (conrelid = i.indrelid AND conindid = i.indexrelid AND contype IN ('p','u','x'))
 WHERE c.relname   IN (SELECT UNNEST(y.table_list) FROM y)
   AND c.oid        = i.indrelid
   AND i.indexrelid = c2.oid 
UNION
SELECT z.relname AS object_name, r.conname AS sub_object_name, NULL AS schema, 'check constraint' AS object_type, 4 AS object_order, 
       pg_catalog.pg_get_constraintdef(r.oid, true) AS subtype, NULL AS comment
  FROM pg_catalog.pg_constraint r, pg_class z
 WHERE r.conrelid = z.oid
   AND z.relname  IN (SELECT UNNEST(y.table_list) FROM y)
   AND r.contype  = 'c'
UNION
SELECT z.relname AS object_name, conname AS sub_object_name, NULL AS schema, 'foreign key constraint' AS object_type, 5 AS object_order,
       pg_catalog.pg_get_constraintdef(r.oid, true) AS subtype, NULL AS comment
  FROM pg_catalog.pg_constraint r, pg_class z
 WHERE r.conrelid = z.oid
   AND z.relname  IN (SELECT UNNEST(y.table_list) FROM y)
   AND r.contype  = 'f'
UNION
SELECT z.relname AS object_name, t.tgname AS sub_object_name, NULL AS schema, 'trigger' AS object_type, 6 AS object_order,
    pg_catalog.pg_get_triggerdef(t.oid, true) AS subtype, t.tgenabled  AS comment
  FROM pg_catalog.pg_trigger t, pg_class z
 WHERE t.tgrelid = z.oid
   AND z.relname IN (SELECT UNNEST(y.table_list) FROM y)
   AND NOT t.tgisinternal
ORDER BY 5, 2, 3;
\copy (SELECT * FROM hash_partition_test_old ORDER BY 5, 2, 3) TO test_scripts/data/hash_partition_test_old.csv WITH CSV HEADER
--SELECT z.relname AS object_name, t.tgname AS sub_object_name, NULL AS schema, 'trigger' AS object_type, 6 AS object_order,
--    pg_catalog.pg_get_triggerdef(t.oid, true) AS subtype, t.tgenabled  AS comment
--  FROM pg_catalog.pg_trigger t, pg_class z
-- WHERE t.tgrelid = z.oid
--   AND z.relname IN (SELECT UNNEST(y.table_list) FROM y)
--   AND NOT t.tgisinternal
--
-- Hash partition all tables with study_id as a column
-- This will cope with data already present in the table
--

--
-- Quit if not sahsuland_dev database
--
\copy (SELECT '\i ../psql_scripts/v4_0_study_id_partitions.sql' AS txt WHERE current_database() = 'sahsuland_dev' OR current_database() = 'sahsuland_empty') TO ../psql_scripts/auto_quit_tmp.sql WITH (FORMAT csv, ESCAPE '/')
--
-- Execute
--
\i ../psql_scripts/auto_quit_tmp.sql

--\dS+ t_rif40_investigations
--\dS+ rif40_partitions.t_rif40_investigations_p1

CREATE LOCAL TEMPORARY TABLE hash_partition_test_new
AS
WITH c AS (   
	SELECT cn.nspname AS schema_child, c.relname AS child, pn.nspname AS schema_parent, p.relname AS parent
	FROM pg_attribute b, pg_inherits 
			LEFT OUTER JOIN pg_class AS c ON (inhrelid=c.oid)
			LEFT OUTER JOIN pg_class as p ON (inhparent=p.oid)
			LEFT OUTER JOIN pg_namespace pn ON pn.oid = p.relnamespace
			LEFT OUTER JOIN pg_namespace cn ON cn.oid = c.relnamespace
	WHERE cn.nspname = 'rif40_partitions'
	  AND p.relkind  = 'r' /* Relational table */
	  AND p.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */  
	  AND p.oid      = b.attrelid
	  AND b.attname  = 'study_id'
), b AS (
	SELECT child AS table_name
	FROM c
	UNION
	SELECT parent
	FROM c
), y AS (
	SELECT ARRAY_AGG(table_name) AS table_list
	  FROM b
)
SELECT a.relname AS object_name, NULL AS sub_object_name, b.nspname AS schema, 'table' AS object_type, 1 AS object_order, NULL AS sub_type, 
       obj_description(a.oid, 'pg_class') AS comment
  FROM pg_namespace b, pg_class a	
		LEFT OUTER JOIN pg_type c ON (a.reltype = c.oid)
 WHERE a.relnamespace = b.oid  	
   AND a.relname      IN (SELECT UNNEST(y.table_list) FROM y)
UNION   
SELECT z.relname AS object_name, a.attname AS sub_object_name, NULL AS schema, 'column' AS object_type, 2 AS object_order, 
       pg_catalog.format_type(a.atttypid, a.atttypmod) AS sub_type, pg_catalog.col_description(a.attrelid, a.attnum) AS comment
  FROM pg_attribute a, pg_class z
 WHERE a.attrelid = z.oid
   AND z.relname  IN (SELECT UNNEST(y.table_list) FROM y)
   AND a.attnum   > 0
   AND NOT a.attisdropped
UNION
SELECT c.relname AS object_name, c2.relname AS sub_object_name, NULL AS schema,
       CASE 
			WHEN i.indisprimary THEN 'primary key index'
			WHEN i.indisunique  THEN 'unique index'
			ELSE                     'other index' END AS object_type, 3 AS object_order,
	   pg_catalog.pg_get_indexdef(i.indexrelid, 0, true) AS subtype,
       pg_catalog.pg_get_constraintdef(con.oid, true) AS comment
FROM pg_catalog.pg_class c, pg_catalog.pg_class c2, pg_catalog.pg_index i
	LEFT JOIN pg_catalog.pg_constraint con ON (conrelid = i.indrelid AND conindid = i.indexrelid AND contype IN ('p','u','x'))
 WHERE c.relname   IN (SELECT UNNEST(y.table_list) FROM y)
   AND c.oid        = i.indrelid
   AND i.indexrelid = c2.oid 
UNION
SELECT z.relname AS object_name, r.conname AS sub_object_name, NULL AS schema, 'check constraint' AS object_type, 4 AS object_order, 
       pg_catalog.pg_get_constraintdef(r.oid, true) AS subtype, NULL AS comment
  FROM pg_catalog.pg_constraint r, pg_class z
 WHERE r.conrelid = z.oid
   AND z.relname  IN (SELECT UNNEST(y.table_list) FROM y)
   AND r.contype  = 'c'
UNION
SELECT z.relname AS object_name, conname AS sub_object_name, NULL AS schema, 'foreign key constraint' AS object_type, 5 AS object_order,
       pg_catalog.pg_get_constraintdef(r.oid, true) AS subtype, NULL AS comment
  FROM pg_catalog.pg_constraint r, pg_class z
 WHERE r.conrelid = z.oid
   AND z.relname  IN (SELECT UNNEST(y.table_list) FROM y)
   AND r.contype  = 'f'
UNION
SELECT z.relname AS object_name, t.tgname AS sub_object_name, NULL AS schema, 'trigger' AS object_type, 6 AS object_order,
    pg_catalog.pg_get_triggerdef(t.oid, true) AS subtype, t.tgenabled  AS comment
  FROM pg_catalog.pg_trigger t, pg_class z
 WHERE t.tgrelid = z.oid
   AND z.relname IN (SELECT UNNEST(y.table_list) FROM y)
   AND NOT t.tgisinternal
ORDER BY 5, 2, 3;
\copy (SELECT * FROM hash_partition_test_new ORDER BY 5, 2, 3) TO test_scripts/data/hash_partition_test_new.csv WITH CSV HEADER

--
-- First compare old with new parent tables
--
\pset title 'Missing objects'
WITH y AS (
			SELECT ARRAY_AGG(a.tablename) AS table_list
			  FROM pg_tables a, pg_attribute b, pg_class c
	 		 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.tablename
  		       AND c.relkind    = 'r' /* Relational table */
		       AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		       AND b.attname    = 'study_id'
		       AND a.schemaname = 'rif40'	
)
SELECT object_name, sub_object_name, object_type, sub_type, comment
 FROM hash_partition_test_old
 WHERE object_name IN (SELECT UNNEST(y.table_list) FROM y)
EXCEPT
SELECT object_name, sub_object_name, object_type, sub_type, comment
  FROM hash_partition_test_new
ORDER BY 1, 2, 3;

\pset title 'Extra objects'
WITH y AS (
			SELECT ARRAY_AGG(a.tablename) AS table_list
			  FROM pg_tables a, pg_attribute b, pg_class c
	 		 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.tablename
  		       AND c.relkind    = 'r' /* Relational table */
		       AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		       AND b.attname    = 'study_id'
		       AND a.schemaname = 'rif40'	
)
SELECT object_name, sub_object_name, object_type, sub_type, comment
  FROM hash_partition_test_new
 WHERE object_name IN (SELECT UNNEST(y.table_list) FROM y)
   AND NOT (sub_object_name = 'hash_partition_number' AND object_type = 'column')  /* Exclude hash column */
   AND NOT (object_name||'_insert' = sub_object_name  AND object_type = 'trigger') /* Exclude partition insert trigger */  
EXCEPT
SELECT object_name, sub_object_name, object_type, sub_type, comment
  FROM hash_partition_test_old
ORDER BY 1, 2, 3;

--
-- Then compare parent with children (i.e. check all partitions are set up correctly)
--
-- Ingnore object_type: TABLE, COLUMN; you are just testing INHERITS
-- 
\pset title 'Extra partition indexes'
SELECT REGEXP_REPLACE(object_name, '_p([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(sub_object_name, '_p([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(sub_type, '_p([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') AS comment 
 FROM hash_partition_test_new
 WHERE object_type LIKE '%index'
   AND REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') NOT LIKE '%USING btree (_rif40_hash(%' /* Ignore hash index */
   AND object_name SIMILAR TO '%_p[1-9]%'
EXCEPT
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM hash_partition_test_new
 WHERE object_type LIKE '%index'
   AND object_name NOT SIMILAR TO '%_p[1-9]%'   
 ORDER BY 5, 1, 2, 3;
\pset title 'Missing partition indexes'
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM hash_partition_test_new
 WHERE object_type LIKE '%index'
   AND object_name NOT SIMILAR TO '%_p[1-9]%'   
EXCEPT
SELECT REGEXP_REPLACE(object_name, '_p([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(sub_object_name, '_p([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(sub_type, '_p([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') AS comment 
 FROM hash_partition_test_new
 WHERE object_type LIKE '%index'
   AND object_name SIMILAR TO '%_p[1-9]%'
 ORDER BY 5, 1, 2, 3;
 
-- Missing partition check constraints: not relevant
 
\pset title 'Extra partition foreign key constraints' 
SELECT REGEXP_REPLACE(object_name, '_p([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(sub_object_name, '_p([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(sub_type, '_p([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') AS comment 
 FROM hash_partition_test_new
 WHERE object_type = 'foreign key constraint'
   AND object_name SIMILAR TO '%_p[1-9]%'
EXCEPT
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM hash_partition_test_new
 WHERE object_type = 'foreign key constraint'
   AND object_name NOT SIMILAR TO '%_p[1-9]%'   
 ORDER BY 5, 1, 2, 3;
\pset title 'Missing partition foreign key constraints' 
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM hash_partition_test_new
 WHERE object_type = 'foreign key constraint'
   AND object_name NOT SIMILAR TO '%_p[1-9]%'
EXCEPT
SELECT REGEXP_REPLACE(object_name, '_p([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(sub_object_name, '_p([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(sub_type, '_p([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') AS comment 
 FROM hash_partition_test_new
 WHERE object_type = 'foreign key constraint'
   AND object_name SIMILAR TO '%_p[1-9]%'
 ORDER BY 5, 1, 2, 3;
 
\pset title 'Extra partition triggers' 
SELECT REGEXP_REPLACE(object_name, '_p([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(sub_object_name, '_p([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(sub_type, '_p([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') AS comment 
 FROM hash_partition_test_new
 WHERE object_type = 'trigger'
   AND object_name SIMILAR TO '%_p[1-9]%'
EXCEPT
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM hash_partition_test_new
 WHERE object_type = 'trigger'
   AND object_name NOT SIMILAR TO '%_p[1-9]%'   
 ORDER BY 5, 1, 2, 3;
\pset title 'Missing partition triggers' 
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM hash_partition_test_new
 WHERE object_type = 'trigger'
   AND object_name NOT SIMILAR TO '%_p[1-9]%' 
   AND sub_object_name != (object_name||'_insert')::Text
EXCEPT
SELECT REGEXP_REPLACE(object_name, '_p([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(sub_object_name, '_p([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(sub_type, '_p([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') AS comment 
 FROM hash_partition_test_new
 WHERE object_type = 'trigger'
   AND object_name SIMILAR TO '%_p[1-9]%'
 ORDER BY 5, 1, 2, 3; 
 
--
-- Check for missing comments
--
\pset title 'Table list with comments'
WITH a AS (   
	SELECT cn.nspname AS schema_child, c.relname AS child, pn.nspname AS schema_parent, p.relname AS parent
	FROM pg_attribute b, pg_inherits 
			LEFT OUTER JOIN pg_class AS c ON (inhrelid=c.oid)
			LEFT OUTER JOIN pg_class as p ON (inhparent=p.oid)
			LEFT OUTER JOIN pg_namespace pn ON pn.oid = p.relnamespace
			LEFT OUTER JOIN pg_namespace cn ON cn.oid = c.relnamespace
	WHERE cn.nspname = 'rif40_partitions'
	  AND p.relkind  = 'r' 
	  AND p.relpersistence IN ('p', 'u') 
	  AND p.oid      = b.attrelid
	  AND b.attname  = 'study_id'
), c AS (
	SELECT 'C' parent_or_child, child AS table_name
	FROM a
	UNION
	SELECT 'P', parent
	FROM a
)
SELECT c.parent_or_child, n.nspname AS schema_owner, c.table_name, b.description
  FROM c, pg_class a
		LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid AND b.objsubid = 0)
		LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
 WHERE b.description IS NULL
   AND c.table_name = a.relname
 ORDER BY 1, 2;
		
--
-- Stop if errors
--
DO LANGUAGE plpgsql $$
DECLARE
	extra CURSOR FOR
		WITH y AS (
			SELECT ARRAY_AGG(a.tablename) AS table_list
			  FROM pg_tables a, pg_attribute b, pg_class c
	 		 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.tablename
  		       AND c.relkind    = 'r' /* Relational table */
		       AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		       AND b.attname    = 'study_id'
		       AND a.schemaname = 'rif40'	
		), z AS (
			SELECT *
				FROM hash_partition_test_new
			   WHERE object_name IN (SELECT UNNEST(y.table_list) FROM y)
				 AND NOT (sub_object_name = 'hash_partition_number' AND object_type = 'column')  /* Exclude hash column */
				 AND NOT (object_name||'_insert' = sub_object_name  AND object_type = 'trigger') /* Exclude partition insert trigger */  
			EXCEPT
			SELECT *
			FROM hash_partition_test_old
		)
		SELECT object_type, COUNT(sub_object_name) AS total
		  FROM z
		 GROUP BY object_type
		 ORDER BY object_type;
--
	missing CURSOR FOR
		WITH y AS (
			SELECT ARRAY_AGG(a.tablename) AS table_list
			  FROM pg_tables a, pg_attribute b, pg_class c
	 		 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.tablename
  		       AND c.relkind    = 'r' /* Relational table */
		       AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		       AND b.attname    = 'study_id'
		       AND a.schemaname = 'rif40'	
		), z AS (
			SELECT *
			  FROM hash_partition_test_old
			 WHERE object_name IN (SELECT UNNEST(y.table_list) FROM y)
			EXCEPT
			SELECT *
			FROM hash_partition_test_new
  		)
		SELECT object_type, COUNT(sub_object_name) AS total
		  FROM z
		 GROUP BY object_type
		 ORDER BY object_type;
--
	p_extra CURSOR FOR
		WITH a AS (
			SELECT REGEXP_REPLACE(object_name, '_p([0-9]){1,}', '', 'g') AS object_name, 
				   REGEXP_REPLACE(sub_object_name, '_p([0-9]){1,}', '', 'g') AS sub_object_name, 
				   schema, object_type, object_order, 
				   REGEXP_REPLACE(sub_type, '_p([0-9]){1,}', '', 'g') AS sub_type,
				   REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') AS comment 
			 FROM hash_partition_test_new
			 WHERE object_type NOT IN ('table', 'column', 'check constraint')
			   AND REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') NOT LIKE '%USING btree (_rif40_hash(%' /* Ignore hash index */		 
			   AND object_name SIMILAR TO '%_p[1-9]%'	
			EXCEPT	
			SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
			 FROM hash_partition_test_new
			 WHERE object_type NOT IN ('table', 'column', 'check constraint')
			   AND object_name NOT SIMILAR TO '%_p[1-9]%' 
		)
		SELECT object_type, COUNT(sub_object_name) AS total
		  FROM a
		 GROUP BY object_type
		 ORDER BY object_type;   
--
	p_missing CURSOR FOR
		WITH a AS (
			SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
			 FROM hash_partition_test_new
			 WHERE object_type NOT IN ('table', 'column', 'check constraint')
			   AND object_name NOT SIMILAR TO '%_p[1-9]%' 
			   AND sub_object_name != (object_name||'_insert')::Text
			EXCEPT
			SELECT REGEXP_REPLACE(object_name, '_p([0-9]){1,}', '', 'g') AS object_name, 
				   REGEXP_REPLACE(sub_object_name, '_p([0-9]){1,}', '', 'g') AS sub_object_name, 
				   schema, object_type, object_order, 
				   REGEXP_REPLACE(sub_type, '_p([0-9]){1,}', '', 'g') AS sub_type,
				   REGEXP_REPLACE(comment, '_p([0-9]){1,}', '', 'g') AS comment 
			 FROM hash_partition_test_new
			 WHERE object_type NOT IN ('table', 'column', 'check constraint')
			   AND object_name SIMILAR TO '%_p[1-9]%'	
		)
		SELECT object_type, COUNT(sub_object_name) AS total
		  FROM a
		 GROUP BY object_type
		 ORDER BY object_type;  
--
	no_table_comments CURSOR FOR
		WITH a AS (   
			SELECT cn.nspname AS schema_child, c.relname AS child, pn.nspname AS schema_parent, p.relname AS parent
			FROM pg_attribute b, pg_inherits 
					LEFT OUTER JOIN pg_class AS c ON (inhrelid=c.oid)
					LEFT OUTER JOIN pg_class as p ON (inhparent=p.oid)
					LEFT OUTER JOIN pg_namespace pn ON pn.oid = p.relnamespace
					LEFT OUTER JOIN pg_namespace cn ON cn.oid = c.relnamespace
			WHERE cn.nspname = 'rif40_partitions'
			  AND p.relkind  = 'r' 
			  AND p.relpersistence IN ('p', 'u') 
			  AND p.oid      = b.attrelid
			  AND b.attname  = 'study_id'
		), c AS (
			SELECT 'C' parent_or_child, child AS table_name
			FROM a
			UNION
			SELECT 'P', parent
			FROM a
		)
		SELECT c.parent_or_child, COUNT(c.table_name) AS total
		  FROM c, pg_class a
				LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid AND b.objsubid = 0)
				LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.description IS NULL
		   AND c.table_name = a.relname
		 GROUP BY c.parent_or_child
		 ORDER BY 1, 2;				
--
	extra_rec RECORD;
	missing_rec RECORD;
	no_table_comments_rec RECORD;
--
	errors INTEGER:=0;
--
BEGIN
	FOR extra_rec IN extra LOOP
		RAISE WARNING 'v4_0_alter_4.sql: Extra hash partition master object %: %', extra_rec.object_type, extra_rec.total;
		errors:=errors+extra_rec.total;
	END LOOP;
	FOR missing_rec IN missing LOOP
		RAISE WARNING 'v4_0_alter_4.sql: Missing hash partition master object %: %', missing_rec.object_type, missing_rec.total;
		errors:=errors+missing_rec.total;
	END LOOP;
	FOR extra_rec IN p_extra LOOP
		IF extra_rec.total > 0 THEN
			RAISE WARNING 'v4_0_alter_4.sql: Extra hash partition partition object %: %', extra_rec.object_type, extra_rec.total;
			errors:=errors+extra_rec.total;
		END IF;
	END LOOP;
	FOR missing_rec IN p_missing LOOP
		IF missing_rec.total > 0 THEN
			RAISE WARNING 'v4_0_alter_4.sql: Missing hash partition partition object %: %', missing_rec.object_type, missing_rec.total;
			errors:=errors+missing_rec.total;
		END IF;
	END LOOP;	
	FOR no_table_comments_rec IN no_table_comments LOOP
		IF no_table_comments_rec.total > 0 THEN
			RAISE WARNING 'v4_0_alter_4.sql: Missing hash partition partition comment %: %', 
				no_table_comments_rec.parent_or_child, no_table_comments_rec.total;
			errors:=errors+no_table_comments_rec.total;
		END IF;
	END LOOP;
--
-- DDL Check b) Missing table/view comments
--
	errors:=errors+rif40_sql_pkg.rif40_ddl_check_b();		
--
-- DDL Check j) Extra table/view columns
--
	errors:=errors+rif40_sql_pkg.rif40_ddl_check_j(); 	
--
-- DDL Check k) Missing comments
--
	errors:=errors+rif40_sql_pkg.rif40_ddl_check_k();
	
--
-- Stop on errors
--
	IF errors > 0 THEN
		RAISE EXCEPTION 'v4_0_alter_4.sql: C20999: % hash partition errors', errors; 
	END IF;
END;
$$;

DROP TABLE hash_partition_test_old;
DROP TABLE hash_partition_test_new;

--
-- Check imsert
--
SAVEPOINT rif40_studies_insert_test;
--
-- Test insert
--
--\set VERBOSITY verbose
DO LANGUAGE plpgsql $$
DECLARE	
	c1sm CURSOR FOR /* Get list of study_id tables with trigger functions */
		WITH a AS (
			SELECT DISTINCT table_name
			  FROM information_schema.columns
			 WHERE column_name = 'study_id'
			   AND table_name NOT LIKE 'g_rif40%'
			   AND table_name IN (
				SELECT table_name
				  FROM information_schema.tables
			 	 WHERE table_schema = 'rif40'
				   AND table_type = 'BASE TABLE')
		)
		SELECT TRANSLATE(SUBSTR(action_statement, STRPOS(action_statement, '.')+1), '()', '') AS function,
		       a.table_name, action_timing, COUNT(trigger_name) AS t
		  FROM a 
			LEFT OUTER JOIN information_schema.triggers b ON (
		  		trigger_schema = 'rif40'
		  	    AND action_timing IN ('BEFORE', 'AFTER') 
			    AND event_object_table = a.table_name)
		 GROUP BY TRANSLATE(SUBSTR(action_statement, STRPOS(action_statement, '.')+1), '()', ''),
		       a.table_name, action_timing
		 ORDER BY 1 DESC, 3;	
	c2sm CURSOR FOR 
		SELECT array_agg(level3) AS level3_array FROM sahsuland_level3;	
	c3sm CURSOR FOR
		SELECT COUNT(study_id) AS total 
		  FROM t_rif40_studies;
	c1sm_rec RECORD;	
	c2sm_rec RECORD;	
	c3sm_rec RECORD;	
--
	condition_array				VARCHAR[4][2]:='{{"SAHSULAND_ICD", "C34", NULL, NULL}, {"SAHSULAND_ICD", "162", "1629", NULL}}';	
										 /* investigation ICD conditions 2 dimensional array (i.e. matrix); 4 columnsxN rows:
											outcome_group_name, min_condition, max_condition, predefined_group_name */
	investigation_desc_array	VARCHAR[]:=array['Lung cancer'];
	covariate_array				VARCHAR[]:=array['SES'];	
--
	debug_level INTEGER:=1;
--
	v_sqlstate 			VARCHAR;
	v_context			VARCHAR;	
	v_detail 			VARCHAR;
	v_message_text		VARCHAR;
--
	start_nextval		INTEGER:=0;
BEGIN
--
-- Check if any studies exist; only run this section of code if study_id 1 has already been created. This occurs 
-- when this partitioning alter script (4) is in development ONLY
--
-- If you don't do this then study id 1 is never created (because of the SAVEPOINT/ROLLBACK) and test script 4 fails.
--
	OPEN c3sm;
	FETCH c3sm INTO c3sm_rec;
	CLOSE c3sm;	
	IF c3sm_rec.total = 0 THEN
		RAISE INFO 'v4_0_alter_4.sql: total studies: %; running study test code', c3sm_rec.total;
		RETURN;
	END IF;
--
	PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
	FOR c1sm_rec IN c1sm LOOP
		IF c1sm_rec.function IS NOT NULL THEN
			RAISE INFO 'v4_0_alter_4.sql: Enable debug for % trigger function: % on table: %',
				c1sm_rec.action_timing, c1sm_rec.function, c1sm_rec.table_name;
				PERFORM rif40_log_pkg.rif40_add_to_debug(c1sm_rec.function||':DEBUG'||debug_level::Text);
		ELSE
			RAISE WARNING 'v4_0_alter_4.sql: No trigger function found for table: %', c1sm_rec.table_name;
		END IF;
	END LOOP;
--
-- Get "SELECTED" study geolevels
--
	OPEN c2sm;
	FETCH c2sm INTO c2sm_rec;
	CLOSE c2sm;	
--
	RAISE INFO 'v4_0_alter_4.sql: Create new test study';
	start_nextval:=nextval('rif40_study_id_seq'::regclass)+1;
	PERFORM rif40_sm_pkg.rif40_create_disease_mapping_example(
		'SAHSU'::VARCHAR 			/* Geography */,
		'LEVEL1'::VARCHAR			/* Geolevel view */,
		'01'::VARCHAR				/* Geolevel area */,
		'LEVEL4'::VARCHAR			/* Geolevel map */,
		'LEVEL3'::VARCHAR			/* Geolevel select */,
		c2sm_rec.level3_array 		/* Geolevel selection array */,
		'TEST'::VARCHAR 			/* project */, 
		'SAHSULAND test 4 study_id 1 example'::VARCHAR /* study name */, 
		'SAHSULAND_POP'::VARCHAR 	/* denominator table */, 
		'SAHSULAND_CANCER'::VARCHAR /* numerator table */,
 		1989						/* year_start */, 
		1996						/* year_stop */,
		condition_array 			/* investigation ICD conditions 2 dimensional array (i.e. matrix); 4 columnsxN rows:
											outcome_group_name, min_condition, max_condition, predefined_group_name */,
		investigation_desc_array 	/* investigation description array */,
		covariate_array				/* covariate array */);	
		
--
-- Test partition movement is disallowed; or you will get a record in the wrong partition 
-- (there is no code to move the record)
--
	BEGIN
		UPDATE /* 1.1: -20239 expected  */ t_rif40_studies
		   SET study_id = study_id + 1;
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
			GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
			GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
			GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
			IF v_sqlstate = 'P0001' /* PL/pgSQL raise_exception */ AND '-20239' = v_detail THEN
				RAISE INFO 'v4_0_alter_4.sql: t_rif40_studies UPDATE caught: -20239 error as expected: %', v_message_text; 
			ELSE				 
				RAISE EXCEPTION 'v4_0_alter_4.sql: t_rif40_studies UPDATE expected: -20239 error; got: sqlstate: %, detail: %, message: %', 
					v_sqlstate, v_detail, v_message_text;
			END IF;
	END;	
	BEGIN
		UPDATE /* 1.2: -20727 expected */ t_rif40_investigations
		   SET study_id = study_id + 1;
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
			GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
			GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
			GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
			IF v_sqlstate = 'P0001' /* PL/pgSQL raise_exception */ AND '-20727' = v_detail THEN
				RAISE INFO 'v4_0_alter_4.sql: t_rif40_investigations UPDATE caught: -20727 error as expected: %', v_message_text; 
			ELSE				
				RAISE EXCEPTION 'v4_0_alter_4.sql: t_rif40_investigations UPDATE expected: -20727 error; got: sqlstate: %, detail: %, message: %', 
					v_sqlstate, v_detail, v_message_text;
			END IF;
	END;	
	BEGIN	
		UPDATE /* 1.3: -20504 expected  */ t_rif40_inv_conditions
		   SET study_id = study_id + 1;	
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
			GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
			GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
			GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
			IF v_sqlstate = 'P0001' /* PL/pgSQL raise_exception */ AND '-20504' = v_detail THEN
				RAISE INFO 'v4_0_alter_4.sql: t_rif40_inv_conditions UPDATE caught: -20504 error as expected: %', v_message_text; 
			ELSE				
				RAISE EXCEPTION 'v4_0_alter_4.sql: t_rif40_inv_conditions UPDATE expected: -20504 error; got: sqlstate: %, detail: %, message: %', 
					v_sqlstate, v_detail, v_message_text;
			END IF;
	END;
	BEGIN	
		UPDATE /* 1.4: -20304 expected  */ t_rif40_comparison_areas
		   SET study_id = study_id + 1;	
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
			GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
			GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
			GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
			IF v_sqlstate = 'P0001' /* PL/pgSQL raise_exception */ AND '-20304' = v_detail THEN
				RAISE INFO 'v4_0_alter_4.sql: t_rif40_comparison_areas UPDATE caught: -20304 error as expected: %', v_message_text; 
			ELSE				
				RAISE EXCEPTION 'v4_0_alter_4.sql: t_rif40_comparison_areas UPDATE expected: -20304 error; got: sqlstate: %, detail: %, message: %', 
					v_sqlstate, v_detail, v_message_text;
			END IF;
	END;
	BEGIN	
		UPDATE /* 1.5: -20284 expected  */ t_rif40_study_areas
		   SET study_id = study_id + 1;	
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
			GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
			GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
			GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
			IF v_sqlstate = 'P0001' /* PL/pgSQL raise_exception */ AND '-20284' = v_detail THEN
				RAISE INFO 'v4_0_alter_4.sql: t_rif40_study_areas UPDATE caught: -20284 error as expected: %', v_message_text; 
			ELSE				
				RAISE EXCEPTION 'v4_0_alter_4.sql: t_rif40_study_areas UPDATE expected: -20284 error; got: sqlstate: %, detail: %, message: %', 
					v_sqlstate, v_detail, v_message_text;
			END IF;
	END;	
	BEGIN	
		UPDATE /* 1.6: -20280 expected  */ t_rif40_inv_covariates
		   SET study_id = study_id + 1;	
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
			GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
			GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
			GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
			IF v_sqlstate = 'P0001' /* PL/pgSQL raise_exception */ AND '-20280' = v_detail THEN
				RAISE INFO 'v4_0_alter_4.sql: t_rif40_inv_covariates UPDATE caught: -20280 error as expected: %', v_message_text; 
			ELSE				
				RAISE EXCEPTION 'v4_0_alter_4.sql: t_rif40_inv_covariates UPDATE expected: -20280 error; got: sqlstate: %, detail: %, message: %', 
					v_sqlstate, v_detail, v_message_text;
			END IF;
	END;
	BEGIN	
		UPDATE /* 1.7: -20324 expected  */ rif40_study_shares
		   SET study_id = study_id + 1;	
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
			GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
			GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
			GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
			IF v_sqlstate = 'P0001' /* PL/pgSQL raise_exception */ AND '-20324' = v_detail THEN
				RAISE INFO 'v4_0_alter_4.sql: rif40_study_shares UPDATE caught: -20324 error as expected: %', v_message_text; 
			ELSE				
				RAISE EXCEPTION 'v4_0_alter_4.sql: rif40_study_shares UPDATE expected: -20324 error; got: sqlstate: %, detail: %, message: %', 
					v_sqlstate, v_detail, v_message_text;
			END IF;
	END;	
	RAISE INFO 'v4_0_alter_4.sql: Nextval - start; %, now: %', start_nextval, currval('rif40_study_id_seq'::regclass);
END;
$$;

ROLLBACK TO SAVEPOINT rif40_studies_insert_test;	

--DO LANGUAGE plpgsql $$
--BEGIN
--	RAISE INFO 'v4_0_alter_4.sql: Aborting (script being tested)';
--	RAISE EXCEPTION 'v4_0_alter_4.sql: C20999: Abort';
--END;
--$$;

/*
--
-- Reset sequences
--
DO LANGUAGE plpgsql $$
DECLARE	
	c5sm CURSOR FOR -- Old studies to delete - not study 1 
		SELECT COUNT(study_id) AS total,
		       ARRAY_AGG(study_id ORDER BY study_id) AS study_list
		  FROM t_rif40_studies;	
--		  
	c5sm_rec RECORD;
--
	sql_stmt VARCHAR[];
BEGIN
--
-- Check if no studies - reset study_id and inv_id sequences to 1
--
	OPEN c5sm;
	FETCH c5sm INTO c5sm_rec;
	CLOSE c5sm;
	IF c5sm_rec.total = 0 THEN
		sql_stmt[1]:='ALTER SEQUENCE rif40_study_id_seq MINVALUE 1';
		sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER SEQUENCE rif40_inv_id_seq MINVALUE 1';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
		RAISE INFO 'v4_0_alter_4.sql: No studies, resetting sequences';
	ELSE
		RAISE EXCEPTION 'v4_0_alter_4.sql: % studies: %; unable to reset sequence numbers', 
			c5sm_rec.total::Text, c5sm_rec.study_list::Text;
	END IF;
END;
$$;	
*/

END;

--
-- Eof
