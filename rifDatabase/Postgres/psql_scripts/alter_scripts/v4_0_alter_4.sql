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
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20900: User check failed: % is not rif40', user;	
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
\i ../psql_scripts/v4_0_study_id_partitions.sql

\dS+ t_rif40_investigations
\dS+ rif40_partitions.t_rif40_investigations_p1

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
		RAISE WARNING 'Extra hash partition master object %: %', extra_rec.object_type, extra_rec.total;
		errors:=errors+extra_rec.total;
	END LOOP;
	FOR missing_rec IN missing LOOP
		RAISE WARNING 'Missing hash partition master object %: %', missing_rec.object_type, missing_rec.total;
		errors:=errors+missing_rec.total;
	END LOOP;
	FOR extra_rec IN p_extra LOOP
		IF extra_rec.total > 0 THEN
			RAISE WARNING 'Extra hash partition partition object %: %', extra_rec.object_type, extra_rec.total;
			errors:=errors+extra_rec.total;
		END IF;
	END LOOP;
	FOR missing_rec IN p_missing LOOP
		IF missing_rec.total > 0 THEN
			RAISE WARNING 'Missing hash partition partition object %: %', missing_rec.object_type, missing_rec.total;
			errors:=errors+missing_rec.total;
		END IF;
	END LOOP;	
	FOR no_table_comments_rec IN no_table_comments LOOP
		IF no_table_comments_rec.total > 0 THEN
			RAISE WARNING 'Missing hash partition partition comment %: %', 
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
		RAISE EXCEPTION 'C20999: % hash partition errors', errors; 
	END IF;
END;
$$;

DROP TABLE hash_partition_test_old;
DROP TABLE hash_partition_test_new;

--
-- Check imsert
--
	SAVEPOINT rif40_studies_insert_test;
	INSERT /* 1 */ INTO rif40_studies (
                geography, project, study_name, study_type,
                comparison_geolevel_name, study_geolevel_name, denom_tab,
                year_start, year_stop, max_age_group, min_age_group,
                suppression_value, extract_permitted, transfer_permitted)
        VALUES (
                 'SAHSU'                                        /* geography */,
                 'TEST'                                                 /* project */,
                 'SAHSULAND test 4 study_id 1 example'                                  /* study_name */,
                 1                                                                      /* study_type [disease mapping] */,
                 'LEVEL2'       /* comparison_geolevel_name */,
                 'LEVEL4'                               /* study_geolevel_name */,
                 'SAHSULAND_POP'                                        /* denom_tab */,
                 1989                                   /* year_start */,
                 1996                                   /* year_stop */,
                 21     /* max_age_group */,
                 0      /* min_age_group */,
                 5 /* suppression_value */,
                 1              /* extract_permitted */,
                 1              /* transfer_permitted */);
				 
--
-- Check rename PK (test 4)
--				 
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT * FROM rif40_studies
		 WHERE study_id = 1;
	c2 CURSOR FOR 
		SELECT COUNT(study_id) AS total FROM rif40_studies;
--
	c1_rec RECORD;
    c2_rec RECORD;
--
-- RIF40_STUDY_SQL
-- RIF40_STUDY_SQL_LOG
-- RIF40_RESULTS
-- RIF40_CONTEXTUAL_STATS
-- RIF40_INV_CONDITIONS 
-- RIF40_INV_COVARIATES 
-- RIF40_INVESTIGATIONS 
-- RIF40_STUDY_AREAS 
-- RIF40_COMPARISON_AREAS 
-- RIF40_STUDY_SHARES
-- RIF40_STUDIES 
--
	rows 		INTEGER;
	sql_stmt 	VARCHAR[];
	debug_level	INTEGER;
BEGIN
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_ddl:DEBUG1'::Text);
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_rename_map_and_extract_tables:DEBUG1'::Text);
--
-- Fetch study 1
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	OPEN c2;
	FETCH c2 INTO c2_rec;
	CLOSE c2;                                                                   
--
-- Check study name
--
	IF c1_rec.study_name IS NULL AND c2_rec.total = 1 THEN
