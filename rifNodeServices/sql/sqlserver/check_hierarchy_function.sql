IF OBJECT_ID (N'check_hierarchy_%1', N'FN') IS NOT NULL  
    DROP FUNCTION check_hierarchy_%1;  
GO

CREATE FUNCTION check_hierarchy_%1(@l_geography VARCHAR, @l_hierarchytable VARCHAR, @l_type VARCHAR) 
RETURNS integer
AS
/*
 * SQL statement name: 	check_hierarchy_function.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: function name; e.g. check_hierarchy_cb_2014_us_500k
 *
 * Description:			Create hierarchy check function
 * Note:				%%%% becomes %% after substitution
 */
 
/*
Function: 		check_hierarchy_%1()
Parameters:		Geography, hierarchy table, type: 'missing', 'spurious additional' or 'multiple hierarchy'
Returns:		Nothing
Description:	Diff geography hierarchy table using dynamic method 4
				Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on
 */
BEGIN 
	DECLARE @error_count INTEGER=0;
	
	RETURN @error_count;
END