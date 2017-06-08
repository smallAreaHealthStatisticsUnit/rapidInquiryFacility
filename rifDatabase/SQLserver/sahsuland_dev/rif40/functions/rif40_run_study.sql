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

-- This script must be run from the installation directory
:r ..\sahsuland_dev\rif40\functions\rif40_ddl.sql
:r ..\sahsuland_dev\rif40\functions\rif40_execute_insert_statement.sql
:r ..\sahsuland_dev\rif40\functions\rif40_create_insert_statement.sql
:r ..\sahsuland_dev\rif40\functions\rif40_insert_extract.sql
:r ..\sahsuland_dev\rif40\functions\rif40_create_extract.sql
:r ..\sahsuland_dev\rif40\functions\rif40_compute_results.sql
:r ..\sahsuland_dev\rif40\functions\rif40_GetAdjacencyMatrix.sql

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_run_study]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_run_study];
GO 

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_run_study2]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_run_study2];
GO 

CREATE PROCEDURE [rif40].[rif40_run_study2](@study_id int, @debug int=0, @recursion_level int=0)
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
Returns:	Success or failure [INTEGER], as  first parameter
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
	DECLARE @rval	INTEGER=0 /* Failure */;
--
	DECLARE @etime DATETIME, @stp DATETIME=GETDATE(), @etp DATETIME;
--
	DECLARE @crlf AS VARCHAR(2)=CHAR(10)+CHAR(13);
	DECLARE @err_msg VARCHAR(MAX);
	DECLARE @msg VARCHAR(MAX);
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
	DECLARE @actual_investigation_count INT;
--
	DECLARE @n_recursion_level INT = @recursion_level + 1;
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
		EXECUTE @rval=rif40.rif40_create_extract
				@study_id	/* Study id */,
				@debug		/* enable debug: 0/1) */;
		INSERT INTO rif40.rif40_study_status(study_id, study_state, message) VALUES (@study_id, 'E', 
			'Study extracted imported or created but neither results nor maps have been created.');				
		IF @rval = 0 BEGIN
			PRINT '55202: WARNING! rif40.rif40_create_extract() FAILED, see previous warnings';
			RETURN @rval;
			END;
		ELSE PRINT '55203: rif40.rif40_create_extract() OK';
		END;
--
-- Compute results, call: rif40.rif40_compute_results()
--
	ELSE IF @new_study_state = 'R' BEGIN
		EXECUTE @rval=rif40.rif40_compute_results
				@study_id	/* Study id */,
				@debug		/* enable debug: 0/1) */;
		IF @rval = 0 BEGIN
			PRINT '55204: WARNING! rif40.rif40_compute_results() FAILED, see previous warnings';
			RETURN @rval;
			END;
		ELSE PRINT '55205: rif40.rif40_compute_results() OK';
		END;

	IF @study_state = 'C' INSERT INTO rif40.rif40_study_status(study_id, study_state, message) 
		VALUES (@study_id, 'C', 
			'Study has been created but it has not been verified.');
			
	IF @new_study_state = 'V' INSERT INTO rif40.rif40_study_status(study_id, study_state, message) 
		VALUES (@study_id, 'V', 
			'Study has been verified.');
			
--
-- Do update. This forces verification
-- (i.e. change in study_state on rif40_studies calls rif40.rif40_verify_state_change)
--
	SET @msg='55206: Start state transition (' + @study_state + '=>' + @new_study_state + ') for study ' + CAST(@study_id AS VARCHAR);
	PRINT @msg;
	UPDATE rif40.rif40_investigations SET investigation_state = @new_study_state WHERE study_id = @study_id AND investigation_state = @study_state;
	SET @actual_investigation_count = @@ROWCOUNT;
	IF @actual_investigation_count != @investigation_count BEGIN
--	
-- Error: [55207] Expecting to update %i %s(s), updated %i during state transition (%s=>%s) for study %i'.
--
		SET @err_msg = formatmessage(55207, @investigation_count, 'investigations', @actual_investigation_count, @study_state, @new_study_state, @study_id);
		THROW 55207, @err_msg, 1;
		END;

--
-- MUST USE TABLE NOT VIEWS WHEN USING LOCKS/WHERE CURRENT OF
--
	UPDATE rif40.rif40_studies SET study_state = @new_study_state WHERE study_id = @study_id AND study_state = @study_state;
	SET @actual_investigation_count = @@ROWCOUNT;
	IF @actual_investigation_count != @investigation_count BEGIN
