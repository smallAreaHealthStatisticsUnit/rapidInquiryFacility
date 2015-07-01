/*
Need some test people
*/
CREATE LOGIN margaretd WITH PASSWORD='xxxxxx';

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'margaretd')
	CREATE USER [margaretd] FOR LOGIN [margaretd] WITH DEFAULT_SCHEMA=[dbo];

ALTER SEVER ROLE [rif_manager] ADD MEMBER [margaretd];