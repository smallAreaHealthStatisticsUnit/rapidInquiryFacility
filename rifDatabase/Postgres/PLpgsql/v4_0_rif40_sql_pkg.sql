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
-- Rapid Enquiry Facility (RIF) - Create PG psql code (SQL and Oracle compatibility processing)
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

\echo Creating PG psql code (SQL and Oracle compatibility processing)...

--
-- Remove old versiom
--
DROP FUNCTION IF EXISTS rif40_range_partition(VARCHAR, VARCHAR, VARCHAR);

CREATE OR REPLACE VIEW rif40_sql_pkg.user_role_privs
AS
SELECT UPPER(role_name) AS granted_role 
  FROM information_schema.enabled_roles;
COMMENT ON VIEW rif40_sql_pkg.user_role_privs IS 'All roles granted to user';
COMMENT ON COLUMN rif40_sql_pkg.user_role_privs.granted_role IS 'Role granted to user. warning this is in uppercase so the roles are compatible with the Oracle uppercase naming convention';

\dv rif40_sql_pkg.user_role_privs

CREATE OR REPLACE FUNCTION rif40_sql_pkg._print_table_size(table_name VARCHAR)
RETURNS BIGINT
SECURITY INVOKER
AS $func$
/*
Function: 	_print_table_size()
Parameters:	Table name
Returns:	Size of table and indexes in bytes 
Description:	Print size of table nicely
 */
DECLARE
	t_size BIGINT;
	rel_size BIGINT;
BEGIN
	t_size:=pg_table_size(table_name);
	rel_size:=pg_total_relation_size(table_name);
	PERFORM rif40_log_pkg.rif40_log('INFO', '_print_table_size', 'Size of table %: %; indexes: %', 
		table_name::VARCHAR				/* Table name */,
		pg_size_pretty(t_size)::VARCHAR			/* Size of table */,
		pg_size_pretty(rel_size-t_size)::VARCHAR	/* Size of indexes */);
--
	RETURN rel_size;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg._print_table_size(VARCHAR) IS 'Function: 	_print_table_size()
Parameters:	Table name
Returns:	Size of table in bytes 
Description:	Print size of table nicely';

CREATE OR REPLACE FUNCTION rif40_sql_pkg.does_role_exist(username VARCHAR)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
/*
Function: 	does_role_exist()
Parameters:	Username
Returns:	TRUE or FALSE 
Description:	Does the role exist

To prevent errors like this:

rif40=> \COPY t_rif40_studies FROM '../sahsuv3_v4/data/t_rif40_studies.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
ERROR:  42704: role "LINDAB@PRIVATE.NET" does not exist
CONTEXT:  PL/pgSQL function "trigger_fct_t_rif40_studies_checks" line 127 at FETCH
COPY t_rif40_studies, line 1: "1046,LINDAB@PRIVATE.NET,EW01,SAHSU,Enter study name here,,,,S1046_EXTRACT,S1046_MAP,2013-03-22 17:15..."
LOCATION:  get_role_oid, acl.c:4822

 */
DECLARE
	c1_umors CURSOR(l_username VARCHAR) FOR
		SELECT rolname FROM pg_roles WHERE rolname = l_username;
	c1_rec RECORD;
BEGIN
	OPEN c1_umors(username);	
	FETCH c1_umors INTO c1_rec;
	CLOSE c1_umors;
--
	IF c1_rec.rolname = username THEN
		RETURN TRUE;
	ELSE
		RETURN FALSE;
	END IF;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg.does_role_exist(VARCHAR) IS 'Function: 	does_role_exist()
Parameters:	Username
Returns:	TRUE or FALSE 
Description:	Does the role exist

To prevent errors like this:

