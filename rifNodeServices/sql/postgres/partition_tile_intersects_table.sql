DO LANGUAGE plpgsql $$
DECLARE
/*
 * SQL statement name: 	partition_tile_intersects_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: tile intersects table; e.g. tile_intersects_cb_2014_us_500k
 *						2: Max zoomlevel; e.g. 11
 *						3: Geolevels table; e.g. geolevels_cb_2014_us_500k
 * 						4: Number of geolevels (e.g. 3)
 *
 * Description:			Create partitioned tables and insert function for tile intersects table; comment partitioned tables and columns
 * Note:				%%%% becomes %% after substitution
 */
DECLARE
	c2_areaid_count 	CURSOR FOR	
		SELECT areaid_count
		  FROM %3	
		 WHERE geolevel_id = 1;
	l_areaid_count	INTEGER;
	end_zoomlevel	INTEGER;
--	
	l_table 	Text:='%1';
	sql_stmt	VARCHAR[];
	trigger_sql	VARCHAR;
BEGIN
	OPEN c2_areaid_count;
	FETCH c2_areaid_count INTO l_areaid_count;
	CLOSE c2_areaid_count;	
--
	FOR i IN 1 .. %4 LOOP
		IF i = 1 AND l_areaid_count = 1 THEN
			end_zoomlevel=0;	
		ELSE
			end_zoomlevel=%2;
		END IF;
--	
		FOR j IN 0 .. end_zoomlevel LOOP
			sql_stmt[COALESCE(array_length(sql_stmt, 1), 0)]:='CREATE TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' ('||E'\n'||
					  '    CHECK ( geolevel_id = '||i::Text||' AND zoomlevel = '||j::Text||' )'||E'\n'||
					  ') INHERITS ('||l_table||')';	
--
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' IS ''Tile area ID intersects''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.geolevel_id IS ''ID for ordering (1=lowest resolution). Up to 99 supported.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.zoomlevel IS ''Zoom level: 0 to %2. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel %2.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.areaid IS ''Area ID.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.x IS ''X tile number. From 0 to (2**<zoomlevel>)-1.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.y IS ''Y tile number. From 0 to (2**<zoomlevel>)-1.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.optimised_geojson IS ''Tile multipolygon in GeoJSON format, optimised for zoomlevel N.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.within IS ''Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.bbox IS ''Bounding box of tile as a polygon.''';
			sql_stmt[array_length(sql_stmt, 1)]:='COMMENT ON COLUMN '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||
							'.geom IS ''Geometry of area.''';
--
			sql_stmt[array_length(sql_stmt, 1)]:='ALTER TABLE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||E'\n'||
					  ' ADD CONSTRAINT '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_pk PRIMARY KEY (areaid, x, y)';
			sql_stmt[array_length(sql_stmt, 1)]:='CREATE INDEX '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||'_geom_gix'||E'\n'||
					  ' ON '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text||' USING GIST (geom);';
-- Analyze
			sql_stmt[array_length(sql_stmt, 1)]:='ANALYZE '||l_table||
							'_geolevel_id_'||i::Text||'_zoomlevel_'||j::Text;
							
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
					'Fix the %_insert_trigger() function!'','||E'\n'||
		'			NEW.zoomlevel, NEW.geolevel_id, l_table;'||E'\n'||
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