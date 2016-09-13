DECLARE @l_geography AS VARCHAR(200)='%1';
/*
 * SQL statement name: 	insert_hierarchy.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *
 * Description:			Create insert statement into hierarchy table
 * Note:				%%%% becomes %% after substitution
 */
--
	
--
DECLARE c1_hier CURSOR FOR
		SELECT geolevel_id, geolevel_name, shapefile_table, shapefile_area_id_column, shapefile_desc_column 
		  FROM geolevels_%1
		 WHERE geography = @l_geography
		 ORDER BY geography, geolevel_id;
DECLARE c2_hier CURSOR FOR
		SELECT i.name AS index_name, 
		       object_name(object_id) AS table_name
  		  FROM sys.indexes i
		 WHERE i.object_id = (
					 SELECT object_id(LOWER(hierarchytable))
				       FROM geography_%1
				      WHERE geography = @l_geography)
		 ORDER BY 1;		 
DECLARE c4_hier CURSOR FOR		 
	SELECT geography, hierarchytable
		  FROM geography_%1
		 WHERE geography = @l_geography;
DECLARE @c3 CURSOR;
DECLARE @geography		AS	VARCHAR(200);
DECLARE @hierarchytable AS 	VARCHAR(200);
--
DECLARE @columns		AS	VARCHAR(200);
DECLARE @sql_stmt	 	AS	NVARCHAR(max);
DECLARE @num_geolevels	AS	INTEGER=0;
--
DECLARE @geolevel_id 				AS INTEGER;
DECLARE @geolevel_name 				AS VARCHAR(200);
DECLARE @shapefile_table		 	AS VARCHAR(200);
DECLARE @shapefile_area_id_column 	AS VARCHAR(200);
DECLARE @shapefile_desc_column 		AS VARCHAR(200);
DECLARE @n_geolevel_name 			AS VARCHAR(200);
DECLARE @n_shapefile_table		 	AS VARCHAR(200);
DECLARE @n_shapefile_area_id_column AS VARCHAR(200);
DECLARE @n_shapefile_desc_column 	AS VARCHAR(200);
--
DECLARE @tablename	 	AS VARCHAR(200);
DECLARE @indexname	 	AS VARCHAR(200);

DECLARE @i 				AS INTEGER=0;
--
DECLARE @crlf			AS VARCHAR(2)=CHAR(10)+CHAR(13);
DECLARE @tab			AS VARCHAR(1)=CHAR(9);
--
DECLARE @rowcount		AS INTEGER=0;

--
BEGIN
--
	OPEN c4_hier;
	FETCH c4_hier INTO @geography, @hierarchytable;
	CLOSE c4_hier;
	DEALLOCATE c4_hier;
--
	IF @geography IS NULL
		RAISERROR('geography: %s not found', 16, 1, @l_geography	/* Geography */);
--
	 PRINT 'Populating ' + @l_geography + ' geography hierarchy table: ' + @hierarchytable + 
		'; spid: ' + CAST(@@spid AS VARCHAR);

	SET @num_geolevels=0;
	OPEN c1_hier;
	FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
								 @shapefile_area_id_column, @shapefile_desc_column;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @num_geolevels+=1;	
		IF @num_geolevels = 1 
			SET @columns=LOWER(@geolevel_name);
		ELSE
			SET @columns+=', ' + LOWER(@geolevel_name);
		FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
									 @shapefile_area_id_column, @shapefile_desc_column;
	END;
	CLOSE c1_hier;
--	
	IF @num_geolevels = 0 
		RAISERROR('No rows found in: geolevels_%s for geography %s', 16, 1, @l_geography, @l_geography);
--
-- CTE x<n><n+1> - CROSS JOINs with intersections
--
	OPEN c1_hier;
	FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
								 @shapefile_area_id_column, @shapefile_desc_column;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i+=1;

		DECLARE c1a_hier CURSOR FOR
				SELECT geolevel_name, shapefile_table, shapefile_area_id_column, shapefile_desc_column 
				  FROM geolevels_%1
				 WHERE geography   = @l_geography
				   AND geolevel_id = @geolevel_id+1
				 ORDER BY geography, geolevel_id;		
		OPEN c1a_hier;
		FETCH NEXT FROM c1a_hier INTO @n_geolevel_name, @n_shapefile_table, 
									  @n_shapefile_area_id_column, @n_shapefile_desc_column;
		CLOSE c1a_hier;
		DEALLOCATE c1a_hier;

