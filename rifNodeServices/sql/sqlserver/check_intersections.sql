DECLARE @l_geography AS VARCHAR(200)='%1';
/*
 * SQL statement name: 	check_intersections.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: geography; e.g. cb_2014_us_500k
 *
 * Description:			Check intersections
 * Note:				%% becomes % after substitution
 */
--
	DECLARE c1 CURSOR FOR
		SELECT geography, hierarchytable
		  FROM geography_%1
		 WHERE geography = @l_geography;
--
	DECLARE @geography 		AS VARCHAR(30);
	DECLARE @hierarchytable AS VARCHAR(200);
--
	DECLARE @CurrentUser	AS VARCHAR(60);
	DECLARE @function_name	AS VARCHAR(200);
	DECLARE @l_type			AS VARCHAR(30);
--
	DECLARE @e AS INTEGER=0;
	DECLARE @f AS INTEGER=0;
	DECLARE @g AS INTEGER=0;
BEGIN
--
	OPEN c1;
	FETCH c1 INTO @geography, @hierarchytable;
	CLOSE c1;
	DEALLOCATE c1;
--
	IF @geography IS NULL
		RAISERROR('geography: % not found', 16, 1, @l_geography	/* Geography */);
--
-- Call diff and multiple hierarchy tests
--
	SELECT @CurrentUser = user_name(); 
	SET @function_name=@CurrentUser + '.check_hierarchy_%1';
	SET @l_type='missing';
	EXECUTE @function_name @geography, @hierarchytable, @l_type, @e;
	SET @l_type='spurious additional';
	EXECUTE @function_name @geography, @hierarchytable, @l_type, @f;
	SET @l_type='multiple hierarchy';
	EXECUTE @function_name @geography, @hierarchytable, @l_type, @g;
--
	IF @e+@f > 0
		RAISERROR('Geography: %s codes check %d missing, %d spurious additional, %d hierarchy fails', 16, 1,  
			@geography	/* Geography */, 
			@e			/* Missing */, 
			@f			/* Spurious additional */, 
			@g			/* Multiple hierarchy */);
	ELSE
		PRINT 'Geography: ' + @geography + ' codes check OK';
END;