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
-- Rapid Enquiry Facility (RIF) - RIF40_TABLE trigger checks
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
-- Only do this if Postgres version is 10 or above
--

DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
	 	SELECT version() AS version,
	 	current_setting('server_version_num')::NUMERIC as numeric_version;
	c1_rec RECORD;
--
BEGIN
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	EXCEPTION
		WHEN others THEN
            RAISE WARNING 'Unsupported version() function: %', version();
            RAISE;
	END;
	--
	IF c1_rec.numeric_version < 100000 THEN
		RAISE INFO 'Not running trigger_fct_rif40_tables_checks.sql on Postgres version %',
			c1_rec.version::VARCHAR;
		RETURN;
    ELSE

        --
        -- Check user is rif40
        --
        BEGIN
            IF user = 'rif40' THEN
                RAISE INFO 'User check: %', user;
            ELSE
                RAISE EXCEPTION 'C20900: User check failed: % is not rif40', user;
            END IF;
        END;

        DROP TRIGGER IF EXISTS rif40_tables_checks_del ON "rif40_tables" CASCADE;
        DROP TRIGGER IF EXISTS rif40_tables_checks ON "rif40_tables" CASCADE;

        CREATE OR REPLACE FUNCTION rif40_trg_pkg.trigger_fct_rif40_tables_checks() RETURNS trigger AS $BODY$
        DECLARE
        /*
        <trigger_rif40_tables_checks_description>
        <para>
        Check TABLE_NAME exists. DO NOT RAISE AN ERROR IF IT DOES; otherwise check, column <TABLE_NAME>.TOTAL_FIELD,  column <TABLE_NAME>.ICD_FIELD_NAME exists. This allows the RIF40 schema owner to not have access to the tables. Access is checked in RIF40_NUM_DENOM. Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). A user specific T_RIF40_NUM_DENOM is supplied for other combinations. Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.
        Check table_name, total_field, sex_field_name, age_group_field_name, age_sex_group_field_name Oracle names.
        </para>
        </trigger_rif40_tables_checks_description>
         */
        --
        -- Error range: -20180 to -20199 - RIF40_TABLES
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
            c1rt CURSOR (l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR) FOR
                WITH all_tab_columns AS (
                    SELECT UPPER(a.tablename) AS tablename, UPPER(b.attname) AS columnname, UPPER(schemaname) AS schemaname	/* Tables */
                      FROM pg_tables a, pg_attribute b, pg_class c
                     WHERE c.oid        = b.attrelid
                       AND c.relname    = a.tablename
                       AND c.relkind    = 'r' /* Relational table */
                       AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */
                     UNION
                    SELECT UPPER(c.relname) AS tablename, UPPER(b.attname) AS columnname, UPPER(n.nspname) AS schemaname	/* Tables */
                      FROM pg_partitioned_table p, pg_attribute b, pg_class c, pg_namespace n
                     WHERE p.partrelid    = c.oid
                       AND c.oid          = b.attrelid
                       AND c.relnamespace = n.oid
                     UNION
                    SELECT UPPER(a.viewname) AS tablename, UPPER(b.attname) AS columnname, UPPER(schemaname) AS schemaname	/* Views */
                      FROM pg_views a, pg_attribute b, pg_class c
                     WHERE c.oid        = b.attrelid
                       AND c.relname    = a.viewname
                       AND c.relkind    = 'v' /* View */
                     UNION
                    SELECT UPPER(a.relname) AS tablename, UPPER(d.attname) AS columnname, UPPER(n.nspname) AS schemaname				/* User FDW foreign tables */
                      FROM pg_foreign_table b, pg_roles r, pg_attribute d, pg_class a
                        LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)
                     WHERE b.ftrelid  = a.oid
                       AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
                       AND a.relowner = r.oid
                       AND n.nspname  = USER
                       AND a.oid      = d.attrelid
                )
                SELECT columnname
                  FROM all_tab_columns
                 WHERE schemaname = UPPER(l_schema)
                   AND tablename  = UPPER(l_table)
                   AND columnname = UPPER(l_column);
            c1_rec RECORD;
        --
            schema		varchar(30);
        --	msg 		varchar(4000)	:= NULL;
        BEGIN
        --
        -- End of delete checks
        --
            IF TG_OP = 'DELETE' THEN
                RETURN OLD;
            END IF;

            schema:=rif40_sql_pkg.rif40_object_resolve(NEW.table_name::VARCHAR);
        --
        -- Normally the schema owner will not be able to see the data tables, so there is no error here
        -- You must have access to automatic indirect standardisation denominators
        --
            IF (schema IS NOT NULL) THEN
                IF (NEW.total_field IS NOT NULL AND NEW.total_field::text <> '') THEN
                    OPEN c1rt(schema, NEW.table_name, NEW.total_field);
                    FETCH c1rt INTO c1_rec;
                    CLOSE c1rt;
                    IF coalesce(c1_rec.columnname::text, '') = '' THEN
                        PERFORM rif40_log_pkg.rif40_error(-20180, 'trigger_fct_rif40_tables_checks', '[20180] Error: RIF40_TABLES TOTAL_FIELD % column not found in table: %',
                            NEW.total_field::VARCHAR	/* Total field */,
                            NEW.table_name::VARCHAR		/* Table name */);
                    ELSE
                        PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'trigger_fct_rif40_tables_checks', '[20180] RIF40_TABLES TOTAL_FIELD % column found in table: %',
                            NEW.total_field::VARCHAR	/* Total field */,
                            NEW.table_name::VARCHAR		/* Table name */);
                    END IF;
                END IF;
            END IF;
        --
        -- Check direct standardised denominators exist system wide
        --
            IF NEW.isdirectdenominator = 1 THEN
                IF coalesce(NEW.table_name::text, '') = '' THEN
                    PERFORM rif40_log_pkg.rif40_error(-20181, 'trigger_fct_rif40_tables_checks', '[20181] RIF40_TABLES direct standardised denominator TABLE_NAME (%) not found',
                        NEW.table_name::VARCHAR		/* Table name */);
                END IF;
            END IF;
        --
        -- Automatic
        --
        --	IF NEW.automatic = 1 AND NEW.isindirectdenominator = 1 THEN
        --		IF coalesce(table_or_view::text, '') = '' THEN
        --			PERFORM rif40_log_pkg.rif40_error(-20182, 'RIF40_TABLES TABLE_NAME ('||NEW.table_name||') not found');
        --		END IF;
        --
        -- This causes:
        -- ORA-04091: table RIF40.RIF40_TABLES is mutating, trigger/function may not see it
        --
        -- Enforced in RIF40_NUM_DENOM
        --
        --		msg:=PERFORM rif40_log_pkg.rif40_auto_indirect_checks(table_or_view);
        --		IF (msg IS NOT NULL AND msg::text <> '') THEN
        --			PERFORM rif40_log_pkg.rif40_error(-20183, 'RIF40_TABLES table: '||UPPER(table_or_view)||
        --				' (automatic indirect denominator) has >1 per geography: '||msg);
        --		END IF;
        --
        --	END IF;
        --
        -- Check table_name, total_field, sex_field_name, age_group_field_name, age_sex_group_field_name Oracle names
        --
            PERFORM rif40_trg_pkg.rif40_db_name_check('TABLE_NAME', NEW.table_name);
            PERFORM rif40_trg_pkg.rif40_db_name_check('TOTAL_FIELD', NEW.total_field);
            IF (NEW.sex_field_name IS NOT NULL AND NEW.sex_field_name::text <> '') THEN
                PERFORM rif40_trg_pkg.rif40_db_name_check('SEX_FIELD_NAME', NEW.sex_field_name);
            END IF;
            IF (NEW.age_group_field_name IS NOT NULL AND NEW.age_group_field_name::text <> '') THEN
                PERFORM rif40_trg_pkg.rif40_db_name_check('AGE_GROUP_FIELD_NAME', NEW.age_group_field_name);
            END IF;
            IF (NEW.age_sex_group_field_name IS NOT NULL AND NEW.age_sex_group_field_name::text <> '') THEN
                PERFORM rif40_trg_pkg.rif40_db_name_check('AGE_SEX_GROUP_FIELD_NAME', NEW.age_sex_group_field_name);
            END IF;
        --
            RETURN NEW;
        END;
        $BODY$
        LANGUAGE 'plpgsql';
        COMMENT ON FUNCTION rif40_trg_pkg.trigger_fct_rif40_tables_checks() IS 'Check TABLE_NAME exists. DO NOT RAISE AN ERROR IF IT DOES; otherwise check, column <TABLE_NAME>.TOTAL_FIELD,  column <TABLE_NAME>.ICD_FIELD_NAME exists. This allows the RIF40 schema owner to not have access to the tables. Access is checked in RIF40_NUM_DENOM. Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). A user specific T_RIF40_NUM_DENOM is supplied for other combinations. Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.
        Check table_name, total_field, sex_field_name, age_group_field_name, age_sex_group_field_name Oracle names.

        Error range: -20180 to -20199 - RIF40_TABLES';

        CREATE TRIGGER rif40_tables_checks
            BEFORE INSERT OR UPDATE OF isdirectdenominator, table_name, total_field,
                sex_field_name, age_group_field_name, age_sex_group_field_name ON rif40_tables
            FOR EACH ROW
            WHEN ((NEW.table_name IS NOT NULL AND NEW.table_name::text <> '') OR
                (NEW.isdirectdenominator IS NOT NULL AND NEW.isdirectdenominator::text <> '') OR
                (NEW.table_name IS NOT NULL AND NEW.table_name::text <> '') OR
                (NEW.total_field IS NOT NULL AND NEW.total_field::text <> '') OR
                (NEW.sex_field_name IS NOT NULL AND NEW.sex_field_name::text <> '') OR
                (NEW.age_group_field_name IS NOT NULL AND NEW.age_group_field_name::text <> '') OR
                (NEW.age_sex_group_field_name IS NOT NULL AND NEW.age_sex_group_field_name::text <> ''))
            EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_rif40_tables_checks();
        COMMENT ON TRIGGER rif40_tables_checks ON rif40_tables IS 'INSERT OR UPDATE trigger: calls rif40_trg_pkg.trigger_fct_rif40_tables_checks()';
        CREATE TRIGGER rif40_tables_checks_del
            BEFORE DELETE ON rif40_tables
            FOR EACH ROW
            EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_rif40_tables_checks();
        COMMENT ON TRIGGER rif40_tables_checks_del ON rif40_tables IS 'DELETE trigger: calls rif40_trg_pkg.trigger_fct_rif40_tables_checks()';

	END IF;

END;
$$;
--
-- Eof
