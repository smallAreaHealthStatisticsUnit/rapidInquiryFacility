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
--								  rif40_delete_study()
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
-- rif40_delete_study: 								57000 to 57199
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_delete_study(study_id INTEGER)
RETURNS void
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_delete_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_STATUS
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS
RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES 

Truncate extract and map tables if defined

Call: cleanup_orphaned_extract_and_map_tables()

 */
	c1_delst	CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1_rec		RECORD;
--
	sql_stmt	VARCHAR[];
	rows		INTEGER;
	schema 		VARCHAR;
BEGIN
	OPEN c1_delst(study_id);
	FETCH c1_delst INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_delst;
		PERFORM rif40_log_pkg.rif40_error(-57000, 'rif40_delete_study', 
			'Study ID % not found',
			study_id::VARCHAR);
	END IF;
	CLOSE c1_delst;
--
	sql_stmt[1]:='DELETE FROM rif40_study_status WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[2]:='DELETE FROM rif40_study_sql WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[3]:='DELETE FROM rif40_study_sql_log WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[4]:='DELETE FROM rif40_results WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[5]:='DELETE FROM rif40_contextual_stats WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[6]:='DELETE FROM rif40_inv_conditions WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[7]:='DELETE FROM rif40_inv_covariates WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[8]:='DELETE FROM rif40_investigations WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[9]:='DELETE FROM rif40_study_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[10]:='DELETE FROM rif40_comparison_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[11]:='DELETE FROM rif40_study_shares WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[12]:='DELETE FROM rif40_studies WHERE study_id = '||study_id::VARCHAR;
--
-- Drop extract and map tables if defined
-- (Cannot be dropped yets - requires definer function to cleanup all zero sized orphaned extract tables)
--
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.extract_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.extract_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_delete_study', 
			'[57001] Will truncate extract table % for study %',
			c1_rec.extract_table::VARCHAR	/* Extract table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
	END IF;
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.map_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.map_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.map_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_delete_study', 
			'[57002] Will truncate map table % for study %',
			c1_rec.map_table::VARCHAR	/* Map table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.map_table);
	END IF;
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);

	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_delete_study', 
		'[57003] Deleted study: %; % rows deleted',
		 study_id::VARCHAR		/* Study ID */,
		 rows::VARCHAR 			/* Rows deleted */);
--
-- Cleanup and map and extract tables not referenced by a study (runs as rif40)
--
	PERFORM rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables();
--
	RETURN;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) IS 'Function:	rif40_delete_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS
RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES

Truncate extract and map tables if defined

Call: cleanup_orphaned_extract_and_map_tables()
';

--
-- Eof