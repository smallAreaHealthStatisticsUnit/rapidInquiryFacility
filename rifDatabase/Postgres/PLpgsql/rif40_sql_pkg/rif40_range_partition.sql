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
-- Rapid Enquiry Facility (RIF) - Auto range partitioning functions
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

DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_range_partition(VARCHAR,VARCHAR, VARCHAR, VARCHAR[]);
-- Old
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_range_partition(VARCHAR,VARCHAR, VARCHAR);

--
-- Error code:
--
--
-- rif40_range_partition:								71450 to 71499
--

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_range_partition(
	l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, l_table_list VARCHAR[])
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_range_partition()
Parameters:	Schema, table, column, list of tables in current partition build
Returns:	Nothing
Description:	Automatic range partition schema.table on column

Call: _rif40_common_partition_create_setup()
 
* Must be rif40 or have rif_user or rif_manager role
* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)

CREATE TEMPORARY TABLE rif40_range_partition AS SELECT * FROM rif40.sahsuland_cancer;
TRUNCATE TABLE rif40.sahsuland_cancer;

* Do not partition if table has only one distinct row
* Do not partition if table has no rows

[End of _rif40_common_partition_create_setup()]

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
                       PERFORM rif40_sql_pkg._rif40_range_partition_create('rif40', 'sahsuland_cancer', p_table, 'year', p_value, NULL::VACHAR[]);
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

* If debug is enabled add newly created function 

SELECT rif40_log_pkg.rif40_add_to_debug('sahsuland_cancer_insert:DEBUG1');

* Add trigger to existing table

CREATE TRIGGER sahsuland_cancer_insert
  BEFORE INSERT ON rif40.sahsuland_cancer
  FOR EACH ROW
  EXECUTE PROCEDURE sahsuland_cancer_insert();
COMMENT ON TRIGGER sahsuland_cancer_insert
 ON rif40.sahsuland_cancer
 IS 'Partition INSERT trigger by year for: rif40.sahsuland_cancer; calls sahsuland_cancer_insert(). Automatically creates partitions';

Call: _rif40_range_partition_create_insert()

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

[End of _rif40_range_partition_create_insert()]

* Check number of rows match original, truncate rif40_range_partition temporary table

SELECT COUNT(DISTINCT(year)) AS num_partitions, COUNT(year) AS total_rows FROM rif40.sahsuland_cancer LIMIT 1;

Call: _rif40_common_partition_create_complete()

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

[End of _rif40_common_partition_create_complete()]

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
--
	create_setup 		RECORD;
	create_insert 		RECORD;
	sql_stmt 		VARCHAR;
	rec_list		VARCHAR;
	bind_list		VARCHAR;
	ddl_stmt 		VARCHAR[];
	fk_stmt 		VARCHAR[];
	num_partitions		INTEGER;
	min_value		VARCHAR;
	total_rows		INTEGER;
	n_num_partitions	INTEGER;
	n_total_rows		INTEGER;
	i			INTEGER:=0;
	warnings		INTEGER:=0;
	total_partitions	INTEGER;
--
	p_schema	VARCHAR:='rif40_partitions';
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-71450, 'rif40_range_partition', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
-- Check if table is already partitioned
--
	total_partitions:=rif40_sql_pkg._rif40_partition_count(l_schema, l_table);
	IF total_partitions >= 1 THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_range_partition', 
			'[71451] Automatic range partition by %: %.%; table name is already partitioned into: % partitions', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, total_partitions::VARCHAR);
	END IF;

--
-- Call: _rif40_common_partition_create_setup()
--	
	create_setup:=rif40_sql_pkg._rif40_common_partition_create_setup(
		l_schema, 			/* Schema of source data (rif_data usually) */
		p_schema, 			/* partition schema */
		l_table, 			/* Master table name */
		l_column, 			/* Column name */
		l_table_list,		/* list of tables in current partition build */
		NULL::INTEGER		/* Not a hash partition */);
	IF create_setup.ddl_stmt IS NULL THEN /* Un partitionable */
		RETURN;
	END IF;
--
-- Copy out parameters
--
	ddl_stmt:=create_setup.ddl_stmt;
	fk_stmt:=create_setup.fk_stmt;
	num_partitions:=create_setup.num_partitions;
	min_value:=create_setup.min_value;
	warnings:=create_setup.warnings;
	total_rows:=create_setup.total_rows;

