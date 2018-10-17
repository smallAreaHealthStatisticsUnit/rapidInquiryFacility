-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 run study - compute results
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
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_compute_results]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_compute_results]
GO 

CREATE PROCEDURE [rif40].[rif40_compute_results](@study_id INT, @debug INT)
WITH EXECUTE AS 'rif40' /* So as to be owned by RIF40 */
AS
BEGIN	
/*
Function:	rif40_compute_results()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Compute results from extract table. Create map table

INSERT INTO rif40_results (study_id, inv_id, band_id, genders, direct_standardisation, adjusted, observed)
WITH a AS (
	SELECT study_id, band_id, sex,
	       SUM(COALESCE(inv_1, 0)) AS inv_1_observed
	  FROM s217_extract
	 WHERE study_or_comparison = 'S'
	 GROUP BY study_id, band_id, sex
)
SELECT study_id, 217 AS inv_id, band_id, 1 AS genders, 0 AS direct_standardisation, 0 AS adjusted, inv_1_observed AS observed
  FROM a
 WHERE sex = 1
UNION
SELECT study_id, 217 AS inv_id, band_id, 2 AS genders, 0 AS direct_standardisation, 0 AS adjusted, inv_1_observed AS observed
  FROM a
 WHERE sex = 2
UNION
SELECT study_id, 217 AS inv_id, band_id, 3 AS genders, 0 AS direct_standardisation, 0 AS adjusted, SUM(COALESCE(inv_1_observed, 0)) AS observed
  FROM a
 GROUP BY study_id, band_id
 ORDER BY 1, 2, 3, 4, 5, 6;

Map table for disease maps supports area_id, gid and gid_rowindex for faster middleware support
This may be added to extract creation later.

 */

--
-- Defaults if set to NULL
--
	DECLARE @rval INTEGER=0; 	-- Success	
	
	DECLARE c1comp CURSOR FOR
		SELECT study_id, extract_table, geography, study_type, map_table, extract_permitted,
			   description, study_geolevel_name
		  FROM rif40.rif40_studies a
		 WHERE a.study_id = @study_id;
	DECLARE c2comp CURSOR FOR
		SELECT inv_id, inv_name, numer_tab, inv_description
		  FROM rif40.rif40_investigations a
		 WHERE a.study_id = @study_id
		 ORDER BY inv_id;
	DECLARE c3comp CURSOR FOR
		SELECT grantee_username
		  FROM rif40.rif40_study_shares a
		 WHERE a.study_id = @study_id;
	DECLARE c4comp CURSOR FOR
		SELECT a.column_name, CAST (b.value AS VARCHAR) AS column_comment
		  FROM information_schema.columns a, 
			   fn_listextendedproperty('MS_Description', 
					'schema', 'rif40', 'table', 't_rif40_results', 'column', default) b
		 WHERE a.table_name  = 't_rif40_results'
		   AND a.column_name COLLATE SQL_Latin1_General_CP1_CI_AS = 
					b.objname COLLATE SQL_Latin1_General_CP1_CI_AS
		   AND b.objtype     = 'COLUMN';

--
	DECLARE @c1_rec_study_id 		INTEGER;
	DECLARE @c1_rec_extract_table	VARCHAR(30);
	DECLARE @c1_rec_geography		VARCHAR(30);
	DECLARE @c1_rec_study_type		INTEGER;
	DECLARE @c1_rec_map_table		VARCHAR(30);
	DECLARE @c1_rec_extract_permitted INTEGER;
	DECLARE @c1_rec_description	 	VARCHAR(250);
	DECLARE @c1_rec_study_geolevel_name	VARCHAR(30);
--	
	DECLARE @c2_rec_inv_id 			INTEGER;
	DECLARE @c2_rec_inv_name		VARCHAR(20);
	DECLARE @c2_rec_numer_tab 		VARCHAR(30);
	DECLARE @c2_rec_inv_description VARCHAR(250);
