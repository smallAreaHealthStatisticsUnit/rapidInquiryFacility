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
 *						Ignore small areas <= 15 km2
 * Note:				%%%% becomes %% after substitution
 */
	WITH a AS (
		SELECT areaname,
			   CAST(area_km2 AS NUMERIC(15,2)) AS area_km2,
			   CAST((%1.STArea()/(1000*1000)) AS NUMERIC(15,2)) AS area_km2_calc
		  FROM %2
	), b AS (
	SELECT a.areaname,
		   a.area_km2,
		   a.area_km2_calc,
		   CASE WHEN a.area_km2 > 0 THEN 100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2)
				WHEN a.area_km2 = a.area_km2_calc THEN 0
				ELSE NULL
		   END AS pct_km2_diff 
	  FROM a
	), c AS (
		SELECT COUNT(areaname) AS total_areas
		  FROM a
	), d AS (
		SELECT b.areaname, b.area_km2, b.area_km2_calc, b.pct_km2_diff
		  FROM b, c
		 WHERE b.pct_km2_diff > 5 /* Allow for 5% error */
		   AND b.area_km2_calc > 15 /* Ignore small areas <= 15 km2 */
	), e AS (
		SELECT COUNT(areaname) AS total_areas_in_error
		  FROM d
	)
	SELECT d.areaname, d.area_km2, d.area_km2_calc, d.pct_km2_diff, c.total_areas AS total_areas, e.total_areas_in_error AS total_areas_in_error, 
		   ROUND((100*CAST(e.total_areas_in_error AS NUMERIC)/CAST(c.total_areas AS NUMERIC)), 2) AS pct_in_error
	  FROM d, c, e;
DECLARE @areaname AS VARCHAR(30);
DECLARE @area_km2 AS NUMERIC(15,2);
DECLARE @area_km2_calc AS NUMERIC(15,2);
DECLARE @pct_km2_diff AS NUMERIC(15,2);
DECLARE @total_areas AS NUMERIC(15,2);
DECLARE @total_areas_in_error AS NUMERIC(15,2);
DECLARE @pct_in_error AS NUMERIC(15,2);
DECLARE @nrows AS int;
SET @nrows=0;
OPEN c1;
FETCH NEXT FROM c1 INTO @areaname, @area_km2, @area_km2_calc, @pct_km2_diff, @total_areas, @total_areas_in_error, @pct_in_error;
WHILE @@FETCH_STATUS = 0
BEGIN
		SET @nrows+=1;
		IF @nrows = 1 PRINT 'WARNING ' + CAST(@total_areas_in_error AS VARCHAR) + ' areas in error of ' + CAST(@total_areas AS VARCHAR) + 
			', ' + CAST(@pct_in_error AS VARCHAR) + 'pct';
		PRINT 'WARNING Area: ' + @areaname + ', area km2: ' + CAST(@area_km2 AS VARCHAR) +  + ', calc: ' +
			CAST(@area_km2_calc AS VARCHAR) + ', diff: ' + CAST(@pct_km2_diff AS VARCHAR);
		FETCH NEXT FROM c1 INTO @areaname, @area_km2, @area_km2_calc, @pct_km2_diff, @total_areas, @total_areas_in_error, @pct_in_error;
END
IF @nrows = 0
	PRINT 'Table: %2 no invalid areas check OK';
ELSE
	IF @pct_in_error < 10 PRINT 'WARNING Table: %2 no invalid areas check WARNING: ' + CAST(@pct_in_error AS VARCHAR) + ' invalid (<10 pct)';
	ELSE
		RAISERROR('Table: %2 no invalid areas check FAILED: %i invalid', 16, 1, @nrows);
CLOSE c1;
DEALLOCATE c1;