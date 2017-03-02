
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_geography')
BEGIN
	DROP TRIGGER [rif40].[tr_geography]
END
GO


---------------------------------------
--create trigger code 
---------------------------------------

-- Check postal_population_table if set and expected columns
-- Check HIERARCHYTABLE exists.
-- Error msg 50020 - 50027

CREATE trigger [tr_geography]
on [rif40].[rif40_geographies]
for insert, update
as
begin

-- Check HIERARCHYTABLE exists.
DECLARE @missing_hierarchytable nvarchar(MAX) =
    (
    SELECT 
		HIERARCHYTABLE 
        FROM inserted a
		WHERE hierarchytable is null or hierarchytable = ''
		or NOT EXISTS (
			SELECT 1
			FROM [INFORMATION_SCHEMA].[TABLES] b
			WHERE b.table_name=a.hierarchytable)
        FOR XML PATH('')
    );

IF @missing_hierarchytable IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(50020, @missing_hierarchytable);
		THROW 50020, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50020, @err_msg1, 1;
	END CATCH;	

--If postal_population_table is set: check table exists and has expected columns
DECLARE @postal_pop_missing nvarchar(MAX) =
    (
    SELECT 
		postal_population_table 
        FROM inserted a
        WHERE postal_population_table IS NOT NULL
		AND NOT EXISTS (
			SELECT 1
			FROM [INFORMATION_SCHEMA].[TABLES] b
			WHERE b.table_name=a.postal_population_table)
        FOR XML PATH('')
    );
IF @postal_pop_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(50021, @postal_pop_missing);
		THROW 50021, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50021, @err_msg2, 1;
	END CATCH;	

--Check postal_point_column + postal_population_table
DECLARE @postal_point_col_missing nvarchar(MAX) =
(
	SELECT postal_population_table postal_point_column 
		FROM inserted  ic 
		where postal_population_table IS NOT NULL
		AND (postal_point_column is null or postal_point_column=''
		or not EXISTS (SELECT 1  
							FROM [INFORMATION_SCHEMA].[columns] c
							WHERE c.table_name=ic.postal_population_table
							AND c.column_name=ic.postal_point_column))
   FOR XML PATH('')
);

IF @postal_point_col_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(50022, @postal_point_col_missing);
		THROW 50022, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50022, @err_msg3, 1;
	END CATCH;	

--check column names MALES , FEMALES , TOTAL, XCOORDINATE, YCOORDINATE 
DECLARE @MALES_missing nvarchar(MAX) =
(
	SELECT postal_population_table 
		FROM inserted  ic 
		where postal_population_table IS NOT NULL
		AND not EXISTS (SELECT 1  
							FROM [INFORMATION_SCHEMA].[columns] c
							WHERE c.table_name=ic.postal_population_table
							AND c.column_name='MALES')
   FOR XML PATH('')
);
IF @MALES_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg4 VARCHAR(MAX) = formatmessage(50023, @MALES_missing);
		THROW 50023, @err_msg4, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50023, @err_msg4, 1;
	END CATCH;	

--CHECK females column
DECLARE @FEMALES_missing nvarchar(MAX) =
(
	SELECT postal_population_table 
		FROM inserted  ic 
		where postal_population_table IS NOT NULL
		AND not EXISTS (SELECT 1  
							FROM [INFORMATION_SCHEMA].[columns] c
							WHERE c.table_name=ic.postal_population_table
							AND c.column_name='FEMALES')
   FOR XML PATH('')

);
IF @FEMALES_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg5 VARCHAR(MAX) = formatmessage(50024, @FEMALES_missing);
		THROW 50024, @err_msg5, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50024, @err_msg5, 1;
	END CATCH;	

--CHECK TOTAL column
DECLARE @TOTAL_missing nvarchar(MAX) =
(
	SELECT postal_population_table 
		FROM inserted  ic 
		where postal_population_table IS NOT NULL
		AND not EXISTS (SELECT 1  
							FROM [INFORMATION_SCHEMA].[columns] c
							WHERE c.table_name=ic.postal_population_table
							AND c.column_name='TOTAL')
   FOR XML PATH('')
);
IF @TOTAL_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg6 VARCHAR(MAX) = formatmessage(50025, @TOTAL_missing);
		THROW 50025, @err_msg6, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50025, @err_msg6, 1;
	END CATCH;	

--Check XCOORDINATE column
DECLARE @XCOORDINATE_missing nvarchar(MAX) =
(
	SELECT postal_population_table 
		FROM inserted  ic 
		where postal_population_table IS NOT NULL
		AND not EXISTS (SELECT 1  
							FROM [INFORMATION_SCHEMA].[columns] c
							WHERE c.table_name=ic.postal_population_table
							AND c.column_name='XCOORDINATE')
   FOR XML PATH('')
);
IF @XCOORDINATE_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg7 VARCHAR(MAX) = formatmessage(50026, @XCOORDINATE_missing);
		THROW 50026, @err_msg7, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50026, @err_msg7, 1;
	END CATCH;	

--Check YCOORDINATE column
DECLARE @YCOORDINATE_missing nvarchar(MAX) =
(
	SELECT postal_population_table 
		FROM inserted  ic 
		where postal_population_table IS NOT NULL
		AND not EXISTS (SELECT 1  
							FROM [INFORMATION_SCHEMA].[columns] c
							WHERE c.table_name=ic.postal_population_table
							AND c.column_name='YCOORDINATE' )
   FOR XML PATH('')
);
IF @YCOORDINATE_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg8 VARCHAR(MAX) = formatmessage(50027, @YCOORDINATE_missing);
		THROW 50027, @err_msg8, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50027, @err_msg8, 1;
	END CATCH;	

--
--  Also check defaultcomparea, defaultstudyarea as FK is now removed
--

DECLARE @missing_defaultcomparea varchar(MAX) = 
(
	select geography, defaultcomparea
	from inserted a
	where defaultcomparea IS NOT NULL AND defaultcomparea <> ''
	and exists (select b.geography, count(geolevel_name) t
		from [rif40].[t_rif40_geolevels] b
		where a.geography=b.geography
		group by b.geography
		having count(geolevel_name) > 0)
	and not exists (select 1
		from [rif40].[t_rif40_geolevels] c
		where a.defaultcomparea=c.geolevel_name)
	FOR XML PATH('')
);
IF @missing_defaultcomparea IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg9 VARCHAR(MAX) = formatmessage(50028, @missing_defaultcomparea);
		THROW 50028, @err_msg9, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50028, @err_msg9, 1;
	END CATCH;	

DECLARE @missing_defaultstudyarea varchar(MAX) = 
(
	select geography, defaultstudyarea
	from inserted a
	where defaultstudyarea IS NOT NULL AND defaultstudyarea <> ''
	and not exists (select 1
		from [rif40].[t_rif40_geolevels] b
		where a.defaultstudyarea=b.geolevel_name)
	FOR XML PATH('')
);
IF @missing_defaultstudyarea IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg10 VARCHAR(MAX) = formatmessage(50029, @missing_defaultstudyarea);
		THROW 50029, @err_msg10, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_geographies]';
		THROW 50029, @err_msg10, 1;
	END CATCH;	
	
END;
GO
