
/*
Function checks if there exists any custom error messages already installed in the database that are not RIF-related. 
Assume that no other program would use the string 'rif40' in its own custom error messages.
Returns number of non-RIF error messages (0 if none found)
(Should this be changed to only check the message IDs we actually use?)

No logging because you cannot call a stored procedure within a function in SQL Server
*/
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_check_custom_errors]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_check_custom_errors];
 GO

  
CREATE FUNCTION [rif40].[rif40_check_custom_errors] ()
	RETURNS int AS
BEGIN
	DECLARE @error_curs CURSOR, @m_id int;
	DECLARE @error_found int = 0;
	SET @error_curs = CURSOR FOR
		select message_id
		from sys.messages
		where message_id >= 50000 and text not like '%rif40%'; 
	OPEN @error_curs;
	FETCH @error_curs INTO @m_id;
			
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @error_found = @error_found + 1;
		FETCH @error_curs INTO @m_id;	
	END;
	CLOSE @error_curs;
	DEALLOCATE @error_curs;
	return @error_found;
END
GO


/*
Procedure deletes all existing RIF-related custom error messages
Defines a RIF-related custom error message as having a message id >= 50000 and the message text includes the string 'rif40'
*/
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_clean_custom_errors]')
                  AND type IN ( N'P', N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP PROCEDURE [rif40].[rif40_clean_custom_errors];
GO

CREATE PROCEDURE [rif40].[rif40_clean_custom_errors] AS
BEGIN
	DECLARE @error_curs CURSOR, @m_id int;
	SET @error_curs = CURSOR FOR
		select message_id
		from sys.messages
		where message_id >= 50000 and text like '%rif40%'; 
	OPEN @error_curs;
	FETCH @error_curs INTO @m_id;
			
	WHILE @@FETCH_STATUS = 0
	BEGIN
		EXEC sp_dropmessage @m_id;
		FETCH @error_curs INTO @m_id;	
	END;
	CLOSE @error_curs;
	DEALLOCATE @error_curs;
	EXEC [rif40].[rif40_log] 'DEBUG1', 'rif40_clean_custom_errors','All RIF-related custom error messages have been removed.';
END;
GO

/*
Set up the custom error messages.  Check if non-RIF error messages exist, then delete all the RIF-related ones
*/
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_setup_custom_errors]')
                  AND type IN (N'P', N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP PROCEDURE [rif40].[rif40_setup_custom_errors];
GO
CREATE PROCEDURE [rif40].[rif40_setup_custom_errors] AS
BEGIN
	DECLARE @num_errors int;
	SET @num_errors = [rif40].[rif40_check_custom_errors]();
	IF @num_errors > 0 
	BEGIN
		DECLARE @err_msg VARCHAR(2000) = 'Problem installing custom error messages.  '+convert(varchar,@num_errors)+' existing custom error messages in database.';
		EXEC [rif40].[rif40_log] 'ERROR', 'rif40_setup_custom_errors',@err_msg;
		THROW 50000,@err_msg,1;
	END
	ELSE
	BEGIN
		EXEC [rif40].[rif40_log] 'DEBUG1', 'rif40_setup_custom_errors','No non-RIF custom error messages in database';
		EXEC [rif40].[rif40_clean_custom_errors];
	END
END;
GO


