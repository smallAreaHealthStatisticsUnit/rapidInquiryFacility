USE [sahsuland_dev]
GO


	
--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_predefined_groups]') AND type in (N'U'))
BEGIN

	--disable foreign key references
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_conditions]') AND type in (N'U'))
	BEGIN
		ALTER TABLE [rif40].[t_rif40_inv_conditions] DROP CONSTRAINT [t_rif40_inv_conditons_pgn];
	END;

	DROP TABLE [rif40].[rif40_predefined_groups];
END
GO

--table definition
CREATE TABLE [rif40].[rif40_predefined_groups](
	[predefined_group_name] [varchar](30) NOT NULL,
	[predefined_group_description] [varchar](250) NOT NULL,
	[outcome_type] [varchar](20) NOT NULL,
	[condition] [varchar](4000) NOT NULL DEFAULT ('1=1'),
 CONSTRAINT [rif40_predefined_groups_pk] PRIMARY KEY CLUSTERED 
(
	[predefined_group_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_predefined_type_fk] FOREIGN KEY([outcome_type])
	REFERENCES [rif40].[rif40_outcomes] ([outcome_type])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [outcome_type_ck3] CHECK  
	(([outcome_type]='BIRTHWEIGHT' OR [outcome_type]='OPCS' OR [outcome_type]='ICD-O' OR [outcome_type]='ICD' OR [outcome_type]='A&E'))
) ON [PRIMARY]
GO

--recreate foreign key reference
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_conditions]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_inv_conditions]  WITH CHECK ADD  CONSTRAINT [t_rif40_inv_conditons_pgn] FOREIGN KEY([predefined_group_name])
	REFERENCES [rif40].[rif40_predefined_groups] ([predefined_group_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO
	
--permissions
GRANT SELECT ON [rif40].[rif40_predefined_groups] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_predefined_groups] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Predefined Health Outcomes' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_predefined_groups'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Predefined Group Name. E.g LUNG_CANCER', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_predefined_groups', @level2type=N'COLUMN',@level2name=N'predefined_group_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Predefined Group Description. E.g. &quot;Lung Cancer&quot;', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_predefined_groups', @level2type=N'COLUMN',@level2name=N'predefined_group_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome type: ICD, ICD-0 or OPCS', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_predefined_groups', @level2type=N'COLUMN',@level2name=N'outcome_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SQL WHERE clause, with the WHERE keyword omitted). Default to 1=1 (use all records matching year/age/sex criteria). Checked for SQL injection.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_predefined_groups', @level2type=N'COLUMN',@level2name=N'condition'
GO