--		PRINT 'i: ' + CAST(@i AS VARCHAR) + '; num_geolevels: ' + CAST(@num_geolevels AS VARCHAR) + 
--			'; geolevel_name: ' + @geolevel_name + '; n_geolevel_name: ' + @n_geolevel_name;
		IF @i = 1
/* E.g

SELECT a1.areaid AS cb_2014_us_nation_5m,
	   a2.areaid AS cb_2014_us_state_500k,
	   a2.geom_11.STArea() AS a2_area,
	   a1.geom_11.STIntersection(a2.geom_11).STArea() AS a12_area
  INTO dbo.#x12
  FROM cb_2014_us_nation_5m a1   CROSS JOIN cb_2014_us_state_500k a2
 WHERE a1.geom_11.STIntersects(a2.geom_11) = 1;
	
Postgres Original: 
	
x23 AS (
	SELECT a2.areaid AS level2,
       	   a3.areaid AS level3,
  	       ST_Area(a3.geom) AS a3_area,
	       ST_Area(ST_Intersection(a2.geom, a3.geom)) AS a23_area
          FROM a2 CROSS JOIN a3
	 WHERE ST_Intersects(a2.geom, a3.geom)
 */		
			BEGIN
				SET @sql_stmt=			
					'SELECT /* Subqueries x' +
						CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + ' ... x' +
						CAST(@num_geolevels-1 AS VARCHAR) + CAST(@num_geolevels AS VARCHAR) +
						': intersection aggregate geometries starting from the lowest resolution.' + @crlf + 
						@tab + '       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.' + @crlf +
						@tab + '       Calculate the area of the higher resolution geolevel and the area of the intersected area */' + @crlf +
					'       a' + CAST(@i AS VARCHAR) + '.areaid AS ' + @geolevel_name + ',' + @crlf + 
					'       a' + CAST(@i+1 AS VARCHAR) + '.areaid AS ' + @n_geolevel_name + ',' + @crlf +
					'       a' + CAST(@i+1 AS VARCHAR) + '.geom_11.STArea() AS a' + CAST(@i+1 AS VARCHAR) + '_area,' + @crlf + 
					'       a' + CAST(@i AS VARCHAR) + '.geom_11.STIntersection(a' + CAST(@i+1 AS VARCHAR) + '.geom_11).STArea() AS a' + 
						CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '_area' + @crlf + 
				    '  INTO ##x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '_' + CAST(@@spid AS VARCHAR) + @crlf + 
					'  FROM ' + @shapefile_table + ' a' + CAST(@i AS VARCHAR) + 
					' CROSS JOIN ' + @n_shapefile_table + ' a' + CAST(@i+1 AS VARCHAR) + '' + @crlf + 
					' WHERE a' + CAST(@i AS VARCHAR) + '.geom_11.STIntersects(a' + CAST(@i+1 AS VARCHAR) + 
						'.geom_11) = 1';
				PRINT 'SQL> ' + @sql_stmt;
				EXECUTE @rowcount = sp_executesql @sql_stmt;	
			END;			
		ELSE IF @i < (@num_geolevels-1) 
