
-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Postgres triggers
--
-- Trigger code will be split up on a per table basis
--
-- This is currently run only when the database is built from scripts
-- Ideally it should be refactored into many bits so it can be run easily in
-- alter scripts
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

DROP TRIGGER IF EXISTS t_rif40_comp_areas_checks ON t_rif40_comparison_areas CASCADE;
DROP TRIGGER IF EXISTS t_rif40_comp_areas_checks_del ON t_rif40_comparison_areas CASCADE;
CREATE OR REPLACE FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_comp_areas_checks() RETURNS trigger AS $BODY$
DECLARE
/*
<trigger_t_rif40_comp_areas_checks_description>
<para>
Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
</para>
</trigger_t_rif40_comp_areas_checks_description>
 */
--
-- -20300 to -20319 - T_RIF40_COMPARISON_AREAS
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
-- Revision 1.8  2012/06/15 11:05:26  peterh
-- Check with working SQL generator (CREATE stmt only); bones of XML generator
-- Numerous lookup tables added.
-- Prepare for documentation
--
-- Revision 1.7  2012/05/22 15:29:41  peterh
--
-- Added RIF40_RESULTS, contextual_stats; more verification
--
-- Revision 1.6  2012/05/21 11:11:28  peterh
--
-- RIF investigattion, inv_covariates, comparison shares checks + SAHSU land example data
--
-- Revision 1.5  2012/04/26 15:49:48  peterh
-- Fixed view security problems
--
-- Revision 1.4  2012/04/13 15:05:02  peterh
--
-- Added RIF studies, investigations etc
--
-- Revision 1.3  2012/04/04 15:22:51  peterh
-- Easter baseline
--
-- Revision 1.2  2012/03/30 11:45:40  peterh
--
-- Baseline with working RIF40_NUM_DENOM
--
-- Revision 1.1  2012/03/28 15:39:19  peterh
--
-- Added check constraints and basic object table+column existance checks
--
-- Revision 1.2  2012/03/27 15:54:33  peterh
--
-- Added more V4 tables
--
-- Revision 1.1  2012/03/23 15:21:18  peterh
--
-- Moved from old RIF directory
--
-- Revision 1.2  2011/07/12 13:18:40  peterh
-- RIF baseline after Kerberosisation, re-doc and N_POP add
--
-- Revision 1.1  2011/05/27 10:31:21  peterh
--
-- Start of user level suppression support in RIF
--
	c4cack CURSOR IS
		SELECT COUNT(study_id) AS total
		  FROM (
		SELECT study_id
		  FROM t_rif40_results
		 LIMIT 1) a;
	c4_rec RECORD;
--
--	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
--	etp		TIMESTAMP WITH TIME ZONE;	
BEGIN
--
-- Check for update of study_id
--
	IF TG_OP = 'UPDATE' AND COALESCE(NEW.study_id::Text, '') != COALESCE(OLD.study_id::Text, '') THEN
		PERFORM rif40_log_pkg.rif40_error(-20304, 'trigger_fct_t_rif40_comp_areas_checks', 
			'T_RIF40_COMPARISON_AREAS study id may not be changed: %=>%',
			OLD.study_id::VARCHAR	/* Study id */,	
			NEW.study_id::VARCHAR	/* Study id */);
	END IF;
--
-- T_RIF40_COMPARISON_AREAS: Check - USERNAME is Kerberos USER on INSERT
--			Check - audsid is SYS_CONTEXT('USERENV', 'SESSIONID') on INSERT
-- 			Check - UPDATE not allowed
--			Check - DELETE only allowed on own records
--
	OPEN c4cack;
	FETCH c4cack INTO c4_rec;
	CLOSE c4cack;
	IF TG_OP = 'INSERT' AND NEW.username != USER THEN
		IF USER = 'rif40' AND c4_rec.total = 0 THEN 
			/* Allowed during build before first result is added to system or before Kerberos update */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20300, 'trigger_fct_t_rif40_comp_areas_checks', 
				'T_RIF40_COMPARISON_AREAS study: % area_id: % username: % is not USER: %',
				NEW.study_id::VARCHAR		/* Study id */,
				NEW.area_id::VARCHAR		/* Area */,
				NEW.username::VARCHAR		/* New username */,
				USER::VARCHAR			/* Username */);
		END IF;
	ELSIF TG_OP = 'UPDATE' AND NEW.username != USER THEN
		IF (USER = 'rif40' AND (c4_rec.total = 0 OR strpos(OLD.username, '@PRIVATE.NET') > 0)) THEN 
			/* Allowed during build before first result is added to system or before Kerberos update */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20300, 'trigger_fct_t_rif40_comp_areas_checks', 
				'T_RIF40_COMPARISON_AREAS study: % area_id: % new username: % is not USER: %; old: %',
				NEW.study_id::VARCHAR		/* Study id */,
				NEW.area_id::VARCHAR		/* Area */,
				NEW.username::VARCHAR		/* New username */,
				USER::VARCHAR			/* Username */,
				OLD.username::VARCHAR		/* Old username */);
		END IF;
