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
--								  rif40_rename_map_and_extract_tables()
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
-- rif40_rename_map_and_extract_tables:				57600 to 57799
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_rename_map_and_extract_tables(old_study_id INTEGER, new_study_id INTEGER)
RETURNS void
SECURITY DEFINER
AS $func$
DECLARE
/*
Function:		rif40_rename_map_and_extract_tables()
Parameter:		Old study ID, new study ID
Returns:		Nothing
Description:	Check if <new study id> exists; delete extract and map tables for <new study id>; 
				rename <old study id> extract and map tables to <new study id> extract and map tables
				
				Check USER is owner of both studies
 */
 	c1_renst			CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM t_rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1a_rec				RECORD;
	c1b_rec				RECORD;
--
	sql_stmt 			VARCHAR[];
--
	new_extract_table	VARCHAR;
	new_map_table		VARCHAR;
 BEGIN
 --
 -- Check studies exist
 --
 	OPEN c1_renst(old_study_id);
	FETCH c1_renst INTO c1a_rec;
	IF NOT FOUND THEN
		CLOSE c1_renst;
		PERFORM rif40_log_pkg.rif40_error(-57600, 'rif40_rename_map_and_extract_tables', 
			'Old study ID % not found',
			old_study_id::VARCHAR		/* Old study ID */);
	END IF;
	CLOSE c1_renst;

--
-- Check USER is owner of <old study ID>
--
	IF c1a_rec.username != session_user THEN
		PERFORM rif40_log_pkg.rif40_error(-57601, 'rif40_rename_map_and_extract_tables', 
			'User: % is not the owner (%) of old study ID %',
			session_user::VARCHAR		/* User */,
			c1a_rec.username			/* Old study owner */,
			c1a_rec.study_id::VARCHAR	/* Old study ID */);
	END IF;

--
-- Check if <new study id> exists
--
 	OPEN c1_renst(new_study_id);
	FETCH c1_renst INTO c1b_rec;
	IF NOT FOUND THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_rename_map_and_extract_tables', 
			'[57602] New study ID % not found',
			new_study_id::VARCHAR		/* New study ID */);
	ELSIF c1b_rec.username != session_user THEN
--
-- Check USER is owner of <new study id>
--
		CLOSE c1_renst;
		PERFORM rif40_log_pkg.rif40_error(-57003, 'rif40_rename_map_and_extract_tables', 
			'User: % is not the owner (%) of old study ID %',
			session_user::VARCHAR			/* User */,
			c1b_rec.username			/* New study owner */,
			c1b_rec.study_id::VARCHAR	/* New study ID */);
	ELSE
--
-- Delete extract and map tables for <new study id>; 
--
		sql_stmt[1]:='DROP TABLE IF EXISTS '||LOWER(c1b_rec.extract_table)||' /* New extract table */;';
		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE IF EXISTS '||LOWER(c1b_rec.map_table)||' /* New map table */;';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_rename_map_and_extract_tables', 
			'[57604] Delete extract and map tables for new study: %; extract: %, map: %, description; %',
			c1b_rec.study_id::VARCHAR		/* New study ID */,
			c1b_rec.extract_table::VARCHAR 	/* New extract table */,
			c1b_rec.map_table::VARCHAR 		/* New map table */,
			c1a_rec.description::VARCHAR	/* Study description */);
	END IF;
	CLOSE c1_renst;

--
-- Rename <old study id> extract and map tables to <new study id> extract and map tables
--
	sql_stmt:=NULL;
	IF c1b_rec.study_id IS NOT NULL THEN /* New study ID */
		new_extract_table:=LOWER(c1b_rec.extract_table);
		new_map_table:=LOWER(c1b_rec.map_table);
	ELSE /* OK; guess  the default */
		new_extract_table:='s'||new_study_id::Text||'_extract';
		new_map_table:='s'||new_study_id::Text||'_map';
	END IF;
	sql_stmt[1]:='ALTER TABLE '||LOWER(c1a_rec.extract_table)||' /* Old extract table */'||E'\n'||
		' RENAME TO /* New extract table */ '||new_extract_table||';';
	sql_stmt[array_length(sql_stmt, 1)+1]:='ALTER TABLE '||LOWER(c1a_rec.map_table)||' /* Old map table */'||E'\n'||
		' RENAME TO /* New map table */ '||new_map_table||';';
	IF c1a_rec.description IS NOT NULL THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='COMMENT ON TABLE rif_studies.'||new_extract_table||' IS ''Study 1 extract: '||c1a_rec.description||'''';
		sql_stmt[array_length(sql_stmt, 1)+1]:='COMMENT ON TABLE rif_studies.'||new_map_table||' IS ''Study 1 extract: '||c1a_rec.description||'''';
	ELSE
		sql_stmt[array_length(sql_stmt, 1)+1]:='COMMENT ON TABLE rif_studies.'||new_extract_table||' IS ''Study 1 extract: No description''';
		sql_stmt[array_length(sql_stmt, 1)+1]:='COMMENT ON TABLE rif_studies.'||new_map_table||' IS ''Study 1 extract: No description''';
	END IF;
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_rename_map_and_extract_tables', 
		'[57605] Rename extract and map tables from old: % to new study: %; extract: %=>%, map: %=>%',
		 c1a_rec.study_id::VARCHAR			/* Old study ID */,
		 new_study_id::VARCHAR				/* New study ID */,
		 c1a_rec.extract_table::VARCHAR 	/* Old extract table */,
		 new_extract_table::VARCHAR			/* New extract table */,
		 c1a_rec.map_table::VARCHAR 		/* Old map table */,
		 new_map_table::VARCHAR				/* New map table */);
 END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_rename_map_and_extract_tables(INTEGER, INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_rename_map_and_extract_tables(INTEGER, INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_rename_map_and_extract_tables(INTEGER, INTEGER) IS 'Function:	rif40_rename_map_and_extract_tables()
Parameter:		Old study ID, new study ID
Returns:		Nothing
Description:	Check if <new study id> exists; delete extract and map tables for <new study id>; 
				rename <old study id> extract and map tables to <new study id> extract and map tables
				
				Check USER is owner of both studies
';

--
-- Eof