--
	DECLARE @c3_rec_grantee_username VARCHAR(30);
	
	DECLARE @c4_rec_column_name 	VARCHAR(30);
	DECLARE @c4_rec_column_comment 	VARCHAR(MAX);
--
	DECLARE @inv_array TABLE (inv_id INTEGER);
	DECLARE c6comp CURSOR FOR 
		SELECT inv_id
		  FROM @inv_array;
	DECLARE @c6_rec_inv_id 			INTEGER;		  
--
	DECLARE @i			INTEGER=0;
--
	DECLARE @sql_stmt 	NVARCHAR(MAX);
--		  
	DECLARE @dml_stmts 	Sql_stmt_table;
	DECLARE @t_dml		INTEGER=0;
--		  
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
-- Use caller execution context to query RIF views
--
	EXECUTE AS CALLER /* RIF user */;
--
	OPEN c1comp;
	FETCH NEXT FROM c1comp INTO @c1_rec_study_id, @c1_rec_extract_table, @c1_rec_geography,
		@c1_rec_study_type, @c1_rec_map_table, @c1_rec_extract_permitted, @c1_rec_description,
		@c1_rec_study_geolevel_name;
	IF @@CURSOR_ROWS = 0 BEGIN
		CLOSE c1comp;
		DEALLOCATE c1comp;
		SET @err_msg = formatmessage(55600, @study_id); -- Study ID %i not found
		THROW 55600, @err_msg, 1;
	END;
	CLOSE c1comp;
	DEALLOCATE c1comp;
	
--
-- Calculate observed
--
-- [No genders support]
--
	SET @sql_stmt='WITH a AS (' + @crlf +
		@tab + 'SELECT study_id, band_id, sex,';				/* Already banded */
--
	OPEN c2comp;
	FETCH NEXT FROM c2comp INTO @c2_rec_inv_id, @c2_rec_inv_name, @c2_rec_numer_tab, 
		@c2_rec_inv_description;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i=@i+1;		
		INSERT INTO @inv_array(inv_id) VALUES (@c2_rec_inv_id);
--
		IF @i = 1 SET @sql_stmt=@sql_stmt + @crlf + @tab + 
			'       SUM(COALESCE(' + LOWER(@c2_rec_inv_name) + ', 0)) AS inv_' + CAST(@i AS VARCHAR) + 
			'_observed' + @tab + '/* ' + CAST (@c2_rec_inv_id AS VARCHAR) + ' -  ' + @c2_rec_numer_tab + ' - ' + 
			@c2_rec_inv_description + ' */'
		ELSE SET @sql_stmt=@sql_stmt + ',' + @crlf + @tab + 
			'       SUM(COALESCE(' + LOWER(@c2_rec_inv_name) + ', 0)) AS inv_' + CAST(@i AS VARCHAR) + 
			'_observed' + @tab + '/* ' + CAST(@c2_rec_inv_id AS VARCHAR) + ' -  ' + @c2_rec_numer_tab + ' - ' + 
			@c2_rec_inv_description + ' */'
--
		FETCH NEXT FROM c2comp INTO @c2_rec_inv_id, @c2_rec_inv_name, @c2_rec_numer_tab, 
			@c2_rec_inv_description;
	END;
	CLOSE c2comp;
	DEALLOCATE c2comp;	
	SET @sql_stmt=@sql_stmt + @crlf + '	  FROM rif_studies.' + LOWER(@c1_rec_extract_table) + @crlf + 
		'	 WHERE study_or_comparison = ''S''' + @crlf +
		'	 GROUP BY study_id, band_id, sex' + @crlf +
		')' + @crlf;

