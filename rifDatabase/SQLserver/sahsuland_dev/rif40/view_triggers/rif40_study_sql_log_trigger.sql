
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_study_sql_log')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_study_sql_log];
END
GO


------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_study_sql_log]
on [rif40].[rif40_study_sql_log]
instead of insert , update , delete
as
BEGIN 
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
DECLARE  @XTYPE varchar(1);
IF EXISTS (SELECT * FROM DELETED)
	SET @XTYPE = 'D';
	
IF EXISTS (SELECT * FROM INSERTED)
BEGIN
	IF (@XTYPE = 'D')
		SET @XTYPE = 'U'
	ELSE 
		SET @XTYPE = 'I'
END;

IF @XTYPE='I'
BEGIN
--
-- Check (USER = username OR NULL) and USER is a RIF user; if OK INSERT
--
/*
	DECLARE @insert_invalid_user VARCHAR(MAX) = 
	(
		select username, study_id, statement_number
		from inserted
		where (username != SUSER_SNAME() and username is not null)
		OR ([rif40].[rif40_has_role](username,'rif_user') = 0
		AND [rif40].[rif40_has_role](username,'rif_manager') = 0)
		FOR XML PATH('')
	);

	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51140, @insert_invalid_user);
		THROW 51140, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_study_sql_log]';
		THROW 51140, @err_msg1, 1;
	END CATCH;	*/

	DECLARE @statement_number INTEGER = (
	SELECT ISNULL(MAX(a.statement_number), 0)+1 
	  FROM [rif40].[t_rif40_study_sql_log] a, inserted b
	 WHERE a.study_id = isnull(b.study_id,
								[rif40].[rif40_sequence_current_value]('rif40.rif40_study_id_seq'))
	); 
		
	INSERT INTO [rif40].[t_rif40_study_sql_log] (
				username,
				study_id,
				statement_type,
				statement_number,
				log_message,
				audsid,
				log_sqlcode,
				[rowcount],
				start_time,
				elapsed_time)
	SELECT
				isnull(username,SUSER_SNAME()),
				isnull(study_id,[rif40].[rif40_sequence_current_value]('rif40_study_id_seq')),
				statement_type /* no default value */,
				isnull(statement_number,@statement_number),
				log_message /* no default value */,
				isnull(audsid, @@spid),
				log_sqlcode /* no default value */,
				[rowcount] /* no default value */,
				isnull(start_time,sysdatetime()),
				elapsed_time /* no default value */
	FROM inserted;
END;

IF @XTYPE='U'
BEGIN
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
	DECLARE @update_invalid_user VARCHAR(MAX) =
	(
		select a.username as 'old_username', b.username as 'new_username', SUSER_SNAME() as 'current_user'
		from deleted a
		left outer join inserted b on a.study_id=b.study_id and a.statement_number=b.statement_number
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51141, @update_invalid_user);
		THROW 51141, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_study_sql_log]';
		THROW 51141, @err_msg2, 1;
	END CATCH;	
				
	DELETE FROM [rif40].[t_rif40_study_sql_log]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_study_sql_log].study_id
		AND b.statement_number=[rif40].[t_rif40_study_sql_log].statement_number);
		
	INSERT INTO [rif40].[t_rif40_study_sql_log] (
				username,
				study_id,
				statement_type,
				statement_number,
				log_message,
				audsid,
				log_sqlcode,
				[rowcount],
				start_time,
				elapsed_time)
	SELECT	username,
				study_id,
				statement_type,
				statement_number,
				log_message,
				audsid,
				log_sqlcode,
				[rowcount],
				start_time,
				elapsed_time
	FROM inserted;
END;

IF @XTYPE='D'
BEGIN
--
-- Check USER = OLD.username; if OK DELETE
--
	DECLARE @delete_invalid_user VARCHAR(MAX) =
	(
		select username
		from deleted
		where username != SUSER_SNAME()
		FOR XML PATH('')
	);
	IF @delete_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51142, @delete_invalid_user);
		THROW 51142, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_study_sql_log]';
		THROW 51142, @err_msg3, 1;
	END CATCH;		
	
	DELETE FROM [rif40].[t_rif40_study_sql_log]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_study_sql_log].study_id
		AND b.statement_number=[rif40].[t_rif40_study_sql_log].statement_number);
END;

END;
GO
