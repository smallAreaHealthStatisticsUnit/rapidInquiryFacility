DO LANGUAGE plpgsql $$
DECLARE 
/*
 * SQL statement name: 	area_check.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry column; e.g. geom_11
 *						2: table name; e.g. cb_2014_us_county_500k
 *
 * Description:			Check Turf araa (area_km2) compared to SQL Server calculated area (area_km2_calc)
 *						Allow for 1% error
 *						Ignore small areas <= 10 km2
 * Note:				%%%% becomes %% after substitution
 */
	c1 CURSOR FOR
		WITH a AS (
			SELECT areaname,
				   area_km2 AS area_km2,
				   ST_Area(geography(%1))/(1000*1000) AS area_km2_calc
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
			 WHERE b.pct_km2_diff > 1 /* Allow for 1% error */
			   AND b.area_km2_calc > 10 /* Ignore small areas <= 10 km2 */
		), e AS (
			SELECT COUNT(areaname) AS total_areas_in_error
			  FROM d
		)
		SELECT d.*, c.total_areas, e.total_areas_in_error, 
		       ROUND((100*e.total_areas_in_error::NUMERIC/c.total_areas::NUMERIC), 2) AS pct_in_error
		  FROM d, c, e;

	c1_rec RECORD;
	total INTEGER:=0;
	pct_in_error NUMERIC:=0;
BEGIN
	FOR c1_rec IN c1 LOOP
		IF total = 0 THEN
			RAISE WARNING '% areas in error of %, % pct', c1_rec.total_areas_in_error, c1_rec.total_areas, c1_rec.pct_in_error;
			pct_in_error:=c1_rec.pct_in_error;
		END IF;
		total:=total+1;
		RAISE WARNING 'Area: %, area km2: %:, calc: %, diff %',
			c1_rec.areaname, c1_rec.area_km2, c1_rec.area_km2_calc, c1_rec.pct_km2_diff;
	END LOOP;
	IF total = 0 THEN
		RAISE INFO 'Table: %2 no invalid areas check OK';
	ELSIF pct_in_error < 10 THEN
		RAISE WARNING 'Table: %2 no invalid areas check WARNING: % invalid (<10 pct)', pct_in_error;
	ELSE
		RAISE EXCEPTION 'Table: %2 no invalid areas check FAILED: % invalid', total;
	END IF;
END;
$$