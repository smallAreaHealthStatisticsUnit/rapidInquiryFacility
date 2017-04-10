
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_health_study_themes]') AND type in (N'U'))
BEGIN

	--first disable foreign key references
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[rif40_tables]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='rif40_tables_theme_fk')
	BEGIN
		ALTER TABLE [rif40].[rif40_tables] DROP CONSTRAINT [rif40_tables_theme_fk];
	END;
	
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

--recreate foreign key references
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[rif40_tables]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[rif40_tables]  WITH CHECK ADD  
	CONSTRAINT [rif40_tables_theme_fk] FOREIGN KEY([theme])
	REFERENCES [rif40].[rif40_health_study_themes] ([theme])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO


GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_health_study_themes] TO [rif_manager]
GO

GRANT SELECT, REFERENCES ON [rif40].[rif40_health_study_themes] TO [rif_user] WITH GRANT OPTION;
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF health study themes' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_health_study_themes'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Theme', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_health_study_themes', @level2type=N'COLUMN',@level2name=N'theme'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_health_study_themes', @level2type=N'COLUMN',@level2name=N'description'
GO
