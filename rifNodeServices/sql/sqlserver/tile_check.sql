/*
 * SQL statement name: 	tile_check.sql
 * Type:				MS SQL Server function
 * Parameters:			
 *						1: Lowest resolution geolevels table
 *						2: Geography
 *						3: min_zoomlevel
 *						4: max_zoomlevel
 *						5: Geolevel id = 1 geometry table
 *
 * Description:			Convert OSM tile y to latitude (WGS84 - 4326)
 * Note:				%% becomes % after substitution
 */
WITH a AS ( /* Geolevel summary */
		SELECT a1.geography, 
		       a1.geolevel_name AS min_geolevel_name,
               MIN(geolevel_id) AS min_geolevel_id,
               CAST(%4 AS INTEGER) AS zoomlevel,
               a2.max_geolevel_id
          FROM %1 a1, (
                        SELECT geography, MAX(geolevel_id) AS max_geolevel_id
  						  FROM %1 
						 GROUP BY geography
						) a2
         WHERE a1.geography     = '%2' 
           AND a1.geography     = a2.geography
         GROUP BY a1.geography, a1.geolevel_name, a2.max_geolevel_id
        HAVING MIN(geolevel_id) = 1
), b AS ( /* Get bounds of geography */		
	SELECT a2.geography,
		   a2.min_geolevel_id,
		   a2.max_geolevel_id,
		   a2.zoomlevel,
		   CASE
				WHEN a2.zoomlevel <= %3 THEN
					geometry::STGeomFromWKB(b.geom_%3.STAsBinary(), b.geom_%3.STSrid /* Cast to geometry */).STEnvelope()
				WHEN a2.zoomlevel BETWEEN (%3+1) AND %4 THEN	
					geometry::STGeomFromWKB(b.geom_%4.STAsBinary(), b.geom_%4.STSrid /* Cast to geometry */).STEnvelope()
				ELSE NULL
           END AS geom_envelope
      FROM %5 b, a a2
)
SELECT b.geography,
	   b.min_geolevel_id,
	   b.max_geolevel_id,
	   b.zoomlevel,
	   CAST(b.geom_envelope.STPointN(1).STX AS numeric(8,5)) AS Xmin,
	   CAST(b.geom_envelope.STPointN(3).STX AS numeric(8,5)) AS Xmax,
	   CAST(b.geom_envelope.STPointN(1).STY AS numeric(8,5)) AS Ymin,
	   CAST(b.geom_envelope.STPointN(3).STY AS numeric(8,5)) AS Ymax,
	   $(SQLCMDUSER).tileMaker_latitude2tile(b.geom_envelope.STPointN(1).STY, zoomlevel) AS Y_mintile,
	   $(SQLCMDUSER).tileMaker_latitude2tile(b.geom_envelope.STPointN(3).STY, zoomlevel) AS Y_maxtile,
	   $(SQLCMDUSER).tileMaker_longitude2tile(b.geom_envelope.STPointN(1).STX, zoomlevel) AS X_mintile,
	   $(SQLCMDUSER).tileMaker_longitude2tile(b.geom_envelope.STPointN(3).STX, zoomlevel) AS X_maxtile
  FROM b