/* E.g

SELECT a2.areaid AS cb_2014_us_state_500k,
	   a3.areaid AS cb_2014_us_county_500k,
	   a3.geom_11.STArea() AS a3_area,
	   a2.geom_11.STIntersection(a3.geom_11).ST_Area() AS a23_area
  INTO dbo.#x23
  FROM cb_2014_us_state_500k a2  CROSS JOIN cb_2014_us_county_500k a3
 WHERE a2.geom_11.ST_Intersects(a3.geom_11) = 1;

*/
			BEGIN
				SET @sql_stmt=
					'SELECT /* Subqueries x' + 
						CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + ' ... x' + 
						CAST(@num_geolevels-1 AS VARCHAR) + CAST(@num_geolevels AS VARCHAR) + 
						': intersection aggregate geometries starting from the lowest resolution.' + @crlf + 
						@tab + '       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.' + @crlf + 
						@tab + '       Calculate the area of the higher resolution geolevel and the area of the intersected area */' + @crlf + 
					'       a' + CAST(@i AS VARCHAR) + '.areaid AS ' + @geolevel_name + ',' + @crlf + 
					'       a' + CAST(@i+1 AS VARCHAR) + '.areaid AS ' + @n_geolevel_name + ',' + @crlf + 
					'       a' + CAST(@i+1 AS VARCHAR) + '.geom_11.STArea() AS a' + CAST(@i+1 AS VARCHAR) + '_area,' + @crlf + 
					'       a' + CAST(@i AS VARCHAR) + '.geom_11.STIntersection(a' + CAST(@i+1 AS VARCHAR) + '.geom_11).STArea() AS a' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '_area' + @crlf + 
				    '  INTO ##x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '_' + CAST(@@spid AS VARCHAR) + @crlf + 
					'  FROM ' + @shapefile_table + ' a' + CAST(@i AS VARCHAR) + 
					' CROSS JOIN ' + @n_shapefile_table + ' a' + CAST(@i+1 AS VARCHAR) + '' + @crlf + 
					' WHERE a' + CAST(@i AS VARCHAR) + '.geom_11.ST_Intersects(a' + CAST(@i+1 AS VARCHAR) + 
						'.geom_11) = 1';
				PRINT 'SQL> ' + @sql_stmt;
				EXECUTE @rowcount = sp_executesql @sql_stmt;
			END;
		ELSE IF @i < @num_geolevels
/* E.g.

SELECT a2.areaid AS cb_2014_us_state_500k,
	   a3.areaid AS cb_2014_us_county_500k,
	   a3.geom_11.STArea() AS a3_area,
	   a2.geom_11.STIntersection(a3.geom_11).STArea() AS a23_area
  INTO dbo.#x23
  FROM cb_2014_us_state_500k a2 CROSS JOIN cb_2014_us_county_500k a3
 WHERE a2.geom_11.STIntersects(a3.geom_11) = 1;
		
Postgres Original: 

 x34 AS (
	SELECT a3.level3, 
	       a4.level4,
  	       ST_Area(a4.geom) AS a4_area,
	       ST_Area(ST_Intersection(a3.geom, a4.geom)) a34_area
          FROM a3 CROSS JOIN a4
	 WHERE ST_Intersects(a3.geom, a4.geom)
*/
			BEGIN
				SET @sql_stmt=
					'SELECT /* Subqueries x' + 
						CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + ' ... x' + 
						CAST(@num_geolevels-1 AS VARCHAR) + CAST(@num_geolevels AS VARCHAR) + 
						': intersection aggregate geometries starting from the lowest resolution.' + @crlf + 
						@tab + '       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.' + @crlf + 
						@tab + '       Calculate the area of the higher resolution geolevel and the area of the intersected area */' + @crlf + 
					'       a' + CAST(@i AS VARCHAR) + '.areaid AS ' + @geolevel_name + ',' + @crlf + 
					'       a' + CAST(@i+1 AS VARCHAR) + '.areaid AS ' + @n_geolevel_name + ',' + @crlf + 
					'       a' + CAST(@i+1 AS VARCHAR) + '.geom_11.STArea() AS a' + CAST(@i+1 AS VARCHAR) + '_area,' + @crlf + 
					'       a' + CAST(@i AS VARCHAR) + '.geom_11.STIntersection(a' + CAST(@i+1 AS VARCHAR) + '.geom_11).STArea() AS a' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '_area' + @crlf + 
				    '  INTO ##x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '_' + CAST(@@spid AS VARCHAR) + @crlf + 
					'  FROM ' + @shapefile_table + ' a' + CAST(@i AS VARCHAR) + ' CROSS JOIN ' + @n_shapefile_table + ' a' + CAST(@i+1 AS VARCHAR) + '' + @crlf + 
					' WHERE a' + CAST(@i AS VARCHAR) + '.geom_11.STIntersects(a' + CAST(@i+1 AS VARCHAR) + 
						'.geom_11) = 1';
				PRINT 'SQL> ' + @sql_stmt;
				EXECUTE @rowcount = sp_executesql @sql_stmt;
			END;
		FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
									 @shapefile_area_id_column, @shapefile_desc_column;
	END;
	CLOSE c1_hier;
