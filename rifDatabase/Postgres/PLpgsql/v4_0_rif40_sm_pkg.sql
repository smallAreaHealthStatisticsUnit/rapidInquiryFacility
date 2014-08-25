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

\echo Building rif40_sm_pkg (state machine and extract SQL generation) package...

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

--
-- Include common code
--

--
-- Include functions
--
\i ../PLpgsql/rif40_sm_pkg/rif40_verify_state_change.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_run_study.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_create_extract.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_compute_results.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_insert_extract.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_create_insert_statement.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_create_disease_mapping_example.sql

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
		PERFORM rif40_log_pkg.rif40_error(-90620, 'rif40_study_ddl_definer', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c2stddl;
	IF c2_rec.audsid != audsid THEN
		PERFORM rif40_log_pkg.rif40_error(-90621, 'rif40_study_ddl_definer', 
			'Study ID % wrong audsid, expecting: %; got: %',
			study_id::VARCHAR		/* Study ID */,
			c2_rec.audsid::VARCHAR		/* Record AUDSID */,
			audsid::VARCHAR			/* Function AUDSID */);
	ELSIF c2_rec.username != username THEN
		PERFORM rif40_log_pkg.rif40_error(-90622, 'rif40_study_ddl_definer', 
			'Study ID % wrong username, expecting: %; got: %',
			study_id::VARCHAR		/* Study ID */,
			c2_rec.username::VARCHAR	/* Record USERNAME */,
			username::VARCHAR		/* Function USERNAME */);
	ELSIF session_user != username THEN
		PERFORM rif40_log_pkg.rif40_error(-90623, 'rif40_study_ddl_definer', 
			'Study ID % wrong session username, expecting: %; got: %; execution context user: %',
			study_id::VARCHAR		/* Study ID */,
			session_user::VARCHAR		/* Session USERNAME */,
			username::VARCHAR		/* Function USERNAME */,
			current_user::VARCHAR		/* Execution context USERNAME */);
	ELSIF session_audsid != audsid THEN
		PERFORM rif40_log_pkg.rif40_error(-90624, 'rif40_study_ddl_definer', 
			'Study ID % wrong session AUDSID, expecting: %; got: %; execution context user: %',
			study_id::VARCHAR		/* Study ID */,
			session_audsid::VARCHAR		/* Session USERNAME */,
			audsid::VARCHAR			/* Function USERNAME */,
			current_user::VARCHAR		/* Execution context USERNAME */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_study_ddl_definer', 
			'[90625] Study ID % session, record and function username verified: %; AUDSID verified: %, execution context user: %',
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
		PERFORM rif40_log_pkg.rif40_error(-90626, 'rif40_study_ddl_definer', 
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
					l_log_message:=format('ERROR, [90627] %s "%s" raised, took: %s', 
						l_sqlcode::VARCHAR		/* Error */,
						sqlerrm::VARCHAR		/* Error */,
						age(etp, stp)::VARCHAR		/* Human readable time take */);      
					PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_study_ddl_definer',
						'[90628] Study %: statement % error: % "%" raised by'||E'\n'||'SQL> %;'||E'\n'||' after: %',
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
			'[90629] Study %: error prevents further processing',
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

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_execute_insert_statement(study_id INTEGER, sql_stmt VARCHAR, description VARCHAR, 
	year_start INTEGER DEFAULT NULL, year_stop INTEGER DEFAULT NULL)
RETURNS BOOLEAN 
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_execute_insert_statement()
Parameter:	Study ID, SQL statement, description, year_start, year_stop
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Execute INSERT SQL statement
		Log statements. Stop and return FALSE on error
 */
	c1exinst CURSOR(l_study_id INTEGER) FOR
		SELECT MAX(statement_number) AS max_statement_number
		  FROM rif40_study_sql_log a
		 WHERE l_study_id = a.study_id;
	c2exinst REFCURSOR;
	c1_rec RECORD;
--
	l_rows		INTEGER:=0;
	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
	etp		TIMESTAMP WITH TIME ZONE;
	elapsed_time	FLOAT:=0;
	l_log_message	VARCHAR:='OK';
	l_sqlcode	VARCHAR:='00000';
	t_ddl 		INTEGER;
	query_plan	VARCHAR[];
	query_plan_text	VARCHAR;
--
-- INSERT INTO t_rif_study_sql (view wont work until statement_type is fixed
--
-- 1 CREATE
-- 2 INSERT
-- 3 POST_INSERT
-- 4 NUMERATOR_CHECK
-- 5 DENOMINATOR_CHECK
--
	l_statement_type VARCHAR:='INSERT' /* INSERT: Local Postgres statement */;
	i		INTEGER:=0;
BEGIN
--
-- Get the maximum statement number for the logs
--
	OPEN c1exinst(study_id);
	FETCH c1exinst INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1exinst;
		PERFORM rif40_log_pkg.rif40_error(-90426, 'rif40_execute_insert_statement', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c1exinst;
	t_ddl:=COALESCE(c1_rec.max_statement_number, 0)+1;
--
	BEGIN
		IF SUBSTR(sql_stmt, 1, 7) = 'EXPLAIN' THEN
			IF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start != year_stop THEN
				OPEN c2exinst FOR EXECUTE sql_stmt USING study_id, year_start, year_stop;
			ELSIF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start = year_stop THEN
				OPEN c2exinst FOR EXECUTE sql_stmt USING study_id, year_start;
			ELSE
				OPEN c2exinst FOR EXECUTE sql_stmt USING study_id;
			END IF;
			GET DIAGNOSTICS l_rows = ROW_COUNT;
 			LOOP
				i:=i+1;
				FETCH c2exinst INTO query_plan_text;
				EXIT WHEN NOT FOUND;
				query_plan[i]:=query_plan_text;
			END LOOP;
			etp:=clock_timestamp();
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_execute_insert_statement', 
				'[90427] Study ID %, statement: %'||E'\n'||'Description: %'||E'\n'||' query plan:'||E'\n'||'%'::VARCHAR,
				study_id::VARCHAR				/* Study ID */,
				t_ddl::VARCHAR					/* Statement number */,
				description::VARCHAR				/* Description */,
				array_to_string(query_plan, E'\n')::VARCHAR	/* Query plan */);
			l_log_message:=description::VARCHAR||' OK, took: '||age(etp, stp)::VARCHAR;
		ELSE
			IF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start != year_stop THEN
				EXECUTE sql_stmt USING study_id, year_start, year_stop;
			ELSIF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start = year_stop THEN
				EXECUTE sql_stmt USING study_id, year_start;
			ELSE
				EXECUTE sql_stmt USING study_id;
			END IF;
			GET DIAGNOSTICS l_rows = ROW_COUNT;
			etp:=clock_timestamp();
			l_log_message:=description::VARCHAR||' OK, inserted '||l_rows::VARCHAR||' rows, took: '||age(etp, stp)::VARCHAR;
		END IF;
     	EXCEPTION
--
-- Handle all errors
--
		WHEN others THEN
			etp:=clock_timestamp();
			l_sqlcode:=SQLSTATE;
			l_log_message:=format('ERROR, [90428] %s %s "%s" raised, took: %s', 
				description::VARCHAR		/* Description */,
				l_sqlcode::VARCHAR		/* SQLSTATE */,
				sqlerrm::VARCHAR		/* Error */,
				age(etp, stp)::VARCHAR		/* Human readable time take */);      
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_execute_insert_statement',
				'[90429] Study %: statement % (%) error: % "%" raised by'||E'\n'||'SQL> %;'||E'\n'||'After: %'::VARCHAR,
				study_id::VARCHAR		/* Study ID */,
				description::VARCHAR		/* Description */,
				t_ddl::VARCHAR			/* Statement number */,
				l_sqlcode::VARCHAR		/* SQLSTATE */,
				sqlerrm::VARCHAR		/* Error */,
				sql_stmt::VARCHAR		/* SQL */,
				age(etp, stp)::VARCHAR		/* Human readable time take */);      
			RETURN FALSE;
	END;
--
	elapsed_time:=EXTRACT(EPOCH FROM etp-stp);
--
-- Log statement 
--
	INSERT INTO rif40_study_sql_log(
		username, study_id, statement_type, statement_number, log_message, log_sqlcode, rowcount, start_time, elapsed_time)
	VALUES (
		USER, study_id,l_statement_type, 
		t_ddl, l_log_message, l_sqlcode, coalesce(l_rows, 0), stp, elapsed_time);
	INSERT INTO rif40_study_sql(username, study_id, statement_type, statement_number, sql_text, line_number)
	VALUES (USER, study_id, l_statement_type, t_ddl, sql_stmt, 1);  
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_execute_insert_statement',
		'[90430] Study %: %',
		study_id::VARCHAR,		/* Study ID */
		l_log_message::VARCHAR		/* Log message */);
--
	RETURN TRUE;
--
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_execute_insert_statement(INTEGER, VARCHAR, VARCHAR, INTEGER, INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_execute_insert_statement(INTEGER, VARCHAR, VARCHAR, INTEGER, INTEGER) IS 'Function:	rif40_execute_insert_statement()
Parameter:	Study ID, SQL statement, description, year_start, year_stop
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Execute INSERT SQL statement
		Log statements. Stop and return FALSE on error';


CREATE OR REPLACE FUNCTION rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(truncate BOOLEAN DEFAULT FALSE)
RETURNS void
SECURITY DEFINER 
AS $func$
DECLARE
/*
Function:	cleanup_orphaned_extract_and_map_tables()
Parameter:	Truncate tables (default FALSE)
Returns:	Nothing
Description:	Cleanup and map and extract tables not referenced by a study (runs as rif40)
		Do not drop table if it contains any data unless truncate is true
		Orphans are defined as tables in rif_studies schema no longer referenced by studies

Note:		DO NOT PUT TABLES INTO THE rif_studies SCHEMA MANUALLY - they will be deleted if empty
 */
	c1_clorpf CURSOR FOR
		SELECT table_name /* tables in rif_studies schema no longer referenced by studies */
		  FROM information_schema.tables
		 WHERE table_schema = 'rif_studies'
		   AND table_type   = 'BASE TABLE'
		EXCEPT
		SELECT extract_table
		  FROM t_rif40_studies
		EXCEPT
		SELECT map_table
		  FROM t_rif40_studies
		 ORDER BY 1;
	c2_clorpf REFCURSOR;
	c1_rec RECORD;
	c2_rec RECORD;
--
	sql_stmt	VARCHAR;
	t_ddl		INTEGER:=0;
	ddl_stmts	VARCHAR[];
BEGIN
	FOR c1_rec IN c1_clorpf LOOP
		sql_stmt:='SELECT COUNT(study_id) AS total'||E'\n'||
			'  FROM ('||E'\n'||
			'SELECT study_id'||E'\n'||
			'  FROM rif_studies.'||c1_rec.table_name||E'\n'||
			' LIMIT 1) a';
		OPEN c2_clorpf FOR EXECUTE sql_stmt;
		FETCH c2_clorpf INTO c2_rec;
		IF NOT FOUND THEN
			CLOSE c2_clorpf;
			PERFORM rif40_log_pkg.rif40_error(-90661, 'cleanup_orphaned_extract_and_map_tables', 
				'[90661] Extract/map table % not found',
				c1_rec.table_name::VARCHAR);
		END IF;
		CLOSE c2_clorpf;
--
		IF c2_rec.total = 0 THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
				'[90662] Extract/map table % does not contain data; will be cleaned up',
				c1_rec.table_name::VARCHAR);
			sql_stmt:='DROP TABLE rif_studies.'||c1_rec.table_name;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
		ELSIF truncate THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
				'[90663] Extract/map table % contains data; truncate is TRUE; will be cleaned up',
				c1_rec.table_name::VARCHAR);
			sql_stmt:='TRUNCATE TABLE rif_studies.'||c1_rec.table_name;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
			sql_stmt:='DROP TABLE rif_studies.'||c1_rec.table_name;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
				'[90664] Extract/map table % contains data; will not be cleaned up',
				c1_rec.table_name::VARCHAR);
		END IF;
	END LOOP;
