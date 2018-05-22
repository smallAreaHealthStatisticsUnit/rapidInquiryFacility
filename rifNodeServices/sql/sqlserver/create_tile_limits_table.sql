/*
 * SQL statement name: 	create_tile_limits_table.sql
 * Type:				MS SQL Server SQL
 * Parameters:
 *						1: Tile limits table; e.g. tile_limits_cb_2014_us_500k
 *						2: Geometry table; e.g. geometry_cb_2014_us_500k
 *						3: max_zoomlevel
 *
 * Description:			Create tile limits table
 * Note:				%% becomes % after substitution
 */
WITH a AS (
	SELECT z.IntValue AS zoomlevel
	  FROM $(SQLCMDUSER).generate_series(0, %3, 1) z
), b AS ( /* Get bounds of geography */
        SELECT a.zoomlevel,
			   geometry::EnvelopeAggregate(b.geom).STPointN(1).STX AS Xmin,
			   geometry::EnvelopeAggregate(b.geom).STPointN(1).STY AS Ymin,
			   geometry::EnvelopeAggregate(b.geom).STPointN(3).STX AS Xmax,
			   geometry::EnvelopeAggregate(b.geom).STPointN(3).STY AS Ymax
      FROM a 
			LEFT OUTER JOIN %2 b ON (b.geolevel_id = 1 AND a.zoomlevel = b.zoomlevel)
	 GROUP BY a.zoomlevel
), c AS (
        SELECT b.zoomlevel,
			   geometry::EnvelopeAggregate(b.geom).STPointN(1).STX AS Xmin,
			   geometry::EnvelopeAggregate(b.geom).STPointN(1).STY AS Ymin,
			   geometry::EnvelopeAggregate(b.geom).STPointN(3).STX AS Xmax,
			   geometry::EnvelopeAggregate(b.geom).STPointN(3).STY AS Ymax
      FROM %2 b
	 WHERE b.geolevel_id  = 1
	   AND b.zoomlevel = 6
	 GROUP BY b.zoomlevel
), d AS ( /* Convert XY bounds to tile numbers */
        SELECT b.zoomlevel,
               COALESCE(b.Xmin, c.Xmin) AS x_min, 
			   COALESCE(b.Xmax, c.Xmax) AS x_max, 
			   COALESCE(b.Ymin, c.Ymin) AS y_min, 
			   COALESCE(b.Ymax, c.Ymax) AS y_max,
               $(SQLCMDUSER).tileMaker_latitude2tile(COALESCE(b.Ymax, c.Ymax), b.zoomlevel) AS Y_mintile,
               $(SQLCMDUSER).tileMaker_latitude2tile(COALESCE(b.Ymin, c.Ymin), b.zoomlevel) AS Y_maxtile,
               $(SQLCMDUSER).tileMaker_longitude2tile(COALESCE(b.Xmin, c.Xmin), b.zoomlevel) AS X_mintile,
               $(SQLCMDUSER).tileMaker_longitude2tile(COALESCE(b.Xmax, c.Xmax), b.zoomlevel) AS X_maxtile
      FROM b, c
)
SELECT d.*,
       $(SQLCMDUSER).tileMaker_STMakeEnvelope(d.x_min, d.y_min, d.x_max, d.y_max, 4326) AS bbox
  INTO %1
  FROM d