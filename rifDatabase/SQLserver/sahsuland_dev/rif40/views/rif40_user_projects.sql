USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_user_projects]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_user_projects]
END
GO

CREATE  VIEW [rif40].[rif40_user_projects] AS 
 SELECT a.project,
    a.username,
    a.grant_date,
    a.revoke_date,
    b.description,
    b.date_started,
    b.date_ended
   FROM [rif40].[t_rif40_user_projects] a,
    [rif40].[t_rif40_projects] b
  WHERE a.project = b.project AND a.username= SUSER_SNAME() 
  OR  IS_MEMBER(N'[rif_manager]') = 1
GO

GRANT SELECT ON [rif40].[rif40_user_projects] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_user_projects] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_user_projects
  IS 'RIF project users';
COMMENT ON COLUMN rif40_user_projects.project IS 'Project name';
COMMENT ON COLUMN rif40_user_projects.username IS 'Username';
COMMENT ON COLUMN rif40_user_projects.grant_date IS 'Date project access granted';
COMMENT ON COLUMN rif40_user_projects.revoke_date IS 'Date project access revoke_date';
COMMENT ON COLUMN rif40_user_projects.description IS 'N/A';
COMMENT ON COLUMN rif40_user_projects.date_started IS 'N/A';
COMMENT ON COLUMN rif40_user_projects.date_ended IS 'N/A';
*/
