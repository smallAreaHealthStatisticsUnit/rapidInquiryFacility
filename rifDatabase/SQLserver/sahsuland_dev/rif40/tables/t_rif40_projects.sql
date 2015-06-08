USE [sahsuland_dev]
GO

--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_projects]') AND type in (N'U'))
BEGIN

	--drop foreign key references
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_studies]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='rif40_studies_project_fk')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_studies] DROP CONSTRAINT [rif40_studies_project_fk];
	END;
	
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_user_projects]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='rif40_user_projects_project_fk')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_user_projects] DROP CONSTRAINT [rif40_user_projects_project_fk];
	END;
	
	DROP TABLE [rif40].[t_rif40_projects]
END
GO

--table definition
CREATE TABLE [rif40].[t_rif40_projects](
	[project] [varchar](30) NOT NULL,
	[description] [varchar](250) NOT NULL,
	[date_started] [datetime2](0) DEFAULT (sysdatetime()),
	[date_ended] [datetime2](0) NULL,
 CONSTRAINT [t_rif40_projects_pk] PRIMARY KEY CLUSTERED 
(
	[project] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_projects_date_ck] CHECK  
	(([date_ended] IS NULL OR [date_ended]>=[date_started]))
) ON [PRIMARY]
GO

--recreate foreign keys
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_studies]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_studies]  WITH CHECK ADD  
	CONSTRAINT [rif40_studies_project_fk] FOREIGN KEY([project])
	REFERENCES [rif40].[t_rif40_projects] ([project])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END;

IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_user_projects]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_user_projects]  WITH CHECK ADD  
	CONSTRAINT [rif40_user_projects_project_fk] FOREIGN KEY([project])
	REFERENCES [rif40].[t_rif40_projects] ([project])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END;	

--permissions
GRANT SELECT ON [rif40].[t_rif40_projects] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_projects] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF projects' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_projects'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Project name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_projects', @level2type=N'COLUMN',@level2name=N'project'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Project description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_projects', @level2type=N'COLUMN',@level2name=N'description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date project started', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_projects', @level2type=N'COLUMN',@level2name=N'date_started'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date project ended', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_projects', @level2type=N'COLUMN',@level2name=N'date_ended'
GO
