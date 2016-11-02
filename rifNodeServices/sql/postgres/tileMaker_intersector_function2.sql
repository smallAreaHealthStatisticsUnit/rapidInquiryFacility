CREATE OR REPLACE FUNCTION %1(
	l_geolevel_id INTEGER, 
	l_zoomlevel INTEGER, 
	l_use_zoomlevel INTEGER, 
	l_debug BOOLEAN DEFAULT FALSE)
RETURNS INTEGER
AS
$BODY$
/*
 * SQL statement name: 	tileMaker_intersector_function2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: function name; e.g. tileMaker_intersector2_cb_2014_us_500k
 *						2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k
 *						3: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Create tile intersects table INSERT function
 * Note:				%%%% becomes %% after substitution
 */
 
/*
Function: 		%1()
Parameters:		geolevel_id, zoomlevel, use zoomlevel (source of data), debug (TRUE/FALSE)
Returns:		Nothing
Description:	tile intersects table INSERT function. Zoomlevels <6 use zoomlevel 6 data
				Insert tile area id intersections missing where not in the previous layer; 
				this is usually due to it being simplified out of existance.  
 */
DECLARE
	c1_i2	CURSOR FOR
		SELECT COUNT(areaid) AS total
		  FROM %2
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel
		   AND NOT within;
--			  
	num_rows 		INTEGER;
	explain_line	text;
	explain_text	text:='';
BEGIN
 	FOR explain_line IN EXPLAIN ANALYZE 
	INSERT INTO %2(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
	WITH a AS (
		SELECT DISTINCT geolevel_id, areaid
		  FROM %3
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel
		EXCEPT 
		SELECT DISTINCT geolevel_id, areaid
		  FROM %2 a
		 WHERE zoomlevel = l_zoomlevel
		   AND geolevel_id = l_geolevel_id
	), b AS (
		SELECT a.geolevel_id, a.areaid, ST_Envelope(b.geom) AS bbox, b.geom
		  FROM a, %3 b
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
							 FROM %2 c2
							WHERE c2.geolevel_id = l_geolevel_id
							  AND c2.zoomlevel   = l_zoomlevel
							  AND c2.x           = e.x
							  AND c2.y           = e.y	
							  AND c2.areaid      = e.areaid)
	), g AS (
			SELECT f.zoomlevel, f.geolevel_id, f.x, f.y, f.bbox, e2.areaid, e2.geom
			  FROM f, %3 e2
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
LANGUAGE plpgsql VOLATILE