
--drop if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_table_outcomes]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_table_outcomes]
END
GO

--table definition
CREATE TABLE [rif40].[rif40_table_outcomes](
	[outcome_group_name] [varchar](30) NOT NULL,
	[numer_tab] [varchar](30) NOT NULL,
	[current_version_start_year] [numeric](4, 0) NULL,
 CONSTRAINT [rif40_table_outcomes_pk] PRIMARY KEY CLUSTERED 
(
	[outcome_group_name] ASC,
	[numer_tab] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_outcome_numer_tab_fk] FOREIGN KEY([numer_tab])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [rif40_outcome_group_name_fk] FOREIGN KEY ([outcome_group_name])
      REFERENCES [rif40].[rif40_outcome_groups] ([outcome_group_name]) 
      ON UPDATE NO ACTION ON DELETE NO ACTION  
) ON [PRIMARY]
GO

--permissions
GRANT SELECT ON [rif40].[rif40_table_outcomes] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_table_outcomes] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Intersection of health outcomes in logical groups and Health tables. Mutliple groups supported per table' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_table_outcomes'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Group Name. E.g SINGLE_VARIABLE_ICD', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_table_outcomes', @level2type=N'COLUMN',@level2name=N'outcome_group_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table name.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_table_outcomes', @level2type=N'COLUMN',@level2name=N'numer_tab'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year of change from current version to previous version', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_table_outcomes', @level2type=N'COLUMN',@level2name=N'current_version_start_year'
GO
