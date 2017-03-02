
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_error_msg_checks')
BEGIN
	DROP TRIGGER [rif40].[tr_error_msg_checks]
END
GO

create trigger [tr_error_msg_checks]
  on  [rif40].[rif40_error_messages]
  for insert , update 
  as
  begin
	 DECLARE @table_missing nvarchar(MAX) =
    (
    SELECT 
		[TABLE_NAME] + ', '
        FROM inserted
        WHERE OBJECT_ID([TABLE_NAME], 'U') IS NULL
        FOR XML PATH('')
    );

	IF @table_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51042, @table_missing);
		THROW 51042, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_error_messages]';
		THROW 51042, @err_msg1, 1;
	END CATCH;	
	
 end ;
GO
