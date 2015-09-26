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
--				  INSERT/UPDATE/DELETE trigger function for T_RIF40_INVESTIGATIONS
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

DROP TRIGGER IF EXISTS t_rif40_investigations_checks_del ON t_rif40_investigations CASCADE;
DROP TRIGGER IF EXISTS t_rif40_investigations_checks ON t_rif40_investigations CASCADE;
CREATE OR REPLACE FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks() RETURNS trigger AS $BODY$
DECLARE
/*
<trigger_t_rif40_investigations_checks_description>
<para>
Check - USERNAME exists.
Check - USERNAME is Kerberos USER on INSERT.
Check - UPDATE only allowed on own records for INVESTIGATION_STATE.
Check - DELETE only allowed on own records.
Check - NUMER_TAB is a valid Oracle name and a numerator, and user has access.
Check - YEAR_START, YEAR_STOP, MAX_AGE_GROUP, MIN_AGE_GROUP.
Check - INV_NAME is a valid Oracle name of 20 characters only.
Check - AGE_GROUP_ID, AGE_SEX_GROUP/AGE_GROUP/SEX_FIELD_NAMES are the same between numerator, denonminator and direct standardisation tables
</para>
<para>Delayed RIF40_TABLES numerator checks: Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists
</para>
</trigger_t_rif40_investigations_checks_description>
 */
--
-- Error range: -20700 to -20759 - T_RIF40_INVESTIGATIONS
--
-- $Author: peterh $
-- $timestamp: 2012/10/23 09:05:57 $
-- Type: PL/SQL trigger
-- $RCSfile: v4_0_postgres_triggers.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/psql_scripts/v4_0_postgres_triggers.sql,v $
-- $Revision: 1.6 $
-- $State: Exp $
-- $Locker:  $
-- $Id$
--
	c1_ick CURSOR(l_table_name  varchar) FOR
		SELECT a.year_start, a.year_stop, a.isindirectdenominator, a.isdirectdenominator, a.isnumerator,
		       a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field, a.age_group_id,
                       MIN(g.offset) min_age_group, MAX(g.offset) max_age_group
		  FROM rif40_tables a
			LEFT OUTER JOIN rif40_age_groups g ON (g.age_group_id  = a.age_group_id)
		 WHERE table_name = l_table_name
		 GROUP BY a.year_start, a.year_stop, a.isindirectdenominator, a.isdirectdenominator, a.isnumerator,
		          a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field, a.age_group_id;
	c1a_rec RECORD; /* Numerator */
	c1b_rec RECORD; /* Denominator */
	c1c_rec RECORD; /* Direct standardisation table */
	c2_ick CURSOR(l_study_id  integer) FOR
		SELECT *
		  FROM t_rif40_studies
		 WHERE study_id = l_study_id;
	c2_rec RECORD;
	c3_ick CURSOR (l_schema  varchar, l_table  varchar, l_column  varchar) FOR
		SELECT column_name
		  FROM information_schema.columns
		 WHERE table_schema = LOWER(l_schema)
		   AND table_name   = LOWER(l_table)
		   AND column_name  = LOWER(l_column);
	c3_rec RECORD;
--
	c4_ick CURSOR IS
		SELECT COUNT(study_id) AS total
		  FROM (
		SELECT study_id
		  FROM t_rif40_results
		 LIMIT 1) a;
	c4_rec RECORD;
--
	table_or_view	varchar(30);
	owner		varchar(30);
--
	investigation_state_only_flag 	boolean:=FALSE;
