USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_study_sql_log')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_study_sql_log]
END
GO

Create trigger [tr_rif40_study_sql_log]
on [rif40].[t_rif40_study_sql_log]
for insert, update , delete
as
BEGIN
DECLARE  @xtype varchar(5)
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
		SELECT study_id, statement_number, username
		FROM deleted
		WHERE username != SUSER_SNAME()
		FOR XML PATH('')
	);
	IF @delete_user_check IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51095, @delete_user_check);
		THROW 51095, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_study_sql_log]';
		THROW 51095, @err_msg1, 1;
	END CATCH;	
	RETURN;
END;

IF @XTYPE = 'U' OR @XTYPE = 'I'
BEGIN
	DECLARE @insert_user_check VARCHAR(MAX) = 
	(
		SELECT study_id, statement_number, username
		FROM inserted
		WHERE username != SUSER_SNAME()
		FOR XML PATH('')
	);
	IF @insert_user_check IS NOT NULL
	BEGIN
		IF @has_studies_check = 0 AND SUSER_SNAME()='RIF40'
		BEGIN
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_study_sql_log]', 't_rif40_study_sql_log insert/update allowed during build';
			RETURN;
		END
		ELSE 
		BEGIN TRY
			rollback;
			DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51096, @insert_user_check);
			THROW 51096, @err_msg2, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_study_sql_log]';
			THROW 51096, @err_msg2, 1;
		END CATCH;	
	END
	ELSE
	BEGIN
		IF @XTYPE = 'U'
		BEGIN TRY
			rollback;
			DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51097);
			THROW 51097, @err_msg3, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_study_sql_log]';
			THROW 51097, @err_msg3, 1;
		END CATCH;	
	END;
END;	

END;
