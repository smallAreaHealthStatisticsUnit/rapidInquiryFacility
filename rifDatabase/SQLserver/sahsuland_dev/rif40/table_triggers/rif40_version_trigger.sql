/*
Check - single column, populate schema_amended. Prevent DELETE or INSERT
UPDATE - update the schema_amended field

error numbers/logging?
*/

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
AFTER insert , update ,delete 
As
Begin

-- Determine the type of transaction 
Declare  @xtype varchar(5)

	IF EXISTS (SELECT * FROM DELETED)
		SET @XTYPE = 'D';
	
	IF EXISTS (SELECT * FROM INSERTED)
	BEGIN
		IF (@XTYPE = 'D')
			SET @XTYPE = 'U';
		ELSE
			SET @XTYPE = 'I';
		
	END

	--When Transaction is a delete  
    IF (@XTYPE = 'D')
		begin
			raiserror( 'Error: RIF40_VERSION DELETE disallowed',16,1);
			rollback transaction;
		end 
    
   
   --invalid data inserted/updated
	IF (@XTYPE = 'I' or @XTYPE = 'U') AND EXISTS (SELECT * FROM INSERTED WHERE version IS NULL OR version='')
	BEGIN
		Raiserror ('Error: RIF40_VERSION INSERT/UPDATE invalid data - missing version field', 16,1 );
		rollback transaction;
	END
	
	--When Transaction is an insert when table is not previously empty
	IF (@XTYPE = 'I')
		BEGIN
		  if (SELECT COUNT(*) total FROM [rif40].[rif40_version]) > 1 
		  begin 
			Raiserror ('Error: RIF40_VERSION INSERT disallowed', 16,1 );
			rollback transaction;
		  end 
		END

   --When Transaction is an Update
   IF (@XTYPE = 'U')
   BEGIN
		update [rif40].[rif40_version] set schema_amended=sysdatetime();
	END
 end 

 