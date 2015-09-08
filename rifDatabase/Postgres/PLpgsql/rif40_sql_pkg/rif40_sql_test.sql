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
-- rif40_sql_test:										71150 to 71199
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

--
-- Include support functions
--
\i ../PLpgsql/rif40_sql_pkg/_rif40_reduce_dim.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_test_sql_template.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_test_harness.sql

--
-- Test case 
--
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, INTEGER, VARCHAR);
	
-- Old	
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	INTEGER, BOOLEAN);	

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN, INTEGER);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, INTEGER);
	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN, INTEGER, Text[]);	
-- Old	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN);	
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, ANYARRAY,
	VARCHAR, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN, INTEGER);	
	
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test_dblink_connect(VARCHAR, INTEGER);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_sql_test_dblink_disconnect(VARCHAR);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_sql_test_log_setup(INTEGER);
	
CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_sql_test_register(
	test_stmt 					VARCHAR, 
	test_run_class 				VARCHAR, 
	test_case_title 			VARCHAR, 
	results 					ANYARRAY,
	results_xml					XML,
	pg_error_code_expected 		VARCHAR DEFAULT NULL, 
	raise_exception_on_failure 	BOOLEAN DEFAULT TRUE, 
	expected_result 			BOOLEAN DEFAULT TRUE,
	parent_test_id				INTEGER DEFAULT NULL,
	pg_debug_functions			Text[] DEFAULT NULL)
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_sql_test_register()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
			Test run class; usually the name of the SQL script that originally ran it,
            test case title,
 			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, 
			raise exception on failure,
			expected result,
			parent test id,
			Array of Postgres functions for test harness to enable debug on
Returns:	Test id, NULL if rif40_test_harness table not created yet
Description:Autoregister test case
 */
DECLARE
	c1st CURSOR FOR	
		SELECT * FROM pg_tables WHERE tablename = 'rif40_test_harness';
	c3st CURSOR (l_test_stmt 					VARCHAR, 
				 l_test_run_class				VARCHAR,
			     l_test_case_title 				VARCHAR, 
				 l_results 						Text[][], 
				 l_results_xml 					XML,
				 l_pg_error_code_expected		VARCHAR, 
				 l_raise_exception_on_failure 	BOOLEAN,
				 l_expected_result				BOOLEAN,
				 l_parent_test_id				INTEGER,
				 l_pg_debug_functions			Text[]) FOR
		INSERT INTO rif40_test_harness (
			test_stmt,
			test_run_class,
			test_case_title,
			pg_error_code_expected,
			raise_exception_on_failure,
			results,
			results_xml,
			expected_result,
			parent_test_id,
			pg_debug_functions)
		SELECT l_test_stmt, l_test_run_class, l_test_case_title, 
			   l_pg_error_code_expected, l_raise_exception_on_failure, l_results, 
			   l_results_xml, l_expected_result, l_parent_test_id, l_pg_debug_functions
		 WHERE NOT EXISTS (
			SELECT a.test_id
			  FROM rif40_test_harness a
			 WHERE a.test_case_title = l_test_case_title
		   AND COALESCE(a.parent_test_id, 0) = COALESCE(l_parent_test_id, 0))
		RETURNING *;
	c1st_rec RECORD;		
	c3st_rec RECORD;
	c5st CURSOR (l_test_case_title VARCHAR, l_parent_test_id INTEGER) FOR
		SELECT a.test_case_title, a.test_id
		  FROM rif40_test_harness a
		 WHERE a.test_case_title             = l_test_case_title
		   AND COALESCE(a.parent_test_id, 0) = COALESCE(l_parent_test_id, 0);
	c5st_rec RECORD;
	c6_st CURSOR(l_function_name VARCHAR) FOR 
		SELECT l.lanname||' function' AS object_type, 
			COALESCE(n.nspname, r.rolname)||'.'||p.proname AS object_name 					/* Functions */
		  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
			LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
		 WHERE p.prolang    = l.oid
		   AND p.prorettype = t.oid
		   AND p.proowner   IN (SELECT oid FROM pg_roles WHERE rolname IN ('postgres', 'rif40'))
		   AND p.proowner   = r.oid
		   AND p.proname    = LOWER(l_function_name);
	c6st_rec RECORD;	
--
	f_test_id 	INTEGER;	
	l_func		VARCHAR;
BEGIN
	OPEN c1st;
	FETCH c1st INTO c1st_rec;
	CLOSE c1st;
	IF c1st_rec.tablename IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test_register', 
			'[71150] No test harness table NOT FOUND: %; TEST CASE NOT INSERTED',
			test_case_title::VARCHAR);		
		RETURN NULL;
	END IF;
	IF pg_debug_functions IS NOT NULL THEN
		FOREACH l_func IN ARRAY pg_debug_functions LOOP
			OPEN c6_st(l_func);
			FETCH c6_st INTO c6st_rec;
			IF c6st_rec.object_name IS NULL THEN
				PERFORM rif40_log_pkg.rif40_error(-71151, '_rif40_sql_test_register', 
					'Test case: %; debug function: % not found',
					test_case_title::VARCHAR,
					l_func::VARCHAR);						
			END IF;
			CLOSE c6_st;
		END LOOP;
	END IF;
--
	OPEN c3st(test_stmt, test_run_class, test_case_title, 
		results, results_xml, pg_error_code_expected, raise_exception_on_failure, expected_result, parent_test_id, pg_debug_functions);
	FETCH c3st INTO c3st_rec;
	CLOSE c3st;
	OPEN c5st(test_case_title, parent_test_id);
	FETCH c5st INTO c5st_rec;
	CLOSE c5st;		
	f_test_id:=c5st_rec.test_id;	
	IF f_test_id IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71152, '_rif40_sql_test_register', 
			'Test id for test case NOT FOUND: %; TEST CASE NOT INSERTED',
			c3st_rec.test_case_title::VARCHAR);	
	END IF;
--	
	IF c3st_rec.test_case_title IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_register', '[71153] Registered test case %: %; parent: %', 
			f_test_id::VARCHAR, test_case_title::VARCHAR, parent_test_id::VARCHAR);	
		UPDATE rif40_test_runs
		   SET number_test_cases_registered = number_test_cases_registered + 1
		 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;	
	ELSE	
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_register', '[71154] Test case already registered %: %', 
			f_test_id::VARCHAR, test_case_title::VARCHAR);
	END IF;
