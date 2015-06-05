/*
Check username exists and is a RIF user
Check project has not ended

 ((((new.project IS NOT NULL) AND ((new.project)::text <> ''::text)) OR ((new.username IS NOT NULL) AND ((new.username)::text <> ''::text))))
 
 */

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_user_projects')
BEGIN
	DROP TRIGGER [rif40].[tr_user_projects]
END
GO


CREATE trigger [tr_user_projects]
on [rif40].[t_rif40_user_projects]
for insert , update 
as
begin

-- Check username exists and is a RIF user
	IF (IS_MEMBER(N'[rif_manager]') = 0 AND IS_MEMBER(N'[rif_user]') = 0)
	BEGIN TRY
		rollback;
		DECLARE @err1 VARCHAR(max);
		SET @err1 = formatmessage(51000, SUSER_SNAME());
		THROW 51000, @err1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc];
	END CATCH;
	
-- Check project has not ended
DECLARE @date_ended NVARCHAR(MAX)= 
( 
  SELECT ic.project +' , '
		 FROM inserted  ic
		 WHERE ic.project = (select [PROJECT] from [rif40].[t_rif40_projects] where DATE_ENDED is null)
 FOR XML PATH('') 
); 
IF @date_ended IS NOT NULL 
	BEGIN TRY
		rollback;
		DECLARE @err2 VARCHAR(max);
		SET @err2 = formatmessage(50070, @date_ended);
		THROW 50070,@err2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc];
	END CATCH;


DECLARE @sysdate NVARCHAR(MAX)= 
( 
  SELECT cast(p.DATE_ENDED as varchar(50)) +' , '
		 FROM inserted  ic , [rif40].[t_rif40_projects] p
		 WHERE ic.project = (select [PROJECT] from [rif40].[t_rif40_projects] where DATE_ENDED < getdate())
 FOR XML PATH('') 
); 
BEGIN TRY
IF @sysdate IS NOT NULL 
	BEGIN 
		raiserror(50071,16,1,@sysdate); 
		rollback
	END;
END TRY
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
end 
