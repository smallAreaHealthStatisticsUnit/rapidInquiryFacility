DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	partition_geometry_table2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *						2: Max zoomlevel; e.g. 11
 *
 * Description:			Add primary key, index and cluster (convert to index organized table)
 * Note:				%%%% becomes %% after substitution
 */
	l_table 	Text:='%1';
	sql_stmt	VARCHAR[];
BEGIN
	FOR i IN 1 .. 3 LOOP
		FOR j IN 6 .. %2 LOOP
			sql_stmt[COALESCE(array_length(sql_stmt, 1), 0)]:='ALTER TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||E'\n'||
					  ' ADD CONSTRAINT '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_pk PRIMARY KEY (areaid)';	
			sql_stmt[array_length(sql_stmt, 1)]:='CREATE INDEX '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_geom_gix'||E'\n'||
					  ' ON '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' USING GIST (geom);';
-- Convert to IOT
			sql_stmt[array_length(sql_stmt, 1)]:='CLUSTER VERBOSE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||E'\n'||
					  ' USING '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_pk';
-- Analyze
			sql_stmt[array_length(sql_stmt, 1)]:='ANALYZE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text;
		END LOOP;
	END LOOP;
--
	FOR i IN 0 .. (array_length(sql_stmt, 1)-1) LOOP
		RAISE INFO 'SQL> %;', sql_stmt[i];
		EXECUTE sql_stmt[i];
	END LOOP;
END;
$$