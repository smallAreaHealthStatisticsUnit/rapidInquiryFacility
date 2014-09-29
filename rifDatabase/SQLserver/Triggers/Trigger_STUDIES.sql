/***************
trigger for  

Check - USERNAME exists.
Check - USERNAME is Kerberos USER on INSERT.
Check - audsid is SYS_CONTEXT('USERENV', 'SESSIONID') on INSERT.
Check - UPDATE not allowed except for IG admin and state changes.
Check - DELETE only allowed on own records.
Check - EXTRACT_TABLE Oracle name.

Check - Comparison area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1
Check - DENOM_TAB, DIRECT_STAND_TAB are valid Oracle names and appropriate denominators, and user has access.
Check - Study area resolution (GEOLEVEL_ID) >= comparision area resolution (GEOLEVEL_ID)  [i.e study area has the same or higher resolution]

Check - suppression_value - Suppress results with low cell counts below this value. If the role RIF_NO_SUPRESSION is granted and the user is not a RIF_STUDENT then SUPPRESSION_VALUE=0; otherwise is equals the parameter "SuppressionValue". If >0 all results with the value or below will be set to 0.
Check - extract_permitted - Is extract permitted from the database: 0/1. Only a RIF MANAGER may change this value. This user is still permitted to create and run a RIF study and to view the results. Geolevel access is rectricted by the RIF40_GEOLEVELS.RESTRICTED Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. All students must be granted permission by a RIF_MANAGER for any extract if the system parameter ExtractControl=1. This is enforced by the RIF application.

Check - authorised_by - must be a RIF MANAGER.

Check - transfer_permitted - Is transfer permitted from the Secure or Private Network: 0/1. This is for purely documentatary purposes only. Only a RIF MANAGER may change this value. The value defaults to the same as EXTRACT_PERMITTED. Only geolevels where RIF40_GEOLEVELS.RESTRICTED=0 may be transferred

Check - authorised_notes -IG authorisation notes. Must be filled in if EXTRACT_PERMITTED=1

Delayed RIF40_TABLES denominator and direct standardisation checks:
Check - Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists


***********/


insert into [dbo].[T_RIF40_STUDIES]
(STUDY_ID, USERNAME, GEOGRAPHY, PROJECT, STUDY_NAME, SUMMARY, DESCRIPTION, OTHER_NOTES, EXTRACT_TABLE, MAP_TABLE, STUDY_DATE, STUDY_TYPE, STUDY_STATE, COMPARISON_GEOLEVEL_NAME, STUDY_GEOLEVEL_NAME, DENOM_TAB, DIRECT_STAND_TAB, SUPPRESSION_VALUE, EXTRACT_PERMITTED, TRANSFER_PERMITTED, AUTHORISED_BY, AUTHORISED_ON, AUTHORISED_NOTES, AUDSID)
values 
(
113,	'PETERH@PRIVATE.NET',	'EW01',	'SAHSU'	,'Enter study name here'	,NULL,	NULL,	NULL	,'S503_EXTRACT_TEST13'	,'S503_MAP_TEST13',	'2014-03-03 16:31:44'	,1	,'C',	'SOA2001_comptest'	,'SOA2001_studytest',	'V_EW01_RIF_POP_ASG_1_OA2001',	NULL	,5	,0	,0,	NULL	,NULL	,NULL,	68796934
),
(
114,	'PETERH@PRIVATE.NET',	'EW01_TEST',	'SAHSU'	,'Enter study name here'	,NULL,	NULL,	NULL	,'S503_EXTRACT_TEST14'	,'S503_MAP_TEST14',	'2014-03-03 16:31:44'	,1	,'C',	'SOA2001'	,'SOA2001',	'V_EW01_RIF_POP_ASG_1_OA2001',	NULL	,5	,0	,0,	NULL	,NULL	,NULL,	68796934
)

--------------------------------
--- session if default
--------------------------------
alter table [dbo].[T_RIF40_STUDIES]
add default @@SPID for [AUDSID]

-

