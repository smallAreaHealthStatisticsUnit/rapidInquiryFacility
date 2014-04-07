-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/02/28 10:54:02 $
-- Type: Postgres PSQL script
-- $RCSfile: db_create.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/psql_scripts/db_create.sql,v $
-- $Revision: 1.1 $
-- $Id$
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Create SAHSULAND
--
-- Copyright:
--
-- The RIF is free software; you can redistribute it and/or modify it under
-- the terms of the GNU General Public License as published by the Free
-- Software Foundation; either version 2, or (at your option) any later
-- version.
--
-- The RIF is distributed in the hope that it will be useful, but WITHOUT ANY
-- WARRANTY; without even the implied warranty of MERCHANTABILITY or
-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
-- for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this file; see the file LICENCE.  If not, write to:
--
-- UK Small Area Health Statistics Unit,
-- Dept. of Epidemiology and Biostatistics
-- Imperial College School of Medicine (St. Mary's Campus),
-- Norfolk Place,
-- Paddington,
-- London, W2 1PG
-- United Kingdom
--
-- The RIF uses Oracle 11g, PL/SQL, PostGres and PostGIS as part of its implementation.
--
-- Oracle11g, PL/SQL and Pro*C are trademarks of the Oracle Corporation.
--
-- All terms mentioned in this software and supporting documentation that are known to be trademarks
-- or service marks have been appropriately capitalised. Imperial College cannot attest to the accuracy
-- of this information. The use of a term in this software or supporting documentation should NOT be
-- regarded as affecting the validity of any trademark or service mark.
--
-- Summary of functions/procedures:
--
-- To be added
--
-- Error handling strategy:
--
-- Output and logging procedures do not HANDLE or PROPAGATE errors. This makes them safe to use
-- in package initialisation and NON recursive.
--
-- References:
--
-- 	None
--
-- Dependencies:
--
--	Packages: None
--
-- 	<This should include: packages, non packages procedures and functions, tables, views, objects>
--
-- Portability:
--
--	Linux, Windows 2003/2008, Oracle 11gR1
--
-- Limitations:
--
-- Change log:
--
-- $Log: db_create.sql,v $
-- Revision 1.1  2014/02/28 10:54:02  peterh
--
-- Further work on transfer of SAHSUland to github. sahsuland build scripts Ok, UK91 geog added.
--
-- Revision 1.1  2013/03/20 15:46:42  peterh
--
-- Result for first install on laptop
--
-- Revision 1.5  2013/03/14 17:35:20  peterh
-- Baseline for TX to laptop
--
-- Prototype database creation script
--
\set ECHO OFF
\set ON_ERROR_STOP ON
\o db_create.rpt

BEGIN;

--
-- Check command line parameters
--
--\prompt 'Password: ' password
\set npassword '''XXXX':encrypted_password''''
SET rif40.password TO :npassword;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR SELECT CURRENT_SETTING('rif40.password') AS password;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF UPPER(c1_rec.password) = 'XXXX' THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: No -v encrypted_password=<encrypted password> parameter';	
	ELSE
		RAISE INFO 'db_create.sql() encrypted password="%"', SUBSTR(c1_rec.password, 5);
	END IF;
END;
$$;
SET rif40.password TO :encrypted_password;

--
-- Check user is postgres on postgres
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'postgres' AND current_database() = 'postgres' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on postgres database (%)', 
			user, current_database();	
	END IF;
END;
$$;

--
-- Check DB version
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
	 	SELECT version() AS version, SUBSTR(version(), 12, 3)::NUMERIC as major_version;
	c1_rec RECORD;
--
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	--
	IF c1_rec.major_version < 9.3 THEN
		RAISE EXCEPTION 'db_create.sql() C902xx: RIF requires Postgres version 9.3 or higher; current version: %',
			c1_rec.version::VARCHAR;
	ELSE
		RAISE INFO 'db_create.sql() RIF required Postgres version 9.3 or higher OK; current version: %', 
			c1_rec.version::VARCHAR;
	END IF;
END;
$$;

--
-- Check availability of extensions
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR(l_name VARCHAR) FOR
		SELECT name, default_version
		  FROM pg_available_extensions
		 WHERE name = l_name;
	c1_rec RECORD;
--
	namelist VARCHAR[]:=ARRAY['adminpack', 'plperl', 'postgis', 'postgis_topology', 'pgcrypto', 'sslinfo', 'xml2', 'dblink', 'plr'];
	x VARCHAR;
	i INTEGER:=0;
BEGIN
	FOREACH x IN ARRAY namelist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		IF c1_rec.name IS NULL THEN
			RAISE WARNING 'db_create.sql() RIF required extension: % is not installable', x;
			i:=i+1;
		ELSE
			RAISE INFO 'db_create.sql() RIF required extension: % V% is installable', c1_rec.name, c1_rec.default_version;
		END IF;
		CLOSE c1;
	END LOOP;
	IF i > 0 THEN
		RAISE EXCEPTION 'db_create.sql() C209xx: % RIF required extensions are not installable', 
			i::VARCHAR USING HINT='See previous warnings';
	END IF;
END;
$$;

\echo ************************************************************************************
\echo *                                                                                  
\echo * WARNING !!!                                                                      
\echo *                                                                                  
\echo * This script will drop sahsuland, re-create it and reset the passwords to the     
\echo * default                                                                          
\echo *                                                                                  
\echo ************************************************************************************
\prompt 'Press any key to continue or <control-C> to interrupt: ' ans

CREATE EXTENSION IF NOT EXISTS plr;
DROP EXTENSION plr;
--
-- This will fail if:
--
-- a) PLR (http://www.joeconway.com/plr) is not installed as per instructions
-- 
/*
In the instructions below:
	PostgreSQL is installed to <pgdir>
	R is installed to <rdir>

For example, these directories might be:
	<pgdir> = C:\PostgreSQL\9.3
	<rdir>  = C:\R\R-3.0.2

I recommend you ensure there are no spaces in the pathname to either <pgdir> or <rdir>.
Correct quoting of pathnames with spaces is left as an exercise for the reader ;-)

The following files are contained in this zip file:
---------------------------------------------------
README.txt:			place in <pgdir>\doc\extension
plr.dll:			place in <pgdir>\lib
plr.sql:			place in <pgdir>\share\extension
plr.control:			place in <pgdir>\share\extension
plr--8.3.0.15.sql		place in <pgdir>\share\extension
plr--unpackaged--8.3.0.15.sql	place in <pgdir>\share\extension

Ensure the following environment variables are set *prior* to starting PostgreSQL:
---------------------------------------------------
PATH=<pgdir>\bin;<rdir>\bin\x64;$PATH
R_HOME=<rdir
 */
--
-- See also: http://www.bostongis.com/PrinterFriendly.aspx?content_name=postgresql_plr_tut01
--           http://www.joeconway.com/plr/doc/plr-install.html 
--
-- b) Postgres is NOT restarted
--

