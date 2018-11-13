
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
		SELECT study_id, year_start, year_stop, extract_table, study_type
		  FROM rif40_studies a
		 WHERE a.study_id = @study_id;
	DECLARE @c1_rec_study_id INTEGER;
	DECLARE @c1_rec_year_start INTEGER;
	DECLARE @c1_rec_year_stop INTEGER;
	DECLARE @c1_rec_extract_table		VARCHAR(30);
	DECLARE @c1_rec_study_type INTEGER;
--
	DECLARE @sql_stmt		NVARCHAR(MAX);
	DECLARE @ddl_stmts 		Sql_stmt_table;
	DECLARE @t_ddl			INTEGER=0;
	
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
	FETCH NEXT FROM c1insext2 INTO @c1_rec_study_id, @c1_rec_year_start, @c1_rec_year_stop, @c1_rec_extract_table,
		@c1_rec_study_type;
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
	SET @sql_stmt='IF OBJECT_ID(''tempdb..##g_rif40_study_areas'') IS NOT NULL' + @crlf + 
			'DROP TABLE ##g_rif40_study_areas';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt, study_id) VALUES (@sql_stmt, @study_id);
--
	IF @c1_rec_study_type != 1 /* Risk Analysis */ 
		SET @sql_stmt='SELECT study_id, area_id, band_id, intersect_count, distance_from_nearest_source, nearest_rifshapepolyid, exposure_value' + @crlf +
			'  INTO ##g_rif40_study_areas' + @crlf + 
			'  FROM rif40.rif40_study_areas' + @crlf +
			' WHERE study_id = @study_id /* Current study ID */' + @crlf +
			' ORDER BY study_id, area_id, band_id';
	ELSE SET @sql_stmt='SELECT study_id, area_id, band_id' + @crlf +
			'  INTO ##g_rif40_study_areas' + @crlf + 
			'  FROM rif40.rif40_study_areas' + @crlf +
			' WHERE study_id = @study_id /* Current study ID */' + @crlf +
			' ORDER BY study_id, area_id, band_id';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt, study_id) VALUES (@sql_stmt, @study_id);	
	
--
-- Comparison area insert
--	
	SET @sql_stmt='IF OBJECT_ID(''tempdb..##g_rif40_comparison_areas'') IS NOT NULL' + @crlf + 
			'DROP TABLE ##g_rif40_comparison_areas';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt, study_id) VALUES (@sql_stmt, @study_id);
--
	SET @sql_stmt='SELECT study_id, area_id' + @crlf +
		'  INTO ##g_rif40_comparison_areas' + @crlf + 
		'  FROM rif40.rif40_comparison_areas' + @crlf +
		' WHERE study_id = @study_id /* Current study ID */' + @crlf +
		' ORDER BY study_id, area_id';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt, study_id) VALUES (@sql_stmt, @study_id);

--
-- Create table for insert DML
--
	SET @sql_stmt='IF OBJECT_ID(''tempdb..##g_insert_dml'') IS NULL' + @crlf + 
		'CREATE TABLE ##g_insert_dml(study_id INTEGER, name VARCHAR(20), sql_stmt NVARCHAR(MAX))';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt, study_id) VALUES (@sql_stmt, @study_id);
	
--
-- Do insert
--
	EXECUTE @rval=rif40.rif40_ddl
			@ddl_stmts	/* SQL table */,
			@debug		/* enable debug: 0/1) */;
	SET @sql_stmt=NULL;	
	DELETE FROM @ddl_stmts; /* Purge */	
	
