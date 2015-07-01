/* 
Original custom error messages from RH, slightly edited.  Now all integrated into rif40_custom_error_messages.sql
*/

use master;

------------------------------------------------
--register custom errors  : geography table 
------------------------------------------------

EXEC sp_addmessage 50020, 16, 
   N'Table_name: [rif40].[RIF40_GEOGRAPHIES] , These HIERARCHYTABLE table/s do not exist:(%s)';
GO


EXEC sp_addmessage 50021, 16, 
   N'Table_name: [rif40].[RIF40_GEOGRAPHIES], These postal population table/s do not exist:(%s)';
GO

EXEC sp_addmessage 50022, 16, 
   N'Table_name: [rif40].[RIF40_GEOGRAPHIES], These postal point columns do not exist:(%s)';
GO

EXEC sp_addmessage 50023, 16, 
   N'Table_name: [rif40].[RIF40_GEOGRAPHIES] , MALES column does not exist in these postal_population_tables:(%s)';
GO

EXEC sp_addmessage 50024, 16, 
   N'Table_name: [rif40].[RIF40_GEOGRAPHIES] , FEMALES column does not exist in these postal_population_tables:(%s)';
GO

EXEC sp_addmessage 50025, 16, 
   N'Table_name: [rif40].[RIF40_GEOGRAPHIES] , TOTAL column does not exist in these postal_population_tables:(%s)';
GO

EXEC sp_addmessage 50026, 16, 
   N'Table_name: [rif40].[RIF40_GEOGRAPHIES] , XCOORDINATE column does not exist in these postal_population_tables:(%s)';
GO
EXEC sp_addmessage 50027, 16, 
   N'Table_name: [rif40].[RIF40_GEOGRAPHIES] , YCOORDINATE column does not exist in these postal_population_tables:(%s)';
GO


----------------------------------
-- Covariates table 
----------------------------------


EXEC sp_addmessage 50010, 16, 
   N'Table_name: [rif40].[RIF40_COVARIATES] , These MIN values are greater than max:(%s)';
GO


EXEC sp_addmessage 50011, 16, 
   N'Table_name: [rif40].[RIF40_COVARIATES] , type = 1 (integer score) and MAX is not an integer:(%s)';
GO

EXEC sp_addmessage 50012, 16, 
   N'Table_name: [rif40].[RIF40_COVARIATES] , type = 1 (integer score) and MIN is not an integer:(%s)';
GO

EXEC sp_addmessage 50013, 16, 
   N'Table_name: [rif40].[RIF40_COVARIATES] , type = 1 (integer score) and min <0:(%s)';
GO

EXEC sp_addmessage 50014, 16, 
   N'Table_name: [rif40].[RIF40_COVARIATES] , Could not Locate covariate name in T_RIF40_GEOLEVELS:(%s)';
GO

-------------------------------
--[T_RIF40_INVESTIGATIONS] 
-------------------------------

EXEC sp_addmessage 50050, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] , T_RIF40_INVESTIGATIONS : Not a numerator table:(%s)';
GO

EXEC sp_addmessage 50051, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] , Entred table name year start is before RIF40_TABLES year start:(%s)';
GO

EXEC sp_addmessage 50052, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] , Entred table name year STOP is AFTER RIF40_TABLES year STOP:(%s)';
GO

EXEC sp_addmessage 50053, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] , Entered  min age group before RIF40_TABLES min age group :(%s)';
GO

EXEC sp_addmessage 50054, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] ,  no age group linkage :(%s)';
GO

EXEC sp_addmessage 50055, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] ,  Entered min age group is after max age group:(%s)';
GO
EXEC sp_addmessage 50056, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] , Entered  year start is after year stop:(%s)';
GO

EXEC sp_addmessage 50057, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] , Sex field column does not exist in the numerator table  :(%s)';
GO

EXEC sp_addmessage 50058, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] , Age group field column does not exist in the numerator table  :(%s)';
GO

EXEC sp_addmessage 50059, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] , Age sex group field column does not exist in the numerator table  :(%s)';
GO

EXEC sp_addmessage 50060, 16, 
   N'Table_name: [rif40].[T_RIF40_INVESTIGATIONS] , Age group ID mismatched  :(%s)';



--------------------------------
--[T_RIF40_NUM_DENOM] 
--------------------------------

EXEC sp_addmessage 50075, 16, 
   N'Table_name: [rif40].[T_RIF40_NUM_DENOM] , These numerator table/s do not exist:(%s)';
GO

EXEC sp_addmessage 50076, 16, 
   N'Table_name: [rif40].[T_RIF40_NUM_DENOM] , These are not numerator table/s :(%s)';
GO

EXEC sp_addmessage 50077, 16, 
   N'Table_name: [rif40].[T_RIF40_NUM_DENOM] , These denominator table/s do not exist :(%s)';
GO

EXEC sp_addmessage 50078, 16, 
   N'Table_name: [rif40].[T_RIF40_NUM_DENOM] , These are not denominator table/s :(%s)';
GO


----------------------------
-- USER PROJECTS 
----------------------------

/*
--This should be logged and not treated as an error:		 
EXEC sp_addmessage 50070, 16, 
   N'Table_name:[rif40].[T_RIF40_USER_PROJECTS] , project has no end set:(%s)';
GO
*/

EXEC sp_addmessage 50071, 16, 
   N'Table_name:[rif40].[T_RIF40_USER_PROJECTS] , project ended on :(%s)';
GO
	

----------------------------
-- [T_RIF40_INV_COVARIATES]
-----------------------------
EXEC sp_addmessage 50091, 16, 
   N'Table_name: [rif40].[T_RIF40_INV_COVARIATES] , Update not allowed';
GO

EXEC sp_addmessage 50092, 16, 
   N'Table_name: [rif40].[T_RIF40_INV_COVARIATES], MIn value is less than expected:(%s)';
GO
EXEC sp_addmessage 50093, 16, 
   N'Table_name: [rif40].[T_RIF40_INV_COVARIATES], MAX value is greater than expected:(%s)';
GO

EXEC sp_addmessage 50094, 16, 
   N'Table_name: [rif40].[T_RIF40_INV_COVARIATES], type = 2 (continuous variable) is not currently supported for geolevel_name given:(%s)';
GO

EXEC sp_addmessage 50095, 16, 
   N'Table_name: [rif40].[T_RIF40_INV_COVARIATES], type = 1 (integer score) and max is not an integer (%s)';
GO
EXEC sp_addmessage 50096, 16, 
   N'Table_name: [rif40].[T_RIF40_INV_COVARIATES], type = 1 (integer score) and min is not an integer:(%s)';
GO
EXEC sp_addmessage 50097, 16, 
   N'Table_name: [rif40].[T_RIF40_INV_COVARIATES], type = 1 (integer score) and min <0:(%s)';
GO
EXEC sp_addmessage 50098, 16, 
   N'Table_name: [rif40].[T_RIF40_INV_COVARIATES], Study geolevel name not found in rif40_geolevels:(%s)';
GO







