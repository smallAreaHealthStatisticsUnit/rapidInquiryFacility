-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 ddl
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

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_ddl]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_ddl];
GO 
		   
IF EXISTS (SELECT *
           FROM   sys.table_types WHERE name = 'Sql_stmt_table')
	DROP TYPE [rif40].[Sql_stmt_table];
GO 

CREATE TYPE [rif40].[Sql_stmt_table] AS TABLE ( 
	sql_stmt 	NVARCHAR(MAX),
	study_id	INTEGER	DEFAULT NULL
	);  
GO  

CREATE PROCEDURE [rif40].[rif40_ddl](@sql_stmts rif40.Sql_stmt_table READONLY, @debug INTEGER=0)
AS
BEGIN
/*
 Function:		rif40_ddl()
 Parameter:		SQL statements table(sql_stmt VARCHAR(MAX)), debug flag (0/1) [Default: 0]
 Returns:		Success or failure [INTEGER]
 Description:	Run DDL.
 */
 
--
-- Defaults if set to NULL
--
	IF @debug IS NULL SET @debug=0;

	DECLARE @rval	INTEGER=1; 	-- Success
	
	DECLARE c1_ddl CURSOR FOR
		SELECT sql_stmt, study_id
		  FROM @sql_stmts;
	DECLARE @sql_stmt			NVARCHAR(MAX);
	DECLARE @study_id			INTEGER;
--
	DECLARE @crlf  		VARCHAR(2)=CHAR(10)+CHAR(13);
	DECLARE @err_msg 	NVARCHAR(2048);
--
	DECLARE @statement_number INTEGER;
	DECLARE @statement_number_table TABLE (statement_number INTEGER);
	DECLARE @etime DATETIME, @stp DATETIME=GETDATE(), @etp DATETIME;
--
	DECLARE @psql_stmt NVARCHAR(MAX);
	DECLARE @sql_frag VARCHAR(4000);
--
	OPEN c1_ddl;
	FETCH NEXT FROM c1_ddl INTO @sql_stmt, @study_id;
	WHILE @@FETCH_STATUS = 0
	BEGIN	
		BEGIN TRY
			IF @study_id IS NULL BEGIN
				EXECUTE sp_executesql @sql_stmt;
				PRINT 'SQL[' + USER + '] OK> ';
				-- 		EXPERIMENTAL CODE TO SPLIT SQL INTO LINES WITH SEPARATE PRINT FOR TOMCAT 
				SET @psql_stmt = REPLACE(@sql_stmt, @crlf, '|');
				SET @sql_frag = NULL;
				WHILE LEN(@psql_stmt) > 0
				BEGIN
					IF PATINDEX('%|%', @psql_stmt) > 0
					BEGIN
						SET @sql_frag = SUBSTRING(@psql_stmt,
													0,
													PATINDEX('%|%', @psql_stmt))
						PRINT @sql_frag

						SET @psql_stmt = SUBSTRING(@psql_stmt,
												  LEN(@sql_frag + '|') + 1,
												  LEN(@psql_stmt))
					END
					ELSE
					BEGIN
						SET @sql_frag = @psql_stmt
						SET @psql_stmt = NULL
						PRINT @sql_frag
					END
				END; 
			END;
			ELSE BEGIN
				EXECUTE sp_executesql @sql_stmt,  
					N'@study_id INTEGER',  
					@study_id;

				SET @etp=GETDATE();
				SET @etime=CAST(@etp - @stp AS TIME);					
				INSERT INTO rif40.rif40_study_sql_log(
					study_id, statement_type, log_message, log_sqlcode, elapsed_time, [rowcount])
				OUTPUT inserted.statement_number INTO @statement_number_table
				VALUES (@study_id, 'RUN_STUDY', 'OK', 0, CONVERT(INT, @etime), @@ROWCOUNT);
				SET @statement_number = (SELECT TOP 1 statement_number FROM @statement_number_table);
				INSERT INTO rif40.rif40_study_sql(
					study_id, statement_type, sql_text, statement_number, line_number, status)
				VALUES (@study_id, 'RUN_STUDY', @sql_stmt, @statement_number, 1, 'R');
				PRINT 'SQL[' + USER + '; study_id: ' + CAST(@study_id AS VARCHAR) + '; no: ' + 
					COALESCE(CAST(@statement_number AS VARCHAR), 'NULL') + 
					'; time taken ' + CAST(CONVERT(VARCHAR(24), @etime, 14) AS VARCHAR) + '] OK> ';
					
