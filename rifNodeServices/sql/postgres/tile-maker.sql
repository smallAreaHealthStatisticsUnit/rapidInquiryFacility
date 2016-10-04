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

DROP TABLE IF EXISTS tile_limits_cb_2014_us_500k; 
CREATE TABLE tile_limits_cb_2014_us_500k
AS 
WITH a AS (
	SELECT generate_series(0, 11, 1) AS zoomlevel 
 ), b AS ( /* Get bounds of geography */
        SELECT a.zoomlevel,
          CASE
                                WHEN a.zoomlevel <= 6 THEN ST_XMax(b.geom_11)                 	/* Optimised for zoom level 6 */
                                WHEN a.zoomlevel BETWEEN (6+1) AND 11 THEN ST_XMax(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Xmax,
          CASE
                                WHEN a.zoomlevel <= 6 THEN ST_XMin(b.geom_11)                 	/* Optimised for zoom level 6 */
                                WHEN a.zoomlevel BETWEEN (6+1) AND 11 THEN ST_XMin(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Xmin,
          CASE
                                WHEN a.zoomlevel <= 6 THEN ST_YMax(b.geom_11)                 	/* Optimised for zoom level 6 */
                                WHEN a.zoomlevel BETWEEN (6+1) AND 11 THEN ST_YMax(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Ymax,
          CASE
                                WHEN a.zoomlevel <= 6 THEN ST_YMin(b.geom_11)                 	/* Optimised for zoom level 6 */
                                WHEN a.zoomlevel BETWEEN (6+1) AND 11 THEN ST_YMin(b.geom_11)	/* Optimised for zoom level 6-11 */
                                ELSE NULL
                   END AS Ymin
      FROM cb_2014_us_nation_5m b, a  
), d AS ( /* Convert XY bounds to tile numbers */
        SELECT zoomlevel,
                   Xmin AS area_Xmin, Xmax AS area_Xmax, Ymin AS area_Ymin, Ymax AS area_Ymax,
           tileMaker_latitude2tile(Ymax, zoomlevel) AS Y_mintile,
           tileMaker_latitude2tile(Ymin, zoomlevel) AS Y_maxtile,
           tileMaker_longitude2tile(Xmin, zoomlevel) AS X_mintile,
           tileMaker_longitude2tile(Xmax, zoomlevel) AS X_maxtile
      FROM b
)
SELECT * FROM d;
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
		   c.geom_6
	  FROM b, cb_2014_us_nation_5m c
	 WHERE ST_Intersects(ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), c.geom_6) /* intersects */
), d AS (
SELECT c.zoomlevel, c.x, c.y, c.bbox,
	   ST_Intersection(c.geom_6, c.bbox) AS intersection
  FROM c
)
SELECT 0 gid, d.zoomlevel, d.x, d.y, d.bbox, 
       d.intersection,
       ST_AsGeoJson(d.intersection)::JSON AS optimised_geojson,
       ST_AsPng(ST_AsRaster(d.intersection, 256, 256)) AS png_tile,
	   to_json('X'::Text)::JSON AS optimised_topojson /* Dummy value */
  FROM d
 ORDER BY d.zoomlevel, d.x, d.y;
 
SELECT zoomlevel, x, y, ST_AsGeoJson(bbox) AS bbox
  FROM tile_intersects_cb_2014_us_500k;
/*
 zoomlevel | x | y |                                                                             bbox
-----------+---+---+--------------------------------------------------------------------------------------------------------------------------------------------------------------
         0 | 0 | 0 | {"type":"Polygon","coordinates":[[[-180,85.0511287794693],[-180,-85.0511287794693],[180,-85.0511287794693],[180,85.0511287794693],[-180,85.0511287794693]]]}
(1 row)
 */  
ALTER TABLE tile_intersects_cb_2014_us_500k 
	ADD CONSTRAINT tile_intersects_cb_2014_us_500k_pk PRIMARY KEY (zoomlevel, x, y);

-- PK is v.v slightly faster than the 3x hash
--CREATE INDEX tile_intersects_cb_2014_us_500k_zoomlevel ON tile_intersects_cb_2014_us_500k USING HASH(zoomlevel);
--CREATE INDEX tile_intersects_cb_2014_us_500k_x ON tile_intersects_cb_2014_us_500k USING HASH(x);
--CREATE INDEX tile_intersects_cb_2014_us_500k_y ON tile_intersects_cb_2014_us_500k USING HASH(y);
REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE VERBOSE tile_intersects_cb_2014_us_500k;

SELECT zoomlevel, x, y, ST_AsGeoJson(bbox) AS bbox
  FROM tile_intersects_cb_2014_us_500k;
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

DROP FUNCTION IF EXISTS tileMaker_intersector();
CREATE OR REPLACE FUNCTION tileMaker_intersector()
RETURNS INTEGER
AS
$BODY$
DECLARE
	new_zoomlevel INTEGER;
	c1_maxzl CURSOR FOR
		SELECT MAX(zoomlevel) AS max_zoomlevel
			  FROM tile_intersects_cb_2014_us_500k;
BEGIN
	INSERT INTO tile_intersects_cb_2014_us_500k(zoomlevel, x, y, bbox, optimised_topojson) 
	WITH a AS (
		SELECT MAX(zoomlevel)+1 AS next_zoomlevel
		  FROM tile_intersects_cb_2014_us_500k
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
		SELECT c.zoomlevel, c.x, c.y, 
		       CASE	
					WHEN c.zoomlevel <= 6 THEN e.geom_6 
					WHEN c.zoomlevel = 7  THEN e.geom_7 
					WHEN c.zoomlevel = 8  THEN e.geom_8 
					WHEN c.zoomlevel = 9  THEN e.geom_9 
					WHEN c.zoomlevel = 10 THEN e.geom_10
					ELSE	                   e.geom_11 
			   END AS geom /* Will need to handle max_zoomlevel */,
			   ST_MakeEnvelope(c.xmin, c.ymin, c.xmax, c.ymax, 4326) AS bbox,
			   tileMaker_latitude2tile(c.ymin, c.zoomlevel-1) AS parent_ymin,
			   tileMaker_longitude2tile(c.xmin, c.zoomlevel-1) AS parent_xmin
		  FROM c, cb_2014_us_nation_5m e
	), e AS (
		SELECT d.zoomlevel, d.x, d.y, d.bbox, d.geom
		  FROM d, tile_intersects_cb_2014_us_500k f 
		WHERE d.zoomlevel-1 = f.zoomlevel /* Join to parent tile from previous geolevel if present */
		  AND d.parent_xmin = f.x  
	      AND d.parent_ymin = f.y
	), f AS (
		SELECT e.zoomlevel, e.x, e.y, e.bbox, e.geom
		  FROM e
		 WHERE ST_Intersects(e.bbox, e.geom) /* intersects: (e.bbox && e.geom) is slower as it generates more tiles */
	)
	SELECT f.zoomlevel, f.x, f.y, f.bbox, 
		   to_json('X'::Text)::JSON AS optimised_topojson /* Dummy value */
	  FROM f
	 ORDER BY f.zoomlevel, f.x, f.y;	 
--	 
	 OPEN c1_maxzl;
	 FETCH c1_maxzl INTO new_zoomlevel;
	 CLOSE c1_maxzl;
--	 
	 RETURN new_zoomlevel;
END;
$BODY$
LANGUAGE plpgsql VOLATILE; 

DROP FUNCTION IF EXISTS tileMaker_intersector2();
CREATE OR REPLACE FUNCTION tileMaker_intersector2()
RETURNS INTEGER
AS
$BODY$
DECLARE
	max_gid INTEGER;
	c2_maxgid CURSOR FOR
		SELECT MAX(gid) AS max_gid
			  FROM intersection_cb_2014_us_500k;
BEGIN
	INSERT INTO intersection_cb_2014_us_500k(gid, zoomlevel, intersection)
	WITH a AS (
		SELECT COALESCE(MAX(gid), 0) AS max_gid
  		  FROM intersection_cb_2014_us_500k
	), b AS (
			SELECT c.gid , c.bbox, c.zoomlevel,
				   CASE	
						WHEN c.zoomlevel <= 6 THEN e.geom_6 
						WHEN c.zoomlevel = 7  THEN e.geom_7 
						WHEN c.zoomlevel = 8  THEN e.geom_8 
						WHEN c.zoomlevel = 9  THEN e.geom_9 
						WHEN c.zoomlevel = 10 THEN e.geom_10
						ELSE	                   e.geom_11 
				   END AS geom /* Will need to handle max_zoomlevel */
			  FROM a, tile_intersects_cb_2014_us_500k c, cb_2014_us_nation_5m e
			 WHERE c.gid BETWEEN a.max_gid+1 AND a.max_gid+100
	)
	SELECT b.gid, b.zoomlevel, ST_Intersection(b.geom, b.bbox) AS intersection
	  FROM b
	 ORDER BY b.gid;
--	 
	 OPEN c2_maxgid;
	 FETCH c2_maxgid INTO max_gid;
	 CLOSE c2_maxgid;
--	 
	 RETURN max_gid;
END;
$BODY$
LANGUAGE plpgsql VOLATILE; 

DROP FUNCTION IF EXISTS tileMaker_intersector3();
CREATE OR REPLACE FUNCTION tileMaker_intersector3()
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
		), b AS (
			SELECT MAX(gid) AS max_gid
				  FROM tile_intersects_cb_2014_us_500k
		)
		SELECT COALESCE(a.min_gid, b.max_gid) AS min_gid
		  FROM a, b;
