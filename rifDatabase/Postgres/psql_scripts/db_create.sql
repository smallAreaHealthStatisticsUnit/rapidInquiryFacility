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
-- Rapid Enquiry Facility (RIF) - Database creation script
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

--\set ECHO all
\set ECHO none
\set ON_ERROR_STOP on
\echo Creating SAHSULAND database if required
--
-- This script is tightly coupled to the Makefile and requires the following variables to run:
--

--
-- Start transaction 1: extensions and user accounts, roles
--
BEGIN;

--
-- check connected as postgres to postgres
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_user != 'postgres' OR current_database() != 'postgres' THEN
		RAISE EXCEPTION 'db_create.sql() current_user: % and current database: % must both be postgres', current_user, current_database();
	END IF;
END;
$$;

--
-- Check command line parameters
--

--
-- Encrypted postgres and rif40 user passwords
--
\set npassword '''XXXX':postgres_password''''
SET rif40.encrypted_postgres_password TO :npassword;
\set npassword '''XXXX':rif40_password''''
SET rif40.encrypted_rif40_password TO :npassword;

--
-- Check user is postgres on postgres, check password
--
DO LANGUAGE plpgsql $$
DECLARE	
	c1up CURSOR(l_param VARCHAR) FOR 
		SELECT CURRENT_SETTING(l_param) AS password;
	c2up CURSOR(l_user VARCHAR) FOR 
		SELECT usename, passwd FROM pg_shadow WHERE usename = l_user;
--
	c1_rec RECORD;
	c2_rec RECORD;
BEGIN
	IF user = 'postgres' AND current_database() = 'postgres' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on postgres database (%)', 
			user, current_database();	
	END IF;
--
-- Check encrypted_rif40_password pareameter
--
	OPEN c1up('rif40.encrypted_rif40_password');
	FETCH c1up INTO c1_rec;
	CLOSE c1up;	
	IF UPPER(c1_rec.password) = 'XXXX' THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v encrypted_rif40_password=<encrypted_rif40_password password> parameter';	
	ELSE
		OPEN c2up('rif40');
		FETCH c2up INTO c2_rec;
		CLOSE c2up;
		IF c2_rec.passwd IS NULL THEN
			RAISE INFO 'db_create.sql() rif40 needs to be created encrypted password will be ="%"', SUBSTR(c1_rec.password, 5);		
		ELSIF c2_rec.passwd = SUBSTR(c1_rec.password, 5) THEN
			RAISE INFO 'db_create.sql() rif40 encrypted password="%"', SUBSTR(c1_rec.password, 5);
		ELSE
			RAISE EXCEPTION 'db_create.sql() rif40 encrypted password set by install script="%" differs from database: "%',
				SUBSTR(c1_rec.password, 5), c2_rec.passwd;	
		END IF;
	END IF;	
--
-- Check encrypted_postgres_password parameter
--
	OPEN c1up('rif40.encrypted_postgres_password');
	FETCH c1up INTO c1_rec;
	CLOSE c1up;	
	IF UPPER(c1_rec.password) = 'XXXX' THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v encrypted_postgres_password=<encrypted_postgres_password password> parameter';	
	ELSE
		OPEN c2up('postgres');
		FETCH c2up INTO c2_rec;
		CLOSE c2up;
		IF c2_rec.passwd = SUBSTR(c1_rec.password, 5) THEN
			RAISE INFO 'db_create.sql() postgres encrypted password="%"', SUBSTR(c1_rec.password, 5);
		ELSE
			RAISE EXCEPTION 'db_create.sql() postgres encrypted password set by install script="%" differs from database: "%',
				SUBSTR(c1_rec.password, 5), c2_rec.passwd;	
		END IF;
	END IF;		
--

	
END;
$$;

SET rif40.encrypted_postgres_password TO :postgres_password;
SET rif40.encrypted_rif40_password TO :rif40_password;

