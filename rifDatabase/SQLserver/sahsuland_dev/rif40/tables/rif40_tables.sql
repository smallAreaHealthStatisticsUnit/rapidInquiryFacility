USE [sahsuland_dev]
GO


--drop table if exists + foreign keys that reference table
/*
Referenced by 7 foriegn keys:
rif40_table_outcomes	rif40_outcome_numer_tab_fk
t_rif40_num_denom	t_rif40_num_denom_numer_fk
t_rif40_num_denom	t_rif40_num_denom_denom_fk
t_rif40_studies	t_rif40_stud_denom_tab_fk
t_rif40_studies	t_rif40_stud_direct_stand_fk
t_rif40_investigations	t_rif40_inv_numer_tab_fk
t_rif40_fdw_tables	t_rif40_fdw_tables_tn_fk
*/
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_tables]') AND type in (N'U'))
BEGIN

	--disable foreign keys that reference rif40_tables:
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[rif40_table_outcomes]') AND type in (N'U')
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='rif40_outcome_numer_tab_fk'))
	BEGIN
		ALTER TABLE [rif40].[rif40_table_outcomes] DROP CONSTRAINT [rif40_outcome_numer_tab_fk];
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_num_denom]') AND type in (N'U'))
	BEGIN
		IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name='t_rif40_num_denom_numer_fk')
			ALTER TABLE [rif40].[t_rif40_num_denom] DROP CONSTRAINT [t_rif40_num_denom_numer_fk];
		IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name='t_rif40_num_denom_denom_fk')
			ALTER TABLE [rif40].[t_rif40_num_denom] DROP CONSTRAINT [t_rif40_num_denom_denom_fk];
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_studies]') AND type in (N'U'))
	BEGIN
		IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name='t_rif40_stud_denom_tab_fk')
			ALTER TABLE [rif40].[t_rif40_studies] DROP CONSTRAINT [t_rif40_stud_denom_tab_fk];
		IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name='t_rif40_stud_direct_stand_fk')
			ALTER TABLE [rif40].[t_rif40_studies] DROP CONSTRAINT [t_rif40_stud_direct_stand_fk];
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_investigations]') AND type in (N'U')
		AND	EXISTS (SELECT * FROM sys.foreign_keys WHERE name='t_rif40_inv_numer_tab_fk'))
	BEGIN
		ALTER TABLE [rif40].[t_rif40_investigations] DROP CONSTRAINT [t_rif40_inv_numer_tab_fk];
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_fdw_tables]') AND type in (N'U')
		AND EXISTS (SELECT * FROM sys.foreign_keys WHERE name='t_rif40_fdw_tables_tn_fk'))
	BEGIN
		ALTER TABLE [rif40].[t_rif40_fdw_tables] DROP CONSTRAINT [t_rif40_fdw_tables_tn_fk];
	END;

	DROP TABLE [rif40].[rif40_tables];
END
GO

