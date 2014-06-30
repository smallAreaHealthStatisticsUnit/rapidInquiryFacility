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
-- Rapid Enquiry Facility (RIF) - Dynamic DDL implementation
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

CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_explain_ddl(sql_stmt VARCHAR)
RETURNS TABLE(explain_line	TEXT)
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_explain_ddl()
Parameters:	SQL statement
Returns: 	TABLE of explain_line
Description:	Coerce EXPLAIN output into a table with a known column
		Supports EXPLAIN and EXPLAIN ANALYZE as text ONLY
 */
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager role
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, '_rif40_explain_ddl', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	RETURN QUERY EXECUTE sql_stmt;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg._rif40_explain_ddl(VARCHAR) IS 'Function: 	_rif40_explain_ddl()
Parameters:	SQL statement
Returns: 	TABLE of explain_line
Description:	Coerce EXPLAIN output into a table with a known column. 
		Supports EXPLAIN and EXPLAIN ANALYZE as text ONLY.';

\df rif40_sql_pkg._rif40_explain_ddl

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl(sql_stmt VARCHAR)
RETURNS integer
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_ddl()
Parameters:	SQL statement
Returns:	Rows
Description:	Log and execute SQL (rif40 schema create version)
		If NULL SQL raises no data exception (02000) with message "ERROR:  rif40_ddl() Null SQL statement"
		Supports EXPLAIN and EXPLAIN ANALYZE as text ONLY

 */
DECLARE
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
	l_rows INTEGER:=NULL;
	l_pos INTEGER:=NULL;
--
	explain_rec	RECORD;
	explain_text	VARCHAR;
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager role
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_ddl', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	IF sql_stmt IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl', 'Null SQL statement');
		RAISE SQLSTATE '02000' /* No data found */ USING MESSAGE='rif40_ddl() Null SQL statement';
	END IF;
--
	stp:=clock_timestamp();
	l_pos:=position('EXPLAIN' IN UPPER(sql_stmt));
	IF l_pos = 1 THEN /* EXPLAIN ANALYZE statement */
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'EXPLAIN SQL> %;', sql_stmt::VARCHAR);
		sql_stmt:='SELECT explain_line FROM rif40_sql_pkg._rif40_explain_ddl('||quote_literal(sql_stmt)||')';
		FOR explain_rec IN EXECUTE sql_stmt LOOP
			IF explain_text IS NULL THEN
				explain_text:=explain_rec.explain_line::VARCHAR;
			ELSE
				explain_text:=explain_text||E'\n'||explain_rec.explain_line::VARCHAR;
			END IF;
		END LOOP;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', '%', explain_text::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'SQL> %;', sql_stmt::VARCHAR);
		EXECUTE sql_stmt;
	END IF;
	GET DIAGNOSTICS l_rows = ROW_COUNT;
	etp:=clock_timestamp();
	took:=age(etp, stp);
--	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'Statement took: % (%)', 
--			took::VARCHAR, EXTRACT ('epoch' FROM took)::VARCHAR);
	if (EXTRACT ('epoch' FROM took) > 1) THEN
		IF l_rows IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'Statement took: %', 
				took::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_ddl', 'Statement took: %, proccessed % rows', 
				took::VARCHAR, l_rows::VARCHAR);
		END IF;
	ELSE
		IF l_rows IS NULL THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_ddl', 'Statement took: %', 
				took::VARCHAR);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_ddl', 'Statement took: %, proccessed % rows', 
				took::VARCHAR, l_rows::VARCHAR);
		END IF;
	END IF;
--
	RETURN l_rows;
EXCEPTION
	WHEN SQLSTATE '02000' /* No data found */ THEN
		RAISE;
	WHEN others THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_ddl', 'SQL in error (%)> %;', 
			SQLSTATE::VARCHAR /* SQL error state */,
			sql_stmt::VARCHAR /* SQL statement */); 
		RAISE;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR) IS 'Function: 	rif40_ddl()
Parameters:	SQL statement
Returns:	Rows
Description:	Log and execute SQL (rif40 schema create version)
';

CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_ddl(sql_stmt VARCHAR[])
RETURNS integer
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_ddl()
Parameters:	SQL statement array (0+ statements; not multiline statements)
Returns:	Rows
Description:	Log and execute SQL (rif40 schema create version) - ARRAY VERSION

 */
DECLARE
	l_rows INTEGER:=NULL;
	l2_rows INTEGER:=0;
--
	l_sql_stmt VARCHAR;
	l_idx INTEGER;
BEGIN
--
	IF sql_stmt IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-20997, 'rif40_ddl', 'Null SQL statement array');
	END IF;
--
	FOR i IN array_lower(sql_stmt, 1) .. array_upper(sql_stmt, 1) LOOP
		l_idx:=i;
		l_sql_stmt:=sql_stmt[l_idx];
		l_rows:=rif40_sql_pkg.rif40_ddl(l_sql_stmt);
		IF l_rows IS NOT NULL THEN
			l2_rows:=l2_rows+l_rows;
		END IF;
	END LOOP;
--
	RETURN l2_rows;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_ddl(VARCHAR[]) IS 'Function: 	rif40_ddl()
Parameters:	SQL statement array (0+ statements; not multiline statements)
Returns:	Rows
Description:	Log and execute SQL (rif40 schema create version) - ARRAY VERSION
';

\df rif40_sql_pkg.rif40_ddl

--
-- Eof
