CREATE OR REPLACE FUNCTION %1(
	l_geolevel_id INTEGER, 
	l_zoomlevel INTEGER,  
	l_debug BOOLEAN DEFAULT FALSE)
RETURNS INTEGER
AS
$BODY$
/*
 * SQL statement name: 	tileMaker_aggregator_function.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: function name; e.g. tileMaker_aggregator_cb_2014_us_500k
 *						2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k
 *						3: tiles table; e.g. t_tiles_cb_2014_us_500k
 *						4: geolevels table; e.g. geolevels_cb_2014_us_500k
 *
 * Description:			Create tiles table INSERT function (tile aggregator)
 * Note:				%%%% becomes %% after substitution
 */
 
/*
Function: 		%1()
Parameters:		geolevel_id, zoomlevel, use zoomlevel (source of data), debug (TRUE/FALSE)
Returns:		Nothing
Description:	tiles table INSERT function. Aggregate area_id JSON into featureCollection
 */
DECLARE
	c1_i3	CURSOR FOR
		SELECT COUNT(tile_id) AS total
		  FROM %3
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
	INSERT INTO %3(
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
	  FROM %2 a, %4 b
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
	sql_stmt:='REINDEX TABLE %2'||'_geolevel_id_'||l_geolevel_id::Text||'_zoomlevel_'||l_zoomlevel::Text;
	EXECUTE sql_stmt;
	sql_stmt:='ANALYZE %2'||'_geolevel_id_'||l_geolevel_id::Text||'_zoomlevel_'||l_zoomlevel::Text;
	EXECUTE sql_stmt;
--
	OPEN c1_i3;
	FETCH c1_i3 INTO num_rows;
	CLOSE c1_i3;
--	 
	RETURN num_rows;
END;
$BODY$
LANGUAGE plpgsql VOLATILE