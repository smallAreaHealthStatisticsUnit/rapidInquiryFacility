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
-- Rapid Enquiry Facility (RIF) - Test harness implementation
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
-- Error codes:
-- _rif40_sql_test_dblink:								71350 to 71399
--
-- THIS CODE IS NOW OBSOLETE
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing
\set VERBOSITY terse
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

DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test_dblink_connect(VARCHAR, INTEGER);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test_dblink_disconnect(VARCHAR);

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_sql_test_dblink_connect(connection_name VARCHAR, debug_level INTEGER DEFAULT 0)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_sql_test_dblink_connect()
Parameters:	Connection name, debug level 
Returns:	Nothing
Description:Create dblink() session; 
			run rif40_startup();
			Initialise logging
 */
DECLARE
	d0st CURSOR(l_connection_name VARCHAR) FOR
		SELECT a.startup
 		  FROM dblink(l_connection_name,
				      'WITH a AS (SELECT rif40_sql_pkg.rif40_startup() AS a) SELECT 1 AS startup FROM a;') AS a(startup INTEGER);
	d0st_rec	RECORD;		
	d1st CURSOR(l_connection_name VARCHAR, l_debug_level INTEGER) FOR
		SELECT a.log_setup
 		  FROM dblink(l_connection_name,
				      'WITH a AS (SELECT rif40_sql_pkg._rif40_sql_test_log_setup('||l_debug_level||') AS a) SELECT 1 AS log_setup FROM a;') AS a(log_setup INTEGER);
	d1st_rec	RECORD;	
	d2st CURSOR(l_connection_name VARCHAR) FOR
		WITH a AS (
			SELECT UNNEST(dblink_get_connections()) AS connection_name
		)
		SELECT a.connection_name
		  FROM a
		 WHERE a.connection_name = l_connection_name;
	d2st_rec RECORD;
--
	debug_level_text	VARCHAR;
BEGIN
--
-- Check connection does not exist
--
	OPEN d2st(connection_name);
	FETCH d2st INTO d2st_rec;
	CLOSE d2st;
	IF d2st_rec.connection_name IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71350, 'rif40_sql_test_dblink_connect', 
			'dblink() subtransaction: % already exists, cannot create subtransaction',
			connection_name::VARCHAR);		
	END IF;
--
-- This only works if username = password; to be replaced by Node.js
--
--	BEGIN
		PERFORM dblink_connect(connection_name, 'hostaddr='||host(inet_server_addr())||' dbname='||current_database()||' user='||USER||' password='||USER||' sslmode=prefer');
--	EXCEPTION	
	/*
Detail: FATAL:  no pg_hba.conf entry for host "129.31.37.103", user "pch", database "sahsuland_dev", SSL on
FATAL:  no pg_hba.conf entry for host "129.31.37.103", user "pch", database "sahsuland_dev", SSL off

Context: SQL statement "SELECT dblink_connect(connection_name, 'hostaddr='||host(inet_server_addr())||' dbname='|
prefer')"
PL/pgSQL function rif40_sql_test_dblink_connect(character varying,integer) line 44 at PERFORM
SQL statement "SELECT rif40_sql_pkg.rif40_sql_test_dblink_connect('test_8_triggers.sql', debug_level)"
PL/pgSQL function inline_code_block line 33 at PERFORM
SQLSTATE: 08001
Error in command execution
	 */
--		WHEN others THEN /* Try connection without password */
/* This will not work:

Detail: Non-superusers must provide a password in the connection string.
Context: SQL statement "SELECT dblink_connect(connection_name, 'hostaddr='||host(inet_server_addr())||' dbname='||current_database()||' user='||USER||' sslmode=prefer')"
PL/pgSQL function rif40_sql_test_dblink_connect(character varying,integer) line 60 at PERFORM
SQL statement "SELECT rif40_sql_pkg.rif40_sql_test_dblink_connect('test_8_triggers.sql', debug_level)"
PL/pgSQL function inline_code_block line 33 at PERFORM
SQLSTATE: 2F003
 */
--			PERFORM dblink_connect(connection_name, 'hostaddr='||host(inet_server_addr())||' dbname='||current_database()||' user='||USER||' sslmode=prefer');				
--	END;
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test_dblink_connect', 
		'[71351] dblink() subtransaction: % for testing connected to: % as: %', 
		connection_name::VARCHAR, current_database()::VARCHAR, USER::VARCHAR);		
--
-- Enable messages - This does not work as the below example illustrates:
-- 

