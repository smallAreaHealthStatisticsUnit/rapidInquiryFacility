/*
 * SQL statement name: 	tile_intersects_select2.sql
 * Type:				MS SQL Server SQL
 * Parameters:
 *						1: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *
 * Description:			Select from tile intersects table
 
geolevel_id zoomlevel   areas       xmin        ymin        xmax        ymax        possible_tiles tiles       pct_saving
----------- ----------- ----------- ----------- ----------- ----------- ----------- -------------- ----------- ----------
          1           0           1           0           0           0           0              1           1         .0
          2           0          56           0           0           0           0              1           1         .0
          2           1          56           0           0           1           1              4           3       25.0
          2           2          56           0           0           3           2             12           5       58.3
          2           3          56           0           1           7           4             32          10       68.8
          2           4          56           0           3          15           8             96          22       77.1
          2           5          56           0           6          31          17            384          48       87.5
          2           6          56           0          13          63          34           1408         111       92.1
          2           7          56           0          27         127          69           5504         281       94.9
          2           8          56           0          54         255         135          20992         665       96.8
          3           0        3233           0           0           0           0              1           1         .0
          3           1        3233           0           0           1           1              4           3       25.0
          3           2        3233           0           0           3           2             12           5       58.3
          3           3        3233           0           1           7           4             32          10       68.8
          3           4        3233           0           3          15           8             96          22       77.1
          3           5        3233           0           6          31          17            384          49       87.2
          3           6        3233           0          13          63          34           1408         119       91.6
          3           7        3233           0          27         127          69           5504         333       94.0
          3           8        3233           0          54         255         138          21760         992       95.4

(19 rows affected)

 * Note:				% becomes % after substitution
 */
SELECT geolevel_id, zoomlevel, 
       COUNT(DISTINCT(areaid)) AS areas,
       MIN(x) AS xmin, MIN(y) AS ymin, 
       MAX(x) AS xmax, MAX(y) AS ymax, 
	   (MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1) AS possible_tiles,
       COUNT(DISTINCT(CAST(x AS VARCHAR) + CAST(y AS VARCHAR))) AS tiles,
	   CAST(ROUND((CAST( (((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1)) /* possible_tiles */ - COUNT(DISTINCT(CAST(x AS VARCHAR) + CAST(y AS VARCHAR)))) AS NUMERIC)/
			((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1)))*100, 2) AS DECIMAL(4,1)) AS pct_saving
  FROM tile_intersects_cb_2014_us_500k
 GROUP BY geolevel_id, zoomlevel
 ORDER BY 1, 2
G