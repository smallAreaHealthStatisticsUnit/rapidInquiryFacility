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
--								  rif40_clone_study()
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
-- rif40_clone_study: 									57400 to 57599
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_clone_study(study_id INTEGER)
RETURNS INTEGER
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_clone_study()
Parameter:	Study ID
Returns:	New study ID
Description:	Clone study [testing purposes only] from tables:

RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES 

Does not clone:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS

 */
	c5clo CURSOR FOR /* Check if rif40_investigations.geography column still exists (pre alter 2) */
		SELECT column_name
		  FROM information_schema.columns
		 WHERE table_name   = 'rif40_investigations'
		   AND column_name  = 'geography'
		   AND table_schema = 'rif40';
	c5clo_rec RECORD;
--
	sql_stmt	VARCHAR[];
	rows		INTEGER;
BEGIN
--	
	OPEN c5clo;
	FETCH c5clo INTO c5clo_rec;
	CLOSE c5clo;
--
	sql_stmt[7]:='INSERT INTO rif40_inv_conditions(line_number, min_condition, max_condition, predefined_group_name, outcome_group_name) 
				SELECT line_number, min_condition, max_condition, predefined_group_name, outcome_group_name
 				  FROM t_rif40_inv_conditions WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[6]:='INSERT INTO rif40_inv_covariates(geography, covariate_name, study_geolevel_name, min, max)
		 SELECT geography, covariate_name, study_geolevel_name, min, max FROM rif40_inv_covariates WHERE study_id = '||study_id::VARCHAR;
--
-- Check if rif40_investigations.geography column still exists (pre alter 2)
--
	IF 	c5clo_rec.column_name = 'geography' THEN	
		sql_stmt[5]:='INSERT INTO rif40_investigations(geography, inv_name, inv_description, genders, numer_tab, 
				year_start, year_stop, max_age_group, min_age_group) 
				SELECT geography, inv_name, inv_description, genders, numer_tab, 
				year_start, year_stop, max_age_group, min_age_group FROM rif40_investigations WHERE study_id = '||study_id::VARCHAR;
	ELSE
		sql_stmt[5]:='INSERT INTO rif40_investigations(/* geography, */ inv_name, inv_description, genders, numer_tab, 
				year_start, year_stop, max_age_group, min_age_group) 
				SELECT /* geography, */ inv_name, inv_description, genders, numer_tab, 
				year_start, year_stop, max_age_group, min_age_group FROM rif40_investigations WHERE study_id = '||study_id::VARCHAR;
	END IF;
	sql_stmt[4]:='INSERT INTO rif40_study_areas(area_id, band_id) SELECT area_id, band_id FROM rif40_study_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[3]:='INSERT INTO rif40_comparison_areas(area_id) SELECT area_id FROM rif40_comparison_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[2]:='INSERT INTO rif40_study_shares(grantee_username) SELECT grantee_username FROM rif40_study_shares WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[1]:='INSERT INTO rif40_studies(geography, project, study_name, study_type,
 		comparison_geolevel_name, study_geolevel_name, denom_tab,            
 		year_start, year_stop, max_age_group, min_age_group, 
 		suppression_value, extract_permitted, transfer_permitted)
		SELECT geography, project, study_name, study_type,
 		comparison_geolevel_name, study_geolevel_name, denom_tab,            
 		year_start, year_stop, max_age_group, min_age_group, 
 		suppression_value, extract_permitted, transfer_permitted FROM rif40_studies WHERE study_id = '||study_id::VARCHAR;
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);

	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_clone_study', 
		'[57400] Cloned study: %; % rows inserted',
		 study_id::VARCHAR		/* Study ID */,
		 rows::VARCHAR 			/* Rows inserted */);
--
	RETURN currval('rif40_study_id_seq'::regclass);
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_clone_study(INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_clone_study(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_clone_study(INTEGER) IS 'Function:	rif40_clone_study()
Parameter:	Study ID
Returns:	New study ID
Description:	Clone study [testing purposes only] from tables:

RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES 

Does not clone:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS
';

--
-- Eof