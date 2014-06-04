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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_drop_master_trigger(l_schema VARCHAR, l_table VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_drop_master_trigger()
Parameters:	Schema, table
Returns:	Drop
Description:	Automatic range/hash partitioning schema.table on column: remove ON-INSERT triggers
  		on naster table (so only inherited table triggers fire). This avoids:

psql:../psql_scripts/v4_0_study_id_partitions.sql:140: INFO:  1: rif40_method4('Partition EXPLAIN test') caught:
cursor "c1" already in use in SQL (see previous trapped error)
Detail:
psql:../psql_scripts/v4_0_study_id_partitions.sql:140: INFO:  2: _rif40_hash_partition_create_insert() caught:
cursor "c1" already in use in SQL> SELECT rif40_sql_pkg._rif40_hash(study_id::VARCHAR, 16) AS partition_value, COUNT(study_id) AS total_rows
  FROM rif40.rif40_study_shares
 GROUP BY rif40_sql_pkg._rif40_hash(study_id::VARCHAR, 16)
 ORDER BY 1
Detail:
psql:../psql_scripts/v4_0_study_id_partitions.sql:140: ERROR:  cursor "c1" already in use
 */
DECLARE
	ddl_stmt	VARCHAR[];
	c1rpct2 CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get triggers */
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
	c2rpct2 CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get triggers */
		SELECT COUNT(c.relname) AS total_child_tables,
		       COUNT(tg.tgname) AS total_child_triggers
		  FROM pg_inherits i 
			LEFT OUTER JOIN pg_class AS c ON (i.inhrelid=c.oid)
			LEFT OUTER JOIN pg_trigger AS tg ON (tg.tgrelid=c.oid)
   			LEFT OUTER JOIN pg_class as p ON (i.inhparent=p.oid)
		        LEFT OUTER JOIN pg_namespace ns1 ON (c.relnamespace = ns1.oid)
		        LEFT OUTER JOIN pg_namespace ns2 ON (p.relnamespace = ns2.oid)
		 WHERE ns2.nspname = l_schema
		   AND p.relname   = l_table;
	c1_rec RECORD;
	c2_rec RECORD;
--
	i INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_drop_master_trigger', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- Check there are child triggers
--
	OPEN c2rpct2(l_schema, l_table);
	FETCH c2rpct2 INTO c2_rec;
	CLOSE c2rpct2;
	IF c2_rec.total_child_tables = 0 THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_drop_master_trigger', 
			'Cannot drop trigger on master table %.%; no inherited child tabls', 
			l_schema::VARCHAR, 
			l_table::VARCHAR);
		RETURN;
	ELSIF c2_rec.total_child_tables != c2_rec.total_child_triggers THEN 
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_drop_master_trigger', 
			'Cannot drop trigger on master table %.%; missing triggers (found: %) on inherited child tables: %', 
			l_schema::VARCHAR, 
			l_table::VARCHAR, 
			c2_rec.total_child_triggers::VARCHAR,
			c2_rec.total_child_tables::VARCHAR);
		RETURN;
	END IF;	
--
	FOR c1_rec IN c1rpct2(l_schema, l_table) LOOP
		i:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_drop_master_trigger', 'Drop trigger [%] %.%(%)', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			c1_rec.tgname::VARCHAR, 
			c1_rec.function_name::VARCHAR);
		ddl_stmt[i]:='DROP TRIGGER '||c1_rec.tgname||' ON '||l_schema||'.'||l_table;
	END LOOP;
--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);

END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_drop_master_trigger(VARCHAR, VARCHAR) IS 'Function: 	_rif40_drop_master_trigger()
Parameters:	Schema, table
Returns:	Drop
Description:	Automatic range/hash partitioning schema.table on column: remove ON-INSERT triggers
  		on naster table (so only inherited table triggers fire). This avoids:

psql:../psql_scripts/v4_0_study_id_partitions.sql:140: INFO:  1: rif40_method4(''Partition EXPLAIN test'') caught:
cursor "c1" already in use in SQL (see previous trapped error)
Detail:
psql:../psql_scripts/v4_0_study_id_partitions.sql:140: INFO:  2: _rif40_hash_partition_create_insert() caught:
cursor "c1" already in use in SQL> SELECT rif40_sql_pkg._rif40_hash(study_id::VARCHAR, 16) AS partition_value, COUNT(study_id) AS total_rows
  FROM rif40.rif40_study_shares
 GROUP BY rif40_sql_pkg._rif40_hash(study_id::VARCHAR, 16)
 ORDER BY 1
