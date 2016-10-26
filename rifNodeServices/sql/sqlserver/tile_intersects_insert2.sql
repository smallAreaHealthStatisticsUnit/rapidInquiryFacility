/*
 * SQL statement name: 	tile_intersects_insert2.sql
 * Type:				MS SQL Server SQL
 * Parameters:
 *						1: Geometry table name; e.g. geometry_cb_2014_us_500k
 *						2: Geolevels table name; e.g. geolevels_cb_2014_us_500k
 *						3: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *
 * Description:			Insert into tile intersects table
 * Note:				%% becomes % after substitution
 */
DECLARE c1_maxgeolevel_id 	CURSOR FOR
		SELECT MAX(geolevel_id) AS max_geolevel_id,
	           MAX(zoomlevel) AS max_zoomlevel
	      FROM geometry_cb_2014_us_500k;
DECLARE c2_areaid_count 	CURSOR FOR	
		SELECT areaid_count
		  FROM geolevels_cb_2014_us_500k	
		 WHERE geolevel_id = 1;
--
DECLARE @max_geolevel_id INTEGER;
DECLARE @max_zoomlevel INTEGER;
DECLARE @areaid_count INTEGER;
DECLARE @start_geolevel_id INTEGER;
DECLARE @l_use_zoomlevel INTEGER;
DECLARE @geolevel_id INTEGER;
DECLARE @zoomlevel INTEGER;
--
DECLARE @i INTEGER;
DECLARE @j INTEGER;
--
DECLARE @rowc INTEGER;
DECLARE @rowc2 INTEGER;
--
DECLARE @pstart DATETIME;
DECLARE @lstart DATETIME;
--
DECLARE @lend DATETIME;
DECLARE @etime TIME;
DECLARE @cesecs VARCHAR(40);
DECLARE @esecs NUMERIC;

DECLARE @lend2 DATETIME;
DECLARE @etime2 TIME;
DECLARE @cesecs2 VARCHAR(40);
DECLARE @esecs2 NUMERIC;

DECLARE @isecs NUMERIC;
DECLARE @cisecs VARCHAR(40);

DECLARE @lend3 DATETIME;
DECLARE @etime3 TIME;
DECLARE @cesecs3 VARCHAR(40);
DECLARE @esecs3 NUMERIC;

BEGIN
	OPEN c1_maxgeolevel_id;
	FETCH c1_maxgeolevel_id INTO @max_geolevel_id, @max_zoomlevel;
	CLOSE c1_maxgeolevel_id;
	DEALLOCATE c1_maxgeolevel_id;
--
	OPEN c2_areaid_count;
	FETCH c2_areaid_count INTO @areaid_count;
	CLOSE c2_areaid_count;
	DEALLOCATE c2_areaid_count;
--
	IF @areaid_count = 1 	/* 0/0/0 tile only;  */			
		SET @start_geolevel_id=2;	
	ELSE
		SET @start_geolevel_id=1;
--
	SET @i=@start_geolevel_id;
--
	SET @pstart = GETDATE();	
--
	WHILE @i <= @max_geolevel_id /* FOR i IN start_geolevel_id .. max_geolevel_id LOOP */
	BEGIN
		SET @geolevel_id=@i;	
		SET @j=1;	
		WHILE @j <= @max_zoomlevel /* FOR j IN 1 .. max_zoomlevel LOOP */
		BEGIN
			SET @lstart = GETDATE();
			SET @zoomlevel=@j;			
--
			SET @l_use_zoomlevel=@zoomlevel;
			IF @zoomlevel<6  
				SET @l_use_zoomlevel=6;
