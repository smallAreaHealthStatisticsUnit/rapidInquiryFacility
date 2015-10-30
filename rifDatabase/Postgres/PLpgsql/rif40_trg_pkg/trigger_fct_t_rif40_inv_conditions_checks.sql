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
-- Rapid Enquiry Facility (RIF) - Create trigger_fct_t_rif40_investigations_checks()
--				  INSERT/UPDATE/DELETE trigger function for T_RIF40_INV_CONDITIONS
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

DROP TRIGGER IF EXISTS t_rif40_inv_conditions_checks ON t_rif40_inv_conditions CASCADE;
CREATE OR REPLACE FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_inv_conditions_checks() RETURNS trigger AS $BODY$
DECLARE
/*
<trigger_t_rif40_inv_conditions_checks_description>
<para>
Check - USERNAME exists.
Check - DELETE only allowed on own records.
</para>
</trigger_t_rif40_inv_conditions_checks_description>
 */
--
-- Error range: -20500 to -20509 - T_RIF40_INV_CONDITIONS
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
-- Revision 1.2  2013/03/14 17:35:38  peterh
-- Baseline for TX to laptop
--
-- Revision 1.4  2013/02/14 10:48:39  peterh
-- Baseline aftyer clean build with screen mockup changes
--
-- Revision 1.12  2012/10/23 09:05:57  peterh
--
-- Baseline after full build of SAHSUland RIF schema including data
-- No triggers, Geo data and system error/logon triggers to be done
--
-- Revision 1.11  2012/09/14 15:44:53  peterh
-- Baseline after full review of check code and error messages
-- IG functionality added
--
-- Revision 1.10  2012/09/05 15:16:37  peterh
--
-- RIF update after initial build of postgres DB
--
-- Revision 1.9  2012/07/03 12:28:55  peterh
--
-- RIF schema as at 3/6/2012. All entities complete
--
--
-- Check - CONDITION for SQL injection
--
	c4_icck CURSOR IS
		SELECT COUNT(study_id) AS total
		  FROM (
		SELECT study_id
		  FROM t_rif40_results
		 LIMIT 1) a;
	c4_rec RECORD;
--
	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
	etp		TIMESTAMP WITH TIME ZONE;	
BEGIN
--
	OPEN c4_icck;
	FETCH c4_icck INTO c4_rec;
	CLOSE c4_icck;
--
-- Check for update of study_id
--
	IF TG_OP = 'UPDATE' AND COALESCE(NEW.study_id::Text, '') != COALESCE(OLD.study_id::Text, '') THEN
		PERFORM rif40_log_pkg.rif40_error(-20504, 'trigger_fct_t_rif40_inv_conditions_checks', 
			'T_RIF40_INV_CONDITIONS study id may not be changed: %=>%',
			OLD.study_id::VARCHAR	/* Study id */,	
			NEW.study_id::VARCHAR	/* Study id */);
	END IF;
	
	IF TG_OP = 'INSERT' AND NEW.username != USER THEN
		IF USER = 'rif40' AND c4_rec.total = 0 THEN 
			/* Allowed during build before first result is added to system or before Kerberos update */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20500, 'trigger_fct_t_rif40_inv_conditions_checks',
				'T_RIF40_INV_CONDITIONS  study: % investigation: % line: % new username: % is not USER: %',
				NEW.study_id::VARCHAR	/* Study */,
				NEW.inv_id::VARCHAR	/* Investigation */,
				NEW.username::VARCHAR 	/* Record username */,
				NEW.line_number::VARCHAR /* Line */,
				USER::VARCHAR		/* Username */,
				NEW.username::VARCHAR	/* New username */);
		END IF;
	ELSIF TG_OP = 'UPDATE' AND NEW.username != USER THEN
		IF (USER = 'rif40' AND (c4_rec.total = 0 OR strpos(OLD.username, '@PRIVATE.NET') > 0)) THEN 
			/* Allowed during build before first result is added to system or before Kerberos update */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20500, 'trigger_fct_t_rif40_inv_conditions_checks',
				'T_RIF40_INV_CONDITIONS  study: % investigation: % line: % new username: % is not USER: %; old: %',
				NEW.study_id::VARCHAR	/* Study */,
				NEW.inv_id::VARCHAR	/* Investigation */,
				NEW.username::VARCHAR 	/* Record username */,
				NEW.line_number::VARCHAR /* Line */,
				USER::VARCHAR		/* Username */,
				NEW.username::VARCHAR	/* New username */,
				OLD.username::VARCHAR	/* Old username */);
		END IF;
/*	ELSIF INSTR(NEW.username, '@PRIVATE.NET') = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-20501, 't_rif40_inv_conditions_checks',
			'T_RIF40_INV_CONDITIONS study: % investigation: % line: % username: % is not a Kerberos USER: %',
			NEW.study_id::VARCHAR	/- Study -/,
			NEW.inv_id::VARCHAR	/- Investigation -/,
			NEW.line_number::VARCHAR /- Line -/,
			NEW.username::VARCHAR 	/- Record username -/,
			USER::VARCHAR		/- Logon username -/); */
	ELSIF TG_OP = 'UPDATE' THEN
		PERFORM rif40_log_pkg.rif40_error(-20502, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_CONDITIONS study: % investigation: % line: % UPDATE not allowed on T_RIF40_INV_CONDITIONS',
			NEW.study_id::VARCHAR		/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.line_number::VARCHAR 	/* Line */);    
	ELSIF TG_OP = 'DELETE' AND OLD.username != USER THEN
		PERFORM rif40_log_pkg.rif40_error(-20503, 'trigger_fct_t_rif40_inv_conditions_checks',
			'T_RIF40_INV_CONDITIONS study: % investigation: % line: % DELETE only allowed on own records in T_RIF40_INV_CONDITIONS, record owned by: %',
			NEW.study_id::VARCHAR	/* Study */,
			NEW.inv_id::VARCHAR	/* Investigation */,
			NEW.line_number::VARCHAR /* Line */,
			OLD.username::VARCHAR	/* Record username */);
	END IF;
--

	etp:=clock_timestamp();
	IF TG_OP = 'DELETE' THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_inv_conditions_checks',
			'[20500-3] T_RIF40_INV_CONDITIONS study: % investigation: % line: % CRUD checks OK; time taken %',
			OLD.study_id::VARCHAR		/* Study id */,
			OLD.inv_id::VARCHAR		/* Investigation */,
			OLD.line_number::VARCHAR 	/* Line */,
			age(etp, stp)::VARCHAR);
		RETURN OLD;
	ELSE  	
--
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_inv_conditions_checks',
			'[20500-3] T_RIF40_INV_CONDITIONS study: % investigation: % line: % CRUD checks OK; time taken %',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.inv_id::VARCHAR		/* Investigation */,
			NEW.line_number::VARCHAR 	/* Line */,
			age(etp, stp)::VARCHAR);
--
		RETURN NEW;
	END IF;
END; 
$BODY$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_inv_conditions_checks() IS 'Check - USERNAME exists.
Check - DELETE only allowed on own records.';

CREATE TRIGGER t_rif40_inv_conditions_checks
	BEFORE DELETE OR INSERT OR UPDATE ON t_rif40_inv_conditions
	FOR EACH ROW
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_inv_conditions_checks();
COMMENT ON TRIGGER t_rif40_inv_conditions_checks ON t_rif40_inv_conditions IS 'INSERT OR UPDATE OR DELETE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_inv_conditions_checks()';
	
--
-- Eof