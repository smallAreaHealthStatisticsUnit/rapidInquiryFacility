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

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_range_partition_create_insert(VARCHAR, VARCHAR, VARCHAR, VARCHAR, INTEGER,
	OUT VARCHAR[], OUT VARCHAR);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_range_partition_create_insert(VARCHAR, VARCHAR, VARCHAR, INTEGER,
	OUT VARCHAR[], OUT VARCHAR);
	
CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_range_partition_create_insert(partitions_schema VARCHAR, data_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, total_rows INTEGER,
	OUT ddl_stmt VARCHAR[], OUT index_name VARCHAR)
RETURNS RECORD
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_range_partition_create_insert()
Parameters:	Partitions schema, data schema, table, column, total rows
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
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_range_partition_create_insert', 
			'User % must be rif40 or have rif_user or rif_manager role', 
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
	l_ddl_stmt:=rif40_sql_pkg._rif40_common_partition_triggers(partitions_schema, l_table, l_column, 'DISABLE'::VARCHAR);
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
	OPEN c3gangep(data_schema, l_table, l_column);
	FETCH c3gangep INTO c3_rec;
	CLOSE c3gangep;
	index_name:=c3_rec.index_name;
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create_insert', 'Restore data from temporary table: %', 
		l_table::VARCHAR);
--
-- Create list of potential partitions
--
	IF total_rows > 0 THEN
		BEGIN
			sql_stmt:='SELECT '||quote_ident(l_column)||' AS partition_value, COUNT('||quote_ident(l_column)||') AS total_rows'||E'\n'||
				'  FROM '||quote_ident(data_schema)||'.'||quote_ident(l_table)||E'\n'||
				' GROUP BY '||quote_ident(l_column)||E'\n'||
				' ORDER BY 1'; 
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_range_partition_create_insert', 'SQL> %;', sql_stmt::VARCHAR);
			FOR c6_rec IN EXECUTE sql_stmt LOOP
--
				IF ddl_stmt IS NULL THEN
					ddl_stmt[1]:='INSERT INTO '||quote_ident(data_schema)||'.'||quote_ident(l_table)||
						' /* Create partition '||c6_rec.partition_value||' */'||E'\n'||
						'SELECT * FROM rif40_auto_partition /* Temporary table */'||E'\n'||
						' WHERE '||l_column||' = '''||c6_rec.partition_value||''''||E'\n'||
						' LIMIT 1';
				ELSE
					ddl_stmt[array_length(ddl_stmt, 1)+1]:='INSERT INTO '||quote_ident(data_schema)||'.'||quote_ident(l_table)||
						' /* Create partition '||c6_rec.partition_value||' */'||E'\n'||
						'SELECT * FROM rif40_auto_partition /* Temporary table */'||E'\n'||
						' WHERE '||l_column||' = '''||c6_rec.partition_value||''''||E'\n'||
						' LIMIT 1';
				END IF;
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='TRUNCATE TABLE '||
					quote_ident(partitions_schema)||'.'||quote_ident('p_'||l_table||'_'||c6_rec.partition_value)||
					' /* Empty newly created partition '||c6_rec.partition_value||' */';
--				
-- Bring data back, order by range partition, primary key
--
				IF c3_rec.column_names IS NOT NULL THEN
					ddl_stmt[array_length(ddl_stmt, 1)+1]:='INSERT INTO '||quote_ident(partitions_schema)||'.'||quote_ident('p_'||l_table||'_'||c6_rec.partition_value)||
						' /* Directly populate partition: '||c6_rec.partition_value||
						', total rows expected: '||c6_rec.total_rows||' */'||E'\n'||
						'SELECT * FROM rif40_auto_partition /* Temporary table */'||E'\n'||
						' WHERE '||l_column||' = '''||c6_rec.partition_value||''''||E'\n'||
						' ORDER BY '||l_column||' /* Partition column */, '||
						c3_rec.column_names||' /* [Rest of ] primary key */';
				ELSE
					ddl_stmt[array_length(ddl_stmt, 1)+1]:='INSERT INTO '||quote_ident(partitions_schema)||'.'||quote_ident('p_'||l_table||'_'||c6_rec.partition_value)||
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
				error_message:='_rif40_range_partition_create_insert() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '2: %', error_message;
--
				RAISE;
		END;
	END IF;
--
-- Re-enable ON-INSERT triggers
--
	l_ddl_stmt:=rif40_sql_pkg._rif40_common_partition_triggers(partitions_schema, l_table, l_column, 'ENABLE'::VARCHAR);
	IF l_ddl_stmt IS NOT NULL THEN
		FOR i IN 1 .. array_length(l_ddl_stmt, 1) LOOP
			ddl_stmt[array_length(ddl_stmt, 1)+1]:=l_ddl_stmt[i];
		END LOOP;
	END IF;

--
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_range_partition_create_insert(VARCHAR, VARCHAR, VARCHAR, VARCHAR, INTEGER,
	OUT VARCHAR[], OUT VARCHAR) IS 'Function: 	_rif40_range_partition_create_insert()
Parameters:	Partitions schema, data schema, table, column, total rows
                [OUT] ddl statement array, [PK/UK] index name
Returns:	OUT parameters as a record
 		DDL statement array is NULL if the function is unable to partition
Description:	Automatic range/hash partition schema.table on column
		INSERT

* Foreach partition:
Call: _rif40_range_partition_create_insert()

* Foreach partition:
+	INSERT 1 rows. This creates the partition
+	TRUNCATE partition
+ 	Bring data back by partition, order by range partition, primary key
[End of _rif40_range_partition_create_insert()]';

--
-- Eof