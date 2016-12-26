/*
 * SQL statement name: 	add_column.sql
 * Type:				Common SQL
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table
 * Note:				%% becomes % after substitution
 */
ALTER TABLE %1
  ADD %2 %3