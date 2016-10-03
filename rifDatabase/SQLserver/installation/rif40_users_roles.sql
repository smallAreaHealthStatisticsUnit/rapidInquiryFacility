/*

THIS SCRIPT MUST BE RUN AS ADMINSITRATOR

Required roles: 
rif_manager
rif_user

Optional:
rif_student
rif_no_suppression (?)

Testing:
notarifuser
*/
USE [sahsuland_dev];

IF DATABASE_PRINCIPAL_ID('rif_manager') IS NULL
	CREATE ROLE [rif_manager];
GO

	
IF DATABASE_PRINCIPAL_ID('rif_user') IS NULL
	CREATE ROLE [rif_user];
GO
	
IF DATABASE_PRINCIPAL_ID('rif_student') IS NULL
	CREATE ROLE [rif_student];
GO
	
IF DATABASE_PRINCIPAL_ID('rif_no_suppression') IS NULL
	CREATE ROLE [rif_no_suppression];
GO

IF DATABASE_PRINCIPAL_ID('notarifuser') IS NULL
	CREATE ROLE [notarifuser];
GO
	
SELECT name, type_desc FROM sys.database_principals;
GO