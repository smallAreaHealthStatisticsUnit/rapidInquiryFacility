/*
Custom error messages for functions in rif40.  Mostly used by the trigger functions.
*/

--clean up old versions:
DECLARE @error_curs CURSOR, @m_id int;
SET @error_curs = CURSOR FOR
	select message_id
	from sys.messages
	where message_id >= 50000 and text like '%rif40%'; 
OPEN @error_curs;
FETCH @error_curs INTO @m_id;
		
WHILE @@FETCH_STATUS = 0
BEGIN
	EXEC sp_dropmessage @m_id;
	FETCH @error_curs INTO @m_id;	
END;
CLOSE @error_curs;
DEALLOCATE @error_curs;
PRINT 'rif40_custom_error_messages.sql: All RIF-related custom error messages have been removed.';
GO

--all new error messages.  They must have an id of > 50000 and the text contains 'rif40'
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
   
EXEC sp_addmessage 50028, 16, 
   N'Table_name: [rif40].[rif40_geographies] , Default comparision area column not found in T_RIF40_GEOLEVELS: %s';

EXEC sp_addmessage 50029, 16, 
   N'Table_name: [rif40].[rif40_geographies] , Default study area column not found in T_RIF40_GEOLEVELS: %s';
   
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


EXEC sp_addmessage 50051, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Entered table name year start is before RIF40_TABLES year start:(%s)';

EXEC sp_addmessage 50052, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Entered table name year STOP is AFTER RIF40_TABLES year STOP:(%s)';

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
   N'Table_name: [rif40].[t_rif40_inv_covariates], Study geolevel name not found in t_rif40_geolevels: %s';

EXEC sp_addmessage 51000, 16,
	N'[rif40] User %s is not a rif_user or rif_manager';

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
EXEC sp_addmessage 51025, 16,
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study may NOT be extracted, study geolevel is not restricted but the user is a RIF_STUDENT: %s';
EXEC sp_addmessage 51026, 16,
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study may NOT be extracted, study geolevel is restricted for user Requires authorisation by a RIF_MANAGER.  %s';
EXEC sp_addmessage 51027, 16,
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study may not be extracted, user is a RIF_STUDENT: %s';
EXEC sp_addmessage 51028, 16,
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study may not be extracted, modifying user is NOT a RIF_MANAGER: %s';
EXEC sp_addmessage 51029, 16,
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study extract table cannot be accessed: %s';
	
EXEC sp_addmessage 51030, 16,
	N'Table name: [rif40].[rif40_covariates] , invalid new values: %s';
EXEC sp_addmessage 51031, 16,
	N'Table name: [rif40].[rif40_covariates] , No covariate table in [rif40].[t_rif40_geolevels] for geolevel: %s';
EXEC sp_addmessage 51032, 16,
	N'Table name: [rif40].[rif40_covariates] , <T_RIF40_GEOLEVELS.COVARIATE_TABLE>.<COVARIATE_NAME> column does not exist: %s';
EXEC sp_addmessage 51033, 16,
	N'Table name: [rif40].[rif40_covariates] , min >= max for covariate : %s';
EXEC sp_addmessage 51034, 16,
	N'Table name: [rif40].[rif40_covariates] , RIF40_COVARIATES type = 1 (integer score) and max is not an integer: %s';
EXEC sp_addmessage 51035, 16,
	N'Table name: [rif40].[rif40_covariates] , RIF40_COVARIATES type = 1 (integer score) and min is not an integer: %s';
EXEC sp_addmessage 51036, 16,
	N'Table name: [rif40].[rif40_covariates] , RIF40_COVARIATES type = 1 (integer score) and min <0: %s';
EXEC sp_addmessage 51037, 16,
	N'Table name: [rif40].[rif40_tables] , RIF40_TABLES TOTAL_FIELD column not found in table: %s';
EXEC sp_addmessage 51038, 16,
	N'Table name: [rif40].[rif40_tables] , RIF40_TABLES direct standardised denominator TABLE_NAME not found: %s';

EXEC sp_addmessage 51039, 16,
	N'Table name: [rif40].[rif40_table_outcomes] , RIF40_TABLE_OUTCOMES outcome group numerator not found in RIF tables: %s';
