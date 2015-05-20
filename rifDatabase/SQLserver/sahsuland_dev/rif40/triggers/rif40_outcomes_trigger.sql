/*
 OUTCOME TRIGGER
Check current_lookup table exists
Check previous lookup table exists if NOT NULL 
Check current_value_Nchar AND current_description_Nchar columns exists if NOT NULL : need clarification
Check previous_value_Nchar AND previous_description_Nchar if NOT NULL AND previous_lookup_table IS NOT NULL: need clarification , as canr see the proc or fucntion 
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_study_outcome_check')
BEGIN
	DROP TRIGGER [rif40].[tr_study_outcome_check]
END
GO

-----------------------------
--trigger code 
-----------------------------
CREATE trigger [tr_study_outcome_check]
on [rif40].[rif40_outcomes]
instead of insert, update
as
begin

--Check current_lookup table exists
DECLARE @tablelist nvarchar(MAX) =
    (
    SELECT 
        CURRENT_LOOKUP_TABLE + ' '
        FROM inserted
        WHERE OBJECT_ID(CURRENT_LOOKUP_TABLE, 'U') IS NULL
        FOR XML PATH('')
    );

IF @tablelist IS NOT NULL
BEGIN
    RAISERROR(50200, 16, 1, @tablelist) with log;
END;
--Check previous lookup table exists if NOT NULL
 IF exists ( select 1 from inserted where [PREVIOUS_LOOKUP_TABLE] IS NOT NULL) or 
	   ( UPDATE([PREVIOUS_LOOKUP_TABLE]))
	begin
	   DECLARE @prevtablelist nvarchar(MAX) =
		(
		 SELECT 
        [PREVIOUS_LOOKUP_TABLE] + ' '
        FROM inserted
        WHERE OBJECT_ID([PREVIOUS_LOOKUP_TABLE], 'U') IS NULL
        FOR XML PATH('')
		 );

IF @prevtablelist IS NOT NULL
BEGIN
    RAISERROR(50201, 16, 1, @prevtablelist) with log;
END;
	   end 
END 
GO