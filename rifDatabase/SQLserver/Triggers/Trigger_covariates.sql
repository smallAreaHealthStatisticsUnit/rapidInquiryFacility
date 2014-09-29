


insert into [dbo].[RIF40_COVARIATES]
(GEOGRAPHY,	GEOLEVEL_NAME,	COVARIATE_NAME,	MIN,	MAX,	TYPE)
values	('EW01'	,'LADUA2001',	'test88',	6.60,	4.600,	1),
	    ('EW01'	,'LADUA2001',	'testing4',	1.000,	4.400,	1)
	

select * 
from [RIF40_COVARIATES]

update [dbo].[RIF40_COVARIATES]
set min=9 where COVARIATE_NAME='test9'


delete [dbo].[RIF40_COVARIATES] 
where COVARIATE_NAME like 'test%'

select * from rif40_ErrorLog

-----------------------------------------
--trigger_covariate
------------------------------------------
--Check <T_RIF40_GEOLEVELS.COVARIATE_TABLE>.<COVARIATE_NAME> column exists.
--Check - min < max, max/min precison is appropriate to type --NEED TO TEST THIS
-- eror msgs -- 50010 - 50014



-------------------------------------
-- start of trigger code
-------------------------------------
alter TRIGGER tr_covariates_check
on [dbo].[RIF40_COVARIATES]
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
		EXEC [ErrorLog_proc]
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
		EXEC [ErrorLog_proc]
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
		EXEC [ErrorLog_proc]
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
		EXEC [ErrorLog_proc]
END CATCH 
---------------------------------------------------------------------------------
--Check <T_RIF40_GEOLEVELS.COVARIATE_TABLE>.<COVARIATE_NAME> column exists.
---------------------------------------------------------------------------------

DECLARE @covar_name NVARCHAR(MAX)= 
( 
 SELECT covariate_name + ' , '
 FROM   inserted  
 WHERE  covariate_name in (select covariate_name from T_RIF40_GEOLEVELS)
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
		EXEC [ErrorLog_proc]
END CATCH 
END

-------------------------------------------------------------------------------------
-- end of trigger code 
-------------------------------------------------------------------------------------



----------------------------------
-- register custome error msgs
----------------------------------

USE master;
GO

EXEC sp_addmessage 50010, 16, 
   N'Table_name: [dbo].[RIF40_COVARIATES] , These MIN values are greater than max:(%s)';
GO


EXEC sp_addmessage 50011, 16, 
   N'Table_name: [dbo].[RIF40_COVARIATES] , type = 1 (integer score) and MAX is not an integer:(%s)';
GO

EXEC sp_addmessage 50012, 16, 
   N'Table_name: [dbo].[RIF40_COVARIATES] , type = 1 (integer score) and MIN is not an integer:(%s)';
GO

EXEC sp_addmessage 50013, 16, 
   N'Table_name: [dbo].[RIF40_COVARIATES] , type = 1 (integer score) and min <0:(%s)';
GO

EXEC sp_addmessage 50014, 16, 
   N'Table_name: [dbo].[RIF40_COVARIATES] , Could not Locate covariate name in T_RIF40_GEOLEVELS:(%s)';
GO



-------------------------------------------------------------------------------------------------
---\\tsclient\S\Projects\SAHSU\RIF_SQL_SERVER
---\\tsclient\S\Projects\SAHSU\RIF_SQL_SERVER\test_db.bak


select * FROM SYS.messages WHERE message_id>50000

EXEC sp_dropmessage 50005;