--
-- Check DB version
--
DO LANGUAGE plpgsql $$
DECLARE
-- PostgreSQL 9.3.5 on x86_64-apple-darwin, compiled by i686-apple-darwin11-llvm-gcc-4.2 (GCC) 4.2.1 (Based on Apple Inc. build 5658) (LLVM build 2336.9.00), 64-bito
--
-- Use SELECT current_setting('server_version_num') to get numeric version, which is
-- better for comparing.
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
            RAISE WARNING 'db_create.sql(): unsupported version() function: %', version();
            RAISE;
	END;
	--
	IF c1_rec.numeric_version < 90300 THEN
		RAISE EXCEPTION 'db_create.sql() C902xx: RIF requires Postgres version 9.3 or higher; current version: %',
			c1_rec.version::VARCHAR;
	ELSIF c1_rec.numeric_version < 90305 THEN
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
--
	sql_stmt VARCHAR;
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
--
	sql_stmt:='SET rif40.use_plr TO '||SUBSTR(c1_rec.use_plr, 5);
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;


--
-- Create SAHSULAND only (no SAHSULAND_DEV) (default N)
--
\set ncreate_sahsuland_only '''XXXX':create_sahsuland_only''''
SET rif40.create_sahsuland_only TO :ncreate_sahsuland_only;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR SELECT CURRENT_SETTING('rif40.create_sahsuland_only') AS create_sahsuland_only;
	c1_rec RECORD;
--
	sql_stmt VARCHAR;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF UPPER(c1_rec.create_sahsuland_only) = 'XXXX' THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v create_sahsuland_only=<create_sahsuland_only> parameter';
	ELSE
		RAISE INFO 'db_create.sql() postgres create_sahsuland_only="%"', SUBSTR(c1_rec.create_sahsuland_only, 5);
	END IF;
--
	sql_stmt:='SET rif40.create_sahsuland_only TO '||SUBSTR(c1_rec.create_sahsuland_only, 5);
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;

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
				RAISE WARNING 'db_create.sql() RIF required extension: % is not installable;%See http://www.joeconway.com/plr/doc/plr-install.html for details%', 
				x, E'\n'::Text, E'\n'::Text;
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
DECLARE	
	c1_r CURSOR FOR 
		SELECT *
	      FROM pg_extension
		 WHERE extname = 'plr';
	c1_rec RECORD;
BEGIN
	IF UPPER(CURRENT_SETTING('rif40.use_plr')) = 'Y' THEN
--
-- Test PLR can be installed
--
		CREATE EXTENSION IF NOT EXISTS plr;
--
-- Test is installed correctly
--
		OPEN c1_r;
		FETCH c1_r INTO c1_rec;
		CLOSE c1_r;
		IF c1_rec.extname IS NOT NULL THEN
			RAISE INFO 'PL/R extension version % loaded', 
				c1_rec.extversion::Text;
		END IF;
		DROP EXTENSION plr;
	END IF;
EXCEPTION
	WHEN undefined_file THEN
--
-- Catch:
-- psql:db_create.sql:319: ERROR:  could not load library "C:/Program Files/PostgreSQL/9.3/lib/plr.dll": The specified module could not be found.
--
		RAISE WARNING 'Caught: %; add R library location to path; and R_HOME; restart Postgres', sqlstate;
		RAISE;
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
		SELECT CURRENT_SETTING('rif40.encrypted_postgres_password') AS encrypted_postgres_password,
		       CURRENT_SETTING('rif40.encrypted_rif40_password') AS encrypted_rif40_password;
	c3 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_roles WHERE rolname = l_name;
	c4 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_user WHERE usename = l_name;
	c5 CURSOR(l_user VARCHAR) FOR
		SELECT 'md5'||md5(l_user||l_user) AS new_passwd, passwd FROM pg_shadow
		 WHERE usename = l_user;
	c11 CURSOR(l_name VARCHAR) FOR 
		SELECT *
		  FROM pg_user WHERE usename = l_name;
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
	c5_rec RECORD;
	c11_rec RECORD;
--
	userlist VARCHAR[]:=ARRAY['rif40', 'gis', 'pop', 'notarifuser', 
			'test_rif_no_suppression', 'test_rif_user', 'test_rif_manager', 'test_rif_student'];
	rolelist VARCHAR[]:=ARRAY['rif_user', 'rif_manager', 'rif_no_suppression', 'rifupg34', 'rif_student'];
	x VARCHAR;
	sql_stmt VARCHAR;
