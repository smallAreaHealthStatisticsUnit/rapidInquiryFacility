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
-- Rapid Enquiry Facility (RIF) - Create SAHSULAND rif40 exmaple schema
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

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_range_partition(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_range_partition()
Parameters:	Schema, table, column
Returns:	Nothing
Description:	Automatic range partition schema.table on column

* Must be rif40 or have rif_user or rif_manager role
* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)
* Do not partition if table has only one distinct row
* Do not partition if table has no rows
* Create auto range trigger function

CREATE OR REPLACE FUNCTION rif40.sahsuland_cancer_insert()
  RETURNS trigger AS
$BODY$
DECLARE
        sql_stmt        VARCHAR;
        p_table         VARCHAR;
        p_value         VARCHAR;
--
        error_message VARCHAR;
        v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Check partition field is not null
--
        IF new.year IS NULL THEN
                PERFORM rif40_log_pkg.rif40_error(-19001, 'sahsuland_cancer_insert',
                        'NULL value for partition column year');
        END IF;
        p_table:=quote_ident('sahsuland_cancer_'||NEW.year::Text);
        p_value:=NEW.year::Text;
        BEGIN
--
-- Copy columns from NEW
--
                sql_stmt:= 'INSERT INTO '||p_table||' VALUES ($1, $2, $3, $4, $5, $6, $7, $8) /- Partition: '||p_value||' -/';
--              PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'sahsuland_cancer_insert',
--                      'SQL> %; rec: %', sql_stmt::VARCHAR, rec::VARCHAR);
                EXECUTE sql_stmt USING NEW.year, NEW.age_sex_group, NEW.level1, NEW.level2, NEW.level3, NEW.level4, NEW.icd, NEW.tot
al;
        EXCEPTION
                WHEN undefined_table /- e.g. 42p01: relation "rif40.rif40_population_europe_1991" does not exist -/ THEN
                        PERFORM rif40_log_pkg.rif40_log('INFO', 'sahsuland_cancer_insert',
                                'Adding partition for year: % to table: %',
                                NEW.year::VARCHAR, 'sahsuland_cancer'::VARCHAR);
--
-- Add partition
--
                       PERFORM rif40_sql_pkg._rif40_range_partition_create('rif40', 'sahsuland_cancer', p_table, 'year', p_value);
--
-- Re-insert failed row
--
                        EXECUTE sql_stmt USING NEW.year, NEW.age_sex_group, NEW.level1, NEW.level2, NEW.level3, NEW.level4, NEW.icd,
 NEW.total;
                        RETURN NEW;
                WHEN others THEN
                        GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
                        error_message:='sahsuland_cancer_insert() caught: ||SQLSTATE::VARCHAR'||E'\n'||SQLERRM::VARCHAR||' in SQL (s
ee previous trapped error)'||E'\n'||'Detail: '||'('||SQLSTATE||') '||v_detail::VARCHAR||' ';
                        RAISE INFO '3: %', error_message;
--
                        RAISE;
        END;
--
        RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql;
COMMENT ON FUNCTION  rif40.sahsuland_cancer_insert() IS 'Partition INSERT function for geography: SAHSU';

* Add trigger to existing table

CREATE TRIGGER sahsuland_cancer_insert
  BEFORE INSERT ON rif40.sahsuland_cancer
  FOR EACH ROW
  EXECUTE PROCEDURE sahsuland_cancer_insert();
COMMENT ON TRIGGER sahsuland_cancer_insert
 ON rif40.sahsuland_cancer
 IS 'Partition INSERT trigger by year for: rif40.sahsuland_cancer; calls sahsuland_cancer_insert(). Automatically creates partitions';

* Bring data back, order by range partition, primary key

INSERT INTO sahsuland_cancer
 SELECT * FROM rif40_range_partition -* Temporary table *- ORDER BY year;
EXECUTE 'INSERT INTO sahsuland_pop_1990 VALUES ($1, $2, $3, $4, $5, $6, $7) -* Partition: 1990 -/' USING new.year, ...;

* If table is a numerator, cluster

