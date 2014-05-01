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
-- Rapid Enquiry Facility (RIF) - Common partitioning functions
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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_create(
	l_schema 	VARCHAR, 
	master_table 	VARCHAR, 
	partition_table VARCHAR, 
	l_column	VARCHAR, 
	l_value		VARCHAR)
RETURNS void
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_common_partition_create()
Parameters:	Schema, master table, partition table, column, value
Returns:	Nothing
Description:	Clones master table primary and foreign keys; trigger, unique, check and exclusion 
		constraints; validation triggers, grants; table and column comments to partition 

Runs as RIF40 (so can create partition table DDL)

Generates the following SQL to:

* Add indexes, primary key
* Add foreign keys
* Add trigger, unique, check and exclusion constraints
* Validation triggers
* Add grants
* Table and column comments

Generates the following SQL>

GRANT SELECT ON rif40.sahsuland_cancer_1990 TO rif_manager;
GRANT SELECT ON rif40.sahsuland_cancer_1990 TO rif_user;
GRANT UPDATE, DELETE, INSERT, REFERENCES, TRIGGER, TRUNCATE, SELECT ON rif40.sahsuland_cancer_1990 TO rif40 WITH GRANT OPTION;
COMMENT ON TABLE sahsuland_cancer_1989 IS 'Range partition: sahsuland_cancer_1989 for value 1989 on column: year; master: rif40.sahsuland_cancer';
COMMENT ON COLUMN sahsuland_cancer_1989.age_sex_group IS'Age sex group';
COMMENT ON COLUMN sahsuland_cancer_1989.icd IS 'ICD';
COMMENT ON COLUMN sahsuland_cancer_1989.level1 IS 'level1';
COMMENT ON COLUMN sahsuland_cancer_1989.level2 IS 'level2';
COMMENT ON COLUMN sahsuland_cancer_1989.level3 IS 'level3';
COMMENT ON COLUMN sahsuland_cancer_1989.level4 IS 'level4';
COMMENT ON COLUMN sahsuland_cancer_1989.total IS 'Total';
COMMENT ON COLUMN sahsuland_cancer_1989.year IS 'Year';

 */
