/*
 * SQL statement name: 	tileMaker_main_function.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *						2: geometry table; e.g. geometry_cb_2014_us_500k
 *						3: geolevels table; e.g. geolevels_cb_2014_us_500k
 *
 * Description:			Main tileMaker function. Create geoJSON tiles
 * Note:				%%%% becomes %% after substitution
 */
DO LANGUAGE plpgsql $$
DECLARE
	max_geolevel_id		INTEGER;
	max_zoomlevel 		INTEGER;
	l_areaid_count		INTEGER;
	start_geolevel_id	INTEGER;
--
	c1_maxgeolevel_id 	CURSOR FOR
		SELECT MAX(geolevel_id) AS max_geolevel_id,
	           MAX(zoomlevel) AS max_zoomlevel
	      FROM %2;
	c2_areaid_count 	CURSOR FOR	
		SELECT areaid_count
		  FROM %3	
		 WHERE geolevel_id = 1;
--
	num_rows 		INTEGER:=0;
	num_rows2 		INTEGER:=0;
	num_rows3 		INTEGER:=0;
	etp 			TIMESTAMP WITH TIME ZONE;
	stp2 			TIMESTAMP WITH TIME ZONE;
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	took 			INTERVAL;
	took2 			INTERVAL;
	took3 			INTERVAL;
	took4 			INTERVAL;
--
	tiles_per_s		NUMERIC;
--
	l_use_zoomlevel INTEGER;
	l_debug 		BOOLEAN;
BEGIN
	OPEN c1_maxgeolevel_id;
	FETCH c1_maxgeolevel_id INTO max_geolevel_id, max_zoomlevel;
	CLOSE c1_maxgeolevel_id;
	OPEN c2_areaid_count;
	FETCH c2_areaid_count INTO l_areaid_count;
	CLOSE c2_areaid_count;	
--	 
--	max_zoomlevel 	:=10;		/* Override for test purposes */
	IF l_areaid_count = 1 THEN	/* 0/0/0 tile only;  */			
		start_geolevel_id=2;	
	ELSE
		start_geolevel_id=1;
	END IF;
--
-- Create zoomleve l0 tiles. Intersect already created
--	
	FOR i IN 1 .. max_geolevel_id LOOP
--			
		stp2:=clock_timestamp();
--		num_rows3:=tileMaker_aggregator_%1(i, 0, l_debug);	
		etp:=clock_timestamp();
		took4:=age(etp, stp2);
--			
		IF num_rows3 > 0 THEN
			took:=age(etp, stp);
			tiles_per_s:=ROUND(num_rows3::NUMERIC/EXTRACT(EPOCH FROM took4)::NUMERIC, 1);
			RAISE INFO 'Processed % tile for geolevel id %/% zoomlevel: %/% in %s, %s total;% tiles/s', 
				num_rows3, 
				i, max_geolevel_id, 0, max_zoomlevel,  
				ROUND(EXTRACT(EPOCH FROM took4)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
				tiles_per_s;	
		END IF;
	END LOOP;
		
--
-- Timing; 3 zoomlevels to:
--
-- Zoomlevel 7: 1 minute (75)
-- Zoomlevel 8: 3 minutes (321..260..154..218..230..217..166 seconds with tile aggregation)
-- Zoomlevel 9: 8 minutes (673..627..460)
-- Zoomlevel 10: 24 minutes (1473)
-- Zoomlevel 11: 80 minutes (4810)
--
	FOR i IN start_geolevel_id .. max_geolevel_id LOOP
		FOR j IN 1 .. max_zoomlevel LOOP
			l_debug:=FALSE;
			IF j = max_zoomlevel AND i = max_geolevel_id THEN
				l_debug:=TRUE;
			END IF;
			l_use_zoomlevel=j;
			IF j<6 THEN 
				l_use_zoomlevel=6;
			END IF;
			stp2:=clock_timestamp();
			num_rows:=tileMaker_intersector_%1(i, j, l_use_zoomlevel, l_debug);
			etp:=clock_timestamp();
			took2:=age(etp, stp2);
--			
			stp2:=clock_timestamp();
			num_rows2:=tileMaker_intersector2_%1(i, j, l_use_zoomlevel, l_debug);	
			etp:=clock_timestamp();
			took3:=age(etp, stp2);
--			
			stp2:=clock_timestamp();
--			num_rows3:=tileMaker_aggregator_cb_2014_us_500k(i, j, l_debug);	/* Replaced by pgTileMaker.js */
			etp:=clock_timestamp();
			took4:=age(etp, stp2);
--			
			took:=age(etp, stp);
			tiles_per_s:=ROUND((num_rows+num_rows2+num_rows3)::NUMERIC/EXTRACT(EPOCH FROM took2)::NUMERIC, 1);
			RAISE INFO 'Processed %+% total areaid intersects for geolevel id %/% zoomlevel: %/% in %+%s+%s, %s total; % intesects/s', 
				num_rows, num_rows2,  
				i, max_geolevel_id, j, max_zoomlevel, 
				ROUND(EXTRACT(EPOCH FROM took2)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took3)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took4)::NUMERIC, 1), 
				ROUND(EXTRACT(EPOCH FROM took)::NUMERIC, 1),
				tiles_per_s;			
		END LOOP;	
	END LOOP;
END;
$$