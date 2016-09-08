CREATE OR REPLACE FUNCTION check_hierarchy_%1(l_geography VARCHAR, l_hierarchytable VARCHAR, l_type VARCHAR)
RETURNS integer 
SECURITY INVOKER
AS $body$
/*
 * SQL statement name: 	check_hierarchy_function.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *
 * Description:			Create insert statement into hierarchy table
 * Note:				%%%% becomes %% after substitution
 */
 
/*
Function: 		check_hierarchy_%1()
Parameters:		Geography, hierarchy table, type: 'missing', 'spurious additional' or 'multiple hierarchy'
Returns:		Nothing
Description:	Diff geography hierarchy table using dynamic method 4
				Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on
 */
DECLARE
	c2 CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM geolevels_%1
		 WHERE geography = l_geography
		 ORDER BY geolevel_id;
	c3 REFCURSOR;
	c4 CURSOR(l_geography VARCHAR, l_geolevel_id INTEGER) FOR
		SELECT * 
		  FROM geolevels_%1
		 WHERE geography   = l_geography
		   AND geolevel_id = l_geolevel_id
		 ORDER BY geolevel_id;
--
	c2_rec geolevels_%1%ROWTYPE;
	c3_rec RECORD;
	c4_rec geolevels_%1%ROWTYPE;
--
	sql_stmt 		VARCHAR;
	previous_geolevel_name 	VARCHAR:=NULL;
	i INTEGER;
	e INTEGER:=0;
	field INTEGER;
BEGIN
--
	sql_stmt:='WITH /* '||l_type||' */ ';
	i:=0;
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF l_type = 'multiple hierarchy' THEN
			IF i = 1 THEN
				NULL;
			ELSIF i > 2 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'SELECT COUNT(*) AS '||quote_ident(LOWER(c2_rec.geolevel_name)||'_total')||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'  FROM ('||E'\n';
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'SELECT COUNT(*) AS '||quote_ident(LOWER(c2_rec.geolevel_name)||'_total')||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'  FROM ('||E'\n';
			END IF;
		ELSE
			IF i != 1 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
			END IF;
			sql_stmt:=sql_stmt||E'\t'||'SELECT COUNT(*) AS '||quote_ident(LOWER(c2_rec.geolevel_name)||'_total')||E'\n';
			sql_stmt:=sql_stmt||E'\t'||'  FROM ('||E'\n';
		END IF;
		IF l_type = 'missing' THEN
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(l_hierarchytable))||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'EXCEPT'||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(c2_rec.lookup_table))||
				') '||quote_ident('as'||c2_rec.geolevel_id)||')'||E'\n';
		ELSIF l_type = 'spurious additional' THEN
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(c2_rec.lookup_table))||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'EXCEPT'||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(l_hierarchytable))||
				') '||quote_ident('as'||c2_rec.geolevel_id)||')'||E'\n';
		ELSIF l_type = 'multiple hierarchy' THEN
			IF previous_geolevel_name IS NOT NULL THEN
				sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||
					', COUNT(DISTINCT('||previous_geolevel_name||')) AS total'||E'\n';
				sql_stmt:=sql_stmt||E'\t'||E'\t'||'  FROM '||quote_ident(LOWER(l_hierarchytable))||E'\n';
				sql_stmt:=sql_stmt||E'\t'||E'\t'||' GROUP BY '||quote_ident(LOWER(c2_rec.geolevel_name))||E'\n';
				sql_stmt:=sql_stmt||E'\t'||E'\t'||'HAVING COUNT(DISTINCT('||previous_geolevel_name||')) > 1'||
					') '||quote_ident('as'||c2_rec.geolevel_id)||')'||E'\n';
			END IF;
		ELSE
			RAISE EXCEPTION 'Invalid check type: %, valid types are: ''missing'', ''spurious additional'', or ''multiple hierarchy''', 
				l_type::VARCHAR 	/* Check type */;
		END IF;
		previous_geolevel_name:=quote_ident(LOWER(c2_rec.geolevel_name));
	END LOOP;
	sql_stmt:=sql_stmt||'SELECT ARRAY[';
	i:=0;
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF l_type = 'multiple hierarchy' THEN
			IF i = 1 THEN
				NULL;
			ELSIF i > 2 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			END IF;
		ELSE
			IF i != 1 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			END IF;
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||'] AS res_array'||E'\n'||'FROM ';
	i:=0;
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF l_type = 'multiple hierarchy' THEN
			IF i = 1 THEN
				NULL;
			ELSIF i > 2 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id);
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id);
			END IF;
		ELSE
			IF i != 1 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id);
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id);
			END IF;
		END IF;
	END LOOP;
--
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	OPEN c3 FOR EXECUTE sql_stmt;
	FETCH c3 INTO c3_rec;
--
-- Process results array
--
	i:=0;
	FOREACH field IN ARRAY c3_rec.res_array LOOP
		i:=i+1;
		OPEN c4(l_geography, i);
		FETCH c4 INTO c4_rec;
		CLOSE c4;
		IF field != 0 THEN
			RAISE WARNING 'Geography: % geolevel %: [%] % codes: %', 
				l_geography::VARCHAR		/* Geography */, 
				i::VARCHAR					/* Geolevel ID */, 
				LOWER(c4_rec.geolevel_name)::VARCHAR	/* Geolevel name */, 
				l_type::VARCHAR				/* Check type */, 
				field::VARCHAR				/* Area ID */;
			e:=e+1;
		ELSE
			RAISE INFO 'Geography: % geolevel %: [%] no % codes', 
				l_geography::VARCHAR		/* Geography */, 
				i::VARCHAR					/* Geolevel ID */, 
				LOWER(c4_rec.geolevel_name)::VARCHAR	/* Geolevel name */, 
				l_type::VARCHAR				/* Check type */;
		END IF;
	END LOOP;
--
	RETURN e;
END;
$body$
LANGUAGE PLPGSQL