--
-- Now drop the tables
--
	IF t_ddl > 0 THEN
		PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmts);
		PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
			'[90665] % orphaned extract/map table(s) cleaned up',
			t_ddl::VARCHAR);
	END IF;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(BOOLEAN) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(BOOLEAN) IS 'Function:	cleanup_orphaned_extract_and_map_tables()
Parameter:	Truncate tables (default FALSE)
Returns:	Nothing
Description:	Cleanup and map and extract tables not referenced by a study (runs as rif40)
		Do not drop table if it contains any data unless truncate is true
		Orphans are defined as tables in rif_studies schema no longer referenced by studies

Note:		DO NOT PUT TABLES INTO THE rif_studies SCHEMA MANUALLY - they will be deleted if empty';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_delete_study(study_id INTEGER)
RETURNS void
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_delete_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS
RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES 

Truncate extract and map tables if defined

Call: cleanup_orphaned_extract_and_map_tables()

 */
	c1_delst	CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1_rec		RECORD;
--
	sql_stmt	VARCHAR[];
	rows		INTEGER;
	schema 		VARCHAR;
BEGIN
	OPEN c1_delst(study_id);
	FETCH c1_delst INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_delst;
		PERFORM rif40_log_pkg.rif40_error(-90601, 'rif40_delete_study', 
			'Study ID % not found',
			study_id::VARCHAR);
	END IF;
	CLOSE c1_delst;
