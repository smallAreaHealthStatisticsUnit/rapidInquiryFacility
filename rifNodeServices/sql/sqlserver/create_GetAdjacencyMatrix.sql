/*
 * SQL statement name: 	create_GetAdjacencyMatrix.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *						2: adjacency table; e.g. adjacency_cb_2014_us_500k
 *
 * Description:			Create <geography>_GetAdjacencyMatrix() function
 * Note:				% becomes % after substitution
 *
 * DECLARE @study_id INTEGER=[rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq') -* Get current sequence *-;
 * SELECT TOP 10 SUBSTRING(areaid, 1, 20) AS areaid, num_adjacencies, SUBSTRING(adjacency_list, 1, 90) AS adjacency_list_truncated
 *  FROM [rif40].[sahsuland_GetAdjacencyMatrix](@study_id);
 * GO
 */
CREATE FUNCTION [rif40].[%1_GetAdjacencyMatrix](@study_id INTEGER)
RETURNS @rtnTable TABLE 
(
--
--  Columns returned by the function
--
	geolevel_id		INTEGER			NOT NULL,
	areaid			VARCHAR(200)	NOT NULL,
	num_adjacencies INTEGER			NOT NULL,
	adjacency_list	VARCHAR(8000)	NOT NULL
)
AS
BEGIN		   
--
	WITH b AS ( /* Tilemaker: has adjacency table */
		SELECT b1.area_id, b3.geolevel_id
		  FROM [rif40].[rif40_study_areas] b1, [rif40].[rif40_studies] b2, [rif40].[rif40_geolevels] b3
		 WHERE b1.study_id  = @study_id
		   AND b1.study_id  = b2.study_id	    
		   AND b2.geography = b3.geography
	)
	INSERT INTO @rtnTable(geolevel_id, areaid, num_adjacencies, adjacency_list)
	SELECT c1.geolevel_id, c1.areaid, c1.num_adjacencies, c1.adjacency_list
	  FROM [rif_data].[%2] c1, b
	 WHERE c1.geolevel_id   = b.geolevel_id
	   AND c1.areaid        = b.area_id;  
--
	RETURN;
END