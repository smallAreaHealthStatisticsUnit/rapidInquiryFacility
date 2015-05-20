USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_user_projects]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_user_projects]
END
GO


CREATE TABLE [rif40].[t_rif40_user_projects](
	[project] [varchar](30) NOT NULL,
	[username] [varchar](90) NOT NULL,
	[grant_date] [datetime2](0) NOT NULL,
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

GRANT SELECT ON [rif40].[t_rif40_user_projects] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_user_projects] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_user_projects
  IS 'RIF project users';
COMMENT ON COLUMN t_rif40_user_projects.project IS 'Project name';
COMMENT ON COLUMN t_rif40_user_projects.username IS 'Username';
COMMENT ON COLUMN t_rif40_user_projects.grant_date IS 'Date project access granted';
COMMENT ON COLUMN t_rif40_user_projects.revoke_date IS 'Date project access revoke_date';
*/

--trigger