--
-- CTE: y - compute intersected area, order analytically
--

/*
	SELECT x12.level1, x12.level2, x23.level3, x34.level4, 
	       CASE WHEN a2_area > 0 THEN a12_area/a2_area ELSE NULL END test12,
	       CASE WHEN a3_area > 0 THEN a23_area/a3_area ELSE NULL END test23,
	       CASE WHEN a4_area > 0 THEN a34_area/a4_area ELSE NULL END test34,
	       MAX(a12_area/a2_area) OVER (PARTITION BY x12.level2) AS max12,
	       MAX(a23_area/a3_area) OVER (PARTITION BY x23.level3) AS max23,
	       MAX(a34_area/a4_area) OVER (PARTITION BY x34.level4) AS max34
	  INTO #y
	  FROM x12, x23, x34
	 WHERE x12.level2 = x23.level2
   	   AND x23.level3 = x34.level3;
)
 */
	SET @sql_stmt=@crlf + 
		'SELECT /* Join x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + ' ... x' + CAST(@num_geolevels-1 AS VARCHAR) + CAST(@num_geolevels AS VARCHAR) + 
			'intersections, pass through the computed areas, compute intersected area/higher resolution geolevel area,' + @crlf + 
		@tab + '     compute maximum intersected area/higher resolution geolevel area using an analytic partition of all' + @crlf + 
		@tab + '     duplicate higher resolution geolevels */' + @crlf;
--
-- First line of SELECT statement
--
	SET @i=0;
	OPEN c1_hier;
	FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
								 @shapefile_area_id_column, @shapefile_desc_column;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i+=1;	
									 
		DECLARE c1a_hier CURSOR FOR
				SELECT geolevel_name, shapefile_table, shapefile_area_id_column, shapefile_desc_column 
				  FROM geolevels_%1
				 WHERE geography   = @l_geography
				   AND geolevel_id = @geolevel_id+1
				 ORDER BY geography, geolevel_id;		
		OPEN c1a_hier;
		FETCH NEXT FROM c1a_hier INTO @n_geolevel_name, @n_shapefile_table, 
									  @n_shapefile_area_id_column, @n_shapefile_desc_column;
		CLOSE c1a_hier;
		DEALLOCATE c1a_hier;
		
--		PRINT 'i: ' + CAST(@i AS VARCHAR) + '; num_geolevels: ' + CAST(@num_geolevels AS VARCHAR) + 
--			'; geolevel_name: ' + @geolevel_name + '; n_geolevel_name: ' + @n_geolevel_name;
			
		IF @i < @num_geolevels 
		BEGIN
			IF @i = 1
				SET @sql_stmt+=
					@tab + '       x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '.' + 
						@geolevel_name + ', ' + @crlf;
			SET @sql_stmt+=
				@tab + '       x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '.' + 
					@n_geolevel_name + ', ' + @crlf;
		END;
		
		FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
									 @shapefile_area_id_column, @shapefile_desc_column;
	END;
	CLOSE c1_hier;
--
-- Add CASE, MAX lines
-- 
	SET @i=0;
	OPEN c1_hier;
	FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
								 @shapefile_area_id_column, @shapefile_desc_column;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i+=1;	
									 
		DECLARE c1a_hier CURSOR FOR
				SELECT geolevel_name, shapefile_table, shapefile_area_id_column, shapefile_desc_column 
				  FROM geolevels_%1
				 WHERE geography   = @l_geography
				   AND geolevel_id = @geolevel_id+1
				 ORDER BY geography, geolevel_id;		
		OPEN c1a_hier;
		FETCH NEXT FROM c1a_hier INTO @n_geolevel_name, @n_shapefile_table, 
									  @n_shapefile_area_id_column, @n_shapefile_desc_column;
		CLOSE c1a_hier;
		DEALLOCATE c1a_hier;
		
