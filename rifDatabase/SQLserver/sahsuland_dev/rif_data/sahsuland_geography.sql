USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_geography]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif_data].[sahsuland_geography]
END
GO


CREATE TABLE [rif_data].sahsuland_geography
(
  level1 varchar(100), -- Lowest level of resolution
  level2 varchar(100), -- 2nd level of resolution
  level3 varchar(100), -- 3rd level of resolution
  level4 varchar(100) -- Highest level of resolution
);

GRANT SELECT ON [rif_data].[sahsuland_geography] TO [public];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SAHSU example database geo-level hierarchy table' , @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lowest level of resolution', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_geography', @level2type=N'COLUMN',@level2name=N'level1'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'2nd level of resolution', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_geography', @level2type=N'COLUMN',@level2name=N'level2'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'3rd level of resolution', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_geography', @level2type=N'COLUMN',@level2name=N'level3'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Highest level of resolution', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_geography', @level2type=N'COLUMN',@level2name=N'level4'
GO

--indexes

CREATE INDEX level1_idx1
  ON [rif_data].[sahsuland_geography](level1);
  
CREATE INDEX level2_idx2
  ON [rif_data].[sahsuland_geography](level2);

CREATE INDEX level3_idx3
  ON [rif_data].[sahsuland_geography](level3);

CREATE UNIQUE INDEX level4_idx4
  ON [rif_data].[sahsuland_geography](level4);
 