EXEC sp_addmessage 51040, 16,
	N'Table name: [rif40].[rif40_table_outcomes] , RIF40_TABLE_OUTCOMES outcome group current_version_start_year not between start/stop years in rif40_tables: %s';
EXEC sp_addmessage 51041, 16,
	N'Table name: [rif40].[rif40_table_outcomes] , RIF40_TABLE_OUTCOMES outcome group numerator table is not a numerator: %s';

EXEC sp_addmessage 51042, 16,
	N'Table name: [rif40].[rif40_error_messages] , RIF40_ERROR_MESSAGES table_name not found: %s';

EXEC sp_addmessage 51043, 16,
	N'Table name: [rif40].[t_rif40_comparison_areas], T_RIF40_COMPARISON_AREAS no study_id found';
	
EXEC sp_addmessage 51044, 16,
	N'Table name: [rif40].[t_rif40_comparison_areas], T_RIF40_COMPARISON_AREAS comparison_study_area, hierarchy values not defined for study_id: %s';
EXEC sp_addmessage 51045, 16,
	N'Table name: [rif40].[t_rif40_comparison_areas], T_RIF40_COMPARISON_AREAS study_id found areas not in hierarchy table: %s';

EXEC sp_addmessage 51046, 16,
	N'Table name: [rif40].[t_rif40_contextual_stats], UPDATE not allowed on T_RIF40_CONTEXTUAL_STATS';
EXEC sp_addmessage 51047, 16,
	N'Table name: [rif40].[t_rif40_contextual_stats], T_RIF40_CONTEXTUAL_STATS study: new username is not current USER: %s';
EXEC sp_addmessage 51048, 16,
	N'Table name: [rif40].[t_rif40_contextual_stats], DELETE only allowed on own records in T_RIF40_CONTEXTUAL_STATS, record owned by: %s';
	
EXEC sp_addmessage 51050, 16,
	N'Table name: [rif40].[rif40_db_name_check] , Invalid Oracle/Postgres/SQL Server name contains NON alphanumeric characters: %s';
EXEC sp_addmessage 51051, 16,
	N'Table name: [rif40].[rif40_db_name_check] , Invalid Database name exceeds maximum length: %s';
EXEC sp_addmessage 51052, 16,
	N'Table name: [rif40].[rif40_db_name_check] , Invalid Database name, first character must be a letter: %s';
	
EXEC sp_addmessage 51053, 16,
	N'Table name: [rif40].[t_rif40_geolevels], T_RIF40_GEOLEVELS COVARIATE_TABLE not found: %s';
EXEC sp_addmessage 51054, 16,
	N'Table name: [rif40].[t_rif40_geolevels], T_RIF40_GEOLEVELS COVARIATE_TABLE column not found for geolevel_name: %s';
EXEC sp_addmessage 51055, 16,
	N'Table name: [rif40].[t_rif40_geolevels], T_RIF40_GEOLEVELS COVARIATE_TABLE missing YEAR column : %s';
EXEC sp_addmessage 51056, 16,
	N'Table name: [rif40].[t_rif40_geolevels], T_RIF40_GEOLEVELS LOOKUP_TABLE not found for geolevel_name: %s';
EXEC sp_addmessage 51057, 16,
	N'Table name: [rif40].[t_rif40_geolevels], T_RIF40_GEOLEVELS LOOKUP_TABLE column not found for geolevel_name: %s';
EXEC sp_addmessage 51058, 16,
	N'Table name: [rif40].[t_rif40_geolevels], T_RIF40_GEOLEVELS LOOKUP_TABLE LOOKUP_DESC_COLUMN column not found for geolevel_name : %s';
EXEC sp_addmessage 51059, 16,
	N'Table name: [rif40].[t_rif40_geolevels], T_RIF40_GEOLEVELS CENTROIDXCOORDINATE_COLUMN column not found for geolevel_name: %s';
EXEC sp_addmessage 51060, 16,
	N'Table name: [rif40].[t_rif40_geolevels], T_RIF40_GEOLEVELS CENTROIDYCOORDINATE_COLUMN column not found for geolevel_name: %s';
EXEC sp_addmessage 51061, 16,
	N'Table name: [rif40].[t_rif40_geolevels], RIF40_GEOGRAPHIES HIERARCHYTABLE not found for geolevel_name: %s';