--
	IF f_test_id IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71155, '_rif40_sql_test_register', 
			'Test id for test case NOT FOUND: %; TEST CASE NEVER INSERTED',
			c3st_rec.test_case_title::VARCHAR);	
	END IF;
--
	RETURN f_test_id;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_sql_test_register(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN, INTEGER, Text[]) IS 'Function: 	_rif40_sql_test_register()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement,
			Test run class; usually the name of the SQL script that originally ran it
            test case title, 
			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, 
			raise exception on failure,
			expected result,
			parent test id,
			Array of Postgres functions for test harness to enable debug on
Returns:	Test id, NULL if rif40_test_harness table not created yet
Description:Autoregister test case';

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
		PERFORM rif40_log_pkg.rif40_error(-71154, 'rif40_sql_test_dblink_connect', 
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
		'[71155] dblink() subtransaction: % for testing connected to: % as: %', 
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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_sql_test_log_setup(debug_level INTEGER DEFAULT 0)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_sql_test_log_setup()
Parameters:	Debug level 
Returns:	Nothing
Description:Setup debug for test harness. Debug level must be 0 to 4
 */
DECLARE
	rif40_pkg_functions 		VARCHAR[] := ARRAY[
				'rif40_delete_study', 'rif40_ddl', 'rif40_sql_test', '_rif40_sql_test', '_rif40_test_sql_template', 
				'_rif40_sql_test_register', 'rif40_sql_test_dblink_connect', 'rif40_sql_test_dblink_disconnect',
				'rif40_test_harness', 'rif40_startup', '_rif40_sql_test_log_setup'];
	l_function 			VARCHAR;	
BEGIN
    PERFORM rif40_log_pkg.rif40_log_setup();
	PERFORM rif40_log_pkg.rif40_add_to_debug('_rif40_sql_test_log_setup:DEBUG1');
	IF debug_level IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_log_setup', 
			'[71156]: NULL debug_level; set to 1');
		debug_level:=1;
	ELSIF debug_level > 4 THEN
		PERFORM rif40_log_pkg.rif40_error(-71155, '_rif40_sql_test_log_setup', 'Invalid debug level [0-4]: %', debug_level::VARCHAR);
	ELSIF debug_level BETWEEN 1 AND 4 THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_log_setup', 
			'[71157]: debug_level %', debug_level::VARCHAR);
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
		FOREACH l_function IN ARRAY rif40_pkg_functions LOOP
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_log_setup', 
				'[71158]: Enable debug for function: %', 
				l_function::VARCHAR);
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
		END LOOP;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test_log_setup', 
			'[71159]: debug_level %', debug_level::VARCHAR);
	END IF;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg._rif40_sql_test_log_setup(INTEGER) IS 'Function: 	_rif40_sql_test_log_setup()
Parameters:	Debug level 
Returns:	Nothing
Description:Setup debug for test harness. Debug level must be 0 to 4';
GRANT EXECUTE ON FUNCTION rif40_sql_pkg._rif40_sql_test_log_setup(INTEGER) TO PUBLIC;
	
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
		PERFORM rif40_log_pkg.rif40_error(-71160, 'rif40_sql_test_dblink_disconnect', 
			'dblink() subtransaction: % does not exist, cannot destroy subtransaction',
			connection_name::VARCHAR);		
	END IF;
--
	PERFORM dblink_disconnect(connection_name);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test_dblink_disconnect', 
		'[71161] dblink() subtransaction: % for testing disconnected from: % as: %', 
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
	
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_sql_test(
	connection_name 			VARCHAR, 
	test_stmt 					VARCHAR, 
	test_case_title 			VARCHAR, 
	results 					ANYARRAY,
	results_xml					XML,
	pg_error_code_expected 		VARCHAR DEFAULT NULL, 
	raise_exception_on_failure 	BOOLEAN DEFAULT TRUE, 
	expected_result 			BOOLEAN DEFAULT TRUE)
RETURNS boolean
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_sql_test()
Parameters:	Connection name,
			SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title, 
			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, 
			raise exception on failure,
			expected result [has NO effect on the return code; for test harness] 
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

Used to check test SQL statements and triggers

Usage:

a) SELECT statements:
			
IF NOT (rif40_sql_pkg.rif40_sql_test(
	'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
	'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
	'{{01,01.015,01.015.016200,01.015.016200.2}
	,{01,01.015,01.015.016200,01.015.016200.3} 
	,{01,01.015,01.015.016200,01.015.016200.4} 
	,{01,01.015,01.015.016900,01.015.016900.1} 
	,{01,01.015,01.015.016900,01.015.016900.2} 
	,{01,01.015,01.015.016900,01.015.016900.3} 
	}'::Text[][]
	/- Use defaults -/)) THEN
	errors:=errors+1;
END IF;				

psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_method4():
Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200
---------------------------------------------------------------------
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_method4():
level1                                   | level2                                   | level3                                   | level4
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
01                                       | 01.015                                   | 01.015.016200                            | 01.015.016200.2
01                                       | 01.015                                   | 01.015.016200                            | 01.015.016200.3
01                                       | 01.015                                   | 01.015.016200                            | 01.015.016200.4
01                                       | 01.015                                   | 01.015.016900                            | 01.015.016900.1
01                                       | 01.015                                   | 01.015.016900                            | 01.015.016900.2
01                                       | 01.015                                   | 01.015.016900                            | 01.015.016900.3
(6 rows)
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_sql_test(): [71152] PASSED: no extra rows for test: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_sql_test(): [71155] PASSED: no missing rows for test: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_sql_test(): [71158] PASSED: Test case: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 no exceptions, no errors, no missing or extra data
				
i)   Original SQL statement:
	SELECT * FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200');
ii)  Add ORDER BY clause, expand * (This becomes the test case SQL)
	SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200') ORDER BY level4;			   
