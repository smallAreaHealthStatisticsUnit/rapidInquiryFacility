
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

DROP TRIGGER IF EXISTS t_rif40_inv_covariates_checks ON "t_rif40_inv_covariates" CASCADE;
DROP TRIGGER IF EXISTS t_rif40_inv_covariates_checks_del ON "t_rif40_inv_covariates" CASCADE;

CREATE OR REPLACE FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_inv_covariates_checks() RETURNS trigger AS $BODY$
DECLARE
/*
<trigger_t_rif40_inv_covariates_checks_description>
<para>
Check - USERNAME exists.
Check - USERNAME is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
Check - study_geolevel_name.
Check - Covariates a) MIN and MAX.  b) Limits c) Check access to covariate table, <covariate name> column exists d) Check score.
</para>
</trigger_t_rif40_inv_covariates_checks_description>
 */
--
-- Error range: -20260 to -20279 - T_RIF40_INV_COVARIATES
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
	c1_ckicov CURSOR (l_geography  varchar, l_geolevel_name  varchar, l_covariate_name  varchar) FOR
		SELECT *
		  FROM rif40_covariates
		 WHERE geography      = l_geography
		   AND geolevel_name  = l_geolevel_name
		   AND covariate_name = l_covariate_name;
	c2_ckicov CURSOR(l_geography  varchar, l_geolevel_name  varchar) FOR
		SELECT *
		  FROM t_rif40_geolevels
		 WHERE l_geography     = geography
		   AND l_geolevel_name = geolevel_name;
	c3_ckicov CURSOR (l_schema  varchar, l_table  varchar, l_covariate_name  varchar) FOR
		SELECT column_name
		  FROM information_schema.columns
		 WHERE table_schema = LOWER(l_schema)
		   AND table_name   = LOWER(l_table)
		   AND column_name  = LOWER(l_covariate_name);
	c4_ckicov CURSOR FOR
		SELECT COUNT(study_id) AS total
		  FROM (
		SELECT study_id
		  FROM t_rif40_results
		 LIMIT 1) a;
	c6_ckicov CURSOR FOR
		SELECT a.study_geolevel_name, b.geolevel_id, a.study_id
		  FROM t_rif40_studies a, t_rif40_geolevels b
		 WHERE currval('rif40_study_id_seq'::regclass) = study_id
		   AND b.geolevel_name = a.study_geolevel_name;
	c6_rec RECORD;
	c4_rec RECORD;
	c1_rec RECORD;
	c2b_rec RECORD;
	c3_rec RECORD;
--
	owner		varchar(30);
--
	c5_ckicov 	REFCURSOR;
	sql_stmt	varchar(2000);
	total		integer;
BEGIN
--
-- Check for update of study_id
--
	IF TG_OP = 'UPDATE' AND COALESCE(NEW.study_id::Text, '') != COALESCE(OLD.study_id::Text, '') THEN
		PERFORM rif40_log_pkg.rif40_error(-20280, 'trigger_fct_t_rif40_inv_covariates_checks', 
			'T_RIF40_INV_COVARIATES study id may not be changed: %=>%',
			OLD.study_id::VARCHAR	/* Study id */,	
			NEW.study_id::VARCHAR	/* Study id */);
	END IF;
--
-- T_RIF40_INV_COVARIATES:	Check - USERNAME is Kerberos USER on INSERT
-- 			Check - UPDATE not allowed
--			Check - DELETE only allowed on own records

	OPEN c4_ckicov;
	FETCH c4_ckicov INTO c4_rec;
	CLOSE c4_ckicov;
	IF TG_OP = 'INSERT' AND NEW.username != USER THEN
		IF USER = 'rif40' AND c4_rec.total = 0 THEN 
			/* Allowed during build before first result is added to system or before Kerberos update */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20260, 'trigger_fct_t_rif40_inv_covariates_checks',
				'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % username: % is not USER: %',
				NEW.study_id::VARCHAR		/* Study */,
				NEW.inv_id::VARCHAR		/* Investigation */,
				NEW.covariate_name::VARCHAR	/* Covariate */,
				NEW.username::VARCHAR		/* New username */,
				USER::VARCHAR			/* Username */);
		END IF;
	ELSIF TG_OP = 'UPDATE' AND NEW.username != USER THEN
		IF (USER = 'rif40' AND (c4_rec.total = 0 OR strpos(OLD.username, '@PRIVATE.NET') > 0)) THEN 
			/* Allowed during build before first result is added to system or before Kerberos update */
			NULL;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20260, 'trigger_fct_t_rif40_inv_covariates_checks',
				'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % new username: % is not USER: %; old: %',
				NEW.study_id::VARCHAR		/* Study */,
				NEW.inv_id::VARCHAR		/* Investigation */,
				NEW.covariate_name::VARCHAR	/* Covariate */,
				NEW.username::VARCHAR 		/* Record username */,
				USER::VARCHAR			/* Username */,
				OLD.username::VARCHAR		/* Old username */);
		END IF;
