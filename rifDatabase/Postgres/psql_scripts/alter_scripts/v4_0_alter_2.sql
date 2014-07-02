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
-- Rapid Enquiry Facility (RIF) - RIF alter script 3:
--
-- Add covariates to comparision area extract;
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
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #2 (Add covariates to comparision area extract; GID, GID_ROWINDEX support in extracts/maps; Make INV_1 INV_<inv_id> in results and results maps)...

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
-- Drop statements if already run
--

--
-- Run common code for state machine
-- PG psql code (SQL and Oracle compatibility processing)
--
\i ../PLpgsql/v4_0_rif40_sql_pkg.sql
\i ../PLpgsql/v4_0_rif40_sm_pkg.sql

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
-- Will need to quit here when in production
--
--\q
\c sahsuland_dev pch
--
--
-- Test in a single transaction
--
\i ../psql_scripts/v4_0_sahsuland_examples.sql   

DO LANGUAGE plpgsql $$
BEGIN
--
	RAISE INFO 'alter_2.sql tested OK';
END;
$$;

\c sahsuland_dev rif40
--
-- Eof
