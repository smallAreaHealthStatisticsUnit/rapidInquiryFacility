USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_outcome_groups]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_outcome_groups]
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

GRANT SELECT ON [rif40].[rif40_outcome_groups] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_outcome_groups] TO [rif_manager]
GO

/*
COMMENT ON TABLE rif40_outcome_groups
  IS 'Collection of Health outcomes into logical groups. E.g. Single variable ICD9 and 10';
COMMENT ON COLUMN rif40_outcome_groups.outcome_type IS 'Outcome type: ICD, ICD-0 or OPCS';
COMMENT ON COLUMN rif40_outcome_groups.outcome_group_name IS 'Outcome Group Name. E.g SINGLE_VARIABLE_ICD';
COMMENT ON COLUMN rif40_outcome_groups.outcome_group_description IS 'Outcome Group Description. E.g. &quot;Single variable ICD&quot;';
COMMENT ON COLUMN rif40_outcome_groups.field_name IS 'Outcome field name, e.g. ICD_SAHSU_01, ICD_SAHSU';
COMMENT ON COLUMN rif40_outcome_groups.multiple_field_count IS 'Outcome Group multiple field count (0-99). E.g if NULL then field is ICD_SAHSU_01; if 20 then fields are ICD_SAHSU_01 to ICD_SAHSU_20. Field numbers are assumed to tbe left padded to 2 characters with &quot;0&quot; and preceeded by an &quot;_&quot;';
*/
