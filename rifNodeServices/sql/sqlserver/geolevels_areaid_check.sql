/*
 * SQL statement name: 	geolevels_areaid_check.sql
 * Type:				MS SQL Server SQL statement
 * Parameters:
 *						1: Geolevels table; e.g. geolevels_cb_2014_us_500k
 *						2: Schema; e.g. rif40. or ""
 *
 * Description:			Update areaid_count column in geolevels table using geometry table
 * Note:				%% becomes % after substitution
 */
DECLARE c1 CURSOR FOR
		SELECT geolevel_id, geolevel_name, areaid_count
		  FROM %2%1;
--
DECLARE @c1_rec_geolevel_id 	INTEGER;
DECLARE @c1_rec_geolevel_name 	VARCHAR(30);
DECLARE @c1_rec_areaid_count 	INTEGER;
--
DECLARE @errors INTEGER=0;

OPEN c1;
FETCH NEXT FROM c1 INTO @c1_rec_geolevel_id, @c1_rec_geolevel_name, @c1_rec_areaid_count;
WHILE @@FETCH_STATUS = 0
BEGIN
	IF @c1_rec_areaid_count > 0 
		PRINT 'geolevel: ' + CAST(@c1_rec_geolevel_id AS VARCHAR) + ':' + @c1_rec_geolevel_name + 
			' areaid_count: ' + CAST(@c1_rec_areaid_count AS VARCHAR)
	ELSE IF @c1_rec_areaid_count IS NULL BEGIN
			SET @errors=@errors+1;
			PRINT 'WARNING: geolevel: ' + CAST(@c1_rec_geolevel_id AS VARCHAR) + ':' + @c1_rec_geolevel_name + 
				' areaid_count IS NULL';	
		END;
	ELSE BEGIN
		PRINT 'WARNING: geolevel: ' + CAST(@c1_rec_geolevel_id AS VARCHAR) + ':' + @c1_rec_geolevel_name + 
			' errors is zero';
		SET @errors=@errors+1;
	END;
    FETCH NEXT FROM c1 INTO @c1_rec_geolevel_id, @c1_rec_geolevel_name, @c1_rec_areaid_count;
END;
CLOSE c1;
DEALLOCATE c1;
IF @errors = 0
	PRINT 'Geolevels table: %1 no zero areaid_counts';
ELSE
	RAISERROR('Geolevels table: %1 %i geolevels have zero areaid_counts', 16, 1, @errors);