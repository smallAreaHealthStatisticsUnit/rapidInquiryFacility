/*
 * SQL statement name: 	tile_intersects_insert2.sql
 * Type:				MS SQL Server SQL
 * Parameters:
 *						1: Geometry table name; e.g. geometry_cb_2014_us_500k
 *						2: Geolevels table name; e.g. geolevels_cb_2014_us_500k
 *						3: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k
 *						4: Tile limits table name; e.g. tile_limits_cb_2014_us_500k
 *
 * Description:			Insert into tile intersects table
 * Note:				%% becomes % after substitution
 *
 * To performance trace add to script:
 *
 * SET STATISTICS PROFILE ON
 * SET STATISTICS TIME ON 
 *
 * You may need to change #temp to temp (i.e. make it a real table to profile it)
 */
--
-- For testing
--
--DECLARE @start_zoomlevel INTEGER=6;
--DECLARE @start_geolevel_id INTEGER=3;
DECLARE @start_zoomlevel INTEGER=1;
DECLARE @start_geolevel_id INTEGER=1;

DECLARE c1_maxgeolevel_id 	CURSOR FOR
		SELECT MAX(geolevel_id) AS max_geolevel_id,
	           MAX(zoomlevel) AS max_zoomlevel
	      FROM %1;
DECLARE c2_areaid_count 	CURSOR FOR	
		SELECT areaid_count
		  FROM %2	
		 WHERE geolevel_id = 1;
--
DECLARE @max_geolevel_id INTEGER;
DECLARE @max_zoomlevel INTEGER;
DECLARE @areaid_count INTEGER;
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
DECLARE @sstart DATETIME;
DECLARE @lstart DATETIME;
--
DECLARE @etime TIME;
--
DECLARE @isecs   NUMERIC;
DECLARE @cisecs  VARCHAR(40);
--
DECLARE @cesecs  VARCHAR(40);
DECLARE @esecs   NUMERIC;
DECLARE @cesecs1 VARCHAR(40);
DECLARE @cesecs2 VARCHAR(40);
DECLARE @cesecs3 VARCHAR(40);
DECLARE @cesecs4 VARCHAR(40);
DECLARE @cesecs5 VARCHAR(40);
--
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
	DELETE FROM %3
	 WHERE zoomlevel >= @start_zoomlevel;
--
-- Override for test purposes
--
--	SET @max_zoomlevel=7;
--
	SET @pstart = GETDATE();	
--
	WHILE @i <= @max_geolevel_id /* FOR i IN start_geolevel_id .. max_geolevel_id LOOP */
	BEGIN
		SET @geolevel_id=@i;	
		SET @j=@start_zoomlevel;	
		WHILE @j <= @max_zoomlevel /* FOR j IN 1 .. max_zoomlevel LOOP */
		BEGIN
			SET @sstart = GETDATE();
			SET @lstart = GETDATE();
			SET @zoomlevel=@j;			
--
			SET @l_use_zoomlevel=@zoomlevel;
			IF @zoomlevel<6  
				SET @l_use_zoomlevel=6;
--			
-- Intersector2: tile intersects table INSERT function. Zoomlevels <6 use zoomlevel 6 data
--				
-- Step 1: Calculate bounding box, parent X/Y min
--		   This is separate to prevent SQL Server unnesting the cross join because it thinks it is very inefficent
--		   This could probably be improved with manual statistics for the function, but there is no method for this in SQL Server
--  
			WITH a	AS (
				SELECT b.zoomlevel AS zoomlevel, b.x_mintile, b.x_maxtile, b.y_mintile, b.y_maxtile	  
				  FROM %4 b
				 WHERE @zoomlevel = b.zoomlevel
			), x AS (
				SELECT zoomlevel, z.IntValue AS x_series
				  FROM a CROSS APPLY $(SQLCMDUSER).generate_series(x_mintile, x_maxtile, 1) z
			), y AS (	 
				SELECT zoomlevel, z.IntValue AS y_series	
				  FROM a CROSS APPLY $(SQLCMDUSER).generate_series(y_mintile, y_maxtile, 1) z       
			), b AS (
				SELECT x.zoomlevel, 
					   x.x_series AS x, 
					   y.y_series AS y,      
					   $(SQLCMDUSER).tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
					   $(SQLCMDUSER).tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
					   $(SQLCMDUSER).tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
					   $(SQLCMDUSER).tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
				  FROM x, y /* Explicit cross join */
				 WHERE x.zoomlevel = y.zoomlevel
			) /* Calculate bounding box, parent X/Y min */
			SELECT b.zoomlevel, 
				   b.x,
				   b.y, 
				   $(SQLCMDUSER).tileMaker_STMakeEnvelope(b.xmin, b.ymin, b.xmax, b.ymax, 4326) AS bbox,
				   $(SQLCMDUSER).tileMaker_latitude2tile(b.ymin, b.zoomlevel-1) AS parent_ymin,
				   $(SQLCMDUSER).tileMaker_longitude2tile(b.xmin, b.zoomlevel-1) AS parent_xmin
			  INTO #temp2
			  FROM b
			 ORDER BY b.zoomlevel, b.x, b.y;