\echo Creating users/roles...

--
-- Create users and roles
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_user WHERE usename = l_name;
	c2 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.password') AS password;
	c3 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_roles WHERE rolname = l_name;

	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
--
	userlist VARCHAR[]:=ARRAY['rif40', 'gis', 'pop', 'peterh'];
	rolelist VARCHAR[]:=ARRAY['rif_user', 'rif_manager', 'rif_no_suppression', 'rifupg34'];
	x VARCHAR;
	sql_stmt VARCHAR;
BEGIN
	OPEN c2;
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	sql_stmt:='ALTER USER postgres ENCRYPTED PASSWORD  '''||c2_rec.password||'''';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	PERFORM sql_stmt;
--
	FOREACH x IN ARRAY userlist LOOP
		OPEN c1(x);
		FETCH c1 INTO c1_rec;
		CLOSE c1;
		IF c1_rec.usename IS NOT NULL THEN
			RAISE INFO 'db_create.sql() RIF schema user % exists', c1_rec.usename::VARCHAR;
		ELSE

	    		sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION ENCRYPTED PASSWORD '''||c2_rec.password||'''';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;
	END LOOP;
	--
	FOREACH x IN ARRAY rolelist LOOP
		OPEN c3(x);
		FETCH c3 INTO c3_rec;
		CLOSE c3;
		IF c3_rec.rolname IS NOT NULL THEN
			RAISE INFO 'db_create.sql() RIF schema role % exists', c3_rec.rolname::VARCHAR;
		ELSE

	    		sql_stmt:='CREATE ROLE '||x||
				' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT NOLOGIN NOREPLICATION';
			RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
			EXECUTE sql_stmt;
		END IF;
	END LOOP;
--
-- Revoke PUBLIC
--
	sql_stmt:='REVOKE CREATE ON SCHEMA public FROM PUBLIC';
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;

END;
\echo Creating SAHSULAND...

--
-- Create SAHSULAND database
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR(l_name VARCHAR) FOR
		SELECT * FROM pg_tablespace WHERE spcname = l_name;
	c1_rec RECORD;
--
	sql_stmt	VARCHAR;
BEGIN
	OPEN c1('sahsuland');
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.spcname IS NULL THEN
		sql_stmt:='CREATE TABLESPACE sahsuland LOCATION ''C:\\PostgresDB\\sahsuland''';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	END IF;
