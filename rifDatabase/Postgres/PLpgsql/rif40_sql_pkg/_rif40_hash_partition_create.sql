CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_hash_partition_create(
	l_schema 	VARCHAR, 
	master_table 	VARCHAR, 
	partition_table VARCHAR, 
	l_column	VARCHAR, 
	l_value		INTEGER,
	num_partitions	INTEGER)
RETURNS void
SECURITY DEFINER
AS $func$
/*
Function: 	_rif40_hash_partition_create()
Parameters:	Schema, master table, partition table, column, hash value, total partitions
Returns:	Nothing
Description:	Create hash partition schema.table_<value> on column <column> value <value>, inheriting from <mnaster table>.
		Comment columns

Runs as RIF40 (so can create partition tables)

Generates the following SQL to create a partition>
	
CREATE TABLE rif40_study_shares_p15 (
 CONSTRAINT rif40_study_shares_p15_ck CHECK (hash_partition_number = 15 /- bucket requested -/)
) INHERITS (rif40_study_shares);

Call rif40_sql_pkg._rif40_common_partition_create to:

* Add indexes, primary key
* Add foreign keys
* Add trigger, unique, check and exclusion constraints
* Validation triggers
* Add grants
* Table and column comments

 */
DECLARE
	ddl_stmt VARCHAR[];
--
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF USER != 'rif40' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_hash_partition_create', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_hash_partition_create', 
		'Create hash partition: % for hash value % (of %) on column: %; master: %.%', 
		partition_table::VARCHAR	/* Partition table */,
		l_value::VARCHAR		/* Partition hash value */,
		num_partitions::VARCHAR		/* Total partitions */,
		l_column::VARCHAR		/* Partition column */,
		l_schema::VARCHAR		/* Schema */, 
		master_table::VARCHAR		/* Master table inheriting from */);

--
-- Create partition table inheriting from master
--
--	IF l_value ~ '^[0-9]*.?[0-9]*$' THEN /* isnumeric */	
-- May need type specific _rif40_hash_bucket_check functions to avoid implicit cast which may break the equality checks in partition elimination
--
	ddl_stmt[1]:='CREATE TABLE '||quote_ident(l_schema)||'.'||quote_ident(partition_table)||' ('||E'\n'||
--
-- These don't work as the Postgres parser only supports partition range elimination by value
--
--		' CONSTRAINT '||quote_ident(partition_table||'_ck')||' CHECK ('''||l_value||''' = rif40_sql_pkg._rif40_hash('||quote_ident(l_column)||'::VARCHAR, '||num_partitions::Text||')::VARCHAR)'||E'\n'||
--		' CONSTRAINT '||quote_ident(partition_table||'_ck')||' CHECK ('||l_column||' = rif40_sql_pkg._rif40_hash_bucket_check('||quote_ident(l_column)||', '||num_partitions||' /* total buckets */, '||l_value||' /* bucket requested */))'||E'\n'||
		' CONSTRAINT '||quote_ident(partition_table||'_ck')||' CHECK (hash_partition_number = '||l_value||' /* bucket requested */)'||E'\n'||
		') INHERITS ('||quote_ident(master_table)||')';
--
-- Run
--
	PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);
	ddl_stmt:=NULL;
--
-- Call rif40_sql_pkg._rif40_common_partition_create to:
-- * Add indexes, primary key
-- * Add foreign keys
-- * Add trigger, unique, check and exclusion constraints
-- * Validation triggers
-- * Add grants
-- * Table and column comments
--
	PERFORM rif40_sql_pkg._rif40_common_partition_create(l_schema, master_table, partition_table, l_column, l_value::VARCHAR);

END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_hash_partition_create(VARCHAR, VARCHAR, VARCHAR, VARCHAR, INTEGER, INTEGER) IS 'Function: 	_rif40_hash_partition_create()
Parameters:	Schema, master table, partition table, column, hash value, number of partitions
Returns:	Nothing
Description:	Create hash partition schema.table_<value> on column <column> value <value>, inheriting from <mnaster table>.
		Comment columns

Runs as RIF40 (so can create partition tables)

Generates the following SQL to create a partition>
	
CREATE TABLE rif40_study_shares_p15 (
 CONSTRAINT rif40_study_shares_p15_ck CHECK (hash_partition_number = 15 /* bucket requested */)
) INHERITS (rif40_study_shares);

Call rif40_sql_pkg._rif40_common_partition_create to:

* Add indexes, primary key
* Add foreign keys
* Add trigger, unique, check and exclusion constraints
* Validation triggers
* Add grants
* Table and column comments';

--
-- Eof