BEGIN
	OPEN c2;
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	OPEN c4('postgres');
	FETCH c4 INTO c4_rec;
	CLOSE c4;
	IF c4_rec.passwd IS NULL THEN
		RAISE INFO 'db_create.sql() postgres user has no password';
	ELSE
		RAISE NOTICE 'db_create.sql() changing postgres user password';
		sql_stmt:='ALTER USER postgres ENCRYPTED PASSWORD  '''||c2_rec.encrypted_postgres_password||'''';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
-- Now fixed
		EXECUTE sql_stmt;
	END IF;
--
-- Non login roles
--
	FOREACH x IN ARRAY rolelist LOOP
		OPEN c3(x);
		FETCH c3 INTO c3_rec;
		CLOSE c3;
		IF c3_rec.rolname IS NOT NULL THEN
			RAISE INFO 'db_create.sql() RIF schema role % exists', c3_rec.rolname::VARCHAR;
--
-- User is a rif user; and MUST be non privileged; streaming replication for backup is allowed
--
			OPEN c11(c3_rec.rolname);
			FETCH c11 INTO c11_rec;
			CLOSE c11;
			IF c11_rec.usesuper IS NULL THEN
				RAISE INFO 'db_create.sql() Role % is not a superuser',
					c3_rec.rolname::VARCHAR;
			ELSIF c11_rec.usesuper THEN
				RAISE EXCEPTION 'db_create.sql() C209xy: Role % is superuser', 
					c11_rec.usename::VARCHAR;
			ELSIF c11_rec.usecreatedb THEN
				RAISE EXCEPTION 'db_create.sql() C209xx: Role % has database creation privilege', 
					c11_rec.usename::VARCHAR;
			ELSIF SUBSTR(version(), 12, 3)::NUMERIC < 9.5 THEN
				IF c11_rec.usecatupd THEN
					RAISE EXCEPTION 'db_create.sql() C209xx: Role % has update system catalog privilege', 
						c11_rec.usename::VARCHAR;
				END IF;
			END IF;
--
		ELSE
	    		sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT NOLOGIN NOREPLICATION';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;
	END LOOP;
--
-- Logon roles (users)
--
	FOREACH x IN ARRAY userlist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.usename IS NOT NULL THEN
--
-- User is a rif user; and MUST be non privileged; streaming replication for backup is allowed
--
			OPEN c11(c1_rec.usename);
			FETCH c11 INTO c11_rec;
			CLOSE c11;
			IF c11_rec.usesuper IS NULL THEN
				RAISE EXCEPTION 'db_create.sql() C209xx: Cannot detect if user % is superuser', 
					c1_rec.usename::VARCHAR;
			ELSIF c11_rec.usesuper THEN
				RAISE EXCEPTION 'db_create.sql() C209xx: User % is superuser', 
					c11_rec.usename::VARCHAR;
			ELSIF c11_rec.usecreatedb THEN
				RAISE EXCEPTION 'db_create.sql() C209xx: User % has database creation privilege', 
					c11_rec.usename::VARCHAR;
			ELSIF SUBSTR(version(), 12, 3)::NUMERIC < 9.5 THEN
				IF c11_rec.usecatupd THEN
					RAISE EXCEPTION 'db_create.sql() C209xx: User % has update system catalog privilege', 
						c11_rec.usename::VARCHAR;
				END IF;
            ELSE
                RAISE INFO 'db_create.sql() privilege check OK for: %', x;
			END IF;
--
			OPEN c4(x);
			FETCH c4 INTO c4_rec;
			CLOSE c4;
			OPEN c5(x);
			FETCH c5 INTO c5_rec;
			CLOSE c5;			
			IF c4_rec.passwd IS NULL THEN
				RAISE INFO 'db_create.sql() RIF schema user % exists, no password', c1_rec.usename::VARCHAR;
			ELSIF c1_rec.usename = 'rif40' THEN
				RAISE INFO 'db_create.sql() RIF schema user % exists; changing password to encrypted', c1_rec.usename::VARCHAR;
				sql_stmt:='ALTER USER '||c1_rec.usename||' ENCRYPTED PASSWORD  '''||
					c2_rec.encrypted_rif40_password||'''';
				RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
-- Now fixed
				EXECUTE sql_stmt;
			ELSIF c5_rec.passwd IS NULL THEN
				RAISE INFO 'db_create.sql() Create RIF schema user % password as username', c1_rec.usename::VARCHAR;
				sql_stmt:='ALTER USER '||c1_rec.usename||' PASSWORD  '''||c1_rec.usename||'''';
				RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
				EXECUTE sql_stmt;			
			ELSIF c5_rec.passwd = c5_rec.new_passwd THEN
				RAISE INFO 'db_create.sql() RIF schema user % exists; password is username', c1_rec.usename::VARCHAR;
				sql_stmt:='ALTER USER '||c1_rec.usename||' PASSWORD  '''||c1_rec.usename||'''';
				RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
				EXECUTE sql_stmt;			
			ELSE
				RAISE WARNING 'db_create.sql() RIF schema user % exists; changing password to username', c1_rec.usename::VARCHAR;
				sql_stmt:='ALTER USER '||c1_rec.usename||' PASSWORD  '''||c1_rec.usename||'''';
				RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
				EXECUTE sql_stmt;
			END IF;
--
-- Code owner: rif40
--
		ELSIF x = 'rif40' THEN
	    	sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION ENCRYPTED PASSWORD '''||
				c2_rec.encrypted_rif40_password||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
