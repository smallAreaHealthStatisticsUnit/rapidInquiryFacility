
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_num_denom_validate2]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_num_denom_validate2]
GO 


CREATE FUNCTION [rif40].[rif40_num_denom_validate2](@l_geography VARCHAR(max), @l_table_name VARCHAR(max))
  RETURNS VARCHAR(120) AS
BEGIN
	DECLARE @l_owner VARCHAR(max);
/*

Function: 	rif40_num_denom_validate2()
Parameters: 	geography, table/view/foreign table name
Returns: 	1 - table/view/foreign table has all geolevels present in geography, 0 - table/view/foreign table has some/all geolevels missing in geography
Description:	Validate numerator or denominator geolevels

 */
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF SUSER_SNAME() <> 'rif40' AND  IS_MEMBER(N'[rif_manager]') = 0 
		RETURN 'Not rif40 or have rif_user or rif_manager role';

--
-- If inputs are NULL return 0
--
	IF @l_geography IS NULL OR @l_table_name IS NULL 
		RETURN 'Null inputs';

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
				c.table_name = @l_table_name AND 
				c.column_name collate database_default= l.geolevel_name collate database_default)
		 WHERE geography  = @l_geography
   		   AND resolution = 1;
		   
			--log: PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_num_denom_validate2', '[%,%.%] total_geolevels (in geography): %; total_columns (in table/view/foreign table; matching geolevels columns): %', 
			--l_geography::VARCHAR, l_owner::VARCHAR, l_table_name::VARCHAR, c1_rec.total_geolevels::VARCHAR, c1_rec.total_columns::VARCHAR);
	
	DECLARE @msg VARCHAR(120);
	IF @total_geolevels = @total_columns BEGIN
		SET @msg='Validated: ' + @l_geography + ' geolevel ids (' + CAST(@total_geolevels AS VARCHAR) + 
		') match column names; ' +
		@l_owner + '.' + @l_table_name;		/* Validated */
		RETURN @msg;
	END;
	
	SET @msg='Not Validated: ' + @l_geography + ' geolevel ids (' + CAST(@total_geolevels AS VARCHAR) + 
		') do not match column names  (' + CAST(@total_columns AS VARCHAR) + '); '  +
		@l_owner + '.' + @l_table_name;		/* Not Validated */
	RETURN @msg;
END
GO

GRANT EXECUTE ON [rif40].[rif40_num_denom_validate2] TO rif_user, rif_manager;
GO
/*
 e.g.
 
 SELECT [rif40].[rif40_num_denom_validate2]('SAHSULAND', 'NUM_SAHSULAND_CANCER');  
 SELECT [rif40].[rif40_num_denom_validate2]('SAHSULAND', 'POP_SAHSULAND_POP'); 
 SELECT [rif40].[rif40_num_denom_validate2]('SAHSULAND', 'NUM_UTAHBREAST');
 SELECT [rif40].[rif40_num_denom_validate2]('USA_2014', 'NUM_UTAHBREAST');
 SELECT [rif40].[rif40_num_denom_validate2]('USA_2014', 'POP_UTAHPOPNORACE');
 GO

------------------------------------------------------------------------------------------------------------------------
Validated: SAHSULAND geolevel ids (4) match column names; RIF_DATA.NUM_SAHSULAND_CANCER

(1 rows affected)

------------------------------------------------------------------------------------------------------------------------
Validated: SAHSULAND geolevel ids (4) match column names; RIF_DATA.POP_SAHSULAND_POP

(1 rows affected)

------------------------------------------------------------------------------------------------------------------------
Not Validated: SAHSULAND geolevel ids (4) do not match column names  (0); RIF_DATA.NUM_UTAHBREAST

(1 rows affected)

------------------------------------------------------------------------------------------------------------------------
Not Validated: USA_2014 geolevel ids (3) do not match column names  (0); RIF_DATA.NUM_UTAHBREAST

(1 rows affected)

------------------------------------------------------------------------------------------------------------------------
Not Validated: USA_2014 geolevel ids (3) do not match column names  (0); RIF_DATA.POP_UTAHPOPNORACE

(1 rows affected)
 
 */
-- 
-- Eof
