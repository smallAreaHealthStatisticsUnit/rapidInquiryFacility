/*
Update/insert: 
Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.

-- T_RIF40_STUDY_AREAS: Check - USERNAME is Kerberos USER on INSERT
--			Check - audsid is SYS_CONTEXT('USERENV', 'SESSIONID') on INSERT
-- 			Check - UPDATE not allowed
--			Check - DELETE only allowed on own records

*/

USE [sahsuland_dev]
GO

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
Declare  @xtype varchar(5);

	IF EXISTS (SELECT * FROM DELETED)
	BEGIN
		SET @XTYPE = 'D'
	END;
	IF EXISTS (SELECT * FROM INSERTED)
		BEGIN
			IF (@XTYPE = 'D')
		BEGIN
			SET @XTYPE = 'U'
		END
	ELSE
		BEGIN
			SET @XTYPE = 'I'
		END
------------------------------------------------
--When Transaction is an Update  then rollback 
-------------------------------------------------
	IF (@XTYPE = 'U')
	BEGIN TRY
		rollback;
		THROW @error_number=51006, @state=1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW @error_number=51006, @state=1;
	END CATCH;
	
--for insert, check that new values are valid
IF (@XTYPE = 'I')
BEGIN
	DECLARE @invalid_values varchar(max) = (
		SELECT 'study_id='+study_id+',grantor='+grantor+',grantee='+grantee_username
		FROM inserted
		WHERE grantor IS NULL or grantor=''
		OR grantee_username IS NULL or grantee_user=''
		OR study_id IS NULL or study_id = ''
		FOR XML PATH('')
		);
	IF @invalid_values IS NOT NULL 
	BEGIN TRY
		rollback;
		DECLARE @err_msg = formatmessage(51007,@invalid_values);
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
			STUDY_ID + ', '
			FROM inserted i
			LEFT OUTER JOIN [rif40].[t_rif40_studies] b ON i.study_id=b.study_id
			WHERE b.study_id IS NULL
			FOR XML PATH('')
		);
	
		IF @not_found_study IS NOT NULL
		BEGIN TRY
			rollback;
			DECLARE @err_msg2 = formatmessage(51008,@not_found_study);
			THROW 51008, @err_msg2, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
			THROW 51008, @err_msg, 1;
		END CATCH;
		
		
	-- Grantor is NOT owner of the study
	DECLARE @GRANTOR nvarchar(MAX) =
		(
		SELECT 
			grantor, study_id
			from inserted
			minus
		select username, study_id
		from t_rif40_studies
			FOR XML PATH('')
		);
	IF @GRANTOR IS NOT NULL
	BEGIN TRY
		IF IS_MEMBER(N'[rif_manager]') = 1 THEN
		BEGIN
			DECLARE @logmsg VARCHAR(MAX) = 'Study not owned by grantor but user is RIF_MANAGER: '+@GRANTOR;
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[tr_study_shares]', @logmsg;
		END
		ELSE
		BEGIN
			rollback;
			DECLARE @err_msg2 = formatmessage(51009, @GRANTOR);
			THROW 51009, @err_msg2, 1;
		END;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51009, @err_msg2, 1;
	END CATCH 
END;

	
--Delete: check that you own the study you are deleting
IF (@XTYPE = 'D')
BEGIN
	
GO