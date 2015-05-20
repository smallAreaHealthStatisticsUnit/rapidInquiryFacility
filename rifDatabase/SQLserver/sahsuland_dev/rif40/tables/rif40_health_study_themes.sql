USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_health_study_themes]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_health_study_themes]
END
GO


CREATE TABLE [rif40].[rif40_health_study_themes](
	[theme] [varchar](30) NOT NULL,
	[description] [varchar](200) NOT NULL,
 CONSTRAINT [rif40_health_study_themes_pk] PRIMARY KEY CLUSTERED 
(
	[theme] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_health_study_themes] TO [rif_manager]
GO
GRANT SELECT, REFERENCES ON [rif40].[rif40_health_study_themes] TO public
GO

