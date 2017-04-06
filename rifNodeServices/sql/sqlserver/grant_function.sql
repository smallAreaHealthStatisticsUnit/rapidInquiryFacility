/*
 * SQL statement name: 	grant_function.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: function; e.g. adjacency_GetAdjacencyMatrix
 *
 * Description:			Create <geography>_GetAdjacencyMatrix() function
 * Note:				% becomes % after substitution
 */
GRANT SELECT, REFERENCES ON [rif40].[%1] TO rif_user, rif_manager