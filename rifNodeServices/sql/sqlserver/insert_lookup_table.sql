/*
 * SQL statement name: 	insert_lookup_table.sql
 * Type:				Microsoft SQL Server T/sql
 * Parameters:
 *						1: lookup table; e.g. lookup_sahsu_grd_level1
 *						2: shapefile table; e.g. sahsu_grd_level4
 *
 * Description:			Insert data into lookup table
 *						Centroid JSON: {"type":"Point","coordinates":[-6.36447811663261,55.1846108882703]}
 * Note:				%%%% becomes %% after substitution
 */
INSERT INTO %1(%2, areaname, gid, geographic_centroid)
SELECT areaid, areaname, ROW_NUMBER() OVER(ORDER BY areaid) AS gid, 
	   '{"type":"Point","coordinates":[' + CAST(geographic_centroid.Long AS VARCHAR) +  ',' + 
			CAST(geographic_centroid.Lat AS VARCHAR) + ']}' AS geographic_centroid
  FROM %2
 ORDER BY 1