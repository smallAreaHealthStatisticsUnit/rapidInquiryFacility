/*
 * SQL statement name: 	latitude2tile.sql
 * Type:				Microsoft SQL Server T/sql function
 * Parameters:			None
 *
 * Description:			Convert latitude (WGS84 - 4326) to OSM tile y
 * Note:				%% becomes % after substitution
 */
IF OBJECT_ID (N'tileMaker_latitude2tile', N'FN') IS NOT NULL  
    DROP FUNCTION tileMaker_latitude2tile;  
GO 

CREATE FUNCTION tileMaker_latitude2tile(@latitude DOUBLE PRECISION, @zoom_level INTEGER)
RETURNS INTEGER 
AS
BEGIN
	DECLARE @tileY INTEGER;
	SET @tileY=CAST(
					FLOOR( 
						(1.0 - LOG /* Natural Log */ 
							(TAN(RADIANS(@latitude)) + 1.0 / COS(RADIANS(@latitude))) / PI()) / 2.0 * POWER(2, @zoom_level) 
						) 
					AS INTEGER);
	RETURN @tileY;
END;
GO
  
DECLARE @CurrentUser sysname;
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty  'MS_Description', 'Function: 	 tileMaker_latitude2tile()
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
* Multiply x and y by n. Round results down to give tilex and tiley.
',
   'user', @CurrentUser,   
   'function', 'tileMaker_latitude2tile'