-- Make EXCEPTION                                                                                
        RAISE NOTICE 'v4_0_alter_4.sql: A4--32: no study 1';
        RETURN;
    ELSIF c1_rec.study_name IS NULL THEN
		RAISE EXCEPTION	'v4_0_alter_4.sql: A4--33: Test 4.8 no study 1 found; total = %', c2_rec.total::Text;
	ELSIF c1_rec.study_name != 'SAHSULAND test 4 study_id 1 example' THEN
		RAISE EXCEPTION	'v4_0_alter_4.sql: A4--34: Test 4.9; Study: 1 name (%) is not test 4 example', 
			c1_rec.study_name;
	END IF;
--
-- Check NOT study 1 - i.e. first run
--
	IF currval('rif40_study_id_seq'::regclass) = 1 THEN
        RAISE INFO 'v4_0_alter_4.sql: A4--35: Only study 1 present';                                                                          
		RETURN;
	END IF;
--
	sql_stmt[1]:='DELETE FROM rif40_inv_conditions'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_inv_covariates'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
-- Do full INSERT for study and comparison areas
-- This is to cope with expected geo-spatial changes
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_areas'||E'\n'||
'	 WHERE study_id = 1';
	sql_stmt[array_length(sql_stmt, 1)+1]:='INSERT INTO rif40_study_areas'||E'\n'||
'SELECT username, 1 study_id, area_id, band_id'||E'\n'||
'  FROM rif40_study_areas'||E'\n'||
' WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_areas'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_comparison_areas'||E'\n'||
'	 WHERE study_id = 1';
	sql_stmt[array_length(sql_stmt, 1)+1]:='INSERT INTO rif40_comparison_areas'||E'\n'||
'SELECT username, 1 study_id, area_id'||E'\n'||
'  FROM rif40_comparison_areas'||E'\n'||
' WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_comparison_areas'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_sql'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_sql_log'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
-- Do full INSERT for results
--
-- This will need to become more sophisticated if T_RIF40_RESULTS is modified
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_results'||E'\n'||
'	 WHERE study_id = 1';
	sql_stmt[array_length(sql_stmt, 1)+1]:='INSERT INTO rif40_results'||E'\n'||
'SELECT  username, 1 AS study_id, 1 AS inv_id, band_id, genders, direct_standardisation, adjusted, observed, expected, lower95,'||E'\n'||
'        upper95, relative_risk, smoothed_relative_risk, posterior_probability, posterior_probability_upper95,'||E'\n'||
'        posterior_probability_lower95, residual_relative_risk, residual_rr_lower95, residual_rr_upper95,'||E'\n'||
'        smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95 '||E'\n'||
'  FROM rif40_results'||E'\n'||
' WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_results'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_contextual_stats'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_study_shares'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';

--
-- Run
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Delete extract and map tables for <new study id>; 
-- rename <old study id> extract and map tables to <new study id> extract and map tables
--
	PERFORM rif40_sm_pkg.rif40_rename_map_and_extract_tables(currval('rif40_study_id_seq'::regclass)::INTEGER /* Old */, 1::INTEGER	/* New */);
--
-- Now delete study N
--
	sql_stmt:=NULL;
--
	sql_stmt[1]:='DELETE FROM rif40_investigations'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
	sql_stmt[array_length(sql_stmt, 1)+1]:='DELETE FROM rif40_studies'||E'\n'||
'	 WHERE study_id = currval(''rif40_study_id_seq''::regclass)';
--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	RAISE INFO 'v4_0_alter_4.sql: A4--36: Study: % renamed to study 1; rows processed: %', 
			currval('rif40_study_id_seq'::regclass)::VARCHAR, 
			rows::VARCHAR;
--
-- Fetch study 1
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
-- Check study name again
--
	IF c1_rec.study_name IS NULL THEN
		RAISE EXCEPTION	'v4_0_alter_4.sql: A4--37: Test 4.10 study 1 no longer found';
	ELSIF c1_rec.study_name != 'SAHSULAND test 4 study_id 1 example' THEN
		RAISE EXCEPTION	'v4_0_alter_4.sql: A4--38: Test 4.11; Study: 1 name (%) is no longer the test 4 example', 
			c1_rec.study_name;
	END IF;
END;
$$;

	ROLLBACK TO SAVEPOINT rif40_studies_insert_test;	

--	RAISE INFO 'v4_0_alter_4.sql: Aborting (script being tested)';
--	RAISE EXCEPTION 'v4_0_alter_4.sql: C20999: Abort';

END;

--
-- Eof
