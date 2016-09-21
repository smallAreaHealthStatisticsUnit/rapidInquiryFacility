USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_fdw_tables')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_fdw_tables];
END
GO


------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_fdw_tables]
on [rif40].[rif40_fdw_tables]
instead of insert , update , delete
as
BEGIN 
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
DECLARE  @XTYPE varchar(5);
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
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
	DECLARE @insert_invalid_user VARCHAR(MAX) = 
	(
		select username, table_name, create_status
		from inserted
		where (username != SUSER_SNAME() and username is not null)
		OR ([rif40].[rif40_has_role](username,'rif_user') = 0
		AND [rif40].[rif40_has_role](username,'rif_manager') = 0)
		FOR XML PATH('')
	);
	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51113, @insert_invalid_user);
		THROW 51113, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_fdw_tables]';
		THROW 51113, @err_msg1, 1;
	END CATCH;	
	
	INSERT INTO [rif40].[t_rif40_fdw_tables] (
				username,
				table_name,
				create_status,
				error_message,
				date_created,
				rowtest_passed)
	SELECT isnull(username, SUSER_SNAME()),
		table_name,
		create_status,
		error_message,
		isnull(date_created,getdate()),
		isnull(rowtest_passed,0)
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
		left outer join inserted b on a.table_name=b.table_name
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
		BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51114, @update_invalid_user);
		THROW 51114, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_fdw_tables]';
		THROW 51114, @err_msg2, 1;
	END CATCH;	
	
	DELETE FROM [rif40].[t_rif40_fdw_tables]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.table_name=[rif40].[t_rif40_fdw_tables].table_name);
	
	INSERT INTO [rif40].[t_rif40_fdw_tables](
				username,
				table_name,
				create_status,
				error_message,
				date_created,
				rowtest_passed)
	SELECT username,
		table_name,
		create_status,
		error_message,
		date_created,
		rowtest_passed
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
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51115, @update_invalid_user);
		THROW 51115, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_fdw_tables]';
		THROW 51115, @err_msg3, 1;
	END CATCH;	
	
	DELETE FROM [rif40].[t_rif40_fdw_tables]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.table_name=[rif40].[t_rif40_fdw_tables].table_name);
END;

END;
GO