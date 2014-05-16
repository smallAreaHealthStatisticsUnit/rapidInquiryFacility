-- ************************************************************************
-- *
-- * DO NOT EDIT THIS SCRIPT OR MODIFY THE RIF SCHEMA - USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - Partition all tables with study_id as a column
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
-- Check database is sahsuland_dev
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'Database check: %', current_database();	
	ELSE
		RAISE EXCEPTION 'C20901: Database check failed: % is not sahsuland_dev', current_database();	
	END IF;
END;
$$;

\echo Partition all tables with study_id as a column...

\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
--
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_hash_partition',
							'_rif40_hash_partition_create',
							'_rif40_common_partition_create',
							'_rif40_common_partition_create_setup',
							'_rif40_hash_partition_create_insert',
							'_rif40_common_partition_create_complete',
							'_rif40_common_partition_triggers',
							'_rif40_method4',
							'rif40_ddl'];
	l_function 			VARCHAR;
--
	c1 CURSOR FOR
		SELECT a.tablename AS tablename, b.attname AS columnname, schemaname AS schemaname	/* Tables */
		  FROM pg_tables a, pg_attribute b, pg_class c
		 WHERE c.oid        = b.attrelid
		   AND c.relname    = a.tablename
		   AND c.relkind    = 'r' /* Relational table */
		   AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		   AND b.attname    = 'study_id'
		   AND a.schemaname = 'rif40'
		 ORDER BY 1;
--
	c1_rec RECORD;
--
	sql_stmt VARCHAR[];
BEGIN
--
-- Check user is rif40
--
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C209xx: User check failed: % is not rif40', user;	
	END IF;
--
-- Turn on some debug
--
        PERFORM rif40_log_pkg.rif40_log_setup();
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
	FOREACH l_function IN ARRAY rif40_sql_pkg_functions LOOP
		RAISE INFO 'Enable debug for function: %', l_function;
		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
	END LOOP;
--
	FOR c1_rec IN c1 LOOP
		RAISE INFO 'Hash partitioning: %.%', c1_rec.schemaname, c1_rec.tablename;
		PERFORM rif40_sql_pkg.rif40_hash_partition(c1_rec.schemaname::VARCHAR, c1_rec.tablename::VARCHAR, 'study_id'::VARCHAR);
	END LOOP;
END;
$$;

\echo Partitioning of all tables with study_id as a column complete.
--
-- Eof