Detail:
psql:../psql_scripts/v4_0_study_id_partitions.sql:140: ERROR:  cursor "c1" already in use';

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_triggers(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, enable_or_disable VARCHAR, OUT ddl_stmt VARCHAR[])
RETURNS VARCHAR[]
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_common_partition_triggers()
Parameters:	Schema, table, column, enable or disable
Returns:	DDL statement array
Description:	Automatic range/hash partitioning schema.table on column: ENABLE or DISABLE ON-INSERT triggers
  		on master table and all inherited tables
 */
DECLARE
	c1rpct CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get triggers */
		SELECT tg.tgname,
		       tg.tgenabled,
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
	c2rpct CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get partitions */
		SELECT i.inhseqno, 
		       ns1.nspname AS child_schema,
		       c.relname AS child, 
		       ns2.nspname AS parent_schema,
		       p.relname AS parent
		  FROM pg_inherits i 
			LEFT OUTER JOIN pg_class AS c ON (i.inhrelid=c.oid)
   			LEFT OUTER JOIN pg_class as p ON (i.inhparent=p.oid)
		        LEFT OUTER JOIN pg_namespace ns1 ON (c.relnamespace = ns1.oid)
		        LEFT OUTER JOIN pg_namespace ns2 ON (p.relnamespace = ns2.oid)
		 WHERE ns2.nspname = l_schema
		   AND p.relname   = l_table
		 ORDER BY 1, 3;
	c1_rec RECORD;
	c2_rec RECORD;
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
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_triggers', 'Trigger [%] %.%(%): (state %)%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			c1_rec.tgname::VARCHAR, 
			c1_rec.function_name::VARCHAR, 
			c1_rec.tgenabled::VARCHAR, 
			enable_or_disable::VARCHAR);
--
-- Do not re-enable master triggers
--
		IF enable_or_disable = 'DISABLE' THEN
			i:=i+1;
			ddl_stmt[i]:='ALTER TABLE '||l_schema||'.'||l_table||' '||enable_or_disable||' TRIGGER '||c1_rec.tgname;
		END IF;
	END LOOP;
--
-- Now do inherited tables
--
	FOR c2_rec IN c2rpct(l_schema, l_table) LOOP
		FOR c1_rec IN c1rpct(l_schema, c2_rec.child) LOOP
			i:=i+1;
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_triggers', 'Child trigger [%] %.%(%): (%)%', 
				i::VARCHAR,
				l_schema::VARCHAR, 
				c1_rec.tgname::VARCHAR, 
				c1_rec.function_name::VARCHAR, 
				c1_rec.tgenabled::VARCHAR, 
				enable_or_disable::VARCHAR);
			ddl_stmt[i]:='ALTER TABLE '||l_schema||'.'||c2_rec.child||' '||enable_or_disable||' TRIGGER '||c1_rec.tgname;
		END LOOP;
	END LOOP;
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_triggers(VARCHAR, VARCHAR, VARCHAR, VARCHAR, OUT VARCHAR[]) IS 'Function: 	_rif40_common_partition_triggers()
Parameters:	Schema, table, column, enable or disable
Returns:	DDL statement array
Description:	Automatic range/hash partitioning schema.table on column: ENABLE or DISABLE ON-INSERT triggers
  		on master table and all inherited tables';

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
					' ADD CONSTRAINT '||t.relname||'_pk '||pg_get_constraintdef(con.oid) 
		       ELSE NULL END AS constraint_def,
		       ix.indisprimary,
		       ix.indisunique
		 FROM pg_index ix, pg_attribute a, pg_namespace n, pg_class t, pg_class i
		        LEFT OUTER JOIN pg_constraint con ON (con.conindid = i.oid AND con.contype = 'p')
		 WHERE t.oid          = ix.indrelid
		   AND i.oid          = ix.indexrelid
		   AND a.attrelid     = t.oid
		   AND a.attnum       = ANY(ix.indkey)
		   AND t.relkind      = 'r'
		   AND t.relnamespace = n.oid 
		   AND n.nspname      = l_schema
		   AND t.relname      = l_table
		   AND a.attname      != l_column
		 GROUP BY n.nspname, t.relname, i.relname, ix.indisprimary, ix.indisunique, i.oid, con.oid
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
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Adding indexes, primary key to: %.%', 
		l_schema::VARCHAR, 
		partition_table::VARCHAR);
	FOR c4_rec IN c4rpcr(l_schema, master_table, l_column) LOOP
		I:=i+1;
		IF c4_rec.indisunique AND c4_rec.indisprimary AND c4_rec.constraint_def IS NOT NULL THEN
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c4_rec.constraint_def::VARCHAR, master_table, partition_table);
		ELSE
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
								REPLACE(c4_rec.index_def::VARCHAR, master_table, partition_table),
									c4_rec.index_name, c4_rec.index_name||'_p'||l_value 
									/* Handle indexes without master table in name */);
		END IF;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Index[%] % on: %.%(%); PK: %, Unique: %'||E'\n'||'SQL> %;', 
			i::VARCHAR,
			c4_rec.index_name::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR, 
			c4_rec.column_names::VARCHAR, 
			c4_rec.indisprimary::VARCHAR, 
			c4_rec.indisunique::VARCHAR,
			ddl_stmt[array_length(ddl_stmt, 1)]::VARCHAR);
