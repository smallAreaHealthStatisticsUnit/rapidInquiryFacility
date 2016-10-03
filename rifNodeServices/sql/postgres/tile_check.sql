/*
 * SQL statement name: 	tileMaker_tile2latitude.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			
 *						1: Lowest resolution geolevels table
 *						2: Geography
 *						3: min_zoomlevel
 *						4: max_zoomlevel
 *						5: Geolevel id = 1 geometry table
 *
 * Description:			Convert OSM tile y to latitude (WGS84 - 4326)
 * Note:				%% becomes % after substitution
 */
WITH a AS ( /* Geolevel summary */
		SELECT a1.geography, 
		       a1.geolevel_name AS min_geolevel_name,
               MIN(geolevel_id) AS min_geolevel_id,
               %4::INTEGER AS zoomlevel,
               a2.max_geolevel_id
          FROM %1 a1, (
                        SELECT geography, MAX(geolevel_id) AS max_geolevel_id
  						  FROM %1 
						 GROUP BY geography
						) a2
         WHERE a1.geography     = '%2' 
           AND a1.geography     = a2.geography
         GROUP BY a1.geography, a1.geolevel_name, a2.max_geolevel_id
        HAVING MIN(geolevel_id) = 1
), b AS ( /* Get bounds of geography */
        SELECT a2.geography,
               a2.min_geolevel_id,
               a2.max_geolevel_id,
               a2.zoomlevel,
          CASE
                                WHEN a2.zoomlevel <= %3 THEN ST_XMax(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (%3+1) AND %4 THEN ST_XMax(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Xmax,
          CASE
                                WHEN a2.zoomlevel <= %3 THEN ST_XMin(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (%3+1) AND %4 THEN ST_XMin(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Xmin,
          CASE
                                WHEN a2.zoomlevel <= %3 THEN ST_YMax(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (%3+1) AND %4 THEN ST_YMax(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Ymax,
          CASE
                                WHEN a2.zoomlevel <= %3 THEN ST_YMin(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (%3+1) AND %4 THEN ST_YMin(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Ymin
      FROM %5 b, a a2  
), d AS ( /* Convert XY bounds to tile numbers */
        SELECT geography, min_geolevel_id, max_geolevel_id, zoomlevel,
                   Xmin AS area_Xmin, Xmax AS area_Xmax, Ymin AS area_Ymin, Ymax AS area_Ymax,
           tileMaker_latitude2tile(Ymin, zoomlevel) AS Y_mintile,
           tileMaker_latitude2tile(Ymax, zoomlevel) AS Y_maxtile,
           tileMaker_longitude2tile(Xmin, zoomlevel) AS X_mintile,
           tileMaker_longitude2tile(Xmax, zoomlevel) AS X_maxtile
      FROM b
)
SELECT * FROM d