USE [master]
GO
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'mdouglas')
CREATE USER [mdouglas] FOR LOGIN [mdouglas] WITH DEFAULT_SCHEMA=[dbo]
GO

CREATE USER [mdouglas]
GO

USE [sahsuland_dev]
GO
CREATE ROLE [rif_manager]
GO
CREATE ROLE [rif_user]
GO

ALTER SEVER ROLE [rif_manager] ADD MEMBER [margaretd]
GO

USE [sahsuland_dev]
GO
CREATE USER [rif40] FOR LOGIN [rif40] WITH DEFAULT_SCHEMA=[dbo]
GO
CREATE SCHEMA [rif40] AUTHORIZATION [rif40]
GO
CREATE SCHEMA [rif_data] AUTHORIZATION [rif40]
GO

