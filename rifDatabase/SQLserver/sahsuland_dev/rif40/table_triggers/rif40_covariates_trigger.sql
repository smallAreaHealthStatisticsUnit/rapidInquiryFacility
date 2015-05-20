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
---------------------------------------------------------------------------------
--Check <T_RIF40_GEOLEVELS.COVARIATE_TABLE>.<COVARIATE_NAME> column exists.
---------------------------------------------------------------------------------

DECLARE @covar_name NVARCHAR(MAX)= 
( 
 SELECT covariate_name + ' , '
 FROM   inserted  
 WHERE  covariate_name in (select covariate_name from [rif40].[t_rif40_geolevels])
 FOR XML PATH('') 
); 
BEGIN TRY
IF @covar_name IS NOT NULL 
	BEGIN 
		raiserror(50014,16,1,@covar_name); 
		rollback 
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
END

-------------------------------------------------------------------------------------
-- end of trigger code 
-------------------------------------------------------------------------------------
