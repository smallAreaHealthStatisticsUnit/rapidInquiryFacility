DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	check_intersections.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *
 * Description:			Check intersections
 * Note:				%%%% becomes %% after substitution
 */
	l_geography VARCHAR:='%1';
--
	c1 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM geography_%1
		 WHERE geography = l_geography;
	c1_rec geography_%1%ROWTYPE;
--
	e INTEGER:=0;
	f INTEGER:=0;
	g INTEGER:=0;
BEGIN
--
	OPEN c1(l_geography);
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.geography IS NULL THEN
		RAISE EXCEPTION 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */;
	END IF;	
--
-- Call diff and multiple hierarchy tests
--
	e:=check_hierarchy_%1(c1_rec.geography, c1_rec.hierarchytable, 'missing');
	f:=check_hierarchy_%1(c1_rec.geography, c1_rec.hierarchytable, 'spurious additional');
	g:=check_hierarchy_%1(c1_rec.geography, c1_rec.hierarchytable, 'multiple hierarchy');
--
	IF e+f > 0 THEN
		RAISE EXCEPTION 'Geography: % codes check % missing, % spurious additional, % hierarchy fails', 
			c1_rec.geography	/* Geography */, 
			e::VARCHAR		/* Missing */, 
			f::VARCHAR		/* Spurious additional */, 
			g::VARCHAR		/* Multiple hierarchy */;
	ELSE
		RAISE INFO 'Geography: % codes check OK', 
			c1_rec.geography::VARCHAR	/* Geography */;
	END IF;
END;
$$