--
	SET @sql_stmt=@sql_stmt +
		'INSERT INTO rif40.rif40_results' + @crlf + @tab +
		'(study_id, inv_id, band_id, genders, direct_standardisation, adjusted, observed)' + 
		@crlf;

	SET @i=0;
	OPEN c6comp;
	FETCH NEXT FROM c6comp INTO @c6_rec_inv_id;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i=@i+1; /* XXXX Reset i in PG */
		IF @i > 1 SET @sql_stmt=@sql_stmt + 'UNION' + @crlf;
		SET @sql_stmt=@sql_stmt +
			'SELECT study_id, ' + CAST (@c6_rec_inv_id AS VARCHAR) + ' AS inv_id, band_id,' +
				' 1 AS genders,' + @crlf + 
			'       0 /* Indirect */ AS direct_standardisation, 0 /* Unadjusted */ AS adjusted,' +
				' inv_' +
				 CAST(@i AS VARCHAR) + '_observed AS observed' + @crlf +
			'  FROM a' + @crlf +
			' WHERE sex = 1' + @crlf +
			'UNION' + @crlf +
			'SELECT study_id, ' + CAST (@c6_rec_inv_id AS VARCHAR) + ' AS inv_id, band_id,' +
				' 2 AS genders,' + @crlf + 
			'       0 /* Indirect */ AS direct_standardisation, 0 /* Unadjusted */ AS adjusted,' +
				' inv_' +
				 CAST(@i AS VARCHAR) + '_observed AS observed' + @crlf +
			'  FROM a' + @crlf +
			' WHERE sex = 2' + @crlf +
			'UNION' + @crlf +
			'SELECT study_id, ' + CAST (@c6_rec_inv_id AS VARCHAR) + ' AS inv_id, band_id,' +
				' 3 /* both */ AS genders,' + @crlf + 
			'       0 /* Indirect */ AS direct_standardisation, 0 /* Unadjusted */'+
				' AS adjusted, SUM(COALESCE(inv_' +
				 CAST(@i AS VARCHAR) + '_observed, 0)) AS observed' + @crlf +
			'  FROM a' + @crlf +
			' GROUP BY study_id, band_id' + @crlf;		
--
		FETCH NEXT FROM c6comp INTO @c6_rec_inv_id;
	END;
	CLOSE c6comp;
	DEALLOCATE c6comp;
--
	SET @sql_stmt=@sql_stmt + ' ORDER BY 1, 2, 3, 4, 5, 6';
--	
	SET @t_dml=@t_dml+1;	
	INSERT INTO @dml_stmts(sql_stmt) VALUES (@sql_stmt);	
 
--
-- Populate rif40_results from extract table
--
	EXECUTE @rval=rif40.rif40_ddl
			@dml_stmts	/* SQL table */,
			@debug		/* enable debug: 0/1) */;
	IF @rval = 0 BEGIN
			SET @msg='55601: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
				' populate rif40_results from extract table failed, see previous warnings'	/* Study id */;
			PRINT @msg;
			RETURN @rval;
		END; 
	ELSE BEGIN
			SET @msg='55602: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
				' populate rif40_results from extract table OK'	/* Study id */ +
				@crlf + 'SQL>' + @sql_stmt;
			PRINT @msg;
		END; 
		
--
-- Create map table [DOES NOT CREATE ANY ROWS]
--
-- [CONTAINS NO GEOMETRY]
--
	IF @c1_rec_study_type = 1 /* Disease mapping */ SET @sql_stmt='SELECT TOP 1 ' +