/*
SELECT dblink_connect('dblink_trans',
	'dbname='||current_database()||' user='||USER||' password='||USER||' application_name =''XX''');
SELECT dblink_exec('dblink_trans', 'SET client_min_messages TO info');
SELECT a.setting FROM dblink('dblink_trans',
	'SELECT name||'':''||setting AS setting FROM pg_settings WHERE name IN (''client_min_messages'', ''application_name'', ''log_destination'')') AS a(setting Text);	
SELECT dblink('dblink_trans','DROP TABLE IF EXISTS xxyy;');
SELECT dblink_disconnect('dblink_trans');

i.e. there is no NOTICE from:

sahsuland_dev=> DROP TABLE IF EXISTS xxyy;
NOTICE:  table "xxyy" does not exist, skipping
DROP TABLE

sahsuland_dev=> SELECT dblink_connect('dblink_trans',
sahsuland_dev(>         'dbname='||current_database()||' user='||USER||' password='||USER||' application_name =''XX''');
 dblink_connect
----------------
 OK
(1 row)


sahsuland_dev=> SELECT dblink_exec('dblink_trans', 'SET client_min_messages TO info');
 dblink_exec
-------------
 SET
(1 row)


sahsuland_dev=> SELECT a.setting FROM dblink('dblink_trans',
sahsuland_dev(>         'SELECT name||'':''||setting AS setting FROM pg_settings WHERE name IN (''client_min_messages'', ''application_name'', ''log_destination'')') AS a(setting Text);
         setting
--------------------------
 application_name:XX
 client_min_messages:info
 log_destination:stderr
(3 rows)


sahsuland_dev=> SELECT dblink('dblink_trans','DROP TABLE IF EXISTS xxyy;');
     dblink
----------------
 ("DROP TABLE")
(1 row)


sahsuland_dev=> SELECT dblink_disconnect('dblink_trans');
 dblink_disconnect
-------------------
 OK
(1 row)
	
 */
	PERFORM dblink_exec(connection_name, 'SET application_name = '''||connection_name||'''');
--	IF debug_level > 0 THEN
--		debug_level_text:='''DEBUG'||debug_level||'''';
--		PERFORM dblink_exec(connection_name, 'SET client_min_messages TO '||debug_level_text);
--	ELSE
		PERFORM dblink_exec(connection_name, 'SET client_min_messages TO info');
--	END IF;
--
-- Call rif40_sql_pkg._rif40_sql_test_log_setup
--
	OPEN d1st(connection_name, debug_level);
	FETCH d1st INTO d1st_rec;
	CLOSE d1st;
	
--
-- Call rif40_startup()
--
	OPEN d0st(connection_name);
	FETCH d0st INTO d0st_rec;
	CLOSE d0st;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg.rif40_sql_test_dblink_connect(VARCHAR, INTEGER) IS 'Function: 	rif40_sql_test_dblink_connect()
Parameters:	Connection name, debug level 
Returns:	Nothing
Description:Create dblink() session; 
			run rif40_startup();
			Initialise logging';
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_sql_test_dblink_connect(VARCHAR, INTEGER) TO PUBLIC;

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_sql_test_dblink_disconnect(connection_name VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_sql_test_dblink_disconnect()
Parameters:	Connection name 
Returns:	Nothing
Description:Release dblink() session;
 */
DECLARE
	d2st CURSOR(l_connection_name VARCHAR) FOR
		WITH a AS (
			SELECT UNNEST(dblink_get_connections()) AS connection_name
		)
		SELECT a.connection_name
		  FROM a
		 WHERE a.connection_name = l_connection_name;
	d2st_rec RECORD;
BEGIN
--
-- Check connection does not exist
--
	OPEN d2st(connection_name);
	FETCH d2st INTO d2st_rec;
	CLOSE d2st;
	IF d2st_rec.connection_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71352, 'rif40_sql_test_dblink_disconnect', 
			'dblink() subtransaction: % does not exist, cannot destroy subtransaction',
			connection_name::VARCHAR);		
	END IF;
--
	PERFORM dblink_disconnect(connection_name);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test_dblink_disconnect', 
		'[71353] dblink() subtransaction: % for testing disconnected from: % as: %', 
		connection_name::VARCHAR,		
		current_database()::VARCHAR, USER::VARCHAR);	
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg.rif40_sql_test_dblink_disconnect(VARCHAR) IS 'Function: 	rif40_sql_test_dblink_disconnect()
Parameters:	Connection name 
Returns:	Nothing
Description:Release dblink() session;';
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_sql_test_dblink_disconnect(VARCHAR) TO PUBLIC;
	
--
-- Eof