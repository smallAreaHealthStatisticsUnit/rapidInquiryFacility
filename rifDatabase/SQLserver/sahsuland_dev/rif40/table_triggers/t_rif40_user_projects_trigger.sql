/*
Check username exists and is a RIF user
Check project has not ended
 */


IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_user_projects')
BEGIN
	DROP TRIGGER [rif40].[tr_user_projects]
END
GO


CREATE trigger [rif40].[tr_user_projects]
on [rif40].[t_rif40_user_projects]
for insert , update , delete
as
BEGIN
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

	DECLARE @num_studies INT;
	SELECT @num_studies = count(study_id) FROM [rif40].[t_rif40_results];	

-- Check that current username exists and is a RIF user
	IF SUSER_SNAME() = 'rif40' 
	BEGIN
		IF (@XTYPE = 'U' or @XTYPE='I') AND @num_studies = 0
			RETURN; -- no more checks necessary
	END
	ELSE
	BEGIN
		IF (IS_MEMBER(N'[rif_manager]') = 0 AND IS_MEMBER(N'[rif_user]') = 0)
		BEGIN TRY
			rollback;
			DECLARE @err1 VARCHAR(max);
			SET @err1 = formatmessage(51000, SUSER_SNAME());
			THROW 51000, @err1, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_user_projects]';
			THROW 51000, @err1, 1; --rethrow
		END CATCH;		
	END;
	
	IF @XTYPE = 'D'
	BEGIN
		DECLARE @log_msg0 VARCHAR(max) = 'T_RIF40_USER_PROJECTS user is a RIF_USER' + SUSER_SNAME();
		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[tr_user_projects]', @log_msg0;
		RETURN;
	END;
	
-- Check that the project name and username values are filled in (already not null constraints on table but values could be '')
	DECLARE @missing_vals NVARCHAR(MAX)= 
	( 
	SELECT ic.project, ic.username
		 FROM inserted  ic
		 WHERE ic.project is null or ic.project='' or ic.username is null or ic.username = ''
	FOR XML PATH('') 
	); 
	IF (@missing_vals IS NOT NULL)
	BEGIN TRY
		rollback;
		DECLARE @err VARCHAR(max);
		SET @err = formatmessage(51005,@missing_vals);
		THROW 51005, @err, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_user_projects]';
		THROW 51005, @err, 1; 
	END CATCH;
	
	-- username is valid user
	DECLARE @invalid_username varchar(max) = (
		SELECT a.username
		FROM inserted a
		WHERE NOT EXISTS (select 1
		FROM [sys].[database_principals] b WHERE a.username collate database_default=b.name collate database_default)
		FOR XML PATH('')
	);
	IF @invalid_username IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 varchar(max) = formatmessage(51010, @invalid_username);
		THROW 51010, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_study_shares]';
		THROW 51010, @err_msg3, 1;
	END CATCH 
	
-- Check project has no end date + log
	DECLARE @date_ended NVARCHAR(MAX)= 
	( 
	SELECT ic.project
			FROM inserted  ic, [rif40].[t_rif40_projects] p
			WHERE ic.project = p.project and DATE_ENDED is null
	FOR XML PATH('') 
	); 
	IF @date_ended IS NOT NULL 
	BEGIN
		DECLARE @log_msg VARCHAR(max) = 'project has no end set: '+@date_ended;
		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[tr_user_projects]', @log_msg;
	END;

-- Check whether project has already ended + produce error if not in testing mode
DECLARE @date_error NVARCHAR(MAX)= 
( 
  SELECT ic.project, p.date_ended
		 FROM inserted  ic , [rif40].[t_rif40_projects] p
		 WHERE ic.project = p.project and p.date_ended < getdate()
 FOR XML PATH('') 
); 

IF @date_error IS NOT NULL 
	BEGIN
		IF @num_studies > 0 
		BEGIN TRY
			rollback;
			DECLARE @err3 VARCHAR(max);
			SET @err3 = formatmessage(500071,@date_error);
			THROW 50071,@err3,1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_user_projects]';
			THROW 50071,@err3,1;
		END CATCH;
		ELSE
		BEGIN
			DECLARE @err_msg VARCHAR(max) = 'Project ended on : '+@date_error;
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[tr_user_projects]', @err_msg;
		END;
	END;
	
end; 
GO
