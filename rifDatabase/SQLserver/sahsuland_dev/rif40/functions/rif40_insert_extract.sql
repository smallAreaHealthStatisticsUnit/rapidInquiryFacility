
-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 run study - insert extract
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
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_insert_extract]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_insert_extract]
GO 

CREATE PROCEDURE [rif40].[rif40_insert_extract](@rval INT OUTPUT, @study_id INT, @debug INT=0)
AS
BEGIN
/*
Function:	rif40_insert_extract()
Parameter:	Success or failure [INTEGER], Study ID
Returns:	Success or failure [INTEGER], as  first parameter
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Insert data into extract table

 */

--
-- Defaults if set to NULL
--
	IF @debug IS NULL SET @debug=0;
	
	SET @rval=1; 	-- Success
	
	DECLARE c1insext2 CURSOR FOR
		SELECT study_id, year_start, year_stop 
		  FROM rif40_studies a
		 WHERE a.study_id = @study_id;
	DECLARE @c1_rec_study_id INTEGER;
	DECLARE @c1_rec_year_start INTEGER;
	DECLARE @c1_rec_year_stop INTEGER;
--
	DECLARE @sql_stmt		NVARCHAR(MAX);
	DECLARE @ddl_stmts 	Sql_stmt_table;
	DECLARE @t_ddl		INTEGER=0;
--
	DECLARE @etime DATETIME, @stp DATETIME=GETDATE(), @etp DATETIME;
--
	DECLARE @crlf  		VARCHAR(2)=CHAR(10)+CHAR(13);
	DECLARE @tab		VARCHAR(1)=CHAR(9);
	DECLARE @err_msg 	VARCHAR(MAX);
	DECLARE @msg	 	VARCHAR(MAX);
--
	DECLARE @yearno		INTEGER=0;
--
	OPEN c1insext2;	
	FETCH NEXT FROM c1insext2 INTO @c1_rec_study_id, @c1_rec_year_start, @c1_rec_year_stop;
	IF @@CURSOR_ROWS = 0 BEGIN
		CLOSE c1insext2;
		DEALLOCATE c1insext2;
		SET @err_msg = formatmessage(55800, @study_id); -- Study ID %i not found
		THROW 55800, @err_msg, 1;
	END;
	CLOSE c1insext2;
	DEALLOCATE c1insext2;
	
--
-- Study area insert
--
	SET @sql_stmt='SELECT study_id, area_id, band_id' + @crlf +
		'  INTO #g_rif40_study_areas' + @crlf + 
		'  FROM rif40.rif40_study_areas' + @crlf +
		' WHERE study_id = @study_id /* Current study ID */' + @crlf +
		' ORDER BY study_id, area_id, band_id';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt, study_id) VALUES (@sql_stmt, @study_id);
	
--
-- Comparison area insert
--
	SET @sql_stmt='SELECT study_id, area_id' + @crlf +
		'  INTO #g_rif40_comparison_areas' + @crlf + 
		'  FROM rif40.rif40_comparison_areas' + @crlf +
		' WHERE study_id = @study_id /* Current study ID */' + @crlf +
		' ORDER BY study_id, area_id';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt, study_id) VALUES (@sql_stmt, @study_id);

--
-- Do insert
--
	EXECUTE rif40.rif40_ddl
			@rval		/* Result: 0/1 */,
			@ddl_stmts	/* SQL table */,
			@debug		/* enable debug: 0/1) */;
			
--
-- Study extract insert
--	
-- This will eventually support paralleisation. Do year by year for the moment
--
	SET @yearno=@c1_rec_year_start;
	WHILE @yearno < @c1_rec_year_stop BEGIN

		SET @msg='[55801] Study ID ' + CAST(@c1_rec_study_id AS VARCHAR) + ' INSERT study year ' + CAST(@yearno AS VARCHAR);
		PRINT @msg;		
--
-- Study extract insert
-- This will eventually support paralleisation. Do year by year for the moment
--
		EXECUTE rif40.rif40_create_insert_statement 
			@rval, 
			@c1_rec_study_id,
			'S' /* study or comparison */,
			@yearno /* Year */,
			@debug  /* Year start, year stop not used */;
		IF @rval = 0 RETURN @rval;

--
-- Comparison extract insert
-- This will eventually support paralleisation. Do year by year for the moment
--
		EXECUTE rif40.rif40_create_insert_statement 
			@rval, 
			@c1_rec_study_id,
			'C' /* study or comparison */,
			@yearno /* Year */,
			@debug;
		IF @rval = 0 RETURN @rval;

	   SET @yearno = @yearno + 1;
	END;
--
	SET @etp=GETDATE();
	SET @etime=CAST(@etp - @stp AS TIME);
	SET @msg='[55803] Study ID ' + CAST(@c1_rec_study_id AS VARCHAR) + ' extract table INSERT completed in ' + 
		CAST(CONVERT(VARCHAR(24), @etime, 14) AS VARCHAR);		
	PRINT @msg;		
--
	RETURN @rval;
END;
GO

--
-- Eof