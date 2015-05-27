/*
Extra Custom error messages for functions in rif40.

I am still ambivalent over whether global system-wise customized error messages are a good idea.
*/

EXEC sp_addmessage 51001, 16, 
   N'Function: [rif40].[rif_is_object_resolveable]: User %s must be rif40 or have rif_user or rif_manager role';
GO

