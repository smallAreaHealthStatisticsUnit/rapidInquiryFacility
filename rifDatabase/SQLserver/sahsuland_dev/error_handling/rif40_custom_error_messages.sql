/*
Custom error messages for functions in rif40.  Mostly used by the trigger functions.
*/

use master;

EXEC sp_addmessage 50020, 16, 
   N'Table_name: [rif40].[rif40_geographies] , These HIERARCHYTABLE table/s do not exist:(%s)';

EXEC sp_addmessage 50021, 16, 
   N'Table_name: [rif40].[rif40_geographies], These postal population table/s do not exist:(%s)';

EXEC sp_addmessage 50022, 16, 
   N'Table_name: [rif40].[rif40_geographies], These postal point columns do not exist:(%s)';

EXEC sp_addmessage 50023, 16, 
   N'Table_name: [rif40].[rif40_geographies] , MALES column does not exist in these postal_population_tables:(%s)';

EXEC sp_addmessage 50024, 16, 
   N'Table_name: [rif40].[rif40_geographies] , FEMALES column does not exist in these postal_population_tables:(%s)';

EXEC sp_addmessage 50025, 16, 
   N'Table_name: [rif40].[rif40_geographies] , TOTAL column does not exist in these postal_population_tables:(%s)';

EXEC sp_addmessage 50026, 16, 
   N'Table_name: [rif40].[rif40_geographies] , XCOORDINATE column does not exist in these postal_population_tables:(%s)';

EXEC sp_addmessage 50027, 16, 
   N'Table_name: [rif40].[rif40_geographies] , YCOORDINATE column does not exist in these postal_population_tables:(%s)';

EXEC sp_addmessage 50010, 16, 
   N'Table_name: [rif40].[rif40_covariates] , These MIN values are greater than max:(%s)';

EXEC sp_addmessage 50011, 16, 
   N'Table_name: [rif40].[rif40_covariates] , type = 1 (integer score) and MAX is not an integer:(%s)';

EXEC sp_addmessage 50012, 16, 
   N'Table_name: [rif40].[rif40_covariates] , type = 1 (integer score) and MIN is not an integer:(%s)';

EXEC sp_addmessage 50013, 16, 
   N'Table_name: [rif40].[rif40_covariates] , type = 1 (integer score) and min <0:(%s)';

EXEC sp_addmessage 50014, 16, 
   N'Table_name: [rif40].[rif40_covariates] , Could not Locate covariate name in T_RIF40_GEOLEVELS:(%s)';

EXEC sp_addmessage 50050, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Not a numerator table:(%s)';

EXEC sp_addmessage 50051, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Entred table name year start is before RIF40_TABLES year start:(%s)';

EXEC sp_addmessage 50052, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Entred table name year STOP is AFTER RIF40_TABLES year STOP:(%s)';

EXEC sp_addmessage 50053, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Entered  min age group before RIF40_TABLES min age group :(%s)';

EXEC sp_addmessage 50054, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] ,  No age group linkage :(%s)';

EXEC sp_addmessage 50055, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] ,  Entered min age group is after max age group:(%s)';

EXEC sp_addmessage 50056, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Entered  year start is after year stop:(%s)';

EXEC sp_addmessage 50057, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Sex field column does not exist in the numerator table  :(%s)';

EXEC sp_addmessage 50058, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Age group field column does not exist in the numerator table  :(%s)';

EXEC sp_addmessage 50059, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Age sex group field column does not exist in the numerator table  :(%s)';

EXEC sp_addmessage 50060, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Age group ID mismatched  :(%s)';

EXEC sp_addmessage 50075, 16, 
   N'Table_name: [rif40].[t_rif40_num_denom] , These numerator table/s do not exist:(%s)';

EXEC sp_addmessage 50076, 16, 
   N'Table_name: [rif40].[t_rif40_num_denom] , These are not numerator table/s :(%s)';

EXEC sp_addmessage 50077, 16, 
   N'Table_name: [rif40].[t_rif40_num_denom] , These denominator table/s do not exist :(%s)';

EXEC sp_addmessage 50078, 16, 
   N'Table_name: [rif40].[t_rif40_num_denom] , These are not denominator table/s :(%s)';

EXEC sp_addmessage 50071, 16, 
   N'Table_name:[rif40].[t_rif40_user_projects] , project ended on :(%s)';

EXEC sp_addmessage 50091, 16, 
   N'Table_name: [rif40].[t_rif40_inv_covariates] , Update not allowed';

EXEC sp_addmessage 50092, 16, 
   N'Table_name: [rif40].[t_rif40_inv_covariates], Min value is less than expected:(%s)';

EXEC sp_addmessage 50093, 16, 
   N'Table_name: [rif40].[t_rif40_inv_covariates], Max value is greater than expected:(%s)';

