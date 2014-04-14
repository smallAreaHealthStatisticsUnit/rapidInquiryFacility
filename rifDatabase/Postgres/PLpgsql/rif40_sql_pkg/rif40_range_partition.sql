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
* Create auto range trigger functionm

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
        sql_stmt:='INSERT INTO '||p_table||' VALUES (NEW.*) -* Partition: '||p_value||' -/';
        BEGIN
                PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
        EXCEPTION
                WHEN undefined_table /- e.g. 42p01: relation "rif40.rif40_population_europe_1991" does not exist -/ THEN
                        PERFORM rif40_log_pkg.rif40_log('INFO', 'sahsuland_cancer_insert',
                                'Adding partition for year: % to table: %',
                                NEW.year::VARCHAR, 'sahsuland_cancer'::VARCHAR);
--
-- Add partition
--
                       PERFORM rif40_sql_pkg._rif40_range_partition_create('rif40', 'sahsuland_cancer', p_table, 'year', p_value);
                WHEN others THEN
                        GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
                        error_message:='sahsuland_cancer_insert() caught: ||SQLSTATE::VARCHAR'||E'\n'||SQLERRM::VARCHAR||' in SQL (s
ee previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR||' ';
                        RAISE INFO '3: %', error_message;
--
                        RAISE;
        END;
-- '||quote_ident(l_schema)||'.'||quote_ident(l_table)
        RETURN NULL;
END;
$BODY$
  LANGUAGE plpgsql;
COMMENT ON FUNCTION  rif40.sahsuland_cancer_insert() IS 'Partition INSERT function for geography: SAHSU';

* Add trigger to existing table

CREATE TRIGGER sahsuland_cancer_insert
  BEFORE INSERT OR UPDATE ON rif40.sahsuland_cancer
  FOR EACH ROW
  EXECUTE PROCEDURE sahsuland_cancer_insert();
COMMENT ON TRIGGER sahsuland_cancer_insert
 ON rif40.sahsuland_cancer
 IS 'Partition INSERT trigger by year for: rif40.sahsuland_cancer; calls sahsuland_cancer_insert(). Automatically creates partitions';

* Bring data back, order by range partition, primary key

INSERT INTO sahsuland_cancer
 SELECT * FROM rif40_range_partition -* Temporary table *- ORDER BY year;
INSERT INTO sahsuland_cancer_1989 VALUES (NEW.*) -* Partition: 1989 *-;

* If table is a numerator, cluster

* Re-anaylse

 */
DECLARE
 	c1gangep 		REFCURSOR;

	sql_stmt 		VARCHAR;
	ddl_stmt 		VARCHAR[];
	num_partitions		INTEGER;
	l_rows			INTEGER:=0;
	table_length		INTEGER:=0;
	i			INTEGER:=0;
	name_length_limit	INTEGER:=30;	/* You may want to set this higher */
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
		sql_stmt:='SELECT COUNT(DISTINCT('||quote_ident(l_column)||')) AS total FROM '||quote_ident(l_schema)||'.'||quote_ident(l_table)||' LIMIT 1'; 
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'SQL> %;', sql_stmt::VARCHAR);
		OPEN c1gangep FOR EXECUTE sql_stmt;
		FETCH c1gangep INTO num_partitions;
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
	IF table_length > 25 THEN
		PERFORM rif40_log_pkg.rif40_error(-20997, 'rif40_range_partition', 
			'Automatic range partitioning by %: %.%; table name is too long %,limit is 25', 
			l_column::VARCHAR, l_schema::VARCHAR, l_table::VARCHAR, table_length::VARCHAR);
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
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'Copy data to temporary table: %.%', 
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
'	sql_stmt:=''INSERT INTO ''||p_table||'' VALUES (NEW.*) /* Partition: ''||p_value||'' */'';'||E'\n'||
'	BEGIN'||E'\n'||
'		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);'||E'\n'||
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
'-- Re-insert failed row'||E'\n'||
'--'||E'\n'||
'--			sql_stmt:=''INSERT INTO ''||p_table||'' VALUES (NEW.*) /* Partition: ''||p_value||'' Attempt: 2 */'';'||E'\n'||
'			sql_stmt:=''SELECT ''||p_table||''_ins(); /* Partition: ''||p_value||'' Attempt: 2 */'';'||E'\n'||
'			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);'||E'\n'||
'			RETURN NEW;'||E'\n'||
'		WHEN others THEN'||E'\n'||
'			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;'||E'\n'||
'			error_message:='''||quote_ident(l_table||'_insert')||'() caught: ||SQLSTATE::VARCHAR''||E''\n''||SQLERRM::VARCHAR||'' in SQL (see previous trapped error)''||E''\n''||''Detail: ''||''(''||SQLSTATE||'') ''||v_detail::VARCHAR||'' '';'||E'\n'|| 
'			RAISE INFO ''3: %'', error_message;'||E'\n'||
'--'||E'\n'||
'			RAISE;'||E'\n'||
'	END;'||E'\n'||
'--'||E'\n'||
'	RETURN NEW;'||E'\n'||
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
'  BEFORE INSERT OR UPDATE ON '||quote_ident(l_schema)||'.'||quote_ident(l_table)||E'\n'||
'  FOR EACH ROW'||E'\n'||
'  EXECUTE PROCEDURE '||quote_ident(l_table||'_insert')||'()';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON TRIGGER '||quote_ident(l_table||'_insert')||E'\n'||
		' ON '||quote_ident(l_schema)||'.'||quote_ident(l_table)||E'\n'||
		' IS ''Partition INSERT trigger by '||quote_ident(l_column)||' for: '||quote_ident(l_schema)||'.'||quote_ident(l_table)||
		'; calls '||quote_ident(l_table||'_insert')||'(). Automatically creates partitions''';