BEGIN
--
-- T_RIF40_INVESTIGATIONS: Check - USERNAME is Kerberos USER on INSERT
--			Check - audsid is SYS_CONTEXT('USERENV', 'SESSIONID') on INSERT
-- 			Check - UPDATE only allowed on own records for INVESTIGATION_STATE
--			Check - DELETE only allowed on own records
	IF TG_OP = 'UPDATE' AND  NEW.username = OLD.username AND
 	   NEW.inv_name = OLD.inv_name AND
 	   NEW.inv_description = OLD.inv_description AND
 	   NEW.year_start = OLD.year_start AND
 	   NEW.year_stop = OLD.year_stop AND
 	   NEW.max_age_group = OLD.max_age_group AND
 	   NEW.min_age_group = OLD.min_age_group AND
 	   NEW.genders = OLD.genders AND
 	   NEW.numer_tab = OLD.numer_tab AND
 --	   NEW.geography = OLD.geography AND
 	   NEW.study_id = OLD.study_id AND
 	   NEW.inv_id = OLD.inv_id AND
 	   NEW.classifier = OLD.classifier AND
 	   NEW.classifier_bands = OLD.classifier_bands AND
 	   NEW.mh_test_type = OLD.mh_test_type  AND
 	   NEW.investigation_state != OLD.investigation_state THEN
		investigation_state_only_flag:=TRUE;
	END IF;
--
	OPEN c4_ick;
	FETCH c4_ick INTO c4_rec;
	CLOSE c4_ick;
	IF TG_OP = 'INSERT' AND NEW.username != USER THEN
		IF USER = 'rif40' AND c4_rec.total = 0 THEN 
			/* Allowed during build before first result is added to system or before Kerberos update */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20700, 'trigger_fct_t_rif40_investigations_checks',
				'T_RIF40_INVESTIGATIONS study: % investigation: % username: % is not USER: %',
				NEW.study_id::VARCHAR	/* Study */,
				NEW.inv_id::VARCHAR	/* Investigation */,
				NEW.username::VARCHAR	/* Record username */,
				USER::VARCHAR		/* Username */);
		END IF;
	ELSIF TG_OP = 'UPDATE' AND NEW.username != USER THEN
		IF (USER = 'rif40' AND (c4_rec.total = 0 OR strpos(OLD.username, '@PRIVATE.NET') > 0)) THEN 
			/* Allowed during build before first result is added to system or before Kerberos update */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20700, 'trigger_fct_t_rif40_investigations_checks',
				'T_RIF40_INVESTIGATIONS study: % investigation: % new username: % is not USER: %; Old: %',
				NEW.study_id::VARCHAR	/* Study */,
				NEW.inv_id::VARCHAR	/* Investigation */,
				NEW.username::VARCHAR	/* Record username */,
				USER::VARCHAR		/* Username */,
				OLD.username::VARCHAR	/* Old username */);
		END IF;
/*	ELSIF INSTR(NEW.username, '@PRIVATE.NET') = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-20701, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % username: % is not a Kerberos USER: %',
			NEW.study_id::VARCHAR	/- Study -/,
			NEW.inv_id::VARCHAR	/- Investigation -/,
			NEW.username::VARCHAR	/- Record username -/,
			USER::VARCHAR		/- User -/); */
	ELSIF TG_OP = 'UPDATE'  AND OLD.username != USER  THEN
		PERFORM rif40_log_pkg.rif40_error(-20702, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % UPDATE only allowed on own records, record owned by: %, user: %',
			NEW.study_id::VARCHAR	/* Study */,
			NEW.inv_id::VARCHAR	/* Investigation */,
			OLD.username::VARCHAR	/* creator username */,
			USER::VARCHAR		/* User */);
	ELSIF TG_OP = 'UPDATE'  AND OLD.username = USER AND investigation_state_only_flag = FALSE THEN
		PERFORM rif40_log_pkg.rif40_error(-20703, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % UPDATE only allowed for T_RIF40_INVESTIGATIONS.INVESTIGATION_STATE',
			NEW.study_id::VARCHAR	/* Study */,
			NEW.inv_id::VARCHAR	/* Investigation */);
	ELSIF TG_OP = 'DELETE' AND OLD.username != USER THEN
		PERFORM rif40_log_pkg.rif40_error(-20704, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % DELETE only allowed on own records in T_RIF40_INVESTIGATIONS, record owned by: %, user: %',
			OLD.study_id::VARCHAR	/* Study */,
			OLD.inv_id::VARCHAR	/* Investigation */,
			OLD.username::VARCHAR	/* creator username */,
			USER::VARCHAR		/* User */);
	END IF;