--
-- GID_ROWINDEX support in maps (extracts subject to performance tests)
--
-- LPAD(YourFieldValue, 10, '0') becomes
-- RIGHT(REPLICATE('0', 10) + YourFieldValue,10)
--
			'REPLICATE(''X'', 60) AS gid,' + @crlf +
			'       RIGHT(REPLICATE(''0'', 10) + CAST(ROW_NUMBER() OVER(' + @crlf +
			'              ORDER BY a.study_id, a.band_id, a.inv_id, a.genders, a.adjusted, a.direct_standardisation' + @crlf +
			'              ) AS VARCHAR), 10) AS gid_rowindex,'+ @crlf +
			'       REPLICATE(''X'', 60) AS area_id,' + @crlf +
			'       a.*' + @crlf +
			'  INTO rif_studies.' + LOWER(@c1_rec_map_table) + @crlf +
			'  FROM rif40.rif40_results a' + @crlf +
			' WHERE a.study_id      = ' + CAST(@study_id AS VARCHAR) + ' /* Current study ID */';
	ELSE SET @sql_stmt='SELECT TOP 1 a.*' + @crlf + 
			'  INTO rif_studies.' + LOWER(@c1_rec_map_table) + @crlf +
			'  FROM rif40.rif40_results a' + @crlf + 
			' WHERE a.study_id      = ' + CAST(@study_id AS VARCHAR) + ' /* Current study ID */';

--	
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
	SET @msg='55602: SQL> ' + @sql_stmt + ';';
	PRINT @msg;
	
--
-- Truncate it anyway to make sure
--
	SET @sql_stmt='TRUNCATE TABLE [rif_studies].[' + LOWER(@c1_rec_map_table) + ']';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
	SET @msg='55603: SQL> ' + @sql_stmt + ';';
	PRINT @msg;
	
--
-- Grant to study owner and all grantees in rif40_study_shares if extract_permitted=1 
--
	IF @c1_rec_extract_permitted = 1 BEGIN
		SET @sql_stmt='GRANT SELECT,INSERT,UPDATE ON [rif_studies].[' + 
			LOWER(@c1_rec_map_table) + '] TO ' + USER;
		SET @t_ddl=@t_ddl+1;	
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
		SET @msg='55604: SQL> ' + @sql_stmt + ';';
		PRINT @msg;

		OPEN c3comp;
		FETCH NEXT FROM c3comp INTO @c3_rec_grantee_username;
		WHILE @@FETCH_STATUS = 0
		BEGIN
			SET @sql_stmt='GRANT SELECT,INSERT,UPDATE ON [rif_studies].[' + 
				LOWER(@c1_rec_map_table) + '] TO ' + @c3_rec_grantee_username;
			SET @t_ddl=@t_ddl+1;	
			INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
			SET @msg='55605: SQL> ' + @sql_stmt + ';';
			PRINT @msg;		
--
			FETCH NEXT FROM c3comp INTO @c3_rec_grantee_username;
		END;
		CLOSE c3comp;
		DEALLOCATE c3comp;	
	END;
	
