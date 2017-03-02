/*
<trigger_t_rif40_inv_conditions_checks_description>
<para>
Check - USERNAME exists.
Check - DELETE only allowed on own records.
</para>
</trigger_t_rif40_inv_conditions_checks_description>
 */


IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_t_rif40_inv_conditions')
BEGIN
	DROP TRIGGER [rif40].[tr_t_rif40_inv_conditions]
END
GO


CREATE trigger [tr_t_rif40_inv_conditions]
on [rif40].[t_rif40_inv_conditions]
for insert, update, delete
as
begin
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
Declare  @XTYPE varchar(5);

IF EXISTS (SELECT * FROM DELETED)
	SET @XTYPE = 'D'
	
IF EXISTS (SELECT * FROM INSERTED)
BEGIN
	IF (@XTYPE = 'D')
		SET @XTYPE = 'U'
	ELSE 
		SET @XTYPE = 'I'
END;


--check if username is correct
IF @XTYPE = 'I' OR @XTYPE='U'
BEGIN
	DECLARE @different_user	VARCHAR(MAX) = 
	(
		SELECT username, study_id, inv_id, line_number
		FROM inserted
		WHERE username != SUSER_SNAME()
	 FOR XML PATH('') 
	); 
	IF @different_user IS NOT NULL
	BEGIN
	DECLARE @has_studies_check VARCHAR(MAX) = 
	(
		SELECT count(study_id) as total
		FROM [rif40].[t_rif40_results]
	);
	IF SUSER_SNAME() = 'RIF40' and @has_studies_check=0
		BEGIN
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_inv_conditions]', 't_rif40_inv_conditions insert allowed during build';
			RETURN;
		END;
		ELSE
			BEGIN TRY
				rollback;
				DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51065, @different_user);
				THROW 51065, @err_msg1, 1;
			END TRY
			BEGIN CATCH
				EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_conditions]';
				THROW 51065, @err_msg1, 1;
			END CATCH;	
	END;
END;

--updates not allowed		
IF @XTYPE = 'U'
BEGIN TRY
	rollback;
	DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51066);
	THROW 51066, @err_msg2, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_conditions]';
	THROW 51066, @err_msg2, 1;
END CATCH;	

--can only delete your own records
IF @XTYPE = 'D'
BEGIN
	DECLARE @different_user_del	VARCHAR(MAX) = 
	(
		SELECT username, study_id, inv_id, line_number
		FROM deleted
		WHERE username != SUSER_SNAME()
	 FOR XML PATH('') 
	); 
	IF @different_user_del IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51067, @different_user_del);
		THROW 51067, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_conditions]';
		THROW 51067, @err_msg3, 1;
	END CATCH;	
END;
	
END;
GO
