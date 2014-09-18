------------------------------------------------
--register custom errors  : geography table 
------------------------------------------------

EXEC sp_addmessage 50020, 16, 
   N'Table_name: [dbo].[RIF40_GEOGRAPHIES] , These HIERARCHYTABLE table/s do not exist:(%s)';
GO


EXEC sp_addmessage 50021, 16, 
   N'Table_name: [dbo].[RIF40_GEOGRAPHIES], These postal population table/s do not exist:(%s)';
GO

EXEC sp_addmessage 50022, 16, 
   N'Table_name: [dbo].[RIF40_GEOGRAPHIES], These postal point columns do not exist:(%s)';
GO

EXEC sp_addmessage 50023, 16, 
   N'Table_name: [dbo].[RIF40_GEOGRAPHIES] , MALES column does not exist in these postal_population_tables:(%s)';
GO

EXEC sp_addmessage 50024, 16, 
   N'Table_name: [dbo].[RIF40_GEOGRAPHIES] , FEMALES column does not exist in these postal_population_tables:(%s)';
GO

EXEC sp_addmessage 50025, 16, 
   N'Table_name: [dbo].[RIF40_GEOGRAPHIES] , TOTAL column does not exist in these postal_population_tables:(%s)';
GO

EXEC sp_addmessage 50026, 16, 
   N'Table_name: [dbo].[RIF40_GEOGRAPHIES] , XCOORDINATE column does not exist in these postal_population_tables:(%s)';
GO
EXEC sp_addmessage 50027, 16, 
   N'Table_name: [dbo].[RIF40_GEOGRAPHIES] , YCOORDINATE column does not exist in these postal_population_tables:(%s)';
GO


----------------------------------
-- Covariates table 
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



