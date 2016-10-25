\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

BEGIN;

EXPLAIN ANALYZE INSERT INTO tile_intersects_cb_2014_us_500k (
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
--	 WHERE ((b.zoomlevel = c.zoomlevel AND b.zoomlevel BETWEEN 6 AND 11) OR 
--		    (c.zoomlevel = 6           AND b.zoomlevel NOT BETWEEN 6 AND 11))
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
	
SELECT geolevel_id,
	   zoomlevel, 
	   areaid,
	   x, 
	   y, 
	   within,
	   ST_AsGeoJson(bbox) AS bbox
  FROM tile_intersects_cb_2014_us_500k
 WHERE zoomlevel = 0 AND geolevel_id = 1;

-- PK is v.v slightly faster than the 3x hash
--CREATE INDEX tile_intersects_cb_2014_us_500k_zoomlevel ON tile_intersects_cb_2014_us_500k USING HASH(zoomlevel);
--CREATE INDEX tile_intersects_cb_2014_us_500k_x ON tile_intersects_cb_2014_us_500k USING HASH(x);
--CREATE INDEX tile_intersects_cb_2014_us_500k_y ON tile_intersects_cb_2014_us_500k USING HASH(y);
--REINDEX TABLE tile_intersects_cb_2014_us_500k;
ANALYZE VERBOSE tile_intersects_cb_2014_us_500k;  

DROP FUNCTION IF EXISTS tileMaker_intersector(INTEGER, INTEGER, INTEGER, BOOLEAN);
CREATE OR REPLACE FUNCTION tileMaker_intersector(
	l_geolevel_id INTEGER, 
	l_zoomlevel INTEGER, 
	l_use_zoomlevel INTEGER, 
	l_debug BOOLEAN DEFAULT FALSE)
RETURNS INTEGER
AS
$BODY$
DECLARE
	c1_i1	CURSOR FOR
		SELECT COUNT(areaid) AS total
		  FROM tile_intersects_cb_2014_us_500k
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel;
--
	num_rows INTEGER;
	explain_line	text;
	explain_text	text:='';
BEGIN
	FOR explain_line IN EXPLAIN ANALYZE 
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
		 WHERE p.geolevel_id = l_geolevel_id
		   AND p.zoomlevel 	 = l_zoomlevel -1/* Join to parent tile from previous geolevel_id; i.e. exclude if not present */
		   AND c.parent_xmin = p.x  
	       AND c.parent_ymin = p.y	
	), e AS (
		SELECT d.zoomlevel, d.x, d.y, d.bbox, d.areaid
		  FROM d
		 WHERE NOT EXISTS (SELECT c2.areaid
						     FROM tile_intersects_cb_2014_us_500k c2
						    WHERE c2.geolevel_id = l_geolevel_id
						      AND c2.zoomlevel   = l_zoomlevel
						 	  AND c2.x           = d.x
					 		  AND c2.y           = d.y
							  AND c2.areaid      = d.areaid)
	), f AS (
		SELECT e.zoomlevel, l_geolevel_id AS geolevel_id, e.x, e.y, e.bbox, e2.areaid, e2.geom
		  FROM e, geometry_cb_2014_us_500k e2
	     WHERE e2.zoomlevel    = l_use_zoomlevel
		   AND e2.geolevel_id  = l_geolevel_id
		   AND e2.areaid       = e.areaid
		   AND (e.bbox && e2.geom) 			  /* Intersect by bounding box */
		   AND ST_Intersects(e.bbox, e2.geom) /* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
	)
	SELECT f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y, f.bbox, f.geom, 
	       ST_AsGeoJson(f.geom)::JSON AS optimised_geojson,
	       true::BOOLEAN AS within
	  FROM f
	 WHERE NOT ST_Within(f.bbox, f.geom) /* Exclude any tile bounding completely within the area */
	 ORDER BY f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y 
	LOOP
		IF num_rows IS NULL THEN
		END IF;
		explain_text:=explain_text||E'\n'||explain_line;	
	END LOOP; 
	IF l_debug THEN
		RAISE INFO '%', explain_text;
	END IF;
--
	OPEN c1_i1;
	FETCH c1_i1 INTO num_rows;
	CLOSE c1_i1;
--	 
	RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE; 

COMMENT ON FUNCTION tileMaker_intersector(INTEGER, INTEGER, INTEGER, BOOLEAN) IS '
Function:	 tileMaker_intersector2()
Parameters:	 geolevel ID, zoomlevel, zoomlevel data to use (<6 use 6), debug (Default: FALSE)
Returns:	 Number of rows inserted
Description: Insert tile area id intersections.  
';

DROP FUNCTION IF EXISTS tileMaker_intersector2(INTEGER, INTEGER, INTEGER, BOOLEAN);
CREATE OR REPLACE FUNCTION tileMaker_intersector2(
	l_geolevel_id INTEGER, 
	l_zoomlevel INTEGER, 
	l_use_zoomlevel INTEGER, 
	l_debug BOOLEAN DEFAULT FALSE)
RETURNS INTEGER
AS
$BODY$
DECLARE
	c1_i2	CURSOR FOR
		SELECT COUNT(areaid) AS total
		  FROM tile_intersects_cb_2014_us_500k
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel
		   AND NOT within;
--			  
	num_rows 		INTEGER;
	explain_line	text;
	explain_text	text:='';
BEGIN
 	FOR explain_line IN EXPLAIN ANALYZE 
	INSERT INTO tile_intersects_cb_2014_us_500k(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
	WITH a AS (
		SELECT DISTINCT geolevel_id, areaid
		  FROM geometry_cb_2014_us_500k
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel
		EXCEPT 
		SELECT DISTINCT geolevel_id, areaid
		  FROM tile_intersects_cb_2014_us_500k a
		 WHERE zoomlevel = l_zoomlevel
		   AND geolevel_id = l_geolevel_id
	), b AS (
		SELECT a.geolevel_id, a.areaid, ST_Envelope(b.geom) AS bbox, b.geom
		  FROM a, geometry_cb_2014_us_500k b
		 WHERE a.geolevel_id = l_geolevel_id
		   AND zoomlevel     = l_zoomlevel
		   AND a.areaid      = b.areaid
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
		 WHERE x.zoomlevel   = y.zoomlevel	
		   AND x.geolevel_id = y.geolevel_id
		   AND x.areaid      = y.areaid
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
							WHERE c2.geolevel_id = l_geolevel_id
							  AND c2.zoomlevel   = l_zoomlevel
							  AND c2.x           = e.x
							  AND c2.y           = e.y	
							  AND c2.areaid      = e.areaid)
	), g AS (
			SELECT f.zoomlevel, f.geolevel_id, f.x, f.y, f.bbox, e2.areaid, e2.geom
			  FROM f, geometry_cb_2014_us_500k e2
			 WHERE e2.zoomlevel    = l_use_zoomlevel
			   AND e2.geolevel_id  = l_geolevel_id
			   AND e2.areaid       = f.areaid
			   AND (f.bbox && e2.geom) 			  /* Intersect by bounding box */
			   AND ST_Intersects(f.bbox, e2.geom) /* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
	)
	SELECT geolevel_id, zoomlevel, areaid, x, y, bbox, geom,	
	       ST_AsGeoJson(g.geom)::JSON AS optimised_geojson,
	       ST_Within(g.bbox, g.geom) AS within
 	  FROM g 
	 ORDER BY geolevel_id, zoomlevel, areaid, x, y
	LOOP		
		explain_text:=explain_text||E'\n'||explain_line;
	END LOOP;
	IF l_debug THEN
		RAISE INFO '%', explain_text;
	END IF;
--
	OPEN c1_i2;
	FETCH c1_i2 INTO num_rows;
	CLOSE c1_i2;
--	 
	RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;
 
COMMENT ON FUNCTION tileMaker_intersector2(INTEGER, INTEGER, INTEGER, BOOLEAN) IS '
Function:	 tileMaker_intersector2()
Parameters:	 geolevel ID, zoomlevel, zoomlevel data to use (<6 use 6), debug (Default: FALSE)
Returns:	 Number of rows inserted
Description: Insert any missing area ids if possible (i.e. have non empty geometry). This is caused by small areas, usually islands,
             being simplified out of existance at a lower zoomlevel.  
';

DROP FUNCTION IF EXISTS tileMaker_aggregator(INTEGER, INTEGER, BOOLEAN);
CREATE OR REPLACE FUNCTION tileMaker_aggregator(
	l_geolevel_id INTEGER, 
	l_zoomlevel INTEGER, 
	l_debug BOOLEAN DEFAULT FALSE)
RETURNS INTEGER
AS
$BODY$
DECLARE
	c1_i3	CURSOR FOR
		SELECT COUNT(tile_id) AS total
		  FROM t_tiles_cb_2014_us_500k
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel;
--
	num_rows 		INTEGER;
	explain_line	text;
	explain_text	text:='';
--
	sql_stmt		text;
BEGIN
 	FOR explain_line IN EXPLAIN ANALYZE 
	INSERT INTO t_tiles_cb_2014_us_500k(
		geolevel_id,
		zoomlevel,
		x, 
		y,
		optimised_geojson,
		optimised_topojson,
		tile_id)
	SELECT a.geolevel_id,
		   zoomlevel,
		   x, 
		   y,
		   json_agg(optimised_geojson) AS optimised_geojson,
		   NULL::JSON AS optimised_topojson,
		   a.geolevel_id::Text||'_'||b.geolevel_name||'_'||a.zoomlevel||'_'||a.x::Text||'_'||a.y::Text AS tile_id
	  FROM tile_intersects_cb_2014_us_500k a, geolevels_cb_2014_us_500k b
	 WHERE a.geolevel_id = b.geolevel_id 
	   AND a.zoomlevel   = l_zoomlevel
	   AND a.geolevel_id = l_geolevel_id
	 GROUP BY a.geolevel_id,
			  b.geolevel_name,
			  a.zoomlevel,
			  a.x, 
			  a.y
	 ORDER BY 1, 2, 3, 4
	LOOP		
		explain_text:=explain_text||E'\n'||explain_line;
	END LOOP;
	IF l_debug THEN
		RAISE INFO '%', explain_text;
	END IF;
--
	sql_stmt:='REINDEX TABLE tile_intersects_cb_2014_us_500k'||'_geolevel_id_'||l_geolevel_id::Text||'_zoomlevel_'||l_zoomlevel::Text;
	EXECUTE sql_stmt;
	sql_stmt:='ANALYZE tile_intersects_cb_2014_us_500k'||'_geolevel_id_'||l_geolevel_id::Text||'_zoomlevel_'||l_zoomlevel::Text;
	EXECUTE sql_stmt;
--
	OPEN c1_i3;
	FETCH c1_i3 INTO num_rows;
	CLOSE c1_i3;
--	 
	RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;
 
COMMENT ON FUNCTION tileMaker_aggregator(INTEGER, INTEGER, BOOLEAN) IS '
Function:	 tileMaker_aggregator()
Parameters:	 geolevel ID, zoomlevel, debug (Default: FALSE)
Returns:	 Number of rows inserted
Description: Aggreate JSON abd insert into t_tiles_cb_2014_us_500k.  
';

/*								  		 
psql:tile-maker.sql:1131: INFO:  Processed 1 tile for geolevel id 1/3 zoomlevel: 0/11 in 0.1s, 0.1s total;16.4 tiles/s
psql:tile-maker.sql:1131: INFO:  Processed 1 tile for geolevel id 2/3 zoomlevel: 0/11 in 0.2s, 0.2s total;5.8 tiles/s
psql:tile-maker.sql:1131: INFO:  Processed 1 tile for geolevel id 3/3 zoomlevel: 0/11 in 0.8s, 1.0s total;1.3 tiles/s
psql:tile-maker.sql:1131: INFO:  Processed 57+0 total areaid intersects, 3 tiles for geolevel id 2/3 zoomlevel: 1/11 in 0.7+0.0s+0.3s, 1.9s total; 92.1 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 67+0 total areaid intersects, 5 tiles for geolevel id 2/3 zoomlevel: 2/11 in 0.8+0.0s+0.3s, 3.1s total; 88.3 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 81+0 total areaid intersects, 10 tiles for geolevel id 2/3 zoomlevel: 3/11 in 0.8+0.0s+0.4s, 4.3s total; 111.1 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 95+0 total areaid intersects, 22 tiles for geolevel id 2/3 zoomlevel: 4/11 in 1.6+0.0s+0.7s, 6.6s total; 71.7 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 142+0 total areaid intersects, 47 tiles for geolevel id 2/3 zoomlevel: 5/11 in 2.9+0.0s+1.1s, 10.6s total; 66.1 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 241+0 total areaid intersects, 110 tiles for geolevel id 2/3 zoomlevel: 6/11 in 5.7+0.0s+2.5s, 18.9s total; 61.2 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 485+0 total areaid intersects, 279 tiles for geolevel id 2/3 zoomlevel: 7/11 in 17.2+0.0s+8.2s, 44.3s total; 44.4 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 1018+0 total areaid intersects, 663 tiles for geolevel id 2/3 zoomlevel: 8/11 in 53.3+0.0s+25.5s, 123.0s total; 31.6 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 2246+0 total areaid intersects, 1566 tiles for geolevel id 2/3 zoomlevel: 9/11 in 192.8+0.0s+78.8s, 394.7s total; 19.8 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 4901+0 total areaid intersects, 3575 tiles for geolevel id 2/3 zoomlevel: 10/11 in 622.7+0.0s+276.1s, 1293.6s total; 13.6 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 10718+0 total areaid intersects, 8045 tiles for geolevel id 2/3 zoomlevel: 11/11 in 1893.1+0.0s+963.3s, 4150.0s total; 9.9 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 3233+0 total areaid intersects, 3 tiles for geolevel id 3/3 zoomlevel: 1/11 in 6.2+0.0s+0.6s, 4156.8s total; 524.0 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 3291+0 total areaid intersects, 5 tiles for geolevel id 3/3 zoomlevel: 2/11 in 6.3+0.0s+0.6s, 4163.8s total; 526.8 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 3390+0 total areaid intersects, 10 tiles for geolevel id 3/3 zoomlevel: 3/11 in 4.2+0.0s+0.6s, 4168.6s total; 809.1 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 3439+0 total areaid intersects, 22 tiles for geolevel id 3/3 zoomlevel: 4/11 in 3.4+0.0s+0.6s, 4172.7s total; 1016.8 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 3657+0 total areaid intersects, 48 tiles for geolevel id 3/3 zoomlevel: 5/11 in 3.4+0.0s+0.7s, 4176.9s total; 1078.5 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 4063+3 total areaid intersects, 117 tiles for geolevel id 3/3 zoomlevel: 6/11 in 3.5+0.0s+0.9s, 4181.4s total; 1190.5 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 4986+0 total areaid intersects, 330 tiles for geolevel id 3/3 zoomlevel: 7/11 in 5.2+0.0s+1.7s, 4188.3s total; 1020.0 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 7124+3 total areaid intersects, 989 tiles for geolevel id 3/3 zoomlevel: 8/11 in 11.8+0.0s+4.1s, 4204.2s total; 685.4 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 12268+5 total areaid intersects, 3135 tiles for geolevel id 3/3 zoomlevel: 9/11 in 32.0+0.0s+11.3s, 4247.6s total; 481.0 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 24459+0 total areaid intersects, 9405 tiles for geolevel id 3/3 zoomlevel: 10/11 in 95.5+0.0s+32.8s, 4375.9s total; 354.6 intesects/s
psql:tile-maker.sql:1131: INFO:  Processed 51437+1 total areaid intersects, 24351 tiles for geolevel id 3/3 zoomlevel: 11/11 in 309.4+0.1s+124.9s, 4810.5s total; 244.9 intesects/s
DO
Time: 4810462.578 ms
 */
DO LANGUAGE plpgsql $$
DECLARE
	max_geolevel_id		INTEGER;
	max_zoomlevel 		INTEGER;
	l_areaid_count		INTEGER;
	start_geolevel_id	INTEGER;
--
	c1_maxgeolevel_id 	CURSOR FOR
		SELECT MAX(geolevel_id) AS max_geolevel_id,
	           MAX(zoomlevel) AS max_zoomlevel
	      FROM geometry_cb_2014_us_500k;
	c2_areaid_count 	CURSOR FOR	
		SELECT areaid_count
		  FROM geolevels_cb_2014_us_500k	
		 WHERE geolevel_id = 1;
--
	num_rows 		INTEGER:=0;
	num_rows2 		INTEGER:=0;
	num_rows3 		INTEGER:=0;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
	took3 			INTERVAL;
	took4 			INTERVAL;
--
	tiles_per_s		NUMERIC;
--
	l_use_zoomlevel INTEGER;
	l_debug 		BOOLEAN;
BEGIN
	OPEN c1_maxgeolevel_id;
	FETCH c1_maxgeolevel_id INTO max_geolevel_id, max_zoomlevel;
	CLOSE c1_maxgeolevel_id;
	OPEN c2_areaid_count;
	FETCH c2_areaid_count INTO l_areaid_count;
	CLOSE c2_areaid_count;	
--	 
--	max_zoomlevel 	:=10;		/* Override for test purposes */
	IF l_areaid_count = 1 THEN	/* 0/0/0 tile only;  */			
		start_geolevel_id=2;	
	ELSE
		start_geolevel_id=1;
	END IF;
--
-- Create zoomleve l0 tiles. Intersect already created
--	
	FOR i IN 1 .. max_geolevel_id LOOP
--			
		stp2:=clock_timestamp();
		num_rows3:=tileMaker_aggregator(i, 0, l_debug);	
		etp:=clock_timestamp();
		took4:=age(etp, stp2);
--			
		took:=age(etp, stp);
		tiles_per_s:=ROUND(num_rows3::NUMERIC/EXTRACT(EPOCH FROM took4)::NUMERIC, 1);
		RAISE INFO 'Processed % tile for geolevel id %/% zoomlevel: %/% in %s, %s total;% tiles/s', 
			num_rows3, 
			i, max_geolevel_id, 0, max_zoomlevel,  
			ROUND(EXTRACT(EPOCH FROM took4)::NUMERIC, 1), 
			ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
			tiles_per_s;	
	END LOOP;
		
--
-- Timing; 3 zoomlevels to:
--
-- Zoomlevel 7: 1 minute (75)
-- Zoomlevel 8: 3 minutes (321..260..154..218..230..217..166 seconds with tile aggregation)
-- Zoomlevel 9: 8 minutes (673..627..460)
-- Zoomlevel 10: 24 minutes (1473)
-- Zoomlevel 11: 80 minutes (4810)
--
	FOR i IN start_geolevel_id .. max_geolevel_id LOOP
		FOR j IN 1 .. max_zoomlevel LOOP
			l_debug:=FALSE;
			IF j = max_zoomlevel AND i = max_geolevel_id THEN
				l_debug:=TRUE;
			END IF;
			l_use_zoomlevel=j;
			IF j<6 THEN 
				l_use_zoomlevel=6;
			END IF;
			stp2:=clock_timestamp();
			num_rows:=tileMaker_intersector(i, j, l_use_zoomlevel, l_debug);
			etp:=clock_timestamp();
			took2:=age(etp, stp2);
--			
			stp2:=clock_timestamp();
			num_rows2:=tileMaker_intersector2(i, j, l_use_zoomlevel, l_debug);	
			etp:=clock_timestamp();
			took3:=age(etp, stp2);
--			
			stp2:=clock_timestamp();
			num_rows3:=tileMaker_aggregator(i, j, l_debug);	
			etp:=clock_timestamp();
			took4:=age(etp, stp2);
--			
			took:=age(etp, stp);
			tiles_per_s:=ROUND((num_rows+num_rows2+num_rows3)::NUMERIC/EXTRACT(EPOCH FROM took2)::NUMERIC, 1);
			RAISE INFO 'Processed %+% total areaid intersects, % tiles for geolevel id %/% zoomlevel: %/% in %+%s+%s, %s total; % intesects/s', 
				num_rows, num_rows2, num_rows3, 
				i, max_geolevel_id, j, max_zoomlevel, 
				ROUND(EXTRACT(EPOCH FROM took2)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took3)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took4)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
				tiles_per_s;			
		END LOOP;	
	END LOOP;
END;
$$; 	
	
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

SELECT geolevel_id, zoomlevel, within, COUNT(areaid)
  FROM tile_intersects_cb_2014_us_500k
 GROUP BY geolevel_id, zoomlevel, within
 ORDER BY geolevel_id, zoomlevel, within;

ALTER TABLE t_tiles_cb_2014_us_500k 
	ADD CONSTRAINT t_tiles_cb_2014_us_500k_pk PRIMARY KEY (geolevel_id, zoomlevel, x, y);
ALTER TABLE t_tiles_cb_2014_us_500k 
	ADD CONSTRAINT t_tiles_cb_2014_us_500k_uk UNIQUE (tile_id);			
ANALYZE VERBOSE t_tiles_cb_2014_us_500k; 
 
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

-- Data
SELECT no_area_ids, SUBSTRING(optimised_geojson::Text FROM 1 FOR 90) AS optimised_geojson
  FROM tiles_cb_2014_us_500k
 WHERE zoomlevel = 6 and geolevel_id = 1 and x = 17 and y = 26;
-- Data
SELECT no_area_ids, SUBSTRING(optimised_geojson::Text FROM 1 FOR 90) AS optimised_geojson
  FROM tiles_cb_2014_us_500k
 WHERE zoomlevel = 6 and geolevel_id = 1 and x = 26 and y = 26;
-- No data
SELECT no_area_ids, SUBSTRING(optimised_geojson::Text FROM 1 FOR 90) AS optimised_geojson
  FROM tiles_cb_2014_us_500k
 WHERE zoomlevel = 6 and geolevel_id = 1 and x = 17 and y = 260; 

SELECT tile_id, zoomlevel, x, y
  FROM t_tiles_cb_2014_us_500k h2
 WHERE h2.zoomlevel   = 0 
   AND h2.x           = 0  
   AND h2.y           = 0   
   AND h2.geolevel_id = 1;
					
END;
--

 