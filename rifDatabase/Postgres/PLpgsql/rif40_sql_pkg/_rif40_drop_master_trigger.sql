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

-- 
-- Eof