--
			SET @etime = CAST(GETDATE() - @lstart AS TIME);
			SET @esecs = (DATEPART(MILLISECOND, @etime));
			SET @esecs = @esecs/10;
			SET @cesecs1 = CAST((DATEPART(HOUR, @etime) * 3600) + (DATEPART(MINUTE, @etime) * 60) + (DATEPART(SECOND, @etime)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs, 1) AS VARCHAR(40));
			SET @lstart = GETDATE();
--				
-- Step 2: Join to parent tile from previous geolevel_id; i.e. exclude if not present, intersect by bounding box (this in combination 
--         removes most tiles not containing data efficiently). This was the cause of most performance problems when SQL Server decided to 
--         change this order. The COUNT(*) always returns 1; it is to prevent SQL Server unesting the query!
-- 		   This may cause problems in future if SQL Server becomes intelligent enough to spot this; although hopefully it will by then 
-- 	       spot the STIntersect() is an expensive operation even with indexes
--
			WITH d AS ( /* Get parent tiles */
				SELECT p.x, p.y, p.areaid, COUNT(p.x) AS total
				  FROM %3 p /* Parent */
				 WHERE p.zoomlevel    = @zoomlevel -1 	/* previous geolevel_id: c.zoomlevel -1 */
				   AND p.geolevel_id  = @geolevel_id 
				 GROUP BY p.x, p.y, p.areaid
			), e AS (	
				SELECT c.zoomlevel, c.x, c.y, d.areaid, c.bbox
				  FROM #temp2 c, d
				 WHERE c.parent_xmin = d.x  			/* Join to parent tile from previous geolevel_id; i.e. exclude if not present */
				   AND c.parent_ymin = d.y
			)
			SELECT e.zoomlevel, e.x, e.y, e.areaid, e.bbox, e2.geom		
			  INTO #temp
			  FROM e, %1 e2
			 WHERE e2.zoomlevel    = @l_use_zoomlevel
			   AND e2.geolevel_id  = @geolevel_id
			   AND e2.areaid       = e.areaid
			   AND e.bbox.STIntersects(e2.bbox) = 1		/* Intersect by bounding box */	
			 ORDER BY e.zoomlevel, e.x, e.y, e.areaid;
			DROP TABLE #temp2;
--
			SET @etime = CAST(GETDATE() - @lstart AS TIME);
			SET @esecs = (DATEPART(MILLISECOND, @etime));
			SET @esecs = @esecs/10;
			SET @cesecs5 = CAST((DATEPART(HOUR, @etime) * 3600) + (DATEPART(MINUTE, @etime) * 60) + (DATEPART(SECOND, @etime)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs, 1) AS VARCHAR(40));
			SET @lstart = GETDATE();
--
-- Do NOT index #temp; it i slower and gives the wrong answer
--
/*
			ALTER TABLE #temp ALTER COLUMN x INTEGER NOT NULL;
			ALTER TABLE #temp ALTER COLUMN y INTEGER NOT NULL;
			ALTER TABLE #temp ALTER COLUMN areaid INTEGER NOT NULL;
			ALTER TABLE #temp ADD PRIMARY KEY (x, y, areaid);
			CREATE SPATIAL INDEX #temp_gix ON #temp (geom)
				WITH ( BOUNDING_BOX = (xmin=-179.148909, ymin=-14.548699000000001, xmax=179.77847, ymax=71.36516200000001));	
			CREATE SPATIAL INDEX #temp_gix2 ON #temp (bbox)
				WITH ( BOUNDING_BOX = (xmin=-179.148909, ymin=-14.548699000000001, xmax=179.77847, ymax=71.36516200000001));			
--
			SET @etime = CAST(GETDATE() - @lstart AS TIME);
			SET @esecs = (DATEPART(MILLISECOND, @etime));
			SET @esecs = @esecs/10;
			SET @cesecs6 = CAST((DATEPART(HOUR, @etime) * 3600) + (DATEPART(MINUTE, @etime) * 60) + (DATEPART(SECOND, @etime)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs, 1) AS VARCHAR(40));
			SET @lstart = GETDATE();
			*/
