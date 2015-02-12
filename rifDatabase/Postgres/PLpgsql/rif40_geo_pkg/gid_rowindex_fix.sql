-- ************************************************************************
-- *
-- * THIS SCRIPT MAY BE EDITED - NO NEED TO USE ALTER SCRIPTS
-- *
-- ************************************************************************
--
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
-- Rapid Enquiry Facility (RIF) - Add: gid_rowindex (i.e 1_1). 
--								  Where gid corresponds to gid in geometry table
--         					      row_index is an incremental serial aggregated by gid (starts from one for each gid)
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.gid_rowindex_fix(
	l_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
DECLARE
/*
Function: 	gid_rowindex_fix()
Parameters:	Geography
Returns:	Nothing
Description:	Add: gid_rowindex (i.e 1_1). Where gid corresponds to gid in geometry table
			row_index is an incremental serial aggregated by gid ( starts from one for each gid)

 */
	c2alter2 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography = l_geography;
	c3alter2 CURSOR(l_table VARCHAR) FOR
		SELECT relhassubclass 
		  FROM pg_class t, pg_namespace n
		 WHERE t.relname = l_table
 		   AND t.relkind = 'r' /* Table */ 
		   AND n.nspname IN ('rif40', 'rif_data')
		   AND t.relnamespace = n.oid ;
	c4alter2 CURSOR(l_table VARCHAR, l_column VARCHAR) FOR
		SELECT column_name 
		  FROM information_schema.columns 
		  WHERE table_name = l_table AND column_name = l_column;	
--
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
--
	ddl_stmt			VARCHAR[];
	l_partition			VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'simplify_geometry', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	OPEN c3alter2(quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry'));
	FETCH c3alter2 INTO c3_rec;
	CLOSE c3alter2;
	OPEN c4alter2(quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry'), 'gid_rowindex');
	FETCH c4alter2 INTO c4_rec;
	CLOSE c4alter2;
-- Geometry table exists and column has not been added yet
	IF NOT c3_rec.relhassubclass THEN 
		PERFORM rif40_log_pkg.rif40_error(-10002, 'simplify_geometry', 'Partitioned geometry table % not found', 
			quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')	/* Geometry table */);	
	ELSE
		IF c4_rec.column_name IS NOT NULL THEN
			IF ddl_stmt IS NULL THEN
				ddl_stmt[1]:='ALTER TABLE rif_data.'||quote_ident('t_rif40_'||
					LOWER(l_geography)||'_geometry')||' DROP COLUMN gid_rowindex';
			ELSE
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='ALTER TABLE rif_data.'||
					quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||' DROP COLUMN gid_rowindex';
			END IF;
		END IF;
-- Add column to master table
		IF ddl_stmt IS NULL THEN
			ddl_stmt[1]:='ALTER TABLE rif_data.'||
				quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||' ADD COLUMN gid_rowindex VARCHAR(50)';
		ELSE
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='ALTER TABLE rif_data.'||
				quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||
				' ADD COLUMN gid_rowindex VARCHAR(50)';
		END IF;
-- Comment master and inherited partitions
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON COLUMN rif_data.'||
			quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||
			'.gid_rowindex IS ''GID rowindex record locator unique key''';
--
		FOR c2_rec IN c2alter2(l_geography) LOOP
			l_partition:=quote_ident('p_rif40_geolevels_geometry_'||
				LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name));
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='COMMENT ON COLUMN rif40_partitions.'||l_partition||
				'.gid_rowindex IS ''GID rowindex record locator unique key''';
/*
Other databases may have issue with this CTE update syntax:

WITH a AS (
	SELECT area_id, gid, gid||'_'||ROW_NUMBER() OVER(PARTITION BY gid ORDER BY area_id) AS gid_rowindex
	  FROM rif40_partitions.p_rif40_geolevels_geometry_sahsu_level2
)
UPDATE rif40_partitions.p_rif40_geolevels_geometry_sahsu_level2 b
   SET gid_rowindex = a.gid_rowindex
  FROM a
 WHERE b.area_id = a.area_id;

 */
-- Fix gid so it it unique per area_id /(ST_Union'ed together - so are)
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='WITH a AS ('||E'\n'||
E'\t'||'SELECT area_id, gid'||E'\n'||
E'\t'||'  FROM rif40_partitions.'||l_partition||E'\n'||
E'\t'||'  ORDER BY area_id '||E'\n'||
'), b AS ('||E'\n'||
E'\t'||'SELECT a.area_id, a.gid, ROW_NUMBER() OVER() AS new_gid'||E'\n'||
E'\t'||'  FROM a'||E'\n'||
')'||E'\n'||
'UPDATE rif40_partitions.'||l_partition||' c'||E'\n'||
'   SET gid = b.new_gid'||E'\n'||
'  FROM b'||E'\n'||
' WHERE c.area_id = b.area_id';
-- Update gid_rowindex
				ddl_stmt[array_length(ddl_stmt, 1)+1]:='WITH a AS ('||E'\n'||
E'\t'||'SELECT area_id, gid,'||E'\n'||
E'\t'||'       LPAD(gid::Text, 10, ''0''::Text)||''_''||LPAD(ROW_NUMBER() OVER(PARTITION BY gid ORDER BY area_id)::Text, 10, ''0''::Text) AS gid_rowindex'||E'\n'||
E'\t'||'  FROM rif40_partitions.'||l_partition||E'\n'||
')'||E'\n'||
'UPDATE rif40_partitions.'||l_partition||' b'||E'\n'||
'   SET gid_rowindex = a.gid_rowindex'||E'\n'||
'  FROM a'||E'\n'||
' WHERE b.area_id = a.area_id';
-- Create unqiue indexes
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='CREATE UNIQUE INDEX '||l_partition||'_gidr ON rif40_partitions.'||l_partition||'(gid_rowindex)';
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='DROP INDEX IF EXISTS rif40_partitions.'||l_partition||'_gid';
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='CREATE UNIQUE INDEX '||l_partition||'_gid ON rif40_partitions.'||l_partition||'(gid)';
-- Make not null
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='ALTER TABLE rif40_partitions.'||l_partition||
				' ALTER COLUMN gid_rowindex SET NOT NULL';
-- Analyse
			ddl_stmt[array_length(ddl_stmt, 1)+1]:='ANALYZE VERBOSE rif40_partitions.'||l_partition;
		END LOOP;

-- Analyze at master level
		ddl_stmt[array_length(ddl_stmt, 1)+1]:='ANALYZE VERBOSE rif_data.'||
			quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry');
	END IF;
--
	IF ddl_stmt IS NOT NULL THEN
		PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmt);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'gid_rowindex_fix', 
			'GID and GID_ROWINDEX support already added');
	END IF;
END;
$body$
LANGUAGE PLPGSQL;
		
COMMENT ON FUNCTION rif40_geo_pkg.gid_rowindex_fix(VARCHAR) IS 'Function: 	gid_rowindex_fix()
Parameters:	Geography
Returns:	Nothing
Description:	Add: gid_rowindex (i.e 1_1). Where gid corresponds to gid in geometry table
			row_index is an incremental serial aggregated by gid ( starts from one for each gid)';
			
--
-- Eof