* Check number of rows match original, truncate rif40_range_partition temporary table
* Re-anaylse

 */
DECLARE
 	c1gangep 		REFCURSOR;
	c2gangep CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR			/* List of columns in original order */
		SELECT *
	          FROM information_schema.columns
	         WHERE table_schema = l_schema
	           AND table_name   = l_table
	         ORDER BY ordinal_position;
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
	c2_rec 			RECORD;
	c3_rec 			RECORD;
	c3a_rec 		RECORD;
	c4_rec 			RECORD;
	c5_rec 			RECORD;
	sql_stmt 		VARCHAR;
	rec_list		VARCHAR;
	bind_list		VARCHAR;
	ddl_stmt 		VARCHAR[];
	num_partitions		INTEGER;
	total_rows		INTEGER;
	n_num_partitions	INTEGER;
	n_total_rows		INTEGER;
	l_rows			INTEGER:=0;
	table_length		INTEGER:=0;
	i			INTEGER:=0;
	warnings		INTEGER:=0;
	name_length_limit	INTEGER:=40;	/* You may want to set this higher */
	part_test_rec		RECORD;
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_range_partition', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- Check if table is valid
--
	BEGIN
		sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(l_column)||')) AS num_partitions, COUNT('||quote_ident(l_column)||') AS total_rows FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||' LIMIT 1'; 
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'SQL> %;', sql_stmt::VARCHAR);
		OPEN c1gangep FOR EXECUTE sql_stmt;
		FETCH c1gangep INTO num_partitions, total_rows;
		GET DIAGNOSTICS l_rows = ROW_COUNT;
		CLOSE c1gangep;
		
	EXCEPTION
		WHEN others THEN
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='rif40_range_partition() caught: '||E'\n'||
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
		PERFORM rif40_log_pkg.rif40_error(-20997, 'rif40_range_partition', 
			'Automatic range partitioning by %: %.%; table name is too long %, limit is %', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, table_length::VARCHAR, (name_length_limit-5)::VARCHAR);
-- 
-- IF yes, copy to temporary table, truncate
--
	ELSIF l_rows > 0 AND num_partitions > 1 THEN
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_range_partition', 
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
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'SQL> %;', sql_stmt::VARCHAR);
			FOR part_test_rec IN EXECUTE sql_stmt LOOP
				i:=i+1;
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_range_partition', 
					'Automatic range partitioning by %: %.%; partition % contains invalid characters to be part of a partition table name', 
					l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, part_test_rec.value::VARCHAR);
			END LOOP;
--
			IF i > 0 THEN
				PERFORM rif40_log_pkg.rif40_error(-20998, 'rif40_range_partition',
					'Automatic range partitioning by %: %.%; % partitions contains invalid characters to be part of partition table names', 
					l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, i::VARCHAR);
			END IF;
		EXCEPTION
			WHEN others THEN
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='rif40_range_partition() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '2: %', error_message;
--
				RAISE;
		END;

--
-- Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)
--
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'Copy data to temporary table fron: %.%', 
			l_schema::VARCHAR, l_table::VARCHAR);
		ddl_stmt[1]:='CREATE TEMPORARY TABLE rif40_range_partition AS SELECT * FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='TRUNCATE TABLE '||quote_ident(l_schema)||'.'||quote_ident(l_table);
	ELSIF num_partitions > 1 THEN
--
-- Do not partition if table has only one distinct row
--
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_range_partition', 'Unable to automatic range partitioning by %: %.%; Not partitionable, only 1 distinct row', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR);
		RETURN;
	ELSE
--
-- Do not partition if table has no rows
--
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_range_partition', 'Unable to automatic range partitioning by %: %.%; Not partitionable, no rows', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR);
		RETURN;
	END IF;

