USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_parameters')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_parameters];
END
GO


------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_parameters]
on [rif40].[rif40_parameters]
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
--but there is no username in the view definition!
/*
	DECLARE @insert_invalid_user VARCHAR(MAX) = 
	(
		select username, parameter_name
		from inserted
		where (username != SUSER_SNAME() and username is not null)
		OR ([rif40].[rif40_has_role](username,'rif_user') = 0
		AND [rif40].[rif40_has_role](username,'rif_manager') = 0)
		FOR XML PATH('')
	);
*/
	IF ([rif40].[rif40_has_role](SUSER_SNAME(),'rif_user') = 0
		AND [rif40].[rif40_has_role](SUSER_NAME(),'rif_manager') = 0)
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51125, SUSER_SNAME());
		THROW 51125, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_parameters]';
		THROW 51125, @err_msg1, 1;
	END CATCH;	

	INSERT INTO [rif40].[t_rif40_parameters] (
                                param_name,
                                param_value,
                                param_description)
	SELECT
								param_name,
								param_value,
								description
	FROM inserted;
	
END;

IF @XTYPE='U'
BEGIN
	DECLARE @update_not_allowed VARCHAR(MAX) =
	(
		select param_name, param_value
		from inserted
		WHERE param_name IN ('SuppressionValue', 'RifParametersTable')
		FOR XML PATH('')
	);
	IF @update_not_allowed IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51126, @update_not_allowed);
		THROW 51126, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_parameters]';
		THROW 51126, @err_msg2, 1;
	END CATCH;	
	
	DELETE FROM [rif40].[t_rif40_parameters]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.param_name=[rif40].[t_rif40_parameters].param_name);
	
	INSERT INTO [rif40].[t_rif40_parameters] (
                                param_name,
                                param_value,
                                param_description)
	SELECT
								param_name,
								param_value,
								description
	FROM inserted;	
END;

IF @XTYPE='D'
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51127);
		THROW 51127, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_parameters]';
		THROW 51127, @err_msg3, 1;
	END CATCH;	
	
END;
GO