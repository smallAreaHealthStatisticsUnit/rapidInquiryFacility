/*
<trigger_rif40_tables_checks_description>
<para>
Check TABLE_NAME exists. DO NOT RAISE AN ERROR IF IT DOES; otherwise check, column <TABLE_NAME>.TOTAL_FIELD exists. 
This allows the RIF40 schema owner to not have access to the tables. Access is checked in RIF40_NUM_DENOM. 
Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). 
A user specific T_RIF40_NUM_DENOM is supplied for other combinations. 
Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.
Check table_name, total_field, sex_field_name, age_group_field_name, age_sex_group_field_name Oracle names.
</para>
</trigger_rif40_tables_checks_description>
*/


IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_tables_checks')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_tables_checks]
END
GO

------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_tables_checks]
on [rif40].[rif40_tables]
FOR insert , update , delete
as
BEGIN 
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
Declare  @XTYPE varchar(5);


	IF EXISTS (SELECT * FROM DELETED)
	BEGIN
		SET @XTYPE = 'D'
	END;
	
	IF EXISTS (SELECT * FROM INSERTED)
	BEGIN
		IF (@XTYPE = 'D')
			SET @XTYPE = 'U'
		ELSE 
			SET @XTYPE = 'I'
	END

-- Check if studies have been run
DECLARE @has_studies_check VARCHAR(MAX) = 
(
	SELECT count(study_id) as total
	FROM [rif40].[t_rif40_studies]
);

--delete not allowed
--IF @XTYPE = 'D' AND @has_studies_check>0
--BEGIN TRY
--	rollback;
--	DECLARE @err_msg0 VARCHAR(MAX) = formatmessage(51147);
--	THROW 51147, @err_msg0, 1;
--END TRY
--BEGIN CATCH
--	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_tables]';
--	THROW 51147, @err_msg0, 1;
--END CATCH;		
	
--Check if column <TABLE_NAME>.TOTAL_FIELD exists
IF (@XTYPE = 'U' or @XTYPE = 'I') 
BEGIN
	declare @total_field_check varchar(max) = (
	SELECT table_name, total_field
	FROM   inserted  b
	where 
		b.total_field is not null and b.total_field <> ''
		AND not exists (
		select 1
		from INFORMATION_SCHEMA.COLUMNS a
		where a.table_name collate database_default=b.table_name collate database_default
		and a.column_name collate database_default=b.total_field collate database_default
		)
	 FOR XML PATH('') 
	);

	IF @total_field_check IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51037, @total_field_check);
		THROW 51037, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_tables]';
		THROW 51037, @err_msg1, 1;
	END CATCH;	
	ELSE
		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[rif40_tables]', 'RIF40_TABLES TOTAL_FIELD column found in table';
		

	-- Check direct standardised denominators exist system wide
	DECLARE @dsd_table_missing VARCHAR(MAX) = (
		SELECT table_name
		FROM inserted b
		WHERE isdirectdenominator = 1
		AND (table_name is null or table_name = ''
		OR not exists (
		select 1
		FROM INFORMATION_SCHEMA.TABLES a
		where a.table_name collate database_default=b.table_name collate database_default))		
		FOR XML PATH(''));
	
	IF @dsd_table_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX)=formatmessage(51038, @dsd_table_missing);
		THROW 51038, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_tables]';
		THROW 51038, @err_msg2, 1;
	END CATCH;	

	--valid Oracle name checks:
	
DECLARE table_name_cursor CURSOR FOR
	SELECT table_name, total_field, sex_field_name, age_group_field_name, age_sex_group_field_name
	from inserted;
DECLARE @curs_table_name VARCHAR(MAX), @curs_total_field VARCHAR(MAX), @curs_sex_field_name VARCHAR(MAX), @curs_age_group_field_name VARCHAR(MAX), @curs_age_sex_group_field_name VARCHAR(MAX);

OPEN table_name_cursor;
FETCH table_name_cursor INTO @curs_table_name, @curs_total_field, @curs_sex_field_name, @curs_age_group_field_name, @curs_age_sex_group_field_name;
WHILE @@FETCH_STATUS = 0  
BEGIN  
	EXEC [rif40].[rif40_db_name_check] 'TABLE_NAME', @curs_table_name;
	EXEC [rif40].[rif40_db_name_check] 'TOTAL_FIELD', @curs_total_field;
	EXEC [rif40].[rif40_db_name_check] 'SEX_FIELD_NAME', @curs_sex_field_name;
	EXEC [rif40].[rif40_db_name_check] 'AGE_GROUP_NAME', @curs_age_group_field_name;
	EXEC [rif40].[rif40_db_name_check] 'AGE_SEX_GROUP_NAME', @curs_age_sex_group_field_name;
	FETCH table_name_cursor INTO @curs_table_name, @curs_total_field, @curs_sex_field_name, @curs_age_group_field_name, @curs_age_sex_group_field_name;
END;
CLOSE table_name_cursor ;
DEALLOCATE table_name_cursor ;


END;

END;
GO