iii) Convert results to array form (Cast to text, string ) 
	[the function rif40_sql_pkg._rif40_test_sql_template() will automate this]

	SELECT ''''||
		   REPLACE(ARRAY_AGG(
				(ARRAY[level1::Text, level2::Text, level3::Text, level4::Text]::Text||E'\n')::Text ORDER BY level4)::Text, 
				'"'::Text, ''::Text)||'''::Text[][]' AS res 
	  FROM sahsuland_geography
	 WHERE level3 IN ('01.015.016900', '01.015.016200');

						 res
	---------------------------------------------
	 '{{01,01.015,01.015.016200,01.015.016200.2}+
	 ,{01,01.015,01.015.016200,01.015.016200.3} +
	 ,{01,01.015,01.015.016200,01.015.016200.4} +
	 ,{01,01.015,01.015.016900,01.015.016900.1} +
	 ,{01,01.015,01.015.016900,01.015.016900.2} +
	 ,{01,01.015,01.015.016900,01.015.016900.3} +
	 }'::Text[][]
	(1 row)
	
	Example call:
	
	PERFORM rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][]);

	Example expand of array to setof record
		  
		WITH a AS (
			SELECT '{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][] AS res
		), row AS (
			SELECT generate_series(1,array_upper(a.res, 1)) AS series
			  FROM a
		)
		SELECT  row.series, 
				(a.res)[row.series][1] AS level1, 
				(a.res)[row.series][2] AS level2, 
				(a.res)[row.series][3] AS level3, 
				(a.res)[row.series][4] AS level4
		  FROM row, a;
		
	WITH a AS ( /- Test data -/ 
		SELECT '{{01,01.015,01.015.016200,01.015.016200.2}
			,{01,01.015,01.015.016200,01.015.016200.3} 
			,{01,01.015,01.015.016200,01.015.016200.4} 
			,{01,01.015,01.015.016900,01.015.016900.1} 
			,{01,01.015,01.015.016900,01.015.016900.2} 
			,{01,01.015,01.015.016900,01.015.016900.3} 
			}'::Text[][] AS res
	), b AS ( /- Test SQL -/
		SELECT level1, level2, level3, level4 
		  FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200') 
		 ORDER BY level4
	), c AS ( /- Convert to 2D array via record -/
		SELECT REPLACE(
					REPLACE(
						REPLACE(
								ARRAY_AGG(b.*)::Text, 
								'"'::Text, ''::Text), 
							'('::Text, '{'::Text), 
						')'::Text, '}'::Text)::Text[][] AS res
		FROM b
	)
	SELECT rif40_sql_pkg._rif40_reduce_dim(c.res) AS missing_data
	  FROM c
	EXCEPT 
	SELECT rif40_sql_pkg._rif40_reduce_dim(a.res)
	  FROM a;

b) TRIGGERS
			
These use INSERT/UPDATE OR DELETE statements. RETURNING is supported, but it must be a single 
text value (test_value). The results array should should also be  single value. Beware that the INSERTed data from the table is not in scope, so you can return a sequence, an input value, but not trigger modified data 

	IF NOT (rif40_sql_pkg.rif40_sql_test(	
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #1'', ''EXTRACT_TRIGGER_TEST_1'', ''MAP_TRIGGER_TEST_1'', 1 /- Disease mapping -/, ''LEVEL1'', ''LEVEL4'', NULL /- FAIL HERE -/, 0)',
		'TRIGGER TEST #1: rif40_studies.denom_tab IS NULL',
		NULL::Text[][] 	/- No results for trigger -/,
		'P0001' 		/- Expected SQLCODE (P0001 - PGpsql raise_exception (from rif40_error) -/, 
		FALSE 			/- Do not RAISE EXCEPTION on failure -/)) THEN
		errors:=errors+1;
    END IF;	 

psql:test_scripts/test_8_triggers.sql:276: WARNING:  rif40_ddl(): SQL in error (P0001)> INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES ('SAHSU', 'TEST', 'TRIGGER TEST #1', 'EXTRACT_TRIGGER_TEST_1', 'MAP_TRIGGER_TEST_1', 1 /- Diease mapping -/, 'LEVEL1', 'LEVEL4', NULL /- FAIL HERE -/, 0);
psql:test_scripts/test_8_triggers.sql:276: WARNING:  71167: rif40_sql_test('TRIGGER TEST #1: rif40_studies.denom_tab IS NULL') caught:
rif40_trg_pkg.trigger_fct_t_rif40_studies_checks(): T_RIF40_STUDIES study 140 denominator:  not found in RIF40_TABLES in SQL >>>
INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES ('SAHSU', 'TEST', 'TRIGGER TEST #1', 'EXTRACT_TRIGGER_TEST_1', 'MAP_TRIGGER_TEST_1', 1 /- Diease mapping -/, 'LEVEL1', 'LEVEL4', NULL /- FAIL HERE -/, 0);
<<<
Error context and message >>>
Message:  rif40_trg_pkg.trigger_fct_t_rif40_studies_checks(): T_RIF40_STUDIES study 140 denominator:  not found in RIF40_TABLES
Hint:     Consult message text
Detail:   -20211
Context:  SQL statement "SELECT rif40_log_pkg.rif40_error(-20211, 'trigger_fct_t_rif40_studies_checks',
                        'T_RIF40_STUDIES study % denominator: % not found in RIF40_TABLES',
                        NEW.study_id::VARCHAR           /- Study id -/,
                        NEW.denom_tab::VARCHAR          /- Denominator -/)"
