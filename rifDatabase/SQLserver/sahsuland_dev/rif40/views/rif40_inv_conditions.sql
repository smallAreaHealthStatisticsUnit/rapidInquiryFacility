/*
I assume that numer_tab in [t_rif40_investigations] has the schema specified so that "object_id(numer_tab)" will give a valid answer

"column_exists" returns 'true' and 'false' instead of boolean true/false in the postgres version
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_inv_conditions]') AND type=(N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_inv_conditions]
END
GO

CREATE VIEW [rif40].[rif40_inv_conditions] AS 
 WITH a AS (
         SELECT c.username,
            c.study_id,
            c.inv_id,
            c.line_number,
            c.min_condition,
            c.max_condition,
            c.predefined_group_name,
            c.outcome_group_name,
            i.numer_tab,
            b.field_name,
                CASE
                    WHEN c.predefined_group_name IS NOT NULL 
						THEN g.condition+'%'+QUOTENAME(lower(b.field_name))	+ ' /* Pre defined group: '+g.predefined_group_description+' */'
                    WHEN c.max_condition IS NOT NULL 
						THEN QUOTENAME(lower(b.field_name))+' BETWEEN '''+c.min_condition+''' AND '''+c.max_condition+'~'' /* Range filter */'
                    ELSE QUOTENAME(lower(b.field_name))+' LIKE '''+c.min_condition+'%'' /* Value filter */'
                END AS condition
           FROM [rif40].[t_rif40_investigations] i,
            [rif40].[rif40_outcome_groups] b,
            [rif40].[t_rif40_inv_conditions] c
             LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username = SUSER_SNAME()
             LEFT JOIN [rif40].[rif40_predefined_groups] g ON c.predefined_group_name = g.predefined_group_name
          WHERE c.inv_id = i.inv_id AND c.study_id = i.study_id AND c.outcome_group_name = b.outcome_group_name 
		  AND 
		  (c.username=SUSER_SNAME() OR 
		  SUSER_SNAME() = 'rif40' OR
		IS_MEMBER(N'[rif_manager]') = 1 OR 
	(s.grantee_username IS NOT NULL AND s.grantee_username <> ''))
        )
 SELECT a.username,
    a.study_id,
    a.inv_id,
    a.line_number,
    a.min_condition,
    a.max_condition,
    a.predefined_group_name,
    a.outcome_group_name,
    a.numer_tab,
    a.field_name,
    a.condition,
        CASE
            WHEN d.name IS NOT NULL THEN 'true'
            ELSE 'false'
        END AS columnn_exists,
    e.value AS column_comment
   FROM a
     LEFT JOIN sys.columns d
            ON lower(d.name) collate database_default = lower(a.field_name) collate database_default and Object_ID = object_id(a.numer_tab)
	LEFT JOIN  sys.extended_properties e
		ON e.name='MS_Description' and major_id=object_id(a.numer_tab)
		and e.minor_id=0
GO
  
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_inv_conditions] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_inv_conditions] TO [rif_manager]
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lines of SQL conditions pertinent to an investigation.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Line number' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'line_number'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Minimum condition; if max condition is not null SQL WHERE Clause evaluates to: "WHERE <field_name> LIKE ''<min_condition>%'' ". ', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'min_condition'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Maximum condition; if max condition is not null SQL WHERE Clause evaluates to: "WHERE <field_name> BETWEEN ''<min_condition> AND <max_condition>~''" ', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'max_condition'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Predefined Group Name. E.g LUNG_CANCER', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'predefined_group_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Group Name. E.g SINGLE_VARIABLE_ICD', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'outcome_group_name'
GO

--this won't work:
/*
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'''true'' if field_name exists, ''false'' if it does not', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'column_exists'
GO
*/