--		PRINT 'i: ' + CAST(@i AS VARCHAR) + '; num_geolevels: ' + CAST(@num_geolevels AS VARCHAR) + 
--			'; geolevel_name: ' + @geolevel_name + '; n_geolevel_name: ' + @n_geolevel_name;
			
		IF @i < @num_geolevels 
		BEGIN
			SET @sql_stmt+=
	    		@tab + '       CASE WHEN x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '.a' + 
						CAST(@i+1 AS VARCHAR) + '_area > 0 THEN x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + 
						'.a' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '_area/x' + 
						CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '.a' + CAST(@i+1 AS VARCHAR) + 
					'_area ELSE NULL END test' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + ',' + @crlf +
				@tab + '       MAX(x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '.a' + 
					CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '_area/x' + 
					CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '.a' + CAST(@i+1 AS VARCHAR) + '_area)' + 
					' OVER (PARTITION BY x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + 
					'.' + @n_geolevel_name + ') AS max' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + ',' + @crlf;		
		END;
		
		FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
									 @shapefile_area_id_column, @shapefile_desc_column;
	END;
	CLOSE c1_hier;
--
-- Trim last CRLF
--
	SET @sql_stmt=SUBSTRING(@sql_stmt, 1, LEN(@sql_stmt)-LEN(','+@crlf)) /* Chop off last ",\r\n" */ + @crlf;
--
-- Add INTO clause
--
	SET @sql_stmt+='  INTO ##y' + '_' + CAST(@@spid AS VARCHAR) + @crlf;
--
-- Add FROM clause
-- 
	SET @i=0;
	OPEN c1_hier;
	FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
								 @shapefile_area_id_column, @shapefile_desc_column;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i+=1;	
		
--		PRINT 'i: ' + CAST(@i AS VARCHAR) + '; num_geolevels: ' + CAST(@num_geolevels AS VARCHAR) + 
--			'; geolevel_name: ' + @geolevel_name + '; n_geolevel_name: ' + @n_geolevel_name;
			
		IF @i < @num_geolevels 
		BEGIN
			IF @i = 1 
				SET @sql_stmt+='  FROM ##x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) +
					'_' + CAST(@@spid AS VARCHAR) +
					' x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR);
			ELSE
				SET @sql_stmt+=', ##x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + 
					'_' + CAST(@@spid AS VARCHAR) +
					' x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR);
		END;
		
		FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
									 @shapefile_area_id_column, @shapefile_desc_column;
	END;
	CLOSE c1_hier;
--
-- Add WHERE clause
-- 
	SET @i=0;
	OPEN c1_hier;
	FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
								 @shapefile_area_id_column, @shapefile_desc_column;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i+=1;	
									 
		DECLARE c1a_hier CURSOR FOR
				SELECT geolevel_name, shapefile_table, shapefile_area_id_column, shapefile_desc_column 
				  FROM geolevels_%1
				 WHERE geography   = @l_geography
				   AND geolevel_id = @geolevel_id+1
				 ORDER BY geography, geolevel_id;		
		OPEN c1a_hier;
		FETCH NEXT FROM c1a_hier INTO @n_geolevel_name, @n_shapefile_table, 
									  @n_shapefile_area_id_column, @n_shapefile_desc_column;
		CLOSE c1a_hier;
		DEALLOCATE c1a_hier;
		
--		PRINT 'i: ' + CAST(@i AS VARCHAR) + '; num_geolevels: ' + CAST(@num_geolevels AS VARCHAR) + 
--			'; geolevel_name: ' + @geolevel_name + '; n_geolevel_name: ' + @n_geolevel_name;
			
		IF @i < (@num_geolevels-1) /* FOR i IN 1 .. (num_geolevels-2) LOOP */
		BEGIN
			IF @i = 1 
				SET @sql_stmt+=@crlf + 
					' WHERE x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '.' + @n_geolevel_name + 
						' = x' + CAST(@i+1 AS VARCHAR) + CAST(@i+2 AS VARCHAR) + '.' + @n_geolevel_name;
			ELSE
				SET @sql_stmt+=@crlf + 
					'   AND x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + '.' + @n_geolevel_name + 
						' = x' + CAST(@i+1 AS VARCHAR) + CAST(@i+2 AS VARCHAR) + '.' + @n_geolevel_name;
		END;		
		FETCH NEXT FROM c1_hier INTO @geolevel_id, @geolevel_name, @shapefile_table, 
									 @shapefile_area_id_column, @shapefile_desc_column;
	END;
	CLOSE c1_hier;	
