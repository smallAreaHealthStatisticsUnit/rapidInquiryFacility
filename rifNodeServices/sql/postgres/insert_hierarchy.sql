DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	insert_hierarchy.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *
 * Description:			Create insert statement into hierarchy table
 * Note:				%%%% becomes %% after substitution
 */
	l_geography VARCHAR:='%1';
--
	c1_hier CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM geolevels_%1
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id;
	c2_hier CURSOR(l_geography VARCHAR) FOR
		SELECT * FROM pg_indexes
		 WHERE schemaname = USER
		   AND tablename IN (SELECT DISTINCT LOWER(hierarchytable)
				       FROM geography_%1
				      WHERE geography = l_geography)
		 ORDER BY 1;	
	c3 REFCURSOR;
	c4_hier CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM geography_%1
		 WHERE geography = l_geography;
	c1_rec geolevels_%1%ROWTYPE;
	c2_rec geography_%1%ROWTYPE;
	c3_rec	RECORD;
	c4_rec geography_%1%ROWTYPE;
--
	columns			VARCHAR;
	sql_stmt	 	VARCHAR;
	i				INTEGER:=0;
	num_geolevels	INTEGER:=0;
--
	geolevel_name			VARCHAR[];
	shapefile_table      		VARCHAR[];
 	shapefile_area_id_column	VARCHAR[];
 	shapefile_desc_column		VARCHAR[];
--
BEGIN
--
	OPEN c4_hier(l_geography);
	FETCH c4_hier INTO c4_rec;
	CLOSE c4_hier;
--
	IF c4_rec.geography IS NULL THEN
		RAISE EXCEPTION 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */;
	END IF;	
--
	 RAISE INFO 'Populating % geography hierarchy table: %',
		l_geography, c4_rec.hierarchytable;
--
-- INSERT statement
--
	sql_stmt:='INSERT INTO '||quote_ident(LOWER(c4_rec.hierarchytable))||' (';
	FOR c1_rec IN c1_hier(l_geography) LOOP
		i:=i+1;
		geolevel_name[i]:=quote_ident(LOWER(c1_rec.geolevel_name));
		shapefile_table[i]:=quote_ident(LOWER(c1_rec.shapefile_table));      	
 		shapefile_area_id_column[i]:=quote_ident(LOWER(c1_rec.shapefile_area_id_column));	
 		shapefile_desc_column[i]:=quote_ident(LOWER(c1_rec.shapefile_desc_column));	
		IF i = 1 THEN
			columns:=geolevel_name[i];
		ELSE
			columns:=columns||', '||geolevel_name[i];
		END IF;
	END LOOP;
	num_geolevels:=i;
	IF num_geolevels = 0 THEN
		RAISE EXCEPTION 'No rows found in: geolevels_%1 for geography %', 
			l_geography::VARCHAR /* Geography */;
	END IF;
	sql_stmt:=sql_stmt||columns||')'||E'\n';
--
-- Start SELECT statement; WITH clause; aggreagate geometries
--
-- Removed ST_Union for performance reasons
--

--
-- WITH clause - INTERSECTION
--
	FOR i IN 1 .. num_geolevels LOOP /* WITH clause - INTERSECTION */
/* E.g

x23 AS (
	SELECT a2.areaid AS level2, a3.areaid AS level3,
  	       ST_Area(a3.geom) AS a3_area,
	       ST_Area(ST_Intersection(a2.geom, a3.geom)) a23_area
          FROM a2 CROSS JOIN a3
	 WHERE ST_Intersects(a2.geom, a3.geom)
 */
		IF i = 1 THEN
			sql_stmt:=sql_stmt||
				'WITH x'||i||i+1||' AS ( /* Subqueries x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||': intersection aggregate geometries starting from the lowest resolution.'||E'\n'||
         		      	E'\t'||'       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.'||E'\n'||
 	       			E'\t'||'       Calculate the area of the higher resolution geolevel and the area of the intersected area */'||E'\n'||
				'SELECT a'||i||'.areaid AS '||geolevel_name[i]||', a'||i+1||'.areaid AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.geom_11) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.geom_11, a'||i+1||'.geom_11)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||shapefile_table[i]||' a'||i||' CROSS JOIN '||shapefile_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.geom_11, a'||i+1||'.geom_11)'||E'\n'||
				'), ';
		ELSIF i < (num_geolevels-1) THEN
			sql_stmt:=sql_stmt||
				'x'||i||i+1||' AS ( /* Subqueries x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||': intersection aggregate geometries starting from the lowest resolution.'||E'\n'||
         		      	E'\t'||'       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.'||E'\n'||
 	       			E'\t'||'       Calculate the area of the higher resolution geolevel and the area of the intersected area */'||E'\n'||
				'SELECT a'||i||'.areaid AS '||geolevel_name[i]||', a'||i+1||'.areaid AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.geom_11) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.geom_11, a'||i+1||'.geom_11)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||shapefile_table[i]||' a'||i||' CROSS JOIN '||shapefile_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.geom_11, a'||i+1||'.geom_11)'||E'\n'||
				'), ';
		ELSIF i < num_geolevels THEN
