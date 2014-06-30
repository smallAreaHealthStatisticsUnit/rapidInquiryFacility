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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_partition_count(l_schema VARCHAR, l_table VARCHAR)
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_partition_count()
Parameters:	Schema, table
Returns:	Number of partitions (sub partitions). NULL if not partitioned
Description:    Count number of partitions

		SELECT nmsp_parent.nspname AS parent_schema,
	   	       parent.relname      AS master_table,
		       nmsp_child.nspname  AS partition_schema,
		       child.relname       AS partition
		  FROM pg_inherits, pg_class parent, pg_class child, pg_namespace nmsp_parent, pg_namespace nmsp_child 
		 WHERE pg_inherits.inhparent = parent.oid
		   AND pg_inherits.inhrelid  = child.oid
		   AND nmsp_parent.oid       = parent.relnamespace 
		   AND nmsp_child.oid        = child.relnamespace
	       	   AND parent.relname        = 'sahsuland_cancer'
		   AND nmsp_parent.nspname   = 'rif40';
--	

 */
DECLARE
	c1tpct CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get triggers */
		SELECT COUNT(child.relname) AS total_partitions
		  FROM pg_inherits, pg_class parent, pg_class child, pg_namespace nmsp_parent, pg_namespace nmsp_child 
		 WHERE pg_inherits.inhparent = parent.oid
		   AND pg_inherits.inhrelid  = child.oid
		   AND nmsp_parent.oid       = parent.relnamespace 
		   AND nmsp_child.oid        = child.relnamespace
	       	   AND parent.relname        = l_table 
		   AND nmsp_parent.nspname   = l_schema;
	c1_rec RECORD;
BEGIN
	OPEN c1tpct(l_schema, l_table);
	FETCH c1tpct INTO c1_rec;
	CLOSE c1tpct;
--
	RETURN c1_rec.total_partitions;	
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_partition_count(VARCHAR, VARCHAR) IS 'Function: 	_rif40_partition_count()
Parameters:	Schema, table
Returns:	Number of partitions (sub partitions). NULL if not partitioned
Description:    Count number of partitions';

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_drop_master_trigger(l_schema VARCHAR, l_table VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_drop_master_trigger()
Parameters:	Schema, table
Returns:	Nothing
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
		 /*  AND a.attname      != l_column -- removed - disabling PKs on partition column */
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
	pk_found	BOOLEAN:=FALSE;
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
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c4_rec.constraint_def::VARCHAR, master_table, partition_table)||E'\n'||'/* Primary key */';
		ELSE
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
								REPLACE(c4_rec.index_def::VARCHAR, master_table, partition_table),
									c4_rec.index_name, c4_rec.index_name||'_p'||l_value 
									/* Handle indexes without master table in name */)||E'\n'||'/* Index */';
		END IF;
		IF c4_rec.indisprimary THEN
			pk_found:=TRUE;
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
	ELSIF NOT pk_found THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create', 'Added % indexes to partition: %.%, no primary key', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
		warnings:=warnings+1;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create', 'Added no indexes to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
		warnings:=warnings+1;
	END IF;

	IF partition_table = 't_rif40_studies_p9' THEN
--		RAISE plpgsql_error;
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
	hash_partition_count INTEGER,
       	OUT ddl_stmt VARCHAR[], OUT fk_stmt VARCHAR[], OUT num_partitions INTEGER, OUT min_value VARCHAR, OUT total_rows INTEGER, OUT warnings INTEGER)