--
	sql_stmt[1]:='DELETE FROM rif40_study_sql WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[2]:='DELETE FROM rif40_study_sql_log WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[3]:='DELETE FROM rif40_results WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[4]:='DELETE FROM rif40_contextual_stats WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[5]:='DELETE FROM rif40_inv_conditions WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[6]:='DELETE FROM rif40_inv_covariates WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[7]:='DELETE FROM rif40_investigations WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[8]:='DELETE FROM rif40_study_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[9]:='DELETE FROM rif40_comparison_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[10]:='DELETE FROM rif40_study_shares WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[11]:='DELETE FROM rif40_studies WHERE study_id = '||study_id::VARCHAR;
--
-- Drop extract and map tables if defined
-- (Cannot be dropped yets - requires definer function to cleanup all zero sized orphaned extract tables)
--
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.extract_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.extract_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_delete_study', 
			'[90940] Will truncate extract table % for study %',
			c1_rec.extract_table::VARCHAR	/* Extract table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
	END IF;
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.map_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.map_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.map_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_delete_study', 
			'[90941] Will truncate map table % for study %',
			c1_rec.map_table::VARCHAR	/* Map table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.map_table);
	END IF;
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);

	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_delete_study', 
		'[90942] Deleted study: %; % rows deleted',
		 study_id::VARCHAR		/* Study ID */,
		 rows::VARCHAR 			/* Rows deleted */);