DECLARE
	c1rpcr CURSOR(l_schema VARCHAR, l_master_table VARCHAR) FOR /* Get column comments */
		WITH a AS (
	 		SELECT table_name, column_name, ordinal_position
			  FROM information_schema.columns a
				LEFT OUTER JOIN pg_tables b1 ON 
					(b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
			 WHERE table_schema = l_schema 
			   AND table_name   = l_master_table
		), b AS (
			SELECT table_name, column_name, ordinal_position, b.oid
  			  FROM a, pg_class b
			 WHERE b.relowner IN (SELECT oid FROM pg_roles WHERE rolname = l_schema) 
			   AND b.relname    = a.table_name
		)
		SELECT column_name, ordinal_position, c.description
		  FROM b
			LEFT OUTER JOIN pg_description c ON (c.objoid = b.oid AND c.objsubid = b.ordinal_position)
		 ORDER BY 1;	
--
-- Use pg_get ... def() functions so DDL is valid
--
	c4rpcr CURSOR(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR) FOR /* GET PK/unique index columns */
		SELECT n.nspname AS schema_name, 
		       t.relname AS table_name, 
		       i.relname AS index_name, 
		       array_to_string(array_agg(a.attname), ', ') AS column_names, 
		       pg_get_indexdef(i.oid) AS index_def,
		       CASE WHEN ix.indisprimary THEN 'ALTER TABLE '||n.nspname||'.'||t.relname||
					' ADD CONSTRAINT '||t.relname||'_pk '||pg_get_constraintdef(i.oid) 
		       ELSE NULL END AS constraint_def,
		       ix.indisprimary,
		       ix.indisunique
		 FROM pg_class t, pg_class i, pg_index ix, pg_attribute a, pg_namespace n
		 WHERE t.oid          = ix.indrelid
		   AND i.oid          = ix.indexrelid
		   AND a.attrelid     = t.oid
		   AND a.attnum       = ANY(ix.indkey)
		   AND t.relkind      = 'r'
		   AND t.relnamespace = n.oid 
		   AND n.nspname      = l_schema
		   AND t.relname      = l_table
		   AND a.attname      != l_column
		 GROUP BY n.nspname, t.relname, i.relname, ix.indisprimary, ix.indisunique, i.oid
		 ORDER BY n.nspname, t.relname, i.relname, ix.indisprimary DESC, ix.indisunique DESC, i.oid;
	c5rpcr CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get foreign keys */
		SELECT con.contype,
		       con.conname,
		       cl.relname AS parent_table, 
		       CASE 
				WHEN con.oid IS NOT NULL THEN 'ALTER TABLE '||con.schema_name||'.'||con.table_name||
					' ADD CONSTRAINT '||con.conname||' '||pg_get_constraintdef(con.oid) 
				ELSE NULL 
		       END AS constraint_def, 
		       array_to_string(array_agg(child_att.attname), ', ') AS child_columns, 
		       array_to_string(array_agg(parent_att.attname), ', ') AS parent_columns
		  FROM (
			SELECT unnest(con1.conkey) as parent, 
		               unnest(con1.confkey) as child, 
		               con1.contype, 
		               con1.conname, 
		               con1.confrelid, 
		               con1.conrelid,
		               con1.oid,
 		               cl.relname AS table_name,
			       ns.nspname AS schema_name  
			    FROM pg_class cl
			        LEFT OUTER JOIN pg_namespace ns ON (cl.relnamespace = ns.oid)
		        	LEFT OUTER JOIN pg_constraint con1 ON (con1.conrelid = cl.oid)
			   WHERE cl.relname   = l_table
 		     	     AND ns.nspname   = l_schema
			     AND con1.contype = 'f'
		   ) con /* Foreign keys */
		   LEFT OUTER JOIN pg_attribute parent_att ON
		       (parent_att.attrelid = con.confrelid AND parent_att.attnum = con.child)
		   LEFT OUTER JOIN pg_class cl ON
		       (cl.oid = con.confrelid)
		   LEFT OUTER JOIN pg_attribute child_att ON
 		      (child_att.attrelid = con.conrelid AND child_att.attnum = con.parent)
		 GROUP BY con.contype,
		       con.conname,
 		       cl.relname, 
		       CASE 
				WHEN con.oid IS NOT NULL THEN 'ALTER TABLE '||con.schema_name||'.'||con.table_name||
					' ADD CONSTRAINT '||con.conname||' '||pg_get_constraintdef(con.oid) 
				ELSE NULL 
		       END;
	c6rpcr CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get trigger, unique, check and exclusion constraints */
		SELECT CASE
				WHEN con.contype = 'x' THEN 'Exclusion'
				WHEN con.contype = 'c' THEN 'Check'
				WHEN con.contype = 't' THEN 'Trigger'
				WHEN con.contype = 'u' THEN 'Unique'
				ELSE '???????'
		       END AS constraint_type,	
	               con.conname, 
	               con.oid,
		       ns.nspname AS schema_name,
		       cl.relname AS table_name,
		       CASE 
				WHEN con.oid IS NOT NULL THEN 'ALTER TABLE '||ns.nspname||'.'||cl.relname||
					' ADD CONSTRAINT '||con.conname||' '||pg_get_constraintdef(con.oid) 
				ELSE NULL 
		       END AS constraint_def
		  FROM pg_constraint con
		        LEFT OUTER JOIN pg_namespace ns ON (con.connamespace = ns.oid)
		        LEFT OUTER JOIN pg_class cl ON (con.conrelid = cl.oid)
		 WHERE ns.nspname   = l_schema
		   AND cl.relname   = l_table
		   AND con.contype IN ('x', 'c', 't', 'u') /* trigger, unique, check and exclusion constraints */;
	c7rpcr CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get triggers */
		SELECT tg.tgname,
		       ns.nspname AS schema_name,
		       cl.relname AS table_name,
		       pc.proname AS function_name,
		       CASE
				WHEN tg.oid IS NOT NULL THEN pg_get_triggerdef(tg.oid)
				ELSE NULL
		       END AS trigger_def,
		       CASE
				WHEN tg.oid IS NOT NULL AND obj_description(tg.oid, 'pg_trigger') IS NOT NULL THEN 
					'COMMENT ON TRIGGER '||tg.tgname||' ON '||cl.relname||
					' IS '''||obj_description(tg.oid, 'pg_trigger')||''''
				ELSE NULL
		       END AS comment_def
		  FROM pg_trigger tg, pg_proc pc, pg_class cl
		        LEFT OUTER JOIN pg_namespace ns ON (cl.relnamespace = ns.oid)
		 WHERE tg.tgrelid            = cl.oid
		   AND ns.nspname            = l_schema
		   AND cl.relname            = l_table
		   AND cl.relname||'_insert' != pc.proname /* Ignore partition INSERT function */
		   AND tg.tgfoid             = pc.oid
		   AND tg.tgisinternal       = FALSE	   /* Ignore constraints triggers */;
	c8rpcr CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get triggers */
		SELECT table_name, grantee, grantor, table_schema, is_grantable,
                       array_to_string(array_agg(privilege_type::Text), ', ') AS privilege_types,
		       CASE 
				WHEN is_grantable = 'YES' THEN
				       'GRANT '||array_to_string(array_agg(privilege_type::Text), ', ')||
						' ON '||table_schema||'.'||table_name||' TO '||grantee||' WITH GRANT OPTION'
				ELSE
				       'GRANT '||array_to_string(array_agg(privilege_type::Text), ', ')||
						' ON '||table_schema||'.'||table_name||' TO '||grantee
		       END AS grant_def
		  FROM information_schema.role_table_grants 
		 WHERE table_name   = l_table
		   AND table_schema = l_schema
		 GROUP BY table_name, grantee, grantor, table_schema, is_grantable;
	c1_rec 		RECORD;
	c4_rec 		RECORD;
	c5_rec 		RECORD;
	c6_rec 		RECORD;
	c7_rec 		RECORD;
	c8_rec 		RECORD;
--
	ddl_stmt	VARCHAR[];
--
	i		INTEGER:=0;
	warnings	INTEGER:=0;
--
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_common_partition_create', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- Table and column comments
--
	ddl_stmt[1]:='COMMENT ON TABLE '||quote_ident(partition_table)||
		' IS ''Range partition: '||partition_table||' for value '||l_value||' on column: '||l_column||'; master: '||l_schema||'.'||master_table||'''';
	FOR c1_rec IN c1rpcr(l_schema, master_table) LOOP
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON COLUMN '||quote_ident(partition_table)||'.'||c1_rec.column_name||
		' IS '''||c1_rec.description||'''';
	END LOOP;

--
-- Add indexes, primary key
--
	FOR c4_rec IN c4rpcr(l_schema, master_table, l_column) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Index[%] % on: %.%(%); PK: %, Unique: %', 
			i::VARCHAR,
			c4_rec.index_name::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR, 
			c4_rec.column_names::VARCHAR, 
			c4_rec.indisprimary::VARCHAR, 
			c4_rec.indisunique::VARCHAR);
--		
		IF c4_rec.indisunique AND c4_rec.indisprimary THEN
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c4_rec.constraint_def::VARCHAR, master_table, partition_table);
		ELSE
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c4_rec.index_def::VARCHAR, master_table, partition_table);
		END IF;
	END LOOP;
	IF i > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added % indexes to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create', 'Added no indexes to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
		warnings:=warnings+1;
	END IF;

--
-- Add foreign keys
--
	i:=0;
	FOR c5_rec IN c5rpcr(l_schema, master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'FK Constraint[%] % on: %.%(%)', 
			i::VARCHAR,
			c5_rec.conname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR, 
			c5_rec.child_column::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c5_rec.constraint_def, master_table, partition_table);
	END LOOP;
	IF i > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added % foreign keys to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added no foreign keys to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	END IF;

--
-- Add trigger, unique, check and exclusion constraints
--
	i:=0;
	FOR c6_rec IN c6rpcr(l_schema, master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', '% constraint[%] % on: %.%(%)', 
			c6_rec.constraint_type::VARCHAR,
			i::VARCHAR,
			c6_rec.conname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR, 
			c6_rec.child_column::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c6_rec.constraint_def, master_table, partition_table);
	END LOOP;
	IF i > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added % trigger, unique, check and exclusion constraints to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added no trigger, unique, check and exclusion constraints to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	END IF;

--
-- Validation triggers
--
	i:=0;
	FOR c7_rec IN c7rpcr(l_schema, master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Validation trigger[%] % on: %.% calls %', 
			i::VARCHAR,
			c7_rec.tgname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR,
			c7_rec.function_name::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c7_rec.trigger_def, master_table, partition_table);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c7_rec.comment_def, master_table, partition_table);
	END LOOP;
	IF i > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added % validation triggers to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added no validation triggers to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	END IF;	

--
-- Add grants
--
	i:=0;
	FOR c8_rec IN c8rpcr(l_schema, master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Grant[%] % on: %.% to %; grant option: %', 
			i::VARCHAR,
			c8_rec.privilege_types::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR,
			c8_rec.grantee::VARCHAR,
			c8_rec.is_grantable::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c8_rec.grant_def, master_table, partition_table);
	END LOOP;
	IF i > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added % grants to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create', 'Added no grants to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
		warnings:=warnings+1;
	END IF;	

--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);
--
	IF warnings > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create', 
			'% warnings in cloning common attributes from master for partition: %.%', 
			warnings::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	END IF;	
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_create(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	_rif40_common_partition_create()
Parameters:	Schema, master table, partition table, column
Returns:	Nothing
Description:	Clones master table primary and foreign keys; trigger, unique, check and exclusion 
		constraints; validation triggers, grants; table and column comments to partition 

Runs as RIF40 (so can create partition table DDL)

Generates the following SQL to:

* Add indexes, primary key
* Add foreign keys
* Add trigger, unique, check and exclusion constraints
* Validation triggers
* Add grants
* Table and column comments

Generates the following SQL>

GRANT SELECT ON rif40.sahsuland_cancer_1990 TO rif_manager;
GRANT SELECT ON rif40.sahsuland_cancer_1990 TO rif_user;
GRANT UPDATE, DELETE, INSERT, REFERENCES, TRIGGER, TRUNCATE, SELECT ON rif40.sahsuland_cancer_1990 TO rif40 WITH GRANT OPTION;
COMMENT ON TABLE sahsuland_cancer_1989 IS ''Range partition: sahsuland_cancer_1989 for value 1989 on column: year; master: rif40.sahsuland_cancer'';
COMMENT ON COLUMN sahsuland_cancer_1989.age_sex_group IS ''Age sex group'';
COMMENT ON COLUMN sahsuland_cancer_1989.icd IS ''ICD'';
COMMENT ON COLUMN sahsuland_cancer_1989.level1 IS ''level1'';
COMMENT ON COLUMN sahsuland_cancer_1989.level2 IS ''level2'';
COMMENT ON COLUMN sahsuland_cancer_1989.level3 IS ''level3'';
COMMENT ON COLUMN sahsuland_cancer_1989.level4 IS ''level4'';
COMMENT ON COLUMN sahsuland_cancer_1989.total IS ''Total'';
COMMENT ON COLUMN sahsuland_cancer_1989.year IS ''Year'';';

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_create_2(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR,
       OUT ddl_stmt VARCHAR[], OUT num_partitions INTEGER, OUT total_rows INTEGER, OUT warnings INTEGER)
RETURNS RECORD
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_common_partition_create_2()
Parameters:	Schema, table, column, 
                [OUT] ddl statement array, [OUT] num_partitions, [OUT] total_rows, [OUT] warnings
Returns:	Nothing
 		DDL statement array is NULL if the function is unable to partition
Description:	Automatic range partition schema.table on column

* Must be rif40 or have rif_user or rif_manager role
* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)

  Add to DDL statement list:

	CREATE TEMPORARY TABLE rif40_range_partition AS SELECT * FROM rif40.sahsuland_cancer;
	TRUNCATE TABLE rif40.sahsuland_cancer;

* Do not partition if table has only one distinct row
* Do not partition if table has no rows

 */
DECLARE
	c1gangep 		REFCURSOR;
--
	sql_stmt 		VARCHAR;
	l_rows			INTEGER:=0;
	table_length		INTEGER:=0;
	name_length_limit	INTEGER:=40;	/* You may want to set this higher */
	i			INTEGER:=0;
	part_test_rec		RECORD;
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_common_partition_create_2', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
	warnings:=0;
--
-- Check if table is valid
--
	BEGIN
		sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(l_column)||')) AS num_partitions, COUNT('||quote_ident(l_column)||') AS total_rows FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||' LIMIT 1'; 
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_2', 'SQL> %;', sql_stmt::VARCHAR);
		OPEN c1gangep FOR EXECUTE sql_stmt;
		FETCH c1gangep INTO num_partitions, total_rows;
		GET DIAGNOSTICS l_rows = ROW_COUNT;
		CLOSE c1gangep;
		
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='_rif40_common_partition_create_2() caught: '||E'\n'||
				SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
			RAISE INFO '2: %', error_message;
--
			RAISE;
	END;
--
-- Check table name length - must be 25 chars or less (assuming the limit is 30)
--
	table_length:=length(quote_ident(l_table));
	IF table_length > name_length_limit-5 THEN
		PERFORM rif40_log_pkg.rif40_error(-20997, '_rif40_common_partition_create_2', 
			'Automatic range partitioning by %: %.%; table name is too long %, limit is %', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, table_length::VARCHAR, (name_length_limit-5)::VARCHAR);
-- 
-- IF yes, copy to temporary table, truncate
--
	ELSIF l_rows > 0 AND num_partitions > 1 THEN
--
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_common_partition_create_2', 
			'Automatic range partitioning by %: %.%; % partitions', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, num_partitions::VARCHAR);
--
-- Check data is partitionable 
--
/*
SELECT year AS value,
       SUBSTRING(year::Text FROM '[[:alnum:]_]{1,5}') AS valid_chars,
       COUNT(year) as total
  FROM rif40.sahsuland_cancer
 WHERE SUBSTRING(year::Text FROM '[[:alnum:]_]{1,5}') != year::Text
 GROUP BY year
 ORDER BY year;
*/
		BEGIN
			sql_stmt:='SELECT '||quote_ident(l_column)||' AS value, '||E'\n'||
'       SUBSTRING('||quote_ident(l_column)||'::Text FROM ''[[:alnum:]_]{1,'||(name_length_limit-table_length-1)::VARCHAR||'}'') AS valid_chars,'||E'\n'||
'       COUNT('||quote_ident(l_column)||') as total'||E'\n'||
'  FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||E'\n'||
' WHERE SUBSTRING('||quote_ident(l_column)||'::Text FROM ''[[:alnum:]_]{1,'||(name_length_limit-table_length-1)::VARCHAR||'}'') != '||quote_ident(l_column)||'::Text'||E'\n'||
' GROUP BY '||quote_ident(l_column)||E'\n'||
' ORDER BY '||quote_ident(l_column)||'';
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_2', 'SQL> %;', sql_stmt::VARCHAR);
			FOR part_test_rec IN EXECUTE sql_stmt LOOP
				i:=i+1;
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_2', 
					'Automatic range partitioning by %: %.%; partition % contains invalid characters to be part of a partition table name', 
					l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, part_test_rec.value::VARCHAR);
			END LOOP;
--
			IF i > 0 THEN
				PERFORM rif40_log_pkg.rif40_error(-20998, '_rif40_common_partition_create_2',
					'Automatic range partitioning by %: %.%; % partitions contains invalid characters to be part of partition table names', 
					l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, i::VARCHAR);
			END IF;
		EXCEPTION
			WHEN others THEN
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='_rif40_common_partition_create_2() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '2: %', error_message;
--
				RAISE;
		END;

--
-- Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)
--
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_2', 'Copy data to temporary table fron: %.%', 
			l_schema::VARCHAR, l_table::VARCHAR);
		ddl_stmt[1]:='CREATE TEMPORARY TABLE rif40_range_partition AS SELECT * FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='TRUNCATE TABLE '||quote_ident(l_schema)||'.'||quote_ident(l_table);

	ELSIF num_partitions > 1 THEN
--
-- Do not partition if table has only one distinct row
--
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_2', 'Unable to automatic range partitioning by %: %.%; Not partitionable, only 1 distinct row', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR);
	ELSE
--
-- Do not partition if table has no rows
--
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_2', 'Unable to automatic range partitioning by %: %.%; Not partitionable, no rows', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR);
	END IF;

END;
$func$ 
LANGUAGE plpgsql;

--\df+ rif40_sql_pkg._rif40_common_partition_create_2

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_create_2(VARCHAR, VARCHAR, VARCHAR, OUT VARCHAR[], OUT INTEGER, OUT INTEGER, OUT INTEGER) IS 'Function: 	_rif40_common_partition_create_2()
Parameters:	Schema, table, column, 
                [OUT] ddl statement array, [OUT] num_partitions, [OUT] total_rows, [OUT] warnings
Returns:	Nothing
 		DDL statement array is NULL if the function is unable to partition
Description:	Automatic range partition schema.table on column

* Must be rif40 or have rif_user or rif_manager role
* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)

  Add to DDL statement list:

	CREATE TEMPORARY TABLE rif40_range_partition AS SELECT * FROM rif40.sahsuland_cancer;
	TRUNCATE TABLE rif40.sahsuland_cancer;

* Do not partition if table has only one distinct row
* Do not partition if table has no rows';

--
-- Eof
