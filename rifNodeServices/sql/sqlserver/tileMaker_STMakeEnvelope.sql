/*
 * SQL statement name: 	tileMaker_STMakeEnvelope.sql
 * Type:				MS SQL Server SQL
 * Parameters:			None
 * Description:			geometry  ST_MakeEnvelope(double precision xmin, double precision ymin, double precision xmax, 
 *						double precision ymax, integer srid=4326);
 * Note:				%% becomes % after substitution
 *
 *  zoomlevel |         xmin     |         xmax     |          ymin     |      ymax | 
 * -----------+------------------+------------------+-------------------+-----------+
 *          0 | -179.13729006727 | 179.773803959804 | -14.3737802873213 | 71.352561 |   
 * WITH a AS (
 *	SELECT ST_MakeEnvelope(-179.13729006727, -14.373780287321, 179.773803959804, 71.352561, 4326) AS bbox
 * )
 * SELECT ST_AsText(a.bbox) AS bbox
 *   FROM a;
 *                                                                               bbox
 * ---------------------------------------------------------------------------------------------------------------------------------------------------------------------
 *  POLYGON((-179.13729006727 -14.373780287321,-179.13729006727 71.352561,179.773803959804 71.352561,179.773803959804 -14.373780287321,-179.13729006727 -14.373780287321))
 * (1 row)
 *
 *  POLYGON((xmin ymin,xmin ymax,xmax ymax,xmax ymin,xmin ymin))
 */
IF OBJECT_ID (N'tileMaker_STMakeEnvelope', N'FN') IS NOT NULL  
    DROP FUNCTION tileMaker_STMakeEnvelope;  
GO 

CREATE FUNCTION tileMaker_STMakeEnvelope(@xmin DOUBLE PRECISION, @ymin DOUBLE PRECISION, @xmax DOUBLE PRECISION, @ymax DOUBLE PRECISION, @srid INTEGER=4326)
RETURNS GEOMETRY AS
BEGIN
	DECLARE @geom GEOMETRY;
	SET @geom=geometry::STGeomFromText('POLYGON(('+
	CAST(@xmin AS VARCHAR) + ' ' + 
	CAST(@ymin AS VARCHAR) + ',' +
	CAST(@xmin AS VARCHAR) + ' ' +
	CAST(@ymax AS VARCHAR) + ',' +
	CAST(@xmax AS VARCHAR) + ' ' +
	CAST(@ymax AS VARCHAR) + ',' +
	CAST(@xmax AS VARCHAR) + ' ' + 
	CAST(@ymin AS VARCHAR) + ',' + 
	CAST(@xmin AS VARCHAR) + ' ' +
	CAST(@ymin AS VARCHAR) + '))', @srid);
	RETURN @geom;
END;
GO
  
DECLARE @CurrentUser sysname;
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty  'MS_Description', 'Function: 	 tileMaker_STMakeEnvelope()
Parameters:	 double precision xmin, double precision ymin, double precision xmax, double precision ymax, integer srid=4326
Returns:	 Geometry
Description: Creates a rectangular Polygon formed from the given minimums and maximums. Input values must be in the 
			 spatial reference system specified by the SRID.

Creates a rectangular Polygon formed from the minima and maxima. by the given shell. Input values must be in SRS specified 
by the SRID. If no SRID is specified the WGS 84 spatial reference system is assumed
',
   'user', @CurrentUser,   
   'function', 'tileMaker_STMakeEnvelope' 