--
-- Cleanup and map and extract tables not referenced by a study (runs as rif40)
--
	PERFORM rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables();
--
	RETURN;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) IS 'Function:	rif40_delete_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS
RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES

Truncate extract and map tables if defined

Call: cleanup_orphaned_extract_and_map_tables()
';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_reset_study(study_id INTEGER)
RETURNS void
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_reset_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Reset study to the 'C' (created state) so it can be re-run

Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS

Truncate extract and map tables if defined

Update study state to C

Call: cleanup_orphaned_extract_and_map_tables()
 */
	c1_reset CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1_rec		RECORD;
--
	sql_stmt	VARCHAR[];
	rows		INTEGER;
	schema 		VARCHAR;
--
	study_upd_count INTEGER;
	inv_upd_count INTEGER;
BEGIN
	OPEN c1_reset(study_id);
	FETCH c1_reset INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_reset;
		PERFORM rif40_log_pkg.rif40_error(-90945, 'rif40_reset_study', 
			'Study ID % not found',
			study_id::VARCHAR);
	END IF;
	CLOSE c1_reset;
--
	sql_stmt[1]:='DELETE FROM rif40_study_sql WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[2]:='DELETE FROM rif40_study_sql_log WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[3]:='DELETE FROM rif40_results WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[4]:='DELETE FROM rif40_contextual_stats WHERE study_id = '||study_id::VARCHAR;
