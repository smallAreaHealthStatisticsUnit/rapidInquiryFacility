-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 run study
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
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_run_study]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_run_study]
GO 

CREATE PROCEDURE [rif40].[rif40_run_study](@rval INT OUTPUT, @study_id int, @debug int=0, @recursion_level int=0)
AS
BEGIN
--
-- Defaults if set to NULL
--
	IF @debug IS NULL SET @debug=0;
	IF @recursion_level IS NULL SET @recursion_level=0;
	
/*
Function:	rif40_run_study()
Parameter:	Study ID, enable debug (INTEGER: default 0), recursion level (internal parameter DO NOT USE)
Returns:	Success or failure [INTEGER]
			Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
			Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Run study 

Runs as INVOKER. Alternate version calling this function runs as DEFINER USER

Check study state - 

C: created, not verfied; 
V: verified, but no other work done; 
E - extracted imported or created, but no results or maps created; 
R: results computed; 
U: upgraded record from V3.1 RIF (has an indeterminate state; probably R).

Define transition
Create extract, call: rif40_sm_pkg.rif40_create_extract()
Runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
Compute results, call: rif40_sm_pkg.rif40_compute_results()
Do update. This forces verification
(i.e. change in study_State on rif40_studies calls rif40_sm_pkg.rif40_verify_state_change)
Recurse until complete
 */
	SET @rval=0 /* Failure */;

	DECLARE @err_msg VARCHAR(MAX);
--
	DECLARE c1_runst CURSOR FOR
		WITH b AS (
			SELECT COUNT(inv_id) AS investigation_count
			  FROM rif40.rif40_investigations c
			 WHERE @study_id = c.study_id
		)
		SELECT study_state, b.investigation_count 
		  FROM rif40.t_rif40_studies a, b /* MUST USE TABLE NOT VIEWS WHEN USING LOCKS/WHERE CURRENT OF */
		 WHERE @study_id = a.study_id
		   FOR UPDATE;
	DECLARE @study_state VARCHAR(1);
	DECLARE @new_study_state VARCHAR(1);
	DECLARE @investigation_count INT;
--
-- Check and lock table
--
	OPEN c1_runst; 
	FETCH NEXT FROM c1_runst INTO @study_state, @investigation_count;
	IF @@CURSOR_ROWS = 0 BEGIN
			CLOSE c1_runst;
			DEALLOCATE c1_runst;
			SET @err_msg = formatmessage(55200, @study_id); -- Study ID %i not found
			THROW 55200, @err_msg, 1;
		END;
--
-- Check study state
--
	ELSE IF @study_state = 'C'
		 SET @new_study_state = 'V';
	ELSE IF @study_state = 'V'
		 SET @new_study_state = 'E';
	ELSE IF @study_state = 'E'
		 SET @new_study_state = 'R';
	ELSE BEGIN
			CLOSE c1_runst;
			DEALLOCATE c1_runst;
			SET @err_msg = formatmessage(55201, @study_id, @study_state); -- Study ID %i cannot be run, in state: %s, needs to be in ''V'' or ''E''
			THROW 55201, @err_msg, 1;
		END;
--
	CLOSE c1_runst;
	DEALLOCATE c1_runst;

--
-- Create extract, call: rif40.rif40_create_extract()
--
	IF @new_study_state = 'E' BEGIN
		PRINT 'Call: rif40.rif40_create_extract()';
		END;
--
-- Compute results, call: rif40.rif40_compute_results()
--
	ELSE IF @new_study_state = 'R' BEGIN
		PRINT 'Call: rif40.rif40_compute_results()';
		END;

