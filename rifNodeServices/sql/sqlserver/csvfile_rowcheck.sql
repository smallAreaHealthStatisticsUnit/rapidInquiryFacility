DECLARE c1 CURSOR FOR SELECT COUNT(%3) AS total FROM %1;
/*
 * SQL statement name: 	csvfile_rowcheck.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: Expected number of rows; e.g. 3233
 *						3: Column to count; e.g. gid
 *
 * Description:			Check number of rows in loaded CSV file is as expected
 * Note:				%%%% becomes %% after substitution
 */
DECLARE @c1_total AS int;
OPEN c1;
FETCH NEXT FROM c1 INTO @c1_total;
IF @c1_total = %2
	PRINT 'Table: %1 row check OK: ' + CAST(@c1_total AS VARCHAR);
ELSE
	RAISERROR('Table: %1 row check FAILED: expected: %2 got: %i', 16, 1, @c1_total);
CLOSE c1;
DEALLOCATE c1;