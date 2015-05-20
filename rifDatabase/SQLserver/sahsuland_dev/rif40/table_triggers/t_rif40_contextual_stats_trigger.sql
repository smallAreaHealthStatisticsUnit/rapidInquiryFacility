/*
Trigger for [dbo].[T_RIF40_CONTEXTUAL_STATS]
CANT WE DO THESE WITH PERMISSION ?
Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
*/
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
------------------------------------------------
--When Transaction is an Update  then rollback 
-------------------------------------------------
	IF (@XTYPE = 'U')
		BEGIN
		 	Raiserror (50088, 16,1 );
			rollback tran
		  end 

--Entered username is not current user 
DECLARE @user nvarchar(MAX) =
    (
    SELECT 
		USERNAME + ', '
        FROM inserted
        WHERE username <> SUSER_SNAME() 
        FOR XML PATH('')
    );
BEGIN TRY 
	IF @user IS NOT NULL
	BEGIN
		RAISERROR(50089, 16, 1, @user) with log;
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 

--Delete allowed on own record only 
	DECLARE @user2 nvarchar(MAX) =
    (
    SELECT 
		USERNAME + ', '
        FROM deleted
        WHERE username not in (select username from inserted)
        FOR XML PATH('')
    );
BEGIN TRY 
	IF @user2 IS NOT NULL and  @XTYPE = 'D'
	BEGIN
		RAISERROR(50090, 16, 1, @user) with log;
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
END