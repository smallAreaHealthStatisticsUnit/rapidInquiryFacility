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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_hash_partition_functional_index(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR, 
	num_partitions INTEGER,
	OUT ddl_stmt VARCHAR[])
RETURNS VARCHAR[]
AS $func$
/*
Function: 	_rif40_hash_partition_functional_index()
Parameters:	Schema, table, columnn, number of partitions
Returns:	DDL statement array
Description:	Create indexes by partition on hashing function
 */
DECLARE
 	i INTEGER:=0;
--
BEGIN
	FOR i IN 1 .. num_partitions LOOP
--		ddl_stmt[i]:='CREATE INDEX '||l_table||'_p'||i||'_hash ON '||l_schema||'.'||l_table||'_p'||i||
--			'(rif40_sql_pkg._rif40_hash_bucket_check('||l_column||', '||num_partitions||' /* total buckets */, '||i||' /* bucket requested */))';
		ddl_stmt[i]:='CREATE INDEX '||l_table||'_p'||i||'_hash ON '||l_schema||'.'||l_table||'_p'||i||
			'(rif40_sql_pkg._rif40_hash('||l_column||'::VARCHAR, '||num_partitions||' /* total buckets */))';
	END LOOP;
END;
$func$ 
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_hash_partition_functional_index(VARCHAR, VARCHAR, VARCHAR, INTEGER, OUT VARCHAR[]) IS 'Function: 	_rif40_hash_partition_functional_index()
Parameters:	Schema, table, columnn, number of partitions, partition value
Returns:	DDL statement array
Description:	Create indexes by partition on hashing function';

--
-- Eof