--
END;
$$;

\set echo ALL
DROP DATABASE IF EXISTS sahsuland;
CREATE DATABASE sahsuland WITH OWNER rif40 TABLESPACE sahsuland;
COMMENT ON DATABASE sahsuland IS 'RIF V4.0 PostGres SAHSULAND Example Database';

\c sahsuland postgres localhost 
--
-- Check user is postgres on sahsuland
--
\set ECHO OFF
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'postgres' AND current_database() = 'sahsuland' THEN
		RAISE INFO 'db_create.sql() User check: %', user;	
	ELSE
		RAISE EXCEPTION 'db_create.sql() C209xx: User check failed: % is not postgres on postgres database (%)', 
			user, current_database();	
	END IF;
END;
$$;
\set echo ALL
CREATE EXTENSION IF NOT EXISTS plr;

RESET rif40.password;
\echo Restoring database...
--
-- Shell pg_restore
-- 
--\! pg_restore -d sahsuland -U postgres -v  sahsuland.dump > pg_restore.txt 2>&1
--
-- Powershell version
--
\! powershell -command "pg_restore -d sahsuland -U postgres -v  sahsuland.dump 2>&1 | tee ('pg_restore{0}.log' -f (Get-Date -format 'yyyy.MM.dd-HH.mm'))"
--
-- Display errors
--
\echo Database restore errors...
\! findstr "ERROR:" pg_restore.txt
/*

Errors like this can be ignored - others may not!

pg_restore: [archiver (db)] could not execute query: ERROR:  could not load library "C:/Program Files/PostgreSQL/9.3/lib/plperl.dll"
: The specified module could not be found.
pg_restore: [archiver (db)] could not execute query: ERROR:  extension "plperl" does not exist
pg_restore: [archiver (db)] could not execute query: ERROR:  could not open extension control file "C:/Program Files/PostgreSQL/9.3/
share/extension/oracle_fdw.control": No such file or directory
pg_restore: [archiver (db)] could not execute query: ERROR:  extension "oracle_fdw" does not exist
pg_restore: [archiver (db)] could not execute query: ERROR:  rule "geometry_columns_delete" for relation "geometry_columns" already
exists
pg_restore: [archiver (db)] could not execute query: ERROR:  rule "geometry_columns_insert" for relation "geometry_columns" already
exists
pg_restore: [archiver (db)] could not execute query: ERROR:  rule "geometry_columns_update" for relation "geometry_columns" already
exists
 */
ALTER DATABASE sahsuland SET search_path TO rif40,public,topology,gis,pop,rif40_sql_pkg;

\c sahsuland
\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
	sql_stmt VARCHAR;
BEGIN
--
-- Force rebuild of user objects
--
	sql_stmt:='DROP VIEW IF EXISTS '||USER||'.rif40_user_version';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_ddl_checks:DEBUG3');
	PERFORM rif40_log_pkg.rif40_log_setup();
	PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
        PERFORM rif40_sql_pkg.rif40_startup();
	IF USER != 'rif40' THEN
	        PERFORM rif40_sql_pkg.rif40_ddl_checks();
	END IF;
	PERFORM rif40_log_pkg.rif40_remove_from_debug('rif40_ddl_checks');
END;
$$;

\echo SAHSUland installed.
--
-- Eof
