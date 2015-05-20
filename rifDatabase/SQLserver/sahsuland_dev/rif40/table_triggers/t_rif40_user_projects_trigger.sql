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

-- Check project has not ended
DECLARE @date_ended NVARCHAR(MAX)= 
( 
  SELECT ic.project +' , '
		 FROM inserted  ic
		 WHERE ic.project = (select [PROJECT] from [rif40].[t_rif40_projects] where DATE_ENDED is null)
 FOR XML PATH('') 
); 
BEGIN TRY
IF @date_ended IS NOT NULL 
	BEGIN 
		raiserror(50070,16,1,@date_ended); 
		rollback
	END;
END TRY
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 


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
