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

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_common_partition_create(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_common_partition_create(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]);


CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_create(
	l_schema 	VARCHAR, 
	master_table 	VARCHAR, 
	partition_table VARCHAR, 
	l_column	VARCHAR, 
	l_value		VARCHAR,
	l_table_list VARCHAR[])
RETURNS void
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_common_partition_create()
Parameters:	Partition schema, master table, partition table, column, value, list of tables in current partition build
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
	c6rpcr CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR /* Get trigger, unique, [check removed: inherited]  and exclusion constraints */
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
		   AND con.contype IN ('x', /* 'c', */ 't', 'u') /* trigger, unique, [check removed: inherited] and exclusion constraints */;
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
	c9rpcr CURSOR(l_table VARCHAR) FOR /* Get Foreign keys for table */		 
		WITH a AS (  
			SELECT con.conname, 
			       con.oid AS constraint_oid,
			       c1.oid AS this_table_oid,			   
			       NULL AS ref_fk_part_oid,
			       ns1.nspname AS con_schema_name,
			       c1.relname AS this_table_name,
			       c2.relname AS ref_fk_table_name,
			       c1.relnamespace AS schema_oid,
			       c2.relnamespace AS ref_fk_schema_oid,
			       c1.relhassubclass AS is_partitioned,
			       c2.relhassubclass AS is_ref_fk_partitioned
			  FROM pg_constraint con
			        LEFT OUTER JOIN pg_namespace ns1 ON (con.connamespace = ns1.oid)
			        LEFT OUTER JOIN pg_class c1 ON (con.conrelid = c1.oid) /* Foreign keys for this table */
			        LEFT OUTER JOIN pg_class c2 ON (con.confrelid = c2.oid) /* Foreign key: referenced table */	
			 WHERE c1.relname    = l_table 	/* This table */
			   AND con.contype   = 'f'     	/* Foreign key constraints */
		)
		SELECT conname, constraint_oid, this_table_oid, ref_fk_part_oid, 
	               con_schema_name, ref_fk_table_name, this_table_name,
	               ns2.nspname AS schema_name, ns3.nspname AS ref_fk_schema_name,
		       is_partitioned, is_ref_fk_partitioned, 
		       CASE 
				WHEN constraint_oid IS NOT NULL THEN 'ALTER TABLE '||con_schema_name||'.'||this_table_name||E'\n'||
					'       ADD CONSTRAINT /* Add support for local partitions */ '||conname||E'\n'||
					pg_get_constraintdef(constraint_oid)||E'\n'||
					'/* Referenced foreign key table: '||ns3.nspname||'.'||ref_fk_table_name||' has partitions: '||
					is_ref_fk_partitioned::VARCHAR||' */'
				ELSE NULL 
		       END AS add_constraint_def,
		       pg_get_constraintdef(constraint_oid) AS constraintdef
		  FROM a
		        LEFT OUTER JOIN pg_namespace ns2 ON (a.schema_oid = ns2.oid)
		        LEFT OUTER JOIN pg_namespace ns3 ON (a.ref_fk_schema_oid = ns3.oid)
		 ORDER BY con_schema_name, conname;		  	 
--		 
	c1_rec 		RECORD;
	c4_rec 		RECORD;
	c5_rec 		RECORD;
	c6_rec 		RECORD;
	c7_rec 		RECORD;
	c8_rec 		RECORD;
	c9_rec 		RECORD;
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
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Adding indexes, primary key to: %.% using: %.%', 
		l_schema::VARCHAR, 
		partition_table::VARCHAR, 
		l_schema::VARCHAR, 
		master_table::VARCHAR);
	FOR c4_rec IN c4rpcr('rif40', master_table, l_column) LOOP
		I:=i+1;
		IF c4_rec.indisunique AND c4_rec.indisprimary AND c4_rec.constraint_def IS NOT NULL THEN
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
														REPLACE(
																c4_rec.constraint_def::VARCHAR, 
																master_table, partition_table),
														'rif40.', 
														l_schema||'.')||E'\n'||'/* Primary key */';
		ELSE
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
														REPLACE(
																REPLACE(
																		c4_rec.index_def::VARCHAR, 
																		master_table, partition_table),
																c4_rec.index_name, c4_rec.index_name||'_p'||l_value 
																/* Handle indexes without master table in name */),
														'rif40.', 
														l_schema||'.')||E'\n'||'/* Index */';
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
	IF i = 0 THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create', 'Added no indexes to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
		warnings:=warnings+1;
	ELSIF NOT pk_found THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_common_partition_create', 'Added % indexes to partition: %.%, no primary key',
			i::VARCHAR,		
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
		warnings:=warnings+1;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added % indexes to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	END IF;

