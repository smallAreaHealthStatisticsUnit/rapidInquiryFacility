USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_table_outcomes]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_table_outcomes]
END
GO

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

GRANT SELECT ON [rif40].[rif40_table_outcomes] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_table_outcomes] TO [rif_manager]
GO

/*
COMMENT ON TABLE rif40_table_outcomes
  IS 'Intersection of health outcomes in logical groups and Health tables. Mutliple groups supported per table';
COMMENT ON COLUMN rif40_table_outcomes.outcome_group_name IS 'Outcome Group Name. E.g SINGLE_VARIABLE_ICD';
COMMENT ON COLUMN rif40_table_outcomes.numer_tab IS 'Numerator table name.';
COMMENT ON COLUMN rif40_table_outcomes.current_version_start_year IS 'Year of change from current version to previous version';
*/

--triggers