EXEC sp_addmessage 51062, 16,
	N'Table name: [rif40].[t_rif40_geolevels], RIF40_GEOGRAPHIES HIERARCHYTABLE geolevel column not found for geolevel_name: %s';
EXEC sp_addmessage 51063, 16,
	N'Table name: [rif40].[t_rif40_geolevels], RIF40_GEOGRAPHIES POSTAL_POPULATION_TABLE not found for geolevel_name: %s';
EXEC sp_addmessage 51064, 16,
	N'Table name: [rif40].[t_rif40_geolevels], RIF40_GEOGRAPHIES POSTAL_POPULATION_TABLE geolevel column not found for geolevel_name: %s';

EXEC sp_addmessage 51065, 16,
	N'Table name: [rif40].[t_rif40_inv_conditions], T_RIF40_INV_CONDITIONS insert/update failed, new username is not current user: %s';
EXEC sp_addmessage 51066, 16,
	N'Table name: [rif40].[t_rif40_inv_conditions], UPDATE not allowed on T_RIF40_INV_CONDITIONS';
EXEC sp_addmessage 51067, 16,
	N'Table name: [rif40].[t_rif40_inv_conditions], DELETE only allowed on own records in T_RIF40_INV_CONDITIONS: %s';

EXEC sp_addmessage 51068, 16,
	N'Table name: [rif40].[t_rif40_inv_covariates], T_RIF40_INV_COVARIATES insert/update failed, new username is not current user: %s';
EXEC sp_addmessage 51069, 16,
	N'Table name: [rif40].[t_rif40_inv_covariates], DELETE only allowed on own records in T_RIF40_INV_COVARIATES: %s';
EXEC sp_addmessage 51070, 16,
	N'Table name: [rif40].[t_rif40_inv_covariates], T_RIF40_INV_COVARIATES study, investigation, covariate, study area geolevel name not found in T_RIF40_STUDIES for current study: %s';
EXEC sp_addmessage 51071, 16,
	N'Table name: [rif40].[t_rif40_inv_covariates], T_RIF40_INV_COVARIATES covariate column in covariate table cannot be accessed: %s';
EXEC sp_addmessage 51072, 16,
	N'Table name: [rif40].[t_rif40_inv_covariates], T_RIF40_INV_COVARIATES YEAR column in covariate table cannot be accessed: %s';	
EXEC sp_addmessage 51073, 16,
	N'Table name: [rif40].[t_rif40_inv_covariates], T_RIF40_INV_COVARIATES geolevel column in covariate table cannot be accessed: %s';		
EXEC sp_addmessage 51074, 16,
	N'Table name: [rif40].[t_rif40_inv_covariates], T_RIF40_INV_COVARIATES problem checking score: %s';
EXEC sp_addmessage 51075, 16,
	N'Table name: [rif40].[t_rif40_inv_covariates], T_RIF40_INV_COVARIATES covariate table''s covariate column does not have min value: %s';
EXEC sp_addmessage 51076, 16,
	N'Table name: [rif40].[t_rif40_inv_covariates], T_RIF40_INV_COVARIATES covariate table''s covariate column does not have max value: %s';
	
EXEC sp_addmessage 51077, 16,
	N'Table name: [rif40].[t_rif40_investigations] , T_RIF40_INVESTIGATIONS INSERT/UPDATE failed - new study username is not current user: %s';
EXEC sp_addmessage 51078, 16,
	N'Table name: [rif40].[t_rif40_investigations] , T_RIF40_INVESTIGATIONS UPDATE/DELETE only allowed on own records: %s';
EXEC sp_addmessage 51079, 16,
	N'Table name: [rif40].[t_rif40_investigations] , UPDATE only allowed for T_RIF40_INVESTIGATIONS.INVESTIGATION_STATE: %s';
EXEC sp_addmessage 51080, 16,
	N'Table name: [rif40].[t_rif40_investigations] , T_RIF40_INVESTIGATIONS numer_tab is not a numerator table: %s';
EXEC sp_addmessage 51081, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Entered  max age group after RIF40_TABLES max age group : %s';
EXEC sp_addmessage 51082, 16, 
   N'Table_name: [rif40].[t_rif40_investigations] , Total field column does not exist in the numerator table : %s';

EXEC sp_addmessage 51083, 16, 
   N'Table_name: [rif40].[t_rif40_results] , DELETE only allowed on own records in T_RIF40_RESULTS: %s';
