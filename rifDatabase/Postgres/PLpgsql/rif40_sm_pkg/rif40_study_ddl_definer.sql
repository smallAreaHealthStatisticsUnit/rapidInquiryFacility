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
-- Rapid Enquiry Facility (RIF) - PG psql code (state machine and extract SQL generation)
--								  rif40_study_ddl_definer()
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
--
-- rif40_study_ddl_definer								56400 to 56599
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_study_ddl_definer(study_id INTEGER, username VARCHAR, audsid VARCHAR, ddl_stmts VARCHAR[])
RETURNS BOOLEAN
SECURITY DEFINER /* i.e. RIF40 */
AS $func$
DECLARE
/*
Function:	rif40_study_ddl_definer()
Parameter:	Study ID, username [study owner], audsid, DDL statements to execute
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Execute DDL statements as RIF40 (so the user cannot control access to extracted data)
		Log statements. Stop and return FALSE on error
Notes:

1. SQL created runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
2. Verify AUDSID and USER

	Verify AUDSID and USER
	Study AUDSID and function AUDSID must be the same
	Study username and session username must be the same
	Study username and function username must be the same
	Study AUDSID and session AUDSID must be the same
	Confirm calling function is rif40_create_extract() [needs 9.2]

   Note that this currently limits this function to being executed by rif40_create_extract()

Issues:

Fix statement type in view/make it an integer

psql:v4_0_sahsuland_examples.sql:178: ERROR:  column "statement_type" is of type smallint but expression is of type text
LINE 11:     NEW.statement_type -* no default value *-,
             ^
HINT:  You will need to rewrite or cast the expression.
QUERY:  INSERT INTO t_rif40_study_sql (
                                username,
                                study_id,
                                statement_type,
                                statement_number,
                                sql_text,
                                line_number)
                        VALUES(
                                coalesce(NEW.username, "current_user"()),
                                coalesce(NEW.study_id, (currval('rif40_study_id_seq'::regclass))::integer),
                                NEW.statement_type -* no default value *-,
                                NEW.statement_number -* no default value *-,
                                NEW.sql_text -* no default value *-,
                                NEW.line_number -* no default value *-)
CONTEXT:  PL/pgSQL function "trgf_rif40_study_sql" line 8 at SQL statement
SQL statement "INSERT INTO rif40_study_sql(statement_type, statement_number, sql_text, line_number)
                VALUES (1::INTEGER -* Local Postgres statement *-, t_ddl, sql_stmt, 1::INTEGER)"
PL/pgSQL function "rif40_create_extract" line 193 at SQL statement

 */
	c1stddl CURSOR(l_study_id INTEGER) FOR
		SELECT MAX(statement_number) AS max_statement_number
		  FROM t_rif40_study_sql_log a
		 WHERE l_study_id = a.study_id;
	c2stddl CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM t_rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1_rec RECORD;
	c2_rec RECORD;
--
	sql_stmt 	VARCHAR;
	t_ddl		INTEGER:=0;
	l_rows		INTEGER:=0;
	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
	etp		TIMESTAMP WITH TIME ZONE;
	elapsed_time	FLOAT:=0;
	l_log_message	VARCHAR:='OK';
	l_sqlcode	VARCHAR:='00000';
