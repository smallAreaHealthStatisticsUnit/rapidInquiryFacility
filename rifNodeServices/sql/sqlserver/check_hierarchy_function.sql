IF OBJECT_ID(N'check_hierarchy_%1', N'P') IS NOT NULL  
    DROP PROCEDURE check_hierarchy_%1;  
GO
SELECT name, type, type_desc FROM sys.objects WHERE name = 'check_hierarchy_%1';
GO

CREATE PROCEDURE check_hierarchy_%1(
	@l_geography 		VARCHAR(30), 
	@l_hierarchytable 	VARCHAR(200), 
	@l_type 			VARCHAR(30),
	@error_count 		INTEGER OUTPUT)
AS
/*
 * SQL statement name: 	check_hierarchy_function.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: function name; e.g. check_hierarchy_cb_2014_us_500k
 *
 * Description:			Create hierarchy check function
 * Note:				%% becomes % after substitution
 */
 
/*
Function: 		check_hierarchy_%1()
Parameters:		Geography, hierarchy table, type: 'missing', 'spurious additional' or 'multiple hierarchy'
Returns:		Nothing
Description:	Diff geography hierarchy table using dynamic method 4
				Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on

WITH /- multiple hierarchy -/ a2 AS (
        SELECT COUNT(*) AS cb_2014_us_state_500k_total
          FROM (
                SELECT cb_2014_us_state_500k, COUNT(DISTINCT(cb_2014_us_nation_5m)) AS total
                  FROM hierarchy_cb_2014_us_500k
                 GROUP BY cb_2014_us_state_500k
                HAVING COUNT(DISTINCT(cb_2014_us_nation_5m)) > 1) as2
), a3 AS (
        SELECT COUNT(*) AS cb_2014_us_county_500k_total
          FROM (
                SELECT cb_2014_us_county_500k, COUNT(DISTINCT(cb_2014_us_state_500k)) AS total
                  FROM hierarchy_cb_2014_us_500k
                 GROUP BY cb_2014_us_county_500k
                HAVING COUNT(DISTINCT(cb_2014_us_state_500k)) > 1) as3
)
SELECT CAST('cb_2014_us_state_500k' AS VARCHAR) AS col,
			a2.cb_2014_us_state_500k_total AS val
  FROM a2
UNION
SELECT CAST('cb_2014_us_county_500k' AS VARCHAR) AS col,
			a3.cb_2014_us_county_500k_total AS val
  FROM a3;
  
*/
BEGIN 
	DECLARE c2 CURSOR FOR
		SELECT geolevel_id, geolevel_name, lookup_table		
		  FROM geolevels_%1
		 WHERE geography = @l_geography
		 ORDER BY geolevel_id;
--
	DECLARE @sql_stmt 				AS NVARCHAR(max)='XXX';
	DECLARE @i 						AS INTEGER=0;	
--
	DECLARE @crlf					AS VARCHAR(2)=CHAR(10)+CHAR(13);
	DECLARE @tab					AS VARCHAR(1)=CHAR(9);
--
	DECLARE @geolevel_id 			AS INTEGER;
	DECLARE @geolevel_name		 	AS VARCHAR(200);
	DECLARE @lookup_table 			AS VARCHAR(200);
	DECLARE @previous_geolevel_name AS VARCHAR(200);
--
	SET @error_count=0;
	SET @sql_stmt='WITH /* ' + @l_type + ' */ ';
--
	OPEN c2;
	FETCH NEXT FROM c2 INTO @geolevel_id, @geolevel_name, @lookup_table;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i+=1;
--
		IF @l_type = 'multiple hierarchy' 
			BEGIN
				IF @i = 1 
					noop:
				ELSE IF @i > 2
					SET @sql_stmt+=', ' + 'a' + CAST(@geolevel_id AS VARCHAR) + ' AS (' + @crlf +
							@tab + 'SELECT COUNT(*) AS ' + LOWER(@geolevel_name) + '_total' + @crlf +
							@tab + '  FROM (' + @crlf;
				ELSE
					SET @sql_stmt+='a' + CAST(@geolevel_id AS VARCHAR) + ' AS (' + @crlf +
							@tab + 'SELECT COUNT(*) AS ' + LOWER(@geolevel_name) + '_total' + @crlf +
							@tab + '  FROM (' + @crlf;
			END;
		ELSE
			BEGIN
				IF @i != 1
					SET @sql_stmt+=', ' + 'a' + CAST(@geolevel_id AS VARCHAR) + ' AS (' + @crlf;
				ELSE
					SET @sql_stmt+='a' + CAST(@geolevel_id AS VARCHAR) + ' AS (' + @crlf;
				SET @sql_stmt+=@tab + 'SELECT COUNT(*) AS ' + LOWER(@geolevel_name) + '_total' + @crlf +
					@tab + '  FROM (' + @crlf;
			END;
