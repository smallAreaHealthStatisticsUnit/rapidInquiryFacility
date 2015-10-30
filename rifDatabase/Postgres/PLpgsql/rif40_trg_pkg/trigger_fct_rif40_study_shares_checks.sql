
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

DROP TRIGGER IF EXISTS rif40_study_shares_checks ON "rif40_study_shares" CASCADE;
DROP TRIGGER IF EXISTS rif40_study_shares_checks_del ON "rif40_study_shares" CASCADE;

CREATE OR REPLACE FUNCTION rif40_trg_pkg.trigger_fct_rif40_study_shares_checks() RETURNS trigger AS $BODY$
DECLARE
/*
<trigger_rif40_study_shares_checks_description>
<para>
Check - Username is NOT a RIF user - i.e. username is NOT rif40 and has either or both RIF_USER and RIF_MANAGER.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
Check - Grantor is owner of the study OR has RIF_MANAGER role.
Check - grantee username.
Check - grantee username has RIF_USER or RIF_MANAGER role.
</para>
</trigger_rif40_study_shares_checks_description>
 */
--
-- -20320 to -20339 - RIF40_STUDY_SHARES
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
-- RIF investigattion, inv_covariates, study shares checks + SAHSU land example data
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
	c1stsh CURSOR(l_study_id  integer) IS
		SELECT username, study_id
		  FROM t_rif40_studies
		 WHERE study_id = l_study_id;
	c2stsh CURSOR(l_grantee  varchar) IS
		SELECT rolname
		  FROM pg_roles
		 WHERE rolname = LOWER(l_grantee);
	c4stsh CURSOR IS
		SELECT COUNT(study_id) AS total
		  FROM (
		SELECT study_id
		  FROM t_rif40_results
		 LIMIT 1) a;
	c4stsh_rec RECORD;
	c1stsh_rec RECORD;
	c2stsh_rec RECORD;
BEGIN
--
-- Check for update of study_id
--
	IF TG_OP = 'UPDATE' AND COALESCE(NEW.study_id::Text, '') != COALESCE(OLD.study_id::Text, '') THEN
		PERFORM rif40_log_pkg.rif40_error(-20324, 'trigger_fct_rif40_study_shares_checks', 
			'RIF40_STUDY_SHARES study id may not be changed: %=>%',
			OLD.study_id::VARCHAR	/* Study id */,	
			NEW.study_id::VARCHAR	/* Study id */);
	END IF;
--
	OPEN c4stsh;
	FETCH c4stsh INTO c4stsh_rec;
	CLOSE c4stsh;
--
-- RIF40_STUDY_SHARES:	Check - Username is NOT a RIF user - i.e. username is NOT rif40 and has either or both RIF_USER and RIF_MANAGER
-- 			Check - UPDATE not allowed
--			Check - DELETE only allowed on own records

	IF NOT TG_OP = 'DELETE' AND USER = 'rif40' AND c4stsh_rec.total = 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_rif40_study_shares_checks', 
			'[20320] RIF40_STUDY_SHARES study % username: %: allowed duing build before first result is added to system',
			NEW.study_id::VARCHAR	/* Study id */,
			USER::VARCHAR 		/* User */);
	ELSIF NOT TG_OP = 'DELETE' AND NEW.grantor != USER AND NOT pg_has_role(USER, 'rif_manager', 'USAGE') /* Not granted RIF_MANAGER */ THEN
		PERFORM rif40_log_pkg.rif40_error(-20320, 'trigger_fct_rif40_study_shares_checks', 
			'RIF40_STUDY_SHARES study_id: % grantor username: % is not USER: % or a RIF40_MANAGER',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.grantor::VARCHAR		/* Grantor */,
			USER::VARCHAR			/* User */);
	ELSIF USER != 'rif40' AND NOT (pg_has_role(USER, 'rif_user', 'USAGE') AND pg_has_role(USER, 'rif_manager', 'USAGE')) THEN
		PERFORM rif40_log_pkg.rif40_error(-20321, 'trigger_fct_rif40_study_shares_checks', 
			'RIF40_STUDY_SHARES study_id: % username: % is not a RIF USER',
			NEW.study_id::VARCHAR	/* Study id */,
			USER::VARCHAR			/* User */);
/* Kerberos checks no longer possible 
		PERFORM rif40_log_pkg.rif40_error(-20321, 'rif40_study_shares_checks', 
			'RIF40_STUDY_SHARES study_id: % grantor username: % is not a Kerberos USER: %',
			NEW.study_id::VARCHAR		-* Study id *-,
			NEW.grantor::VARCHAR		-* Grantor *-,
			USER::VARCHAR			-* User *-);  */
	ELSIF TG_OP = 'UPDATE' THEN
		PERFORM rif40_log_pkg.rif40_error(-20322, 'trigger_fct_rif40_study_shares_checks', 
			'RIF40_STUDY_SHARES study_id: % UPDATE not allowed on RIF40_STUDY_SHARES',
			NEW.study_id		/* Study id */);
	ELSIF TG_OP = 'DELETE' AND OLD.grantor != USER AND NOT pg_has_role(USER, 'rif_manager', 'USAGE') /* Not granted RIF_MANAGER */ THEN
		PERFORM rif40_log_pkg.rif40_error(-20323, 'trigger_fct_rif40_study_shares_checks', 
			'RIF40_STUDY_SHARES study_id: % DELETE only allowed on own records or by RIF40_MANAGER in RIF40_STUDY_SHARES, record owned by: %',
			OLD.study_id::VARCHAR		/* Study id */,
			OLD.grantor::VARCHAR		/* INSERTER */);
	END IF;

