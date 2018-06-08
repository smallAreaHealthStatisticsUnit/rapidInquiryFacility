/*
 * SQL statement name: 	area_centroid_report.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Area and centroids report
 * Note:				%% becomes % after substitution
 */
 WITH a AS (
	SELECT areaname,
		   ROUND(area_km2::numeric, 4) AS area_km2,
		   ROUND((ST_Area(geography(%1))/(1000*1000))::numeric, 4) AS area_km2_calc,
		   ROUND(ST_X(geographic_centroid)::numeric, 4)||','||ROUND(ST_Y(geographic_centroid)::numeric, 4) AS geographic_centroid,
		   ROUND(ST_X(ST_Centroid(%1))::numeric, 4)||','||ROUND(ST_Y(ST_Centroid(%1))::numeric, 4) AS geographic_centroid_calc,
		   ROUND(ST_DistanceSphere(ST_Centroid(%1), geographic_centroid)::numeric/1000, 2) AS centroid_diff_km
	  FROM %2
	 GROUP BY areaname, area_km2, %1, geographic_centroid
)
SELECT a.areaname,
       a.area_km2,
	   a.area_km2_calc,
	   CASE 
			WHEN area_km2_calc = 0 THEN 0 
			ELSE ROUND(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2_calc), 4) 
	   END AS pct_km2_diff,
	   a.geographic_centroid,
       a.geographic_centroid_calc,
	   a.centroid_diff_km
  FROM a
 ORDER BY 1
 LIMIT 100;