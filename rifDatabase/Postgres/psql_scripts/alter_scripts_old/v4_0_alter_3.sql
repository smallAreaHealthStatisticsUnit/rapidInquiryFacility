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
-- Rapid Enquiry Facility (RIF) - RIF alter script 3 - range partitioning
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

\echo Running SAHSULAND schema alter script #3 (Range partitioning, automatic clustering of denominator tables)...

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
 
CREATE LOCAL TEMPORARY TABLE range_partition_test_old
AS
WITH y AS (
			SELECT ARRAY_AGG(a.tablename) AS table_list
			  FROM pg_tables a, pg_attribute b, pg_class c
	 		 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.tablename
  		       AND c.relkind    = 'r' /* Relational table */
		       AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		       AND b.attname    = 'year'
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
\copy (SELECT * FROM range_partition_test_old ORDER BY 5, 2, 3) TO test_scripts/data/range_partition_test_old.csv WITH CSV HEADER

--
-- Range partition all tables with year as a column
-- This will cope with data already present in the table
--
\i ../psql_scripts/v4_0_year_partitions.sql

CREATE LOCAL TEMPORARY TABLE range_partition_test_new
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
	  AND b.attname  = 'year'
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
\copy (SELECT * FROM range_partition_test_new ORDER BY 5, 2, 3) TO test_scripts/data/range_partition_test_new.csv WITH CSV HEADER

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
 FROM range_partition_test_old
 WHERE object_name IN (SELECT UNNEST(y.table_list) FROM y)
EXCEPT
SELECT object_name, sub_object_name, object_type, sub_type, comment
  FROM range_partition_test_new
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
  FROM range_partition_test_new
 WHERE object_name IN (SELECT UNNEST(y.table_list) FROM y)
   AND NOT (object_name||'_insert' = sub_object_name  AND object_type = 'trigger') /* Exclude partition insert trigger */  
EXCEPT
SELECT object_name, sub_object_name, object_type, sub_type, comment
  FROM range_partition_test_old
ORDER BY 1, 2, 3;

--\dS+ sahsuland_cancer
--\dS+ p_sahsuland_cancer_1989
--
-- Then compare parent with children (i.e. check all partitions are set up correctly)
--
-- Ingnore object_type: TABLE, COLUMN; you are just testing INHERITS
-- 
\pset title 'Extra partition indexes'
SELECT REGEXP_REPLACE(REPLACE(object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(REPLACE(sub_object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(REPLACE(sub_type, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(REPLACE(comment, 'p_', ''), '_([0-9]){1,}', '', 'g') AS comment 
 FROM range_partition_test_new
 WHERE object_type LIKE '%index'
   AND object_name SIMILAR TO 'p%[1-2]([0-9]){3}%'
EXCEPT
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM range_partition_test_new
 WHERE object_type LIKE '%index'
   AND object_name NOT SIMILAR TO 'p%[1-2]([0-9]){3}%'   
 ORDER BY 5, 1, 2, 3;
\pset title 'Missing partition indexes'
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM range_partition_test_new
 WHERE object_type LIKE '%index'
   AND object_name NOT SIMILAR TO 'p%[1-2]([0-9]){3}%'   
EXCEPT
SELECT REGEXP_REPLACE(REPLACE(object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(REPLACE(sub_object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(REPLACE(sub_type, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(REPLACE(comment, 'p_', ''), '_([0-9]){1,}', '', 'g') AS comment 
 FROM range_partition_test_new
 WHERE object_type LIKE '%index'
   AND object_name SIMILAR TO 'p%[1-2]([0-9]){3}%'
 ORDER BY 5, 1, 2, 3;
 -- p_sahsuland_cancer_1989 => sahsuland_cancer
 -- p_sahsuland_cancer_1989_age_sex_group => sahsuland_cancer_age_group
\pset title 'Actual partition indexes - convert paritions to old form for comparision' 
SELECT DISTINCT REGEXP_REPLACE(REPLACE(object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(REPLACE(sub_object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(REPLACE(sub_type, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(REPLACE(comment, 'p_', ''), '_([0-9]){1,}', '', 'g') AS comment 
 FROM range_partition_test_new
 WHERE object_type LIKE '%index'
   AND object_name SIMILAR TO 'p%[1-2]([0-9]){3}%'
 ORDER BY 5, 1, 2, 3;
\pset title 'Actual partition indexes'  
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM range_partition_test_new
 WHERE object_type LIKE '%index'
   AND object_name NOT SIMILAR TO 'p%[1-2]([0-9]){3}%'  
 ORDER BY 5, 1, 2, 3;
-- Missing partition check constraints: not relevant
 
\pset title 'Extra partition foreign key constraints' 
SELECT REGEXP_REPLACE(REPLACE(object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(REPLACE(sub_object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(REPLACE(sub_type, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(REPLACE(comment, 'p_', ''), '_([0-9]){1,}', '', 'g') AS comment 
 FROM range_partition_test_new
 WHERE object_type = 'foreign key constraint'
   AND object_name SIMILAR TO 'p%[1-2]([0-9]){3}%'
EXCEPT
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM range_partition_test_new
 WHERE object_type = 'foreign key constraint'
   AND object_name NOT SIMILAR TO 'p%[1-2]([0-9]){3}%'   
 ORDER BY 5, 1, 2, 3;
\pset title 'Missing partition foreign key constraints' 
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM range_partition_test_new
 WHERE object_type = 'foreign key constraint'
   AND object_name NOT SIMILAR TO 'p%[1-2]([0-9]){3}%'
EXCEPT
SELECT REGEXP_REPLACE(REPLACE(object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(REPLACE(sub_object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(REPLACE(sub_type, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(REPLACE(comment, 'p_', ''), '_([0-9]){1,}', '', 'g') AS comment 
 FROM range_partition_test_new
 WHERE object_type = 'foreign key constraint'
   AND object_name SIMILAR TO 'p%[1-2]([0-9]){3}%'
 ORDER BY 5, 1, 2, 3;
 
\pset title 'Extra partition triggers' 
SELECT REGEXP_REPLACE(REPLACE(object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(REPLACE(sub_object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(REPLACE(sub_type, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(REPLACE(comment, 'p_', ''), '_([0-9]){1,}', '', 'g') AS comment 
 FROM range_partition_test_new
 WHERE object_type = 'trigger'
   AND object_name SIMILAR TO 'p%[1-2]([0-9]){3}%'
EXCEPT
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM range_partition_test_new
 WHERE object_type = 'trigger'
   AND object_name NOT SIMILAR TO 'p%[1-2]([0-9]){3}%'   
 ORDER BY 5, 1, 2, 3;
\pset title 'Missing partition triggers' 
SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
 FROM range_partition_test_new
 WHERE object_type = 'trigger'
   AND object_name NOT SIMILAR TO 'p%[1-2]([0-9]){3}%' 
   AND sub_object_name != (object_name||'_insert')::Text
EXCEPT
SELECT REGEXP_REPLACE(REPLACE(object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS object_name, 
       REGEXP_REPLACE(REPLACE(sub_object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_object_name, 
	   schema, object_type, object_order, 
	   REGEXP_REPLACE(REPLACE(sub_type, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_type,
	   REGEXP_REPLACE(REPLACE(comment, 'p_', ''), '_([0-9]){1,}', '', 'g') AS comment 
 FROM range_partition_test_new
 WHERE object_type = 'trigger'
   AND object_name SIMILAR TO 'p%[1-2]([0-9]){3}%'
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
	  AND b.attname  = 'year'
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
		       AND b.attname    = 'year'
		       AND a.schemaname = 'rif40'	
		), z AS (
			SELECT *
				FROM range_partition_test_new
			   WHERE object_name IN (SELECT UNNEST(y.table_list) FROM y)
				 AND NOT (object_name||'_insert' = sub_object_name  AND object_type = 'trigger') /* Exclude partition insert trigger */  
			EXCEPT
			SELECT *
			FROM range_partition_test_old
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
		       AND b.attname    = 'year'
		       AND a.schemaname = 'rif40'	
		), z AS (
			SELECT *
			  FROM range_partition_test_old
			 WHERE object_name IN (SELECT UNNEST(y.table_list) FROM y)
			EXCEPT
			SELECT *
			FROM range_partition_test_new
  		)
		SELECT object_type, COUNT(sub_object_name) AS total
		  FROM z
		 GROUP BY object_type
		 ORDER BY object_type;
--
	p_extra CURSOR FOR
		WITH a AS (
			SELECT REGEXP_REPLACE(REPLACE(object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS object_name, 
				   REGEXP_REPLACE(REPLACE(sub_object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_object_name, 
				   schema, object_type, object_order, 
				   REGEXP_REPLACE(REPLACE(sub_type, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_type,
				   REGEXP_REPLACE(REPLACE(comment, 'p_', ''), '_([0-9]){1,}', '', 'g') AS comment 
			 FROM range_partition_test_new
			 WHERE object_type NOT IN ('table', 'column', 'check constraint')	 
			   AND object_name SIMILAR TO 'p%[1-2]([0-9]){3}%'	
			EXCEPT	
			SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
			 FROM range_partition_test_new
			 WHERE object_type NOT IN ('table', 'column', 'check constraint')
			   AND object_name NOT SIMILAR TO 'p%[1-2]([0-9]){3}%' 
		)
		SELECT object_type, COUNT(sub_object_name) AS total
		  FROM a
		 GROUP BY object_type
		 ORDER BY object_type;   
--
	p_missing CURSOR FOR
		WITH a AS (
			SELECT object_name, sub_object_name, schema, object_type, object_order, sub_type, comment
			 FROM range_partition_test_new
			 WHERE object_type NOT IN ('table', 'column', 'check constraint')
			   AND object_name NOT SIMILAR TO 'p%[1-2]([0-9]){3}%' 
			   AND sub_object_name != (object_name||'_insert')::Text
			EXCEPT
			SELECT REGEXP_REPLACE(REPLACE(object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS object_name, 
				   REGEXP_REPLACE(REPLACE(sub_object_name, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_object_name, 
				   schema, object_type, object_order, 
				   REGEXP_REPLACE(REPLACE(sub_type, 'p_', ''), '_([0-9]){1,}', '', 'g') AS sub_type,
				   REGEXP_REPLACE(REPLACE(comment, 'p_', ''), '_([0-9]){1,}', '', 'g') AS comment 
			 FROM range_partition_test_new
			 WHERE object_type NOT IN ('table', 'column', 'check constraint')
			   AND object_name SIMILAR TO 'p%[1-2]([0-9]){3}%'	
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
			  AND b.attname  = 'year'
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
		RAISE WARNING 'Extra range partition master object %: %', extra_rec.object_type, extra_rec.total;
		errors:=errors+extra_rec.total;
	END LOOP;
	FOR missing_rec IN missing LOOP
		RAISE WARNING 'Missing range partition master object %: %', missing_rec.object_type, missing_rec.total;
		errors:=errors+missing_rec.total;
	END LOOP;
	FOR extra_rec IN p_extra LOOP
		IF extra_rec.total > 0 THEN
			RAISE WARNING 'Extra range partition partition object %: %', extra_rec.object_type, extra_rec.total;
			errors:=errors+extra_rec.total;
		END IF;
	END LOOP;
	FOR missing_rec IN p_missing LOOP
		IF missing_rec.total > 0 THEN
			RAISE WARNING 'Missing range partition partition object %: %', missing_rec.object_type, missing_rec.total;
			errors:=errors+missing_rec.total;
		END IF;
	END LOOP;	
	FOR no_table_comments_rec IN no_table_comments LOOP
		IF no_table_comments_rec.total > 0 THEN
			RAISE WARNING 'Missing range partition partition comment %: %', 
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
		RAISE EXCEPTION 'C20999: % range partition errors', errors; 
	END IF;
END;
$$;

DROP TABLE range_partition_test_old;
DROP TABLE range_partition_test_new;

--DO LANGUAGE plpgsql $$
--BEGIN
--	RAISE INFO 'Aborting (script being tested)';
--	RAISE EXCEPTION 'C20999: Abort';
--END;
--$$;

END;

--
-- Eof