RETURNS RECORD
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_common_partition_create_setup()
Parameters:	Schema, table, column, if hash partition: number of partitions that will be created
                [OUT] ddl statement array, [OUT] foreign key statement (re-)creation array,
	       	[OUT] num_partitions, [OUT] min_value, [OUT] total_rows, [OUT] warnings
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

	ALTER TABLE rif40.rif40_study_shares ADD CONSTRAINT rif40_study_shares_study_id_fk FOREIGN KEY (study_id) 
		REFERENCES t_rif40_studies_p1(study_id);

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
			       con.oid AS constraint_oid,
			       c1.oid AS this_table_oid,
			       c2.oid AS ref_fk_part_oid,
			       ns1.nspname AS con_schema_name,
			       c1.relname AS this_table_name,
			       c2.relname AS ref_fk_table_name,
			       c1.relnamespace AS schema_oid,
			       c2.relnamespace AS ref_fk_schema_oid,
			       c1.relhassubclass AS is_partitioned,
			       c2.relhassubclass AS is_ref_fk_partitioned,
		               CASE WHEN ih2.inhrelid IS NOT NULL THEN TRUE ELSE FALSE END is_a_ref_fk_partition,
			       ih2.inhseqno AS inhseqno_ref_fk,
			       ih2.inhparent AS ref_fk_master_oid
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
		SELECT conname, constraint_oid, this_table_oid, ref_fk_part_oid, ref_fk_master_oid,
	               con_schema_name, ref_fk_table_name, this_table_name,
	               ns2.nspname AS schema_name, ns3.nspname AS ref_fk_schema_name,
		       is_partitioned, is_ref_fk_partitioned, is_a_ref_fk_partition, 
		       inhseqno_ref_fk,
		       CASE 
				WHEN constraint_oid IS NOT NULL THEN 'ALTER TABLE '||ns3.nspname||'.'||ref_fk_table_name||E'\n'||
					'      MODIFY CONSTRAINT '||conname||' DEFERRABLE INITIALLY IMMEDIATE'||E'\n'||
					'/* '||pg_get_constraintdef(constraint_oid)||' */'
				ELSE NULL 
		       END AS defer_constraint_def /* Not yet supported in Postgres */,
		       CASE 
				WHEN constraint_oid IS NOT NULL THEN 'ALTER TABLE '||ns3.nspname||'.'||ref_fk_table_name||E'\n'||
					'      DROP CONSTRAINT '||conname||E'\n'||
					'/* '||pg_get_constraintdef(constraint_oid)||' */'
				ELSE NULL 
		       END AS drop_constraint_def,
/*

Fix for:

psql:../psql_scripts/v4_0_study_id_partitions.sql:145: WARNING:  rif40_ddl(): SQL in error (23503)> 
ALTER TABLE rif40.t_rif40_inv_conditions_p8 ADD CONSTRAINT /- Add support for local partitions -/ t_rif40_inv_conditions_p8_si_fk FOREIGN KEY (study_id, inv_id) REFERENCES t_rif40_investigations(study_id, inv_id) /- has partitions: false, is a partition: true -/;
psql:../psql_scripts/v4_0_study_id_partitions.sql:145: ERROR:  insert or update on table "t_rif40_inv_conditions_p8" violates foreign key constraint "t_rif40_inv_conditions_p8_si_fk"

psql:../psql_scripts/v4_0_study_id_partitions.sql:145: INFO:  [DEBUG1] _rif40_common_partition_create_setup(): Drop, re-create [later after data re import] referenced foreign key c
onstraint[49] (partitions has: false, is a: true): 

t_rif40_inv_covariates_p8_si_fk on: rif40.t_rif40_investigations from: rif40.t_rif40_inv_covariates_p8 (FOREIGN KEY (study_id, inv_id) REFERENCES t_rif40_investigations(study_id, inv_id))

 */
		       CASE 
				WHEN constraint_oid IS NOT NULL THEN 'ALTER TABLE '||ns3.nspname||'.'||ref_fk_table_name||E'\n'||
					'       ADD CONSTRAINT /* Add support for local partitions */ '||conname||E'\n'||
					pg_get_constraintdef(constraint_oid)||E'\n'||
					'/* Referenced foreign key table: '||ns3.nspname||'.'||ref_fk_table_name||' has partitions: '||
					is_ref_fk_partitioned::VARCHAR||', is a partition: '||is_a_ref_fk_partition::VARCHAR||' */'
				ELSE NULL 
		       END AS add_constraint_def,
		       pg_get_constraintdef(constraint_oid) AS constraintdef
		  FROM a
		        LEFT OUTER JOIN pg_namespace ns2 ON (a.schema_oid = ns2.oid)
		        LEFT OUTER JOIN pg_namespace ns3 ON (a.ref_fk_schema_oid = ns3.oid)
		 ORDER BY con_schema_name, conname;
	c3gangep CURSOR(l_ref_fk_part_oid OID, l_ref_fk_master_oid OID) FOR
		WITH a AS ( /* Get partition list for referenced foreign key table */
			SELECT inhrelid, inhparent, inhseqno,
			       ROW_NUMBER() OVER(PARTITION BY b2.relname ORDER BY inhrelid) AS part_seq_no,
			       COUNT(inhparent) OVER(PARTITION BY b2.relname) AS total_part
			  FROM pg_inherits i
				LEFT OUTER JOIN pg_class b1 ON (b1.oid = i.inhrelid)
				LEFT OUTER JOIN pg_class b2 ON (b2.oid = i.inhparent)
			  WHERE i.inhparent = l_ref_fk_master_oid
		)
		SELECT inhrelid, inhparent, inhseqno, part_seq_no, total_part
		  FROM a 
		 WHERE a.inhrelid = l_ref_fk_part_oid /* Filter on partition object ID */;
		
