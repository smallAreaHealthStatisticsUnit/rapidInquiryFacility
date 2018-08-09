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
-- Rapid Enquiry Facility (RIF) - Create trigger_fct_t_rif40_studies_checks()
--				  INSERT/UPDATE/DELETE trigger function for T_RIF40_STUDIES
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

DROP TRIGGER IF EXISTS t_rif40_studies_checks ON "t_rif40_studies" CASCADE;

CREATE OR REPLACE FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_studies_checks() RETURNS trigger AS 
$BODY$
DECLARE
/*
<trigger_t_rif40_studies_checks_description>
<para>
Check - USERNAME exists.
Check - USERNAME is Kerberos USER on INSERT.
Check - audsid is SYS_CONTEXT('USERENV', 'SESSIONID') on INSERT.
Check - UPDATE not allowed except for IG admin and state changes.
Check - DELETE only allowed on own records.
Check - EXTRACT_TABLE Oracle name.
Check - Comparison area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1
Check - DENOM_TAB, DIRECT_STAND_TAB are valid Oracle names and appropriate denominators, and user has access.
Check - Study area resolution (GEOLEVEL_ID) >= comparision area resolution (GEOLEVEL_ID)  [i.e study area has the same or higher resolution]

Check - suppression_value - Suppress results with low cell counts below this value. If the role RIF_NO_SUPRESSION is granted and the user is not a RIF_STUDENT then SUPPRESSION_VALUE=0; otherwise is equals the parameter "SuppressionValue". If >0 all results with the value or below will be set to 0.
Check - extract_permitted - Is extract permitted from the database: 0/1. Only a RIF MANAGER may change this value. This user is still permitted to create and run a RIF study and to view the results. Geolevel access is rectricted by the RIF40_GEOLEVELS.RESTRICTED Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. All students must be granted permission by a RIF_MANAGER for any extract if the system parameter ExtractControl=1. This is enforced by the RIF application.

Check - authorised_by - must be a RIF MANAGER.

Check - transfer_permitted - Is transfer permitted from the Secure or Private Network: 0/1. This is for purely documentatary purposes only. Only a RIF MANAGER may change this value. The value defaults to the same as EXTRACT_PERMITTED. Only geolevels where RIF40_GEOLEVELS.RESTRICTED=0 may be transferred

Check - authorised_notes -IG authorisation notes. Must be filled in if EXTRACT_PERMITTED=1

Delayed RIF40_TABLES denominator and direct standardisation checks:
Check - Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists

IF USER = NEW.username (i.e. not initial RIF40 INSERT) THEN
	grant to all shared users if not already granted
 </para>
</trigger_t_rif40_studies_checks_description>
 */
--
-- Error range: -20200 to -20259 - T_RIF40_STUDIES
--
-- $Author: peterh $
-- $timestamp: 2012/10/23 09:05:57 $
-- Type: PL/SQL trigger
-- $RCSfile: v4_0_postgres_triggers.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/psql_scripts/v4_0_postgres_triggers.sql,v $
-- $Revision: 1.6 $
-- $State: Exp $
-- $Locker:  $
--
-- $Log: v4_0_postgres_triggers.sql,v $
-- Revision 1.6  2014/02/24 10:50:28  peterh
-- Full build from Oracle, including default study area and removal of year_start/stop min/max_age_group from T_RIF40_STUDIES
--
-- Still present in view
--
-- Revision 1.5  2013/09/25 12:12:22  peterh
-- Baseline after 2x full clean builds at Postgres level
-- TODO.txt uptodate
--
-- Revision 1.4  2013/09/18 15:20:32  peterh
-- Checkin at end of 6 week RIF focus. Got as far as SAHSULAND run study to completion for observed only
--
-- Revision 1.3  2013/09/02 14:08:33  peterh
--
-- Baseline after full trigger implmentation
--
--
	c1_stck CURSOR(l_table_name VARCHAR) FOR
		SELECT a.year_start, a.year_stop, a.isindirectdenominator, a.isdirectdenominator, a.isnumerator,
		       a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field,
                       MIN(g.offset) AS min_age_group, MAX(g.offset) AS max_age_group
		  FROM rif40_tables a
			LEFT OUTER JOIN rif40_age_groups g ON (g.age_group_id  = a.age_group_id)
		 WHERE table_name = l_table_name
		 GROUP BY a.year_start, a.year_stop, a.isindirectdenominator, a.isdirectdenominator, a.isnumerator,
  		          a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field;
	c1b_rec RECORD; /* Denominator */
	c1c_rec RECORD; /* Direct standardisation table */
	c2_stck CURSOR(l_geography VARCHAR, l_geolevel_name VARCHAR) FOR
		SELECT *
		  FROM t_rif40_geolevels
		 WHERE l_geography     = geography
		   AND l_geolevel_name = geolevel_name;
	c2a_rec RECORD;
	c2b_rec RECORD;
	c3_stck CURSOR (l_study_id INTEGER) FOR
		SELECT comparison_geolevel_name
		  FROM t_rif40_studies
		 WHERE study_id = l_study_id;
	c3_rec RECORD;
	c4_stck CURSOR FOR
		SELECT COUNT(study_id) AS total
		  FROM (
		SELECT study_id
		  FROM t_rif40_results
		 LIMIT 1) a;
	c4_rec RECORD;
	c5_stck CURSOR(l_username VARCHAR /* Study owner/IG role */) FOR
		SELECT CASE WHEN rif40_sql_pkg.does_role_exist(l_username) AND
		      		pg_has_role(l_username, 'rif_manager', 'USAGE') THEN 1 /* Not RIF_MANAGER */ ELSE 0 END AS rif_manager,
     		       CASE WHEN rif40_sql_pkg.does_role_exist(l_username) AND
		      		rif40_sql_pkg.rif40_does_role_exist('rif_student') AND
				pg_has_role(l_username, 'rif_student', 'USAGE') THEN 1 /* Not RIF_STUDENT */ ELSE 0 END AS rif_student,
		       c.param_value AS suppressionvalue,
     		       p1.param_value::INTEGER AS extractcontrol
		  FROM (
			SELECT p2.param_name, 
			       CASE WHEN rif40_sql_pkg.does_role_exist(l_username) AND
		      		pg_has_role(l_username, 'rif_no_suppression', 'USAGE') THEN 1 /* Not Suppressed */ ELSE p2.param_value::INTEGER END param_value
			  FROM rif40.t_rif40_parameters p2
			 WHERE p2.param_name = 'SuppressionValue') c
			LEFT OUTER JOIN t_rif40_parameters p1 ON (p1.param_name = 'ExtractControl');
	c5_rec RECORD;
	c6_stck CURSOR (l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR) IS
		SELECT column_name
		  FROM information_schema.columns
		 WHERE table_schema = LOWER(l_schema)
		   AND table_name   = LOWER(l_table)
		   AND column_name  = LOWER(l_column);
	c6_rec RECORD;
--
	schema			VARCHAR(30);
	denom_owner		VARCHAR(30);
	denom_table		VARCHAR(30);
	direct_stand_owner	VARCHAR(30);
	direct_stand_table	VARCHAR(30);
--
	study_state_only_flag 		BOOLEAN:=FALSE;
	ig_state_only_flag 			BOOLEAN:=FALSE;
	printselectstate_only_flag	BOOLEAN:=FALSE;
--
	kerberos_update BOOLEAN:=FALSE;
--
	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
	etp		TIMESTAMP WITH TIME ZONE;	
BEGIN
--
-- T_RIF40_STUDIES:	Check - USERNAME is Kerberos USER on INSERT
--			Check - audsid is SYS_CONTEXT('USERENV', 'SESSIONID') on INSERT
-- 			Check - UPDATE not allowed except for IG admin and state changes
--			Check - DELETE only allowed on own records
--
	OPEN c4_stck;	/* Initial RIF40 insert */
	FETCH c4_stck INTO c4_rec;
	CLOSE c4_stck;
            
	IF NOT TG_OP = 'DELETE' THEN
		OPEN c5_stck(NEW.username); /* Study owner/IG role */
		FETCH c5_stck INTO c5_rec;
		CLOSE c5_stck;
    END IF;
	
