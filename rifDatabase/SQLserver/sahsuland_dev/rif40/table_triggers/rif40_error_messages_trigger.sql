USE [sahsuland_dev]
GO

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
	 DECLARE @tablelist nvarchar(MAX) =
    (
    SELECT 
		[TABLE_NAME] + ', '
        FROM inserted
        WHERE OBJECT_ID([TABLE_NAME], 'U') IS NULL
        FOR XML PATH('')
    );

	IF @tablelist IS NOT NULL
	BEGIN
		RAISERROR('These table/s do not exist: %s', 16, 1, @tablelist) with log;
	END;
  
 end 
GO