BEGIN
	WITH a AS (
		SELECT COALESCE(MIN(gid), 0) AS min_gid
  		  FROM tile_intersects_cb_2014_us_500k
		 WHERE intersection IS NULL
	) 
	UPDATE tile_intersects_cb_2014_us_500k b
	   SET intersection = (SELECT a1.intersection
							 FROM intersection_cb_2014_us_500k a1
							WHERE a1.gid = b.gid)
	 WHERE intersection IS NULL AND EXISTS (SELECT c.gid
											  FROM intersection_cb_2014_us_500k c, a
											 WHERE c.gid = b.gid
											   AND c.gid BETWEEN a.min_gid AND a.min_gid+500);
	UPDATE tile_intersects_cb_2014_us_500k
	   SET optimised_geojson = ST_AsGeoJson(intersection)::JSON
	 WHERE optimised_geojson IS NULL AND intersection IS NOT NULL;
	UPDATE tile_intersects_cb_2014_us_500k b
	   SET png_tile = ST_AsPng(
							ST_AsRaster(
									ST_Transform(intersection, 3857 /* Spherical Mercator */), 
									256				/* Scale X */, 
									256				/* Scale Y */, 
									ARRAY['8BUI'] 	/* 8-bit unsigned integer pixeltype */, 
									ARRAY[1]		/* Value */, 
									ARRAY[0] 		/* nodataval */) 
								)
	 WHERE png_tile IS NULL AND intersection IS NOT NULL;
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
		 
