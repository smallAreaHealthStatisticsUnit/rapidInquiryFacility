/*
 * SQL statement name: 	create_tile_limits_table.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: Tile limits table; e.g. tile_limits_cb_2014_us_500k
 *						2: Geometry table; e.g. geometry_cb_2014_us_500k
 *						3: max_zoomlevel
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
CREATE TABLE %1
AS 
WITH a AS (
	SELECT generate_series(0, %3, 1) AS zoomlevel
 ), b AS ( /* Get bounds of geography */
        SELECT a.zoomlevel,
		       ST_XMax(b.geom) AS Xmax,
		       ST_XMin(b.geom) AS Xmin,
		       ST_YMax(b.geom) AS Ymax,
		       ST_YMin(b.geom) AS Ymin
      FROM a 
			LEFT OUTER JOIN %2 b ON (b.geolevel_id = 1 AND a.zoomlevel = b.zoomlevel)
), c AS (
        SELECT b.zoomlevel,
		       ST_XMax(b.geom) AS Xmax,
		       ST_XMin(b.geom) AS Xmin,
		       ST_YMax(b.geom) AS Ymax,
		       ST_YMin(b.geom) AS Ymin
      FROM %2 b
	 WHERE b.geolevel_id  = 1
	   AND b.zoomlevel = 6
), d AS ( /* Convert XY bounds to tile numbers */
        SELECT b.zoomlevel,
               COALESCE(b.Xmin, c.Xmin) AS x_min, 
			   COALESCE(b.Xmax, c.Xmax) AS x_max, 
			   COALESCE(b.Ymin, c.Ymin) AS y_min, 
			   COALESCE(b.Ymax, c.Ymax) AS y_max,
               tileMaker_latitude2tile(COALESCE(b.Ymax, c.Ymax), b.zoomlevel) AS Y_mintile,
               tileMaker_latitude2tile(COALESCE(b.Ymin, c.Ymin), b.zoomlevel) AS Y_maxtile,
               tileMaker_longitude2tile(COALESCE(b.Xmin, c.Xmin), b.zoomlevel) AS X_mintile,
               tileMaker_longitude2tile(COALESCE(b.Xmax, c.Xmax), b.zoomlevel) AS X_maxtile
      FROM b, c
)
SELECT d.*,
       ST_MakeEnvelope(d.x_min, d.y_min, d.x_max, d.y_max, 4326) AS bbox
  FROM d