rif40=> \COPY t_rif40_studies FROM ''../sahsuv3_v4/data/t_rif40_studies.csv'' WITH (FORMAT csv, QUOTE ''"'', ESCAPE ''\\'');
ERROR:  42704: role "LINDAB@PRIVATE.NET" does not exist
CONTEXT:  PL/pgSQL function "trigger_fct_t_rif40_studies_checks" line 127 at FETCH
COPY t_rif40_studies, line 1: "1046,LINDAB@PRIVATE.NET,EW01,SAHSU,Enter study name here,,,,S1046_EXTRACT,S1046_MAP,2013-03-22 17:15..."
LOCATION:  get_role_oid, acl.c:4822';

CREATE OR REPLACE FUNCTION rif40_sql_pkg.is_rif40_user_manager_or_schema()
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
/*

Function: 	is_rif40_user_manager_or_schema()
Parameters:	None
Returns:	TRUE or FALSE 
Description:	Is user rif40 or does the user have the rif_user or rif_manager role?
		The user postgres is never a RIF_USER
 */
DECLARE
BEGIN
	IF USER = 'rif40' THEN
		RETURN TRUE;
	ELSIF USER = 'postgres' THEN
		RETURN FALSE;
	ELSIF pg_has_role(USER, 'rif_user', 'USAGE') THEN
		RETURN TRUE;
	ELSIF pg_has_role(USER, 'rif_manager', 'USAGE') THEN
		RETURN TRUE;
	ELSE
		RETURN FALSE;
	END IF;
END;
$func$
LANGUAGE PLPGSQL;
COMMENT ON FUNCTION rif40_sql_pkg.is_rif40_user_manager_or_schema() IS 'Function: 	is_rif40_user_manager_or_schema()
Parameters:	None
Returns:	TRUE or FALSE 
Description:	Is user rif40 or does the user have the rif_user or rif_manager role?
		The user postgres is never a RIF_USER';

\df rif40_sql_pkg.is_rif40_user_manager_or_schema

CREATE OR REPLACE FUNCTION rif40_sql_pkg.is_rif40_manager_or_schema()
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
/*

Function: 	is_rif40_manager_or_schema()
Parameters:	None
Returns:	TRUE or FALSE 
Description:	Is user rif40 or does the user have the rif_manager role?
 */
DECLARE
BEGIN
	IF USER = 'rif40' THEN
		RETURN TRUE;
	ELSIF pg_has_role(USER, 'rif_manager', 'USAGE') THEN
		RETURN TRUE;
	ELSE
		RETURN FALSE;
	END IF;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.is_rif40_manager_or_schema() IS 'Function: 	is_rif40_manager_or_schema()
Parameters:	None
Returns:	TRUE or FALSE 
Description:	Is user rif40 or does the user have the rif_manager role?';

\df rif40_sql_pkg.is_rif40_manager_or_schema

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_create_fdw_table(numerator_table VARCHAR, total_geographies INTEGER, drop_objects BOOLEAN, fdwservername VARCHAR, fdwservertype VARCHAR) 
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_create_fdw_table()
Parameters: 	numerator_table, total_geographies, drop objects (boolean), FDW server name, FDW server type
Returns: 	Nothing
Description:	Attmept if needed to create FDW tables for potential FDW tables i.e. those numerators in RIF40_NUM_DENOM_ERRORS with no local table of the same name 

		ONLY SUPPORTS NUMERATORS - DENOMINATORS ARE EXPECTED TO ALWAYS BE LOCAL
		ONLY SUPPORTS TABLES AT PRESENT - MAY SUPPORT VIEWS AND MATERIALIZED VIEWS IN FUTURE
 */
DECLARE
	c2cfdwt CURSOR(l_table_or_view VARCHAR) FOR 	
		SELECT a.relname AS table_or_view				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a, pg_namespace n 
		 WHERE b.ftrelid  = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND n.nspname  = USER
		   AND n.oid      = a.relnamespace
		   AND a.relname  = LOWER(l_table_or_view);
	c3cfdwt CURSOR(l_table_or_view VARCHAR) FOR 	
		SELECT *
		  FROM fdw_all_tables
		 WHERE table_name = UPPER(l_table_or_view);
	c4cfdwt CURSOR(l_table_or_view VARCHAR) FOR
		SELECT table_name, column_name,
		       CASE
				WHEN data_type = 'VARCHAR2'		 		THEN 'varchar('||data_length||')'
				WHEN data_type = 'CHAR' 				THEN 'char('||data_length||')'
				WHEN data_type = 'NUMBER' AND data_scale = 0 		THEN 'integer'
				WHEN data_type = 'NUMBER' AND data_scale != 0 		THEN 'numeric('||data_precision||','||data_scale||')'
				WHEN data_type = 'NUMBER' AND data_scale IS NULL 	THEN 'double precision'
				WHEN data_type = 'DATE' 				THEN 'timestamp'
				WHEN data_type LIKE 'TIMESTAMP(%) WITH TIME ZONE' 	THEN 'timestamp'
				WHEN data_type LIKE 'TIMESTAMP(%)' 			THEN 'timestamp'
				ELSE NULL	/* NEED TO RAISE AN ERROR HERE - IT WILL CAUSE ONE LATER */
		       END data_defn, data_type, data_length, data_scale, data_precision, column_id, comments
		  FROM fdw_all_tab_columns
		 WHERE table_name  = UPPER(l_table_or_view)
		 ORDER BY column_id;
	c5cfdwt CURSOR(l_table_or_view VARCHAR) FOR 	
		SELECT *
		  FROM rif40_fdw_tables
		 WHERE table_name = UPPER(l_table_or_view);
--
	c2cfdwt_rec RECORD;
	c3cfdwt_rec RECORD;
	c4cfdwt_rec RECORD;
	c5cfdwt_rec RECORD;
--
	create_fdw BOOLEAN:=FALSE;
	sql_stmt VARCHAR[];
	l_sql_stmt VARCHAR;
	column_list VARCHAR[];
	column_defn VARCHAR[];
	column_comment VARCHAR[];
	test_column VARCHAR;
	test VARCHAR;
--
	c_idx INTEGER:=0;
	j INTEGER:=0;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Check if table exists
--
	OPEN c2cfdwt(numerator_table);
	FETCH c2cfdwt INTO c2cfdwt_rec;
	CLOSE c2cfdwt;
--
-- Get records from FDW_ALL_TABLES
--
	OPEN c3cfdwt(numerator_table);
	FETCH c3cfdwt INTO c3cfdwt_rec;
	CLOSE c3cfdwt;
--
-- Get records from RIF40_FDW_TABLES
--
	OPEN c5cfdwt(numerator_table);
	FETCH c5cfdwt INTO c5cfdwt_rec;
	CLOSE c5cfdwt;
--
	IF c2cfdwt_rec.table_or_view IS NOT NULL /* Already exists */ AND drop_objects THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_table', 'Re-creating foreign data wrapper table: %', 
			c2cfdwt_rec.table_or_view::VARCHAR);
		create_fdw:=TRUE;
		sql_stmt[1]:='DROP FOREIGN TABLE '||USER||'.'||LOWER(numerator_table);
	ELSIF c2cfdwt_rec.table_or_view IS NOT NULL /* Already exists */ AND NOT drop_objects AND c5cfdwt_rec.table_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_table', 'Re-creating foreign data wrapper table: %; no entry in RIF40_FDW_TABLES', 
			c2cfdwt_rec.table_or_view::VARCHAR);
		create_fdw:=TRUE;
		sql_stmt[1]:='DROP FOREIGN TABLE '||USER||'.'||LOWER(numerator_table);
	ELSIF c2cfdwt_rec.table_or_view IS NOT NULL /* Already exists */ AND NOT drop_objects THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_table', 'Foreign data wrapper table: % exists; create status: %, date: %', 
			c2cfdwt_rec.table_or_view::VARCHAR, c5cfdwt_rec.create_status::VARCHAR, c5cfdwt_rec.date_created::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_table', 'Creating foreign data wrapper table: %', 
			LOWER(numerator_table)::VARCHAR);
		create_fdw:=TRUE;
	END IF;
--
-- If the table needs to be created, check it is accessible remotely
--
	IF create_fdw THEN
--
		IF c3cfdwt_rec.table_name IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_create_fdw_table', 'Cannot find numerator table % in remote FDW_ALL_TABLES on % server %', 
				LOWER(numerator_table)::VARCHAR, fdwservertype::VARCHAR, fdwservername::VARCHAR);
			create_fdw:=FALSE;
		ELSIF STRPOS(c3cfdwt_rec.owner, '@') > 1 /* Kerberos user */ THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_fdw_table', 'Found numerator table "%".% in remote FDW_ALL_TABLES on % server %', 
				c3cfdwt_rec.owner::VARCHAR, c3cfdwt_rec.table_name::VARCHAR, fdwservertype::VARCHAR, fdwservername::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_fdw_table', 'Found numerator table %.% in remote FDW_ALL_TABLES on % server %', 
				c3cfdwt_rec.owner::VARCHAR, c3cfdwt_rec.table_name::VARCHAR, fdwservertype::VARCHAR, fdwservername::VARCHAR);
		END IF;
	END IF;
--
-- OK, so get list of columns
--
	IF create_fdw THEN
		l_sql_stmt:='CREATE FOREIGN TABLE '||USER||'.'||LOWER(numerator_table)||' ('||E'\n';
		FOR c4cfdwt_rec IN c4cfdwt(numerator_table) LOOP
			column_list[c4cfdwt_rec.column_id]:=c4cfdwt_rec.column_name;
			column_defn[c4cfdwt_rec.column_id]:=LOWER(c4cfdwt_rec.column_name)||E'\t'||c4cfdwt_rec.data_defn;
			column_comment[c4cfdwt_rec.column_id]:=c4cfdwt_rec.comments;
		END LOOP;
		FOR i IN array_lower(column_list, 1) .. array_upper(column_list, 1) LOOP
			IF column_defn[i] IS NOT NULL THEN
				c_idx:=c_idx+1;
				IF c_idx = 1 THEN
					test_column:=column_list[i];
					l_sql_stmt:=l_sql_stmt||E'\t'||column_defn[i];
				ELSE
					l_sql_stmt:=l_sql_stmt||','||E'\n'||E'\t'||column_defn[i];
				END IF;
			END IF;
		END LOOP;
		IF STRPOS(c3cfdwt_rec.owner, '@') > 1 /* Kerberos user */ THEN
			l_sql_stmt:=l_sql_stmt||E'\n'||') SERVER '||fdwservername||' OPTIONS (SCHEMA ''"'||c3cfdwt_rec.owner||'"'', TABLE '''||c3cfdwt_rec.table_name||''')';
		ELSE
			l_sql_stmt:=l_sql_stmt||E'\n'||') SERVER '||fdwservername||' OPTIONS (SCHEMA '''||c3cfdwt_rec.owner||''', TABLE '''||c3cfdwt_rec.table_name||''')';
		END IF;
		sql_stmt[2]:=l_sql_stmt;
		FOR i IN array_lower(column_list, 1) .. array_upper(column_list, 1) LOOP
			sql_stmt[array_upper(sql_stmt, 1)+1]:='COMMENT ON FOREIGN TABLE '||USER||'.'||LOWER(numerator_table)||' IS '''||
				REPLACE(column_comment[i], '''', ''''||'''' /* Escape comments */)||'''';
		END LOOP;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		j:=1;
	END IF;

--
-- Test
--
	IF test_column IS NULL THEN /* Guess year */
		test:=rif40_sql_pkg.rif40_fdw_table_select_test(LOWER(numerator_table), 'YEAR', fdwservername, fdwservertype);
	ELSE
		test:=rif40_sql_pkg.rif40_fdw_table_select_test(LOWER(numerator_table), test_column, fdwservername, fdwservertype);
	END IF;
	IF test IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_fdw_table', 'Foreign data wrapper numerator table: % tested OK', 
			LOWER(numerator_table)::VARCHAR);
		PERFORM rif40_sql_pkg.rif40_update_fdw_tables(
			UPPER(numerator_table) /* table name */, 'C' /* Create status */, NULL::varchar /* Error message */, 1 /* Rowtest passed */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_create_fdw_table', 'returned: %', 
			test::VARCHAR);
		l_sql_stmt:='DROP FOREIGN TABLE '||USER||'.'||LOWER(numerator_table);
		PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		PERFORM rif40_sql_pkg.rif40_update_fdw_tables(
			UPPER(numerator_table) /* table name */, 'E' /* Create status */, test /* Error message */, 0 /* Rowtest NOT passed */);
