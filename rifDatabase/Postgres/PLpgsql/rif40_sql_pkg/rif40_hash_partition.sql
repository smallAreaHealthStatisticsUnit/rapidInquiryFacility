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

DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_hash_partition(VARCHAR, VARCHAR, VARCHAR, INTEGER);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_hash_partition(VARCHAR, VARCHAR, VARCHAR, VARCHAR[], INTEGER);

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_hash_partition(
	l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, l_table_list VARCHAR[], l_num_partitions INTEGER DEFAULT 16)
RETURNS VARCHAR[]
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_hash_partition()
Parameters:	Schema, table, column, number of partitions, list of tables in current partition build, number of foreign keys (OUT)
Returns:	Foreign key rebuild SQL array
Description:	Hash partition schema.table on column

 */
DECLARE
 	c1gangep 		REFCURSOR;
	c2gangep CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR			/* List of columns in original order */
		SELECT *
	          FROM information_schema.columns
	         WHERE table_schema = l_schema
	           AND table_name   = l_table
	         ORDER BY ordinal_position;
--
	c2_rec 			RECORD;
	create_setup 		RECORD;
	create_insert 		RECORD;
--
	sql_stmt 		VARCHAR;
	rec_list		VARCHAR;
	bind_list		VARCHAR;
	ddl_stmt 		VARCHAR[];
	l_ddl_stmt 		VARCHAR[];
	num_partitions		INTEGER;
	min_value		VARCHAR;
	total_rows		INTEGER;
	n_num_partitions	INTEGER;
	n_total_rows		INTEGER;
	i			INTEGER:=0;
	j			INTEGER:=0;
	warnings		INTEGER:=0;
	total_partitions	INTEGER;
	p_schema	VARCHAR:='rif40_partitions';
	fk_stmt VARCHAR[];	
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Check if table is already partitioned
--
	total_partitions:=rif40_sql_pkg._rif40_partition_count(l_schema, l_table);
	IF total_partitions >= 1 THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_hash_partition', 
			'Automatic hash partition by %: %.%; table name is already partitioned into: % partitions', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, total_partitions::VARCHAR);
	END IF;

--
-- Add hash_partition_number if required
--
	ddl_stmt[1]:='ALTER TABLE '||l_schema||'.'||quote_ident(l_table)||' ADD COLUMN hash_partition_number INTEGER';
	ddl_stmt[2]:='COMMENT ON COLUMN '||l_schema||'.'||quote_ident(l_table)||'.hash_partition_number IS ''Hash partition number (for partition elimination)''';
--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);

--
-- Call: _rif40_common_partition_create_setup()
--
	create_setup:=rif40_sql_pkg._rif40_common_partition_create_setup(
		l_schema, 			/* Schema of source data (rif_data usually) */
		p_schema, 			/* partition schema */
		l_table, 			/* Table name */
		l_column, 			/* Column name */
		l_table_list, 		/* list of tables in current partition build */	
		l_num_partitions 	/* if hash partition: number of partitions that will be created */);
--
-- Force creation - tables are mainly empty in dev.
--
--	IF create_setup.ddl_stmt IS NULL THEN /* Un partitionable */
--		RETURN;
--	END IF;

--
-- Copy out parameters
--
	ddl_stmt:=create_setup.ddl_stmt;
	fk_stmt:=create_setup.fk_stmt;
	num_partitions:=create_setup.num_partitions;
	min_value:=create_setup.min_value;
	warnings:=create_setup.warnings;
	total_rows:=create_setup.total_rows;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_hash_partition', 
		'Automatic hash partitioning by %: %.%;  rows: %; partitions: %; warnings: %', 
		l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, 
		total_rows::VARCHAR, num_partitions::VARCHAR, warnings::VARCHAR);

