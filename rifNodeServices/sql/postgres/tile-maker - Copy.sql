\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

BEGIN;

WITH a AS ( /* Geolevel summary */
		SELECT a1.geography, 
		       a1.geolevel_name AS min_geolevel_name,
               MIN(geolevel_id) AS min_geolevel_id,
               11::INTEGER AS zoomlevel,
               a2.max_geolevel_id
          FROM geolevels_cb_2014_us_500k a1, (
                        SELECT geography, MAX(geolevel_id) AS max_geolevel_id
  						  FROM geolevels_cb_2014_us_500k 
						 GROUP BY geography
						) a2
         WHERE a1.geography     = 'cb_2014_us_500k' 
           AND a1.geography     = a2.geography
         GROUP BY a1.geography, a1.geolevel_name, a2.max_geolevel_id
        HAVING MIN(geolevel_id) = 1
), b AS ( /* Get bounds of geography */
        SELECT a2.geography,
               a2.min_geolevel_id,
               a2.max_geolevel_id,
               a2.zoomlevel,
          CASE
                                WHEN a2.zoomlevel <= 6 THEN ST_XMax(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (6+1) AND 11 THEN ST_XMax(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Xmax,
          CASE
                                WHEN a2.zoomlevel <= 6 THEN ST_XMin(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (6+1) AND 11 THEN ST_XMin(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Xmin,
          CASE
                                WHEN a2.zoomlevel <= 6 THEN ST_YMax(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (6+1) AND 11 THEN ST_YMax(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Ymax,
          CASE
                                WHEN a2.zoomlevel <= 6 THEN ST_YMin(b.geom_6)                 	/* Optimised for zoom level 6 */
                                WHEN a2.zoomlevel BETWEEN (6+1) AND 11 THEN ST_YMin(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Ymin
      FROM cb_2014_us_nation_5m b, a a2  
), d AS ( /* Convert XY bounds to tile numbers */
        SELECT geography, min_geolevel_id, max_geolevel_id, zoomlevel,
                   Xmin AS area_Xmin, Xmax AS area_Xmax, Ymin AS area_Ymin, Ymax AS area_Ymax,
           tileMaker_latitude2tile(Ymin, zoomlevel) AS Y_mintile,
           tileMaker_latitude2tile(Ymax, zoomlevel) AS Y_maxtile,
           tileMaker_longitude2tile(Xmin, zoomlevel) AS X_mintile,
           tileMaker_longitude2tile(Xmax, zoomlevel) AS X_maxtile
      FROM b
)
SELECT * FROM d;
/*
    geography    | min_geolevel_id | max_geolevel_id | zoomlevel | area_xmin  | area_xmax | area_ymin  | area_ymax | y_mintile | y_maxtile | x_mintile | x_maxtile
-----------------+-----------------+-----------------+-----------+------------+-----------+------------+-----------+-----------+-----------+-----------+-----------
 cb_2014_us_500k |               1 |               3 |        11 | -179.14734 | 179.77847 | -14.552549 | 71.352561 |      1107 |       435 |         4 |      2046
(1 row)
 */
SELECT geolevel_name, geolevel_id, shapefile_table
  FROM geolevels_cb_2014_us_500k
 WHERE geography = 'cb_2014_us_500k';
/*
     geolevel_name      | geolevel_id |    shapefile_table
------------------------+-------------+------------------------
 cb_2014_us_county_500k |           3 | cb_2014_us_county_500k
 cb_2014_us_nation_5m   |           1 | cb_2014_us_nation_5m
 cb_2014_us_state_500k  |           2 | cb_2014_us_state_500k
(3 rows)
 */
 
DROP TABLE IF EXISTS geometry_cb_2014_us_500k;
CREATE TABLE geometry_cb_2014_us_500k
AS
SELECT 1 geolevel,
       areaid, 
       6 AS zoomlevel, 
       geom_6 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel,
       areaid, 
       7 AS zoomlevel, 
       geom_7 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel,
       areaid, 
       8 AS zoomlevel, 
       geom_8 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel,
       areaid, 
       9 AS zoomlevel, 
       geom_9 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel,
       areaid, 
       10 AS zoomlevel, 
       geom_10 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel,
       areaid, 
       11 AS zoomlevel, 
       geom_11 AS geom
  FROM cb_2014_us_nation_5m
UNION  
SELECT 2 geolevel,
       areaid, 
       6 AS zoomlevel, 
       geom_6 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel,
       areaid, 
       7 AS zoomlevel, 
       geom_7 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel,
       areaid, 
       8 AS zoomlevel, 
       geom_8 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel,
       areaid, 
       9 AS zoomlevel, 
       geom_9 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel,
       areaid, 
       10 AS zoomlevel, 
       geom_10 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel,
       areaid, 
       11 AS zoomlevel, 
       geom_11 AS geom
  FROM cb_2014_us_state_500k  
 /* 
UNION  
SELECT 3 geolevel,
       areaid, 
       6 AS zoomlevel, 
       geom_6 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel,
       areaid, 
       7 AS zoomlevel, 
       geom_7 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel,
       areaid, 
       8 AS zoomlevel, 
       geom_8 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel,
       areaid, 
       9 AS zoomlevel, 
       geom_9 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel,
       areaid, 
       10 AS zoomlevel, 
       geom_10 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel,
       areaid, 
       11 AS zoomlevel, 
       geom_11 AS geom
  FROM cb_2014_us_county_500k  */  
 ORDER BY 1, 3, 2;
ALTER TABLE geometry_cb_2014_us_500k 
	ADD CONSTRAINT geometry_cb_2014_us_500k_pk PRIMARY KEY (geolevel, zoomlevel, areaid);	
CREATE INDEX geometry_cb_2014_us_500k_geom_gix ON geometry_cb_2014_us_500k USING GIST (geom);	
ANALYZE geometry_cb_2014_us_500k;
-- Convert to IOT
CLUSTER VERBOSE geometry_cb_2014_us_500k USING geometry_cb_2014_us_500k_pk;

SELECT geolevel, areaid, COUNT(zoomlevel) AS zoomlevels
  FROM geometry_cb_2014_us_500k
 WHERE geolevel IN (1, 2)
 GROUP BY geolevel, areaid
 ORDER BY 1, 2, 3;
 
DROP TABLE IF EXISTS tile_limits_cb_2014_us_500k; 
CREATE TABLE tile_limits_cb_2014_us_500k
AS 
WITH a AS (
	SELECT generate_series(0, 11, 1) AS zoomlevel 
 ), b AS ( /* Get bounds of geography */
        SELECT a.zoomlevel,
		       ST_XMax(b.geom) AS Xmax,
		       ST_XMin(b.geom) AS Xmin,
		       ST_YMax(b.geom) AS Ymax,
		       ST_YMin(b.geom) AS Ymin
      FROM a
			LEFT OUTER JOIN geometry_cb_2014_us_500k b ON (b.geolevel = 1 AND a.zoomlevel = b.zoomlevel)
), c AS (
        SELECT b.zoomlevel,
		       ST_XMax(b.geom) AS Xmax,
		       ST_XMin(b.geom) AS Xmin,
		       ST_YMax(b.geom) AS Ymax,
		       ST_YMin(b.geom) AS Ymin
      FROM geometry_cb_2014_us_500k b 
	 WHERE b.geolevel  = 1
	   AND b.zoomlevel = 6
), d AS ( /* Convert XY bounds to tile numbers */
        SELECT b.zoomlevel,
               COALESCE(b.Xmin, c.Xmin) AS area_Xmin, 
			   COALESCE(b.Xmax, c.Xmax) AS area_Xmax, 
			   COALESCE(b.Ymin, c.Ymin) AS area_Ymin, 
			   COALESCE(b.Ymax, c.Ymax) AS area_Ymax,
               tileMaker_latitude2tile(COALESCE(b.Ymax, c.Ymax), b.zoomlevel) AS Y_mintile,
               tileMaker_latitude2tile(COALESCE(b.Ymin, c.Ymin), b.zoomlevel) AS Y_maxtile,
               tileMaker_longitude2tile(COALESCE(b.Xmin, c.Xmin), b.zoomlevel) AS X_mintile,
               tileMaker_longitude2tile(COALESCE(b.Xmax, c.Xmax), b.zoomlevel) AS X_maxtile
      FROM b, c
)
SELECT * FROM d;

ALTER TABLE tile_limits_cb_2014_us_500k 
	ADD CONSTRAINT tile_limits_cb_2014_us_500k_pk PRIMARY KEY (zoomlevel);		
ANALYZE tile_limits_cb_2014_us_500k;
SELECT * FROM tile_limits_cb_2014_us_500k;

/*
 zoomlevel |    area_xmin     |    area_xmax     |     area_ymin     | area_ymax | y_mintile | y_maxtile | x_mintile | x_maxtile
-----------+------------------+------------------+-------------------+-----------+-----------+-----------+-----------+-----------
         0 | -179.13729006727 | 179.773803959804 | -14.3737802873213 | 71.352561 |         0 |         0 |         0 |         0
         1 | -179.13729006727 | 179.773803959804 | -14.3737802873213 | 71.352561 |         0 |         1 |         0 |         1
         2 | -179.13729006727 | 179.773803959804 | -14.3737802873213 | 71.352561 |         0 |         2 |         0 |         3
         3 | -179.13729006727 | 179.773803959804 | -14.3737802873213 | 71.352561 |         1 |         4 |         0 |         7
         4 | -179.13729006727 | 179.773803959804 | -14.3737802873213 | 71.352561 |         3 |         8 |         0 |        15
         5 | -179.13729006727 | 179.773803959804 | -14.3737802873213 | 71.352561 |         6 |        17 |         0 |        31
         6 | -179.13729006727 | 179.773803959804 | -14.3737802873213 | 71.352561 |        13 |        34 |         0 |        63
         7 |       -179.14734 |        179.77847 |        -14.552549 | 71.352561 |        27 |        69 |         0 |       127
         8 |       -179.14734 |        179.77847 |        -14.552549 | 71.352561 |        54 |       138 |         0 |       255
         9 |       -179.14734 |        179.77847 |        -14.552549 | 71.352561 |       108 |       276 |         1 |       511
        10 |       -179.14734 |        179.77847 |        -14.552549 | 71.352561 |       217 |       553 |         2 |      1023
        11 |       -179.14734 |        179.77847 |        -14.552549 | 71.352561 |       435 |      1107 |         4 |      2046
(12 rows)
 */

/*
WITH a AS (
	SELECT zoomlevel, x_mintile, x_maxtile, y_mintile, y_maxtile	  
	  FROM tile_limits_cb_2014_us_500k
	 WHERE zoomlevel <= 11
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
	SELECT b.zoomlevel,
	       ST_Intersects(ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), c.geom_6) AS intersects
	  FROM b, cb_2014_us_nation_5m c
)
SELECT c.zoomlevel, 
       COUNT(c.zoomlevel) AS total_tiles, 
	   SUM(CASE WHEN c.intersects THEN 1 ELSE 0 END) AS intersected_tiles,
	   ROUND((SUM(CASE WHEN c.intersects THEN 1 ELSE 0 END)::numeric/COUNT(c.zoomlevel)::numeric)*100, 2) AS intersect_pct
  FROM c
 GROUP BY c.zoomlevel
 ORDER BY 1;
 */
/*
 zoomlevel | total_tiles | intersected_tiles | intersect_pct
-----------+-------------+-------------------+---------------
         0 |           1 |                 1 |        100.00
         1 |           4 |                 3 |         75.00
         2 |          12 |                 5 |         41.67
         3 |          32 |                10 |         31.25
         4 |          96 |                22 |         22.92
         5 |         384 |                46 |         11.98
         6 |        1408 |               113 |          8.03
         7 |        5504 |               339 |          6.16
         8 |       21760 |              1139 |          5.23
         9 |       86359 |              4087 |          4.73
        10 |      344414 |             15287 |          4.44
        11 |     1374939 |             58907 |          4.28
(12 rows)

Time: 254075.332 ms

So potential speed gain ~25x or 10 seconds!
 */ 
DROP TABLE IF EXISTS tile_intersects_cb_2014_us_500k;
CREATE TABLE tile_intersects_cb_2014_us_500k
AS
WITH a AS (
	SELECT zoomlevel, x_mintile, x_maxtile, y_mintile, y_maxtile	  
	  FROM tile_limits_cb_2014_us_500k
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
		   c.geolevel,
		   c.areaid,
		   c.geom
	  FROM b, geometry_cb_2014_us_500k c
	 WHERE ((b.zoomlevel = c.zoomlevel AND b.zoomlevel BETWEEN 6 AND 11) OR 
		    (c.zoomlevel = 6           AND b.zoomlevel NOT BETWEEN 6 AND 11))
	 /*  AND geolevel < 3 -* remove county */
	   AND ST_Intersects(ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), c.geom) /* intersects */
), d AS (
SELECT c.geolevel,
	   c.zoomlevel, 
	   c.areaid,
	   c.x, 
	   c.y, 
	   c.bbox,
	   ST_Collect(
			ST_ClipByBox2D(c.geom, c.bbox)) AS intersection /* Can use ST_Intersection() */
  FROM c
 GROUP BY c.geolevel,
	      c.zoomlevel, 
	      c.areaid,
	      c.x, 
		  c.y, 
		  c.bbox
), e AS (
	SELECT d.geolevel,
	       d.zoomlevel, 
	       d.areaid,
	       d.x, 
	       d.y, 
	       d.bbox,
	        CASE ST_GeometryType(d.intersection) /* Convert GeometryCollection to multipolygon */
				 WHEN 'ST_GeometryCollection' THEN ST_Multi(ST_UnaryUnion(ST_MakeValid(d.intersection)))
				 ELSE ST_Multi(d.intersection) 
		    END AS intersection,
			c.geom
	   FROM d, c
	  WHERE d.geolevel  = c.geolevel
	    AND d.zoomlevel = c.zoomlevel
	    AND d.areaid    = c.areaid
	    AND d.x         = c.x 
	    AND d.y         = c.y
)
SELECT 0 gid,
       e.geolevel,
	   e.zoomlevel,
	   e.areaid,
	   e.x, e.y, e.bbox, 
       e.intersection,
	   e.geom,
       ST_AsGeoJson(e.geom)::JSON AS optimised_geojson
  FROM e
 ORDER BY e.geolevel, e.zoomlevel, e.x, e.y;

SELECT geolevel, zoomlevel, areaid, x, y, 
--       ST_AsGeoJson(bbox) AS bbox,   
       ST_IsValid(intersection) AS valid,
--       SUBSTRING(ST_AsText(intersection) FROM 1 FOR 30) AS wkt, 
       LENGTH(ST_AsText(intersection)) AS wkt_len, 
       LENGTH(ST_AsGeoJson(intersection)) AS geojson_len, 
	   ST_GeometryType(intersection) AS geomtype 
  FROM tile_intersects_cb_2014_us_500k
 ORDER BY 1, 2, 3, 4, 5 LIMIT 20;
 
/*
 zoomlevel | x | y |                                                                             bbox
-----------+---+---+--------------------------------------------------------------------------------------------------------------------------------------------------------------
         0 | 0 | 0 | {"type":"Polygon","coordinates":[[[-180,85.0511287794693],[-180,-85.0511287794693],[180,-85.0511287794693],[180,85.0511287794693],[-180,85.0511287794693]]]}
(1 row)
 */  
 
ALTER TABLE tile_intersects_cb_2014_us_500k 
	ADD CONSTRAINT tile_intersects_cb_2014_us_500k_pk PRIMARY KEY (geolevel, zoomlevel, areaid, x, y);

-- PK is v.v slightly faster than the 3x hash
--CREATE INDEX tile_intersects_cb_2014_us_500k_zoomlevel ON tile_intersects_cb_2014_us_500k USING HASH(zoomlevel);
--CREATE INDEX tile_intersects_cb_2014_us_500k_x ON tile_intersects_cb_2014_us_500k USING HASH(x);
--CREATE INDEX tile_intersects_cb_2014_us_500k_y ON tile_intersects_cb_2014_us_500k USING HASH(y);
--REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE VERBOSE tile_intersects_cb_2014_us_500k;  

/*
 zoomlevel | x | y |                                                                             bbox
-----------+---+---+--------------------------------------------------------------------------------------------------------------------------------------------------------------
         0 | 0 | 0 | {"type":"Polygon","coordinates":[[[-180,85.0511287794693],[-180,-85.0511287794693],[180,-85.0511287794693],[180,85.0511287794693],[-180,85.0511287794693]]]}
         1 | 0 | 0 | {"type":"Polygon","coordinates":[[[-180,85.0511287794693],[-180,0],[0,0],[0,85.0511287794693],[-180,85.0511287794693]]]}
         1 | 0 | 1 | {"type":"Polygon","coordinates":[[[-180,0],[-180,-85.0511287794693],[0,-85.0511287794693],[0,0],[-180,0]]]}
         1 | 1 | 0 | {"type":"Polygon","coordinates":[[[0,85.0511287794693],[0,0],[180,0],[180,85.0511287794693],[0,85.0511287794693]]]}
(4 rows)
 */ 

DROP FUNCTION IF EXISTS tileMaker_exception(VARCHAR);
/*
CREATE OR REPLACE FUNCTION tileMaker_exception(text VARCHAR)
RETURNS void AS $$
BEGIN
  RAISE EXCEPTION '%', $1;
END;
$$ LANGUAGE plpgsql;
*/
--SELECT tileMaker_exception('Test');

DROP FUNCTION IF EXISTS tileMaker_intersector(INTEGER);
CREATE OR REPLACE FUNCTION tileMaker_intersector(l_geolevel INTEGER)
RETURNS INTEGER
AS
$BODY$
DECLARE
	new_zoomlevel INTEGER;
	c1_maxzl CURSOR FOR
		SELECT MAX(zoomlevel) AS max_zoomlevel
	      FROM tile_intersects_cb_2014_us_500k
		 WHERE geolevel = l_geolevel;
BEGIN
	INSERT INTO tile_intersects_cb_2014_us_500k(geolevel, zoomlevel, areaid, x, y, bbox) 
	WITH a AS (
		SELECT MAX(zoomlevel)+1 AS next_zoomlevel
		  FROM tile_intersects_cb_2014_us_500k
		 WHERE geolevel = l_geolevel
	), b AS (
		SELECT a.next_zoomlevel AS zoomlevel, b.x_mintile, b.x_maxtile, b.y_mintile, b.y_maxtile	  
		  FROM a, tile_limits_cb_2014_us_500k b
		 WHERE a.next_zoomlevel = b.zoomlevel
	), x AS (
		SELECT zoomlevel, generate_series(x_mintile, x_maxtile) AS x_series
		  FROM b
	), y AS (	 
		SELECT zoomlevel, generate_series(y_mintile, y_maxtile) AS y_series	
		  FROM b       
	), c AS (
		SELECT x.zoomlevel, 
			   x.x_series AS x, 
			   y.y_series AS y,      
			   tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
			   tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
			   tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
			   tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
		  FROM x, y
		 WHERE x.zoomlevel = y.zoomlevel
	), d AS (
		SELECT c.zoomlevel, 
		       c.x,
			   c.y, 
			   ST_MakeEnvelope(c.xmin, c.ymin, c.xmax, c.ymax, 4326) AS bbox,
			   tileMaker_latitude2tile(c.ymin, c.zoomlevel-1) AS parent_ymin,
			   tileMaker_longitude2tile(c.xmin, c.zoomlevel-1) AS parent_xmin
		  FROM c
	), e AS (
		SELECT d.zoomlevel, d.x, d.y, d.bbox
		  FROM d, tile_intersects_cb_2014_us_500k f 
		WHERE f.geolevel    = l_geolevel
		  AND d.zoomlevel-1 = f.zoomlevel /* Join to parent tile from previous geolevel; i.e. exclude if not present */
		  AND d.parent_xmin = f.x  
	      AND d.parent_ymin = f.y
	), f AS (
		SELECT e.zoomlevel, l_geolevel AS geolevel, e.x, e.y, e.bbox, e2.areaid, e2.geom
		  FROM e, geometry_cb_2014_us_500k e2
		 WHERE ((e.zoomlevel  = e2.zoomlevel AND e.zoomlevel BETWEEN 6 AND 11) OR 
		        (e2.zoomlevel = 6            AND e.zoomlevel NOT BETWEEN 6 AND 11))
		   AND e2.geolevel  = l_geolevel
		   AND (e.bbox && e2.geom) 			  /* Intersect by bounding box */
		   AND ST_Intersects(e.bbox, e2.geom) /* intersects: (e.bbox && e.geom) is slower as it generates more tiles */
	)
	SELECT f.geolevel, f.zoomlevel, f.areaid, f.x, f.y, f.bbox
	  FROM f
	 GROUP BY f.geolevel, f.zoomlevel, f.areaid, f.x, f.y, f.bbox
	 ORDER BY f.geolevel, f.zoomlevel, f.areaid, f.x, f.y;	 
--	 
	 OPEN c1_maxzl;
	 FETCH c1_maxzl INTO new_zoomlevel;
	 CLOSE c1_maxzl;
--	 
	 RETURN new_zoomlevel;
END;
$BODY$
LANGUAGE plpgsql VOLATILE; 

DROP FUNCTION IF EXISTS tileMaker_intersector2(INTEGER);
CREATE OR REPLACE FUNCTION tileMaker_intersector2(l_geolevel INTEGER)
RETURNS INTEGER
AS
$BODY$
DECLARE
	max_gid INTEGER;
	c2_maxgid CURSOR(l_geolevel INTEGER) FOR
		SELECT MAX(gid)	AS max_gid
			  FROM intersection_cb_2014_us_500k
			 WHERE l_geolevel = geolevel;
BEGIN
	INSERT INTO intersection_cb_2014_us_500k(gid, geolevel, zoomlevel, areaid, intersection, geom)
	WITH a AS (
		SELECT COALESCE(MAX(gid), 0) AS max_gid
  		  FROM intersection_cb_2014_us_500k
		 WHERE l_geolevel = geolevel
	), a1  AS (
		SELECT COALESCE(MAX(gid), 0) AS max_prev_gid
  		  FROM intersection_cb_2014_us_500k
		 WHERE l_geolevel-1 = geolevel
	), b AS (
			SELECT c.gid, c.geolevel, c.bbox, c.zoomlevel,
				   e.areaid, e.geom 
			  FROM a, a1, tile_intersects_cb_2014_us_500k c, geometry_cb_2014_us_500k e
			 WHERE c.areaid = e.areaid 			 
			   AND c.geolevel = l_geolevel		 
			   AND e.geolevel = l_geolevel
			   AND ((e.zoomlevel = c.zoomlevel AND c.zoomlevel BETWEEN 6 AND 11) OR 
		            (e.zoomlevel = 6           AND c.zoomlevel NOT BETWEEN 6 AND 11))
		       AND ((c.gid BETWEEN a.max_gid+1 AND a.max_gid+100)			 
			    OR  (a.max_gid = 0 AND c.gid BETWEEN a1.max_prev_gid+1 AND a1.max_prev_gid+100))
	), c AS (
		SELECT b.gid, b.geolevel, b.zoomlevel, b.areaid, b.bbox,
					ST_Collect( /* CollectionAggregate() in SQL Server */
							ST_ClipByBox2D(b.geom, b.bbox))  AS intersection
		  FROM b
		 GROUP BY b.gid, b.geolevel, b.zoomlevel, b.areaid, b.bbox
	 ), d AS (
		 SELECT c.gid, c.geolevel, c.zoomlevel, c.areaid, c.bbox, 
				CASE ST_GeometryType(c.intersection) /* Convert GeometryCollection to multipolygon */
					 WHEN 'ST_GeometryCollection' THEN ST_Multi(ST_UnaryUnion(ST_MakeValid(c.intersection)))
					 ELSE ST_Multi(c.intersection) 
				END AS intersection
		   FROM c
	  )
	 SELECT d.gid, d.geolevel, d.zoomlevel, d.areaid,
/*		    CASE 
				WHEN ST_IsEmpty(ST_Difference(d.bbox, d.intersection)) THEN NULL 
				ELSE ST_Multi(d.intersection)
			END AS intersection */
			ST_Multi(d.intersection) AS intersection,
			b.geom
	   FROM d, b
	  WHERE d.geolevel  = b.geolevel
	    AND d.zoomlevel = b.zoomlevel
	    AND d.areaid    = b.areaid
	    AND d.gid		= b.gid
	  ORDER BY d.gid;
--	 
	 OPEN c2_maxgid(l_geolevel);
	 FETCH c2_maxgid INTO max_gid;
	 CLOSE c2_maxgid;
--	 
	 RETURN max_gid;
END;
$BODY$
LANGUAGE plpgsql VOLATILE; 

DROP FUNCTION IF EXISTS tileMaker_intersector3(INTEGER);
CREATE OR REPLACE FUNCTION tileMaker_intersector3(l_geolevel INTEGER)
RETURNS INTEGER
AS
$BODY$
DECLARE
	min_gid INTEGER;
	c3_mingid CURSOR FOR
		WITH a AS (
			SELECT MIN(gid) AS min_gid
				  FROM tile_intersects_cb_2014_us_500k
				 WHERE intersection IS NULL
				   AND l_geolevel = geolevel
		), b AS (
			SELECT MAX(gid) AS max_gid
				  FROM tile_intersects_cb_2014_us_500k
			     WHERE l_geolevel = geolevel
		)
		SELECT COALESCE(a.min_gid, b.max_gid) AS min_gid
		  FROM a, b;
BEGIN
	WITH a AS (
		SELECT COALESCE(MIN(gid), 0) AS min_gid
  		  FROM tile_intersects_cb_2014_us_500k
		 WHERE intersection IS NULL
		   AND l_geolevel = geolevel
	) 
	UPDATE tile_intersects_cb_2014_us_500k b
	   SET intersection = (SELECT a1.intersection
							 FROM intersection_cb_2014_us_500k a1
							WHERE a1.gid = b.gid)
	 WHERE intersection IS NULL AND EXISTS (SELECT c.gid
											  FROM intersection_cb_2014_us_500k c, a
											 WHERE c.gid = b.gid
											   AND c.gid BETWEEN a.min_gid AND a.min_gid+500);
--											   
	WITH a AS (
		SELECT COALESCE(MIN(gid), 0) AS min_gid
  		  FROM tile_intersects_cb_2014_us_500k
		 WHERE geom IS NULL
		   AND l_geolevel = geolevel
	) 
	UPDATE tile_intersects_cb_2014_us_500k b
	   SET geom = (SELECT a1.geom
					 FROM intersection_cb_2014_us_500k a1
					WHERE a1.gid = b.gid)
	 WHERE geom IS NULL AND EXISTS (SELECT c.gid
											  FROM intersection_cb_2014_us_500k c, a
											 WHERE c.gid = b.gid
											   AND c.gid BETWEEN a.min_gid AND a.min_gid+500);
--		
-- Trim off bounding box:
--
-- 1. C = ST_Difference(A, B) = geomA - ST_Intersection(A,B)
--								 
	UPDATE tile_intersects_cb_2014_us_500k
	   SET trimmed_intersect = ST_Multi(ST_Boundary(ST_SymDifference(bbox, intersection)))
     WHERE intersection IS NOT NULL
	   AND trimmed_intersect IS NULL
  	   AND NOT ST_IsEmpty(ST_SymDifference(bbox, intersection));
--
-- 2. T = ST_SymDifference(A, C) = A XOR C = C XOR A = ST_Union(geomA,geomB) - ST_Intersection(A,B).
--	
-- ST_Intersection is a binary AND; ST_Difference is a binary minus and ST_SymDifference binary XOR
--
-- T = geomA XOR (A - (A AND B))
-- T = B - A
-- 
--	UPDATE tile_intersects_cb_2014_us_500k
--	   SET trimmed_intersect = ST_Multi(ST_Difference(intersection, trimmed_intersect))
--     WHERE trimmed_intersect IS NOT NULL AND NOT ST_IsEmpty(ST_Difference(intersection, trimmed_intersect));	
--
	REINDEX TABLE tile_intersects_cb_2014_us_500k; 			
--	 
	UPDATE tile_intersects_cb_2014_us_500k
	   SET optimised_geojson = ST_AsGeoJson(geom)::JSON
	 WHERE optimised_geojson IS NULL AND geom IS NOT NULL;
--
--	UPDATE tile_intersects_cb_2014_us_500k b
--	   SET png_tile = ST_AsPng(
--							ST_AsRaster(
--									ST_Transform(intersection, 3857 /* Spherical Mercator */), 
--									256				/* Scale X */, 
--									256				/* Scale Y */, 
--									ARRAY['8BUI'] 	/* 8-bit unsigned integer pixeltype */, 
--									ARRAY[1]		/* Value */, 
--									ARRAY[0] 		/* nodataval */) 
--								)
--	 WHERE png_tile IS NULL AND intersection IS NOT NULL; 
--	 
	 OPEN c3_mingid;
	 FETCH c3_mingid INTO min_gid;
	 CLOSE c3_mingid;
--	 
	 RETURN min_gid;
END;
$BODY$
LANGUAGE plpgsql VOLATILE; 

/*
intersects | parent_intersects | OK

         t                   t   Intersection condition
         f                   t   No intersect at this geolevel 
         t                   f   Not possible; error [checked hence long 1460s run]
         f                   f   Neither intersects 
		 
INFO:  Processed zoomlevel: 11 in 1460.186944 seconds reduced to:	

psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 1 in 0.0s, 0.0 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 2 in 0.0s, 0.1 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 3 in 0.0s, 0.1 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 4 in 0.0s, 0.1 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 5 in 0.1s, 0.2 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 6 in 0.1s, 0.3 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 7 in 0.2s, 0.5 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 8 in 0.7s, 1.2 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 9 in 2.4s, 3.6 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 10 in 9.8s, 13.4 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 1/3 zoomlevel: 11 in 40.0s, 53.5 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 1 in 0.0s, 53.7 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 2 in 0.2s, 53.8 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 3 in 0.2s, 54.1 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 4 in 0.6s, 54.7 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 5 in 0.5s, 55.1 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 6 in 1.0s, 56.1 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 7 in 1.1s, 57.2 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 8 in 3.2s, 60.4 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 9 in 9.6s, 70.0 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 10 in 31.8s, 101.8 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 2/3 zoomlevel: 11 in 118.4s, 220.2 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 1 in 0.1s, 220.6 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 2 in 0.1s, 220.7 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 3 in 0.1s, 220.8 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 4 in 0.1s, 220.9 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 5 in 0.1s, 221.0 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 6 in 0.2s, 221.2 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 7 in 0.4s, 221.6 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 8 in 1.3s, 222.9 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 9 in 4.3s, 227.2 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 10 in 15.7s, 242.9 total
psql:tile-maker.sql:677: INFO:  Processed intersects for geolevel 3/3 zoomlevel: 11 in 61.4s, 304.3 total
DO
Time: 304729.900 ms

Faaste than the original UJS n ation run!

INFO:  Processed zoomlevel: 11 in 52.88089 seconds	 

DO
Time: ~70s
From before: 254 s

So actual speed gain ~5x. Note using correct geom_<zoomlevel>, not geom_6 as before
 */
DO LANGUAGE plpgsql $$
DECLARE
	max_geolevel INTEGER;
	c1_maxgeolevel CURSOR FOR
		SELECT MAX(geolevel) AS max_geolevel
			  FROM tile_intersects_cb_2014_us_500k;
--
	new_zoomlevel 	INTEGER:=0;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
	max_zoomlevel 	INTEGER:=6;	/* 11 */ 
BEGIN
	 OPEN c1_maxgeolevel;
	 FETCH c1_maxgeolevel INTO max_geolevel;
	 CLOSE c1_maxgeolevel;
--
	FOR i IN 1 .. max_geolevel LOOP
		new_zoomlevel:=0;
		WHILE new_zoomlevel <= (max_zoomlevel-1) LOOP
			stp2:=clock_timestamp();
			new_zoomlevel:=tileMaker_intersector(i);
			etp:=clock_timestamp();
			took:=age(etp, stp);
			took2:=age(etp, stp2);
			RAISE INFO 'Processed intersects for geolevel %/% zoomlevel: % in %s, % total', 
				i, max_geolevel, new_zoomlevel, 
				ROUND(EXTRACT(EPOCH FROM took2)::NUMERIC, 1), ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1);
				
		END LOOP;	
	END LOOP;
END;
$$; 	

REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE tile_intersects_cb_2014_us_500k;
		
SELECT geolevel, zoomlevel, 
       COUNT(DISTINCT(areaid)) AS areas,
       MIN(x) AS xmin, MIN(y) AS ymin, 
       MAX(x) AS xmax, MAX(y) AS ymax, 
	   (MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1) AS possible_tiles,
       COUNT(DISTINCT(x::Text||y::Text)) AS tiles,
	   ROUND((((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1)-COUNT(DISTINCT(x::Text||y::Text)))::numeric/
			((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1))::numeric)*100, 2) AS pct_saving
  FROM tile_intersects_cb_2014_us_500k
 GROUP BY geolevel, zoomlevel
 ORDER BY 1, 2;
 
/*
 geolevel | zoomlevel | xmin | ymin | xmax | ymax | possible_tiles | tiles | pct_saving
----------+-----------+------+------+------+------+----------------+-------+------------
        1 |         0 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
        1 |         1 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
        1 |         2 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
        1 |         3 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
        1 |         4 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
        1 |         5 |    0 |    6 |   31 |   17 |            384 |    46 |      88.02
        1 |         6 |    0 |   13 |   63 |   29 |           1088 |   112 |      89.71
        1 |         7 |    0 |   27 |  127 |   59 |           4224 |   338 |      92.00
        1 |         8 |    0 |   54 |  255 |  118 |          16640 |  1139 |      93.16
        1 |         9 |    1 |  108 |  511 |  237 |          66430 |  4093 |      93.84
        1 |        10 |    2 |  217 | 1023 |  474 |         263676 | 15308 |      94.19
        1 |        11 |    4 |  435 | 2046 |  948 |        1050102 | 58968 |      94.38
        2 |         0 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
        2 |         1 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
        2 |         2 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
        2 |         3 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
        2 |         4 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
        2 |         5 |    0 |    6 |   31 |   17 |            384 |    48 |      87.50
        2 |         6 |    0 |   13 |   63 |   34 |           1408 |   117 |      91.69
        2 |         7 |    0 |   27 |  127 |   69 |           5504 |   348 |      93.68
        2 |         8 |    0 |   54 |  255 |  135 |          20992 |  1150 |      94.52
        2 |         9 |    1 |  108 |  511 |  271 |          83804 |  4110 |      95.10
        2 |        10 |    2 |  217 | 1023 |  543 |         334194 | 15341 |      95.41
        2 |        11 |    4 |  435 | 2046 | 1087 |        1334079 | 59070 |      95.57
        3 |         0 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
        3 |         1 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
        3 |         2 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
        3 |         3 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
        3 |         4 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
        3 |         5 |    0 |    6 |   31 |   17 |            384 |    48 |      87.50
        3 |         6 |    0 |   13 |   63 |   34 |           1408 |   117 |      91.69
        3 |         7 |    0 |   27 |  127 |   69 |           5504 |   348 |      93.68
        3 |         8 |    0 |   54 |  255 |  135 |          20992 |  1150 |      94.52
        3 |         9 |    1 |  108 |  511 |  271 |          83804 |  4110 |      95.10
        3 |        10 |    2 |  217 | 1023 |  543 |         334194 | 15341 |      95.41
        3 |        11 |    4 |  435 | 2046 | 1087 |        1334079 | 59070 |      95.57
(36 rows)

Time: 107.764 ms
 */  

CREATE TEMPORARY TABLE rownum_cb_2014_us_500k 
AS
SELECT ROW_NUMBER() OVER (ORDER BY geolevel, zoomlevel, areaid, x, y) AS gid, geolevel, zoomlevel, areaid, x, y
  FROM tile_intersects_cb_2014_us_500k
 ORDER BY 1;
ALTER TABLE rownum_cb_2014_us_500k 
	ADD CONSTRAINT rownum_cb_2014_us_500k_pk PRIMARY KEY (geolevel, zoomlevel, areaid, x, y);	
ANALYZE rownum_cb_2014_us_500k;
UPDATE tile_intersects_cb_2014_us_500k b
   SET gid = (SELECT a.gid
                FROM rownum_cb_2014_us_500k a
			   WHERE a.zoomlevel = b.zoomlevel
			     AND a.geolevel  = b.geolevel
   			     AND a.x         = b.x
				 AND a.y         = b.y
				 AND a.areaid    = b.areaid);				 
ALTER TABLE tile_intersects_cb_2014_us_500k 
	ADD CONSTRAINT tile_intersects_cb_2014_us_500k_uk UNIQUE (gid);	
ANALYZE tile_intersects_cb_2014_us_500k;	
DROP TABLE rownum_cb_2014_us_500k;

SELECT geolevel, MIN(gid) AS min_gid, MAX(gid) AS max_gid
  FROM tile_intersects_cb_2014_us_500k
  GROUP BY geolevel
  ORDER BY 1;
   
DROP TABLE IF EXISTS intersection_cb_2014_us_500k;
CREATE TEMPORARY TABLE intersection_cb_2014_us_500k 
AS	
WITH a AS (
		SELECT c.gid, c.geolevel, c.bbox, c.zoomlevel,
		       e.areaid, e.geom 
		  FROM tile_intersects_cb_2014_us_500k c, geometry_cb_2014_us_500k e
		 WHERE c.geolevel  = e.geolevel
		   AND c.areaid    = e.areaid
		   AND c.zoomlevel = 0
		   AND ((c.zoomlevel = e.zoomlevel AND c.zoomlevel BETWEEN 6 AND 11) OR 
		        (e.zoomlevel = 6           AND c.zoomlevel NOT BETWEEN 6 AND 11))
), b AS (
SELECT a.gid, a.geolevel, a.zoomlevel, a.areaid, ST_ClipByBox2D(a.geom, a.bbox) AS intersection  
  FROM a
), c as (
	SELECT b.gid, b.geolevel, b.zoomlevel, b.areaid, ST_Collect(b.intersection) AS intersection
	  FROM b
	 WHERE b.intersection IS NOT NULL
	GROUP BY b.gid, b.geolevel, b.zoomlevel, b.areaid
)
SELECT c.gid, c.geolevel, c.zoomlevel, c.areaid, 
       CASE ST_GeometryType(c.intersection) /* Convert GeometryCollection to multipolygon */
				 WHEN 'ST_GeometryCollection' THEN ST_Multi(ST_UnaryUnion(ST_MakeValid(c.intersection)))
				 ELSE ST_Multi(c.intersection) 
		    END AS intersection,
	   a.geom
  FROM c, a
 WHERE a.geolevel  = c.geolevel
   AND a.zoomlevel = c.zoomlevel
   AND a.areaid    = c.areaid
   AND a.gid       = c.gid
 ORDER BY c.gid;
 
DELETE FROM intersection_cb_2014_us_500k;

DO LANGUAGE plpgsql $$
DECLARE
	max_geolevel INTEGER;
	c2_maxgeolevel CURSOR FOR
		SELECT MAX(geolevel) AS max_geolevel
	      FROM tile_intersects_cb_2014_us_500k;
--
	new_max_gid INTEGER=0;
	max_gid INTEGER=0;
	c3_maxgid CURSOR(l_geolevel INTEGER) FOR
		SELECT COALESCE(MAX(gid), 0) AS max_gid
		  FROM tile_intersects_cb_2014_us_500k
		 WHERE geolevel = l_geolevel;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
	pct 			NUMERIC;
	tps				NUMERIC;
BEGIN
	 OPEN c2_maxgeolevel;
	 FETCH c2_maxgeolevel INTO max_geolevel;
	 CLOSE c2_maxgeolevel;
--
	FOR i IN 1 .. max_geolevel LOOP
		new_max_gid:=0;
--
		OPEN c3_maxgid(i);
		FETCH c3_maxgid INTO max_gid;
		CLOSE c3_maxgid;
--
		WHILE new_max_gid < max_gid LOOP
			stp2:=clock_timestamp();
			new_max_gid:=tileMaker_intersector2(i);
			etp:=clock_timestamp();
			took:=age(etp, stp);
			took2:=age(etp, stp2);
			pct:=ROUND((new_max_gid::NUMERIC/max_gid::NUMERIC)*100, 2);
			tps:=ROUND((new_max_gid::NUMERIC/EXTRACT(EPOCH FROM took)::NUMERIC), 0);
			RAISE INFO 'Processed %/% intersections for geolevel %/%; % %% in %s; % tiles/s', 
				new_max_gid, 
				max_gid, 
				i,
				max_geolevel,
				pct, 
				ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
				tps;
--
			REINDEX TABLE intersection_cb_2014_us_500k;
			ANALYZE intersection_cb_2014_us_500k;
		END LOOP;
	END LOOP;
END;
$$; 

WITH a AS (
	SELECT gid, COUNT(gid) AS total
	  FROM intersection_cb_2014_us_500k
	 GROUP BY gid
	HAVING COUNT(gid) > 1
)
SELECT gid, geolevel, zoomlevel
  FROM intersection_cb_2014_us_500k b WHERE b.gid IN (SELECT a.gid FROM a) LIMIT 20;
 
ALTER TABLE intersection_cb_2014_us_500k 
	ADD CONSTRAINT intersection_cb_2014_us_500k_pk PRIMARY KEY (gid);	
ANALYZE tile_intersects_cb_2014_us_500k;

SELECT gid, geolevel, zoomlevel, 
       ST_IsValid(intersection) AS valid,
       SUBSTRING(ST_AsText(intersection) FROM 1 FOR 30) AS wkt, 
       LENGTH(ST_AsText(intersection)) AS wkt_len, 
--       LENGTH(ST_AsGeoJson(intersection)) AS geojson_len, 
	   ST_GeometryType(intersection) AS geomtype, 
	   ST_Srid(intersection) AS srid
  FROM intersection_cb_2014_us_500k
  ORDER BY 1 LIMIT 40;

SELECT AddGeometryColumn('tile_intersects_cb_2014_us_500k','trimmed_intersect', 4326, 'MULTILINESTRING', 
		2 		/* Dimension */, 
		true 	/* use typmod geometry column instead of constraint-based */);

CREATE INDEX tile_intersects_cb_2014_us_500k_gist1 ON tile_intersects_cb_2014_us_500k USING GIST (intersection);
---CREATE INDEX tile_intersects_cb_2014_us_500k_gist2 ON tile_intersects_cb_2014_us_500k USING GIST (trimmed_intersect);

DO LANGUAGE plpgsql $$
DECLARE
	max_geolevel INTEGER;
	c3_maxgeolevel CURSOR FOR
		SELECT MAX(geolevel) AS max_geolevel
	      FROM tile_intersects_cb_2014_us_500k;
--
	new_max_gid INTEGER=0;
	max_gid INTEGER=0;
	c3_maxgid CURSOR(l_geolevel INTEGER) FOR
		SELECT MAX(gid) AS max_zoomlevel
			  FROM tile_intersects_cb_2014_us_500k
			 WHERE geolevel = l_geolevel;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
	pct 			NUMERIC;
	tps				NUMERIC;
BEGIN

	OPEN c3_maxgeolevel;
	FETCH c3_maxgeolevel INTO max_geolevel;
	CLOSE c3_maxgeolevel;
--
	FOR i IN 1 .. max_geolevel LOOP
		new_max_gid:=0;
--
		OPEN c3_maxgid(i);
		FETCH c3_maxgid INTO max_gid;
		CLOSE c3_maxgid;
--
		WHILE new_max_gid < max_gid LOOP
			stp2:=clock_timestamp();
			new_max_gid:=tileMaker_intersector3(i);
			etp:=clock_timestamp();
			took:=age(etp, stp);
			took2:=age(etp, stp2);
			pct:=ROUND((new_max_gid::NUMERIC/max_gid::NUMERIC)*100, 2);
			tps:=ROUND((new_max_gid::NUMERIC/EXTRACT(EPOCH FROM took)::NUMERIC), 0);
			RAISE INFO 'Processed %/% tiles (Tile trims/GeoJSON) for geolevel %/%; % %% in %s; % tiles/s', 
				new_max_gid, 
				max_gid, 
				i,
				max_geolevel,
				pct, 
				ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
				tps;
		END LOOP;
	END LOOP;
END;
$$;

--REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE tile_intersects_cb_2014_us_500k;

DROP TABLE IF EXISTS tiles_cb_2014_us_500k;
CREATE TABLE tiles_cb_2014_us_500k
AS
SELECT geolevel,
	   zoomlevel,
	   x, 
	   y,
	   json_agg(optimised_geojson) AS optimised_geojson
  FROM tile_intersects_cb_2014_us_500k
 GROUP BY geolevel,
	      zoomlevel,
	      x, 
	      y
 ORDER BY 1, 2, 3, 4;
ALTER TABLE tiles_cb_2014_us_500k 
	ADD CONSTRAINT tiles_cb_2014_us_500k_pk PRIMARY KEY (geolevel, zoomlevel, x, y);			
ANALYZE VERBOSE tiles_cb_2014_us_500k; 
	
DROP TABLE intersection_cb_2014_us_500k; 
  
SELECT geolevel, zoomlevel, COUNT(gid) AS total
  FROM tile_intersects_cb_2014_us_500k
 WHERE trimmed_intersect IS NULL
  GROUP BY geolevel, zoomlevel
  ORDER BY geolevel, zoomlevel;
/*
 geolevel | zoomlevel | total
----------+-----------+-------
        1 |         5 |     4
        1 |         6 |    28
        2 |         5 |     1
        2 |         6 |     7
(4 rows)
 */
END;
--

\dS+ tile_intersects_cb_2014_us_500k

DROP TABLE IF EXISTS test;
CREATE TABLE test AS
WITH a AS (
	SELECT gid, bbox,
		   (ST_DumpPoints(trimmed_intersect)).path[1] pt_num, 
		   (ST_DumpPoints(trimmed_intersect)).geom geom
	  FROM tile_intersects_cb_2014_us_500k
	 WHERE intersection IS NOT NULL
	   AND trimmed_intersect IS NOT NULL
), b AS (  
	SELECT gid, bbox, pt_num, geom
	  FROM a
     WHERE NOT (ST_Equals(geom, ST_SetSrid(ST_Point(ST_Xmin(bbox), ST_Ymin(bbox)), ST_Srid(geom)))
   	    OR ST_Equals(geom, ST_SetSrid(ST_Point(ST_Xmin(bbox), ST_Ymax(bbox)), ST_Srid(geom)))
	    OR ST_Equals(geom, ST_SetSrid(ST_Point(ST_Xmax(bbox), ST_Ymin(bbox)), ST_Srid(geom)))
	    OR ST_Equals(geom, ST_SetSrid(ST_Point(ST_Xmax(bbox), ST_Ymax(bbox)), ST_Srid(geom))))
), c AS (
	SELECT gid, ST_MakeLine(geom) AS line
	  FROM b
	 GROUP BY gid
)
SELECT c.gid, geolevel, zoomlevel, x, y, c.line
  FROM c, tile_intersects_cb_2014_us_500k d
 WHERE c.gid = d.gid
 ORDER BY c.gid;
ALTER TABLE test ADD CONSTRAINT test_pk PRIMARY KEY (gid);		   	   

WITH a AS (
	SELECT gid, geolevel, zoomlevel, trimmed_intersect AS intersection
	  FROM tile_intersects_cb_2014_us_500k
)
SELECT gid, geolevel, zoomlevel, 
       ST_IsValid(intersection) AS valid,
       SUBSTRING(ST_AsText(intersection) FROM 1 FOR 30) AS wkt, 
       LENGTH(ST_AsText(intersection)) AS wkt_len, 
--       LENGTH(ST_AsGeoJson(intersection)) AS geojson_len, 
	   ST_GeometryType(intersection) AS geomtype, 
	   ST_Srid(intersection) AS srid
  FROM a
  ORDER BY 1 LIMIT 40;
  
SELECT ST_Srid(intersection) FROM tile_intersects_cb_2014_us_500k LIMIT 1;

SELECT X, Y, areaid
  FROM tile_intersects_cb_2014_us_500k
 WHERE geolevel = 2 AND zoomlevel = 6 AND areaid = '00294478';
   
SELECT X, Y, areaid
  FROM tile_intersects_cb_2014_us_500k
   WHERE geolevel = 2 AND zoomlevel = 6 AND x = 17 AND y = 26 /* Florida, Georgia */ LIMIT 20;
/*
SELECT lo_from_bytea(0, png_tile) FROM tile_intersects_cb_2014_us_500k WHERE gid=1;
\lo_export 4194857 'tile_1.png' 
 lo_from_bytea
---------------
       4194857
\lo_list
\lo_unlink 4194857
 */