EXEC sp_addmessage 51084, 16, 
   N'Table_name: [rif40].[t_rif40_results] , T_RIF40_RESULTS insert/update failed, new username is not current user: %s';
EXEC sp_addmessage 51085, 16, 
   N'Table_name: [rif40].[t_rif40_results] , UPDATE not allowed on T_RIF40_RESULTS';
EXEC sp_addmessage 51086, 16, 
   N'Table_name: [rif40].[t_rif40_results] , Expecting NULL relative_risk with direct standardised results: %s';
   
EXEC sp_addmessage 51087, 16, 
   N'Table_name: [rif40].[t_rif40_study_areas] , DELETE only allowed on own records in T_RIF40_STUDY_AREAS: %s';
EXEC sp_addmessage 51088, 16, 
   N'Table_name: [rif40].[t_rif40_study_areas] , T_RIF40_STUDY_AREAS insert/update failed, new username is not current user: %s';
EXEC sp_addmessage 51089, 16, 
   N'Table_name: [rif40].[t_rif40_study_areas] , UPDATE not allowed on T_RIF40_STUDY_AREAS';
EXEC sp_addmessage 51090, 16, 
   N'Table_name: [rif40].[t_rif40_study_areas] , T_RIF40_STUDY_AREAS no study found for study_id: %s';
EXEC sp_addmessage 51091, 16, 
   N'Table_name: [rif40].[t_rif40_study_areas] , T_RIF40_STUDY_AREAS area_ids not found in study_geolevel_name in hierarchy table: %s';

EXEC sp_addmessage 51092, 16, 
   N'Table_name: [rif40].[t_rif40_study_sql] , DELETE only allowed on own records in T_RIF40_STUDY_SQL: %s';
EXEC sp_addmessage 51093, 16, 
   N'Table_name: [rif40].[t_rif40_study_sql] , T_RIF40_STUDY_SQL insert/update failed, new username is not current user: %s';
EXEC sp_addmessage 51094, 16, 
   N'Table_name: [rif40].[t_rif40_study_sql] , UPDATE not allowed on T_RIF40_STUDY_SQL';
   
EXEC sp_addmessage 51095, 16, 
   N'Table_name: [rif40].[t_rif40_study_sql_log] , DELETE only allowed on own records in T_RIF40_STUDY_SQL_LOG: %s';
EXEC sp_addmessage 51096, 16, 
   N'Table_name: [rif40].[t_rif40_study_sql_log] , T_RIF40_STUDY_SQL_LOG insert/update failed, new username is not current user: %s';
EXEC sp_addmessage 51097, 16, 
   N'Table_name: [rif40].[t_rif40_study_sql_log] , UPDATE not allowed on T_RIF40_STUDY_SQL_LOG';

EXEC sp_addmessage 51098, 16, 
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study map table cannot be accessed: %s';
EXEC sp_addmessage 51099, 16, 
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study denominator RIF40_TABLES total field column not found: %s';
EXEC sp_addmessage 51100, 16, 
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study denominator RIF40_TABLES sex field column not found: %s';
EXEC sp_addmessage 51101, 16, 
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study denominator RIF40_TABLES age group field column not found: %s';
EXEC sp_addmessage 51102, 16, 
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study denominator RIF40_TABLES age sex field column not found: %s';
EXEC sp_addmessage 51103, 16, 
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study direct standardisation RIF40_TABLES total field column not found: %s';
EXEC sp_addmessage 51104, 16, 
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study direct standardisation RIF40_TABLES sex field column not found: %s';	
EXEC sp_addmessage 51105, 16, 
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study direct standardisation RIF40_TABLES age group field column not found: %s';		
EXEC sp_addmessage 51106, 16, 
	N'Table name: [rif40].[t_rif40_studies] , T_RIF40_STUDIES study direct standardisation RIF40_TABLES age sex group field column not found: %s';		

EXEC sp_addmessage 51107, 16, 
	N'View name: [rif40].[rif40_comparison_areas] , Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51108, 16, 
	N'View name: [rif40].[rif40_comparison_areas] , Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51109, 16, 
	N'View name: [rif40].[rif40_comparison_areas] , Cannot DELETE: User is not the owner of the record: %s';

