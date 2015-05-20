USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_age_groups]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_version]
END
GO

CREATE TABLE [rif40].[rif40_version](
	[version] [varchar](50) NOT NULL DEFAULT (sysdatetime()),
	[schema_created] [datetime2](0) NOT NULL,
	[schema_amended] [datetime2](0) NULL,
	[cvs_revision] [varchar](50) NOT NULL,
	[rowid] [uniqueidentifier] NOT NULL DEFAULT (newid())
) ON [PRIMARY]
GO

--cannot grant anything to entity owner:
--GRANT ALL ON [rif40].[rif40_version] TO [rif40]
--GO
GRANT SELECT ON [rif40].[rif40_version] TO [public]
GO
GRANT UPDATE ON [rif40].[rif40_version] TO [rif_manager]
GO

/*
EXEC rif40_version_trigger.sql
GO
*/