use sahsuland_dev;

DROP PROCEDURE [rif40].[rif40_clean_custom_errors];
CREATE PROCEDURE [rif40].[rif40_clean_custom_errors] AS
BEGIN
	DECLARE @error_curs CURSOR, @m_id int;
	SET @error_curs = CURSOR FOR
		select message_id
		from sys.messages
		where message_id >= 50010 and text like '%rif%'; --when all our error message start in our SQL Server installation
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


EXEC [rif40].[rif40_clean_custom_errors];
