/*
Check - single column, populate schema_amended. Prevent DELETE or INSERT
UPDATE - update the schema_amended field

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

CREATE  trigger [rif40].[tr_version]
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
	BEGIN
		BEGIN TRY
			rollback transaction;
			DECLARE @error_msg2 varchar(max);
			SET @error_msg2 = formatmessage(51002);
			THROW 51002,@error_msg2, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_version]';
			THROW 51002,@error_msg2, 1;  --rethrow
		END CATCH;
	END
   
   --invalid data inserted/updated
	IF (@XTYPE = 'I' or @XTYPE = 'U') AND EXISTS (SELECT * FROM INSERTED WHERE version IS NULL OR version='')
	BEGIN
		BEGIN TRY
			rollback transaction;
			DECLARE @error_msg1 varchar(max);
			SET @error_msg1 = formatmessage(51004);  --'[rif40].[rif40_version] , INSERT disallowed, rows'+convert(char,@num_rows);
			THROW 51004, @error_msg1, 1; --'[rif40].[RIF40_VERSION] , INSERT/UPDATE invalid data - missing version field', 1 ;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_version]';
			THROW 51004, @error_msg1, 1; --rethrow
		END CATCH;
	END
	
	--When Transaction is an insert when table is not previously empty
	IF (@XTYPE = 'I')
		BEGIN
			BEGIN TRY 
			DECLARE @num_rows int;
			SELECT @num_rows = count(*) FROM inserted;
			if (SELECT COUNT(*) total FROM [rif40].[rif40_version]) > 1 
			BEGIN
				rollback transaction;
				DECLARE @error_msg varchar(max);
				SET @error_msg = formatmessage(51003,convert(char,@num_rows));  --'[rif40].[rif40_version] , INSERT disallowed, rows'+convert(char,@num_rows);
				THROW 51003, @error_msg,1;
			END;
			END TRY
			BEGIN CATCH
				EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_version]';
				THROW 51003, @error_msg,1; --rethrow
			END CATCH ;		
		END

   --When Transaction is an Update
   IF (@XTYPE = 'U')
   BEGIN
		update [rif40].[rif40_version] set schema_amended=sysdatetime();
	END
 end 
 
 
