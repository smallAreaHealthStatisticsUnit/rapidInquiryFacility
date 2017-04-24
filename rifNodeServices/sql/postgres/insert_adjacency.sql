/*
 * SQL statement name: 	insert_adjacency.sql
 * Type:				Postgres/PostGIS SQL
 * Parameters:
 *						1: adjacency table; e.g. adjacency_cb_2014_us_500k
 *						2: geometry table; e.g. geometry_cb_2014_us_500k
 *						3: Max zoomlevel
 *
 * Description:			Create insert statement into adjacency table
 * Note:				%%%% becomes %% after substitution
 */
INSERT INTO %1(geolevel_id, areaid, num_adjacencies, adjacency_list)
WITH a AS (
	SELECT a.geolevel_id, a.areaid, b.areaid AS adjacent_areaid
	  FROM %2 a, %2 b
	 WHERE a.zoomlevel   = %3
	   AND b.zoomlevel   = %3
	   AND a.geolevel_id = b.geolevel_id
	   AND a.areaid      != b.areaid
	   AND a.geom        && b.geom 			/* Bounding box intersect first for efficiency */
	   AND ST_Intersects(a.geom, b.geom)
)
SELECT geolevel_id, areaid,
	   COUNT(adjacent_areaid) AS num_adjacencies, 
	   STRING_AGG(adjacent_areaid, ',' ORDER BY adjacent_areaid)::VARCHAR AS adjacency_list
  FROM a
 GROUP BY geolevel_id, areaid
 ORDER BY 1, 2