--	IF partition_table = 't_rif40_studies_p9' THEN
--		RAISE plpgsql_error;
--	END IF;

--
-- Add foreign keys
--
	i:=0;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Adding foreign keys to: %.%', 
		l_schema::VARCHAR, 
		partition_table::VARCHAR);
	FOR c5_rec IN c5rpcr('rif40', master_table) LOOP
		i:=i+1;
		IF NOT ARRAY[master_table]::VARCHAR[] <@ l_table_list THEN /* List does not contain c2_rec.ref_fk_table_name */
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'FK Constraint[%] % on: %.%(%)', 
				i::VARCHAR,
				c5_rec.conname::VARCHAR, 
				l_schema::VARCHAR, 
				partition_table::VARCHAR, 
				c5_rec.child_columns::VARCHAR);
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
														REPLACE(
																REPLACE(c5_rec.constraint_def, 
																		master_table, partition_table),
																c5_rec.conname, c5_rec.conname||'_p'||l_value
																/* Handle constraints without master table in name */),
														'rif40.', 
														l_schema||'.');
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Deferred FK Constraint[%] % on: %.%(%)', 
				i::VARCHAR,
				c5_rec.conname::VARCHAR, 
				l_schema::VARCHAR, 
				partition_table::VARCHAR, 
				c5_rec.child_columns::VARCHAR) /* Should be created by _rif40_common_partition_create_setup() */;		
		END IF;
	END LOOP;
	IF i > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added % referenced foreign keys to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Added no referenced foreign keys to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	END IF;
--
-- Foreign keys to tables not in the psrtition list
-- 	
	i:=0;
	FOR c9_rec IN c9rpcr(master_table) LOOP
		i:=i+1;
		IF NOT ARRAY[c9_rec.ref_fk_table_name]::VARCHAR[] <@ l_table_list THEN /* List does not contain c2_rec.ref_fk_table_name */
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
														REPLACE(c9_rec.add_constraint_def, 
																c9_rec.con_schema_name||'.'||c9_rec.this_table_name, 
																l_schema||'.'||c9_rec.this_table_name||'_p'||l_value),
														c9_rec.conname,
														c9_rec.conname||'_p'||l_value);	
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'FK Constraint[%] % to: %;'||E'\n'||'SQL> %', 
				i::VARCHAR,
				c9_rec.conname::VARCHAR, 
				c9_rec.ref_fk_table_name::VARCHAR,
				ddl_stmt[array_length(ddl_stmt, 1)]::VARCHAR);																
--			RAISE INFO 'Aborting (script being tested)';
--			RAISE EXCEPTION 'C20999: Abort';																
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Deferred FK Constraint[%] % to: %', 
				i::VARCHAR,
				c9_rec.conname::VARCHAR, 
				c9_rec.ref_fk_table_name::VARCHAR	 /* Should be created by _rif40_common_partition_create_setup() */);		
		END IF;				
	END LOOP;
	IF i > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 
			'Added % foreign keys to tables not in the psrtition list', 
			i::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 
			'Added no foreign keys to tables not in the psrtition list');
	END IF;
	
--
-- Add trigger, unique, check and exclusion constraints
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Adding trigger, unique, check and exclusion constraints to: %.%', 
		l_schema::VARCHAR, 
		partition_table::VARCHAR);
	i:=0;
	FOR c6_rec IN c6rpcr('rif40', master_table) LOOP
		i:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', '% constraint[%] % on: %.%', 
			c6_rec.constraint_type::VARCHAR,
			i::VARCHAR,
			c6_rec.conname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
													REPLACE(
															REPLACE(c6_rec.constraint_def, 
																	master_table, partition_table),
															c6_rec.conname, c6_rec.conname||'_p'||l_value
															/* Handle constraints without master table in name */),
													'rif40.', 
													l_schema||'.');
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
	FOR c7_rec IN c7rpcr('rif40', master_table) LOOP
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
	FOR c8_rec IN c8rpcr('rif40', master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_create', 'Grant[%] % on: %.% to %; grant option: %', 
			i::VARCHAR,
			c8_rec.privilege_types::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR,
			c8_rec.grantee::VARCHAR,
			c8_rec.is_grantable::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(
													REPLACE(
															c8_rec.grant_def, master_table, partition_table),
													'rif40.', 
													l_schema||'.');
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
	
--	IF master_table = 't_rif40_investigations' THEN
--		RAISE EXCEPTION 'Stop I';
--	END IF;
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

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_create(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) IS 'Function: 	_rif40_common_partition_create()
Parameters:	Schema, master table, partition table, column, list of tables in current partition build
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

--
-- Eof
