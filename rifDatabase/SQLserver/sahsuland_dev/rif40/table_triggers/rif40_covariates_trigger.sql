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
 FROM   inserted  
 WHERE  geolevel_name not in (select geolevel_name from [rif40].[t_rif40_geolevels] where covariate_table is not null and covariate_table <> '' 
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

--we actually only want the records that fail this...
DECLARE @col_found NVARCHAR(MAX) = 
(
	SELECT a.geolevel_name, b.covariate_table, a.covariate_name
	FROM inserted a, [rif40].[t_rif40_geolevels] b, [INFORMATION_SCHEMA].[COLUMNS] c
	WHERE a.geolevel_name=b.geolevel_name 
	AND c.table_name=b.covariate_table
	AND c.column_name=a.covariate_name



	EXEC [rif40].[rif_log] 'DEBUG1', '[rif40].[tr_covariates_check]', '<T_RIF40_GEOLEVELS.COVARIATE_TABLE>.<COVARIATE_NAME> column exists.';


--------------------------------
-- MIN value is greater than MAX
--------------------------------
DECLARE @min NVARCHAR(MAX)= 
( 
 SELECT cast(min as varchar(20)) , ':' ,covariate_name + '  '
 FROM   inserted  
 WHERE   min>=max 
 FOR XML PATH('') 
);
BEGIN TRY
IF @min IS NOT NULL 
	BEGIN 
		RAISERROR(50010,16,1,@min); 
		rollback 
	END;
END TRY
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
-----------------------------------
-- Type is 1 and MAX is not INT
-----------------------------------
DECLARE @type NVARCHAR(MAX)= --- NEED TO TEST THIS : type field has acheck constraint
( 
 SELECT cast(max as varchar(20)), ':' ,covariate_name + '  '
 FROM   inserted  
 WHERE   type=1 and round(max,0)<>max 
 FOR XML PATH('') 
); 
BEGIN TRY
IF @type IS NOT NULL 
	BEGIN 
		raiserror(50011,16,1,@type); 
		rollback
	END;
END TRY
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
-----------------------------------
-- Type is 1 and MIN is not INT
-----------------------------------	
DECLARE @type2 NVARCHAR(MAX)= --- NEED TO TEST THIS : type field has acheck constraint
( 
 SELECT cast(min as varchar(20)), ':' ,covariate_name + '  '
 FROM   inserted  
 WHERE   type=1 and round(min,0)<>min -- DID IT WORK ?
 FOR XML PATH('') 
); 
BEGIN TRY
IF @type2 IS NOT NULL 
	BEGIN 
		raiserror(50012,16,1,@type2); 
		rollback
	END;
END TRY
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
-----------------------------------
-- Type is 1 and MIN <0
-----------------------------------
DECLARE @type3 NVARCHAR(MAX)= --- NEED TO TEST THIS : type field has a check constraint
( 
 SELECT min ,geolevel_name, ':' ,covariate_name + '  '
 FROM   inserted  
 WHERE   type=1 and min<0 
 FOR XML PATH('') 
); 
BEGIN TRY
IF @type3 IS NOT NULL 
	BEGIN 
		raiserror(50013,16,1,@type3); 
		rollback
	END;
END TRY
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 


-------------------------------------------------------------------------------------
-- end of trigger code 
-------------------------------------------------------------------------------------
