USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_version')
BEGIN
	DROP TRIGGER [rif40].[tr_version]
END
GO

CREATE  trigger [tr_version]
on [rif40].[rif40_version]
for insert , update ,delete 
As
Begin

-- Determine the type of transaction 
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
--When Transaction is an insert 
	IF (@XTYPE = 'I')
		BEGIN
		  if (SELECT COUNT(*) total FROM [rif40].[rif40_version]) > 0 
		  begin 
			Raiserror ('Error: RIF40_VERSION INSERT disallowed', 16,1 );
			rollback tran
		  end 
		END
--When Transaction is a delete  
    IF (@XTYPE = 'D')
		begin
			raiserror( 'Error: RIF40_VERSION DELETE disallowed',16,1)
			rollback tran
		end 
   End 
 end 

 