--
-- End of delete checks
--
	IF TG_OP = 'DELETE' THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_investigations_checks',
   	     		'[20700-4] T_RIF40_INVESTIGATIONS study: % investigation: % CRUD checks OK',
			OLD.study_id::VARCHAR	/* Study id */,
			OLD.inv_id::VARCHAR	/* Investigation */);
		RETURN OLD;
	END IF;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_investigations_checks',
		'[20700-4] T_RIF40_INVESTIGATIONS study: % investigation: % CRUD checks OK',
		NEW.study_id::VARCHAR	/* Study id */,
		NEW.inv_id::VARCHAR	/* Investigation */);
 
--
-- Check - NUMER_TAB is a valid Oracle name and a numerator, and user has access.
--
	PERFORM rif40_trg_pkg.rif40_db_name_check('NUMER_TAB', NEW.numer_tab);
--
	OPEN c1_ick(NEW.numer_tab);
	FETCH c1_ick INTO c1a_rec; /* RIF40_TABLES numerator settings */
	CLOSE c1_ick;
	OPEN c2_ick(NEW.study_id);
	FETCH c2_ick INTO c2_rec;
	CLOSE c2_ick;
	IF c1a_rec.isnumerator != 1 THEN
		PERFORM rif40_log_pkg.rif40_error(-20705, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % is not a numerator table',
			NEW.study_id::VARCHAR	/* Study */,
			NEW.inv_id::VARCHAR	/* Investigation */,
			NEW.numer_tab::VARCHAR	/* Numerator */);
	END IF;
	owner:=rif40_sql_pkg.rif40_object_resolve(NEW.numer_tab::VARCHAR);
	table_or_view:=NEW.numer_tab::VARCHAR;
	IF coalesce(owner::text, '') = '' THEN
		IF USER = 'rif40' AND (c4_rec.total = 0 OR strpos(OLD.username, '@PRIVATE.NET') > 0) THEN 
			/* Allowed duing build before first result is added to system or when converting Kerberos users */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20706, 'trigger_fct_t_rif40_investigations_checks',
				'T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % cannot be accessed',
				NEW.study_id::VARCHAR	/* Study */,
				NEW.inv_id::VARCHAR	/* Investigation */,
				NEW.numer_tab::VARCHAR	/* Numerator */);
		END IF;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_investigations_checks',
			'[20706] T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % accessible',
			NEW.study_id::VARCHAR	/* Study id */,
			NEW.inv_id::VARCHAR	/* Investigation */,
			NEW.numer_tab::VARCHAR 	/* map_table */);
	END IF;

--
-- Check - YEAR_START, YEAR_STOP, MAX_AGE_GROUP, MIN_AGE_GROUP.
--
	IF (c1a_rec.year_start IS NOT NULL AND c1a_rec.year_start::text <> '') AND NEW.YEAR_START < c1a_rec.year_start  THEN
		PERFORM rif40_log_pkg.rif40_error(-20707, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % year start: % is before RIF40_TABLES year start: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.numer_tab::VARCHAR			/* Numerator */,
			NEW.year_start::VARCHAR			/* Investigation year start */,
			c1a_rec.year_start::VARCHAR		/* RIF40_TABLES year start */);
	END IF;
	IF (c1a_rec.year_stop IS NOT NULL AND c1a_rec.year_stop::text <> '') AND NEW.YEAR_START > c1a_rec.year_stop  THEN
		PERFORM rif40_log_pkg.rif40_error(-20708, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % year stop: % is after RIF40_TABLES year stop: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.numer_tab::VARCHAR			/* Numerator */,
			NEW.year_stop::VARCHAR			/* Investigation year stop */,
			c1a_rec.year_stop::VARCHAR		/* RIF40_TABLES year stop */);
	END IF;
