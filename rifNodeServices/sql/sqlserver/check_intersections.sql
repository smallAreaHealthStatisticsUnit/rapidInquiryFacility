DECLARE @l_geography AS VARCHAR(200)='%1';
/*
 * SQL statement name: 	check_intersections.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *
 * Description:			Check intersections
 * Note:				%%%% becomes %% after substitution
 */
BEGIN
	PRINT 'OK';
END