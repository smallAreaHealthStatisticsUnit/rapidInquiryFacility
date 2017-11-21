
-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 run study - execute insert statement
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
-- Error codes:  ..\..\error_handling\rif40_custom_error_messages.sql
-- 
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_execute_insert_statement]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_execute_insert_statement]
GO 

CREATE PROCEDURE [rif40].[rif40_execute_insert_statement](@rval INT OUTPUT,
	@study_id INT, @year_start INTEGER=NULL, @year_stop INTEGER=NULL, @debug INT=0)
AS
BEGIN
/*
Function:	rif40_execute_insert_statement()
Parameter:	Success or failure [INTEGER], study ID, start year, stop year, debug
Returns:	Success or failure [INTEGER], as  first parameter
Description:	Execute INSERT SQL statement
 */

--
-- Defaults if set to NULL
--
	IF @debug IS NULL SET @debug=0;
	
	SET @rval=1; 	-- Success

	DECLARE c1_dml CURSOR FOR
		SELECT sql_stmt, name
		  FROM ##g_insert_dml
		 WHERE study_id = @study_id
	DECLARE @sql_stmt	NVARCHAR(MAX);
	DECLARE @name		VARCHAR(20);
	DECLARE @rowcount	INTEGER;
--
	DECLARE @statement_number INTEGER;
	DECLARE @statement_number_table TABLE (statement_number INTEGER);
	DECLARE @etime DATETIME, @stp DATETIME=GETDATE(), @etp DATETIME;
--
	DECLARE @crlf  		VARCHAR(2)=CHAR(10)+CHAR(13);
	DECLARE @err_msg 	NVARCHAR(2048);
--
	OPEN c1_dml;
	FETCH NEXT FROM c1_dml INTO @sql_stmt, @name;
	WHILE @@FETCH_STATUS = 0
	BEGIN	
		IF @sql_stmt IS NOT NULL BEGIN
			BEGIN TRY
				IF @study_id IS NULL BEGIN
					SET @err_msg = formatmessage(56600); 
					THROW 56600, @err_msg, 1;
				END;
				IF @year_start IS NULL BEGIN
					SET @err_msg = formatmessage(56601,@study_id); 
					THROW 56601, @err_msg, 1;
				END;
				IF @year_stop IS NULL BEGIN
					SET @err_msg = formatmessage(56602, @study_id); 
					THROW 56602, @err_msg, 2;
				END;
				
				EXECUTE sp_executesql @sql_stmt,  
					N'@studyid INTEGER, @yearstart INTEGER, @yearstop INTEGER',
					@studyid=@study_id,
					@yearstart=@year_start,
					@yearstop=@year_stop;
				SET @rowcount=@@ROWCOUNT;
				
				SET @etp=GETDATE();
				SET @etime=CAST(@etp - @stp AS TIME);				
				INSERT INTO rif40.rif40_study_sql_log(
					study_id, statement_type, log_message, log_sqlcode, elapsed_time, [rowcount])
				OUTPUT inserted.statement_number INTO @statement_number_table
				VALUES (@study_id, 'RUN_STUDY', 'OK', 0, CONVERT(INT, @etime), @@ROWCOUNT);
				SET @statement_number = (SELECT TOP 1 statement_number FROM @statement_number_table);
				INSERT INTO rif40.rif40_study_sql(
					study_id, statement_type, sql_text, statement_number, line_number, status)
				VALUES (@study_id, 'RUN_STUDY', SUBSTRING(@sql_stmt, 1, 4000), @statement_number, 1, 'R');
				
				IF @rowcount > 0 
					PRINT 'SQL[' + USER + '] study_id: ' + CAST(@study_id AS VARCHAR) + 
						'; start: ' + CAST(@year_start AS VARCHAR) +
						'; stop: ' + CAST(@year_stop AS VARCHAR) +
						' extract insert OK: ' + COALESCE(@name, 'No name') +
						'; rows: ' + CAST(@rowcount AS VARCHAR)
				ELSE BEGIN
					PRINT '[55820] SQL[' + USER + '] study_id: ' + CAST(@study_id AS VARCHAR);
					SET @err_msg = formatmessage(55820, @study_id); 
						-- Study ID %i no rows INSERTED into extract table.
					THROW 55820, @err_msg, 1;
				END;

			END TRY
			BEGIN CATCH	
				
				SET @etp=GETDATE();
				SET @etime=CAST(@etp - @stp AS TIME);				
				INSERT INTO rif40.rif40_study_sql_log(
					study_id, statement_type, log_message, log_sqlcode, elapsed_time, [rowcount])
				OUTPUT inserted.statement_number INTO @statement_number_table
				VALUES (@study_id, 'RUN_STUDY', COALESCE(error_message(), 'No error!'), ERROR_NUMBER(), CONVERT(INT, @etime), 0);
				SET @statement_number = (SELECT TOP 1 statement_number FROM @statement_number_table);
				INSERT INTO rif40.rif40_study_sql(
					study_id, statement_type, sql_text, statement_number, line_number, status)
				VALUES (@study_id, 'RUN_STUDY', SUBSTRING(@sql_stmt, 1, 4000), @statement_number, 1, 'R');
				
	--	 		[55999] SQL statement had error: %s%sSQL[%s]> %s;	
				IF LEN(@sql_stmt) > 1900 BEGIN	
					SET @err_msg = formatmessage(56699, error_message(), @crlf, USER, '[SQL statement too long for error; see SQL above]');
					PRINT '[56699] SQL> ' + SUBSTRING(@sql_stmt, 1, 1900);
					END;			
				ELSE SET @err_msg = formatmessage(56699, error_message(), @crlf, USER, @sql_stmt); 
				THROW 56699, @err_msg, 1;
			END CATCH;
		END;
--
		FETCH NEXT FROM c1_dml INTO @sql_stmt, @name;
	END;
	CLOSE c1_dml;
	DEALLOCATE c1_dml;
	
	RETURN @rval;
END;
GO

--
-- Eof	