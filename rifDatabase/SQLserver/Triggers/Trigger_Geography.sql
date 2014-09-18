---------------------------------------
--create trigger code 
---------------------------------------

-- Check postal_population_table if set and expected columns
-- Check HIERARCHYTABLE exists.
-- Error msg 50020 - 50027

alter trigger tr_GEOGRAPHY
on [dbo].[RIF40_GEOGRAPHIES]
instead of insert, update
as
begin
DECLARE @HIERARCHYTABLE nvarchar(MAX) =
    (
    SELECT 
		HIERARCHYTABLE + ', '
        FROM inserted
        WHERE OBJECT_ID(HIERARCHYTABLE, 'U') IS NULL
        FOR XML PATH('')
    );
BEGIN TRY
IF @HIERARCHYTABLE IS NOT NULL
	BEGIN
		RAISERROR(50020, 16, 1, @HIERARCHYTABLE) with log;
		ROLLBACK
	END;
END TRY
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 
--Check postal_population_table if set and expected columns-- 

DECLARE @postal_pop nvarchar(MAX) =
    (
    SELECT 
		postal_population_table + ', '
        FROM inserted
        WHERE OBJECT_ID(postal_population_table, 'U') IS NULL
        FOR XML PATH('')
    );
BEGIN TRY 
IF @postal_pop IS NOT NULL
	BEGIN
		RAISERROR(50021, 16, 1, @postal_pop) with log;
		ROLLBACK
	END;
END TRY
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 
--Check postal_population_column 

DECLARE @postal_point_col nvarchar(MAX) =
(
	SELECT concat (postal_population_table,'-', postal_point_column )
		 + '  '
		FROM inserted  ic 
		where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.POSTAL_POPULATION_TABLE and c.name=ic.POSTAL_POINT_COLUMN )
   FOR XML PATH('')

);
BEGIN TRY 
IF @postal_point_col IS NOT NULL
	BEGIN
		RAISERROR(50022, 16, 1, @postal_point_col) with log;
		ROLLBACK
	END;
END TRY 
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 
--check column names MALES , FEMALES , TOTAL, XCOORDINATE, YCOORDINATE 
DECLARE @MALES nvarchar(MAX) =
(
	SELECT postal_population_table 
		 + '  '
		FROM inserted  ic 
		where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.POSTAL_POPULATION_TABLE and c.name='MALES' )
   FOR XML PATH('')

);
BEGIN TRY
IF @MALES IS NOT NULL
	BEGIN
		RAISERROR(50023, 16, 1, @MALES) with log;
		ROLLBACK
	END;
END TRY 
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

--CHECK females 
DECLARE @FEMALES nvarchar(MAX) =
(
	SELECT postal_population_table 
		 + '  '
		FROM inserted  ic 
		where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.POSTAL_POPULATION_TABLE and c.name='FEMALES' )
   FOR XML PATH('')

);
BEGIN TRY 
IF @FEMALES IS NOT NULL
	BEGIN
		RAISERROR(50024, 16, 1, @FEMALES) with log;
		ROLLBACK
	END;
END TRY 
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 
--CHECK TOTAL 
DECLARE @TOTAL nvarchar(MAX) =
(
	SELECT postal_population_table 
		 + '  '
		FROM inserted  ic 
		where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.POSTAL_POPULATION_TABLE and c.name='TOTAL' )
   FOR XML PATH('')

);
BEGIN TRY 
IF @TOTAL IS NOT NULL
	BEGIN
		RAISERROR(50025, 16, 1, @TOTAL) with log;
		ROLLBACK
	END;
END TRY 
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

--XCOORDINATE

DECLARE @XCOORDINATE nvarchar(MAX) =
(
	SELECT postal_population_table 
		 + '  '
		FROM inserted  ic 
		where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.POSTAL_POPULATION_TABLE and c.name='XCOORDINATE' )
   FOR XML PATH('')

);
BEGIN TRY 
IF @XCOORDINATE IS NOT NULL
	BEGIN
		RAISERROR(50026, 16, 1, @XCOORDINATE) with log;
		ROLLBACK
	END;
END TRY 

BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

--YCOORDINATE

DECLARE @YCOORDINATE nvarchar(MAX) =
(
	SELECT postal_population_table 
		 + '  '
		FROM inserted  ic 
		where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.POSTAL_POPULATION_TABLE and c.name='YCOORDINATE' )
   FOR XML PATH('')

);
BEGIN TRY 
IF @YCOORDINATE IS NOT NULL
	BEGIN
		RAISERROR(50027, 16, 1, @YCOORDINATE) with log;
		ROLLBACK
	END;
END TRY 
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 
END