/*
Year_stop/start, min/max_age_group removed from t_rif40_studies. Still in view

	IF (c2_rec.year_start IS NOT NULL AND c2_rec.year_start::text <> '') AND NEW.YEAR_START < c2_rec.year_start  THEN
		PERFORM rif40_log_pkg.rif40_error(-20709, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % year start: % is before T_RIF40_STUDIES year start: %',
			NEW.study_id::VARCHAR			/- Study -/,
			NEW.inv_id::VARCHAR			/- Investigation -/,
			NEW.numer_tab::VARCHAR			/- Numerator -/,
			NEW.year_start::VARCHAR			/- Investigation year start -/,
			c2_rec.year_start::VARCHAR		/- T_RIF40_STUDIES year start -/);
	END IF;
	IF (c2_rec.year_stop IS NOT NULL AND c2_rec.year_stop::text <> '') AND NEW.YEAR_START > c2_rec.year_stop  THEN
		PERFORM rif40_log_pkg.rif40_error(-20710, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % year stop: % is after T_RIF40_STUDIES year stop: %',
			NEW.study_id::VARCHAR			/- Study -/,
			NEW.inv_id::VARCHAR			/- Investigation -/,
			NEW.numer_tab::VARCHAR			/- Numerator -/,
			NEW.year_stop::VARCHAR			/- Investigation year stop -/,
			c2_rec.year_stop::VARCHAR		/- T_RIF40_STUDIES year stop -/);
	END IF;
 */
	IF (c1a_rec.min_age_group IS NOT NULL AND c1a_rec.min_age_group::text <> '') AND NEW.min_age_group < c1a_rec.min_age_group  THEN
		PERFORM rif40_log_pkg.rif40_error(-20711, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % min age group: % is before RIF40_TABLES min age group: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.numer_tab::VARCHAR			/* Numerator */,
			NEW.min_age_group::VARCHAR		/* Investigation min age group */,
			c1a_rec.min_age_group::VARCHAR		/* RIF40_TABLES min age group */);
	END IF;
/*
Year_stop/start, min/max_age_group removed from t_rif40_studies. Still in view

	IF (c2_rec.max_age_group IS NOT NULL AND c2_rec.max_age_group::text <> '') AND NEW.max_age_group > c2_rec.max_age_group  THEN
		PERFORM rif40_log_pkg.rif40_error(-20712, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator:% max age group: % is after RIF40_TABLES max age group: %',
			NEW.study_id::VARCHAR			/- Study -/,
			NEW.inv_id::VARCHAR			/- Investigation -/,
			NEW.numer_tab::VARCHAR			/- Numerator -/,
			NEW.max_age_group::VARCHAR		/- Investigation max age group -/,
			c1a_rec.max_age_group::VARCHAR		/- RIF40_TABLES max age group -/);
	END IF;
	IF (c2_rec.min_age_group IS NOT NULL AND c2_rec.min_age_group::text <> '') AND NEW.min_age_group < c2_rec.min_age_group  THEN
		PERFORM rif40_log_pkg.rif40_error(-20713, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % min age group: % is before T_RIF40_STUDIES min age group: %',
			NEW.study_id::VARCHAR			/- Study -/,
			NEW.inv_id::VARCHAR			/- Investigation -/,
			NEW.numer_tab::VARCHAR			/- Numerator -/,
			NEW.min_age_group::VARCHAR		/- Investigation min age group -/,
			c2_rec.min_age_group::VARCHAR		/- T_RIF40_STUDIES min age group -/);
	END IF;
 */
	IF (c1a_rec.max_age_group IS NOT NULL AND c1a_rec.max_age_group::text <> '') AND NEW.max_age_group > c1a_rec.max_age_group  THEN
		PERFORM rif40_log_pkg.rif40_error(-20714, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator:% max age group: % is after T_RIF40_STUDIES max age group: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.numer_tab::VARCHAR			/* Numerator */,
			NEW.max_age_group::VARCHAR		/* Investigation max age group */,
			c2_rec.max_age_group::VARCHAR		/* T_RIF40_STUDIES max age group */);
	END IF;

	IF coalesce(c1a_rec.max_age_group::text, '') = '' OR coalesce(c1a_rec.min_age_group::text, '') = '' THEN
		PERFORM rif40_log_pkg.rif40_error(-20715, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % numerator: % no age group linkage',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.numer_tab::VARCHAR			/* Numerator */);
	END IF;


	IF NEW.min_age_group > NEW.max_age_group THEN
		PERFORM rif40_log_pkg.rif40_error(-20716, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % min age group: % is after max age group: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.min_age_group::VARCHAR		/* min age group*/,
			NEW.max_age_group::VARCHAR		/* max age group */);
	END IF;
	IF NEW.year_start > NEW.year_stop THEN
		PERFORM rif40_log_pkg.rif40_error(-20717, 'trigger_fct_t_rif40_investigations_checks',
			'T_RIF40_INVESTIGATIONS study: % investigation: % year stop: % is after year stop: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.year_start::VARCHAR			/* Year start */,
			NEW.year_stop::VARCHAR			/* Year stop */);
	END IF;

	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_investigations_checks',
		'[20707-17] T_RIF40_INVESTIGATIONS study: % investigation: % year/age checks OK',
		NEW.study_id::VARCHAR	/* Study id */,
		NEW.inv_id::VARCHAR	/* Investigation */);

