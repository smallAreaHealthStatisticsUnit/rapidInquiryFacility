
-- Remove incorrectly named trigger: tr_rif40_use_projects
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_use_projects')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_use_projects];
END
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_user_projects')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_user_projects];
END
GO

------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_user_projects]
on [rif40].[rif40_user_projects]
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
	DECLARE @insert_invalid_user VARCHAR(MAX) = 
	(
		SELECT SUSER_SNAME() AS username, project
		  FROM inserted
		 WHERE NOT (username = SUSER_SNAME() OR username is null)		  /* Not the study owner */
		   AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_user')    != 1 /* Not a rif_user or a rif_manager */
	       AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') != 1
		FOR XML PATH('')
	);

	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51143, @insert_invalid_user);
		THROW 51143, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_user_projects]';
		THROW 51143, @err_msg1, 1;
	END CATCH;	
	
	INSERT INTO [rif40].[t_rif40_user_projects] (
				project,
				username,
				grant_date,
				revoke_date)
	SELECT
				project /* no default value */,
				isnull(username,SUSER_SNAME()),
				isnull(grant_date,sysdatetime()),
				revoke_date /* no default value */
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
		left outer join inserted b on a.project=b.project and a.username=b.username
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51144, @update_invalid_user);
		THROW 51144, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_user_projects]';
		THROW 51144, @err_msg2, 1;
	END CATCH;	
	
	DELETE FROM [rif40].[t_rif40_user_projects]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.project=[rif40].[t_rif40_user_projects].project
		AND b.username=[rif40].[t_rif40_user_projects].username);
		
	INSERT INTO [rif40].[t_rif40_user_projects] (
				project,
				username,
				grant_date,
				revoke_date)
	SELECT
				project,
				username,
				grant_date,
				revoke_date
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
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51145, @delete_invalid_user);
		THROW 51145, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_user_projects]';
		THROW 51145, @err_msg3, 1;
	END CATCH;		
	
	DELETE FROM [rif40].[t_rif40_user_projects]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.project=[rif40].[t_rif40_user_projects].project
		AND b.username=[rif40].[t_rif40_user_projects].username);
END;

END;
GO
