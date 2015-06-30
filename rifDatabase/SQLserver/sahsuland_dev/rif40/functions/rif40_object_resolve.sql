/*
This draft does not provide logging or error messages or any other useful feedback

COMMENT ON FUNCTION rif40_sql_pkg.rif40_object_resolve(character varying) IS 'Function: 	rif40_object_resolve()
Parameters:	Table/view name
Returns: 	Schema owner for first object found with name in search path
Description:	Is object resolvable?

Search search path for table/view/foreign table
*/

use sahsuland_dev
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_object_resolve]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_object_resolve]
GO 

CREATE FUNCTION [rif40].[rif40_object_resolve](@l_table_name VARCHAR(500))
  RETURNS VARCHAR AS
BEGIN

	IF (object_id(@l_table_name) IS NOT NULL)
		RETURN object_schema_name(object_id(@l_table_name));
	
	RETURN NULL;
END
GO

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