--
-- Create auto hash trigger function
--
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='CREATE FUNCTION '||quote_ident(l_schema)||'.'||quote_ident(l_table||'_insert')||'()'||E'\n'||
'  RETURNS trigger AS'||E'\n'||
'$BODY$'||E'\n'||
'DECLARE'||E'\n'||
'	sql_stmt 	VARCHAR;'||E'\n'||
'	p_table		VARCHAR;'||E'\n'||
'	p_hash		VARCHAR;'||E'\n'||
'	num_partitions	VARCHAR:='||l_num_partitions::Text||';'||E'\n'||
'--'||E'\n'||
'	error_message VARCHAR;'||E'\n'||
'	p_schema	VARCHAR:='''||p_schema||''';'||E'\n'||
'	v_detail VARCHAR:=''(Not supported until 9.2; type SQL statement into psql to see remote error)'';'||E'\n'||
'BEGIN'||E'\n'||
'--'||E'\n'||
'-- Check partition field is not null'||E'\n'||
'--'||E'\n'||
'	IF new.'||quote_ident(l_column)||' IS NULL THEN'||E'\n'||
'		PERFORM rif40_log_pkg.rif40_error(-19001, '''||quote_ident(l_table||'_insert')||''','||E'\n'||
'		       	''NULL value for partition column '||quote_ident(l_column)||''');'||E'\n'||
'	END IF;'||E'\n'||
'	p_hash:=rif40_sql_pkg._rif40_hash(NEW.'||l_column||'::text, '||l_num_partitions::Text||')::Text;'||E'\n'||
--
-- Fix for 42P01 error
--
-- p_table:=quote_ident('p_t_rif40_studies_p'||p_hash);
-- Should be:
-- p_table:=quote_ident('t_rif40_studies_p'||p_hash);
--
'	p_table:=quote_ident('''||l_table||'_p''||p_hash);'||E'\n'||
'	BEGIN'||E'\n'||
'--'||E'\n'||
'-- Copy columns from NEW'||E'\n'||
'--'||E'\n';
	FOR c2_rec IN c2gangep(l_schema, l_table) LOOP
		i:=i+1;
		IF rec_list IS NULL THEN
			rec_list:='NEW.'||c2_rec.column_name;
			bind_list:='$'||i::Text;
		ELSE
			rec_list:=rec_list||', NEW.'||c2_rec.column_name;
			bind_list:=bind_list||', $'||i::Text;
		END IF;
	END LOOP;
	ddl_stmt[array_length(ddl_stmt, 1)]:=ddl_stmt[array_length(ddl_stmt, 1)]||
'		sql_stmt:= ''INSERT INTO ''||p_schema||''.''||p_table||'' VALUES ('||bind_list||') /* Partition: ''||p_hash||'' of ''||num_partitions||'' */'';'||E'\n'||
'--		PERFORM rif40_log_pkg.rif40_log(''DEBUG3'', '''||quote_ident(l_table||'_insert')||''','||E'\n'||
'--			''Row N SQL> EXECUTE ''''%'''' USING '||rec_list||'; /* rec: % */'', sql_stmt::VARCHAR, NEW.*::VARCHAR);'||E'\n'||
'		EXECUTE sql_stmt USING '||rec_list||';'||E'\n'||
'	EXCEPTION'||E'\n'||
'		WHEN others THEN'||E'\n'||
'			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;'||E'\n'||
'			error_message:='''||quote_ident(l_table||'_insert')||'() caught: ''||SQLSTATE::VARCHAR||E''\n''||SQLERRM::VARCHAR||'' in SQL>''||E''\n''||sql_stmt||'';''||E''\n''||''(see previous trapped error)''||E''\n''||''Detail: ''||''(''||SQLSTATE||'') ''||v_detail::VARCHAR||'' '';'||E'\n'|| 
'			RAISE INFO ''3: %'', error_message;'||E'\n'||
'--'||E'\n'||
'			RAISE;'||E'\n'||
'	END;'||E'\n'||
'--'||E'\n'||
'	RETURN NULL /* You must return NULL or you will INSERT into the master table... */;'||E'\n'||
'END;'||E'\n'||
'$BODY$'||E'\n'||
'  LANGUAGE plpgsql';

	ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON FUNCTION  '||quote_ident(l_schema)||'.'||quote_ident(l_table||'_insert')||'()'||
       		' IS ''Hash partition INSERT function for table: '||l_table||'''';
--
-- If debug is enabled add newly created function 
--
	IF rif40_log_pkg.rif40_is_debug_enabled('rif40_hash_partition', 'DEBUG1') THEN
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='SELECT rif40_log_pkg.rif40_add_to_debug('''||quote_ident(l_table||'_insert')||':DEBUG1'')';
	END IF;
-- 
-- Add trigger to existing table
--
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='CREATE TRIGGER '||quote_ident(l_table||'_insert')||E'\n'||
'  BEFORE INSERT ON '||quote_ident(l_schema)||'.'||quote_ident(l_table)||E'\n'||
'  FOR EACH ROW'||E'\n'||
'  EXECUTE PROCEDURE '||quote_ident(l_table||'_insert')||'()';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON TRIGGER '||quote_ident(l_table||'_insert')||E'\n'||
		' ON '||quote_ident(l_schema)||'.'||quote_ident(l_table)||E'\n'||
		' IS ''Partition INSERT trigger by '||quote_ident(l_column)||' for: '||quote_ident(l_schema)||'.'||quote_ident(l_table)||
		'; calls '||quote_ident(l_table||'_insert')||'(). Automatically creates partitions''';
--
-- Create hash partitions
--
	FOR i IN 1 .. l_num_partitions LOOP
		PERFORM _rif40_hash_partition_create(p_schema /* partitions schema */,
			l_table, l_table||'_p'||i::Text, l_column, i, l_num_partitions, l_table_list);
	END LOOP;

