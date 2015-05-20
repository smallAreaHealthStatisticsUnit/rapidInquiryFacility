-- geolevel checks 

--OK :Check LOOKUP_TABLE, COVARIATE_TABLE exist.

--Check YEAR, <GEOLEVEL_NAME> exist in COVARIATE_TABLE -- foreign key already in place ?? Not too sure about the year ? is it the covariates table or [dbo].[SAHSULAND_COVARIATES_LEVEL3]

--Check LOOKUP_TABLE.<T_RIF40_GEOLEVELS.GEOLEVEL_NAME>  ? Not clear

--OK :LOOKUP_TABLE.LOOKUP_DESC_COLUMN,
--LOOKUP_TABLE.<CENTROIDXCOORDINATE_COLUMN>, LOOKUP_TABLE.<CENTROIDYCOORDINATE_COLUMN> columns exist. It sometimes is null e.g. [dbo].[SAHSULAND_LEVEL1] is a lookup table but has no such columns

--Check <RIF40_GEOGRAPHIES.HIERARCHYTABLE>.<GEOLEVEL_NAME> column exists
-- cant find geoname on geoagraphy table .but geoname matches with default study area field is this what is required ?

--Check <postal_population_table>.<GEOLEVEL_NAME> column exists if POSTAL_POPULATION_TABLE set if RIF40_GEOGAPHIES

---------------------------------------------------
----------add identity column----------------------
---------------------------------------------------

alter table [dbo].[RIF40_COVARIATES]
add id int identity(1,1)
alter table [dbo].[T_RIF40_GEOLEVELS]
add id int identity(1,1)

---------------------------------------------------
----------add TRIGGER code----------------------
---------------------------------------------------

create  alter trigger tr_geolevel_check
on [dbo].[T_RIF40_GEOLEVELS]
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
 
IF @covariatetablelist IS NOT NULL
BEGIN
    RAISERROR('These covariate tables do not exist: %s', 16, 1, @covariatetablelist) with log;
END;

-- check geolevel name 
DECLARE @geolevelname NVARCHAR(MAX)= 
( 
 SELECT [GEOLEVEL_NAME] + ' , '
 FROM   inserted  
 WHERE   GEOLEVEL_NAME NOT IN (SELECT GEOLEVEL_NAME FROM [RIF40_COVARIATES])
 FOR XML PATH('') 
); 
IF @geolevelname IS NOT NULL 
	BEGIN 
		RAISERROR('These geolevel/s are missing:  %s',16,1,@geolevelname); 
	END;

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
 
IF @covariatetablelist2 IS NOT NULL
BEGIN
    RAISERROR('These covariate tables do not have YEAR COLUMN: %s', 16, 1, @covariatetablelist2) with log;
END;
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
 
IF @covariatetablelist3 IS NOT NULL
BEGIN
    RAISERROR('These covariate tables do not have the Geolevel_name COLUMN: %s', 16, 1, @covariatetablelist3) with log;
END;
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

IF @lookuptablelist IS NOT NULL
BEGIN
    RAISERROR('These lookup tables do not exist: %s', 16, 1, @lookuptablelist) with log;
END;
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
 
IF @lookuptablelist2 IS NOT NULL
BEGIN
    RAISERROR('These covariate tables do not have the x-cord COLUMN: %s', 16, 1, @lookuptablelist2) with log;
END;
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
 
IF @lookuptablelist3 IS NOT NULL
BEGIN
    RAISERROR('These covariate tables do not have the y-cord COLUMN: %s', 16, 1, @lookuptablelist3) with log;
END;	
-- Check &lt;postal_population_table&gt;.&lt;GEOLEVEL_NAME&gt; column exists if POSTAL_POPULATION_TABLE set if RIF40_GEOGAPHIES

DECLARE @TABLE_NAMES table 
(
ID INT IDENTITY(1,1), Name VARCHAR(255)
)

INSERT INTO @TABLE_NAMES (Name)
select distinct B.POSTAL_POPULATION_TABLE 
		from inserted  a
		left outer join RIF40_GEOGRAPHIES b
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

if @TABLE_NAMES2 is not null 
begin 
raiserror ('Table/s do not exist: %s' ,16,1, @TABLE_NAMES2)
end 

end 


------------------------------------------------------------------
------------LAST CONDITION ON GEOLEVEL TRIGGER -------------------
------------------------------------------------------------------

		select distinct a.geography, b.POSTAL_POPULATION_TABLE,a.CENTROIDXCOORDINATE_COLUMN,a.CENTROIDYCOORDINATE_COLUMN,
		a.CENTROIDSFILE
		from T_RIF40_GEOLEVELS a
		left outer join RIF40_GEOGRAPHIES b
		on a.GEOGRAPHY=b.GEOGRAPHY
		where	b.POSTAL_POPULATION_TABLE is not null and 
				(a.CENTROIDXCOORDINATE_COLUMN is not null and a.CENTROIDYCOORDINATE_COLUMN is not null) OR
				a.CENTROIDSFILE is not null 