--
		IF @l_type = 'missing'
			SET @sql_stmt+=@tab + @tab + 'SELECT ' + LOWER(@geolevel_name) + 
				' FROM ' + LOWER(@l_hierarchytable) + @crlf +
				@tab + @tab + 'EXCEPT' + @crlf +
				@tab + @tab + 'SELECT ' + LOWER(@geolevel_name) + ' FROM ' + LOWER(@lookup_table) + 
				') as' + CAST(@geolevel_id AS VARCHAR) + ')' + @crlf;
		ELSE IF @l_type = 'spurious additional' 
			SET @sql_stmt+=@tab + @tab + 'SELECT ' + LOWER(@geolevel_name) + 
				' FROM ' + LOWER(@lookup_table) + @crlf +
				@tab + @tab + 'EXCEPT' + @crlf +
				@tab + @tab + 'SELECT ' + LOWER(@geolevel_name) + ' FROM ' + LOWER(@l_hierarchytable) + 
				') as' + CAST(@geolevel_id AS VARCHAR) + ')' + @crlf;
		ELSE IF @l_type = 'multiple hierarchy' 
			BEGIN
				IF @previous_geolevel_name IS NOT NULL 
					SET @sql_stmt+=@tab + @tab + 'SELECT ' + LOWER(@geolevel_name) + 
						', COUNT(DISTINCT(' + @previous_geolevel_name + ')) AS total' + @crlf +
						@tab + @tab + '  FROM ' + LOWER(@l_hierarchytable) + @crlf +
						@tab + @tab + ' GROUP BY ' + LOWER(@geolevel_name) + @crlf +
						@tab + @tab + 'HAVING COUNT(DISTINCT(' + @previous_geolevel_name + ')) > 1' + 
						') as' + CAST(@geolevel_id AS VARCHAR) + ')' + @crlf;
			END;
		ELSE
			RAISERROR('Invalid check type: %s, valid types are: ''missing'', ''spurious additional'', or ''multiple hierarchy''', 
				1, 16, @l_type 	/* Check type */);
		SET @previous_geolevel_name=LOWER(@geolevel_name);
--
		FETCH NEXT FROM c2 INTO @geolevel_id, @geolevel_name, @lookup_table;
	END;
	CLOSE c2;
--
	SET @i=0;
	OPEN c2;
	FETCH NEXT FROM c2 INTO @geolevel_id, @geolevel_name, @lookup_table;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i+=1;
		IF @l_type = 'multiple hierarchy' 
			BEGIN
			IF @i = 1
				noop2:
			ELSE IF @i > 2
				SET @sql_stmt+=@crlf + 'UNION' + @crlf + 'SELECT CAST(''' + LOWER(@geolevel_name) + 
					''' AS VARCHAR) AS col, ' + @crlf + 
					@tab + 'a' + CAST(@geolevel_id AS VARCHAR) + '.' + 
					LOWER(@geolevel_name) + '_total AS val' + @crlf +
					'  FROM a' + CAST(@geolevel_id AS VARCHAR);
			ELSE
				SET @sql_stmt+='SELECT CAST(''' + LOWER(@geolevel_name) + ''' AS VARCHAR) AS col,' + @crlf + 
					@tab + 'a' + CAST(@geolevel_id AS VARCHAR) + '.' + LOWER(@geolevel_name) + 
					'_total AS val' + @crlf +
					'  FROM a' + CAST(@geolevel_id AS VARCHAR);
			END;
		ELSE
			BEGIN
			IF @i != 1 
				SET @sql_stmt+=@crlf + 'UNION' + @crlf + 'SELECT CAST(''' + LOWER(@geolevel_name) + 
					''' AS VARCHAR) AS col, ' + @crlf + 
					@tab + 'a' + CAST(@geolevel_id AS VARCHAR) + '.' + 
					LOWER(@geolevel_name) + '_total AS val' + @crlf +
					'  FROM a' + CAST(@geolevel_id AS VARCHAR);
			ELSE
				SET @sql_stmt+='SELECT CAST(''' + LOWER(@geolevel_name) + ''' AS VARCHAR) AS col,' + @crlf + 
					@tab + 'a' + CAST(@geolevel_id AS VARCHAR) + '.' + LOWER(@geolevel_name) + 
					'_total AS val' + @crlf +
					'  FROM a' + CAST(@geolevel_id AS VARCHAR);
			END;	
--
		FETCH NEXT FROM c2 INTO @geolevel_id, @geolevel_name, @lookup_table;
	END;
	CLOSE c2;
--
	DEALLOCATE c2;
--	
	PRINT 'SQL> ' + @sql_stmt;
    DECLARE @results AS TABLE (
		col VARCHAR(200), val NUMERIC) 
	INSERT into @results EXECUTE sp_executesql @sql_stmt;
--
-- Process results table
--	
	DECLARE @col AS VARCHAR(200);
	DECLARE @val AS NUMERIC;
	DECLARE c1 CURSOR FOR
		SELECT col, val
		  FROM @results;
	OPEN c1;
	FETCH NEXT FROM c1 INTO @col, @val;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		PRINT 'Geography: ' + @l_geography + ' geolevel: ' + @col + ' has ' + CAST(@val AS VARCHAR) + 
			' ' + @l_type + ' codes';
		IF @val != 0
			SET @error_count+=1;	
--
		FETCH NEXT FROM c1 INTO @col, @val;
	END;
	CLOSE c1;  
--
	DEALLOCATE c1;
END;