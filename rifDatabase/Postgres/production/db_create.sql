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
-- Rapid Enquiry Facility (RIF) - Postgres Database creation script
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

--
-- TODO check valid names
--

--\set ECHO all
\set ECHO none
\set ON_ERROR_STOP on
--
-- This script is tightly coupled to rif40_database_install.bat and requires the following variables to run:
--
--	-v testuser=%NEWUSER% -v newdb=%NEWDB% -v newpw=%NEWPW% 
--	-v postgres_password=%PGPASSWORD% ^
--	-v rif40_password=%RIF40PW% 
--	-v tablespace_dir= 
--	-v pghost=localhost 
	
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

\set nnewdb '''XXXX':newdb''''
SET rif40.nnewdb TO :nnewdb;
SET rif40.newdb TO :newdb;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.nnewdb') AS nnewdb, 
		       CURRENT_SETTING('rif40.newdb') AS newdb,
			   REGEXP_REPLACE(CURRENT_SETTING('rif40.newdb'), 
					'[[:lower:]]{1,}[[:lower:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g') AS invalid_characters,
		       CASE
					WHEN LENGTH(REGEXP_REPLACE(CURRENT_SETTING('rif40.newdb'), 
						'[[:lower:]]{1,}[[:lower:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g')) > 0 THEN TRUE
					ELSE FALSE 
			   END::BOOLEAN AS is_invalid,
		       CASE
					WHEN LENGTH(CURRENT_SETTING('rif40.newdb')) > 30 THEN TRUE
					ELSE FALSE 
			   END::BOOLEAN AS length_invalid;
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
	IF c1_rec.nnewdb IN ('XXXX', 'XXXX:nnewdb') THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v newdb=<new database> parameter';	
	ELSIF c1_rec.is_invalid THEN
		RAISE EXCEPTION 'db_create.sql() new database name="%" contains invalid characters: "%"',
			c1_rec.newdb, 
			c1_rec.invalid_characters;
	ELSIF c1_rec.length_invalid THEN
		RAISE EXCEPTION 'db_create.sql() new database name="%" is too long (30 characters max)', 
			c1_rec.newdb;
	ELSE
		RAISE INFO 'db_create.sql() test new database name="%" is OK', c1_rec.newdb;
	END IF;
	
END;
$$;

\echo Creating :newdb database if required

--
-- Encrypted postgres and rif40 user passwords
--
\set npassword '''XXXX':postgres_password''''
SET rif40.postgres_password TO :npassword;
\set npassword '''XXXX':rif40_password''''
SET rif40.rif40_password TO :npassword;

--
-- Check user is postgres on postgres, check password
--
DO LANGUAGE plpgsql $$
DECLARE	
	c1up CURSOR(l_param VARCHAR) FOR 
		SELECT CURRENT_SETTING(l_param) AS password;
	c2up CURSOR(l_user VARCHAR, l_pass VARCHAR) FOR 
		SELECT rolname AS usename, rolpassword, l_pass AS password,
		       'md5'||md5(l_pass||l_user) AS encrypted_passwd
		  FROM pg_authid  
		 WHERE rolname = l_user;
--
	c1_rec RECORD;
	c2_rec RECORD;
	encrypted_passwd	VARCHAR;
	password	VARCHAR;
	
--
	sql_stmt VARCHAR;
BEGIN
	IF user = 'postgres' AND current_database() = 'postgres' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on postgres database (%)', 
			user, current_database();	
	END IF;
--
-- Check rif40_password pareameter
--
	OPEN c1up('rif40.rif40_password');
	FETCH c1up INTO c1_rec;
	CLOSE c1up;	
	IF UPPER(c1_rec.password) = 'XXXX' THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v rif40_password=<rif40_password password> parameter';	
	ELSE
		OPEN c2up('rif40', SUBSTR(CURRENT_SETTING('rif40.rif40_password'), 5));
		FETCH c2up INTO c2_rec;
		CLOSE c2up;
