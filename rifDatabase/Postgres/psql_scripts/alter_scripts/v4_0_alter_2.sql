-- ************************************************************************
-- *
-- * THIS IS A SCHEMA ALTER SCRIPT - IT CAN BE RE-RUN BUT THEY MUST BE RUN 
-- * IN NUMERIC ORDER
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
-- Rapid Enquiry Facility (RIF) - RIF alter script 2:
--
-- Add covariates to comparison area extract;
-- GID, GID_ROWINDEX support in extracts/maps; 
-- Make INV_1 INV_<inv_id> in results and results maps
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
-- To run:
--
-- Sync github to current
-- cd to: <Github repository>\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
-- e.g. P:\Github\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
-- Run script alter_scripts\v4_0_alter_2.sql using psql on sahusland_dev as rif40 (schema owner)
-- The relative path is important!
-- P:\Github\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>psql -U rif40 -d sahsuland_dev -w -e -v testuser=<test user account> -f alter_scripts\v4_0_alter_2.sql
-- Beware: script discards all changes whilst under development!:
/* At end of script>>>
--
-- Disable script by discarding all changes
--
DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'Aborting (script being tested)';
	RAISE EXCEPTION 'C20999: Abort';
END;
$$;
 */
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #2 (Add covariates to comparision area extract; GID, GID_ROWINDEX support in maps (extracts subject to performance tests); Make INV_1 INV_<inv_id> in results maps)...

BEGIN;

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
SET rif40.testuser TO :ntestuser;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.testuser') AS testuser;
	c2 CURSOR(l_usename VARCHAR) FOR 
		SELECT * FROM pg_user WHERE usename = l_usename;
	c1_rec RECORD;
	c2_rec RECORD;
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
-- Drop statements if already run
--

--
-- Run common code for state machine, meddileware and
-- PG psql code (SQL and Oracle compatibility processing)
--
\i ../PLpgsql/v4_0_rif40_sql_pkg.sql
\i ../PLpgsql/v4_0_rif40_sm_pkg.sql
\i ../PLpgsql/v4_0_rif40_xml_pkg.sql

DO LANGUAGE plpgsql $$
BEGIN
	RAISE INFO 'alter_2.sql completed OK';
--
--	RAISE INFO 'Aborting (script being tested)';
--	RAISE EXCEPTION 'C20999: Abort';
END;
$$;

END;

--
-- Eof