--table definition
CREATE TABLE [rif40].[rif40_tables](
	[theme] [varchar](30) NOT NULL,
	[table_name] [varchar](30) NOT NULL,
	[description] [varchar](250) NOT NULL,
	[year_start] [numeric](4, 0) NOT NULL,
	[year_stop] [numeric](4, 0) NOT NULL,
	[total_field] [varchar](30) NULL,
	[isindirectdenominator] [numeric](1, 0) NOT NULL,
	[isdirectdenominator] [numeric](1, 0) NOT NULL,
	[isnumerator] [numeric](1, 0) NOT NULL,
	[automatic] [numeric](1, 0) NOT NULL DEFAULT ((0)),
	[sex_field_name] [varchar](30) NULL,
	[age_group_field_name] [varchar](30) NULL,
	[age_sex_group_field_name] [varchar](30) NULL DEFAULT ('AGE_SEX_GROUP'),
	[age_group_id] [numeric](3, 0) NULL,
	[validation_date] [datetime2](0) NULL,
 CONSTRAINT [rif40_tables_pk] PRIMARY KEY CLUSTERED 
(
	[table_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_tables_age_group_id_fk] FOREIGN KEY([age_group_id])
	REFERENCES [rif40].[rif40_age_group_names] ([age_group_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [rif40_tables_theme_fk] FOREIGN KEY([theme])
	REFERENCES [rif40].[rif40_health_study_themes] ([theme])	
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [rif40_tab_asg_ck] CHECK  
	(([age_sex_group_field_name] IS NOT NULL AND [age_group_field_name] IS NULL AND [sex_field_name] IS NULL )
	OR ([age_sex_group_field_name] IS NULL AND [age_group_field_name] IS NOT NULL AND [sex_field_name] IS NOT NULL)),
CONSTRAINT [rif40_tab_automatic_ck] CHECK  
	([automatic]=(0) OR ([automatic]=(1) AND [isnumerator]=(1)) OR ([automatic]=(1) AND [isindirectdenominator]=(1))),
CONSTRAINT [rif40_tab_exclusive_ck] CHECK  
	(([isnumerator]=(1) AND [isdirectdenominator]=(0) AND [isindirectdenominator]=(0)) OR 
	([isnumerator]=(0) AND [isdirectdenominator]=(1) AND [isindirectdenominator]=(0)) OR 
	([isnumerator]=(0) AND [isdirectdenominator]=(0) AND [isindirectdenominator]=(1))),
CONSTRAINT [rif40_tab_isdirectdenom_ck] CHECK  
	(([isdirectdenominator]=(1) OR [isdirectdenominator]=(0)))	,
CONSTRAINT [rif40_tab_isindirectdenom_ck] CHECK  
	(([isindirectdenominator]=(1) OR [isindirectdenominator]=(0))),
CONSTRAINT [rif40_tab_isnumerator_ck] CHECK  
	(([isnumerator]=(1) OR [isnumerator]=(0))),
CONSTRAINT [rif40_tab_years_ck] CHECK  
	(([year_start]<=[year_stop]))
) ON [PRIMARY]
GO

--replace foreign keys
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[rif40_table_outcomes]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[rif40_table_outcomes]  WITH CHECK ADD  CONSTRAINT [rif40_outcome_numer_tab_fk] FOREIGN KEY([numer_tab])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_num_denom]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_num_denom]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_num_denom_numer_fk] FOREIGN KEY([numerator_table])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
	ALTER TABLE [rif40].[t_rif40_num_denom]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_num_denom_denom_fk] FOREIGN KEY([denominator_table])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_studies]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_studies]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_stud_denom_tab_fk] FOREIGN KEY([denom_tab])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
	ALTER TABLE [rif40].[t_rif40_studies]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_stud_direct_stand_fk] FOREIGN KEY([direct_stand_tab])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_investigations]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_investigations]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_inv_numer_tab_fk] FOREIGN KEY([numer_tab])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_fdw_tables]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_fdw_tables]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_fdw_tables_tn_fk] FOREIGN KEY([table_name])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO
	
--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_tables] TO [rif_manager]
GO
GRANT SELECT, REFERENCES ON [rif40].[rif40_tables] TO public
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF numerator and denominator tables' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Health Study theme', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'theme'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF table name. Normally the schema owner will not be able to see the health data tables, so no error is raised if the table cannot be resolved to an acceisble object. The schema owner must have access to automatic indirect standardisation denominators.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'table_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year table starts', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'year_start'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year table stops', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'year_stop'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Total field (when used aggregated tables)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'total_field'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is table a denominator to be used in indirect standardisation (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'isindirectdenominator'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is table a denominator to be used in direct standardisation (0/1). E.g. POP_WORLD, POP_EUROPE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'isdirectdenominator'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is table a numerator  (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'isnumerator'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having &gt;1 pair per numerator. This restriction is actually enforced in RIF40_NUM_DENOM because of the &quot;ORA-04091: table RIF40.RIF40_TABLES is mutating, trigger/function may not see it&quot; error. A user specific T_RIF40_NUM_DENOM is supplied for other combinations. The default is 0 because of the restrictions.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'automatic'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of SEX field. No default. AGE_GROUP_FIELD_NAME must be set, AGE_SEX_GROUP_FIELD_NAME must not be set.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'sex_field_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of AGE_GROUP field. No default. SEX_FIELD_NAME must be set, AGE_SEX_GROUP_FIELD_NAME must not be set.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'age_group_field_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of AGE_SEX_GROUP field. Default: AGE_SEX_GROUP; AGE_GROUP_FIELD_NAME and SEX_FIELD_NAME must not be set.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'age_sex_group_field_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Type of RIF age group in use. Link to RIF40_AGE_GROUP_NAMES. No default.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'age_group_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date table contents were validated OK.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables', @level2type=N'COLUMN',@level2name=N'validation_date'
GO
