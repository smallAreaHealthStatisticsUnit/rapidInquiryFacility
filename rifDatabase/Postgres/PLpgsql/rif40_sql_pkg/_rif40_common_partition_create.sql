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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_hash(l_value VARCHAR, l_bucket INTEGER)
RETURNS INTEGER
AS $func$
/*
Function: 	_rif40_hash()
Parameters:	Value (must be cast if required), number of buckets
Returns:	Hash in the range 1 .. l_bucket 
Description:	Hashing function
 */
DECLARE
BEGIN
	RETURN (ABS(hashtext(l_value))%l_bucket)+1;
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_hash(VARCHAR, INTEGER) IS 'Function: 	_rif40_hash()
Parameters:	Value (must be cast if required), number of buckets
Returns:	Hash in the range 1 .. l_bucket 
Description:	Hashing function';

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_triggers(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, enable_or_disable VARCHAR, OUT ddl_stmt VARCHAR[])
RETURNS VARCHAR[]
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_common_partition_triggers()
Parameters:	Schema, table, column, enable or disable
Returns:	DDL statement array
Description:	Automatic range partitioning schema.table on column: ENABLE or DISABLE ON-INSERT triggers
 */
DECLARE
	c1rpct CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get triggers */
		SELECT tg.tgname,
		       ns.nspname AS schema_name,
		       cl.relname AS table_name,
		       pc.proname AS function_name,
		       CASE
				WHEN tg.oid IS NOT NULL THEN pg_get_triggerdef(tg.oid)
				ELSE NULL
		       END AS trigger_def
		  FROM pg_trigger tg, pg_proc pc, information_schema.triggers tgi, pg_class cl
		        LEFT OUTER JOIN pg_namespace ns ON (cl.relnamespace = ns.oid)
		 WHERE tg.tgrelid              = cl.oid
		   AND ns.nspname              = l_schema
		   AND cl.relname              = l_table
		   AND tgi.event_object_table  = l_table
		   AND tgi.event_object_schema = l_schema
		   AND tgi.trigger_name        = tg.tgname
		   AND tgi.event_manipulation  = 'INSERT'
		   AND cl.relname||'_insert'  != pc.proname /* Ignore partition INSERT function */
		   AND tg.tgfoid               = pc.oid
		   AND tg.tgisinternal         = FALSE	   /* Ignore constraints triggers */;
	c1_rec RECORD;
--
	i INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_common_partition_triggers', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- Check enable or disable parameter
--
	IF enable_or_disable NOT IN ('ENABLE', 'DISABLE') THEN
		PERFORM rif40_log_pkg.rif40_error(-20997, '_rif40_common_partition_triggers', 'Invalid parameter: % must be ''ENABLE'', ''DISABLE''',
			enable_or_disable::VARCHAR);
	END IF;
--
	FOR c1_rec IN c1rpct(l_schema, l_table) LOOP
		i:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_triggers', 'Trigger [%] %.%(%): %', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			c1_rec.tgname::VARCHAR, 
			c1_rec.function_name::VARCHAR, 
			enable_or_disable::VARCHAR);
		ddl_stmt[i]:='ALTER TABLE '||l_schema||'.'||l_table||' '||enable_or_disable||' TRIGGER '||c1_rec.tgname;
	END LOOP;
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_triggers(VARCHAR, VARCHAR, VARCHAR, VARCHAR, OUT VARCHAR[]) IS 'Function: 	_rif40_common_partition_triggers()
Parameters:	Schema, table, column, enable or disable
Returns:	DDL statement array
Description:	Automatic range partitioning schema.table on column: ENABLE or DISABLE ON-INSERT triggers';

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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_create_setup(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR,
       OUT ddl_stmt VARCHAR[], OUT num_partitions INTEGER, OUT total_rows INTEGER, OUT warnings INTEGER)
RETURNS RECORD
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_common_partition_create_setup()
Parameters:	Schema, table, column, 
                [OUT] ddl statement array, [OUT] num_partitions, [OUT] total_rows, [OUT] warnings
Returns:	OUT parameters as a record
 		DDL statement array is NULL if the function is unable to partition
Description:	Automatic range/hash partition schema.table on column
		Prequiste checks, COPY <table> to rif40_auto_partition, truncate <table>