--			
-- Intersector2: tile intersects table INSERT function. Zoomlevels <6 use zoomlevel 6 data
--				Insert tile area id intersections missing where not in the previous layer; 
--				this is usually due to it being simplified out of existance.  
--	
			WITH a	AS (
				SELECT b.zoomlevel AS zoomlevel, b.x_mintile, b.x_maxtile, b.y_mintile, b.y_maxtile	  
				  FROM tile_limits_cb_2014_us_500k b
				 WHERE @zoomlevel = b.zoomlevel
			), x AS (
				SELECT zoomlevel, z.IntValue AS x_series
				  FROM a CROSS APPLY $(USERNAME).generate_series(x_mintile, x_maxtile, 1) z
			), y AS (	 
				SELECT zoomlevel, z.IntValue AS y_series	
				  FROM a CROSS APPLY $(USERNAME).generate_series(y_mintile, y_maxtile, 1) z       
			), b AS (
				SELECT x.zoomlevel, 
					   x.x_series AS x, 
					   y.y_series AS y,      
					   $(USERNAME).tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
					   $(USERNAME).tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
					   $(USERNAME).tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
					   $(USERNAME).tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
				  FROM x, y
				 WHERE x.zoomlevel = y.zoomlevel
			), c AS ( /* Calculate bounding box, parent X/Y min */
				SELECT b.zoomlevel, 
					   b.x,
					   b.y, 
					   $(USERNAME).tileMaker_STMakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) AS bbox,
					   $(USERNAME).tileMaker_latitude2tile(b.ymin, b.zoomlevel-1) AS parent_ymin,
					   $(USERNAME).tileMaker_longitude2tile(b.xmin, b.zoomlevel-1) AS parent_xmin
				  FROM b
			), d AS (
				SELECT c.zoomlevel, c.x, c.y, c.bbox, p.areaid, p.within
				  FROM c, tile_intersects_cb_2014_us_500k p /* Parent */
				 WHERE p.geolevel_id = @geolevel_id
				   AND p.zoomlevel 	 = @zoomlevel -1/* Join to parent tile from previous geolevel_id; i.e. exclude if not present */
				   AND c.parent_xmin = p.x  
				   AND c.parent_ymin = p.y	
			), e AS (
				SELECT d.zoomlevel, d.x, d.y, d.bbox, d.areaid
				  FROM d
				 WHERE NOT EXISTS (SELECT c2.areaid
									 FROM tile_intersects_cb_2014_us_500k c2
									WHERE c2.geolevel_id = @geolevel_id
									  AND c2.zoomlevel   = @zoomlevel
									  AND c2.x           = d.x
									  AND c2.y           = d.y
									  AND c2.areaid      = d.areaid)
			), f AS (
				SELECT e.zoomlevel, 
				       @geolevel_id AS geolevel_id, 
					   e.x, e.y, e.bbox, e2.areaid, e2.geom
				  FROM e, geometry_cb_2014_us_500k e2
				 WHERE e2.zoomlevel    = @l_use_zoomlevel
				   AND e2.geolevel_id  = @geolevel_id
				   AND e2.areaid       = e.areaid
				   AND e.bbox.STIntersects(e2.geom.STEnvelope()) = 1	/* Intersect by bounding box */
				   AND e.bbox.STIntersects(e2.geom) = 1 				/* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
			)
			INSERT INTO tile_intersects_cb_2014_us_500k(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 			
			SELECT f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y, f.bbox, f.geom, 
				   NULL AS optimised_geojson,
				   1 AS within
			  FROM f
			 WHERE NOT f.bbox.STWithin(f.geom) = 1 /* Exclude any tile bounding completely within the area */
			 ORDER BY f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y;
--
			SET @rowc = @@ROWCOUNT;
			SET @etime = CAST(GETDATE() - @lstart AS TIME);
			SET @esecs = (DATEPART(MILLISECOND, @etime));
			SET @esecs = @esecs/10;
			SET @cesecs = CAST((DATEPART(HOUR, @etime) * 3600) + (DATEPART(MINUTE, @etime) * 60) + (DATEPART(SECOND, @etime)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs, 1) AS VARCHAR(40));
--
-- Run 2
--
			SET @lstart = GETDATE();	
--			
-- Intersector2: tile intersects table INSERT function. Zoomlevels <6 use zoomlevel 6 data
--				Insert tile area id intersections missing where not in the previous layer; 
--				this is usually due to it being simplified out of existance.  
--	
			WITH a AS (
				SELECT DISTINCT geolevel_id, areaid
				  FROM geometry_cb_2014_us_500k
				 WHERE geolevel_id = @geolevel_id
				   AND zoomlevel   = @zoomlevel
				EXCEPT 
				SELECT DISTINCT geolevel_id, areaid
				  FROM tile_intersects_cb_2014_us_500k a
				 WHERE geolevel_id = @geolevel_id
				   AND zoomlevel   = @zoomlevel
			), b AS (
				SELECT a.geolevel_id, a.areaid, b.geom.STEnvelope() AS bbox, b.geom
				  FROM a, geometry_cb_2014_us_500k b
				 WHERE a.geolevel_id = @geolevel_id
				   AND zoomlevel     = @zoomlevel
				   AND a.areaid      = b.areaid
				   AND NOT b.geom.STIsEmpty() = 1
			), c1 AS (
				SELECT @zoomlevel AS zoomlevel, 
					   b.geolevel_id, 
					   b.areaid,
					   $(USERNAME).tileMaker_latitude2tile(geometry::EnvelopeAggregate(bbox).STPointN(1).STY   /* Ymin */, @zoomlevel) AS y_mintile,
					   $(USERNAME).tileMaker_longitude2tile(geometry::EnvelopeAggregate(bbox).STPointN(1).STX  /* Xmin */, @zoomlevel) AS x_mintile,
					   $(USERNAME).tileMaker_latitude2tile(geometry::EnvelopeAggregate(bbox).STPointN(3).STY /* Ymax */, @zoomlevel) AS y_maxtile,
					   $(USERNAME).tileMaker_longitude2tile(geometry::EnvelopeAggregate(bbox).STPointN(3).STX /* Xmax */, @zoomlevel) AS x_maxtile
				   FROM b
				  GROUP BY b.geolevel_id, 
					   b.areaid
			), c AS (
				SELECT c1.zoomlevel,
					   c1.geolevel_id, 
					   c1.areaid,
					   c1.x_mintile,
					   c1.y_mintile,
					   c1.x_maxtile,
					   c1.y_maxtile,
					   b.geom
				  FROM c1, b
				 WHERE c1.areaid = b.areaid
			), x AS (
				SELECT c.zoomlevel, 
					   c.geolevel_id, 
					   c.areaid,
					   z.IntValue AS x_series
				  FROM c CROSS APPLY $(USERNAME).generate_series(x_mintile, x_maxtile, 1) z 
			), y AS (	 
				SELECT c.zoomlevel, 
					   c.geolevel_id, 
					   c.areaid,
					   z.IntValue AS y_series	
				  FROM c CROSS APPLY $(USERNAME).generate_series(y_mintile, y_maxtile, 1) z 
			), d AS (
				SELECT x.zoomlevel, 
					   x.geolevel_id, 
					   x.areaid,
					   x.x_series AS x, 
					   y.y_series AS y,      
					   $(USERNAME).tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
					   $(USERNAME).tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
					   $(USERNAME).tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
					   $(USERNAME).tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
				  FROM x, y
				 WHERE x.zoomlevel   = y.zoomlevel	
				   AND x.geolevel_id = y.geolevel_id
				   AND x.areaid      = y.areaid
			), e AS (
				SELECT d.zoomlevel, 
					   d.geolevel_id, 
					   d.areaid,
					   d.x,
					   d.y, 
					   $(USERNAME).tileMaker_STMakeEnvelope(d.xmin, d.ymin, d.xmax, d.ymax, 4326) AS bbox
				  FROM d
			), f1 AS (
				SELECT DISTINCT e.zoomlevel, 
					   e.geolevel_id, 
					   e.areaid, 
					   e.x,
					   e.y
				  FROM e
				 WHERE NOT EXISTS (SELECT c2.areaid
									 FROM tile_intersects_cb_2014_us_500k c2
									WHERE c2.geolevel_id = @geolevel_id
									  AND c2.zoomlevel   = @zoomlevel
									  AND c2.x           = e.x
									  AND c2.y           = e.y	
									  AND c2.areaid      = e.areaid)
			), f AS (
				SELECT f1.zoomlevel, 
					   f1.geolevel_id, 
					   f1.areaid, 
					   f1.x,
					   f1.y,
					   e.bbox
				  FROM f1, e
				 WHERE e.areaid      = f1.areaid
				   AND e.x           = f1.x
				   AND e.y           = f1.y
				   AND e.zoomlevel   = f1.zoomlevel
				   AND e.geolevel_id = f1.geolevel_id
			), g AS (
					SELECT f.zoomlevel, f.geolevel_id, f.x, f.y, f.bbox, e2.areaid, e2.geom
					  FROM f, geometry_cb_2014_us_500k e2
					 WHERE e2.zoomlevel    = @l_use_zoomlevel
					   AND e2.geolevel_id  = @geolevel_id
					   AND e2.areaid       = f.areaid
				       AND f.bbox.STIntersects(e2.geom.STEnvelope()) = 1	/* Intersect by bounding box */
					   AND f.bbox.STIntersects(e2.geom) = 1 /* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
			)
			INSERT INTO tile_intersects_cb_2014_us_500k(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
			SELECT geolevel_id, zoomlevel, areaid, x, y, bbox, geom,	
				   NULL AS optimised_geojson,
				   g.bbox.STWithin(g.geom) AS within
			  FROM g 
			 ORDER BY geolevel_id, zoomlevel, areaid, x, y;	
--			 
			SET @rowc2 = @@ROWCOUNT;			
			SET @etime2 = CAST(GETDATE() - @lstart AS TIME);
			SET @esecs2 = (DATEPART(MILLISECOND, @etime2));
			SET @esecs2 = @esecs/210;
			SET @cesecs2 = CAST((DATEPART(HOUR, @etime2) * 3600) + (DATEPART(MINUTE, @etime2) * 60) + (DATEPART(SECOND, @etime2)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs2, 1) AS VARCHAR(40));
--
			SET @etime3 = CAST(GETDATE() - @pstart AS TIME);
			SET @esecs3 = (DATEPART(MILLISECOND, @etime3));
			SET @esecs3 = @esecs3/10;
			SET @cesecs3 = CAST((DATEPART(HOUR, @etime3) * 3600) + (DATEPART(MINUTE, @etime3) * 60) + (DATEPART(SECOND, @etime3)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs3, 1) AS VARCHAR(40));
					
			SET @isecs=0;
			SET @cisecs=CAST(ROUND(@isecs, 1) AS VARCHAR(40))
--
-- Processed 57+0 total areaid intersects, 3 tiles for geolevel id 2/3 zoomlevel: 1/11 in 0.7+0.0s+0.3s, 1.9s total; 92.1 intesects/s
			RAISERROR('%d/%d Processed %d+%d for geolevel id: %d/%d; zoomlevel: %d/%d; in %s+%s %s total; %s intesects/s)', 10, 1,
				@i, @j, @rowc, @rowc2, @geolevel_id, @max_geolevel_id, @zoomlevel, @max_zoomlevel, @cesecs, @cesecs2, @cesecs3, @cisecs) WITH NOWAIT;
			SET @j+=1;	
		END;
		SET @i+=1;	
	END;
--				
END