--
	c2_rec			RECORD;
	c3_rec			RECORD;
	c3b_rec			RECORD;
--
	sql_stmt 		VARCHAR;
	l_rows			INTEGER:=0;
	table_length		INTEGER:=0;
	name_length_limit	INTEGER:=40;	/* You may want to set this higher */
	i			INTEGER:=0;
	j			INTEGER:=0;
	part_test_rec		RECORD;
	l_min_value		VARCHAR;
	total_partitions	INTEGER;
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
		sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(l_column)||')) AS num_partitions, COUNT('||quote_ident(l_column)||') AS total_rows, MIN('||quote_ident(l_column)||')::VARCHAR AS l_min_value FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||' LIMIT 1'; 
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 'SQL> %;', sql_stmt::VARCHAR);
		OPEN c1gangep FOR EXECUTE sql_stmt;
		FETCH c1gangep INTO num_partitions, total_rows, l_min_value;
		GET DIAGNOSTICS l_rows = ROW_COUNT;
		CLOSE c1gangep;
		IF l_min_value ~ '^[0-9]*.?[0-9]*$' THEN /* isnumeric */	
			min_value:=l_min_value;
		ELSE
			min_value:=''''||l_min_value||'''';
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
-- Check table name length - must be 25 chars or less (assuming the limit is 30)
--
	table_length:=length(quote_ident(l_table));
--
-- Hash partiitions depends on the hash, not the data so set
--
	IF hash_partition_count IS NOT NULL THEN
		num_partitions:=hash_partition_count;
	END IF;
--
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
-- Check if table is already partitioned
--
		total_partitions:=rif40_sql_pkg._rif40_partition_count(l_schema, l_table);
		IF total_partitions >= 1 THEN
			PERFORM rif40_log_pkg.rif40_error(-20991, '_rif40_common_partition_create_setup', 
				'Automatic range/hash partition by %: %.%; table name is already partitioned into: % partitions', 
				l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, total_partitions::VARCHAR);
		END IF;
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
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:145: WARNING:  rif40_ddl(): SQL in error (23503)> 
-- ALTER TABLE rif40.t_rif40_inv_conditions_p8 
-- ADD CONSTRAINT /* Add support for local partitions */ t_rif40_inv_conditions_p8_si_fk 
-- FOREIGN KEY (study_id, inv_id) REFERENCES t_rif40_investigations_p1(study_id, inv_id) /* has partitions: false, is a partition: true */;
--psql:../psql_scripts/v4_0_study_id_partitions.sql:145: ERROR:  insert or update on table "t_rif40_inv_conditions_p8" violates foreign key constraint "t_rif40_inv_conditions_p8_si_fk" 
--
			IF c2_rec.is_ref_fk_partitioned = TRUE /* has partitions */ AND 
			   c2_rec.is_a_ref_fk_partition = FALSE /* is NOT a partition */ THEN
--
-- Referenced foreign key is not partitioned, but this table is
--
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 
					'Ignoring referenced foreign key constraint[%.%] as has partitions (partitions has: %, is a: %): % on: %.% from: %.%', 
					i::VARCHAR,
					j::VARCHAR,
					c2_rec.is_ref_fk_partitioned::VARCHAR	/* has partitions */,
					c2_rec.is_a_ref_fk_partition::VARCHAR	/* is a partition */,
					c2_rec.conname::VARCHAR			/* Foreign key constraint */,
					l_schema::VARCHAR, 
					l_table::VARCHAR,
					c2_rec.ref_fk_schema_name::VARCHAR	/* Schema of table referencing foreign key */,
					c2_rec.ref_fk_table_name::VARCHAR	/* Table referencing foreign key */);
			ELSE
--
-- Referenced foreign key is partitioned, workout which partition it is in the sequence
--
				OPEN c3gangep(c2_rec.ref_fk_part_oid, c2_rec.ref_fk_master_oid);
				FETCH c3gangep INTO c3_rec;