EXEC sp_addmessage 51110, 16, 
	N'View name: [rif40].[rif40_contextual_stats], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51111, 16, 
	N'View name: [rif40].[rif40_contextual_stats], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51112, 16, 
	N'View name: [rif40].[rif40_contextual_stats], Cannot DELETE: User is not the owner of the record: %s';

EXEC sp_addmessage 51113, 16, 
	N'View name: [rif40].[rif40_fdw_tables], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51114, 16, 
	N'View name: [rif40].[rif40_fdw_tables], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51115, 16, 
	N'View name: [rif40].[rif40_fdw_tables], Cannot DELETE: User is not the owner of the record: %s';
	
EXEC sp_addmessage 51116, 16, 
	N'View name: [rif40].[rif40_inv_conditions], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51117, 16, 
	N'View name: [rif40].[rif40_inv_conditions], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51118, 16, 
	N'View name: [rif40].[rif40_inv_conditions], Cannot DELETE: User is not the owner of the record: %s';

EXEC sp_addmessage 51119, 16, 
	N'View name: [rif40].[rif40_inv_covariates], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51120, 16, 
	N'View name: [rif40].[rif40_inv_covariates], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51121, 16, 
	N'View name: [rif40].[rif40_inv_covariates], Cannot DELETE: User is not the owner of the record: %s';	

EXEC sp_addmessage 51122, 16, 
	N'View name: [rif40].[rif40_investigations], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51123, 16, 
	N'View name: [rif40].[rif40_investigations], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51124, 16, 
	N'View name: [rif40].[rif40_investigations], Cannot DELETE: User is not the owner of the record: %s';	

EXEC sp_addmessage 51125, 16, 
	N'View name: [rif40].[rif40_parameters], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51126, 16, 
	N'View name: [rif40].[rif40_parameters], Cannot UPDATE: RIF40_PARAMETERS param_name: %s';
EXEC sp_addmessage 51127, 16, 
	N'View name: [rif40].[rif40_parameters], Cannot DELETE RIF40_PARAMETERS records';

EXEC sp_addmessage 51128, 16, 
	N'View name: [rif40].[rif40_results], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51129, 16, 
	N'View name: [rif40].[rif40_results], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51130, 16, 
	N'View name: [rif40].[rif40_results], Cannot DELETE: User is not the owner of the record: %s';	
	
EXEC sp_addmessage 51131, 16, 
	N'View name: [rif40].[rif40_studies], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51132, 16, 
	N'View name: [rif40].[rif40_studies], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51133, 16, 
	N'View name: [rif40].[rif40_studies], Cannot DELETE: User is not the owner of the record: %s';	

EXEC sp_addmessage 51134, 16, 
	N'View name: [rif40].[rif40_study_areas], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51135, 16, 
	N'View name: [rif40].[rif40_study_areas], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51136, 16, 
	N'View name: [rif40].[rif40_study_areas], Cannot DELETE: User is not the owner of the record: %s';	

EXEC sp_addmessage 51137, 16, 
	N'View name: [rif40].[rif40_study_sql], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51138, 16, 
	N'View name: [rif40].[rif40_study_sql], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51139, 16, 
	N'View name: [rif40].[rif40_study_sql], Cannot DELETE: User is not the owner of the record: %s';	
	
EXEC sp_addmessage 51140, 16, 
	N'View name: [rif40].[rif40_study_sql_log], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51141, 16, 
	N'View name: [rif40].[rif40_study_sql_log], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51142, 16, 
	N'View name: [rif40].[rif40_study_sql_log], Cannot DELETE: User is not the owner of the record: %s';	

EXEC sp_addmessage 51143, 16, 
	N'View name: [rif40].[rif40_user_projects], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: %s';
EXEC sp_addmessage 51144, 16, 
	N'View name: [rif40].[rif40_user_projects], Cannot UPDATE: User is not the owner of the record: %s';
EXEC sp_addmessage 51145, 16, 
	N'View name: [rif40].[rif40_user_projects], Cannot DELETE: User is not the owner of the record: %s';	

EXEC sp_addmessage 51146, 16,
	N'Table name: [rif40].[t_rif40_geolevels], Cannot DELETE from T_RIF40_GEOLEVELS';
EXEC sp_addmessage 51147, 16,
	N'Table name: [rif40].[rif40_tables], Cannot DELETE from RIF40_TABLES';	

