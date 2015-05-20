USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_tables]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_tables]
END
GO


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
	[rowid] [uniqueidentifier] NOT NULL DEFAULT (newid()),
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
	(([age_sex_group_field_name] IS NOT NULL AND [age_group_field_name] IS NULL AND [sex_field_name] IS NULL 
	OR [age_sex_group_field_name] IS NULL AND [age_group_field_name] IS NOT NULL AND [sex_field_name] IS NOT NULL)),
CONSTRAINT [rif40_tab_automatic_ck] CHECK  
	(([automatic]=(0) OR [automatic]=(1) AND [isnumerator]=(1) OR [automatic]=(1) AND [isindirectdenominator]=(1))),
CONSTRAINT [rif40_tab_exclusive_ck] CHECK  
	(([isnumerator]=(1) AND [isdirectdenominator]=(0) AND [isindirectdenominator]=(0) OR [isnumerator]=(0) 
	AND [isdirectdenominator]=(1) AND [isindirectdenominator]=(0) OR [isnumerator]=(0) AND [isdirectdenominator]=(0) 
	AND [isindirectdenominator]=(1))),
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

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_tables] TO [rif_manager]
GO
GRANT SELECT, REFERENCES ON [rif40].[rif40_tables] TO public
GO

/*
COMMENT ON TABLE rif40_tables
  IS 'RIF numerator and denominator tables';
COMMENT ON COLUMN rif40_tables.theme IS 'Health Study theme';
COMMENT ON COLUMN rif40_tables.table_name IS 'RIF table name. Normally the schema owner will not be able to see the health data tables, so no error is raised if the table cannot be resolved to an acceisble object. The schema owner must have access to automatic indirect standardisation denominators.';
COMMENT ON COLUMN rif40_tables.description IS 'Description';
COMMENT ON COLUMN rif40_tables.year_start IS 'Year table starts';
COMMENT ON COLUMN rif40_tables.year_stop IS 'Year table stops';
COMMENT ON COLUMN rif40_tables.total_field IS 'Total field (when used aggregated tables)';
COMMENT ON COLUMN rif40_tables.isindirectdenominator IS 'Is table a denominator to be used in indirect standardisation (0/1)';
COMMENT ON COLUMN rif40_tables.isdirectdenominator IS 'Is table a denominator to be used in direct standardisation (0/1). E.g. POP_WORLD, POP_EUROPE.';
COMMENT ON COLUMN rif40_tables.isnumerator IS 'Is table a numerator  (0/1)';
COMMENT ON COLUMN rif40_tables.automatic IS 'Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having &gt;1 pair per numerator. This restriction is actually enforced in RIF40_NUM_DENOM because of the &quot;ORA-04091: table RIF40.RIF40_TABLES is mutating, trigger/function may not see it&quot; error. A user specific T_RIF40_NUM_DENOM is supplied for other combinations. The default is 0 because of the restrictions.';
COMMENT ON COLUMN rif40_tables.sex_field_name IS 'Name of SEX field. No default. AGE_GROUP_FIELD_NAME must be set, AGE_SEX_GROUP_FIELD_NAME must not be set.';
COMMENT ON COLUMN rif40_tables.age_group_field_name IS 'Name of AGE_GROUP field. No default. SEX_FIELD_NAME must be set, AGE_SEX_GROUP_FIELD_NAME must not be set.';
COMMENT ON COLUMN rif40_tables.age_sex_group_field_name IS 'Name of AGE_SEX_GROUP field. Default: AGE_SEX_GROUP; AGE_GROUP_FIELD_NAME and SEX_FIELD_NAME must not be set.';
COMMENT ON COLUMN rif40_tables.age_group_id IS 'Type of RIF age group in use. Link to RIF40_AGE_GROUP_NAMES. No default.';
COMMENT ON COLUMN rif40_tables.validation_date IS 'Date table contents were validated OK.';
*/

--triggers MISSING
