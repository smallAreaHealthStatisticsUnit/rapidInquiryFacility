-- geolevel checks 

--OK :Check LOOKUP_TABLE, COVARIATE_TABLE exist.

--Check YEAR, <GEOLEVEL_NAME> exist in COVARIATE_TABLE -- foreign key already in place ?? Not too sure about the year ? is it the covariates table or [dbo].[SAHSULAND_COVARIATES_LEVEL3]

--Check LOOKUP_TABLE.<T_RIF40_GEOLEVELS.GEOLEVEL_NAME>  ? Not clear

--OK :LOOKUP_TABLE.LOOKUP_DESC_COLUMN,
--LOOKUP_TABLE.<CENTROIDXCOORDINATE_COLUMN>, LOOKUP_TABLE.<CENTROIDYCOORDINATE_COLUMN> columns exist. It sometimes is null e.g. [dbo].[SAHSULAND_LEVEL1] is a lookup table but has no such columns

--Check <RIF40_GEOGRAPHIES.HIERARCHYTABLE>.<GEOLEVEL_NAME> column exists
-- cant find geoname on geoagraphy table .but geoname matches with default study area field is this what is required ?

--Check <postal_population_table>.<GEOLEVEL_NAME> column exists if POSTAL_POPULATION_TABLE set if RIF40_GEOGAPHIES


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
instead of insert, update
as
begin

-- check covariate table
DECLARE @covariatetablelist nvarchar(MAX) =
    (
    SELECT 
		[COVARIATE_TABLE] + ', '
        FROM inserted
        WHERE OBJECT_ID([COVARIATE_TABLE], 'U') IS NULL
        FOR XML PATH('')
    );
BEGIN TRY 
	IF @covariatetablelist IS NOT NULL
	BEGIN
		RAISERROR(50040, 16, 1, @covariatetablelist) with log;
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 

-- check geolevel name 
DECLARE @geolevelname NVARCHAR(MAX)= 
( 
 SELECT [GEOLEVEL_NAME] + ' , '
 FROM   inserted  
 WHERE   GEOLEVEL_NAME NOT IN (SELECT GEOLEVEL_NAME FROM [rif40].[rif40_covariates])
 FOR XML PATH('') 
); 
BEGIN TRY 
IF @geolevelname IS NOT NULL 
	BEGIN 
		RAISERROR(50041,16,1,@geolevelname); 
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
-- check covariate table YEAR column  	
DECLARE @covariatetablelist2 nvarchar(MAX) =
    (
	select [COVARIATE_TABLE]
	from inserted ic 
   	where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.COVARIATE_TABLE and c.name='YEAR' )
    );
BEGIN TRY  
	IF @covariatetablelist2 IS NOT NULL
	BEGIN
		RAISERROR(50042, 16, 1, @covariatetablelist2) with log;
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
---CHECK GEOLEVEL name exists in covariate table 
DECLARE @covariatetablelist3 nvarchar(MAX) =
    (
	select [COVARIATE_TABLE]
	from inserted ic 
   	where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.COVARIATE_TABLE and c.name=ic.GEOLEVEL_NAME )
    );
BEGIN TRY  
	IF @covariatetablelist3 IS NOT NULL
	BEGIN
		RAISERROR(50043, 16, 1, @covariatetablelist3) with log;
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
-------------------------
--check lookup table 
-------------------------
DECLARE @lookuptablelist nvarchar(MAX) =
    (
    SELECT 
		LOOKUP_TABLE + ', '
        FROM inserted
        WHERE OBJECT_ID(LOOKUP_TABLE, 'U') IS NULL
        FOR XML PATH('')
    );
BEGIN TRY 
	IF @lookuptablelist IS NOT NULL
	BEGIN
		RAISERROR(50044, 16, 1, @lookuptablelist) with log;
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
-----------------------------------
-- check lookup table x-cord 
-----------------------------------
DECLARE @lookuptablelist2  nvarchar(MAX) =
    (
	select LOOKUP_TABLE
	from inserted ic 
   	where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.LOOKUP_TABLE and c.name=ic.CENTROIDXCOORDINATE_COLUMN )
    );
BEGIN TRY  
	IF @lookuptablelist2 IS NOT NULL
	BEGIN
		RAISERROR(50045, 16, 1, @lookuptablelist2) with log;
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 
-----------------------------------
-- check lookup table y-cord 
-----------------------------------
DECLARE @lookuptablelist3  nvarchar(MAX) =
    (
	select LOOKUP_TABLE
	from inserted ic 
   	where not EXISTS (SELECT 1  
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =ic.LOOKUP_TABLE and c.name=ic.CENTROIDYCOORDINATE_COLUMN )
    );
BEGIN TRY  
	IF @lookuptablelist3 IS NOT NULL
	BEGIN
		RAISERROR(50046, 16, 1, @lookuptablelist3) with log;
	END;
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 	
-- Check &lt;postal_population_table&gt;.&lt;GEOLEVEL_NAME&gt; column exists if POSTAL_POPULATION_TABLE set if RIF40_GEOGAPHIES

DECLARE @TABLE_NAMES table 
(
ID INT IDENTITY(1,1), Name VARCHAR(255)
)

INSERT INTO @TABLE_NAMES (Name)
select distinct B.POSTAL_POPULATION_TABLE 
		from inserted  a
		left outer join [rif40].[rif40_geographies] b
		on a.GEOGRAPHY=b.GEOGRAPHY
		where	b.POSTAL_POPULATION_TABLE is not null and --TO CLARIFY THIS LOGIC WITH PH 
				(a.CENTROIDXCOORDINATE_COLUMN is not null and a.CENTROIDYCOORDINATE_COLUMN is not null) OR
				a.CENTROIDSFILE is not null 

DECLARE @TABLE_NAMES2 nvarchar(MAX) =
(
SELECT name + ' '
FROM @TABLE_NAMES
where OBJECT_ID(Name) is null 
FOR XML PATH('') 
)
BEGIN TRY 
	if @TABLE_NAMES2 is not null 
	begin 
	raiserror ('Table/s do not exist: %s' ,16,1, @TABLE_NAMES2)
	end 
END TRY 
BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc]
END CATCH 

END  