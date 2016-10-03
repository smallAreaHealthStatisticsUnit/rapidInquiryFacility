USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_study_areas')
BEGIN
	DROP TRIGGER [rif40].[tr_study_areas]
END
GO


Create trigger [tr_STUDY_AREAS]
on [rif40].[t_rif40_study_areas]
for insert, update , delete
as
BEGIN
Declare  @xtype varchar(5)
	IF EXISTS (SELECT * FROM DELETED)
	BEGIN
		SET @XTYPE = 'D';
	END
	IF EXISTS (SELECT * FROM INSERTED)
		BEGIN
			IF (@XTYPE = 'D')
			BEGIN
				SET @XTYPE = 'U';
			END
		END
	ELSE
		BEGIN
			SET @XTYPE = 'I'
		END;

DECLARE @has_studies_check VARCHAR(MAX) = 
(
	SELECT count(study_id) as total
	FROM [rif40].[t_rif40_results]
);
	

IF @XTYPE = 'D'
BEGIN
	DECLARE @delete_user_check VARCHAR(MAX) = 
	(
		SELECT study_id, area_id, username
		FROM deleted
		WHERE username != SUSER_SNAME()
		FOR XML PATH('')
	);
	IF @delete_user_check IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51087, @delete_user_check);
		THROW 51087, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_study_areas]';
		THROW 51087, @err_msg1, 1;
	END CATCH;	
	RETURN;
END;

IF @XTYPE = 'U' OR @XTYPE = 'I'
BEGIN
	DECLARE @insert_user_check VARCHAR(MAX) = 
	(
		SELECT study_id, area_id, username
		FROM inserted
		WHERE username != SUSER_SNAME()
		FOR XML PATH('')
	);
	IF @insert_user_check IS NOT NULL
	BEGIN
		IF @has_studies_check = 0 AND SUSER_SNAME()='RIF40'
		BEGIN
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_study_areas]', 't_rif40_study_areas insert/update allowed during build';
			RETURN;
		END
		ELSE 
		BEGIN TRY
			rollback;
			DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51088, @insert_user_check);
			THROW 51088, @err_msg2, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_study_areas]';
			THROW 51088, @err_msg2, 1;
		END CATCH;	
	END
	ELSE
	BEGIN
		IF @XTYPE = 'U'
		BEGIN TRY
			rollback;
			DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51089);
			THROW 51089, @err_msg3, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_study_areas]';
			THROW 51089, @err_msg3, 1;
		END CATCH;	
	END;
	
	DECLARE @current_study_id int = [rif40].[rif40_sequence_current_value]( 'study_id');
	
	DECLARE @study_area_not_found VARCHAR(MAX) = 
	(
		SELECT a.study_id, a.geography
		FROM [rif40].[t_rif40_studies] a
		WHERE a.study_id = @current_study_id
		AND NOT EXISTS (
			SELECT 1
			FROM rif40_geographies b
			WHERE a.geography = b.geography)
		FOR XML PATH('')
	);
	IF @study_area_not_found IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg4 VARCHAR(MAX) = formatmessage(51090);
		THROW 51090, @err_msg4, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_study_areas]';
		THROW 51090, @err_msg4, 1;
	END CATCH;	
	
--
-- Check - area_id (messy dynamic SQL)
--
	DECLARE @study_geolevel_name VARCHAR(MAX), @study_hierarchytable VARCHAR(MAX)
	SELECT @study_geolevel_name=a.study_geolevel_name, @study_hierarchytable = b.hierarchytable
	FROM t_rif40_studies a, rif40_geographies b
	WHERE a.geography = b.geography
	AND a.study_id  = @current_study_id;

	DECLARE @area_id_query VARCHAR(MAX);
	SET @area_id_query = 'select count(*) as total from (SELECT area_id FROM t_rif40_study_areas a WHERE study_id = '+@current_study_id+' AND NOT EXISTS (
	SELECT 1
	FROM '+@study_hierarchytable+' b WHERE b.'+@study_geolevel_name+'=a.area_id))';
	DECLARE  @ParmDefinition nvarchar(500) = N'@totalOUT int OUTPUT', @total int;
	EXEC sp_executesql @area_id_query, @ParmDefinition, @totalOUT=@total OUTPUT;
	
	IF @total > 0
	BEGIN TRY
		rollback;
		DECLARE @err_msg5 VARCHAR(MAX) = formatmessage(51091, @current_study_id);
		THROW 51091, @err_msg5, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_study_areas]';
		THROW 51091, @err_msg5, 1;
	END CATCH;
	
END;	

END;
GO
