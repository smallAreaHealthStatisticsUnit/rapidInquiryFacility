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
-- Rapid Enquiry Facility (RIF) - Drop instead of triggers
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

CREATE OR REPLACE FUNCTION rif40_trg_pkg.drop_instead_of_triggers() 
RETURNS void
AS
$func$
/*

Function: 	drop_instead_of_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Drop INSTEAD OF functions and triggers for all views of T_ tables where username as a column
	 	INSERT/UPDATE/DELETE of another users data is NOT permitted	
 */
DECLARE
	c1diot CURSOR FOR
		WITH t AS (
			SELECT a.relname AS tablename 				
 			 FROM pg_roles r, pg_class a, pg_namespace n, pg_attribute c		
			 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relkind        = 'r' 				/* Relational table */
			   AND a.relpersistence IN ('p', 'u') 			/* Persistent */
			   AND a.relowner       = r.oid
			   AND n.oid 		= a.relnamespace
			   AND n.nspname  	= USER    
			   AND c.attrelid       = a.oid
			   AND c.attname  	= 'username'
		), v AS (
			SELECT a.relname AS viewname 				
 			 FROM pg_roles r, pg_namespace n, pg_attribute c, pg_class a		
				LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid)
			 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relkind        = 'v' 				/* Views */
			   AND a.relowner       = r.oid
			   AND n.oid 		= a.relnamespace
			   AND n.nspname  	= USER    
			   AND c.attrelid       = a.oid
			   AND c.attname  	= 'username'
		)
		SELECT t.tablename, v.viewname
		  FROM t, v
		 WHERE t.tablename  = 't_'||v.viewname
		 ORDER BY 1;
--
	c1diot_rec RECORD;
	sql_stmt VARCHAR[];
BEGIN
--
-- Must be RIF40
--
	IF USER != 'rif40' THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'drop_instead_of_triggers',
                	'Cannot drop INSTEAD OF triggers; user % must must be RIF40', USER::VARCHAR);
	END IF;
--
	FOR c1diot_rec IN c1diot LOOP
		sql_stmt[1]:='DROP TRIGGER IF EXISTS trg_'||c1diot_rec.viewname||' ON '||c1diot_rec.viewname||' CASCADE';
		sql_stmt[2]:='DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_'||c1diot_rec.viewname||'() CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_trg_pkg.drop_instead_of_triggers() IS 'Function: 	drop_instead_of_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Drop INSTEAD OF functions and triggers for all views of T_ tables where username as a column
	 	INSERT/UPDATE/DELETE of another users data is NOT permitted';

--
-- Eof