--
-- Check - INV_NAME is a valid Oracle name (of 20 chaacters only - is an old ORacle restriction).
--
	PERFORM rif40_trg_pkg.rif40_db_name_check('INV_NAME', NEW.inv_name);
--	IF LENGTH(NEW.inv_name) > 20 THEN
--	END IF;

--
-- Delayed RIF40_TABLES numerator checks:
-- Check - Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists
--
	IF c4_rec.total > 0 AND owner IS NOT NULL AND table_or_view IS NOT NULL THEN /* Not first insert */
		IF (c1a_rec.total_field IS NOT NULL AND c1a_rec.total_field::text <> '') THEN
			OPEN c3_ick(owner, table_or_view, c1a_rec.total_field);
			FETCH c3_ick INTO c3_rec;
			IF NOT FOUND THEN
				CLOSE c3_ick;
				PERFORM rif40_log_pkg.rif40_error(-20718, 'trigger_fct_t_rif40_investigations_checks',
					'T_RIF40_INVESTIGATIONS study: % investigation: % numerator RIF40_TABLES total field column: %.%.% NOT found',
					NEW.study_id::VARCHAR			/* Study */,
					NEW.inv_id::VARCHAR			/* Investigation */,
					owner::VARCHAR				/* Numerator owner */,
					table_or_view::VARCHAR			/* Numerator */,
					c1a_rec.total_field::VARCHAR		/* Total field name */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_investigations_checks',
					'[20718] T_RIF40_INVESTIGATIONS study: % investigation: % numerator RIF40_TABLES total field column: %.%.% found',
					NEW.study_id::VARCHAR			/* Study */,
					NEW.inv_id::VARCHAR			/* Investigation */,
					owner::VARCHAR				/* Numerator owner */,
					table_or_view::VARCHAR			/* Numerator */,
					c1a_rec.total_field::VARCHAR		/* Total field name */);
			END IF;
			CLOSE c3_ick;
		END IF;
		IF (c1a_rec.sex_field_name IS NOT NULL AND c1a_rec.sex_field_name::text <> '') THEN
			OPEN c3_ick(owner, table_or_view, c1a_rec.sex_field_name);
			FETCH c3_ick INTO c3_rec;
			IF NOT FOUND THEN
				CLOSE c3_ick;
				PERFORM rif40_log_pkg.rif40_error(-20719, 'trigger_fct_t_rif40_investigations_checks',
					'T_RIF40_INVESTIGATIONS study: % investigation: % numerator RIF40_TABLES sex field column: %.%.% NOT found',
					NEW.study_id::VARCHAR			/* Study */,
					NEW.inv_id::VARCHAR			/* Investigation */,
					owner::VARCHAR				/* Numerator owner */,
					table_or_view::VARCHAR			/* Numerator */,
					c1a_rec.sex_field_name::VARCHAR		/* Sex field name */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', '[20719] T_RIF40_INVESTIGATIONS study: % investigation: % numerator RIF40_TABLES sex field column: %.%.% found',
					NEW.study_id::VARCHAR			/* Study */,
					NEW.inv_id::VARCHAR			/* Investigation */,
					owner::VARCHAR				/* Numerator owner */,
					table_or_view::VARCHAR			/* Numerator */,
					c1a_rec.sex_field_name::VARCHAR		/* Sex field name */);
			END IF;
			CLOSE c3_ick;
		END IF;
		IF (c1a_rec.age_group_field_name IS NOT NULL AND c1a_rec.age_group_field_name::text <> '') THEN
			OPEN c3_ick(owner, table_or_view, c1a_rec.age_group_field_name);
			FETCH c3_ick INTO c3_rec;
			IF NOT FOUND THEN
				CLOSE c3_ick;
				PERFORM rif40_log_pkg.rif40_error(-20720, 'trigger_fct_t_rif40_investigations_checks',
					'T_RIF40_INVESTIGATIONS study: % investigation: % numerator RIF40_TABLES age group field name column: %.%.% NOT found',
					NEW.study_id::VARCHAR			/* Study */,
					NEW.inv_id::VARCHAR			/* Investigation */,
					owner::VARCHAR				/* Numerator owner */,
					table_or_view::VARCHAR			/* Numerator */,
					c1a_rec.age_group_field_name::VARCHAR	/* Age group field name */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_investigations_checks',
					'[20720] T_RIF40_INVESTIGATIONS study: % investigation: % numerator RIF40_TABLES age group field name column: %.%.% found',
					NEW.study_id::VARCHAR			/* Study */,
					NEW.inv_id::VARCHAR			/* Investigation */,
					owner::VARCHAR				/* Numerator owner */,
					table_or_view::VARCHAR			/* Numerator */,
					c1a_rec.age_group_field_name::VARCHAR	/* Age group field name */);
			END IF;
			CLOSE c3_ick;
		END IF;
		IF (c1a_rec.age_sex_group_field_name IS NOT NULL AND c1a_rec.age_sex_group_field_name::text <> '') THEN
			OPEN c3_ick(owner, table_or_view, c1a_rec.age_sex_group_field_name);
			FETCH c3_ick INTO c3_rec;
			IF NOT FOUND THEN
				CLOSE c3_ick;
				PERFORM rif40_log_pkg.rif40_error(-20721, 'trigger_fct_t_rif40_investigations_checks',
					'T_RIF40_INVESTIGATIONS study: % investigation: % numerator RIF40_TABLES age sex group field column: %.%.% NOT found',
					NEW.study_id::VARCHAR				/* Study */,
					NEW.inv_id::VARCHAR				/* Investigation */,
					owner::VARCHAR					/* Numerator owner */,
					table_or_view::VARCHAR				/* Numerator */,
					c1a_rec.age_sex_group_field_name::VARCHAR	/* Age sex group field name */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_investigations_checks',
					'[20721] T_RIF40_INVESTIGATIONS study: % investigation: % numerator RIF40_TABLES age sex group field column: %.%.% found',
					NEW.study_id::VARCHAR				/* Study */,
					NEW.inv_id::VARCHAR				/* Investigation */,
					owner::VARCHAR					/* Numerator owner */,
					table_or_view::VARCHAR				/* Numerator */,
					c1a_rec.age_sex_group_field_name::VARCHAR	/* Age sex group field name */);
			END IF;
			CLOSE c3_ick;
		END IF;
	END IF;

