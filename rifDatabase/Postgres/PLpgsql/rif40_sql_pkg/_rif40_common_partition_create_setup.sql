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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_create_setup(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR,
	hash_partition_count INTEGER,
       	OUT ddl_stmt VARCHAR[], OUT fk_stmt VARCHAR[], OUT num_partitions INTEGER, OUT min_value VARCHAR, OUT total_rows INTEGER, OUT warnings INTEGER)
RETURNS RECORD
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_common_partition_create_setup()
Parameters:	Schema of source data (rif_data usually), table, column, if hash partition: number of partitions that will be created
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

--
-- Eof