--
-- Test user: not_a_rif_user - should have no access at all
--
		ELSIF x = 'notarifuser' THEN
	    	sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||
				x||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
--
-- Data owners, non health data (so wider access can be granted if required)
--
		ELSIF x IN ('gis', 'pop') THEN
	    	sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||
				x||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
--
-- Test users for roles
--
		ELSIF x IN ('test_rif_no_suppression', 'test_rif_user', 'test_rif_manager', 'test_rif_student') THEN
	    	sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||
				x||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
			sql_stmt:='GRANT '||right(x, -5)||' TO '||x;
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
        ELSE
            RAISE WARNING 'db_create.sql() Do nothing for user: %', x;
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
\echo ntestuser
SET rif40.testuser TO :ntestuser;
\set ntestpassword '''XXXX':newpw''''
\echo ntestpassword
SET rif40.testpassword TO :ntestpassword;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.testuser') AS testuser,
		       CURRENT_SETTING('rif40.testpassword') AS testpassword;
	c2 CURSOR(l_usename VARCHAR) FOR 
		SELECT * FROM pg_user WHERE usename = l_usename;
	c1_rec RECORD;
	c2_rec RECORD;
--
	sql_stmt VARCHAR;
	u_name	VARCHAR;
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
		RAISE INFO 'db_create.sql() test user account parameter="%"', c1_rec.testpassword;
	END IF;
	u_name:=LOWER(SUBSTR(CURRENT_SETTING('rif40.testuser'), 5));
--
-- Test account exists
--
	OPEN c2(u_name);
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.usename IS NULL THEN
		RAISE NOTICE 'db_create.sql() C209xx: User account does not exist: %; creating', u_name;	
		sql_stmt:='CREATE ROLE '||LOWER(SUBSTR(c1_rec.testuser, 5))||
			' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||LOWER(SUBSTR(c1_rec.testpassword, 5))||'''';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	ELSE
	    RAISE INFO 'db_create.sql() RIF schema user % exists; changing password to encrypted',
	        c2_rec.usename::VARCHAR;
		sql_stmt:='ALTER USER '||c2_rec.usename||' ENCRYPTED PASSWORD  '''||LOWER(SUBSTR(c1_rec.testpassword, 5))||'''';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;

	    IF pg_has_role(c2_rec.usename, 'rif_user', 'MEMBER') THEN
		    RAISE INFO 'db_create.sql() user account="%" is a rif_user', c2_rec.usename;
	    ELSIF pg_has_role(c2_rec.usename, 'rif_manager', 'MEMBER') THEN
		    RAISE INFO 'db_create.sql() user account="%" is a rif manager', c2_rec.usename;
	    ELSE
		    RAISE EXCEPTION 'db_create.sql() C209xx: User account: % is not a rif_user or rif_manager', c2_rec.usename;
	    END IF;
	END IF;
--
	sql_stmt:='GRANT CONNECT ON DATABASE postgres to '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='GRANT rif_manager TO '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='GRANT rif_user TO '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
	sql_stmt:='GRANT CONNECT ON DATABASE postgres to rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;

--
-- End transaction 1: extensions and user accounts, roles
--
END;

--
\echo *****************************************************************************************************
\echo * Try to connect as test user. This will fail if pgpass is not setup correctly
\echo '* Stored password file is in ~/.pgpass on Linux/Macos or: %APPDATA%/postgresql/pgpass.conf on Windows'
\echo * Format is: hostname:port:database:username:password
\echo *****************************************************************************************************
--
\set ECHO all
\c postgres :testuser :pghost
\echo "Try to connect as rif40. This will fail if if the password file is not setup correctly"
\c postgres rif40 :pghost
\echo "Try to re-connect as postgres. This will fail if if the password file is not setup correctly"
-- Re-connect as postgres
\c postgres postgres :pghost

\set ECHO none
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
\set nsahsuland_tablespace_dir '''XXXX':tablespace_dir''''
SET rif40.sahsuland_tablespace_dir TO :nsahsuland_tablespace_dir;
SET rif40.os TO :os;

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
		RAISE NOTICE 'db_create.sql() No -v sahsuland_tablespace_dir=<sahsuland_tablespace_dir> parameter';	
		sql_stmt:='SET rif40.sahsuland_tablespace_dir TO ''''';
