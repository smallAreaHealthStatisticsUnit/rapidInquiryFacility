USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_level1]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif_data].[sahsuland_level1]
END
GO


CREATE TABLE [rif_data].[sahsuland_level1]
(
  level1 [varchar](5), -- level1
  name [varchar](100), -- level1 name
  gid [bigint] -- Artificial primary key for RIF web interface
);

GRANT SELECT ON [rif_data].[sahsuland_level1] TO [public];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lowest level of resolution lookup table' , @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level1'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level1', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level1', @level2type=N'COLUMN',@level2name=N'level1'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level1 name', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level1', @level2type=N'COLUMN',@level2name=N'name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Artificial primary key for RIF web interface', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level1', @level2type=N'COLUMN',@level2name=N'gid'
GO

--indexes
CREATE UNIQUE INDEX sahsuland_level1_gid
  ON [rif_data].[sahsuland_level1] (gid);

CREATE UNIQUE INDEX sahsuland_level1_pk
  ON rif_data.sahsuland_level1(level1);

 CREATE UNIQUE INDEX sahsuland_level1_uk2
  ON rif_data.sahsuland_level1(name);
