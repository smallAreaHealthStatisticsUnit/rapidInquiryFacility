
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

DROP TRIGGER IF EXISTS t_rif40_study_areas_checks ON t_rif40_study_areas CASCADE;
DROP TRIGGER IF EXISTS t_rif40_study_areas_checks_del ON t_rif40_study_areas CASCADE;
CREATE OR REPLACE FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_study_areas_checks() RETURNS trigger AS $BODY$
DECLARE
/*
<trigger_t_rif40_study_areas_checks_description>
<para>
Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
</para>
</trigger_t_rif40_study_areas_checks_description>
 */
--
-- -20280 to -20299 - T_RIF40_STUDY_AREAS
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
	c4_cksa CURSOR IS
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
		PERFORM rif40_log_pkg.rif40_error(-20284, 'trigger_fct_t_rif40_study_areas_checks', 
			'T_RIF40_STUDY_AREAS study id may not be changed: %=>%',
			OLD.study_id::VARCHAR	/* Study id */,	
			NEW.study_id::VARCHAR	/* Study id */);
	END IF;
--
-- T_RIF40_STUDY_AREAS: Check - USERNAME is Kerberos USER on INSERT
--			Check - audsid is SYS_CONTEXT('USERENV', 'SESSIONID') on INSERT
-- 			Check - UPDATE not allowed
--			Check - DELETE only allowed on own records
--
	OPEN c4_cksa;
	FETCH c4_cksa INTO c4_rec;
	CLOSE c4_cksa;
	IF NOT TG_OP = 'DELETE' AND NEW.username != USER THEN
		IF USER = 'rif40' AND (c4_rec.total = 0 OR strpos(OLD.username, '@PRIVATE.NET') > 0) THEN 
			/* Allowed duing build before first result is added to system or when converting Kerberos users */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20280,  'trigger_fct_t_rif40_study_areas_checks',
				'T_RIF40_STUDY_AREAS study: % area_id: % band_id: % username: % is not USER: %',
				NEW.study_id::VARCHAR		/* Study id */,
				NEW.area_id::VARCHAR		/* Area */,
			 	NEW.band_id::VARCHAR		/* Band */,
				NEW.username::VARCHAR		/* Record username */,
				USER::VARCHAR			/* Logon username */);
		END IF;
/*	ELSIF INSTR(NEW.username, '@PRIVATE.NET') = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-20281,  't_rif40_study_areas_checks',
			'T_RIF40_STUDY_AREAS study: % area_id: % band_id: % username: % is not a Kerberos USER: %',
			NEW.study_id::VARCHAR		/- Study id -/,
			NEW.area_id::VARCHAR		/- Area -/,
			NEW.band_id::VARCHAR		/- Band -/,
			NEW.username::VARCHAR 		/- Record username -/,
			USER::VARCHAR			/- Logon username -/); */
	ELSIF TG_OP = 'UPDATE' THEN
		PERFORM rif40_log_pkg.rif40_error(-20282,  'trigger_fct_t_rif40_study_areas_checks',
			'T_RIF40_STUDY_AREAS UPDATE study: % area_id: % band_id: % not allowed on T_RIF40_STUDY_AREAS',
			NEW.study_id::VARCHAR		/* Study id */);
	ELSIF TG_OP = 'DELETE' AND OLD.username != USER THEN
		PERFORM rif40_log_pkg.rif40_error(-20283,  'trigger_fct_t_rif40_study_areas_checks',
			'T_RIF40_STUDY_AREAS DELETE study: % area_id: % band_id: % only allowed on own records in T_RIF40_STUDY_AREAS, record owned by: %',
			OLD.study_id::VARCHAR		/* Study id */,
			OLD.area_id::VARCHAR		/* Area */,
			OLD.band_id::VARCHAR		/* Band */,
			OLD.username::VARCHAR		/* INSERT username */);
	END IF;
--
--	etp:=clock_timestamp();
	IF TG_OP = 'DELETE' THEN
--
--		PERFORM rif40_log_pkg.rif40_log('DEBUG4', 'trigger_fct_t_rif40_study_areas_checks',
--	     	 	'[20280-3] T_RIF40_STUDY_AREAS study: % area_id: % band_id % CRUD checks OK',
--			OLD.study_id::VARCHAR		/* Study id */,
--			OLD.area_id::VARCHAR		/* Area */,
--		 	OLD.band_id::VARCHAR		/* Band */);
--
		RETURN OLD;
	ELSE  	
--
--		PERFORM rif40_log_pkg.rif40_log('DEBUG4', 'trigger_fct_t_rif40_study_areas_checks',
--			'[20280-3] T_RIF40_STUDY_AREAS study: % area_id: % band_id % CRUD checks OK',
--			NEW.study_id::VARCHAR		/* Study id */,
--			NEW.area_id::VARCHAR		/* Area */,
--		 	NEW.band_id::VARCHAR		/* Band */);
--
		RETURN NEW;
	END IF;
END;
$BODY$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_study_areas_checks() IS 'Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.';

CREATE TRIGGER t_rif40_study_areas_checks
	BEFORE INSERT OR UPDATE OF area_id, band_id, username, study_id ON t_rif40_study_areas
	FOR EACH ROW
	WHEN ((NEW.band_id IS NOT NULL AND NEW.band_id::text <> '') OR (NEW.username IS NOT NULL AND NEW.username::text <> '') OR (NEW.study_id IS NOT NULL AND NEW.study_id::text <> ''))
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_study_areas_checks();
COMMENT ON TRIGGER t_rif40_study_areas_checks ON t_rif40_study_areas IS 'INSERT OR UPDATE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_study_areas_checks()';
CREATE TRIGGER t_rif40_study_areas_checks_del
	BEFORE DELETE ON t_rif40_study_areas
	FOR EACH ROW
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_study_areas_checks();
COMMENT ON TRIGGER t_rif40_study_areas_checks_del ON t_rif40_study_areas IS 'DELETE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_study_areas_checks()';

--
-- Eof