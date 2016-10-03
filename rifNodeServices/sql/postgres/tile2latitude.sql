/*
 * SQL statement name: 	tileMaker_tile2latitude.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			None
 *
 * Description:			Convert OSM tile y to latitude (WGS84 - 4326)
 * Note:				%% becomes % after substitution
 */
DROP FUNCTION IF EXISTS tileMaker_tile2latitude(INTEGER, INTEGER);

CREATE OR REPLACE FUNCTION tileMaker_tile2latitude(y INTEGER, zoom_level INTEGER)
RETURNS DOUBLE PRECISION AS
$BODY$
DECLARE
	n FLOAT;
	sinh FLOAT;
	E FLOAT = 2.7182818284;
BEGIN
    n = PI() - (2.0 * PI() * y) / POWER(2.0, zoom_level);
    sinh = (1 - POWER(E, -2*n)) / (2 * POWER(E, -n));
    RETURN DEGREES(ATAN(sinh));
END;
$BODY$
LANGUAGE plpgsql IMMUTABLE;
  
COMMENT ON FUNCTION tileMaker_tile2latitude(INTEGER, INTEGER) IS 'Function: 	 tileMaker_tile2latitude()
Parameters:	 OSM Tile y, zoom level
Returns:	 Latitude
Description: Convert OSM tile y to latitude (WGS84 - 4326)'