/*
	ELSIF INSTR(NEW.username, '@PRIVATE.NET') = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-20301, 'trigger_fct_t_rif40_comp_areas_checks', 
			'T_RIF40_COMPARISON_AREAS study: % area_id: % username: % is not a Kerberos USER: %',
			NEW.username::VARCHAR		/- Record username -/,
			NEW.study_id::VARCHAR		/- Study id -/,
			NEW.area_id::VARCHAR		/- Area -/,
			USER::VARCHAR			/- Username -/); */
	ELSIF TG_OP = 'UPDATE' THEN
		PERFORM rif40_log_pkg.rif40_error(-20302, 'trigger_fct_t_rif40_comp_areas_checks', 
			'T_RIF40_COMPARISON_AREAS study: % area_id: % UPDATE not allowed on T_RIF40_COMPARISON_AREAS',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.area_id::VARCHAR		/* Area */);
	ELSIF TG_OP = 'DELETE' AND OLD.username != USER THEN
		PERFORM rif40_log_pkg.rif40_error(-20303, 'trigger_fct_t_rif40_comp_areas_checks', 
			'T_RIF40_COMPARISON_AREAS study: % area_id: % DELETE only allowed on own records in T_RIF40_COMPARISON_AREAS, record owned by: %',
			OLD.username::VARCHAR		/* INSERT Username */,
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.area_id::VARCHAR		/* Area */);
	END IF;
--
--	etp:=clock_timestamp();
	IF TG_OP = 'DELETE' THEN
--		PERFORM rif40_log_pkg.rif40_log('DEBUG4','trigger_fct_t_rif40_comp_areas_checks', 
--     	 		'[20300-3] T_RIF40_COMPARISON_AREAS study: % area_id: % CRUD checks OK',
--     	 		OLD.study_id::VARCHAR		/* Study id */,
--			OLD.area_id::VARCHAR		/* Area */);  
		RETURN OLD;
	ELSE  	
--
--		PERFORM rif40_log_pkg.rif40_log('DEBUG4','trigger_fct_t_rif40_comp_areas_checks', 
--     	 		'[20300-3] T_RIF40_COMPARISON_AREAS study: % area_id: % CRUD checks OK',
--     	 		NEW.study_id::VARCHAR		/* Study id */,
--			NEW.area_id::VARCHAR		/* Area */);
--
		RETURN NEW;
	END IF;
END; 
$BODY$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_comp_areas_checks() IS 'Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.';

CREATE TRIGGER t_rif40_comp_areas_checks
	BEFORE INSERT OR UPDATE OF area_id, username, study_id ON t_rif40_comparison_areas
	FOR EACH ROW
	WHEN ((NEW.username IS NOT NULL AND NEW.username::text <> '') OR (NEW.study_id IS NOT NULL AND NEW.study_id::text <> ''))
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_comp_areas_checks();
COMMENT ON TRIGGER t_rif40_comp_areas_checks ON t_rif40_comparison_areas IS 'INSERT OR UPDATE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_comp_areas_checks()';

CREATE TRIGGER t_rif40_comp_areas_checks_del
	BEFORE DELETE ON t_rif40_comparison_areas
	FOR EACH ROW
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_comp_areas_checks();
COMMENT ON TRIGGER t_rif40_comp_areas_checks_del ON t_rif40_comparison_areas IS 'DELETE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_comp_areas_checks()';

--
-- Eof