--
-- Comment
--
	DECLARE @comment_text NVARCHAR(MAX)='Study ' + 
			CAST(@study_id AS VARCHAR) + ' map: ' + COALESCE(@c1_rec_description, 'NO DESCRIPTION');
	SET @sql_stmt='sp_addextendedproperty' + @crlf +
		'		@name = N''MS_Description'',' + @crlf + 
		'		@value = N''' + @comment_text + ''',' + @crlf +  
		'		@level0type = N''Schema'', @level0name = ''rif_studies'',' + @crlf +  
		'		@level1type = N''Table'', @level1name = ''' + LOWER(@c1_rec_map_table) + '''';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
	SET @msg='55606: SQL> ' + @sql_stmt + ';';
	PRINT @msg;
	
	OPEN c4comp;
	FETCH NEXT FROM c4comp INTO @c4_rec_column_name, @c4_rec_column_comment;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @sql_stmt='sp_addextendedproperty' + @crlf +
			'		@name = N''MS_Description'',' + @crlf + 
			'		@value = N''' + @c4_rec_column_comment + ''',' + @crlf +  
			'		@level0type = N''Schema'', @level0name = ''rif_studies'',' + @crlf +  
			'		@level1type = N''Table'', @level1name = ''' + LOWER(@c1_rec_map_table) + ''',' + @crlf +
			'		@level2type = N''Column'', @level2name = ''' + LOWER(@c4_rec_column_name) + '''';
		SET @t_ddl=@t_ddl+1;	
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
		SET @msg='55607: SQL> ' + @sql_stmt + ';';
		PRINT @msg;	
--
		FETCH NEXT FROM c4comp INTO @c4_rec_column_name, @c4_rec_column_comment;
	END;
	CLOSE c4comp;
	DEALLOCATE c4comp;	
		
--
-- GID, GID_ROWINDEX support in maps (extracts subject to performance tests)
-- AREA_ID in disease mapping
--
	IF @c1_rec_study_type = 1 /* Disease mapping */ BEGIN
		SET @sql_stmt='sp_addextendedproperty' + @crlf +
			'		@name = N''MS_Description'',' + @crlf + 
			'		@value = N''GID rowindex record locator unique key'',' + @crlf +  
			'		@level0type = N''Schema'', @level0name = ''rif_studies'',' + @crlf +  
			'		@level1type = N''Table'', @level1name = ''' + LOWER(@c1_rec_map_table) + ''',' + @crlf +
			'		@level2type = N''Column'', @level2name = ''gid_rowindex''';
		SET @t_ddl=@t_ddl+1;	
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
		SET @msg='55608: SQL> ' + @sql_stmt + ';';
		PRINT @msg;	
		
		SET @sql_stmt='sp_addextendedproperty' + @crlf +
			'		@name = N''MS_Description'',' + @crlf + 
			'		@value = N''Geographic ID (artificial primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)'',' + @crlf +  
			'		@level0type = N''Schema'', @level0name = ''rif_studies'',' + @crlf +  
			'		@level1type = N''Table'', @level1name = ''' + LOWER(@c1_rec_map_table) + ''',' + @crlf +
			'		@level2type = N''Column'', @level2name = ''gid''';
		SET @t_ddl=@t_ddl+1;	
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
		SET @msg='55609: SQL> ' + @sql_stmt + ';';
		PRINT @msg;	
	END;

	REVERT;	/* Revert to procedure owner context (RIF40) to create map table */

	EXECUTE @rval=rif40.rif40_ddl
			@ddl_stmts	/* SQL table */,
			@debug		/* enable debug: 0/1) */;
	IF @rval = 0 BEGIN
			SET @msg='55610: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
				' map table creation failed, see previous warnings'	/* Study id */;
			PRINT @msg;
			RETURN @rval;
		END; 
	ELSE BEGIN
			SET @msg='55611: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
				' map table creation OK'	/* Study id */;
			PRINT @msg;
		END; 

--
-- Use caller execution context to query RIF views
--
	EXECUTE AS CALLER /* RIF user */;
	
--
-- Now do real insert as user
--
	IF @c1_rec_study_type = 1 /* Disease mapping */ SET @sql_stmt='INSERT INTO rif_studies.' + 
		LOWER(@c1_rec_map_table) + @crlf +
--
-- GID, GID_ROWINDEX support in maps (extracts subject to performance tests)
-- AREA_ID in disease mapping
--
			'SELECT ''X'' AS gid,' + @crlf +
			'       RIGHT(REPLICATE(''0'', 10) + CAST(ROW_NUMBER() OVER(' + @crlf +
			'              ORDER BY a.study_id, a.band_id, a.inv_id, a.genders, a.adjusted, a.direct_standardisation' + @crlf +
			'              ) AS VARCHAR), 10) AS gid_rowindex,'+ @crlf +
			'       ''X'' AS area_id,' + @crlf +
			'       a.*' + @crlf +
			'  FROM rif40.rif40_results a' + @crlf +
			' WHERE a.study_id      = ' + CAST(@study_id AS VARCHAR) + 
				' /* Current study ID */' + @crlf +	
			' ORDER BY 2			/* GID_ROWINDEX */';
	ELSE SET @sql_stmt='INSERT INTO rif_studies.' + 
		LOWER(@c1_rec_map_table) + @crlf +
			'SELECT a.*' + @crlf + 
			'  FROM rif40.rif40_results a' + @crlf + 
			' WHERE a.study_id      = ' + CAST(@study_id AS VARCHAR) + 
				' /* Current study ID */' + @crlf +	
			' ORDER BY 1, 2, 3, 4, 5, 6'; 	
	PRINT @sql_stmt;
	
	DECLARE @rowcount	INTEGER;
	BEGIN TRY	
		EXECUTE sp_executesql @sql_stmt;
		SET @rowcount=@@ROWCOUNT;
		IF @rowcount > 0 
			PRINT 'SQL[' + USER + '] study_id: ' + CAST(@study_id AS VARCHAR) + 
				' map table insert OK; rows: ' + CAST(@rowcount AS VARCHAR)
		ELSE BEGIN	
			SET @err_msg = formatmessage(55810, @study_id); 
				-- Study ID %i no rows INSERTED into map table.
			THROW 55810, @err_msg, 1;
		END;
	END TRY
	BEGIN CATCH		
