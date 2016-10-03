/*
 * SQL statement name: 	tile2longitude.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert OSM tile x to longitude (WGS84 - 4326) 
 * Note:				%% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_tile2longitude(INTEGER, INTEGER);

CREATE OR REPLACE FUNCTION tileMaker_tile2longitude(x INTEGER, zoom_level INTEGER)
RETURNS DOUBLE PRECISION AS
$$
	SELECT ( ( (x * 1.0) / (1 << zoom_level) * 360.0) - 180.0)::DOUBLE PRECISION
$$
LANGUAGE sql IMMUTABLE;
  
COMMENT ON FUNCTION tileMaker_tile2longitude(INTEGER, INTEGER) IS 'Function: 	 tileMaker_tile2longitude()
Parameters:	 OSM Tile x, zoom level
Returns:	 Longitude
Description: Convert OSM tile x to longitude (WGS84 - 4326)'