EXEC sp_addmessage 51150, 16,
	N'Function: [rif40].[rif40_startup], User must be non-privileged: %s';	

EXEC sp_addmessage 51151, 16,
	N'Function: [rif40].[rif40_addorupdate_comment], Missing object or schema name and cannot create comment: %s';	

EXEC sp_addmessage 55200, 16,
	N'Function: [rif40].[rif40_run_study], Study ID %i not found.';	
EXEC sp_addmessage 55201, 16,
	N'Function: [rif40].[rif40_run_study], Study ID %i cannot be run, in state: %s, needs to be in ''V'' or ''E''.';	
EXEC sp_addmessage 55216, 16,
	N'Function: [rif40].[rif40_run_study], Recursion %i, rif40_run_study study %i had error.';		
EXEC sp_addmessage 55207, 16,
	N'Function: [rif40].[rif40_run_study], Expecting to update %i %s(s), updated %i during state transition (%s=>%s) for study %i.';	
EXEC sp_addmessage 55212, 16,
	N'Function: [rif40].[rif40_run_study], Study ID %i not found, study in unexpected and unknown state.';	
EXEC sp_addmessage 55213, 16,
	N'Function: [rif40].[rif40_run_study], Study %i in unexpected state %s.';	

EXEC sp_addmessage 55400, 16,
	N'Function: [rif40].[rif40_create_extract], Study ID %i not found.';		
EXEC sp_addmessage 55401, 16,
	N'Function: [rif40].[rif40_create_extract], RIF40_STUDIES study %i extract table: not defined.';
EXEC sp_addmessage 55403, 16,
	N'Function: [rif40].[rif40_create_extract], RIF40_STUDIES study %i extract table: %s; in wrong state: %s.';
EXEC sp_addmessage 55404, 16,
	N'Function: [rif40].[rif40_create_extract], RIF40_STUDIES study %i extract table: %s; exists in schema: %s.';	
EXEC sp_addmessage 55405, 16,
	N'Function: [rif40].[rif40_create_extract], RIF40_STUDIES study %i extract not currently permitted [use RIF IG tool].';	
EXEC sp_addmessage 55406, 16,
	N'Function: [rif40].[rif40_create_extract], RIF40_STUDIES study %i extract must be run by study owner %s not %s.';	

EXEC sp_addmessage 56000, 16,
	N'Function: [rif40].[rif40_create_insert_statement], Study ID %i not found.';	
EXEC sp_addmessage 56001, 16,
	N'Function: [rif40].[rif40_create_insert_statement], Study ID %i no columns found for extract table: %s.';	
EXEC sp_addmessage 56004, 16,
	N'Function: [rif40].[rif40_create_insert_statement], Study ID %i no investigations created: distinct numerator: %s.';	
EXEC sp_addmessage 56006, 16,
	N'Function: [rif40].[rif40_create_insert_statement], Study ID %i NULL covariate table.';	
EXEC sp_addmessage 56007, 16,
	N'Function: [rif40].[rif40_create_insert_statement], Study ID %i multiple covariate tables: %s, %s.';	
	
	
EXEC sp_addmessage 56600, 16,
	N'Function: [rif40].[rif40_execute_insert_statement], Study ID is NULL.';	
EXEC sp_addmessage 56601, 16,
	N'Function: [rif40].[rif40_execute_insert_statement], Year start is NULL for study ID: %i.';
EXEC sp_addmessage 56602, 16,
	N'Function: [rif40].[rif40_execute_insert_statement], Year stop is NULL for study ID: %i.';
EXEC sp_addmessage 56699, 16,
	N'Function: [rif40].[rif40_execute_insert_statement],  SQL statement had error: %s%sSQL[%s]> %s;';
	
EXEC sp_addmessage 55800, 16,
	N'Function: [rif40].[rif40_insert_extract], Study ID %i not found.';	
	
EXEC sp_addmessage 55999, 16,
	N'Function: [rif40].[rif40_ddl], SQL statement had error: %s%sSQL[%s]> %s;';		
	
EXEC [sahsuland_dev].[rif40].[rif40_log] 'DEBUG1', 'rif40_custom_error_messages', 'Rif40 Custom error messages added to database';

USE sahsuland_dev;

--
-- Eof (rif40_custom_error_messages.sql)
