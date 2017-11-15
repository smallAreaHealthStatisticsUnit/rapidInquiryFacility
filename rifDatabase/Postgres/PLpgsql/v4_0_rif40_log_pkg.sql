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
-- Rapid Enquiry Facility (RIF) - PG psql code (logging, auditing and debug)
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

\echo Building rif40_log_pkg (logging, auditing and debug) package...

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C209xx: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

\set ON_ERROR_STOP OFF
DROP TYPE IF EXISTS rif40_log_pkg.rif40_log_debug_level CASCADE;
DROP TYPE IF EXISTS rif40_log_pkg.rif40_debug_record CASCADE;

\set ON_ERROR_STOP ON
CREATE TYPE rif40_log_pkg.rif40_log_debug_level AS ENUM ('WARNING', 'INFO', 'DEBUG1', 'DEBUG2', 'DEBUG3', 'DEBUG4');
CREATE TYPE rif40_log_pkg.rif40_debug_record AS (function_name VARCHAR, debug RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL);

COMMENT ON TYPE rif40_log_pkg.rif40_log_debug_level IS 'Error/debug/info level enumerated datatype';
COMMENT ON TYPE rif40_log_pkg.rif40_debug_record IS 'Function name, RIF40_LOG_DEBUG_LEVEL struct';

CREATE OR REPLACE FUNCTION rif40_log_pkg.rif40_log_setup()
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_log_setup()
Parameters:	None
Returns:	Nothing
Description:	Print log setup
 */
DECLARE
	c1lgs CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.send_debug_to_info') AS send_debug_to_info, 
		       CURRENT_SETTING('rif40.debug') AS debug;
--
	c1lgs_rec RECORD;
BEGIN
	OPEN c1lgs;
	FETCH c1lgs INTO c1lgs_rec;
	CLOSE c1lgs;
--
	RAISE INFO 'rif40_log_setup() send DEBUG to INFO: %; debug function list: [%]', 
		c1lgs_rec.send_debug_to_info, c1lgs_rec.debug;
EXCEPTION
	WHEN others THEN /* Try defaulting settings */
		SET rif40.debug = '';
		SET rif40.send_debug_to_info = 'off';
--
		OPEN c1lgs;
		FETCH c1lgs INTO c1lgs_rec;
		CLOSE c1lgs;
--
	RAISE INFO 'rif40_log_setup() DEFAULTED send DEBUG to INFO: %; debug function list: [%]', 
		c1lgs_rec.send_debug_to_info, c1lgs_rec.debug;

END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_log_pkg.rif40_log_setup() IS 'Function: 	rif40_log_setup()
Parameters:	None
Returns:	Nothing
Description:	Print log setup';

\df rif40_log_pkg.rif40_log_setup

CREATE OR REPLACE FUNCTION rif40_log_pkg.rif40_send_debug_to_info(enable BOOLEAN)
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_send_debug_to_info()
Parameters:	Enable (BOOLEAN)
Returns:	Nothing
Description:	Set/unset rif40.send_debug_to_info
 */
DECLARE
	c1d2i CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.send_debug_to_info') AS send_debug_to_info, 
		       CURRENT_SETTING('rif40.debug') AS debug;
--
	c1d2i_rec RECORD;
--
	sql_stmt VARCHAR;
BEGIN
	BEGIN
		OPEN c1d2i;
		FETCH c1d2i INTO c1d2i_rec;
		CLOSE c1d2i;
	EXCEPTION
		WHEN others THEN /* Try defaulting settings */
			SET rif40.debug = '';
			SET rif40.send_debug_to_info = 'off';
--
			OPEN c1d2i;
			FETCH c1d2i INTO c1d2i_rec;
			CLOSE c1d2i;
--
		RAISE INFO 'rif40_send_debug_to_info() DEFAULTED send DEBUG to INFO: %; debug function list: [%]', 
			c1d2i_rec.send_debug_to_info::VARCHAR, c1d2i_rec.debug::VARCHAR;
	END;
--
	RAISE INFO 'rif40_send_debug_to_info(%) SET send DEBUG to INFO: %; debug function list: [%]', 
		enable::VARCHAR, c1d2i_rec.send_debug_to_info::VARCHAR, c1d2i_rec.debug::VARCHAR;
--
	IF enable THEN
		sql_stmt:='SET rif40.send_debug_to_info = ''on''';
	ELSE
		sql_stmt:='SET rif40.send_debug_to_info = ''off''';
	END IF;
	IF rif40_log_pkg.rif40_is_debug_enabled('rif40_send_debug_to_info', 'DEBUG4') AND (enable OR c1d2i_rec.send_debug_to_info = 'on') THEN
		RAISE INFO 'SQL> %;', sql_stmt;
	ELSIF rif40_log_pkg.rif40_is_debug_enabled('rif40_send_debug_to_info', 'DEBUG4') THEN
		RAISE DEBUG 'SQL> %;', sql_stmt;
	END IF;
	EXECUTE sql_stmt;
--	
	OPEN c1d2i;
	FETCH c1d2i INTO c1d2i_rec;
	CLOSE c1d2i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_log_pkg.rif40_send_debug_to_info(BOOLEAN) IS 'Function: 	rif40_send_debug_to_info()
