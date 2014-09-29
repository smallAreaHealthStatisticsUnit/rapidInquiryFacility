/**** OUTCOME TRIGGER

Check current_lookup table exists
Check previous lookup table exists if NOT NULL 
Check current_value_Nchar AND current_description_Nchar columns exists if NOT NULL : need clarification
Check previous_value_Nchar AND previous_description_Nchar if NOT NULL AND previous_lookup_table IS NOT NULL: need clarification , as canr see the proc or fucntion 

****/

insert into [dbo].[RIF40_OUTCOMES]
(OUTCOME_TYPE, OUTCOME_DESCRIPTION, CURRENT_VERSION, CURRENT_SUB_VERSION, PREVIOUS_VERSION, PREVIOUS_SUB_VERSION, CURRENT_LOOKUP_TABLE, PREVIOUS_LOOKUP_TABLE, CURRENT_VALUE_1CHAR, CURRENT_VALUE_2CHAR, CURRENT_VALUE_3CHAR, CURRENT_VALUE_4CHAR, CURRENT_VALUE_5CHAR, CURRENT_DESCRIPTION_1CHAR, CURRENT_DESCRIPTION_2CHAR, CURRENT_DESCRIPTION_3CHAR, CURRENT_DESCRIPTION_4CHAR, CURRENT_DESCRIPTION_5CHAR, PREVIOUS_VALUE_1CHAR, PREVIOUS_VALUE_2CHAR, PREVIOUS_VALUE_3CHAR, PREVIOUS_VALUE_4CHAR, PREVIOUS_VALUE_5CHAR, PREVIOUS_DESCRIPTION_1CHAR, PREVIOUS_DESCRIPTION_2CHAR, PREVIOUS_DESCRIPTION_3CHAR, PREVIOUS_DESCRIPTION_4CHAR, PREVIOUS_DESCRIPTION_5CHAR)
values('Test1-ICD',	'International Classification of Disease',	'10'	,'11th Revision - 2010',	'9',	NULL	,'RIF40_ICD10',	'RIF40_ICD90',	'ICD10_1CHAR',	NULL	,'ICD10_3CHAR',	'ICD10_4CHAR',	NULL	,'TEXT_1CHAR',	NULL,	'TEXT_3CHAR',	'TEXT_4CHAR',	NULL,	NULL,	NULL,	'ICD9_3CHAR',	'ICD9_4CHAR',	NULL	,NULL,	NULL,	'TEXT_3CHAR'	,'TEXT_4CHAR',	NULL),
('test2-ICD',	'International Classification of Disease',	'10'	,'11th Revision - 2010',	'9',	NULL	,'RIF40_ICD104',	null,	'ICD10_1CHAR',	NULL	,'ICD10_3CHAR',	'ICD10_4CHAR',	NULL	,'TEXT_1CHAR',	NULL,	'TEXT_3CHAR',	'TEXT_4CHAR',	NULL,	NULL,	NULL,	'ICD9_3CHAR',	'ICD9_4CHAR',	NULL	,NULL,	NULL,	'TEXT_3CHAR'	,'TEXT_4CHAR',	NULL)



-----------------------------
--trigger code 
-----------------------------
create alter trigger tr_study_outcome_check
on [dbo].[RIF40_OUTCOMES]
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
    RAISERROR('these lookup table/s do not exist: %s', 16, 1, @tablelist) with log;
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
    RAISERROR('these previous lookup table/s do not exist: %s', 16, 1, @prevtablelist) with log;
END;
	   end 
END 



