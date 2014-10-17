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
-- Rapid Enquiry Facility (RIF) - Created PG psql code (INSTEAD OF triggers for views with USERNAME as a column)
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
--
\set ON_ERROR_STOP Off
DROP TRIGGER IF EXISTS trg_rif40_parameters ON rif40_parameters;
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_parameters();

\set ON_ERROR_STOP ON
\echo Creating PG psql code (INSTEAD OF triggers for views with USERNAME as a column)...

\i ../PLpgsql/rif40_trg_pkg/drop_instead_of_triggers.sql
\i ../PLpgsql/rif40_trg_pkg/create_instead_of_triggers.sql

CREATE OR REPLACE FUNCTION rif40_trg_pkg.rif40_db_name_check(column_name VARCHAR, value VARCHAR) 
RETURNS void
AS
$func$
/*

Function: 	rif40_db_name_check()
Parameters:	Column name, value
Returns:	NONE
Description:	Check column name value obeys DB naming conventions; i.e. Oracles. Value is assumed to be in upper case; even on Postgres where the convention is lower case	

SELECT regexp_replace('1 AA_123a ()*+Cb', '[[:upper:]]{1,}[[:upper:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g');
 regexp_replace 
----------------
 1 a ()*+b
(1 row)


 */
DECLARE
--
-- Check for valid Oracle name
--
	c3dbnc CURSOR(l_value VARCHAR) FOR
		SELECT REGEXP_REPLACE(l_value, '[[:upper:]]{1,}[[:upper:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g') AS invalid_characters,
		       CASE
				WHEN LENGTH(REGEXP_REPLACE(l_value, '[[:upper:]]{1,}[[:upper:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g')) > 0 THEN TRUE
				ELSE FALSE END::BOOLEAN AS is_invalid;
--
	c3dbnc_rec 		RECORD;
	maxlen 			INTEGER:=30;
BEGIN
--
-- Check for valid Oracle name
--
	OPEN c3dbnc(value);
	FETCH c3dbnc INTO c3dbnc_rec;
--
-- Invalid Oracle name
--
	IF c3dbnc_rec.is_invalid THEN
		CLOSE c3dbnc;
		PERFORM rif40_log_pkg.rif40_error(-20098, 'rif40_db_name_check', 'Invalid Oracle/Postgres name %: "%"; contains NON alphanumeric characters: %',
			UPPER(column_name)::VARCHAR/* Oracle name */, 
			value::VARCHAR		/* Value */,
			c3dbnc_rec.invalid_characters::VARCHAR	/* Invalid characters */);
	END IF;
	CLOSE c3dbnc;
--
-- Length must <= 30
--
	IF LENGTH(value) > maxlen THEN
		PERFORM rif40_log_pkg.rif40_error(-20097, 'rif40_db_name_check', 'Invalid Oracle name %: "%"; length (%) > %',
			UPPER(column_name)::VARCHAR	/* Oracle name */, 
			value::VARCHAR		/* Value */,
			LENGTH(value)::VARCHAR	/* Length */, 
			maxlen::VARCHAR		/* Maximum permitted length */);
	END IF;
--
-- First character must be a letter
--
	IF SUBSTR(value, 1, 1) NOT BETWEEN 'A' AND 'Z' THEN
		PERFORM rif40_log_pkg.rif40_error(-20096, 'rif40_db_name_check', 'Invalid Oracle/Postgres name s: "%"; First character (%) must be a letter', 
			UPPER(column_name)::VARCHAR	/* Oracle name */, 
			value::VARCHAR		/* Value */, 
			SUBSTR(value, 1, 1)::VARCHAR	/* First character */);
	END IF;
--
	IF value IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_db_name_check', 'rif40_db_name_check(%, %) Ok',
			UPPER(column_name)::VARCHAR	/* Oracle name */, 
			value::VARCHAR		/* Value */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_db_name_check', 'rif40_db_name_check(%, %) Ok',
			UPPER(column_name)::VARCHAR	/* Oracle name */, 
			'NULL'::VARCHAR		/* Value */);
	END IF;
END;
$func$
LANGUAGE 'plpgsql';

COMMENT ON FUNCTION rif40_trg_pkg.rif40_db_name_check(VARCHAR, VARCHAR) IS 'Function: 	rif40_db_name_check()
Parameters:	Column name, value
Returns:	NONE
Description:	Check column name value obeys DB naming conventions; i.e. Oracle''s. Value is assumed to be in upper case; even on Postgres where the convention is lower case';

