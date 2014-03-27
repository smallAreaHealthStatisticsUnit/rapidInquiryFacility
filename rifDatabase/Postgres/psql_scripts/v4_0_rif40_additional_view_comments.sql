-- ************************************************************************
-- *
-- * DO NOT EDIT THIS SCRIPT OR MODIFY THE RIF SCHEMA - USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - Additional view comments
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
-- Check database is sahsuland_dev
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'Database check: %', current_database();	
	ELSE
		RAISE EXCEPTION 'C20901: Database check failed: % is not sahsuland_dev', current_database();	
	END IF;
END;
$$;

\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		WITH a AS (
			SELECT table_name table_or_view, table_schema, column_name, ordinal_position
				  FROM information_schema.columns a
					LEFT OUTER JOIN pg_tables b1 ON 
						(b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
					LEFT OUTER JOIN pg_views b2  ON 
						(b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
			 WHERE (viewowner IN (USER, 'rif40') OR tableowner IN (USER, 'rif40'))
				AND table_schema = 'rif40'
		), b1 AS (
			SELECT table_or_view, table_schema, column_name, ordinal_position, b.oid
  			  FROM a, pg_class b
			 WHERE b.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, 'rif40')) 
			   AND b.relname  = a.table_or_view
		), b2 AS (
			SELECT b.relname AS table_or_view, table_schema, column_name, ordinal_position, b.oid
  			  FROM a, pg_class b
			 WHERE b.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, 'rif40')) 
			   AND b.relname  = a.table_or_view
		), c1 AS (
			SELECT table_or_view, table_schema, column_name, ordinal_position, c.description
			  FROM b1
				LEFT OUTER JOIN pg_description c ON (c.objoid = b1.oid AND c.objsubid = b1.ordinal_position)
			 WHERE c.description IS NULL
		), c2 AS (
			SELECT table_or_view, table_schema, column_name, ordinal_position, c.description
			  FROM b2
				LEFT OUTER JOIN pg_description c ON (c.objoid = b2.oid AND c.objsubid = b2.ordinal_position)
			 WHERE c.description IS NOT NULL
		)
		SELECT c1.table_or_view, c1.table_schema, c1.column_name, c2.description
		  FROM c1
			LEFT OUTER JOIN c2 ON ('t_'||c1.table_or_view = c2.table_or_view AND c1.column_name = c2.column_name)
		 WHERE c2.description IS NOT NULL
		 ORDER BY 1, 2;
	c1_rec RECORD;
--
	sql_stmt	VARCHAR[];
	i INTEGER:=0;
BEGIN
--
-- Turn on some debug
--
        PERFORM rif40_log_pkg.rif40_log_setup();
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_ddl:DEBUG1');
--
	FOR c1_rec IN c1 LOOP
		i:=i+1;
		sql_stmt[i]:='COMMENT ON COLUMN '||c1_rec.table_or_view||'.'||c1_rec.column_name||' IS '''||c1_rec.description||'''';
	END LOOP;
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
END;
$$;
\set VERBOSITY default

COMMENT ON COLUMN rif40_columns.table_or_view_name_hide  IS 'Table name';
COMMENT ON COLUMN rif40_columns.column_name_hide         IS 'Column name';
COMMENT ON COLUMN rif40_columns.table_or_view_name_href  IS 'Table name (web version)';
COMMENT ON COLUMN rif40_columns.column_name_href         IS 'Column name (web version)'; 
COMMENT ON COLUMN rif40_columns.nullable                 IS 'Nollable';
COMMENT ON COLUMN rif40_columns.oracle_data_type         IS 'Oracle data type';
COMMENT ON COLUMN rif40_columns.comments  	         IS 'Comments';

COMMENT ON COLUMN rif40_tables_and_views.class                   IS 'Class';          
COMMENT ON COLUMN rif40_tables_and_views.table_or_view           IS 'Table name';      
COMMENT ON COLUMN rif40_tables_and_views.table_or_view_name_href IS 'Table name (web version)';
COMMENT ON COLUMN rif40_tables_and_views.table_or_view_name_hide IS 'Table name (web version)';
COMMENT ON COLUMN rif40_tables_and_views.comments                IS 'Comments';  

COMMENT ON COLUMN rif40_triggers.table_name         IS 'Table name';
COMMENT ON COLUMN rif40_triggers.column_name        IS 'Column name';
COMMENT ON COLUMN rif40_triggers.trigger_name       IS 'Trigger name';
COMMENT ON COLUMN rif40_triggers.trigger_type       IS 'Type type';
COMMENT ON COLUMN rif40_triggers.triggering_event   IS 'Triggering event';
COMMENT ON COLUMN rif40_triggers.when_clause        IS 'When clause';
COMMENT ON COLUMN rif40_triggers.action_type        IS 'Action type';
COMMENT ON COLUMN rif40_triggers.comments           IS 'Comments';
--
-- Eof