--	 		[55699] SQL statement had error: %s%sSQL[%s]> %s;	
		IF LEN(@sql_stmt) > 1900 BEGIN	
			SET @err_msg = formatmessage(55699, error_message(), @crlf, USER, '[SQL statement too long for error; see SQL above]');
			PRINT '[55699] SQL> ' + @sql_stmt;
		END;
		ELSE SET @err_msg = formatmessage(55699, error_message(), @crlf, USER, @sql_stmt); 
		THROW 55699, @err_msg, 1;
	END CATCH;	

	DELETE FROM @ddl_stmts;
	SET @t_ddl=1;
	
	REVERT;	/* Revert to procedure owner context (RIF40) to add primary key */

--
-- Add primary key
--
	SET @sql_stmt='ALTER TABLE rif_studies.' + LOWER(@c1_rec_map_table) + 
		' ADD CONSTRAINT ' + LOWER(@c1_rec_map_table) + '_pk' +
		' PRIMARY KEY (study_id, band_id, inv_id, genders, adjusted, direct_standardisation)';
	INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
	SET @msg='55612: SQL> ' + @sql_stmt + ';';
	PRINT @msg;	

--
-- Make gid_rowindex bigger
--
	IF @c1_rec_study_type = 1 /* Disease mapping */ BEGIN
		SET @sql_stmt='ALTER TABLE rif_studies.' + LOWER(@c1_rec_map_table) + 
			' ALTER COLUMN gid_rowindex VARCHAR(70)';
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
		SET @msg='55613: SQL> ' + @sql_stmt + ';';
		PRINT @msg;
	END;
	
--
-- Execute DDL code as rif40
--
	EXECUTE @rval=rif40.rif40_ddl
			@ddl_stmts	/* SQL table */,
			@debug		/* enable debug: 0/1) */;
	IF @rval = 0 BEGIN
			SET @msg='55614: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
				' map primary key creation failed, see previous warnings'	/* Study id */;
			PRINT @msg;
			RETURN @rval;
		END; 
	ELSE BEGIN
			SET @msg='55615: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
				' map primary key creation OK'	/* Study id */;
			PRINT @msg;
		END; 

--
-- Use caller execution context to query RIF views
--
	EXECUTE AS CALLER /* RIF user */;
	
	DELETE FROM @ddl_stmts;
	SET @t_ddl=1;	
--
-- Update area_id, gid
--
	IF @c1_rec_study_type = 1 /* Disease mapping */ BEGIN	
		
		SET @sql_stmt='UPDATE rif_studies.' + LOWER(@c1_rec_map_table) + @crlf +
			'   SET area_id = (' + @crlf +
			'           SELECT b.area_id' + @crlf +
			'             FROM rif40.rif40_study_areas b' + @crlf +
			'            WHERE rif_studies.' + LOWER(@c1_rec_map_table) + '.study_id = b.study_id' + @crlf +
			'              AND rif_studies.' + LOWER(@c1_rec_map_table) + '.band_id  = b.band_id)';
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
		SET @msg='55616: SQL> ' + @sql_stmt + ';';	
		PRINT @msg;
		