-- 		EXPERIMENTAL CODE TO SPLIT SQL INTO LINES WITH SEPARATE PRINT FOR TOMCAT 
				SET @psql_stmt = REPLACE(@sql_stmt, @crlf, '|');
				SET @sql_frag = NULL;
				WHILE LEN(@psql_stmt) > 0
				BEGIN
					IF PATINDEX('%|%', @psql_stmt) > 0
					BEGIN
						SET @sql_frag = SUBSTRING(@psql_stmt,
													0,
													PATINDEX('%|%', @psql_stmt))
						PRINT @sql_frag

						SET @psql_stmt = SUBSTRING(@psql_stmt,
												  LEN(@sql_frag + '|') + 1,
												  LEN(@psql_stmt))
					END
					ELSE
					BEGIN
						SET @sql_frag = @psql_stmt
						SET @psql_stmt = NULL
						PRINT @sql_frag
					END
				END; 					
				
			END;
		END TRY
		BEGIN CATCH		
--	 		[55999] SQL statement had error: %s%sSQL[%s]> %s;	
			IF LEN(@sql_stmt) > 1900 BEGIN	
				SET @err_msg = formatmessage(55999, COALESCE(error_message(), 'No error!'), @crlf, USER, '[SQL statement too long for error; see SQL above]');
				PRINT '[55999] SQL> ' + @sql_stmt;
			END
			ELSE SET @err_msg = formatmessage(55999, COALESCE(error_message(), 'No error!'), @crlf, USER, @sql_stmt); 
			
			IF @err_msg IS NULL BEGIN
				SET @err_msg = '[55998] [formatmessage issue] SQL statement had error: ' + 
					COALESCE(error_message(), 'No error!') +  @crlf + 'SQL[' + USER + ']';
			END;

			PRINT @err_msg;

			SET @etp=GETDATE();
			SET @etime=CAST(@etp - @stp AS TIME);				
			INSERT INTO rif40.rif40_study_sql_log(
				study_id, statement_type, log_message, log_sqlcode, elapsed_time, [rowcount])
			OUTPUT inserted.statement_number INTO @statement_number_table
			VALUES (@study_id, 'RUN_STUDY', COALESCE(error_message(), 'No error!'), ERROR_NUMBER(), CONVERT(INT, @etime), 0);
			SET @statement_number = (SELECT TOP 1 statement_number FROM @statement_number_table);
			INSERT INTO rif40.rif40_study_sql(
				study_id, statement_type, sql_text, statement_number, line_number, status)
			VALUES (@study_id, 'RUN_STUDY', @sql_stmt, @statement_number, 1, 'R');
			
			PRINT 'SQL[' + USER + '; study_id: ' + CAST(@study_id AS VARCHAR) + '; no: ' + 
				COALESCE(CAST(@statement_number AS VARCHAR), 'NULL') + '] OK> ';
				
-- 		EXPERIMENTAL CODE TO SPLIT SQL INTO LINES WITH SEPARATE PRINT FOR TOMCAT 
			SET @psql_stmt = REPLACE(@sql_stmt, @crlf, '|');
			SET @sql_frag = NULL;
			WHILE LEN(@psql_stmt) > 0
			BEGIN
				IF PATINDEX('%|%', @psql_stmt) > 0
				BEGIN
					SET @sql_frag = SUBSTRING(@psql_stmt,
												0,
												PATINDEX('%|%', @psql_stmt))
					PRINT @sql_frag

					SET @psql_stmt = SUBSTRING(@psql_stmt,
											  LEN(@sql_frag + '|') + 1,
											  LEN(@psql_stmt))
				END
				ELSE
				BEGIN
					SET @sql_frag = @psql_stmt
					SET @psql_stmt = NULL
					PRINT @sql_frag
				END
			END; 
						
			THROW 55999, @err_msg, 1;
		END CATCH;
--
		FETCH NEXT FROM c1_ddl INTO @sql_stmt, @study_id;
	END;
	CLOSE c1_ddl;
	DEALLOCATE c1_ddl;
	
	RETURN @rval;
END;
GO

--
-- Eof