-- 
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
--
-- Escape sahsuland_tablespace_dir appropriately for os
--
	ELSIF CURRENT_SETTING('rif40.os') = 'windows_nt' THEN
		RAISE INFO 'db_create.sql() Windows sahsuland_tablespace_dir="%"', 
				SUBSTR(c1_rec.sahsuland_tablespace_dir, 5);
		sql_stmt:='SET rif40.sahsuland_tablespace_dir TO ''"'||
			REPLACE(SUBSTR(c1_rec.sahsuland_tablespace_dir, 5), '\', '/')||
			'"''';
-- '
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	ELSIF CURRENT_SETTING('rif40.os') IN ('linux', 'darwin', 'macos') THEN
		RAISE INFO 'db_create.sql() Windows postgres sahsuland_tablespace_dir="%"', 
				SUBSTR(c1_rec.sahsuland_tablespace_dir, 5);
		sql_stmt:='SET rif40.sahsuland_tablespace_dir TO '''||
			REPLACE(SUBSTR(c1_rec.sahsuland_tablespace_dir, 5), ' ', '\ ')||
			'''';
-- '
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	ELSE	
		RAISE EXCEPTION 'db_create.sql() Unsupported OS: %', CURRENT_SETTING('rif40.os');
	END IF;
END;
$$;
 
--
-- Re-create SAHSULAND
--
\set ECHO ALL
SET rif40.create_sahsuland_only TO :create_sahsuland_only;
-- SET rif40.sahsuland_tablespace_dir TO :sahsuland_tablespace_dir;

WITH a AS (
	SELECT unnest(ARRAY['rif40.sahsuland_tablespace_dir', 
			'rif40.create_sahsuland_only', 'rif40.os']) AS param
	)
SELECT param,
       CURRENT_SETTING(a.param) AS value
	     FROM a;
\set sahsuland_tablespace_dir TO rif40.sahsuland_tablespace_dir;

--
-- Conditionally create a script to recreate sahsuland_dev
-- Database creation is not transactional!
--
-- Create SQL
--
CREATE OR REPLACE VIEW recreate_sahsu_dev_tmp
AS
WITH ts AS (
	SELECT CASE
	           WHEN CURRENT_SETTING('rif40.sahsuland_tablespace_dir')::Text != '' 
						THEN  'TABLESPACE sahsuland'
		       ELSE '/* No sahsuland tablespace */' 
		   END AS sahsuland_tablespace
), a AS (
	SELECT 'CREATE TABLESPACE sahsuland LOCATION '''||
		CURRENT_SETTING('rif40.sahsuland_tablespace_dir')||''';' AS sql_stmt, 
       10 AS ord 
	 WHERE CURRENT_SETTING('rif40.sahsuland_tablespace_dir')::Text != ''
	   AND 'sahsuland' NOT IN (SELECT spcname FROM pg_tablespace WHERE spcname = 'sahsuland')
),b AS (
SELECT 'DROP DATABASE IF EXISTS sahsuland_dev;' AS sql_stmt, 
       20 AS ord 
UNION 
SELECT 'CREATE DATABASE sahsuland_dev WITH OWNER rif40 '||ts.sahsuland_tablespace||';' AS sql_stmt, 
       30 AS ord 
  FROM ts
UNION 
SELECT 'COMMENT ON DATABASE sahsuland_dev IS ''RIF V4.0 PostGres SAHSULAND development Database'';' AS sql_stmt,
       40 AS ord
), c AS (
SELECT 'DROP DATABASE IF EXISTS sahsuland;' AS sql_stmt,
       50 AS ord
UNION 
SELECT 'CREATE DATABASE sahsuland WITH OWNER rif40 '||ts.sahsuland_tablespace||';' AS sql_stmt,
       60 AS ord
  FROM ts
UNION 
SELECT 'COMMENT ON DATABASE sahsuland IS ''RIF V4.0 PostGres SAHSULAND Example Database'';' AS sql_stmt,
       70 AS ord
), c2 AS (
SELECT 'DROP DATABASE IF EXISTS sahsuland_empty;' AS sql_stmt,
       80 AS ord
UNION 
SELECT 'CREATE DATABASE sahsuland_empty WITH OWNER rif40 '||ts.sahsuland_tablespace||';' AS sql_stmt,
       90 AS ord
  FROM ts
UNION 
SELECT 'COMMENT ON DATABASE sahsuland_empty IS ''RIF V4.0 PostGres SAHSULAND Empty Database'';' AS sql_stmt,
       100 AS ord
), d AS (
SELECT '--' AS sql_stmt, 0 AS ord
UNION
SELECT '-- ************************************************************************' AS sql_stmt, 1 AS ord
UNION
SELECT '--' AS sql_stmt, 2 AS ord
UNION
SELECT '-- Description:' AS sql_stmt, 3 AS ord
UNION
SELECT '--' AS sql_stmt, 4 AS ord
UNION
SELECT '-- Rapid Enquiry Facility (RIF) - Database creation autogenerated temp script to create tablespace and databases' AS sql_stmt,
       5 AS ord
UNION
SELECT '--' AS sql_stmt, 6 AS ord
UNION
SELECT '--' AS sql_stmt, 998 AS ord
UNION
SELECT '-- Eof' AS sql_stmt, 999 AS ord
)
SELECT * FROM a 
UNION
SELECT * FROM b
 WHERE CURRENT_SETTING('rif40.create_sahsuland_only') IN ('n', 'N') 
UNION
SELECT * FROM c
UNION
SELECT * FROM c2
UNION
SELECT * FROM d;

SELECT sql_stmt FROM recreate_sahsu_dev_tmp
 ORDER BY ord;
 
--
-- Extract SQL to run
--
\copy (SELECT sql_stmt FROM recreate_sahsu_dev_tmp ORDER BY ord) TO recreate_sahsu_dev_tmp.sql WITH (FORMAT text)

DROP VIEW IF EXISTS recreate_sahsu_dev_tmp;

DO $$
BEGIN
--
	IF CURRENT_SETTING('rif40.create_sahsuland_only') NOT IN ('n', 'N') THEN
		RAISE INFO 'Database: sahusland_dev not re-created, create_sahsuland_only=%', CURRENT_SETTING('rif40.create_sahsuland_only');
	END IF;
END
$$;

--
-- Execute
--
\i recreate_sahsu_dev_tmp.sql

\c sahsuland postgres :pghost 

--
-- Start transaction 2: sahsuland build
--
BEGIN;
--
-- Check user is postgres on sahsuland
--
\set ECHO none
SET rif40.use_plr TO :use_plr;
SET rif40.testuser TO :ntestuser;
DO LANGUAGE plpgsql $$
DECLARE	
	sql_stmt VARCHAR;
	u_name	VARCHAR;
BEGIN
	u_name:=LOWER(SUBSTR(CURRENT_SETTING('rif40.testuser'), 5));
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
	sql_stmt:='CREATE EXTENSION IF NOT EXISTS adminpack';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='CREATE EXTENSION IF NOT EXISTS dblink';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;	
	IF UPPER(CURRENT_SETTING('rif40.use_plr')) IN ('y', 'Y') THEN
--
-- Install PLR. PL/R is untrusted because R gives access to system commands and the
-- file system; so use of the language must be by the super user
--
		sql_stmt:='CREATE EXTENSION IF NOT EXISTS plr';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT EXECUTE ON FUNCTION public.install_rcmd(text) to rif40';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	END IF;
--
-- RIF40 grants
--	
	sql_stmt:='GRANT ALL ON DATABASE sahsuland to rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='REVOKE CREATE ON SCHEMA public FROM rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
	sql_stmt:='GRANT CONNECT ON DATABASE sahsuland to '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
-- Set default search pathname
--
	sql_stmt:='ALTER DATABASE sahsuland SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
\set echo ALL
--
-- End transaction 2: sahsuland build
--
END;
 
\c sahsuland_empty postgres :pghost 
--
-- Start transaction 3: sahsuland_empty build
--
BEGIN;
--
-- Check user is postgres on sahsuland_empty
--
\set ECHO none
SET rif40.use_plr TO :use_plr;
SET rif40.testuser TO :ntestuser;
DO LANGUAGE plpgsql $$
DECLARE	
	c1 CURSOR(l_schema VARCHAR) FOR
		SELECT * FROM information_schema.schemata
		 WHERE LOWER(schema_name) = LOWER(l_schema);
	c2 CURSOR FOR
		 SELECT usename
		   FROM pg_user
		 WHERE pg_has_role(usename, 'rif_manager', 'MEMBER')
		    OR pg_has_role(usename, 'rif_user', 'MEMBER')
		    OR pg_has_role(usename, 'rif_student', 'MEMBER')
		  ORDER BY 1;
	c1_rec RECORD;
	c2_rec RECORD;
--
	schemalist VARCHAR[]:=ARRAY['rif40_dmp_pkg', 'rif40_sql_pkg', 'rif40_sm_pkg', 'rif40_log_pkg', 'rif40_trg_pkg',
			'rif40_geo_pkg', 'rif40_xml_pkg', 'rif40_R_pkg', 
			'rif40', 'gis', 'pop', 'rif_studies', 'rif_data', 'data_load', 'rif40_partitions'];
	x VARCHAR;
	sql_stmt VARCHAR;
	u_name	VARCHAR;
BEGIN
	u_name:=LOWER(SUBSTR(CURRENT_SETTING('rif40.testuser'), 5));
	IF user = 'postgres' AND current_database() = 'sahsuland_empty' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on sahsuland_empty database (%)', 
			user, current_database();	
	END IF;
--
	sql_stmt:='CREATE EXTENSION IF NOT EXISTS postgis';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='CREATE EXTENSION IF NOT EXISTS adminpack';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	IF UPPER(CURRENT_SETTING('rif40.use_plr')) IN ('y', 'Y') THEN
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
	sql_stmt:='GRANT ALL ON DATABASE sahsuland_empty to rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='REVOKE CREATE ON SCHEMA public FROM rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
	sql_stmt:='GRANT CONNECT ON DATABASE sahsuland_empty to '||u_name;
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
		sql_stmt:='GRANT ALL 
		ON SCHEMA '||u_name||' TO '||u_name;
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	END IF;
--
-- Re-create user schemas
--
	FOR c2_rec IN c2 LOOP
		OPEN c1(c2_rec.usename);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.schema_name IS NULL THEN
			sql_stmt:='CREATE SCHEMA '||c2_rec.usename||' AUTHORIZATION '||c2_rec.usename;
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
			sql_stmt:='GRANT ALL 
			ON SCHEMA '||c2_rec.usename||' TO '||c2_rec.usename;
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;	
	END LOOP;
--
	FOREACH x IN ARRAY schemalist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.schema_name IS NULL THEN
			sql_stmt:='CREATE SCHEMA '||x||' AUTHORIZATION rif40';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;
		sql_stmt:='GRANT ALL ON SCHEMA '||x||' TO rif40 WITH GRANT OPTION';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT USAGE ON SCHEMA '||x||' TO rif_user';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT USAGE ON SCHEMA '||x||' TO rif_manager WITH GRANT OPTION';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
--
-- Grant CREATE privilege to data load schema; allow onwards grants
--
		IF x IN ('rif_data', 'data_load') THEN
				sql_stmt:='GRANT CREATE ON SCHEMA '||x||' TO rif_manager WITH GRANT OPTION';
				RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
				EXECUTE sql_stmt;
		END IF;
	END LOOP;

--
-- Set default search pathname
--
	sql_stmt:='ALTER DATABASE sahsuland_empty SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
\set echo ALL

--
-- Set search path for rif40
--
ALTER USER rif40 SET search_path 
	TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
SET search_path 
	TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
	
--
-- End transaction 3: sahsuland_empty build
--
END;

\c sahsuland_dev postgres :pghost 
--
-- Start transaction 4: sahsuland_dev build
--
BEGIN;
--
-- Check user is postgres on sahsuland_dev
--
--\set ECHO none
SET rif40.use_plr TO :use_plr;
SET rif40.testuser TO :ntestuser;
DO LANGUAGE plpgsql $$
DECLARE	
	c1 CURSOR(l_schema VARCHAR) FOR
		SELECT * FROM information_schema.schemata
		 WHERE LOWER(schema_name) = LOWER(l_schema);
	c2 CURSOR FOR
		 SELECT usename
		   FROM pg_user
		 WHERE pg_has_role(usename, 'rif_manager', 'MEMBER')
		    OR pg_has_role(usename, 'rif_user', 'MEMBER')
		    OR pg_has_role(usename, 'rif_student', 'MEMBER')
		  ORDER BY 1;
	c1_rec RECORD;
	c2_rec RECORD;
--
	schemalist VARCHAR[]:=ARRAY['rif40_dmp_pkg', 'rif40_sql_pkg', 'rif40_sm_pkg', 'rif40_log_pkg', 'rif40_trg_pkg',
			'rif40_geo_pkg', 'rif40_xml_pkg', 'rif40_R_pkg', 
			'rif40', 'gis', 'pop', 'rif_studies', 'rif_data', 'data_load', 'rif40_partitions'];
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
	sql_stmt:='CREATE EXTENSION IF NOT EXISTS adminpack';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	IF UPPER(CURRENT_SETTING('rif40.use_plr')) IN ('y', 'Y') THEN
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
		sql_stmt:='GRANT ALL 
		ON SCHEMA '||u_name||' TO '||u_name;
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	END IF;
--
-- Re-create user schemas
--
	FOR c2_rec IN c2 LOOP
		OPEN c1(c2_rec.usename);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.schema_name IS NULL THEN
			sql_stmt:='CREATE SCHEMA '||c2_rec.usename||' AUTHORIZATION '||c2_rec.usename;
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
			sql_stmt:='GRANT ALL 
			ON SCHEMA '||c2_rec.usename||' TO '||c2_rec.usename;
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;	
	END LOOP;
--
	FOREACH x IN ARRAY schemalist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.schema_name IS NULL THEN
			sql_stmt:='CREATE SCHEMA '||x||' AUTHORIZATION rif40';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;
		sql_stmt:='GRANT ALL ON SCHEMA '||x||' TO rif40 WITH GRANT OPTION';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT USAGE ON SCHEMA '||x||' TO rif_user';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT USAGE ON SCHEMA '||x||' TO rif_manager WITH GRANT OPTION';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
--
-- Grant CREATE privilege to data load schema; allow onwards grants
--
		IF x IN ('rif_data', 'data_load') THEN
				sql_stmt:='GRANT CREATE ON SCHEMA '||x||' TO rif_manager WITH GRANT OPTION';
				RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
				EXECUTE sql_stmt;
		END IF;
	END LOOP;

--
-- Set default search pathname
--
	sql_stmt:='ALTER DATABASE sahsuland_dev SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
\set echo ALL

--
-- Set search path for rif40
--
ALTER USER rif40 SET search_path 
	TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
SET search_path 
	TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
	
--
-- End transaction 4: sahsuland_dev build
--

END;
SHOW search_path;

/*
DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'Stop.';
END;
$$;
 */
--
-- Eof
