use sahsuland_dev
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_num_denom_validate]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_num_denom_validate]
GO 


CREATE FUNCTION [rif40].[rif40_num_denom_validate](@l_geography VARCHAR(max), @l_table_name VARCHAR(max))
  RETURNS INT AS
BEGIN
	DECLARE @l_owner VARCHAR(max);
/*

Function: 	rif40_num_denom_validate()
Parameters: 	geography, table/view/foreign table name
Returns: 	1 - table/view/foreign table has all geolevels present in geography, 0 - table/view/foreign table has some/all geolevels missing in geography
Description:	Validate numerator or denominator geolevels

 */
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF SUSER_SNAME() <> 'rif40' AND  IS_MEMBER(N'[rif_manager]') = 0 
		RETURN 0;

--
-- If inputs are NULL return 0
--
	IF @l_geography IS NULL OR @l_table_name IS NULL 
		RETURN 0;

--
-- Resolve schema owner of table (oracle version of this has to deal with synonyms as well)
--
	SET @l_owner = UPPER(rif40.rif40_object_resolve(@l_table_name));
--
-- Check geolevels
--
	DECLARE @total_geolevels int, @total_columns int;
	
	SELECT @total_geolevels=COUNT(l.geolevel_name),
    	@total_columns = COUNT(c.column_name)  
	FROM rif40.t_rif40_geolevels l
	LEFT OUTER JOIN [INFORMATION_SCHEMA].[COLUMNS] c 
			ON (c.TABLE_SCHEMA = @l_owner AND 
				c.table_name = @l_table_name   AND c.column_name collate database_default= l.geolevel_name collate database_default)
		 WHERE geography  = @l_geography
   		   AND resolution = 1;
		   
			--log: PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_num_denom_validate', '[%,%.%] total_geolevels (in geography): %; total_columns (in table/view/foreign table; matching geolevels columns): %', 
			--l_geography::VARCHAR, l_owner::VARCHAR, l_table_name::VARCHAR, c1_rec.total_geolevels::VARCHAR, c1_rec.total_columns::VARCHAR);
	IF @total_geolevels = @total_columns
		RETURN 1;		/* Validated */
	
	RETURN 0;
END
GO

--GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_num_denom_validate(character varying, character varying) TO public;

/*
COMMENT ON FUNCTION rif40_sql_pkg.rif40_num_denom_validate(character varying, character varying) IS 'Function: 	rif40_num_denom_validate()
Parameters: 	geography, table/view/foreign table name
Returns: 	1 - table/view/foreign table has all geolevels present in geography, 0 - table/view/foreign table has some/all geolevels missing in geography
Description:	Validate numerator or denominator geolevels';
*/