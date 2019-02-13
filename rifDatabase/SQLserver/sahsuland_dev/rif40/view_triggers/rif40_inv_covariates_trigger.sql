
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_inv_covariates')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_inv_covariates];
END
GO


------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_inv_covariates]
on [rif40].[rif40_inv_covariates]
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
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
	DECLARE @insert_invalid_user VARCHAR(MAX) = 
	(
		select SUSER_SNAME() AS username
		from inserted
		where NOT (username = SUSER_SNAME() OR username is null)
		OR NOT ([rif40].[rif40_has_role](SUSER_SNAME(),'rif_user') = 1
		AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') = 1)
	);

	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51119, @insert_invalid_user);
		THROW 51119, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_inv_covariates]';
		THROW 51119, @err_msg1, 1;
	END CATCH;	

	INSERT INTO [rif40].[t_rif40_inv_covariates] (
				username,
				study_id,
				inv_id,
				covariate_name,
				covariate_type,
				[min],
				[max],
				geography,
				study_geolevel_name)
	SELECT
				isnull(username,SUSER_SNAME()),
				isnull(study_id,[rif40].[rif40_sequence_current_value]('rif40.rif40_study_id_seq')),
				isnull(inv_id,[rif40].[rif40_sequence_current_value]('rif40.rif40_inv_id_seq')),
				covariate_name /* no default value */,
                isnull(covariate_type, 'N'),
				[min] /* no default value */,
				[max] /* no default value */,
				geography /* no default value */,
				study_geolevel_name /* no default value */
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
		left outer join inserted b on a.study_id=b.study_id AND a.inv_id=b.inv_id AND a.covariate_name=b.covariate_name
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51120, @update_invalid_user);
		THROW 51120, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_inv_covariates]';
		THROW 51120, @err_msg2, 1;
	END CATCH;	
	
	DELETE FROM [rif40].[t_rif40_inv_covariates]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_inv_covariates].study_id
		AND b.inv_id=[rif40].[t_rif40_inv_covariates].inv_id
		AND b.covariate_name=[rif40].[t_rif40_inv_covariates].covariate_name);

	INSERT INTO [rif40].[t_rif40_inv_covariates] (
				username,
				study_id,
				inv_id,
				covariate_name,
				covariate_type,
				[min],
				[max],
				geography,
				study_geolevel_name)
	SELECT
				username,
				study_id,
				inv_id,
				covariate_name,
				covariate_type,
				[min],
				[max],
				geography,
				study_geolevel_name
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
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51121, @delete_invalid_user);
		THROW 51121, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_inv_covariates]';
		THROW 51121, @err_msg3, 1;
	END CATCH;	
	
	DELETE FROM [rif40].[t_rif40_inv_covariates]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_inv_covariates].study_id
		AND b.inv_id=[rif40].[t_rif40_inv_covariates].inv_id
		AND b.covariate_name=[rif40].[t_rif40_inv_covariates].covariate_name);
		
END;
	
END;
GO