--
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_create_fdw_table', 'Foreign data wrapper numerator table: % test SELECT returned error', 
			LOWER(numerator_table)::VARCHAR);
	END IF;
--
	RETURN j;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		error_message:='rif40_create_fdw_table() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'for FDW '||
			LOWER(numerator_table)::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_create_fdw_table', 'Caught exception: % [IGNORED]', 
			error_message::VARCHAR);
		RAISE INFO '2: %', error_message;
		PERFORM rif40_sql_pkg.rif40_update_fdw_tables(
			UPPER(numerator_table) /* table name */, 'N' /* Create status */, error_message /* Error message */, 0 /* Rowtest NOT passed */);
		BEGIN
			l_sql_stmt:='DROP FOREIGN TABLE '||USER||'.'||LOWER(numerator_table);
			PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		EXCEPTION
			WHEN others THEN NULL;
		END;
--		RAISE /* the original error */;
--
-- Or you could just return (which is what we will do in the long run)
--
		RETURN 0;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_create_fdw_table(VARCHAR, INTEGER, BOOLEAN, VARCHAR, VARCHAR) IS 'Function: 	rif40_create_fdw_table()
Parameters: 	numerator_table, total_geographies, drop objects (boolean), FDW server name, FDW server type
Returns: 	Nothing
Description:	Attmept if needed to create FDW tables for potential FDW tables i.e. those numerators in RIF40_NUM_DENOM_ERRORS with no local table of the same name 

		ONLY SUPPORTS NUMERATORS - DENOMINATORS ARE EXPECTED TO ALWAYS BE LOCAL
		ONLY SUPPORTS TABLES AT PRESENT - MAY SUPPORT VIEWS AND MATERIALIZED VIEWS IN FUTURE';

\df rif40_sql_pkg.rif40_create_fdw_table

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_update_fdw_tables(l_table_name VARCHAR, create_status VARCHAR, error_message VARCHAR, rowtest_passed INTEGER)
RETURNS void
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_update_fdw_tables()
Parameters: 	Table name, Create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors), error message, rowtest passed (0/1)
Returns: 	Nothing
Description:	MERGE rif40_fdw_tables

 */
DECLARE
	c1ufdw CURSOR(l_table_name VARCHAR) FOR
		SELECT * 
		  FROM rif40_fdw_tables
		 WHERE table_name = UPPER(l_table_name);
--
	c1ufdw_rec RECORD;
--
	sql_stmt VARCHAR;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
	OPEN c1ufdw(l_table_name);
	FETCH c1ufdw INTO c1ufdw_rec;
	CLOSE c1ufdw;
--
	IF c1ufdw_rec.table_name IS NULL THEN
		sql_stmt:='INSERT INTO t_rif40_fdw_tables (create_status, error_message, rowtest_passed, table_name, username) VALUES ($1, $2, $3, $4, $5)';
	ELSE
		sql_stmt:='UPDATE t_rif40_fdw_tables SET create_status=$1, error_message=$2, rowtest_passed=$3 WHERE table_name=$4 AND username = $5';
	END IF;
	EXECUTE sql_stmt USING create_status, error_message, rowtest_passed, UPPER(l_table_name), USER;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		IF sql_stmt IS NULL THEN
			error_message:='rif40_update_fdw_tables() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL> (Not known, probably CURSOR c1ufdw)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		ELSE
			error_message:='rif40_update_fdw_tables() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL>'||sql_stmt::VARCHAR||E'\n'||'Detail: '||v_detail::VARCHAR;
		END IF;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_update_fdw_tables', 'Caught exception: % [IGNORED]', 
			error_message::VARCHAR);
END;

$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_update_fdw_tables(VARCHAR, VARCHAR, VARCHAR, INTEGER) IS 'Function: 	rif40_update_fdw_tables()
Parameters: 	Table name, Create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors), error message, rowtest passed (0/1)
Returns: 	Nothing
Description:	MERGE rif40_fdw_tables
';

\df rif40_sql_pkg.rif40_update_fdw_tables

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_create_fdw_view(drop_objects BOOLEAN, fdwservername VARCHAR, fdwservertype VARCHAR, l_view VARCHAR, l_column VARCHAR, sql_stmt VARCHAR[]) 
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_create_fdw_view()
Parameters: 	Drop objects (boolean), FDW server name, FDW server type, FDW name for remote view, remote column to be tested, arrays of SQL statements
Returns: 	Nothing
Description:	Create support view if needed (FDW_ALL_TABLES, FDW_ALL_TAB_COLUMNS)

Sse rif40_create_fdw_views() for SQL exmaples

This contains Oracle specific code (i.e. data dictionary items ALL_TABLES, ALL_TAB_COLUMNS owned by SYS)

Test user can select from FDW table name using rif40_sql_pkg.rif40_fdw_table_select_test()

 */
DECLARE
	c2cfdwv CURSOR(l_table_or_view VARCHAR) FOR 	
		SELECT a.relname AS table_or_view				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a, pg_namespace n 
		 WHERE b.ftrelid  = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND n.nspname  = USER
		   AND n.oid      = a.relnamespace
		   AND a.relname  = LOWER(l_table_or_view);
--
	c2cfdwv_rec RECORD;
--
	test VARCHAR;
	i INTEGER:=0;
	l_sql_stmt VARCHAR[]:=sql_stmt;
BEGIN
	OPEN c2cfdwv(l_view);
	FETCH c2cfdwv INTO c2cfdwv_rec;
	CLOSE c2cfdwv;
	IF c2cfdwv_rec.table_or_view IS NOT NULL /* Already exists */ AND drop_objects THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_view', 'Re-creating foreign data wrapper view: %', 
			c2cfdwv_rec.table_or_view::VARCHAR);
		l_sql_stmt[1]:='DROP FOREIGN TABLE '||USER||'.'||l_view;
		PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		i:=1;
	ELSIF c2cfdwv_rec.table_or_view IS NOT NULL /* Already exists */ AND NOT drop_objects THEN
		NULL;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_fdw_view', 'Creating foreign data wrapper view: %', 
			l_view::VARCHAR);
		PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		i:=i+1;
	END IF;
