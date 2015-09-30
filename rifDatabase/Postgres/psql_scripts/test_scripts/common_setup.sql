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
-- Rapid Enquiry Facility (RIF) - Test suite - comnmon setup for scripts run as testuser
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
-- the Free Software Foundation, either verquote_ident(l_schema)||'.'||quote_ident(l_tablesion 3 of the License, or
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
\set ECHO :echo
\set ON_ERROR_STOP on
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
-- Test user account
-- 
\set ntestuser '''XXXX':testuser''''
\set npgdatabase '''XXXX':pgdatabase''''
SET rif40.testuser TO :ntestuser;
SET rif40.pgdatabase TO :npgdatabase;

DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.testuser') AS testuser;
	c2 CURSOR(l_usename VARCHAR) FOR 
		SELECT * FROM pg_user WHERE usename = l_usename;
	c3 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.pgdatabase') AS pgdatabase;		
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	OPEN c3;
	FETCH c3 INTO c3_rec;
	CLOSE c3;	
--
-- Test parameters
--
	IF c1_rec.testuser IN ('XXXX', 'XXXX:testuser') THEN
		RAISE EXCEPTION 'common_setup.sql() C209xx: No -v testuser=<test user account> parameter';	
	ELSE
		RAISE INFO 'common_setup.sql() test user account parameter="%"', c1_rec.testuser;
	END IF;
	IF c3_rec.pgdatabase IN ('XXXX', 'XXXX:pgdatabase') THEN
		RAISE EXCEPTION 'common_setup.sql() C209xx: No -v pgdatabase=<PG database: sahsuland/sahsuland_dev> parameter';	
	ELSIF SUBSTR(c3_rec.pgdatabase, 5) NOT IN ('sahsuland', 'sahsuland_dev') THEN
		RAISE EXCEPTION 'common_setup.sql() C209xx: Invalid pgdatabas parameter: %; must be sahsuland/sahsuland_dev', SUBSTR(c3_rec.pgdatabase, 5);	
	ELSIF SUBSTR(c3_rec.pgdatabase, 5) != current_database() THEN
		RAISE EXCEPTION 'common_setup.sql() C209xx: pgdatabas parameter: % != current: %',SUBSTR(c3_rec.pgdatabase, 5), current_database();	
		
	ELSE
		RAISE INFO 'common_setup.sql() PG database parameter="%"', SUBSTR(c3_rec.pgdatabase, 5);
	END IF;	
--
-- Test account exists
--
	OPEN c2(LOWER(SUBSTR(c1_rec.testuser, 5)));
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.usename IS NULL THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: User account does not exist: %', LOWER(SUBSTR(c1_rec.testuser, 5));	
	ELSIF pg_has_role(c2_rec.usename, 'rif_user', 'MEMBER') THEN
		RAISE INFO 'db_create.sql() user account="%" is a rif_user', c2_rec.usename;
	ELSIF pg_has_role(c2_rec.usename, 'rif_manager', 'MEMBER') THEN
		RAISE INFO 'db_create.sql() user account="%" is a rif manager', c2_rec.usename;
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User account: % is not a rif_user or rif_manager', c2_rec.usename;	
	END IF;
--
END;
$$;

--
-- Connect as testuser
--
\c :pgdatabase :testuser
\conninfo
--
-- Run RIF startup script
--
\set ECHO :echo
\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
 	c1 CURSOR FOR
		SELECT p.proname
		  FROM pg_proc p, pg_namespace n
		 WHERE p.proname  = 'rif40_startup'
		   AND n.nspname  = 'rif40_sql_pkg'
		   AND p.proowner = (SELECT oid FROM pg_roles WHERE rolname = 'rif40')
		   AND n.oid      = p.pronamespace;
--
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.proname = 'rif40_startup' THEN
		PERFORM rif40_sql_pkg.rif40_startup();
	ELSE
		RAISE INFO 'RIF startup: not a RIF database';
	END IF;
END;
$$;

--
-- Check user is NOT rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user != 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20902: User check failed: % is rif40', user;	
	END IF;
END;
$$;

--
-- Use make test for more debug
--
\set VERBOSITY :verbosity
\pset pager off

--
-- Eof