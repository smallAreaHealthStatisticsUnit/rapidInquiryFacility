
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_study_shares')
BEGIN
	DROP TRIGGER [rif40].[tr_study_shares]
END
GO


CREATE trigger [rif40].[tr_study_shares]
on [rif40].[rif40_study_shares]
AFTER insert , update , delete
as

begin
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
Declare  @XTYPE varchar(5);

	IF EXISTS (SELECT * FROM DELETED)
	BEGIN
		SET @XTYPE = 'D'
	END;
	
	IF EXISTS (SELECT * FROM INSERTED)
	BEGIN
		IF (@XTYPE = 'D')
			SET @XTYPE = 'U'
		ELSE 
			SET @XTYPE = 'I'
	END
		
-----------------------------------------------
--When Transaction is an Update  then rollback 
-------------------------------------------------
	IF (@XTYPE = 'U')
	BEGIN TRY
		rollback;
		DECLARE @err_msg8 VARCHAR(max) = formatmessage(51006);
		THROW 51006, @err_msg8, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51006, @err_msg8, 1;
	END CATCH;

--
-- For insert, check that new values are valid:
-- Check all fields are not null: study_id, grantor,grantee_username
-- Check that the study_id exists
-- Grantee username is valid user
-- Grantor username is valid user
-- Grantee username has RIF_USER or RIF_MANAGER
-- If the Grantor is NOT owner of the study they are a RIF_MANAGER	
-- If the grantor != grantee_username then the grantor is a RIF_MANAGER
--	
IF (@XTYPE = 'I')
BEGIN
	
	-- Check all not null: study_id, grantor,grantee_username
	DECLARE @invalid_values varchar(max) = (
		SELECT study_id, grantor,grantee_username 
		FROM inserted
		WHERE grantor IS NULL or grantor=''
		OR grantee_username IS NULL or grantee_username=''
		OR study_id IS NULL or study_id = ''
		FOR XML PATH('')
		);
	IF @invalid_values IS NOT NULL 
	BEGIN TRY
		rollback;
		DECLARE @err_msg varchar(max) = formatmessage(51007,@invalid_values);
		THROW 51007, @err_msg, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51007, @err_msg, 1;
	END CATCH;
	
	-- STUDY NOT FOUND 
	DECLARE @not_found_study nvarchar(MAX) =
		(
		SELECT 
			convert(varchar,i.STUDY_ID) + ', '
			FROM inserted i
			WHERE NOT EXISTS (select 1
			FROM [rif40].[t_rif40_studies] b 
			WHERE i.study_id=b.study_id)
			FOR XML PATH('')
		);
	
		IF @not_found_study IS NOT NULL
		BEGIN TRY
			rollback;
			DECLARE @err_msg2 varchar(max) = formatmessage(51008,@not_found_study);
			THROW 51008, @err_msg2, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
			THROW 51008, @err_msg2, 1;
		END CATCH;
		
	--Grantee username is valid user
	DECLARE @invalid_grantee varchar(max) = (
		SELECT a.study_id, a.grantee_username
		FROM inserted a
		WHERE NOT EXISTS (select 1
		FROM [sys].[database_principals] b WHERE a.grantee_username collate database_default=b.name collate database_default)
		FOR XML PATH('')
	);
	IF @invalid_grantee IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 varchar(max) = formatmessage(51010, @invalid_grantee);
		THROW 51010, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51010, @err_msg3, 1;
	END CATCH 
	
	--GRANTOR username is valid user
	DECLARE @invalid_grantor varchar(max) = (
		SELECT a.study_id, a.grantor
		FROM inserted a
		WHERE NOT EXISTS (select 1
		FROM [sys].[database_principals] b WHERE a.grantor collate database_default=b.name collate database_default)
		FOR XML PATH('')
	);
	IF @invalid_grantor IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg9 varchar(max) = formatmessage(51010, @invalid_grantor);
		THROW 51010, @err_msg9, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51010, @err_msg9, 1;
	END CATCH 
	
	-- grantee_username has RIF_USER or RIF_MANAGER
	DECLARE @grantee_roles varchar(max) = (
		SELECT study_id, grantee_username
		  FROM inserted
		 WHERE [rif40].[rif40_has_role](SUSER_SNAME(),'rif_user')    != 1 /* Not a rif_user or a rif_manager */
	       AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') != 1
		FOR XML PATH('')
	);
	IF @grantee_roles IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg4 varchar(max) = formatmessage(51011,@grantee_roles);
		THROW 51011, @err_msg4, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51011, @err_msg4, 1;
	END CATCH
	
	-- If the grantor is NOT owner of the study they are a RIF_MANAGER
	DECLARE @GRANTOR nvarchar(MAX) =
		(
		SELECT 
			grantor, grantee_username, a.study_id
			from inserted a
			WHERE NOT EXISTS (select 1
			FROM [rif40].[t_rif40_studies] b 
			WHERE b.username=a.grantor and b.study_id=a.study_id)
			FOR XML PATH('')
		);
	IF @GRANTOR IS NOT NULL
	BEGIN TRY
		IF IS_MEMBER(N'rif_manager') = 1 
		BEGIN
			DECLARE @logmsg VARCHAR(MAX) = 'Study not owned by grantor but current user is a RIF_MANAGER: '+@GRANTOR;
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[tr_study_shares]', @logmsg;
		END
		ELSE
		BEGIN
			rollback;
			DECLARE @err_msg7 varchar(max) = formatmessage(51009, @GRANTOR);
			THROW 51009, @err_msg7, 1;
		END;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51009, @err_msg7, 1;
	END CATCH 
	
	-- If the grantor != grantee_username then the grantor is a RIF_MANAGER
	DECLARE @MISMATCHED_GRANTOR nvarchar(MAX) =
		(
		SELECT 
			grantor, grantee_username, a.study_id
			from inserted a
			WHERE grantor != grantee_username
			FOR XML PATH('')
		);
	IF @GRANTOR IS NOT NULL
	BEGIN TRY
		IF IS_MEMBER(N'rif_manager') = 1 
		BEGIN
			DECLARE @logmsg8 VARCHAR(MAX) = 'Study owned by grantor with a different grantee but current user is a RIF_MANAGER: '+@GRANTOR;
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[tr_study_shares]', @logmsg8;
		END
		ELSE
		BEGIN
			rollback;
			DECLARE @err_msg10 varchar(max) = formatmessage(51009, @MISMATCHED_GRANTOR);
			THROW 51009, @err_msg10, 1;
		END;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51009, @err_msg10, 1;
	END CATCH 

END;

--Delete: check that user owns the study being deleted or is a RIF_MANAGER
IF (@XTYPE = 'D')
BEGIN
	DECLARE @not_owner varchar(max) = 
	(
		SELECT study_id, grantor
		FROM deleted
		where grantor != SUSER_SNAME()
		FOR XML PATH('')
	);
	IF @not_owner IS NOT NULL
	BEGIN TRY
			IF IS_MEMBER(N'rif_manager') = 1 
			BEGIN
				DECLARE @logmsg2 VARCHAR(MAX) = 'Study not owned by grantor but current user is RIF_MANAGER: '+@not_owner;
				EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[tr_study_shares]', @logmsg2;
			END
			ELSE
			BEGIN
				rollback;
				DECLARE @err_msg5 varchar(max) = formatmessage(51012, @not_owner);
				THROW 51012, @err_msg5, 1;
			END;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51012, @err_msg5, 1;
	END CATCH	
END
END;
GO