--		
		RAISE INFO 'db_create.sql() rif40 password="%"', c2_rec.password;
		IF c2_rec.rolpassword IS NULL THEN
			RAISE INFO 'db_create.sql() rif40 needs to be created encrypted password will be ="%"', c2_rec.encrypted_passwd;	
		ELSIF c2_rec.rolpassword = c2_rec.encrypted_passwd THEN
			RAISE INFO 'db_create.sql() rif40 encrypted password="%"', c2_rec.encrypted_passwd;
		ELSE
			RAISE EXCEPTION 'db_create.sql() rif40 password set in install script="%" differs from database: "%"',
				c2_rec.encrypted_passwd, c2_rec.rolpassword;	
		END IF;
		password:=COALESCE(c2_rec.password, SUBSTR(CURRENT_SETTING('rif40.rif40_password'), 5));
		encrypted_passwd:=COALESCE(c2_rec.encrypted_passwd, 'md5'||md5(password||'rif40'));
		sql_stmt:='SET rif40.encrypted_rif40_password TO '''|| encrypted_passwd||'''';
-- 
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='SET rif40.rif40_password TO '''|| password||'''';
-- 
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
			
	END IF;	
--
-- Check postgres_password parameter
--
	OPEN c1up('rif40.postgres_password');
	FETCH c1up INTO c1_rec;
	CLOSE c1up;
	IF UPPER(c1_rec.password) = 'XXXX' THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v postgres_password=<postgres_password password> parameter';	
	ELSE
		OPEN c2up('postgres', SUBSTR(CURRENT_SETTING('rif40.postgres_password'), 5));
		FETCH c2up INTO c2_rec;
		CLOSE c2up;
--		
		RAISE INFO 'db_create.sql() postgres password="%"', c2_rec.password;
		IF c2_rec.rolpassword = c2_rec.encrypted_passwd THEN
			RAISE INFO 'db_create.sql() postgres encrypted password="%"', c2_rec.encrypted_passwd;
			sql_stmt:='SET rif40.encrypted_postgres_password TO '''|| c2_rec.encrypted_passwd||'''';
-- 
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		ELSE
			RAISE EXCEPTION 'db_create.sql() postgres password set in install script="%" differs from database: "%". Unencrypted is %',
				c2_rec.encrypted_passwd, c2_rec.rolpassword, CURRENT_SETTING('rif40.postgres_password');
		END IF;
	END IF;		
--	
END;
$$;

--
-- Check DB version
--
DO LANGUAGE plpgsql $$
DECLARE
-- PostgreSQL 9.3.5 on x86_64-apple-darwin, compiled by i686-apple-darwin11-llvm-gcc-4.2 (GCC) 4.2.1 (Based on Apple Inc. build 5658) (LLVM build 2336.9.00), 64-bito
--
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
	namelist VARCHAR[]:=ARRAY['adminpack', 'postgis', 'sslinfo'];
	x VARCHAR;
	i INTEGER:=0;
BEGIN
	FOREACH x IN ARRAY namelist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		IF c1_rec.name IS NULL THEN
			RAISE INFO 'db_create.sql() Optional extension: % is not installable', x;
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

\echo Create users and roles
--
-- Create users and roles
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_user WHERE usename = l_name;
	c2 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.encrypted_postgres_password') AS postgres_password,
		       CURRENT_SETTING('rif40.encrypted_rif40_password') AS rif40_password;
	c3 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_roles WHERE rolname = l_name;
	c4 CURSOR(l_name VARCHAR) FOR
		SELECT rolpassword FROM pg_authid WHERE rolname = l_name;
	c5 CURSOR(l_user VARCHAR, l_pass VARCHAR) FOR
		SELECT 'md5'||md5(l_pass||l_user) AS new_passwd, passwd FROM pg_shadow
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
	IF c4_rec.rolpassword IS NULL THEN
		RAISE EXCEPTION 'db_create.sql() postgres user has no password';
	ELSIF c4_rec.rolpassword = c2_rec.postgres_password THEN
		RAISE NOTICE 'db_create.sql() postgres user password is unchanged';
	ELSE
		RAISE EXCEPTION 'db_create.sql() postgres user password would change; set postgres_password correctly';
--		RAISE NOTICE 'db_create.sql() changing postgres user password';
--		sql_stmt:='ALTER USER postgres ENCRYPTED PASSWORD  '''||c2_rec.postgres_password||'''';
--		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
-- Now fixed
--		EXECUTE sql_stmt;
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
			IF x = 'rif40' THEN
				OPEN c5(x, SUBSTR(CURRENT_SETTING('rif40.rif40_password'), 5));
			ELSE
				OPEN c5(x, x);
			END IF;
			FETCH c5 INTO c5_rec;
			CLOSE c5;			
			IF c4_rec.rolpassword IS NULL THEN
				RAISE INFO 'db_create.sql() RIF schema user % exists, no password', c1_rec.usename::VARCHAR;
			ELSIF c1_rec.usename = 'rif40' THEN
				RAISE INFO 'db_create.sql() RIF schema user % exists; changing password to encrypted', c1_rec.usename::VARCHAR;
				sql_stmt:='ALTER USER '||c1_rec.usename||' ENCRYPTED PASSWORD  '''||
					c2_rec.rif40_password||'''';
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
				c2_rec.rif40_password||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
--
-- Test user: not_a_rif_user - should have no access at all
--
		ELSIF x = 'notarifuser' THEN
	    	sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||
				md5(CURRENT_TIMESTAMP::Text)||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
--
-- Data owners, non health data (so wider access can be granted if required)
--
		ELSIF x IN ('gis', 'pop') THEN
	    	sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||
				md5(CURRENT_TIMESTAMP::Text)||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
--
-- Test users for roles
--
		ELSIF x IN ('test_rif_no_suppression', 'test_rif_user', 'test_rif_manager', 'test_rif_student') THEN
	    	sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||
				md5(CURRENT_TIMESTAMP::Text)||'''';
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
SET rif40.ntestuser TO :ntestuser;
\set quotedtestuser '\"':testuser'\"'
SET rif40.testuser TO :quotedtestuser;
\set nnewpw '''XXXX':newpw''''
SET rif40.nnewpw TO :nnewpw;
SET rif40.newpw TO :newpw;

DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR(l_name VARCHAR) FOR 
		SELECT CURRENT_SETTING('rif40.ntestuser') AS ntestuser, 
		       l_name AS testuser,
			   REGEXP_REPLACE(l_name, 
					'[[:lower:]]{1,}[[:lower:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g') AS invalid_characters,
		       CASE
					WHEN LENGTH(REGEXP_REPLACE(l_name, 
						'[[:lower:]]{1,}[[:lower:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g')) > 0 THEN TRUE
					ELSE FALSE 
			   END::BOOLEAN AS is_invalid,
		       CASE
					WHEN LENGTH(l_name) > 30 THEN TRUE
					ELSE FALSE 
			   END::BOOLEAN AS length_invalid;
	c2 CURSOR(l_usename VARCHAR) FOR 
		SELECT * FROM pg_user WHERE usename = l_usename;
	c3 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.nnewpw') AS nnewpw,
		       CURRENT_SETTING('rif40.newpw') AS newpw;
	c4 CURSOR(l_name VARCHAR, l_pass VARCHAR) FOR
		SELECT rolpassword::Text AS rolpassword, 
		       'md5'||md5(l_pass||l_name)::Text AS password
	  	  FROM pg_authid
	     WHERE rolname = l_name;
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
--
	sql_stmt VARCHAR;
	u_name	VARCHAR;
	u_pass	VARCHAR;
BEGIN
	OPEN c1(CURRENT_SETTING('rif40.testuser'));
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
-- Test parameter
--
	IF c1_rec.ntestuser IN ('XXXX', 'XXXX:testuser') THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v testuser=<test user account> parameter';	
	ELSIF c1_rec.is_invalid THEN
		RAISE EXCEPTION 'db_create.sql() test user account name="%" contains invalid characters: "%"',
			c1_rec.testuser, 
			c1_rec.invalid_characters;
	ELSIF c1_rec.length_invalid THEN
		RAISE EXCEPTION 'db_create.sql() test user account name="%" is too long (30 characters max)', 
			c1_rec.testuser;		
	ELSE
		RAISE INFO 'db_create.sql() test user account parameter="%/%" OK', 
			c1_rec.testuser, CURRENT_SETTING('rif40.testuser');
	END IF;	
	OPEN c3;
	FETCH c3 INTO c3_rec;
	CLOSE c3;
	IF c3_rec.newpw IN ('XXXX', 'XXXX:newpw') THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v newpw=<test user password> parameter';	
	ELSE
		RAISE INFO 'db_create.sql() test user password parameter="%"', c3_rec.newpw;
	END IF;	
	
--
-- Check is valid DB name
--
	u_name:=CURRENT_SETTING('rif40.testuser');
	u_pass:=CURRENT_SETTING('rif40.newpw');
