USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_level3]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif_data].[sahsuland_level3]
END
GO

CREATE TABLE [rif_data].[sahsuland_level3]
(
  level3 varchar(6), -- level3
  name varchar(100), -- level3 name
  gid bigint -- Artificial primary key for RIF web interface
);

GRANT SELECT ON [rif_data].[sahsuland_level3] TO [public];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'2nd level of resolution lookup table' , @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level3'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level3', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level3', @level2type=N'COLUMN',@level2name=N'level3'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level3 name', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level3', @level2type=N'COLUMN',@level2name=N'name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Artificial primary key for RIF web interface', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level3', @level2type=N'COLUMN',@level2name=N'gid'
GO

--index
CREATE UNIQUE INDEX sahsuland_level3_gid
  ON [rif_data].[sahsuland_level3](gid);

CREATE UNIQUE INDEX sahsuland_level3_pk
  ON [rif_data].[sahsuland_level3](level3);
 
CREATE UNIQUE INDEX sahsuland_level3_uk2
  ON [rif_data].[sahsuland_level3](name);
  