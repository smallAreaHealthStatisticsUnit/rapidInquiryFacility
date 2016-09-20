/*
Need some test people - Peter is the user environment username (admin in my case)

THIS SCRIPT MUST BE RUN AS ADMINSITRATOR
*/

USE [sahsuland_dev];

BEGIN
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'Peter')
    BEGIN
		CREATE LOGIN Peter WITH PASSWORD='Peter';
		CREATE USER [Peter] FOR LOGIN [Peter] WITH DEFAULT_SCHEMA=[dbo];
	END;
/*

THIS DOES NOT WORK IF THE USER EXISTS BUT HAS NOT HAD ACCESS GRANTED TO THE DATABASE
Use SQL Server Management Studio to do this until I work out the obscure SQL command

BEGIN
	ALTER SERVER ROLE [rif_manager] ADD MEMBER [Peter];
	ALTER SERVER ROLE [rif_user] ADD MEMBER [Peter];		
END;
 */	
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'peter')
	BEGIN
		EXEC('CREATE SCHEMA [peter] AUTHORIZATION [Peter]');
		ALTER USER [Peter] WITH DEFAULT_SCHEMA=[peter];
	END;

SELECT name, type_desc FROM sys.database_principals WHERE name = N'PH-LAPTOP\Peter';
SELECT * FROM sys.schemas WHERE name = N'peter';
SELECT DP1.name AS DatabaseRoleName, ISNULL(DP2.name, 'No members') AS DatabaseUserName   
  FROM sys.database_role_members AS DRM  
	RIGHT OUTER JOIN sys.database_principals AS DP1  
		ON DRM.role_principal_id = DP1.principal_id  
	LEFT OUTER JOIN sys.database_principals AS DP2  
		ON DRM.member_principal_id = DP2.principal_id  
 ORDER BY DP1.name;

END;
GO