--		
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
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Adding foreign keys to: %.%', 
		l_schema::VARCHAR, 
		partition_table::VARCHAR);
	FOR c5_rec IN c5rpcr(l_schema, master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'FK Constraint[%] % on: %.%(%)', 
			i::VARCHAR,
			c5_rec.conname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR, 
			c5_rec.child_columns::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
							REPLACE(c5_rec.constraint_def, master_table, partition_table),
								c5_rec.conname, c5_rec.conname||'_p'||l_value
								/* Handle constraints without master table in name */);
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
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Adding trigger, unique, check and exclusion constraints to: %.%', 
		l_schema::VARCHAR, 
		partition_table::VARCHAR);
	i:=0;
	FOR c6_rec IN c6rpcr(l_schema, master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', '% constraint[%] % on: %.%', 
			c6_rec.constraint_type::VARCHAR,
			i::VARCHAR,
			c6_rec.conname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
							REPLACE(c6_rec.constraint_def, master_table, partition_table),
								c6_rec.conname, c6_rec.conname||'_p'||l_value
								/* Handle constraints without master table in name */);
		IF ddl_stmt[array_length(ddl_stmt, 1)] IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20555, '_rif40_common_partition_create',  '% constraint[%] % on: %.% NULL SQL statement', 
			c6_rec.constraint_type::VARCHAR,
			i::VARCHAR,
			c6_rec.conname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
		END IF;	
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
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Adding validation triggers to: %.%', 
		l_schema::VARCHAR, 
		partition_table::VARCHAR);
	i:=0;
	FOR c7_rec IN c7rpcr(l_schema, master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Validation trigger[%] % on: %.% calls %', 
			i::VARCHAR,
			c7_rec.tgname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR,
			c7_rec.function_name::VARCHAR);
--
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:140: WARNING:  rif40_ddl(): SQL in error (42883)> 
-- CREATE TRIGGER rif40_study_shares_p1_checks BEFORE INSERT OR UPDATE OF grantor, grantee_username, study_id ON rif40_study_shares_p1 
-- FOR EACH ROW WHEN (((((new.grantor IS NOT NULL) AND ((new.grantor)::text <> ''::text)) OR 
--			((new.grantee_username IS NOT NULL) AND ((new.grantee_username)::text <> ''::text))) OR
--	       		((new.study_id IS NOT NULL) AND ((new.study_id)::text <> ''::text)))) 
--	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_rif40_study_shares_p1_checks();
--
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
								REPLACE(c7_rec.trigger_def, master_table, partition_table),
									REPLACE(c7_rec.function_name, master_table, partition_table),												 c7_rec.function_name /* Put the function name back! */);
		
		IF ddl_stmt[array_length(ddl_stmt, 1)] IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20555, '_rif40_common_partition_create',  'Validation trigger[%] % on: %.% calls % NULL SQL statement', 
			i::VARCHAR,
			c7_rec.tgname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR,
			c7_rec.function_name::VARCHAR);
		END IF;
		IF c7_rec.comment_def IS NOT NULL THEN
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c7_rec.comment_def, master_table, partition_table);
		END IF;
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
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Adding grants to: %.%', 
		l_schema::VARCHAR, 
		partition_table::VARCHAR);
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
       OUT ddl_stmt VARCHAR[], OUT fk_stmt VARCHAR[], OUT num_partitions INTEGER, OUT total_rows INTEGER, OUT warnings INTEGER)