--
-- Do update. This forces verification
-- (i.e. change in study_state on rif40_studies calls rif40.rif40_verify_state_change)
--
/*
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_run_study',
		'[55206] Start state transition (%=>%) for study %',
		c1_rec.study_state::VARCHAR,
		new_study_state::VARCHAR,
		c1_rec.study_id::VARCHAR);
	UPDATE rif40_investigations a SET investigation_state = new_study_state WHERE a.study_id = c1_rec.study_id;
	GET DIAGNOSTICS investigation_count = ROW_COUNT;
	IF investigation_count != c1_rec.investigation_count THEN
		PERFORM rif40_log_pkg.rif40_error(-90708, 'rif40_run_study', 
			'[55207] Expecting to update % investigation(s), updated % during state transition (%=>%) for study %',
			c1_rec.investigation_count::VARCHAR,
			investigation_count::VARCHAR,
			c1_rec.study_state::VARCHAR,
			new_study_state::VARCHAR,
			c1_rec.study_id::VARCHAR);
	END IF;

--
-- MUST USE TABLE NOT VIEWS WHEN USING LOCKS/WHERE CURRENT OF
--
	UPDATE rif40_studies a SET study_state = new_study_state WHERE a.study_id = c1_rec.study_id;
	GET DIAGNOSTICS study_count = ROW_COUNT;
	etp:=clock_timestamp();
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_run_study',
		'[55208] Recurse [%] Completed state transition (%=>%) for study % with % investigation(s); time taken %',
		n_recursion_level::VARCHAR,
		c1_rec.study_state::VARCHAR,
		new_study_state::VARCHAR,
		c1_rec.study_id::VARCHAR,
		investigation_count::VARCHAR,
		age(etp, stp)::VARCHAR);
 
--
-- Recurse until complete
--
	IF new_study_state != c1_rec.study_state AND new_study_state IN ('V', 'E') THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_run_study',
			'[55209] Recurse [%] rif40_run_study using new state % for study %',
			n_recursion_level::VARCHAR,
			new_study_state::VARCHAR,
			c1_rec.study_id::VARCHAR);
		IF rif40_sm_pkg.rif40_run_study(c1_rec.study_id, debug, n_recursion_level) = FALSE THEN /- Halt on failure -/
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_run_study',
				'[55210] Recurse [%] rif40_run_study to new state % for study % failed, see previous warnings',
				n_recursion_level::VARCHAR,
				new_study_state::VARCHAR,
				c1_rec.study_id::VARCHAR);
			RETURN FALSE;
		END IF;
	ELSIF new_study_state != c1_rec.study_state AND new_study_state = 'R' THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_run_study', E'\n'||
'************************************************************************'||E'\n'||
'*                                                                      *'||E'\n'||
'* [55211] Completed study %                         *'||E'\n'||
'*                                                                      *'||E'\n'||
'************************************************************************',
			RPAD(c1_rec.study_id::VARCHAR, 20)::VARCHAR);
	ELSE
		OPEN c1_runst(study_id);
		FETCH c1_runst INTO c1_rec;
		IF NOT FOUND THEN
			CLOSE c1_runst;
			PERFORM rif40_log_pkg.rif40_error(-55212, 'rif40_run_study', 
				'Study ID % not found, study in unexpected and unknown state',
				study_id::VARCHAR);
		END IF;
		PERFORM rif40_log_pkg.rif40_error(-55213, 'rif40_run_study', 
			'Study % in unexpected state %',
			c1_rec.study_id::VARCHAR,
			c1_rec.study_state::VARCHAR);
	END IF;
--
-- All recursion unwound
--
	etp:=clock_timestamp();
	IF recursion_level = 0 THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_run_study',
			'[55214] Recursion complete, state %, rif40_run_study study % with % investigation(s); time taken %',
			c1_rec.study_state::VARCHAR,
			c1_rec.study_id::VARCHAR,
			investigation_count::VARCHAR,
			age(etp, stp)::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_run_study',
			'[55215] Recursion %, state %, rif40_run_study study % with % investigation(s); time taken %',
			recursion_level::VARCHAR,
			c1_rec.study_state::VARCHAR,
			c1_rec.study_id::VARCHAR,
			investigation_count::VARCHAR,
			age(etp, stp)::VARCHAR);
	END IF;
 */

--	
-- Error: Recursion %i, rif40_run_study study %i had error.
--
--	SET @@err_msg = formatmessage(55216, @recursion_level, @study_id);
--	THROW 55216, @err_msg, 1;
		
	RETURN @rval;
END;
GO

GRANT EXECUTE ON [rif40].[rif40_run_study] TO rif_user, rif_manager;
GO

--
-- Eof