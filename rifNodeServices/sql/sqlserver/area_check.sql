DECLARE c1 CURSOR FOR
/*
 * SQL statement name: 	area_check.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Check Turf araa (area_km2) compared to SQL Server calculated area (area_km2_calc)
 *						Allow for 5% error
 *						Ignore small areas <= 10 km2
 * Note:				%%%% becomes %% after substitution
 */
	WITH a AS (
		SELECT areaname,
			   CAST(area_km2 AS NUMERIC(15,2)) AS area_km2,
			   CAST((%1.STArea()/(1000*1000)) AS NUMERIC(15,2)) AS area_km2_calc
		  FROM %2
	), b AS (
	SELECT SUBSTRING(a.areaname, 1, 30) AS areaname,
		   a.area_km2,
		   a.area_km2_calc,
		   CASE WHEN a.area_km2 > 0 THEN CAST(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2) AS NUMERIC(15,2))
				WHEN a.area_km2 = a.area_km2_calc THEN 0
				ELSE NULL
		   END AS pct_km2_diff
	  FROM a
	)
	SELECT b.areaname, b.area_km2, b.area_km2_calc, b.pct_km2_diff
	  FROM b
	 WHERE b.pct_km2_diff > 5 /* Allow for 5% error */
	   AND b.area_km2_calc > 10 /* Ignore small areas <= 10 km2 */;
DECLARE @areaname AS VARCHAR(30);
DECLARE @area_km2 AS NUMERIC(15,2);
DECLARE @area_km2_calc AS NUMERIC(15,2);
DECLARE @pct_km2_diff AS NUMERIC(15,2);
DECLARE @nrows AS int;
SET @nrows=0;
OPEN c1;
FETCH NEXT FROM c1 INTO @areaname, @area_km2, @area_km2_calc, @pct_km2_diff;
WHILE @@FETCH_STATUS = 0
BEGIN
		SET @nrows+=1;
		PRINT 'Area: ' + @areaname + ', area km2: ' + CAST(@area_km2 AS VARCHAR) +  + ', calc: ' +
			CAST(@area_km2_calc AS VARCHAR) + ', diff: ' + CAST(@pct_km2_diff AS VARCHAR);
		FETCH NEXT FROM c1 INTO @areaname, @area_km2, @area_km2_calc, @pct_km2_diff;
END
IF @nrows = 0
	PRINT 'Table: %2 no invalid areas check OK';
ELSE
	RAISERROR('Table: %2 no invalid areas check FAILED: %i invalid', 16, 1, @nrows);
CLOSE c1;
DEALLOCATE c1