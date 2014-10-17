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
-- Rapid Enquiry Facility (RIF) - Create PG psql code (SQL and Oracle compatibility processing)
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
\set ECHO OFF
\set ON_ERROR_STOP ON
\echo Creating SAHSULAND database if required

--
-- Start transaction 1: extensions and user accounts, roles
--
BEGIN;

--
-- Check command line parameters
--

--
-- Encrypted postgres user password
--
\set npassword '''XXXX':encrypted_postgres_password''''
SET rif40.password TO :npassword;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR SELECT CURRENT_SETTING('rif40.password') AS password;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF UPPER(c1_rec.password) = 'XXXX' THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v encrypted_postgres_password=<encrypted_postgres_password password> parameter';	
	ELSE
		RAISE INFO 'db_create.sql() postgres encrypted password="%"', SUBSTR(c1_rec.password, 5);
	END IF;
END;
$$;
SET rif40.password TO :encrypted_postgres_password;

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
-- Check DB version
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
	 	SELECT version() AS version, 
		SUBSTR(version(), 12, 3)::NUMERIC as major_version, 
		SUBSTR(version(), 16, position(', ' IN version())-16)::NUMERIC as minor_version;
	c1_rec RECORD;
--
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	--
	IF c1_rec.major_version < 9.3 THEN
		RAISE EXCEPTION 'db_create.sql() C902xx: RIF requires Postgres version 9.3 or higher; current version: %',
			c1_rec.version::VARCHAR;
	ELSIF c1_rec.major_version = 9.3 AND c1_rec.minor_version < 5 THEN 
--
-- Avoid postgis bug: ERROR: invalid join selectivity: 1.000000 
-- in PostGIS 2.1.1 (fixed in 2.2.1/2.1.2 - to be release May 3rd 2014)
--
-- See: http://trac.osgeo.org/postgis/ticket/2543
--
		RAISE EXCEPTION 'db_create.sql() C902xx: RIF requires Postgres version 9.3 minor version 5 or higher; minor version: "%"',
			c1_rec.minor_version::VARCHAR;
	ELSE
		RAISE INFO 'db_create.sql() RIF required Postgres version 9.3 or higher OK; current version: %', 
			c1_rec.version::VARCHAR;
	END IF;
END;
$$;

--
-- IS PL/R in use (default N)
--
\set nuse_plr '''XXXX':use_plr''''
SET rif40.use_plr TO :nuse_plr;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR SELECT CURRENT_SETTING('rif40.use_plr') AS use_plr;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF UPPER(c1_rec.use_plr) = 'XXXX' THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v use_plr=<use_plr> parameter';	
	ELSE
		RAISE INFO 'db_create.sql() postgres use_plr="%"', SUBSTR(c1_rec.use_plr, 5);
	END IF;
END;
$$;
SET rif40.use_plr TO :use_plr;

--
-- Check availability of extensions
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR(l_name VARCHAR) FOR
		SELECT name, default_version
		  FROM pg_available_extensions
		 WHERE name = l_name;
	c1_rec RECORD;
--
	namelist VARCHAR[]:=ARRAY['adminpack', 'plperl', 'postgis', 'postgis_topology', 'pgcrypto', 'sslinfo', 'xml2', 'dblink', 'plr'];
	x VARCHAR;
	i INTEGER:=0;
BEGIN
	FOREACH x IN ARRAY namelist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		IF c1_rec.name IS NULL THEN
			IF UPPER(CURRENT_SETTING('rif40.use_plr')) = 'Y' THEN
				RAISE WARNING 'db_create.sql() RIF required extension: % is not installable', x;
				i:=i+1;
			ELSE
				RAISE INFO 'db_create.sql() Optional extension: % is not installable', x;
			END IF;
		ELSE
			RAISE INFO 'db_create.sql() RIF required extension: % V% is installable', c1_rec.name, c1_rec.default_version;
		END IF;
		CLOSE c1;
	END LOOP;
	IF i > 0 THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: % RIF required extensions are not installable', 
			i::VARCHAR USING HINT='See previous warnings';
	END IF;
END;
$$;

DO LANGUAGE plpgsql $$