--
-- Test
--
	test:=rif40_sql_pkg.rif40_fdw_table_select_test(l_view, l_column, fdwservername, fdwservertype);
	IF test IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_fdw_view', 'Foreign data wrapper view: % tested OK', 
			l_view::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_create_fdw_view', 'returned: %', 
			test::VARCHAR);
		l_sql_stmt:=NULL;
		l_sql_stmt[1]:='DROP FOREIGN TABLE '||USER||'.'||l_view;
		PERFORM rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_create_fdw_view', 'Foreign data wrapper view: % test SELECT returned error', 
			l_view::VARCHAR);
	END IF;
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON  FUNCTION rif40_sql_pkg.rif40_create_fdw_view(BOOLEAN, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) IS 'Function: 	rif40_create_fdw_view()
Parameters: 	Drop objects (boolean), FDW server name, FDW server type, FDW name for remote view, remote column to be tested, arrays of SQL statements
Returns: 	Nothing
Description:	Create support view if needed (FDW_ALL_TABLES, FDW_ALL_TAB_COLUMNS)

Sse rif40_create_fdw_views() for SQL exmaples

This contains Oracle specific code (i.e. data dictionary items ALL_TABLES, ALL_TAB_COLUMNS owned by SYS)

Test user can select from FDW table name using rif40_sql_pkg.rif40_fdw_table_select_test()';

\df rif40_sql_pkg.rif40_create_fdw_view

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_create_fdw_views(drop_objects BOOLEAN, fdwservername VARCHAR, fdwservertype VARCHAR) 
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_create_fdw_views()
Parameters: 	Drop objects (boolean), FDW server name, FDW server type
Returns: 	Nothing
Description:	Create support views if needed (FDW_ALL_TABLES, FDW_ALL_TAB_COLUMNS) using rif40_sql_pkg.rif40_create_fdw_view()

CREATE FOREIGN TABLE fdw_all_tables (
	owner		VARCHAR(30),
	table_name	VARCHAR(30),
	comments	VARCHAR(4000)
) SERVER eph1 OPTIONS (SCHEMA 'SAHSU', TABLE 'RIF_ALL_TABLES');

CREATE FOREIGN TABLE fdw_all_tab_columns (
	owner		VARCHAR(30),
	table_name	VARCHAR(30),
	column_name	VARCHAR(30),
	data_type       VARCHAR(106),
 	data_length     NUMERIC(22,0), 
 	data_precision  NUMERIC(22,0),
 	data_scale      NUMERIC(22,0),
 	column_id       NUMERIC(22,0),
	comments	VARCHAR(4000)
) SERVER eph1 OPTIONS (SCHEMA 'SAHSU', TABLE 'RIF_ALL_TAB_COLUMNS');

This contains Oracle specific code (i.e. data dictionary items ALL_TABLES, ALL_TAB_COLUMNS owned by SYS)

Test user can select from FDW table name

RIF_ALL_TAB_COLUMNS is defined (as user SAHSU) as:

CREATE OR REPLACE VIEW rif_all_tab_columns AS
SELECT t.owner, t.table_name, t.column_name, 
       data_type,
       CAST(data_length AS NUMBER(22,0)) AS data_length,  
       CAST(data_precision AS NUMBER(22,0)) AS data_precision, 
       CAST(data_scale AS NUMBER(22,0)) AS data_scale,
       CAST(column_id AS NUMBER(22,0)) AS column_id, 
       c.comments
  FROM all_tab_columns t
	LEFT OUTER JOIN all_col_comments c ON (c.owner = t.owner AND c.table_name = t.table_name AND c.column_name = t.column_name);
GRANT SELECT ON rif_all_tab_columns TO PUBLIC;
EXECUTE recreate_public_synonym('rif_all_tab_columns');
DESC rif_all_tab_columns
COLUMN data_type FORMAT a20
SELECT  * from rif_all_tab_columns where table_name = 'RIF_CANC_EW_74_ON_ED91';

CREATE OR REPLACE VIEW rif40_all_tables AS
SELECT t.owner, t.table_name, c.comments
  FROM all_tab_columns t
	LEFT OUTER JOIN all_col_comments c ON (c.owner = t.owner AND c.table_name = t.table_name);
GRANT SELECT ON rif40_all_tables TO PUBLIC;
EXECUTE recreate_public_synonym('rif40_all_tables');
DESC rif40_all_tables
SELECT  * from rif40_all_tables where table_name = 'RIF_CANC_EW_74_ON_ED91';

This has been create to define the NUMBER data type exactly (the is a bug in Oracle FDW); and to include the column comments

The ordering of column in Oracle FDW must be the same as in Oracle

 */
DECLARE
	l_view	VARCHAR;
	l_column VARCHAR;
	sql_stmt VARCHAR[];
	i INTEGER:=0;
BEGIN
	l_view:='fdw_all_tables';
	l_column:='table_name';
	sql_stmt[2]:='CREATE FOREIGN TABLE '||USER||'.fdw_all_tables ('||E'\n'||
		E'\t'||'owner		VARCHAR(30),'||E'\n'||
		E'\t'||'table_name	VARCHAR(30),'||E'\n'||
		E'\t'||'comments	VARCHAR(4000)'||E'\n'||
		') SERVER '||fdwservername||' OPTIONS (SCHEMA ''SAHSU'', TABLE ''RIF40_ALL_TABLES'')';
	sql_stmt[3]:='COMMENT ON FOREIGN TABLE '||USER||'.fdw_all_tables IS ''List of remote tables/views/materialized views accessible via foreign data wrapper '||
		fdwservertype||' server '||fdwservername||'. Used to check table exists''';
	sql_stmt[4]:='COMMENT ON COLUMN '||USER||'.fdw_all_tables.owner IS ''Owner of remote table/view/materialized view''';
	sql_stmt[5]:='COMMENT ON COLUMN '||USER||'.fdw_all_tables.table_name IS ''Name of remote table/view/materialized view''';
	sql_stmt[6]:='COMMENT ON COLUMN '||USER||'.fdw_all_tables.comments IS ''Comments''';
--
	i:=i+rif40_sql_pkg.rif40_create_fdw_view(drop_objects, fdwservername, fdwservertype, l_view, l_column, sql_stmt);
--
	l_view:='fdw_all_tab_columns';
	l_column:='table_name';
	sql_stmt:=NULL;
	sql_stmt[2]:='CREATE FOREIGN TABLE '||USER||'.fdw_all_tab_columns ('||E'\n'||
		E'\t'||'owner		VARCHAR(30),'||E'\n'||
		E'\t'||'table_name	VARCHAR(30),'||E'\n'||
		E'\t'||'column_name	VARCHAR(30),'||E'\n'||
		E'\t'||'data_type	VARCHAR(30),'||E'\n'||
		E'\t'||'data_length	NUMERIC(22,0),'||E'\n'||
		E'\t'||'data_precision	NUMERIC(22,0),'||E'\n'||
		E'\t'||'data_scale	NUMERIC(22,0),'||E'\n'||
		E'\t'||'column_id	NUMERIC(22,0),'||E'\n'||
		E'\t'||'comments	VARCHAR(4000)'||E'\n'||
		') SERVER '||fdwservername||' OPTIONS (SCHEMA ''SAHSU'', TABLE ''RIF_ALL_TAB_COLUMNS'')';
	sql_stmt[3]:='COMMENT ON FOREIGN TABLE '||USER||'.fdw_all_tab_columns IS ''List of remote table/view/materialized view columns accessible via foreign data wrapper '||
		fdwservertype||' server '||fdwservername||'. Used to check table exists''';
	sql_stmt[4]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.owner IS ''Owner of remote table/view/materialized view''';
	sql_stmt[5]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.table_name IS ''Name of remote table/view/materialized view''';
	sql_stmt[6]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.column_name IS ''Column name''';
	sql_stmt[7]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.data_type IS ''Data type''';
	sql_stmt[8]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.data_length IS ''Data length''';
	sql_stmt[9]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.data_precision IS ''Data precision''';
	sql_stmt[10]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.data_scale IS ''Data scale''';
	sql_stmt[11]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.column_id IS ''Column id (order)''';
	sql_stmt[12]:='COMMENT ON COLUMN '||USER||'.fdw_all_tab_columns.comments IS ''Comments''';