/*
	ELSIF INSTR(NEW.username, '@PRIVATE.NET') = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-20261, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % username: % is not a Kerberos USER: %',
			NEW.study_id::VARCHAR		/- Study -/,
			NEW.inv_id::VARCHAR		/- Investigation -/,
			NEW.covariate_name::VARCHAR	/- Covariate -/,
			NEW.username::VARCHAR 		/- Record username -/,
			USER::VARCHAR			/- Logon username -/); */
	ELSIF TG_OP = 'UPDATE' THEN
		PERFORM rif40_log_pkg.rif40_error(-20262, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % UPDATE not allowed on T_RIF40_INV_COVARIATES',
			NEW.study_id::VARCHAR		/* Study */,
			NEW.inv_id::VARCHAR		/* Investigation */,
			NEW.covariate_name::VARCHAR	/* Covariate */);
	ELSIF TG_OP = 'DELETE' AND OLD.username != USER THEN
		PERFORM rif40_log_pkg.rif40_error(-20263, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % DELETE only allowed on own records in T_RIF40_INV_COVARIATES, record owned by: ',
			OLD.study_id::VARCHAR		/* Study */,
			OLD.inv_id::VARCHAR		/* Investigation */,
			OLD.covariate_name::VARCHAR	/* Covariate */,
			OLD.username::VARCHAR		/* INSERT username */);
	END IF;

--
-- End of delete checks
--
	IF TG_OP = 'DELETE' THEN
--
 	       PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_inv_covariates_checks',
       			'[20600-3] T_RIF40_INV_COVARIATES study: % investigation: % covariate: % CRUD checks OK',
			OLD.study_id::VARCHAR			/* Study id */,
			OLD.inv_id::VARCHAR			/* Investigation */,
			OLD.covariate_name::VARCHAR		/* Covariate */);
		RETURN OLD;
	END IF;

--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_inv_covariates_checks',
		'[20600-3] T_RIF40_INV_COVARIATES study: % investigation: % covariate: % CRUD checks OK',
		NEW.study_id::VARCHAR			/* Study id */,
		NEW.inv_id::VARCHAR			/* Investigation */,
		NEW.covariate_name::VARCHAR		/* Covariate */);
--
-- Check - STUDY_GEOLEVEL_NAME. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS [this is now an FK and not really needed]
--
-- This inmplies that the geolevel can be different to the study
--
	IF (NEW.study_geolevel_name IS NOT NULL AND NEW.study_geolevel_name::text <> '') THEN
		OPEN c2_ckicov(NEW.geography, NEW.study_geolevel_name);
		FETCH c2_ckicov INTO c2b_rec;
		IF NOT FOUND THEN
			CLOSE c2_ckicov;
			PERFORM rif40_log_pkg.rif40_error(-20264, 'trigger_fct_t_rif40_inv_covariates_checks',
				'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % study area geolevel name: % not found in RIF40_GEOLEVELS',
				NEW.study_id::VARCHAR		/* Study */,
				NEW.inv_id::VARCHAR		/* Investigation */,
				NEW.covariate_name::VARCHAR	/* Covariate */,
				NEW.study_geolevel_name::VARCHAR /* Study geolevel */);
		END IF;
/*
 * Wrong - all areas are assummed to be mappable
 *
		IF c2b_rec.resolution != 1 THEN
			CLOSE c2_ckicov;
			PERFORM rif40_log_pkg.rif40_Xerror(-202xx, 'trigger_fct_t_rif40_inv_covariates_checks',
				'T_RIF40_INV_COVARIATES study area geolevel name: '||NEW.study_geolevel_name||
				' in RIF40_GEOLEVELS is not a mappable area');
		END IF;
*/
		CLOSE c2_ckicov;
