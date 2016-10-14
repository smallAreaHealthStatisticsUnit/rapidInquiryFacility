\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

BEGIN;

DROP TABLE IF EXISTS geometry_cb_2014_us_500k;
CREATE TABLE geometry_cb_2014_us_500k
AS
SELECT 1 geolevel_id,
       areaid, 
       6 AS zoomlevel, 
       geom_6 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel_id,
       areaid, 
       7 AS zoomlevel, 
       geom_7 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel_id,
       areaid, 
       8 AS zoomlevel, 
       geom_8 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel_id,
       areaid, 
       9 AS zoomlevel, 
       geom_9 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel_id,
       areaid, 
       10 AS zoomlevel, 
       geom_10 AS geom
  FROM cb_2014_us_nation_5m
UNION
SELECT 1 geolevel_id,
       areaid, 
       11 AS zoomlevel, 
       geom_11 AS geom
  FROM cb_2014_us_nation_5m
UNION  
SELECT 2 geolevel_id,
       areaid, 
       6 AS zoomlevel, 
       geom_6 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel_id,
       areaid, 
       7 AS zoomlevel, 
       geom_7 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel_id,
       areaid, 
       8 AS zoomlevel, 
       geom_8 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel_id,
       areaid, 
       9 AS zoomlevel, 
       geom_9 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel_id,
       areaid, 
       10 AS zoomlevel, 
       geom_10 AS geom
  FROM cb_2014_us_state_500k
UNION
SELECT 2 geolevel_id,
       areaid, 
       11 AS zoomlevel, 
       geom_11 AS geom
  FROM cb_2014_us_state_500k  
UNION  
SELECT 3 geolevel_id,
       areaid, 
       6 AS zoomlevel, 
       geom_6 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel_id,
       areaid, 
       7 AS zoomlevel, 
       geom_7 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel_id,
       areaid, 
       8 AS zoomlevel, 
       geom_8 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel_id,
       areaid, 
       9 AS zoomlevel, 
       geom_9 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel_id,
       areaid, 
       10 AS zoomlevel, 
       geom_10 AS geom
  FROM cb_2014_us_county_500k
UNION
SELECT 3 geolevel_id,
       areaid, 
       11 AS zoomlevel, 
       geom_11 AS geom
  FROM cb_2014_us_county_500k 
 ORDER BY 1, 3, 2;
 
ALTER TABLE geometry_cb_2014_us_500k 
	ADD CONSTRAINT geometry_cb_2014_us_500k_pk PRIMARY KEY (geolevel_id, zoomlevel, areaid);	
CREATE INDEX geometry_cb_2014_us_500k_geom_gix ON geometry_cb_2014_us_500k USING GIST (geom);	
ANALYZE geometry_cb_2014_us_500k;
-- Convert to IOT
CLUSTER VERBOSE geometry_cb_2014_us_500k USING geometry_cb_2014_us_500k_pk;  

COMMENT ON TABLE geometry_cb_2014_us_500k IS 'All geolevels geometry combined into a single table';
COMMENT ON COLUMN geometry_cb_2014_us_500k.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
COMMENT ON COLUMN geometry_cb_2014_us_500k.areaid IS 'Area ID.';
COMMENT ON COLUMN geometry_cb_2014_us_500k.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT ON COLUMN geometry_cb_2014_us_500k.geom IS 'Geometry data in SRID 4326 (WGS84).';
\dS+ geometry_cb_2014_us_500k

