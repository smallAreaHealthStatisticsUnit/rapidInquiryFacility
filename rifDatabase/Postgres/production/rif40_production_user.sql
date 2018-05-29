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
-- Rapid Enquiry Facility (RIF) - Postgres production user creation script
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
-- This script requires the following variables to run:
--
--	-v newuser=%NEWUSER% -v %NEWDB% -v newpw=%NEWPW% 
	
--
-- Start transaction 1: create account
--
BEGIN;

--
-- check connected as postgres to postgres
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_user != 'postgres' OR current_database() != 'postgres' THEN
		RAISE EXCEPTION 'rif40_production_user.sql() current_user: % and current database: % must both be postgres', current_user, current_database();
	END IF;
END;
$$;

--
-- Check command line parameters
--

--
-- Test user account
-- 
\set nnewuser '''XXXX':newuser''''
SET rif40.nnewuser TO :nnewuser;
\set quotednewuser '\"':newuser'\"'
SET rif40.newuser TO :quotednewuser;
\set nnewpw '''XXXX':newpw''''
SET rif40.nnewpw TO :nnewpw;
SET rif40.newpw TO :newpw;
\set nnewdb '''XXXX':newdb''''
SET rif40.nnewdb TO :nnewdb;
SET rif40.newdb TO :newdb;

DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR(l_name VARCHAR) FOR 
		SELECT CURRENT_SETTING('rif40.nnewuser') AS nnewuser, 
		       l_name AS newuser,
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
	u_database VARCHAR;
BEGIN
	OPEN c1(CURRENT_SETTING('rif40.newuser'));
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
-- Test parameter
--
	IF c1_rec.nnewuser IN ('XXXX', 'XXXX:newuser') THEN
		RAISE EXCEPTION 'rif40_production_user.sql() C209xx: No -v newuser=<test user account> parameter';	
	ELSIF c1_rec.is_invalid THEN
		RAISE EXCEPTION 'rif40_production_user.sql() test user account name="%" contains invalid characters: "%"',
			c1_rec.newuser, 
			c1_rec.invalid_characters;
	ELSIF c1_rec.length_invalid THEN
		RAISE EXCEPTION 'rif40_production_user.sql() test user account name="%" is too long (30 characters max)', 
			c1_rec.newuser;		
	ELSE
		RAISE INFO 'rif40_production_user.sql() test user account parameter="%/%" OK', 
			c1_rec.newuser, CURRENT_SETTING('rif40.newuser');
	END IF;	
	OPEN c3;
	FETCH c3 INTO c3_rec;
	CLOSE c3;
	IF c3_rec.newpw IN ('XXXX', 'XXXX:newpw') THEN
		RAISE EXCEPTION 'rif40_production_user.sql() C209xx: No -v newpw=<test user password> parameter';	
	ELSE
		RAISE INFO 'rif40_production_user.sql() test user password parameter="%"', c3_rec.newpw;
	END IF;	
	
--
-- Check is valid DB name
--
	u_name:=CURRENT_SETTING('rif40.newuser');
	u_pass:=CURRENT_SETTING('rif40.newpw');
	u_database:=CURRENT_SETTING('rif40.newdb');
--
-- Test account exists
--
	OPEN c2(u_name);
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.usename IS NULL THEN
		RAISE NOTICE 'rif40_production_user.sql() C209xx: User account does not exist: %; creating', u_name;	
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
			RAISE EXCEPTION 'rif40_production_user.sql() C209xx: User account: % has a NULL password', 
				c2_rec.usename;	
		ELSIF c4_rec.rolpassword != c4_rec.password THEN
			RAISE INFO 'rolpassword: "%"', c4_rec.rolpassword;
			RAISE INFO 'password(%):    "%"', u_pass, c4_rec.password;
			RAISE EXCEPTION 'rif40_production_user.sql() C209xx: User account: % password (%) would change; set password correctly', c2_rec.usename, u_pass;		
		ELSE
			RAISE NOTICE 'rif40_production_user.sql() C209xx: User account: % password is unchanged', 
				c2_rec.usename;
		END IF;
--
		IF pg_has_role(c2_rec.usename, 'rif_user', 'MEMBER') THEN
			RAISE INFO 'rif40_production_user.sql() user account="%" is a rif_user', c2_rec.usename;
		ELSIF pg_has_role(c2_rec.usename, 'rif_manager', 'MEMBER') THEN
			RAISE INFO 'rif40_production_user.sql() user account="%" is a rif manager', c2_rec.usename;
		ELSE
			RAISE EXCEPTION 'rif40_production_user.sql() C209xx: User account: % is not a rif_user or rif_manager', c2_rec.usename;	
		END IF;
	END IF;	
--
	sql_stmt:='GRANT CONNECT ON DATABASE '||u_database||' to '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='GRANT rif_manager TO '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='GRANT rif_user TO '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;

END;

\c :newdb postgres

--
-- Check user is postgres on :newdb
--
\set ECHO none
SET rif40.newuser TO :nnewuser;
SET rif40.newdb TO :newdb;
	
--
-- Start transaction 2: create schema
--
BEGIN;

DO LANGUAGE plpgsql $$
DECLARE	
	sql_stmt VARCHAR;
	u_name	VARCHAR;
	u_database	VARCHAR;
BEGIN
	u_name:=SUBSTR(CURRENT_SETTING('rif40.newuser'), 5);
	u_database:=CURRENT_SETTING('rif40.newdb');
	IF user = 'postgres' AND current_database() = CURRENT_SETTING('rif40.newdb') THEN
		RAISE INFO 'rif40_production_user.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'rif40_production_user.sql() C209xx: User check failed: % is not postgres on % database (%)', 
			user, u_database, current_database();	
	END IF;	

--
	sql_stmt:='GRANT CONNECT ON DATABASE '||CURRENT_SETTING('rif40.newdb')||' to '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;

	sql_stmt:='CREATE SCHEMA IF NOT EXISTS '||u_name||' AUTHORIZATION '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;	
END;
$$;

END;

\q

--
-- Eof