--
-- INSERT INTO t_rif_study_sql (view wont work until statement_type is fixed
--
-- 1 CREATE
-- 2 INSERT
-- 3 POST_INSERT
-- 4 NUMERATOR_CHECK
-- 5 DENOMINATOR_CHECK
--
	l_statement_type VARCHAR:='CREATE' /* CREATE: Local Postgres statement */;
	session_audsid	VARCHAR:=SYS_CONTEXT('USERENV', 'SESSIONID');
BEGIN
--
-- Verify AUDSID and USER
-- Study AUDSID and function AUDSID must be the same
-- Study username and session username must be the same
-- Study username and function username must be the same
-- Study AUDSID and session AUDSID must be the same
-- 
-- Note that this currently limits this function to being executed by rif40_run_study()
--
	OPEN c2stddl(study_id);
	FETCH c2stddl INTO c2_rec;
	IF NOT FOUND THEN
		CLOSE c2stddl;
		PERFORM rif40_log_pkg.rif40_error(-56400, 'rif40_study_ddl_definer', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c2stddl;
	IF c2_rec.audsid != audsid THEN
		PERFORM rif40_log_pkg.rif40_error(-56401, 'rif40_study_ddl_definer', 
			'Study ID % wrong audsid, expecting: %; got: %',
			study_id::VARCHAR		/* Study ID */,
			c2_rec.audsid::VARCHAR		/* Record AUDSID */,
			audsid::VARCHAR			/* Function AUDSID */);
	ELSIF c2_rec.username != username THEN
		PERFORM rif40_log_pkg.rif40_error(-56402, 'rif40_study_ddl_definer', 
			'Study ID % wrong username, expecting: %; got: %',
			study_id::VARCHAR		/* Study ID */,
			c2_rec.username::VARCHAR	/* Record USERNAME */,
			username::VARCHAR		/* Function USERNAME */);
	ELSIF session_user != username THEN
		PERFORM rif40_log_pkg.rif40_error(-56403, 'rif40_study_ddl_definer', 
			'Study ID % wrong session username, expecting: %; got: %; execution context user: %',
			study_id::VARCHAR		/* Study ID */,
			session_user::VARCHAR		/* Session USERNAME */,
			username::VARCHAR		/* Function USERNAME */,
			current_user::VARCHAR		/* Execution context USERNAME */);
	ELSIF session_audsid != audsid THEN
		PERFORM rif40_log_pkg.rif40_error(-56404, 'rif40_study_ddl_definer', 
			'Study ID % wrong session AUDSID, expecting: %; got: %; execution context user: %',
			study_id::VARCHAR		/* Study ID */,
			session_audsid::VARCHAR		/* Session USERNAME */,
			audsid::VARCHAR			/* Function USERNAME */,
			current_user::VARCHAR		/* Execution context USERNAME */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_study_ddl_definer', 
			'[56405] Study ID % session, record and function username verified: %; AUDSID verified: %, execution context user: %',
			study_id::VARCHAR		/* Study ID */,
			session_user::VARCHAR		/* Session USERNAME */,
			audsid::VARCHAR			/* Function AUDSID */,
			current_user::VARCHAR		/* Execution context USERNAME (RIF40) */);
	END IF;

--
-- Confirm calling function is rif40_create_extract() [needs 9.3/PG_CONTEXT]
--

--
-- Get the maximum statement number for the logs
--
	OPEN c1stddl(study_id);
	FETCH c1stddl INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1stddl;
		PERFORM rif40_log_pkg.rif40_error(-56406, 'rif40_study_ddl_definer', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c1stddl;
	t_ddl:=COALESCE(c1_rec.max_statement_number, 0);
	IF t_ddl > 0 THEN
		l_statement_type:='POST_INSERT';
	END IF;
--
	FOREACH sql_stmt IN ARRAY ddl_stmts LOOP
		t_ddl:=t_ddl+1;	
--
-- Execute statement
--
		IF sql_stmt IS NOT NULL THEN
			BEGIN
				l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
				etp:=clock_timestamp();
				l_log_message:='OK, took: '||age(etp, stp);
 		     	EXCEPTION
--
-- Handle all errors
--
				WHEN others THEN
					etp:=clock_timestamp();
					l_sqlcode:=SQLSTATE;
					l_log_message:=format('ERROR, [56407] %s "%s" raised, took: %s', 
						l_sqlcode::VARCHAR		/* Error */,
						sqlerrm::VARCHAR		/* Error */,
						age(etp, stp)::VARCHAR		/* Human readable time take */);      
					PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_study_ddl_definer',
						'[56408] Study %: statement % error: % "%" raised by'||E'\n'||'SQL> %;'||E'\n'||' after: %',
						study_id::VARCHAR,		/* Study ID */
						t_ddl::VARCHAR			/* Statement number */,
						l_sqlcode::VARCHAR		/* Error */,
						sqlerrm::VARCHAR		/* Error */,
						sql_stmt::VARCHAR		/* SQL */,
						age(etp, stp)::VARCHAR		/* Human readable time take */);      
			END;
--
			elapsed_time:=EXTRACT(EPOCH FROM etp-stp);
--
-- Log statement 
--
			INSERT INTO t_rif40_study_sql_log(
				username, study_id, statement_type, statement_number, log_message, log_sqlcode, rowcount, start_time, elapsed_time, audsid)
			VALUES (
				username, study_id,l_statement_type, 
				t_ddl, l_log_message, l_sqlcode, coalesce(l_rows, 0), stp, elapsed_time, audsid);
			INSERT INTO t_rif40_study_sql(username, study_id, statement_type, statement_number, sql_text, line_number)
			VALUES (username, study_id, l_statement_type, t_ddl, sql_stmt, 1);  
--
--  Detect failure
--
			EXIT WHEN l_sqlcode != '00000' /* OK */;
		END IF;
	END LOOP;
--
	IF l_sqlcode != '00000' /* OK */ THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_study_ddl_definer',
			'[56409] Study %: error prevents further processing',
			study_id::VARCHAR		/* Study ID */);
		RETURN FALSE;
	ELSE
		RETURN TRUE;
	END IF;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_study_ddl_definer(INTEGER, VARCHAR, VARCHAR, VARCHAR[]) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_study_ddl_definer(INTEGER, VARCHAR, VARCHAR, VARCHAR[]) IS 'Function:	rif40_study_ddl_definer()
Parameter:	Study ID, username [study owner], audsid, DDL statements to execute
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Execute DDL statements as RIF40 (so the user cannot control access to extracted data)
		Log statements. Stop and return FALSE on error
Notes:

1. SQL created runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
2. Verify AUDSID and USER

	Verify AUDSID and USER
	Study AUDSID and function AUDSID must be the same
	Study username and session username must be the same
	Study username and function username must be the same
	Study AUDSID and session AUDSID must be the same
	Confirm calling function is rif40_create_extract() [needs 9.3]

   Note that this currently limits this function to being executed by rif40_create_extract()
';

--
-- Eof