--
-- Set kerberos_update flag is needed
--	
	IF TG_OP = 'UPDATE' THEN
		IF (strpos(OLD.username, '@PRIVATE.NET') > 0) THEN	
			kerberos_update:=TRUE;
		END IF;
	END IF;
--
-- Determine if the study state only has changed
--
	IF TG_OP = 'UPDATE' AND
	   coalesce(NEW.study_id::text, '') = coalesce(OLD.study_id::text, '') AND
	   coalesce(NEW.username::text, '') = coalesce(OLD.username::text, '') AND
	   coalesce(NEW.geography::text, '') = coalesce(OLD.geography::text, '') AND
	   coalesce(NEW.project::text, '') = coalesce(OLD.project::text, '') AND
	   coalesce(NEW.study_name::text, '') = coalesce(OLD.study_name::text, '') AND
	   coalesce(NEW.extract_table::text, '') = coalesce(OLD.extract_table::text, '') AND
	   coalesce(NEW.map_table::text, '') = coalesce(OLD.map_table::text, '') AND
	   coalesce(NEW.study_date::text, '') = coalesce(OLD.study_date::text, '') AND
	   coalesce(NEW.study_type::text, '') = coalesce(OLD.study_type::text, '') AND
	   coalesce(NEW.comparison_geolevel_name::text, '') = coalesce(OLD.comparison_geolevel_name::text, '') AND
	   coalesce(NEW.study_geolevel_name::text, '') = coalesce(OLD.study_geolevel_name::text, '') AND
	   coalesce(NEW.denom_tab::text, '') = coalesce(OLD.denom_tab::text, '') AND
	   coalesce(NEW.direct_stand_tab::text, '') = coalesce(OLD.direct_stand_tab::text, '') AND
/*
Year_stop/start, min/max_age_group removed from t_rif40_studies. Still in view

	   coalesce(NEW.year_start::text, '') = coalesce(OLD.year_start::text, '') AND
	   coalesce(NEW.year_stop::text, '') = coalesce(OLD.year_stop::text, '') AND
	   coalesce(NEW.max_age_group::text, '') = coalesce(OLD.max_age_group::text, '') AND
	   coalesce(NEW.min_age_group::text, '') = coalesce(OLD.min_age_group::text, '') AND
*/
	   coalesce(NEW.suppression_value::text, '') = coalesce(OLD.suppression_value::text, '') AND
	   coalesce(NEW.extract_permitted::text, '') = coalesce(OLD.extract_permitted::text, '') AND
	   coalesce(NEW.transfer_permitted::text, '') = coalesce(OLD.transfer_permitted::text, '') AND
	   coalesce(NEW.authorised_by::text, '') = coalesce(OLD.authorised_by::text, '') AND
	   coalesce(NEW.authorised_on::text, '') = coalesce(OLD.authorised_on::text, '') AND
	   coalesce(NEW.authorised_notes::text, '') = coalesce(OLD.authorised_notes::text, '') AND
	   coalesce(NEW.audsid::text, '') = coalesce(OLD.audsid::text, '') AND
	   coalesce(NEW.print_state::text, '') = coalesce(OLD.print_state::text, '') AND
	   coalesce(NEW.select_state::text, '') = coalesce(OLD.select_state::text, '') AND
 	 	  NEW.study_state != OLD.study_state THEN
		study_state_only_flag:=TRUE;
	END IF;

--
-- Determine if the IG state only has changed
--
	IF TG_OP = 'UPDATE' AND
       coalesce(NEW.study_id::text, '') = coalesce(OLD.study_id::text, '') AND
	   coalesce(NEW.username::text, '') = coalesce(OLD.username::text, '') AND
	   coalesce(NEW.geography::text, '') = coalesce(OLD.geography::text, '') AND
	   coalesce(NEW.project::text, '') = coalesce(OLD.project::text, '') AND
	   coalesce(NEW.study_name::text, '') = coalesce(OLD.study_name::text, '') AND
	   coalesce(NEW.extract_table::text, '') = coalesce(OLD.extract_table::text, '') AND
	   coalesce(NEW.map_table::text, '') = coalesce(OLD.map_table::text, '') AND
	   coalesce(NEW.study_date::text, '') = coalesce(OLD.study_date::text, '') AND
	   coalesce(NEW.study_type::text, '') = coalesce(OLD.study_type::text, '') AND
	   coalesce(NEW.comparison_geolevel_name::text, '') = coalesce(OLD.comparison_geolevel_name::text, '') AND
	   coalesce(NEW.study_geolevel_name::text, '') = coalesce(OLD.study_geolevel_name::text, '') AND
	   coalesce(NEW.denom_tab::text, '') = coalesce(OLD.denom_tab::text, '') AND
	   coalesce(NEW.direct_stand_tab::text, '') = coalesce(OLD.direct_stand_tab::text, '') AND
/*
Year_stop/start, min/max_age_group removed from t_rif40_studies. Still in view

	   coalesce(NEW.year_start::text, '') = coalesce(OLD.year_start::text, '') AND
	   coalesce(NEW.year_stop::text, '') = coalesce(OLD.year_stop::text, '') AND
	   coalesce(NEW.max_age_group::text, '') = coalesce(OLD.max_age_group::text, '') AND
	   coalesce(NEW.min_age_group::text, '') = coalesce(OLD.min_age_group::text, '') AND
 */
	   coalesce(NEW.suppression_value::text, '') = coalesce(OLD.suppression_value::text, '') AND
	   coalesce(NEW.audsid::text, '') = coalesce(OLD.audsid::text, '') AND (      
	   	coalesce(NEW.extract_permitted::text, '') != coalesce(OLD.extract_permitted::text, '') OR
	   	coalesce(NEW.transfer_permitted::text, '') != coalesce(OLD.transfer_permitted::text, '') OR
	   	coalesce(NEW.authorised_by::text, '') != coalesce(OLD.authorised_by::text, '') OR
	   	coalesce(NEW.authorised_on::text, '') != coalesce(OLD.authorised_on::text, '') OR
	   	coalesce(NEW.authorised_notes::text, '') != coalesce(OLD.authorised_notes::text, '') ) THEN  

		ig_state_only_flag:=TRUE;
	END IF;
	

--
-- Determine if the select/print state only has changed
--
	IF TG_OP = 'UPDATE' AND
       coalesce(NEW.study_id::text, '') = coalesce(OLD.study_id::text, '') AND
	   coalesce(NEW.username::text, '') = coalesce(OLD.username::text, '') AND
	   coalesce(NEW.geography::text, '') = coalesce(OLD.geography::text, '') AND
	   coalesce(NEW.project::text, '') = coalesce(OLD.project::text, '') AND
	   coalesce(NEW.study_name::text, '') = coalesce(OLD.study_name::text, '') AND
	   coalesce(NEW.extract_table::text, '') = coalesce(OLD.extract_table::text, '') AND
	   coalesce(NEW.map_table::text, '') = coalesce(OLD.map_table::text, '') AND
	   coalesce(NEW.study_date::text, '') = coalesce(OLD.study_date::text, '') AND
	   coalesce(NEW.study_type::text, '') = coalesce(OLD.study_type::text, '') AND
	   coalesce(NEW.comparison_geolevel_name::text, '') = coalesce(OLD.comparison_geolevel_name::text, '') AND
	   coalesce(NEW.study_geolevel_name::text, '') = coalesce(OLD.study_geolevel_name::text, '') AND
	   coalesce(NEW.denom_tab::text, '') = coalesce(OLD.denom_tab::text, '') AND
	   coalesce(NEW.direct_stand_tab::text, '') = coalesce(OLD.direct_stand_tab::text, '') AND
