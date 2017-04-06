/*
 * SQL statement name: 	drop_GetAdjacencyMatrix.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *
 * Description:			Drop <geography>_GetAdjacencyMatrix() function
 * Note:				% becomes % after substitution
 */ 
IF EXISTS (SELECT *
             FROM sys.objects
            WHERE object_id = OBJECT_ID(N'[rif40].[%1_GetAdjacencyMatrix]')
              AND type IN ( N'TF' )) /*  SQL table-valued-function */
	DROP FUNCTION [rif40].[%1_GetAdjacencyMatrix]