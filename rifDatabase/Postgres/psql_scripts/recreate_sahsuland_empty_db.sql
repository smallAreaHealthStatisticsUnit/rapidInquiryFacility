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
-- Rapid Enquiry Facility (RIF) - Database creation script: SAHSULAND_EMPTY
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

--\set ECHO all
\set ECHO none
\set ON_ERROR_STOP on
\echo Creating SAHSULAND_EMPTY database if required

--
-- check connected as postgres to postgres
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_user != 'postgres' OR current_database() != 'postgres' THEN
		RAISE EXCEPTION 'db_create.sql() current_user: % and current database: % must both be postgres', current_user, current_database();
	END IF;
END;
$$;

\set ntestuser '''XXXX':testuser''''
SET rif40.testuser TO :ntestuser;

DROP DATABASE IF EXISTS sahsuland_empty;
CREATE DATABASE sahsuland_empty WITH OWNER rif40 /* No sahsuland tablespace */;
COMMENT ON DATABASE sahsuland_empty IS 'RIF V4.0 PostGres SAHSULAND Empty Database';

\c sahsuland_empty postgres :pghost
--
-- Start transaction 3: sahsuland_empty build
--
BEGIN;
--
-- Check user is postgres on sahsuland_empty
--
\set ECHO NONE
SET rif40.testuser TO :ntestuser;
DO LANGUAGE plpgsql $$
DECLARE	
	c1 CURSOR(l_schema VARCHAR) FOR
		SELECT * FROM information_schema.schemata
		 WHERE LOWER(schema_name) = LOWER(l_schema);
	c2 CURSOR FOR
		 SELECT usename
		   FROM pg_user
		 WHERE pg_has_role(usename, 'rif_manager', 'MEMBER')
		    OR pg_has_role(usename, 'rif_user', 'MEMBER')
		    OR pg_has_role(usename, 'rif_student', 'MEMBER')
		  ORDER BY 1;
	c1_rec RECORD;
	c2_rec RECORD;
--
	schemalist VARCHAR[]:=ARRAY['rif40_dmp_pkg', 'rif40_sql_pkg', 'rif40_sm_pkg', 'rif40_log_pkg', 'rif40_trg_pkg',
			'rif40_geo_pkg', 'rif40_xml_pkg', 'rif40_R_pkg', 
			'rif40', 'gis', 'pop', 'rif_studies', 'rif_data', 'data_load', 'rif40_partitions'];
	x VARCHAR;
	sql_stmt VARCHAR;
	u_name	VARCHAR;
BEGIN
	u_name:=LOWER(SUBSTR(CURRENT_SETTING('rif40.testuser'), 5));
	IF user = 'postgres' AND current_database() = 'sahsuland_empty' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on sahsuland_empty database (%)', 
			user, current_database();	
	END IF;
--
	sql_stmt:='CREATE EXTENSION IF NOT EXISTS postgis';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='CREATE EXTENSION IF NOT EXISTS adminpack';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
-- RIF40 grants
--	
	sql_stmt:='GRANT ALL ON DATABASE sahsuland_empty to rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='REVOKE CREATE ON SCHEMA public FROM rif40';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
	sql_stmt:='GRANT CONNECT ON DATABASE sahsuland_empty to '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
--
-- Add user, rif_studies schema and PKG (package) schemas
--
	OPEN c1(u_name);
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.schema_name IS NULL THEN
		sql_stmt:='CREATE SCHEMA '||u_name||' AUTHORIZATION '||u_name;
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT ALL 
		ON SCHEMA '||u_name||' TO '||u_name;
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	END IF;
--
-- Re-create user schemas
--
	FOR c2_rec IN c2 LOOP
		OPEN c1(c2_rec.usename);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.schema_name IS NULL THEN
			sql_stmt:='CREATE SCHEMA '||c2_rec.usename||' AUTHORIZATION '||c2_rec.usename;
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
			sql_stmt:='GRANT ALL 
			ON SCHEMA '||c2_rec.usename||' TO '||c2_rec.usename;
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;	
	END LOOP;
--
	FOREACH x IN ARRAY schemalist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.schema_name IS NULL THEN
			sql_stmt:='CREATE SCHEMA '||x||' AUTHORIZATION rif40';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;
		sql_stmt:='GRANT ALL ON SCHEMA '||x||' TO rif40 WITH GRANT OPTION';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT USAGE ON SCHEMA '||x||' TO rif_user';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
		sql_stmt:='GRANT USAGE ON SCHEMA '||x||' TO rif_manager WITH GRANT OPTION';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
--
-- Grant CREATE privilege to data load schema; allow onwards grants
--
		IF x IN ('rif_data', 'data_load') THEN
				sql_stmt:='GRANT CREATE ON SCHEMA '||x||' TO rif_manager WITH GRANT OPTION';
				RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
				EXECUTE sql_stmt;
		END IF;
	END LOOP;

--
-- Set default search pathname
--
	sql_stmt:='ALTER DATABASE sahsuland_empty SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
\set echo ALL

--
-- Set search path for rif40
--
ALTER USER rif40 SET search_path 
	TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
SET search_path 
	TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
	
--
-- End transaction 3: sahsuland_empty build
--
END;


--
-- Eof