--
-- Test account exists
--
	OPEN c2(u_name);
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.usename IS NULL THEN
		RAISE NOTICE 'db_create.sql() C209xx: User account does not exist: %; creating', u_name;	
		sql_stmt:='CREATE ROLE '||u_name||
			' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||
				CURRENT_SETTING('rif40.newpw')||'''';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	ELSE
--	
		OPEN c4(u_name, u_pass);
		FETCH c4 INTO c4_rec;
		CLOSE c4;
		IF c4_rec.rolpassword IS NULL THEN
			RAISE EXCEPTION 'db_create.sql() C209xx: User account: % has a NULL password', 
				c2_rec.usename;	
		ELSIF c4_rec.rolpassword != c4_rec.password THEN
			RAISE INFO 'rolpassword: "%"', c4_rec.rolpassword;
			RAISE INFO 'password(%):    "%"', u_pass, c4_rec.password;
			RAISE EXCEPTION 'db_create.sql() C209xx: User account: % password (%) would change; set password correctly', c2_rec.usename, u_pass;		
		ELSE
			RAISE NOTICE 'db_create.sql() C209xx: User account: % password is unchanged', 
				c2_rec.usename;
		END IF;
--
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

SET rif40.postgres_password TO :postgres_password;
CREATE OR REPLACE VIEW pgpass_tmp
AS
SELECT 'localhost:5432:*:'||rolname||':'||
			CASE WHEN rolname = 'rif40' 							THEN CURRENT_SETTING('rif40.rif40_password')
			     WHEN rolname = 'postgres'							THEN CURRENT_SETTING('rif40.postgres_password')
			     WHEN rolname = CURRENT_SETTING('rif40.testuser') 	THEN CURRENT_SETTING('rif40.newpw')
			END AS pgpassline
  FROM pg_authid
 WHERE rolname IN ('rif40', 'postgres')
     OR rolname = CURRENT_SETTING('rif40.testuser');

\set ECHO none
--
\pset tuples_only on
--
\echo '*****************************************************************************************************'
\echo '* Try to connect as test user. This will fail if pgpass is not setup correctly'
\echo '* Stored password file is in ~/.pgpass on Linux/Macos or: %APPDATA%/postgresql/pgpass.conf on Windows'
\echo '* Format is: hostname:port:database:username:password'
\echo '* An example is supplied in pgpass.conf in the current directory'
\echo '*****************************************************************************************************'

SELECT * FROM pgpass_tmp;
\pset tuples_only off
\copy (SELECT pgpassline FROM pgpass_tmp) TO pgpass.conf WITH (FORMAT text)	 
DROP VIEW IF EXISTS pgpass_tmp;

--
\c postgresql://:testuser::newpw@:pghost/postgres
\echo Connection as :testuser to postgres OK!
SELECT 'Connected to: '||current_database()||' AS '||USER AS connected;
\echo Try to connect as rif40. This will fail if if the password file is not setup correctly
\c postgresql://rif40::rif40_password@:pghost/postgres
\echo Connection as rif40 to postgres OK!
SELECT 'Connected to: '||current_database()||' AS '||USER AS connected;
\c postgres postgres :pghost

\echo ************************************************************************************
\echo *                                                                                  
\echo * WARNING !!!                                                                      
\echo *                                                                                  
\echo * This script will drop :newdb, re-create it in (:tablespace_dir) or DEFAULT                                                                     
\echo *                                                                                  
\echo ************************************************************************************

--
-- Location of :newdb tablespace
--
\set ntablespace_dir '''XXXX':tablespace_dir''''
SET rif40.tablespace_dir TO :ntablespace_dir;
SET rif40.os TO :os;
SET rif40.newdb TO :newdb;

DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR SELECT CURRENT_SETTING('rif40.tablespace_dir') AS tablespace_dir;
	c1_rec RECORD;
--
	sql_stmt VARCHAR;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF UPPER(c1_rec.tablespace_dir) = 'XXXX' THEN
		RAISE NOTICE 'db_create.sql() No -v tablespace_dir=<tablespace_dir> parameter';	
		sql_stmt:='SET rif40.tablespace_dir TO ''''';