--
-- check for >1 row
--
				IF c3_rec.total_part IS NULL THEN
					FETCH c3gangep INTO c3b_rec;
					IF c3_rec.total_part IS NOT NULL THEN
						PERFORM rif40_log_pkg.rif40_error(-20792, '_rif40_common_partition_create_setup', 
							'Automatic range/hash partition by %: %.%; table name has % partitions, referenced foreign key table: %.% c3gangep partition error; expected 1 row, got >1', 
							l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, total_partitions::VARCHAR,
							c2_rec.ref_fk_schema_name::VARCHAR	/* Schema of table referencing foreign key */,
							c2_rec.ref_fk_table_name::VARCHAR	/* Table referencing foreign key */);
					END IF;
				END IF;
				CLOSE c3gangep;
--
-- No master table for ref_fk (i.e. it is not partition and can be ignored)
--
				IF c2_rec.ref_fk_master_oid IS NULL THEN
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 
						'Automatic range/hash partition by %: %.%; table name has % partitions, referenced foreign key table: %.% has no partitions; ignored', 
						l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, total_partitions::VARCHAR,
						c2_rec.ref_fk_schema_name::VARCHAR	/* Schema of table referencing foreign key */,
						c2_rec.ref_fk_table_name::VARCHAR	/* Table referencing foreign key */);
				ELSIF c3_rec.total_part IS NULL THEN
--
-- OK I am expecting partitions, but have found none
--
					PERFORM rif40_sql_pkg.rif40_method4('WITH a AS (
SELECT inhrelid, inhparent, inhseqno,
       b1.relname AS partition_table_name,
       b2.relname AS master_table_name,
       ROW_NUMBER() OVER(PARTITION BY b2.relname ORDER BY inhrelid) AS part_seq_no,
       COUNT(inhparent) OVER(PARTITION BY b2.relname) AS total_part,
       CASE 
		WHEN a.inhparent = '||COALESCE(c2_rec.ref_fk_part_oid, 0)||'   THEN ''master ref fk partition''
     		WHEN a.inhrelid = '||COALESCE(c2_rec.ref_fk_part_oid, 0)||'    THEN ''partition ref fk partition''
     		WHEN a.inhparent = '||COALESCE(c2_rec.ref_fk_master_oid, 0)||' THEN ''master ref fk table''
     		WHEN a.inhrelid = '||COALESCE(c2_rec.ref_fk_master_oid, 0)||'  THEN ''partition ref fk table''
     		WHEN a.inhparent = '||COALESCE(c2_rec.this_table_oid, 0)||'    THEN ''this master table''
     		WHEN a.inhrelid = '||COALESCE(c2_rec.this_table_oid, 0)||'     THEN ''this partition table''
		ELSE ''Other'' END AS rel_type
  FROM pg_inherits a
	LEFT OUTER JOIN pg_class b1 ON (b1.oid = a.inhrelid)
	LEFT OUTER JOIN pg_class b2 ON (b2.oid = a.inhparent)
  WHERE a.inhparent = '||COALESCE(c2_rec.ref_fk_part_oid, 0)||' /* ref_fk_part_oid */
     OR a.inhrelid = '||COALESCE(c2_rec.ref_fk_part_oid, 0)||' /* ref_fk_part_oid */
     OR a.inhparent = '||COALESCE(c2_rec.ref_fk_master_oid, 0)||' /* ref_fk_master_oid */
     OR a.inhrelid = '||COALESCE(c2_rec.ref_fk_master_oid, 0)||' /* ref_fk_master_oid */
     OR a.inhparent = '||COALESCE(c2_rec.this_table_oid, 0)||' /* this_table_oid */
     OR a.inhrelid = '||COALESCE(c2_rec.this_table_oid, 0)||' /* this_table_oid */
)
SELECT * FROM a', 'inheritance table');
--
					PERFORM rif40_log_pkg.rif40_error(-20791, '_rif40_common_partition_create_setup', 
						'Automatic range/hash partition by %: %.%; table name has % partitions, referenced foreign key table: %.% has no partitions; expected it to be partitioned', 
						l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, total_partitions::VARCHAR,
						c2_rec.ref_fk_schema_name::VARCHAR	/* Schema of table referencing foreign key */,
						c2_rec.ref_fk_table_name::VARCHAR	/* Table referencing foreign key */);
--
-- Check the number of partitions in master table match number in referenced foreign key table
--
				ELSIF c3_rec.total_part != num_partitions THEN
