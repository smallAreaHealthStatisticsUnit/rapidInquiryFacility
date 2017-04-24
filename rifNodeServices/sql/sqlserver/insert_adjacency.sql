/*
 * SQL statement name: 	insert_adjacency.sql
 * Type:				Microsoft SQL Server SQL
 * Parameters:
 *						1: adjacency table; e.g. adjacency_cb_2014_us_500k
 *						2: geometry table; e.g. geometry_cb_2014_us_500k
 *						3: Max zoomlevel
 *
 * Description:			Create insert statement into adjacency table
 * Note:				%%%% becomes %% after substitution
 */
WITH a AS (
	SELECT a.geolevel_id, a.areaid, b.areaid AS adjacent_areaid
	  FROM %2 a, %2 b
	 WHERE a.zoomlevel                 = %3
	   AND b.zoomlevel                 = %3
	   AND a.geolevel_id               = b.geolevel_id
	   AND a.areaid                    != b.areaid
	   AND a.geom.STIntersects(b.geom) = 1
), b AS (
	SELECT a.geolevel_id, a.areaid,
		   c.adjacency_list 
	  FROM a OUTER APPLY (
		SELECT STUFF(( SELECT ',' + b.adjacent_areaid
		   FROM a AS b
		  WHERE a.areaid      = b.areaid
			AND a.geolevel_id = b.geolevel_id
		  ORDER BY b.adjacent_areaid
			FOR XML PATH('') ), 1,1,'') AS adjacency_list) AS c
)
INSERT INTO %1(geolevel_id, areaid, num_adjacencies, adjacency_list)
SELECT DISTINCT geolevel_id, areaid, LEN(adjacency_list)-LEN(REPLACE(adjacency_list, ',', ''))+1 AS num_adjacencies, adjacency_list
  FROM b
 ORDER BY 1, 2