-- 
-- Bring data back, order by range partition, primary key
--
	IF l_rows > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_range_partition', 'Restore data from temporary table: %.%', 
			l_schema::VARCHAR, l_table::VARCHAR);
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='INSERT INTO '||quote_ident(l_table)||E'\n'||
			' SELECT * FROM rif40_range_partition /* Temporary table */ ORDER BY year';
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='DROP TABLE rif40_range_partition /* Temporary table */';
	END IF;

--
-- If table is a numerator, cluster
--

--
-- Re-anaylse
--
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='ANALYZE '||quote_ident(l_schema)||'.'||quote_ident(l_table);

--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);

END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_range_partition(VARCHAR,VARCHAR, VARCHAR) IS '';

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

Runs as RIF40 (so can create partitions)

Generates the following SQL to create a partition>
	
CREATE TABLE sahsuland_cancer_1989 (
 CONSTRAINT sahsuland_cancer_1989_ck CHECK (year::text = '1989'::text)
) INHERITS (sahsuland_cancer);
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
	c1_rec RECORD;
--
	ddl_stmt	VARCHAR[];
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
-- Comments
--
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON TABLE '||quote_ident(partition_table)||
		' IS ''Range partition: '||partition_table||' for value '||l_value||' on column: '||l_column||'; master: '||l_schema||'.'||master_table||'''';
	FOR c1_rec IN c1rpcr(l_schema, master_table) LOOP
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON COLUMN '||quote_ident(partition_table)||'.'||c1_rec.column_name||
		' IS '''||c1_rec.description||'''';
	END LOOP;
--
--  Create the partition INSERT function
--
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='CREATE OR REPLACE FUNCTION  '||quote_ident(l_schema)||'.'||quote_ident(partition_table||'_ins')||'()'||E'\n'||
'  RETURNS trigger AS'||E'\n'||
'$BODY$'||E'\n'||
'DECLARE'||E'\n'||
'BEGIN'||E'\n'||
'	INSERT INTO '||quote_ident(l_schema)||'.'||quote_ident(partition_table)||' VALUES (NEW.*);'||E'\n'||
'	RETURN NEW;'||E'\n'||
'END;'||E'\n'||
'$BODY$'||E'\n'||
'LANGUAGE plpgsql;';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON FUNCTION '||quote_ident(l_schema)||'.'||quote_ident(partition_table||'_ins')||'() IS ''INSERT function for partition when called from master table INSERT trigger function''';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='GRANT EXECUTE ON FUNCTION '||quote_ident(l_schema)||'.'||quote_ident(partition_table||'_ins')||'() TO rif_user';
	ddl_stmt[array_length(ddl_stmt, 1)+1]:='GRANT EXECUTE ON FUNCTION '||quote_ident(l_schema)||'.'||quote_ident(partition_table||'_ins')||'() TO rif_manager';
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
