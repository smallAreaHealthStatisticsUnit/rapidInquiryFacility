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
--								  rif40_reset_study()
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
-- rif40_reset_study: 									57200 to 57399
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_reset_study(study_id INTEGER)
RETURNS void
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_reset_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Reset study to the 'C' (created state) so it can be re-run

Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS

Truncate extract and map tables if defined

Update study state to C

Call: cleanup_orphaned_extract_and_map_tables()
 */
	c1_reset CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1_rec		RECORD;
--
	sql_stmt	VARCHAR[];
	rows		INTEGER;
	schema 		VARCHAR;
--
	study_upd_count INTEGER;
	inv_upd_count INTEGER;
BEGIN
	OPEN c1_reset(study_id);
	FETCH c1_reset INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_reset;
		PERFORM rif40_log_pkg.rif40_error(-57200, 'rif40_reset_study', 
			'Study ID % not found',
			study_id::VARCHAR);
	END IF;
	CLOSE c1_reset;
--
	sql_stmt[1]:='DELETE FROM rif40_study_sql WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[2]:='DELETE FROM rif40_study_sql_log WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[3]:='DELETE FROM rif40_results WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[4]:='DELETE FROM rif40_contextual_stats WHERE study_id = '||study_id::VARCHAR;
--
-- Drop extract and map tables if defined
-- (Cannot be dropped yets - requires definer function to cleanup all zero sized orphaned extract tables)
--
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.extract_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.extract_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_reset_study', 
			'[57201] Will truncate extract table % for study %',
			c1_rec.extract_table::VARCHAR	/* Extract table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
	END IF;
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.map_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.map_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.map_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_reset_study', 
			'[57202] Will truncate map table % for study %',
			c1_rec.map_table::VARCHAR	/* Map table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.map_table);
	END IF;
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Update study state
--
	EXECUTE 'UPDATE rif40_investigations a
	   SET investigation_state = ''C''
 	 WHERE a.study_id = $1' USING study_id;
	GET DIAGNOSTICS study_upd_count = ROW_COUNT;
	EXECUTE 'UPDATE rif40_studies a
	   SET study_state = ''C''
 	 WHERE a.study_id = $1' USING study_id;
	GET DIAGNOSTICS inv_upd_count = ROW_COUNT;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_reset_study', 
		'[57203] Reset study: %; % rows deleted; % study, % investigation(s) updated',
		study_id::VARCHAR		/* Study ID */,
		rows::VARCHAR 			/* Rows deleted */,
		study_upd_count::VARCHAR 	/* Study update count */,
		inv_upd_count::VARCHAR 		/* Study update count */);
--
-- Cleanup and map and extract tables not referenced by a study (runs as rif40)
--
	PERFORM rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables();
--
	RETURN;
--
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_reset_study(INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_reset_study(INTEGER) TO rif40;

COMMENT ON FUNCTION rif40_sm_pkg.rif40_reset_study(INTEGER) IS 'Function:	rif40_reset_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Reset study to the ''C'' (created state) so it can be re-run

Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS

Truncate extract and map tables if defined

Update study state to ''C''

Call: cleanup_orphaned_extract_and_map_tables()
';

--
-- Eof