/*
 * SQL statement name: 	in_use_check.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: Geography; e.g. SAHSULAND
 *
 * Description:			Check if geography is in use in studies. Raise error if it is. 
 *						To prevent accidental replacement
 * Note:				%% becomes % after substitution
 */
DECLARE c1 CURSOR FOR 
		SELECT COUNT(DISTINCT(a.study_id)) AS total
		  FROM t_rif40_studies a
		 WHERE a.geography  = '%1';
DECLARE @c1_total AS int;
OPEN c1;
FETCH NEXT FROM c1 INTO @c1_total;
IF @c1_total = 0
	PRINT 'Geography: %1 is not used by any studies';
ELSE
	RAISERROR('Geography: %1 is used by: %%d studies', 16, 1, @c1_total);
CLOSE c1;
DEALLOCATE c1