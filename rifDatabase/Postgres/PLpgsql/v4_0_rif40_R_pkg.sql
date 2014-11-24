-- ************************************************************************
-- *
-- * THIS SCRIPT MAY BE EDITED - NO NEED TO USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - Create PG psql code (Optional R support)
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
--\set VERBOSITY terse

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

\echo Creating PG psql code (Optional R support)...

DO LANGUAGE plpgsql $$
DECLARE	
	c1_r CURSOR FOR 
		SELECT *
	      FROM pg_extension
		 WHERE extname = 'plr';
	c1_rec RECORD;
BEGIN
	OPEN c1_r;
	FETCH c1_r INTO c1_rec;
	CLOSE c1_r;
	IF c1_rec.extname IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'v4_0_rif40_R_pkg', 'PL/R extension version % loaded', 
			c1_rec.extversion::VARCHAR);
			
CREATE OR REPLACE FUNCTION rif40_r_pkg.install_all_packages(use_internet BOOLEAN DEFAULT TRUE)
RETURNS void
AS $func$
BEGIN
	IF use_internet THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'install_all_packages', 'Load packages from internet');
		PERFORM public.install_rcmd('source("C:/Users/Peter/Documents/GitHub/rapidInquiryFacility/rifDatabase/Postgres/PLpgsql/rif40_R_pkg/_install_all_packages_from_internet.R")');
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-30999, 'install_all_packages', 'Load packages from files - NOT YET IMPLEMENTED');
	END IF;
END;
$func$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS rif40_r_pkg._install_all_packages_from_internet();

--
-- Grants
--
GRANT EXECUTE ON FUNCTION rif40_r_pkg.install_all_packages(boolean) TO rif_manager;

--
-- Install all required packages
-- 
		PERFORM rif40_r_pkg.install_all_packages();
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'v4_0_rif40_R_pkg', 'Optional PL/R extension not loaded');
	END IF;
END;
$$;

\echo Created PG psql code (Optional R support).

--
-- Eof