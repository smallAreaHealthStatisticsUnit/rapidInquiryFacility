USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_outcome_groups]') AND type in (N'U'))
BEGIN

	--first disable foreign key references
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_conditions]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_inv_conditons_ogn')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_inv_conditions] DROP CONSTRAINT [t_rif40_inv_conditons_ogn];
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[rif40_table_outcomes]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='rif40_outcome_group_name_fk')
	BEGIN
		ALTER TABLE [rif40].[rif40_table_outcomes] DROP CONSTRAINT [rif40_outcome_group_name_fk];
	END;
	
	DROP TABLE [rif40].[rif40_outcome_groups];
END
GO

CREATE TABLE [rif40].[rif40_outcome_groups](
	[outcome_type] [varchar](20) NOT NULL,
	[outcome_group_name] [varchar](30) NOT NULL,
	[outcome_group_description] [varchar](250) NOT NULL,
	[field_name] [varchar](30) NOT NULL,
	[multiple_field_count] [numeric](2, 0) NOT NULL,
 CONSTRAINT [rif40_outcome_groups_pk] PRIMARY KEY CLUSTERED 
(
	[outcome_group_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_outcome_groups_type_fk] FOREIGN KEY([outcome_type])
	REFERENCES [rif40].[rif40_outcomes] ([outcome_type])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [outcome_type_ck2] CHECK  
	(([outcome_type]='BIRTHWEIGHT' OR [outcome_type]='OPCS' OR [outcome_type]='ICD-O' OR [outcome_type]='ICD' OR [outcome_type]='A&E'))
) ON [PRIMARY]
GO

--recreate foreign key references
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_conditions]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_inv_conditions]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_inv_conditons_ogn] FOREIGN KEY([outcome_group_name])
	REFERENCES [rif40].[rif40_outcome_groups] ([outcome_group_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[rif40_table_outcomes]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[rif40_table_outcomes]  WITH CHECK ADD  
	CONSTRAINT [rif40_outcome_group_name_fk] FOREIGN KEY([outcome_group_name])
	REFERENCES [rif40].[rif40_outcome_groups] ([outcome_group_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO

GRANT SELECT ON [rif40].[rif40_outcome_groups] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_outcome_groups] TO [rif_manager]
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Collection of Health outcomes into logical groups. E.g. Single variable ICD9 and 10' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcome_groups'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome type: ICD, ICD-0 or OPCS', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcome_groups', @level2type=N'COLUMN',@level2name=N'outcome_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Group Name. E.g SINGLE_VARIABLE_ICD', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcome_groups', @level2type=N'COLUMN',@level2name=N'outcome_group_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Group Description. E.g. &quot;Single variable ICD&quot;', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcome_groups', @level2type=N'COLUMN',@level2name=N'outcome_group_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome field name, e.g. ICD_SAHSU_01, ICD_SAHSU', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcome_groups', @level2type=N'COLUMN',@level2name=N'field_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Group multiple field count (0-99). E.g if NULL then field is ICD_SAHSU_01; if 20 then fields are ICD_SAHSU_01 to ICD_SAHSU_20. Field numbers are assumed to tbe left padded to 2 characters with &quot;0&quot; and preceeded by an &quot;_&quot;', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcome_groups', @level2type=N'COLUMN',@level2name=N'multiple_field_count'
GO
