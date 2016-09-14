DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	csvfile_rowcheck.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: Expected number of rows; e.g. 3233
 *						3: Column to count; e.g. gid
 *
 * Description:			Check number of rows in loaded CSV file is as expected
 * Note:				%%%% becomes %% after substitution
 */
	c1 CURSOR FOR
		SELECT COUNT(%3) AS total
		  FROM %1;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
	IF c1_rec.total = %2 THEN
		RAISE INFO 'Table: %1 row check OK: %', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: %1 row check FAILED: expected: %2 got: %', c1_rec.total;
	END IF;
END;
$$