RETURNS RECORD
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_common_partition_create_setup()
Parameters:	Schema, table, column, 
                [OUT] ddl statement array, [OUT] foreign key statement (re-)creation array,
	       	[OUT] num_partitions, [OUT] total_rows, [OUT] warnings
Returns:	OUT parameters as a record
 		DDL statement array is NULL if the function is unable to partition
Description:	Automatic range/hash partition schema.table on column
		Prequiste checks, COPY <table> to rif40_auto_partition, truncate <table>

* Must be rif40 or have rif_user or rif_manager role
* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, disable foreign keys on tables referencing this table, create foreign key statement (re-)creation array [OUT parameter], 
  truncate (dont panic - Postgres DDL is part of a transaction)

  Add to DDL statement list:

	CREATE TEMPORARY TABLE rif40_auto_partition AS SELECT * FROM rif40.rif40_study_shares;
	ALTER TABLE rif40.rif40_study_shares DROP CONSTRAINT rif40_study_shares_study_id_fk;
	TRUNCATE TABLE rif40.rif40_study_shares;

  Add to foreign key DDL statement list for tables referencing this table via foreign keys, but not for:
	a) The master tables referencing this table (as they have no data):
	b) This table is a master (as they also have no data):

	ALTER TABLE rif40.rif40_study_shares ADD CONSTRAINT rif40_study_shares_study_id_fk FOREIGN KEY (study_id) REFERENCES t_rif40_studies(study_id);

* Do not partition if table has only one distinct row
* Do not partition if table has no rows

Foreign keys need to be disabled to avoid:

psql:../psql_scripts/v4_0_study_id_partitions.sql:141: WARNING:  rif40_ddl(): SQL in error (0A000)> TRUNCATE TABLE rif40.t_rif40_investigations;
psql:../psql_scripts/v4_0_study_id_partitions.sql:141: ERROR:  cannot truncate a table referenced in a foreign key constraint

 */
DECLARE
	c1gangep 		REFCURSOR;
	c2gangep CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get trigger, unique, check and exclusion constraints */
		WITH a AS (
			SELECT con.conname, 
			       con.oid,
			       ns1.nspname AS con_schema_name,
			       c1.relname AS table_name,
			       c2.relname AS ref_fk_table_name,
			       c1.relnamespace AS schema_oid,
			       c2.relnamespace AS ref_fk_schema_oid,
			       c1.relhassubclass AS is_partitioned,
			       c2.relhassubclass AS is_ref_fk_partitioned,
		               CASE WHEN ih2.inhrelid IS NOT NULL THEN TRUE ELSE FALSE END is_a_ref_fk_partition
			  FROM pg_constraint con
			        LEFT OUTER JOIN pg_namespace ns1 ON (con.connamespace = ns1.oid)
			        LEFT OUTER JOIN pg_class c1 ON (con.confrelid = c1.oid) /* Foreign keys referencing this table */
			        LEFT OUTER JOIN pg_class c2 ON (con.conrelid  = c2.oid) /* Tables referencing this table by foreign keys */
		        	LEFT OUTER JOIN pg_inherits ih2 ON (c2.oid    = ih2.inhrelid)
							/* Is the table referencing this table inheriting (i.e. is a partition) */
			 WHERE ns1.nspname   = l_schema
			   AND c1.relname    = l_table 	/* This table */
			   AND con.contype   = 'f'     	/* Foreign key constraints */
		)
		SELECT conname, oid, con_schema_name, ref_fk_table_name, table_name,
	               ns2.nspname AS schema_name, ns3.nspname AS ref_fk_schema_name,
		       is_partitioned, is_ref_fk_partitioned, is_a_ref_fk_partition,
		       CASE 
				WHEN oid IS NOT NULL THEN 'ALTER TABLE '||ns3.nspname||'.'||ref_fk_table_name||
					' MODIFY CONSTRAINT '||conname||' DEFERRABLE INITIALLY IMMEDIATE /* '||
					pg_get_constraintdef(oid)||' */'
				ELSE NULL 
		       END AS defer_constraint_def,
		       CASE 
				WHEN oid IS NOT NULL THEN 'ALTER TABLE '||ns3.nspname||'.'||ref_fk_table_name||
					' DROP CONSTRAINT '||conname||' /* '||
					pg_get_constraintdef(oid)||' */'
				ELSE NULL 
		       END AS drop_constraint_def,
		       CASE 
				WHEN oid IS NOT NULL THEN 'ALTER TABLE '||ns3.nspname||'.'||ref_fk_table_name||
					' ADD CONSTRAINT '||conname||' '||
					pg_get_constraintdef(oid)||
					'/* has partitions: '||is_ref_fk_partitioned::VARCHAR||', is a partition: '||is_a_ref_fk_partition::VARCHAR||' */'
				ELSE NULL 
		       END AS add_constraint_def,
		       pg_get_constraintdef(oid) AS constraintdef
		  FROM a
		        LEFT OUTER JOIN pg_namespace ns2 ON (a.schema_oid = ns2.oid)
		        LEFT OUTER JOIN pg_namespace ns3 ON (a.ref_fk_schema_oid = ns3.oid)
		 ORDER BY con_schema_name, conname;