PL/pgSQL function rif40_trg_pkg.trigger_fct_t_rif40_studies_checks() line 460 at PERFORM
SQL statement "INSERT INTO t_rif40_studies (
                                username,
                                study_id,
                                extract_table,
                                study_name,
                                summary,
                                description,
                                other_notes,
                                study_date,
                                geography,
                                study_type,
                                study_state,
                                comparison_geolevel_name,
                                denom_tab,
                                direct_stand_tab,
                                study_geolevel_name,
                                map_table,
                                suppression_value,
                                extract_permitted,
                                transfer_permitted,
                                authorised_by,
                                authorised_on,
                                authorised_notes,
                                audsid,
                                project)
                        VALUES(
                                coalesce(NEW.username, "current_user"()),
                                coalesce(NEW.study_id, (nextval('rif40_study_id_seq'::regclass))::integer),
                                NEW.extract_table /- no default value -/,
                                NEW.study_name /- no default value -/,
                                NEW.summary /- no default value -/,
                                NEW.description /- no default value -/,
                                NEW.other_notes /- no default value -/,
                                coalesce(NEW.study_date, ('now'::text)::timestamp without time zone),
                                NEW.geography /- no default value -/,
                                NEW.study_type /- no default value -/,
                                coalesce(NEW.study_state, 'C'::character varying),
                                NEW.comparison_geolevel_name /- no default value -/,
                                NEW.denom_tab /- no default value -/,
                                NEW.direct_stand_tab /- no default value -/,
                                NEW.study_geolevel_name /- no default value -/,
                                NEW.map_table /- no default value -/,
                                NEW.suppression_value /- no default value -/,
                                coalesce(NEW.extract_permitted, 0),
                                coalesce(NEW.transfer_permitted, 0),
                                NEW.authorised_by /- no default value -/,
                                NEW.authorised_on /- no default value -/,
                                NEW.authorised_notes /- no default value -/,
                                coalesce(NEW.audsid, sys_context('USERENV'::character varying, 'SESSIONID'::character varying)),
                                NEW.project /- no default value -/)"
PL/pgSQL function rif40_trg_pkg.trgf_rif40_studies() line 8 at SQL statement
SQL statement "INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES ('SAHSU', 'TEST', 'TRIGGER TEST #1', 'EXTRACT_TRIGGER_TEST_1', 'MAP_TRIGGER_TEST_1', 1 /- Diease mapping -/, 'LEVEL1', 'LEVEL4', NULL /- FAIL HERE -/, 0)"
PL/pgSQL function rif40_ddl(character varying) line 51 at EXECUTE statement
SQL statement "SELECT rif40_sql_pkg.rif40_ddl(test_stmt)"
PL/pgSQL function rif40_sql_test(character varying,character varying,anyarray,character varying,boolean) line 253 at PERFORM
PL/pgSQL function inline_code_block line 157 at IF
SQLSTATE: P0001
<<< End of trace.

psql:test_scripts/test_8_triggers.sql:276: WARNING:  rif40_sql_test(): [71169] Test case: TRIGGER TEST #1: rif40_studies.denom_tab IS NULL PASSED, caught expecting SQLSTATE P0001;
Message:  rif40_trg_pkg.trigger_fct_t_rif40_studies_checks(): T_RIF40_STUDIES study 140 denominator:  not found in RIF40_TABLES
Detail:   -20211

 */
DECLARE			  
	d1st CURSOR(l_connection_name VARCHAR, l_sql_stmt VARCHAR) FOR
		SELECT a.rcode
 		  FROM dblink(l_connection_name,
				      l_sql_stmt) AS a(rcode INTEGER);
	d2st CURSOR(l_connection_name VARCHAR) FOR
		WITH a AS (
			SELECT UNNEST(dblink_get_connections()) AS connection_name
		)
		SELECT a.connection_name
		  FROM a
		 WHERE a.connection_name = l_connection_name;
--
	c3_rth CURSOR(l_test_case_title VARCHAR) FOR
		SELECT a.test_case_title, COUNT(test_id) AS total_test_id
		  FROM rif40_test_harness a
		 WHERE a.test_case_title = l_test_case_title
		   AND a.parent_test_id  IS NOT NULL
		 GROUP BY a.test_case_title;	
	c3_rth_rec RECORD;	
	c4_rth CURSOR(l_test_case_title VARCHAR) FOR
		SELECT *
		  FROM rif40_test_harness a
		 WHERE a.test_case_title = l_test_case_title
		   AND a.parent_test_id  IS NOT NULL
		 ORDER BY a.parent_test_id;	
--		 
	d2st_rec RECORD;					  
	d1st_rec	RECORD;
	c4_rth_rec RECORD;	
--		
	rcode 		BOOLEAN;
	f_test_id 	INTEGER;
	sql_stmt	VARCHAR;
	dblink_status	VARCHAR;
	op			VARCHAR;
--
	f_errors	INTEGER:=0;	
	f_tests_run	INTEGER:=0;	
--
	stp TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
--
	v_message_text		VARCHAR;
	v_pg_exception_hint	VARCHAR;
	v_column_name		VARCHAR;
	v_constraint_name	VARCHAR;
	v_pg_datatype_name	VARCHAR;
	v_table_name		VARCHAR;
	v_schema_name		VARCHAR;
--
	error_message VARCHAR;
	v_sqlstate 	VARCHAR;
	v_context	VARCHAR;	
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';		
BEGIN
--
-- Check connection does exist
--
	OPEN d2st(connection_name);
	FETCH d2st INTO d2st_rec;
	CLOSE d2st;
	IF d2st_rec.connection_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-71162, 'rif40_sql_test', 
			'dblink() subtransaction: % does not exist',
			connection_name::VARCHAR);		
	END IF;
	
--
-- Auto register test case
--
	f_test_id:=rif40_sql_pkg._rif40_sql_test_register(test_stmt, connection_name, test_case_title, results, results_xml,
		pg_error_code_expected, raise_exception_on_failure, expected_result);
			
--
-- Do test; reversing effects
--	
	sql_stmt:='SELECT rif40_sql_pkg._rif40_sql_test('||E'\n'||
						coalesce(quote_literal(test_stmt), 'NULL')||'::VARCHAR /* test_stmt */,'||E'\n'||
						coalesce(quote_literal(test_case_title), 'NULL')||'::VARCHAR /* test_case_title */,'||E'\n'||
						coalesce(quote_literal(results), 'NULL')||'::VARCHAR[][] /* results */,'||E'\n'||
						coalesce(quote_literal(results_xml), 'NULL')||'::XML /* results_xml */,'||E'\n'||
						coalesce(quote_literal(pg_error_code_expected), 'NULL')||'::VARCHAR /* pg_error_code_expected */,'||E'\n'||
						coalesce(quote_literal(raise_exception_on_failure), 'NULL')||'::BOOLEAN /* raise_exception_on_failure */,'||E'\n'||
						coalesce(quote_literal(f_test_id), 'NULL')||'::INTEGER /* test_id */)::INTEGER AS rcode';
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71163] SQL[SELECT]> %;', 
			sql_stmt::VARCHAR);						
	BEGIN	
		op='dblink (BEGIN)';		
		PERFORM dblink(connection_name, 'BEGIN;');				
