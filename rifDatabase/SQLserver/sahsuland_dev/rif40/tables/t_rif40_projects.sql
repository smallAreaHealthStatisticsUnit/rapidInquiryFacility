USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_projects]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_projects]
END
GO

CREATE TABLE [rif40].[t_rif40_projects](
	[project] [varchar](30) NOT NULL,
	[description] [varchar](250) NOT NULL,
	[date_started] [datetime2](0) NOT NULL DEFAULT (sysdatetime()),
	[date_ended] [datetime2](0) NULL,
 CONSTRAINT [t_rif40_projects_pk] PRIMARY KEY CLUSTERED 
(
	[project] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_projects_date_ck] CHECK  
	(([date_ended] IS NULL OR [date_ended]>=[date_started]))
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[t_rif40_projects] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_projects] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_projects
  IS 'RIF projects';
COMMENT ON COLUMN t_rif40_projects.project IS 'Project name';
COMMENT ON COLUMN t_rif40_projects.description IS 'Project description';
COMMENT ON COLUMN t_rif40_projects.date_started IS 'Date project started';
COMMENT ON COLUMN t_rif40_projects.date_ended IS 'Date project ended';
*/
