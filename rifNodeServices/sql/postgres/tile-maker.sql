\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

BEGIN;

WITH a AS ( /* Geolevel summary */
		SELECT a1.geography, 
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
 /*
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
  FROM cb_2014_us_county_500k 
 ORDER BY 1, 3, 2;
ALTER TABLE geometry_cb_2014_us_500k 
	ADD CONSTRAINT geometry_cb_2014_us_500k_pk PRIMARY KEY (geolevel, zoomlevel, areaid);	
CREATE INDEX geometry_cb_2014_us_500k_geom_gix ON geometry_cb_2014_us_500k USING GIST (geom);	
ANALYZE geometry_cb_2014_us_500k;
-- Convert to IOT
CLUSTER VERBOSE geometry_cb_2014_us_500k USING geometry_cb_2014_us_500k_pk;   */ 

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
SELECT d.*,
       ST_MakeEnvelope(d.area_Xmin, d.area_Ymin, d.area_Xmax, d.area_Ymax, 4326) AS bbox
  FROM d;

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
	   AND ST_Intersects(ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), c.geom) /* intersects */
)
SELECT 0 gid,
       c.geolevel,
	   c.zoomlevel, 
	   c.areaid,
	   c.x, 
	   c.y, 
	   c.bbox,
	   c.geom,
       ST_AsGeoJson(c.geom)::JSON AS optimised_geojson,
	   ST_Within(c.bbox, c.geom) AS within
  FROM c
 ORDER BY c.geolevel, c.zoomlevel, c.x, c.y;
 
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

DROP FUNCTION IF EXISTS tileMaker_intersector(INTEGER, INTEGER);
CREATE OR REPLACE FUNCTION tileMaker_intersector(l_geolevel INTEGER, l_zoomlevel INTEGER)
RETURNS INTEGER
AS
$BODY$
DECLARE
	num_rows INTEGER;
