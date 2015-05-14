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

DROP TRIGGER IF EXISTS rif40_outcomes_checks ON rif40_outcomes;
DROP TRIGGER IF EXISTS rif40_outcomes_checks_del ON rif40_outcomes;
DROP FUNCTION FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_outcomes_checks();
	
CREATE OR REPLACE FUNCTION rif40_trg_pkg.trigger_fct_rif40_outcomes_checks() RETURNS trigger AS $BODY$
DECLARE
/*
<trigger_rif40_outcomes_checks_description>
<para>
Check current_lookup table exists
Check current_value_Nchar AND current_description_Nchar columns exists if NOT NULL
Check previous lookup table exists if NOT NULL
Check previous_value_Nchar AND previous_description_Nchar if NOT NULL AND (previous_lookup_table IS NOT NULL AND previous_lookup_table::text <> '')
</para>
</trigger_rif40_outcomes_checks_description>
 */
--
-- -20400 to -20419 - RIF40_OUTCOMES
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
	schema VARCHAR(30);
BEGIN
	IF TG_OP = 'DELETE' THEN
		RETURN NULL;
	END IF;
--
-- Check current_lookup table exists
--
	schema:=rif40_sql_pkg.rif40_object_resolve(NEW.current_lookup_table::VARCHAR);
	IF (NEW.current_lookup_table IS NOT NULL AND NEW.current_lookup_table::text <> '') AND coalesce(schema::text, '') = '' THEN
		PERFORM rif40_log_pkg.rif40_error(-20400, 'trigger_fct_rif40_outcomes_checks', 'RIF40_OUTCOMES outcome type: % current_lookup_table (%) not found',
			NEW.outcome_type::VARCHAR		/* Outcome type */,
			NEW.current_lookup_table::VARCHAR	/* Current lookup table */);
	END IF;

--
-- Check current_value_Nchar AND current_description_Nchar columns exists if NOT NULL
--
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_value_1char',  schema, NEW.current_lookup_table, NEW.current_value_1char);
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_value_2char',  schema, NEW.current_lookup_table, NEW.current_value_2char);
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_value_3char',  schema, NEW.current_lookup_table, NEW.current_value_3char);
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_value_4char',  schema, NEW.current_lookup_table, NEW.current_value_4char);
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_value_5char',  schema, NEW.current_lookup_table, NEW.current_value_5char);
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_description_1char',  schema, NEW.current_lookup_table, NEW.current_description_1char);
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_description_2char',  schema, NEW.current_lookup_table, NEW.current_description_2char);
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_description_3char',  schema, NEW.current_lookup_table, NEW.current_description_3char);
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_description_4char',  schema, NEW.current_lookup_table, NEW.current_description_4char);
	PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'current_description_5char',  schema, NEW.current_lookup_table, NEW.current_description_5char);

--
-- Check previous lookup table exists if NOT NULL
--
	schema:=rif40_sql_pkg.rif40_object_resolve(NEW.previous_lookup_table::VARCHAR);
	IF (NEW.previous_lookup_table IS NOT NULL AND NEW.previous_lookup_table::text <> '') AND coalesce(schema::text, '') = '' THEN
		PERFORM rif40_log_pkg.rif40_error(-20402, 'trigger_fct_rif40_outcomes_checks', 'RIF40_OUTCOMES outcome type: % previous_lookup_table (%) not found',
			NEW.outcome_type::VARCHAR		/* Outcome type */,
			NEW.previous_lookup_table::VARCHAR	/* Previous lookup table */);
	END IF;

--
-- Check previous_value_Nchar AND previous_description_Nchar if NOT NULL AND (previous_lookup_table IS NOT NULL AND previous_lookup_table::text <> '')
--
	IF (NEW.previous_lookup_table IS NOT NULL AND NEW.previous_lookup_table::text <> '') THEN
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_value_1char',  schema, NEW.previous_lookup_table, NEW.previous_value_1char);
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_value_2char',  schema, NEW.previous_lookup_table, NEW.previous_value_2char);
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_value_3char',  schema, NEW.previous_lookup_table, NEW.previous_value_3char);
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_value_4char',  schema, NEW.previous_lookup_table, NEW.previous_value_4char);
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_value_5char',  schema, NEW.previous_lookup_table, NEW.previous_value_5char);
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_description_1char',  schema, NEW.previous_lookup_table, NEW.previous_description_1char);
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_description_2char',  schema, NEW.previous_lookup_table, NEW.previous_description_2char);
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_description_3char',  schema, NEW.previous_lookup_table, NEW.previous_description_3char);
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_description_4char',  schema, NEW.previous_lookup_table, NEW.previous_description_4char);
		PERFORM rif40_trg_pkg.lp_outcomes_check_column(NEW.outcome_type, 'previous_description_5char',  schema, NEW.previous_lookup_table, NEW.previous_description_5char);
	END IF;
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_rif40_outcomes_checks', '[20400-2] RIF40_OUTCOMES outcome type: % OK',
		NEW.outcome_type::VARCHAR	/* Outcome type */);
--
	IF TG_OP = 'DELETE' THEN
		RETURN OLD;
	ELSE  	
		RETURN NEW;
	END IF;
END;
$BODY$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.trigger_fct_rif40_outcomes_checks() IS 'Check current_lookup table exists
Check current_value_Nchar AND current_description_Nchar columns exists if NOT NULL
Check previous lookup table exists if NOT NULL
Check previous_value_Nchar AND previous_description_Nchar if NOT NULL AND (previous_lookup_table IS NOT NULL)

-20400 to -20419 - RIF40_OUTCOMES';

CREATE TRIGGER rif40_outcomes_checks
	BEFORE INSERT OR UPDATE OF outcome_type, outcome_description, current_version, current_sub_version,
		previous_version, previous_sub_version, current_lookup_table, previous_lookup_table,
		current_value_1char, current_value_2char, current_value_3char, current_value_4char, current_value_5char,
		current_description_1char, current_description_2char, current_description_3char, current_description_4char, current_description_5char,
		previous_value_1char, previous_value_2char, previous_value_3char, previous_value_4char, previous_value_5char,
		previous_description_1char, previous_description_2char, previous_description_3char, previous_description_4char, previous_description_5char
	ON rif40_outcomes
	FOR EACH ROW
	WHEN ((NEW.outcome_type IS NOT NULL AND NEW.outcome_type::text <> '') OR (NEW.outcome_description IS NOT NULL AND NEW.outcome_description::text <> '') OR (NEW.current_version IS NOT NULL AND NEW.current_version::text <> '') OR (NEW.current_lookup_table IS NOT NULL AND NEW.current_lookup_table::text <> ''))
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_rif40_outcomes_checks();
COMMENT ON TRIGGER rif40_outcomes_checks ON rif40_outcomes IS 'INSERT OR UPDATE trigger: calls rif40_trg_pkg.trigger_fct_rif40_outcomes_checks()';

CREATE TRIGGER rif40_outcomes_checks_del
	BEFORE DELETE ON rif40_outcomes
	FOR EACH ROW
	EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_rif40_outcomes_checks();
COMMENT ON TRIGGER rif40_outcomes_checks_del ON rif40_outcomes IS 'DELETE trigger: calls rif40_trg_pkg.trigger_fct_rif40_outcomes_checks()';


--
-- Eof