------------------------------
-- create trigger code 
------------------------------
create alter trigger tr_studies_checks
on [dbo].[T_RIF40_STUDIES]
for insert , update 
as
BEGIN 
------------------------------------------------------------------------------------------
--Check - Comparison area geolevel name. 
--Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1
------------------------------------------------------------------------------------------------

	 DECLARE @geolevel_name nvarchar(MAX) =
		(
		SELECT concat ([STUDY_ID],'-', [COMPARISON_GEOLEVEL_NAME], '-', geography )
		 + '  '
		FROM inserted ic 
		where not EXISTS (SELECT 1 FROM [dbo].[T_RIF40_GEOLEVELS] c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
				    c.geolevel_name=ic.[COMPARISON_GEOLEVEL_NAME] and
					c.[COMPAREA]=1)
        FOR XML PATH('')
		 );

	IF @geolevel_name IS NOT NULL
		BEGIN
			RAISERROR('Geolevel name not found ,studyid-comparison_geolevel_name-geography: %s', 16, 1, @geolevel_name) with log;
		END;
-----------------------------------------------------------------------------------------------------------
-- Check - STUDY_GEOLEVEL_NAME. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS
-----------------------------------------------------------------------------------------------------------

	 DECLARE @geolevel_name2 nvarchar(MAX) =
		(
		SELECT concat ([STUDY_ID],'-', [STUDY_GEOLEVEL_NAME], '-', geography )
		 + '  '
		FROM inserted ic 
		where not EXISTS (SELECT 1 FROM [dbo].[T_RIF40_GEOLEVELS] c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
				    c.geolevel_name=ic.[STUDY_GEOLEVEL_NAME] 
						)
        FOR XML PATH('')
		 );

	IF @geolevel_name2 IS NOT NULL
		BEGIN
			RAISERROR('Geolevel name not found ,studyid-study_geolevel_name-geography: %s', 16, 1, @geolevel_name2) with log;
		END;
-------------------------------------
-- Check -  direct denominator
------------------------------------

DECLARE @direct_denom nvarchar(MAX) =
 (
SELECT a.TABLE_NAME + '  '
FROM rif40_tables a
WHERE table_name IN (select direct_stand_tab from [dbo].[T_RIF40_STUDIES] where direct_stand_tab is not null)
and a.ISDIRECTDENOMINATOR <>1
 FOR XML PATH('')
)

IF @direct_denom IS NOT NULL
		BEGIN
			RAISERROR('direct standardisation table: %s is not a direct denominator table: %s', 16, 1, @direct_denom) with log;
		END;

DECLARE @indirect_denom nvarchar(MAX) =
 (
SELECT a.TABLE_NAME + '  '
FROM rif40_tables a
WHERE table_name IN (select direct_stand_tab from [dbo].[T_RIF40_STUDIES] where direct_stand_tab is not null)
and a.ISINDIRECTDENOMINATOR <>1
 FOR XML PATH('')
)

IF @indirect_denom IS NOT NULL
		BEGIN
			RAISERROR('study %s denominator: %s is not a denominator table: %s', 16, 1, @indirect_denom) with log;
		END;
------------------------------
-- max/min age group is null-- NEED TO FINISH 
------------------------------
DECLARE @min_age nvarchar(MAX) =
 (
SELECT a.TABLE_NAME + '  '
FROM rif40_tables a
LEFT OUTER JOIN rif40_age_groups g ON (g.age_group_id  = a.age_group_id)
WHERE table_name IN (select direct_stand_tab from [dbo].[T_RIF40_STUDIES] where direct_stand_tab is not null)
GROUP BY a.TABLE_NAME,a.year_start, a.year_stop, a.isindirectdenominator, a.isdirectdenominator, a.isnumerator,
a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field
having MAX(g.offset) is  null or MIN(g.offset) is  null 
 FOR XML PATH('')
)

IF @min_age IS NOT NULL
		BEGIN
			RAISERROR('study %s denominator: %s is not a denominator table: %s', 16, 1, @min_age) with log;
		END;


-------------------------------------------------------------------------------------------------
-- Check - Comparison area geolevel name(COMPARISON_GEOLEVEL_NAME). 
--Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1.
--------------------------------------------------------------------------------------------------

