/*
Extra Custom error messages for functions in rif40.
*/

EXEC sp_addmessage 51000, 16,
	N'User %s is not a rif_user or rif_manager';
GO

EXEC sp_addmessage 51001, 16, 
   N'Function: [rif40].[rif_is_object_resolveable]: User %s must be rif40 or have rif_user or rif_manager role';
GO

EXEC sp_addmessage 51002, 16,
	N'Table name: [rif40].[rif40_version] , DELETE disallowed';
GO
EXEC sp_addmessage 51003, 16,
	N'Table name: [rif40].[rif40_version] , INSERT disallowed, rows %s';
GO
EXEC sp_addmessage 51004, 16,
	N'Table name: [rif40].[rif40_version] , INSERT/UPDATE invalid data - missing version field';
GO

EXEC sp_addmessage 51005, 16,
	N'Table name: [rif40].[t_rif40_user_projects] , INSERT invalid data - missing project or username fields (%s)';
GO