------------------------------------------------------------

DECLARE @TABLE_NAMES nvarchar(MAX) =
(
		select distinct B.POSTAL_POPULATION_TABLE + ' '
		from T_RIF40_GEOLEVELS a
		left outer join RIF40_GEOGRAPHIES b
		on a.GEOGRAPHY=b.GEOGRAPHY
		where	b.POSTAL_POPULATION_TABLE is not null and 
				(a.CENTROIDXCOORDINATE_COLUMN is not null and a.CENTROIDYCOORDINATE_COLUMN is not null) OR
				a.CENTROIDSFILE is not null 
				FOR XML PATH('') 

)
IF  OBJECT_ID(@TABLE_NAMES, 'U') IS NULL
PRINT 'Table not found : ' +  @TABLE_NAMES
RAISERROR('TABLE NOT FOUND %S',16,1,@TABLE_NAMES)

---------------------------------------------------------------
---------------------INSERT STATEMENTS ------------------------
---------------------------------------------------------------

insert into [dbo].[T_RIF40_GEOLEVELS](GEOGRAPHY, GEOLEVEL_NAME, GEOLEVEL_ID, DESCRIPTION, LOOKUP_TABLE, LOOKUP_DESC_COLUMN, CENTROIDXCOORDINATE_COLUMN, CENTROIDYCOORDINATE_COLUMN, SHAPEFILE, CENTROIDSFILE, SHAPEFILE_TABLE, SHAPEFILE_AREA_ID_COLUMN, SHAPEFILE_DESC_COLUMN, ST_SIMPLIFY_TOLERANCE, CENTROIDS_TABLE, CENTROIDS_AREA_ID_COLUMN, AVG_NPOINTS_GEOM, AVG_NPOINTS_OPT, FILE_GEOJSON_LEN, LEG_GEOM, LEG_OPT, COVARIATE_TABLE, RESTRICTED, RESOLUTION, COMPAREA, LISTING)
values ('UK91'	,'WARD95',	6	,'Wards  1991'	,'UK91_WARD91',	'NAME'	,'NULL',	NULL,	NULL	,NULL	,'X_UK91_WARD91'	,'WARD91'	,'WARD91_NAM',	30	,NULL,	NULL,	NULL,	NULL	,NULL	,NULL	,NULL,	'UK91_COVARIATES_WARD91'	,1	,1	,1,0) ,
 ('UK91'	,'WARD91',	6	,'Wards  1991'	,'UK91_WARD91',	'NAME'	,'NULL',	NULL,	NULL	,NULL	,'X_UK91_WARD91'	,'WARD91'	,'WARD91_NAM',	30	,NULL,	NULL,	NULL,	NULL	,NULL	,NULL	,NULL,	'UK91_COVARIATES_WARD91'	,1	,1	,1,0	)

 -----------------------------------------------------------------
 -----------------------create dummy table to test ---------------
 -----------------------------------------------------------------
 create table ARP_POSTCODE_SUMMARY_R10_test 
 (id int identity(1,1), name varchar(25) )

select * from sys.tables where name='ARP_POSTCODE_SUMMARY_R10_test'

----------------------------------------------------
-- ------last from stack 
----------------------------------------------------
set nocount on 
DECLARE @TABLE_NAMES table 
(
ID INT IDENTITY(1,1), Name VARCHAR(255)
)

INSERT INTO @TABLE_NAMES (Name)
select distinct B.POSTAL_POPULATION_TABLE 
		from T_RIF40_GEOLEVELS a
		left outer join RIF40_GEOGRAPHIES b
		on a.GEOGRAPHY=b.GEOGRAPHY
		where	b.POSTAL_POPULATION_TABLE is not null and 
				(a.CENTROIDXCOORDINATE_COLUMN is not null and a.CENTROIDYCOORDINATE_COLUMN is not null) OR
				a.CENTROIDSFILE is not null 

DECLARE @TABLE_NAMES2 nvarchar(MAX) =
(
SELECT name + ' '
FROM @TABLE_NAMES
where OBJECT_ID(Name) is null 
FOR XML PATH('') 
)
--print 'These table/s do not exist:' +  @TABLE_NAMES2
raiserror ('Table/s do not exist: %s' ,16,1, @TABLE_NAMES2)