--					PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create_setup', 
					PERFORM rif40_log_pkg.rif40_error(-20991, '_rif40_common_partition_create_setup', 
						'Automatic range/hash partition by %: %.%; table name has % partitions, referenced foreign key table: %.% has % partitions, expecting: %', 
						l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, total_partitions::VARCHAR,
						c2_rec.ref_fk_schema_name::VARCHAR	/* Schema of table referencing foreign key */,
						c2_rec.ref_fk_table_name::VARCHAR	/* Table referencing foreign key */,
						c3_rec.total_part::VARCHAR,
						num_partitions::VARCHAR);

				END IF;
--
				IF c2_rec.add_constraint_def IS NOT NULL AND c3_rec.part_seq_no IS NOT NULL THEN
					IF fk_stmt IS NULL THEN
						fk_stmt[1]:=REPLACE(c2_rec.add_constraint_def, 
							'REFERENCES '||l_table,
							'REFERENCES '||l_table||'_p'||c3_rec.part_seq_no::Text)||E'\n'||
							'/* Referenced foreign key partition: '||c3_rec.part_seq_no::Text||' of '||c3_rec.total_part::Text||' */';
					ELSE
						fk_stmt[array_length(fk_stmt, 1)+1]:=REPLACE(c2_rec.add_constraint_def, 
							'REFERENCES '||l_table,
							'REFERENCES '||l_table||'_p'||c3_rec.part_seq_no::Text)||E'\n'||
							'/* Referenced foreign key partition: '||c3_rec.part_seq_no::Text||' of '||c3_rec.total_part::Text||' */';
					END IF;
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create_setup', 
						'Drop, re-create [later after data re import] referenced foreign key constraint[%] (partitions has: %, is a: %): % on: %.% from: %.% [%,%,%]%;', 
						i::VARCHAR,
						c2_rec.is_ref_fk_partitioned::VARCHAR	/* has partitions */,
						c2_rec.is_a_ref_fk_partition::VARCHAR	/* is a partition */,
						c2_rec.conname::VARCHAR			/* Foreign key constraint */,
						l_schema::VARCHAR, 
						l_table::VARCHAR,
						c2_rec.ref_fk_schema_name::VARCHAR	/* Schema of table referencing foreign key */,
						c2_rec.ref_fk_table_name::VARCHAR	/* Table referencing foreign key */,
						c2_rec.ref_fk_part_oid::VARCHAR,	/* ref_fk_part_oid */
						c2_rec.ref_fk_master_oid::VARCHAR,	/* ref_fk_master_oid */
						c2_rec.this_table_oid::VARCHAR,		/* this_table_oid */
						E'\n'||'SQL> '||fk_stmt[array_length(fk_stmt, 1)]::VARCHAR)		
											/* Foreign key SQL */;
