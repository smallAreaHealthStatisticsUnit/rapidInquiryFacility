
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_covariates_level3]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif_data].[sahsuland_covariates_level3]
END
GO


CREATE TABLE [rif_data].[sahsuland_covariates_level3]
(
  year smallint NOT NULL, -- Year
  level3 varchar(20) NOT NULL, -- Level3
  ses smallint NOT NULL, -- Social Economic Status (quintiles)
  ethnicity smallint NOT NULL -- Ethnicity % non white - 1: <5%, 2: 5 to 10%, 3: >= 10%
);


GRANT SELECT ON [rif_data].[sahsuland_covariates_level3] TO [rif_user];
GRANT SELECT ON [rif_data].[sahsuland_covariates_level3] TO [rif_manager];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SAHSU land covariates - level3' , @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level3'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level3', @level2type=N'COLUMN',@level2name=N'year'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Level3', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level3', @level2type=N'COLUMN',@level2name=N'level3'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Social Economic Status (quintiles)', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level3', @level2type=N'COLUMN',@level2name=N'ses'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Ethnicity % non white - 1: <5%, 2: 5 to 10%, 3: >= 10%', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level3', @level2type=N'COLUMN',@level2name=N'ethnicity'
GO

CREATE UNIQUE INDEX sahsuland_covariates_level3_pk
  ON [rif_data].[sahsuland_covariates_level3](year, level3);

GO  
