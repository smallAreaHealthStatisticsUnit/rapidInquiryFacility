
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_covariates_level4]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif_data].[sahsuland_covariates_level4]
END
GO

CREATE TABLE [rif_data].[sahsuland_covariates_level4]
(
  year smallint NOT NULL, -- Year
  level4 varchar(20) NOT NULL, -- Level4
  ses smallint NOT NULL, -- Social Economic Status (quintiles)
  areatri1km smallint NOT NULL, -- Toxic Release Inventory within 1km of area (0=no/1=yes)
  near_dist float NOT NULL, -- Distance (m) from area centroid to nearest TRI site
  tri_1km smallint NOT NULL -- Toxic Release Inventory within 1km of areai centroid (0=no/1=yes)
);

GRANT SELECT ON [rif_data].[sahsuland_covariates_level4] TO [rif_user];
GRANT SELECT ON [rif_data].[sahsuland_covariates_level4] TO [rif_manager];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SAHSU land covariates - level4' , @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level4'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level4', @level2type=N'COLUMN',@level2name=N'year'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Level4', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level4', @level2type=N'COLUMN',@level2name=N'level4'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Social Economic Status (quintiles)', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level4', @level2type=N'COLUMN',@level2name=N'ses'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Toxic Release Inventory within 1km of area (0=no/1=yes)', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level4', @level2type=N'COLUMN',@level2name=N'areatri1km'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Distance (m) from area centroid to nearest TRI site', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level4', @level2type=N'COLUMN',@level2name=N'near_dist'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Toxic Release Inventory within 1km of areai centroid (0=no/1=yes)', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_covariates_level4', @level2type=N'COLUMN',@level2name=N'tri_1km'
GO

--index
CREATE UNIQUE INDEX sahsuland_covariates_level4_pk
  ON [rif_data].[sahsuland_covariates_level4] (year, level4);
  
GO
