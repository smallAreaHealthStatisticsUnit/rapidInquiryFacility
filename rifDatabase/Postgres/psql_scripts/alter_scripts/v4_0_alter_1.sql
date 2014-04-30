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
-- Rapid Enquiry Facility (RIF) - Create SAHSULAND rif40 exmaple schema
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

\echo Running SAHSULAND schema alter script #1 (Hash and range partitioning, automatic clustering of denominator tables)...

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
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_range_partition(VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_range_partition(VARCHAR,VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_range_partition_create(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS  rif40_sql_pkg._rif40_common_partition_create(VARCHAR, VARCHAR, VARCHAR, VARCHAR);

--
-- PG psql code (SQL and Oracle compatibility processing)
--
\i ../PLpgsql/v4_0_rif40_sql_pkg.sql

--
-- Hash partition all tables with study_id as a column
-- This will cope with data already present in the table
--
--\i ../psql_scripts/v4_0_study_id_partitions.sql

--
-- Range partition all tables with year as a column
-- This will cope with data already present in the table
--
\i ../psql_scripts/v4_0_year_partitions.sql

DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'C20999: Abort';
END;
$$;

END;

--
-- Eof