DECLARE @COMP_GEOLEVEL nvarchar(MAX) =
 (
SELECT a.GEOLEVEL_NAME + '  '
FROM T_RIF40_GEOLEVELS a
WHERE A.GEOLEVEL_NAME IN	(select COMPARISON_GEOLEVEL_NAME from INSERTED 
							 where COMPARISON_GEOLEVEL_NAME is not null AND COMPARISON_GEOLEVEL_NAME <>'') and 
		a.GEOGRAPHY IN		(select GEOGRAPHY from INSERTED)
		 FOR XML PATH('')
 
)
IF @COMP_GEOLEVEL IS NOT NULL
		BEGIN
			RAISERROR('omparison area geolevel name: "%" not found in RIF40_GEOLEVELS: %s', 16, 1, @COMP_GEOLEVEL) with log;
		END;

DECLARE @COMP_GEOLEVEL2 nvarchar(MAX) =
(
SELECT a.GEOLEVEL_NAME + '  '
FROM T_RIF40_GEOLEVELS a
WHERE A.GEOLEVEL_NAME IN	(select COMPARISON_GEOLEVEL_NAME from INSERTED 
							 where COMPARISON_GEOLEVEL_NAME is not null AND COMPARISON_GEOLEVEL_NAME <>'') and 
		a.GEOGRAPHY IN		(select GEOGRAPHY from INSERTED) and 
		a.COMPAREA <>1 
		 FOR XML PATH('')

)
IF @COMP_GEOLEVEL IS NOT NULL
		BEGIN
			RAISERROR('comparison area geolevel name in RIF40_GEOLEVELS is not a comparison area: %s', 16, 1, @COMP_GEOLEVEL) with log;
		END;
	
DECLARE @COMP_GEOLEVEL3 nvarchar(MAX) =
(
SELECT a.STUDY_ID + '  '
FROM inserted  a
WHERE a.COMPARISON_GEOLEVEL_NAME is null 
		 FOR XML PATH('')

)
IF @COMP_GEOLEVEL3 IS NOT NULL
		BEGIN
			RAISERROR('study has NULL comparison area geolevel name: %s', 16, 1, @COMP_GEOLEVEL3) with log;
		END;
		
END	
-------------------------------------------------------------------------------------------------------------
-- Check - STUDY_GEOLEVEL_NAME. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS
-------------------------------------------------------------------------------------------------------------
DECLARE @STUDY_GEOLEVEL nvarchar(MAX) =
(
SELECT a.GEOLEVEL_NAME + '  '
FROM T_RIF40_GEOLEVELS a
WHERE A.GEOLEVEL_NAME IN	(select STUDY_GEOLEVEL_NAME from INSERTED 
							 where STUDY_GEOLEVEL_NAME is not null AND STUDY_GEOLEVEL_NAME <>'') and 
		a.GEOGRAPHY IN		(select GEOGRAPHY from INSERTED) and 
	   FOR XML PATH('')

)
IF @STUDY_GEOLEVEL IS NOT NULL
		BEGIN
			RAISERROR(' study area geolevel name: "%" not found in RIF40_GEOLEVELS: %s', 16, 1, @STUDY_GEOLEVEL) with log;
		END;
----------------------------------------------------------------------------------------------------------------
-- Check - DENOM_TAB, DIRECT_STAND_TAB are valid Oracle names and appropriate denominators, and user has access.
----------------------------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------
-- Check - Study area resolution (GEOLEVEL_ID) >= comparision area resolution (GEOLEVEL_ID)
----------------------------------------------------------------------------------------------
DECLARE @GEOLEVEL2 nvarchar(MAX) =
(
	select concat( 'studyid-',X.STUDY_ID ,'/','study geolevel-', X.STUDY_GEOLEVEL_NAME,'/' ,'study geo ID-', X.Study_area_geovlevelID,'/',
				   'comparison geolevel name-' ,y.COMPARISON_GEOLEVEL_NAME ,'/','comparison geo id-',y.COMPARISON_area_geolevelID) + ' '
from (
	select distinct b. study_id , a.GEOGRAPHY , a.[GEOLEVEL_ID] as Study_area_geovlevelID, 
	b.STUDY_GEOLEVEL_NAME
	from [dbo].[T_RIF40_GEOLEVELS] a  INNER JOIN [dbo].[T_RIF40_STUDIES] b 
	ON  a.[GEOGRAPHY]=b.GEOGRAPHY 
	WHERE  a.GEOLEVEL_NAME=b.STUDY_GEOLEVEL_NAME
) x ,
(
	select distinct b.study_id, a.GEOGRAPHY , a.[GEOLEVEL_ID] as COMPARISON_area_geolevelID, 
	b.COMPARISON_GEOLEVEL_NAME
	from [dbo].[T_RIF40_GEOLEVELS] a INNER JOIN [dbo].[T_RIF40_STUDIES] b 
	ON a.[GEOGRAPHY]=b.GEOGRAPHY 
	WHERE a.GEOLEVEL_NAME=b.COMPARISON_GEOLEVEL_NAME
) Y
where x.STUDY_ID=y.STUDY_ID and x.Study_area_geovlevelID>=y.COMPARISON_area_geolevelID
FOR XML PATH('')
)
IF @GEOLEVEL2 IS NOT NULL
		BEGIN
			RAISERROR(' T_RIF40_STUDIES study area geolevel id < comparision area  [i.e study area IS LOWER THAN COMPARISON area  resolution] %s',16, 1, @GEOLEVEL2) 
			-- there are TWO LOG entries 
		END;
		
