/*
 * SQL statement name: 	tileMaker_tile2latitude.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert OSM tile y to latitude (WGS84 - 4326)
 * Note:				%% becomes % after substitution
 */
IF OBJECT_ID (N'tileMaker_tile2latitude', N'FN') IS NOT NULL  
    DROP FUNCTION tileMaker_tile2latitude;  
GO 

CREATE FUNCTION tileMaker_tile2latitude(@y INTEGER, @zoom_level INTEGER)
RETURNS DOUBLE PRECISION AS
BEGIN
	DECLARE @latitude DOUBLE PRECISION;
	DECLARE @n FLOAT;
	DECLARE @sinh FLOAT;
	DECLARE @E FLOAT = 2.7182818284;
	
    SET @n = PI() - (2.0 * PI() * @y) / POWER(2.0, @zoom_level);
    SET @sinh = (1 - POWER(@E, -2*@n)) / (2 * POWER(@E, -@n));
    SET @latitude = DEGREES(ATAN(@sinh));
	RETURN @latitude;
END;
GO
  
DECLARE @CurrentUser sysname;
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty  'MS_Description', 'Function: 	 tileMaker_tile2latitude()
Parameters:	 OSM Tile y, zoom level
Returns:	 Latitude
Description: Convert OSM tile y to latitude (WGS84 - 4326)
',
   'user', @CurrentUser,   
   'function', 'tileMaker_tile2latitude'