--		
		op='dblink open(SQL)';
		OPEN d1st(connection_name, sql_stmt);
		op='dblink fetch(SQL)';		
		FETCH d1st INTO d1st_rec;
		op='dblink close(SQL)';		
		CLOSE d1st;
--
		rcode:=d1st_rec.rcode;
--
-- Process return code
--
		etp:=clock_timestamp();
		took:=age(etp, stp);
		UPDATE rif40_test_harness
		   SET pass        = rcode,
			   test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer,
			   test_date   = statement_timestamp(),
			   time_taken  = EXTRACT(EPOCH FROM took)::NUMERIC
		 WHERE test_id = f_test_id;
		IF rcode THEN
			UPDATE rif40_test_runs
			   SET number_passed = number_passed + 1,
				   tests_run     = tests_run + 1
			 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
		ELSE
			UPDATE rif40_test_runs
			   SET number_failed = number_failed + 1,
				   tests_run     = tests_run + 1
			 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
		END IF;			
--
-- Do dependent tests
--
		OPEN c3_rth(test_case_title);
		FETCH c3_rth INTO c3_rth_rec;
		CLOSE c3_rth;
		IF c3_rth_rec.total_test_id = 0 THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71207] Test % no dependent tests to run',
				c3_rth_rec.test_case_title::VARCHAR);
		ELSIF NOT rcode THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
				'[71208a] Test % % dependent tests to run; halting because of error in master test',
				c3_rth_rec.test_case_title::VARCHAR, c3_rth_rec.total_test_id::VARCHAR);		
			RETURN rcode;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
				'[71208] Test % % dependent tests to run',
				c3_rth_rec.test_case_title::VARCHAR, c3_rth_rec.total_test_id::VARCHAR);	
--	
-- Causes:
--		
-- 2: notice: _rif40_sql_test(): [71187] Test case: SAHSULAND test 4 study_id 1 example FAILED, no error expected; got: 42P03;
-- Message:  cursor "c4_rth" already in use
-- Detail:
--		
-- Solution - move code to Node.js trest harness - also can then update
--
			FOR c4_rth_rec IN c4_rth(c3_rth_rec.test_case_title) LOOP
				f_tests_run:=f_tests_run+1;	
--
-- 	test_stmt 					VARCHAR, 
--	test_case_title 			VARCHAR, 
--	results 					ANYARRAY, 
--	results_xml					XML,
--	pg_error_code_expected 		VARCHAR, 
--	raise_exception_on_failure 	BOOLEAN, 
--	f_test_id 					INTEGER
--
				PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
					'[71208a] Dependent %/% test: %',
					f_tests_run::VARCHAR, 
					c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);
				sql_stmt:='SELECT rif40_sql_pkg._rif40_sql_test('||E'\n'||
									coalesce(quote_literal(c4_rth_rec.test_stmt), 'NULL')||'::VARCHAR /* test_stmt */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.test_case_title), 'NULL')||'::VARCHAR /* test_case_title */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.results), 'NULL')||'::VARCHAR[][] /* results */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.results_xml), 'NULL')||'::XML /* results_xml */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.pg_error_code_expected), 'NULL')||'::VARCHAR /* pg_error_code_expected */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.raise_exception_on_failure), 'NULL')||'::BOOLEAN /* raise_exception_on_failure */,'||E'\n'||
									coalesce(quote_literal(c4_rth_rec.test_id), 'NULL')||'::INTEGER /* test_id */)::INTEGER AS rcode';
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_sql_test', '[71163a] SQL[SELECT] %/%> %;',
						f_tests_run::VARCHAR, 
						c3_rth_rec.total_test_id::VARCHAR, 
						sql_stmt::VARCHAR);	
--		
				op='dblink open(SQL) ['||f_tests_run::VARCHAR||'/'||c3_rth_rec.total_test_id::VARCHAR||']';
				OPEN d1st(connection_name, sql_stmt);
				op='dblink fetch(SQL) ['||f_tests_run::VARCHAR||'/'||c3_rth_rec.total_test_id::VARCHAR||']';		
				FETCH d1st INTO d1st_rec;
				op='dblink close(SQL) ['||f_tests_run::VARCHAR||'/'||c3_rth_rec.total_test_id::VARCHAR||']';		
				CLOSE d1st;
--
-- Process return code
--
				etp:=clock_timestamp();
				took:=age(etp, stp);
				UPDATE rif40_test_harness
				   SET pass        = d1st_rec.rcode,
					   test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer,
					   test_date   = statement_timestamp(),
					   time_taken  = EXTRACT(EPOCH FROM took)::NUMERIC
				 WHERE test_id = c4_rth_rec.test_id;
				IF d1st_rec.rcode THEN
					UPDATE rif40_test_runs
					   SET number_passed = number_passed + 1,
						   tests_run     = tests_run + 1
					 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
				ELSE
					UPDATE rif40_test_runs
					   SET number_failed = number_failed + 1,
						   tests_run     = tests_run + 1
					 WHERE test_run_id = (currval('rif40_test_run_id_seq'::regclass))::integer;
				END IF;							
				IF NOT d1st_rec.rcode THEN 	/* Test failed */
					IF NOT c4_rth_rec.expected_result THEN
						PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
							'[71209] TEST %/%: % FAILED AS EXPECTED.',
							f_tests_run::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);	
					ELSE
						PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
							'[71210] TEST %/%: % FAILED; EXPECTED TO PASS.',
							f_tests_run::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);	
						f_errors:=f_errors+1; 				
					END IF;
				ELSE					/* Passed */
					IF NOT c4_rth_rec.expected_result THEN		
						PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
							'[71211] TEST %/%: % FAILED; EXPECTED TO PASS.',
							f_tests_run::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);
						f_errors:=f_errors+1; 									
					ELSE		
						PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
							'[71212] TEST %/%: % PASSED AS EXPECTED.',
							f_tests_run::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, c4_rth_rec.test_case_title::VARCHAR);	
					END IF;
				END IF;									
			END LOOP;
			IF f_errors > 0 THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_test', 
					'[71212a] Test % % dependent tests ran, % failed',
					c3_rth_rec.test_case_title::VARCHAR, c3_rth_rec.total_test_id::VARCHAR, f_errors::VARCHAR);	
					rcode:=FALSE;			
			ELSE
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 		
					'[71212b] Test % % dependent tests ran OK',
					c3_rth_rec.test_case_title::VARCHAR, c3_rth_rec.total_test_id::VARCHAR);		
			END IF;
		END IF;			
