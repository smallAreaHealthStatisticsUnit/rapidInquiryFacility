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
-- Rapid Enquiry Facility (RIF) - Create SAHSULAND rif40 exmaple schema
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
-- Check user is postgres on postgres
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'postgres' AND current_database() = 'postgres' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on postgres database (%)', 
			user, current_database();	
	END IF;
END;
$$;

--
-- Tablespace must exist
-- 
-- CREATE TABLESPACE sahsuland_dev LOCATION 'C:\\PostgresDB\\sahsuland_dev';

DROP DATABASE IF EXISTS sahsuland_dev;
CREATE DATABASE sahsuland_dev WITH OWNER rif40 TABLESPACE sahsuland_dev;
COMMENT ON DATABASE sahsuland_dev IS 'RIF V4.0 PostGres SAHSULAND Development Database';

--
-- This could be cleverer!!
--
GRANT CONNECT ON DATABASE sahsuland_dev to pch;
GRANT ALL ON DATABASE sahsuland_dev to rif40;

\c sahsuland_dev postgres localhost 
CREATE SCHEMA pch AUTHORIZATION pch;
GRANT ALL ON SCHEMA pch TO pch;
--
-- Check user is postgres on sahsuland
--
\set ECHO OFF
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'postgres' AND current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on postgres database (%)', 
			user, current_database();	
	END IF;
END;
$$;
\set echo ALL
CREATE EXTENSION IF NOT EXISTS plr;

RESET rif40.password;
--
-- Teo database restore options:
--
-- 1) Create from scripts
--
-- Run from directory: rifDatabase\Postgres\psql_scripts
--\c sahsuland_dev rif40 localhost
--\i v4_0_create_sahsuland.sql
--\q 
--
-- 2) Import from sahsuland_dev.dump (Not supplied as part of the Github repository)
--
\echo Restoring database...
--
-- Powershell version
--
\! powershell -command "pg_restore -d sahsuland_dev -U postgres -v  sahsuland_dev.dump 2>&1 | tee ('pg_restore{0}.log' -f (Get-Date -format 'yyyy.MM.dd-HH.mm'))"
--
-- Display errors
--
\echo Database restore errors...
\! findstr "ERROR:" pg_restore.txt
/*

Errors like this can be ignored - others may not!

pg_restore: [archiver (db)] could not execute query: ERROR:  could not load library "C:/Program Files/PostgreSQL/9.3/lib/plperl.dll"
: The specified module could not be found.
pg_restore: [archiver (db)] could not execute query: ERROR:  extension "plperl" does not exist
pg_restore: [archiver (db)] could not execute query: ERROR:  could not open extension control file "C:/Program Files/PostgreSQL/9.3/
share/extension/oracle_fdw.control": No such file or directory
pg_restore: [archiver (db)] could not execute query: ERROR:  extension "oracle_fdw" does not exist
pg_restore: [archiver (db)] could not execute query: ERROR:  rule "geometry_columns_delete" for relation "geometry_columns" already
exists
pg_restore: [archiver (db)] could not execute query: ERROR:  rule "geometry_columns_insert" for relation "geometry_columns" already
exists
pg_restore: [archiver (db)] could not execute query: ERROR:  rule "geometry_columns_update" for relation "geometry_columns" already
exists
 */
\c sahsuland_dev rif40 localhost
ALTER DATABASE sahsuland_dev SET search_path TO rif40,public,topology,gis,pop,rif40_sql_pkg;

\c sahsuland_dev rif40 localhost
\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
	sql_stmt VARCHAR;
BEGIN
--
-- Force rebuild of user objects
--
	sql_stmt:='DROP VIEW IF EXISTS '||USER||'.rif40_user_version';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_ddl_checks:DEBUG3');
	PERFORM rif40_log_pkg.rif40_log_setup();
	PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
        PERFORM rif40_sql_pkg.rif40_startup();
	IF USER != 'rif40' THEN
	        PERFORM rif40_sql_pkg.rif40_ddl_checks();
	END IF;
	PERFORM rif40_log_pkg.rif40_remove_from_debug('rif40_ddl_checks');
END;
$$;

\echo SAHSUland_dev installed.
--
-- Eof