INFO:  Processed zoomlevel: 11 in 1460.186944 seconds to:	

INFO:  Processed zoomlevel: 3 in 0.031965 seconds
INFO:  Processed zoomlevel: 4 in 0.080428 seconds
INFO:  Processed zoomlevel: 5 in 0.1256 seconds
INFO:  Processed zoomlevel: 6 in 0.193881 seconds
INFO:  Processed zoomlevel: 7 in 0.355169 seconds
INFO:  Processed zoomlevel: 8 in 0.882271 seconds
INFO:  Processed zoomlevel: 9 in 3.214994 seconds
INFO:  Processed zoomlevel: 10 in 12.710113 seconds
INFO:  Processed zoomlevel: 11 in 52.88089 seconds	 
DO
Time: ~70s
From before: 254 s

So actual speed gain ~5x. Note using correct geom_<zoomlevel>, not geom_6 as before
 */
DO LANGUAGE plpgsql $$
DECLARE
	new_zoomlevel 	INTEGER:=0;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
	max_zoomlevel 	INTEGER:=7;	/* 11 */ 
BEGIN
	WHILE new_zoomlevel <= (max_zoomlevel-1) LOOP
		stp2:=clock_timestamp();
		new_zoomlevel:=tileMaker_intersector();
		etp:=clock_timestamp();
		took:=age(etp, stp);
		took2:=age(etp, stp2);
		RAISE INFO 'Processed intersects for zoomlevel: % in %s, % total', 
			new_zoomlevel, ROUND(EXTRACT(EPOCH FROM took2)::NUMERIC, 1), ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1);