--
-- Create auto range trigger function
--
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='CREATE OR REPLACE FUNCTION '||quote_ident(l_schema)||'.'||quote_ident(l_table||'_insert')||'()'||E'\n'||
'  RETURNS trigger AS'||E'\n'||
'$BODY$'||E'\n'||
'DECLARE'||E'\n'||
'	sql_stmt 	VARCHAR;'||E'\n'||
'	p_table		VARCHAR;'||E'\n'||
'	p_value		VARCHAR;'||E'\n'||
'--'||E'\n'||
'	error_message VARCHAR;'||E'\n'||
'	v_detail VARCHAR:=''(Not supported until 9.2; type SQL statement into psql to see remote error)'';'||E'\n'||
'BEGIN'||E'\n'||
'--'||E'\n'||
'-- Check partition field is not null'||E'\n'||
'--'||E'\n'||
'	IF new.'||quote_ident(l_column)||' IS NULL THEN'||E'\n'||
'		PERFORM rif40_log_pkg.rif40_error(-19001, '''||quote_ident(l_table||'_insert')||''','||E'\n'||
'		       	''NULL value for partition column '||quote_ident(l_column)||''');'||E'\n'||
'	END IF;'||E'\n'||
'	p_table:=quote_ident('||''''||l_table||'_''||NEW.'||l_column||'::Text'||');'||E'\n'||
'	p_value:=NEW.'||l_column||'::Text;'||E'\n'||
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
'		sql_stmt:= ''INSERT INTO ''||p_table||'' VALUES ('||bind_list||') /* Partition: ''||p_value||'' */'';'||E'\n'||
'--		PERFORM rif40_log_pkg.rif40_log(''DEBUG3'', '''||quote_ident(l_table||'_insert')||''','||E'\n'||
'--			''Row N SQL> EXECUTE ''''%'''' USING '||rec_list||'; /* rec: % */'', sql_stmt::VARCHAR, NEW.*::VARCHAR);'||E'\n'||
'		EXECUTE sql_stmt USING '||rec_list||';'||E'\n'||
'	EXCEPTION'||E'\n'||
'		WHEN undefined_table /* e.g. 42p01: relation "rif40.rif40_population_europe_1991" does not exist */ THEN'||E'\n'||
'			PERFORM rif40_log_pkg.rif40_log(''INFO'', '''||quote_ident(l_table||'_insert')||''','||E'\n'||
'			       	''Adding partition for '||quote_ident(l_column)||': % to table: %'','||E'\n'||
'				NEW.'||quote_ident(l_column)||'::VARCHAR, '''||quote_ident(l_table)||'''::VARCHAR);'||E'\n'||
'--'||E'\n'||
'-- Add partition'||E'\n'||
'--'||E'\n'||
'                       PERFORM rif40_sql_pkg._rif40_range_partition_create('''||l_schema||''', '''||l_table||''', p_table, '''||l_column||''', p_value);'||E'\n'||
'--'||E'\n'||
'-- Re-insert failed first row'||E'\n'||
'--'||E'\n'||
'			PERFORM rif40_log_pkg.rif40_log(''DEBUG1'', '''||quote_ident(l_table||'_insert')||''','||E'\n'||
'				''Row 1 SQL> EXECUTE ''''%'''' USING '||rec_list||'; /* rec: % */'', sql_stmt::VARCHAR, NEW.*::VARCHAR);'||E'\n'||
'			EXECUTE sql_stmt USING '||rec_list||';'||E'\n'||
'			RETURN NULL;'||E'\n'||
'		WHEN others THEN'||E'\n'||
'			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;'||E'\n'||
'			error_message:='''||quote_ident(l_table||'_insert')||'() caught: ||SQLSTATE::VARCHAR''||E''\n''||SQLERRM::VARCHAR||'' in SQL (see previous trapped error)''||E''\n''||''Detail: ''||''(''||SQLSTATE||'') ''||v_detail::VARCHAR||'' '';'||E'\n'|| 
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
       		' IS ''Partition INSERT function for geography: SAHSU''';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='SELECT rif40_log_pkg.rif40_add_to_debug('''||quote_ident(l_table||'_insert')||':DEBUG1'')';
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
-- GET PK/unique index column
--
	OPEN c3gangep(l_schema, l_table, l_column);
	FETCH c3gangep INTO c3_rec;
	CLOSE c3gangep;