/*
Year_stop/start, min/max_age_group removed from t_rif40_studies. Still in view

	   coalesce(NEW.year_start::text, '') = coalesce(OLD.year_start::text, '') AND
	   coalesce(NEW.year_stop::text, '') = coalesce(OLD.year_stop::text, '') AND
	   coalesce(NEW.max_age_group::text, '') = coalesce(OLD.max_age_group::text, '') AND
	   coalesce(NEW.min_age_group::text, '') = coalesce(OLD.min_age_group::text, '') AND
 */
	   coalesce(NEW.suppression_value::text, '') = coalesce(OLD.suppression_value::text, '') AND
	   coalesce(NEW.extract_permitted::text, '') = coalesce(OLD.extract_permitted::text, '') AND
	   coalesce(NEW.transfer_permitted::text, '') = coalesce(OLD.transfer_permitted::text, '') AND
	   coalesce(NEW.authorised_by::text, '') = coalesce(OLD.authorised_by::text, '') AND
	   coalesce(NEW.authorised_on::text, '') = coalesce(OLD.authorised_on::text, '') AND
	   coalesce(NEW.authorised_notes::text, '') = coalesce(OLD.authorised_notes::text, '') AND	   
	   coalesce(NEW.audsid::text, '') = coalesce(OLD.audsid::text, '') AND (      
	   coalesce(NEW.print_state::text, '') != coalesce(OLD.print_state::text, '') OR
	   coalesce(NEW.select_state::text, '') != coalesce(OLD.select_state::text, '') ) THEN  

		printselectstate_only_flag:=TRUE;
	END IF;	
--
-- Check for update of study_id
--
	IF TG_OP = 'UPDATE' AND COALESCE(NEW.study_id::Text, '') != COALESCE(OLD.study_id::Text, '') THEN
		PERFORM rif40_log_pkg.rif40_error(-20239, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study id may not be changed: %=>%',
			OLD.study_id::VARCHAR	/* Study id */,	
			NEW.study_id::VARCHAR	/* Study id */);
	END IF;
--
	IF TG_OP = 'INSERT' AND NEW.username != USER THEN
		IF USER = 'rif40' AND c4_rec.total = 0 THEN 
			/* Allowed during build before first result is added to system or when converting Kerberos users */
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
				'[20200] T_RIF40_STUDIES study % username: % is not USER: %; allowed during build before first result is added to system',
				NEW.study_id::VARCHAR	/* Study id */,
				NEW.username::VARCHAR	/* New username */,
				USER::VARCHAR		/* Username */);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20200, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study % username: % is not USER: %',
				NEW.study_id::VARCHAR	/* Study id */,
				NEW.username::VARCHAR	/* New username */,
				USER::VARCHAR		/* Username */);
		END IF;
	ELSIF TG_OP = 'UPDATE' AND NEW.username != USER THEN
		IF USER = 'rif40' AND (c4_rec.total = 0 OR kerberos_update) THEN 
			/* Allowed during build before first result is added to system or when converting Kerberos users */
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
				'[20200] T_RIF40_STUDIES study % new username: % is not USER: %; allowed during build before first result is added to system; Old: %',
				NEW.study_id::VARCHAR	/* Study id */,
				NEW.username::VARCHAR	/* New username */,
				USER::VARCHAR		/* Username */,
				OLD.username::VARCHAR	/* Old username */);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20200, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study % new username: % is not USER: %; Old: %',
				NEW.study_id::VARCHAR	/* Study id */,
				NEW.username::VARCHAR	/* New username */,
				USER::VARCHAR		/* Username */,
				OLD.username::VARCHAR	/* Old username */);
		END IF;
/* Kerberos checks no longer possible 
	ELSIF INSTR(NEW.username, '@PRIVATE.NET') = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-20201, 'T_RIF40_STUDIES study % username: % is not a Kerberos USER: %',
			NEW.study_id::VARCHAR	-* Study id *-,
			NEW.username::VARCHAR 	-* Insert username *-,
			USER::VARCHAR -* User *-); */
	END IF;
	IF TG_OP = 'INSERT' THEN
		NEW.audsid:=SYS_CONTEXT('USERENV', 'SESSIONID');
	ELSIF TG_OP = 'UPDATE' THEN /* Allow */
		IF NEW.username = OLD.username THEN
--
-- Only allow state changes
--
			IF study_state_only_flag THEN
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
					'[20202] T_RIF40_STUDIES study % UPDATE state changes allowed on T_RIF40_STUDIES by user: %',
					NEW.study_id::VARCHAR	/* Study id */,
					USER::VARCHAR 			/* username */);
--
-- Verify state change
--
				NEW.study_state:=rif40_sm_pkg.rif40_verify_state_change(NEW.study_id, OLD.study_state, NEW.study_state);
			ELSIF printselectstate_only_flag THEN			
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
					'[20202] T_RIF40_STUDIES study % UPDATE select/printstate changes allowed on T_RIF40_STUDIES by user: %',
					NEW.study_id::VARCHAR	/* Study id */,
					USER::VARCHAR 			/* username */);
					
			ELSE -- This implies you cannot IG authorise yourself!
				PERFORM rif40_log_pkg.rif40_error(-20202, 'trigger_fct_t_rif40_studies_checks', 
					'T_RIF40_STUDIES study % non state change UPDATE not allowed on T_RIF40_STUDIES by user: %',
					NEW.study_id::VARCHAR	/* Study id */,
					USER::VARCHAR 		/* username */);
			END IF;
		ELSE
			IF c5_rec.rif_manager = 1 THEN 
--
-- Only allow IG changes
--
				IF USER = 'rif40' AND strpos(OLD.username, '@PRIVATE.NET') > 0 THEN
					NULL; 		/* RIF studies update OK from @PRIVATE.NET to postgres */
				ELSIF ig_state_only_flag THEN
					NEW.username :=OLD.username; /* Keep the original user name */
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
						'[20203] T_RIF40_STUDIES study % UPDATE IG changes allowed on T_RIF40_STUDIES by user: %',
						NEW.study_id::VARCHAR	/* Study id */,
						USER::VARCHAR 		/* username */);
				ELSE
					PERFORM rif40_log_pkg.rif40_error(-20203, 'trigger_fct_t_rif40_studies_checks', 
						'T_RIF40_STUDIES study % non IG UPDATE not allowed on T_RIF40_STUDIES by user: %',
						NEW.study_id::VARCHAR	/* Study id */,
						USER::VARCHAR 		/* username */);
				END IF;
			ELSIF USER = 'rif40' AND strpos(OLD.username, '@PRIVATE.NET') > 0 THEN
				NULL; 		/* RIF studies update OK from @PRIVATE.NET to postgres */
			ELSE
				PERFORM rif40_log_pkg.rif40_error(-20204, 'trigger_fct_t_rif40_studies_checks', 
					'T_RIF40_STUDIES study % UPDATE not allowed on T_RIF40_STUDIES by user: %',
					NEW.study_id::VARCHAR	/* Study id */,
					USER::VARCHAR 		/* username */);
			END IF;
		END IF;
	ELSIF TG_OP = 'DELETE' AND OLD.username != USER THEN
		PERFORM rif40_log_pkg.rif40_error(-20205, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % DELETE only allowed on own records in T_RIF40_STUDIES, record created by: %',
			OLD.study_id::VARCHAR			/* Study id */,
			OLD.username::VARCHAR 			/* Insert (old) user name */);
	END IF;

--
-- End of delete checks
--
	IF TG_OP = 'DELETE' THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
			'[20200-5] T_RIF40_STUDIES study % CRUD checks OK',
			OLD.study_id::VARCHAR			/* Study id */);
		RETURN OLD;
	END IF;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
	       	'[20200-5] T_RIF40_STUDIES study % CRUD checks OK',
       		NEW.study_id::VARCHAR			/* Study id */);
    
