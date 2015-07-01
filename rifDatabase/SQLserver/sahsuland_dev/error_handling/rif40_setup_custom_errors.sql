use sahsuland_dev;

/*
Function checks if there exists any custom error messages already installed in the database that are not RIF-related.
Returns number of non-RIF error messages (0 if none found)
(Should this be changed to only check the message IDs we actually use?)
*/
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_check_custom_errors]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_check_custom_errors];
CREATE FUNCTION [rif40].[rif40_check_custom_errors] AS
BEGIN
	DECLARE @other_error_curs CURSOR, @m_id int, @m_text nvarchar(2048);
	DECLARE @error_found int = 0;
	SET @other_error_curs = CURSOR FOR
		select message_id, text
		from sys.messages
		where message_id >= 5000 and text not like '%rif%'; 
	OPEN @error_curs;
	FETCH @error_curs INTO @m_id, @m_text;
			
	WHILE @@FETCH_STATUS = 0
	BEGIN
		DECLARE @err_text VARCHAR(2000) := 'Existing custom error message '+@m_id+' ('+@m_text@+')';
		EXEC [rif40].[rif40_log] 'ERROR', 'rif40_check_custom_errors', @err_text;
		@error_found = @error_found + 1;
		FETCH @error_curs INTO @m_id, @m_text;	
	END;
	CLOSE @error_curs;
	DEALLOCATE @error_curs;
	return @error_found;
END;


/*
Procedure deletes all existing RIF-related custom error messages
*/
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_clean_custom_errors]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP PROCEDURE [rif40].[rif40_clean_custom_errors];
CREATE PROCEDURE [rif40].[rif40_clean_custom_errors] AS
BEGIN
	DECLARE @error_curs CURSOR, @m_id int;
	SET @error_curs = CURSOR FOR
		select message_id
		from sys.messages
		where message_id >= 50000 and text like '%rif%'; --when all our error message start in our SQL Server installation
	OPEN @error_curs;
	FETCH @error_curs INTO @m_id;
			
	WHILE @@FETCH_STATUS = 0
	BEGIN
		EXEC sp_dropmessage @m_id;
		FETCH @error_curs INTO @m_id;	
	END;
	CLOSE @error_curs;
	DEALLOCATE @error_curs;
END;

/*
Set up the custom error messages.  Check if non-RIF error messages exist, then delete all the RIF-related ones
*/
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_setup_custom_errors]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP PROCEDURE [rif40].[rif40_setup_custom_errors];
CREATE PROCEDURE [rif40].[rif40_setup_custom_errors] AS
BEGIN
	DECLARE @num_errors int;
	SET @num_errors = [rif40].[rif40_check_custom_errors];
	IF @num_errors > 0 
	BEGIN
		DECLARE @err_msg = 'Problem installing custom error messages.  '+@num_errors+' existing custom error messages in database.';
		EXEC [rif40].[rif40_log] 'ERROR', 'rif40_setup_custom_errors',@err_msg;
		THROW 'Failed to install custom error messages';
	END
	ELSE
	BEGIN
		EXEC [rif40].[rif40_log] 'DEBUG1', 'rif40_setup_custom_errors','No non-RIF custom error messages in database';
		EXEC [rif40].[rif40_clean_custom_errors];
		EXEC [rif40].[rif40_log] 'DEBUG1', 'rif40_setup_custom_errors','All RIF-related custom error messages have been removed.';
	END
END;
