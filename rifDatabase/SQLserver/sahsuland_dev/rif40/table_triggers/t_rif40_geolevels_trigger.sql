/*
-- geolevel checks 

Check LOOKUP_TABLE, COVARIATE_TABLE exist.
Check YEAR, &lt;GEOLEVEL_NAME&gt; exist in COVARIATE_TABLE
Check LOOKUP_TABLE.&lt;T_RIF40_GEOLEVELS.GEOLEVEL_NAME&gt;, LOOKUP_TABLE.LOOKUP_DESC_COLUMN,
LOOKUP_TABLE.<CENTROIDXCOORDINATE_COLUMN>, LOOKUP_TABLE.<CENTROIDYCOORDINATE_COLUMN> columns exist
Check &lt;RIF40_GEOGRAPHIES.HIERARCHYTABLE&gt;.&lt;GEOLEVEL_NAME&gt; column exists
Check &lt;postal_population_table&gt;.&lt;GEOLEVEL_NAME&gt; column exists if POSTAL_POPULATION_TABLE set if RIF40_GEOGAPHIES

*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_geolevel_check')
BEGIN
	DROP TRIGGER [rif40].[tr_geolevel_check]
END
GO


CREATE trigger [tr_geolevel_check]
on [rif40].[t_rif40_geolevels]
for insert, update, delete
as
begin
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
Declare  @XTYPE varchar(5);


	IF EXISTS (SELECT * FROM DELETED)
	BEGIN
		SET @XTYPE = 'D'
	END;
	
	IF EXISTS (SELECT * FROM INSERTED)
	BEGIN
		IF (@XTYPE = 'D')
			SET @XTYPE = 'U'
		ELSE 
			SET @XTYPE = 'I'
	END

--delete not allowed
IF @XTYPE = 'D'
BEGIN TRY
	rollback;
	DECLARE @err_msg0 VARCHAR(MAX) = formatmessage(51146);
	THROW 51146, @err_msg0, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
	THROW 51146, @err_msg0, 1;
END CATCH;	

--insert OK if during initial build
DECLARE @has_studies_check VARCHAR(MAX) = 
(
	SELECT count(study_id) as total
	FROM [rif40].[t_rif40_results]
);
IF @has_studies_check=0
BEGIN
	EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_geolevels]', 't_rif40_geolevels insert allowed during build before first result is added to system';
	RETURN;
END;


-- if covariates table field is not null, check that covariate table exists
DECLARE @covariatetable_missing nvarchar(MAX) =
    (
    SELECT 
		[COVARIATE_TABLE]
        FROM inserted a
        WHERE covariate_table is not null and covariate_table <> ''
		AND not exists (
			select 1
			from information_schema.tables b
			where b.table_name=a.covariate_table)
        FOR XML PATH('')
    );
IF @covariatetable_missing IS NOT NULL 
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51053, @covariatetable_missing);
		THROW 51053, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51053, @err_msg1, 1;
	END CATCH;	


-- check geolevel name column exists in covariate_table
DECLARE @geolevelname_missing NVARCHAR(MAX)= 
( 
 SELECT [GEOLEVEL_NAME], covariate_table
 FROM   inserted  b
 WHERE   covariate_table is not null and  covariate_table <> ''
	AND not exists (
		select 1
		from INFORMATION_SCHEMA.COLUMNS a
		where a.table_name=b.covariate_table
		and a.column_name=b.GEOLEVEL_NAME
		)
	FOR XML PATH('')
);

IF @geolevelname_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51054, @geolevelname_missing);
		THROW 51054, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51054, @err_msg2, 1;
	END CATCH;	
	

-- check covariate table YEAR column  	
DECLARE @yearcol_missing nvarchar(MAX) =
    (
	select [COVARIATE_TABLE]
	from inserted b
   	where  covariate_table is not null and  covariate_table <> ''
	AND not EXISTS (SELECT 1  
		from INFORMATION_SCHEMA.COLUMNS a
		where a.table_name=b.covariate_table
		and a.column_name='YEAR')
	 FOR XML PATH('') 	
    );
IF @yearcol_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51055, @yearcol_missing);
		THROW 51055, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51055, @err_msg3, 1;
	END CATCH;	

-------------------------
--check lookup table, required table
-------------------------
DECLARE @lookuptable_missing nvarchar(MAX) =
    (
    SELECT 
		LOOKUP_TABLE, geolevel_name
        FROM inserted a
        WHERE lookup_table is  null or lookup_table=''
		or NOT EXISTS (
			SELECT 1
			FROM information_schema.tables b
			where b.table_name=a.lookup_table)
        FOR XML PATH('')
    );
IF @lookuptable_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg4 VARCHAR(MAX) = formatmessage(51056, @lookuptable_missing);
		THROW 51056, @err_msg4, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51056, @err_msg4, 1;
	END CATCH;	

--check geolevel_name column exsists in lookup table
DECLARE @geolevelname_lookup_missing NVARCHAR(MAX)= 
( 
 SELECT [GEOLEVEL_NAME], lookup_table
 FROM   inserted  b
 WHERE   lookup_table is not null and lookup_table <> ''
	and	not exists (
		select 1
		from INFORMATION_SCHEMA.COLUMNS a
		where a.table_name=b.lookup_table
		and a.column_name=b.GEOLEVEL_NAME
		)
 FOR XML PATH('') 
); 
IF @geolevelname_lookup_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg5 VARCHAR(MAX) = formatmessage(51057, @geolevelname_lookup_missing);
		THROW 51057, @err_msg5, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51057, @err_msg5, 1;
	END CATCH;	

--check LOOKUP_TABLE.LOOKUP_DESC_COLUMN exists 
DECLARE @lookup_desc_missing NVARCHAR(MAX)= 
( 
 SELECT [LOOKUP_DESC_COLUMN], lookup_table, geolevel_name
 FROM   inserted  b
 WHERE   lookup_table is not null and lookup_table <> ''
	and	not exists  (
		select 1
		from INFORMATION_SCHEMA.COLUMNS a
		where a.table_name=b.lookup_table
		and a.column_name=b.LOOKUP_DESC_COLUMN
		)
 FOR XML PATH('') 
); 
IF @lookup_desc_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg6 VARCHAR(MAX) = formatmessage(51058, @lookup_desc_missing);
		THROW 51058, @err_msg6, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51058, @err_msg6, 1;
	END CATCH;	

-----------------------------------
-- check lookup table x-cord 
-----------------------------------
DECLARE @lookup_xcoord_missing  nvarchar(MAX) =
    (
	select LOOKUP_TABLE, CENTROIDXCOORDINATE_COLUMN, geolevel_name
	from inserted b
   	where lookup_table is not null and lookup_table <> ''
	AND CENTROIDXCOORDINATE_COLUMN is not null and CENTROIDXCOORDINATE_COLUMN <> ''
	and	not exists (SELECT 1  
			from INFORMATION_SCHEMA.COLUMNS a
			where a.table_name=b.lookup_table
			and a.column_name=b.CENTROIDXCOORDINATE_COLUMN)
	FOR XML PATH('') 
   );
IF @lookup_xcoord_missing  IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg7 VARCHAR(MAX) = formatmessage(51059, @lookup_xcoord_missing);
		THROW 51059, @err_msg7, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51059, @err_msg7, 1;
	END CATCH;	

-----------------------------------
-- check lookup table y-cord 
-----------------------------------
DECLARE @lookup_ycoord_missing  nvarchar(MAX) =
    (
	select LOOKUP_TABLE, CENTROIDYCOORDINATE_COLUMN, geolevel_name
	from inserted b
   	where lookup_table is not null and lookup_table <> ''
	AND CENTROIDYCOORDINATE_COLUMN is not null and CENTROIDYCOORDINATE_COLUMN <> ''
	and	not EXISTS (SELECT 1  
			from INFORMATION_SCHEMA.COLUMNS a
			where a.table_name=b.lookup_table
			and a.column_name=b.CENTROIDYCOORDINATE_COLUMN)
	FOR XML PATH('') 
   );		
IF @lookup_ycoord_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg8 VARCHAR(MAX) = formatmessage(51060, @lookup_ycoord_missing);
		THROW 51060, @err_msg8, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51060, @err_msg8, 1;
	END CATCH;	
  
 --Check <RIF40_GEOGRAPHIES.HIERARCHYTABLE>.<GEOLEVEL_NAME> column exists
DECLARE @geography_missing varchar(MAX) = 
(
	SELECT geography, geolevel_name
	FROM inserted a
	WHERE geography is null or geography = ''
		or NOT EXISTS (SELECT 1
		FROM [rif40].[rif40_geographies] b
		WHERE a.geography=b.geography
		AND b.hierarchytable IS NOT NULL and b.hierarchytable <> '')
	FOR XML PATH('') 
   );			
IF @geography_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg9 VARCHAR(MAX) = formatmessage(51061, @geography_missing);
		THROW 51061, @err_msg9, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51061, @err_msg9, 1;
	END CATCH;	
  
 DECLARE @geolevel_geography_missing varchar(MAX) = 
 (
	SELECT a.geolevel_name, c.hierarchytable
	FROM inserted a, [rif40].[rif40_geographies] c
	WHERE a.geography=c.geography
	AND not EXISTS (SELECT 1  
			from INFORMATION_SCHEMA.COLUMNS b
			where b.table_name=c.hierarchytable
			and b.column_name=a.geolevel_name)
	FOR XML PATH('') 
 );
 IF @geolevel_geography_missing IS NOT NULL
 	BEGIN TRY
		rollback;
		DECLARE @err_msg10 VARCHAR(MAX) = formatmessage(51062, @geolevel_geography_missing);
		THROW 51062, @err_msg10, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51062, @err_msg10, 1;
	END CATCH;	
 
  
-- If rif40_geographies has set postal_popluation_table, check that postal_population_table.GEOLEVEL_NAME column exists
DECLARE @postalpop_missing varchar(MAX) = 
(
	SELECT a.geography, c.postal_population_table
	FROM inserted a, [rif40].[rif40_geographies] c
	WHERE a.geography=c.geography
	AND c.postal_population_table IS NOT NULL and c.postal_population_table <> ''
	AND ((a.centroidxcoordinate_column IS NOT NULL AND a.centroidxcoordinate_column <> ''
	AND a.centroidycoordinate_column IS NOT NULL AND a.centroidycoordinate_column <> '') OR
		 (a.centroidsfile IS NOT NULL AND a.centroidsfile <> ''))
	AND not EXISTS (SELECT 1  
			from INFORMATION_SCHEMA.TABLES b
			where b.table_name=c.postal_population_table)
	FOR XML PATH('') 
   );
IF @postalpop_missing IS NOT NULL
 	BEGIN TRY
		rollback;
		DECLARE @err_msg11 VARCHAR(MAX) = formatmessage(51063, @postalpop_missing);
		THROW 51063, @err_msg11, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51063, @err_msg11, 1;
	END CATCH;	

DECLARE @geolevel_postal_missing varchar(MAX) = 
 (
	SELECT a.geolevel_name, c.postal_population_table
	FROM inserted a, [rif40].[rif40_geographies] c
	WHERE a.geography=c.geography
	AND c.postal_population_table IS NOT NULL and c.postal_population_table <> ''
	AND ((a.centroidxcoordinate_column IS NOT NULL AND a.centroidxcoordinate_column <> ''
	AND a.centroidycoordinate_column IS NOT NULL AND a.centroidycoordinate_column <> '') OR
		 (a.centroidsfile IS NOT NULL AND a.centroidsfile <> ''))
	AND not EXISTS (SELECT 1  
			from INFORMATION_SCHEMA.COLUMNS b
			where b.table_name=c.postal_population_table
			and b.column_name=a.geolevel_name)
	FOR XML PATH('') 
 ); 
IF @geolevel_postal_missing IS NOT NULL
 	BEGIN TRY
		rollback;
		DECLARE @err_msg12 VARCHAR(MAX) = formatmessage(51064, @geolevel_postal_missing);
		THROW 51064, @err_msg12, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_geolevels]';
		THROW 51064, @err_msg12, 1;
	END CATCH;	
   
END;