CREATE OR REPLACE FUNCTION rif40_trg_pkg.rif40_sql_injection_check(
	table_name	VARCHAR,
	study_id	VARCHAR,
	inv_id 		VARCHAR,
	line_number	VARCHAR,
	name 		VARCHAR,
	value		VARCHAR)
RETURNS void
AS
$func$
/*
Parameters: 	Table nmame, study_id, inv_id, line_number, name, value
Description:	Check value for SQL injection	
 */
DECLARE
BEGIN
	PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_injection_check', 'SQL injection check not yet implemented for table: %, study: %', 
		table_name::VARCHAR, study_id::VARCHAR);
END;
$func$
LANGUAGE 'plpgsql';

COMMENT ON FUNCTION rif40_trg_pkg.rif40_sql_injection_check(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) IS 'Parameters: 	Table nmame, study_id, inv_id, line_number, name, value
Description:	Check value for SQL injection';

\df rif40_trg_pkg.rif40_sql_injection_check

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_sql_injection_check(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_sql_injection_check(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif_user, rif_manager;

CREATE OR REPLACE FUNCTION rif40_trg_pkg.rif40_drop_table_triggers() 
RETURNS void
AS
$func$
/*

Function: 	rif40_drop_table_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Drop triggers and trigger functions on all standard RIF tables (Used as part of the build ONLY)
		rfi40 table trigger in schema rif40_trg_pkg

*/
DECLARE
	c1dtt CURSOR FOR
		SELECT tgname AS trigger_name, 
		       relname AS table_name,
		       n.nspname AS table_schema,
		       n2.nspname||'.'||p.proname||rif40_sql_pkg.rif40_get_function_arg_types(p.proargtypes) AS function_name
		  FROM pg_tables t, pg_class c, pg_trigger b, pg_namespace n, pg_namespace n2, pg_proc p
		 WHERE b.tgrelid        = c.oid				
                   AND NOT b.tgisinternal				/* Ignore constraints */
		   AND n.oid            = c.relnamespace		/* Table schema */
		   AND b.tgfoid         = p.oid				/* Trigger function */
		   AND n2.oid            = p.pronamespace		/* Fu8nction schema */
		   AND c.relowner       IN (SELECT oid FROM pg_roles WHERE rolname = USER)	/* RIF40 tables */
		   AND n2.nspname        = 'rif40_trg_pkg' 		/* Function schema: rif40_trg_pkg */
		   AND c.relname        = t.tablename			/* Tables only */
		   AND c.relkind        = 'r' 				/* Relational table */
		   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
		 ORDER BY 1, 2;
--
	c1dtt_rec RECORD;
--
	sql_stmt VARCHAR[];
BEGIN
--
-- Must be RIF40
--
	IF USER != 'rif40' THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_drop_table_triggers',
                	'Cannot drop INSTEAD OF triggers; user % must must be RIF40', USER::VARCHAR);
	END IF;
--
	FOR c1dtt_rec IN c1dtt LOOP
		sql_stmt[1]:='DROP TRIGGER IF EXISTS '||c1dtt_rec.trigger_name||' ON '||c1dtt_rec.table_schema||'.'||c1dtt_rec.table_name||' CASCADE';
		sql_stmt[2]:='DROP FUNCTION IF EXISTS '||c1dtt_rec.function_name||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$func$
LANGUAGE 'plpgsql';

COMMENT ON FUNCTION rif40_trg_pkg.rif40_drop_table_triggers() IS 'Function: 	rif40_drop_table_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Drop triggers and trigger functions on all standard RIF tables (Used as part of the build ONLY)
		rfi40 table trigger in schema rif40_trg_pkg';

\df rif40_trg_pkg.rif40_drop_table_triggers

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_drop_table_triggers() TO rif40;

/* Remaining INSTEAD OF triggers for VIEW of TABLES without USERNAME */ 

\df rif40_trg_pkg.rif40_db_name_check

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_db_name_check(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_db_name_check(VARCHAR, VARCHAR) TO rif_user, rif_manager;

\df rif40_trg_pkg.create_instead_of_triggers

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.create_instead_of_triggers() TO rif40;

\df rif40_trg_pkg.drop_instead_of_triggers

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.drop_instead_of_triggers() TO rif40;

CREATE OR REPLACE FUNCTION rif40_trg_pkg.lp_outcomes_check_column(
	l_outcome_type	VARCHAR,
	l_rif40_column	VARCHAR,
	l_schema 	VARCHAR,
	l_table_name	VARCHAR,
	l_column	VARCHAR)
RETURNS void
AS
$func$
/*

Function: 	lp_outcomes_check_column()
Parameters:	Outcome type, Column name in RIF40_OUTCOMES, Column (value in above), table, column [both being checked]
Returns:	NONE
Description:	Check outcome type exists

*/
DECLARE
	c1outc CURSOR (l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR) IS
		SELECT column_name
		  FROM information_schema.columns
		 WHERE table_schema = LOWER(l_schema)
		   AND table_name   = LOWER(l_table)
		   AND column_name  = LOWER(l_column);
--
	c1outc_rec RECORD;
BEGIN
	IF (l_column IS NOT NULL AND l_column::text <> '') THEN
		OPEN c1outc(l_schema, l_table_name, l_column);
		FETCH c1outc INTO c1outc_rec;
		IF c1outc_rec.column_name IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20401, 'lp_outcomes_check_column', 'RIF40_OUTCOMES outcome type: % % column (%) not found in table %.%',
				l_outcome_type::VARCHAR 	/* Outcome type */,
				l_rif40_column::VARCHAR 	/* Column name in RIF40_OUTCOMES */,
				l_column::VARCHAR  		/* Column (value in above) */,
				l_schema::VARCHAR  		/* Schema */,
				l_table_name::VARCHAR		/* Table */);
			CLOSE c1outc;
		END IF;
		CLOSE c1outc;
	END IF;
END;
$func$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.lp_outcomes_check_column(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	lp_outcomes_check_column()
Parameters:	Outcome type, Column name in RIF40_OUTCOMES, Column (value in above), table, column [both being checked]
Returns:	NONE
Description:	Check outcome type exists';

\df rif40_trg_pkg.lp_outcomes_check_column_check

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.lp_outcomes_check_column(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_trg_pkg.lp_outcomes_check_column(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif_user, rif_manager;

CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_rif40_parameters()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $trigger_function$
BEGIN
        IF TG_OP = 'INSERT' THEN
--
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
                IF (USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) AND rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
                        INSERT INTO t_rif40_parameters (
                                param_name,
                                param_value,
                                param_description)
                        VALUES(
                                NEW.param_name,
                                NEW.param_value,
                                NEW.param_description);
                ELSE
                        PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_parameters',
                                'Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL', USER::VARCHAR, NEW.username::VARCHAR);
                END IF;
                RETURN NEW;
        ELSIF TG_OP = 'UPDATE' THEN
--
-- Cannot update SuppressionValue or RifParametersTable
--
                IF NEW.param_name NOT IN ('SuppressionValue', 'RifParametersTable') THEN
                        UPDATE t_rif40_parameters
                           SET param_value=NEW.param_value,
                               param_description=NEW.param_description;
                ELSE
                        PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_parameters',
                                'Cannot UPDATE: RIF40_PARAMETERS param_name  % (=%)', NEW.param_name::VARCHAR, NEW.param_value::VARCHAR);
                END IF;
                RETURN NEW;
        ELSIF TG_OP = 'DELETE' THEN
--
-- Check USER = OLD.username; if OK DELETE
--
                PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_parameters',
                    	'Cannot DELETE RIF40_PARAMETERS records');
                RETURN NULL;
        END IF;
        RETURN NEW;
END;
$trigger_function$;

CREATE TRIGGER trg_rif40_parameters INSTEAD OF INSERT OR UPDATE OR DELETE ON rif40_parameters FOR EACH ROW EXECUTE PROCEDURE rif40_trg_pkg.trgf_rif40_parameters();

COMMENT ON TRIGGER trg_rif40_parameters ON rif40_parameters IS 'INSTEAD OF trigger for view T_RIF40_PARAMETERS to allow INSERT/UPDATE by the RIF manager. Update not allowed on parameters: SuppressionValue or RifParametersTable';

COMMENT ON FUNCTION rif40_trg_pkg.trgf_rif40_parameters() IS 'INSTEAD OF trigger for view T_RIF40_PARAMETERS to allow INSERT/UPDATE by the RIF manager. Update not allowed on parameters: SuppressionValue or RifParametersTable';

GRANT INSERT ON rif40_parameters TO rif_manager;
GRANT UPDATE ON rif40_parameters TO rif_manager;

/*
BEGIN;
INSERT INTO rif40_comparison_areas(username, study_id, area_id) VALUES (NULL, 123456, 'XXYY');
ROLLBACK;
 */
			  
\set VERBOSITY default

\echo Created PG psql code (INSTEAD OF triggers for views with USERNAME as a column).
--
-- Eof