-- 
-- Bring data back, order by range partition, primary key
--
	IF l_rows > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'Restore data from temporary table: %.%', 
			l_schema::VARCHAR, l_table::VARCHAR);
		IF c3_rec.column_names IS NOT NULL THEN
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='INSERT INTO '||quote_ident(l_table)||E'\n'||
				' SELECT * FROM rif40_range_partition /* Temporary table */ ORDER BY '||l_column||' /* Partition column */, '||
					c3_rec.column_names||' /* [Rest of ] primary key */';
		ELSE
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='INSERT INTO '||quote_ident(l_table)||E'\n'||
				' SELECT * FROM rif40_range_partition /* Temporary table */ ORDER BY '||l_column||' /* Partition column */, '||
					' /* NO [Rest of ] primary key - no unique index found */';
		END IF;
	END IF;

--
-- If table is a numerator, cluster
--
	OPEN c4gangep(l_table);
	FETCH c4gangep INTO c4_rec;
	CLOSE c4gangep;
-- 
	IF c4_rec.table_name IS NOT NULL AND c3_rec.index_name IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'Rebuild master cluster: %.%', 
			l_schema::VARCHAR, l_table::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='CLUSTER VERBOSE '||quote_ident(l_schema)||'.'||quote_ident(l_table)||
			' USING '||c3_rec.index_name;
	END IF;

--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);

--
-- Check number of rows match original, truncate rif40_range_partition temporary table
--
	sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(l_column)||')) AS num_partitions, COUNT('||quote_ident(l_column)||') AS total_rows FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||' LIMIT 1'; 
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'SQL> %;', sql_stmt::VARCHAR);
	OPEN c1gangep FOR EXECUTE sql_stmt;
	FETCH c1gangep INTO n_num_partitions, n_total_rows;
	CLOSE c1gangep;
--
	IF num_partitions = n_num_partitions AND total_rows = n_total_rows THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'Partition of: %.% created % partitions, % rows total', 
			l_schema::VARCHAR, l_table::VARCHAR, num_partitions::VARCHAR, total_rows::VARCHAR);
	ELSIF total_rows != n_total_rows THEN
		PERFORM rif40_log_pkg.rif40_error(-20190, 'rif40_range_partition', 'Partition of: %.% rows mismatch: expected: %, got % rows total', 
			l_schema::VARCHAR, l_table::VARCHAR, total_rows::VARCHAR, n_total_rows::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-20191, 'rif40_range_partition', 'Partition of: %.% partition mismatch: expected: %, got % partition total', 
			l_schema::VARCHAR, l_table::VARCHAR, num_partitions::VARCHAR, n_num_partitions::VARCHAR);
	END IF;
--
	ddl_stmt:=NULL;
--
-- Cluster partitions
-- 
	i:=1;
	IF c4_rec.table_name IS NOT NULL AND c3_rec.index_name IS NOT NULL THEN
		FOR c5_rec IN c5gangep(l_schema, l_table) LOOP
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'Cluster: %.%', 
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
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_range_partition', 
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
                PERFORM rif40_log_pkg.rif40_error(-19005, 'rif40_range_partition',
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
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='DROP TABLE rif40_range_partition /* Temporary table */';

--
-- Run 2
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);

	IF c4_rec.table_name IS NOT NULL AND c3_rec.index_name IS NOT NULL THEN
		RAISE plpgsql_error;
	END IF;
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_range_partition(VARCHAR,VARCHAR, VARCHAR) IS 'Function: 	rif40_range_partition()
Parameters:	Schema, table, column
Returns:	Nothing
Description:	Automatic range partition schema.table on column

