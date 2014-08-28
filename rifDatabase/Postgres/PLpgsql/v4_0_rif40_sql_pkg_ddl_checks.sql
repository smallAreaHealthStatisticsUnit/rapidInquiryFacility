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
-- Rapid Enquiry Facility (RIF) - RIF SQL package rif40_ddl_checks() function
--
-- This function is now split into sub-functions by check and using pg_ tables only if possible
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

\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_a.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_b.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_c.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_d.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_e.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_f.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_g.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_h.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_i.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_j.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl_check_k.sql

-- rif40_ddl_checks:									70000 to 70049
CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl_checks()
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 		rif40_ddl_checks()
Parameters: 	None
Returns: 		Nothing
Description:	Validate RIF DDL

Check for:

a) Missing tables and views
b) Missing table/view comments
c) Missing table/view columns
d) Missing table/view column comments
e) Missing triggers
f) Extra tables and views
g) Missing sequences
h) All tables, views and sequences GRANT SELECT to rif_user and rif_manager
i) All functions in rif40_sql_pkg, rif40_geo_pkg GRANT EXECUTE to rif40, rif_user and rif_manager
j) Extra table/view columns
k) Missing comments

Will work as any RIF user, not just RIF40

 */
DECLARE
	i INTEGER:=0;
	j INTEGER:=0;
--
	schema_owner VARCHAR:='rif40';
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager role
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-70000, 'rif40_ddl_checks', 'User % must be rif40 or have rif_user or rif_manager role',
			USER::VARCHAR);
	END IF;
--
-- Schema owner is RIF40 unless we are RIFUPG34 and then it is public
--
	IF USER = 'rifupg34' THEN
		schema_owner:='public';
	END IF;
--
-- Check d) Missing table/view column comments
--
	i:=i+rif40_sql_pkg.rif40_ddl_check_d();	
--
-- Check c) Missing table/view columns
--
	i:=i+rif40_sql_pkg.rif40_ddl_check_c();	
--
-- Check b) Missing table/view comments
--
	i:=i+rif40_sql_pkg.rif40_ddl_check_b();	
--
-- Check f) Extra tables and views
--
	j:=j+rif40_sql_pkg.rif40_ddl_check_f();
--
	IF USER != 'rifupg34' THEN
--
-- Check e) Missing triggers
--
		i:=i+rif40_sql_pkg.rif40_ddl_check_e();	
--
-- Check g) Missing sequences
--
		i:=i+rif40_sql_pkg.rif40_ddl_check_g();
--
-- Check h) All tables, views and sequences GRANT SELECT to rif_user and rif_manager
--
		i:=i+rif40_sql_pkg.rif40_ddl_check_h();
--
-- Check i) All functions in rif40_sql_pkg, rif40_geo_pkg GRANT EXECUTE to rif40, rif_user and rif_manager
--
		i:=i+rif40_sql_pkg.rif40_ddl_check_i();
--
-- Check j) Extra table/view columns
--
		j:=j+rif40_sql_pkg.rif40_ddl_check_j();
--
-- Check k) Missing comments
--
		i:=i+rif40_sql_pkg.rif40_ddl_check_k();
	END IF;
--
-- Check a) Missing tables and views
--
	i:=i+rif40_sql_pkg.rif40_ddl_check_a();	
	IF i > 0 OR j > 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-70001, 'rif40_ddl_checks', 
			'Schema: % % missing tables/triggers/views/columns/comments/sequences/grants; % extra', 
			schema_owner::VARCHAR, i::VARCHAR, j::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', '[70002]: rif40_ddl_checks', 
			'Schema: % All tables/triggers/views/columns/comments/sequences/grants present; % extra', 
			schema_owner::VARCHAR, j::VARCHAR);
	END IF;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl_checks() IS 'Function: 	rif40_ddl_checks()
Parameters: 	None
Returns: 		Nothing
Description:	Validate RIF DDL

Check for:

a) Missing tables and views
b) Missing table/view comment
c) Missing table/view columns
d) Missing table/view column comments
e) Missing triggers
f) Extra tables and views
g) Missing sequences
h) All tables, views and sequences GRANT SELECT to rif_user and rif_manager
i) All functions in rif40_sql_pkg, rif40_geo_pkg GRANT EXECUTE to rif40, rif_user and rif_manager
j) Extra columns

Will work as any RIF user, not just RIF40';

\df rif40_sql_pkg.rif40_ddl_check*

GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl_checks() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl_checks() TO PUBLIC;

-- SELECT rif40_sql_pkg.rif40_ddl_checks();

--
-- Eof
