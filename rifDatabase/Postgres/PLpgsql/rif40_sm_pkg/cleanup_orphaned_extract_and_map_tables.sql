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
-- Rapid Enquiry Facility (RIF) - PG psql code (state machine and extract SQL generation)
--								  rif40_execute_insert_statement()
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
-- Error codes:
--
-- cleanup_orphaned_extract_and_map_tables				56800 to 56999
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

CREATE OR REPLACE FUNCTION rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(truncate BOOLEAN DEFAULT FALSE)
RETURNS void
SECURITY DEFINER 
AS $func$
DECLARE
/*
Function:	cleanup_orphaned_extract_and_map_tables()
Parameter:	Truncate tables (default FALSE)
Returns:	Nothing
Description:	Cleanup and map and extract tables not referenced by a study (runs as rif40)
		Do not drop table if it contains any data unless truncate is true
		Orphans are defined as tables in rif_studies schema no longer referenced by studies

Note:		DO NOT PUT TABLES INTO THE rif_studies SCHEMA MANUALLY - they will be deleted if empty
 */
	c1_clorpf CURSOR FOR
		SELECT table_name /* tables in rif_studies schema no longer referenced by studies */
		  FROM information_schema.tables
		 WHERE table_schema = 'rif_studies'
		   AND table_type   = 'BASE TABLE'
		EXCEPT
		SELECT extract_table
		  FROM t_rif40_studies
		EXCEPT
		SELECT map_table
		  FROM t_rif40_studies
		 ORDER BY 1;
	c2_clorpf REFCURSOR;
	c1_rec RECORD;
	c2_rec RECORD;
--
	sql_stmt	VARCHAR;
	t_ddl		INTEGER:=0;
	ddl_stmts	VARCHAR[];
BEGIN
	FOR c1_rec IN c1_clorpf LOOP
		sql_stmt:='SELECT COUNT(study_id) AS total'||E'\n'||
			'  FROM ('||E'\n'||
			'SELECT study_id'||E'\n'||
			'  FROM rif_studies.'||c1_rec.table_name||E'\n'||
			' LIMIT 1) a';
		OPEN c2_clorpf FOR EXECUTE sql_stmt;
		FETCH c2_clorpf INTO c2_rec;
		IF NOT FOUND THEN
			CLOSE c2_clorpf;
			PERFORM rif40_log_pkg.rif40_error(-56800, 'cleanup_orphaned_extract_and_map_tables', 
				'Extract/map table % not found',
				c1_rec.table_name::VARCHAR);
		END IF;
		CLOSE c2_clorpf;
--
		IF c2_rec.total = 0 THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
				'[56801] Extract/map table % does not contain data; will be cleaned up',
				c1_rec.table_name::VARCHAR);
			sql_stmt:='DROP TABLE rif_studies.'||c1_rec.table_name;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
		ELSIF truncate THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
				'[56802] Extract/map table % contains data; truncate is TRUE; will be cleaned up',
				c1_rec.table_name::VARCHAR);
			sql_stmt:='TRUNCATE TABLE rif_studies.'||c1_rec.table_name;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
			sql_stmt:='DROP TABLE rif_studies.'||c1_rec.table_name;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
				'[56803] Extract/map table % contains data; will not be cleaned up',
				c1_rec.table_name::VARCHAR);
		END IF;
	END LOOP;
--
-- Now drop the tables
--
	IF t_ddl > 0 THEN
		PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmts);
		PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
			'[56804] % orphaned extract/map table(s) cleaned up',
			t_ddl::VARCHAR);
	END IF;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(BOOLEAN) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(BOOLEAN) IS 'Function:	cleanup_orphaned_extract_and_map_tables()
Parameter:	Truncate tables (default FALSE)
Returns:	Nothing
Description:	Cleanup and map and extract tables not referenced by a study (runs as rif40)
		Do not drop table if it contains any data unless truncate is true
		Orphans are defined as tables in rif_studies schema no longer referenced by studies

Note:		DO NOT PUT TABLES INTO THE rif_studies SCHEMA MANUALLY - they will be deleted if empty';

-- 
-- Eof