--
-- Check - Comparison area geolevel name(COMPARISON_GEOLEVEL_NAME). Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1.
--
	IF (NEW.comparison_geolevel_name IS NOT NULL AND NEW.comparison_geolevel_name::text <> '') THEN
		OPEN c2_stck(NEW.geography, NEW.comparison_geolevel_name);
		FETCH c2_stck INTO c2a_rec;
		IF NOT FOUND THEN
			CLOSE c2_stck;
			PERFORM rif40_log_pkg.rif40_error(-20206, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study % comparison area geolevel name: "%" not found in RIF40_GEOLEVELS',
				NEW.study_id::VARCHAR			/* Study id */,
				NEW.comparison_geolevel_name::VARCHAR 	/* comparison area geolevel name */ );
		END IF;
		IF c2a_rec.comparea != 1 THEN
			CLOSE c2_stck;
			PERFORM rif40_log_pkg.rif40_error(-20207, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study % comparison area geolevel name: "%" in RIF40_GEOLEVELS is not a comparison area',
				NEW.study_id::VARCHAR			/* Study id */,
				NEW.comparison_geolevel_name 		/* comparison area geolevel name */ );
		END IF;
		CLOSE c2_stck;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
			'[20206-7] T_RIF40_STUDIES study % comparison area geolevel name: "%" OK',
			NEW.study_id::VARCHAR				/* Study id */,
			NEW.comparison_geolevel_name::VARCHAR 		/* comparison area geolevel name */ );
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-20208, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % NULL comparison area geolevel name',
			NEW.study_id::VARCHAR				/* Study id */);
	END IF;
--
-- Check - STUDY_GEOLEVEL_NAME. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS
--
	IF (NEW.study_geolevel_name IS NOT NULL AND NEW.study_geolevel_name::text <> '') THEN
		OPEN c2_stck(NEW.geography, NEW.study_geolevel_name);
		FETCH c2_stck INTO c2b_rec;
		IF NOT FOUND THEN
			CLOSE c2_stck;
			PERFORM rif40_log_pkg.rif40_error(-20209, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study % study area geolevel name: "%" not found in RIF40_GEOLEVELS',
				NEW.study_id::VARCHAR			/* Study id */,
				NEW.study_geolevel_name::VARCHAR 	/* Study area geolevel name */);
		END IF;
/*
 * Wrong - all areas are assummed to be mappable
 *
		IF c2b_rec.resolution != 1 THEN
			CLOSE c2;
			PERFORM rif40_log_pkg.rif40x_error(-202xx, 'T_RIF40_STUDIES study area geolevel name: '||NEW.study_geolevel_name||
				' in RIF40_GEOLEVELS is not a mappable area');
		END IF;
 */
		CLOSE c2_stck;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
			'[20209] T_RIF40_STUDIES study % study area geolevel name: "%" OK',
			NEW.study_id::VARCHAR				/* Study id */,
			NEW.study_geolevel_name::VARCHAR 		/* Study area geolevel name */);
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-20210, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % NULL study area geolevel name',
			NEW.study_id::VARCHAR				/* Study id */);
	END IF;

--
-- Check - DENOM_TAB, DIRECT_STAND_TAB are valid Oracle names and appropriate denominators, and user has access.
--
	PERFORM rif40_trg_pkg.rif40_db_name_check('DENOM_TAB', NEW.denom_tab);
	IF (NEW.direct_stand_tab IS NOT NULL AND NEW.direct_stand_tab::text <> '') THEN
		PERFORM rif40_trg_pkg.rif40_db_name_check('DIRECT_STAND_TAB', NEW.direct_stand_tab);
	END IF;
--
	OPEN c1_stck(NEW.denom_tab);
	FETCH c1_stck INTO c1b_rec;
	IF NOT FOUND THEN
		CLOSE c1_stck;
		PERFORM rif40_log_pkg.rif40_error(-20211, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % denominator: % not found in RIF40_TABLES',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.denom_tab::VARCHAR 		/* Denominator */);
	END IF;
	denom_owner:=rif40_sql_pkg.rif40_object_resolve(NEW.denom_tab::VARCHAR);
	IF USER = 'rif40' AND (
		c4_rec.total = 0 OR kerberos_update) THEN 
		/* Allowed during build before first result is added to system or when converting Kerberos users */
		NULL;
	ELSIF coalesce(denom_owner::text, '') = '' THEN
		CLOSE c1_stck;
		PERFORM rif40_log_pkg.rif40_error(-20212, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % denominator: % cannot be accessed',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.denom_tab::VARCHAR 		/* Denominator */);
	ELSE
		denom_table:=NEW.denom_tab::VARCHAR;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
			'[20212] T_RIF40_STUDIES study % denominator accessible as: %.%',
			NEW.study_id::VARCHAR		/* Study id */,
			denom_owner::VARCHAR		/* Denominator owner */,
			denom_table::VARCHAR 		/* Denominator table */);
	END IF;
	CLOSE c1_stck;
--
	IF (NEW.direct_stand_tab IS NOT NULL AND NEW.direct_stand_tab::text <> '') THEN
		OPEN c1_stck(NEW.direct_stand_tab);
		FETCH c1_stck INTO c1c_rec;
		IF NOT FOUND THEN
			CLOSE c1_stck;
			PERFORM rif40_log_pkg.rif40_error(-20213, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study % direct standardisation: % not found in RIF40_TABLES',
				NEW.study_id::VARCHAR		/* Study id */,
				NEW.direct_stand_tab::VARCHAR	/* direct standardisation table */);
		END IF;
		direct_stand_owner:=rif40_sql_pkg.rif40_object_resolve(NEW.direct_stand_tab::VARCHAR);
		IF coalesce(direct_stand_owner::text, '') = '' THEN
			CLOSE c1_stck;
			PERFORM rif40_log_pkg.rif40_error(-20214, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study % direct standardisation table: % cannot be accessed',
				NEW.study_id::VARCHAR		/* Study id */,
				NEW.direct_stand_tab::VARCHAR	/* direct standardisation table */);
		ELSE
			direct_stand_table:=NEW.direct_stand_tab::VARCHAR;
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
				'[20214] T_RIF40_STUDIES study % direct standardisation table accessible as: %.%',
				NEW.study_id::VARCHAR		/* Study id */,
				direct_stand_owner::VARCHAR	/* direct standardisation owner */,
				direct_stand_table::VARCHAR	/* direct standardisation table */);
		END IF;
		CLOSE c1_stck;
	END IF;
--
-- Check -  direct denominator
--
	IF NEW.direct_stand_tab IS NOT NULL AND NEW.direct_stand_tab::text <> '' THEN
		IF c1c_rec.isdirectdenominator != 1 THEN
			PERFORM rif40_log_pkg.rif40_error(-20215, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study % direct standardisation table: % is not a direct denominator table',
				NEW.study_id::VARCHAR			/* Study id */,
				NEW.direct_stand_tab::VARCHAR		/* direct standardisation table */);
		ELSIF c1c_rec.isdirectdenominator = 1 THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
				'[20215] T_RIF40_STUDIES study % direct standardisation table: % is a direct denominator table',
				NEW.study_id::VARCHAR			/* Study id */,
				NEW.direct_stand_tab::VARCHAR		/* direct standardisation table */);
		END IF;
	END IF;
/*
ONLY NEEDED IF RIF40_STUDIES_DENOMINATORS is created

	OPEN c3(NEW.study_id);
	FETCH c3 INTO c3_rec;
	IF NOT FOUND THEN
		CLOSE c3;
		PERFORM rif40_log_pkg.rif40x_error(-202xx, 'RIF40_STUDIES_DENOMINATORS no study found for study_id: '||TO_CHAR(NEW.study_id));
	END IF;
	CLOSE c3;
 */