EXEC sp_addmessage 50094, 16, 
   N'Table_name: [rif40].[t_rif40_inv_covariates], type = 2 (continuous variable) is not currently supported for geolevel_name given:(%s)';

EXEC sp_addmessage 50095, 16, 
   N'Table_name: [rif40].[t_rif40_inv_covariates], type = 1 (integer score) and max is not an integer (%s)';

EXEC sp_addmessage 50096, 16, 
   N'Table_name: [rif40].[t_rif40_inv_covariates], type = 1 (integer score) and min is not an integer:(%s)';

EXEC sp_addmessage 50097, 16, 
   N'Table_name: [rif40].[t_rif40_inv_covariates], type = 1 (integer score) and min <0:(%s)';

EXEC sp_addmessage 50098, 16, 
   N'Table_name: [rif40].[t_rif40_inv_covariates], Study geolevel name not found in rif40_geolevels:(%s)';

EXEC sp_addmessage 51000, 16,
	N'User %s is not a rif_user or rif_manager';

EXEC sp_addmessage 51001, 16, 
   N'Function: [rif40].[rif_is_object_resolveable]: User %s must be rif40 or have rif_user or rif_manager role';

EXEC sp_addmessage 51002, 16,
	N'Table name: [rif40].[rif40_version] , DELETE disallowed';

EXEC sp_addmessage 51003, 16,
	N'Table name: [rif40].[rif40_version] , INSERT disallowed, rows %s';

EXEC sp_addmessage 51004, 16,
	N'Table name: [rif40].[rif40_version] , INSERT/UPDATE invalid data - missing version field';

EXEC sp_addmessage 51005, 16,
	N'Table name: [rif40].[t_rif40_user_projects] , INSERT invalid data - missing project or username fields (%s)';

EXEC sp_addmessage 51006, 16,
	N'Table name: [rif40].[rif40_study_shares] , UPDATE not allowed'
	
EXEC sp_addmessage 51007, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT invalid data - missing study_id, grantor, or grantee fields (%s)';

EXEC sp_addmessage 51008, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT invalid data - study_id cannot be found (%s)';

EXEC sp_addmessage 51009, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT failed - study_id not owned by grantor (%s)';

EXEC sp_addmessage 51010, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT failed - grantee_username does not exist in database (%s)';

EXEC sp_addmessage 51011, 16,
	N'Table name: [rif40].[rif40_study_shares] , INSERT failed - grantee_username is NOT a RIF_USER/RIF_MANAGER (%s)';

EXEC sp_addmessage 51012, 16,
	N'Table name: [rif40].[rif40_study_shares] , DELETE only allowed on own records or by RIF40_MANAGER in RIF40_STUDY_SHARES (%s)';

EXEC sp_addmessage 51013, 16,
	N'Table name: [rif40].[t_rif40_studies] , INSERT/UPDATE failed - new study username is not current user (%s)';

EXEC sp_addmessage 51014, 16,
	N'Table name: [rif40].[t_rif40_studies] , UPDATE failed - UPDATE not allowed on T_RIF40_STUDIES by user (%s)';

EXEC sp_addmessage 51015, 16,
	N'Table name: [rif40].[t_rif40_studies] , UPDATE failed - non IG UPDATE not allowed on T_RIF40_STUDIES by user: (%s)'

EXEC sp_addmessage 51016, 16,
	N'Table name: [rif40].[t_rif40_studies] , UPDATE failed - new study username is not same as original study username: (%s)';

EXEC sp_addmessage 51017, 16,
	N'Table name: [rif40].[t_rif40_studies] , DELETE failed - DELETE only allowed on own records in T_RIF40_STUDIES: (%s)';
	
EXEC sp_addmessage 51018, 16,
	N'Table name: [rif40].[t_rif40_studies] , Geolevel name not found ,studyid-comparison_geolevel_name-geography: %s';
	
EXEC sp_addmessage 51019, 16,
	N'Table name: [rif40].[t_rif40_studies] , Geolevel name not found ,studyid-study_geolevel_name-geography: %s';

EXEC sp_addmessage 51020, 16,
	N'Table name: [rif40].[t_rif40_studies] , direct standardisation table is not a direct denominator table: %s';

EXEC sp_addmessage 51021, 16,
	N'Table name: [rif40].[t_rif40_studies] , denominator table is not a valid denominator table: %s';

EXEC sp_addmessage 51022, 16,
	N'Table name: [rif40].[t_rif40_studies] , denominator has no age group linkage: %s';

EXEC sp_addmessage 51023, 16,
	N'Table name: [rif40].[t_rif40_studies] , study area geolevel id < comparision area [i.e study area a lower resolution than the comparison area]: %s';

EXEC sp_addmessage 51024, 16,
	N'Table name: [rif40].[t_rif40_studies] , study not suppressed, but user is a RIF_STUDENT: %s';