* Must be rif40 or have rif_user or rif_manager role
* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)

  Add to DDL statement list:

	CREATE TEMPORARY TABLE rif40_auto_partition AS SELECT * FROM rif40.sahsuland_cancer;
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
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_common_partition_create_setup', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
	warnings:=0;
--
-- Check if table is valid
--
	BEGIN
		sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(l_column)||')) AS num_partitions, COUNT('||quote_ident(l_column)||') AS total_rows FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||' LIMIT 1'; 
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 'SQL> %;', sql_stmt::VARCHAR);
		OPEN c1gangep FOR EXECUTE sql_stmt;
		FETCH c1gangep INTO num_partitions, total_rows;
		GET DIAGNOSTICS l_rows = ROW_COUNT;
		CLOSE c1gangep;
		
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='_rif40_common_partition_create_setup() caught: '||E'\n'||
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
		PERFORM rif40_log_pkg.rif40_error(-20997, '_rif40_common_partition_create_setup', 
			'Automatic range/hash partition by %: %.%; table name is too long %, limit is %', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, table_length::VARCHAR, (name_length_limit-5)::VARCHAR);
-- 
-- IF yes, copy to temporary table, truncate
--
	ELSIF l_rows > 0 AND num_partitions > 1 THEN
--
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_common_partition_create_setup', 
			'Automatic range/hash partition by %: %.%; % partitions', 
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
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 'SQL> %;', sql_stmt::VARCHAR);
			FOR part_test_rec IN EXECUTE sql_stmt LOOP
				i:=i+1;
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_setup', 
					'Automatic range/hash partition by %: %.%; partition % contains invalid characters to be part of a partition table name', 
					l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, part_test_rec.value::VARCHAR);
			END LOOP;
--
			IF i > 0 THEN
				PERFORM rif40_log_pkg.rif40_error(-20998, '_rif40_common_partition_create_setup',
					'Automatic range/hash partition by %: %.%; % partitions contains invalid characters to be part of partition table names', 
					l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, i::VARCHAR);
			END IF;
		EXCEPTION
			WHEN others THEN
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='_rif40_common_partition_create_setup() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '2: %', error_message;
--
				RAISE;
		END;

--
-- Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)
--
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 'Copy data to temporary table fron: %.%', 
			l_schema::VARCHAR, l_table::VARCHAR);
		ddl_stmt[1]:='CREATE TEMPORARY TABLE rif40_auto_partition AS SELECT * FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='TRUNCATE TABLE '||quote_ident(l_schema)||'.'||quote_ident(l_table);

	ELSIF num_partitions > 1 THEN
--
-- Do not partition if table has only one distinct row
--
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_setup', 'Unable to automatic range/hash partition by %: %.%; Not partitionable, only 1 distinct row', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR);
	ELSE
--
-- Warn if table has no rows or only 1 partition
--
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_setup', 'Automatic range/hash partition by %: %.%; no rows (%)/only 1 partition (%)', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR,
			l_rows::VARCHAR, num_partitions::VARCHAR);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 'Copy data to temporary table fron: %.%', 
			l_schema::VARCHAR, l_table::VARCHAR);
--
-- Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)
--

		ddl_stmt[1]:='CREATE TEMPORARY TABLE rif40_auto_partition AS SELECT * FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='TRUNCATE TABLE '||quote_ident(l_schema)||'.'||quote_ident(l_table);
	END IF;

END;
$func$ 
LANGUAGE plpgsql;

--\df+ rif40_sql_pkg._rif40_common_partition_create_setup

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_create_setup(VARCHAR, VARCHAR, VARCHAR, OUT VARCHAR[], OUT INTEGER, OUT INTEGER, OUT INTEGER) IS 'Function: 	_rif40_common_partition_create_setup()
Parameters:	Schema, table, column, 
                [OUT] ddl statement array, [OUT] num_partitions, [OUT] total_rows, [OUT] warnings
Returns:	OUT parameters as a record
 		DDL statement array is NULL if the function is unable to partition
Description:	Automatic range/hash partition schema.table on column
		Prequiste checks, COPY <table> to rif40_auto_partition, truncate <table>

* Must be rif40 or have rif_user or rif_manager role
* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)

  Add to DDL statement list:

	CREATE TEMPORARY TABLE rif40_auto_partition AS SELECT * FROM rif40.sahsuland_cancer;
	TRUNCATE TABLE rif40.sahsuland_cancer;