--
	c2_rec			RECORD;
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

	ELSIF num_partitions > 1 THEN
--
-- Do not partition if table has only one distinct row
--
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_setup', 'Unable to automatic range/hash partition by %: %.%; Not partitionable, only 1 distinct row', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR);
		RETURN;
	ELSE
--
-- Warn if table has no rows or only 1 partition
--
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_setup', 'Automatic range/hash partition by %: %.%; no rows (%)/only 1 partition (%)', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR,
			l_rows::VARCHAR, num_partitions::VARCHAR);
	END IF;

--
-- Copy to temp table, defer foreign key constraint triggers on table, truncate (dont panic - Postgres DDL is part of a transaction)
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 'Copy data to temporary table fron: %.%', 
		l_schema::VARCHAR, l_table::VARCHAR);
--
-- Foreign key constraints cause:
--
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:141: WARNING:  rif40_ddl(): SQL in error (0A000)> TRUNCATE TABLE rif40.t_rif40_investigations;
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:141: ERROR:  cannot truncate a table referenced in a foreign key constraint
-- 
-- It would be better to defer foreign key constraints, but
-- defer needs Postgres 9.4; so drop and re-create
--
	ddl_stmt[1]:='CREATE TEMPORARY TABLE rif40_auto_partition AS SELECT * FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table);
	i:=0;
	FOR c2_rec IN c2gangep(l_schema, l_table) LOOP
		IF c2_rec.defer_constraint_def IS NOT NULL THEN
			i:=i+1;
--			ddl_stmt[array_length(ddl_stmt, 1)+1]:=c2_rec.defer_constraint_def;
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=c2_rec.drop_constraint_def;
--
-- Only add back referenced foreign key constraints if they are on a a) partitioned table or b) table is not partitioned, or
-- you will get:
--
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:141: WARNING:  rif40_ddl(): SQL in error (23503)> ALTER TABLE rif40.t_rif40_inv_conditions_p8 ADD CONSTRAINT t_rif40_inv_conditions_p8_si_fk FOREIGN KEY (study_id, inv_id) REFERENCES t_rif40_investigations(study_id, inv_id);
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:141: ERROR:  insert or update on table "t_rif40_inv_conditions_p8" violates foreign key constraint "t_rif40_inv_conditions_p8_si_fk"
-- 
-- This is because the master table you are creating has no rows...
--
-- or:
--
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:141: WARNING:  rif40_ddl(): SQL in error (23503)> ALTER TABLE rif40.t_rif40_inv_conditions_p8 ADD CONSTRAINT t_rif40_inv_conditions_p8_si_fk FOREIGN KEY (study_id, inv_id) REFERENCES t_rif40_investigations(study_id, inv_id)/* has partitions: false, is a partition: true */;
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:141: ERROR:  insert or update on table "t_rif40_inv_conditions_p8" violates foreign key constraint "t_rif40_inv_conditions_p8_si_fk"
-- 
--
			IF c2_rec.is_ref_fk_partitioned = TRUE /* has partitions */ AND 
			   c2_rec.is_a_ref_fk_partition = FALSE /* is NOT a partition */ THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_setup', 
					'Drop, suppress re-create referenced foreign key constraint[%] (partitions has: %, is a: %): % on: %.% from: %.% (%)', 
					i::VARCHAR,
					c2_rec.is_ref_fk_partitioned::VARCHAR	/* has partitions */,
					c2_rec.is_a_ref_fk_partition::VARCHAR	/* is a partition */,
					c2_rec.conname::VARCHAR			/* Foreign key constraint */,
					l_schema::VARCHAR, 
					l_table::VARCHAR,
					c2_rec.ref_fk_schema_name::VARCHAR	/* Schema of table referencing foreign key */,
					c2_rec.ref_fk_table_name::VARCHAR	/* Table referencing foreign key */,
					c2_rec.constraintdef::VARCHAR)		/* Foregin key */;
			ELSE
				IF fk_stmt IS NULL THEN
					fk_stmt[1]:=c2_rec.add_constraint_def;
				ELSE
					fk_stmt[array_length(fk_stmt, 1)+1]:=c2_rec.add_constraint_def;
				END IF;
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 
					'Drop, re-create [later after data re import] referenced foreign key constraint[%] (partitions has: %, is a: %): % on: %.% from: %.% (%)', 
					i::VARCHAR,
					c2_rec.is_ref_fk_partitioned::VARCHAR	/* has partitions */,
					c2_rec.is_a_ref_fk_partition::VARCHAR	/* is a partition */,
					c2_rec.conname::VARCHAR			/* Foreign key constraint */,
					l_schema::VARCHAR, 
					l_table::VARCHAR,
					c2_rec.ref_fk_schema_name::VARCHAR	/* Schema of table referencing foreign key */,
					c2_rec.ref_fk_table_name::VARCHAR	/* Table referencing foreign key */,
					c2_rec.constraintdef::VARCHAR)		/* Foregin key */;
			END IF;	
		END IF;
	END LOOP;
	IF l_table = 't_rif40_investigations' THEN