--
-- Foriegn key constrsints are not supported by the called scripts - it is an error if there are any
--
	IF create_setup.fk_stmt IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71452, 'rif40_range_partition', 
			'Automatic range partitioning setup %: %.%;  rows: %; partitions: %; warnings: %', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, 
			total_rows::VARCHAR, num_partitions::VARCHAR, warnings::VARCHAR);	
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 
			'[71453] Automatic range partitioning setup %: %.%;  rows: %; partitions: %; warnings: %', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, 
			total_rows::VARCHAR, num_partitions::VARCHAR, warnings::VARCHAR);	
	END IF;
--
-- Create auto range trigger function
--
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='CREATE OR REPLACE FUNCTION '||quote_ident(l_schema)||'.'||quote_ident(l_table||'_insert')||'()'||E'\n'||
'  RETURNS trigger AS'||E'\n'||
'$BODY$'||E'\n'||
'DECLARE'||E'\n'||
'	sql_stmt 	VARCHAR;'||E'\n'||
'	p_schema	VARCHAR:='''||p_schema||''';'||E'\n'||
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
'	p_table:=quote_ident(''p_'||l_table||'_''||NEW.'||l_column||'::Text'||');'||E'\n'||
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
'		sql_stmt:= ''INSERT INTO ''||p_schema||''.''||p_table||'' VALUES ('||bind_list||') /* Partition: ''||p_value||'' */'';'||E'\n'||
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
'                       PERFORM rif40_sql_pkg._rif40_range_partition_create(p_schema, '''||l_table||''', p_table, '''||l_column||''', p_value, NULL::VARCHAR[] /* table list being created */);'||E'\n'||
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
--
-- If debug is enabled add newly created function 
--
	IF rif40_log_pkg.rif40_is_debug_enabled('rif40_range_partition', 'DEBUG1') THEN
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
-- Call: _rif40_range_partition_create_insert()
--
	create_insert:=rif40_sql_pkg._rif40_range_partition_create_insert(p_schema /* partitions schema */, l_schema /* Source table schema */, 
		l_table, l_column, total_rows);
	IF create_insert.ddl_stmt IS NULL THEN /* Un partitionable */
		RETURN;
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
-- Put back foreign keys
--
	IF fk_stmt IS NOT NULL THEN
		PERFORM rif40_sql_pkg.rif40_ddl(fk_stmt);
	END IF;

--
-- Check number of rows match original
--
	sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(l_column)||')) AS num_partitions, COUNT('||quote_ident(l_column)||') AS total_rows'||E'\n'||
		'FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||' LIMIT 1'; 
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'SQL> %;', sql_stmt::VARCHAR);
	OPEN c1gangep FOR EXECUTE sql_stmt;
	FETCH c1gangep INTO n_num_partitions, n_total_rows;
	CLOSE c1gangep;
--
	IF num_partitions = n_num_partitions AND total_rows = n_total_rows THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'Partition of: %.% created % partitions, % rows total OK', 
			l_schema::VARCHAR, l_table::VARCHAR, num_partitions::VARCHAR, total_rows::VARCHAR);
	ELSIF total_rows != n_total_rows THEN
		PERFORM rif40_log_pkg.rif40_error(-20190, 'rif40_range_partition', 'Partition of: %.% rows mismatch: expected: %, got % rows total', 
			l_schema::VARCHAR, l_table::VARCHAR, total_rows::VARCHAR, n_total_rows::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-20191, 'rif40_range_partition', 'Partition of: %.% partition mismatch: expected: %, got % partition total', 
			l_schema::VARCHAR, l_table::VARCHAR, num_partitions::VARCHAR, n_num_partitions::VARCHAR);
	END IF;

--
-- Call: _rif40_common_partition_create_complete()
--
	PERFORM rif40_sql_pkg._rif40_common_partition_create_complete(l_schema, l_table, l_column, create_insert.index_name);

--
-- Test partition exclusion. Works. Note any cast will break it!!!
--
	sql_stmt:='SELECT *'||E'\n'||
		'  FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||E'\n'||
		' WHERE '||quote_ident(l_column)||' = '||min_value||E'\n'||
	        ' ORDER BY 1 LIMIT 10'; 
	PERFORM rif40_sql_pkg.rif40_method4(sql_stmt, 'Partition EXPLAIN test 2');
/*
psql:../psql_scripts/v4_0_year_partitions.sql:157: INFO:  [DEBUG1] rif40_ddl(): EXPLAIN SQL> EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE l_3448_2456814_53253_979000 AS
SELECT *
  FROM rif40.sahsuland_pop
 WHERE year = 1989
 ORDER BY 1 LIMIT 10;
psql:../psql_scripts/v4_0_year_partitions.sql:157: INFO:  [DEBUG1] rif40_ddl(): Limit  (cost=0.00..0.23 rows=10 width=52) (actual time=0.013..0.024 rows=10 loops=1)
  Output: sahsuland_pop.year, sahsuland_pop.age_sex_group, sahsuland_pop.level1, sahsuland_pop.level2, sahsuland_pop.level3, sahsuland_pop.level4, sahsuland_pop.total
  ->  Append  (cost=0.00..1234.50 rows=54121 width=52) (actual time=0.012..0.019 rows=10 loops=1)
        ->  Seq Scan on rif40.sahsuland_pop  (cost=0.00..0.00 rows=1 width=52) (actual time=0.001..0.001 rows=0 loops=1)
              Output: sahsuland_pop.year, sahsuland_pop.age_sex_group, sahsuland_pop.level1, sahsuland_pop.level2, sahsuland_pop.level3, sahsuland_pop.level4, sahsuland_pop.total
              Filter: (sahsuland_pop.year = 1989)
        ->  Seq Scan on rif40.sahsuland_pop_1989  (cost=0.00..1234.50 rows=54120 width=52) (actual time=0.010..0.014 rows=10 loops=1)
              Output: sahsuland_pop_1989.year, sahsuland_pop_1989.age_sex_group, sahsuland_pop_1989.level1, sahsuland_pop_1989.level2, sahsuland_pop_1989.level3, sahsuland_pop_1989
.level4, sahsuland_pop_1989.total
              Filter: (sahsuland_pop_1989.year = 1989)
Total runtime: 1.521 ms
psql:../psql_scripts/v4_0_year_partitions.sql:157: INFO:  rif40_method4():
Partition EXPLAIN test 2
------------------------
psql:../psql_scripts/v4_0_year_partitions.sql:157: INFO:  rif40_method4():
year  | age_sex_group | level1               | level2               | level3               | level4               | total
--------------------------------------------------------------------------------------------------------------------------------------------
1989  | 100           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 34
1989  | 101           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 34
1989  | 102           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 34
1989  | 103           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 34
1989  | 104           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 34
1989  | 105           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 134
1989  | 106           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 112
1989  | 107           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 334
1989  | 108           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 828
1989  | 109           | 01                   | 01.001               | 01.001.000100        | 01.001.000100.1      | 702
(10 rows)
 */

--
-- Used to halt alter_1.sql for testing
--
--	RAISE plpgsql_error;
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_range_partition(VARCHAR,VARCHAR, VARCHAR, VARCHAR[]) IS 'Function: 	rif40_range_partition()
Parameters:	Schema, table, column, list of tables in current partition build
Returns:	Nothing
Description:	Automatic range partition schema.table on column

* Must be rif40 or have rif_user or rif_manager role

Call: _rif40_common_partition_create_setup()

* Check if table is valid
* Check table name length - must be 25 chars or less (assuming the limit is 30)
* Check data is partitionable 
* Copy to temp table, truncate (dont panic - Postgres DDL is part of a transaction)
* Do not partition if table has only one distinct row
* Do not partition if table has no rows
[End of _rif40_common_partition_create_setup()]

* Create auto range trigger function
* Add trigger to existing table

Call: _rif40_range_partition_create_insert()

* Foreach partition:
+	INSERT 1 rows. This creates the partition
+	TRUNCATE partition
+ 	Bring data back by partition, order by range partition, primary key
[End of _rif40_range_partition_create_insert()]

* Check number of rows match original, truncate rif40_range_partition temporary table

Call: _rif40_common_partition_create_complete()
* If table is a numerator, cluster each partition (not the master table)
* Re-anaylse
[End of _rif40_common_partition_create_complete()]';

--
-- Eof