* Do not partition if table has only one distinct row
* Do not partition if table has no rows';

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_create_insert(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, total_rows INTEGER,
	OUT ddl_stmt VARCHAR[], OUT index_name VARCHAR)
RETURNS RECORD
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_common_partition_create_insert()
Parameters:	Schema, table, column, total rows
                [OUT] ddl statement array, [PK/UK] index name
Returns:	OUT parameters as a record
 		DDL statement array is NULL if the function is unable to partition
Description:	Automatic range/hash partition schema.table on column
		INSERT

* Foreach partition:
+	INSERT 1 rows. This creates the partition

	INSERT INTO sahsuland_cancer /- Create partition 1989 -/
	SELECT * FROM rif40_range_partition /- Temporary table -/
	 WHERE year = '1989'
	 LIMIT 1;

+	TRUNCATE partition

	TRUNCATE TABLE rif40.sahsuland_cancer_1989 /- Empty newly created partition 1989 -/;

+ 	Bring data back by partition, order by range partition, primary key

	INSERT INTO sahsuland_cancer_1989 /- Directly populate partition: 1989, total rows expected: 8103 -/
	SELECT * FROM rif40_range_partition /- Temporary table -/
	 WHERE year = '1989'
	 ORDER BY year /- Partition column -/, age_sex_group, icd, level4 /- [Rest of ] primary key -/;

* The trigger created earlier fires and calls sahsuland_cancer_insert();
  This then call _rif40_range_partition_create() for the first row in a partition (detected by trapping the undefined_table EXCEPTION 
  e.g. 42p01: relation "rif40.rif40_population_europe_1991" does not exist) 

psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  _rif40_range_partition_create(): Create range partition: sahsuland_cancer_1989 for value 1989 on column: year; master: rif40.sahsuland_cancer

  The trigger then re-fires to redo the bind insert. NEW.<column name> must be explicitly defined unlike in conventional INSERT triggers

psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  [DEBUG1] sahsuland_cancer_insert(): Row 1 SQL> EXECUTE 
'INSERT INTO sahsuland_cancer_1989 VALUES ($1, $2, $3, $4, $5, $6, $7, $8) /- Partition: 1989 -/' 
USING NEW.year, NEW.age_sex_group, NEW.level1, NEW.level2, NEW.level3, NEW.level4, NEW.icd, NEW.total; 
/- rec: (1989,100,01,01.008,01.008.006800,01.008.006800.1,1890,2) -/

 */
DECLARE
	c3gangep CURSOR(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR) FOR /* GET PK/unique index column */
		SELECT n.nspname AS schema_name, t.relname AS table_name, 
		       i.relname AS index_name, array_to_string(array_agg(a.attname), ', ') AS column_names, ix.indisprimary
		 FROM pg_class t, pg_class i, pg_index ix, pg_attribute a, pg_namespace n
		 WHERE t.oid          = ix.indrelid
		   AND i.oid          = ix.indexrelid
		   AND a.attrelid     = t.oid
		   AND a.attnum       = ANY(ix.indkey)
		   AND t.relkind      = 'r'
		   AND ix.indisunique = TRUE
		   AND t.relnamespace = n.oid 
		   AND n.nspname      = l_schema
		   AND t.relname      = l_table
		   AND a.attname      != l_column
		 GROUP BY n.nspname, t.relname, i.relname, ix.indisprimary
		 ORDER BY n.nspname, t.relname, i.relname, ix.indisprimary DESC;
	c3_rec 			RECORD;
	c6_rec 			RECORD;
--
	sql_stmt		VARCHAR;
	l_ddl_stmt		VARCHAR[];
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_common_partition_create_insert', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- Disable ON-INSERT triggers to avoid:
--
-- /* psql:../psql_scripts/v4_0_study_id_partitions.sql:139: WARNING:  rif40_ddl(): SQL in error (P0001)> INSERT INTO rif40_study_shares /* Create partition 1 */
-- SELECT * FROM rif40_auto_partition /* Temporary table */
--  WHERE study_id = '1'
--  LIMIT 1;
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:139: ERROR:  rif40_trg_pkg.trigger_fct_rif40_study_shares_checks(): RIF40_STUDY_SHARES study_id: 1 grantor username: pch is not USER: rif40 or a RIF40_MANAGER
--
	l_ddl_stmt:=rif40_sql_pkg._rif40_common_partition_triggers(l_schema, l_table, l_column, 'DISABLE'::VARCHAR);
	IF l_ddl_stmt IS NOT NULL THEN
