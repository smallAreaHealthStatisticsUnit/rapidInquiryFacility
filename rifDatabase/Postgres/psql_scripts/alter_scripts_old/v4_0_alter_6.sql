-- 
-- 
--  THIS IS A SCHEMA ALTER SCRIPT - IT CAN BE RE-RUN BUT THEY MUST BE RUN 
--  IN NUMERIC ORDER
-- 
-- 
--
-- 
--
-- GIT Header
--
-- $FormatGit ID (%h) %ci$
-- $Id$
-- Version hash $Format%H$
--
-- Description
--
-- Rapid Enquiry Facility (RIF) - RIF alter script 6 - PL/R support
--
-- Copyright
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
-- RIF is free software you can redistribute it andor modify
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
-- along with RIF. If not, see http://www.gnu.org/licenses; or write 
-- to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
-- Boston, MA 02110-1301 USA
--
-- Author
--
-- Peter Hambly, SAHSU
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #6 PL/R support.

DO LANGUAGE plpgsql $$
DECLARE	
	c1_r1 CURSOR FOR 
		SELECT *
	      FROM pg_extension
		 WHERE extname = 'plr';
	c1_rec RECORD;

BEGIN
--
	PERFORM rif40_log_pkg.rif40_log_setup();
    PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
	OPEN c1_r1;
	FETCH c1_r1 INTO c1_rec;
	CLOSE c1_r1;
	IF c1_rec.extname IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'v4_0_alter_6', 'PL/R extension version % loaded', 
			c1_rec.extversion::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-99903, 'v4_0_alter_6', 'PL/R extension not enabled');	
	END IF;
END;
$$;
			
--
-- Connect as postgres and install privileged PL/R packages
--
\c sahsuland_dev postgres :pghost
\i ../PLpgsql/v4_0_rif40_R_pkg.sql

\c sahsuland_dev rif40 :pghost 

\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
--
-- Functions to enable debug for
--
	rif40_r_pkg_functions 	VARCHAR[] := ARRAY[
		'installed_packages', 'r_cleanup'];
	l_function 					VARCHAR;
BEGIN
--
	PERFORM rif40_log_pkg.rif40_log_setup();
    PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_r_pkg_functions functions
--
	FOREACH l_function IN ARRAY rif40_r_pkg_functions LOOP
		RAISE INFO 'Enable debug for function: %', l_function;
		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
	END LOOP;
--
	PERFORM rif40_r_pkg.r_init();
	PERFORM rif40_sql_pkg.rif40_method4('SELECT * FROM rif40_r_pkg.installed_packages()', 'Installed R packages');
	PERFORM rif40_r_pkg.r_cleanup();
	
--
	RAISE INFO 'Aborting (script being tested)';
	RAISE EXCEPTION 'C20999: Abort';
END;
$$;

--
-- Eof