--
-- This has been removed as analysis of the RIF30 data shows that the data is present. COMPARISON_GEOLEVEL_NAME has been made NOT NULL
-- TODO: verify this from math
--
--	IF (coalesce(NEW.comparison_geolevel_name::text, '') = ''     AND coalesce(NEW.direct_stand_tab::text, '') = '') OR
--	   ((NEW.comparison_geolevel_name IS NOT NULL AND NEW.comparison_geolevel_name::text <> '') AND (NEW.direct_stand_tab IS NOT NULL AND NEW.direct_stand_tab::text <> '')) THEN
--		PERFORM rif40_log_pkg.rif40x_error(-202xx, 'T_RIF40_STUDIES study % one and only one of COMPARISON_GEOLEVEL_NAME and DIRECT_STAND_TAB must be set',
--			NEW.study_id			/* Study id */);
--	END IF;

/*
Year_stop/start, min/max_age_group removed from t_rif40_studies. Still in view

	IF (c1b_rec.year_stop IS NOT NULL AND c1b_rec.year_stop::text <> '') AND NEW.year_start > c1b_rec.year_stop  THEN
		PERFORM rif40_log_pkg.rif40_error(-20216, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % denominator: % year stop: % is after RIF40_TABLES year stop: %',
			NEW.study_id::VARCHAR			/- Study id -/,
			NEW.denom_tab::VARCHAR 			/- Denominator -/,
			NEW.year_stop::VARCHAR			/- Denominator year stop -/,
			c1b_rec.year_stop::VARCHAR		/- RIF TABLES year stop -/);
	END IF;
	IF (c1b_rec.min_age_group IS NOT NULL AND c1b_rec.min_age_group::text <> '') AND NEW.min_age_group < c1b_rec.min_age_group  THEN
		PERFORM rif40_log_pkg.rif40_error(-20217, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % denominator: % min age group: % is before RIF40_TABLES min age group: %',
			NEW.study_id::VARCHAR			/- Study id -/,
			NEW.denom_tab::VARCHAR 			/- Denominator -/,
			NEW.min_age_group::VARCHAR		/- Denominator min age group -/,
			c1b_rec.min_age_group::VARCHAR		/- RIF TABLES min age group -/);
	END IF;
	IF (c1b_rec.max_age_group IS NOT NULL AND c1b_rec.max_age_group::text <> '') AND NEW.max_age_group > c1b_rec.max_age_group  THEN
		PERFORM rif40_log_pkg.rif40_error(-20218, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % denominator: % max age group: % is after RIF40_TABLES max age group: %',
			NEW.study_id::VARCHAR			/- Study id -/,
			NEW.denom_tab::VARCHAR 			/- Denominator -/,
			NEW.max_age_group::VARCHAR		/- Denominator max age group -/,
			c1b_rec.max_age_group::VARCHAR		/- RIF TABLES max age group -/);
	END IF;
	IF (c1b_rec.year_start IS NOT NULL AND c1b_rec.year_start::text <> '') AND NEW.year_start < c1b_rec.year_start  THEN
		PERFORM rif40_log_pkg.rif40_error(-20219, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % denominator: % year start: % is before RIF40_TABLES year start: %',
			NEW.study_id::VARCHAR			/- Study id -/,
			NEW.denom_tab::VARCHAR 			/- Denominator -/,
			NEW.year_start::VARCHAR			/- Denominator year start -/,
			c1b_rec.year_start::VARCHAR		/- RIF TABLES year start -/);
	END IF;
 */
	IF c1b_rec.isindirectdenominator != 1 THEN
		PERFORM rif40_log_pkg.rif40_error(-20220, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % denominator: % is not a denominator table',
			NEW.study_id::VARCHAR			/* Study id */,
			NEW.denom_tab::VARCHAR 			/* Denominator */);
	END IF;
	IF coalesce(c1b_rec.max_age_group::text, '') = '' OR coalesce(c1b_rec.min_age_group::text, '') = '' THEN
		PERFORM rif40_log_pkg.rif40_error(-20221, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % denominator: % no age group linkage',
			NEW.study_id::VARCHAR			/* Study id */,
			NEW.denom_tab::VARCHAR			/* Denominator */);
	END IF;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
		'[20216-21] T_RIF40_STUDIES study % year/age bands checks OK against RIF40_TABLES',
		NEW.study_id::VARCHAR				/* Study id */);

--
-- Check - Study area resolution (GEOLEVEL_ID) >= comparision area resolution (GEOLEVEL_ID)
--
	IF c2b_rec.geolevel_id /* study */ >= c2a_rec.geolevel_id /* comparision */ THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 
			'trigger_fct_t_rif40_studies_checks', 
			'[20222] T_RIF40_STUDIES study % study area geolevel id (%/%) >= comparision area (%/%) [i.e study area has the same or higher resolution]',
			NEW.study_id::VARCHAR			/* Study id */,
			c2b_rec.geolevel_id::VARCHAR		/* Study area geolevel ID */,
			NEW.study_geolevel_name::VARCHAR 	/* Study area geolevel name */,
			c2a_rec.geolevel_id::VARCHAR		/* Comparison area geolevel ID */,
			NEW.comparison_geolevel_name::VARCHAR 	/* Comparison area geolevel name */ );
	ELSIF USER = 'rif40' AND (c4_rec.total = 0 OR kerberos_update) THEN 
		/* Allowed during build before first result is added to system or when converting Kerberos users */
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 
			'trigger_fct_t_rif40_studies_checks', 
			'[20222] WARNING [OLD] T_RIF40_STUDIES study % study area geolevel id (%/%) < comparision area (%/%) [i.e study area a lower resolution]',
			NEW.study_id::VARCHAR			/* Study id */,
			c2b_rec.geolevel_id::VARCHAR		/* Study area geolevel ID */,
			NEW.study_geolevel_name::VARCHAR 	/* Study area geolevel name */,
			c2a_rec.geolevel_id::VARCHAR		/* Comparison area geolevel ID */,
			NEW.comparison_geolevel_name::VARCHAR 	/* Comparison area geolevel name */ );
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-20222, 
			'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % study area geolevel id (%/%) < comparision area (%/%) [i.e study area a lower resolution than the comparison area]',
			NEW.study_id::VARCHAR			/* Study id */,
			c2b_rec.geolevel_id::VARCHAR		/* Study area geolevel ID */,
			NEW.study_geolevel_name::VARCHAR 	/* Study area geolevel name */,
			c2a_rec.geolevel_id::VARCHAR		/* Comparison area geolevel ID */,
			NEW.comparison_geolevel_name::VARCHAR 	/* Comparison area geolevel name */ );
	END IF;

--
-- Check - suppression_value - Suppress results with low cell counts below this value. If the role RIF_NO_SUPRESSION is granted and the user is not a
-- RIF_STUDENT then SUPPRESSION_VALUE=0; otherwise is equals the parameter "SuppressionValue". If >0 all results with the value or below will be set to 0.
--
	NEW.suppression_value:=c5_rec.suppressionvalue;
	IF NEW.suppression_value > 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
			'[20223] T_RIF40_STUDIES study % suppressed at: %',
			NEW.study_id::VARCHAR			/* Study id */,
			NEW.suppression_value::VARCHAR		/* Suppression value*/);
	ELSIF c5_rec.rif_student = 1 THEN
		PERFORM rif40_log_pkg.rif40_error(-20223, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % not suppressed, but user % is a RIF_STUDENT',
			NEW.study_id::VARCHAR			/* Study id */,
			NEW.username::VARCHAR			/* Username */);
	END IF;