--
	i:=i+rif40_sql_pkg.rif40_create_fdw_view(drop_objects, fdwservername, fdwservertype, l_view, l_column, sql_stmt);
--
	RETURN i;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_create_fdw_views(BOOLEAN, VARCHAR, VARCHAR) IS 'Function: 	rif40_create_fdw_views()
Parameters: 	Drop objects (boolean), FDW server name, FDW server type
Returns: 	Nothing
Description:	Create support views if needed (FDW_ALL_TABLES, FDW_ALL_TAB_COLUMNS) using rif40_sql_pkg.rif40_create_fdw_view()

CREATE FOREIGN TABLE fdw_all_tables (
	owner		VARCHAR(30),
	table_name	VARCHAR(30),
	comments	VARCHAR(4000)
) SERVER eph1 OPTIONS (SCHEMA ''SAHSU'', TABLE ''RIF_ALL_TABLES'');

CREATE FOREIGN TABLE fdw_all_tab_columns (
	owner		VARCHAR(30),
	table_name	VARCHAR(30),
	column_name	VARCHAR(30),
	data_type       VARCHAR(106),
 	data_length     NUMERIC(22,0), 
 	data_precision  NUMERIC(22,0),
 	data_scale      NUMERIC(22,0),
 	column_id       NUMERIC(22,0),
	comments	VARCHAR(4000)
) SERVER eph1 OPTIONS (SCHEMA ''SAHSU'', TABLE ''RIF_ALL_TAB_COLUMNS'');

This contains Oracle specific code (i.e. data dictionary items ALL_TABLES, ALL_TAB_COLUMNS owned by SYS)

Test user can select from FDW table name

RIF_ALL_TAB_COLUMNS is defined (as user SAHSU) as:

CREATE OR REPLACE VIEW rif_all_tab_columns AS
SELECT t.owner, t.table_name, t.column_name, 
       data_type,
       CAST(data_length AS NUMBER(22,0)) AS data_length,  
       CAST(data_precision AS NUMBER(22,0)) AS data_precision, 
       CAST(data_scale AS NUMBER(22,0)) AS data_scale,
       CAST(column_id AS NUMBER(22,0)) AS column_id, 
       c.comments
  FROM all_tab_columns t
	LEFT OUTER JOIN all_col_comments c ON (c.owner = t.owner AND c.table_name = t.table_name AND c.column_name = t.column_name);
GRANT SELECT ON rif_all_tab_columns TO PUBLIC;
EXECUTE recreate_public_synonym(''rif_all_tab_columns'');
DESC rif_all_tab_columns
COLUMN data_type FORMAT a20
SELECT  * from rif_all_tab_columns where table_name = ''RIF_CANC_EW_74_ON_ED91'';

CREATE OR REPLACE VIEW rif40_all_tables AS
SELECT t.owner, t.table_name, c.comments
  FROM all_tab_columns t
	LEFT OUTER JOIN all_col_comments c ON (c.owner = t.owner AND c.table_name = t.table_name);
GRANT SELECT ON rif40_all_tables TO PUBLIC;
EXECUTE recreate_public_synonym(''rif40_all_tables'');
DESC rif40_all_tables
SELECT  * from rif40_all_tables where table_name = ''RIF_CANC_EW_74_ON_ED91'';

This has been create to define the NUMBER data type exactly (the is a bug in Oracle FDW); and to include the column comments

The ordering of column in Oracle FDW must be the same as in Oracle
';

\df rif40_sql_pkg.rif40_create_fdw_views

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_fdw_table_select_test(l_table_name VARCHAR, l_column_name VARCHAR, fdwservername VARCHAR, fdwservertype VARCHAR) 
RETURNS text
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_fdw_table_select_test()
Parameters: 	Table name, column name, FDW server name, FDW server type
Returns: 	Text of error or NULL if all OK
Description:	Test user can select from FDW table name
		
		SQL> SELECT <column name> AS test FROM <table name> LIMIT 1;

		IF 1 row returned, RETURN NULL
		IF any of the inputs are NULL RETURN custom error message
		IF FDW server name is invalid/not accessible RETURN custom error message
		IF SQL error RETURN SQL error message

 */
DECLARE
	c1_st REFCURSOR;
	c6_st CURSOR(l_fdwservername VARCHAR) FOR 		/* FDW server name check */
		SELECT r.rolname AS srvowner, s.srvname
		  FROM pg_foreign_server s, pg_roles r
		 WHERE s.srvname  = l_fdwservername
		   AND s.srvowner = r.oid;
--
	c6_st_rec RECORD;
--
	test VARCHAR;
	sql_stmt VARCHAR;
	error_message VARCHAR:=NULL;
	l_rows INTEGER:=NULL;
--
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- IF any of the inputs are NULL RETURN custom error message
--
	IF l_table_name IS NULL OR l_column_name IS NULL OR fdwservername IS NULL OR fdwservertype IS NULL THEN
		error_message:='rif40_fdw_table_select_test() C209xx: NULL Table name, column name, FDW server name or FDW server type';
	END IF;
	sql_stmt:='SELECT '||l_column_name||'::VARCHAR AS test FROM '||USER||'.'||l_table_name||' LIMIT 1';
--
-- Check access to FDWServerName
--
	IF error_message IS NULL THEN
		OPEN c6_st(fdwservername);
		FETCH c6_st INTO c6_st_rec;
		CLOSE c6_st;
		IF c6_st_rec.srvowner IS NULL THEN
			error_message:='rif40_fdw_table_select_test() FDW functionality disabled - FDWServerName: '||fdwservername::VARCHAR||' not found';
		ELSIF has_server_privilege(c6_st_rec.srvowner, c6_st_rec.srvname, 'USAGE') THEN
			NULL;
		ELSE
			error_message:='rif40_fdw_table_select_test() FDW functionality disabled - no access to FDWServerName: '||c6_st_rec.srvowner::VARCHAR||'.'||c6_st_rec.srvname::VARCHAR;
		END IF;
	END IF;
--
-- Run SELECT test
--
	IF error_message IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_fdw_table_select_test', 'SQL> %;', 
			sql_stmt::VARCHAR);
		OPEN c1_st FOR EXECUTE sql_stmt;
		FETCH c1_st INTO test;
		GET DIAGNOSTICS l_rows = ROW_COUNT;
		CLOSE c1_st;
--
		IF l_rows IS NULL THEN
			error_message:='rif40_fdw_table_select_test() C209xx: NULL ROW_COUNT in: '||E'\n'||'SQL> '||sql_stmt||E'\n'||'for FDW '||
				l_table_name::VARCHAR||'.'||l_column_name::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR;
		ELSIF l_rows = 0 THEN
			error_message:='rif40_fdw_table_select_test() C209xx: 0 ROW_COUNT in: '||E'\n'||'SQL> '||sql_stmt||E'\n'||'for FDW '||
				l_table_name::VARCHAR||'.'||l_column_name::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR;
		ELSIF l_rows > 1 THEN
			error_message:='rif40_fdw_table_select_test() C209xx: Non zero ('||l_rows||') ROW_COUNT in: '||E'\n'||'SQL> '||sql_stmt||E'\n'||'for FDW '||
				l_table_name::VARCHAR||'.'||l_column_name::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR;
		END IF;
	END IF;
--
	IF error_message IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_fdw_table_select_test', 'SQL> %; %', 
			sql_stmt::VARCHAR, error_message::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_fdw_table_select_test', 'OK SQL> %;', 
			sql_stmt::VARCHAR);
	END IF;