/* E.g.

 x34 AS (
	SELECT a3.level3, a4.level4,
	       total_a3_gid, total_a4_gid,
  	       ST_Area(a4.geom) AS a4_area,
	       ST_Area(ST_Intersection(a3.geom, a4.geom)) a34_area
          FROM a3 CROSS JOIN a4
	 WHERE ST_Intersects(a3.geom, a4.geom)
*/
			sql_stmt:=sql_stmt||
				'x'||i||i+1||' AS ( /* Subqueries x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||': intersection aggregate geometries starting from the lowest resolution.'||E'\n'||
         		      	E'\t'||'       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.'||E'\n'||
 	       			E'\t'||'       Calculate the area of the higher resolution geolevel and the area of the intersected area */'||E'\n'||
				'SELECT a'||i||'.areaid AS '||geolevel_name[i]||', a'||i+1||'.areaid AS '||geolevel_name[i+1]||','||E'\n'||
				'       ST_Area(a'||i+1||'.geom_11) AS a'||i+1||'_area,'||E'\n'||
				'       ST_Area(ST_Intersection(a'||i||'.geom_11, a'||i+1||'.geom_11)) AS a'||i||i+1||'_area'||E'\n'||
				'  FROM '||shapefile_table[i]||' a'||i||' CROSS JOIN '||shapefile_table[i+1]||' a'||i+1||''||E'\n'||
				' WHERE ST_Intersects(a'||i||'.geom_11, a'||i+1||'.geom_11)'||E'\n'||
				'), ';
		END IF;
	END LOOP;
--
-- Compute intersected area, order analytically
--