--
-- Verify AGE_GROUP_ID, AGE_SEX_GROUP/AGE_GROUP/SEX_FIELD_NAMES are the same between numerator, denonminator and direct standardisation tables
--
	OPEN c1_ick(c2_rec.denom_tab);
	FETCH c1_ick INTO c1b_rec; /* RIF40_TABLES denominator settings */
	CLOSE c1_ick;
	IF (c2_rec.direct_stand_tab IS NOT NULL AND c2_rec.direct_stand_tab::text <> '') THEN
		OPEN c1_ick(c2_rec.direct_stand_tab);
		FETCH c1_ick INTO c1c_rec; /* RIF40_TABLES Direct standardisation table settings */
		CLOSE c1_ick;
		IF c1a_rec.age_group_id != c1b_rec.age_group_id OR c1a_rec.age_group_id != c1c_rec.age_group_id THEN
			PERFORM rif40_log_pkg.rif40_error(-20722, 'trigger_fct_t_rif40_investigations_checks',
				'T_RIF40_INVESTIGATIONS study: % investigation: % age_group ID mismatch; numerator %, denominator:: %, direct standardisation table: %',
				NEW.study_id::VARCHAR		/* Study */,
				NEW.inv_id::VARCHAR		/* Investigation */,
				c1a_rec.age_group_id::VARCHAR	/* Numerator age group ID */,
				c1a_rec.age_group_id::VARCHAR	/* Denominator age group ID */,
				c1b_rec.age_group_id::VARCHAR	/* Direct standardisation age group ID */);
		END IF;
		IF ((c1a_rec.age_sex_group_field_name IS NOT NULL AND c1a_rec.age_sex_group_field_name::text <> '') AND 
		    (c1b_rec.age_sex_group_field_name IS NOT NULL AND c1b_rec.age_sex_group_field_name::text <> '') AND
		    (c1c_rec.age_sex_group_field_name IS NOT NULL AND c1c_rec.age_sex_group_field_name::text <> '')) OR
           (
		     ((c1a_rec.age_group_field_name IS NOT NULL AND c1a_rec.age_group_field_name::text <> '') AND 
			  (c1b_rec.age_group_field_name IS NOT NULL AND c1b_rec.age_group_field_name::text <> '') AND 
			  (c1c_rec.age_group_field_name IS NOT NULL AND c1c_rec.age_group_field_name::text <> '')) AND
		      ((c1a_rec.sex_field_name IS NOT NULL AND c1a_rec.sex_field_name::text <> '') AND 
			   (c1b_rec.sex_field_name IS NOT NULL AND c1b_rec.sex_field_name::text <> '') AND 
			   (c1c_rec.sex_field_name IS NOT NULL AND c1c_rec.sex_field_name::text <> ''))) THEN
			NULL;

		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20723, 'trigger_fct_t_rif40_investigations_checks',
				'T_RIF40_INVESTIGATIONS study: % investigation: % differing AGE_SEX_GROUP/AGE_GROUP/SEX_FIELD_NAMES used',
				NEW.study_id::VARCHAR		/* Study */,
				NEW.inv_id::VARCHAR		/* Investigation */);
		END IF;
	ELSE
		IF c1a_rec.age_group_id != c1b_rec.age_group_id  THEN
			PERFORM rif40_log_pkg.rif40_error(-20724, 'trigger_fct_t_rif40_investigations_checks',
				'T_RIF40_INVESTIGATIONS study: % investigation: % age_group ID mismatch; numerator %, denominator: %',
				NEW.study_id::VARCHAR		/* Study */,
				NEW.inv_id::VARCHAR		/* Investigation */,
				c1a_rec.age_group_id::VARCHAR	/* Numerator age group ID */,
				c1a_rec.age_group_id::VARCHAR	/* Denominator age group ID */);
		END IF;
		IF ((c1a_rec.age_sex_group_field_name IS NOT NULL AND c1a_rec.age_sex_group_field_name::text <> '') AND (c1b_rec.age_sex_group_field_name IS NOT NULL AND c1b_rec.age_sex_group_field_name::text <> '')) OR
                   (
		     ((c1a_rec.age_group_field_name IS NOT NULL AND c1a_rec.age_group_field_name::text <> '') AND (c1b_rec.age_group_field_name IS NOT NULL AND c1b_rec.age_group_field_name::text <> '')) AND
		     ((c1a_rec.sex_field_name IS NOT NULL AND c1a_rec.sex_field_name::text <> '') AND (c1b_rec.sex_field_name IS NOT NULL AND c1b_rec.sex_field_name::text <> ''))) THEN
			NULL;

		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20725, 'trigger_fct_t_rif40_investigations_checks',
				'T_RIF40_INVESTIGATIONS study: % investigation: %'||E'\n'||
				'differing AGE_SEX_GROUP/AGE_GROUP/SEX_FIELD_NAMES used, studies: %/%/%'||E'\n'||
				'differing AGE_SEX_GROUP/AGE_GROUP/SEX_FIELD_NAMES used, denominator: %/%/%',
				NEW.study_id::VARCHAR		/* Study */,
				NEW.inv_id::VARCHAR			/* Investigation */,
				COALESCE(c1a_rec.age_sex_group_field_name, 'Not set ')::VARCHAR 	/* Age sex group field - studies */,
				COALESCE(c1a_rec.age_group_field_name, 'Not set ')::VARCHAR 		/* Age group field - studies */,
				COALESCE(c1a_rec.sex_field_name, 'Not set ')::VARCHAR 				/* Sex field - studies */,
				COALESCE(c1b_rec.age_sex_group_field_name, 'Not set ')::VARCHAR 	/* Age sex group field - denominator */,
				COALESCE(c1b_rec.age_group_field_name, 'Not set ')::VARCHAR 		/* Age group field - denominator */,
				COALESCE(c1b_rec.sex_field_name, 'Not set ')::VARCHAR 				/* Sex field - denominator */);
		END IF;
	END IF;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_investigations_checks',
		'[20722-5] T_RIF40_INVESTIGATIONS study: % investigation: % age_group IDs match; %, same AGE_SEX_GROUP/AGE_GROUP/SEX_FIELD_NAMES used',
		NEW.study_id::VARCHAR		/* Study */,
		NEW.inv_id::VARCHAR		/* Investigation */,
		c1a_rec.age_group_id::VARCHAR	/* Numerator age group ID */);

