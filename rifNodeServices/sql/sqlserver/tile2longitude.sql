/*
 * SQL statement name: 	tile2longitude.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert OSM tile x to longitude (WGS84 - 4326) 
 * Note:				%% becomes % after substitution
 */
IF OBJECT_ID (N'tileMaker_tile2longitude', N'FN') IS NOT NULL  
    DROP FUNCTION tileMaker_tile2longitude;  
GO 

CREATE FUNCTION tileMaker_tile2longitude(@x INTEGER, @zoom_level INTEGER)
RETURNS DOUBLE PRECISION AS
BEGIN
	DECLARE @longitude DOUBLE PRECISION;
	SET @longitude=CAST( ( (@x * 1.0) / POWER(2, @zoom_level) * 360.0) - 180.0 AS DOUBLE PRECISION);
	RETURN @longitude;
END;
GO
  
DECLARE @CurrentUser sysname;
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty  'MS_Description', 'Function: 	 tileMaker_tile2longitude()
Parameters:	 OSM Tile x, zoom level
Returns:	 Longitude
Description: Convert OSM tile x to longitude (WGS84 - 4326)
',
   'user', @CurrentUser,   
   'function', 'tileMaker_tile2longitude'