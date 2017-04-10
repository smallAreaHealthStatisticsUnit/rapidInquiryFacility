/*
This draft does not provide logging or error messages or any other useful feedback

COMMENT ON FUNCTION rif40_sql_pkg.rif40_object_resolve(character varying) IS 'Function: 	rif40_object_resolve()
Parameters:	Table/view name
Returns: 	Schema owner for first object found with name in search path
Description:	Is object resolvable?

Search search path for table/view/foreign table
*/

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_object_resolve]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_object_resolve]
GO 

CREATE FUNCTION [rif40].[rif40_object_resolve](@l_table_name VARCHAR(500))
RETURNS VARCHAR(30) AS
BEGIN

	IF (object_id('rif_data.' + LOWER(@l_table_name)) IS NOT NULL)
		RETURN 'rif_data';
	ELSE IF (object_id('rif_studies.' + LOWER(@l_table_name)) IS NOT NULL)
		RETURN 'rif_studies';
	ELSE IF (object_id('rif40.' + LOWER(@l_table_name)) IS NOT NULL)
		RETURN 'rif40';
		
	RETURN NULL;
END
GO

GRANT EXECUTE ON [rif40].[rif40_object_resolve] TO rif_user, rif_manager;
GO

-- 
-- Eof


--produces error:
/*
GRANT EXECUTE ON rif40.rif40_object_resolve(varchar) TO public
GO
*/
/*
COMMENT ON FUNCTION rif40_sql_pkg.rif40_object_resolve(character varying) IS 'Function: 	rif40_object_resolve()
Parameters:	Table/view name
Returns: 	Schema owner for first object found with name in search path
Description:	Is object resolvable?

Search search path for table/view/foreign table
';
*/