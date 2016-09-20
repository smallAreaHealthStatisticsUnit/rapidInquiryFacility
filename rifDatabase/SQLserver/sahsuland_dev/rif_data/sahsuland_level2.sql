USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_level2]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif_data].[sahsuland_level2]
END
GO

CREATE TABLE [rif_data].[sahsuland_level2]
(
  level2 varchar(6), -- level2
  name varchar(100), -- level2 name
  gid bigint -- Artificial primary key for RIF web interface
);

GRANT SELECT ON [rif_data].[sahsuland_level2] TO [public];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'2nd level of resolution lookup table' , @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level2'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level2', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level2', @level2type=N'COLUMN',@level2name=N'level2'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level2 name', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level2', @level2type=N'COLUMN',@level2name=N'name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Artificial primary key for RIF web interface', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_level2', @level2type=N'COLUMN',@level2name=N'gid'
GO

--index
CREATE UNIQUE INDEX sahsuland_level2_gid
  ON [rif_data].[sahsuland_level2](gid);

CREATE UNIQUE INDEX sahsuland_level2_pk
  ON [rif_data].[sahsuland_level2](level2);
 
GO