/*
y AS ( 
	SELECT x12.level1, x12.level2, x23.level3, x34.level4, 
	       CASE WHEN a2_area > 0 THEN a12_area/a2_area ELSE NULL END test12,
	       CASE WHEN a3_area > 0 THEN a23_area/a3_area ELSE NULL END test23,
	       CASE WHEN a4_area > 0 THEN a34_area/a4_area ELSE NULL END test34,
	       MAX(a12_area/a2_area) OVER (PARTITION BY x12.level2) AS max12,
	       MAX(a23_area/a3_area) OVER (PARTITION BY x23.level3) AS max23,
	       MAX(a34_area/a4_area) OVER (PARTITION BY x34.level4) AS max34
	  FROM x12, x23, x34
	 WHERE x12.level2 = x23.level2
   	   AND x23.level3 = x34.level3
)
 */
	sql_stmt:=sql_stmt||
		'y AS ( /* Join x'||i||i+1||' ... x'||num_geolevels-1||num_geolevels||
			'intersections, pass through the computed areas, compute intersected area/higher resolution geolevel area,'||E'\n'||
		E'\t'||'     compute maximum intersected area/higher resolution geolevel area using an analytic partition of all'||E'\n'||
		E'\t'||'     duplicate higher resolution geolevels */'||E'\n';
	FOR i IN 1 .. num_geolevels LOOP /* x12.level1, x12.level2, x23.level3, x34.level4, */
		IF i < num_geolevels THEN
			IF i = 1 THEN
				sql_stmt:=sql_stmt||
					'SELECT x'||i||i+1||'.'||geolevel_name[i]||', '||E'\n';
			END IF;
			sql_stmt:=sql_stmt||
				'       x'||i||i+1||'.'||geolevel_name[i+1]||', '||E'\n';
		END IF;
	END LOOP;
	FOR i IN 1 .. num_geolevels LOOP /* CASE MAX analytic clause */ 
		IF i < num_geolevels THEN
			sql_stmt:=sql_stmt||
	    		   	'       CASE WHEN x'||i||i+1||'.a'||i+1||'_area > 0 THEN x'||i||i+1||'.a'||i||i+1||'_area/x'||i||i+1||'.a'||i+1||
				'_area ELSE NULL END test'||i||i+1||','||E'\n';
			sql_stmt:=sql_stmt||
				'       MAX(x'||i||i+1||'.a'||i||i+1||'_area/x'||i||i+1||'.a'||i+1||'_area)'||
				' OVER (PARTITION BY x'||i||i+1||'.'||geolevel_name[i+1]||') AS max'||i||i+1||','||E'\n';
		END IF;
	END LOOP;
	sql_stmt:=SUBSTR(sql_stmt, 1, LENGTH(sql_stmt)-LENGTH(','||E'\n')) /* Chop off last ",\n" */||E'\n';
	FOR i IN 1 .. num_geolevels LOOP /* FROM clause */ 
		IF i < num_geolevels THEN
			IF i = 1 THEN
				sql_stmt:=sql_stmt||
					'  FROM x'||i||i+1;
			ELSE
				sql_stmt:=sql_stmt||
					', x'||i||i+1;
			END IF;
		END IF;
	END LOOP;
	FOR i IN 1 .. (num_geolevels-2) LOOP /* WHERE clause */ 
		IF i = 1 THEN
			sql_stmt:=sql_stmt||E'\n'||
				' WHERE x'||i||i+1||'.'||geolevel_name[i+1]||' = x'||i+1||i+2||'.'||geolevel_name[i+1];
		ELSE
			sql_stmt:=sql_stmt||E'\n'||
				'   AND x'||i||i+1||'.'||geolevel_name[i+1]||' = x'||i+1||i+2||'.'||geolevel_name[i+1];
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||E'\n'||')'||E'\n';
--
-- Final SELECT
--
	sql_stmt:=sql_stmt||'SELECT /* Select y intersection, eliminating duplicates using selecting the lower geolevel resolution'||E'\n'||
         E'\t'||' with the largest intersection by area for each (higher resolution) geolevel */'||E'\n'||'       '||columns||E'\n';
	sql_stmt:=sql_stmt||'  FROM y'||E'\n';
/*
SELECT level1, level2, level3, level4,
  FROM y
 WHERE max12 = test12
   AND max23 = test23
   AND max34 = test34
 ORDER BY 1, 2, 3, 4;  
 */
	FOR i IN 1 .. num_geolevels LOOP /* FROM clause */ 
		IF i < num_geolevels THEN
			IF i = 1 THEN
				sql_stmt:=sql_stmt||' WHERE max'||i||i+1||' = test'||i||i+1||E'\n';
			ELSE
				sql_stmt:=sql_stmt||'   AND max'||i||i+1||' = test'||i||i+1||E'\n';
			END IF;
		END IF;
	END LOOP;
	
	sql_stmt:=sql_stmt||' ORDER BY 1';
	FOR i IN 2 .. num_geolevels LOOP /* ORDER BY clause */ 	
		sql_stmt:=sql_stmt||', '||i;
	END LOOP;
	
	RAISE NOTICE 'SQL> %;', sql_stmt;
	EXECUTE sql_stmt;
--
-- Check rows were inserted
--
	sql_stmt:='SELECT COUNT(*) AS total FROM '||quote_ident(LOWER(c4_rec.hierarchytable));
	OPEN c3 FOR EXECUTE sql_stmt;
	FETCH c3 INTO c3_rec;
	CLOSE c3;	
	IF c3_rec.total = 0 THEN
		RAISE EXCEPTION 'No rows found in % geography hierarchy table: %', 
			l_geography::VARCHAR 			/* Geography */,
			quote_ident(LOWER(c4_rec.hierarchytable))	/* Hierarchy table */;
	END IF;
--
-- Re-index
--
	FOR c2_rec IN c2_hier(l_geography) LOOP
		sql_stmt:='REINDEX INDEX /* '||quote_ident(c2_rec.tablename)||' */ '||quote_ident(c2_rec.indexname);
		RAISE NOTICE 'SQL> %;', sql_stmt;
		EXECUTE sql_stmt;
	END LOOP;
--
-- Analyze
--
	sql_stmt:='ANALYZE VERBOSE '||quote_ident(LOWER(c4_rec.hierarchytable));
	RAISE NOTICE 'SQL> %;', sql_stmt;
	EXECUTE sql_stmt;
END;
$$