--
-- Check study geolevel name is the same (or lower than) than study geolevel name in t_rif40_studies
--
		IF  TG_OP = 'INSERT' AND c4_rec.total = 0 THEN
			/* Allowed during build before first result is added to system */
			NULL;
		ELSIF TG_OP = 'UPDATE' AND strpos(OLD.username, '@PRIVATE.NET') > 0 THEN 
			/* Allowed Kerberos username update */
			NULL;
		ELSE
			OPEN c6_ckicov;
			FETCH c6_ckicov INTO c6_rec;
			IF NOT FOUND THEN
				CLOSE c6_ckicov;
				PERFORM rif40_log_pkg.rif40_error(-20364, 'trigger_fct_t_rif40_inv_covariates_checks',
					'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % study area geolevel name: % not found in T_RIF40_STUDIES for study %',
					NEW.study_id::VARCHAR		/* Study */,
					NEW.inv_id::VARCHAR		/* Investigation */,
					NEW.covariate_name::VARCHAR	/* Covariate */,
					NEW.study_geolevel_name::VARCHAR /* Study geolevel */,
					currval('rif40_study_id_seq'::regclass)::VARCHAR	/* Covariate geolevel_id */);
			ELSIF c2b_rec.geolevel_id = c6_rec.geolevel_id THEN
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_inv_covariates_checks',
					'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % study area geolevel name: % (id %) same as geolevel in T_RIF40_STUDIES for study %',
					NEW.study_id::VARCHAR		/* Study */,
					NEW.inv_id::VARCHAR		/* Investigation */,
					NEW.covariate_name::VARCHAR	/* Covariate */,
					NEW.study_geolevel_name::VARCHAR /* Study geolevel */,
					c2b_rec.geolevel_id::VARCHAR	/* Covariate geolevel_id */,
					currval('rif40_study_id_seq'::regclass)::VARCHAR);
			ELSIF c2b_rec.geolevel_id = c6_rec.geolevel_id THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'trigger_fct_t_rif40_inv_covariates_checks',
					'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % study area geolevel name: % (id %) higher resolution than geolevel in T_RIF40_STUDIES for study % geolevel (id %) [NOT CURRENTLY SUPPORTED]',
					NEW.study_id::VARCHAR		/* Study */,
					NEW.inv_id::VARCHAR		/* Investigation */,
					NEW.covariate_name::VARCHAR	/* Covariate */,
					NEW.study_geolevel_name::VARCHAR /* Study geolevel */,
					c2b_rec.geolevel_id::VARCHAR	/* Covariate geolevel_id */,
					currval('rif40_study_id_seq'::regclass)::VARCHAR,
					c6_rec.study_geolevel_name::VARCHAR	/* Study geolevel_name  */,
					c6_rec.geolevel_id::VARCHAR	/* Study geolevel_id */);
			ELSE
				PERFORM rif40_log_pkg.rif40_error(-20365, 'trigger_fct_t_rif40_inv_covariates_checks',
					'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % study area geolevel name: % (id %) lower resolution than geolevel in T_RIF40_STUDIES for study % geolevel (id %) [NOT CURRENTLY SUPPORTED]',
					NEW.study_id::VARCHAR		/* Study */,
					NEW.inv_id::VARCHAR		/* Investigation */,
					NEW.covariate_name::VARCHAR	/* Covariate */,
					NEW.study_geolevel_name::VARCHAR /* Study geolevel */,
					c2b_rec.geolevel_id::VARCHAR	/* Covariate geolevel_id */,
					currval('rif40_study_id_seq'::regclass)::VARCHAR,
					c6_rec.study_geolevel_name::VARCHAR	/* Study geolevel_name  */,
					c6_rec.geolevel_id::VARCHAR	/* Study geolevel_id */);
			END IF;
			CLOSE c6_ckicov;
		END IF;
	END IF;
--
-- Check - Covariates
--
-- a) MIN and MAX.
--
	OPEN c1_ckicov(NEW.geography, NEW.study_geolevel_name, NEW.covariate_name);
	FETCH c1_ckicov INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_ckicov;
		PERFORM rif40_log_pkg.rif40_error(-20265, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % no % covariate: % found for study area geolevel name: %',
			NEW.study_id::VARCHAR		/* Study */,
			NEW.inv_id::VARCHAR		/* Investigation */,
			NEW.covariate_name::VARCHAR	/* Covariate */,
			NEW.study_geolevel_name::VARCHAR /* Study geolevel */);
	END IF;
	CLOSE c1_ckicov;