* Must be rif40 or have rif_user or rif_manager role
* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)
* Do not partition if table has only one distinct row
* Do not partition if table has no rows
* Create auto range trigger function
* Add trigger to existing table
* Bring data back, order by range partition, primary key
* If table is a numerator, cluster
* Check number of rows match original, truncate rif40_range_partition temporary table
* Re-anaylse';

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_range_partition_create(
	l_schema 	VARCHAR, 
	master_table 	VARCHAR, 
	partition_table VARCHAR, 
	l_column	VARCHAR, 
	l_value		VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_range_partition_create()
Parameters:	Schema, master table, partition table, column, value
Returns:	Nothing
Description:	Create range partition schema.table_<value> on column <column> value <value>, inheriting from <mnaster table>.
		Comment columns

Runs as RIF40 (so can create partitiol_tablens)

Generates the following SQL to create a partition>
	
CREATE TABLE sahsuland_cancer_1989 (
 CONSTRAINT sahsuland_cancer_1989_ck CHECK (year::text = '1989'::text)
) INHERITS (sahsuland_cancer);
CREATE INDEX sahsuland_cancer_1989_age_sex_group ON sahsuland_cancer_1989 USING btree (age_sex_group);
CREATE INDEX sahsuland_cancer_1989_icd ON sahsuland_cancer_1989 USING btree (icd);
CREATE INDEX sahsuland_cancer_1989_level1 ON sahsuland_cancer_1989 USING btree (level1);
CREATE INDEX sahsuland_cancer_1989_level2 ON sahsuland_cancer_1989 USING btree (level2);
CREATE INDEX sahsuland_cancer_1989_level3 ON sahsuland_cancer_1989 USING btree (level3);
CREATE INDEX sahsuland_cancer_1989_level4 ON sahsuland_cancer_1989 USING btree (level4);
CREATE UNIQUE INDEX sahsuland_cancer_1989_pk ON sahsuland_cancer_1989 USING btree (year, level4, age_sex_group, icd);
COMMENT ON TABLE sahsuland_cancer_1989 IS 'Range partition: sahsuland_cancer_1989 for value 1989 on column: year; master: rif40.sahsuland_cancer';
COMMENT ON COLUMN sahsuland_cancer_1989.age_sex_group IS 'Age sex group';
COMMENT ON COLUMN sahsuland_cancer_1989.icd IS 'ICD';
COMMENT ON COLUMN sahsuland_cancer_1989.level1 IS 'level1';
COMMENT ON COLUMN sahsuland_cancer_1989.level2 IS 'level2';
COMMENT ON COLUMN sahsuland_cancer_1989.level3 IS 'level3';
COMMENT ON COLUMN sahsuland_cancer_1989.level4 IS 'level4';
COMMENT ON COLUMN sahsuland_cancer_1989.total IS 'Total';
COMMENT ON COLUMN sahsuland_cancer_1989.year IS 'Year';

Then runs the following SQL to create the partition INSERT function>

 */
DECLARE
	c1rpcr CURSOR(l_schema VARCHAR, l_master_table VARCHAR) FOR /* Column comment */
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
	c3rpcr CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR
		SELECT *
	          FROM information_schema.columns
	         WHERE table_schema = l_schema
	           AND table_name   = l_table
	         ORDER BY ordinal_position;
	c4rpcr CURSOR(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR) FOR /* GET PK/unique index column */
		SELECT n.nspname AS schema_name, 
		       t.relname AS table_name, 
		       i.relname AS index_name, 
		       array_to_string(array_agg(a.attname), ', ') AS column_names, 
		       pg_get_indexdef(i.oid) AS index_def,
		       CASE WHEN ix.indisprimary THEN pg_get_constraintdef(i.oid) ELSE NULL END AS constraint_def,
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
		   AND con.contype IN  ('x', 'c', 't', 'u') /* trigger, unique, check and exclusion constraints */;

--
	c1_rec 		RECORD;
	c2_rec 		RECORD;
	c3_rec 		RECORD;
	c4_rec 		RECORD;
	c5_rec 		RECORD;
	c6_rec 		RECORD;
--
	ddl_stmt	VARCHAR[];
	rec_list 	VARCHAR;
--
	i		INTEGER:=0;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_range_partition_create', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_range_partition_create', 
		'Create range partition: % for value % on column: %; master: %.%', 
		partition_table::VARCHAR	/* Partition table */,
		l_value::VARCHAR		/* Partition range value */,
		l_column::VARCHAR		/* Partition column */,
		l_schema::VARCHAR		/* Schema */, 
		master_table::VARCHAR		/* Master table inheriting from */);

