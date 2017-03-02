/*
<para>
Check current_version_start_year (if not NULL) BETWEEN rif40_tables.year_start AND rif40_tables.year_stop
Check numer_tab is a numerator
</para>
*/


IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_table_outcome')
BEGIN
	DROP TRIGGER [rif40].[tr_table_outcome]
END
GO


 CREATE TRIGGER [tr_table_outcome]
 ON [rif40].[rif40_table_outcomes]
 FOR INSERT ,  UPDATE 
 AS
 BEGIN 

 --check that numerator table exists in rif_tables
 DECLARE @numer_table_missing  nvarchar(MAX) =
(
		SELECT a.OUTCOME_GROUP_NAME , a.numer_tab
		FROM inserted a
        where not exists (select 1 from [rif40].[rif40_tables] b
				WHERE a.numer_tab=b.table_name)
        FOR XML PATH('')
 );

IF @numer_table_missing is NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51039, @numer_table_missing);
		THROW 51039, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_table_outcomes]';
		THROW 51039, @err_msg1, 1;
	END CATCH;	
	

--check current start year  
  DECLARE @start_year_prob nvarchar(MAX) =
		(
		SELECT 
        a.OUTCOME_GROUP_NAME , a.[CURRENT_VERSION_START_YEAR]
		FROM inserted a, [rif40].[rif40_tables] b
        where a.NUMER_TAB=b.TABLE_NAME
		and (a.CURRENT_VERSION_START_YEAR < b.YEAR_START or b.YEAR_STOP < a.CURRENT_VERSION_START_YEAR)
        FOR XML PATH('')
		 );

IF @start_year_prob  is NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51040, @start_year_prob);
		THROW 51040, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_table_outcomes]';
		THROW 51040, @err_msg2, 1;
	END CATCH;	

--
-- Check numer_tab is a numerator
--
  DECLARE @table_not_num nvarchar(MAX) =
		(
		SELECT 
        a.OUTCOME_GROUP_NAME , a.[CURRENT_VERSION_START_YEAR]
		FROM inserted a, [rif40].[rif40_tables] b
        where a.NUMER_TAB=b.TABLE_NAME
		and (b.isnumerator is null or b.isnumerator != 1)
		 FOR XML PATH('')
		 );
		 
IF @table_not_num  is NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51041, @table_not_num);
		THROW 51041, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_table_outcomes]';
		THROW 51041, @err_msg3, 1;
	END CATCH;	

DECLARE @ok_new_numerators VARCHAR(MAX) =
(
	SELECT outcome_group_name+' table='+numer_tab+' '
	from inserted
	FOR XML PATH('')
);
DECLARE @log_msg1 VARCHAR(MAX) = 'RIF40_TABLE_OUTCOMES outcome group numerator OK: '+@ok_new_numerators;
EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[rif40_table_outcomes]', @log_msg1;
		
END; 
GO