--
-- b) Limits
--
	IF NEW.min < c1_rec.min THEN
		PERFORM rif40_log_pkg.rif40_error(-20266, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate: min (%) < RIF40_COVARIATES min (%) for study area geolevel name: ',
			NEW.study_id::VARCHAR		/* Study */,
			NEW.inv_id::VARCHAR		/* Investigation */,
			NEW.covariate_name::VARCHAR	/* Covariate */,
			NEW.min::VARCHAR		/* T_RIF40_INV_COVARIATES min */,
			c1_rec.min::VARCHAR		/* RIF40_COVARIATES min */,
			NEW.study_geolevel_name::VARCHAR /* Study geolevel */);
	ELSIF NEW.max > c1_rec.max THEN
		PERFORM rif40_log_pkg.rif40_error(-20267, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate: max (%) > RIF40_COVARIATES max (%) for study area geolevel name: ',
			NEW.study_id::VARCHAR		/* Study */,
			NEW.inv_id::VARCHAR		/* Investigation */,
			NEW.covariate_name::VARCHAR	/* Covariate */,
			NEW.max::VARCHAR		/* T_RIF40_INV_COVARIATES max */,
			c1_rec.max::VARCHAR		/* RIF40_COVARIATES max */,
			NEW.study_geolevel_name::VARCHAR /* Study geolevel */);
--
-- Remove when supported
--
	ELSIF c1_rec.type = 2 THEN
		PERFORM rif40_log_pkg.rif40_error(-20268, 'trigger_fct_t_rif40_inv_covariates_checks',
			'Error: T_RIF40_INV_COVARIATES study: % type = 2 (continuous variable) is not currently supported for geolevel_name: % covariate: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.study_geolevel_name::VARCHAR	/* Study geolevel */,
			NEW.covariate_name::VARCHAR		/* Covariate name */);

	ELSIF c1_rec.type = 1 AND ROUND(NEW.max) != NEW.max THEN /* integer score */
		PERFORM rif40_log_pkg.rif40_error(-20269, 'trigger_fct_t_rif40_inv_covariates_checks',
			'Error: T_RIF40_INV_COVARIATES study: % type = 1 (integer score) and max is not an integer: % for geolevel_name: % covariate: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.max::VARCHAR			/* New max */,
			NEW.study_geolevel_name::VARCHAR	/* Study geolevel */,
			NEW.covariate_name::VARCHAR		/* Covariate name */);
	ELSIF c1_rec.type = 1 AND ROUND(NEW.min) != NEW.min THEN /* integer score */
		PERFORM rif40_log_pkg.rif40_error(-20270, 'trigger_fct_t_rif40_inv_covariates_checks',
			'Error: T_RIF40_INV_COVARIATES study: % type = 1 (integer score) and min is not an integer: % for geolevel_name: % covariate: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.min::VARCHAR			/* New min */,
			NEW.study_geolevel_name::VARCHAR	/*Study geolevel */,
			NEW.covariate_name::VARCHAR		/* Covariate name */);
	ELSIF c1_rec.type = 1 AND NEW.min < 0 THEN /* integer score */
		PERFORM rif40_log_pkg.rif40_error(-20271, 'trigger_fct_t_rif40_inv_covariates_checks',
			'Error: T_RIF40_INV_COVARIATES study: % type = 1 (integer score) and min <0: % for geolevel_name: % covariate: %',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.min::VARCHAR			/* New min */,
			NEW.study_geolevel_name::VARCHAR	/* Study geolevel */,
			NEW.covariate_name::VARCHAR		/* Covariate name */);
	END IF;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_inv_covariates_checks',
			'[20266-71] T_RIF40_INV_COVARIATES study: % investigation: % covariate: % max/in checks OK',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.covariate_name::VARCHAR		/* Covariate */);