--
-- Get geographies settings
--
		SET @sql_stmt='UPDATE rif_studies.' + LOWER(@c1_rec_map_table) + @crlf +
			'   SET gid = ('+ @crlf +
			'           SELECT d.gid' + @crlf +
			'             FROM rif_data.lookup_' + LOWER(@c1_rec_study_geolevel_name) + ' d' + @crlf +
			'            WHERE rif_studies.' + LOWER(@c1_rec_map_table) + '.area_id       = d.' + LOWER(@c1_rec_study_geolevel_name) + ')';
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
		SET @msg='55617: SQL> ' + @sql_stmt + ';';	
		PRINT @msg;	

--
-- Merge gid with gid_rowindex
--		
-- Error message: Function: [rif40].[rif40_ddl], SQL statement had error: String or binary data would be truncated.
		SET @sql_stmt='UPDATE rif_studies.' + LOWER(@c1_rec_map_table) + @crlf +
			'   SET gid_rowindex = gid + ''_'' + gid_rowindex' + @crlf +
			' WHERE gid IS NOT NULL';
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
		SET @msg='55618: SQL> ' + @sql_stmt + ';';	
		PRINT @msg;	
		
--
-- Execute as USER
--
		EXECUTE @rval=rif40.rif40_ddl
				@ddl_stmts	/* SQL table */,
				@debug		/* enable debug: 0/1) */;
		IF @rval = 0 BEGIN
				SET @msg='55619: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
					' GID update failed, see previous warnings'	/* Study id */;
				PRINT @msg;
				RETURN @rval;
			END; 
		ELSE BEGIN
				SET @msg='55620: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
					' GID update OK'	/* Study id */;
				PRINT @msg;
			END; 
	
	
		DELETE FROM @ddl_stmts;
		SET @t_ddl=1;

--
-- Add gid_rowindex unique key for disease maps
--
		SET @sql_stmt='CREATE UNIQUE INDEX ' + LOWER(@c1_rec_map_table) + '_uk' +
			' ON rif_studies.' + LOWER(@c1_rec_map_table) + '(gid_rowindex)';
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
		SET @msg='55621: SQL> ' + @sql_stmt + ';';	
		PRINT @msg;		
	END; /* Disease mapping */ 

	REVERT;	/* Revert to procedure owner context (RIF40) to add indexes */	
	
--
-- Analyze
--
	SET @sql_stmt='UPDATE STATISTICS rif_studies.' + LOWER(@c1_rec_map_table) + 
		' WITH SAMPLE 10 PERCENT';
	INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
	SET @msg='55622: SQL> ' + @sql_stmt + ';';	
	PRINT @msg;	
	
--
-- Execute DDL code as rif40
--
	EXECUTE @rval=rif40.rif40_ddl
			@ddl_stmts	/* SQL table */,
			@debug		/* enable debug: 0/1) */;
	IF @rval = 0 BEGIN
			SET @msg='55623: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
				' Statistics update failed, see previous warnings'	/* Study id */;
			PRINT @msg;
			RETURN @rval;
		END; 
	ELSE BEGIN
			SET @msg='55624: RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
				' Statistics update OK'	/* Study id */;
			PRINT @msg;
		END; 

--
	SET @etp=GETDATE();
	SET @etime=CAST(@etp - @stp AS TIME);
	SET @msg='55625: Study ID ' + CAST(@c1_rec_study_id AS VARCHAR) +
		' map table ' + @c1_rec_map_table + ' created; time taken ' + 
		CAST(CONVERT(VARCHAR(24), @etime, 14) AS VARCHAR);
	PRINT @msg;
--
	RETURN @rval;
END;
GO

--
-- Eof