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
-- Rapid Enquiry Facility (RIF) - Test 2: DDL checks
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
-- Check all tables and views are built
--
SELECT table_or_view, LOWER(table_or_view_name_hide) AS table_or_view_name
  FROM rif40_tables_and_views
 WHERE table_or_view = 'TABLE'
EXCEPT
SELECT 'TABLE' AS table_or_view, table_name AS table_or_view_name
  FROM information_schema.tables
UNION
SELECT table_or_view, LOWER(table_or_view_name_hide) AS table_or_view_name
  FROM rif40_tables_and_views
 WHERE table_or_view = 'VIEW'
EXCEPT
SELECT 'TABLE' AS table_or_view, table_name AS table_or_view_name
  FROM information_schema.views
 ORDER BY 1, 2;
--
-- Check all tables, triggers, columns and comments are present, objects granted to rif_user/rif_manmger, sequences granted
-- 
\i ../psql_scripts/v4_0_postgres_ddl_checks.sql

\echo Test 2 - DDL checks completed OK.

--
-- Eof