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

EXEC sp_addmessage 51006, 16,
	N'Table name: [rif40].[rif40_study_shares] , UPDATE not allowed'
GO	
EXEC sp_addmessage 51007, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT invalid data - missing study_id, grantor, or grantee fields (%s)';
GO

EXEC sp_addmessage 51008, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT invalid data - study_id cannot be found (%s)';
GO

EXEC sp_addmessage 51009, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT failed - study_id not owned by grantor (%s)';
GO

EXEC sp_addmessage 51010, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT failed - grantee_username does not exist in database (%s)';
GO

EXEC sp_addmessage 51011, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT failed - grantee_username is NOT a RIF_USER/RIF_MANAGER (%s)';
GO

EXEC sp_addmessage 51012, 16,
	N'Table name: [rif40].[rif40_study_shares] , DELETE only allowed on own records or by RIF40_MANAGER in RIF40_STUDY_SHARES (%s)';
GO

EXEC sp_addmessage 51013, 16,
	N'Table name: [rif40].[tr_studies_checks] , INSERT/UPDATE failed - new study username is not current user (%s)';
GO

EXEC sp_addmessage 51014, 16,
	N'Table name: [rif40].[tr_studies_checks] , UPDATE failed - UPDATE not allowed on T_RIF40_STUDIES by user (%s)';
GO

EXEC sp_addmessage 51015, 16,
	N'Table name: [rif40].[tr_studies_checks] , UPDATE failed - non IG UPDATE not allowed on T_RIF40_STUDIES by user: (%s)'
GO

EXEC sp_addmessage 51016, 16,
	N'Table name: [rif40].[tr_studies_checks] , UPDATE failed - new study username is not same as original study username: (%s)';
GO	

EXEC sp_addmessage 51017, 16,
	N'Table name: [rif40].[tr_studies_checks] , DELETE failed - DELETE only allowed on own records in T_RIF40_STUDIES: (%s)';
GO	

EXEC sp_addmessage 51018, 16,
	N'Table name: [rif40].[tr_studies_checks] , Geolevel name not found ,studyid-comparison_geolevel_name-geography: %s';
GO	

EXEC sp_addmessage 51019, 16,
	N'Table name: [rif40].[tr_studies_checks] , Geolevel name not found ,studyid-study_geolevel_name-geography: %s';

EXEC sp_addmessage 51020, 16,
	N'Table name: [rif40].[tr_studies_checks] , direct standardisation table is not a direct denominator table: %s';

EXEC sp_addmessage 51021, 16,
	N'Table name: [rif40].[tr_studies_checks] , denominator table is not a valid denominator table: %s';
