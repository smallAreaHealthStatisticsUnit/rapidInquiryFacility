/*
 * SQL statement name: 	force_rhr.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Create insert statement into hierarchy table
 * Note:				%%%% becomes %% after substitution
 * 						c.%1.STUnion(%1.STStartPoint()) is also possible instead of c.%1.ReorientObject()
 */
WITH a AS (
	SELECT gid, %1,
		   CAST(area_km2 AS NUMERIC(21,6)) AS area_km2,
		   CAST((%1.STArea()/(1000*1000)) AS NUMERIC(21,6)) AS area_km2_calc
	  FROM %2
), b AS (
	SELECT a.gid,
	       a.%1,
           a.area_km2,
	       a.area_km2_calc,
          CASE WHEN a.area_km2 > 0 THEN CAST(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2) AS NUMERIC(21,6))
				WHEN a.area_km2 = a.area_km2_calc THEN 0
	        	ELSE NULL
	   	   END AS pct_km2_diff 
  FROM a
)
UPDATE %2
   SET %1 = c.%1.ReorientObject()
  FROM %2 c
 JOIN b ON b.gid = c.gid
 WHERE b.pct_km2_diff > 200 /* Threshold test */