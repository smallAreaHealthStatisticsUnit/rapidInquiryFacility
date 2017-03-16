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
-- Rapid Enquiry Facility (RIF) - Test 4: Study ID 1
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
\set ON_ERROR_STOP ON

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'test_3_user_setup.sql: T3__01: User check: %', user;	
	ELSE
		RAISE EXCEPTION 'test_3_user_setup.sql: T3__02: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

--
-- Reload DDL check code
--
--\i ../PLpgsql/v4_0_rif40_sql_pkg_ddl_checks.sql

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
		RAISE EXCEPTION 'test_3_user_setup.sql: T3__03: No -v testuser=<test user account> parameter';	
	ELSE
		RAISE INFO 'test_3_user_setup.sql: T3__04: test user account parameter="%"', c1_rec.testuser;
	END IF;
--
-- Test account exists
--
	OPEN c2(LOWER(SUBSTR(c1_rec.testuser, 5)));
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.usename IS NULL THEN
		RAISE EXCEPTION 'test_3_user_setup.sql: T3__05: User account does not exist: %', LOWER(SUBSTR(c1_rec.testuser, 5));	
	ELSIF pg_has_role(c2_rec.usename, 'rif_user', 'MEMBER') THEN
		RAISE INFO 'test_3_user_setup.sql: T3__06: user account="%" is a rif_user', c2_rec.usename;
	ELSIF pg_has_role(c2_rec.usename, 'rif_manager', 'MEMBER') THEN
		RAISE INFO 'test_3_user_setup.sql: T3__07: user account="%" is a rif manager', c2_rec.usename;
	ELSE
		RAISE EXCEPTION 'test_3_user_setup.sql: T3__08: User account: % is not a rif_user or rif_manager', c2_rec.usename;	
	END IF;
--
END;
$$;

--
-- Connect as testuser
--
\c - :testuser
--
-- Test in a single transaction  
--
-- Initialise user
--
\i ../psql_scripts/v4_0_user.sql  
--
-- Re-connect as rif40
--
\c - rif40
--
-- Call common setup to run as testuser (passed parameter)
--
\i ../psql_scripts/test_scripts/common_setup.sql

--
-- Begin transaction
--
BEGIN;

\pset title 'Search path'
WITH a AS (
	SELECT unnest(current_schemas(true)) AS schema
), b AS (
	SELECT ROW_NUMBER() OVER() AS recnum, schema
      FROM a
)
SELECT recnum, schema, CASE	
			WHEN recnum = 1 AND schema LIKE 'pg_temp%' THEN true
			WHEN recnum = 2 AND schema = 'pg_catalog' THEN true
			WHEN recnum = 3 AND schema = USER THEN true
			WHEN recnum = 4 AND schema = 'rif40' THEN true
			WHEN recnum = 5 AND schema = 'public' THEN true
			WHEN recnum = 6 AND schema = 'topology' THEN true
			WHEN recnum IN (6,7) AND schema = 'gis' THEN true
			WHEN recnum IN (7,8) AND schema = 'pop' THEN true
			WHEN recnum IN (8,9) AND schema = 'rif_data' THEN true
			WHEN recnum IN (9,10) AND schema = 'data_load' THEN true
			WHEN recnum IN (10,11) AND schema = 'rif40_sql_pkg' THEN true
			WHEN recnum IN (11,12) AND schema = 'rif_studies' THEN true
			WHEN recnum IN (12,13) AND schema = 'rif40_partitions' THEN true
			ELSE false
	   END AS schema_ok, CASE	
			WHEN recnum = 1 THEN 'pg_temp%'
			WHEN recnum = 2 THEN 'pg_catalog'
			WHEN recnum = 3 THEN USER
			WHEN recnum = 4 THEN 'rif40'
			WHEN recnum = 5 THEN 'public'
			WHEN recnum = 6 THEN 'topology'
			WHEN recnum = 7 THEN 'gis'
			WHEN recnum = 8 THEN 'pop'
			WHEN recnum = 9 THEN 'rif_data'
			WHEN recnum = 10 THEN 'data_load'
			WHEN recnum = 11 THEN 'rif40_sql_pkg'
			WHEN recnum = 12 THEN 'rif_studies'
			WHEN recnum = 13 THEN 'rif40_partitions'
			ELSE 'UNKNOWN'
	   END expected_schema
  FROM b;
\pset title	   
/*
 recnum |      schema      | schema_ok | expected_schema
--------+------------------+-----------+-----------------
      1 | pg_temp_2        | t         | pg_temp%
      2 | pg_catalog       | t         | pg_catalog
      3 | pch              | t         | pch
      4 | rif40            | t         | rif40
      5 | public           | t         | public
      6 | gis              | t         | topology
      7 | pop              | t         | gis
      8 | rif_data         | t         | pop
      9 | data_load        | t         | rif_data
     10 | rif40_sql_pkg    | t         | data_load
     11 | rif_studies      | t         | rif40_sql_pkg
     12 | rif40_partitions | t         | rif_studies
(12 rows)
 */

DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR	
		SELECT COUNT(numerator_table) AS total
		  FROM rif40_num_denom;
	c2 CURSOR(l_table VARCHAR) FOR
		SELECT l_table AS tablename, COUNT(tablename) AS total
		  FROM pg_tables
		 WHERE tableowner = USER;
	c3 CURSOR FOR
		WITH a AS (
			SELECT unnest(current_schemas(true)) AS schema
		), b AS (
			SELECT ROW_NUMBER() OVER() AS recnum, schema
			  FROM a
		)
		SELECT recnum, schema, CASE	
					WHEN recnum = 1 AND schema LIKE 'pg_temp%' THEN true
					WHEN recnum = 2 AND schema = 'pg_catalog' THEN true
					WHEN recnum = 3 AND schema = USER THEN true
					WHEN recnum = 4 AND schema = 'rif40' THEN true
					WHEN recnum = 5 AND schema = 'public' THEN true
					WHEN recnum = 6 AND schema = 'topology' THEN true
					WHEN recnum IN (6,7) AND schema = 'gis' THEN true
					WHEN recnum IN (7,8) AND schema = 'pop' THEN true
					WHEN recnum IN (8,9) AND schema = 'rif_data' THEN true
					WHEN recnum IN (9,10) AND schema = 'data_load' THEN true
					WHEN recnum IN (10,11) AND schema = 'rif40_sql_pkg' THEN true
					WHEN recnum IN (11,12) AND schema = 'rif_studies' THEN true
					WHEN recnum IN (12,13) AND schema = 'rif40_partitions' THEN true
					ELSE false
			END AS schema_ok, CASE
					WHEN recnum = 1 THEN 'pg_temp%'
					WHEN recnum = 2 THEN 'pg_catalog'
					WHEN recnum = 3 THEN USER
					WHEN recnum = 4 THEN 'rif40'
					WHEN recnum = 5 THEN 'public'
					WHEN recnum = 6 THEN 'topology'
					WHEN recnum = 7 THEN 'gis'
					WHEN recnum = 8 THEN 'pop'
					WHEN recnum = 9 THEN 'rif_data'
					WHEN recnum = 10 THEN 'data_load'
					WHEN recnum = 11 THEN 'rif40_sql_pkg'
					WHEN recnum = 12 THEN 'rif_studies'
					WHEN recnum = 12 THEN 'rif40_partitions'
					ELSE 'UNKNOWN'
			   END expected_schema
		  FROM b; 
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
BEGIN
--
-- Test user access
--
-- Check a) RIF40_NUM_DENOM has >=1 row
--
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.total = 0 AND current_database() = 'sahsuland_empty' THEN
		RAISE INFO 'C20900: RIF40_NUM_DENOM had no rows (sahsuland_empty)';
	ELSIF c1_rec.total = 0 AND current_database() != 'sahsuland_empty' THEN
		RAISE EXCEPTION 'C20900: RIF40_NUM_DENOM had no rows';
	ELSIF c1_rec.total > 0 AND current_database() = 'sahsuland_empty' THEN
		RAISE EXCEPTION 'C20900: RIF40_NUM_DENOM had % rows I(sahsuland_empty)', c1_rec.total::Text;
	ELSE
		RAISE INFO 'C20900: RIF40_NUM_DENOM had % rows', c1_rec.total::Text;	
	END IF;
	
--
-- b) G_ tables exist
--
	OPEN c2('g_rif40_study_areas');
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.total = 0 THEN
		RAISE EXCEPTION 'C20900: Temporary tale: % not found', c2_rec.tablename;
	ELSE
		RAISE INFO 'C20900: Temporary tale: % OK', c2_rec.tablename;
	END IF;
	OPEN c2('g_rif40_comparison_areas');
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.total = 0 THEN
		RAISE EXCEPTION 'C20900: Temporary table: % not found', c2_rec.tablename;
	ELSE
		RAISE INFO 'C20900: Temporary table: % OK', c2_rec.tablename;
	END IF;
--
-- c) Search path is correct
--
	FOR c3_rec IN c3 LOOP
		IF c3_rec.schema_ok = false THEN
			RAISE EXCEPTION 'C20900: Position: %; Schema: % found, expecting: %', c3_rec.recnum, c3_rec.schema, c3_rec.expected_schema;
		ELSE
			RAISE INFO 'C20900: Position: %; Schema: % found OK', c3_rec.recnum, c3_rec.schema;
		END IF;
	END LOOP;
END;
$$;
--
-- End transaction
--
END;

--
-- Eof