--	
-- Error: [55207] Expecting to update %i %s(s), updated %i during state transition (%s=>%s) for study %i'.
--
		SET @err_msg = formatmessage(55207, @investigation_count, 'studies', @actual_investigation_count, @study_state, @new_study_state, @study_id);
		THROW 55207, @err_msg, 1;
		END;
	ELSE BEGIN
		SET @etp=GETDATE();
		SET @etime=CAST(@etp - @stp AS TIME);
		SET @msg='55208: Recurse (' + CAST(@n_recursion_level AS VARCHAR) + ') Completed state transition (' + @study_state + '=>' + @new_study_state + 
			') for study ' + CAST(@study_id AS VARCHAR) + ' with ' + CAST(@investigation_count AS VARCHAR) + ' investigation(s); time taken ' +
			CAST(CONVERT(VARCHAR(24), @etime, 14) AS VARCHAR);
		PRINT @msg;
		END;

--
-- Recurse until complete
--
	IF @new_study_state != @study_state AND @new_study_state IN ('V', 'E') BEGIN
			SET @msg='55209: Recurse (' + CAST(@n_recursion_level AS VARCHAR) + ') rif40_run_study using new state ' + 
				@new_study_state + ' for study ' + CAST(@study_id AS VARCHAR);
			PRINT @msg;
			EXECUTE @rval=rif40.rif40_run_study2
				@study_id 			/* Study_id */, 
				@debug 				/* Debug: 0/1 */, 
				@n_recursion_level 	/* Recursion level: Use default */;
			IF @rval = 0 BEGIN 		/* Halt on failure */
				SET @msg='WARNING 55210: Recurse (' + CAST(@n_recursion_level AS VARCHAR) + ') rif40_run_study to new state ' + 
				@new_study_state + ' for study ' + CAST(@study_id AS VARCHAR) + ' failed, see previous warnings';
				PRINT @msg;
				RETURN @rval;
			END;
		END;
	ELSE IF @new_study_state != @study_state AND @new_study_state = 'R' BEGIN
			SET @msg=@crlf + 
'************************************************************************' + @crlf +
'*                                                                      *' + @crlf +
'* 55211: Completed study ' + RIGHT(REPLICATE(' ',20)+(CAST(@study_id AS VARCHAR)), 20) + 
	'                          *' + @crlf +
'*                                                                      *' + @crlf +
'************************************************************************';
			PRINT @msg;
			SET @rval=1;
		END;
	ELSE BEGIN
		OPEN c1_runst; 
		FETCH NEXT FROM c1_runst INTO @study_state, @investigation_count;
		IF @@CURSOR_ROWS = 0 BEGIN
			CLOSE c1_runst;
			DEALLOCATE c1_runst;			
--
-- Error: [55212] Study ID %i not found, study in unexpected and unknown state	
--		
			SET @err_msg = formatmessage(55212, @study_id);
			THROW 55212, @err_msg, 1;			
		END;
--
-- Error: [55213] Study %i in unexpected state %s
--
		SET @err_msg = formatmessage(55213, @study_id, @study_state);
		THROW 55213, @err_msg, 1;
	END;

--
-- All recursion unwound
--
		SET @etp=GETDATE();
		SET @etime=CAST(@etp - @stp AS TIME);
	IF @recursion_level = 0 BEGIN
			SET @msg='55214: Recursion complete, state ' + @study_state + ', rif40_run_study study ' + CAST(@study_id AS VARCHAR) + 
				' with ' + CAST(@study_id AS VARCHAR) + ' investigation_count(s); time taken ' + CAST(CONVERT(VARCHAR(24), @etime, 14) AS VARCHAR);
			PRINT @msg;
		END;
	ELSE BEGIN
		SET @msg='55215: Recursion ' + CAST(@n_recursion_level AS VARCHAR) + ', state ' + @study_state + 
			', rif40_run_study study ' + CAST(@study_id AS VARCHAR) + ' with ' + CAST(@investigation_count AS VARCHAR) + 
			' investigation(s); time taken ' + CAST(CONVERT(VARCHAR(24), @etime, 14) AS VARCHAR);
		PRINT @msg;
	END;
--		
	RETURN @rval;
END;
GO

CREATE PROCEDURE [rif40].[rif40_run_study](@study_id INTEGER, @debug INTEGER=0, @rval INTEGER OUTPUT)
AS
BEGIN 
	SET NOCOUNT ON;
