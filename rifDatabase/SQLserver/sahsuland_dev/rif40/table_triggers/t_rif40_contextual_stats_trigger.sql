/*
Trigger for [dbo].[T_RIF40_CONTEXTUAL_STATS]
CANT WE DO THESE WITH PERMISSION ?
Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_contextual_stats')
BEGIN
	DROP TRIGGER [rif40].[tr_contextual_stats]
END
GO


CREATE trigger [tr_contextual_stats]
on [rif40].[t_rif40_contextual_stats]
for insert, update 
as

Declare  @xtype varchar(5)

	IF EXISTS (SELECT * FROM DELETED)
	BEGIN
		SET @XTYPE = 'D'
	END
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
		
--check if it is during a build, then insert and update are fine
DECLARE @has_studies_check VARCHAR(MAX) = 
(
	SELECT count(study_id) as total
	FROM [rif40].[t_rif40_results]
);

IF (@has_studies_check=0 AND SUSER_SNAME() = 'RIF40' AND (@XTYPE='U' OR @XTYPE='I'))
	RETURN;
	
--Entered username is not current user on insert
IF (@XTYPE = 'I') 
BEGIN
	DECLARE @new_user_check nvarchar(MAX) =
    (
    SELECT 
		USERNAME, study_id, inv_id, area_id
        FROM inserted
        WHERE username <> SUSER_SNAME() 
        FOR XML PATH('')
    );
	
	IF @new_user_check IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51047, @new_user_check);
		THROW 51047, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_contextual_stats]';
		THROW 51047, @err_msg2, 1;
	END CATCH;	
END;
	
------------------------------------------------
--When Transaction is an Update  then rollback
-------------------------------------------------
IF (@XTYPE = 'U')
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51046);
		THROW 51046, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_contextual_stats]';
		THROW 51046, @err_msg1, 1;
	END CATCH;	

--Delete allowed on own record only 
IF (@XTYPE='D')
BEGIN
	DECLARE @old_user_check nvarchar(MAX) =
    (
    SELECT 
		USERNAME, study_id, inv_id, area_id
        FROM deleted a
		WHERE username <> SUSER_SNAME() 
        FOR XML PATH('')
    );
	IF @old_user_check IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51048, @old_user_check);
		THROW 51048, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_contextual_stats]';
		THROW 51048, @err_msg3, 1;
	END CATCH;	
END;
END;