--
-- For for rows in ##g_rif40_comparison_areas, ##g_rif40_study_areas
--
	DECLARE @g_rif40_study_areas_check INTEGER = (SELECT COUNT(study_id) FROM ##g_rif40_study_areas);
	IF @g_rif40_study_areas_check > 0 
			PRINT 'SQL[' + USER + '] study_id: ' + CAST(@study_id AS VARCHAR) + 
				' ##g_rif40_study_areas insert OK; rows: ' + CAST(@g_rif40_study_areas_check AS VARCHAR)
	ELSE BEGIN	
		SET @err_msg = formatmessage(55801, @study_id); 
			-- Study ID %i no rows INSERTED into ##g_rif40_study_areas temporary table.
		THROW 55801, @err_msg, 1;
	END;
	
	DECLARE @g_rif40_comparison_areas_check INTEGER = (SELECT COUNT(study_id) FROM ##g_rif40_study_areas);
	IF @g_rif40_comparison_areas_check > 0 
			PRINT 'SQL[' + USER + '] study_id: ' + CAST(@study_id AS VARCHAR) + 
				' ##g_rif40_comparison_areas insert OK; rows: ' + CAST(@g_rif40_comparison_areas_check AS VARCHAR)
	ELSE BEGIN	
		SET @err_msg = formatmessage(55802, @study_id); 
			-- Study ID %i no rows INSERTED into ##g_rif40_comparison_areas temporary table.
		THROW 55802, @err_msg, 1;
	END;	
	
--
-- Study extract insert
--	
-- This will eventually support paralleisation. Do year by year for the moment
--

--
-- Study extract insert
-- This will eventually support paralleisation. Do year by year for the moment
--	
	EXECUTE @rval=rif40.rif40_create_insert_statement  
		@c1_rec_study_id			/* Study ID */,
		'S' 						/* study or comparison */,
		@c1_rec_year_start			/* Year start */,
		@c1_rec_year_stop			/* Year stop */,
		@debug						/* Debug */;
	IF @rval = 0 RETURN @rval;

--
-- Comparison extract insert
-- This will eventually support paralleisation. Do year by year for the moment
--	
	EXECUTE @rval=rif40.rif40_create_insert_statement 
		@c1_rec_study_id			/* Study ID */,
		'C' 						/* study or comparison */,
		@c1_rec_year_start			/* Year start */,
		@c1_rec_year_stop			/* Year stop */,
		@debug						/* Debug */;
	IF @rval = 0 RETURN @rval;
		
	SET @yearno=@c1_rec_year_start;
	WHILE @yearno <= @c1_rec_year_stop BEGIN

		SET @msg='55801: Study ID ' + CAST(@c1_rec_study_id AS VARCHAR) + ' INSERT study year ' + CAST(@yearno AS VARCHAR);
		PRINT @msg;		
		
--
-- Do insert
--
		EXECUTE rif40.rif40_execute_insert_statement
			@rval		/* Result: 0/1 */,
			@study_id	/* Study ID */, 
			@yearno		/* Year to be extracted [start] */,
			@yearno		/* Year to be extracted [stop] */,
			@debug		/* enable debug: 0/1) */;
			
	   SET @yearno = @yearno + 1;
	END;
--
	SET @sql_stmt='SELECT @totalOUT=COUNT(*) FROM rif_studies.' + LOWER(@c1_rec_extract_table); 
	DECLARE @ParmDefinition nvarchar(500) = N'@totalOUT int OUTPUT', @total int=0;
	EXEC sp_executesql @sql_stmt, @ParmDefinition, @totalOUT=@total OUTPUT;	
--	
	SET @etp=GETDATE();
	SET @etime=CAST(@etp - @stp AS TIME);
	IF @total > 0 BEGIN
		SET @msg='55803: Study ID ' + CAST(@c1_rec_study_id AS VARCHAR) + ' extract table INSERT completed in ' + 
			CAST(CONVERT(VARCHAR(24), @etime, 14) AS VARCHAR) + 
			'; rows: ' + CAST(@total AS VARCHAR);		
		PRINT @msg;	
		END
	ELSE BEGIN
		SET @err_msg = formatmessage(55803, @study_id); 
			-- Study ID %i no rows INSERTED into extract table.
		THROW 55803, @err_msg, 1;	
	END;
--
	RETURN @rval;
END;
GO

--
-- Eof