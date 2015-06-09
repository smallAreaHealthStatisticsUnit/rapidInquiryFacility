/*
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
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_studies_checks')
BEGIN
	DROP TRIGGER [rif40].[tr_studies_checks]
END
GO

------------------------------
-- create trigger code 
------------------------------
CREATE trigger [tr_studies_checks]
on [rif40].[t_rif40_studies]
AFTER insert , update 
as
BEGIN 
------------------------------------------------------------------------------------------
--Check - Comparison area geolevel name. 
--Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1
------------------------------------------------------------------------------------------------

	 DECLARE @geolevel_name nvarchar(MAX) =
		(
		SELECT concat (ic.study_id,'-', ic.COMPARISON_GEOLEVEL_NAME, '-', ic.geography )
		 + '  '
		FROM inserted ic 
		LEFT OUTER JOIN ON [rif40].[t_rif40_geolevels] c
		ON ic.[geography]=c.[GEOGRAPHY] and 
			c.geolevel_name=ic.[COMPARISON_GEOLEVEL_NAME] and
			c.[COMPAREA]=1
		WHERE c.geography is null
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
		where not EXISTS (SELECT 1 FROM [rif40].[t_rif40_geolevels] c 
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
FROM [rif40].[rif40_tables] a
WHERE table_name IN (select direct_stand_tab from [rif40].[t_rif40_studies] where direct_stand_tab is not null)
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
FROM [rif40].[rif40_tables] a
WHERE table_name IN (select direct_stand_tab from [rif40].[t_rif40_studies] where direct_stand_tab is not null)
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
FROM [rif40].[rif40_tables] a
LEFT OUTER JOIN [rif40].[rif40_age_groups] g ON (g.age_group_id  = a.age_group_id)
WHERE table_name IN (select direct_stand_tab from [rif40].[t_rif40_studies] where direct_stand_tab is not null)
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
FROM [rif40].[t_rif40_geolevels] a
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
FROM [rif40].[t_rif40_geolevels]  a
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
	