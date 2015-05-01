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

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_common_partition_triggers(master_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, 
	enable_or_disable VARCHAR, partition_schema VARCHAR, OUT ddl_stmt VARCHAR[]);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_common_partition_triggers(master_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, 
	enable_or_disable VARCHAR, OUT ddl_stmt VARCHAR[]);
	
CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_common_partition_triggers(master_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, 
	enable_or_disable VARCHAR, partition_schema VARCHAR, OUT ddl_stmt VARCHAR[])
RETURNS VARCHAR[]
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_common_partition_triggers()
Parameters:	Schema, table, column, enable or disable, partition schema
Returns:	DDL statement array
Description:	Automatic range/hash partitioning schema.table on column: ENABLE or DISABLE ON-INSERT triggers
  		on master table and all inherited tables
 */
DECLARE
	c1rpct CURSOR(master_schema VARCHAR, l_table VARCHAR) FOR /* Get triggers */
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
		   AND ns.nspname              = master_schema
		   AND cl.relname              = l_table
		   AND tgi.event_object_table  = l_table
		   AND tgi.event_object_schema = master_schema
		   AND tgi.trigger_name        = tg.tgname
		   AND tgi.event_manipulation  = 'INSERT'
		   AND cl.relname||'_insert'  != pc.proname /* Ignore partition INSERT function */
		   AND tg.tgfoid               = pc.oid
		   AND tg.tgisinternal         = FALSE	   /* Ignore constraints triggers */;
	c2rpct CURSOR(master_schema VARCHAR, l_table VARCHAR) FOR /* Get partitions */
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
		 WHERE ns2.nspname = master_schema
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
	FOR c1_rec IN c1rpct(master_schema, l_table) LOOP
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_triggers', 'Trigger [%] %.%(%): (state %)%', 
			i::VARCHAR,
			master_schema::VARCHAR, 
			c1_rec.tgname::VARCHAR, 
			c1_rec.function_name::VARCHAR, 
			c1_rec.tgenabled::VARCHAR, 
			enable_or_disable::VARCHAR);
--
-- Do not re-enable master triggers
--
		IF enable_or_disable = 'DISABLE' THEN
			i:=i+1;
			ddl_stmt[i]:='ALTER TABLE '||master_schema||'.'||l_table||' '||enable_or_disable||' TRIGGER '||c1_rec.tgname;
		END IF;
	END LOOP;
--
-- Now do inherited tables
--
	FOR c2_rec IN c2rpct(master_schema, l_table) LOOP
		FOR c1_rec IN c1rpct(partition_schema, c2_rec.child) LOOP
			i:=i+1;
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_common_partition_triggers', 'Child trigger [%] %.%(%): (%)%', 
				i::VARCHAR,
				master_schema::VARCHAR, 
				c1_rec.tgname::VARCHAR, 
				c1_rec.function_name::VARCHAR, 
				c1_rec.tgenabled::VARCHAR, 
				enable_or_disable::VARCHAR);
			ddl_stmt[i]:='ALTER TABLE '||partition_schema||'.'||c2_rec.child||' '||enable_or_disable||' TRIGGER '||c1_rec.tgname;
		END LOOP;
	END LOOP;
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_common_partition_triggers(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, OUT VARCHAR[]) IS 'Function: 	_rif40_common_partition_triggers()
Parameters:	Schema, table, column, enable or disable, partition schema
Returns:	DDL statement array
Description:	Automatic range/hash partitioning schema.table on column: ENABLE or DISABLE ON-INSERT triggers
  		on master table and all inherited tables';

--
-- Eof 