/*
Need some test people - $(USERNAME) is the user environment username (admin in my case)

THIS SCRIPT MUST BE RUN AS ADMINSITRATOR
*/

USE [sahsuland_dev];

BEGIN
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'$(USERNAME)')
    BEGIN
		CREATE LOGIN $(USERNAME) WITH PASSWORD='$(USERNAME)';
		CREATE USER [$(USERNAME)] FOR LOGIN [$(USERNAME)] WITH DEFAULT_SCHEMA=[dbo];
		ALTER SERVER ROLE [rif_manager] ADD MEMBER [$(USERNAME)];
		ALTER SERVER ROLE [rif_user] ADD MEMBER [$(USERNAME)];		
	END;
	
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'$(USERNAME)')
	BEGIN
		EXEC('CREATE SCHEMA [$(USERNAME)] AUTHORIZATION [$(USERNAME)]');
		ALTER USER [$(USERNAME)] WITH DEFAULT_SCHEMA=[$(USERNAME)];
	END;

SELECT name, type_desc FROM sys.database_principals WHERE name = N'$(USERNAME)';
SELECT * FROM sys.schemas WHERE name = N'$(USERNAME)';
SELECT DP1.name AS DatabaseRoleName, ISNULL(DP2.name, 'No members') AS DatabaseUserName   
  FROM sys.database_role_members AS DRM  
	RIGHT OUTER JOIN sys.database_principals AS DP1  
		ON DRM.role_principal_id = DP1.principal_id  
	LEFT OUTER JOIN sys.database_principals AS DP2  
		ON DRM.member_principal_id = DP2.principal_id  
 ORDER BY DP1.name;

END;
GO
