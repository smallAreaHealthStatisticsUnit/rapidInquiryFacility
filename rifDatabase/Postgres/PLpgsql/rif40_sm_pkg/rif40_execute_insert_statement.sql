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
--								  rif40_execute_insert_statement()
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
-- rif40_execute_insert_statement						56600 to 56799
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

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
	n_sql_stmt		VARCHAR;
--
	use_bind_variables BOOLEAN:=FALSE; 
BEGIN
	IF sql_stmt IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-56600, 'rif40_execute_insert_statement', 
			'No SQL statement generated for study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
--
-- Get the maximum statement number for the logs
--
	OPEN c1exinst(study_id);
	FETCH c1exinst INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1exinst;
		PERFORM rif40_log_pkg.rif40_error(-56601, 'rif40_execute_insert_statement', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c1exinst;
	t_ddl:=COALESCE(c1_rec.max_statement_number, 0)+1;
--
	BEGIN
--
-- Replace year because Postgres does not have bind variable peeking
--	
		IF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start != year_stop THEN
			n_sql_stmt:=REPLACE(REPLACE(sql_stmt, '$2', year_start::VARCHAR), '$3', year_stop::VARCHAR);
		ELSIF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start = year_stop THEN
			n_sql_stmt:=REPLACE(sql_stmt, '$2', year_start::VARCHAR);
		ELSE 
			n_sql_stmt:=sql_stmt;
		END IF;
		n_sql_stmt:=REPLACE(n_sql_stmt, '$1', study_id::VARCHAR);
			
		IF SUBSTR(sql_stmt, 1, 7) = 'EXPLAIN' THEN
			IF use_bind_variables THEN		
				IF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start != year_stop THEN
					OPEN c2exinst FOR EXECUTE sql_stmt USING study_id, year_start, year_stop;
				ELSIF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start = year_stop THEN
					OPEN c2exinst FOR EXECUTE sql_stmt USING study_id, year_start;
				ELSE
					OPEN c2exinst FOR EXECUTE sql_stmt USING study_id;
				END IF;
			ELSE
				OPEN c2exinst FOR EXECUTE n_sql_stmt;
			END IF;
			GET DIAGNOSTICS l_rows = ROW_COUNT;
 			LOOP
				i:=i+1;
				FETCH c2exinst INTO query_plan_text;
				EXIT WHEN NOT FOUND;
				query_plan[i]:=query_plan_text;
			END LOOP;
			etp:=clock_timestamp();		
--
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_execute_insert_statement', 
				'[56005] SQL> %;', n_sql_stmt::VARCHAR);
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_execute_insert_statement', 
				'[56602] Study ID %, statement: %'||E'\n'||'Description: %'||E'\n'||' query plan:'||E'\n'||'%'::VARCHAR,
				study_id::VARCHAR				/* Study ID */,
				t_ddl::VARCHAR					/* Statement number */,
				description::VARCHAR				/* Description */,
				array_to_string(query_plan, E'\n')::VARCHAR	/* Query plan */);
			l_log_message:=description::VARCHAR||' OK, took: '||age(etp, stp)::VARCHAR;
		ELSE	
			IF use_bind_variables THEN		
				IF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start != year_stop THEN
					EXECUTE sql_stmt USING study_id, year_start, year_stop;
				ELSIF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start = year_stop THEN
					EXECUTE sql_stmt USING study_id, year_start;
				ELSE
					EXECUTE sql_stmt USING study_id;
				END IF;			
			ELSE
				EXECUTE n_sql_stmt;
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
			l_log_message:=format('ERROR, [56603] %s %s "%s" raised, took: %s', 
				description::VARCHAR		/* Description */,
				l_sqlcode::VARCHAR		/* SQLSTATE */,
				sqlerrm::VARCHAR		/* Error */,
				age(etp, stp)::VARCHAR		/* Human readable time take */);      
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_execute_insert_statement',
				'[56604] Study %: statement % (%) error: % "%" raised by'||E'\n'||'SQL> %;'||E'\n'||'After: %'::VARCHAR,
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
	VALUES (USER, study_id, l_statement_type, t_ddl, SUBSTRING(sql_stmt FROM 1 FOR 4000), 1);  
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_execute_insert_statement',
		'[56605] Study %: %',
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

--
-- Eof