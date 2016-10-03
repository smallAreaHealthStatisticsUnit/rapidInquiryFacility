/*
 * SQL statement name: 	latitude2tile.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert latitude (WGS84 - 4326) to OSM tile y
 * Note:				%% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_latitude2tile(DOUBLE PRECISION, INTEGER);

CREATE OR REPLACE FUNCTION tileMaker_latitude2tile(latitude DOUBLE PRECISION, zoom_level INTEGER)
RETURNS INTEGER AS
$$
    SELECT FLOOR( (1.0 - LN(TAN(RADIANS(latitude)) + 1.0 / COS(RADIANS(latitude))) / PI()) / 2.0 * (1 << zoom_level) )::INTEGER
$$
LANGUAGE sql IMMUTABLE;
  
COMMENT ON FUNCTION tileMaker_latitude2tile(DOUBLE PRECISION, INTEGER) IS 'Function: 	 tileMaker_latitude2tile()
Parameters:	 Latitude, zoom level
Returns:	 OSM Tile y
Description: Convert latitude (WGS84 - 4326) to OSM tile x

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