--	IF l_table = 't_rif40_investigations' THEN
--		RAISE plpgsql_error;
--	END IF;

--
-- Remove INSERT triggers from master table so they don't fire twice (replace with no re-enable) 
--
--	PERFORM rif40_sql_pkg._rif40_drop_master_trigger(l_schema, l_table);

--
-- Call: _rif40_hash_partition_create_insert()
--
	create_insert:=rif40_sql_pkg._rif40_hash_partition_create_insert(p_schema /* partitions schema */, l_schema /* Source table schema */,
			l_table, l_column, total_rows, l_num_partitions);
	IF create_insert.ddl_stmt IS NULL THEN /* Un partitionable */
		RETURN NULL;
	END IF;
--
-- Copy out parameters
--
	FOR i IN 1 .. array_length(create_insert.ddl_stmt, 1) LOOP
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=create_insert.ddl_stmt[i];
	END LOOP;

--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);

--
-- Check number of rows match original
--
	sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(l_column)||')) AS num_partitions, COUNT('||quote_ident(l_column)||') AS total_rows'||E'\n'||
		'FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||' LIMIT 1'; 
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_hash_partition', 'SQL> %;', sql_stmt::VARCHAR);
	OPEN c1gangep FOR EXECUTE sql_stmt;
	FETCH c1gangep INTO n_num_partitions, n_total_rows;
	CLOSE c1gangep;
--
	IF num_partitions = n_num_partitions AND total_rows = n_total_rows THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_hash_partition', 'Partition of: %.% created % partitions, % rows total OK', 
			l_schema::VARCHAR, l_table::VARCHAR, num_partitions::VARCHAR, total_rows::VARCHAR);
	ELSIF total_rows != n_total_rows THEN
		PERFORM rif40_log_pkg.rif40_error(-20190, 'rif40_hash_partition', 'Partition of: %.% rows mismatch: expected: %, got % rows total', 
			l_schema::VARCHAR, l_table::VARCHAR, total_rows::VARCHAR, n_total_rows::VARCHAR);
	ELSIF l_num_partitions = num_partitions THEN
-- Hash partition - test not valid
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-20191, 'rif40_hash_partition', 'Partition of: %.% partition mismatch: expected: %, got % partition total', 
			l_schema::VARCHAR, l_table::VARCHAR, num_partitions::VARCHAR, n_num_partitions::VARCHAR);
	END IF;

--
-- Call: _rif40_common_partition_create_complete()
--
	BEGIN
		PERFORM rif40_sql_pkg._rif40_common_partition_create_complete(l_schema, l_table, l_column, create_insert.index_name);
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='rif40_hash_partition() caught in _rif40_common_partition_create_complete(): '||E'\n'||
				SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
			RAISE INFO '2: %', error_message;
--
			RAISE;
	END;

--	IF l_table = 't_rif40_studies' THEN
--		RAISE EXCEPTION 'C20999: Stopping at t_rif40_studies';
--	END IF;

