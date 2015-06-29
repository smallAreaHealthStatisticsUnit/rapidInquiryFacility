/*
Required roles: 
rif_manager
rif_user

Optional:
rif_student
rif_no_suppression (?)
*/
USE [sahsuland_dev];

IF DATABASE_PRINCIPAL_ID('rif_manager') IS NULL
	CREATE ROLE [rif_manager];
	
IF DATABASE_PRINCIPAL_ID('rif_user') IS NULL
	CREATE ROLE [rif_user];
	
IF DATABASE_PRINCIPAL_ID('rif_student') IS NULL
	CREATE ROLE [rif_student];
	
IF DATABASE_PRINCIPAL_ID('rif_no_suppression') IS NULL
	CREATE ROLE [rif_no_suppression];