--
	RETURN error_message;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		error_message:='rif40_fdw_table_select_test() caught: '||E'\n'||SQLERRM::VARCHAR||' in: '||E'\n'||'SQL> '||sql_stmt::VARCHAR||E'\n'||'for FDW '||
			l_table_name::VARCHAR||'.'||l_column_name::VARCHAR||' on '||fdwservertype::VARCHAR||' server '||fdwservername::VARCHAR||E'\n'||'Detail: '||v_detail::VARCHAR;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_fdw_table_select_test', 'Caught exception: % [IGNORED]', 
			error_message::VARCHAR);
		RETURN error_message;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_fdw_table_select_test(VARCHAR, VARCHAR, VARCHAR, VARCHAR)  IS 'Function: 	rif40_fdw_table_select_test()
Parameters: 	Table name, column name, FDW server name, FDW server type
Returns: 	Text of error or NULL if all OK
Description:	Test user can select from FDW table name
		
		SQL> SELECT <column name> AS test FROM <table name> LIMIT 1;

		IF 1 row returned, RETURN NULL
		IF any of the inputs are NULL RETURN custom error message
		IF FDW server name is invalid/not accessible RETURN custom error message
		IF SQL error RETURN SQL error message
';

\df rif40_sql_pkg.rif40_fdw_table_select_test

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_get_function_arg_types(arg_type_list oid[]) 
RETURNS text
SECURITY INVOKER
AS $func$
/*

Function: 	rif40_get_function_arg_types()
Parameters: 	arg_type_list (pg_types.oid)
Returns: 	Text
Description:	Returns function signature e.g (character varying, character varying)

		Similar to pg_get_function_identity_arguments() without the function names
		For use with pg_proc
 */
DECLARE
	ret TEXT;
	j INTEGER:=0;
BEGIN
	ret:='(';
--
	IF arg_type_list IS NOT NULL THEN
		FOR i IN array_lower(arg_type_list, 1) .. array_upper(arg_type_list, 1) LOOP
			IF i = array_lower(arg_type_list, 1) THEN
				ret:=ret||format_type(arg_type_list[i], NULL);
			ELSE
				ret:=ret||', '||format_type(arg_type_list[i], NULL);
			END IF;
			j:=j+1;
		END LOOP;
	END IF;
--
	ret:=ret||')';
	PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_get_function_arg_types', 'args: %, signature: %', 
		j::VARCHAR, ret::VARCHAR);
	RETURN ret;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_get_function_arg_types(oid[]) IS 'Function: 	rif40_get_function_arg_types()
Parameters: 	arg_type_list (pg_types.oid)
Returns: 	Text
Description:	Returns function signature e.g (character varying, character varying)

		Similar to pg_get_function_identity_arguments() without the function names
		For use with pg_proc';

\df rif40_sql_pkg.rif40_get_function_arg_types

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_num_denom_validate(l_geography VARCHAR, l_table_name VARCHAR)
RETURNS integer
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_num_denom_validate()
Parameters: 	geography, table/view/foreign table name
Returns: 	1 - table/view/foreign table has all geolevels present in geography, 0 - table/view/foreign table has some/all geolevels missing in geography
Description:	Validate numerator or denominator geolevels

 */
DECLARE
	c1ndv CURSOR(l_geography VARCHAR, l_owner VARCHAR, l_table VARCHAR) FOR
/*		WITH all_tab_columns AS (
			SELECT UPPER(a.tablename) AS tablename, 
			       UPPER(b.attname) AS columnname, 
				   UPPER(schemaname) AS schemaname	/- Tables -/
			  FROM pg_tables a, pg_attribute b, pg_class c
			 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.tablename
			   AND c.relkind    = 'r' 				/- Relational table -/
			   AND c.relpersistence IN ('p', 'u') 	/- Persistence: permanent/unlogged -/ 
			 UNION
			SELECT UPPER(a.viewname) AS tablename, 
			       UPPER(b.attname) AS columnname, 
				   UPPER(schemaname) AS schemaname	/- Views -/
			  FROM pg_views a, pg_attribute b, pg_class c
			 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.viewname
			   AND c.relkind    = 'v' 				/- View -/
		) */
		SELECT COUNT(l.geolevel_name) total_geolevels,
    		   COUNT(c.column_name) total_columns 
		  FROM t_rif40_geolevels l
			LEFT OUTER JOIN information_schema.columns c ON (
				c.table_schema = LOWER(l_owner)			AND 
				c.table_name   = LOWER(l_table)			AND 
				c.column_name  = LOWER(l.geolevel_name))
		 WHERE geography  = l_geography
   		   AND resolution = 1;
	c1_rec RECORD;
--
	l_owner	VARCHAR:=NULL;
	l_table	VARCHAR:=NULL;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_num_denom_validate', 'User % must be rif40 or have rif_user or rif_manager role', 
				USER::VARCHAR);
		ELSE		
			RETURN 0;
		END IF;
	END IF;
--
-- If inputs are NULL return 0
--
	IF l_geography IS NULL OR l_table_name IS NULL THEN
		RETURN 0;
	END IF;
--
-- Resolve schema owner of table (oracle version of this has to deal with synonyms as well)
--
	l_owner:=UPPER(rif40_sql_pkg.rif40_object_resolve(l_table_name));
--
-- Check geolevels
--
	IF l_owner IS NOT NULL THEN
		OPEN c1ndv(l_geography, l_owner, l_table_name);
		FETCH c1ndv INTO c1_rec;
		CLOSE c1ndv;
		
		IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_num_denom_validate', '[%,%.%] total_geolevels (in geography): %; total_columns (in table/view/foreign table; matching geolevels columns): %', 
				l_geography::VARCHAR, l_owner::VARCHAR, l_table_name::VARCHAR, c1_rec.total_geolevels::VARCHAR, c1_rec.total_columns::VARCHAR);
		END IF;
		IF c1_rec.total_geolevels = c1_rec.total_columns THEN 
			RETURN 1;		/* Validated */
		END IF;
	END IF;
--
	RETURN 0;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON  FUNCTION rif40_sql_pkg.rif40_num_denom_validate(VARCHAR, VARCHAR) IS 'Function: 	rif40_num_denom_validate()
Parameters: 	geography, table/view/foreign table name
Returns: 	1 - table/view/foreign table has all geolevels present in geography, 0 - table/view/foreign table has some/all geolevels missing in geography
Description:	Validate numerator or denominator geolevels';

\df rif40_sql_pkg.rif40_num_denom_validate

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_is_object_resolvable(l_table_name VARCHAR)
RETURNS integer
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_is_object_resolvable()
Parameters:	Table/view name
Returns: 	1 - resolvable and accessible, 0 - NOT
Description:	Is object resolvable?

Search search path for table/view/foreign table; check resolvable

Will need OracleFDW objects to check remote access

 */
DECLARE
	c1resolv CURSOR FOR
		SELECT REPLACE(regexp_split_to_table(setting, E'\\s+'), ',', '') AS schemaname
	 	  FROM pg_settings
		 WHERE name = 'search_path';
	c2 CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR
		SELECT schemaname, tablename
		  FROM pg_tables
		 WHERE schemaname = l_schema
		   AND tablename  = LOWER(l_table)
		 UNION
		SELECT schemaname, viewname AS tablename
		  FROM pg_views
		 WHERE schemaname = l_schema
		   AND viewname   = LOWER(l_table)
	 	 UNION
		SELECT n.nspname schemaname, a.relname tablename				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.ftrelid = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND n.nspname  = USER
		   AND n.nspname  = l_schema
		   AND a.relname  = LOWER(l_table);