--
-- Error message end: -20759, last message: -20725
--
--
	IF TG_OP = 'DELETE' THEN
		RETURN OLD;
	ELSE  	
		RETURN NEW;
	END IF;
END;
$BODY$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks() IS 'Check - USERNAME exists.
Check - USERNAME is Kerberos USER on INSERT.
Check - UPDATE only allowed on own records for INVESTIGATION_STATE.
Check - DELETE only allowed on own records.
Check - NUMER_TAB is a valid Oracle name and a numerator, and user has access.
Check - YEAR_START, YEAR_STOP, MAX_AGE_GROUP, MIN_AGE_GROUP.
Check - INV_NAME is a valid Oracle name of 20 characters only.
Check - AGE_GROUP_ID, AGE_SEX_GROUP/AGE_GROUP/SEX_FIELD_NAMES are the same between numerator, denonminator and direct standardisation tables

Delayed RIF40_TABLES numerator checks: Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists';

CREATE TRIGGER t_rif40_investigations_checks
	BEFORE INSERT OR UPDATE OF username, inv_name, inv_description, year_start, year_stop,
		max_age_group, min_age_group, genders, numer_tab, investigation_state,
	        /* geography, */ study_id, inv_id, classifier, classifier_bands, mh_test_type ON t_rif40_investigations
	FOR EACH ROW	
	WHEN (	(NEW.username IS NOT NULL AND NEW.username::text <> '') OR 
		(NEW.inv_name IS NOT NULL AND NEW.inv_name::text <> '') OR 
		(NEW.inv_description IS NOT NULL AND NEW.inv_description::text <> '') OR
		(NEW.year_start IS NOT NULL AND NEW.year_start::text <> '') OR 
		(NEW.year_stop IS NOT NULL AND NEW.year_stop::text <> '') OR 
		(NEW.max_age_group IS NOT NULL AND NEW.max_age_group::text <> '') OR 
		(NEW.min_age_group IS NOT NULL AND NEW.min_age_group::text <> '') OR
		(NEW.genders IS NOT NULL AND NEW.genders::text <> '') OR 
		(NEW.investigation_state IS NOT NULL AND NEW.investigation_state::text <> '') OR 
		(NEW.numer_tab IS NOT NULL AND NEW.numer_tab::text <> '') OR
		/* (NEW.geography IS NOT NULL AND NEW.geography::text <> '') OR */ 
		(NEW.study_id IS NOT NULL AND NEW.study_id::text <> '') OR 
		(NEW.inv_id IS NOT NULL AND NEW.inv_id::text <> '') OR
		(NEW.classifier IS NOT NULL AND NEW.classifier::text <> '') OR 
		(NEW.classifier_bands IS NOT NULL AND NEW.classifier_bands::text <> '') OR 
		(NEW.mh_test_type IS NOT NULL AND NEW.mh_test_type::text <> ''))
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks();
COMMENT ON TRIGGER t_rif40_investigations_checks ON t_rif40_investigations IS 'INSERT OR UPDATE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks()';

CREATE TRIGGER t_rif40_investigations_checks_del
	BEFORE DELETE  ON t_rif40_investigations
	FOR EACH ROW
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks();
COMMENT ON TRIGGER t_rif40_investigations_checks ON t_rif40_investigations IS 'DELETE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks()';

--
-- Eof
