USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_outcomes]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_outcomes]
END
GO


CREATE TABLE [rif40].[rif40_outcomes](
	[outcome_type] [varchar](20) NOT NULL,
	[outcome_description] [varchar](250) NOT NULL,
	[current_version] [varchar](20) NOT NULL,
	[current_sub_version] [varchar](20) NULL,
	[previous_version] [varchar](20) NULL,
	[previous_sub_version] [varchar](20) NULL,
	[current_lookup_table] [varchar](30) NULL,
	[previous_lookup_table] [varchar](30) NULL,
	[current_value_1char] [varchar](30) NULL,
	[current_value_2char] [varchar](30) NULL,
	[current_value_3char] [varchar](30) NULL,
	[current_value_4char] [varchar](30) NULL,
	[current_value_5char] [varchar](30) NULL,
	[current_description_1char] [varchar](250) NULL,
	[current_description_2char] [varchar](250) NULL,
	[current_description_3char] [varchar](250) NULL,
	[current_description_4char] [varchar](250) NULL,
	[current_description_5char] [varchar](250) NULL,
	[previous_value_1char] [varchar](30) NULL,
	[previous_value_2char] [varchar](30) NULL,
	[previous_value_3char] [varchar](30) NULL,
	[previous_value_4char] [varchar](30) NULL,
	[previous_value_5char] [varchar](30) NULL,
	[previous_description_1char] [varchar](250) NULL,
	[previous_description_2char] [varchar](250) NULL,
	[previous_description_3char] [varchar](250) NULL,
	[previous_description_4char] [varchar](250) NULL,
	[previous_description_5char] [varchar](250) NULL,
 CONSTRAINT [rif40_outcomes_pk] PRIMARY KEY CLUSTERED 
(
	[outcome_type] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [current_lookup_table_ck] CHECK  
	(([current_lookup_table] IS NOT NULL AND [current_version] IS NOT NULL 
	AND ([current_value_1char] IS NOT NULL OR [current_value_2char] IS NOT NULL OR [current_value_3char] IS NOT NULL 
	OR [current_value_4char] IS NOT NULL OR [current_value_5char] IS NOT NULL) OR [current_lookup_table] IS NULL)),
CONSTRAINT [current_value_nchar_ck] CHECK  
	(([current_lookup_table] IS NOT NULL AND [current_version] IS NOT NULL AND ([current_value_1char] IS NOT NULL 
	AND [current_description_1char] IS NOT NULL OR [current_value_2char] IS NOT NULL AND [current_description_2char] IS NOT NULL 
	OR [current_value_3char] IS NOT NULL AND [current_description_3char] IS NOT NULL OR [current_value_4char] IS NOT NULL 
	AND [current_description_4char] IS NOT NULL OR [current_value_5char] IS NOT NULL AND [current_description_5char] IS NOT NULL) 
	OR [current_lookup_table] IS NULL)),
CONSTRAINT [outcome_type_ck1] CHECK  
	(([outcome_type]='BIRTHWEIGHT' OR [outcome_type]='OPCS' OR [outcome_type]='ICD-O' OR [outcome_type]='ICD' OR [outcome_type]='A&E')),
CONSTRAINT [previous_lookup_table_ck] CHECK  
	(([previous_lookup_table] IS NOT NULL AND [previous_version] IS NOT NULL AND 
	([previous_value_1char] IS NOT NULL OR [previous_value_2char] IS NOT NULL OR [previous_value_3char] IS NOT NULL OR 
	[previous_value_4char] IS NOT NULL OR [previous_value_5char] IS NOT NULL) OR [previous_lookup_table] IS NULL AND [previous_version] IS NULL)),
CONSTRAINT [previous_value_nchar_ck] CHECK  
	(([previous_lookup_table] IS NOT NULL AND ([previous_value_1char] IS NOT NULL AND [previous_description_1char] IS NOT NULL
	OR [previous_value_2char] IS NOT NULL AND [previous_description_2char] IS NOT NULL OR [previous_value_3char] IS NOT NULL 
	AND [previous_description_3char] IS NOT NULL OR [previous_value_4char] IS NOT NULL AND [previous_description_4char] IS NOT NULL 
	OR [previous_value_5char] IS NOT NULL AND [previous_description_5char] IS NOT NULL) OR [previous_lookup_table] IS NULL 
	AND [previous_version] IS NULL))
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_outcomes] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_outcomes] TO [rif_manager]
GO

/*
COMMENT ON TABLE rif40_outcomes
  IS 'Health Outcomes. This table give the locations of the various Health Outcomes lookup tables, e.g RIF40_ICD10. Outcomes typically supported are ICD 9, 10, OPCS4 operation codes, ICD-0 Histology';
COMMENT ON COLUMN rif40_outcomes.outcome_type IS 'Outcome type: ICD, ICD-0 or OPCS';
COMMENT ON COLUMN rif40_outcomes.outcome_description IS 'Outcome Description';
COMMENT ON COLUMN rif40_outcomes.current_version IS 'Current Version, e.g 10 for ICD';
COMMENT ON COLUMN rif40_outcomes.current_sub_version IS 'Current Sub Version, e.g. 11th Revision - 2010';
COMMENT ON COLUMN rif40_outcomes.previous_version IS 'Previous version, e.g. 9';
COMMENT ON COLUMN rif40_outcomes.previous_sub_version IS 'Previous sub version';
COMMENT ON COLUMN rif40_outcomes.current_lookup_table IS 'Current lookup table, e.g. RIF40_ICD10. If this is NULL there is no lookup table';
COMMENT ON COLUMN rif40_outcomes.previous_lookup_table IS 'Previous lookup table, e.g. RIF40_ICD9';
COMMENT ON COLUMN rif40_outcomes.current_value_1char IS 'Field name containing values for current version 1 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.current_value_2char IS 'Field name containing values for current version 2 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.current_value_3char IS 'Field name containing values for current version 3 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.current_value_4char IS 'Field name containing values for current version 4 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.current_value_5char IS 'Field name containing values for current version 5 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.current_description_1char IS 'Field name contianing descriptions for current version 1 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.current_description_2char IS 'Field name contianing descriptions for current version 2 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.current_description_3char IS 'Field name contianing descriptions for current version 3 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.current_description_4char IS 'Field name contianing descriptions for current version 4 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.current_description_5char IS 'Field name contianing descriptions for current version 5 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_value_1char IS 'Field name containing values for previous version 1 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_value_2char IS 'Field name containing values for previous version 2 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_value_3char IS 'Field name containing values for previous version 3 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_value_4char IS 'Field name containing values for previous version 4 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_value_5char IS 'Field name containing values for previous version 5 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_description_1char IS 'Field name contianing descriptions for previous version 1 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_description_2char IS 'Field name contianing descriptions for previous version 2 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_description_3char IS 'Field name contianing descriptions for previous version 3 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_description_4char IS 'Field name contianing descriptions for previous version 4 character code (may be NULL)';
COMMENT ON COLUMN rif40_outcomes.previous_description_5char IS 'Field name contianing descriptions for previous version 5 character code (may be NULL)';
*/

--triggers