--
-- Drop extract and map tables if defined
-- (Cannot be dropped yets - requires definer function to cleanup all zero sized orphaned extract tables)
--
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.extract_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.extract_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_reset_study', 
			'[90946] Will truncate extract table % for study %',
			c1_rec.extract_table::VARCHAR	/* Extract table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
	END IF;
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.map_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.map_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.map_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_reset_study', 
			'[90947] Will truncate map table % for study %',
			c1_rec.map_table::VARCHAR	/* Map table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.map_table);
	END IF;
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Update study state
--
	EXECUTE 'UPDATE rif40_investigations a
	   SET investigation_state = ''C''
 	 WHERE a.study_id = $1' USING study_id;
	GET DIAGNOSTICS study_upd_count = ROW_COUNT;
	EXECUTE 'UPDATE rif40_studies a
	   SET study_state = ''C''
 	 WHERE a.study_id = $1' USING study_id;
	GET DIAGNOSTICS inv_upd_count = ROW_COUNT;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_reset_study', 
		'[90948] Reset study: %; % rows deleted; % study, % investigation(s) updated',
		study_id::VARCHAR		/* Study ID */,
		rows::VARCHAR 			/* Rows deleted */,
		study_upd_count::VARCHAR 	/* Study update count */,
		inv_upd_count::VARCHAR 		/* Study update count */);
--
-- Cleanup and map and extract tables not referenced by a study (runs as rif40)
--
	PERFORM rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables();
--
	RETURN;
--
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_reset_study(INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_reset_study(INTEGER) TO rif40;

COMMENT ON FUNCTION rif40_sm_pkg.rif40_reset_study(INTEGER) IS 'Function:	rif40_reset_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Reset study to the ''C'' (created state) so it can be re-run

Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS

Truncate extract and map tables if defined

Update study state to ''C''

Call: cleanup_orphaned_extract_and_map_tables()
';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_clone_study(study_id INTEGER)
RETURNS INTEGER
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_clone_study()
Parameter:	Study ID
Returns:	New study ID
Description:	Clone study [testing purposes only] from tables:

RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES 

Does not clone:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS

 */
	sql_stmt	VARCHAR[];
	rows		INTEGER;
BEGIN
	sql_stmt[7]:='INSERT INTO rif40_inv_conditions(condition) SELECT condition FROM rif40_inv_conditions WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[6]:='INSERT INTO rif40_inv_covariates(geography, covariate_name, study_geolevel_name, min, max)
		 SELECT geography, covariate_name, study_geolevel_name, min, max FROM rif40_inv_covariates WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[5]:='INSERT INTO rif40_investigations(/* geography, */ inv_name, inv_description, genders, numer_tab, 
			year_start, year_stop, max_age_group, min_age_group) 
			SELECT /* geography, */ inv_name, inv_description, genders, numer_tab, 
			year_start, year_stop, max_age_group, min_age_group FROM rif40_investigations WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[4]:='INSERT INTO rif40_study_areas(area_id, band_id) SELECT area_id, band_id FROM rif40_study_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[3]:='INSERT INTO rif40_comparison_areas(area_id) SELECT area_id FROM rif40_comparison_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[2]:='INSERT INTO rif40_study_shares(grantee_username) SELECT grantee_username FROM rif40_study_shares WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[1]:='INSERT INTO rif40_studies(geography, project, study_name, study_type,
 		comparison_geolevel_name, study_geolevel_name, denom_tab,            
 		year_start, year_stop, max_age_group, min_age_group, 
 		suppression_value, extract_permitted, transfer_permitted) SELECT geography, project, study_name, study_type,
 		comparison_geolevel_name, study_geolevel_name, denom_tab,            
 		year_start, year_stop, max_age_group, min_age_group, 
 		suppression_value, extract_permitted, transfer_permitted FROM rif40_studies WHERE study_id = '||study_id::VARCHAR;
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);

	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_clone_study', 
		'[90930] Cloned study: %; % rows inserted',
		 study_id::VARCHAR		/* Study ID */,
		 rows::VARCHAR 			/* Rows inserted */);
--
	RETURN currval('rif40_study_id_seq'::regclass);
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_clone_study(INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_clone_study(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_clone_study(INTEGER) IS 'Function:	rif40_clone_study()
Parameter:	Study ID
Returns:	New study ID
Description:	Clone study [testing purposes only] from tables:

RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES 

Does not clone:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS
';

\echo Built rif40_sm_pkg (state machine and extract SQL generation) package.
--
-- Eof