--
-- Create partition table inheriting from master
--
	ddl_stmt[1]:='CREATE TABLE '||quote_ident(partition_table)||' ('||E'\n'||
		' CONSTRAINT '||quote_ident(partition_table||'_ck')||' CHECK ('||quote_ident(l_column)||'::text = '''||l_value||'''::text)'||E'\n'||
		') INHERITS ('||quote_ident(master_table)||')';

--
-- Add indexes, primary key
--
	FOR c4_rec IN c4rpcr(l_schema, master_table, l_column) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create', 'Index[%] % on: %.%(%); PK: %, Unique: %', 
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
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create', 'Added % indexes to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_range_partition_create', 'Added no indexes to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	END IF;

--
-- Add foreign keys
--
	i:=0;
	FOR c5_rec IN c5rpcr(l_schema, master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create', 'FK Constraint[%] % on: %.%(%)', 
			i::VARCHAR,
			c5_rec.conname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR, 
			c5_rec.child_column::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c5_rec.constraint_def, master_table, partition_table);
	END LOOP;
	IF i > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create', 'Added % foreign keys to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create', 'Added no foreign keys to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	END IF;

--
-- Add trigger, unique, check and exclusion constraints
--
	i:=0;
	FOR c6_rec IN c6rpcr(l_schema, master_table) LOOP
		I:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create', '% constraint[%] % on: %.%(%)', 
			c6_rec.constraint_type::VARCHAR,
			i::VARCHAR,
			c6_rec.conname::VARCHAR, 
			l_schema::VARCHAR, 
			partition_table::VARCHAR, 
			c6_rec.child_column::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:=REPLACE(c6_rec.constraint_def, master_table, partition_table);
	END LOOP;
	IF i > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create', 'Added % trigger, unique, check and exclusion constraints to partition: %.%', 
			i::VARCHAR,
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create', 'Added no trigger, unique, check and exclusion constraints to partition: %.%', 
			l_schema::VARCHAR, 
			partition_table::VARCHAR);
	END IF;

--
-- Validation triggers
--
	
--
-- Add grants
--

--
-- Comments
--
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON TABLE '||quote_ident(partition_table)||
		' IS ''Range partition: '||partition_table||' for value '||l_value||' on column: '||l_column||'; master: '||l_schema||'.'||master_table||'''';
	FOR c1_rec IN c1rpcr(l_schema, master_table) LOOP
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON COLUMN '||quote_ident(partition_table)||'.'||c1_rec.column_name||
		' IS '''||c1_rec.description||'''';
	END LOOP;
--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_range_partition_create(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	_rif40_range_partition_create()
Parameters:	Schema, master table, partition table, column, value
Returns:	Nothing
Description:	Create range partition schema.table_<value> on column <column> value <value>, inheriting from <mnaster table>.
		Comment columns

Generates the following SQL>
	
CREATE TABLE sahsuland_cancer_1989 (
 CONSTRAINT sahsuland_cancer_1989_ck CHECK (year::text = ''1989''::text)
) INHERITS (sahsuland_cancer);
CREATE INDEX sahsuland_cancer_1989_age_sex_group ON sahsuland_cancer_1989 USING btree (age_sex_group);
CREATE INDEX sahsuland_cancer_1989_icd ON sahsuland_cancer_1989 USING btree (icd);
CREATE INDEX sahsuland_cancer_1989_level1 ON sahsuland_cancer_1989 USING btree (level1);
CREATE INDEX sahsuland_cancer_1989_level2 ON sahsuland_cancer_1989 USING btree (level2);
CREATE INDEX sahsuland_cancer_1989_level3 ON sahsuland_cancer_1989 USING btree (level3);
CREATE INDEX sahsuland_cancer_1989_level4 ON sahsuland_cancer_1989 USING btree (level4);
CREATE UNIQUE INDEX sahsuland_cancer_1989_pk ON sahsuland_cancer_1989 USING btree (year, level4, age_sex_group, icd);
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
