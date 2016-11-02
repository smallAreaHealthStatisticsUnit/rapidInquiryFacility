CREATE OR REPLACE FUNCTION %1(
	l_geolevel_id INTEGER, 
	l_zoomlevel INTEGER, 
	l_use_zoomlevel INTEGER, 
	l_debug BOOLEAN DEFAULT FALSE)
RETURNS INTEGER
AS
$BODY$
/*
 * SQL statement name: 	tileMaker_intersector_function.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: function name; e.g. tileMaker_intersector_cb_2014_us_500k
 *						2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k
 *						3: tile limits table; e.g. tile_limits_cb_2014_us_500k
 *						4: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Create tile intersects table INSERT function
 * Note:				%%%% becomes %% after substitution
 */
 
/*
Function: 		%1()
Parameters:		geolevel_id, zoomlevel, use zoomlevel (source of data), debug (TRUE/FALSE)
Returns:		Nothing
Description:	tile intersects table INSERT function. Zoomlevels <6 use zoomlevel 6 data
				Insert tile area id intersections.  
 */
DECLARE
	c1_i1	CURSOR FOR
		SELECT COUNT(areaid) AS total
		  FROM %2
		 WHERE geolevel_id = l_geolevel_id
		   AND zoomlevel   = l_zoomlevel;
--
	num_rows INTEGER;
	explain_line	text;
	explain_text	text:='';
BEGIN
	FOR explain_line IN EXPLAIN ANALYZE 
	INSERT INTO %2(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
	WITH a	AS (
		SELECT b.zoomlevel AS zoomlevel, b.x_mintile, b.x_maxtile, b.y_mintile, b.y_maxtile	  
		  FROM %3 b
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
		  FROM c, %2 p /* Parent */
		 WHERE p.geolevel_id = l_geolevel_id
		   AND p.zoomlevel 	 = l_zoomlevel -1/* Join to parent tile from previous geolevel_id; i.e. exclude if not present */
		   AND c.parent_xmin = p.x  
	       AND c.parent_ymin = p.y	
	), e AS (
		SELECT d.zoomlevel, d.x, d.y, d.bbox, d.areaid
		  FROM d
		 WHERE NOT EXISTS (SELECT c2.areaid
						     FROM %2 c2
						    WHERE c2.geolevel_id = l_geolevel_id
						      AND c2.zoomlevel   = l_zoomlevel
						 	  AND c2.x           = d.x
					 		  AND c2.y           = d.y
							  AND c2.areaid      = d.areaid)
	), f AS (
		SELECT e.zoomlevel, l_geolevel_id AS geolevel_id, e.x, e.y, e.bbox, e2.areaid, e2.geom
		  FROM e, %4 e2
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
LANGUAGE plpgsql VOLATILE