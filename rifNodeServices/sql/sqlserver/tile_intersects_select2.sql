/*
 * SQL statement name: 	tile_intersects_select2.sql
 * Type:				MS SQL Server SQL
 * Parameters:
 *						1: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *
 * Description:			Select from tile intersects table
 
geolevel_id zoomlevel   areas       xmin        ymin        xmax        ymax        possible_tiles tiles       pct_saving
----------- ----------- ----------- ----------- ----------- ----------- ----------- -------------- ----------- -----------
          1           0           1           0           0           0           0              1           1           0
          2           0          56           0           0           0           0              1           1           0
          2           1          56           0           0           1           1              4           3           0
          2           2          56           0           0           3           2             12           5           0
          2           3          56           0           1           7           4             32          10           0
          2           4          56           0           3          15           8             96          22           0
          2           5          56           0           6          31          17            384          48           0
          2           6          56           0          13          63          34           1408         111           0
          3           0        3233           0           0           0           0              1           1           0
          3           1        3233           0           0           1           1              4           3           0
          3           2        3233           0           0           3           2             12           5           0
          3           3        3233           0           1           7           4             32          10           0
          3           4        3233           0           3          15           8             96          22           0
          3           5        3233           0           6          31          17            384          49           0
          3           6        3233           0          13          63          34           1408         119           0

(15 rows affected)

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
 ORDER BY 1, 2