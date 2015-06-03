USE [sahsuland_dev]
GO

--drop table if already exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_age_groups]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_version]
END
GO

--table definition
CREATE TABLE [rif40].[rif40_version](
	[version] [varchar](50) NOT NULL ,
	[schema_created] [datetime2](0) NOT NULL DEFAULT (sysdatetime()),
	[schema_amended] [datetime2](0) NULL,
	[cvs_revision] [varchar](50) NOT NULL
) ON [PRIMARY]
GO

--permissions
GRANT SELECT ON [rif40].[rif40_version] TO [public]
GO
GRANT UPDATE ON [rif40].[rif40_version] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF version' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_version'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Version. Used for change control to ensure front end matches database.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_version', @level2type=N'COLUMN',@level2name=N'version'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date schema created', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_version', @level2type=N'COLUMN',@level2name=N'schema_created'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date schema amended', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_version', @level2type=N'COLUMN',@level2name=N'schema_amended'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'CVS revison control information for last amendment', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_version', @level2type=N'COLUMN',@level2name=N'cvs_revision'
GO