--
-- This will fail if:
--
-- a) PLR (http://www.joeconway.com/plr) is not installed as per instructions
-- 
/*
In the instructions below:
	PostgreSQL is installed to <pgdir>
	R is installed to <rdir>

For example, these directories might be:
	<pgdir> = C:\PostgreSQL\9.3
	<rdir>  = C:\R\R-3.0.2

I recommend you ensure there are no spaces in the pathname to either <pgdir> or <rdir>.
Correct quoting of pathnames with spaces is left as an exercise for the reader ;-)

The following files are contained in this zip file:
---------------------------------------------------
README.txt:			place in <pgdir>\doc\extension
plr.dll:			place in <pgdir>\lib
plr.sql:			place in <pgdir>\share\extension
plr.control:			place in <pgdir>\share\extension
plr--8.3.0.15.sql		place in <pgdir>\share\extension
plr--unpackaged--8.3.0.15.sql	place in <pgdir>\share\extension

Ensure the following environment variables are set *prior* to starting PostgreSQL:
---------------------------------------------------
PATH=<pgdir>\bin;<rdir>\bin\x64;$PATH
R_HOME=<rdir
 */
--
-- See also: http://www.bostongis.com/PrinterFriendly.aspx?content_name=postgresql_plr_tut01
--           http://www.joeconway.com/plr/doc/plr-install.html 
--
-- b) Postgres is NOT restarted
--

BEGIN
	IF UPPER(CURRENT_SETTING('rif40.use_plr')) = 'Y' THEN
--
-- Test PLR can be installed
--
		CREATE EXTENSION IF NOT EXISTS plr;
		DROP EXTENSION plr;
	END IF;
END;
$$;

\echo "Create users and roles"
--
-- Create users and roles
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_user WHERE usename = l_name;
	c2 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.password') AS password;
	c3 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_roles WHERE rolname = l_name;

	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
--
	userlist VARCHAR[]:=ARRAY['rif40', 'gis', 'pop'];
	rolelist VARCHAR[]:=ARRAY['rif_user', 'rif_manager', 'rif_no_suppression', 'rifupg34', 'rif_student'];
	x VARCHAR;
	sql_stmt VARCHAR;
BEGIN
	OPEN c2;
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	sql_stmt:='ALTER USER postgres ENCRYPTED PASSWORD  '''||c2_rec.password||'''';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	PERFORM sql_stmt;
--
	FOREACH x IN ARRAY userlist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.usename IS NOT NULL THEN
			RAISE INFO 'db_create.sql() RIF schema user % exists', c1_rec.usename::VARCHAR;
				sql_stmt:='ALTER USER '||c1_rec.usename||' ENCRYPTED PASSWORD  '''||c2_rec.password||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			PERFORM sql_stmt;
		ELSE
	    	sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION ENCRYPTED PASSWORD '''||c2_rec.password||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;
	END LOOP;
--
	FOREACH x IN ARRAY rolelist LOOP
		OPEN c3(x);
		FETCH c3 INTO c3_rec;
		CLOSE c3;
		IF c3_rec.rolname IS NOT NULL THEN
			RAISE INFO 'db_create.sql() RIF schema role % exists', c3_rec.rolname::VARCHAR;
		ELSE

	    		sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT NOLOGIN NOREPLICATION';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;
	END LOOP;
--
-- Revoke PUBLIC
--
	sql_stmt:='REVOKE CREATE ON SCHEMA public FROM PUBLIC';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;

END;
$$;

--
-- Test user account
-- 
\set ntestuser '''XXXX':testuser''''
SET rif40.testuser TO :ntestuser;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.testuser') AS testuser;
	c2 CURSOR(l_usename VARCHAR) FOR 
		SELECT * FROM pg_user WHERE usename = l_usename;
	c1_rec RECORD;
	c2_rec RECORD;
--
	sql_stmt VARCHAR;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
-- Test parameter
--
	IF c1_rec.testuser IN ('XXXX', 'XXXX:testuser') THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v testuser=<test user account> parameter';	
	ELSE
		RAISE INFO 'db_create.sql() test user account parameter="%"', c1_rec.testuser;
	END IF;
--
-- Test account exists
--
	OPEN c2(LOWER(SUBSTR(c1_rec.testuser, 5)));
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.usename IS NULL THEN
		RAISE NOTICE 'db_create.sql() C209xx: User account does not exist: %; creating', LOWER(SUBSTR(c1_rec.testuser, 5));	
		sql_stmt:='CREATE ROLE '||LOWER(SUBSTR(c1_rec.testuser, 5))||
			' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||LOWER(SUBSTR(c1_rec.testuser, 5))||'''';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT rif_manager TO '||LOWER(SUBSTR(c1_rec.testuser, 5));
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT rif_user TO '||LOWER(SUBSTR(c1_rec.testuser, 5));
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	ELSIF pg_has_role(c2_rec.usename, 'rif_user', 'MEMBER') THEN
		RAISE INFO 'db_create.sql() user account="%" is a rif_user', c2_rec.usename;
	ELSIF pg_has_role(c2_rec.usename, 'rif_manager', 'MEMBER') THEN
		RAISE INFO 'db_create.sql() user account="%" is a rif manager', c2_rec.usename;
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User account: % is not a rif_user or rif_manager', c2_rec.usename;	
	END IF;