-- 
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
--
-- Escape tablespace_dir appropriately for os
--
	ELSIF CURRENT_SETTING('rif40.os') = 'windows_nt' THEN
		RAISE INFO 'db_create.sql() Windows tablespace_dir="%"', 
				SUBSTR(c1_rec.tablespace_dir, 5);
		sql_stmt:='SET rif40.tablespace_dir TO ''"'||
			REPLACE(SUBSTR(c1_rec.tablespace_dir, 5), '\', '/')||
			'"''';
-- '
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	ELSIF CURRENT_SETTING('rif40.os') IN ('linux', 'darwin') THEN
		RAISE INFO 'db_create.sql() Windows postgres tablespace_dir="%"', 
				SUBSTR(c1_rec.tablespace_dir, 5);
		sql_stmt:='SET rif40.tablespace_dir TO '''||
			REPLACE(SUBSTR(c1_rec.tablespace_dir, 5), ' ', '\ ')||
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
-- Re-create :newdb
--

WITH a AS (
	SELECT unnest(ARRAY['rif40.tablespace_dir', 'rif40.newdb',
			'rif40.os']) AS param
	)
SELECT param,
       CURRENT_SETTING(a.param) AS value
	     FROM a;
\set tablespace_dir TO rif40.tablespace_dir;
 
--
-- Conditionally create a script to recreate :newdb
-- Database creation is not transactional!
--
-- Create SQL
--
CREATE OR REPLACE VIEW recreate_sahsu_dev_tmp
AS
WITH ts AS (
	SELECT CASE
	           WHEN CURRENT_SETTING('rif40.tablespace_dir')::Text != '' 
						THEN  'TABLESPACE '||CURRENT_SETTING('rif40.newdb')
		       ELSE '/* No '||CURRENT_SETTING('rif40.newdb')||' tablespace */' 
		   END AS newdb_tablespace
), a AS (
	SELECT 'CREATE TABLESPACE '||CURRENT_SETTING('rif40.newdb')||' LOCATION '''||
		CURRENT_SETTING('rif40.tablespace_dir')||''';' AS sql_stmt, 
       10 AS ord 
	 WHERE CURRENT_SETTING('rif40.tablespace_dir')::Text != ''
	   AND ''||CURRENT_SETTING('rif40.newdb')||'' NOT IN (
			SELECT spcname FROM pg_tablespace WHERE spcname = ''||CURRENT_SETTING('rif40.newdb')||'')
), c AS (
SELECT 'DROP DATABASE IF EXISTS '||CURRENT_SETTING('rif40.newdb')||';' AS sql_stmt,
       50 AS ord
UNION 
SELECT 'CREATE DATABASE '||CURRENT_SETTING('rif40.newdb')||
			' WITH OWNER rif40 '||ts.newdb_tablespace||';' AS sql_stmt,
       60 AS ord
  FROM ts
UNION 
SELECT 'COMMENT ON DATABASE '||CURRENT_SETTING('rif40.newdb')||
			' IS ''RIF V4.0 PostGres '||CURRENT_SETTING('rif40.newdb')||
			' Example Database'';' AS sql_stmt,
       70 AS ord
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
SELECT * FROM c
UNION
SELECT * FROM d;

SELECT sql_stmt FROM recreate_sahsu_dev_tmp
 ORDER BY ord;
 
--
-- Extract SQL to run
--
\copy (SELECT sql_stmt FROM recreate_sahsu_dev_tmp ORDER BY ord) TO recreate_sahsu_dev_tmp.sql WITH (FORMAT text)

DROP VIEW IF EXISTS recreate_sahsu_dev_tmp;

--
-- Execute
--
\set ECHO all
\set ON_ERROR_STOP on
\i recreate_sahsu_dev_tmp.sql

\c :newdb postgres :pghost 

--
-- Start transaction 2: :newdb build
--
BEGIN;
--
-- Check user is postgres on :newdb
--
\set ECHO none
SET rif40.testuser TO :ntestuser;
SET rif40.newdb TO :newdb;

DO LANGUAGE plpgsql $$
DECLARE	
	sql_stmt VARCHAR;
	u_name	VARCHAR;
BEGIN
	u_name:=SUBSTR(CURRENT_SETTING('rif40.testuser'), 5);
	IF user = 'postgres' AND current_database() = CURRENT_SETTING('rif40.newdb') THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on :newdb database (%)', 
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

--
-- RIF40 grants
--	
	sql_stmt:='GRANT ALL ON DATABASE '||CURRENT_SETTING('rif40.newdb')||' to rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='REVOKE CREATE ON SCHEMA public FROM rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
	sql_stmt:='GRANT CONNECT ON DATABASE '||CURRENT_SETTING('rif40.newdb')||' to '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;

	sql_stmt:='CREATE SCHEMA IF NOT EXISTS '||u_name||' AUTHORIZATION '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;	
--
-- Set default search pathname
--
	sql_stmt:='ALTER DATABASE '||CURRENT_SETTING('rif40.newdb')||' SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
\set echo ALL
--
-- End transaction 2: newdb build
--
END;

\set echo ALL

--
-- Set search path for rif40
--
ALTER USER rif40 SET search_path 
	TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
SET search_path 
	TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
	
SHOW search_path;

--
\echo *****************************************************************************************************
\echo * Try to connect as test user and rif40
\echo *****************************************************************************************************
--
\c postgresql://:testuser::newpw@:pghost/:newdb
\echo Connection as :testuser to :newdb OK!
SELECT 'Connected to: '||current_database()||' AS '||USER AS connected;
\echo Try to connect as rif40. This will fail if if the password file is not setup correctly
\c postgresql://rif40::rif40_password@:pghost/:newdb
\echo Connection as rif40 to :newdb OK!
SELECT 'Connected to: '||current_database()||' AS '||USER AS connected;

/*
DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'Stop.';
END;
$$;
 */
--
-- Eof