--
-- c) Check access to covariate table, <covariate name> column exists
--
	owner:=rif40_sql_pkg.rif40_object_resolve(c2b_rec.covariate_table::VARCHAR);
	IF coalesce(owner::text, '') = '' THEN
		PERFORM rif40_log_pkg.rif40_error(-20272, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate table: % cannot be accessed',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.covariate_name::VARCHAR		/* Covariate */,
			c2b_rec.covariate_table::VARCHAR	/* Covariate table */);
	END IF;
	OPEN c3_ckicov(owner, c2b_rec.covariate_table, NEW.covariate_name);
	FETCH c3_ckicov INTO c3_rec;
	IF NOT FOUND THEN
		CLOSE c3_ckicov;
		PERFORM rif40_log_pkg.rif40_error(-20273, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate table column: %.%.% cannot be accessed',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.covariate_name::VARCHAR		/* Covariate */,
			owner::VARCHAR				/* Owner */,
			c2b_rec.covariate_table::VARCHAR	/* Covariate table */,
			NEW.covariate_name::VARCHAR		/* Covariate column */);
	ELSE	
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_inv_covariates_checks',
			'[20272-3] T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate table column: %.%.% can be accessed',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.covariate_name::VARCHAR		/* Covariate */,
			owner::VARCHAR				/* Owner */,
			c2b_rec.covariate_table::VARCHAR	/* Covariate table */,
			NEW.covariate_name::VARCHAR		/* Covariate column */);
	END IF;
--
	CLOSE c3_ckicov;
	OPEN c3_ckicov(owner, c2b_rec.covariate_table, 'YEAR');
	FETCH c3_ckicov INTO c3_rec;
	IF NOT FOUND THEN
		CLOSE c3_ckicov;
		PERFORM rif40_log_pkg.rif40_error(-20274, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate table column: %.%.YEAR cannot be accessed',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.covariate_name::VARCHAR		/* Covariate */,
			owner::VARCHAR				/* Owner */,
			c2b_rec.covariate_table::VARCHAR	/* Covariate table */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_inv_covariates_checks',
			'[20274] T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate table column: %.%.YEAR can be accessed',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.covariate_name::VARCHAR		/* Covariate */,
			owner::VARCHAR				/* Owner */,
			c2b_rec.covariate_table::VARCHAR	/* Covariate table */);
	END IF;
	CLOSE c3_ckicov;
	OPEN c3_ckicov(owner, c2b_rec.covariate_table, NEW.study_geolevel_name);
	FETCH c3_ckicov INTO c3_rec;
	IF NOT FOUND THEN
		CLOSE c3_ckicov;
		PERFORM rif40_log_pkg.rif40_error(-20275, 'trigger_fct_t_rif40_inv_covariates_checks',
			'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate table column: %.%.% cannot be accessed',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.covariate_name::VARCHAR		/* Covariate */,
			owner::VARCHAR				/* Owner */,
			c2b_rec.covariate_table::VARCHAR	/* Covariate table */,
			NEW.study_geolevel_name::VARCHAR 	/* Study geolevel column */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_t_rif40_inv_covariates_checks',
			'[20275] T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate table column: %.%.% can be accessed',
			NEW.study_id::VARCHAR			/* Study */,
			NEW.inv_id::VARCHAR			/* Investigation */,
			NEW.covariate_name::VARCHAR		/* Covariate */,
			owner::VARCHAR				/* Owner */,
			c2b_rec.covariate_table::VARCHAR	/* Covariate table */,
			NEW.study_geolevel_name::VARCHAR 	/* Study geolevel column */);
	END IF;
	CLOSE c3_ckicov;

