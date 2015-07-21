/*
Function: 	rif40_db_name_check()
Parameters:	Column name, value
Returns:	NONE
Description:	Check column name value obeys DB naming conventions; i.e. Oracles. Value is assumed to be in upper case; even on Postgres where the convention is lower case.
	(SQL Server's rules are not nearly as strict as Oracle's)
*/

use sahsuland_dev
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_db_name_check]')
                  AND type IN (N'P', N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP PROCEDURE [rif40].[rif40_db_name_check]
GO 


CREATE PROCEDURE [rif40].[rif40_db_name_check](@l_columnname VARCHAR(max), @l_value VARCHAR(max))
	AS
BEGIN
	DECLARE @maxlen int = 30; --for Oracle

--No special characters (A-Za-z0-9_)
    DECLARE @invalid_chars VARCHAR(max) =substring(@l_value, PATINDEX('%[^A-Za-z0-9]%', @l_value),1);
	IF @invalid_chars IS NOT NULL AND @invalid_chars <> ''
	BEGIN TRY
		DECLARE @err_txt1 VARCHAR(max) = 'column '+@l_columnname+' value='+@l_value+' contains '+@invalid_chars;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51050, @err_txt1);
		THROW 51050, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_db_name_check]';
		THROW 51050, @err_msg1, 1;
	END CATCH;	
	
--
-- Length must <= 30
--
	IF len(@l_value) > @maxlen 
	BEGIN TRY
		DECLARE @err_txt2 VARCHAR(max) = 'column '+@l_columnname+' value='+@l_value;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51051, @err_txt2);
		THROW 51051, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_db_name_check]';
		THROW 51051, @err_msg2, 1;
	END CATCH;	

--
-- First character must be a letter
--
	IF substring(@l_value, 1, 1) NOT LIKE '[A-Za-z]%'
	BEGIN TRY
		DECLARE @err_txt3 VARCHAR(max) = 'column '+@l_columnname+' value='+@l_value;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51052, @err_txt3);
		THROW 51052, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_db_name_check]';
		THROW 51052, @err_msg3, 1;
	END CATCH;	

	--no error, therefore must be fine
	DECLARE @log_msg VARCHAR(max) = 'Valid name for column '+@l_columnname+': '+@l_value;
	EXEC [rif40].[rif40_log] 'DEBUG3', '[rif40].[rif40_db_name_check]', @log_msg;
END;