--
-- Check - extract_permitted - Is extract permitted from the database: 0/1. Only a RIF MANAGER may change this value. This user is still permitted to create
-- and run a RIF study and to view the results. Geolevel access is rectricted by the RIF40_GEOLEVELS.RESTRICTED Inforamtion Governance restrictions (0/1).
-- If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a
-- RIF_MANAGER to extract from the database the results, data extract and maps tables. All students must be granted permission by a RIF_MANAGER for any
-- extract if the system parameter ExtractControl=1. This is enforced by the RIF application.
--
	IF c5_rec.extractcontrol = 0 THEN
		NEW.extract_permitted:=1;
		NEW.transfer_permitted:=1;
		NEW.authorised_by:=NEW.username;
		NEW.authorised_on:=LOCALTIMESTAMP;
		NEW.authorised_notes:='Auto authorised; extract control is disabled';
	ELSE
		IF TG_OP = 'INSERT' THEN /* Force through IG process */
			IF c2b_rec.restricted = 0 AND c5_rec.rif_student = 1 THEN
				NEW.extract_permitted:=0;
				NEW.transfer_permitted:=0;
				NEW.authorised_by:=NULL;
				NEW.authorised_on:=NULL;
				NEW.authorised_notes:=NULL;
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 
					'trigger_fct_t_rif40_studies_checks', 
					'[20224-5] T_RIF40_STUDIES study % may NOT be extracted, study geolevel % is not restricted but the user: % is a RIF_STUDENT',
					NEW.study_id::VARCHAR			/* Study id */,
					NEW.study_geolevel_name::VARCHAR 	/* Study area geolevel name */,
					NEW.username::VARCHAR			/* Username */);
			ELSIF c2b_rec.restricted = 0 AND c5_rec.rif_student = 0 THEN
				NEW.extract_permitted:=1;
				NEW.transfer_permitted:=1;
				NEW.authorised_by:=NEW.username;
				NEW.authorised_on:=LOCALTIMESTAMP;
				NEW.authorised_notes:='Auto authorised; study geolevel is not restricted';
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 
					'trigger_fct_t_rif40_studies_checks', 
					'[20224-5] T_RIF40_STUDIES study % may be extracted, study geolevel % is not restricted for user: %',
					NEW.study_id::VARCHAR			/* Study id */,
					NEW.study_geolevel_name::VARCHAR 	/* Study area geolevel name */,
					NEW.username::VARCHAR			/* Username */);
			ELSE
				NEW.extract_permitted:=0;
				NEW.transfer_permitted:=0;
				NEW.authorised_by:=NULL;
				NEW.authorised_on:=NULL;
				NEW.authorised_notes:=NULL;
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 
					'trigger_fct_t_rif40_studies_checks', 
					'[20224-5] T_RIF40_STUDIES study % may NOT be extracted, study geolevel % is restricted for user: %. Requires authorisation by a RIF_MANAGER',
					NEW.study_id::VARCHAR			/* Study id */,
					NEW.study_geolevel_name::VARCHAR 	/* Study area geolevel name */,
					NEW.username::VARCHAR			/* Username */);
			END IF;
--
-- Check - authorised_by - must be a RIF MANAGER.
--
		ELSIF TG_OP = 'UPDATE' THEN
			IF NEW.extract_permitted = 1 THEN /* Attempting permit extraction */
				IF c5_rec.rif_student = 1 THEN
					PERFORM rif40_log_pkg.rif40_error(-20224, 'trigger_fct_t_rif40_studies_checks', 
						'T_RIF40_STUDIES study % may not be extracted, user % is a RIF_STUDENT',
						NEW.study_id::VARCHAR			/* Study id */,
						NEW.username::VARCHAR			/* Username */);
				ELSIF c5_rec.rif_manager = 1 THEN
					NEW.extract_permitted:=1;
					NEW.authorised_by:=NEW.username;
					NEW.authorised_on:=LOCALTIMESTAMP;
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
						'[20224-5] T_RIF40_STUDIES study % may be extracted, user % RIF_STUDENT/RIF_MANAGER tests passed',
						NEW.study_id::VARCHAR			/* Study id */,
						NEW.username::VARCHAR			/* Username */);
				ELSIF USER = 'rif40' AND kerberos_update THEN
					NULL; 		/* RIF studies update OK from @PRIVATE.NET to postgres */
				ELSE
					PERFORM rif40_log_pkg.rif40_error(-20225, 'trigger_fct_t_rif40_studies_checks', 
						'T_RIF40_STUDIES study % may not be extracted, modifying user % is NOT a RIF_MANAGER',
						NEW.study_id::VARCHAR			/* Study id */,
						USER::VARCHAR				/* Username */);
				END IF;
			END IF;
--
-- Check - transfer_permitted - Is transfer permitted from the Secure or Private Network: 0/1. This is for purely documentatary purposes only. Only a
-- RIF_MANAGER may change this value. The value defaults to the same as EXTRACT_PERMITTED. Only geolevels where RIF40_GEOLEVELS.RESTRICTED=0 may be
-- transferred
--
			IF c2b_rec.restricted = 1 THEN
				 NEW.transfer_permitted:=0;
			ELSE
				 NEW.transfer_permitted:=NEW.extract_permitted;
			END IF;
--
-- Check - authorised_notes - IG authorisation notes. Must be filled in if EXTRACT_PERMITTED=1
--
			IF NEW.transfer_permitted = 1 AND NEW.extract_permitted = 0 THEN
				PERFORM rif40_log_pkg.rif40_error(-20226, 'trigger_fct_t_rif40_studies_checks', 
					'T_RIF40_STUDIES study % may not be transferred, extract not permitted',
					NEW.study_id::VARCHAR				/* Study id */);
			ELSIF NEW.extract_permitted =  1 AND coalesce(NEW.authorised_notes::text, '') = '' THEN
				PERFORM rif40_log_pkg.rif40_error(-20227, 'trigger_fct_t_rif40_studies_checks', 
					'T_RIF40_STUDIES study % may not be extracted, no IG authorisation notes',
					NEW.study_id::VARCHAR				/* Study id */);
			ELSE
				IF NEW.transfer_permitted = 1 AND NEW.extract_permitted = 1 THEN
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
						'[20226-7] T_RIF40_STUDIES study % may be extracted and transferred',
						NEW.study_id::VARCHAR			/* Study id */);
				ELSIF NEW.transfer_permitted = 0 AND NEW.extract_permitted = 1 THEN
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
						'[20226-7] T_RIF40_STUDIES study % may be extracted but not transferred',
						NEW.study_id::VARCHAR			/* Study id */);
				END IF;
			END IF;
		END IF;
	END IF;

--
-- TODO
--
-- IF USER = NEW.username (i.e. not initial RIF40 INSERT) THEN
--	grant to all shared users if not already granted
--
	IF NEW.username != USER THEN
		NULL;
	END IF;

--
-- Check extract_table, map_table Oracle name, access (dependent on state)
--
	IF coalesce(NEW.extract_table::text, '') = '' THEN
		NEW.extract_table:='S'||NEW.study_id::Text||'_EXTRACT';
	END IF;
	PERFORM rif40_trg_pkg.rif40_db_name_check('EXTRACT_TABLE', NEW.extract_table);
	schema:=rif40_sql_pkg.rif40_object_resolve(NEW.extract_table::VARCHAR);
	IF (schema IS NOT NULL AND schema::text <> '') THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
			'[20228] T_RIF40_STUDIES study % extract table: % accessible',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.extract_table::VARCHAR 	/* extract_table */);
	ELSIF NEW.study_state NOT IN ('C', 'V', 'U') /* i.e. E, R */ THEN
		PERFORM rif40_log_pkg.rif40_error(-20228, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % extract table: % cannot be accessed; state: %',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.extract_table::VARCHAR 	/* extract_table */,
			NEW.study_state::VARCHAR	/* State */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
			'[20228] T_RIF40_STUDIES study % extract table: % cannot be accessed; state: % [IGNORED]',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.extract_table::VARCHAR 	/* extract_table */,
			NEW.study_state::VARCHAR	/* State */);
	END IF;
	IF coalesce(NEW.map_table::text, '') = '' THEN
		NEW.map_table:='S'||NEW.study_id::Text||'_MAP';
	END IF;
	PERFORM rif40_trg_pkg.rif40_db_name_check('MAP_TABLE', NEW.map_table);
	schema:=rif40_sql_pkg.rif40_object_resolve(NEW.map_table::VARCHAR);
	IF (schema IS NOT NULL AND schema::text <> '') THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
			'[20229] T_RIF40_STUDIES study % map table: % accessible',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.map_table::VARCHAR 		/* map_table */);
	ELSIF NEW.study_state NOT IN ('C', 'V', 'U', 'E') /* i.e. R */ THEN
		PERFORM rif40_log_pkg.rif40_error(-20229, 'trigger_fct_t_rif40_studies_checks', 
			'T_RIF40_STUDIES study % map table: % cannot be accessed; state: %',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.map_table::VARCHAR	 	/* map_table */,
			NEW.study_state::VARCHAR	/* State */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
			'[20229] T_RIF40_STUDIES study % map table: % cannot be accessed; state: % [IGNORED]',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.map_table::VARCHAR 		/* map_table */,
			NEW.study_state::VARCHAR	/* State */);
	END IF;

