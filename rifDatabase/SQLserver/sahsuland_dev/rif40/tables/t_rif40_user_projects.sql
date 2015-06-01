USE [sahsuland_dev]
GO

--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_user_projects]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_user_projects]
END
GO

--table definition
CREATE TABLE [rif40].[t_rif40_user_projects](
	[project] [varchar](30) NOT NULL,
	[username] [varchar](90) NOT NULL DEFAULT (SUSER_SNAME()),
	[grant_date] [datetime2](0) NOT NULL DEFAULT (sysdatetime()),
	[revoke_date] [datetime2](0) NULL,
	[rowid] [uniqueidentifier] NOT NULL,
 CONSTRAINT [t_rif40_user_projects_pk] PRIMARY KEY CLUSTERED 
(
	[project] ASC,
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_user_projects_project_fk] FOREIGN KEY ([project])
      REFERENCES [rif40].[t_rif40_projects] ([project]) 
      ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_user_projects_date_ck] CHECK 
	([revoke_date] IS NULL OR [revoke_date] > [grant_date])
) ON [PRIMARY]
GO

--permissions
GRANT SELECT ON [rif40].[t_rif40_user_projects] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_user_projects] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF project users' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_user_projects'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Project name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_user_projects', @level2type=N'COLUMN',@level2name=N'project'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_user_projects', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date project access granted', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_user_projects', @level2type=N'COLUMN',@level2name=N'grant_date'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date project access revoke_date', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_user_projects', @level2type=N'COLUMN',@level2name=N'revoke_date'
GO