--
		REINDEX TABLE tile_intersects_cb_2014_us_500k;
		ANALYZE tile_intersects_cb_2014_us_500k;
	END LOOP;
--	RAISE EXCEPTION 'Stop';
END;
$$; 	

SELECT zoomlevel, 
       MIN(x) AS xmin, MIN(y) AS ymin, 
       MAX(x) AS xmax, MAX(y) AS ymax, 
	   (MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1) AS possible_tiles,
       COUNT(zoomlevel) AS tiles,
	   ROUND((((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1)-COUNT(zoomlevel))::numeric/
			((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1))::numeric)*100, 2) AS pct_saving
  FROM tile_intersects_cb_2014_us_500k
 GROUP BY zoomlevel
 ORDER BY 1;
/*
 zoomlevel | xmin | ymin | xmax | ymax | possible_tiles | tiles | pct_saving
-----------+------+------+------+------+----------------+-------+------------
         0 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
         1 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
         2 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
         3 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
         4 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
         5 |    0 |    6 |   31 |   17 |            384 |    46 |      88.02
         6 |    0 |   13 |   63 |   29 |           1088 |   112 |      89.71
         7 |    0 |   27 |  127 |   59 |           4224 |   338 |      92.00
         8 |    0 |   54 |  255 |  118 |          16640 |  1139 |      93.16
         9 |    1 |  108 |  511 |  237 |          66430 |  4093 |      93.84
        10 |    2 |  217 | 1023 |  474 |         263676 | 15308 |      94.19
        11 |    4 |  435 | 2046 |  948 |        1050102 | 58968 |      94.38
(12 rows)

Time: 33.048 ms
 */  

CREATE TEMPORARY TABLE rownum_cb_2014_us_500k 
AS
SELECT ROW_NUMBER() OVER (ORDER BY zoomlevel, x, y) AS gid, zoomlevel, x, y
	  FROM tile_intersects_cb_2014_us_500k;
ALTER TABLE rownum_cb_2014_us_500k 
	ADD CONSTRAINT rownum_cb_2014_us_500k_pk PRIMARY KEY (zoomlevel, x, y);	
ANALYZE rownum_cb_2014_us_500k;
UPDATE tile_intersects_cb_2014_us_500k b
   SET gid = (SELECT a.gid
                FROM rownum_cb_2014_us_500k a
			   WHERE a.zoomlevel = b.zoomlevel
   			     AND a.x         = b.x
				 AND a.y         = b.y);				 
ALTER TABLE tile_intersects_cb_2014_us_500k 
	ADD CONSTRAINT tile_intersects_cb_2014_us_500k_uk UNIQUE (gid);	
ANALYZE tile_intersects_cb_2014_us_500k;	
DROP TABLE rownum_cb_2014_us_500k;

DROP TABLE IF EXISTS intersection_cb_2014_us_500k;
CREATE TEMPORARY TABLE intersection_cb_2014_us_500k 
AS	
WITH a AS (
		SELECT c.gid , c.bbox, c.zoomlevel,
		       CASE	
					WHEN c.zoomlevel <= 6 THEN e.geom_6 
					WHEN c.zoomlevel = 7  THEN e.geom_7 
					WHEN c.zoomlevel = 8  THEN e.geom_8 
					WHEN c.zoomlevel = 9  THEN e.geom_9 
					WHEN c.zoomlevel = 10 THEN e.geom_10
					ELSE	                   e.geom_11 
			   END AS geom /* Will need to handle max_zoomlevel */
		  FROM tile_intersects_cb_2014_us_500k c, cb_2014_us_nation_5m e
		 WHERE c.zoomlevel = 0
)
SELECT a.gid, a.zoomlevel, ST_Intersection(a.geom, a.bbox) AS intersection  
  FROM a
 ORDER BY gid;
