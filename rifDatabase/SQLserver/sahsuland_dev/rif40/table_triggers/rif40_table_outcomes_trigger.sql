/*
table outcome triger 
Check current_version_start_year (if not NULL) BETWEEN rif40_tables.year_start AND rif40_tables.year_stop: 
NEED CLARIFICATION SHOULD THE TABLE NAME MATCH TO COMPARE A PARTICULAR START , STOP YEAR ? NUMER_TAB MATCHES WITH RIF40_TABLES TABLE_NAME
Check numer_tab is a numerator :NEED CLARIFICATION 
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_table_outcome')
BEGIN
	DROP TRIGGER [rif40].[tr_table_outcome]
END
GO


 -- current version start year 
 CREATE TRIGGER [tr_table_outcome]
 ON [rif40].[rif40_table_outcomes]
 FOR INSERT ,  UPDATE 
 AS
 BEGIN 

 DECLARE @table  nvarchar(MAX) =
		(
		SELECT 
        concat(a.OUTCOME_GROUP_NAME , a.numer_tab)+ ' '
		FROM inserted a
        where a.NUMER_TAB not in (select TABLE_NAME from [rif40].[rif40_tables] )
		
        FOR XML PATH('')
		 );

IF @table is NOT NULL
BEGIN
    RAISERROR('table does not exist %s', 16, 1, @table) with log;
END;

 -------check current start year  

	   DECLARE @STARTYEAR nvarchar(MAX) =
		(
		SELECT 
        concat(a.OUTCOME_GROUP_NAME , a.[CURRENT_VERSION_START_YEAR])+ ' '
		FROM inserted a
		inner join [rif40].[rif40_tables] b
        on a.NUMER_TAB=b.TABLE_NAME
		where a.CURRENT_VERSION_START_YEAR between b.YEAR_START and b.YEAR_STOP and a.CURRENT_VERSION_START_YEAR is not null 
        FOR XML PATH('')
		 );

IF @STARTYEAR is NOT NULL
BEGIN
    RAISERROR('Current version start date didnt match for %s', 16, 1, @STARTYEAR) with log;
END;
END 