BEGIN
	INSERT INTO tile_intersects_cb_2014_us_500k(geolevel, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
	WITH a	AS (
		SELECT b.zoomlevel AS zoomlevel, b.x_mintile, b.x_maxtile, b.y_mintile, b.y_maxtile	  
		  FROM tile_limits_cb_2014_us_500k b
		 WHERE l_zoomlevel = b.zoomlevel
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
	), c AS ( /* Calculate bounding box, parent X/Y min */
		SELECT b.zoomlevel, 
		       b.x,
			   b.y, 
			   ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) AS bbox,
			   tileMaker_latitude2tile(b.ymin, b.zoomlevel-1) AS parent_ymin,
			   tileMaker_longitude2tile(b.xmin, b.zoomlevel-1) AS parent_xmin
		  FROM b
	), d AS (
		SELECT c.zoomlevel, c.x, c.y, c.bbox, p.areaid
		  FROM c, tile_intersects_cb_2014_us_500k p /* Parent */
		 WHERE p.geolevel    = l_geolevel
		   AND c.zoomlevel-1 = p.zoomlevel 	/* Join to parent tile from previous geolevel; i.e. exclude if not present */
		   AND c.parent_xmin = p.x  
	       AND c.parent_ymin = p.y	
	), e AS (
		SELECT d.zoomlevel, d.x, d.y, d.bbox, d.areaid
		  FROM d
		 WHERE NOT EXISTS (SELECT c2.areaid
						    FROM tile_intersects_cb_2014_us_500k c2
						   WHERE c2.geolevel  = l_geolevel
						     AND c2.zoomlevel = d.zoomlevel
							 AND c2.x         = d.x
							 AND c2.y         = d.y
							 AND c2.bbox      = d.bbox	
							 AND c2.areaid    = d.areaid)
--	), e AS (
--		SELECT d.zoomlevel, d.x, d.y, d.bbox, COUNT(d.areaid) AS total_areaid
--		  FROM e1 d 
--		GROUP BY d.zoomlevel, d.x, d.y, d.bbox
	), f AS (
		SELECT e.zoomlevel, l_geolevel AS geolevel, e.x, e.y, e.bbox, e2.areaid, e2.geom
		  FROM e, geometry_cb_2014_us_500k e2
		 WHERE ((e.zoomlevel  = e2.zoomlevel AND e.zoomlevel BETWEEN 6 AND 11) OR 
		        (e2.zoomlevel = 6            AND e.zoomlevel NOT BETWEEN 6 AND 11))
		   AND e2.geolevel  = l_geolevel
		   AND e2.areaid    = e.areaid
		   AND (e.bbox && e2.geom) 			  /* Intersect by bounding box */
		   AND ST_Intersects(e.bbox, e2.geom) /* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
	)
	SELECT f.geolevel, f.zoomlevel, f.areaid, f.x, f.y, f.bbox, f.geom, 
	       ST_AsGeoJson(f.geom)::JSON,
	       ST_Within(f.bbox, f.geom) AS within
	  FROM f
--	 WHERE NOT ST_Within(f.bbox, f.geom) /* Exclude any tile bouning completely within the area */
	 ORDER BY f.geolevel, f.zoomlevel, f.areaid, f.x, f.y;	 
	 GET DIAGNOSTICS num_rows = ROW_COUNT;
--	 
	 RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE; 

/*
SELECT geolevel, zoomlevel, areaid, x, y
  FROM tile_intersects_cb_2014_us_500k_t2
  EXCEPT
SELECT geolevel, zoomlevel, areaid, x, y
  FROM tile_intersects_cb_2014_us_500k;
  
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
	max_geolevel 	INTEGER;
	max_zoomlevel 	INTEGER;
	c1_maxgeolevel 	CURSOR FOR
		SELECT MAX(geolevel) AS max_geolevel, MAX(zoomlevel) AS max_zoomlevel
			  FROM tile_intersects_cb_2014_us_500k;		  
--
	num_rows 		INTEGER:=0;
	geolevel_rows	INTEGER:=0;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
--
	done 			BOOLEAN:=FALSE;
	tiles_per_s		NUMERIC;
BEGIN
	OPEN c1_maxgeolevel;
	FETCH c1_maxgeolevel INTO max_geolevel, max_zoomlevel;
	CLOSE c1_maxgeolevel;
	 
	max_zoomlevel 	:=7;	/* 11 */ 
--
	FOR i IN 1 .. max_geolevel LOOP
		FOR j IN 1 .. max_zoomlevel LOOP
			geolevel_rows:=0;
			done:=FALSE;
			WHILE NOT done LOOP
--				RAISE INFO 'Call tileMaker_intersector() for geolevel: %; zoomlevel: %', i, j;
				stp2:=clock_timestamp();
				num_rows:=tileMaker_intersector(i, j);
				IF num_rows != 0 THEN
					geolevel_rows:=geolevel_rows+num_rows;
					etp:=clock_timestamp();
					took:=age(etp, stp);
					took2:=age(etp, stp2);
					tiles_per_s:=ROUND(num_rows::NUMERIC/EXTRACT(EPOCH FROM took2)::NUMERIC, 1);
					IF geolevel_rows != num_rows THEN
						RAISE INFO 'Processed % intersects, % total for geolevel %/% zoomlevel: %/% in %s, %s total; % tiles/s', 
							num_rows, geolevel_rows, i, max_geolevel, j, max_zoomlevel, 
							ROUND(EXTRACT(EPOCH FROM took2)::NUMERIC, 1), 
							ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
							tiles_per_s;	
					ELSE
						RAISE INFO 'Processed % intersects for geolevel %/% zoomlevel: %/% in %s, %s total; % tiles/s', 
							num_rows, i, max_geolevel, j, max_zoomlevel, 
							ROUND(EXTRACT(EPOCH FROM took2)::NUMERIC, 1), 
							ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
							tiles_per_s;
					END IF;
				ELSE		
					done:=TRUE;
				END IF;
			END LOOP;	
			RAISE INFO 'Processed % total intersects for geolevel %/% zoomlevel: %/% in %s, %s total; % tiles/s', 
				geolevel_rows, i, max_geolevel, j, max_zoomlevel, 
				ROUND(EXTRACT(EPOCH FROM took2)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
				tiles_per_s;			
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
was:
 geolevel | zoomlevel | areas | xmin | ymin | xmax | ymax | possible_tiles | tiles | pct_saving
----------+-----------+-------+------+------+------+------+----------------+-------+------------
        1 |         0 |     1 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
        1 |         1 |     1 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
        1 |         2 |     1 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
        1 |         3 |     1 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
        1 |         4 |     1 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
        1 |         5 |     1 |    0 |    6 |   31 |   17 |            384 |    46 |      88.02
        1 |         6 |     1 |    0 |   13 |   63 |   29 |           1088 |   112 |      89.71
        1 |         7 |     1 |    0 |   27 |  127 |   59 |           4224 |   338 |      92.00
        2 |         0 |    56 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
        2 |         1 |    56 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
        2 |         2 |    56 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
        2 |         3 |    56 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
        2 |         4 |    56 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
        2 |         5 |    56 |    0 |    6 |   31 |   17 |            384 |    48 |      87.50
        2 |         6 |    56 |    0 |   13 |   63 |   34 |           1408 |   117 |      91.69
        2 |         7 |    56 |    0 |   27 |  127 |   69 |           5504 |   348 |      93.68
        3 |         0 |  3232 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
        3 |         1 |  3232 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
        3 |         2 |  3232 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
        3 |         3 |  3232 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
        3 |         4 |  3232 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
        3 |         5 |  3232 |    0 |    6 |   31 |   17 |            384 |    48 |      87.50
        3 |         6 |  3232 |    0 |   13 |   63 |   34 |           1408 |   117 |      91.69
        3 |         7 |  3232 |    0 |   27 |  127 |   69 |           5504 |   348 |      93.68
(24 rows)

is now: 
geolevel | zoomlevel | areas | xmin | ymin | xmax | ymax | possible_tiles | tiles | pct_saving
---------+-----------+-------+------+------+------+------+----------------+-------+------------
       1 |         0 |     1 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
       1 |         1 |     1 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
       1 |         2 |     1 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
       1 |         3 |     1 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
       1 |         4 |     1 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
       1 |         5 |     1 |    0 |    6 |   31 |   17 |            384 |    46 |      88.02
       1 |         6 |     1 |    0 |   13 |   63 |   29 |           1088 |   112 |      89.71
       1 |         7 |     1 |    0 |   27 |  127 |   59 |           4224 |   338 |      92.00
       2 |         0 |    56 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
       2 |         1 |    56 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
       2 |         2 |    56 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
       2 |         3 |    56 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
       2 |         4 |    56 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
       2 |         5 |    56 |    0 |    6 |   31 |   17 |            384 |    48 |      87.50
       2 |         6 |    56 |    0 |   13 |   63 |   34 |           1408 |   117 |      91.69
       2 |         7 |    56 |    0 |   27 |  127 |   69 |           5504 |   348 |      93.68
       3 |         0 |  3232 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
       3 |         1 |  3232 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
       3 |         2 |  3232 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
       3 |         3 |  3232 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
       3 |         4 |  3232 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
       3 |         5 |  3232 |    0 |    6 |   31 |   17 |            384 |    48 |      87.50
       3 |         6 |  3229 |    0 |   13 |   63 |   33 |           1344 |   116 |      91.37 <== 1 missing
       3 |         7 |  3229 |    0 |   27 |  127 |   67 |           5248 |   347 |      93.39 <== 1 missing
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
DROP TABLE rownum_cb_2014_us_500k;

SELECT geolevel, MIN(gid) AS min_gid, MAX(gid) AS max_gid
  FROM tile_intersects_cb_2014_us_500k
  GROUP BY geolevel
  ORDER BY 1;

REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE tile_intersects_cb_2014_us_500k;

SELECT geolevel, zoomlevel, within, COUNT(gid)
  FROM tile_intersects_cb_2014_us_500k
 GROUP BY geolevel, zoomlevel, within
 ORDER BY geolevel, zoomlevel, within;
  
--  Replace geolevel WITH geolevel_id

DROP VIEW IF EXISTS tiles_cb_2014_us_500k;
DROP TABLE IF EXISTS t_tiles_cb_2014_us_500k;
CREATE TABLE t_tiles_cb_2014_us_500k
AS
SELECT a.geolevel AS geolevel_id,
	   zoomlevel,
	   x, 
	   y,
	   json_agg(optimised_geojson) AS optimised_geojson,
	   NULL::JSON AS optimised_topojson,
	   a.geolevel::Text||'_'||b.geolevel_name||'_'||a.zoomlevel||'_'||a.x::Text||'_'||a.y::Text AS tile_id
  FROM tile_intersects_cb_2014_us_500k a, geolevels_cb_2014_us_500k b
 WHERE a.geolevel = b.geolevel_id 
 GROUP BY a.geolevel,
		  b.geolevel_name,
	      a.zoomlevel,
	      a.x, 
	      a.y
 ORDER BY 1, 2, 3, 4;
ALTER TABLE t_tiles_cb_2014_us_500k 
	ADD CONSTRAINT t_tiles_cb_2014_us_500k_pk PRIMARY KEY (geolevel_id, zoomlevel, x, y);
ALTER TABLE t_tiles_cb_2014_us_500k 
	ADD CONSTRAINT t_tiles_cb_2014_us_500k_uk UNIQUE (tile_id);			
ANALYZE VERBOSE t_tiles_cb_2014_us_500k; 
  
CREATE VIEW tiles_cb_2014_us_500k AS 
WITH a AS (
         SELECT geography,
            MAX(geolevel_id) AS max_geolevel_id
           FROM geolevels_cb_2014_us_500k
          GROUP BY geography
        ), b AS (
         SELECT a.geography,
            generate_series(1, a.max_geolevel_id::integer, 1) AS geolevel_id
           FROM a
        ), c AS (
         SELECT
            b2.geolevel_name,
            b.geolevel_id,
            b.geography
           FROM b, geolevels_cb_2014_us_500k b2
		  WHERE b.geolevel_id = b2.geolevel_id
        ), d AS (
         SELECT generate_series(0, 11, 1) AS zoomlevel
        ), ex AS (
         SELECT d.zoomlevel,
            generate_series(0, power(2::double precision, d.zoomlevel::double precision)::integer - 1, 1) AS xy_series
           FROM d
        ), ey AS (
         SELECT c.geolevel_name,
            c.geolevel_id,
            c.geography,
            ex.zoomlevel,
            ex.xy_series
           FROM c,
            ex
        )
 SELECT z.geography,
        z.geolevel_id,
    z.geolevel_name,
        CASE
            WHEN h.tile_id IS NULL THEN 1
            ELSE 0
        END AS no_area_ids, 
    COALESCE(h.tile_id, z.geolevel_id::Text||'_'||z.geolevel_name||'_'||z.zoomlevel||'_'||z.x::Text||'_'||z.y::Text) AS tile_id,
    z.x,
    z.y,
    z.zoomlevel,
    COALESCE(h.optimised_geojson, '{"type": "FeatureCollection","features":[]}'::json) AS optimised_geojson,
    COALESCE(h.optimised_topojson, '{"type": "FeatureCollection","features":[]}'::json) AS optimised_topojson
   FROM ( SELECT ey.geolevel_name,
            ey.geolevel_id,
            ey.geography,
            ex.zoomlevel,
            ex.xy_series AS x,
            ey.xy_series AS y
           FROM ey,
            ex
          WHERE ex.zoomlevel = ey.zoomlevel) z
     LEFT JOIN t_tiles_cb_2014_us_500k h ON (
		z.zoomlevel = h.zoomlevel AND 
		z.x = h.x AND 
		z.y = h.y AND 
		z.geolevel_id = h.geolevel_id);

COMMENT ON VIEW tiles_cb_2014_us_500k
  IS 'Maptiles view for geography; empty tiles are added to complete zoomlevels for zoomlevels 0 to 11. This view is efficent!';
COMMENT ON COLUMN tiles_cb_2014_us_500k.geography IS 'Geography';
COMMENT ON COLUMN tiles_cb_2014_us_500k.geolevel_name IS 'Name of geolevel. This will be a column name in the numerator/denominator tables';
COMMENT ON COLUMN tiles_cb_2014_us_500k.no_area_ids IS 'Tile contains no area_ids flag: 0/1';
COMMENT ON COLUMN tiles_cb_2014_us_500k.tile_id IS 'Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>';
COMMENT ON COLUMN tiles_cb_2014_us_500k.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT ON COLUMN tiles_cb_2014_us_500k.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT ON COLUMN tiles_cb_2014_us_500k.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
COMMENT ON COLUMN tiles_cb_2014_us_500k.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N. ';
COMMENT ON COLUMN tiles_cb_2014_us_500k.optimised_topojson IS 'Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.';
 
-- Data
SELECT no_area_ids, SUBSTRING(optimised_geojson::Text FROM 1 FOR 90) AS optimised_geojson
  FROM tiles_cb_2014_us_500k
 WHERE zoomlevel = 6 and geolevel_id = 3 and x = 17 and y = 26;
-- Null tile
SELECT no_area_ids, SUBSTRING(optimised_geojson::Text FROM 1 FOR 90) AS optimised_geojson
  FROM tiles_cb_2014_us_500k
 WHERE zoomlevel = 6 and geolevel_id = 3 and x = 26 and y = 26;
-- No data
SELECT no_area_ids, SUBSTRING(optimised_geojson::Text FROM 1 FOR 90) AS optimised_geojson
  FROM tiles_cb_2014_us_500k
 WHERE zoomlevel = 6 and geolevel_id = 3 and x = 17 and y = 260; 
 
END;
--

 