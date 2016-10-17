/*
 * SQL statement name: 	drop_generate_series.sql
 * Type:				MS SQL Server SQL statement
 * Parameters:			None
 * Description:			Drop generate_series() function
 */
IF OBJECT_ID (N'generate_series', N'FN') IS NOT NULL  
    DROP FUNCTION generate_series;  