--
	IF @study_id IS NULL SET @study_id=[rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
    BEGIN TRANSACTION;
    DECLARE @rval2 INT=-2;
    DECLARE @msg VARCHAR(MAX);
-- ============================================================
    BEGIN TRY
         EXECUTE @rval2=rif40.rif40_run_study2
			 @study_id  /* Study_id */,
			 @debug     /* Debug: 0/1 */,
			 default    /* Recursion level: Use default */;
-- ============================================================
    END TRY
    BEGIN CATCH
         SET @rval=0;
         SET @msg='Caught error in rif40.rif40_run_study2(' + CAST(@study_id AS VARCHAR) + ')' + CHAR(10) +
			 'Error number: ' + NULLIF(CAST(ERROR_NUMBER() AS VARCHAR), 'N/A') +
			 '; severity: ' + NULLIF(CAST(ERROR_SEVERITY() AS VARCHAR), 'N/A') + 
			 '; state: ' + NULLIF(CAST(ERROR_STATE() AS VARCHAR), 'N/A') + CHAR(10) +
			 'Procedure: ' + NULLIF(ERROR_PROCEDURE(), 'N/A') +
			 ';  line: ' + NULLIF(CAST(ERROR_LINE() AS VARCHAR), 'N/A') + CHAR(10) +
			 'Error message: ' + NULLIF(ERROR_MESSAGE(), 'N/A') + CHAR(10);
         PRINT @msg;
--
-- Set study status 
--
		BEGIN TRY
			INSERT INTO rif40.rif40_study_status(study_id, study_state, message) VALUES(@study_id, 'G', @msg);
-- ============================================================
-- Always commit, even though this may fail because trigger failure have caused a rollback:
-- The COMMIT TRANSACTION request has no corresponding BEGIN TRANSACTION.
-- ============================================================
			COMMIT TRANSACTION;
		END TRY
		BEGIN CATCH	
			 SET @msg='Caught error handler error in rif40.rif40_run_study2(' + CAST(@study_id AS VARCHAR) + ')' + CHAR(10) +
				 'Error number: ' + NULLIF(CAST(ERROR_NUMBER() AS VARCHAR), 'N/A') +
				 '; severity: ' + NULLIF(CAST(ERROR_SEVERITY() AS VARCHAR), 'N/A') + 
				 '; state: ' + NULLIF(CAST(ERROR_STATE() AS VARCHAR), 'N/A') + CHAR(10) +
				 'Procedure: ' + NULLIF(ERROR_PROCEDURE(), 'N/A') +
				 ';  line: ' + NULLIF(CAST(ERROR_LINE() AS VARCHAR), 'N/A') + CHAR(10) +
				 'Error message: ' + NULLIF(ERROR_MESSAGE(), 'N/A') + CHAR(10);
			 PRINT @msg;
			 EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_run_study]';		
		END CATCH;		

--
-- Call error proc; throws error
--				
        EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_run_study]';
    END CATCH;
-- ============================================================
    IF @rval2 = 1 BEGIN
		SET @rval=1;
		SET @msg = 'Study extract ran ' + CAST(@study_id AS VARCHAR) + ' OK';
        PRINT @msg;
--
-- Set study status 
--
		INSERT INTO rif40.rif40_study_status(study_id, study_state, message) 
		SELECT @study_id, 'R', @msg
		 WHERE NOT EXISTS (
			SELECT study_id
			  FROM rif40.rif40_study_status
			 WHERE study_id = @study_id
			   AND study_state = 'R');		
-- ============================================================
-- Always commit, even though this may fail because trigger failure have caused a rollback:
-- The COMMIT TRANSACTION request has no corresponding BEGIN TRANSACTION.
-- ============================================================
		COMMIT TRANSACTION;		
		RETURN 1;
		END
    ELSE BEGIN
		SET @rval=0;
		SET @rval2=COALESCE(@rval2, -1);
		SET @msg = 'Study extract run ' + CAST(@study_id AS VARCHAR) + 
			' FAILED; rval2=' + CAST(@rval2 AS VARCHAR) + ' (see previous errors)';	
--
-- Set study status 
--
		INSERT INTO rif40.rif40_study_status(study_id, study_state, message) 
		SELECT @study_id, 'R', @msg
		 WHERE NOT EXISTS (
			SELECT study_id
			  FROM rif40.rif40_study_status
			 WHERE study_id = @study_id
			   AND study_state = 'R');	
-- ============================================================
-- Always commit, even though this may fail because trigger failure have caused a rollback:
-- The COMMIT TRANSACTION request has no corresponding BEGIN TRANSACTION.
-- ============================================================
		COMMIT TRANSACTION;	
        RAISERROR('Study extract run %i FAILED; rval2=%i (see previous errors)', 16, 1, @study_id, @rval2);
	END;
--
	RETURN -9;
END;
GO

GRANT EXECUTE ON [rif40].[rif40_run_study] TO rif_user, rif_manager;
GO

--
-- Eof