--
-- Delayed RIF40_TABLES denominator checks:
-- Check - Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists
--
	IF USER = 'rif40' AND (c4_rec.total = 0 OR kerberos_update) THEN 
		/* Allowed during build before first result is added to system or when converting Kerberos users */
		NULL;
	ELSIF c1b_rec.total_field IS NOT NULL AND c1b_rec.total_field IS NOT NULL THEN
		OPEN c6_stck(denom_owner, denom_table, c1b_rec.total_field);
		FETCH c6_stck INTO c6_rec;
		IF NOT FOUND THEN
			CLOSE c6_stck;
			PERFORM rif40_log_pkg.rif40_error(-20230, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study: % denominator RIF40_TABLES total field column: %.%.% NOT found',
				NEW.study_id::VARCHAR			/* Study */,
				denom_owner::VARCHAR			/* Denominator owner */,
				denom_table::VARCHAR			/* Denominator table */,
				c1b_rec.total_field::VARCHAR		/* Total field name */);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
				'[20230] T_RIF40_STUDIES study: % denominator RIF40_TABLES total field column: %.%.% found',
				NEW.study_id::VARCHAR			/* Study */,
				denom_owner::VARCHAR			/* Denominator owner */,
				denom_table::VARCHAR			/* Denominator table */,
				c1b_rec.total_field::VARCHAR		/* Total field name */);
		END IF;
		CLOSE c6_stck;
	END IF;
	IF USER = 'rif40' AND (c4_rec.total = 0 OR kerberos_update) THEN 
		/* Allowed during build before first result is added to system or when converting Kerberos users */
		NULL;
	ELSIF c1b_rec.sex_field_name IS NOT NULL AND c1b_rec.sex_field_name IS NOT NULL THEN
		OPEN c6_stck(denom_owner, denom_table, c1b_rec.sex_field_name);
		FETCH c6_stck INTO c6_rec;
		IF NOT FOUND THEN
			CLOSE c6_stck;
			PERFORM rif40_log_pkg.rif40_error(-20231, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study: % denominator RIF40_TABLES sex field column: %.%.% NOT found',
				NEW.study_id::VARCHAR			/* Study */,
				denom_owner::VARCHAR			/* Denominator owner */,
				denom_table::VARCHAR			/* Denominator table */,
				c1b_rec.sex_field_name::VARCHAR		/* Sex field name */);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
				'[20231] T_RIF40_STUDIES study: % denominator RIF40_TABLES sex field column: %.%.% found',
				NEW.study_id::VARCHAR			/* Study */,
				denom_owner::VARCHAR			/* Denominator owner */,
				denom_table::VARCHAR			/* Denominator table */,
				c1b_rec.sex_field_name::VARCHAR		/* Sex field name */);
		END IF;
		CLOSE c6_stck;
	END IF;
	IF USER = 'rif40' AND (c4_rec.total = 0 OR kerberos_update) THEN 
		/* Allowed during build before first result is added to system or when converting Kerberos users */
		NULL;
	ELSIF c1b_rec.age_group_field_name IS NOT NULL AND c1b_rec.age_group_field_name IS NOT NULL THEN
		OPEN c6_stck(denom_owner, denom_table, c1b_rec.age_group_field_name);
		FETCH c6_stck INTO c6_rec;
		IF NOT FOUND THEN
			CLOSE c6_stck;
			PERFORM rif40_log_pkg.rif40_error(-20232, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study: % denominator RIF40_TABLES age group field name column: %.%.% NOT found',
				NEW.study_id::VARCHAR			/* Study */,
				denom_owner::VARCHAR			/* Denominator owner */,
				denom_table::VARCHAR			/* Denominator table */,
				c1b_rec.age_group_field_name::VARCHAR	/* Age group field name */);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
				'[20232] T_RIF40_STUDIES study: % denominator RIF40_TABLES age group field name column: %.%.% found',
				NEW.study_id::VARCHAR			/* Study */,
				denom_owner::VARCHAR			/* Denominator owner */,
				denom_table::VARCHAR			/* Denominator table */,
				c1b_rec.age_group_field_name::VARCHAR	/* Age group field name */);
		END IF;
		CLOSE c6_stck;
	END IF;
	IF USER = 'rif40' AND (c4_rec.total = 0 OR kerberos_update) THEN 
		/* Allowed during build before first result is added to system or when converting Kerberos users */
		NULL;
	ELSIF c1b_rec.age_sex_group_field_name IS NOT NULL AND c1b_rec.age_sex_group_field_name IS NOT NULL THEN
		OPEN c6_stck(denom_owner, denom_table, c1b_rec.age_sex_group_field_name);
		FETCH c6_stck INTO c6_rec;
		IF NOT FOUND THEN
			CLOSE c6_stck;
			PERFORM rif40_log_pkg.rif40_error(-20233, 'trigger_fct_t_rif40_studies_checks', 
				'T_RIF40_STUDIES study: % denominator RIF40_TABLES age sex group field column: %.%.% NOT found',
				NEW.study_id::VARCHAR				/* Study */,
				denom_owner::VARCHAR				/* Denominator owner */,
				denom_table::VARCHAR				/* Denominator table */,
				c1b_rec.age_sex_group_field_name::VARCHAR	/* Age sex group field name */);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
				'[20233] T_RIF40_STUDIES study: % denominator RIF40_TABLES age sex group field column: %.%.% found',
				NEW.study_id::VARCHAR				/* Study */,
				denom_owner::VARCHAR				/* Denominator owner */,
				denom_table::VARCHAR				/* Denominator table */,
				c1b_rec.age_sex_group_field_name::VARCHAR	/* Age sex group field name */);
		END IF;
		CLOSE c6_stck;
	END IF;

	IF USER = 'rif40' AND (c4_rec.total = 0 OR kerberos_update) THEN 
		/* Allowed during build before first result is added to system or when converting Kerberos users */
		NULL;
	ELSIF direct_stand_owner IS NOT NULL AND direct_stand_owner::text <> '' THEN /* Direct standardisation table exists */
