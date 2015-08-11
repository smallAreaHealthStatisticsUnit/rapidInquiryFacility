USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_level4]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif_data].[sahsuland_level4]
END
GO

CREATE TABLE [rif_data].[sahsuland_level4]
(
  level4 varchar(15), -- level4
  name varchar(100), -- level4 name
  gid bigint -- Artificial primary key for RIF web interface
);

GRANT SELECT ON [rif_data].[sahsuland_level4] TO [public];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'2nd level of resolution lookup table' , @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level4'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level4', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level4', @level2type=N'COLUMN',@level2name=N'level4'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level4 name', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level4', @level2type=N'COLUMN',@level2name=N'name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Artificial primary key for RIF web interface', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level4', @level2type=N'COLUMN',@level2name=N'gid'
GO

--index
CREATE UNIQUE INDEX sahsuland_level4_gid
  ON [rif_data].[sahsuland_level4](gid);

CREATE UNIQUE INDEX sahsuland_level4_pk
  ON [rif_data].[sahsuland_level4](level4);
 
CREATE UNIQUE INDEX sahsuland_level4_uk2
  ON [rif_data].[sahsuland_level4](name);
  