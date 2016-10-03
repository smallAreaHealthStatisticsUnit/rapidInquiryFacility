/*
 * SQL statement name: 	longitude2tile.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert longitude (WGS84 - 4326) to OSM tile x
 * Note:				%% becomes % after substitution
 */
IF OBJECT_ID (N'tileMaker_longitude2tile', N'FN') IS NOT NULL  
    DROP FUNCTION tileMaker_longitude2tile;  
GO 

CREATE FUNCTION tileMaker_longitude2tile(@longitude DOUBLE PRECISION, @zoom_level INTEGER)
RETURNS INTEGER AS
BEGIN
	DECLARE @tileX INTEGER;
	SET @tileX=CAST(
			FLOOR( (@longitude + 180) / 360 * POWER(2, @zoom_level) ) AS INTEGER);
	RETURN @tileX;
END;
GO
  
DECLARE @CurrentUser sysname;
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty  'MS_Description', 'Function: 	 tileMaker_longitude2tile()
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
* Multiply x and y by n. Round results down to give tilex and tiley.
',
   'user', @CurrentUser,   
   'function', 'tileMaker_longitude2tile'