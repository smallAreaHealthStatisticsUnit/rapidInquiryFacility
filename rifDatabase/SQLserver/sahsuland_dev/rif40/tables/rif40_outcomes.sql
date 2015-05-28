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
 CONSTRAINT [rif40_outcomes_pk] PRIMARY KEY CLUSTERED 
(
	[outcome_type] ASC
),
CONSTRAINT [outcome_type_ck1] CHECK  
	(([outcome_type]='BIRTHWEIGHT' OR [outcome_type]='OPCS' OR [outcome_type]='ICD-O' OR [outcome_type]='ICD' OR [outcome_type]='A&E'))
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_outcomes] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_outcomes] TO [rif_manager]
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Health Outcomes. This table give the locations of the various Health Outcomes lookup tables, e.g RIF40_ICD10. Outcomes typically supported are ICD 9, 10, OPCS4 operation codes, ICD-0 Histology' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcomes'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome type: ICD, ICD-0 or OPCS', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcomes', @level2type=N'COLUMN',@level2name=N'outcome_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcomes', @level2type=N'COLUMN',@level2name=N'outcome_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Current Version, e.g 10 for ICD', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcomes', @level2type=N'COLUMN',@level2name=N'current_version'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Current Sub Version, e.g. 11th Revision - 2010', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcomes', @level2type=N'COLUMN',@level2name=N'current_sub_version'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Previous version, e.g. 9', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcomes', @level2type=N'COLUMN',@level2name=N'previous_version'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Previous sub version', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_outcomes', @level2type=N'COLUMN',@level2name=N'previous_sub_version'
GO