--
-- End of delete checks
--
	IF TG_OP = 'DELETE' THEN
		RETURN OLD;
	END IF;
     
--
-- Grantor is owner of the study OR has RIF_MANAGER role
--
	OPEN c1stsh(NEW.study_id);
	FETCH c1stsh INTO c1stsh_rec;
	IF c1stsh_rec.study_id IS NULL THEN
		CLOSE c1stsh;
		PERFORM rif40_log_pkg.rif40_error(-20328, 'trigger_fct_rif40_study_shares_checks', 
			'RIF40_STUDY_SHARES study_id: % not found',
			NEW.study_id::VARCHAR			/* Study id */);
	END IF;
	CLOSE c1stsh;
	IF pg_has_role(USER, 'rif_manager', 'USAGE') THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_rif40_study_shares_checks', 
			'[20325] RIF40_STUDY_SHARES study_id: % not owned by grantor: %; owned by: %; but grantor is a RIF_MANAGER',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.grantor::VARCHAR		/* Grantor */,
			c1stsh_rec.username::VARCHAR	/* Study user */);
	ELSIF c1stsh_rec.username != NEW.grantor THEN
		PERFORM rif40_log_pkg.rif40_error(-20325, 'trigger_fct_rif40_study_shares_checks', 
			'RIF40_STUDY_SHARES study_id: % not owned by grantor: %; owned by: %',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.grantor::VARCHAR		/* Grantor */,
			c1stsh_rec.username::VARCHAR	/* Study user */);
	END IF;
--
-- Check - grantee username.
--
	OPEN c2stsh(NEW.grantee_username);
	FETCH c2stsh INTO c2stsh_rec;
	IF c2stsh_rec.rolname IS NULL THEN
		CLOSE c2stsh;
		PERFORM rif40_log_pkg.rif40_error(-20326, 'trigger_fct_rif40_study_shares_checks', 
			'RIF40_STUDY_SHARES study_id: % grantee username: % not a valid database user',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.grantee_username::VARCHAR	/* Grantee username */);
	END IF;
	CLOSE c2stsh;
--
-- Check grantee_username has RIF_USER or RIF_MANAGER role.
--
	IF pg_has_role(NEW.grantee_username, 'rif_user', 'USAGE') OR pg_has_role(NEW.grantee_username, 'rif_manager', 'USAGE') THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_rif40_study_shares_checks', 
			'[20327] RIF40_STUDY_SHARES study_id: % grantee username: % is a RIF_USER/RIF_MANAGER',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.grantee_username::VARCHAR	/* Study user */);
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-20327, 'trigger_fct_rif40_study_shares_checks', 
			'[20327] RIF40_STUDY_SHARES study_id: % grantee username: % is NOT a RIF_USER/RIF_MANAGER',
			NEW.study_id::VARCHAR		/* Study id */,
			NEW.grantee_username::VARCHAR	/* Study user */);
	END IF;
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_rif40_study_shares_checks', 
		'[20320-7] RIF40_STUDY_SHARES study: % CRUD checks OK',
		NEW.study_id::VARCHAR			/* Study id */);
--
	IF TG_OP = 'DELETE' THEN
		RETURN OLD;
	ELSE  	
		RETURN NEW;
	END IF;
END;
$BODY$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.trigger_fct_rif40_study_shares_checks() IS 'Check - Username is NOT a RIF user - i.e. username is NOT rif40 and has either or both RIF_USER and RIF_MANAGER.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
Check - Grantor is owner of the study OR has RIF_MANAGER role.
Check - grantee username.
Check - grantee username has RIF_USER or RIF_MANAGER role.

 -20320 to -20339 - RIF40_STUDY_SHARES';

CREATE TRIGGER rif40_study_shares_checks
	BEFORE INSERT OR UPDATE OF grantor, grantee_username, study_id  ON rif40_study_shares
	FOR EACH ROW
	WHEN ((NEW.grantor IS NOT NULL AND NEW.grantor::text <> '') OR (NEW.grantee_username IS NOT NULL AND NEW.grantee_username::text <> '') OR (NEW.study_id IS NOT NULL AND NEW.study_id::text <> ''))
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_rif40_study_shares_checks();
COMMENT ON TRIGGER rif40_study_shares_checks ON rif40_study_shares IS 'INSERT OR UPDATE trigger: calls rif40_trg_pkg.trigger_fct_rif40_study_shares_checks()';
CREATE TRIGGER rif40_study_shares_checks_del
	BEFORE DELETE ON rif40_study_shares
	FOR EACH ROW
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_rif40_study_shares_checks();
COMMENT ON TRIGGER rif40_study_shares_checks_del ON rif40_study_shares IS 'DELETE trigger: calls rif40_trg_pkg.trigger_fct_rif40_study_shares_checks()';

--
-- Eof