--		
		op='dblink (ROLLBACK)';		
		PERFORM dblink(connection_name, 'ROLLBACK;');			
	EXCEPTION	
		WHEN others THEN
-- 
-- Not supported until 9.2
--
			GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
			GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
			GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
			GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
			GET STACKED DIAGNOSTICS v_pg_exception_hint = PG_EXCEPTION_HINT;
			
			GET STACKED DIAGNOSTICS v_column_name       = COLUMN_NAME;
			GET STACKED DIAGNOSTICS v_constraint_name   = CONSTRAINT_NAME;
			GET STACKED DIAGNOSTICS v_pg_datatype_name  = PG_DATATYPE_NAME;
			GET STACKED DIAGNOSTICS v_table_name        = TABLE_NAME;
			GET STACKED DIAGNOSTICS v_schema_name       = SCHEMA_NAME;
			
			error_message:='rif40_sql_test('''||coalesce(test_case_title::VARCHAR, '')||''') caught: '||E'\n'||SQLERRM::VARCHAR||
				' in SQL >>>'||E'\n'||test_stmt||';'||E'\n'||
				'<<<'||E'\n'||
				' in '||op||' SQL >>>'||E'\n'||COALESCE(sql_stmt, 'NULL')||';'||E'\n'||
				'<<<'||E'\n'||				
				'Error context and message >>>'||E'\n'||			
				'Message:  '||v_message_text::VARCHAR||E'\n'||
				'Hint:     '||v_pg_exception_hint::VARCHAR||E'\n'||
				'Detail:   '||v_detail::VARCHAR||E'\n'||
				'Context:  '||v_context::VARCHAR||E'\n'||
				'SQLSTATE: '||v_sqlstate::VARCHAR||E'\n'||'<<< End of trace.'||E'\n';
--
			PERFORM rif40_log_pkg.rif40_error(-71164, 'rif40_sql_test', 
				'Test case: % FAILED, error in dblink_open(); link: %: %', 
				test_case_title::VARCHAR,
				connection_name::VARCHAR,
				error_message::VARCHAR);
			RAISE;
	END;				

	RETURN rcode;
END;
$func$ LANGUAGE plpgsql;
			
COMMENT ON FUNCTION rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, BOOLEAN) IS 'Function: 	rif40_sql_test()
Parameters:	Connection name.
			SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title, 
			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, 
			raise exception on failure,
			expected result [has NO effect on the return code; for test harness]   
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

			Used to check test SQL statements and triggers';
--
-- So can be used to test non rif user access to functions 
--
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_sql_test(VARCHAR, VARCHAR, VARCHAR, ANYARRAY, XML, VARCHAR, BOOLEAN, BOOLEAN) TO PUBLIC;

			
CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_sql_test(
	test_stmt 					VARCHAR, 
	test_case_title 			VARCHAR, 
	results 					ANYARRAY, 
	results_xml					XML,
	pg_error_code_expected 		VARCHAR, 
	raise_exception_on_failure 	BOOLEAN, 
	f_test_id 					INTEGER, 
	pg_debug_functions			Text[] DEFAULT NULL)
RETURNS boolean
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_sql_test()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title,
 			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first 
				negative number in the message is assumed to be the number; 
				NULL means it is expected to NOT raise an exception, 
			raise exception on failure,
			test_id,
			Array of Postgres functions for test harness to enable debug on
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE
Description:	
*/
DECLARE
--
	c1sqlt 				REFCURSOR;
	c1sqlt_result_row 	RECORD;
	c2sqlt 				REFCURSOR;
	c2sqlt_result_row 	RECORD;
--	
	sql_frag 	VARCHAR;
	sql_stmt 	VARCHAR;
--
	extra		INTEGER:=0;
	missing		INTEGER:=0;	
--
	f_pass		BOOLEAN;
	f_result	BOOLEAN;
--
	l_pg_debug_function VARCHAR;
--
	v_message_text		VARCHAR;
	v_pg_exception_hint	VARCHAR;
	v_column_name		VARCHAR;
	v_constraint_name	VARCHAR;
	v_pg_datatype_name	VARCHAR;
	v_table_name		VARCHAR;
	v_schema_name		VARCHAR;
--
	error_message VARCHAR;
	v_sqlstate 	VARCHAR;
	v_context	VARCHAR;	
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN	
--
-- Add test specific debug messages
--
	IF pg_debug_functions IS NOT NULL THEN
		FOREACH l_pg_debug_function IN ARRAY pg_debug_functions LOOP
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
				'[71158a]: Enable debug for function: %', 
				l_pg_debug_function::VARCHAR);
			PERFORM rif40_log_pkg.rif40_add_to_debug(l_pg_debug_function||':DEBUG1');		
		END LOOP;
	END IF;
	
--
-- Do test
--
	IF UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 6)) = 'SELECT' THEN
		PERFORM rif40_sql_pkg.rif40_method4(test_stmt, test_case_title);
--
		sql_frag:='WITH a AS ( /* Test data */'||E'\n'||
'	SELECT '''||
		results::Text||
		'''::Text[][] AS res'||E'\n'||
'), b AS ( /* Test SQL */'||E'\n'||
	test_stmt||E'\n'||
'), c AS ( /* Convert to 2D array via record */'||E'\n'||
'	SELECT REPLACE('||E'\n'||
'				REPLACE('||E'\n'||
'					REPLACE('||E'\n'||
'							ARRAY_AGG(b.*)::Text, '||E'\n'||
'							''"''::Text, ''''::Text),'||E'\n'|| 
'						''(''::Text, ''{''::Text),'||E'\n'|| 
'					'')''::Text, ''}''::Text)::Text[][] AS res'||E'\n'||
'	FROM b'||E'\n'||
')';
--
-- Check for extra data
--
		sql_stmt:=sql_frag||E'\n'||