SELECT geolevel_id, areaid, COUNT(zoomlevel) AS zoomlevels
  FROM geometry_cb_2014_us_500k
 WHERE geolevel_id IN (1, 2)
 GROUP BY geolevel_id, areaid
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
			LEFT OUTER JOIN geometry_cb_2014_us_500k b ON (b.geolevel_id = 1 AND a.zoomlevel = b.zoomlevel)
), c AS (
        SELECT b.zoomlevel,
		       ST_XMax(b.geom) AS Xmax,
		       ST_XMin(b.geom) AS Xmin,
		       ST_YMax(b.geom) AS Ymax,
		       ST_YMin(b.geom) AS Ymin
      FROM geometry_cb_2014_us_500k b
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
  FROM d;

ALTER TABLE tile_limits_cb_2014_us_500k 
	ADD CONSTRAINT tile_limits_cb_2014_us_500k_pk PRIMARY KEY (zoomlevel);		
ANALYZE tile_limits_cb_2014_us_500k;
SELECT * FROM tile_limits_cb_2014_us_500k;

COMMENT ON TABLE tile_limits_cb_2014_us_500k IS 'Tile limits';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.x_min IS 'Min X (longitude)';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.x_max IS 'Max X (longitude)';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.y_min IS 'Min Y (latitude)';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.y_max IS 'Max Y (latitude)';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.bbox IS 'Bounding box polygon for geolevel_id 1 area';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.x_mintile IS 'Min X tile number (longitude)';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.x_maxtile IS 'Max X tile number (longitude)';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.y_mintile IS 'Min Y tile number (latitude)';
COMMENT ON COLUMN tile_limits_cb_2014_us_500k.y_maxtile IS 'Max Y tile number (latitude)';
\dS+ tile_limits_cb_2014_us_500k

/*
 zoomlevel |         xmin     |         xmax     |          ymin     |      ymax | y_mintile | y_maxtile | x_mintile | x_maxtile
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
		   c.geolevel_id,
		   c.areaid,
		   c.geom
	  FROM b, geometry_cb_2014_us_500k c
	 WHERE ((b.zoomlevel = c.zoomlevel AND b.zoomlevel BETWEEN 6 AND 11) OR 
		    (c.zoomlevel = 6           AND b.zoomlevel NOT BETWEEN 6 AND 11))
	   AND ST_Intersects(ST_MakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326), c.geom) /* intersects */
)
SELECT 0 gid,
       c.geolevel_id,
	   c.zoomlevel, 
	   c.areaid,
	   c.x, 
	   c.y, 
	   c.bbox,
	   c.geom,
       ST_AsGeoJson(c.geom)::JSON AS optimised_geojson,
	   ST_Within(c.bbox, c.geom) AS within
  FROM c
 ORDER BY c.geolevel_id, c.zoomlevel, c.x, c.y;
 
/*
 zoomlevel | x | y |                                                                             bbox
-----------+---+---+--------------------------------------------------------------------------------------------------------------------------------------------------------------
         0 | 0 | 0 | {"type":"Polygon","coordinates":[[[-180,85.0511287794693],[-180,-85.0511287794693],[180,-85.0511287794693],[180,85.0511287794693],[-180,85.0511287794693]]]}
(1 row)
 */  
 
ALTER TABLE tile_intersects_cb_2014_us_500k 
	ADD CONSTRAINT tile_intersects_cb_2014_us_500k_pk PRIMARY KEY (geolevel_id, zoomlevel, areaid, x, y);

-- PK is v.v slightly faster than the 3x hash
--CREATE INDEX tile_intersects_cb_2014_us_500k_zoomlevel ON tile_intersects_cb_2014_us_500k USING HASH(zoomlevel);
--CREATE INDEX tile_intersects_cb_2014_us_500k_x ON tile_intersects_cb_2014_us_500k USING HASH(x);
--CREATE INDEX tile_intersects_cb_2014_us_500k_y ON tile_intersects_cb_2014_us_500k USING HASH(y);
--REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE VERBOSE tile_intersects_cb_2014_us_500k;  

DROP FUNCTION IF EXISTS tileMaker_intersector(INTEGER, INTEGER);
CREATE OR REPLACE FUNCTION tileMaker_intersector(l_geolevel_id INTEGER, l_zoomlevel INTEGER)
RETURNS INTEGER
AS
$BODY$
DECLARE
	num_rows INTEGER;
