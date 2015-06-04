/*
Extra Custom error messages for functions in rif40.

I am still uncertain whether global system-wise customized error messages are a good idea.
*/

EXEC sp_addmessage 51001, 16, 
   N'Function: [rif40].[rif_is_object_resolveable]: User %s must be rif40 or have rif_user or rif_manager role';
GO


EXEC sp_addmessage 51002, 16,
	N'Table name: RIF40_VERSION DELETE disallowed';
GO
EXEC sp_addmessage 51003, 16,
	N'Table name: RIF40_VERSION INSERT disallowed';
GO
