------------------------------------------------------
---trigger code for [dbo].[T_RIF40_USER_PROJECTS]
-----------------------------------------------------


create alter trigger tr_USER_PROJECTS
on [dbo].[T_RIF40_USER_PROJECTS]
for insert , update 
as
begin

-- Check project has not ended
DECLARE @date_ended NVARCHAR(MAX)= 
( 
  SELECT ic.project +' , '
		 FROM inserted  ic
		 WHERE ic.project = (select [PROJECT] from [dbo].[T_RIF40_PROJECTS] where DATE_ENDED is null)
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
		EXEC [ErrorLog_proc]
END CATCH 


DECLARE @sysdate NVARCHAR(MAX)= 
( 
  SELECT cast(p.DATE_ENDED as varchar(50)) +' , '
		 FROM inserted  ic , [T_RIF40_PROJECTS] p
		 WHERE ic.project = (select [PROJECT] from [dbo].[T_RIF40_PROJECTS] where DATE_ENDED < getdate())
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
		EXEC [ErrorLog_proc]
END CATCH 
end 


----------end of trigger code ------------------		
		 
EXEC sp_addmessage 50070, 16, 
   N'Table_name:[dbo].[T_RIF40_USER_PROJECTS] , project has no end set:(%s)';
GO
	
EXEC sp_addmessage 50071, 16, 
   N'Table_name:[dbo].[T_RIF40_USER_PROJECTS] , project ended on :(%s)';
GO
	



