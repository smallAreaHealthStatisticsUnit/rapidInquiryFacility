
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_inv_conditions')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_inv_conditions];
END
GO


------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_inv_conditions]
on [rif40].[rif40_inv_conditions]
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
		SELECT SUSER_SNAME() AS username
		  FROM inserted
		 WHERE NOT (username = SUSER_SNAME() OR username is null)		  /* Not the study owner */
		   AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_user')    != 1 /* Not a rif_user or a rif_manager */
	       AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') != 1
	);

	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51116, @insert_invalid_user);
		THROW 51116, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_inv_conditions]';
		THROW 51116, @err_msg1, 1;
	END CATCH;	
	
	INSERT INTO [rif40].[t_rif40_inv_conditions] (
				username,
				study_id,
				inv_id,
				line_number,
				outcome_group_name, 
				min_condition, 
				max_condition, 
				predefined_group_name)
	SELECT
				isnull(username,SUSER_SNAME()),
				isnull(study_id,[rif40].[rif40_sequence_current_value]('rif40.rif40_study_id_seq')),
				isnull(inv_id,[rif40].[rif40_sequence_current_value]('rif40.rif40_inv_id_seq')),
				isnull(line_number,1),
				outcome_group_name, 
				min_condition, 
				max_condition, 
				predefined_group_name
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
		left outer join inserted b on a.study_id=b.study_id AND a.inv_id=b.inv_id AND a.line_number=b.line_number
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51117, @update_invalid_user);
		THROW 51117, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_inv_conditions]';
		THROW 51117, @err_msg2, 1;
	END CATCH;	

	DELETE FROM [rif40].[t_rif40_inv_conditions]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_inv_conditions].study_id
		AND b.inv_id=[rif40].[t_rif40_inv_conditions].inv_id
		AND b.line_number=[rif40].[t_rif40_inv_conditions].line_number);
	
	INSERT INTO [rif40].[t_rif40_inv_conditions] (
				username,
				study_id,
				inv_id,
				line_number,
				outcome_group_name, 
				min_condition, 
				max_condition, 
				predefined_group_name)
	SELECT
				username,
				study_id,
				inv_id,
				line_number,
				outcome_group_name, 
				min_condition, 
				max_condition, 
				predefined_group_name
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
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51118, @delete_invalid_user);
		THROW 51118, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_inv_conditions]';
		THROW 51118, @err_msg3, 1;
	END CATCH;	
	
	DELETE FROM [rif40].[t_rif40_inv_conditions]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_inv_conditions].study_id
		AND b.inv_id=[rif40].[t_rif40_inv_conditions].inv_id
		AND b.line_number=[rif40].[t_rif40_inv_conditions].line_number);
END;

END;
GO
