/*
 * SQL statement name: 	longitude2tile.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert longitude (WGS84 - 4326) to OSM tile x
 * Note:				%% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_longitude2tile(DOUBLE PRECISION, INTEGER);

CREATE OR REPLACE FUNCTION tileMaker_longitude2tile(longitude DOUBLE PRECISION, zoom_level INTEGER)
RETURNS INTEGER AS
$$
    SELECT FLOOR( (longitude + 180) / 360 * (1 << zoom_level) )::INTEGER
$$
LANGUAGE sql IMMUTABLE;
  
COMMENT ON FUNCTION tileMaker_longitude2tile(DOUBLE PRECISION, INTEGER) IS 'Function: 	 tileMaker_longitude2tile()
Parameters:	 Longitude, zoom level
Returns:	 OSM Tile x
Description: Convert longitude (WGS84 - 4326) to OSM tile x

Derivation of the tile X/Y 

* Reproject the coordinates to the Mercator projection (from EPSG:4326 to EPSG:3857):

x = lon
y = arsinh(tan(lat)) = log[tan(lat) + sec(lat)]
(lat and lon are in radians)

* Transform range of x and y to 0 – 1 and shift origin to top left corner:

x = [1 + (x / p)] / 2
y = [1 - (y / p)] / 2

* Calculate the number of tiles across the map, n, using 2**zoom
* Multiply x and y by n. Round results down to give tilex and tiley.'