--		RAISE plpgsql_error;
	END IF;
--	ddl_stmt[array_length(ddl_stmt, 1)+1]:='SET CONSTRAINTS ALL DEFERRED';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='TRUNCATE TABLE '||quote_ident(l_schema)||'.'||quote_ident(l_table);
END;
$func$ 
LANGUAGE plpgsql;

--\df+ rif40_sql_pkg._rif40_common_partition_create_setup

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_create_setup(VARCHAR, VARCHAR, VARCHAR, OUT VARCHAR[], OUT VARCHAR[], OUT INTEGER, OUT INTEGER, OUT INTEGER) IS 'Function: 	_rif40_common_partition_create_setup()
Parameters:	Schema, table, column, 
                [OUT] ddl statement array, [OUT] foreign key statement (re-)creation array,
	       	[OUT] num_partitions, [OUT] total_rows, [OUT] warnings
Returns:	OUT parameters as a record
 		DDL statement array is NULL if the function is unable to partition
Description:	Automatic range/hash partition schema.table on column
		Prequiste checks, COPY <table> to rif40_auto_partition, truncate <table>

* Must be rif40 or have rif_user or rif_manager role
* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, disable foreign keys on table, create foreign key statement (re-)creation array [OUT parameter], 
  truncate (dont panic - Postgres DDL is part of a transaction)

  Add to DDL statement list:

	CREATE TEMPORARY TABLE rif40_auto_partition AS SELECT * FROM rif40.rif40_study_shares;
	ALTER TABLE rif40.rif40_study_shares DROP CONSTRAINT rif40_study_shares_study_id_fk;
	TRUNCATE TABLE rif40.rif40_study_shares;

  Add to foreign key DDL statement list:

	ALTER TABLE rif40.rif40_study_shares ADD CONSTRAINT rif40_study_shares_study_id_fk FOREIGN KEY (study_id) REFERENCES t_rif40_studies(study_id);

* Do not partition if table has only one distinct row
* Do not partition if table has no rows';

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
	i 		INTEGER:=1;
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
		ddl_stmt[i]:='CLUSTER VERBOSE '||quote_ident(l_schema)||'.'||quote_ident(l_table)||
			' USING '||index_name;
		i:=i+1;
	END IF;
--
-- Cluster partitions
-- 
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
	i:=i+1;
	FOR c5_rec IN c5gangep(l_schema, l_table) LOOP
		ddl_stmt[i]:='ANALYZE VERBOSE '||quote_ident(c5_rec.partition_schema)||'.'||quote_ident(c5_rec.partition);
		i:=i+1;
	END LOOP;
--
-- Drop
--
	ddl_stmt[i]:='DROP TABLE rif40_auto_partition /* Temporary table */';

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
