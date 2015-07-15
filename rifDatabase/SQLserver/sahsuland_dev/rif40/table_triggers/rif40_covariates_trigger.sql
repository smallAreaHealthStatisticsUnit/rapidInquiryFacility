-----------------------------------------
--trigger_covariate
------------------------------------------
--Check <T_RIF40_GEOLEVELS.COVARIATE_TABLE>.<COVARIATE_NAME> column exists.
--Check - min < max, max/min precison is appropriate to type --NEED TO TEST THIS
-- eror msgs -- 50010 - 50014


USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_covariates_check')
BEGIN
	DROP TRIGGER [rif40].[tr_covariates_check]
END
GO



CREATE TRIGGER [tr_covariates_check]
on [rif40].[rif40_covariates]
for insert ,update 
as 
SET XACT_ABORT off
begin

--valid values
DECLARE @invalid_values VARCHAR(max) =
(
	select covariate_name, max, min, type
	from inserted
	where covariate_name is null or covariate_name = ''
	or max is null or max = ''
	or min is null or min = ''
	or type is null or type = ''
	FOR XML PATH('')
);
IF @invalid_values IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51030, @invalid_values);
	THROW 51030, @err_msg1, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_covariates]';
	THROW 51030, @err_msg1, 1;
END CATCH;


---------------------------------------------------------------------------------
--Check <T_RIF40_GEOLEVELS.COVARIATE_TABLE>.<COVARIATE_NAME> column exists.
---------------------------------------------------------------------------------
DECLARE @no_covar_table NVARCHAR(MAX)= 
( 
 SELECT geolevel_name
 FROM   inserted  a
 WHERE  not exists 
		(select 1 from  [rif40].[t_rif40_geolevels] b
		where covariate_table is not null and covariate_table <> '' 
		and b.geolevel_name=a.geolevel_name
		and [rif40].[rif40_is_object_resolvable](covariate_table) = 1)
 FOR XML PATH('') 
); 

IF @no_covar_table IS NOT NULL 
BEGIN  TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51031, @no_covar_table);
		THROW 51031, @err_msg2, 1;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_covariates]';
		THROW 51031, @err_msg2, 1;
END CATCH ;

DECLARE @col_not_found NVARCHAR(MAX) = 
(
	SELECT b.covariate_table, a.covariate_name
	FROM inserted a, [rif40].[t_rif40_geolevels] b
	WHERE a.geolevel_name=b.geolevel_name 
	and not exists 
		(select 1 from [INFORMATION_SCHEMA].[COLUMNS] c 
		where c.table_name=b.covariate_table
		and c.column_name=a.covariate_name)
	FOR XML PATH ('')
);
IF @col_not_found IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51032, @col_not_found);
	THROW 51032, @err_msg3, 1;
END TRY
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_covariates]';
		THROW 51032, @err_msg3, 1;
END CATCH ;


--------------------------------
-- MIN value is greater than MAX
--------------------------------
DECLARE @min_check NVARCHAR(MAX)= 
( 
 SELECT min, max, geolevel_name, covariate_name
 FROM   inserted  
 WHERE   min>=max 
 FOR XML PATH('') 
);
IF @min_check IS NOT NULL 
BEGIN TRY
	rollback;
	DECLARE @err_msg4 VARCHAR(MAX) = formatmessage(51033, @min_check);
	THROW 51033, @err_msg4, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_covariates]';
	THROW 51033, @err_msg3, 1;
END CATCH;	
		
-----------------------------------
-- Type is 1 and MAX is not INT
-----------------------------------
DECLARE @max_type_check NVARCHAR(MAX)= 
( 
 SELECT max, geolevel_name, covariate_name
 FROM   inserted  
 WHERE   type=1 and round(max,0)<>max 
 FOR XML PATH('') 
); 
IF @max_type_check IS NOT NULL 
BEGIN TRY
	rollback;
	DECLARE @err_msg5 VARCHAR(MAX) = formatmessage(51034, @min_check);
	THROW 51034, @err_msg5, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_covariates]';
	THROW 51034, @err_msg5, 1;
END CATCH;	

-----------------------------------
-- Type is 1 and MIN is not INT
-----------------------------------	
DECLARE @min_type_check NVARCHAR(MAX)= 
( 
 SELECT min, geolevel_name, covariate_name
 FROM   inserted  
 WHERE   type=1 and round(min,0)<>min 
 FOR XML PATH('') 
); 
IF @min_type_check IS NOT NULL 
BEGIN TRY
	rollback;
	DECLARE @err_msg6 VARCHAR(MAX) = formatmessage(51035, @min_check);
	THROW 51035, @err_msg6, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_covariates]';
	THROW 51035, @err_msg6, 1;
END CATCH;	
-----------------------------------
-- Type is 1 and MIN <0
-----------------------------------
DECLARE @min_value_check NVARCHAR(MAX)= 
( 
 SELECT min ,geolevel_name,covariate_name
 FROM   inserted  
 WHERE   type=1 and min<0 
 FOR XML PATH('') 
);
IF @min_value_check IS NOT NULL 
BEGIN TRY
	rollback;
	DECLARE @err_msg7 VARCHAR(MAX) = formatmessage(51036, @min_check);
	THROW 51036, @err_msg7, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_covariates]';
	THROW 51036, @err_msg7, 1;
END CATCH;	
END;
-------------------------------------------------------------------------------------
-- end of trigger code 
-------------------------------------------------------------------------------------
