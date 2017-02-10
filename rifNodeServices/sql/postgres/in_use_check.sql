DO LANGUAGE plpgsql $$
/*
 * SQL statement name: 	in_use_check.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Geography; e.g. SAHSULAND
 *
 * Description:			Check if geography is in use in studies. Raise error if it is. 
 *						To prevent accidental replacement
 * Note:				%% becomes % after substitution
 */
DECLARE
	c1 CURSOR FOR
		SELECT COUNT(DISTINCT(a.study_id)) AS total
		  FROM t_rif40_studies a
		 WHERE a.geography  = '%1';
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total = 0 THEN
		RAISE INFO 'Geography: %1 is not used by any studies';
	ELSE
		RAISE EXCEPTION 'Geography: %1 is used by: % studies', c1_rec.total;
	END IF;
END;
$$