DELETE FROM intersection_cb_2014_us_500k;

DO LANGUAGE plpgsql $$
DECLARE
	new_max_gid INTEGER=0;
	max_gid INTEGER=0;
	c3_maxgid CURSOR FOR
		SELECT MAX(gid) AS max_zoomlevel
			  FROM tile_intersects_cb_2014_us_500k;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
	pct 			NUMERIC;
	tps				NUMERIC;
BEGIN
	 OPEN c3_maxgid;
	 FETCH c3_maxgid INTO max_gid;
	 CLOSE c3_maxgid;
--
	WHILE new_max_gid < max_gid LOOP
		stp2:=clock_timestamp();
		new_max_gid:=tileMaker_intersector2();
		etp:=clock_timestamp();
		took:=age(etp, stp);
		took2:=age(etp, stp2);
		pct:=ROUND((new_max_gid::NUMERIC/max_gid::NUMERIC)*100, 2);
		tps:=ROUND((new_max_gid::NUMERIC/EXTRACT(EPOCH FROM took)::NUMERIC), 0);
		RAISE INFO 'Processed %/% intersections; % %% in %s; % tiles/s', 
			new_max_gid, 
			max_gid, 
			pct, 
			ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
			tps;
--
		REINDEX TABLE intersection_cb_2014_us_500k;
		ANALYZE intersection_cb_2014_us_500k;
	END LOOP;
--	RAISE EXCEPTION 'Stop';
END;
$$;
 
ALTER TABLE intersection_cb_2014_us_500k 
	ADD CONSTRAINT intersection_cb_2014_us_500k_pk PRIMARY KEY (gid);	
ANALYZE tile_intersects_cb_2014_us_500k;

DO LANGUAGE plpgsql $$
DECLARE
	new_max_gid INTEGER=0;
	max_gid INTEGER=0;
	c3_maxgid CURSOR FOR
		SELECT MAX(gid) AS max_zoomlevel
			  FROM tile_intersects_cb_2014_us_500k;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
	pct 			NUMERIC;
	tps				NUMERIC;
BEGIN
	 OPEN c3_maxgid;
	 FETCH c3_maxgid INTO max_gid;
	 CLOSE c3_maxgid;
--
	WHILE new_max_gid < max_gid LOOP
		stp2:=clock_timestamp();
		new_max_gid:=tileMaker_intersector3();
		etp:=clock_timestamp();
		took:=age(etp, stp);
		took2:=age(etp, stp2);
		pct:=ROUND((new_max_gid::NUMERIC/max_gid::NUMERIC)*100, 2);
		tps:=ROUND((new_max_gid::NUMERIC/EXTRACT(EPOCH FROM took)::NUMERIC), 0);
		RAISE INFO 'Processed %/% tiles (GeoJSON/PNG); % %% in %s; % tiles/s', 
			new_max_gid, 
			max_gid, 
			pct, 
			ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
			tps;
	END LOOP;
--	RAISE EXCEPTION 'Stop';
END;
$$;

REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE tile_intersects_cb_2014_us_500k;
						 
DROP TABLE intersection_cb_2014_us_500k; 

END;

\dS+ tile_intersects_cb_2014_us_500k
SELECT ST_Srid(intersection) FROM tile_intersects_cb_2014_us_500k LIMIT 1;

/*
SELECT lo_from_bytea(0, png_tile) FROM tile_intersects_cb_2014_us_500k WHERE gid=1;
\lo_export 4194857 'tile_1.png' 
 lo_from_bytea
---------------
       4194857
\lo_list
\lo_unlink 4194857
 */