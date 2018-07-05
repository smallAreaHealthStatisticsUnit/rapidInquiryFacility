/*
 * SQL statement name: 	area_centroid_report.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Area and centroids report
 * Note:				%% becomes % after substitution
 */
WITH a AS (
	SELECT areaname, %1,
		   CAST(area_km2 AS NUMERIC(15,4)) AS area_km2,
		   CAST((%1.STArea()/(1000*1000)) AS NUMERIC(15,4)) AS area_km2_calc,
		   CONCAT(
				CAST(CAST(geographic_centroid.Long AS NUMERIC(15,7)) AS VARCHAR(30)),
				',',
				CAST(CAST(geographic_centroid.Lat AS NUMERIC(15,7)) AS VARCHAR(30))
				) AS geographic_centroid,
		   CONCAT(
				CAST(CAST(%1.EnvelopeCenter().Long AS NUMERIC(15,7)) AS VARCHAR(30)),
				',',
				CAST(CAST(%1.EnvelopeCenter().Lat AS NUMERIC(15,7)) AS VARCHAR(30))
				) AS geographic_centroid_calc,
		   CAST((%1.EnvelopeCenter().STDistance(geographic_centroid))/1000 AS VARCHAR(30)) AS centroid_diff_km,
		   ROW_NUMBER() OVER (ORDER BY areaname) as nrow
	  FROM %2
)
SELECT SUBSTRING(a.areaname, 1, 30) AS areaname,
       a.area_km2,
	   a.area_km2_calc,
	   CASE WHEN area_km2 = 0 THEN NULL 
			ELSE CAST(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2) AS NUMERIC(15,4)) 
			END AS pct_km2_diff,
	   a.geographic_centroid,
       a.geographic_centroid_calc,
	   a.centroid_diff_km
  FROM a
 WHERE nrow <= 100
 ORDER BY 1 