--
-- d) Check score
--
	IF TG_OP = 'INSERT' AND NEW.username != USER AND c4_rec.total = 0 THEN 
		/* Allowed during build before first result is added to system or before Kerberos update */
		NULL;
	ELSIF TG_OP = 'UPDATE' AND NEW.username != USER AND strpos(OLD.username, '@PRIVATE.NET') > 0  THEN 
		/* Allowed during build before first result is added to system or before Kerberos update */
		NULL;
	ELSIF c1_rec.type = 1 /* integer score */ THEN 
		/* Not during build before first result is added to system or before Kerberos update */
		BEGIN
			sql_stmt:='SELECT COUNT(*) AS total FROM "'||owner||'".'||c2b_rec.covariate_table||' WHERE '||NEW.covariate_name||' = $1';
			OPEN c5_ckicov FOR EXECUTE sql_stmt USING NEW.min::INTEGER;
			FETCH c5_ckicov INTO total;
			CLOSE c5_ckicov;
		EXCEPTION
			WHEN others THEN
				PERFORM rif40_log_pkg.rif40_error(-20276, 'trigger_fct_t_rif40_inv_covariates_checks',
					'T_RIF40_INV_COVARIATES Caught % for study: % investigation: % covariate: % covariate table column: %.%.% min value: %; SQL %',
					sqlerrm::VARCHAR			/* Error message */,
					NEW.study_id::VARCHAR			/* Study */,
					NEW.inv_id::VARCHAR			/* Investigation */,
					NEW.covariate_name::VARCHAR		/* Covariate */,
					owner::VARCHAR				/* Owner */,
					c2b_rec.covariate_table::VARCHAR	/* Covariate table */,
					NEW.covariate_name::VARCHAR		/* Covariate column */,
					NEW.min::VARCHAR			/* Min value */,
					sql_stmt::VARCHAR			/* SQL Statement */);
		END;
--
		IF total = 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-20277, 'trigger_fct_t_rif40_inv_covariates_checks',
				'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate table column: %.%.% min value: % not found',
				NEW.study_id::VARCHAR			/* Study */,
				NEW.inv_id::VARCHAR			/* Investigation */,
				NEW.covariate_name::VARCHAR		/* Covariate */,
				owner::VARCHAR				/* Owner */,
				c2b_rec.covariate_table::VARCHAR	/* Covariate table */,
				NEW.covariate_name::VARCHAR		/* Covariate column */,
				NEW.min::VARCHAR			/* Min value */);
		END IF;
		BEGIN
			OPEN c5_ckicov FOR EXECUTE sql_stmt USING NEW.max::INTEGER;
			FETCH c5_ckicov INTO total;
			CLOSE c5_ckicov;
		EXCEPTION
			WHEN others THEN
				PERFORM rif40_log_pkg.rif40_error(-20278, 'trigger_fct_t_rif40_inv_covariates_checks',
					'T_RIF40_INV_COVARIATES Caught % for study: % investigation: % covariate: % covariate table column: %.%.% max value: %; SQL %',
					sqlerrm::VARCHAR			/* Error message */,
					NEW.study_id::VARCHAR			/* Study */,
					NEW.inv_id::VARCHAR			/* Investigation */,
					NEW.covariate_name::VARCHAR		/* Covariate */,
					owner::VARCHAR				/* Owner */,
					c2b_rec.covariate_table::VARCHAR	/* Covariate table */,
					NEW.covariate_name::VARCHAR		/* Covariate column */,
					NEW.max::VARCHAR			/* Min value */,
					sql_stmt::VARCHAR			/* SQL Statement */);
		END;
		IF total = 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-20279, 'trigger_fct_t_rif40_inv_covariates_checks',
				'T_RIF40_INV_COVARIATES study: % investigation: % covariate: % covariate table column: %.%.% max value: % not found',
				NEW.study_id::VARCHAR			/* Study */,
				NEW.inv_id::VARCHAR			/* Investigation */,
				NEW.covariate_name::VARCHAR		/* Covariate */,
				owner::VARCHAR				/* Owner */,
				c2b_rec.covariate_table::VARCHAR	/* Covariate table */,
				NEW.covariate_name::VARCHAR		/* Covariate column */,
				NEW.max::VARCHAR			/* Max value */);
		END IF;
	END IF;
--
	IF TG_OP = 'DELETE' THEN
		RETURN OLD;
	ELSE  	
		RETURN NEW;
	END IF;
END; 
$BODY$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.trigger_fct_t_rif40_inv_covariates_checks() IS 'Check - USERNAME exists.
Check - USERNAME is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
Check - study_geolevel_name.
Check - Covariates a) MIN and MAX.  b) Limits c) Check access to covariate table, <covariate name> column exists d) Check score.';

CREATE TRIGGER t_rif40_inv_covariates_checks
	BEFORE INSERT OR UPDATE OR DELETE ON "t_rif40_inv_covariates" FOR EACH ROW
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_inv_covariates_checks();
COMMENT ON TRIGGER t_rif40_inv_covariates_checks ON t_rif40_inv_covariates IS 'INSERT OR UPDATE OR DELETE trigger: calls rif40_trg_pkg.trigger_fct_t_rif40_inv_covariates_checks()';

--
-- Eof