--
-- Run SQL to create Y
--
	PRINT 'SQL> ' + @sql_stmt;
	EXECUTE @rowcount = sp_executesql @sql_stmt;
--
-- Drop x_NN temporary tables
--
	SET @i=1;
	WHILE @i < @num_geolevels
	BEGIN
		SET @sql_stmt='DROP TABLE ##x' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + 
				 '_' + CAST(@@spid AS VARCHAR);
		PRINT 'SQL> ' + @sql_stmt;
		EXECUTE @rowcount = sp_executesql @sql_stmt;
		SET @i+=1;	
	END;
	
--
-- Insert statement and columns
--
	SET @sql_stmt='INSERT INTO ' + LOWER(@hierarchytable) + ' (' + @columns + ')' + @crlf;	
	
--	
-- Final SELECT
--
/*
SELECT level1, level2, level3, level4,
  FROM y
 WHERE max12 = test12
   AND max23 = test23
   AND max34 = test34
 ORDER BY 1, 2, 3, 4;  
 */
	SET @sql_stmt+='SELECT /* Select y intersection, eliminating duplicates using selecting the lower geolevel resolution' + @crlf + 
         @tab + ' with the largest intersection by area for each (higher resolution) geolevel */' + @crlf + '       ' + @columns + @crlf +
		'  FROM ##y_' + CAST(@@spid AS VARCHAR) + @crlf;
--
-- WHERE clause
--
	SET @i=1;
	WHILE @i < @num_geolevels
	BEGIN
		IF @i = 1
			SET @sql_stmt+=' WHERE max' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + 
				' = test' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + @crlf;
		ELSE
			SET @sql_stmt+='   AND max' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + 
				' = test' + CAST(@i AS VARCHAR) + CAST(@i+1 AS VARCHAR) + @crlf;
		SET @i+=1;	
	END;
--
-- ORDER BY clause
--	
	SET @sql_stmt+=' ORDER BY 1';
	SET @i=2;
	WHILE @i <= @num_geolevels /* FOR i IN 2 .. num_geolevels LOOP */
	BEGIN	
		SET @sql_stmt+=', ' + CAST(@i AS VARCHAR);
		SET @i+=1;	
	END;	
--
	DEALLOCATE c1_hier;
--
	PRINT 'SQL> ' + @sql_stmt;
--
-- Execute SQL statement
--
	EXECUTE @i=sp_executesql @sql_stmt
	SET @rowcount = @@ROWCOUNT;
--
-- Drop Y temp table
--	
	SET @sql_stmt='DROP TABLE ##y_' + CAST(@@spid AS VARCHAR);
	PRINT 'SQL> ' + @sql_stmt;
	EXECUTE sp_executesql @sql_stmt;
	SELECT name FROM tempdb.sys.objects;
--
-- Check rows were inserted
--	
	IF @rowcount = 0 
		RAISERROR('No rows found in %s geography hierarchy table: %s; sp_executesql rval: %d', 16, 1, 
			@l_geography 			/* Geography */,
			@hierarchytable			/* Hierarchy table */,
			@i						/* Return value from sp_executesql() */);
--
-- Re-index
--
	OPEN c2_hier;
	FETCH NEXT FROM c2_hier INTO @indexname, @tablename;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @sql_stmt='ALTER INDEX ' + @indexname + ' ON ' + @tablename + ' REORGANIZE';
		PRINT 'SQL> ' + @sql_stmt;
		EXECUTE sp_executesql @sql_stmt;	
		FETCH NEXT FROM c2_hier INTO @indexname, @tablename;
	END;
	CLOSE c2_hier;
	DEALLOCATE c2_hier;
--
-- Analyze
--
	SET @sql_stmt='UPDATE STATISTICS ' + LOWER(@hierarchytable);
	PRINT 'SQL> ' + @sql_stmt;
	EXECUTE sp_executesql @sql_stmt;			
END