--
	c1_rec RECORD;
	c2_rec RECORD;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_is_object_resolvable', 'User % must be rif40 or have rif_user or rif_manager role', 
				USER::VARCHAR);
		ELSE 
			RETURN 0;
		END IF;
	END IF;
--
-- If inputs are NULL return 0
--
	IF l_table_name IS NULL THEN
		RETURN 0;
	END IF;
--
	FOR c1_rec IN c1resolv LOOP
		OPEN c2(c1_rec.schemaname, l_table_name);
		FETCH c2 INTO c2_rec;
--
		IF c2_rec.tablename IS NOT NULL AND has_schema_privilege(USER, c1_rec.schemaname, 'USAGE') AND 
		   has_table_privilege(USER, c1_rec.schemaname||'.'||c2_rec.tablename, 'SELECT') /* or view or foreign table */ THEN
			CLOSE c2;
			RETURN 1; /* Resolvable */
		ELSE /* Nor resolvable */
			IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
				IF c2_rec.tablename IS NULL THEN
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_is_object_resolvable', 'No table/view/foreign table: %.%', 
						c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
				ELSIF c2_rec.tablename IS NOT NULL AND NOT has_schema_privilege(USER, c1_rec.schemaname, 'USAGE') THEN
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_is_object_resolvable', 'No access to schema % for table/view/foreign table: %.%', 
						c1_rec.schemaname::VARCHAR, c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
				ELSE
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_is_object_resolvable', 'No access to: %.%', 
						c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
				END IF;
			END IF;
		END IF;
		CLOSE c2;
	END LOOP;
--
	RETURN 0;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_is_object_resolvable(VARCHAR) IS 'Function: 	rif40_is_object_resolvable()
Parameters:	Table/view name
Returns: 	1 - resolvable and accessible, 0 - NOT
Description:	Is object resolvable?

Search search path for table/view/foreign table; check resolvable

Will need OracleFDW objects to check remote access';

\df rif40_sql_pkg.rif40_is_object_resolvable

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_object_resolve(l_table_name VARCHAR)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_object_resolve()
Parameters:	Table/view name (forced into lower case)
Returns: 	Schema owner for first object found with name in search path
Description:	Is object resolvable?

Search search path for table/view/foreign table

 */
DECLARE
	c1or CURSOR FOR
		SELECT REPLACE(regexp_split_to_table(setting, E'\\s+'), ',', '') AS schemaname
	 	  FROM pg_settings
		 WHERE name = 'search_path';
	c2or CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR
		SELECT schemaname, tablename
		  FROM pg_tables
		 WHERE schemaname = l_schema
		   AND tablename  = LOWER(l_table)
		 UNION
		SELECT schemaname, viewname AS tablename
		  FROM pg_views
		 WHERE schemaname = l_schema
		   AND viewname   = LOWER(l_table)
	 	 UNION
		SELECT n.nspname schemaname, a.relname tablename				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.ftrelid = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND n.nspname  = l_schema
		   AND a.relname  = LOWER(l_table);
--
	c1_rec RECORD;
	c2_rec RECORD;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_object_resolve', 'User % must be rif40 or have rif_user or rif_manager role', 
				USER::VARCHAR);
		ELSE 
			RETURN 0;
		END IF;	
	END IF;
--
-- If inputs are NULL return 0
--
	IF l_table_name IS NULL THEN
		RETURN NULL;
	END IF;
--
	FOR c1_rec IN c1or LOOP
		OPEN c2or(c1_rec.schemaname, l_table_name);
		FETCH c2or INTO c2_rec;
--
		IF c2_rec.tablename IS NOT NULL AND has_schema_privilege(USER, c1_rec.schemaname, 'USAGE') /* or view or foreign table */ THEN
			CLOSE c2or;
			RETURN c1_rec.schemaname; /* Resolvable */
		ELSE /* Nor resolvable */
			IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
				IF c2_rec.tablename IS NULL THEN
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_object_resolve', 'No table/view/foreign table: %.%', 
						c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
				ELSIF c2_rec.tablename IS NOT NULL AND NOT has_schema_privilege(USER, c1_rec.schemaname, 'USAGE') THEN
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_object_resolve', 'No access to schema % for table/view/foreign table: %.%', 
						c1_rec.schemaname::VARCHAR, c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
				ELSE
					PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_object_resolve', 'No access to: %.%', 
						c1_rec.schemaname::VARCHAR, LOWER(l_table_name)::VARCHAR);
				END IF;
			END IF;
		END IF;
		CLOSE c2or;
	END LOOP;
--
	RETURN NULL;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_object_resolve(VARCHAR) IS 'Function: 	rif40_object_resolve()
Parameters:	Table/view name
Returns: 	Schema owner for first object found with name in search path
Description:	Is object resolvable?

Search search path for table/view/foreign table
';

\df rif40_sql_pkg.rif40_object_resolve

\i ../PLpgsql/rif40_sql_pkg/rif40_auto_indirect_checks.sql

--
-- Oracle compatibility objects
--
CREATE OR REPLACE FUNCTION rif40_sql_pkg.sys_context(namespace VARCHAR, parameter VARCHAR) 
RETURNS VARCHAR 
SECURITY INVOKER
AS
$func$
/*

Function: 	sys_context()
Parameters:	Namespace, parameter
Returns:	TIMESTAMP
Description:	Oracle compatability function SYS_CONTEXT
		Namespace is ignored unless it is SAHSU_CONTEXT when the role is checked to see if it was GRANTED the ROLE
		Parameter must be one of: DB_NAME, CURRENT_SCHEMA, AUDSID

		Used to support auditing

		Could be extended to use FDW to get the real AUDSID
 */
DECLARE
	ret	VARCHAR;
	c1syscon CURSOR FOR
		SELECT pid /* Procpid in 9.2 */ ||'.'||TO_CHAR(backend_start, 'J.SSSS.US') audsid 
			/* Backend PID.Julian day.Seconds from midnight.uSeconds (backend start) */
		  FROM pg_stat_activity
		 WHERE datname     = current_database()
		   AND usename     = session_user
		   AND client_addr = inet_client_addr()
		   AND client_port = inet_client_port();		
BEGIN
--
-- Emulate Oracle security contexts by returning YES if the user has the role of the same name
--
	IF namespace = 'SAHSU_CONTEXT' THEN 
		IF pg_has_role(LOWER(parameter), 'USAGE') THEN
			RETURN 'NO';
		ELSE
			RETURN 'YES';
		END IF;
	ELSE
		IF parameter = 'DB_NAME' THEN 
			ret:=current_database();
		ELSIF parameter = 'CURRENT_SCHEMA' THEN 
			ret:=current_schema();
		ELSIF parameter = 'AUDSID' OR parameter = 'SESSIONID' THEN 
			OPEN c1syscon;
			FETCH c1syscon INTO ret;
			CLOSE c1syscon;
		ELSIF namespace IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'sys_context', 'namespace: NULL invalid parameter: %', 
				parameter::VARCHAR);

		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'sys_context', 'namespace: % invalid parameter: %', 
				namespace::VARCHAR, parameter::VARCHAR);
		END IF;
	END IF;
--
	RETURN ret;
END;
$func$ LANGUAGE plpgsql;
COMMENT ON FUNCTION rif40_sql_pkg.sys_context(VARCHAR, VARCHAR) IS 'Replacement for Oracle SYS_CONTEXT(namespace, parameter). Only limited parameters are supported, namespace is ignored.';

\df rif40_sql_pkg.sys_context