--		
-- Step 3: intersects tile bounding box with geometry, exclude any tile bounded completely within the area
--
			WITH f AS (
				SELECT @geolevel_id AS geolevel_id, e.zoomlevel, e.x, e.y, e.bbox, e.areaid, e.geom
				  FROM #temp e  
				 WHERE e.bbox.STIntersects(e.geom) = 1 /* intersects tile bounding box with geometry */ 
			)
			INSERT INTO %3(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 			
			SELECT f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y, f.bbox, f.geom, 
				   NULL AS optimised_geojson,
				   1 AS within 
			  FROM f
			 WHERE NOT f.bbox.STWithin(f.geom) = 1 /* Exclude any tile bounded completely within the area */
			 ORDER BY f.geolevel_id, f.zoomlevel, f.areaid, f.x, f.y;
--
			SET @rowc = @@ROWCOUNT;
			SET @etime = CAST(GETDATE() - @lstart AS TIME);
			SET @esecs = (DATEPART(MILLISECOND, @etime));
			SET @esecs = @esecs/10;
			SET @cesecs2 = CAST((DATEPART(HOUR, @etime) * 3600) + (DATEPART(MINUTE, @etime) * 60) + (DATEPART(SECOND, @etime)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs, 1) AS VARCHAR(40));
--						
			DROP TABLE #temp;
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
				  FROM %1
				 WHERE geolevel_id = @geolevel_id
				   AND zoomlevel   = @zoomlevel
				EXCEPT 
				SELECT DISTINCT geolevel_id, areaid
				  FROM %3 a
				 WHERE geolevel_id = @geolevel_id
				   AND zoomlevel   = @zoomlevel
			), b AS (
				SELECT a.geolevel_id, a.areaid, b.geom.STEnvelope() AS bbox, b.geom
				  FROM a, %1 b
				 WHERE a.geolevel_id = @geolevel_id
				   AND zoomlevel     = @zoomlevel
				   AND a.areaid      = b.areaid
				   AND NOT b.geom.STIsEmpty() = 1
			), c1 AS (
				SELECT @zoomlevel AS zoomlevel, 
					   b.geolevel_id, 
					   b.areaid,
					   $(SQLCMDUSER).tileMaker_latitude2tile(geometry::EnvelopeAggregate(bbox).STPointN(1).STY  /* Ymin */, @zoomlevel) AS y_mintile,
					   $(SQLCMDUSER).tileMaker_longitude2tile(geometry::EnvelopeAggregate(bbox).STPointN(1).STX /* Xmin */, @zoomlevel) AS x_mintile,
					   $(SQLCMDUSER).tileMaker_latitude2tile(geometry::EnvelopeAggregate(bbox).STPointN(3).STY  /* Ymax */, @zoomlevel) AS y_maxtile,
					   $(SQLCMDUSER).tileMaker_longitude2tile(geometry::EnvelopeAggregate(bbox).STPointN(3).STX /* Xmax */, @zoomlevel) AS x_maxtile
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
				  FROM c CROSS APPLY $(SQLCMDUSER).generate_series(x_mintile, x_maxtile, 1) z 
			), y AS (	 
				SELECT c.zoomlevel, 
					   c.geolevel_id, 
					   c.areaid,
					   z.IntValue AS y_series	
				  FROM c CROSS APPLY $(SQLCMDUSER).generate_series(y_mintile, y_maxtile, 1) z 
			), d AS (
				SELECT x.zoomlevel, 
					   x.geolevel_id, 
					   x.areaid,
					   x.x_series AS x, 
					   y.y_series AS y,      
					   $(SQLCMDUSER).tileMaker_tile2longitude(x.x_series, x.zoomlevel) AS xmin, 
					   $(SQLCMDUSER).tileMaker_tile2latitude(y.y_series, x.zoomlevel) AS ymin,
					   $(SQLCMDUSER).tileMaker_tile2longitude(x.x_series+1, x.zoomlevel) AS xmax, 
					   $(SQLCMDUSER).tileMaker_tile2latitude(y.y_series+1, x.zoomlevel) AS ymax
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
					   $(SQLCMDUSER).tileMaker_STMakeEnvelope(d.xmin, d.ymin, d.xmax, d.ymax, 4326) AS bbox
				  FROM d
			), f1 AS (
				SELECT DISTINCT e.zoomlevel, 
					   e.geolevel_id, 
					   e.areaid, 
					   e.x,
					   e.y
				  FROM e
				 WHERE NOT EXISTS (SELECT c2.areaid
									 FROM %3 c2
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
					  FROM f, %1 e2
					 WHERE e2.zoomlevel    = @l_use_zoomlevel
					   AND e2.geolevel_id  = @geolevel_id
					   AND e2.areaid       = f.areaid
				       AND f.bbox.STIntersects(e2.bbox) = 1	/* Intersect by bounding box */
					   AND f.bbox.STIntersects(e2.geom) = 1 /* intersects: (e.bbox && e.geom) is slower as it generates many more tiles */
			)
			INSERT INTO %3(geolevel_id, zoomlevel, areaid, x, y, bbox, geom, optimised_geojson, within) 
			SELECT geolevel_id, zoomlevel, areaid, x, y, bbox, geom,	
				   NULL AS optimised_geojson,
				   g.bbox.STWithin(g.geom) AS within
			  FROM g 
			 ORDER BY geolevel_id, zoomlevel, areaid, x, y;	
--			 
			SET @rowc2 = @@ROWCOUNT;	
--			
			SET @etime = CAST(GETDATE() - @lstart AS TIME);
			SET @esecs = (DATEPART(MILLISECOND, @etime));
			SET @esecs = @esecs/10;
			SET @cesecs3 = CAST((DATEPART(HOUR, @etime) * 3600) + (DATEPART(MINUTE, @etime) * 60) + (DATEPART(SECOND, @etime)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs, 1) AS VARCHAR(40));
			SET @lstart = GETDATE();	
--
-- Rebuild tile intersects index
--
			ALTER INDEX ALL ON %3 REORGANIZE; 
--			
			SET @etime = CAST(GETDATE() - @lstart AS TIME);
			SET @esecs = (DATEPART(MILLISECOND, @etime));
			SET @esecs = @esecs/10;
			SET @cesecs4 = CAST((DATEPART(HOUR, @etime) * 3600) + (DATEPART(MINUTE, @etime) * 60) + (DATEPART(SECOND, @etime)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs, 1) AS VARCHAR(40));
--
-- Calculate overall time since start
--
			SET @etime = CAST(GETDATE() - @pstart AS TIME);
			SET @esecs = (DATEPART(MILLISECOND, @etime));
			SET @esecs = @esecs/10;
			SET @cesecs = CAST((DATEPART(HOUR, @etime) * 3600) + (DATEPART(MINUTE, @etime) * 60) + (DATEPART(SECOND, @etime)) AS VARCHAR(40)) + 
					'.' + CAST(ROUND(@esecs, 1) AS VARCHAR(40));
--		
-- Calculate intersects/s for this geolevel/zoomlevel combination
--			
			SET @etime = CAST(GETDATE() - @sstart AS TIME); -- For all queries in fop loop
			IF (DATEPART(SECOND, @etime) > 0)
				SET @isecs=@rowc/((DATEPART(HOUR, @etime) * 3600) + (DATEPART(MINUTE, @etime) * 60) + (DATEPART(SECOND, @etime)));
			ELSE 
				SET @isecs=0;
			SET @cisecs=CAST(ROUND(@isecs, 1) AS VARCHAR(40))
--
-- Processed 57+0 total areaid intersects, 3 tiles for geolevel id 2/3 zoomlevel: 1/11 in 0.7+0.0s+0.3s, 1.9s total; 92.1 intesects/s
--
			RAISERROR('Processed %d+%d for geolevel id: %d/%d; zoomlevel: %d/%d; in #temp2: %s, #temp: %s, insert: %s, insert2: %s, re-index: %s, %s total; %s intesects/s)', 10, 1,
				@rowc, @rowc2, @geolevel_id, @max_geolevel_id, @zoomlevel, @max_zoomlevel, 
				@cesecs1, -- #temp2 create
				@cesecs5, -- #temp create
				@cesecs2, -- INSERT into tile intersects table
				@cesecs3, -- 2nd INSERT into tile intersects table (Insert tile area id intersections missing where not in the previous layer)
				@cesecs4, -- Re-index tile intersects table
				@cesecs, -- Overall running total
				@cisecs  -- Intersects/sec
				) WITH NOWAIT;
			SET @j+=1;	
		END;
		SET @i+=1;	
	END;
--				
END