BEGIN
	INSERT INTO tile_intersects_cb_2014_us_500k(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
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
		SELECT c.zoomlevel, c.x, c.y, c.bbox, p.areaid, p.within
		  FROM c, tile_intersects_cb_2014_us_500k p /* Parent */
		 WHERE p.geolevel_id    = l_geolevel_id
		   AND c.zoomlevel-1 = p.zoomlevel 	/* Join to parent tile from previous geolevel_id; i.e. exclude if not present */
		   AND c.parent_xmin = p.x  
	       AND c.parent_ymin = p.y	
	), e AS (
		SELECT d.zoomlevel, d.x, d.y, d.bbox, d.areaid
		  FROM d
		 WHERE NOT EXISTS (SELECT c2.areaid
						    FROM tile_intersects_cb_2014_us_500k c2
						   WHERE c2.geolevel_id  = l_geolevel_id
						     AND c2.zoomlevel = d.zoomlevel
							 AND c2.x         = d.x
							 AND c2.y         = d.y
							 AND c2.areaid    = d.areaid)
	), f AS (
		SELECT e.zoomlevel, l_geolevel_id AS geolevel_id, e.x, e.y, e.bbox, e2.areaid, e2.geom
		  FROM e, geometry_cb_2014_us_500k e2
		 WHERE ((e.zoomlevel  = e2.zoomlevel AND e.zoomlevel BETWEEN 6 AND 11) OR 
		        (e2.zoomlevel = 6            AND e.zoomlevel NOT BETWEEN 6 AND 11))
		   AND e2.geolevel_id  = l_geolevel_id
		   AND e2.areaid    = e.areaid
		   AND (e.bbox && e2.geom) 			  /* Intersect by bounding box */
		   AND ST_Intersects(e.bbox, e2.geom) /* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
	)
	SELECT f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y, f.bbox, f.geom, 
	       ST_AsGeoJson(f.geom)::JSON AS optimised_geojson,
	       true::BOOLEAN AS within
	  FROM f
	 WHERE NOT ST_Within(f.bbox, f.geom) /* Exclude any tile bounding completely within the area */
	 ORDER BY f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y;	 
	 GET DIAGNOSTICS num_rows = ROW_COUNT;
--	 
	 RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE; 

COMMENT ON FUNCTION tileMaker_intersector(INTEGER, INTEGER) IS '
Function:	 tileMaker_intersector2()
Parameters:	 geolevel ID, zoomlevel
Returns:	 Number of rows inserted
Description: Insert tile area id intersections.  
';

DROP FUNCTION IF EXISTS tileMaker_intersector2(INTEGER, INTEGER);
CREATE OR REPLACE FUNCTION tileMaker_intersector2(l_geolevel_id INTEGER, l_zoomlevel INTEGER)
RETURNS INTEGER
AS
$BODY$
DECLARE
	num_rows INTEGER;
BEGIN
	INSERT INTO tile_intersects_cb_2014_us_500k(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
	WITH a AS (
		SELECT DISTINCT geolevel_id, areaid
		  FROM geometry_cb_2014_us_500k
		 WHERE geolevel_id = l_geolevel_id
		EXCEPT 
		SELECT DISTINCT geolevel_id, areaid
		  FROM tile_intersects_cb_2014_us_500k a
		 WHERE zoomlevel = l_zoomlevel
	), b AS (
		SELECT a.geolevel_id, a.areaid, ST_Envelope(b.geom) AS bbox, b.geom
		  FROM a, geometry_cb_2014_us_500k b
		 WHERE a.geolevel_id = b.geolevel_id
		   AND a.areaid   = b.areaid
		   AND NOT ST_IsEmpty(b.geom)
	), c AS (
		SELECT l_zoomlevel AS zoomlevel, 
			   b.geolevel_id, b.areaid,
			   tileMaker_latitude2tile(ST_Ymin(bbox), l_zoomlevel) AS y_mintile,
			   tileMaker_longitude2tile(ST_Xmin(bbox), l_zoomlevel) AS x_mintile,
			   tileMaker_latitude2tile(ST_Ymax(bbox), l_zoomlevel) AS y_maxtile,
			   tileMaker_longitude2tile(ST_Xmax(bbox), l_zoomlevel) AS x_maxtile,
			   b.geom
		   FROM b
	), x AS (
		SELECT c.zoomlevel, 
			   c.geolevel_id, 
			   c.areaid,
			   generate_series(x_mintile, x_maxtile) AS x_series
		  FROM c
	), y AS (	 
		SELECT c.zoomlevel, 
			   c.geolevel_id, 
			   c.areaid,
			   generate_series(y_mintile, y_maxtile) AS y_series	
		  FROM c 
	), d AS (
		SELECT x.zoomlevel, 
			   x.geolevel_id, 
			   x.areaid,
			   x.x_series AS x, 
			   y.y_series AS y,      
			   tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
			   tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
			   tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
			   tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
		  FROM x, y
		 WHERE x.zoomlevel = y.zoomlevel	
		   AND x.geolevel_id  = y.geolevel_id
		   AND x.areaid    = y.areaid
	), e AS (
		SELECT d.zoomlevel, 
			   d.geolevel_id, 
			   d.areaid,
			   d.x,
			   d.y, 
			   ST_MakeEnvelope(d.xmin, d.ymin, d.xmax, d.ymax, 4326) AS bbox
		  FROM d
	), f AS (
		SELECT DISTINCT e.zoomlevel, 
			   e.geolevel_id, 
			   e.areaid, 
			   e.x,
			   e.y,
			   e.bbox
		  FROM e
		 WHERE NOT EXISTS (SELECT c2.areaid
							 FROM tile_intersects_cb_2014_us_500k c2
							WHERE c2.geolevel_id  = e.geolevel_id
							  AND c2.zoomlevel = e.zoomlevel
							  AND c2.x         = e.x
							  AND c2.y         = e.y	
							  AND c2.areaid    = e.areaid)
	), g AS (
			SELECT f.zoomlevel, f.geolevel_id, f.x, f.y, f.bbox, e2.areaid, e2.geom
			  FROM f, geometry_cb_2014_us_500k e2
			 WHERE ((f.zoomlevel  = e2.zoomlevel AND f.zoomlevel BETWEEN 6 AND 11) OR 
					(e2.zoomlevel = 6            AND f.zoomlevel NOT BETWEEN 6 AND 11))
			   AND e2.geolevel_id  = f.geolevel_id
			   AND e2.areaid    = f.areaid
			   AND (f.bbox && e2.geom) 			  /* Intersect by bounding box */
			   AND ST_Intersects(f.bbox, e2.geom) /* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
	)
	SELECT geolevel_id, zoomlevel, areaid, x, y, bbox, geom,	
	       ST_AsGeoJson(g.geom)::JSON AS optimised_geojson,
	       ST_Within(g.bbox, g.geom) AS within
 	  FROM g 
	 ORDER BY geolevel_id, zoomlevel, areaid, x, y;	 
	 GET DIAGNOSTICS num_rows = ROW_COUNT;
--	 
	 RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;
 
COMMENT ON FUNCTION tileMaker_intersector2(INTEGER, INTEGER) IS '
Function:	 tileMaker_intersector2()
Parameters:	 geolevel ID, zoomlevel
Returns:	 Number of rows inserted
Description: Insert any missing area ids if possible (i.e. have non empty geometry). This is caused by small areas, usually islands,
             being simplified out of existance at a lower zoomlevel.  
';
/*								  		 
psql:tile-maker.sql:550: INFO:  Processed 3+0 total areaid intersects for geolevel 1/3 zoomlevel: 1/11 in 0.1s, 0.1s total; 22.3 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 5+0 total areaid intersects for geolevel 1/3 zoomlevel: 2/11 in 0.2s, 0.4s total; 21.1 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 10+0 total areaid intersects for geolevel 1/3 zoomlevel: 3/11 in 0.4s, 0.8s total; 25.2 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 22+0 total areaid intersects for geolevel 1/3 zoomlevel: 4/11 in 0.8s, 1.5s total; 28.6 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 42+0 total areaid intersects for geolevel 1/3 zoomlevel: 5/11 in 1.5s, 3.0s total; 28.7 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 84+0 total areaid intersects for geolevel 1/3 zoomlevel: 6/11 in 2.9s, 5.9s total; 29.0 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 190+0 total areaid intersects for geolevel 1/3 zoomlevel: 7/11 in 8.6s, 14.5s total; 22.1 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 432+0 total areaid intersects for geolevel 1/3 zoomlevel: 8/11 in 26.1s, 40.6s total; 16.6 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 1003+0 total areaid intersects for geolevel 1/3 zoomlevel: 9/11 in 83.5s, 124.1s total; 12.0 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 2314+0 total areaid intersects for geolevel 1/3 zoomlevel: 10/11 in 270.7s, 394.7s total; 8.5 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 5240+0 total areaid intersects for geolevel 1/3 zoomlevel: 11/11 in 833.4s, 1228.1s total; 6.3 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 57+0 total areaid intersects for geolevel 2/3 zoomlevel: 1/11 in 2.5s, 1230.6s total; 22.8 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 67+0 total areaid intersects for geolevel 2/3 zoomlevel: 2/11 in 1.9s, 1232.4s total; 35.6 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 81+0 total areaid intersects for geolevel 2/3 zoomlevel: 3/11 in 1.5s, 1234.0s total; 53.4 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 95+0 total areaid intersects for geolevel 2/3 zoomlevel: 4/11 in 2.2s, 1236.2s total; 42.8 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 142+0 total areaid intersects for geolevel 2/3 zoomlevel: 5/11 in 3.9s, 1240.1s total; 36.6 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 241+0 total areaid intersects for geolevel 2/3 zoomlevel: 6/11 in 6.8s, 1246.9s total; 35.4 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 485+0 total areaid intersects for geolevel 2/3 zoomlevel: 7/11 in 19.5s, 1266.4s total; 24.8 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 1018+0 total areaid intersects for geolevel 2/3 zoomlevel: 8/11 in 69.1s, 1335.5s total; 14.7 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 2246+0 total areaid intersects for geolevel 2/3 zoomlevel: 9/11 in 245.9s, 1581.4s total; 9.1 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 4901+0 total areaid intersects for geolevel 2/3 zoomlevel: 10/11 in 857.4s, 2438.8s total; 5.7 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 10718+0 total areaid intersects for geolevel 2/3 zoomlevel: 11/11 in 2594.4s, 5033.1s total; 4.1 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 3233+0 total areaid intersects for geolevel 3/3 zoomlevel: 1/11 in 58.2s, 5091.4s total; 55.5 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 3291+0 total areaid intersects for geolevel 3/3 zoomlevel: 2/11 in 46.0s, 5137.3s total; 71.6 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 3390+0 total areaid intersects for geolevel 3/3 zoomlevel: 3/11 in 23.8s, 5161.1s total; 142.6 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 3439+0 total areaid intersects for geolevel 3/3 zoomlevel: 4/11 in 16.0s, 5177.1s total; 214.7 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 3657+0 total areaid intersects for geolevel 3/3 zoomlevel: 5/11 in 16.7s, 5193.8s total; 219.5 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 4063+3 total areaid intersects for geolevel 3/3 zoomlevel: 6/11 in 12.7s, 5206.5s total; 320.3 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 4986+0 total areaid intersects for geolevel 3/3 zoomlevel: 7/11 in 10.0s, 5216.5s total; 496.9 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 7124+3 total areaid intersects for geolevel 3/3 zoomlevel: 8/11 in 15.3s, 5231.8s total; 464.7 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 12268+5 total areaid intersects for geolevel 3/3 zoomlevel: 9/11 in 35.8s, 5267.6s total; 343.1 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 24459+0 total areaid intersects for geolevel 3/3 zoomlevel: 10/11 in 100.4s, 5368.0s total; 243.5 intesects/s
psql:tile-maker.sql:550: INFO:  Processed 51437+1 total areaid intersects for geolevel 3/3 zoomlevel: 11/11 in 336.5s, 5704.6s total; 152.8 intesects/s
DO
Time: 5704568.058 ms
 */
DO LANGUAGE plpgsql $$
DECLARE
	max_geolevel_id	INTEGER;
	max_zoomlevel 	INTEGER;
	c1_maxgeolevel_id 	CURSOR FOR
		SELECT MAX(geolevel_id) AS max_geolevel_id
			  FROM tile_intersects_cb_2014_us_500k;		  
--
	num_rows 		INTEGER:=0;
	num_rows2 		INTEGER:=0;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
--
	tiles_per_s		NUMERIC;
BEGIN
	OPEN c1_maxgeolevel_id;
	FETCH c1_maxgeolevel_id INTO max_geolevel_id;
	CLOSE c1_maxgeolevel_id;
--	 
	max_zoomlevel 	:=6;
--
-- Timing; 3 zoomlevels to:
--
-- Zoomlevel 7: 3 minutes
-- Zoomlevel 8: 5 minutes (321)
-- Zoomlevel 9: 11 minutes (673)
-- Zoomlevel 11: 95 minutes (5704)
--
	FOR i IN 1 .. max_geolevel_id LOOP
		FOR j IN 1 .. max_zoomlevel LOOP
			stp2:=clock_timestamp();
			num_rows:=tileMaker_intersector(i, j);
			num_rows2:=tileMaker_intersector2(i, j);
			etp:=clock_timestamp();
			took:=age(etp, stp);
			took2:=age(etp, stp2);
			tiles_per_s:=ROUND(num_rows::NUMERIC/EXTRACT(EPOCH FROM took2)::NUMERIC, 1);
			RAISE INFO 'Processed %+% total areaid intersects for geolevel id %/% zoomlevel: %/% in %s, %s total; % intesects/s', 
				num_rows, num_rows2, i, max_geolevel_id, j, max_zoomlevel, 
				ROUND(EXTRACT(EPOCH FROM took2)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
				tiles_per_s;			
		END LOOP;	
	END LOOP;
END;
$$; 	

REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE tile_intersects_cb_2014_us_500k;
	
SELECT DISTINCT geolevel_id, areaid
  FROM geometry_cb_2014_us_500k
EXCEPT 
SELECT DISTINCT geolevel_id, areaid
  FROM tile_intersects_cb_2014_us_500k a
 WHERE zoomlevel = (SELECT MAX(zoomlevel) AS max_zoomlevel
					  FROM tile_intersects_cb_2014_us_500k);
/*
 geolevel_id |  areaid
-------------+----------
           3 | 01805243
(1 row)
 */	
-- Missing area IDs
WITH a AS (
	SELECT DISTINCT geolevel_id, areaid
	  FROM geometry_cb_2014_us_500k
	EXCEPT 
	SELECT DISTINCT geolevel_id, areaid
	  FROM tile_intersects_cb_2014_us_500k
)
SELECT a.geolevel_id, a.areaid, g.zoomlevel, ST_IsEmpty(g.geom) AS is_empty
  FROM a
	LEFT OUTER JOIN geometry_cb_2014_us_500k g ON (a.areaid = g.areaid AND a.geolevel_id = g.geolevel_id)
 ORDER BY 1, 2, 3;
/*
 geolevel_id |  areaid  | zoomlevel | is_empty
-------------+----------+-----------+----------
           3 | 01805243 |         6 | t
           3 | 01805243 |         7 | t
           3 | 01805243 |         8 | t
           3 | 01805243 |         9 | f
           3 | 01805243 |        10 | f
           3 | 01805243 |        11 | f
(6 rows)
 */
 
SELECT geolevel_id, zoomlevel, 
       COUNT(DISTINCT(areaid)) AS areas,
       MIN(x) AS xmin, MIN(y) AS ymin, 
       MAX(x) AS xmax, MAX(y) AS ymax, 
	   (MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1) AS possible_tiles,
       COUNT(DISTINCT(x::Text||y::Text)) AS tiles,
	   ROUND((((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1)-COUNT(DISTINCT(x::Text||y::Text)))::numeric/
			((MAX(x)-MIN(x)+1)*(MAX(y)-MIN(y)+1))::numeric)*100, 2) AS pct_saving
  FROM tile_intersects_cb_2014_us_500k
 GROUP BY geolevel_id, zoomlevel
 ORDER BY 1, 2;
 
/*
with within restrictor:

 geolevel_id | zoomlevel | areas | xmin | ymin | xmax | ymax | possible_tiles | tiles | pct_saving
----------+-----------+-------+------+------+------+------+----------------+-------+------------
        1 |         0 |     1 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
        1 |         1 |     1 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
        1 |         2 |     1 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
        1 |         3 |     1 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
        1 |         4 |     1 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
        1 |         5 |     1 |    0 |    6 |   31 |   17 |            384 |    42 |      89.06
        1 |         6 |     1 |    0 |   13 |   63 |   29 |           1088 |    84 |      92.28
        1 |         7 |     1 |    0 |   27 |  127 |   59 |           4224 |   190 |      95.50
        1 |         8 |     1 |    0 |   54 |  255 |  118 |          16640 |   432 |      97.40
        1 |         9 |     1 |    1 |  108 |  511 |  237 |          66430 |  1003 |      98.49
        2 |         0 |    56 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
        2 |         1 |    56 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
        2 |         2 |    56 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
        2 |         3 |    56 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
        2 |         4 |    56 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
        2 |         5 |    56 |    0 |    6 |   31 |   17 |            384 |    47 |      87.76
        2 |         6 |    56 |    0 |   13 |   63 |   34 |           1408 |   110 |      92.19
        2 |         7 |    56 |    0 |   27 |  127 |   69 |           5504 |   279 |      94.93
        2 |         8 |    56 |    0 |   54 |  255 |  135 |          20992 |   663 |      96.84
        2 |         9 |    56 |    1 |  108 |  511 |  271 |          83804 |  1566 |      98.13
        3 |         0 |  3232 |    0 |    0 |    0 |    0 |              1 |     1 |       0.00
        3 |         1 |  3232 |    0 |    0 |    1 |    1 |              4 |     3 |      25.00
        3 |         2 |  3232 |    0 |    0 |    3 |    2 |             12 |     5 |      58.33
        3 |         3 |  3232 |    0 |    1 |    7 |    4 |             32 |    10 |      68.75
        3 |         4 |  3232 |    0 |    3 |   15 |    8 |             96 |    22 |      77.08
        3 |         5 |  3232 |    0 |    6 |   31 |   17 |            384 |    48 |      87.50
        3 |         6 |  3232 |    0 |   13 |   63 |   34 |           1408 |   117 |      91.69
        3 |         7 |  3232 |    0 |   27 |  127 |   69 |           5504 |   330 |      94.00
        3 |         8 |  3232 |    0 |   54 |  255 |  138 |          21760 |   989 |      95.45
        3 |         9 |  3233 |    1 |  108 |  511 |  276 |          86359 |  3135 |      96.37
(30 rows)

 */  
		  
CREATE TEMPORARY TABLE rownum_cb_2014_us_500k 
AS
SELECT ROW_NUMBER() OVER (ORDER BY geolevel_id, zoomlevel, areaid, x, y) AS gid, geolevel_id, zoomlevel, areaid, x, y
  FROM tile_intersects_cb_2014_us_500k
 ORDER BY 1;
ALTER TABLE rownum_cb_2014_us_500k 
	ADD CONSTRAINT rownum_cb_2014_us_500k_pk PRIMARY KEY (geolevel_id, zoomlevel, areaid, x, y);	
ANALYZE rownum_cb_2014_us_500k;
UPDATE tile_intersects_cb_2014_us_500k b
   SET gid = (SELECT a.gid
                FROM rownum_cb_2014_us_500k a
			   WHERE a.zoomlevel = b.zoomlevel
			     AND a.geolevel_id  = b.geolevel_id
   			     AND a.x         = b.x
				 AND a.y         = b.y
				 AND a.areaid    = b.areaid);				 
ALTER TABLE tile_intersects_cb_2014_us_500k 
	ADD CONSTRAINT tile_intersects_cb_2014_us_500k_uk UNIQUE (gid);	
DROP TABLE rownum_cb_2014_us_500k;

SELECT geolevel_id, MIN(gid) AS min_gid, MAX(gid) AS max_gid
  FROM tile_intersects_cb_2014_us_500k
  GROUP BY geolevel_id
  ORDER BY 1;

REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE tile_intersects_cb_2014_us_500k;

SELECT geolevel_id, zoomlevel, within, COUNT(gid)
  FROM tile_intersects_cb_2014_us_500k
 GROUP BY geolevel_id, zoomlevel, within
 ORDER BY geolevel_id, zoomlevel, within;
  
COMMENT ON TABLE tile_intersects_cb_2014_us_500k IS 'Tile areaid intersections';COMMENT ON COLUMN tiles_cb_2014_us_500k.geography IS 'Geography';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.gid IS 'Primary key.';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.bbox IS 'Bounding box of tile as a polygon.';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.geom IS 'Geometry of area.';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.areaid IS 'Tile contains no area_ids flag: 0/1';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.within IS 'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
COMMENT ON COLUMN tile_intersects_cb_2014_us_500k.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N. ';

\dS+ tile_intersects_cb_2014_us_500k

DROP VIEW IF EXISTS tiles_cb_2014_us_500k;
DROP TABLE IF EXISTS t_tiles_cb_2014_us_500k;
CREATE TABLE t_tiles_cb_2014_us_500k
AS
SELECT a.geolevel_id,
	   zoomlevel,
	   x, 
	   y,
	   json_agg(optimised_geojson) AS optimised_geojson,
	   NULL::JSON AS optimised_topojson,
	   a.geolevel_id::Text||'_'||b.geolevel_name||'_'||a.zoomlevel||'_'||a.x::Text||'_'||a.y::Text AS tile_id
  FROM tile_intersects_cb_2014_us_500k a, geolevels_cb_2014_us_500k b
 WHERE a.geolevel_id = b.geolevel_id 
 GROUP BY a.geolevel_id,
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
COMMENT ON COLUMN tiles_cb_2014_us_500k.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT ON COLUMN tiles_cb_2014_us_500k.geolevel_name IS 'Name of geolevel. This will be a column name in the numerator/denominator tables';
COMMENT ON COLUMN tiles_cb_2014_us_500k.no_area_ids IS 'Tile contains no area_ids flag: 0/1';
COMMENT ON COLUMN tiles_cb_2014_us_500k.tile_id IS 'Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>';
COMMENT ON COLUMN tiles_cb_2014_us_500k.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT ON COLUMN tiles_cb_2014_us_500k.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1';
COMMENT ON COLUMN tiles_cb_2014_us_500k.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.';
COMMENT ON COLUMN tiles_cb_2014_us_500k.optimised_geojson IS 'Tile multipolygon in GeoJSON format, optimised for zoomlevel N. ';
COMMENT ON COLUMN tiles_cb_2014_us_500k.optimised_topojson IS 'Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.';

\dS+ tiles_cb_2014_us_500k
 
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

 