SELECT rif40_sql_pkg.sys_context(NULL::VARCHAR, 'DB_NAME');
SELECT rif40_sql_pkg.sys_context(NULL::VARCHAR, 'CURRENT_SCHEMA');
SELECT rif40_sql_pkg.sys_context(NULL::VARCHAR, 'AUDSID');

CREATE OR REPLACE FUNCTION rif40_sql_pkg.systimestamp(tprecision BIGINT) RETURNS TIMESTAMP
AS
$func$
/*

Function: 	systimestamp()
Parameters:	Precision
Returns:	TIMESTAMP
Description:	Oracle compatability function SYSTIMESTAMP

 */
DECLARE
BEGIN
	RETURN current_time;
END;
$func$ LANGUAGE plpgsql;
COMMENT ON FUNCTION rif40_sql_pkg.systimestamp(tprecision BIGINT) IS 'Replacement for Oracle SYSTIMESTAMP. Precision is not used';

\df rif40_sql_pkg.systimestamp

SELECT rif40_sql_pkg.SYS_CONTEXT('SAHSU_CONTEXT', 'RIF_STUDENT');

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_drop_user_table_or_view(table_or_view VARCHAR)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_drop_user_table_or_view()
Parameters:	Table or view name
Returns:	Nothing
Description:	Drops users table, view or temporary table if it exists. Trucates table first.
 */
DECLARE
	c1dut CURSOR(l_table_or_view VARCHAR) FOR 		/* User objects */
		SELECT TRUE AS truncate_table, TRUE AS is_table, tablename AS table_or_view		/* Local tables */
		  FROM pg_tables
		 WHERE tableowner = USER
		   AND schemaname = USER
		   AND tablename  = l_table_or_view
		 UNION
		SELECT FALSE AS truncate_table, FALSE AS is_table, viewname AS table_or_view 		/* Local views */
		  FROM pg_views
		 WHERE viewowner  = USER
		   AND schemaname = USER
		   AND viewname   = l_table_or_view
		 UNION
		SELECT FALSE AS truncate_table, TRUE AS is_table, a.relname AS table_or_view 		/* Temporary tables */
 		 FROM pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relkind        = 'r' 				/* Relational table */
		   AND a.relpersistence = 't' 				/* Persistence: temporary */
		   AND a.relowner       = r.oid
		   AND n.nspname        LIKE 'pg_temp%'
		   AND a.relname        = l_table_or_view
		ORDER BY 1;
	c1dut_rec  RECORD;
--
	sql_stmt 	VARCHAR;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
	v_version BOOLEAN:=(string_to_array(version()::Text, ' '::Text))[2] >= '9.2'; /* Check Postgres server version */
BEGIN
	IF table_or_view IS NULL THEN 
		RETURN;
	END IF;
--
	OPEN c1dut(table_or_view);
	FETCH c1dut INTO c1dut_rec;
	IF NOT FOUND THEN /* Does not exist, ignore */
		CLOSE c1dut;
		RETURN; 
	END IF;
	CLOSE c1dut;
--
--  Bin object
--
	IF c1dut.truncate_table THEN
		sql_stmt:='TRUNCATE TABLE '||table_or_view;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_drop_user_table_or_view', 'SQL> %;', 
			sql_stmt::VARCHAR);
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END IF;
	IF c1dut.is_table THEN
		sql_stmt:='DROP TABLE '||table_or_view;
	ELSE
		sql_stmt:='DROP VIEW '||table_or_view;
	END IF;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_drop_user_table_or_view', 'SQL> %;', 
		sql_stmt::VARCHAR);
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		IF v_version THEN
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		END IF;
		error_message:='rif40_drop_user_table_or_view() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
--
		RAISE;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_drop_user_table_or_view(VARCHAR) IS 'Function: 	rif40_drop_user_table_or_view()
Parameters:	Table or view name
Returns:	Nothing
Description:	Drops users table, view or temporary table if it exists. Trucates table first.';

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_does_role_exist(role VARCHAR)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_does_role_exist()
Parameters:	Database role
Returns:	TRUE or FALSE
Description:	Check if database role exists
 */
DECLARE
	c1dre CURSOR(l_role VARCHAR) FOR /* Extra table/view columns */
		SELECT rolname
		  FROM pg_roles
		 WHERE rolname = l_role;
	c1dre_rec RECORD;
BEGIN
	IF role IS NULL THEN 
		RETURN FALSE;
	END IF;
--
	OPEN c1dre(role);
	FETCH c1dre INTO c1dre_rec;
	IF NOT FOUND THEN /* Does not exist, ignore */
		CLOSE c1dre;
		RETURN FALSE; 
	END IF;
	CLOSE c1dre;
--
	RETURN TRUE;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_does_role_exist(VARCHAR) IS 'Function: 	rif40_does_role_exist()
Parameters:	Database role
Returns:	TRUE or FALSE
Description:	Check if database role exists.';

SELECT rif40_sql_pkg.rif40_does_role_exist('rif_student');

\i ../PLpgsql/rif40_sql_pkg/rif40_method4.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_ddl.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_hash_partition.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_hash.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_hash_bucket_check.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_hash_partition_create.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_hash_partition_create_insert.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_hash_partition_functional_index.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_range_partition.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_range_partition_create_insert.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_range_partition_create.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_partition_count.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_drop_master_trigger.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_common_partition_triggers.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_common_partition_create_setup.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_common_partition_create_complete.sql
\i ../PLpgsql/rif40_sql_pkg/_rif40_common_partition_create.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_startup.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_table_diff.sql
\i ../PLpgsql/rif40_sql_pkg/rif40_sql_test.sql

--
-- Add DDL checks (now run separately)
--
--\i ../PLpgsql/v4_0_rif40_sql_pkg_ddl_checks.sql 

/*
DO LANGUAGE plpgsql $$
BEGIN
	PERFORM rif40_sql_pkg.rif40_method4('SELECT * 
  FROM rif40_geographies', 'Test');
END;
$$;
 */
GRANT SELECT ON rif40_sql_pkg.user_role_privs TO rif_user;
GRANT SELECT ON rif40_sql_pkg.user_role_privs TO rif40;

GRANT EXECUTE ON FUNCTION rif40_sql_pkg.does_role_exist(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.does_role_exist(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_does_role_exist(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_does_role_exist(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_drop_user_table_or_view(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_drop_user_table_or_view(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_method4(VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_method4(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.systimestamp(BIGINT) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.systimestamp(BIGINT) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR[]) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR[]) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.sys_context(VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.sys_context(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_startup(BOOLEAN) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_startup(BOOLEAN) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_num_denom_validate(VARCHAR, VARCHAR) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(VARCHAR) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_is_object_resolvable(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_is_object_resolvable(VARCHAR) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_object_resolve(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_object_resolve(VARCHAR) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.is_rif40_manager_or_schema() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.is_rif40_manager_or_schema() TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.is_rif40_user_manager_or_schema() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.is_rif40_user_manager_or_schema() TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_get_function_arg_types(oid[]) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_get_function_arg_types(oid[]) TO PUBLIC;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_table(VARCHAR, INTEGER, BOOLEAN, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_table(VARCHAR, INTEGER, BOOLEAN, VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_views(BOOLEAN, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_views(BOOLEAN, VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_view(BOOLEAN, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_create_fdw_view(BOOLEAN, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_fdw_table_select_test(VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_fdw_table_select_test(VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_update_fdw_tables(VARCHAR, VARCHAR, VARCHAR, INTEGER) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_update_fdw_tables(VARCHAR, VARCHAR, VARCHAR, INTEGER) TO rif_user, rif_manager;

\echo Created PG psql code (SQL and Oracle compatibility processing).

--
-- Eof