/*
psql:../psql_scripts/v4_0_study_id_partitions.sql:145: INFO:  [DEBUG1] _rif40_common_partition_create_setup(): Drop, re-create [later after data re import] referenced foreign key c
onstraint[50] (partitions has: false, is a: true): t_rif40_inv_covariates_p9_si_fk on: rif40.t_rif40_investigations from: rif40.t_rif40_inv_covariates_p9_p0 [5120807,214281,214255]


SQL> ALTER TABLE rif40.t_rif40_inv_covariates_p9
       ADD CONSTRAINT /- Add support for local partitions -/ t_rif40_inv_covariates_p9_si_fk
FOREIGN KEY (study_id, inv_id) REFERENCES t_rif40_investigations_p0(study_id, inv_id)
 /- has partitions: false, is a partition: true -/
/- Referenced foreign key is partitioned -/;
psql:../psql_scripts/v4_0_study_id_partitions.sql:145: INFO:  [DEBUG1] rif40_ddl(): EXPLAIN SQL> EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE l_3244_2456828_55876_269000 AS
WITH a AS (
SELECT inhrelid, inhparent, inhseqno,
       b1.relname AS partition_table_name,
       b2.relname AS master_table_name,
       ROW_NUMBER() OVER(PARTITION BY b2.relname ORDER BY inhrelid) AS part_seq_no,
       COUNT(inhparent) OVER(PARTITION BY b2.relname) AS total_part,
       CASE
                WHEN a.inhparent = 5120807   THEN 'master ref fk partition'
                WHEN a.inhrelid = 5120807    THEN 'partition ref fk partition'
                WHEN a.inhparent = 214281 THEN 'master ref fk table'
                WHEN a.inhrelid = 214281  THEN 'partition ref fk table'
                WHEN a.inhparent = 214255    THEN 'this master table'
                WHEN a.inhrelid = 214255     THEN 'this partition table'
                ELSE 'Other' END AS rel_type
  FROM pg_inherits a
        LEFT OUTER JOIN pg_class b1 ON (b1.oid = a.inhrelid)
        LEFT OUTER JOIN pg_class b2 ON (b2.oid = a.inhparent)
  WHERE a.inhparent = 5120807 /- ref_fk_part_oid -/
     OR a.inhrelid = 5120807 /- ref_fk_part_oid -/
     OR a.inhparent = 214281 /- ref_fk_master_oid -/
     OR a.inhrelid = 214281 /- ref_fk_master_oid -/
     OR a.inhparent = 214255 /- this_table_oid -/
     OR a.inhrelid = 214255 /- this_table_oid -/
)
SELECT * FROM a;
psql:../psql_scripts/v4_0_study_id_partitions.sql:145: INFO:  [DEBUG1] rif40_ddl(): CTE Scan on a  (cost=34.43..34.47 rows=2 width=188) (actual time=0.326..0.356 rows=16 loops=1)
  Output: a.inhrelid, a.inhparent, a.inhseqno, a.partition_table_name, a.master_table_name, a.part_seq_no, a.total_part, a.rel_type
  CTE a
    ->  WindowAgg  (cost=34.33..34.43 rows=2 width=140) (actual time=0.323..0.337 rows=16 loops=1)
          Output: a_1.inhrelid, a_1.inhparent, a_1.inhseqno, b1.relname, b2.relname, (row_number() OVER (?)), count(a_1.inhparent) OVER (?), CASE WHEN (a_1.inhparent = 5120807::oid
) THEN 'master ref fk partition'::text WHEN (a_1.inhrelid = 5120807::oid) THEN 'partition ref fk partition'::text WHEN (a_1.inhparent = 214281::oid) THEN 'master ref fk table'::tex
t WHEN (a_1.inhrelid = 214281::oid) THEN 'partition ref fk table'::text WHEN (a_1.inhparent = 214255::oid) THEN 'this master table'::text WHEN (a_1.inhrelid = 214255::oid) THEN 'th
is partition table'::text ELSE 'Other'::text END
          ->  WindowAgg  (cost=34.33..34.37 rows=2 width=140) (actual time=0.287..0.305 rows=16 loops=1)
                Output: a_1.inhrelid, b2.relname, a_1.inhparent, a_1.inhseqno, b1.relname, row_number() OVER (?)
                ->  Sort  (cost=34.33..34.34 rows=2 width=140) (actual time=0.283..0.286 rows=16 loops=1)
                      Output: a_1.inhrelid, b2.relname, a_1.inhparent, a_1.inhseqno, b1.relname
                      Sort Key: b2.relname, a_1.inhrelid
                      Sort Method: quicksort  Memory: 29kB
                      ->  Nested Loop Left Join  (cost=0.56..34.32 rows=2 width=140) (actual time=0.045..0.265 rows=16 loops=1)
                            Output: a_1.inhrelid, b2.relname, a_1.inhparent, a_1.inhseqno, b1.relname
                            ->  Nested Loop Left Join  (cost=0.28..17.71 rows=2 width=76) (actual time=0.039..0.169 rows=16 loops=1)
                                  Output: a_1.inhrelid, a_1.inhparent, a_1.inhseqno, b1.relname
                                  ->  Seq Scan on pg_catalog.pg_inherits a_1  (cost=0.00..1.10 rows=2 width=12) (actual time=0.032..0.041 rows=16 loops=1)
                                        Output: a_1.inhrelid, a_1.inhparent, a_1.inhseqno
                                        Filter: ((a_1.inhparent = 5120807::oid) OR (a_1.inhrelid = 5120807::oid) OR (a_1.inhparent = 214281::oid) OR (a_1.inhrelid = 214281::oid) OR
 (a_1.inhparent = 214255::oid) OR (a_1.inhrelid = 214255::oid))
                                        Rows Removed by Filter: 68
                                  ->  Index Scan using pg_class_oid_index on pg_catalog.pg_class b1  (cost=0.28..8.29 rows=1 width=68) (actual time=0.003..0.007 rows=1 loops=16)
                                        Output: b1.relname, b1.oid
                                        Index Cond: (b1.oid = a_1.inhrelid)
                            ->  Index Scan using pg_class_oid_index on pg_catalog.pg_class b2  (cost=0.28..8.29 rows=1 width=68) (actual time=0.002..0.005 rows=1 loops=16)
                                  Output: b2.relname, b2.oid
                                  Index Cond: (b2.oid = a_1.inhparent)
Total runtime: 3.618 ms
psql:../psql_scripts/v4_0_study_id_partitions.sql:145: INFO:  rif40_method4():
inheritance table
-----------------
psql:../psql_scripts/v4_0_study_id_partitions.sql:145: INFO:  rif40_method4():
inhrelid | inhparent | inhseqno   | partition_table_name | master_table_name | part_seq_no           | total_part            | rel_type
------------------------------------------------------------------------------------------------------------------------------------------
5120567  | 214281    | 1          | t_rif40_inv_covariates_p1 | t_rif40_inv_covariates | 1                     | 16                    | "master ref fk table"
5120597  | 214281    | 1          | t_rif40_inv_covariates_p2 | t_rif40_inv_covariates | 2                     | 16                    | "master ref fk table"
5120627  | 214281    | 1          | t_rif40_inv_covariates_p3 | t_rif40_inv_covariates | 3                     | 16                    | "master ref fk table"
5120657  | 214281    | 1          | t_rif40_inv_covariates_p4 | t_rif40_inv_covariates | 4                     | 16                    | "master ref fk table"
5120687  | 214281    | 1          | t_rif40_inv_covariates_p5 | t_rif40_inv_covariates | 5                     | 16                    | "master ref fk table"
5120717  | 214281    | 1          | t_rif40_inv_covariates_p6 | t_rif40_inv_covariates | 6                     | 16                    | "master ref fk table"
5120747  | 214281    | 1          | t_rif40_inv_covariates_p7 | t_rif40_inv_covariates | 7                     | 16                    | "master ref fk table"
5120777  | 214281    | 1          | t_rif40_inv_covariates_p8 | t_rif40_inv_covariates | 8                     | 16                    | "master ref fk table"
5120807  | 214281    | 1          | t_rif40_inv_covariates_p9 | t_rif40_inv_covariates | 9                     | 16                    | "partition ref fk partition"
5120837  | 214281    | 1          | t_rif40_inv_covariates_p10 | t_rif40_inv_covariates | 10                    | 16                    | "master ref fk table"
5120867  | 214281    | 1          | t_rif40_inv_covariates_p11 | t_rif40_inv_covariates | 11                    | 16                    | "master ref fk table"
5120897  | 214281    | 1          | t_rif40_inv_covariates_p12 | t_rif40_inv_covariates | 12                    | 16                    | "master ref fk table"
5120927  | 214281    | 1          | t_rif40_inv_covariates_p13 | t_rif40_inv_covariates | 13                    | 16                    | "master ref fk table"
5120957  | 214281    | 1          | t_rif40_inv_covariates_p14 | t_rif40_inv_covariates | 14                    | 16                    | "master ref fk table"
5120987  | 214281    | 1          | t_rif40_inv_covariates_p15 | t_rif40_inv_covariates | 15                    | 16                    | "master ref fk table"
5121017  | 214281    | 1          | t_rif40_inv_covariates_p16 | t_rif40_inv_covariates | 16                    | 16                    | "master ref fk table"
(16 rows)

 */
--
-- See above error: case already dealt with...
--				ELSE
--
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:145: INFO:  [DEBUG1] _rif40_common_partition_create_setup(): Automatic range/hash partition by study_id: rif40.t_rif40_investigati ons; table name has 0 partitions, referenced foreign key table: rif40.t_rif40_results has no partitions; ignored
-- 
--					RAISE plpgsql_error;
				END IF;	
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

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_create_setup(VARCHAR, VARCHAR, VARCHAR, INTEGER, OUT VARCHAR[], OUT VARCHAR[], OUT VARCHAR[], OUT INTEGER, OUT INTEGER, OUT INTEGER) IS 'Function: 	_rif40_common_partition_create_setup()
Parameters:	Schema, table, column, if hash partition: number of partitions that will be created
                [OUT] ddl statement array, [OUT] foreign key statement (re-)creation array,
	       	[OUT] num_partitions, [OUT] min_value, [OUT] total_rows, [OUT] warnings
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
