DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	partition_geometry_table1.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *						2: Max zoomlevel; e.g. 11
 * 						3: Number of geolevels (e.g. 3)
 *
 * Description:			Create partitioned tables and insert function for geometry table; comment partitioned tables and columns
 * Note:				%%%% becomes %% after substitution
 */
	l_table 	Text:='%1';
	sql_stmt	VARCHAR[];
	trigger_sql	VARCHAR;
BEGIN
	FOR i IN 1 .. %3 LOOP
		FOR j IN 6 .. %2 LOOP
			sql_stmt[COALESCE(array_length(sql_stmt, 1), 0)]:='CREATE TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' ('||E'\n'||
					  '    CHECK ( geolevel_id = '||i::Text||' AND zoomlevel = '||j::Text||' )'||E'\n'||
					  ') INHERITS ('||l_table||')';	
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
					  ' IS ''All geolevels geometry combined into a single table.  Geolevel '||
							i::Text||', zoomlevel '||j::Text||' partition.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
							'.zoomlevel IS ''Zoom level: 0 to Max zoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
							'.areaid IS ''Area ID.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
							'.geolevel_id IS ''ID for ordering (1=lowest resolution). Up to 99 supported.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text|| 
							'.geom IS ''Geometry data in SRID 4326 (WGS84).''';
			IF trigger_sql IS NULL THEN
				trigger_sql:='IF ( NEW.zoomlevel = '||j::Text||' AND NEW.geolevel_id = '||i::Text||' ) THEN'||E'\n'||
							' 	INSERT INTO '||l_table||'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' VALUES (NEW.*);'||E'\n';
			ELSE
				trigger_sql:=trigger_sql||
							'ELSIF ( NEW.zoomlevel = '||j::Text||' AND NEW.geolevel_id = '||i::Text||' ) THEN'||E'\n'||
							' 	INSERT INTO '||l_table||'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' VALUES (NEW.*);'||E'\n';
			END IF;
		END LOOP;
	END LOOP;
	
	sql_stmt[array_length(sql_stmt, 1)]:='CREATE OR REPLACE FUNCTION '||l_table||'_insert_trigger()'||E'\n'||
		'RETURNS TRIGGER AS $trigger$'||E'\n'||
		'BEGIN'||E'\n'||
		trigger_sql||
		'    ELSE'||E'\n'||
		'        RAISE EXCEPTION ''Zoomlevel (%) or geolevel_id(%) out of range. '||
					'Fix the '||l_table||'_insert_trigger() function!'','||E'\n'||
		'			NEW.zoomlevel, NEW.geolevel_id;'||E'\n'||
		'    END IF;'||E'\n'||
		'    RETURN NULL;'||E'\n'||
		'END;'||E'\n'||
		'$trigger$'||E'\n'||
		'LANGUAGE plpgsql';
--
	FOR i IN 0 .. (array_length(sql_stmt, 1)-1) LOOP
		RAISE INFO 'SQL> %;', sql_stmt[i];
		EXECUTE sql_stmt[i];
	END LOOP;
END;
$$