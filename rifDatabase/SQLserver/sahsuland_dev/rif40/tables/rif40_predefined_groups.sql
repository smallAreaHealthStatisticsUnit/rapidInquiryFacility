USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_predefined_groups]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_predefined_groups]
END
GO

CREATE TABLE [rif40].[rif40_predefined_groups](
	[predefined_group_name] [varchar](30) NOT NULL,
	[predefined_group_description] [varchar](250) NOT NULL,
	[outcome_type] [varchar](20) NOT NULL,
	[condition] [varchar](4000) NOT NULL DEFAULT ('1=1'),
	[rowid] [uniqueidentifier] NOT NULL DEFAULT (newid()),
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

GRANT SELECT ON [rif40].[rif40_predefined_groups] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_predefined_groups] TO [rif_manager]
GO

/*
COMMENT ON TABLE rif40_predefined_groups
  IS 'Predefined Health Outcomes';
COMMENT ON COLUMN rif40_predefined_groups.predefined_group_name IS 'Predefined Group Name. E.g LUNG_CANCER';
COMMENT ON COLUMN rif40_predefined_groups.predefined_group_description IS 'Predefined Group Description. E.g. &quot;Lung Cancer&quot;';
COMMENT ON COLUMN rif40_predefined_groups.outcome_type IS 'Outcome type: ICD, ICD-0 or OPCS';
COMMENT ON COLUMN rif40_predefined_groups.condition IS 'SQL WHERE clause, with the WHERE keyword omitted). Default to 1=1 (use all records matching year/age/sex criteria). Checked for SQL injection.';
*/

--triggers NOT INCLUDED!!