END;
$$;

--
-- End transaction 1: extensions and user accounts, roles
--
END;

--
\echo "Try to connect as test user. This will fail if pgpass is not setup correctly"
\echo "Stored password file is in ~/.pgpass on Linux or: %APPDATA%\postgresql\pgpass.conf on Windows"
\echo "Format is: hostname:port:database:username:password"
\c postgres :testuser
\echo "Try to connect as rif40. This will fail if if the password file is not setup correctly"
\c postgres rif40
-- Re-connect as postgres
\c postgres postgres

\echo ************************************************************************************
\echo *                                                                                  
\echo * WARNING !!!                                                                      
\echo *                                                                                  
\echo * This script will drop sahsuland, re-create it and set the passwords to the     
\echo * default                                                                          
\echo *                                                                                  
\echo ************************************************************************************

--
-- Location of SAHSULAND tablespace
--
\set nsahsuland_tablespace_dir '''XXXX':sahsuland_tablespace_dir''''
SET rif40.sahsuland_tablespace_dir TO :nsahsuland_tablespace_dir;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR SELECT CURRENT_SETTING('rif40.sahsuland_tablespace_dir') AS sahsuland_tablespace_dir;
	c1_rec RECORD;
--
	sql_stmt VARCHAR;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF UPPER(c1_rec.sahsuland_tablespace_dir) = 'XXXX' THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v use_plr=<sahsuland_tablespace_dir> parameter';	
	ELSE
		RAISE INFO 'db_create.sql() postgres sahsuland_tablespace_dir="%"', SUBSTR(c1_rec.sahsuland_tablespace_dir, 5);
		sql_stmt:='SET rif40.sahsuland_tablespace_dir TO '''||SUBSTR(c1_rec.sahsuland_tablespace_dir, 5)||'''';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	END IF;
END;
$$;

/*
 * Needs to be a separate program
 *
psql:db_create.sql:433: ERROR:  CREATE TABLESPACE cannot be executed from a function or multi-command string
CONTEXT:  SQL statement "CREATE TABLESPACE sahsuland LOCATION 'C:\PostgresDB\sahsuland'"
\echo Creating SAHSULAND tablespace...
--
-- Create SAHSULAND database
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_tablespace WHERE spcname = l_name;
	c1_rec RECORD;
--
	sql_stmt	VARCHAR;
BEGIN
	OPEN c1('sahsuland');
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.spcname IS NULL THEN
		sql_stmt:='CREATE TABLESPACE sahsuland LOCATION '''||CURRENT_SETTING('rif40.sahsuland_tablespace_dir')||'''';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	END IF;
--
END;
$$;
 */
 
\set echo ALL
DROP DATABASE IF EXISTS sahsuland;
-- 
-- Normally sahuslanbd_dev is NOT deleted
--
-- DROP DATABASE IF EXISTS sahsuland_dev;
CREATE DATABASE sahsuland WITH OWNER rif40 /* TABLESPACE sahsuland */;
COMMENT ON DATABASE sahsuland IS 'RIF V4.0 PostGres SAHSULAND Example Database';
\set ON_ERROR_STOP OFF
CREATE DATABASE sahsuland_dev WITH OWNER rif40 /* TABLESPACE sahsuland */;
\set ON_ERROR_STOP ON
COMMENT ON DATABASE sahsuland IS 'RIF V4.0 PostGres SAHSULAND development Database';

\c sahsuland postgres localhost 

--
-- Start transaction 2: sahsuland build
--
BEGIN;
--
-- Check user is postgres on sahsuland
--
\set ECHO OFF
SET rif40.use_plr TO :use_plr;
SET rif40.testuser TO :ntestuser;
DO LANGUAGE plpgsql $$
DECLARE	
	sql_stmt VARCHAR;
BEGIN
	IF user = 'postgres' AND current_database() = 'sahsuland' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on sahsuland database (%)', 
			user, current_database();	
	END IF;
--
	sql_stmt:='CREATE EXTENSION IF NOT EXISTS postgis';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	IF UPPER(CURRENT_SETTING('rif40.use_plr')) = 'Y' THEN
--
-- Test PLR can be installed
--
		sql_stmt:='CREATE EXTENSION IF NOT EXISTS plr';
	END IF;
--
-- RIF40 grants
--	
	sql_stmt:='GRANT ALL ON DATABASE sahsuland_dev to rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='REVOKE CREATE ON SCHEMA public FROM rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
	sql_stmt:='GRANT CONNECT ON DATABASE sahsuland_dev to '||LOWER(SUBSTR(CURRENT_SETTING('rif40.testuser'), 5));
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
\set echo ALL
--
-- End transaction 2: sahsuland build
--
END;
 
\c sahsuland_dev postgres localhost 
--
-- Start transaction 3: sahsuland_dev build
--
BEGIN;
--
-- Check user is postgres on sahsuland_dev
--
\set ECHO OFF
SET rif40.use_plr TO :use_plr;
SET rif40.testuser TO :ntestuser;
DO LANGUAGE plpgsql $$
DECLARE	
	c1 CURSOR(l_schema VARCHAR) FOR
		SELECT * FROM information_schema.schemata
		 WHERE schema_name = l_schema;
	c1_rec RECORD;
--
	schemalist VARCHAR[]:=ARRAY['rif40_dmp_pkg', 'rif40_sql_pkg', 'rif40_sm_pkg', 'rif40_log_pkg', 'rif40_trg_pkg',
			'rif40_geo_pkg', 'rif40_xml_pkg', 'rif40', 'gis', 'pop', 'rif_studies'];
	x VARCHAR;
	sql_stmt VARCHAR;
	u_name	VARCHAR;
BEGIN
	u_name:=LOWER(SUBSTR(CURRENT_SETTING('rif40.testuser'), 5));
	IF user = 'postgres' AND current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on sahsuland_dev database (%)', 
			user, current_database();	
	END IF;
--
	sql_stmt:='CREATE EXTENSION IF NOT EXISTS postgis';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	IF UPPER(CURRENT_SETTING('rif40.use_plr')) = 'Y' THEN
--
-- Test PLR can be installed
--
		sql_stmt:='CREATE EXTENSION IF NOT EXISTS plr';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	END IF;
--
-- RIF40 grants
--	
	sql_stmt:='GRANT ALL ON DATABASE sahsuland_dev to rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='REVOKE CREATE ON SCHEMA public FROM rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
	sql_stmt:='GRANT CONNECT ON DATABASE sahsuland_dev to '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
-- Add user, rif_studies schema and PKG (package) schemas
--
	OPEN c1(u_name);
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.schema_name IS NULL THEN
		sql_stmt:='CREATE SCHEMA '||u_name||' AUTHORIZATION '||u_name;
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT ALL ON SCHEMA '||u_name||' TO '||u_name;
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	END IF;
--
	FOREACH x IN ARRAY schemalist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.schema_name IS NULL THEN
			sql_stmt:='CREATE SCHEMA '||x||' AUTHORIZATION rif40';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
			sql_stmt:='GRANT ALL ON SCHEMA '||x||' TO rif40';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
			sql_stmt:='GRANT USAGE ON SCHEMA '||x||' TO rif_user';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
			sql_stmt:='GRANT USAGE ON SCHEMA '||x||' TO rif_manager';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;
	END LOOP;

--
-- Set default search pathname
--
	sql_stmt:='ALTER DATABASE sahsuland_dev SET search_path TO rif40, public, topology, gis, pop, rif40_sql_pkg, rif_studies';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
\set echo ALL
--
-- End transaction 3: sahsuland_dev build
--
END;
 
\q
 
\echo Restoring database...
--
-- Shell pg_restore
-- 
--\! pg_restore -d sahsuland -U postgres -v  sahsuland.dump > pg_restore.txt 2>&1
--
-- Powershell version
--
\! powershell -command "pg_restore -d sahsuland -U postgres -v  sahsuland.dump 2>&1 | tee ('pg_restore{0}.log' -f (Get-Date -format 'yyyy.MM.dd-HH.mm'))"
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
ALTER DATABASE sahsuland SET search_path TO rif40,public,topology,gis,pop,rif40_sql_pkg;

\q
\c sahsuland
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

\echo SAHSUland installed.
--
-- Eof
