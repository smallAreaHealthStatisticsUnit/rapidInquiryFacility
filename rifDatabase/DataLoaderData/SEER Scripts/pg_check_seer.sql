-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Checks the SEER data in the RIF
--								  * Requires the user has "seer_user" role
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
-- Postgres RIF40 specific parameters
--
-- Usage: psql -w -e -f check_seer.sql
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

--
-- Setup RIF user
--
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
		RAISE EXCEPTION 'RIF startup(SEER checker): not a RIF database';
	END IF;
--
-- Set a default path and schema for user
--
	IF current_user = 'rif40' THEN
		RAISE EXCEPTION 'RIF startup(SEER checker): do NOT run as RIF user: %', current_user;
	END IF;
END;
$$;

--
-- Check user has seer_user role
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT rolname
		  FROM pg_roles
		 WHERE pg_has_role(current_user, oid, 'member')
		   AND rolname = 'seer_user';
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.rolname = 'seer_user' THEN
		RAISE INFO 'User % has SEER_USER role', current_user;
	ELSE
		RAISE EXCEPTION 'User % does NOT have SEER_USER role', current_user;
	END IF;
END;
$$;

--
-- Check user can access SEER_CANCER
--

SELECT * FROM rif40_num_denom_errors
 WHERE numerator_table = 'SEER_CANCER'
   AND geography = 'USA_2014';
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT * FROM rif40_num_denom
		 WHERE numerator_table = 'SEER_CANCER';
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.numerator_table = 'SEER_CANCER' THEN
		RAISE INFO 'User % can access SEER_CANCER table', current_user;
	ELSE
		RAISE EXCEPTION 'User % does NOT have access to SEER_CANCER table', current_user;
	END IF;
END;
$$;

--
-- Eof