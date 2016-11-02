/*
 * SQL statement name: 	tile_intersects_insert.sql
 * Type:				Postgres/PostGIS SQL
 * Parameters:
 *						1: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *						2: Tile limits table name; e.g. tile_limits_cb_2014_us_500k
 *						3: Geometry table name; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Insert into tile intersects table
 * Note:				%% becomes % after substitution
 */
EXPLAIN ANALYZE INSERT INTO %1 (
	geolevel_id,
	zoomlevel, 
	areaid,
	x, 
	y, 
	bbox,
	geom,
    optimised_geojson,
	within
)
WITH a AS (
	SELECT zoomlevel, x_mintile, x_maxtile, y_mintile, y_maxtile	  
	  FROM %2
	 WHERE zoomlevel = 0
), x AS (
	SELECT zoomlevel, generate_series(x_mintile, x_maxtile) AS x_series
	  FROM a
), y AS (	 
	SELECT zoomlevel, generate_series(y_mintile, y_maxtile) AS y_series	
	  FROM a       
), b AS (
	SELECT x.zoomlevel, 
	       x.x_series AS x, 
	       y.y_series AS y,      
	       tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
		   tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
		   tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
		   tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
      FROM x, y
	 WHERE x.zoomlevel = y.zoomlevel
), c AS (
	SELECT b.zoomlevel, b.x, b.y, 
		   ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) AS bbox,
		   c.geolevel_id,
		   c.areaid,
		   c.geom
	  FROM b, %3 c
	 WHERE c.zoomlevel = 6
	   AND ST_Intersects(ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), c.geom) /* intersects */
)
SELECT c.geolevel_id,
	   c.zoomlevel, 
	   c.areaid,
	   c.x, 
	   c.y, 
	   c.bbox,
	   c.geom,
       ST_AsGeoJson(c.geom)::JSON AS optimised_geojson,
	   ST_Within(c.bbox, c.geom) AS within /* Used to exclude any tile boundary completely within the area, i.e. there are no bounaries in the tile */
  FROM c
 ORDER BY c.geolevel_id, c.zoomlevel, c.x, c.y