--
-- Delayed RIF40_TABLES direct standardisation table checks:
-- Check - Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists
--
		IF c1c_rec.total_field IS NOT NULL AND c1c_rec.total_field::text <> '' THEN
			OPEN c6_stck(direct_stand_owner, direct_stand_table, c1c_rec.total_field);
			FETCH c6_stck INTO c6_rec;
			IF NOT FOUND THEN
				CLOSE c6_stck;
				PERFORM rif40_log_pkg.rif40_error(-20234, 'trigger_fct_t_rif40_studies_checks', 
					'T_RIF40_STUDIES study: % direct standardisation RIF40_TABLES total field column: %.%.% NOT found',
					NEW.study_id::VARCHAR			/* Study */,
					direct_stand_owner::VARCHAR		/* Direct standardisation owner */,
					direct_stand_table::VARCHAR		/* Direct standardisation table */,
					c1c_rec.total_field::VARCHAR		/* Total field name */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
					'[20234] T_RIF40_STUDIES study: % direct standardisation RIF40_TABLES total field column: %.%.% found',
					NEW.study_id::VARCHAR			/* Study */,
					direct_stand_owner::VARCHAR		/* Direct standardisation owner */,
					direct_stand_table::VARCHAR		/* Direct standardisation table */,
					c1c_rec.total_field::VARCHAR		/* Total field name */);
			END IF;
			CLOSE c6_stck;
		END IF;
		IF c1c_rec.sex_field_name IS NOT NULL AND c1c_rec.sex_field_name::text <> '' THEN
			OPEN c6_stck(direct_stand_owner, direct_stand_table, c1c_rec.sex_field_name);
			FETCH c6_stck INTO c6_rec;
			IF NOT FOUND THEN
				CLOSE c6_stck;
				PERFORM rif40_log_pkg.rif40_error(-20235, 'trigger_fct_t_rif40_studies_checks', 
					'T_RIF40_STUDIES study: % direct standardisation RIF40_TABLES sex field column: %.%.% NOT found',
					NEW.study_id::VARCHAR			/* Study */,
					direct_stand_owner::VARCHAR		/* Direct standardisation owner */,
					direct_stand_table::VARCHAR		/* Direct standardisation table */,
					c1c_rec.sex_field_name::VARCHAR		/* Sex field name */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_studies_checks', 
					'[20235] T_RIF40_STUDIES study: % direct standardisation RIF40_TABLES sex field column: %.%.% found',
					NEW.study_id::VARCHAR			/* Study */,
					direct_stand_owner::VARCHAR		/* Direct standardisation owner */,
					direct_stand_table::VARCHAR		/* Direct standardisation table */,
					c1c_rec.sex_field_name::VARCHAR		/* Sex field name */);
			END IF;
			CLOSE c6_stck;
		END IF;
		IF c1c_rec.age_group_field_name IS NOT NULL AND c1c_rec.age_group_field_name::text <> '' THEN
			OPEN c6_stck(direct_stand_owner, direct_stand_table, c1c_rec.age_group_field_name);
			FETCH c6_stck INTO c6_rec;
			IF NOT FOUND THEN
				CLOSE c6_stck;
				PERFORM rif40_log_pkg.rif40_error(-20236, 
					'trigger_fct_t_rif40_studies_checks', 
					'T_RIF40_STUDIES study: % direct standardisation RIF40_TABLES age group field name column: %.%.% NOT found',
					NEW.study_id::VARCHAR			/* Study */,
					direct_stand_owner::VARCHAR		/* Direct standardisation owner */,
					direct_stand_table::VARCHAR		/* Direct standardisation table */,
					c1c_rec.age_group_field_name::VARCHAR	/* Age group field name */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 
					'trigger_fct_t_rif40_studies_checks', 
					'[20236] T_RIF40_STUDIES study: % direct standardisation RIF40_TABLES age group field name column: %.%.% found',
					NEW.study_id::VARCHAR			/* Study */,
					direct_stand_owner::VARCHAR		/* Direct standardisation owner */,
					direct_stand_table::VARCHAR		/* Direct standardisation table */,
					c1c_rec.age_group_field_name::VARCHAR	/* Age group field name */);
			END IF;
			CLOSE c6_stck;
		END IF;
		IF c1c_rec.age_sex_group_field_name IS NOT NULL AND c1c_rec.age_sex_group_field_name::text <> '' THEN
			OPEN c6_stck(direct_stand_owner, direct_stand_table, c1c_rec.age_sex_group_field_name);
			FETCH c6_stck INTO c6_rec;
			IF NOT FOUND THEN
				CLOSE c6_stck;
				PERFORM rif40_log_pkg.rif40_error(-20237, 
					'trigger_fct_t_rif40_studies_checks', 
					'T_RIF40_STUDIES study: % direct standardisation RIF40_TABLES age sex group field column: %.%.% NOT found',
					NEW.study_id::VARCHAR				/* Study */,
					direct_stand_owner::VARCHAR			/* Direct standardisation owner */,
					direct_stand_table::VARCHAR			/* Direct standardisation table */,
					c1c_rec.age_sex_group_field_name::VARCHAR	/* Age sex group field name */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 
					'trigger_fct_t_rif40_studies_checks', 
					'[20237] T_RIF40_STUDIES study: % direct standardisation RIF40_TABLES age sex group field column: %.%.% found',
					NEW.study_id::VARCHAR				/* Study */,
					direct_stand_owner::VARCHAR			/* Direct standardisation owner */,
					direct_stand_table::VARCHAR			/* Direct standardisation table */,
					c1c_rec.age_sex_group_field_name::VARCHAR	/* Age sex group field name */);
			END IF;
			CLOSE c6_stck;
		END IF;
	END IF;

--
-- Error message end: -20259, last message: -20239
--
	etp:=clock_timestamp();
	IF TG_OP = 'DELETE' THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 
			'trigger_fct_t_rif40_studies_checks', 
			'[20238] T_RIF40_STUDIES study: % checks complete; time taken %',
			OLD.study_id::VARCHAR				/* Study */,
			age(etp, stp)::VARCHAR);
--		
		RETURN OLD;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 
			'trigger_fct_t_rif40_studies_checks', 
			'[20238] T_RIF40_STUDIES study: % checks complete; time taken %',
			NEW.study_id::VARCHAR				/* Study */,
			age(etp, stp)::VARCHAR);
--		
		RETURN NEW;
	END IF;
END;
$BODY$
LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_studies_checks() IS 'Check - USERNAME exists.
Check - USERNAME is Kerberos USER on INSERT.
Check - audsid is SYS_CONTEXT(''USERENV'', ''SESSIONID'') on INSERT.
Check - UPDATE not allowed except for IG admin and state changes.
Check - DELETE only allowed on own records.
Check - EXTRACT_TABLE Oracle name.
Check - Comparison area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1
Check - DENOM_TAB, DIRECT_STAND_TAB are valid Oracle names and appropriate denominators, and user has access.
Check - Study area resolution (GEOLEVEL_ID) >= comparision area resolution (GEOLEVEL_ID)  [i.e study area has the same or higher resolution]

Check - suppression_value - Suppress results with low cell counts below this value. If the role RIF_NO_SUPRESSION is granted and the user is not a RIF_STUDENT then SUPPRESSION_VALUE=0; otherwise is equals the parameter "SuppressionValue". If >0 all results with the value or below will be set to 0.
Check - extract_permitted - Is extract permitted from the database: 0/1. Only a RIF MANAGER may change this value. This user is still permitted to create and run a RIF study and to view the results. Geolevel access is rectricted by the RIF40_GEOLEVELS.RESTRICTED Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. All students must be granted permission by a RIF_MANAGER for any extract if the system parameter ExtractControl=1. This is enforced by the RIF application.

Check - authorised_by - must be a RIF MANAGER.

Check - transfer_permitted - Is transfer permitted from the Secure or Private Network: 0/1. This is for purely documentatary purposes only. Only a RIF MANAGER may change this value. The value defaults to the same as EXTRACT_PERMITTED. Only geolevels where RIF40_GEOLEVELS.RESTRICTED=0 may be transferred

Check - authorised_notes -IG authorisation notes. Must be filled in if EXTRACT_PERMITTED=1

Delayed RIF40_TABLES denominator and direct standardisation checks:
Check - Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists

IF USER = NEW.username (i.e. not initial RIF40 INSERT) THEN
	grant to all shared users if not already granted';

CREATE TRIGGER t_rif40_studies_checks
	BEFORE INSERT OR UPDATE OR DELETE ON "t_rif40_studies" FOR EACH ROW
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_studies_checks();
COMMENT ON TRIGGER t_rif40_studies_checks ON t_rif40_studies IS 'INSERT OR UPDATE OR DELETE trigger: calls rif40_trg_pkg.trigger_fct_rif40_version_checks()';

--
-- Eof
