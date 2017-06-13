/*
 * SQL statement name: 	geolevels_areaid_check.sql
 * Type:				Postgres SQL statement
 * Parameters:
 *						1: Geolevels table; e.g. geolevels_cb_2014_us_500k
 *
 * Description:			Update areaid_count column in geolevels table using geometry table
 * Note:				%% becomes % after substitution
 */
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT geolevel_id, geolevel_name, areaid_count
		  FROM %1;
 	c1_rec RECORD;
--
	errors INTEGER:=0;
BEGIN
	FOR c1_rec IN c1 LOOP
		IF c1_rec.areaid_count > 0 THEN
			RAISE INFO 'geolevel: %:% areaid_count: %', c1_rec.geolevel_id, c1_rec.geolevel_name, c1_rec.areaid_count;
		ELSIF c1_rec.areaid_count IS NULL THEN
			errors:=errors+1;
			RAISE WARNING 'geolevel: %:% areaid_count IS NULL', c1_rec.geolevel_id, c1_rec.geolevel_name;			
		ELSE	
			errors:=errors+1;
			RAISE WARNING 'geolevel: %:% errors is zero', c1_rec.geolevel_id, c1_rec.geolevel_name;
		END IF;
	END LOOP;
	IF errors = 0 THEN
		RAISE INFO 'Geolevels table: %1 no zero areaid_counts';
	ELSE
		RAISE EXCEPTION 'Geolevels table: %1 % geolevels have zero areaid_counts', errors;
	END IF;
END;
$$ 