'SELECT rif40_sql_pkg._rif40_reduce_dim(c.res) AS extra_data'||E'\n'||
'  FROM c /* Test SQL data */'||E'\n'||
'EXCEPT'||E'\n'|| 
'SELECT rif40_sql_pkg._rif40_reduce_dim(a.res)'||E'\n'||
'  FROM a /* Test data */';
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_sql_test', '[71165] SQL[SELECT]> %;', 
			sql_stmt::VARCHAR);
		OPEN c1sqlt FOR EXECUTE sql_stmt;
		LOOP
			FETCH c1sqlt INTO c1sqlt_result_row;
			IF NOT FOUND THEN EXIT; END IF;
--
			extra:=extra+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71166] Test case %: % extra[%]: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, c1sqlt_result_row.extra_data::VARCHAR);		
		END LOOP;
		CLOSE c1sqlt;
		IF extra = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
				'[71167] PASSED: test case %: no extra rows for test: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71168] FAILED: test case %: % extra rows for test: %', 
				f_test_id::VARCHAR, extra::VARCHAR, test_case_title::VARCHAR);	
		END IF;
--
		sql_stmt:=sql_frag||E'\n'||
'SELECT rif40_sql_pkg._rif40_reduce_dim(a.res) AS missing_data'||E'\n'||
'  FROM a /* Test data */'||E'\n'||
'EXCEPT'||E'\n'|| 
'SELECT rif40_sql_pkg._rif40_reduce_dim(c.res)'||E'\n'||
'  FROM c /* Test SQL data */';
		OPEN c2sqlt FOR EXECUTE sql_stmt;
		LOOP
			FETCH c2sqlt INTO c2sqlt_result_row;
			IF NOT FOUND THEN EXIT; END IF;
--
			missing:=missing+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71169] Test case %: % missing[%]: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, c2sqlt_result_row.missing_data::VARCHAR);
		END LOOP;
		CLOSE c2sqlt;
		
--
-- Check for missing
--
		IF missing = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
				'[71170] PASSED: no missing rows for test case %: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71171] FAILED: Test case %: % % missing rows for test: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, test_case_title::VARCHAR);	
		END IF;
--		
		IF pg_error_code_expected IS NOT NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71172] FAILED: Test case %: % no exception, expected SQLSTATE: %', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, pg_error_code_expected::VARCHAR);				
			f_pass:=FALSE;	
		ELSIF extra = 0 AND missing = 0 THEN 
			PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
				'[71173] PASSED: Test case %: % no exceptions, no errors, no missing or extra data', 
				f_test_id::VARCHAR, test_case_title::VARCHAR);			
			f_pass:=TRUE;
		ELSIF raise_exception_on_failure THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71174] FAILED: Test case %: % % missing, % extra', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, extra::VARCHAR);		
			RAISE no_data_found;
		ELSE /* Just failed above */
			PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
				'[71175] FAILED: Test case %: % % missing, % extra', 
				f_test_id::VARCHAR, test_case_title::VARCHAR, missing::VARCHAR, extra::VARCHAR);			
			f_pass:=FALSE;	
		END IF;
--
-- Other SQL test statements (i.e. INSERT/UPDATE/DELETE for triggers) with RETURNING clause
--
	ELSIF UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 6)) IN ('INSERT', 'UPDATE', 'DELETE') THEN
		IF results IS NOT NULL THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_sql_test', '[71176] SQL[INSERT/UPDATE/DELETE; RETURNING]> %;', 
				test_stmt::VARCHAR);	
			OPEN c1sqlt FOR EXECUTE test_stmt;
			FETCH c1sqlt INTO c1sqlt_result_row;
			CLOSE c1sqlt;
		
--
-- Check for errors (or rather the lack of them)
--		
			IF pg_error_code_expected IS NOT NULL THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71177] FAILED: Test case %: % no exception, expected SQLSTATE: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, pg_error_code_expected::VARCHAR);				
				f_pass:=FALSE;	
			ELSIF c1sqlt_result_row.test_value = results[1] THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
					'[71178] PASSED: Test case %: % no exceptions, no errors, return value as expected: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, results[1]::VARCHAR);			
				f_pass:=TRUE;
			ELSIF raise_exception_on_failure THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71179] FAILED: Test case %: % got: %, expected: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, c1sqlt_result_row.test_value::VARCHAR, results[1]::VARCHAR);			
				RAISE no_data_found;
			ELSE /* Value test failed */
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71180] FAILED: Test case %: % got: %, expected: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, c1sqlt_result_row.test_value::VARCHAR, results[1]::VARCHAR);			
				f_pass:=FALSE;
			END IF;
		
--
-- Other SQL test statements (i.e. INSERT/UPDATE/DELETE for triggers)
--
		ELSE
			PERFORM rif40_sql_pkg.rif40_ddl(test_stmt);
				
--
-- Check for errors (or rather the lack of them)
--			
			IF pg_error_code_expected IS NULL THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
					'[71181] PASSED: Test case %: % no exceptions, no error expected', 
					f_test_id::VARCHAR, test_case_title::VARCHAR);		
				f_pass:=TRUE;
			ELSIF raise_exception_on_failure THEN		
				RAISE no_data_found;			
			ELSE
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71182] FAILED: Test case %: % no exceptions, expected SQLSTATE: %', 
					f_test_id::VARCHAR, test_case_title::VARCHAR, pg_error_code_expected::VARCHAR);		
				f_pass:=FALSE;		
			END IF;
		END IF;
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-71183, '_rif40_sql_test', 
			'Test case %: % FAILED, invalid statement type: % %SQL> %;', 
			f_test_id::VARCHAR, test_case_title::VARCHAR, UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 6))::VARCHAR, E'\n'::VARCHAR, test_stmt::VARCHAR);	
	END IF;
--
-- Remove test specific debug messages
--
	IF pg_debug_functions IS NOT NULL THEN
		FOREACH l_pg_debug_function IN ARRAY pg_debug_functions LOOP
			PERFORM rif40_log_pkg.rif40_remove_from_debug(l_pg_debug_function);		
		END LOOP;
	END IF;	
--
	RETURN f_pass;
