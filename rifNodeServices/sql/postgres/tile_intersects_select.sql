/*
 * SQL statement name: 	tile_intersects_select.sql
 * Type:				Postgres/PostGIS SQL
 * Parameters:
 *						1: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *
 * Description:			Select from tile intersects table
 
 geolevel_id | zoomlevel | areaid | x | y | within |                                                                             bbox

-------------+-----------+--------+---+---+--------+--------------------------------------------------------------------------------------------------------------------------------------------------------------
           1 |         0 | US     | 0 | 0 | f      | {"type":"Polygon","coordinates":[[[-180,85.0511287794693],[-180,-85.0511287794693],[180,-85.0511287794693],[180,85.0511287794693],[-180,85.0511287794693]]]}
(1 row)

 * Note:				%% becomes % after substitution
 */
SELECT geolevel_id,
	   zoomlevel, 
	   areaid,
	   x, 
	   y, 
	   within,
	   ST_AsGeoJson(bbox) AS bbox
  FROM %1
 WHERE zoomlevel = 0 AND geolevel_id = 1