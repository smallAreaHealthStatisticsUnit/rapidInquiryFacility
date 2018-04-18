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
-- Rapid Enquiry Facility (RIF) - RIF state machine
--     				  rif40_insert_extract
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
-- Error codes assignment (see PLpgsql\Error_codes.txt):
--
-- rif40_sm_pkg:
--
-- rif40_insert_extract: 		55800 to 55999
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

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_insert_extract(study_id INTEGER)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_insert_extract()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Insert data into extract table


 */
	c1insext2 CURSOR(l_study_id INTEGER) FOR
		SELECT * 
		  FROM rif40_studies a
		 WHERE a.study_id = l_study_id;	 	 
	c2insext2 REFCURSOR;
	c1_rec RECORD;
	c2_rec RECORD;
--
	sql_stmt		VARCHAR;
--
	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
	etp		TIMESTAMP WITH TIME ZONE;
BEGIN
	OPEN c1insext2(study_id);
	FETCH c1insext2 INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1insext2;
		PERFORM rif40_log_pkg.rif40_error(-55800, 'rif40_insert_extract', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c1insext2;
--
-- Study area insert
--
	sql_stmt:='INSERT INTO g_rif40_study_areas(study_id, area_id, band_id)'||E'\n'||
		'SELECT study_id, area_id, band_id'||E'\n'||
		'  FROM rif40_study_areas'||E'\n'||
		' WHERE study_id = $1 /* Current study ID */'||E'\n'||
		' ORDER BY study_id, area_id, band_id'::VARCHAR;
	IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 'Study area insert'::VARCHAR) = FALSE THEN 
		RETURN FALSE;
	END IF;
--
-- Study extract insert
--	
-- This will eventually support paralleisation. Do year by year for the moment
--
	FOR i IN c1_rec.year_start .. c1_rec.year_stop LOOP
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_insert_extract', 
			'[55801] Study ID % INSERT study year %',
			study_id::VARCHAR		/* Study ID */,
			i::VARCHAR);
--
-- Do explain plan at the same time (DOES NOT INSERT DATA!)
--
		IF i = c1_rec.year_start THEN
			sql_stmt:=rif40_sm_pkg.rif40_create_insert_statement(study_id, 'S', i, i);
			IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, 
				'EXPLAIN (VERBOSE, FORMAT text)'||E'\n'||sql_stmt, 
				'Study extract EXPLAIN (non) insert '||i::VARCHAR||' (EXPLAIN)'::VARCHAR, i, i) = FALSE THEN 
				RETURN FALSE;
--			ELSIF rif40_sm_pkg.rif40_execute_insert_statement(study_id, 
--				'EXPLAIN (ANALYZE, VERBOSE, COSTS, BUFFERS, /* TIMING, (9.2+) */ FORMAT text)'||E'\n'||sql_stmt, 
--				'Study extract '||i::VARCHAR||' insert (EXPLAIN ANALYZE)'::VARCHAR, i, i) = FALSE THEN 
--				RETURN FALSE;
			END IF;
		END IF;
--
-- Do actual INSERT
--
		IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 
			'Study extract insert '||i::VARCHAR, i, i) = FALSE THEN 
			RETURN FALSE;
		END IF;
	END LOOP;
--
-- Comparison area insert
--
	sql_stmt:='INSERT INTO g_rif40_comparison_areas(study_id, area_id)'||E'\n'||
		'SELECT study_id, area_id'||E'\n'||
		'  FROM rif40_comparison_areas'||E'\n'||
		' WHERE study_id = $1 /* Current study ID */'||E'\n'||
		' ORDER BY study_id, area_id'::VARCHAR;
	IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 'Comparison area insert'::VARCHAR) = FALSE THEN 
		RETURN FALSE;
	END IF;
--
-- Comparison extract insert
--	
-- This will eventually support paralleisation. Do year by year for the moment
--
	FOR i IN c1_rec.year_start .. c1_rec.year_stop LOOP
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_insert_extract', 
			'[55802] Study ID % INSERT comparison year %',
			study_id::VARCHAR		/* Study ID */,
			i::VARCHAR);
--
-- Do explain plan at the same time (DOES NOT INSERT DATA!)
--
		IF i = c1_rec.year_start THEN
			sql_stmt:=rif40_sm_pkg.rif40_create_insert_statement(study_id, 'C', i, i);
			IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, 
				'EXPLAIN (VERBOSE, FORMAT text)'||E'\n'||sql_stmt, 
				'Comparison extract EXPLAIN (non) insert '||i::VARCHAR||' (EXPLAIN)'::VARCHAR, i, i) = FALSE THEN 
				RETURN FALSE;
--			ELSIF rif40_sm_pkg.rif40_execute_insert_statement(study_id, 
--				'EXPLAIN (ANALYZE, VERBOSE, COSTS, BUFFERS, /* TIMING, (9.2+) */ FORMAT text)'||E'\n'||sql_stmt, 
--				'Comparison extract '||i::VARCHAR||' insert (EXPLAIN ANALYZE)'::VARCHAR, i, i) = FALSE THEN 
--				RETURN FALSE;
			END IF;
		END IF;
--
-- Do actual INSERT
--
		IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 
			'Comparison extract insert '||i::VARCHAR, i, i) = FALSE THEN 
			RETURN FALSE;
		END IF;
	END LOOP;
	
	sql_stmt:='SELECT COUNT(year) AS total FROM rif_studies.'||quote_ident(LOWER(c1_rec.extract_table));
	OPEN c2insext2 FOR EXECUTE sql_stmt;
	FETCH c2insext2 INTO c2_rec;
	CLOSE c2insext2;
--
	etp:=clock_timestamp();
	IF c2_rec.total > 0 THEN 
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_insert_extract', 
			'[55803] Study ID % extract table INSERT % rows in %',
			study_id::VARCHAR		/* Study ID */,
			c2_rec.total::VARCHAR,	/* Total extract rows */
			age(etp, stp)::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-55810, 'rif40_insert_extract', 
			'Study ID % no rows INSERTED into extract table',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	
--
	RETURN TRUE;
--
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_insert_extract(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_insert_extract(INTEGER) IS 'Function:	rif40_insert_extract()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Insert data into extract table
';

--
-- Eof