--
EXCEPTION
	WHEN no_data_found THEN	
		IF pg_error_code_expected IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-71184, '_rif40_sql_test', 
				'Test case: % FAILED, % errors', 
				test_case_title::VARCHAR, (extra+missing)::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-71185, '_rif40_sql_test', 
				'Test case: % FAILED, % errors; expecting SQLSTATE: %; not thrown', 
				test_case_title::VARCHAR, (extra+missing)::VARCHAR, pg_error_code_expected::VARCHAR);	
		END IF;
/*
RETURNED_SQLSTATE		the SQLSTATE error code of the exception
COLUMN_NAME				the name of column related to exception
CONSTRAINT_NAME			the name of constraint related to exception
PG_DATATYPE_NAME		the name of datatype related to exception
MESSAGE_TEXT			the text of the exception's primary message
TABLE_NAME				the name of table related to exception
SCHEMA_NAME				the name of schema related to exception
PG_EXCEPTION_DETAIL		the text of the exception's detail message, if any
PG_EXCEPTION_HINT		the text of the exception's hint message, if any
PG_EXCEPTION_CONTEXT	line(s) of text describing the call stack
 */			
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail            = PG_EXCEPTION_DETAIL;
		GET STACKED DIAGNOSTICS v_sqlstate          = RETURNED_SQLSTATE;
		GET STACKED DIAGNOSTICS v_context           = PG_EXCEPTION_CONTEXT;
		GET STACKED DIAGNOSTICS v_message_text      = MESSAGE_TEXT;
		GET STACKED DIAGNOSTICS v_pg_exception_hint = PG_EXCEPTION_HINT;
		
		GET STACKED DIAGNOSTICS v_column_name       = COLUMN_NAME;
		GET STACKED DIAGNOSTICS v_constraint_name   = CONSTRAINT_NAME;
		GET STACKED DIAGNOSTICS v_pg_datatype_name  = PG_DATATYPE_NAME;
		GET STACKED DIAGNOSTICS v_table_name        = TABLE_NAME;
		GET STACKED DIAGNOSTICS v_schema_name       = SCHEMA_NAME;
		
		error_message:='_rif40_sql_test('''||coalesce(test_case_title::VARCHAR, '')||''') caught: '||E'\n'||SQLERRM::VARCHAR||
			' in SQL >>>'||E'\n'||test_stmt||';'||E'\n'||
			'<<<'||E'\n'||'Error context and message >>>'||E'\n'||			
			'Message:  '||v_message_text::VARCHAR||E'\n'||
			'Hint:     '||v_pg_exception_hint::VARCHAR||E'\n'||
			'Detail:   '||v_detail::VARCHAR||E'\n'||
			'Context:  '||v_context::VARCHAR||E'\n'||
			'SQLSTATE: '||v_sqlstate::VARCHAR||E'\n'||'<<< End of trace.'||E'\n';
		IF COALESCE(v_schema_name, '') != '' AND COALESCE(v_table_name, '') != '' THEN
			error_message:=error_message||'Object: '||v_schema_name||'.'||v_table_name;	
		END IF;
		IF COALESCE(v_column_name, '') != '' THEN
			error_message:=error_message||'.'||v_column_name;	
		END IF;
		IF COALESCE(v_pg_datatype_name, '') != '' THEN
			error_message:=error_message||'; datatype: '||v_pg_datatype_name;	
		END IF;
		IF COALESCE(v_constraint_name, '') != '' THEN
			error_message:=error_message||'; constraint: '||v_constraint_name;	
		END IF;			
		RAISE WARNING '71186: %', error_message;
--
-- Check error SQLSTATE
--
		BEGIN
			IF pg_error_code_expected IS NULL THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71187] Test case: % FAILED, no error expected; got: %;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);		
				IF raise_exception_on_failure THEN
					RAISE;
				ELSE					
					RETURN FALSE;
				END IF;		
			ELSIF v_sqlstate = 'P0001' AND pg_error_code_expected = v_detail THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
					'[71188] Test case: % PASSED, caught expecting SQLSTATE/RIF error code: %/%;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR, pg_error_code_expected::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);								
				RETURN TRUE;				
			ELSIF pg_error_code_expected = v_sqlstate THEN
				PERFORM rif40_log_pkg.rif40_log('INFO', '_rif40_sql_test', 
					'[71189] Test case: % PASSED, caught expecting SQLSTATE %;%', 
					test_case_title::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);							
				RETURN TRUE;			
			ELSE	
				PERFORM rif40_log_pkg.rif40_log('WARNING', '_rif40_sql_test', 
					'[71190] Test case: % FAILED, expecting SQLSTATE %; got: %;%', 
					test_case_title::VARCHAR, pg_error_code_expected::VARCHAR, v_sqlstate::VARCHAR,
					E'\n'||'Message:  '||v_message_text::VARCHAR||E'\n'||
					'Detail:   '||v_detail::VARCHAR);		
				IF raise_exception_on_failure THEN
					RAISE;
				ELSE							
					RETURN FALSE;
				END IF;
			END IF;
		EXCEPTION
			WHEN others THEN			
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
				GET STACKED DIAGNOSTICS v_context = PG_EXCEPTION_CONTEXT;
				error_message:='rif40_sql_test() exception handler caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||
					'Detail: '||v_detail::VARCHAR||E'\n'||
					'Context: '||v_context::VARCHAR||E'\n'||
					'SQLSTATE: '||v_sqlstate::VARCHAR;
				RAISE EXCEPTION '71191: %', error_message USING DETAIL=v_detail;			
		END;
END;
$func$ LANGUAGE plpgsql;
	
COMMENT ON FUNCTION rif40_sql_pkg._rif40_sql_test(VARCHAR, VARCHAR, ANYARRAY, XML,
	VARCHAR, BOOLEAN, INTEGER, Text[]) IS 'Function: 	_rif40_sql_test()
Parameters:	SQL test (SELECT of INSERT/UPDATE/DELETE with RETURNING clause) statement, 
            test case title,
 			results 3d text array,
			results as XML,
			[negative] error SQLSTATE expected [as part of an exception]; the first 
			negative number in the message is assumed to be the number; 
			NULL means it is expected to NOT raise an exception, raise exception on failure,
			test_id,
			Array of Postgres functions for test harness to enable debug on
Returns:	Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE
Description:	Log and execute SQL Dynamic SQL method 4 (Oracle name) SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause

			Used to check test SQL statements and triggers';
	
--
-- Eof
	
