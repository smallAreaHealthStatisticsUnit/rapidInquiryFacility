/*
<trigger_rif40_tables_checks_description>
<para>
Check TABLE_NAME exists. DO NOT RAISE AN ERROR IF IT DOES; otherwise check, column <TABLE_NAME>.TOTAL_FIELD,  column <TABLE_NAME>.ICD_FIELD_NAME exists. This allows the RIF40 schema owner to not have access to the tables. Access is checked in RIF40_NUM_DENOM. Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). A user specific T_RIF40_NUM_DENOM is supplied for other combinations. Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.
Check table_name, total_field, sex_field_name, age_group_field_name, age_sex_group_field_name Oracle names.
</para>
</trigger_rif40_tables_checks_description>
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_tables_checks')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_tables_checks]
END
GO

------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_tables_checks]
on [rif40].[rif40_tables]
BEFORE insert , update 
as
BEGIN 
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