----------------------------------------
-- finding total field column 
-----------------------------------------	

DECLARE @total_field nvarchar(MAX) =
(
SELECT t.name +' '
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where t.name  in
 (
 SELECT 
  a.[TABLE_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.[TOTAL_FIELD] is not null 
  )
  AND 
  c.name in  (select  a.TOTAL_FIELD
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.[TOTAL_FIELD] is not null )	
  FOR XML PATH('')
 )
 IF @total_field IS NOT NULL
		BEGIN
			RAISERROR(' T_RIF40_STUDIES direct standardisation RIF40_TABLES total field column missing for : ',16, 1, @total_field) 
			-- there are TWO LOG entries 
		END;

--------------------------------
--- finding age sex group 
-----------------------------------
DECLARE @age_sex_GROUP nvarchar(MAX) =
(
SELECT t.name 
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where t.name  in
 (
 SELECT 
  a.[TABLE_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [AGE_SEX_GROUP_FIELD_NAME]
 is not null 
  )
  AND 
  c.name in  (select  a.  [AGE_SEX_GROUP_FIELD_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [AGE_SEX_GROUP_FIELD_NAME]
 is not null )
)
 IF @age_sex_GROUP IS NOT NULL
		BEGIN
			RAISERROR(' T_RIF40_STUDIES direct standardisation RIF40_TABLES AGE SEX GROUP column missing for : ',16, 1, @age_sex_GROUP) 
			-- there are TWO LOG entries 
		END;
--------------------------------
--- finding age group 
-----------------------------------
DECLARE @age_GROUP nvarchar(MAX) =
(
SELECT t.name 
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where t.name  in
 (
 SELECT 
  a.[TABLE_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [AGE_GROUP_FIELD_NAME]
 is not null 
  )
  AND 
  c.name in  (select  a.  [AGE_GROUP_FIELD_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [AGE_GROUP_FIELD_NAME]
 is not null )
)
 IF @age_GROUP IS NOT NULL
		BEGIN
			RAISERROR(' T_RIF40_STUDIES direct standardisation RIF40_TABLES AGE GROUP column missing for : ',16, 1, @age_GROUP) 
			-- there are TWO LOG entries 
		END 
		
--------------------------------
--- finding sex field name 		
---------------------------------
DECLARE @sex_field nvarchar(MAX) =
(
SELECT t.name 
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where t.name  in
 (
 SELECT 
  a.[TABLE_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [SEX_FIELD_NAME]
 is not null 
  )
  AND 
  c.name in  (select  a.  [sex_FIELD_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [sex_FIELD_NAME]
 is not null )
)
 IF @sex_field IS NOT NULL
		BEGIN
			RAISERROR(' T_RIF40_STUDIES direct standardisation RIF40_TABLES sex field name missing for : ',16, 1, @sex_field) 
			-- there are TWO LOG entries 
		END 			  	
end 
	

--=================================
/**** RH : testing something  
===================================


SELECT a.TABLE_NAME,a.year_start, a.year_stop, a.isindirectdenominator, a.isdirectdenominator, a.isnumerator,
a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field,
MIN(g.offset) min_age_group, MAX(g.offset) max_age_group
FROM rif40_tables a
LEFT OUTER JOIN rif40_age_groups g ON (g.age_group_id  = a.age_group_id)
WHERE table_name IN (select direct_stand_tab from [dbo].[T_RIF40_STUDIES] where direct_stand_tab is not null)
and a.ISDIRECTDENOMINATOR =1
GROUP BY a.TABLE_NAME,a.year_start, a.year_stop, a.isindirectdenominator, a.isdirectdenominator, a.isnumerator,
a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field
having MAX(g.offset) is not null or MIN(g.offset) is not null 



select X.STUDY_ID , X.STUDY_GEOLEVEL_NAME , X.Study_area_geovlevelID,
 X.STUDY_GEOLEVEL_NAME , y.COMPARISON_area_geolevelID,y.COMPARISON_GEOLEVEL_NAME
from (
select distinct b. study_id , a.GEOGRAPHY , a.[GEOLEVEL_ID] as Study_area_geovlevelID, 
b.STUDY_GEOLEVEL_NAME
from [dbo].[T_RIF40_GEOLEVELS] a , [dbo].[T_RIF40_STUDIES] b 
where a.[GEOGRAPHY]=b.GEOGRAPHY and a.GEOLEVEL_NAME=b.STUDY_GEOLEVEL_NAME
) x ,
(
select distinct b.study_id, a.GEOGRAPHY , a.[GEOLEVEL_ID] as COMPARISON_area_geolevelID, 
b.COMPARISON_GEOLEVEL_NAME
from [dbo].[T_RIF40_GEOLEVELS] a , [dbo].[T_RIF40_STUDIES] b 
where a.[GEOGRAPHY]=b.GEOGRAPHY and a.GEOLEVEL_NAME=b.COMPARISON_GEOLEVEL_NAME
) Y
where x.STUDY_ID=y.STUDY_ID and x.Study_area_geovlevelID>=y.COMPARISON_area_geolevelID




select * from T_RIF40_GEOLEVELS


select distinct a.GEOGRAPHY ,a.[GEOLEVEL_ID] as comparison_area_geoveleID, 
b.COMPARISON_GEOLEVEL_NAME
from [dbo].[T_RIF40_GEOLEVELS] a , [dbo].[T_RIF40_STUDIES] b 
where a.[GEOGRAPHY]=b.GEOGRAPHY and a.GEOLEVEL_NAME=b.COMPARISON_GEOLEVEL_NAME
order by geography , GEOLEVEL_ID




 ----------------------------------------
 -- USING CTE try one - successfull 
 ----------------------------------------

 ;WITH CTE1 AS (
	select distinct b. study_id , a.GEOGRAPHY , a.[GEOLEVEL_ID] as Study_area_geovlevelID, 
	b.STUDY_GEOLEVEL_NAME
	from [dbo].[T_RIF40_GEOLEVELS] a INNER JOIN [dbo].[T_RIF40_STUDIES] b 
	ON  a.[GEOGRAPHY]=b.GEOGRAPHY 
	WHERE  a.GEOLEVEL_NAME=b.STUDY_GEOLEVEL_NAME
	),
 CTE2 AS (
 	select distinct b.study_id, a.GEOGRAPHY , a.[GEOLEVEL_ID] as COMPARISON_area_geolevelID, 
	b.COMPARISON_GEOLEVEL_NAME
	from [dbo].[T_RIF40_GEOLEVELS] a INNER JOIN [dbo].[T_RIF40_STUDIES] b 
	ON a.[GEOGRAPHY]=b.GEOGRAPHY 
	WHERE a.GEOLEVEL_NAME=b.COMPARISON_GEOLEVEL_NAME
 )
 SELECT concat( CTE1.STUDY_ID ,CTE1.STUDY_GEOLEVEL_NAME, CTE1.Study_area_geovlevelID,
				CTE2.COMPARISON_GEOLEVEL_NAME ,CTE2.COMPARISON_area_geolevelID) 
 FROM CTE1
 inner join CTE2 on cte1.study_id =cte2.study_id and 
			CTE1.Study_area_geovlevelID>=CTE2.COMPARISON_area_geolevelID
 GO

 --------------------------
 --using JOIN
 --------------------------



	select distinct b. study_id , a.GEOGRAPHY , B.[GEOLEVEL_ID] as Study_area_geovlevelID, 
	b.STUDY_GEOLEVEL_NAME,
	C.GEOLEVEL_ID AS COMPARISON_area_geolevelID
	from [dbo].[T_RIF40_STUDIES] a 
	 INNER JOIN b [dbo].[T_RIF40_GEOLEVELS]
	ON  a.[GEOGRAPHY]=b.GEOGRAPHY and a.GEOLEVEL_NAME=b.STUDY_GEOLEVEL_NAME INNER JOIN 
	[dbo].[T_RIF40_GEOLEVELS] C
	ON B.[GEOGRAPHY]=C.GEOGRAPHY and C.GEOLEVEL_NAME=B.STUDY_GEOLEVEL_NAME
	where B.STUDY_ID=C.STUDY_ID and B.Study_area_geovlevelID>=C.COMPARISON_area_geolevelID


-------------------------
--- FROM STACK
-------------------------
 
select t1.d_id, t1.employee_name, te.emp_id,
       t1.employee_name as manager_name, tm.emp_id as manager_id
from tbl1 t1 join
     tbl2 te
     on t1.employee_name = te.employee_name join
     tbl2 tm
     on t1.manager_name = tm.employee_name
where te.emp_id > tm.emp_id;

 **********/


 
SELECT a.TABLE_NAME,
a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field
FROM rif40_tables a
LEFT OUTER JOIN rif40_age_groups g ON (g.age_group_id  = a.age_group_id)
WHERE table_name IN (select DENOM_TAB from [dbo].[T_RIF40_STUDIES] where DENOM_TAB  is not null)
GROUP BY a.TABLE_NAME,
a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field


SELECT t.name AS table_name,
 c.name AS column_name
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where t.name IN (
 SELECT a.TABLE_NAME
 FROM rif40_tables a
LEFT OUTER JOIN rif40_age_groups g ON (g.age_group_id  = a.age_group_id)
WHERE table_name IN (select DENOM_TAB from [dbo].[T_RIF40_STUDIES] where DENOM_TAB  is not null)
GROUP BY a.TABLE_NAME,
a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field
)


SELECT t.name AS table_name,
 c.name AS column_name
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where t.name  in (
 select x.DENOM_TAB from [dbo].[T_RIF40_STUDIES] x
 inner join RIF40_TABLES y on x.DENOM_TAB=y.TABLE_NAME 
 where x.DENOM_TAB is not null and y.total_field is not null)


 
 --------------this worked : finding 'total field' column --------------------------------------
 
 SELECT t.name AS table_name,
 c.name AS column_name
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where t.name  in
 (
 SELECT 
  a.[TABLE_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.[TOTAL_FIELD] is not null 
  )
  AND 
  c.name in  (select  a.TOTAL_FIELD
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.[TOTAL_FIELD] is not null )
 
 ----------------------------------------------------
 
 
  create table UK91_RIF_POP_ASG_1_ED91(id int identity, total int )
  alter table UK91_RIF_POP_ASG_1_ED91
  add  AGE_SEX_GROUP int 
  
  ---------------------age sex group ---------------------------------------------------

  SELECT t.name AS table_name,
 c.name AS column_name
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where t.name  in
 (
 SELECT 
  a.[TABLE_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [AGE_SEX_GROUP_FIELD_NAME]
 is not null 
  )
  AND 
  c.name in  (select  a.  [AGE_SEX_GROUP_FIELD_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [AGE_SEX_GROUP_FIELD_NAME]
 is not null )

 -------------------------------------------
 --------[SEX_FIELD_NAME]

 
  SELECT t.name AS table_name,
 c.name AS column_name
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where t.name  in
 (
 SELECT 
  a.[TABLE_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [SEX_FIELD_NAME]
 is not null 
  )
  AND 
  c.name in  (select  a.  [SEX_FIELD_NAME]
  FROM [RIF40].[dbo].[RIF40_TABLES] a
  where TABLE_NAME in (
  select denom_tab 
  from [dbo].[T_RIF40_STUDIES]
  where denom_tab is not null ) 
  and a.  [SEX_FIELD_NAME]
 is not null )