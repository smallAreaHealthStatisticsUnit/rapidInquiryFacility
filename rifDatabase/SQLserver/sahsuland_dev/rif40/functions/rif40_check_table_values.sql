--used by rif40_table_trigger, check every new row that its column names are valid

use sahsuland_dev
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_check_table_values]')
                  AND type IN (N'P', N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_check_table_values]
GO 

CREATE FUNCTION [rif40].[rif40_check_table_values](@l_table_name VARCHAR(max), @l_total_field VARCHAR(max), @l_sex_field VARCHAR(max),
	@l_age_group_field_name VARCHAR(max), @l_age_sex_group_field_name VARCHAR(max))
	RETURNS int AS
BEGIN
	EXEC [rif40].[rif40_db_name_check] 'TABLE_NAME', @l_table_name;
	EXEC [rif40].[rif40_db_name_check] 'TOTAL_FIELD', @l_total_field;
	IF @l_sex_field IS NOT NULL and @l_sex_field <> ''
		EXEC [rif40].[rif40_db_name_check] 'SEX_FIELD_NAME', @l_sex_field;
	IF @l_age_group_field_name IS NOT NULL AND @l_age_group_field_name <> ''
		EXEC [rif40].[rif40_db_name_check] 'AGE_GROUP_FIELD_NAME', @l_age_group_field_name;
	IF @l_age_sex_group_field_name IS NOT NULL AND @l_age_sex_group_field_name <> ''
		EXEC [rif40].[rif40_db_name_check] 'AGE_SEX_GROUP_FIELD_NAME', @l_age_sex_group_field_name;
	
	DECLARE @log_msg VARCHAR(max) = 'Valid names for TABLE_NAME='+@l_table_name+', TOTAL_FIELD='+@l_total_field+', SEX_FIELD_NAME='+@l_sex_field+
		' AGE_GROUP_FIELD_NAME='+@l_age_group_field_name+', AGE_SEX_GROUP_FIELD_NAME='+@l_age_sex_group_field_name;
	EXEC [rif40].[rif40_log] 'DEBUG3', '[rif40].[rif40_check_table_values]', @log_msg;
	RETURN 1;
END;
