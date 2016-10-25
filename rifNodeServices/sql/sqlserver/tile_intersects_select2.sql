/*
 * SQL statement name: 	tile_intersects_select2.sql
 * Type:				MS SQL Server SQL
 * Parameters:
 *						1: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *
 * Description:			Select from tile intersects table
 
 geolevel_id | zoomlevel | areaid | x | y | within |                                                                             bbox

-------------+-----------+--------+---+---+--------+--------------------------------------------------------------------------------------------------------------------------------------------------------------
           1 |         0 | US     | 0 | 0 | f      | {"type":"Polygon","coordinates":[[[-180,85.0511287794693],[-180,-85.0511287794693],[180,-85.0511287794693],[180,85.0511287794693],[-180,85.0511287794693]]]}
(1 row)

 * Note:				% becomes % after substitution
 */
SELECT geolevel_id, zoomlevel, 
       COUNT(DISTINCT(areaid)) AS areas,
       MIN(x) AS xmin, MIN(y) AS ymin, 
       MAX(x) AS xmax, MAX(y) AS ymax, 
	   (MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1) AS possible_tiles,
       COUNT(DISTINCT(CAST(x AS VARCHAR) + CAST(y AS VARCHAR))) AS tiles,
	   ROUND((((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1)-COUNT(DISTINCT(CAST(x AS VARCHAR) + CAST(y AS VARCHAR))))/
			((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1)))*100, 2) AS pct_saving
  FROM %1
 GROUP BY geolevel_id, zoomlevel
 ORDER BY 1, 2;