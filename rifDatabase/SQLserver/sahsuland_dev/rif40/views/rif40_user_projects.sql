
--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_user_projects]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_user_projects]
END
GO

--view definition
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
  WHERE a.project = b.project AND (a.username= SUSER_SNAME() 
  OR  IS_MEMBER(N'[rif_manager]') = 1)
GO

--permissions
GRANT SELECT ON [rif40].[rif40_user_projects] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_user_projects] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF projects' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_projects'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Project name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_projects', @level2type=N'COLUMN',@level2name=N'project'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_projects', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date project access granted', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_projects', @level2type=N'COLUMN',@level2name=N'grant_date'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date project access revoke_date', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_projects', @level2type=N'COLUMN',@level2name=N'revoke_date'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_projects', @level2type=N'COLUMN',@level2name=N'description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_projects', @level2type=N'COLUMN',@level2name=N'date_started'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_projects', @level2type=N'COLUMN',@level2name=N'date_ended'
GO