Parameters:	Enable (BOOLEAN)
Returns:	Nothing
Description:	Set/unset rif40.send_debug_to_info';

\df rif40_log_pkg.rif40_send_debug_to_info

CREATE OR REPLACE FUNCTION rif40_log_pkg.rif40_is_debug_enabled(function_name VARCHAR, debug RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_is_debug_enabled()
Parameters:	Debug string pairs <function name><debug level>, <function name><debug level>, ...
Returns:	Boolean (TRUE/FALSE)
Description:
		Function name is the name of a valid RIF40 function owned by RIF40 with NO schema component
		Debug level is one of: 'DEBUG1', 'DEBUG2', 'DEBUG3', 'DEBUG4'
 */
DECLARE
	c2ide CURSOR(l_function_name VARCHAR) FOR 
		SELECT l.lanname||' function' object_type, 
			COALESCE(n.nspname, r.rolname)||'.'||p.proname object_name 					/* Functions */
		  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
			LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
		 WHERE p.prolang    = l.oid
		   AND p.prorettype = t.oid
		   AND p.proowner   IN (SELECT oid FROM pg_roles WHERE rolname IN ('postgres', 'rif40'))
		   AND p.proowner   = r.oid
		   AND p.proname    = LOWER(l_function_name);	
	c3ide CURSOR(l_function_name VARCHAR) FOR 
		WITH a AS (
			SELECT rif40_log_pkg.rif40_get_debug(CURRENT_SETTING('rif40.debug')) AS debug 	/* Current debug */
		)
		SELECT (a.debug).function_name AS function_name,
  		       (a.debug).debug AS current_debug,
		       SUBSTR((a.debug).debug::TEXT, 6)::INTEGER AS current_debug_level
		  FROM a
		 WHERE (a.debug).function_name = l_function_name					/* Remove function */
		ORDER BY 1;
--
	c2ide_rec RECORD;
	c3ide_rec RECORD;
--
	debug_level INTEGER;
	ret BOOLEAN:=FALSE;
BEGIN
--
-- Check for NULL
--
	IF function_name IS NULL OR debug IS NULL THEN
		RETURN ret;
	END IF;
--
-- Check function name exists
--
	OPEN c2ide(function_name);
	FETCH c2ide INTO c2ide_rec;
	CLOSE c2ide;
	IF c2ide_rec.object_name IS NULL THEN
		RAISE WARNING 'rif40_is_debug_enabled() [99901]: function name %() NOT FOUND/NOT EXECUTABLE by user.', function_name;
--
-- Check debug level
--
	ELSIF debug NOT IN ('DEBUG1', 'DEBUG2', 'DEBUG3', 'DEBUG4') THEN
		RAISE WARNING 'rif40_is_debug_enabled() [99902]: function %() invalid <debug level> % to RIF40_LOG_PKG.RIF40_DEBUG_RECORD', 
		c2ide_rec.object_name, debug;
	ELSE
		debug_level:=SUBSTR(debug::TEXT, 6)::INTEGER;
		OPEN c3ide(function_name);
		FETCH c3ide INTO c3ide_rec;
		CLOSE c3ide;
		IF debug_level <= c3ide_rec.current_debug_level THEN
			ret:=TRUE;
		END IF;
	END IF;
--
-- Do not call rif40_is_debug_enabled() or you will recurse in an undivine manner
--
--	RAISE INFO 'rif40_is_debug_enabled() function %() debug_level (%); c3ide_rec.current_debug_level (%) - %',
--		function_name, debug_level, c3ide_rec.current_debug_level, ret;
--
	RETURN ret;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_log_pkg.rif40_is_debug_enabled(VARCHAR, RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL) IS 'Function: 	rif40_is_debug_enabled()
Parameters:	Debug string pairs <function name><debug level>, <function name><debug level>, ...
Returns:	Boolean (TRUE/FALSE)
Description:
		Function name is the name of a valid RIF40 function owned by RIF40 with NO schema component
		Debug level is one of: ''DEBUG1'', ''DEBUG2'', ''DEBUG3'', ''DEBUG4''';

\df rif40_log_pkg.rif40_is_debug_enabled

CREATE OR REPLACE FUNCTION rif40_log_pkg.rif40_remove_from_debug(function_name VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_remove_from_debug()
Parameters:	function name
Returns:	Nothing
Description:	Remove function name from debug string. 
		Debug string example 'rif40_log:DEBUG4', 'rif40_error:DEBUG4'

		Function name is the name of a valid RIF40 function owned by RIF40 with NO schema component

 */
DECLARE
	c1r4d CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.send_debug_to_info') AS send_debug_to_info, 
		       CURRENT_SETTING('rif40.debug') AS debug;
	c2r4d CURSOR(l_function_name VARCHAR) FOR 
		WITH a AS (
			SELECT rif40_log_pkg.rif40_get_debug(CURRENT_SETTING('rif40.debug')) AS debug 	/* Current debug */
		)
		SELECT (a.debug).function_name AS function_name,
  		       (a.debug).debug AS old_debug,
  		       (a.debug).debug AS new_debug,
		       SUBSTR((a.debug).debug::TEXT, 6)::INTEGER AS old_debug_level,
		       SUBSTR((a.debug).debug::TEXT, 6)::INTEGER AS new_debug_level
		  FROM a
		 WHERE (a.debug).function_name != l_function_name					/* Remove function */
		ORDER BY 1;
	c3r4d CURSOR(l_function_name VARCHAR) FOR 
		SELECT l.lanname||' function' object_type, 
			COALESCE(n.nspname, r.rolname)||'.'||p.proname object_name 					/* Functions */
		  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
			LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
		 WHERE p.prolang    = l.oid
		   AND p.prorettype = t.oid
		   AND p.proowner   = (SELECT oid FROM pg_roles WHERE rolname = 'rif40')
		   AND p.proowner   = r.oid
		   AND p.proname    = LOWER(l_function_name);	
--
	c1r4da_rec RECORD;
	c1r4db_rec RECORD;
	c2r4d_rec RECORD;
	c3r4d_rec RECORD;
--
	sql_stmt VARCHAR;
	i INTEGER:=0;
--
	is_debug_enabled BOOLEAN;
BEGIN
--
-- Check for NULL
--
	IF function_name IS NULL THEN
		RETURN;
	END IF;
--
-- Validate function name
--
	OPEN c3r4d(function_name);	
	FETCH c3r4d INTO c3r4d_rec;
	CLOSE c3r4d;
	IF c3r4d_rec.object_name IS NULL THEN
		RAISE WARNING 'rif40_remove_from_debug() [99903]: function name %() NOT FOUND/NOT EXECUTABLE by user.', function_name;
		RETURN;
	END IF;
--
	OPEN c1r4d;	/* Before */
	FETCH c1r4d INTO c1r4da_rec;
	CLOSE c1r4d;
	
	is_debug_enabled:=rif40_log_pkg.rif40_is_debug_enabled('rif40_remove_from_debug', 'DEBUG4');
--
-- Re-process debug string
--
	sql_stmt:='SET rif40.debug = ''';
	FOR c2r4d_rec IN c2r4d(function_name) LOOP
		i:=i+1;
		IF is_debug_enabled AND c1r4da_rec.send_debug_to_info = 'on' THEN
			RAISE INFO 'rif40_remove_from_debug() Function %() % to %', c2r4d_rec.function_name, c2r4d_rec.old_debug_level, c2r4d_rec.new_debug_level;
		ELSIF is_debug_enabled THEN
			RAISE DEBUG 'rif40_remove_from_debug() Function %() % to %', c2r4d_rec.function_name, c2r4d_rec.old_debug_level, c2r4d_rec.new_debug_level;
		END IF;
		IF i = 1 THEN
			sql_stmt:=sql_stmt||c2r4d_rec.function_name||':'||c2r4d_rec.new_debug;
		ELSE
			sql_stmt:=sql_stmt||', '||c2r4d_rec.function_name||':'||c2r4d_rec.new_debug;
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||'''';
--
	IF i > 0 THEN
		IF is_debug_enabled AND c1r4da_rec.send_debug_to_info = 'on' THEN
			RAISE INFO 'SQL> %;', sql_stmt;
		ELSIF is_debug_enabled THEN
			RAISE DEBUG 'SQL> %;', sql_stmt;
		END IF;
		EXECUTE sql_stmt;
	END IF;
--
	OPEN c1r4d;	/* After */
	FETCH c1r4d INTO c1r4db_rec;
	CLOSE c1r4d;
--
	IF is_debug_enabled AND c1r4db_rec.send_debug_to_info = 'on' THEN
		RAISE INFO 'rif40_remove_from_debug() SET DEBUG from: "%" to: "%"', c1r4da_rec.debug, c1r4db_rec.debug;
	ELSIF is_debug_enabled THEN
		RAISE DEBUG 'rif40_remove_from_debug() SET DEBUG from: "%" to: "%"', c1r4da_rec.debug, c1r4db_rec.debug;
	END IF;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_log_pkg.rif40_remove_from_debug(VARCHAR) IS 'Function: 	rif40_remove_from_debug()
Parameters:	function name
Returns:	Nothing
Description:	Remove function name from debug string. 
		Debug string example ''rif40_log:DEBUG4'', ''rif40_error:DEBUG4''

		Function name is the name of a valid RIF40 function owned by RIF40 with NO schema component';

\df rif40_log_pkg.rif40_remove_from_debug

CREATE OR REPLACE FUNCTION rif40_log_pkg.rif40_add_to_debug(debug_text VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_add_to_debug()
Parameters:	Debug string pairs <function name><debug level>, <function name><debug level>, ...
Returns:	Nothing
Description:	Add debug string pairs to debug string.
		Debug string example 'rif40_log:DEBUG4', 'rif40_error:DEBUG4'

		Function name is the name of a valid RIF40 function owned by RIF40 with NO schema component
		Debug level is one of: 'DEBUG1', 'DEBUG2', 'DEBUG3', 'DEBUG4'
 		Checks: you will get a warning; the debug string pair will be ignored

		If the pair already exists you will get a warning
		If the function name already exists the previous pair is replaced
 */
DECLARE
	c1a2d CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.send_debug_to_info') AS send_debug_to_info, 
		       CURRENT_SETTING('rif40.debug') AS debug;
	c2a2d CURSOR(l_debug_text VARCHAR) FOR 
		WITH a AS (
			SELECT rif40_log_pkg.rif40_get_debug(l_debug_text) AS debug 			/* Valid new debug */
		), b AS (
			SELECT rif40_log_pkg.rif40_get_debug(CURRENT_SETTING('rif40.debug')) AS debug 	/* Old debug */
		)
		SELECT (a.debug).function_name AS function_name,
  		       (a.debug).debug AS old_debug,
  		       (a.debug).debug AS new_debug,
		       SUBSTR((a.debug).debug::TEXT, 6)::INTEGER AS old_debug_level,
		       SUBSTR((a.debug).debug::TEXT, 6)::INTEGER AS new_debug_level
		  FROM a
		 WHERE (a.debug).function_name NOT IN (SELECT (b.debug).function_name FROM b)		/* New debug */
		UNION
		SELECT (b.debug).function_name AS function_name,
  		       (b.debug).debug AS old_debug,
  		       (b.debug).debug AS new_debug,
		       SUBSTR((b.debug).debug::TEXT, 6)::INTEGER AS old_debug_level,
		       SUBSTR((b.debug).debug::TEXT, 6)::INTEGER AS new_debug_level
		  FROM b
		 WHERE (b.debug).function_name NOT IN (SELECT (a.debug).function_name FROM a)		/* Old debug */
		UNION
		SELECT (a.debug).function_name AS function_name,
  		       (a.debug).debug AS old_debug,
  		       (b.debug).debug AS new_debug,
		       SUBSTR((a.debug).debug::TEXT, 6)::INTEGER AS old_debug_level,
		       SUBSTR((b.debug).debug::TEXT, 6)::INTEGER AS new_debug_level
		  FROM a, b
		 WHERE (a.debug).function_name = (b.debug).function_name				/* Updated debug */
		ORDER BY 1;
--
	c1a2da_rec RECORD;
	c1a2db_rec RECORD;
	c2a2d_rec RECORD;
--
	sql_stmt VARCHAR;
	i INTEGER:=0;
--
	is_debug_enabled BOOLEAN;
BEGIN
--
-- Check for NULL
--
	IF debug_text IS NULL THEN
		RETURN;
	END IF;
--
	BEGIN
		OPEN c1a2d;	/* Before */
		FETCH c1a2d INTO c1a2da_rec;
		CLOSE c1a2d;
	EXCEPTION
		WHEN others THEN /* Try defaulting settings */
			SET rif40.debug = '';
			SET rif40.send_debug_to_info = 'off';
--
			OPEN c1a2d;
			FETCH c1a2d INTO c1a2da_rec;
			CLOSE c1a2d;
--
		RAISE INFO 'rif40_add_to_debug() DEFAULTED send DEBUG to INFO: %; debug function list: [%]', 
			c1a2da_rec.send_debug_to_info, c1a2da_rec.debug;
	END;
	is_debug_enabled:=rif40_log_pkg.rif40_is_debug_enabled('rif40_add_to_debug', 'DEBUG4');
--
-- Re-process debug string
--
	sql_stmt:='SET rif40.debug = ''';
	FOR c2a2d_rec IN c2a2d(debug_text) LOOP
		i:=i+1;
		IF is_debug_enabled AND c1a2da_rec.send_debug_to_info = 'on' THEN
			RAISE INFO 'rif40_add_to_debug() Function %() % to %', c2a2d_rec.function_name, c2a2d_rec.old_debug_level, c2a2d_rec.new_debug_level;
		ELSIF is_debug_enabled THEN
			RAISE DEBUG 'rif40_add_to_debug() Function %() % to %', c2a2d_rec.function_name, c2a2d_rec.old_debug_level, c2a2d_rec.new_debug_level;
		END IF;
		IF i = 1 THEN
			sql_stmt:=sql_stmt||c2a2d_rec.function_name||':'||c2a2d_rec.new_debug;
		ELSE
			sql_stmt:=sql_stmt||', '||c2a2d_rec.function_name||':'||c2a2d_rec.new_debug;
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||'''';
--
	IF i > 0 THEN
		IF is_debug_enabled AND c1a2da_rec.send_debug_to_info = 'on' THEN
			RAISE INFO 'SQL> %;', sql_stmt;
		ELSIF is_debug_enabled THEN
			RAISE DEBUG 'SQL> %;', sql_stmt;
		END IF;
		EXECUTE sql_stmt;
	ELSE
		RAISE WARNING 'rif40_add_to_debug() all debug string pairs in "%" are invalid', debug_text;
	END IF;
--
	OPEN c1a2d;	/* After */
	FETCH c1a2d INTO c1a2db_rec;
	CLOSE c1a2d;
--
	IF is_debug_enabled AND c1a2db_rec.send_debug_to_info = 'on' THEN
		RAISE INFO 'rif40_add_to_debug() SET DEBUG from: "%" to: "%"', c1a2da_rec.debug, c1a2db_rec.debug;
	ELSIF is_debug_enabled THEN
		RAISE DEBUG 'rif40_add_to_debug() SET DEBUG from: "%" to: "%"', c1a2da_rec.debug, c1a2db_rec.debug;
	END IF;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_log_pkg.rif40_add_to_debug(VARCHAR) IS 'Function: 	rif40_add_to_debug()
Parameters:	Debug string pairs <function name><debug level>, <function name><debug level>, ...
Returns:	Nothing
Description:	Add debug string pairs to debug string.
		Debug string example ''rif40_log:DEBUG4'', ''rif40_error:DEBUG4''

		Function name is the name of a valid RIF40 function owned by RIF40 with NO schema component
		Debug level is one of: ''DEBUG1'', ''DEBUG2'', ''DEBUG3'', ''DEBUG4''
 		Checks: you will get a warning; the debug string pair will be ignored

		If the pair already exists you will get a warning
		If the function name already exists the previous pair is replaced';

\df rif40_log_pkg.rif40_add_to_debug

CREATE OR REPLACE FUNCTION rif40_log_pkg.rif40_get_debug(debug_text VARCHAR)
RETURNS SETOF RIF40_LOG_PKG.RIF40_DEBUG_RECORD
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_get_debug()
Parameters:	Debug string pairs <function name><debug level>, <function name><debug level>, ...
Returns:	1 or more RIF40_LOG_PKG.RIF40_DEBUG_RECORD records
Description:	Get set of 1 or more RIF40_LOG_PKG.RIF40_DEBUG_RECORD records
 */
DECLARE
	c1rg CURSOR(l_debug VARCHAR) FOR 
		SELECT RTRIM(LTRIM(UNNEST(STRING_TO_ARRAY(l_debug, ',')))) AS debug;
	c2rg CURSOR(l_function_name VARCHAR) FOR 
		SELECT l.lanname||' function' object_type, 
			COALESCE(n.nspname, r.rolname)||'.'||p.proname object_name 					/* Functions */
		  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
			LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
		 WHERE p.prolang    = l.oid
		   AND p.prorettype = t.oid
		   AND p.proowner   IN (SELECT oid FROM pg_roles WHERE rolname IN ('postgres', 'rif40'))
		   AND p.proowner   = r.oid
		   AND p.proname    = LOWER(l_function_name);	
--
	c1rg_rec RECORD;
	c2rg_rec RECORD;
--
	rec RIF40_LOG_PKG.RIF40_DEBUG_RECORD;
	v VARCHAR[];
	l_debug VARCHAR;
	l_function_name VARCHAR;
BEGIN
--
-- Check for NULL
--
	IF debug_text IS NULL THEN
		RETURN;
	END IF;
--
	FOR c1rg_rec IN c1rg(debug_text) LOOP
		IF c1rg_rec.debug IS NOT NULL THEN
			v:=NULL;
			v:=STRING_TO_ARRAY(c1rg_rec.debug, ':');
--
-- Check function name exists
--
			l_function_name:=v[array_lower(v, 1)];
			OPEN c2rg(l_function_name);
			FETCH c2rg INTO c2rg_rec;
			CLOSE c2rg;
--
-- Fixed. This is now an error!
--
			IF array_lower(v, 1) != array_upper(v, 1)-1 THEN
				RAISE EXCEPTION 
					'rif40_debug_record() [99904]: expecting two elements converting v(%) to RIF40_LOG_PKG.RIF40_DEBUG_RECORD, l_debug=%', 
					c1rg_rec.debug::Text, l_debug::Text;
			ELSIF c2rg_rec.object_name IS NULL THEN
				RAISE WARNING 'rif40_debug_record() [99905]: function name %() NOT FOUND/NOT EXECUTABLE by user.', l_function_name;
			ELSE
				rec.function_name:=l_function_name;
				l_debug:=v[array_lower(v, 1)+1];
				IF l_debug NOT IN ('DEBUG1', 'DEBUG2', 'DEBUG3', 'DEBUG4') THEN
					RAISE WARNING 'rif40_debug_record() [99911]: function %() invalid <debug level> % converting v(%) to RIF40_LOG_PKG.RIF40_DEBUG_RECORD', 
						c2rg_rec.object_name, l_debug, c1rg_rec.debug;
				ELSE
					rec.debug:=l_debug;
					RETURN NEXT rec;
				END IF;
			END IF;
		END IF;
	END LOOP;
--
	RETURN;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_log_pkg.rif40_get_debug(VARCHAR) IS 'Function: 	rif40_get_debug()
Parameters:	Debug string pairs <function name><debug level>, <function name><debug level>, ...
Returns:	1 or more RIF40_LOG_PKG.RIF40_DEBUG_RECORD records
Description:	Get set of 1 or more RIF40_LOG_PKG.RIF40_DEBUG_RECORD records';

\df rif40_log_pkg.rif40_get_debug

CREATE OR REPLACE FUNCTION rif40_log_pkg.rif40_get_error_code_action(l_error_code INTEGER, function_name VARCHAR)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_get_error_code_action()
Parameters:	error code, function name
Returns:	RIF40_ERROR_MESSAGES.ACTION or NULL
Description:	Get action for EXCEPTION HINT
 */
DECLARE
	c1err CURSOR(l_code INTEGER) FOR
		SELECT * 
		  FROM rif40_error_messages
		 WHERE l_code = rif40_error_messages.error_code;
--
	c1err_rec RECORD;
BEGIN
	OPEN c1err(l_error_code);
	FETCH c1err INTO c1err_rec;
	CLOSE c1err;
	IF c1err_rec.action IS NULL THEN
		RAISE WARNING 'rif40_get_error_code_action() [99909]: function name %() NO ERROR MESSAGE FOUND IN DB FOR: %.', 
			COALESCE(function_name, '<ONKNOWN>'), l_error_code;
		RETURN NULL;
	END IF;
--
	RETURN c1err_rec.action;
EXCEPTION
	WHEN others THEN
		RAISE WARNING 'rif40_get_error_code_action() [99910]: function name %() GETTING ERROR MESSAGE FOUND IN DB FOR: % RAISED %. [IGNORED]', 
			COALESCE(function_name, '<ONKNOWN>'), l_error_code, SQLERRM;
		RETURN NULL;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_log_pkg.rif40_get_error_code_action(INTEGER, VARCHAR) IS 'Function: 	rif40_get_error_code_action()
Parameters:	error code, function name
Returns:	RIF40_ERROR_MESSAGES.ACTION or NULL
Description:	Get action for EXCEPTION HINT';

\df rif40_log_pkg.rif40_get_error_code_action

DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_error(INTEGER, VARCHAR, VARIADIC VARCHAR[]);

CREATE OR REPLACE FUNCTION rif40_log_pkg.rif40_error(l_error_code INTEGER, function_name VARCHAR, format_and_args VARIADIC VARCHAR[])
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_error()
Parameters:	error code, function name, format, args ...
Returns:	Nothing (RAISE EXCEPTION). The return code is to allow is use in SQL
Description:	This is the error handler!

 desc rif40_error_messages
 Name                                                                     Null?    Type
 ------------------------------------------------------------------------ -------- -------------------------------------------------
 ERROR_CODE                                                               NOT NULL NUMBER(5)
 TAG                                                                      NOT NULL VARCHAR2(80)
 TABLE_NAME                                                                        VARCHAR2(30)
 CAUSE                                                                    NOT NULL VARCHAR2(4000)
 ACTION                                                                   NOT NULL VARCHAR2(512)
 MESSAGE                                                                  NOT NULL VARCHAR2(512)

 */
DECLARE
	c2err CURSOR(l_function_name VARCHAR) FOR 
		SELECT l.lanname||' function' object_type, 
			COALESCE(n.nspname, r.rolname)||'.'||p.proname object_name 					/* Functions */
		  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
			LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
		 WHERE p.prolang    = l.oid
		   AND p.prorettype = t.oid
		   AND p.proowner   = (SELECT oid FROM pg_roles WHERE rolname = 'rif40')
		   AND p.proowner   = r.oid
		   AND p.proname    = LOWER(l_function_name);	
--
	c2err_rec RECORD;
--
	format VARCHAR;
	output VARCHAR;
	format_len INTEGER;
	chr_count INTEGER:=0;
	arg_count INTEGER:=0;
	max_args INTEGER;
	chr VARCHAR;
--
	l_function_name VARCHAR;
	l_action VARCHAR;
BEGIN
--
-- Get error message from DB
--
	l_action:=rif40_log_pkg.rif40_get_error_code_action(l_error_code, function_name);
--
-- Check format and args are not NULL
--
	IF format_and_args IS NULL THEN
		output:='<NO MESSAGE>';
	ELSE
		arg_count:=array_lower(format_and_args, 1);
		max_args:=array_upper(format_and_args, 1);
		format:=format_and_args[arg_count];
		output:='';
		format_len:=LENGTH(format);
		IF format_len = 0 THEN
			RETURN NULL;
		END IF;
--
-- Format string
--
		LOOP
			chr_count:=chr_count+1;
			IF chr_count > format_len THEN 
				EXIT;
			END IF;
			chr:=SUBSTRING(format, chr_count, 1);
			IF chr = '%' AND SUBSTRING(format, chr_count+1, 1) = '%' THEN 		/* %% */
				output:=output||'%%';
				chr_count:=chr_count+1;
			ELSIF chr = '%' THEN							/* Arg */
				arg_count:=arg_count+1;
				IF arg_count > max_args THEN					/* run out of args */
					EXIT;
				END IF;
				output:=output||COALESCE(format_and_args[arg_count]::TEXT, '');
			ELSE
				output:=output||chr;
			END IF;
--
		END LOOP;
		IF arg_count != max_args THEN
			RAISE WARNING 'rif40_error() [99907]: Message in %(): too many/too few args (got: %; expecting %) for format: %', function_name, max_args, arg_count, format;
		END IF;
	END IF;
--
-- Check function name exists
--
	OPEN c2err(function_name);
	FETCH c2err INTO c2err_rec;
	CLOSE c2err;
	IF c2err_rec.object_name IS NULL THEN
		RAISE WARNING 'rif40_error() [99906]: function name %() NOT FOUND/NOT EXECUTABLE by user.', function_name;
		l_function_name:='<UNKNOWN>';
	ELSE
		l_function_name:=c2err_rec.object_name;
 	END IF;
--
-- RAISE error
--
	IF l_action IS NULL THEN
		RAISE EXCEPTION '%(): %', l_function_name, output USING DETAIL=l_error_code::Text;
	ELSE
		RAISE EXCEPTION '%(): %', l_function_name, output USING HINT=l_action, DETAIL=l_error_code::Text;
	END IF;
--
	RETURN l_error_code;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_log_pkg.rif40_error(INTEGER, VARCHAR, VARIADIC VARCHAR[]) IS 'Function: 	rif40_error()
Parameters:	error code, function name, format, args ...
Returns:	Nothing
Description:	This is the error handler!

 desc rif40_error_messages
 Name                                                                     Null?    Type
 ------------------------------------------------------------------------ -------- -------------------------------------------------
 ERROR_CODE                                                               NOT NULL NUMBER(5)
 TAG                                                                      NOT NULL VARCHAR2(80)
 TABLE_NAME                                                                        VARCHAR2(30)
 CAUSE                                                                    NOT NULL VARCHAR2(4000)
 ACTION                                                                   NOT NULL VARCHAR2(512)
 MESSAGE                                                                  NOT NULL VARCHAR2(512)
';

\df rif40_log_pkg.rif40_error

CREATE OR REPLACE FUNCTION rif40_log_pkg.rif40_log(debug_level RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, function_name VARCHAR, format_and_args VARIADIC VARCHAR[])
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_log()
Parameters:	Debug level, function name, format, args ...
Returns:	Nothing
Description:	Log to INFO/DEBUG/WARNING. This is not the error handler!

		Debug level is one of: 'WARNING', 'INFO', 'DEBUG1', 'DEBUG2', 'DEBUG3', 'DEBUG4'
		Function name is the name of a valid RIF40 function owned by RIF40 with NO schema component (or you will get a warning)
		Foramt and args is a variadic VARCHAR array; minimum size 1 in the same format as RAISE, ie. % is the replacement character, %% escapes %. 
		Printf style %s will cause strange plurals (i.e. the s is ignored)
		args must equal format specifiers (%) (or you will get a warning)

		Log messages do not have to exist in RIF40 error messages

		This function will eventually call a further function to do an autonomous INSERT into rif40_log_messages using dblink
 */
DECLARE
	c1log CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.send_debug_to_info') AS send_debug_to_info, 
		       CURRENT_SETTING('rif40.debug') AS debug;
	c2log CURSOR(l_function_name VARCHAR) FOR 
		SELECT l.lanname||' function' object_type, 
			COALESCE(n.nspname, r.rolname)||'.'||p.proname object_name 					/* Functions */
		  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
			LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
		 WHERE p.prolang    = l.oid
		   AND p.prorettype = t.oid
		   AND p.proowner   IN (SELECT oid FROM pg_roles WHERE rolname IN ('rif40', 'postgres'))
		   AND p.proowner   = r.oid
		   AND p.proname    = LOWER(l_function_name);	
--
	c1log_rec RECORD;
	c2log_rec RECORD;
--
	format VARCHAR;
	output VARCHAR;
	format_len INTEGER;
	chr_count INTEGER:=0;
	arg_count INTEGER:=0;
	max_args INTEGER;
	chr VARCHAR;
	ts		VARCHAR;
BEGIN
--
-- Check format and args are not NULL
--
	IF format_and_args IS NULL THEN
		RETURN;
	END IF;
	arg_count:=array_lower(format_and_args, 1);
	max_args:=array_upper(format_and_args, 1);
	format:=format_and_args[arg_count];
	output:='';
	format_len:=LENGTH(format);
	IF format_len = 0 THEN
		RETURN;
	END IF;
--
-- Format string
--
	LOOP
		chr_count:=chr_count+1;
		IF chr_count > format_len THEN 
			EXIT;
		END IF;
		chr:=SUBSTRING(format, chr_count, 1);
		IF chr = '%' AND SUBSTRING(format, chr_count+1, 1) = '%' THEN 		/* %% */
			output:=output||'%%';
			chr_count:=chr_count+1;
		ELSIF chr = '%' THEN							/* Arg */
			arg_count:=arg_count+1;
			IF arg_count > max_args THEN					/* run out of args */
				EXIT;
			END IF;
			output:=output||COALESCE(format_and_args[arg_count]::TEXT, '');
		ELSE
			output:=output||chr;
		END IF;
--
	END LOOP;
	IF arg_count != max_args THEN
		RAISE WARNING 'rif40_log() [99912]: Message in %(): too many/too few args (got: %; expecting %) for format: %', function_name, max_args, arg_count, format;
	END IF;

--
-- Transaction timestamp
--		
	ts:='+'||LPAD(ROUND(EXTRACT(EPOCH FROM clock_timestamp()-transaction_timestamp())::NUMERIC, 2)::VARCHAR, 8, '0')||'s ';
--
	IF debug_level = 'WARNING' THEN
		RAISE WARNING '% %(): %', ts, function_name, output;
	ELSIF debug_level = 'INFO' THEN
		RAISE INFO '% %(): %', ts, function_name, output;
	ELSE -- Debug level: DEBUG
--
-- Check function name exists
--
--	OPEN c2log(function_name);
--	FETCH c2log INTO c2log_rec;
--	CLOSE c2log;
--	IF c2log_rec.object_name IS NULL THEN
--		RAISE WARNING 'rif40_log() [99913]: function name %() NOT FOUND/NOT EXECUTABLE by user.', function_name;
-- 	END IF;
--
-- Get logging parameters
--
		OPEN c1log;
		FETCH c1log INTO c1log_rec;
		CLOSE c1log;
--	
		IF c1log_rec.send_debug_to_info = 'on' AND rif40_log_pkg.rif40_is_debug_enabled(function_name, debug_level) THEN
			RAISE INFO '% [%] %(): %', ts, debug_level, function_name, output;
		ELSIF rif40_log_pkg.rif40_is_debug_enabled(function_name, debug_level) THEN
			RAISE DEBUG '% %(): %', ts, function_name, output;
		END IF;
	END IF;
--
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_log_pkg.rif40_log(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR, VARIADIC VARCHAR[]) IS 'Function: 	rif40_log()
Parameters:	Debug level, function name, format, args ...
Returns:	Nothing
Description:	Log to INFO/DEBUG/WARNING. This is not the error handler!

		Debug level is one of: ''WARNING'', ''INFO'', ''DEBUG1'', ''DEBUG2'', ''DEBUG3'', ''DEBUG4''
		Function name is the name of a valid RIF40 function owned by RIF40 with NO schema component (or you will get a warning)
		Foramt and args is a variadic VARCHAR array; minimum size 1 in the same format as RAISE, ie. % is the replacement character, %% escapes %. 
		Printf style %s will cause strange plurals (i.e. the s is ignored)
		args must equal format specifiers (%) (or you will get a warning)

		Log messages do not have to exist in RIF40 error messages

		This function will eventually call a further function to do an autonomous INSERT into rif40_log_messages using dblink';

\df rif40_log_pkg.rif40_log

GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_log_setup() TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_log_setup() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_send_debug_to_info(BOOLEAN) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_send_debug_to_info(BOOLEAN) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_get_error_code_action(INTEGER, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_get_error_code_action(INTEGER, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_log(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR, VARIADIC VARCHAR[]) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_log(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR, VARIADIC VARCHAR[]) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_error(INTEGER, VARCHAR, VARIADIC VARCHAR[]) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_error(INTEGER, VARCHAR, VARIADIC VARCHAR[]) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_add_to_debug(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_add_to_debug(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_remove_from_debug(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_remove_from_debug(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_get_debug(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_get_debug(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_is_debug_enabled(function_name VARCHAR, debug RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_log_pkg.rif40_is_debug_enabled(function_name VARCHAR, debug RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL) TO rif40;

\set
--SELECT CURRENT_SETTING('rif40.debug');
\set VERBOSITY verbose
\echo Test A: Log setup
BEGIN;
SELECT rif40_log_pkg.rif40_log_setup();
\set
--SELECT CURRENT_SETTING('rif40.debug');
\echo Test B: send debug output to info
SELECT rif40_log_pkg.rif40_send_debug_to_info(TRUE);
\echo Test C: Log function
SELECT rif40_log_pkg.rif40_log('INFO', 'rif40_log', 'Built rif40_log_pkg (logging, auditing and debug) package.');
SELECT rif40_log_pkg.rif40_log('INFO', 'rif40_log', 'Built rif40_log_pkg (logging, auditing and debug) package on % %.', 'bbb');
SELECT rif40_log_pkg.rif40_log('INFO', 'rif40_log', 'Built rif40_log_pkg (logging, auditing and debug) package on % %.', 'bbb', 'ccc');
SELECT rif40_log_pkg.rif40_log('INFO', 'rif40_lo', 'Built rif40_log_pkg (logging, auditing and debug) package on % %.', 'bbb', 'ccc', 'dddd');
\echo Test D: Add debug
SELECT rif40_log_pkg.rif40_add_to_debug('rif40_add_to_debug:DEBUG4,rif40_log:DEBUG4, rif40_error:DEBUG4');
SELECT rif40_log_pkg.rif40_log_setup();
SELECT rif40_log_pkg.rif40_is_debug_enabled('rif40_add_to_debug', 'DEBUG4');
SELECT rif40_log_pkg.rif40_remove_from_debug('rif40_log');
SELECT rif40_log_pkg.rif40_add_to_debug('rif40_log:DEBUG4');
SELECT rif40_log_pkg.rif40_add_to_debug('rif40_log:DEBUG4');
SELECT rif40_log_pkg.rif40_get_debug(CURRENT_SETTING('rif40.debug')) AS debug /* Current debug */;

WITH a AS (
	SELECT rif40_log_pkg.rif40_get_debug(CURRENT_SETTING('rif40.debug')) AS debug /* Current debug */
)
SELECT (a.debug).function_name AS function_name,
         (a.debug).debug AS debug,
       SUBSTR((a.debug).debug::TEXT, 6)::INTEGER AS debug_level
  FROM a;

SELECT rif40_log_pkg.rif40_log_setup();
SELECT rif40_log_pkg.rif40_remove_from_debug('rif40_log');
SELECT rif40_log_pkg.rif40_log_setup();
SELECT rif40_log_pkg.rif40_remove_from_debug('rif40_add_to_debug');
SELECT rif40_log_pkg.rif40_remove_from_debug('rif40_error');

DO LANGUAGE plpgsql $$
BEGIN
	PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_log', 'Test RAISE EXEPTION rif40_log_pkg (logging, auditing and debug) package on % %.', 'bbb', 'ccc', 'dddd');
EXCEPTION
	WHEN others THEN 
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_log', 'Caught test exception.');
END;
$$;
END;
\set VERBOSITY default

--
-- Eof
