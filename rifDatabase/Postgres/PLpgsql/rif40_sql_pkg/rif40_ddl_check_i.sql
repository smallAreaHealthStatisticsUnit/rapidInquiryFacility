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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function - check i) 
--								  All functions in rif40_sql_pkg, rif40_geo_pkg GRANT EXECUTE to rif40, rif_user and rif_manager
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
--rif40_ddl_check_i:									70450 to 70499
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_check_i()
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_ddl_check_i()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check i) All functions owner by rif40 role (multiple schemas, e.g. rif40_sql_pkg, rif40_sm_pkg) 
         GRANT EXECUTE to rif40, rif_user and rif_manager

SELECT has_function_privilege(USER, 'rif40_sql_pkg._rif40_range_partition_create_insert(character varying, character varying, character varying, integer)', 'execute');

PERFORM rif40_sql_pkg._rif40_range_partition_create_insert('a', 'b', 'c', 1); will work as peterh and should not; it should not; caused by default execute privileges

As Postgres:

GRANT USAGE ON SCHEMA rif40_sql_pkg TO rif_user;
GRANT USAGE ON SCHEMA rif40_sql_pkg TO rif_manager;
ALTER DEFAULT PRIVILEGES FOR ROLE rif40 REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA rif40_sm_pkg REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA rif40_sql_pkg REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA rif40_sim_pkg REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA rif40_log_pkg REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA rif40_dmp_pkg REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA rif40_geo_pkg REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC;

sahsuland_dev=# \ddp
           Default access privileges
 Owner | Schema |   Type   | Access privileges
-------+--------+----------+-------------------
 rif40 |        | function | rif40=X/rif40
(1 row)

 */
DECLARE
	c9 CURSOR(l_schema VARCHAR) FOR /* All functions in rif40_sql_pkg, rif40_geo_pkg GRANT EXECUTE to rif40, rif_user and rif_manager */
		SELECT object_type, schemaname, 
                       schemaname||'.'||function_name AS function_name,
  		       has_function_privilege(USER, schemaname||'.'||function_name, 'execute') AS  has_user_select,
  		       has_function_privilege('rif40', schemaname||'.'||function_name, 'execute') AS has_rif40_select,
  		       has_function_privilege('rif_user', schemaname||'.'||function_name, 'execute') AS has_rif_user_select,
  		       has_function_privilege('rif_manager', schemaname||'.'||function_name, 'execute') AS has_rif_manager_select
		  FROM (
			SELECT l.lanname||' function' AS object_type, 
			       COALESCE(n.nspname, r.rolname) AS schemaname,
			       p.proname||rif40_sql_pkg.rif40_get_function_arg_types(p.proargtypes) AS function_name	
			  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
				LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
			 WHERE p.prolang    = l.oid
			   AND p.prorettype = t.oid
			   AND p.proowner   = (SELECT oid FROM pg_roles WHERE rolname = l_schema)
			   AND p.proowner   = r.oid
			) AS a
		 WHERE has_schema_privilege('rif40', 'USAGE') 
		 ORDER BY 1, 2;
--
	c9_rec RECORD;
--
	schema_owner VARCHAR:='rif40';
	i INTEGER:=0;
BEGIN	
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_ddl_check_i', '[70450]: Checking all functions in rif40_sql_pkg, rif40_geo_pkg for GRANT EXECUTE to rif40, rif_user and rif_manager');
		FOR c9_rec IN c9(schema_owner) LOOP
			IF c9_rec.function_name = 'rif40.t_rif40_sahsu_geometry_insert()' AND c9_rec.has_rif40_select = TRUE THEN
			/* Ignore */
			ELSIF c9_rec.schemaname IN ('rif40_geo_pkg', 'rif40_trg_pkg', 'rif40_sql_pkg', 'rif40_sm_pkg') AND c9_rec.has_rif40_select = FALSE THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_i', '[70451]: Missing grant EXECUTE on % % to rif40(%); rif_user(%) and rif_manager(%) privileges ignored', 
					c9_rec.object_type::VARCHAR, c9_rec.function_name::VARCHAR, c9_rec.has_rif40_select::VARCHAR, 
					c9_rec.has_rif_user_select::VARCHAR, c9_rec.has_rif_manager_select::VARCHAR);
				i:=i+1;
			ELSIF c9_rec.schemaname NOT IN ('rif40_geo_pkg', 'rif40_trg_pkg', 'rif40_sql_pkg', 'rif40_sm_pkg') AND (
			      c9_rec.has_rif40_select = FALSE OR c9_rec.has_rif_user_select = FALSE OR c9_rec.has_rif_manager_select = FALSE) THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl_check_i', '[70452]: Missing grant EXECUTE on % % to rif40(%), rif_user(%) and rif_manager(%)', 
					c9_rec.object_type::VARCHAR, c9_rec.function_name::VARCHAR, c9_rec.has_rif40_select::VARCHAR, 
					c9_rec.has_rif_user_select::VARCHAR, c9_rec.has_rif_manager_select::VARCHAR);
				i:=i+1;
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl_check_i', '[70453]: EXECUTE grant OK on % % to rif40(%), rif_user(%) and rif_manager(%)', 
					c9_rec.object_type::VARCHAR, c9_rec.function_name::VARCHAR, c9_rec.has_rif40_select::VARCHAR, 
					c9_rec.has_rif_user_select::VARCHAR, c9_rec.has_rif_manager_select::VARCHAR);
			END IF;
		END LOOP;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_check_i() IS 'Function: 		rif40_ddl_check_i()
Parameters: 	None
Returns: 		Error count
Description:	Validate RIF DDL

Check i) All functions in rif40_sql_pkg, rif40_geo_pkg GRANT EXECUTE to rif40, rif_user and rif_manager';

--
-- Eof