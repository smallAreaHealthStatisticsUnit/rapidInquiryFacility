/*
Creation of sahsuland_dev database

THIS SCRIPT MUST BE RUN AS ADMINSITRATOR
*/
USE master;
IF EXISTS(SELECT * FROM sys.sysdatabases where name='sahsuland_dev')
	DROP DATABASE sahsuland_dev;
GO
	
CREATE DATABASE sahsuland_dev;
GO

IF EXISTS (SELECT * FROM sys.sql_logins WHERE name = N'rif40')
DROP LOGIN rif40;
GO

--
-- This will only work if the user has no objects
--
IF EXISTS (SELECT name FROM sys.server_principals WHERE name = N'rif40')
DROP USER rif40;
GO

USE [sahsuland_dev];

SELECT name FROM sys.server_principals;
GO

CREATE LOGIN rif40 WITH PASSWORD='rif40';
GO

CREATE USER [rif40] FOR LOGIN [rif40] WITH DEFAULT_SCHEMA=[dbo];
GO
	
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif40')
EXEC('CREATE SCHEMA [rif40] AUTHORIZATION [rif40]');
GO

IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif_data')
EXEC('CREATE SCHEMA [rif_data] AUTHORIZATION [rif40]');
GO

ALTER USER [rif40] WITH DEFAULT_SCHEMA=[rif40];
GO