--
-- Test partition exclusion. This currently does not work and Postgres only supports range values (e.g. year = 1999; not 
-- study_id = rif40_sql_pkg._rif40_hash_bucket_check(study_id, 16 /* total buckets */, 15 /* bucket requested */)
--
-- Need a simpler example and look at the code in predtest.c
--
--'  WHERE '||quote_ident(l_column)||'::VARCHAR = ''1''/* ||min_first_part_value */'||E'\n'||
	sql_stmt:='WITH a AS (SELECT rif40_sql_pkg._rif40_hash('||min_value||'::VARCHAR, 16) AS part_no )'||E'\n'||
	        'SELECT b.*, rif40_sql_pkg._rif40_hash('||quote_ident(l_column)||'::VARCHAR, 16) AS hash,'||E'\n'||
	        '         rif40_sql_pkg._rif40_hash_bucket_check('||quote_ident(l_column)||', 16, '||E'\n'||
	        '               rif40_sql_pkg._rif40_hash('||quote_ident(l_column)||'::VARCHAR, 16)) AS hash_check'||E'\n'||
		'  FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||' b, a'||E'\n'||
		' WHERE '||quote_ident(l_column)||' = '||min_value||E'\n'||
		'   AND rif40_sql_pkg._rif40_hash('||quote_ident(l_column)||'::VARCHAR, 16) = hash_partition_number'||E'\n'||
		'   AND hash_partition_number = a.part_no'||E'\n'||
--		'   AND 8 = hash_partition_number'||E'\n'|| /* Eliminates */
	        ' ORDER BY 1 LIMIT 10'; 
--
-- Turn on parser debugging. This appears mainly in the eventlog/syslog; not stderr
--
-- debug_print_parse (boolean)
-- debug_print_rewritten (boolean)
-- debug_print_plan (boolean)
-- 
/*
	ddl_stmt:=NULL;
	ddl_stmt[1]:='SET SESSION client_min_messages = DEBUG5';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='SET SESSION log_min_messages = DEBUG5';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='SET SESSION debug_print_parse = TRUE';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='SET SESSION debug_print_rewritten = TRUE';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='SET SESSION debug_print_plan = TRUE';
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);
 */
--
-- This will eliminate; at a cost in all queries needing to use this format
-- rif40_sql_pkg._rif40_hash(study_id) = hash_partition_number will NOT eliminate;  almost certainly because
-- it cannot be evavulated at parse time; this is also true of the earlier CHECK constraint which also does not work.
-- CONSTRAINT rif40_study_shares_p15_ck CHECK (study_id = rif40_sql_pkg._rif40_hash_bucket_check(study_id, 16 /* total buckets */, 15 /* bucket requested */))
--
	sql_stmt:='SELECT *, rif40_sql_pkg._rif40_hash('||quote_ident(l_column)||'::VARCHAR, 16) AS hash,'||E'\n'||
	        '         rif40_sql_pkg._rif40_hash_bucket_check('||quote_ident(l_column)||', 16, '||E'\n'||
	        '               rif40_sql_pkg._rif40_hash('||quote_ident(l_column)||'::VARCHAR, 16)) AS hash_check'||E'\n'||
		'  FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||E'\n'||
		' WHERE rif40_sql_pkg._rif40_hash('||min_value||'::VARCHAR, 16) = hash_partition_number /* Elininates */'||E'\n'||
		'   AND '||quote_ident(l_column)||' = '||min_value||E'\n'||
	        ' ORDER BY 1 LIMIT 10'; 
	PERFORM rif40_sql_pkg.rif40_method4(sql_stmt, 'Partition EXPLAIN test 2 (Elinination)');
/*
psql:../psql_scripts/v4_0_study_id_partitions.sql:145: INFO:  [DEBUG1] rif40_ddl(): Limit  (cost=0.00..1.58 rows=2 width=210) (actual time=0.085..0.087 rows=1 loops=1)
  Output: rif40_study_shares.study_id, rif40_study_shares.grantor, rif40_study_shares.grantee_username, rif40_study_shares.hash_partition_number, (((abs(hashtext(((rif40_study_shar
es.study_id)::character varying)::text)) % 16) + 1)), (_rif40_hash_bucket_check(rif40_study_shares.study_id, 16, ((abs(hashtext(((rif40_study_shares.study_id)::character varying)::
text)) % 16) + 1)))
  ->  Result  (cost=0.00..1.58 rows=2 width=210) (actual time=0.082..0.083 rows=1 loops=1)
        Output: rif40_study_shares.study_id, rif40_study_shares.grantor, rif40_study_shares.grantee_username, rif40_study_shares.hash_partition_number, ((abs(hashtext(((rif40_study
_shares.study_id)::character varying)::text)) % 16) + 1), _rif40_hash_bucket_check(rif40_study_shares.study_id, 16, ((abs(hashtext(((rif40_study_shares.study_id)::character varying
)::text)) % 16) + 1))
        ->  Append  (cost=0.00..1.01 rows=2 width=210) (actual time=0.007..0.008 rows=1 loops=1)
              ->  Seq Scan on rif40.rif40_study_shares  (cost=0.00..0.00 rows=1 width=404) (actual time=0.001..0.001 rows=0 loops=1)
                    Output: rif40_study_shares.study_id, rif40_study_shares.grantor, rif40_study_shares.grantee_username, rif40_study_shares.hash_partition_number
                    Filter: ((8 = rif40_study_shares.hash_partition_number) AND (rif40_study_shares.study_id = 1))
              ->  Seq Scan on rif40.rif40_study_shares_p8  (cost=0.00..1.01 rows=1 width=16) (actual time=0.005..0.006 rows=1 loops=1)
                    Output: rif40_study_shares_p8.study_id, rif40_study_shares_p8.grantor, rif40_study_shares_p8.grantee_username, rif40_study_shares_p8.hash_partition_number
                    Filter: ((8 = rif40_study_shares_p8.hash_partition_number) AND (rif40_study_shares_p8.study_id = 1))
Total runtime: 1.310 ms
 */
--
-- Used to halt alter_1.sql for testing
--
--	RAISE plpgsql_error;
	RETURN fk_stmt;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_hash_partition(VARCHAR, VARCHAR, VARCHAR, VARCHAR[], INTEGER) IS 'Function: 	rif40_hash_partition()
Parameters:	Schema, table, columnn, number of partitions
Returns:	Foreign key rebuild SQL array
Description:	Hash partition schema.table on column
';

--
-- Eof
