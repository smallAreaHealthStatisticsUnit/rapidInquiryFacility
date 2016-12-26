/*
 * SQL statement name: 	add_column.sql
 * Type:				Microsoft SQL Server SQL statement
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table if it does not exist
 * Note:				%% becomes % after substitution
 */
IF COL_LENGTH('%1', '%2') IS NULL
BEGIN
    ALTER TABLE %1 ADD %2 %3;
END
