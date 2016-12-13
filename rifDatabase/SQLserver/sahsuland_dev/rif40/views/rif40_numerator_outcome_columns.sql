USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_numerator_outcome_columns]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_numerator_outcome_columns]
END
GO

CREATE VIEW [rif40].[rif40_numerator_outcome_columns] AS 
 WITH a AS (
         SELECT z.geography,
            a_1.table_name,
            c.outcome_group_name,
            c.outcome_type,
            c.outcome_group_description,
            c.field_name,
            c.multiple_field_count
           FROM [rif40].[rif40_num_denom] z,
            [rif40].[rif40_tables] a_1,
            [rif40].[rif40_table_outcomes] b,
            [rif40].[rif40_outcome_groups] c
          WHERE a_1.table_name = z.numerator_table AND a_1.table_name = b.numer_tab
        )
 SELECT a.geography,
    a.table_name,
    a.outcome_group_name,
    a.outcome_type,
    a.outcome_group_description,
    a.field_name,
    a.multiple_field_count,
        CASE
            WHEN d.name IS NOT NULL THEN 'true'
            ELSE 'false'
        END AS column_exists,
    e.value AS column_comment
   FROM a
     LEFT JOIN sys.columns d
            ON lower(d.name) collate database_default = lower(a.field_name) collate database_default and Object_ID = object_id(a.table_name)
	LEFT JOIN  sys.extended_properties e
		ON e.name='MS_Description' and major_id=object_id(a.table_name)
		and e.minor_id=0
GO

GRANT SELECT ON [rif40].[rif40_numerator_outcome_columns] TO [rif_manager]
GO
GRANT SELECT ON [rif40].[rif40_numerator_outcome_columns] TO [rif_user]
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'All numerator outcome fields (columns)' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table name' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns', @level2type=N'COLUMN',@level2name=N'table_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Group Name. E.g SINGLE_VARIABLE_ICD' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns', @level2type=N'COLUMN',@level2name=N'outcome_group_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome type: ICD, ICD-0 or OPCS' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns', @level2type=N'COLUMN',@level2name=N'outcome_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Group Description. E.g. &quot;Single variable ICD&quot;' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns', @level2type=N'COLUMN',@level2name=N'outcome_group_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table outcome field name, e.g. ICD_SAHSU_01, ICD_SAHSU' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns', @level2type=N'COLUMN',@level2name=N'field_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Group multiple field count (0-99). E.g if NULL then field is ICD_SAHSU_01; if 20 then fields are ICD_SAHSU_01 to ICD_SAHSU_20. Field numbers are assumed to tbe left padded to 2 characters with &quot;0&quot; and preceeded by an &quot;_&quot;' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns', @level2type=N'COLUMN',@level2name=N'multiple_field_count'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table outcome columnn exists (''yes'' or ''no'')' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns', @level2type=N'COLUMN',@level2name=N'column_exists'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table outcome column comment' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_numerator_outcome_columns', @level2type=N'COLUMN',@level2name=N'column_comment'
GO