--
-- Copy out parameters
--
		FOR i IN 1 .. array_length(l_ddl_stmt, 1) LOOP
			ddl_stmt[i]:=l_ddl_stmt[i];
		END LOOP;
	END IF;

--
-- GET PK/unique index column
--
	OPEN c3gangep(l_schema, l_table, l_column);
	FETCH c3gangep INTO c3_rec;
	CLOSE c3gangep;
	index_name:=c3_rec.index_name;
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_insert', 'Restore data from temporary table: %.%', 
		l_schema::VARCHAR, l_table::VARCHAR);
--
-- Create list of potential partitions
--
	IF total_rows > 0 THEN
		BEGIN
			sql_stmt:='SELECT '||quote_ident(l_column)||' AS partition_value, COUNT('||quote_ident(l_column)||') AS total_rows'||E'\n'||
				'  FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||E'\n'||
				' GROUP BY '||quote_ident(l_column)||E'\n'||
				' ORDER BY 1'; 
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_insert', 'SQL> %;', sql_stmt::VARCHAR);
			FOR c6_rec IN EXECUTE sql_stmt LOOP
--
				IF ddl_stmt IS NULL THEN
					ddl_stmt[1]:='INSERT INTO '||quote_ident(l_table)||
						' /* Create partition '||c6_rec.partition_value||' */'||E'\n'||
						'SELECT * FROM rif40_auto_partition /* Temporary table */'||E'\n'||
						' WHERE '||l_column||' = '''||c6_rec.partition_value||''''||E'\n'||
						' LIMIT 1';
				ELSE
					ddl_stmt[array_length(ddl_stmt, 1)+1]:='INSERT INTO '||quote_ident(l_table)||
						' /* Create partition '||c6_rec.partition_value||' */'||E'\n'||
						'SELECT * FROM rif40_auto_partition /* Temporary table */'||E'\n'||
						' WHERE '||l_column||' = '''||c6_rec.partition_value||''''||E'\n'||
						' LIMIT 1';
				END IF;
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='TRUNCATE TABLE '||
					quote_ident(l_schema)||'.'||quote_ident(l_table)||'_'||c6_rec.partition_value||
					' /* Empty newly created partition '||c6_rec.partition_value||' */';
--				
-- Bring data back, order by range partition, primary key
--
				IF c3_rec.column_names IS NOT NULL THEN
					ddl_stmt[array_length(ddl_stmt, 1)+1]:='INSERT INTO '||quote_ident(l_table)||'_'||c6_rec.partition_value||
						' /* Directly populate partition: '||c6_rec.partition_value||
						', total rows expected: '||c6_rec.total_rows||' */'||E'\n'||
						'SELECT * FROM rif40_auto_partition /* Temporary table */'||E'\n'||
						' WHERE '||l_column||' = '''||c6_rec.partition_value||''''||E'\n'||
						' ORDER BY '||l_column||' /* Partition column */, '||
						c3_rec.column_names||' /* [Rest of ] primary key */';
				ELSE
					ddl_stmt[array_length(ddl_stmt, 1)+1]:='INSERT INTO '||quote_ident(l_table)||'_'||c6_rec.partition_value||
						' /* Directly populate partition: '||c6_rec.partition_value||
						', total rows expected: '||c6_rec.total_rows||' */'||E'\n'||
						'SELECT * FROM rif40_auto_partition /* Temporary table */'||E'\n'||
						' WHERE '||l_column||' = '''||c6_rec.partition_value||''''||E'\n'||
						' ORDER BY '||l_column||' /* Partition column */, '||
						' /* NO [Rest of ] primary key - no unique index found */';
				END IF;
--
			END LOOP;	
		EXCEPTION
			WHEN others THEN
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='_rif40_common_partition_create_insert() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '2: %', error_message;
--
				RAISE;
		END;
	END IF;
--
-- Re-enable ON-INSERT triggers
--
	l_ddl_stmt:=rif40_sql_pkg._rif40_common_partition_triggers(l_schema, l_table, l_column, 'ENABLE'::VARCHAR);
	IF l_ddl_stmt IS NOT NULL THEN
		FOR i IN 1 .. array_length(l_ddl_stmt, 1) LOOP
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=l_ddl_stmt[i];
		END LOOP;
	END IF;

--
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_create_insert(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, total_rows INTEGER,
	OUT ddl_stmt VARCHAR[], OUT index_name VARCHAR) IS 'Function: 	_rif40_common_partition_create_insert()
Parameters:	Schema, table, column, total rows
                [OUT] ddl statement array, [PK/UK] index name
Returns:	OUT parameters as a record
 		DDL statement array is NULL if the function is unable to partition
Description:	Automatic range/hash partition schema.table on column
		INSERT

* Foreach partition:
Call: _rif40_common_partition_create_insert()

* Foreach partition:
+	INSERT 1 rows. This creates the partition
+	TRUNCATE partition
+ 	Bring data back by partition, order by range partition, primary key
[End of _rif40_common_partition_create_insert()]';

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_create_complete(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, index_name VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_common_partition_create_complete()
Parameters:	Schema, table, column, UK/PK indexname
Returns:	Nothing
Description:	Automatic range partition schema.table on column
		Completion tasks

* If table is a numerator, cluster each partition (not the master table)

CLUSTER VERBOSE rif40.sahsuland_pop_1989 USING sahsuland_pop_1989_pk;
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  clustering "rif40.sahsuland_pop_1989" using sequential scan and sort
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_pop_1989": found 0 removable, 54120 nonremovable row versions in 558 pages

* Re-anaylse

ANALYZE VERBOSE rif40.sahsuland_cancer;
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  analyzing "rif40.sahsuland_cancer"
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_cancer": scanned 0 of 0 pages, containing 0 live rows and 0 dead rows; 0 rows in sample, 0 estimated total rows
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  analyzing "rif40.sahsuland_cancer" inheritance tree
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_cancer_1989": scanned 93 of 93 pages, containing 8103 live rows and 0 dead rows; 3523 rows in sample, 8103 estimated total rows
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_cancer_1990": scanned 94 of 94 pages, containing 8244 live rows and 0 dead rows; 3561 rows in sample, 8244 estimated total rows
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_cancer_1991": scanned 95 of 95 pages, containing 8357 live rows and 0 dead rows; 3598 rows in sample, 8357 estimated total rows
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_cancer_1992": scanned 102 of 102 pages, containing 8971 live rows and 0 dead rows; 3864 rows in sample, 8971 estimated total rows
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_cancer_1993": scanned 103 of 103 pages, containing 9052 live rows and 0 dead rows; 3902 rows in sample, 9052 estimated total rows
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_cancer_1994": scanned 103 of 103 pages, containing 8978 live rows and 0 dead rows; 3902 rows in sample, 8978 estimated total rows
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_cancer_1995": scanned 103 of 103 pages, containing 9043 live rows and 0 dead rows; 3902 rows in sample, 9043 estimated total rows
psql:../psql_scripts/v4_0_year_partitions.sql:150: INFO:  "sahsuland_cancer_1996": scanned 99 of 99 pages, containing 8707 live rows and 0 dead rows; 3748 rows in sample, 8707 estimated total rows

* Drop temporary table:

DROP TABLE rif40_range_partition /- Temporary table -/;

 */
DECLARE
	c3gangep CURSOR(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR) FOR /* GET PK/unique index column */
		SELECT n.nspname AS schema_name, t.relname AS table_name, 
		       i.relname AS index_name, array_to_string(array_agg(a.attname), ', ') AS column_names, ix.indisprimary
		 FROM pg_class t, pg_class i, pg_index ix, pg_attribute a, pg_namespace n
		 WHERE t.oid          = ix.indrelid
		   AND i.oid          = ix.indexrelid
		   AND a.attrelid     = t.oid
		   AND a.attnum       = ANY(ix.indkey)
		   AND t.relkind      = 'r'
		   AND ix.indisunique = TRUE
		   AND t.relnamespace = n.oid 
		   AND n.nspname      = l_schema
		   AND t.relname      = l_table
		   AND a.attname      != l_column
		 GROUP BY n.nspname, t.relname, i.relname, ix.indisprimary
		 ORDER BY n.nspname, t.relname, i.relname, ix.indisprimary DESC;		
	c4gangep CURSOR(l_table VARCHAR) FOR					/* Is table indirect denominator */
		SELECT table_name
	       	  FROM rif40_tables
	       	 WHERE isindirectdenominator = 1 
		   AND table_name            = UPPER(l_table);
	c5gangep CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR			/* List of partitions */
		SELECT nmsp_parent.nspname AS parent_schema,
	   	       parent.relname      AS master_table,
		       nmsp_child.nspname  AS partition_schema,
		       child.relname       AS partition
		  FROM pg_inherits, pg_class parent, pg_class child, pg_namespace nmsp_parent, pg_namespace nmsp_child 
		 WHERE pg_inherits.inhparent = parent.oid
		   AND pg_inherits.inhrelid  = child.oid
		   AND nmsp_parent.oid       = parent.relnamespace 
		   AND nmsp_child.oid        = child.relnamespace
	       	   AND parent.relname        = l_table 
		   AND nmsp_parent.nspname   = l_schema;
--	
	c3a_rec 	RECORD;
	c4_rec 		RECORD;
	c5_rec 		RECORD;
--
	ddl_stmt	VARCHAR[];
	i 		INTEGER;
	warnings	INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_common_partition_create_insert', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- If table is a numerator, cluster
--
	OPEN c4gangep(l_table);
	FETCH c4gangep INTO c4_rec;
	CLOSE c4gangep;
-- 
	IF c4_rec.table_name IS NOT NULL AND index_name IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_complete', 'Rebuild master cluster: %.%', 
			l_schema::VARCHAR, l_table::VARCHAR);
		ddl_stmt[1]:='CLUSTER VERBOSE '||quote_ident(l_schema)||'.'||quote_ident(l_table)||
			' USING '||index_name;
	END IF;
--
-- Cluster partitions
-- 
	i:=2;
	IF c4_rec.table_name IS NOT NULL AND index_name IS NOT NULL THEN
		FOR c5_rec IN c5gangep(l_schema, l_table) LOOP
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_complete', 'Cluster: %.%', 
				c5_rec.partition_schema::VARCHAR, c5_rec.partition::VARCHAR);
--
-- GET PK/unique index column
--
			OPEN c3gangep(c5_rec.partition_schema, c5_rec.partition, l_column);	
			FETCH c3gangep INTO c3a_rec;
			CLOSE c3gangep;
			IF c3a_rec.index_name IS NOT NULL THEN
				ddl_stmt[i]:='CLUSTER VERBOSE '||c5_rec.partition_schema||'.'||c5_rec.partition||
					' USING '||c3a_rec.index_name;
				i:=i+1;
			ELSE
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_complete', 
					'Unable to cluster: %.%:%; no unique index or primary key found', 
					c5_rec.partition_schema::VARCHAR	/* Schema */, 
					l_table::VARCHAR 			/* Master table */,
					c5_rec.partition::VARCHAR		/* Partition */);
				warnings:=warnings+1;
			END IF;
		END LOOP;
	END IF;
--
	IF warnings > 0 THEN
                PERFORM rif40_log_pkg.rif40_error(-19005, '_rif40_common_partition_create_complete',
			'Unable to cluster: %.%; no unique indexes or primary keys found for % partitions', 
			l_schema::VARCHAR	/* Schema */, 
			l_table::VARCHAR 	/* Master table */,
			warnings::VARCHAR	/* Warnings */);
		END IF;		
--
-- Re-anaylse
--
	ddl_stmt[i]:='ANALYZE VERBOSE '||quote_ident(l_schema)||'.'||quote_ident(l_table);
--
-- Drop
--
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='DROP TABLE rif40_auto_partition /* Temporary table */';

--
-- Run 2
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);
--
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_create_complete(VARCHAR, VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	_rif40_common_partition_create_complete()
Parameters:	Schema, table, column, UK/PK indexname
Returns:	Nothing
Description:	Automatic range partition schema.table on column
		Completion tasks

* If table is a numerator, cluster each partition (not the master table)
* Re-anaylse';

--
-- Eof
